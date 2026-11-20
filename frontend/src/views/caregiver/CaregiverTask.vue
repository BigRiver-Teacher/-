<template>
  <ListManagePage
    title="今日任务"
    icon="Calendar"
    :stats="stats"
    :total="filteredList.length"
    :show-pagination="false"
    table-title="任务列表"
  >
    <template #header-actions>
      <el-button plain @click="loadTasks">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </template>

    <template #filters>
      <el-form inline>
        <el-form-item label="状态">
          <el-select v-model="filterStatus" placeholder="全部" clearable style="width: 140px">
            <el-option label="待处理" value="pending" />
            <el-option label="进行中" value="processing" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="filterPriority" placeholder="全部" clearable style="width: 140px">
            <el-option label="紧急" value="urgent" />
            <el-option label="重要" value="important" />
            <el-option label="普通" value="normal" />
          </el-select>
        </el-form-item>
      </el-form>
    </template>

    <template #table>
      <el-table :data="filteredList" v-loading="loading" border>
        <template #empty>
          <el-empty description="今日暂无任务" :image-size="80" />
        </template>
        <el-table-column prop="taskCode" label="编号" width="130" />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">{{ typeText(row.taskType) }}</template>
        </el-table-column>
        <el-table-column prop="elderName" label="老人" width="100" />
        <el-table-column prop="location" label="位置" width="120" />
        <el-table-column label="优先级" width="90">
          <template #default="{ row }">
            <el-tag :type="priorityType(row.priority)" size="small">{{ priorityText(row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="triggerReson" label="内容" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.taskStatus)" size="small">{{ statusText(row.taskStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.taskStatus !== 'completed'" link type="primary" @click="openFinish(row)">完成</el-button>
            <span v-else class="done">已完成</span>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <template #dialogs>
      <el-dialog v-model="finishVisible" title="完成任务" width="480px">
        <el-form label-width="80px">
          <el-form-item label="处理结果">
            <el-input v-model="finishForm.handleResult" type="textarea" :rows="3" placeholder="请填写处理结果" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="finishForm.note" placeholder="选填" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="finishVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmFinish">确认完成</el-button>
        </template>
      </el-dialog>
    </template>
  </ListManagePage>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import ListManagePage from '@/components/ListManagePage.vue'
import { getMyTasks, getCaregiverMe, type CaregiverTask } from '@/api/caregiverPortal'
import { processScheduleTask } from '@/api/schedule'

const tasks = ref<CaregiverTask[]>([])
const loading = ref(false)
const filterStatus = ref('')
const filterPriority = ref('')
const finishVisible = ref(false)
const finishForm = ref({ handleResult: '', note: '' })
const currentTask = ref<CaregiverTask | null>(null)
const myName = ref('')

const typeText = (t: string) =>
  ({ HEALTH_CHECK: '健康检查', DEVICE_REPAIR: '设备维修', ENV_INSPECT: '环境巡检', REHAB_TRAINING: '康复训练', MEDICATION: '用药提醒', NUTRITION: '营养餐', PSYCHOLOGY: '心理关怀', DAILY_VISIT: '日常巡视' }[t] || t)
const priorityText = (p: string) => ({ urgent: '紧急', important: '重要', medium: '中等', normal: '普通', low: '低' }[p] || p)
const priorityType = (p: string) => ({ urgent: 'danger', important: 'warning', medium: 'primary', normal: 'info', low: 'info' }[p] as any || 'info')
const statusText = (s: string) => ({ pending: '待处理', processing: '进行中', completed: '已完成' }[s] || s)
const statusType = (s: string) => ({ pending: 'warning', processing: 'primary', completed: 'success' }[s] as any || 'info')

const stats = computed(() => [
  { iconComponent: 'Calendar', value: tasks.value.length, label: '今日任务', iconBgColor: '#e6f7ff', iconColor: '#409eff' },
  { iconComponent: 'Loading', value: tasks.value.filter((t) => t.taskStatus === 'processing').length, label: '进行中', iconBgColor: '#fdf6ec', iconColor: '#e6a23c' },
  { iconComponent: 'CircleCheck', value: tasks.value.filter((t) => t.taskStatus === 'completed').length, label: '已完成', iconBgColor: '#f0f9eb', iconColor: '#67c23a' },
  { iconComponent: 'Warning', value: tasks.value.filter((t) => t.priority === 'urgent').length, label: '紧急任务', iconBgColor: '#fef0f0', iconColor: '#f56c6c' },
])

const filteredList = computed(() => {
  return tasks.value.filter((t) => {
    if (filterStatus.value && t.taskStatus !== filterStatus.value) return false
    if (filterPriority.value && t.priority !== filterPriority.value) return false
    return true
  })
})

async function loadMe() {
  try {
    const res = await getCaregiverMe()
    if (res.data.code === 200) myName.value = res.data.data?.name || ''
  } catch {
    /* 忽略 */
  }
}

async function loadTasks() {
  loading.value = true
  try {
    const res = await getMyTasks()
    if (res.data.code === 200) tasks.value = res.data.data || []
  } finally {
    loading.value = false
  }
}

function openFinish(row: CaregiverTask) {
  currentTask.value = row
  finishForm.value = { handleResult: '', note: '' }
  finishVisible.value = true
}

async function confirmFinish() {
  if (!currentTask.value) return
  try {
    await processScheduleTask(currentTask.value.id, {
      taskStatus: 'completed',
      handleResult: finishForm.value.handleResult,
      handler: myName.value,
      note: finishForm.value.note,
    })
    ElMessage.success('任务已完成')
    finishVisible.value = false
    loadTasks()
  } catch {
    /* 忽略 */
  }
}

onMounted(() => {
  loadMe()
  loadTasks()
})
</script>

<style scoped>
.done {
  color: #67c23a;
  font-size: 13px;
}
</style>
