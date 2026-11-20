# 架构说明（ARCHITECTURE）

## 总体结构

```
浏览器 (localhost:5173 dev / 4173 preview)
   │  /api  (Vite dev / preview proxy)
   ▼
Spring Boot 后端 (localhost:8080)  ──►  MySQL 8 (localhost:3306 / elderly_care)
   ▲
   │  HTTP（可选，独立部署；当前由接口文档/脚本直接调用）
FastAPI 跌倒检测服务 (localhost:8001)  ──►  模型工程（YOLO11n-pose + LSTM）
```

> 后端与算法服务为**并列独立**关系，无相互 HTTP 调用；两者可由同一前端或外部调用方分别接入。
> 算法服务可选用 `FALL_API_KEY` 开启 `X-API-Key` 鉴权，`/health` 始终匿名以便探活。

## 四端与 5 类角色

四个前端端 + 5 类数据库角色（`user.role` 字段取值）：

| 端 | 角色码 | 演示账号数 | 职责范围 |
| --- | --- | --- | --- |
| 管理员端 | `admin` / `staff` | 1 + 4 | 全局管理：统计、老人档案、设备、咨询、陪护、告警、空间、干预、日程、对话监管、系统管理 |
| 专家端 | `expert` | 2 | 我的咨询、排班请假、只读健康档案、干预建议 |
| 陪护端 | `caregiver` | 2 | 负责老人、今日任务、告警处置、健康记录、空间巡检、联系家属 |
| 陪护主管 | `supervisor` | 2 | 排班管理、请假审批、全组视角 + 陪护端全部功能 |
| 老人端（家属代办） | `elder` | 2 | 健康概览、在线咨询与打分、联系护工、提醒、一键呼叫 |

> 合计 **13 个演示账号**（9 个业务演示 + 4 个 staff 内置账号），密码统一 `123456`，详见根目录 README。

## 模块职责

| 模块 | 技术栈 | 端口 | 职责 |
| --- | --- | --- | --- |
| frontend | Vue3 + TS + Vite + Element Plus + ECharts + Pinia | 5173（dev）/ 4173（preview） | 四端界面、大屏图表、业务交互 |
| backend | Spring Boot 3.3.4 + MyBatis-Plus + MySQL | 8080 | REST 接口、JWT 鉴权、RBAC、业务逻辑、数据访问 |
| algorithm | FastAPI + PyTorch + Ultralytics | 8001 | 视频跌倒检测，输出事件 JSON 与标注视频 |

## 认证与授权

1. **身份**：登录（手机号 + 明文密码，后端 BCrypt 比对）签发 JWT，`Authorization: Bearer <token>` 携带；
   生产环境 `JWT_SECRET` 无内置默认值，长度 < 32 位直接启动失败。本地开发由 `LocalEnvBootstrap` 在容器启动前
   自动补齐（读 `backend/.env.local`，缺失密钥时读/生成 `backend/.jwt-secret.local`），不覆盖已有环境变量。
2. **默认拒绝**：`JwtAuthenticationFilter` 对**所有**路径默认要求有效 Token，仅放行白名单
   （`/api/auth/**`、Knife4j 文档、`/uploads/**`、`/favicon.ico`、`/error`、`OPTIONS` 预检）；
   未携带或无效 Token 直接返回 401 JSON，不进入 Controller。
3. **RBAC**：`@RequireRole` 声明 + `RoleAuthInterceptor` 校验，**以数据库中的真实角色为准**，
   不信任 JWT 内的 `role` claim；越权返回 403。
4. **数据行级隔离**：按角色 + 服务关系（`elder_caregiver` / `elder_expert` / `elder_family`）过滤，
   一个账号只能访问本人服务对象的数据。
5. **前端**：路由 `meta.roles` 仅作展示层拦截，真实权限以后端 `/api/user/profile` 返回的角色为准。

## 数据流

1. 前端页面通过 `/api` 调用后端；Vite 将请求代理到 8080（后端无 `/api` 前缀，代理不 rewrite）。
2. 后端读写 MySQL `elderly_care`（**37 张表**，演示数据由 `seed_data.sql` 导入）。
3. 算法服务独立提供 HTTP 接口（8001），接收视频并输出事件 JSON 与标注视频；
   当前前端与后端均未直接调用该服务，属可独立验证的推理微服务，事件回写告警为后续扩展能力
   （见 `algorithm/README.md` 第十二节）。

## 关键约定

- 后端统一响应 `{code, message, data, timestamp}`，业务成功 `code=200`，且 **HTTP 状态码与 body.code 一致**；
- 分页返回 `{total, list}`，入参接受 `page/size`（推荐）、`current/size`、`pageNum/pageSize` 三种写法，
  由 `PaginationAliasFilter` 归一化；非法分页值返回 400 友好提示；
- 异常统一由 `GlobalExceptionHandler` 处理：400 参数错误、401 未认证、403 越权、404 不存在、429 限流、500 脱敏；
- 前端 axios baseURL=`/api`，拦截器统一提示 401/403/404/500（800ms 内相同提示去重），
  并注册全局渲染错误兜底避免空白页；
- 算法服务响应结构同为 `{code, message, data, timestamp}`，错误信息不回显服务器绝对路径；
- 后端接口文档：Knife4j `/doc.html`，OpenAPI `/v3/api-docs`；算法接口文档：`/docs`。

## 运行顺序

数据库（初始化）→ 后端（8080）→ 前端（5173）→ 算法（8001，按需）。
详见根目录 `README.md` 的“快速开始”，回归与发布检查见 `docs/RELEASE_CHECKLIST.md`。
