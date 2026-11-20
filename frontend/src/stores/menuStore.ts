// stores/menuStore.ts — 侧边栏菜单配置 Store
// 管理菜单列表、图标、顺序、显示/隐藏，持久化到 localStorage

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getMenus, type BackendMenuItem } from '@/api/menu'

// ===== 类型定义 =====
export interface MenuItem {
  id: string // 菜单唯一标识，对应路由映射 key（如 "1", "3-1"）
  name: string // 菜单显示名称
  icon: string // Element Plus 图标组件名（字符串，如 "HomeFilled"）
  route: string // 路由路径（如 "/home/management"）
  visible: boolean // 是否在侧边栏显示
  order: number // 排序序号
  parentId: string | null // 父级菜单 ID（null 表示顶级，非 null 表示子菜单项）
}

export interface MenuGroup {
  id: string // 父级菜单 ID
  name: string // 父级菜单名称（如 "老人详情"）
  icon: string // 父级图标
  order: number // 排序序号
  visible: boolean // 是否显示（如果所有子项都隐藏，父级也不显示）
  children: MenuItem[] // 子菜单项
}

// ===== 默认菜单配置（保持与原 Layout.vue 硬编码完全一致） =====
const defaultMenuItems: MenuItem[] = [
  // 顶级菜单项（无父级）
  { id: '1', name: '管理主页', icon: 'HomeFilled', route: '/home/management', visible: true, order: 1, parentId: null },
  { id: '2', name: '跌倒预警', icon: 'Warning', route: '/home/warning', visible: true, order: 2, parentId: null },
  // 老人详情子菜单
  { id: '3-1', name: '基础档案', icon: 'Files', route: '/home/elder/base', visible: true, order: 3, parentId: '3' },
  { id: '3-2', name: '步态历史', icon: 'List', route: '/home/elder/gait', visible: true, order: 4, parentId: '3' },
  { id: '3-3', name: '既往跌倒记录', icon: 'Notebook', route: '/home/elder/fall-record', visible: true, order: 5, parentId: '3' },
  { id: '3-4', name: '健康备注', icon: 'Edit', route: '/home/elder/health-note', visible: true, order: 6, parentId: '3' },
  // 顶级菜单项
  { id: '6', name: '传感设备管理', icon: 'Connection', route: '/home/device', visible: true, order: 7, parentId: null },
  { id: '7', name: '专家康养咨询', icon: 'Headset', route: '/home/expert', visible: true, order: 8, parentId: null },
  { id: '8', name: '陪护人员管理', icon: 'UserFilled', route: '/home/caregiver', visible: true, order: 9, parentId: null },
  { id: '9', name: '个人主页', icon: 'Avatar', route: '/home/profile', visible: true, order: 10, parentId: null },
  // 告警信息中心子菜单
  { id: '10-1', name: '实时告警列表', icon: 'List', route: '/home/alert/list', visible: true, order: 11, parentId: '10' },
  { id: '10-2', name: '家属推送日志', icon: 'Share', route: '/home/alert/push-log', visible: true, order: 12, parentId: '10' },
  { id: '10-3', name: '社区处理台账', icon: 'Files', route: '/home/alert/community', visible: true, order: 13, parentId: '10' },
  { id: '10-4', name: '通知偏好设置', icon: 'Setting', route: '/home/alert/preference', visible: true, order: 14, parentId: '10' },
  // 顶级菜单项
  { id: '11', name: '居家空间档案', icon: 'House', route: '/home/space', visible: true, order: 15, parentId: null },
  { id: '12', name: '干预方案管理', icon: 'Tools', route: '/home/intervention', visible: true, order: 16, parentId: null },
  { id: '13', name: '日常康养日程', icon: 'Calendar', route: '/home/schedule', visible: true, order: 17, parentId: null },
  // 系统管理子菜单
  { id: '14-1', name: '账号权限', icon: 'User', route: '/home/system/permission', visible: true, order: 18, parentId: '14' },
  { id: '14-2', name: '操作日志', icon: 'Document', route: '/home/system/log', visible: true, order: 19, parentId: '14' },
  { id: '14-3', name: '数据备份', icon: 'Files', route: '/home/system/backup', visible: true, order: 20, parentId: '14' },
  { id: '14-4', name: '模型参数设置', icon: 'Operation', route: '/home/system/model', visible: true, order: 21, parentId: '14' },
  // 组件&功能子菜单
  { id: '15-1', name: '侧边栏自定义', icon: 'Menu', route: '/home/custom/sidebar', visible: true, order: 22, parentId: '15' },
  { id: '15-2', name: '首页看板布局', icon: 'Grid', route: '/home/custom/dashboard', visible: true, order: 23, parentId: '15' },
  { id: '15-3', name: '移动端导航设置', icon: 'Operation', route: '/home/custom/mobile', visible: true, order: 24, parentId: '15' },
]

// ===== 父级分组定义（仅用于配置页面的分组管理，左侧侧边栏不显示分组标题） =====
const defaultParentGroups: Record<string, { name: string; icon: string; order: number }> = {
  '3': { name: '老人详情', icon: 'User', order: 3 },
  '10': { name: '告警信息中心', icon: 'Bell', order: 10 },
  '14': { name: '系统管理', icon: 'Setting', order: 14 },
  '15': { name: '组件&功能', icon: 'Grid', order: 15 },
}

const LOCAL_STORAGE_KEY = 'sidebarMenuConfig'

export const useMenuStore = defineStore('menuStore', () => {
  // ===== State =====
  const menuItems = ref<MenuItem[]>([])
  const parentGroups = ref<Record<string, { name: string; icon: string; order: number }>>({ ...defaultParentGroups })

  // ===== Getters =====
  /** 所有可见且按 order 排序的菜单项（含父子结构） */
  const visibleMenuItems = computed<MenuItem[]>(() => {
    return menuItems.value
      .filter((item) => item.visible)
      .sort((a, b) => a.order - b.order)
  })

  /** 供 Layout.vue 侧边栏渲染的结构化数据 */
  const sidebarMenuStructure = computed<MenuGroup[]>(() => {
    const items = visibleMenuItems.value
    const groups: MenuGroup[] = []
    const standaloneItems: MenuItem[] = []
    const groupMap = new Map<string, MenuItem[]>()

    // 分离独立项和子项
    items.forEach((item) => {
      if (item.parentId === null) {
        standaloneItems.push(item)
      } else {
        if (!groupMap.has(item.parentId)) {
          groupMap.set(item.parentId, [])
        }
        groupMap.get(item.parentId)!.push(item)
      }
    })

    // 构建结果：独立项 + 分组项交替，按 order 排序
    const allEntries: Array<{ type: 'standalone'; item: MenuItem } | { type: 'group'; group: MenuGroup }> = []

    standaloneItems.forEach((item) => {
      allEntries.push({ type: 'standalone', item })
    })

    groupMap.forEach((children, parentId) => {
      const pg = parentGroups.value[parentId]
      if (pg && children.length > 0) {
        allEntries.push({
          type: 'group',
          group: {
            id: parentId,
            name: pg.name,
            icon: pg.icon,
            order: pg.order,
            visible: true,
            children: children.sort((a, b) => a.order - b.order),
          },
        })
      }
    })

    // 按 order 排序
    allEntries.sort((a, b) => {
      const orderA = a.type === 'standalone' ? a.item.order : a.group.order
      const orderB = b.type === 'standalone' ? b.item.order : b.group.order
      return orderA - orderB
    })

    return allEntries
      .filter((e) => e.type === 'group')
      .map((e) => (e as { type: 'group'; group: MenuGroup }).group)
  })

  /** 供侧边栏渲染用的完整列表（独立项 + 分组，按 order 排序） */
  const sidebarItems = computed<(MenuItem | MenuGroup)[]>(() => {
    const items = visibleMenuItems.value
    const standaloneItems = items.filter((item) => item.parentId === null)
    const groupMap = new Map<string, MenuItem[]>()

    items.forEach((item) => {
      if (item.parentId !== null) {
        if (!groupMap.has(item.parentId)) {
          groupMap.set(item.parentId, [])
        }
        groupMap.get(item.parentId)!.push(item)
      }
    })

    const result: (MenuItem | MenuGroup)[] = [...standaloneItems]

    groupMap.forEach((children, parentId) => {
      const pg = parentGroups.value[parentId]
      if (pg && children.length > 0) {
        result.push({
          id: parentId,
          name: pg.name,
          icon: pg.icon,
          order: pg.order,
          visible: true,
          children: children.sort((a, b) => a.order - b.order),
        } as MenuGroup)
      }
    })

    return result.sort((a, b) => a.order - b.order)
  })

  // 未读/配置统计（供 15-1sidebar.vue 统计卡片使用）
  const totalCount = computed(() => menuItems.value.length)
  const visibleCount = computed(() => menuItems.value.filter((i) => i.visible).length)
  const hiddenCount = computed(() => menuItems.value.filter((i) => !i.visible).length)

  // ===== Actions =====

  /** 从 localStorage 加载配置 */
  function loadConfig(): void {
    const saved = localStorage.getItem(LOCAL_STORAGE_KEY)
    if (saved) {
      try {
        const parsed = JSON.parse(saved) as MenuItem[]
        // 合并默认配置：以默认配置为基础，用保存的配置覆盖
        menuItems.value = defaultMenuItems.map((defaultItem) => {
          const savedItem = parsed.find((c) => c.id === defaultItem.id)
          return savedItem ? { ...defaultItem, ...savedItem } : { ...defaultItem }
        })
        return
      } catch (e) {
        console.warn('加载侧边栏菜单配置失败，使用默认配置', e)
      }
    }
    menuItems.value = JSON.parse(JSON.stringify(defaultMenuItems))
  }

  /** 保存配置到 localStorage */
  function saveConfig(): void {
    localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(menuItems.value))
  }

  /** 重新排序（拖拽后调用） */
  function reorderItems(newOrderedIds: string[]): void {
    // 按新顺序分配 order
    newOrderedIds.forEach((id, index) => {
      const item = menuItems.value.find((m) => m.id === id)
      if (item) {
        item.order = index + 1
      }
    })
    saveConfig()
  }

  /** 切换显示/隐藏 */
  function toggleVisible(id: string): void {
    const item = menuItems.value.find((m) => m.id === id)
    if (item) {
      item.visible = !item.visible
      saveConfig()
    }
  }

  /** 更换图标 */
  function setIcon(id: string, iconName: string): void {
    const item = menuItems.value.find((m) => m.id === id)
    if (item) {
      item.icon = iconName
      saveConfig()
    }
  }

  /** 更新菜单项名称 */
  function updateName(id: string, name: string): void {
    const item = menuItems.value.find((m) => m.id === id)
    if (item) {
      item.name = name
      saveConfig()
    }
  }

  /** 恢复默认配置 */
  function resetDefault(): void {
    menuItems.value = JSON.parse(JSON.stringify(defaultMenuItems))
    parentGroups.value = { ...defaultParentGroups }
    saveConfig()
  }

  /** TODO: 后续对接后端 API */
  // async function fetchMenuConfigFromApi(): Promise<void> {
  //   const res = await api.getMenuConfig()
  //   menuItems.value = res.data
  // }

  // ===== 后端动态菜单（按角色，供 layout 侧边栏优先使用） =====
  const dynamicMenus = ref<BackendMenuItem[]>([])

  /** 后端菜单树的侧边栏渲染结构（分组节点带 children，叶子节点不带） */
  const dynamicSidebarItems = computed<BackendMenuItem[]>(() => dynamicMenus.value)

  /** 从后端拉取当前角色的菜单树 */
  async function fetchDynamicMenus(): Promise<boolean> {
    try {
      const res = await getMenus()
      const body = res.data
      if (body.code === 200 && body.data) {
        dynamicMenus.value = body.data
        return true
      }
      return false
    } catch (e) {
      console.warn('[menuStore] 加载后端菜单失败，使用本地兜底', e)
      return false
    }
  }

  /** 在后端菜单树中按 path 查找菜单名称（供面包屑/标签页使用） */
  function findMenuNameByPath(path: string): string {
    const walk = (nodes: BackendMenuItem[]): string => {
      for (const n of nodes) {
        if (n.path === path) return n.name
        if (n.children?.length) {
          const r = walk(n.children)
          if (r) return r
        }
      }
      return ''
    }
    return walk(dynamicMenus.value)
  }

  return {
    // state
    menuItems,
    parentGroups,
    // getters
    visibleMenuItems,
    sidebarMenuStructure,
    sidebarItems,
    totalCount,
    visibleCount,
    hiddenCount,
    // 后端动态菜单
    dynamicMenus,
    dynamicSidebarItems,
    // actions
    loadConfig,
    saveConfig,
    reorderItems,
    toggleVisible,
    setIcon,
    updateName,
    resetDefault,
    fetchDynamicMenus,
    findMenuNameByPath,
  }
})
