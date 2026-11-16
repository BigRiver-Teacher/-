"""Missingness-aware skeleton features for the v2 fall LSTM."""

import math
from dataclasses import dataclass

import numpy as np

from src.feature_extraction import (
    LEFT_ANKLE,
    LEFT_HIP,
    LEFT_KNEE,
    LEFT_SHOULDER,
    NOSE,
    RIGHT_ANKLE,
    RIGHT_HIP,
    RIGHT_KNEE,
    RIGHT_SHOULDER,
    FeatureExtractor,
)

NUM_FEATURES_V2 = 8


@dataclass
class FeatureVectorV2:
    """The eight v2 features in the documented LSTM input order."""

    torso_angle: float
    cog_height: float
    vertical_velocity: float
    bbox_aspect_ratio: float
    keypoint_confidence: float
    leg_angle: float
    torso_visible: float
    legs_visible: float

    def to_array(self) -> np.ndarray:
        return np.array(
            [
                self.torso_angle,
                self.cog_height,
                self.vertical_velocity,
                self.bbox_aspect_ratio,
                self.keypoint_confidence,
                self.leg_angle,
                self.torso_visible,
                self.legs_visible,
            ],
            dtype=np.float32,
        )


class FeatureExtractorV2(FeatureExtractor):
    """Extract pose features without treating an occluded head as upright."""

    def extract(
        self,
        keypoints: np.ndarray,
        bbox: tuple[float, float, float, float],
        frame_height: int,
        prev_cog_height: float | None = None,
    ) -> FeatureVectorV2:
        torso_angle, torso_visible = self._compute_torso_angle(keypoints)
        leg_angle, legs_visible = self._compute_leg_angle(keypoints)
        cog_height = self._compute_cog_height(keypoints, frame_height)
        return FeatureVectorV2(
            torso_angle=torso_angle,
            cog_height=cog_height,
            vertical_velocity=self._compute_velocity(cog_height, prev_cog_height),
            bbox_aspect_ratio=self._compute_bbox_aspect_ratio(bbox),
            keypoint_confidence=self._compute_mean_confidence(keypoints),
            leg_angle=leg_angle,
            torso_visible=float(torso_visible),
            legs_visible=float(legs_visible),
        )

    def _compute_torso_angle(self, keypoints: np.ndarray) -> tuple[float, bool]:
        hips = self._midpoint(keypoints, LEFT_HIP, RIGHT_HIP)
        shoulders = self._midpoint(keypoints, LEFT_SHOULDER, RIGHT_SHOULDER)
        if hips is not None and shoulders is not None:
            return self._angle_from_vertical(hips, shoulders), True

        nose = self._visible_point(keypoints, NOSE)
        if hips is not None and nose is not None:
            return self._angle_from_vertical(hips, nose), True
        return 0.0, False

    def _compute_leg_angle(self, keypoints: np.ndarray) -> tuple[float, bool]:
        chains = []
        for hip_index, knee_index, ankle_index in (
            (LEFT_HIP, LEFT_KNEE, LEFT_ANKLE),
            (RIGHT_HIP, RIGHT_KNEE, RIGHT_ANKLE),
        ):
            hip = self._visible_point(keypoints, hip_index)
            knee = self._visible_point(keypoints, knee_index)
            ankle = self._visible_point(keypoints, ankle_index)
            if hip is not None and knee is not None and ankle is not None:
                chains.append((hip, ankle))

        if not chains:
            return 0.0, False
        hip = np.mean([item[0] for item in chains], axis=0)
        ankle = np.mean([item[1] for item in chains], axis=0)
        return self._angle_from_vertical(hip, ankle), True

    def _midpoint(
        self, keypoints: np.ndarray, first_index: int, second_index: int
    ) -> np.ndarray | None:
        points = [
            point
            for index in (first_index, second_index)
            if (point := self._visible_point(keypoints, index)) is not None
        ]
        if not points:
            return None
        return np.mean(points, axis=0)

    def _visible_point(self, keypoints: np.ndarray, index: int) -> np.ndarray | None:
        if keypoints is None or index >= len(keypoints):
            return None
        point = keypoints[index]
        if point.shape[0] < 3 or point[2] < self.confidence_threshold:
            return None
        if not (math.isfinite(float(point[0])) and math.isfinite(float(point[1]))):
            return None
        return np.asarray(point[:2], dtype=np.float32)

    @staticmethod
    def _angle_from_vertical(bottom: np.ndarray, top: np.ndarray) -> float:
        dx = float(top[0] - bottom[0])
        dy = abs(float(top[1] - bottom[1]))
        if not (math.isfinite(dx) and math.isfinite(dy)) or (dx == 0 and dy == 0):
            return 0.0
        angle = math.degrees(math.atan2(abs(dx), dy)) / 180.0
        return float(np.clip(angle, 0.0, 1.0))
