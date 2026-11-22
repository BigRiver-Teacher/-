<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import '@/assets/bigscreen.css'

/** 当前所属页面：monitor / device / person / expert */
const props = defineProps<{ active: 'monitor' | 'device' | 'person' | 'expert' }>()

const router = useRouter()

const NAV = [
  { key: 'monitor', label: '监控管理', path: '/echart/monitor', icon: 'VideoCamera' },
  { key: 'device', label: '设备管理', path: '/echart/device', icon: 'Cpu' },
  { key: 'person', label: '人员管理', path: '/echart/person', icon: 'User' },
  { key: 'expert', label: '专家管理', path: '/echart/export', icon: 'FirstAidKit' },
] as const

const clock = ref('')
const today = ref('')
let timer: ReturnType<typeof setInterval> | null = null

const WEEK = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
const pad = (n: number) => String(n).padStart(2, '0')

function tick() {
  const d = new Date()
  clock.value = `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
  today.value = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${WEEK[d.getDay()]}`
}

function go(path: string) {
  if (path !== router.currentRoute.value.path) router.push(path)
}

onMounted(() => {
  tick()
  timer = setInterval(tick, 1000)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <div class="bs-page">
    <!-- 背景：网格 / 光晕 / 扫描线 -->
    <div class="bs-bg" aria-hidden="true">
      <span class="bs-bg__grid"></span>
      <span class="bs-bg__glow bs-bg__glow--a"></span>
      <span class="bs-bg__glow bs-bg__glow--b"></span>
      <span class="bs-bg__scan"></span>
    </div>

    <!-- 顶部导航 -->
    <header class="bs-header">
      <div class="bs-brand" @click="router.push('/home')">
        <span class="bs-brand__mark">
          <el-icon><Management /></el-icon>
        </span>
        <span class="bs-brand__text">
          <b>朝夕护行</b>
          <span>多模态防跌倒智慧养老平台</span>
        </span>
      </div>

      <nav class="bs-nav">
        <button
          v-for="item in NAV"
          :key="item.key"
          type="button"
          class="bs-tab"
          :class="{ 'is-active': props.active === item.key }"
          @click="go(item.path)"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          {{ item.label }}
        </button>
      </nav>

      <div class="bs-meta">
        <span class="bs-status"><i></i>数据链路正常</span>
        <div class="bs-clock">
          <b>{{ clock }}</b>
          <span>{{ today }}</span>
        </div>
        <button type="button" class="bs-back" @click="router.push('/home')">
          <el-icon><Back /></el-icon>
          返回后台
        </button>
      </div>
    </header>

    <!-- 页面内容 -->
    <slot />
  </div>
</template>
