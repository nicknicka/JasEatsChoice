// 测试 electron 模块
console.log('Testing electron module...')
console.log('process.env.ELECTRON_RUN_AS_NODE:', process.env.ELECTRON_RUN_AS_NODE)
console.log('process.versions.electron:', process.versions.electron)

try {
  const electron = require('electron')
  console.log('require("electron"):', electron)
  console.log('typeof electron:', typeof electron)
  if (electron && typeof electron === 'object') {
    console.log('electron.app:', electron.app)
    console.log('electron.BrowserWindow:', electron.BrowserWindow)
  }
} catch (error) {
  console.error('Error requiring electron:', error)
}
