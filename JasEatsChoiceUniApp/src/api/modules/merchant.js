/**
 * 商家相关API
 * 对接后端 MerchantController
 * 基础路径: /v1/merchants
 */
import { get, post, put, del } from '@/utils/request'

export const merchantApi = {
  /**
   * 获取商家列表
   * GET /v1/merchants
   * @param {Object} params - 查询参数
   * @param {string} params.keyword - 搜索关键词
   * @param {string} params.category - 分类
   * @param {string} params.sort - 排序方式(distance/rating/sales)
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   */
  getList: (params) => get('/v1/merchants', params),

  /**
   * 获取商家详情
   * GET /v1/merchants/{merchantId}
   * @param {string} merchantId - 商家ID
   */
  getDetail: (merchantId) => get(`/v1/merchants/${merchantId}`),

  /**
   * 获取附近商家
   * GET /v1/merchants/nearby
   * @param {Object} params - 查询参数
   * @param {number} params.latitude - 纬度
   * @param {number} params.longitude - 经度
   * @param {number} params.radius - 半径(米)
   * @param {number} params.limit - 数量限制
   */
  getNearby: (params) => get('/v1/merchants/nearby', params),

  /**
   * 商家登录
   * POST /v1/merchant/login
   * @param {Object} data - 登录数据
   * @param {string} data.username - 用户名/手机号
   * @param {string} data.password - 密码
   */
  login: (data) => post('/v1/merchant/login', data),

  /**
   * 获取商家信息（当前登录商家）
   * GET /v1/merchant/info
   */
  getInfo: () => get('/v1/merchant/info'),

  /**
   * 更新商家信息
   * PUT /v1/merchants/{merchantId}
   * @param {string} merchantId - 商家ID
   * @param {Object} data - 商家信息
   */
  updateInfo: (merchantId, data) => put(`/v1/merchants/${merchantId}`, data),

  /**
   * 获取商家优惠券列表
   * GET /v1/merchants/{merchantId}/coupons
   * @param {string} merchantId - 商家ID
   */
  getCoupons: (merchantId) => get(`/v1/merchants/${merchantId}/coupons`),

  /**
   * 获取商家评价列表
   * GET /v1/merchants/{merchantId}/reviews
   * @param {string} merchantId - 商家ID
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   */
  getReviews: (merchantId, params) => get(`/v1/merchants/${merchantId}/reviews`, params),

  /**
   * 获取商家统计数据
   * GET /v1/merchants/{merchantId}/statistics
   * @param {string} merchantId - 商家ID
   * @param {Object} params - 查询参数
   * @param {string} params.timeRange - 时间范围(today/week/month)
   */
  getStatistics: (merchantId, params) => get(`/v1/merchants/${merchantId}/statistics`, params),

  /**
   * 获取商家财务数据
   * GET /v1/merchants/{merchantId}/finance
   * @param {string} merchantId - 商家ID
   */
  getFinance: (merchantId) => get(`/v1/merchants/${merchantId}/finance`),

  /**
   * 商家提现申请
   * POST /v1/merchants/{merchantId}/withdraw
   * @param {string} merchantId - 商家ID
   * @param {Object} data - 提现数据
   * @param {number} data.amount - 提现金额
   * @param {string} data.account - 提现账户
   */
  withdraw: (merchantId, data) => post(`/v1/merchants/${merchantId}/withdraw`, data),

  /**
   * 收藏商家
   * POST /v1/users/{userId}/favorites/merchants
   * @param {string} userId - 用户ID
   * @param {string} merchantId - 商家ID
   */
  favorite: (userId, merchantId) => post(`/v1/users/${userId}/favorites/merchants`, { merchantId }),

  /**
   * 取消收藏商家
   * DELETE /v1/users/{userId}/favorites/merchants/{merchantId}
   * @param {string} userId - 用户ID
   * @param {string} merchantId - 商家ID
   */
  unfavorite: (userId, merchantId) => del(`/v1/users/${userId}/favorites/merchants/${merchantId}`),

  /**
   * 检查是否收藏商家
   * GET /v1/users/{userId}/favorites/merchants/{merchantId}/check
   * @param {string} userId - 用户ID
   * @param {string} merchantId - 商家ID
   */
  checkFavorite: (userId, merchantId) => get(`/v1/users/${userId}/favorites/merchants/${merchantId}/check`)
}

export default merchantApi
