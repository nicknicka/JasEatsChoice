/**
 * 可拖拽元素 Composable
 * @description 提供元素拖拽功能，支持边界限制和点击/拖拽区分
 */
import { ref, onBeforeUnmount } from 'vue'

export function useDraggable(options = {}) {
  const {
    onDragStart = null,
    onDrag = null,
    onDragEnd = null,
    boundary = null // 边界元素选择器或window
  } = options

  // ========== 状态管理 ==========
  const targetRef = ref(null)
  const isDragging = ref(false)
  const hasDragged = ref(false) // 用于区分点击和拖拽
  const startX = ref(0)
  const startY = ref(0)
  const initialLeft = ref(0)
  const initialTop = ref(0)

  // 事件处理器引用（用于清理）
  let handleMouseMoveFn = null
  let handleMouseUpFn = null

  /**
   * 开始拖拽
   */
  const startDrag = (e) => {
    if (!targetRef.value) return

    isDragging.value = true
    hasDragged.value = false

    // 记录初始位置
    startX.value = e.clientX
    startY.value = e.clientY
    initialLeft.value = targetRef.value.offsetLeft
    initialTop.value = targetRef.value.offsetTop

    // 创建移动事件处理器
    handleMouseMoveFn = (moveEvent) => {
      handleDragMove(moveEvent)
    }

    // 创建结束事件处理器
    handleMouseUpFn = () => {
      stopDrag()
    }

    // 绑定到document以避免阻尼效果
    document.addEventListener('mousemove', handleMouseMoveFn)
    document.addEventListener('mouseup', handleMouseUpFn)

    // 防止文本选择
    e.preventDefault()

    // 触发拖拽开始回调
    if (onDragStart) {
      onDragStart(e)
    }
  }

  /**
   * 拖拽过程中
   */
  const handleDragMove = (e) => {
    if (!isDragging.value || !targetRef.value) return

    hasDragged.value = true

    // 计算新位置
    const deltaX = e.clientX - startX.value
    const deltaY = e.clientY - startY.value
    let newX = initialLeft.value + deltaX
    let newY = initialTop.value + deltaY

    // 获取边界
    const bounds = getBounds()

    // 限制在边界内
    if (bounds) {
      const elementWidth = targetRef.value.offsetWidth
      const elementHeight = targetRef.value.offsetHeight

      newX = Math.max(bounds.left, Math.min(newX, bounds.right - elementWidth))
      newY = Math.max(bounds.top, Math.min(newY, bounds.bottom - elementHeight))
    }

    // 更新位置
    targetRef.value.style.left = `${newX}px`
    targetRef.value.style.top = `${newY}px`
    targetRef.value.style.bottom = 'auto'
    targetRef.value.style.right = 'auto'

    // 触发拖拽回调
    if (onDrag) {
      onDrag(e, { x: newX, y: newY })
    }

    e.preventDefault()
  }

  /**
   * 停止拖拽
   */
  const stopDrag = () => {
    if (!isDragging.value) return

    isDragging.value = false

    // 移除事件监听器
    if (handleMouseMoveFn) {
      document.removeEventListener('mousemove', handleMouseMoveFn)
      handleMouseMoveFn = null
    }
    if (handleMouseUpFn) {
      document.removeEventListener('mouseup', handleMouseUpFn)
      handleMouseUpFn = null
    }

    // 触发拖拽结束回调
    if (onDragEnd) {
      onDragEnd(hasDragged.value)
    }
  }

  /**
   * 处理点击事件（区分点击和拖拽）
   */
  const handleClick = (callback) => {
    return (e) => {
      // 如果是拖拽操作后的松开，不触发点击
      if (hasDragged.value) {
        hasDragged.value = false
        return
      }

      // 只有在非拖拽状态下才触发点击
      if (!isDragging.value && callback) {
        callback(e)
      }
    }
  }

  /**
   * 获取边界范围
   */
  const getBounds = () => {
    if (!boundary) {
      // 默认使用窗口边界
      return {
        left: 0,
        top: 0,
        right: window.innerWidth,
        bottom: window.innerHeight
      }
    }

    if (typeof boundary === 'string') {
      // 如果是选择器，获取元素边界
      const element = document.querySelector(boundary)
      if (element) {
        const rect = element.getBoundingClientRect()
        return {
          left: rect.left,
          top: rect.top,
          right: rect.right,
          bottom: rect.bottom
        }
      }
    }

    return null
  }

  /**
   * 设置位置
   */
  const setPosition = (x, y) => {
    if (!targetRef.value) return

    targetRef.value.style.left = `${x}px`
    targetRef.value.style.top = `${y}px`
    targetRef.value.style.bottom = 'auto'
    targetRef.value.style.right = 'auto'
  }

  /**
   * 重置位置
   */
  const resetPosition = () => {
    if (!targetRef.value) return

    targetRef.value.style.left = ''
    targetRef.value.style.top = ''
    targetRef.value.style.bottom = ''
    targetRef.value.style.right = ''
  }

  /**
   * 防止文本选择
   */
  const handleSelectStart = (e) => {
    if (isDragging.value) {
      e.preventDefault()
    }
  }

  // 组件卸载时清理
  onBeforeUnmount(() => {
    stopDrag()
  })

  return {
    // 状态
    targetRef,
    isDragging,
    hasDragged,

    // 方法
    startDrag,
    stopDrag,
    handleClick,
    setPosition,
    resetPosition,
    handleSelectStart
  }
}
