"""Conservative per-person gate for fall labels and event creation."""

from __future__ import annotations

from dataclasses import dataclass

FALL_STATES = {"falling", "fallen"}


@dataclass(frozen=True)
class FallStateDecision:
    """The label that is safe to display for one current observation."""

    display_state: str
    is_confirmed: bool


@dataclass
class _PersonFallState:
    fall_frames: int = 0
    normal_frames: int = 0
    confirmed: bool = False


class FallStateGate:
    """Require repeated evidence before displaying or reporting a fall."""

    def __init__(self, confirmation_frames: int = 3, release_frames: int = 3):
        self.confirmation_frames = confirmation_frames
        self.release_frames = release_frames
        self._states: dict[int, _PersonFallState] = {}

    def update(
        self, person_id: int, raw_state: str, confidence: float
    ) -> FallStateDecision:
        """Apply one raw LSTM prediction to a person's short-lived state."""
        state = self._states.setdefault(person_id, _PersonFallState())
        is_fall = raw_state in FALL_STATES and confidence > 0.0

        if is_fall:
            state.fall_frames += 1
            state.normal_frames = 0
            if state.fall_frames >= self.confirmation_frames:
                state.confirmed = True
        else:
            state.fall_frames = 0
            state.normal_frames += 1
            if state.normal_frames >= self.release_frames:
                state.confirmed = False

        if state.confirmed and is_fall:
            return FallStateDecision(raw_state, True)
        return FallStateDecision("normal", state.confirmed)

    def discard_missing(self, visible_person_ids: set[int]) -> None:
        """Forget local state whenever a logical person is no longer visible."""
        self._states = {
            person_id: state
            for person_id, state in self._states.items()
            if person_id in visible_person_ids
        }


