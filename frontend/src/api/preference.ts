import request from '@/utils/request'
import type { Result } from '@/utils/request'

// ===== 类型定义 =====
export interface PreferenceChannel {
  code: string
  name: string
  enable: boolean
}

export interface PreferenceLevelFilter {
  code: string
  name: string
  channels: string[]
}

export interface PreferenceReceiver {
  // 后端接口文档字段为 enderName（疑似 elderName 的笔误），读取/保存时两端兼容
  elderName?: string
  enderName?: string
  managerName: string
  managerPhone: string
  familyName: string
  familyPhone: string
  backupName: string
  backupPhone: string
}

export interface PreferenceScene {
  name: string
  enable: boolean
  channels: string[]
  delayMin: number
  silentMode: boolean
}

export interface PreferenceTemplate {
  scene: string
  content: string
}

export interface PreferenceSilentPeriod {
  start: string
  end: string
  name: string
}

export interface PreferenceConfig {
  channels: PreferenceChannel[]
  levelFilters: PreferenceLevelFilter[]
  receivers: PreferenceReceiver[]
  scenes: PreferenceScene[]
  templates: PreferenceTemplate[]
  silentPeriods: PreferenceSilentPeriod[]
}

// ===== 获取推送偏好配置 =====
export function getPreferenceConfig() {
  return request.get<Result<PreferenceConfig>>('/preference/config')
}

// ===== 保存推送偏好配置 =====
export function savePreferenceConfig(data: PreferenceConfig) {
  return request.put<Result<string>>('/preference/config', data)
}

// ===== 测试推送 =====
export function testPush(data: { elderName: string; channel: string; content: string }) {
  return request.post<Result<string>>('/preference/test-push', data)
}
