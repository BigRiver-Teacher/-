import request from '@/utils/request'
import type { Result, PageVO } from '@/utils/request'

// ===== 类型定义 =====
export interface ElderVO {
  id: number
  code: string
  name: string
  gender: string
  age: number
  idCard: string
  birthDate: string
  ethnicity: string
  phone: string
  address: string
  residenceType: string
  remark: string
  createTime?: string
  updateTime?: string
}

export interface ElderDTO {
  id?: number
  name: string
  gender: string
  age?: number
  idCard: string
  birthDate?: string
  ethnicity?: string
  phone: string
  address?: string
  residenceType?: string
  remark?: string
}

// ===== 老人列表（分页） =====
export function getElderList(params: {
  page?: number
  size?: number
  name?: string
  phone?: string
}) {
  return request.get<Result<PageVO<ElderVO>>>('/elder/list', { params })
}

// ===== 根据 ID 获取老人详情 =====
export function getElderById(id: number) {
  return request.get<Result<ElderVO>>(`/elder/${id}`)
}

// ===== 新增老人 =====
export function addElder(data: ElderDTO) {
  return request.post<Result<number>>('/elder', data)
}

// ===== 编辑老人 =====
export function updateElder(id: number, data: ElderDTO) {
  return request.put<Result<void>>(`/elder/${id}`, data)
}

// ===== 删除老人 =====
export function deleteElder(id: number) {
  return request.delete<Result<void>>(`/elder/${id}`)
}
