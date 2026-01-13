/**
 * 订单相关API
 */
import api from '../utils/api'

export default {
  /**
   * 创建订单
   */
  createOrder(orderData) {
    return api.post('/v1/orders', orderData)
  },

  /**
   * 根据用户ID获取订单列表
   */
  getOrdersByUserId(userId) {
    return api.get(`/v1/orders/user/${userId}`)
  },

  /**
   * 根据商家ID获取订单列表
   */
  getOrdersByMerchantId(merchantId) {
    return api.get(`/v1/orders/merchant/${merchantId}`)
  },

  /**
   * 获取订单详情
   */
  getOrderDetail(orderId) {
    return api.get(`/v1/orders/${orderId}`)
  },

  /**
   * 更新订单状态
   */
  updateOrderStatus(orderId, status) {
    return api.put(`/v1/orders/${orderId}/status`, null, {
      params: { status }
    })
  }
}
