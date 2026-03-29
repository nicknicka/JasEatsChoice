/**
 * 购物车相关API
 * 对接后端购物车功能
 * 基础路径: /api/cart 或 /v1/cart
 */
import { get, post, put, del } from '@/utils/request'
import { CART_API, buildUrl } from '../urlEnum'

export const cartApi = {
  /**
   * 获取购物车列表
   * GET /v1/cart/{userId}
   * @param {string} userId - 用户ID
   * @returns {Promise} 返回购物车列表
   */
  getCart: (userId) => get(buildUrl(CART_API.GET_CART, { userId })),

  /**
   * 获取购物车列表（别名）
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @returns {Promise} 返回购物车列表
   */
  getList: (params) => get('/api/cart', params),

  /**
   * 添加到购物车
   * POST /v1/cart/items
   * @param {Object} data - 购物车数据
   * @param {string} data.dishId - 菜品ID
   * @param {number} data.quantity - 数量
   * @param {Array} data.optionalIngredients - 可选食材
   * @param {string} data.spec - 规格
   * @param {string} data.remark - 备注
   * @returns {Promise} 返回添加结果
   */
  add: (data) => post(CART_API.ADD_ITEM, data),

  /**
   * 添加到购物车（别名）
   * @param {Object} data - 购物车数据
   * @returns {Promise} 返回添加结果
   */
  addItem: (data) => post(CART_API.ADD_ITEM, data),

  /**
   * 更新购物车项
   * PUT /v1/cart/items/{itemId}
   * @param {string} cartId - 购物车项ID
   * @param {Object} data - 更新数据
   * @param {number} data.quantity - 数量
   * @param {string} data.spec - 规格
   * @param {string} data.remark - 备注
   * @returns {Promise} 返回更新结果
   */
  update: (cartId, data) => put(buildUrl(CART_API.UPDATE_ITEM, { itemId: cartId }), data),

  /**
   * 删除购物车项
   * DELETE /v1/cart/items/{itemId}
   * @param {string} cartId - 购物车项ID
   * @returns {Promise} 返回删除结果
   */
  delete: (cartId) => del(buildUrl(CART_API.DELETE_ITEM, { itemId: cartId })),

  /**
   * 批量删除购物车项
   * POST /api/cart/batch-delete
   * @param {Object} data - 数据
   * @param {Array} data.cartIds - 购物车项ID数组
   * @returns {Promise} 返回删除结果
   */
  batchDelete: (data) => post('/api/cart/batch-delete', data),

  /**
   * 清空购物车
   * DELETE /v1/cart/{userId}/clear
   * @param {string} userId - 用户ID
   * @param {string} merchantId - 商家ID（可选，不传则清空全部）
   * @returns {Promise} 返回清空结果
   */
  clearCart: (userId, merchantId) => {
    const url = buildUrl(CART_API.CLEAR_CART, { userId })
    return del(url, merchantId ? { merchantId } : {})
  },

  /**
   * 清空购物车（别名）
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.merchantId - 商家ID（可选）
   * @returns {Promise} 返回清空结果
   */
  clear: (data) => post('/api/cart/clear', data),

  /**
   * 选中/取消选中购物车项
   * PUT /api/cart/{cartId}/select
   * @param {string} cartId - 购物车项ID
   * @param {Object} data - 数据
   * @param {boolean} data.selected - 是否选中
   * @returns {Promise} 返回更新结果
   */
  select: (cartId, data) => put(`/api/cart/${cartId}/select`, data),

  /**
   * 全选/取消全选
   * PUT /api/cart/select-all
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.merchantId - 商家ID（可选）
   * @param {boolean} data.selected - 是否选中
   * @returns {Promise} 返回更新结果
   */
  selectAll: (data) => put('/api/cart/select-all', data)
}

export default cartApi
