<template>
  <div class="backup-page">
    <!-- ===== 页面标题 ===== -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><FolderOpened /></el-icon>
        数据备份
      </h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleBackup">
          <el-icon><Plus /></el-icon>
          立即备份
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
          <el-icon><FolderOpened /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.total }}</div>
          <div class="stat-label">备份总数</div>
        </div>
      </div>
      <div class="stat-card success">
        <div class="stat-icon">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.success }}</div>
          <div class="stat-label">备份成功</div>
        </div>
      </div>
      <div class="stat-card failed">
        <div class="stat-icon">
          <el-icon><CircleClose /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.failed }}</div>
          <div class="stat-label">备份失败</div>
        </div>
      </div>
      <div class="stat-card size">
        <div class="stat-icon">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.totalSize }}</div>
          <div class="stat-label">总大小</div>
        </div>
      </div>
    </div>

    <!-- ===== 定时备份设置 ===== -->
    <div class="schedule-section">
      <div class="schedule-header">
        <div class="schedule-title">
          <el-icon><Timer /></el-icon>
          <span>定时备份设置</span>
        </div>
      </div>
      <div class="schedule-body">
        <el-form label-width="100px" inline>
          <el-form-item label="备份频率">
            <el-radio-group v-model="scheduleConfig.frequency">
              <el-radio label="daily">每天</el-radio>
              <el-radio label="weekly">每周</el-radio>
              <el-radio label="monthly">每月</el-radio>
              <el-radio label="disabled">关闭</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="scheduleConfig.frequency !== 'disabled'" label="备份时间">
            <el-time-picker
              v-model="scheduleConfig.time"
              format="HH:mm"
              value-format="HH:mm"
              placeholder="选择时间"
              style="width: 120px"
            />
          </el-form-item>
          <el-form-item label="保留策略">
            <el-select v-model="scheduleConfig.retention" style="width: 140px">
              <el-option label="保留最近7天" value="7" />
              <el-option label="保留最近30天" value="30" />
              <el-option label="保留最近90天" value="90" />
              <el-option label="保留最近180天" value="180" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveSchedule">保存设置</el-button>
          </el-form-item>
        </el-form>
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
          <el-form-item label="备份状态">
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
          <el-form-item label="备份类型">
            <el-select v-model="filterForm.type" placeholder="全部" clearable style="width: 120px">
              <el-option label="全部" value="" />
              <el-option label="全量备份" value="全量备份" />
              <el-option label="增量备份" value="增量备份" />
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
          <span>备份列表</span>
          <el-tag size="small" type="info">共 {{ total }} 条</el-tag>
        </div>
      </div>

      <el-table :data="paginatedList" stripe style="width: 100%" row-key="index" border>
        <el-table-column prop="fileName" label="备份文件名" min-width="200" />
        <el-table-column prop="time" label="备份时间" width="160" align="center" />
        <el-table-column prop="type" label="备份类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === '全量备份' ? 'danger' : 'info'" size="small">
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="size" label="文件大小" width="120" align="center" />
        <el-table-column prop="status" label="备份状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'success' ? 'success' : 'danger'" size="small">
              {{ row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button
                v-if="row.status === 'success'"
                size="small"
                type="primary"
                link
                @click="handleDownload(row)"
              >
                <el-icon><Download /></el-icon>
                下载
              </el-button>
              <el-button
                v-if="row.status === 'success'"
                size="small"
                type="warning"
                link
                @click="handleRestore(row)"
              >
                <el-icon><RefreshRight /></el-icon>
                恢复
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  FolderOpened,
  Plus,
  Refresh,
  CircleCheck,
  CircleClose,
  Document,
  Timer,
  Filter,
  Search,
  List,
  Download,
  RefreshRight,
  Delete,
} from '@element-plus/icons-vue'
import {
  getBackupList,
  createBackup,
  downloadBackup,
  restoreBackup,
  deleteBackup,
  type DataBackupVO,
} from '@/api/backup'

// ===== 统计数据 =====
const stats = reactive({
  total: 0,
  success: 0,
  failed: 0,
  totalSize: '0 MB',
})

// ===== 定时备份设置 =====
const scheduleConfig = reactive({
  frequency: 'daily',
  time: '02:00',
  retention: '30',
})

// ===== 数据列表 =====
const backupList = ref<DataBackupVO[]>([])

// ===== 筛选条件 =====
const filterForm = reactive({
  status: '',
  type: '',
  dateRange: [],
})

// ===== 分页 =====
const currentPage = ref(1)
const pageSize = ref(5)

// ===== 初始化数据（从后端获取） =====
function formatTime(t: string) {
  return t ? t.replace('T', ' ') : t
}

function formatFileSize(bytes: number) {
  if (bytes == null || isNaN(bytes)) return '0 MB'
  const mb = bytes / 1024 / 1024
  if (mb > 1024) {
    return `${(mb / 1024).toFixed(2)} GB`
  }
  return `${mb.toFixed(1)} MB`
}

async function fetchData() {
  try {
    const res = await getBackupList({ page: 1, size: 200 })
    const data = res.data.data
    backupList.value = (data.list ?? []).map((item: DataBackupVO) => ({
      id: item.id,
      fileName: item.fileName,
      time: formatTime(item.backupTime),
      type: item.type === 'AUTO' ? '全量备份' : '增量备份',
      size: formatFileSize(item.fileSize),
      status: item.status,
      filePath: item.filePath,
    }))
    updateStats()
  } catch {
    ElMessage.error('获取备份列表失败')
  }
}

// ===== 更新统计数据 =====
function updateStats() {
  const list = backupList.value
  stats.total = list.length
  stats.success = list.filter((item) => item.status === 'success').length
  stats.failed = list.filter((item) => item.status === 'failed').length

  // 计算总大小
  let totalMB = 0
  list.forEach((item) => {
    const num = parseFloat(item.size)
    if (!isNaN(num)) {
      totalMB += num
    }
  })
  if (totalMB > 1024) {
    stats.totalSize = `${(totalMB / 1024).toFixed(2)} GB`
  } else {
    stats.totalSize = `${totalMB.toFixed(1)} MB`
  }
}

// ===== 筛选数据 =====
const filteredList = computed(() => {
  let list = [...backupList.value]

  if (filterForm.status) {
    list = list.filter((item) => item.status === filterForm.status)
  }
  if (filterForm.type) {
    list = list.filter((item) => item.type === filterForm.type)
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
async function handleBackup() {
  try {
    await ElMessageBox.confirm(
      '确定要立即执行数据备份吗？\n备份过程可能需要几分钟，请勿关闭页面。',
      '立即备份确认',
      {
        confirmButtonText: '确定备份',
        cancelButtonText: '取消',
        type: 'info',
      },
    )
    await createBackup('MANUAL')
    ElMessage.success('备份任务已启动，请稍后查看结果')
    await fetchData()
  } catch { /* 用户取消 */ }
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

async function handleDownload(row: DataBackupVO) {
  try {
    const res = await downloadBackup(row.id)
    const disposition = res.headers?.['content-disposition'] as string | undefined
    const filename = getDownloadFileName(disposition, row.fileName)
    downloadBlob(res.data, filename)
    ElMessage.success(`正在下载 ${filename}`)
  } catch {
    ElMessage.error('下载失败')
  }
}

async function handleRestore(row: DataBackupVO) {
  try {
    await ElMessageBox.confirm(
      `确定要从备份 "${row.fileName}" 恢复数据吗？\n此操作将覆盖当前数据，请谨慎操作！`,
      '数据恢复确认',
      {
        confirmButtonText: '确定恢复',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
    await restoreBackup(row.id)
    ElMessage.success('数据恢复任务已启动')
  } catch { /* 用户取消 */ }
}

async function handleDelete(row: DataBackupVO) {
  try {
    await ElMessageBox.confirm(`确定要删除备份 "${row.fileName}" 吗？`, '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteBackup(row.id)
    ElMessage.success('删除成功')
    await fetchData()
  } catch { /* 用户取消 */ }
}

function saveSchedule() {
  ElMessage.success('定时备份设置已保存')
}

function resetFilter() {
  filterForm.status = ''
  filterForm.type = ''
  filterForm.dateRange = []
  currentPage.value = 1
}

async function refreshData() {
  await fetchData()
  ElMessage.success('数据已刷新')
}

// ===== 生命周期 =====
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.backup-page {
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
.stat-card.success .stat-icon {
  background: #f0f9eb;
  color: #67c23a;
}
.stat-card.failed .stat-icon {
  background: #fef0f0;
  color: #f56c6c;
}
.stat-card.size .stat-icon {
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

.schedule-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.schedule-header {
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}

.schedule-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.schedule-title .el-icon {
  font-size: 18px;
  color: #409eff;
}

.schedule-body :deep(.el-form-item) {
  margin-bottom: 4px;
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

  .backup-page {
    padding: 12px;
  }

  .header-actions {
    flex-wrap: wrap;
  }

  .schedule-body :deep(.el-form-item) {
    display: block;
    width: 100%;
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