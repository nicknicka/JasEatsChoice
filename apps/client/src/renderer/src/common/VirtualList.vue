<template>
  <div
    ref="containerRef"
    class="virtual-list-container"
    :style="{ height: containerHeight }"
    @scroll="handleScroll"
  >
    <div
      class="virtual-list-phantom"
      :style="{ height: totalHeight + 'px' }"
    ></div>
    <div
      class="virtual-list-content"
      :style="{ transform: `translateY(${offset}px)` }"
    >
      <div
        v-for="item in visibleData"
        :key="item[keyField]"
        class="virtual-list-item"
        :style="{
          height: estimatedItemHeight + 'px',
          minHeight: estimatedItemHeight + 'px'
        }"
      >
        <slot :item="item" :index="item._index" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { rafThrottle } from '../../utils/performanceUtils.js'

const props = defineProps({
  // 数据列表
  data: {
    type: Array,
    required: true,
    default: () => []
  },
  // 预估项目高度
  estimatedItemHeight: {
    type: Number,
    default: 80
  },
  // 容器高度
  containerHeight: {
    type: String,
    default: '100%'
  },
  // 缓冲区数量（上下各渲染多少个额外项目）
  buffer: {
    type: Number,
    default: 5
  },
  // 唯一标识字段
  keyField: {
    type: String,
    default: 'id'
  }
})

const emit = defineEmits(['scroll'])

const containerRef = ref(null)
const scrollTop = ref(0)
const viewportHeight = ref(0)

// 计算总高度
const totalHeight = computed(() => {
  return props.data.length * props.estimatedItemHeight
})

// 计算可见区域的起始和结束索引
const startIndex = computed(() => {
  const index = Math.floor(scrollTop.value / props.estimatedItemHeight)
  return Math.max(0, index - props.buffer)
})

const endIndex = computed(() => {
  const index = Math.ceil((scrollTop.value + viewportHeight.value) / props.estimatedItemHeight)
  return Math.min(props.data.length, index + props.buffer)
})

// 计算偏移量
const offset = computed(() => {
  return startIndex.value * props.estimatedItemHeight
})

// 可见数据
const visibleData = computed(() => {
  return props.data.slice(startIndex.value, endIndex.value).map((item, index) => ({
    ...item,
    _index: startIndex.value + index
  }))
})

// 处理滚动
const handleScroll = rafThrottle((e) => {
  scrollTop.value = e.target.scrollTop
  emit('scroll', e)
})

// 更新视口高度
const updateViewportHeight = () => {
  if (containerRef.value) {
    viewportHeight.value = containerRef.value.clientHeight
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (containerRef.value) {
    containerRef.value.scrollTop = totalHeight.value
  }
}

// 滚动到指定索引
const scrollToIndex = (index) => {
  if (containerRef.value) {
    containerRef.value.scrollTop = index * props.estimatedItemHeight
  }
}

// 监听数据变化
watch(() => props.data.length, () => {
  nextTick(() => {
    updateViewportHeight()
  })
})

onMounted(() => {
  updateViewportHeight()
  window.addEventListener('resize', updateViewportHeight)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateViewportHeight)
})

defineExpose({
  scrollToBottom,
  scrollToIndex,
  containerRef
})
</script>

<style scoped lang="less">
.virtual-list-container {
  position: relative;
  overflow: auto;
  -webkit-overflow-scrolling: touch;
  will-change: scroll-position;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: #dee2e6;
    border-radius: 3px;

    &:hover {
      background: #adb5bd;
    }
  }
}

.virtual-list-phantom {
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
  z-index: -1;
}

.virtual-list-content {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  will-change: transform;
}

.virtual-list-item {
  box-sizing: border-box;
  overflow: hidden;
}
</style>
