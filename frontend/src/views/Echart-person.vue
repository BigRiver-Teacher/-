<template>
  <div class="common-layout">
    <el-container class="full-container">
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
              <div class="logo" style="cursor: pointer" @click="router.push('/home')">
                <el-icon :size="24" color="#409eff"><Management /></el-icon>
                <h2>朝夕护行 人员管理</h2>
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
                服务人员统计
              </div>
              <div id="personAround" class="chart-container"></div>
            </div>
            <div class="card card-bottom">
              <div class="card-title">
                <span class="title-dot"></span>
                陪护人员分布
              </div>
              <div id="personPosition" class="chart-container"></div>
            </div>
          </div>

          <!-- ===== 中列 ===== -->
          <div class="middle">
            <div class="card card-stat">
              <div class="stats-label">在职员工总数</div>
              <div class="stats-number">
                <span class="number">680</span>
                <span class="number-unit">人</span>
              </div>
              <div class="stats-sub">较上月 <span class="up">↑ 3.2%</span></div>
            </div>
            <div class="card card-chart">
              <div class="card-title">
                <span class="title-dot"></span>
                人员占比
              </div>
              <div id="personPercent" class="chart-container"></div>
            </div>
          </div>

          <!-- ===== 右列 ===== -->
          <div class="right">
            <div class="card card-stat-mini">
              <div class="stat-mini-label">服务对象总数</div>
              <div class="stat-mini-number">
                <span class="mini-number">1080</span>
                <span class="mini-unit">人</span>
              </div>
            </div>
            <div class="card card-chart">
              <div class="card-title">
                <span class="title-dot"></span>
                服务对象年龄分布
              </div>
              <div id="personAge" class="chart-container"></div>
            </div>
            <div class="card card-bottom">
              <div class="card-title">
                <span class="title-dot"></span>
                性别分布
              </div>
              <div id="personSex" class="chart-container-right"></div>
              <div class="detail-btn" @click="router.push('/home/elder/base')">
                <span>跳转详情页</span>
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
import { onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { useRouter } from 'vue-router'
import {
  getPersonTrend,
  getPersonRatio,
  getGenderDistribution,
  getAgeDistribution,
  getCaregiverDistribution,
} from '@/api/stats'

const router = useRouter()

// ===== 图表数据（后端统计接口填充） =====
let personRatio = []
let personTrend = { categories: [], series: [] }
let caregiverDist = { indicator: [], series: [] }
let ageDist = []
let genderDist = []

// ===== 从后端加载图表数据 =====
async function loadChartData() {
  try {
    const res = await getPersonRatio()
    personRatio = res.data.data || []
  } catch (e) {}
  try {
    const res = await getPersonTrend()
    personTrend = res.data.data || { categories: [], series: [] }
  } catch (e) {}
  try {
    const res = await getCaregiverDistribution()
    caregiverDist = res.data.data || { indicator: [], series: [] }
  } catch (e) {}
  try {
    const res = await getAgeDistribution()
    ageDist = res.data.data || []
  } catch (e) {}
  try {
    const res = await getGenderDistribution()
    genderDist = res.data.data || []
  } catch (e) {}
  nextTick(() => initChart())
}

let chartPersonPercent = null
let chartPersonAround = null
let chartPersonPosition = null
let chartPersonAge = null
let chartPersonSex = null

function initChart() {
  // 销毁旧实例
  const charts = [
    chartPersonPercent,
    chartPersonAround,
    chartPersonPosition,
    chartPersonAge,
    chartPersonSex,
  ]
  charts.forEach((chart) => {
    if (chart) {
      chart.dispose()
      chart = null
    }
  })

  // ===== 1. 人员占比（饼图） =====
  const personPercent = document.getElementById('personPercent')
  if (personPercent) {
    chartPersonPercent = echarts.init(personPercent)
    chartPersonPercent.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} ({d}%)',
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
          name: '人员占比',
          type: 'pie',
          radius: ['40%', '72%'],
          center: ['45%', '55%'],
          avoidLabelOverlap: false,
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
            label: {
              show: true,
              fontSize: 16,
              fontWeight: 'bold',
            },
            scale: true,
            scaleSize: 6,
          },
          data: personRatio,
        },
      ],
    })
  }

  // ===== 2. 服务人员统计（横向柱状图） =====
  const personAround = document.getElementById('personAround')
  if (personAround) {
    chartPersonAround = echarts.init(personAround)
    chartPersonAround.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        formatter: function (params) {
          let html = `<strong>${params[0].name}</strong><br/>`
          params.forEach((p) => {
            html += `${p.marker} ${p.seriesName}：${p.value} 人<br/>`
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
        left: '10%',
        right: '5%',
        bottom: '5%',
        top: '15%',
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
        data: personTrend.categories,
        axisLabel: { fontSize: 12, color: '#666' },
        axisLine: { lineStyle: { color: '#e8e8e8' } },
      },
      series: personTrend.series.map((s, i) => ({
        name: s.name,
        type: 'bar',
        stack: 'total',
        data: s.data,
        itemStyle: {
          borderRadius: i === personTrend.series.length - 1 ? [0, 4, 4, 0] : 0,
          color: new echarts.graphic.LinearGradient(
            0,
            0,
            1,
            0,
            i % 2 === 0
              ? [
                  { offset: 0, color: '#a8d8ff' },
                  { offset: 1, color: '#409eff' },
                ]
              : [
                  { offset: 0, color: '#79bbff' },
                  { offset: 1, color: '#337ecc' },
                ],
          ),
        },
      })),
    })
  }

  // ===== 3. 陪护人员分布（雷达图） =====
  const personPosition = document.getElementById('personPosition')
  if (personPosition) {
    chartPersonPosition = echarts.init(personPosition)
    // 后端 indicator 的 max 固定为 100，而实际人数只有 1~2 人，直接用会把图形缩成一个点；
    // 这里按当前数据最大值动态计算雷达刻度上限。
    const radarValues: number[] =
      ((caregiverDist.series?.[0]?.value as number[] | undefined) || [])
    const radarPeak = radarValues.length > 0 ? Math.max(...radarValues) : 0
    const radarMax = Math.max(1, Math.ceil(radarPeak * 1.2))
    const radarIndicator = (caregiverDist.indicator || []).map((ind: any) => ({
      name: ind.name,
      max: radarMax,
    }))
    chartPersonPosition.setOption({
      tooltip: {
        trigger: 'item',
        formatter: function (params: { name: any; value: any[] }) {
          let html = `<strong>${params.name}</strong><br/>`
          const labels = caregiverDist.indicator.map((ind) => ind.name)
          params.value.forEach((v: any, i: number) => {
            html += `${labels[i] || '维度' + (i + 1)}：${v} 人<br/>`
          })
          return html
        },
      },
      legend: {
        data: caregiverDist.series.map((s) => s.name),
        top: 0,
        right: 0,
        textStyle: { fontSize: 11, color: '#666' },
        itemWidth: 11,
        itemHeight: 12,
      },
      radar: {
        indicator: radarIndicator,
        shape: 'circle',
        center: ['45%', '55%'],
        radius: '72%',
        axisName: {
          color: '#666',
          fontSize: 11,
        },
        splitArea: {
          areaStyle: {
            color: ['rgba(64,158,255,0.02)', 'rgba(64,158,255,0.02)'],
          },
        },
        axisLine: {
          lineStyle: {
            color: 'rgba(64,158,255,0.2)',
          },
        },
      },
      series: [
        {
          name: '陪护人员分布',
          type: 'radar',
          data: caregiverDist.series.map((s, i) => ({
            value: s.value,
            name: s.name,
            areaStyle: {
              color: i % 2 === 0 ? 'rgba(64,158,255,0.2)' : 'rgba(103,194,58,0.2)',
            },
            lineStyle: {
              color: i % 2 === 0 ? '#409eff' : '#67c23a',
              width: 2,
            },
            itemStyle: {
              color: i % 2 === 0 ? '#409eff' : '#67c23a',
            },
          })),
        },
      ],
    })
  }

  // ===== 4. 服务对象年龄分布（饼图） =====
  const personAge = document.getElementById('personAge')
  if (personAge) {
    chartPersonAge = echarts.init(personAge)
    chartPersonAge.setOption({
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
          name: '年龄分布',
          type: 'pie',
          radius: ['35%', '65%'],
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
          data: ageDist,
        },
      ],
    })
  }

  // ===== 5. 性别分布（半圆饼图） =====
  const personSex = document.getElementById('personSex')
  if (personSex) {
    chartPersonSex = echarts.init(personSex)
    chartPersonSex.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c}人 ({d}%)',
      },
      legend: {
        top: '5%',
        left: 'center',
        textStyle: { fontSize: 11, color: '#666' },
        itemWidth: 12,
        itemHeight: 12,
      },
      color: ['#409eff', '#f56c6c'],
      series: [
        {
          name: '性别分布',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['50%', '70%'],
          startAngle: 180,
          endAngle: 360,
          label: {
            show: true,
            formatter: '{d}%',
            fontSize: 12,
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
          data: genderDist,
        },
      ],
    })
  }
}

function resizeCharts() {
  chartPersonPercent?.resize()
  chartPersonAround?.resize()
  chartPersonPosition?.resize()
  chartPersonAge?.resize()
  chartPersonSex?.resize()
}

onMounted(() => {
  loadChartData()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  chartPersonPercent?.dispose()
  chartPersonAround?.dispose()
  chartPersonPosition?.dispose()
  chartPersonAge?.dispose()
  chartPersonSex?.dispose()
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
.chart-container-right {
  flex: 1;
  width: 100%;
  /* min-height: 260px; */
  height: 220px;
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
