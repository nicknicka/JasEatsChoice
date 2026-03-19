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
  checkFavorite: (userId, merchantId) => get(`/v1/users/${userId}/favorites/merchants/${merchantId}/check`),

  // ============= 订单管理相关API =============

  /**
   * 获取商家订单列表
   * GET /v1/orders/merchant/{merchantId}
   * @param {string} merchantId - 商家ID
   * @param {Object} params - 查询参数
   * @param {boolean} params.today - 是否只查询今日订单，默认true
   */
  getOrders: (merchantId, params) => get(`/v1/orders/merchant/${merchantId}`, params),

  /**
   * 获取今日订单
   * GET /v1/orders/merchant/{merchantId}?today=true
   * @param {string} merchantId - 商家ID
   */
  getTodayOrders: (merchantId) => get(`/v1/orders/merchant/${merchantId}`, { today: true }),

  /**
   * 获取订单详情
   * GET /v1/orders/{orderId}
   * @param {string} orderId - 订单ID
   */
  getOrderDetail: (orderId) => get(`/v1/orders/${orderId}`),

  /**
   * 获取订单菜品列表
   * GET /v1/orders/{orderId}/dishes
   * @param {string} orderId - 订单ID
   */
  getOrderDishes: (orderId) => get(`/v1/orders/${orderId}/dishes`),

  /**
   * 更新订单状态
   * PUT /v1/orders/{orderId}/status?status={status}
   * @param {string} orderId - 订单ID
   * @param {number} status - 订单状态(0-待支付,1-待接单,2-制作中,3-已完成,4-已取消)
   */
  updateOrderStatus: (orderId, status) => put(`/v1/orders/${orderId}/status`, null, { params: { status } }),

  /**
   * 接单 - 将状态更新为2(制作中)
   * PUT /v1/orders/{orderId}/status?status=2
   * @param {string} orderId - 订单ID
   */
  acceptOrder: (orderId) => put(`/v1/orders/${orderId}/status`, null, { params: { status: 2 } }),

  /**
   * 拒单 - 将状态更新为4(已取消)
   * PUT /v1/orders/{orderId}/cancel?reason={reason}
   * @param {string} orderId - 订单ID
   * @param {string} reason - 拒单原因
   */
  rejectOrder: (orderId, reason) => put(`/v1/orders/${orderId}/cancel`, null, { params: { reason } }),

  /**
   * 完成订单 - 将状态更新为3(已完成)
   * PUT /v1/orders/{orderId}/status?status=3
   * @param {string} orderId - 订单ID
   */
  completeOrder: (orderId) => put(`/v1/orders/${orderId}/status`, null, { params: { status: 3 } })
}

export default merchantApi
