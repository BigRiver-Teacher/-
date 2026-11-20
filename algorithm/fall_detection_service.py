# -*- coding: utf-8 -*-
"""摔倒检测服务类 —— 封装 FallDetectionPipeline，提供单例、预加载、同步/异步检测能力。

设计要点：
1. 单例模式：整个进程共享一个 FallDetectionPipeline 实例，避免重复加载 YOLO + LSTM 模型；
2. 启动预加载：应用启动时调用 load()，模型加载失败会抛出 FallModelNotFoundError；
3. 同步/异步双接口：同步接口直接返回结果，异步接口返回 task_id 供轮询；
4. 模型文件校验：加载前检查 YOLO 权重与 LSTM 检查点是否存在；
5. 线程安全：YOLO 检测 + LSTM 推理为计算密集型，通过全局锁串行执行，避免并发冲突。
"""

from __future__ import annotations

import logging
import os
import sys
import threading
import uuid
from concurrent.futures import ThreadPoolExecutor
from datetime import datetime
from pathlib import Path
from typing import Any, Dict, Optional

logger = logging.getLogger(__name__)


class FallModelNotFoundError(FileNotFoundError):
    """模型文件不存在时抛出，用于向上层返回明确的错误信息。"""

    def __init__(self, path: str) -> None:
        self.missing_path = path
        super().__init__("模型文件不存在: " + path)


class FallDetectionService:
    """摔倒检测服务（单例）。

    封装 src.pipeline.FallDetectionPipeline，对外提供统一的检测接口。
    """

    _instance: Optional["FallDetectionService"] = None
    _instance_lock = threading.Lock()

    # 姿态模型权重（v1 原版 / v2 改进版：躺地-跌倒人体微调，COCO 17 关键点布局不变）
    YOLO_V1 = "yolo11n-pose.pt"
    YOLO_V2 = "yolo11n-pose-fallen.pt"

    def __init__(
        self,
        model_dir: str = "models",
        yolo_model: Optional[str] = None,
        lstm_checkpoint: str = "models/checkpoints/best.pth",
        device: Optional[str] = None,
        confidence_threshold: float = 0.40,
        cooldown_frames: int = 90,
        tracker_config: str = "configs/botsort.yaml",
        upload_dir: str = "uploads",
        output_dir: str = "outputs",
        max_workers: int = 1,
    ) -> None:
        """初始化服务配置（不加载模型，模型在 load() 时预加载）。

        Args:
            model_dir: 摔倒检测模型项目根目录。
            yolo_model: YOLO 姿态估计模型文件名（相对 model_dir）。
            lstm_checkpoint: LSTM 跌倒分类检查点路径（相对 model_dir）。
            device: 推理设备，cuda / cpu / None(自动选择)。
            confidence_threshold: 跌倒置信度阈值，默认 0.40。
            cooldown_frames: 两次告警之间的冷却帧数，默认 90。
            tracker_config: 跟踪器配置文件（相对 model_dir）。
            upload_dir: 上传视频保存目录。
            output_dir: 标注视频输出目录。
            max_workers: 异步任务线程数（检测为计算密集型，默认 1 串行）。
        """
        # 解析为绝对路径：load() 会临时切换工作目录，相对路径会被二次解析而定位失败
        self.model_dir = Path(model_dir).resolve()
        # 请求的 YOLO 权重：显式传入 > 环境变量 > 目录内 v2/v1 自动择优
        if not yolo_model:
            yolo_model = os.getenv("FALL_YOLO_MODEL") or self._default_yolo_name()
        self.yolo_model = yolo_model
        self.lstm_checkpoint = lstm_checkpoint
        # 实际生效的 YOLO 权重（可能因缺失自动回退到另一版本）
        self._active_yolo_name: Optional[str] = None
        self._active_yolo_path: Optional[Path] = None
        self.device = device
        self.confidence_threshold = confidence_threshold
        self.cooldown_frames = cooldown_frames
        self.tracker_config = tracker_config
        self.upload_dir = Path(upload_dir)
        self.output_dir = Path(output_dir)

        # 模型对象（延迟加载）
        self._pipeline: Any = None
        self._load_lock = threading.Lock()
        self._inference_lock = threading.Lock()

        # 异步任务执行器与状态表
        self._executor = ThreadPoolExecutor(max_workers=max_workers, thread_name_prefix="fall-detect")
        self._tasks: Dict[str, Dict[str, Any]] = {}

        # 目录准备
        self.upload_dir.mkdir(parents=True, exist_ok=True)
        self.output_dir.mkdir(parents=True, exist_ok=True)

    # ------------------------------------------------------------------
    # 单例
    # ------------------------------------------------------------------
    @classmethod
    def get_instance(cls, **kwargs: Any) -> "FallDetectionService":
        """获取全局单例实例（线程安全）。"""
        with cls._instance_lock:
            if cls._instance is None:
                cls._instance = cls(**kwargs)
        return cls._instance

    # ------------------------------------------------------------------
    # 模型加载
    # ------------------------------------------------------------------
    def _default_yolo_name(self) -> str:
        """目录内自动择优：优先改进版 v2 权重，缺失则用原版 v1。"""
        if (self.model_dir / self.YOLO_V2).exists():
            return self.YOLO_V2
        return self.YOLO_V1

    def _pick_yolo(self) -> Path:
        """按“请求权重优先、缺失自动回退”策略选择实际使用的 YOLO 权重。

        候选顺序：请求的权重 → 另一版本权重（v2/v1 互补）→ 抛错。
        回退发生时记录日志并在 model_status 中暴露，便于核对版本。
        """
        candidates: "list[str]" = [self.yolo_model]
        for name in (self.YOLO_V2, self.YOLO_V1):
            if name not in candidates:
                candidates.append(name)
        for name in candidates:
            candidate = self.model_dir / name
            if candidate.exists():
                if name != self.yolo_model:
                    logger.warning(
                        "YOLO 权重 %s 不存在，已自动回退使用 %s", self.yolo_model, name
                    )
                self._active_yolo_name = name
                self._active_yolo_path = candidate
                return candidate
        raise FallModelNotFoundError(str(self.model_dir / self.yolo_model))

    def _resolve_model_paths(self) -> "tuple[Path, Path]":
        """解析并校验模型文件路径，返回 (yolo_path, lstm_path)。

        Raises:
            FallModelNotFoundError: 任一模型文件不存在。
        """
        yolo_path = self._pick_yolo()
        lstm_path = self.model_dir / self.lstm_checkpoint
        if not lstm_path.exists():
            raise FallModelNotFoundError(str(lstm_path))
        return yolo_path, lstm_path

    def load(self) -> None:
        """预加载摔倒检测模型（幂等，可重复调用）。"""
        if self._pipeline is not None:
            return
        with self._load_lock:
            if self._pipeline is not None:
                return
            yolo_path, lstm_path = self._resolve_model_paths()
            logger.info(
                "开始加载摔倒检测模型: yolo=%s (active=%s), lstm=%s, device=%s",
                yolo_path, self._active_yolo_name, lstm_path, self.device or "auto",
            )
            # 将模型项目目录加入模块搜索路径，保证可导入其 src 包（src.pipeline 等）
            model_dir_str = str(self.model_dir)
            if model_dir_str not in sys.path:
                sys.path.insert(0, model_dir_str)

            # 延迟导入：仅在实际运行时依赖 torch / ultralytics，避免影响无模型的启动
            from src.pipeline import FallDetectionPipeline

            old_cwd = os.getcwd()
            os.chdir(self.model_dir)  # 切换工作目录，保证模型内部相对路径（缓存/配置）可用
            try:
                pipeline = FallDetectionPipeline(
                    yolo_model=str(yolo_path),
                    lstm_checkpoint=str(lstm_path),
                    device=self.device,
                    confidence_threshold=self.confidence_threshold,
                    cooldown_frames=self.cooldown_frames,
                    tracker_config=str(self.model_dir / self.tracker_config),
                )
            finally:
                os.chdir(old_cwd)
            self._pipeline = pipeline
            logger.info("摔倒检测模型加载完成")

    @property
    def is_loaded(self) -> bool:
        """模型是否已加载。"""
        return self._pipeline is not None

    def model_status(self) -> Dict[str, Any]:
        """返回模型加载状态信息（供健康检查使用）。"""
        requested_path = self.model_dir / self.yolo_model
        lstm_path = self.model_dir / self.lstm_checkpoint
        active = self._active_yolo_name or (
            self.YOLO_V2 if (self.model_dir / self.YOLO_V2).exists() else self.YOLO_V1
        )
        return {
            "loaded": self.is_loaded,
            "yolo_requested": str(requested_path),
            "yolo_requested_exists": requested_path.exists(),
            "yolo_model": str(self.model_dir / active),
            "yolo_active_name": active,
            "yolo_exists": (self.model_dir / active).exists(),
            "yolo_fallback": active != self.yolo_model,
            "lstm_checkpoint": str(lstm_path),
            "lstm_exists": lstm_path.exists(),
            "device": getattr(self._pipeline, "device", None) if self._pipeline else self.device,
            "confidence_threshold": self.confidence_threshold,
            "cooldown_frames": self.cooldown_frames,
        }

    # ------------------------------------------------------------------
    # 同步检测
    # ------------------------------------------------------------------
    def process_video_sync(
        self,
        input_path: "str | Path",
        output_path: "str | Path | None" = None,
        json_path: "str | Path | None" = None,
        confidence_threshold: "float | None" = None,
        cooldown_frames: "int | None" = None,
    ) -> Dict[str, Any]:
        """同步检测视频中的摔倒事件。

        Args:
            input_path: 输入视频路径。
            output_path: 标注视频输出路径（None 表示不输出）。
            json_path: 事件日志 JSON 输出路径（None 表示不输出）。
            confidence_threshold: 本次推理的跌倒置信度阈值（None 表示用服务默认值）。
            cooldown_frames: 本次推理的告警冷却帧数（None 表示用服务默认值）。
        """
        if self._pipeline is None:
            self.load()
        with self._inference_lock:
            # 阶段3：按请求覆盖参数，推理结束后恢复默认值；锁内执行避免并发串扰。
            old_conf = self.confidence_threshold
            old_cooldown = self.cooldown_frames
            try:
                if confidence_threshold is not None:
                    self.confidence_threshold = confidence_threshold
                    self._pipeline.confidence_threshold = confidence_threshold
                if cooldown_frames is not None:
                    self.cooldown_frames = cooldown_frames
                    self._pipeline.cooldown_frames = cooldown_frames
                return self._pipeline.process_video(
                    input_path=input_path,
                    output_path=output_path,
                    json_path=json_path,
                )
            finally:
                self.confidence_threshold = old_conf
                self.cooldown_frames = old_cooldown
                if hasattr(self._pipeline, "confidence_threshold"):
                    self._pipeline.confidence_threshold = old_conf
                if hasattr(self._pipeline, "cooldown_frames"):
                    self._pipeline.cooldown_frames = old_cooldown

    # ------------------------------------------------------------------
    # 异步检测
    # ------------------------------------------------------------------
    def process_video_async(
        self,
        input_path: "str | Path",
        output_path: "str | Path | None" = None,
        json_path: "str | Path | None" = None,
        confidence_threshold: "float | None" = None,
        cooldown_frames: "int | None" = None,
    ) -> str:
        """异步提交视频检测任务，立即返回 task_id。

        Args:
            input_path: 输入视频路径。
            output_path: 标注视频输出路径。
            json_path: 事件日志 JSON 输出路径。

        Returns:
            str: 任务 ID，可用 get_task_status 查询结果。
        """
        task_id = uuid.uuid4().hex
        self._tasks[task_id] = {
            "id": task_id,
            "status": "pending",  # pending / running / done / failed
            "created_at": datetime.now().isoformat(),
            "finished_at": None,
            "input": str(input_path),
            "output": str(output_path) if output_path else None,
            "confidence_threshold": confidence_threshold,
            "cooldown_frames": cooldown_frames,
            "result": None,
            "error": None,
        }

        def _run() -> None:
            """后台执行检测并更新任务状态。"""
            self._tasks[task_id]["status"] = "running"
            try:
                result = self.process_video_sync(
                    input_path, output_path, json_path,
                    confidence_threshold=confidence_threshold,
                    cooldown_frames=cooldown_frames,
                )
                self._tasks[task_id]["status"] = "done"
                self._tasks[task_id]["result"] = result
            except Exception as exc:  # noqa: BLE001 需捕获所有异常写入任务状态
                logger.exception("异步检测任务 %s 失败", task_id)
                self._tasks[task_id]["status"] = "failed"
                self._tasks[task_id]["error"] = str(exc)
            finally:
                self._tasks[task_id]["finished_at"] = datetime.now().isoformat()

        self._executor.submit(_run)
        return task_id

    def get_task_status(self, task_id: str) -> Optional[Dict[str, Any]]:
        """查询异步任务状态。

        Args:
            task_id: 任务 ID。

        Returns:
            dict | None: 任务信息；不存在时返回 None。
        """
        return self._tasks.get(task_id)

    def shutdown(self) -> None:
        """关闭线程池（应用退出时调用）。"""
        self._executor.shutdown(wait=False, cancel_futures=True)


# 模块级单例访问入口
_service: Optional[FallDetectionService] = None


def get_service() -> FallDetectionService:
    """获取全局摔倒检测服务实例（懒加载）。"""
    global _service
    if _service is None:
        _service = FallDetectionService.get_instance()
    return _service