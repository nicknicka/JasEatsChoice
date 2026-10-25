/**
 * 优惠券相关API - 示例文件
 *
 * TODO: 后端需要实现以下优惠券相关接口
 *
 * 需要的后端实现：
 * 1. Coupon实体类（entity/Coupon.java）
 *    - id: 优惠券ID
 *    - userId: 用户ID
 *    - name: 优惠券名称
 *    - amount: 优惠金额
 *    - minAmount: 最低消费金额
 *    - status: 状态（available-可用, used-已使用, expired-已过期）
 *    - orderId: 关联订单ID（使用后）
 *    - createTime: 创建时间
 *    - expireTime: 过期时间
 *
 * 2. CouponService服务类
 *
 * 3. CouponController控制器类，提供以下接口：
 *    - GET  /v1/coupons/user?userId={userId} - 获取用户优惠券列表
 *    - POST /v1/coupons/check - 检查优惠券是否可用
 *    - POST /v1/coupons/use - 使用优惠券
 *    - POST /v1/coupons/release - 释放优惠券
 *
 * 当前状态：前端使用默认优惠券数据进行演示
 */
import api from '../utils/api'

export default {
  /**
   * 获取用户可用优惠券列表
   */
  getUserCoupons(userId) {
    return api.get('/v1/coupons/user', {
      params: { userId }
    })
  },

  /**
   * 检查优惠券是否可用
   */
  checkCouponAvailable(couponId, orderAmount) {
    return api.post('/v1/coupons/check', null, {
      params: { couponId, orderAmount }
    })
  },

  /**
   * 使用优惠券
   */
  useCoupon(couponId, orderId) {
    return api.post('/v1/coupons/use', null, {
      params: { couponId, orderId }
    })
  },

  /**
   * 释放优惠券（取消订单时）
   */
  releaseCoupon(couponId, orderId) {
    return api.post('/v1/coupons/release', null, {
      params: { couponId, orderId }
    })
  }
}
