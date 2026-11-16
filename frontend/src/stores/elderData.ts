// stores/elderData.ts — 老人数据管理（Pinia Store + 兼容旧版静态导出）
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  getElderList,
  getElderById,
  addElder,
  updateElder,
  deleteElder,
  type ElderVO,
  type ElderDTO,
} from '@/api/elder'
import { getFallList, type FallVO } from '@/api/fall'

// ===== 类型定义（兼容旧版） =====
export type { ElderVO as Elder }

export interface FallRecord {
  date: string
  location: string
  cause: string
  severity: string
  status: string
}

// ===== 枚举映射（后端代码 <-> 页面中文显示） =====
const GENDER_TEXT: Record<string, string> = { MALE: '男', FEMALE: '女' }
const GENDER_CODE: Record<string, string> = { 男: 'MALE', 女: 'FEMALE' }
const RESIDENCE_TEXT: Record<string, string> = {
  PERMANENT: '常住',
  TEMPORARY: '临时居住',
  临时居住: '临时居住',
  ALONE: '独居',
  WITH_FAMILY: '与子女同住',
  NURSING_HOME: '养老院',
  独居: '独居',
  与子女同住: '与子女同住',
  养老院: '养老院',
}
const RESIDENCE_CODE: Record<string, string> = {
  临时居住: 'TEMPORARY',
  独居: 'ALONE',
  与子女同住: 'WITH_FAMILY',
  养老院: 'NURSING_HOME',
}

// ===== Pinia Store =====
export const useElderStore = defineStore('elder', () => {
  // ===== state =====
  const elderList = ref<ElderVO[]>([])
  const total = ref(0)
  const loading = ref(false)
  const fallHistory = ref<FallVO[]>([])

  // ===== getters =====
  const elderNames = computed(() => elderList.value.map((e) => e.name))

  // ===== actions =====

  /** 加载老人列表（分页） */
  async function fetchElderList(params?: {
    page?: number
    size?: number
    name?: string
    phone?: string
  }): Promise<void> {
    loading.value = true
    try {
      const res = await getElderList(params)
      const data = res.data.data
      elderList.value = (data.list ?? []).map((e: any) => ({
        ...e,
        gender: GENDER_TEXT[e.gender] || e.gender,
        residenceType: RESIDENCE_TEXT[e.residenceType] || e.residenceType,
      }))
      total.value = data.total ?? 0
    } finally {
      loading.value = false
    }
  }

  /** 根据 ID 获取老人详情 */
  async function fetchElderById(id: number): Promise<ElderVO | null> {
    const res = await getElderById(id)
    return res.data.data ?? null
  }

  /** 根据姓名查找老人（内存搜索） */
  function findElderByName(name: string): ElderVO | undefined {
    return elderList.value.find((e) => e.name === name)
  }

  /** 根据身份码查找老人（内存搜索） */
  function findElderByCode(code: string): ElderVO | undefined {
    return elderList.value.find((e) => e.code === code)
  }

  /** 获取老人的跌倒历史（从 API 获取） */
  async function fetchFallHistory(elderName?: string): Promise<void> {
    try {
      const res = await getFallList({ page: 1, size: 200, elderName })
      fallHistory.value = res.data.data?.list ?? []
    } catch {
      // 静默失败，保留静态 fallback
    }
  }

  /** 获取指定老人的跌倒历史（优先使用 API 数据，fallback 到静态数据） */
  function getFallHistory(name: string): FallRecord[] {
    return fallHistory.value
      .filter((f) => f.elderName === name)
      .map((f) => ({
        date: f.date,
        location: f.location,
        cause: f.cause,
        severity: f.severity,
        status: f.status,
      }))
  }

  /** 新增老人 */
  async function createElder(data: ElderDTO): Promise<number> {
    const res = await addElder({
      ...data,
      gender: GENDER_CODE[data.gender || ''] || data.gender,
      residenceType: RESIDENCE_CODE[data.residenceType || ''] || data.residenceType,
    })
    return res.data.data
  }

  /** 编辑老人 */
  async function editElder(id: number, data: ElderDTO): Promise<void> {
    await updateElder(id, {
      ...data,
      gender: GENDER_CODE[data.gender || ''] || data.gender,
      residenceType: RESIDENCE_CODE[data.residenceType || ''] || data.residenceType,
    })
  }

  /** 删除老人 */
  async function removeElder(id: number): Promise<void> {
    await deleteElder(id)
  }

  return {
    // state
    elderList,
    total,
    loading,
    // getters
    elderNames,
    // actions
    fetchElderList,
    fetchElderById,
    findElderByName,
    findElderByCode,
    getFallHistory,
    fetchFallHistory,
    createElder,
    editElder,
    removeElder,
  }
})
