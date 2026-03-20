<!--
组件名称：MicronutrientList
用途：维生素与矿物质列表
页面：AI高级分析页面
创建时间：2026-03-20
-->
<template>
  <view class="micronutrients">
    <view class="micro-item" v-for="item in list" :key="item.name">
      <view class="micro-icon">
        <text class="icon">{{ item.icon }}</text>
      </view>
      <view class="micro-info">
        <text class="micro-name">{{ item.name }}</text>
        <text class="micro-value">{{ item.value }}{{ item.unit }}</text>
      </view>
      <view class="micro-status" :class="item.status">
        <text>{{ item.statusText }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  list: {
    type: Array,
    default: () => []
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.micronutrients {
  .micro-item {
    display: flex;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }
  }

  .micro-icon {
    width: 60rpx;
    height: 60rpx;
    border-radius: 12rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    @include flex-center;
    margin-right: 20rpx;

    .icon {
      font-size: 24rpx;
      font-weight: bold;
      color: #fff;
    }
  }

  .micro-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 5rpx;

    .micro-name {
      font-size: 28rpx;
      color: #333;
    }

    .micro-value {
      font-size: 24rpx;
      color: #666;
    }
  }

  .micro-status {
    padding: 8rpx 20rpx;
    border-radius: 20rpx;
    font-size: 24rpx;

    &.normal {
      background: #E8F5E9;
      color: #4CAF50;
    }

    &.lack {
      background: #FFF3E0;
      color: #FF9800;
    }

    &.good {
      background: #E3F2FD;
      color: #2196F3;
    }
  }
}
</style>
