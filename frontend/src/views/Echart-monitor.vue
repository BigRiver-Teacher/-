<template>
  <BigScreenShell active="monitor">
    <main class="bs-main bs-monitor">
      <!-- ===== 左：双通道监控 ===== -->
      <section class="bs-col bs-monitor__video">
        <div class="bs-panel bs-video" style="--d: 0.04s">
          <header class="bs-panel__head">
            <span class="bs-panel__title">通道 01 · 客厅</span>
            <span class="bs-live"><i></i>LIVE</span>
            <span class="bs-panel__tag">{{ currentElder?.address || '居家监护场景' }}</span>
          </header>
          <div class="bs-video__stage">
            <MonitorVideo slot="monitor-1" />
          </div>
        </div>

        <div class="bs-panel bs-video" style="--d: 0.1s">
          <header class="bs-panel__head">
            <span class="bs-panel__title">通道 02 · 卧室</span>
            <span class="bs-live"><i></i>LIVE</span>
            <span class="bs-panel__tag">姿态识别 · 跨帧跟踪</span>
          </header>
          <div class="bs-video__stage">
            <MonitorVideo slot="monitor-2" />
          </div>
        </div>
      </section>

      <!-- ===== 右：设备 / 体征 / 档案 ===== -->
      <section class="bs-col bs-monitor__side">
        <!-- 设备状态 -->
        <div class="bs-panel bs-monitor__status" style="--d: 0.08s">
          <header class="bs-panel__head">
            <span class="bs-panel__title">监护设备状态</span>
            <span class="bs-panel__tag">在线 <b>{{ onlineCount }}</b> / {{ deviceTotal }}</span>
          </header>
          <div class="bs-panel__body">
            <div class="bs-donut">
              <div class="bs-donut__chart">
                <div id="monitorStatus" class="bs-chart"></div>
                <div class="bs-donut__center">
                  <b>{{ deviceTotal }}</b>
                  <span>接入设备</span>
                </div>
              </div>
              <ul class="bs-legend">
                <li v-for="(item, i) in statusData" :key="item.name">
                  <i :style="{ '--c': statusColor(item.name, i) }"></i>
                  <span>{{ item.name }}</span>
                  <b>{{ item.value }}</b>
                </li>
              </ul>
            </div>
          </div>
        </div>

        <!-- 生命体征 -->
        <div class="bs-panel bs-monitor__vital" style="--d: 0.14s">
          <header class="bs-panel__head">
            <span class="bs-panel__title">实时体征曲线</span>
            <span class="bs-panel__tag vital-tag">
              <i class="vital-live"></i>{{ currentElder?.name || '—' }}
            </span>
          </header>
          <div class="bs-panel__body bs-vitals">
            <div class="bs-vital">
              <div class="bs-vital__head">
                <span class="bs-vital__name"><i class="dot dot--rose"></i>心跳</span>
                <span class="bs-metric">
                  <b>{{ heartText }}</b><span>bpm</span>
                </span>
              </div>
              <div id="heart" class="bs-chart"></div>
            </div>
            <div class="bs-vital">
              <div class="bs-vital__head">
                <span class="bs-vital__name"><i class="dot dot--cyan"></i>加速度</span>
                <span class="bs-metric">
                  <b>{{ accText }}</b><span>g</span>
                </span>
              </div>
              <div id="acceleration" class="bs-chart"></div>
            </div>
          </div>
        </div>

        <!-- 对象档案 -->
        <div class="bs-panel bs-monitor__profile" style="--d: 0.2s">
          <header class="bs-panel__head">
            <span class="bs-panel__title">监护对象档案</span>
            <span
              class="bs-panel__tag"
              style="cursor: pointer"
              @click="openFallHistory"
              >跌倒史 <el-icon><ArrowRight /></el-icon
            ></span>
          </header>

          <div v-if="currentElder" class="bs-panel__body bs-profile">
            <div class="bs-info">
              <div class="bs-info__row">
                <span class="bs-info__label">身份证号</span>
                <span class="bs-info__dots"></span>
                <span class="bs-info__value is-code">{{ maskIdCard(currentElder.idCard) }}</span>
              </div>
              <div class="bs-info__row">
                <span class="bs-info__label">姓名</span>
                <span class="bs-info__dots"></span>
                <span class="bs-info__value">{{ currentElder.name }}</span>
              </div>
              <div class="bs-info__row">
                <span class="bs-info__label">年龄</span>
                <span class="bs-info__dots"></span>
                <span class="bs-info__value">{{ currentElder.age }} 岁</span>
              </div>
              <div class="bs-info__row">
                <span class="bs-info__label">居住地址</span>
                <span class="bs-info__dots"></span>
                <el-tooltip :content="currentElder.address" placement="top">
                  <span class="bs-info__value">{{ currentElder.address }}</span>
                </el-tooltip>
              </div>
              <div class="bs-info__row">
                <span class="bs-info__label">专家备注</span>
                <span class="bs-info__dots"></span>
                <el-tooltip :content="currentElder.remark" placement="top">
                  <span class="bs-info__value is-link">{{ currentElder.remark }}</span>
                </el-tooltip>
              </div>
            </div>

            <div class="bs-actions">
              <button type="button" class="bs-btn" @click="router.push('/home/caregiver')">
                <el-icon><Service /></el-icon>陪护
              </button>
              <button type="button" class="bs-btn" @click="router.push('/home/expert')">
                <el-icon><FirstAidKit /></el-icon>专家
              </button>
              <button type="button" class="bs-btn" @click="router.push('/home/elder/base')">
                <el-icon><Document /></el-icon>档案
              </button>
            </div>

            <button
              type="button"
              class="bs-btn bs-btn--primary bs-btn--wide bs-btn--breath"
              @click="openSwitchDialog"
            >
              <el-icon><Switch /></el-icon>切换监护对象
            </button>
          </div>

          <div v-else class="bs-panel__body bs-profile__empty">暂无监护对象数据</div>
        </div>
      </section>
    </main>

    <!-- ===== 切换对象弹窗 ===== -->
    <el-dialog v-model="switchDialogVisible" title="选择监护对象" width="900px" :close-on-click-modal="true">
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
        <el-button type="primary" size="large" @click="handleSearch">搜索</el-button>
        <el-button size="large" @click="resetSearch">重置</el-button>
      </div>

      <div class="dialog-filter">
        <el-form :model="filterForm" inline size="large">
          <el-form-item label="性别">
            <el-select v-model="filterForm.gender" placeholder="全部" clearable style="width: 120px">
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
            >
              <el-option label="全部" value="" />
              <el-option label="独居" value="独居" />
              <el-option label="与子女同住" value="与子女同住" />
              <el-option label="养老院" value="养老院" />
            </el-select>
          </el-form-item>
          <el-form-item label="年龄范围">
            <div class="filter-age">
              <el-slider
                v-model="filterForm.ageRange"
                range
                :min="0"
                :max="120"
                :step="1"
                style="flex: 1"
                :marks="{ 0: '0', 60: '60', 120: '120' }"
              />
              <span class="filter-age__text">
                {{ filterForm.ageRange[0] }} - {{ filterForm.ageRange[1] }} 岁
              </span>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="applyFilter">筛选</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="paginatedElders" stripe border style="width: 100%" max-height="350">
        <el-table-column prop="name" label="姓名" width="88" align="center" />
        <el-table-column prop="gender" label="性别" width="64" align="center" />
        <el-table-column prop="age" label="年龄" width="64" align="center" />
        <el-table-column prop="idCard" label="身份证号" width="180" align="center">
          <template #default="{ row }">
            <span class="cell-code">{{ maskIdCard(row.idCard) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="126" align="center" />
        <el-table-column prop="address" label="居住地址" min-width="140">
          <template #default="{ row }">
            <el-tooltip :content="row.address" placement="top">
              <span class="cell-ellipsis">{{ row.address }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="88" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="selectElder(row)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="dialog-pagination">
        <el-pagination
          v-model:current-page="dialogPage"
          v-model:page-size="dialogPageSize"
          background
          :page-sizes="[5, 10, 20]"
          :total="dialogTotal"
          layout="total, sizes, prev, pager, next"
          @size-change="dialogPage = 1"
        />
      </div>

      <template #footer>
        <el-button @click="switchDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- ===== 跌倒史弹窗 ===== -->
    <el-dialog v-model="fallHistoryVisible" title="既往跌倒历史" width="660px" :close-on-click-modal="true">
      <template v-if="currentElder">
        <p class="dialog-lead">
          {{ currentElder.name }} · {{ currentElder.age }} 岁 · 跌倒记录
        </p>
        <el-table
          :data="fallHistoryData"
          stripe
          border
          style="width: 100%"
          empty-text="该对象暂无跌倒记录"
        >
          <el-table-column prop="date" label="跌倒时间" width="150" align="center" />
          <el-table-column prop="location" label="跌倒地点" width="120" align="center" />
          <el-table-column prop="cause" label="跌倒原因" width="120" align="center" />
          <el-table-column prop="severity" label="严重程度" width="100" align="center">
            <template #default="{ row }">
              <el-tag
                :type="row.severity === '轻微' ? 'success' : row.severity === '中度' ? 'warning' : 'danger'"
                size="small"
              >
                {{ row.severity }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="恢复状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag
                :type="row.status === '已恢复' ? 'success' : row.status === '康复中' ? 'warning' : 'info'"
                size="small"
              >
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </template>
      <template #footer>
        <el-button @click="fallHistoryVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </BigScreenShell>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import BigScreenShell from '@/components/BigScreenShell.vue'
import MonitorVideo from '@/components/MonitorVideo.vue'
import { useElderStore } from '@/stores/elderData'
import { getMonitorDeviceStatus, getMonitorHeartbeat } from '@/api/stats'
import { deviceStatusLabel } from '@/utils/echartLabels'
import {
  BS_SEMANTIC,
  areaGradient,
  bsAnimation,
  bsEmpty,
  bsTooltip,
  hexA,
} from '@/utils/echartsTheme'

const router = useRouter()
const elderStore = useElderStore()

/* ================================================================
   监护对象
   ================================================================ */
const currentElderIndex = ref(0)
const elderData = computed(() => elderStore.elderList)
const currentElder = computed(() => elderData.value[currentElderIndex.value] || null)

const fallHistoryList = computed(() => {
  const storeAny = elderStore as any
  const directList = Array.isArray(storeAny?.fallHistory) ? storeAny.fallHistory : []
  if (directList.length > 0) return directList
  const methodResult =
    typeof storeAny?.getFallHistory === 'function' ? storeAny.getFallHistory() : []
  return Array.isArray(methodResult) ? methodResult : []
})

/** 身份证号脱敏：保留前 6 位与后 4 位 */
function maskIdCard(idCard?: string) {
  if (!idCard) return '—'
  if (idCard.length <= 10) return idCard
  return `${idCard.slice(0, 6)}${'*'.repeat(idCard.length - 10)}${idCard.slice(-4)}`
}

const fallHistoryData = computed(() => {
  if (!currentElder.value) return []
  return fallHistoryList.value.filter((f: any) => f.elderName === currentElder.value.name)
})

/* ================================================================
   图表数据
   ================================================================ */
const statusData = reactive<Array<{ name: string; value: number }>>([])

const deviceTotal = computed(() => statusData.reduce((a, b) => a + (b.value || 0), 0))
const onlineCount = computed(() => statusData.find((s) => s.name === '在线')?.value ?? 0)

/* ================================================================
   实时体征：滚动窗口 + 定时追加模拟采样
   ================================================================ */
/** 窗口内采样点数（36 × 1.6s ≈ 1 分钟） */
const STREAM_SIZE = 36
/** 采样间隔（毫秒） */
const TICK_MS = 1600
/** 加速度基线：统一用 g 为单位展示，默认 0.75g */
const ACC_BASE = 0.75
const ACC_MIN = 0.62
const ACC_MAX = 0.9
/** 心率基线兜底值 */
const HEART_FALLBACK = 78

const heartCurve = reactive<{ categories: string[]; data: number[] }>({ categories: [], data: [] })
const accCurve = reactive<{ categories: string[]; data: number[] }>({ categories: [], data: [] })

const heartText = computed(() => {
  const v = heartCurve.data[heartCurve.data.length - 1]
  return Number.isFinite(v) ? String(Math.round(v)) : '—'
})
const accText = computed(() => {
  const v = accCurve.data[accCurve.data.length - 1]
  return Number.isFinite(v) ? v.toFixed(2) : '—'
})

const STATUS_COLOR: Record<string, string> = {
  在线: BS_SEMANTIC.online,
  离线: BS_SEMANTIC.offline,
  故障: BS_SEMANTIC.fault,
  维护: BS_SEMANTIC.maintain,
}
const statusColor = (name: string, i: number) =>
  STATUS_COLOR[name] || [BS_SEMANTIC.cyan, BS_SEMANTIC.blue, BS_SEMANTIC.violet][i % 3]

/* ================================================================
   加载
   ================================================================ */
let chartStatus: echarts.ECharts | null = null
let chartHeart: echarts.ECharts | null = null
let chartAcc: echarts.ECharts | null = null

/** 当前曲线归属的监护对象 id：用于丢弃切换过程中的过期响应 */
let activeElderId: string | number = 0
/** 心率基线（由当前对象的步频记录推算，缺失时用兜底值） */
let heartBase = HEART_FALLBACK
let streamTimer: ReturnType<typeof setInterval> | null = null

const clamp = (v: number, min: number, max: number) => Math.min(max, Math.max(min, v))
const round = (v: number, digits: number) => Number(v.toFixed(digits))
const pad2 = (n: number) => String(n).padStart(2, '0')
const clockOf = (d: Date) => `${pad2(d.getHours())}:${pad2(d.getMinutes())}:${pad2(d.getSeconds())}`

/** 心率：随机游走 + 向基线回归 + 轻微呼吸周期，保证曲线自然不突跳 */
function nextHeart(prev: number, t: number) {
  // 双频呼吸波 + 微噪声构成目标值，再用一阶滤波逼近，保证相邻采样点差值很小
  const breathe = Math.sin(t / 13000) * 3.2 + Math.sin(t / 4700) * 1.1
  const noise = (Math.random() - 0.5) * 1.5
  const target = heartBase + breathe + noise
  return clamp(prev + (target - prev) * 0.42, heartBase - 10, heartBase + 10)
}

/** 加速度：在 0.75g 附近小幅波动，偶发体位变化带来的尖峰 */
function nextAcc(prev: number, t: number) {
  const breathe = Math.sin(t / 11000) * 0.045 + Math.sin(t / 3900) * 0.015
  const noise = (Math.random() - 0.5) * 0.018
  const target = ACC_BASE + breathe + noise
  // 低概率的体位变化尖峰，幅度克制，不破坏 0.75g 的读数
  const spike = Math.random() < 0.015 ? 0.03 + Math.random() * 0.03 : 0
  return round(clamp(prev + (target - prev) * 0.4 + spike, 0.64, 0.88), 3)
}

async function loadDeviceStatus() {
  try {
    const res = await getMonitorDeviceStatus()
    // 后端返回设备状态枚举 code（ONLINE/OFFLINE/...），转成中文再交给环形图
    const list = (res.data.data || []).map((item: any) => ({
      name: deviceStatusLabel(item.name),
      value: item.value,
    }))
    statusData.splice(0, statusData.length, ...list)
  } catch {
    /* ignore */
  }
}

/** 按当前基线铺满整个窗口，作为切换对象后的初始曲线 */
function seedWave() {
  const cats: string[] = []
  const hearts: number[] = []
  const accs: number[] = []
  const now = Date.now()
  let h = heartBase + (Math.random() - 0.5) * 4
  let a = ACC_BASE + (Math.random() - 0.5) * 0.06
  for (let i = STREAM_SIZE - 1; i >= 0; i--) {
    const at = now - i * TICK_MS
    cats.push(clockOf(new Date(at)))
    h = nextHeart(h, at)
    a = nextAcc(a, at)
    hearts.push(round(h, 1))
    accs.push(a)
  }
  // 末位锚定到基线，首屏即可看到「0.75g / 基线心率」的稳定值
  hearts[hearts.length - 1] = round(heartBase, 1)
  accs[accs.length - 1] = ACC_BASE

  // 两个图表各持一份时间轴（同内容不同引用），避免共用数组被 push 两次导致轴与数据错位
  heartCurve.categories = cats
  heartCurve.data = hearts
  accCurve.categories = [...cats]
  accCurve.data = accs
}

/** 追加一个采样点；超出窗口长度即丢弃最旧点，保证不残留其它对象的数据 */
function pushSample() {
  if (!heartCurve.data.length) return
  const now = Date.now()
  const t = clockOf(new Date(now))
  const h = round(nextHeart(heartCurve.data[heartCurve.data.length - 1], now), 1)
  const a = nextAcc(accCurve.data[accCurve.data.length - 1], now)

  heartCurve.categories.push(t)
  accCurve.categories.push(t)
  heartCurve.data.push(h)
  accCurve.data.push(a)

  if (heartCurve.data.length > STREAM_SIZE) {
    heartCurve.categories.shift()
    accCurve.categories.shift()
    heartCurve.data.shift()
    accCurve.data.shift()
  }
  renderVitals()
}

function startStream() {
  stopStream()
  streamTimer = setInterval(pushSample, TICK_MS)
}

function stopStream() {
  if (streamTimer) {
    clearInterval(streamTimer)
    streamTimer = null
  }
}

/** 把窗口数据推给两张曲线；y 轴范围随基线固定，避免每次刷新上下跳动 */
function renderVitals() {
  if (chartHeart) {
    chartHeart.setOption({
      xAxis: { data: heartCurve.categories },
      yAxis: { min: Math.round(heartBase - 10), max: Math.round(heartBase + 10) },
      series: [{ data: heartCurve.data }],
    })
  }
  if (chartAcc) {
    chartAcc.setOption({
      xAxis: { data: accCurve.categories },
      yAxis: { min: ACC_MIN, max: ACC_MAX },
      series: [{ data: accCurve.data }],
    })
  }
}

/** 切换对象时整体重建两条曲线（notMerge），避免新旧数据互相插值出现折返乱线 */
function resetVitals() {
  if (chartHeart) {
    chartHeart.setOption(
      miniLineOption(
        heartCurve.data,
        heartCurve.categories,
        BS_SEMANTIC.rose,
        Math.round(heartBase - 10),
        Math.round(heartBase + 10),
        'bpm',
        0,
      ),
      true,
    )
  }
  if (chartAcc) {
    chartAcc.setOption(
      miniLineOption(accCurve.data, accCurve.categories, BS_SEMANTIC.cyan, ACC_MIN, ACC_MAX, 'g', 2),
      true,
    )
  }
}

/**
 * 绑定某个监护对象的实时体征：
 * 停表 → 清空窗口 → 取该对象基线 → 重新铺满窗口 → 开流
 */
async function bindElderVitals(elder: any) {
  const id = elder?.id ?? 0
  activeElderId = id

  stopStream()
  heartCurve.categories = []
  heartCurve.data = []
  accCurve.categories = []
  accCurve.data = []
  heartBase = HEART_FALLBACK

  try {
    const res = await getMonitorHeartbeat(id)
    // 响应返回时若已切换到别的对象，直接丢弃，避免旧数据串入
    if (activeElderId !== id) return
    const raw = (res.data.data?.series?.[0]?.data || []) as number[]
    const vals = raw.filter((v) => Number.isFinite(v))
    if (vals.length >= 3) {
      const mean = vals.reduce((a, b) => a + b, 0) / vals.length
      heartBase = clamp(round(mean, 1), 66, 96)
    }
  } catch {
    /* 后端不可用时用默认基线 */
  }
  if (activeElderId !== id) return

  seedWave()
  if (!chartHeart || !chartAcc) initCharts()
  resetVitals()
  startStream()
}

/* ================================================================
   图表
   ================================================================ */
function miniLineOption(
  data: number[],
  categories: string[],
  color: string,
  min: number,
  max: number,
  unit: string,
  digits = 0,
) {
  return {
    ...bsAnimation,
    // 与采样间隔对齐的线性补间：新点推出、旧点左移，形成连续滚动的示波器效果
    animationDurationUpdate: Math.round(TICK_MS * 0.8),
    animationEasingUpdate: 'linear' as const,
    ...bsEmpty(data.length),
    tooltip: bsTooltip({
      trigger: 'axis',
      axisPointer: { type: 'line', lineStyle: { color: hexA(color, 0.35) } },
      formatter: (params: any[]) => {
        const p = params[0]
        if (!p) return ''
        return `${p.axisValue}<br/>${p.marker} ${Number(p.value).toFixed(digits)} ${unit}`
      },
    }),
    grid: { top: 8, left: 0, right: 0, bottom: 0 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: categories,
      show: false,
    },
    yAxis: {
      type: 'value',
      show: false,
      min,
      max,
    },
    series: [
      {
        type: 'line',
        smooth: true,
        showSymbol: false,
        symbolSize: 6,
        data,
        lineStyle: {
          width: 2,
          color,
          shadowColor: hexA(color, 0.65),
          shadowBlur: 12,
        },
        itemStyle: { color },
        areaStyle: { color: areaGradient(color, 0.36, 0.01) },
        emphasis: { focus: 'series', scale: 1.4 },
      },
    ],
  }
}

function initCharts() {
  ;[chartStatus, chartHeart, chartAcc].forEach((c) => c?.dispose())
  chartStatus = chartHeart = chartAcc = null

  /* 1. 设备状态 —— 环形 */
  const statusEl = document.getElementById('monitorStatus')
  if (statusEl) {
    chartStatus = echarts.init(statusEl)
    chartStatus.setOption({
      ...bsAnimation,
      tooltip: bsTooltip({ trigger: 'item', formatter: '{b}：{c} 台（{d}%）' }),
      series: [
        {
          name: '监护设备',
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
          data: statusData.map((d, i) => ({
            name: d.name,
            value: d.value,
            itemStyle: { color: statusColor(d.name, i) },
          })),
        },
      ],
    })
  }

  /* 2. 心跳 */
  const heartEl = document.getElementById('heart')
  if (heartEl) {
    chartHeart = echarts.init(heartEl)
    chartHeart.setOption(
      miniLineOption(
        heartCurve.data,
        heartCurve.categories,
        BS_SEMANTIC.rose,
        Math.round(heartBase - 10),
        Math.round(heartBase + 10),
        'bpm',
        0,
      ),
    )
  }

  /* 3. 加速度 */
  const accEl = document.getElementById('acceleration')
  if (accEl) {
    chartAcc = echarts.init(accEl)
    chartAcc.setOption(
      miniLineOption(accCurve.data, accCurve.categories, BS_SEMANTIC.cyan, ACC_MIN, ACC_MAX, 'g', 2),
    )
  }
}

function resizeCharts() {
  chartStatus?.resize()
  chartHeart?.resize()
  chartAcc?.resize()
}

/* ================================================================
   切换对象弹窗
   ================================================================ */
const switchDialogVisible = ref(false)
const searchKeyword = ref('')
const filterForm = ref({ gender: '', residenceType: '', ageRange: [0, 120] })
const dialogPage = ref(1)
const dialogPageSize = ref(5)

const filteredElders = computed(() => {
  let list = [...elderData.value]
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.trim().toLowerCase()
    list = list.filter((item) => item.name.includes(keyword))
  }
  if (filterForm.value.gender) list = list.filter((i) => i.gender === filterForm.value.gender)
  if (filterForm.value.residenceType)
    list = list.filter((i) => i.residenceType === filterForm.value.residenceType)
  const [minAge, maxAge] = filterForm.value.ageRange
  list = list.filter((item) => item.age >= minAge && item.age <= maxAge)
  return list
})

const dialogTotal = computed(() => filteredElders.value.length)
const paginatedElders = computed(() => {
  const start = (dialogPage.value - 1) * dialogPageSize.value
  return filteredElders.value.slice(start, start + dialogPageSize.value)
})

function openSwitchDialog() {
  switchDialogVisible.value = true
  searchKeyword.value = ''
  filterForm.value = { gender: '', residenceType: '', ageRange: [0, 120] }
  dialogPage.value = 1
}
const handleSearch = () => (dialogPage.value = 1)
const resetSearch = () => {
  searchKeyword.value = ''
  dialogPage.value = 1
}
const applyFilter = () => (dialogPage.value = 1)
const resetFilter = () => {
  filterForm.value = { gender: '', residenceType: '', ageRange: [0, 120] }
  dialogPage.value = 1
}

function selectElder(elder: any) {
  const index = elderData.value.findIndex(
    (item) => item.id === elder.id || (!!item.code && item.code === elder.code),
  )
  if (index > -1) {
    currentElderIndex.value = index
    switchDialogVisible.value = false
    bindElderVitals(elderData.value[index])
  }
}

/* ================================================================
   跌倒史
   ================================================================ */
const fallHistoryVisible = ref(false)
const openFallHistory = () => (fallHistoryVisible.value = true)

/* ================================================================
   生命周期
   ================================================================ */
onMounted(async () => {
  // 设备状态与老人列表并行拉取，避免串行等待拖慢首屏
  await Promise.all([
    elderStore.fetchElderList({ page: 1, size: 200 }).catch(() => {}),
    loadDeviceStatus(),
  ])
  await nextTick()
  // 列表就绪后再绑定，确保首个监护对象的心率基线取值正确
  await bindElderVitals(currentElder.value)
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  stopStream()
  ;[chartStatus, chartHeart, chartAcc].forEach((c) => c?.dispose())
  window.removeEventListener('resize', resizeCharts)
})
</script>

<style scoped>
.bs-monitor {
  grid-template-columns: minmax(0, 1.58fr) minmax(0, 1fr);
}

.bs-monitor__video,
.bs-monitor__side {
  min-height: 0;
}

.bs-video {
  flex: 1 1 0;
}

/* 右侧三块的高度配比 */
.bs-monitor__status {
  flex: 0 0 27%;
}

.bs-monitor__vital {
  flex: 1 1 0;
  min-height: 118px;
}

.bs-monitor__profile {
  flex: 0 0 auto;
}

/* ===== 体征 ===== */
.vital-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.vital-live {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #22d3a7;
  box-shadow: 0 0 0 0 rgba(34, 211, 167, 0.6);
  animation: vital-pulse 1.6s ease-out infinite;
}

@keyframes vital-pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(34, 211, 167, 0.65);
  }
  70% {
    box-shadow: 0 0 0 7px rgba(34, 211, 167, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(34, 211, 167, 0);
  }
}

.bs-vitals {
  flex-direction: column;
  gap: 6px;
}

.bs-vital {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  position: relative;
}

.bs-vital + .bs-vital {
  border-top: 1px solid rgba(86, 148, 220, 0.12);
  padding-top: 6px;
}

.bs-vital__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
  padding: 0 2px 2px;
}

.bs-vital__name {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 11.5px;
  letter-spacing: 0.5px;
  color: #9db8d6;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.dot--rose {
  background: #ff5c7c;
  box-shadow: 0 0 8px #ff5c7c;
}

.dot--cyan {
  background: #38e1ff;
  box-shadow: 0 0 8px #38e1ff;
}

/* ===== 档案 ===== */
.bs-profile {
  flex-direction: column;
  gap: 2px;
}

.bs-profile__empty {
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: var(--bs-faint);
}

/* ===== 视频占满舞台 ===== */
.bs-video__stage :deep(.monitor-video) {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 0;
}

.bs-video__stage :deep(.monitor-fallback) {
  border: none;
}

/* ===== 弹窗内 ===== */
.dialog-search {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.dialog-filter {
  margin-bottom: 12px;
  padding: 10px 0;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
}

.dialog-filter :deep(.el-form-item) {
  margin-bottom: 6px;
}

.filter-age {
  display: flex;
  align-items: center;
  gap: 14px;
  width: 320px;
}

.filter-age__text {
  flex: 0 0 auto;
  font-size: 13px;
  font-weight: 600;
  color: #409eff;
}

.dialog-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.dialog-lead {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.cell-code {
  font-family: Consolas, Monaco, monospace;
  font-size: 12.5px;
  color: #409eff;
}

.cell-ellipsis {
  display: block;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #606266;
}

@media (max-width: 1360px) {
  .bs-monitor {
    gap: 10px;
  }
}
</style>
