"""Compatibility rules between model checkpoints and pose feature versions."""


def resolve_feature_config(checkpoint: dict) -> tuple[str, int]:
    """Return the feature version and input width required by a checkpoint.

    Checkpoints written before v2 contain neither metadata field and therefore
    retain their original five-feature v1 behavior.
    """
    feature_version = checkpoint.get("feature_version", "v1")
    default_size = 5 if feature_version == "v1" else 8
    input_size = checkpoint.get("input_size", default_size)

    if feature_version not in {"v1", "v2"}:
        raise ValueError(f"Unsupported feature_version: {feature_version}")
    if input_size != default_size:
        raise ValueError(
            f"feature_version {feature_version} requires input_size={default_size}, "
            f"got input_size={input_size}"
        )
    return feature_version, input_size


def resolve_class_names(checkpoint: dict) -> dict[int, str]:
    """Read checkpoint-owned class semantics, retaining legacy v1 defaults."""
    class_names = checkpoint.get("class_names")
    if class_names is None:
        return {0: "normal", 1: "falling", 2: "fallen"}
    if not isinstance(class_names, list) or not class_names:
        raise ValueError("class_names must be a non-empty list")
    if class_names[0] != "normal":
        raise ValueError("class_names must begin with normal")
    supported = {"normal", "falling", "fallen"}
    for class_name in class_names:
        if class_name not in supported:
            raise ValueError(f"Unsupported class name: {class_name}")
    if len(set(class_names)) != len(class_names):
        raise ValueError("class_names must not contain duplicates")
    return dict(enumerate(class_names))


def resolve_model_config(checkpoint: dict) -> tuple[str, int, dict[int, str]]:
    """Return every checkpoint setting required to construct the LSTM."""
    feature_version, input_size = resolve_feature_config(checkpoint)
    return feature_version, input_size, resolve_class_names(checkpoint)
