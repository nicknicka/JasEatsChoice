/**
 * WebSocket 工具类
 * IM-001: 连接WebSocket服务
 */
class WebSocketClient {
  constructor(url) {
    this.url = url
    this.ws = null
    this.reconnectTimer = null
    this.heartbeatTimer = null
    this.listeners = {}
    this.isConnecting = false
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
  }

  /**
   * 连接WebSocket - IM-001
   * @param {String} token - JWT Token（可选，默认从storage获取）
   * @returns {Promise} 返回连接Promise
   */
  connect(token) {
    return new Promise((resolve, reject) => {
      if (this.isConnecting || (this.ws && this.ws.readyState === 1)) {
        resolve()
        return
      }

      this.isConnecting = true

      // 获取token
      const authToken = token || uni.getStorageSync('token') || ''

      this.ws = uni.connectSocket({
        url: this.url,
        header: {
          'Authorization': authToken
        },
        success: () => {
          console.log('[WebSocket] 连接请求已发送')
        },
        fail: (err) => {
          console.error('[WebSocket] 连接请求失败', err)
          this.isConnecting = false
          reject(err)
        }
      })

      this.ws.onOpen(() => {
        console.log('[WebSocket] 连接成功')
        this.isConnecting = false
        this.reconnectAttempts = 0
        this.startHeartbeat()
        this.emit('_connected')
        resolve()
      })

      this.ws.onMessage((res) => {
        try {
          const data = JSON.parse(res.data)

          // 处理心跳响应
          if (data.type === 'pong') {
            return
          }

          this.emit(data.type || 'message', data)
        } catch (error) {
          console.error('[WebSocket] 解析消息失败:', error)
        }
      })

      this.ws.onClose(() => {
        console.log('[WebSocket] 连接关闭')
        this.isConnecting = false
        this.stopHeartbeat()
        this.emit('_disconnected')
        this.reconnect()
      })

      this.ws.onError((err) => {
        console.error('[WebSocket] 连接错误:', err)
        this.isConnecting = false
        this.emit('_error', err)
        this.reconnect()
      })
    })
  }

  /**
   * 重新连接
   */
  reconnect() {
    if (this.reconnectTimer) return

    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('WebSocket重连次数过多，停止重连')
      this.emit('_reconnectFailed')
      return
    }

    this.reconnectAttempts++
    const delay = Math.min(3000 * Math.pow(2, this.reconnectAttempts - 1), 30000)

    this.reconnectTimer = setTimeout(() => {
      console.log(`WebSocket重连中... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`)
      this.connect()
      this.reconnectTimer = null
    }, delay)
  }

  /**
   * 发送消息 - IM-004（文本消息）、IM-005（图片消息）
   * @param {Object} data - 消息数据
   * @returns {Promise} 返回发送Promise
   */
  send(data) {
    return new Promise((resolve, reject) => {
      if (this.ws && this.ws.readyState === 1) {
        this.ws.send({
          data: JSON.stringify(data),
          success: () => {
            console.log('[WebSocket] 消息发送成功', data)
            resolve()
          },
          fail: (err) => {
            console.error('[WebSocket] 消息发送失败', err)
            reject(err)
          }
        })
      } else {
        const error = new Error('WebSocket未连接，无法发送消息')
        console.error('[WebSocket]', error.message)
        reject(error)
      }
    })
  }

  /**
   * 获取连接状态
   */
  isConnected() {
    return this.ws && this.ws.readyState === 1
  }

  /**
   * 监听事件
   * @param {string} event - 事件名称
   * @param {Function} callback - 回调函数
   */
  on(event, callback) {
    if (!this.listeners[event]) {
      this.listeners[event] = []
    }
    this.listeners[event].push(callback)
  }

  /**
   * 移除监听
   * @param {string} event - 事件名称
   * @param {Function} callback - 回调函数
   */
  off(event, callback) {
    if (!this.listeners[event]) return

    const index = this.listeners[event].indexOf(callback)
    if (index > -1) {
      this.listeners[event].splice(index, 1)
    }
  }

  /**
   * 触发事件
   * @param {string} event - 事件名称
   * @param {Object} data - 事件数据
   */
  emit(event, data) {
    if (this.listeners[event]) {
      this.listeners[event].forEach(callback => callback(data))
    }
  }

  /**
   * 开始心跳
   */
  startHeartbeat() {
    this.heartbeatTimer = setInterval(() => {
      this.send({ type: 'ping', timestamp: Date.now() })
    }, 30000) // 30秒一次心跳
  }

  /**
   * 停止心跳
   */
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  /**
   * 关闭连接
   */
  close() {
    this.stopHeartbeat()
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
    this.listeners = {}
  }
}

export default WebSocketClient
