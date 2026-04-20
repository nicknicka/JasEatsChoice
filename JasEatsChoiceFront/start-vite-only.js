// 此脚本在前端项目目录下运行，只启动 Vite renderer dev server
const { createServer } = require('vite')
const vue = require('@vitejs/plugin-vue')
const path = require('path')

async function start() {
  const server = await createServer({
    root: path.resolve(__dirname, 'src/renderer'),
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src/renderer/src'),
        '@renderer': path.resolve(__dirname, 'src/renderer/src')
      },
      extensions: ['.js', '.ts', '.jsx', '.tsx', '.json', '.vue']
    },
    plugins: [vue()],
    css: {
      preprocessorOptions: {
        less: { javascriptEnabled: true }
      }
    },
    server: {
      port: 5173,
      host: true,
      fs: { strict: false },
      proxy: {
        '/api': {
          target: 'http://localhost:7777',
          changeOrigin: true,
          secure: false
        }
      }
    },
    cache: { dir: undefined },
    optimizeDeps: { force: true }
  })
  await server.listen()
  server.printUrls()
}

start().catch(e => { console.error(e); process.exit(1) })