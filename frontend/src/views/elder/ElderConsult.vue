<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import ChatPanel from '@/components/ChatPanel.vue'
import { getConversations, createConversation } from '@/api/chat'
import { rateConsult, getElderExperts, getElderProfile } from '@/api/elderPortal'
import type { ElderExpert } from '@/api/elderPortal'

const consultConversations = ref<{ id: number; peerName: string }[]>([])
const rateConversationId = ref<number | null>(null)
const rateValue = ref(0)
const feedback = ref('')
const submitting = ref(false)

// ===== 新建咨询 =====
const experts = ref<ElderExpert[]>([])
const elderId = ref<number | null>(null)
const newVisible = ref(false)
const newExpertId = ref<number | null>(null)
const creating = ref(false)
const chatKey = ref(0)

async function loadConversations() {
  try {
    const res = await getConversations()
    if (res.data.code === 200) {
      consultConversations.value = (res.data.data || []).filter((c) => c.type === 'CONSULT')
    }
  } catch (e) {
    console.warn('加载会话失败', e)
  }
}

async function loadExperts() {
  try {
    const [expRes, profileRes] = await Promise.all([getElderExperts(), getElderProfile()])
    if (expRes.data.code === 200) {
      experts.value = expRes.data.data || []
    }
    if (profileRes.data.code === 200) {
      elderId.value = profileRes.data.data?.id ?? null
    }
  } catch (e) {
    console.warn('加载专家列表失败', e)
  }
}

function openNew() {
  newExpertId.value = null
  newVisible.value = true
}

async function submitNew() {
  if (!newExpertId.value) {
    ElMessage.warning('请选择要咨询的专家')
    return
  }
  if (!elderId.value) {
    ElMessage.warning('未关联老人档案')
    return
  }
  const expert = experts.value.find((e) => e.id === newExpertId.value)
  creating.value = true
  try {
    await createConversation({
      type: 'CONSULT',
      elderId: elderId.value,
      serviceUserId: expert!.userId,
      serviceId: expert!.id,
    })
    ElMessage.success('咨询会话已创建')
    newVisible.value = false
    chatKey.value++ // 强制刷新 ChatPanel
    loadConversations()
  } catch (e) {
    ElMessage.error('创建会话失败')
  } finally {
    creating.value = false
  }
}

async function submitRate() {
  if (!rateConversationId.value) {
    ElMessage.warning('请选择要评价的咨询会话')
    return
  }
  if (!rateValue.value) {
    ElMessage.warning('请选择评分')
    return
  }
  submitting.value = true
  try {
    await rateConsult({
      conversationId: rateConversationId.value,
      rating: rateValue.value,
      feedback: feedback.value,
    })
    ElMessage.success('评价已提交，感谢您的反馈！')
    rateValue.value = 0
    feedback.value = ''
    rateConversationId.value = null
  } catch (e) {
    ElMessage.error('提交评价失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadConversations()
  loadExperts()
})
</script>

<template>
  <div class="elder-consult">
    <div class="rate-bar">
      <span class="rate-label">评价本次咨询</span>
      <el-select v-model="rateConversationId" placeholder="选择咨询会话" style="width: 180px" clearable>
        <el-option v-for="c in consultConversations" :key="c.id" :label="`${c.peerName}`" :value="c.id" />
      </el-select>
      <el-rate v-model="rateValue" />
      <el-button type="primary" size="small" :loading="submitting" @click="submitRate">提交评价</el-button>
      <div class="spacer"></div>
      <el-button type="primary" @click="openNew">
        <el-icon><Plus /></el-icon>
        新建咨询
      </el-button>
    </div>
    <div class="chat-wrap">
      <ChatPanel :key="chatKey" type="CONSULT" />
    </div>

    <!-- 新建咨询弹窗 -->
    <el-dialog v-model="newVisible" title="新建在线咨询" width="460px" :close-on-click-modal="false">
      <el-form label-width="80px">
        <el-form-item label="咨询专家">
          <el-select v-model="newExpertId" placeholder="请选择专家" style="width: 100%">
            <el-option v-for="e in experts" :key="e.id" :label="`${e.name} · ${e.title || '专家'}`" :value="e.id">
              <span>{{ e.name }}</span>
              <span class="opt-sub">{{ e.title }} · {{ e.specialty }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <div v-if="experts.length === 0" class="no-expert">暂无可咨询专家</div>
      </el-form>
      <template #footer>
        <el-button @click="newVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="submitNew">发起咨询</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.elder-consult {
  height: calc(100vh - 150px);
  min-height: 480px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.rate-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff;
  border-radius: 8px;
  padding: 12px 16px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.06);
  flex-wrap: wrap;
}
.rate-label {
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
}
.spacer {
  flex: 1;
}
.chat-wrap {
  flex: 1;
  min-height: 400px;
}
.opt-sub {
  float: right;
  color: #909399;
  font-size: 12px;
}
.no-expert {
  color: #909399;
  font-size: 13px;
  text-align: center;
  padding: 8px 0;
}
</style>
