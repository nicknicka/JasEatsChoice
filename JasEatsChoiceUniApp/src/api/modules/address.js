/**
 * 地址相关API
 * 对接后端 AddressController
 * 基础路径: /v1/addresses
 */
import { get, post, put, del } from '@/utils/request'
import { ADDRESS_API, buildUrl } from '../urlEnum'

export const addressApi = {
  /**
   * 获取地址列表
   * GET /v1/addresses
   * @returns {Promise} 返回地址列表
   */
  getAddresses: () => get(ADDRESS_API.GET_ADDRESSES),

  /**
   * 获取地址列表（别名）
   * @returns {Promise} 返回地址列表
   */
  getList: () => get('/api/address/list'),

  /**
   * 获取默认地址
   * GET /api/address/default
   * @returns {Promise} 返回默认地址
   */
  getDefault: () => get('/api/address/default'),

  /**
   * 获取地址详情
   * GET /v1/addresses/{addressId}
   * @param {string} id - 地址ID
   * @returns {Promise} 返回地址详情
   */
  getAddress: (id) => get(buildUrl(ADDRESS_API.GET_ADDRESS, { addressId: id })),

  /**
   * 获取地址详情（别名）
   * @param {number} id - 地址ID
   * @returns {Promise} 返回地址详情
   */
  getDetail: (id) => get(`/api/address/${id}`),

  /**
   * 创建地址
   * POST /v1/addresses
   * @param {Object} data - 地址数据
   * @param {string} data.name - 收货人姓名
   * @param {string} data.phone - 手机号
   * @param {string} data.province - 省份
   * @param {string} data.city - 城市
   * @param {string} data.district - 区县
   * @param {string} data.detail - 详细地址
   * @param {boolean} data.isDefault - 是否默认
   * @param {number} data.latitude - 纬度（可选）
   * @param {number} data.longitude - 经度（可选）
   * @returns {Promise} 返回创建结果
   */
  createAddress: (data) => post(ADDRESS_API.CREATE_ADDRESS, data),

  /**
   * 创建地址（别名）
   * @param {Object} data - 地址数据
   * @returns {Promise} 返回创建结果
   */
  create: (data) => post('/api/address', data),

  /**
   * 更新地址
   * PUT /v1/addresses/{addressId}
   * @param {string} id - 地址ID
   * @param {Object} data - 地址数据
   * @param {string} data.name - 收货人姓名
   * @param {string} data.phone - 手机号
   * @param {string} data.province - 省份
   * @param {string} data.city - 城市
   * @param {string} data.district - 区县
   * @param {string} data.detail - 详细地址
   * @param {boolean} data.isDefault - 是否默认
   * @param {number} data.latitude - 纬度（可选）
   * @param {number} data.longitude - 经度（可选）
   * @returns {Promise} 返回更新结果
   */
  updateAddress: (id, data) => put(buildUrl(ADDRESS_API.UPDATE_ADDRESS, { addressId: id }), data),

  /**
   * 更新地址（别名）
   * @param {number} id - 地址ID
   * @param {Object} data - 地址数据
   * @returns {Promise} 返回更新结果
   */
  update: (id, data) => put(`/api/address/${id}`, data),

  /**
   * 删除地址
   * DELETE /v1/addresses/{addressId}
   * @param {string} id - 地址ID
   * @returns {Promise} 返回删除结果
   */
  deleteAddress: (id) => del(buildUrl(ADDRESS_API.DELETE_ADDRESS, { addressId: id })),

  /**
   * 删除地址（别名）
   * @param {number} id - 地址ID
   * @returns {Promise} 返回删除结果
   */
  delete: (id) => del(`/api/address/${id}`),

  /**
   * 设置默认地址
   * PUT /v1/addresses/{addressId}/default
   * @param {string} id - 地址ID
   * @returns {Promise} 返回设置结果
   */
  setDefaultAddress: (id) => put(buildUrl(ADDRESS_API.SET_DEFAULT, { addressId: id })),

  /**
   * 设置默认地址（别名）
   * @param {number} id - 地址ID
   * @returns {Promise} 返回设置结果
   */
  setDefault: (id) => put(`/api/address/${id}/default`)
}

export default addressApi
