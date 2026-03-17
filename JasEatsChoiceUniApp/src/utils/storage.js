/**
 * 本地存储工具类
 */
export const storage = {
  /**
   * 设置数据
   * @param {string} key - 键
   * @param {*} value - 值
   */
  set(key, value) {
    try {
      uni.setStorageSync(key, value)
      return true
    } catch (e) {
      console.error('存储数据失败:', e)
      return false
    }
  },

  /**
   * 获取数据
   * @param {string} key - 键
   * @param {*} defaultValue - 默认值
   */
  get(key, defaultValue = null) {
    try {
      const value = uni.getStorageSync(key)
      return value !== '' ? value : defaultValue
    } catch (e) {
      console.error('获取数据失败:', e)
      return defaultValue
    }
  },

  /**
   * 移除数据
   * @param {string} key - 键
   */
  remove(key) {
    try {
      uni.removeStorageSync(key)
      return true
    } catch (e) {
      console.error('移除数据失败:', e)
      return false
    }
  },

  /**
   * 清空所有数据
   */
  clear() {
    try {
      uni.clearStorageSync()
      return true
    } catch (e) {
      console.error('清空数据失败:', e)
      return false
    }
  },

  /**
   * 获取所有数据
   */
  getAll() {
    try {
      const res = uni.getStorageInfoSync()
      const data = {}
      res.keys.forEach(key => {
        data[key] = uni.getStorageSync(key)
      })
      return data
    } catch (e) {
      console.error('获取所有数据失败:', e)
      return {}
    }
  }
}

/**
 * Token管理
 */
export const tokenStorage = {
  /**
   * 设置Token
   * @param {string} token - JWT Token
   */
  setToken(token) {
    return storage.set('token', token)
  },

  /**
   * 获取Token
   * @returns {string}
   */
  getToken() {
    return storage.get('token', '')
  },

  /**
   * 移除Token
   */
  removeToken() {
    return storage.remove('token')
  }
}

/**
 * 用户信息管理
 */
export const userStorage = {
  /**
   * 设置用户信息
   * @param {Object} userInfo - 用户信息
   */
  setUserInfo(userInfo) {
    return storage.set('userInfo', userInfo)
  },

  /**
   * 获取用户信息
   * @returns {Object|null}
   */
  getUserInfo() {
    return storage.get('userInfo', null)
  },

  /**
   * 移除用户信息
   */
  removeUserInfo() {
    return storage.remove('userInfo')
  }
}
