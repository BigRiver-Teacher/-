import request from '@/utils/request'
import type { Result } from '@/utils/request'

// ===== 设备统计 =====
export interface EchartDeviceStatDTO {
  totalDevices: number
  onlineDevices: number
  offlineDevices: number
  faultDevices: number
  bindRate: number
  todayAlertCount: number
}

export interface EchartTrendDTO {
  categories: string[]
  series: Array<{ name: string; data: number[] }>
}

export interface EchartDeviceFaultDTO {
  categories: string[]
  series: Array<{ name: string; data: number[] }>
}

export interface EchartPieDTO {
  name: string
  value: number
}

export interface EchartTimeCurveVO {
  categories: string[]
  series: Array<{ name: string; data: number[] }>
}

export interface EchartPieVO {
  name: string
  value: number
}

export interface EchartLineBarVO {
  categories: string[]
  lineSeries: Array<{ name: string; data: number[] }>
  barSeries: Array<{ name: string; data: number[] }>
}

export interface EchartRadarDTO {
  indicator: Array<{ name: string; max: number }>
  series: Array<{ name: string; value: number[] }>
}

export interface EchartStatVO {
  totalExperts: number
  totalConsultations: number
  avgSatisfaction: number
  pendingConsultations: number
}

// ===== 设备统计接口 =====
export function getDeviceStatistics() {
  return request.get<Result<EchartDeviceStatDTO>>('/echart/device/statistics')
}

export function getDeviceOnlineTrend() {
  return request.get<Result<EchartTrendDTO>>('/echart/device/online-trend')
}

export function getDeviceFaultStatistics() {
  return request.get<Result<EchartDeviceFaultDTO>>('/echart/device/fault-statistics')
}

export function getDeviceAlertTrend() {
  return request.get<Result<EchartTrendDTO>>('/echart/device/alert-trend')
}

// ===== 监控统计接口 =====
export function getMonitorHeartbeat(elderId: number) {
  // 后端实际路由：MonitorEchartController#heartbeat → GET /api/echart/monitor/heartbeat
  return request.get<Result<EchartTimeCurveVO>>('/echart/monitor/heartbeat', {
    params: { elderId },
  })
}

export function getMonitorDeviceStatus() {
  return request.get<Result<EchartPieVO[]>>('/echart/monitor/device-status')
}

export function getMonitorAcceleration(elderId: number) {
  return request.get<Result<EchartTimeCurveVO>>('/echart/monitor/acceleration', {
    params: { elderId },
  })
}

// ===== 人员统计接口 =====
export function getPersonTrend() {
  return request.get<Result<EchartTrendDTO>>('/echart/person/trend')
}

export function getPersonRatio() {
  return request.get<Result<EchartPieDTO[]>>('/echart/person/ratio')
}

export function getGenderDistribution() {
  return request.get<Result<EchartPieDTO[]>>('/echart/person/gender-distribution')
}

export function getCaregiverDistribution() {
  return request.get<Result<EchartRadarDTO>>('/echart/person/caregiver-distribution')
}

export function getAgeDistribution() {
  return request.get<Result<EchartPieDTO[]>>('/echart/person/age-distribution')
}

// ===== 专家统计接口 =====
export function getExpertTitleDistribution() {
  return request.get<Result<EchartPieVO[]>>('/echart/expert/title-distribution')
}

export function getExpertStatistics() {
  return request.get<Result<EchartStatVO>>('/echart/expert/statistics')
}

export function getExpertSatisfactionDistribution() {
  return request.get<Result<EchartPieVO[]>>('/echart/expert/satisfaction-distribution')
}

export function getExpertFieldDistribution() {
  return request.get<Result<EchartLineBarVO>>('/echart/expert/field-distribution')
}

export function getExpertConsultationTrend() {
  return request.get<Result<EchartLineBarVO>>('/echart/expert/consultation-trend')
}

// ===== 系统概览（注册用户/生成报告等） =====
export interface SystemOverviewVO {
  registeredUsers: number
  generatedReports: number
  onlineDevices: number
  pendingWarnings: number
}

export function getSystemOverview() {
  return request.get<Result<SystemOverviewVO>>('/stats/overview')
}

// ===== 全局搜索 =====
export function globalSearch(keyword: string) {
  return request.get<Result<any[]>>('/search', { params: { keyword } })
}
