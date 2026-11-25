"""End-to-end fall detection pipeline: video → annotated output + JSON log."""

import json
import logging
import signal
import threading
import time
from datetime import datetime
from pathlib import Path

import numpy as np
import torch
from tqdm import tqdm

from models.fall_detector import FallDetector
from src.detector import PersonDetector
from src.fall_state import FallStateGate
from src.feature_extraction import FeatureExtractor
from src.feature_extraction_v2 import FeatureExtractorV2
from src.model_config import resolve_model_config
from src.incident_manager import FallIncidentManager
from src.person_identity import LogicalPersonResolver, PersonObservation
from src.track_manager import TrackManager
from src.utils import (
    COLOR_GREEN,
    COLOR_ORANGE,
    COLOR_RED,
    draw_alert,
    draw_skeleton,
    draw_status_bar,
)
from src.video_io import VideoReader, VideoWriter, format_timestamp

logger = logging.getLogger(__name__)

# State colors
STATE_COLORS = {
    "normal": COLOR_GREEN,
    "falling": COLOR_RED,
    "fallen": COLOR_ORANGE,
}


def _label_scale(scale: float) -> float:
    """Keep person labels readable without growing with high-resolution frames."""
    return min(float(scale), 1.0)


def _identity_is_confirmed(
    raw_track_id: int,
    logical_ids: dict[int, int],
    ambiguous_raw_ids: set[int],
) -> bool:
    """Return whether a detection can safely inherit a person's history."""
    return raw_track_id in logical_ids and raw_track_id not in ambiguous_raw_ids


def _fall_label_is_allowed(
    person_id: int, incident_person_id: int, target_status: str
) -> bool:
    """Only draw FALLEN on the confirmed target of an active event."""
    return person_id == incident_person_id and target_status in {
        "target_observed",
        "target_reconnected",
    }


class FallDetectionPipeline:
    """Full pipeline: video → YOLO detection → tracking → features → LSTM → output."""

    def __init__(
        self,
        yolo_model: str = "yolo11n-pose.pt",
        lstm_checkpoint: str = "models/checkpoints/best.pth",
        device: str | None = None,
        confidence_threshold: float = 0.40,
        cooldown_frames: int = 90,
        tracker_config: str = "configs/botsort.yaml",
    ):
        if device is None:
            self.device = "cuda" if torch.cuda.is_available() else "cpu"
        else:
            self.device = device

        logger.info(f"Initializing pipeline on {self.device}...")

        # YOLO detector
        self.detector = PersonDetector(
            model_name=yolo_model,
            device=self.device,
            confidence_threshold=0.3,
            tracker_config=tracker_config,
        )

        # LSTM classifier and feature extractor must agree on checkpoint width.
        ckpt = torch.load(lstm_checkpoint, map_location=self.device, weights_only=False)
        feature_version, input_size, self.class_names = resolve_model_config(ckpt)
        self.lstm = FallDetector(input_size=input_size, num_classes=len(self.class_names))
        self.lstm.load_state_dict(ckpt["model_state_dict"])
        self.lstm.to(self.device)
        self.lstm.eval()
        logger.info(f"LSTM loaded from {lstm_checkpoint}")

        # Feature extraction
        self.feature_version = feature_version
        self.feature_extractor = FeatureExtractorV2() if feature_version == "v2" else FeatureExtractor()
        self.track_manager = TrackManager(window_size=30, stale_threshold=90)
        self.person_resolver = LogicalPersonResolver(max_gap_frames=60)
        self.incident_manager = FallIncidentManager()
        self.fall_state_gate = FallStateGate()

        # Settings
        self.confidence_threshold = confidence_threshold
        self.cooldown_frames = cooldown_frames

        # State
        self._interrupted = False
        self.alarm_lost_timeout_seconds = 10.0

    def process_video(
        self,
        input_path: str | Path,
        output_path: str | Path | None = None,
        json_path: str | Path | None = None,
    ) -> dict:
        """Process a video file for fall detection.

        Args:
            input_path: Path to input video.
            output_path: Path for annotated output video (None to skip).
            json_path: Path for JSON event log (None to skip).

        Returns:
            Dict with events, summary, and metadata.
        """
        input_path = Path(input_path)
        self._interrupted = False
        self.track_manager.reset()
        self.person_resolver.reset()
        self.incident_manager.reset()
        self.fall_state_gate = FallStateGate()
        self.detector.reset_tracker()

        # Setup signal handler for graceful Ctrl+C
        # signal 只能在主线程安装：异步检测运行在线程池里，
        # 硬安装会抛 "signal only works in main thread"，故非主线程时跳过。
        in_main_thread = threading.current_thread() is threading.main_thread()
        original_handler = signal.getsignal(signal.SIGINT)
        if in_main_thread:
            signal.signal(signal.SIGINT, self._handle_interrupt)

        start_time = time.time()
        falls_detected = 0
        unique_people = set()

        try:
            with VideoReader(input_path) as reader:
                meta = reader.metadata
                writer = None

                if output_path:
                    output_path = Path(output_path)
                    writer = VideoWriter(output_path, meta.width, meta.height, meta.fps)
                    writer.__enter__()

                logger.info(
                    f"Processing {input_path.name}: "
                    f"{meta.width}x{meta.height} @ {meta.fps:.1f}fps, "
                    f"{meta.total_frames} frames ({meta.duration_seconds:.1f}s)"
                )

                for frame_num, frame in tqdm(
                    reader, total=meta.total_frames, desc="Processing"
                ):
                    if self._interrupted:
                        logger.info("Interrupted by user, saving partial output...")
                        break

                    # 1. Detect + Track
                    frame_result = self.detector.detect_frame(frame, frame_num)
                    observations = [
                        PersonObservation(person.track_id, person.bbox, person.keypoints)
                        for person in frame_result.persons
                        if person.track_id >= 0
                    ]
                    logical_ids = self.person_resolver.resolve_frame(observations, frame_num)
                    ambiguous_raw_ids = self.person_resolver.ambiguous_raw_ids

                    # 2. Extract features + classify per person
                    active_person_ids = set()
                    for person in frame_result.persons:
                        if person.track_id < 0:
                            continue

                        raw_track_id = person.track_id
                        scale = max(meta.height / 720, 1.0)
                        if not _identity_is_confirmed(
                            raw_track_id, logical_ids, ambiguous_raw_ids
                        ):
                            self._draw_label_scaled(
                                frame,
                                person.bbox,
                                "TARGET UNCERTAIN",
                                COLOR_ORANGE,
                                "normal",
                                scale,
                            )
                            continue

                        person_id = logical_ids[raw_track_id]
                        active_person_ids.add(person_id)
                        unique_people.add(person_id)

                        # Extract features
                        prev_cog = self.track_manager.get_prev_cog_height(person_id)
                        features = self.feature_extractor.extract(
                            person.keypoints, person.bbox, meta.height, prev_cog
                        )
                        self.track_manager.update(person_id, features, frame_num)

                        # Classify if buffer is full
                        seq = self.track_manager.get_sequence(person_id)
                        state = "normal"
                        conf = 0.0

                        if seq is not None:
                            state, conf = self._classify(seq)

                        decision = self.fall_state_gate.update(person_id, state, conf)
                        state = decision.display_state
                        if state == "normal":
                            conf = 0.0
                        self.track_manager.set_classification(person_id, state, conf)

                        # Handle fall events
                        incident = None
                        if state in ("falling", "fallen") and conf >= self.confidence_threshold:
                            incident = self._handle_fall_event(
                                person_id, raw_track_id, frame_num, state, conf, meta.fps
                            )
                            falls_detected = len(self.incident_manager.all_incidents)

                        # Draw on frame — scale sizes to resolution
                        # Determine display state and color
                        show_fall_label = (
                            state in ("falling", "fallen")
                            and conf >= self.confidence_threshold
                            and incident is not None
                            and _fall_label_is_allowed(
                                person_id, incident.person_id, incident.target_status
                            )
                        )
                        if show_fall_label:
                            display_state = state
                        else:
                            display_state = "normal"

                        color = STATE_COLORS.get(display_state, COLOR_GREEN)

                        # Build label — always show class with confidence
                        state_labels = {
                            "normal": "NORMAL",
                            "falling": "FALLING",
                            "fallen": "FALLEN",
                        }
                        if incident is not None and not show_fall_label:
                            label = "TARGET UNCERTAIN"
                            color = COLOR_ORANGE
                        elif display_state != "normal":
                            label = (
                                f"P:{person_id} "
                                f"{state_labels[display_state]} {conf:.0%}"
                            )
                        else:
                            label = f"P:{person_id} {state_labels[display_state]}"

                        draw_skeleton(
                            frame, person.keypoints, color=color,
                            thickness=max(2, int(2 * scale)),
                            point_radius=max(4, int(4 * scale)),
                        )
                        self._draw_label_scaled(
                            frame, person.bbox, label, color, state, scale
                        )

                    # Draw alerts based on current per-person state
                    self.fall_state_gate.discard_missing(active_person_ids)
                    self.incident_manager.refresh_visible_people(active_person_ids, frame_num)
                    incidents_by_person = {
                        incident.person_id: incident
                        for incident in self.incident_manager.active_incidents()
                    }
                    alert_scale = max(meta.height / 720, 1.0)
                    has_falling = False
                    has_fallen = False
                    fallen_duration = 0.0
                    alert_incident = None

                    for person in frame_result.persons:
                        if person.track_id < 0:
                            continue
                        if not _identity_is_confirmed(
                            person.track_id, logical_ids, ambiguous_raw_ids
                        ):
                            continue
                        person_id = logical_ids[person.track_id]
                        t_state, t_conf = self.track_manager.get_classification(person_id)
                        if t_state == "falling" and t_conf >= self.confidence_threshold:
                            has_falling = True
                        elif t_state == "fallen" and t_conf >= self.confidence_threshold:
                            has_fallen = True
                            if person_id in incidents_by_person:
                                incident = incidents_by_person[person_id]
                                alert_incident = incident
                                fallen_duration = max(
                                    fallen_duration,
                                    (frame_num - incident.start_frame)
                                    / max(meta.fps, 1),
                                )

                    # Keep the alert during a short detection loss, but never draw a stale bbox.
                    lost_timeout = meta.fps * self.alarm_lost_timeout_seconds
                    for incident in self.incident_manager.active_incidents():
                        if (
                            incident.target_status in ("target_lost", "target_pending_confirmation")
                            and frame_num - incident.last_seen_frame <= lost_timeout
                        ):
                            has_fallen = True
                            alert_incident = incident
                            fallen_duration = max(
                                fallen_duration,
                                (frame_num - incident.start_frame)
                                / max(meta.fps, 1),
                            )

                    if has_falling:
                        draw_alert(
                            frame, "!! FALL DETECTED !!",
                            color=COLOR_RED,
                            font_scale=1.2 * alert_scale,
                            thickness=max(3, int(3 * alert_scale)),
                        )
                    elif has_fallen:
                        event_label = "PERSON FALLEN"
                        if alert_incident is not None:
                            if alert_incident.target_status == "target_lost":
                                event_label = f"FALL EVENT #{alert_incident.event_id}: TARGET LOST"
                            elif alert_incident.target_status == "target_pending_confirmation":
                                event_label = (
                                    f"FALL EVENT #{alert_incident.event_id}: TARGET PENDING"
                                )
                        draw_alert(
                            frame,
                            f"{event_label} ({fallen_duration:.1f}s)",
                            color=COLOR_ORANGE,
                            font_scale=1.2 * alert_scale,
                            thickness=max(3, int(3 * alert_scale)),
                        )

                    # Status bar
                    draw_status_bar(
                        frame, frame_num, meta.total_frames,
                        meta.fps, len(active_person_ids), falls_detected,
                    )

                    # Write annotated frame
                    if writer:
                        writer.write(frame)

                    # Periodic cleanup
                    if frame_num % 100 == 0:
                        self.track_manager.cleanup(frame_num)

                if writer:
                    writer.__exit__(None, None, None)

        finally:
            if in_main_thread:
                signal.signal(signal.SIGINT, original_handler)

        processing_time = time.time() - start_time

        # Build result
        result = {
            "metadata": {
                "input_file": str(input_path),
                "processing_date": datetime.now().isoformat(),
                "model_version": "1.0",
                "confidence_threshold": self.confidence_threshold,
                "video_fps": meta.fps,
                "video_duration": format_timestamp(meta.total_frames, meta.fps),
                "total_frames": meta.total_frames,
                "resolution": f"{meta.width}x{meta.height}",
            },
            "events": [
                incident.to_dict(meta.fps) for incident in self.incident_manager.all_incidents
            ],
            "summary": {
                "total_falls": len(self.incident_manager.all_incidents),
                "persons_tracked": len(unique_people),
                "processing_time_seconds": round(processing_time, 1),
                "interrupted": self._interrupted,
            },
        }

        # Save JSON
        if json_path:
            json_path = Path(json_path)
            json_path.parent.mkdir(parents=True, exist_ok=True)
            with open(json_path, "w") as f:
                json.dump(result, f, indent=2)
            logger.info(f"Event log saved to {json_path}")

        # Print summary
        logger.info(f"\n{'='*60}")
        logger.info("RESULTS")
        logger.info(f"{'='*60}")
        logger.info(f"Falls detected: {len(self.incident_manager.all_incidents)}")
        logger.info(f"Persons tracked: {len(unique_people)}")
        logger.info(f"Processing time: {processing_time:.1f}s")
        if output_path:
            logger.info(f"Output video: {output_path}")
        if json_path:
            logger.info(f"Event log: {json_path}")

        return result

    @staticmethod
    def _draw_label_scaled(frame, bbox, label, color, state, scale):
        """Draw a compact label above the current bounding box."""
        import cv2

        x1, y1, x2, y2 = [int(v) for v in bbox]
        ui_scale = _label_scale(scale)
        bbox_thick = max(2, int(2 * ui_scale))
        cv2.rectangle(frame, (x1, y1), (x2, y2), color, bbox_thick)

        font = cv2.FONT_HERSHEY_SIMPLEX
        font_scale_val = 0.55 * ui_scale
        font_thick = max(1, int(1.5 * ui_scale))
        padding = int(4 * ui_scale)

        (tw, th), baseline = cv2.getTextSize(label, font, font_scale_val, font_thick)

        # Position label above bbox, or below if no room
        if y1 - th - baseline - padding * 2 >= 0:
            lbl_y2 = y1
            lbl_y1 = y1 - th - baseline - padding * 2
        else:
            lbl_y1 = y1
            lbl_y2 = y1 + th + baseline + padding * 2

        lbl_x1 = x1
        lbl_x2 = x1 + tw + padding * 2

        # Background
        cv2.rectangle(frame, (lbl_x1, lbl_y1), (lbl_x2, lbl_y2), color, -1)

        # Text — white for fall/fallen states, black for normal
        text_color = (255, 255, 255) if state != "normal" else (0, 0, 0)
        if y1 - th - padding * 2 >= 0:
            text_y = lbl_y2 - baseline - padding
        else:
            text_y = lbl_y1 + th + padding
        cv2.putText(
            frame, label, (lbl_x1 + padding, text_y),
            font, font_scale_val, text_color, font_thick, cv2.LINE_AA,
        )

    def _classify(self, sequence: np.ndarray) -> tuple[str, float]:
        """Classify a feature sequence using the LSTM."""
        x = torch.FloatTensor(sequence).unsqueeze(0).to(self.device)
        preds, confs = self.lstm.predict(x)
        class_id = preds[0].item()
        confidence = confs[0].item()
        class_name = self.class_names.get(class_id, "normal")
        return class_name, confidence

    def _handle_fall_event(
        self,
        person_id: int,
        raw_track_id: int,
        frame_num: int,
        state: str,
        conf: float,
        fps: float,
    ):
        """Report a fall into the event-first incident manager."""
        incident_count = len(self.incident_manager.all_incidents)
        incident = self.incident_manager.report_fall(
            person_id=person_id,
            raw_track_id=raw_track_id,
            frame_num=frame_num,
            confidence=conf,
        )
        if len(self.incident_manager.all_incidents) > incident_count:
            logger.info(
                f"FALL DETECTED: Event {incident.event_id}, Person {person_id} "
                f"at frame {frame_num} "
                f"({format_timestamp(frame_num, fps)}) conf={conf:.2f}"
            )
        return incident

    def _handle_interrupt(self, signum, frame):
        self._interrupted = True


