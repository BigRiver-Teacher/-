// stores/statsStore.ts — 各页面概况卡片数据管理
import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  getDeviceStatistics,
  getExpertStatistics,
  getPersonTrend,
  getDeviceAlertTrend,
  getDeviceOnlineTrend,
  type EchartDeviceStatDTO,
  type EchartTrendDTO,
} from '@/api/stats'

// ===== 类型定义 =====
export interface StatItem {
  id: string
  name: string
  value: number | string
  changeValue?: string
  changeType?: 'up' | 'down'
  changeLabel?: string
  icon?: string
  color?: string
}

export const useStatsStore = defineStore('statsStore', () => {
  // ===== State =====
  const managementStats = ref<StatItem[]>([])
  const profileStats = ref<StatItem[]>([])
  const overviewStats = ref<StatItem[]>([])
  const loading = ref(false)

  // ===== 图表数据状态 =====
  const deviceStats = ref<EchartDeviceStatDTO | null>(null)
  const deviceAlertTrend = ref<EchartTrendDTO | null>(null)
  const deviceOnlineTrend = ref<EchartTrendDTO | null>(null)

  // ===== Actions =====

  /** 加载管理主页统计数据 */
  async function fetchManagementStats(): Promise<void> {
    loading.value = true
    try {
      const [deviceRes, expertRes, personRes] = await Promise.allSettled([
        getDeviceStatistics(),
        getExpertStatistics(),
        getPersonTrend(),
      ])

      const statsData = deviceRes.status === 'fulfilled' ? deviceRes.value.data.data : null
      const expertStats = expertRes.status === 'fulfilled' ? expertRes.value.data.data : null

      // 保存设备统计数据供图表使用
      if (statsData) {
        deviceStats.value = statsData
      }

      managementStats.value = [
        {
          id: 'm1',
          name: '设备总数',
          value: statsData?.totalDevices ?? 0,
          changeType: 'up',
          changeLabel: '运行中',
          icon: 'Monitor',
          color: '#409eff',
        },
        {
          id: 'm2',
          name: '在线设备',
          value: statsData?.onlineDevices ?? 0,
          changeType: 'up',
          changeLabel: '当前在线',
          icon: 'Connection',
          color: '#67c23a',
        },
        {
          id: 'm3',
          name: '设备绑定率',
          value: `${statsData?.bindRate ?? 0}%`,
          changeType: 'up',
          changeLabel: '已绑定老人',
          icon: 'Link',
          color: '#409eff',
        },
        {
          id: 'm4',
          name: '今日告警',
          value: statsData?.todayAlertCount ?? 0,
          changeType: 'down',
          changeLabel: '需及时处理',
          icon: 'Warning',
          color: '#f56c6c',
        },
        {
          id: 'm5',
          name: '专家人数',
          value: expertStats?.totalExperts ?? 0,
          changeType: 'up',
          changeLabel: '提供咨询服务',
          icon: 'StarFilled',
          color: '#e6a23c',
        },
      ]
    } finally {
      loading.value = false
    }
  }

  /** 加载设备趋势数据（供管理主页图表使用） */
  async function fetchDeviceTrends(): Promise<void> {
    try {
      const [alertRes, trendRes] = await Promise.allSettled([
        getDeviceAlertTrend(),
        getDeviceOnlineTrend(),
      ])
      if (alertRes.status === 'fulfilled') {
        deviceAlertTrend.value = alertRes.value.data.data ?? null
      }
      if (trendRes.status === 'fulfilled') {
        deviceOnlineTrend.value = trendRes.value.data.data ?? null
      }
    } catch {
      // 趋势数据加载失败不影响主流程
    }
  }

  /** 加载个人主页统计数据（从后端获取真实数据） */
  async function fetchProfileStats(): Promise<void> {
    try {
      const [deviceRes, expertRes] = await Promise.allSettled([
        getDeviceStatistics(),
        getExpertStatistics(),
      ])
      const deviceStats = deviceRes.status === 'fulfilled' ? deviceRes.value.data.data : null
      const expertStats = expertRes.status === 'fulfilled' ? expertRes.value.data.data : null

      profileStats.value = [
        { id: 'p1', name: '发布通知', value: '--', icon: 'Notification', color: '#409eff' },
        { id: 'p2', name: '管理设备', value: deviceStats?.totalDevices ?? '--', icon: 'Monitor', color: '#67c23a' },
        { id: 'p3', name: '老人数量', value: '--', icon: 'User', color: '#e6a23c' },
        { id: 'p4', name: '处理预警', value: '--', icon: 'Warning', color: '#f56c6c' },
      ]
    } catch {
      profileStats.value = [
        { id: 'p1', name: '发布通知', value: '--', icon: 'Notification', color: '#409eff' },
        { id: 'p2', name: '管理设备', value: '--', icon: 'Monitor', color: '#67c23a' },
        { id: 'p3', name: '老人数量', value: '--', icon: 'User', color: '#e6a23c' },
        { id: 'p4', name: '处理预警', value: '--', icon: 'Warning', color: '#f56c6c' },
      ]
    }
  }

  /** 加载系统概览数据（从后端获取真实数据） */
  async function fetchOverviewStats(): Promise<void> {
    try {
      const [deviceRes] = await Promise.allSettled([getDeviceStatistics()])
      const deviceStats = deviceRes.status === 'fulfilled' ? deviceRes.value.data.data : null

      overviewStats.value = [
        { id: 'o1', name: '注册用户', value: '--', color: '#409eff' },
        { id: 'o2', name: '在线设备', value: deviceStats?.onlineDevices ?? '--', color: '#67c23a' },
        { id: 'o3', name: '待处理预警', value: deviceStats?.todayAlertCount ?? '--', color: '#e6a23c' },
        { id: 'o4', name: '生成报告', value: '--', color: '#f56c6c' },
      ]
    } catch {
      overviewStats.value = [
        { id: 'o1', name: '注册用户', value: '--', color: '#409eff' },
        { id: 'o2', name: '在线设备', value: '--', color: '#67c23a' },
        { id: 'o3', name: '待处理预警', value: '--', color: '#e6a23c' },
        { id: 'o4', name: '生成报告', value: '--', color: '#f56c6c' },
      ]
    }
  }

  return {
    // state
    managementStats,
    profileStats,
    overviewStats,
    loading,
    deviceStats,
    deviceAlertTrend,
    deviceOnlineTrend,
    // actions
    fetchManagementStats,
    fetchDeviceTrends,
    fetchProfileStats,
    fetchOverviewStats,
  }
})
