/**
 * WebSocket 连接管理组合式函数
 */
import { WS_CONFIG } from '../constants/wsConstants'
import { HOME_CONSTANTS } from '../constants/home'
import type { WebSocketMessage, WebSocketAuthMessage } from '../types'

export function useWebSocket() {
  let wsAttempts = 0
  const maxAttempts = HOME_CONSTANTS.WS.MAX_ATTEMPTS

  /**
   * 初始化 WebSocket 连接
   */
  const initializeWebSocket = () => {
    const wsUrl = `${WS_CONFIG.URL}${WS_CONFIG.ENDPOINT}`
    console.log('Connecting to WebSocket server:', wsUrl)

    // 通过 IPC 使用主进程的 WebSocket
    if (window.api) {
      window.api.connectWebSocket(wsUrl)
    } else {
      console.warn('WebSocket API not available')
    }
  }

  /**
   * 发送 WebSocket 消息
   */
  const sendWebSocketMessage = (message: WebSocketMessage) => {
    if (window.api) {
      window.api.sendWebSocketMessage(message)
    } else {
      console.error('API not available, cannot send WebSocket message')
    }
  }

  /**
   * 发送身份验证消息
   */
  const sendAuthMessage = () => {
    const authMsg: WebSocketAuthMessage = {
      msgType: 'auth',
      userId: localStorage.getItem('userId') || '',
      token: localStorage.getItem('token') || 'test-token'
    }
    sendWebSocketMessage(authMsg)
  }

  /**
   * 设置 WebSocket 事件监听器
   */
  const setupWebSocketListeners = () => {
    const listenersRegistered =
      window.api?.webSocketListenersRegistered || window.webSocketListenersRegistered

    if (!listenersRegistered && window.api) {
      // 监听连接打开事件
      window.api.onWebSocketOpen(() => {
        console.log('WebSocket connection established')
        sendAuthMessage()
      })

      // 监听消息接收事件
      window.api.onWebSocketMessage((message: string | Uint8Array) => {
        handleWebSocketMessage(message)
      })

      // 监听连接关闭事件
      window.api.onWebSocketClose((code: number, reason: string) => {
        handleWebSocketClose(code, reason)
      })

      // 监听错误事件
      window.api.onWebSocketError((error: any) => {
        console.error('WebSocket error:', error)
      })

      // 标记监听器已注册
      if (window.api && Object.isExtensible(window.api)) {
        window.api.webSocketListenersRegistered = true
      } else {
        window.webSocketListenersRegistered = true
      }
    }
  }

  /**
   * 处理接收到的 WebSocket 消息
   */
  const handleWebSocketMessage = (message: string | Uint8Array) => {
    console.log('WebSocket message received:', message)

    // 处理字符串和 Uint8Array 类型的消息
    let messageString: string
    if (message instanceof Uint8Array) {
      messageString = new TextDecoder().decode(message)
    } else if (typeof message === 'string') {
      messageString = message
    } else {
      console.error('Unknown WebSocket message type:', typeof message)
      return
    }

    try {
      const parsedMessage: WebSocketMessage = JSON.parse(messageString)
      console.log('Parsed WebSocket message:', parsedMessage)
      processMessage(parsedMessage)
    } catch (error) {
      console.error('Failed to parse WebSocket message:', error)
    }
  }

  /**
   * 处理不同类型的消息
   */
  const processMessage = (message: WebSocketMessage) => {
    const { msgType, content, fromId, toId } = message

    switch (msgType) {
      case 'auth':
        console.log('Authentication response:', content)
        break

      case 'orderUpdate':
        console.log('Order update received:', content)
        // TODO: 更新订单状态的UI
        break

      case 'chat':
        console.log('Chat message from', fromId, 'to', toId, ':', content)
        // TODO: 更新聊天UI
        break

      case 'system':
        console.log('System message:', content)
        // TODO: 显示系统通知
        break

      default:
        console.log('Unknown message type:', msgType)
    }
  }

  /**
   * 处理 WebSocket 连接关闭
   */
  const handleWebSocketClose = (code: number, reason: string) => {
    console.log('WebSocket connection closed:', code, reason)

    // 如果未达到最大尝试次数则自动重连
    if (wsAttempts < maxAttempts) {
      wsAttempts++
      const delay = Math.min(
        HOME_CONSTANTS.WS.RETRY_DELAY_BASE * wsAttempts,
        HOME_CONSTANTS.WS.RETRY_DELAY_MAX
      )

      setTimeout(() => {
        console.log(`Reconnecting WebSocket... Attempt ${wsAttempts}/${maxAttempts}`)
        initializeWebSocket()
      }, delay)
    }
  }

  return {
    initializeWebSocket,
    setupWebSocketListeners,
    sendWebSocketMessage
  }
}
