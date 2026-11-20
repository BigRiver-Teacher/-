<template>
  <ListManagePage
    title="我负责的老人"
    icon="User"
    :stats="stats"
    :total="filteredList.length"
    :show-pagination="false"
    table-title="负责老人列表"
  >
    <template #header-actions>
      <el-button plain @click="loadList">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </template>

    <template #filters>
      <el-form inline>
        <el-form-item label="姓名">
          <el-input v-model="filterName" placeholder="请输入姓名" clearable prefix-icon="Search" style="width: 180px" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="filterPhone" placeholder="请输入电话" clearable prefix-icon="Phone" style="width: 180px" />
        </el-form-item>
      </el-form>
    </template>

    <template #table>
      <el-table :data="filteredList" v-loading="loading" border>
        <template #empty>
          <el-empty description="暂无负责的老人" :image-size="80" />
        </template>
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column label="性别" width="80">
          <template #default="{ row }">{{ genderText(row.gender) }}</template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="80" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <template #dialogs>
      <el-dialog v-model="detailVisible" title="老人详情" width="680px">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="base">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="姓名">{{ detail.name }}</el-descriptions-item>
              <el-descriptions-item label="性别">{{ genderText(detail.gender) }}</el-descriptions-item>
              <el-descriptions-item label="年龄">{{ detail.age }} 岁</el-descriptions-item>
              <el-descriptions-item label="电话">{{ detail.phone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="地址" :span="2">{{ detail.address || '-' }}</el-descriptions-item>
              <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          <el-tab-pane label="健康备注" name="note">
            <el-table :data="healthNotes" size="small" border max-height="320">
              <template #empty><el-empty description="暂无健康备注" :image-size="60" /></template>
              <el-table-column prop="recordTime" label="时间" width="150">
                <template #default="{ row }">{{ fmt(row.recordTime) }}</template>
              </el-table-column>
              <el-table-column prop="noteType" label="类型" width="110" />
              <el-table-column prop="recorder" label="记录人" width="100" />
              <el-table-column prop="content" label="内容" show-overflow-tooltip />
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </el-dialog>
    </template>
  </ListManagePage>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import ListManagePage from '@/components/ListManagePage.vue'
import { getMyElders, type CaregiverElder } from '@/api/caregiverPortal'
import { getHealthNoteList, type HealthNote } from '@/api/healthNote'

const elders = ref<CaregiverElder[]>([])
const loading = ref(false)
const filterName = ref('')
const filterPhone = ref('')
const detailVisible = ref(false)
const detail = ref<Partial<CaregiverElder>>({})
const activeTab = ref('base')
const healthNotes = ref<HealthNote[]>([])

const genderText = (g?: string) => ({ MALE: '男', FEMALE: '女' }[g as string] || g || '-')
const fmt = (t?: string) => (t ? String(t).replace('T', ' ').slice(0, 16) : '-')

const stats = computed(() => [
  { iconComponent: 'User', value: elders.value.length, label: '负责老人', iconBgColor: '#e6f7ff', iconColor: '#409eff' },
  { iconComponent: 'Male', value: elders.value.filter((e) => e.gender === 'MALE').length, label: '男性', iconBgColor: '#e6f7ff', iconColor: '#409eff' },
  { iconComponent: 'Female', value: elders.value.filter((e) => e.gender === 'FEMALE').length, label: '女性', iconBgColor: '#fef0f0', iconColor: '#f56c6c' },
  { iconComponent: 'Edit', value: elders.value.filter((e) => e.remark).length, label: '有备注', iconBgColor: '#fdf6ec', iconColor: '#e6a23c' },
])

const filteredList = computed(() => {
  return elders.value.filter((e) => {
    if (filterName.value && !e.name.includes(filterName.value)) return false
    if (filterPhone.value && !(e.phone || '').includes(filterPhone.value)) return false
    return true
  })
})

async function loadList() {
  loading.value = true
  try {
    const res = await getMyElders()
    if (res.data.code === 200) elders.value = res.data.data || []
  } finally {
    loading.value = false
  }
}

async function showDetail(row: CaregiverElder) {
  detail.value = row
  detailVisible.value = true
  activeTab.value = 'base'
  healthNotes.value = []
  try {
    const res = await getHealthNoteList({ elderName: row.name, page: 1, size: 50 })
    healthNotes.value = res.data.data?.list || []
  } catch {
    /* 忽略 */
  }
}

onMounted(loadList)
</script>
