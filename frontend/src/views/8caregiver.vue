<template>
  <div class="caregiver-page">
    <!-- ===== 页面标题 ===== -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><UserFilled /></el-icon>
        陪护人员管理
      </h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加陪护人员
        </el-button>
        <el-button type="primary" plain @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="success" plain>
          <el-icon><Download /></el-icon>
          导出
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
          <div class="stat-label">陪护人员总数</div>
        </div>
      </div>
      <div class="stat-card active">
        <div class="stat-icon">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.active }}</div>
          <div class="stat-label">在职人员</div>
        </div>
      </div>
      <div class="stat-card female">
        <div class="stat-icon">
          <el-icon><Female /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.female }}</div>
          <div class="stat-label">女性</div>
        </div>
      </div>
      <div class="stat-card male">
        <div class="stat-icon">
          <el-icon><Male /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.male }}</div>
          <div class="stat-label">男性</div>
        </div>
      </div>
    </div>

    <!-- ===== 筛选区域 ===== -->
    <div class="filter-section">
      <div class="filter-header">
        <div class="filter-title">
          <el-icon><Search /></el-icon>
          <span>搜索</span>
        </div>
        <div class="filter-actions">
          <el-button type="primary" plain @click="resetFilter">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </div>
      </div>
      <div class="filter-body">
        <el-form :model="filterForm" label-width="100px" inline>
          <el-form-item label="姓名">
            <el-input
              v-model="filterForm.name"
              placeholder="请输入姓名"
              clearable
              style="width: 180px"
              prefix-icon="Search"
            />
          </el-form-item>
          <el-form-item label="电话号码">
            <el-input
              v-model="filterForm.phone"
              placeholder="请输入电话号码"
              clearable
              style="width: 180px"
              prefix-icon="Phone"
            />
          </el-form-item>
          <el-form-item label="职位">
            <el-select
              v-model="filterForm.position"
              placeholder="全部"
              clearable
              style="width: 160px"
            >
              <el-option
                v-for="item in positionOptions"
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
              style="width: 140px"
            >
              <el-option
                v-for="item in statusOptions"
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
          <span>陪护人员列表</span>
          <el-tag size="small" type="info">共 {{ total }} 条</el-tag>
        </div>
      </div>

      <el-table :data="paginatedList" stripe style="width: 100%" row-key="id" border>
        <el-table-column prop="name" label="姓名" width="100" align="center" />
        <el-table-column prop="age" label="年龄" width="80" align="center" />
        <el-table-column prop="position" label="职位" width="140" align="center">
          <template #default="{ row }">
            <el-tag :type="getPositionType(row.position)" size="small">{{ row.position }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话号码" width="150" align="center" />
        <el-table-column prop="address" label="地址" min-width="200">
          <template #default="{ row }">
            <span class="address-text">{{ row.address }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '在职' ? 'success' : 'danger'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" type="warning" link @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                编辑
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

    <!-- ===== 添加/编辑弹框 ===== -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number
            v-model="formData.age"
            :min="18"
            :max="65"
            style="width: 100%"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="职位" prop="position">
          <el-select v-model="formData.position" placeholder="请选择职位" style="width: 100%">
            <el-option label="陪护主管" value="陪护主管" />
            <el-option label="高级陪护员" value="高级陪护员" />
            <el-option label="陪护员" value="陪护员" />
            <el-option label="康复护理师" value="康复护理师" />
            <el-option label="心理辅导师" value="心理辅导师" />
          </el-select>
        </el-form-item>
        <el-form-item label="电话号码" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入电话号码" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="formData.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="在职" value="在职" />
            <el-option label="离职" value="离职" />
          </el-select>
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
  UserFilled,
  Refresh,
  Plus,
  Edit,
  Delete,
  Search,
  List,
  Download,
  User,
  CircleCheck,
  Female,
  Male,
  Phone,
} from '@element-plus/icons-vue'
import { getCaregiverList, addCaregiver, updateCaregiver, deleteCaregiver } from '@/api/caregiver'

// ===== 统计数据 =====
const stats = reactive({
  total: 0,
  active: 0,
  female: 0,
  male: 0,
})

// ===== 数据列表 =====
const caregiverList = ref([])

// ===== 职位枚举映射（后端 code → 页面中文） =====
const POSITION_TEXT_MAP = {
  NURSING_SUPERVISOR: '陪护主管',
  SENIOR_CAREGIVER: '高级陪护员',
  CAREGIVER: '陪护员',
  REHAB_NURSE: '康复护理师',
  PSYCHO_COUNSELOR: '心理辅导师',
  care_supervisor: '陪护主管',
  senior_caregiver: '高级陪护员',
  caregiver: '陪护员',
  rehab_nurse: '康复护理师',
  psycho_counselor: '心理辅导师',
}

// ===== 职位枚举映射（页面中文 → 后端 code） =====
const POSITION_CODE_MAP = {
  陪护主管: 'NURSING_SUPERVISOR',
  高级陪护员: 'SENIOR_CAREGIVER',
  陪护员: 'CAREGIVER',
  康复护理师: 'REHAB_NURSE',
  心理辅导师: 'PSYCHO_COUNSELOR',
}

// ===== 状态枚举映射 =====
const STATUS_TEXT_MAP = {
  ACTIVE: '在职',
  INACTIVE: '离职',
}

const STATUS_CODE_MAP = {
  在职: 'ACTIVE',
  离职: 'INACTIVE',
}

// ===== 初始化数据（从后端加载） =====
async function initData() {
  try {
    const res = await getCaregiverList({ page: 1, size: 10000 })
    const data = res.data.data
    caregiverList.value = (data?.list || []).map((item) => ({
      ...item,
      position: POSITION_TEXT_MAP[item.position] || item.position,
      status: STATUS_TEXT_MAP[item.status] || item.status,
    }))
    updateStats()
  } catch (e) {
    // 错误提示已由响应拦截器统一处理
  }
}

// ===== 更新统计数据 =====
function updateStats() {
  const list = caregiverList.value
  stats.total = list.length
  stats.active = list.filter((item) => item.status === '在职').length
  stats.female = list.filter(
    (item) => item.name.endsWith('丽') || item.name.endsWith('芳') || item.name.endsWith('红'),
  ).length
  stats.male = stats.total - stats.female
}

// ===== 筛选条件 =====
const filterForm = reactive({
  name: '',
  phone: '',
  position: '',
  status: '',
})

const positionOptions = [
  { label: '全部', value: '' },
  { label: '陪护主管', value: '陪护主管' },
  { label: '高级陪护员', value: '高级陪护员' },
  { label: '陪护员', value: '陪护员' },
  { label: '康复护理师', value: '康复护理师' },
  { label: '心理辅导师', value: '心理辅导师' },
]

const statusOptions = [
  { label: '全部', value: '' },
  { label: '在职', value: '在职' },
  { label: '离职', value: '离职' },
]

// ===== 分页 =====
const currentPage = ref(1)
const pageSize = ref(5)

// ===== 添加/编辑弹框 =====
const dialogVisible = ref(false)
const dialogTitle = ref('添加陪护人员')
const isEdit = ref(false)
const editId = ref(null)

const formData = reactive({
  name: '',
  age: undefined as number | undefined,
  position: '',
  phone: '',
  address: '',
  status: '在职',
})

const formRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  age: [{ required: true, message: '请输入年龄', trigger: 'blur' }],
  position: [{ required: true, message: '请选择职位', trigger: 'change' }],
  phone: [{ required: true, message: '请输入电话号码', trigger: 'blur' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }],
}

const formRef = ref(null)

// ===== 筛选数据 =====
const filteredList = computed(() => {
  let list = [...caregiverList.value]

  if (filterForm.name) {
    list = list.filter((item) => item.name.includes(filterForm.name))
  }
  if (filterForm.phone) {
    list = list.filter((item) => item.phone.includes(filterForm.phone))
  }
  if (filterForm.position) {
    list = list.filter((item) => item.position === filterForm.position)
  }
  if (filterForm.status) {
    list = list.filter((item) => item.status === filterForm.status)
  }

  return list
})

const total = computed(() => filteredList.value.length)

const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredList.value.slice(start, end)
})

// ===== 工具函数 =====
function getPositionType(position) {
  const map = {
    陪护主管: 'danger',
    高级陪护员: 'warning',
    陪护员: 'primary',
    康复护理师: 'success',
    心理辅导师: 'info',
  }
  return map[position] || 'info'
}

// ===== 操作函数 =====
function handleAdd() {
  isEdit.value = false
  editId.value = null
  dialogTitle.value = '添加陪护人员'
  Object.assign(formData, {
    name: '',
    age: '',
    position: '',
    phone: '',
    address: '',
    status: '在职',
  })
  formRef.value?.resetFields()
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑陪护人员'
  Object.assign(formData, row)
  dialogVisible.value = true
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除陪护人员 "${row.name}" 吗？`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      await deleteCaregiver(row.id)
      ElMessage.success('删除成功')
      await initData()
    })
    .catch(() => {})
}

function resetFilter() {
  filterForm.name = ''
  filterForm.phone = ''
  filterForm.position = ''
  filterForm.status = ''
  currentPage.value = 1
}

function refreshData() {
  initData()
  ElMessage.success('数据已刷新')
}

// ===== 提交表单 =====
async function submitForm() {
  formRef.value?.validate(async (valid) => {
    if (!valid) return

    const payload = {
      name: formData.name,
      age: Number(formData.age),
      position: POSITION_CODE_MAP[formData.position] || formData.position,
      phone: formData.phone,
      address: formData.address,
      status: STATUS_CODE_MAP[formData.status] || formData.status,
    }

    try {
      if (isEdit.value) {
        await updateCaregiver(editId.value, payload)
        ElMessage.success('更新成功')
      } else {
        await addCaregiver(payload)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      await initData()
    } catch (e) {
      // 错误提示已由响应拦截器统一处理
    }
  })
}

// ===== 生命周期 =====
onMounted(() => {
  initData()
})
</script>

<style scoped>
.caregiver-page {
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
}

/* ===== 统计卡片 ===== */
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
.stat-card.female .stat-icon {
  background: #fef0f0;
  color: #f56c6c;
}
.stat-card.male .stat-icon {
  background: #e6f7ff;
  color: #409eff;
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

/* ===== 筛选区域 ===== */
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

/* ===== 表格区域 ===== */
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

.address-text {
  display: block;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #666;
}

.action-buttons {
  display: flex;
  gap: 2px;
  flex-wrap: nowrap;
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

/* ===== 分页 ===== */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  margin-top: 16px;
}

/* ===== 响应式 ===== */
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

  .caregiver-page {
    padding: 12px;
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