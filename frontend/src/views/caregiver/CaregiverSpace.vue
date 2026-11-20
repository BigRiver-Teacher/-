<template>
  <ListManagePage
    title="空间巡检"
    icon="House"
    :stats="stats"
    :total="filteredList.length"
    :show-pagination="false"
    table-title="空间列表"
  >
    <template #header-actions>
      <el-button plain @click="loadList">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </template>

    <template #filters>
      <el-form inline>
        <el-form-item label="入住状态">
          <el-select v-model="filterStatus" placeholder="全部" clearable style="width: 140px">
            <el-option label="已入住" value="occupied" />
            <el-option label="空闲" value="vacant" />
          </el-select>
        </el-form-item>
      </el-form>
    </template>

    <template #table>
      <el-table :data="filteredList" v-loading="loading" border>
        <template #empty><el-empty description="暂无空间" :image-size="80" /></template>
        <el-table-column prop="code" label="编号" min-width="100" />
        <el-table-column prop="name" label="名称" min-width="130" />
        <el-table-column prop="elderName" label="入住老人" min-width="100">
          <template #default="{ row }">{{ row.elderName || '-' }}</template>
        </el-table-column>
        <el-table-column label="安全评级" min-width="90">
          <template #default="{ row }">
            <el-tag :type="ratingType(row.safetyRating)" size="small">{{ row.safetyRating || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最近检查" min-width="120">
          <template #default="{ row }">{{ fmt(row.lastInspection) }}</template>
        </el-table-column>
        <el-table-column label="检查结果" min-width="100">
          <template #default="{ row }">{{ resultText(row.lastInspectionResult) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openInspect(row)">提交检查</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <template #dialogs>
      <el-dialog v-model="inspectVisible" title="提交空间检查" width="480px">
        <el-form label-width="90px">
          <el-form-item label="空间">{{ currentSpace?.name || '' }}</el-form-item>
          <el-form-item label="检查结果">
            <el-select v-model="inspectForm.result" style="width: 100%">
              <el-option label="合格" value="PASS" />
              <el-option label="不合格" value="FAIL" />
              <el-option label="需整改" value="NEED_FIX" />
            </el-select>
          </el-form-item>
          <el-form-item label="问题描述">
            <el-input v-model="inspectForm.issue" type="textarea" :rows="3" placeholder="选填" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="inspectVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmInspect">提交</el-button>
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
import { getSpaceList, addInspection, type SpaceVO } from '@/api/space'
import { getCaregiverMe } from '@/api/caregiverPortal'

const spaces = ref<SpaceVO[]>([])
const loading = ref(false)
const filterStatus = ref('')
const inspectVisible = ref(false)
const currentSpace = ref<SpaceVO | null>(null)
const inspectForm = ref({ result: 'PASS', issue: '' })
const myName = ref('')

const fmt = (t?: string | null) => (t ? String(t).replace('T', ' ').slice(0, 10) : '-')
const ratingType = (r?: string) => ({ A: 'success', B: 'primary', C: 'warning', D: 'danger' }[r as string] || 'info')
const resultText = (r?: string) => ({ PASS: '合格', FAIL: '不合格', NEED_FIX: '需整改' }[r as string] || r || '-')

const stats = computed(() => [
  { iconComponent: 'House', value: spaces.value.length, label: '空间总数', iconBgColor: '#e6f7ff', iconColor: '#409eff' },
  { iconComponent: 'User', value: spaces.value.filter((s) => (s.occupancyStatus || '').toLowerCase() === 'occupied').length, label: '已入住', iconBgColor: '#f0f9eb', iconColor: '#67c23a' },
  { iconComponent: 'Lock', value: spaces.value.filter((s) => (s.occupancyStatus || '').toLowerCase() === 'vacant').length, label: '空闲', iconBgColor: '#fdf6ec', iconColor: '#e6a23c' },
  { iconComponent: 'Warning', value: spaces.value.filter((s) => (s.status || '').toLowerCase() === 'repair').length, label: '待维修', iconBgColor: '#fef0f0', iconColor: '#f56c6c' },
])

const filteredList = computed(() => {
  if (!filterStatus.value) return spaces.value
  return spaces.value.filter((s) => (s.occupancyStatus || '').toLowerCase() === filterStatus.value)
})

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
    const res = await getSpaceList({ page: 1, size: 100 })
    if (res.data.code === 200) spaces.value = res.data.data?.list || []
  } finally {
    loading.value = false
  }
}

function openInspect(row: SpaceVO) {
  currentSpace.value = row
  inspectForm.value = { result: 'PASS', issue: '' }
  inspectVisible.value = true
}

async function confirmInspect() {
  if (!currentSpace.value) return
  try {
    await addInspection({
      spaceId: currentSpace.value.id,
      type: 'MONTHLY',
      inspector: myName.value,
      result: inspectForm.value.result,
      issue: inspectForm.value.issue || undefined,
    })
    ElMessage.success('检查已提交')
    inspectVisible.value = false
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
