/**
 * 商家相关API
 */
import request from '@/utils/request'

/**
 * 获取商家列表
 */
export const getMerchantList = (params) => {
  return request({
    url: '/api/merchant/list',
    method: 'GET',
    params
  })
}

/**
 * 获取商家详情
 */
export const getMerchantDetail = (merchantId) => {
  return request({
    url: `/api/merchant/${merchantId}`,
    method: 'GET'
  })
}

/**
 * 搜索商家
 */
export const searchMerchant = (params) => {
  return request({
    url: '/api/merchant/search',
    method: 'GET',
    params
  })
}

/**
 * 获取附近商家
 */
export const getNearbyMerchants = (params) => {
  return request({
    url: '/api/merchant/nearby',
    method: 'GET',
    params
  })
}

/**
 * 获取商家分类
 */
export const getMerchantCategories = () => {
  return request({
    url: '/api/merchant/categories',
    method: 'GET'
  })
}

/**
 * 获取商家菜品列表
 */
export const getMerchantDishes = (merchantId, params) => {
  return request({
    url: `/api/merchant/${merchantId}/dishes`,
    method: 'GET',
    params
  })
}

/**
 * 获取商家评价
 */
export const getMerchantReviews = (merchantId, params) => {
  return request({
    url: `/api/merchant/${merchantId}/reviews`,
    method: 'GET',
    params
  })
}

/**
 * 收藏商家
 */
export const favoriteMerchant = (merchantId) => {
  return request({
    url: `/api/merchant/${merchantId}/favorite`,
    method: 'POST'
  })
}

/**
 * 取消收藏商家
 */
export const unfavoriteMerchant = (merchantId) => {
  return request({
    url: `/api/merchant/${merchantId}/favorite`,
    method: 'DELETE'
  })
}

/**
 * 获取商家优惠券
 */
export const getMerchantCoupons = (merchantId) => {
  return request({
    url: `/api/merchant/${merchantId}/coupons`,
    method: 'GET'
  })
}

export default {
  getMerchantList,
  getMerchantDetail,
  searchMerchant,
  getNearbyMerchants,
  getMerchantCategories,
  getMerchantDishes,
  getMerchantReviews,
  favoriteMerchant,
  unfavoriteMerchant,
  getMerchantCoupons
}
