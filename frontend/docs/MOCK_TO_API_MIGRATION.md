# 模拟数据替换为后端 API 完整改造方案

> 项目：朝夕护行管理系统 (older-protect)
> 生成日期：2026-08-07
> 文档用途：供前端开发团队和后端开发团队共同参考

---

## 一、模拟数据清单

### 1.1 概览统计

| 序号 | 文件位置 | 模拟数据变量名 | 数据条数 | 页面/组件 | 替换优先级 |
|------|---------|--------------|---------|----------|-----------|
| 1 | `src/stores/elderData.ts:196` | `elderData` | 12 条 | Echart-monitor, 3-1base | **P0-紧急** |
| 2 | `src/stores/elderData.ts:40` | `fallHistoryMap` | 11 组 | Echart-monitor, 3-3fall-record | **P0-紧急** |
| 3 | `src/stores/dashboard.ts:18` | `defaultCards` | 8 张卡片 | 1management (管理主页) | **P0-紧急** |
| 4 | `src/views/3elder/3-2gait.vue:359` | `mockData` | 10 条 | 步态历史页 | **P0-紧急** |
| 5 | `src/views/3elder/3-3fall-record.vue:380` | `mockData` | 10 条(含重复) | 既往跌倒记录页 | **P0-紧急** |
| 6 | `src/views/3elder/3-4health-note.vue:360` | `mockData` | 9 条 | 健康备注页 | **P0-紧急** |
| 7 | `src/views/7expert.vue:879` | `mockExperts` | 6 条 | 专家管理页 | **P1-重要** |
| 8 | `src/views/7expert.vue:961` | `mockConsults` | 8 条 | 专家管理页 | **P1-重要** |
| 9 | `src/views/8caregiver.vue:294` | `mockData` | 10 条 | 陪护人员管理页 | **P1-重要** |
| 10 | `src/views/10alert/10-1list.vue:434` | `mockData` | 10 条 | 实时告警列表页 | **P0-紧急** |
| 11 | `src/views/10alert/10-2push-log.vue:369` | `mockData` | 15 条 | 家属推送日志页 | **P1-重要** |
| 12 | `src/views/10alert/10-3community.vue:595` | `mockData` | 15 条 | 社区处理台账页 | **P1-重要** |
| 13 | `src/views/11space.vue:690` | `mockData` | 20 条 | 居家空间档案页 | **P1-重要** |
| 14 | `src/views/12intervention.vue:936` | `mockData` | 7 条 | 干预方案管理页 | **P1-重要** |
| 15 | `src/views/13schedule.vue:596` | `mockTasks` | 12 条 | 日常康养日程页 | **P1-重要** |
| 16 | `src/views/14system/14-2log.vue:310` | `mockData` | 12 条 | 操作日志页 | **P2-普通** |
| 17 | `src/views/14system/14-3backup.vue:286` | `mockData` | 10 条 | 数据备份页 | **P2-普通** |
| 18 | `src/views/Echart-monitor.vue:534` | 图表硬编码数据 | 5+7+50 条 | 监控管理页 | **P2-普通** |
| 19 | `src/views/10alert/10-4preference.vue` | 表单默认值 | — | 通知偏好设置页 | **P2-普通** |

### 1.2 详细逐条分析

#### #1 — 老人基础数据 (`stores/elderData.ts`)

**字段结构：**
```typescript
interface ElderVO {
  id: number            // 主键 ID
  code: string          // 身份码 (8位大写字母+数字)
  name: string          // 姓名
  gender: string        // 性别 (男/女)
  age: number           // 年龄
  idCard: string        // 身份证号
  birthDate: string     // 出生日期
  ethnicity: string     // 民族
  phone: string         // 联系电话(脱敏)
  address: string       // 居住地址
  residenceType: string // 居住类型 (独居/与子女同住/养老院)
  remark: string        // 备注
  createTime?: string   // 创建时间
  updateTime?: string   // 更新时间
}
```

**已有 API 文件：** `src/api/elder.ts` — 接口已定义但组件未使用

**使用组件：**
- `src/views/Echart-monitor.vue` (line 457) — 使用静态 `elderData` 和 `fallHistoryMap`
- `src/views/3elder/3-1base.vue` — 已通过 `useElderStore` 调用 API，但 store 内部有静态 fallback
- `src/views/6device.vue` — 绑定设备时选老人

---

#### #2 — 跌倒历史 (`stores/elderData.ts`)

**字段结构：**
```typescript
interface FallRecord {
  date: string       // 跌倒时间
  location: string   // 跌倒地点
  cause: string      // 跌倒原因
  severity: string   // 严重程度 (轻微/中度/重度)
  status: string     // 恢复状态 (已恢复/康复中/后遗症)
}
```

**已有 API 文件：** `src/api/fall.ts` — 接口已定义（`getFallList`, `getFallById`）

**使用组件：**
- `src/views/Echart-monitor.vue` — 跌倒历史弹窗
- `src/views/3elder/3-3fall-record.vue` — 独立页面的 mock 数据（#5）

---

#### #3 — 仪表盘卡片 (`stores/dashboard.ts`)

**字段结构：**
```typescript
interface Card {
  id: string          // 卡片唯一ID ('1' ~ '8')
  name: string        // 卡片名称
  icon: string        // Element Plus 图标名
  type: string        // 类型 (统计)
  visible: boolean    // 是否显示
  colspan: number     // 列宽 (1 或 2)
  color: string       // 图标背景色
  previewValue: number // 预览值（默认值）
  value: number       // 当前值（运行时更新）
}
```

**当前行为：** 8 张卡片的 `previewValue` 是硬编码的，卡片 ID 7（今日预警）和 ID 8（在线设备）通过 `refreshCardValues()` 从 API 刷新，其余始终使用默认值。

---

#### #4 — 步态历史 (`views/3elder/3-2gait.vue`)

**字段结构：**
```typescript
interface GaitRecord {
  name: string       // 老人姓名
  date: string       // 测量日期
  speed: number      // 步速 (m/s)
  frequency: number  // 步频 (步/分)
  stepLength: number // 步长 (cm)
  score: number      // 步态评分 (0-100)
  result: string     // 评估结果 (正常/异常/高风险)
}
```

**⚠ 无对应 API 文件，需新建。**

---

#### #5 — 跌倒记录独立页面 (`views/3elder/3-3fall-record.vue`)

**字段结构（比 #2 更丰富）：**
```typescript
interface FallRecordDetail {
  name: string       // 老人姓名
  date: string       // 跌倒时间
  location: string   // 跌倒地点
  cause: string      // 跌倒原因
  injury: string     // 受伤部位
  severity: string   // 严重程度 (轻微/中度/重度)
  treatment: string  // 处理方式
  status: string     // 恢复状态 (已恢复/康复中/后遗症)
  handler: string    // 处理人
}
```

---

#### #6 — 健康备注 (`views/3elder/3-4health-note.vue`)

**字段结构：**
```typescript
interface HealthNote {
  name: string       // 老人姓名
  date: string       // 记录日期
  recorder: string   // 记录人
  type: string       // 类型 (健康观察/医嘱/用药提醒/心理关怀/日常护理)
  content: string    // 备注内容
  importance: string // 重要性 (普通/重要/紧急)
  status: string     // 状态 (已完成/跟进中/待跟进)
}
```

**⚠ 无对应 API 文件，需新建。**

---

#### #7 — 专家数据 (`views/7expert.vue`)

**mockExperts 字段：**
```typescript
interface Expert {
  name: string       // 姓名
  title: string      // 职称 (主任医师/副主任医师/主治医师/康复师)
  specialty: string  // 专业领域 (老年医学/康复理疗)
  institution: string// 所属机构
  phone: string      // 联系电话
  status: string     // 在线状态 (online/busy/offline)
  todayCount: number // 今日咨询数
  totalCount: number // 累计咨询数
  rating: number     // 评分
  schedule: string   // 出诊时间
}
```

**mockConsults 字段：**
```typescript
interface Consultation {
  elderName: string  // 老人姓名
  type: string       // 咨询类型 (健康咨询/用药指导/康复建议)
  expertName: string // 专家姓名 (为空表示待分配)
  status: string     // 状态 (pending/ongoing/completed)
  urgency: string    // 紧急程度 (urgent/important/normal)
  time: string       // 咨询时间
  content: string    // 咨询内容
  reply: string      // 专家回复
  rating: number     // 评分 (可为null)
  feedback: string   // 反馈
}
```

**⚠ 无对应 API 文件，需新建。**

---

#### #8 — 陪护人员 (`views/8caregiver.vue`)

**字段结构：**
```typescript
interface Caregiver {
  name: string       // 姓名
  age: number        // 年龄
  position: string   // 职务 (陪护主管/高级陪护员/陪护员/康复护理师/心理辅导师)
  phone: string      // 联系电话
  address: string    // 住址
  status: string     // 状态 (在职/离职)
}
```

**⚠ 无对应 API 文件，需新建。**

---

#### #10 — 实时告警列表 (`views/10alert/10-1list.vue`)

**字段结构：**
```typescript
interface AlertItem {
  elderName: string     // 老人姓名
  alertType: string     // 告警类型 (跌倒检测/SOS紧急呼叫/心率异常/设备离线/血压异常/活动量异常/睡眠质量差)
  level: string         // 级别 (urgent/important/normal)
  description: string   // 告警描述
  time: string          // 告警时间
  location: string      // 位置
  status: string        // 状态 (pending/processing/resolved/ignored)
  handler: string       // 处理人
  handleTime: string    // 处理时间
  handleNote: string    // 处理备注
  read: boolean         // 已读标记
}
```

**已有 API 文件：** `src/api/alert.ts` — 接口已定义（`getAlertList`, `processAlert`, `ignoreAlert`, `markAlertRead`）

**当前状态：** 2warning.vue 已通过 `alertStore.fetchAlerts()` 调用 API，但 10-1list.vue 仍使用独立 mock 数据。

---

#### #11 — 家属推送日志 (`views/10alert/10-2push-log.vue`)

**字段结构：**
```typescript
interface PushLog {
  elderName: string      // 老人姓名
  familyPhone: string    // 家属电话
  channel: string        // 推送渠道 (APP推送/微信推送/短信推送/电话语音/邮件推送)
  scene: string          // 推送场景 (跌倒告警/心率异常/SOS呼叫/设备离线/血压异常/活动异常/定期报告)
  content: string        // 简要内容
  fullContent: string    // 完整推送内容
  status: string         // 状态 (success/failed/sending)
  time: string           // 推送时间
  duration: string       // 耗时
  retryCount: number     // 重试次数
  response: string       // 响应JSON
  retryHistory: Array<{  // 重试历史
    attempt: number
    time: string
    status: string
    error: string
  }>
}
```

**⚠ 无对应 API 文件，需新建。**

---

#### #12 — 社区处理台账 (`views/10alert/10-3community.vue`)

**字段结构：**
```typescript
interface CommunityRecord {
  code: string           // 处理编号
  elderName: string      // 老人姓名
  alertType: string      // 告警类型
  action: string         // 处理措施 (上门查看/联系家属/送医就诊/设备检查/电话回访)
  handler: string        // 处理人
  handlerId: string      // 处理人编号
  result: string         // 处理结果 (已解决/跟进中/待复查/无法解决)
  reviewStatus: string   // 审核状态 (pending/approved/rejected)
  time: string           // 处理时间(短)
  fullTime: string       // 完整时间
  description: string    // 处理详情
  attachments: string[]  // 附件
  community: string      // 所属社区
  alertCode: string      // 关联告警编号
  reviewer: string       // 审核人
  reviewTime: string     // 审核时间
  rejectReason: string   // 驳回原因
}
```

**⚠ 无对应 API 文件，需新建。**

---

#### #13 — 居家空间档案 (`views/11space.vue`)

**字段结构：**
```typescript
interface SpaceRecord {
  code: string                // 空间编号
  name: string                // 空间名称
  building: string            // 楼栋
  floor: number               // 楼层
  layout: string              // 户型 (两室一厅/一室一厅/独居公寓)
  area: number                // 面积 (㎡)
  occupancyStatus: string     // 入住状态 (occupied/vacant)
  elderName: string           // 居住老人
  moveInDate: string          // 入住日期
  lastOccupied: string        // 最后居住日期
  facilities: string[]        // 适老化设施
  risks: string[]             // 风险因素
  status: string              // 空间状态 (normal/renovating/repair/inactive)
  safetyRating: string        // 安全评级 (A/B/C/D)
  lastInspection: string      // 最近检查日期
  lastInspectionResult: string// 最近检查结果
  nextInspection: string      // 下次检查日期
  remark: string              // 备注
  inspectionHistory: Array<{  // 检查历史
    date: string
    result: string
    inspector: string
  }>
}
```

**⚠ 无对应 API 文件，需新建。**

---

#### #14 — 干预方案管理 (`views/12intervention.vue`)

**字段结构：**
```typescript
interface InterventionPlan {
  code: string                // 方案编号
  name: string                // 方案名称
  type: string                // 干预类型 (综合干预/营养干预/心理疏导/运动康复/用药管理/日常护理)
  priority: string            // 优先级 (urgent/important/normal)
  status: string              // 状态 (draft/pending/approved/ongoing/completed/terminated)
  leader: string              // 负责人
  targetGroup: string         // 适用对象描述
  totalWeeks: number          // 执行总周数
  currentWeek: number         // 当前周
  frequency: string           // 执行频率
  startDate: string           // 开始日期
  endDate: string             // 结束日期
  createTime: string          // 创建时间
  rating: number              // 评分
  avgExecRating: number       // 平均执行评分
  progress: number            // 进度 (0-100)
  executedCount: number       // 已执行次数
  totalCount: number          // 总次数
  abnormalCount: number       // 异常次数
  associatedElders: string[]  // 关联老人
  goals: string[]             // 干预目标
  measures: string[]          // 具体措施
  notes: string               // 注意事项
  stepIndex: number           // 当前步骤
  steps: Array<{              // 审批步骤链
    key: string
    title: string
    time: string
    status: string            // finish/process/wait
  }>
  records: Array<{            // 执行记录
    date: string
    elderName: string
    executor: string
    status: string
    rating: number
    note: string
    attachments: string[]
  }>
}
```

**⚠ 无对应 API 文件，需新建。**

---

#### #15 — 日常康养日程 (`views/13schedule.vue`)

**字段结构：**
```typescript
interface ScheduleTask {
  code: string           // 任务编号
  taskType: string       // 任务类型 (数据复核/设备巡检/设备佩戴检查/风险评估/家属沟通)
  elderName: string      // 老人姓名
  location: string       // 地点
  priority: string       // 优先级 (urgent/important/normal)
  status: string         // 状态 (pending/processing/completed/cancelled)
  triggerReason: string  // 触发原因
  createTime: string     // 创建时间
  relatedAlert: string   // 关联告警编号
  relatedDevice: string  // 关联设备
  result: string         // 处理结果
  handler: string        // 处理人
  completeTime: string   // 完成时间
  note: string           // 备注
}
```

**⚠ 无对应 API 文件，需新建。**

---

#### #16 — 操作日志 (`views/14system/14-2log.vue`)

**字段结构：**
```typescript
interface OperationLog {
  time: string       // 操作时间
  user: string       // 操作用户
  type: string       // 操作类型 (登录/登出/新增/编辑/删除/审核/处理/导出)
  module: string     // 操作模块
  content: string    // 操作内容
  ip: string         // IP 地址
  status: string     // 状态 (success/failed)
  error: string      // 错误信息
}
```

**⚠ 无对应 API 文件，需新建。**

---

#### #17 — 数据备份 (`views/14system/14-3backup.vue`)

**字段结构：**
```typescript
interface BackupRecord {
  fileName: string   // 文件名
  time: string       // 备份时间
  type: string       // 备份类型 (全量备份/增量备份)
  size: string       // 文件大小
  status: string     // 状态 (success/failed)
}
```

**⚠ 无对应 API 文件，需新建。**

---

#### #18 — EChart 监控页 (`views/Echart-monitor.vue`)

**硬编码图表数据：**
- 设备状态饼图：`data: [{value:4,name:'在线'},{value:1,name:'离线'},...]` (line 534-540)
- 加速度曲线：`data: [-0.1, -0.2, 0, 0.2, 0.2, 0.1, 0.4]` (line 566)
- 心跳曲线：`generateHeartData(50)` (line 580) — 随机生成模拟数据

**已有 API：** `src/api/stats.ts` 中的 `getMonitorHeartbeat()`, `getMonitorDeviceStatus()`, `getMonitorAcceleration()` 接口已定义。

---

## 二、API 接口设计文档（按模块分组）

### 通用规范

**Base URL：** `/api`

**统一请求头：**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**统一响应格式：**
```json
{
  "code": 200,
  "message": "success",
  "data": <具体数据>,
  "timestamp": "2026-08-07T12:00:00.000Z"
}
```

**分页响应格式：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "list": [ ... ]
  },
  "timestamp": "2026-08-07T12:00:00.000Z"
}
```

**查询参数命名规范：**
- 分页：`page`（页码，从1开始）, `size`（每页条数，默认10）
- 筛选：字段名与 VO 一致，如 `?name=张三&status=active`
- 排序：`sortField=createTime&sortOrder=desc`
- 日期范围：`startDate=2026-01-01&endDate=2026-12-31`
- 模糊搜索：`keyword=搜索词`

---

### 模块 A：老人管理 (Elder) — 已有 API，需后端实现

> 已有 API 文件：`src/api/elder.ts`

| # | 方法 | 路径 | 说明 | 请求参数 | 响应数据 |
|---|------|------|------|---------|---------|
| A1 | GET | `/api/elder/list` | 老人列表(分页) | `page, size, name?, phone?` | `PageVO<ElderVO>` |
| A2 | GET | `/api/elder/{id}` | 老人详情 | `id` (路径) | `ElderVO` |
| A3 | POST | `/api/elder` | 新增老人 | `ElderDTO` (Body) | `number` (新ID) |
| A4 | PUT | `/api/elder/{id}` | 编辑老人 | `id` (路径), `ElderDTO` (Body) | `void` |
| A5 | DELETE | `/api/elder/{id}` | 删除老人 | `id` (路径) | `void` |

**ElderVO 响应字段：**
```json
{
  "id": 1,
  "code": "AB12CD34",
  "name": "张桂花",
  "gender": "女",
  "age": 78,
  "idCard": "110101194601011234",
  "birthDate": "1946-01-01",
  "ethnicity": "汉族",
  "phone": "138****1234",
  "address": "北京市朝阳区朝阳路3栋502室",
  "residenceType": "独居",
  "remark": "患有高血压，需定期监测",
  "createTime": "2024-01-15T10:30:00",
  "updateTime": "2026-07-15T14:20:00"
}
```

> **⚠ 注意：** `code` 字段（身份码）需后端自动生成（8位字母+数字混编），前端不再用 `generateCode()`。

---

### 模块 B：跌倒记录 (Fall) — 已有 API，需后端实现

> 已有 API 文件：`src/api/fall.ts`

| # | 方法 | 路径 | 说明 | 请求参数 | 响应数据 |
|---|------|------|------|---------|---------|
| B1 | GET | `/api/fall/list` | 跌倒列表(分页) | `page, size, elderName?` | `PageVO<FallVO>` |
| B2 | GET | `/api/fall/{id}` | 跌倒详情 | `id` (路径) | `FallVO` |

**新增接口（3-3fall-record.vue 独立页面需要）：**

| # | 方法 | 路径 | 说明 | 请求参数 | 响应数据 |
|---|------|------|------|---------|---------|
| B3 | POST | `/api/fall` | 新增跌倒记录 | `FallCreateDTO` (Body) | `number` (新ID) |
| B4 | PUT | `/api/fall/{id}` | 编辑跌倒记录 | `id` (路径), `FallUpdateDTO` (Body) | `void` |
| B5 | DELETE | `/api/fall/{id}` | 删除跌倒记录 | `id` (路径) | `void` |

**FallVO 完整响应字段（合并 #2 和 #5 的数据）：**
```json
{
  "id": 1,
  "elderName": "张桂花",
  "elderId": 1,
  "date": "2024-07-10 14:30",
  "location": "卫生间",
  "cause": "地面湿滑",
  "injury": "手腕扭伤",
  "severity": "轻微",
  "treatment": "冰敷处理",
  "status": "已恢复",
  "handler": "王医生",
  "handleTime": "2024-07-10 15:00",
  "remark": "已通知家属"
}
```

**FallCreateDTO：**
```json
{
  "elderName": "张桂花",
  "date": "2024-07-10T14:30:00",
  "location": "卫生间",
  "cause": "地面湿滑",
  "injury": "手腕扭伤",
  "severity": "轻微",
  "treatment": "冰敷处理",
  "status": "已恢复",
  "handler": "王医生"
}
```

> **⚠ 注意：** 需要在 `src/api/fall.ts` 中补充 `addFall`, `updateFall`, `deleteFall` 三个接口函数。

---

### 模块 C：步态历史 (Gait) — 需新建 API

> ⚠ 需新建 API 文件：`src/api/gait.ts`

| # | 方法 | 路径 | 说明 | 请求参数 | 响应数据 |
|---|------|------|------|---------|---------|
| C1 | GET | `/api/gait/list` | 步态列表(分页) | `page, size, name?, result?, startDate?, endDate?` | `PageVO<GaitVO>` |
| C2 | GET | `/api/gait/{id}` | 步态详情 | `id` (路径) | `GaitVO` |
| C3 | POST | `/api/gait` | 新增步态记录 | `GaitCreateDTO` (Body) | `number` |
| C4 | DELETE | `/api/gait/{id}` | 删除步态记录 | `id` (路径) | `void` |
| C5 | GET | `/api/gait/stats` | 步态统计数据 | — | `GaitStatsVO` |

**GaitVO：**
```json
{
  "id": 1,
  "name": "张桂花",
  "date": "2026-07-14 08:30",
  "speed": 0.72,
  "frequency": 98,
  "stepLength": 52,
  "score": 78,
  "result": "正常"
}
```

**GaitCreateDTO：**
```json
{
  "name": "张桂花",
  "date": "2026-07-14T08:30:00",
  "speed": 0.72,
  "frequency": 98,
  "stepLength": 52,
  "result": "正常"
}
```

> **注意：** `score` 由后端计算返回，前端不再用 `Math.floor(Math.random() * 30 + 50)` 随机生成。

**GaitStatsVO：**
```json
{
  "total": 10,
  "avgSpeed": 0.65,
  "abnormalCount": 3,
  "lastAssessment": "2026-07-14"
}
```

---

### 模块 D：健康备注 (HealthNote) — 需新建 API

> ⚠ 需新建 API 文件：`src/api/healthNote.ts`

| # | 方法 | 路径 | 说明 | 请求参数 | 响应数据 |
|---|------|------|------|---------|---------|
| D1 | GET | `/api/health-note/list` | 健康备注列表(分页) | `page, size, name?, type?, importance?, status?, startDate?, endDate?` | `PageVO<HealthNoteVO>` |
| D2 | POST | `/api/health-note` | 新增备注 | `HealthNoteCreateDTO` (Body) | `number` |
| D3 | PUT | `/api/health-note/{id}` | 编辑备注 | `id` (路径), `HealthNoteCreateDTO` (Body) | `void` |
| D4 | DELETE | `/api/health-note/{id}` | 删除备注 | `id` (路径) | `void` |
| D5 | GET | `/api/health-note/stats` | 备注统计 | — | `HealthNoteStatsVO` |

**HealthNoteVO：**
```json
{
  "id": 1,
  "name": "张桂花",
  "date": "2026-07-14 08:30",
  "recorder": "王医生",
  "type": "健康观察",
  "content": "血压控制良好，建议继续服药",
  "importance": "普通",
  "status": "已完成"
}
```

**HealthNoteStatsVO：**
```json
{
  "total": 9,
  "monthNew": 3,
  "pendingFollow": 2,
  "importantCount": 4
}
```

---

### 模块 E：专家管理 (Expert) — 需新建 API

> ⚠ 需新建 API 文件：`src/api/expert.ts`

#### E-1：专家信息

| # | 方法 | 路径 | 说明 | 请求参数 | 响应数据 |
|---|------|------|------|---------|---------|
| E1 | GET | `/api/expert/list` | 专家列表 | `page?, size?, status?, specialty?` | `PageVO<ExpertVO>` |
| E2 | POST | `/api/expert` | 新增专家 | `ExpertCreateDTO` (Body) | `number` |
| E3 | PUT | `/api/expert/{id}` | 编辑专家 | `id` (路径), `ExpertCreateDTO` (Body) | `void` |
| E4 | DELETE | `/api/expert/{id}` | 删除专家 | `id` (路径) | `void` |
| E5 | GET | `/api/expert/stats` | 专家统计 | — | `ExpertStatsVO` |

**ExpertVO：**
```json
{
  "id": 1,
  "name": "王建国",
  "title": "主任医师",
  "specialty": "老年医学",
  "institution": "市第一人民医院",
  "phone": "138****1234",
  "status": "online",
  "todayCount": 4,
  "totalCount": 156,
  "rating": 4.9,
  "schedule": "周一至周五 08:00-17:00"
}
```

**ExpertStatsVO：**
```json
{
  "totalExperts": 6,
  "todayConsultations": 12,
  "pendingAssign": 3,
  "avgSatisfaction": 4.7
}
```

#### E-2：咨询记录

| # | 方法 | 路径 | 说明 | 请求参数 | 响应数据 |
|---|------|------|------|---------|---------|
| E6 | GET | `/api/consultation/list` | 咨询列表(分页) | `page?, size?, elderName?, expertName?, status?, urgency?` | `PageVO<ConsultationVO>` |
| E7 | POST | `/api/consultation` | 新建咨询 | `ConsultationCreateDTO` (Body) | `number` |
| E8 | PUT | `/api/consultation/{id}` | 更新咨询 | `id` (路径), `ConsultationUpdateDTO` (Body) | `void` |
| E9 | PUT | `/api/consultation/{id}/assign` | 分配专家 | `id` (路径), `{ expertName }` (Body) | `void` |
| E10 | PUT | `/api/consultation/{id}/reply` | 专家回复 | `id` (路径), `{ reply }` (Body) | `void` |

**ConsultationVO：**
```json
{
  "id": 1,
  "elderName": "张桂花",
  "type": "健康咨询",
  "expertName": "王建国",
  "status": "ongoing",
  "urgency": "urgent",
  "time": "2024-07-14 14:30",
  "content": "近期血压波动较大，伴有头晕症状，请求专家评估",
  "reply": "",
  "rating": null,
  "feedback": ""
}
```

---

### 模块 F：陪护人员 (Caregiver) — 需新建 API

> ⚠ 需新建 API 文件：`src/api/caregiver.ts`

| # | 方法 | 路径 | 说明 | 请求参数 | 响应数据 |
|---|------|------|------|---------|---------|
| F1 | GET | `/api/caregiver/list` | 陪护列表(分页) | `page?, size?, name?, status?, position?` | `PageVO<CaregiverVO>` |
| F2 | POST | `/api/caregiver` | 新增陪护人员 | `CaregiverCreateDTO` (Body) | `number` |
| F3 | PUT | `/api/caregiver/{id}` | 编辑陪护人员 | `id` (路径), `CaregiverCreateDTO` (Body) | `void` |
| F4 | DELETE | `/api/caregiver/{id}` | 删除陪护人员 | `id` (路径) | `void` |
| F5 | GET | `/api/caregiver/stats` | 陪护统计 | — | `CaregiverStatsVO` |

**CaregiverVO：**
```json
{
  "id": 1,
  "name": "张三",
  "age": 25,
  "position": "陪护主管",
  "phone": "1234567890",
  "address": "北京市海淀区中关村大街1号",
  "status": "在职"
}
```

**CaregiverStatsVO：**
```json
{
  "total": 10,
  "active": 8,
  "female": 4,
  "male": 6
}
```

---

### 模块 G：告警信息中心 (Alert) — 大部分已有 API

> 已有 API 文件：`src/api/alert.ts`，但 10-1list 仍需改造

#### G-1：实时告警 — 已有 API，需后端实现

| # | 方法 | 路径 | 说明 | 请求参数 | 响应数据 |
|---|------|------|------|---------|---------|
| G1 | GET | `/api/alert/list` | 告警列表 | `page, size, warningType?, priority?, status?, startDate?, endDate?, keyword?` | `PageVO<AlertVO>` |
| G2 | POST | `/api/alert/process/{id}` | 处理告警 | `id` (路径), `{ process, result }` (Body) | `string` |
| G3 | POST | `/api/alert/ignore/{id}` | 忽略告警 | `id` (路径) | `string` |
| G4 | PUT | `/api/alert/read/{id}` | 标记已读 | `id` (路径) | `string` |

> **⚠ 注意：** 10-1list.vue 中的 `mockData` 字段名（`elderName`, `alertType`, `level`）与 `api/alert.ts` 中定义的 `AlertVO` 字段名（`elderlyName`, `warningType`, `priority`）**不一致**，需要统一。

**建议统一后的 AlertVO：**
```json
{
  "id": 1,
  "elderlyName": "张桂花",
  "elderlyId": "E001",
  "warningType": "跌倒检测",
  "priority": "urgent",
  "description": "检测到老人在卫生间发生跌倒，请立即确认",
  "time": "2024-07-15 14:30:25",
  "location": "3栋502室-卫生间",
  "status": "pending",
  "handler": "",
  "handleTime": "",
  "handleNote": "",
  "contactPhone": "138****1234",
  "emergencyContact": "家属-张先生 139****0000",
  "suggestedAction": "立即上门查看，联系家属",
  "read": false
}
```

#### G-2：家属推送日志 — 需新建 API

> ⚠ 需新建 API 文件：`src/api/pushLog.ts`

| # | 方法 | 路径 | 说明 | 请求参数 | 响应数据 |
|---|------|------|------|---------|---------|
| G5 | GET | `/api/push-log/list` | 推送日志列表(分页) | `page, size, elderName?, channel?, status?, startDate?, endDate?` | `PageVO<PushLogVO>` |
| G6 | POST | `/api/push-log/retry/{id}` | 重试推送 | `id` (路径) | `void` |
| G7 | GET | `/api/push-log/stats` | 推送统计 | `startDate?, endDate?` | `PushLogStatsVO` |

**PushLogVO：**
```json
{
  "id": 1,
  "elderName": "张桂花",
  "familyPhone": "138****1234",
  "channel": "APP推送",
  "scene": "跌倒告警",
  "content": "张奶奶于14:30在卫生间发生跌倒，请立即确认",
  "fullContent": "【朝夕护行】尊敬的张奶奶家属：...",
  "status": "success",
  "time": "2024-07-15 14:30:25",
  "duration": "0.8秒",
  "retryCount": 0,
  "response": "{\"code\":200,\"msg\":\"推送成功\",\"data\":{\"pushId\":\"PUSH20240715001\"}}",
  "retryHistory": []
}
```

**PushLogStatsVO：**
```json
{
  "total": 15,
  "success": 11,
  "failed": 4,
  "successRate": 73.3
}
```

#### G-3：社区处理台账 — 需新建 API

> ⚠ 需新建 API 文件：`src/api/communityLedger.ts`

| # | 方法 | 路径 | 说明 | 请求参数 | 响应数据 |
|---|------|------|------|---------|---------|
| G8 | GET | `/api/community-ledger/list` | 台账列表(分页) | `page, size, elderName?, handler?, result?, reviewStatus?, startDate?, endDate?` | `PageVO<CommunityLedgerVO>` |
| G9 | POST | `/api/community-ledger` | 新增台账记录 | `CommunityLedgerCreateDTO` (Body) | `number` |
| G10 | PUT | `/api/community-ledger/{id}` | 编辑台账 | `id` (路径), `CommunityLedgerCreateDTO` (Body) | `void` |
| G11 | PUT | `/api/community-ledger/{id}/review` | 审核台账 | `id` (路径), `{ result, comment? }` (Body) | `void` |
| G12 | DELETE | `/api/community-ledger/{id}` | 删除台账 | `id` (路径) | `void` |
| G13 | GET | `/api/community-ledger/stats` | 台账统计 | — | `CommunityLedgerStatsVO` |

**CommunityLedgerVO：**
```json
{
  "id": 1,
  "code": "CH20240715001",
  "elderName": "张桂花",
  "alertType": "跌倒检测",
  "action": "上门查看",
  "handler": "王工",
  "handlerId": "001",
  "result": "已解决",
  "reviewStatus": "pending",
  "time": "14:30",
  "fullTime": "2024-07-15 14:30:25",
  "description": "接到告警后15分钟内到达现场，确认老人无大碍",
  "attachments": ["现场照片1.jpg", "处理报告.pdf"],
  "community": "朝阳社区",
  "alertCode": "A20240715001",
  "reviewer": "",
  "reviewTime": "",
  "rejectReason": ""
}
```

---

### 模块 H：居家空间档案 (Space) — 需新建 API

> ⚠ 需新建 API 文件：`src/api/space.ts`

| # | 方法 | 路径 | 说明 | 请求参数 | 响应数据 |
|---|------|------|------|---------|---------|
| H1 | GET | `/api/space/list` | 空间列表(分页) | `page, size, building?, floor?, occupancyStatus?, safetyRating?, elderName?` | `PageVO<SpaceVO>` |
| H2 | GET | `/api/space/{id}` | 空间详情 | `id` (路径) | `SpaceVO` |
| H3 | POST | `/api/space` | 新增空间 | `SpaceCreateDTO` (Body) | `number` |
| H4 | PUT | `/api/space/{id}` | 编辑空间 | `id` (路径), `SpaceCreateDTO` (Body) | `void` |
| H5 | DELETE | `/api/space/{id}` | 删除空间 | `id` (路径) | `void` |
| H6 | POST | `/api/space/{id}/inspection` | 新增检查记录 | `id` (路径), `InspectionCreateDTO` (Body) | `void` |
| H7 | GET | `/api/space/stats` | 空间统计 | — | `SpaceStatsVO` |

**SpaceVO：** 见 1.2 #13 中字段定义

**SpaceStatsVO：**
```json
{
  "total": 20,
  "occupied": 13,
  "vacant": 7,
  "aRated": 8,
  "dRated": 3,
  "risks": 8
}
```

> **⚠ 注意：** `safetyRating` 应由后端根据设施配置、风险因素、检查结果自动计算，前端不再使用 `calculateSafetyRating()` 函数。

---

### 模块 I：干预方案 (Intervention) — 需新建 API

> ⚠ 需新建 API 文件：`src/api/intervention.ts`

| # | 方法 | 路径 | 说明 | 请求参数 | 响应数据 |
|---|------|------|------|---------|---------|
| I1 | GET | `/api/intervention/list` | 方案列表(分页) | `page, size, name?, type?, status?, priority?, elderName?` | `PageVO<InterventionVO>` |
| I2 | GET | `/api/intervention/{id}` | 方案详情 | `id` (路径) | `InterventionVO` |
| I3 | POST | `/api/intervention` | 新增方案 | `InterventionCreateDTO` (Body) | `number` |
| I4 | PUT | `/api/intervention/{id}` | 编辑方案 | `id` (路径), `InterventionCreateDTO` (Body) | `void` |
| I5 | DELETE | `/api/intervention/{id}` | 删除方案 | `id` (路径) | `void` |
| I6 | PUT | `/api/intervention/{id}/review` | 审核方案 | `id` (路径), `{ result, comment? }` (Body) | `void` |
| I7 | POST | `/api/intervention/{id}/record` | 添加执行记录 | `id` (路径), `ExecutionRecordDTO` (Body) | `void` |
| I8 | GET | `/api/intervention/stats` | 方案统计 | — | `InterventionStatsVO` |

**InterventionVO：** 见 1.2 #14 中字段定义

**InterventionStatsVO：**
```json
{
  "total": 7,
  "ongoing": 2,
  "completed": 2,
  "pendingReview": 1,
  "avgRating": 4.5
}
```

---

### 模块 J：日常康养日程 (Schedule) — 需新建 API

> ⚠ 需新建 API 文件：`src/api/schedule.ts`

| # | 方法 | 路径 | 说明 | 请求参数 | 响应数据 |
|---|------|------|------|---------|---------|
| J1 | GET | `/api/schedule/list` | 日程列表(分页) | `page, size, taskType?, status?, elderName?, date?` | `PageVO<ScheduleTaskVO>` |
| J2 | POST | `/api/schedule` | 新增任务 | `ScheduleTaskCreateDTO` (Body) | `number` |
| J3 | PUT | `/api/schedule/{id}` | 编辑任务 | `id` (路径), `ScheduleTaskCreateDTO` (Body) | `void` |
| J4 | PUT | `/api/schedule/{id}/complete` | 完成任务 | `id` (路径), `{ result, note }` (Body) | `void` |
| J5 | DELETE | `/api/schedule/{id}` | 删除任务 | `id` (路径) | `void` |
| J6 | GET | `/api/schedule/stats` | 日程统计 | — | `ScheduleStatsVO` |

**ScheduleTaskVO：** 见 1.2 #15 中字段定义

**ScheduleStatsVO：**
```json
{
  "total": 12,
  "urgent": 3,
  "pending": 6,
  "completed": 6
}
```

---

### 模块 K：系统管理 (System) — 需新建 API

#### K-1：操作日志

> ⚠ 需新建 API 文件：`src/api/operationLog.ts`

| # | 方法 | 路径 | 说明 | 请求参数 | 响应数据 |
|---|------|------|------|---------|---------|
| K1 | GET | `/api/operation-log/list` | 日志列表(分页) | `page, size, user?, type?, module?, status?, startDate?, endDate?` | `PageVO<OperationLogVO>` |
| K2 | GET | `/api/operation-log/stats` | 日志统计 | — | `OperationLogStatsVO` |

**OperationLogVO：** 见 1.2 #16 中字段定义

**OperationLogStatsVO：**
```json
{
  "total": 120,
  "today": 15,
  "success": 110,
  "failed": 10
}
```

#### K-2：数据备份

> ⚠ 需新建 API 文件：`src/api/backup.ts`

| # | 方法 | 路径 | 说明 | 请求参数 | 响应数据 |
|---|------|------|------|---------|---------|
| K3 | GET | `/api/backup/list` | 备份列表(分页) | `page, size, type?, status?, startDate?, endDate?` | `PageVO<BackupVO>` |
| K4 | POST | `/api/backup/create` | 手动创建备份 | `{ type }` (Body) | `string` (任务ID) |
| K5 | POST | `/api/backup/{id}/restore` | 恢复备份 | `id` (路径) | `void` |
| K6 | DELETE | `/api/backup/{id}` | 删除备份文件 | `id` (路径) | `void` |
| K7 | GET | `/api/backup/config` | 获取备份配置 | — | `BackupConfigVO` |
| K8 | PUT | `/api/backup/config` | 更新备份配置 | `BackupConfigDTO` (Body) | `void` |
| K9 | GET | `/api/backup/stats` | 备份统计 | — | `BackupStatsVO` |

**BackupVO：** 见 1.2 #17 中字段定义

---

### 模块 L：仪表盘统计 (Dashboard Stats) — 已有 API，需改造

> 已有 API 文件：`src/api/stats.ts`

已有接口覆盖了大部分仪表盘需求，但 `dashboard.ts` 中的 `defaultCards` 的 `previewValue` 需要新增一个接口来获取。

**新增接口：**

| # | 方法 | 路径 | 说明 | 请求参数 | 响应数据 |
|---|------|------|------|---------|---------|
| L1 | GET | `/api/dashboard/cards` | 获取仪表盘卡片数据 | — | `DashboardCardsVO` |

**DashboardCardsVO：**
```json
{
  "totalPlans": 45,
  "inProgress": 12,
  "completed": 18,
  "pendingReview": 5,
  "avgRating": 4.2,
  "totalUsers": 128,
  "todayAlerts": 3,
  "onlineDevices": 32
}
```

---

### 模块 M：监控页图表 (Monitor Charts) — API 已定义，需使用

> 已有 API 文件：`src/api/stats.ts` 中的 `getMonitorHeartbeat()`, `getMonitorDeviceStatus()`, `getMonitorAcceleration()`

这三个接口已有定义，但 `Echart-monitor.vue` 没有调用它们，而是使用硬编码数据和 `generateHeartData()`。

---

## 三、代码改造步骤（操作清单）

### 阶段 0：准备工作

#### 0.1 新建 API 文件

需要新建以下文件：

| 文件 | 对应模块 |
|------|---------|
| `src/api/gait.ts` | 步态历史 |
| `src/api/healthNote.ts` | 健康备注 |
| `src/api/expert.ts` | 专家管理 + 咨询记录 |
| `src/api/caregiver.ts` | 陪护人员 |
| `src/api/pushLog.ts` | 家属推送日志 |
| `src/api/communityLedger.ts` | 社区处理台账 |
| `src/api/space.ts` | 居家空间档案 |
| `src/api/intervention.ts` | 干预方案管理 |
| `src/api/schedule.ts` | 日常康养日程 |
| `src/api/operationLog.ts` | 操作日志 |
| `src/api/backup.ts` | 数据备份 |

#### 0.2 补充已有 API 文件

| 文件 | 补充内容 |
|------|---------|
| `src/api/fall.ts` | 补充 `addFall`, `updateFall`, `deleteFall` 接口函数 |
| `src/api/elder.ts` | 无需修改（已完整） |
| `src/api/device.ts` | 无需修改（已完整） |
| `src/api/alert.ts` | 字段名统一（见下方说明） |
| `src/api/stats.ts` | 补充 `getDashboardCards` 接口函数 |
| `src/api/auth.ts` | 无需修改（已完整） |
| `src/api/user.ts` | 无需修改（已完整） |
| `src/api/notification.ts` | 无需修改（已完整） |

---

### 阶段 1：P0 优先级改造

#### Step 1.1 — 移除 elderData.ts 中的静态 Mock 数据

**文件：** `src/stores/elderData.ts`

**改前：**
```typescript
// 第 31-81 行：完全移除 generateCode() 和 fallHistoryMap
function generateCode(): string { ... }   // ❌ 删除
export const fallHistoryMap: Record<string, FallRecord[]> = { ... }  // ❌ 删除

// 第 196-209 行：移除静态 elderData 数组
export const elderData: ElderVO[] = [ ... ]  // ❌ 删除

// 第 212-224 行：移除兼容旧版的静态函数
export const getElderNames = (): string[] => elderData.map(...)  // ❌ 删除
export const findElderByNameStatic = (...) => ...  // ❌ 删除
export const findElderByCodeStatic = (...) => ...  // ❌ 删除
export const getFallHistory = (...) => ...  // ❌ 删除
```

**改后：**
```typescript
// 只保留 Pinia Store，所有数据通过 API 获取
export const useElderStore = defineStore('elder', () => {
  // ... 保持原有 store 逻辑不变
  // 删除 fetchElderList 中同步到 elderData 数组的代码：
  // elderData.length = 0
  // elderData.push(...(data.list ?? []))
})
```

#### Step 1.2 — 改造 Echart-monitor.vue

**文件：** `src/views/Echart-monitor.vue`

**改前 (line 457-458)：**
```typescript
import { elderData as _elderStatic, fallHistoryMap } from '@/stores/elderData'
// ...
const elderData = computed(() => {
  return elderStore.elderList.length > 0 ? elderStore.elderList : _elderStatic
})
```

**改后：**
```typescript
import { useElderStore } from '@/stores/elderData'
// 移除 _elderStatic 和 fallHistoryMap 导入

const elderData = computed(() => elderStore.elderList)
```

**改前 (line 476-479) — 跌倒历史：**
```typescript
const fallHistoryData = computed(() => {
  if (!currentElder.value) return []
  return fallHistoryMap[currentElder.value.name] || []
})
```

**改后：**
```typescript
const fallHistoryData = computed(() => {
  if (!currentElder.value) return []
  return elderStore.getFallHistory(currentElder.value.name)
})
```

**改前 (line 533-543) — 设备状态饼图硬编码数据：**
```typescript
data: [
  { value: 4, name: '在线' },
  { value: 1, name: '离线' },
  { value: 1, name: '低电量' },
  { value: 1, name: '故障' },
  { value: 1, name: '待激活' },
],
```

**改后：**
```typescript
import { getMonitorDeviceStatus } from '@/api/stats'
// 在 initCharts 中：
async function initCharts() {
  try {
    const res = await getMonitorDeviceStatus()
    const pieData = res.data.data || []
    chartMonitoring.setOption({
      series: [{
        data: pieData.map(item => ({ value: item.value, name: item.name })),
        // ... 其余配置保持不变
      }]
    })
  } catch {
    // 降级：显示空数据
    chartMonitoring.setOption({
      series: [{ data: [] }]
    })
  }
}
```

**改前 (line 546-574) — 加速度曲线硬编码数据：**
```typescript
// 硬编码 data: [-0.1, -0.2, 0, 0.2, 0.2, 0.1, 0.4]
```

**改后：**
```typescript
import { getMonitorAcceleration } from '@/api/stats'

async function initAccelerationChart(elderId: number) {
  try {
    const res = await getMonitorAcceleration(elderId)
    const data = res.data.data
    chartAcceleration.setOption({
      xAxis: { data: data.categories },
      series: [{ data: data.series[0]?.data || [] }]
    })
  } catch { /* 降级 */ }
}
```

**改前 (line 576-632) — 心跳曲线随机生成：**
```typescript
// generateHeartData(50) + setInterval 随机更新
```

**改后：**
```typescript
import { getMonitorHeartbeat } from '@/api/stats'

async function initHeartChart(elderId: number) {
  try {
    const res = await getMonitorHeartbeat(elderId)
    const data = res.data.data
    chartHeart.setOption({
      xAxis: { data: data.categories },
      series: [{ data: data.series[0]?.data || [] }]
    })
  } catch { /* 降级 */ }
}

// 轮询更新（每 30 秒拉一次新数据，替代随机生成）
let heartPollTimer: ReturnType<typeof setInterval> | null = null
function startHeartPolling(elderId: number) {
  heartPollTimer = setInterval(async () => {
    try {
      const res = await getMonitorHeartbeat(elderId)
      const data = res.data.data
      chartHeart?.setOption({ series: [{ data: data.series[0]?.data || [] }] })
    } catch { /* 忽略 */ }
  }, 30000)
}
```

#### Step 1.3 — 改造 3-2gait.vue（步态历史）

**文件：** `src/views/3elder/3-2gait.vue`

**改前：**
```typescript
import request from '@/utils/request'

const mockData = [ ... ]  // ❌ 删除全部 mock 数据

function initData() {
  gaitList.value = mockData.map((item, index) => ({ ...item, id: index + 1 }))
  updateStats()
  nextTick(() => setTimeout(initChart, 300))
}
```

**改后：**
```typescript
import { getGaitList, addGaitRecord, deleteGaitRecord, getGaitStats, type GaitVO, type GaitCreateDTO } from '@/api/gait'

const gaitList = ref<GaitVO[]>([])
const total = ref(0)
const loading = ref(false)

async function fetchGaitList(params?: { page?: number; size?: number; name?: string; result?: string }) {
  loading.value = true
  try {
    const res = await getGaitList(params)
    gaitList.value = res.data.data?.list ?? []
    total.value = res.data.data?.total ?? 0
    await updateStats()
    await nextTick(() => setTimeout(initChart, 300))
  } catch {
    ElMessage.error('加载步态数据失败')
  } finally {
    loading.value = false
  }
}

async function updateStats() {
  try {
    const res = await getGaitStats()
    const data = res.data.data
    if (data) {
      Object.assign(stats, data)
    }
  } catch { /* 静默失败 */ }
}
```

**提交表单改前：**
```typescript
const score = Math.floor(Math.random() * 30 + 50)  // ❌ 随机评分
```

**改后：**
```typescript
async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    await addGaitRecord({
      name: formData.name,
      date: formData.date,
      speed: parseFloat(formData.speed),
      frequency: parseInt(formData.frequency),
      stepLength: parseInt(formData.stepLength),
      result: formData.result,
    })
    ElMessage.success('添加成功')
    dialogVisible.value = false
    await fetchGaitList()
  } catch {
    ElMessage.error('添加失败')
  }
}
```

#### Step 1.4 — 改造 3-3fall-record.vue（跌倒记录）

**文件：** `src/views/3elder/3-3fall-record.vue`

类似 3-2gait.vue 的改造模式，替换为 `src/api/fall.ts` 的接口调用。

#### Step 1.5 — 改造 3-4health-note.vue（健康备注）

**文件：** `src/views/3elder/3-4health-note.vue`

替换为新建的 `src/api/healthNote.ts` 接口调用。

#### Step 1.6 — 10-1list.vue 实时告警页面改造

**文件：** `src/views/10alert/10-1list.vue`

此页面需要统一字段名，将 `mockData` 中的 `elderName → elderlyName`, `alertType → warningType`, `level → priority`，然后使用 `src/api/alert.ts` 中的接口。

#### Step 1.7 — 改造 dashboard.ts

**文件：** `src/stores/dashboard.ts`

将 `defaultCards` 中的 `previewValue` 改为从 API 获取。

```typescript
// 改后：新增方法
import { getDashboardCards } from '@/api/stats'

async function refreshAllCardValues(): Promise<void> {
  try {
    const res = await getDashboardCards()
    const data = res.data.data
    if (!data) return
    
    const valueMap: Record<string, number> = {
      '1': data.totalPlans,
      '2': data.inProgress,
      '3': data.completed,
      '4': data.pendingReview,
      '5': data.avgRating,
      '6': data.totalUsers,
      '7': data.todayAlerts,
      '8': data.onlineDevices,
    }
    cards.value.forEach(card => {
      if (valueMap[card.id] !== undefined) {
        card.value = valueMap[card.id]
      }
    })
  } catch { /* 保持当前值 */ }
}
```

---

### 阶段 2：P1 优先级改造

#### Step 2.1 — 改造 7expert.vue（专家管理）

- 替换 `mockExperts` → `getExpertList()`
- 替换 `mockConsults` → `getConsultationList()`
- 增删改操作对接 API

#### Step 2.2 — 改造 8caregiver.vue（陪护人员）

- 替换 `mockData` → `getCaregiverList()`
- 增删改操作对接 API

#### Step 2.3 — 改造 10-2push-log.vue（推送日志）

- 替换 `mockData` → `getPushLogList()`
- 统计卡片替换为 `getPushLogStats()`

#### Step 2.4 — 改造 10-3community.vue（社区台账）

- 替换 `mockData` → `getCommunityLedgerList()`
- 增删改审核操作对接 API

#### Step 2.5 — 改造 11space.vue（空间档案）

- 替换 `mockData` → `getSpaceList()`
- 安全评级由后端计算
- 检查记录增删对接 API

#### Step 2.6 — 改造 12intervention.vue（干预方案）

- 替换 `mockData` → `getInterventionList()`
- 审批流程对接 API
- 执行记录对接 API

#### Step 2.7 — 改造 13schedule.vue（日常日程）

- 替换 `mockTasks` → `getScheduleList()`
- 任务完成对接 API

---

### 阶段 3：P2 优先级改造

#### Step 3.1 — 改造 14-2log.vue（操作日志）

- 替换 `mockData` → `getOperationLogList()`

#### Step 3.2 — 改造 14-3backup.vue（数据备份）

- 替换 `mockData` → `getBackupList()`
- 备份/恢复操作对接 API

#### Step 3.3 — 改造 10-4preference.vue（通知偏好）

- 表单默认值从 API 获取

---

### 通用模式：数据加载状态处理

每个改造后的页面都应遵循统一的数据加载模式：

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const dataList = ref([])
const loading = ref(false)
const error = ref<string | null>(null)

async function fetchData() {
  loading.value = true
  error.value = null
  try {
    const res = await someApiCall(params)
    dataList.value = res.data.data?.list ?? []
  } catch (err) {
    error.value = '数据加载失败'
    ElMessage.error('数据加载失败，请稍后重试')
    // 降级：显示空状态，不阻断用户操作
    dataList.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div v-loading="loading">
    <!-- 错误状态 -->
    <el-alert v-if="error" type="error" :title="error" show-icon closable @close="error = null" />
    
    <!-- 空状态 -->
    <el-empty v-if="!loading && dataList.length === 0" description="暂无数据" />
    
    <!-- 正常内容 -->
    <el-table v-else :data="dataList">...</el-table>
  </div>
</template>
```

---

## 四、后端实现优先级建议

| 批次 | 模块 | API 数量 | 依赖关系 |
|------|------|---------|---------|
| **第一批** | 老人管理 + 跌倒记录 | 5 (已有API) + 3 (补充) | 无外部依赖，是所有页面基础 |
| **第二批** | 告警列表 + 设备统计 | 4 (已有API) + 1 (新增卡片) | 依赖老人数据 |
| **第三批** | 步态 + 健康备注 + 专家 + 陪护 | 5+5+10+5 = 25 | 依赖老人数据 |
| **第四批** | 推送日志 + 社区台账 + 空间 + 干预 + 日程 | 7+6+8+8+6 = 35 | 依赖老人和告警数据 |
| **第五批** | 系统日志 + 备份 + 监控图表 | 9 (含图表) | 独立模块 |

---

## 五、附录：兼容旧版导入处理

### A. `loginNew.vue` 兼容路由

`src/api/login.ts` 当前是兼容层：
```typescript
export { login } from './auth'
export { getUserProfile as getUserInfo } from './user'
```

`loginNew.vue` 中有两处命名冲突需要处理：
- `loginNew.vue` 内部定义了 `function register()` (line 147) 和 `function Login()` (line 155)
- 同时又从 `@/api/auth` 导入了 `register` 和 `sendRegisterCode` 等函数

建议将 `loginNew.vue` 中的 `function register()` 重命名为 `function goToRegister()` 或移到路由跳转中处理，避免与导入的 API 函数重名。

### B. 数据加载顺序

在 `1management.vue` 等页面加载时，应遵循先基础后图表的原则：
```typescript
onMounted(async () => {
  // 1. 先加载基础数据
  await Promise.all([
    userStore.fetchUserInfo(),
    statsStore.fetchManagementStats(),
    notificationStore.fetchNotifications({ pageNum: 1, pageSize: 10 }),
  ])
  // 2. 再加载图表数据（可能依赖基础数据）
  await statsStore.fetchDeviceTrends()
  // 3. 初始化图表
  await nextTick(() => initCharts())
})
```

### C. 降级方案

当后端 API 不可用时，各组件应：
1. `loading` 为 false 时显示空状态（`el-empty`）而非崩溃
2. 表单提交失败时给出明确的错误提示
3. 图表数据为空时显示空图表（而非 mock 数据或报错）
4. 列表分页功能正常（total=0 时显示空列表）

---

*本文档由代码自动化扫描生成，用于前后端协作。如有疑问请联系前端开发团队。*
