import request from '@/utils/request'
import type { Result, PageVO } from '@/utils/request'

// ===== 类型定义 =====
export interface FallVO {
  treatment: string
  injuryPart: string
  id: number
  elderName: string
  date: string
  location: string
  cause: string
  severity: string
  status: string
  elderId?: number
  handler?: string
  handleTime?: string
  remark?: string
}

// ===== 跌倒记录列表（分页） =====
export function getFallList(params?: { page?: number; size?: number; elderName?: string }) {
  return request.get<Result<PageVO<FallVO>>>('/fall/list', { params })
}

// ===== 根据 ID 获取跌倒记录 =====
export function getFallById(id: number) {
  return request.get<Result<FallVO>>(`/fall/${id}`)
}
