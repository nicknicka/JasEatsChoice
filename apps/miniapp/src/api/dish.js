/**
 * 菜品相关API
 */
import request from '@/utils/request'

/**
 * 获取菜品列表
 */
export const getDishList = (params) => {
  return request({
    url: '/api/dish/list',
    method: 'GET',
    params
  })
}

/**
 * 获取菜品详情
 */
export const getDishDetail = (id) => {
  return request({
    url: `/api/dish/${id}`,
    method: 'GET'
  })
}

/**
 * 搜索菜品
 */
export const searchDish = (params) => {
  return request({
    url: '/api/dish/search',
    method: 'GET',
    params
  })
}

/**
 * 获取菜品分类
 */
export const getDishCategories = () => {
  return request({
    url: '/api/dish/categories',
    method: 'GET'
  })
}

/**
 * 获取分类下的菜品
 */
export const getDishesByCategory = (categoryId, params) => {
  return request({
    url: `/api/dish/category/${categoryId}`,
    method: 'GET',
    params
  })
}

/**
 * 收藏菜品
 */
export const favoriteDish = (dishId) => {
  return request({
    url: `/api/dish/${dishId}/favorite`,
    method: 'POST'
  })
}

/**
 * 取消收藏
 */
export const unfavoriteDish = (dishId) => {
  return request({
    url: `/api/dish/${dishId}/favorite`,
    method: 'DELETE'
  })
}

/**
 * 获取收藏列表
 */
export const getFavoriteList = (params) => {
  return request({
    url: '/api/user/favorites',
    method: 'GET',
    params
  })
}

/**
 * 获取浏览历史
 */
export const getViewHistory = (params) => {
  return request({
    url: '/api/user/history',
    method: 'GET',
    params
  })
}

/**
 * 记录浏览
 */
export const recordView = (dishId) => {
  return request({
    url: `/api/dish/${dishId}/view`,
    method: 'POST'
  })
}

/**
 * 获取菜品评价
 */
export const getDishReviews = (dishId, params) => {
  return request({
    url: `/api/dish/${dishId}/reviews`,
    method: 'GET',
    params
  })
}

/**
 * 提交菜品评价
 */
export const submitDishReview = (data) => {
  return request({
    url: '/api/review/dish',
    method: 'POST',
    data
  })
}

export default {
  getDishList,
  getDishDetail,
  searchDish,
  getDishCategories,
  getDishesByCategory,
  favoriteDish,
  unfavoriteDish,
  getFavoriteList,
  getViewHistory,
  recordView,
  getDishReviews,
  submitDishReview
}
