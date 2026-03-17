/**
 * 优惠券相关API
 */
import request from '@/utils/request'

/**
 * 获取用户优惠券列表
 */
export const getUserCoupons = (params) => {
  return request({
    url: '/api/coupon/user/list',
    method: 'GET',
    params
  })
}

/**
 * 获取可用优惠券
 */
export const getAvailableCoupons = (params) => {
  return request({
    url: '/api/coupon/available',
    method: 'GET',
    params
  })
}

/**
 * 领取优惠券
 */
export const receiveCoupon = (couponId) => {
  return request({
    url: `/api/coupon/${couponId}/receive`,
    method: 'POST'
  })
}

/**
 * 使用优惠券
 */
export const useCoupon = (couponId, orderId) => {
  return request({
    url: `/api/coupon/${couponId}/use`,
    method: 'POST',
    data: { orderId }
  })
}

/**
 * 获取优惠券详情
 */
export const getCouponDetail = (couponId) => {
  return request({
    url: `/api/coupon/${couponId}`,
    method: 'GET'
  })
}

/**
 * 获取商家优惠券
 */
export const getMerchantCoupons = (merchantId) => {
  return request({
    url: `/api/coupon/merchant/${merchantId}`,
    method: 'GET'
  })
}

export default {
  getUserCoupons,
  getAvailableCoupons,
  receiveCoupon,
  useCoupon,
  getCouponDetail,
  getMerchantCoupons
}
