/**
 * 地址相关API
 */
import request from '@/utils/request'

/**
 * 获取地址列表
 */
export const getAddressList = () => {
  return request({
    url: '/api/address/list',
    method: 'GET'
  })
}

/**
 * 获取地址详情
 */
export const getAddressDetail = (addressId) => {
  return request({
    url: `/api/address/${addressId}`,
    method: 'GET'
  })
}

/**
 * 创建地址
 */
export const createAddress = (data) => {
  return request({
    url: '/api/address/create',
    method: 'POST',
    data
  })
}

/**
 * 更新地址
 */
export const updateAddress = (addressId, data) => {
  return request({
    url: `/api/address/${addressId}`,
    method: 'PUT',
    data
  })
}

/**
 * 删除地址
 */
export const deleteAddress = (addressId) => {
  return request({
    url: `/api/address/${addressId}`,
    method: 'DELETE'
  })
}

/**
 * 设置默认地址
 */
export const setDefaultAddress = (addressId) => {
  return request({
    url: `/api/address/${addressId}/default`,
    method: 'POST'
  })
}

/**
 * 获取省市区数据
 */
export const getRegionData = () => {
  return request({
    url: '/api/region/data',
    method: 'GET'
  })
}

export default {
  getAddressList,
  getAddressDetail,
  createAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress,
  getRegionData
}
