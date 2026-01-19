// WebSocket配置
export const WS_CONFIG = {
  URL: 'ws://localhost:11277', // WebSocket服务器地址
  ENDPOINT: '/ws/chat' // WebSocket端点路径（必须与后端NettyServer配置一致）
}

// WebSocket消息类型
export const WS_MESSAGE_TYPE = {
  AUTH: 'auth', // 身份验证
  ORDER_UPDATE: 'orderUpdate', // 订单更新
  CHAT: 'chat', // 聊天消息
  SYSTEM: 'system' // 系统消息
}
