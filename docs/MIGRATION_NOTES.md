# 整理记录（MIGRATION NOTES）

> 本仓库由三端源码（前端 / 后端 / 算法）整合而成，自包含、可直接构建运行。
> 整理原则：只复制、不改业务逻辑；仅新增运行所需的说明与配置模板。

## 一、模块与来源（相对路径）

| 模块 | 仓库内路径 | 说明 |
| --- | --- | --- |
| 前端 | `frontend/` | Vue3 + TypeScript + Vite（Element Plus / ECharts / Pinia） |
| 后端 | `backend/` | Spring Boot 3.3.4 + MyBatis-Plus（Java 17） |
| 算法服务 | `algorithm/` | FastAPI + YOLO11n-pose + LSTM 跌倒检测 |
| 模型工程 | `algorithm/models/` | 推理所需源码、LSTM 权重、跟踪器配置、YOLO 权重 |
| 模型评测资源 | `algorithm/model-v2-resources/` | v2 权重、微调与对比脚本、训练证据 |
| 文档 | `docs/` | 架构说明、技术路线图、处置流程图 |

## 二、未纳入仓库的内容及原因

| 内容 | 原因 |
| --- | --- |
| `frontend/node_modules` | 第三方依赖，`npm ci` / `pnpm install` 可还原 |
| `frontend/dist` | 构建产物，`npm run build` 可重新生成 |
| `frontend/src/video/*.mp4` | 演示视频体积大（约 38MB），改由 `VITE_MONITOR_VIDEO_URL` 外部指定 |
| `backend/target` | Maven 构建产物，含可执行 jar |
| `algorithm/__pycache__`、`*.pyc` | Python 字节码缓存 |
| `algorithm/.env` | 本机运行配置，仓库提供 `.env.example` 模板 |
| IDE 配置（`.vscode`、`.idea`、`*.iml`） | 个人开发环境配置 |
| 训练数据集、原始场景视频 | 体积大（数百 MB），与运行无关 |

## 三、为适配仓库所做的修改（不含业务逻辑改动）

| 文件 | 类型 | 说明 |
| --- | --- | --- |
| `README.md` | 重写 | 相对路径、快速开始、端口与启动顺序、配置说明 |
| `algorithm/models/` | 补齐 | 从算法交付包补入 `src/`、`models/fall_detector.py`、`models/checkpoints/best.pth`、`configs/botsort.yaml`、`yolo11n-pose-fallen.pt` |
| `algorithm/.env.example` | 保留并核对 | `FALL_MODEL_DIR=models`，与仓库布局一致 |
| `backend/src/main/resources/application.properties` | 修改 | 数据库账号密码、JWT 密钥改为环境变量 + 本地默认值 |
| `backend/.../util/JwtUtil.java` | 修改 | 签名密钥改为从配置项 `jwt.secret` 注入 |
| `frontend/src/views/Echart-monitor.vue` | 修改 | 演示视频由静态 import 改为 `VITE_MONITOR_VIDEO_URL` 可选配置 |
| `frontend/env.d.ts` | 修改 | 补充 `VITE_MONITOR_VIDEO_URL` 类型声明 |
| `scripts/01-init-db.bat` | 修改 | 数据库账号密码改由 `DB_USER` / `DB_PASSWORD` 环境变量提供 |
| `.gitignore` / `.gitattributes` / `.editorconfig` | 新增 | 忽略规则、换行符与编码统一 |
| `LICENSE` | 新增 | MIT |

## 四、配置与密钥

- 数据库：`DB_URL` / `DB_USERNAME` / `DB_PASSWORD`（默认 `root/123456`，仅本地演示）
- JWT：`JWT_SECRET`（默认值为开发用占位密钥，公开部署请务必覆盖）
- 算法服务：`algorithm/.env`（由 `.env.example` 复制），`FALL_*` 前缀环境变量优先
- 前端：`frontend/.env.local`（由 `.env.example` 复制），目前仅 `VITE_MONITOR_VIDEO_URL`

> 公开部署前请轮换数据库密码与 JWT 密钥，不要沿用仓库中的默认值。
