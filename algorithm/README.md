# 朝夕护行 · 摔倒检测服务（algorithm）

基于 **YOLO 姿态估计 + LSTM** 的老年人摔倒检测微服务（FastAPI），作为朝夕护行智慧养老平台的独立推理服务，接收视频并输出摔倒事件。

---

## 运行模式

本服务有两种运行模式，**“能跑起来”与“能完整推理”是两件事**：

### 1. 完整模式（真实推理，仓库默认）

模型工程已内置在 `algorithm/models/`，目录结构：

    algorithm/models/
    ├── src/pipeline.py               # FallDetectionPipeline（服务通过 src.pipeline 导入）
    ├── models/fall_detector.py       # LSTM 分类器定义
    ├── models/checkpoints/best.pth   # LSTM 跌倒三分类权重
    ├── configs/botsort.yaml          # 跟踪器配置
    └── yolo11n-pose-fallen.pt        # 改进版权重（v2）

配置：`FALL_MODEL_DIR=models`（相对 algorithm 目录），`FALL_YOLO_MODEL=yolo11n-pose-fallen.pt`
（缺失时会回退到 `yolo11n-pose.pt`）。环境变量见 `.env.example`。

启动后 `/api/fall-detection/health` 返回 `loaded=true`，检测接口可返回真实事件。

### 2. 降级模式（模型缺失时自动进入）

若 `models/` 中缺少 `src/pipeline.py` 或 `models/checkpoints/best.pth`，服务仍然可以启动：

- ✅ 服务可以启动，`/health`、`/models/status`、`/docs` 正常返回；
- ❌ `/detect` 等推理接口返回明确的模型缺失错误（HTTP 500，提示缺失文件路径）；
- 用途：前端联调、部署流程验证、CI 环境自检。

> 结论：**仓库已内置完整模型工程，按下方步骤配置环境后可直接完成真实推理。**


---

## 一、目录结构

    algorithm/
    ├── main.py                     # FastAPI 主应用（入口）
    ├── fall_detection_service.py   # 服务类：单例、预加载、同步/异步检测
    ├── fall_detection_routes.py    # API 路由
    ├── requirements.txt            # Python 依赖清单
    ├── .env.example                # 环境变量模板（复制为 .env）
    ├── models/                     # 完整模型工程（推理必需，已内置）
    │   ├── src/                    # 流水线源码：pipeline / detector / feature_extraction ...
    │   ├── models/                 # LSTM 分类器与 best.pth 权重
    │   ├── configs/botsort.yaml    # 跟踪器配置
    │   └── yolo11n-pose-fallen.pt  # 改进版姿态权重
    ├── model-v2-resources/         # 改进版姿态权重与评测证据
    │   ├── weights/yolo11n-pose-fallen.pt
    │   ├── scripts/train_pose.py
    │   ├── data/data.yaml
    │   └── evidence/               # 训练日志、曲线、事件 JSON
    ├── uploads/                    # 上传视频临时目录（运行自动创建，已忽略）
    └── outputs/                    # 标注视频输出目录（运行自动创建，已忽略）

## 二、环境准备（conda 虚拟环境隔离 PyTorch 版本冲突）

> 本项目依赖 torch / ultralytics，与 Java 后端互不影响；为避免与系统中其他
> Python 环境的 PyTorch 版本冲突，**强烈建议使用 conda 独立虚拟环境**。

### 1. 创建并激活 conda 环境

    conda create -n fall-detect python=3.10 -y
    conda activate fall-detect

### 2. 安装 PyTorch（先装 CUDA 版或 CPU 版）

GPU 环境（按本机 CUDA 版本选择，示例为 cu121）：

    pip install torch torchvision --index-url https://download.pytorch.org/whl/cu121

CPU 环境：

    pip install torch torchvision

### 3. 安装项目依赖

    cd algorithm
    pip install -r requirements.txt

## 三、配置

复制环境变量模板并修改：

    copy .env.example .env      # Windows
    # cp .env.example .env      # Linux / macOS

核心配置项（完整说明见 .env.example）：

    FALL_MODEL_DIR=models                        # 相对 algorithm 目录（完整模式）
    FALL_YOLO_MODEL=yolo11n-pose-fallen.pt       # 改进版权重；回退用 yolo11n-pose.pt
    FALL_LSTM_CHECKPOINT=models/checkpoints/best.pth
    FALL_DEVICE=                                 # cuda / cpu / 留空自动
    FALL_PORT=8001

> 路径说明：`FALL_MODEL_DIR` 为相对路径时，以启动 uvicorn 时的当前工作目录为基准，
> 请从 `algorithm` 目录启动（脚本已按此方式编写）。

## 四、启动服务

    conda activate fall-detect
    cd algorithm
    uvicorn main:app --host 0.0.0.0 --port 8001

启动日志会显示模型预加载状态：
- 完整模式：`loaded=true`；
- 降级模式：`loaded=false`，并提示缺失的模型文件路径（服务仍可用）。

## 五、接口说明

交互式文档（Swagger UI）：http://localhost:8001/docs

| 接口 | 方法 | 说明 |
| --- | --- | --- |
| /api/fall-detection/health | GET | 健康检查，返回模型加载状态 |
| /api/fall-detection/models/status | GET | 模型配置与状态 |
| /api/fall-detection/detect | POST | 同步检测（上传视频，等待结果） |
| /api/fall-detection/detect/async | POST | 异步检测（返回 task_id） |
| /api/fall-detection/detect/task/{task_id} | GET | 查询异步任务状态 |
| /api/fall-detection/output/{file_name} | GET | 下载标注视频 |
| /api/fall-detection/detect/file | POST | 按服务器本地路径检测 |

### 同步检测示例

    curl -X POST http://localhost:8001/api/fall-detection/detect \
      -F "file=@test.mp4" -F "with_output=true"

返回示例：

    {
      "code": 200,
      "message": "检测完成",
      "data": {
        "metadata": { "input_file": "...", "video_fps": 30, "total_frames": 900 },
        "events": [
          { "type": "fall", "track_id": 1, "start_frame": 120, "end_frame": 150,
            "timestamp_start": "00:04.0", "timestamp_end": "00:05.0", "confidence": 0.87 }
        ],
        "summary": { "total_falls": 1, "persons_tracked": 2, "processing_time_seconds": 12.3, "interrupted": false },
        "output_url": "/api/fall-detection/output/xxx_annotated.mp4"
      }
    }

### 异步检测示例

    curl -X POST http://localhost:8001/api/fall-detection/detect/async -F "file=@test.mp4"
    # -> {"code":200,"data":{"task_id":"abc123","status":"pending"}}
    curl http://localhost:8001/api/fall-detection/detect/task/abc123
    # -> status: pending / running / done / failed，done 时 data.result 含事件列表

## 六、与朝夕护行平台对接

1. **后端（Spring Boot）联动**：在 Java 后端新增告警触发逻辑，当摔倒检测服务返回
   events 非空时，调用现有 /api/alert 接口写入告警（elder 绑定视频/设备），进入
   "自动感知 → 分级告警 → 人工处置"闭环。
2. **前端调用**：在管理端"实时监控"或"设备管理"页面接入视频上传组件，调用
   本服务的 /api/fall-detection/detect 接口，展示检测结果与标注视频。
3. **网络与 CORS**：服务已开启全量 CORS；生产环境请将 allow_origins 改为前端域名白名单，
   并建议通过 Nginx 反向代理统一入口。

## 七、常见问题

- **模型加载失败 / FallModelNotFoundError**：检查 .env 中 FALL_MODEL_DIR 路径是否正确，
  以及 yolo11n-pose.pt、models/checkpoints/best.pth 是否存在。
- **torch 版本冲突**：使用 conda 独立环境（见第二节），不要与系统 Python 混装。
- **无 GPU 运行慢**：设置 FALL_DEVICE=cpu，视频较长时可使用异步接口避免请求阻塞。
- **Windows 下 uvicorn 无法启动**：确认已 conda activate fall-detect，并安装 uvicorn[standard]。
- **降级模式误判**：若 health 返回 loaded=false，先确认 models/ 目录与权重文件是否齐全。

## 八、模型改进版（v2）接入说明

算法组对 YOLO 姿态估计模型进行了改进：在保持 COCO 17 关键点布局与 LSTM 分类器不变的前提下，
使用“躺地/跌倒人体”数据集对 yolo11n-pose.pt 微调，得到改进权重 yolo11n-pose-fallen.pt
（随工作区提供于 `model-v2-resources/weights/`，完整训练与评测证据在 `model-v2-resources/evidence/`）。

接入方式（完整模式下）：

    # 1. 将改进权重放入模型工程根目录（models/）
    copy model-v2-resources\weights\yolo11n-pose-fallen.pt models\
    # 2. .env 中启用 v2
    FALL_YOLO_MODEL=yolo11n-pose-fallen.pt
    # 3. 回退 v1：改回 yolo11n-pose.pt 后重启

服务默认优先加载 v2：配置的权重缺失时会自动回退到另一版本（v2/v1 互补）并记录日志；
健康检查 /models/status 返回 yolo_active_name、yolo_fallback 等字段，便于核对当前生效版本。

## 九、代码说明

本目录中的服务代码为平台独立推理微服务，与前端、后端解耦，可单独启动验证。
仓库整理时仅做了三件事：新增 `requirements.txt`、补齐 `.env.example` 模板相对路径、
补充本文档的目录与运行说明；**业务代码（.py）未做任何修改**。
