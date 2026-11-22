<template>
  <BigScreenShell active="person">
    <main class="bs-main bs-person">
      <!-- ===== KPI ===== -->
      <div class="person-kpis">
        <div class="bs-kpi" style="--kpi-color: #38e1ff; --d: 0.04s">
          <div class="bs-kpi__label"><el-icon><UserFilled /></el-icon>服务人员总数</div>
          <div class="bs-kpi__row">
            <span class="bs-kpi__value"><CountUp :value="staffTotal" /></span>
            <span class="bs-kpi__unit">人</span>
          </div>
          <el-icon class="bs-kpi__wm"><UserFilled /></el-icon>
        </div>

        <div class="bs-kpi" style="--kpi-color: #22d3a7; --d: 0.08s">
          <div class="bs-kpi__label"><el-icon><User /></el-icon>服务对象总数</div>
          <div class="bs-kpi__row">
            <span class="bs-kpi__value"><CountUp :value="elderTotal" /></span>
            <span class="bs-kpi__unit">人</span>
          </div>
          <el-icon class="bs-kpi__wm"><User /></el-icon>
        </div>

        <div class="bs-kpi" style="--kpi-color: #8b5cf6; --d: 0.12s">
          <div class="bs-kpi__label"><el-icon><Suitcase /></el-icon>岗位类别</div>
          <div class="bs-kpi__row">
            <span class="bs-kpi__value"><CountUp :value="jobCount" /></span>
            <span class="bs-kpi__unit">类</span>
          </div>
          <el-icon class="bs-kpi__wm"><Suitcase /></el-icon>
        </div>

        <div class="bs-kpi" style="--kpi-color: #f5a524; --d: 0.16s">
          <div class="bs-kpi__label"><el-icon><PieChart /></el-icon>服务对象男女比</div>
          <div class="bs-kpi__row">
            <span class="bs-kpi__value">{{ genderRatio }}</span>
          </div>
          <el-icon class="bs-kpi__wm"><PieChart /></el-icon>
        </div>
      </div>

      <!-- ===== 人员构成 ===== -->
      <section class="bs-panel" style="grid-column: span 2; --d: 0.1s">
        <header class="bs-panel__head">
          <span class="bs-panel__title">人员构成</span>
          <span class="bs-panel__tag">在册 <b>{{ staffTotal }}</b> 人</span>
        </header>
        <div class="bs-panel__body">
          <div class="bs-donut">
            <div class="bs-donut__chart">
              <div id="personRatio" class="bs-chart"></div>
              <div class="bs-donut__center">
                <b>{{ staffTotal }}</b>
                <span>服务人员</span>
              </div>
            </div>
            <ul class="bs-legend">
              <li v-for="(item, i) in ratioData" :key="item.name">
                <i :style="{ '--c': colorAt(i) }"></i>
                <span>{{ item.name }}</span>
                <b>{{ item.value }}</b>
                <em>{{ percentOf(item.value, staffTotal) }}%</em>
              </li>
            </ul>
          </div>
        </div>
      </section>

      <!-- ===== 岗位分布 ===== -->
      <section class="bs-panel" style="grid-column: span 2; --d: 0.14s">
        <header class="bs-panel__head">
          <span class="bs-panel__title">各岗位人员分布</span>
          <span class="bs-panel__tag"
            >共 <b>{{ jobCount }}</b> 类岗位</span
          >
        </header>
        <div class="bs-panel__body">
          <div id="personJob" class="bs-chart"></div>
        </div>
      </section>

      <!-- ===== 陪护能力雷达 ===== -->
      <section class="bs-panel" style="grid-column: span 2; --d: 0.18s">
        <header class="bs-panel__head">
          <span class="bs-panel__title">陪护人员岗位覆盖</span>
          <span class="bs-panel__tag">按岗位归一化</span>
        </header>
        <div class="bs-panel__body">
          <div id="personRadar" class="bs-chart"></div>
        </div>
      </section>

      <!-- ===== 年龄分布 ===== -->
      <section class="bs-panel" style="grid-column: span 4; --d: 0.22s">
        <header class="bs-panel__head">
          <span class="bs-panel__title">服务对象年龄分布</span>
          <span class="bs-panel__tag"
            >合计 <b>{{ elderTotal }}</b> 人 · 60 岁以上占比
            <b>{{ agedPercent }}%</b></span
          >
        </header>
        <div class="bs-panel__body">
          <div id="personAge" class="bs-chart"></div>
        </div>
      </section>

      <!-- ===== 性别构成 ===== -->
      <section class="bs-panel" style="grid-column: span 2; --d: 0.26s">
        <header class="bs-panel__head">
          <span class="bs-panel__title">服务对象性别构成</span>
          <span
            class="bs-panel__tag"
            style="cursor: pointer"
            @click="router.push('/home/elder/base')"
            >档案 <el-icon><ArrowRight /></el-icon
          ></span>
        </header>
        <div class="bs-panel__body gender-body">
          <div id="personGender" class="bs-chart"></div>
          <ul class="gender-legend">
            <li>
              <i style="--c: #4f8cff"></i>
              <span>男</span>
              <b>{{ maleCount }}</b>
              <em>{{ malePercent }}%</em>
            </li>
            <li>
              <i style="--c: #ff5c7c"></i>
              <span>女</span>
              <b>{{ femaleCount }}</b>
              <em>{{ 100 - malePercent }}%</em>
            </li>
          </ul>
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
  getPersonTrend,
  getPersonRatio,
  getGenderDistribution,
  getAgeDistribution,
  getCaregiverDistribution,
} from '@/api/stats'
import {
  BS_COLORS,
  areaGradient,
  bsAnimation,
  bsAxis,
  bsEmpty,
  bsTooltip,
  hexA,
  hGradient,
  vGradient,
} from '@/utils/echartsTheme'

const router = useRouter()

/** 女性对比色，与全局语义色一致 */
const GENDER_FEMALE = '#ff5c7c'

/* ================================================================
   数据
   ================================================================ */
const ratioData = reactive<Array<{ name: string; value: number }>>([])
const jobData = reactive<{ names: string[]; values: number[] }>({ names: [], values: [] })
const radarData = reactive<{ indicators: Array<{ name: string; max: number }>; value: number[] }>({
  indicators: [],
  value: [],
})
const ageData = reactive<Array<{ name: string; value: number }>>([])
const genderData = reactive<Array<{ name: string; value: number }>>([])

const colorAt = (i: number) => BS_COLORS[i % BS_COLORS.length]
const percentOf = (v: number, total: number) => (total ? Math.round((v / total) * 100) : 0)

const staffTotal = computed(() => ratioData.reduce((a, b) => a + (b.value || 0), 0))
const elderTotal = computed(() => genderData.reduce((a, b) => a + (b.value || 0), 0))
const jobCount = computed(() => jobData.names.length)
const agedPercent = computed(() => {
  const aged = ageData.reduce((a, b) => a + (b.value || 0), 0)
  return elderTotal.value ? Math.round((aged / elderTotal.value) * 100) : 0
})
const maleCount = computed(() => genderData.find((g) => g.name.includes('男'))?.value ?? 0)
const femaleCount = computed(() => genderData.find((g) => g.name.includes('女'))?.value ?? 0)
const malePercent = computed(() =>
  elderTotal.value ? Math.round((maleCount.value / elderTotal.value) * 100) : 0,
)
const genderRatio = computed(() => {
  const male = genderData.find((g) => g.name.includes('男'))?.value ?? 0
  const female = genderData.find((g) => g.name.includes('女'))?.value ?? 0
  if (!male && !female) return '—'
  return `${male} : ${female}`
})

/* ================================================================
   加载
   ================================================================ */
async function loadChartData() {
  try {
    const res = await getPersonRatio()
    ratioData.splice(0, ratioData.length, ...(res.data.data || []))
  } catch {
    /* ignore */
  }
  try {
    const res = await getPersonTrend()
    const d = res.data.data
    if (d) {
      // 后端按岗位统计人数，这里按人数倒序，横向条形更直观
      const pairs = (d.categories || []).map((c: string, i: number) => ({
        name: c,
        value: d.series?.[0]?.data?.[i] ?? 0,
      }))
      pairs.sort((a, b) => a.value - b.value)
      jobData.names = pairs.map((p) => p.name)
      jobData.values = pairs.map((p) => p.value)
    }
  } catch {
    /* ignore */
  }
  try {
    const res = await getCaregiverDistribution()
    const d = res.data.data
    if (d) {
      // 后端 indicator 的 max 固定为 100，实际人数只有个位数，
      // 直接用会把雷达图缩成一点，这里按当前数据峰值重算刻度上限
      const values_: number[] = d.series?.[0]?.value || []
      const peak = values_.length ? Math.max(...values_) : 0
      const max = Math.max(1, Math.ceil(peak * 1.2))
      radarData.indicators = (d.indicator || []).map((i: { name: string }) => ({ name: i.name, max }))
      radarData.value = values_
    }
  } catch {
    /* ignore */
  }
  try {
    const res = await getAgeDistribution()
    ageData.splice(0, ageData.length, ...(res.data.data || []))
  } catch {
    /* ignore */
  }
  try {
    const res = await getGenderDistribution()
    genderData.splice(0, genderData.length, ...(res.data.data || []))
  } catch {
    /* ignore */
  }
  nextTick(() => initCharts())
}

/* ================================================================
   图表
   ================================================================ */
let chartRatio: echarts.ECharts | null = null
let chartJob: echarts.ECharts | null = null
let chartRadar: echarts.ECharts | null = null
let chartAge: echarts.ECharts | null = null
let chartGender: echarts.ECharts | null = null

function initCharts() {
  ;[chartRatio, chartJob, chartRadar, chartAge, chartGender].forEach((c) => c?.dispose())
  chartRatio = chartJob = chartRadar = chartAge = chartGender = null

  /* 1. 人员构成 —— 环形 */
  const ratioEl = document.getElementById('personRatio')
  if (ratioEl) {
    chartRatio = echarts.init(ratioEl)
    chartRatio.setOption({
      ...bsAnimation,
      tooltip: bsTooltip({ trigger: 'item', formatter: '{b}：{c} 人（{d}%）' }),
      series: [
        {
          name: '人员构成',
          type: 'pie',
          radius: ['58%', '82%'],
          center: ['50%', '50%'],
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
            itemStyle: { shadowBlur: 22, shadowColor: 'rgba(56,225,255,0.5)' },
          },
          data: ratioData.map((d, i) => ({
            name: d.name,
            value: d.value,
            itemStyle: { color: colorAt(i) },
          })),
        },
      ],
    })
  }

  /* 2. 各岗位人员分布 —— 横向条形 */
  const jobEl = document.getElementById('personJob')
  if (jobEl) {
    chartJob = echarts.init(jobEl)
    chartJob.setOption({
      ...bsAnimation,
      ...bsEmpty(jobData.values.length),
      tooltip: bsTooltip({
        trigger: 'axis',
        axisPointer: { type: 'shadow', shadowStyle: { color: 'rgba(56,225,255,0.06)' } },
        valueFormatter: (v: number) => `${v} 人`,
      }),
      grid: { top: 6, left: 4, right: 44, bottom: 2, containLabel: true },
      xAxis: {
        type: 'value',
        minInterval: 1,
        ...bsAxis,
        axisLine: { show: false },
        splitLine: { lineStyle: { color: 'rgba(120,170,230,0.08)', type: 'dashed' } },
      },
      yAxis: {
        type: 'category',
        data: jobData.names,
        ...bsAxis,
        axisLine: { show: false },
        axisLabel: { color: '#9db8d6', fontSize: 11.5 },
      },
      series: [
        {
          name: '人数',
          type: 'bar',
          barWidth: 11,
          data: jobData.values,
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
            color: hGradient(hexA(BS_COLORS[0], 0.45), BS_COLORS[0]),
          },
          showBackground: true,
          backgroundStyle: { color: 'rgba(86,148,220,0.05)', borderRadius: [0, 6, 6, 0] },
          emphasis: { itemStyle: { shadowBlur: 16, shadowColor: hexA(BS_COLORS[0], 0.8) } },
        },
      ],
    })
  }

  /* 3. 陪护岗位覆盖 —— 雷达 */
  const radarEl = document.getElementById('personRadar')
  if (radarEl) {
    chartRadar = echarts.init(radarEl)
    chartRadar.setOption({
      ...bsAnimation,
      ...bsEmpty(radarData.value.length),
      tooltip: bsTooltip({ trigger: 'item' }),
      radar: {
        center: ['50%', '54%'],
        radius: '70%',
        shape: 'polygon',
        indicator: radarData.indicators,
        splitNumber: 3,
        axisName: { color: '#8ba8ca', fontSize: 10.5 },
        splitLine: { lineStyle: { color: 'rgba(86,148,220,0.18)' } },
        splitArea: {
          areaStyle: {
            color: ['rgba(56,225,255,0.03)', 'rgba(56,225,255,0.06)'],
          },
        },
        axisLine: { lineStyle: { color: 'rgba(86,148,220,0.2)' } },
      },
      series: [
        {
          type: 'radar',
          symbolSize: 5,
          data: [
            {
              value: radarData.value,
              name: '当前水平',
              areaStyle: { color: areaGradient(BS_COLORS[0], 0.5, 0.08) },
              lineStyle: { color: BS_COLORS[0], width: 2 },
              itemStyle: { color: BS_COLORS[0], shadowBlur: 10, shadowColor: BS_COLORS[0] },
            },
          ],
        },
      ],
    })
  }

  /* 4. 年龄分布 —— 柱状 */
  const ageEl = document.getElementById('personAge')
  if (ageEl) {
    chartAge = echarts.init(ageEl)
    const peak = ageData.length ? Math.max(...ageData.map((d) => d.value)) : 0
    chartAge.setOption({
      ...bsAnimation,
      ...bsEmpty(ageData.length),
      tooltip: bsTooltip({
        trigger: 'axis',
        axisPointer: { type: 'shadow', shadowStyle: { color: 'rgba(56,225,255,0.06)' } },
        valueFormatter: (v: number) => `${v} 人`,
      }),
      grid: { top: 30, left: 6, right: 16, bottom: 4, containLabel: true },
      xAxis: {
        type: 'category',
        data: ageData.map((d) => d.name),
        ...bsAxis,
        splitLine: { show: false },
        axisLabel: { color: '#9db8d6', fontSize: 11 },
      },
      yAxis: {
        type: 'value',
        minInterval: 1,
        ...bsAxis,
        splitLine: { lineStyle: { color: 'rgba(120,170,230,0.08)', type: 'dashed' } },
      },
      series: [
        {
          name: '人数',
          type: 'bar',
          barWidth: '42%',
          data: ageData.map((d) => d.value),
          label: {
            show: true,
            position: 'top',
            distance: 8,
            formatter: '{c}',
            color: '#cfe6ff',
            fontSize: 12,
            fontWeight: 700,
          },
          itemStyle: {
            borderRadius: [6, 6, 0, 0],
            color: (p: any) =>
              p.value >= peak && peak > 0
                ? vGradient(BS_COLORS[0], hexA(BS_COLORS[1], 0.15))
                : vGradient(hexA(BS_COLORS[1], 0.8), hexA(BS_COLORS[1], 0.1)),
          },
          emphasis: { itemStyle: { shadowBlur: 18, shadowColor: hexA(BS_COLORS[0], 0.6) } },
        },
      ],
    })
  }

  /* 5. 性别构成 —— 半环 */
  const genderEl = document.getElementById('personGender')
  if (genderEl) {
    chartGender = echarts.init(genderEl)
    const total = genderData.reduce((a, b) => a + b.value, 0) || 1
    chartGender.setOption({
      ...bsAnimation,
      ...bsEmpty(genderData.length),
      tooltip: bsTooltip({ trigger: 'item', formatter: '{b}：{c} 人（{d}%）' }),
      series: [
        {
          name: '性别构成',
          type: 'pie',
          radius: ['64%', '92%'],
          center: ['50%', '82%'],
          startAngle: 180,
          endAngle: 0,
          padAngle: 2,
          label: {
            show: true,
            position: 'inside',
            formatter: '{d}%',
            color: '#04121f',
            fontSize: 12,
            fontWeight: 700,
          },
          labelLine: { show: false },
          itemStyle: { borderColor: 'rgba(6,14,28,0.6)', borderWidth: 1, borderRadius: 4 },
          emphasis: { scale: true, scaleSize: 5 },
          data: genderData.map((d, i) => ({
            name: d.name,
            value: d.value,
            itemStyle: { color: i === 0 ? '#4f8cff' : '#ff5c7c' },
          })),
        },
      ],
      graphic: [
        {
          type: 'text',
          left: 'center',
          top: '56%',
          style: {
            text: String(total),
            fill: '#f2faff',
            fontSize: 24,
            fontWeight: 700,
            fontFamily: 'DIN Alternate, Bahnschrift, Arial',
          },
        },
        {
          type: 'text',
          left: 'center',
          top: '72%',
          style: { text: '服务对象', fill: '#6d8aac', fontSize: 10.5 },
        },
      ],
    })
  }
}

/* ================================================================
   自适应
   ================================================================ */
function resizeCharts() {
  chartRatio?.resize()
  chartJob?.resize()
  chartRadar?.resize()
  chartAge?.resize()
  chartGender?.resize()
}

onMounted(async () => {
  await loadChartData()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  ;[chartRatio, chartJob, chartRadar, chartAge, chartGender].forEach((c) => c?.dispose())
  window.removeEventListener('resize', resizeCharts)
})
</script>

<style scoped>
.bs-person {
  grid-template-columns: repeat(6, minmax(0, 1fr));
  grid-template-rows: auto minmax(0, 1fr) minmax(0, 1.05fr);
}

.person-kpis {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

/* ===== 性别构成 ===== */
.gender-body {
  flex-direction: column;
  gap: 2px;
}

.gender-body > .bs-chart {
  flex: 1 1 auto;
  min-height: 0;
}

.gender-legend {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 22px;
  flex-shrink: 0;
  margin: 0;
  padding: 0 0 2px;
  list-style: none;
}

.gender-legend li {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--bs-sub);
}

.gender-legend i {
  width: 7px;
  height: 7px;
  border-radius: 2px;
  background: var(--c);
  box-shadow: 0 0 8px var(--c);
}

.gender-legend b {
  font-family: 'DIN Alternate', 'Bahnschrift', 'Arial Narrow', Arial, sans-serif;
  font-size: 14px;
  color: #eaf7ff;
  font-variant-numeric: tabular-nums;
}

.gender-legend em {
  font-style: normal;
  font-size: 11px;
  color: #7d97b4;
}

@media (max-width: 1360px) {
  .person-kpis,
  .bs-person {
    gap: 10px;
  }
  .bs-kpi__value {
    font-size: 22px;
  }
}
</style>
