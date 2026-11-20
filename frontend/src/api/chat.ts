import request from '@/utils/request'
import type { Result } from '@/utils/request'

export interface ConversationVO {
  id: number
  type: string
  elderId: number
  elderName: string
  peerName: string
  peerRole: string
  lastMessage: string
  lastMessageTime: string
  unreadCount: number
}

export interface Attachment {
  id?: number
  fileName: string
  fileType: string
  filePath: string
  fileSize: number
}

export interface MessageVO {
  id: number
  conversationId: number
  senderId: number
  senderRole: string
  senderName: string
  content: string
  contentType: string
  isRead: number
  createTime: string
  attachments?: Attachment[]
}

export function getConversations() {
  return request.get<Result<ConversationVO[]>>('/chat/conversations')
}

export function getMessages(conversationId: number) {
  return request.get<Result<MessageVO[]>>(`/chat/conversations/${conversationId}/messages`)
}

export function sendMessage(conversationId: number, data: { content: string; contentType?: string; attachments?: Attachment[] }) {
  return request.post<Result<MessageVO>>(`/chat/conversations/${conversationId}/messages`, data)
}

export function createConversation(data: { type: string; elderId: number; serviceUserId: number; serviceId?: number }) {
  return request.post<Result<number>>('/chat/conversations', data)
}

export function markConversationRead(conversationId: number) {
  return request.put<Result<boolean>>(`/chat/conversations/${conversationId}/read`)
}

export function uploadFile(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<Result<Attachment>>('/chat/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
