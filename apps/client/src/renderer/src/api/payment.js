/**
 * 支付相关API
 */
import api from '../utils/api'

export default {
  /**
   * 订单支付
   * @param {string} orderId 订单ID
   * @param {string} userId 用户ID
   * @param {string} paymentMethod 支付方式
   * @param {string} paymentPassword 支付密码（后端强制校验，缺失或错误返回 400）
   */
  payOrder(orderId, userId, paymentMethod = 'wallet', paymentPassword) {
    return api.post(`/v1/orders/${orderId}/pay`, null, {
      params: { userId, paymentMethod, paymentPassword }
    })
  },

  /**
   * 获取订单支付记录
   */
  getOrderPayment(orderId) {
    return api.get(`/v1/orders/${orderId}/payment`)
  },

  /**
   * 检查支付密码状态
   */
  checkPaymentPassword(userId) {
    return api.get(`/v1/payment-password/check/${userId}`)
  },

  /**
   * 设置支付密码
   */
  setupPaymentPassword(userId, password, verificationCode) {
    return api.post('/v1/payment-password/setup', null, {
      params: { userId, password, verificationCode }
    })
  },

  /**
   * 修改支付密码
   */
  changePaymentPassword(userId, oldPassword, newPassword) {
    return api.post('/v1/payment-password/change', null, {
      params: { userId, oldPassword, newPassword }
    })
  },

  /**
   * 重置支付密码
   */
  resetPaymentPassword(userId, newPassword, verificationCode) {
    return api.post('/v1/payment-password/reset', null, {
      params: { userId, newPassword, verificationCode }
    })
  }
}
