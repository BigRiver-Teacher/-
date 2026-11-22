<template>
  <BigScreenShell active="device">
    <main class="bs-main bs-device">
      <!-- ===== KPI 指标带 ===== -->
      <div class="device-kpis">
        <div class="bs-kpi" style="--kpi-color: #38e1ff; --d: 0.04s">
          <div class="bs-kpi__label"><el-icon><Cpu /></el-icon>设备总数</div>
          <div class="bs-kpi__row">
            <span class="bs-kpi__value"><CountUp :value="stat.totalDevices" /></span>
            <span class="bs-kpi__unit">台</span>
          </div>
          <el-icon class="bs-kpi__wm"><Cpu /></el-icon>
        </div>

        <div class="bs-kpi" style="--kpi-color: #22d3a7; --d: 0.08s">
          <div class="bs-kpi__label"><el-icon><CircleCheck /></el-icon>在线设备</div>
          <div class="bs-kpi__row">
            <span class="bs-kpi__value"><CountUp :value="stat.onlineDevices" /></span>
            <span class="bs-kpi__unit">台</span>
          </div>
          <div class="bs-kpi__bar" :style="{ '--w': onlineRate + '%' }"><i></i></div>
          <el-icon class="bs-kpi__wm"><CircleCheck /></el-icon>
        </div>

        <div class="bs-kpi" style="--kpi-color: #f5a524; --d: 0.12s">
          <div class="bs-kpi__label"><el-icon><SwitchButton /></el-icon>离线设备</div>
          <div class="bs-kpi__row">
            <span class="bs-kpi__value"><CountUp :value="stat.offlineDevices" /></span>
            <span class="bs-kpi__unit">台</span>
          </div>
          <div class="bs-kpi__bar" :style="{ '--w': offlineRate + '%' }"><i></i></div>
          <el-icon class="bs-kpi__wm"><SwitchButton /></el-icon>
        </div>

        <div class="bs-kpi" style="--kpi-color: #ff5c7c; --d: 0.16s">
          <div class="bs-kpi__label"><el-icon><WarningFilled /></el-icon>故障设备</div>
          <div class="bs-kpi__row">
            <span class="bs-kpi__value"><CountUp :value="stat.faultDevices" /></span>
            <span class="bs-kpi__unit">台</span>
          </div>
          <div class="bs-kpi__bar" :style="{ '--w': faultRate + '%' }"><i></i></div>
          <el-icon class="bs-kpi__wm"><WarningFilled /></el-icon>
        </div>

        <div class="bs-kpi" style="--kpi-color: #8b5cf6; --d: 0.2s">
          <div class="bs-kpi__label"><el-icon><Connection /></el-icon>绑定率</div>
          <div class="bs-kpi__row">
            <span class="bs-kpi__value"><CountUp :value="stat.bindRate" /></span>
            <span class="bs-kpi__unit">%</span>
          </div>
          <div class="bs-kpi__bar" :style="{ '--w': stat.bindRate + '%' }"><i></i></div>
          <el-icon class="bs-kpi__wm"><Connection /></el-icon>
        </div>

        <div class="bs-kpi" style="--kpi-color: #4f8cff; --d: 0.24s">
          <div class="bs-kpi__label"><el-icon><BellFilled /></el-icon>今日告警</div>
          <div class="bs-kpi__row">
            <span class="bs-kpi__value"><CountUp :value="stat.todayAlertCount" /></span>
            <span class="bs-kpi__unit">次</span>
          </div>
          <el-icon class="bs-kpi__wm"><BellFilled /></el-icon>
        </div>
      </div>

      <!-- ===== 在线设备趋势 ===== -->
      <section class="bs-panel" style="grid-column: span 4; --d: 0.1s">
        <header class="bs-panel__head">
          <span class="bs-panel__title">在线设备趋势</span>
          <span class="bs-panel__tag">近 7 日 · 峰值 <b>{{ onlinePeak }}</b> 台</span>
        </header>
        <div class="bs-panel__body">
          <div id="deviceOnlineTrend" class="bs-chart"></div>
        </div>
      </section>

      <!-- ===== 设备占比 ===== -->
      <section class="bs-panel" style="grid-column: span 2; --d: 0.14s">
        <header class="bs-panel__head">
          <span class="bs-panel__title">设备运行状态占比</span>
          <span class="bs-panel__tag" @click="router.push('/home/device')" style="cursor: pointer"
            >明细 <el-icon><ArrowRight /></el-icon
          ></span>
        </header>
        <div class="bs-panel__body">
          <div class="bs-donut">
            <div class="bs-donut__chart">
              <div id="deviceStatusPie" class="bs-chart"></div>
              <div class="bs-donut__center">
                <b>{{ stat.totalDevices }}</b>
                <span>设备总数</span>
              </div>
            </div>
            <ul class="bs-legend">
              <li v-for="item in statusLegend" :key="item.name">
                <i :style="{ '--c': item.color }"></i>
                <span>{{ item.name }}</span>
                <b>{{ item.value }}</b>
                <em>{{ item.percent }}%</em>
              </li>
            </ul>
          </div>
        </div>
      </section>

      <!-- ===== 告警次数趋势 ===== -->
      <section class="bs-panel" style="grid-column: span 3; --d: 0.18s">
        <header class="bs-panel__head">
          <span class="bs-panel__title">告警触发趋势</span>
          <span class="bs-panel__tag"
            >近 7 日合计 <b>{{ alertTotal }}</b> 次 ·
            峰值 <b>{{ alertPeak }}</b> 次</span
          >
        </header>
        <div class="bs-panel__body">
          <div id="deviceAlertTrend" class="bs-chart"></div>
        </div>
      </section>

      <!-- ===== 故障类型 ===== -->
      <section class="bs-panel" style="grid-column: span 2; --d: 0.22s">
        <header class="bs-panel__head">
          <span class="bs-panel__title">故障类型分布</span>
          <span class="bs-panel__tag">按发生次数排序</span>
        </header>
        <div class="bs-panel__body">
          <div id="deviceFault" class="bs-chart"></div>
        </div>
      </section>

      <!-- ===== 绑定率 ===== -->
      <section class="bs-panel" style="grid-column: span 1; --d: 0.26s">
        <header class="bs-panel__head">
          <span class="bs-panel__title">设备绑定率</span>
        </header>
        <div class="bs-panel__body">
          <div id="deviceBindGauge" class="bs-chart"></div>
        </div>
      </section>
    </main>
  </BigScreenShell>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import BigScreenShell from '@/components/BigScreenShell.vue'
import CountUp from '@/components/CountUp.vue'
import {
  getDeviceStatistics,
  getDeviceOnlineTrend,
  getDeviceAlertTrend,
  getDeviceFaultStatistics,
} from '@/api/stats'
import { alertTypeLabel } from '@/utils/echartLabels'
import {
  BS_SEMANTIC,
  areaGradient,
  bsAnimation,
  bsAxis,
  bsEmpty,
  bsGrid,
  bsTooltip,
  hexA,
  hGradient,
  vGradient,
} from '@/utils/echartsTheme'

const router = useRouter()

/* ================================================================
   数据
   ================================================================ */
const stat = reactive({
  totalDevices: 0,
  onlineDevices: 0,
  offlineDevices: 0,
  faultDevices: 0,
  bindRate: 0,
  todayAlertCount: 0,
})

const onlineTrend = reactive<{ categories: string[]; data: number[] }>({ categories: [], data: [] })
const alertTrend = reactive<{ categories: string[]; data: number[] }>({ categories: [], data: [] })
const faultDist = reactive<{ categories: string[]; data: number[] }>({ categories: [], data: [] })

const onlineRate = computed(() =>
  stat.totalDevices ? Math.round((stat.onlineDevices / stat.totalDevices) * 100) : 0,
)
const offlineRate = computed(() =>
  stat.totalDevices ? Math.round((stat.offlineDevices / stat.totalDevices) * 100) : 0,
)
const faultRate = computed(() =>
  stat.totalDevices ? Math.round((stat.faultDevices / stat.totalDevices) * 100) : 0,
)
const onlinePeak = computed(() => (onlineTrend.data.length ? Math.max(...onlineTrend.data) : 0))
const alertTotal = computed(() => alertTrend.data.reduce((a, b) => a + b, 0))
const alertPeak = computed(() => (alertTrend.data.length ? Math.max(...alertTrend.data) : 0))

const statusLegend = computed(() => {
  const total = stat.totalDevices || 1
  return [
    { name: '在线', value: stat.onlineDevices, color: BS_SEMANTIC.online },
    { name: '离线', value: stat.offlineDevices, color: BS_SEMANTIC.offline },
    { name: '故障', value: stat.faultDevices, color: BS_SEMANTIC.fault },
  ].map((i) => ({ ...i, percent: Math.round((i.value / total) * 100) }))
})

/* ================================================================
   加载
   ================================================================ */
async function loadChartData() {
  try {
    const res = await getDeviceStatistics()
    Object.assign(stat, res.data.data || {})
  } catch {
    /* 后端不可用时保持空数据 */
  }
  try {
    const res = await getDeviceOnlineTrend()
    const d = res.data.data
    onlineTrend.categories = d?.categories || []
    onlineTrend.data = d?.series?.[0]?.data || []
  } catch {
    /* ignore */
  }
  try {
    const res = await getDeviceAlertTrend()
    const d = res.data.data
    alertTrend.categories = d?.categories || []
    alertTrend.data = d?.series?.[0]?.data || []
  } catch {
    /* ignore */
  }
  try {
    const res = await getDeviceFaultStatistics()
    const d = res.data.data
    if (d) {
      // 后端返回告警类型枚举 code，转中文后按次数倒序，条形图更易读
      const pairs = (d.categories || []).map((c: string, i: number) => ({
        name: alertTypeLabel(c),
        value: d.series?.[0]?.data?.[i] ?? 0,
      }))
      pairs.sort((a, b) => a.value - b.value)
      faultDist.categories = pairs.map((p) => p.name)
      faultDist.data = pairs.map((p) => p.value)
    }
  } catch {
    /* ignore */
  }
  nextTick(() => initCharts())
}

/* ================================================================
   图表
   ================================================================ */
let chartOnline: echarts.ECharts | null = null
let chartStatus: echarts.ECharts | null = null
let chartAlert: echarts.ECharts | null = null
let chartFault: echarts.ECharts | null = null
let chartBind: echarts.ECharts | null = null

function initCharts() {
  ;[chartOnline, chartStatus, chartAlert, chartFault, chartBind].forEach((c) => c?.dispose())
  chartOnline = chartStatus = chartAlert = chartFault = chartBind = null

  /* 1. 在线设备趋势 —— 折线 + 面积渐变 */
  const onlineEl = document.getElementById('deviceOnlineTrend')
  if (onlineEl) {
    chartOnline = echarts.init(onlineEl)
    chartOnline.setOption({
      ...bsAnimation,
      ...bsEmpty(onlineTrend.data.length),
      tooltip: bsTooltip({
        trigger: 'axis',
        axisPointer: { type: 'line', lineStyle: { color: 'rgba(56,225,255,0.45)' } },
        valueFormatter: (v: number) => `${v} 台`,
      }),
      grid: { ...bsGrid, top: 34, right: 30 },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: onlineTrend.categories,
        ...bsAxis,
        splitLine: { show: false },
      },
      yAxis: {
        type: 'value',
        minInterval: 1,
        ...bsAxis,
        splitLine: { lineStyle: { color: 'rgba(120,170,230,0.08)', type: 'dashed' } },
      },
      series: [
        {
          name: '在线设备数',
          type: 'line',
          smooth: true,
          showSymbol: false,
          symbolSize: 7,
          data: onlineTrend.data,
          lineStyle: {
            width: 2.4,
            color: BS_SEMANTIC.cyan,
            shadowColor: hexA(BS_SEMANTIC.cyan, 0.6),
            shadowBlur: 14,
          },
          itemStyle: { color: BS_SEMANTIC.cyan },
          areaStyle: { color: areaGradient(BS_SEMANTIC.cyan, 0.32, 0.01) },
          emphasis: { focus: 'series', scale: 1.4 },
          markPoint: {
            symbol: 'circle',
            symbolSize: 9,
            data: [{ type: 'max', name: '峰值' }],
            itemStyle: {
              color: '#04121f',
              borderColor: BS_SEMANTIC.cyan,
              borderWidth: 2,
              shadowBlur: 14,
              shadowColor: hexA(BS_SEMANTIC.cyan, 0.9),
            },
            label: {
              show: true,
              position: 'top',
              distance: 8,
              formatter: '{c}',
              color: '#bfe9ff',
              fontSize: 11,
              fontWeight: 700,
            },
          },
        },
      ],
    })
  }

  /* 2. 设备状态占比 —— 环形 */
  const statusEl = document.getElementById('deviceStatusPie')
  if (statusEl) {
    chartStatus = echarts.init(statusEl)
    chartStatus.setOption({
      ...bsAnimation,
      tooltip: bsTooltip({ trigger: 'item', formatter: '{b}：{c} 台（{d}%）' }),
      series: [
        {
          name: '设备状态',
          type: 'pie',
          radius: ['58%', '82%'],
          center: ['50%', '50%'],
          avoidLabelOverlap: true,
          padAngle: 3,
          label: { show: false },
          labelLine: { show: false },
          itemStyle: {
            borderColor: 'rgba(6,14,28,0.92)',
            borderWidth: 2,
            borderRadius: 5,
          },
          emphasis: {
            scale: true,
            scaleSize: 8,
            itemStyle: { shadowBlur: 22, shadowColor: 'rgba(56,225,255,0.55)' },
          },
          data: [
            { name: '在线', value: stat.onlineDevices, itemStyle: { color: BS_SEMANTIC.online } },
            { name: '离线', value: stat.offlineDevices, itemStyle: { color: BS_SEMANTIC.offline } },
            { name: '故障', value: stat.faultDevices, itemStyle: { color: BS_SEMANTIC.fault } },
          ].filter((d) => d.value > 0),
        },
      ],
    })
  }

  /* 3. 告警触发趋势 —— 柱状，峰值高亮 */
  const alertEl = document.getElementById('deviceAlertTrend')
  if (alertEl) {
    chartAlert = echarts.init(alertEl)
    const peak = alertPeak.value
    chartAlert.setOption({
      ...bsAnimation,
      ...bsEmpty(alertTrend.data.length),
      tooltip: bsTooltip({
        trigger: 'axis',
        axisPointer: { type: 'shadow', shadowStyle: { color: 'rgba(56,225,255,0.06)' } },
        valueFormatter: (v: number) => `${v} 次`,
      }),
      grid: { ...bsGrid, top: 26, right: 18 },
      xAxis: {
        type: 'category',
        data: alertTrend.categories,
        ...bsAxis,
        splitLine: { show: false },
      },
      yAxis: {
        type: 'value',
        minInterval: 1,
        ...bsAxis,
        splitLine: { lineStyle: { color: 'rgba(120,170,230,0.08)', type: 'dashed' } },
      },
      series: [
        {
          name: '告警次数',
          type: 'bar',
          barWidth: '44%',
          data: alertTrend.data,
          itemStyle: {
            borderRadius: [5, 5, 0, 0],
            color: (p: { value: number }) =>
              p.value >= peak && peak > 0
                ? vGradient(BS_SEMANTIC.rose, hexA(BS_SEMANTIC.amber, 0.35))
                : vGradient(hexA(BS_SEMANTIC.cyan, 0.85), hexA(BS_SEMANTIC.blue, 0.12)),
          },
          emphasis: {
            itemStyle: { shadowBlur: 18, shadowColor: hexA(BS_SEMANTIC.cyan, 0.65) },
          },
        },
      ],
    })
  }

  /* 4. 故障类型 —— 横向条形 */
  const faultEl = document.getElementById('deviceFault')
  if (faultEl) {
    chartFault = echarts.init(faultEl)
    chartFault.setOption({
      ...bsAnimation,
      ...bsEmpty(faultDist.data.length),
      tooltip: bsTooltip({
        trigger: 'axis',
        axisPointer: { type: 'shadow', shadowStyle: { color: 'rgba(56,225,255,0.06)' } },
        valueFormatter: (v: number) => `${v} 次`,
      }),
      grid: { top: 6, left: 4, right: 34, bottom: 2, containLabel: true },
      xAxis: {
        type: 'value',
        minInterval: 1,
        ...bsAxis,
        axisLine: { show: false },
        splitLine: { lineStyle: { color: 'rgba(120,170,230,0.08)', type: 'dashed' } },
      },
      yAxis: {
        type: 'category',
        data: faultDist.categories,
        ...bsAxis,
        axisLine: { show: false },
        axisLabel: { color: '#9db8d6', fontSize: 11.5 },
      },
      series: [
        {
          name: '故障次数',
          type: 'bar',
          barWidth: 11,
          data: faultDist.data,
          label: {
            show: true,
            position: 'right',
            distance: 8,
            formatter: '{c}',
            color: '#eaf7ff',
            fontSize: 12,
            fontWeight: 700,
          },
          itemStyle: {
            borderRadius: [0, 6, 6, 0],
            color: hGradient(hexA(BS_SEMANTIC.rose, 0.55), BS_SEMANTIC.rose),
          },
          showBackground: true,
          backgroundStyle: {
            color: 'rgba(86,148,220,0.08)',
            borderRadius: [0, 6, 6, 0],
          },
          emphasis: { itemStyle: { shadowBlur: 16, shadowColor: hexA(BS_SEMANTIC.rose, 0.8) } },
        },
      ],
    })
  }

  /* 5. 绑定率 —— 仪表盘 */
  const bindEl = document.getElementById('deviceBindGauge')
  if (bindEl) {
    chartBind = echarts.init(bindEl)
    chartBind.setOption({
      ...bsAnimation,
      tooltip: bsTooltip({ trigger: 'item', formatter: '绑定率：{c}%' }),
      series: [
        {
          type: 'gauge',
          startAngle: 215,
          endAngle: -35,
          min: 0,
          max: 100,
          radius: '96%',
          center: ['50%', '56%'],
          progress: {
            show: true,
            width: 9,
            roundCap: true,
            itemStyle: { color: vGradient(BS_SEMANTIC.violet, BS_SEMANTIC.cyan) },
          },
          axisLine: { lineStyle: { width: 9, color: [[1, 'rgba(86,148,220,0.14)']] } },
          axisTick: { show: false },
          splitLine: { show: false },
          axisLabel: { show: false },
          pointer: { show: false },
          anchor: { show: false },
          title: {
            offsetCenter: [0, '34%'],
            color: '#6d8aac',
            fontSize: 11,
          },
          detail: {
            valueAnimation: true,
            offsetCenter: [0, '-2%'],
            formatter: '{value}%',
            color: '#f2faff',
            fontSize: 26,
            fontWeight: 700,
            fontFamily: 'DIN Alternate, Bahnschrift, Arial',
          },
          data: [{ value: Math.round(stat.bindRate), name: '设备绑定率' }],
        },
      ],
    })
  }
}

/* ================================================================
   自适应
   ================================================================ */
function resizeCharts() {
  chartOnline?.resize()
  chartStatus?.resize()
  chartAlert?.resize()
  chartFault?.resize()
  chartBind?.resize()
}

onMounted(async () => {
  await loadChartData()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  ;[chartOnline, chartStatus, chartAlert, chartFault, chartBind].forEach((c) => c?.dispose())
  window.removeEventListener('resize', resizeCharts)
})
</script>

<style scoped>
.bs-device {
  grid-template-columns: repeat(6, minmax(0, 1fr));
  grid-template-rows: auto minmax(0, 1fr) minmax(0, 1fr);
}

.device-kpis {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 14px;
}

/* Gauge 面板在窄列里隐藏标题说明，避免挤压 */
.bs-panel__tag .el-icon {
  vertical-align: -2px;
}

@media (max-width: 1360px) {
  .device-kpis {
    gap: 10px;
  }
  .bs-device {
    gap: 10px;
  }
}
</style>
