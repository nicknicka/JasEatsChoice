import { get, post, put, del } from '@/utils/request'

/**
 * 聊天相关API
 */
export const chatApi = {
  /**
   * 获取会话列表
   */
  getConversations: () => get('/api/chat/conversations'),

  /**
   * 获取会话详情
   * @param {string} conversationId - 会话ID
   */
  getConversation: (conversationId) => get(`/api/chat/conversation/${conversationId}`),

  /**
   * 获取消息列表
   * @param {string} conversationId - 会话ID
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   */
  getMessages: (conversationId, params) => get(`/api/chat/conversation/${conversationId}/messages`, params),

  /**
   * 发送消息
   * @param {Object} data - 消息数据
   * @param {string} data.conversationId - 会话ID
   * @param {string} data.type - 消息类型(text/image/dish/order)
   * @param {string} data.content - 消息内容
   */
  sendMessage: (data) => post('/api/chat/message', data),

  /**
   * 发送图片消息
   * @param {Object} data - 消息数据
   * @param {string} data.conversationId - 会话ID
   * @param {string} data.imageUrl - 图片URL
   */
  sendImage: (data) => post('/api/chat/message/image', data),

  /**
   * 发送菜品卡片
   * @param {Object} data - 数据
   * @param {string} data.conversationId - 会话ID
   * @param {number} data.dishId - 菜品ID
   */
  sendDishCard: (data) => post('/api/chat/message/dish', data),

  /**
   * 发送订单卡片
   * @param {Object} data - 数据
   * @param {string} data.conversationId - 会话ID
   * @param {number} data.orderId - 订单ID
   */
  sendOrderCard: (data) => post('/api/chat/message/order', data),

  /**
   * 标记消息已读
   * @param {string} conversationId - 会话ID
   */
  markRead: (conversationId) => put(`/api/chat/conversation/${conversationId}/read`),

  /**
   * 撤回消息
   * @param {string} messageId - 消息ID
   */
  recallMessage: (messageId) => del(`/api/chat/message/${messageId}`),

  /**
   * 创建会话
   * @param {Object} data - 会话数据
   * @param {number} data.targetUserId - 目标用户ID
   */
  createConversation: (data) => post('/api/chat/conversation', data),

  /**
   * 创建群聊
   * @param {Object} data - 群聊数据
   * @param {string} data.name - 群名称
   * @param {Array} data.memberIds - 成员ID列表
   */
  createGroup: (data) => post('/api/chat/group', data),

  /**
   * 获取未读消息数
   */
  getUnreadCount: () => get('/api/chat/unread'),

  /**
   * 删除会话
   * @param {string} conversationId - 会话ID
   */
  deleteConversation: (conversationId) => del(`/api/chat/conversation/${conversationId}`),

  /**
   * 获取快捷回复
   */
  getQuickReplies: () => get('/api/chat/quick-replies')
}
