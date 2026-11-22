<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = withDefaults(
  defineProps<{
    /** 目标数值 */
    value: number
    /** 动画时长（ms） */
    duration?: number
    /** 小数位，0 表示整数并加千分位 */
    decimals?: number
  }>(),
  { duration: 900, decimals: 0 },
)

const shown = ref(0)
let raf = 0

function format(n: number) {
  if (props.decimals > 0) return n.toFixed(props.decimals)
  return Math.round(n).toLocaleString('en-US')
}

function run(from: number, to: number) {
  if (raf) cancelAnimationFrame(raf)
  if (!Number.isFinite(to)) {
    shown.value = 0
    return
  }
  const start = performance.now()
  const step = (now: number) => {
    const p = Math.min(1, (now - start) / Math.max(1, props.duration))
    // easeOutCubic
    const eased = 1 - Math.pow(1 - p, 3)
    shown.value = from + (to - from) * eased
    if (p < 1) raf = requestAnimationFrame(step)
  }
  raf = requestAnimationFrame(step)
}

onMounted(() => run(0, Number(props.value) || 0))

watch(
  () => props.value,
  (next, prev) => run(Number(prev) || 0, Number(next) || 0),
)

onBeforeUnmount(() => {
  if (raf) cancelAnimationFrame(raf)
})
</script>

<template>
  <span class="bs-count">{{ format(shown) }}</span>
</template>
