<template>
  <div class="expert-workbench">
    <!-- 欢迎卡片 -->
    <div class="welcome-card">
      <div class="welcome-title">欢迎回来，{{ wb.expertName || '专家' }}</div>
      <div class="welcome-sub">{{ wb.title || '老年健康服务专家' }}</div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon blue"><el-icon><ChatDotRound /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ wb.todayCount ?? '-' }}</div>
          <div class="stat-label">今日咨询</div>
        </div>
      </div>
      <div class="stat-card orange">
        <div class="stat-icon"><el-icon><Bell /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ wb.pendingCount ?? '-' }}</div>
          <div class="stat-label">待回复</div>
        </div>
      </div>
      <div class="stat-card green">
        <div class="stat-icon"><el-icon><DataAnalysis /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ wb.totalCount ?? '-' }}</div>
          <div class="stat-label">累计咨询</div>
        </div>
      </div>
      <div class="stat-card red">
        <div class="stat-icon"><el-icon><Star /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ wb.rating ?? '-' }}</div>
          <div class="stat-label">满意度</div>
        </div>
      </div>
    </div>

    <!-- 咨询趋势折线图 -->
    <div class="chart-card">
      <div class="chart-title">近 7 天咨询趋势</div>
      <div ref="chartRef" class="chart"></div>
    </div>

    <!-- 快速操作（大卡片） -->
    <div class="quick-actions">
      <div class="quick-title">快速操作</div>
      <div class="quick-grid">
        <div v-for="q in quickActions" :key="q.path" class="quick-item" @click="router.push(q.path)">
          <el-icon><component :is="q.icon" /></el-icon>
          <span>{{ q.label }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { getExpertWorkbench, type ExpertWorkbench } from '@/api/expertPortal'

const router = useRouter()
const wb = ref<ExpertWorkbench>({})
const chartRef = ref<HTMLDivElement>()
let chart: echarts.ECharts | null = null

const quickActions = [
  { icon: 'ChatDotRound', label: '我的咨询', path: '/expert/consult' },
  { icon: 'Calendar', label: '排班请假', path: '/expert/schedule' },
  { icon: 'User', label: '老人健康档案', path: '/expert/elder' },
  { icon: 'Tools', label: '干预方案建议', path: '/expert/intervention' },
  { icon: 'Avatar', label: '个人中心', path: '/expert/profile' },
]

function renderChart() {
  const trend = wb.value.trend || []
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: trend.map((t) => t.date), boundaryGap: false },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        name: '咨询量',
        type: 'line',
        smooth: true,
        data: trend.map((t) => t.count),
        itemStyle: { color: '#409eff' },
        areaStyle: { opacity: 0.12 },
      },
    ],
  })
}

function handleResize() {
  chart?.resize()
}

onMounted(async () => {
  try {
    const res = await getExpertWorkbench()
    if (res.data.code === 200) {
      wb.value = res.data.data || {}
      await nextTick()
      renderChart()
    }
  } catch (e) {
    console.warn('加载工作台失败', e)
  }
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
</script>

<style scoped>
.expert-workbench {
  padding: 16px 20px;
  background: #f0f2f5;
  min-height: 100%;
}
.welcome-card {
  background: linear-gradient(120deg, #409eff, #36cfc9);
  color: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 16px;
}
.welcome-title {
  font-size: 22px;
  font-weight: 600;
}
.welcome-sub {
  margin-top: 6px;
  font-size: 14px;
  opacity: 0.9;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}
.stat-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
}
.stat-icon.blue {
  background: #e6f7ff;
  color: #409eff;
}
.stat-icon.orange {
  background: #fdf6ec;
  color: #e6a23c;
}
.stat-icon.green {
  background: #f0f9eb;
  color: #67c23a;
}
.stat-icon.red {
  background: #fef0f0;
  color: #f56c6c;
}
.stat-info {
  flex: 1;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.2;
}
.stat-label {
  font-size: 14px;
  color: #999;
}

.chart-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}
.chart {
  width: 100%;
  height: 260px;
}

.quick-actions {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.quick-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}
.quick-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}
.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 22px 12px;
  background: #f8f9fa;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
}
.quick-item:hover {
  border-color: #409eff;
  background: #e6f7ff;
  transform: translateY(-2px);
}
.quick-item .el-icon {
  font-size: 30px;
  color: #409eff;
}
.quick-item span {
  font-size: 14px;
  color: #606266;
}

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
  .quick-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
