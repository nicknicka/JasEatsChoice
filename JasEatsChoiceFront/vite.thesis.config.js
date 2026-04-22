import { resolve } from 'path'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  root: resolve(__dirname, 'src/renderer'),
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
      less: {
        javascriptEnabled: true
      }
    }
  },
  server: {
    host: '::1',
    port: 5173,
    strictPort: true,
    fs: {
      strict: false
    },
    proxy: {
      '/api': {
        target: 'http://localhost:7777',
        changeOrigin: true,
        secure: false
      }
    }
  },
  optimizeDeps: {
    force: true,
    include: ['dayjs', 'dayjs/plugin/relativeTime']
  }
})
