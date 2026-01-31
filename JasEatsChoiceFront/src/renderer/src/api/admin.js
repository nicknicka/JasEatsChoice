/**
 * 管理员API服务
 * 统一管理所有管理员相关的API调用
 */

import api from '../utils/api'
import { API_CONFIG } from '../config'

// ==================== 管理员认证 ====================

/**
 * 管理员登录
 * @param {string} username - 用户名
 * @param {string} password - 密码
 * @returns {Promise} 登录结果
 */
export function adminLogin(username, password) {
  console.log('[管理员API] 尝试登录:', username)
  return api.post(API_CONFIG.admin.login, { username, password })
    .then(response => {
      console.log('[管理员API] 登录成功:', response)
      return response
    })
    .catch(error => {
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
  return api.get(API_CONFIG.admin.current)
    .then(response => {
      console.log('[管理员API] 获取管理员信息成功:', response)
      return response
    })
    .catch(error => {
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
  return api.get(API_CONFIG.admin.list, { params })
    .then(response => {
      console.log('[管理员API] 获取管理员列表成功')
      return response
    })
    .catch(error => {
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
  return api.post(API_CONFIG.admin.create, adminData)
    .then(response => {
      console.log('[管理员API] 创建管理员成功')
      return response
    })
    .catch(error => {
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
  return api.put(API_CONFIG.admin.updateStatus.replace('{adminId}', adminId), { status })
    .then(response => {
      console.log('[管理员API] 修改管理员状态成功')
      return response
    })
    .catch(error => {
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
  return api.put(API_CONFIG.admin.resetPassword.replace('{adminId}', adminId), { password })
    .then(response => {
      console.log('[管理员API] 重置管理员密码成功')
      return response
    })
    .catch(error => {
      console.error('[管理员API] 重置管理员密码失败:', error)
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
  return api.get(API_CONFIG.admin.dashboard)
    .then(response => {
      console.log('[管理员API] 获取控制台统计数据成功')
      return response
    })
    .catch(error => {
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
  return api.get(API_CONFIG.admin.userStats, { params: { days } })
    .then(response => {
      console.log('[管理员API] 获取用户统计数据成功')
      return response
    })
    .catch(error => {
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
  return api.get(API_CONFIG.admin.orderStats, { params: { days } })
    .then(response => {
      console.log('[管理员API] 获取订单统计数据成功')
      return response
    })
    .catch(error => {
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
  return api.get(API_CONFIG.admin.revenueStats, { params: { days } })
    .then(response => {
      console.log('[管理员API] 获取收入统计数据成功')
      return response
    })
    .catch(error => {
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
  return api.get(API_CONFIG.admin.userList, { params })
    .then(response => {
      console.log('[管理员API] 获取用户列表成功')
      return response
    })
    .catch(error => {
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
  return api.get(API_CONFIG.admin.userDetail.replace('{userId}', userId))
    .then(response => {
      console.log('[管理员API] 获取用户详情成功')
      return response
    })
    .catch(error => {
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
  return api.put(API_CONFIG.admin.updateUserStatus.replace('{userId}', userId), { status })
    .then(response => {
      console.log('[管理员API] 修改用户状态成功')
      return response
    })
    .catch(error => {
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
  return api.delete(API_CONFIG.admin.deleteUser.replace('{userId}', userId))
    .then(response => {
      console.log('[管理员API] 删除用户成功')
      return response
    })
    .catch(error => {
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
  return api.get(API_CONFIG.admin.merchantList, { params })
    .then(response => {
      console.log('[管理员API] 获取商家列表成功')
      return response
    })
    .catch(error => {
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
  return api.get(API_CONFIG.admin.merchantDetail.replace('{merchantId}', merchantId))
    .then(response => {
      console.log('[管理员API] 获取商家详情成功')
      return response
    })
    .catch(error => {
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
  return api.post(API_CONFIG.admin.auditMerchant.replace('{merchantId}', merchantId), auditData)
    .then(response => {
      console.log('[管理员API] 审核商家成功')
      return response
    })
    .catch(error => {
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
  return api.put(API_CONFIG.admin.updateMerchantStatus.replace('{merchantId}', merchantId), { status })
    .then(response => {
      console.log('[管理员API] 修改商家状态成功')
      return response
    })
    .catch(error => {
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
  return api.get(API_CONFIG.admin.orderList, { params })
    .then(response => {
      console.log('[管理员API] 获取订单列表成功')
      return response
    })
    .catch(error => {
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
  return api.get(API_CONFIG.admin.orderDetail.replace('{orderId}', orderId))
    .then(response => {
      console.log('[管理员API] 获取订单详情成功')
      return response
    })
    .catch(error => {
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
  return api.get(API_CONFIG.admin.dishList, { params })
    .then(response => {
      console.log('[管理员API] 获取菜品列表成功')
      return response
    })
    .catch(error => {
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
  return api.get(API_CONFIG.admin.dishDetail.replace('{dishId}', dishId))
    .then(response => {
      console.log('[管理员API] 获取菜品详情成功')
      return response
    })
    .catch(error => {
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
  return api.post(API_CONFIG.admin.auditDish.replace('{dishId}', dishId), auditData)
    .then(response => {
      console.log('[管理员API] 审核菜品成功')
      return response
    })
    .catch(error => {
      console.error('[管理员API] 审核菜品失败:', error)
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
  return api.put(API_CONFIG.admin.updateDishStatus.replace('{dishId}', dishId), { status })
    .then(response => {
      console.log('[管理员API] 修改菜品状态成功')
      return response
    })
    .catch(error => {
      console.error('[管理员API] 修改菜品状态失败:', error)
      throw error
    })
}

// ==================== 财务管理 ====================

/**
 * 获取提现申请列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 提现申请列表
 */
export function getWithdrawalList(params = {}) {
  console.log('[管理员API] 获取提现申请列表:', params)
  return api.get(API_CONFIG.admin.withdrawalList, { params })
    .then(response => {
      console.log('[管理员API] 获取提现申请列表成功')
      return response
    })
    .catch(error => {
      console.error('[管理员API] 获取提现申请列表失败:', error)
      throw error
    })
}

/**
 * 审核提现申请
 * @param {number} id - 提现ID
 * @param {Object} auditData - 审核数据
 * @returns {Promise} 审核结果
 */
export function auditWithdrawal(id, auditData) {
  console.log('[管理员API] 审核提现申请:', id, auditData)
  return api.post(API_CONFIG.admin.auditWithdrawal.replace('{id}', id), auditData)
    .then(response => {
      console.log('[管理员API] 审核提现申请成功')
      return response
    })
    .catch(error => {
      console.error('[管理员API] 审核提现申请失败:', error)
      throw error
    })
}

/**
 * 获取充值记录列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 充值记录列表
 */
export function getRechargeList(params = {}) {
  console.log('[管理员API] 获取充值记录列表:', params)
  return api.get(API_CONFIG.admin.rechargeList, { params })
    .then(response => {
      console.log('[管理员API] 获取充值记录列表成功')
      return response
    })
    .catch(error => {
      console.error('[管理员API] 获取充值记录列表失败:', error)
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
  return api.get(API_CONFIG.admin.refundList, { params })
    .then(response => {
      console.log('[管理员API] 获取退款记录列表成功')
      return response
    })
    .catch(error => {
      console.error('[管理员API] 获取退款记录列表失败:', error)
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
  return api.get(API_CONFIG.admin.operationLogs, { params })
    .then(response => {
      console.log('[管理员API] 获取操作日志成功')
      return response
    })
    .catch(error => {
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
  return api.get(API_CONFIG.admin.systemLogs, { params })
    .then(response => {
      console.log('[管理员API] 获取系统日志成功')
      return response
    })
    .catch(error => {
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
  return api.get(API_CONFIG.admin.loginLogs, { params })
    .then(response => {
      console.log('[管理员API] 获取登录日志成功')
      return response
    })
    .catch(error => {
      console.error('[管理员API] 获取登录日志失败:', error)
      throw error
    })
}
