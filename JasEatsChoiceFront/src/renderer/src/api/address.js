/**
 * 地址簿相关API
 *
 * 后端需要实现的接口：
 * - GET    /v1/addresses/user?userId={userId} - 获取用户地址列表
 * - GET    /v1/addresses/{id} - 获取地址详情
 * - POST   /v1/addresses - 添加地址
 * - PUT    /v1/addresses/{id} - 更新地址
 * - DELETE /v1/addresses/{id} - 删除地址
 * - PUT    /v1/addresses/{id}/default - 设置默认地址
 *
 * 当前状态：使用模拟数据，待后端API实现后启用真实API调用
 */
import api from '../utils/api'

export default {
  /**
   * 获取用户地址列表
   * @param {number} userId - 用户ID
   * @returns {Promise} 地址列表
   */
  getUserAddresses(userId) {
    // TODO: 后端API实现后启用以下代码
    // return api.get('/v1/addresses/user', {
    //   params: { userId }
    // })

    // 临时使用模拟数据
    return Promise.resolve({
      code: '200',
      message: '获取成功（模拟数据）',
      data: [
        {
          id: '1',
          userId: String(userId),
          contactName: '张三',
          contactPhone: '13800138000',
          province: '北京市',
          city: '北京市',
          district: '朝阳区',
          detail: 'XX路XX号XX小区X号楼X单元X室',
          fullAddress: '北京市北京市朝阳区XX路XX号XX小区X号楼X单元X室',
          isDefault: true,
          createTime: new Date().toISOString()
        }
      ],
      note: '模拟数据 - 后端地址API待实现'
    })
  },

  /**
   * 获取默认地址
   * @param {number} userId - 用户ID
   * @returns {Promise} 默认地址
   */
  getDefaultAddress(userId) {
    // TODO: 后端API实现后启用以下代码
    // return api.get('/v1/addresses/default', {
    //   params: { userId }
    // })

    // 临时调用getUserAddresses并过滤默认地址
    return this.getUserAddresses(userId).then(response => {
      const defaultAddress = response.data.find(addr => addr.isDefault)
      return {
        code: '200',
        message: '获取成功',
        data: defaultAddress || null,
        note: '模拟数据 - 后端地址API待实现'
      }
    })
  },

  /**
   * 获取地址详情
   * @param {string} addressId - 地址ID
   * @returns {Promise} 地址详情
   */
  getAddressDetail(addressId) {
    // TODO: 后端API实现后启用以下代码
    // return api.get(`/v1/addresses/${addressId}`)

    // 临时使用模拟响应
    return Promise.resolve({
      code: '200',
      message: '获取成功（模拟数据）',
      data: {
        id: addressId,
        contactName: '张三',
        contactPhone: '13800138000',
        province: '北京市',
        city: '北京市',
        district: '朝阳区',
        detail: 'XX路XX号XX小区X号楼X单元X室',
        fullAddress: '北京市北京市朝阳区XX路XX号XX小区X号楼X单元X室',
        isDefault: true
      },
      note: '模拟数据 - 后端地址API待实现'
    })
  },

  /**
   * 添加地址
   * @param {Object} addressData - 地址数据
   * @returns {Promise} 添加结果
   */
  addAddress(addressData) {
    // TODO: 后端API实现后启用以下代码
    // return api.post('/v1/addresses', addressData)

    // 临时使用模拟响应
    return Promise.resolve({
      code: '200',
      message: '添加成功（模拟数据）',
      data: {
        id: String(Date.now()),
        ...addressData,
        createTime: new Date().toISOString()
      },
      note: '模拟数据 - 后端地址API待实现'
    })
  },

  /**
   * 更新地址
   * @param {string} addressId - 地址ID
   * @param {Object} addressData - 地址数据
   * @returns {Promise} 更新结果
   */
  updateAddress(addressId, addressData) {
    // TODO: 后端API实现后启用以下代码
    // return api.put(`/v1/addresses/${addressId}`, addressData)

    // 临时使用模拟响应
    return Promise.resolve({
      code: '200',
      message: '更新成功（模拟数据）',
      data: {
        id: addressId,
        ...addressData,
        updateTime: new Date().toISOString()
      },
      note: '模拟数据 - 后端地址API待实现'
    })
  },

  /**
   * 删除地址
   * @param {string} addressId - 地址ID
   * @returns {Promise} 删除结果
   */
  deleteAddress(addressId) {
    // TODO: 后端API实现后启用以下代码
    // return api.delete(`/v1/addresses/${addressId}`)

    // 临时使用模拟响应
    return Promise.resolve({
      code: '200',
      message: '删除成功（模拟数据）',
      data: {
        addressId
      },
      note: '模拟数据 - 后端地址API待实现'
    })
  },

  /**
   * 设置默认地址
   * @param {string} addressId - 地址ID
   * @param {number} userId - 用户ID
   * @returns {Promise} 设置结果
   */
  setDefaultAddress(addressId, userId) {
    // TODO: 后端API实现后启用以下代码
    // return api.put(`/v1/addresses/${addressId}/default`, null, {
    //   params: { userId }
    // })

    // 临时使用模拟响应
    return Promise.resolve({
      code: '200',
      message: '设置成功（模拟数据）',
      data: {
        addressId,
        userId
      },
      note: '模拟数据 - 后端地址API待实现'
    })
  }
}
