<template>
  <view class="status-card" :class="statusClass">
    <view class="status-header">
      <text class="status-icon">{{ statusIcon }}</text>
      <view class="status-info">
        <text class="status-title">{{ statusText }}</text>
        <text class="status-desc" v-if="order.statusDesc">{{ order.statusDesc }}</text>
      </view>
    </view>

    <!-- 预计时间 -->
    <view class="estimated-time" v-if="order.estimatedTime">
      <text class="time-icon">⏱️</text>
      <text class="time-text">预计{{ order.estimatedTime }}</text>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 订单数据
  order: {
    type: Object,
    required: true
  }
})

// 订单状态映射（5状态系统：pending-待支付、pendingAccept-待接单、preparing-制作中、completed-已完成、cancelled-已取消）
const statusConfig = {
  pending: {
    icon: '⏰',
    text: '等待支付',
    class: 'pending'
  },
  pendingAccept: {
    icon: '👨‍🍳',
    text: '待接单',
    class: 'pendingAccept'
  },
  preparing: {
    icon: '🍳',
    text: '制作中',
    class: 'preparing'
  },
  completed: {
    icon: '✓',
    text: '已完成',
    class: 'completed'
  },
  cancelled: {
    icon: '✕',
    text: '已取消',
    class: 'cancelled'
  }
}

// 订单状态样式类
const statusClass = computed(() => {
  const config = statusConfig[props.order.status]
  return config ? config.class : ''
})

// 状态图标
const statusIcon = computed(() => {
  const config = statusConfig[props.order.status]
  return config ? config.icon : ''
})

// 状态文字
const statusText = computed(() => {
  const config = statusConfig[props.order.status]
  return config ? config.text : '未知状态'
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.status-card {
  margin: $spacing-md;
  padding: $spacing-xl;
  border-radius: $border-radius-lg;
  box-shadow: $box-shadow-md;

  &.pending {
    background: linear-gradient(135deg, #FFB74D, #FF9800);
  }

  &.pendingAccept {
    background: linear-gradient(135deg, #64B5F6, #42A5F5);
  }

  &.preparing {
    background: linear-gradient(135deg, #FF6B35, #FF8F61);
  }

  &.completed {
    background: linear-gradient(135deg, #81C784, #66BB6A);
  }

  &.cancelled {
    background: linear-gradient(135deg, #E0E0E0, #BDBDBD);
  }
}

.status-header {
  @include flex-center;
  gap: $spacing-md;
  margin-bottom: $spacing-md;
}

.status-icon {
  font-size: 64rpx;
}

.status-info {
  flex: 1;
  @include flex-center-column;
  align-items: flex-start;
  gap: $spacing-xs;
}

.status-title {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: #fff;
}

.status-desc {
  font-size: $font-size-sm;
  color: rgba(255, 255, 255, 0.8);
}

.estimated-time {
  @include flex-center;
  gap: $spacing-sm;
  padding: $spacing-md;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: $border-radius-base;
}

.time-icon {
  font-size: $font-size-xl;
}

.time-text {
  font-size: $font-size-base;
  color: #fff;
  font-weight: $font-weight-medium;
}
</style>
