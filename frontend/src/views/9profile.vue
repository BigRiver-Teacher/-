<template>
  <div class="profile-page">
    <!-- ===== 页面标题 ===== -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Avatar /></el-icon>
        个人主页
      </h2>
      <div class="header-actions">
        <el-button type="primary" plain @click="handleNotification">
          <el-icon><Bell /></el-icon>
          通知中心
          <el-badge v-if="notificationStore.unreadCount > 0" :value="notificationStore.unreadCount" class="badge" />
        </el-button>
        <el-button type="primary" plain @click="handleTodo">
          <el-icon><List /></el-icon>
          待办事项
          <el-badge v-if="todoCount > 0" :value="todoCount" class="badge" />
        </el-button>
        <el-button type="primary" plain @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- ===== 用户信息区域 ===== -->
    <div class="user-section">
      <div class="user-card">
        <div class="user-avatar">
          <el-avatar :size="80" :src="userInfo.avatar" />
          <el-button
            type="primary"
            size="small"
            class="avatar-upload-btn"
            @click="handleUploadAvatar"
          >
            <el-icon><Camera /></el-icon>
            更换头像
          </el-button>
        </div>
        <div class="user-info">
          <div class="user-name">{{ userInfo.name }}</div>
          <div class="user-role">{{ userInfo.role }}</div>
          <div class="user-contact">
            <span
              ><el-icon><Message /></el-icon> {{ userInfo.email }}</span
            >
            <span
              ><el-icon><Phone /></el-icon> {{ userInfo.phone }}</span
            >
          </div>
        </div>
        <div class="user-actions">
          <el-button type="primary" @click="handleEditProfile">
            <el-icon><Edit /></el-icon>
            编辑资料
          </el-button>
        </div>
      </div>

      <!-- ===== 统计卡片（使用 StatsCard 公共组件，颜色由父级控制） ===== -->
      <div class="stats-grid">
        <StatsCard
          icon-component="Notification"
          :value="stats.noticeCount"
          label="发布通知"
          icon-bg-color="#e6f7ff"
          icon-color="#409eff"
          card-bg-color="#f8f9fa"
        />
        <StatsCard
          icon-component="Monitor"
          :value="stats.deviceCount"
          label="管理设备"
          icon-bg-color="#f0f9eb"
          icon-color="#67c23a"
          card-bg-color="#f8f9fa"
        />
        <StatsCard
          icon-component="User"
          :value="stats.elderCount"
          label="老人数量"
          icon-bg-color="#fdf6ec"
          icon-color="#e6a23c"
          card-bg-color="#f8f9fa"
        />
        <StatsCard
          icon-component="Warning"
          :value="stats.warningCount"
          label="处理预警"
          icon-bg-color="#fef0f0"
          icon-color="#f56c6c"
          card-bg-color="#f8f9fa"
        />
      </div>
    </div>

    <!-- ===== 下部分：最近活动 + 系统概览 + 快速操作 ===== -->
    <div class="bottom-section">
      <!-- 左：最近活动 -->
      <div class="activity-section">
        <div class="section-header">
          <span class="section-title"
            ><el-icon><Clock /></el-icon>最近活动</span
          >
        </div>
        <div class="activity-list">
          <div v-for="(item, index) in recentActivities" :key="index" class="activity-item">
            <div class="activity-icon" :class="item.type">
              <el-icon v-if="item.type === 'notice'"><Notification /></el-icon>
              <el-icon v-else-if="item.type === 'device'"><Monitor /></el-icon>
              <el-icon v-else-if="item.type === 'warning'"><Warning /></el-icon>
              <el-icon v-else><Document /></el-icon>
            </div>
            <div class="activity-content">
              <div class="activity-text">{{ item.content }}</div>
              <div class="activity-time">{{ item.time }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右：系统概览 + 快速操作 -->
      <div class="right-section">
        <!-- 系统概览 -->
        <div class="overview-section">
          <div class="section-header">
            <span class="section-title"
              ><el-icon><DataAnalysis /></el-icon>系统概览</span
            >
          </div>
          <div class="overview-grid">
            <div class="overview-item">
              <div class="overview-number">{{ overview.registeredUsers }}</div>
              <div class="overview-label">注册用户</div>
            </div>
            <div class="overview-item">
              <div class="overview-number">{{ overview.onlineDevices }}</div>
              <div class="overview-label">在线设备</div>
            </div>
            <div class="overview-item">
              <div class="overview-number">{{ overview.pendingWarnings }}</div>
              <div class="overview-label">待处理预警</div>
            </div>
            <div class="overview-item">
              <div class="overview-number">{{ overview.generatedReports }}</div>
              <div class="overview-label">生成报告</div>
            </div>
          </div>
        </div>

        <!-- 快速操作 -->
        <div class="quick-actions">
          <div class="section-header">
            <span class="section-title"
              ><el-icon><Operation /></el-icon>快速操作</span
            >
          </div>
          <div class="quick-grid">
            <div class="quick-item" @click="goToDevice">
              <el-icon><Connection /></el-icon>
              <span>传感设备管理</span>
            </div>
            <div class="quick-item" @click="goToExpert">
              <el-icon><Headset /></el-icon>
              <span>专家康养咨询</span>
            </div>
            <div class="quick-item" @click="goToCaregiver">
              <el-icon><UserFilled /></el-icon>
              <span>陪护人员管理</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== 编辑资料弹框 ===== -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑资料"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form ref="editFormRef" :model="editFormData" label-width="100px">
        <el-form-item label="头像">
          <div class="edit-avatar">
            <el-avatar :size="60" :src="editFormData.avatar" />
            <el-button size="small" @click="handleUploadAvatar">更换头像</el-button>
          </div>
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="editFormData.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="角色">
          <el-input v-model="editFormData.role" disabled />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editFormData.email" disabled />
          <span class="field-hint">邮箱不可更改</span>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editFormData.phone" disabled />
          <span class="field-hint">手机号不可更改</span>
        </el-form-item>
        <el-form-item label="修改密码">
          <el-button type="warning" @click="handleChangePassword">
            <el-icon><Lock /></el-icon>
            修改密码
          </el-button>
        </el-form-item>
        <el-divider />
        <el-form-item label="账号操作">
          <div style="display: flex; gap: 12px">
            <el-button type="danger" @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-button>
            <el-button type="info" @click="handleSwitchAccount">
              <el-icon><RefreshRight /></el-icon>
              切换账号
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEditProfile">保存</el-button>
      </template>
    </el-dialog>

    <!-- ===== 修改密码弹框 ===== -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="420px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入当前密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPassword">确认修改</el-button>
      </template>
    </el-dialog>

    <!-- ===== 通知中心弹窗（使用 NotificationCenter 公共组件） ===== -->
    <NotificationCenter v-model="notificationDialogVisible" />

    <!-- ===== 待办事项弹框 ===== -->
    <el-dialog v-model="todoDialogVisible" title="待办事项" width="500px">
      <div class="todo-list">
        <div v-for="(item, index) in todoList" :key="index" class="todo-item">
          <el-checkbox v-model="item.done" @change="handleTodoToggle(item)">
            <span :class="{ 'todo-done': item.done }">{{ item.content }}</span>
          </el-checkbox>
          <el-tag
            :type="
              item.priority === 'urgent'
                ? 'danger'
                : item.priority === 'important'
                  ? 'warning'
                  : 'info'
            "
            size="small"
          >
            {{ item.priorityLabel }}
          </el-tag>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" plain @click="clearCompleted">清除已完成</el-button>
        <el-button @click="todoDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- ===== 发布通知弹框 ===== -->
    <el-dialog
      v-model="publishDialogVisible"
      title="发布通知"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form ref="publishFormRef" :model="publishForm" :rules="publishRules" label-width="100px">
        <el-form-item label="通知标题" prop="title">
          <el-input v-model="publishForm.title" placeholder="请输入通知标题" />
        </el-form-item>
        <el-form-item label="通知类型" prop="type">
          <el-select v-model="publishForm.type" placeholder="请选择通知类型" style="width: 100%">
            <el-option label="系统通知" value="system" />
            <el-option label="预警通知" value="warning" />
            <el-option label="公告通知" value="notice" />
          </el-select>
        </el-form-item>
        <el-form-item label="通知对象" prop="target">
          <el-select v-model="publishForm.target" placeholder="请选择通知对象" style="width: 100%">
            <el-option label="全部用户" value="all" />
            <el-option label="老人" value="elder" />
            <el-option label="家属" value="family" />
            <el-option label="工作人员" value="staff" />
          </el-select>
        </el-form-item>
        <el-form-item label="通知内容" prop="content">
          <el-input
            v-model="publishForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入通知内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPublish">发布通知</el-button>
      </template>
    </el-dialog>

    <!-- ===== 隐藏的文件上传input ===== -->
    <input
      ref="avatarInput"
      type="file"
      accept="image/*"
      style="display: none"
      @change="handleFileChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onActivated } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  Avatar,
  Refresh,
  Bell,
  List,
  Edit,
  Camera,
  Message,
  Phone,
  Notification,
  Monitor,
  User,
  Warning,
  Clock,
  DataAnalysis,
  Operation,
  Connection,
  Headset,
  UserFilled,
  Lock,
  SwitchButton,
  RefreshRight,
  Document,
  Check,
  Close,
  Plus,
} from '@element-plus/icons-vue'
import StatsCard from '@/components/StatsCard.vue'
import NotificationCenter from '@/components/NotificationCenter.vue'
import { useNotificationStore } from '@/stores/notificationStore'
import { useUserStore } from '@/stores/user'
import { useDeviceStore } from '@/stores/deviceStore'
import { useElderStore } from '@/stores/elderData'
import { useAlertStore } from '@/stores/alertStore'
import { useStatsStore } from '@/stores/statsStore'
import { getSystemOverview } from '@/api/stats'

const router = useRouter()
const notificationStore = useNotificationStore()
const userStore = useUserStore()
const deviceStore = useDeviceStore()
const elderStore = useElderStore()
const alertStore = useAlertStore()
const statsStore = useStatsStore()

// ===== 用户信息（从 store 获取） =====
const userInfo = computed(() => ({
  name: userStore.userInfo?.realName || userStore.userInfo?.username || '管理员',
  role: userStore.userInfo?.role || '系统管理员',
  email: 'admin@example.com',
  phone: '***********',
  avatar: 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg',
}))

// ===== 统计数据（从 store 获取） =====
const stats = reactive({
  noticeCount: 0,
  deviceCount: 0,
  elderCount: 0,
  warningCount: 0,
})

// ===== 系统概览 =====
const overview = reactive({
  registeredUsers: '--' as number | string,
  onlineDevices: 0,
  pendingWarnings: 0,
  generatedReports: '--' as number | string,
})

// ===== 最近活动（从通知列表生成） =====
const recentActivities = computed(() => {
  return notificationStore.notifications.slice(0, 8).map((n) => ({
    type: n.type === 'warning' ? 'warning' : n.type === 'system' ? 'notice' : 'report',
    content: n.title,
    time: n.time,
  }))
})

const isNoticeOpen = ref(false)
const notificationDialogVisible = ref(false)

function handleNotification() {
  notificationDialogVisible.value = true
}

// ===== 待办事项 =====
const todoDialogVisible = ref(false)
const todoCount = ref(0)

interface TodoItem {
  id: number
  content: string
  done: boolean
  priority: string
  priorityLabel: string
}

const todoList = ref<TodoItem[]>([])

/** 从后端数据动态生成待办列表 */
function updateTodoList() {
  const items: TodoItem[] = []
  let id = 0

  const pendingAlerts = alertStore.alertList.filter((a) => a.status === 'pending').length
  if (pendingAlerts > 0) {
    items.push({ id: ++id, content: `处理${pendingAlerts}条未处理的预警`, done: false, priority: 'urgent', priorityLabel: '紧急' })
  }

  const offlineDevices = deviceStore.deviceList.filter((d) => d.status === 'offline' || d.status === 'fault').length
  if (offlineDevices > 0) {
    items.push({ id: ++id, content: `检查${offlineDevices}台离线/故障设备`, done: false, priority: 'important', priorityLabel: '重要' })
  }

  items.push({ id: ++id, content: '更新老人健康档案', done: false, priority: 'normal', priorityLabel: '普通' })
  items.push({ id: ++id, content: '生成月度康养报告', done: false, priority: 'normal', priorityLabel: '普通' })

  todoList.value = items
  todoCount.value = items.filter((t) => !t.done).length
}

function handleTodo() {
  todoDialogVisible.value = true
}

function handleTodoToggle(item: TodoItem) {
  if (item.done) {
    ElMessage.success(`已完成：${item.content}`)
    todoCount.value = todoList.value.filter((t) => !t.done).length
  }
}

function clearCompleted() {
  todoList.value = todoList.value.filter((item) => !item.done)
  ElMessage.success('已清除所有已完成事项')
}

// ===== 发布通知 =====
const publishDialogVisible = ref(false)
const publishForm = reactive({
  title: '',
  type: '',
  target: '',
  content: '',
})

const publishRules = {
  title: [{ required: true, message: '请输入通知标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择通知类型', trigger: 'change' }],
  target: [{ required: true, message: '请选择通知对象', trigger: 'change' }],
  content: [{ required: true, message: '请输入通知内容', trigger: 'blur' }],
}

const publishFormRef = ref<any>(null)

function handlePublish() {
  publishDialogVisible.value = true
}

function submitPublish() {
  publishFormRef.value?.validate((valid: boolean) => {
    if (!valid) return

    ElMessage.success('通知发布成功')
    publishDialogVisible.value = false
    Object.assign(publishForm, { title: '', type: '', target: '', content: '' })
  })
}

// ===== 编辑资料 =====
const editDialogVisible = ref(false)
const editFormData = reactive({
  name: '',
  role: '',
  email: '',
  phone: '',
  avatar: '',
})

const editFormRef = ref<any>(null)

function handleEditProfile() {
  Object.assign(editFormData, {
    name: userInfo.value.name,
    role: userInfo.value.role,
    email: userInfo.value.email,
    phone: userInfo.value.phone,
    avatar: userInfo.value.avatar,
  })
  editDialogVisible.value = true
}

function submitEditProfile() {
  if (!editFormData.name) {
    ElMessage.warning('请输入姓名')
    return
  }
  ElMessage.success('资料更新成功')
  editDialogVisible.value = false
}

// ===== 更换头像 =====
const avatarInput = ref<HTMLInputElement | null>(null)

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
    const avatarUrl = e.target?.result as string
    editFormData.avatar = avatarUrl
    ElMessage.success('头像更换成功')
  }
  reader.readAsDataURL(file)
  target.value = ''
}

// ===== 修改密码 =====
const passwordDialogVisible = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: (err?: Error) => void) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

const passwordFormRef = ref<any>(null)

function handleChangePassword() {
  Object.assign(passwordForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
  passwordFormRef.value?.resetFields()
  passwordDialogVisible.value = true
}

function submitPassword() {
  passwordFormRef.value?.validate((valid: boolean) => {
    if (!valid) return
    ElMessage.info('密码修改功能请通过忘记密码流程操作')
    passwordDialogVisible.value = false
  })
}

// ===== 退出登录 =====
function handleLogout() {
  ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
    confirmButtonText: '确定退出',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      userStore.clearToken()
      ElMessage.success('已退出登录')
      setTimeout(() => {
        router.push('/login')
      }, 500)
    })
    .catch(() => {})
}

// ===== 切换账号 =====
function handleSwitchAccount() {
  handleLogout()
}

// ===== 快速操作跳转 =====
function goToDevice() {
  router.push('/home/device')
}

function goToExpert() {
  router.push('/home/expert')
}

function goToCaregiver() {
  router.push('/home/caregiver')
}

// ===== 刷新数据（从后端获取真实数据） =====
async function refreshData() {
  try {
    await Promise.all([
      userStore.fetchUserInfo(),
      deviceStore.fetchStats(),
      deviceStore.fetchDevices({ page: 1, size: 200 }),
      elderStore.fetchElderList({ page: 1, size: 200 }),
      alertStore.fetchAlerts({ page: 1, size: 200 }),
      notificationStore.fetchNotifications({ pageNum: 1, pageSize: 10 }),
      notificationStore.fetchUnreadCount(),
    ])

    // 更新统计数据
    stats.noticeCount = notificationStore.total
    stats.deviceCount = deviceStore.total
    stats.elderCount = elderStore.total
    stats.warningCount = alertStore.pendingCount

    overview.onlineDevices = deviceStore.stats.online
    overview.pendingWarnings = deviceStore.stats.todayAlert

    // 从后端拉取注册用户数 / 生成报告数
    try {
      const ov = await getSystemOverview()
      const od = ov.data.data
      if (od) {
        overview.registeredUsers = od.registeredUsers ?? overview.registeredUsers
        overview.generatedReports = od.generatedReports ?? overview.generatedReports
      }
    } catch {
      // 概览接口失败时保持占位
    }

    updateTodoList()
    ElMessage.success('数据已刷新')
  } catch {
    ElMessage.error('刷新数据失败')
  }
}

onMounted(async () => {
  await refreshData()
  checkExpandNotice()
})

// keep-alive 场景下 onMounted 不会重复触发，需要通过 onActivated 检测铃铛跳转
onActivated(() => {
  checkExpandNotice()
})

/** 检测铃铛图标跳转带来的 expandNotice 标记 */
function checkExpandNotice() {
  if (window.history.state?.expandNotice) {
    isNoticeOpen.value = true
    notificationDialogVisible.value = true
    history.replaceState({ ...window.history.state, expandNotice: false }, '')
  }
}
</script>

<style scoped>
.profile-page {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: 100%;
}

/* ===== 页面标题 ===== */
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
  align-items: center;
}

.badge {
  margin-left: 4px;
}

:deep(.el-badge__content) {
  border: none;
}

/* ===== 用户信息区域 ===== */
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
  margin: 4px 0 8px 0;
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

/* ===== 统计卡片 ===== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  background: #f8f9fa;
  border-radius: 10px;
  padding: 16px 20px;
  transition: all 0.3s;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  margin-right: 14px;
  flex-shrink: 0;
}

.stat-icon.blue {
  background: #e6f7ff;
  color: #409eff;
}
.stat-icon.green {
  background: #f0f9eb;
  color: #67c23a;
}
.stat-icon.orange {
  background: #fdf6ec;
  color: #e6a23c;
}
.stat-icon.red {
  background: #fef0f0;
  color: #f56c6c;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #999;
}

/* ===== 下部分 ===== */
.bottom-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

/* ===== 最近活动 ===== */
.activity-section {
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

.activity-list {
  max-height: 400px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-shrink: 0;
  color: #fff;
}

.activity-icon.notice {
  background: #409eff;
}
.activity-icon.device {
  background: #67c23a;
}
.activity-icon.warning {
  background: #f56c6c;
}
.activity-icon.report {
  background: #e6a23c;
}

.activity-content {
  flex: 1;
}

.activity-text {
  font-size: 14px;
  color: #333;
  line-height: 1.4;
}

.activity-time {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

/* ===== 右侧区域 ===== */
.right-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ===== 系统概览 ===== */
.overview-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.overview-item {
  text-align: center;
  padding: 8px 0;
}

.overview-number {
  font-size: 26px;
  font-weight: 700;
  color: #409eff;
}

.overview-label {
  font-size: 13px;
  color: #999;
  margin-top: 2px;
}

/* ===== 快速操作 ===== */
.quick-actions {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
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

/* ===== 编辑资料弹框 ===== */
.edit-avatar {
  display: flex;
  align-items: center;
  gap: 16px;
}

.field-hint {
  font-size: 12px;
  color: #999;
  display: block;
  margin-top: 2px;
}

/* ===== 通知中心 ===== */
.notification-list {
  max-height: 400px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: background 0.2s;
}

.notification-item:hover {
  background: #f8f9fa;
  margin: 0 -16px;
  padding: 12px 16px;
}

.notification-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
  background: #dcdfe6;
}

.notification-dot.unread {
  background: #409eff;
}

.notification-content {
  flex: 1;
}

.notification-title {
  font-size: 14px;
  color: #333;
}

.notification-time {
  font-size: 12px;
  color: #999;
}

/* ===== 通知详情 ===== */
.notification-detail {
  padding: 8px 0;
}

.detail-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.detail-time {
  font-size: 13px;
  color: #999;
  margin: 8px 0 16px 0;
}

.detail-content {
  font-size: 14px;
  color: #666;
  line-height: 1.8;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.detail-type {
  margin-top: 16px;
  font-size: 13px;
  color: #999;
}

/* ===== 待办事项 ===== */
.todo-list {
  max-height: 400px;
  overflow-y: auto;
}

.todo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}

.todo-item:last-child {
  border-bottom: none;
}

.todo-done {
  text-decoration: line-through;
  color: #999;
}

/* ===== 响应式 ===== */
@media (max-width: 1200px) {
  .bottom-section {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .overview-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .quick-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .user-card {
    flex-direction: column;
    text-align: center;
  }

  .user-contact {
    flex-direction: column;
    gap: 8px;
    align-items: center;
  }

  .stats-grid {
    grid-template-columns: 1fr 1fr;
  }

  .overview-grid {
    grid-template-columns: 1fr 1fr;
  }

  .quick-grid {
    grid-template-columns: 1fr 1fr;
  }

  .profile-page {
    padding: 12px;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .overview-grid {
    grid-template-columns: 1fr;
  }

  .quick-grid {
    grid-template-columns: 1fr;
  }

  .header-actions {
    flex-wrap: wrap;
  }

  .page-title {
    font-size: 18px;
  }

  .user-name {
    font-size: 20px;
  }
}
</style>
