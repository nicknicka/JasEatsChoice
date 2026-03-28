import { get, post, put, del } from '@/utils/request'

/**
 * 群聊管理 API
 */
export const groupApi = {
  /**
   * IM-011: 获取群消息列表
   */
  getMessages: (groupId, params) => get(`/v1/groups/${groupId}/messages`, params),

  /**
   * IM-012: 发送群消息
   */
  sendMessage: (data) => post('/v1/groups/messages', data),

  /**
   * IM-018: 获取群详情
   */
  getGroupDetail: (groupId) => get(`/v1/groups/${groupId}`),

  /**
   * IM-019: 获取成员列表
   */
  getMembers: (groupId) => get(`/v1/groups/${groupId}/members`),

  /**
   * IM-016: 退出群聊
   */
  leaveGroup: (groupId) => post(`/v1/groups/${groupId}/leave`),

  /**
   * IM-028: 解散群聊
   */
  dismissGroup: (groupId) => del(`/v1/groups/${groupId}`),

  /**
   * IM-020: 更新群公告
   */
  updateNotice: (groupId, notice) => put(`/v1/groups/${groupId}/notice`, { notice })
}
