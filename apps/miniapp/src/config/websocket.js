/**
 * WebSocket 配置
 */
import config from './index'

// WebSocket 服务器地址：H5 走同源，小程序/App 用配置里的绝对地址（VITE_WS_URL）
export const getWebSocketUrl = (path = '') => {
  let wsUrl = ''

  #ifdef H5
  // H5环境
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = window.location.host
  wsUrl = `${protocol}//${host}/ws${path}`
  #endif

  #ifdef MP-WEIXIN
  // 微信小程序环境
  wsUrl = `${config.wsURL}${path}`
  #endif

  #ifdef APP-PLUS
  // App环境
  wsUrl = `${config.wsURL}${path}`
  #endif

  return wsUrl
}

// WebSocket 连接配置
export const WSConfig = {
  // 心跳间隔（毫秒）
  HEARTBEAT_INTERVAL: 30000,

  // 重连间隔（毫秒）
  RECONNECT_INTERVAL: 3000,

  // 最大重连次数
  MAX_RECONNECT_ATTEMPTS: 5,

  // 连接超时时间（毫秒）
  CONNECTION_TIMEOUT: 10000,

  // 消息类型
  MessageType: {
    TEXT: 'text',
    IMAGE: 'image',
    DISH: 'dish',
    ORDER: 'order',
    SYSTEM: 'system'
  },

  // 消息状态
  MessageStatus: {
    SENDING: 'sending',
    SUCCESS: 'success',
    FAILED: 'failed'
  }
}

export default {
  getWebSocketUrl,
  WSConfig
}
