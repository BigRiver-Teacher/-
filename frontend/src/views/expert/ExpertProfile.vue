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
import { getExpertMe, type ExpertMe } from '@/api/expertPortal'

const router = useRouter()
const userStore = useUserStore()
const me = ref<ExpertMe>({})

const userInfo = computed(() => ({
  name: me.value.name || '专家',
  role: me.value.title || '专家',
  phone: me.value.phone || '',
  avatar: 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg',
}))

const stats = computed(() => [
  { iconComponent: 'Star', value: me.value.rating ?? '-', label: '服务评分', iconBgColor: '#fdf6ec', iconColor: '#e6a23c' },
  { iconComponent: 'ChatDotRound', value: me.value.todayCount ?? '-', label: '今日咨询', iconBgColor: '#e6f7ff', iconColor: '#409eff' },
  { iconComponent: 'DataAnalysis', value: me.value.totalCount ?? '-', label: '累计咨询', iconBgColor: '#f0f9eb', iconColor: '#67c23a' },
  { iconComponent: 'OfficeBuilding', value: me.value.institution || '-', label: '所属机构', iconBgColor: '#fef0f0', iconColor: '#f56c6c' },
])

const quickActions = [
  { icon: 'ChatDotRound', label: '我的咨询', path: '/expert/consult' },
  { icon: 'Calendar', label: '排班请假', path: '/expert/schedule' },
  { icon: 'User', label: '老人健康档案', path: '/expert/elder' },
  { icon: 'Tools', label: '干预方案建议', path: '/expert/intervention' },
]

function handleSaveProfile(data: { name: string; avatar?: string }) {
  // TODO: 对接后端更新昵称/头像接口
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
    const res = await getExpertMe()
    if (res.data.code === 200) me.value = res.data.data || {}
  } catch (e) {
    console.warn('加载个人档案失败', e)
  }
})
</script>
