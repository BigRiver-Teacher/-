<template>
  <ListManagePage
    title="老人健康档案"
    icon="User"
    :stats="stats"
    :total="filteredList.length"
    :show-pagination="false"
    table-title="服务对象列表"
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
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column label="性别" width="80">
          <template #default="{ row }">{{ genderText(row.gender) }}</template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="80" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <template #dialogs>
      <el-dialog v-model="detailVisible" title="老人详情" width="720px">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="base">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="姓名">{{ detail.name }}</el-descriptions-item>
              <el-descriptions-item label="性别">{{ genderText(detail.gender) }}</el-descriptions-item>
              <el-descriptions-item label="年龄">{{ detail.age }} 岁</el-descriptions-item>
              <el-descriptions-item label="民族">{{ detail.ethnicity || '-' }}</el-descriptions-item>
              <el-descriptions-item label="身份证号">{{ detail.idCard || '-' }}</el-descriptions-item>
              <el-descriptions-item label="出生日期">{{ fmtDate(detail.birthDate) }}</el-descriptions-item>
              <el-descriptions-item label="电话">{{ detail.phone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="居住类型">{{ residenceText(detail.residenceType) }}</el-descriptions-item>
              <el-descriptions-item label="地址" :span="2">{{ detail.address || '-' }}</el-descriptions-item>
              <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          <el-tab-pane label="健康备注" name="note">
            <el-table :data="healthNotes" size="small" border max-height="320">
              <el-table-column prop="recordTime" label="时间" width="150">
                <template #default="{ row }">{{ fmt(row.recordTime) }}</template>
              </el-table-column>
              <el-table-column prop="noteType" label="类型" width="100" />
              <el-table-column prop="recorder" label="记录人" width="100" />
              <el-table-column prop="content" label="内容" show-overflow-tooltip />
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="跌倒记录" name="fall">
            <el-table :data="fallRecords" size="small" border max-height="320">
              <el-table-column prop="date" label="时间" width="150">
                <template #default="{ row }">{{ fmt(row.date) }}</template>
              </el-table-column>
              <el-table-column prop="location" label="位置" width="140" />
              <el-table-column prop="severity" label="严重程度" width="100" />
              <el-table-column prop="cause" label="原因" show-overflow-tooltip />
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
import { getElderList, type ElderVO } from '@/api/elder'
import { getHealthNoteList, type HealthNote } from '@/api/healthNote'
import { getFallList, type FallVO } from '@/api/fall'

const elderList = ref<ElderVO[]>([])
const loading = ref(false)
const filterName = ref('')
const filterPhone = ref('')
const detailVisible = ref(false)
const detail = ref<Partial<ElderVO>>({})
const activeTab = ref('base')
const healthNotes = ref<HealthNote[]>([])
const fallRecords = ref<FallVO[]>([])

const genderText = (g?: string) => ({ MALE: '男', FEMALE: '女' }[g as string] || g || '-')
const residenceText = (r?: string) => ({ PERMANENT: '长期居住', TEMPORARY: '临时居住' }[r as string] || r || '-')
const fmt = (t?: string) => (t ? String(t).replace('T', ' ').slice(0, 16) : '-')
const fmtDate = (t?: string) => (t ? String(t).slice(0, 10) : '-')

const stats = computed(() => [
  { iconComponent: 'User', value: elderList.value.length, label: '服务对象', iconBgColor: '#e6f7ff', iconColor: '#409eff' },
  { iconComponent: 'Male', value: elderList.value.filter((e) => e.gender === 'MALE').length, label: '男性', iconBgColor: '#e6f7ff', iconColor: '#409eff' },
  { iconComponent: 'Female', value: elderList.value.filter((e) => e.gender === 'FEMALE').length, label: '女性', iconBgColor: '#fef0f0', iconColor: '#f56c6c' },
  { iconComponent: 'House', value: elderList.value.filter((e) => e.residenceType === 'TEMPORARY').length, label: '临时居住', iconBgColor: '#fdf6ec', iconColor: '#e6a23c' },
])

const filteredList = computed(() => {
  return elderList.value.filter((e) => {
    if (filterName.value && !e.name.includes(filterName.value)) return false
    if (filterPhone.value && !(e.phone || '').includes(filterPhone.value)) return false
    return true
  })
})

async function loadList() {
  loading.value = true
  try {
    const res = await getElderList({ page: 1, size: 100 })
    if (res.data.code === 200) elderList.value = res.data.data?.list || []
  } finally {
    loading.value = false
  }
}

async function showDetail(row: ElderVO) {
  detail.value = row
  detailVisible.value = true
  activeTab.value = 'base'
  healthNotes.value = []
  fallRecords.value = []
  // 并行加载健康备注与跌倒记录
  try {
    const [noteRes, fallRes] = await Promise.all([
      getHealthNoteList({ elderName: row.name, page: 1, size: 50 }),
      getFallList({ elderName: row.name, page: 1, size: 50 }),
    ])
    healthNotes.value = noteRes.data.data?.list || []
    fallRecords.value = fallRes.data.data?.list || []
  } catch {
    /* 忽略 */
  }
}

onMounted(loadList)
</script>
