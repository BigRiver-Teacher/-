import request from '@/utils/request'
import type { Result, PageVO } from '@/utils/request'

// ===== 类型定义 =====
export interface ConsultVO {
  id: number
  elderName: string
  type: string
  expertId: number | null
  expertName: string | null
  status: string
  urgency: string
  consultTime: string
  content: string
  reply: string | null
  rating: number | null
  feedback: string | null
  createTime: string
}

export interface ConsultCreateDTO {
  elderName: string
  type: string
  urgency: string
  content: string
}

export interface ConsultCompleteDTO {
  reply: string
  rating: number
  feedback?: string
}

// ===== 咨询列表（分页） =====
export function getConsultList(params?: {
  page?: number
  size?: number
  elderName?: string
  expertName?: string
  status?: string
  urgency?: string
}) {
  return request.get<Result<PageVO<ConsultVO>>>('/consult/list', { params })
}

// ===== 新建咨询 =====
export function addConsult(data: ConsultCreateDTO) {
  return request.post<Result<number>>('/consult', data)
}

// ===== 分配专家 =====
export function assignConsult(id: number, expertId: number) {
  return request.put<Result<string>>(`/consult/assign/${id}`, { expertId })
}

// ===== 完成咨询 =====
export function completeConsult(id: number, data: ConsultCompleteDTO) {
  return request.put<Result<string>>(`/consult/complete/${id}`, data)
}

// ===== 删除咨询 =====
export function deleteConsult(id: number) {
  return request.delete<Result<string>>(`/consult/${id}`)
}
