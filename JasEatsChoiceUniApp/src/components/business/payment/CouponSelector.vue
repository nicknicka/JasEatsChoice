<!--
组件名称：CouponSelector
用途：优惠券选择
复用情况：支付页面、订单确认页面
创建时间：2026-03-20
-->
<template>
  <view class="coupon-selector" @tap="$emit('select')">
    <view class="coupon-left">
      <uni-icons type="gift-filled" size="20" color="#FF6B35" />
      <text class="coupon-text">{{ displayText }}</text>
    </view>
    <view class="coupon-right">
      <text class="coupon-saving" v-if="selectedCoupon">-¥{{ selectedCoupon.discount }}</text>
      <uni-icons type="arrowright" size="16" color="#999" />
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  selectedCoupon: {
    type: Object,
    default: null
  },
  availableCount: {
    type: Number,
    default: 0
  }
})

defineEmits(['select'])

const displayText = computed(() => {
  if (props.selectedCoupon) {
    return props.selectedCoupon.name
  }
  return `选择优惠券（${props.availableCount}张可用）`
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.coupon-selector {
  background: #FFF7E6;
  border-radius: 16rpx;
  padding: 25rpx 30rpx;
  margin-bottom: 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.coupon-left {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.coupon-text {
  font-size: 26rpx;
  color: #FF6B35;
}

.coupon-right {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.coupon-saving {
  font-size: 28rpx;
  font-weight: bold;
  color: #52C41A;
}
</style>
