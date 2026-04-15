/**
 * 聊天相关API
 * 对接后端 ChatSessionController / ChatController
 */
import { get, post, put, del } from '@/utils/request'
import { CHAT_API, buildUrl } from '../urlEnum'

const getCurrentUserId = () => {
  const userInfo = uni.getStorageSync('userInfo') || {}
  return userInfo.userId || userInfo.id || uni.getStorageSync('userId') || ''
}

const buildQueryUrl = (url, params = {}) => {
  const query = Object.entries(params)
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join('&')

  return query ? `${url}?${query}` : url
}

const normalizeConversation = (session = {}) => ({
  ...session,
  id: session.id || session.sessionId,
  conversationId: session.conversationId || session.sessionId || session.id,
  sessionId: session.sessionId || session.id || session.conversationId,
  targetUserId: session.targetUserId || session.targetId || '',
  unreadCount: Number(session.unreadCount || 0)
})

const normalizeConversationList = (response) => {
  const list = Array.isArray(response?.data) ? response.data.map(normalizeConversation) : []
  return {
    ...response,
    data: list,
    list
  }
}

const normalizeConversationResponse = (response) => {
  const data = normalizeConversation(response?.data || response || {})
  return {
    ...response,
    conversationId: data.conversationId,
    sessionId: data.sessionId,
    id: data.id,
    data
  }
}

const sumUnreadCount = (sessions = []) => sessions.reduce(
  (total, item) => total + Number(item.unreadCount || 0),
  0
)

export const chatApi = {
  /**
   * 获取会话列表
   * GET /v1/chat/users/{userId}/chat-sessions
   * @param {string} userId - 用户ID
   * @returns {Promise} 返回会话列表
   */
  getConversations: (userId = getCurrentUserId()) => get(
    buildUrl(CHAT_API.GET_CONVERSATIONS, { userId })
  ).then(normalizeConversationList),

  /**
   * 获取会话列表（别名）
   * @param {string} userId - 用户ID
   * @returns {Promise} 返回会话列表
   */
  getConversationsOld: (userId) => chatApi.getConversations(userId),

  /**
   * 获取会话详情
   * 当前后端未提供单独详情接口，先从列表中筛选
   * @param {string} conversationId - 会话ID
   * @param {string} userId - 用户ID
   * @returns {Promise<Object|null>} 返回会话详情
   */
  getConversation: async (conversationId, userId = getCurrentUserId()) => {
    const response = await chatApi.getConversations(userId)
    return response.data.find(item => item.conversationId === conversationId) || null
  },

  /**
   * 获取会话详情（别名）
   * @param {string} conversationId - 会话ID
   * @param {string} userId - 用户ID
   * @returns {Promise<Object|null>} 返回会话详情
   */
  getConversationOld: (conversationId, userId) => chatApi.getConversation(conversationId, userId),

  /**
   * 创建会话
   * POST /v1/chat/sessions
   * @param {Object} data - 会话数据
   * @returns {Promise} 返回创建结果
   */
  createConversation: (data = {}) => {
    const userId = data.userId || getCurrentUserId()
    const targetId = data.targetUserId || data.targetId || data.merchantId || ''

    return post(CHAT_API.CREATE_CONVERSATION, {
      userId,
      targetId,
      sessionType: data.type || data.sessionType || 'single',
      sessionName: data.sessionName || data.targetName || data.userName || '聊天会话',
      avatar: data.avatar || data.targetAvatar || '',
      relatedDishId: data.dishId,
      relatedOrderId: data.orderId
    }).then(normalizeConversationResponse)
  },

  /**
   * 创建会话（别名）
   * @param {Object} data - 会话数据
   * @returns {Promise} 返回创建结果
   */
  createConversationOld: (data) => chatApi.createConversation(data),

  /**
   * 删除会话
   * DELETE /v1/chat/sessions/{conversationId}?userId=xxx
   * @param {string} conversationId - 会话ID
   * @param {string} userId - 用户ID
   * @returns {Promise} 返回删除结果
   */
  deleteConversation: (conversationId, userId = getCurrentUserId()) => del(
    buildQueryUrl(buildUrl(CHAT_API.DELETE_CONVERSATION, { conversationId }), { userId })
  ),

  /**
   * 删除会话（别名）
   * @param {string} conversationId - 会话ID
   * @param {string} userId - 用户ID
   * @returns {Promise} 返回删除结果
   */
  deleteConversationOld: (conversationId, userId) => chatApi.deleteConversation(conversationId, userId),

  /**
   * 获取消息列表
   * @param {Object} params - 查询参数
   * @returns {Promise}
   */
  getMessages: (params) => get(
    buildUrl(CHAT_API.GET_MESSAGES, { conversationId: params?.conversationId || params?.sessionId || '' }),
    {
      userId: params?.userId || getCurrentUserId(),
      page: params?.page || params?.pageNum || 1,
      size: params?.size || params?.pageSize || 20
    }
  ),

  /**
   * 获取消息列表（别名）
   * @param {Object} params - 查询参数
   * @returns {Promise}
   */
  getMessagesOld: (params) => chatApi.getMessages(params),

  /**
   * 发送消息
   * POST /v1/chat/messages
   * @param {Object} data - 消息数据
   * @returns {Promise}
   */
  sendMessage: (data) => post(CHAT_API.SEND_MESSAGE, data),

  /**
   * 发送消息（别名）
   * @param {Object} data - 消息数据
   * @returns {Promise}
   */
  sendMessageOld: (data) => chatApi.sendMessage(data),

  /**
   * 删除消息
   * 当前后端提供的是撤回接口，这里兼容为撤回
   * @param {string} messageId - 消息ID
   * @returns {Promise}
   */
  deleteMessage: (messageId) => post(buildUrl(CHAT_API.RECALL_MESSAGE, { messageId })),

  /**
   * 标记消息已读
   * PUT /v1/chat/messages/{messageId}/read
   * @param {string} messageId - 消息ID
   * @returns {Promise}
   */
  markMessageRead: (messageId) => put(buildUrl(CHAT_API.MARK_READ, { messageId })),

  /**
   * 标记消息已读（别名）
   * @param {string} messageId - 消息ID
   * @returns {Promise}
   */
  markReadOld: (messageId) => chatApi.markMessageRead(messageId),

  /**
   * 发送图片消息
   * @param {Object} data - 消息数据
   * @returns {Promise}
   */
  sendImage: (data) => chatApi.sendMessage(data),

  /**
   * 发送菜品卡片
   * @param {Object} data - 数据
   * @returns {Promise}
   */
  sendDishCard: (data) => chatApi.sendMessage(data),

  /**
   * 发送订单卡片
   * @param {Object} data - 数据
   * @returns {Promise}
   */
  sendOrderCard: (data) => chatApi.sendMessage(data),

  /**
   * 撤回消息
   * @param {string} messageId - 消息ID
   * @returns {Promise}
   */
  recallMessage: (messageId) => post(buildUrl(CHAT_API.RECALL_MESSAGE, { messageId })),

  /**
   * 创建群聊
   * 当前小程序未接群聊创建接口，这里保留占位
   * @returns {Promise}
   */
  createGroup: () => Promise.reject(new Error('小程序暂未接入群聊创建接口')),

  /**
   * 获取未读消息数
   * 当前后端未提供单独计数接口，改为汇总会话未读数
   * @param {string} userId - 用户ID
   * @returns {Promise}
   */
  getUnreadCount: async (userId = getCurrentUserId()) => {
    const response = await chatApi.getConversations(userId)
    const count = sumUnreadCount(response.data)
    return {
      ...response,
      count,
      total: count,
      data: count
    }
  },

  /**
   * 获取快捷回复
   * 当前后端未提供快捷回复接口
   * @returns {Promise<Array>}
   */
  getQuickReplies: async () => []
}

export default chatApi
