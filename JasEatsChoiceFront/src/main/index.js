// CommonJS syntax for Electron main process
const { app, shell, BrowserWindow, ipcMain, session, screen } = require('electron')
const path = require('path')
const fs = require('fs/promises')
const Store = require('electron-store')
const WebSocket = require('ws')
// 窗口尺寸常量
const WINDOW_SIZES = {
  LOGIN: { width: 400, height: 560 },
  REGISTER: { width: 400, height: 620 },
  ADMIN_LOGIN: { width: 400, height: 560 },
  MAIN: { width: 1200, height: 800 }
}

/**
 * 将窗口居中到屏幕
 */
function centerWindow(win, width, height) {
  const { width: screenW, height: screenH } = screen.getPrimaryDisplay().workAreaSize
  win.setPosition(Math.round((screenW - width) / 2), Math.round((screenH - height) / 2))
}

/**
 * 切换窗口尺寸并居中
 */
function resizeWindow(win, sizeType, options = {}) {
  const { animate = true, resizable } = options
  const targetSize = WINDOW_SIZES[sizeType]
  if (!targetSize) {
    console.error(`[WindowManager] 未知的窗口尺寸类型: ${sizeType}`)
    return
  }
  const { width, height } = targetSize
  const shouldResizable = resizable !== undefined ? resizable : sizeType === 'MAIN'
  win.setResizable(shouldResizable)
  if (process.platform === 'darwin' && animate) {
    const { width: screenW, height: screenH } = screen.getPrimaryDisplay().workAreaSize
    win.setBounds({
      x: Math.round((screenW - width) / 2),
      y: Math.round((screenH - height) / 2),
      width, height
    }, true)
  } else {
    win.setSize(width, height)
    centerWindow(win, width, height)
  }
  console.log(`[WindowManager] 窗口调整到 ${sizeType} (${width}x${height}), resizable=${shouldResizable}`)
}

// Check if we're in development mode
const isDev = process.env.NODE_ENV === 'development' || process.env.ELECTRON_IS_DEV
const icon = path.join(__dirname, '../../resources/icon.png')

// Initialize electron-store later when app is ready
let store
let mainWindow
let webSocketClient = null
// 当前窗口状态：login / main
let windowState = 'login'

function createWindow() {
  // 创建小窗口用于登录
  mainWindow = new BrowserWindow({
    width: WINDOW_SIZES.LOGIN.width,
    height: WINDOW_SIZES.LOGIN.height,
    show: false,
    frame: false,
    resizable: false,
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
    // 登录阶段不自动打开 DevTools
  })

  mainWindow.webContents.setWindowOpenHandler((details) => {
    shell.openExternal(details.url)
    return { action: 'deny' }
  })

  // 处理权限请求（地理位置等）
  mainWindow.webContents.session.setPermissionRequestHandler(
    (webContents, permission, callback, details) => {
      console.log('[权限请求] 权限类型:', permission)
      // 允许地理位置权限
      if (permission === 'geolocation') {
        console.log('[权限请求] 允许地理位置访问')
        callback(true)
        return
      }
      // 其他权限默认拒绝，用户可以根据需要添加更多权限
      console.log('[权限请求] 拒绝权限:', permission)
      callback(false)
    }
  )

  // 处理权限检查（用于查询权限状态）
  mainWindow.webContents.session.setPermissionCheckHandler(
    (webContents, permission, requestingOrigin, details) => {
      console.log('[权限检查] 权限类型:', permission, '来源:', requestingOrigin)
      // 允许地理位置权限检查
      if (permission === 'geolocation') {
        console.log('[权限检查] 允许地理位置检查')
        return true
      }
      // 其他权限默认拒绝
      console.log('[权限检查] 拒绝权限检查:', permission)
      return false
    }
  )

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

  // 开发模式下清除缓存以避免旧代码问题
  if (isDev) {
    const clearCache = async () => {
      console.log('[Main] 清除应用缓存...')
      await session.defaultSession.clearCache()
      await session.defaultSession.clearStorageData({
        storages: [
          'appcache',
          'cookies',
          'filesystem',
          'indexdb',
          'localstorage',
          'shadercache',
          'websql',
          'serviceworkers',
          'cachestorage'
        ]
      })
      console.log('[Main] 缓存已清除')
    }
    clearCache()
  }

  // 设置全局权限处理器（地理位置等）
  session.defaultSession.setPermissionRequestHandler((webContents, permission, callback) => {
    console.log('[全局权限请求] 权限类型:', permission)
    if (permission === 'geolocation') {
      console.log('[全局权限请求] 允许地理位置访问')
      callback(true)
    } else {
      console.log('[全局权限请求] 拒绝权限:', permission)
      callback(false)
    }
  })

  session.defaultSession.setPermissionCheckHandler((webContents, permission) => {
    console.log('[全局权限检查] 权限类型:', permission)
    if (permission === 'geolocation') {
      console.log('[全局权限检查] 允许地理位置检查')
      return true
    }
    console.log('[全局权限检查] 拒绝权限检查:', permission)
    return false
  })

  // 设置内容安全策略 (CSP) - 根据环境动态配置
  const scriptSrcPolicy = isDev
    ? "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://webapi.amap.com https://restapi.amap.com https://vdata.amap.com"
    : "script-src 'self' 'unsafe-inline' https://webapi.amap.com https://restapi.amap.com https://vdata.amap.com"

  // 忽略高德地图相关的证书错误（仅开发环境）
  if (isDev) {
    app.commandLine.appendSwitch('ignore-certificate-errors')
  }

  session.defaultSession.webRequest.onHeadersReceived((details, callback) => {
    callback({
      responseHeaders: {
        ...details.responseHeaders,
        'Content-Security-Policy': [
          `default-src 'self'; ${scriptSrcPolicy} blob:;` +
            " style-src 'self' 'unsafe-inline' https://webapi.amap.com https://vdata.amap.com;" +
            " img-src 'self' data: blob: https: http: https://webapi.amap.com https://restapi.amap.com https://vdata.amap.com;" +
            " font-src 'self' data: https://webapi.amap.com https://vdata.amap.com;" +
            " worker-src 'self' blob:;" +
            " connect-src 'self' ws://localhost:* wss://localhost:* ws://127.0.0.1:* wss://127.0.0.1:* http://localhost:* http://127.0.0.1:* https://localhost:* https://127.0.0.1:* https: https://webapi.amap.com https://restapi.amap.com https://vdata.amap.com;" +
            " media-src 'self' blob: http://localhost:8080 http://127.0.0.1:8080;" +
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

  // Handle image upload (without sharp for now)
  ipcMain.handle('user:uploadImage', async (event, imageData) => {
    try {
      console.log('Received image data for upload:', imageData)
      const ext = imageData.type.split('/')[1] || 'png'

      // Return original image without processing for now
      // TODO: Re-enable sharp when compatible version is found
      return {
        original: `data:${imageData.type};base64,${imageData.base64}`,
        thumbnail: `data:${imageData.type};base64,${imageData.base64}`,
        filename: `${Date.now()}.${ext}`,
        ext
      }
    } catch (error) {
      console.error('Image upload failed:', error)
      return { error: error.message }
    }
  })

  createWindow()

  // === 窗口控制 IPC 通道 ===
  ipcMain.handle('window:resizeToLogin', () => {
    windowState = 'login'
    resizeWindow(mainWindow, 'LOGIN')
  })

  ipcMain.handle('window:resizeToRegister', () => {
    windowState = 'login'
    resizeWindow(mainWindow, 'REGISTER')
  })

  ipcMain.handle('window:resizeToAdminLogin', () => {
    windowState = 'login'
    resizeWindow(mainWindow, 'ADMIN_LOGIN')
  })

  ipcMain.handle('window:resizeToMain', () => {
    windowState = 'main'
    resizeWindow(mainWindow, 'MAIN')
    // 切换到主窗口后打开 DevTools（仅开发模式）
    if (isDev) {
      mainWindow.webContents.openDevTools()
    }
  })

  ipcMain.handle('window:close', () => {
    if (mainWindow) {
      mainWindow.close()
    }
  })

  ipcMain.handle('window:minimize', () => {
    if (mainWindow) {
      mainWindow.minimize()
    }
  })

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
