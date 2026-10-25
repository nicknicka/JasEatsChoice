<template>
  <div
    ref="floatBtnRef"
    class="floating-order-btn"
    :class="{ 'has-items': itemCount > 0, 'is-dragging': isDragging }"
    @click="handleClick"
    @mousedown="startDrag"
    @selectstart="handleSelectStart"
  >
    <div class="order-btn-inner">
      <el-icon :size="26" class="cart-icon"><ShoppingCart /></el-icon>
      <span class="cart-count" v-if="itemCount > 0">
        {{ itemCount > 99 ? '99+' : itemCount }}
      </span>

      <!-- 悬浮提示 -->
      <div class="tooltip" v-if="itemCount > 0">
        <span class="tooltip-text">查看订单</span>
        <span class="tooltip-count">{{ itemCount }} 件商品</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onBeforeUnmount } from 'vue'
import { ShoppingCart } from '@element-plus/icons-vue'

defineProps({
  itemCount: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['click'])

const floatBtnRef = ref(null)
const isDragging = ref(false)
const hasDragged = ref(false)
const startX = ref(0)
const startY = ref(0)
const initialMouseX = ref(0)
const initialMouseY = ref(0)

const DRAG_THRESHOLD = 5 // 拖动阈值（像素）

let handleMouseMoveFn = null
let handleMouseUpFn = null

const handleClick = () => {
  if (hasDragged.value) {
    hasDragged.value = false
    return
  }

  if (!isDragging.value) {
    emit('click')
  }
}

const onDrag = (e) => {
  if (!isDragging.value || !floatBtnRef.value) return

  // 计算鼠标移动的距离
  const deltaX = Math.abs(e.clientX - initialMouseX.value)
  const deltaY = Math.abs(e.clientY - initialMouseY.value)

  // 只有移动超过阈值才认为是拖动
  if (deltaX > DRAG_THRESHOLD || deltaY > DRAG_THRESHOLD) {
    hasDragged.value = true

    const floatBtn = floatBtnRef.value
    let newX = e.clientX - startX.value
    let newY = e.clientY - startY.value

    const windowWidth = window.innerWidth
    const windowHeight = window.innerHeight
    const btnWidth = floatBtn.offsetWidth
    const btnHeight = floatBtn.offsetHeight

    newX = Math.max(0, Math.min(newX, windowWidth - btnWidth))
    newY = Math.max(0, Math.min(newY, windowHeight - btnHeight))

    floatBtn.style.left = newX + 'px'
    floatBtn.style.top = newY + 'px'
    floatBtn.style.bottom = 'auto'
    floatBtn.style.right = 'auto'

    e.preventDefault()
  }
}

const startDrag = (e) => {
  if (!floatBtnRef.value) return

  isDragging.value = true
  hasDragged.value = false
  startX.value = e.clientX - floatBtnRef.value.offsetLeft
  startY.value = e.clientY - floatBtnRef.value.offsetTop
  initialMouseX.value = e.clientX
  initialMouseY.value = e.clientY

  handleMouseMoveFn = (moveEvent) => {
    onDrag(moveEvent)
  }

  handleMouseUpFn = () => {
    stopDrag()
  }

  document.addEventListener('mousemove', handleMouseMoveFn)
  document.addEventListener('mouseup', handleMouseUpFn)

  e.preventDefault()
}

const stopDrag = () => {
  isDragging.value = false

  if (handleMouseMoveFn) {
    document.removeEventListener('mousemove', handleMouseMoveFn)
    handleMouseMoveFn = null
  }
  if (handleMouseUpFn) {
    document.removeEventListener('mouseup', handleMouseUpFn)
    handleMouseUpFn = null
  }
}

const handleSelectStart = (e) => {
  e.preventDefault()
}

onBeforeUnmount(() => {
  stopDrag()
})
</script>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';

.floating-order-btn {
  position: fixed;
  bottom: 100px;
  right: 38px;
  width: 72px;
  height: 72px;
  background:
    linear-gradient(180deg, #fffaf4 0%, #f5e5d8 100%);
  border-radius: 22px;
  border: 1px solid fade(@nordic-accent, 26%);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: move;
  box-shadow:
    0 18px 32px rgba(117, 82, 55, 0.14),
    inset 0 1px 0 rgba(255, 255, 255, 0.78);
  transition: all @nordic-transition-base ease;
  z-index: 1000;
  user-select: none;
  overflow: visible;

  &:hover {
    transform: translateY(-2px);
    box-shadow:
      0 22px 38px rgba(117, 82, 55, 0.18),
      0 0 0 8px fade(@nordic-accent-light, 26%);

    .tooltip {
      opacity: 1;
      visibility: visible;
      transform: translateY(-50%) translateX(-6px);
    }
  }

  &:active {
    transform: translateY(0);
  }

  &.has-items {
    border-color: fade(@nordic-accent, 34%);
  }

  &.is-dragging {
    cursor: grabbing;
    transform: scale(1.05);
    box-shadow:
      0 22px 38px rgba(117, 82, 55, 0.2),
      0 0 0 12px fade(@nordic-accent-light, 22%);
    transition: none;
  }

  .order-btn-inner {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1;

    .cart-icon {
      color: @nordic-accent-dark;
      transition: color @nordic-transition-fast ease;
    }

    .cart-count {
      position: absolute;
      top: -16px;
      right: -14px;
      background: @nordic-red;
      color: #fff;
      border-radius: 50%;
      width: 24px;
      height: 24px;
      padding: 0;
      font-size: @nordic-text-xs;
      font-weight: 700;
      line-height: 24px;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 2px solid @nordic-surface;
      box-shadow:
        0 8px 16px fade(@nordic-red, 22%);
      z-index: 10;
    }

    .tooltip {
      position: absolute;
      right: calc(100% + 12px);
      top: 50%;
      transform: translateY(-50%) translateX(-10px);
      background: @nordic-surface;
      border: 1px solid @nordic-border;
      padding: 10px 12px;
      border-radius: 12px;
      box-shadow: 0 14px 28px rgba(105, 78, 57, 0.14);
      white-space: nowrap;
      opacity: 0;
      visibility: hidden;
      transition: all @nordic-transition-fast ease;
      pointer-events: none;

      &::after {
        content: '';
        position: absolute;
        right: -6px;
        top: 50%;
        transform: translateY(-50%);
        width: 0;
        height: 0;
        border-left: 6px solid @nordic-surface;
        border-top: 6px solid transparent;
        border-bottom: 6px solid transparent;
      }

      .tooltip-text {
        display: block;
        font-size: @nordic-text-sm;
        font-weight: 600;
        color: @nordic-text;
        margin-bottom: 2px;
      }

      .tooltip-count {
        display: block;
        font-size: @nordic-text-xs;
        color: @nordic-text-muted;
      }
    }
  }
}
</style>
