<template>
  <div class="permission-page">
    <!-- ===== 页面标题 ===== -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><User /></el-icon>
        账号权限
      </h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增用户
        </el-button>
        <el-button type="primary" plain @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- ===== 统计卡片 ===== -->
    <div class="stats-grid">
      <div class="stat-card total">
        <div class="stat-icon">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.total }}</div>
          <div class="stat-label">总用户</div>
        </div>
      </div>
      <div class="stat-card active">
        <div class="stat-icon">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.active }}</div>
          <div class="stat-label">启用</div>
        </div>
      </div>
      <div class="stat-card inactive">
        <div class="stat-icon">
          <el-icon><CircleClose /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.inactive }}</div>
          <div class="stat-label">禁用</div>
        </div>
      </div>
      <div class="stat-card roles">
        <div class="stat-icon">
          <el-icon><Avatar /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.roleCount }}</div>
          <div class="stat-label">角色数</div>
        </div>
      </div>
    </div>

    <!-- ===== 筛选区域 ===== -->
    <div class="filter-section">
      <div class="filter-header">
        <div class="filter-title">
          <el-icon><Filter /></el-icon>
          <span>筛选条件</span>
        </div>
        <div class="filter-actions">
          <el-button type="primary" plain @click="resetFilter">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </div>
      </div>
      <div class="filter-body">
        <el-form :model="filterForm" label-width="80px" inline>
          <el-form-item label="姓名">
            <el-input
              v-model="filterForm.name"
              placeholder="请输入姓名"
              clearable
              style="width: 150px"
              prefix-icon="Search"
            />
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="filterForm.role" placeholder="全部" clearable style="width: 140px">
              <el-option
                v-for="item in roleOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="filterForm.status"
              placeholder="全部"
              clearable
              style="width: 120px"
            >
              <el-option label="全部" value="" />
              <el-option label="启用" value="active" />
              <el-option label="禁用" value="inactive" />
            </el-select>
          </el-form-item>
          <el-form-item label="所属社区">
            <el-select
              v-model="filterForm.community"
              placeholder="全部"
              clearable
              style="width: 140px"
            >
              <el-option
                v-for="item in communityOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- ===== 表格区域 ===== -->
    <div class="table-section">
      <div class="table-header">
        <div class="table-title">
          <el-icon><List /></el-icon>
          <span>用户列表</span>
          <el-tag size="small" type="info">共 {{ total }} 条</el-tag>
        </div>
      </div>

      <el-table :data="paginatedList" stripe style="width: 100%" row-key="index" border>
        <el-table-column prop="account" label="账号" width="120" align="center" />
        <el-table-column prop="name" label="姓名" width="90" align="center" />
        <el-table-column prop="role" label="角色" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)" size="small">
              {{ row.role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="community" label="所属社区" width="120" align="center" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'danger'" size="small">
              {{ row.status === 'active' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLogin" label="最后登录" min-width="160" align="center" />
        <el-table-column label="操作" width="300" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" type="primary" link @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button size="small" type="warning" link @click="handleResetPwd(row)">
                <el-icon><Lock /></el-icon>
                重置密码
              </el-button>
              <el-button
                size="small"
                :type="row.status === 'active' ? 'danger' : 'success'"
                link
                @click="handleToggleStatus(row)"
              >
                <el-icon>
                  <component :is="row.status === 'active' ? 'CircleClose' : 'CircleCheck'" />
                </el-icon>
                {{ row.status === 'active' ? '禁用' : '启用' }}
              </el-button>
              <el-button size="small" type="danger" link @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          background
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="currentPage = 1"
        >
          <template #total="{ total }">
            <span style="font-weight: 500">共 {{ total }} 条</span>
          </template>
          <template #jumper="{ go }">
            <span style="margin-left: 8px; font-size: 14px; color: #666">前往</span>
            <el-input-number
              :min="1"
              :max="Math.ceil(total / pageSize) || 1"
              :model-value="currentPage"
              @update:model-value="
                (val) => {
                  if (val >= 1 && val <= Math.ceil(total / pageSize)) go(val)
                }
              "
              size="small"
              controls-position="right"
              style="width: 60px; margin: 0 4px"
            />
            <span style="font-size: 14px; color: #666">页</span>
          </template>
        </el-pagination>
      </div>
    </div>

    <!-- ===== 新增/编辑弹框 ===== -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="账号" prop="account">
          <el-input v-model="formData.account" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="formData.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="管理员" />
            <el-option label="社区主管" value="社区主管" />
            <el-option label="工作人员" value="工作人员" />
            <el-option label="专家" value="专家" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属社区" prop="community">
          <el-select v-model="formData.community" placeholder="请选择社区" style="width: 100%">
            <el-option label="朝阳社区" value="朝阳社区" />
            <el-option label="海淀社区" value="海淀社区" />
            <el-option label="西城社区" value="西城社区" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio label="active">启用</el-radio>
            <el-radio label="inactive">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="!isEdit" label="初始密码" prop="password">
          <el-input v-model="formData.password" placeholder="请输入初始密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User,
  Plus,
  Refresh,
  Filter,
  Search,
  List,
  Edit,
  Delete,
  Lock,
  CircleCheck,
  CircleClose,
  Avatar,
} from '@element-plus/icons-vue'
import {
  getUserList,
  addUser,
  editUser,
  deleteUser,
  toggleUserStatus,
  resetUserPassword,
  type UserVO,
} from '@/api/user'

// ===== 统计数据 =====
const stats = reactive({
  total: 0,
  active: 0,
  inactive: 0,
  roleCount: 0,
})

// ===== 数据列表 =====
const userList = ref<UserVO[]>([])
const loading = ref(false)

// ===== 选项数据 =====
const roleOptions = [
  { label: '全部', value: '' },
  { label: '管理员', value: '管理员' },
  { label: '社区主管', value: '社区主管' },
  { label: '工作人员', value: '工作人员' },
  { label: '专家', value: '专家' },
]

const communityOptions = [
  { label: '全部', value: '' },
  { label: '朝阳社区', value: '朝阳社区' },
  { label: '海淀社区', value: '海淀社区' },
  { label: '西城社区', value: '西城社区' },
]

// ===== 筛选条件 =====
const filterForm = reactive({
  name: '',
  role: '',
  status: '',
  community: '',
})

// ===== 分页 =====
const currentPage = ref(1)
const pageSize = ref(5)

// ===== 弹框 =====
const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const isEdit = ref(false)
const editId = ref<number | null>(null)
const formRef = ref<any>(null)
const submitting = ref(false)

const formData = reactive({
  account: '',
  name: '',
  role: '',
  community: '',
  status: 'active',
  password: '',
})

const formRules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  password: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
}

// ===== 初始化数据（从后端获取） =====
async function fetchData() {
  loading.value = true
  try {
    const res = await getUserList({ page: 1, size: 200, ...filterForm })
    const data = res.data.data
    userList.value = data.list ?? []
    updateStats()
  } catch {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// ===== 更新统计数据 =====
function updateStats() {
  const list = userList.value
  stats.total = list.length
  stats.active = list.filter((item) => item.status === 'active').length
  stats.inactive = list.filter((item) => item.status !== 'active').length
  stats.roleCount = new Set(list.map((item) => item.role)).size
}

// ===== 工具函数 =====
function getRoleType(role: string) {
  const map: Record<string, string> = {
    '管理员': 'danger',
    '社区主管': 'warning',
    '工作人员': 'primary',
    '专家': 'success',
  }
  return map[role] || 'info'
}

// ===== 筛选数据 =====
const filteredList = computed(() => {
  let list = [...userList.value]

  if (filterForm.name) {
    list = list.filter((item) => item.name?.includes(filterForm.name))
  }
  if (filterForm.role) {
    list = list.filter((item) => item.role === filterForm.role)
  }
  if (filterForm.status) {
    list = list.filter((item) => item.status === filterForm.status)
  }
  if (filterForm.community) {
    list = list.filter((item) => item.community === filterForm.community)
  }

  return list
})

const total = computed(() => filteredList.value.length)

const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredList.value.slice(start, end)
})

// ===== 操作函数 =====
function handleAdd() {
  isEdit.value = false
  editId.value = null
  dialogTitle.value = '新增用户'
  Object.assign(formData, {
    account: '',
    name: '',
    role: '',
    community: '',
    status: 'active',
    password: '',
  })
  formRef.value?.resetFields()
  dialogVisible.value = true
}

function handleEdit(row: UserVO) {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑用户'
  Object.assign(formData, {
    account: row.account,
    name: row.name,
    role: row.role,
    community: row.community,
    status: row.status,
    password: '',
  })
  dialogVisible.value = true
}

async function handleDelete(row: UserVO) {
  try {
    await ElMessageBox.confirm(`确定要删除用户 "${row.name}" 吗？`, '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    await fetchData()
  } catch { /* 用户取消 */ }
}

async function handleToggleStatus(row: UserVO) {
  const action = row.status === 'active' ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}用户 "${row.name}" 吗？`, `${action}确认`, {
      confirmButtonText: `确定${action}`,
      cancelButtonText: '取消',
      type: 'warning',
    })
    await toggleUserStatus(row.id)
    ElMessage.success(`${action}成功`)
    await fetchData()
  } catch { /* 用户取消 */ }
}

async function handleResetPwd(row: UserVO) {
  try {
    await ElMessageBox.confirm(
      `确定要重置用户 "${row.name}" 的密码吗？`,
      '重置密码确认',
      { confirmButtonText: '确定重置', cancelButtonText: '取消', type: 'info' },
    )
    await resetUserPassword(row.id)
    ElMessage.success('密码已重置')
  } catch { /* 用户取消 */ }
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value && editId.value) {
      await editUser(editId.value, {
        account: formData.account,
        name: formData.name,
        role: formData.role,
        community: formData.community,
        status: formData.status,
        password: formData.password,
      })
      ElMessage.success('更新成功')
    } else {
      await addUser({
        account: formData.account,
        name: formData.name,
        role: formData.role,
        community: formData.community,
        status: formData.status,
        password: formData.password,
      })
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    await fetchData()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

function resetFilter() {
  filterForm.name = ''
  filterForm.role = ''
  filterForm.status = ''
  filterForm.community = ''
  currentPage.value = 1
}

async function refreshData() {
  await fetchData()
  ElMessage.success('数据已刷新')
}

// ===== 生命周期 =====
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.permission-page {
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
  gap: 8px;
  align-items: center;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
  flex-shrink: 0;
}

.stat-card.total .stat-icon {
  background: #e6f7ff;
  color: #409eff;
}

.stat-card.active .stat-icon {
  background: #f0f9eb;
  color: #67c23a;
}

.stat-card.inactive .stat-icon {
  background: #fef0f0;
  color: #f56c6c;
}

.stat-card.roles .stat-icon {
  background: #fdf6ec;
  color: #e6a23c;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 2px;
}

.filter-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}

.filter-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.filter-title .el-icon {
  font-size: 18px;
  color: #409eff;
}

.filter-body :deep(.el-form-item) {
  margin-bottom: 8px;
}

.table-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow-x: auto;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 16px;
}

.table-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.table-title .el-icon {
  font-size: 18px;
  color: #409eff;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th.el-table__cell) {
  background-color: #f5f7fa !important;
  color: #333;
  font-weight: 600;
  padding: 12px 0;
}

:deep(.el-table .el-table__cell) {
  padding: 8px 0;
}

:deep(.el-table .cell) {
  padding: 0 8px;
}

.action-buttons {
  display: flex;
  gap: 2px;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
}

.action-buttons .el-button {
  padding: 4px 6px;
  font-size: 12px;
}

.action-buttons .el-button .el-icon {
  font-size: 14px;
  margin-right: 2px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  margin-top: 16px;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .filter-body :deep(.el-form-item) {
    display: block;
    width: 100%;
  }

  .filter-body :deep(.el-select) {
    width: 100% !important;
  }

  .filter-body :deep(.el-input) {
    width: 100% !important;
  }

  .action-buttons {
    flex-wrap: wrap;
  }

  .permission-page {
    padding: 12px;
  }

  .header-actions {
    flex-wrap: wrap;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 18px;
  }

  .stat-number {
    font-size: 22px;
  }

  .stat-card {
    padding: 16px;
  }

  :deep(.el-table .el-table__cell) {
    padding: 6px 0;
  }

  .action-buttons .el-button {
    padding: 2px 4px;
    font-size: 11px;
  }
}
</style>
