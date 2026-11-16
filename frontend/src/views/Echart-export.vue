<template>
  <div class="common-layout">
    <el-container class="full-container">
      <!-- ===== 顶部导航栏 ===== -->
      <el-header>
        <el-row class="row-bg" justify="space-evenly" align="middle">
          <el-col :span="3">
            <div
              class="grid-content ep-bg-purple"
              style="cursor: pointer"
              @click="router.push('/echart/person')"
            >
              人员管理
            </div>
          </el-col>
          <el-col :span="3">
            <div
              class="grid-content ep-bg-purple"
              style="cursor: pointer"
              @click="router.push('/echart/export')"
            >
              专家管理
            </div>
          </el-col>
          <el-col :span="6">
            <div class="grid-content ep-bg-purple-light">
              <div class="logo">
                <el-icon :size="24" color="#409eff"><Management /></el-icon>
                <h2 style="cursor: pointer" @click="router.push('/home')">朝夕护行 专家管理</h2>
              </div>
            </div>
          </el-col>
          <el-col :span="3">
            <div
              class="grid-content ep-bg-purple"
              style="cursor: pointer"
              @click="router.push('/echart/monitor')"
            >
              监控管理
            </div>
          </el-col>
          <el-col :span="3">
            <div
              class="grid-content ep-bg-purple"
              style="cursor: pointer"
              @click="router.push('/echart/device')"
            >
              设备管理
            </div>
          </el-col>
        </el-row>
      </el-header>

      <el-main>
        <div class="total">
          <!-- ===== 左列 ===== -->
          <div class="left">
            <div class="card card-top">
              <div class="card-title">
                <span class="title-dot"></span>
                专家职称分布
              </div>
              <div id="expertTitle" class="chart-container"></div>
            </div>
            <div class="card card-bottom">
              <div class="card-title">
                <span class="title-dot"></span>
                各领域专家数量
              </div>
              <div id="expertField" class="chart-container"></div>
            </div>
          </div>

          <!-- ===== 中列 ===== -->
          <div class="middle">
            <div class="card card-stat">
              <div class="stats-label">专家总数</div>
              <div class="stats-number">
                <span class="number">{{ statCards.totalExperts }}</span>
                <span class="number-unit">人</span>
              </div>
              <div class="stats-sub">较上月 <span class="up">↑ 5.8%</span></div>
            </div>
            <div class="card card-chart">
              <div class="card-title">
                <span class="title-dot"></span>
                专家类型占比
              </div>
              <div id="expertType" class="chart-container"></div>
            </div>
          </div>

          <!-- ===== 右列 ===== -->
          <div class="right">
            <div class="card card-stat-mini">
              <div class="stat-mini-label">本月咨询量</div>
              <div class="stat-mini-number">
                <span class="mini-number">{{ statCards.totalConsultations.toLocaleString() }}</span>
                <span class="mini-unit">次</span>
              </div>
              <div class="stats-sub" style="text-align: center">
                较上月 <span class="up">↑ 12.3%</span>
              </div>
            </div>
            <div class="card card-chart">
              <div class="card-title">
                <span class="title-dot"></span>
                专家咨询趋势
              </div>
              <div id="expertTrend" class="chart-container"></div>
            </div>
            <div class="card card-bottom">
              <div class="card-title">
                <span class="title-dot"></span>
                满意度评分
              </div>
              <div id="expertRating" class="chart-container-right" style="height: 65%"></div>
              <div class="detail-btn" @click="router.push('/home/expert')">
                <span>进入专家管理</span>
                <el-icon><ArrowRight /></el-icon>
              </div>
            </div>
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { Management, ArrowRight } from '@element-plus/icons-vue'
import { onMounted, onUnmounted, nextTick, reactive } from 'vue'
import * as echarts from 'echarts'
import { useRouter } from 'vue-router'
import {
  getExpertStatistics,
  getExpertTitleDistribution,
  getExpertFieldDistribution,
  getExpertConsultationTrend,
  getExpertSatisfactionDistribution,
} from '@/api/stats'

const router = useRouter()

// ===== 统计卡片（后端统计接口填充） =====
const statCards = reactive({
  totalExperts: 0,
  totalConsultations: 0,
})

// ===== 图表数据（后端统计接口填充） =====
let titleDist = []
let fieldDist = { categories: [], lineSeries: [], barSeries: [] }
let trendDist = { categories: [], lineSeries: [], barSeries: [] }
let satisfactionDist = []

const FIELD_GRADIENTS: Array<Array<{ offset: number; color: string }>> = [
  [
    { offset: 0, color: '#79bbff' },
    { offset: 1, color: '#409eff' },
  ],
  [
    { offset: 0, color: '#67c23a' },
    { offset: 1, color: '#95d475' },
  ],
  [
    { offset: 0, color: '#e6a23c' },
    { offset: 1, color: '#f0c78a' },
  ],
  [
    { offset: 0, color: '#f56c6c' },
    { offset: 1, color: '#f89898' },
  ],
  [
    { offset: 0, color: '#909399' },
    { offset: 1, color: '#b0b3b8' },
  ],
]

// ===== 从后端加载图表数据 =====
async function loadChartData() {
  try {
    const res = await getExpertStatistics()
    const stat = res.data.data
    if (stat) {
      statCards.totalExperts = stat.totalExperts || 0
      statCards.totalConsultations = stat.totalConsultations || 0
    }
  } catch (e) {}
  try {
    const res = await getExpertTitleDistribution()
    titleDist = res.data.data || []
  } catch (e) {}
  try {
    const res = await getExpertFieldDistribution()
    fieldDist = res.data.data || { categories: [], lineSeries: [], barSeries: [] }
  } catch (e) {}
  try {
    const res = await getExpertConsultationTrend()
    trendDist = res.data.data || { categories: [], lineSeries: [], barSeries: [] }
  } catch (e) {}
  try {
    const res = await getExpertSatisfactionDistribution()
    satisfactionDist = res.data.data || []
  } catch (e) {}
  nextTick(() => initChart())
}

let chartExpertTitle = null
let chartExpertField = null
let chartExpertType = null
let chartExpertTrend = null
let chartExpertRating = null

function initChart() {
  // 销毁旧实例
  const charts = [
    chartExpertTitle,
    chartExpertField,
    chartExpertType,
    chartExpertTrend,
    chartExpertRating,
  ]
  charts.forEach((chart) => {
    if (chart) {
      chart.dispose()
      chart = null
    }
  })

  // ===== 1. 专家职称分布（饼图） =====
  const expertTitle = document.getElementById('expertTitle')
  if (expertTitle) {
    chartExpertTitle = echarts.init(expertTitle)
    chartExpertTitle.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c}人 ({d}%)',
      },
      legend: {
        orient: 'vertical',
        right: '5%',
        top: 'center',
        textStyle: { fontSize: 11, color: '#666' },
        itemWidth: 12,
        itemHeight: 12,
      },
      color: ['#f56c6c', '#e6a23c', '#409eff', '#67c23a', '#909399'],
      series: [
        {
          name: '职称分布',
          type: 'pie',
          radius: ['35%', '68%'],
          center: ['45%', '55%'],
          label: {
            show: true,
            formatter: '{d}%',
            fontSize: 11,
            color: '#666',
          },
          labelLine: {
            length: 10,
            length2: 6,
          },
          emphasis: {
            scale: true,
            scaleSize: 6,
          },
          data: titleDist,
        },
      ],
    })
  }

  // ===== 2. 各领域专家数量（横向柱状图） =====
  const expertField = document.getElementById('expertField')
  if (expertField) {
    chartExpertField = echarts.init(expertField)
    chartExpertField.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        formatter: function (params) {
          const p = params[0]
          return `<strong>${p.name}</strong><br/>专家数量：${p.value} 人`
        },
      },
      grid: {
        left: '10%',
        right: '8%',
        bottom: '5%',
        top: '5%',
        containLabel: true,
      },
      xAxis: {
        type: 'value',
        name: '人数',
        nameTextStyle: { fontSize: 11, color: '#999' },
        axisLabel: { fontSize: 11, color: '#666' },
        splitLine: {
          lineStyle: { color: '#f0f0f0', type: 'dashed' },
        },
      },
      yAxis: {
        type: 'category',
        data: fieldDist.categories,
        axisLabel: { fontSize: 12, color: '#666' },
        axisLine: { lineStyle: { color: '#e8e8e8' } },
      },
      series: fieldDist.barSeries.map((s) => ({
        name: s.name,
        type: 'bar',
        barWidth: '45%',
        data: s.data.map((v, j) => ({
          value: v,
          itemStyle: {
            borderRadius: [0, 4, 4, 0],
            color: new echarts.graphic.LinearGradient(
              0,
              0,
              1,
              0,
              FIELD_GRADIENTS[j % FIELD_GRADIENTS.length],
            ),
          },
        })),
        label: {
          show: true,
          position: 'right',
          fontSize: 12,
          fontWeight: 500,
          color: '#333',
          formatter: '{c}人',
        },
      })),
    })
  }

  // ===== 3. 专家类型占比（环形图） =====
  const expertType = document.getElementById('expertType')
  if (expertType) {
    chartExpertType = echarts.init(expertType)
    chartExpertType.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c}人 ({d}%)',
      },
      legend: {
        orient: 'vertical',
        right: '5%',
        top: 'center',
        textStyle: { fontSize: 11, color: '#666' },
        itemWidth: 12,
        itemHeight: 12,
      },
      color: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399'],
      series: [
        {
          name: '专家类型',
          type: 'pie',
          radius: ['40%', '72%'],
          center: ['45%', '55%'],
          label: {
            show: true,
            formatter: '{d}%',
            fontSize: 11,
            color: '#666',
          },
          labelLine: {
            length: 10,
            length2: 6,
          },
          emphasis: {
            scale: true,
            scaleSize: 6,
          },
          data: titleDist,
        },
      ],
    })
  }

  // ===== 4. 专家咨询趋势（折线图） =====
  const expertTrend = document.getElementById('expertTrend')
  if (expertTrend) {
    chartExpertTrend = echarts.init(expertTrend)
    chartExpertTrend.setOption({
      tooltip: {
        trigger: 'axis',
        formatter: function (params) {
          let html = `<strong>${params[0].axisValue}</strong><br/>`
          params.forEach((p) => {
            html += `${p.marker} ${p.seriesName}：${p.value} 次<br/>`
          })
          return html
        },
      },
      legend: {
        top: 0,
        right: 0,
        textStyle: { fontSize: 11, color: '#666' },
        itemWidth: 12,
        itemHeight: 12,
      },
      grid: {
        left: '8%',
        right: '5%',
        bottom: '10%',
        top: '18%',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: trendDist.categories,
        axisLabel: { fontSize: 11, color: '#666' },
        axisLine: { lineStyle: { color: '#e8e8e8' } },
      },
      yAxis: {
        type: 'value',
        name: '咨询量',
        nameTextStyle: { fontSize: 11, color: '#999' },
        axisLabel: { fontSize: 11, color: '#666' },
        splitLine: {
          lineStyle: { color: '#f0f0f0', type: 'dashed' },
        },
      },
      series: (trendDist.lineSeries.length > 0 ? trendDist.lineSeries : trendDist.barSeries).map(
        (s) => ({
          name: s.name,
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 8,
          lineStyle: {
            width: 3,
            color: '#409eff',
          },
          itemStyle: {
            color: '#409eff',
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(64,158,255,0.3)' },
              { offset: 1, color: 'rgba(64,158,255,0.02)' },
            ]),
          },
          data: s.data,
        }),
      ),
    })
  }

  // ===== 5. 满意度评分（环形图） =====
  const expertRating = document.getElementById('expertRating')
  if (expertRating) {
    chartExpertRating = echarts.init(expertRating)
    chartExpertRating.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c}人 ({d}%)',
      },
      legend: {
        orient: 'vertical',
        right: '5%',
        top: 'center',
        textStyle: { fontSize: 11, color: '#666' },
        itemWidth: 12,
        itemHeight: 12,
      },
      color: ['#67c23a', '#409eff', '#e6a23c', '#f56c6c', '#909399'],
      series: [
        {
          name: '满意度评分',
          type: 'pie',
          radius: ['40%', '72%'],
          center: ['45%', '55%'],
          label: {
            show: true,
            formatter: '{d}%',
            fontSize: 11,
            color: '#666',
          },
          labelLine: {
            length: 10,
            length2: 6,
          },
          emphasis: {
            scale: true,
            scaleSize: 6,
          },
          data: satisfactionDist,
        },
      ],
    })
  }
}

function resizeCharts() {
  chartExpertTitle?.resize()
  chartExpertField?.resize()
  chartExpertType?.resize()
  chartExpertTrend?.resize()
  chartExpertRating?.resize()
}

onMounted(() => {
  loadChartData()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  chartExpertTitle?.dispose()
  chartExpertField?.dispose()
  chartExpertType?.dispose()
  chartExpertTrend?.dispose()
  chartExpertRating?.dispose()
  window.removeEventListener('resize', resizeCharts)
})
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html,
body {
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.common-layout {
  width: 100vw;
  height: 100vh;
  padding: 16px;
  background: linear-gradient(135deg, #e8edf5 0%, #d5dce6 100%);
  overflow: hidden;
}

.full-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.el-header {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  padding: 12px 20px;
  border-radius: 12px;
  margin-bottom: 12px;
  height: 72px;
  flex-shrink: 0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.el-main {
  flex: 1;
  padding: 0;
  overflow: hidden;
}

/* ===== 主布局 ===== */
.total {
  display: flex;
  gap: 14px;
  width: 100%;
  height: 100%;
}

.left,
.right {
  width: 24%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.middle {
  width: 50%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* ===== 卡片样式 ===== */
.card {
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(10px);
  border-radius: 14px;
  padding: 14px 18px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.5);
  transition: all 0.3s ease;
}

.card:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.left .card-top,
.left .card-bottom {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.middle .card-stat {
  height: 22%;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.middle .card-chart {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.right .card-stat-mini {
  height: 18%;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.right .card-chart {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.right .card-bottom {
  height: 28%;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}

.chart-container {
  flex: 1;
  width: 100%;
  min-height: 220px;
  height: 220px;
}
.chart-container {
  flex: 1;
  width: 100%;
  min-height: 200px;
  height: 200px;
}

/* ===== 卡片标题 ===== */
.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 6px;
  border-bottom: 2px solid #f0f0f0;
  margin-bottom: 6px;
  flex-shrink: 0;
}

.title-dot {
  display: inline-block;
  width: 4px;
  height: 16px;
  background: linear-gradient(180deg, #409eff, #79bbff);
  border-radius: 2px;
}

/* ===== 统计数字样式 ===== */
.stats-label {
  font-size: 13px;
  color: #999;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.stats-number {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin: 2px 0;
}

.number {
  font-size: 42px;
  font-weight: 700;
  color: #1a1a1a;
  font-family: 'DIN Alternate', 'Arial', sans-serif;
  letter-spacing: 1px;
  line-height: 1.1;
  background: linear-gradient(135deg, #1a1a1a 60%, #409eff);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.number-unit {
  font-size: 18px;
  color: #999;
  font-weight: 400;
}

.stats-sub {
  font-size: 13px;
  color: #999;
}

.stats-sub .up {
  color: #67c23a;
  font-weight: 600;
}

.stats-sub .down {
  color: #f56c6c;
  font-weight: 600;
}

/* ===== 小统计卡片 ===== */
.stat-mini-label {
  font-size: 13px;
  color: #999;
  font-weight: 500;
}

.stat-mini-number {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.mini-number {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a1a;
  font-family: 'DIN Alternate', 'Arial', sans-serif;
  background: linear-gradient(135deg, #1a1a1a 60%, #e6a23c);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.mini-unit {
  font-size: 16px;
  color: #999;
}

/* ===== 跳转按钮 ===== */
.detail-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 28%;
  margin-top: 4px;
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
  border-radius: 8px;
  cursor: pointer;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 10px rgba(64, 158, 255, 0.3);
}

.detail-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.4);
}

.detail-btn span {
  letter-spacing: 1px;
}

.detail-btn .el-icon {
  font-size: 16px;
}

/* ===== 顶部导航 ===== */
.grid-content {
  height: 44px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  transition: all 0.3s ease;
}

.grid-content:hover {
  background: #b3d8ff !important;
  transform: scale(1.02);
}

.ep-bg-purple {
  background: #e6f7ff;
  border: 1px solid #91d5ff;
}

.ep-bg-purple-light {
  background: #f6ffed;
  border: 1px solid #b7eb8f;
}

.row-bg {
  height: 100%;
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 20px;
}

.logo h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  background: linear-gradient(135deg, #409eff, #1a5a9c);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 2px;
}

/* ===== 响应式 ===== */
@media (max-width: 1200px) {
  .total {
    flex-wrap: wrap;
  }
  .left,
  .right {
    width: 49%;
  }
  .middle {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .left,
  .right {
    width: 100%;
  }
  .middle {
    width: 100%;
  }
  .common-layout {
    padding: 8px;
  }
  .el-header {
    height: auto;
    padding: 8px 12px;
  }
  .number {
    font-size: 32px;
  }
  .mini-number {
    font-size: 24px;
  }
}
</style>
