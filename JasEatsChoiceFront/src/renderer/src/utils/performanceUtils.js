/**
 * 性能优化工具函数
 */

/**
 * 防抖函数
 * @param {Function} func - 要防抖的函数
 * @param {number} wait - 等待时间（毫秒）
 * @param {boolean} immediate - 是否立即执行
 * @returns {Function} 防抖后的函数
 */
export function debounce(func, wait = 300, immediate = false) {
  let timeout

  return function executedFunction(...args) {
    const context = this

    const later = () => {
      timeout = null
      if (!immediate) func.apply(context, args)
    }

    const callNow = immediate && !timeout

    clearTimeout(timeout)
    timeout = setTimeout(later, wait)

    if (callNow) func.apply(context, args)
  }
}

/**
 * 节流函数
 * @param {Function} func - 要节流的函数
 * @param {number} limit - 限制时间（毫秒）
 * @returns {Function} 节流后的函数
 */
export function throttle(func, limit = 300) {
  let inThrottle

  return function executedFunction(...args) {
    const context = this

    if (!inThrottle) {
      func.apply(context, args)
      inThrottle = true
      setTimeout(() => (inThrottle = false), limit)
    }
  }
}

/**
 * 请求AnimationFrame节流（用于滚动等高频事件）
 * @param {Function} func - 要节流的函数
 * @returns {Function} 节流后的函数
 */
export function rafThrottle(func) {
  let rafId = null

  return function executedFunction(...args) {
    const context = this

    if (rafId === null) {
      rafId = requestAnimationFrame(() => {
        func.apply(context, args)
        rafId = null
      })
    }
  }
}

/**
 * 批量更新（减少渲染次数）
 * @param {Function} callback - 回调函数
 * @param {number} delay - 延迟时间（毫秒）
 * @returns {Function} 批量更新函数
 */
export function batchUpdate(callback, delay = 0) {
  let timer = null
  let pendingArgs = []

  return function (...args) {
    pendingArgs.push(args)

    if (timer === null) {
      timer = setTimeout(() => {
        callback(pendingArgs)
        pendingArgs = []
        timer = null
      }, delay)
    }
  }
}

/**
 * 懒加载图片
 * @param {string} src - 图片地址
 * @param {Object} options - 配置选项
 * @returns {Promise<HTMLImageElement>} 图片元素
 */
export function lazyLoadImage(src, options = {}) {
  return new Promise((resolve, reject) => {
    const img = new Image()

    img.onload = () => resolve(img)
    img.onerror = () => reject(new Error(`Failed to load image: ${src}`))

    if (options.crossorigin) {
      img.crossOrigin = options.crossorigin
    }

    img.src = src
  })
}

/**
 * 检测元素是否在视口中
 * @param {HTMLElement} element - 要检测的元素
 * @param {number} threshold - 阈值（0-1）
 * @returns {boolean} 是否在视口中
 */
export function isInViewport(element, threshold = 0.1) {
  const rect = element.getBoundingClientRect()
  const windowHeight = window.innerHeight || document.documentElement.clientHeight
  const windowWidth = window.innerWidth || document.documentElement.clientWidth

  const vertInView = rect.top <= windowHeight && rect.top + rect.height >= 0
  const horInView = rect.left <= windowWidth && rect.left + rect.width >= 0

  const visibleHeight = Math.min(rect.height, windowHeight - rect.top, rect.bottom)
  const visibleWidth = Math.min(rect.width, windowWidth - rect.left, rect.right)
  const visibleArea = visibleHeight * visibleWidth
  const totalArea = rect.height * rect.width
  const visiblePercentage = visibleArea / totalArea

  return vertInView && horInView && visiblePercentage >= threshold
}

/**
 * 空闲时执行任务（利用requestIdleCallback）
 * @param {Function} callback - 要执行的回调
 * @param {Object} options - 配置选项
 * @returns {number} 任务ID
 */
export function runWhenIdle(callback, options = {}) {
  if ('requestIdleCallback' in window) {
    return window.requestIdleCallback(callback, options)
  } else {
    // 降级方案：使用setTimeout
    return setTimeout(callback, 1)
  }
}

/**
 * 取消空闲任务
 * @param {number} id - 任务ID
 */
export function cancelIdleCallback(id) {
  if ('cancelIdleCallback' in window) {
    window.cancelIdleCallback(id)
  } else {
    clearTimeout(id)
  }
}
