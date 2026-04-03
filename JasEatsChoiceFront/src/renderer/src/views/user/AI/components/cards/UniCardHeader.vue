<template>
  <div class="uni-card-header" :style="headerStyle">
    <div class="header-title">
      <span v-if="header.title?.icon" class="icon">{{ header.title.icon }}</span>
      <span class="title">{{ header.title?.text }}</span>
    </div>
    <div v-if="header.subtitle" class="header-subtitle">{{ header.subtitle }}</div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  header: {
    type: Object,
    required: true
  }
})

// 预设主题色映射
const THEME_MAP = {
  dish: 'linear-gradient(135deg, #ff6b6b, #ff9f43)',
  order: 'linear-gradient(135deg, #ff6b6b, #c44569)',
  health: 'linear-gradient(135deg, #667eea, #764ba2)',
  user: 'linear-gradient(135deg, #ff6b6b, #e17055)',
  coupon: 'linear-gradient(135deg, #ff6b6b, #f8a5c2)',
  error: 'linear-gradient(135deg, #ef4444, #dc2626)',
  default: 'linear-gradient(135deg, #667eea, #764ba2)'
}

// 计算头部样式
const headerStyle = computed(() => {
  const theme = props.header.theme || 'default'
  const gradient = THEME_MAP[theme] || THEME_MAP.default

  return {
    background: props.header.background || gradient
  }
})
</script>

<style scoped>
.uni-card-header {
  color: white;
  padding: 16px 20px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 4px;
}

.icon {
  font-size: 24px;
}

.title {
  line-height: 1.4;
}

.header-subtitle {
  font-size: 14px;
  opacity: 0.9;
  margin-top: 2px;
}
</style>
