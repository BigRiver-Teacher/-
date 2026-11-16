<template>
  <div class="gait-page">
    <!-- ===== 页面标题 ===== -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><TrendCharts /></el-icon>
        步态历史
      </h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加
        </el-button>
        <el-button type="primary" plain @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="success" plain>
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>

    <!-- ===== 统计卡片 ===== -->
    <div class="stats-grid">
      <div class="stat-card total">
        <div class="stat-icon">
          <el-icon><List /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.total }}</div>
          <div class="stat-label">总记录数</div>
        </div>
      </div>
      <div class="stat-card speed">
        <div class="stat-icon">
          <el-icon><TrendCharts /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">
            {{ stats.avgSpeed }}
            <span style="font-size: 16px; color: #999">m/s</span>
          </div>
          <div class="stat-label">平均步速</div>
        </div>
      </div>
      <div class="stat-card abnormal">
        <div class="stat-icon">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.abnormalCount }}</div>
          <div class="stat-label">步态异常次数</div>
        </div>
      </div>
      <div class="stat-card assessment">
        <div class="stat-icon">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.lastAssessment }}</div>
          <div class="stat-label">最近评估时间</div>
        </div>
      </div>
    </div>

    <!-- ===== 图表区域 ===== -->
    <div class="chart-section">
      <div class="chart-header">
        <span class="chart-title">步态趋势分析</span>
      </div>
      <div ref="chartRef" class="chart-container"></div>
    </div>

    <!-- ===== 筛选区域 ===== -->
    <div class="filter-section">
      <div class="filter-header">
        <div class="filter-title">
          <el-icon><Filter /></el-icon>
          <span>筛选条件</span>
        </div>
        <div class="filter-actions">
          <el-button type="primary" plain @click="resetFilter">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </div>
      </div>
      <div class="filter-body">
        <el-form :model="filterForm" label-width="80px" inline>
          <el-form-item label="老人姓名">
            <el-input
              v-model="filterForm.name"
              placeholder="请输入姓名"
              clearable
              style="width: 160px"
              prefix-icon="Search"
            />
          </el-form-item>
          <el-form-item label="评估结果">
            <el-select
              v-model="filterForm.result"
              placeholder="全部"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="item in resultOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="日期范围">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始"
              end-placeholder="结束"
              value-format="YYYY-MM-DD"
              style="width: 260px"
            />
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- ===== 表格区域 ===== -->
    <div class="table-section">
      <div class="table-header">
        <div class="table-title">
          <el-icon><List /></el-icon>
          <span>步态记录列表</span>
          <el-tag size="small" type="info">共 {{ total }} 条</el-tag>
        </div>
      </div>

      <el-table :data="paginatedList" stripe style="width: 100%" row-key="id" border>
        <el-table-column prop="name" label="老人姓名" width="100" align="center" />
        <el-table-column prop="date" label="测量日期" width="160" align="center" />
        <el-table-column prop="speed" label="步速(m/s)" width="100" align="center" />
        <el-table-column prop="frequency" label="步频(步/分)" width="110" align="center" />
        <el-table-column prop="stepLength" label="步长(cm)" width="100" align="center" />
        <el-table-column prop="score" label="步态评分" width="100" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.score >= 80 ? 'success' : row.score >= 60 ? 'warning' : 'danger'"
              size="small"
            >
              {{ row.score }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="result" label="评估结果" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag
              :type="
                row.result === '正常' ? 'success' : row.result === '异常' ? 'warning' : 'danger'
              "
              size="small"
            >
              {{ row.result }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" type="primary" link @click="handleView(row)">
                <el-icon><View /></el-icon>
                查看详情
              </el-button>
              <el-button size="small" type="danger" link @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          background
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="currentPage = 1"
        >
          <template #total="{ total }">
            <span style="font-weight: 500">共 {{ total }} 条</span>
          </template>
          <template #jumper="{ go }">
            <span style="margin-left: 8px; font-size: 14px; color: #666">前往</span>
            <el-input-number
              :min="1"
              :max="Math.ceil(total / pageSize) || 1"
              :model-value="currentPage"
              @update:model-value="
                (val) => {
                  if (val >= 1 && val <= Math.ceil(total / pageSize)) go(val)
                }
              "
              size="small"
              controls-position="right"
              style="width: 60px; margin: 0 4px"
            />
            <span style="font-size: 14px; color: #666">页</span>
          </template>
        </el-pagination>
      </div>
    </div>

    <!-- ===== 添加弹框 ===== -->
    <el-dialog
      v-model="dialogVisible"
      title="添加步态记录"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" label-width="100px" :rules="formRules">
        <el-form-item label="老人姓名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入老人姓名" />
        </el-form-item>
        <el-form-item label="测量日期" prop="date">
          <el-date-picker
            v-model="formData.date"
            type="datetime"
            placeholder="选择日期时间"
            value-format="YYYY-MM-DD HH:mm"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="步速(m/s)" prop="speed">
          <el-input-number
            v-model.number="formData.speed"
            :min="0"
            :max="2"
            :step="0.01"
            style="width: 100%"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="步频(步/分)" prop="frequency">
          <el-input-number
            v-model.number="formData.frequency"
            :min="0"
            :max="200"
            style="width: 100%"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="步长(cm)" prop="stepLength">
          <el-input-number
            v-model.number="formData.stepLength"
            :min="0"
            :max="100"
            style="width: 100%"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="评估结果" prop="result">
          <el-select v-model="formData.result" style="width: 100%">
            <el-option label="正常" value="正常" />
            <el-option label="异常" value="异常" />
            <el-option label="高风险" value="高风险" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>

    <!-- ===== 详情弹框 ===== -->
    <el-dialog v-model="detailVisible" title="步态记录详情" width="500px">
      <div v-if="detailData" class="detail-grid">
        <div class="detail-item">
          <span class="detail-label">老人姓名</span>
          <span class="detail-value">{{ detailData.name }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">测量日期</span>
          <span class="detail-value">{{ detailData.date }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">步速</span>
          <span class="detail-value">{{ detailData.speed }} m/s</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">步频</span>
          <span class="detail-value">{{ detailData.frequency }} 步/分</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">步长</span>
          <span class="detail-value">{{ detailData.stepLength }} cm</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">步态评分</span>
          <span class="detail-value">{{ detailData.score }}</span>
        </div>
        <div class="detail-item full-width">
          <span class="detail-label">评估结果</span>
          <el-tag
            :type="
              detailData.result === '正常'
                ? 'success'
                : detailData.result === '异常'
                  ? 'warning'
                  : 'danger'
            "
            size="small"
          >
            {{ detailData.result }}
          </el-tag>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, markRaw, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import {
  Refresh,
  Plus,
  View,
  Delete,
  Filter,
  List,
  Download,
  Clock,
  Warning,
  TrendCharts,
} from '@element-plus/icons-vue'
import { getGaitList, addGait, deleteGait } from '@/api/gait'

// ===== 统计数据 =====
const stats = reactive({
  total: 0,
  avgSpeed: 0,
  abnormalCount: 0,
  lastAssessment: '',
})

// ===== 数据列表 =====
const gaitList = ref([])

// ===== 图表引用 =====
const chartRef = ref(null)
let chartInstance = null

// ===== 评估结果枚举映射（后端 code → 页面中文） =====
const RESULT_TEXT_MAP = {
  NORMAL: '正常',
  ABNORMAL: '异常',
  HIGH_RISK: '高风险',
  BORDERLINE: '异常',
}

// ===== 评估结果枚举映射（页面中文 → 后端 code） =====
const RESULT_CODE_MAP = {
  正常: 'NORMAL',
  异常: 'ABNORMAL',
  高风险: 'HIGH_RISK',
}

// ===== 老人姓名 → elderId 映射（新增时后端要求 elderId） =====
const elderIdMap = computed(() => {
  const map = {}
  gaitList.value.forEach((item) => {
    if (item.elderId != null && !map[item.name]) {
      map[item.name] = item.elderId
    }
  })
  return map
})

// ===== 日期格式转换（页面 'YYYY-MM-DD HH:mm' → 后端 'YYYY-MM-DDTHH:mm:ss'） =====
function toBackendDateTime(value) {
  if (!value) return value
  let s = String(value).trim().replace(' ', 'T')
  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(s)) {
    s = s + ':00'
  }
  return s
}

// ===== 初始化数据（从后端加载） =====
async function initData() {
  try {
    const res = await getGaitList({ page: 1, size: 10000 })
    const data = res.data.data
    gaitList.value = (data?.list || []).map((item) => ({
      ...item,
      date: item.date ? String(item.date).replace('T', ' ') : item.date,
      result: RESULT_TEXT_MAP[item.result] || item.result,
    }))
    updateStats()
    nextTick(() => {
      setTimeout(initChart, 300)
    })
  } catch (e) {
    // 错误提示已由响应拦截器统一处理
  }
}

// ===== 更新统计数据 =====
function updateStats() {
  const list = gaitList.value
  stats.total = list.length
  if (list.length > 0) {
    const totalSpeed = list.reduce((sum, item) => sum + item.speed, 0)
    stats.avgSpeed = parseFloat((totalSpeed / list.length).toFixed(2))
    stats.abnormalCount = list.filter(
      (item) => item.result === '异常' || item.result === '高风险',
    ).length
    stats.lastAssessment = list[0]?.date?.split(' ')[0] || '-'
  }
}

// ===== 图表 =====
function initChart() {
  if (!chartRef.value) return
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }

  const list = gaitList.value.slice(0, 10)
  const dates = list.map((item) => item.date.split(' ')[0])
  const speeds = list.map((item) => item.speed)
  const scores = list.map((item) => item.score)

  chartInstance = markRaw(echarts.init(chartRef.value))

  chartInstance.setOption({
    tooltip: {
      trigger: 'axis',
    },
    legend: {
      data: ['步速(m/s)', '步态评分'],
      bottom: 0,
    },
    grid: {
      left: 50,
      right: 20,
      top: 20,
      bottom: 50,
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        rotate: 30,
        fontSize: 11,
      },
    },
    yAxis: [
      {
        type: 'value',
        name: '步速(m/s)',
        min: 0,
        max: 1.5,
      },
      {
        type: 'value',
        name: '评分',
        min: 0,
        max: 100,
      },
    ],
    series: [
      {
        name: '步速(m/s)',
        type: 'line',
        data: speeds,
        smooth: true,
        lineStyle: {
          color: '#409eff',
          width: 3,
        },
        itemStyle: {
          color: '#409eff',
        },
        areaStyle: {
          color: 'rgba(64,158,255,0.15)',
        },
      },
      {
        name: '步态评分',
        type: 'bar',
        data: scores,
        yAxisIndex: 1,
        itemStyle: {
          color: '#67c23a',
          borderRadius: [4, 4, 0, 0],
        },
      },
    ],
  })

  const resizeHandler = () => {
    if (chartInstance) {
      chartInstance.resize()
    }
  }
  window.removeEventListener('resize', resizeHandler)
  window.addEventListener('resize', resizeHandler)
}

// ===== 筛选条件 =====
const filterForm = reactive({
  name: '',
  result: '',
  dateRange: [],
})

const resultOptions = [
  { label: '全部', value: '' },
  { label: '正常', value: '正常' },
  { label: '异常', value: '异常' },
  { label: '高风险', value: '高风险' },
]

// ===== 分页 =====
const currentPage = ref(1)
const pageSize = ref(5)

// ===== 详情弹框 =====
const detailVisible = ref(false)
const detailData = ref(null)

// ===== 新增弹框 =====
const dialogVisible = ref(false)
const formData = reactive({
  name: '',
  date: '',
  speed: null as number | null,
  frequency: null as number | null,
  stepLength: null as number | null,
  result: '',
})

const formRules = {
  name: [{ required: true, message: '请输入老人姓名', trigger: 'blur' }],
  date: [{ required: true, message: '请选择测量日期', trigger: 'change' }],
  speed: [{ required: true, message: '请输入步速', trigger: 'blur' }],
  frequency: [{ required: true, message: '请输入步频', trigger: 'blur' }],
  stepLength: [{ required: true, message: '请输入步长', trigger: 'blur' }],
  result: [{ required: true, message: '请选择评估结果', trigger: 'change' }],
}

const formRef = ref(null)

// ===== 筛选数据 =====
const filteredList = computed(() => {
  let list = [...gaitList.value]

  if (filterForm.name) {
    list = list.filter((item) => item.name.includes(filterForm.name))
  }
  if (filterForm.result) {
    list = list.filter((item) => item.result === filterForm.result)
  }
  if (filterForm.dateRange && filterForm.dateRange.length === 2) {
    list = list.filter((item) => {
      const date = item.date.split(' ')[0]
      return date >= filterForm.dateRange[0] && date <= filterForm.dateRange[1]
    })
  }

  return list
})

const total = computed(() => filteredList.value.length)

const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredList.value.slice(start, end)
})

// ===== 操作函数 =====
function handleView(row) {
  detailData.value = row
  detailVisible.value = true
}

function handleAdd() {
  Object.assign(formData, {
    name: '',
    date: '',
    speed: '',
    frequency: '',
    stepLength: '',
    result: '',
  })
  formRef.value?.resetFields()
  dialogVisible.value = true
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除 "${row.name}" 的步态记录吗？`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      await deleteGait(row.id)
      ElMessage.success('删除成功')
      await initData()
    })
    .catch(() => {})
}

function resetFilter() {
  filterForm.name = ''
  filterForm.result = ''
  filterForm.dateRange = []
  currentPage.value = 1
}

function refreshData() {
  initData()
  ElMessage.success('数据已刷新')
}

// ===== 提交表单 =====
async function submitForm() {
  formRef.value?.validate(async (valid) => {
    if (!valid) return

    const elderId = elderIdMap.value[formData.name]
    if (elderId == null) {
      ElMessage.warning('未找到该老人的档案信息，无法添加步态记录')
      return
    }

    const score = Math.floor(Math.random() * 30 + 50)
    const payload = {
      name: formData.name,
      elderId: elderId,
      date: toBackendDateTime(formData.date),
      speed: formData.speed,
      frequency: formData.frequency,
      stepLength: formData.stepLength,
      score: score,
      result: RESULT_CODE_MAP[formData.result] || formData.result,
    }

    try {
      await addGait(payload)
      ElMessage.success('添加成功')
      dialogVisible.value = false
      await initData()
    } catch (e) {
      // 错误提示已由响应拦截器统一处理
    }
  })
}

// ===== 生命周期 =====
onMounted(() => {
  initData()
})
</script>

<style scoped>
.gait-page {
  padding: 20px;
  background-color: #f0f2f5;
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

.header-actions {
  display: flex;
  gap: 10px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
  flex-shrink: 0;
}

.stat-card.total .stat-icon {
  background: #e6f7ff;
  color: #409eff;
}

.stat-card.speed .stat-icon {
  background: #f0f9eb;
  color: #67c23a;
}

.stat-card.abnormal .stat-icon {
  background: #fef0f0;
  color: #f56c6c;
}

.stat-card.assessment .stat-icon {
  background: #fdf6ec;
  color: #e6a23c;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 2px;
}

.chart-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.chart-header {
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}

.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.chart-container {
  width: 100%;
  height: 280px;
}

.filter-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}

.filter-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.filter-title .el-icon {
  font-size: 18px;
  color: #409eff;
}

.filter-body :deep(.el-form-item) {
  margin-bottom: 8px;
}

.table-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow-x: auto;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 16px;
}

.table-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.table-title .el-icon {
  font-size: 18px;
  color: #409eff;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th.el-table__cell) {
  background-color: #f5f7fa !important;
  color: #333;
  font-weight: 600;
  padding: 12px 0;
}

:deep(.el-table .el-table__cell) {
  padding: 8px 0;
}

:deep(.el-table .cell) {
  padding: 0 8px;
}

.action-buttons {
  display: flex;
  gap: 2px;
  flex-wrap: nowrap;
  justify-content: center;
  align-items: center;
}

.action-buttons .el-button {
  padding: 4px 6px;
  font-size: 12px;
}

.action-buttons .el-button .el-icon {
  font-size: 14px;
  margin-right: 2px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  margin-top: 16px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 24px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
}

.detail-item.full-width {
  grid-column: 1 / -1;
}

.detail-label {
  font-size: 13px;
  color: #999;
  margin-bottom: 2px;
}

.detail-value {
  font-size: 14px;
  color: #333;
  word-break: break-all;
}

@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .filter-body :deep(.el-form-item) {
    display: block;
    width: 100%;
  }

  .filter-body :deep(.el-select) {
    width: 100% !important;
  }

  .filter-body :deep(.el-input) {
    width: 100% !important;
  }

  .action-buttons {
    flex-wrap: wrap;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .gait-page {
    padding: 12px;
  }

  .chart-container {
    height: 200px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 18px;
  }

  .stat-number {
    font-size: 22px;
  }

  .stat-card {
    padding: 16px;
  }

  :deep(.el-table .el-table__cell) {
    padding: 6px 0;
  }

  .action-buttons .el-button {
    padding: 2px 4px;
    font-size: 11px;
  }
}
</style>
