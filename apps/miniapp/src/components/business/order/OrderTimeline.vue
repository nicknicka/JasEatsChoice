<!--
组件名称：OrderTimeline
用途：订单进度时间线
复用情况：订单详情、订单进度
创建时间：2026-03-20
-->
<template>
  <view class="order-timeline">
    <view class="section-title">订单进度</view>
    <view class="timeline">
      <view
        class="timeline-item"
        :class="{ active: index === currentIndex, completed: step.completed }"
        v-for="(step, index) in steps"
        :key="index"
      >
        <view class="timeline-dot">
          <uni-icons
            v-if="step.completed"
            type="checkbox-filled"
            size="16"
            color="#52C41A"
          />
          <text v-else>{{ index + 1 }}</text>
        </view>
        <view class="timeline-line" v-if="index < steps.length - 1"></view>
        <view class="timeline-info">
          <view class="timeline-title">{{ step.title }}</view>
          <view class="timeline-time">{{ step.time }}</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  steps: {
    type: Array,
    default: () => []
  },
  currentIndex: {
    type: Number,
    default: 0
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '../../styles/mixins.scss';

.order-timeline {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 25rpx;
}

.timeline {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.timeline-item {
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
  position: relative;

  &.active .timeline-title {
    color: #FF6B35;
    font-weight: bold;
  }

  &.completed .timeline-dot {
    background: #52C41A;
    color: #fff;
  }
}

.timeline-dot {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background: #E8E8E8;
  color: #999;
  font-size: 20rpx;
  flex-shrink: 0;
  @include flex-center;
  z-index: 1;
}

.timeline-line {
  position: absolute;
  left: 20rpx;
  top: 40rpx;
  bottom: -30rpx;
  width: 2rpx;
  background: #E8E8E8;
  z-index: 0;
}

.timeline-item:last-child .timeline-line {
  display: none;
}

.timeline-item.completed .timeline-line {
  background: #52C41A;
}

.timeline-info {
  flex: 1;
  padding-top: 5rpx;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.timeline-title {
  font-size: 26rpx;
  color: #333;
}

.timeline-time {
  font-size: 22rpx;
  color: #999;
}
</style>
