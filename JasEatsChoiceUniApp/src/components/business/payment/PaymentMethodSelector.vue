<!--
组件名称：PaymentMethodSelector
用途：支付方式选择器
复用情况：支付页面、订单确认页面
创建时间：2026-03-20
-->
<template>
  <view class="payment-method-selector">
    <view class="section-title">选择支付方式</view>
    <view class="methods-list">
      <view
        class="method-item"
        :class="{ active: modelValue === method.value }"
        v-for="method in methods"
        :key="method.value"
        @tap="selectMethod(method.value)"
      >
        <view class="method-icon" :class="method.value">
          <uni-icons :type="method.icon" size="28" color="#fff" />
        </view>
        <view class="method-info">
          <text class="method-name">{{ method.name }}</text>
          <text class="method-desc">{{ method.desc }}</text>
        </view>
        <view class="method-check" v-if="modelValue === method.value">
          <uni-icons type="circle-filled" size="20" :color="method.color" />
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  modelValue: {
    type: String,
    default: 'wechat'
  },
  userBalance: {
    type: String,
    default: '0.00'
  }
})

const emit = defineEmits(['update:modelValue'])

const methods = [
  {
    value: 'wechat',
    name: '微信支付',
    desc: '推荐使用',
    icon: 'weixin',
    color: '#09BB07'
  },
  {
    value: 'alipay',
    name: '支付宝',
    desc: '数亿用户的选择',
    icon: 'wallet-filled',
    color: '#1677FF'
  },
  {
    value: 'wallet',
    name: '余额支付',
    desc: `余额：¥${props.userBalance}`,
    icon: 'wallet',
    color: '#FF6B35'
  }
]

const selectMethod = (value) => {
  emit('update:modelValue', value)
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.payment-method-selector {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.methods-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.method-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 25rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s;

  &.active {
    border-color: #FF6B35;
    background: #FFF7E6;
  }
}

.method-icon {
  width: 70rpx;
  height: 70rpx;
  border-radius: 12rpx;
  @include flex-center;

  &.wechat {
    background: linear-gradient(135deg, #09BB07, #30D167);
  }

  &.alipay {
    background: linear-gradient(135deg, #1677FF, #409EFF);
  }

  &.wallet {
    background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  }
}

.method-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.method-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
}

.method-desc {
  font-size: 22rpx;
  color: #999;
}
</style>
