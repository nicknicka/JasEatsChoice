/**
 * 消息管理 API
 * 对接后端 ChatController
 */
import { get, post, put } from '@/utils/request'
import { CHAT_API, buildUrl } from '../urlEnum'

const getCurrentUserId = () => {
  const userInfo = uni.getStorageSync('userInfo') || {}
  return userInfo.userId || userInfo.id || uni.getStorageSync('userId') || ''
}

const normalizeMessageItem = (message = {}) => ({
  ...message,
  id: message.id || message.msgId,
  msgId: message.msgId || message.id,
  senderId: message.senderId || message.fromId,
  receiverId: message.receiverId || message.toId,
  messageType: message.messageType || message.msgType || 'text',
  senderNickname: message.senderNickname || message.fromName,
  senderAvatar: message.senderAvatar || message.fromAvatar
})

const normalizeMessageResponse = (response) => {
  const pageData = response?.data || {}
  const list = Array.isArray(pageData.records)
    ? pageData.records.map(normalizeMessageItem)
    : Array.isArray(pageData)
      ? pageData.map(normalizeMessageItem)
      : []

  return {
    ...response,
    list,
    pageData,
    data: list
  }
}

const resolveMessageQuery = (conversationIdOrParams, maybeParams) => {
  if (typeof conversationIdOrParams === 'object' || conversationIdOrParams === undefined) {
    return { ...(conversationIdOrParams || {}) }
  }

  return {
    ...(maybeParams || {}),
    conversationId: conversationIdOrParams
  }
}

const buildMessagePayload = (data = {}, msgType, content) => ({
  fromId: data.senderId || data.fromId || getCurrentUserId(),
  toId: data.receiverId || data.toId || data.targetId || '',
  sessionType: data.sessionType || 'single',
  msgType,
  content,
  replyTo: data.replyTo,
  sessionId: data.conversationId || data.sessionId
})

export const messageApi = {
  /**
   * 获取消息列表
   * GET /v1/chat/{conversationId}/messages
   * @param {Object|string} conversationId - 会话ID或查询参数对象
   * @param {Object} params - 查询参数
   * @returns {Promise} 返回消息列表
   */
  getMessages: (conversationId, params) => {
    const query = resolveMessageQuery(conversationId, params)
    const conversationKey = query.conversationId || query.sessionId

    return get(buildUrl(CHAT_API.GET_MESSAGES, { conversationId: conversationKey }), {
      userId: query.userId || getCurrentUserId(),
      page: query.page || query.pageNum || 1,
      size: query.size || query.pageSize || 20
    }).then(normalizeMessageResponse)
  },

  /**
   * 获取消息列表（旧版）
   * @param {Object|string} conversationId - 会话ID或查询参数对象
   * @param {Object} params - 查询参数
   * @returns {Promise} 返回消息列表
   */
  getMessagesLegacy: (conversationId, params) => messageApi.getMessages(conversationId, params),

  /**
   * 发送文本消息
   * POST /v1/chat/messages
   * @param {Object} data - 消息数据
   * @returns {Promise} 返回发送结果
   */
  sendTextMessage: (data) => post(
    CHAT_API.SEND_MESSAGE,
    buildMessagePayload(data, 'text', data.content)
  ),

  /**
   * 发送文本消息（旧版）
   * @param {Object} data - 消息数据
   * @returns {Promise} 返回发送结果
   */
  sendTextMessageLegacy: (data) => messageApi.sendTextMessage(data),

  /**
   * 发送图片消息
   * POST /v1/chat/messages
   * @param {Object} data - 消息数据
   * @returns {Promise} 返回发送结果
   */
  sendImageMessage: (data) => post(
    CHAT_API.SEND_MESSAGE,
    buildMessagePayload(data, 'image', data.imageUrl || data.content)
  ),

  /**
   * 发送图片消息（旧版）
   * @param {Object} data - 消息数据
   * @returns {Promise} 返回发送结果
   */
  sendImageMessageLegacy: (data) => messageApi.sendImageMessage(data),

  /**
   * 发送菜品卡片消息
   * @param {Object} data - 消息数据
   * @returns {Promise} 返回发送结果
   */
  sendDishMessage: (data) => post(
    CHAT_API.SEND_MESSAGE,
    buildMessagePayload(data, 'dish', JSON.stringify({ dishId: data.dishId }))
  ),

  /**
   * 发送订单卡片消息
   * @param {Object} data - 消息数据
   * @returns {Promise} 返回发送结果
   */
  sendOrderMessage: (data) => post(
    CHAT_API.SEND_MESSAGE,
    buildMessagePayload(data, 'order', JSON.stringify({ orderId: data.orderId }))
  ),

  /**
   * 标记消息已读
   * @param {string} messageId - 消息ID
   * @returns {Promise}
   */
  markRead: (messageId) => put(buildUrl(CHAT_API.MARK_READ, { messageId }))
}

export default messageApi
