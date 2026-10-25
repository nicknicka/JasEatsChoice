<template>
  <view class="order-card" @click="handleClick">
    <!-- 订单头部 -->
    <view class="order-header">
      <view class="header-left">
        <text class="merchant-name">{{ order.merchantName }}</text>
        <text class="order-status" :class="statusClass">{{ order.statusText }}</text>
      </view>
      <text class="order-time">{{ order.timeText }}</text>
    </view>

    <!-- 订单内容 -->
    <view class="order-content">
      <!-- 菜品列表 -->
      <scroll-view class="dish-scroll" scroll-x v-if="order.items && order.items.length">
        <view class="dish-list">
          <view
            class="dish-item"
            v-for="(item, index) in order.items.slice(0, 4)"
            :key="index"
          >
            <image class="dish-image" :src="item.image" mode="aspectFill" />
            <view class="dish-info">
              <text class="dish-name">{{ item.name }}</text>
              <text class="dish-quantity">x{{ item.quantity }}</text>
            </view>
          </view>

          <!-- 更多提示 -->
          <view class="more-dishes" v-if="order.items.length > 4">
            <text class="more-text">共{{ order.items.length }}件</text>
            <text class="more-arrow">›</text>
          </view>
        </view>
      </scroll-view>

      <!-- 订单金额 -->
      <view class="order-amount">
        <text class="amount-label">实付</text>
        <text class="amount-value">¥{{ order.totalAmount }}</text>
      </view>
    </view>

    <!-- 订单底部操作 -->
    <view class="order-footer" v-if="order.actions && order.actions.length">
      <button
        class="action-btn"
        :class="action.class"
        v-for="(action, index) in order.actions"
        :key="index"
        @click.stop="handleAction(action)"
      >
        {{ action.text }}
      </button>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 订单数据
  order: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['click', 'action'])

/**
 * 状态样式类
 */
const statusClass = computed(() => {
  const statusMap = {
    pending: 'pending',
    confirmed: 'confirmed',
    preparing: 'preparing',
    delivering: 'delivering',
    completed: 'completed',
    cancelled: 'cancelled'
  }
  return statusMap[props.order.status] || ''
})

/**
 * 点击卡片
 */
const handleClick = () => {
  emit('click', props.order)
}

/**
 * 处理操作
 */
const handleAction = (action) => {
  emit('action', {
    action: action.type,
    order: props.order
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '../../styles/mixins.scss';

.order-card {
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: $spacing-md;
  margin-bottom: $spacing-md;
  box-shadow: $box-shadow-sm;

  &:active {
    background-color: $bg-color-base;
  }
}

.order-header {
  @include flex-between;
  align-items: center;
  margin-bottom: $spacing-md;
  padding-bottom: $spacing-md;
  border-bottom: 1rpx solid $border-color-lighter;
}

.header-left {
  @include flex-center;
  gap: $spacing-md;
}

.merchant-name {
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.order-status {
  padding: 4rpx 12rpx;
  border-radius: $border-radius-round;
  font-size: $font-size-xs;
  font-weight: $font-weight-medium;

  &.pending {
    background-color: rgba(255, 107, 53, 0.1);
    color: $primary-color;
  }

  &.confirmed,
  &.preparing {
    background-color: rgba(33, 150, 243, 0.1);
    color: #2196F3;
  }

  &.delivering {
    background-color: rgba(76, 175, 80, 0.1);
    color: $success-color;
  }

  &.completed {
    background-color: rgba(158, 158, 158, 0.1);
    color: $text-color-secondary;
  }

  &.cancelled {
    background-color: rgba(244, 67, 54, 0.1);
    color: $danger-color;
  }
}

.order-time {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.order-content {
  @include flex-between;
  margin-bottom: $spacing-md;
}

.dish-scroll {
  flex: 1;
  margin-right: $spacing-md;
}

.dish-list {
  @include flex-center;
  white-space: nowrap;
}

.dish-item {
  display: inline-flex;
  align-items: center;
  width: 200rpx;
  margin-right: $spacing-sm;
  vertical-align: top;
}

.dish-image {
  width: 80rpx;
  height: 80rpx;
  border-radius: $border-radius-base;
  flex-shrink: 0;
}

.dish-info {
  flex: 1;
  margin-left: $spacing-sm;
  @include flex-center-column;
  align-items: flex-start;
  gap: 4rpx;
  min-width: 0;
}

.dish-name {
  font-size: $font-size-sm;
  color: $text-color-primary;
  @include text-ellipsis;
}

.dish-quantity {
  font-size: $font-size-xs;
  color: $text-color-secondary;
}

.more-dishes {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100rpx;
  height: 100rpx;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  margin-left: $spacing-sm;
}

.more-text {
  font-size: $font-size-xs;
  color: $text-color-secondary;
}

.more-arrow {
  font-size: $font-size-xl;
  color: $text-color-placeholder;
  margin-top: 4rpx;
}

.order-amount {
  @include flex-center-column;
  align-items: flex-end;
  gap: 4rpx;
}

.amount-label {
  font-size: $font-size-xs;
  color: $text-color-secondary;
}

.amount-value {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $primary-color;
}

.order-footer {
  @include flex-center;
  justify-content: flex-end;
  gap: $spacing-sm;
  padding-top: $spacing-md;
  border-top: 1rpx solid $border-color-lighter;
}

.action-btn {
  padding: $spacing-xs $spacing-md;
  border-radius: $border-radius-round;
  font-size: $font-size-sm;
  border: none;

  &.primary {
    background-color: $primary-color;
    color: #fff;
  }

  &.secondary {
    background-color: $bg-color-base;
    color: $text-color-primary;
  }

  &.outline {
    background-color: transparent;
    color: $primary-color;
    border: 1rpx solid $primary-color;
  }

  &:active {
    transform: scale(0.98);
  }
}
</style>
