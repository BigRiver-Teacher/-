import request from '@/utils/request'
import type { Result, PageVO } from '@/utils/request'

// ===== 类型定义 =====
export interface ScheduleTaskVO {
  id: number
  taskCode: string
  taskType: string
  elderName: string | null
  location: string | null
  priority: string
  taskStatus: string
  triggerReson: string
  relateAlertCode: string | null
  relateDeviceSn: string | null
  handleResult: string | null
  handler: string | null
  completeTime: string | null
  note: string | null
  taskDate: string
  createTime: string
}

export interface ScheduleTaskQueryDTO {
  date: string
  page?: number
  size?: number
}

export interface ProcessTaskDTO {
  taskStatus: string
  handleResult: string
  handler: string
  note?: string
}

// ===== 按日期查询任务（分页） =====
export function getScheduleTasks(params: ScheduleTaskQueryDTO) {
  return request.get<Result<PageVO<ScheduleTaskVO>>>('/schedule/tasks', { params })
}

// ===== 任务详情 =====
export function getScheduleTaskById(id: number) {
  return request.get<Result<ScheduleTaskVO>>(`/schedule/task/${id}`)
}

// ===== 处理任务 =====
export function processScheduleTask(id: number, data: ProcessTaskDTO) {
  return request.put<Result<void>>(`/schedule/task/${id}/process`, data)
}
