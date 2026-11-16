<template>
  <div class="management-page">
    <div class="page-header">
      <div class="page-title">
        <span class="title-icon">📊</span><span class="title-text">管理主页</span>
      </div>
      <div class="page-subtitle">欢迎来到朝夕护行管理系统</div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div
        v-for="card in visibleStats"
        :key="card.id"
        :style="{ gridColumn: `span ${card.colspan}` }"
      >
        <StatsCard
          :icon-component="card.icon"
          :value="card.value"
          :label="card.name"
          :icon-bg-color="card.color"
          icon-color="#fff"
          :change-value="card.changeValue"
          :change-type="card.changeType"
          :change-label="card.changeLabel"
        />
      </div>
    </div>

    <!-- 快速跳转 + 欢迎卡片 -->
    <div class="quick-section">
      <div class="quick-left">
        <div class="quick-title">快速跳转</div>
        <div class="quick-grid">
          <div
            v-for="item in quickLinks"
            :key="item.path"
            class="quick-item"
            @click="goTo(item.path)"
          >
            <span class="quick-name">{{ item.name }}</span>
          </div>
        </div>
      </div>
      <div class="quick-right">
        <div class="welcome-card">
          <div class="welcome-avatar"><el-avatar :size="48" :src="userPic" /></div>
          <div class="welcome-info">
            <div class="welcome-text">欢迎您登入</div>
            <div class="welcome-name">{{ userDisplayName }}</div>
            <div class="welcome-time">角色：{{ userRole }}</div>
          </div>
          <el-button type="primary" size="small" @click="goToProfile">进入个人主页</el-button>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="chart-section">
      <div class="chart-row">
        <div class="chart-box">
          <div class="chart-header">
            <span class="chart-title">预警数量趋势</span
            ><span class="chart-subtitle">近7天预警数量变化</span>
          </div>
          <div ref="trendChartRef" class="chart-container"></div>
        </div>
        <div class="chart-box">
          <div class="chart-header">
            <span class="chart-title">设备在线率</span
            ><span class="chart-subtitle">设备在线/离线/故障分布</span>
          </div>
          <div ref="deviceChartRef" class="chart-container"></div>
        </div>
      </div>
      <div class="chart-row">
        <div class="chart-box">
          <div class="chart-header">
            <span class="chart-title">设备趋势</span
            ><span class="chart-subtitle">设备在线/告警趋势</span>
          </div>
          <div ref="elderChartRef" class="chart-container"></div>
        </div>
        <div class="chart-box">
          <div class="chart-header">
            <span class="chart-title">最近活动</span
            ><span class="chart-subtitle">最新通知消息</span>
          </div>
          <div class="activity-list">
            <div v-for="item in recentActivities" :key="item.id" class="activity-item">
              <div
                class="activity-dot"
                :class="
                  item.type === 'warning' ? 'danger' : item.type === 'notice' ? 'info' : 'success'
                "
              ></div>
              <div class="activity-content">
                <span class="activity-name">{{ item.title }}</span>
                <span class="activity-desc"
                  >{{ item.content?.substring(0, 30)
                  }}{{ item.content?.length > 30 ? '...' : '' }}</span
                >
              </div>
              <span class="activity-time">{{ item.time }}</span>
            </div>
            <div v-if="recentActivities.length === 0" class="activity-empty">暂无活动记录</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import {
  User,
  Monitor,
  Connection,
  Warning,
  Document,
  Timer,
  CircleCheck,
  Clock,
  StarFilled,
} from '@element-plus/icons-vue'
import { useDashboardStore } from '@/stores/dashboard'
import { useStatsStore } from '@/stores/statsStore'
import { useUserStore } from '@/stores/user'
import { useNotificationStore } from '@/stores/notificationStore'
import StatsCard from '@/components/StatsCard.vue'

const router = useRouter()
const dashboardStore = useDashboardStore()
const statsStore = useStatsStore()
const userStore = useUserStore()
const notificationStore = useNotificationStore()

const userPic = 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'

const iconComponents: Record<string, any> = {
  User,
  Monitor,
  Connection,
  Warning,
  Document,
  Timer,
  CircleCheck,
  Clock,
  StarFilled,
}

const userDisplayName = computed(
  () => userStore.userInfo?.realName || userStore.userInfo?.username || '管理员',
)
const userRole = computed(() => userStore.userInfo?.role || '系统管理员')

// ===== 统计卡片 —— 优先来自 statsStore，fallback 到 dashboardStore =====
const visibleStats = computed(() => {
  const statsCards = statsStore.managementStats
  if (statsCards.length > 0) {
    return statsCards.map((card) => ({
      ...card,
      colspan: 1,
      icon: iconComponents[card.icon || 'Document'] || Document,
    }))
  }
  return dashboardStore.visibleCards.map((card) => ({
    id: card.id,
    name: card.name,
    value: card.value,
    colspan: card.colspan,
    color: card.color,
    changeValue: undefined,
    changeType: undefined,
    changeLabel: undefined,
    icon: iconComponents[card.icon] || Document,
  }))
})

// ===== 最近活动（来自通知列表） =====
const recentActivities = computed(() => {
  return notificationStore.notifications.slice(0, 8)
})

const quickLinks = [
  { name: '老人详情', path: '/home/elder/base' },
  { name: '跌倒预警', path: '/home/warning' },
  { name: '专家咨询', path: '/home/expert' },
  { name: '个人主页', path: '/home/profile' },
  { name: '设备管理', path: '/home/device' },
  { name: '人员管理', path: '/home/caregiver' },
]

function goTo(path: string) {
  router.push(path)
}
function goToProfile() {
  router.push('/home/profile')
}

// ===== 图表 =====
const trendChartRef = ref<HTMLElement | null>(null)
const deviceChartRef = ref<HTMLElement | null>(null)
const elderChartRef = ref<HTMLElement | null>(null)
let trendChartInstance: echarts.ECharts | null = null
let deviceChartInstance: echarts.ECharts | null = null
let elderChartInstance: echarts.ECharts | null = null

function createChartInstance(
  container: HTMLElement | null,
  existing: echarts.ECharts | null,
  option: EChartsOption,
): echarts.ECharts | null {
  if (!container) return null
  existing?.dispose()
  const instance = echarts.init(container)
  instance.setOption(option)
  return instance
}

function getTrendOption(): EChartsOption {
  const trend = statsStore.deviceAlertTrend
  if (trend && trend.categories?.length > 0) {
    return {
      tooltip: {
        trigger: 'axis',
        formatter: (p: any) => `${p[0].axisValue}<br/>告警：${p[0].value} 条`,
      },
      grid: { left: 40, right: 20, top: 20, bottom: 30 },
      xAxis: {
        type: 'category',
        data: trend.categories,
        axisLabel: { fontSize: 12, color: '#999' },
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } },
        axisLabel: { fontSize: 12, color: '#999' },
      },
      series: [
        {
          type: 'line',
          data: trend.series[0]?.data || [],
          smooth: true,
          symbol: 'circle',
          symbolSize: 8,
          lineStyle: { color: '#f56c6c', width: 3 },
          itemStyle: { color: '#f56c6c' },
          areaStyle: { color: 'rgba(245,108,108,0.15)' },
        },
      ],
    }
  }
  // fallback
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: {
      type: 'category',
      data: ['--', '--', '--', '--', '--', '--', '--'],
      axisLabel: { fontSize: 12 },
    },
    yAxis: { type: 'value' },
    series: [
      {
        type: 'line',
        data: [0, 0, 0, 0, 0, 0, 0],
        smooth: true,
        lineStyle: { color: '#f56c6c', width: 3 },
      },
    ],
  }
}

function getDeviceOption(): EChartsOption {
  // 设备在线率：在线/离线/故障分布饼图
  const stats = statsStore.deviceStats
  return {
    tooltip: { trigger: 'item', formatter: '{b}: {c}台 ({d}%)' },
    color: ['#67c23a', '#909399', '#f56c6c'],
    legend: { orient: 'vertical', right: 20, top: 'center' },
    series: [
      {
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['40%', '50%'],
        data: [
          { name: '在线', value: stats?.onlineDevices || 0 },
          { name: '离线', value: stats?.offlineDevices || 0 },
          { name: '故障', value: stats?.faultDevices || 0 },
        ],
      },
    ],
  }
}

function getElderOption(): EChartsOption {
  // 设备趋势：在线设备（折线）+ 告警数量（柱状），按日期合并
  const online = statsStore.deviceOnlineTrend
  const alertTrend = statsStore.deviceAlertTrend
  if (online && online.categories?.length > 0) {
    const cats: string[] = [...(online.categories || [])]
    ;(alertTrend?.categories || []).forEach((x: string) => {
      if (!cats.includes(x)) cats.push(x)
    })
    cats.sort()
    const pick = (
      list:
        | { categories?: string[]; series?: Array<{ data?: Array<number | string> }> }
        | null
        | undefined,
      c: string,
    ) => {
      const idx = list?.categories?.indexOf(c) ?? -1
      return idx >= 0 ? (list?.series?.[0]?.data?.[idx] ?? 0) : 0
    }
    const onlineData = cats.map((c2) => pick(online, c2))
    const alertData = cats.map((c2) => pick(alertTrend, c2))
    return {
      tooltip: { trigger: 'axis' },
      legend: { data: ['在线设备', '告警数量'], bottom: 0 },
      grid: { left: 50, right: 20, top: 20, bottom: 40 },
      xAxis: { type: 'category', data: cats, axisLabel: { fontSize: 12, color: '#999' } },
      yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } } },
      series: [
        {
          name: '在线设备',
          type: 'line',
          data: onlineData,
          smooth: true,
          lineStyle: { color: '#409eff', width: 3 },
          itemStyle: { color: '#409eff' },
        },
        {
          name: '告警数量',
          type: 'bar',
          data: alertData,
          itemStyle: { color: '#e6a23c', borderRadius: [4, 4, 0, 0] },
        },
      ],
    }
  }
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: {
      type: 'category',
      data: ['--', '--', '--', '--', '--', '--', '--'],
      axisLabel: { fontSize: 12 },
    },
    yAxis: { type: 'value' },
    series: [
      {
        type: 'bar',
        data: [0, 0, 0, 0, 0, 0, 0],
        barWidth: '40%',
        itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] },
      },
    ],
  }
}

function initCharts() {
  trendChartInstance = createChartInstance(
    trendChartRef.value,
    trendChartInstance,
    getTrendOption(),
  )
  deviceChartInstance = createChartInstance(
    deviceChartRef.value,
    deviceChartInstance,
    getDeviceOption(),
  )
  elderChartInstance = createChartInstance(
    elderChartRef.value,
    elderChartInstance,
    getElderOption(),
  )
}

function resizeCharts() {
  trendChartInstance?.resize()
  deviceChartInstance?.resize()
  elderChartInstance?.resize()
}

onMounted(async () => {
  dashboardStore.loadConfig()
  // 并行加载所有数据
  await Promise.all([
    userStore.fetchUserInfo(),
    statsStore.fetchManagementStats(),
    statsStore.fetchDeviceTrends(),
    notificationStore.fetchNotifications({ pageNum: 1, pageSize: 10 }),
  ])
  await nextTick(() => initCharts())
  window.addEventListener('resize', resizeCharts, { passive: true } as AddEventListenerOptions)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts, { passive: true } as AddEventListenerOptions)
  trendChartInstance?.dispose()
  deviceChartInstance?.dispose()
  elderChartInstance?.dispose()
})
</script>

<style scoped>
.management-page {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: 100%;
}
.page-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 20px;
}
.page-title {
  display: flex;
  align-items: center;
  gap: 10px;
}
.title-icon {
  font-size: 24px;
}
.title-text {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a1a;
}
.page-subtitle {
  font-size: 14px;
  color: #999;
  padding-left: 34px;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.quick-section {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}
.quick-left {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.quick-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}
.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}
.quick-item {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
  height: 40px;
}
.quick-item:hover {
  background: #e6f7ff;
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}
.quick-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}
.quick-item:hover .quick-name {
  color: #409eff;
}
.quick-right {
  width: 280px;
  flex-shrink: 0;
}
.welcome-card {
  background: linear-gradient(135deg, #409eff, #337ecc);
  border-radius: 12px;
  padding: 20px 24px;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.3);
}
.welcome-avatar {
  margin-bottom: 8px;
}
.welcome-avatar :deep(.el-avatar) {
  border: 2px solid rgba(255, 255, 255, 0.4);
}
.welcome-text {
  font-size: 14px;
  opacity: 0.85;
}
.welcome-name {
  font-size: 18px;
  font-weight: 600;
  margin: 2px 0;
}
.welcome-time {
  font-size: 12px;
  opacity: 0.7;
  margin-bottom: 8px;
}
.welcome-card .el-button {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #fff;
  border-radius: 8px;
  padding: 6px 16px;
}
.welcome-card .el-button:hover {
  background: rgba(255, 255, 255, 0.3);
}
.chart-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.chart-box {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}
.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}
.chart-subtitle {
  font-size: 12px;
  color: #999;
}
.chart-container {
  width: 100%;
  height: 220px;
  touch-action: pan-y;
}
.activity-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 4px 0;
}
.activity-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: background 0.2s;
}
.activity-item:hover {
  background: #f0f2f5;
}
.activity-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.activity-dot.danger {
  background: #f56c6c;
}
.activity-dot.warning {
  background: #e6a23c;
}
.activity-dot.info {
  background: #409eff;
}
.activity-dot.success {
  background: #67c23a;
}
.activity-content {
  flex: 1;
  display: flex;
  gap: 6px;
  align-items: center;
  min-width: 0;
}
.activity-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  white-space: nowrap;
}
.activity-desc {
  font-size: 13px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.activity-time {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
}
.activity-empty {
  text-align: center;
  color: #999;
  font-size: 14px;
  padding: 20px 0;
}
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .quick-section {
    flex-direction: column;
  }
  .quick-right {
    width: 100%;
  }
  .welcome-card {
    flex-direction: row;
    gap: 16px;
    padding: 16px 20px;
    justify-content: space-between;
  }
  .welcome-avatar {
    margin-bottom: 0;
  }
}
@media (max-width: 992px) {
  .chart-row {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  .quick-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .management-page {
    padding: 12px;
  }
  .welcome-card {
    flex-direction: column;
    text-align: center;
  }
  .chart-container {
    height: 180px;
  }
}
@media (max-width: 480px) {
  .quick-grid {
    grid-template-columns: 1fr;
  }
  .title-text {
    font-size: 18px;
  }
  .page-subtitle {
    font-size: 13px;
    padding-left: 0;
  }
  .chart-container {
    height: 160px;
  }
  .activity-item {
    flex-wrap: wrap;
    gap: 4px;
  }
  .activity-time {
    margin-left: 18px;
  }
}
</style>
