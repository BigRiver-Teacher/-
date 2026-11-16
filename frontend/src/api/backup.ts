import request from '@/utils/request'
import type { Result, PageVO } from '@/utils/request'

// ===== 类型定义 =====
export interface DataBackupVO {
  id: number
  fileName: string
  backupTime: string
  type: string
  fileSize: number
  status: string
  filePath: string
}

// ===== 备份列表（分页） =====
export function getBackupList(params?: { page?: number; size?: number }) {
  return request.get<Result<PageVO<DataBackupVO>>>('/backup/list', { params })
}

// ===== 立即创建备份（AUTO=自动备份 / MANUAL=手动备份） =====
export function createBackup(type: 'AUTO' | 'MANUAL') {
  return request.post<Result<DataBackupVO>>('/backup', { type })
}

// ===== 下载备份文件（blob 流） =====
export function downloadBackup(id: number) {
  return request.get<Blob>(`/backup/download/${id}`, { responseType: 'blob' })
}

// ===== 从备份恢复数据 =====
export function restoreBackup(id: number) {
  return request.post<Result<void>>(`/backup/restore/${id}`)
}

// ===== 删除备份 =====
export function deleteBackup(id: number) {
  return request.delete<Result<void>>(`/backup/${id}`)
}
