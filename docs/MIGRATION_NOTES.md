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
| `backend/src/main/resources/application.properties` | 修改 | 数据库账号密码、JWT 密钥改为**纯环境变量**（不内置任何默认值） |
| `backend/.../util/JwtUtil.java` | 修改 | 签名密钥从配置项 `jwt.secret` 注入，缺失或 < 32 字节直接启动失败 |
| `backend/.../config/LocalEnvBootstrap.java` | 新增 | 本地启动引导：读 `.env.local`、自动生成/复用 `.jwt-secret.local`，不覆盖已设置的环境变量 |
| `frontend/src/views/Echart-monitor.vue` | 修改 | 演示视频由静态 import 改为 `VITE_MONITOR_VIDEO_URL` 可选配置 |
| `frontend/env.d.ts` | 修改 | 补充 `VITE_MONITOR_VIDEO_URL` 类型声明 |
| `scripts/01-init-db.bat` | 修改 | 数据库账号密码改由 `DB_USER` / `DB_PASSWORD` 环境变量提供 |
| `.gitignore` / `.gitattributes` / `.editorconfig` | 新增 | 忽略规则、换行符与编码统一 |
| `LICENSE` | 新增 | MIT |

## 四、配置与密钥

- 数据库：`DB_URL` / `DB_USERNAME` / `DB_PASSWORD`（**无内置默认值**，必须显式提供；本地演示常用 `root/123456`）
- JWT：`JWT_SECRET`（**无内置默认值**，长度 < 32 字节启动失败；`scripts\02-start-backend.bat` 会自动生成并复用 `backend/.jwt-secret.local`）
- 本地开发兜底：启动引导 `LocalEnvBootstrap` 在容器启动前读 `backend/.env.local`（模板 `backend/.env.local.example`）
  补齐未设置的变量，并在缺少 `JWT_SECRET` 时读取/生成 `backend/.jwt-secret.local`；已设置的环境变量优先级最高
- 算法服务：`algorithm/.env`（由 `.env.example` 复制），`FALL_*` 前缀环境变量优先；`FALL_API_KEY` 非空即开启接口鉴权
- 前端：`frontend/.env.local`（由 `.env.example` 复制），目前仅 `VITE_MONITOR_VIDEO_URL`

> 公开部署前请轮换数据库密码与 JWT 密钥，不要沿用任何文档示例值。

## 五、安全与契约治理（阶段 0~4 概要）

整合完成后，针对安全审计发现的 22 项问题做了 5 个阶段的治理，逐项处置状态见 `测试问题清单.md`「处置状态」，
回归资产与发布检查见 `docs/RELEASE_CHECKLIST.md`。要点：

| 阶段 | 主题 | 主要落地 |
| --- | --- | --- |
| 0 | 紧急止血 | 移除默认密钥、JWT 默认拒绝、CORS 白名单、500 脱敏、根路径按角色重定向 |
| 1 | 权限与账号 | `@RequireRole` + 服务端 RBAC（按库内角色）、验证码注册/找回、BCrypt、前端按权威角色守卫 |
| 2 | 契约与异常 | HTTP 与 body.code 一致、分页别名归一化、算法统一错误结构、前端 toast 去重与渲染兜底 |
| 3 | 算法治理 | API Key 鉴权、上传魔数与大小校验、`detect/file` 路径白名单、推理参数按请求生效 |
| 4 | 文档与回归 | 文档零偏差、权限矩阵/冒烟用例/发布清单、全链路回归 |
