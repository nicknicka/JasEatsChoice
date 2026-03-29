/**
 * 优惠券相关API
 * 对接后端 CouponController
 * 基础路径: /api/coupon 或 /v1/coupons
 */
import { get, post } from '@/utils/request'
import { COUPON_API, buildUrl } from '../urlEnum'

export const couponApi = {
  /**
   * 获取我的优惠券
   * GET /v1/coupons/user/{userId}
   * @param {string} userId - 用户ID
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} params.status - 状态(available/used/expired)
   * @returns {Promise} 返回优惠券列表
   */
  getUserCoupons: (userId, params) => get(buildUrl(COUPON_API.GET_USER_COUPONS, { userId }), params),

  /**
   * 获取我的优惠券（别名）
   * @param {Object} params - 查询参数
   * @returns {Promise} 返回优惠券列表
   */
  getMyCoupons: (params) => get('/api/coupon/my', params),

  /**
   * 获取优惠券详情
   * GET /api/coupon/{id}
   * @param {number} id - 优惠券ID
   * @returns {Promise} 返回优惠券详情
   */
  getDetail: (id) => get(`/api/coupon/${id}`),

  /**
   * 领取优惠券
   * POST /v1/coupons/{couponId}/claim
   * @param {string} couponId - 优惠券ID
   * @returns {Promise} 返回领取结果
   */
  claim: (couponId) => post(buildUrl(COUPON_API.CLAIM_COUPON, { couponId })),

  /**
   * 领取优惠券（别名）
   * @param {number} couponId - 优惠券ID
   * @returns {Promise} 返回领取结果
   */
  receive: (couponId) => post(`/api/coupon/${couponId}/receive`),

  /**
   * 获取可领取的优惠券
   * GET /v1/coupons/available
   * @param {Object} params - 查询参数
   * @returns {Promise} 返回可领取的优惠券列表
   */
  getAvailable: (params) => get(COUPON_API.GET_AVAILABLE_COUPONS, params),

  /**
   * 获取商家优惠券
   * GET /v1/coupons/merchant/{merchantId}
   * @param {string} merchantId - 商家ID
   * @returns {Promise} 返回优惠券列表
   */
  getMerchantCoupons: (merchantId) => get(buildUrl(COUPON_API.GET_MERCHANT_COUPONS, { merchantId })),

  /**
   * 获取商家优惠券（别名）
   * @param {number} merchantId - 商家ID
   * @returns {Promise} 返回优惠券列表
   */
  getMerchantCouponsOld: (merchantId) => get(`/api/coupon/merchant/${merchantId}`),

  /**
   * 使用优惠券
   * POST /v1/coupons/{couponId}/use
   * @param {string} couponId - 优惠券ID
   * @param {Object} data - 使用数据
   * @param {string} data.orderId - 订单ID
   * @returns {Promise} 返回使用结果
   */
  use: (couponId, data) => post(buildUrl(COUPON_API.USE_COUPON, { couponId }), data),

  /**
   * 校验优惠券
   * POST /api/coupon/validate
   * @param {Object} data - 校验数据
   * @param {number} data.couponId - 优惠券ID
   * @param {number} data.merchantId - 商家ID
   * @param {number} data.amount - 订单金额
   * @returns {Promise} 返回校验结果
   */
  validate: (data) => post('/api/coupon/validate', data),

  /**
   * 获取优惠券使用记录
   * GET /api/coupon/{couponId}/history
   * @param {number} couponId - 优惠券ID
   * @param {Object} params - 查询参数
   * @returns {Promise} 返回使用记录
   */
  getUsageHistory: (couponId, params) => get(`/api/coupon/${couponId}/history`, params)
}

export default couponApi
