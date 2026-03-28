/**
 * 轮播图相关API
 * 基础路径: /v1/banners
 */
import { get, post, put, del } from '@/utils/request'

export const bannerApi = {
  /**
   * 获取轮播图列表
   * GET /v1/banners
   * @param {Object} params - 查询参数
   * @param {string} params.position - 位置(home/merchant/dish等)
   * @param {number} params.status - 状态(1-启用,0-禁用)
   */
  getList: (params) => get('/v1/banners', params),

  /**
   * 获取轮播图详情
   * GET /v1/banners/{bannerId}
   * @param {string} bannerId - 轮播图ID
   */
  getDetail: (bannerId) => get(`/v1/banners/${bannerId}`),

  /**
   * 创建轮播图
   * POST /v1/banners
   * @param {Object} data - 轮播图数据
   */
  create: (data) => post('/v1/banners', data),

  /**
   * 更新轮播图
   * PUT /v1/banners/{bannerId}
   * @param {string} bannerId - 轮播图ID
   * @param {Object} data - 轮播图数据
   */
  update: (bannerId, data) => put(`/v1/banners/${bannerId}`, data),

  /**
   * 删除轮播图
   * DELETE /v1/banners/{bannerId}
   * @param {string} bannerId - 轮播图ID
   */
  delete: (bannerId) => del(`/v1/banners/${bannerId}`),

  /**
   * 更新轮播图状态
   * PUT /v1/banners/{bannerId}/status
   * @param {string} bannerId - 轮播图ID
   * @param {number} status - 状态(1-启用,0-禁用)
   */
  updateStatus: (bannerId, status) => put(`/v1/banners/${bannerId}/status`, { status })
}

export default bannerApi
