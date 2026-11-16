# 朝夕护行 · 摔倒检测服务（algorithm）

基于 **YOLO11n-pose 姿态估计 + 跨帧跟踪 + LSTM 时序分类** 的老年人摔倒检测微服务（FastAPI）。
输入一段视频，输出结构化跌倒事件（12 字段）与叠加了骨架、人体框、事件标签的标注视频。

本目录的推理工程对应 **v2 版本：八特征、二分类（normal / fallen）**，并保留对 v1
（五特征、三分类 normal / falling / fallen）检查点的兼容，切换只需改一项环境变量。

---

## 一、推理链与版本

    视频 → YOLO11n-pose(17 关键点) → BoT-SORT 跨帧跟踪 → 逻辑人合并(原始 track ID → 逻辑人 ID)
         → v2 八特征序列(窗口 30 帧) → LSTM 二分类 → 跌倒状态机 → 事件(12 字段) + 标注视频

- **姿态权重**：`yolo11n-pose-fallen.pt`（在 Simuletic_Person 躺地人体数据上冻结主干微调，
  保持 COCO 17 关键点布局），缺失时自动回退 `yolo11n-pose.pt`，回退状态可在 `/models/status` 查看。
- **分类器 v2（默认）**：`models/checkpoints/v2_hard_samples_20260903/last.pth`，
  输入 8 维特征、输出 2 类（`normal` / `fallen`）。
- **分类器 v1（可选）**：`models/checkpoints/best.pth`，输入 5 维特征、输出 3 类
  （`normal` / `falling` / `fallen`）。两者由 `src/model_config.py` 按检查点内的
  `feature_version` / `input_size` / `class_names` 元数据自动适配，无需改代码。

v2 八特征（`src/feature_extraction_v2.py`，按 LSTM 输入顺序）：

| # | 特征 | 含义 |
| --- | --- | --- |
| 1 | `torso_angle` | 躯干与竖直方向夹角，判断是否躺倒 |
| 2 | `cog_height` | 重心高度（按画面高度归一化） |
| 3 | `vertical_velocity` | 重心竖直方向速度 |
| 4 | `bbox_aspect_ratio` | 人体框宽高比 |
| 5 | `keypoint_confidence` | 关键点平均置信度 |
| 6 | `leg_angle` | 腿部夹角 |
| 7 | `torso_visible` | 躯干关键点可见比例 |
| 8 | `legs_visible` | 腿部关键点可见比例 |

> 后三项为“缺失感知”特征：头部或腿部被遮挡时不再被误当成直立姿态，这是 v2 相对 v1 的主要改动。

---

## 二、目录结构

    algorithm/
    ├── main.py                          # FastAPI 入口（启动时预加载模型）
    ├── fall_detection_service.py        # 服务类：单例、模型加载/回退、同步/异步检测
    ├── fall_detection_routes.py         # API 路由
    ├── requirements.txt                 # 依赖（版本已锁定，含 lap）
    ├── .env.example                     # 环境变量模板（复制为 .env）
    ├── models/                          # 完整推理工程（服务通过 sys.path 导入 src.pipeline）
    │   ├── detect_falls.py              # 命令行入口（单视频 → 标注视频 + 事件 JSON）
    │   ├── src/
    │   │   ├── pipeline.py              # v2 流水线（FallDetectionPipeline）
    │   │   ├── detector.py              # YOLO 姿态检测 + BoT-SORT 跟踪
    │   │   ├── feature_extraction.py    # v1 五特征
    │   │   ├── feature_extraction_v2.py # v2 八特征（缺失感知）
    │   │   ├── track_manager.py         # 30 帧滑窗、序列归一化
    │   │   ├── fall_state.py            # 连续帧确认门
    │   │   ├── person_identity.py       # 原始 track ID → 逻辑人 ID
    │   │   ├── incident_manager.py      # 事件生命周期与 12 字段序列化
    │   │   ├── model_config.py          # 检查点 ↔ 特征版本/类别语义适配
    │   │   ├── video_io.py              # 读写视频（H.264 输出）
    │   │   ├── utils.py
    │   │   └── dataset/train/evaluate/label_parser/dataset_processor/simple_tracker.py
    │   ├── models/
    │   │   ├── fall_detector.py         # LSTM 分类器定义
    │   │   └── checkpoints/             # 权重目录（权重不入库，见第三节）
    │   ├── configs/botsort.yaml         # 跟踪器配置
    │   ├── output/test_events.json      # 12 字段事件 JSON 示例
    │   └── yolo11n-pose-fallen.pt       # 姿态权重（不入库）
    ├── model-v2-resources/              # v2 姿态权重副本、微调脚本、训练与评测证据
    ├── uploads/                         # 上传视频临时目录（运行自动创建，已忽略）
    └── outputs/                         # 标注视频输出目录（运行自动创建，已忽略）

---

## 三、权重文件（必需，均不纳入版本库）

仓库不包含任何 `.pt` / `.pth`，clone 后需按下表放置，否则 `/health` 返回 `loaded=false`。

| 文件 | 放置路径（相对 `algorithm/`） | 必需性 | 说明 |
| --- | --- | --- | --- |
| `yolo11n-pose-fallen.pt` | `models/yolo11n-pose-fallen.pt` | 必需（缺失则回退 `yolo11n-pose.pt`） | v2 微调姿态权重，约 5.9 MB |
| `last.pth`（v2） | `models/models/checkpoints/v2_hard_samples_20260903/last.pth` | 默认分类器 | 8 特征 2 分类，约 2.4 MB |
| `best.pth`（v1） | `models/models/checkpoints/best.pth` | 可选 | 5 特征 3 分类，约 792 KB，用于回退对照 |
| `yolo11n-pose.pt`（v1） | `models/yolo11n-pose.pt` | 可选 | 官方基础姿态权重，作为回退 |

获取方式（三处内容一致，MD5 `6a6394629424bcb72a7e20596c07bb9d`）：

1. 项目交付包中的模型工程根目录；
2. `algorithm/model-v2-resources/weights/yolo11n-pose-fallen.pt`（仅姿态权重）；
3. 数据与证据包 `fall-detection-data/weights/`。

放置后自检：

    cd algorithm
    python -c "from pathlib import Path; p=Path('models'); print((p/'yolo11n-pose-fallen.pt').exists(), (p/'models/checkpoints/v2_hard_samples_20260903/last.pth').exists())"

---

## 四、环境准备

实测环境：Windows 11 / Python 3.13.12 / CPU 推理；依赖版本见 `requirements.txt`（已锁定）。

    python -m venv .venv
    .venv\Scripts\activate            # Windows
    # source .venv/bin/activate       # Linux / macOS

    # 1) 先按本机情况安装 PyTorch（CPU 或对应 CUDA 版本）
    pip install torch==2.15.0 torchvision==0.29.2
    # GPU 示例：pip install torch==2.15.0 torchvision==0.29.2 --index-url https://download.pytorch.org/whl/cu121

    # 2) 再安装其余依赖（含 BoT-SORT 必需的 lap）
    cd algorithm
    pip install -r requirements.txt

> `lap` 必须预装。若缺失，ultralytics 会在首次推理时尝试联网自动安装，离线环境将直接失败。

---

## 五、配置（.env）

复制模板后按需修改：

    copy .env.example .env           # Windows
    # cp .env.example .env           # Linux / macOS

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `FALL_MODEL_DIR` | `models` | 模型工程根目录，相对 `algorithm/`，也可写绝对路径 |
| `FALL_YOLO_MODEL` | `yolo11n-pose-fallen.pt` | 姿态权重文件名；缺失自动回退 `yolo11n-pose.pt` |
| `FALL_LSTM_CHECKPOINT` | `models/checkpoints/v2_hard_samples_20260903/last.pth` | v2 八特征二分类；改 `models/checkpoints/best.pth` 即回退 v1 三分类 |
| `FALL_TRACKER_CONFIG` | `configs/botsort.yaml` | BoT-SORT 跟踪器配置 |
| `FALL_DEVICE` | 空 | `cuda` / `cpu` / 留空自动选择 |
| `FALL_CONFIDENCE_THRESHOLD` | `0.40` | 跌倒置信度阈值 |
| `FALL_COOLDOWN_FRAMES` | `90` | 两次告警的最小间隔帧数 |
| `FALL_MAX_WORKERS` | `1` | 异步任务线程数（计算密集型，串行） |
| `FALL_UPLOAD_DIR` | `uploads` | 上传视频临时目录 |
| `FALL_OUTPUT_DIR` | `outputs` | 标注视频输出目录 |
| `FALL_HOST` / `FALL_PORT` | `0.0.0.0` / `8001` | 监听地址与端口 |
| `FALL_RELOAD` | `false` | 开发热重载 |

> 阈值与冷却只在服务启动时读取，属服务级参数；请求里的 `confidence` / `cooldown` 表单字段
> 当前不会覆盖服务级配置（见第十节）。

---

## 六、启动

    cd algorithm
    python main.py
    # 等价：uvicorn main:app --host 0.0.0.0 --port 8001

启动日志出现 `模型预加载成功 ... loaded: True` 即为完整模式；若模型缺失，服务仍可启动，
`/health` 返回 `loaded=false` 并在 `yolo_requested` / `lstm_checkpoint` 中给出缺失路径（降级模式）。

---

## 七、接口

Swagger UI：http://localhost:8001/docs

| 接口 | 方法 | 说明 |
| --- | --- | --- |
| `/api/fall-detection/health` | GET | 健康检查，返回模型加载状态与生效权重 |
| `/api/fall-detection/models/status` | GET | 模型路径、是否存在、是否回退 |
| `/api/fall-detection/detect` | POST | 同步检测（上传视频，等待结果） |
| `/api/fall-detection/detect/file` | POST | 按服务器本地绝对路径检测 |
| `/api/fall-detection/output/{file_name}` | GET | 下载标注视频（`video/mp4`） |
| `/api/fall-detection/detect/async` | POST | 异步检测（当前版本不可用，见第十节） |
| `/api/fall-detection/detect/task/{task_id}` | GET | 查询异步任务状态 |

同步检测并返回标注视频：

    curl -X POST http://localhost:8001/api/fall-detection/detect \
      -F "file=@sample.mp4" -F "with_output=true"

`with_output` 默认 `false`，设为 `true` 才会生成标注视频，响应中通过 `output_url` 下载。

返回结构（节选，字段含义见第九节）：

    {
      "code": 200,
      "message": "检测完成",
      "data": {
        "metadata": { "video_fps": 24.16, "video_duration": "00:01:04.766", "total_frames": 1565 },
        "events": [
          { "type": "fall", "event_id": 1, "track_id": 1, "logical_person_id": 1,
            "raw_track_ids": [1], "candidate_person_ids": [3], "target_status": "target_lost",
            "start_frame": 85, "end_frame": 708,
            "timestamp_start": "00:00:03.518", "timestamp_end": "00:00:29.300",
            "confidence": 1.0 }
        ],
        "summary": { "total_falls": 1, "persons_tracked": 4,
                     "processing_time_seconds": 129.6, "interrupted": false },
        "output_file": "xxx_annotated.mp4",
        "output_url": "/api/fall-detection/output/xxx_annotated.mp4"
      }
    }

---

## 八、命令行（不启动服务）

    cd algorithm/models
    python detect_falls.py -i sample.mp4 -o out_annotated.mp4 -j events.json

默认值均为仓库内实际存在的路径：`--yolo-model yolo11n-pose-fallen.pt`、
`--model models/checkpoints/v2_hard_samples_20260903/last.pth`。
回退 v1：`--model models/checkpoints/best.pth`。

---

## 九、事件 JSON 字段（12 字段）

示例文件：`models/output/test_events.json`

| 字段 | 类型 | 含义 |
| --- | --- | --- |
| `type` | string | 事件类型，固定 `fall` |
| `event_id` | int | 事件序号，同一视频内从 1 递增 |
| `track_id` | int | 逻辑人 ID（与 `logical_person_id` 同值，保留兼容） |
| `logical_person_id` | int | 逻辑人 ID，跨轨迹片段保持稳定 |
| `raw_track_ids` | int[] | 归属该逻辑人的原始跟踪 ID 列表 |
| `candidate_person_ids` | int[] | 事件期间出现过的候选人体 ID |
| `target_status` | string | 事件收尾状态，`target_lost`（目标离开画面）或 `target_observed` |
| `start_frame` | int | 事件开始帧 |
| `end_frame` | int | 事件结束帧 |
| `timestamp_start` | string | 开始时间（视频内相对时间，`HH:MM:SS.mmm`） |
| `timestamp_end` | string | 结束时间（同上） |
| `confidence` | float | 事件置信度（0–1，保留 3 位） |

> 事件起止由目标关联状态决定：`end_frame` 对应“目标丢失/仍可见”的时刻，不是跌倒动作的起止，
> 因此事件时长会明显长于跌倒动作本身。若需要精确跌倒时刻，应另取首次触发告警的帧。

---

## 十、已知限制

1. **异步接口不可用**：流水线在 `process_video` 中注册 `signal` 处理器，在 FastAPI 的线程池里
   调用会抛 `signal only works in main thread of the main interpreter`，`/detect/async` 任务直接
   `failed`。请使用同步 `/detect` 或 `/detect/file`。
2. **请求级阈值不生效**：`confidence` / `cooldown` 表单字段未透传到流水线，实际以 `.env`
   （`FALL_CONFIDENCE_THRESHOLD` / `FALL_COOLDOWN_FRAMES`）为准。
3. **推理分辨率**：检测器未显式设置 `imgsz`，默认 640，而姿态权重按 1024 微调，远处小目标召回会受影响。
4. **速度**：CPU 约 12–15 帧/秒，60 秒 1080p 视频约 2 分钟，长视频请直接调用接口并放宽超时。
5. **v2 分类器为小样本训练**（813 条、train-only，无独立验证集），指标不宜作为泛化性能对外承诺；
   推车、遮挡等场景仍可能漏检。
6. `metadata.model_version` 是流水线内置标识（当前 `1.0`），不代表权重版本；实际权重以
   `/models/status` 的 `lstm_checkpoint` / `yolo_active_name` 为准。

---

## 十一、常见问题

- **`/health` 返回 `loaded=false`**：按第三节核对四个权重文件是否就位，注意 `FALL_LSTM_CHECKPOINT`
  是相对 `FALL_MODEL_DIR` 的路径（因此正确写法是 `models/checkpoints/...`）。
- **`FallModelNotFoundError`**：错误信息里会给出期望的绝对路径，按路径补齐即可。
- **`No module named lap`**：执行 `pip install lap==0.5.14`。
- **`No module named uvicorn` / 依赖找不到**：确认已激活虚拟环境，且 `pip install -r requirements.txt`
  是在该环境中执行。
- **标注视频打不开**：输出为 H.264（`avc1`），可用 PotPlayer / VLC / Chrome 直接播放。

---

## 十二、与朝夕护行业务端的对接（可选）

服务与前端的对接不在本仓库范围内。业务侧接入有两种方式：

1. 管理端上传视频后调用 `/detect`，取 `events` 与 `output_url` 展示、回放；
2. 后端在 `events` 非空时调用现有 `/api/alert` 写入告警，进入人工处置流程。

生产部署请将 CORS `allow_origins` 收窄为前端域名白名单，并为接口增加鉴权。
