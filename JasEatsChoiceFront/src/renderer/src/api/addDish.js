/**
 * 加菜功能API接口
 */
import api from './index'

export default {
  /**
   * 创建加菜请求
   */
  createAddDishRequest(data) {
    return api.post('/v1/add-dish/request', data)
  },

  /**
   * 获取加菜审核列表
   */
  getReviewList(groupOrderId) {
    return api.get(`/v1/add-dish/review-list/${groupOrderId}`)
  },

  /**
   * 批量审核加菜请求
   */
  batchReview(data) {
    return api.put('/v1/add-dish/review', data)
  },

  /**
   * 撤回加菜请求
   */
  withdrawRequest(requestId) {
    return api.delete(`/v1/add-dish/request/${requestId}`)
  },

  /**
   * 获取加菜历史
   */
  getHistory(groupOrderId) {
    return api.get(`/v1/add-dish/history/${groupOrderId}`)
  },

  /**
   * 检查饮食禁忌冲突
   */
  checkAllergy(data) {
    return api.post('/v1/add-dish/check-allergy', data)
  },

  /**
   * 获取加菜设置
   */
  getSetting(groupOrderId) {
    return api.get(`/v1/add-dish/setting/${groupOrderId}`)
  },

  /**
   * 更新加菜设置
   */
  updateSetting(data) {
    return api.put('/v1/add-dish/setting', data)
  }
}
