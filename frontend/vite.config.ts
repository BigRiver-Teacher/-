import { fileURLToPath, URL } from 'node:url'
import { existsSync, readFileSync } from 'node:fs'
import { resolve } from 'node:path'

import { defineConfig, type Plugin } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

const REGISTRY_FILE = fileURLToPath(new URL('./src/config/videoRegistry.json', import.meta.url))
// 项目根目录（仓库根），用于解析 videoRegistry.json 中"相对项目根"的路径
const PROJECT_ROOT = fileURLToPath(new URL('..', import.meta.url))

/**
 * 跌倒检测算法服务的 API Key。
 * 优先读进程环境变量，其次读 algorithm/.env，由代理在服务端注入 X-API-Key，
 * 避免把密钥打进前端产物（构建后任何人都能看到）。
 */
function resolveFallApiKeyHeaders(): Record<string, string> {
  const fromEnv = process.env.FALL_API_KEY?.trim()
  if (fromEnv) return { 'X-API-Key': fromEnv }
  try {
    const envPath = fileURLToPath(new URL('../algorithm/.env', import.meta.url))
    if (!existsSync(envPath)) return {}
    const matched = readFileSync(envPath, 'utf-8').match(/^FALL_API_KEY=(.+)$/m)
    const key = matched?.[1]?.trim()
    return key ? { 'X-API-Key': key } : {}
  } catch {
    return {}
  }
}
const ABSOLUTE_PATH_RE = /^([a-zA-Z]:[\\/]|\\\\|\/)/

/**
 * 视频引用校验：启动开发服务器或构建时检查 videoRegistry.json 中登记的路径。
 * 只警告不中断，避免因缺少可选演示视频而阻塞开发。
 */
function videoRegistryGuard(): Plugin {
  return {
    name: 'video-registry-guard',
    buildStart() {
      let registry: { publicDir?: string; slots?: Record<string, { path?: string }> }

      try {
        registry = JSON.parse(readFileSync(REGISTRY_FILE, 'utf-8'))
      } catch (error) {
        this.warn(
          `[video] 无法读取视频引用记录 ${REGISTRY_FILE}：${(error as Error).message}`,
        )
        return
      }

      const publicPrefix = `${registry.publicDir ?? 'frontend/public'}/`

      for (const [slotId, slot] of Object.entries(registry.slots ?? {})) {
        const configured = (slot?.path ?? '').trim()
        if (!configured) continue

        if (ABSOLUTE_PATH_RE.test(configured)) {
          this.warn(`[video] 槽位 ${slotId} 使用了绝对路径，必须改为相对项目根目录：${configured}`)
          continue
        }

        const normalized = configured.replace(/\\/g, '/').replace(/^\.\//, '')
        if (!normalized.startsWith(publicPrefix)) {
          this.warn(`[video] 槽位 ${slotId} 的视频不在 ${publicPrefix} 下：${configured}`)
          continue
        }

        const absolute = resolve(PROJECT_ROOT, normalized)
        if (!existsSync(absolute)) {
          this.warn(
            `[video] 槽位 ${slotId} 的视频文件不存在：${configured}\n` +
              `        期望位置：${absolute}\n` +
              `        处理：放入该文件，或执行 node scripts/05-insert-video.mjs --file <路径> --slot ${slotId}`,
          )
        }
      }
    },
  }
}

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
    videoRegistryGuard(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    // HMR 配置：延长超时，避免打开多个页面后 WebSocket 断开导致页面刷新
    hmr: {
      timeout: 60000,
    },
    // 文件监听：排除 node_modules、.git、视频大文件与编辑临时目录，减少监听文件数量并避免 Windows 文件锁崩溃
    watch: {
      ignored: ['**/node_modules/**', '**/.git/**', '**/src/video/**', '**/*.tmpdir/**'],
    },
    // 代理配置：解决跨域问题
    proxy: {
      // 跌倒检测算法服务（FastAPI，8001）。必须排在 '/api' 之前，
      // 否则 /api/fall-detection/* 会被下面的 '/api' 规则转发到后端 8080 而 404。
      '/api/fall-detection': {
        target: 'http://localhost:8001',
        changeOrigin: true,
        headers: resolveFallApiKeyHeaders(),
      },
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // 后端没有 /api 前缀时，用 rewrite 去掉；如果有则不需要 rewrite
        // rewrite: (path) => path.replace(/^\/api/, ''),
      },
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  preview: {
    port: 4173,
    proxy: {
      '/api/fall-detection': {
        target: 'http://localhost:8001',
        changeOrigin: true,
        headers: resolveFallApiKeyHeaders(),
      },
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
