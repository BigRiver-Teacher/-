<template>
  <ListManagePage
    title="告警处置"
    icon="Bell"
    :stats="stats"
    :total="filteredList.length"
    :show-pagination="false"
    table-title="告警列表"
  >
    <template #header-actions>
      <el-button plain @click="loadList">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </template>

    <template #filters>
      <el-form inline>
        <el-form-item label="级别">
          <el-select v-model="filterPriority" placeholder="全部" clearable style="width: 130px">
            <el-option label="高" value="high" />
            <el-option label="中" value="medium" />
            <el-option label="低" value="low" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterStatus" placeholder="全部" clearable style="width: 130px">
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已处理" value="resolved" />
            <el-option label="已忽略" value="ignored" />
          </el-select>
        </el-form-item>
      </el-form>
    </template>

    <template #table>
      <el-table :data="filteredList" v-loading="loading" border :row-class-name="rowClass">
        <template #empty>
          <el-empty description="暂无告警" :image-size="80" />
        </template>
        <el-table-column label="告警时间" width="150">
          <template #default="{ row }">{{ fmt(row.time) }}</template>
        </el-table-column>
        <el-table-column prop="elderlyName" label="老人" width="100" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.warningType === 'EMERGENCY'" type="danger" size="small" class="sos-tag">紧急呼救</el-tag>
            <span v-else>{{ typeText(row.warningType) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="级别" width="80">
          <template #default="{ row }">
            <el-tag :type="priorityType(row.priority)" size="small">{{ priorityText(row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
        <el-table-column prop="location" label="位置" width="130" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'pending'" link type="primary" @click="openProcess(row)">处理</el-button>
            <el-button v-if="row.status === 'pending'" link type="info" @click="doIgnore(row)">忽略</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <template #dialogs>
      <el-dialog v-model="processVisible" title="处理告警" width="480px">
        <el-form label-width="80px">
          <el-form-item label="处理结果">
            <el-input v-model="processForm.handleNote" type="textarea" :rows="3" placeholder="请填写处理结果" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="processVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmProcess">确认处理</el-button>
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
import { getAlertList, processAlert, ignoreAlert, type AlertVO } from '@/api/alert'
import { getCaregiverMe } from '@/api/caregiverPortal'

const list = ref<AlertVO[]>([])
const loading = ref(false)
const filterPriority = ref('')
const filterStatus = ref('')
const processVisible = ref(false)
const processForm = ref({ handleNote: '' })
const currentAlert = ref<AlertVO | null>(null)
const myName = ref('')

const typeText = (t: string) => ({ FALL: '跌倒', ABNORMAL_GAIT: '步态异常', DEVICE_OFFLINE: '设备离线', EMERGENCY: '紧急呼救' }[t] || t)
const priorityText = (p: string) => ({ high: '高', medium: '中', low: '低' }[p] || p)
const priorityType = (p: string) => ({ high: 'danger', medium: 'warning', low: 'info' }[p] as any || 'info')
const statusText = (s: string) => ({ pending: '待处理', processing: '处理中', resolved: '已处理', ignored: '已忽略' }[s] || s)
const statusType = (s: string) => ({ pending: 'warning', processing: 'primary', resolved: 'success', ignored: 'info' }[s] as any || 'info')
const fmt = (t?: string) => (t ? String(t).replace('T', ' ').slice(0, 16) : '-')

const stats = computed(() => {
  const locations = new Set(list.value.map((a) => a.location).filter(Boolean))
  return [
    { iconComponent: 'Bell', value: list.value.filter((a) => a.status === 'pending').length, label: '待处理', iconBgColor: '#fdf6ec', iconColor: '#e6a23c' },
    { iconComponent: 'Warning', value: list.value.filter((a) => a.priority === 'high').length, label: '高级别', iconBgColor: '#fef0f0', iconColor: '#f56c6c' },
    { iconComponent: 'Phone', value: list.value.filter((a) => a.warningType === 'EMERGENCY').length, label: '紧急呼救', iconBgColor: '#fef0f0', iconColor: '#f56c6c' },
    { iconComponent: 'Location', value: locations.size, label: '涉及位置', iconBgColor: '#e6f7ff', iconColor: '#409eff' },
  ]
})

const filteredList = computed(() => {
  return list.value.filter((a) => {
    if (filterPriority.value && a.priority !== filterPriority.value) return false
    if (filterStatus.value && a.status !== filterStatus.value) return false
    return true
  })
})

// SOS 紧急呼救行红色高亮 + 闪烁
function rowClass({ row }: { row: AlertVO }) {
  return row.warningType === 'EMERGENCY' && row.status === 'pending' ? 'sos-row' : ''
}

async function loadMe() {
  try {
    const res = await getCaregiverMe()
    if (res.data.code === 200) myName.value = res.data.data?.name || ''
  } catch {
    /* 忽略 */
  }
}

async function loadList() {
  loading.value = true
  try {
    const res = await getAlertList({ page: 1, size: 100 })
    if (res.data.code === 200) list.value = res.data.data?.list || []
  } finally {
    loading.value = false
  }
}

function openProcess(row: AlertVO) {
  currentAlert.value = row
  processForm.value = { handleNote: '' }
  processVisible.value = true
}

async function confirmProcess() {
  if (!currentAlert.value) return
  try {
    await processAlert(currentAlert.value.id, { handler: myName.value, handleNote: processForm.value.handleNote })
    ElMessage.success('告警已处理')
    processVisible.value = false
    loadList()
  } catch {
    /* 忽略 */
  }
}

async function doIgnore(row: AlertVO) {
  try {
    await ignoreAlert(row.id)
    ElMessage.success('已忽略')
    loadList()
  } catch {
    /* 忽略 */
  }
}

onMounted(() => {
  loadMe()
  loadList()
})
</script>

<style scoped>
:deep(.el-table .sos-row) {
  background-color: #fef0f0 !important;
}
:deep(.el-table .sos-row td) {
  animation: sos-blink 1.2s ease-in-out infinite;
}
@keyframes sos-blink {
  0%,
  100% {
    background-color: #fef0f0;
  }
  50% {
    background-color: #fde2e2;
  }
}
.sos-tag {
  animation: sos-blink 1.2s ease-in-out infinite;
}
</style>
