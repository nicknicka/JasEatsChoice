<template>
  <view class="order-progress-container">
    <!-- 加载状态 -->
    <view class="loading-state" v-if="loading">
      <Loading type="spinner" text="加载中..." />
    </view>

    <!-- 订单进度 -->
    <view class="progress-content" v-else-if="order">
      <!-- 订单状态卡片 -->
      <OrderStatusCard :order="order" />

      <!-- 进度时间轴 -->
      <TimelineSection :order="order" />

      <!-- 商家信息 -->
      <view class="merchant-section">
        <view class="section-header">
          <text class="section-title">商家信息</text>
        </view>

        <view class="merchant-card" @click="contactMerchant">
          <image class="merchant-image" :src="order.merchant.image" mode="aspectFill" />
          <view class="merchant-info">
            <text class="merchant-name">{{ order.merchant.name }}</text>
            <text class="merchant-address">{{ order.merchant.address }}</text>
          </view>
          <view class="contact-btn">
            <text class="btn-icon">📞</text>
          </view>
        </view>
      </view>

      <!-- 配送信息 -->
      <DeliveryInfo
        :order="order"
        :showMap="true"
        @contactRider="contactRider"
      />

      <!-- 订单详情 -->
      <OrderDetail :order="order" />

      <!-- 订单备注 -->
      <view class="remark-section" v-if="order.remark">
        <view class="section-header">
          <text class="section-title">订单备注</text>
        </view>
        <text class="remark-text">{{ order.remark }}</text>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-else>
      <Empty
        icon="📦"
        text="订单不存在"
        description="该订单可能已被删除"
      />
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar" v-if="order">
      <button
        class="action-btn secondary"
        v-if="showCancelButton"
        @click="cancelOrder"
      >
        取消订单
      </button>
      <button
        class="action-btn primary"
        v-if="showConfirmButton"
        @click="confirmReceipt"
      >
        确认收货
      </button>
      <button
        class="action-btn outline"
        v-if="showContactButton"
        @click="contactService"
      >
        联系客服
      </button>
      <button
        class="action-btn outline"
        @click="viewOrderDetail"
      >
        订单详情
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import Loading from '@/components/common/Loading.vue'
import Empty from '@/components/common/Empty.vue'
import OrderStatusCard from './components/OrderStatusCard.vue'
import TimelineSection from './components/TimelineSection.vue'
import DeliveryInfo from './components/DeliveryInfo.vue'
import OrderDetail from './components/OrderDetail.vue'
import api from '@/api'

// 订单ID
const orderId = ref('')

// 订单数据
const order = ref(null)

// 加载状态
const loading = ref(true)

// 按钮显示
const showCancelButton = computed(() => {
  return order.value && ['pending', 'confirmed'].includes(order.value.status)
})

const showConfirmButton = computed(() => {
  return order.value && order.value.status === 'delivering'
})

const showContactButton = computed(() => {
  return order.value && ['pending', 'confirmed', 'preparing', 'ready'].includes(order.value.status)
})

/**
 * 加载订单进度
 */
const loadOrderProgress = async () => {
  loading.value = true

  try {
    const res = await api.order.getOrderProgress(orderId.value)
    order.value = res.data
  } catch (error) {
    console.error('加载订单进度失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 联系商家
 */
const contactMerchant = () => {
  if (!order.value.merchant.phone) {
    uni.showToast({
      title: '商家电话暂无',
      icon: 'none'
    })
    return
  }

  uni.makePhoneCall({
    phoneNumber: order.value.merchant.phone
  })
}

/**
 * 联系骑手
 */
const contactRider = (rider) => {
  if (!rider || !rider.phone) {
    uni.showToast({
      title: '骑手电话暂无',
      icon: 'none'
    })
    return
  }

  uni.makePhoneCall({
    phoneNumber: rider.phone
  })
}

/**
 * 取消订单
 */
const cancelOrder = () => {
  uni.showModal({
    title: '取消订单',
    content: '确定要取消此订单吗？',
    confirmColor: '#FF6B35',
    success: async (res) => {
      if (res.confirm) {
        try {
          await api.order.cancelOrder(orderId.value)
          uni.showToast({
            title: '订单已取消',
            icon: 'success'
          })
          setTimeout(() => {
            loadOrderProgress()
          }, 1500)
        } catch (error) {
          console.error('取消订单失败:', error)
          uni.showToast({
            title: '取消失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 确认收货
 */
const confirmReceipt = () => {
  uni.showModal({
    title: '确认收货',
    content: '确认已收到餐品吗？',
    confirmColor: '#FF6B35',
    success: async (res) => {
      if (res.confirm) {
        try {
          await api.order.confirmReceipt(orderId.value)
          uni.showToast({
            title: '已确认收货',
            icon: 'success'
          })
          setTimeout(() => {
            loadOrderProgress()
          }, 1500)
        } catch (error) {
          console.error('确认收货失败:', error)
          uni.showToast({
            title: '操作失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 联系客服
 */
const contactService = () => {
  uni.navigateTo({
    url: '/pages/service/chat/index'
  })
}

/**
 * 查看订单详情
 */
const viewOrderDetail = () => {
  uni.navigateTo({
    url: `/pages/order/detail/index?id=${orderId.value}`
  })
}

// 页面加载
onLoad((options) => {
  orderId.value = options.id
  loadOrderProgress()
})

// 定时刷新订单状态
let refreshTimer = null

onMounted(() => {
  refreshTimer = setInterval(() => {
    if (order.value && !['completed', 'cancelled'].includes(order.value.status)) {
      loadOrderProgress()
    }
  }, 30000) // 30秒刷新一次
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.order-progress-container {
  min-height: 100vh;
  background-color: $bg-color-base;
  padding-bottom: 120rpx;
}

.loading-state {
  @include flex-center;
  height: 100vh;
}

.merchant-section {
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

.merchant-card {
  @include flex-center;
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;

  &:active {
    background-color: rgba(0, 0, 0, 0.02);
  }
}

.merchant-image {
  width: 96rpx;
  height: 96rpx;
  border-radius: $border-radius-base;
  flex-shrink: 0;
}

.merchant-info {
  flex: 1;
  margin-left: $spacing-md;
  @include flex-center-column;
  align-items: flex-start;
  gap: $spacing-xs;
}

.merchant-name {
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.merchant-address {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.contact-btn {
  width: 72rpx;
  height: 72rpx;
  @include flex-center;
  background-color: $primary-color;
  border-radius: 50%;
  flex-shrink: 0;

  &:active {
    opacity: 0.8;
  }
}

.btn-icon {
  font-size: $font-size-xl;
  color: #fff;
}

.remark-section {
  background-color: $bg-color-white;
  margin: $spacing-md;
  padding: $spacing-lg;
  border-radius: $border-radius-lg;
  box-shadow: $box-shadow-sm;
}

.remark-text {
  font-size: $font-size-base;
  color: $text-color-regular;
  line-height: $line-height-lg;
}

.empty-state {
  padding: 120rpx $spacing-lg;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: $bg-color-white;
  padding: $spacing-md;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
  z-index: $z-index-fixed;
  @include flex-center;
  gap: $spacing-md;
  @include safe-area-bottom;
}

.action-btn {
  flex: 1;
  height: 88rpx;
  @include flex-center;
  border-radius: $border-radius-round;
  font-size: $font-size-base;
  border: none;

  &.primary {
    background: linear-gradient(135deg, $primary-color, #FF8F61);
    color: #fff;
  }

  &.secondary {
    background-color: $bg-color-base;
    color: $text-color-primary;
  }

  &.outline {
    background-color: $bg-color-white;
    color: $primary-color;
    border: 2rpx solid $primary-color;
  }

  &:active {
    transform: scale(0.98);
  }
}
</style>
