/**
 * 消息管理 API
 * 对接后端 MessageController
 * 基础路径: /v1/messages
 */
import { get, post } from '@/utils/request'
import { CHAT_API, buildUrl } from '../urlEnum'

export const messageApi = {
  /**
   * IM-002: 获取消息列表
   * GET /v1/messages
   * @param {Object} params - 查询参数
   * @param {string} params.conversationId - 会话ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} 返回消息列表
   */
  getMessages: (params) => get(CHAT_API.GET_MESSAGES, params),

  /**
   * 获取消息列表（旧版）
   * GET /v1/legacy/message/list
   * @param {Object} params - 查询参数
   * @returns {Promise} 返回消息列表
   */
  getMessagesLegacy: (params) => get('/v1/legacy/message/list', params),

  /**
   * IM-004: 发送文本消息
   * POST /v1/messages
   * @param {Object} data - 消息数据
   * @param {string} data.senderId - 发送者ID
   * @param {string} data.receiverId - 接收者ID
   * @param {string} data.content - 消息内容
   * @param {string} data.messageType - 消息类型(text)
   * @returns {Promise} 返回发送结果
   */
  sendTextMessage: (data) => post(CHAT_API.SEND_MESSAGE, {
    senderId: data.senderId,
    receiverId: data.receiverId,
    content: data.content,
    messageType: 'text'
  }),

  /**
   * 发送文本消息（旧版）
   * POST /v1/legacy/message/send
   * @param {Object} data - 消息数据
   * @returns {Promise} 返回发送结果
   */
  sendTextMessageLegacy: (data) => post('/v1/legacy/message/send', {
    senderId: data.senderId,
    receiverId: data.receiverId,
    content: data.content,
    messageType: 'text'
  }),

  /**
   * IM-005: 发送图片消息
   * POST /v1/messages
   * @param {Object} data - 消息数据
   * @param {string} data.senderId - 发送者ID
   * @param {string} data.receiverId - 接收者ID
   * @param {string} data.imageUrl - 图片URL
   * @param {string} data.messageType - 消息类型(image)
   * @returns {Promise} 返回发送结果
   */
  sendImageMessage: (data) => post(CHAT_API.SEND_MESSAGE, {
    senderId: data.senderId,
    receiverId: data.receiverId,
    content: data.imageUrl,
    messageType: 'image'
  }),

  /**
   * 发送图片消息（旧版）
   * POST /v1/legacy/message/send
   * @param {Object} data - 消息数据
   * @returns {Promise} 返回发送结果
   */
  sendImageMessageLegacy: (data) => post('/v1/legacy/message/send', {
    senderId: data.senderId,
    receiverId: data.receiverId,
    content: data.imageUrl,
    messageType: 'image'
  }),

  /**
   * 发送菜品卡片消息
   * POST /v1/messages
   * @param {Object} data - 消息数据
   * @param {string} data.senderId - 发送者ID
   * @param {string} data.receiverId - 接收者ID
   * @param {string} data.dishId - 菜品ID
   * @param {string} data.messageType - 消息类型(dish)
   * @returns {Promise} 返回发送结果
   */
  sendDishMessage: (data) => post(CHAT_API.SEND_MESSAGE, {
    senderId: data.senderId,
    receiverId: data.receiverId,
    content: JSON.stringify({ dishId: data.dishId }),
    messageType: 'dish'
  }),

  /**
   * 发送订单卡片消息
   * POST /v1/messages
   * @param {Object} data - 消息数据
   * @param {string} data.senderId - 发送者ID
   * @param {string} data.receiverId - 接收者ID
   * @param {string} data.orderId - 订单ID
   * @param {string} data.messageType - 消息类型(order)
   * @returns {Promise} 返回发送结果
   */
  sendOrderMessage: (data) => post(CHAT_API.SEND_MESSAGE, {
    senderId: data.senderId,
    receiverId: data.receiverId,
    content: JSON.stringify({ orderId: data.orderId }),
    messageType: 'order'
  })
}

export default messageApi
