<template>
  <ListManagePage
    title="健康记录"
    icon="Edit"
    :stats="stats"
    :total="activeTab === 'note' ? filteredNotes.length : filteredFalls.length"
    :show-pagination="false"
    table-title="记录列表"
  >
    <template #header-actions>
      <el-button type="primary" @click="openAdd">
        <el-icon><Plus /></el-icon>
        登记记录
      </el-button>
      <el-button plain @click="loadAll">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </template>

    <template #filters>
      <el-form inline>
        <el-form-item>
          <el-radio-group v-model="activeTab">
            <el-radio-button value="note">健康备注</el-radio-button>
            <el-radio-button value="fall">跌倒记录</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="activeTab === 'note'" label="类型">
          <el-select v-model="filterNoteType" placeholder="全部" clearable style="width: 140px">
            <el-option label="血压记录" value="BLOOD_PRESSURE" />
            <el-option label="日常巡视" value="DAILY_CHECK" />
            <el-option label="用药记录" value="MEDICATION" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
      </el-form>
    </template>

    <template #table>
      <!-- 健康备注 -->
      <el-table v-if="activeTab === 'note'" :data="filteredNotes" v-loading="loading" border>
        <template #empty><el-empty description="暂无健康备注" :image-size="80" /></template>
        <el-table-column label="时间" width="150">
          <template #default="{ row }">{{ fmt(row.recordTime) }}</template>
        </el-table-column>
        <el-table-column prop="elderName" label="老人" width="100" />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">{{ noteTypeText(row.noteType) }}</template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="180" show-overflow-tooltip />
        <el-table-column prop="recorder" label="记录人" width="100" />
        <el-table-column label="重要程度" width="90">
          <template #default="{ row }">
            <el-tag :type="importanceType(row.importance)" size="small">{{ importanceText(row.importance) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <!-- 跌倒记录 -->
      <el-table v-else :data="filteredFalls" v-loading="loading" border>
        <template #empty><el-empty description="暂无跌倒记录" :image-size="80" /></template>
        <el-table-column label="时间" width="150">
          <template #default="{ row }">{{ fmt(row.date) }}</template>
        </el-table-column>
        <el-table-column prop="elderName" label="老人" width="100" />
        <el-table-column prop="location" label="位置" width="140" />
        <el-table-column prop="cause" label="原因" min-width="160" show-overflow-tooltip />
        <el-table-column label="严重程度" width="90">
          <template #default="{ row }">
            <el-tag :type="severityType(row.severity)" size="small">{{ severityText(row.severity) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handler" label="处理人" width="100" />
      </el-table>
    </template>

    <template #dialogs>
      <el-dialog v-model="addVisible" title="登记健康记录" width="520px">
        <el-form label-width="90px">
          <el-form-item label="老人">
            <el-select v-model="addForm.elderId" placeholder="选择老人" style="width: 100%">
              <el-option v-for="e in elders" :key="e.id" :label="e.name" :value="e.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="记录类型">
            <el-select v-model="addForm.noteType" placeholder="选择类型" style="width: 100%">
              <el-option label="血压记录" value="BLOOD_PRESSURE" />
              <el-option label="日常巡视" value="DAILY_CHECK" />
              <el-option label="用药记录" value="MEDICATION" />
              <el-option label="其他" value="OTHER" />
            </el-select>
          </el-form-item>
          <el-form-item label="重要程度">
            <el-select v-model="addForm.importance" style="width: 100%">
              <el-option label="普通" value="NORMAL" />
              <el-option label="重要" value="IMPORTANT" />
              <el-option label="紧急" value="URGENT" />
            </el-select>
          </el-form-item>
          <el-form-item label="记录内容">
            <el-input v-model="addForm.content" type="textarea" :rows="3" placeholder="请填写记录内容" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="addVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmAdd">提交</el-button>
        </template>
      </el-dialog>
    </template>
  </ListManagePage>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import ListManagePage from '@/components/ListManagePage.vue'
import { getHealthNoteList, addHealthNote, type HealthNote } from '@/api/healthNote'
import { getFallList, type FallVO } from '@/api/fall'
import { getMyElders, getCaregiverMe, type CaregiverElder } from '@/api/caregiverPortal'

const activeTab = ref('note')
const notes = ref<HealthNote[]>([])
const falls = ref<FallVO[]>([])
const elders = ref<CaregiverElder[]>([])
const loading = ref(false)
const filterNoteType = ref('')
const addVisible = ref(false)
const myName = ref('')
const addForm = ref({ elderId: undefined as number | undefined, noteType: 'BLOOD_PRESSURE', importance: 'NORMAL', content: '' })

const noteTypeText = (t: string) => ({ BLOOD_PRESSURE: '血压记录', DAILY_CHECK: '日常巡视', MEDICATION: '用药记录', OTHER: '其他' }[t] || t)
const importanceText = (i: string) => ({ NORMAL: '普通', IMPORTANT: '重要', URGENT: '紧急' }[i] || i)
const importanceType = (i: string) => ({ NORMAL: 'info', IMPORTANT: 'warning', URGENT: 'danger' }[i] as any || 'info')
const severityText = (s: string) => ({ MILD: '轻度', MODERATE: '中度', SEVERE: '重度' }[s] || s)
const severityType = (s: string) => ({ MILD: 'info', MODERATE: 'warning', SEVERE: 'danger' }[s] as any || 'info')
const fmt = (t?: string) => (t ? String(t).replace('T', ' ').slice(0, 16) : '-')

const stats = computed(() => [
  { iconComponent: 'Edit', value: notes.value.length, label: '健康备注', iconBgColor: '#e6f7ff', iconColor: '#409eff' },
  { iconComponent: 'Warning', value: falls.value.length, label: '跌倒记录', iconBgColor: '#fef0f0', iconColor: '#f56c6c' },
  { iconComponent: 'Bell', value: notes.value.filter((n) => n.importance === 'URGENT' || n.importance === 'IMPORTANT').length, label: '重要记录', iconBgColor: '#fdf6ec', iconColor: '#e6a23c' },
  { iconComponent: 'Document', value: notes.value.length + falls.value.length, label: '总记录', iconBgColor: '#f0f9eb', iconColor: '#67c23a' },
])

const filteredNotes = computed(() => {
  if (!filterNoteType.value) return notes.value
  return notes.value.filter((n) => n.noteType === filterNoteType.value)
})
const filteredFalls = computed(() => falls.value)

async function loadMe() {
  try {
    const res = await getCaregiverMe()
    if (res.data.code === 200) myName.value = res.data.data?.name || ''
  } catch {
    /* 忽略 */
  }
}

async function loadElders() {
  try {
    const res = await getMyElders()
    if (res.data.code === 200) elders.value = res.data.data || []
  } catch {
    /* 忽略 */
  }
}

async function loadNotes() {
  loading.value = true
  try {
    const res = await getHealthNoteList({ page: 1, size: 100 })
    if (res.data.code === 200) notes.value = res.data.data?.list || []
  } finally {
    loading.value = false
  }
}

async function loadFalls() {
  try {
    const res = await getFallList({ page: 1, size: 100 })
    if (res.data.code === 200) falls.value = res.data.data?.list || []
  } catch {
    /* 忽略 */
  }
}

function loadAll() {
  loadNotes()
  loadFalls()
  ElMessage.success('数据已刷新')
}

function openAdd() {
  addForm.value = { elderId: undefined, noteType: 'BLOOD_PRESSURE', importance: 'NORMAL', content: '' }
  addVisible.value = true
}

async function confirmAdd() {
  if (!addForm.value.elderId || !addForm.value.content) {
    ElMessage.warning('请选择老人并填写内容')
    return
  }
  const elder = elders.value.find((e) => e.id === addForm.value.elderId)
  try {
    await addHealthNote({
      elderId: addForm.value.elderId,
      elderName: elder?.name || '',
      recordTime: new Date().toISOString().slice(0, 19),
      recorder: myName.value,
      noteType: addForm.value.noteType,
      content: addForm.value.content,
      importance: addForm.value.importance,
      fallowStatus: 'pending',
    })
    ElMessage.success('记录已提交')
    addVisible.value = false
    loadNotes()
  } catch {
    /* 忽略 */
  }
}

onMounted(() => {
  loadMe()
  loadElders()
  loadNotes()
  loadFalls()
})
</script>
