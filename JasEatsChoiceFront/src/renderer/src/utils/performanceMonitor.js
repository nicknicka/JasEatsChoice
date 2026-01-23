/**
 * 性能监控工具
 * 用于追踪应用性能指标
 */

class PerformanceMonitor {
  constructor() {
    this.metrics = new Map()
    this.observers = []
    this.isEnabled = import.meta.env.DEV
  }

  /**
   * 开始计时
   * @param {string} label - 计时标签
   */
  start(label) {
    if (!this.isEnabled) return
    this.metrics.set(label, {
      startTime: performance.now(),
      endTime: null,
      duration: null
    })
  }

  /**
   * 结束计时
   * @param {string} label - 计时标签
   * @returns {number|null} 持续时间（毫秒）
   */
  end(label) {
    if (!this.isEnabled) return null

    const metric = this.metrics.get(label)
    if (!metric) {
      console.warn(`[PerformanceMonitor] 未找到标签: ${label}`)
      return null
    }

    metric.endTime = performance.now()
    metric.duration = metric.endTime - metric.startTime

    this.log(label, metric.duration)
    return metric.duration
  }

  /**
   * 记录性能指标
   * @param {string} label - 标签
   * @param {number} duration - 持续时间
   */
  log(label, duration) {
    if (!this.isEnabled) return

    const style = duration > 1000 ? 'color: #f56c6c' : duration > 500 ? 'color: #e6a23c' : 'color: #67c23a'
    console.log(`%c[Performance] ${label}: ${duration.toFixed(2)}ms`, style)
  }

  /**
   * 测量异步函数执行时间
   * @param {string} label - 标签
   * @param {Function} fn - 异步函数
   * @returns {Promise} 函数执行结果
   */
  async measure(label, fn) {
    if (!this.isEnabled) return fn()

    this.start(label)
    try {
      const result = await fn()
      this.end(label)
      return result
    } catch (error) {
      this.end(label)
      throw error
    }
  }

  /**
   * 监控Web Vitals指标
   */
  observeWebVitals() {
    if (!this.isEnabled || !('PerformanceObserver' in window)) return

    // 监控 Largest Contentful Paint (LCP)
    this.observe('LCP', (entry) => {
      console.log(`[WebVitals] LCP: ${entry.startTime.toFixed(2)}ms`)
    })

    // 监控 First Input Delay (FID)
    this.observe('FID', (entry) => {
      console.log(`[WebVitals] FID: ${entry.processingStart - entry.startTime.toFixed(2)}ms`)
    })

    // 监控 Cumulative Layout Shift (CLS)
    this.observe('CLS', (entry) => {
      if (!entry.hadRecentInput) {
        console.log(`[WebVitals] CLS: ${entry.value.toFixed(4)}`)
      }
    })
  }

  /**
   * 观察性能条目
   * @param {string} type - 性能条目类型
   * @param {Function} callback - 回调函数
   */
  observe(type, callback) {
    if (!('PerformanceObserver' in window)) return

    try {
      const observer = new PerformanceObserver((list) => {
        for (const entry of list.getEntries()) {
          callback(entry)
        }
      })

      observer.observe({ entryTypes: [type] })
      this.observers.push(observer)
    } catch (e) {
      console.warn(`[PerformanceMonitor] 无法观察 ${type}:`, e)
    }
  }

  /**
   * 获取内存使用情况
   * @returns {Object|null} 内存信息
   */
  getMemoryUsage() {
    if (!performance.memory) return null

    return {
      used: (performance.memory.usedJSHeapSize / 1048576).toFixed(2), // MB
      total: (performance.memory.totalJSHeapSize / 1048576).toFixed(2),
      limit: (performance.memory.jsHeapSizeLimit / 1048576).toFixed(2),
      percentage: ((performance.memory.usedJSHeapSize / performance.memory.jsHeapSizeLimit) * 100).toFixed(2)
    }
  }

  /**
   * 获取性能指标摘要
   * @returns {Object} 性能指标摘要
   */
  getSummary() {
    const summary = {
      metrics: {},
      memory: this.getMemoryUsage(),
      navigation: this.getNavigationTiming()
    }

    for (const [label, metric] of this.metrics.entries()) {
      summary.metrics[label] = {
        duration: metric.duration?.toFixed(2) + 'ms',
        startTime: metric.startTime?.toFixed(2) + 'ms'
      }
    }

    return summary
  }

  /**
   * 获取导航时序
   * @returns {Object|null} 导航时序信息
   */
  getNavigationTiming() {
    const timing = performance.getEntriesByType('navigation')[0]
    if (!timing) return null

    return {
      domContentLoaded: timing.domContentLoadedEventEnd - timing.domContentLoadedEventStart,
      loadComplete: timing.loadEventEnd - timing.loadEventStart,
      firstPaint: this.getFirstPaint(),
      totalLoadTime: timing.loadEventEnd - timing.fetchStart
    }
  }

  /**
   * 获取首次绘制时间
   * @returns {number|null} 首次绘制时间（毫秒）
   */
  getFirstPaint() {
    const paintEntries = performance.getEntriesByType('paint')
    const fp = paintEntries.find(entry => entry.name === 'first-paint')
    return fp ? fp.startTime.toFixed(2) : null
  }

  /**
   * 清除所有指标
   */
  clear() {
    this.metrics.clear()
  }

  /**
   * 断开所有观察者
   */
  disconnect() {
    this.observers.forEach(observer => observer.disconnect())
    this.observers = []
  }

  /**
   * 标记性能时间点
   * @param {string} name - 标记名称
   */
  mark(name) {
    if (!this.isEnabled) return
    performance.mark(name)
  }

  /**
   * 测量两个标记之间的时间
   * @param {string} name - 测量名称
   * @param {string} startMark - 起始标记
   * @param {string} endMark - 结束标记
   */
  measure(name, startMark, endMark) {
    if (!this.isEnabled) return
    try {
      performance.measure(name, startMark, endMark)
      const measure = performance.getEntriesByName(name)[0]
      this.log(name, measure.duration)
    } catch (e) {
      console.warn('[PerformanceMonitor] 测量失败:', e)
    }
  }
}

// 导出单例
export const performanceMonitor = new PerformanceMonitor()

// 导出便捷函数
export function startMeasure(label) {
  performanceMonitor.start(label)
}

export function endMeasure(label) {
  performanceMonitor.end(label)
}

export async function measureAsync(label, fn) {
  return performanceMonitor.measure(label, fn)
}

// Vue 3 性能指令
export const vPerformance = {
  mounted(el, binding) {
    const name = binding.value || el.tagName.toLowerCase()
    performanceMonitor.mark(`${name}-mounted`)
  },

  updated(el, binding) {
    const name = binding.value || el.tagName.toLowerCase()
    performanceMonitor.measure(`${name}-update`, `${name}-mounted`, performance.now())
  }
}
