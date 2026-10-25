<!--
组件名称：MerchantHeader
用途：商家头部信息展示
复用情况：商家详情、商家列表
创建时间：2026-03-20
-->
<template>
  <view class="merchant-header">
    <view class="header-bg"></view>

    <view class="merchant-info-card">
      <image class="merchant-logo" :src="merchant.logo" mode="aspectFill" />

      <view class="merchant-basic">
        <view class="merchant-name">{{ merchant.name }}</view>
        <view class="merchant-rating">
          <text class="star">⭐</text>
          <text class="rating-score">{{ merchant.rating }}</text>
          <text class="rating-count">（{{ merchant.reviewCount }}条评价）</text>
        </view>

        <view class="merchant-tags">
          <text class="tag" v-for="tag in merchant.tags" :key="tag">{{ tag }}</text>
        </view>

        <view class="merchant-stats">
          <view class="stat-item">
            <text class="stat-value">{{ merchant.monthlySales }}</text>
            <text class="stat-label">月售</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-value">{{ merchant.dishCount }}</text>
            <text class="stat-label">菜品</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-value">{{ merchant.deliveryTime }}</text>
            <text class="stat-label">分钟</text>
          </view>
        </view>
      </view>

      <view class="merchant-actions">
        <view class="action-btn" @tap="$emit('toggle-favorite')">
          <text class="action-icon">{{ isFavorite ? '❤️' : '🤍' }}</text>
          <text class="action-text">{{ isFavorite ? '已收藏' : '收藏' }}</text>
        </view>
        <view class="action-btn" @tap="$emit('share')">
          <text class="action-icon">📤</text>
          <text class="action-text">分享</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  merchant: {
    type: Object,
    required: true
  },
  isFavorite: {
    type: Boolean,
    default: false
  }
})

defineEmits(['toggle-favorite', 'share'])
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '../../styles/mixins.scss';

.merchant-header {
  position: relative;
  margin-bottom: 20rpx;
}

.header-bg {
  height: 200rpx;
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
}

.merchant-info-card {
  position: relative;
  margin: -80rpx 30rpx 0;
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.merchant-logo {
  width: 120rpx;
  height: 120rpx;
  border-radius: 16rpx;
  border: 4rpx solid #fff;
  margin-top: -60rpx;
  background: #fff;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.merchant-basic {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.merchant-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.merchant-rating {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.rating-score {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.rating-count {
  font-size: 22rpx;
  color: #999;
}

.merchant-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.tag {
  font-size: 22rpx;
  color: #FF6B35;
  background: #FFF7E6;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  border: 1rpx solid #FFE7CC;
}

.merchant-stats {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5rpx;
}

.stat-value {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 22rpx;
  color: #999;
}

.stat-divider {
  width: 1rpx;
  height: 40rpx;
  background: #e8e8e8;
}

.merchant-actions {
  display: flex;
  gap: 20rpx;
}

.action-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  padding: 15rpx 0;
  background: #F5F5F5;
  border-radius: 12rpx;
}

.action-icon {
  font-size: 32rpx;
}

.action-text {
  font-size: 22rpx;
  color: #666;
}
</style>
