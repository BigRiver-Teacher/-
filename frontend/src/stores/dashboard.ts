// stores/dashboard.ts — 首页看板布局 & 统计卡片数据
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getDeviceStatistics } from '@/api/stats'

interface Card {
  id: string
  name: string
  icon: string
  type: string
  visible: boolean
  colspan: number
  color: string
  previewValue: number
  value: number
}

const defaultCards: Card[] = [
  {
    id: '1', name: '总方案数', icon: 'Document', type: '统计',
    visible: true, colspan: 1, color: '#409eff', previewValue: 45, value: 45,
  },
  {
    id: '2', name: '进行中', icon: 'Timer', type: '统计',
    visible: true, colspan: 1, color: '#67c23a', previewValue: 12, value: 12,
  },
  {
    id: '3', name: '已完成', icon: 'CircleCheck', type: '统计',
    visible: true, colspan: 1, color: '#67c23a', previewValue: 18, value: 18,
  },
  {
    id: '4', name: '待审核', icon: 'Clock', type: '统计',
    visible: true, colspan: 1, color: '#e6a23c', previewValue: 5, value: 5,
  },
  {
    id: '5', name: '平均评分', icon: 'StarFilled', type: '统计',
    visible: true, colspan: 1, color: '#e6a23c', previewValue: 4.2, value: 4.2,
  },
  {
    id: '6', name: '总用户数', icon: 'User', type: '统计',
    visible: true, colspan: 1, color: '#409eff', previewValue: 128, value: 128,
  },
  {
    id: '7', name: '今日预警', icon: 'Warning', type: '统计',
    visible: false, colspan: 2, color: '#f56c6c', previewValue: 3, value: 3,
  },
  {
    id: '8', name: '在线设备', icon: 'Grid', type: '统计',
    visible: false, colspan: 1, color: '#67c23a', previewValue: 32, value: 32,
  },
]

export const useDashboardStore = defineStore('dashboard', () => {
  // ===== 默认卡片配置 =====
  const cards = ref<Card[]>([])

  // ===== 加载配置（从 localStorage） =====
  function loadConfig(): void {
    const saved = localStorage.getItem('dashboardConfig')
    if (saved) {
      try {
        const parsed = JSON.parse(saved) as Card[]
        cards.value = defaultCards.map((defaultCard) => {
          const savedCard = parsed.find((c) => c.id === defaultCard.id)
          return savedCard ? { ...defaultCard, ...savedCard } : { ...defaultCard }
        })
        return
      } catch (e) {
        console.warn('加载看板配置失败', e)
      }
    }
    cards.value = JSON.parse(JSON.stringify(defaultCards))
  }

  // ===== 从后端刷新卡片实时数据 =====
  async function refreshCardValues(): Promise<void> {
    try {
      const res = await getDeviceStatistics()
      const stats = res.data.data

      // 更新在线设备卡片的值
      const onlineCard = cards.value.find((c) => c.id === '8')
      if (onlineCard && stats) {
        onlineCard.value = stats.onlineDevices ?? onlineCard.value
      }

      // 更新今日预警卡片的值
      const alertCard = cards.value.find((c) => c.id === '7')
      if (alertCard && stats) {
        alertCard.value = stats.todayAlertCount ?? alertCard.value
      }
    } catch {
      // 接口失败时保持预览值
    }
  }

  // ===== 保存配置到 localStorage =====
  function saveConfig(): void {
    localStorage.setItem('dashboardConfig', JSON.stringify(cards.value))
  }

  // ===== 更新卡片属性 =====
  function updateCard(id: string, updates: Partial<Card>) {
    const index = cards.value.findIndex((c) => c.id === id)
    if (index > -1) {
      cards.value[index] = { ...cards.value[index], ...updates }
      saveConfig()
    }
  }

  // ===== 切换显示状态 =====
  function toggleVisible(id: string): void {
    const card = cards.value.find((c) => c.id === id)
    if (card) {
      card.visible = !card.visible
      saveConfig()
    }
  }

  // ===== 调整列宽 =====
  function setColspan(id: string, colspan: number): void {
    const card = cards.value.find((c) => c.id === id)
    if (card) {
      card.colspan = colspan
      saveConfig()
    }
  }

  // ===== 调整顺序 =====
  function reorderCards(newOrder: string[]): void {
    const ordered: Card[] = []
    newOrder.forEach((id: string) => {
      const card = cards.value.find((c) => c.id === id)
      if (card) ordered.push(card)
    })
    cards.value.forEach((card: Card) => {
      if (!ordered.find((c) => c.id === card.id)) {
        ordered.push(card)
      }
    })
    cards.value = ordered
    saveConfig()
  }

  // ===== 重置为默认 =====
  function resetDefault(): void {
    cards.value = JSON.parse(JSON.stringify(defaultCards))
    saveConfig()
  }

  // ===== 获取可见卡片 =====
  const visibleCards = computed(() => {
    return cards.value.filter((card: Card) => card.visible)
  })

  // ===== 获取某个卡片的配置 =====
  function getCardConfig(id: string): Card | undefined {
    return cards.value.find((c: Card) => c.id === id)
  }

  return {
    cards,
    visibleCards,
    loadConfig,
    refreshCardValues,
    saveConfig,
    updateCard,
    toggleVisible,
    setColspan,
    reorderCards,
    resetDefault,
    getCardConfig,
  }
})
