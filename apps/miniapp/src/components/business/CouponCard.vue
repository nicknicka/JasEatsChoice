<template>
  <view class="coupon-card" :class="{ disabled: coupon.status !== 'available' }">
    <!-- 左侧金额 -->
    <view class="coupon-left">
      <view class="amount-section">
        <text class="amount-symbol">¥</text>
        <text class="amount-value">{{ coupon.amount }}</text>
      </view>
      <text class="amount-condition">{{ coupon.conditionText }}</text>
    </view>

    <!-- 分割线 -->
    <view class="coupon-divider">
      <view class="divider-circle top"></view>
      <view class="divider-line"></view>
      <view class="divider-circle bottom"></view>
    </view>

    <!-- 右侧信息 -->
    <view class="coupon-right">
      <text class="coupon-name">{{ coupon.name }}</text>
      <text class="coupon-time">{{ coupon.timeText }}</text>

      <!-- 操作按钮 -->
      <view class="coupon-action">
        <button
          class="action-btn"
          :class="actionClass"
          @click="handleAction"
          :disabled="coupon.status !== 'available'"
        >
          {{ actionText }}
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 优惠券数据
  coupon: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['use', 'receive'])

/**
 * 操作按钮样式
 */
const actionClass = computed(() => {
  switch (props.coupon.status) {
    case 'available':
      return 'primary'
    case 'used':
      return 'disabled'
    case 'expired':
      return 'disabled'
    case 'received':
      return 'secondary'
    default:
      return 'disabled'
  }
})

/**
 * 操作按钮文字
 */
const actionText = computed(() => {
  switch (props.coupon.status) {
    case 'available':
      return '立即使用'
    case 'used':
      return '已使用'
    case 'expired':
      return '已过期'
    case 'received':
      return '去使用'
    default:
      return '不可用'
  }
})

/**
 * 处理操作
 */
const handleAction = () => {
  if (props.coupon.status === 'available') {
    emit('use', props.coupon)
  } else if (props.coupon.status === 'received') {
    emit('use', props.coupon)
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '../../styles/mixins.scss';

.coupon-card {
  @include flex-center;
  background: linear-gradient(135deg, #FF6B35, #FF8F61);
  border-radius: $border-radius-lg;
  overflow: hidden;
  position: relative;

  &.disabled {
    background: linear-gradient(135deg, #BDBDBD, #9E9E9E);
  }
}

.coupon-left {
  width: 240rpx;
  @include flex-center-column;
  padding: $spacing-lg;
  background-color: rgba(255, 255, 255, 0.15);
  border-right: 2rpx dashed rgba(255, 255, 255, 0.3);
}

.amount-section {
  @include flex-center;
  align-items: baseline;
  margin-bottom: $spacing-xs;
}

.amount-symbol {
  font-size: $font-size-lg;
  color: #fff;
  font-weight: $font-weight-bold;
}

.amount-value {
  font-size: 64rpx;
  color: #fff;
  font-weight: $font-weight-bold;
  line-height: 1;
}

.amount-condition {
  font-size: $font-size-xs;
  color: rgba(255, 255, 255, 0.8);
  text-align: center;
}

.coupon-divider {
  position: absolute;
  left: 240rpx;
  top: 0;
  bottom: 0;
  width: 4rpx;
}

.divider-circle {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  width: 16rpx;
  height: 16rpx;
  background-color: $bg-color-base;
  border-radius: 50%;

  &.top {
    top: -8rpx;
  }

  &.bottom {
    bottom: -8rpx;
  }
}

.divider-line {
  position: absolute;
  top: 8rpx;
  bottom: 8rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 2rpx;
  background: repeating-linear-gradient(
    to bottom,
    rgba(255, 255, 255, 0.3) 0,
    rgba(255, 255, 255, 0.3) 8rpx,
    transparent 8rpx,
    transparent 16rpx
  );
}

.coupon-right {
  flex: 1;
  padding: $spacing-lg;
  @include flex-center-column;
  align-items: flex-start;
  gap: $spacing-xs;
  position: relative;
}

.coupon-name {
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
  color: #fff;
  @include text-ellipsis;
}

.coupon-time {
  font-size: $font-size-sm;
  color: rgba(255, 255, 255, 0.8);
}

.coupon-action {
  position: absolute;
  top: 50%;
  right: $spacing-lg;
  transform: translateY(-50%);
}

.action-btn {
  padding: $spacing-xs $spacing-md;
  border-radius: $border-radius-round;
  font-size: $font-size-sm;
  border: none;

  &.primary {
    background-color: #fff;
    color: $primary-color;
  }

  &.secondary {
    background-color: rgba(255, 255, 255, 0.2);
    color: #fff;
  }

  &.disabled {
    background-color: rgba(0, 0, 0, 0.2);
    color: rgba(255, 255, 255, 0.6);
  }
}
</style>
