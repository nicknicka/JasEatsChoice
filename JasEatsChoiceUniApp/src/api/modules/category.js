/**
 * 分类管理API
 * 对接后端 CategoryController
 */
import { get } from '@/utils/request'

export const categoryApi = {
  /**
   * 获取常用品类列表
   * 用于首页快捷分类展示
   * @returns {Promise} 常用品类列表
   */
  getCommon() {
    return get('/v1/category/common')
  },

  /**
   * 获取所有品类列表
   * 用于分类选择页面
   * @returns {Promise} 所有品类列表
   */
  getList() {
    return get('/v1/category/list')
  }
}
