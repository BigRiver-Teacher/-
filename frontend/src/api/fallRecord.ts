import request from '@/utils/request'
import type { Result } from '@/utils/request'
import type { FallVO } from '@/api/fall'

// ===== 类型定义 =====
// 跌倒记录新增/编辑入参（对应后端 FallRecordAddDTO / FallRecordUpdateDTO）
export interface FallRecordSaveDTO {
  id?: number
  elderId?: number
  elderName: string
  fallTime: string
  location: string
  cause: string
  injuryPart?: string
  severity: string
  treatment?: string
  recoverStatus: string
  handler: string
}

// ===== 新增跌倒记录 =====
export function addFallRecord(data: FallRecordSaveDTO) {
  return request.post<Result<number>>('/fall', data)
}

// ===== 编辑跌倒记录（后端要求路径 ID 与请求体 ID 一致） =====
export function updateFallRecord(id: number, data: FallRecordSaveDTO) {
  return request.put<Result<void>>(`/fall/${id}`, { ...data, id })
}

// ===== 删除跌倒记录 =====
export function deleteFallRecord(id: number) {
  return request.delete<Result<void>>(`/fall/${id}`)
}
