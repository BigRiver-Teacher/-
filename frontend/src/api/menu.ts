import request from '@/utils/request'
import type { Result } from '@/utils/request'

// ===== 后端菜单结构（与后端 Menu 实体对应） =====
export interface BackendMenuItem {
  id: number
  name: string
  path: string
  icon: string
  parentId: number
  visible: boolean
  sort: number
  createTime?: string
  updateTime?: string
  children?: BackendMenuItem[] | null
}

// ===== 获取当前用户按角色过滤后的菜单树 =====
export function getMenus() {
  return request.get<Result<BackendMenuItem[]>>('/menus')
}
