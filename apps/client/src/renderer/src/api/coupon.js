/**
 * 优惠券相关API
 */
import api from '../utils/api'

export default {
  /**
   * 获取用户可用优惠券列表
   * @param {number} userId - 用户ID
   * @returns {Promise} 优惠券列表
   */
  getUserCoupons(userId) {
    return api.get('/v1/coupons/user', {
      params: { userId }
    })
  },

  /**
   * 检查优惠券是否可用
   * @param {string} couponId - 优惠券ID
   * @param {number} orderAmount - 订单金额
   * @returns {Promise} 检查结果
   */
  checkCouponAvailable(couponId, orderAmount) {
    return api.post('/v1/coupons/check', null, {
      params: { couponId, orderAmount }
    })
  },

  /**
   * 使用优惠券
   * @param {string} couponId - 优惠券ID
   * @param {string} orderId - 订单ID
   * @returns {Promise} 使用结果
   */
  useCoupon(couponId, orderId) {
    return api.post('/v1/coupons/use', null, {
      params: { couponId, orderId }
    })
  },

  /**
   * 释放优惠券（取消订单时）
   * @param {string} couponId - 优惠券ID
   * @param {string} orderId - 订单ID
   * @returns {Promise} 释放结果
   */
  releaseCoupon(couponId, orderId) {
    return api.post('/v1/coupons/release', null, {
      params: { couponId, orderId }
    })
  }
}
