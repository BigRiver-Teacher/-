<template>
  <div class="push-log-page">
    <!-- ===== 页面标题 ===== -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Share /></el-icon>
        家属推送日志
      </h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleBatchRetry">
          <el-icon><RefreshRight /></el-icon>
          批量重推
        </el-button>
        <el-button type="primary" plain @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="success" plain>
          <el-icon><Download /></el-icon>
          导出日志
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
          <div class="stat-label">总推送数</div>
        </div>
      </div>
      <div class="stat-card today">
        <div class="stat-icon">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.today }}</div>
          <div class="stat-label">今日推送</div>
        </div>
      </div>
      <div class="stat-card success">
        <div class="stat-icon">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.success }}</div>
          <div class="stat-label">推送成功</div>
          <div class="stat-rate">{{ stats.successRate }}%</div>
        </div>
      </div>
      <div class="stat-card failed">
        <div class="stat-icon">
          <el-icon><CircleClose /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.failed }}</div>
          <div class="stat-label">推送失败</div>
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
          <el-form-item label="推送渠道">
            <el-select
              v-model="filterForm.channel"
              placeholder="全部"
              clearable
              style="width: 130px"
            >
              <el-option
                v-for="item in channelOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="推送状态">
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
          <span>推送记录</span>
          <el-tag size="small" type="info">共 {{ total }} 条</el-tag>
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
        <el-table-column prop="elderName" label="老人姓名" width="90" align="center" />
        <el-table-column prop="familyPhone" label="家属电话" width="110" align="center" />
        <el-table-column prop="channel" label="推送渠道" width="100" align="center">
          <template #default="{ row }">
            <div class="channel-tag">
              <el-icon v-if="row.channel === 'APP推送'" color="#409eff"><Iphone /></el-icon>
              <el-icon v-else-if="row.channel === '微信推送'" color="#67c23a"
                ><ChatDotSquare
              /></el-icon>
              <el-icon v-else-if="row.channel === '短信推送'" color="#e6a23c"><Message /></el-icon>
              <el-icon v-else-if="row.channel === '电话语音'" color="#f56c6c"><Phone /></el-icon>
              <el-icon v-else-if="row.channel === '邮件推送'" color="#909399"><Message /></el-icon>
              <span>{{ row.channel }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="scene" label="推送场景" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getSceneType(row.scene)" size="small">
              {{ getSceneText(row.scene) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容摘要" min-width="160">
          <template #default="{ row }">
            <el-tooltip :content="row.content" placement="top">
              <span class="content-text">{{ row.content }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="推送时间" width="160" align="center" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row, $index }">
            <div class="action-buttons">
              <el-button size="small" type="primary" link @click="handleView(row)">
                <el-icon><View /></el-icon>
                详情
              </el-button>
              <el-button
                v-if="row.status === 'failed'"
                size="small"
                type="warning"
                link
                @click="handleRetry(row)"
              >
                <el-icon><RefreshRight /></el-icon>
                重推
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

    <!-- ===== 推送详情弹框 ===== -->
    <el-dialog v-model="detailVisible" title="推送详情" width="580px">
      <div v-if="detailData" class="detail-content">
        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">老人姓名</span>
            <span class="detail-value">{{ detailData.elderName }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">家属电话</span>
            <span class="detail-value">{{ detailData.familyPhone }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">推送渠道</span>
            <span class="detail-value">{{ detailData.channel }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">推送场景</span>
            <el-tag :type="getSceneType(detailData.scene)" size="small">
              {{ getSceneText(detailData.scene) }}
            </el-tag>
          </div>
          <div class="detail-item">
            <span class="detail-label">推送状态</span>
            <el-tag :type="getStatusType(detailData.status)" size="small">
              {{ getStatusText(detailData.status) }}
            </el-tag>
          </div>
          <div class="detail-item">
            <span class="detail-label">推送时间</span>
            <span class="detail-value">{{ detailData.time }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">推送耗时</span>
            <span class="detail-value">{{ detailData.duration || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">重试次数</span>
            <span class="detail-value"> {{ detailData.retryCount || 0 }} / 3 </span>
          </div>
          <div class="detail-item full-width">
            <span class="detail-label">推送内容</span>
            <div class="content-box">{{ detailData.fullContent || detailData.content }}</div>
          </div>
          <div v-if="detailData.response" class="detail-item full-width">
            <span class="detail-label">推送响应</span>
            <div class="response-box">{{ detailData.response }}</div>
          </div>
          <div
            v-if="detailData.retryHistory && detailData.retryHistory.length > 0"
            class="detail-item full-width"
          >
            <span class="detail-label">重试记录</span>
            <div class="retry-box">
              <div v-for="(item, idx) in detailData.retryHistory" :key="idx" class="retry-item">
                <span class="retry-time">{{ item.time }}</span>
                <span class="retry-label">第{{ item.attempt }}次尝试</span>
                <el-tag :type="item.status === 'success' ? 'success' : 'danger'" size="small">
                  {{ item.status === 'success' ? '成功' : '失败' }}
                </el-tag>
                <span v-if="item.error" class="retry-error">{{ item.error }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="detailData?.status === 'failed'"
          type="warning"
          @click="
            () => {
              detailVisible = false
              handleRetry(detailData)
            }
          "
        >
          <el-icon><RefreshRight /></el-icon>
          重新推送
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Share,
  Refresh,
  RefreshRight,
  View,
  Filter,
  List,
  Document,
  Clock,
  CircleCheck,
  CircleClose,
  Download,
  Search,
  Iphone,
  ChatDotSquare,
  Message,
  Phone,
} from '@element-plus/icons-vue'
import { getPushLogList, retryPushLog, batchRetryPushLogs } from '@/api/pushLog'

// ===== 统计数据 =====
const stats = reactive({
  total: 0,
  today: 0,
  success: 0,
  failed: 0,
  successRate: 0,
})

// ===== 渠道代码 → 显示名称映射（后端可能返回代码，页面需要中文名） =====
const channelNameMap: Record<string, string> = {
  app: 'APP推送',
  wechat: '微信推送',
  sms: '短信推送',
  phone: '电话语音',
  email: '邮件推送',
}

// ===== 数据列表 =====
const pushList = ref([])
const selectedLogs = ref([])

// ===== 筛选条件 =====
const filterForm = reactive({
  elderName: '',
  channel: '',
  status: '',
  dateRange: [],
})

const channelOptions = [
  { label: '全部', value: '' },
  { label: 'APP推送', value: 'APP推送' },
  { label: '微信推送', value: '微信推送' },
  { label: '短信推送', value: '短信推送' },
  { label: '电话语音', value: '电话语音' },
  { label: '邮件推送', value: '邮件推送' },
]

const statusOptions = [
  { label: '全部', value: '' },
  { label: '成功', value: 'success' },
  { label: '失败', value: 'failed' },
  { label: '发送中', value: 'sending' },
]

// ===== 分页 =====
const currentPage = ref(1)
const pageSize = ref(5)

// ===== 详情弹框 =====
const detailVisible = ref(false)
const detailData = ref(null)

// ===== 加载推送日志 =====
async function loadLogs() {
  try {
    const res = await getPushLogList({ page: 1, size: 1000 })
    const data = res.data.data
    const list = data?.list ?? []
    pushList.value = list.map((item) => ({
      id: item.id,
      elderName: item.elderName || '',
      familyPhone: item.familyPhone || '',
      channel: channelNameMap[item.channel] || item.channel || '',
      scene: item.scence || (item as any).scene || '',
      content: item.content || '',
      fullContent: item.fullContent || '',
      status: String(item.status || '').toLowerCase(),
      time: item.time ? String(item.time).replace('T', ' ') : item.time || '',
      duration: item.duration || '',
      retryCount: item.retryCount ?? 0,
      response: item.response || '',
      retryHistory: [],
    }))
    updateStats()
  } catch {
    // 请求失败时由响应拦截器统一提示
  }
}

// ===== 初始化数据 =====
function initData() {
  loadLogs()
}

// ===== 更新统计数据 =====
function updateStats() {
  const list = pushList.value
  stats.total = list.length
  const today = new Date().toISOString().split('T')[0]
  stats.today = list.filter((item) => (item.time || '').startsWith(today)).length
  stats.success = list.filter((item) => item.status === 'success').length
  stats.failed = list.filter((item) => item.status === 'failed').length
  stats.successRate = stats.total > 0 ? Math.round((stats.success / stats.total) * 100) : 0
}

// ===== 筛选数据 =====
const filteredList = computed(() => {
  let list = [...pushList.value]

  if (filterForm.elderName) {
    list = list.filter((item) => item.elderName.includes(filterForm.elderName))
  }
  if (filterForm.channel) {
    list = list.filter((item) => item.channel === filterForm.channel)
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

  // 按时间倒序排列
  return list.sort((a, b) => new Date(b.time) - new Date(a.time))
})

const total = computed(() => filteredList.value.length)

const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredList.value.slice(start, end)
})

// ===== 工具函数 =====
function getStatusType(status) {
  const map = {
    success: 'success',
    failed: 'danger',
    sending: 'warning',
  }
  return map[status] || 'info'
}

function getStatusText(status) {
  const map = {
    success: '成功',
    failed: '失败',
    sending: '发送中',
  }
  return map[status] || status
}

const SCENE_TEXT = {
  FALL_ALERT: '跌倒告警',
  DEVICE_OFFLINE: '设备离线',
  DAILY_HEALTH: '健康日报',
  HEALTH_ALERT: '健康提醒',
  SOS: 'SOS呼叫',
}

function getSceneText(scene) {
  return SCENE_TEXT[scene] || scene || '-'
}

function getSceneType(scene) {
  const map = {
    FALL_ALERT: 'danger',
    SOS: 'danger',
    HEALTH_ALERT: 'warning',
    DEVICE_OFFLINE: 'info',
    DAILY_HEALTH: 'success',
  }
  return map[scene] || 'info'
}

// ===== 操作函数 =====
function handleView(row) {
  detailData.value = row
  detailVisible.value = true
}

function handleRetry(row) {
  // 检查重试次数
  if (row.retryCount >= 3) {
    ElMessage.warning('该推送已超过最大重试次数（3次）')
    return
  }

  ElMessageBox.confirm(
    `确定要重新推送该消息吗？（当前已重试 ${row.retryCount} 次）`,
    '重新推送确认',
    {
      confirmButtonText: '确定重推',
      cancelButtonText: '取消',
      type: 'info',
    },
  )
    .then(async () => {
      try {
        await retryPushLog(row.id)
        ElMessage.success('重新推送成功')
        await loadLogs()
      } catch {
        // 失败提示由拦截器统一处理
      }
    })
    .catch(() => {})
}

function handleBatchRetry() {
  if (selectedLogs.value.length === 0) {
    ElMessage.warning('请先选择推送记录')
    return
  }

  const failedLogs = selectedLogs.value.filter((item) => item.status === 'failed')
  if (failedLogs.length === 0) {
    ElMessage.warning('选中的记录中没有失败的可重推')
    return
  }

  const canRetry = failedLogs.filter((item) => item.retryCount < 3)
  if (canRetry.length === 0) {
    ElMessage.warning('选中的失败记录均已超过最大重试次数（3次）')
    return
  }

  ElMessageBox.confirm(`确定要批量重推 ${canRetry.length} 条推送吗？`, '批量重推确认', {
    confirmButtonText: '确定重推',
    cancelButtonText: '取消',
    type: 'info',
  })
    .then(async () => {
      try {
        const ids = canRetry.map((item) => item.id).filter((id) => id != null)
        await batchRetryPushLogs(ids)
        ElMessage.success(`已发起 ${ids.length} 条推送的批量重推`)
        selectedLogs.value = []
        await loadLogs()
      } catch {
        // 失败提示由拦截器统一处理
      }
    })
    .catch(() => {})
}

function handleSelectionChange(selection) {
  selectedLogs.value = selection
}

function resetFilter() {
  filterForm.elderName = ''
  filterForm.channel = ''
  filterForm.status = ''
  filterForm.dateRange = []
  currentPage.value = 1
}

function refreshData() {
  loadLogs()
}

// ===== 生命周期 =====
onMounted(() => {
  initData()
})
</script>

<style scoped>
.push-log-page {
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

.stat-rate {
  font-size: 13px;
  color: #67c23a;
  font-weight: 600;
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

.content-text {
  display: block;
  max-width: 160px;
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

.channel-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  justify-content: center;
}

.channel-tag .el-icon {
  font-size: 16px;
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
  gap: 10px 20px;
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
}

.content-box {
  font-size: 14px;
  color: #333;
  line-height: 1.8;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 3px solid #409eff;
  word-break: break-all;
}

.response-box {
  font-size: 13px;
  color: #666;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 6px;
  font-family: monospace;
  word-break: break-all;
}

.retry-box {
  padding: 4px 0;
}

.retry-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 0;
  border-bottom: 1px solid #f5f5f5;
  font-size: 13px;
}

.retry-item:last-child {
  border-bottom: none;
}

.retry-time {
  color: #999;
  font-size: 12px;
}

.retry-label {
  color: #666;
}

.retry-error {
  color: #f56c6c;
  font-size: 12px;
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

  .push-log-page {
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