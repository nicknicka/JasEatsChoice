/**
 * 管理员权限验证工具
 */

// 管理员token存储key
const ADMIN_TOKEN_KEY = 'admin_token'
const ADMIN_INFO_KEY = 'admin_info'

/**
 * 获取管理员token
 */
export function getAdminToken() {
  return localStorage.getItem(ADMIN_TOKEN_KEY)
}

/**
 * 设置管理员token
 */
export function setAdminToken(token) {
  localStorage.setItem(ADMIN_TOKEN_KEY, token)
}

/**
 * 移除管理员token
 */
export function removeAdminToken() {
  localStorage.removeItem(ADMIN_TOKEN_KEY)
  localStorage.removeItem(ADMIN_INFO_KEY)
}

/**
 * 获取管理员信息
 */
export function getAdminInfo() {
  const info = localStorage.getItem(ADMIN_INFO_KEY)
  return info ? JSON.parse(info) : null
}

/**
 * 设置管理员信息
 */
export function setAdminInfo(info) {
  localStorage.setItem(ADMIN_INFO_KEY, JSON.stringify(info))
}

/**
 * 检查管理员是否已登录
 */
export function isAdminLoggedIn() {
  const token = getAdminToken()
  return !!token
}

/**
 * 检查是否有指定权限
 * @param {string} permissionCode - 权限编码
 */
export function hasPermission(permissionCode) {
  const adminInfo = getAdminInfo()
  if (!adminInfo) return false

  // 超级管理员拥有所有权限
  if (adminInfo.roleCode === 'SUPER_ADMIN') return true

  // TODO: 从管理员权限列表中检查
  // 目前先返回true，等权限接口完善后实现
  return true
}

/**
 * 检查是否有任一权限
 * @param {string[]} permissionCodes - 权限编码数组
 */
export function hasAnyPermission(permissionCodes) {
  if (!permissionCodes || permissionCodes.length === 0) return true
  return permissionCodes.some(code => hasPermission(code))
}

/**
 * 管理员退出登录
 */
export function adminLogout() {
  removeAdminToken()
  // 跳转到登录页
  if (window.location.pathname !== '/admin/login') {
    window.location.href = '/admin/login'
  }
}
