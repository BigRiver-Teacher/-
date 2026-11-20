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
