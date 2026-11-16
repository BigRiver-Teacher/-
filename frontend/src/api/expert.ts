import request from '@/utils/request'
import type { Result, PageVO } from '@/utils/request'

// ===== 类型定义 =====
export interface ExpertVO {
  id: number
  name: string
  title: string
  titleText?: string
  specialty?: string
  institution?: string
  phone?: string
  status?: string
  todayCount?: number
  totalCount?: number
  rating?: number
  schedule?: string
}

export interface ExpertSaveDTO {
  name: string
  title: string
  specialty?: string
  institution?: string
  phone?: string
  status?: string
  rating?: number
  schedule?: string
}

export interface ExpertUpdateDTO {
  name: string
  title: string
  specialty?: string
  institution?: string
  phone?: string
  status?: string
  rating?: number
}

// ===== 专家列表（分页） =====
export function getExpertList(params?: {
  page?: number
  size?: number
  name?: string
  title?: string
  specialty?: string
  status?: string
}) {
  return request.get<Result<PageVO<ExpertVO>>>('/expert/list', { params })
}

// ===== 根据 ID 获取专家详情 =====
export function getExpertById(id: number) {
  return request.get<Result<ExpertVO>>(`/expert/${id}`)
}

// ===== 新增专家 =====
export function addExpert(data: ExpertSaveDTO) {
  return request.post<Result<void>>('/expert', data)
}

// ===== 编辑专家 =====
export function updateExpert(id: number, data: ExpertUpdateDTO) {
  return request.put<Result<void>>(`/expert/${id}`, data)
}

// ===== 删除专家 =====
export function deleteExpert(id: number) {
  return request.delete<Result<void>>(`/expert/${id}`)
}

// ===== 更新专家排班 =====
export function updateExpertSchedule(id: number, data: { schedule: string }) {
  return request.put<Result<void>>(`/expert/schedule/${id}`, data)
}
