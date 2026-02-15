const { contextBridge, ipcRenderer, clipboard } = require('electron')
const { electronAPI } = require('@electron-toolkit/preload')

// Custom APIs for renderer
const api = {
  // electron-store methods
  store: {
    set: (key, value) => ipcRenderer.invoke('store:set', key, value),
    get: (key) => ipcRenderer.invoke('store:get', key),
    delete: (key) => ipcRenderer.invoke('store:delete', key),
    clear: () => ipcRenderer.invoke('store:clear')
  },
  // clipboard methods
  clipboard: {
    writeText: (text) => clipboard.writeText(text),
    readText: () => clipboard.readText()
  },
  // WebSocket event listeners
  onWebSocketMessage: (callback) => {
    ipcRenderer.on('websocket:message', (event, message) => callback(message))
  },
  onWebSocketOpen: (callback) => {
    ipcRenderer.on('websocket:open', (event, eventData) => callback(eventData))
  },
  onWebSocketClose: (callback) => {
    ipcRenderer.on('websocket:close', (event, code, reason) => callback(code, reason))
  },
  onWebSocketError: (callback) => {
    ipcRenderer.on('websocket:error', (event, error) => callback(error))
  },
  // WebSocket event listeners for specific message types
  onWebSocketAuth: (callback) => {
    ipcRenderer.on('websocket:auth', (event, message) => callback(message))
  },
  onWebSocketOrderUpdate: (callback) => {
    ipcRenderer.on('websocket:orderUpdate', (event, message) => callback(message))
  },
  onWebSocketChat: (callback) => {
    ipcRenderer.on('websocket:chat', (event, message) => callback(message))
  },
  onWebSocketMerchantUpdate: (callback) => {
    ipcRenderer.on('websocket:merchantUpdate', (event, message) => callback(message))
  },
  onWebSocketRecommend: (callback) => {
    ipcRenderer.on('websocket:recommend', (event, message) => callback(message))
  },
  onWebSocketNotification: (callback) => {
    ipcRenderer.on('websocket:notification', (event, message) => callback(message))
  },
  // WebSocket actions
  sendWebSocketMessage: (message) => {
    ipcRenderer.invoke('websocket:send', message)
  },
  connectWebSocket: (url) => {
    ipcRenderer.invoke('websocket:connect', url)
  },
  disconnectWebSocket: () => {
    ipcRenderer.invoke('websocket:disconnect')
  },
  // Image upload functionality
  uploadImage: (imageData) => {
    return ipcRenderer.invoke('user:uploadImage', imageData)
  }
}

// Use `contextBridge` APIs to expose Electron APIs to
// renderer only if context isolation is enabled, otherwise
// just add to the DOM global.
if (process.contextIsolated) {
  try {
    contextBridge.exposeInMainWorld('electron', electronAPI)
    contextBridge.exposeInMainWorld('api', api)
  } catch (error) {
    console.error(error)
  }
} else {
  window.electron = electronAPI
  window.api = api
}
