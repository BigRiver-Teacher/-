// router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import layout from '../views/layout.vue'
//RouterRecordRaw 是路由记录的类型，表示一个路由对象的结构。它包含了路由的路径、组件、子路由等信息。

//路由配置routes是一个数组，包含了所有的路由记录。每个路由记录都是一个对象，包含了path,component,redirect,children等属性。路由记录可以嵌套，形成多级路由结构。
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/login',
  },
  {
    path: '/login',
    component: () => import('../views/loginNew.vue'),
  },
  {
    path: '/echart',
    redirect: '/echart/monitor',
  },
  {
    path: '/echart/monitor',
    component: () => import('../views/Echart-monitor.vue'),
  },
  {
    path: '/echart/device',
    component: () => import('../views/Echart-device.vue'),
  },
  {
    path: '/echart/person',
    component: () => import('../views/Echart-person.vue'),
  },
  {
    path: '/echart/export',
    component: () => import('../views/Echart-export.vue'),
  },
  {
    path: '/home',
    component: layout,
    redirect: '/home/management',
    children: [
      // ===== 1. 管理主页 =====
      {
        path: 'management',
        component: () => import('../views/1management.vue'),
        meta: { title: '管理主页' },
      },
      // ===== 2. 跌倒预警 =====
      {
        path: 'warning',
        component: () => import('../views/2warning.vue'),
        meta: { title: '跌倒预警' },
      },
      // ===== 3. 老人详情 =====
      {
        path: 'elder/base',
        component: () => import('../views/3elder/3-1base.vue'),
        meta: { title: '基础档案' },
      },
      {
        path: 'elder/gait',
        component: () => import('../views/3elder/3-2gait.vue'),
        meta: { title: '步态历史' },
      },
      {
        path: 'elder/fall-record',
        component: () => import('../views/3elder/3-3fall-record.vue'),
        meta: { title: '既往跌倒记录' },
      },
      {
        path: 'elder/health-note',
        component: () => import('../views/3elder/3-4health-note.vue'),
        meta: { title: '健康备注' },
      },
      // ===== 6. 传感设备管理 =====
      {
        path: 'device',
        component: () => import('../views/6device.vue'),
        meta: { title: '传感设备管理' },
      },
      // ===== 7. 专家康养咨询 =====
      {
        path: 'expert',
        component: () => import('../views/7expert.vue'),
        meta: { title: '专家康养咨询' },
      },
      // ===== 8. 陪护人员管理 =====
      {
        path: 'caregiver',
        component: () => import('../views/8caregiver.vue'),
        meta: { title: '陪护人员管理' },
      },
      // ===== 9. 个人主页 =====
      {
        path: 'profile',
        component: () => import('../views/9profile.vue'),
        meta: { title: '个人主页' },
      },
      // ===== 10. 告警信息中心 =====
      {
        path: 'alert/list',
        component: () => import('../views/10alert/10-1list.vue'),
        meta: { title: '实时告警列表' },
      },
      {
        path: 'alert/push-log',
        component: () => import('../views/10alert/10-2push-log.vue'),
        meta: { title: '家属推送日志' },
      },
      {
        path: 'alert/community',
        component: () => import('../views/10alert/10-3community.vue'),
        meta: { title: '社区处理台账' },
      },
      {
        path: 'alert/preference',
        component: () => import('../views/10alert/10-4preference.vue'),
        meta: { title: '通知偏好设置' },
      },
      // ===== 11. 居家空间档案 =====
      {
        path: 'space',
        component: () => import('../views/11space.vue'),
        meta: { title: '居家空间档案' },
      },
      // ===== 12. 干预方案管理 =====
      {
        path: 'intervention',
        component: () => import('../views/12intervention.vue'),
        meta: { title: '干预方案管理' },
      },
      // ===== 13. 日常康养日程 =====
      {
        path: 'schedule',
        component: () => import('../views/13schedule.vue'),
        meta: { title: '日常康养日程' },
      },
      // ===== 14. 系统管理 =====
      {
        path: 'system/permission',
        component: () => import('../views/14system/14-1permission.vue'),
        meta: { title: '账号权限' },
      },
      {
        path: 'system/log',
        component: () => import('../views/14system/14-2log.vue'),
        meta: { title: '操作日志' },
      },
      {
        path: 'system/backup',
        component: () => import('../views/14system/14-3backup.vue'),
        meta: { title: '数据备份' },
      },
      {
        path: 'system/model',
        component: () => import('../views/14system/14-4model.vue'),
        meta: { title: '模型参数设置' },
      },
      // ===== 15. 组件&功能 =====
      {
        path: 'custom/sidebar',
        component: () => import('../views/15custom/15-1sidebar.vue'),
        meta: { title: '侧边栏自定义' },
      },
      {
        path: 'custom/dashboard',
        component: () => import('../views/15custom/15-2dashboard.vue'),
        meta: { title: '首页看板布局' },
      },
      {
        path: 'custom/mobile',
        component: () => import('../views/15custom/15-3mobile.vue'),
        meta: { title: '移动端导航设置' },
      },
    ],
  },
]
//创建router实例，使用createRouter函数，并传入一个配置对象。配置对象包含两个属性：history和routes。history属性指定路由的历史模式，这里使用createWebHistory函数创建一个HTML5历史模式的路由实例。routes属性指定路由的配置数组，这里使用上面定义的routes数组。
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})
//导出router实例，以便在其他模块中使用。通常在main.ts中引入router实例，并通过app.use(router)将其注册到vue应用中，从而启用路由功能。
export default router
