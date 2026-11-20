import request from '@/utils/request'
import type { Result } from '@/utils/request'

export interface ExpertMe {
  id: number
  name: string
  title: string
  specialty: string
  institution: string
  phone: string
  rating: number
  schedule: string
  todayCount: number
  totalCount: number
}

export interface ExpertWorkbench {
  expertName: string
  title: string
  todayCount: number
  pendingCount: number
  totalCount: number
  rating: number
  trend?: { date: string; count: number }[]
}

export function getExpertMe() {
  return request.get<Result<ExpertMe>>('/expert/portal/me')
}

export function getExpertWorkbench() {
  return request.get<Result<ExpertWorkbench>>('/expert/portal/workbench')
}
