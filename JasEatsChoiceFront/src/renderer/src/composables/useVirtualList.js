/**
 * 虚拟列表 Composable
 * 用于优化长列表渲染性能
 */
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { rafThrottle } from '../utils/performanceUtils'

export function useVirtualList(options = {}) {
  const {
    itemHeight = 80,           // 预估项目高度
    containerHeight = 0,       // 容器高度
    buffer = 5,                // 缓冲区大小
    overscan = 3               // 预渲染数量
  } = options

  // 状态
  const scrollTop = ref(0)
  const viewportHeight = ref(containerHeight)
  const containerRef = ref(null)
  const innerRef = ref(null)

  // 数据
  const listData = ref([])
  const itemHeights = ref(new Map()) // 存储实际项目高度

  // 计算总高度
  const totalHeight = computed(() => {
    let height = 0
    for (let i = 0; i < listData.value.length; i++) {
      height += itemHeights.value.get(i) || itemHeight
    }
    return height
  })

  // 计算可见范围
  const visibleRange = computed(() => {
    let startIndex = 0
    let endIndex = 0
    let offset = 0

    // 使用缓存的高度计算
    let currentOffset = 0
    for (let i = 0; i < listData.value.length; i++) {
      const itemHeightCached = itemHeights.value.get(i) || itemHeight

      if (currentOffset + itemHeightCached < scrollTop.value) {
        currentOffset += itemHeightCached
        continue
      }

      if (currentOffset >= scrollTop.value + viewportHeight.value) {
        break
      }

      if (startIndex === 0) {
        startIndex = Math.max(0, i - buffer)
        offset = currentOffset - (startIndex > 0 ? itemHeight * startIndex : 0)
      }

      endIndex = Math.min(listData.value.length, i + buffer + 1)
    }

    // 如果没有找到可见项，返回空范围
    if (startIndex === 0 && endIndex === 0 && listData.value.length > 0) {
      startIndex = 0
      endIndex = Math.min(buffer * 2, listData.value.length)
      offset = 0
    }

    return { startIndex, endIndex, offset }
  })

  // 可见数据
  const visibleData = computed(() => {
    const { startIndex, endIndex } = visibleRange.value
    return listData.value.slice(startIndex, endIndex).map((item, index) => ({
      data: item,
      index: startIndex + index
    }))
  })

  // 处理滚动
  const handleScroll = rafThrottle((e) => {
    scrollTop.value = e.target.scrollTop
  })

  // 更新项目高度
  const updateItemHeight = (index, height) => {
    if (itemHeights.value.get(index) !== height) {
      itemHeights.value.set(index, height)
    }
  }

  // 设置数据
  const setData = (data) => {
    listData.value = data
    // 清空高度缓存
    itemHeights.value.clear()
  }

  // 添加项目
  const addItem = (item) => {
    listData.value.push(item)
  }

  // 在末尾添加项目
  const pushItems = (items) => {
    listData.value.push(...items)
  }

  // 滚动到底部
  const scrollToBottom = () => {
    if (containerRef.value) {
      containerRef.value.scrollTop = totalHeight.value
    }
  }

  // 滚动到指定索引
  const scrollToIndex = (index) => {
    if (!containerRef.value || index < 0 || index >= listData.value.length) {
      return
    }

    let offset = 0
    for (let i = 0; i < index; i++) {
      offset += itemHeights.value.get(i) || itemHeight
    }

    containerRef.value.scrollTop = offset
  }

  // 获取项目信息
  const getItemInfo = (index) => {
    return {
      data: listData.value[index],
      height: itemHeights.value.get(index) || itemHeight,
      offset: Array.from({ length: index }, (_, i) =>
        itemHeights.value.get(i) || itemHeight
      ).reduce((sum, h) => sum + h, 0)
    }
  }

  // 更新容器高度
  const updateViewportHeight = () => {
    if (containerRef.value) {
      viewportHeight.value = containerRef.value.clientHeight
    }
  }

  // 重置
  const reset = () => {
    scrollTop.value = 0
    listData.value = []
    itemHeights.value.clear()
  }

  // 获取统计信息
  const getStats = () => {
    return {
      totalItems: listData.value.length,
      visibleItems: visibleRange.value.endIndex - visibleRange.value.startIndex,
      totalHeight: totalHeight.value,
      cachedHeights: itemHeights.value.size,
      cacheHitRate: (itemHeights.value.size / listData.value.length * 100).toFixed(2) + '%'
    }
  }

  // 生命周期
  onMounted(() => {
    updateViewportHeight()
    window.addEventListener('resize', updateViewportHeight)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', updateViewportHeight)
  })

  return {
    // 状态
    scrollTop,
    viewportHeight,
    containerRef,
    innerRef,
    listData,
    visibleData,
    visibleRange,
    totalHeight,

    // 方法
    handleScroll,
    updateItemHeight,
    setData,
    addItem,
    pushItems,
    scrollToBottom,
    scrollToIndex,
    getItemInfo,
    updateViewportHeight,
    reset,
    getStats
  }
}
