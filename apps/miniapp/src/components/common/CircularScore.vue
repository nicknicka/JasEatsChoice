<!--
组件名称：CircularScore
用途：圆形评分显示
复用情况：健康评分、信用评分等
创建时间：2026-03-20
-->
<template>
  <view class="circular-score" :style="{ width: size + 'rpx', height: size + 'rpx' }">
    <view class="score-circle" :class="`score-${scoreLevel}`">
      <view class="score-content">
        <text class="score-number">{{ score }}</text>
        <text class="score-label">{{ label }}</text>
      </view>
    </view>
    <view class="score-details" v-if="details && details.length > 0">
      <view class="detail-item" v-for="(item, index) in details" :key="index">
        <text class="detail-label">{{ item.label }}</text>
        <text class="detail-value">{{ item.value }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  score: {
    type: Number,
    required: true
  },
  label: {
    type: String,
    default: '分'
  },
  size: {
    type: [String, Number],
    default: 180
  },
  details: {
    type: Array,
    default: () => []
  }
})

const scoreLevel = computed(() => {
  if (props.score >= 90) return 'excellent'
  if (props.score >= 80) return 'good'
  if (props.score >= 60) return 'medium'
  return 'low'
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '../../styles/mixins.scss';

.circular-score {
  display: flex;
  align-items: center;
  gap: 40rpx;
}

.score-circle {
  border-radius: 50%;
  @include flex-center;
  flex-direction: column;
  flex-shrink: 0;
  padding: 20rpx;

  &.score-excellent {
    background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%);
  }

  &.score-good {
    background: linear-gradient(135deg, #FF6B35 0%, #FF8C42 100%);
  }

  &.score-medium {
    background: linear-gradient(135deg, #FFC107 0%, #FFD54F 100%);
  }

  &.score-low {
    background: linear-gradient(135deg, #F44336 0%, #EF5350 100%);
  }
}

.score-content {
  text-align: center;
}

.score-number {
  font-size: 56rpx;
  font-weight: bold;
  color: #fff;
  line-height: 1;
  display: block;
}

.score-label {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.9);
  margin-top: 5rpx;
}

.score-details {
  flex: 1;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.detail-label {
  font-size: 26rpx;
  color: #666;
}

.detail-value {
  font-size: 26rpx;
  font-weight: bold;
  color: #FF6B35;
}
</style>
