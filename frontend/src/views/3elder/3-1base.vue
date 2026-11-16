<template>
  <div class="base-page">
    <div class="page-header">
      <h2 class="page-title"><el-icon><User /></el-icon>基础档案</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>添加</el-button>
        <el-button type="primary" plain @click="refreshData" :loading="elderStore.loading"><el-icon><Refresh /></el-icon>刷新</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card total"><div class="stat-icon"><el-icon><Document /></el-icon></div>
        <div class="stat-info"><div class="stat-number">{{ elderStore.total }}</div><div class="stat-label">档案数量</div></div></div>
      <div class="stat-card new"><div class="stat-icon"><el-icon><Plus /></el-icon></div>
        <div class="stat-info"><div class="stat-number">{{ deviceStore.stats.bindRate }}%</div><div class="stat-label">设备覆盖率</div></div></div>
      <div class="stat-card coverage"><div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
        <div class="stat-info"><div class="stat-number">{{ deviceStore.stats.online }}</div><div class="stat-label">在线设备</div></div></div>
    </div>

    <!-- 筛选 -->
    <div class="filter-section">
      <div class="filter-header"><div class="filter-title"><el-icon><Filter /></el-icon><span>筛选条件</span></div>
        <div class="filter-actions"><el-button type="primary" plain @click="resetFilter"><el-icon><Refresh /></el-icon>重置</el-button></div>
      </div>
      <div class="filter-body">
        <el-form :model="filterForm" label-width="80px" inline>
          <el-form-item label="姓名"><el-input v-model="filterForm.name" placeholder="请输入姓名" prefix-icon="Search" clearable style="width: 160px" /></el-form-item>
          <el-form-item label="性别">
            <el-select v-model="filterForm.gender" placeholder="全部" clearable style="width: 100px">
              <el-option label="男" value="男" /><el-option label="女" value="女" />
            </el-select>
          </el-form-item>
          <el-form-item label="居住类型">
            <el-select v-model="filterForm.residenceType" placeholder="全部" clearable style="width: 140px">
              <el-option label="常住" value="常住" /><el-option label="临时居住" value="临时居住" /><el-option label="独居" value="独居" /><el-option label="与子女同住" value="与子女同住" /><el-option label="养老院" value="养老院" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- 表格 -->
    <div class="table-section">
      <div class="table-header"><div class="table-title"><el-icon><List /></el-icon><span>老人列表</span><el-tag size="small" type="info">共 {{ elderStore.total }} 条</el-tag></div></div>
      <el-table :data="elderStore.elderList" stripe style="width: 100%" row-key="id" border v-loading="elderStore.loading">
        <el-table-column prop="name" label="姓名" width="80" align="center" />
        <el-table-column prop="gender" label="性别" width="70" align="center">
          <template #default="{ row }"><el-tag :type="row.gender === '男' ? 'primary' : 'danger'" size="small">{{ row.gender }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="70" align="center" />
        <el-table-column prop="idCard" label="身份证号" width="170" align="center">
          <template #default="{ row }"><span style="font-family: monospace; font-size: 13px">{{ row.idCard }}</span></template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="120" align="center" />
        <el-table-column prop="address" label="居住地址" min-width="180">
          <template #default="{ row }"><el-tooltip :content="row.address" placement="top"><span class="address-text">{{ row.address }}</span></el-tooltip></template>
        </el-table-column>
        <el-table-column prop="residenceType" label="居住类型" width="110" align="center">
          <template #default="{ row }"><el-tag :type="row.residenceType === '独居' ? 'danger' : 'success'" size="small">{{ row.residenceType }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" width="140" align="center">
          <template #default="{ row }"><span class="remark-text">{{ row.remark || '-' }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" type="primary" link @click="handleView(row)"><el-icon><View /></el-icon>详情</el-button>
              <el-button size="small" type="warning" link @click="handleEdit(row)"><el-icon><Edit /></el-icon>编辑</el-button>
              <el-button size="small" type="danger" link @click="handleDelete(row)"><el-icon><Delete /></el-icon>删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 添加/编辑弹框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="姓名" prop="name"><el-input v-model="formData.name" placeholder="请输入姓名" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="性别" prop="gender"><el-select v-model="formData.gender" placeholder="请选择性别" style="width:100%"><el-option label="男" value="男" /><el-option label="女" value="女" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="年龄"><el-input-number v-model="formData.age" :min="0" :max="120" style="width:100%" controls-position="right" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="民族"><el-input v-model="formData.ethnicity" placeholder="请输入民族" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="身份证号"><el-input v-model="formData.idCard" placeholder="请输入身份证号" /></el-form-item>
        <el-form-item label="联系电话" prop="phone"><el-input v-model="formData.phone" placeholder="请输入联系电话" /></el-form-item>
        <el-form-item label="居住地址"><el-input v-model="formData.address" placeholder="请输入详细地址" /></el-form-item>
        <el-form-item label="居住类型">
          <el-select v-model="formData.residenceType" placeholder="请选择居住类型" style="width:100%">
            <el-option label="常住" value="常住" /><el-option label="临时居住" value="临时居住" /><el-option label="独居" value="独居" /><el-option label="与子女同住" value="与子女同住" /><el-option label="养老院" value="养老院" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入备注信息" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submitForm" :loading="submitting">确认</el-button></template>
    </el-dialog>

    <!-- 详情弹框 -->
    <el-dialog v-model="detailVisible" title="老人详情" width="560px">
      <div v-if="detailData" class="detail-content">
        <div class="detail-grid">
          <div class="detail-item"><span class="detail-label">姓名</span><span class="detail-value">{{ detailData.name }}</span></div>
          <div class="detail-item"><span class="detail-label">性别</span><el-tag :type="detailData.gender === '男' ? 'primary' : 'danger'" size="small">{{ detailData.gender }}</el-tag></div>
          <div class="detail-item"><span class="detail-label">年龄</span><span class="detail-value">{{ detailData.age }} 岁</span></div>
          <div class="detail-item"><span class="detail-label">民族</span><span class="detail-value">{{ detailData.ethnicity }}</span></div>
          <div class="detail-item"><span class="detail-label">身份证号</span><span class="detail-value" style="font-family: monospace">{{ detailData.idCard }}</span></div>
          <div class="detail-item"><span class="detail-label">联系电话</span><span class="detail-value">{{ detailData.phone }}</span></div>
          <div class="detail-item"><span class="detail-label">居住类型</span><el-tag :type="detailData.residenceType === '独居' ? 'danger' : 'success'" size="small">{{ detailData.residenceType }}</el-tag></div>
          <div class="detail-item full-width"><span class="detail-label">居住地址</span><span class="detail-value">{{ detailData.address }}</span></div>
          <div class="detail-item full-width"><span class="detail-label">备注</span><span class="detail-value" :class="{ highlight: detailData.remark }">{{ detailData.remark || '无' }}</span></div>
        </div>
      </div>
      <template #footer><el-button @click="detailVisible = false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Refresh, Plus, View, Edit, Delete, Filter, Document, CircleCheck, List } from '@element-plus/icons-vue'
import { useElderStore } from '@/stores/elderData'
import { useDeviceStore } from '@/stores/deviceStore'
import type { ElderVO, ElderDTO } from '@/api/elder'

const elderStore = useElderStore()
const deviceStore = useDeviceStore()

// ===== 筛选 =====
const filterForm = reactive({ name: '', gender: '', residenceType: '' })

// ===== 弹框 =====
const dialogVisible = ref(false)
const dialogTitle = ref('添加档案')
const isEdit = ref(false)
const editId = ref<number | null>(null)
const submitting = ref(false)
const formData = reactive<ElderDTO>({ name: '', gender: '', idCard: '', phone: '', age: undefined, ethnicity: '', address: '', residenceType: '', remark: '' })
const formRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  idCard: [{ required: true, message: '请输入身份证号', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
}
const formRef = ref<any>(null)

const detailVisible = ref(false)
const detailData = ref<ElderVO | null>(null)

// ===== 加载 =====
async function refreshData() {
  await elderStore.fetchElderList()
  await deviceStore.fetchStats()
  ElMessage.success('数据已刷新')
}

function handleView(row: any) { detailData.value = row; detailVisible.value = true }

function handleAdd() {
  isEdit.value = false; editId.value = null; dialogTitle.value = '添加档案'
  Object.assign(formData, { name: '', gender: '', idCard: '', phone: '', age: undefined, ethnicity: '', address: '', residenceType: '', remark: '' })
  formRef.value?.resetFields(); dialogVisible.value = true
}

function handleEdit(row: any) {
  isEdit.value = true; editId.value = row.id; dialogTitle.value = '编辑档案'
  Object.assign(formData, { name: row.name, gender: row.gender, idCard: row.idCard, phone: row.phone, age: row.age, ethnicity: row.ethnicity, address: row.address, residenceType: row.residenceType, remark: row.remark })
  dialogVisible.value = true
}

function handleDelete(row: any) {
  ElMessageBox.confirm(`确定要删除 "${row.name}" 的档案吗？`, '删除确认', { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' })
    .then(async () => { await elderStore.removeElder(row.id); ElMessage.success('删除成功') })
    .catch(() => {})
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value && editId.value) {
      await elderStore.editElder(editId.value, formData)
      ElMessage.success('更新成功')
    } else {
      await elderStore.createElder(formData)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
  } catch { ElMessage.error('操作失败') }
  finally { submitting.value = false }
}

function resetFilter() { filterForm.name = ''; filterForm.gender = ''; filterForm.residenceType = ''; elderStore.fetchElderList() }

onMounted(async () => {
  await Promise.all([elderStore.fetchElderList({ page: 1, size: 200 }), deviceStore.fetchStats()])
})
</script>

<style scoped>
.base-page { padding: 20px; background-color: #f0f2f5; min-height: 100%; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { display: flex; align-items: center; gap: 10px; font-size: 22px; font-weight: 700; color: #1a1a1a; margin: 0; }
.page-title .el-icon { font-size: 26px; color: #409eff; }
.header-actions { display: flex; gap: 10px; }
.stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 20px; }
.stat-card { display: flex; align-items: center; background: #fff; border-radius: 12px; padding: 20px 24px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); transition: all 0.3s; cursor: pointer; }
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 4px 20px rgba(0,0,0,0.1); }
.stat-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px; margin-right: 16px; flex-shrink: 0; }
.stat-card.total .stat-icon { background: #e6f7ff; color: #409eff; }
.stat-card.new .stat-icon { background: #f0f9eb; color: #67c23a; }
.stat-card.coverage .stat-icon { background: #fdf6ec; color: #e6a23c; }
.stat-info { flex: 1; }
.stat-number { font-size: 28px; font-weight: 700; color: #1a1a1a; line-height: 1.2; }
.stat-label { font-size: 14px; color: #999; margin-top: 2px; }
.filter-section { background: #fff; border-radius: 12px; padding: 16px 20px; margin-bottom: 16px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); }
.filter-header { display: flex; justify-content: space-between; align-items: center; padding-bottom: 12px; border-bottom: 1px solid #f0f0f0; margin-bottom: 12px; }
.filter-title { display: flex; align-items: center; gap: 8px; font-size: 15px; font-weight: 600; color: #333; }
.filter-title .el-icon { font-size: 18px; color: #409eff; }
.table-section { background: #fff; border-radius: 12px; padding: 16px 20px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); overflow-x: auto; }
.table-header { display: flex; justify-content: space-between; align-items: center; padding-bottom: 12px; border-bottom: 1px solid #f0f0f0; margin-bottom: 16px; }
.table-title { display: flex; align-items: center; gap: 8px; font-size: 15px; font-weight: 600; color: #333; }
.table-title .el-icon { font-size: 18px; color: #409eff; }
:deep(.el-table) { border-radius: 8px; overflow: hidden; }
:deep(.el-table th.el-table__cell) { background-color: #f5f7fa !important; color: #333; font-weight: 600; padding: 12px 0; }
:deep(.el-table .el-table__cell) { padding: 8px 0; }
:deep(.el-table .cell) { padding: 0 8px; }
.address-text { display: block; max-width: 180px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #666; }
.remark-text { display: block; max-width: 140px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #999; font-size: 13px; }
.action-buttons { display: flex; gap: 2px; flex-wrap: nowrap; justify-content: center; align-items: center; }
.action-buttons .el-button { padding: 4px 6px; font-size: 12px; }
.action-buttons .el-button .el-icon { font-size: 14px; margin-right: 2px; }
.detail-content { padding: 4px 0; }
.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px 24px; }
.detail-item { display: flex; flex-direction: column; padding: 8px 0; border-bottom: 1px solid #f5f5f5; }
.detail-item.full-width { grid-column: 1 / -1; }
.detail-label { font-size: 13px; color: #999; margin-bottom: 2px; }
.detail-value { font-size: 14px; color: #333; word-break: break-all; }
.detail-value.highlight { color: #e6a23c; font-weight: 500; }
@media (max-width: 1024px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 768px) { .page-header { flex-direction: column; align-items: flex-start; gap: 12px; } .stats-grid { grid-template-columns: 1fr; } .base-page { padding: 12px; } .detail-grid { grid-template-columns: 1fr; } }
@media (max-width: 480px) { .page-title { font-size: 18px; } .stat-number { font-size: 22px; } .stat-card { padding: 16px; } }
</style>
