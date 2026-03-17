<template>
  <view class="wallet-card">
    <view class="card-header">
      <text class="header-title">我的钱包</text>
      <text class="header-icon">💰</text>
    </view>

    <view class="balance-section">
      <text class="balance-label">账户余额（元）</text>
      <view class="balance-value">
        <text class="balance-amount">{{ balanceInteger }}</text>
        <text class="balance-unit">.{{ balanceDecimal }}</text>
      </view>
    </view>

    <view class="action-buttons">
      <button class="action-btn primary" @click="handleRecharge">
        <text class="btn-icon">💵</text>
        <text>充值</text>
      </button>
      <button class="action-btn outline" @click="handleWithdraw">
        <text class="btn-icon">🏦</text>
        <text>提现</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 余额（字符串或数字）
  balance: {
    type: [Number, String],
    default: 0
  }
})

const emit = defineEmits(['recharge', 'withdraw'])

/**
 * 整数部分
 */
const balanceInteger = computed(() => {
  const amount = parseFloat(props.balance || 0)
  return Math.floor(amount).toString()
})

/**
 * 小数部分
 */
const balanceDecimal = computed(() => {
  const amount = parseFloat(props.balance || 0)
  const decimal = (amount % 1).toFixed(2).substring(2)
  return decimal || '00'
})

/**
 * 充值
 */
const handleRecharge = () => {
  emit('recharge')
}

/**
 * 提现
 */
const handleWithdraw = () => {
  emit('withdraw')
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.wallet-card {
  background: linear-gradient(135deg, #FF6B35, #FF8F61);
  margin: $spacing-md;
  padding: $spacing-xl;
  border-radius: $border-radius-lg;
  box-shadow: $box-shadow-md;
}

.card-header {
  @include flex-between;
  align-items: center;
  margin-bottom: $spacing-xl;
}

.header-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: #fff;
}

.header-icon {
  font-size: 48rpx;
}

.balance-section {
  margin-bottom: $spacing-xl;
}

.balance-label {
  font-size: $font-size-sm;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: $spacing-sm;
}

.balance-value {
  @include flex-center;
  align-items: baseline;
}

.balance-amount {
  font-size: 64rpx;
  font-weight: $font-weight-bold;
  color: #fff;
  line-height: 1;
}

.balance-unit {
  font-size: $font-size-base;
  color: rgba(255, 255, 255, 0.8);
  margin-left: 4rpx;
}

.action-buttons {
  @include flex-center;
  gap: $spacing-md;
}

.action-btn {
  flex: 1;
  height: 80rpx;
  @include flex-center;
  gap: $spacing-sm;
  border-radius: $border-radius-round;
  font-size: $font-size-base;
  border: none;

  &.primary {
    background-color: #fff;
    color: $primary-color;
  }

  &.outline {
    background-color: transparent;
    color: #fff;
    border: 2rpx solid #fff;
  }

  &:active {
    transform: scale(0.98);
  }
}

.btn-icon {
  font-size: $font-size-xl;
}
</style>
