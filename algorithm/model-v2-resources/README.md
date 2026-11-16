# 姿态模型改进版（v2）资源说明

> 来源：算法组交付包 D:\fall-detection-data.zip（完整包已解压至 D:\fall-detection-data，约 445MB，含训练数据集与场景视频，供复现）。
> 本目录仅收录推理与验证所需精选资源。

## 目录
| 路径 | 内容 |
| --- | --- |
| weights/yolo11n-pose-fallen.pt | 改进版权重（6MB，微调“躺地/跌倒人体”，COCO 17 关键点布局不变） |
| scripts/train_pose.py | 微调脚本（60 轮 / imgsz=1024 / batch=8 / freeze=10 / lr0=0.0005） |
| scripts/ab_test23.py, ab_test.py | 新旧权重对比测试脚本 |
| data/data.yaml | Simuletic_Person 数据集配置（单 person 类，kpt_shape=[17,3]） |
| evidence/ | 训练日志、推理日志、事件 JSON、results.csv 与训练曲线（真实记录） |

## 关键实测指标（来自 results.csv 第 60 轮验证集）
- Precision(B)=0.9947，Recall(B)=0.9167，mAP50=0.915，mAP50-95=0.8713
- 训练耗时约 367.9s（60 轮）

## 场景验证（置信度阈值 0.4）
- 视频 01（台阶跌倒）：检出跌倒 1 起，置信度 1.0（事件 out_01_new.events.json）
- 视频 03（老人洗衣服台阶跌倒）：检出跌倒 1 起，置信度 0.745（out_03_*_new.events.json）
- 视频 02（推车老人跌倒）：out_02_*_new.events.json

## 接入与回退
- 启用：复制权重至模型工程根目录后，设置 FALL_YOLO_MODEL=yolo11n-pose-fallen.pt（跌倒服务 .env / .env.example 已默认指向 v2）
- 回退：FALL_YOLO_MODEL=yolo11n-pose.pt
- 完整算法交付包（训练数据集 CCTV / Simuletic 等）位于 D:\fall-detection-data
