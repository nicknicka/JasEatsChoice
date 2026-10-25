<template>
  <div class="uni-card-header" :style="headerStyle">
    <!-- 噪点纹理覆盖层 -->
    <div class="header-noise-overlay"></div>
    <div class="header-content">
      <div class="header-title">
        <span v-if="header.title?.icon" class="icon">{{ header.title.icon }}</span>
        <span class="title">{{ header.title?.text }}</span>
      </div>
      <div v-if="header.subtitle" class="header-subtitle">{{ header.subtitle }}</div>
    </div>
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
  position: relative;
  color: white;
  padding: 14px 20px;
  border-radius: 16px 16px 0 0;
  overflow: hidden;
}

/* SVG 噪点纹理 */
.header-noise-overlay {
  position: absolute;
  inset: 0;
  opacity: 0.06;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)'/%3E%3C/svg%3E");
  background-size: 128px 128px;
  pointer-events: none;
  z-index: 1;
}

.header-content {
  position: relative;
  z-index: 2;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 4px;
}

.icon {
  font-size: 22px;
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.15));
}

.title {
  line-height: 1.4;
  letter-spacing: 0.3px;
}

.header-subtitle {
  font-size: 13px;
  opacity: 0.85;
  margin-top: 2px;
  letter-spacing: 0.2px;
}
</style>
