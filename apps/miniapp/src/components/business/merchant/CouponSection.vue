<!--
组件名称：CouponSection
用途：优惠券区域展示
复用情况：商家详情、个人中心
创建时间：2026-03-20
-->
<template>
  <view class="coupon-section" v-if="coupons.length > 0">
    <view class="section-title">店铺优惠</view>
    <scroll-view class="coupon-scroll" scroll-x show-scrollbar="false">
      <view class="coupon-list">
        <view
          class="coupon-item"
          v-for="coupon in coupons"
          :key="coupon.id"
          @tap="$emit('receive', coupon)"
        >
          <view class="coupon-left">
            <text class="coupon-amount">¥{{ coupon.amount }}</text>
            <text class="coupon-condition">{{ coupon.condition }}</text>
          </view>
          <view class="coupon-right">
            <text class="coupon-btn">{{ coupon.received ? '已领取' : '领取' }}</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
const props = defineProps({
  coupons: {
    type: Array,
    default: () => []
  }
})

defineEmits(['receive'])
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '../../styles/mixins.scss';

.coupon-section {
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

.coupon-scroll {
  white-space: nowrap;
}

.coupon-list {
  display: inline-flex;
  gap: 20rpx;
}

.coupon-item {
  display: inline-flex;
  align-items: center;
  min-width: 400rpx;
  height: 140rpx;
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  border-radius: 12rpx;
  padding: 0 20rpx;
}

.coupon-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8rpx;
}

.coupon-amount {
  font-size: 48rpx;
  font-weight: bold;
  color: #fff;
}

.coupon-condition {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.9);
}

.coupon-right {
  display: flex;
  align-items: center;
}

.coupon-btn {
  padding: 12rpx 30rpx;
  background: #fff;
  border-radius: 20rpx;
  font-size: 24rpx;
  color: #FF6B35;
  font-weight: 500;
}
</style>
