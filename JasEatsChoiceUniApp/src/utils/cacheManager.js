/**
 * 缓存管理工具
 * 用于实现离线支持和数据缓存
 */
class CacheManager {
  constructor() {
    this.CACHE_PREFIX = 'jaseatschoice_cache_'
    this.CACHE_VERSION = 'v1'
    this.DEFAULT_EXPIRY = 24 * 60 * 60 * 1000 // 24小时
  }

  /**
   * 生成缓存键
   * @private
   * @param {string} key - 键名
   * @returns {string} 完整的缓存键
   */
  getCacheKey(key) {
    return `${this.CACHE_PREFIX}${this.CACHE_VERSION}_${key}`
  }

  /**
   * 设置缓存
   * @param {string} key - 键名
   * @param {any} data - 数据
   * @param {number} expiry - 过期时间（毫秒）
   */
  set(key, data, expiry = this.DEFAULT_EXPIRY) {
    try {
      const cacheData = {
        data,
        timestamp: Date.now(),
        expiry: Date.now() + expiry
      }

      uni.setStorageSync(this.getCacheKey(key), JSON.stringify(cacheData))

      console.log(`✓ 缓存已设置: ${key}`)
    } catch (error) {
      console.error('设置缓存失败:', error)
    }
  }

  /**
   * 获取缓存
   * @param {string} key - 键名
   * @returns {any|null} 缓存数据，如果不存在或已过期则返回null
   */
  get(key) {
    try {
      const cacheStr = uni.getStorageSync(this.getCacheKey(key))

      if (!cacheStr) {
        return null
      }

      const cacheData = JSON.parse(cacheStr)

      // 检查是否过期
      if (Date.now() > cacheData.expiry) {
        this.remove(key)
        return null
      }

      console.log(`✓ 缓存命中: ${key}`)
      return cacheData.data
    } catch (error) {
      console.error('读取缓存失败:', error)
      return null
    }
  }

  /**
   * 移除缓存
   * @param {string} key - 键名
   */
  remove(key) {
    try {
      uni.removeStorageSync(this.getCacheKey(key))
      console.log(`✓ 缓存已移除: ${key}`)
    } catch (error) {
      console.error('移除缓存失败:', error)
    }
  }

  /**
   * 清空所有缓存
   */
  clear() {
    try {
      const info = uni.getStorageInfoSync()

      for (const key of info.keys) {
        if (key.startsWith(this.CACHE_PREFIX)) {
          uni.removeStorageSync(key)
        }
      }

      console.log('✓ 所有缓存已清空')
    } catch (error) {
      console.error('清空缓存失败:', error)
    }
  }

  /**
   * 检查缓存是否存在且有效
   * @param {string} key - 键名
   * @returns {boolean} 是否有效
   */
  has(key) {
    return this.get(key) !== null
  }

  /**
   * 缓存API响应
   * @param {string} key - 缓存键
   * @param {Function} apiFn - API请求函数
   * @param {number} expiry - 过期时间
   */
  async cacheApiResponse(key, apiFn, expiry = this.DEFAULT_EXPIRY) {
    // 尝试从缓存获取
    const cached = this.get(key)
    if (cached) {
      console.log(`✓ 使用缓存数据: ${key}`)
      return cached
    }

    // 缓存不存在，调用API
    try {
      const response = await apiFn()

      // 缓存响应数据
      this.set(key, response, expiry)

      return response
    } catch (error) {
      console.error(`API请求失败: ${key}`, error)
      throw error
    }
  }

  /**
   * 获取缓存统计信息
   * @returns {object} 统计信息
   */
  getStats() {
    try {
      const info = uni.getStorageInfoSync()
      let cacheCount = 0
      let totalSize = 0

      for (const key of info.keys) {
        if (key.startsWith(this.CACHE_PREFIX)) {
          cacheCount++
          const data = uni.getStorageSync(key)
          totalSize += new Blob([data]).size
        }
      }

      return {
        count: cacheCount,
        size: totalSize,
        sizeFormatted: this.formatSize(totalSize)
      }
    } catch (error) {
      console.error('获取缓存统计失败:', error)
      return {
        count: 0,
        size: 0,
        sizeFormatted: '0 B'
      }
    }
  }

  /**
   * 格式化文件大小
   * @private
   */
  formatSize(bytes) {
    if (bytes === 0) return '0 B'

    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))

    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
  }

  /**
   * 清理过期缓存
   */
  cleanExpired() {
    try {
      const info = uni.getStorageInfoSync()
      const now = Date.now()
      let cleaned = 0

      for (const key of info.keys) {
        if (key.startsWith(this.CACHE_PREFIX)) {
          const cacheStr = uni.getStorageSync(key)
          if (cacheStr) {
            try {
              const cacheData = JSON.parse(cacheStr)
              if (now > cacheData.expiry) {
                uni.removeStorageSync(key)
                cleaned++
              }
            } catch (e) {
              // 解析失败，删除该缓存
              uni.removeStorageSync(key)
              cleaned++
            }
          }
        }
      }

      console.log(`✓ 已清理 ${cleaned} 个过期缓存`)
      return cleaned
    } catch (error) {
      console.error('清理过期缓存失败:', error)
      return 0
    }
  }

  /**
   * 预热缓存（提前加载常用数据）
   * @param {Array} tasks - 任务数组，每项包含key和apiFn
   */
  async warmup(tasks) {
    console.log('开始预热缓存...')

    const results = await Promise.allSettled(
      tasks.map(async (task) => {
        try {
          await this.cacheApiResponse(task.key, task.apiFn, task.expiry)
          return { key: task.key, success: true }
        } catch (error) {
          return { key: task.key, success: false, error }
        }
      })
    )

    const success = results.filter(r => r.value.success).length
    const failed = results.filter(r => !r.value.success).length

    console.log(`✓ 缓存预热完成: 成功${success}个, 失败${failed}个`)

    return { success, failed }
  }
}

// 创建单例
const cacheManager = new CacheManager()

// 定期清理过期缓存（每小时）
setInterval(() => {
  cacheManager.cleanExpired()
}, 60 * 60 * 1000)

export default cacheManager
