/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<object, object, unknown>
  export default component
}

interface ImportMetaEnv {
  /** 监控页演示视频地址（可选，未设置时不播放） */
  readonly VITE_MONITOR_VIDEO_URL?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
