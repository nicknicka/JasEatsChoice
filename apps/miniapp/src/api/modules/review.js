/**
 * 评价相关API
 * 对接后端 ReviewController
 * 基础路径: /v1/reviews
 */
import { get, post, del } from '@/utils/request'
import { REVIEW_API, buildUrl } from '../urlEnum'

export const reviewApi = {
  /**
   * 发表评价
   * POST /v1/reviews
   * @param {Object} data - 评价数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.targetType - 目标类型(dish/merchant/order)
   * @param {string} data.targetId - 目标ID
   * @param {number} data.rating - 评分(1-5)
   * @param {string} data.content - 评价内容
   * @param {Array} data.images - 图片列表
   * @param {Array} data.tags - 标签列表
   * @returns {Promise} 返回创建结果
   */
  create: (data) => post(REVIEW_API.CREATE_REVIEW, data),

  /**
   * 获取评价列表
   * GET /v1/reviews
   * @param {Object} params - 查询参数
   * @param {string} params.targetType - 目标类型(dish/merchant/order)
   * @param {string} params.targetId - 目标ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} params.sort - 排序方式(time/rating)
   * @returns {Promise} 返回评价列表
   */
  getReviews: (params) => get(REVIEW_API.GET_REVIEWS, params),

  /**
   * 获取菜品评价列表
   * GET /v1/reviews/dish/{dishId}
   * @param {string} dishId - 菜品ID
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} params.sort - 排序方式(time/rating)
   * @returns {Promise} 返回评价列表
   */
  getDishReviews: (dishId, params) => get(buildUrl(REVIEW_API.GET_DISH_REVIEWS, { dishId }), params),

  /**
   * 获取商家评价列表
   * GET /v1/reviews/merchant/{merchantId}
   * @param {string} merchantId - 商家ID
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} params.sort - 排序方式(time/rating)
   * @returns {Promise} 返回评价列表
   */
  getMerchantReviews: (merchantId, params) => get(buildUrl(REVIEW_API.GET_MERCHANT_REVIEWS, { merchantId }), params),

  /**
   * 获取用户评价列表
   * GET /v1/reviews/user/{userId}
   * @param {string} userId - 用户ID
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} 返回评价列表
   */
  getUserReviews: (userId, params) => get(buildUrl(REVIEW_API.GET_USER_REVIEWS, { userId }), params),

  /**
   * 获取订单评价
   * GET /v1/reviews/order/{orderId}
   * @param {string} orderId - 订单ID
   * @returns {Promise} 返回评价信息
   */
  getOrderReview: (orderId) => get(buildUrl('/v1/reviews/order/:orderId', { orderId })),

  /**
   * 商家回复评价
   * POST /v1/reviews/{reviewId}/reply
   * @param {string} reviewId - 评价ID
   * @param {Object} data - 回复数据
   * @param {string} data.merchantId - 商家ID
   * @param {string} data.content - 回复内容
   * @returns {Promise} 返回回复结果
   */
  reply: (reviewId, data) => post(buildUrl('/v1/reviews/:reviewId/reply', { reviewId }), data),

  /**
   * 删除评价
   * DELETE /v1/reviews/{reviewId}
   * @param {string} reviewId - 评价ID
   * @param {string} userId - 用户ID
   * @returns {Promise} 返回删除结果
   */
  delete: (reviewId, userId) => del(buildUrl(REVIEW_API.DELETE_REVIEW, { reviewId }), { userId }),

  /**
   * 获取商家评价统计
   * GET /v1/reviews/merchant/{merchantId}/statistics
   * @param {string} merchantId - 商家ID
   * @returns {Promise} 返回统计数据
   */
  getStatistics: (merchantId) => get(buildUrl('/v1/reviews/merchant/:merchantId/statistics', { merchantId }))
}

export default reviewApi
