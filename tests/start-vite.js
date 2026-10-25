// 启动 Playwright 用例要打的渲染进程开发服务器。
// 唯一的配置来源是 apps/client/vite.web.config.js（同一份配置也用于 build:web），
// 这里只负责把它拉起来，不再重复定义 alias / proxy。
const { spawn } = require('child_process')
const path = require('path')

const CLIENT_DIR = path.resolve(__dirname, '../apps/client')

const child = spawn('npm', ['run', 'dev:web'], {
  cwd: CLIENT_DIR,
  stdio: 'inherit',
  env: process.env
})

child.on('exit', (code, signal) => {
  if (signal) {
    process.kill(process.pid, signal)
    return
  }
  process.exit(code == null ? 1 : code)
})

for (const signal of ['SIGINT', 'SIGTERM']) {
  process.on(signal, () => child.kill(signal))
}
