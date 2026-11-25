<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, QuestionFilled } from '@element-plus/icons-vue'
import type { UploadFile, UploadInstance } from 'element-plus'
import {
  detectVideoAsync,
  getDetectTask,
  getModelStatus,
  resolveOutputUrl,
  type FallDetectResult,
  type ModelStatus,
} from '@/api/fallDetection'
import { createAlert } from '@/api/alert'
import { getElderList, type ElderVO } from '@/api/elder'

const props = defineProps<{ modelValue: boolean }>()
const emit = defineEmits<{ (e: 'update:modelValue', value: boolean): void }>()

const visible = computed({
  get: () => props.modelValue,
  set: (v: boolean) => emit('update:modelValue', v),
})

/** 服务状态：null 表示还没查过 */
const status = ref<ModelStatus | null>(null)
const statusError = ref('')
const checking = ref(false)

const uploadRef = ref<UploadInstance>()
const fileList = ref<UploadFile[]>([])
const selectedFile = ref<File | null>(null)
const confidence = ref(0.4)

const detecting = ref(false)
const uploadPercent = ref(0)
const result = ref<FallDetectResult | null>(null)
const elapsedTip = ref('')
/** 浏览器无法解码该视频时置位，用于提示改为下载 */
const videoError = ref(false)

/** 老人列表：检测到跌倒时把结果挂到具体老人名下 */
const elders = ref<ElderVO[]>([])
const selectedElderId = ref<number | undefined>(undefined)
/** 本次检测写入告警的条数 */
const alertsWritten = ref(0)
const writingAlerts = ref(false)

/** 模型是否可用：必须 loaded=true */
const modelReady = computed(() => status.value?.loaded === true)
const canDetect = computed(
  () => !!selectedFile.value && modelReady.value && !detecting.value,
)
const outputUrl = computed(() => resolveOutputUrl(result.value?.output_url))
const events = computed(() => result.value?.events ?? [])

/** 模型缺失时，把缺失的具体路径提示出来，便于排查 */
const missingHint = computed(() => {
  const s = status.value
  if (!s || s.loaded) return ''
  const missing: string[] = []
  if (s.yolo_exists === false) missing.push('YOLO 姿态权重')
  if (s.lstm_exists === false) missing.push('LSTM 检查点')
  if (!missing.length) return '模型未加载完成，请查看算法服务日志。'
  return `缺少：${missing.join('、')}。请按 algorithm/README.md 放置权重后重启算法服务。`
})

async function checkStatus() {
  checking.value = true
  statusError.value = ''
  try {
    const res = await getModelStatus()
    status.value = res.data.data
  } catch {
    status.value = null
    statusError.value = '检测服务未启动（8001 无响应）。请先启动 algorithm 服务后重试。'
  } finally {
    checking.value = false
  }
}

function handleChange(file: UploadFile) {
  selectedFile.value = (file.raw as File) ?? null
  result.value = null
  uploadPercent.value = 0
}

function handleRemove() {
  selectedFile.value = null
  result.value = null
  uploadPercent.value = 0
}

function resetAll() {
  fileList.value = []
  selectedFile.value = null
  result.value = null
  uploadPercent.value = 0
  elapsedTip.value = ''
  videoError.value = false
  alertsWritten.value = 0
  uploadRef.value?.clearFiles()
}

/** 轮询异步任务直到结束，返回检测结果 */
async function waitForTask(taskId: string, startedAt: number): Promise<FallDetectResult> {
  const POLL_INTERVAL = 2000
  const MAX_WAIT = 20 * 60 * 1000
  for (;;) {
    await new Promise((resolve) => setTimeout(resolve, POLL_INTERVAL))
    const res = await getDetectTask(taskId)
    const task = res.data?.data
    if (!task) throw new Error('检测任务不存在或已失效')
    if (task.status === 'done') {
      if (!task.result) throw new Error('检测已完成但没有返回结果')
      // 异步接口把标注视频地址放在任务顶层，同步接口放在 result 里，这里统一
      const merged: FallDetectResult = { ...task.result }
      if (!merged.output_url && task.output_url) {
        merged.output_url = task.output_url
        merged.output_file = task.output_url.split('/').pop()
      }
      return merged
    }
    if (task.status === 'failed') throw new Error(task.error || '检测任务执行失败')
    const seconds = Math.round((Date.now() - startedAt) / 1000)
    elapsedTip.value =
      task.status === 'pending'
        ? `任务排队中…已等待 ${seconds}s`
        : `模型推理中…已用时 ${seconds}s（视频越长越久，可保持窗口打开）`
    if (Date.now() - startedAt > MAX_WAIT) throw new Error('检测超时（超过 20 分钟）')
  }
}

/** 把检测事件写成业务告警，失败不影响结果展示 */
async function writeAlertsToBackend(data: FallDetectResult) {
  const events = data.events ?? []
  if (!events.length) return
  writingAlerts.value = true
  const elder = elders.value.find((e) => e.id === selectedElderId.value)
  let created = 0
  try {
    for (const event of events) {
      await createAlert({
        elderName: elder?.name || '未关联老人',
        elderId: elder?.id,
        alertType: 'FALL',
        level: 'urgent',
        description: `AI 视频检测：${event.timestamp_start} - ${event.timestamp_end} 检测到跌倒，置信度 ${(event.confidence * 100).toFixed(0)}%${
          data.output_url ? `，标注视频：${data.output_url}` : ''
        }`,
        alertTime: new Date().toISOString().slice(0, 19),
        location: `视频检测 · 跟踪ID ${event.track_id}`,
        contactPhone: elder?.phone,
        suggestedAction: '立即核实老人状态，必要时联系家属或拨打 120',
      })
      created += 1
    }
    alertsWritten.value = created
    ElMessage.success(`已写入 ${created} 条告警到「实时告警列表」`)
  } catch (err: any) {
    const detail = err?.response?.data?.message || err?.message || '写入告警失败'
    ElMessage.warning(`检测成功，但写入告警失败：${detail}`)
  } finally {
    writingAlerts.value = false
  }
}

async function loadElders() {
  try {
    const res = await getElderList({ page: 1, size: 200 })
    elders.value = res.data?.data?.list ?? []
  } catch {
    elders.value = []
  }
}

async function startDetect() {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择视频文件')
    return
  }
  detecting.value = true
  result.value = null
  uploadPercent.value = 0
  videoError.value = false
  alertsWritten.value = 0
  elapsedTip.value = '正在上传…'
  const started = Date.now()
  try {
    // 异步提交：长视频不会把请求挂死，前端轮询任务状态
    const submit = await detectVideoAsync(selectedFile.value, {
      confidence: confidence.value,
      withOutput: true,
      onProgress: (p) => {
        uploadPercent.value = p
        if (p >= 100) elapsedTip.value = '上传完成，等待模型排队…'
      },
    })
    const payload = submit.data?.data
    const taskId =
      typeof payload === 'string' ? payload : (payload as { task_id?: string })?.task_id
    if (!taskId) {
      ElMessage.error(submit.data?.message || '提交任务失败')
      return
    }
    const data = await waitForTask(taskId, started)
    result.value = data
    const seconds = ((Date.now() - started) / 1000).toFixed(1)
    ElMessage.success(
      data.summary?.total_falls
        ? `检测到 ${data.summary.total_falls} 次跌倒（耗时 ${seconds}s）`
        : `未检测到跌倒（耗时 ${seconds}s）`,
    )
    if (data.summary?.total_falls > 0) {
      await writeAlertsToBackend(data)
    }
  } catch (err: any) {
    const detail =
      err?.response?.data?.message ||
      err?.response?.data?.detail ||
      err?.message ||
      '检测失败'
    ElMessage.error(String(detail))
  } finally {
    detecting.value = false
    elapsedTip.value = ''
  }
}

function onOpen() {
  resetAll()
  checkStatus()
  loadElders()
}

watch(visible, (v) => {
  if (v) onOpen()
})

function close() {
  visible.value = false
}
</script>

<template>
  <el-dialog
    v-model="visible"
    title="上传视频检测"
    width="880px"
    top="6vh"
    destroy-on-close
    class="fall-detect-dialog"
  >
    <!-- ===== 服务状态 ===== -->
    <el-alert
      v-if="statusError"
      :title="statusError"
      type="error"
      :closable="false"
      show-icon
      class="mb"
    />
    <el-alert
      v-else-if="status && !status.loaded"
      :title="missingHint"
      type="warning"
      :closable="false"
      show-icon
      class="mb"
    />
    <el-alert
      v-else-if="modelReady"
      :title="`模型就绪 · ${status?.device ? String(status.device).toUpperCase() : 'CPU'} 推理`"
      type="success"
      :closable="false"
      show-icon
      class="mb"
    />

    <!-- ===== 上传区 ===== -->
    <el-upload
      ref="uploadRef"
      v-model:file-list="fileList"
      drag
      :auto-upload="false"
      :limit="1"
      accept="video/*"
      :on-change="handleChange"
      :on-remove="handleRemove"
      :on-exceed="() => ElMessage.warning('一次只检测一个视频，请先移除已选文件')"
    >
      <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
      <div class="el-upload__text">把视频拖到这里，或<em>点击选择</em></div>
      <template #tip>
        <div class="el-upload__tip">
          支持 mp4 / avi / mov 等常见格式，单文件上限 500 MB。视频不会长期保存，检测后自动清理。
        </div>
      </template>
    </el-upload>

    <!-- ===== 参数 ===== -->
    <div class="param-row">
      <span class="param-label">关联老人</span>
      <el-select
        v-model="selectedElderId"
        placeholder="可选：不选则记为「未关联老人」"
        clearable
        filterable
        :disabled="detecting"
        class="elder-select"
      >
        <el-option
          v-for="e in elders"
          :key="e.id"
          :label="`${e.name}${e.age ? `（${e.age}岁）` : ''}`"
          :value="e.id"
        />
      </el-select>
    </div>
    <div class="param-row">
      <span class="param-label">跌倒置信度阈值</span>
      <el-slider
        v-model="confidence"
        :min="0.1"
        :max="0.9"
        :step="0.05"
        :disabled="detecting"
        class="param-slider"
      />
      <span class="param-value">{{ confidence.toFixed(2) }}</span>
      <el-tooltip content="阈值越低越敏感、误报越多；默认 0.40" placement="top">
        <el-icon class="param-help"><QuestionFilled /></el-icon>
      </el-tooltip>
    </div>

    <!-- ===== 进度 ===== -->
    <div v-if="detecting" class="progress-box">
      <el-progress :percentage="uploadPercent" :stroke-width="10" />
      <div class="progress-tip">{{ elapsedTip }}</div>
    </div>

    <!-- ===== 结果 ===== -->
    <div v-if="result" class="result-box">
      <div class="summary-row">
        <div class="summary-card" :class="{ danger: result.summary.total_falls > 0 }">
          <div class="summary-num">{{ result.summary.total_falls }}</div>
          <div class="summary-label">检测到跌倒</div>
        </div>
        <div class="summary-card">
          <div class="summary-num">{{ result.summary.persons_tracked }}</div>
          <div class="summary-label">跟踪人数</div>
        </div>
        <div class="summary-card">
          <div class="summary-num">{{ result.summary.processing_time_seconds?.toFixed(1) }}</div>
          <div class="summary-label">推理耗时(秒)</div>
        </div>
        <div class="summary-card">
          <div class="summary-num small">{{ result.metadata?.resolution || '-' }}</div>
          <div class="summary-label">分辨率</div>
        </div>
      </div>

      <el-table
        v-if="events.length"
        :data="events"
        size="small"
        border
        max-height="240"
        class="event-table"
      >
        <el-table-column prop="event_id" label="#" width="56" />
        <el-table-column prop="track_id" label="跟踪ID" width="80" />
        <el-table-column prop="timestamp_start" label="开始时间" width="120" />
        <el-table-column prop="timestamp_end" label="结束时间" width="120" />
        <el-table-column label="置信度" width="90">
          <template #default="{ row }">{{ (row.confidence * 100).toFixed(0) }}%</template>
        </el-table-column>
        <el-table-column prop="type" label="类型" />
      </el-table>

      <div v-else class="empty-result">该视频未检测到跌倒事件</div>

      <el-alert
        v-if="result.summary.total_falls > 0"
        type="success"
        :closable="false"
        show-icon
        class="alert-written"
        :title="
          alertsWritten > 0
            ? `已写入 ${alertsWritten} 条告警，可在「实时告警列表」中处置`
            : writingAlerts
              ? '正在写入告警…'
              : '检测到跌倒，但未能写入告警表'
        "
      />

      <div v-if="outputUrl" class="video-box">
        <div class="video-title">
          <span>标注结果回放</span>
          <a :href="outputUrl" :download="result.output_file" class="video-download">
            下载标注视频
          </a>
        </div>
        <video
          v-if="!videoError"
          :src="outputUrl"
          controls
          class="result-video"
          @error="videoError = true"
        />
        <el-alert
          v-else
          type="info"
          :closable="false"
          show-icon
          title="浏览器无法直接播放该视频（编码不兼容）"
          description="请点击右上角「下载标注视频」用本地播放器观看。"
        />
      </div>
    </div>

    <template #footer>
      <el-button @click="close">关闭</el-button>
      <el-button :loading="checking" @click="checkStatus">重新检测服务</el-button>
      <el-button type="primary" :disabled="!canDetect" :loading="detecting" @click="startDetect">
        {{ detecting ? '检测中…' : '开始检测' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.mb {
  margin-bottom: 12px;
}
.param-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 14px 0 4px;
}
.param-label {
  font-size: 13px;
  color: #606266;
  white-space: nowrap;
}
.param-slider {
  flex: 1;
  max-width: 320px;
}
.param-value {
  font-family: ui-monospace, monospace;
  font-size: 13px;
  color: #303133;
  width: 36px;
}
.elder-select {
  width: 260px;
}
.alert-written {
  margin-top: 12px;
}
.param-help {
  color: #a8abb2;
  cursor: help;
}
.progress-box {
  margin-top: 14px;
}
.progress-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}
.result-box {
  margin-top: 16px;
}
.summary-row {
  display: flex;
  gap: 12px;
}
.summary-card {
  flex: 1;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 10px 12px;
  text-align: center;
  background: #fafafa;
}
.summary-card.danger {
  border-color: #fbc4c4;
  background: #fef0f0;
}
.summary-num {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}
.summary-card.danger .summary-num {
  color: #f56c6c;
}
.summary-num.small {
  font-size: 14px;
  padding-top: 6px;
}
.summary-label {
  margin-top: 2px;
  font-size: 12px;
  color: #909399;
}
.event-table {
  margin-top: 12px;
}
.empty-result {
  margin-top: 12px;
  padding: 18px;
  text-align: center;
  color: #909399;
  background: #fafafa;
  border-radius: 8px;
  font-size: 13px;
}
.video-box {
  margin-top: 14px;
}
.video-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  color: #606266;
  margin-bottom: 6px;
}
.video-download {
  font-size: 12px;
  color: #409eff;
  text-decoration: none;
}
.video-download:hover {
  text-decoration: underline;
}
.result-video {
  width: 100%;
  max-height: 380px;
  background: #000;
  border-radius: 8px;
}
</style>
