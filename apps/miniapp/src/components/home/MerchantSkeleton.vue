<template>
  <view class="merchant-skeleton">
    <view class="section-header">
      <view class="skeleton-title"></view>
      <view class="skeleton-more"></view>
    </view>
    <scroll-view class="merchant-scroll" scroll-x show-scrollbar="false">
      <view class="merchant-list">
        <view class="merchant-card" v-for="n in 5" :key="n">
          <view class="skeleton-logo"></view>
          <view class="skeleton-info">
            <view class="skeleton-name"></view>
            <view class="skeleton-rating"></view>
            <view class="skeleton-tags">
              <view class="skeleton-tag short"></view>
              <view class="skeleton-tag"></view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
/**
 * 商家卡片骨架屏组件
 */
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.merchant-skeleton {
  background-color: $bg-color-white;
  padding: $spacing-md;
  margin-bottom: $spacing-md;
}

.section-header {
  @include flex-between;
  margin-bottom: $spacing-md;
}

.skeleton-title {
  width: 150rpx;
  height: 36rpx;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4rpx;
}

.skeleton-more {
  width: 100rpx;
  height: 32rpx;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4rpx;
}

.merchant-scroll {
  white-space: nowrap;
}

.merchant-list {
  @include flex-center;
  gap: $spacing-md;
}

.merchant-card {
  width: 240rpx;
  flex-shrink: 0;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  overflow: hidden;
}

.skeleton-logo {
  width: 100%;
  height: 160rpx;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-info {
  padding: $spacing-sm;
}

.skeleton-name {
  width: 70%;
  height: 32rpx;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4rpx;
  margin-bottom: $spacing-xs;
}

.skeleton-rating {
  width: 50%;
  height: 28rpx;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4rpx;
  margin-bottom: $spacing-xs;
}

.skeleton-tags {
  display: flex;
  gap: $spacing-xs;
}

.skeleton-tag {
  width: 40%;
  height: 24rpx;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4rpx;

  &.short {
    width: 30%;
  }
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
