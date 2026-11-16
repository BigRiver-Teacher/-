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
                <h2 style="cursor: pointer" @click="router.push('/home')">朝夕护行 监控管理</h2>
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

      <!-- ===== 主内容区域 ===== -->
      <el-main>
        <!-- ===== 第一行：左 + 右 ===== -->
        <div class="row-container">
          <!-- 左上方盒子 - 监控1 -->
          <div class="box box-left-top">
            <div class="box-content">
              <h3>监控1：</h3>
              <MonitorVideo slot="monitor-1" />
            </div>
          </div>

          <!-- 右上方盒子 -->
          <div class="box box-right-top">
            <div class="sub-box sub-box-left">
              <div style="width: 100%; height: 100%; padding: 10px">
                <h2 style="text-align: center; margin: 0 0 4px 0; font-size: 16px">设备状态统计</h2>
                <div id="monitoring" style="width: 100%; height: 85%"></div>
              </div>
            </div>
            <div class="sub-box sub-box-right">
              <div class="sub-sub-box sub-sub-top">
                <div style="width: 100%; height: 100%; padding: 6px">
                  <h3 style="text-align: center; margin: 0 0 4px 0; font-size: 14px">加速度曲线</h3>
                  <div id="acceleration" style="width: 100%; height: calc(100% - 28px)"></div>
                </div>
              </div>
              <div class="sub-sub-box sub-sub-bottom">
                <div style="width: 100%; height: 100%; padding: 6px">
                  <h3 style="text-align: center; margin: 0 0 4px 0; font-size: 14px">心跳曲线</h3>
                  <div id="heart" style="width: 100%; height: calc(100% - 28px)"></div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- ===== 第二行：左 + 右 ===== -->
        <div class="row-container">
          <!-- 左下方盒子 - 监控2 -->
          <div class="box box-left-bottom">
            <div class="box-content">
              <h3>监控2：</h3>
              <MonitorVideo slot="monitor-2" />
            </div>
          </div>

          <div class="box box-right-bottom">
            <!-- 基本信息区域 -->
            <div
              class="sub-box sub-box-left"
              style="flex-direction: column; padding: 12px 16px; overflow: hidden"
            >
              <h2 style="font-size: 16px; margin-bottom: 6px; width: 100%; text-align: center">
                对象基本信息
              </h2>
              <div v-if="currentElder" style="width: 100%">
                <div class="info-row">
                  <span class="info-label">身份码：</span>
                  <span class="info-value code">{{ currentElder.code }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">姓名：</span>
                  <span class="info-value">{{ currentElder.name }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">年龄：</span>
                  <span class="info-value">{{ currentElder.age }}岁</span>
                </div>
                <div class="info-row">
                  <span class="info-label">居住地址：</span>
                  <el-tooltip :content="currentElder.address" placement="top" effect="dark">
                    <span class="info-value ellipsis">{{ currentElder.address }}</span>
                  </el-tooltip>
                </div>
                <div class="info-row">
                  <span class="info-label">专家备注：</span>
                  <el-tooltip :content="currentElder.remark" placement="top" effect="dark">
                    <span class="info-value ellipsis remark">{{ currentElder.remark }}</span>
                  </el-tooltip>
                </div>
                <div class="info-row" style="border-bottom: none">
                  <span class="info-label">既往跌倒历史：</span>
                  <span class="info-value link-btn" @click="openFallHistory">查看详情</span>
                </div>
              </div>
              <div v-else style="color: #999; text-align: center; width: 100%; padding: 20px 0">
                暂无老人数据
              </div>
            </div>

            <!-- 右侧操作区域 -->
            <div class="sub-box sub-box-right">
              <div class="sub-sub-box sub-sub-top">
                <span
                  class="box-label"
                  style="
                    width: 100%;
                    height: 100%;
                    cursor: pointer;
                    font-size: 18px;
                    text-align: center;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                  "
                  @click="router.push('/home/elder/base')"
                  >进入基本详情页</span
                >
              </div>
              <div class="sub-sub-box sub-sub-mid" style="flex-direction: column; gap: 4px">
                <span style="font-size: 14px; font-weight: 600">联系相关人员</span>
                <div style="display: flex; gap: 10px">
                  <span
                    style="
                      cursor: pointer;
                      background-color: #fce4ec;
                      padding: 4px 12px;
                      border-radius: 4px;
                      font-size: 13px;
                    "
                    @click="router.push('/home/caregiver')"
                    >陪护人员</span
                  >
                  <span
                    style="
                      cursor: pointer;
                      background-color: #e3f2fd;
                      padding: 4px 12px;
                      border-radius: 4px;
                      font-size: 13px;
                    "
                    @click="router.push('/home/expert')"
                    >康养专家</span
                  >
                </div>
              </div>
              <div class="sub-sub-box sub-sub-bottom">
                <div
                  style="
                    width: 100%;
                    height: 100%;
                    cursor: pointer;
                    text-align: center;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                  "
                  @click="openSwitchDialog"
                >
                  <span style="font-size: 22px; font-weight: 600">切换下一位</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-main>
    </el-container>

    <!-- ===== 切换下一位弹窗 ===== -->
    <el-dialog
      v-model="switchDialogVisible"
      title="选择老人"
      width="800px"
      :close-on-click-modal="true"
    >
      <!-- 搜索栏 -->
      <div class="dialog-search">
        <el-input
          v-model="searchKeyword"
          placeholder="请输入姓名搜索"
          clearable
          prefix-icon="Search"
          style="width: 260px"
          @input="handleSearch"
          size="large"
        />
        <el-button
          type="primary"
          size="large"
          @click="handleSearch"
          style="width: 60px; height: 40px"
          >搜索</el-button
        >
        <el-button size="large" @click="resetSearch" style="width: 60px; height: 40px"
          >重置</el-button
        >
      </div>

      <!-- 筛选条件 -->
      <div class="dialog-filter">
        <el-form :model="filterForm" inline size="large">
          <el-form-item label="性别">
            <el-select
              v-model="filterForm.gender"
              placeholder="全部"
              clearable
              style="width: 120px"
              size="large"
            >
              <el-option label="全部" value="" />
              <el-option label="男" value="男" />
              <el-option label="女" value="女" />
            </el-select>
          </el-form-item>
          <el-form-item label="居住类型">
            <el-select
              v-model="filterForm.residenceType"
              placeholder="全部"
              clearable
              style="width: 140px"
              size="large"
            >
              <el-option label="全部" value="" />
              <el-option label="独居" value="独居" />
              <el-option label="与子女同住" value="与子女同住" />
              <el-option label="养老院" value="养老院" />
            </el-select>
          </el-form-item>
          <el-form-item label="年龄范围" style="width: 340px">
            <div style="display: flex; align-items: center; gap: 12px; width: 100%">
              <el-slider
                v-model="filterForm.ageRange"
                range
                :min="0"
                :max="120"
                :step="1"
                style="flex: 1"
                :marks="{ 0: '0', 60: '60', 120: '120' }"
              />
              <span style="font-weight: 600; color: #409eff; min-width: 70px">
                {{ filterForm.ageRange[0] }} - {{ filterForm.ageRange[1] }} 岁
              </span>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              @click="applyFilter"
              style="width: 60px; height: 40px"
              >筛选</el-button
            >
            <el-button size="large" @click="resetFilter" style="width: 60px; height: 40px"
              >重置</el-button
            >
          </el-form-item>
        </el-form>
      </div>

      <!-- 老人列表 -->
      <el-table
        :data="paginatedElders"
        stripe
        border
        style="width: 100%; max-height: 350px; overflow-y: auto"
      >
        <el-table-column prop="code" label="身份码" width="130" align="center">
          <template #default="{ row }">
            <span style="font-family: monospace; font-size: 13px; color: #409eff">{{
              row.code
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" width="90" align="center" />
        <el-table-column prop="gender" label="性别" width="70" align="center" />
        <el-table-column prop="age" label="年龄" width="70" align="center" />
        <el-table-column prop="idCard" label="身份证号" width="160" align="center">
          <template #default="{ row }">
            <span style="font-family: monospace; font-size: 13px">{{ row.idCard }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="120" align="center" />
        <el-table-column prop="address" label="居住地址" min-width="150">
          <template #default="{ row }">
            <el-tooltip :content="row.address" placement="top">
              <span class="address-text">{{ row.address }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="large" type="primary" style="width: 80%" @click="selectElder(row)">
              选择
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="dialog-pagination">
        <el-pagination
          background
          v-model:current-page="dialogPage"
          v-model:page-size="dialogPageSize"
          :page-sizes="[5, 10, 20]"
          :total="dialogTotal"
          layout="total, sizes, prev, pager, next"
          @size-change="dialogPage = 1"
          @current-change="handleDialogPageChange"
          size="large"
        />
      </div>

      <template #footer>
        <el-button size="large" @click="switchDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- ===== 既往跌倒历史弹窗 ===== -->
    <el-dialog
      v-model="fallHistoryVisible"
      title="既往跌倒历史"
      width="650px"
      :close-on-click-modal="true"
    >
      <div v-if="currentElder">
        <div style="margin-bottom: 12px; font-weight: 600; color: #333; font-size: 16px">
          {{ currentElder.name }}（{{ currentElder.code }}） 的跌倒记录
        </div>
        <el-table :data="fallHistoryData" stripe border style="width: 100%">
          <el-table-column prop="date" label="跌倒时间" width="150" align="center" />
          <el-table-column prop="location" label="跌倒地点" width="120" align="center" />
          <el-table-column prop="cause" label="跌倒原因" width="120" align="center" />
          <el-table-column prop="severity" label="严重程度" width="100" align="center">
            <template #default="{ row }">
              <el-tag
                :type="
                  row.severity === '轻微'
                    ? 'success'
                    : row.severity === '中度'
                      ? 'warning'
                      : 'danger'
                "
                size="small"
              >
                {{ row.severity }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="恢复状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag
                :type="
                  row.status === '已恢复' ? 'success' : row.status === '康复中' ? 'warning' : 'info'
                "
                size="small"
              >
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <div
          v-if="fallHistoryData.length === 0"
          style="text-align: center; padding: 30px 0; color: #999"
        >
          暂无跌倒记录
        </div>
      </div>
      <template #footer>
        <el-button
          size="large"
          @click="fallHistoryVisible = false"
          style="width: 60px; height: 40px"
          >关闭</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { Management } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { useElderStore } from '@/stores/elderData'
import { getMonitorDeviceStatus, getMonitorHeartbeat, getMonitorAcceleration } from '@/api/stats'
import { deviceStatusLabel } from '@/utils/echartLabels'
// 监控视频：来源与路径统一登记在 src/config/videoRegistry.json，由 MonitorVideo 负责解析与失败兜底
import MonitorVideo from '@/components/MonitorVideo.vue'

const router = useRouter()
const elderStore = useElderStore()

// ===== 当前显示的老人（默认第一个） =====
const currentElderIndex = ref(0)
const elderData = computed(() => elderStore.elderList)
const currentElder = computed(() => elderData.value[currentElderIndex.value] || null)

// ===== 当前老人的跌倒历史 =====
const fallHistoryList = computed(() => {
  const storeAny = elderStore as any
  const directList = Array.isArray(storeAny?.fallHistory) ? storeAny.fallHistory : []
  if (directList.length > 0) return directList

  const methodResult =
    typeof storeAny?.getFallHistory === 'function' ? storeAny.getFallHistory() : []
  return Array.isArray(methodResult) ? methodResult : []
})

const fallHistoryData = computed(() => {
  if (!currentElder.value) return []
  return fallHistoryList.value.filter((f: any) => f.elderName === currentElder.value.name)
})

// ===== 图表实例 =====
let chartMonitoring = null
let chartAcceleration = null
let chartHeart = null

// ===== 图表数据（后端统计接口填充） =====
let monitorStatus = []
let heartCurve = { categories: [], series: [] }
let accCurve = { categories: [], series: [] }

// ===== 从后端加载图表数据 =====
async function loadChartData(elderId = 1) {
  try {
    const res = await getMonitorDeviceStatus()
    // 后端返回的是设备状态枚举 code（ONLINE/OFFLINE/...），转成中文再交给饼图
    monitorStatus = (res.data.data || []).map((item: any) => ({
      ...item,
      name: deviceStatusLabel(item.name),
    }))
  } catch (e) {}
  try {
    const res = await getMonitorHeartbeat(elderId)
    heartCurve = res.data.data || { categories: [], series: [] }
  } catch (e) {}
  try {
    const res = await getMonitorAcceleration(elderId)
    accCurve = res.data.data || { categories: [], series: [] }
  } catch (e) {}
  nextTick(() => initCharts())
}

// ===== 切换老人后刷新心跳/加速度曲线 =====
async function refreshElderCurves(elderId) {
  try {
    const res = await getMonitorHeartbeat(elderId)
    heartCurve = res.data.data || { categories: [], series: [] }
    if (chartHeart) {
      chartHeart.setOption({
        xAxis: { type: 'category', data: heartCurve.categories },
        series: [{ data: heartCurve.series.length > 0 ? heartCurve.series[0].data : [] }],
      })
    }
  } catch (e) {}
  try {
    const res = await getMonitorAcceleration(elderId)
    accCurve = res.data.data || { categories: [], series: [] }
    if (chartAcceleration) {
      chartAcceleration.setOption({
        xAxis: { data: accCurve.categories },
        series: [{ data: accCurve.series.length > 0 ? accCurve.series[0].data : [] }],
      })
    }
  } catch (e) {}
}

// ===== 初始化所有图表 =====
function initCharts() {
  const monitoringDom = document.getElementById('monitoring')
  if (monitoringDom) {
    if (chartMonitoring) chartMonitoring.dispose()
    chartMonitoring = echarts.init(monitoringDom)
    chartMonitoring.setOption({
      tooltip: { trigger: 'item' },
      legend: {
        top: '5%',
        left: 'center',
        textStyle: { fontSize: 11 },
      },
      series: [
        {
          name: '监控设备统计',
          type: 'pie',
          radius: ['35%', '65%'],
          center: ['50%', '55%'],
          avoidLabelOverlap: false,
          itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
          label: { show: false },
          emphasis: {
            label: { show: true, fontSize: 16, fontWeight: 'bold' },
          },
          labelLine: { show: false },
          data: monitorStatus,
        },
      ],
    })
  }

  const accDom = document.getElementById('acceleration')
  if (accDom) {
    if (chartAcceleration) chartAcceleration.dispose()
    chartAcceleration = echarts.init(accDom)
    chartAcceleration.setOption({
      xAxis: {
        type: 'category',
        data: accCurve.categories,
        axisLabel: { fontSize: 9, hideOverlap: true },
      },
      yAxis: {
        type: 'value',
        splitLine: { show: false },
        axisLabel: { fontSize: 9 },
      },
      grid: { top: 8, bottom: 4, left: 8, right: 8, containLabel: true },
      series: [
        {
          data: accCurve.series.length > 0 ? accCurve.series[0].data : [],
          type: 'line',
          smooth: true,
          lineStyle: { color: '#409eff', width: 2 },
          areaStyle: { color: 'rgba(64,158,255,0.15)' },
        },
      ],
    })
  }

  const heartDom = document.getElementById('heart')
  if (heartDom) {
    if (chartHeart) chartHeart.dispose()
    chartHeart = echarts.init(heartDom)
    chartHeart.setOption({
      tooltip: {
        trigger: 'axis',
        formatter: function (params: any[]) {
          const p = params[0]
          if (!p || p.value === undefined || p.value === null) return ''
          return '时间: ' + p.name + '<br/>心跳: ' + p.value + ' bpm'
        },
      },
      grid: { top: 10, bottom: 10, left: 30, right: 8 },
      xAxis: {
        type: 'category',
        data: heartCurve.categories,
        splitLine: { show: false },
        axisLabel: { fontSize: 9, show: false },
      },
      yAxis: {
        type: 'value',
        splitLine: { show: false },
        axisLabel: { fontSize: 9 },
      },
      series: [
        {
          name: '心跳数据',
          type: 'line',
          showSymbol: false,
          data: heartCurve.series.length > 0 ? heartCurve.series[0].data : [],
          lineStyle: { color: '#f56c6c', width: 2 },
          areaStyle: { color: 'rgba(245,108,108,0.15)' },
        },
      ],
    })
  }
}

// ===== 窗口自适应 =====
function resizeCharts() {
  chartMonitoring?.resize()
  chartAcceleration?.resize()
  chartHeart?.resize()
}

// ===== 切换下一位弹窗 =====
const switchDialogVisible = ref(false)
const searchKeyword = ref('')
const filterForm = ref({
  gender: '',
  residenceType: '',
  ageRange: [0, 120],
})
const dialogPage = ref(1)
const dialogPageSize = ref(5)

// ===== 筛选后的老人列表 =====
const filteredElders = computed(() => {
  let list = [...elderData.value]

  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.trim().toLowerCase()
    list = list.filter((item) => item.name.includes(keyword))
  }

  if (filterForm.value.gender) {
    list = list.filter((item) => item.gender === filterForm.value.gender)
  }

  if (filterForm.value.residenceType) {
    list = list.filter((item) => item.residenceType === filterForm.value.residenceType)
  }

  const [minAge, maxAge] = filterForm.value.ageRange
  list = list.filter((item) => item.age >= minAge && item.age <= maxAge)

  return list
})

const dialogTotal = computed(() => filteredElders.value.length)

const paginatedElders = computed(() => {
  const start = (dialogPage.value - 1) * dialogPageSize.value
  const end = start + dialogPageSize.value
  return filteredElders.value.slice(start, end)
})

// ===== 弹窗事件 =====
function openSwitchDialog() {
  switchDialogVisible.value = true
  searchKeyword.value = ''
  filterForm.value = { gender: '', residenceType: '', ageRange: [0, 120] }
  dialogPage.value = 1
}

function handleSearch() {
  dialogPage.value = 1
}

function resetSearch() {
  searchKeyword.value = ''
  dialogPage.value = 1
}

function applyFilter() {
  dialogPage.value = 1
}

function resetFilter() {
  filterForm.value = { gender: '', residenceType: '', ageRange: [0, 120] }
  dialogPage.value = 1
}

function handleDialogPageChange() {}

function selectElder(elder) {
  const index = elderData.value.findIndex((item) => item.code === elder.code)
  if (index > -1) {
    currentElderIndex.value = index
    switchDialogVisible.value = false
    refreshElderCurves(elder.id)
  }
}

// ===== 既往跌倒历史弹窗 =====
const fallHistoryVisible = ref(false)

function openFallHistory() {
  fallHistoryVisible.value = true
}

// ===== 生命周期 =====
onMounted(async () => {
  // 从后端加载老人数据（静默 fallback 到静态数据）
  elderStore.fetchElderList({ page: 1, size: 200 }).catch(() => {})
  await nextTick()
  loadChartData(currentElder.value?.id || 1)
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  chartMonitoring?.dispose()
  chartAcceleration?.dispose()
  chartHeart?.dispose()
  window.removeEventListener('resize', resizeCharts)
})
</script>

<style scoped>
/* ===== 全局重置 ===== */
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

.box-content video {
  width: 90%;
  height: 80%;
  object-fit: cover;
  border-radius: 6px;
  background: #000;
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
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: transparent;
  box-shadow: none;
  min-height: 0;
  overflow: hidden;
}

.grid-content {
  height: 44px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 600;
  color: #333;
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

.row-container {
  display: flex;
  gap: 12px;
  flex: 1;
  min-height: 0;
}

.box {
  border-radius: 10px;
  border: 2px solid #409eff;
  background-color: #f8f9fa;
  padding: 12px;
  display: flex;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.box-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  background-color: rgba(255, 255, 255, 0.5);
}

.box-content img {
  width: 90%;
  height: 80%;
  object-fit: contain;
}

.box-left-top {
  background-color: #e6f7ff;
  border-color: #409eff;
}

.box-left-bottom {
  background-color: #f6ffed;
  border-color: #67c23a;
}

.box-right-top {
  background-color: #fdf6ec;
  border-color: #e6a23c;
  gap: 12px;
}

.box-right-bottom {
  background-color: #fef0f0;
  border-color: #f56c6c;
  gap: 12px;
}

.sub-box {
  border-radius: 8px;
  border: 2px solid #d0d0d0;
  background-color: rgba(255, 255, 255, 0.6);
  display: flex;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.sub-box-left {
  background-color: rgba(255, 255, 255, 0.5);
  border-color: #b0b0b0;
}

.sub-box-right {
  background-color: rgba(255, 255, 255, 0.3);
  border-color: #a0a0a0;
  flex-direction: column;
  gap: 8px;
  padding: 8px;
}

.sub-sub-box {
  flex: 1;
  border-radius: 6px;
  border: 2px solid #c8c8c8;
  background-color: rgba(255, 255, 255, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 0;
  overflow: hidden;
}

.sub-sub-top {
  background-color: rgba(255, 245, 230, 0.6);
  border-color: #d4a574;
}

.sub-sub-bottom {
  background-color: rgba(230, 245, 255, 0.6);
  border-color: #74a5d4;
}

.sub-sub-mid {
  background-color: rgba(230, 255, 230, 0.6);
  border-color: #74d4a5;
}

.box-label {
  font-size: 14px;
  font-weight: 600;
  color: #555;
  letter-spacing: 1px;
}

/* ===== 基本信息行样式 ===== */
.info-row {
  display: flex;
  justify-content: space-between;
  padding: 3px 0;
  border-bottom: 1px solid #f0f0f0;
  font-size: 13px;
  line-height: 1.6;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  font-weight: 600;
  color: #666;
  flex-shrink: 0;
}

.info-value {
  color: #333;
  text-align: right;
  max-width: 55%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.info-value.code {
  color: #409eff;
  font-family: monospace;
  font-weight: 600;
  font-size: 13px;
}

.info-value.ellipsis {
  display: inline-block;
  max-width: 55%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.info-value.remark {
  color: #409eff;
  border-bottom: 1px dashed #409eff;
  cursor: pointer;
}

.info-value.link-btn {
  color: #409eff;
  cursor: pointer;
  text-decoration: underline;
  font-weight: 500;
}

.info-value.link-btn:hover {
  color: #66b1ff;
}

/* ===== 弹窗样式 ===== */
.dialog-search {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.dialog-filter {
  margin-bottom: 12px;
  padding: 12px 0;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
}

.dialog-filter :deep(.el-form-item) {
  margin-bottom: 4px;
}

.dialog-filter :deep(.el-form-item__label) {
  font-weight: 500;
  font-size: 14px;
}

.dialog-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}

.address-text {
  display: block;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #666;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .common-layout {
    padding: 8px;
  }
  .el-header {
    height: auto;
    padding: 8px 12px;
  }
  .row-container {
    flex-direction: column;
  }
  .box-right-top,
  .box-right-bottom {
    flex-direction: column;
  }
  .sub-box-right {
    flex-direction: row;
  }
  .sub-sub-box {
    min-height: 60px;
  }
  .sub-box-left {
    padding: 10px 12px;
  }
  .info-row {
    font-size: 12px;
  }
  .info-value.ellipsis {
    max-width: 45%;
  }
  .dialog-search {
    flex-wrap: wrap;
  }
  .dialog-filter :deep(.el-form-item) {
    width: 100%;
  }
  .dialog-filter :deep(.el-form-item__content) {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .sub-box-left h2 {
    font-size: 14px;
  }
  .info-row {
    font-size: 11px;
    padding: 2px 0;
  }
  .info-value.ellipsis {
    max-width: 35%;
  }
  .info-value.code {
    font-size: 11px;
  }
}
#monitoring,
#acceleration,
#heart {
  min-height: 240px;
}
</style>
