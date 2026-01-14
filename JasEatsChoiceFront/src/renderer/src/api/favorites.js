/**
 * 收藏相关API接口
 */
import axios from 'axios'
import { API_CONFIG } from '../config/index.js'

/**
 * 获取用户收藏列表
 * @param {number} userId - 用户ID
 * @returns {Promise}
 */
export const getUserFavorites = (userId) => {
  return axios.get(`${API_CONFIG.baseURL}/v1/favorites/user/${userId}`)
}

/**
 * 添加收藏
 * @param {Object} data - 收藏数据
 * @param {number} data.userId - 用户ID
 * @param {number} data.dishId - 菜品ID
 * @param {string} data.dishName - 菜品名称
 * @param {string} data.dishType - 菜品类型
 * @param {number} data.calories - 卡路里
 * @param {Array} data.tags - 标签
 * @param {string} data.image - 图片
 * @param {number} data.rating - 评分
 * @returns {Promise}
 */
export const addFavorite = (data) => {
  return axios.post(`${API_CONFIG.baseURL}/v1/favorites`, data)
}

/**
 * 取消收藏
 * @param {number} favoriteId - 收藏ID
 * @returns {Promise}
 */
export const removeFavorite = (favoriteId) => {
  return axios.delete(`${API_CONFIG.baseURL}/v1/favorites/${favoriteId}`)
}

/**
 * 检查菜品是否已收藏
 * @param {number} userId - 用户ID
 * @param {number} dishId - 菜品ID
 * @returns {Promise}
 */
export const checkFavorite = (userId, dishId) => {
  return axios.get(`${API_CONFIG.baseURL}/v1/favorites/check`, {
    params: { userId, dishId }
  })
}

export default {
  getUserFavorites,
  addFavorite,
  removeFavorite,
  checkFavorite
}
