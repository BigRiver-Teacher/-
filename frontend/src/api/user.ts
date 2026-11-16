import request from '@/utils/request'
import type { Result, PageVO } from '@/utils/request'
import type { UserInfoVO } from './auth'

// ===== 类型定义 =====
export interface UserVO {
  id: number
  account: string
  name: string
  role: string
  community: string
  status: string
  lastLogin: string
}

export interface UserSaveDTO {
  id?: number
  account: string
  name: string
  role: string
  community?: string
  password: string
  status?: string
}

export interface UserQueryDTO {
  page: number
  size: number
  account?: string
  role?: string
  community?: string
  status?: string
}

// ===== 获取当前用户信息 =====
export function getUserProfile() {
  return request.get<Result<UserInfoVO>>('/user/profile')
}

// ===== 获取当前用户权限 =====
export function getUserPermissions() {
  return request.get<Result<string[]>>('/user/permissions')
}

// ===== 用户列表（分页） =====
export function getUserList(params: UserQueryDTO) {
  return request.get<Result<PageVO<UserVO>>>('/user/list', { params })
}

// ===== 新增用户 =====
export function addUser(data: UserSaveDTO) {
  return request.post<Result<number>>('/user', data)
}

// ===== 编辑用户 =====
export function editUser(id: number, data: UserSaveDTO) {
  return request.put<Result<string>>(`/user/${id}`, data)
}

// ===== 删除用户 =====
export function deleteUser(id: number) {
  return request.delete<Result<string>>(`/user/${id}`)
}

// ===== 切换用户状态 =====
export function toggleUserStatus(id: number) {
  return request.put<Result<string>>(`/user/${id}/toggle-status`)
}

// ===== 重置用户密码 =====
export function resetUserPassword(id: number) {
  return request.put<Result<string>>(`/user/${id}/reset-password`)
}
