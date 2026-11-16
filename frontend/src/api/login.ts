// 兼容旧版导入：loginNew.vue 等组件仍使用 `import { login } from '../api/login'`
// 实际逻辑已迁移到 @/api/auth 和 @/api/user
export { login } from './auth'
export { getUserProfile as getUserInfo } from './user'
