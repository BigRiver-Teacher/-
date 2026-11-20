# -*- coding: utf-8 -*-
"""摔倒检测 FastAPI 路由 —— 提供健康检查、同步检测、异步检测、任务查询等接口。"""

from __future__ import annotations

import logging
import os
import tempfile
import time
import uuid
from pathlib import Path
from typing import Any, Dict, Optional

from fastapi import APIRouter, File, Form, HTTPException, UploadFile
from fastapi.responses import FileResponse

from fall_detection_service import FallModelNotFoundError, get_service

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/fall-detection", tags=["摔倒检测"])

# 允许上传的视频扩展名
ALLOWED_EXTENSIONS = {".mp4", ".avi", ".mov", ".mkv", ".webm", ".flv"}

# 阶段3：上传限制。默认 500MB，可用 FALL_MAX_UPLOAD_MB 覆盖。
def _max_upload_bytes() -> int:
    try:
        mb = int(os.getenv("FALL_MAX_UPLOAD_MB", "500"))
    except ValueError:
        mb = 500
    return max(1, mb) * 1024 * 1024

# 常见视频文件头（用于识别伪造扩展名）
_VIDEO_MAGIC = (
    b"ftyp",            # MP4 / MOV
    b"RIFF",            # AVI / WebM(EBML 另列)
    b"\x1aE\xdf\xa3",   # Matroska / WebM
    b"FLV\x01",         # FLV
)

# 简单的响应包装：{code, message, data}
def _is_under(path: Path, root: Path) -> bool:
    """判断 path 是否位于 root 目录内（含自身）。"""
    try:
        path.relative_to(root)
        return True
    except ValueError:
        return False


def _validate_infer_params(
    confidence: Optional[float], cooldown: Optional[int]
) -> "tuple[float | None, int | None]":
    """阶段3：校验并转换本次推理覆盖参数。"""
    if confidence is not None and not (0.0 < confidence <= 1.0):
        raise HTTPException(status_code=400, detail="confidence 必须在 (0, 1] 范围内")
    if cooldown is not None and not (0 <= cooldown <= 100000):
        raise HTTPException(status_code=400, detail="cooldown 必须在 0~100000 范围内")
    return confidence, cooldown


async def _validate_upload(file: UploadFile) -> bytes:
    """阶段3：校验扩展名 + 文件头 + 大小，返回已读取的前 32 字节用于写入。"""
    _validate_extension(file.filename or "")
    header = await file.read(32)
    if not any(header.startswith(magic) or magic in header[:32] for magic in _VIDEO_MAGIC):
        raise HTTPException(status_code=400, detail="文件内容不是可识别的视频格式")

    # 读取完整内容以校验大小（同时避免重复读流）
    rest = await file.read()
    data = header + rest
    if len(data) > _max_upload_bytes():
        raise HTTPException(status_code=413, detail="上传文件超过大小限制")
    if len(data) == 0:
        raise HTTPException(status_code=400, detail="上传文件为空")
    return data


def _ok(data: Any = None, message: str = "操作成功") -> Dict[str, Any]:
    return {"code": 200, "message": message, "data": data}


def _fail(code: int, message: str) -> Dict[str, Any]:
    return {"code": code, "message": message, "data": None}


def _ensure_model() -> None:
    """确保模型已加载，模型缺失时抛出 500 错误。"""
    service = get_service()
    try:
        service.load()
    except FallModelNotFoundError as exc:
        raise HTTPException(status_code=500, detail="模型文件缺失，请联系管理员") from exc
    except Exception as exc:  # noqa: BLE001 —— 模型加载失败统一返回 500
        logger.exception("模型加载失败")
        raise HTTPException(status_code=500, detail="模型加载失败，请联系管理员") from exc


def _validate_extension(filename: str) -> None:
    """校验上传文件扩展名是否允许。"""
    ext = Path(filename).suffix.lower()
    if ext not in ALLOWED_EXTENSIONS:
        raise HTTPException(
            status_code=400,
            detail="不支持的文件类型: " + ext + "，仅支持 " + ", ".join(sorted(ALLOWED_EXTENSIONS)),
        )


@router.get("/health", summary="健康检查")
async def health() -> Dict[str, Any]:
    """返回服务与模型加载状态，用于探活。"""
    service = get_service()
    return _ok(service.model_status())


@router.post("/detect", summary="同步检测视频摔倒")
async def detect_sync(
    file: UploadFile = File(..., description="待检测的视频文件"),
    confidence: Optional[float] = Form(None, description="跌倒置信度阈值，覆盖默认值"),
    cooldown: Optional[int] = Form(None, description="告警冷却帧数，覆盖默认值"),
    with_output: bool = Form(False, description="是否返回标注后的视频文件"),
) -> Any:
    """同步接口：上传视频并等待检测完成，返回检测事件与摘要。

    说明：
    - 视频在服务端保存到临时目录后交给模型检测；
    - with_output=True 时额外返回标注视频（通过 /api/fall-detection/output/{file} 下载）。
    """
    data = await _validate_upload(file)
    conf, cooldown = _validate_infer_params(confidence, cooldown)
    _ensure_model()

    service = get_service()
    run_id = uuid.uuid4().hex
    input_path = service.upload_dir / (run_id + Path(file.filename or "video.mp4").suffix)

    try:
        # 保存已校验的上传内容
        input_path.write_bytes(data)
    finally:
        await file.close()

    output_path: Optional[Path] = None
    if with_output:
        output_path = service.output_dir / (run_id + "_annotated.mp4")

    try:
        result = service.process_video_sync(
            input_path, output_path,
            confidence_threshold=conf, cooldown_frames=cooldown,
        )
    except Exception as exc:  # noqa: BLE001
        logger.exception("同步检测失败")
        raise HTTPException(status_code=500, detail="视频检测失败，请检查文件是否可正常解码") from exc
    finally:
        # 清理上传的原始视频（保留标注视频供下载）
        if input_path.exists():
            input_path.unlink(missing_ok=True)

    data = dict(result)
    if output_path and output_path.exists():
        data["output_file"] = output_path.name
        data["output_url"] = "/api/fall-detection/output/" + output_path.name
    return _ok(data, "检测完成")


@router.post("/detect/async", summary="异步提交检测任务")
async def detect_async(
    file: UploadFile = File(..., description="待检测的视频文件"),
    confidence: Optional[float] = Form(None, description="跌倒置信度阈值"),
    cooldown: Optional[int] = Form(None, description="告警冷却帧数"),
    with_output: bool = Form(False, description="是否生成标注视频"),
) -> Dict[str, Any]:
    """异步接口：提交任务后立即返回 task_id，结果通过任务查询接口获取。"""
    data = await _validate_upload(file)
    conf, cooldown = _validate_infer_params(confidence, cooldown)
    _ensure_model()

    service = get_service()
    run_id = uuid.uuid4().hex
    input_path = service.upload_dir / (run_id + Path(file.filename or "video.mp4").suffix)

    try:
        input_path.write_bytes(data)
    finally:
        await file.close()

    output_path: Optional[Path] = None
    if with_output:
        output_path = service.output_dir / (run_id + "_annotated.mp4")

    task_id = service.process_video_async(
        input_path, output_path,
        confidence_threshold=conf, cooldown_frames=cooldown,
    )
    return _ok({"task_id": task_id, "status": "pending"}, "任务已提交")


@router.get("/detect/task/{task_id}", summary="查询异步任务状态")
async def get_task(task_id: str) -> Dict[str, Any]:
    """查询异步检测任务状态与结果。

    状态取值：pending / running / done / failed。
    """
    service = get_service()
    task = service.get_task_status(task_id)
    if task is None:
        raise HTTPException(status_code=404, detail="任务不存在")
    data = dict(task)
    if task.get("output") and Path(task["output"]).exists():
        data["output_url"] = "/api/fall-detection/output/" + Path(task["output"]).name
    return _ok(data)


@router.get("/output/{file_name}", summary="下载标注视频")
async def download_output(file_name: str) -> FileResponse:
    """下载检测生成的标注视频文件。"""
    service = get_service()
    # 防止路径穿越
    safe_name = Path(file_name).name
    file_path = service.output_dir / safe_name
    if not file_path.exists():
        raise HTTPException(status_code=404, detail="文件不存在")
    return FileResponse(file_path, media_type="video/mp4", filename=safe_name)


@router.get("/models/status", summary="模型状态")
async def models_status() -> Dict[str, Any]:
    """返回模型加载状态与配置信息。"""
    return _ok(get_service().model_status())


@router.post("/detect/file", summary="按路径检测视频（服务器本地文件）")
async def detect_by_path(
    input_path: str = Form(..., description="服务器上的视频绝对路径"),
    with_output: bool = Form(False, description="是否生成标注视频"),
) -> Dict[str, Any]:
    """按服务器本地路径直接检测视频（无需上传），便于与现有后端联动。"""
    path = Path(input_path)
    service = get_service()
    # 阶段3：仅允许服务自身 uploads/ 与 outputs/ 目录内的视频，防止任意路径探测
    allowed_roots = (service.upload_dir.resolve(), service.output_dir.resolve())
    try:
        resolved = path.resolve(strict=True)
        if not any(_is_under(resolved, root) for root in allowed_roots):
            raise HTTPException(status_code=400, detail="仅支持检测服务 uploads/ 与 outputs/ 目录内的视频")
    except FileNotFoundError:
        raise HTTPException(status_code=404, detail="文件不存在")
    _ensure_model()
    output_path: Optional[Path] = None
    if with_output:
        output_path = service.output_dir / (path.stem + "_annotated" + path.suffix)
    try:
        result = service.process_video_sync(path, output_path)
    except Exception as exc:  # noqa: BLE001
        logger.exception("按路径检测失败")
        raise HTTPException(status_code=500, detail="视频检测失败，请检查文件是否可正常解码") from exc
    data = dict(result)
    if output_path and output_path.exists():
        data["output_url"] = "/api/fall-detection/output/" + output_path.name
    return _ok(data, "检测完成")
