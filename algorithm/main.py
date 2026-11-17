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

from dotenv import load_dotenv
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

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
    return FallDetectionService(
        model_dir=model_dir,
        yolo_model=os.getenv("FALL_YOLO_MODEL", "yolo11n-pose-fallen.pt"),
        lstm_checkpoint=os.getenv("FALL_LSTM_CHECKPOINT", "models/checkpoints/best.pth"),
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


# 创建应用
app = FastAPI(
    title="朝夕护行 · 摔倒检测服务",
    description="基于 YOLO 姿态估计 + LSTM 的老年人摔倒检测服务，"
    "支持视频上传同步/异步检测、事件日志输出与标注视频下载。",
    version="1.0.0",
    lifespan=lifespan,
)

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