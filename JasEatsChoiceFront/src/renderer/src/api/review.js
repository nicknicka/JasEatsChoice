/**
 * 评价相关API
 */
import api from '../utils/api'

export default {
  /**
   * 获取商家评价列表
   * @param {string} merchantId - 商家ID
   * @param {Object} params - 查询参数
   * @param {string} params.status - 状态筛选（all, unreplied, replied）
   * @param {number} params.rating - 评分筛选（1-5）
   * @param {string} params.keyword - 搜索关键词
   */
  getMerchantReviews(merchantId, params) {
    return api.get(`/v1/reviews/merchant/${merchantId}`, { params })
  },

  /**
   * 回复评价
   * @param {string} reviewId - 评价ID
   * @param {Object} data - 回复数据
   * @param {string} data.content - 回复内容
   * @param {string} data.merchantId - 商家ID
   */
  replyReview(reviewId, data) {
    return api.post(`/v1/reviews/${reviewId}/reply`, data)
  },

  /**
   * 获取评价统计
   * @param {string} merchantId - 商家ID
   */
  getReviewStatistics(merchantId) {
    return api.get(`/v1/reviews/merchant/${merchantId}/statistics`)
  }
}
