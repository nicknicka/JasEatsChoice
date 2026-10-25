<!--
组件名称：OrderActions
用途：订单操作按钮栏
复用情况：订单详情
创建时间：2026-03-20
-->
<template>
  <view class="order-actions">
    <view class="action-buttons">
      <button
        class="action-btn secondary"
        v-if="showCancel"
        @tap="$emit('cancel')"
      >
        取消订单
      </button>

      <button
        class="action-btn secondary"
        v-if="showConfirm"
        @tap="$emit('confirm')"
      >
        确认收货
      </button>

      <button
        class="action-btn primary"
        v-if="showReview"
        @tap="$emit('review')"
      >
        评价订单
      </button>

      <button
        class="action-btn primary"
        v-if="showPay"
        @tap="$emit('pay')"
      >
        立即支付
      </button>

      <button
        class="action-btn secondary"
        @tap="$emit('reorder')"
      >
        再来一单
      </button>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  status: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['cancel', 'confirm', 'review', 'pay', 'reorder'])

const showCancel = props.status === 'pending'
const showConfirm = props.status === 'delivering'
const showReview = props.status === 'completed'
const showPay = props.status === 'pending'
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '../../styles/mixins.scss';

.order-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 20rpx 30rpx;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.action-buttons {
  display: flex;
  gap: 20rpx;
  justify-content: flex-end;
}

.action-btn {
  min-width: 180rpx;
  height: 70rpx;
  font-size: 26rpx;
  border-radius: 35rpx;
  border: none;
  @include flex-center;

  &.secondary {
    background: #fff;
    color: #333;
    border: 1rpx solid #ddd;
  }

  &.primary {
    background: #FF6B35;
    color: #fff;
  }
}
</style>
