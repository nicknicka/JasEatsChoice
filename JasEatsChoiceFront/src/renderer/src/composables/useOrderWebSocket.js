/**
 * 订单 WebSocket 管理组合式函数
 */
import { ref, onUnmounted } from 'vue'
import { WS_CONFIG } from '../config'
import { ElMessage } from 'element-plus'
import { ORDER_STATUS_MAP } from '../utils/orderStatus'

// WebSocket 配置常量
const MAX_RETRIES = 3
const RETRY_DELAY_BASE = 3000 // 3秒
const RETRY_DELAY_MAX = 30000 // 30秒
const HEARTBEAT_INTERVAL = 30000 // 30秒心跳

/**
 * 订单 WebSocket 管理
 * @param {Function} onOrderUpdate - 订单更新回调函数
 */
export function useOrderWebSocket(onOrderUpdate) {
  const ws = ref(null)
  const isConnected = ref(false)
  const retryCount = ref(0)
  let heartbeatTimer = null

  /**
   * 初始化 WebSocket 连接
   */
  function initWebSocket() {
    try {
      ws.value = new WebSocket(WS_CONFIG.url)

      setupWebSocketHandlers()
    } catch (error) {
      console.error('初始化WebSocket失败:', error)
      ElMessage.error('WebSocket连接失败，无法接收实时订单更新')
      handleReconnect()
    }
  }

  /**
   * 设置 WebSocket 事件处理器
   */
  function setupWebSocketHandlers() {
    if (!ws.value) return

    // 连接成功
    ws.value.onopen = () => {
      console.log('WebSocket连接成功')
      isConnected.value = true
      retryCount.value = 0

      // 发送认证信息
      sendAuthMessage()

      // 启动心跳
      startHeartbeat()
    }

    // 接收消息
    ws.value.onmessage = (event) => {
      handleMessage(event)
    }

    // 连接关闭
    ws.value.onclose = () => {
      console.log('WebSocket连接关闭')
      isConnected.value = false
      stopHeartbeat()
      handleReconnect()
    }

    // 连接错误
    ws.value.onerror = (error) => {
      console.error('WebSocket连接错误:', error)
      isConnected.value = false
    }
  }

  /**
   * 发送认证消息
   */
  function sendAuthMessage() {
    const userId = localStorage.getItem('userId') || '1'
    const token = localStorage.getItem('token') || 'test-token'

    try {
      if (ws.value && ws.value.readyState === WebSocket.OPEN) {
        ws.value.send(
          JSON.stringify({
            msgType: 'auth',
            fromId: userId,
            toId: '',
            content: '',
            token
          })
        )
      }
    } catch (error) {
      console.error('发送认证消息失败:', error)
    }
  }

  /**
   * 处理接收到的消息
   * @param {MessageEvent} event - WebSocket 消息事件
   */
  function handleMessage(event) {
    try {
      const message = JSON.parse(event.data)

      // 处理订单更新消息
      if (message.msgType === 'orderUpdate' || message.type === 'orderUpdate') {
        handleOrderUpdate(message)
      }
    } catch (error) {
      console.error('解析WebSocket消息失败:', error)
    }
  }

  /**
   * 处理订单更新
   * @param {Object} message - 订单更新消息
   */
  function handleOrderUpdate(message) {
    const orderUpdate = message.content || message.data || message

    if (!orderUpdate || !orderUpdate.id) {
      console.warn('无效的订单更新消息:', message)
      return
    }

    // 调用回调函数更新本地订单状态
    if (onOrderUpdate) {
      onOrderUpdate(orderUpdate)
    }

    // 显示更新提示
    const statusText = ORDER_STATUS_MAP[orderUpdate.status] || orderUpdate.status
    ElMessage.info(`订单 ${orderUpdate.id} 状态已更新为: ${statusText}`)
  }

  /**
   * 启动心跳
   */
  function startHeartbeat() {
    stopHeartbeat()

    heartbeatTimer = setInterval(() => {
      if (ws.value && ws.value.readyState === WebSocket.OPEN) {
        try {
          ws.value.send(JSON.stringify({ msgType: 'heartbeat' }))
        } catch (error) {
          console.error('发送心跳失败:', error)
        }
      }
    }, HEARTBEAT_INTERVAL)
  }

  /**
   * 停止心跳
   */
  function stopHeartbeat() {
    if (heartbeatTimer) {
      clearInterval(heartbeatTimer)
      heartbeatTimer = null
    }
  }

  /**
   * 处理重连
   */
  function handleReconnect() {
    if (retryCount.value >= MAX_RETRIES) {
      console.error('达到最大重试次数，停止重连')
      ElMessage.error('WebSocket连接失败，请刷新页面重试')
      return
    }

    retryCount.value++
    const delay = Math.min(RETRY_DELAY_BASE * retryCount.value, RETRY_DELAY_MAX)

    console.log(`${delay}ms 后尝试第 ${retryCount.value} 次重连...`)

    setTimeout(() => {
      initWebSocket()
    }, delay)
  }

  /**
   * 手动重连
   */
  function reconnect() {
    retryCount.value = 0
    disconnect()
    initWebSocket()
  }

  /**
   * 断开连接
   */
  function disconnect() {
    stopHeartbeat()

    if (ws.value) {
      ws.value.close()
      ws.value = null
    }

    isConnected.value = false
  }

  // 组件卸载时自动断开连接
  onUnmounted(() => {
    disconnect()
  })

  return {
    // 状态
    isConnected,
    retryCount,

    // 方法
    initWebSocket,
    disconnect,
    reconnect
  }
}
