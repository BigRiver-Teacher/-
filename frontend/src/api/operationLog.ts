import request from '@/utils/request'
import type { Result, PageVO } from '@/utils/request'

// ===== 类型定义 =====
export interface OperationLogVO {
  id: number
  operateTime: string
  operator: string
  operateType: string
  module: string
  content: string
  ip: string
  status: string
  errorMsg: string | null
}

export interface OperationLogQuery {
  page?: number
  size?: number
  operator?: string
  operateType?: string
  module?: string
  status?: string
}

// ===== 操作日志列表（分页） =====
export function getOperationLogList(params?: OperationLogQuery) {
  return request.get<Result<PageVO<OperationLogVO>>>('/log/list', { params })
}

// ===== 导出操作日志（文件下载） =====
export function exportOperationLog() {
  return request.get<Blob>('/log/export', { responseType: 'blob' })
}

// ===== 清理指定时间之前的日志 =====
export function cleanOperationLog(beforeDate: string) {
  return request.delete<Result<void>>('/log/clean', { params: { beforeDate } })
}
