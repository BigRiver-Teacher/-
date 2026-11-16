<template>
  <div class="sidebar-custom-page">
    <!-- ===== 页面标题 ===== -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Menu /></el-icon>
        侧边栏自定义
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

    <!-- ===== 统计卡片（使用 StatsCard 公共组件） ===== -->
    <div class="stats-grid">
      <StatsCard
        icon-component="Menu"
        :value="menuStore.totalCount"
        label="菜单总数"
        icon-bg-color="#e6f7ff"
        icon-color="#409eff"
      />
      <StatsCard
        icon-component="View"
        :value="menuStore.visibleCount"
        label="已显示"
        icon-bg-color="#f0f9eb"
        icon-color="#67c23a"
      />
      <StatsCard
        icon-component="Hide"
        :value="menuStore.hiddenCount"
        label="已隐藏"
        icon-bg-color="#fdf6ec"
        icon-color="#e6a23c"
      />
      <StatsCard
        icon-component="Grid"
        :value="Object.keys(menuStore.parentGroups).length"
        label="分组数"
        icon-bg-color="#f0f9eb"
        icon-color="#67c23a"
      />
    </div>

    <!-- ===== 配置区域 ===== -->
    <div class="config-section">
      <!-- 分组管理 -->
      <div class="config-group">
        <div class="group-header">
          <span class="group-title">菜单分组管理</span>
          <span class="group-desc">将菜单项分组显示，便于管理</span>
          <el-button size="small" type="primary" plain @click="addGroup">
            <el-icon><Plus /></el-icon>
            添加分组
          </el-button>
        </div>
        <div class="group-list">
          <div v-for="(group, groupId) in menuStore.parentGroups" :key="groupId" class="group-item">
            <div class="group-info">
              <el-icon class="drag-icon"><Rank /></el-icon>
              <el-input
                :model-value="group.name"
                size="small"
                placeholder="分组名称"
                style="width: 150px"
                @change="(val) => updateGroupName(groupId, val)"
              />
              <el-tag size="small" type="info">
                {{ getGroupItemCount(groupId) }} 项
              </el-tag>
            </div>
            <div class="group-actions">
              <el-button size="small" type="danger" link @click="deleteGroup(groupId)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 菜单配置（vuedraggable 拖拽排序） -->
      <div class="config-group">
        <div class="group-header">
          <span class="group-title">菜单配置</span>
          <span class="group-desc">拖拽排序、勾选控制显示/隐藏、选择图标</span>
        </div>

        <!-- ★ vuedraggable 自定义表格（参考 15-2dashboard.vue 的实现） -->
        <div class="draggable-table">
          <!-- 表头 -->
          <div class="dt-header">
            <span class="dt-cell dt-sort">排序</span>
            <span class="dt-cell dt-icon-col">图标</span>
            <span class="dt-cell dt-name">菜单名称</span>
            <span class="dt-cell dt-group">所属分组</span>
            <span class="dt-cell dt-visible">显示</span>
            <span class="dt-cell dt-icon-select">图标选择</span>
          </div>
          <!-- 拖拽行容器 -->
          <draggable
            v-model="draggableMenuList"
            item-key="id"
            handle=".drag-handle"
            ghost-class="dt-ghost"
            :animation="200"
            @change="onDragChange"
          >
            <template #item="{ element }">
              <div class="dt-row">
                <span class="dt-cell dt-sort">
                  <el-icon class="drag-handle"><Rank /></el-icon>
                </span>
                <span class="dt-cell dt-icon-col">
                  <el-icon :size="20"><component :is="element.icon" /></el-icon>
                </span>
                <span class="dt-cell dt-name">
                  <el-input
                    :model-value="element.name"
                    size="small"
                    placeholder="请输入菜单名称"
                    style="width: 180px"
                    @change="(val) => menuStore.updateName(element.id, val)"
                  />
                </span>
                <span class="dt-cell dt-group">
                  <el-select
                    :model-value="element.parentId || ''"
                    size="small"
                    placeholder="选择分组"
                    style="width: 130px"
                    @change="(val) => updateItemGroup(element.id, val)"
                  >
                    <el-option label="无分组" value="" />
                    <el-option
                      v-for="(group, groupId) in menuStore.parentGroups"
                      :key="groupId"
                      :label="group.name"
                      :value="groupId"
                    />
                  </el-select>
                </span>
                <span class="dt-cell dt-visible">
                  <el-switch
                    :model-value="element.visible"
                    size="small"
                    @change="() => menuStore.toggleVisible(element.id)"
                  />
                </span>
                <span class="dt-cell dt-icon-select">
                  <el-select
                    :model-value="element.icon"
                    size="small"
                    placeholder="选择图标"
                    style="width: 110px"
                    @change="(val) => menuStore.setIcon(element.id, val)"
                  >
                    <el-option
                      v-for="icon in iconOptions"
                      :key="icon.value"
                      :label="icon.label"
                      :value="icon.value"
                    >
                      <el-icon :size="16"><component :is="icon.value" /></el-icon>
                      {{ icon.label }}
                    </el-option>
                  </el-select>
                </span>
              </div>
            </template>
          </draggable>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useMenuStore } from '@/stores/menuStore'
import { getSidebarConfig, saveSidebarConfig, resetSidebarConfig } from '@/api/custom'
import draggable from 'vuedraggable'
import StatsCard from '@/components/StatsCard.vue'
import {
  Menu,
  Check,
  Refresh,
  View,
  Hide,
  Grid,
  Rank,
  Plus,
  Delete,
  HomeFilled,
  Warning,
  User,
  DataAnalysis,
  Monitor,
  Connection,
  Headset,
  UserFilled,
  Avatar,
  Bell,
  House,
  Tools,
  Calendar,
  Setting,
} from '@element-plus/icons-vue'

const menuStore = useMenuStore()

// ===== vuedraggable 使用的计算属性（支持 v-model 双向绑定） =====
const draggableMenuList = computed({
  get: () => menuStore.menuItems,
  set: (val) => {
    // vuedraggable 拖拽后触发 setter，更新 order 并持久化
    menuStore.reorderItems(val.map((item) => item.id))
  },
})

// ===== 图标选项（仅使用 Element Plus 自带图标库） =====
const iconOptions = [
  { label: '首页', value: 'HomeFilled' },
  { label: '警告', value: 'Warning' },
  { label: '用户', value: 'User' },
  { label: '分析', value: 'DataAnalysis' },
  { label: '监控', value: 'Monitor' },
  { label: '连接', value: 'Connection' },
  { label: '耳机', value: 'Headset' },
  { label: '人员', value: 'UserFilled' },
  { label: '头像', value: 'Avatar' },
  { label: '铃铛', value: 'Bell' },
  { label: '房屋', value: 'House' },
  { label: '工具', value: 'Tools' },
  { label: '日历', value: 'Calendar' },
  { label: '设置', value: 'Setting' },
  { label: '文件', value: 'Files' },
  { label: '列表', value: 'List' },
  { label: '编辑', value: 'Edit' },
  { label: '分享', value: 'Share' },
  { label: '操作', value: 'Operation' },
  { label: '文档', value: 'Document' },
  { label: '笔记本', value: 'Notebook' },
  { label: '趋势', value: 'TrendCharts' },
  { label: '直方图', value: 'Histogram' },
  { label: '定位', value: 'Location' },
]

// ===== 拖拽排序变化回调 =====
function onDragChange(): void {
  // draggableMenuList 的 setter 已自动触发，此处仅做日志
}

// ===== 获取分组下菜单项数量 =====
function getGroupItemCount(groupId: string): number {
  return menuStore.menuItems.filter((item) => item.parentId === groupId).length
}

// ===== 更新菜单项所属分组 =====
function updateItemGroup(itemId: string, groupId: string): void {
  const item = menuStore.menuItems.find((m) => m.id === itemId)
  if (item) {
    item.parentId = groupId || null
    menuStore.saveConfig()
  }
}

// ===== 更新分组名称 =====
function updateGroupName(groupId: string, name: string): void {
  if (menuStore.parentGroups[groupId]) {
    menuStore.parentGroups[groupId].name = name
    menuStore.saveConfig()
  }
}

// ★ let 替换为可变的 ref 计数器
let groupIdCounter = ref(4)

// ===== 添加分组 =====
function addGroup(): void {
  const newId = `group-${groupIdCounter.value}`
  menuStore.parentGroups[newId] = {
    name: `新分组 ${groupIdCounter.value - 3}`,
    icon: 'Grid',
    order: Object.keys(menuStore.parentGroups).length + 1,
  }
  groupIdCounter.value++
  menuStore.saveConfig()
  ElMessage.success('分组已添加')
}

// ===== 删除分组 =====
function deleteGroup(groupId: string): void {
  if (Object.keys(menuStore.parentGroups).length <= 1) {
    ElMessage.warning('至少保留一个分组')
    return
  }
  ElMessageBox.confirm(`确定要删除分组 "${menuStore.parentGroups[groupId].name}" 吗？`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      // 将属于该分组的菜单移到第一个分组
      const firstGroupId = Object.keys(menuStore.parentGroups)[0]
      menuStore.menuItems.forEach((item) => {
        if (item.parentId === groupId) {
          item.parentId = firstGroupId
        }
      })
      delete menuStore.parentGroups[groupId]
      menuStore.saveConfig()
      ElMessage.success('分组已删除')
    })
    .catch(() => {})
}

// ===== 更新分组计数器（保证新增分组 id 不冲突） =====
function updateGroupCounter(): void {
  const nums = Object.keys(menuStore.parentGroups)
    .map((id) => parseInt(id.replace(/^group-/, ''), 10))
    .filter((n) => !Number.isNaN(n))
  groupIdCounter.value = (nums.length > 0 ? Math.max(...nums) : Object.keys(menuStore.parentGroups).length) + 1
}

// ===== 组装后端保存所需的数据结构 =====
function buildSidebarPayload() {
  return {
    groups: Object.entries(menuStore.parentGroups).map(([id, group]) => ({
      id,
      name: group.name,
    })),
    items: menuStore.menuItems.map((item) => ({
      id: item.id,
      name: item.name,
      icon: item.icon,
      group: item.parentId || null,
      visible: item.visible,
    })),
  }
}

// ===== 从后端加载侧边栏配置（失败返回 false，由调用方回退） =====
async function loadConfigFromApi(): Promise<boolean> {
  try {
    const res = await getSidebarConfig()
    const data = res.data.data
    if (!data || !Array.isArray(data.groups) || !Array.isArray(data.items)) {
      return false
    }
    // 先以默认/本地配置兜底，保证后端缺失的 route 等字段完整
    if (menuStore.menuItems.length === 0) {
      menuStore.loadConfig()
    }
    const routeMap = new Map(menuStore.menuItems.map((m) => [m.id, m.route]))
    const iconMap = new Map(menuStore.menuItems.map((m) => [m.id, m.icon]))

    // 字段映射：后端 groups:[{id,name}] -> parentGroups:{[id]:{name,icon,order}}
    const groups: Record<string, { name: string; icon: string; order: number }> = {}
    data.groups.forEach((group, index) => {
      if (group && group.id != null) {
        const prev = menuStore.parentGroups[String(group.id)]
        groups[String(group.id)] = {
          name: group.name ?? '',
          icon: prev?.icon || 'Grid',
          order: index + 1,
        }
      }
    })
    menuStore.parentGroups = groups

    // 字段映射：后端 items:[{id,name,icon,group,visible}] -> menuItems（保留默认 route）
    menuStore.menuItems = data.items.map((item, index) => {
      const id = String(item.id)
      return {
        id,
        name: item.name ?? '',
        icon: item.icon || iconMap.get(id) || 'Menu',
        route: routeMap.get(id) || '/',
        visible: item.visible !== false,
        order: index + 1,
        parentId: item.group != null && item.group !== '' ? String(item.group) : null,
      }
    })
    updateGroupCounter()
    return true
  } catch (e) {
    return false
  }
}

// ===== 保存配置（PUT 到后端，并同步本地缓存） =====
async function saveConfig(): Promise<void> {
  try {
    await saveSidebarConfig(buildSidebarPayload())
    menuStore.saveConfig()
    ElMessage.success('侧边栏配置已保存')
  } catch {
    ElMessage.error('保存失败，请稍后重试')
  }
}

// ===== 恢复默认（POST 后端重置，失败时回退本地默认） =====
function resetDefault(): void {
  ElMessageBox.confirm('确定要恢复侧边栏到默认配置吗？', '恢复默认确认', {
    confirmButtonText: '确定恢复',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      let restored = false
      try {
        await resetSidebarConfig()
        restored = await loadConfigFromApi()
      } catch {
        restored = false
      }
      if (!restored) {
        menuStore.resetDefault()
      }
      updateGroupCounter()
      ElMessage.success('已恢复默认配置')
    })
    .catch(() => {})
}

// ===== 刷新（优先加载后端配置） =====
async function refreshData(): Promise<void> {
  const ok = await loadConfigFromApi()
  if (ok) {
    ElMessage.success('数据已刷新')
  } else {
    menuStore.loadConfig()
    updateGroupCounter()
    ElMessage.warning('加载后端配置失败，已使用本地配置')
  }
}

// ===== 生命周期 =====
onMounted(async () => {
  const ok = await loadConfigFromApi()
  if (!ok) {
    menuStore.loadConfig()
  }
  updateGroupCounter()
})
</script>

<style scoped>
.sidebar-custom-page {
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

/* ===== 统计卡片容器（使用 StatsCard 后保持原布局） ===== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

/* ===== 配置区域 ===== */
.config-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.config-group {
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
  flex-wrap: wrap;
}

.group-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.group-desc {
  font-size: 13px;
  color: #999;
  flex: 1;
}

.group-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.group-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.group-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.drag-icon {
  cursor: grab;
  color: #999;
  font-size: 18px;
}

.drag-icon:hover {
  color: #409eff;
}

.group-actions {
  display: flex;
  gap: 4px;
}

/* ===== vuedraggable 自定义表格（参考 15-2dashboard.vue） ===== */
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

.dt-icon-col {
  width: 80px;
}

.dt-name {
  min-width: 140px;
  flex: 1;
  justify-content: flex-start;
  padding: 0 4px;
}

.dt-group {
  width: 160px;
}

.dt-visible {
  width: 100px;
}

.dt-icon-select {
  width: 140px;
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

/* vuedraggable 拖拽占位/幽灵样式 */
.dt-ghost {
  opacity: 0.35;
  background: #e6f7ff !important;
  border-left: 3px solid #409eff;
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

  .group-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .group-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .sidebar-custom-page {
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
    min-width: 680px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 18px;
  }

  .stat-card {
    padding: 16px;
  }
}
</style>
