import axios from 'axios'
import type { AxiosError, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// ===== 统一响应类型 =====
export interface Result<T = any> {
  code: number
  message: string
  data: T
  timestamp: string
}

// ===== 分页数据类型 =====
export interface PageVO<T = any> {
  total: number
  list: T[]
}

// ===== 创建 axios 实例 =====
const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// ===== 请求拦截器 =====
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 从 localStorage 读取 token（避免在模块顶层引用 store 导致循环依赖）
    const token = localStorage.getItem('token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (err) => Promise.reject(err),
)

// ===== 响应拦截器 =====
// 阶段2：确保每个失败响应只提示一次，避免"业务码提示 + HTTP 错误提示"重复弹窗
let lastErrorToastAt = 0
let lastErrorToastMsg = ''
function toastOnce(message: string) {
  const now = Date.now()
  if (message === lastErrorToastMsg && now - lastErrorToastAt < 800) return
  lastErrorToastMsg = message
  lastErrorToastAt = now
  ElMessage.error(message)
}

request.interceptors.response.use(
  (response: AxiosResponse<Result>) => {
    const res = response.data

    // 后端直接返回 blob（如文件下载）
    if (response.config.responseType === 'blob') {
      return response
    }

    // 业务状态码判断
    if (res.code !== undefined && res.code !== 200) {
      toastOnce(res.message || '请求失败')

      // 401 未授权 → 回登录页
      if (res.code === 401) {
        localStorage.removeItem('token')
        router.push('/login')
      }

      return Promise.reject(new Error(res.message || '请求失败'))
    }

    // 成功时返回整个 AxiosResponse（调用方通过 res.data.data 取业务数据）
    return response
  },
  (error: AxiosError<Result>) => {
    const status = error.response?.status
    const msg = error.response?.data?.message

    switch (status) {
      case 401:
        localStorage.removeItem('token')
        router.push('/login')
        toastOnce('登录过期，请重新登录！')
        break
      case 403:
        toastOnce(msg || '权限不足，请联系管理员')
        break
      case 404:
        toastOnce(msg || '请求的资源不存在')
        break
      case 500:
        toastOnce(msg || '服务器错误，请联系管理员')
        break
      default:
        toastOnce(msg || '网络异常，请稍后重试')
    }

    return Promise.reject(error)
  },
)

export default request
