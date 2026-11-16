import request from '@/utils/request'
import type { Result, PageVO } from '@/utils/request'

// ===== 类型定义 =====
export interface SpaceVO {
  id: number
  code: string
  name: string
  building: string
  floor: number
  layout: string
  area: number
  occupancyStatus: string
  elderName: string | null
  elderId: number | null
  moveInDate: string | null
  lastOccupied: string | null
  facilities: string[] | string
  risks: string[] | string
  status: string
  safetyRating: string
  lastInspection: string | null
  lastInspectionResult: string | null
  nextInspection: string | null
  remark: string
  createTime?: string
}

// ===== 检查记录（SpaceInspection） =====
export interface InspectionRecordVO {
  id: number
  spaceId: number
  date: string
  result: string
  inspector: string
  type?: string
  issue?: string
  repairStatus?: string
  deadline?: string
  createTime?: string
}

export interface SpaceQueryDTO {
  page?: number
  size?: number
  building?: string
  floor?: number
  occupancyStatus?: string
  safetyRating?: string
  elderName?: string
}

export interface SpaceUpdateDTO {
  name?: string
  building?: string
  floor?: number
  layout?: string
  area?: number
  occupancyStatus?: string
  elderName?: string
  facilities?: string[]
  risks?: string[]
  status?: string
  remark?: string
}

export interface InspectionSaveDTO {
  spaceId: number
  type?: string
  inspector: string
  result: string
  issue?: string
  repairStatus?: string
  deadline?: string
}

// ===== 空间列表（分页） =====
export function getSpaceList(params: SpaceQueryDTO) {
  return request.get<Result<PageVO<SpaceVO>>>('/space/list', { params })
}

// ===== 空间详情（含检查历史） =====
export function getSpaceById(id: number) {
  return request.get<Result<{ space: SpaceVO; inspectionHistory: InspectionRecordVO[] }>>(
    `/space/${id}`,
  )
}

// ===== 更新空间 =====
export function updateSpace(id: number, data: SpaceUpdateDTO) {
  return request.put<Result<SpaceVO>>(`/space/${id}`, data)
}

// ===== 删除空间 =====
export function deleteSpace(id: number) {
  return request.delete<Result<void>>(`/space/${id}`)
}

// ===== 添加检查记录 =====
export function addInspection(data: InspectionSaveDTO) {
  return request.post<Result<void>>('/space/inspection', data)
}
