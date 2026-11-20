<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllApplies, reviewApply } from '@/api/supervisorPortal'
import type { ScheduleApply } from '@/api/supervisorPortal'
import ListManagePage from '@/components/ListManagePage.vue'

const list = ref<ScheduleApply[]>([])
const loading = ref(false)

const APPLY_TYPE: Record<string, string> = { LEAVE: '请假', SHIFT: '换班' }
const STATUS: Record<string, { label: string; type: string }> = {
  PENDING: { label: '待审批', type: 'warning' },
  APPROVED: { label: '已通过', type: 'success' },
  REJECTED: { label: '已驳回', type: 'danger' },
}

// 筛选
const typeFilter = ref('')
const statusFilter = ref('')

const filteredList = computed(() =>
  list.value.filter((x) => {
    if (typeFilter.value && x.applyType !== typeFilter.value) return false
    if (statusFilter.value && x.status !== statusFilter.value) return false
    return true
  }),
)

// 概览统计
const stats = computed(() => {
  const c = (v: string) => list.value.filter((x) => x.status === v).length
  return [
    { iconComponent: 'AlarmClock', value: c('PENDING'), label: '待审批', iconBgColor: '#fdf6ec', iconColor: '#e6a23c' },
    { iconComponent: 'CircleCheck', value: c('APPROVED'), label: '已通过', iconBgColor: '#f0f9eb', iconColor: '#67c23a' },
    { iconComponent: 'CircleClose', value: c('REJECTED'), label: '已驳回', iconBgColor: '#fef0f0', iconColor: '#f56c6c' },
    { iconComponent: 'Document', value: list.value.length, label: '总申请', iconBgColor: '#e6f7ff', iconColor: '#409eff' },
  ]
})

async function load() {
  loading.value = true
  try {
    const res = await getAllApplies()
    list.value = res.data.data || []
  } catch {
    ElMessage.error('加载申请失败')
  } finally {
    loading.value = false
  }
}

function resetFilter() {
  typeFilter.value = ''
  statusFilter.value = ''
}

async function handleApprove(row: ScheduleApply) {
  try {
    await ElMessageBox.confirm(`确认通过「${row.caregiverName}」的${APPLY_TYPE[row.applyType] || row.applyType}申请？`, '审批确认', {
      confirmButtonText: '确认通过',
      cancelButtonText: '取消',
      type: 'success',
    })
  } catch {
    return
  }
  await reviewApply(row.id, { status: 'APPROVED', comment: '同意' })
  ElMessage.success('已通过')
  await load()
}

async function handleReject(row: ScheduleApply) {
  let comment = ''
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回申请', {
      confirmButtonText: '确认驳回',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '请输入驳回原因',
      inputValidator: (v: string) => (v && v.trim() ? true : '请填写驳回原因'),
    })
    comment = value.trim()
  } catch {
    return
  }
  await reviewApply(row.id, { status: 'REJECTED', comment })
  ElMessage.success('已驳回')
  await load()
}

function fmt(v?: string) {
  return v ? String(v).replace('T', ' ').slice(0, 16) : '-'
}

onMounted(load)
</script>

<template>
  <ListManagePage
    title="请假审批"
    icon="Checked"
    :stats="stats"
    :total="filteredList.length"
    :show-pagination="false"
    table-title="护工请假/换班申请"
  >
    <template #header-actions>
      <el-button :loading="loading" @click="load">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </template>

    <template #filters>
      <el-form inline>
        <el-form-item label="申请类型">
          <el-select v-model="typeFilter" placeholder="全部类型" clearable style="width: 140px">
            <el-option label="请假" value="LEAVE" />
            <el-option label="换班" value="SHIFT" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批状态">
          <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="待审批" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </template>

    <template #table>
      <el-table :data="filteredList" border v-loading="loading">
        <el-table-column prop="caregiverName" label="护工" width="100" />
        <el-table-column prop="community" label="社区" width="110" />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">{{ APPLY_TYPE[row.applyType] || row.applyType }}</template>
        </el-table-column>
        <el-table-column label="开始" width="140">
          <template #default="{ row }">{{ fmt(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="结束" width="140">
          <template #default="{ row }">{{ fmt(row.endTime) }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="STATUS[row.status]?.type || 'info'" size="small">
              {{ STATUS[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewComment" label="审批意见" width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button type="success" size="small" @click="handleApprove(row)">通过</el-button>
              <el-button type="danger" size="small" @click="handleReject(row)">驳回</el-button>
            </template>
            <span v-else class="done">已处理</span>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无申请记录" :image-size="100" />
        </template>
      </el-table>
    </template>
  </ListManagePage>
</template>

<style scoped>
.done {
  color: #909399;
  font-size: 13px;
}
</style>
