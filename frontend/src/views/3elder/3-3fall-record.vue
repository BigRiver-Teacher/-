<template>
  <div class="fall-page">
    <!-- ===== 页面标题 ===== -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Warning /></el-icon>
        既往跌倒记录
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
          <div class="stat-label">总跌倒次数</div>
        </div>
      </div>
      <div class="stat-card recent">
        <div class="stat-icon">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.recent }}</div>
          <div class="stat-label">30天内跌倒</div>
        </div>
      </div>
      <div class="stat-card risk">
        <div class="stat-icon">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.highRisk }}</div>
          <div class="stat-label">中重度跌倒</div>
        </div>
      </div>
      <div class="stat-card recovery">
        <div class="stat-icon">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">
            {{ stats.avgRecoveryDays }}
            <span style="font-size: 16px; color: #999">天</span>
          </div>
          <div class="stat-label">平均恢复天数</div>
        </div>
      </div>
    </div>

    <!-- ===== 图表区域 ===== -->
    <div class="chart-section">
      <div class="chart-header">
        <span class="chart-title">跌倒原因分布</span>
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
          <el-form-item label="严重程度">
            <el-select
              v-model="filterForm.severity"
              placeholder="全部"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="item in severityOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="跌倒原因">
            <el-select v-model="filterForm.cause" placeholder="全部" clearable style="width: 130px">
              <el-option
                v-for="item in causeOptions"
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
          <span>跌倒记录列表</span>
          <el-tag size="small" type="info">共 {{ total }} 条</el-tag>
        </div>
      </div>

      <el-table :data="paginatedList" stripe style="width: 100%" row-key="id" border>
        <el-table-column prop="name" label="老人姓名" width="100" align="center" />
        <el-table-column prop="date" label="跌倒时间" width="160" align="center" />
        <el-table-column prop="location" label="跌倒地点" width="100" align="center" />
        <el-table-column prop="cause" label="跌倒原因" width="110" align="center" />
        <el-table-column prop="severity" label="严重程度" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getSeverityType(row.severity)" size="small">
              {{ row.severity }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="恢复状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handler" label="处理人" min-width="90" align="center" />
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" type="primary" link @click="handleView(row)">
                <el-icon><View /></el-icon>
                查看详情
              </el-button>
              <el-button size="small" type="warning" link @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                编辑
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

    <!-- ===== 添加/编辑弹框 ===== -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" label-width="100px" :rules="formRules">
        <el-form-item label="老人姓名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入老人姓名" />
        </el-form-item>
        <el-form-item label="跌倒时间" prop="date">
          <el-date-picker
            v-model="formData.date"
            type="datetime"
            placeholder="选择跌倒时间"
            value-format="YYYY-MM-DD HH:mm"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="跌倒地点">
          <el-input v-model="formData.location" placeholder="请输入跌倒地点" />
        </el-form-item>
        <el-form-item label="跌倒原因">
          <el-select v-model="formData.cause" style="width: 100%">
            <el-option label="地面湿滑" value="地面湿滑" />
            <el-option label="头晕" value="头晕" />
            <el-option label="绊倒" value="绊倒" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="受伤部位">
          <el-input v-model="formData.injury" placeholder="请输入受伤部位" />
        </el-form-item>
        <el-form-item label="严重程度">
          <el-select v-model="formData.severity" style="width: 100%">
            <el-option label="轻微" value="轻微" />
            <el-option label="中度" value="中度" />
            <el-option label="重度" value="重度" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理方式">
          <el-input v-model="formData.treatment" placeholder="请输入处理方式" />
        </el-form-item>
        <el-form-item label="恢复状态">
          <el-select v-model="formData.status" style="width: 100%">
            <el-option label="已恢复" value="已恢复" />
            <el-option label="康复中" value="康复中" />
            <el-option label="后遗症" value="后遗症" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理人">
          <el-input v-model="formData.handler" placeholder="请输入处理人" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>

    <!-- ===== 详情弹框 ===== -->
    <el-dialog v-model="detailVisible" title="跌倒记录详情" width="520px">
      <div v-if="detailData" class="detail-content">
        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">老人姓名</span>
            <span class="detail-value">{{ detailData.name }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">跌倒时间</span>
            <span class="detail-value">{{ detailData.date }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">跌倒地点</span>
            <span class="detail-value">{{ detailData.location }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">跌倒原因</span>
            <span class="detail-value">{{ detailData.cause }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">受伤部位</span>
            <span class="detail-value">{{ detailData.injury }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">严重程度</span>
            <el-tag :type="getSeverityType(detailData.severity)" size="small">
              {{ detailData.severity }}
            </el-tag>
          </div>
          <div class="detail-item full-width">
            <span class="detail-label">处理方式</span>
            <span class="detail-value">{{ detailData.treatment || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">恢复状态</span>
            <el-tag :type="getStatusType(detailData.status)" size="small">
              {{ detailData.status }}
            </el-tag>
          </div>
          <div class="detail-item">
            <span class="detail-label">处理人</span>
            <span class="detail-value">{{ detailData.handler }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          type="primary"
          @click="
            () => {
              detailVisible = false
              handleEdit(detailData)
            }
          "
        >
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
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
  Edit,
  Delete,
  Filter,
  List,
  Download,
  Clock,
  Warning,
  CircleCheck,
} from '@element-plus/icons-vue'
import { getFallList } from '@/api/fall'
import { addFallRecord, updateFallRecord, deleteFallRecord } from '@/api/fallRecord'

// ===== 统计数据 =====
const stats = reactive({
  total: 0,
  recent: 0,
  highRisk: 0,
  avgRecoveryDays: 0,
})

// ===== 数据列表 =====
const fallList = ref([])

// ===== 图表引用 =====
const chartRef = ref(null)
let chartInstance = null

// ===== 严重程度枚举映射（后端 code → 页面中文） =====
const SEVERITY_TEXT_MAP = {
  MILD: '轻微',
  MODERATE: '中度',
  SEVERE: '重度',
  LIGHT: '轻微',
  MID: '中度',
  HEAVY: '重度',
}

// ===== 严重程度枚举映射（页面中文 → 后端 code） =====
const SEVERITY_CODE_MAP = {
  轻微: 'MILD',
  中度: 'MODERATE',
  重度: 'SEVERE',
}

// ===== 恢复状态枚举映射（后端 code → 页面中文） =====
const RECOVER_TEXT_MAP = {
  RECOVERED: '已恢复',
  RECOVERING: '康复中',
  SEQUELA: '后遗症',
}

// ===== 恢复状态枚举映射（页面中文 → 后端 code） =====
const RECOVER_CODE_MAP = {
  已恢复: 'RECOVERED',
  康复中: 'RECOVERING',
  后遗症: 'SEQUELA',
}

// ===== 老人姓名 → elderId 映射（新增/编辑时后端要求 elderId） =====
const elderIdMap = computed(() => {
  const map = {}
  fallList.value.forEach((item) => {
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
    const res = await getFallList({ page: 1, size: 10000 })
    const data = res.data.data
    fallList.value = (data?.list || []).map((item) => ({
      ...item,
      name: item.elderName ?? '',
      date: item.date ? String(item.date).replace('T', ' ') : item.date,
      injury: item.injuryPart ?? '',
      treatment: item.treatment ?? item.remark ?? '',
      severity: SEVERITY_TEXT_MAP[item.severity] || item.severity,
      status: RECOVER_TEXT_MAP[item.status] || item.status,
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
  const list = fallList.value
  stats.total = list.length

  const thirtyDaysAgo = new Date()
  thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30)
  stats.recent = list.filter((item) => new Date(item.date) >= thirtyDaysAgo).length

  stats.highRisk = list.filter(
    (item) => item.severity === '中度' || item.severity === '重度',
  ).length

  const recoveryDays = list
    .filter((item) => item.status === '已恢复')
    .map(() => Math.floor(Math.random() * 20 + 5))
  stats.avgRecoveryDays =
    recoveryDays.length > 0
      ? Math.floor(recoveryDays.reduce((a, b) => a + b, 0) / recoveryDays.length)
      : 0
}

// ===== 图表 =====
function initChart() {
  if (!chartRef.value) return
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }

  const causeData = {}
  fallList.value.forEach((item) => {
    causeData[item.cause] = (causeData[item.cause] || 0) + 1
  })
  const causeNames = Object.keys(causeData)
  const causeValues = Object.values(causeData)

  chartInstance = markRaw(echarts.init(chartRef.value))

  chartInstance.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}次 ({d}%)',
    },
    color: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399'],
    legend: {
      orient: 'vertical',
      right: 20,
      top: 'center',
      textStyle: { fontSize: 13 },
    },
    series: [
      {
        name: '跌倒原因分布',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['45%', '50%'],
        label: {
          show: true,
          formatter: '{b}\n{d}%',
          fontSize: 12,
        },
        labelLine: {
          length: 15,
          length2: 10,
        },
        data: causeNames.map((name, i) => ({
          name: name,
          value: causeValues[i],
        })),
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
  severity: '',
  cause: '',
  dateRange: [],
})

const severityOptions = [
  { label: '全部', value: '' },
  { label: '轻微', value: '轻微' },
  { label: '中度', value: '中度' },
  { label: '重度', value: '重度' },
]

const causeOptions = [
  { label: '全部', value: '' },
  { label: '地面湿滑', value: '地面湿滑' },
  { label: '头晕', value: '头晕' },
  { label: '绊倒', value: '绊倒' },
  { label: '其他', value: '其他' },
]

// ===== 分页 =====
const currentPage = ref(1)
const pageSize = ref(5)

// ===== 详情弹框 =====
const detailVisible = ref(false)
const detailData = ref(null)

// ===== 新增/编辑弹框 =====
const dialogVisible = ref(false)
const dialogTitle = ref('添加跌倒记录')
const isEdit = ref(false)
const editId = ref(null)

const formData = reactive({
  name: '',
  date: '',
  location: '',
  cause: '',
  injury: '',
  severity: '',
  treatment: '',
  status: '',
  handler: '',
})

const formRules = {
  name: [{ required: true, message: '请输入老人姓名', trigger: 'blur' }],
  date: [{ required: true, message: '请选择跌倒时间', trigger: 'change' }],
}

const formRef = ref(null)

// ===== 筛选数据 =====
const filteredList = computed(() => {
  let list = [...fallList.value]

  if (filterForm.name) {
    list = list.filter((item) => item.name.includes(filterForm.name))
  }
  if (filterForm.severity) {
    list = list.filter((item) => item.severity === filterForm.severity)
  }
  if (filterForm.cause) {
    list = list.filter((item) => item.cause === filterForm.cause)
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

// ===== 工具函数 =====
function getSeverityType(severity) {
  const map = {
    轻微: 'success',
    中度: 'warning',
    重度: 'danger',
  }
  return map[severity] || 'info'
}

function getStatusType(status) {
  const map = {
    已恢复: 'success',
    康复中: 'warning',
    后遗症: 'danger',
  }
  return map[status] || 'info'
}

// ===== 操作函数 =====
function handleView(row) {
  detailData.value = row
  detailVisible.value = true
}

function handleAdd() {
  isEdit.value = false
  editId.value = null
  dialogTitle.value = '添加跌倒记录'
  Object.assign(formData, {
    name: '',
    date: '',
    location: '',
    cause: '',
    injury: '',
    severity: '',
    treatment: '',
    status: '',
    handler: '',
  })
  formRef.value?.resetFields()
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑跌倒记录'
  Object.assign(formData, row)
  dialogVisible.value = true
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除 "${row.name}" 的跌倒记录吗？`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      await deleteFallRecord(row.id)
      ElMessage.success('删除成功')
      await initData()
    })
    .catch(() => {})
}

function resetFilter() {
  filterForm.name = ''
  filterForm.severity = ''
  filterForm.cause = ''
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
      ElMessage.warning('未找到该老人的档案信息，无法保存跌倒记录')
      return
    }

    const payload = {
      elderId: elderId,
      elderName: formData.name,
      fallTime: toBackendDateTime(formData.date),
      location: formData.location,
      cause: formData.cause,
      injuryPart: formData.injury,
      severity: SEVERITY_CODE_MAP[formData.severity] || formData.severity,
      treatment: formData.treatment,
      recoverStatus: RECOVER_CODE_MAP[formData.status] || formData.status,
      handler: formData.handler,
    }

    try {
      if (isEdit.value) {
        await updateFallRecord(editId.value, { ...payload, id: editId.value })
        ElMessage.success('更新成功')
      } else {
        await addFallRecord(payload)
        ElMessage.success('添加成功')
      }
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
/* ===== 页面容器 ===== */
.fall-page {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: 100%;
}

/* ===== 页面标题 ===== */
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
  color: #f56c6c;
}

.header-actions {
  display: flex;
  gap: 10px;
}

/* ===== 统计卡片 ===== */
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

.stat-card.recent .stat-icon {
  background: #fdf6ec;
  color: #e6a23c;
}

.stat-card.risk .stat-icon {
  background: #fef0f0;
  color: #f56c6c;
}

.stat-card.recovery .stat-icon {
  background: #f0f9eb;
  color: #67c23a;
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

/* ===== 图表区域 ===== */
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

/* ===== 筛选区域 ===== */
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

/* ===== 表格区域 ===== */
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

/* ===== 表格样式 ===== */
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

/* ===== 分页 ===== */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  margin-top: 16px;
}

/* ===== 详情弹框 ===== */
.detail-content {
  padding: 4px 0;
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

/* ===== 响应式 ===== */
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

  .fall-page {
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
