/**
 * 会话管理 API
 * 对接后端 ChatSessionController
 */
import { get, post, del } from '@/utils/request'
import { CHAT_API, buildUrl } from '../urlEnum'

const buildQueryUrl = (url, params = {}) => {
  const query = Object.entries(params)
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join('&')

  return query ? `${url}?${query}` : url
}

const normalizeConversation = (conversation = {}) => ({
  ...conversation,
  id: conversation.conversationId || conversation.sessionId || conversation.id,
  sessionId: conversation.sessionId || conversation.id,
  conversationId: conversation.conversationId || conversation.sessionId || conversation.id,
  type: conversation.type || conversation.sessionType || 'single',
  isPinned: conversation.isPinned ?? conversation.pinned ?? false,
  unreadCount: Number(conversation.unreadCount || 0),
  targetId: conversation.targetId || conversation.targetUserId || '',
  lastMessage: typeof conversation.lastMessage === 'string'
    ? {
        content: conversation.lastMessage,
        type: conversation.lastMessageType || 'text'
      }
    : (conversation.lastMessage || { content: '', type: 'text' })
})

const normalizeListResponse = (response) => {
  const list = Array.isArray(response?.data) ? response.data.map(normalizeConversation) : []
  return {
    ...response,
    data: list,
    list
  }
}

export const conversationApi = {
  /**
   * 获取会话列表
   * GET /v1/chat/users/{userId}/chat-sessions
   * @param {string} userId - 用户ID
   * @returns {Promise} 返回会话列表
   */
  getList: (userId) => get(
    buildUrl(CHAT_API.GET_CONVERSATIONS, { userId })
  ).then(normalizeListResponse),

  /**
   * 搜索会话
   * 当前后端未提供独立搜索接口，改为本地过滤
   * @param {string} userId - 用户ID
   * @param {string} keyword - 搜索关键词
   * @returns {Promise} 返回搜索结果
   */
  search: async (userId, keyword) => {
    const response = await conversationApi.getList(userId)
    const lowerKeyword = (keyword || '').trim().toLowerCase()

    if (!lowerKeyword) {
      return response
    }

    return {
      ...response,
      data: response.data.filter(item =>
        item.name?.toLowerCase().includes(lowerKeyword) ||
        item.lastMessage?.content?.toLowerCase().includes(lowerKeyword)
      )
    }
  },

  /**
   * 保存置顶状态
   * POST /v1/chat/sessions/{conversationId}/toggle-pin
   * @param {string} conversationId - 会话ID
   * @returns {Promise} 返回更新结果
   */
  setPin: (conversationId) => post(
    buildUrl('/v1/chat/sessions/:conversationId/toggle-pin', { conversationId }),
    { userId: uni.getStorageSync('userInfo')?.userId || uni.getStorageSync('userId') || '' }
  ),

  /**
   * 标记已读
   * POST /v1/chat/sessions/{conversationId}/unread-clear
   * @param {string} conversationId - 会话ID
   * @returns {Promise} 返回标记结果
   */
  markRead: (conversationId) => post(
    buildUrl('/v1/chat/sessions/:conversationId/unread-clear', { conversationId }),
    { userId: uni.getStorageSync('userInfo')?.userId || uni.getStorageSync('userId') || '' }
  ),

  /**
   * 删除会话
   * DELETE /v1/chat/sessions/{conversationId}?userId=xxx
   * @param {string} conversationId - 会话ID
   * @returns {Promise} 返回删除结果
   */
  delete: (conversationId) => del(buildQueryUrl(
    buildUrl(CHAT_API.DELETE_CONVERSATION, { conversationId }),
    { userId: uni.getStorageSync('userInfo')?.userId || uni.getStorageSync('userId') || '' }
  ))
}

export default conversationApi
