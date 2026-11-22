<template>
  <BigScreenShell active="expert">
    <main class="bs-main bs-expert">
      <!-- ===== KPI ===== -->
      <div class="expert-kpis">
        <div class="bs-kpi" style="--kpi-color: #38e1ff; --d: 0.04s">
          <div class="bs-kpi__label"><el-icon><FirstAidKit /></el-icon>在册专家</div>
          <div class="bs-kpi__row">
            <span class="bs-kpi__value"><CountUp :value="stat.totalExperts" /></span>
            <span class="bs-kpi__unit">人</span>
          </div>
          <el-icon class="bs-kpi__wm"><FirstAidKit /></el-icon>
        </div>

        <div class="bs-kpi" style="--kpi-color: #4f8cff; --d: 0.08s">
          <div class="bs-kpi__label"><el-icon><ChatDotRound /></el-icon>累计咨询</div>
          <div class="bs-kpi__row">
            <span class="bs-kpi__value"><CountUp :value="stat.totalConsultations" /></span>
            <span class="bs-kpi__unit">次</span>
          </div>
          <el-icon class="bs-kpi__wm"><ChatDotRound /></el-icon>
        </div>

        <div class="bs-kpi" style="--kpi-color: #f5a524; --d: 0.12s">
          <div class="bs-kpi__label"><el-icon><StarFilled /></el-icon>平均满意度</div>
          <div class="bs-kpi__row">
            <span class="bs-kpi__value"><CountUp :value="stat.avgSatisfaction" :decimals="1" /></span>
            <span class="bs-kpi__unit">/ 5.0</span>
          </div>
          <el-icon class="bs-kpi__wm"><StarFilled /></el-icon>
        </div>

        <div class="bs-kpi" style="--kpi-color: #ff5c7c; --d: 0.16s">
          <div class="bs-kpi__label"><el-icon><AlarmClock /></el-icon>待处理咨询</div>
          <div class="bs-kpi__row">
            <span class="bs-kpi__value"><CountUp :value="stat.pendingConsultations" /></span>
            <span class="bs-kpi__unit">单</span>
          </div>
          <el-icon class="bs-kpi__wm"><AlarmClock /></el-icon>
        </div>
      </div>

      <!-- ===== 职称构成 ===== -->
      <section class="bs-panel" style="grid-column: span 2; --d: 0.1s">
        <header class="bs-panel__head">
          <span class="bs-panel__title">专家职称构成</span>
          <span class="bs-panel__tag">共 <b>{{ titleData.length }}</b> 类职称</span>
        </header>
        <div class="bs-panel__body">
          <div class="bs-donut">
            <div class="bs-donut__chart">
              <div id="expertTitle" class="bs-chart"></div>
              <div class="bs-donut__center">
                <b>{{ stat.totalExperts }}</b>
                <span>在册专家</span>
              </div>
            </div>
            <ul class="bs-legend">
              <li v-for="(item, i) in titleData" :key="item.name">
                <i :style="{ '--c': colorAt(i) }"></i>
                <span>{{ item.name }}</span>
                <b>{{ item.value }}</b>
                <em>{{ percentOf(item.value, stat.totalExperts) }}%</em>
              </li>
            </ul>
          </div>
        </div>
      </section>

      <!-- ===== 咨询趋势 ===== -->
      <section class="bs-panel" style="grid-column: span 4; --d: 0.14s">
        <header class="bs-panel__head">
          <span class="bs-panel__title">专家咨询趋势</span>
          <span class="bs-panel__tag"
            >近 7 日合计 <b>{{ trendTotal }}</b> 次 · 峰值 <b>{{ trendPeak }}</b> 次</span
          >
        </header>
        <div class="bs-panel__body">
          <div id="expertTrend" class="bs-chart"></div>
        </div>
      </section>

      <!-- ===== 领域覆盖 ===== -->
      <section class="bs-panel" style="grid-column: span 3; --d: 0.18s">
        <header class="bs-panel__head">
          <span class="bs-panel__title">各领域专家覆盖</span>
          <span
            class="bs-panel__tag"
            style="cursor: pointer"
            @click="router.push('/home/expert')"
            >专家名册 <el-icon><ArrowRight /></el-icon
          ></span>
        </header>
        <div class="bs-panel__body">
          <div id="expertField" class="bs-chart"></div>
        </div>
      </section>

      <!-- ===== 满意度 ===== -->
      <section class="bs-panel" style="grid-column: span 3; --d: 0.22s">
        <header class="bs-panel__head">
          <span class="bs-panel__title">咨询满意度评分</span>
          <span class="bs-panel__tag">有效评价 <b>{{ ratingTotal }}</b> 条</span>
        </header>
        <div class="bs-panel__body expert-rating">
          <div class="expert-rating__gauge">
            <div id="expertRating" class="bs-chart"></div>
          </div>
          <div class="expert-rating__list">
            <div class="expert-rating__stars">
              <el-icon v-for="n in 5" :key="n" :class="{ 'is-on': n <= Math.round(stat.avgSatisfaction) }">
                <StarFilled />
              </el-icon>
            </div>
            <div v-for="item in ratingData" :key="item.name" class="expert-rating__row">
              <span class="expert-rating__name">{{ item.name }}</span>
              <span class="expert-rating__track">
                <i :style="{ width: barWidth(item.value) + '%' }"></i>
              </span>
              <b>{{ item.value }}</b>
            </div>
            <p v-if="!ratingData.length" class="expert-rating__empty">暂无评价数据</p>
          </div>
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
  getExpertStatistics,
  getExpertTitleDistribution,
  getExpertFieldDistribution,
  getExpertConsultationTrend,
  getExpertSatisfactionDistribution,
} from '@/api/stats'
import {
  BS_COLORS,
  bsAnimation,
  bsAxis,
  bsEmpty,
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
  totalExperts: 0,
  totalConsultations: 0,
  avgSatisfaction: 0,
  pendingConsultations: 0,
})

const titleData = reactive<Array<{ name: string; value: number }>>([])
const fieldData = reactive<{ names: string[]; values: number[] }>({ names: [], values: [] })
const trendData = reactive<{ categories: string[]; values: number[] }>({ categories: [], values: [] })
const ratingData = reactive<Array<{ name: string; value: number }>>([])

const colorAt = (i: number) => BS_COLORS[i % BS_COLORS.length]
const percentOf = (v: number, total: number) => (total ? Math.round((v / total) * 100) : 0)

const trendTotal = computed(() => trendData.values.reduce((a, b) => a + b, 0))
const trendPeak = computed(() => (trendData.values.length ? Math.max(...trendData.values) : 0))
const ratingTotal = computed(() => ratingData.reduce((a, b) => a + b.value, 0))
const barWidth = (v: number) =>
  ratingTotal.value ? Math.max(6, Math.round((v / ratingTotal.value) * 100)) : 0

/* ================================================================
   加载
   ================================================================ */
async function loadChartData() {
  try {
    const res = await getExpertStatistics()
    Object.assign(stat, res.data.data || {})
  } catch {
    /* ignore */
  }
  try {
    const res = await getExpertTitleDistribution()
    titleData.splice(0, titleData.length, ...(res.data.data || []))
  } catch {
    /* ignore */
  }
  try {
    const res = await getExpertFieldDistribution()
    const d = res.data.data
    if (d) {
      const pairs = (d.categories || []).map((c: string, i: number) => ({
        name: c,
        value: d.barSeries?.[0]?.data?.[i] ?? d.lineSeries?.[0]?.data?.[i] ?? 0,
      }))
      pairs.sort((a, b) => a.value - b.value)
      fieldData.names = pairs.map((p) => p.name)
      fieldData.values = pairs.map((p) => p.value)
    }
  } catch {
    /* ignore */
  }
  try {
    const res = await getExpertConsultationTrend()
    const d = res.data.data
    if (d) {
      trendData.categories = d.categories || []
      trendData.values = d.barSeries?.[0]?.data || d.lineSeries?.[0]?.data || []
    }
  } catch {
    /* ignore */
  }
  try {
    const res = await getExpertSatisfactionDistribution()
    ratingData.splice(0, ratingData.length, ...(res.data.data || []))
  } catch {
    /* ignore */
  }
  nextTick(() => initCharts())
}

/* ================================================================
   图表
   ================================================================ */
let chartTitle: echarts.ECharts | null = null
let chartTrend: echarts.ECharts | null = null
let chartField: echarts.ECharts | null = null
let chartRating: echarts.ECharts | null = null

function initCharts() {
  ;[chartTitle, chartTrend, chartField, chartRating].forEach((c) => c?.dispose())
  chartTitle = chartTrend = chartField = chartRating = null

  /* 1. 职称构成 —— 环形 */
  const titleEl = document.getElementById('expertTitle')
  if (titleEl) {
    chartTitle = echarts.init(titleEl)
    chartTitle.setOption({
      ...bsAnimation,
      tooltip: bsTooltip({ trigger: 'item', formatter: '{b}：{c} 人（{d}%）' }),
      series: [
        {
          name: '职称构成',
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
          data: titleData.map((d, i) => ({
            name: d.name,
            value: d.value,
            itemStyle: { color: colorAt(i) },
          })),
        },
      ],
    })
  }

  /* 2. 咨询趋势 —— 柱状，峰值高亮 */
  const trendEl = document.getElementById('expertTrend')
  if (trendEl) {
    chartTrend = echarts.init(trendEl)
    const peak = trendPeak.value
    chartTrend.setOption({
      ...bsAnimation,
      ...bsEmpty(trendData.values.length),
      tooltip: bsTooltip({
        trigger: 'axis',
        axisPointer: { type: 'shadow', shadowStyle: { color: 'rgba(56,225,255,0.06)' } },
        valueFormatter: (v: number) => `${v} 次`,
      }),
      grid: { top: 30, left: 6, right: 18, bottom: 4, containLabel: true },
      xAxis: {
        type: 'category',
        data: trendData.categories,
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
          name: '咨询量',
          type: 'bar',
          barWidth: '38%',
          data: trendData.values,
          label: {
            show: true,
            position: 'top',
            distance: 7,
            formatter: (p: any) => (p.value > 0 ? p.value : ''),
            color: '#cfe6ff',
            fontSize: 11.5,
            fontWeight: 700,
          },
          itemStyle: {
            borderRadius: [6, 6, 0, 0],
            color: (p: any) =>
              p.value >= peak && peak > 0
                ? vGradient(BS_COLORS[0], hexA(BS_COLORS[2], 0.25))
                : vGradient(hexA(BS_COLORS[1], 0.85), hexA(BS_COLORS[1], 0.12)),
          },
          emphasis: { itemStyle: { shadowBlur: 18, shadowColor: hexA(BS_COLORS[0], 0.6) } },
        },
      ],
    })
  }

  /* 3. 领域覆盖 —— 横向条形 */
  const fieldEl = document.getElementById('expertField')
  if (fieldEl) {
    chartField = echarts.init(fieldEl)
    chartField.setOption({
      ...bsAnimation,
      ...bsEmpty(fieldData.values.length),
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
        data: fieldData.names,
        ...bsAxis,
        axisLine: { show: false },
        axisLabel: { color: '#9db8d6', fontSize: 11 },
      },
      series: [
        {
          name: '专家人数',
          type: 'bar',
          barWidth: 10,
          data: fieldData.values,
          label: {
            show: true,
            position: 'right',
            distance: 8,
            formatter: '{c}',
            color: '#eaf7ff',
            fontSize: 11.5,
            fontWeight: 700,
          },
          itemStyle: {
            borderRadius: [0, 6, 6, 0],
            color: hGradient(hexA(BS_COLORS[2], 0.5), BS_COLORS[0]),
          },
          showBackground: true,
          backgroundStyle: { color: 'rgba(86,148,220,0.05)', borderRadius: [0, 6, 6, 0] },
          emphasis: { itemStyle: { shadowBlur: 14, shadowColor: hexA(BS_COLORS[0], 0.7) } },
        },
      ],
    })
  }

  /* 4. 满意度 —— 仪表盘 */
  const ratingEl = document.getElementById('expertRating')
  if (ratingEl) {
    chartRating = echarts.init(ratingEl)
    chartRating.setOption({
      ...bsAnimation,
      tooltip: bsTooltip({ trigger: 'item', formatter: '平均满意度：{c} 分' }),
      series: [
        {
          type: 'gauge',
          startAngle: 215,
          endAngle: -35,
          min: 0,
          max: 5,
          radius: '96%',
          center: ['50%', '56%'],
          progress: {
            show: true,
            width: 9,
            roundCap: true,
            itemStyle: { color: vGradient(BS_COLORS[3], BS_COLORS[0]) },
          },
          axisLine: { lineStyle: { width: 9, color: [[1, 'rgba(86,148,220,0.14)']] } },
          axisTick: { show: false },
          splitLine: { show: false },
          axisLabel: { show: false },
          pointer: { show: false },
          anchor: { show: false },
          title: { offsetCenter: [0, '34%'], color: '#6d8aac', fontSize: 11 },
          detail: {
            valueAnimation: true,
            offsetCenter: [0, '-2%'],
            formatter: (v: number) => v.toFixed(1),
            color: '#f2faff',
            fontSize: 26,
            fontWeight: 700,
            fontFamily: 'DIN Alternate, Bahnschrift, Arial',
          },
          data: [{ value: Number(stat.avgSatisfaction) || 0, name: '平均分' }],
        },
      ],
    })
  }
}

/* ================================================================
   自适应
   ================================================================ */
function resizeCharts() {
  chartTitle?.resize()
  chartTrend?.resize()
  chartField?.resize()
  chartRating?.resize()
}

onMounted(async () => {
  await loadChartData()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  ;[chartTitle, chartTrend, chartField, chartRating].forEach((c) => c?.dispose())
  window.removeEventListener('resize', resizeCharts)
})
</script>

<style scoped>
.bs-expert {
  grid-template-columns: repeat(6, minmax(0, 1fr));
  grid-template-rows: auto minmax(0, 1fr) minmax(0, 1fr);
}

.expert-kpis {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

/* ===== 满意度面板 ===== */
.expert-rating {
  gap: 14px;
  align-items: stretch;
}

.expert-rating__gauge {
  position: relative;
  flex: 0 0 42%;
  min-width: 0;
}

.expert-rating__gauge > .bs-chart {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.expert-rating__list {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.expert-rating__stars {
  display: flex;
  gap: 4px;
  color: rgba(120, 170, 230, 0.28);
  font-size: 15px;
}

.expert-rating__stars .is-on {
  color: #f5a524;
  filter: drop-shadow(0 0 6px rgba(245, 165, 36, 0.7));
}

.expert-rating__row {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: var(--bs-sub);
}

.expert-rating__name {
  flex: 0 0 38px;
  color: #9db8d6;
}

.expert-rating__track {
  position: relative;
  flex: 1;
  height: 6px;
  border-radius: 3px;
  background: rgba(86, 148, 220, 0.12);
  overflow: hidden;
}

.expert-rating__track i {
  display: block;
  height: 100%;
  border-radius: 3px;
  background: linear-gradient(90deg, #38e1ff, #4f8cff);
  box-shadow: 0 0 10px rgba(56, 225, 255, 0.6);
  transition: width 1s cubic-bezier(0.2, 0.9, 0.3, 1);
}

.expert-rating__row b {
  flex: 0 0 18px;
  text-align: right;
  font-size: 12.5px;
  color: #eaf7ff;
  font-variant-numeric: tabular-nums;
}

.expert-rating__empty {
  margin: 0;
  font-size: 12px;
  color: var(--bs-faint);
}

@media (max-width: 1360px) {
  .expert-kpis,
  .bs-expert {
    gap: 10px;
  }
}
</style>
