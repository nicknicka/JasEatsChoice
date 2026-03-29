<template>
  <view class="dish-card-skeleton">
    <view class="skeleton-image"></view>
    <view class="skeleton-content">
      <view class="skeleton-title"></view>
      <view class="skeleton-tags">
        <view class="skeleton-tag short"></view>
        <view class="skeleton-tag"></view>
      </view>
      <view class="skeleton-reason"></view>
      <view class="skeleton-bottom">
        <view class="skeleton-price"></view>
        <view class="skeleton-rating"></view>
      </view>
    </view>
  </view>
</template>

<script setup>
/**
 * 菜品卡片骨架屏组件
 * 用于菜品加载时显示占位内容
 */
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.dish-card-skeleton {
  display: flex;
  padding: $spacing-sm;
  background: #fff;
  border-radius: $border-radius-base;
  margin-bottom: $spacing-md;
  overflow: hidden;
}

.skeleton-image {
  width: 200rpx;
  height: 200rpx;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: $border-radius-base;
  flex-shrink: 0;
}

.skeleton-content {
  flex: 1;
  padding-left: $spacing-sm;
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.skeleton-title {
  width: 70%;
  height: 40rpx;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4rpx;
}

.skeleton-tags {
  display: flex;
  gap: $spacing-xs;
}

.skeleton-tag {
  width: 30%;
  height: 32rpx;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 12rpx;

  &.short {
    width: 20%;
  }
}

.skeleton-reason {
  width: 80%;
  height: 32rpx;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4rpx;
}

.skeleton-bottom {
  @include flex-between;
  margin-top: auto;
}

.skeleton-price {
  width: 25%;
  height: 36rpx;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4rpx;
}

.skeleton-rating {
  width: 20%;
  height: 32rpx;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4rpx;
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}
</style>
