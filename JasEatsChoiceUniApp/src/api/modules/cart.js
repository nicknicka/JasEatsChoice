/**
 * 购物车相关API
 * 对接后端购物车功能
 * 基础路径: /api/cart 或 /v1/cart
 */
import { get, post, put, del } from '@/utils/request'

export const cartApi = {
  /**
   * 获取购物车列表
   * GET /api/cart
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   */
  getList: (params) => get('/api/cart', params),

  /**
   * 添加到购物车
   * POST /api/cart/add
   * @param {Object} data - 购物车数据
   * @param {string} data.dishId - 菜品ID
   * @param {number} data.quantity - 数量
   * @param {Array} data.optionalIngredients - 可选食材
   * @param {string} data.spec - 规格
   * @param {string} data.remark - 备注
   */
  add: (data) => post('/api/cart/add', data),

  /**
   * 更新购物车项
   * PUT /api/cart/{cartId}
   * @param {string} cartId - 购物车项ID
   * @param {Object} data - 更新数据
   * @param {number} data.quantity - 数量
   * @param {string} data.spec - 规格
   * @param {string} data.remark - 备注
   */
  update: (cartId, data) => put(`/api/cart/${cartId}`, data),

  /**
   * 删除购物车项
   * DELETE /api/cart/{cartId}
   * @param {string} cartId - 购物车项ID
   */
  delete: (cartId) => del(`/api/cart/${cartId}`),

  /**
   * 批量删除购物车项
   * POST /api/cart/batch-delete
   * @param {Object} data - 数据
   * @param {Array} data.cartIds - 购物车项ID数组
   */
  batchDelete: (data) => post('/api/cart/batch-delete', data),

  /**
   * 清空购物车
   * POST /api/cart/clear
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.merchantId - 商家ID（可选，不传则清空全部）
   */
  clear: (data) => post('/api/cart/clear', data),

  /**
   * 选中/取消选中购物车项
   * PUT /api/cart/{cartId}/select
   * @param {string} cartId - 购物车项ID
   * @param {Object} data - 数据
   * @param {boolean} data.selected - 是否选中
   */
  select: (cartId, data) => put(`/api/cart/${cartId}/select`, data),

  /**
   * 全选/取消全选
   * PUT /api/cart/select-all
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.merchantId - 商家ID（可选）
   * @param {boolean} data.selected - 是否选中
   */
  selectAll: (data) => put('/api/cart/select-all', data)
}

export default cartApi
