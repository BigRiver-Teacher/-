import request from '@/utils/request'
import type { Result, PageVO } from '@/utils/request'

// ===== 类型定义 =====
export interface AlertVO {
  id: number
  elderlyName: string
  elderlyId: string
  warningType: string
  priority: string
  description: string
  time: string
  location: string
  status: string
  handler: string
  handleTime: string
  contactPhone: string
  emergencyContact: string
  suggestedAction: string
}

export interface AlertQueryDTO {
  page?: number
  size?: number
  warningType?: string
  priority?: string
  status?: string
  startDate?: string
  endDate?: string
  keyword?: string
}

// ===== 告警列表（分页） =====
export function getAlertList(params: AlertQueryDTO) {
  return request.get<Result<PageVO<AlertVO>>>('/alert/list', { params })
}

// ===== 处理告警 =====
export function processAlert(id: number, data: { handler: string; handleNote: string }) {
  return request.put<Result<string>>(`/alert/process/${id}`, data)
}

// ===== 忽略告警 =====
export function ignoreAlert(id: number) {
  return request.put<Result<string>>(`/alert/ignore/${id}`)
}

// ===== 标记已读 =====
export function markAlertRead(id: number) {
  return request.put<Result<string>>(`/alert/read/${id}`)
}

// ===== 批量处理 =====
export function batchProcessAlerts(ids: number[]) {
  return request.put<Result<string>>('/alert/batch-process', { ids })
}

// ===== 批量忽略 =====
export function batchIgnoreAlerts(ids: number[]) {
  return request.put<Result<string>>('/alert/batch-ignore', { ids })
}
