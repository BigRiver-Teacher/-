<template>
  <!-- 概况统计卡片 — 纯展示组件，颜色/数值全部由父组件通过 props 控制 -->
  <div class="stat-card" :style="{ background: cardBgColor }">
    <div class="stat-icon" :style="{ background: iconBgColor, color: iconColor }">
      <el-icon :size="iconSize"><component :is="iconComponent" /></el-icon>
    </div>
    <div class="stat-info">
      <div class="stat-number">{{ value }}</div>
      <div class="stat-label">{{ label }}</div>
      <div v-if="changeValue" class="stat-change" :class="changeType">
        {{ changeType === 'up' ? '↑' : '↓' }} {{ changeValue }}
        <span v-if="changeLabel" class="change-label">{{ changeLabel }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// ===== Props：父组件控制所有颜色和数值，子组件不做任何颜色逻辑判断 =====
defineProps<{
  iconComponent: any // Element Plus 图标组件（由父组件 import 后传入）
  value: string | number // 统计数值
  label: string // 统计标签（如 "专家总数"）
  iconBgColor: string // 图标区域背景色（如 "#409eff" / "#e6f7ff"）
  iconColor: string // 图标颜色（如 "#fff" / "#409eff"）
  cardBgColor?: string // 卡片背景色，默认 '#fff'
  iconSize?: number // 图标尺寸，默认 28
  changeValue?: string // 变化值（如 "+3.2%"），不传则不显示
  changeType?: 'up' | 'down' // 变化方向
  changeLabel?: string // 变化说明文字（如 "较上月增长"）
}>()
</script>

<style scoped>
/* ===== 卡片样式（与原管理主页 1management.vue 完全一致） ===== */
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
  margin-right: 16px;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 2px;
}

.stat-change {
  font-size: 13px;
  font-weight: 600;
  margin-top: 4px;
}

.stat-change.up {
  color: #67c23a;
}

.stat-change.down {
  color: #f56c6c;
}

.change-label {
  font-weight: 400;
  color: #999;
}
</style>
