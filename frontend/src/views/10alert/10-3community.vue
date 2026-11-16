<template>
  <div class="community-page">
    <!-- ===== 页面标题 ===== -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Document /></el-icon>
        社区处理台账
      </h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加记录
        </el-button>
        <el-button type="success" plain @click="handleExport">
          <el-icon><Download /></el-icon>
          导出Excel
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
          <div class="stat-label">总处理数</div>
        </div>
      </div>
      <div class="stat-card today">
        <div class="stat-icon">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.today }}</div>
          <div class="stat-label">今日处理</div>
        </div>
      </div>
      <div class="stat-card pending-review">
        <div class="stat-icon">
          <el-icon><Operation /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.pendingReview }}</div>
          <div class="stat-label">待审核</div>
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
          <el-form-item label="处理人">
            <el-input
              v-model="filterForm.handler"
              placeholder="请输入处理人"
              clearable
              style="width: 150px"
              prefix-icon="Search"
            />
          </el-form-item>
          <el-form-item label="处理结果">
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
          <el-form-item label="审核状态">
            <el-select
              v-model="filterForm.reviewStatus"
              placeholder="全部"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="item in reviewStatusOptions"
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

    <!-- ===== 时间线区域 ===== -->
    <div class="timeline-section">
      <div class="timeline-header">
        <div class="timeline-title">
          <el-icon><Clock /></el-icon>
          <span>处理时间线</span>
          <el-tag size="small" type="info">共 {{ total }} 条记录</el-tag>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredList.length === 0" class="empty-state">
        <el-empty description="暂无处理记录" />
      </div>

      <!-- 时间线 -->
      <div v-else class="timeline-container">
        <div v-for="(group, groupIndex) in groupedList" :key="groupIndex" class="timeline-group">
          <!-- 日期分组标题 -->
          <div class="group-date">
            <span class="date-label">{{ group.date }}</span>
            <span class="date-count">{{ group.items.length }} 条记录</span>
          </div>

          <!-- 该日期下的记录 -->
          <div
            v-for="(item, itemIndex) in group.items"
            :key="itemIndex"
            class="timeline-item"
            :class="item.reviewStatus"
          >
            <!-- 时间线左侧 - 时间 + 状态图标 -->
            <div class="timeline-left">
              <div class="timeline-time">{{ item.time }}</div>
              <div class="timeline-dot" :class="item.reviewStatus">
                <el-icon v-if="item.reviewStatus === 'pending'"><Clock /></el-icon>
                <el-icon v-else-if="item.reviewStatus === 'approved'"><CircleCheck /></el-icon>
                <el-icon v-else-if="item.reviewStatus === 'rejected'"><CircleClose /></el-icon>
              </div>
              <div class="timeline-line"></div>
            </div>

            <!-- 时间线右侧 - 内容卡片 -->
            <div class="timeline-card">
              <div class="card-header">
                <div class="card-left">
                  <span class="card-id">#{{ item.code }}</span>
                  <span class="card-elder">{{ item.elderName }}</span>
                  <el-tag :type="getAlertTypeColor(item.alertType)" size="small">
                    {{ item.alertType }}
                  </el-tag>
                </div>
                <div class="card-right">
                  <el-tag :type="getReviewStatusType(item.reviewStatus)" size="small">
                    {{ getReviewStatusText(item.reviewStatus) }}
                  </el-tag>
                  <el-tag :type="getResultType(item.result)" size="small">
                    {{ item.result }}
                  </el-tag>
                </div>
              </div>

              <div class="card-body">
                <div class="card-row">
                  <span class="row-label">处理人：</span>
                  <span class="row-value">{{ item.handler }} ({{ item.handlerId }})</span>
                  <span class="row-label" style="margin-left: 20px">处理措施：</span>
                  <span class="row-value">{{ item.action }}</span>
                </div>
                <div class="card-row">
                  <span class="row-label">处理结果：</span>
                  <span class="row-value" :style="{ color: getResultColor(item.result) }">
                    {{ item.result }}
                  </span>
                  <span
                    v-if="item.reviewStatus === 'rejected' && item.rejectReason"
                    class="reject-reason"
                  >
                    <el-icon><Warning /></el-icon>
                    驳回原因：{{ item.rejectReason }}
                  </span>
                </div>
                <div class="card-desc">
                  {{ item.description }}
                </div>
                <!-- 附件 -->
                <div
                  v-if="item.attachments && item.attachments.length > 0"
                  class="card-attachments"
                >
                  <span class="attachment-label">附件：</span>
                  <span
                    v-for="(att, attIdx) in item.attachments"
                    :key="attIdx"
                    class="attachment-item"
                  >
                    <el-icon><Paperclip /></el-icon>
                    {{ att }}
                  </span>
                </div>
              </div>

              <div class="card-footer">
                <el-button size="small" type="primary" link @click="handleView(item)">
                  <el-icon><View /></el-icon>
                  详情
                </el-button>
                <el-button
                  v-if="item.reviewStatus !== 'approved'"
                  size="small"
                  type="warning"
                  link
                  @click="handleEdit(item)"
                >
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button
                  v-if="item.reviewStatus === 'pending'"
                  size="small"
                  type="success"
                  link
                  @click="handleReview(item)"
                >
                  <el-icon><Operation /></el-icon>
                  审核
                </el-button>
                <el-button
                  v-if="userRole === 'admin'"
                  size="small"
                  type="danger"
                  link
                  @click="handleDelete(item)"
                >
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
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
    <el-dialog v-model="detailVisible" title="处理记录详情" width="580px">
      <div v-if="detailData" class="detail-content">
        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">处理编号</span>
            <span class="detail-value" style="font-family: monospace">{{ detailData.code }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">关联告警</span>
            <span class="detail-value" style="font-family: monospace">{{
              detailData.alertCode || '-'
            }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">老人姓名</span>
            <span class="detail-value">{{ detailData.elderName }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">告警类型</span>
            <el-tag :type="getAlertTypeColor(detailData.alertType)" size="small">
              {{ detailData.alertType }}
            </el-tag>
          </div>
          <div class="detail-item">
            <span class="detail-label">处理人</span>
            <span class="detail-value">{{ detailData.handler }} ({{ detailData.handlerId }})</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">所属社区</span>
            <span class="detail-value">{{ detailData.community || '朝阳社区' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">处理措施</span>
            <span class="detail-value">{{ detailData.action }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">处理结果</span>
            <el-tag :type="getResultType(detailData.result)" size="small">
              {{ detailData.result }}
            </el-tag>
          </div>
          <div class="detail-item">
            <span class="detail-label">处理时间</span>
            <span class="detail-value">{{ detailData.fullTime || detailData.time }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">审核状态</span>
            <el-tag :type="getReviewStatusType(detailData.reviewStatus)" size="small">
              {{ getReviewStatusText(detailData.reviewStatus) }}
            </el-tag>
          </div>
          <div v-if="detailData.reviewer" class="detail-item">
            <span class="detail-label">审核人</span>
            <span class="detail-value">{{ detailData.reviewer }}</span>
          </div>
          <div v-if="detailData.reviewTime" class="detail-item">
            <span class="detail-label">审核时间</span>
            <span class="detail-value">{{ detailData.reviewTime }}</span>
          </div>
          <div
            v-if="detailData.reviewStatus === 'rejected' && detailData.rejectReason"
            class="detail-item full-width"
          >
            <span class="detail-label">驳回原因</span>
            <span class="detail-value" style="color: #f56c6c">{{ detailData.rejectReason }}</span>
          </div>
          <div class="detail-item full-width">
            <span class="detail-label">处理详情</span>
            <div class="desc-box">{{ detailData.description }}</div>
          </div>
          <div
            v-if="detailData.attachments && detailData.attachments.length > 0"
            class="detail-item full-width"
          >
            <span class="detail-label">附件</span>
            <div class="attachment-box">
              <span
                v-for="(att, attIdx) in detailData.attachments"
                :key="attIdx"
                class="attachment-tag"
              >
                <el-icon><Paperclip /></el-icon>
                {{ att }}
              </span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="detailData?.reviewStatus === 'pending'"
          type="success"
          @click="
            () => {
              detailVisible = false
              handleReview(detailData)
            }
          "
        >
          <el-icon><Operation /></el-icon>
          审核
        </el-button>
      </template>
    </el-dialog>

    <!-- ===== 审核弹框 ===== -->
    <el-dialog
      v-model="reviewDialogVisible"
      title="审核处理记录"
      width="480px"
      :close-on-click-modal="false"
    >
      <div v-if="reviewData" class="review-content">
        <div class="review-info">
          <div class="review-row">
            <span class="review-label">处理编号</span>
            <span class="review-value" style="font-family: monospace">{{ reviewData.code }}</span>
          </div>
          <div class="review-row">
            <span class="review-label">老人姓名</span>
            <span class="review-value">{{ reviewData.elderName }}</span>
          </div>
          <div class="review-row">
            <span class="review-label">处理人</span>
            <span class="review-value">{{ reviewData.handler }} ({{ reviewData.handlerId }})</span>
          </div>
          <div class="review-row">
            <span class="review-label">处理措施</span>
            <span class="review-value">{{ reviewData.action }}</span>
          </div>
          <div class="review-row">
            <span class="review-label">处理结果</span>
            <span class="review-value">{{ reviewData.result }}</span>
          </div>
        </div>
        <el-divider />
        <el-form label-width="80px">
          <el-form-item label="审核意见" required>
            <el-input
              v-model="reviewForm.comment"
              type="textarea"
              :rows="3"
              placeholder="请输入审核意见..."
            />
          </el-form-item>
          <el-form-item label="审核结果" required>
            <el-radio-group v-model="reviewForm.result">
              <el-radio label="approved">通过</el-radio>
              <el-radio label="rejected">驳回</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">确认审核</el-button>
      </template>
    </el-dialog>

    <!-- ===== 添加/编辑弹框 ===== -->
    <el-dialog
      v-model="formDialogVisible"
      :title="formTitle"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="老人姓名" prop="elderName">
          <el-input v-model="formData.elderName" placeholder="请输入老人姓名" />
        </el-form-item>
        <el-form-item label="告警类型" prop="alertType">
          <el-select v-model="formData.alertType" placeholder="请选择告警类型" style="width: 100%">
            <el-option label="跌倒检测" value="跌倒检测" />
            <el-option label="心率异常" value="心率异常" />
            <el-option label="设备离线" value="设备离线" />
            <el-option label="血压异常" value="血压异常" />
            <el-option label="活动量异常" value="活动量异常" />
            <el-option label="SOS紧急呼叫" value="SOS紧急呼叫" />
            <el-option label="睡眠质量差" value="睡眠质量差" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理措施" prop="action">
          <el-select v-model="formData.action" placeholder="请选择处理措施" style="width: 100%">
            <el-option label="上门查看" value="上门查看" />
            <el-option label="联系家属" value="联系家属" />
            <el-option label="送医就诊" value="送医就诊" />
            <el-option label="设备检查" value="设备检查" />
            <el-option label="电话回访" value="电话回访" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理结果" prop="result">
          <el-select v-model="formData.result" placeholder="请选择处理结果" style="width: 100%">
            <el-option label="已解决" value="已解决" />
            <el-option label="跟进中" value="跟进中" />
            <el-option label="待复查" value="待复查" />
            <el-option label="无法解决" value="无法解决" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理详情" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请详细描述处理过程..."
          />
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
            v-model:file-list="formData.fileList"
            action="#"
            :auto-upload="false"
            :limit="5"
            multiple
            accept=".pdf,.jpg,.jpeg,.png,.mp4,.doc,.docx"
          >
            <el-button size="small">
              <el-icon><Upload /></el-icon>
              上传附件
            </el-button>
            <template #tip>
              <div style="font-size: 12px; color: #999; margin-top: 4px">
                支持 PDF、图片、视频格式，单个文件不超过10MB，最多5个
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
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
  Operation,
  CircleCheck,
  CircleClose,
  Refresh,
  Plus,
  Download,
  Filter,
  Search,
  View,
  Edit,
  Delete,
  Warning,
  Paperclip,
  Upload,
} from '@element-plus/icons-vue'
import { getCommunityList, addCommunity, updateCommunity, deleteCommunity, reviewCommunity } from '@/api/community'

// ===== 用户角色（模拟） =====
const userRole = ref('admin')

// ===== 统计数据 =====
const stats = reactive({
  total: 0,
  today: 0,
  pendingReview: 0,
  completed: 0,
})

// ===== 短时间格式化（后端 time 可能是完整时间，页面时间线需要 HH:mm） =====
function formatShortTime(value: any): string {
  if (!value) return ''
  const s = String(value).replace('T', ' ')
  const m = s.match(/\d{4}-\d{2}-\d{2} \d{2}:\d{2}/)
  if (m) return m[0].slice(11)
  return s.slice(0, 5)
}

// ===== 数据列表 =====
const communityList = ref([])

// ===== 筛选条件 =====
const filterForm = reactive({
  elderName: '',
  handler: '',
  result: '',
  reviewStatus: '',
  dateRange: [],
})

const resultOptions = [
  { label: '全部', value: '' },
  { label: '已解决', value: '已解决' },
  { label: '跟进中', value: '跟进中' },
  { label: '待复查', value: '待复查' },
  { label: '无法解决', value: '无法解决' },
]

const reviewStatusOptions = [
  { label: '全部', value: '' },
  { label: '待审核', value: 'pending' },
  { label: '已通过', value: 'approved' },
  { label: '已驳回', value: 'rejected' },
]

// ===== 分页 =====
const currentPage = ref(1)
const pageSize = ref(5)

// ===== 详情弹框 =====
const detailVisible = ref(false)
const detailData = ref(null)

// ===== 审核弹框 =====
const reviewDialogVisible = ref(false)
const reviewData = ref(null)
const reviewForm = reactive({
  comment: '',
  result: 'approved',
})

// ===== 添加/编辑弹框 =====
const formDialogVisible = ref(false)
const formTitle = ref('添加处理记录')
const isEdit = ref(false)
const editItem = ref(null)

const formData = reactive({
  elderName: '',
  alertType: '',
  action: '',
  result: '',
  description: '',
  fileList: [],
})

const formRules = {
  elderName: [{ required: true, message: '请输入老人姓名', trigger: 'blur' }],
  alertType: [{ required: true, message: '请选择告警类型', trigger: 'change' }],
  action: [{ required: true, message: '请选择处理措施', trigger: 'change' }],
  result: [{ required: true, message: '请选择处理结果', trigger: 'change' }],
  description: [{ required: true, message: '请输入处理详情', trigger: 'blur' }],
}

const formRef = ref(null)

// ===== 加载社区处理台账 =====
async function loadCommunity() {
  try {
    const res = await getCommunityList({ page: 1, size: 1000 })
    const data = res.data.data
    const list = data?.list ?? []
    communityList.value = list.map((item) => ({
      id: item.id,
      code: item.code,
      elderName: item.elderName,
      alertType: item.alertType,
      action: item.action,
      handler: item.handler,
      handlerId: item.handlerId,
      result: item.result,
      reviewStatus: String(item.reviewStatus || '').toLowerCase(),
      time: formatShortTime(item.time || item.fullTime),
      fullTime: item.fullTime ? String(item.fullTime).replace('T', ' ') : item.fullTime || '',
      description: item.description || '',
      attachments: Array.isArray(item.attachments) ? item.attachments : [],
      community: item.community || '',
      alertCode: item.alertCode || '',
      reviewer: item.review || '',
      reviewTime: item.reviewTime ? String(item.reviewTime).replace('T', ' ') : item.reviewTime || '',
      rejectReason: item.rejectReason || '',
    }))
    updateStats()
  } catch {
    // 请求失败时由响应拦截器统一提示
  }
}

// ===== 初始化数据 =====
function initData() {
  loadCommunity()
}

// ===== 更新统计数据 =====
function updateStats() {
  const list = communityList.value
  stats.total = list.length
  const today = new Date().toISOString().split('T')[0]
  stats.today = list.filter((item) => (item.fullTime || '').startsWith(today)).length
  stats.pendingReview = list.filter((item) => item.reviewStatus === 'pending').length
  stats.completed = list.filter(
    (item) => item.reviewStatus === 'approved' || item.reviewStatus === 'rejected',
  ).length
}

// ===== 排序函数 =====
function sortByStatusAndTime(list) {
  const statusOrder = {
    pending: 0,
    approved: 1,
    rejected: 2,
  }

  return [...list].sort((a, b) => {
    // 先按状态排序（待审核 > 已通过 > 已驳回）
    if (statusOrder[a.reviewStatus] !== statusOrder[b.reviewStatus]) {
      return statusOrder[a.reviewStatus] - statusOrder[b.reviewStatus]
    }
    // 同状态下按时间倒序
    return new Date(b.fullTime) - new Date(a.fullTime)
  })
}

// ===== 按日期分组 =====
const groupedList = computed(() => {
  const sorted = sortByStatusAndTime(filteredList.value)
  const groups = {}

  sorted.forEach((item) => {
    const date = item.fullTime.split(' ')[0]
    if (!groups[date]) {
      groups[date] = []
    }
    groups[date].push(item)
  })

  // 按日期倒序排列
  const sortedDates = Object.keys(groups).sort((a, b) => new Date(b) - new Date(a))
  return sortedDates.map((date) => ({
    date: date,
    items: groups[date],
  }))
})

// ===== 筛选数据 =====
const filteredList = computed(() => {
  let list = [...communityList.value]

  if (filterForm.elderName) {
    list = list.filter((item) => item.elderName.includes(filterForm.elderName))
  }
  if (filterForm.handler) {
    list = list.filter((item) => item.handler.includes(filterForm.handler))
  }
  if (filterForm.result) {
    list = list.filter((item) => item.result === filterForm.result)
  }
  if (filterForm.reviewStatus) {
    list = list.filter((item) => item.reviewStatus === filterForm.reviewStatus)
  }
  if (filterForm.dateRange && filterForm.dateRange.length === 2) {
    list = list.filter((item) => {
      const date = item.fullTime.split(' ')[0]
      return date >= filterForm.dateRange[0] && date <= filterForm.dateRange[1]
    })
  }

  return list
})

const total = computed(() => filteredList.value.length)

// ===== 工具函数 =====
function getAlertTypeColor(type) {
  const map = {
    跌倒检测: 'danger',
    SOS紧急呼叫: 'danger',
    心率异常: 'warning',
    血压异常: 'warning',
    设备离线: 'info',
    活动量异常: 'info',
    睡眠质量差: 'info',
  }
  return map[type] || 'info'
}

function getResultType(result) {
  const map = {
    已解决: 'success',
    跟进中: 'warning',
    待复查: 'info',
    无法解决: 'danger',
  }
  return map[result] || 'info'
}

function getResultColor(result) {
  const map = {
    已解决: '#67c23a',
    跟进中: '#e6a23c',
    待复查: '#409eff',
    无法解决: '#f56c6c',
  }
  return map[result] || '#666'
}

function getReviewStatusType(status) {
  const map = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger',
  }
  return map[status] || 'info'
}

function getReviewStatusText(status) {
  const map = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已驳回',
  }
  return map[status] || status
}

// ===== 操作函数 =====
function handleView(item) {
  detailData.value = item
  detailVisible.value = true
}

function handleEdit(item) {
  isEdit.value = true
  editItem.value = item
  formTitle.value = '编辑处理记录'
  Object.assign(formData, {
    elderName: item.elderName,
    alertType: item.alertType,
    action: item.action,
    result: item.result,
    description: item.description,
    fileList: item.attachments.map((name, index) => ({
      name: name,
      url: '#',
      uid: index,
    })),
  })
  formDialogVisible.value = true
}

function handleAdd() {
  isEdit.value = false
  editItem.value = null
  formTitle.value = '添加处理记录'
  Object.assign(formData, {
    elderName: '',
    alertType: '',
    action: '',
    result: '',
    description: '',
    fileList: [],
  })
  formRef.value?.resetFields()
  formDialogVisible.value = true
}

function handleDelete(item) {
  ElMessageBox.confirm(`确定要删除处理记录 "${item.code}" 吗？此操作不可恢复！`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        await deleteCommunity(item.id)
        const index = communityList.value.findIndex(
          (i) =>
            i.code === item.code && i.elderName === item.elderName && i.fullTime === item.fullTime,
        )
        if (index > -1) {
          communityList.value.splice(index, 1)
        }
        ElMessage.success('删除成功')
        updateStats()
      } catch {
        // 失败提示由拦截器统一处理
      }
    })
    .catch(() => {})
}

function handleReview(item) {
  reviewData.value = item
  reviewForm.comment = ''
  reviewForm.result = 'approved'
  reviewDialogVisible.value = true
}

async function submitReview() {
  if (!reviewForm.comment) {
    ElMessage.warning('请输入审核意见')
    return
  }

  const row = reviewData.value
  if (!row) return
  try {
    await reviewCommunity(row.id, {
      reviewStatus: reviewForm.result,
      review: reviewForm.comment,
      rejectReason: reviewForm.result === 'rejected' ? reviewForm.comment : '',
    })
    const index = communityList.value.findIndex(
      (i) => i.code === row.code && i.elderName === row.elderName && i.fullTime === row.fullTime,
    )
    if (index > -1) {
      communityList.value[index].reviewStatus = reviewForm.result
      communityList.value[index].reviewer = reviewForm.comment
      communityList.value[index].reviewTime = new Date().toLocaleString('zh-CN')
      if (reviewForm.result === 'rejected') {
        communityList.value[index].rejectReason = reviewForm.comment
      }
    }
    ElMessage.success(`审核${reviewForm.result === 'approved' ? '通过' : '驳回'}成功`)
    reviewDialogVisible.value = false
    updateStats()
  } catch {
    // 失败提示由拦截器统一处理
  }
}

async function submitForm() {
  const valid = formRef.value ? await formRef.value.validate().catch(() => false) : true
  if (!valid) return

  try {
    if (isEdit.value && editItem.value) {
      await updateCommunity(editItem.value.id, {
        elderName: formData.elderName,
        alertType: formData.alertType,
        action: formData.action,
        result: formData.result,
        description: formData.description,
      })
      ElMessage.success('更新成功')
    } else {
      await addCommunity({
        elderName: formData.elderName,
        alertType: formData.alertType,
        action: formData.action,
        description: formData.description,
        community: '朝阳社区',
        result: formData.result,
        handler: '王工',
      })
      ElMessage.success('添加成功')
    }
    formDialogVisible.value = false
    await loadCommunity()
  } catch {
    // 失败提示由拦截器统一处理
  }
}

function handleExport() {
  ElMessage.success('导出Excel成功')
}

function resetFilter() {
  filterForm.elderName = ''
  filterForm.handler = ''
  filterForm.result = ''
  filterForm.reviewStatus = ''
  filterForm.dateRange = []
  currentPage.value = 1
}

function refreshData() {
  loadCommunity()
}

// ===== 生命周期 =====
onMounted(() => {
  initData()
})
</script>

<style scoped>
.community-page {
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
.stat-card.pending-review .stat-icon {
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

/* ===== 时间线区域 ===== */
.timeline-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 16px;
}

.timeline-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.timeline-title .el-icon {
  font-size: 18px;
  color: #409eff;
}

.empty-state {
  padding: 40px 0;
}

/* ===== 时间线分组 ===== */
.timeline-group {
  margin-bottom: 24px;
}

.group-date {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0 12px 0;
  border-bottom: 2px solid #e6e6e6;
}

.date-label {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.date-count {
  font-size: 13px;
  color: #999;
}

/* ===== 时间线项目 ===== */
.timeline-item {
  display: flex;
  gap: 16px;
  padding: 12px 0;
  position: relative;
}

.timeline-item:last-child .timeline-line {
  display: none;
}

/* ===== 时间线左侧 ===== */
.timeline-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 70px;
  flex-shrink: 0;
  position: relative;
}

.timeline-time {
  font-size: 13px;
  color: #999;
  font-weight: 500;
}

.timeline-dot {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 4px 0;
  flex-shrink: 0;
  font-size: 14px;
  color: #fff;
  z-index: 1;
}

.timeline-dot.pending {
  background: #e6a23c;
}
.timeline-dot.approved {
  background: #67c23a;
}
.timeline-dot.rejected {
  background: #f56c6c;
}

.timeline-line {
  flex: 1;
  width: 2px;
  background: #e6e6e6;
  min-height: 20px;
}

/* ===== 时间线卡片 ===== */
.timeline-card {
  flex: 1;
  background: #f8f9fa;
  border-radius: 10px;
  padding: 14px 18px;
  border-left: 4px solid #999;
  transition: all 0.3s;
}

.timeline-item.pending .timeline-card {
  border-left-color: #e6a23c;
}
.timeline-item.approved .timeline-card {
  border-left-color: #67c23a;
}
.timeline-item.rejected .timeline-card {
  border-left-color: #f56c6c;
}

.timeline-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
}

.card-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.card-id {
  font-size: 13px;
  font-weight: 600;
  color: #409eff;
  font-family: monospace;
}

.card-elder {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
}

.card-right {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.card-body {
  font-size: 14px;
  color: #666;
  line-height: 1.8;
}

.card-row {
  margin-bottom: 4px;
}

.row-label {
  color: #999;
}

.row-value {
  color: #333;
}

.reject-reason {
  margin-left: 16px;
  color: #f56c6c;
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.card-desc {
  padding: 6px 0;
  color: #555;
}

.card-attachments {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
  margin-top: 4px;
  padding: 4px 0;
}

.attachment-label {
  font-size: 13px;
  color: #999;
}

.attachment-item {
  font-size: 13px;
  color: #409eff;
  display: inline-flex;
  align-items: center;
  gap: 2px;
  cursor: pointer;
  padding: 2px 8px;
  background: #e6f7ff;
  border-radius: 4px;
}

.attachment-item:hover {
  text-decoration: underline;
}

.card-footer {
  display: flex;
  gap: 4px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}

.card-footer .el-button {
  font-size: 12px;
  padding: 4px 8px;
}

.card-footer .el-button .el-icon {
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

.desc-box {
  font-size: 14px;
  color: #555;
  line-height: 1.8;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 6px;
}

.attachment-box {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.attachment-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #409eff;
  padding: 4px 12px;
  background: #e6f7ff;
  border-radius: 4px;
  cursor: pointer;
}

.attachment-tag:hover {
  text-decoration: underline;
}

/* ===== 审核弹框 ===== */
.review-content {
  padding: 4px 0;
}

.review-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4px 16px;
}

.review-row {
  display: flex;
  flex-direction: column;
  padding: 4px 0;
}

.review-label {
  font-size: 12px;
  color: #999;
}

.review-value {
  font-size: 14px;
  color: #333;
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

  .timeline-item {
    flex-direction: column;
  }

  .timeline-left {
    flex-direction: row;
    width: 100%;
    gap: 8px;
  }

  .timeline-line {
    display: none !important;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .review-info {
    grid-template-columns: 1fr;
  }

  .community-page {
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

  .timeline-card {
    padding: 12px;
  }

  .card-left {
    flex-wrap: wrap;
  }

  .card-right {
    flex-wrap: wrap;
  }

  .card-footer {
    flex-wrap: wrap;
  }
}
</style>