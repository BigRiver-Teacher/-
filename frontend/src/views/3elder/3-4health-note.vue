<template>
  <div class="note-page">
    <!-- ===== 页面标题 ===== -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Document /></el-icon>
        健康备注
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
          <div class="stat-label">总备注数</div>
        </div>
      </div>
      <div class="stat-card month">
        <div class="stat-icon">
          <el-icon><Plus /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.monthNew }}</div>
          <div class="stat-label">本月新增</div>
        </div>
      </div>
      <div class="stat-card pending">
        <div class="stat-icon">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.pendingFollow }}</div>
          <div class="stat-label">待跟进事项</div>
        </div>
      </div>
      <div class="stat-card important">
        <div class="stat-icon">
          <el-icon><Bell /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.importantCount }}</div>
          <div class="stat-label">重要提醒</div>
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
              v-model="filterForm.name"
              placeholder="请输入姓名"
              clearable
              style="width: 160px"
              prefix-icon="Search"
            />
          </el-form-item>
          <el-form-item label="备注类型">
            <el-select v-model="filterForm.type" placeholder="全部" clearable style="width: 130px">
              <el-option
                v-for="item in typeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="重要程度">
            <el-select
              v-model="filterForm.importance"
              placeholder="全部"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="item in importanceOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="跟进状态">
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
          <span>备注列表</span>
          <el-tag size="small" type="info">共 {{ total }} 条</el-tag>
        </div>
      </div>

      <el-table :data="paginatedList" stripe style="width: 100%" row-key="id" border>
        <el-table-column prop="name" label="老人姓名" width="90" align="center" />
        <el-table-column prop="date" label="记录时间" width="160" align="center" />
        <el-table-column prop="recorder" label="记录人" width="90" align="center" />
        <el-table-column prop="type" label="备注类型" width="100" align="center" />
        <el-table-column prop="content" label="内容摘要" min-width="180">
          <template #default="{ row }">
            <span class="content-text">{{ row.content }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="importance" label="重要程度" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getImportanceType(row.importance)" size="small">
              {{ row.importance }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="跟进状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ row.status }}
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
      title="添加健康备注"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" label-width="100px" :rules="formRules">
        <el-form-item label="老人姓名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入老人姓名" />
        </el-form-item>
        <el-form-item label="记录时间" prop="date">
          <el-date-picker
            v-model="formData.date"
            type="datetime"
            placeholder="选择记录时间"
            value-format="YYYY-MM-DD HH:mm"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="记录人" prop="recorder">
          <el-input v-model="formData.recorder" placeholder="请输入记录人" />
        </el-form-item>
        <el-form-item label="备注类型" prop="type">
          <el-select v-model="formData.type" style="width: 100%">
            <el-option label="健康观察" value="健康观察" />
            <el-option label="医嘱" value="医嘱" />
            <el-option label="日常护理" value="日常护理" />
            <el-option label="用药提醒" value="用药提醒" />
            <el-option label="心理关怀" value="心理关怀" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="formData.content"
            type="textarea"
            :rows="3"
            placeholder="请输入备注内容"
          />
        </el-form-item>
        <el-form-item label="重要程度" prop="importance">
          <el-select v-model="formData.importance" style="width: 100%">
            <el-option label="普通" value="普通" />
            <el-option label="重要" value="重要" />
            <el-option label="紧急" value="紧急" />
          </el-select>
        </el-form-item>
        <el-form-item label="跟进状态" prop="status">
          <el-select v-model="formData.status" style="width: 100%">
            <el-option label="待跟进" value="待跟进" />
            <el-option label="跟进中" value="跟进中" />
            <el-option label="已完成" value="已完成" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>

    <!-- ===== 详情弹框 ===== -->
    <el-dialog v-model="detailVisible" title="健康备注详情" width="520px">
      <div v-if="detailData" class="detail-grid">
        <div class="detail-item">
          <span class="detail-label">老人姓名</span>
          <span class="detail-value">{{ detailData.name }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">记录时间</span>
          <span class="detail-value">{{ detailData.date }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">记录人</span>
          <span class="detail-value">{{ detailData.recorder }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">备注类型</span>
          <span class="detail-value">{{ detailData.type }}</span>
        </div>
        <div class="detail-item full-width">
          <span class="detail-label">内容</span>
          <span class="detail-value">{{ detailData.content }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">重要程度</span>
          <el-tag :type="getImportanceType(detailData.importance)" size="small">
            {{ detailData.importance }}
          </el-tag>
        </div>
        <div class="detail-item">
          <span class="detail-label">跟进状态</span>
          <el-tag :type="getStatusType(detailData.status)" size="small">
            {{ detailData.status }}
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Refresh,
  Plus,
  View,
  Delete,
  Filter,
  List,
  Download,
  Clock,
  Bell,
  Document,
} from '@element-plus/icons-vue'
import { getHealthNoteList, addHealthNote, deleteHealthNote } from '@/api/healthNote'

// ===== 统计数据 =====
const stats = reactive({
  total: 0,
  monthNew: 0,
  pendingFollow: 0,
  importantCount: 0,
})

// ===== 数据列表 =====
const noteList = ref([])

// ===== 备注类型枚举映射（后端 code → 页面中文） =====
const NOTE_TYPE_TEXT_MAP = {
  HEALTH_OBSERVE: '健康观察',
  DOCTOR_ADVICE: '医嘱',
  DAILY_NURSE: '日常护理',
  DAILY_CHECK: '日常护理',
  MEDICINE_REMIND: '用药提醒',
  MEDICATION: '用药提醒',
  PSYCHOLOGY_CARE: '心理关怀',
  BLOOD_PRESSURE: '健康观察',
}

// ===== 备注类型枚举映射（页面中文 → 后端 code） =====
const NOTE_TYPE_CODE_MAP = {
  健康观察: 'HEALTH_OBSERVE',
  医嘱: 'DOCTOR_ADVICE',
  日常护理: 'DAILY_NURSE',
  用药提醒: 'MEDICINE_REMIND',
  心理关怀: 'PSYCHOLOGY_CARE',
}

// ===== 重要程度枚举映射 =====
const IMPORTANCE_TEXT_MAP = {
  NORMAL: '普通',
  IMPORTANT: '重要',
  URGENT: '紧急',
}

const IMPORTANCE_CODE_MAP = {
  普通: 'NORMAL',
  重要: 'IMPORTANT',
  紧急: 'URGENT',
}

// ===== 跟进状态枚举映射 =====
const FALLOW_TEXT_MAP = {
  PENDING: '待跟进',
  FOLLOWING: '跟进中',
  FINISH: '已完成',
  DONE: '已完成',
}

const FALLOW_CODE_MAP = {
  待跟进: 'PENDING',
  跟进中: 'FOLLOWING',
  已完成: 'FINISH',
}

// ===== 老人姓名 → elderId 映射（新增时后端要求 elderId） =====
const elderIdMap = computed(() => {
  const map = {}
  noteList.value.forEach((item) => {
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
    const res = await getHealthNoteList({ page: 1, size: 10000 })
    const data = res.data.data
    noteList.value = (data?.list || []).map((item) => ({
      ...item,
      name: item.elderName ?? item.name,
      date: item.recordTime ? String(item.recordTime).replace('T', ' ') : item.recordTime,
      type: NOTE_TYPE_TEXT_MAP[item.noteType] || item.noteType,
      importance: IMPORTANCE_TEXT_MAP[item.importance] || item.importance,
      status: FALLOW_TEXT_MAP[item.fallowStatus] || item.fallowStatus,
    }))
    updateStats()
  } catch (e) {
    // 错误提示已由响应拦截器统一处理
  }
}

// ===== 更新统计数据 =====
function updateStats() {
  const list = noteList.value
  stats.total = list.length

  const monthAgo = new Date()
  monthAgo.setMonth(monthAgo.getMonth() - 1)
  stats.monthNew = list.filter((item) => new Date(item.date) >= monthAgo).length

  stats.pendingFollow = list.filter(
    (item) => item.status === '待跟进' || item.status === '跟进中',
  ).length

  stats.importantCount = list.filter(
    (item) => item.importance === '重要' || item.importance === '紧急',
  ).length
}

// ===== 筛选条件 =====
const filterForm = reactive({
  name: '',
  type: '',
  importance: '',
  status: '',
  dateRange: [],
})

const typeOptions = [
  { label: '全部', value: '' },
  { label: '健康观察', value: '健康观察' },
  { label: '医嘱', value: '医嘱' },
  { label: '日常护理', value: '日常护理' },
  { label: '用药提醒', value: '用药提醒' },
  { label: '心理关怀', value: '心理关怀' },
]

const importanceOptions = [
  { label: '全部', value: '' },
  { label: '普通', value: '普通' },
  { label: '重要', value: '重要' },
  { label: '紧急', value: '紧急' },
]

const statusOptions = [
  { label: '全部', value: '' },
  { label: '待跟进', value: '待跟进' },
  { label: '跟进中', value: '跟进中' },
  { label: '已完成', value: '已完成' },
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
  recorder: '',
  type: '',
  content: '',
  importance: '',
  status: '',
})

const formRules = {
  name: [{ required: true, message: '请输入老人姓名', trigger: 'blur' }],
  date: [{ required: true, message: '请选择记录时间', trigger: 'change' }],
  recorder: [{ required: true, message: '请输入记录人', trigger: 'blur' }],
  type: [{ required: true, message: '请选择备注类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入备注内容', trigger: 'blur' }],
  importance: [{ required: true, message: '请选择重要程度', trigger: 'change' }],
  status: [{ required: true, message: '请选择跟进状态', trigger: 'change' }],
}

const formRef = ref(null)

// ===== 筛选数据 =====
const filteredList = computed(() => {
  let list = [...noteList.value]

  if (filterForm.name) {
    list = list.filter((item) => item.name.includes(filterForm.name))
  }
  if (filterForm.type) {
    list = list.filter((item) => item.type === filterForm.type)
  }
  if (filterForm.importance) {
    list = list.filter((item) => item.importance === filterForm.importance)
  }
  if (filterForm.status) {
    list = list.filter((item) => item.status === filterForm.status)
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
function getImportanceType(importance) {
  const map = {
    普通: 'info',
    重要: 'warning',
    紧急: 'danger',
  }
  return map[importance] || 'info'
}

function getStatusType(status) {
  const map = {
    待跟进: 'danger',
    跟进中: 'warning',
    已完成: 'success',
  }
  return map[status] || 'info'
}

// ===== 操作函数 =====
function handleView(row) {
  detailData.value = row
  detailVisible.value = true
}

function handleAdd() {
  Object.assign(formData, {
    name: '',
    date: '',
    recorder: '',
    type: '',
    content: '',
    importance: '',
    status: '',
  })
  formRef.value?.resetFields()
  dialogVisible.value = true
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除 "${row.name}" 的备注吗？`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      await deleteHealthNote(row.id)
      ElMessage.success('删除成功')
      await initData()
    })
    .catch(() => {})
}

function resetFilter() {
  filterForm.name = ''
  filterForm.type = ''
  filterForm.importance = ''
  filterForm.status = ''
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
      ElMessage.warning('未找到该老人的档案信息，无法添加健康备注')
      return
    }

    const payload = {
      elderId: elderId,
      elderName: formData.name,
      recordTime: toBackendDateTime(formData.date),
      recorder: formData.recorder,
      noteType: NOTE_TYPE_CODE_MAP[formData.type] || formData.type,
      content: formData.content,
      importance: IMPORTANCE_CODE_MAP[formData.importance] || formData.importance,
      fallowStatus: FALLOW_CODE_MAP[formData.status] || formData.status,
    }

    try {
      await addHealthNote(payload)
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
.note-page {
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

.stat-card.month .stat-icon {
  background: #f0f9eb;
  color: #67c23a;
}

.stat-card.pending .stat-icon {
  background: #fdf6ec;
  color: #e6a23c;
}

.stat-card.important .stat-icon {
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

  .note-page {
    padding: 12px;
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