import request from '@/utils/request'
import type { Result } from '@/utils/request'

export interface ElderProfile {
  id: number
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
  familyCount: number
  families: { name: string; relationship: string; phone: string; isEmergency: boolean }[]
}

export interface ElderExpert {
  id: number
  userId: number
  name: string
  title: string
  specialty: string
  institution: string
}

export interface MetricPoint {
  time: string
  value?: number
  systolic?: number
  diastolic?: number
}

export interface ElderHealth {
  elderId: number
  elderName: string
  latest: Record<string, any>
  trend: Record<string, MetricPoint[]>
}

export function getElderProfile() {
  return request.get<Result<ElderProfile>>('/elder/portal/me')
}

export function getElderExperts() {
  return request.get<Result<ElderExpert[]>>('/elder/portal/experts')
}

export function getElderHealth() {
  return request.get<Result<ElderHealth>>('/elder/portal/health')
}

// ===== 一键呼救 =====
export function submitSos(location?: string) {
  return request.post<Result<{ sosId: number; alertId: number }>>('/elder/sos', location ? { location } : {})
}

// ===== 关系 / 提醒 / 打分 =====
export interface ElderRelation {
  caregivers: { name: string; serviceType: string; status: string }[]
  families: { name: string; relationship: string; phone: string; isEmergency: boolean }[]
}

export interface ElderReminder {
  elderName: string
  tasks: {
    id: number
    taskCode: string
    taskType: string
    elderName: string
    priority: string
    taskStatus: string
    triggerReson: string
    note: string
    taskDate: string
  }[]
}

export function getElderRelation() {
  return request.get<Result<ElderRelation>>('/elder/portal/relation')
}

export function getElderReminder() {
  return request.get<Result<ElderReminder>>('/elder/portal/reminder')
}

export function rateConsult(data: { conversationId: number; rating: number; feedback?: string }) {
  return request.post<Result<boolean>>('/elder/portal/rate', data)
}
