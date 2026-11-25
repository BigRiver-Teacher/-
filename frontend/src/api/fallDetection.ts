import axios from 'axios'
import type { Result } from '@/utils/request'

/**
 * 跌倒检测算法服务（FastAPI，默认 8001）接口封装。
 *
 * 与业务后端（8080）相互独立：
 * - 不携带业务 JWT，算法侧用自己的 X-API-Key（.env 中 FALL_API_KEY，留空则匿名）
 * - baseURL 前缀 /api/fall-detection 由 vite.config.ts 单独代理到 8001，
 *   该规则必须排在 '/api' 之前，否则会被转发到后端导致 404
 */

export interface FallEvent {
  type: string
  event_id: number
  track_id: number
  logical_person_id?: number
  raw_track_ids?: number[]
  start_frame: number
  end_frame: number
  timestamp_start: string
  timestamp_end: string
  confidence: number
}

export interface FallDetectMetadata {
  input_file?: string
  processing_date?: string
  model_version?: string
  confidence_threshold?: number
  video_fps?: number
  video_duration?: string
  total_frames?: number
  resolution?: string
}

export interface FallDetectSummary {
  total_falls: number
  persons_tracked: number
  processing_time_seconds: number
  interrupted?: boolean
}

export interface FallDetectResult {
  metadata: FallDetectMetadata
  events: FallEvent[]
  summary: FallDetectSummary
  /** with_output=true 时返回，可用 /api/fall-detection/output/{output_file} 下载 */
  output_file?: string
  output_url?: string
}

export interface ModelStatus {
  loaded: boolean
  yolo_requested?: string
  yolo_requested_exists?: boolean
  yolo_model?: string
  yolo_exists?: boolean
  yolo_fallback?: boolean
  lstm_checkpoint?: string
  lstm_exists?: boolean
  device?: string | null
  confidence_threshold?: number
  cooldown_frames?: number
}

const detectApi = axios.create({
  // 单次同步检测可能跑几分钟，超时放宽到 15 分钟
  baseURL: '/api/fall-detection',
  timeout: 15 * 60 * 1000,
})

detectApi.interceptors.request.use((config) => {
  // 算法服务若启用了 API Key（FALL_API_KEY），前端通过环境变量注入
  const apiKey = import.meta.env.VITE_FALL_API_KEY
  if (apiKey && config.headers) {
    config.headers['X-API-Key'] = apiKey as string
  }
  return config
})

/** 查询服务与模型状态；服务未启动时抛错，页面据此降级 */
export function getModelStatus() {
  return detectApi.get<Result<ModelStatus>>('/health', { timeout: 8000 })
}

export interface DetectOptions {
  /** 跌倒置信度阈值，0~1，留空用服务默认 */
  confidence?: number
  /** 是否返回标注视频 */
  withOutput?: boolean
  /** 上传进度回调，0~100 */
  onProgress?: (percent: number) => void
}

/** 同步检测：上传视频并等待结果 */
export function detectVideo(file: File, options: DetectOptions = {}) {
  const { confidence, withOutput = true, onProgress } = options
  const form = new FormData()
  form.append('file', file)
  if (confidence !== undefined && confidence !== null) {
    form.append('confidence', String(confidence))
  }
  form.append('with_output', String(withOutput))

  return detectApi.post<Result<FallDetectResult>>('/detect', form, {
    onUploadProgress: (e) => {
      if (onProgress && e.total) {
        onProgress(Math.round((e.loaded / e.total) * 100))
      }
    },
  })
}

/** 标注视频的播放地址（走同一个代理前缀） */
export function resolveOutputUrl(url?: string) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return url.startsWith('/api/fall-detection') ? url : `/api/fall-detection${url}`
}

export interface DetectTaskStatus {
  id: string
  /** pending 排队 / running 推理中 / done 完成 / failed 失败 */
  status: 'pending' | 'running' | 'done' | 'failed'
  created_at?: string
  finished_at?: string | null
  result?: FallDetectResult | null
  error?: string | null
  /** 标注视频地址（异步接口放在任务顶层，不在 result 里） */
  output_url?: string
  output?: string
}

/**
 * 异步检测：立即返回 task_id，适合长视频。
 * 同步接口会让请求一直挂着，视频长时容易被网关或浏览器判超时。
 */
export function detectVideoAsync(file: File, options: DetectOptions = {}) {
  const { confidence, withOutput = true, onProgress } = options
  const form = new FormData()
  form.append('file', file)
  if (confidence !== undefined && confidence !== null) {
    form.append('confidence', String(confidence))
  }
  form.append('with_output', String(withOutput))

  return detectApi.post<Result<string | { task_id: string }>>('/detect/async', form, {
    onUploadProgress: (e) => {
      if (onProgress && e.total) {
        onProgress(Math.round((e.loaded / e.total) * 100))
      }
    },
  })
}

/** 查询异步任务状态 */
export function getDetectTask(taskId: string) {
  return detectApi.get<Result<DetectTaskStatus>>(`/detect/task/${taskId}`, { timeout: 15000 })
}
