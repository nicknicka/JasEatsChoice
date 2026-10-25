<template>
  <uni-popup ref="popupRef" type="bottom">
    <view class="recharge-popup">
      <view class="popup-header">
        <text class="popup-title">账户充值</text>
        <text class="popup-close" @click="handleClose">×</text>
      </view>

      <view class="recharge-amount">
        <text class="amount-label">充值金额</text>
        <view class="amount-input">
          <text class="currency-symbol">¥</text>
          <input
            class="amount-field"
            type="digit"
            :value="modelValue"
            @input="handleInput"
            placeholder="请输入充值金额"
          />
        </view>
      </view>

      <view class="quick-amounts">
        <view
          class="amount-item"
          v-for="amount in quickAmounts"
          :key="amount"
          @click="selectAmount(amount)"
        >
          <text class="amount-text">{{ amount }}元</text>
        </view>
      </view>

      <view class="payment-methods">
        <text class="methods-title">支付方式</text>
        <view class="method-list">
          <view
            class="method-item"
            :class="{ active: paymentMethod === 'wechat' }"
            @click="selectPaymentMethod('wechat')"
          >
            <text class="method-icon">💚</text>
            <text class="method-name">微信支付</text>
            <view class="method-check" v-if="paymentMethod === 'wechat'">✓</view>
          </view>
          <view
            class="method-item"
            :class="{ active: paymentMethod === 'alipay' }"
            @click="selectPaymentMethod('alipay')"
          >
            <text class="method-icon">💙</text>
            <text class="method-name">支付宝</text>
            <view class="method-check" v-if="paymentMethod === 'alipay'">✓</view>
          </view>
        </view>
      </view>

      <button class="confirm-btn" @click="handleConfirm" :disabled="!modelValue">
        确认充值
      </button>
    </view>
  </uni-popup>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  // v-model 绑定的充值金额
  modelValue: {
    type: [Number, String],
    default: ''
  },
  // 快捷金额选项
  quickAmounts: {
    type: Array,
    default: () => [10, 20, 50, 100, 200, 500]
  }
})

const emit = defineEmits(['update:modelValue', 'confirm', 'close'])

const popupRef = ref(null)
const paymentMethod = ref('wechat')

/**
 * 打开弹窗
 */
const open = () => {
  popupRef.value?.open()
}

/**
 * 关闭弹窗
 */
const close = () => {
  popupRef.value?.close()
}

/**
 * 处理输入
 */
const handleInput = (e) => {
  emit('update:modelValue', e.detail.value)
}

/**
 * 选择快捷金额
 */
const selectAmount = (amount) => {
  emit('update:modelValue', amount.toString())
}

/**
 * 选择支付方式
 */
const selectPaymentMethod = (method) => {
  paymentMethod.value = method
}

/**
 * 确认充值
 */
const handleConfirm = () => {
  if (!props.modelValue) {
    uni.showToast({
      title: '请输入充值金额',
      icon: 'none'
    })
    return
  }

  const amount = parseFloat(props.modelValue)
  if (amount <= 0) {
    uni.showToast({
      title: '充值金额必须大于0',
      icon: 'none'
    })
    return
  }

  emit('confirm', {
    amount: amount,
    paymentMethod: paymentMethod.value
  })
}

/**
 * 关闭弹窗
 */
const handleClose = () => {
  emit('close')
  close()
}

// 暴露方法给父组件
defineExpose({
  open,
  close
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.recharge-popup {
  background-color: $bg-color-white;
  border-radius: $border-radius-lg $border-radius-lg 0 0;
  padding: $spacing-lg;
  max-height: 80vh;
  overflow-y: auto;
}

.popup-header {
  @include flex-between;
  align-items: center;
  margin-bottom: $spacing-xl;
}

.popup-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.popup-close {
  width: 48rpx;
  height: 48rpx;
  @include flex-center;
  font-size: 48rpx;
  color: $text-color-placeholder;

  &:active {
    opacity: 0.6;
  }
}

.recharge-amount {
  margin-bottom: $spacing-xl;
}

.amount-label {
  font-size: $font-size-base;
  color: $text-color-primary;
  margin-bottom: $spacing-md;
}

.amount-input {
  @include flex-center;
  padding: $spacing-lg;
  background-color: $bg-color-base;
  border-radius: $border-radius-lg;
}

.currency-symbol {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
  margin-right: $spacing-sm;
}

.amount-field {
  flex: 1;
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.quick-amounts {
  @include flex-center;
  flex-wrap: wrap;
  gap: $spacing-sm;
  margin-bottom: $spacing-xl;
}

.amount-item {
  flex: 0 0 calc(33.33% - #{$spacing-sm} * 2 / 3);
  @include flex-center;
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-lg;
  border: 2rpx solid transparent;

  &:active {
    border-color: $primary-color;
    background-color: rgba(255, 107, 53, 0.05);
  }
}

.amount-text {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-medium;
}

.payment-methods {
  margin-bottom: $spacing-xl;
}

.methods-title {
  font-size: $font-size-base;
  color: $text-color-primary;
  margin-bottom: $spacing-md;
}

.method-list {
  @include flex-center-column;
  gap: $spacing-sm;
}

.method-item {
  @include flex-center;
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-lg;
  border: 2rpx solid transparent;
  position: relative;

  &.active {
    border-color: $primary-color;
    background-color: rgba(255, 107, 53, 0.05);
  }

  &:active {
    background-color: rgba(255, 107, 53, 0.1);
  }
}

.method-icon {
  font-size: $font-size-xl;
  margin-right: $spacing-md;
}

.method-name {
  flex: 1;
  font-size: $font-size-base;
  color: $text-color-primary;
}

.method-check {
  position: absolute;
  right: $spacing-md;
  top: 50%;
  transform: translateY(-50%);
  width: 32rpx;
  height: 32rpx;
  @include flex-center;
  background-color: $primary-color;
  color: #fff;
  border-radius: 50%;
  font-size: $font-size-sm;
  font-weight: $font-weight-bold;
}

.confirm-btn {
  width: 100%;
  height: 88rpx;
  @include flex-center;
  background: linear-gradient(135deg, $primary-color, #FF8F61);
  color: #fff;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  border-radius: $border-radius-round;
  border: none;

  &:active {
    transform: scale(0.98);
  }

  &[disabled] {
    opacity: 0.5;
  }
}
</style>
