<template>
  <div class="mobile-custom-page">
    <!-- ===== 页面标题 ===== -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Operation /></el-icon>
        移动端导航设置
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
          <el-icon><Operation /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.totalTabs }}</div>
          <div class="stat-label">底部导航项</div>
        </div>
      </div>
      <div class="stat-card menu">
        <div class="stat-icon">
          <el-icon><Menu /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.totalMenuItems }}</div>
          <div class="stat-label">侧边菜单项</div>
        </div>
      </div>
      <div class="stat-card features">
        <div class="stat-icon">
          <el-icon><Grid /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.totalFeatures }}</div>
          <div class="stat-label">功能入口</div>
        </div>
      </div>
      <div class="stat-card theme">
        <div class="stat-icon">
          <el-icon><ColorPicker /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.themeColor }}</div>
          <div class="stat-label">主题颜色</div>
        </div>
      </div>
    </div>

    <!-- ===== 配置区域 ===== -->
    <div class="config-section">
      <!-- ★★★ 底部导航（vuedraggable 拖拽排序） ★★★ -->
      <div class="config-group">
        <div class="group-header">
          <span class="group-title">底部导航栏</span>
          <span class="group-desc">拖拽左侧手柄调整顺序，配置移动端底部Tab栏</span>
        </div>
        <div class="draggable-table">
          <div class="dt-header">
            <span class="dt-cell dt-sort">排序</span>
            <span class="dt-cell dt-name">名称</span>
            <span class="dt-cell dt-icon">图标</span>
            <span class="dt-cell dt-path">路径</span>
            <span class="dt-cell dt-visible">显示</span>
          </div>
          <draggable
            v-model="tabList"
            item-key="id"
            handle=".drag-handle"
            ghost-class="dt-ghost"
            :animation="200"
            @change="updateStats"
          >
            <template #item="{ element }">
              <div class="dt-row">
                <span class="dt-cell dt-sort">
                  <el-icon class="drag-handle"><Rank /></el-icon>
                </span>
                <span class="dt-cell dt-name">
                  <el-input v-model="element.name" size="small" placeholder="名称" style="width: 100px" />
                </span>
                <span class="dt-cell dt-icon">
                  <el-select v-model="element.icon" size="small" style="width: 120px">
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
                <span class="dt-cell dt-path">
                  <el-input v-model="element.path" size="small" placeholder="/home" style="width: 120px" />
                </span>
                <span class="dt-cell dt-visible">
                  <el-switch v-model="element.visible" size="small" @change="updateStats" />
                </span>
              </div>
            </template>
          </draggable>
        </div>
      </div>

      <!-- ★★★ 侧边菜单（vuedraggable 拖拽排序） ★★★ -->
      <div class="config-group">
        <div class="group-header">
          <span class="group-title">侧边菜单</span>
          <span class="group-desc">拖拽左侧手柄调整顺序，配置移动端侧边抽屉菜单的显示项</span>
        </div>
        <div class="draggable-table">
          <div class="dt-header">
            <span class="dt-cell dt-sort">排序</span>
            <span class="dt-cell dt-name">菜单名称</span>
            <span class="dt-cell dt-icon">图标</span>
            <span class="dt-cell dt-visible">显示</span>
          </div>
          <draggable
            v-model="menuList"
            item-key="id"
            handle=".drag-handle"
            ghost-class="dt-ghost"
            :animation="200"
            @change="updateStats"
          >
            <template #item="{ element }">
              <div class="dt-row">
                <span class="dt-cell dt-sort">
                  <el-icon class="drag-handle"><Rank /></el-icon>
                </span>
                <span class="dt-cell dt-name">
                  <el-input v-model="element.name" size="small" placeholder="名称" style="width: 120px" />
                </span>
                <span class="dt-cell dt-icon">
                  <el-select v-model="element.icon" size="small" style="width: 120px">
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
                <span class="dt-cell dt-visible">
                  <el-switch v-model="element.visible" size="small" @change="updateStats" />
                </span>
              </div>
            </template>
          </draggable>
        </div>
      </div>

      <!-- ★★★ 快捷功能入口（vuedraggable 拖拽排序） ★★★ -->
      <div class="config-group">
        <div class="group-header">
          <span class="group-title">快捷功能入口</span>
          <span class="group-desc">拖拽左侧手柄调整顺序，配置移动端首页的快捷功能入口</span>
        </div>
        <div class="draggable-table">
          <div class="dt-header">
            <span class="dt-cell dt-sort">排序</span>
            <span class="dt-cell dt-fname">功能名称</span>
            <span class="dt-cell dt-icon">图标</span>
            <span class="dt-cell dt-visible">显示</span>
            <span class="dt-cell dt-badge">角标</span>
          </div>
          <draggable
            v-model="featureList"
            item-key="id"
            handle=".drag-handle"
            ghost-class="dt-ghost"
            :animation="200"
            @change="updateStats"
          >
            <template #item="{ element }">
              <div class="dt-row">
                <span class="dt-cell dt-sort">
                  <el-icon class="drag-handle"><Rank /></el-icon>
                </span>
                <span class="dt-cell dt-fname">
                  <el-input v-model="element.name" size="small" placeholder="名称" style="width: 120px" />
                </span>
                <span class="dt-cell dt-icon">
                  <el-select v-model="element.icon" size="small" style="width: 120px">
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
                <span class="dt-cell dt-visible">
                  <el-switch v-model="element.visible" size="small" @change="updateStats" />
                </span>
                <span class="dt-cell dt-badge">
                  <el-input
                    v-model="element.badge"
                    size="small"
                    placeholder="角标文字"
                    style="width: 80px"
                  />
                </span>
              </div>
            </template>
          </draggable>
        </div>
      </div>

      <!-- 主题配色 -->
      <div class="config-group">
        <div class="group-header">
          <span class="group-title">主题配色</span>
          <span class="group-desc">配置移动端的主题颜色</span>
        </div>
        <div class="theme-config">
          <div class="theme-item">
            <span class="theme-label">主色</span>
            <div class="theme-color-wrapper">
              <div class="theme-color-preview" :style="{ background: themeColors.primary }"></div>
              <el-color-picker v-model="themeColors.primary" show-alpha />
            </div>
          </div>
          <div class="theme-item">
            <span class="theme-label">辅助色</span>
            <div class="theme-color-wrapper">
              <div class="theme-color-preview" :style="{ background: themeColors.secondary }"></div>
              <el-color-picker v-model="themeColors.secondary" show-alpha />
            </div>
          </div>
          <div class="theme-item">
            <span class="theme-label">背景色</span>
            <div class="theme-color-wrapper">
              <div
                class="theme-color-preview"
                :style="{ background: themeColors.background }"
              ></div>
              <el-color-picker v-model="themeColors.background" show-alpha />
            </div>
          </div>
        </div>
      </div>

      <!-- 推送设置 -->
      <div class="config-group">
        <div class="group-header">
          <span class="group-title">推送设置</span>
          <span class="group-desc">配置移动端推送通知</span>
        </div>
        <div class="push-config">
          <div class="push-item">
            <span class="push-label">推送通知</span>
            <el-switch v-model="pushSettings.enabled" size="large" />
          </div>
          <div class="push-item">
            <span class="push-label">声音提醒</span>
            <el-switch v-model="pushSettings.sound" size="large" />
          </div>
          <div class="push-item">
            <span class="push-label">横幅通知</span>
            <el-switch v-model="pushSettings.banner" size="large" />
          </div>
          <div class="push-item">
            <span class="push-label">推送内容</span>
            <el-select v-model="pushSettings.content" style="width: 200px">
              <el-option label="仅告警通知" value="alert" />
              <el-option label="全部通知" value="all" />
              <el-option label="仅重要通知" value="important" />
            </el-select>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== 手机预览 ===== -->
    <div class="preview-section">
      <div class="preview-header">
        <span class="preview-title">📱 手机预览</span>
        <span class="preview-desc">实时预览移动端导航效果</span>
      </div>
      <div class="phone-frame">
        <div class="phone-header" :style="{ background: themeColors.primary }">
          <span class="phone-title">朝夕护行</span>
        </div>
        <!-- ★★★ 新增：侧边菜单预览（抽屉效果） ★★★ -->
        <TransitionGroup name="menu-item" tag="div" class="phone-menu-drawer">
          <div
            v-for="menu in visibleMenuItems"
            :key="menu.id"
            class="phone-menu-item"
            :style="{ borderLeftColor: themeColors.primary }"
          >
            <el-icon :size="18"><component :is="menu.icon" /></el-icon>
            <span>{{ menu.name }}</span>
          </div>
        </TransitionGroup>
        <div class="phone-body">
          <div class="phone-features">
            <TransitionGroup name="feature-item" tag="template">
              <div
                v-for="feature in visibleFeatures"
                :key="feature.id"
                class="phone-feature"
                :style="{ background: themeColors.primary + '15' }"
              >
                <el-icon :size="28"><component :is="feature.icon" /></el-icon>
                <span>{{ feature.name }}</span>
                <span v-if="feature.badge" class="phone-badge">{{ feature.badge }}</span>
              </div>
            </TransitionGroup>
          </div>
          <div class="phone-content">
            <div class="phone-placeholder">首页内容区域</div>
          </div>
        </div>
        <div class="phone-bottom" :style="{ background: themeColors.primary }">
          <TransitionGroup name="tab-item" tag="template">
            <div v-for="tab in visibleTabs" :key="tab.id" class="phone-tab">
              <el-icon :size="20"><component :is="tab.icon" /></el-icon>
              <span>{{ tab.name }}</span>
            </div>
          </TransitionGroup>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMobileConfig, saveMobileConfig, resetMobileConfig } from '@/api/custom'
// ★★★ 新增：导入 vuedraggable ★★★
import draggable from 'vuedraggable'
import {
  Operation,
  Check,
  Refresh,
  Menu,
  Grid,
  // ColorPicker,
  Rank,
  HomeFilled,
  Warning,
  Bell,
  User,
  Setting,
  DataAnalysis,
  Tools,
  Calendar,
  House,
  Connection,
  Headset,
  UserFilled,
  Avatar,
} from '@element-plus/icons-vue'

// ===== 统计数据 =====
const stats = reactive({
  totalTabs: 0,
  totalMenuItems: 0,
  totalFeatures: 0,
  themeColor: '#409eff',
})

// ===== 图标选项 =====
const iconOptions = [
  { label: '首页', value: 'HomeFilled' },
  { label: '警告', value: 'Warning' },
  { label: '铃铛', value: 'Bell' },
  { label: '用户', value: 'User' },
  { label: '设置', value: 'Setting' },
  { label: '分析', value: 'DataAnalysis' },
  { label: '工具', value: 'Tools' },
  { label: '日历', value: 'Calendar' },
  { label: '房屋', value: 'House' },
  { label: '连接', value: 'Connection' },
  { label: '耳机', value: 'Headset' },
  { label: '人员', value: 'UserFilled' },
  { label: '头像', value: 'Avatar' },
]

// ===== 默认配置（后端加载失败时的回退数据） =====
const defaultTabList = [
  { id: '1', name: '首页', icon: 'HomeFilled', path: '/home', visible: true },
  { id: '2', name: '预警', icon: 'Warning', path: '/warning', visible: true },
  { id: '3', name: '老人', icon: 'User', path: '/elder', visible: true },
  { id: '4', name: '设备', icon: 'Connection', path: '/device', visible: true },
  { id: '5', name: '我的', icon: 'UserFilled', path: '/profile', visible: true },
]

const defaultMenuList = [
  { id: '1', name: '管理主页', icon: 'HomeFilled', visible: true },
  { id: '2', name: '跌倒预警', icon: 'Warning', visible: true },
  { id: '3', name: '老人详情', icon: 'User', visible: true },
  { id: '4', name: '传感设备管理', icon: 'Connection', visible: true },
  { id: '5', name: '专家康养咨询', icon: 'Headset', visible: true },
  { id: '6', name: '个人主页', icon: 'Avatar', visible: true },
  { id: '7', name: '居家空间档案', icon: 'House', visible: false },
  { id: '8', name: '系统设置', icon: 'Setting', visible: true },
]

const defaultFeatureList = [
  { id: '1', name: '一键SOS', icon: 'Warning', visible: true, badge: '' },
  { id: '2', name: '查看预警', icon: 'Bell', visible: true, badge: '3' },
  { id: '3', name: '健康档案', icon: 'User', visible: true, badge: '' },
  { id: '4', name: '设备状态', icon: 'Connection', visible: true, badge: '' },
  { id: '5', name: '专家咨询', icon: 'Headset', visible: false, badge: '' },
  { id: '6', name: '日程安排', icon: 'Calendar', visible: false, badge: '' },
]

const defaultThemeColors = {
  primary: '#409eff',
  secondary: '#67c23a',
  background: '#f5f7fa',
}

const defaultPushSettings = {
  enabled: true,
  sound: true,
  banner: true,
  content: 'alert',
}

// ===== 底部导航（初始为空，由后端配置填充） =====
const tabList = ref<Array<{ id: string; name: string; icon: string; path: string; visible: boolean }>>([])

// ===== 侧边菜单 =====
const menuList = ref<Array<{ id: string; name: string; icon: string; visible: boolean }>>([])

// ===== 快捷功能入口 =====
const featureList = ref<Array<{ id: string; name: string; icon: string; visible: boolean; badge: string }>>([])

// ===== 主题配色 =====
const themeColors = reactive({ ...defaultThemeColors })

// ===== 推送设置 =====
const pushSettings = reactive({ ...defaultPushSettings })

// ===== 可见数据 =====
const visibleTabs = computed(() => tabList.value.filter((tab) => tab.visible))
const visibleFeatures = computed(() => featureList.value.filter((feature) => feature.visible))
// ★★★ 新增：可见菜单项（用于手机预览中的侧边菜单） ★★★
const visibleMenuItems = computed(() => menuList.value.filter((menu) => menu.visible))

// ===== 更新统计数据 =====
function updateStats() {
  stats.totalTabs = tabList.value.length
  stats.totalMenuItems = menuList.value.length
  stats.totalFeatures = featureList.value.length
  stats.themeColor = themeColors.primary
}

// ===== 应用默认配置（后端加载失败时的回退） =====
function applyDefaultConfig(): void {
  tabList.value = defaultTabList.map((tab) => ({ ...tab }))
  menuList.value = defaultMenuList.map((menu) => ({ ...menu }))
  featureList.value = defaultFeatureList.map((feature) => ({ ...feature }))
  themeColors.primary = defaultThemeColors.primary
  themeColors.secondary = defaultThemeColors.secondary
  themeColors.background = defaultThemeColors.background
  pushSettings.enabled = defaultPushSettings.enabled
  pushSettings.sound = defaultPushSettings.sound
  pushSettings.banner = defaultPushSettings.banner
  pushSettings.content = defaultPushSettings.content
}

// ===== 从后端加载移动端配置（失败返回 false，由调用方回退） =====
async function loadConfigFromApi(): Promise<boolean> {
  try {
    const res = await getMobileConfig()
    const data = res.data.data
    if (!data) {
      return false
    }
    if (Array.isArray(data.tabList)) {
      tabList.value = data.tabList.map((tab) => ({
        id: String(tab.id),
        name: tab.name ?? '',
        icon: tab.icon || 'HomeFilled',
        path: tab.path || '/',
        visible: tab.visible !== false,
      }))
    }
    if (Array.isArray(data.menuList)) {
      menuList.value = data.menuList.map((menu) => ({
        id: String(menu.id),
        name: menu.name ?? '',
        icon: menu.icon || 'Menu',
        visible: menu.visible !== false,
      }))
    }
    if (Array.isArray(data.featureList)) {
      featureList.value = data.featureList.map((feature) => ({
        id: String(feature.id),
        name: feature.name ?? '',
        icon: feature.icon || 'Grid',
        visible: feature.visible !== false,
        badge: feature.badge ?? '',
      }))
    }
    if (data.themeColors) {
      themeColors.primary = data.themeColors.primary || defaultThemeColors.primary
      themeColors.secondary = data.themeColors.secondary || defaultThemeColors.secondary
      themeColors.background = data.themeColors.background || defaultThemeColors.background
    }
    // 字段映射：后端 pushSetting -> 页面 pushSettings
    if (data.pushSetting) {
      pushSettings.enabled = data.pushSetting.enabled ?? defaultPushSettings.enabled
      pushSettings.sound = data.pushSetting.sound ?? defaultPushSettings.sound
      pushSettings.banner = data.pushSetting.banner ?? defaultPushSettings.banner
      pushSettings.content = data.pushSetting.content ?? defaultPushSettings.content
    }
    return true
  } catch (e) {
    return false
  }
}

// ===== 组装后端保存所需的数据结构 =====
function buildMobilePayload() {
  return {
    tabList: tabList.value.map((tab) => ({
      id: tab.id,
      name: tab.name,
      icon: tab.icon,
      path: tab.path,
      visible: tab.visible,
    })),
    menuList: menuList.value.map((menu) => ({
      id: menu.id,
      name: menu.name,
      icon: menu.icon,
      visible: menu.visible,
    })),
    featureList: featureList.value.map((feature) => ({
      id: feature.id,
      name: feature.name,
      icon: feature.icon,
      visible: feature.visible,
      badge: feature.badge,
    })),
    themeColors: {
      primary: themeColors.primary,
      secondary: themeColors.secondary,
      background: themeColors.background,
    },
    pushSetting: {
      enabled: pushSettings.enabled,
      sound: pushSettings.sound,
      banner: pushSettings.banner,
      content: pushSettings.content,
    },
  }
}

// ===== 保存配置（PUT 到后端） =====
async function saveConfig() {
  try {
    await saveMobileConfig(buildMobilePayload())
    ElMessage.success('移动端配置已保存')
  } catch {
    ElMessage.error('保存失败，请稍后重试')
  }
}

// ===== 恢复默认（POST 后端重置，失败时回退默认配置） =====
function resetDefault() {
  ElMessageBox.confirm('确定要恢复移动端配置到默认值吗？', '恢复默认确认', {
    confirmButtonText: '确定恢复',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      let restored = false
      try {
        await resetMobileConfig()
        restored = await loadConfigFromApi()
      } catch {
        restored = false
      }
      if (!restored) {
        applyDefaultConfig()
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
    if (tabList.value.length === 0) {
      applyDefaultConfig()
    }
    ElMessage.warning('加载后端配置失败，已保留当前配置')
  }
  updateStats()
}

// ===== 生命周期 =====
onMounted(async () => {
  const ok = await loadConfigFromApi()
  if (!ok) {
    applyDefaultConfig()
  }
  updateStats()
})
</script>

<style scoped>
.mobile-custom-page {
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
.stat-card.menu .stat-icon {
  background: #f0f9eb;
  color: #67c23a;
}
.stat-card.features .stat-icon {
  background: #fdf6ec;
  color: #e6a23c;
}
.stat-card.theme .stat-icon {
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

.config-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
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
  width: 140px;
}

.dt-fname {
  width: 140px;
}

.dt-icon {
  width: 140px;
}

.dt-path {
  width: 140px;
}

.dt-visible {
  width: 90px;
}

.dt-badge {
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

.theme-config {
  display: flex;
  gap: 32px;
  flex-wrap: wrap;
}

.theme-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.theme-label {
  font-size: 14px;
  color: #666;
  min-width: 60px;
}

.theme-color-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.theme-color-preview {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 2px solid #e8e8e8;
}

.push-config {
  display: flex;
  flex-wrap: wrap;
  gap: 20px 40px;
}

.push-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.push-label {
  font-size: 14px;
  color: #666;
  min-width: 80px;
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
  margin-bottom: 16px;
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

/* ===== 手机框架 ===== */
.phone-frame {
  width: 360px;
  max-width: 100%;
  margin: 0 auto;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.15);
  background: #fff;
}

.phone-header {
  padding: 14px 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.phone-title {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  letter-spacing: 2px;
}

/* ★★★ 新增：侧边菜单预览 ★★★ */
.phone-menu-drawer {
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  padding: 4px 0;
}

.phone-menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 20px;
  font-size: 14px;
  color: #333;
  border-left: 3px solid #409eff;
  margin: 2px 0;
  transition: all 0.3s ease;
}

.phone-menu-item .el-icon {
  color: #666;
  flex-shrink: 0;
}

/* ★★★ TransitionGroup：菜单项动画 ★★★ */
.menu-item-enter-active,
.menu-item-leave-active {
  transition: all 0.35s ease;
}

.menu-item-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.menu-item-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

.menu-item-move {
  transition: transform 0.35s ease;
}

.phone-body {
  padding: 16px;
  background: #f5f7fa;
  min-height: 300px;
}

.phone-features {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.phone-feature {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 8px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.phone-feature:hover {
  transform: scale(1.05);
}

.phone-feature .el-icon {
  color: #409eff;
  margin-bottom: 4px;
}

.phone-feature span {
  font-size: 12px;
  color: #333;
}

/* ★★★ TransitionGroup：功能入口动画 ★★★ */
.feature-item-enter-active,
.feature-item-leave-active {
  transition: all 0.35s ease;
}

.feature-item-enter-from {
  opacity: 0;
  transform: scale(0.8);
}

.feature-item-leave-to {
  opacity: 0;
  transform: scale(0.8);
}

.feature-item-move {
  transition: transform 0.35s ease;
}

.phone-badge {
  position: absolute;
  top: 4px;
  right: 8px;
  background: #f56c6c;
  color: #fff;
  font-size: 10px;
  border-radius: 50%;
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.phone-content {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  text-align: center;
  color: #999;
  min-height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.phone-placeholder {
  font-size: 14px;
  color: #ccc;
}

.phone-bottom {
  display: flex;
  padding: 8px 0;
}

.phone-tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  transition: all 0.3s;
  font-size: 11px;
}

.phone-tab:hover {
  color: #fff;
}

.phone-tab .el-icon {
  font-size: 20px;
}

/* ★★★ TransitionGroup：底部Tab动画 ★★★ */
.tab-item-enter-active,
.tab-item-leave-active {
  transition: all 0.35s ease;
}

.tab-item-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.tab-item-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

.tab-item-move {
  transition: transform 0.35s ease;
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

  .theme-config {
    flex-direction: column;
    gap: 12px;
  }

  .push-config {
    flex-direction: column;
    gap: 12px;
  }

  .phone-frame {
    width: 100%;
  }

  .phone-features {
    grid-template-columns: repeat(4, 1fr);
  }

  .mobile-custom-page {
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
    min-width: 560px;
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

  .phone-features {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
