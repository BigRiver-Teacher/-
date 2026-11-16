// stores/deviceStore.ts — 传感设备数据管理
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  getDeviceList,
  addDevice,
  updateDevice,
  deleteDevice,
  bindDevice,
  unbindDevice,
  type DeviceVO,
  type DeviceSaveDTO,
  type DeviceUpdateDTO,
} from '@/api/device'
import { getDeviceStatistics } from '@/api/stats'

export type { DeviceVO as Device }

// 设备类型代码 <-> 中文显示
const DEVICE_TYPE_TEXT: Record<string, string> = {
  WEARABLE: '智能手环',
  CAMERA: '摄像头',
  SENSOR: '传感器',
  GATEWAY: '网关',
  LOCATOR: '定位器',
  智能手环: '智能手环',
  摄像头: '摄像头',
  传感器: '传感器',
  网关: '网关',
  定位器: '定位器',
}
const DEVICE_TYPE_CODE: Record<string, string> = {
  智能手环: 'WEARABLE',
  摄像头: 'CAMERA',
  传感器: 'SENSOR',
  网关: 'GATEWAY',
  定位器: 'LOCATOR',
}

// 设备统计数据类型
export interface DeviceStatsVO {
  total: number
  online: number
  offline: number
  fault: number
  bindRate: number
  todayAlert: number
}

export const useDeviceStore = defineStore('deviceStore', () => {
  // ===== State =====
  const deviceList = ref<DeviceVO[]>([])
  const total = ref(0)
  const loading = ref(false)
  const stats = ref<DeviceStatsVO>({
    total: 0,
    online: 0,
    offline: 0,
    fault: 0,
    bindRate: 0,
    todayAlert: 0,
  })

  // ===== Getters =====
  const onlineCount = computed(() => deviceList.value.filter((d) => d.status === 'online').length)
  const offlineCount = computed(() =>
    deviceList.value.filter((d) => d.status === 'offline' || d.status === 'fault').length,
  )
  const bindRate = computed(() => {
    if (deviceList.value.length === 0) return 0
    const bound = deviceList.value.filter((d) => d.bindElder).length
    return Math.round((bound / deviceList.value.length) * 100)
  })

  // ===== Actions =====

  /** 加载设备列表（分页） */
  async function fetchDevices(params?: {
    page?: number
    size?: number
    keyword?: string
    deviceType?: string
    status?: string
  }): Promise<void> {
    loading.value = true
    try {
      const res = await getDeviceList(params)
      const data = res.data.data
      deviceList.value = (data.list ?? []).map((d: any) => ({
        ...d,
        status: (d.status || '').toLowerCase(),
        deviceType: DEVICE_TYPE_TEXT[d.deviceType] || d.deviceType,
      }))
      total.value = data.total ?? 0
    } finally {
      loading.value = false
    }
  }

  /** 加载设备统计数据 */
  async function fetchStats(): Promise<void> {
    try {
      const res = await getDeviceStatistics()
      const d = res.data.data
      if (d) {
        stats.value = {
          total: d.totalDevices ?? 0,
          online: d.onlineDevices ?? 0,
          offline: d.offlineDevices ?? 0,
          fault: d.faultDevices ?? 0,
          bindRate: d.bindRate ?? 0,
          todayAlert: d.todayAlertCount ?? 0,
        }
      }
    } catch {
      // 统计加载失败不影响列表展示
    }
  }

  /** 获取单个设备 */
  function getDeviceById(id: number): DeviceVO | undefined {
    return deviceList.value.find((d) => d.id === id)
  }

  /** 新增设备 */
  async function createDevice(data: DeviceSaveDTO): Promise<void> {
    await addDevice({
      ...data,
      deviceType: DEVICE_TYPE_CODE[data.deviceType || ''] || data.deviceType,
    })
    await fetchDevices()
  }

  /** 编辑设备 */
  async function editDevice(id: number, data: DeviceUpdateDTO): Promise<void> {
    await updateDevice(id, data)
    await fetchDevices()
  }

  /** 删除设备 */
  async function removeDevice(id: number): Promise<void> {
    await deleteDevice(id)
    await fetchDevices()
  }

  /** 绑定设备到老人 */
  async function bindDeviceToElder(deviceId: string, elderId: string): Promise<void> {
    await bindDevice({ deviceId, elderId })
  }

  /** 解绑设备 */
  async function unbindDeviceFromElder(deviceId: number): Promise<void> {
    await unbindDevice(deviceId)
  }

  return {
    // state
    deviceList,
    total,
    loading,
    stats,
    // getters
    onlineCount,
    offlineCount,
    bindRate,
    // actions
    fetchDevices,
    fetchStats,
    getDeviceById,
    createDevice,
    editDevice,
    removeDevice,
    bindDeviceToElder,
    unbindDeviceFromElder,
  }
})
