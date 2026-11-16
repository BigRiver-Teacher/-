import request from '@/utils/request'
import type { Result, PageVO } from '@/utils/request'

// ===== 类型定义 =====
export interface NotificationVO {
  id: number
  title: string
  content: string
  type: string
  time: string
  read: boolean
  link?: string
}

// ===== 通知列表（分页） =====
export function getNotificationList(params: {
  pageNum?: number
  pageSize?: number
}) {
  return request.get<Result<PageVO<NotificationVO>>>('/notification/list', {
    params,
  })
}

// ===== 未读通知数量 =====
export function getUnreadCount() {
  return request.get<Result<Record<string, number>>>('/notification/count')
}

// ===== 标记单条已读 =====
export function markNotificationRead(id: number) {
  return request.put<Result<boolean>>(`/notification/${id}/read`)
}

// ===== 全部标记已读 =====
export function markAllNotificationsRead() {
  return request.put<Result<boolean>>('/notification/read-all')
}
