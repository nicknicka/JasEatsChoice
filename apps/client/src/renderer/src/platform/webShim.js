/**
 * Web 版运行时垫片：在没有 Electron preload 的浏览器里补齐渲染进程用到的 window.api。
 *
 * 这个文件刻意不写 import / export，因此可以两种方式使用同一份实现：
 *   1) apps/client/src/renderer/src/main.js 里 `import './platform/webShim'`（Web 构建）
 *   2) Playwright 用 page.addInitScript({ path }) 注入（tests/fixtures/test-base.js）
 * Electron 下 preload 已经提供了真正的 window.api，这里直接跳过，桌面端行为不受影响。
 */
;(function installWebShim() {
  if (typeof window === 'undefined' || window.api) return

  var STORE_KEY = '__jaseats_web_store__'

  // Electron 下 WebSocket 由主进程持有，渲染进程通过 window.api 收发消息；
  // 浏览器里没有主进程，这里用原生 WebSocket 顶替同一份契约。
  var socket = null
  var queued = []
  var wsHandlers = { open: [], message: [], close: [], error: [] }

  function emitWs(kind, args) {
    wsHandlers[kind].forEach(function (cb) {
      try {
        cb.apply(null, args)
      } catch (error) {
        console.error('webShim WebSocket 回调异常:', error)
      }
    })
  }

  function readStore() {
    try {
      return JSON.parse(window.localStorage.getItem(STORE_KEY) || '{}')
    } catch (error) {
      return {}
    }
  }

  function writeStore(data) {
    window.localStorage.setItem(STORE_KEY, JSON.stringify(data))
  }

  window.api = {
    store: {
      get: function (key) {
        var store = readStore()
        return Promise.resolve(Object.prototype.hasOwnProperty.call(store, key) ? store[key] : null)
      },
      set: function (key, value) {
        var store = readStore()
        store[key] = value
        writeStore(store)
        return Promise.resolve(true)
      },
      delete: function (key) {
        var store = readStore()
        delete store[key]
        writeStore(store)
        return Promise.resolve(true)
      },
      clear: function () {
        writeStore({})
        return Promise.resolve(true)
      },
      // preload 里没有 remove，但历史代码与用例曾用过这个名字，保持兼容
      remove: function (key) {
        return window.api.store.delete(key)
      }
    },
    window: {
      resizeToMain: function () {
        return Promise.resolve(true)
      },
      resizeToLogin: function () {
        return Promise.resolve(true)
      },
      resizeToRegister: function () {
        return Promise.resolve(true)
      },
      resizeToAdminLogin: function () {
        return Promise.resolve(true)
      },
      close: function () {
        return Promise.resolve(true)
      },
      minimize: function () {
        return Promise.resolve(true)
      }
    },
    // 小红书登录窗口只在 Electron 桌面端可用，Web 版明确声明不支持
    xhs: { supported: false, login: async () => ({ canceled: true }), clear: async () => false },
    clipboard: {
      writeText: function (text) {
        if (navigator.clipboard && navigator.clipboard.writeText) {
          return navigator.clipboard.writeText(String(text)).catch(function () {
            return true
          })
        }
        return Promise.resolve(true)
      },
      readText: function () {
        if (navigator.clipboard && navigator.clipboard.readText) {
          return navigator.clipboard.readText().catch(function () {
            return ''
          })
        }
        return Promise.resolve('')
      }
    },
    // Electron 里 openExternal 由主进程 shell.openExternal 提供；浏览器只能开新标签
    openExternal: function (url) {
      if (url) window.open(String(url), '_blank', 'noopener')
      return Promise.resolve(true)
    },
    connectWebSocket: function (url) {
      if (
        socket &&
        (socket.readyState === WebSocket.CONNECTING || socket.readyState === WebSocket.OPEN)
      ) {
        return Promise.resolve(true)
      }
      try {
        socket = new WebSocket(url)
      } catch (error) {
        emitWs('error', [error.message])
        return Promise.resolve(false)
      }
      socket.onopen = function () {
        queued.forEach(function (payload) {
          socket.send(payload)
        })
        queued = []
        emitWs('open', [{}])
      }
      socket.onmessage = function (event) {
        emitWs('message', [event.data])
      }
      socket.onclose = function (event) {
        emitWs('close', [event.code, event.reason])
      }
      socket.onerror = function (event) {
        emitWs('error', [event.message || 'WebSocket 连接失败'])
      }
      return Promise.resolve(true)
    },
    sendWebSocketMessage: function (message) {
      var payload = typeof message === 'string' ? message : JSON.stringify(message)
      if (socket && socket.readyState === WebSocket.OPEN) {
        socket.send(payload)
      } else {
        queued.push(payload)
      }
      return Promise.resolve(true)
    },
    disconnectWebSocket: function () {
      if (socket) {
        socket.close()
        socket = null
      }
      return Promise.resolve(true)
    },
    onWebSocketOpen: function (callback) {
      wsHandlers.open.push(callback)
    },
    onWebSocketMessage: function (callback) {
      wsHandlers.message.push(callback)
    },
    onWebSocketClose: function (callback) {
      wsHandlers.close.push(callback)
    },
    onWebSocketError: function (callback) {
      wsHandlers.error.push(callback)
    }
  }
})()
