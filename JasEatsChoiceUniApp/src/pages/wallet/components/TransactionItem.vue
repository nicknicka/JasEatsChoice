<template>
  <view class="transaction-item" @click="handleClick">
    <!-- 交易图标 -->
    <view class="transaction-icon" :class="transaction.type">
      <text class="icon-text">{{ transaction.icon }}</text>
    </view>

    <!-- 交易信息 -->
    <view class="transaction-info">
      <text class="transaction-name">{{ transaction.name }}</text>
      <text class="transaction-time">{{ transaction.time }}</text>
    </view>

    <!-- 交易金额 -->
    <view class="transaction-amount" :class="transaction.type">
      <text class="amount-text">{{ transaction.type === 'income' ? '+' : '-' }}{{ transaction.amount }}</text>
      <text class="amount-status" v-if="transaction.status">{{ transaction.statusText }}</text>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  // 交易数据
  transaction: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['click'])

/**
 * 点击交易项
 */
const handleClick = () => {
  emit('click', props.transaction)
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.transaction-item {
  @include flex-center;
  padding: $spacing-md;
  background-color: $bg-color-white;
  border-bottom: 1rpx solid $border-color-lighter;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background-color: $bg-color-base;
  }
}

.transaction-icon {
  width: 72rpx;
  height: 72rpx;
  @include flex-center;
  border-radius: 50%;
  flex-shrink: 0;

  &.income {
    background-color: rgba(103, 194, 58, 0.1);
  }

  &.expense {
    background-color: rgba(255, 107, 53, 0.1);
  }
}

.icon-text {
  font-size: $font-size-xl;
}

.transaction-info {
  flex: 1;
  margin-left: $spacing-md;
  @include flex-center-column;
  align-items: flex-start;
  gap: $spacing-xs;
  min-width: 0;
}

.transaction-name {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $text-color-primary;
}

.transaction-time {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.transaction-amount {
  @include flex-center-column;
  align-items: flex-end;
  gap: $spacing-xs;

  &.income .amount-text {
    color: $success-color;
  }

  &.expense .amount-text {
    color: $text-color-primary;
  }
}

.amount-text {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
}

.amount-status {
  font-size: $font-size-xs;
  color: $text-color-placeholder;
  padding: 2rpx 8rpx;
  background-color: $bg-color-base;
  border-radius: $border-radius-round;
}
</style>
