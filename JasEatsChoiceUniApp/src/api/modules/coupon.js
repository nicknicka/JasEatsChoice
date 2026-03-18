import { get, post } from '@/utils/request'

/**
 * 优惠券相关API
 */
export const couponApi = {
  /**
   * 获取我的优惠券
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} params.status - 状态(available/used/expired)
   */
  getMyCoupons: (params) => get('/api/coupon/my', params),

  /**
   * 获取优惠券详情
   * @param {number} id - 优惠券ID
   */
  getDetail: (id) => get(`/api/coupon/${id}`),

  /**
   * 领取优惠券
   * @param {number} couponId - 优惠券ID
   */
  receive: (couponId) => post(`/api/coupon/${couponId}/receive`),

  /**
   * 获取可领取的优惠券
   * @param {Object} params - 查询参数
   */
  getAvailable: (params) => get('/api/coupon/available', params),

  /**
   * 获取商家优惠券
   * @param {number} merchantId - 商家ID
   */
  getMerchantCoupons: (merchantId) => get(`/api/coupon/merchant/${merchantId}`),

  /**
   * 校验优惠券
   * @param {Object} data - 校验数据
   * @param {number} data.couponId - 优惠券ID
   * @param {number} data.merchantId - 商家ID
   * @param {number} data.amount - 订单金额
   */
  validate: (data) => post('/api/coupon/validate', data),

  /**
   * 获取优惠券使用记录
   * @param {number} couponId - 优惠券ID
   * @param {Object} params - 查询参数
   */
  getUsageHistory: (couponId, params) => get(`/api/coupon/${couponId}/history`, params)
}
