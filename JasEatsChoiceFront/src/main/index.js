// CommonJS syntax for Electron main process

// 直接导入 electron 模块
const { app, shell, BrowserWindow, ipcMain, session } = require('electron')
const path = require('path')
const fs = require('fs/promises')
const Store = require('electron-store')
const sharp = require('sharp') // Image processing library

// Check if we're in development mode
const isDev = process.env.NODE_ENV === 'development' || process.env.ELECTRON_IS_DEV
const icon = path.join(__dirname, '../../resources/icon.png')

// Initialize electron-store later when app is ready
let store
let mainWindow

function createWindow() {
  // Create the browser window.
  mainWindow = new BrowserWindow({
    width: 1200,
    height: 800,
    show: false,
    autoHideMenuBar: true,
    ...(process.platform === 'linux' ? { icon } : {}),
    webPreferences: {
      preload: path.join(__dirname, '../preload/index.js'),
      sandbox: false,
      nodeIntegration: false,
      contextIsolation: true,
      enableRemoteModule: false,
      webSecurity: true,
      allowRunningInsecureContent: false
    }
  })

  mainWindow.on('ready-to-show', () => {
    mainWindow.show()
    mainWindow.webContents.openDevTools()
  })

  mainWindow.webContents.setWindowOpenHandler((details) => {
    shell.openExternal(details.url)
    return { action: 'deny' }
  })

  // HMR for renderer base on electron-vite cli.
  if (isDev && process.env['ELECTRON_RENDERER_URL']) {
    mainWindow.loadURL(process.env['ELECTRON_RENDERER_URL'])
  } else {
    mainWindow.loadFile(path.join(__dirname, '../renderer/index.html'))
  }
}

// This method will be called when Electron has finished initialization
app.whenReady().then(() => {
  app.setAppUserModelId('com.electron')

  // 设置内容安全策略 (CSP) - 根据环境动态配置
  const scriptSrcPolicy = isDev
    ? "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://webapi.amap.com https://restapi.amap.com"
    : "script-src 'self' 'unsafe-inline' https://webapi.amap.com https://restapi.amap.com"

  session.defaultSession.webRequest.onHeadersReceived((details, callback) => {
    callback({
      responseHeaders: {
        ...details.responseHeaders,
        'Content-Security-Policy': [
          `default-src 'self'; ${scriptSrcPolicy};` +
            " style-src 'self' 'unsafe-inline' https://webapi.amap.com;" +
            " img-src 'self' data: https: http: https://webapi.amap.com https://restapi.amap.com;" +
            " font-src 'self' data: https://webapi.amap.com;" +
            " connect-src 'self' ws://localhost:* ws://127.0.0.1:* http://localhost:* http://127.0.0.1:* https: https://webapi.amap.com https://restapi.amap.com;" +
            " media-src 'self' blob:;" +
            " object-src 'none';" +
            " base-uri 'self';" +
            " form-action 'self';"
        ]
      }
    })
  })

  // Initialize electron-store now that app is ready
  store = new Store({
    projectName: 'jaseatschoice',
    defaults: {
      userPreferences: {
        theme: 'light',
        notification: true,
        defaultFilter: 'all'
      },
      offlineMenus: [],
      historyOrders: []
    }
  })

  // Create user data directory
  const createUserDataDir = async () => {
    const userDataPath = path.join(app.getPath('userData'), 'user')
    const imagesPath = path.join(userDataPath, 'images')
    const chatDataPath = path.join(userDataPath, 'chat')

    try {
      await fs.mkdir(imagesPath, { recursive: true })
      await fs.mkdir(chatDataPath, { recursive: true })
      console.log('User data directories created successfully')
    } catch (error) {
      console.error('Failed to create user data directories:', error)
    }

    return { userDataPath, imagesPath, chatDataPath }
  }

  // Handle image upload and processing
  ipcMain.handle('user:uploadImage', async (event, imageData) => {
    try {
      console.log('Received image data for upload:', imageData)
      // Resize image and return as data URL instead of file path
      const buffer = Buffer.from(imageData.base64, 'base64')
      const ext = imageData.type.split('/')[1] || 'png'

      // Generate thumbnail
      const thumbnailBuffer = await sharp(buffer)
        .resize({ width: 200, height: 200, fit: 'cover' })
        .toBuffer()

      // Convert to base64 and create data URL
      const thumbnailBase64 = thumbnailBuffer.toString('base64')
      const thumbnailDataUrl = `data:${imageData.type};base64,${thumbnailBase64}`

      return {
        original: `data:${imageData.type};base64,${imageData.base64}`,
        thumbnail: thumbnailDataUrl,
        filename: `${Date.now()}.${ext}`,
        ext
      }
    } catch (error) {
      console.error('Image upload failed:', error)
      return { error: error.message }
    }
  })

  createWindow()

  // Electron-store IPC handlers
  ipcMain.handle('store:set', (_, key, value) => store.set(key, value))
  ipcMain.handle('store:get', (_, key) => store.get(key))
  ipcMain.handle('store:delete', (_, key) => store.delete(key))
  ipcMain.handle('store:clear', () => store.clear())

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow()
  })
})

// Quit when all windows are closed (except on macOS)
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit()
})

// WebSocket implementation
const WebSocket = require('ws')

let webSocketClient = null

// WebSocket IPC handlers
ipcMain.handle('websocket:connect', async (event, url) => {
  try {
    if (webSocketClient && webSocketClient.readyState === WebSocket.OPEN) {
      webSocketClient.close()
    }

    webSocketClient = new WebSocket(url)

    webSocketClient.on('open', (eventData) => {
      mainWindow.webContents.send('websocket:open', eventData)
    })

    webSocketClient.on('message', (message) => {
      mainWindow.webContents.send('websocket:message', message)

      // Dispatch message to specific handlers based on message type
      try {
        const parsedMessage = JSON.parse(message)
        // 后端使用 msgType 字段，兼容 type 字段
        const messageType = parsedMessage.msgType || parsedMessage.type
        switch (messageType) {
          case 'auth':
            mainWindow.webContents.send('websocket:auth', message)
            break
          case 'orderUpdate':
          case 'order_status':
          case 'order_sync':
            mainWindow.webContents.send('websocket:orderUpdate', message)
            break
          case 'chat':
          case 'single':
          case 'group':
            mainWindow.webContents.send('websocket:chat', message)
            break
          case 'merchantUpdate':
            mainWindow.webContents.send('websocket:merchantUpdate', message)
            break
          case 'recommend':
            mainWindow.webContents.send('websocket:recommend', message)
            break
          case 'notification':
          case 'avatar_update':
            mainWindow.webContents.send('websocket:notification', message)
            break
          default:
            // 记录未知消息类型用于调试
            console.log('WebSocket message type:', messageType, 'full message:', parsedMessage)
            break
        }
      } catch (error) {
        // Not a JSON message, handle as raw
        console.error('WebSocket message parse error:', error)
      }
    })

    webSocketClient.on('close', (code, reason) => {
      mainWindow.webContents.send('websocket:close', code, reason)
      webSocketClient = null
    })

    webSocketClient.on('error', (error) => {
      mainWindow.webContents.send('websocket:error', error)
    })

    return 'WebSocket connection initiated'
  } catch (error) {
    console.error('WebSocket connection error:', error)
    return { error: error.message }
  }
})

ipcMain.handle('websocket:send', async (event, message) => {
  try {
    if (webSocketClient && webSocketClient.readyState === WebSocket.OPEN) {
      webSocketClient.send(JSON.stringify(message))
      return 'Message sent successfully'
    } else {
      return { error: 'WebSocket not connected' }
    }
  } catch (error) {
    console.error('WebSocket send error:', error)
    return { error: error.message }
  }
})

ipcMain.handle('websocket:disconnect', async () => {
  try {
    if (webSocketClient) {
      webSocketClient.close()
      webSocketClient = null
      return 'WebSocket disconnected successfully'
    } else {
      return 'WebSocket was not connected'
    }
  } catch (error) {
    console.error('WebSocket disconnect error:', error)
    return { error: error.message }
  }
})
