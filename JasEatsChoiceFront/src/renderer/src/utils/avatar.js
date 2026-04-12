/**
 * 图片URL处理工具
 */

import { API_CONFIG } from '@/config'

/**
 * 标准化图片URL，修正指向错误主机/端口的完整URL
 * 数据库中可能存储了指向 localhost:8080 的旧URL，需要修正为当前配置的后端地址
 * @param {string} url - 完整的图片URL
 * @returns {string} 标准化后的URL
 */
export function normalizeImageUrl(url) {
  if (!url || !url.startsWith('http')) {
    return url
  }

  try {
    const urlObj = new URL(url)
    const configuredBase = new URL(API_CONFIG.baseURL)

    // 如果主机或端口不匹配配置的后端地址，进行修正
    if (urlObj.host !== configuredBase.host) {
      let path = urlObj.pathname
      // 提取 /api 之后的路径部分（baseURL 已包含 /api）
      if (path.startsWith(configuredBase.pathname)) {
        path = path.substring(configuredBase.pathname.length)
      } else if (path.startsWith('/api/')) {
        path = path.substring(4)
      }
      return configuredBase.origin + configuredBase.pathname + path
    }

    return url
  } catch {
    return url
  }
}

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

  // 已经是完整URL，标准化后返回（修正错误端口等）
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return normalizeImageUrl(avatar)
  }

  // /api/uploads/xxx -> baseURL（已含/api）+ /uploads/xxx
  if (avatar.startsWith('/api/')) {
    return API_CONFIG.baseURL + avatar.substring(4)
  }

  // /uploads/xxx -> baseURL + /uploads/xxx
  if (avatar.startsWith('/uploads/')) {
    return API_CONFIG.baseURL + avatar
  }

  // uploads/xxx（缺少前导/）-> baseURL + /uploads/xxx
  if (avatar.startsWith('uploads/')) {
    return API_CONFIG.baseURL + '/uploads/' + avatar.substring(8)
  }

  // avatar/xxx 或其他相对路径 -> baseURL + /uploads/ + path
  if (!avatar.startsWith('/')) {
    return API_CONFIG.baseURL + '/uploads/' + avatar
  }

  // 其他以 / 开头的路径
  return API_CONFIG.baseURL + avatar
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
