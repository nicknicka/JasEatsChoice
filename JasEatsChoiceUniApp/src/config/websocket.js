/**
 * WebSocket 配置
 */

// WebSocket 服务器地址（根据环境自动切换）
export const getWebSocketUrl = (path = '') => {
  // 获取当前页面协议
  const pages = getCurrentPages()
  const url = pages[pages.length - 1].route || ''

  // 根据当前页面URL判断环境
  let wsUrl = ''

  #ifdef H5
  // H5环境
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = window.location.host
  wsUrl = `${protocol}//${host}/ws${path}`
  #endif

  #ifdef MP-WEIXIN
  // 微信小程序环境
  wsUrl = 'wss://api.example.com/ws' + path
  #endif

  #ifdef APP-PLUS
  // App环境
  wsUrl = 'wss://api.example.com/ws' + path
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
