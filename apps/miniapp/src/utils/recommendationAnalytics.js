/**
 * 推荐系统效果分析工具
 * 用于分析推荐系统的表现和用户反馈
 */
import analytics from './analytics.js'

class RecommendationAnalytics {
  constructor() {
    this.impressions = new Map()
    this.clicks = new Map()
    this.exposures = new Map()
  }

  /**
   * 记录曝光
   * @param {string|number} dishId - 菜品ID
   * @param {string} recommendSource - 推荐来源
   * @param {object} metadata - 元数据
   */
  recordImpression(dishId, recommendSource, metadata = {}) {
    const key = `${dishId}_${recommendSource}`

    if (!this.impressions.has(key)) {
      this.impressions.set(key, {
        dishId: String(dishId),
        recommendSource,
        count: 0,
        firstSeen: Date.now(),
        lastSeen: Date.now(),
        metadata
      })
    }

    const impression = this.impressions.get(key)
    impression.count++
    impression.lastSeen = Date.now()

    // 同时记录到通用埋点
    analytics.trackImpression('recommendation_impression', {
      dish_id: String(dishId),
      recommend_source: recommendSource,
      ...metadata
    })

    console.log(`📊 推荐曝光: ${dishId} (${recommendSource})`)
  }

  /**
   * 记录点击
   * @param {string|number} dishId - 菜品ID
   * @param {string} recommendSource - 推荐来源
   * @param {object} metadata - 元数据
   */
  recordClick(dishId, recommendSource, metadata = {}) {
    const key = `${dishId}_${recommendSource}`

    if (!this.clicks.has(key)) {
      this.clicks.set(key, {
        dishId: String(dishId),
        recommendSource,
        count: 0,
        firstClick: Date.now(),
        lastClick: Date.now(),
        metadata
      })
    }

    const click = this.clicks.get(key)
    click.count++
    click.lastClick = Date.now()

    // 同时记录到通用埋点
    analytics.trackClick('recommendation_click', 'dish', {
      dish_id: String(dishId),
      recommend_source: recommendSource,
      ...metadata
    })

    console.log(`📊 推荐点击: ${dishId} (${recommendSource})`)
  }

  /**
   * 记录下单转化
   * @param {string|number} dishId - 菜品ID
   * @param {string} recommendSource - 推荐来源
   * @param {object} orderInfo - 订单信息
   */
  recordConversion(dishId, recommendSource, orderInfo = {}) {
    const key = `${dishId}_${recommendSource}`

    // 记录转化事件
    analytics.trackEvent('recommendation_conversion', {
      dish_id: String(dishId),
      recommend_source: recommendSource,
      order_id: orderInfo.orderId,
      order_amount: orderInfo.amount,
      timestamp: Date.now()
    })

    console.log(`📊 推荐转化: ${dishId} (${recommendSource}) - 订单${orderInfo.orderId}`)
  }

  /**
   * 记录负面反馈
   * @param {string|number} dishId - 菜品ID
   * @param {string} recommendSource - 推荐来源
   * @param {string} reason - 负面反馈原因
   */
  recordNegativeFeedback(dishId, recommendSource, reason) {
    analytics.trackEvent('recommendation_negative_feedback', {
      dish_id: String(dishId),
      recommend_source: recommendSource,
      reason,
      timestamp: Date.now()
    })

    console.log(`📊 推荐负面反馈: ${dishId} (${recommendSource}) - ${reason}`)
  }

  /**
   * 计算点击率 (CTR)
   * @param {string|number} dishId - 菜品ID
   * @param {string} recommendSource - 推荐来源
   * @returns {number} 点击率 (0-1)
   */
  calculateCTR(dishId, recommendSource) {
    const key = `${dishId}_${recommendSource}`
    const impression = this.impressions.get(key)
    const click = this.clicks.get(key)

    if (!impression || impression.count === 0) return 0

    return (click?.count || 0) / impression.count
  }

  /**
   * 计算转化率 (CVR)
   * @param {string|number} dishId - 菜品ID
   * @param {string} recommendSource - 推荐来源
   * @param {number} conversions - 转化次数
   * @returns {number} 转化率 (0-1)
   */
  calculateCVR(dishId, recommendSource, conversions = 0) {
    const key = `${dishId}_${recommendSource}`
    const click = this.clicks.get(key)

    if (!click || click.count === 0) return 0

    return conversions / click.count
  }

  /**
   * 获取推荐来源的统计信息
   * @param {string} recommendSource - 推荐来源
   * @returns {object} 统计信息
   */
  getSourceStats(recommendSource) {
    let totalImpressions = 0
    let totalClicks = 0
    const dishes = new Set()

    // 统计曝光
    for (const [key, value] of this.impressions) {
      if (value.recommendSource === recommendSource) {
        totalImpressions += value.count
        dishes.add(value.dishId)
      }
    }

    // 统计点击
    for (const [key, value] of this.clicks) {
      if (value.recommendSource === recommendSource) {
        totalClicks += value.count
      }
    }

    return {
      source: recommendSource,
      totalImpressions,
      totalClicks,
      uniqueDishes: dishes.size,
      ctr: totalImpressions > 0 ? totalClicks / totalImpressions : 0
    }
  }

  /**
   * 上报分析数据到后端
   */
  async report() {
    try {
      const data = {
        impressions: Array.from(this.impressions.values()),
        clicks: Array.from(this.clicks.values()),
        timestamp: Date.now()
      }

      await uni.request({
        url: '/api/v1/recommendation/analytics',
        method: 'POST',
        data,
        timeout: 5000
      })

      console.log('✓ 推荐分析数据已上报')

      // 清空已上报的数据
      this.impressions.clear()
      this.clicks.clear()
    } catch (error) {
      console.error('上报推荐分析数据失败:', error)
    }
  }

  /**
   * 获取当前统计摘要
   * @returns {object} 统计摘要
   */
  getSummary() {
    const sources = new Set()

    // 收集所有推荐来源
    for (const value of this.impressions.values()) {
      sources.add(value.recommendSource)
    }

    // 生成各来源的统计
    const sourceStats = {}
    for (const source of sources) {
      sourceStats[source] = this.getSourceStats(source)
    }

    return {
      totalImpressions: this.impressions.size,
      totalClicks: this.clicks.size,
      sources: sourceStats
    }
  }

  /**
   * 清空所有数据
   */
  clear() {
    this.impressions.clear()
    this.clicks.clear()
    this.exposures.clear()
  }
}

// 创建单例
const recommendationAnalytics = new RecommendationAnalytics()

// 定时上报（每5分钟）
setInterval(() => {
  recommendationAnalytics.report()
}, 5 * 60 * 1000)

// 页面卸载时上报
uni.onAppHide(() => {
  recommendationAnalytics.report()
})

export default recommendationAnalytics
