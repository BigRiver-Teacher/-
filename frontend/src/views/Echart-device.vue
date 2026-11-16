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
              <div class="logo">
                <el-icon :size="24" color="#409eff"><Management /></el-icon>
                <h2 style="cursor: pointer" @click="router.push('/home')">朝夕护行 设备管理</h2>
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
        <div class="bd">
          <!-- 左列 -->
          <div class="left">
            <div class="top">
              <div class="chart-title" @click="router.push('/home/device')" style="cursor: pointer">
                在线设备
              </div>
              <div id="deviceIng" class="chart-container"></div>
            </div>
            <div class="bottom">
              <div class="chart-title" @click="router.push('/home/device')" style="cursor: pointer">
                故障设备统计
              </div>
              <div id="deviceTrouble" class="chart-container"></div>
            </div>
          </div>

          <!-- 中列 -->
          <div class="middle">
            <div class="top">
              <div class="chart-title" @click="router.push('/home/device')" style="cursor: pointer">
                设备数量
              </div>
              <div id="device" class="chart-container"></div>
            </div>
            <div class="bottom">
              <div class="chart-title" @click="router.push('/home/device')" style="cursor: pointer">
                设备占比
              </div>
              <div id="deviceRound" class="chart-container"></div>
            </div>
          </div>

          <!-- 右列 -->
          <div class="right">
            <div class="top">
              <div
                class="chart-title"
                @click="router.push('/home/alert/list')"
                style="cursor: pointer"
              >
                告警次数
              </div>
              <div id="deviceWarning" class="chart-container"></div>
            </div>
            <div class="bottom">
              <div class="chart-title" @click="router.push('/home/device')" style="cursor: pointer">
                未绑定设备
              </div>
              <div id="deviceNothing" class="chart-container"></div>
            </div>
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { Management } from '@element-plus/icons-vue'
import { onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { useRouter } from 'vue-router'
import {
  getDeviceStatistics,
  getDeviceOnlineTrend,
  getDeviceAlertTrend,
  getDeviceFaultStatistics,
} from '@/api/stats'
import { alertTypeLabel } from '@/utils/echartLabels'

const router = useRouter()

// ===== 图表数据（后端统计接口填充） =====
let deviceStat = {
  totalDevices: 0,
  onlineDevices: 0,
  offlineDevices: 0,
  faultDevices: 0,
  bindRate: 0,
  todayAlertCount: 0,
}
let onlineTrend = { categories: [], series: [] }
let alertTrend = { categories: [], series: [] }
let faultStat = { categories: [], series: [] }

const TREND_GRADIENTS = [
  [
    { offset: 0, color: '#a8d8ff' },
    { offset: 1, color: '#409eff' },
  ],
  [
    { offset: 0, color: '#79bbff' },
    { offset: 1, color: '#337ecc' },
  ],
  [
    { offset: 0, color: '#337ecc' },
    { offset: 1, color: '#1a5a9c' },
  ],
  [
    { offset: 0, color: '#67c23a' },
    { offset: 1, color: '#95d475' },
  ],
]
const LINE_COLORS = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c']

// ===== 从后端加载图表数据 =====
async function loadChartData() {
  try {
    const res = await getDeviceStatistics()
    deviceStat = res.data.data || deviceStat
  } catch (e) {
    /* 后端不可用时保持空数据 */
  }
  try {
    const res = await getDeviceOnlineTrend()
    onlineTrend = res.data.data || onlineTrend
  } catch (e) {}
  try {
    const res = await getDeviceAlertTrend()
    alertTrend = res.data.data || alertTrend
  } catch (e) {}
  try {
    const res = await getDeviceFaultStatistics()
    const data = res.data.data
    if (data) {
      // 后端返回告警类型枚举 code（FALL/DEVICE_OFFLINE/...），转成中文再显示
      faultStat = {
        ...data,
        categories: (data.categories || []).map((c) => alertTypeLabel(c)),
      }
    }
  } catch (e) {}
  nextTick(() => initCharts())
}

let chartDevice = null
let chartDeviceRound = null
let chartDeviceIng = null
let chartDeviceWarning = null
let chartDeviceTrouble = null
let chartDeviceNothing = null

function initCharts() {
  // 销毁旧实例
  ;[
    chartDevice,
    chartDeviceRound,
    chartDeviceIng,
    chartDeviceWarning,
    chartDeviceTrouble,
    chartDeviceNothing,
  ].forEach((chart) => {
    if (chart) {
      chart.dispose()
    }
  })
  chartDevice = null
  chartDeviceRound = null
  chartDeviceIng = null
  chartDeviceWarning = null
  chartDeviceTrouble = null
  chartDeviceNothing = null

  // ===== 1. 设备数量（柱状图） =====
  const device = document.getElementById('device')
  if (device) {
    chartDevice = echarts.init(device)
    chartDevice.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        formatter: function (params) {
          const p = params[0]
          return `<strong>${p.name}</strong><br/>数量：${p.value} 台`
        },
      },
      grid: {
        left: '8%',
        right: '5%',
        bottom: '12%',
        top: '5%',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        data: ['设备总数', '在线设备', '离线设备', '故障设备'],
        axisTick: { alignWithLabel: true },
        axisLabel: {
          fontSize: 12,
          color: '#666',
        },
        axisLine: {
          lineStyle: { color: '#e8e8e8' },
        },
      },
      yAxis: {
        type: 'value',
        name: '数量（台）',
        nameTextStyle: { fontSize: 12, color: '#999' },
        axisLabel: { fontSize: 12, color: '#666' },
        splitLine: {
          lineStyle: { color: '#f0f0f0', type: 'dashed' },
        },
      },
      series: [
        {
          name: '数量',
          type: 'bar',
          barWidth: '50%',
          data: [
            deviceStat.totalDevices,
            deviceStat.onlineDevices,
            deviceStat.offlineDevices,
            deviceStat.faultDevices,
          ],
          itemStyle: {
            borderRadius: [4, 4, 0, 0],
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#409eff' },
              { offset: 1, color: '#79bbff' },
            ]),
          },
        },
      ],
    })
  }

  // ===== 2. 设备占比（饼图） =====
  const deviceRound = document.getElementById('deviceRound')
  if (deviceRound) {
    chartDeviceRound = echarts.init(deviceRound)
    chartDeviceRound.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c}台 ({d}%)',
      },
      legend: {
        orient: 'vertical',
        right: '5%',
        top: 'center',
        textStyle: { fontSize: 12, color: '#666' },
        itemWidth: 14,
        itemHeight: 14,
      },
      series: [
        {
          name: '设备占比',
          type: 'pie',
          radius: ['35%', '72%'],
          center: ['45%', '50%'],
          avoidLabelOverlap: false,
          padAngle: 2,
          itemStyle: {
            borderRadius: 6,
            borderColor: '#fff',
            borderWidth: 2,
          },
          label: {
            show: true,
            formatter: '{d}%',
            fontSize: 12,
            color: '#666',
          },
          labelLine: {
            length: 12,
            length2: 8,
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
          data: [
            { value: deviceStat.onlineDevices, name: '在线', itemStyle: { color: '#409eff' } },
            { value: deviceStat.offlineDevices, name: '离线', itemStyle: { color: '#e6a23c' } },
            { value: deviceStat.faultDevices, name: '故障', itemStyle: { color: '#f56c6c' } },
          ],
        },
      ],
    })
  }

  // ===== 3. 在线设备（横向柱状图） =====
  const deviceIng = document.getElementById('deviceIng')
  if (deviceIng) {
    chartDeviceIng = echarts.init(deviceIng)
    chartDeviceIng.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        formatter: function (params) {
          let html = `<strong>${params[0].name}</strong><br/>`
          params.forEach((p) => {
            html += `${p.seriesName}：${p.value} 台<br/>`
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
        top: '18%',
        containLabel: true,
      },
      xAxis: {
        type: 'value',
        name: '在线数量',
        nameTextStyle: { fontSize: 11, color: '#999' },
        axisLabel: { fontSize: 11, color: '#666' },
        splitLine: {
          lineStyle: { color: '#f0f0f0', type: 'dashed' },
        },
      },
      yAxis: {
        type: 'category',
        data: onlineTrend.categories,
        axisLabel: { fontSize: 12, color: '#666' },
        axisLine: {
          lineStyle: { color: '#e8e8e8' },
        },
      },
      series: onlineTrend.series.map((s, i) => ({
        name: s.name,
        type: 'bar',
        stack: 'total',
        data: s.data,
        itemStyle: {
          borderRadius: i === onlineTrend.series.length - 1 ? [0, 4, 4, 0] : 0,
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, TREND_GRADIENTS[i % TREND_GRADIENTS.length]),
        },
      })),
    })
  }

  // ===== 4. 告警次数（折线图） =====
  const deviceWarning = document.getElementById('deviceWarning')
  if (deviceWarning) {
    chartDeviceWarning = echarts.init(deviceWarning)
    chartDeviceWarning.setOption({
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
        data: alertTrend.categories,
        axisLabel: { fontSize: 11, color: '#666' },
        axisLine: {
          lineStyle: { color: '#e8e8e8' },
        },
      },
      yAxis: {
        type: 'value',
        name: '告警次数',
        nameTextStyle: { fontSize: 11, color: '#999' },
        axisLabel: { fontSize: 11, color: '#666' },
        splitLine: {
          lineStyle: { color: '#f0f0f0', type: 'dashed' },
        },
      },
      series: alertTrend.series.map((s) => ({
        name: s.name,
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 2 },
        areaStyle: {
          opacity: 0.15,
        },
        data: s.data,
      })),
    })
  }

  // ===== 5. 故障设备统计（横向柱状图） =====
  const deviceTrouble = document.getElementById('deviceTrouble')
  if (deviceTrouble) {
    chartDeviceTrouble = echarts.init(deviceTrouble)
    chartDeviceTrouble.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        formatter: function (params) {
          const p = params[0]
          return `<strong>${p.name}</strong><br/>故障数量：${p.value} 台`
        },
      },
      grid: {
        left: '10%',
        right: '12%',
        bottom: '8%',
        top: '5%',
        containLabel: true,
      },
      xAxis: {
        type: 'value',
        name: '故障数量',
        nameTextStyle: { fontSize: 11, color: '#999' },
        axisLabel: { fontSize: 11, color: '#666' },
        splitLine: {
          lineStyle: { color: '#f0f0f0', type: 'dashed' },
        },
      },
      yAxis: {
        type: 'category',
        data: faultStat.categories,
        inverse: true,
        axisLabel: { fontSize: 12, color: '#666' },
        axisLine: {
          lineStyle: { color: '#e8e8e8' },
        },
      },
      series: faultStat.series.map((s) => ({
        name: s.name,
        type: 'bar',
        data: s.data,
        barWidth: '50%',
        label: {
          show: true,
          position: 'right',
          fontSize: 12,
          color: '#f56c6c',
          fontWeight: 'bold',
        },
        itemStyle: {
          borderRadius: [0, 4, 4, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#fdaaaa' },
            { offset: 1, color: '#f56c6c' },
          ]),
        },
      })),
    })
  }

  // ===== 6. 未绑定设备（折线图） =====
  const deviceNothing = document.getElementById('deviceNothing')
  if (deviceNothing) {
    chartDeviceNothing = echarts.init(deviceNothing)
    chartDeviceNothing.setOption({
      tooltip: {
        trigger: 'axis',
        formatter: function (params) {
          let html = `<strong>${params[0].axisValue}</strong><br/>`
          params.forEach((p) => {
            html += `${p.marker} ${p.seriesName}：${p.value} 台<br/>`
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
        data: onlineTrend.categories,
        axisLabel: { fontSize: 11, color: '#666' },
        axisLine: {
          lineStyle: { color: '#e8e8e8' },
        },
      },
      yAxis: {
        type: 'value',
        name: '未绑定数量',
        nameTextStyle: { fontSize: 11, color: '#999' },
        axisLabel: { fontSize: 11, color: '#666' },
        splitLine: {
          lineStyle: { color: '#f0f0f0', type: 'dashed' },
        },
      },
      series: onlineTrend.series.map((s, i) => ({
        name: s.name,
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 2, color: LINE_COLORS[i % LINE_COLORS.length] },
        data: s.data,
      })),
    })
  }
}

function resizeCharts() {
  chartDevice?.resize()
  chartDeviceRound?.resize()
  chartDeviceIng?.resize()
  chartDeviceWarning?.resize()
  chartDeviceTrouble?.resize()
  chartDeviceNothing?.resize()
}

onMounted(() => {
  loadChartData()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  chartDevice?.dispose()
  chartDeviceRound?.dispose()
  chartDeviceIng?.dispose()
  chartDeviceWarning?.dispose()
  chartDeviceTrouble?.dispose()
  chartDeviceNothing?.dispose()
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
  background-color: #f0f2f5;
  overflow: hidden;
}

.full-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.el-header {
  background-color: #fff;
  padding: 12px 20px;
  border-radius: 8px;
  margin-bottom: 12px;
  height: 72px;
  flex-shrink: 0;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.el-main {
  flex: 1;
  padding: 0;
  overflow: hidden;
}

/* ===== 主布局 ===== */
.bd {
  width: 100%;
  height: 100%;
  display: flex;
  gap: 14px;
}

.left,
.right {
  width: 24%;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.middle {
  width: 49%;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.top,
.bottom {
  flex: 1;
  background: #ffffff;
  border-radius: 10px;
  padding: 12px 14px 10px 14px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: box-shadow 0.3s;
}

.top:hover,
.bottom:hover {
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.1);
}

.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  padding-bottom: 6px;
  border-bottom: 2px solid #f0f0f0;
  margin-bottom: 6px;
  flex-shrink: 0;
  letter-spacing: 0.5px;
}

.chart-title::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 16px;
  background: #409eff;
  border-radius: 2px;
  margin-right: 8px;
  vertical-align: middle;
}

.chart-container {
  flex: 1;
  width: 100%;
  min-height: 260px;
  height: 260px;
}

/* ===== 顶部导航 ===== */
.grid-content {
  height: 44px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  transition: all 0.3s;
}

.grid-content:hover {
  background: #b3d8ff !important;
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
  color: #409eff;
  letter-spacing: 2px;
}

/* ===== 响应式 ===== */
@media (max-width: 1200px) {
  .bd {
    flex-wrap: wrap;
    gap: 12px;
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
  .chart-title {
    font-size: 13px;
  }
  .top,
  .bottom {
    padding: 8px 10px;
  }
}
</style>