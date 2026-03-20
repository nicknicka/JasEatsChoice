<!--
页面名称：payment/index（重构版）
原代码行数：1318行
重构后行数：约300行
减少比例：77%
重构时间：2026-03-20
-->
<template>
  <view class="payment-container">
    <!-- 订单信息 -->
    <OrderInfoCard
      :order-info="orderInfo"
      @merchant-tap="viewMerchant"
    />

    <!-- 支付方式 -->
    <PaymentMethodSelector
      v-model="selectedMethod"
      :user-balance="userBalance"
    />

    <!-- 支付详情 -->
    <PaymentDetail
      :order-info="orderInfo"
    />

    <!-- 优惠券 -->
    <CouponSelector
      :selected-coupon="selectedCoupon"
      :available-count="availableCoupons.length"
      @select="showCouponPopup"
    />

    <!-- 支付说明 -->
    <view class="payment-notice">
      <view class="notice-title">
        <uni-icons type="info" size="16" color="#FF6B35" />
        <text class="notice-text">支付说明</text>
      </view>
      <view class="notice-list">
        <text class="notice-item">• 支付成功后将自动跳转到订单详情页</text>
        <text class="notice-item">• 如遇支付问题，请联系客服处理</text>
        <text class="notice-item">• 订单超时未支付将自动取消</text>
      </view>
    </view>

    <!-- 底部支付按钮 -->
    <view class="payment-footer">
      <view class="footer-info">
        <text class="pay-label">实付金额</text>
        <text class="pay-amount">¥{{ finalAmount }}</text>
      </view>
      <button
        class="pay-btn"
        :disabled="!selectedMethod || paying"
        @tap="handlePayment"
      >
        {{ paying ? '支付中...' : '确认支付' }}
      </button>
    </view>

    <!-- 优惠券选择弹窗 -->
    <uni-popup ref="couponPopup" type="bottom">
      <view class="coupon-popup">
        <view class="popup-header">
          <text class="popup-title">选择优惠券</text>
          <view class="popup-close" @tap="closeCouponPopup">
            <uni-icons type="close" size="20" color="#999" />
          </view>
        </view>
        <scroll-view class="coupon-list" scroll-y>
          <view
            class="coupon-option"
            v-for="coupon in availableCoupons"
            :key="coupon.id"
            @tap="handleSelectCoupon(coupon)"
          >
            <view class="coupon-left">
              <view class="coupon-amount">
                <text class="amount-value">¥{{ coupon.discount }}</text>
                <text class="amount-condition">{{ coupon.condition }}</text>
              </view>
              <text class="coupon-name">{{ coupon.name }}</text>
              <text class="coupon-time">{{ coupon.validTime }}</text>
            </view>
            <view class="coupon-check" v-if="selectedCoupon && selectedCoupon.id === coupon.id">
              <uni-icons type="circle-filled" size="20" color="#FF6B35" />
            </view>
          </view>

          <!-- 不使用优惠券 -->
          <view class="coupon-option" @tap="handleCancelCoupon">
            <view class="coupon-left">
              <text class="coupon-name">不使用优惠券</text>
            </view>
            <view class="coupon-check" v-if="!selectedCoupon">
              <uni-icons type="circle-filled" size="20" color="#FF6B35" />
            </view>
          </view>
        </scroll-view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import OrderInfoCard from '@/components/business/order/OrderInfoCard.vue'
import PaymentMethodSelector from '@/components/business/payment/PaymentMethodSelector.vue'
import PaymentDetail from '@/components/business/payment/PaymentDetail.vue'
import CouponSelector from '@/components/business/payment/CouponSelector.vue'
import { usePayment } from '@/composables/payment/usePayment'

// 使用支付逻辑
const {
  orderInfo,
  userBalance,
  selectedMethod,
  selectedCoupon,
  availableCoupons,
  paying,
  finalAmount,
  selectCoupon,
  cancelCoupon,
  confirmPayment,
  viewMerchant
} = usePayment()

// 弹窗引用
const couponPopup = ref(null)

/**
 * 显示优惠券弹窗
 */
const showCouponPopup = () => {
  couponPopup.value?.open()
}

/**
 * 关闭优惠券弹窗
 */
const closeCouponPopup = () => {
  couponPopup.value?.close()
}

/**
 * 选择优惠券
 */
const handleSelectCoupon = (coupon) => {
  selectCoupon(coupon)
  closeCouponPopup()
}

/**
 * 取消优惠券
 */
const handleCancelCoupon = () => {
  cancelCoupon()
  closeCouponPopup()
}

/**
 * 处理支付
 */
const handlePayment = () => {
  confirmPayment()
}

// 生命周期
onLoad((options) => {
  // usePayment 会自动处理初始化
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.payment-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding: 20rpx;
  padding-bottom: 180rpx;
}

.payment-notice {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.notice-title {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 20rpx;
}

.notice-text {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.notice-item {
  font-size: 24rpx;
  color: #666;
  line-height: 1.6;
}

.payment-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 20rpx 30rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.footer-info {
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.pay-label {
  font-size: 22rpx;
  color: #999;
}

.pay-amount {
  font-size: 36rpx;
  font-weight: bold;
  color: #FF6B35;
}

.pay-btn {
  min-width: 240rpx;
  height: 80rpx;
  background: #FF6B35;
  color: #fff;
  font-size: 28rpx;
  border-radius: 40rpx;
  border: none;
  @include flex-center;

  &[disabled] {
    background: #E8E8E8;
    color: #999;
  }
}

/* 优惠券弹窗 */
.coupon-popup {
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  max-height: 80vh;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.popup-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.popup-close {
  width: 40rpx;
  height: 40rpx;
  @include flex-center;
}

.coupon-list {
  max-height: 60vh;
  padding: 20rpx;
}

.coupon-option {
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  border-radius: 16rpx;
  padding: 25rpx;
  margin-bottom: 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;

  &:last-child {
    margin-bottom: 0;
  }
}

.coupon-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.coupon-amount {
  display: flex;
  align-items: baseline;
  gap: 5rpx;
}

.amount-value {
  font-size: 40rpx;
  font-weight: bold;
  color: #fff;
}

.amount-condition {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.8);
}

.coupon-name {
  font-size: 26rpx;
  color: #fff;
}

.coupon-time {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.7);
}
</style>
