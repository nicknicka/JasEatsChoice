const { app } = require('electron')
console.log('electron.app:', app)
console.log('app.whenReady:', app.whenReady)
app.whenReady().then(() => {
  console.log('Electron app is ready!')
  setTimeout(() => app.quit(), 1000)
})
