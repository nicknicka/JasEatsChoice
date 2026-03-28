<!--
组件名称：OrderInfoCard
用途：订单信息展示卡片
复用情况：支付页面、订单详情页面
创建时间：2026-03-20
-->
<template>
  <view class="order-info-card">
    <view class="merchant-info" @tap="$emit('merchant-tap')">
      <image class="merchant-avatar" :src="orderInfo.merchantAvatar" mode="aspectFill" />
      <view class="merchant-details">
        <text class="merchant-name">{{ orderInfo.merchantName }}</text>
        <text class="merchant-desc">{{ orderInfo.merchantDesc }}</text>
      </view>
    </view>

    <view class="order-summary">
      <view class="summary-item">
        <text class="item-label">订单号</text>
        <text class="item-value">{{ orderInfo.orderNo }}</text>
      </view>
      <view class="summary-item">
        <text class="item-label">下单时间</text>
        <text class="item-value">{{ orderInfo.orderTime }}</text>
      </view>
      <view class="summary-item">
        <text class="item-label">订单金额</text>
        <text class="item-value amount">¥{{ orderInfo.totalAmount }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  orderInfo: {
    type: Object,
    required: true
  }
})

defineEmits(['merchant-tap'])
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '../../styles/mixins.scss';

.order-info-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.merchant-info {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding-bottom: 20rpx;
  margin-bottom: 20rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.merchant-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 12rpx;
}

.merchant-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.merchant-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
}

.merchant-desc {
  font-size: 22rpx;
  color: #999;
}

.order-summary {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-label {
  font-size: 26rpx;
  color: #666;
}

.item-value {
  font-size: 26rpx;
  color: #333;

  &.amount {
    font-size: 32rpx;
    font-weight: bold;
    color: #FF6B35;
  }
}
</style>
