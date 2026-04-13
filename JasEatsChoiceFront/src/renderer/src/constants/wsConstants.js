// WebSocket配置
// 注意：URL 从集中配置导入，部署 Nginx 时只需修改 config/index.js
import { ENV_CONFIG } from '@/config'

const _wsBase = ENV_CONFIG.wsURL || 'ws://localhost:11277/ws'

export const WS_CONFIG = {
  URL: _wsBase.replace(/\/ws\/?$/, ''), // 去掉末尾 /ws，保留协议+host
  ENDPOINT: '/ws/chat' // WebSocket端点路径（必须与后端NettyServer配置一致）
}

// WebSocket消息类型
export const WS_MESSAGE_TYPE = {
  AUTH: 'auth', // 身份验证
  ORDER_UPDATE: 'orderUpdate', // 订单更新
  CHAT: 'chat', // 聊天消息
  SYSTEM: 'system' // 系统消息
}
