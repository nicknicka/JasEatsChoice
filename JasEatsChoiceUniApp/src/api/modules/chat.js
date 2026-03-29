/**
 * 聊天相关API
 * 对接后端 ChatController
 * 基础路径: /api/chat
 */
import { get, post, put, del } from '@/utils/request'
import { CHAT_API, buildUrl } from '../urlEnum'

export const chatApi = {
  /**
   * 获取会话列表
   * GET /v1/conversations
   * @returns {Promise} 返回会话列表
   */
  getConversations: () => get(CHAT_API.GET_CONVERSATIONS),

  /**
   * 获取会话列表（别名）
   * @returns {Promise} 返回会话列表
   */
  getConversationsOld: () => get('/api/chat/conversations'),

  /**
   * 获取会话详情
   * GET /v1/conversations/{conversationId}
   * @param {string} conversationId - 会话ID
   * @returns {Promise} 返回会话详情
   */
  getConversation: (conversationId) => get(buildUrl(CHAT_API.GET_CONVERSATION, { conversationId })),

  /**
   * 获取会话详情（别名）
   * @param {string} conversationId - 会话ID
   * @returns {Promise} 返回会话详情
   */
  getConversationOld: (conversationId) => get(`/api/chat/conversation/${conversationId}`),

  /**
   * 创建会话
   * POST /v1/conversations
   * @param {Object} data - 会话数据
   * @param {string} data.targetUserId - 目标用户ID
   * @param {string} data.type - 会话类型(single/group)
   * @returns {Promise} 返回创建结果
   */
  createConversation: (data) => post(CHAT_API.CREATE_CONVERSATION, data),

  /**
   * 创建会话（别名）
   * @param {Object} data - 会话数据
   * @returns {Promise} 返回创建结果
   */
  createConversationOld: (data) => post('/api/chat/conversation', data),

  /**
   * 删除会话
   * DELETE /v1/conversations/{conversationId}
   * @param {string} conversationId - 会话ID
   * @returns {Promise} 返回删除结果
   */
  deleteConversation: (conversationId) => del(buildUrl(CHAT_API.DELETE_CONVERSATION, { conversationId })),

  /**
   * 删除会话（别名）
   * @param {string} conversationId - 会话ID
   * @returns {Promise} 返回删除结果
   */
  deleteConversationOld: (conversationId) => del(`/api/chat/conversation/${conversationId}`),

  /**
   * 获取消息列表
   * GET /v1/messages
   * @param {Object} params - 查询参数
   * @param {string} params.conversationId - 会话ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} 返回消息列表
   */
  getMessages: (params) => get(CHAT_API.GET_MESSAGES, params),

  /**
   * 获取消息列表（别名）
   * @param {string} conversationId - 会话ID
   * @param {Object} params - 查询参数
   * @returns {Promise} 返回消息列表
   */
  getMessagesOld: (conversationId, params) => get(`/api/chat/conversation/${conversationId}/messages`, params),

  /**
   * 发送消息
   * POST /v1/messages
   * @param {Object} data - 消息数据
   * @param {string} data.conversationId - 会话ID
   * @param {string} data.type - 消息类型(text/image/dish/order)
   * @param {string} data.content - 消息内容
   * @returns {Promise} 返回发送结果
   */
  sendMessage: (data) => post(CHAT_API.SEND_MESSAGE, data),

  /**
   * 发送消息（别名）
   * @param {Object} data - 消息数据
   * @returns {Promise} 返回发送结果
   */
  sendMessageOld: (data) => post('/api/chat/message', data),

  /**
   * 删除消息
   * DELETE /v1/messages/{messageId}
   * @param {string} messageId - 消息ID
   * @returns {Promise} 返回删除结果
   */
  deleteMessage: (messageId) => del(buildUrl(CHAT_API.DELETE_MESSAGE, { messageId })),

  /**
   * 标记消息已读
   * PUT /v1/messages/{messageId}/read
   * @param {string} messageId - 消息ID
   * @returns {Promise} 返回标记结果
   */
  markMessageRead: (messageId) => put(buildUrl(CHAT_API.MARK_READ, { messageId })),

  /**
   * 标记消息已读（别名）
   * @param {string} conversationId - 会话ID
   * @returns {Promise} 返回标记结果
   */
  markReadOld: (conversationId) => put(`/api/chat/conversation/${conversationId}/read`),

  /**
   * 发送图片消息
   * POST /api/chat/message/image
   * @param {Object} data - 消息数据
   * @param {string} data.conversationId - 会话ID
   * @param {string} data.imageUrl - 图片URL
   * @returns {Promise} 返回发送结果
   */
  sendImage: (data) => post('/api/chat/message/image', data),

  /**
   * 发送菜品卡片
   * POST /api/chat/message/dish
   * @param {Object} data - 数据
   * @param {string} data.conversationId - 会话ID
   * @param {number} data.dishId - 菜品ID
   * @returns {Promise} 返回发送结果
   */
  sendDishCard: (data) => post('/api/chat/message/dish', data),

  /**
   * 发送订单卡片
   * POST /api/chat/message/order
   * @param {Object} data - 数据
   * @param {string} data.conversationId - 会话ID
   * @param {number} data.orderId - 订单ID
   * @returns {Promise} 返回发送结果
   */
  sendOrderCard: (data) => post('/api/chat/message/order', data),

  /**
   * 撤回消息
   * DELETE /api/chat/message/{messageId}
   * @param {string} messageId - 消息ID
   * @returns {Promise} 返回撤回结果
   */
  recallMessage: (messageId) => del(`/api/chat/message/${messageId}`),

  /**
   * 创建群聊
   * POST /api/chat/group
   * @param {Object} data - 群聊数据
   * @param {string} data.name - 群名称
   * @param {Array} data.memberIds - 成员ID列表
   * @returns {Promise} 返回创建结果
   */
  createGroup: (data) => post('/api/chat/group', data),

  /**
   * 获取未读消息数
   * GET /api/chat/unread
   * @returns {Promise} 返回未读数
   */
  getUnreadCount: (userId) => get(`/v1/message/unread-count`, { userId }),

  /**
   * 获取快捷回复
   * GET /api/chat/quick-replies
   * @returns {Promise} 返回快捷回复列表
   */
  getQuickReplies: () => get('/api/chat/quick-replies')
}

export default chatApi
