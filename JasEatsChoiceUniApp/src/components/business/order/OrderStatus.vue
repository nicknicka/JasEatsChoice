<!--
组件名称：OrderStatus
用途：订单状态展示
复用情况：订单详情、订单列表
创建时间：2026-03-20
-->
<template>
  <view class="order-status" :class="statusClass">
    <view class="status-icon">
      <uni-icons :type="statusInfo.icon" size="60" :color="statusInfo.color" />
    </view>
    <view class="status-text" :style="{ color: statusInfo.color }">
      {{ statusInfo.text }}
    </view>
    <view class="status-tips">{{ statusInfo.tips }}</view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: {
    type: String,
    required: true
  }
})

const statusMap = {
  pending: {
    icon: 'clock',
    color: '#FFA500',
    text: '等待支付',
    tips: '请在30分钟内完成支付'
  },
  pendingAccept: {
    icon: 'checkmarkempty',
    color: '#1677FF',
    text: '待接单',
    tips: '商家正在接单'
  },
  preparing: {
    icon: 'spinner-cycle',
    color: '#FF6B35',
    text: '制作中',
    tips: '预计还需15分钟'
  },
  completed: {
    icon: 'checkbox-filled',
    color: '#52C41A',
    text: '已完成',
    tips: '感谢您的订购'
  },
  cancelled: {
    icon: 'close',
    color: '#999',
    text: '已取消',
    tips: '订单已取消'
  }
}

const statusInfo = computed(() => {
  return statusMap[props.status] || statusMap.pending
})

const statusClass = computed(() => {
  return `status-${props.status}`
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '../../styles/mixins.scss';

.order-status {
  background: #fff;
  border-radius: 16rpx;
  padding: 60rpx 30rpx;
  margin-bottom: 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20rpx;
}

.status-icon {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: #F5F5F5;
  @include flex-center;
}

.status-text {
  font-size: 36rpx;
  font-weight: bold;
}

.status-tips {
  font-size: 24rpx;
  color: #999;
}
</style>
