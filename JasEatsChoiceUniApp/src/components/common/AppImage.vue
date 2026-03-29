<template>
  <image
    :src="processedSrc"
    :mode="mode"
    :lazy-load="lazyLoad"
    :class="customClass"
    @error="handleError"
    @load="handleLoad"
  />
</template>

<script setup>
import { computed } from 'vue'
import { processImageUrl } from '@/utils/helper'

const props = defineProps({
  src: {
    type: String,
    default: ''
  },
  mode: {
    type: String,
    default: 'aspectFill'
  },
  lazyLoad: {
    type: Boolean,
    default: true
  },
  customClass: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['error', 'load'])

// 处理图片URL
const processedSrc = computed(() => processImageUrl(props.src))

// 错误处理
const handleError = (e) => {
  console.error('图片加载失败:', props.src, e)
  emit('error', e)
}

// 加载成功
const handleLoad = (e) => {
  emit('load', e)
}
</script>

<style scoped>
image {
  width: 100%;
  height: 100%;
  display: block;
}
</style>
