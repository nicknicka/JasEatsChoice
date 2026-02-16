/**
 * 评价API
 * 与后端评价系统接口对接
 */

import axios from 'axios'
import { API_CONFIG } from '../config/index.js'

const BASE_URL = API_CONFIG.baseURL

/**
 * 评价API
 */
export const reviewAPI = {
  /**
   * 用户提交评价
   * @param {Object} reviewData - 评价数据
   * @param {string} reviewData.orderId - 订单ID
   * @param {string} reviewData.merchantId - 商家ID
   * @param {number} reviewData.rating - 评分（1-5星，支持0.5分）
   * @param {string[]} reviewData.tags - 标签列表
   * @param {string} reviewData.content - 评价内容
   * @param {string[]} reviewData.images - 图片列表（可选）
   */
  submitReview(reviewData) {
    // 后端暂无此接口，待实现
    // 预期接口: POST /v1/reviews
    return axios.post(`${BASE_URL}/v1/reviews`, reviewData)
  },

  /**
   * 用户追加评价
   * @param {string} reviewId - 原评价ID
   * @param {Object} additionalReviewData - 追评数据
   * @param {string} additionalReviewData.content - 追评内容
   * @param {string[]} additionalReviewData.images - 追评图片（可选）
   */
  addAdditionalReview(reviewId, additionalReviewData) {
    // 后端暂无此接口，待实现
    // 预期接口: POST /v1/reviews/{reviewId}/additional
    return axios.post(`${BASE_URL}/v1/reviews/${reviewId}/additional`, additionalReviewData)
  },

  /**
   * 获取用户的评价列表
   * @param {string} userId - 用户ID
   * @param {Object} options - 查询选项
   * @param {number} options.page - 页码
   * @param {number} options.pageSize - 每页数量
   * @param {string} options.status - 评价状态筛选
   */
  getUserReviews(userId, options = {}) {
    const params = {
      page: options.page || 1,
      pageSize: options.pageSize || 20,
      ...(options.status && { status: options.status })
    }
    return axios.get(`${BASE_URL}/v1/reviews/user/${userId}`, { params })
  },

  /**
   * 获取订单的评价详情
   * @param {string} orderId - 订单ID
   */
  getReviewByOrderId(orderId) {
    return axios.get(`${BASE_URL}/v1/reviews/order/${orderId}`)
  },

  /**
   * 更新订单状态（确认收货/完成订单）
   * @param {string} orderId - 订单ID
   * @param {number} status - 新状态（7-待评价）
   */
  updateOrderStatus(orderId, status) {
    return axios.put(`${BASE_URL}/v1/orders/${orderId}/status`, null, {
      params: { status }
    })
  },

  /**
   * 确认收货并更新订单状态为待评价
   * @param {string} orderId - 订单ID
   */
  confirmReceipt(orderId) {
    return this.updateOrderStatus(orderId, 7) // 7-待评价状态
  },

  /**
   * 获取商家的评价列表
   * @param {string} merchantId - 商家ID
   * @param {Object} options - 筛选选项
   * @param {string} options.status - 状态筛选（replied/unreplied/all）
   * @param {number} options.rating - 评分筛选
   * @param {string} options.keyword - 关键词搜索
   */
  getMerchantReviews(merchantId, options = {}) {
    const params = {}
    if (options.status) params.status = options.status
    if (options.rating) params.rating = options.rating
    if (options.keyword) params.keyword = options.keyword

    return axios.get(`${BASE_URL}/v1/reviews/merchant/${merchantId}`, { params })
  },

  /**
   * 商家回复评价
   * @param {string} reviewId - 评价ID
   * @param {Object} replyData - 回复数据
   * @param {string} replyData.content - 回复内容
   * @param {string} replyData.merchantId - 商家ID
   */
  replyReview(reviewId, replyData) {
    return axios.post(`${BASE_URL}/v1/reviews/${reviewId}/reply`, replyData)
  },

  /**
   * 获取商家评价统计
   * @param {string} merchantId - 商家ID
   */
  getMerchantStatistics(merchantId) {
    return axios.get(`${BASE_URL}/v1/reviews/merchant/${merchantId}/statistics`)
  }
}

export default reviewAPI
