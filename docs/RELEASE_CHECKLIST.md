# 发布检查清单与回归资产

> 本文件是阶段 4 的交付物：**角色 × 接口权限矩阵**、**账号冒烟用例表**、**发布前检查清单** 与 **已知遗留**。  
> 所有数据均为 2026-11-20 在本机实测（MySQL 8 / 后端 8080 / 前端 5173 / 算法 8001）结果，不是设计推演。

---

## 一、角色 × 接口权限矩阵（实测）

图例：`200` 允许 ｜ `403` 拒绝（角色不符）｜ `404` 拒绝（无可访问档案，不泄露是否存在）｜ `401` 未登录

复现账号：admin=13800000000、staff=13800001111、expert=13800001001、caregiver=13800002101、supervisor=13800002201、elder=13800003001（密码均 `123456`）。

| 接口                                                                                                                                  | admin | staff | expert  | caregiver | supervisor | elder   |
| ----------------------------------------------------------------------------------------------------------------------------------- | ----- | ----- | ------- | --------- | ---------- | ------- |
| `/api/user/list`                                                                                                                    | 200   | 200   | 403     | 403       | 403        | 403     |
| `/api/log/list`                                                                                                                     | 200   | 200   | 403     | 403       | 403        | 403     |
| `/api/backup/list`                                                                                                                  | 200   | 200   | 403     | 403       | 403        | 403     |
| `/api/admin/chat/conversations`                                                                                                     | 200   | 200   | 403     | 403       | 403        | 403     |
| `/api/supervisor/portal/me`、`/team`                                                                                                 | 403   | 403   | 403     | 403       | **200**    | 403     |
| `/api/expert/portal/me`、`/workbench`                                                                                                | 404   | 404   | **200** | 404       | 404        | 404     |
| `/api/caregiver/portal/me`、`/workbench`                                                                                             | 404   | 404   | 404     | **200**   | **200**    | 404     |
| `/api/elder/portal/me`、`/health`                                                                                                    | 404   | 404   | 404     | 404       | 404        | **200** |
| `/api/elder/list`、`/alert/list`、`/fall/list`、`/consult/list`、`/device/list`、`/space/list`、`/intervention/list`、`/notification/list` | 200   | 200   | 200     | 200       | 200        | 200     |
| `/api/stats/overview`、`/model/params`（读）、`/preference/config`（读）                                                                    | 200   | 200   | 200     | 200       | 200        | 200     |
| 上述接口**未登录**                                                                                                                         | 401   | 401   | 401     | 401       | 401        | 401     |

关键结论：

1. **默认拒绝生效**：未携带 Token 访问 5 个抽样业务接口全部 401；登录页/文档/`/uploads/**` 为白名单。
2. **管理写接口收口**：`user`、`log`、`backup`、`admin/chat` 四组接口非 admin/staff 一律 403。
3. **门户接口隔离**：四端门户只对本端角色开放；非本端返回 404「XX 档案不存在」而非 403，可避免探测账号是否存在。
4. **业务读接口全员可读但按关系过滤**：`elder/list` 等对 6 类角色均返回 200，但内容受 `DataScopeUtil` 约束  
   （护工只见自己负责的老人），因此"能调用"不等于"能看全"。
5. **设计说明（非缺陷）**：`stats/overview`（聚合统计）、`model/params`（只读）、`preference/config`（只读）  
   当前对所有登录角色开放，写操作仍限 admin/staff。若要进一步收敛，需确认专家/陪护首页是否依赖这些读接口。

---

## 二、账号冒烟用例表（13 个账号）

接口层：13 个账号全部登录成功（HTTP 200 且 `code=200`），角色与库内 `user.role` 一致。

UI 层（Playwright 真实登录，headless Chromium）：

| 账号       | 手机号         | 角色         | 落地页                    | 可见菜单数 | JS 错误 | 5xx 请求 | 结果   |
| -------- | ----------- | ---------- | ---------------------- | ----- | ----- | ------ | ---- |
| admin    | 13800000000 | admin      | `/home/management`     | 25    | 0     | 0      | PASS |
| zhangsan | 13800001111 | staff      | `/home/management`     | 25    | 0     | 0      | PASS |
| expert01 | 13800001001 | expert     | `/expert/workbench`    | 6     | 0     | 0      | PASS |
| cg01     | 13800002101 | caregiver  | `/caregiver/workbench` | 8     | 0     | 0      | PASS |
| sup01    | 13800002201 | supervisor | `/caregiver/workbench` | 10    | 0     | 0      | PASS |
| fam01    | 13800003001 | elder      | `/elder/home`          | 6     | 0     | 0      | PASS |

> 其余 7 个账号（lisi/wangwu/zhaoliu/expert02/cg02/sup02/fam02）在接口层验证通过；  
> UI 层与上表同角色账号共用同一路由与菜单配置，不重复截图验证。

数据隔离实测：

| 账号           | 可见老人            | 说明                       |
| ------------ | --------------- | ------------------------ |
| cg01 周丽华（朝阳） | 3 人：张建国、赵玉兰、钱淑珍 | 与 `elder_caregiver` 关系一致 |
| cg02 吴晓芳（海淀） | 2 人：李秀英、刘德胜     | 两护工交集为空，行级隔离生效           |
| fam01 张小明    | 张建国             | 仅本人关联老人                  |
| fam02 李大明    | 李秀英             | 同上                       |

---

## 三、发布前检查清单

### 3.1 配置与启动

- [ ] `JWT_SECRET` 已通过环境变量提供，**长度 ≥ 32 位**（未配置时本地会由 `LocalEnvBootstrap` 自动生成到 `backend/.jwt-secret.local`；生产环境必须显式设置）
- [ ] `DB_PASSWORD` 已提供（仓库不再内置默认值；本地可由 `backend/.env.local` 提供，模板见 `.env.local.example`）
- [ ] 数据库已导入 `init.sql`（37 张表）与 `seed_data.sql`
- [ ] 前端 `vite.config.ts` 的代理目标与后端端口一致
- [ ] 后端 `app.cors.allowed-origins` 覆盖实际前端地址（默认 5173/4173 各含 localhost 与 127.0.0.1）
- [ ] 算法权重已按 `algorithm/README.md` 第三节放置，`/health` 返回 `loaded: true`
- [ ] 生产部署设置随机 `FALL_API_KEY`（留空 = 不鉴权），并收紧算法 CORS

### 3.2 功能回归

- [ ] 13 个账号可登录，5 类角色落地页正确（见第二节）
- [ ] 管理员端 13 个模块均可进入，无空白页、无重复 toast
- [ ] 专家端：咨询回复、排班请假申请
- [ ] 陪护端：今日任务、告警处置、空间巡检、联系家属
- [ ] 主管端：排班保存、请假审批、全组视角
- [ ] 老人端：健康概览 5 指标趋势、在线咨询与打分、一键呼叫
- [ ] 系统管理：模型参数保存/重置/回滚、偏好配置、备份创建与列表、日志查询/导出/清理、对话监管
- [ ] 算法：`/health` 匿名 200、`models/status` 带 key 200 / 无 key 401、`detect` 参数越界 400、伪造视频 400、`detect/file` 外部路径 400

### 3.3 契约与异常

- [ ] 任取 50+ 接口，**HTTP 状态码 == 响应体 `code`**（本次实测 68 个静态 GET 接口不一致数 0）
- [ ] 分页 `page/size`、`current/size`、`pageNum/pageSize` 三种写法结果一致，`size=2` 只返回 2 条
- [ ] 非法分页/日期/缺参 → 400 且文案可读（缺参实测返回「缺少必要参数 elderId」）
- [ ] 404 路径、403 越权、500 异常均不含堆栈与服务器绝对路径

### 3.4 本次回归实测汇总

| 项目                      | 结果                           |
| ----------------------- | ---------------------------- |
| 13 账号登录                 | 13/13 PASS                   |
| 权限矩阵 23 个接口 × 6 角色      | 全部符合预期（拒绝均为 403/404，无越权 200） |
| 静态 GET 接口 HTTP/code 一致性 | 68 个，不一致 **0**，5xx **0**     |
| UI 冒烟 6 个代表账号           | 全部 PASS（0 JS 错误、0 个 5xx 请求）  |
| 数据行级隔离                  | PASS（交集为空）                   |
| 算法服务 7 项校验              | 全部 PASS                      |

---

## 四、已知遗留（不阻塞发布，需知悉）

| # | 事项                                                                                                                             | 影响                                        | 建议                                             |
| - | ------------------------------------------------------------------------------------------------------------------------------ | ----------------------------------------- | ---------------------------------------------- |
| 1 | `/api/fall-detection/detect/async` 不可用：流水线在 `process_video` 注册 `signal`，在线程池调用抛 `signal only works in main thread`，任务直接 failed | 异步检测不可用，需改用同步 `/detect`                   | 后续把信号处理改为可选开关（`register_signal=False`）         |
| 2 | 算法 CORS 仍为 `allow_origins=['*']` 且 `allow_credentials=True`                                                                    | 浏览器侧来源不受限                                 | 生产部署收窄为前端域名白名单                                 |
| 3 | 算法鉴权默认关闭（`FALL_API_KEY` 为空即不校验）                                                                                                | 内网演示方便，公网暴露风险                             | 公网部署必须设置随机 Key                                 |
| 4 | `stats/overview`、`model/params`（读）、`preference/config`（读）对所有登录角色开放                                                             | 聚合统计与非敏感只读配置可被任意角色读取                      | 若需收敛，先确认专家/陪护首页是否依赖                            |
| 5 | 注册/找回密码依赖邮箱验证码，演示环境验证码写在后端日志                                                                                                   | 无邮件通道时无法真实收码                              | 接入 SMTP 或短信网关                                  |
| 6 | 推理分辨率固定 640，姿态权重按 1024 微调                                                                                                      | 远处小目标召回偏低                                 | 按视频分辨率动态设置 `imgsz`                             |
| 7 | **数据备份为占位实现**：`DataBackupServiceImpl.execMysqlDump` 仅 `createNewFile()` 生成空文件，未真正执行 `mysqldump`；备份根目录硬编码 `D:/elderly_backup/`  | 备份列表有记录但文件为空，**无法用于真实恢复**；「恢复」按钮同理不产生实际效果 | 接入真实 `mysqldump`（或逻辑导出）并把目录改配置项；在此之前不要把备份当灾备手段 |

---

## 五、阶段 4 期间修复的既有缺陷

以下两项是本次全链路回归**实测发现**并当场修复的（原属阶段 2 契约范畴的漏项）：

|   |   |
| - | - |

1. **缺少必填参数返回 500**：`GET /api/echart/monitor/heartbeat|acceleration` 不带 `elderId` 时抛  
   `MissingServletRequestParameterException` 落到兜底 500。已在 `GlobalExceptionHandler` 增加 400 映射，  
   实测返回 `{"code":400,"message":"缺少必要参数 elderId"}`。
2. **日志导出/清理 500「SQL映射配置异常」**：`OperationLogMapper.deleteBeforeDate` / `selectExportList`  
   只有接口声明、无 SQL 映射。已用注解补齐（`DELETE ... WHERE operate_time < ?` 与条件查询），  
   实测导出返回数据、清理返回「清理成功」、非法日期仍 400。
3. **IDE 终端直接 `java -jar` 启动失败**（`jwt.secret 未配置或长度不足 32 字节`）：阶段 0 移除默认密钥后，  
   只有 `02-start-backend.bat` 会补密钥，手工启动没人补。已新增启动引导 `LocalEnvBootstrap`：  
   容器启动前读取 `backend/.env.local` 补齐未设置的变量，缺少 `JWT_SECRET` 时读取/生成  
   `backend/.jwt-secret.local`（优先复用任意位置已有文件，不在项目根重复生成），**不覆盖已设置的环境变量**。  
   实测三种方式均可零配置启动：`java -jar`（backend/ 与项目根目录）、`mvn spring-boot:run`。
4. **换机器可移植性**：备份目录硬编码 `D:/elderly_backup/`（没有 D 盘的机器备份必失败）、附件目录写死 `uploads/`。
   已改为 `app.backup.dir` / `app.upload.dir`（默认相对启动目录，`APP_BACKUP_DIR` / `APP_UPLOAD_DIR` 可覆盖），
   并把 `.env.local.example` 纳入仓库，首次启动自动生成 `.env.local`。用「暂存区内容单独导出后从零构建启动」验证通过
   （8083 端口：文档 200、登录 200、`stats/overview`/`log/list`/`user/list`/`elder/list` 均 200、备份落到 `backend/backup/`）。
