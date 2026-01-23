/**
 * 商家相关API
 */
import api from '../utils/api'

export default {
  /**
   * 获取商家列表（支持搜索）
   * @param {Object} params - 查询参数
   * @param {string} params.category - 商家分类（可选）
   * @param {string} params.keyword - 搜索关键词（可选）
   */
  getMerchants(params) {
    return api.get('/v1/merchant', { params })
  },

  /**
   * 获取商家详情
   * @param {string} merchantId - 商家ID
   */
  getMerchantDetail(merchantId) {
    return api.get(`/v1/merchant/${merchantId}`)
  },

  /**
   * 商家注册
   * @param {Object} registerData - 注册数据
   */
  register(registerData) {
    return api.post('/v1/merchant/register', registerData)
  },

  /**
   * 更新商家信息
   * @param {string} merchantId - 商家ID
   * @param {Object} updateData - 更新数据
   */
  updateMerchant(merchantId, updateData) {
    return api.put(`/v1/merchant/${merchantId}`, updateData)
  },

  /**
   * 更新商家营业状态
   * @param {string} merchantId - 商家ID
   */
  updateMerchantStatus(merchantId) {
    return api.put(`/v1/merchant/${merchantId}/status`)
  },

  /**
   * 获取商家相册
   * @param {string} merchantId - 商家ID
   */
  getMerchantAlbum(merchantId) {
    return api.get(`/v1/merchant/${merchantId}/album`)
  },

  /**
   * 上传商家相册照片
   * @param {string} merchantId - 商家ID
   * @param {FormData} formData - 包含images和albumType的表单数据
   */
  uploadMerchantAlbum(merchantId, formData) {
    return api.post(`/v1/merchant/${merchantId}/album`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 删除商家相册照片
   * @param {string} merchantId - 商家ID
   * @param {Object} params - 包含imageUrl和albumType的参数
   */
  deleteMerchantAlbum(merchantId, params) {
    return api.delete(`/v1/merchant/${merchantId}/album`, { params })
  },

  /**
   * 上传商家头像
   * @param {string} merchantId - 商家ID
   * @param {FormData} formData - 包含avatar的表单数据
   */
  uploadMerchantAvatar(merchantId, formData) {
    return api.post(`/v1/merchant/${merchantId}/avatar`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 获取商家营业概览
   * @param {string} merchantId - 商家ID
   */
  getBusinessOverview(merchantId) {
    return api.get(`/v1/merchant/${merchantId}/business-overview`)
  },

  /**
   * 获取商家统计数据
   * @param {string} merchantId - 商家ID
   * @param {string} timeRange - 时间范围（today/yesterday/week/month）
   */
  getStatistics(merchantId, timeRange) {
    return api.get(`/v1/merchant/${merchantId}/statistics`, {
      params: { timeRange }
    })
  },

  /**
   * 获取商家公告列表
   * @param {string} merchantId - 商家ID
   */
  getAnnouncements(merchantId) {
    return api.get(`/v1/merchant/${merchantId}/announcements`)
  },

  /**
   * 添加商家公告
   * @param {string} merchantId - 商家ID
   * @param {Object} announcement - 公告数据
   */
  addAnnouncement(merchantId, announcement) {
    return api.post(`/v1/merchant/${merchantId}/announcements`, announcement)
  },

  /**
   * 更新商家公告
   * @param {string} merchantId - 商家ID
   * @param {string} announcementId - 公告ID
   * @param {Object} announcement - 公告数据
   */
  updateAnnouncement(merchantId, announcementId, announcement) {
    return api.put(`/v1/merchant/${merchantId}/announcements/${announcementId}`, announcement)
  },

  /**
   * 切换商家公告状态
   * @param {string} merchantId - 商家ID
   * @param {string} announcementId - 公告ID
   * @param {string} status - 状态
   */
  toggleAnnouncementStatus(merchantId, announcementId, status) {
    return api.put(`/v1/merchant/${merchantId}/announcements/${announcementId}/status`, { status })
  },

  /**
   * 删除商家公告
   * @param {string} merchantId - 商家ID
   * @param {string} announcementId - 公告ID
   */
  deleteAnnouncement(merchantId, announcementId) {
    return api.delete(`/v1/merchant/${merchantId}/announcements/${announcementId}`)
  }
}
