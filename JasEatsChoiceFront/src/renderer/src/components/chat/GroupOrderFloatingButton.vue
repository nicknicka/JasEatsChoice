<template>
  <div
    ref="floatBtnRef"
    class="floating-order-btn"
    @click="handleClick"
    @mousedown="startDrag"
    @selectstart="handleSelectStart"
  >
    <div class="order-btn-inner">
      <el-icon :size="24" color="white"><ShoppingCart /></el-icon>
      <span class="cart-count" v-if="itemCount > 0">{{ itemCount }}</span>
    </div>
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
  hasDragged.value = true
  if (isDragging.value && floatBtnRef.value) {
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
  startX.value = e.clientX - floatBtnRef.value.offsetLeft
  startY.value = e.clientY - floatBtnRef.value.offsetTop

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
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: move;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: transform 0.2s, box-shadow 0.2s;
  z-index: 1000;
  user-select: none;

  &:hover {
    transform: scale(1.1);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
  }

  &:active {
    transform: scale(1.05);
  }

  .order-btn-inner {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;

    .cart-count {
      position: absolute;
      top: -8px;
      right: -8px;
      background-color: #f56c6c;
      color: #fff;
      border-radius: 50%;
      width: 20px;
      height: 20px;
      font-size: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: bold;
      border: 2px solid #fff;
    }
  }
}
</style>
