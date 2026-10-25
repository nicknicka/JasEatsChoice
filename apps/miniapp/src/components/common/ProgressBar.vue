<!--
组件名称：ProgressBar
用途：进度条组件
复用情况：营养分析、任务进度等
创建时间：2026-03-20
-->
<template>
  <view class="progress-bar-container">
    <view class="progress-header" v-if="showLabel">
      <view class="progress-label">
        <view class="color-dot" v-if="color" :style="{ background: color }"></view>
        <text class="label-text">{{ label }}</text>
      </view>
      <text class="progress-value">{{ value }}{{ unit }}</text>
    </view>
    <view class="progress-bar">
      <view
        class="progress-fill"
        :style="{ width: percent + '%', background: color }"
      ></view>
    </view>
    <text class="progress-target" v-if="target">目标: {{ target }}{{ unit }}</text>
  </view>
</template>

<script setup>
const props = defineProps({
  label: {
    type: String,
    default: ''
  },
  value: {
    type: Number,
    required: true
  },
  target: {
    type: Number,
    default: 0
  },
  unit: {
    type: String,
    default: ''
  },
  percent: {
    type: Number,
    required: true
  },
  color: {
    type: String,
    default: '#FF6B35'
  },
  showLabel: {
    type: Boolean,
    default: true
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '../../styles/mixins.scss';

.progress-bar-container {
  margin-bottom: 30rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.progress-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10rpx;
}

.progress-label {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.color-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
}

.label-text {
  font-size: 28rpx;
  color: #333;
}

.progress-value {
  font-size: 28rpx;
  font-weight: bold;
  color: #FF6B35;
}

.progress-bar {
  height: 12rpx;
  background: #F0F0F0;
  border-radius: 6rpx;
  overflow: hidden;
  margin-bottom: 8rpx;
}

.progress-fill {
  height: 100%;
  border-radius: 6rpx;
  transition: width 0.3s;
}

.progress-target {
  font-size: 24rpx;
  color: #999;
}
</style>
