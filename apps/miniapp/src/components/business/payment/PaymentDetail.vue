<!--
组件名称：PaymentDetail
用途：支付详情展示
复用情况：支付页面、订单确认页面
创建时间：2026-03-20
-->
<template>
  <view class="payment-detail">
    <view class="section-title">支付详情</view>
    <view class="detail-row">
      <text class="detail-label">订单金额</text>
      <text class="detail-value">¥{{ orderInfo.totalAmount }}</text>
    </view>
    <view class="detail-row" v-if="orderInfo.discount > 0">
      <text class="detail-label">优惠减免</text>
      <text class="detail-value discount">-¥{{ orderInfo.discount }}</text>
    </view>
    <view class="detail-row total">
      <text class="detail-label">实付金额</text>
      <text class="detail-value final">¥{{ orderInfo.finalAmount }}</text>
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
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '../../styles/mixins.scss';

.payment-detail {
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

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15rpx 0;

  &.total {
    border-top: 1rpx solid #f5f5f5;
    margin-top: 10rpx;
    padding-top: 20rpx;
  }
}

.detail-label {
  font-size: 26rpx;
  color: #666;
}

.detail-value {
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
