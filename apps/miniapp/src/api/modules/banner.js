/**
 * 轮播图相关API
 * 对接后端 BannerController
 * 基础路径: /v1/banners
 */
import { get, post, put, del } from '@/utils/request'
import { BANNER_API, buildUrl } from '../urlEnum'

export const bannerApi = {
  /**
   * 获取轮播图列表
   * GET /v1/banners
   * @param {Object} params - 查询参数
   * @param {string} params.position - 位置(home/merchant/dish等)
   * @param {number} params.status - 状态(1-启用,0-禁用)
   * @returns {Promise} 返回轮播图列表
   */
  getList: (params) => get(BANNER_API.GET_LIST, params),

  /**
   * 获取轮播图详情
   * GET /v1/banners/{bannerId}
   * @param {string} bannerId - 轮播图ID
   * @returns {Promise} 返回轮播图详情
   */
  getDetail: (bannerId) => get(buildUrl(BANNER_API.GET_DETAIL, { bannerId })),

  /**
   * 创建轮播图
   * POST /v1/banners
   * @param {Object} data - 轮播图数据
   * @param {string} data.title - 标题
   * @param {string} data.imageUrl - 图片URL
   * @param {string} data.type - 类型(link/dish/merchant/activity)
   * @param {string} data.targetType - 跳转目标类型
   * @param {string} data.targetId - 跳转目标ID
   * @param {string} data.link - 外部链接
   * @param {string} data.position - 位置
   * @param {number} data.sortOrder - 排序
   * @param {number} data.status - 状态(1-启用,0-禁用)
   * @returns {Promise} 返回创建结果
   */
  create: (data) => post(BANNER_API.CREATE, data),

  /**
   * 更新轮播图
   * PUT /v1/banners/{bannerId}
   * @param {string} bannerId - 轮播图ID
   * @param {Object} data - 轮播图数据
   * @returns {Promise} 返回更新结果
   */
  update: (bannerId, data) => put(buildUrl(BANNER_API.UPDATE, { bannerId }), data),

  /**
   * 删除轮播图
   * DELETE /v1/banners/{bannerId}
   * @param {string} bannerId - 轮播图ID
   * @returns {Promise} 返回删除结果
   */
  delete: (bannerId) => del(buildUrl(BANNER_API.DELETE, { bannerId })),

  /**
   * 更新轮播图状态
   * PUT /v1/banners/{bannerId}/status
   * @param {string} bannerId - 轮播图ID
   * @param {number} status - 状态(1-启用,0-禁用)
   * @returns {Promise} 返回更新结果
   */
  updateStatus: (bannerId, status) => put(buildUrl(BANNER_API.UPDATE_STATUS, { bannerId }), { status })
}

export default bannerApi
