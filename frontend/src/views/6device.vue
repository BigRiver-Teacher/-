<template>
  <div class="device-page">
    <!-- ===== 页面标题 ===== -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Connection /></el-icon>
        传感设备管理
      </h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加设备
        </el-button>
        <el-button type="primary" plain @click="refreshData" :loading="deviceStore.loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- ===== 统计卡片 ===== -->
    <div class="stats-grid">
      <div class="stat-card total"><div class="stat-icon"><el-icon><Connection /></el-icon></div>
        <div class="stat-info"><div class="stat-number">{{ deviceStore.stats.total }}</div><div class="stat-label">设备总数</div></div></div>
      <div class="stat-card online"><div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
        <div class="stat-info"><div class="stat-number">{{ deviceStore.stats.online }}</div><div class="stat-label">在线设备</div></div></div>
      <div class="stat-card offline"><div class="stat-icon"><el-icon><CircleClose /></el-icon></div>
        <div class="stat-info"><div class="stat-number">{{ deviceStore.stats.offline }}</div><div class="stat-label">离线设备</div></div></div>
      <div class="stat-card bind-rate"><div class="stat-icon"><el-icon><User /></el-icon></div>
        <div class="stat-info"><div class="stat-number">{{ deviceStore.stats.bindRate }}%</div><div class="stat-label">设备绑定率</div></div></div>
      <div class="stat-card alert"><div class="stat-icon"><el-icon><Warning /></el-icon></div>
        <div class="stat-info"><div class="stat-number">{{ deviceStore.stats.todayAlert }}</div><div class="stat-label">今日告警</div></div></div>
    </div>

    <!-- ===== 筛选区域 ===== -->
    <div class="filter-section">
      <div class="filter-header">
        <div class="filter-title"><el-icon><Filter /></el-icon><span>筛选条件</span></div>
        <div class="filter-actions"><el-button type="primary" plain @click="resetFilter"><el-icon><Refresh /></el-icon>重置</el-button></div>
      </div>
      <div class="filter-body">
        <el-form :model="filterForm" label-width="80px" inline>
          <el-form-item label="设备状态">
            <el-select v-model="filterForm.status" placeholder="全部" clearable style="width: 120px">
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="设备类型">
            <el-select v-model="filterForm.deviceType" placeholder="全部" clearable style="width: 130px">
              <el-option v-for="item in deviceTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="老人姓名">
            <el-input v-model="filterForm.elderName" placeholder="请输入姓名" clearable style="width: 150px" prefix-icon="Search" />
          </el-form-item>
          <el-form-item label="设备编号">
            <el-input v-model="filterForm.deviceSn" placeholder="请输入编号" clearable style="width: 150px" prefix-icon="Search" />
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- ===== 表格区域 ===== -->
    <div class="table-section">
      <div class="table-header">
        <div class="table-title"><el-icon><List /></el-icon><span>设备列表</span><el-tag size="small" type="info">共 {{ deviceStore.total }} 条</el-tag></div>
      </div>
      <el-table :data="deviceStore.deviceList" stripe style="width: 100%" row-key="id" border v-loading="deviceStore.loading">
        <el-table-column prop="deviceSn" label="设备编号" width="150" align="center">
          <template #default="{ row }"><span style="font-family: monospace; font-size: 13px">{{ row.deviceSn }}</span></template>
        </el-table-column>
        <el-table-column prop="deviceName" label="设备名称" width="140" align="center" />
        <el-table-column prop="deviceType" label="设备类型" width="110" align="center">
          <template #default="{ row }"><el-tag :type="getDeviceTypeColor(row.deviceType)" size="small">{{ row.deviceType }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="bindElder" label="绑定老人" width="100" align="center">
          <template #default="{ row }"><span v-if="row.bindElder" style="color: #409eff; font-weight: 500">{{ row.bindElder }}</span><span v-else style="color: #ccc">未绑定</span></template>
        </el-table-column>
        <el-table-column prop="status" label="设备状态" width="100" align="center">
          <template #default="{ row }"><el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="battery" label="电量" width="80" align="center">
          <template #default="{ row }"><span v-if="row.battery !== null" :style="{ color: row.battery > 30 ? '#67c23a' : '#f56c6c' }">{{ row.battery }}%</span><span v-else style="color: #ccc">-</span></template>
        </el-table-column>
        <el-table-column prop="lastOnline" label="最后在线" width="160" align="center" />
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" type="primary" link @click="handleView(row)"><el-icon><View /></el-icon>详情</el-button>
              <el-button v-if="!row.bindElder" size="small" type="success" link @click="handleBind(row)"><el-icon><Link /></el-icon>绑定</el-button>
              <el-button v-else size="small" type="warning" link @click="handleUnbind(row)"><el-icon><Link /></el-icon>解绑</el-button>
              <el-button size="small" type="danger" link @click="handleDelete(row)"><el-icon><Delete /></el-icon>删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- ===== 添加/编辑设备弹框 ===== -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="设备编号" prop="deviceSn"><el-input v-model="formData.deviceSn" placeholder="请输入设备编号" /></el-form-item>
        <el-form-item label="设备名称" prop="deviceName"><el-input v-model="formData.deviceName" placeholder="请输入设备名称" /></el-form-item>
        <el-form-item label="设备类型" prop="deviceType">
          <el-select v-model="formData.deviceType" placeholder="请选择设备类型" style="width: 100%">
            <el-option label="摄像头" value="摄像头" /><el-option label="智能手环" value="智能手环" />
            <el-option label="定位器" value="定位器" /><el-option label="紧急按钮" value="紧急按钮" />
          </el-select>
        </el-form-item>
        <el-form-item label="安装位置"><el-input v-model="formData.location" placeholder="请输入安装位置" /></el-form-item>
        <el-form-item label="告警规则"><el-input v-model="formData.alertRule" placeholder="请输入告警规则" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submitForm" :loading="submitting">确认</el-button></template>
    </el-dialog>

    <!-- ===== 绑定设备弹框 ===== -->
    <el-dialog v-model="bindDialogVisible" title="绑定设备" width="420px" :close-on-click-modal="false">
      <el-form label-width="80px">
        <el-form-item label="设备编号"><span style="font-weight: 600">{{ bindDevice?.deviceSn }}</span></el-form-item>
        <el-form-item label="设备名称"><span>{{ bindDevice?.deviceName }}</span></el-form-item>
        <el-form-item label="选择老人" required>
          <el-select v-model="bindElderId" placeholder="请选择老人" style="width: 100%">
            <el-option v-for="elder in elderStore.elderList" :key="elder.id" :label="elder.name" :value="elder.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="bindDialogVisible = false">取消</el-button><el-button type="primary" @click="submitBind" :loading="binding">确认绑定</el-button></template>
    </el-dialog>

    <!-- ===== 设备详情弹框 ===== -->
    <el-dialog v-model="detailVisible" title="设备详情" width="560px">
      <div v-if="detailData" class="detail-content">
        <div class="detail-grid">
          <div class="detail-item"><span class="detail-label">设备编号</span><span class="detail-value" style="font-family: monospace">{{ detailData.deviceSn }}</span></div>
          <div class="detail-item"><span class="detail-label">设备名称</span><span class="detail-value">{{ detailData.deviceName }}</span></div>
          <div class="detail-item"><span class="detail-label">设备类型</span><el-tag :type="getDeviceTypeColor(detailData.deviceType)" size="small">{{ detailData.deviceType }}</el-tag></div>
          <div class="detail-item"><span class="detail-label">固件版本</span><span class="detail-value">{{ detailData.firmware }}</span></div>
          <div class="detail-item"><span class="detail-label">设备状态</span><el-tag :type="getStatusType(detailData.status)" size="small">{{ getStatusText(detailData.status) }}</el-tag></div>
          <div class="detail-item"><span class="detail-label">绑定老人</span><span class="detail-value">{{ detailData.bindElder || '未绑定' }}</span></div>
          <div class="detail-item"><span class="detail-label">安装位置</span><span class="detail-value">{{ detailData.location || '-' }}</span></div>
          <div class="detail-item"><span class="detail-label">最后在线</span><span class="detail-value">{{ detailData.lastOnline }}</span></div>
          <div class="detail-item"><span class="detail-label">告警规则</span><span class="detail-value">{{ detailData.alertRule || '默认规则' }}</span></div>
        </div>
      </div>
      <template #footer><el-button @click="detailVisible = false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Connection, Refresh, Plus, View, Delete, Filter, List, Link, User, CircleCheck, CircleClose, Warning } from '@element-plus/icons-vue'
import { useDeviceStore } from '@/stores/deviceStore'
import { useElderStore } from '@/stores/elderData'
import type { DeviceVO, DeviceSaveDTO } from '@/api/device'

const deviceStore = useDeviceStore()
const elderStore = useElderStore()

// ===== 筛选 =====
const filterForm = reactive({ status: '', deviceType: '', elderName: '', deviceSn: '' })
const statusOptions = [
  { label: '全部', value: '' }, { label: '在线', value: 'ONLINE' }, { label: '离线', value: 'OFFLINE' },
  { label: '故障', value: 'FAULT' }, { label: '维修中', value: 'MAINTENANCE' },
  { label: '低电量', value: 'LOW_BATTERY' }, { label: '待激活', value: 'PENDING' },
]
const deviceTypeOptions = [
  { label: '全部', value: '' }, { label: '摄像头', value: '摄像头' }, { label: '智能手环', value: '智能手环' },
  { label: '定位器', value: '定位器' }, { label: '紧急按钮', value: '紧急按钮' },
]

// ===== 新增/编辑弹框 =====
const dialogVisible = ref(false)
const dialogTitle = ref('添加设备')
const isEdit = ref(false)
const editId = ref<number | null>(null)
const submitting = ref(false)
const formData = reactive<DeviceSaveDTO & { id?: number }>({ deviceSn: '', deviceName: '', deviceType: '', location: '', alertRule: '' })
const formRules = {
  deviceSn: [{ required: true, message: '请输入设备编号', trigger: 'blur' }],
  deviceName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
}
const formRef = ref<any>(null)

// ===== 绑定弹框 =====
const bindDialogVisible = ref(false)
const bindDevice = ref<DeviceVO | null>(null)
const bindElderId = ref<number | null>(null)
const binding = ref(false)

// ===== 详情弹框 =====
const detailVisible = ref(false)
const detailData = ref<DeviceVO | null>(null)

// ===== 工具函数 =====
function getStatusType(status: string): 'success' | 'info' | 'danger' | 'warning' | 'primary' {
  const map: Record<string, 'success' | 'info' | 'danger' | 'warning'> = {
    online: 'success', offline: 'info', fault: 'danger', maintenance: 'warning',
    low_battery: 'warning', pending: 'warning',
  }
  return map[(status || '').toLowerCase()] || 'info'
}
function getStatusText(status: string) {
  const map: Record<string, string> = {
    online: '在线', offline: '离线', fault: '故障', maintenance: '维修中',
    low_battery: '低电量', pending: '待激活',
  }
  return map[(status || '').toLowerCase()] || status
}
function getDeviceTypeColor(type: string): 'success' | 'info' | 'danger' | 'warning' | 'primary' {
  const map: Record<string, 'success' | 'info' | 'danger' | 'warning' | 'primary'> = { '摄像头': 'primary', '智能手环': 'success', '定位器': 'warning', '紧急按钮': 'danger' }
  return map[type] || 'info'
}

// ===== 加载数据 =====
async function refreshData() {
  await Promise.all([deviceStore.fetchDevices(filterForm), deviceStore.fetchStats()])
  ElMessage.success('数据已刷新')
}

// ===== 新增 =====
function handleAdd() {
  isEdit.value = false; editId.value = null; dialogTitle.value = '添加设备'
  Object.assign(formData, { deviceSn: '', deviceName: '', deviceType: '', location: '', alertRule: '' })
  formRef.value?.resetFields(); dialogVisible.value = true
}

// ===== 查看详情 =====
function handleView(row: any) { detailData.value = row; detailVisible.value = true }

// ===== 删除 =====
function handleDelete(row: any) {
  ElMessageBox.confirm(`确定要删除设备 "${row.deviceSn}" 吗？`, '删除确认', { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' })
    .then(async () => {
      await deviceStore.removeDevice(row.id)
      ElMessage.success('删除成功')
    }).catch(() => {})
}

// ===== 绑定 =====
function handleBind(row: any) { bindDevice.value = row; bindElderId.value = null; bindDialogVisible.value = true }

// ===== 解绑 =====
function handleUnbind(row: any) {
  ElMessageBox.confirm(`确定要解除设备 "${row.deviceSn}" 与 "${row.bindElder}" 的绑定吗？`, '解绑确认', { confirmButtonText: '确定解绑', cancelButtonText: '取消', type: 'warning' })
    .then(async () => {
      await deviceStore.unbindDeviceFromElder(row.id)
      ElMessage.success('解绑成功')
    }).catch(() => {})
}

// ===== 提交表单 =====
async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value && editId.value) {
      await deviceStore.editDevice(editId.value, formData)
      ElMessage.success('更新成功')
    } else {
      await deviceStore.createDevice(formData)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
  } catch { ElMessage.error('操作失败') }
  finally { submitting.value = false }
}

// ===== 提交绑定 =====
async function submitBind() {
  if (!bindElderId.value) { ElMessage.warning('请选择要绑定的老人'); return }
  binding.value = true
  try {
    await deviceStore.bindDeviceToElder(String(bindDevice.value!.id), String(bindElderId.value))
    ElMessage.success('绑定成功')
    bindDialogVisible.value = false
  } catch { ElMessage.error('绑定失败') }
  finally { binding.value = false }
}

function resetFilter() {
  filterForm.status = ''; filterForm.deviceType = ''; filterForm.elderName = ''; filterForm.deviceSn = ''
  deviceStore.fetchDevices(filterForm)
}

onMounted(async () => {
  await Promise.all([deviceStore.fetchDevices(), deviceStore.fetchStats(), elderStore.fetchElderList({ page: 1, size: 200 })])
})
</script>

<style scoped>
.device-page { padding: 20px; background-color: #f0f2f5; min-height: 100%; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { display: flex; align-items: center; gap: 10px; font-size: 22px; font-weight: 700; color: #1a1a1a; margin: 0; }
.page-title .el-icon { font-size: 26px; color: #409eff; }
.header-actions { display: flex; gap: 10px; }
.stats-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 16px; margin-bottom: 20px; }
.stat-card { display: flex; align-items: center; background: #fff; border-radius: 12px; padding: 20px 24px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); transition: all 0.3s; cursor: pointer; }
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 4px 20px rgba(0,0,0,0.1); }
.stat-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px; margin-right: 16px; flex-shrink: 0; }
.stat-card.total .stat-icon { background: #e6f7ff; color: #409eff; }
.stat-card.online .stat-icon { background: #f0f9eb; color: #67c23a; }
.stat-card.offline .stat-icon { background: #fef0f0; color: #f56c6c; }
.stat-card.bind-rate .stat-icon { background: #fdf6ec; color: #e6a23c; }
.stat-card.alert .stat-icon { background: #fef0f0; color: #f56c6c; }
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
.action-buttons { display: flex; gap: 2px; flex-wrap: nowrap; justify-content: center; align-items: center; }
.action-buttons .el-button { padding: 4px 6px; font-size: 12px; }
.action-buttons .el-button .el-icon { font-size: 14px; margin-right: 2px; }
.detail-content { padding: 4px 0; }
.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px 24px; }
.detail-item { display: flex; flex-direction: column; padding: 8px 0; border-bottom: 1px solid #f5f5f5; }
.detail-label { font-size: 13px; color: #999; margin-bottom: 2px; }
.detail-value { font-size: 14px; color: #333; word-break: break-all; }
@media (max-width: 1200px) { .stats-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px) { .page-header { flex-direction: column; align-items: flex-start; gap: 12px; } .stats-grid { grid-template-columns: repeat(2, 1fr); } .device-page { padding: 12px; } }
@media (max-width: 480px) { .stats-grid { grid-template-columns: 1fr; } .page-title { font-size: 18px; } .stat-number { font-size: 22px; } .stat-card { padding: 16px; } }
</style>
