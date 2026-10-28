/**
 * 订单/推荐 WebSocket 管理组合式函数
 *
 * 连接是模块级共享的：用户端布局层与订单页会同时使用本组合式函数，
 * 若各自新建连接，同一用户就会产生多条连接（后端虽能各自投递，
 * 但无谓地占用连接且容易分叉状态）。这里用引用计数保证整棵用户端
 * 组件树只维持一条连接，最后一个使用者卸载时才真正断开。
 */
import { ref, onUnmounted } from 'vue'
import { WS_CONFIG } from '../config'
import { ElMessage, ElNotification } from 'element-plus'
import { getOrderStatusText, orderStatusToText } from '../utils/orderStatus'
import router from '../router'
import pinia from '../store'
import { useAuthStore } from '../store/authStore'

// WebSocket 配置常量
const MAX_RETRIES = 3
const RETRY_DELAY_BASE = 3000 // 3秒
const RETRY_DELAY_MAX = 30000 // 30秒
const HEARTBEAT_INTERVAL = 30000 // 30秒心跳

// 推荐类消息类型：后端 MsgType 中与推荐相关的取值
const RECOMMENDATION_MESSAGE_TYPES = [
  'recommendation',
  'recommend_update',
  'new_dish_recommend',
  'realtime_recommend'
]

// ==== 模块级共享状态（整棵用户端组件树共用一条连接）====
const ws = ref(null)
const isConnected = ref(false)
const retryCount = ref(0)
let heartbeatTimer = null
let reconnectTimer = null
let shouldReconnect = true
// 使用者计数：布局层与订单页同时挂载时为 2，降到 0 才断开
let subscriberCount = 0
// 订单更新订阅者与推荐消息订阅者
const orderUpdateListeners = new Set()
const recommendationListeners = new Set()

/**
 * 发送认证消息（连接建立后）
 */
function sendAuthMessage() {
  const authStore = useAuthStore(pinia)
  const userId = authStore.userId || '1'
  const token = authStore.token || ''

  if (!token) {
    console.error('未找到认证 token，无法发送认证消息')
    return
  }

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
 * 处理订单更新消息
 */
function handleOrderUpdate(message) {
  const orderUpdate = message.content || message.data || message

  if (!orderUpdate || (!orderUpdate.id && !orderUpdate.orderId)) {
    console.warn('无效的订单更新消息:', message)
    return
  }

  orderUpdateListeners.forEach((listener) => {
    try {
      listener(orderUpdate)
    } catch (error) {
      console.error('订单更新回调执行失败:', error)
    }
  })

  const statusText = getOrderStatusText(orderStatusToText(orderUpdate.status))
  ElMessage.info(`订单 ${orderUpdate.id || orderUpdate.orderId} 状态已更新为: ${statusText}`)
}

/**
 * 处理推荐消息：非阻塞提示，点击进入推荐页
 */
function handleRecommendation(message) {
  const items = message.data || message.content || []
  const count = Array.isArray(items) ? items.length : 0

  recommendationListeners.forEach((listener) => {
    try {
      listener(message)
    } catch (error) {
      console.error('推荐消息回调执行失败:', error)
    }
  })

  const period = message.context || ''
  const text = count > 0
    ? `${period ? period + '：' : ''}为你推荐了 ${count} 道菜，点击查看`
    : '有新的推荐内容，点击查看'

  ElNotification({
    title: '为你推荐',
    message: text,
    type: 'info',
    duration: 6000,
    onClick: () => {
      router.push('/user/home/recommend')
    }
  })
}

/**
 * 处理接收到的消息
 */
function handleMessage(event) {
  try {
    const message = JSON.parse(event.data)
    const msgType = message.msgType || message.type

    if (msgType === 'orderUpdate') {
      handleOrderUpdate(message)
      return
    }

    if (RECOMMENDATION_MESSAGE_TYPES.includes(msgType)) {
      handleRecommendation(message)
    }
  } catch (error) {
    console.error('解析WebSocket消息失败:', error)
  }
}

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

function stopHeartbeat() {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
}

/**
 * 建立连接并挂载事件处理器（仅在没有连接时调用）
 */
function openConnection() {
  const authStore = useAuthStore(pinia)
  const userId = authStore.userId || '1'
  const token = authStore.token || ''

  if (!token) {
    console.error('未找到认证 token，无法连接WebSocket')
    return
  }

  shouldReconnect = true
  const wsUrl = `${WS_CONFIG.url}?userId=${userId}&token=${token}`
  ws.value = new WebSocket(wsUrl)

  ws.value.onopen = () => {
    isConnected.value = true
    retryCount.value = 0
    sendAuthMessage()
    startHeartbeat()
  }

  ws.value.onmessage = (event) => {
    handleMessage(event)
  }

  ws.value.onclose = () => {
    isConnected.value = false
    stopHeartbeat()
    if (shouldReconnect && subscriberCount > 0) {
      handleReconnect()
    }
  }

  ws.value.onerror = (error) => {
    console.error('WebSocket连接错误:', error)
    isConnected.value = false
  }
}

function handleReconnect() {
  if (retryCount.value >= MAX_RETRIES) {
    console.error('达到最大重试次数，停止重连')
    return
  }

  retryCount.value++
  const delay = Math.min(RETRY_DELAY_BASE * retryCount.value, RETRY_DELAY_MAX)

  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
  }

  reconnectTimer = setTimeout(() => {
    reconnectTimer = null
    if (subscriberCount > 0) {
      openConnection()
    }
  }, delay)
}

function closeConnection() {
  shouldReconnect = false
  stopHeartbeat()

  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }

  if (ws.value) {
    ws.value.close()
    ws.value = null
  }

  isConnected.value = false
  retryCount.value = 0
}

/**
 * 订单/推荐 WebSocket 管理
 * @param {Object} options - 订阅选项
 * @param {Function} [options.onOrderUpdate] - 订单更新回调
 * @param {Function} [options.onRecommendation] - 推荐消息回调
 */
export function useOrderWebSocket(options = {}) {
  const { onOrderUpdate, onRecommendation } = options

  if (onOrderUpdate) {
    orderUpdateListeners.add(onOrderUpdate)
  }
  if (onRecommendation) {
    recommendationListeners.add(onRecommendation)
  }

  /**
   * 初始化 WebSocket 连接（共享连接，重复调用不会新建）
   */
  function initWebSocket() {
    if (ws.value) {
      return
    }
    try {
      openConnection()
    } catch (error) {
      console.error('初始化WebSocket失败:', error)
      handleReconnect()
    }
  }

  function reconnect() {
    retryCount.value = 0
    closeConnection()
    openConnection()
  }

  function disconnect() {
    if (onOrderUpdate) {
      orderUpdateListeners.delete(onOrderUpdate)
    }
    if (onRecommendation) {
      recommendationListeners.delete(onRecommendation)
    }

    subscriberCount = Math.max(0, subscriberCount - 1)
    if (subscriberCount === 0) {
      closeConnection()
    }
  }

  // 每个使用者占用一次引用；重复挂载不会重复建立连接
  subscriberCount++

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
