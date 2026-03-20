import { get, post, put, del } from '../request'

/**
 * 菜单管理 API
 */
export const menuApi = {
  /**
   * MENU-001: 获取商家菜单列表
   */
  getList: (merchantId) => get(`/v1/menus/merchants/${merchantId}/menu`),

  /**
   * MENU-002: 获取菜单详情
   */
  getDetail: (menuId) => get(`/v1/menus/${menuId}`),

  /**
   * MENU-003: 获取菜单下的菜品列表
   */
  getMenuDishes: (merchantId, menuId) => get(`/v1/menus/merchants/${merchantId}/menu/${menuId}/dishes`),

  /**
   * MENU-002: 更新菜品在菜单中的排序
   */
  updateDishSort: (menuId, dishId, sortOrder) => put(`/v1/menus/menu/${menuId}/dishes/${dishId}/sort`, { sortOrder }),

  /**
   * MENU-004: 更新菜品在菜单中的状态
   */
  updateDishStatus: (menuId, dishId, status) => put(`/v1/menus/menu/${menuId}/dishes/${dishId}/status`, { status }),

  /**
   * MENU-005: 批量更新菜品在多个菜单中的状态
   */
  batchUpdateDishStatus: (dishId, menuIds, status) => put(`/v1/menus/dishes/${dishId}/status`, { menuIds, status }),

  /**
   * MENU-006: 获取菜品关联的所有菜单
   */
  getDishMenus: (dishId) => get(`/v1/menus/dishes/${dishId}/menus`),

  /**
   * MENU-007: 设置菜单自动上下架时间
   */
  setMenuSchedule: (menuId, autoStartTime, autoEndTime) => put(`/v1/menus/menu/${menuId}/schedule`, {
    autoStartTime,
    autoEndTime
  }),

  /**
   * MENU-008: 菜单批量操作
   */
  batchOperate: (menuIds, action) => post('/v1/menus/menu/batch', { menuIds, action }),

  /**
   * MENU-009: 创建新菜单
   */
  create: (merchantId, data) => post(`/v1/menus/merchants/${merchantId}/menu`, data),

  /**
   * MENU-010: 更新菜单
   */
  update: (merchantId, menuId, data) => put(`/v1/menus/merchants/${merchantId}/menu/${menuId}`, data)
}
