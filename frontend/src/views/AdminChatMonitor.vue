<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminConversations, getAdminMessages } from '@/api/adminChat'
import type { AdminConversation, AdminMessage } from '@/api/adminChat'
import StatsCard from '@/components/StatsCard.vue'

const conversations = ref<AdminConversation[]>([])
const messages = ref<AdminMessage[]>([])
const activeConv = ref<AdminConversation | null>(null)
const loading = ref(false)
const msgLoading = ref(false)
const msgListRef = ref<HTMLElement | null>(null)
const previewVisible = ref(false)
const previewUrl = ref('')

const TYPE_TEXT: Record<string, string> = { CONSULT: '专家咨询', CAREGIVER: '联系陪护' }

const stats = computed(() => {
  const consult = conversations.value.filter((c) => c.type === 'CONSULT').length
  const caregiver = conversations.value.filter((c) => c.type === 'CAREGIVER').length
  return [
    { iconComponent: 'ChatDotRound', value: conversations.value.length, label: '总会话', iconBgColor: '#e6f7ff', iconColor: '#409eff' },
    { iconComponent: 'Star', value: consult, label: '专家咨询', iconBgColor: '#f0f9eb', iconColor: '#67c23a' },
    { iconComponent: 'User', value: caregiver, label: '联系陪护', iconBgColor: '#fdf6ec', iconColor: '#e6a23c' },
    { iconComponent: 'DataAnalysis', value: conversations.value.reduce((s, c) => s + (c.lastMessage ? 1 : 0), 0), label: '有消息会话', iconBgColor: '#fef0f0', iconColor: '#f56c6c' },
  ]
})

async function load() {
  loading.value = true
  try {
    const res = await getAdminConversations()
    if (res.data.code === 200) {
      conversations.value = res.data.data || []
    }
  } catch (e) {
    ElMessage.error('加载会话列表失败')
  } finally {
    loading.value = false
  }
}

async function openConversation(conv: AdminConversation) {
  activeConv.value = conv
  msgLoading.value = true
  try {
    const res = await getAdminMessages(conv.id)
    if (res.data.code === 200) {
      messages.value = res.data.data || []
      scrollToBottom()
    }
  } catch (e) {
    ElMessage.error('加载消息失败')
  } finally {
    msgLoading.value = false
  }
}

function scrollToBottom() {
  nextTick(() => {
    msgListRef.value?.scrollTo({ top: msgListRef.value.scrollHeight })
  })
}

function isFamily(msg: AdminMessage) {
  return activeConv.value ? msg.senderId === activeConv.value.familyUserId : msg.senderRole === 'elder'
}

function fmt(v?: string) {
  return v ? String(v).replace('T', ' ').slice(0, 16) : ''
}

function isImage(att: { fileType?: string }) {
  return !!att.fileType?.startsWith('image')
}

function fileUrl(att: { filePath?: string }) {
  return '/' + (att.filePath || '').replace(/\\/g, '/')
}

function previewImage(att: { filePath?: string }) {
  previewUrl.value = fileUrl(att)
  previewVisible.value = true
}

onMounted(load)
</script>

<template>
  <div class="admin-chat-monitor">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><ChatDotRound /></el-icon>
        对话监管
      </h2>
      <el-button :loading="loading" @click="load">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- 概览卡片 -->
    <div class="stats-grid">
      <StatsCard v-for="(s, i) in stats" :key="i" v-bind="s" />
    </div>

    <!-- 会话列表 + 消息流 -->
    <div class="chat-layout">
      <div class="conv-sidebar">
        <div class="conv-title">全部会话（{{ conversations.length }}）</div>
        <div
          v-for="conv in conversations"
          :key="conv.id"
          class="conv-item"
          :class="{ active: activeConv?.id === conv.id }"
          @click="openConversation(conv)"
        >
          <div class="conv-head">
            <el-tag :type="conv.type === 'CONSULT' ? 'success' : 'warning'" size="small">
              {{ TYPE_TEXT[conv.type] || conv.type }}
            </el-tag>
            <span class="conv-elder">{{ conv.elderName || '-' }}</span>
          </div>
          <div class="conv-peer">{{ conv.familyName }} ↔ {{ conv.serviceName }}</div>
          <div class="conv-last">{{ conv.lastMessage || '暂无消息' }}</div>
          <div class="conv-time">{{ fmt(conv.lastMessageTime) }}</div>
        </div>
        <el-empty v-if="!conversations.length" description="暂无会话" :image-size="80" />
      </div>

      <div class="conv-main">
        <div v-if="!activeConv" class="conv-empty">
          <el-empty description="选择一个会话查看消息" />
        </div>
        <template v-else>
          <div class="conv-header">
            <span class="conv-header-title">{{ activeConv.elderName || '-' }} · {{ TYPE_TEXT[activeConv.type] || activeConv.type }}</span>
            <span class="conv-header-peer">{{ activeConv.familyName }} ↔ {{ activeConv.serviceName }}</span>
          </div>
          <div ref="msgListRef" class="msg-list" v-loading="msgLoading">
            <div v-for="msg in messages" :key="msg.id" class="msg-row" :class="isFamily(msg) ? 'family' : 'service'">
              <div class="msg-bubble">
                <div class="msg-sender">{{ msg.senderName }} · {{ fmt(msg.createTime) }}</div>
                <div v-if="msg.content" class="msg-content">{{ msg.content }}</div>
                <div v-for="att in msg.attachments || []" :key="att.id || att.filePath" class="msg-attachment">
                  <img v-if="isImage(att)" :src="fileUrl(att)" class="msg-img" @click="previewImage(att)" />
                  <a v-else :href="fileUrl(att)" target="_blank" class="msg-file">{{ att.fileName }}</a>
                </div>
              </div>
            </div>
            <el-empty v-if="!msgLoading && messages.length === 0" description="暂无消息" :image-size="80" />
          </div>
        </template>
      </div>
    </div>

    <el-dialog v-model="previewVisible" width="600px" append-to-body>
      <img :src="previewUrl" style="width: 100%" />
    </el-dialog>
  </div>
</template>

<style scoped>
.admin-chat-monitor {
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
.chat-layout {
  display: flex;
  height: calc(100vh - 300px);
  min-height: 460px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.conv-sidebar {
  width: 320px;
  min-width: 320px;
  border-right: 1px solid #f0f0f0;
  background: #fafafa;
  overflow-y: auto;
}
.conv-title {
  padding: 14px 16px;
  font-weight: 600;
  color: #333;
  border-bottom: 1px solid #f0f0f0;
}
.conv-item {
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #f5f5f5;
  transition: background 0.2s;
}
.conv-item:hover {
  background: #f0f7ff;
}
.conv-item.active {
  background: #e6f4ff;
}
.conv-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.conv-elder {
  font-weight: 600;
  color: #303133;
}
.conv-peer {
  font-size: 13px;
  color: #606266;
  margin-bottom: 4px;
}
.conv-last {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.conv-time {
  margin-top: 4px;
  font-size: 11px;
  color: #c0c4cc;
}
.conv-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.conv-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
.conv-header {
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.conv-header-title {
  font-weight: 600;
  color: #303133;
}
.conv-header-peer {
  font-size: 13px;
  color: #909399;
}
.msg-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f7fa;
}
.msg-row {
  display: flex;
  margin-bottom: 12px;
}
.msg-row.family {
  justify-content: flex-start;
}
.msg-row.service {
  justify-content: flex-end;
}
.msg-bubble {
  max-width: 65%;
  padding: 10px 14px;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  word-break: break-word;
}
.msg-row.service .msg-bubble {
  background: #409eff;
  color: #fff;
}
.msg-sender {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}
.msg-row.service .msg-sender {
  color: rgba(255, 255, 255, 0.85);
}
.msg-content {
  line-height: 1.5;
  white-space: pre-wrap;
}
.msg-attachment {
  margin-top: 8px;
}
.msg-img {
  max-width: 200px;
  max-height: 200px;
  border-radius: 6px;
  cursor: pointer;
  display: block;
}
.msg-file {
  color: #409eff;
  text-decoration: none;
  font-size: 13px;
}
.msg-row.service .msg-file {
  color: #fff;
}
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .conv-sidebar {
    width: 240px;
    min-width: 240px;
  }
}
</style>
