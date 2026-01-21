<template>
  <div
    ref="floatBtnRef"
    class="floating-order-btn"
    :class="{ 'has-items': itemCount > 0, 'is-dragging': isDragging }"
    @click="handleClick"
    @mousedown="startDrag"
    @selectstart="handleSelectStart"
  >
    <!-- 波纹效果 -->
    <div class="ripple-effect"></div>
    <div class="ripple-effect ripple-effect-2"></div>

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

    <!-- 呼吸光环 -->
    <div class="breathing-ring" v-if="itemCount > 0"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ShoppingCart } from '@element-plus/icons-vue'

const props = defineProps({
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
.floating-order-btn {
  position: fixed;
  bottom: 100px;
  right: 50px;
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: move;
  box-shadow:
    0 4px 12px rgba(102, 126, 234, 0.4),
    0 0 0 0 rgba(102, 126, 234, 0.4);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1000;
  user-select: none;
  overflow: visible;

  // 波纹动画
  .ripple-effect {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 100%;
    height: 100%;
    border-radius: 50%;
    border: 2px solid rgba(255, 255, 255, 0.3);
    opacity: 0;
    animation: ripple 2s ease-out infinite;
  }

  .ripple-effect-2 {
    animation-delay: 1s;
  }

  @keyframes ripple {
    0% {
      width: 100%;
      height: 100%;
      opacity: 0.6;
    }
    100% {
      width: 200%;
      height: 200%;
      opacity: 0;
    }
  }

  // 呼吸光环
  .breathing-ring {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 100%;
    height: 100%;
    border-radius: 50%;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    opacity: 0.3;
    animation: breathing 2s ease-in-out infinite;
    z-index: -1;
  }

  @keyframes breathing {
    0%, 100% {
      transform: translate(-50%, -50%) scale(1);
      opacity: 0.3;
    }
    50% {
      transform: translate(-50%, -50%) scale(1.3);
      opacity: 0.1;
    }
  }

  &:hover {
    transform: scale(1.15);
    box-shadow:
      0 8px 24px rgba(102, 126, 234, 0.5),
      0 0 0 8px rgba(102, 126, 234, 0.1);

    .cart-icon {
      animation: shake 0.5s ease-in-out;
    }

    .tooltip {
      opacity: 1;
      visibility: visible;
      transform: translateX(-12px);
    }
  }

  &:active {
    transform: scale(1.05);
  }

  &.has-items {
    // 有商品时添加脉冲动画
    animation: pulse 2s ease-in-out infinite;
  }

  &.is-dragging {
    cursor: grabbing;
    transform: scale(1.05);
    box-shadow:
      0 12px 32px rgba(102, 126, 234, 0.6),
      0 0 0 12px rgba(102, 126, 234, 0.2);
    transition: none;
  }

  @keyframes pulse {
    0%, 100% {
      box-shadow:
        0 4px 12px rgba(102, 126, 234, 0.4),
        0 0 0 0 rgba(102, 126, 234, 0.4);
    }
    50% {
      box-shadow:
        0 4px 12px rgba(102, 126, 234, 0.4),
        0 0 0 12px rgba(102, 126, 234, 0.2);
    }
  }

  @keyframes shake {
    0%, 100% { transform: rotate(0deg); }
    25% { transform: rotate(-10deg); }
    75% { transform: rotate(10deg); }
  }

  .order-btn-inner {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1;

    .cart-icon {
      color: white;
      transition: all 0.3s;
    }

    .cart-count {
      position: absolute;
      top: -6px;
      right: -6px;
      background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
      color: #fff;
      border-radius: 50%;
      min-width: 24px;
      height: 24px;
      padding: 0 6px;
      font-size: 12px;
      font-weight: 700;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 3px solid #fff;
      box-shadow: 0 2px 8px rgba(245, 108, 108, 0.4);
      animation: bounce-in 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55);
    }

    @keyframes bounce-in {
      0% {
        transform: scale(0);
      }
      50% {
        transform: scale(1.2);
      }
      100% {
        transform: scale(1);
      }
    }

    // 悬浮提示
    .tooltip {
      position: absolute;
      right: calc(100% + 12px);
      top: 50%;
      transform: translateY(-50%) translateX(-8px);
      background: white;
      padding: 8px 12px;
      border-radius: 8px;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
      white-space: nowrap;
      opacity: 0;
      visibility: hidden;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      pointer-events: none;

      &::after {
        content: '';
        position: absolute;
        right: -6px;
        top: 50%;
        transform: translateY(-50%);
        width: 0;
        height: 0;
        border-left: 6px solid white;
        border-top: 6px solid transparent;
        border-bottom: 6px solid transparent;
      }

      .tooltip-text {
        display: block;
        font-size: 13px;
        font-weight: 500;
        color: #303133;
        margin-bottom: 2px;
      }

      .tooltip-count {
        display: block;
        font-size: 12px;
        color: #909399;
      }
    }
  }
}
</style>
