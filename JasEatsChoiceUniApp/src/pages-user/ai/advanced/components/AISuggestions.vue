<!--
组件名称：AISuggestions
用途：AI饮食建议列表
页面：AI高级分析页面
创建时间：2026-03-20
-->
<template>
  <view class="ai-suggestions">
    <view class="suggestion-item" v-for="(suggestion, index) in suggestions" :key="index">
      <view class="suggestion-icon" :class="suggestion.type">
        <uni-icons :type="suggestion.icon" size="20" color="#fff"></uni-icons>
      </view>
      <view class="suggestion-content">
        <text class="suggestion-title">{{ suggestion.title }}</text>
        <text class="suggestion-desc">{{ suggestion.desc }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  suggestions: {
    type: Array,
    default: () => []
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.ai-suggestions {
  .suggestion-item {
    display: flex;
    gap: 20rpx;
    padding: 24rpx;
    background: #F8F9FA;
    border-radius: 12rpx;
    margin-bottom: 16rpx;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .suggestion-icon {
    width: 60rpx;
    height: 60rpx;
    border-radius: 50%;
    @include flex-center;
    flex-shrink: 0;

    &.warning {
      background: #FFF3E0;
    }

    &.success {
      background: #E8F5E9;
    }

    &.info {
      background: #E3F2FD;
    }
  }

  .suggestion-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8rpx;

    .suggestion-title {
      font-size: 28rpx;
      font-weight: 500;
      color: #333;
    }

    .suggestion-desc {
      font-size: 26rpx;
      color: #666;
      line-height: 1.6;
    }
  }
}
</style>
