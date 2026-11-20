<template>
  <div class="caregiver-workbench">
    <div class="welcome-card">
      <div class="welcome-title">欢迎回来，{{ wb.name || '陪护人员' }}</div>
      <div class="welcome-sub">{{ positionText(wb.position) }}</div>
    </div>

    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-label">我负责的老人</div>
        <div class="stat-value">{{ wb.elderCount ?? '-' }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">今日任务</div>
        <div class="stat-value">{{ wb.taskCount ?? '-' }}</div>
      </div>
      <div class="stat-card danger">
        <div class="stat-label">待处置告警</div>
        <div class="stat-value">{{ wb.alertCount ?? '-' }}</div>
      </div>
    </div>

    <div class="quick-entry">
      <el-button type="primary" @click="router.push('/caregiver/elder')">查看我负责的老人</el-button>
      <el-button @click="router.push('/caregiver/task')">今日任务</el-button>
      <el-button type="success" plain @click="router.push('/caregiver/chat')">联系家属 / 老人</el-button>
      <el-button type="warning" @click="router.push('/caregiver/alert')">去处置告警</el-button>
      <el-button type="danger" plain @click="openApply">请假 / 换班申请</el-button>
    </div>

    <!-- 请假/换班申请弹窗 -->
    <el-dialog v-model="applyVisible" title="请假 / 换班申请" width="520px">
      <el-form label-width="90px">
        <el-form-item label="申请类型">
          <el-radio-group v-model="applyForm.applyType">
            <el-radio-button value="LEAVE">请假</el-radio-button>
            <el-radio-button value="SHIFT">换班</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="applyForm.startTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="选择开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="applyForm.endTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="选择结束时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="申请原因">
          <el-input v-model="applyForm.reason" type="textarea" :rows="2" placeholder="请填写申请原因" />
        </el-form-item>
      </el-form>

      <el-divider content-position="left">我的申请记录</el-divider>
      <el-table :data="myApplies" size="small" max-height="200">
        <el-table-column prop="applyType" label="类型" width="70">
          <template #default="{ row }">{{ row.applyType === 'LEAVE' ? '请假' : '换班' }}</template>
        </el-table-column>
        <el-table-column label="时间" min-width="140">
          <template #default="{ row }">{{ fmt(row.startTime) }} ~ {{ fmt(row.endTime) }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="120" show-overflow-tooltip />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="applyVisible = false">关闭</el-button>
        <el-button type="primary" :loading="submitting" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCaregiverWorkbench, type CaregiverWorkbench } from '@/api/caregiverPortal'
import { submitCaregiverApply, getMyApplies, type ScheduleApply } from '@/api/supervisorPortal'

const router = useRouter()
const wb = ref<CaregiverWorkbench>({})
const applyVisible = ref(false)
const submitting = ref(false)
const myApplies = ref<ScheduleApply[]>([])
const applyForm = ref({
  applyType: 'LEAVE',
  startTime: '',
  endTime: '',
  reason: '',
})

const positionText = (p?: string) =>
  ({ SENIOR_CAREGIVER: '高级护工', CAREGIVER: '护工', NURSING_SUPERVISOR: '护理主管', REHAB_NURSE: '康复护士', PSYCHO_COUNSELOR: '心理辅导员' }[p as string] || p || '')

const statusText = (s?: string) => ({ PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回' }[s as string] || s || '')
const statusType = (s?: string) => ({ PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }[s as string] as any || 'info')
const fmt = (v?: string) => (v ? String(v).replace('T', ' ').slice(0, 16) : '-')

async function loadApplies() {
  try {
    const res = await getMyApplies()
    if (res.data.code === 200) myApplies.value = res.data.data || []
  } catch (e) {
    console.warn('加载申请记录失败', e)
  }
}

function openApply() {
  applyForm.value = { applyType: 'LEAVE', startTime: '', endTime: '', reason: '' }
  applyVisible.value = true
  loadApplies()
}

async function submitApply() {
  if (!applyForm.value.startTime || !applyForm.value.endTime) {
    ElMessage.warning('请选择开始和结束时间')
    return
  }
  submitting.value = true
  try {
    await submitCaregiverApply({
      applyType: applyForm.value.applyType,
      startTime: applyForm.value.startTime,
      endTime: applyForm.value.endTime,
      reason: applyForm.value.reason,
    })
    ElMessage.success('申请已提交，等待主管审批')
    applyVisible.value = false
    await loadApplies()
  } catch (e) {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try {
    const res = await getCaregiverWorkbench()
    if (res.data.code === 200) {
      wb.value = res.data.data || {}
    }
  } catch (e) {
    console.warn('加载工作台失败', e)
  }
})
</script>

<style scoped>
.caregiver-workbench {
  padding: 8px 4px;
}

.welcome-card {
  background: linear-gradient(120deg, #52c41a, #36cfc9);
  color: #fff;
  border-radius: 10px;
  padding: 24px;
  margin-bottom: 16px;
}

.welcome-title {
  font-size: 22px;
  font-weight: 600;
}

.welcome-sub {
  margin-top: 6px;
  font-size: 14px;
  opacity: 0.9;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.stat-card.danger .stat-value {
  color: #f5222d;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #52c41a;
  margin-top: 8px;
}

.quick-entry {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}
</style>
