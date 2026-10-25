/**
 * 地址相关API
 * 对接后端 AddressController
 * 基础路径: /v1/addresses
 */
import { get, post, put, del } from '@/utils/request'
import { ADDRESS_API, buildUrl } from '../urlEnum'

const getCurrentUserId = () => {
  const userInfo = uni.getStorageSync('userInfo') || {}
  return userInfo.userId || userInfo.id || ''
}

const ensureUserId = (params = {}) => ({
  ...params,
  userId: params.userId || getCurrentUserId()
})

const normalizeAddressPayload = (data = {}) => {
  const normalized = {
    ...data,
    userId: data.userId || getCurrentUserId(),
    receiverName: data.receiverName || data.name || '',
    receiverPhone: data.receiverPhone || data.phone || '',
    detail: data.detail || data.detailAddress || ''
  }

  delete normalized.name
  delete normalized.phone
  delete normalized.detailAddress
  delete normalized.latitude
  delete normalized.longitude

  return normalized
}

const buildQueryUrl = (url, params = {}) => {
  const query = Object.entries(params)
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join('&')
  return query ? `${url}?${query}` : url
}

export const addressApi = {
  /**
   * 获取地址列表
   * GET /v1/addresses/user
   * @returns {Promise} 返回地址列表
   */
  getAddresses: (params = {}) => get(ADDRESS_API.GET_ADDRESSES, ensureUserId(params)),

  /**
   * 获取地址列表（别名）
   * @returns {Promise} 返回地址列表
   */
  getList: (params = {}) => get(ADDRESS_API.GET_ADDRESSES, ensureUserId(params)),

  /**
   * 获取默认地址
   * GET /v1/addresses/default
   * @returns {Promise} 返回默认地址
   */
  getDefault: (params = {}) => get(ADDRESS_API.GET_DEFAULT, ensureUserId(params)),

  /**
   * 获取地址详情
   * 当前后端未提供独立详情接口，先从地址列表中兼容读取
   * @param {string} id - 地址ID
   * @returns {Promise} 返回地址详情
   */
  getAddress: async (id, params = {}) => {
    const res = await get(ADDRESS_API.GET_ADDRESSES, ensureUserId(params))
    const list = res?.data || res || []
    return Array.isArray(list) ? list.find(item => (item.id || item.addressId) === id) || null : null
  },

  /**
   * 获取地址详情（别名）
   * @param {number} id - 地址ID
   * @returns {Promise} 返回地址详情
   */
  getDetail: (id, params = {}) => addressApi.getAddress(id, params),

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
  createAddress: (data) => post(ADDRESS_API.CREATE_ADDRESS, normalizeAddressPayload(data)),

  /**
   * 创建地址（别名）
   * @param {Object} data - 地址数据
   * @returns {Promise} 返回创建结果
   */
  create: (data) => post(ADDRESS_API.CREATE_ADDRESS, normalizeAddressPayload(data)),

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
  updateAddress: (id, data) => put(buildUrl(ADDRESS_API.UPDATE_ADDRESS, { addressId: id }), normalizeAddressPayload(data)),

  /**
   * 更新地址（别名）
   * @param {number} id - 地址ID
   * @param {Object} data - 地址数据
   * @returns {Promise} 返回更新结果
   */
  update: (id, data) => put(buildUrl(ADDRESS_API.UPDATE_ADDRESS, { addressId: id }), normalizeAddressPayload(data)),

  /**
   * 删除地址
   * DELETE /v1/addresses/{addressId}
   * @param {string} id - 地址ID
   * @returns {Promise} 返回删除结果
   */
  deleteAddress: (id, params = {}) => del(
    buildQueryUrl(buildUrl(ADDRESS_API.DELETE_ADDRESS, { addressId: id }), ensureUserId(params))
  ),

  /**
   * 删除地址（别名）
   * @param {number} id - 地址ID
   * @returns {Promise} 返回删除结果
   */
  delete: (id, params = {}) => del(
    buildQueryUrl(buildUrl(ADDRESS_API.DELETE_ADDRESS, { addressId: id }), ensureUserId(params))
  ),

  /**
   * 设置默认地址
   * PUT /v1/addresses/{addressId}/default
   * @param {string} id - 地址ID
   * @returns {Promise} 返回设置结果
   */
  setDefaultAddress: (id, params = {}) => put(
    buildQueryUrl(buildUrl(ADDRESS_API.SET_DEFAULT, { addressId: id }), ensureUserId(params))
  ),

  /**
   * 设置默认地址（别名）
   * @param {number} id - 地址ID
   * @returns {Promise} 返回设置结果
   */
  setDefault: (id, params = {}) => put(
    buildQueryUrl(buildUrl(ADDRESS_API.SET_DEFAULT, { addressId: id }), ensureUserId(params))
  )
}

export default addressApi
