/**
 * 管理员API服务
 * 统一管理所有管理员相关的API调用
 */

import api from '../utils/api'
import API, { buildUrl } from './index'

// ==================== 管理员认证 ====================

/**
 * 管理员登录
 * @param {string} username - 用户名
 * @param {string} password - 密码
 * @returns {Promise} 登录结果
 */
export function adminLogin(username, password) {
  console.log('[管理员API] 尝试登录:', username)
  return api
    .post(API.ADMIN.LOGIN, { username, password })
    .then((response) => {
      console.log('[管理员API] 登录成功:', response)
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 登录失败:', error)
      throw error
    })
}

/**
 * 获取当前管理员信息
 * @returns {Promise} 管理员信息
 */
export function getCurrentAdmin() {
  console.log('[管理员API] 获取当前管理员信息')
  return api
    .get(API.ADMIN.CURRENT)
    .then((response) => {
      console.log('[管理员API] 获取管理员信息成功:', response)
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取管理员信息失败:', error)
      throw error
    })
}

/**
 * 获取管理员列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 管理员列表
 */
export function getAdminList(params = {}) {
  console.log('[管理员API] 获取管理员列表:', params)
  return api
    .get(API.ADMIN.LIST, { params })
    .then((response) => {
      console.log('[管理员API] 获取管理员列表成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取管理员列表失败:', error)
      throw error
    })
}

/**
 * 创建管理员
 * @param {Object} adminData - 管理员数据
 * @returns {Promise} 创建结果
 */
export function createAdmin(adminData) {
  console.log('[管理员API] 创建管理员:', adminData.username)
  return api
    .post(API.ADMIN.CREATE, adminData)
    .then((response) => {
      console.log('[管理员API] 创建管理员成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 创建管理员失败:', error)
      throw error
    })
}

/**
 * 修改管理员状态
 * @param {number} adminId - 管理员ID
 * @param {string} status - 状态
 * @returns {Promise} 修改结果
 */
export function updateAdminStatus(adminId, status) {
  console.log('[管理员API] 修改管理员状态:', adminId, status)
  return api
    .put(buildUrl(API.ADMIN.UPDATE_STATUS, { adminId }), { status })
    .then((response) => {
      console.log('[管理员API] 修改管理员状态成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 修改管理员状态失败:', error)
      throw error
    })
}

/**
 * 重置管理员密码
 * @param {number} adminId - 管理员ID
 * @param {string} password - 新密码
 * @returns {Promise} 重置结果
 */
export function resetAdminPassword(adminId, password) {
  console.log('[管理员API] 重置管理员密码:', adminId)
  return api
    .put(buildUrl(API.ADMIN.RESET_PASSWORD, { adminId }), { password })
    .then((response) => {
      console.log('[管理员API] 重置管理员密码成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 重置管理员密码失败:', error)
      throw error
    })
}

/**
 * 更新个人信息
 * @param {Object} profileData - 个人信息数据
 * @returns {Promise} 更新结果
 */
export function updateAdminProfile(profileData) {
  console.log('[管理员API] 更新个人信息:', profileData)
  return api
    .put('/admin/profile', profileData)
    .then((response) => {
      console.log('[管理员API] 更新个人信息成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 更新个人信息失败:', error)
      throw error
    })
}

/**
 * 修改密码
 * @param {Object} passwordData - 密码数据
 * @returns {Promise} 修改结果
 */
export function changeAdminPassword(passwordData) {
  console.log('[管理员API] 修改密码')
  return api
    .put('/admin/password', passwordData)
    .then((response) => {
      console.log('[管理员API] 修改密码成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 修改密码失败:', error)
      throw error
    })
}

// ==================== 统计数据 ====================

/**
 * 获取控制台统计数据
 * @returns {Promise} 统计数据
 */
export function getDashboardStats() {
  console.log('[管理员API] 获取控制台统计数据')
  return api
    .get(API.ADMIN.DASHBOARD)
    .then((response) => {
      console.log('[管理员API] 获取控制台统计数据成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取控制台统计数据失败:', error)
      throw error
    })
}

/**
 * 获取用户统计数据
 * @param {number} days - 天数
 * @returns {Promise} 用户统计数据
 */
export function getUserStats(days = 7) {
  console.log('[管理员API] 获取用户统计数据, 天数:', days)
  return api
    .get(API.ADMIN.USER_STATS, { params: { days } })
    .then((response) => {
      console.log('[管理员API] 获取用户统计数据成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取用户统计数据失败:', error)
      throw error
    })
}

/**
 * 获取订单统计数据
 * @param {number} days - 天数
 * @returns {Promise} 订单统计数据
 */
export function getOrderStats(days = 7) {
  console.log('[管理员API] 获取订单统计数据, 天数:', days)
  return api
    .get(API.ADMIN.ORDER_STATS, { params: { days } })
    .then((response) => {
      console.log('[管理员API] 获取订单统计数据成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取订单统计数据失败:', error)
      throw error
    })
}

/**
 * 获取收入统计数据
 * @param {number} days - 天数
 * @returns {Promise} 收入统计数据
 */
export function getRevenueStats(days = 7) {
  console.log('[管理员API] 获取收入统计数据, 天数:', days)
  return api
    .get(API.ADMIN.REVENUE_STATS, { params: { days } })
    .then((response) => {
      console.log('[管理员API] 获取收入统计数据成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取收入统计数据失败:', error)
      throw error
    })
}

// ==================== 用户管理 ====================

/**
 * 获取用户列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 用户列表
 */
export function getUserList(params = {}) {
  console.log('[管理员API] 获取用户列表:', params)
  return api
    .get(API.ADMIN.USER_LIST, { params })
    .then((response) => {
      console.log('[管理员API] 获取用户列表成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取用户列表失败:', error)
      throw error
    })
}

/**
 * 获取用户详情
 * @param {number} userId - 用户ID
 * @returns {Promise} 用户详情
 */
export function getUserDetail(userId) {
  console.log('[管理员API] 获取用户详情:', userId)
  return api
    .get(buildUrl(API.ADMIN.USER_DETAIL, { userId }))
    .then((response) => {
      console.log('[管理员API] 获取用户详情成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取用户详情失败:', error)
      throw error
    })
}

/**
 * 修改用户状态
 * @param {number} userId - 用户ID
 * @param {string} status - 状态
 * @returns {Promise} 修改结果
 */
export function updateUserStatus(userId, status) {
  console.log('[管理员API] 修改用户状态:', userId, status)
  return api
    .put(buildUrl(API.ADMIN.UPDATE_USER_STATUS, { userId }), { status })
    .then((response) => {
      console.log('[管理员API] 修改用户状态成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 修改用户状态失败:', error)
      throw error
    })
}

/**
 * 删除用户
 * @param {number} userId - 用户ID
 * @returns {Promise} 删除结果
 */
export function deleteUser(userId) {
  console.log('[管理员API] 删除用户:', userId)
  return api
    .delete(buildUrl(API.ADMIN.DELETE_USER, { userId }))
    .then((response) => {
      console.log('[管理员API] 删除用户成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 删除用户失败:', error)
      throw error
    })
}

// ==================== 商家管理 ====================

/**
 * 获取商家列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 商家列表
 */
export function getMerchantList(params = {}) {
  console.log('[管理员API] 获取商家列表:', params)
  return api
    .get(API.ADMIN.MERCHANT_LIST, { params })
    .then((response) => {
      console.log('[管理员API] 获取商家列表成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取商家列表失败:', error)
      throw error
    })
}

/**
 * 获取商家详情
 * @param {number} merchantId - 商家ID
 * @returns {Promise} 商家详情
 */
export function getMerchantDetail(merchantId) {
  console.log('[管理员API] 获取商家详情:', merchantId)
  return api
    .get(buildUrl(API.ADMIN.MERCHANT_DETAIL, { merchantId }))
    .then((response) => {
      console.log('[管理员API] 获取商家详情成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取商家详情失败:', error)
      throw error
    })
}

/**
 * 审核商家
 * @param {number} merchantId - 商家ID
 * @param {Object} auditData - 审核数据
 * @returns {Promise} 审核结果
 */
export function auditMerchant(merchantId, auditData) {
  console.log('[管理员API] 审核商家:', merchantId, auditData)
  return api
    .post(buildUrl(API.ADMIN.AUDIT_MERCHANT, { merchantId }), auditData)
    .then((response) => {
      console.log('[管理员API] 审核商家成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 审核商家失败:', error)
      throw error
    })
}

/**
 * 修改商家状态
 * @param {number} merchantId - 商家ID
 * @param {string} status - 状态
 * @returns {Promise} 修改结果
 */
export function updateMerchantStatus(merchantId, status) {
  console.log('[管理员API] 修改商家状态:', merchantId, status)
  return api
    .put(buildUrl(API.ADMIN.UPDATE_MERCHANT_STATUS, { merchantId }), { status })
    .then((response) => {
      console.log('[管理员API] 修改商家状态成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 修改商家状态失败:', error)
      throw error
    })
}

// ==================== 订单管理 ====================

/**
 * 获取订单列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 订单列表
 */
export function getOrderList(params = {}) {
  console.log('[管理员API] 获取订单列表:', params)
  return api
    .get(API.ADMIN.ORDER_LIST, { params })
    .then((response) => {
      console.log('[管理员API] 获取订单列表成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取订单列表失败:', error)
      throw error
    })
}

/**
 * 获取订单详情
 * @param {number} orderId - 订单ID
 * @returns {Promise} 订单详情
 */
export function getOrderDetail(orderId) {
  console.log('[管理员API] 获取订单详情:', orderId)
  return api
    .get(buildUrl(API.ADMIN.ORDER_DETAIL, { orderId }))
    .then((response) => {
      console.log('[管理员API] 获取订单详情成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取订单详情失败:', error)
      throw error
    })
}

// ==================== 菜品管理 ====================

/**
 * 获取菜品列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 菜品列表
 */
export function getDishList(params = {}) {
  console.log('[管理员API] 获取菜品列表:', params)
  return api
    .get(API.ADMIN.DISH_LIST, { params })
    .then((response) => {
      console.log('[管理员API] 获取菜品列表成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取菜品列表失败:', error)
      throw error
    })
}

/**
 * 获取菜品详情
 * @param {number} dishId - 菜品ID
 * @returns {Promise} 菜品详情
 */
export function getDishDetail(dishId) {
  console.log('[管理员API] 获取菜品详情:', dishId)
  return api
    .get(buildUrl(API.ADMIN.DISH_DETAIL, { dishId }))
    .then((response) => {
      console.log('[管理员API] 获取菜品详情成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取菜品详情失败:', error)
      throw error
    })
}

/**
 * 审核菜品
 * @param {number} dishId - 菜品ID
 * @param {Object} auditData - 审核数据
 * @returns {Promise} 审核结果
 */
export function auditDish(dishId, auditData) {
  console.log('[管理员API] 审核菜品:', dishId, auditData)
  return api
    .post(buildUrl(API.ADMIN.AUDIT_DISH, { dishId }), auditData)
    .then((response) => {
      console.log('[管理员API] 审核菜品成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 审核菜品失败:', error)
      throw error
    })
}

/**
 * 获取菜品审核列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 菜品审核列表
 */
export function getDishAuditList(params = {}) {
  console.log('[管理员API] 获取菜品审核列表:', params)
  return api
    .get(API.ADMIN.DISH_AUDIT_LIST, { params })
    .then((response) => {
      console.log('[管理员API] 获取菜品审核列表成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取菜品审核列表失败:', error)
      throw error
    })
}

/**
 * 获取菜品审核详情
 * @param {number} dishId - 菜品ID
 * @returns {Promise} 菜品审核详情
 */
export function getDishAuditDetail(dishId) {
  console.log('[管理员API] 获取菜品审核详情:', dishId)
  return api
    .get(buildUrl(API.ADMIN.DISH_AUDIT_DETAIL, { dishId }))
    .then((response) => {
      console.log('[管理员API] 获取菜品审核详情成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取菜品审核详情失败:', error)
      throw error
    })
}

/**
 * 修改菜品状态
 * @param {number} dishId - 菜品ID
 * @param {string} status - 状态
 * @returns {Promise} 修改结果
 */
export function updateDishStatus(dishId, status) {
  console.log('[管理员API] 修改菜品状态:', dishId, status)
  return api
    .put(buildUrl(API.ADMIN.UPDATE_DISH_STATUS, { dishId }), { status })
    .then((response) => {
      console.log('[管理员API] 修改菜品状态成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 修改菜品状态失败:', error)
      throw error
    })
}

// ==================== 财务管理 ====================

// 旧函数已删除，使用下方的 getWithdrawList 替代

/**
 * 获取充值记录列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 充值记录列表
 */
export function getRechargeList(params = {}) {
  console.log('[管理员API] 获取充值记录列表:', params)
  return api
    .get(API.ADMIN.RECHARGE_LIST, { params })
    .then((response) => {
      console.log('[管理员API] 获取充值记录列表成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取充值记录列表失败:', error)
      throw error
    })
}

/**
 * 获取充值统计
 * @returns {Promise} 充值统计数据
 */
export function getRechargeStats() {
  console.log('[管理员API] 获取充值统计')
  return api
    .get(API.ADMIN.RECHARGE_STATS)
    .then((response) => {
      console.log('[管理员API] 获取充值统计成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取充值统计失败:', error)
      throw error
    })
}

/**
 * 获取充值详情
 * @param {string} rechargeId - 充值ID
 * @returns {Promise} 充值详情
 */
export function getRechargeDetail(rechargeId) {
  console.log('[管理员API] 获取充值详情:', rechargeId)
  return api
    .get(buildUrl(API.ADMIN.RECHARGE_DETAIL, { rechargeId }))
    .then((response) => {
      console.log('[管理员API] 获取充值详情成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取充值详情失败:', error)
      throw error
    })
}

/**
 * 获取退款记录列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 退款记录列表
 */
export function getRefundList(params = {}) {
  console.log('[管理员API] 获取退款记录列表:', params)
  return api
    .get(API.ADMIN.REFUND_LIST, { params })
    .then((response) => {
      console.log('[管理员API] 获取退款记录列表成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取退款记录列表失败:', error)
      throw error
    })
}

/**
 * 获取退款统计
 * @returns {Promise} 退款统计数据
 */
export function getRefundStats() {
  console.log('[管理员API] 获取退款统计')
  return api
    .get(API.ADMIN.REFUND_STATS)
    .then((response) => {
      console.log('[管理员API] 获取退款统计成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取退款统计失败:', error)
      throw error
    })
}

/**
 * 获取退款详情
 * @param {string} refundId - 退款ID
 * @returns {Promise} 退款详情
 */
export function getRefundDetail(refundId) {
  console.log('[管理员API] 获取退款详情:', refundId)
  return api
    .get(buildUrl(API.ADMIN.REFUND_DETAIL, { refundId }))
    .then((response) => {
      console.log('[管理员API] 获取退款详情成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取退款详情失败:', error)
      throw error
    })
}

// ==================== 系统日志 ====================

/**
 * 获取操作日志
 * @param {Object} params - 查询参数
 * @returns {Promise} 操作日志列表
 */
export function getOperationLogs(params = {}) {
  console.log('[管理员API] 获取操作日志:', params)
  return api
    .get(API.ADMIN.OPERATION_LOGS, { params })
    .then((response) => {
      console.log('[管理员API] 获取操作日志成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取操作日志失败:', error)
      throw error
    })
}

/**
 * 获取系统日志
 * @param {Object} params - 查询参数
 * @returns {Promise} 系统日志列表
 */
export function getSystemLogs(params = {}) {
  console.log('[管理员API] 获取系统日志:', params)
  return api
    .get(API.ADMIN.SYSTEM_LOGS, { params })
    .then((response) => {
      console.log('[管理员API] 获取系统日志成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取系统日志失败:', error)
      throw error
    })
}

/**
 * 获取登录日志
 * @param {Object} params - 查询参数
 * @returns {Promise} 登录日志列表
 */
export function getLoginLogs(params = {}) {
  console.log('[管理员API] 获取登录日志:', params)
  return api
    .get(API.ADMIN.LOGIN_LOGS, { params })
    .then((response) => {
      console.log('[管理员API] 获取登录日志成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取登录日志失败:', error)
      throw error
    })
}

// ==================== 用户管理（新增） ====================

/**
 * 编辑用户信息
 * @param {string} userId - 用户ID
 * @param {Object} userData - 用户数据
 * @returns {Promise} 编辑结果
 */
export function updateUser(userId, userData) {
  console.log('[管理员API] 编辑用户:', userId, userData)
  return api
    .put(`/admin/users/${userId}`, userData)
    .then((response) => {
      console.log('[管理员API] 编辑用户成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 编辑用户失败:', error)
      throw error
    })
}

// ==================== 订单管理（新增） ====================

/**
 * 修改订单状态
 * @param {string} orderId - 订单ID
 * @param {Object} statusData - 状态数据
 * @returns {Promise} 修改结果
 */
export function updateOrderStatus(orderId, statusData) {
  console.log('[管理员API] 修改订单状态:', orderId, statusData)
  return api
    .put(buildUrl(API.ADMIN.ORDER_UPDATE_STATUS, { orderId }), statusData)
    .then((response) => {
      console.log('[管理员API] 修改订单状态成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 修改订单状态失败:', error)
      throw error
    })
}

/**
 * 批量修改订单状态
 * @param {Object} batchData - 批量数据 {orderIds: [], status: number, reason: string}
 * @returns {Promise} 批量修改结果
 */
export function batchUpdateOrderStatus(batchData) {
  console.log('[管理员API] 批量修改订单状态:', batchData)
  return api
    .put('/admin/orders/batch/status', batchData)
    .then((response) => {
      console.log('[管理员API] 批量修改订单状态成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 批量修改订单状态失败:', error)
      throw error
    })
}

/**
 * 获取订单统计
 * @returns {Promise} 订单统计数据
 */
export function getOrderStatistics() {
  console.log('[管理员API] 获取订单统计')
  return api
    .get('/admin/orders/statistics')
    .then((response) => {
      console.log('[管理员API] 获取订单统计成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取订单统计失败:', error)
      throw error
    })
}

// ==================== 系统日志（新增） ====================

/**
 * 获取系统日志列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 日志列表
 */
export function getLogList(params = {}) {
  console.log('[管理员API] 获取系统日志列表:', params)
  return api
    .get('/admin/system/logs', { params })
    .then((response) => {
      console.log('[管理员API] 获取系统日志列表成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取系统日志列表失败:', error)
      throw error
    })
}

/**
 * 获取日志统计
 * @returns {Promise} 日志统计数据
 */
export function getLogStatistics() {
  console.log('[管理员API] 获取日志统计')
  return api
    .get('/admin/system/logs/statistics')
    .then((response) => {
      console.log('[管理员API] 获取日志统计成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取日志统计失败:', error)
      throw error
    })
}

/**
 * 清理过期日志
 * @param {number} days - 保留天数
 * @returns {Promise} 清理结果
 */
export function cleanExpiredLogs(days = 90) {
  console.log('[管理员API] 清理过期日志, 天数:', days)
  return api
    .delete('/admin/system/logs/clean', { params: { days } })
    .then((response) => {
      console.log('[管理员API] 清理过期日志成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 清理过期日志失败:', error)
      throw error
    })
}

// ==================== 角色管理（新增） ====================

/**
 * 获取角色列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 角色列表
 */
export function getRoleList(params = {}) {
  console.log('[管理员API] 获取角色列表:', params)
  return api
    .get('/admin/roles', { params })
    .then((response) => {
      console.log('[管理员API] 获取角色列表成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取角色列表失败:', error)
      throw error
    })
}

/**
 * 获取所有角色（不分页）
 * @returns {Promise} 角色列表
 */
export function getAllRoles() {
  console.log('[管理员API] 获取所有角色')
  return api
    .get('/admin/roles/all')
    .then((response) => {
      console.log('[管理员API] 获取所有角色成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取所有角色失败:', error)
      throw error
    })
}

/**
 * 获取角色详情
 * @param {number} roleId - 角色ID
 * @returns {Promise} 角色详情
 */
export function getRoleDetail(roleId) {
  console.log('[管理员API] 获取角色详情:', roleId)
  return api
    .get(`/admin/roles/${roleId}`)
    .then((response) => {
      console.log('[管理员API] 获取角色详情成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取角色详情失败:', error)
      throw error
    })
}

/**
 * 创建角色
 * @param {Object} roleData - 角色数据
 * @returns {Promise} 创建结果
 */
export function createRole(roleData) {
  console.log('[管理员API] 创建角色:', roleData)
  return api
    .post('/admin/roles', roleData)
    .then((response) => {
      console.log('[管理员API] 创建角色成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 创建角色失败:', error)
      throw error
    })
}

/**
 * 更新角色
 * @param {number} roleId - 角色ID
 * @param {Object} roleData - 角色数据
 * @returns {Promise} 更新结果
 */
export function updateRole(roleId, roleData) {
  console.log('[管理员API] 更新角色:', roleId, roleData)
  return api
    .put(`/admin/roles/${roleId}`, roleData)
    .then((response) => {
      console.log('[管理员API] 更新角色成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 更新角色失败:', error)
      throw error
    })
}

/**
 * 删除角色
 * @param {number} roleId - 角色ID
 * @returns {Promise} 删除结果
 */
export function deleteRole(roleId) {
  console.log('[管理员API] 删除角色:', roleId)
  return api
    .delete(`/admin/roles/${roleId}`)
    .then((response) => {
      console.log('[管理员API] 删除角色成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 删除角色失败:', error)
      throw error
    })
}

/**
 * 为角色分配权限
 * @param {number} roleId - 角色ID
 * @param {Object} permissionData - 权限数据 {permissionIds: []}
 * @returns {Promise} 分配结果
 */
export function assignRolePermissions(roleId, permissionData) {
  console.log('[管理员API] 为角色分配权限:', roleId, permissionData)
  return api
    .post(`/admin/roles/${roleId}/permissions`, permissionData)
    .then((response) => {
      console.log('[管理员API] 为角色分配权限成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 为角色分配权限失败:', error)
      throw error
    })
}

/**
 * 获取角色的权限列表
 * @param {number} roleId - 角色ID
 * @returns {Promise} 权限列表
 */
export function getRolePermissions(roleId) {
  console.log('[管理员API] 获取角色权限:', roleId)
  return api
    .get(`/admin/roles/${roleId}/permissions`)
    .then((response) => {
      console.log('[管理员API] 获取角色权限成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取角色权限失败:', error)
      throw error
    })
}

// ==================== 权限管理（新增） ====================

/**
 * 获取权限列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 权限列表
 */
export function getPermissionList(params = {}) {
  console.log('[管理员API] 获取权限列表:', params)
  return api
    .get('/admin/permissions', { params })
    .then((response) => {
      console.log('[管理员API] 获取权限列表成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取权限列表失败:', error)
      throw error
    })
}

/**
 * 获取权限树
 * @returns {Promise} 权限树
 */
export function getPermissionTree() {
  console.log('[管理员API] 获取权限树')
  return api
    .get('/admin/permissions/tree')
    .then((response) => {
      console.log('[管理员API] 获取权限树成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取权限树失败:', error)
      throw error
    })
}

/**
 * 获取顶级权限
 * @returns {Promise} 顶级权限列表
 */
export function getTopLevelPermissions() {
  console.log('[管理员API] 获取顶级权限')
  return api
    .get('/admin/permissions/top')
    .then((response) => {
      console.log('[管理员API] 获取顶级权限成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取顶级权限失败:', error)
      throw error
    })
}

/**
 * 获取子权限列表
 * @param {number} parentId - 父级ID
 * @returns {Promise} 子权限列表
 */
export function getChildPermissions(parentId) {
  console.log('[管理员API] 获取子权限:', parentId)
  return api
    .get(`/admin/permissions/children/${parentId}`)
    .then((response) => {
      console.log('[管理员API] 获取子权限成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取子权限失败:', error)
      throw error
    })
}

/**
 * 获取权限详情
 * @param {number} permissionId - 权限ID
 * @returns {Promise} 权限详情
 */
export function getPermissionDetail(permissionId) {
  console.log('[管理员API] 获取权限详情:', permissionId)
  return api
    .get(`/admin/permissions/${permissionId}`)
    .then((response) => {
      console.log('[管理员API] 获取权限详情成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取权限详情失败:', error)
      throw error
    })
}

/**
 * 创建权限
 * @param {Object} permissionData - 权限数据
 * @returns {Promise} 创建结果
 */
export function createPermission(permissionData) {
  console.log('[管理员API] 创建权限:', permissionData)
  return api
    .post('/admin/permissions', permissionData)
    .then((response) => {
      console.log('[管理员API] 创建权限成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 创建权限失败:', error)
      throw error
    })
}

/**
 * 更新权限
 * @param {number} permissionId - 权限ID
 * @param {Object} permissionData - 权限数据
 * @returns {Promise} 更新结果
 */
export function updatePermission(permissionId, permissionData) {
  console.log('[管理员API] 更新权限:', permissionId, permissionData)
  return api
    .put(`/admin/permissions/${permissionId}`, permissionData)
    .then((response) => {
      console.log('[管理员API] 更新权限成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 更新权限失败:', error)
      throw error
    })
}

/**
 * 删除权限
 * @param {number} permissionId - 权限ID
 * @returns {Promise} 删除结果
 */
export function deletePermission(permissionId) {
  console.log('[管理员API] 删除权限:', permissionId)
  return api
    .delete(`/admin/permissions/${permissionId}`)
    .then((response) => {
      console.log('[管理员API] 删除权限成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 删除权限失败:', error)
      throw error
    })
}

// ==================== 退款管理（新增） ====================

/**
 * 处理退款申请
 * @param {string} refundId - 退款ID
 * @param {Object} processData - 处理数据
 * @returns {Promise} 处理结果
 */
export function processRefund(refundId, processData) {
  console.log('[管理员API] 处理退款申请:', refundId, processData)
  return api
    .post(buildUrl(API.ADMIN.PROCESS_REFUND, { refundId }), processData)
    .then((response) => {
      console.log('[管理员API] 处理退款申请成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 处理退款申请失败:', error)
      throw error
    })
}

/**
 * 获取退款统计
 * @returns {Promise} 退款统计数据
 */
export function getRefundStatistics() {
  console.log('[管理员API] 获取退款统计')
  return api
    .get(API.ADMIN.REFUND_STATS)
    .then((response) => {
      console.log('[管理员API] 获取退款统计成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取退款统计失败:', error)
      throw error
    })
}

// ==================== 待审核商家（新增） ====================

/**
 * 获取待审核商家列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 待审核商家列表
 */
export function getPendingMerchants(params = {}) {
  console.log('[管理员API] 获取待审核商家列表:', params)
  return api
    .get('/admin/merchants/pending', { params })
    .then((response) => {
      console.log('[管理员API] 获取待审核商家列表成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取待审核商家列表失败:', error)
      throw error
    })
}

// ==================== 提现管理（新增） ====================

/**
 * 获取提现记录列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 提现记录列表
 */
export function getWithdrawList(params = {}) {
  console.log('[管理员API] 获取提现记录列表:', params)
  return api
    .get(API.ADMIN.WITHDRAWAL_LIST, { params })
    .then((response) => {
      console.log('[管理员API] 获取提现记录列表成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取提现记录列表失败:', error)
      throw error
    })
}

/**
 * 获取提现详情
 * @param {string} withdrawId - 提现记录ID
 * @returns {Promise} 提现详情
 */
export function getWithdrawDetail(withdrawId) {
  console.log('[管理员API] 获取提现详情:', withdrawId)
  return api
    .get(buildUrl(API.ADMIN.WITHDRAWAL_DETAIL, { id: withdrawId }))
    .then((response) => {
      console.log('[管理员API] 获取提现详情成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取提现详情失败:', error)
      throw error
    })
}

/**
 * 审核提现申请
 * @param {string} withdrawId - 提现记录ID
 * @param {Object} auditData - 审核数据 {decision: 'APPROVE'|'REJECT', comment: string}
 * @returns {Promise} 审核结果
 */
export function processWithdraw(withdrawId, auditData) {
  console.log('[管理员API] 审核提现申请:', withdrawId, auditData)
  return api
    .post(buildUrl(API.ADMIN.AUDIT_WITHDRAWAL, { id: withdrawId }), auditData)
    .then((response) => {
      console.log('[管理员API] 审核提现申请成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 审核提现申请失败:', error)
      throw error
    })
}

/**
 * 批量审核提现
 * @param {Object} batchData - 批量数据 {withdrawIds: [], decision: string, comment: string}
 * @returns {Promise} 批量审核结果
 */
export function batchProcessWithdraw(batchData) {
  console.log('[管理员API] 批量审核提现:', batchData)
  return api
    .post(API.ADMIN.BATCH_PROCESS_WITHDRAWAL, batchData)
    .then((response) => {
      console.log('[管理员API] 批量审核提现成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 批量审核提现失败:', error)
      throw error
    })
}

/**
 * 完成提现
 * @param {string} withdrawId - 提现记录ID
 * @param {Object} data - 完成数据 {remark?: string}
 * @returns {Promise} 完成结果
 */
export function completeWithdraw(withdrawId, data = {}) {
  console.log('[管理员API] 完成提现:', withdrawId)
  return api
    .put(buildUrl(API.ADMIN.COMPLETE_WITHDRAWAL, { id: withdrawId }), data)
    .then((response) => {
      console.log('[管理员API] 完成提现成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 完成提现失败:', error)
      throw error
    })
}

/**
 * 标记提现失败
 * @param {string} withdrawId - 提现记录ID
 * @param {Object} data - 失败数据 {reason: string}
 * @returns {Promise} 失败标记结果
 */
export function failWithdraw(withdrawId, data) {
  console.log('[管理员API] 标记提现失败:', withdrawId)
  return api
    .put(buildUrl(API.ADMIN.FAIL_WITHDRAWAL, { id: withdrawId }), data)
    .then((response) => {
      console.log('[管理员API] 标记提现失败成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 标记提现失败失败:', error)
      throw error
    })
}

/**
 * 获取提现统计
 * @returns {Promise} 提现统计数据
 */
export function getWithdrawStatistics() {
  console.log('[管理员API] 获取提现统计')
  return api
    .get(API.ADMIN.WITHDRAWAL_STATISTICS)
    .then((response) => {
      console.log('[管理员API] 获取提现统计成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取提现统计失败:', error)
      throw error
    })
}

/**
 * 获取提现趋势图表数据
 * @param {number} days - 天数 (7或30)
 * @returns {Promise} 提现趋势数据
 */
export function getWithdrawTrend(days = 7) {
  console.log('[管理员API] 获取提现趋势, 天数:', days)
  return api
    .get('/admin/finance/withdrawals/trend', { params: { days } })
    .then((response) => {
      console.log('[管理员API] 获取提现趋势成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取提现趋势失败:', error)
      throw error
    })
}

// ==================== 系统设置（新增） ====================

/**
 * 获取配置列表（分页）
 * @param {Object} params - 查询参数 {page, pageSize, keyword, configGroup, status}
 * @returns {Promise} 配置列表
 */
export function getSystemConfigList(params = {}) {
  console.log('[管理员API] 获取配置列表:', params)
  return api
    .get('/admin/settings/config', { params })
    .then((response) => {
      console.log('[管理员API] 获取配置列表成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取配置列表失败:', error)
      throw error
    })
}

/**
 * 获取所有配置分组
 * @returns {Promise} 配置分组列表
 */
export function getSystemConfigGroups() {
  console.log('[管理员API] 获取配置分组')
  return api
    .get('/admin/settings/config/groups')
    .then((response) => {
      console.log('[管理员API] 获取配置分组成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取配置分组失败:', error)
      throw error
    })
}

/**
 * 根据分组获取配置
 * @param {string} configGroup - 配置分组 (system/notification/security)
 * @returns {Promise} 配置数据
 */
export function getSystemConfigsByGroup(configGroup) {
  console.log('[管理员API] 获取分组配置:', configGroup)
  return api
    .get(`/admin/settings/config/group/${configGroup}`)
    .then((response) => {
      console.log('[管理员API] 获取分组配置成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取分组配置失败:', error)
      throw error
    })
}

/**
 * 获取配置详情
 * @param {string} configId - 配置ID
 * @returns {Promise} 配置详情
 */
export function getSystemConfigDetail(configId) {
  console.log('[管理员API] 获取配置详情:', configId)
  return api
    .get(`/admin/settings/config/${configId}`)
    .then((response) => {
      console.log('[管理员API] 获取配置详情成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 获取配置详情失败:', error)
      throw error
    })
}

/**
 * 创建配置
 * @param {Object} configData - 配置数据
 * @returns {Promise} 创建结果
 */
export function createSystemConfig(configData) {
  console.log('[管理员API] 创建配置:', configData)
  return api
    .post('/admin/settings/config', configData)
    .then((response) => {
      console.log('[管理员API] 创建配置成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 创建配置失败:', error)
      throw error
    })
}

/**
 * 更新配置
 * @param {string} configId - 配置ID
 * @param {Object} configData - 配置数据
 * @returns {Promise} 更新结果
 */
export function updateSystemConfig(configId, configData) {
  console.log('[管理员API] 更新配置:', configId, configData)
  return api
    .put(`/admin/settings/config/${configId}`, configData)
    .then((response) => {
      console.log('[管理员API] 更新配置成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 更新配置失败:', error)
      throw error
    })
}

/**
 * 批量更新配置（按分组）
 * @param {string} configGroup - 配置分组
 * @param {Object} configs - 配置键值对
 * @returns {Promise} 更新结果
 */
export function batchUpdateSystemConfigs(configGroup, configs) {
  console.log('[管理员API] 批量更新配置:', configGroup, configs)
  return api
    .post('/admin/settings/config/batch', { configGroup, configs })
    .then((response) => {
      console.log('[管理员API] 批量更新配置成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 批量更新配置失败:', error)
      throw error
    })
}

/**
 * 删除配置
 * @param {string} configId - 配置ID
 * @returns {Promise} 删除结果
 */
export function deleteSystemConfig(configId) {
  console.log('[管理员API] 删除配置:', configId)
  return api
    .delete(`/admin/settings/config/${configId}`)
    .then((response) => {
      console.log('[管理员API] 删除配置成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 删除配置失败:', error)
      throw error
    })
}

/**
 * 刷新配置缓存
 * @returns {Promise} 刷新结果
 */
export function refreshSystemConfigCache() {
  console.log('[管理员API] 刷新配置缓存')
  return api
    .post('/admin/settings/config/refresh')
    .then((response) => {
      console.log('[管理员API] 刷新配置缓存成功')
      return response
    })
    .catch((error) => {
      console.error('[管理员API] 刷新配置缓存失败:', error)
      throw error
    })
}
