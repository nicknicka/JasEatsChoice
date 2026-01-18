/**
 * WebSocket 聊天连接管理
 */
import { ref, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { WEBSOCKET_CONFIG, MESSAGE_TYPES } from '@/constants/chatConstants'

export function useWebSocketChat({ userId, token, onMessage }) {
  const websocket = ref(null)
  const reconnectTimer = ref(null)
  const heartbeatTimer = ref(null)
  const isConnected = ref(false)

  // 消息去重
  const receivedMessageIds = ref(new Set())
  const reconnectAttempts = ref(0)

  /**
   * 检查消息是否已接收
   */
  const isMessageReceived = (messageId) => {
    return receivedMessageIds.value.has(messageId)
  }

  /**
   * 标记消息已接收
   */
  const markMessageReceived = (messageId) => {
    receivedMessageIds.value.add(messageId)
    cleanOldMessageIds()
  }

  /**
   * 清理过期的消息ID
   */
  const cleanOldMessageIds = () => {
    if (receivedMessageIds.value.size > WEBSOCKET_CONFIG.MAX_RECEIVED_IDS) {
      const idsArray = Array.from(receivedMessageIds.value)
      receivedMessageIds.value = new Set(
        idsArray.slice(Math.floor(WEBSOCKET_CONFIG.MAX_RECEIVED_IDS / 2))
      )
    }
  }

  /**
   * 计算重连延迟（指数退避策略）
   */
  const calculateReconnectDelay = () => {
    const delay = Math.min(
      WEBSOCKET_CONFIG.BASE_RECONNECT_DELAY * Math.pow(2, reconnectAttempts.value),
      WEBSOCKET_CONFIG.MAX_RECONNECT_DELAY
    )
    // 添加随机抖动，避免同时重连
    return delay + Math.random() * 1000
  }

  /**
   * 重置重连计数
   */
  const resetReconnectAttempts = () => {
    reconnectAttempts.value = 0
  }

  /**
   * 启动心跳
   */
  const startHeartbeat = () => {
    heartbeatTimer.value = setInterval(() => {
      if (websocket.value && websocket.value.readyState === WebSocket.OPEN) {
        websocket.value.send(JSON.stringify({ type: MESSAGE_TYPES.HEARTBEAT }))
      }
    }, WEBSOCKET_CONFIG.HEARTBEAT_INTERVAL)
  }

  /**
   * 停止心跳
   */
  const stopHeartbeat = () => {
    if (heartbeatTimer.value) {
      clearInterval(heartbeatTimer.value)
      heartbeatTimer.value = null
    }
  }

  /**
   * 初始化 WebSocket 连接
   */
  const initWebSocket = () => {
    // 检查是否已有连接
    if (
      websocket.value &&
      (websocket.value.readyState === WebSocket.CONNECTING ||
        websocket.value.readyState === WebSocket.OPEN)
    ) {
      console.log('WebSocket连接已存在或正在连接中');
      return
    }

    // 检查是否超过最大重连次数
    if (reconnectAttempts.value >= WEBSOCKET_CONFIG.MAX_RECONNECT_ATTEMPTS) {
      console.error('WebSocket重连次数已达上限，停止重连')
      ElMessage.error('连接已断开，请刷新页面重新连接')
      return
    }

    try {
      // 构建 WebSocket URL - 修改为正确的格式
      const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
      // 使用固定的端口11277，后端Netty服务器配置的端口
      const wsUrl = `${wsProtocol}//localhost:11277/ws/chat?userId=${userId.value}&token=${token}`

      console.log('尝试连接到WebSocket服务器:', wsUrl)
      
      websocket.value = new WebSocket(wsUrl)

      websocket.value.onopen = () => {
        console.log('WebSocket 连接成功')
        isConnected.value = true
        resetReconnectAttempts()
        startHeartbeat()

        if (reconnectTimer.value) {
          clearTimeout(reconnectTimer.value)
          reconnectTimer.value = null
        }
        
        // 重置重连尝试次数
        reconnectAttempts.value = 0
      }

      websocket.value.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)

          // 消息去重
          if (data.content && data.content.id) {
            if (isMessageReceived(data.content.id)) {
              console.log('重复消息已忽略:', data.content.id)
              return
            }
            markMessageReceived(data.content.id)
          }

          onMessage(data)
        } catch (error) {
          console.error('解析 WebSocket 消息失败:', error)
        }
      }

      websocket.value.onerror = (error) => {
        console.error('WebSocket 错误:', error)
        // 发生错误时，尝试立即重连
        if (reconnectAttempts.value < WEBSOCKET_CONFIG.MAX_RECONNECT_ATTEMPTS) {
          setTimeout(() => {
            reconnectAttempts.value++
            initWebSocket()
          }, 1000) // 错误后1秒重连
        }
      }

      websocket.value.onclose = (event) => {
        console.log('WebSocket 连接关闭，代码:', event.code, '原因:', event.reason)
        isConnected.value = false
        stopHeartbeat()

        // 自动重连
        if (reconnectAttempts.value < WEBSOCKET_CONFIG.MAX_RECONNECT_ATTEMPTS) {
          const delay = calculateReconnectDelay()
          console.log(
            `将在${Math.round(delay / 1000)}秒后尝试第${reconnectAttempts.value + 1}次重连...`
          )

          reconnectTimer.value = setTimeout(() => {
            reconnectAttempts.value++
            initWebSocket()
          }, delay)
        } else {
          console.error('WebSocket重连失败，已达到最大重连次数')
          ElMessage.error('连接已断开，请刷新页面重新连接')
        }
      }
    } catch (error) {
      console.error('初始化 WebSocket 失败:', error)
      // 如果初始化失败，也尝试重连
      if (reconnectAttempts.value < WEBSOCKET_CONFIG.MAX_RECONNECT_ATTEMPTS) {
        setTimeout(() => {
          reconnectAttempts.value++
          initWebSocket()
        }, 1000)
      }
    }
  }

  /**
   * 关闭 WebSocket 连接
   */
  const closeWebSocket = () => {
    console.log('正在关闭WebSocket连接...')
    
    if (websocket.value) {
      // 移除所有事件监听器
      websocket.value.onopen = null
      websocket.value.onmessage = null
      websocket.value.onerror = null
      websocket.value.onclose = null
      
      // 关闭连接
      websocket.value.close()
      websocket.value = null
    }
    
    if (reconnectTimer.value) {
      clearTimeout(reconnectTimer.value)
      reconnectTimer.value = null
    }
    
    stopHeartbeat()
    isConnected.value = false
    
    console.log('WebSocket连接已关闭')
  }

  /**
   * 发送消息
   */
  const sendMessage = (message) => {
    if (websocket.value && websocket.value.readyState === WebSocket.OPEN) {
      websocket.value.send(JSON.stringify(message))
      return true
    }
    console.warn('WebSocket未连接，无法发送消息:', message)
    return false
  }

  // 组件卸载时清理
  onBeforeUnmount(() => {
    closeWebSocket()
  })

  return {
    isConnected,
    initWebSocket,
    closeWebSocket,
    sendMessage
  }
}