/**
 * 订单相关API
 * 对接后端 OrderController
 * 基础路径: /v1/orders
 */
import { get, post, put, del } from '@/utils/request'

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
   */
  create: (data) => post('/v1/orders', data),

  /**
   * 获取用户订单列表
   * GET /v1/orders/user/{userId}
   * @param {string} userId - 用户ID
   */
  getByUser: (userId) => get(`/v1/orders/user/${userId}`),

  /**
   * 获取商家订单列表
   * GET /v1/orders/merchant/{merchantId}
   * @param {string} merchantId - 商家ID
   */
  getByMerchant: (merchantId) => get(`/v1/orders/merchant/${merchantId}`),

  /**
   * 获取订单详情
   * GET /v1/orders/{orderId}
   * @param {string} orderId - 订单ID
   */
  getDetail: (orderId) => get(`/v1/orders/${orderId}`),

  /**
   * 获取订单的菜品列表
   * GET /v1/orders/{orderId}/dishes
   * @param {string} orderId - 订单ID
   */
  getDishes: (orderId) => get(`/v1/orders/${orderId}/dishes`),

  /**
   * 更新订单状态
   * PUT /v1/orders/{orderId}/status
   * @param {string} orderId - 订单ID
   * @param {Object} data - 状态数据
   * @param {string} data.status - 新状态
   * @param {string} data.remark - 备注信息
   */
  updateStatus: (orderId, data) => put(`/v1/orders/${orderId}/status`, data),

  /**
   * 取消订单
   * POST /v1/orders/{orderId}/cancel
   * @param {string} orderId - 订单ID
   * @param {Object} data - 取消原因
   * @param {string} data.reason - 取消原因
   */
  cancel: (orderId, data) => post(`/v1/orders/${orderId}/cancel`, data),

  /**
   * 确认收货
   * POST /v1/orders/{orderId}/confirm
   * @param {string} orderId - 订单ID
   */
  confirm: (orderId) => post(`/v1/orders/${orderId}/confirm`),

  /**
   * 申请退款
   * POST /v1/orders/{orderId}/refund
   * @param {string} orderId - 订单ID
   * @param {Object} data - 退款数据
   * @param {string} data.reason - 退款原因
   * @param {number} data.refAmount - 退款金额
   */
  refund: (orderId, data) => post(`/v1/orders/${orderId}/refund`, data),

  /**
   * 订单统计
   * GET /v1/orders/stats/{merchantId}
   * @param {string} merchantId - 商家ID
   */
  getStats: (merchantId) => get(`/v1/orders/stats/${merchantId}`),

  /**
   * 再来一单
   * POST /v1/orders/{orderId}/reorder
   * @param {string} orderId - 订单ID
   */
  reorder: (orderId) => post(`/v1/orders/${orderId}/reorder`),

  /**
   * 获取订单数量统计
   * GET /v1/orders/count
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID（可选）
   * @param {string} params.merchantId - 商家ID（可选）
   * @param {string} params.status - 订单状态（可选）
   */
  getCount: (params) => get('/v1/orders/count', params)
}

export default orderApi
