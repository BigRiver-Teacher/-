<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getSupervisorTeam } from '@/api/supervisorPortal'
import type { TeamMember } from '@/api/supervisorPortal'
import StatsCard from '@/components/StatsCard.vue'

const community = ref('')
const supervisor = ref('')
const members = ref<TeamMember[]>([])
const loading = ref(false)

const barRef = ref<HTMLDivElement>()
const pieRef = ref<HTMLDivElement>()
let barChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

const positionText = (p?: string) =>
  ({ SENIOR_CAREGIVER: '高级护工', CAREGIVER: '护工', NURSING_SUPERVISOR: '护理主管', REHAB_NURSE: '康复护士', PSYCHO_COUNSELOR: '心理辅导员' }[p as string] || p || '')

// ===== 概览统计 =====
const stats = computed(() => {
  const total = members.value.length
  const totalElders = members.value.reduce((s, m) => s + (m.elderCount || 0), 0)
  const empty = members.value.filter((m) => (m.elderCount || 0) === 0).length
  const avg = total ? (totalElders / total).toFixed(1) : '0'
  return [
    { iconComponent: 'User', value: total, label: '组内护工', iconBgColor: '#e6f7ff', iconColor: '#409eff' },
    { iconComponent: 'UserFilled', value: totalElders, label: '覆盖老人', iconBgColor: '#f0f9eb', iconColor: '#67c23a' },
    { iconComponent: 'DataAnalysis', value: avg, label: '人均负责(位)', iconBgColor: '#fdf6ec', iconColor: '#e6a23c' },
    { iconComponent: 'Warning', value: empty, label: '空岗护工', iconBgColor: '#fef0f0', iconColor: '#f56c6c' },
  ]
})

function renderCharts() {
  // 护工工作量柱状图
  if (barRef.value) {
    if (!barChart) barChart = echarts.init(barRef.value)
    barChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 20, top: 30, bottom: 40 },
      xAxis: {
        type: 'category',
        data: members.value.map((m) => m.name),
        axisLabel: { interval: 0, rotate: members.value.length > 5 ? 20 : 0 },
      },
      yAxis: { type: 'value', minInterval: 1 },
      series: [
        {
          name: '负责老人数',
          type: 'bar',
          barWidth: '45%',
          data: members.value.map((m) => m.elderCount || 0),
          itemStyle: { color: '#409eff', borderRadius: [6, 6, 0, 0] },
        },
      ],
    })
  }
  // 老人分布饼图（有负责老人 vs 空岗）
  if (pieRef.value) {
    if (!pieChart) pieChart = echarts.init(pieRef.value)
    const busy = members.value.filter((m) => (m.elderCount || 0) > 0).length
    const empty = members.value.filter((m) => (m.elderCount || 0) === 0).length
    pieChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [
        {
          name: '护工分布',
          type: 'pie',
          radius: ['45%', '70%'],
          center: ['50%', '45%'],
          data: [
            { name: '有负责老人', value: busy, itemStyle: { color: '#409eff' } },
            { name: '空岗', value: empty, itemStyle: { color: '#f56c6c' } },
          ],
          label: { formatter: '{b}: {c} 人' },
        },
      ],
    })
  }
}

function handleResize() {
  barChart?.resize()
  pieChart?.resize()
}

async function load() {
  loading.value = true
  try {
    const res = await getSupervisorTeam()
    community.value = res.data.data?.community || ''
    supervisor.value = res.data.data?.supervisor || ''
    members.value = res.data.data?.members || []
    await nextTick()
    renderCharts()
  } catch {
    ElMessage.error('加载全组信息失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  load()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  barChart?.dispose()
  pieChart?.dispose()
})
</script>

<template>
  <div class="supervisor-team">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><UserFilled /></el-icon>
        全组视角
      </h2>
      <el-button :loading="loading" @click="load">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- 概览卡片 -->
    <div class="stats-grid">
      <StatsCard v-for="(s, i) in stats" :key="i" v-bind="s" />
    </div>

    <!-- 图表区 -->
    <div class="charts-row">
      <div class="chart-card">
        <div class="chart-title"><el-icon><Histogram /></el-icon>护工工作量</div>
        <div ref="barRef" class="chart"></div>
      </div>
      <div class="chart-card">
        <div class="chart-title"><el-icon><PieChart /></el-icon>护工分布</div>
        <div ref="pieRef" class="chart"></div>
      </div>
    </div>

    <!-- 护工卡片 -->
    <div class="member-section">
      <div class="section-title">
        <el-icon><Grid /></el-icon>
        <span>组内护工 · {{ community || '本社区' }}（共 {{ members.length }} 人）</span>
      </div>
      <el-empty v-if="!loading && members.length === 0" description="本社区暂无护工" :image-size="100" />
      <el-row v-else :gutter="16" v-loading="loading">
        <el-col v-for="m in members" :key="m.id" :xs="24" :sm="12" :md="8" :lg="6" class="col">
          <el-card shadow="hover" class="member-card">
            <div class="member-head">
              <el-avatar :size="40">{{ m.name.charAt(0) }}</el-avatar>
              <div class="member-info">
                <div class="member-name">{{ m.name }}</div>
                <div class="member-pos">{{ positionText(m.position) }} · {{ m.phone }}</div>
              </div>
            </div>
            <div class="member-count">负责老人 <b>{{ m.elderCount }}</b> 位</div>
            <div class="elder-tags">
              <el-tag v-for="e in m.elders" :key="e" size="small" type="info" class="tag">{{ e }}</el-tag>
              <span v-if="m.elders.length === 0" class="empty">暂无负责老人</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<style scoped>
.supervisor-team {
  padding: 20px;
  background: #f0f2f5;
  min-height: 100%;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}
.page-title .el-icon {
  font-size: 26px;
  color: #409eff;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.charts-row {
  display: grid;
  grid-template-columns: 3fr 2fr;
  gap: 16px;
  margin-bottom: 20px;
}
.chart-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.chart-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}
.chart-title .el-icon {
  font-size: 18px;
  color: #409eff;
}
.chart {
  width: 100%;
  height: 260px;
}
.member-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 16px;
}
.section-title .el-icon {
  font-size: 18px;
  color: #409eff;
}
.col {
  margin-bottom: 16px;
}
.member-card {
  height: 100%;
}
.member-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.member-name {
  font-weight: 700;
  font-size: 15px;
}
.member-pos {
  font-size: 12px;
  color: #909399;
}
.member-count {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
}
.member-count b {
  color: #409eff;
  font-size: 16px;
}
.elder-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.tag {
  margin: 0;
}
.empty {
  color: #c0c4cc;
  font-size: 12px;
}
@media (max-width: 992px) {
  .charts-row {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
