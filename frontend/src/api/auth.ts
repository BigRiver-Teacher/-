import request from '@/utils/request'
import type { Result } from '@/utils/request'

// ===== 类型定义 =====
export interface LoginDTO {
  username: string
  password: string
}

export interface LoginVO {
  token: string
  userInfo: UserInfoVO
}

export interface UserInfoVO {
  id: number
  username: string
  realName: string
  role: string
}

export interface RegisterDTO {
  phone: string
  verifyCode: string
  password: string
}

export interface ResetPasswordDTO {
  email?: string
  phone?: string
  verifyCode: string
  newPassword: string
}

// ===== 登录 =====
export function login(data: LoginDTO) {
  return request.post<Result<LoginVO>>('/auth/login', data)
}

// ===== 注册 =====
export function register(data: RegisterDTO) {
  return request.post<Result<string>>('/auth/register', data)
}

// ===== 发送注册验证码 =====
export function sendRegisterCode(phone: string) {
  return request.post<Result<string>>('/auth/send-register-code', { phone })
}

// ===== 发送重置密码验证码 =====
export function sendResetCode(email: string) {
  return request.post<Result<string>>('/auth/send-reset-code', { email })
}

// ===== 重置密码 =====
export function resetPassword(data: ResetPasswordDTO) {
  return request.post<Result<string>>('/auth/reset-password', data)
}
