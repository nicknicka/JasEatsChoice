/**
 * 地址簿相关API
 */
import api from '../utils/api'

export default {
  /**
   * 获取用户地址列表
   * @param {number} userId - 用户ID
   * @returns {Promise} 地址列表
   */
  getUserAddresses(userId) {
    return api.get('/v1/addresses/user', {
      params: { userId }
    })
  },

  /**
   * 获取默认地址
   * @param {number} userId - 用户ID
   * @returns {Promise} 默认地址
   */
  getDefaultAddress(userId) {
    return api.get('/v1/addresses/default', {
      params: { userId }
    })
  },

  /**
   * 获取地址详情
   * @param {string} addressId - 地址ID
   * @returns {Promise} 地址详情
   */
  getAddressDetail(addressId) {
    // 后端暂不支持单独获取地址详情，前端可以从列表中获取
    return api.get(`/v1/addresses/user`).then((response) => {
      const address = response.data.find((addr) => addr.id === addressId)
      return {
        code: address ? '200' : '404',
        message: address ? '获取成功' : '地址不存在',
        data: address || null
      }
    })
  },

  /**
   * 添加地址
   * @param {Object} addressData - 地址数据
   * @returns {Promise} 添加结果
   */
  addAddress(addressData) {
    return api.post('/v1/addresses', addressData)
  },

  /**
   * 更新地址
   * @param {string} addressId - 地址ID
   * @param {Object} addressData - 地址数据
   * @returns {Promise} 更新结果
   */
  updateAddress(addressId, addressData) {
    return api.put(`/v1/addresses/${addressId}`, addressData)
  },

  /**
   * 删除地址
   * @param {string} addressId - 地址ID
   * @returns {Promise} 删除结果
   */
  deleteAddress(addressId) {
    return api.delete(`/v1/addresses/${addressId}`)
  },

  /**
   * 设置默认地址
   * @param {string} addressId - 地址ID
   * @param {number} userId - 用户ID
   * @returns {Promise} 设置结果
   */
  setDefaultAddress(addressId, userId) {
    return api.put(`/v1/addresses/${addressId}/default`, null, {
      params: { userId }
    })
  }
}
