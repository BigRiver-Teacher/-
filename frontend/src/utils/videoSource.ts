import { computed, ref, watch } from 'vue'
import videoRegistry from '@/config/videoRegistry.json'

/**
 * 视频引用解析工具
 *
 * 约定：`videoRegistry.json` 中的 path 一律为【相对项目根目录】的路径，
 * 形如 `frontend/public/videos/monitor-1.mp4`。前端运行时只认站点路径，
 * 因此这里负责把项目根相对路径换算成可被浏览器加载的 `/videos/xxx`。
 */

export type VideoStatus = 'ready' | 'empty' | 'error'

export interface VideoResolution {
  /** 可直接绑定到 <video src> 的地址，失败时为空串 */
  url: string
  status: VideoStatus
  /** 供界面展示的说明文案 */
  message: string
  /** 配置中的原始相对路径，便于排查 */
  configuredPath: string
}

const PUBLIC_PREFIX = `${videoRegistry.publicDir}/`

const ABSOLUTE_PATH_RE = /^([a-zA-Z]:[\\/]|\\\\|\/)/

function normalizePath(raw: string): string {
  return raw.trim().replace(/\\/g, '/').replace(/^\.\//, '')
}

/** 判断是否为绝对路径（含 Windows 盘符、UNC、以 / 开头的根路径） */
export function isAbsolutePath(path: string): boolean {
  return ABSOLUTE_PATH_RE.test(path.trim())
}

/**
 * 把"相对项目根目录"的视频路径解析为站点地址。
 * 任一步不合法都会返回 error 状态并给出明确原因，不会抛出异常打断页面渲染。
 */
export function resolveVideoPath(configuredPath?: string | null): VideoResolution {
  const raw = (configuredPath ?? '').trim()

  if (!raw) {
    return {
      url: '',
      status: 'empty',
      message: '该槽位尚未配置视频',
      configuredPath: '',
    }
  }

  if (isAbsolutePath(raw)) {
    return {
      url: '',
      status: 'error',
      message: `视频路径必须是相对项目根目录的路径，当前配置为绝对路径：${raw}`,
      configuredPath: raw,
    }
  }

  const normalized = normalizePath(raw)

  if (!normalized.startsWith(PUBLIC_PREFIX)) {
    return {
      url: '',
      status: 'error',
      message: `视频需放在 ${PUBLIC_PREFIX} 目录下，当前路径不在该目录内：${raw}`,
      configuredPath: raw,
    }
  }

  const publicRelative = normalized.slice(PUBLIC_PREFIX.length)

  if (!publicRelative) {
    return {
      url: '',
      status: 'error',
      message: `视频路径缺少文件名：${raw}`,
      configuredPath: raw,
    }
  }

  return {
    url: `/${encodeURI(publicRelative)}`,
    status: 'ready',
    message: '',
    configuredPath: normalized,
  }
}

/**
 * 监控页视频组合式函数：解析配置 + 处理加载失败。
 * @param slotId videoRegistry.json 中的槽位 ID，如 monitor-1
 */
export function useVideoSource(slotId: string) {
  const slots = (videoRegistry.slots ?? {}) as Record<string, { label?: string; path?: string }>
  const envFallback = (import.meta.env.VITE_MONITOR_VIDEO_URL ?? '').trim()

  const resolution = computed<VideoResolution>(() => {
    const slot = slots[slotId]

    if (!slot) {
      return {
        url: '',
        status: 'error',
        message: `视频引用失败：配置中不存在槽位「${slotId}」`,
        configuredPath: '',
      }
    }

    const resolved = resolveVideoPath(slot.path)

    // 槽位未配置时，允许用环境变量兜底（便于临时演示，不必改仓库配置）
    if (resolved.status === 'empty' && envFallback) {
      return { url: envFallback, status: 'ready', message: '', configuredPath: 'VITE_MONITOR_VIDEO_URL' }
    }

    return resolved
  })

  /** 浏览器实际加载失败（404 / 编解码不支持 / 网络中断） */
  const loadFailed = ref(false)

  const status = computed<VideoStatus>(() => (loadFailed.value ? 'error' : resolution.value.status))

  const message = computed(() => {
    if (loadFailed.value) {
      const hint = resolution.value.configuredPath
        ? `：${resolution.value.configuredPath}`
        : ''
      return `视频引用失败，文件无法加载${hint}（请确认已放入 frontend/public/videos 且格式受支持）`
    }
    return resolution.value.message
  })

  function onError(event?: Event) {
    loadFailed.value = true
    // 便于开发者在控制台定位具体是哪个槽位、哪个地址出错
    console.error(
      `[video] 槽位 ${slotId} 加载失败：${resolution.value.url || '(空地址)'}`,
      event,
    )
  }

  function retry() {
    loadFailed.value = false
  }

  // 配置变化（如热更新后路径改变）时重置失败状态
  watch(
    () => resolution.value.url,
    () => {
      loadFailed.value = false
    },
  )

  return {
    url: computed(() => (status.value === 'ready' ? resolution.value.url : '')),
    status,
    message,
    configuredPath: computed(() => resolution.value.configuredPath),
    onError,
    retry,
  }
}
