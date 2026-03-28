<template>
  <view class="timeline-section">
    <view class="section-header">
      <text class="section-title">订单进度</text>
    </view>

    <view class="timeline-list">
      <view
        class="timeline-item"
        v-for="(step, index) in steps"
        :key="index"
        :class="{ active: step.active, completed: step.completed }"
      >
        <!-- 时间轴节点 -->
        <view class="timeline-node">
          <view class="node-icon">
            <text v-if="step.completed">✓</text>
            <text v-else>{{ index + 1 }}</text>
          </view>
          <view class="node-line" v-if="index < steps.length - 1"></view>
        </view>

        <!-- 步骤信息 -->
        <view class="timeline-content">
          <text class="step-title">{{ step.title }}</text>
          <text class="step-time" v-if="step.time">{{ step.time }}</text>
          <text class="step-desc" v-if="step.desc">{{ step.desc }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  // 订单数据
  order: {
    type: Object,
    required: true
  }
})

// 时间轴步骤（从父组件传入或计算）
const steps = computed(() => {
  const order = props.order

  const timeline = [
    {
      title: '提交订单',
      completed: true,
      active: false,
      time: order.createdAt
    },
    {
      title: '商家接单',
      completed: ['confirmed', 'preparing', 'ready', 'delivering', 'completed'].includes(order.status),
      active: order.status === 'pending',
      time: order.confirmedAt
    },
    {
      title: '准备餐品',
      completed: ['ready', 'delivering', 'completed'].includes(order.status),
      active: order.status === 'preparing',
      time: order.preparingAt,
      desc: '预计15分钟'
    },
    {
      title: '等待配送',
      completed: ['delivering', 'completed'].includes(order.status),
      active: order.status === 'ready',
      time: order.readyAt
    }
  ]

  // 如果有配送信息，添加配送中步骤
  if (order.delivery) {
    timeline.push({
      title: '配送中',
      completed: order.status === 'completed',
      active: order.status === 'delivering',
      time: order.deliveringAt,
      desc: order.delivery.rider ? `骑手：${order.delivery.rider.name}` : '等待骑手接单'
    })
  }

  timeline.push({
    title: '已送达',
    completed: order.status === 'completed',
    active: order.status === 'completed',
    time: order.completedAt
  })

  return timeline
})
</script>

<script>
import { computed } from 'vue'
export default {
  name: 'TimelineSection'
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.timeline-section {
  background-color: $bg-color-white;
  margin: $spacing-md;
  padding: $spacing-lg;
  border-radius: $border-radius-lg;
  box-shadow: $box-shadow-sm;
}

.section-header {
  margin-bottom: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.timeline-list {
  @include flex-center-column;
}

.timeline-item {
  @include flex-start;
  gap: $spacing-md;
  position: relative;

  &.active .node-icon {
    background: linear-gradient(135deg, $primary-color, #FF8F61);
    color: #fff;
    animation: pulse 2s infinite;
  }

  &.completed .node-icon {
    background-color: $success-color;
    color: #fff;
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

.timeline-node {
  position: relative;
  @include flex-center-column;
}

.node-icon {
  width: 48rpx;
  height: 48rpx;
  @include flex-center;
  background-color: $bg-color-base;
  border-radius: 50%;
  font-size: $font-size-sm;
  font-weight: $font-weight-bold;
  color: $text-color-secondary;
  flex-shrink: 0;
  z-index: 1;
}

.node-line {
  position: absolute;
  top: 48rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 2rpx;
  height: 80rpx;
  background-color: $border-color;
  z-index: 0;
}

.timeline-item:last-child .node-line {
  display: none;
}

.timeline-content {
  flex: 1;
  padding-top: 4rpx;
  @include flex-center-column;
  align-items: flex-start;
  gap: 4rpx;
}

.step-title {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $text-color-primary;
}

.step-time {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.step-desc {
  font-size: $font-size-sm;
  color: $text-color-regular;
}
</style>
