<template>
  <!-- 通知中心弹窗 — 数据来自 useNotificationStore -->
  <el-dialog v-model="visible" title="通知中心" width="520px" :close-on-click-modal="false">
    <div class="notification-list">
      <div
        v-for="item in notificationStore.notifications"
        :key="item.id"
        class="notification-item"
        @click="handleViewNotification(item)"
      >
        <div class="notification-dot" :class="{ unread: !item.read }"></div>
        <div class="notification-content">
          <div class="notification-title">{{ item.title }}</div>
          <div class="notification-time">{{ item.time }}</div>
        </div>
        <el-tag
          :type="item.type === 'warning' ? 'danger' : item.type === 'system' ? 'warning' : 'info'"
          size="small"
        >
          {{ item.typeLabel }}
        </el-tag>
      </div>
      <div v-if="notificationStore.notifications.length === 0" class="notification-empty">
        暂无通知
      </div>
    </div>
    <template #footer>
      <el-button type="primary" plain @click="handleMarkAllRead">全部已读</el-button>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 通知详情弹窗 -->
  <el-dialog v-model="detailVisible" title="通知详情" width="480px">
    <div v-if="currentNotification" class="notification-detail">
      <div class="detail-title">{{ currentNotification.title }}</div>
      <div class="detail-time">{{ currentNotification.time }}</div>
      <div class="detail-content">{{ currentNotification.content }}</div>
      <div class="detail-type">
        类型：<el-tag
          :type="
            currentNotification.type === 'warning'
              ? 'danger'
              : currentNotification.type === 'system'
                ? 'warning'
                : 'info'
          "
          size="small"
        >
          {{ currentNotification.typeLabel }}
        </el-tag>
      </div>
    </div>
    <template #footer>
      <el-button @click="detailVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useNotificationStore, type Notification } from '@/stores/notificationStore'

const notificationStore = useNotificationStore()

// ===== v-model 支持 =====
const visible = defineModel<boolean>({ required: true })

// ===== 通知详情弹窗 =====
const detailVisible = ref(false)
const currentNotification = ref<Notification | null>(null)

/** 查看通知详情（自动标记已读） */
function handleViewNotification(item: Notification): void {
  notificationStore.markRead(item.id)
  currentNotification.value = item
  detailVisible.value = true
}

/** 全部标记已读 */
function handleMarkAllRead(): void {
  notificationStore.markAllRead()
  ElMessage.success('已全部标记为已读')
}
</script>

<style scoped>
/* ===== 通知列表（样式与原 9profile.vue 完全一致） ===== */
.notification-list {
  max-height: 400px;
  overflow-y: auto;
}

.notification-empty {
  text-align: center;
  color: #999;
  padding: 40px 0;
  font-size: 14px;
}

.notification-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: background 0.2s;
}

.notification-item:hover {
  background: #f8f9fa;
  margin: 0 -16px;
  padding: 12px 16px;
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
  background: #dcdfe6;
}

.notification-dot.unread {
  background: #409eff;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-size: 14px;
  color: #333;
}

.notification-time {
  font-size: 12px;
  color: #999;
}

/* ===== 通知详情 ===== */
.notification-detail {
  padding: 8px 0;
}

.detail-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.detail-time {
  font-size: 13px;
  color: #999;
  margin: 8px 0 16px 0;
}

.detail-content {
  font-size: 14px;
  color: #666;
  line-height: 1.8;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.detail-type {
  margin-top: 16px;
  font-size: 13px;
  color: #999;
}
</style>
