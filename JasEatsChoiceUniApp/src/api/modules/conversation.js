import { get, post, put, del } from '@/utils/request'

/**
 * 会话管理 API
 */
export const conversationApi = {
  /**
   * IM-029: 获取会话列表
   */
  getList: (userId) => get(`/v1/conversations/user/${userId}`),

  /**
   * IM-033: 搜索会话
   */
  search: (userId, keyword) => get(`/v1/conversations/user/${userId}/search`, { keyword }),

  /**
   * IM-034: 保存置顶状态
   */
  setPin: (conversationId, isPinned) => put(`/v1/conversations/${conversationId}/pin`, { isPinned }),

  /**
   * IM-035: 标记已读
   */
  markRead: (conversationId) => put(`/v1/conversations/${conversationId}/read`),

  /**
   * IM-036: 删除会话
   */
  delete: (conversationId) => del(`/v1/conversations/${conversationId}`)
}
