# -*- coding: utf-8 -*-
"""摔倒检测服务 —— FastAPI 主应用入口。

启动方式（在 conda 虚拟环境中）：
    uvicorn main:app --host 0.0.0.0 --port 8001 --reload

功能：
- 启动时预加载摔倒检测模型（YOLO 姿态估计 + LSTM 分类）；
- 提供同步 / 异步视频摔倒检测接口；
- 模型文件缺失时给出明确报错，服务仍可启动并返回模型状态。
"""

from __future__ import annotations

import logging
import os
from contextlib import asynccontextmanager
from pathlib import Path
from typing import AsyncIterator

from datetime import datetime

from dotenv import load_dotenv
from fastapi import FastAPI, HTTPException, Request
from fastapi.exceptions import RequestValidationError
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from starlette.middleware.base import BaseHTTPMiddleware

from fall_detection_routes import router as fall_detection_router
from fall_detection_service import FallDetectionService

# 加载 .env 环境变量（若存在）
load_dotenv()

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s [%(name)s] %(message)s",
)
logger = logging.getLogger("fall-detection-app")


def build_service() -> FallDetectionService:
    """根据环境变量构造摔倒检测服务实例。"""
    model_dir = os.getenv(
        "FALL_MODEL_DIR",
        "models",
    )
    # 默认检查点为 v2（八特征二分类）；v2 缺失时自动回退 v1 best.pth，保持开箱可用
    lstm_default = "models/checkpoints/v2_hard_samples_20260903/last.pth"
    lstm_ckpt = os.getenv("FALL_LSTM_CHECKPOINT") or lstm_default
    if lstm_ckpt == lstm_default:
        v1_path = Path(model_dir) / "models/checkpoints/best.pth"
        if not (Path(model_dir) / lstm_default).exists() and v1_path.exists():
            logger.warning("v2 检查点缺失，已回退使用 v1 检查点 %s", v1_path)
            lstm_ckpt = "models/checkpoints/best.pth"
    return FallDetectionService(
        model_dir=model_dir,
        yolo_model=os.getenv("FALL_YOLO_MODEL", "yolo11n-pose-fallen.pt"),
        lstm_checkpoint=lstm_ckpt,
        device=os.getenv("FALL_DEVICE") or None,
        confidence_threshold=float(os.getenv("FALL_CONFIDENCE_THRESHOLD", "0.40")),
        cooldown_frames=int(os.getenv("FALL_COOLDOWN_FRAMES", "90")),
        tracker_config=os.getenv("FALL_TRACKER_CONFIG", "configs/botsort.yaml"),
        upload_dir=os.getenv("FALL_UPLOAD_DIR", "uploads"),
        output_dir=os.getenv("FALL_OUTPUT_DIR", "outputs"),
        max_workers=int(os.getenv("FALL_MAX_WORKERS", "1")),
    )


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncIterator[None]:
    """应用生命周期：启动时预加载模型，退出时释放资源。"""
    service = build_service()
    # 将单例注入模块级访问入口，保证路由使用同一实例
    import fall_detection_service as svc

    svc._service = service

    try:
        service.load()
        logger.info("模型预加载成功: %s", service.model_status())
    except Exception as exc:  # noqa: BLE001 —— 模型缺失时服务仍可启动，接口会返回明确错误
        logger.error("模型预加载失败: %s", exc)
    yield
    service.shutdown()
    logger.info("摔倒检测服务已退出")


def _result(code: int, message: str, data=None) -> dict:
    """与后端一致的统一响应结构。"""
    return {
        "code": code,
        "message": message,
        "data": data,
        "timestamp": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
    }


# 阶段3：算法接口鉴权中间件。
# FALL_API_KEY 未配置时不启用鉴权（保持本地演示零配置可用）；配置后除 /health 与文档外均需 X-API-Key。
class ApiKeyAuthMiddleware(BaseHTTPMiddleware):
    PUBLIC_PATHS = ("/", "/health", "/api/fall-detection/health", "/docs", "/redoc", "/openapi.json")

    async def dispatch(self, request: Request, call_next):
        api_key = os.getenv("FALL_API_KEY", "").strip()
        path = request.url.path
        if api_key and path not in self.PUBLIC_PATHS and not path.startswith("/docs"):
            supplied = request.headers.get("X-API-Key", "")
            if supplied != api_key:
                return JSONResponse(
                    status_code=401,
                    content=_result(401, "无效的 API Key"),
                )
        return await call_next(request)


# 创建应用
app = FastAPI(
    title="朝夕护行 · 摔倒检测服务",
    description="基于 YOLO 姿态估计 + LSTM 的老年人摔倒检测服务，"
    "支持视频上传同步/异步检测、事件日志输出与标注视频下载。",
    version="1.0.0",
    lifespan=lifespan,
)

# 阶段3：接口鉴权。先于 CORS 添加，使其位于中间件栈内层
# （CORS 后添加成为最外层，保证 401 响应同样带 CORS 头）。
app.add_middleware(ApiKeyAuthMiddleware)

# CORS：允许前端（Vue dev server 及生产域名）跨域调用
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 生产环境请改为具体域名白名单
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 挂载摔倒检测路由
app.include_router(fall_detection_router)


@app.exception_handler(HTTPException)
async def http_exception_handler(request: Request, exc: HTTPException):
    """把 FastAPI 默认 {"detail": ...} 改为统一响应体，且对 5xx 隐藏内部细节。"""
    status = exc.status_code
    if status >= 500:
        message = str(exc.detail)
        # 阶段2：不回显服务器绝对路径、堆栈等内部信息
        for marker in ("D:", "E:", "/home/", "/Users/", "Traceback"):
            if marker in message:
                message = "服务内部错误，请联系管理员"
                break
    else:
        message = str(exc.detail)
    return JSONResponse(status_code=status, content=_result(status, message))


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    """参数错误统一 HTTP 400，避免默认 422 且与后端约定一致。"""
    errors = exc.errors()
    names = [".".join(str(p) for p in e.get("loc", [])[1:]) or "参数" for e in errors]
    return JSONResponse(
        status_code=400,
        content=_result(400, "参数格式不正确: " + ", ".join(sorted(set(names))[:5])),
    )


@app.exception_handler(Exception)
async def unhandled_exception_handler(request: Request, exc: Exception):
    """兜底异常：仅记录日志，响应不暴露内部实现。"""
    logger.exception("未处理异常: %s", exc)
    return JSONResponse(status_code=500, content=_result(500, "服务内部错误，请联系管理员"))


@app.get("/", summary="服务信息")
async def root() -> dict:
    """服务根路径：返回服务基本信息。"""
    return {
        "service": "朝夕护行 · 摔倒检测服务",
        "version": "1.0.0",
        "docs": "/docs",
        "health": "/api/fall-detection/health",
    }


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(
        "main:app",
        host=os.getenv("FALL_HOST", "0.0.0.0"),
        port=int(os.getenv("FALL_PORT", "8001")),
        reload=os.getenv("FALL_RELOAD", "false").lower() == "true",
    )