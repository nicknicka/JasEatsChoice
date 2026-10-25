/**
 * 想吃列表相关API
 */
import api from '../utils/api'

export default {
  /**
   * 创建想吃列表项（用户）
   */
  createItem(data) {
    return api.post('/v1/wish-list/item', data)
  },

  /**
   * 获取当前用户的想吃列表
   */
  getUserItems() {
    return api.get('/v1/wish-list/items')
  },

  /**
   * 获取想吃列表项详情
   */
  getItemDetail(itemId) {
    return api.get(`/v1/wish-list/item/${itemId}`)
  },

  /**
   * 撤回想吃列表项（用户）
   */
  withdrawItem(itemId) {
    return api.delete(`/v1/wish-list/item/${itemId}`)
  },

  /**
   * 获取待审核列表（商家）
   */
  getPendingItems() {
    return api.get('/v1/wish-list/merchant/pending')
  },

  /**
   * 审核想吃列表项
   */
  auditItem(itemId, data) {
    return api.post('/v1/wish-list/audit', {
      wishListItemId: itemId,
      ...data
    })
  },

  /**
   * 获取审核历史
   */
  getAuditHistory() {
    return Promise.resolve({
      code: 200,
      success: true,
      message: '后端暂未提供审核历史接口',
      data: []
    })
  },

  /**
   * 用户申诉
   */
  appealRejection(itemId, data) {
    return api.post('/v1/wish-list/appeal', {
      wishListItemId: itemId,
      ...data
    })
  },

  /**
   * 商家回复申诉
   */
  replyAppeal() {
    return Promise.reject(new Error('后端暂未提供申诉回复接口'))
  }
}
