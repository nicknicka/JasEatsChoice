<template>
  <view class="stats-card">
    <view
      class="stat-item clickable"
      v-for="item in statsItems"
      :key="item.key"
      @click="handleClick(item.key)"
    >
      <text class="stat-value">{{ item.value }}</text>
      <text class="stat-label">{{ item.label }}</text>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

// Props
const props = defineProps({
  stats: {
    type: Object,
    default: () => ({
      orders: 0,
      favorites: 0,
      history: 0,
      coupons: 0
    })
  }
})

// Emits
const emit = defineEmits(['navigate'])

// 统计数据项配置
const statsConfig = [
  { key: 'orders', label: '订单' },
  { key: 'favorites', label: '收藏' },
  { key: 'history', label: '浏览' },
  { key: 'coupons', label: '优惠券' }
]

// 计算属性：统计数据项
const statsItems = computed(() => {
  return statsConfig.map(config => ({
    key: config.key,
    label: config.label,
    value: props.stats[config.key] || 0
  }))
})

/**
 * 点击统计项
 */
const handleClick = (key) => {
  emit('navigate', key)
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.stats-card {
  background-color: $bg-color-white;
  margin: 0 $spacing-md $spacing-md;
  border-radius: $border-radius-lg;
  padding: $spacing-lg $spacing-md;
  @include flex-around;
  box-shadow: $box-shadow-sm;
}

.stat-item {
  @include flex-center-column;
  gap: $spacing-xs;
  flex: 1;
  min-height: 96rpx;  // 触控目标最小尺寸
  cursor: pointer;
  transition: all 0.3s ease;

  // 可点击反馈
  &:active {
    transform: scale(0.95);
    opacity: 0.7;
  }
}

.stat-value {
  font-size: 40rpx;
  font-weight: $font-weight-bold;
  color: $primary-color;
  line-height: 1.2;
}

.stat-label {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}
</style>
