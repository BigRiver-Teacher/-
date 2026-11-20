<template>
  <ProfilePage
    :title="'个人中心'"
    :user-info="userInfo"
    :stats="stats"
    :quick-actions="quickActions"
    @save-profile="handleSaveProfile"
    @change-password="handleChangePassword"
    @logout="handleLogout"
  />
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import ProfilePage from '@/components/ProfilePage.vue'
import { useUserStore } from '@/stores/user'
import { getCaregiverMe, getCaregiverWorkbench } from '@/api/caregiverPortal'

const router = useRouter()
const userStore = useUserStore()
const me = ref({ name: '', position: '', phone: '', community: '' })
const wb = ref({ elderCount: 0, taskCount: 0, alertCount: 0 })

const positionText = (p?: string) =>
  ({ SENIOR_CAREGIVER: '高级护工', CAREGIVER: '护工', NURSING_SUPERVISOR: '护理主管', REHAB_NURSE: '康复护士', PSYCHO_COUNSELOR: '心理辅导员' }[p as string] || p || '护工')

const userInfo = computed(() => ({
  name: me.value.name || '陪护人员',
  role: positionText(me.value.position),
  phone: me.value.phone || '',
  avatar: 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg',
}))

const stats = computed(() => [
  { iconComponent: 'User', value: wb.value.elderCount ?? '-', label: '负责老人', iconBgColor: '#e6f7ff', iconColor: '#409eff' },
  { iconComponent: 'Calendar', value: wb.value.taskCount ?? '-', label: '今日任务', iconBgColor: '#fdf6ec', iconColor: '#e6a23c' },
  { iconComponent: 'Bell', value: wb.value.alertCount ?? '-', label: '待处置告警', iconBgColor: '#fef0f0', iconColor: '#f56c6c' },
  { iconComponent: 'OfficeBuilding', value: me.value.community || '-', label: '所属社区', iconBgColor: '#f0f9eb', iconColor: '#67c23a' },
])

const quickActions = [
  { icon: 'User', label: '我负责的老人', path: '/caregiver/elder' },
  { icon: 'Calendar', label: '今日任务', path: '/caregiver/task' },
  { icon: 'ChatDotRound', label: '联系家属', path: '/caregiver/chat' },
  { icon: 'Bell', label: '告警处置', path: '/caregiver/alert' },
]

function handleSaveProfile(data: { name: string; avatar?: string }) {
  me.value.name = data.name
  ElMessage.success('资料已更新（演示：暂存本地）')
}

function handleChangePassword() {
  ElMessage.info('密码修改请通过登录页"忘记密码"流程操作')
}

function handleLogout() {
  userStore.clearToken()
  router.push('/login')
}

onMounted(async () => {
  try {
    const [meRes, wbRes] = await Promise.all([getCaregiverMe(), getCaregiverWorkbench()])
    if (meRes.data.code === 200) me.value = meRes.data.data || me.value
    if (wbRes.data.code === 200) wb.value = wbRes.data.data || wb.value
  } catch (e) {
    console.warn('加载个人档案失败', e)
  }
})
</script>
