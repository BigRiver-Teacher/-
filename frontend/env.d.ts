/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<object, object, unknown>
  export default component
}

interface ImportMetaEnv {
  /** 监控页演示视频地址（可选，未设置时不播放） */
  readonly VITE_MONITOR_VIDEO_URL?: string
  /** 跌倒检测算法服务的 API Key（算法侧启用 FALL_API_KEY 时填写） */
  readonly VITE_FALL_API_KEY?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
