import request from '@/utils/request'
import type { Result, PageVO } from '@/utils/request'

// ===== 类型定义 =====
export interface Caregiver {
  id: number
  name: string
  age: number
  position: string
  phone: string
  address: string
  status: string
  createTime?: string
}

export interface CaregiverSaveDTO {
  name: string
  age: number
  position: string
  phone: string
  address?: string
  status: string
}

// ===== 陪护/护工列表（分页） =====
export function getCaregiverList(params?: {
  page?: number
  size?: number
  name?: string
  position?: string
  status?: string
}) {
  return request.get<Result<PageVO<Caregiver>>>('/caregiver/list', { params })
}

// ===== 新增护工 =====
export function addCaregiver(data: CaregiverSaveDTO) {
  return request.post<Result<number>>('/caregiver', data)
}

// ===== 编辑护工 =====
export function updateCaregiver(id: number, data: CaregiverSaveDTO) {
  return request.put<Result<void>>(`/caregiver/${id}`, data)
}

// ===== 删除护工 =====
export function deleteCaregiver(id: number) {
  return request.delete<Result<void>>(`/caregiver/${id}`)
}
