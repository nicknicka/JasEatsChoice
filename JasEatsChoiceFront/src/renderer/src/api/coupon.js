/**
 * 优惠券相关API
 *
 * 后端需要实现的接口：
 * - GET  /v1/coupons/user?userId={userId} - 获取用户优惠券列表
 * - POST /v1/coupons/check - 检查优惠券是否可用
 * - POST /v1/coupons/use - 使用优惠券
 * - POST /v1/coupons/release - 释放优惠券
 *
 * 当前状态：使用模拟数据，待后端API实现后启用真实API调用
 */
import api from '../utils/api'

export default {
  /**
   * 获取用户可用优惠券列表
   * @param {number} userId - 用户ID
   * @returns {Promise} 优惠券列表
   */
  getUserCoupons(userId) {
    // TODO: 后端API实现后启用以下代码
    // return api.get('/v1/coupons/user', {
    //   params: { userId }
    // })

    // 临时使用模拟数据
    return Promise.resolve({
      code: '200',
      message: '获取成功（模拟数据）',
      data: [
        {
          id: '1',
          userId: String(userId),
          name: '新用户专享50元优惠券',
          amount: 50.0,
          minAmount: 100,
          status: 'available',
          createTime: new Date().toISOString(),
          expireTime: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString()
        }
      ],
      note: '模拟数据 - 后端优惠券API待实现'
    })
  },

  /**
   * 检查优惠券是否可用
   * @param {string} couponId - 优惠券ID
   * @param {number} orderAmount - 订单金额
   * @returns {Promise} 检查结果
   */
  checkCouponAvailable(couponId, orderAmount) {
    // TODO: 后端API实现后启用以下代码
    // return api.post('/v1/coupons/check', null, {
    //   params: { couponId, orderAmount }
    // })

    // 临时使用模拟逻辑
    return Promise.resolve({
      code: '200',
      message: '优惠券可用',
      data: {
        available: true,
        discountAmount: 50.0,
        note: '模拟数据 - 后端优惠券API待实现'
      }
    })
  },

  /**
   * 使用优惠券
   * @param {string} couponId - 优惠券ID
   * @param {string} orderId - 订单ID
   * @returns {Promise} 使用结果
   */
  useCoupon(couponId, orderId) {
    // TODO: 后端API实现后启用以下代码
    // return api.post('/v1/coupons/use', null, {
    //   params: { couponId, orderId }
    // })

    // 临时使用模拟响应
    return Promise.resolve({
      code: '200',
      message: '优惠券使用成功',
      data: {
        couponId,
        orderId,
        note: '模拟数据 - 后端优惠券API待实现'
      }
    })
  },

  /**
   * 释放优惠券（取消订单时）
   * @param {string} couponId - 优惠券ID
   * @param {string} orderId - 订单ID
   * @returns {Promise} 释放结果
   */
  releaseCoupon(couponId, orderId) {
    // TODO: 后端API实现后启用以下代码
    // return api.post('/v1/coupons/release', null, {
    //   params: { couponId, orderId }
    // })

    // 临时使用模拟响应
    return Promise.resolve({
      code: '200',
      message: '优惠券已释放',
      data: {
        couponId,
        orderId,
        note: '模拟数据 - 后端优惠券API待实现'
      }
    })
  }
}
