"""Fine-tune yolo11n-pose on the Simuletic 'laying person' set (single 'person' class).

Keeps the COCO 17-keypoint layout so the existing pipeline keeps working, but
teaches the model that a person lying on the ground is still a valid detection.
"""
from ultralytics import YOLO


def main() -> None:
    model = YOLO("D:/fall-detection-data/weights/yolo11n-pose.pt")
    results = model.train(
        data="D:/fall-detection-data/Simuletic_Person/data.yaml",
        epochs=60,
        imgsz=1024,
        batch=8,
        lr0=0.0005,
        lrf=0.01,
        freeze=10,          # keep COCO backbone, only adapt neck/head
        amp=False,          # skip the AMP check that downloads a model
        workers=0,          # avoid Windows spawn multiprocessing issues
        device=0,
        seed=20260904,
        project="D:/fall-detection-data/runs",
        name="yolo11n-pose-fallen",
        exist_ok=True,
        verbose=True,
        plots=True,
    )
    print("BEST_DIR:", getattr(results, "save_dir", None))


if __name__ == "__main__":
    main()
