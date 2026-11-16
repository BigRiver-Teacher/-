"""Event-first fall incident lifecycle, independent from tracker person IDs."""

from __future__ import annotations

from dataclasses import dataclass, field

from src.video_io import format_timestamp


@dataclass
class FallIncident:
    """One fall event whose target may be temporarily unobserved."""

    event_id: int
    person_id: int
    raw_track_ids: set[int]
    start_frame: int
    end_frame: int
    confidence: float
    target_status: str = "target_observed"
    last_seen_frame: int = 0
    person_history: set[int] = field(default_factory=set)
    candidate_person_ids: set[int] = field(default_factory=set)

    def update(self, person_id: int, raw_track_id: int, frame_num: int, confidence: float) -> None:
        self.person_id = person_id
        self.raw_track_ids.add(raw_track_id)
        self.person_history.add(person_id)
        self.end_frame = frame_num
        self.last_seen_frame = frame_num
        self.confidence = max(self.confidence, confidence)
        self.target_status = "target_observed"

    def to_dict(self, fps: float) -> dict:
        """Serialize the stable event identity and its target-association state."""
        return {
            "type": "fall",
            "event_id": self.event_id,
            "track_id": self.person_id,
            "logical_person_id": self.person_id,
            "raw_track_ids": sorted(self.raw_track_ids),
            "candidate_person_ids": sorted(self.candidate_person_ids),
            "target_status": self.target_status,
            "start_frame": self.start_frame,
            "end_frame": self.end_frame,
            "timestamp_start": format_timestamp(self.start_frame, fps),
            "timestamp_end": format_timestamp(self.end_frame, fps),
            "confidence": round(self.confidence, 3),
        }


class FallIncidentManager:
    """Keeps alarm continuity while only reconnecting identities with ReID evidence."""

    def __init__(self, reid_match_threshold: float = 0.85):
        self.reid_match_threshold = reid_match_threshold
        self._next_event_id = 1
        self._by_person: dict[int, FallIncident] = {}
        self.all_incidents: list[FallIncident] = []

    def report_fall(
        self,
        person_id: int,
        raw_track_id: int,
        frame_num: int,
        confidence: float,
        appearance_similarity: float | None = None,
    ) -> FallIncident:
        """Create/update an incident, reconnecting a lost target only with ReID evidence."""
        incident = self._by_person.get(person_id)
        reconnected = False
        if incident is None:
            incident = self._find_reconnect_candidate(appearance_similarity)
            if incident is None:
                lost = [
                    item for item in self.active_incidents() if item.target_status == "target_lost"
                ]
                if len(lost) == 1:
                    lost[0].candidate_person_ids.add(person_id)
                    lost[0].target_status = "target_pending_confirmation"
                    return lost[0]
                incident = FallIncident(
                    event_id=self._next_event_id,
                    person_id=person_id,
                    raw_track_ids={raw_track_id},
                    start_frame=frame_num,
                    end_frame=frame_num,
                    confidence=confidence,
                    last_seen_frame=frame_num,
                    person_history={person_id},
                )
                self._next_event_id += 1
                self.all_incidents.append(incident)
            else:
                self._by_person.pop(incident.person_id, None)
                reconnected = True

            self._by_person[person_id] = incident

        incident.update(person_id, raw_track_id, frame_num, confidence)
        if reconnected:
            incident.target_status = "target_reconnected"
        return incident

    def refresh_visible_people(self, visible_person_ids: set[int], frame_num: int) -> None:
        """Mark missing targets without expiring their alarm event or drawing a stale box."""
        for incident in self.active_incidents():
            if incident.person_id not in visible_person_ids:
                incident.target_status = "target_lost"
            else:
                incident.last_seen_frame = frame_num
                incident.target_status = "target_observed"

    def active_incidents(self) -> list[FallIncident]:
        return list(self.all_incidents)

    def reset(self) -> None:
        self._next_event_id = 1
        self._by_person.clear()
        self.all_incidents.clear()

    def _find_reconnect_candidate(self, appearance_similarity: float | None) -> FallIncident | None:
        if appearance_similarity is None or appearance_similarity < self.reid_match_threshold:
            return None
        lost = [item for item in self.active_incidents() if item.target_status == "target_lost"]
        return lost[-1] if len(lost) == 1 else None


