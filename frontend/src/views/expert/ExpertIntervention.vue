<template>
  <ListManagePage
    title="干预方案建议"
    icon="Tools"
    :stats="stats"
    :total="filteredList.length"
    :show-pagination="false"
    table-title="干预方案列表"
  >
    <template #header-actions>
      <el-button plain @click="loadList">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </template>

    <template #filters>
      <el-form inline>
        <el-form-item label="方案名称">
          <el-input v-model="filterName" placeholder="请输入方案名称" clearable prefix-icon="Search" style="width: 180px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterStatus" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
      </el-form>
    </template>

    <template #table>
      <el-table :data="filteredList" v-loading="loading" border>
        <el-table-column prop="code" label="编号" width="130" />
        <el-table-column prop="name" label="方案名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="110" />
        <el-table-column prop="leader" label="负责人" width="100" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="进度" width="140">
          <template #default="{ row }">
            <el-progress :percentage="row.progress || 0" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <template #dialogs>
      <el-dialog v-model="detailVisible" :title="detail.name || '干预方案详情'" width="680px">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="方案编号">{{ detail.code }}</el-descriptions-item>
          <el-descriptions-item label="干预类型">{{ detail.type }}</el-descriptions-item>
          <el-descriptions-item label="负责人">{{ detail.leader }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.status }}</el-descriptions-item>
          <el-descriptions-item label="目标人群">{{ detail.targetGroup || '-' }}</el-descriptions-item>
          <el-descriptions-item label="周期">{{ detail.totalWeeks ? `${detail.totalWeeks} 周` : '-' }}</el-descriptions-item>
          <el-descriptions-item label="干预目标" :span="2">{{ join(detail.goals) }}</el-descriptions-item>
          <el-descriptions-item label="具体措施" :span="2">{{ join(detail.measures) }}</el-descriptions-item>
          <el-descriptions-item label="关联老人" :span="2">{{ join(detail.associatedElders) }}</el-descriptions-item>
          <el-descriptions-item label="注意事项" :span="2">{{ detail.notes || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-dialog>
    </template>
  </ListManagePage>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import ListManagePage from '@/components/ListManagePage.vue'
import { getInterventionList, type InterventionVO } from '@/api/intervention'

const list = ref<InterventionVO[]>([])
const loading = ref(false)
const filterName = ref('')
const filterStatus = ref('')
const detailVisible = ref(false)
const detail = ref<Partial<InterventionVO>>({})

const statusOptions = computed(() => Array.from(new Set(list.value.map((i) => i.status).filter(Boolean))))

const statusType = (s?: string) => ({ 进行中: 'primary', 已完成: 'success', 已暂停: 'warning', 已终止: 'danger' }[s as string] || 'info')

const stats = computed(() => [
  { iconComponent: 'Document', value: list.value.length, label: '方案总数', iconBgColor: '#e6f7ff', iconColor: '#409eff' },
  { iconComponent: 'Loading', value: list.value.filter((i) => (i.progress || 0) < 100).length, label: '进行中', iconBgColor: '#fdf6ec', iconColor: '#e6a23c' },
  { iconComponent: 'CircleCheck', value: list.value.filter((i) => (i.progress || 0) >= 100).length, label: '已完成', iconBgColor: '#f0f9eb', iconColor: '#67c23a' },
  { iconComponent: 'Warning', value: list.value.reduce((s, i) => s + (i.abnormalCount || 0), 0), label: '异常记录', iconBgColor: '#fef0f0', iconColor: '#f56c6c' },
])

const filteredList = computed(() => {
  return list.value.filter((i) => {
    if (filterName.value && !i.name.includes(filterName.value)) return false
    if (filterStatus.value && i.status !== filterStatus.value) return false
    return true
  })
})

const join = (v: string[] | string | undefined) => {
  if (Array.isArray(v)) return v.join('；')
  if (typeof v === 'string') {
    try {
      const arr = JSON.parse(v)
      if (Array.isArray(arr)) return arr.join('；')
    } catch {
      return v
    }
  }
  return '-'
}

async function loadList() {
  loading.value = true
  try {
    const res = await getInterventionList({ page: 1, size: 100 })
    if (res.data.code === 200) list.value = res.data.data?.list || []
  } finally {
    loading.value = false
  }
}

function showDetail(row: InterventionVO) {
  detail.value = row
  detailVisible.value = true
}

onMounted(loadList)
</script>
