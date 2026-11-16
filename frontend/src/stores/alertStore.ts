// stores/alertStore.ts — 告警数据管理
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  getAlertList,
  processAlert,
  ignoreAlert,
  markAlertRead,
  type AlertVO,
  type AlertQueryDTO,
} from '@/api/alert'

export const useAlertStore = defineStore('alertStore', () => {
  // ===== State =====
  const alertList = ref<AlertVO[]>([])
  const total = ref(0)
  const loading = ref(false)

  // ===== Getters =====
  const pendingCount = computed(() => alertList.value.filter((a) => a.status === 'pending').length)
  const processingCount = computed(() => alertList.value.filter((a) => a.status === 'processing').length)
  const resolvedCount = computed(() => alertList.value.filter((a) => a.status === 'resolved').length)

  // ===== Actions =====

  /** 加载告警列表（分页+筛选） */
  async function fetchAlerts(params?: AlertQueryDTO): Promise<void> {
    loading.value = true
    try {
      const res = await getAlertList(params || {})
      const data = res.data.data
      alertList.value = data.list ?? []
      total.value = data.total ?? 0
    } finally {
      loading.value = false
    }
  }

  /** 处理告警 */
  async function handleAlert(id: number, data: { process: string; result: string }): Promise<void> {
    await processAlert(id, data)
    // 更新本地状态
    const item = alertList.value.find((a) => a.id === id)
    if (item) {
      item.status = 'resolved'
      item.handler = '管理员'
      item.handleTime = new Date().toLocaleString('zh-CN', {
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit', second: '2-digit',
      }).replace(/\//g, '-')
    }
  }

  /** 忽略告警 */
  async function ignoreAlertById(id: number): Promise<void> {
    await ignoreAlert(id)
    const item = alertList.value.find((a) => a.id === id)
    if (item) item.status = 'ignored'
  }

  /** 标记已读 */
  async function markRead(id: number): Promise<void> {
    await markAlertRead(id)
  }

  return {
    // state
    alertList,
    total,
    loading,
    // getters
    pendingCount,
    processingCount,
    resolvedCount,
    // actions
    fetchAlerts,
    handleAlert,
    ignoreAlertById,
    markRead,
  }
})
