import { resolve } from 'path'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// Web 版（浏览器）构建与本地开发服务器配置。
// Electron 目标用 electron.vite.config.js；这份只产出 dist-web 静态产物。
//
// 后端地址一律从环境变量注入，代码里不留 localhost：
//   API_PROXY_TARGET  默认 http://localhost:7777
//   WS_PROXY_TARGET   默认 http://localhost:11277
//   VITE_DEV_HOST / VITE_DEV_PORT  开发服务器监听地址（默认 localhost:5173）
const API_PROXY_TARGET = process.env.API_PROXY_TARGET || 'http://localhost:7777'
const WS_PROXY_TARGET = process.env.WS_PROXY_TARGET || 'http://localhost:11277'

export default defineConfig({
  root: resolve(__dirname, 'src/renderer'),
  envDir: __dirname,
  base: '/',
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src/renderer/src'),
      '@renderer': resolve(__dirname, 'src/renderer/src')
    },
    extensions: ['.js', '.ts', '.jsx', '.tsx', '.json', '.vue']
  },
  css: {
    preprocessorOptions: {
      less: { javascriptEnabled: true }
    }
  },
  build: {
    outDir: resolve(__dirname, 'dist-web'),
    emptyOutDir: true,
    sourcemap: false
  },
  server: {
    host: process.env.VITE_DEV_HOST || 'localhost',
    port: Number(process.env.VITE_DEV_PORT || 5173),
    fs: { strict: false },
    proxy: {
      '/api': {
        target: API_PROXY_TARGET,
        changeOrigin: true,
        secure: false
      },
      // 聊天/订单 WebSocket 走 Netty 独立端口；浏览器侧用同源 /ws 由代理转发
      '/ws': {
        target: WS_PROXY_TARGET,
        ws: true,
        changeOrigin: true
      }
    }
  }
})
