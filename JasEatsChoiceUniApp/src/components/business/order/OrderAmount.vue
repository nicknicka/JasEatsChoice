<!--
组件名称：OrderAmount
用途：订单金额明细
复用情况：订单详情、订单确认
创建时间：2026-03-20
-->
<template>
  <view class="order-amount">
    <view class="section-title">金额明细</view>
    <view class="amount-item">
      <text class="amount-label">商品金额</text>
      <text class="amount-value">¥{{ amount.dishPrice }}</text>
    </view>
    <view class="amount-item">
      <text class="amount-label">配送费</text>
      <text class="amount-value">¥{{ amount.deliveryFee }}</text>
    </view>
    <view class="amount-item">
      <text class="amount-label">包装费</text>
      <text class="amount-value">¥{{ amount.packingFee }}</text>
    </view>
    <view class="amount-item" v-if="amount.couponDiscount > 0">
      <text class="amount-label">优惠券</text>
      <text class="amount-value discount">-¥{{ amount.couponDiscount }}</text>
    </view>
    <view class="amount-item total">
      <text class="amount-label">实付金额</text>
      <text class="amount-value final">¥{{ amount.totalPrice }}</text>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  amount: {
    type: Object,
    required: true
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.order-amount {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.amount-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;

  &.total {
    border-top: 1rpx solid #f5f5f5;
    margin-top: 10rpx;
    padding-top: 20rpx;
  }
}

.amount-label {
  font-size: 26rpx;
  color: #666;
}

.amount-value {
  font-size: 26rpx;
  color: #333;

  &.discount {
    color: #52C41A;
  }

  &.final {
    font-size: 32rpx;
    font-weight: bold;
    color: #FF6B35;
  }
}
</style>
