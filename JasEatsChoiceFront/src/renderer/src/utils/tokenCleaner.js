/**
 * Token 清理工具
 * 用于在 Electron 应用启动时检查并清理过期的 token
 */

/**
 * 解码 JWT token 并获取过期时间
 * @param {string} token - JWT token
 * @returns {object|null} - 解码后的 token 数据，如果解析失败返回 null
 */
export const decodeJwtToken = (token) => {
  try {
    if (!token || typeof token !== 'string') return null

    const parts = token.split('.')
    if (parts.length !== 3) return null

    const payload = parts[1]
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=')

    const decoded = atob(padded)
    return JSON.parse(decoded)
  } catch (error) {
    console.error('Token 解码失败:', error)
    return null
  }
}

/**
 * 检查 token 是否已过期
 * @param {string} token - JWT token
 * @returns {boolean} - true 表示已过期，false 表示未过期
 */
export const isTokenExpired = (token) => {
  const decoded = decodeJwtToken(token)
  if (!decoded || !decoded.exp) return true

  const expirationTime = decoded.exp * 1000 // 转换为毫秒
  const now = Date.now()

  // 添加 5 秒的缓冲时间，避免临界情况
  return now >= expirationTime - 5000
}

/**
 * 清理过期的 token
 * @returns {object} - 清理结果 { cleaned: boolean, tokens: string[] }
 */
export const cleanExpiredTokens = () => {
  const cleanedTokens = []
  const currentTime = new Date().toISOString()

  // 检查用户端 token
  const userToken = localStorage.getItem('auth_token')
  if (userToken) {
    if (isTokenExpired(userToken)) {
      console.log('[TokenCleaner] 清理过期的用户端 token')
      cleanedTokens.push('auth_token')
      localStorage.removeItem('auth_token')
      localStorage.removeItem('auth_userId')
      localStorage.removeItem('auth_phone')
      localStorage.removeItem('auth_merchantId')
      localStorage.removeItem('auth_currentRole')
    } else {
      console.log('[TokenCleaner] 用户端 token 有效')
    }
  }

  // 检查管理员 token
  const adminToken = localStorage.getItem('admin_token')
  if (adminToken) {
    if (isTokenExpired(adminToken)) {
      console.log('[TokenCleaner] 清理过期的管理员 token')
      cleanedTokens.push('admin_token')
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_info')
    } else {
      console.log('[TokenCleaner] 管理员 token 有效')
    }
  }

  // 记录清理时间
  if (cleanedTokens.length > 0) {
    localStorage.setItem('last_token_clean', currentTime)
    console.log(`[TokenCleaner] 已清理 ${cleanedTokens.length} 个过期 token:`, cleanedTokens)
  }

  return {
    cleaned: cleanedTokens.length > 0,
    tokens: cleanedTokens,
    timestamp: currentTime
  }
}

/**
 * 获取 token 的过期时间
 * @param {string} token - JWT token
 * @returns {Date|null} - 过期时间，如果解析失败返回 null
 */
export const getTokenExpiration = (token) => {
  const decoded = decodeJwtToken(token)
  if (!decoded || !decoded.exp) return null

  return new Date(decoded.exp * 1000)
}

/**
 * 获取所有 token 的状态信息
 * @returns {object} - 所有 token 的状态
 */
export const getAllTokenStatus = () => {
  const userToken = localStorage.getItem('auth_token')
  const adminToken = localStorage.getItem('admin_token')

  const result = {
    user: {
      exists: !!userToken,
      expired: userToken ? isTokenExpired(userToken) : null,
      expiration: userToken ? getTokenExpiration(userToken) : null
    },
    admin: {
      exists: !!adminToken,
      expired: adminToken ? isTokenExpired(adminToken) : null,
      expiration: adminToken ? getTokenExpiration(adminToken) : null
    }
  }

  console.log('[TokenCleaner] Token 状态:', result)
  return result
}

export default {
  decodeJwtToken,
  isTokenExpired,
  cleanExpiredTokens,
  getTokenExpiration,
  getAllTokenStatus
}
