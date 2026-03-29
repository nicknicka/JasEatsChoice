/**
 * 会话管理 API
 * 对接后端 ConversationController
 * 基础路径: /v1/conversations
 */
import { get, post, put, del } from '@/utils/request'
import { CHAT_API, buildUrl } from '../urlEnum'

export const conversationApi = {
  /**
   * IM-029: 获取会话列表
   * GET /v1/conversations
   * @param {string} userId - 用户ID
   * @returns {Promise} 返回会话列表
   */
  getList: (userId) => get(buildUrl('/v1/conversations/user/:userId', { userId })),

  /**
   * IM-033: 搜索会话
   * GET /v1/conversations/user/{userId}/search
   * @param {string} userId - 用户ID
   * @param {string} keyword - 搜索关键词
   * @returns {Promise} 返回搜索结果
   */
  search: (userId, keyword) => get(buildUrl('/v1/conversations/user/:userId/search', { userId }), { keyword }),

  /**
   * IM-034: 保存置顶状态
   * PUT /v1/conversations/{conversationId}/pin
   * @param {string} conversationId - 会话ID
   * @param {boolean} isPinned - 是否置顶
   * @returns {Promise} 返回更新结果
   */
  setPin: (conversationId, isPinned) => put(buildUrl('/v1/conversations/:conversationId/pin', { conversationId }), { isPinned }),

  /**
   * IM-035: 标记已读
   * PUT /v1/conversations/{conversationId}/read
   * @param {string} conversationId - 会话ID
   * @returns {Promise} 返回标记结果
   */
  markRead: (conversationId) => put(buildUrl('/v1/conversations/:conversationId/read', { conversationId })),

  /**
   * IM-036: 删除会话
   * DELETE /v1/conversations/{conversationId}
   * @param {string} conversationId - 会话ID
   * @returns {Promise} 返回删除结果
   */
  delete: (conversationId) => del(buildUrl(CHAT_API.DELETE_CONVERSATION, { conversationId }))
}

export default conversationApi
