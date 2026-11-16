import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { login as loginApi, type UserInfoVO } from '@/api/auth'
import { getUserProfile } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  // ===== state =====
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfoVO | null>(null)

  // ===== getters =====
  const isLoggedIn = computed(() => !!token.value)

  // ===== actions =====

  /** 登录：调用后端登录接口，返回 true 表示登录成功 */
  async function login(username: string, password: string): Promise<boolean> {
    try {
      const res = await loginApi({ username, password })
      // res.data 结构：{ code, message, data: { token, userInfo }, timestamp }
      const body = res.data
      if (!body.data?.token) {
        console.error('登录响应数据结构异常，完整响应:', JSON.stringify(body))
        return false
      }
      token.value = body.data.token
      userInfo.value = body.data.userInfo ?? null
      localStorage.setItem('token', body.data.token)
      return true
    } catch (err) {
      console.error('登录异常:', err)
      return false
    }
  }

  /** 获取当前用户信息 */
  async function fetchUserInfo(): Promise<void> {
    try {
      const res = await getUserProfile()
      const body = res.data
      if (!body.data) {
        console.error('获取用户信息失败，响应数据为空')
        return
      }
      userInfo.value = body.data
    } catch (err) {
      console.error('获取用户信息异常:', err)
    }
  }

  /** 存储登录 token */
  function setToken(val: string): void {
    token.value = val
    localStorage.setItem('token', val)
  }

  /** 清空 token，退出登录 */
  function clearToken(): void {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    login,
    fetchUserInfo,
    setToken,
    clearToken,
  }
})
