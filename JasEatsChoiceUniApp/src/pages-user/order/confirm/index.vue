<template>
  <view class="order-confirm-container">
    <scroll-view class="scroll-container" scroll-y>
      <!-- 收货地址 -->
      <view class="address-section card" @click="selectAddress">
        <view class="address-content" v-if="selectedAddress">
          <view class="address-header">
            <text class="address-name">{{ selectedAddress.name }}</text>
            <text class="address-phone">{{ selectedAddress.phone }}</text>
          </view>
          <view class="address-detail">
            <text class="address-tag" v-if="selectedAddress.isDefault">默认</text>
            <text class="address-text">{{ selectedAddress.address }}</text>
          </view>
        </view>
        <view class="address-empty" v-else>
          <view class="empty-icon">📍</view>
          <view class="empty-text">请选择收货地址</view>
        </view>
        <view class="address-arrow">›</view>
      </view>

      <!-- 配送时间 -->
      <view class="delivery-section card" @click="selectDeliveryTime">
        <view class="section-label">配送时间</view>
        <view class="delivery-content">
          <text class="delivery-text">{{ deliveryTime || '尽快送达（预计30分钟）' }}</text>
          <text class="delivery-arrow">›</text>
        </view>
      </view>

      <!-- 订单商品 -->
      <view class="order-dishes-section card">
        <view class="section-title">订单详情</view>

        <view class="merchant-group" v-for="group in orderItems" :key="group.merchantId">
          <view class="merchant-info">
            <image class="merchant-logo" :src="group.merchant.logo" mode="aspectFill" />
            <text class="merchant-name">{{ group.merchant.name }}</text>
          </view>

          <view class="dish-list">
            <view class="dish-item" v-for="item in group.items" :key="item.dish.id">
              <image class="dish-image" :src="item.dish.image" mode="aspectFill" />

              <view class="dish-info">
                <view class="dish-name">{{ item.dish.name }}</view>
                <view class="dish-spec" v-if="item.spec">{{ item.spec }}</view>
                <view class="dish-bottom">
                  <view class="dish-price">
                    <text class="price-symbol">¥</text>
                    <text class="price-value">{{ item.dish.price }}</text>
                  </view>
                  <view class="dish-quantity">×{{ item.quantity }}</view>
                </view>
              </view>
            </view>
          </view>

          <view class="merchant-fee">
            <view class="fee-item">
              <text class="fee-label">配送费</text>
              <text class="fee-value">¥5.00</text>
            </view>
            <view class="fee-item">
              <text class="fee-label">包装费</text>
              <text class="fee-value">¥{{ group.packingFee.toFixed(2) }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 优惠券 -->
      <view class="coupon-section card" @click="selectCoupon">
        <view class="section-label">优惠券</view>
        <view class="coupon-content">
          <text class="coupon-text" v-if="selectedCoupon">
            -¥{{ selectedCoupon.amount }}
          </text>
          <text class="coupon-text" v-else>
            {{ availableCoupons.length }}张可用
          </text>
          <text class="coupon-arrow">›</text>
        </view>
      </view>

      <!-- 备注 -->
      <view class="remark-section card">
        <view class="section-label">备注</view>
        <textarea
          class="remark-input"
          v-model="remark"
          placeholder="口味、偏好等要求（选填）"
          :maxlength="200"
        />
        <view class="quick-remarks">
          <text
            class="quick-remark-tag"
            v-for="tag in quickRemarks"
            :key="tag"
            @click="addQuickRemark(tag)"
          >
            {{ tag }}
          </text>
        </view>
      </view>

      <!-- 支付方式 -->
      <view class="payment-section card">
        <view class="section-title">支付方式</view>
        <view class="payment-list">
          <view
            class="payment-item"
            :class="{ active: paymentMethod === item.value }"
            v-for="item in paymentMethods"
            :key="item.value"
            @click="selectPayment(item.value)"
          >
            <view class="payment-info">
              <text class="payment-icon">{{ item.icon }}</text>
              <text class="payment-name">{{ item.label }}</text>
            </view>
            <view class="payment-radio">
              <text>{{ paymentMethod === item.value ? '✓' : '' }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 金额明细 -->
      <view class="amount-section card">
        <view class="amount-item">
          <text class="amount-label">商品金额</text>
          <text class="amount-value">¥{{ totalDishPrice }}</text>
        </view>
        <view class="amount-item">
          <text class="amount-label">配送费</text>
          <text class="amount-value">¥{{ totalDeliveryFee }}</text>
        </view>
        <view class="amount-item">
          <text class="amount-label">包装费</text>
          <text class="amount-value">¥{{ totalPackingFee }}</text>
        </view>
        <view class="amount-item" v-if="selectedCoupon">
          <text class="amount-label">优惠券</text>
          <text class="amount-value discount">-¥{{ selectedCoupon.amount }}</text>
        </view>
        <view class="amount-item total">
          <text class="amount-label">实付金额</text>
          <view class="amount-value-wrapper">
            <text class="amount-value final">¥{{ finalPrice }}</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 底部提交栏 -->
    <view class="bottom-bar">
      <view class="price-section">
        <text class="price-label">合计：</text>
        <text class="price-symbol">¥</text>
        <text class="price-value">{{ finalPrice }}</text>
      </view>
      <view class="submit-btn" @click="submitOrder">
        提交订单
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store'
import { orderApi, addressApi } from '@/api'

// Store
const userStore = useUserStore()

// 状态
const selectedAddress = ref(null)
const deliveryTime = ref('')
const remark = ref('')
const selectedCoupon = ref(null)
const paymentMethod = ref('wechat')

// 订单商品（模拟数据）
const orderItems = ref([
  {
    merchantId: '1',
    merchant: {
      id: '1',
      name: '老王家常菜',
      logo: 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=老王'
    },
    items: [
      {
        dish: {
          id: '1',
          name: '宫保鸡丁',
          price: 28,
          image: 'https://via.placeholder.com/300x300/FF6B35/FFFFFF?text=宫保鸡丁'
        },
        quantity: 2,
        spec: ''
      },
      {
        dish: {
          id: '4',
          name: '麻婆豆腐',
          price: 18,
          image: 'https://via.placeholder.com/300x300/faad14/FFFFFF?text=麻婆豆腐'
        },
        quantity: 1,
        spec: '微辣'
      }
    ],
    packingFee: 3 // 每份1元包装费
  }
])

// 可用优惠券
const availableCoupons = ref([
  {
    id: 1,
    amount: 10,
    condition: '满50可用'
  },
  {
    id: 2,
    amount: 5,
    condition: '满30可用'
  }
])

// 快捷备注
const quickRemarks = ref([
  '不要辣',
  '少放油',
  '多放葱',
  '按时送达',
  '联系我'
])

// 支付方式
const paymentMethods = ref([
  {
    label: '微信支付',
    value: 'wechat',
    icon: '💬'
  },
  {
    label: '支付宝',
    value: 'alipay',
    icon: '💰'
  },
  {
    label: '余额支付',
    value: 'balance',
    icon: '💳'
  }
])

// 计算属性
const totalDishPrice = computed(() => {
  let total = 0
  orderItems.value.forEach(group => {
    group.items.forEach(item => {
      total += item.dish.price * item.quantity
    })
  })
  return total.toFixed(2)
})

const totalDeliveryFee = computed(() => {
  // 每个商家5元配送费
  return (orderItems.value.length * 5).toFixed(2)
})

const totalPackingFee = computed(() => {
  let total = 0
  orderItems.value.forEach(group => {
    total += group.packingFee
  })
  return total.toFixed(2)
})

const finalPrice = computed(() => {
  let total = parseFloat(totalDishPrice.value)
  total += parseFloat(totalDeliveryFee.value)
  total += parseFloat(totalPackingFee.value)

  if (selectedCoupon.value) {
    total -= selectedCoupon.value.amount
  }

  return Math.max(0, total).toFixed(2)
})

/**
 * 选择地址
 */
const selectAddress = () => {
  uni.showToast({
    title: '地址选择功能开发中',
    icon: 'none'
  })
  // TODO: 跳转到地址选择页
  // uni.navigateTo({
  //   url: '/pages/address/select/index'
  // })
}

/**
 * 选择配送时间
 */
const selectDeliveryTime = () => {
  uni.showActionSheet({
    itemList: [
      '尽快送达（预计30分钟）',
      '12:00-12:30',
      '12:30-13:00',
      '18:00-18:30',
      '18:30-19:00'
    ],
    success: (res) => {
      const times = [
        '',
        '12:00-12:30',
        '12:30-13:00',
        '18:00-18:30',
        '18:30-19:00'
      ]
      deliveryTime.value = times[res.tapIndex]
    }
  })
}

/**
 * 选择优惠券
 */
const selectCoupon = () => {
  if (availableCoupons.value.length === 0) {
    uni.showToast({
      title: '暂无可用优惠券',
      icon: 'none'
    })
    return
  }

  const items = availableCoupons.value.map(c => `${c.amount}元（${c.condition}）`)

  uni.showActionSheet({
    itemList: ['不使用优惠券', ...items],
    success: (res) => {
      if (res.tapIndex === 0) {
        selectedCoupon.value = null
      } else {
        selectedCoupon.value = availableCoupons.value[res.tapIndex - 1]
      }
    }
  })
}

/**
 * 添加快捷备注
 */
const addQuickRemark = (tag) => {
  if (remark.value) {
    remark.value += '，' + tag
  } else {
    remark.value = tag
  }
}

/**
 * 选择支付方式
 */
const selectPayment = (method) => {
  paymentMethod.value = method
}

/**
 * 提交订单
 */
const submitOrder = async () => {
  if (!userStore.isLogin) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    setTimeout(() => {
      uni.navigateTo({
        url: '/pages/login/index'
      })
    }, 1500)
    return
  }

  if (!selectedAddress.value) {
    uni.showToast({
      title: '请选择收货地址',
      icon: 'none'
    })
    return
  }

  if (orderItems.value.length === 0) {
    uni.showToast({
      title: '请选择商品',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '确认支付',
    content: `支付金额：¥${finalPrice.value}`,
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '提交订单中...' })

          const userId = userStore.userInfo?.userId || userStore.userInfo?.id

          // 准备订单数据
          // 如果只有一个商家，创建单个订单
          // 如果有多个商家，需要创建多个订单
          if (orderItems.value.length === 1) {
            const group = orderItems.value[0]

            const orderData = {
              userId,
              merchantId: group.merchantId,
              addressId: selectedAddress.value.id,
              deliveryTime: deliveryTime.value || 'immediate',
              remark: remark.value,
              paymentMethod: paymentMethod.value,
              items: group.items.map(item => ({
                dishId: item.dish.id,
                dishName: item.dish.name,
                price: parseFloat(item.dish.price),
                quantity: item.quantity,
                spec: item.spec
              })),
              couponId: selectedCoupon.value?.id,
              deliveryFee: 5,
              packingFee: group.packingFee
            }

            // 调用后端API创建订单
            const result = await orderApi.create(orderData)

            uni.hideLoading()

            // 从购物车中移除已下单的商品
            const cartStore = (await import('@/store')).useCartStore()
            group.items.forEach(item => {
              cartStore.removeItem(group.merchantId, item.dish.id)
            })

            uni.showToast({
              title: '订单创建成功',
              icon: 'success'
            })

            setTimeout(() => {
              // 跳转到订单详情页
              const orderId = result.orderId || result.id
              uni.redirectTo({
                url: `/pages-user/order/detail/index?id=${orderId}`
              })
            }, 1500)
          } else {
            // 多个商家的情况
            uni.hideLoading()
            uni.showToast({
              title: '多商家订单暂不支持',
              icon: 'none'
            })
          }
        } catch (error) {
          console.error('创建订单失败:', error)
          uni.hideLoading()
          uni.showToast({
            title: error.message || '订单创建失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

// 组件挂载时初始化
onMounted(async () => {
  // 从临时存储中读取购物车传递的数据
  try {
    const tempItems = uni.getStorageSync('temp_order_items')
    const tempSummary = uni.getStorageSync('temp_order_summary')

    if (tempItems && tempItems.length > 0) {
      // 将商品按商家分组
      const grouped = {}
      tempItems.forEach(item => {
        if (!grouped[item.merchantId]) {
          grouped[item.merchantId] = {
            merchantId: item.merchantId,
            merchant: item.merchant,
            items: [],
            packingFee: 0
          }
        }
        grouped[item.merchantId].items.push(item)
        // 计算包装费（每份1元）
        grouped[item.merchantId].packingFee += item.quantity * 1
      })

      orderItems.value = Object.values(grouped)
    }

    // 清除临时存储
    uni.removeStorageSync('temp_order_items')
    uni.removeStorageSync('temp_order_summary')
  } catch (error) {
    console.error('读取订单数据失败:', error)
  }

  // 加载默认地址
  await loadDefaultAddress()
})

/**
 * 加载默认地址
 */
const loadDefaultAddress = async () => {
  try {
    if (!userStore.isLogin) {
      return
    }

    const userId = userStore.userInfo?.userId || userStore.userInfo?.id
    const res = await addressApi.getDefault({ userId })

    if (res) {
      selectedAddress.value = {
        id: res.addressId || res.id,
        name: res.receiverName || res.name,
        phone: res.receiverPhone || res.phone,
        address: `${res.province || ''}${res.city || ''}${res.district || ''}${res.detailAddress || res.address || ''}`,
        isDefault: res.isDefault || false
      }
    }
  } catch (error) {
    console.error('加载默认地址失败:', error)
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.order-confirm-container {
  min-height: 100vh;
  background-color: $bg-color-base;
  padding-bottom: 120rpx;
}

.scroll-container {
  height: calc(100vh - 120rpx);
}

.card {
  background-color: $bg-color-white;
  margin-bottom: $spacing-md;
  padding: $spacing-md;
}

.section-label {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $text-color-primary;
  margin-bottom: $spacing-sm;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
  margin-bottom: $spacing-md;
}

/* 地址区域 */
.address-section {
  @include flex-between;
  align-items: center;
}

.address-content {
  flex: 1;
}

.address-header {
  @include flex-center;
  gap: $spacing-md;
  margin-bottom: $spacing-xs;
}

.address-name {
  font-size: $font-size-lg;
  font-weight: $font-weight-medium;
  color: $text-color-primary;
}

.address-phone {
  font-size: $font-size-base;
  color: $text-color-regular;
}

.address-detail {
  @include flex-center;
  gap: $spacing-sm;
}

.address-tag {
  font-size: $font-size-xs;
  color: $primary-color;
  background-color: rgba(255, 107, 53, 0.1);
  padding: 4rpx 8rpx;
  border-radius: 4rpx;
}

.address-text {
  font-size: $font-size-base;
  color: $text-color-regular;
  line-height: $line-height-lg;
}

.address-empty {
  @include flex-center;
  gap: $spacing-sm;
  flex: 1;
}

.empty-icon {
  font-size: 48rpx;
}

.empty-text {
  font-size: $font-size-base;
  color: $text-color-secondary;
}

.address-arrow {
  font-size: 48rpx;
  color: $text-color-secondary;
}

/* 配送时间 */
.delivery-section {
  @include flex-between;
  align-items: center;
}

.delivery-content {
  @include flex-center;
  gap: $spacing-sm;
}

.delivery-text {
  font-size: $font-size-base;
  color: $text-color-regular;
}

.delivery-arrow {
  font-size: 32rpx;
  color: $text-color-secondary;
}

/* 订单商品 */
.order-dishes-section {
  .merchant-group {
    margin-bottom: $spacing-md;
    padding-bottom: $spacing-md;
    border-bottom: 1rpx solid $border-color-light;

    &:last-child {
      margin-bottom: 0;
      padding-bottom: 0;
      border-bottom: none;
    }
  }

  .merchant-info {
    @include flex-center;
    gap: $spacing-sm;
    margin-bottom: $spacing-md;
  }

  .merchant-logo {
    width: 60rpx;
    height: 60rpx;
    border-radius: $border-radius-sm;
  }

  .merchant-name {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $text-color-primary;
  }

  .dish-list {
    .dish-item {
      display: flex;
      margin-bottom: $spacing-md;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }

  .dish-image {
    width: 120rpx;
    height: 120rpx;
    border-radius: $border-radius-base;
    flex-shrink: 0;
  }

  .dish-info {
    flex: 1;
    margin-left: $spacing-sm;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }

  .dish-name {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $text-color-primary;
  }

  .dish-spec {
    font-size: $font-size-sm;
    color: $text-color-secondary;
    margin-top: $spacing-xs;
  }

  .dish-bottom {
    @include flex-between;
    align-items: center;
  }

  .dish-price {
    @include flex-center;
    gap: 2rpx;
    color: $danger-color;
    font-weight: $font-weight-bold;

    .price-symbol {
      font-size: $font-size-sm;
    }

    .price-value {
      font-size: $font-size-lg;
    }
  }

  .dish-quantity {
    font-size: $font-size-sm;
    color: $text-color-secondary;
  }

  .merchant-fee {
    margin-top: $spacing-md;
    padding-top: $spacing-md;
    border-top: 1rpx solid $border-color-light;
  }

  .fee-item {
    @include flex-between;
    padding: $spacing-xs 0;
  }

  .fee-label {
    font-size: $font-size-sm;
    color: $text-color-secondary;
  }

  .fee-value {
    font-size: $font-size-sm;
    color: $text-color-primary;
  }
}

/* 优惠券 */
.coupon-section {
  @include flex-between;
  align-items: center;
}

.coupon-content {
  @include flex-center;
  gap: $spacing-sm;
}

.coupon-text {
  font-size: $font-size-base;
  color: $danger-color;
}

.coupon-arrow {
  font-size: 32rpx;
  color: $text-color-secondary;
}

/* 备注 */
.remark-section {
  .remark-input {
    width: 100%;
    min-height: 120rpx;
    padding: $spacing-sm;
    background-color: $bg-color-base;
    border-radius: $border-radius-base;
    font-size: $font-size-base;
    color: $text-color-primary;
    margin-bottom: $spacing-sm;
  }

  .quick-remarks {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
  }

  .quick-remark-tag {
    font-size: $font-size-sm;
    color: $primary-color;
    background-color: rgba(255, 107, 53, 0.1);
    padding: 8rpx 16rpx;
    border-radius: $border-radius-round;
  }
}

/* 支付方式 */
.payment-section {
  .payment-list {
    .payment-item {
      @include flex-between;
      align-items: center;
      padding: $spacing-md 0;
      border-bottom: 1rpx solid $border-color-light;

      &:last-child {
        border-bottom: none;
      }

      &.active {
        .payment-name {
          color: $primary-color;
        }
      }
    }
  }

  .payment-info {
    @include flex-center;
    gap: $spacing-sm;
  }

  .payment-icon {
    font-size: 36rpx;
  }

  .payment-name {
    font-size: $font-size-base;
    color: $text-color-primary;
  }

  .payment-radio {
    font-size: $font-size-xl;
    color: $primary-color;
  }
}

/* 金额明细 */
.amount-section {
  .amount-item {
    @include flex-between;
    padding: $spacing-sm 0;

    &.total {
      margin-top: $spacing-md;
      padding-top: $spacing-md;
      border-top: 1rpx solid $border-color-light;
    }
  }

  .amount-label {
    font-size: $font-size-base;
    color: $text-color-regular;
  }

  .amount-value {
    font-size: $font-size-base;
    color: $text-color-primary;

    &.discount {
      color: $danger-color;
    }

    &.final {
      font-size: $font-size-xl;
      font-weight: $font-weight-bold;
      color: $danger-color;
    }
  }

  .amount-value-wrapper {
    @include flex-center;
    gap: 2rpx;
  }
}

/* 底部提交栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  @include flex-between;
  align-items: center;
  background-color: $bg-color-white;
  padding: $spacing-md;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
  z-index: $z-index-fixed;
}

.price-section {
  @include flex-center;
  font-size: $font-size-lg;

  .price-label {
    color: $text-color-regular;
  }

  .price-symbol {
    color: $danger-color;
    font-size: $font-size-sm;
  }

  .price-value {
    color: $danger-color;
    font-size: $font-size-xxl;
    font-weight: $font-weight-bold;
  }
}

.submit-btn {
  width: 320rpx;
  height: 80rpx;
  @include flex-center;
  background-color: $primary-color;
  color: #fff;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  border-radius: $border-radius-round;
}
</style>
