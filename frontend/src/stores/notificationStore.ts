// stores/notificationStore.ts — 通知中心数据管理
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  getNotificationList,
  getUnreadCount,
  markNotificationRead,
  markAllNotificationsRead,
  type NotificationVO,
} from '@/api/notification'

export type { NotificationVO as Notification }

export const useNotificationStore = defineStore('notificationStore', () => {
  // ===== State =====
  const notifications = ref<NotificationVO[]>([])
  const total = ref(0)
  const unreadNum = ref(0)
  const loading = ref(false)

  // ===== Getters =====
  const unreadCount = computed(() => unreadNum.value)

  const unreadNotifications = computed(() =>
    notifications.value.filter((item) => !item.read),
  )

  // ===== Actions =====

  /** 加载通知列表（分页） */
  async function fetchNotifications(params?: {
    pageNum?: number
    pageSize?: number
  }): Promise<void> {
    loading.value = true
    try {
      const res = await getNotificationList(params)
      const data = res.data.data
      notifications.value = data.list ?? []
      total.value = data.total ?? 0
    } finally {
      loading.value = false
    }
  }

  /** 加载未读数量 */
  async function fetchUnreadCount(): Promise<void> {
    try {
      const res = await getUnreadCount()
      const data = res.data.data
      // 后端返回 Map<String, Long>，key 可能是 "unread" 或 "count"
      unreadNum.value = data?.unread ?? data?.count ?? 0
    } catch {
      // 静默失败
    }
  }

  /** 标记单条通知已读 */
  async function markRead(id: number): Promise<void> {
    await markNotificationRead(id)
    const item = notifications.value.find((n) => n.id === id)
    if (item) item.read = true
    if (unreadNum.value > 0) unreadNum.value--
  }

  /** 全部标记已读 */
  async function markAllRead(): Promise<void> {
    await markAllNotificationsRead()
    notifications.value.forEach((item) => {
      item.read = true
    })
    unreadNum.value = 0
  }

  return {
    // state
    notifications,
    total,
    unreadNum,
    loading,
    // getters
    unreadCount,
    unreadNotifications,
    // actions
    fetchNotifications,
    fetchUnreadCount,
    markRead,
    markAllRead,
  }
})
