const { createServer } = require('vite')
const vue = require('@vitejs/plugin-vue')
const path = require('path')

async function start() {
  const server = await createServer({
    root: path.resolve(__dirname, '../JasEatsChoiceFront/src/renderer'),
    resolve: {
      alias: {
        '@': path.resolve(__dirname, '../JasEatsChoiceFront/src/renderer/src'),
        '@renderer': path.resolve(__dirname, '../JasEatsChoiceFront/src/renderer/src')
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
  console.log('Vite renderer dev server running at http://localhost:5173/')
}

start().catch(e => { console.error(e); process.exit(1) })