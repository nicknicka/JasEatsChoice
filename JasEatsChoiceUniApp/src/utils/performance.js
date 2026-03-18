/**
 * 性能优化工具函数
 * 包括图片懒加载、列表虚拟化、防抖节流等
 */

import { ref, onMounted, onUnmounted, nextTick } from 'vue'

/**
 * 图片懒加载 Composable
 * @param {String} defaultImage - 默认图片
 * @param {Number} threshold - 触发阈值（px）
 */
export function useLazyLoad(defaultImage = '/static/images/placeholder.png', threshold = 200) {
  const loadedImages = ref(new Set())

  /**
   * 检查图片是否在可视区域
   */
  const checkInView = (element) => {
    if (!element) return false

    const rect = element.getBoundingClientRect()
    const windowHeight = uni.getSystemInfoSync().windowHeight

    return rect.top <= windowHeight + threshold && rect.bottom >= -threshold
  }

  /**
   * 加载图片
   */
  const loadImage = (src, element) => {
    if (loadedImages.value.has(src)) return

    if (checkInView(element)) {
      const img = new Image()
      img.onload = () => {
        loadedImages.value.add(src)
      }
      img.src = src
    }
  }

  /**
   * 获取图片显示URL
   */
  const getImageUrl = (src) => {
    return loadedImages.value.has(src) ? src : defaultImage
  }

  return {
    loadedImages,
    loadImage,
    getImageUrl,
    checkInView
  }
}

/**
 * 列表虚拟化 Composable
 * @param {Array} list - 数据列表
 * @param {Number} itemHeight - 单项高度
 * @param {Number} visibleCount - 可见数量
 */
export function useVirtualList(list, itemHeight = 100, visibleCount = 10) {
  const scrollTop = ref(0)
  const containerHeight = ref(0)

  // 计算可视区域的起始索引和结束索引
  const range = computed(() => {
    const start = Math.floor(scrollTop.value / itemHeight)
    const end = start + visibleCount

    return {
      start: Math.max(0, start - 5), // 预加载5条
      end: Math.min(list.value.length, end + 5) // 预加载5条
    }
  })

  // 可见列表数据
  const visibleList = computed(() => {
    return list.value.slice(range.value.start, range.value.end)
  })

  // 列表总高度
  const totalHeight = computed(() => {
    return list.value.length * itemHeight
  })

  // 偏移量
  const offsetY = computed(() => {
    return range.value.start * itemHeight
  })

  /**
   * 滚动事件处理
   */
  const onScroll = (e) => {
    scrollTop.value = e.detail.scrollTop
  }

  return {
    scrollTop,
    containerHeight,
    range,
    visibleList,
    totalHeight,
    offsetY,
    onScroll
  }
}

/**
 * 防抖函数
 * @param {Function} fn - 要执行的函数
 * @param {Number} delay - 延迟时间
 */
export function useDebounce(fn, delay = 300) {
  let timer = null

  return function (...args) {
    if (timer) clearTimeout(timer)

    timer = setTimeout(() => {
      fn.apply(this, args)
      timer = null
    }, delay)
  }
}

/**
 * 节流函数
 * @param {Function} fn - 要执行的函数
 * @param {Number} interval - 间隔时间
 */
export function useThrottle(fn, interval = 300) {
  let lastTime = 0

  return function (...args) {
    const now = Date.now()

    if (now - lastTime >= interval) {
      fn.apply(this, args)
      lastTime = now
    }
  }
}

/**
 * 请求动画帧节流
 * @param {Function} fn - 要执行的函数
 */
export function useRafThrottle(fn) {
  let pending = false

  return function (...args) {
    if (pending) return

    pending = true
    requestAnimationFrame(() => {
      fn.apply(this, args)
      pending = false
    })
  }
}

/**
 * 无限滚动 Composable
 * @param {Function} loadData - 加载数据函数
 * @param {Number} threshold - 触发阈值（px）
 */
export function useInfiniteScroll(loadData, threshold = 100) {
  const loading = ref(false)
  const finished = ref(false)
  const error = ref(null)

  /**
   * 检查是否触底
   */
  const checkBottom = (e) => {
    if (loading.value || finished.value) return

    const { scrollTop, scrollHeight, clientHeight } = e.detail
    const distance = scrollHeight - scrollTop - clientHeight

    if (distance <= threshold) {
      loadMore()
    }
  }

  /**
   * 加载更多
   */
  const loadMore = async () => {
    if (loading.value || finished.value) return

    loading.value = true
    error.value = null

    try {
      const hasMore = await loadData()
      finished.value = !hasMore
    } catch (err) {
      error.value = err
      console.error('加载数据失败:', err)
    } finally {
      loading.value = false
    }
  }

  /**
   * 重置状态
   */
  const reset = () => {
    loading.value = false
    finished.value = false
    error.value = null
  }

  return {
    loading,
    finished,
    error,
    checkBottom,
    loadMore,
    reset
  }
}

/**
 * 下拉刷新 Composable
 * @param {Function} onRefresh - 刷新函数
 */
export function usePullRefresh(onRefresh) {
  const refreshing = ref(false)

  /**
   * 刷新
   */
  const refresh = async () => {
    refreshing.value = true

    try {
      await onRefresh()
    } catch (err) {
      console.error('刷新失败:', err)
    } finally {
      // 延迟停止刷新动画
      setTimeout(() => {
        refreshing.value = false
      }, 300)
    }
  }

  return {
    refreshing,
    refresh
  }
}

/**
 * 图片压缩
 * @param {String} filePath - 图片路径
 * @param {Number} quality - 压缩质量（0-100）
 * @param {Number} maxWidth - 最大宽度
 */
export function compressImage(filePath, quality = 80, maxWidth = 1080) {
  return new Promise((resolve, reject) => {
    uni.getImageInfo({
      src: filePath,
      success: (info) => {
        const { width, height } = info

        // 如果宽度大于最大宽度，按比例压缩
        let targetWidth = width
        let targetHeight = height

        if (width > maxWidth) {
          targetWidth = maxWidth
          targetHeight = (maxWidth / width) * height
        }

        // 使用canvas压缩
        const canvas = uni.createCanvasContext('compressCanvas')
        canvas.drawImage(filePath, 0, 0, targetWidth, targetHeight)
        canvas.draw(false, () => {
          uni.canvasToTempFilePath({
            canvasId: 'compressCanvas',
            fileType: 'jpg',
            quality: quality / 100,
            success: (res) => {
              resolve(res.tempFilePath)
            },
            fail: reject
          })
        })
      },
      fail: reject
    })
  })
}

/**
 * 批量操作 Composable
 * @param {Array} list - 数据列表
 * @param {Function} operation - 操作函数
 */
export function useBatchOperation(list, operation) {
  const selectedItems = ref(new Set())
  const isAllSelected = computed(() => {
    return list.value.length > 0 && selectedItems.value.size === list.value.length
  })

  /**
   * 切换选中状态
   */
  const toggleSelect = (item) => {
    if (selectedItems.value.has(item)) {
      selectedItems.value.delete(item)
    } else {
      selectedItems.value.add(item)
    }
  }

  /**
   * 全选/取消全选
   */
  const toggleSelectAll = () => {
    if (isAllSelected.value) {
      selectedItems.value.clear()
    } else {
      list.value.forEach(item => selectedItems.value.add(item))
    }
  }

  /**
   * 批量操作
   */
  const batchOperate = async () => {
    if (selectedItems.value.size === 0) {
      uni.showToast({
        title: '请先选择项目',
        icon: 'none'
      })
      return
    }

    try {
      await operation(Array.from(selectedItems.value))

      uni.showToast({
        title: '操作成功',
        icon: 'success'
      })

      selectedItems.value.clear()
    } catch (err) {
      uni.showToast({
        title: '操作失败',
        icon: 'error'
      })
    }
  }

  /**
   * 清空选择
   */
  const clearSelection = () => {
    selectedItems.value.clear()
  }

  return {
    selectedItems,
    isAllSelected,
    toggleSelect,
    toggleSelectAll,
    batchOperate,
    clearSelection
  }
}

/**
 * 性能监控 Composable
 */
export function usePerformanceMonitor() {
  const metrics = ref({
    fps: 0,
    memory: 0,
    loadTime: 0
  })

  let fpsTimer = null
  let frameCount = 0
  let lastTime = Date.now()

  /**
   * 开始监控FPS
   */
  const startFPSMonitor = () => {
    frameCount = 0
    lastTime = Date.now()

    const countFrame = () => {
      frameCount++
      const now = Date.now()
      const diff = now - lastTime

      if (diff >= 1000) {
        metrics.value.fps = Math.round((frameCount * 1000) / diff)
        frameCount = 0
        lastTime = now
      }

      fpsTimer = requestAnimationFrame(countFrame)
    }

    countFrame()
  }

  /**
   * 停止监控FPS
   */
  const stopFPSMonitor = () => {
    if (fpsTimer) {
      cancelAnimationFrame(fpsTimer)
      fpsTimer = null
    }
  }

  /**
   * 获取内存信息
   */
  const getMemoryInfo = () => {
    const info = uni.getPerformance()
    if (info && info.memory) {
      metrics.value.memory = info.memory.usedJSHeapSize / 1024 / 1024 // MB
    }
  }

  /**
   * 记录页面加载时间
   */
  const recordLoadTime = (startTime) => {
    metrics.value.loadTime = Date.now() - startTime
  }

  onMounted(() => {
    startFPSMonitor()

    // 定期获取内存信息
    setInterval(() => {
      getMemoryInfo()
    }, 5000)
  })

  onUnmounted(() => {
    stopFPSMonitor()
  })

  return {
    metrics,
    recordLoadTime,
    getMemoryInfo
  }
}
