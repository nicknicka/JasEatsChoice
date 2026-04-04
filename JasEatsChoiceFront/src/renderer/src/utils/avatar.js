/**
 * 头像URL处理工具
 */

import { API_CONFIG } from '@/config'

/**
 * 获取完整的头像URL
 * @param {string} avatar - 头像URL（可以是相对路径或完整URL）
 * @returns {string} 完整的头像URL
 */
export function getAvatarUrl(avatar) {
  if (!avatar) {
    // 返回默认头像（Element Plus默认头像）
    return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  }

  // 如果已经是完整URL，直接返回
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar
  }

  // 如果是 /api 开头的路径，直接使用 baseURL（baseURL 已包含 /api）
  // avatar: /api/uploads/xxx.png -> http://localhost:7777/api/uploads/xxx.png
  if (avatar.startsWith('/api/')) {
    return API_CONFIG.baseURL + avatar.substring(4) // 去掉 /api 前缀
  }

  // 如果是 /uploads 开头的路径，添加 baseURL
  if (avatar.startsWith('/uploads/')) {
    return API_CONFIG.baseURL + avatar
  }

  // 其他情况，直接返回
  return avatar
}

/**
 * 获取用户头像显示的文本（当头像加载失败时使用）
 * @param {string} nickname - 用户昵称
 * @param {string} username - 用户名（备用）
 * @returns {string} 显示文本
 */
export function getAvatarText(nickname, username) {
  if (nickname) {
    return nickname.charAt(0).toUpperCase()
  }
  if (username) {
    return username.charAt(0).toUpperCase()
  }
  return 'U'
}
