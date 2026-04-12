<template>
  <view class="order-status-grid">
    <view
      class="status-item clickable"
      v-for="status in orderStatuses"
      :key="status.key"
      @click="handleClick(status.key)"
    >
      <view class="status-icon-wrapper" :style="{ backgroundColor: status.bgColor }">
        <uni-icons
          :type="status.icon"
          :color="status.color"
          size="24"
        ></uni-icons>
        <!-- 红色角标（仅待支付显示） -->
        <view
          class="status-badge"
          v-if="status.showBadge && status.count > 0"
          :style="{ backgroundColor: status.badgeColor }"
        >
          <text class="badge-text">{{ formatCount(status.count) }}</text>
        </view>
      </view>
      <text class="status-text">{{ status.label }}</text>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { getCoreStatuses } from '@/config/order-status'

// Props
const props = defineProps({
  orderCounts: {
    type: Object,
    default: () => ({
      pending: 0,
      preparing: 0,
      completed: 0
    })
  }
})

// Emits
const emit = defineEmits(['status-click'])

// 计算属性：订单状态列表（包含数量）
const orderStatuses = computed(() => {
  const coreStatuses = getCoreStatuses()

  return coreStatuses.map(status => ({
    ...status,
    count: props.orderCounts[status.key] || 0
  }))
})

/**
 * 格式化数量显示（最大显示99+）
 */
const formatCount = (count) => {
  return count > 99 ? '99+' : count.toString()
}

/**
 * 点击状态
 */
const handleClick = (statusKey) => {
  emit('status-click', statusKey)
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.order-status-grid {
  @include flex-around;
}

.status-item {
  @include flex-center-column;
  gap: $spacing-sm;
  padding: $spacing-md;
  flex: 1;
  min-height: 120rpx;  // 触控目标最小尺寸
  cursor: pointer;
  transition: all 0.3s ease;

  // 可点击反馈
  &:active {
    transform: scale(0.95);
    opacity: 0.7;
  }
}

.status-icon-wrapper {
  position: relative;
  width: 88rpx;
  height: 88rpx;
  @include flex-center;
  border-radius: $border-radius-lg;
  transition: all 0.3s ease;
}

.status-badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  min-width: 32rpx;
  height: 32rpx;
  @include flex-center;
  padding: 0 8rpx;
  border-radius: $border-radius-round;
  border: 2rpx solid $bg-color-white;
  box-shadow: $box-shadow-sm;

  .badge-text {
    font-size: $font-size-xs;
    color: #FFFFFF;
    font-weight: $font-weight-bold;
    line-height: 1;
  }
}

.status-text {
  font-size: $font-size-sm;
  color: $text-color-regular;
  text-align: center;
}
</style>
