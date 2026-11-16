/**
 * 会话管理 API
 * 对接后端 ChatSessionController
 */
import { get, post, del } from '@/utils/request'
import { CHAT_API, buildUrl } from '../urlEnum'

const getCurrentUserId = () => {
  const userInfo = uni.getStorageSync('userInfo') || {}
  return userInfo.userId || userInfo.id || uni.getStorageSync('userId') || ''
}

/**
 * 解析会话操作要用的 userId。
 *
 * 后端三个写操作都按 (userId, sessionId) 定位 chat_session 记录，所以这里必须和使用
 * getList(userId) 拉列表时传的身份一致。商家端列表传的是商家 ID（#593），若这里继续
 * 用 getCurrentUserId() 取登录用户 ID，两个口径不一致，后端查不到会话直接 404。
 *
 * 页面显式传入的 ID 优先；旧调用点 pages-common/chat/conversation-list.vue 的第二参数
 * 是布尔（setPin(id, true/false)），不是 userId，一律忽略并回退到登录用户身份。
 */
const resolveUserId = (userId) => {
  if (typeof userId === 'string' && userId) return userId
  if (typeof userId === 'number' && Number.isFinite(userId)) return String(userId)
  return getCurrentUserId()
}

/**
 * 缺少身份时直接失败，不让请求带着空 userId 打过去（后端只会回 404，看不出真实原因）
 */
const withUserId = (userId, send) => {
  const id = resolveUserId(userId)
  if (!id) {
    return Promise.reject(new Error('未获取到用户身份，无法同步会话状态'))
  }
  return send(id)
}

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

const createEmptyListResponse = () => ({
  success: true,
  code: 200,
  message: '成功',
  data: [],
  list: []
})

export const conversationApi = {
  /**
   * 获取会话列表
   * GET /v1/chat/users/{userId}/chat-sessions
   * @param {string} userId - 用户ID
   * @returns {Promise} 返回会话列表
   */
  getList: (userId) => {
    if (!userId) {
      return Promise.resolve(createEmptyListResponse())
    }

    return get(
      buildUrl(CHAT_API.GET_CONVERSATIONS, { userId })
    ).then(normalizeListResponse)
  },

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
   * 切换置顶状态
   * POST /v1/chat/sessions/{conversationId}/toggle-pin
   * @param {string} conversationId - 会话ID
   * @param {string} [userId] - 会话所属身份；商家端必须传商家ID（与拉列表的口径一致）
   * @returns {Promise} data 为切换后的置顶状态布尔值
   */
  setPin: (conversationId, userId) => withUserId(userId, id => post(
    buildUrl('/v1/chat/sessions/:conversationId/toggle-pin', { conversationId }),
    { userId: id }
  )),

  /**
   * 标记已读
   * POST /v1/chat/sessions/{conversationId}/unread-clear
   * @param {string} conversationId - 会话ID
   * @param {string} [userId] - 会话所属身份；商家端必须传商家ID（与拉列表的口径一致）
   * @returns {Promise} 返回标记结果
   */
  markRead: (conversationId, userId) => withUserId(userId, id => post(
    buildUrl('/v1/chat/sessions/:conversationId/unread-clear', { conversationId }),
    { userId: id }
  )),

  /**
   * 删除会话
   * DELETE /v1/chat/sessions/{conversationId}?userId=xxx
   * @param {string} conversationId - 会话ID
   * @param {string} [userId] - 会话所属身份；商家端必须传商家ID（与拉列表的口径一致）
   * @returns {Promise} 返回删除结果
   */
  delete: (conversationId, userId) => withUserId(userId, id => del(buildQueryUrl(
    buildUrl(CHAT_API.DELETE_CONVERSATION, { conversationId }),
    { userId: id }
  )))
}

export default conversationApi
