import request from '@/utils/request'
import type { Result } from '@/utils/request'

export interface SupervisorMe {
  id: number
  name: string
  position: string
  community: string
}

export interface TeamMember {
  id: number
  name: string
  position: string
  phone: string
  elderCount: number
  elders: string[]
}

export interface SupervisorTeam {
  supervisor: string
  community: string
  members: TeamMember[]
}

export interface ScheduleItem {
  id: number
  caregiverId: number
  caregiverName: string
  community: string
  workDate: string
  shift: string
  startTime: string
  endTime: string
  note: string
}

export interface ScheduleSaveDTO {
  caregiverId: number
  workDate: string
  shift: string
  startTime?: string
  endTime?: string
  note?: string
}

export interface ScheduleApply {
  id: number
  caregiverId: number
  caregiverName: string
  community: string
  applyType: string
  startTime: string
  endTime: string
  reason: string
  status: string
  reviewerName: string
  reviewComment: string
  reviewTime: string
  createTime: string
}

export function getSupervisorMe() {
  return request.get<Result<SupervisorMe>>('/supervisor/portal/me')
}

export function getSupervisorTeam() {
  return request.get<Result<SupervisorTeam>>('/supervisor/portal/team')
}

export function getSchedule(start?: string, end?: string) {
  return request.get<Result<ScheduleItem[]>>('/supervisor/portal/schedule', {
    params: { start, end },
  })
}

export function saveSchedule(data: ScheduleSaveDTO) {
  return request.post<Result<number>>('/supervisor/portal/schedule/save', data)
}

export function deleteSchedule(id: number) {
  return request.delete<Result<boolean>>(`/supervisor/portal/schedule/${id}`)
}

// ===== 陪护请假申请（护工端发起 / 主管端审批） =====
export function submitCaregiverApply(data: {
  applyType: string
  startTime: string
  endTime: string
  reason?: string
}) {
  return request.post<Result<number>>('/caregiver/schedule/apply', data)
}

export function getMyApplies() {
  return request.get<Result<ScheduleApply[]>>('/caregiver/schedule/apply/list')
}

export function getAllApplies() {
  return request.get<Result<ScheduleApply[]>>('/caregiver/schedule/apply/all')
}

export function reviewApply(id: number, data: { status: string; comment?: string }) {
  return request.put<Result<boolean>>(`/caregiver/schedule/apply/${id}/review`, data)
}
