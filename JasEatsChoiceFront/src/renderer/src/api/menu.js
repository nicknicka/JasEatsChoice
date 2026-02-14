/**
 * 菜单相关API
 */
import api from '../utils/api'

export default {
  /**
   * 获取商家的菜单列表
   * @param {string} merchantId - 商家ID
   */
  getMerchantMenus(merchantId) {
    return api.get(`/v1/menus/merchants/${merchantId}/menu`)
  },

  /**
   * 获取指定菜单的菜品列表
   * @param {string} merchantId - 商家ID
   * @param {string} menuId - 菜单ID
   */
  getMenuDishes(merchantId, menuId) {
    return api.get(`/v1/menus/merchants/${merchantId}/menu/${menuId}/dishes`)
  }
}
