/**
 * WebSocket 相关类型定义
 */

export type WebSocketMessageType = 'auth' | 'orderUpdate' | 'chat' | 'system'

export interface WebSocketMessage {
  msgType: WebSocketMessageType
  content?: string
  fromId?: string
  toId?: string
}

export interface WebSocketAuthMessage extends WebSocketMessage {
  msgType: 'auth'
  userId: string
  token: string
}
