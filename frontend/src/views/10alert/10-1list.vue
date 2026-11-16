<template>
  <div class="alert-list-page">
    <!-- ===== 页面标题 ===== -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Bell /></el-icon>
        实时告警列表
        <el-tag v-if="newAlertCount > 0" type="danger" size="large" class="new-alert-tag">
          新告警 {{ newAlertCount }}
        </el-tag>
      </h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleBatchProcess">
          <el-icon><Operation /></el-icon>
          批量处理
        </el-button>
        <el-button type="primary" plain @click="handleBatchRead">
          <el-icon><View /></el-icon>
          批量标记已读
        </el-button>
        <el-button type="danger" plain @click="handleBatchIgnore">
          <el-icon><CircleClose /></el-icon>
          批量忽略
        </el-button>
        <el-button type="primary" plain @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-select v-model="refreshInterval" size="small" style="width: 100px; margin-left: 8px">
          <el-option label="5秒" :value="5" />
          <el-option label="10秒" :value="10" />
          <el-option label="30秒" :value="30" />
        </el-select>
      </div>
    </div>

    <!-- ===== 统计卡片 ===== -->
    <div class="stats-grid">
      <div class="stat-card total">
        <div class="stat-icon">
          <el-icon><Bell /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.total }}</div>
          <div class="stat-label">总告警数</div>
        </div>
      </div>
      <div class="stat-card pending">
        <div class="stat-icon">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.pending }}</div>
          <div class="stat-label">未处理</div>
        </div>
      </div>
      <div class="stat-card processing">
        <div class="stat-icon">
          <el-icon><Timer /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.processing }}</div>
          <div class="stat-label">处理中</div>
        </div>
      </div>
      <div class="stat-card today">
        <div class="stat-icon">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.todayNew }}</div>
          <div class="stat-label">今日新增</div>
        </div>
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
          <el-form-item label="老人姓名">
            <el-input
              v-model="filterForm.elderName"
              placeholder="请输入姓名"
              clearable
              style="width: 150px"
              prefix-icon="Search"
            />
          </el-form-item>
          <el-form-item label="告警类型">
            <el-select
              v-model="filterForm.alertType"
              placeholder="全部"
              clearable
              style="width: 140px"
            >
              <el-option
                v-for="item in alertTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="告警级别">
            <el-select v-model="filterForm.level" placeholder="全部" clearable style="width: 120px">
              <el-option
                v-for="item in levelOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="处理状态">
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
          <el-form-item label="时间范围">
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
          <span>告警列表</span>
          <el-tag size="small" type="info">共 {{ total }} 条</el-tag>
          <el-tag v-if="unreadCount > 0" size="small" type="danger">
            未读 {{ unreadCount }}
          </el-tag>
        </div>
        <div class="table-actions">
          <el-button size="small" @click="toggleSound">
            <el-icon><Microphone /></el-icon>
            {{ soundEnabled ? '声音开启' : '声音关闭' }}
          </el-button>
        </div>
      </div>

      <el-table
        :data="paginatedList"
        stripe
        style="width: 100%"
        row-key="index"
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="45" align="center" />
        <el-table-column label="序号" width="60" align="center">
          <template #default="{ $index }">
            <span style="font-size: 12px; color: #999">
              {{ $index + 1 }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="elderName" label="老人姓名" width="90" align="center" />
        <el-table-column prop="alertType" label="告警类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getAlertTypeColor(row.alertType)" size="small">
              {{ getAlertTypeText(row.alertType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="告警级别" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.level)" size="small" effect="dark">
              {{ getLevelText(row.level) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="告警描述" min-width="180">
          <template #default="{ row }">
            <el-tooltip :content="row.description" placement="top">
              <span class="desc-text">{{ row.description }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="告警时间" width="160" align="center" />
        <el-table-column prop="location" label="位置" width="120" align="center" />
        <el-table-column prop="status" label="处理状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right" align="center">
          <template #default="{ row, $index }">
            <div class="action-buttons">
              <el-button size="small" type="primary" link @click="handleView(row, $index)">
                <el-icon><View /></el-icon>
                详情
              </el-button>
              <el-button
                v-if="row.status === 'pending' || row.status === 'processing'"
                size="small"
                type="success"
                link
                @click="handleProcess(row, $index)"
              >
                <el-icon><Edit /></el-icon>
                处理
              </el-button>
              <el-button
                v-if="row.status === 'pending'"
                size="small"
                type="info"
                link
                @click="handleIgnore(row, $index)"
              >
                <el-icon><CircleClose /></el-icon>
                忽略
              </el-button>
              <el-button size="small" type="danger" link @click="handleDelete($index)">
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

    <!-- ===== 查看详情弹框 ===== -->
    <el-dialog v-model="detailVisible" title="告警详情" width="560px">
      <div v-if="detailData" class="detail-content">
        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">老人姓名</span>
            <span class="detail-value">{{ detailData.elderName }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">告警类型</span>
            <el-tag :type="getAlertTypeColor(detailData.alertType)" size="small">
              {{ getAlertTypeText(detailData.alertType) }}
            </el-tag>
          </div>
          <div class="detail-item">
            <span class="detail-label">告警级别</span>
            <el-tag :type="getLevelType(detailData.level)" size="small" effect="dark">
              {{ getLevelText(detailData.level) }}
            </el-tag>
          </div>
          <div class="detail-item">
            <span class="detail-label">告警描述</span>
            <span class="detail-value">{{ detailData.description }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">告警时间</span>
            <span class="detail-value">{{ detailData.time }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">位置</span>
            <span class="detail-value">{{ detailData.location }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">处理状态</span>
            <el-tag :type="getStatusType(detailData.status)" size="small">
              {{ getStatusText(detailData.status) }}
            </el-tag>
          </div>
          <div v-if="detailData.handler" class="detail-item">
            <span class="detail-label">处理人</span>
            <span class="detail-value">{{ detailData.handler }}</span>
          </div>
          <div v-if="detailData.handleTime" class="detail-item">
            <span class="detail-label">处理时间</span>
            <span class="detail-value">{{ detailData.handleTime }}</span>
          </div>
          <div v-if="detailData.handleNote" class="detail-item">
            <span class="detail-label">处理备注</span>
            <span class="detail-value">{{ detailData.handleNote }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="detailData?.status === 'pending'"
          type="primary"
          @click="
            () => {
              detailVisible = false
              handleProcess(detailData, detailIndex)
            }
          "
        >
          处理告警
        </el-button>
      </template>
    </el-dialog>

    <!-- ===== 处理告警弹框 ===== -->
    <el-dialog
      v-model="processDialogVisible"
      title="处理告警"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form label-width="100px">
        <el-form-item label="老人姓名">
          <span>{{ processData?.elderName }}</span>
        </el-form-item>
        <el-form-item label="告警类型">
          <span>{{ getAlertTypeText(processData?.alertType) }}</span>
        </el-form-item>
        <el-form-item label="处理措施" required>
          <el-select v-model="processForm.action" placeholder="请选择处理措施" style="width: 100%">
            <el-option label="上门查看" value="上门查看" />
            <el-option label="联系家属" value="联系家属" />
            <el-option label="送医就诊" value="送医就诊" />
            <el-option label="设备检查" value="设备检查" />
            <el-option label="电话回访" value="电话回访" />
            <el-option label="其他" value="其他" />
          </el-select>
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
      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProcess">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Bell,
  Warning,
  Timer,
  Clock,
  Refresh,
  View,
  Edit,
  Delete,
  Filter,
  List,
  Operation,
  CircleClose,
  Search,
  Microphone,
} from '@element-plus/icons-vue'
import { getAlertList, processAlert, ignoreAlert, markAlertRead, batchProcessAlerts, batchIgnoreAlerts } from '@/api/alert'

// ===== 统计数据 =====
const stats = reactive({
  total: 0,
  pending: 0,
  processing: 0,
  todayNew: 0,
})

// ===== 已读记录集合（后端无 read 字段，本地维护，跨刷新保持已读状态） =====
const readIds = new Set<number>()

// ===== 数据列表 =====
const alertList = ref([])
const selectedAlerts = ref([])

// ===== 当前操作的索引 =====
const currentIndex = ref(-1)

// ===== 刷新定时器 =====
let refreshTimer = null
const refreshInterval = ref(5)
const soundEnabled = ref(true)
const newAlertCount = ref(0)

// ===== 筛选条件 =====
const filterForm = reactive({
  elderName: '',
  alertType: '',
  level: '',
  status: '',
  dateRange: [],
})

const alertTypeOptions = [
  { label: '全部', value: '' },
  { label: '跌倒', value: 'FALL' },
  { label: '设备离线', value: 'DEVICE_OFFLINE' },
  { label: '紧急呼叫', value: 'EMERGENCY' },
  { label: '异常步态', value: 'ABNORMAL_GAIT' },
  { label: '心率异常', value: 'HEART_RATE' },
  { label: '血压异常', value: 'BLOOD_PRESSURE' },
]

const levelOptions = [
  { label: '全部', value: '' },
  { label: '紧急', value: 'urgent' },
  { label: '重要', value: 'important' },
  { label: '普通', value: 'normal' },
]

const statusOptions = [
  { label: '全部', value: '' },
  { label: '待处理', value: 'pending' },
  { label: '处理中', value: 'processing' },
  { label: '已处理', value: 'resolved' },
  { label: '已忽略', value: 'ignored' },
]

// ===== 分页 =====
const currentPage = ref(1)
const pageSize = ref(5)

// ===== 详情弹框 =====
const detailVisible = ref(false)
const detailData = ref(null)
const detailIndex = ref(-1)

// ===== 处理弹框 =====
const processDialogVisible = ref(false)
const processData = ref(null)
const processIndex = ref(-1)
const processForm = reactive({
  action: '',
  note: '',
})

// ===== 加载告警数据（分页+筛选） =====
async function loadAlerts() {
  try {
    // 后端查询参数名：alertType/level/status/elderName/startDate/endDate
    const params: Record<string, any> = { page: 1, size: 1000 }
    if (filterForm.elderName) params.elderName = filterForm.elderName
    if (filterForm.alertType) params.alertType = filterForm.alertType
    if (filterForm.level) params.level = filterForm.level
    if (filterForm.status) params.status = filterForm.status.toUpperCase()
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startDate = filterForm.dateRange[0]
      params.endDate = filterForm.dateRange[1]
    }
    const res = await getAlertList(params as any)
    const data = res.data.data
    const list = data?.list ?? []
    const prevIds = new Set(alertList.value.map((i: any) => i.id))
    alertList.value = list.map((item) => ({
      id: item.id,
      elderName: item.elderlyName,
      alertType: item.warningType,
      level: item.priority,
      description: item.description,
      time: item.time ? String(item.time).replace('T', ' ') : item.time,
      location: item.location,
      status: item.status ? String(item.status).toLowerCase() : item.status,
      handler: item.handler || '',
      handleTime: item.handleTime ? String(item.handleTime).replace('T', ' ') : item.handleTime,
      handleNote: '',
      read:
        readIds.has(item.id) ||
        !['pending', 'processing'].includes(String(item.status || '').toLowerCase()),
    }))
    // 按重要程度降序排列：紧急 > 重要 > 普通
    sortByLevel()
    updateStats()
    newAlertCount.value = alertList.value.filter((item) => !item.read).length
    const newIds = alertList.value.filter((i: any) => !prevIds.has(i.id))
    if (prevIds.size > 0 && newIds.length > 0) {
      if (soundEnabled.value) {
        // 播放提示音（可选）
      }
      ElMessage.info('有新告警到达')
    }
  } catch {
    // 请求失败时由响应拦截器统一提示
  }
}

// ===== 初始化数据 =====
function initData() {
  loadAlerts()
}

// ===== 按重要程度降序排列 =====
function sortByLevel() {
  const levelOrder: Record<string, number> = { high: 0, medium: 1, low: 2, urgent: 0, important: 1, normal: 2 }
  alertList.value.sort((a, b) => {
    return levelOrder[a.level] - levelOrder[b.level]
  })
}

// ===== 更新统计数据 =====
function updateStats() {
  const list = alertList.value
  stats.total = list.length
  stats.pending = list.filter((item) => item.status === 'pending').length
  stats.processing = list.filter((item) => item.status === 'processing').length
  const today = new Date().toISOString().split('T')[0]
  stats.todayNew = list.filter((item) => (item.time || '').startsWith(today)).length
}

// ===== 筛选数据 =====
const filteredList = computed(() => {
  let list = [...alertList.value]

  if (filterForm.elderName) {
    list = list.filter((item) => item.elderName.includes(filterForm.elderName))
  }
  if (filterForm.alertType) {
    list = list.filter((item) => item.alertType === filterForm.alertType)
  }
  if (filterForm.level) {
    list = list.filter((item) => item.level === filterForm.level)
  }
  if (filterForm.status) {
    list = list.filter((item) => item.status === filterForm.status)
  }
  if (filterForm.dateRange && filterForm.dateRange.length === 2) {
    list = list.filter((item) => {
      const date = item.time.split(' ')[0]
      return date >= filterForm.dateRange[0] && date <= filterForm.dateRange[1]
    })
  }

  // 按重要程度降序排列（筛选后重新排序）
  const levelOrder: Record<string, number> = { high: 0, medium: 1, low: 2, urgent: 0, important: 1, normal: 2 }
  list.sort((a, b) => {
    return levelOrder[a.level] - levelOrder[b.level]
  })

  return list
})

const total = computed(() => filteredList.value.length)
const unreadCount = computed(() => {
  return filteredList.value.filter((item) => !item.read).length
})

const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredList.value.slice(start, end)
})

// ===== 工具函数 =====
const ALERT_TYPE_TEXT = {
  FALL: '跌倒',
  DEVICE_OFFLINE: '设备离线',
  EMERGENCY: '紧急呼叫',
  ABNORMAL_GAIT: '异常步态',
  HEART_RATE: '心率异常',
  BLOOD_PRESSURE: '血压异常',
  SOS: 'SOS紧急呼叫',
}

function getAlertTypeText(type) {
  return ALERT_TYPE_TEXT[type] || type || '-'
}

function getAlertTypeColor(type) {
  const map = {
    FALL: 'danger',
    EMERGENCY: 'danger',
    SOS: 'danger',
    HEART_RATE: 'warning',
    BLOOD_PRESSURE: 'warning',
    ABNORMAL_GAIT: 'warning',
    DEVICE_OFFLINE: 'info',
  }
  return map[type] || 'info'
}

function getLevelType(level) {
  const map = {
    high: 'danger',
    medium: 'warning',
    low: 'info',
    urgent: 'danger',
    important: 'warning',
    normal: 'info',
  }
  return map[level] || 'info'
}

function getLevelText(level) {
  const map = {
    high: '紧急',
    medium: '重要',
    low: '普通',
    urgent: '紧急',
    important: '重要',
    normal: '普通',
  }
  return map[level] || level
}

function getStatusType(status) {
  const map = {
    pending: 'danger',
    processing: 'warning',
    resolved: 'success',
    ignored: 'info',
  }
  return map[status] || 'info'
}

function getStatusText(status) {
  const map = {
    pending: '待处理',
    processing: '处理中',
    resolved: '已处理',
    ignored: '已忽略',
  }
  return map[status] || status
}

// ===== 操作函数 =====
function handleView(row, index) {
  row.read = true
  if (row.id != null) readIds.add(row.id)
  detailData.value = row
  detailIndex.value = index
  detailVisible.value = true
  updateStats()
  newAlertCount.value = alertList.value.filter((item) => !item.read).length
  if (row.id != null) {
    markAlertRead(row.id).catch(() => {})
  }
}

function handleProcess(row, index) {
  processData.value = row
  processIndex.value = index
  processForm.action = ''
  processForm.note = ''
  processDialogVisible.value = true
}

function handleIgnore(row, index) {
  ElMessageBox.confirm(`确定要忽略 "${row.elderName}" 的告警吗？`, '忽略确认', {
    confirmButtonText: '确定忽略',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      const row = alertList.value[index]
      if (!row) return
      await ignoreAlert(row.id)
      row.status = 'ignored'
      row.read = true
      if (row.id != null) readIds.add(row.id)
      ElMessage.success('已忽略该告警')
      sortByLevel()
      updateStats()
      newAlertCount.value = alertList.value.filter((item) => !item.read).length
    })
    .catch(() => {})
}

function handleDelete(index) {
  ElMessageBox.confirm(`确定要删除该告警记录吗？`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      alertList.value.splice(index, 1)
      ElMessage.success('删除成功')
      updateStats()
      if (currentPage.value > Math.ceil(total.value / pageSize.value)) {
        currentPage.value = 1
      }
    })
    .catch(() => {})
}

function handleSelectionChange(selection) {
  selectedAlerts.value = selection
}

// ===== 批量操作 =====
function handleBatchProcess() {
  if (selectedAlerts.value.length === 0) {
    ElMessage.warning('请先选择告警')
    return
  }
  if (
    selectedAlerts.value.some((item) => item.status === 'resolved' || item.status === 'ignored')
  ) {
    ElMessage.warning('选中的告警中已有已处理或已忽略的项')
    return
  }

  ElMessageBox.confirm(`确定要批量处理 ${selectedAlerts.value.length} 条告警吗？`, '批量处理确认', {
    confirmButtonText: '确定处理',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      const ids = selectedAlerts.value.map((row) => row.id).filter((id) => id != null)
      await batchProcessAlerts(ids)
      selectedAlerts.value.forEach((row) => {
        const index = alertList.value.findIndex(
          (item) =>
            item.elderName === row.elderName &&
            item.time === row.time &&
            item.alertType === row.alertType,
        )
        if (index > -1) {
          alertList.value[index].status = 'processing'
          alertList.value[index].handler = '管理员'
          alertList.value[index].handleTime = new Date().toLocaleString('zh-CN')
        }
      })
      ElMessage.success(`已处理 ${selectedAlerts.value.length} 条告警`)
      sortByLevel()
      updateStats()
      selectedAlerts.value = []
    })
    .catch(() => {})
}

async function handleBatchRead() {
  if (selectedAlerts.value.length === 0) {
    ElMessage.warning('请先选择告警')
    return
  }
  const ids = selectedAlerts.value.map((row) => row.id).filter((id) => id != null)
  try {
    await Promise.all(ids.map((id) => markAlertRead(id)))
    selectedAlerts.value.forEach((row) => {
      const index = alertList.value.findIndex(
        (item) =>
          item.elderName === row.elderName &&
          item.time === row.time &&
          item.alertType === row.alertType,
      )
      if (index > -1) {
        alertList.value[index].read = true
        if (alertList.value[index].id != null) readIds.add(alertList.value[index].id)
      }
    })
    ElMessage.success(`已标记 ${selectedAlerts.value.length} 条告警为已读`)
    newAlertCount.value = alertList.value.filter((item) => !item.read).length
    selectedAlerts.value = []
  } catch {
    // 失败提示由拦截器统一处理
  }
}

function handleBatchIgnore() {
  if (selectedAlerts.value.length === 0) {
    ElMessage.warning('请先选择告警')
    return
  }
  if (selectedAlerts.value.some((item) => item.status === 'resolved')) {
    ElMessage.warning('选中的告警中有已处理的项')
    return
  }

  ElMessageBox.confirm(`确定要批量忽略 ${selectedAlerts.value.length} 条告警吗？`, '批量忽略确认', {
    confirmButtonText: '确定忽略',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      const ids = selectedAlerts.value.map((row) => row.id).filter((id) => id != null)
      await batchIgnoreAlerts(ids)
      selectedAlerts.value.forEach((row) => {
        const index = alertList.value.findIndex(
          (item) =>
            item.elderName === row.elderName &&
            item.time === row.time &&
            item.alertType === row.alertType,
        )
        if (index > -1) {
          alertList.value[index].status = 'ignored'
          alertList.value[index].read = true
          if (alertList.value[index].id != null) readIds.add(alertList.value[index].id)
        }
      })
      ElMessage.success(`已忽略 ${selectedAlerts.value.length} 条告警`)
      sortByLevel()
      updateStats()
      newAlertCount.value = alertList.value.filter((item) => !item.read).length
      selectedAlerts.value = []
    })
    .catch(() => {})
}

// ===== 提交处理 =====
async function submitProcess() {
  if (!processForm.action) {
    ElMessage.warning('请选择处理措施')
    return
  }

  const row = processData.value
  if (!row) return
  try {
    await processAlert(row.id, {
      handler: '管理员',
      handleNote: `处理措施：${processForm.action}；备注：${processForm.note || '无'}`,
    })
    const index = alertList.value.findIndex(
      (item) =>
        item.elderName === row.elderName &&
        item.time === row.time &&
        item.alertType === row.alertType,
    )
    if (index > -1) {
      alertList.value[index].status = 'resolved'
      alertList.value[index].handler = '管理员'
      alertList.value[index].handleTime = new Date().toLocaleString('zh-CN')
      alertList.value[index].handleNote =
        `处理措施：${processForm.action}；备注：${processForm.note || '无'}`
      alertList.value[index].read = true
      if (alertList.value[index].id != null) readIds.add(alertList.value[index].id)
    }
    ElMessage.success('告警处理完成')
    processDialogVisible.value = false
    sortByLevel()
    updateStats()
    newAlertCount.value = alertList.value.filter((item) => !item.read).length
  } catch {
    // 失败提示由拦截器统一处理
  }
}

// ===== 筛选重置 =====
function resetFilter() {
  filterForm.elderName = ''
  filterForm.alertType = ''
  filterForm.level = ''
  filterForm.status = ''
  filterForm.dateRange = []
  currentPage.value = 1
}

// ===== 刷新数据 =====
function refreshData() {
  loadAlerts()
}

// ===== 声音控制 =====
function toggleSound() {
  soundEnabled.value = !soundEnabled.value
  ElMessage.success(soundEnabled.value ? '声音已开启' : '声音已关闭')
}

// ===== 自动刷新 =====
function startAutoRefresh() {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
  refreshTimer = setInterval(() => {
    refreshData()
  }, refreshInterval.value * 1000)
}

// ===== 监听刷新间隔变化 =====
watch(refreshInterval, () => {
  startAutoRefresh()
})

// ===== 生命周期 =====
onMounted(() => {
  initData()
  startAutoRefresh()
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})
</script>

<style scoped>
.alert-list-page {
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

.new-alert-tag {
  animation: blink 1s infinite;
}

@keyframes blink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
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
.stat-card.pending .stat-icon {
  background: #fef0f0;
  color: #f56c6c;
}
.stat-card.processing .stat-icon {
  background: #fdf6ec;
  color: #e6a23c;
}
.stat-card.today .stat-icon {
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

.desc-text {
  display: block;
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #666;
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
  gap: 8px 20px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  padding: 6px 0;
  border-bottom: 1px solid #f5f5f5;
}

.detail-item .detail-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 2px;
}

.detail-item .detail-value {
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

  .alert-list-page {
    padding: 12px;
  }

  .header-actions {
    flex-wrap: wrap;
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