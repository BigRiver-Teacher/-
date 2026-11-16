<template>
  <div class="layout">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="logo" @click="router.push('/echart')" style="cursor: pointer">
        <el-icon :size="26" color="#1890ff"><Management /></el-icon>
        <h2>朝夕护行</h2>
      </div>
      <nav class="menu">
        <el-menu
          :default-active="defaultActive"
          :default-openeds="defaultOpeneds"
          @select="handleSelect"
          unique-opened
          background-color="#001529"
          text-color="rgba(255,255,255,0.65)"
          active-text-color="#fff"
        >
          <!-- ★ 侧边栏由 useMenuStore 驱动渲染，支持动态排序/显示隐藏/图标切换 -->
          <template v-for="item in menuStore.sidebarItems" :key="item.id">
            <!-- 有 children 的为分组菜单（el-sub-menu） -->
            <el-sub-menu
              v-if="'children' in item"
              :index="item.id"
              :class="{ 'parent-active': activeParentId === item.id }"
            >
              <template #title>
                <el-icon><component :is="item.icon" /></el-icon>
                <span>{{ item.name }}</span>
              </template>
              <el-menu-item v-for="child in item.children" :key="child.id" :index="child.id">
                <el-icon><component :is="child.icon" /></el-icon>
                <span>{{ child.name }}</span>
              </el-menu-item>
            </el-sub-menu>
            <!-- 无 children 的为独立菜单项 -->
            <el-menu-item v-else :index="item.id">
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.name }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </nav>
    </aside>

    <!-- 主内容 -->
    <main class="main">
      <header class="header">
        <div class="header-left">
          <div class="breadcrumb-wrapper">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/home' }">
                <el-icon><HomeFilled /></el-icon>
                <span>首页</span>
              </el-breadcrumb-item>
              <el-breadcrumb-item v-if="breadcrumbParent">{{ breadcrumbParent }}</el-breadcrumb-item>
              <el-breadcrumb-item>
                <span class="breadcrumb-current">{{ breadcrumbCurrent }}</span>
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>
        </div>
        <div class="header-right">
          <!-- 搜索 -->
          <div ref="searchContainer" class="search-wrapper">
            <el-icon :size="20" @click="toggleSearch" class="header-icon">
              <Search />
            </el-icon>
            <div v-if="searchVisible" class="search-dropdown">
              <div class="search-input-wrap">
                <el-icon><Search /></el-icon>
                <el-input
                  ref="searchInput"
                  v-model="searchKeyword"
                  placeholder="搜索菜单..."
                  size="small"
                  @input="handleSearch"
                  clearable
                />
                <el-icon class="close-search" @click="searchVisible = false">
                  <Close />
                </el-icon>
              </div>
              <div v-if="searchResults.length > 0" class="search-results">
                <div
                  v-for="item in searchResults"
                  :key="item.index"
                  class="search-result-item"
                  @click="selectSearchResult(item)"
                >
                  <span class="result-name">{{ item.name }}</span>
                  <span class="result-path">{{ item.path }}</span>
                </div>
              </div>
              <div v-else-if="searchKeyword && searchResults.length === 0" class="search-empty">
                未找到相关菜单
              </div>
            </div>
          </div>

          <!-- 邮件 -->
          <div class="mail-wrapper" ref="mailWrapperRef">
            <el-icon :size="20" @click.stop="toggleMail" class="header-icon">
              <Message />
            </el-icon>
            <el-badge v-if="mailCount > 0" :value="mailCount" class="mail-badge" />
            <Transition name="mail">
              <div v-show="mailVisible" class="mail-dropdown">
                <div class="mail-header">
                  <span class="mail-title">邮件通知</span>
                  <el-button type="primary" link size="small" @click="markAllRead">
                    全部已读
                  </el-button>
                </div>
                <div class="mail-list">
                  <div
                    v-for="mail in mailList"
                    :key="mail.id"
                    class="mail-item"
                    :class="{ unread: !mail.read }"
                    @click="markMailRead(mail.id)"
                  >
                    <div class="mail-item-title">{{ mail.title }}</div>
                    <div class="mail-item-content">{{ mail.content }}</div>
                    <div class="mail-item-time">{{ mail.time }}</div>
                    <div v-if="!mail.read" class="mail-dot"></div>
                  </div>
                </div>
                <div class="mail-footer">
                  <el-button type="primary" link size="small" @click="mailVisible = false">
                    关闭
                  </el-button>
                </div>
              </div>
            </Transition>
          </div>

          <!-- 铃铛 - 跳转个人主页并展开通知 -->
          <el-icon :size="20" @click="goToProfileWithNotice" class="header-icon">
            <Bell />
          </el-icon>

          <el-divider direction="vertical" />

          <!-- 用户下拉菜单 -->
          <el-dropdown trigger="click" @command="handleUserCommand" class="user-dropdown">
            <span class="user-info">
              <el-avatar :size="32" :src="user_pic" />
              <span class="admin-name">管理员</span>
              <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><Avatar /></el-icon>
                  <span>个人中心</span>
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>
                  <span>账号设置</span>
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><Close /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 标签页栏 -->
      <div v-if="tabs.length > 0" class="tabs-bar">
        <div ref="tabsContainerRef" class="tabs-container">
          <div
            v-for="tab in tabs"
            :key="tab.path"
            class="tab-item"
            :class="{ active: activeTab === tab.path }"
            @click="switchTab(tab.path)"
          >
            <span class="tab-name">{{ tab.name }}</span>
            <el-icon class="tab-close" @click.stop="removeTab(tab.path)">
              <Close />
            </el-icon>
          </div>
        </div>
        <div class="tabs-actions">
          <el-dropdown trigger="click" size="small">
            <el-button size="small" type="primary" plain>
              <el-icon style="width: 30px"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="closeOtherTabs(activeTab)">关闭其他</el-dropdown-item>
                <el-dropdown-item @click="closeAllTabs">关闭所有</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 内容区域 -->
      <section class="content">
        <RouterView v-slot="{ Component }">
          <keep-alive :max="10">
            <component :is="Component" />
          </keep-alive>
        </RouterView>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import {
  HomeFilled,
  Warning,
  User,
  Files,
  List,
  Notebook,
  Edit,
  DataAnalysis,
  Histogram,
  TrendCharts,
  Location,
  Monitor,
  Connection,
  Document,
  Headset,
  UserFilled,
  Avatar,
  Bell,
  Share,
  Setting,
  House,
  Tools,
  Calendar,
  Operation,
  Grid,
  Menu,
  Management,
  Message,
  Search,
  Close,
  ArrowDown,
} from '@element-plus/icons-vue'
import { RouterView, useRouter, useRoute } from 'vue-router'
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { useMenuStore } from '@/stores/menuStore'
import { useNotificationStore } from '@/stores/notificationStore'

const router = useRouter()
const route = useRoute()

// ===== 菜单 Store（驱动侧边栏动态渲染） =====
const menuStore = useMenuStore()
// ===== 通知 Store（邮件下拉数据） =====
const notificationStore = useNotificationStore()

// ===== 路由映射 =====
const routeMap = {
  // ✅ 添加 /home 的映射（用于处理直接访问 /home）
  1: '/home/management',
  2: '/home/warning',
  '3-1': '/home/elder/base',
  '3-2': '/home/elder/gait',
  '3-3': '/home/elder/fall-record',
  '3-4': '/home/elder/health-note',
  6: '/home/device',
  7: '/home/expert',
  8: '/home/caregiver',
  9: '/home/profile',
  '10-1': '/home/alert/list',
  '10-2': '/home/alert/push-log',
  '10-3': '/home/alert/community',
  '10-4': '/home/alert/preference',
  11: '/home/space',
  12: '/home/intervention',
  13: '/home/schedule',
  '14-1': '/home/system/permission',
  '14-2': '/home/system/log',
  '14-3': '/home/system/backup',
  '14-4': '/home/system/model',
  '15-1': '/home/custom/sidebar',
  '15-2': '/home/custom/dashboard',
  '15-3': '/home/custom/mobile',
}

// ===== 反向路由映射 =====
const pathToIndex = {}
Object.entries(routeMap).forEach(([index, path]) => {
  pathToIndex[path] = index
})
// ✅ 添加 /home 的反向映射
pathToIndex['/home'] = '1'

// ===== 菜单名称映射 =====
const menuNames = {
  1: '管理主页',
  2: '跌倒预警',
  '3-1': '基础档案',
  '3-2': '步态历史',
  '3-3': '既往跌倒记录',
  '3-4': '健康备注',
  6: '传感设备管理',
  7: '专家康养咨询',
  8: '陪护人员管理',
  9: '个人主页',
  '10-1': '实时告警列表',
  '10-2': '家属推送日志',
  '10-3': '社区处理台账',
  '10-4': '通知偏好设置',
  11: '居家空间档案',
  12: '干预方案管理',
  13: '日常康养日程',
  '14-1': '账号权限',
  '14-2': '操作日志',
  '14-3': '数据备份',
  '14-4': '模型参数设置',
  '15-1': '侧边栏自定义',
  '15-2': '首页看板布局',
  '15-3': '移动端导航设置',
}

// ===== 当前选中的菜单项 =====
const defaultActive = ref('1')

// ===== 当前激活项所属的父级菜单 ID（用于父级标题高亮） =====
// 当选中二级菜单时，其所属的一级菜单保持激活态颜色
const activeParentId = computed(() => {
  const idx = defaultActive.value
  if (idx && idx.includes('-')) {
    return idx.split('-')[0]
  }
  return null
})

// ===== 面包屑导航 =====
const breadcrumbCurrent = computed(() => {
  const matchedIndex = pathToIndex[route.path]
  return matchedIndex ? (menuNames[matchedIndex] || '') : ''
})

const breadcrumbParent = computed(() => {
  const idx = defaultActive.value
  if (idx && idx.includes('-')) {
    const parentId = idx.split('-')[0]
    const parentGroup = menuStore.sidebarItems.find(
      (item) => 'children' in item && item.id === parentId
    ) as any
    return parentGroup ? parentGroup.name : ''
  }
  return ''
})

// ===== 用户下拉菜单 =====
const handleUserCommand = (command: string) => {
  switch (command) {
    case 'profile':
      goToProfile()
      break
    case 'settings':
      router.push('/home/system/permission')
      break
    case 'logout':
      router.push('/login')
      break
  }
}

// ===== 打开标签页列表 =====
const tabs = ref([])
const activeTab = ref('')
const tabsContainerRef = ref(null)

// ===== 默认展开的子菜单 =====
const defaultOpeneds = ref(['3', '10', '14', '15'])
const user_pic = 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'

// ✅ 修复：更新活跃菜单
const updateActiveMenu = () => {
  const currentPath = route.path

  // ✅ 特殊处理 /home，默认选中管理主页
  if (currentPath === '/home' || currentPath === '/home/') {
    defaultActive.value = '1'
    // 如果管理主页标签不存在，添加它
    const exists = tabs.value.some((tab) => tab.path === '/home/management')
    if (!exists) {
      tabs.value.push({
        index: '1',
        path: '/home/management',
        name: '管理主页',
      })
    }
    if (activeTab.value !== '/home/management') {
      activeTab.value = '/home/management'
    }
    return
  }

  const matchedIndex = pathToIndex[currentPath]
  if (matchedIndex) {
    defaultActive.value = matchedIndex
    // 只有二级菜单项（如 '3-1'）才需要展开其父级子菜单
    if (matchedIndex.includes('-')) {
      const parentIndex = matchedIndex.split('-')[0]
      if (!defaultOpeneds.value.includes(parentIndex)) {
        defaultOpeneds.value.push(parentIndex)
      }
    }
    addTab(matchedIndex)
  }
}

// ===== 添加标签页 =====
function addTab(index) {
  const path = routeMap[index]
  const name = menuNames[index] || index
  const exists = tabs.value.some((tab) => tab.path === path)
  if (!exists) {
    tabs.value.push({
      index: index,
      path: path,
      name: name,
    })
  }
  activeTab.value = path
}

// ===== 移除标签页 =====
function removeTab(path) {
  const index = tabs.value.findIndex((tab) => tab.path === path)
  if (index > -1) {
    tabs.value.splice(index, 1)
    if (tabs.value.length === 0) {
      router.push('/home/management')
      activeTab.value = ''
    } else if (activeTab.value === path) {
      const newIndex = Math.min(index, tabs.value.length - 1)
      const tab = tabs.value[newIndex]
      activeTab.value = tab.path
      router.push(tab.path)
    }
  }
}

// ===== 切换标签页 =====
function switchTab(path) {
  if (path !== activeTab.value) {
    activeTab.value = path
    router.push(path)
  }
}

// ===== 关闭其他标签 =====
function closeOtherTabs(path) {
  tabs.value = tabs.value.filter((tab) => tab.path === path)
  activeTab.value = path
}

// ===== 关闭所有标签 =====
function closeAllTabs() {
  tabs.value = []
  activeTab.value = ''
  router.push('/home/management')
}

// ===== 监听路由变化 =====
watch(
  () => route.path,
  () => {
    updateActiveMenu()
  },
  { immediate: true },
)

watch(activeTab, async () => {
  await nextTick()
  const activeEl = tabsContainerRef.value?.querySelector('.tab-item.active')
  activeEl?.scrollIntoView({ behavior: 'smooth', inline: 'center' })
})

// ===== 菜单点击跳转 =====
const handleSelect = (index) => {
  const path = routeMap[index]
  if (path) {
    const name = menuNames[index] || index
    const exists = tabs.value.some((tab) => tab.path === path)
    if (!exists) {
      tabs.value.push({
        index: index,
        path: path,
        name: name,
      })
    }
    activeTab.value = path
    router.push(path)
  }
}

// ===== 搜索功能 =====
const searchVisible = ref(false)
const searchKeyword = ref('')
const searchResults = ref([])
const searchInput = ref(null)

const allMenuItems = [
  { index: '1', name: '管理主页', path: '/home/management' },
  { index: '2', name: '跌倒预警', path: '/home/warning' },
  { index: '3-1', name: '基础档案', path: '/home/elder/base' },
  { index: '3-2', name: '步态历史', path: '/home/elder/gait' },
  { index: '3-3', name: '既往跌倒记录', path: '/home/elder/fall-record' },
  { index: '3-4', name: '健康备注', path: '/home/elder/health-note' },
  { index: '6', name: '传感设备管理', path: '/home/device' },
  { index: '7', name: '专家康养咨询', path: '/home/expert' },
  { index: '8', name: '陪护人员管理', path: '/home/caregiver' },
  { index: '9', name: '个人主页', path: '/home/profile' },
  { index: '10-1', name: '实时告警列表', path: '/home/alert/list' },
  { index: '10-2', name: '家属推送日志', path: '/home/alert/push-log' },
  { index: '10-3', name: '社区处理台账', path: '/home/alert/community' },
  { index: '10-4', name: '通知偏好设置', path: '/home/alert/preference' },
  { index: '11', name: '居家空间档案', path: '/home/space' },
  { index: '12', name: '干预方案管理', path: '/home/intervention' },
  { index: '13', name: '日常康养日程', path: '/home/schedule' },
  { index: '14-1', name: '账号权限', path: '/home/system/permission' },
  { index: '14-2', name: '操作日志', path: '/home/system/log' },
  { index: '14-3', name: '数据备份', path: '/home/system/backup' },
  { index: '14-4', name: '模型参数设置', path: '/home/system/model' },
  { index: '15-1', name: '侧边栏自定义', path: '/home/custom/sidebar' },
  { index: '15-2', name: '首页看板布局', path: '/home/custom/dashboard' },
  { index: '15-3', name: '移动端导航设置', path: '/home/custom/mobile' },
]

function toggleSearch() {
  searchVisible.value = !searchVisible.value
  if (searchVisible.value) {
    searchKeyword.value = ''
    searchResults.value = []
    setTimeout(() => {
      searchInput.value?.focus()
    }, 100)
  }
}

function handleSearch() {
  if (!searchKeyword.value.trim()) {
    searchResults.value = []
    return
  }
  const keyword = searchKeyword.value.trim().toLowerCase()
  searchResults.value = allMenuItems.filter((item) => item.name.includes(keyword))
}

function selectSearchResult(item) {
  const exists = tabs.value.some((tab) => tab.path === item.path)
  if (!exists) {
    tabs.value.push({
      index: item.index,
      path: item.path,
      name: item.name,
    })
  }
  activeTab.value = item.path
  searchVisible.value = false
  searchKeyword.value = ''
  searchResults.value = []
  router.push(item.path)
}

// ===== 邮件通知（来自 notificationStore） =====
const mailVisible = ref(false)
const mailWrapperRef = ref(null)
const mailCount = computed(() => notificationStore.unreadCount)
const mailList = computed(() => notificationStore.notifications.slice(0, 10))

function toggleMail() {
  mailVisible.value = !mailVisible.value
}

function markMailRead(id: number) {
  notificationStore.markRead(id)
}

function markAllRead() {
  notificationStore.markAllRead()
  ElMessage.success('已全部标记为已读')
}

function goToProfile() {
  const path = '/home/profile'
  const exists = tabs.value.some((tab) => tab.path === path)
  if (!exists) {
    tabs.value.push({
      index: '9',
      path: path,
      name: '个人主页',
    })
  }
  activeTab.value = path
  router.push(path)
}

function goToProfileWithNotice() {
  const path = '/home/profile'
  const exists = tabs.value.some((tab) => tab.path === path)
  if (!exists) {
    tabs.value.push({
      index: '9',
      path: path,
      name: '个人主页',
    })
  }
  activeTab.value = path
  router.push({ path: path, state: { expandNotice: true } })
}

// ===== 搜索容器 =====
const searchContainer = ref(null)

// ===== 点击外部关闭邮件下拉 =====
const handleClickOutside = (event) => {
  if (mailVisible.value && mailWrapperRef.value) {
    const isInside = mailWrapperRef.value.contains(event.target)
    if (!isInside) {
      mailVisible.value = false
    }
  }
}

// ✅ 修复：组件挂载
onMounted(() => {
  // 加载菜单配置（从 localStorage 恢复，驱动侧边栏渲染）
  menuStore.loadConfig()
  notificationStore.fetchNotifications({ pageNum: 1, pageSize: 10 })
  notificationStore.fetchUnreadCount()
  const currentPath = route.path

  // ✅ 如果当前路径是 /home，默认打开管理主页
  if (currentPath === '/home' || currentPath === '/home/') {
    const name = '管理主页'
    const exists = tabs.value.some((tab) => tab.path === '/home/management')
    if (!exists) {
      tabs.value.push({
        index: '1',
        path: '/home/management',
        name: name,
      })
    }
    activeTab.value = '/home/management'
    defaultActive.value = '1'
  } else {
    const matchedIndex = pathToIndex[currentPath]
    if (matchedIndex) {
      const name = menuNames[matchedIndex] || matchedIndex
      const exists = tabs.value.some((tab) => tab.path === currentPath)
      if (!exists) {
        tabs.value.push({
          index: matchedIndex,
          path: currentPath,
          name: name,
        })
      }
      activeTab.value = currentPath
      defaultActive.value = matchedIndex
    }
  }
  document.addEventListener('click', handleClickOutside)
})

// ===== 组件卸载 =====
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
/* ===== 布局容器 ===== */
.layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background-color: #f0f2f5;
}

/* ================================================================
   侧边栏 — 深色主题（参考 Ant Design Pro / vue-vben-admin）
   ================================================================ */
.sidebar {
  width: 220px;
  min-width: 220px;
  height: 100vh;
  background: #001529;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
  z-index: 100;
}

/* --- Logo 区域 --- */
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 18px 20px;
  flex-shrink: 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  transition: all var(--transition-normal, 0.3s ease);
}

.logo h2 {
  margin: 0;
  font-size: 19px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 2px;
  white-space: nowrap;
}

.sidebar .menu {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

/* ================================================================
   菜单样式 — 深色主题
   ================================================================ */
:deep(.el-menu) {
  border-right: none !important;
  background-color: transparent !important;
}

/* --- 菜单项默认状态 --- */
:deep(.el-menu-item) {
  height: 44px;
  line-height: 44px;
  margin: 2px 8px;
  border-radius: 6px;
  color: rgba(255, 255, 255, 0.65) !important;
  font-size: 14px;
  transition: all 0.25s ease;
  position: relative;
}

:deep(.el-sub-menu .el-sub-menu__title) {
  height: 44px;
  line-height: 44px;
  margin: 2px 8px;
  border-radius: 6px;
  color: rgba(255, 255, 255, 0.65) !important;
  font-size: 14px;
  transition: all 0.25s ease;
}

/* --- 菜单项悬停 --- */
:deep(.el-menu-item:hover),
:deep(.el-sub-menu .el-sub-menu__title:hover) {
  background-color: rgba(255, 255, 255, 0.08) !important;
  color: rgba(255, 255, 255, 0.9) !important;
}

/* --- 一级菜单激活态（独立菜单项）：#1890ff 文字加粗，深蓝底 + 左侧指示条 --- */
:deep(.el-menu-item.is-active) {
  color: #fff !important;
  font-weight: 600;
  background: linear-gradient(90deg, rgba(24, 144, 255, 0.35) 0%, rgba(24, 144, 255, 0.12) 100%) !important;
}

:deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: #1890ff;
  border-radius: 0 3px 3px 0;
}

/* --- 子菜单父级标题激活态（当子项被选中时） --- */
:deep(.el-sub-menu.parent-active > .el-sub-menu__title) {
  color: #fff !important;
  font-weight: 600;
  background: linear-gradient(90deg, rgba(24, 144, 255, 0.35) 0%, rgba(24, 144, 255, 0.12) 100%) !important;
}

:deep(.el-sub-menu.parent-active > .el-sub-menu__title::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: #1890ff;
  border-radius: 0 3px 3px 0;
}

/* --- 子菜单展开箭头颜色 --- */
:deep(.el-sub-menu .el-sub-menu__icon-arrow) {
  color: rgba(255, 255, 255, 0.4);
  transition: all 0.25s ease;
}

:deep(.el-sub-menu.parent-active .el-sub-menu__icon-arrow) {
  color: #1890ff;
}

/* --- 二级菜单项 --- */
:deep(.el-menu--inline) {
  background: rgba(0, 0, 0, 0.15) !important;
  border-radius: 0 0 6px 6px;
  margin: 0 8px;
}

:deep(.el-menu--inline .el-menu-item) {
  padding-left: 52px !important;
  height: 40px;
  line-height: 40px;
  margin: 1px 0;
  font-size: 13px;
  border-radius: 4px;
}

/* --- 二级菜单激活态：#40a9ff，略浅，不加粗 --- */
:deep(.el-menu--inline .el-menu-item.is-active) {
  color: #40a9ff !important;
  font-weight: 400;
  background: rgba(24, 144, 255, 0.08) !important;
}

:deep(.el-menu--inline .el-menu-item.is-active::before) {
  display: none;
}

/* --- 二级菜单悬停 --- */
:deep(.el-menu--inline .el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.06) !important;
  color: rgba(255, 255, 255, 0.85) !important;
}

/* ===== 侧边栏滚动条 ===== */
.sidebar .menu::-webkit-scrollbar {
  width: 4px;
}

.sidebar .menu::-webkit-scrollbar-thumb {
  background-color: rgba(255, 255, 255, 0.12);
  border-radius: 4px;
}

.sidebar .menu::-webkit-scrollbar-track {
  background-color: transparent;
}

/* ================================================================
   主内容区域
   ================================================================ */
.main {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background-color: #f0f2f5;
}

/* ================================================================
   Header — 白色背景 + 微妙阴影
   ================================================================ */
.header {
  height: 60px;
  min-height: 60px;
  background: #fff;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  flex-shrink: 0;
  z-index: 50;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* --- 面包屑 --- */
.breadcrumb-wrapper :deep(.el-breadcrumb) {
  font-size: 14px;
}

.breadcrumb-wrapper :deep(.el-breadcrumb__item) {
  display: flex;
  align-items: center;
}

.breadcrumb-wrapper :deep(.el-breadcrumb__inner) {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #666;
  font-weight: 400;
  transition: color 0.2s;
}

.breadcrumb-wrapper :deep(.el-breadcrumb__inner:hover) {
  color: #1890ff;
}

.breadcrumb-wrapper :deep(.el-breadcrumb__separator) {
  color: #ccc;
  margin: 0 6px;
}

.breadcrumb-current {
  color: #333;
  font-weight: 500;
}

/* --- Header 右侧 --- */
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  color: #666;
  font-size: 14px;
  position: relative;
}

.header-icon {
  cursor: pointer;
  font-size: 20px;
  color: #666;
  transition: color 0.2s;
}

.header-icon:hover {
  color: #1890ff;
}

/* --- 用户下拉 --- */
.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.2s;
}

.user-info:hover {
  background: #f5f5f5;
}

.admin-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  user-select: none;
}

.dropdown-arrow {
  font-size: 12px;
  color: #999;
  transition: transform 0.2s;
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

/* ================================================================
   搜索下拉
   ================================================================ */
.search-wrapper {
  position: relative;
}

.search-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: -50px;
  width: 360px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  padding: 12px 16px;
  z-index: 1000;
}

.search-input-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  border-bottom: 1px solid #e8e8e8;
  padding-bottom: 8px;
}

.search-input-wrap .el-icon {
  color: #999;
}

.search-input-wrap :deep(.el-input) {
  flex: 1;
}

.search-input-wrap :deep(.el-input__wrapper) {
  box-shadow: none !important;
  padding: 0;
}

.search-input-wrap :deep(.el-input__inner) {
  font-size: 14px;
}

.close-search {
  cursor: pointer;
  color: #999;
  transition: color 0.2s;
}

.close-search:hover {
  color: #333;
}

.search-results {
  max-height: 300px;
  overflow-y: auto;
  margin-top: 8px;
}

.search-result-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}

.search-result-item:hover {
  background: #f0f7ff;
}

.result-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.result-path {
  font-size: 12px;
  color: #999;
}

.search-empty {
  padding: 16px 0;
  text-align: center;
  color: #999;
  font-size: 14px;
}

/* ================================================================
   邮件通知
   ================================================================ */
.mail-wrapper {
  position: relative;
}

.mail-badge {
  position: absolute;
  top: -6px;
  right: -10px;
}

:deep(.mail-badge .el-badge__content) {
  font-size: 10px;
  padding: 0 5px;
  height: 18px;
  line-height: 18px;
  background-color: #f56c6c;
}

.mail-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: -80px;
  width: 380px;
  max-height: 420px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  z-index: 1000;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.mail-enter-active,
.mail-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.mail-enter-from,
.mail-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.mail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.mail-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.mail-list {
  flex: 1;
  overflow-y: auto;
  padding: 4px 0;
}

.mail-item {
  padding: 10px 16px;
  cursor: pointer;
  transition: background 0.2s;
  position: relative;
  border-bottom: 1px solid #f5f5f5;
}

.mail-item:hover {
  background: #f8f9fa;
}

.mail-item.unread {
  background: #f0f7ff;
}

.mail-item.unread:hover {
  background: #e6f0ff;
}

.mail-item-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.mail-item.unread .mail-item-title {
  font-weight: 600;
}

.mail-item-content {
  font-size: 13px;
  color: #666;
  margin-top: 2px;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.mail-item-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.mail-dot {
  position: absolute;
  top: 14px;
  right: 16px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #1890ff;
}

.mail-footer {
  padding: 8px 16px;
  border-top: 1px solid #f0f0f0;
  text-align: center;
  flex-shrink: 0;
}

/* ================================================================
   标签页栏 — 现代化设计
   ================================================================ */
.tabs-bar {
  display: flex;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  padding: 0 12px;
  min-height: 38px;
  flex-shrink: 0;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.tabs-container {
  display: flex;
  flex: 1;
  overflow-x: auto;
  gap: 4px;
  padding: 5px 0;
}

.tabs-container::-webkit-scrollbar {
  height: 2px;
}

.tabs-container::-webkit-scrollbar-thumb {
  background: #d0d0d0;
  border-radius: 2px;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 14px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  color: #666;
  white-space: nowrap;
  transition: all 0.2s ease;
  background: transparent;
  border: 1px solid transparent;
  flex-shrink: 0;
}

.tab-item:hover {
  background: #f5f5f5;
  color: #333;
}

.tab-item.active {
  background: #e6f4ff;
  color: #1890ff;
  font-weight: 500;
  border-color: rgba(24, 144, 255, 0.2);
}

.tab-name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tab-close {
  font-size: 13px;
  color: #bbb;
  border-radius: 3px;
  padding: 1px;
  transition: all 0.15s;
  cursor: pointer;
}

.tab-close:hover {
  background: rgba(0, 0, 0, 0.08);
  color: #666;
}

.tab-item.active .tab-close:hover {
  color: #1890ff;
}

.tabs-actions {
  flex-shrink: 0;
  padding-left: 8px;
  margin-left: 4px;
  border-left: 1px solid #f0f0f0;
}

/* ================================================================
   内容区域
   ================================================================ */
.content {
  flex: 1;
  padding: 16px 20px;
  overflow-y: auto;
  background-color: #f0f2f5;
}

/* ===== 内容区域滚动条 ===== */
.content::-webkit-scrollbar {
  width: 6px;
}

.content::-webkit-scrollbar-thumb {
  background-color: #d0d0d0;
  border-radius: 4px;
}

.content::-webkit-scrollbar-track {
  background-color: transparent;
}

/* ================================================================
   响应式
   ================================================================ */
@media (max-width: 768px) {
  .sidebar {
    width: 200px;
    min-width: 200px;
  }

  .search-dropdown {
    width: 300px;
    right: -30px;
  }

  .mail-dropdown {
    width: 320px;
    right: -60px;
  }
}

@media (max-width: 480px) {
  .sidebar {
    display: none;
  }

  .header {
    padding: 0 12px;
  }

  .content {
    padding: 12px;
  }

  .search-dropdown {
    width: 280px;
    right: -20px;
  }

  .mail-dropdown {
    width: 280px;
    right: -40px;
  }

  .tab-name {
    max-width: 60px;
  }

  .tabs-bar {
    padding: 0 8px;
  }
}
</style>
