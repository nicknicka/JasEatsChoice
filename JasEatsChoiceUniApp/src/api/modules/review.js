/**
 * 评价相关API
 * 对接后端 ReviewController
 * 基础路径: /v1/reviews
 */
import { get, post, put, del } from '@/utils/request'

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
   */
  create: (data) => post('/v1/reviews', data),

  /**
   * 获取菜品评价列表
   * GET /v1/reviews/dishes/{dishId}
   * @param {string} dishId - 菜品ID
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} params.sort - 排序方式(time/rating)
   */
  getDishReviews: (dishId, params) => get(`/v1/reviews/dishes/${dishId}`, params),

  /**
   * 获取商家评价列表
   * GET /v1/reviews/merchants/{merchantId}
   * @param {string} merchantId - 商家ID
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} params.sort - 排序方式(time/rating)
   */
  getMerchantReviews: (merchantId, params) => get(`/v1/reviews/merchants/${merchantId}`, params),

  /**
   * 获取用户评价列表
   * GET /v1/reviews/users/{userId}
   * @param {string} userId - 用户ID
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   */
  getUserReviews: (userId, params) => get(`/v1/reviews/users/${userId}`, params),

  /**
   * 获取订单评价
   * GET /v1/reviews/orders/{orderId}
   * @param {string} orderId - 订单ID
   */
  getOrderReview: (orderId) => get(`/v1/reviews/orders/${orderId}`),

  /**
   * 获取评价详情
   * GET /v1/reviews/{reviewId}
   * @param {string} reviewId - 评价ID
   */
  getDetail: (reviewId) => get(`/v1/reviews/${reviewId}`),

  /**
   * 商家回复评价
   * POST /v1/reviews/{reviewId}/reply
   * @param {string} reviewId - 评价ID
   * @param {Object} data - 回复数据
   * @param {string} data.merchantId - 商家ID
   * @param {string} data.content - 回复内容
   */
  reply: (reviewId, data) => post(`/v1/reviews/${reviewId}/reply`, data),

  /**
   * 删除评价
   * DELETE /v1/reviews/{reviewId}
   * @param {string} reviewId - 评价ID
   * @param {string} userId - 用户ID
   */
  delete: (reviewId, userId) => del(`/v1/reviews/${reviewId}`, { userId }),

  /**
   * 上传评价图片
   * POST /v1/reviews/images
   * @param {FormData} formData - 图片文件
   */
  uploadImages: (formData) => post('/v1/reviews/images', formData),

  /**
   * 点赞评价
   * POST /v1/reviews/{reviewId}/like
   * @param {string} reviewId - 评价ID
   * @param {Object} data - 点赞数据
   * @param {string} data.userId - 用户ID
   */
  like: (reviewId, data) => post(`/v1/reviews/${reviewId}/like`, data),

  /**
   * 取消点赞评价
   * POST /v1/reviews/{reviewId}/unlike
   * @param {string} reviewId - 评价ID
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   */
  unlike: (reviewId, data) => post(`/v1/reviews/${reviewId}/unlike`, data),

  /**
   * 获取评价统计
   * GET /v1/reviews/statistics
   * @param {Object} params - 查询参数
   * @param {string} params.targetType - 目标类型(dish/merchant)
   * @param {string} params.targetId - 目标ID
   */
  getStatistics: (params) => get('/v1/reviews/statistics', params),

  /**
   * 获取评价标签
   * GET /v1/reviews/tags
   * @param {Object} params - 查询参数
   * @param {string} params.targetType - 目标类型(dish/merchant)
   * @param {string} params.targetId - 目标ID
   */
  getTags: (params) => get('/v1/reviews/tags', params)
}

export default reviewApi
