<template>
  <view class="order-progress-container">
    <!-- 加载状态 -->
    <view class="loading-state" v-if="loading">
      <Loading type="spinner" text="加载中..." />
    </view>

    <!-- 订单进度 -->
    <view class="progress-content" v-else-if="order">
      <!-- 订单状态卡片 -->
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

      <!-- 进度时间轴 -->
      <view class="timeline-section">
        <view class="section-header">
          <text class="section-title">订单进度</text>
        </view>

        <view class="timeline-list">
          <view
            class="timeline-item"
            v-for="(step, index) in timelineSteps"
            :key="index"
            :class="{ active: step.active, completed: step.completed }"
          >
            <!-- 时间轴节点 -->
            <view class="timeline-node">
              <view class="node-icon">
                <text v-if="step.completed">✓</text>
                <text v-else>{{ index + 1 }}</text>
              </view>
              <view class="node-line" v-if="index < timelineSteps.length - 1"></view>
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
      <view class="delivery-section" v-if="order.delivery">
        <view class="section-header">
          <text class="section-title">配送信息</text>
        </view>

        <view class="delivery-card">
          <!-- 骑手信息 -->
          <view class="rider-info" v-if="order.delivery.rider" @click="contactRider">
            <image class="rider-avatar" :src="order.delivery.rider.avatar" mode="aspectFill" />
            <view class="rider-detail">
              <text class="rider-name">{{ order.delivery.rider.name }}</text>
              <text class="rider-phone">{{ order.delivery.rider.phone }}</text>
            </view>
            <view class="contact-btn">
              <text class="btn-icon">📞</text>
            </view>
          </view>

          <!-- 配送地址 -->
          <view class="delivery-address">
            <view class="address-item">
              <text class="address-icon">📍</text>
              <view class="address-detail">
                <text class="address-text">{{ order.delivery.address }}</text>
                <text class="address-contact">{{ order.delivery.contact }} {{ order.delivery.phone }}</text>
              </view>
            </view>
          </view>

          <!-- 配送地图 -->
          <view class="delivery-map" v-if="order.delivery.showMap">
            <map
              class="map-view"
              :latitude="order.delivery.latitude"
              :longitude="order.delivery.longitude"
              :markers="mapMarkers"
              :polyline="mapPolyline"
            />
          </view>
        </view>
      </view>

      <!-- 订单详情 -->
      <view class="order-detail-section">
        <view class="section-header">
          <text class="section-title">订单详情</text>
          <text class="order-no">订单号：{{ order.orderNo }}</text>
        </view>

        <view class="dish-list">
          <view
            class="dish-item"
            v-for="(item, index) in order.items"
            :key="index"
          >
            <image class="dish-image" :src="item.image" mode="aspectFill" />
            <view class="dish-info">
              <text class="dish-name">{{ item.name }}</text>
              <text class="dish-spec" v-if="item.spec">{{ item.spec }}</text>
            </view>
            <view class="dish-price">
              <text class="price-text">¥{{ item.price }}</text>
              <text class="quantity-text">x{{ item.quantity }}</text>
            </view>
          </view>
        </view>

        <view class="order-summary">
          <view class="summary-item">
            <text class="summary-label">商品小计</text>
            <text class="summary-value">¥{{ order.subtotal }}</text>
          </view>
          <view class="summary-item" v-if="order.deliveryFee">
            <text class="summary-label">配送费</text>
            <text class="summary-value">¥{{ order.deliveryFee }}</text>
          </view>
          <view class="summary-item" v-if="order.discount">
            <text class="summary-label">优惠</text>
            <text class="summary-value discount">-¥{{ order.discount }}</text>
          </view>
          <view class="summary-item total">
            <text class="summary-label">实付金额</text>
            <text class="summary-value">¥{{ order.totalAmount }}</text>
          </view>
        </view>
      </view>

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
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import Loading from '@/components/common/Loading.vue'
import Empty from '@/components/common/Empty.vue'
import api from '@/api'

// 订单ID
const orderId = ref('')

// 订单数据
const order = ref(null)

// 加载状态
const loading = ref(true)

// 订单状态映射
const statusConfig = {
  pending: {
    icon: '⏰',
    text: '等待商家接单',
    class: 'pending'
  },
  confirmed: {
    icon: '👨‍🍳',
    text: '商家已接单',
    class: 'confirmed'
  },
  preparing: {
    icon: '🍳',
    text: '正在准备中',
    class: 'preparing'
  },
  ready: {
    icon: '✅',
    text: '餐品已完成',
    class: 'ready'
  },
  delivering: {
    icon: '🚴',
    text: '配送中',
    class: 'delivering'
  },
  completed: {
    icon: '✓',
    text: '已送达',
    class: 'completed'
  },
  cancelled: {
    icon: '✕',
    text: '已取消',
    class: 'cancelled'
  }
}

// 订单状态
const statusClass = computed(() => {
  if (!order.value) return ''
  const config = statusConfig[order.value.status]
  return config ? config.class : ''
})

const statusIcon = computed(() => {
  if (!order.value) return ''
  const config = statusConfig[order.value.status]
  return config ? config.icon : ''
})

const statusText = computed(() => {
  if (!order.value) return ''
  const config = statusConfig[order.value.status]
  return config ? config.text : '未知状态'
})

// 时间轴步骤
const timelineSteps = computed(() => {
  if (!order.value) return []

  const steps = [
    {
      title: '提交订单',
      completed: true,
      active: false,
      time: order.value.createdAt
    },
    {
      title: '商家接单',
      completed: ['confirmed', 'preparing', 'ready', 'delivering', 'completed'].includes(order.value.status),
      active: order.value.status === 'pending',
      time: order.value.confirmedAt
    },
    {
      title: '准备餐品',
      completed: ['ready', 'delivering', 'completed'].includes(order.value.status),
      active: order.value.status === 'preparing',
      time: order.value.preparingAt,
      desc: '预计15分钟'
    },
    {
      title: '等待配送',
      completed: ['delivering', 'completed'].includes(order.value.status),
      active: order.value.status === 'ready',
      time: order.value.readyAt
    }
  ]

  if (order.value.delivery) {
    steps.push({
      title: '配送中',
      completed: order.value.status === 'completed',
      active: order.value.status === 'delivering',
      time: order.value.deliveringAt,
      desc: order.value.delivery.rider ? `骑手：${order.value.delivery.rider.name}` : '等待骑手接单'
    })
  }

  steps.push({
    title: '已送达',
    completed: order.value.status === 'completed',
    active: order.value.status === 'completed',
    time: order.value.completedAt
  })

  return steps
})

// 地图标记
const mapMarkers = computed(() => {
  if (!order.value || !order.value.delivery) return []

  const markers = []

  // 商家位置
  if (order.value.merchant.latitude && order.value.merchant.longitude) {
    markers.push({
      id: 1,
      latitude: order.value.merchant.latitude,
      longitude: order.value.merchant.longitude,
      iconPath: '/static/marker-merchant.png',
      width: 30,
      height: 30
    })
  }

  // 骑手位置
  if (order.value.delivery.rider && order.value.delivery.rider.latitude) {
    markers.push({
      id: 2,
      latitude: order.value.delivery.rider.latitude,
      longitude: order.value.delivery.rider.longitude,
      iconPath: '/static/marker-rider.png',
      width: 30,
      height: 30
    })
  }

  // 收货地址
  if (order.value.delivery.latitude && order.value.delivery.longitude) {
    markers.push({
      id: 3,
      latitude: order.value.delivery.latitude,
      longitude: order.value.delivery.longitude,
      iconPath: '/static/marker-address.png',
      width: 30,
      height: 30
    })
  }

  return markers
})

// 地图路线
const mapPolyline = computed(() => {
  if (!order.value || !order.value.delivery) return []

  const points = []
  if (order.value.merchant.latitude && order.value.merchant.longitude) {
    points.push({
      latitude: order.value.merchant.latitude,
      longitude: order.value.merchant.longitude
    })
  }
  if (order.value.delivery.latitude && order.value.delivery.longitude) {
    points.push({
      latitude: order.value.delivery.latitude,
      longitude: order.value.delivery.longitude
    })
  }

  if (points.length < 2) return []

  return [{
    points,
    color: '#FF6B35',
    width: 4,
    dottedLine: true
  }]
})

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
const contactRider = () => {
  if (!order.value.delivery.rider || !order.value.delivery.rider.phone) {
    uni.showToast({
      title: '骑手电话暂无',
      icon: 'none'
    })
    return
  }

  uni.makePhoneCall({
    phoneNumber: order.value.delivery.rider.phone
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
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.order-progress-container {
  min-height: 100vh;
  background-color: $bg-color-base;
  padding-bottom: 120rpx;
}

/* 加载状态 */
.loading-state {
  @include flex-center;
  height: 100vh;
}

/* 状态卡片 */
.status-card {
  margin: $spacing-md;
  padding: $spacing-xl;
  border-radius: $border-radius-lg;
  box-shadow: $box-shadow-md;

  &.pending {
    background: linear-gradient(135deg, #FFB74D, #FF9800);
  }

  &.confirmed,
  &.preparing {
    background: linear-gradient(135deg, #64B5F6, #42A5F5);
  }

  &.ready {
    background: linear-gradient(135deg, #81C784, #66BB6A);
  }

  &.delivering {
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

/* 通用区块样式 */
.timeline-section,
.merchant-section,
.delivery-section,
.order-detail-section,
.remark-section {
  background-color: $bg-color-white;
  margin: $spacing-md;
  padding: $spacing-lg;
  border-radius: $border-radius-lg;
  box-shadow: $box-shadow-sm;
}

.section-header {
  @include flex-between;
  align-items: center;
  margin-bottom: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.order-no {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

/* 时间轴 */
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

/* 商家卡片 */
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

/* 配送卡片 */
.rider-info {
  @include flex-center;
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  margin-bottom: $spacing-md;

  &:active {
    background-color: rgba(0, 0, 0, 0.02);
  }
}

.rider-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.rider-detail {
  flex: 1;
  margin-left: $spacing-md;
  @include flex-center-column;
  align-items: flex-start;
  gap: 4rpx;
}

.rider-name {
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.rider-phone {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.delivery-address {
  @include flex-center-column;
  gap: $spacing-md;
}

.address-item {
  @include flex-start;
  gap: $spacing-md;
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
}

.address-icon {
  font-size: $font-size-xl;
  flex-shrink: 0;
}

.address-detail {
  flex: 1;
  @include flex-center-column;
  align-items: flex-start;
  gap: 4rpx;
}

.address-text {
  font-size: $font-size-base;
  color: $text-color-primary;
}

.address-contact {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.delivery-map {
  margin-top: $spacing-md;
  border-radius: $border-radius-base;
  overflow: hidden;
}

.map-view {
  width: 100%;
  height: 400rpx;
}

/* 订单详情 */
.dish-list {
  @include flex-center-column;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
}

.dish-item {
  @include flex-center;
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
}

.dish-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: $border-radius-base;
  flex-shrink: 0;
}

.dish-info {
  flex: 1;
  margin-left: $spacing-md;
  @include flex-center-column;
  align-items: flex-start;
  gap: 4rpx;
}

.dish-name {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $text-color-primary;
}

.dish-spec {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.dish-price {
  @include flex-center-column;
  align-items: flex-end;
  gap: 4rpx;
}

.price-text {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $primary-color;
}

.quantity-text {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.order-summary {
  padding-top: $spacing-md;
  border-top: 1rpx solid $border-color-lighter;
}

.summary-item {
  @include flex-between;
  margin-bottom: $spacing-sm;

  &.total {
    padding-top: $spacing-sm;
    border-top: 1rpx solid $border-color-lighter;
    margin-top: $spacing-sm;
  }
}

.summary-label {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.summary-value {
  font-size: $font-size-sm;
  color: $text-color-primary;

  &.discount {
    color: $success-color;
  }

  &.total {
    font-size: $font-size-lg;
    font-weight: $font-weight-bold;
    color: $primary-color;
  }
}

/* 订单备注 */
.remark-text {
  font-size: $font-size-base;
  color: $text-color-regular;
  line-height: $line-height-lg;
}

/* 底部操作栏 */
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

/* 空状态 */
.empty-state {
  padding: 120rpx $spacing-lg;
}
</style>
