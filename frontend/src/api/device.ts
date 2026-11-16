import request from '@/utils/request'
import type { Result, PageVO } from '@/utils/request'

// ===== 类型定义 =====
export interface DeviceVO {
  id: number
  deviceSn: string
  deviceName: string
  deviceType: string
  status: string
  battery: number | null
  firmware: string
  bindElder: string | null
  bindElderId: number | null
  bindTime: string | null
  lastOnline: string
  location: string
  installDate: string
  alertRule: string
}

export interface DeviceSaveDTO {
  deviceSn: string
  deviceName: string
  deviceType?: string
  location?: string
  installDate?: string
  alertRule?: string
}

export interface DeviceUpdateDTO {
  deviceName?: string
  deviceType?: string
  alertRule?: string
  location?: string
}

export interface DeviceBindDTO {
  deviceId: string
  elderId: string
}

export interface DeviceQueryDTO {
  page?: number
  size?: number
  keyword?: string
  deviceType?: string
  status?: string
  bindStatus?: string
}

export interface DeviceStatsVO {
  total: number
  online: number
  offline: number
  fault: number
  bindRate: number
  todayAlert: number
}

// ===== 设备列表（分页） =====
export function getDeviceList(params: DeviceQueryDTO) {
  return request.get<Result<PageVO<DeviceVO>>>('/device/list', { params })
}

// ===== 新增设备 =====
export function addDevice(data: DeviceSaveDTO) {
  return request.post<Result<DeviceVO>>('/device', data)
}

// ===== 编辑设备 =====
export function updateDevice(id: number, data: DeviceUpdateDTO) {
  return request.put<Result<DeviceVO>>(`/device/${id}`, data)
}

// ===== 删除设备 =====
export function deleteDevice(id: number) {
  return request.delete<Result<DeviceVO>>(`/device/${id}`)
}

// ===== 绑定设备到老人 =====
export function bindDevice(data: DeviceBindDTO) {
  return request.post<Result<DeviceVO>>('/device/bind', data)
}

// ===== 解绑设备 =====
export function unbindDevice(deviceId: number) {
  return request.post<Result<DeviceVO>>('/device/unbind', null, {
    params: { deviceId },
  })
}
