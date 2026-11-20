# 朝夕护行 · 多模态防跌倒智慧养老平台

面向居家与社区养老场景的安全监护平台：通过姿态估计 + 跨帧跟踪 + 时序分类识别跌倒，联动告警推送、家属/社区处置与健康档案管理。

> ⚠️ **免责声明**：本项目提供的数据库演示数据均为虚构，仅用于功能测试。

| 模块 | 技术栈 | 端口 | 说明 |
| --- | --- | --- | --- |
| 前端 | Vue 3 + TypeScript + Vite + Element Plus + ECharts + Pinia | 5173（预览 4173） | 四端界面（管理员/专家/陪护/老人端，含陪护主管），同一登录页按手机号识别 5 类角色分端 |
| 后端 | Spring Boot 3.3.4 + MyBatis-Plus + MySQL 8（Java 17） | 8080 | REST 接口、JWT 默认拒绝鉴权 + 服务端 RBAC、业务逻辑 |
| 算法 | FastAPI + YOLO11n-pose + BoT-SORT + LSTM v2（八特征、二分类） | 8001 | 视频跌倒检测，输出 12 字段事件 JSON 与标注视频；可选 API Key 鉴权 |

## 目录结构

```
zhaoxi-huxing/
├── frontend/                     Vue3 前端
│   ├── src/api/                  接口封装（按业务域拆分，33 个模块）
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
│   │   ├── init.sql                        建库建表（37 张表）
│   │   └── seed_data.sql                   演示数据（含四端账号、服务关系、聊天、健康指标）
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
├── docs/                         架构说明、代码修改与使用说明、发布检查清单、技术路线图、流程图
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

> **仓库内不再内置任何默认密钥**。`JWT_SECRET` 未配置或长度不足 32 位时，后端启动即失败
> （`JwtUtil` 抛 `IllegalStateException`），这是刻意的安全设计，避免用公开默认密钥签发可被伪造的 Token。

| 环境变量 | 默认值 | 说明 |
| --- | --- | --- |
| `DB_USERNAME` | `root` | 数据库账号 |
| `DB_PASSWORD` | 空 | 数据库密码，**必需**（本地演示常用 `123456`） |
| `JWT_SECRET` | 空 | JWT 签名密钥，**必需**，长度至少 32 位，否则启动失败 |
| `APP_AUTH_REGISTRATION_ENABLED` | `true` | 注册/找回密码开关；开启时仍需图形外的邮箱验证码（演示验证码写在后端日志） |

**本地开发不用手配**：启动引导 `LocalEnvBootstrap` 会在 `java -jar` / `mvn spring-boot:run` 前自动补齐——
读取 `backend/.env.local`（模板见 `backend/.env.local.example`）填入未设置的变量，并在缺少 `JWT_SECRET` 时
读取或生成 `backend/.jwt-secret.local`（与 `scripts\02-start-backend.bat` 共用同一文件）。已设置的环境变量优先级最高，不会被覆盖。

推荐用脚本启动：

```bash
scripts\02-start-backend.bat        # 生成 backend\.jwt-secret.local（已 gitignore），后续启动自动复用
```

也可直接在 IDE 终端启动（VSCode / IDEA，三种方式任选）：

```powershell
# 方式一：直接用 jar（工作目录为 backend/ 或项目根目录均可）
cd backend
java -jar target\javapratice-0.0.1-SNAPSHOT.jar

# 方式二：Maven 插件运行
cd backend
mvn spring-boot:run

# 方式三：手工指定变量（注意 PowerShell 用 $env:，bash 用 export）
# PowerShell:
$env:DB_PASSWORD="123456"
$env:JWT_SECRET="请填写至少32位的随机字符串"
java -jar target\javapratice-0.0.1-SNAPSHOT.jar
```

> ⚠️ **PowerShell 里不要用 bash 写法** `JWT_SECRET=xxx java -jar ...`，会报
> `无法将“xxx”项识别为 cmdlet`；PowerShell 必须先 `$env:JWT_SECRET="..."` 再执行命令。
> 首次启动若看到 `[本地引导] 未设置 JWT_SECRET，已生成随机密钥并保存到 ...`，属正常行为。

验证：未登录访问 `http://127.0.0.1:8080/api/stats/overview` 返回 **401（属正常，接口默认拒绝匿名访问）**；
接口文档 `http://127.0.0.1:8080/doc.html` 可匿名打开。登录后携带 `Authorization: Bearer <token>` 再访问业务接口。

### 第 2 步：启动前端

```bash
cd frontend
npm install        # 或 pnpm install
npm run dev
```

访问 `http://localhost:5173`（Vite 默认绑定 IPv6，用 localhost 而非 127.0.0.1）。
`/api` 由 `vite.config.ts` 代理到 `http://localhost:8080`，后端改端口时需同步修改代理目标。

验证生产构建时可用 preview（端口 **4173**，同样配置了 `/api` 代理；后端 CORS 白名单已包含 4173）：

```bash
npm run build
npm run preview      # http://localhost:4173
```

> 后端 `app.cors.allowed-origins` 默认允许 `5173` 与 `4173`（localhost 与 127.0.0.1 各一份），
> 换端口部署需用环境变量 `APP_CORS_ALLOWED_ORIGINS` 覆盖，否则浏览器会被 CORS 拦截。

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

> 算法接口默认不鉴权（`FALL_API_KEY` 为空）。公网部署请设置随机 Key，之后除 health/文档外
> 均需带 `X-API-Key` 请求头；上传限制由 `FALL_MAX_UPLOAD_MB`（默认 500MB）控制。

> ⚠️ **【重要】权重文件放置步骤（必须执行，否则算法无法工作）**：
> 推理代码已内置在 `algorithm/models/`，但**权重文件不入库**。clone 后必须按
> `algorithm/README.md` 第三节把以下权重放到指定路径，否则 `/health` 返回 `loaded: false`：
> - `algorithm/models/yolo11n-pose-fallen.pt`（YOLO 姿态权重，约 5.9 MB）
> - `algorithm/models/models/checkpoints/v2_hard_samples_20260903/last.pth`（LSTM v2 检查点，约 2.4 MB）
>
> 权重文件需由项目作者通过微信/网盘单独发送，见文末「需要单独发送的文件清单」。
> 未放置时服务仍可启动，但 `/health` 返回 `loaded: false` 并给出缺失路径。

### 第 4 步：自检

```bash
scripts\99-verify.bat
```

发布前请按 `docs/RELEASE_CHECKLIST.md` 逐项勾选：角色权限矩阵、账号冒烟、契约与异常、已知遗留。
完整的架构与鉴权说明见 `docs/ARCHITECTURE.md`；
**本次代码改了什么、别人 clone 后怎么跑通各端口**，见 `docs/代码修改与使用说明.md`。

## 多端登录账号（演示）

项目为同一套前端 + 后端，按登录**手机号**自动识别角色并进入对应端（后端按 `phone` 查库定位账号）。
密码统一为 `123456`。

> **密码传输与存储**：登录请求体传的是**明文密码**（HTTP body，生产环境请置于 HTTPS 之后），
> 后端以 BCrypt 哈希比对，库内不存明文；旧 MD5 数据首次登录成功后会自动升级为 BCrypt。

| 端 | 角色 | 账号（手机号） | 姓名 | 职称/职位 | 负责对象 / 备注 |
| --- | --- | --- | --- | --- | --- |
| 管理员端 | admin | 13800000000 | 系统管理员 | — | 全局管理 |
| 专家端 | expert | 13800001001 | 陈伟明 | 主任医师 | 老年心血管疾病（市一医院） |
| 专家端 | expert | 13800001002 | 林美丽 | 副主任医师 | 老年神经系统疾病（市二医院） |
| 陪护端 | caregiver | 13800002101 | 周丽华 | 高级护工（朝阳社区） | 负责：张建国、赵玉兰、钱淑珍 |
| 陪护端 | caregiver | 13800002102 | 吴晓芳 | 护工（海淀社区） | 负责：李秀英、刘德胜 |
| 陪护主管 | supervisor | 13800002201 | 郑国栋 | 护理主管（朝阳社区） | 负责：王德发、陈阿妹 |
| 陪护主管 | supervisor | 13800002202 | 陈志强 | 护理主管（海淀社区） | 全组管理 |
| 老人端（家属代办） | elder | 13800003001 | 张小明 | 家属 | 张建国的儿子（紧急联系人） |
| 老人端（家属代办） | elder | 13800003002 | 李大明 | 家属 | 李秀英的儿子（紧急联系人） |

> 登录后进入：管理员→管理主页、专家→专家工作台、陪护/主管→陪护工作台、老人(家属)→健康概览。

> 另有 4 个内置员工演示账号（staff 角色）：`zhangsan`(13800001111 张三)、`lisi`(13800002222 李四)、`wangwu`(13800003333 王五)、`zhaoliu`(13800004444 赵六)，密码同为 `123456`，仅用于管理员端「账号权限」列表与操作日志署名，具备管理员端同等权限；非四端业务演示账号，请勿用于日常演示登录。原未文档化的隐藏管理员账号 `sunqi` 已移除。

> 说明：老人端由家属账号代办（无老人独立账号）；一键呼叫、健康指标等硬件数据均为模拟数据（无真实设备，`HealthMetricGenerator` 定时生成）。四端共享同一 MySQL `elderly_care`，按角色 + 服务关系（`elder_caregiver` / `elder_expert` / `elder_family`）做数据行级隔离——一个账号只能访问本端与本人服务对象的数据（如护工 A 只能看到其负责的老人 B）。

### 各端功能模块概览

- **管理员端**：管理主页、跌倒预警、老人详情（基础档案/步态/跌倒/健康备注）、传感设备管理、专家康养咨询、陪护人员管理、个人主页、告警信息中心、居家空间档案、干预方案管理、日常康养日程、**对话监管（查看专家↔家属/老人全部对话）**、系统管理、组件与功能。
- **专家端**：工作台、我的咨询（聊天+图片+家属评分）、排班请假（管理员审批）、老人健康档案（只读）、干预方案建议、个人中心。
- **陪护端**：工作台、我负责的老人、今日任务、联系家属（聊天）、告警处置、健康记录、空间巡检、个人中心。
- **陪护主管**：排班管理（课程表）、请假审批、全组视角 + 陪护端全部功能。
- **老人端（家属代办）**：首页健康概览（5 指标 7 天趋势）、在线咨询（新建咨询+满意度打分）、联系陪护、我的护工与家人、用药与日程提醒、个人档案、一键呼叫（侧边栏底部常驻）。

## 配置说明

### 后端

`backend/src/main/resources/application.properties` 中的敏感项均支持环境变量覆盖：

| 配置项 | 环境变量 | 默认值 |
| --- | --- | --- |
| `spring.datasource.url` | `DB_URL` | `jdbc:mysql://localhost:3306/elderly_care?...` |
| `spring.datasource.username` | `DB_USERNAME` | `root` |
| `spring.datasource.password` | `DB_PASSWORD` | **空（必须提供）** |
| `jwt.secret` | `JWT_SECRET` | **空（必须提供，≥32 位）** |
| `app.cors.allowed-origins` | `APP_CORS_ALLOWED_ORIGINS` | `5173`/`4173` 的 localhost 与 127.0.0.1 |
| `app.auth.registration-enabled` | `APP_AUTH_REGISTRATION_ENABLED` | `true`（注册仍需邮箱验证码） |

也可使用 profile 方式：复制 `application-local.properties.example` 为 `application-local.properties`，
以 `--spring.profiles.active=local` 启动。

### 算法服务

由 `algorithm/.env` 或 `FALL_*` 环境变量控制（环境变量优先）：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `FALL_MODEL_DIR` | `models` | 模型工程根目录，**相对路径即可**（相对启动时的当前目录，即 `algorithm/`）；也可写绝对路径。服务启动时会 `resolve()` 成绝对路径 |
| `FALL_YOLO_MODEL` | `yolo11n-pose-fallen.pt` | 姿态权重，缺失自动回退 `yolo11n-pose.pt` |
| `FALL_LSTM_CHECKPOINT` | `models/checkpoints/v2_hard_samples_20260903/last.pth` | 默认 v2 八特征二分类；改 `models/checkpoints/best.pth` 即回退 v1 三分类 |
| `FALL_TRACKER_CONFIG` | `configs/botsort.yaml` | BoT-SORT 跟踪器配置 |
| `FALL_DEVICE` | 空 | `cuda` / `cpu` / 留空自动 |
| `FALL_CONFIDENCE_THRESHOLD` | `0.40` | 服务级跌倒置信度阈值（请求可用 `confidence` 覆盖） |
| `FALL_COOLDOWN_FRAMES` | `90` | 服务级告警冷却帧数（请求可用 `cooldown` 覆盖） |
| `FALL_API_KEY` | 空 | 接口鉴权 Key。**留空则不启用鉴权**；设置后除 `/health` 与文档外，所有接口需带 `X-API-Key` 头 |
| `FALL_MAX_UPLOAD_MB` | `500` | 上传视频大小上限（MB），超限返回 413 |
| `FALL_PORT` | `8001` | 服务端口 |

> 启用鉴权后，`curl` 调用需加 `-H "X-API-Key: <你的key>"`，否则返回 401；`/health` 始终匿名可访问以便探活。

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

## 需要单独发送的文件清单（模型/权重）

以下文件因体积大或涉及第三方数据，**不随 Git 仓库分发**。克隆代码后，需由项目作者通过微信/网盘单独发送，并按「应放入的相对目录」放置：

| 文件 | 当前路径（本机） | 应放入的项目相对目录 | 用途 |
| --- | --- | --- | --- |
| `yolo11n-pose-fallen.pt` | `algorithm/model-v2-resources/weights/yolo11n-pose-fallen.pt` | `algorithm/models/yolo11n-pose-fallen.pt` | YOLO 姿态估计权重（v2，躺地人体微调，约 5.9 MB） |
| `last.pth`（v2 LSTM） | `algorithm/models/models/checkpoints/v2_hard_samples_20260903/last.pth` | `algorithm/models/models/checkpoints/v2_hard_samples_20260903/last.pth` | LSTM 分类器检查点（8 特征二分类，约 2.4 MB） |
| `best.pth`（v1 LSTM，可选） | `algorithm/models/models/checkpoints/best.pth` | `algorithm/models/models/checkpoints/best.pth` | LSTM v1 检查点（5 特征三分类，用于回退对照，约 792 KB） |
| `yolo11n-pose.pt`（v1，可选） | 待确认 | `algorithm/models/yolo11n-pose.pt` | 官方基础姿态权重，作为 v2 缺失时的回退 |

> 权重文件校验：`yolo11n-pose-fallen.pt` 的 MD5 为 `6a6394629424bcb72a7e20596c07bb9d`，放置后可据此核对。

## 许可

本项目基于 [MIT License](LICENSE) 开源。数据库与算法权重如涉及第三方数据，请遵循其原始许可。
