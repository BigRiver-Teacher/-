"""Conservative association from tracker IDs to stable logical person IDs."""

from __future__ import annotations

from dataclasses import dataclass, field

import numpy as np


@dataclass(frozen=True)
class PersonObservation:
    """The geometry available when a tracker emits a person detection."""

    raw_track_id: int
    bbox: tuple[float, float, float, float]
    keypoints: np.ndarray


@dataclass
class _LogicalPerson:
    person_id: int
    last_bbox: tuple[float, float, float, float]
    last_keypoints: np.ndarray
    last_seen_frame: int
    previous_center: tuple[float, float] | None = None
    raw_track_ids: set[int] = field(default_factory=set)

    @property
    def center(self) -> tuple[float, float]:
        x1, y1, x2, y2 = self.last_bbox
        return ((x1 + x2) / 2, (y1 + y2) / 2)

    @property
    def diagonal(self) -> float:
        x1, y1, x2, y2 = self.last_bbox
        return max(float(np.hypot(x2 - x1, y2 - y1)), 1.0)

    def predicted_center(self, frame_num: int) -> tuple[float, float]:
        center = self.center
        if self.previous_center is None:
            return center
        dx = center[0] - self.previous_center[0]
        dy = center[1] - self.previous_center[1]
        gap = max(frame_num - self.last_seen_frame, 1)
        return center[0] + dx * gap, center[1] + dy * gap

    def update(self, observation: PersonObservation, frame_num: int) -> None:
        self.previous_center = self.center
        self.last_bbox = observation.bbox
        self.last_keypoints = observation.keypoints.copy()
        self.last_seen_frame = frame_num
        self.raw_track_ids.add(observation.raw_track_id)


class LogicalPersonResolver:
    """Maps volatile tracker IDs to stable, video-local person IDs.

    A new raw ID can inherit an unseen logical person only during a short gap
    and only when spatial motion and usable pose observations agree. This is
    intentionally conservative: an unmatched detection becomes a new person
    instead of risking a cross-person merge in a crowded scene.
    """

    def __init__(
        self,
        max_gap_frames: int = 60,
        min_iou: float = 0.05,
        max_center_distance: float = 0.75,
        max_keypoint_distance: float = 0.75,
        ambiguity_margin: float = 0.08,
    ):
        self.max_gap_frames = max_gap_frames
        self.min_iou = min_iou
        self.max_center_distance = max_center_distance
        self.max_keypoint_distance = max_keypoint_distance
        self.ambiguity_margin = ambiguity_margin
        self._next_person_id = 1
        self._raw_to_person: dict[int, int] = {}
        self._persons: dict[int, _LogicalPerson] = {}
        self._ambiguous_raw_ids: set[int] = set()

    @property
    def ambiguous_raw_ids(self) -> set[int]:
        """Raw IDs that are visible but cannot safely inherit person state."""
        return set(self._ambiguous_raw_ids)

    def resolve_frame(
        self, observations: list[PersonObservation], frame_num: int
    ) -> dict[int, int]:
        """Return logical IDs using geometry, not permanent tracker-ID bindings.

        Tracker IDs are useful evidence, but a crowded scene can keep two raw
        IDs alive while assigning their boxes to opposite people. Re-evaluating
        every observation against the prior-frame logical people prevents their
        LSTM histories from being exchanged in that case.
        """
        self._ambiguous_raw_ids.clear()
        candidate_scores: list[tuple[float, int, int]] = []
        scores_by_observation: dict[int, list[float]] = {
            index: [] for index in range(len(observations))
        }
        for observation_index, observation in enumerate(observations):
            for person_id, person in self._persons.items():
                gap = frame_num - person.last_seen_frame
                if gap <= 0 or gap > self.max_gap_frames:
                    continue
                score = self._association_score(person, observation, frame_num)
                if score is not None:
                    candidate_scores.append((score, observation_index, person_id))
                    scores_by_observation[observation_index].append(score)

        ambiguous_observations = {
            observation_index
            for observation_index, scores in scores_by_observation.items()
            if len(scores) > 1
            and max(scores) - sorted(scores, reverse=True)[1] < self.ambiguity_margin
        }

        candidate_scores.sort(reverse=True)
        assigned_observations: set[int] = set()
        assigned_people: set[int] = set()
        selected: dict[int, int] = {}

        for score, observation_index, person_id in candidate_scores:
            if observation_index in ambiguous_observations:
                continue
            if observation_index in assigned_observations or person_id in assigned_people:
                continue
            selected[observation_index] = person_id
            assigned_observations.add(observation_index)
            assigned_people.add(person_id)

        assignments: dict[int, int] = {}
        for observation_index, observation in enumerate(observations):
            if observation_index in ambiguous_observations:
                self._ambiguous_raw_ids.add(observation.raw_track_id)
                continue
            person_id = selected.get(observation_index)
            if person_id is None:
                person_id = self._create_person(observation, frame_num)
            else:
                self._persons[person_id].update(observation, frame_num)

            self._raw_to_person[observation.raw_track_id] = person_id
            assignments[observation.raw_track_id] = person_id

        return assignments

    def raw_ids_for(self, person_id: int) -> set[int]:
        """Return all raw tracker IDs associated with one logical person."""
        person = self._persons.get(person_id)
        return set() if person is None else set(person.raw_track_ids)

    def reset(self) -> None:
        self._next_person_id = 1
        self._raw_to_person.clear()
        self._persons.clear()
        self._ambiguous_raw_ids.clear()

    def _create_person(self, observation: PersonObservation, frame_num: int) -> int:
        person_id = self._next_person_id
        self._next_person_id += 1
        self._persons[person_id] = _LogicalPerson(
            person_id=person_id,
            last_bbox=observation.bbox,
            last_keypoints=observation.keypoints.copy(),
            last_seen_frame=frame_num,
            raw_track_ids={observation.raw_track_id},
        )
        return person_id

    def _find_reassociation(
        self,
        observation: PersonObservation,
        current_raw_ids: set[int],
        frame_num: int,
    ) -> int | None:
        candidates: list[tuple[float, int]] = []
        current_person_ids = {
            self._raw_to_person[raw_id]
            for raw_id in current_raw_ids
            if raw_id in self._raw_to_person
        }

        for person_id, person in self._persons.items():
            if person_id in current_person_ids:
                continue
            gap = frame_num - person.last_seen_frame
            if gap <= 0 or gap > self.max_gap_frames:
                continue

            score = self._association_score(person, observation, frame_num)
            if score is not None:
                candidates.append((score, person_id))

        if not candidates:
            return None
        candidates.sort(reverse=True)
        if len(candidates) > 1 and candidates[0][0] - candidates[1][0] < self.ambiguity_margin:
            return None
        return candidates[0][1]

    def _association_score(
        self, person: _LogicalPerson, observation: PersonObservation, frame_num: int
    ) -> float | None:
        iou = _bbox_iou(person.last_bbox, observation.bbox)
        predicted_center = person.predicted_center(frame_num)
        center_distance = _center_distance(predicted_center, observation.bbox) / person.diagonal
        if iou < self.min_iou and center_distance > self.max_center_distance:
            return None

        keypoint_distance = _keypoint_distance(
            person.last_keypoints, observation.keypoints, person.diagonal
        )
        if keypoint_distance is not None and keypoint_distance > self.max_keypoint_distance:
            return None

        spatial_score = max(iou, 1.0 - min(center_distance, 1.0))
        pose_score = 0.5 if keypoint_distance is None else 1.0 - min(keypoint_distance, 1.0)
        return 0.8 * spatial_score + 0.2 * pose_score


def _bbox_iou(
    first: tuple[float, float, float, float], second: tuple[float, float, float, float]
) -> float:
    x1 = max(first[0], second[0])
    y1 = max(first[1], second[1])
    x2 = min(first[2], second[2])
    y2 = min(first[3], second[3])
    intersection = max(0.0, x2 - x1) * max(0.0, y2 - y1)
    first_area = max(0.0, first[2] - first[0]) * max(0.0, first[3] - first[1])
    second_area = max(0.0, second[2] - second[0]) * max(0.0, second[3] - second[1])
    union = first_area + second_area - intersection
    return intersection / union if union > 0 else 0.0


def _center_distance(
    center: tuple[float, float], bbox: tuple[float, float, float, float]
) -> float:
    x = (bbox[0] + bbox[2]) / 2
    y = (bbox[1] + bbox[3]) / 2
    return float(np.hypot(center[0] - x, center[1] - y))


def _keypoint_distance(
    first: np.ndarray, second: np.ndarray, normalization: float
) -> float | None:
    if first.ndim != 2 or second.ndim != 2 or first.shape[1] < 3 or second.shape[1] < 3:
        return None
    limit = min(len(first), len(second))
    valid = (first[:limit, 2] >= 0.3) & (second[:limit, 2] >= 0.3)
    if np.count_nonzero(valid) < 4:
        return None
    distances = np.linalg.norm(first[:limit, :2][valid] - second[:limit, :2][valid], axis=1)
    return float(np.median(distances) / max(normalization, 1.0))


