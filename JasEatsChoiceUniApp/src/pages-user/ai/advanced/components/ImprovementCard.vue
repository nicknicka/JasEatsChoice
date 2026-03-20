<!--
组件名称：ImprovementCard
用途：改进建议卡片
页面：AI高级分析页面
创建时间：2026-03-20
-->
<template>
  <view class="improvements">
    <view class="improvement-item" v-for="(item, index) in improvements" :key="index">
      <view class="improvement-header">
        <view class="priority-badge" :class="'priority-' + item.priority">
          <text>{{ item.priorityText }}</text>
        </view>
        <text class="improvement-title">{{ item.title }}</text>
      </view>
      <text class="improvement-desc">{{ item.desc }}</text>
      <view class="improvement-actions">
        <button class="action-btn" @tap="$emit('apply', item)">采纳建议</button>
        <button class="action-btn secondary" @tap="$emit('detail', item)">查看详情</button>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  improvements: {
    type: Array,
    default: () => []
  }
})

defineEmits(['apply', 'detail'])
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.improvements {
  .improvement-item {
    padding: 24rpx;
    background: #F8F9FA;
    border-radius: 12rpx;
    margin-bottom: 20rpx;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .improvement-header {
    display: flex;
    align-items: center;
    gap: 15rpx;
    margin-bottom: 12rpx;

    .priority-badge {
      padding: 6rpx 16rpx;
      border-radius: 8rpx;
      font-size: 22rpx;

      &.priority-high {
        background: #FFEBEE;
        color: #F44336;
      }

      &.priority-medium {
        background: #FFF3E0;
        color: #FF9800;
      }

      &.priority-low {
        background: #E3F2FD;
        color: #2196F3;
      }
    }

    .improvement-title {
      font-size: 28rpx;
      font-weight: 500;
      color: #333;
    }
  }

  .improvement-desc {
    font-size: 26rpx;
    color: #666;
    line-height: 1.6;
    margin-bottom: 20rpx;
  }

  .improvement-actions {
    display: flex;
    gap: 20rpx;

    .action-btn {
      flex: 1;
      height: 60rpx;
      border-radius: 8rpx;
      font-size: 26rpx;
      border: none;
      background: #FF6B35;
      color: #fff;

      &.secondary {
        background: #fff;
        color: #666;
        border: 1rpx solid #ddd;
      }
    }
  }
}
</style>
