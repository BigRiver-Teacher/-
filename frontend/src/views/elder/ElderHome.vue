<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getElderHealth } from '@/api/elderPortal'
import type { ElderHealth } from '@/api/elderPortal'

const elderName = ref('')
const health = ref<ElderHealth | null>(null)
const loading = ref(false)

// 指标卡片配置
const CARDS = [
  { key: 'BLOOD_PRESSURE', label: '血压', icon: '❤️', unit: 'mmHg' },
  { key: 'HEART_RATE', label: '心率', icon: '💓', unit: 'bpm' },
  { key: 'BLOOD_SUGAR', label: '血糖', icon: '🩸', unit: 'mmol/L' },
  { key: 'BLOOD_OXYGEN', label: '血氧', icon: '🫁', unit: '%' },
  { key: 'TEMPERATURE', label: '体温', icon: '🌡️', unit: '℃' },
]

function cardValue(key: string): string {
  const l = health.value?.latest?.[key]
  if (!l) return '-'
  if (key === 'BLOOD_PRESSURE') return `${l.systolic}/${l.diastolic}`
  return `${l.value}`
}
function cardTime(key: string): string {
  return health.value?.latest?.[key]?.time || ''
}

// ===== 血压趋势图 =====
const bpChartRef = ref<HTMLDivElement>()
let bpChart: echarts.ECharts | null = null

function renderBpChart() {
  const trend = health.value?.trend?.BLOOD_PRESSURE || []
  if (!bpChartRef.value) return
  if (!bpChart) bpChart = echarts.init(bpChartRef.value)
  bpChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['收缩压', '舒张压'] },
    grid: { left: 50, right: 20, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: trend.map((p) => p.time), boundaryGap: false },
    yAxis: { type: 'value', min: 40, max: 180 },
    series: [
      { name: '收缩压', type: 'line', smooth: true, data: trend.map((p) => p.systolic), itemStyle: { color: '#f56c6c' }, areaStyle: { opacity: 0.1 } },
      { name: '舒张压', type: 'line', smooth: true, data: trend.map((p) => p.diastolic), itemStyle: { color: '#409eff' } },
    ],
  })
}

// ===== 单值指标趋势图（心率/血糖/血氧/体温 tab 切换） =====
const singleChartRef = ref<HTMLDivElement>()
let singleChart: echarts.ECharts | null = null
const singleKey = ref('HEART_RATE')
const SINGLE_OPTS = [
  { key: 'HEART_RATE', label: '心率', unit: 'bpm' },
  { key: 'BLOOD_SUGAR', label: '血糖', unit: 'mmol/L' },
  { key: 'BLOOD_OXYGEN', label: '血氧', unit: '%' },
  { key: 'TEMPERATURE', label: '体温', unit: '℃' },
]

function renderSingleChart() {
  const opt = SINGLE_OPTS.find((o) => o.key === singleKey.value)
  const trend = health.value?.trend?.[singleKey.value] || []
  if (!singleChartRef.value) return
  if (!singleChart) singleChart = echarts.init(singleChartRef.value)
  singleChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: trend.map((p) => p.time), boundaryGap: false },
    yAxis: { type: 'value', scale: true },
    series: [
      {
        name: opt?.label || '',
        type: 'line',
        smooth: true,
        data: trend.map((p) => p.value),
        itemStyle: { color: '#52c41a' },
        areaStyle: { opacity: 0.12 },
      },
    ],
  })
}

async function load() {
  loading.value = true
  try {
    const res = await getElderHealth()
    if (res.data.code === 200) {
      health.value = res.data.data || null
      elderName.value = res.data.data?.elderName || ''
      await nextTick()
      renderBpChart()
      renderSingleChart()
    }
  } catch (e) {
    ElMessage.error('加载健康数据失败')
  } finally {
    loading.value = false
  }
}

function switchSingle(k: string) {
  singleKey.value = k
  nextTick(() => renderSingleChart())
}

function handleResize() {
  bpChart?.resize()
  singleChart?.resize()
}

onMounted(() => {
  load()
  window.addEventListener('resize', handleResize)
})
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  bpChart?.dispose()
  singleChart?.dispose()
})
</script>

<template>
  <div class="elder-home" v-loading="loading">
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><HomeFilled /></el-icon>
        首页健康概览
      </h2>
    </div>

    <div class="welcome">
      <div class="welcome-title">您好，{{ elderName || '老人' }} 的健康概览</div>
      <div class="welcome-sub">近 7 天健康数据趋势（模拟数据）</div>
    </div>

    <!-- 指标卡片 -->
    <div class="cards">
      <div v-for="c in CARDS" :key="c.key" class="metric-card">
        <div class="metric-icon">{{ c.icon }}</div>
        <div class="metric-label">{{ c.label }}</div>
        <div class="metric-value">{{ cardValue(c.key) }} <span class="metric-unit">{{ c.unit }}</span></div>
        <div class="metric-time">{{ cardTime(c.key) }}</div>
      </div>
    </div>

    <!-- 血压趋势 -->
    <div class="chart-card">
      <div class="chart-title">血压趋势（近 7 天）</div>
      <div ref="bpChartRef" class="chart"></div>
    </div>

    <!-- 单值指标趋势 -->
    <div class="chart-card">
      <div class="chart-head">
        <div class="chart-title">其他指标趋势</div>
        <el-radio-group v-model="singleKey" size="small" @change="switchSingle">
          <el-radio-button v-for="o in SINGLE_OPTS" :key="o.key" :value="o.key">{{ o.label }}</el-radio-button>
        </el-radio-group>
      </div>
      <div ref="singleChartRef" class="chart"></div>
    </div>
  </div>
</template>

<style scoped>
.elder-home {
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
.welcome {
  background: linear-gradient(120deg, #5aa9f7, #2f7fe0);
  color: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 16px;
}
.welcome-title {
  font-size: 20px;
  font-weight: 600;
}
.welcome-sub {
  margin-top: 4px;
  font-size: 13px;
  opacity: 0.9;
}
.cards {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
  margin-bottom: 16px;
}
.metric-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  text-align: center;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.06);
}
.metric-icon {
  font-size: 24px;
}
.metric-label {
  margin-top: 6px;
  color: #909399;
  font-size: 13px;
}
.metric-value {
  margin-top: 6px;
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}
.metric-unit {
  font-size: 12px;
  color: #909399;
  font-weight: 400;
}
.metric-time {
  margin-top: 6px;
  font-size: 11px;
  color: #c0c4cc;
}
.chart-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.06);
}
.chart-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 8px;
}
.chart-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.chart {
  width: 100%;
  height: 260px;
}
</style>
