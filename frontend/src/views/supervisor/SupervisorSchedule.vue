<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSupervisorMe, getSupervisorTeam, getSchedule, saveSchedule, deleteSchedule } from '@/api/supervisorPortal'
import type { ScheduleItem, TeamMember } from '@/api/supervisorPortal'
import StatsCard from '@/components/StatsCard.vue'

const community = ref('')
const members = ref<TeamMember[]>([])
const schedules = ref<ScheduleItem[]>([])
const weekOffset = ref(0)
const loading = ref(false)

const SHIFTS = [
  { value: 'MORNING', label: '早班', time: '08:00-12:00', color: '#67c23a' },
  { value: 'AFTERNOON', label: '中班', time: '12:00-18:00', color: '#e6a23c' },
  { value: 'NIGHT', label: '夜班', time: '18:00-24:00', color: '#909399' },
  { value: 'FULL', label: '全天', time: '08:00-18:00', color: '#409eff' },
  { value: 'REST', label: '休息', time: '-', color: '#c0c4cc' },
  { value: 'LEAVE', label: '请假', time: '-', color: '#f56c6c' },
]

function shiftLabel(v?: string) {
  const s = SHIFTS.find((x) => x.value === v)
  return s ? s.label : '-'
}
function shiftColor(v?: string) {
  const s = SHIFTS.find((x) => x.value === v)
  return s ? s.color : '#fff'
}

// 本周起始（周一）
const weekStart = computed(() => {
  const now = new Date()
  const day = now.getDay() || 7
  const monday = new Date(now)
  monday.setDate(now.getDate() - (day - 1) + weekOffset.value * 7)
  return monday
})

const weekDates = computed(() => {
  const arr: { date: string; label: string }[] = []
  for (let i = 0; i < 7; i++) {
    const d = new Date(weekStart.value)
    d.setDate(d.getDate() + i)
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const dd = String(d.getDate()).padStart(2, '0')
    arr.push({ date: `${y}-${m}-${dd}`, label: `${m}/${dd}` })
  }
  return arr
})

function fmtDate(d: Date) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${dd}`
}

function cellSchedule(caregiverId: number, date: string) {
  return schedules.value.find((s) => s.caregiverId === caregiverId && s.workDate === date)
}

// ===== 概览卡片统计 =====
const stats = computed(() => {
  const c = (v: string) => schedules.value.filter((s) => s.shift === v).length
  return [
    { iconComponent: 'Calendar', value: schedules.value.length, label: '本周班次', iconBgColor: '#e6f7ff', iconColor: '#409eff' },
    { iconComponent: 'Sunrise', value: c('MORNING'), label: '早班', iconBgColor: '#f0f9eb', iconColor: '#67c23a' },
    { iconComponent: 'Sunny', value: c('AFTERNOON') + c('FULL'), label: '中班/全天', iconBgColor: '#fdf6ec', iconColor: '#e6a23c' },
    { iconComponent: 'Moon', value: c('NIGHT'), label: '夜班', iconBgColor: '#f4f4f5', iconColor: '#909399' },
  ]
})

async function load() {
  loading.value = true
  try {
    const [me, team, sch] = await Promise.all([
      getSupervisorMe(),
      getSupervisorTeam(),
      getSchedule(weekDates.value[0].date, weekDates.value[6].date),
    ])
    community.value = me.data.data?.community || ''
    members.value = team.data.data?.members || []
    schedules.value = sch.data.data || []
  } catch {
    ElMessage.error('加载排班数据失败')
  } finally {
    loading.value = false
  }
}

// 编辑弹窗
const editVisible = ref(false)
const editForm = ref<{ caregiverId: number; caregiverName: string; date: string; shift: string; note: string }>({
  caregiverId: 0,
  caregiverName: '',
  date: '',
  shift: '',
  note: '',
})
const editing = ref(false)

function openEdit(caregiver: TeamMember, date: string) {
  const exist = cellSchedule(caregiver.id, date)
  editForm.value = {
    caregiverId: caregiver.id,
    caregiverName: caregiver.name,
    date,
    shift: exist?.shift || '',
    note: exist?.note || '',
  }
  editVisible.value = true
}

async function submitEdit() {
  if (!editForm.value.shift) {
    ElMessage.warning('请选择班次')
    return
  }
  editing.value = true
  try {
    const s = SHIFTS.find((x) => x.value === editForm.value.shift)
    const [st, et] = s?.time !== '-' ? (s?.time.split('-') as [string, string]) : ['', '']
    await saveSchedule({
      caregiverId: editForm.value.caregiverId,
      workDate: editForm.value.date,
      shift: editForm.value.shift,
      startTime: st,
      endTime: et,
      note: editForm.value.note,
    })
    ElMessage.success('排班已保存')
    editVisible.value = false
    await load()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    editing.value = false
  }
}

async function removeSchedule(item: ScheduleItem) {
  await deleteSchedule(item.id)
  ElMessage.success('已删除')
  await load()
}

function prevWeek() {
  weekOffset.value--
  load()
}
function nextWeek() {
  weekOffset.value++
  load()
}

onMounted(load)
</script>

<template>
  <div class="supervisor-schedule">
    <!-- 页面标题（左上角标明模块） -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Calendar /></el-icon>
        排班管理
      </h2>
      <div class="week-nav">
        <el-button @click="prevWeek">上一周</el-button>
        <span class="week-range">{{ weekDates[0].label }} ~ {{ weekDates[6].label }}</span>
        <el-button type="primary" @click="nextWeek">下一周</el-button>
      </div>
    </div>

    <!-- 概览卡片 -->
    <div class="stats-grid">
      <StatsCard v-for="(s, i) in stats" :key="i" v-bind="s" />
    </div>

    <!-- 课程表 / 空状态 -->
    <div class="schedule-card" v-loading="loading">
      <div class="card-title">
        <el-icon><Grid /></el-icon>
        <span>本周排班课程表 · {{ community || '本社区' }}</span>
      </div>

      <el-empty
        v-if="!loading && members.length === 0"
        description="本社区暂无护工，请先添加护工后再排班"
        :image-size="120"
        class="empty-state"
      />

      <el-table v-else :data="members" border class="schedule-table">
        <el-table-column label="陪护" width="130" fixed>
          <template #default="{ row }">
            <div class="cg-cell">
              <div class="cg-name">{{ row.name }}</div>
              <div class="cg-sub">{{ row.elderCount }} 位老人</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column v-for="d in weekDates" :key="d.date" :label="d.label" min-width="110" align="center">
          <template #default="{ row }">
            <div
              v-if="cellSchedule(row.id, d.date)"
              class="shift-cell"
              :style="{ background: shiftColor(cellSchedule(row.id, d.date)?.shift) }"
            >
              <div class="shift-label">{{ shiftLabel(cellSchedule(row.id, d.date)?.shift) }}</div>
              <div class="shift-time">{{ cellSchedule(row.id, d.date)?.startTime || '' }}</div>
              <div class="shift-ops">
                <el-button link size="small" @click.stop="openEdit(row, d.date)">改</el-button>
                <el-button link size="small" type="danger" @click.stop="removeSchedule(cellSchedule(row.id, d.date)!)">删</el-button>
              </div>
            </div>
            <div v-else class="empty-cell" @click="openEdit(row, d.date)">
              <el-icon><Plus /></el-icon>
              <span>排班</span>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="legend">
      <span v-for="s in SHIFTS" :key="s.value" class="legend-item">
        <i :style="{ background: s.color }"></i>{{ s.label }}
      </span>
    </div>

    <el-dialog v-model="editVisible" :title="`排班 · ${editForm.caregiverName} · ${editForm.date}`" width="420px">
      <el-form label-width="80px">
        <el-form-item label="班次">
          <el-radio-group v-model="editForm.shift">
            <el-radio-button v-for="s in SHIFTS" :key="s.value" :value="s.value">{{ s.label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.note" type="textarea" :rows="2" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editing" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.supervisor-schedule {
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
.week-nav {
  display: flex;
  align-items: center;
  gap: 12px;
}
.week-range {
  font-weight: 600;
  color: #409eff;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.schedule-card {
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
.empty-state {
  padding: 60px 0;
}
.schedule-table {
  width: 100%;
}
.cg-cell {
  line-height: 1.3;
}
.cg-name {
  font-weight: 600;
}
.cg-sub {
  font-size: 12px;
  color: #909399;
}
.shift-cell {
  border-radius: 6px;
  padding: 10px 4px;
  color: #fff;
  position: relative;
  min-height: 62px;
}
.shift-label {
  font-weight: 700;
  font-size: 14px;
}
.shift-time {
  font-size: 11px;
  opacity: 0.9;
}
.shift-ops {
  margin-top: 4px;
  display: flex;
  gap: 4px;
  justify-content: center;
}
.shift-ops :deep(.el-button) {
  color: #fff;
}
.empty-cell {
  min-height: 62px;
  border: 1.5px dashed #dcdfe6;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  color: #c0c4cc;
  cursor: pointer;
  transition: all 0.2s;
}
.empty-cell:hover {
  border-color: #409eff;
  color: #409eff;
  background: #ecf5ff;
}
.empty-cell .el-icon {
  font-size: 16px;
}
.empty-cell span {
  font-size: 12px;
}
.legend {
  margin-top: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}
.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}
.legend-item i {
  width: 14px;
  height: 14px;
  border-radius: 3px;
  display: inline-block;
}
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
