<template>
  <div class="log-page">
    <!-- ===== 页面标题 ===== -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Document /></el-icon>
        操作日志
      </h2>
      <div class="header-actions">
        <el-button type="success" plain @click="handleExport">
          <el-icon><Download /></el-icon>
          导出日志
        </el-button>
        <el-button type="danger" plain @click="handleClear">
          <el-icon><Delete /></el-icon>
          清理日志
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
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.total }}</div>
          <div class="stat-label">总日志数</div>
        </div>
      </div>
      <div class="stat-card today">
        <div class="stat-icon">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.today }}</div>
          <div class="stat-label">今日新增</div>
        </div>
      </div>
      <div class="stat-card success">
        <div class="stat-icon">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.success }}</div>
          <div class="stat-label">操作成功</div>
        </div>
      </div>
      <div class="stat-card failed">
        <div class="stat-icon">
          <el-icon><CircleClose /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.failed }}</div>
          <div class="stat-label">操作失败</div>
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
          <el-form-item label="操作用户">
            <el-input
              v-model="filterForm.user"
              placeholder="请输入用户名"
              clearable
              style="width: 150px"
              prefix-icon="Search"
            />
          </el-form-item>
          <el-form-item label="操作类型">
            <el-select v-model="filterForm.type" placeholder="全部" clearable style="width: 130px">
              <el-option
                v-for="item in typeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="操作模块">
            <el-select
              v-model="filterForm.module"
              placeholder="全部"
              clearable
              style="width: 130px"
            >
              <el-option
                v-for="item in moduleOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="操作状态">
            <el-select
              v-model="filterForm.status"
              placeholder="全部"
              clearable
              style="width: 120px"
            >
              <el-option label="全部" value="" />
              <el-option label="成功" value="success" />
              <el-option label="失败" value="failed" />
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
          <span>日志列表</span>
          <el-tag size="small" type="info">共 {{ total }} 条</el-tag>
        </div>
        <div class="table-actions">
          <el-button size="small" type="primary" plain @click="handleRefresh">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>

      <el-table :data="paginatedList" stripe style="width: 100%" row-key="index" border>
        <el-table-column prop="time" label="操作时间" width="160" align="center" />
        <el-table-column prop="user" label="操作用户" width="100" align="center" />
        <el-table-column prop="type" label="操作类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.type)" size="small">
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="操作模块" width="120" align="center" />
        <el-table-column prop="content" label="操作内容" min-width="180">
          <template #default="{ row }">
            <el-tooltip :content="row.content" placement="top">
              <span class="content-text">{{ row.content }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP地址" width="140" align="center" />
        <el-table-column prop="status" label="操作状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'success' ? 'success' : 'danger'" size="small">
              {{ row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleView(row)">
              <el-icon><View /></el-icon>
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          background
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
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
    <el-dialog v-model="detailVisible" title="日志详情" width="540px">
      <div v-if="detailData" class="detail-content">
        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">操作时间</span>
            <span class="detail-value">{{ detailData.time }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">操作用户</span>
            <span class="detail-value">{{ detailData.user }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">操作类型</span>
            <el-tag :type="getTypeColor(detailData.type)" size="small">
              {{ detailData.type }}
            </el-tag>
          </div>
          <div class="detail-item">
            <span class="detail-label">操作模块</span>
            <span class="detail-value">{{ detailData.module }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">IP地址</span>
            <span class="detail-value">{{ detailData.ip }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">操作状态</span>
            <el-tag :type="detailData.status === 'success' ? 'success' : 'danger'" size="small">
              {{ detailData.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </div>
          <div class="detail-item full-width">
            <span class="detail-label">操作内容</span>
            <span class="detail-value">{{ detailData.content }}</span>
          </div>
          <div
            v-if="detailData.status === 'failed' && detailData.error"
            class="detail-item full-width"
          >
            <span class="detail-label">错误信息</span>
            <span class="detail-value" style="color: #f56c6c">
              {{ detailData.error }}
            </span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Document,
  Clock,
  CircleCheck,
  CircleClose,
  Refresh,
  Download,
  Delete,
  Filter,
  Search,
  List,
  View,
} from '@element-plus/icons-vue'
import {
  getOperationLogList,
  exportOperationLog,
  cleanOperationLog,
  type OperationLogVO,
} from '@/api/operationLog'

// ===== 统计数据 =====
const stats = reactive({
  total: 0,
  today: 0,
  success: 0,
  failed: 0,
})

// ===== 数据列表 =====
const logList = ref<OperationLogVO[]>([])

// ===== 选项数据 =====
const typeOptions = [
  { label: '全部', value: '' },
  { label: '登录', value: '登录' },
  { label: '登出', value: '登出' },
  { label: '新增', value: '新增' },
  { label: '编辑', value: '编辑' },
  { label: '删除', value: '删除' },
  { label: '审核', value: '审核' },
  { label: '处理', value: '处理' },
  { label: '导出', value: '导出' },
]

const moduleOptions = [
  { label: '全部', value: '' },
  { label: '系统管理', value: '系统管理' },
  { label: '账号权限', value: '账号权限' },
  { label: '跌倒预警', value: '跌倒预警' },
  { label: '专家康养咨询', value: '专家康养咨询' },
  { label: '模型参数设置', value: '模型参数设置' },
  { label: '设备管理', value: '设备管理' },
  { label: '告警信息中心', value: '告警信息中心' },
  { label: '干预方案管理', value: '干预方案管理' },
  { label: '居家空间档案', value: '居家空间档案' },
  { label: '日常康养日程', value: '日常康养日程' },
]

// ===== 筛选条件 =====
const filterForm = reactive({
  user: '',
  type: '',
  module: '',
  status: '',
  dateRange: [],
})

// ===== 分页 =====
const currentPage = ref(1)
const pageSize = ref(10)

// ===== 详情弹框 =====
const detailVisible = ref(false)
const detailData = ref(null)

// ===== 初始化数据（从后端获取） =====
function formatTime(t: string) {
  return t ? t.replace('T', ' ') : t
}

async function fetchData() {
  try {
    const res = await getOperationLogList({ page: 1, size: 200 })
    const data = res.data.data
    logList.value = (data.list ?? []).map((item: OperationLogVO) => ({
      id: item.id,
      time: formatTime(item.operateTime),
      user: item.operator,
      type: item.operateType,
      module: item.module,
      content: item.content,
      ip: item.ip,
      status: item.status,
      error: item.errorMsg ?? '',
    }))
    updateStats()
  } catch {
    ElMessage.error('获取操作日志失败')
  }
}

// ===== 更新统计数据 =====
function updateStats() {
  const list = logList.value
  stats.total = list.length
  const today = new Date().toISOString().split('T')[0]
  stats.today = list.filter((item) => item.time.startsWith(today)).length
  stats.success = list.filter((item) => item.status === 'success').length
  stats.failed = list.filter((item) => item.status === 'failed').length
}

// ===== 工具函数 =====
function getTypeColor(type) {
  const map = {
    登录: 'success',
    登出: 'info',
    新增: 'success',
    编辑: 'warning',
    删除: 'danger',
    审核: 'primary',
    处理: 'success',
    导出: 'info',
  }
  return map[type] || 'info'
}

// ===== 筛选数据 =====
const filteredList = computed(() => {
  let list = [...logList.value]

  if (filterForm.user) {
    list = list.filter((item) => item.user.includes(filterForm.user))
  }
  if (filterForm.type) {
    list = list.filter((item) => item.type === filterForm.type)
  }
  if (filterForm.module) {
    list = list.filter((item) => item.module === filterForm.module)
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

  // 按时间倒序
  return list.sort((a, b) => new Date(b.time) - new Date(a.time))
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

// ===== 下载工具函数 =====
function getDownloadFileName(contentDisposition: string | undefined, fallback: string) {
  if (!contentDisposition) return fallback
  const star = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i)
  if (star) return decodeURIComponent(star[1].trim())
  const plain = contentDisposition.match(/filename="?([^";]+)"?/i)
  if (plain) return plain[1].trim()
  return fallback
}

function downloadBlob(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

async function handleExport() {
  try {
    const res = await exportOperationLog()
    const disposition = res.headers?.['content-disposition'] as string | undefined
    const filename = getDownloadFileName(disposition, `operation_log_${Date.now()}.xlsx`)
    downloadBlob(res.data, filename)
    ElMessage.success('日志导出成功')
  } catch {
    ElMessage.error('日志导出失败')
  }
}

async function handleClear() {
  try {
    await ElMessageBox.confirm(
      '确定要清理日志吗？\n将保留最近3个月的日志，更早的日志将被删除。',
      '清理日志确认',
      {
        confirmButtonText: '确定清理',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
    const d = new Date()
    d.setMonth(d.getMonth() - 3)
    const pad = (n: number) => String(n).padStart(2, '0')
    const beforeDate = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
    await cleanOperationLog(beforeDate)
    ElMessage.success('日志清理成功')
    await fetchData()
  } catch { /* 用户取消 */ }
}

function resetFilter() {
  filterForm.user = ''
  filterForm.type = ''
  filterForm.module = ''
  filterForm.status = ''
  filterForm.dateRange = []
  currentPage.value = 1
}

async function refreshData() {
  await fetchData()
  ElMessage.success('数据已刷新')
}

function handleRefresh() {
  refreshData()
}

// ===== 生命周期 =====
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.log-page {
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
  gap: 8px;
  align-items: center;
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
.stat-card.today .stat-icon {
  background: #f0f9eb;
  color: #67c23a;
}
.stat-card.success .stat-icon {
  background: #f0f9eb;
  color: #67c23a;
}
.stat-card.failed .stat-icon {
  background: #fef0f0;
  color: #f56c6c;
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

.table-actions {
  display: flex;
  gap: 8px;
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

.content-text {
  display: block;
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #666;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  margin-top: 16px;
}

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

.detail-item.full-width {
  grid-column: 1 / -1;
}

.detail-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 2px;
}

.detail-value {
  font-size: 14px;
  color: #333;
  word-break: break-all;
}

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

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .log-page {
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
}
</style>