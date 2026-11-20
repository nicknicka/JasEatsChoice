/**
 * 评价相关API
 * 对接后端 ReviewController
 * 基础路径: /v1/reviews
 */
import { get, post, del } from '@/utils/request'
import { REVIEW_API, buildUrl } from '../urlEnum'

/**
 * 解析评价图片。
 * 后端 t_review.images 存的是 JSON 字符串（ReviewController 用 objectMapper 写入，
 * 无图时是 "[]"，历史数据可能是 NULL），不是数组；直接当数组遍历会逐个字符渲染。
 * 商家评价列表页与评价详情页共用这一处口径。
 * @param {string|Array} images - 接口返回的 images 字段
 * @returns {Array<string>} 图片地址数组
 */
export const parseReviewImages = (images) => {
  if (Array.isArray(images)) return images
  if (typeof images !== 'string' || !images.trim()) return []

  try {
    const parsed = JSON.parse(images)
    return Array.isArray(parsed) ? parsed : []
  } catch (error) {
    console.error('解析评价图片失败:', images, error)
    return []
  }
}

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
   * 获取单条评价详情
   * GET /v1/reviews/{reviewId}
   * 路径写成字面量：urlEnum.js 里没有对应常量，且该文件本次不允许改动。
   * 返回字段与商家评价列表里的单个条目一致（userName/orderNo/dishes/replies 等）。
   * @param {string} reviewId - 评价ID
   * @returns {Promise} 返回评价详情；评价不存在时后端返回 code 404
   */
  getDetail: (reviewId) => get(buildUrl('/v1/reviews/:reviewId', { reviewId })),

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
   * 给已有评价追加评论（追评）
   * POST /v1/reviews/{reviewId}/additional
   * @param {string} reviewId - 评价ID
   * @param {Object} data - 追评数据
   * @param {string} data.content - 追评内容
   * @param {string[]} data.images - 追评图片
   * @returns {Promise} 返回追评结果
   */
  addComment: (reviewId, data) => post(buildUrl('/v1/reviews/:reviewId/additional', { reviewId }), data),

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
