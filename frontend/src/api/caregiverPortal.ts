import request from '@/utils/request'
import type { Result } from '@/utils/request'

export interface CaregiverMe {
  id: number
  name: string
  position: string
  phone: string
  community: string
}

export interface CaregiverWorkbench {
  name: string
  position: string
  elderCount: number
  taskCount: number
  alertCount: number
}

export interface CaregiverElder {
  id: number
  name: string
  gender: string
  age: number
  phone: string
  address: string
  remark: string
}

export interface CaregiverTask {
  id: number
  taskCode: string
  taskType: string
  elderName: string | null
  location: string | null
  priority: string
  taskStatus: string
  triggerReson: string
  handleResult: string | null
  handler: string | null
  note: string | null
  taskDate: string
}

export function getCaregiverMe() {
  return request.get<Result<CaregiverMe>>('/caregiver/portal/me')
}

export function getCaregiverWorkbench() {
  return request.get<Result<CaregiverWorkbench>>('/caregiver/portal/workbench')
}

export function getMyElders() {
  return request.get<Result<CaregiverElder[]>>('/caregiver/portal/elders')
}

export function getMyTasks() {
  return request.get<Result<CaregiverTask[]>>('/caregiver/portal/tasks')
}
