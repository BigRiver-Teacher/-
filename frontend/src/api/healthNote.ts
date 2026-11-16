import request from '@/utils/request'
import type { Result, PageVO } from '@/utils/request'

// ===== 类型定义 =====
export interface HealthNote {
  name: string
  id: number
  elderId: number
  elderName: string
  recordTime: string
  recorder: string
  noteType: string
  content: string
  importance: string
  fallowStatus: string
  createTime?: string
}

export interface HealthNoteDTO {
  elderId?: number
  elderName: string
  recordTime: string
  recorder: string
  noteType: string
  content: string
  importance: string
  fallowStatus: string
}

// ===== 健康档案/备注列表（分页） =====
export function getHealthNoteList(params?: {
  page?: number
  size?: number
  elderName?: string
  noteType?: string
  importance?: string
  followStatus?: string
}) {
  return request.get<Result<PageVO<HealthNote>>>('/health-note/list', { params })
}

// ===== 新增健康备注 =====
export function addHealthNote(data: HealthNoteDTO) {
  return request.post<Result<number>>('/health-note', data)
}

// ===== 删除健康备注 =====
export function deleteHealthNote(id: number) {
  return request.delete<Result<void>>(`/health-note/${id}`)
}
