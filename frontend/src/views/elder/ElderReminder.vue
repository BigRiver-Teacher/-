<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getElderReminder } from '@/api/elderPortal'
import type { ElderReminder } from '@/api/elderPortal'
import StatsCard from '@/components/StatsCard.vue'

const elderName = ref('')
const tasks = ref<ElderReminder['tasks']>([])
const loading = ref(false)

const TYPE_TEXT: Record<string, string> = {
  MEDICATION: '用药提醒',
  HEALTH_CHECK: '健康检查',
  REHAB_TRAINING: '康复训练',
  NUTRITION: '营养膳食',
  PSYCHOLOGY: '心理关怀',
  DAILY_VISIT: '日常探访',
  DEVICE_REPAIR: '设备维修',
  ENV_INSPECT: '环境巡检',
}
const PRIORITY: Record<string, { label: string; type: string }> = {
  urgent: { label: '紧急', type: 'danger' },
  important: { label: '重要', type: 'warning' },
  normal: { label: '普通', type: 'info' },
}
const STATUS: Record<string, { label: string; type: string }> = {
  pending: { label: '待处理', type: 'warning' },
  processing: { label: '进行中', type: 'primary' },
  completed: { label: '已完成', type: 'success' },
}

const stats = computed(() => {
  const c = (type: string) => tasks.value.filter((t) => t.taskType === type).length
  const pending = tasks.value.filter((t) => t.taskStatus === 'pending').length
  return [
    { iconComponent: 'AlarmClock', value: pending, label: '待处理', iconBgColor: '#fdf6ec', iconColor: '#e6a23c' },
    { iconComponent: 'FirstAidKit', value: c('MEDICATION'), label: '用药提醒', iconBgColor: '#fef0f0', iconColor: '#f56c6c' },
    { iconComponent: 'Search', value: c('HEALTH_CHECK'), label: '健康检查', iconBgColor: '#e6f7ff', iconColor: '#409eff' },
    { iconComponent: 'TrendCharts', value: c('REHAB_TRAINING'), label: '康复训练', iconBgColor: '#f0f9eb', iconColor: '#67c23a' },
  ]
})

function fmt(v?: string) {
  return v ? String(v).replace('T', ' ').slice(0, 16) : '-'
}

async function load() {
  loading.value = true
  try {
    const res = await getElderReminder()
    if (res.data.code === 200) {
      elderName.value = res.data.data?.elderName || ''
      tasks.value = res.data.data?.tasks || []
    }
  } catch (e) {
    ElMessage.error('加载提醒失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="elder-reminder" v-loading="loading">
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Calendar /></el-icon>
        用药与日程提醒
      </h2>
      <el-button :loading="loading" @click="load">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <div class="stats-grid">
      <StatsCard v-for="(s, i) in stats" :key="i" v-bind="s" />
    </div>

    <div class="timeline-card">
      <div class="card-title">
        <el-icon><Bell /></el-icon>
        <span>{{ elderName }} 的日程与用药提醒（共 {{ tasks.length }} 条）</span>
      </div>
      <el-timeline v-if="tasks.length">
        <el-timeline-item
          v-for="t in tasks"
          :key="t.id"
          :timestamp="fmt(t.taskDate)"
          :type="PRIORITY[t.priority]?.type || 'primary'"
          placement="top"
        >
          <div class="task-card">
            <div class="task-head">
              <el-tag :type="PRIORITY[t.priority]?.type || 'info'" size="small">
                {{ PRIORITY[t.priority]?.label || t.priority }}
              </el-tag>
              <span class="task-type">{{ TYPE_TEXT[t.taskType] || t.taskType }}</span>
              <el-tag :type="STATUS[t.taskStatus]?.type || 'info'" size="small">
                {{ STATUS[t.taskStatus]?.label || t.taskStatus }}
              </el-tag>
            </div>
            <div class="task-desc">{{ t.triggerReson || t.note || '无说明' }}</div>
          </div>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无日程提醒" />
    </div>
  </div>
</template>

<style scoped>
.elder-reminder {
  padding: 20px;
  background: #f0f2f5;
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
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.timeline-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 16px;
}
.card-title .el-icon {
  font-size: 18px;
  color: #409eff;
}
.task-card {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 12px 16px;
}
.task-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}
.task-type {
  font-weight: 600;
  color: #303133;
}
.task-desc {
  font-size: 13px;
  color: #909399;
}
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
