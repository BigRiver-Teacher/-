<template>
  <div class="expert-schedule-page">
    <!-- 当前排班概览 -->
    <div class="schedule-overview">
      <div class="schedule-title"><el-icon><Calendar /></el-icon>当前排班</div>
      <div class="schedule-text">{{ scheduleText || '暂无排班信息' }}</div>
    </div>

    <ListManagePage
      title="排班请假"
      icon="Calendar"
      :stats="stats"
      :total="filteredApplies.length"
      :show-pagination="false"
      table-title="申请记录"
    >
      <template #header-actions>
        <el-button type="primary" @click="openApply">
          <el-icon><Plus /></el-icon>
          提交申请
        </el-button>
        <el-button plain @click="loadAll">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </template>

      <template #filters>
        <el-form inline>
          <el-form-item label="类型">
            <el-select v-model="filterType" placeholder="全部" clearable style="width: 140px" @change="applyFilter">
              <el-option label="请假" value="LEAVE" />
              <el-option label="换班" value="SHIFT" />
              <el-option label="排班变更" value="SCHEDULE" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="filterStatus" placeholder="全部" clearable style="width: 140px" @change="applyFilter">
              <el-option label="待审批" value="PENDING" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已驳回" value="REJECTED" />
            </el-select>
          </el-form-item>
        </el-form>
      </template>

      <template #table>
        <el-table :data="filteredApplies" v-loading="loading" border>
          <el-table-column label="类型" width="90">
            <template #default="{ row }">{{ typeText(row.applyType) }}</template>
          </el-table-column>
          <el-table-column label="开始时间" width="150">
            <template #default="{ row }">{{ fmt(row.startTime) }}</template>
          </el-table-column>
          <el-table-column label="结束时间" width="150">
            <template #default="{ row }">{{ fmt(row.endTime) }}</template>
          </el-table-column>
          <el-table-column prop="reason" label="原因" min-width="160" show-overflow-tooltip />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="reviewComment" label="审批意见" min-width="120" show-overflow-tooltip />
          <el-table-column label="操作" width="90" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="showDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <template #dialogs>
        <!-- 提交申请弹窗 -->
        <el-dialog v-model="applyVisible" title="提交请假 / 排班申请" width="480px" :close-on-click-modal="false">
          <el-form label-width="90px">
            <el-form-item label="申请类型">
              <el-select v-model="applyForm.applyType" style="width: 100%">
                <el-option label="请假" value="LEAVE" />
                <el-option label="换班" value="SHIFT" />
                <el-option label="排班变更" value="SCHEDULE" />
              </el-select>
            </el-form-item>
            <el-form-item label="开始时间">
              <el-date-picker v-model="applyForm.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" placeholder="选择开始时间" />
            </el-form-item>
            <el-form-item label="结束时间">
              <el-date-picker v-model="applyForm.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" placeholder="选择结束时间" />
            </el-form-item>
            <el-form-item label="申请原因">
              <el-input v-model="applyForm.reason" type="textarea" :rows="3" placeholder="请填写申请原因" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="applyVisible = false">取消</el-button>
            <el-button type="primary" :loading="submitting" @click="submitApplyForm">提交</el-button>
          </template>
        </el-dialog>

        <!-- 详情弹窗 -->
        <el-dialog v-model="detailVisible" title="申请详情" width="520px">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="类型">{{ typeText(detail.applyType) }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="statusType(detail.status)" size="small">{{ statusText(detail.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="开始时间">{{ fmt(detail.startTime) }}</el-descriptions-item>
            <el-descriptions-item label="结束时间">{{ fmt(detail.endTime) }}</el-descriptions-item>
            <el-descriptions-item label="原因" :span="2">{{ detail.reason || '-' }}</el-descriptions-item>
            <el-descriptions-item label="审批人" :span="2">{{ detail.reviewerName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="审批意见" :span="2">{{ detail.reviewComment || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-dialog>
      </template>
    </ListManagePage>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Calendar, Plus, Refresh } from '@element-plus/icons-vue'
import ListManagePage from '@/components/ListManagePage.vue'
import { getExpertMe } from '@/api/expertPortal'
import { submitApply, getMyApplies, type ExpertScheduleApply } from '@/api/expertSchedule'

const DAY_MAP: Record<string, string> = { mon: '周一', tue: '周二', wed: '周三', thu: '周四', fri: '周五', sat: '周六', sun: '周日' }
const SHIFT_MAP: Record<string, string> = { AM: '上午', PM: '下午', FULL: '全天' }

const scheduleText = ref('')
const applies = ref<ExpertScheduleApply[]>([])
const loading = ref(false)
const filterType = ref('')
const filterStatus = ref('')
const applyVisible = ref(false)
const detailVisible = ref(false)
const submitting = ref(false)
const applyForm = ref({ applyType: 'LEAVE', startTime: '', endTime: '', reason: '' })
const detail = ref<Partial<ExpertScheduleApply>>({})

const typeText = (t?: string) => ({ LEAVE: '请假', SHIFT: '换班', SCHEDULE: '排班变更' }[t as string] || t || '-')
const statusText = (s?: string) => ({ PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回' }[s as string] || s || '-')
const statusType = (s?: string) => ({ PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }[s as string] as any || 'info')
const fmt = (t?: string) => (t ? String(t).replace('T', ' ').slice(0, 16) : '-')

const stats = computed(() => [
  { iconComponent: 'Bell', value: applies.value.filter((a) => a.status === 'PENDING').length, label: '待审批', iconBgColor: '#fdf6ec', iconColor: '#e6a23c' },
  { iconComponent: 'CircleCheck', value: applies.value.filter((a) => a.status === 'APPROVED').length, label: '已通过', iconBgColor: '#f0f9eb', iconColor: '#67c23a' },
  { iconComponent: 'CircleClose', value: applies.value.filter((a) => a.status === 'REJECTED').length, label: '已驳回', iconBgColor: '#fef0f0', iconColor: '#f56c6c' },
  { iconComponent: 'Document', value: applies.value.length, label: '总申请', iconBgColor: '#e6f7ff', iconColor: '#409eff' },
])

const filteredApplies = computed(() => {
  return applies.value.filter((a) => {
    if (filterType.value && a.applyType !== filterType.value) return false
    if (filterStatus.value && a.status !== filterStatus.value) return false
    return true
  })
})

function applyFilter() {
  /* 筛选由 computed 自动响应 */
}

async function loadSchedule() {
  try {
    const res = await getExpertMe()
    if (res.data.code === 200 && res.data.data?.schedule) {
      const s = JSON.parse(res.data.data.schedule)
      scheduleText.value = Object.entries(s).map(([k, v]) => `${DAY_MAP[k] || k} ${SHIFT_MAP[v as string] || v}`).join('，')
    }
  } catch {
    /* 忽略 */
  }
}

async function loadApplies() {
  loading.value = true
  try {
    const res = await getMyApplies()
    if (res.data.code === 200) applies.value = res.data.data || []
  } finally {
    loading.value = false
  }
}

function loadAll() {
  loadSchedule()
  loadApplies()
  ElMessage.success('数据已刷新')
}

function openApply() {
  applyForm.value = { applyType: 'LEAVE', startTime: '', endTime: '', reason: '' }
  applyVisible.value = true
}

async function submitApplyForm() {
  if (!applyForm.value.startTime || !applyForm.value.endTime) {
    ElMessage.warning('请选择开始和结束时间')
    return
  }
  submitting.value = true
  try {
    const res = await submitApply({ ...applyForm.value })
    if (res.data.code === 200) {
      ElMessage.success('申请已提交')
      applyVisible.value = false
      loadApplies()
    }
  } finally {
    submitting.value = false
  }
}

function showDetail(row: ExpertScheduleApply) {
  detail.value = row
  detailVisible.value = true
}

onMounted(() => {
  loadSchedule()
  loadApplies()
})
</script>

<style scoped>
.expert-schedule-page {
  min-height: 100%;
}
.schedule-overview {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.schedule-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}
.schedule-title .el-icon {
  font-size: 18px;
  color: #409eff;
}
.schedule-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.7;
}
</style>
