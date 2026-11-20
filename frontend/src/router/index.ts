// router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import layout from '../views/layout.vue'
import placeholder from '../views/placeholder.vue'

// 角色 → 默认首页（登录后按此重定向）
const ROLE_HOME: Record<string, string> = {
  admin: '/home/management',
  staff: '/home/management',
  expert: '/expert/workbench',
  caregiver: '/caregiver/workbench',
  supervisor: '/caregiver/workbench',
  elder: '/elder/home',
}

const routes: RouteRecordRaw[] = [
  {
    // 根路径不再写死 redirect:'/login'，改为按当前登录角色动态分发。
    // 旧写法在路由解析阶段就被重定向到登录页，导致守卫中的按角色分发分支永远不可达。
    path: '/',
    redirect: () => {
      const token = localStorage.getItem('token')
      if (!token) return '/login'
      const role = localStorage.getItem('role') || 'admin'
      return ROLE_HOME[role] || '/home/management'
    },
  },
  {
    path: '/login',
    component: () => import('../views/loginNew.vue'),
  },
  {
    path: '/echart',
    redirect: '/echart/monitor',
    meta: { roles: ['admin', 'staff'] },
  },
  {
    path: '/echart/monitor',
    component: () => import('../views/Echart-monitor.vue'),
    meta: { roles: ['admin', 'staff'] },
  },
  {
    path: '/echart/device',
    component: () => import('../views/Echart-device.vue'),
    meta: { roles: ['admin', 'staff'] },
  },
  {
    path: '/echart/person',
    component: () => import('../views/Echart-person.vue'),
    meta: { roles: ['admin', 'staff'] },
  },
  {
    path: '/echart/export',
    component: () => import('../views/Echart-export.vue'),
    meta: { roles: ['admin', 'staff'] },
  },
  // ===== 管理员端 =====
  {
    path: '/home',
    component: layout,
    meta: { roles: ['admin', 'staff'] },
    children: [
      { path: 'management', component: () => import('../views/1management.vue'), meta: { title: '管理主页' } },
      { path: 'warning', component: () => import('../views/2warning.vue'), meta: { title: '跌倒预警' } },
      { path: 'elder/base', component: () => import('../views/3elder/3-1base.vue'), meta: { title: '基础档案' } },
      { path: 'elder/gait', component: () => import('../views/3elder/3-2gait.vue'), meta: { title: '步态历史' } },
      { path: 'elder/fall-record', component: () => import('../views/3elder/3-3fall-record.vue'), meta: { title: '既往跌倒记录' } },
      { path: 'elder/health-note', component: () => import('../views/3elder/3-4health-note.vue'), meta: { title: '健康备注' } },
      { path: 'device', component: () => import('../views/6device.vue'), meta: { title: '传感设备管理' } },
      { path: 'expert', component: () => import('../views/7expert.vue'), meta: { title: '专家康养咨询' } },
      { path: 'caregiver', component: () => import('../views/8caregiver.vue'), meta: { title: '陪护人员管理' } },
      { path: 'profile', component: () => import('../views/9profile.vue'), meta: { title: '个人主页' } },
      { path: 'alert/list', component: () => import('../views/10alert/10-1list.vue'), meta: { title: '实时告警列表' } },
      { path: 'alert/push-log', component: () => import('../views/10alert/10-2push-log.vue'), meta: { title: '家属推送日志' } },
      { path: 'alert/community', component: () => import('../views/10alert/10-3community.vue'), meta: { title: '社区处理台账' } },
      { path: 'alert/preference', component: () => import('../views/10alert/10-4preference.vue'), meta: { title: '通知偏好设置' } },
      { path: 'space', component: () => import('../views/11space.vue'), meta: { title: '居家空间档案' } },
      { path: 'intervention', component: () => import('../views/12intervention.vue'), meta: { title: '干预方案管理' } },
      { path: 'schedule', component: () => import('../views/13schedule.vue'), meta: { title: '日常康养日程' } },
      { path: 'system/permission', component: () => import('../views/14system/14-1permission.vue'), meta: { title: '账号权限' } },
      { path: 'system/log', component: () => import('../views/14system/14-2log.vue'), meta: { title: '操作日志' } },
      { path: 'system/backup', component: () => import('../views/14system/14-3backup.vue'), meta: { title: '数据备份' } },
      { path: 'system/model', component: () => import('../views/14system/14-4model.vue'), meta: { title: '模型参数设置' } },
      { path: 'chat-monitor', component: () => import('../views/AdminChatMonitor.vue'), meta: { title: '对话监管' } },
      { path: 'custom/sidebar', component: () => import('../views/15custom/15-1sidebar.vue'), meta: { title: '侧边栏自定义' } },
      { path: 'custom/dashboard', component: () => import('../views/15custom/15-2dashboard.vue'), meta: { title: '首页看板布局' } },
      { path: 'custom/mobile', component: () => import('../views/15custom/15-3mobile.vue'), meta: { title: '移动端导航设置' } },
    ],
  },
  // ===== 专家端 =====
  {
    path: '/expert',
    component: layout,
    redirect: '/expert/workbench',
    meta: { roles: ['expert'] },
    children: [
      { path: 'workbench', component: () => import('../views/expert/ExpertWorkbench.vue'), meta: { title: '工作台' } },
      { path: 'consult', component: () => import('../views/expert/ExpertConsult.vue'), meta: { title: '我的咨询' } },
      { path: 'schedule', component: () => import('../views/expert/ExpertSchedule.vue'), meta: { title: '排班请假' } },
      { path: 'elder', component: () => import('../views/expert/ExpertElder.vue'), meta: { title: '老人健康档案' } },
      { path: 'intervention', component: () => import('../views/expert/ExpertIntervention.vue'), meta: { title: '干预方案建议' } },
      { path: 'profile', component: () => import('../views/expert/ExpertProfile.vue'), meta: { title: '个人中心' } },
    ],
  },
  // ===== 陪护端 =====
  {
    path: '/caregiver',
    component: layout,
    redirect: '/caregiver/workbench',
    meta: { roles: ['caregiver', 'supervisor'] },
    children: [
      { path: 'workbench', component: () => import('../views/caregiver/CaregiverWorkbench.vue'), meta: { title: '工作台' } },
      { path: 'elder', component: () => import('../views/caregiver/CaregiverElder.vue'), meta: { title: '我负责的老人' } },
      { path: 'task', component: () => import('../views/caregiver/CaregiverTask.vue'), meta: { title: '今日任务' } },
      { path: 'chat', component: () => import('../views/caregiver/CaregiverChat.vue'), meta: { title: '联系家属' } },
      { path: 'alert', component: () => import('../views/caregiver/CaregiverAlert.vue'), meta: { title: '告警处置' } },
      { path: 'health', component: () => import('../views/caregiver/CaregiverHealth.vue'), meta: { title: '健康记录' } },
      { path: 'space', component: () => import('../views/caregiver/CaregiverSpace.vue'), meta: { title: '空间巡检' } },
      { path: 'profile', component: () => import('../views/caregiver/CaregiverProfile.vue'), meta: { title: '个人中心' } },
    ],
  },
  // ===== 陪护主管 =====
  {
    path: '/supervisor',
    component: layout,
    redirect: '/supervisor/schedule',
    meta: { roles: ['supervisor'] },
    children: [
      { path: 'schedule', component: () => import('../views/supervisor/SupervisorSchedule.vue'), meta: { title: '排班管理' } },
      { path: 'apply', component: () => import('../views/supervisor/SupervisorApply.vue'), meta: { title: '请假审批' } },
      { path: 'team', component: () => import('../views/supervisor/SupervisorTeam.vue'), meta: { title: '全组视角' } },
    ],
  },
  // ===== 老人端 =====
  {
    path: '/elder',
    component: layout,
    redirect: '/elder/home',
    meta: { roles: ['elder'] },
    children: [
      { path: 'home', component: () => import('../views/elder/ElderHome.vue'), meta: { title: '首页健康概览' } },
      { path: 'consult', component: () => import('../views/elder/ElderConsult.vue'), meta: { title: '在线咨询' } },
      { path: 'caregiver', component: () => import('../views/elder/ElderCaregiverChat.vue'), meta: { title: '联系陪护' } },
      { path: 'relation', component: () => import('../views/elder/ElderRelation.vue'), meta: { title: '我的护工与家人' } },
      { path: 'reminder', component: () => import('../views/elder/ElderReminder.vue'), meta: { title: '用药与日程提醒' } },
      { path: 'profile', component: () => import('../views/elder/ElderProfile.vue'), meta: { title: '个人档案' } },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

// 阶段1：角色以后端 /api/user/profile 返回值为准，localStorage.role 仅作缓存展示，不作为授权依据
let authoritativeRole: string | null = null
let authoritativeToken: string | null = null
let roleFetchPromise: Promise<string | null> | null = null

async function resolveAuthoritativeRole(token: string): Promise<string | null> {
  if (authoritativeToken === token && authoritativeRole) return authoritativeRole
  if (roleFetchPromise) return roleFetchPromise
  roleFetchPromise = (async () => {
    try {
      // 直接使用 fetch，避免通过 request 拦截器在守卫中触发 401 重定向造成死循环
      const resp = await fetch('/api/user/profile', {
        headers: { Authorization: `Bearer ${token}` },
      })
      const body = await resp.json()
      const role = body?.data?.role
      if (resp.ok && body?.code === 200 && typeof role === 'string' && role) {
        authoritativeToken = token
        authoritativeRole = role
        localStorage.setItem('role', role) // 仅刷新展示缓存
        return role
      }
      return null
    } catch {
      return null
    } finally {
      roleFetchPromise = null
    }
  })()
  return roleFetchPromise
}

router.beforeEach(async (to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login') {
    next()
    return
  }
  if (!token) {
    next('/login')
    return
  }
  const role = await resolveAuthoritativeRole(token)
  if (!role) {
    // 权威角色获取失败（token 失效 / 网络异常）→ 退回登录页
    localStorage.removeItem('token')
    localStorage.removeItem('role')
    authoritativeToken = null
    authoritativeRole = null
    next('/login')
    return
  }
  const knownRole = Object.prototype.hasOwnProperty.call(ROLE_HOME, role)
  if (!knownRole) {
    // 待激活/未知角色：无可用端，退回登录页并清理本地登录态
    localStorage.removeItem('token')
    localStorage.removeItem('role')
    authoritativeToken = null
    authoritativeRole = null
    next('/login')
    return
  }
  const home = ROLE_HOME[role]
  if (to.path === '/' || to.path === '/home') {
    next(home)
    return
  }
  // 按路由 meta.roles 拦截越权访问，重定向到本端首页
  const allowed = to.meta?.roles as string[] | undefined
  if (allowed && !allowed.includes(role)) {
    next(home)
    return
  }
  next()
})

export default router
