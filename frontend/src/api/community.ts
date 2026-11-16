import request from '@/utils/request'
import type { Result, PageVO } from '@/utils/request'

// ===== 类型定义 =====
export interface CommunityVO {
  id: number
  code: string
  elderName: string
  alertType: string
  action: string
  handler: string
  handlerId: string
  result: string
  reviewStatus: string
  time: string
  fullTime: string
  description: string
  attachments: string[]
  community: string
  alertCode: string
  review: string
  reviewTime: string
  rejectReason: string
  createTime: string
}

export interface CommunityDTO {
  elderName: string
  alertType: string
  action: string
  description: string
  community: string
  result: string
  handler: string
}

// ===== 社区处理台账列表（分页） =====
export function getCommunityList(params?: { page?: number; size?: number }) {
  return request.get<Result<PageVO<CommunityVO>>>('/community/list', { params })
}

// ===== 新增台账 =====
export function addCommunity(data: CommunityDTO) {
  return request.post<Result<number>>('/community', data)
}

// ===== 编辑台账 =====
export function updateCommunity(id: number, data: Partial<CommunityDTO>) {
  return request.put<Result<string>>(`/community/${id}`, data)
}

// ===== 删除台账 =====
export function deleteCommunity(id: number) {
  return request.delete<Result<string>>(`/community/${id}`)
}

// ===== 审核台账 =====
export function reviewCommunity(
  id: number,
  data: { reviewStatus: string; review: string; rejectReason: string },
) {
  return request.put<Result<string>>(`/community/review/${id}`, data)
}
