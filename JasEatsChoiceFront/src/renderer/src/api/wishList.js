/**
 * 想吃列表相关API
 */
import api from '../utils/api'

export default {
  /**
   * 获取待审核列表（商家）
   */
  getPendingItems(merchantId) {
    return api.get(`/v1/wish-list/pending/${merchantId}`)
  },

  /**
   * 审核想吃列表项
   */
  auditItem(itemId, data) {
    return api.post(`/v1/wish-list/${itemId}/audit`, data)
  },

  /**
   * 获取审核历史
   */
  getAuditHistory(merchantId, params) {
    return api.get(`/v1/wish-list/audit-history/${merchantId}`, { params })
  },

  /**
   * 用户申诉
   */
  appealRejection(itemId, data) {
    return api.post(`/v1/wish-list/${itemId}/appeal`, data)
  },

  /**
   * 商家回复申诉
   */
  replyAppeal(itemId, data) {
    return api.post(`/v1/wish-list/${itemId}/appeal-reply`, data)
  }
}
