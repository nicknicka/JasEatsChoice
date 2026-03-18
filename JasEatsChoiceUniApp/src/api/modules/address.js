import { get, post, put, del } from '@/utils/request'

/**
 * 地址相关API
 */
export const addressApi = {
  /**
   * 获取地址列表
   */
  getList: () => get('/api/address/list'),

  /**
   * 获取默认地址
   */
  getDefault: () => get('/api/address/default'),

  /**
   * 获取地址详情
   * @param {number} id - 地址ID
   */
  getDetail: (id) => get(`/api/address/${id}`),

  /**
   * 新增地址
   * @param {Object} data - 地址数据
   * @param {string} data.name - 收货人姓名
   * @param {string} data.phone - 手机号
   * @param {string} data.province - 省份
   * @param {string} data.city - 城市
   * @param {string} data.district - 区县
   * @param {string} data.detail - 详细地址
   * @param {boolean} data.isDefault - 是否默认
   */
  create: (data) => post('/api/address', data),

  /**
   * 更新地址
   * @param {number} id - 地址ID
   * @param {Object} data - 地址数据
   */
  update: (id, data) => put(`/api/address/${id}`, data),

  /**
   * 删除地址
   * @param {number} id - 地址ID
   */
  delete: (id) => del(`/api/address/${id}`),

  /**
   * 设置默认地址
   * @param {number} id - 地址ID
   */
  setDefault: (id) => put(`/api/address/${id}/default`)
}
