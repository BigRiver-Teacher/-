# 架构说明（ARCHITECTURE）

## 总体结构

```
浏览器 (localhost:5173)
   │  /api  (Vite dev proxy)
   ▼
Spring Boot 后端 (localhost:8080)  ──►  MySQL 8 (localhost:3306 / elderly_care)

FastAPI 跌倒检测服务 (localhost:8001)  ──►  模型工程（YOLO11n-pose + LSTM）
   ▲
   │  HTTP（独立部署，按需调用；当前由接口文档/脚本直接调用）
```

> 后端与算法服务当前为**并列独立**关系，无相互 HTTP 调用；两者可由同一前端或外部调用方分别接入。

## 三端职责

| 模块 | 技术栈 | 端口 | 职责 |
| --- | --- | --- | --- |
| frontend | Vue3 + TS + Vite + Element Plus + ECharts + Pinia | 5173（dev） | 管理端界面、大屏图表、业务交互 |
| backend | Spring Boot 3.3.4 + MyBatis-Plus + MySQL | 8080 | REST 接口、JWT 鉴权、业务逻辑、数据访问 |
| algorithm | FastAPI + PyTorch + Ultralytics | 8001 | 视频跌倒检测，输出事件 JSON 与标注视频 |

## 数据流

1. 前端页面通过 `/api` 调用后端；Vite 将请求代理到 8080（后端无 `/api` 前缀，代理不 rewrite）。
2. 后端读写 MySQL `elderly_care`（25 张表，演示数据由 seed_data.sql 导入）。
3. 算法服务独立提供 HTTP 接口（8001），接收视频并输出事件 JSON 与标注视频；
   当前前端与后端均未直接调用该服务，属可独立验证的推理微服务，事件回写告警为后续扩展能力
   （见 algorithm/README.md 第六节）。

## 关键约定

- 后端统一响应 `{code, message, data, timestamp}`，业务成功 code=200；
- 分页返回 `{total, list}`；
- 前端 axios baseURL=`/api`，拦截器统一提示 401/403/404/500；
- 后端接口文档：Knife4j `/doc.html`，OpenAPI `/v3/api-docs`。

## 运行顺序

数据库（初始化）→ 后端（8080）→ 前端（5173）→ 算法（8001，按需）。
详见根目录 README.md 的“快速开始”。
