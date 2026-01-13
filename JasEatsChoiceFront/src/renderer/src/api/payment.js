/**
 * 支付相关API
 */
import api from '../utils/api'

export default {
  /**
   * 订单支付
   */
  payOrder(orderId, userId, paymentMethod = 'wallet') {
    return api.post(`/v1/orders/${orderId}/pay`, null, {
      params: { userId, paymentMethod }
    })
  },

  /**
   * 获取订单支付记录
   */
  getOrderPayment(orderId) {
    return api.get(`/v1/orders/${orderId}/payment`)
  }
}
