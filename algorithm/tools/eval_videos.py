# -*- coding: utf-8 -*-
"""端到端跌倒检测评估脚本：批量跑视频，输出混淆矩阵与分类指标。

为什么不用特征包（X.npy / y.npy）：
    特征包只能评估 LSTM 分类器，评估不到 YOLO 姿态与跟踪的误差；
    用真实视频跑完整管线，得到的才是“系统在这批场景上能不能用”。

数据组织（二选一）：

    1) 文件名约定（推荐，零配置）：
       视频文件名以 fall / normal 开头，可选带场景名：
           fall_A栋走廊_01.mp4
           normal_办公室_02.mp4
       场景名取第 2 段（即 A栋走廊 / 办公室），用于分组统计。

    2) manifest.csv（更精确）：
           file,label,scene
           clips/001.mp4,fall,A栋走廊
           clips/002.mp4,normal,办公室

用法：
    python tools/eval_videos.py --data-dir D:/eval_clips --out D:/eval_out
    python tools/eval_videos.py --manifest D:/eval_clips/manifest.csv --device cuda
    python tools/eval_videos.py --data-dir D:/eval_clips --montage   # 额外拼接演示视频

输出（--out 目录）：
    predictions.csv        每个视频的预测明细
    confusion_matrix.png   混淆矩阵图（带计数与百分比）
    metrics.json           准确率 / 精确率 / 召回率 / F1 / 误报率
    per_scene.csv          按场景分组的准确率
    montage.mp4            （--montage）标注视频拼接的演示片

注意：
    判定口径是“视频级”——只要该视频检出任一跌倒事件即预测为 fall。
    若需要事件级评估（同一条视频里有多次跌倒/站立切换），应改用逐帧标注。
"""

from __future__ import annotations

import argparse
import csv
import json
import os
import re
import sys
import time
from pathlib import Path

import numpy as np

ALGO_DIR = Path(__file__).resolve().parent.parent
MODEL_DIR = ALGO_DIR / "models"
VIDEO_EXTS = {".mp4", ".avi", ".mov", ".mkv", ".flv", ".webm", ".m4v"}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="批量视频跌倒检测评估")
    parser.add_argument("--data-dir", help="视频目录（按文件名推断标签与场景）")
    parser.add_argument("--manifest", help="manifest.csv（file,label,scene）")
    parser.add_argument("--out", default=str(ALGO_DIR / "eval_out"), help="输出目录")
    parser.add_argument("--device", default=os.getenv("FALL_DEVICE", "cpu"), help="cpu / cuda")
    parser.add_argument("--confidence", type=float, default=0.40, help="跌倒置信度阈值")
    parser.add_argument("--yolo", default="yolo11n-pose-fallen.pt", help="YOLO 权重文件名")
    parser.add_argument(
        "--lstm",
        default="models/checkpoints/v2_hard_samples_20260903/last.pth",
        help="LSTM 检查点（相对 models/）",
    )
    parser.add_argument("--montage", action="store_true", help="额外拼接标注视频演示片")
    return parser.parse_args()


def collect_samples(args: argparse.Namespace) -> list[dict]:
    """收集待评估样本，返回 [{path, label, scene}]。"""
    samples: list[dict] = []
    if args.manifest:
        manifest = Path(args.manifest)
        base = manifest.parent
        with manifest.open(encoding="utf-8-sig") as fh:
            for row in csv.DictReader(fh):
                label = (row.get("label") or "").strip().lower()
                if label not in {"fall", "normal"}:
                    print(f"[跳过] 标签非法: {row}")
                    continue
                path = Path(row["file"])
                if not path.is_absolute():
                    path = base / path
                samples.append(
                    {"path": path, "label": label, "scene": (row.get("scene") or "未标注").strip()}
                )
    elif args.data_dir:
        for path in sorted(Path(args.data_dir).rglob("*")):
            if path.suffix.lower() not in VIDEO_EXTS:
                continue
            parts = path.stem.split("_")
            label = parts[0].strip().lower()
            if label not in {"fall", "normal"}:
                print(f"[跳过] 文件名未以 fall/normal 开头: {path.name}")
                continue
            scene = parts[1] if len(parts) > 1 else path.parent.name
            samples.append({"path": path, "label": label, "scene": scene})
    else:
        raise SystemExit("必须提供 --data-dir 或 --manifest")

    if not samples:
        raise SystemExit("没有可评估的样本，请检查目录或文件名约定")
    return samples


def build_pipeline(args: argparse.Namespace):
    """构建推理管线（与 fall_detection_service 的加载方式保持一致）。"""
    if str(MODEL_DIR) not in sys.path:
        sys.path.insert(0, str(MODEL_DIR))
    from src.pipeline import FallDetectionPipeline

    yolo_path = MODEL_DIR / args.yolo
    lstm_path = MODEL_DIR / args.lstm
    for p, name in ((yolo_path, "YOLO 权重"), (lstm_path, "LSTM 检查点")):
        if not p.exists():
            raise SystemExit(f"{name}不存在：{p}")

    old_cwd = os.getcwd()
    os.chdir(MODEL_DIR)  # 管线内部依赖相对路径（缓存/配置）
    try:
        return FallDetectionPipeline(
            yolo_model=str(yolo_path),
            lstm_checkpoint=str(lstm_path),
            device=args.device,
            confidence_threshold=args.confidence,
            cooldown_frames=90,
            tracker_config=str(MODEL_DIR / "configs" / "botsort.yaml"),
        )
    finally:
        os.chdir(old_cwd)


def run_eval(pipeline, samples: list[dict], out_dir: Path) -> list[dict]:
    """逐个视频推理，返回明细记录。"""
    annotated_dir = out_dir / "annotated"
    annotated_dir.mkdir(parents=True, exist_ok=True)
    rows: list[dict] = []

    for idx, item in enumerate(samples, start=1):
        path: Path = item["path"]
        out_video = annotated_dir / f"{path.stem}__annotated.mp4"
        print(f"[{idx}/{len(samples)}] {path.name} …", end="", flush=True)
        started = time.time()
        try:
            result = pipeline.process_video(
                input_path=str(path),
                output_path=str(out_video) if args_montage_enabled else None,
            )
            falls = int(result.get("summary", {}).get("total_falls", 0))
            error = ""
        except Exception as exc:  # noqa: BLE001 单个视频失败不中断整批
            falls, error = 0, str(exc)
            print(f" 失败: {error}", end="")
        elapsed = time.time() - started
        label_pred = "fall" if falls > 0 else "normal"
        rows.append(
            {
                "file": path.name,
                "scene": item["scene"],
                "label_true": item["label"],
                "label_pred": label_pred,
                "n_falls": falls,
                "elapsed_s": round(elapsed, 2),
                "error": error,
            }
        )
        flag = "✓" if label_pred == item["label"] else "✗"
        print(f" {flag} 真实={item['label']} 预测={label_pred} ({elapsed:.1f}s)")
    return rows


def summarize(rows: list[dict]) -> tuple[dict, np.ndarray]:
    """计算二分类指标与混淆矩阵。"""
    labels = ["fall", "normal"]
    cm = np.zeros((2, 2), dtype=int)
    for row in rows:
        i = labels.index(row["label_true"])
        j = labels.index(row["label_pred"])
        cm[i, j] += 1

    tn, fp = cm[1, 1], cm[1, 0]  # normal 判对 / normal 误报为 fall
    fn, tp = cm[0, 1], cm[0, 0]  # fall 漏报 / fall 判对
    total = int(cm.sum())
    accuracy = (tp + tn) / total if total else 0.0
    precision = tp / (tp + fp) if (tp + fp) else 0.0
    recall = tp / (tp + fn) if (tp + fn) else 0.0
    f1 = 2 * precision * recall / (precision + recall) if (precision + recall) else 0.0
    # 误报率：正常视频被报跌倒的比例，养老场景里这个比准确率更关键
    false_alarm = fp / (fp + tn) if (fp + tn) else 0.0

    metrics = {
        "samples": total,
        "fall_samples": int(tp + fn),
        "normal_samples": int(tn + fp),
        "accuracy": round(accuracy, 4),
        "precision": round(precision, 4),
        "recall": round(recall, 4),
        "f1": round(f1, 4),
        "false_alarm_rate": round(false_alarm, 4),
        "confusion_matrix": {"labels": labels, "matrix": cm.tolist()},
    }
    return metrics, cm


def plot_confusion(cm: np.ndarray, metrics: dict, out_path: Path) -> None:
    """绘制混淆矩阵图（不依赖 sklearn）。"""
    import matplotlib

    matplotlib.use("Agg")
    import matplotlib.pyplot as plt
    from matplotlib import font_manager

    # matplotlib 默认字体不含中文，不设置会把中文画成方框
    installed = {f.name for f in font_manager.fontManager.ttflist}
    for candidate in ("Microsoft YaHei", "SimHei", "Noto Sans CJK SC", "PingFang SC"):
        if candidate in installed:
            plt.rcParams["font.sans-serif"] = [candidate, "DejaVu Sans"]
            break
    plt.rcParams["axes.unicode_minus"] = False

    fig, ax = plt.subplots(figsize=(5.6, 4.8), dpi=150)
    total = cm.sum() or 1
    display = [
        [(int(cm[i, j]), f"{cm[i, j] / total:.1%}") for j in range(2)] for i in range(2)
    ]

    im = ax.imshow(cm, cmap="Blues")
    labels_cn = ["跌倒 (fall)", "正常 (normal)"]
    ax.set_xticks([0, 1], labels_cn)
    ax.set_yticks([0, 1], labels_cn)
    ax.set_xlabel("模型预测")
    ax.set_ylabel("真实标签")
    ax.set_title("跌倒检测混淆矩阵（视频级）", fontsize=12, pad=12)

    for i in range(2):
        for j in range(2):
            count, pct = display[i][j]
            color = "white" if count > cm.max() * 0.6 else "#1f2937"
            ax.text(j, i - 0.12, str(count), ha="center", va="center", fontsize=18, color=color)
            ax.text(j, i + 0.22, pct, ha="center", va="center", fontsize=11, color=color)

    fig.colorbar(im, ax=ax, fraction=0.046, pad=0.04)
    subtitle = (
        f"样本 {metrics['samples']} · 准确率 {metrics['accuracy']:.1%} · "
        f"召回率 {metrics['recall']:.1%} · 误报率 {metrics['false_alarm_rate']:.1%}"
    )
    fig.text(0.5, 0.015, subtitle, ha="center", fontsize=9, color="#4b5563")
    fig.tight_layout(rect=(0, 0.04, 1, 1))
    # bbox_inches="tight" 避免中文轴标签被裁掉
    fig.savefig(out_path, bbox_inches="tight")
    plt.close(fig)


def write_per_scene(rows: list[dict], out_path: Path) -> None:
    """按场景统计准确率，便于发现“哪些场景不行”。"""
    scenes: dict[str, list[dict]] = {}
    for row in rows:
        scenes.setdefault(row["scene"], []).append(row)
    with out_path.open("w", newline="", encoding="utf-8-sig") as fh:
        writer = csv.writer(fh)
        writer.writerow(["场景", "样本数", "判对数", "准确率", "漏报数", "误报数"])
        for scene, items in sorted(scenes.items()):
            correct = sum(1 for r in items if r["label_true"] == r["label_pred"])
            missed = sum(1 for r in items if r["label_true"] == "fall" and r["label_pred"] == "normal")
            false_alarm = sum(
                1 for r in items if r["label_true"] == "normal" and r["label_pred"] == "fall"
            )
            writer.writerow(
                [scene, len(items), correct, f"{correct / len(items):.1%}", missed, false_alarm]
            )


def make_montage(out_dir: Path) -> None:
    """把标注视频按顺序拼接成一个演示片（需 imageio-ffmpeg）。"""
    annotated = sorted((out_dir / "annotated").glob("*__annotated.mp4"))
    if not annotated:
        print("没有标注视频可拼接")
        return
    try:
        import imageio_ffmpeg

        ffmpeg = imageio_ffmpeg.get_ffmpeg_exe()
    except Exception as exc:  # noqa: BLE001
        print(f"缺少 imageio-ffmpeg，跳过拼接: {exc}")
        return
    list_file = out_dir / "concat.txt"
    with list_file.open("w", encoding="utf-8") as fh:
        for p in annotated:
            fh.write(f"file '{p.as_posix()}'\n")
    target = out_dir / "montage.mp4"
    import subprocess

    subprocess.run(
        [ffmpeg, "-y", "-f", "concat", "-safe", "0", "-i", str(list_file),
         "-c:v", "libx264", "-pix_fmt", "yuv420p", "-crf", "23", str(target)],
        capture_output=True,
        timeout=1800,
    )
    print(f"演示片已生成: {target}")


args_montage_enabled = False


def main() -> None:
    global args_montage_enabled
    args = parse_args()
    args_montage_enabled = args.montage
    out_dir = Path(args.out)
    out_dir.mkdir(parents=True, exist_ok=True)

    samples = collect_samples(args)
    fall_n = sum(1 for s in samples if s["label"] == "fall")
    print(f"共 {len(samples)} 个视频（跌倒 {fall_n} / 正常 {len(samples) - fall_n}），设备={args.device}")

    pipeline = build_pipeline(args)
    rows = run_eval(pipeline, samples, out_dir)

    with (out_dir / "predictions.csv").open("w", newline="", encoding="utf-8-sig") as fh:
        writer = csv.DictWriter(fh, fieldnames=list(rows[0].keys()))
        writer.writeheader()
        writer.writerows(rows)

    metrics, cm = summarize(rows)
    (out_dir / "metrics.json").write_text(
        json.dumps(metrics, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    plot_confusion(cm, metrics, out_dir / "confusion_matrix.png")
    write_per_scene(rows, out_dir / "per_scene.csv")

    print("\n===== 评估结果 =====")
    print(f"准确率   : {metrics['accuracy']:.1%}")
    print(f"召回率   : {metrics['recall']:.1%}  （跌倒漏报 {metrics['confusion_matrix']['matrix'][0][1]} 例）")
    print(f"精确率   : {metrics['precision']:.1%}")
    print(f"F1       : {metrics['f1']:.3f}")
    print(f"误报率   : {metrics['false_alarm_rate']:.1%}  （正常视频被误报）")
    print(f"输出目录 : {out_dir}")

    if args.montage:
        make_montage(out_dir)


if __name__ == "__main__":
    main()
