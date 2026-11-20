<template>
  <div class="chat-panel">
    <!-- 会话列表 -->
    <div class="chat-sidebar">
      <div class="chat-sidebar-title">会话</div>
      <div
        v-for="conv in conversations"
        :key="conv.id"
        class="chat-item"
        :class="{ active: conv.id === activeId }"
        @click="openConversation(conv)"
      >
        <div class="chat-item-head">
          <span class="chat-item-name">{{ conv.peerName }}</span>
          <el-badge v-if="conv.unreadCount" :value="conv.unreadCount" type="danger" />
        </div>
        <div class="chat-item-last">{{ conv.lastMessage || '暂无消息' }}</div>
      </div>
      <el-empty v-if="!conversations.length" description="暂无会话" :image-size="60" />
    </div>

    <!-- 消息区 -->
    <div class="chat-main">
      <div v-if="!activeId" class="chat-empty">
        <el-empty description="选择一个会话开始聊天" />
      </div>
      <template v-else>
        <div ref="msgListRef" class="chat-messages">
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="msg-row"
            :class="msg.senderId === currentUserId ? 'mine' : 'theirs'"
          >
            <div class="msg-bubble">
              <div v-if="msg.senderId !== currentUserId" class="msg-sender">{{ msg.senderName }}</div>
              <div v-if="msg.content" class="msg-content">{{ msg.content }}</div>
              <div v-for="att in msg.attachments || []" :key="att.id || att.filePath" class="msg-attachment">
                <img
                  v-if="isImage(att)"
                  :src="fileUrl(att)"
                  class="msg-img"
                  @click="previewImage(att)"
                />
                <a v-else :href="fileUrl(att)" target="_blank" class="msg-file">{{ att.fileName }}</a>
              </div>
            </div>
          </div>
        </div>
        <div class="chat-input">
          <el-upload
            :show-file-list="false"
            :auto-upload="false"
            :on-change="onFileChange"
            accept="image/*,.pdf,.doc,.docx"
          >
            <el-button :icon="Picture" circle text title="发送图片/文件" />
          </el-upload>
          <div v-if="pendingAttachments.length" class="pending-files">
            <el-tag
              v-for="(att, i) in pendingAttachments"
              :key="att.filePath"
              closable
              size="small"
              @close="pendingAttachments.splice(i, 1)"
            >
              {{ att.fileName }}
            </el-tag>
          </div>
          <el-input
            v-model="inputText"
            placeholder="输入消息..."
            @keyup.enter="handleSend"
            clearable
          />
          <el-button
            type="primary"
            :disabled="!inputText.trim() && !pendingAttachments.length"
            @click="handleSend"
          >
            发送
          </el-button>
        </div>
      </template>
    </div>

    <!-- 图片预览 -->
    <el-dialog v-model="previewVisible" width="600px" append-to-body>
      <img :src="previewUrl" style="width: 100%" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { Picture } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  getConversations,
  getMessages,
  sendMessage,
  markConversationRead,
  uploadFile,
  type ConversationVO,
  type MessageVO,
  type Attachment,
} from '@/api/chat'

const userStore = useUserStore()
const currentUserId = computed(() => userStore.userInfo?.id ?? 0)

// 可选：按会话类型过滤（CONSULT 专家咨询 / CAREGIVER 联系陪护）
const props = defineProps<{ type?: string }>()

const conversations = ref<ConversationVO[]>([])
const messages = ref<MessageVO[]>([])
const activeId = ref<number | null>(null)
const inputText = ref('')
const pendingAttachments = ref<Attachment[]>([])
const msgListRef = ref<HTMLElement | null>(null)
const previewVisible = ref(false)
const previewUrl = ref('')

async function loadConversations() {
  try {
    const res = await getConversations()
    if (res.data.code === 200) {
      let list = res.data.data || []
      if (props.type) {
        list = list.filter((c) => c.type === props.type)
      }
      conversations.value = list
    }
  } catch (e) {
    console.warn('加载会话失败', e)
  }
}

async function openConversation(conv: ConversationVO) {
  activeId.value = conv.id
  await loadMessages(conv.id)
  try {
    await markConversationRead(conv.id)
  } catch (e) {
    /* 忽略 */
  }
  conv.unreadCount = 0
}

async function loadMessages(id: number) {
  try {
    const res = await getMessages(id)
    if (res.data.code === 200) {
      messages.value = res.data.data || []
      scrollToBottom()
    }
  } catch (e) {
    console.warn('加载消息失败', e)
  }
}

function scrollToBottom() {
  nextTick(() => {
    msgListRef.value?.scrollTo({ top: msgListRef.value.scrollHeight })
  })
}

async function onFileChange(file: any) {
  const raw = file.raw as File
  if (!raw) return
  try {
    const res = await uploadFile(raw)
    if (res.data.code === 200) {
      pendingAttachments.value.push(res.data.data)
    }
  } catch (e) {
    console.warn('上传失败', e)
  }
}

async function handleSend() {
  const content = inputText.value.trim()
  if (!content && !pendingAttachments.value.length) return
  if (!activeId.value) return
  const payload = {
    content: content || '[图片]',
    contentType: pendingAttachments.value.length ? 'IMAGE' : 'TEXT',
    attachments: pendingAttachments.value,
  }
  try {
    const res = await sendMessage(activeId.value, payload)
    if (res.data.code === 200) {
      messages.value.push(res.data.data)
      inputText.value = ''
      pendingAttachments.value = []
      scrollToBottom()
      loadConversations()
    }
  } catch (e) {
    console.warn('发送失败', e)
  }
}

function isImage(att: Attachment) {
  return !!att.fileType?.startsWith('image')
}

function fileUrl(att: Attachment) {
  return '/' + (att.filePath || '').replace(/\\/g, '/')
}

function previewImage(att: Attachment) {
  previewUrl.value = fileUrl(att)
  previewVisible.value = true
}

onMounted(loadConversations)
</script>

<style scoped>
.chat-panel {
  display: flex;
  height: 100%;
  min-height: 480px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}

.chat-sidebar {
  width: 240px;
  min-width: 240px;
  border-right: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  background: #fafafa;
}

.chat-sidebar-title {
  padding: 14px 16px;
  font-weight: 600;
  border-bottom: 1px solid #f0f0f0;
  color: #333;
}

.chat-item {
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #f5f5f5;
  transition: background 0.2s;
}

.chat-item:hover {
  background: #f0f7ff;
}

.chat-item.active {
  background: #e6f4ff;
}

.chat-item-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-item-name {
  font-weight: 500;
  color: #333;
}

.chat-item-last {
  margin-top: 4px;
  font-size: 12px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f7fa;
}

.msg-row {
  display: flex;
  margin-bottom: 12px;
}

.msg-row.mine {
  justify-content: flex-end;
}

.msg-row.theirs {
  justify-content: flex-start;
}

.msg-bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  word-break: break-word;
}

.msg-row.mine .msg-bubble {
  background: #409eff;
  color: #fff;
}

.msg-sender {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
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

.msg-row.mine .msg-file {
  color: #fff;
}

.chat-input {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  border-top: 1px solid #f0f0f0;
  flex-wrap: wrap;
}

.chat-input .el-input {
  flex: 1;
  min-width: 160px;
}

.pending-files {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  width: 100%;
  order: -1;
}
</style>
