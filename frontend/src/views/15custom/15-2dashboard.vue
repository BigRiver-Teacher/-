<template>
  <div class="dashboard-custom-page">
    <!-- ===== 页面标题 ===== -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Grid /></el-icon>
        首页看板布局
      </h2>
      <div class="header-actions">
        <el-button type="primary" @click="saveConfig">
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
      <div class="stat-card total">
        <div class="stat-icon">
          <el-icon><Grid /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.total }}</div>
          <div class="stat-label">卡片总数</div>
        </div>
      </div>
      <div class="stat-card visible">
        <div class="stat-icon">
          <el-icon><View /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.visible }}</div>
          <div class="stat-label">已显示</div>
        </div>
      </div>
      <div class="stat-card hidden">
        <div class="stat-icon">
          <el-icon><Hide /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.hidden }}</div>
          <div class="stat-label">已隐藏</div>
        </div>
      </div>
      <div class="stat-card presets">
        <div class="stat-icon">
          <el-icon><Files /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.presetCount }}</div>
          <div class="stat-label">布局预设</div>
        </div>
      </div>
    </div>

    <!-- ===== 布局预设 ===== -->
    <div class="preset-section">
      <div class="preset-header">
        <span class="preset-title">布局预设</span>
        <span class="preset-desc">选择预设布局快速配置看板</span>
      </div>
      <div class="preset-list">
        <div
          v-for="preset in presets"
          :key="preset.id"
          class="preset-item"
          :class="{ active: currentPreset === preset.id }"
          @click="applyPreset(preset.id)"
        >
          <div class="preset-preview">
            <div class="preview-grid" :class="preset.layout">
              <span v-for="n in preset.count" :key="n" class="preview-cell"></span>
            </div>
          </div>
          <div class="preset-name">{{ preset.name }}</div>
          <div class="preset-desc">{{ preset.desc }}</div>
        </div>
      </div>
    </div>

    <!-- ===== 卡片配置（vuedraggable 拖拽排序） ===== -->
    <div class="config-section">
      <div class="config-header">
        <span class="config-title">卡片配置</span>
        <span class="config-desc">拖拽左侧手柄调整顺序，配置卡片的显示、列宽和颜色</span>
      </div>

      <!-- ★★★ 新增：使用 vuedraggable 包裹自定义表格，替换原 el-table ★★★ -->
      <div class="draggable-table">
        <!-- 表头 -->
        <div class="dt-header">
          <span class="dt-cell dt-sort">排序</span>
          <span class="dt-cell dt-name">卡片名称</span>
          <span class="dt-cell dt-icon">图标</span>
          <span class="dt-cell dt-type">卡片类型</span>
          <span class="dt-cell dt-visible">显示</span>
          <span class="dt-cell dt-colspan">列宽</span>
          <span class="dt-cell dt-color">主题色</span>
        </div>
        <!-- 拖拽行容器 -->
        <draggable
          v-model="cardList"
          item-key="id"
          handle=".drag-handle"
          ghost-class="dt-ghost"
          :animation="200"
          @change="onCardOrderChange"
        >
          <template #item="{ element }">
            <div class="dt-row">
              <span class="dt-cell dt-sort">
                <el-icon class="drag-handle"><Rank /></el-icon>
              </span>
              <span class="dt-cell dt-name">
                <el-input
                  v-model="element.name"
                  size="small"
                  placeholder="请输入卡片名称"
                  style="width: 160px"
                  @change="onCardFieldChange"
                />
              </span>
              <span class="dt-cell dt-icon">
                <el-icon :size="20"><component :is="element.icon" /></el-icon>
              </span>
              <span class="dt-cell dt-type">
                <el-tag :type="element.type === '统计' ? 'success' : 'primary'" size="small">
                  {{ element.type }}
                </el-tag>
              </span>
              <span class="dt-cell dt-visible">
                <el-switch
                  v-model="element.visible"
                  size="small"
                  @change="onCardFieldChange"
                />
              </span>
              <span class="dt-cell dt-colspan">
                <el-select
                  v-model="element.colspan"
                  size="small"
                  style="width: 100px"
                  @change="onCardFieldChange"
                >
                  <el-option label="1列" :value="1" />
                  <el-option label="2列" :value="2" />
                  <el-option label="3列" :value="3" />
                  <el-option label="4列" :value="4" />
                </el-select>
              </span>
              <span class="dt-cell dt-color">
                <div class="color-picker-wrapper">
                  <div class="color-dot" :style="{ background: element.color }"></div>
                  <el-color-picker
                    v-model="element.color"
                    size="small"
                    show-alpha
                    @change="onCardFieldChange"
                  />
                </div>
              </span>
            </div>
          </template>
        </draggable>
      </div>
      <!-- ★★★ 拖拽表格结束 ★★★ -->
    </div>

    <!-- ===== 预览区域 ===== -->
    <div class="preview-section">
      <div class="preview-header">
        <span class="preview-title">实时预览</span>
        <span class="preview-desc">以下为配置后的看板效果</span>
      </div>
      <!-- ★★★ 修改：使用 TransitionGroup 实现卡片平滑过渡 ★★★ -->
      <TransitionGroup name="preview-card" tag="div" class="preview-grid-container">
        <div
          v-for="card in visibleCards"
          :key="card.id"
          class="preview-card"
          :style="{ gridColumn: `span ${card.colspan}` }"
        >
          <div class="preview-card-icon" :style="{ background: card.color }">
            <el-icon :size="24"><component :is="card.icon" /></el-icon>
          </div>
          <div class="preview-card-info">
            <div class="preview-card-number">{{ card.previewValue }}</div>
            <div class="preview-card-label">{{ card.name }}</div>
          </div>
        </div>
      </TransitionGroup>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useDashboardStore } from '@/stores/dashboard'
import { getDashboardConfig, saveDashboardConfig, resetDashboardConfig } from '@/api/custom'
// ★★★ 新增：导入 vuedraggable ★★★
import draggable from 'vuedraggable'
import {
  Grid,
  Check,
  Refresh,
  View,
  Hide,
  Files,
  Rank,
  Document,
  User,
  Warning,
  Timer,
  Clock,
  CircleCheck,
  StarFilled,
} from '@element-plus/icons-vue'

// ===== 使用 Store =====
const dashboardStore = useDashboardStore()

// ===== 统计数据 =====
const stats = reactive({
  total: 0,
  visible: 0,
  hidden: 0,
  presetCount: 0,
})

// ===== 当前预设 =====
const currentPreset = ref('standard')

// ===== 布局预设 =====
const presets = ref([
  { id: 'standard', name: '标准布局', desc: '4列统计卡片', layout: 'standard', count: 4 },
  { id: 'compact', name: '紧凑布局', desc: '2列统计卡片', layout: 'compact', count: 2 },
  { id: 'data', name: '数据优先', desc: '6列统计卡片', layout: 'data', count: 6 },
  { id: 'simple', name: '简洁布局', desc: '3列统计卡片', layout: 'simple', count: 3 },
])

// ★★★ 修改：cardList 改为可写 computed，支持 vuedraggable 的 v-model ★★★
const cardList = computed({
  get: () => dashboardStore.cards,
  set: (val) => {
    // vuedraggable 拖拽后会调用此 setter，将新顺序写入 store
    dashboardStore.cards = val
    dashboardStore.saveConfig()
    updateStats()
  },
})

// ===== 可见卡片 =====
const visibleCards = computed(() => {
  return cardList.value.filter((card) => card.visible)
})

// ===== 更新统计数据 =====
function updateStats() {
  const list = cardList.value
  stats.total = list.length
  stats.visible = list.filter((item) => item.visible).length
  stats.hidden = list.filter((item) => !item.visible).length
  stats.presetCount = presets.value.length
}

// ★★★ 新增：拖拽排序变化回调 ★★★
function onCardOrderChange() {
  // cardList 的 setter 已通过 v-model 自动触发，此处做额外处理
  updateStats()
}

// ★★★ 新增：卡片字段变更（开关、列宽、颜色等）统一回调 ★★★
function onCardFieldChange() {
  dashboardStore.saveConfig()
  updateStats()
}

// ===== 卡片操作（调用Store方法） =====
function toggleVisible(row) {
  dashboardStore.toggleVisible(row.id)
  updateStats()
}

function setColspan(row, value) {
  dashboardStore.setColspan(row.id, value)
  updateStats()
}

function setColor(row, color) {
  dashboardStore.updateCard(row.id, { color: color })
}

function setName(row, name) {
  dashboardStore.updateCard(row.id, { name: name })
}

// ===== 应用预设 =====
function applyPreset(id) {
  currentPreset.value = id
  const preset = presets.value.find((p) => p.id === id)
  const colMap = { standard: 1, compact: 2, data: 1, simple: 1 }
  const visibleMap = { standard: 4, compact: 2, data: 6, simple: 3 }

  cardList.value.forEach((card, index) => {
    dashboardStore.updateCard(card.id, {
      colspan: colMap[id] || 1,
      visible: index < visibleMap[id],
    })
  })
  updateStats()
  ElMessage.success(`已切换到 ${preset.name}`)
}

// ===== 从后端加载看板配置（失败返回 false，由调用方回退） =====
async function loadConfigFromApi(): Promise<boolean> {
  try {
    const res = await getDashboardConfig()
    const data = res.data.data
    if (!data || !Array.isArray(data.cards)) {
      return false
    }
    // 字段映射：后端卡片无 value 字段，用 previewValue 兜底
    dashboardStore.cards = data.cards.map((card) => ({
      id: String(card.id),
      name: card.name ?? '',
      icon: card.icon || 'Grid',
      type: card.type || '统计',
      visible: card.visible !== false,
      colspan: card.colspan || 1,
      color: card.color || '#409eff',
      previewValue: card.previewValue ?? 0,
      value: card.previewValue ?? 0,
    }))
    return true
  } catch (e) {
    return false
  }
}

// ===== 组装后端保存所需的数据结构 =====
function buildDashboardPayload() {
  return {
    cards: dashboardStore.cards.map((card) => ({
      id: card.id,
      name: card.name,
      icon: card.icon,
      type: card.type,
      visible: card.visible,
      colspan: card.colspan,
      color: card.color,
      previewValue: card.previewValue,
    })),
  }
}

// ===== 保存配置（PUT 到后端，并同步 localStorage 缓存） =====
async function saveConfig() {
  try {
    await saveDashboardConfig(buildDashboardPayload())
    dashboardStore.saveConfig()
    ElMessage.success('看板配置已保存')
  } catch {
    ElMessage.error('保存失败，请稍后重试')
  }
}

// ===== 恢复默认（POST 后端重置，失败时回退本地默认） =====
function resetDefault() {
  ElMessageBox.confirm('确定要恢复看板到默认配置吗？', '恢复默认确认', {
    confirmButtonText: '确定恢复',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      let restored = false
      try {
        await resetDashboardConfig()
        restored = await loadConfigFromApi()
      } catch {
        restored = false
      }
      if (!restored) {
        dashboardStore.resetDefault()
      }
      updateStats()
      ElMessage.success('已恢复默认配置')
    })
    .catch(() => {})
}

// ===== 刷新（优先加载后端配置） =====
async function refreshData() {
  const ok = await loadConfigFromApi()
  if (ok) {
    ElMessage.success('数据已刷新')
  } else {
    dashboardStore.loadConfig()
    ElMessage.warning('加载后端配置失败，已使用本地配置')
  }
  updateStats()
}

// ===== 生命周期 =====
onMounted(async () => {
  const ok = await loadConfigFromApi()
  if (!ok) {
    dashboardStore.loadConfig()
  }
  updateStats()
})
</script>

<style scoped>
.dashboard-custom-page {
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
.stat-card.visible .stat-icon {
  background: #f0f9eb;
  color: #67c23a;
}
.stat-card.hidden .stat-icon {
  background: #fdf6ec;
  color: #e6a23c;
}
.stat-card.presets .stat-icon {
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

.preset-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.preset-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}

.preset-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.preset-desc {
  font-size: 13px;
  color: #999;
}

.preset-list {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.preset-item {
  width: 140px;
  padding: 12px;
  border-radius: 10px;
  border: 2px solid #e8e8e8;
  cursor: pointer;
  text-align: center;
  transition: all 0.3s;
}

.preset-item:hover {
  border-color: #409eff;
  transform: translateY(-2px);
}

.preset-item.active {
  border-color: #409eff;
  background: #e6f7ff;
}

.preset-preview {
  padding: 8px;
  background: #f8f9fa;
  border-radius: 6px;
  margin-bottom: 8px;
}

.preview-grid {
  display: grid;
  gap: 4px;
  min-height: 40px;
}

.preview-grid.standard {
  grid-template-columns: repeat(4, 1fr);
}
.preview-grid.compact {
  grid-template-columns: repeat(2, 1fr);
}
.preview-grid.data {
  grid-template-columns: repeat(3, 1fr);
}
.preview-grid.simple {
  grid-template-columns: repeat(3, 1fr);
}

.preview-cell {
  height: 16px;
  background: #d0d0d0;
  border-radius: 4px;
}

.preset-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.preset-desc {
  font-size: 12px;
  color: #999;
}

.config-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.config-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}

.config-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.config-desc {
  font-size: 13px;
  color: #999;
}

/* ★★★ 新增：vuedraggable 自定义表格样式 ★★★ */
.draggable-table {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}

.dt-header {
  display: flex;
  align-items: center;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  padding: 10px 0;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.dt-row {
  display: flex;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  padding: 8px 0;
  transition: background 0.2s;
}

.dt-row:last-child {
  border-bottom: none;
}

.dt-row:hover {
  background: #f5f7fa;
}

.dt-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.dt-sort {
  width: 60px;
}

.dt-name {
  min-width: 140px;
  flex: 1;
  justify-content: flex-start;
  padding: 0 4px;
}

.dt-icon {
  width: 80px;
}

.dt-type {
  width: 140px;
}

.dt-visible {
  width: 90px;
}

.dt-colspan {
  width: 130px;
}

.dt-color {
  width: 110px;
}

/* 拖拽手柄 */
.drag-handle {
  cursor: grab;
  color: #999;
  font-size: 18px;
  transition: color 0.2s;
}

.drag-handle:hover {
  color: #409eff;
}

.drag-handle:active {
  cursor: grabbing;
}

/* vuedraggable 拖拽时的占位/幽灵样式 */
.dt-ghost {
  opacity: 0.35;
  background: #e6f7ff !important;
  border-left: 3px solid #409eff;
}

.color-picker-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: center;
}

.color-dot {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 1px solid #e8e8e8;
}

.preview-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.preview-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}

.preview-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.preview-desc {
  font-size: 13px;
  color: #999;
}

.preview-grid-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.preview-card {
  display: flex;
  align-items: center;
  background: #f8f9fa;
  border-radius: 10px;
  padding: 16px 20px;
  transition: all 0.3s ease;
  min-height: 80px;
}

.preview-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

/* ★★★ 新增：TransitionGroup 卡片过渡动画 ★★★ */
.preview-card-enter-active,
.preview-card-leave-active {
  transition: all 0.4s ease;
}

.preview-card-enter-from {
  opacity: 0;
  transform: scale(0.9) translateY(-10px);
}

.preview-card-leave-to {
  opacity: 0;
  transform: scale(0.9) translateY(10px);
}

.preview-card-move {
  transition: transform 0.4s ease;
}

.preview-card-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 16px;
  flex-shrink: 0;
}

.preview-card-number {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.2;
}

.preview-card-label {
  font-size: 14px;
  color: #999;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .preview-grid-container {
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

  .preset-list {
    flex-wrap: wrap;
  }

  .preset-item {
    width: 100%;
  }

  .preview-grid-container {
    grid-template-columns: 1fr;
  }

  .dashboard-custom-page {
    padding: 12px;
  }

  .header-actions {
    flex-wrap: wrap;
  }

  /* 小屏幕下表格横向滚动 */
  .draggable-table {
    overflow-x: auto;
  }

  .dt-header,
  .dt-row {
    min-width: 760px;
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
