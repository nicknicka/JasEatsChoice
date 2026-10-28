<template>
  <view class="integral-unavailable">
    <text class="title">积分功能暂未开放</text>
    <text class="description">后端还没有提供积分能力，当前页面仅作占位</text>
  </view>
</template>

<style lang="scss" scoped>
.integral-unavailable {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  background: #F5F5F5;
  padding: 40rpx;

  .title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
  }

  .description {
    font-size: 26rpx;
    color: #999;
  }
}
</style>
