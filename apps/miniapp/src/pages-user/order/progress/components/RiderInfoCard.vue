<!--
组件名称：RiderInfoCard
用途：骑手信息展示
页面专用：订单进度页面
创建时间：2026-03-20
-->
<template>
  <view class="rider-info-card" @tap="$emit('contact')">
    <image class="rider-avatar" :src="rider.avatar" mode="aspectFill" />
    <view class="rider-detail">
      <text class="rider-name">{{ rider.name }}</text>
      <text class="rider-phone">{{ rider.phone }}</text>
    </view>
    <view class="contact-btn">
      <text class="btn-icon">📞</text>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  rider: {
    type: Object,
    required: true
  }
})

defineEmits(['contact'])
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.rider-info-card {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 25rpx;
  background: #FFF7E6;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
}

.rider-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  border: 3rpx solid #fff;
}

.rider-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.rider-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
}

.rider-phone {
  font-size: 24rpx;
  color: #999;
}

.contact-btn {
  width: 70rpx;
  height: 70rpx;
  background: #fff;
  border-radius: 50%;
  @include flex-center;
}

.btn-icon {
  font-size: 32rpx;
}
</style>
