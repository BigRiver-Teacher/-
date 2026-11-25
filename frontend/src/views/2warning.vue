<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Warning,
  Bell,
  Search,
  Refresh,
  View,
  Edit,
  Delete,
  Timer,
  Location,
  User,
  Document,
  WarningFilled,
  List,
  Filter,
  ArrowLeft,
  ArrowRight,
  Clock,
  CircleCheck,
  CircleClose,
  Plus,
  VideoCamera,
} from '@element-plus/icons-vue'
import { useAlertStore } from '@/stores/alertStore'
import FallDetectDialog from '@/views/ai/FallDetectDialog.vue'

const alertStore = useAlertStore()

// ===== AI 视频检测弹窗 =====
const detectVisible = ref(false)

// ===== 统计数据（直接从 store 获取） =====
const stats = computed(() => ({
  total: alertStore.total,
  pending: alertStore.pendingCount,
  processing: alertStore.processingCount,
  resolved: alertStore.resolvedCount,
}))

// ===== 排序函数 =====
function sortWarnings(list: any[]) {
  const priorityOrder: Record<string, number> = { high: 0, medium: 1, low: 2 }
  const statusOrder: Record<string, number> = { pending: 0, processing: 1, resolved: 2, ignored: 3 }

  return [...list].sort((a, b) => {
    if (statusOrder[a.status] !== statusOrder[b.status]) {
      return statusOrder[a.status] - statusOrder[b.status]
    }
    return priorityOrder[a.priority] - priorityOrder[b.priority]
  })
}

// ===== 待处理预警 =====
const pendingWarnings = computed(() => {
  return sortWarnings(alertStore.alertList.filter((item) => item.status === 'pending'))
})

// ===== 轮播图相关 =====
const carouselItems = computed(() => {
  const items: any[][] = []
  const list = pendingWarnings.value
  for (let i = 0; i < list.length; i += 3) {
    items.push(list.slice(i, i + 3))
  }
  return items
})

const currentSlide = ref(0)
const totalSlides = computed(() => carouselItems.value.length)

function prevSlide() {
  if (currentSlide.value > 0) {
    currentSlide.value--
  } else {
    currentSlide.value = totalSlides.value - 1
  }
}

function nextSlide() {
  if (currentSlide.value < totalSlides.value - 1) {
    currentSlide.value++
  } else {
    currentSlide.value = 0
  }
}

let autoPlayTimer: ReturnType<typeof setInterval> | null = null

function startAutoPlay() {
  if (autoPlayTimer) clearInterval(autoPlayTimer)
  if (totalSlides.value > 1) {
    autoPlayTimer = setInterval(() => {
      nextSlide()
    }, 5000)
  }
}

function stopAutoPlay() {
  if (autoPlayTimer) {
    clearInterval(autoPlayTimer)
    autoPlayTimer = null
  }
}

// 当待处理预警变化时重启轮播
watch(totalSlides, () => {
  if (totalSlides.value > 1) startAutoPlay()
  else stopAutoPlay()
})

// ===== 筛选条件 =====
const filterForm = reactive({
  warningType: '',
  priority: '',
  status: '',
  startDate: '',
  endDate: '',
  keyword: '',
})

const warningTypes = ['异常数据预警', '生长状态异常', '天气预警']
const priorityOptions = [
  { label: '高优先级', value: 'high' },
  { label: '中优先级', value: 'medium' },
  { label: '低优先级', value: 'low' },
]
const statusOptions = [
  { label: '待处理', value: 'pending' },
  { label: '处理中', value: 'processing' },
  { label: '已处理', value: 'resolved' },
]

// ===== 分页 =====
const currentPage = ref(1)
const pageSize = ref(5)

// ===== 处理弹框 =====
const dialogVisible = ref(false)
const dialogTitle = ref('处理预警')
const currentWarning = ref<any>(null)
const handleForm = reactive({
  process: '',
  result: '',
})

// ===== 查看详情弹框 =====
const detailVisible = ref(false)
const detailData = ref<any>(null)

// ===== 筛选后的数据 =====
const filteredList = computed(() => {
  let list = [...alertStore.alertList]

  if (filterForm.warningType) {
    list = list.filter((item) => item.warningType === filterForm.warningType)
  }
  if (filterForm.priority) {
    list = list.filter((item) => item.priority === filterForm.priority)
  }
  if (filterForm.status) {
    list = list.filter((item) => item.status === filterForm.status)
  }
  if (filterForm.keyword) {
    const keyword = filterForm.keyword.toLowerCase()
    list = list.filter(
      (item) =>
        item.elderlyName?.includes(keyword) ||
        item.location?.includes(keyword) ||
        item.description?.includes(keyword),
    )
  }
  if (filterForm.startDate && filterForm.endDate) {
    list = list.filter((item) => {
      const date = item.time?.split(' ')[0] || ''
      return date >= filterForm.startDate && date <= filterForm.endDate
    })
  }

  return sortWarnings(list)
})

const total = computed(() => filteredList.value.length)

const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredList.value.slice(start, end)
})

// ===== 状态/优先级标签工具函数 =====
const getStatusType = (status: string) => {
  const map: Record<string, string> = { pending: 'danger', processing: 'warning', resolved: 'success', ignored: 'info' }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = { pending: '待处理', processing: '处理中', resolved: '已处理', ignored: '已忽略' }
  return map[status] || status
}

const getPriorityText = (priority: string) => {
  const map: Record<string, string> = { high: '高优先级', medium: '中优先级', low: '低优先级' }
  return map[priority] || priority
}

const getPriorityType = (priority: string) => {
  const map: Record<string, string> = { high: 'danger', medium: 'warning', low: 'info' }
  return map[priority] || 'info'
}

// ===== 操作函数 =====
function handleView(row: any) {
  detailData.value = row
  detailVisible.value = true
}

function handleProcess(row: any) {
  currentWarning.value = row
  dialogTitle.value = '处理预警'
  handleForm.handler = ''
  handleForm.handleNote = ''
  dialogVisible.value = true
}

function handleDelete(row: any) {
  ElMessageBox.confirm(`确定要忽略 "${row.elderlyName}" 的预警记录吗？`, '忽略确认', {
    confirmButtonText: '确定忽略',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      await alertStore.ignoreAlertById(row.id)
      ElMessage.success('已忽略该预警')
    })
    .catch(() => {})
}

function handleExport(_row: any) {
  ElMessage.success('导出成功')
}

// ===== 提交处理 =====
async function submitHandle() {
  if (!handleForm.handler) {
    ElMessage.warning('请输入处理过程')
    return
  }
  if (!handleForm.handleNote) {
    ElMessage.warning('请输入处理结果')
    return
  }

  const row = currentWarning.value
  try {
    await alertStore.handleAlert(row.id, {
      handler: handleForm.handler,
      handleNote: handleForm.handleNote,
    })
    ElMessage.success('处理成功')
    dialogVisible.value = false
    if (currentPage.value > Math.ceil(total.value / pageSize.value)) {
      currentPage.value = 1
    }
  } catch {
    ElMessage.error('处理失败，请稍后重试')
  }
}

function resetFilter() {
  filterForm.warningType = ''
  filterForm.priority = ''
  filterForm.status = ''
  filterForm.startDate = ''
  filterForm.endDate = ''
  filterForm.keyword = ''
  currentPage.value = 1
}

async function refreshData() {
  await alertStore.fetchAlerts()
  ElMessage.success('数据已刷新')
}

function handleProcessAndClose(row: any) {
  detailVisible.value = false
  setTimeout(() => {
    handleProcess(row)
  }, 300)
}

// ===== 添加新预警（本地操作，后端目前无此接口） =====
function addWarning() {
  ElMessage.info('请通过设备监控自动生成预警，或联系开发人员')
}

// ===== 生命周期 =====
onMounted(async () => {
  await alertStore.fetchAlerts()
  if (totalSlides.value > 1) startAutoPlay()
})

onUnmounted(() => {
  stopAutoPlay()
})
</script>

<template>
  <div class="warning-page">
    <!-- ===== 页面标题 ===== -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><WarningFilled /></el-icon>
        跌倒预警
      </h2>
      <div class="header-actions">
        <el-button type="warning" size="default" @click="detectVisible = true">
          <el-icon><VideoCamera /></el-icon>
          上传视频检测
        </el-button>
        <el-button type="success" size="default" @click="addWarning">
          <el-icon><Plus /></el-icon>
          添加预警
        </el-button>
        <el-button type="primary" size="default" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- ===== 未处理预警轮播图 ===== -->
    <div v-if="pendingWarnings.length > 0" class="carousel-wrapper">
      <div class="carousel-header">
        <div class="carousel-title">
          <span class="title-icon">⚠️</span>
          <span class="title-text">未处理预警</span>
          <el-tag type="danger" size="large" class="pending-count">
            待处理 {{ stats.pending }}
          </el-tag>
        </div>
        <div v-if="totalSlides > 1" class="carousel-controls">
          <el-button text @click="prevSlide" @mouseenter="stopAutoPlay" @mouseleave="startAutoPlay">
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <span class="slide-indicator">{{ currentSlide + 1 }} / {{ totalSlides }}</span>
          <el-button text @click="nextSlide" @mouseenter="stopAutoPlay" @mouseleave="startAutoPlay">
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>

      <div class="carousel-container" @mouseenter="stopAutoPlay" @mouseleave="startAutoPlay">
        <div class="carousel-track" :style="{ transform: `translateX(-${currentSlide * 100}%)` }">
          <div
            v-for="(group, groupIndex) in carouselItems"
            :key="groupIndex"
            class="carousel-slide"
          >
            <div v-for="item in group" :key="item.id" class="warning-card" :class="item.priority">
              <div class="card-header">
                <div class="card-title">
                  <span class="card-icon" :class="item.priority">
                    <el-icon><Warning /></el-icon>
                  </span>
                  <span class="card-name">{{ item.elderlyName }}</span>
                  <el-tag :type="getPriorityType(item.priority)" size="small">
                    {{ getPriorityText(item.priority) }}
                  </el-tag>
                </div>
                <span class="card-time">
                  <el-icon><Clock /></el-icon>
                  {{ item.time }}
                </span>
              </div>
              <div class="card-body">
                <div class="card-field">
                  <span class="field-label">异常信息：</span>
                  <span class="field-value">{{ item.description }}</span>
                </div>
                <div class="card-field">
                  <span class="field-label">建议：</span>
                  <span class="field-value highlight">{{ item.suggestedAction }}</span>
                </div>
              </div>
              <div class="card-footer">
                <el-button type="primary" size="small" @click="handleProcess(item)">
                  <el-icon><Edit /></el-icon>
                  处理
                </el-button>
                <el-button size="small" @click="handleView(item)">
                  <el-icon><View /></el-icon>
                  查看详情
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 指示点 -->
      <div v-if="totalSlides > 1" class="carousel-dots">
        <span
          v-for="(_, index) in totalSlides"
          :key="index"
          class="dot"
          :class="{ active: currentSlide === index }"
          @click="currentSlide = index"
        />
      </div>
    </div>

    <!-- ===== 无待处理预警 ===== -->
    <div v-else class="empty-carousel">
      <div class="empty-content">
        <el-icon :size="48" color="#67c23a"><CircleCheck /></el-icon>
        <p>暂无待处理预警，继续保持！</p>
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
          <el-button type="primary" @click="resetFilter">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </div>
      </div>
      <div class="filter-body">
        <el-form :model="filterForm" label-width="80px" inline>
          <el-form-item label="预警类型">
            <el-select v-model="filterForm.warningType" placeholder="全部类型" clearable>
              <el-option v-for="item in warningTypes" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="优先级">
            <el-select v-model="filterForm.priority" placeholder="全部优先级" clearable>
              <el-option
                v-for="item in priorityOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="filterForm.status" placeholder="全部状态" clearable>
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="开始日期">
            <el-date-picker
              v-model="filterForm.startDate"
              type="date"
              placeholder="选择开始日期"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="结束日期">
            <el-date-picker
              v-model="filterForm.endDate"
              type="date"
              placeholder="选择结束日期"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="关键词">
            <el-input
              v-model="filterForm.keyword"
              placeholder="搜索老人姓名/位置"
              prefix-icon="Search"
              clearable
              style="width: 200px"
            />
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- ===== 预警列表 ===== -->
    <div class="table-section">
      <div class="table-header">
        <div class="table-title">
          <el-icon><List /></el-icon>
          <span>历史预警</span>
          <el-tag size="small" type="info">共 {{ total }} 条</el-tag>
        </div>
      </div>

      <el-table :data="paginatedList" stripe style="width: 100%" row-key="id">
        <el-table-column prop="id" label="预警ID" width="80" align="center">
          <template #default="{ row }">
            <span style="font-size: 12px; color: #999">#{{ String(row.id).slice(-6) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="elderlyName" label="老人姓名" width="100">
          <template #default="{ row }">
            <div class="elderly-info">
              <span class="name">{{ row.elderlyName }}</span>
              <span class="id">{{ row.elderlyId }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="warningType" label="预警类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.priority === 'high' ? 'danger' : 'warning'" size="small">
              {{ row.warningType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="预警描述" min-width="180">
          <template #default="{ row }">
            <el-tooltip :content="row.description" placement="top">
              <span class="desc-text">{{ row.description }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)" size="small">
              {{ getPriorityText(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="预警时间" width="160" />
        <el-table-column prop="location" label="位置" width="140" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" type="primary" link @click="handleView(row)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <el-button
                v-if="row.status !== 'resolved'"
                size="small"
                type="success"
                link
                @click="handleProcess(row)"
              >
                <el-icon><Edit /></el-icon>
                处理
              </el-button>
              <el-button size="small" type="info" link @click="handleExport(row)">
                <el-icon><Document /></el-icon>
                导出
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
            <span style="font-weight: 500">总共 {{ total }} 项</span>
          </template>
          <template #jumper="{ go }">
            <span style="margin-left: 8px; font-size: 14px; color: #666">跳至</span>
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

    <!-- ===== 处理弹框 ===== -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px">
      <el-form label-width="100px">
        <el-form-item label="老人姓名">
          <span class="form-value">{{ currentWarning?.elderlyName }}</span>
        </el-form-item>
        <el-form-item label="预警类型">
          <span class="form-value">{{ currentWarning?.warningType }}</span>
        </el-form-item>
        <el-form-item label="预警描述">
          <span class="form-value">{{ currentWarning?.description }}</span>
        </el-form-item>
        <el-form-item label="处理过程" required>
          <el-input
            v-model="handleForm.handler"
            type="textarea"
            :rows="3"
            placeholder="请输入处理过程..."
          />
        </el-form-item>
        <el-form-item label="处理结果" required>
          <el-input
            v-model="handleForm.handleNote"
            type="textarea"
            :rows="3"
            placeholder="请输入处理结果..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle">确认处理</el-button>
      </template>
    </el-dialog>

    <!-- ===== 查看详情弹框 ===== -->
    <el-dialog v-model="detailVisible" title="预警详情" width="560px">
      <div v-if="detailData" class="detail-content">
        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">预警ID</span>
            <span class="detail-value">#{{ String(detailData.id).slice(-6) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">老人姓名</span>
            <span class="detail-value">{{ detailData.elderlyName }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">老人编号</span>
            <span class="detail-value">{{ detailData.elderlyId }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">预警类型</span>
            <el-tag :type="detailData.priority === 'high' ? 'danger' : 'warning'" size="small">
              {{ detailData.warningType }}
            </el-tag>
          </div>
          <div class="detail-item">
            <span class="detail-label">优先级</span>
            <el-tag :type="getPriorityType(detailData.priority)" size="small">
              {{ getPriorityText(detailData.priority) }}
            </el-tag>
          </div>
          <div class="detail-item">
            <span class="detail-label">状态</span>
            <el-tag :type="getStatusType(detailData.status)" size="small">
              {{ getStatusText(detailData.status) }}
            </el-tag>
          </div>
          <div class="detail-item full-width">
            <span class="detail-label">预警描述</span>
            <span class="detail-value">{{ detailData.description }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">预警时间</span>
            <span class="detail-value">{{ detailData.time }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">位置</span>
            <span class="detail-value">{{ detailData.location }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">联系电话</span>
            <span class="detail-value">{{ detailData.contactPhone }}</span>
          </div>
          <div class="detail-item full-width">
            <span class="detail-label">紧急联系人</span>
            <span class="detail-value">{{ detailData.emergencyContact }}</span>
          </div>
          <div class="detail-item full-width">
            <span class="detail-label">建议措施</span>
            <span class="detail-value highlight">{{ detailData.suggestedAction }}</span>
          </div>
          <div v-if="detailData.handler" class="detail-item">
            <span class="detail-label">处理人</span>
            <span class="detail-value">{{ detailData.handler }}</span>
          </div>
          <div v-if="detailData.handleTime" class="detail-item">
            <span class="detail-label">处理时间</span>
            <span class="detail-value">{{ detailData.handleTime }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="detailData?.status !== 'resolved'"
          type="primary"
          @click="handleProcessAndClose(detailData)"
        >
          处理预警
        </el-button>
      </template>
    </el-dialog>

    <!-- ===== AI 视频跌倒检测 ===== -->
    <FallDetectDialog v-model="detectVisible" />
  </div>
</template>

<style scoped>
/* ===== 页面容器 ===== */
.warning-page {
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

/* ===== 空状态轮播 ===== */
.empty-carousel {
  background: #fff;
  border-radius: 12px;
  padding: 40px 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.empty-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: #67c23a;
}

.empty-content p {
  font-size: 16px;
  color: #999;
  margin: 0;
}

/* ===== 轮播图容器 ===== */
.carousel-wrapper {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.carousel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.carousel-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  font-size: 20px;
}

.title-text {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.pending-count {
  font-weight: 600;
  padding: 4px 16px;
  border-radius: 20px;
}

.carousel-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.carousel-controls .el-button {
  width: 32px;
  height: 32px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  border: 1px solid #dcdfe6;
}

.carousel-controls .el-button:hover {
  border-color: #409eff;
  color: #409eff;
}

.slide-indicator {
  font-size: 14px;
  color: #999;
  min-width: 50px;
  text-align: center;
}

/* ===== 轮播轨道 ===== */
.carousel-container {
  overflow: hidden;
  border-radius: 8px;
  position: relative;
}

.carousel-track {
  display: flex;
  transition: transform 0.5s ease;
}

.carousel-slide {
  min-width: 100%;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  padding: 4px 0;
}

/* ===== 预警卡片 ===== */
.warning-card {
  background: #f8f9fa;
  border-radius: 10px;
  padding: 16px 18px;
  border-left: 4px solid #999;
  transition: all 0.3s;
}

.warning-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.warning-card.high {
  border-left-color: #f56c6c;
}

.warning-card.medium {
  border-left-color: #e6a23c;
}

.warning-card.low {
  border-left-color: #409eff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  color: #fff;
  font-size: 14px;
}

.card-icon.high {
  background: #f56c6c;
}

.card-icon.medium {
  background: #e6a23c;
}

.card-icon.low {
  background: #409eff;
}

.card-name {
  font-weight: 600;
  color: #1a1a1a;
  font-size: 15px;
}

.card-time {
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-body {
  margin-bottom: 12px;
}

.card-field {
  font-size: 13px;
  line-height: 1.6;
  color: #666;
}

.field-label {
  color: #999;
}

.field-value {
  color: #333;
}

.field-value.highlight {
  color: #e6a23c;
  font-weight: 500;
}

.card-footer {
  display: flex;
  gap: 8px;
  padding-top: 10px;
  border-top: 1px solid #e8e8e8;
}

.card-footer .el-button {
  font-size: 12px;
  padding: 4px 12px;
}

/* ===== 指示点 ===== */
.carousel-dots {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 14px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #dcdfe6;
  cursor: pointer;
  transition: all 0.3s;
}

.dot.active {
  background: #409eff;
  width: 24px;
  border-radius: 4px;
}

.dot:hover {
  background: #409eff;
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

.filter-body :deep(.el-select) {
  width: 140px;
}

/* ===== 表格区域 ===== */
.table-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
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

.elderly-info {
  display: flex;
  flex-direction: column;
  line-height: 1.4;
}

.elderly-info .name {
  font-weight: 500;
  color: #333;
}

.elderly-info .id {
  font-size: 12px;
  color: #999;
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
  gap: 4px;
  flex-wrap: wrap;
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

.detail-value.highlight {
  color: #e6a23c;
  font-weight: 500;
}

.form-value {
  font-size: 14px;
  color: #333;
}

/* ===== 响应式 ===== */
@media (max-width: 1024px) {
  .carousel-slide {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .carousel-slide {
    grid-template-columns: 1fr;
  }

  .carousel-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .carousel-controls {
    align-self: flex-end;
  }

  .filter-body :deep(.el-form-item) {
    display: block;
    width: 100%;
  }

  .filter-body :deep(.el-select) {
    width: 100%;
  }

  .action-buttons {
    flex-wrap: wrap;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .warning-page {
    padding: 12px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 18px;
  }

  .warning-card {
    padding: 12px 14px;
  }

  .title-text {
    font-size: 14px;
  }

  .pending-count {
    font-size: 12px;
    padding: 2px 12px;
  }

  .card-name {
    font-size: 14px;
  }
}
</style>
