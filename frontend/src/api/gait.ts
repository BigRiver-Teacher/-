import request from '@/utils/request'
import type { Result, PageVO } from '@/utils/request'

// ===== 类型定义 =====
export interface GaitRecord {
  id: number
  name: string
  elderId: number
  date: string
  speed: number
  frequency: number
  stepLength: number
  score: number
  result: string
}

export interface GaitDTO {
  name: string
  elderId: number
  date: string
  speed: number
  frequency: number
  stepLength: number
  score: number
  result: string
}

// ===== 步态记录列表（分页） =====
export function getGaitList(params?: {
  page?: number
  size?: number
  name?: string
  result?: string
}) {
  return request.get<Result<PageVO<GaitRecord>>>('/gait/list', { params })
}

// ===== 新增步态记录 =====
export function addGait(data: GaitDTO) {
  return request.post<Result<number>>('/gait', data)
}

// ===== 删除步态记录 =====
export function deleteGait(id: number) {
  return request.delete<Result<void>>(`/gait/${id}`)
}
