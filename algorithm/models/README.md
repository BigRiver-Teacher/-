# models/ —— 完整模型工程

跌倒检测的真实推理工程，由 `fall_detection_service.py` 以 `sys.path` 方式导入，
因此本目录必须作为一个可导入的“模型项目根”存在（`FALL_MODEL_DIR` 指向它）。

## 目录结构

```
algorithm/models/
├── src/
│   ├── __init__.py
│   ├── pipeline.py            # FallDetectionPipeline（服务 from src.pipeline import）
│   ├── detector.py            # YOLO 姿态检测/跟踪封装
│   ├── feature_extraction.py  # 关键点 → 时序特征
│   ├── track_manager.py       # 多目标轨迹管理
│   ├── video_io.py            # 视频读写与时间戳
│   ├── utils.py               # 绘制工具（骨架/告警/状态栏）
│   ├── simple_tracker.py
│   ├── label_parser.py
│   ├── dataset.py             # 数据集定义（训练用）
│   ├── dataset_processor.py   # 数据预处理（训练用）
│   ├── train.py               # LSTM 训练脚本
│   └── evaluate.py            # 评测脚本
├── models/
│   ├── __init__.py
│   ├── fall_detector.py       # LSTM 三分类器定义
│   └── checkpoints/
│       ├── best.pth           # LSTM 跌倒三分类权重（推理使用）
│       └── training_report.json
├── configs/
│   └── botsort.yaml           # 跟踪器配置
└── yolo11n-pose-fallen.pt     # 改进版姿态权重（v2）
```

## 配置对应关系

| `.env` 配置项 | 默认值 | 对应文件 |
| --- | --- | --- |
| `FALL_MODEL_DIR` | `models` | 本目录 |
| `FALL_YOLO_MODEL` | `yolo11n-pose-fallen.pt` | `models/yolo11n-pose-fallen.pt` |
| `FALL_LSTM_CHECKPOINT` | `models/checkpoints/best.pth` | `models/models/checkpoints/best.pth` |
| `FALL_TRACKER_CONFIG` | `configs/botsort.yaml` | `models/configs/botsort.yaml` |

> 注意路径层级：`FALL_MODEL_DIR` 指向 `algorithm/models`，其内部的 Python 包也叫 `models`，
> 因此 LSTM 权重路径写作 `models/checkpoints/best.pth`。

## 启用方式

1. 确认 `.env` 存在（`cp .env.example .env`）且 `FALL_MODEL_DIR=models`；
2. 从 `algorithm` 目录启动：`uvicorn main:app --host 0.0.0.0 --port 8001`；
3. 访问 `/api/fall-detection/health`，`loaded=true` 表示加载成功。

## 训练与评测

`src/train.py`、`src/evaluate.py`、`src/dataset*.py` 用于复现 LSTM 训练流程，
需要自备数据集（未随仓库分发）。姿态权重的微调脚本见 `../model-v2-resources/scripts/`。
