/**
 * 数据分析埋点工具
 * 用于收集用户行为数据，支持批量上传
 */
class Analytics {
  constructor() {
    this.baseUrl = '/api/v1/analytics'
    this.queue = []
    this.isUploading = false
    this.maxQueueSize = 50
    this.uploadInterval = 10000 // 10秒自动上传一次

    // 启动定时上传
    this.startAutoUpload()
  }

  /**
   * 页面访问
   * @param {string} pageName - 页面名称
   * @param {object} properties - 自定义属性
   */
  trackPageView(pageName, properties = {}) {
    this.track('page_view', {
      page_name: pageName,
      timestamp: Date.now(),
      ...properties
    })
  }

  /**
   * 点击事件
   * @param {string} eventName - 事件名称
   * @param {string} element - 元素类型
   * @param {object} properties - 自定义属性
   */
  trackClick(eventName, element, properties = {}) {
    this.track('click', {
      event_name: eventName,
      element_type: element,
      timestamp: Date.now(),
      ...properties
    })
  }

  /**
   * 曝光事件
   * @param {string} eventName - 事件名称
   * @param {object} properties - 自定义属性
   */
  trackImpression(eventName, properties = {}) {
    this.track('impression', {
      event_name: eventName,
      timestamp: Date.now(),
      ...properties
    })
  }

  /**
   * 业务事件
   * @param {string} eventName - 事件名称
   * @param {object} properties - 自定义属性
   */
  trackEvent(eventName, properties = {}) {
    this.track(eventName, {
      timestamp: Date.now(),
      ...properties
    })
  }

  /**
   * 发送埋点数据
   * @private
   */
  track(eventType, properties) {
    // 添加通用属性
    const data = {
      event_type: eventType,
      user_id: this.getUserId(),
      session_id: this.getSessionId(),
      platform: this.getPlatform(),
      device_id: this.getDeviceId(),
      properties
    }

    // 添加到队列
    this.queue.push(data)

    console.log(`📊 埋点记录: [${eventType}]`, properties)

    // 达到阈值立即上传
    if (this.queue.length >= 10) {
      this.flush()
    }
  }

  /**
   * 批量上传埋点数据
   */
  async flush() {
    if (this.isUploading || this.queue.length === 0) return

    this.isUploading = true

    try {
      const events = this.queue.splice(0, this.maxQueueSize)

      // 调用后端API
      await uni.request({
        url: this.baseUrl + '/batch',
        method: 'POST',
        data: { events },
        timeout: 5000
      })

      console.log(`✓ 埋点上传成功: ${events.length}条`)
    } catch (error) {
      console.error('埋点上传失败:', error)
      // 失败的数据放回队列头部
      if (events && events.length > 0) {
        this.queue.unshift(...events)
      }
    } finally {
      this.isUploading = false
    }
  }

  /**
   * 启动定时上传
   * @private
   */
  startAutoUpload() {
    setInterval(() => {
      if (this.queue.length > 0) {
        this.flush()
      }
    }, this.uploadInterval)
  }

  /**
   * 获取用户ID
   * @private
   */
  getUserId() {
    try {
      const userStore = require('@/store').useUserStore()
      return userStore.userInfo?.userId || userStore.userInfo?.id || 'anonymous'
    } catch (error) {
      return 'anonymous'
    }
  }

  /**
   * 获取会话ID
   * @private
   */
  getSessionId() {
    let sessionId = uni.getStorageSync('analytics_session_id')
    if (!sessionId) {
      sessionId = this.generateUUID()
      uni.setStorageSync('analytics_session_id', sessionId)
    }
    return sessionId
  }

  /**
   * 获取平台信息
   * @private
   */
  getPlatform() {
    const systemInfo = uni.getSystemInfoSync()
    return {
      os: systemInfo.platform,
      app: systemInfo.app,
      version: systemInfo.version,
      system: systemInfo.system
    }
  }

  /**
   * 获取设备ID
   * @private
   */
  getDeviceId() {
    let deviceId = uni.getStorageSync('analytics_device_id')
    if (!deviceId) {
      deviceId = this.generateUUID()
      uni.setStorageSync('analytics_device_id', deviceId)
    }
    return deviceId
  }

  /**
   * 生成UUID
   * @private
   */
  generateUUID() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
      const r = Math.random() * 16 | 0
      const v = c === 'x' ? r : (r & 0x3 | 0x8)
      return v.toString(16)
    })
  }

  /**
   * 清空队列
   */
  clearQueue() {
    this.queue = []
  }

  /**
   * 获取队列大小
   */
  getQueueSize() {
    return this.queue.length
  }
}

// 创建单例
const analytics = new Analytics()

// 页面卸载时上传
uni.onAppHide(() => {
  analytics.flush()
})

// 页面卸载时上传
if (typeof window !== 'undefined') {
  window.addEventListener('beforeunload', () => {
    analytics.flush()
  })
}

export default analytics
