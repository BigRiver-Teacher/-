<template>
  <div class="model-page">
    <!-- ===== 页面标题 ===== -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Setting /></el-icon>
        模型参数设置
      </h2>
      <div class="header-actions">
        <el-button type="primary" @click="saveParams">
          <el-icon><Check /></el-icon>
          保存配置
        </el-button>
        <el-button type="danger" plain @click="resetDefault">
          <el-icon><Refresh /></el-icon>
          恢复默认
        </el-button>
        <el-button type="primary" plain @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- ===== 统计卡片 ===== -->
    <div class="stats-grid">
      <div class="stat-card version">
        <div class="stat-icon">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.version }}</div>
          <div class="stat-label">当前版本</div>
        </div>
      </div>
      <div class="stat-card status">
        <div class="stat-icon">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.status }}</div>
          <div class="stat-label">运行状态</div>
        </div>
      </div>
      <div class="stat-card updates">
        <div class="stat-icon">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.lastUpdate }}</div>
          <div class="stat-label">最后更新</div>
        </div>
      </div>
      <div class="stat-card versions">
        <div class="stat-icon">
          <el-icon><List /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.totalVersions }}</div>
          <div class="stat-label">历史版本</div>
        </div>
      </div>
    </div>

    <!-- ===== 参数设置区域 ===== -->
    <div class="params-section">
      <!-- 检测参数 -->
      <div class="param-group">
        <div class="group-header">
          <span class="group-title">检测参数</span>
          <span class="group-desc">控制跌倒检测的核心算法参数</span>
        </div>
        <div class="param-grid">
          <div class="param-item">
            <div class="param-label">
              <span>检测灵敏度</span>
              <el-tooltip content="数值越高，检测越敏感，误报率可能上升" placement="top">
                <el-icon><InfoFilled /></el-icon>
              </el-tooltip>
            </div>
            <div class="param-control">
              <el-slider
                v-model="params.detection.sensitivity"
                :min="1"
                :max="10"
                :step="1"
                show-stops
              />
              <span class="param-value">{{ params.detection.sensitivity }}</span>
            </div>
          </div>
          <div class="param-item">
            <div class="param-label">
              <span>加速度阈值 (m/s²)</span>
              <el-tooltip content="超过此阈值判定为异常加速度" placement="top">
                <el-icon><InfoFilled /></el-icon>
              </el-tooltip>
            </div>
            <div class="param-control">
              <el-slider v-model="params.detection.acceleration" :min="0.5" :max="5" :step="0.1" />
              <span class="param-value">{{ params.detection.acceleration }}</span>
            </div>
          </div>
          <div class="param-item">
            <div class="param-label">
              <span>姿态角度阈值 (°)</span>
              <el-tooltip content="身体倾斜超过此角度判定为异常姿态" placement="top">
                <el-icon><InfoFilled /></el-icon>
              </el-tooltip>
            </div>
            <div class="param-control">
              <el-slider v-model="params.detection.angle" :min="10" :max="60" :step="1" />
              <span class="param-value">{{ params.detection.angle }}°</span>
            </div>
          </div>
          <div class="param-item">
            <div class="param-label">
              <span>检测时间窗口 (秒)</span>
              <el-tooltip content="在此时间内持续异常才触发告警" placement="top">
                <el-icon><InfoFilled /></el-icon>
              </el-tooltip>
            </div>
            <div class="param-control">
              <el-slider v-model="params.detection.timeWindow" :min="1" :max="10" :step="0.5" />
              <span class="param-value">{{ params.detection.timeWindow }}s</span>
            </div>
          </div>
          <div class="param-item">
            <div class="param-label">
              <span>置信度阈值 (%)</span>
              <el-tooltip content="检测结果置信度超过此值才触发告警" placement="top">
                <el-icon><InfoFilled /></el-icon>
              </el-tooltip>
            </div>
            <div class="param-control">
              <el-slider v-model="params.detection.confidence" :min="50" :max="95" :step="1" />
              <span class="param-value">{{ params.detection.confidence }}%</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 告警参数 -->
      <div class="param-group">
        <div class="group-header">
          <span class="group-title">告警参数</span>
          <span class="group-desc">控制告警触发的规则</span>
        </div>
        <div class="param-grid">
          <div class="param-item">
            <div class="param-label">
              <span>告警延迟 (秒)</span>
              <el-tooltip content="检测到异常后延迟发送告警，避免误报" placement="top">
                <el-icon><InfoFilled /></el-icon>
              </el-tooltip>
            </div>
            <div class="param-control">
              <el-slider v-model="params.alert.delay" :min="0" :max="30" :step="1" />
              <span class="param-value">{{ params.alert.delay }}s</span>
            </div>
          </div>
          <div class="param-item">
            <div class="param-label">
              <span>告警冷却时间 (分钟)</span>
              <el-tooltip content="同一设备告警后需等待冷却才能再次告警" placement="top">
                <el-icon><InfoFilled /></el-icon>
              </el-tooltip>
            </div>
            <div class="param-control">
              <el-slider v-model="params.alert.cooldown" :min="1" :max="30" :step="1" />
              <span class="param-value">{{ params.alert.cooldown }}min</span>
            </div>
          </div>
          <div class="param-item">
            <div class="param-label">
              <span>连续告警次数阈值</span>
              <el-tooltip content="连续检测到异常达到此次数才触发告警" placement="top">
                <el-icon><InfoFilled /></el-icon>
              </el-tooltip>
            </div>
            <div class="param-control">
              <el-slider v-model="params.alert.threshold" :min="1" :max="5" :step="1" show-stops />
              <span class="param-value">{{ params.alert.threshold }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 设备参数 -->
      <div class="param-group">
        <div class="group-header">
          <span class="group-title">设备参数</span>
          <span class="group-desc">控制传感器设备的采集参数</span>
        </div>
        <div class="param-grid">
          <div class="param-item">
            <div class="param-label">
              <span>手部传感器采样频率 (Hz)</span>
              <el-tooltip content="传感器每秒采集的数据次数" placement="top">
                <el-icon><InfoFilled /></el-icon>
              </el-tooltip>
            </div>
            <div class="param-control">
              <el-slider v-model="params.device.sensorFrequency" :min="10" :max="100" :step="5" />
              <span class="param-value">{{ params.device.sensorFrequency }}Hz</span>
            </div>
          </div>
          <div class="param-item">
            <div class="param-label">
              <span>红外摄像头帧率 (fps)</span>
              <el-tooltip content="摄像头每秒采集的图像帧数" placement="top">
                <el-icon><InfoFilled /></el-icon>
              </el-tooltip>
            </div>
            <div class="param-control">
              <el-slider v-model="params.device.cameraFps" :min="5" :max="30" :step="1" />
              <span class="param-value">{{ params.device.cameraFps }}fps</span>
            </div>
          </div>
          <div class="param-item">
            <div class="param-label">
              <span>设备离线超时 (分钟)</span>
              <el-tooltip content="设备超过此时间未上报数据判定为离线" placement="top">
                <el-icon><InfoFilled /></el-icon>
              </el-tooltip>
            </div>
            <div class="param-control">
              <el-slider v-model="params.device.offlineTimeout" :min="5" :max="60" :step="5" />
              <span class="param-value">{{ params.device.offlineTimeout }}min</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 评估参数 -->
      <div class="param-group">
        <div class="group-header">
          <span class="group-title">评估参数</span>
          <span class="group-desc">控制风险评估的计算参数</span>
        </div>
        <div class="param-grid">
          <div class="param-item">
            <div class="param-label">
              <span>步态异常阈值 (%)</span>
              <el-tooltip content="步态数据偏离正常值超过此百分比判定为异常" placement="top">
                <el-icon><InfoFilled /></el-icon>
              </el-tooltip>
            </div>
            <div class="param-control">
              <el-slider v-model="params.evaluation.gaitThreshold" :min="10" :max="50" :step="1" />
              <span class="param-value">{{ params.evaluation.gaitThreshold }}%</span>
            </div>
          </div>
          <div class="param-item">
            <div class="param-label">
              <span>活动量异常阈值 (%)</span>
              <el-tooltip content="活动量低于正常值超过此百分比判定为异常" placement="top">
                <el-icon><InfoFilled /></el-icon>
              </el-tooltip>
            </div>
            <div class="param-control">
              <el-slider
                v-model="params.evaluation.activityThreshold"
                :min="20"
                :max="70"
                :step="1"
              />
              <span class="param-value">{{ params.evaluation.activityThreshold }}%</span>
            </div>
          </div>
          <div class="param-item">
            <div class="param-label">
              <span>风险评估周期 (天)</span>
              <el-tooltip content="自动进行风险评估的时间间隔" placement="top">
                <el-icon><InfoFilled /></el-icon>
              </el-tooltip>
            </div>
            <div class="param-control">
              <el-slider v-model="params.evaluation.riskCycle" :min="1" :max="30" :step="1" />
              <span class="param-value">{{ params.evaluation.riskCycle }}天</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== 版本历史 ===== -->
    <div class="history-section">
      <div class="history-header">
        <span class="history-title">参数版本历史</span>
        <el-button size="small" type="primary" plain @click="showHistory">
          <el-icon><Clock /></el-icon>
          查看全部
        </el-button>
      </div>
      <el-table :data="versionHistory" border style="width: 100%">
        <el-table-column prop="version" label="版本号" width="120" align="center" />
        <el-table-column prop="time" label="更新时间" width="180" align="center" />
        <el-table-column prop="user" label="操作人" width="120" align="center" />
        <el-table-column prop="description" label="变更说明" min-width="200" />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="rollbackVersion(row)">
              回滚
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Setting,
  Check,
  Refresh,
  Document,
  CircleCheck,
  Clock,
  List,
  InfoFilled,
} from '@element-plus/icons-vue'
import {
  getModelParams,
  updateModelParams,
  resetModelParams,
  getModelHistory,
  rollbackModelVersion,
  type ModelParamsVO,
  type ModelParamsResult,
  type VersionHistoryItem,
} from '@/api/model'

// ===== 统计数据 =====
const stats = reactive({
  version: 'v2.3.1',
  status: '运行中',
  lastUpdate: '2026-07-15 13:45',
  totalVersions: 12,
})

// ===== 参数配置 =====
const params = reactive({
  detection: {
    sensitivity: 7,
    acceleration: 2.5,
    angle: 45,
    timeWindow: 3,
    confidence: 80,
  },
  alert: {
    delay: 2,
    cooldown: 10,
    threshold: 2,
  },
  device: {
    sensorFrequency: 50,
    cameraFps: 15,
    offlineTimeout: 30,
  },
  evaluation: {
    gaitThreshold: 30,
    activityThreshold: 50,
    riskCycle: 7,
  },
})

// ===== 版本历史 =====
const versionHistory = ref<VersionHistoryItem[]>([])

// ===== 初始化数据（从后端获取） =====
function formatTime(t: string) {
  return t ? t.replace('T', ' ') : t
}

async function fetchParams() {
  try {
    const res = await getModelParams()
    const data = res.data.data as ModelParamsResult
    Object.assign(params.detection, data.detection)
    Object.assign(params.alert, data.alert)
    Object.assign(params.device, data.device)
    Object.assign(params.evaluation, data.evaluation)
    if (data.version) {
      stats.version = data.version.current
      stats.status = data.version.status
      stats.lastUpdate = formatTime(data.version.lastUpdate)
      stats.totalVersions = data.version.totalVersions
    }
  } catch {
    ElMessage.error('获取模型参数失败')
  }
}

async function fetchHistory() {
  try {
    const res = await getModelHistory()
    versionHistory.value = (res.data.data ?? []).map((item: VersionHistoryItem) => ({
      version: item.version,
      time: formatTime(item.time),
      user: item.user,
      description: item.description,
    }))
  } catch {
    ElMessage.error('获取版本历史失败')
  }
}

// ===== 保存参数 =====
async function saveParams() {
  try {
    await ElMessageBox.confirm('确定要保存当前参数配置吗？\n保存后将在系统运行时生效。', '保存确认', {
      confirmButtonText: '确定保存',
      cancelButtonText: '取消',
      type: 'info',
    })
    await updateModelParams({
      detection: { ...params.detection },
      evaluation: { ...params.evaluation },
      alert: { ...params.alert },
      device: { ...params.device },
    })
    ElMessage.success('参数配置保存成功')
    await fetchParams()
    await fetchHistory()
  } catch { /* 用户取消 */ }
}

// ===== 恢复默认 =====
async function resetDefault() {
  try {
    await ElMessageBox.confirm('确定要恢复所有参数到默认值吗？\n当前配置将被覆盖！', '恢复默认确认', {
      confirmButtonText: '确定恢复',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await resetModelParams()
    ElMessage.success('已恢复默认参数')
    await fetchParams()
    await fetchHistory()
  } catch { /* 用户取消 */ }
}

// ===== 查看历史 =====
async function showHistory() {
  await fetchHistory()
  ElMessage.success('已加载全部版本历史')
}

// ===== 回滚版本 =====
async function rollbackVersion(row: VersionHistoryItem) {
  try {
    await ElMessageBox.confirm(`确定要回滚到版本 ${row.version} 吗？\n当前配置将被覆盖！`, '回滚确认', {
      confirmButtonText: '确定回滚',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await rollbackModelVersion(row.version)
    ElMessage.success(`已回滚到版本 ${row.version}`)
    await fetchParams()
    await fetchHistory()
  } catch { /* 用户取消 */ }
}

// ===== 刷新数据 =====
async function refreshData() {
  await fetchParams()
  await fetchHistory()
  ElMessage.success('数据已刷新')
}

// ===== 生命周期 =====
onMounted(() => {
  fetchParams()
  fetchHistory()
})
</script>

<style scoped>
.model-page {
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

.stat-card.version .stat-icon {
  background: #e6f7ff;
  color: #409eff;
}
.stat-card.status .stat-icon {
  background: #f0f9eb;
  color: #67c23a;
}
.stat-card.updates .stat-icon {
  background: #fdf6ec;
  color: #e6a23c;
}
.stat-card.versions .stat-icon {
  background: #f0f9eb;
  color: #67c23a;
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

.params-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
}

.param-group {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.group-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}

.group-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.group-desc {
  font-size: 13px;
  color: #999;
}

.param-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px 32px;
}

.param-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.param-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #666;
}

.param-label .el-icon {
  font-size: 14px;
  color: #999;
  cursor: pointer;
}

.param-control {
  display: flex;
  align-items: center;
  gap: 12px;
}

.param-control .el-slider {
  flex: 1;
}

.param-value {
  font-size: 14px;
  font-weight: 600;
  color: #409eff;
  min-width: 40px;
  text-align: right;
}

.history-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}

.history-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th.el-table__cell) {
  background-color: #f5f7fa !important;
  color: #333;
  font-weight: 600;
  padding: 10px 0;
}

:deep(.el-table .el-table__cell) {
  padding: 8px 0;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .param-grid {
    grid-template-columns: 1fr;
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

  .model-page {
    padding: 12px;
  }

  .header-actions {
    flex-wrap: wrap;
  }

  .param-grid {
    grid-template-columns: 1fr;
  }

  .param-control {
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
}
</style>