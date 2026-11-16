<script setup lang="ts">
import { useVideoSource } from '@/utils/videoSource'

const props = defineProps<{
  /** videoRegistry.json 中的槽位 ID */
  slot: string
}>()

const { url, status, message, onError } = useVideoSource(props.slot)
</script>

<template>
  <video
    v-if="status === 'ready'"
    class="monitor-video"
    :src="url"
    autoplay
    muted
    loop
    playsinline
    @error="onError"
  ></video>

  <div v-else class="monitor-video monitor-fallback" :class="`is-${status}`">
    <span class="fallback-tag">{{ status === 'error' ? '视频引用失败' : '未配置视频' }}</span>
    <span class="fallback-msg">{{ message }}</span>
    <span class="fallback-slot">槽位：{{ slot }}</span>
  </div>
</template>

<style scoped>
.monitor-video {
  display: block;
  width: 90%;
  height: 80%;
  object-fit: cover;
  border-radius: 6px;
  background: #000;
}

.monitor-fallback {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px;
  box-sizing: border-box;
  text-align: center;
  color: #9aa4b2;
  font-size: 12px;
  line-height: 1.6;
  border: 1px dashed #3a4454;
}

.monitor-fallback.is-error {
  color: #f0a1a1;
  border-color: #7a3b3b;
}

.fallback-tag {
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 1px;
}

.fallback-msg {
  max-width: 100%;
  word-break: break-all;
  opacity: 0.85;
}

.fallback-slot {
  opacity: 0.55;
  font-family: Consolas, Monaco, monospace;
}
</style>
