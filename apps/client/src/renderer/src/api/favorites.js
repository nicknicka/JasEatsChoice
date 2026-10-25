/**
 * 收藏相关API接口
 */
import axios from 'axios'
import { API_CONFIG } from '../config/index.js'

/**
 * 获取用户收藏列表
 * @param {string} userId - 用户ID
 * @returns {Promise}
 */
export const getUserFavorites = (userId) => {
  return axios.get(`${API_CONFIG.baseURL}/v1/collections`, {
    params: { userId }
  })
}

/**
 * 根据类型获取用户收藏
 * @param {string} userId - 用户ID
 * @param {string} type - 收藏类型
 * @returns {Promise}
 */
export const getUserFavoritesByType = (userId, type) => {
  return axios.get(`${API_CONFIG.baseURL}/v1/collections/type`, {
    params: { userId, type }
  })
}

/**
 * 添加收藏
 * @param {Object} data - 收藏数据
 * @param {string} data.userId - 用户ID
 * @param {string} data.type - 收藏类型 (dish/merchant/menu等)
 * @param {string} data.id - 收藏对象ID
 * @returns {Promise}
 */
export const addFavorite = (data) => {
  return axios.post(`${API_CONFIG.baseURL}/v1/collections`, data)
}

/**
 * 取消收藏
 * @param {string} userId - 用户ID
 * @param {string} type - 收藏类型
 * @param {string} id - 收藏对象ID
 * @returns {Promise}
 */
export const removeFavorite = (userId, type, id) => {
  return axios.delete(`${API_CONFIG.baseURL}/v1/collections`, {
    params: { userId, type, id }
  })
}

/**
 * 检查是否已收藏
 * @param {string} userId - 用户ID
 * @param {string} type - 收藏类型
 * @param {string} id - 收藏对象ID
 * @returns {Promise}
 */
export const checkFavorite = (userId, type, id) => {
  return axios.get(`${API_CONFIG.baseURL}/v1/collections/check`, {
    params: { userId, type, id }
  })
}

export default {
  getUserFavorites,
  getUserFavoritesByType,
  addFavorite,
  removeFavorite,
  checkFavorite
}
