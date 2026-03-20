import { get, post } from '../request'

/**
 * 消息管理 API
 */
export const messageApi = {
  /**
   * IM-002: 获取消息列表
   */
  getMessages: (params) => get('/v1/legacy/message/list', params),

  /**
   * IM-004: 发送文本消息
   */
  sendTextMessage: (data) => post('/v1/legacy/message/send', {
    senderId: data.senderId,
    receiverId: data.receiverId,
    content: data.content,
    messageType: 'text'
  }),

  /**
   * IM-005: 发送图片消息
   */
  sendImageMessage: (data) => post('/v1/legacy/message/send', {
    senderId: data.senderId,
    receiverId: data.receiverId,
    content: data.imageUrl,
    messageType: 'image'
  })
}
