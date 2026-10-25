import { resolve } from 'path'
import { defineConfig, externalizeDepsPlugin } from 'electron-vite'
import vue from '@vitejs/plugin-vue'

// 开发服务器代理的后端地址（Electron dev 下 renderer 也走同源 /api、/ws，见 src/renderer/src/config/index.js）
const API_PROXY_TARGET = process.env.API_PROXY_TARGET || 'http://localhost:7777'
const WS_PROXY_TARGET = process.env.WS_PROXY_TARGET || 'http://localhost:11277'

export default defineConfig({
  main: {
    plugins: [externalizeDepsPlugin()],
    build: {
      rollupOptions: {
        external: ['electron', 'electron-store', 'ws']
      }
    },
    resolve: {
      alias: {
        '@': resolve('src/renderer/src'),
        '@renderer': resolve('src/renderer/src')
      },
      extensions: ['.js', '.ts', '.json', '.node']
    }
  },
  preload: {
    plugins: [externalizeDepsPlugin()],
    build: {
      rollupOptions: {
        external: ['electron', '@electron-toolkit/preload']
      }
    }
  },
  renderer: {
    resolve: {
      alias: {
        '@': resolve('src/renderer/src'),
        '@renderer': resolve('src/renderer/src')
      },
      extensions: ['.js', '.ts', '.jsx', '.tsx', '.json', '.vue']
    },
    plugins: [vue()],
    css: {
      preprocessorOptions: {
        less: {
          javascriptEnabled: true
        }
      }
    },
    // 禁用文件系统缓存以避免缓存问题
    server: {
      fs: {
        strict: false
      },
      // API / WebSocket 代理配置
      proxy: {
        '/api': {
          target: API_PROXY_TARGET,
          changeOrigin: true,
          secure: false
        },
        '/ws': {
          target: WS_PROXY_TARGET,
          ws: true,
          changeOrigin: true
        }
      }
    },
    // 强制不使用缓存
    cache: {
      dir: undefined
    },
    optimizeDeps: {
      force: true,
      include: ['dayjs', 'dayjs/plugin/relativeTime'],
      exclude: []
    }
  }
})
