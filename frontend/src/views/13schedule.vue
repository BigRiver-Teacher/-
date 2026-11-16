<template>
  <div class="schedule-page">
    <!-- ===== 页面标题 ===== -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Calendar /></el-icon>
        日常康养日程
      </h2>
      <div class="header-actions">
        <el-button type="success" plain @click="handleExport">
          <el-icon><Download /></el-icon>
          导出日程
        </el-button>
        <el-button type="primary" plain @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- ===== 统计卡片 ===== -->
    <div class="stats-grid">
      <div class="stat-card total">
        <div class="stat-icon">
          <el-icon><Calendar /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.today }}</div>
          <div class="stat-label">今日任务</div>
        </div>
      </div>
      <div class="stat-card urgent">
        <div class="stat-icon">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.urgent }}</div>
          <div class="stat-label">紧急任务</div>
        </div>
      </div>
      <div class="stat-card pending">
        <div class="stat-icon">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.pending }}</div>
          <div class="stat-label">待执行</div>
        </div>
      </div>
      <div class="stat-card completed">
        <div class="stat-icon">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.completed }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
    </div>

    <!-- ===== 日期导航 ===== -->
    <div class="date-nav">
      <div class="date-nav-left">
        <el-button size="small" @click="prevDay">
          <el-icon><ArrowLeft /></el-icon>
          前一天
        </el-button>
        <el-button size="small" type="primary" @click="today">今天</el-button>
        <el-button size="small" @click="nextDay">
          后一天
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      <div class="date-nav-center">
        <span class="current-date">{{ currentDateDisplay }}</span>
        <span class="weekday">{{ currentWeekday }}</span>
      </div>
      <div class="date-nav-right">
        <el-button size="small" type="primary" plain @click="showAllTasks">
          <el-icon><List /></el-icon>
          查看全部任务
        </el-button>
      </div>
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
          <el-form-item label="任务类型">
            <el-select
              v-model="filterForm.taskType"
              placeholder="全部"
              clearable
              style="width: 140px"
            >
              <el-option
                v-for="item in typeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="任务状态">
            <el-select
              v-model="filterForm.status"
              placeholder="全部"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="关联老人">
            <el-input
              v-model="filterForm.elderName"
              placeholder="请输入姓名"
              clearable
              style="width: 150px"
              prefix-icon="Search"
            />
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- ===== 任务列表 ===== -->
    <div class="task-section">
      <div class="task-header">
        <div class="task-title">
          <el-icon><List /></el-icon>
          <span>任务列表</span>
          <el-tag size="small" type="info">共 {{ total }} 条</el-tag>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredList.length === 0" class="empty-state">
        <el-empty description="暂无任务" />
      </div>

      <!-- 按状态分组显示 -->
      <div v-else class="task-groups">
        <!-- 紧急任务 -->
        <div v-if="urgentTasks.length > 0" class="task-group">
          <div class="group-header">
            <span class="group-title urgent">
              <el-icon><Warning /></el-icon>
              紧急任务
            </span>
            <span class="group-count">{{ urgentTasks.length }}</span>
          </div>
          <div v-for="task in urgentTasks" :key="task.code" class="task-card urgent">
            <div class="task-left">
              <div class="task-icon urgent">
                <el-icon><Warning /></el-icon>
              </div>
            </div>
            <div class="task-center">
              <div class="task-row">
                <span class="task-type">{{ task.taskType }}</span>
                <span class="task-elder">{{ task.elderName || task.location }}</span>
                <el-tag type="danger" size="small">紧急</el-tag>
                <el-tag :type="getStatusType(task.status)" size="small">
                  {{ getStatusText(task.status) }}
                </el-tag>
              </div>
              <div class="task-row">
                <span class="task-trigger">触发原因：{{ task.triggerReason }}</span>
              </div>
              <div class="task-row">
                <span class="task-time">
                  <el-icon><Clock /></el-icon>
                  {{ task.createTime }}
                </span>
                <span v-if="task.relatedAlert" class="task-related">
                  关联告警：{{ task.relatedAlert }}
                </span>
                <span v-if="task.relatedDevice" class="task-related">
                  关联设备：{{ task.relatedDevice }}
                </span>
              </div>
            </div>
            <div class="task-right">
              <el-button
                v-if="task.status === 'pending'"
                size="small"
                type="primary"
                @click="handleProcess(task)"
              >
                处理
              </el-button>
              <el-button size="small" type="info" link @click="handleView(task)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
            </div>
          </div>
        </div>

        <!-- 待执行任务 -->
        <div v-if="pendingTasks.length > 0" class="task-group">
          <div class="group-header">
            <span class="group-title pending">
              <el-icon><Clock /></el-icon>
              待执行
            </span>
            <span class="group-count">{{ pendingTasks.length }}</span>
          </div>
          <div v-for="task in pendingTasks" :key="task.code" class="task-card">
            <div class="task-left">
              <div class="task-icon pending">
                <el-icon><Clock /></el-icon>
              </div>
            </div>
            <div class="task-center">
              <div class="task-row">
                <span class="task-type">{{ task.taskType }}</span>
                <span class="task-elder">{{ task.elderName || task.location }}</span>
                <el-tag :type="getPriorityType(task.priority)" size="small">
                  {{ getPriorityText(task.priority) }}
                </el-tag>
                <el-tag :type="getStatusType(task.status)" size="small">
                  {{ getStatusText(task.status) }}
                </el-tag>
              </div>
              <div class="task-row">
                <span class="task-trigger">触发原因：{{ task.triggerReason }}</span>
              </div>
              <div class="task-row">
                <span class="task-time">
                  <el-icon><Clock /></el-icon>
                  {{ task.createTime }}
                </span>
                <span v-if="task.relatedAlert" class="task-related">
                  关联告警：{{ task.relatedAlert }}
                </span>
                <span v-if="task.relatedDevice" class="task-related">
                  关联设备：{{ task.relatedDevice }}
                </span>
              </div>
            </div>
            <div class="task-right">
              <el-button
                v-if="task.status === 'pending'"
                size="small"
                type="primary"
                @click="handleProcess(task)"
              >
                处理
              </el-button>
              <el-button size="small" type="info" link @click="handleView(task)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
            </div>
          </div>
        </div>

        <!-- 已完成任务 -->
        <div v-if="completedTasks.length > 0" class="task-group">
          <div class="group-header">
            <span class="group-title completed">
              <el-icon><CircleCheck /></el-icon>
              已完成
            </span>
            <span class="group-count">{{ completedTasks.length }}</span>
          </div>
          <div v-for="task in completedTasks" :key="task.code" class="task-card completed">
            <div class="task-left">
              <div class="task-icon completed">
                <el-icon><CircleCheck /></el-icon>
              </div>
            </div>
            <div class="task-center">
              <div class="task-row">
                <span class="task-type">{{ task.taskType }}</span>
                <span class="task-elder">{{ task.elderName || task.location }}</span>
                <el-tag :type="getStatusType(task.status)" size="small">
                  {{ getStatusText(task.status) }}
                </el-tag>
              </div>
              <div class="task-row">
                <span class="task-trigger">触发原因：{{ task.triggerReason }}</span>
              </div>
              <div class="task-row">
                <span class="task-time">
                  <el-icon><Clock /></el-icon>
                  {{ task.createTime }}
                </span>
                <span v-if="task.completeTime" class="task-complete-time">
                  完成时间：{{ task.completeTime }}
                </span>
                <span v-if="task.handler" class="task-handler"> 处理人：{{ task.handler }} </span>
              </div>
              <div v-if="task.result" class="task-row">
                <span class="task-result">处理结果：{{ task.result }}</span>
              </div>
            </div>
            <div class="task-right">
              <el-button size="small" type="info" link @click="handleView(task)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          background
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 15, 20]"
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

    <!-- ===== 处理任务弹框 ===== -->
    <el-dialog
      v-model="processDialogVisible"
      title="处理任务"
      width="520px"
      :close-on-click-modal="false"
    >
      <div v-if="processTask" class="process-content">
        <div class="process-info">
          <div class="process-row">
            <span class="process-label">任务类型</span>
            <span class="process-value">{{ processTask.taskType }}</span>
          </div>
          <div class="process-row">
            <span class="process-label">关联对象</span>
            <span class="process-value">{{ processTask.elderName || processTask.location }}</span>
          </div>
          <div class="process-row">
            <span class="process-label">触发原因</span>
            <span class="process-value">{{ processTask.triggerReason }}</span>
          </div>
          <div v-if="processTask.relatedAlert" class="process-row">
            <span class="process-label">关联告警</span>
            <span class="process-value" style="font-family: monospace">{{
              processTask.relatedAlert
            }}</span>
          </div>
          <div v-if="processTask.relatedDevice" class="process-row">
            <span class="process-label">关联设备</span>
            <span class="process-value" style="font-family: monospace">{{
              processTask.relatedDevice
            }}</span>
          </div>
        </div>

        <el-divider />

        <el-form
          ref="processFormRef"
          :model="processForm"
          :rules="processRules"
          label-width="100px"
        >
          <el-form-item v-if="processTask.taskType === '数据复核'" label="复核结果" prop="result">
            <el-select
              v-model="processForm.result"
              placeholder="请选择复核结果"
              style="width: 100%"
            >
              <el-option label="确认告警（真实事件）" value="确认告警" />
              <el-option label="误报（无需处理）" value="误报" />
              <el-option label="需进一步观察" value="需进一步观察" />
            </el-select>
          </el-form-item>
          <el-form-item
            v-else-if="processTask.taskType === '设备巡检'"
            label="巡检结果"
            prop="result"
          >
            <el-select
              v-model="processForm.result"
              placeholder="请选择巡检结果"
              style="width: 100%"
            >
              <el-option label="正常" value="正常" />
              <el-option label="需维修" value="需维修" />
              <el-option label="需更换" value="需更换" />
              <el-option label="异常" value="异常" />
            </el-select>
          </el-form-item>
          <el-form-item
            v-else-if="processTask.taskType === '设备佩戴检查'"
            label="检查结果"
            prop="result"
          >
            <el-select
              v-model="processForm.result"
              placeholder="请选择检查结果"
              style="width: 100%"
            >
              <el-option label="正常佩戴" value="正常佩戴" />
              <el-option label="未佩戴" value="未佩戴" />
              <el-option label="佩戴不规范" value="佩戴不规范" />
            </el-select>
          </el-form-item>
          <el-form-item
            v-else-if="processTask.taskType === '风险评估'"
            label="评估结果"
            prop="result"
          >
            <el-select
              v-model="processForm.result"
              placeholder="请选择评估结果"
              style="width: 100%"
            >
              <el-option label="高风险" value="高风险" />
              <el-option label="中风险" value="中风险" />
              <el-option label="低风险" value="低风险" />
            </el-select>
          </el-form-item>
          <el-form-item v-else label="处理结果" prop="result">
            <el-input v-model="processForm.result" placeholder="请输入处理结果" />
          </el-form-item>
          <el-form-item label="处理备注">
            <el-input
              v-model="processForm.note"
              type="textarea"
              :rows="3"
              placeholder="请输入处理备注..."
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProcess">确认处理</el-button>
      </template>
    </el-dialog>

    <!-- ===== 查看任务详情弹框 ===== -->
    <el-dialog v-model="detailVisible" title="任务详情" width="540px">
      <div v-if="detailTask" class="detail-content">
        <div class="detail-header">
          <span class="detail-type">{{ detailTask.taskType }}</span>
          <el-tag :type="getPriorityType(detailTask.priority)" size="small">
            {{ getPriorityText(detailTask.priority) }}
          </el-tag>
          <el-tag :type="getStatusType(detailTask.status)" size="small">
            {{ getStatusText(detailTask.status) }}
          </el-tag>
        </div>

        <el-divider />

        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">任务编号</span>
            <span class="detail-value" style="font-family: monospace">{{ detailTask.code }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">关联对象</span>
            <span class="detail-value">{{ detailTask.elderName || detailTask.location }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">触发原因</span>
            <span class="detail-value">{{ detailTask.triggerReason }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">创建时间</span>
            <span class="detail-value">{{ detailTask.createTime }}</span>
          </div>
          <div v-if="detailTask.relatedAlert" class="detail-item">
            <span class="detail-label">关联告警</span>
            <span class="detail-value" style="font-family: monospace">{{
              detailTask.relatedAlert
            }}</span>
          </div>
          <div v-if="detailTask.relatedDevice" class="detail-item">
            <span class="detail-label">关联设备</span>
            <span class="detail-value" style="font-family: monospace">{{
              detailTask.relatedDevice
            }}</span>
          </div>
          <div v-if="detailTask.status === 'completed'" class="detail-item">
            <span class="detail-label">处理结果</span>
            <span class="detail-value">{{ detailTask.result || '-' }}</span>
          </div>
          <div v-if="detailTask.status === 'completed'" class="detail-item">
            <span class="detail-label">处理人</span>
            <span class="detail-value">{{ detailTask.handler || '-' }}</span>
          </div>
          <div v-if="detailTask.status === 'completed'" class="detail-item">
            <span class="detail-label">完成时间</span>
            <span class="detail-value">{{ detailTask.completeTime || '-' }}</span>
          </div>
          <div v-if="detailTask.note" class="detail-item full-width">
            <span class="detail-label">处理备注</span>
            <span class="detail-value">{{ detailTask.note }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="detailTask?.status === 'pending'"
          type="primary"
          @click="
            () => {
              detailVisible = false
              handleProcess(detailTask)
            }
          "
        >
          处理任务
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Calendar,
  Warning,
  Clock,
  CircleCheck,
  Refresh,
  Download,
  Filter,
  Search,
  List,
  View,
  ArrowLeft,
  ArrowRight,
  Edit,
} from '@element-plus/icons-vue'
import { getScheduleTasks, getScheduleTaskById, processScheduleTask } from '@/api/schedule'

// ===== 当前日期 =====
const currentDate = ref(new Date())
const showAll = ref(false)

// ===== 统计数据 =====
const stats = reactive({
  today: 0,
  urgent: 0,
  pending: 0,
  completed: 0,
})

// ===== 数据来源：后端 API（GET /schedule/tasks?date=YYYY-MM-DD） =====

// ===== 数据列表 =====
const taskList = ref([])

// ===== 筛选条件 =====
const filterForm = reactive({
  taskType: '',
  status: '',
  elderName: '',
})

const typeOptions = [
  { label: '全部', value: '' },
  { label: '数据复核', value: '数据复核' },
  { label: '设备巡检', value: '设备巡检' },
  { label: '设备佩戴检查', value: '设备佩戴检查' },
  { label: '风险评估', value: '风险评估' },
  { label: '家属沟通', value: '家属沟通' },
]

const statusOptions = [
  { label: '全部', value: '' },
  { label: '待执行', value: 'pending' },
  { label: '执行中', value: 'processing' },
  { label: '已完成', value: 'completed' },
  { label: '已取消', value: 'cancelled' },
]

// ===== 分页 =====
const currentPage = ref(1)
const pageSize = ref(5)

// ===== 处理弹框 =====
const processDialogVisible = ref(false)
const processTask = ref(null)
const processFormRef = ref(null)

const processForm = reactive({
  result: '',
  note: '',
})

const processRules = {
  result: [{ required: true, message: '请选择处理结果', trigger: 'change' }],
}

// ===== 详情弹框 =====
const detailVisible = ref(false)
const detailTask = ref(null)

// ===== 日期计算 =====
const currentDateDisplay = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = String(currentDate.value.getMonth() + 1).padStart(2, '0')
  const day = String(currentDate.value.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
})

const currentWeekday = computed(() => {
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  return weekdays[currentDate.value.getDay()]
})

const dateStr = computed(() => {
  const d = currentDate.value
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
})

// ===== 获取当天任务 =====
const todayTasks = computed(() => {
  return taskList.value.filter((task) => task.createTime.startsWith(dateStr.value))
})

// ===== 字段映射 =====
function formatDateTime(value) {
  if (!value) return ''
  return String(value).replace('T', ' ')
}

function mapTask(item) {
  return {
    id: item.id,
    code: item.taskCode,
    taskType: item.taskType,
    elderName: item.elderName || '',
    location: item.location || '',
    priority: item.priority,
    status: item.taskStatus,
    triggerReason: item.triggerReson,
    createTime: formatDateTime(item.createTime),
    relatedAlert: item.relateAlertCode || '',
    relatedDevice: item.relateDeviceSn || '',
    result: item.handleResult || '',
    handler: item.handler || '',
    completeTime: formatDateTime(item.completeTime),
    note: item.note || '',
    taskDate: item.taskDate,
  }
}

// ===== 加载数据（按日期） =====
async function loadData() {
  try {
    const res = await getScheduleTasks({ date: dateStr.value, page: 1, size: 1000 })
    const data = res.data.data
    taskList.value = (data.list || []).map(mapTask)
    updateStats()
  } catch {
    // 错误已由响应拦截器统一提示
  }
}

// ===== 更新统计数据 =====
function updateStats() {
  const list = todayTasks.value
  stats.today = list.length
  stats.urgent = list.filter(
    (item) =>
      item.priority === 'urgent' && item.status !== 'completed' && item.status !== 'cancelled',
  ).length
  stats.pending = list.filter(
    (item) => item.status === 'pending' || item.status === 'processing',
  ).length
  stats.completed = list.filter((item) => item.status === 'completed').length
}

// ===== 筛选数据 =====
const filteredList = computed(() => {
  let list = [...todayTasks.value]

  if (filterForm.taskType) {
    list = list.filter((item) => item.taskType === filterForm.taskType)
  }
  if (filterForm.status) {
    list = list.filter((item) => item.status === filterForm.status)
  }
  if (filterForm.elderName) {
    list = list.filter((item) => item.elderName && item.elderName.includes(filterForm.elderName))
  }

  return list
})

const total = computed(() => filteredList.value.length)

const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredList.value.slice(start, end)
})

// ===== 按状态分组 =====
const urgentTasks = computed(() => {
  return paginatedList.value.filter(
    (task) =>
      task.priority === 'urgent' && task.status !== 'completed' && task.status !== 'cancelled',
  )
})

const pendingTasks = computed(() => {
  return paginatedList.value.filter(
    (task) => task.status === 'pending' && task.priority !== 'urgent',
  )
})

const completedTasks = computed(() => {
  return paginatedList.value.filter(
    (task) => task.status === 'completed' || task.status === 'cancelled',
  )
})

// ===== 日期导航 =====
function prevDay() {
  const d = new Date(currentDate.value)
  d.setDate(d.getDate() - 1)
  currentDate.value = d
  loadData()
  currentPage.value = 1
}

function nextDay() {
  const d = new Date(currentDate.value)
  d.setDate(d.getDate() + 1)
  currentDate.value = d
  loadData()
  currentPage.value = 1
}

function today() {
  currentDate.value = new Date()
  loadData()
  currentPage.value = 1
}

function showAllTasks() {
  showAll.value = !showAll.value
}

// ===== 工具函数 =====
function getStatusType(status) {
  const map = {
    pending: 'warning',
    processing: 'primary',
    completed: 'success',
    cancelled: 'info',
  }
  return map[status] || 'info'
}

function getStatusText(status) {
  const map = {
    pending: '待执行',
    processing: '执行中',
    completed: '已完成',
    cancelled: '已取消',
  }
  return map[status] || status
}

function getPriorityType(priority) {
  const map = {
    urgent: 'danger',
    important: 'warning',
    normal: 'info',
  }
  return map[priority] || 'info'
}

function getPriorityText(priority) {
  const map = {
    urgent: '紧急',
    important: '重要',
    normal: '普通',
  }
  return map[priority] || priority
}

// ===== 操作函数 =====
async function handleView(task) {
  let detail = task
  try {
    const res = await getScheduleTaskById(task.id)
    if (res.data.data) {
      detail = mapTask(res.data.data)
    }
  } catch {
    // 使用列表数据兜底
  }
  detailTask.value = detail
  detailVisible.value = true
}

function handleProcess(task) {
  processTask.value = task
  processForm.result = ''
  processForm.note = ''
  processFormRef.value?.resetFields()
  processDialogVisible.value = true
}

function handleExport() {
  ElMessage.success('日程导出成功')
}

function resetFilter() {
  filterForm.taskType = ''
  filterForm.status = ''
  filterForm.elderName = ''
  currentPage.value = 1
}

async function refreshData() {
  await loadData()
  ElMessage.success('数据已刷新')
}

// ===== 提交处理 =====
async function submitProcess() {
  const valid = await processFormRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    const target = processTask.value
    if (target && target.id) {
      await processScheduleTask(target.id, {
        taskStatus: 'completed',
        handleResult: processForm.result,
        handler: '管理员',
        note: processForm.note,
      })
    }

    const index = taskList.value.findIndex(
      (t) => t.code === target.code && t.createTime === target.createTime,
    )

    if (index > -1) {
      taskList.value[index].status = 'completed'
      taskList.value[index].result = processForm.result
      taskList.value[index].handler = '管理员'
      taskList.value[index].completeTime = new Date().toLocaleString('zh-CN')
      taskList.value[index].note = processForm.note || taskList.value[index].note

      ElMessage.success('任务处理完成')
      processDialogVisible.value = false
      updateStats()
    }
  } catch {
    // 错误已由响应拦截器统一提示
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.schedule-page {
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
  color: #409eff;
}

.header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
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
.stat-card.urgent .stat-icon {
  background: #fef0f0;
  color: #f56c6c;
}
.stat-card.pending .stat-icon {
  background: #fdf6ec;
  color: #e6a23c;
}
.stat-card.completed .stat-icon {
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

/* ===== 日期导航 ===== */
.date-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-radius: 12px;
  padding: 12px 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.date-nav-left {
  display: flex;
  gap: 8px;
  align-items: center;
}

.date-nav-center {
  display: flex;
  align-items: center;
  gap: 12px;
}

.current-date {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.weekday {
  font-size: 14px;
  color: #999;
}

.date-nav-right {
  display: flex;
  gap: 8px;
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

/* ===== 任务区域 ===== */
.task-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 16px;
}

.task-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.task-title .el-icon {
  font-size: 18px;
  color: #409eff;
}

.empty-state {
  padding: 40px 0;
}

/* ===== 任务分组 ===== */
.task-groups {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.task-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.group-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
}

.group-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.group-title.urgent {
  color: #f56c6c;
}
.group-title.pending {
  color: #e6a23c;
}
.group-title.completed {
  color: #67c23a;
}

.group-count {
  font-size: 12px;
  color: #999;
  background: #f5f5f5;
  padding: 0 10px;
  border-radius: 12px;
  line-height: 22px;
}

/* ===== 任务卡片 ===== */
.task-card {
  display: flex;
  align-items: stretch;
  background: #f8f9fa;
  border-radius: 10px;
  padding: 12px 16px;
  border-left: 4px solid #909399;
  transition: all 0.3s;
}

.task-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transform: translateX(4px);
}

.task-card.urgent {
  border-left-color: #f56c6c;
  background: #fef6f6;
}

.task-card.completed {
  opacity: 0.7;
}

.task-left {
  display: flex;
  align-items: center;
  padding-right: 12px;
  flex-shrink: 0;
}

.task-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: #fff;
}

.task-icon.urgent {
  background: #f56c6c;
}
.task-icon.pending {
  background: #e6a23c;
}
.task-icon.completed {
  background: #67c23a;
}

.task-center {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.task-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.task-type {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
}

.task-elder {
  font-size: 14px;
  color: #409eff;
  font-weight: 500;
}

.task-trigger {
  font-size: 13px;
  color: #666;
}

.task-time {
  font-size: 13px;
  color: #999;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.task-related {
  font-size: 12px;
  color: #999;
  background: #f0f0f0;
  padding: 2px 10px;
  border-radius: 4px;
}

.task-complete-time {
  font-size: 13px;
  color: #67c23a;
}

.task-handler {
  font-size: 13px;
  color: #666;
}

.task-result {
  font-size: 13px;
  color: #409eff;
  font-weight: 500;
}

.task-right {
  display: flex;
  align-items: center;
  gap: 6px;
  padding-left: 12px;
  flex-shrink: 0;
}

.task-right .el-button {
  font-size: 12px;
  padding: 4px 10px;
}

.task-right .el-button .el-icon {
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

/* ===== 处理弹框 ===== */
.process-content {
  padding: 4px 0;
}

.process-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4px 16px;
}

.process-row {
  display: flex;
  flex-direction: column;
  padding: 4px 0;
}

.process-label {
  font-size: 12px;
  color: #999;
}

.process-value {
  font-size: 14px;
  color: #333;
}

/* ===== 详情弹框 ===== */
.detail-content {
  padding: 4px 0;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.detail-type {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px 20px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  padding: 4px 0;
  border-bottom: 1px solid #f5f5f5;
}

.detail-item.full-width {
  grid-column: 1 / -1;
}

.detail-label {
  font-size: 12px;
  color: #999;
}

.detail-value {
  font-size: 14px;
  color: #333;
  word-break: break-all;
}

/* ===== 响应式 ===== */
@media (max-width: 1200px) {
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
    grid-template-columns: 1fr 1fr;
  }

  .date-nav {
    flex-direction: column;
    gap: 12px;
  }

  .date-nav-left {
    width: 100%;
    justify-content: center;
  }

  .date-nav-center {
    width: 100%;
    justify-content: center;
  }

  .date-nav-right {
    width: 100%;
    justify-content: center;
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

  .task-card {
    flex-direction: column;
    gap: 8px;
  }

  .task-left {
    padding-right: 0;
  }

  .task-right {
    padding-left: 0;
    padding-top: 8px;
    border-top: 1px solid #f0f0f0;
    flex-wrap: wrap;
  }

  .process-info {
    grid-template-columns: 1fr;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .schedule-page {
    padding: 12px;
  }

  .header-actions {
    flex-wrap: wrap;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .page-title {
    font-size: 18px;
  }

  .stat-number {
    font-size: 22px;
  }

  .stat-card {
    padding: 16px;
  }

  .current-date {
    font-size: 16px;
  }

  .task-row {
    gap: 4px;
  }

  .task-type {
    font-size: 13px;
  }
}
</style>