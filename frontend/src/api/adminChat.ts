import request from '@/utils/request'
import type { Result } from '@/utils/request'

export interface AdminConversation {
  id: number
  type: string
  elderId: number
  elderName: string
  familyUserId: number
  familyName: string
  serviceUserId: number
  serviceId: number
  serviceName: string
  status: string
  lastMessage: string
  lastMessageTime: string
  createTime: string
}

export interface AdminAttachment {
  id?: number
  fileName: string
  fileType: string
  filePath: string
  fileSize: number
}

export interface AdminMessage {
  id: number
  conversationId: number
  senderId: number
  senderRole: string
  senderName: string
  content: string
  contentType: string
  isRead: number
  createTime: string
  attachments?: AdminAttachment[]
}

export function getAdminConversations() {
  return request.get<Result<AdminConversation[]>>('/admin/chat/conversations')
}

export function getAdminMessages(conversationId: number) {
  return request.get<Result<AdminMessage[]>>(`/admin/chat/conversations/${conversationId}/messages`)
}
