/**
 * 订单相关API
 * 对接后端 OrderController
 * 基础路径: /v1/orders
 */
import { get, post, put, del } from '@/utils/request'
import { ORDER_API, buildUrl } from '../urlEnum'

const buildQueryUrl = (url, params = {}) => {
  const query = Object.entries(params)
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join('&')
  return query ? `${url}?${query}` : url
}

export const orderApi = {
  /**
   * 创建订单
   * POST /v1/orders
   * @param {Object} data - 订单数据
   * @param {Object} data.order - 订单基本信息
   * @param {string} data.order.userId - 用户ID
   * @param {string} data.order.merchantId - 商家ID
   * @param {number} data.order.totalAmount - 总金额
   * @param {string} data.order.deliveryAddress - 配送地址
   * @param {string} data.order.remark - 订单备注
   * @param {Array} data.dishes - 菜品列表
   * @param {string} data.dishes[].dishId - 菜品ID
   * @param {number} data.dishes[].quantity - 数量
   * @param {number} data.dishes[].price - 单价
   * @returns {Promise} 返回创建结果
   */
  create: (data) => post(ORDER_API.CREATE_ORDER, data),

  /**
   * 获取用户订单列表
   * GET /v1/orders/user/{userId}
   * @param {string} userId - 用户ID
   * @param {Object} params - 查询参数
   * @param {string} params.status - 订单状态（可选）
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} 返回订单列表
   */
  getByUser: (userId, params) => get(buildUrl(ORDER_API.GET_USER_ORDERS, { userId }), params),

  /**
   * 获取商家订单列表
   * GET /v1/orders/merchant/{merchantId}
   * @param {string} merchantId - 商家ID
   * @param {Object} params - 查询参数
   * @param {string} params.status - 订单状态（可选）
   * @param {boolean} params.today - 是否只查询今日订单
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} 返回订单列表
   */
  getByMerchant: (merchantId, params) => get(buildUrl(ORDER_API.GET_MERCHANT_ORDERS, { merchantId }), params),

  /**
   * 获取订单详情
   * GET /v1/orders/{orderId}
   * @param {string} orderId - 订单ID
   * @returns {Promise} 返回订单详情
   */
  getDetail: (orderId) => get(buildUrl(ORDER_API.GET_ORDER_DETAIL, { orderId })),

  /**
   * 获取订单的菜品列表
   * GET /v1/orders/{orderId}/dishes
   * @param {string} orderId - 订单ID
   * @returns {Promise} 返回菜品列表
   */
  getDishes: (orderId) => get(buildUrl(ORDER_API.GET_ORDER_DISHES, { orderId })),

  /**
   * 更新订单状态
   * PUT /v1/orders/{orderId}/status
   * @param {string} orderId - 订单ID
   * @param {Object|number|string} data - 状态数据或状态值
   * @param {string|number} data.status - 新状态(0-待支付,1-待接单,2-制作中,3-已完成,4-已取消)
   * @param {string} data.remark - 备注信息
   * @returns {Promise} 返回更新结果
   */
  updateStatus: (orderId, data) => {
    const payload = typeof data === 'object' ? data : { status: data }
    return put(buildUrl(ORDER_API.UPDATE_ORDER_STATUS, { orderId }), payload)
  },

  /**
   * 取消订单
   * PUT /v1/orders/{orderId}/cancel
   * @param {string} orderId - 订单ID
   * @param {Object} data - 取消原因
   * @param {string} data.reason - 取消原因
   * @returns {Promise} 返回取消结果
   */
  cancel: (orderId, data = {}) => put(
    buildQueryUrl(buildUrl(ORDER_API.CANCEL_ORDER, { orderId }), { reason: data.reason })
  ),

  /**
   * 确认收货
   * POST /v1/orders/{orderId}/confirm
   * @param {string} orderId - 订单ID
   * @returns {Promise} 返回确认结果
   */
  confirm: (orderId) => post(buildUrl('/v1/orders/:orderId/confirm', { orderId })),

  /**
   * 申请退款
   * POST /v1/orders/{orderId}/refund
   * @param {string} orderId - 订单ID
   * @param {Object} data - 退款数据
   * @param {string} data.reason - 退款原因
   * @param {number} data.refAmount - 退款金额
   * @returns {Promise} 返回退款结果
   */
  refund: (orderId, data) => post(buildUrl('/v1/orders/:orderId/refund', { orderId }), data),

  /**
   * 订单统计
   * GET /v1/orders/stats/{merchantId}
   * @param {string} merchantId - 商家ID
   * @returns {Promise} 返回统计数据
   */
  getStats: (merchantId) => get(buildUrl('/v1/orders/stats/:merchantId', { merchantId })),

  /**
   * 再来一单
   * POST /v1/orders/{orderId}/reorder
   * @param {string} orderId - 订单ID
   * @returns {Promise} 返回新订单
   */
  reorder: (orderId) => post(buildUrl('/v1/orders/:orderId/reorder', { orderId })),

  /**
   * 获取订单数量统计
   * GET /v1/orders/count
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID（可选）
   * @param {string} params.merchantId - 商家ID（可选）
   * @param {string} params.status - 订单状态（可选）
   * @returns {Promise} 返回统计数据
   */
  getCount: (params) => get('/v1/orders/count', params)
}

export default orderApi
