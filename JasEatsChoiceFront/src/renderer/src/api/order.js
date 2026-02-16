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
  },

  /**
   * 确认收货（将订单从已上菜状态更新为已完成状态）
   */
  confirmReceipt(orderId) {
    console.log('📦 orderApi - 调用确认收货API', {
      orderId,
      endpoint: `/v1/orders/${orderId}/status`,
      method: 'PUT',
      params: { status: 7 },
      timestamp: new Date().toISOString()
    })

    return api.put(`/v1/orders/${orderId}/status`, null, {
      params: { status: 7 } // 7表示已完成
    })
  }
}
