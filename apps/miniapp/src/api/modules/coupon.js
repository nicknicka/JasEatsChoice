/**
 * 优惠券相关API
 * 对接后端 CouponController
 * 基础路径: /v1/coupons
 */
import { get, post } from '@/utils/request'
import { COUPON_API } from '../urlEnum'

const getCurrentUserId = () => {
  const userInfo = uni.getStorageSync('userInfo') || {}
  return userInfo.userId || userInfo.id || ''
}

const extractData = (response) => response?.data ?? response ?? null

const buildQueryUrl = (url, params = {}) => {
  const query = Object.entries(params)
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join('&')
  return query ? `${url}?${query}` : url
}

export const couponApi = {
  /**
   * 获取我的优惠券
   * GET /v1/coupons/user
   * @param {string|Object} userId - 用户ID，或直接传查询参数对象
   * @param {Object} params - 查询参数
   * @param {string} params.status - 状态(available/used/expired)
   * @returns {Promise<Array>} 返回优惠券列表
   */
  getUserCoupons: async (userId, params) => {
    const query = typeof userId === 'object' || userId === undefined
      ? { ...(userId || {}) }
      : { ...(params || {}), userId }

    query.userId = query.userId || getCurrentUserId()

    const response = await get(COUPON_API.GET_USER_COUPONS, query)
    return extractData(response) || []
  },

  /**
   * 获取我的优惠券（别名）
   * @param {Object} params - 查询参数
   * @returns {Promise<Array>} 返回优惠券列表
   */
  getMyCoupons: (params) => couponApi.getUserCoupons(params),

  /**
   * 获取优惠券详情
   * 当前后端未提供详情接口，前端从列表中筛选
   * @param {number} id - 优惠券ID
   * @returns {Promise<Object|null>} 返回优惠券详情
   */
  getDetail: async (id, params = {}) => {
    const list = await couponApi.getUserCoupons(params)
    return list.find(item => item.id === id || item.userCouponId === id) || null
  },

  /**
   * 领取优惠券
   * 后端暂未提供商家领券接口
   * @param {string} couponId - 优惠券ID
   * @returns {Promise} 返回领取结果
   */
  claim: () => Promise.reject(new Error('后端暂未提供领券接口')),

  /**
   * 领取优惠券（别名）
   * @param {number} couponId - 优惠券ID
   * @returns {Promise} 返回领取结果
   */
  receive: (couponId) => couponApi.claim(couponId),

  /**
   * 获取支付场景可用优惠券
   * GET /v1/payment/coupons
   * @param {Object} params - 查询参数
   * @returns {Promise<Array>} 返回可用优惠券列表
   */
  getAvailable: async (params = {}) => {
    const query = {
      ...params,
      userId: params.userId || getCurrentUserId()
    }
    const response = await get(COUPON_API.GET_AVAILABLE_COUPONS, query)
    return extractData(response) || []
  },

  /**
   * 使用优惠券
   * POST /v1/coupons/use
   * @param {string} couponId - 优惠券ID
   * @param {Object} data - 使用数据
   * @param {string} data.orderId - 订单ID
   * @returns {Promise} 返回使用结果
   */
  use: (couponId, data) => post(buildQueryUrl(COUPON_API.USE_COUPON, {
    couponId,
    orderId: data?.orderId
  })),

  /**
   * 释放优惠券
   * POST /v1/coupons/release
   * @param {string} couponId - 优惠券ID
   * @param {Object} data - 释放数据
   * @param {string} data.orderId - 订单ID
   * @returns {Promise} 返回释放结果
   */
  release: (couponId, data) => post(buildQueryUrl(COUPON_API.RELEASE_COUPON, {
    couponId,
    orderId: data?.orderId
  })),

  /**
   * 校验优惠券
   * POST /v1/coupons/check
   * @param {Object} data - 校验数据
   * @param {number} data.couponId - 优惠券ID
   * @param {number} data.amount - 订单金额
   * @returns {Promise} 返回校验结果
   */
  validate: async (data) => {
    const response = await post(buildQueryUrl(COUPON_API.CHECK_COUPON, {
      couponId: data?.couponId,
      orderAmount: data?.orderAmount ?? data?.amount
    }))
    return extractData(response)
  },

  /**
   * 发放测试优惠券（开发环境）
   * POST /v1/coupons/issue-test
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @returns {Promise} 返回发券结果
   */
  issueTestCoupon: (params = {}) => post(buildQueryUrl(COUPON_API.ISSUE_TEST_COUPON, {
    userId: params.userId || getCurrentUserId()
  })),

  /**
   * 获取优惠券使用记录
   * 当前后端未提供独立历史接口，前端从列表中过滤
   * @param {number} couponId - 优惠券ID
   * @returns {Promise<Array>} 返回使用记录
   */
  getUsageHistory: async (couponId, params = {}) => {
    const list = await couponApi.getUserCoupons(params)
    return list.filter(item => (item.id === couponId || item.userCouponId === couponId) && item.orderId)
  }
}

export default couponApi
