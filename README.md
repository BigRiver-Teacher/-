# 朝夕护行 · 多模态防跌倒智慧养老平台

面向居家与社区养老场景的安全监护平台：通过姿态估计 + 跨帧跟踪 + 时序分类识别跌倒，联动告警推送、家属/社区处置与健康档案管理。

| 模块 | 技术栈 | 端口 | 说明 |
| --- | --- | --- | --- |
| 前端 | Vue 3 + TypeScript + Vite + Element Plus + ECharts + Pinia | 5173 | 管理端界面、监控大屏、业务交互 |
| 后端 | Spring Boot 3.3.4 + MyBatis-Plus + MySQL 8（Java 17） | 8080 | REST 接口、JWT 鉴权、业务逻辑 |
| 算法 | FastAPI + YOLO11n-pose + BoT-SORT + LSTM v2（八特征、二分类） | 8001 | 视频跌倒检测，输出 12 字段事件 JSON 与标注视频 |

## 目录结构

```
zhaoxi-huxing/
├── frontend/                     Vue3 前端
│   ├── src/api/                  接口封装（按业务域拆分，26 个模块）
│   ├── src/views/                页面（按业务编号 1~15 组织）
│   ├── src/stores/               Pinia 状态
│   ├── src/router/               路由与菜单权限映射
│   ├── src/config/videoRegistry.json  视频引用记录（路径相对项目根目录）
│   ├── src/utils/request.ts      axios 实例（baseURL=/api）
│   ├── public/videos/            监控演示视频（站点路径 /videos/xxx）
│   └── vite.config.ts            dev 代理 /api → localhost:8080
├── backend/                      Spring Boot 后端
│   ├── src/main/java/com/example/elderly/
│   │   ├── controller/           接口层
│   │   ├── service/              业务层
│   │   ├── mapper/               MyBatis-Plus 数据访问
│   │   ├── entity/ dto/ vo/      数据模型
│   │   ├── config/               安全、跨域、JWT 过滤器
│   │   └── util/                 工具类（JWT、响应封装等）
│   ├── src/main/resources/
│   │   ├── application.properties          主配置（支持环境变量覆盖）
│   │   ├── init.sql                        建库建表（25 张表）
│   │   └── seed_data.sql                   演示数据
│   └── pom.xml
├── algorithm/                    跌倒检测微服务（v2：八特征、二分类）
│   ├── main.py                   FastAPI 入口
│   ├── fall_detection_routes.py  路由
│   ├── fall_detection_service.py 推理封装与模型加载
│   ├── models/                   推理工程（代码内置，权重需按 algorithm/README.md 第三节放置）
│   │   ├── detect_falls.py               命令行入口
│   │   ├── src/pipeline.py               端到端推理管线（v2）
│   │   ├── src/feature_extraction_v2.py  v2 八特征（缺失感知）
│   │   ├── models/fall_detector.py       LSTM 分类器
│   │   ├── models/checkpoints/           LSTM 检查点（v2 last.pth / v1 best.pth，不入库）
│   │   ├── configs/botsort.yaml          跟踪器配置
│   │   ├── output/test_events.json       12 字段事件 JSON 示例
│   │   └── yolo11n-pose-fallen.pt        改进版姿态权重（不入库）
│   ├── model-v2-resources/       v2 微调/对比脚本与训练评测证据
│   ├── requirements.txt
│   └── .env.example
├── docs/                         架构说明、技术路线图、流程图
├── scripts/                      Windows 一键初始化/启动/自检脚本
├── LICENSE
└── README.md
```

## 环境要求

| 组件 | 版本要求 |
| --- | --- |
| JDK | 17 或 21（Spring Boot 3.3.4 支持上限约 JDK 23，勿用更高版本） |
| Maven | 3.6+ |
| MySQL | 8.0+ |
| Node.js | ^22.18.0 或 >=24.12.0 |
| Python | 3.10（建议 conda 环境） |
| PyTorch / Ultralytics | 按 CPU 或 CUDA 选择，CPU 亦可运行 |

## 快速开始

启动顺序：**数据库 → 后端(8080) → 前端(5173) → 算法服务(8001)**。

### 第 0 步：初始化数据库

```bash
mysql -uroot -p --default-character-set=utf8mb4 -e "source backend/src/main/resources/init.sql"
mysql -uroot -p --default-character-set=utf8mb4 -e "source backend/src/main/resources/seed_data.sql"
```

或直接运行 `scripts\01-init-db.bat`（可用 `DB_USER` / `DB_PASSWORD` 覆盖默认账号）。

### 第 1 步：启动后端

```bash
cd backend
mvn -DskipTests package
java -jar target/javapratice-0.0.1-SNAPSHOT.jar
```

验证：`http://127.0.0.1:8080/api/stats/overview`、接口文档 `http://127.0.0.1:8080/doc.html`。

### 第 2 步：启动前端

```bash
cd frontend
npm install        # 或 pnpm install
npm run dev
```

访问 `http://localhost:5173`（Vite 默认绑定 IPv6，用 localhost 而非 127.0.0.1）。
`/api` 由 `vite.config.ts` 代理到 `http://localhost:8080`，后端改端口时需同步修改代理目标。

### 第 3 步：启动算法服务（可选）

```bash
cd algorithm
python -m venv .venv && .venv\Scripts\activate     # Linux/macOS: source .venv/bin/activate
pip install torch==2.15.0 torchvision==0.29.2      # GPU 环境按 CUDA 版本换 wheel
pip install -r requirements.txt
cp .env.example .env                               # Windows: copy .env.example .env
python main.py
```

验证：`http://127.0.0.1:8001/api/fall-detection/health`，模型加载成功时返回 `loaded: true`。
接口文档：`http://127.0.0.1:8001/docs`。

> 推理代码已内置在 `algorithm/models/`，但**权重文件不入库**：需按 `algorithm/README.md`
> 第三节把 `yolo11n-pose-fallen.pt` 与 LSTM 检查点放到指定路径。未放置时服务仍可启动，
> `/health` 返回 `loaded: false` 并给出缺失路径。

### 第 4 步：自检

```bash
scripts\99-verify.bat
```

## 配置说明

### 后端

`backend/src/main/resources/application.properties` 中的敏感项均支持环境变量覆盖：

| 配置项 | 环境变量 | 默认值 |
| --- | --- | --- |
| `spring.datasource.url` | `DB_URL` | `jdbc:mysql://localhost:3306/elderly_care?...` |
| `spring.datasource.username` | `DB_USERNAME` | `root` |
| `spring.datasource.password` | `DB_PASSWORD` | `123456` |
| `jwt.secret` | `JWT_SECRET` | 开发占位密钥 |

也可使用 profile 方式：复制 `application-local.properties.example` 为 `application-local.properties`，
以 `--spring.profiles.active=local` 启动。

### 算法服务

由 `algorithm/.env` 或 `FALL_*` 环境变量控制（环境变量优先）：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `FALL_MODEL_DIR` | `models` | 模型工程根目录（相对 `algorithm/`） |
| `FALL_YOLO_MODEL` | `yolo11n-pose-fallen.pt` | 姿态权重，缺失自动回退 `yolo11n-pose.pt` |
| `FALL_LSTM_CHECKPOINT` | `models/checkpoints/v2_hard_samples_20260903/last.pth` | v2 八特征二分类；改 `models/checkpoints/best.pth` 即回退 v1 三分类 |
| `FALL_TRACKER_CONFIG` | `configs/botsort.yaml` | BoT-SORT 跟踪器配置 |
| `FALL_DEVICE` | 空 | `cuda` / `cpu` / 留空自动 |
| `FALL_CONFIDENCE_THRESHOLD` | `0.40` | 跌倒置信度阈值 |
| `FALL_PORT` | `8001` | 服务端口 |

完整清单与说明见 `algorithm/.env.example` 与 `algorithm/README.md`。

### 前端

- 接口统一前缀 `/api`，代理目标见 `frontend/vite.config.ts`。
- 监控页（`/echart/monitor`）的「监控1 / 监控2」视频由 `src/config/videoRegistry.json` 统一登记。

## 监控视频的插入与路径约定

**目标文件夹**：`frontend/public/videos/`（Vite 静态资源根，对外路径为 `/videos/<文件名>`）。

**引用记录**：`frontend/src/config/videoRegistry.json` 的 `slots` 字段，`path` 一律填写
**相对项目根目录**的路径，例如 `frontend/public/videos/monitor-1.mp4`，不使用绝对路径。

### 方式一：命令行插入（推荐）

```bash
# 插入到监控1
node scripts/05-insert-video.mjs --file D:/demo/fall.mp4 --slot monitor-1

# 目标目录已有同名文件时自动编号（fall-2.mp4）
node scripts/05-insert-video.mjs --file D:/demo/fall.mp4 --slot monitor-2 --rename

# 覆盖同名文件 / 查看现状 / 移除引用
node scripts/05-insert-video.mjs --file D:/demo/fall.mp4 --slot monitor-1 --overwrite
node scripts/05-insert-video.mjs --list
node scripts/05-insert-video.mjs --remove monitor-2 --delete-file
```

Windows 下也可双击运行 `scripts\05-insert-video.bat`（参数同上）。

### 方式二：手工放置

1. 复制视频到 `frontend/public/videos/`；
2. 在 `videoRegistry.json` 对应槽位把 `path` 写为 `frontend/public/videos/<文件名>`。

### 异常处理

| 情况 | 处理方式 |
| --- | --- |
| 目标文件夹不存在 | 插入脚本自动创建并提示；手工放置时需自行建目录；仓库已带 `public/videos/README.md` 占位 |
| 文件名重复 | 默认拒绝并给出三种解决方式；`--rename` 自动编号 `-2/-3`；`--overwrite` 覆盖 |
| 路径写成了绝对路径 | 脚本拒绝写入且校验失败；前端渲染「视频引用失败」并提示改为相对路径 |
| 路径不在 `frontend/public` 下 | 同上，构建时会给出定位到槽位的警告 |
| 文件缺失 / 格式不支持 / 加载失败 | `<video>` 触发 `error` 事件，页面显示「视频引用失败」占位块并在控制台输出槽位与地址；启动/构建时 `video-registry-guard` 插件提前告警 |
| 槽位未配置 | 页面显示「未配置视频」占位，不影响页面其他功能 |
| 引用记录 JSON 损坏 | 脚本中止且不做任何写入，避免破坏配置 |

> 临时演示也可不改仓库配置：把 `frontend/.env.example` 复制为 `.env.local`，设置
> `VITE_MONITOR_VIDEO_URL` 作为槽位为空时的兜底地址。

## 模块关系

1. 前端通过 `/api` 经 Vite 代理访问后端 8080；
2. 后端独占访问 MySQL `elderly_care`，前端不直连数据库；
3. 算法服务为独立微服务，通过自身 HTTP 接口（8001）提供视频跌倒检测，与后端暂无强制依赖，可单独启动；
4. 告警业务数据（跌倒记录、处置台账）由后端提供，算法侧事件回写为后续扩展能力。

## 常见问题

- **8080 被占用**：`netstat -ano | findstr 8080` 结束进程，或改 `server.port` 并同步前端代理。
- **JDK 版本过高**：改用 JDK 17 或 21。
- **npm install 慢**：切换镜像源，或使用仓库内的 `pnpm-lock.yaml`。
- **算法服务 `loaded: false`**：权重未放置。按 `algorithm/README.md` 第三节核对
  `algorithm/models/yolo11n-pose-fallen.pt` 与
  `algorithm/models/models/checkpoints/v2_hard_samples_20260903/last.pth` 是否在位，
  或改用 `FALL_LSTM_CHECKPOINT=models/checkpoints/best.pth` 搭配 v1 权重。
- **5173 打不开**：用 `http://localhost:5173`，不要用 `127.0.0.1`。

## 许可

本项目基于 [MIT License](LICENSE) 开源。数据库与算法权重如涉及第三方数据，请遵循其原始许可。
