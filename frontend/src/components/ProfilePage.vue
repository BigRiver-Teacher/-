<template>
  <div class="profile-page">
    <!-- ===== 页面标题 ===== -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Avatar /></el-icon>
        {{ title }}
      </h2>
      <div class="header-actions">
        <slot name="header-actions" />
      </div>
    </div>

    <!-- ===== 用户信息区域 ===== -->
    <div class="user-section">
      <div class="user-card">
        <div class="user-avatar">
          <el-avatar :size="80" :src="userInfo.avatar" />
          <el-button type="primary" size="small" class="avatar-upload-btn" @click="handleUploadAvatar">
            <el-icon><Camera /></el-icon>
            更换头像
          </el-button>
          <input ref="avatarInput" type="file" accept="image/*" style="display: none" @change="handleFileChange" />
        </div>
        <div class="user-info">
          <div class="user-name">{{ userInfo.name }}</div>
          <div class="user-role">{{ userInfo.role }}</div>
          <div class="user-contact">
            <span v-if="userInfo.email"><el-icon><Message /></el-icon> {{ userInfo.email }}</span>
            <span v-if="userInfo.phone"><el-icon><Phone /></el-icon> {{ userInfo.phone }}</span>
          </div>
        </div>
        <div class="user-actions">
          <el-button type="primary" @click="openEdit">
            <el-icon><Edit /></el-icon>
            编辑资料
          </el-button>
          <slot name="user-actions" />
        </div>
      </div>

      <!-- 统计卡片 -->
      <div v-if="stats.length" class="stats-grid">
        <StatsCard v-for="(s, i) in stats" :key="i" v-bind="s" />
      </div>
    </div>

    <!-- ===== 快速操作 ===== -->
    <div v-if="quickActions.length" class="quick-actions">
      <div class="section-header">
        <span class="section-title"><el-icon><Operation /></el-icon>快速操作</span>
      </div>
      <div class="quick-grid">
        <div v-for="(q, i) in quickActions" :key="i" class="quick-item" @click="go(q.path)">
          <el-icon><component :is="q.icon" /></el-icon>
          <span>{{ q.label }}</span>
        </div>
      </div>
    </div>

    <!-- ===== 额外内容（最近活动等，由父组件填充） ===== -->
    <slot name="extra" />

    <!-- ===== 编辑资料弹窗 ===== -->
    <el-dialog v-model="editVisible" title="编辑资料" width="480px" :close-on-click-modal="false">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="头像">
          <div class="edit-avatar">
            <el-avatar :size="60" :src="editForm.avatar" />
            <el-button size="small" @click="handleUploadAvatar">更换头像</el-button>
          </div>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="editForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="角色">
          <el-input v-model="editForm.role" disabled />
        </el-form-item>
        <el-form-item v-if="editForm.email" label="邮箱">
          <el-input v-model="editForm.email" disabled />
        </el-form-item>
        <el-form-item v-if="editForm.phone" label="手机号">
          <el-input v-model="editForm.phone" disabled />
        </el-form-item>
        <el-form-item label="修改密码">
          <el-button type="warning" @click="openPassword">
            <el-icon><Lock /></el-icon>
            修改密码
          </el-button>
        </el-form-item>
        <el-divider />
        <el-form-item label="账号操作">
          <el-button type="danger" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            退出登录
          </el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- ===== 修改密码弹窗 ===== -->
    <el-dialog v-model="passwordVisible" title="修改密码" width="420px" :close-on-click-modal="false">
      <el-form :model="passwordForm" label-width="100px">
        <el-form-item label="当前密码">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPassword">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { Avatar, Camera, Edit, Message, Phone, Operation, Lock, SwitchButton } from '@element-plus/icons-vue'
import StatsCard from './StatsCard.vue'

interface StatItem {
  iconComponent: string
  value: string | number
  label: string
  iconBgColor: string
  iconColor: string
}

interface QuickAction {
  icon: string
  label: string
  path: string
}

const props = withDefaults(
  defineProps<{
    title?: string
    userInfo: { name: string; role: string; email?: string; phone?: string; avatar?: string }
    stats?: StatItem[]
    quickActions?: QuickAction[]
  }>(),
  {
    title: '个人中心',
    stats: () => [],
    quickActions: () => [],
  },
)

const emit = defineEmits<{
  (e: 'save-profile', data: { name: string; avatar?: string }): void
  (e: 'change-password', data: { oldPassword: string; newPassword: string }): void
  (e: 'logout'): void
}>()

const router = useRouter()
const avatarInput = ref<HTMLInputElement | null>(null)
const editVisible = ref(false)
const passwordVisible = ref(false)

const editForm = reactive({
  name: props.userInfo.name,
  role: props.userInfo.role,
  email: props.userInfo.email || '',
  phone: props.userInfo.phone || '',
  avatar: props.userInfo.avatar || '',
})

const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

function handleUploadAvatar() {
  avatarInput.value?.click()
}

function handleFileChange(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请上传图片文件')
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过2MB')
    return
  }
  const reader = new FileReader()
  reader.onload = (e) => {
    editForm.avatar = e.target?.result as string
    ElMessage.success('头像已更新（保存后生效）')
  }
  reader.readAsDataURL(file)
  target.value = ''
}

function openEdit() {
  Object.assign(editForm, {
    name: props.userInfo.name,
    role: props.userInfo.role,
    email: props.userInfo.email || '',
    phone: props.userInfo.phone || '',
    avatar: props.userInfo.avatar || '',
  })
  editVisible.value = true
}

function submitEdit() {
  if (!editForm.name.trim()) {
    ElMessage.warning('请输入姓名')
    return
  }
  emit('save-profile', { name: editForm.name.trim(), avatar: editForm.avatar })
  editVisible.value = false
}

function openPassword() {
  Object.assign(passwordForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
  passwordVisible.value = true
}

function submitPassword() {
  if (!passwordForm.oldPassword || passwordForm.newPassword.length < 6) {
    ElMessage.warning('请填写当前密码，新密码不少于6位')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  emit('change-password', { oldPassword: passwordForm.oldPassword, newPassword: passwordForm.newPassword })
  passwordVisible.value = false
}

function handleLogout() {
  ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
    confirmButtonText: '确定退出',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      emit('logout')
    })
    .catch(() => {})
}

function go(path: string) {
  router.push(path)
}
</script>

<style scoped>
.profile-page {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: 100%;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}
.page-title .el-icon {
  font-size: 26px;
  color: #409eff;
}
.header-actions {
  display: flex;
  gap: 10px;
}

.user-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.user-card {
  display: flex;
  align-items: center;
  gap: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 20px;
}
.user-avatar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.avatar-upload-btn {
  font-size: 12px;
  padding: 4px 12px;
}
.user-info {
  flex: 1;
}
.user-name {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
}
.user-role {
  font-size: 14px;
  color: #999;
  margin: 4px 0 8px;
}
.user-contact {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: #666;
}
.user-contact span {
  display: flex;
  align-items: center;
  gap: 6px;
}
.user-actions {
  display: flex;
  gap: 10px;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.quick-actions {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}
.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}
.section-title .el-icon {
  font-size: 18px;
  color: #409eff;
}
.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 12px;
  background: #f8f9fa;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
}
.quick-item:hover {
  border-color: #409eff;
  background: #e6f7ff;
  transform: translateY(-2px);
}
.quick-item .el-icon {
  font-size: 28px;
  color: #409eff;
}
.quick-item span {
  font-size: 13px;
  color: #666;
  text-align: center;
}

.edit-avatar {
  display: flex;
  align-items: center;
  gap: 16px;
}

@media (max-width: 768px) {
  .user-card {
    flex-direction: column;
    text-align: center;
  }
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .quick-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
