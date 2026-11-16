import request from '@/utils/request'
import type { Result } from '@/utils/request'

// ===== 自定义配置模块（侧边栏 / 看板 / 移动端） =====

// ---- 侧边栏配置 ----
export interface SidebarGroup {
  id: string
  name: string
}

export interface SidebarItem {
  id: string
  name: string
  icon: string
  group: string | null
  visible: boolean
}

export interface SidebarConfig {
  groups: SidebarGroup[]
  items: SidebarItem[]
}

// ---- 看板配置 ----
export interface DashboardCard {
  id: string
  name: string
  icon: string
  type: string
  visible: boolean
  colspan: number
  color: string
  previewValue: number
}

export interface DashboardConfig {
  cards: DashboardCard[]
}

// ---- 移动端配置 ----
export interface MobileTab {
  id: string
  name: string
  icon: string
  path: string
  visible: boolean
}

export interface MobileMenuItem {
  id: string
  name: string
  icon: string
  visible: boolean
}

export interface MobileFeature {
  id: string
  name: string
  icon: string
  visible: boolean
  badge: string
}

export interface MobileThemeColors {
  primary: string
  secondary: string
  background: string
}

export interface MobilePushSetting {
  enabled: boolean
  sound: boolean
  banner: boolean
  content: string
}

export interface MobileConfig {
  tabList: MobileTab[]
  menuList: MobileMenuItem[]
  featureList: MobileFeature[]
  themeColors: MobileThemeColors
  pushSetting: MobilePushSetting
}

// ===== 侧边栏配置接口 =====
export function getSidebarConfig() {
  return request.get<Result<SidebarConfig>>('/custom/sidebar')
}

export function saveSidebarConfig(data: SidebarConfig) {
  return request.put<Result<null>>('/custom/sidebar', data)
}

export function resetSidebarConfig() {
  return request.post<Result<null>>('/custom/sidebar/reset')
}

// ===== 看板配置接口 =====
export function getDashboardConfig() {
  return request.get<Result<DashboardConfig>>('/custom/dashboard')
}

export function saveDashboardConfig(data: DashboardConfig) {
  return request.put<Result<null>>('/custom/dashboard', data)
}

export function resetDashboardConfig() {
  return request.post<Result<null>>('/custom/dashboard/reset')
}

// ===== 移动端配置接口 =====
export function getMobileConfig() {
  return request.get<Result<MobileConfig>>('/custom/mobile')
}

export function saveMobileConfig(data: MobileConfig) {
  return request.put<Result<null>>('/custom/mobile', data)
}

export function resetMobileConfig() {
  return request.post<Result<null>>('/custom/mobile/reset')
}
