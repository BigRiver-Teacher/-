import request from '@/utils/request'
import type { Result, PageVO } from '@/utils/request'

// ===== 类型定义 =====
export interface PushLogVO {
  id: number
  elderName: string
  familyPhone: string
  channel: string
  scence: string
  content: string
  fullContent: string
  status: string
  time: string
  duration: string
  retryCount: number
  response: string
  createTime: string
}

// ===== 推送日志列表（分页） =====
export function getPushLogList(params?: { page?: number; size?: number }) {
  return request.get<Result<PageVO<PushLogVO>>>('/push-log/list', { params })
}

// ===== 单条重推 =====
export function retryPushLog(id: number) {
  return request.post<Result<string>>(`/push-log/retry/${id}`)
}

// ===== 批量重推 =====
export function batchRetryPushLogs(ids: number[]) {
  return request.post<Result<string>>('/push-log/batch-retry', { ids })
}
