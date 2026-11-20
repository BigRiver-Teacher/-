import request from '@/utils/request'
import type { Result } from '@/utils/request'

export interface ExpertScheduleApply {
  id: number
  expertId: number
  expertName: string
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

export function submitApply(data: { applyType: string; startTime: string; endTime: string; reason: string }) {
  return request.post<Result<number>>('/expert/schedule/apply', data)
}

export function getMyApplies() {
  return request.get<Result<ExpertScheduleApply[]>>('/expert/schedule/apply/list')
}
