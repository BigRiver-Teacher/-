import request from '@/utils/request'
import type { Result } from '@/utils/request'

// ===== 类型定义 =====
export interface ModelParamsVO {
  detection: {
    sensitivity: number
    acceleration: number
    angle: number
    timeWindow: number
    confidence: number
  }
  evaluation: {
    gaitThreshold: number
    activityThreshold: number
    riskCycle: number
  }
  alert: {
    delay: number
    cooldown: number
    threshold: number
  }
  device: {
    sensorFrequency: number
    cameraFps: number
    offlineTimeout: number
  }
}

export interface ModelParamsResult extends ModelParamsVO {
  version: {
    current: string
    status: string
    lastUpdate: string
    totalVersions: number
  }
}

export interface VersionHistoryItem {
  version: string
  time: string
  user: string
  description: string
}

// ===== 获取模型参数 =====
export function getModelParams() {
  return request.get<Result<ModelParamsResult>>('/model/params')
}

// ===== 保存模型参数（不带 version 字段） =====
export function updateModelParams(data: ModelParamsVO) {
  return request.put<Result<void>>('/model/params', data)
}

// ===== 恢复默认参数 =====
export function resetModelParams() {
  return request.put<Result<void>>('/model/reset')
}

// ===== 参数版本历史 =====
export function getModelHistory() {
  return request.get<Result<VersionHistoryItem[]>>('/model/history')
}

// ===== 回滚到指定版本 =====
export function rollbackModelVersion(version: string) {
  return request.post<Result<void>>(`/model/rollback/${version}`)
}
