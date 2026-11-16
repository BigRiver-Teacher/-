import request from '@/utils/request'
import type { Result, PageVO } from '@/utils/request'

// ===== 类型定义 =====
export interface InterventionVO {
  id: number
  code: string
  name: string
  type: string
  priority: string
  status: string
  leader: string
  targetGroup: string
  totalWeeks: number
  currentWeek: number
  frequency: string
  startDate: string | null
  endDate: string | null
  associatedElders: string[] | string
  goals: string[] | string
  measures: string[] | string
  notes: string
  rating: number
  avgExecRating: number
  progress: number
  executedCount: number
  totalCount: number
  abnormalCount: number
  records?: InterventionRecordVO[]
  createTime: string
}

// ===== 执行记录（InterventionRecord） =====
export interface InterventionRecordVO {
  id: number
  interventionId: number
  executeDate: string
  elderName: string
  executor: string
  status: string
  rating: number
  remark?: string
  note?: string
  attachments?: string[]
  createTime: string
}

export interface InterventionUpdateDTO {
  name?: string
  type?: string
  priority?: string
  status?: string
  leader?: string
  targetGroup?: string
  totalWeeks?: number
  frequency?: string
  associatedElders?: string[]
  goals?: string[]
  measures?: string[]
  notes?: string
}

export interface ReviewDTO {
  result: string
  comment: string
}

export interface ExecRecordSaveDTO {
  interventionId: number
  elderName: string
  executor: string
  status: string
  rating: number
  note?: string
}

// ===== 干预方案列表（分页） =====
export function getInterventionList(params: {
  page?: number
  size?: number
  name?: string
  type?: string
  status?: string
  priority?: string
}) {
  return request.get<Result<PageVO<InterventionVO>>>('/intervention/list', { params })
}

// ===== 干预方案详情 =====
export function getInterventionById(id: number) {
  return request.get<Result<InterventionVO>>(`/intervention/${id}`)
}

// ===== 干预方案执行记录 =====
export function getInterventionRecords(id: number) {
  return request.get<Result<PageVO<InterventionRecordVO>>>(`/intervention/records/${id}`)
}

// ===== 更新干预方案 =====
export function updateIntervention(id: number, data: InterventionUpdateDTO) {
  return request.put<Result<InterventionVO>>(`/intervention/${id}`, data)
}

// ===== 删除干预方案 =====
export function deleteIntervention(id: number) {
  return request.delete<Result<void>>(`/intervention/${id}`)
}

// ===== 审核干预方案 =====
export function reviewIntervention(id: number, data: ReviewDTO) {
  return request.put<Result<void>>(`/intervention/review/${id}`, data)
}

// ===== 暂停干预方案 =====
export function pauseIntervention(id: number) {
  return request.put<Result<void>>(`/intervention/pause/${id}`)
}

// ===== 终止干预方案 =====
export function terminateIntervention(id: number) {
  return request.put<Result<void>>(`/intervention/terminate/${id}`)
}

// ===== 完成干预方案 =====
export function completeIntervention(id: number) {
  return request.put<Result<void>>(`/intervention/complete/${id}`)
}

// ===== 添加执行记录 =====
export function addInterventionRecord(data: ExecRecordSaveDTO) {
  return request.post<Result<void>>('/intervention/record', data)
}
