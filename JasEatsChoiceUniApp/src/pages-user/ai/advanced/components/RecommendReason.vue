<!--
组件名称：RecommendReason
用途：推荐理由列表
页面：AI高级分析页面
创建时间：2026-03-20
-->
<template>
  <view class="recommend-reason">
    <view class="reason-item" v-for="(reason, index) in reasons" :key="index">
      <uni-icons :type="reason.icon" size="22" color="#FF6B35"></uni-icons>
      <view class="reason-content">
        <text class="reason-title">{{ reason.title }}</text>
        <text class="reason-desc">{{ reason.desc }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  reasons: {
    type: Array,
    default: () => []
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.recommend-reason {
  .reason-item {
    display: flex;
    gap: 20rpx;
    padding: 24rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }
  }

  .reason-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8rpx;

    .reason-title {
      font-size: 28rpx;
      font-weight: 500;
      color: #333;
    }

    .reason-desc {
      font-size: 26rpx;
      color: #666;
      line-height: 1.6;
    }
  }
}
</style>
