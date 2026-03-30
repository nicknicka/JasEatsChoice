<template>
  <view class="coupons-container">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <view class="nav-title">优惠券</view>
      <view class="nav-placeholder"></view>
    </view>

    <!-- 分类标签 -->
    <view class="category-tabs">
      <view
        class="category-tab"
        v-for="tab in categoryTabs"
        :key="tab.value"
        :class="{ active: currentCategory === tab.value }"
        @click="switchCategory(tab.value)"
      >
        <text class="tab-text">{{ tab.label }}</text>
        <view class="tab-badge" v-if="tab.count > 0">{{ tab.count }}</view>
      </view>
    </view>

    <!-- 优惠券列表 -->
    <scroll-view
      class="coupons-scroll"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 空状态 -->
      <view class="empty-container" v-if="filteredCoupons.length === 0 && !loading">
        <text class="empty-icon">🎫</text>
        <text class="empty-text">{{ getEmptyText() }}</text>
        <text class="empty-desc">{{ getEmptyDesc() }}</text>
        <button class="explore-btn" v-if="currentCategory === 'available'" @click="goToExplore">去领券</button>
      </view>

      <!-- 优惠券列表 -->
      <view class="coupons-list" v-else>
        <view
          class="coupon-item"
          v-for="coupon in filteredCoupons"
          :key="coupon.id"
          :class="[
            'status-' + coupon.status,
            { disabled: coupon.status !== 'available' }
          ]"
        >
          <!-- 左侧金额区域 -->
          <view class="coupon-left">
            <view class="coupon-amount-wrapper">
              <text class="coupon-currency">¥</text>
              <text class="coupon-amount">{{ coupon.amount }}</text>
            </view>
            <text class="coupon-condition">{{ coupon.condition }}</text>
          </view>

          <!-- 分割线 -->
          <view class="coupon-divider">
            <view class="divider-circle top"></view>
            <view class="divider-line"></view>
            <view class="divider-circle bottom"></view>
          </view>

          <!-- 右侧信息区域 -->
          <view class="coupon-right">
            <view class="coupon-header">
              <text class="coupon-name">{{ coupon.name }}</text>
              <view class="coupon-status-badge" :class="'badge-' + coupon.status">
                {{ getStatusText(coupon.status) }}
              </view>
            </view>

            <text class="coupon-desc">{{ coupon.description }}</text>

            <view class="coupon-meta">
              <text class="meta-item" v-if="coupon.minAmount">满¥{{ coupon.minAmount }}可用</text>
              <text class="meta-item" v-if="coupon.validDays">{{ coupon.validDays }}天有效</text>
            </view>

            <view class="coupon-footer">
              <text class="coupon-time">{{ formatDateRange(coupon.startTime, coupon.endTime) }}</text>

              <!-- 操作按钮 -->
              <view class="coupon-actions">
                <button
                  v-if="coupon.status === 'available'"
                  class="action-btn use-btn"
                  @click="useCoupon(coupon)"
                >
                  立即使用
                </button>
                <button
                  v-else-if="coupon.status === 'expired'"
                  class="action-btn expired-btn"
                  @click="viewDetail(coupon)"
                >
                  查看详情
                </button>
                <text
                  v-else-if="coupon.status === 'used'"
                  class="used-text"
                  @click="viewDetail(coupon)"
                >
                  已使用
                </text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 优惠券详情弹窗 -->
    <view class="coupon-modal" v-if="showModal" @click="closeModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">优惠券详情</text>
          <text class="modal-close" @click="closeModal">×</text>
        </view>

        <view class="modal-body" v-if="selectedCoupon">
          <view class="detail-amount">
            <text class="amount-currency">¥</text>
            <text class="amount-value">{{ selectedCoupon.amount }}</text>
          </view>

          <view class="detail-info">
            <view class="info-row">
              <text class="info-label">优惠券名称</text>
              <text class="info-value">{{ selectedCoupon.name }}</text>
            </view>

            <view class="info-row">
              <text class="info-label">使用条件</text>
              <text class="info-value">{{ selectedCoupon.condition }}</text>
            </view>

            <view class="info-row">
              <text class="info-label">适用范围</text>
              <text class="info-value">{{ selectedCoupon.scope }}</text>
            </view>

            <view class="info-row">
              <text class="info-label">有效期</text>
              <text class="info-value">{{ formatDateRange(selectedCoupon.startTime, selectedCoupon.endTime) }}</text>
            </view>

            <view class="info-row" v-if="selectedCoupon.description">
              <text class="info-label">使用说明</text>
              <text class="info-value">{{ selectedCoupon.description }}</text>
            </view>
          </view>

          <button class="modal-close-btn" @click="closeModal">关闭</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { couponApi } from '@/api'

// 当前分类
const currentCategory = ref('available')

// 分类标签
const categoryTabs = ref([
  { label: '可使用', value: 'available', count: 0 },
  { label: '已使用', value: 'used', count: 0 },
  { label: '已过期', value: 'expired', count: 0 }
])

// 优惠券列表
const coupons = ref([])

// 加载状态
const loading = ref(false)
const refreshing = ref(false)

// 弹窗状态
const showModal = ref(false)
const selectedCoupon = ref(null)

// 组件挂载
onMounted(() => {
  loadCoupons()
})

// 过滤后的优惠券列表
const filteredCoupons = computed(() => {
  return coupons.value.filter(coupon => coupon.status === currentCategory.value)
})

/**
 * 加载优惠券列表
 */
const loadCoupons = async () => {
  loading.value = true

  try {
    // TODO: 调用真实API
    // const res = await couponApi.getList()

    // 模拟数据
    const now = new Date()
    const tomorrow = new Date(now)
    tomorrow.setDate(tomorrow.getDate() + 1)

    const nextWeek = new Date(now)
    nextWeek.setDate(nextWeek.getDate() + 7)

    const yesterday = new Date(now)
    yesterday.setDate(yesterday.getDate() - 1)

    const mockData = [
      {
        id: 1,
        name: '新用户专享券',
        amount: 10,
        condition: '满30可用',
        description: '新用户首次下单专享优惠',
        minAmount: 30,
        scope: '全场通用',
        startTime: now.toISOString(),
        endTime: nextWeek.toISOString(),
        status: 'available',
        validDays: 7
      },
      {
        id: 2,
        name: '午餐特惠券',
        amount: 5,
        condition: '满20可用',
        description: '仅限午餐时段使用（11:00-14:00）',
        minAmount: 20,
        scope: '午餐时段',
        startTime: now.toISOString(),
        endTime: nextWeek.toISOString(),
        status: 'available',
        validDays: 7
      },
      {
        id: 3,
        name: '满50减15',
        amount: 15,
        condition: '满50可用',
        description: '全场通用，无门槛',
        minAmount: 50,
        scope: '全场通用',
        startTime: now.toISOString(),
        endTime: nextWeek.toISOString(),
        status: 'available',
        validDays: 7
      },
      {
        id: 4,
        name: '已使用优惠券',
        amount: 8,
        condition: '满25可用',
        description: '此优惠券已使用',
        minAmount: 25,
        scope: '全场通用',
        startTime: yesterday.toISOString(),
        endTime: tomorrow.toISOString(),
        status: 'used',
        validDays: 7
      },
      {
        id: 5,
        name: '过期优惠券',
        amount: 20,
        condition: '满60可用',
        description: '此优惠券已过期',
        minAmount: 60,
        scope: '全场通用',
        startTime: yesterday.toISOString(),
        endTime: yesterday.toISOString(),
        status: 'expired',
        validDays: 7
      }
    ]

    coupons.value = mockData

    // 更新分类数量
    categoryTabs.value[0].count = mockData.filter(c => c.status === 'available').length
    categoryTabs.value[1].count = mockData.filter(c => c.status === 'used').length
    categoryTabs.value[2].count = mockData.filter(c => c.status === 'expired').length
  } catch (error) {
    console.error('加载优惠券失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 下拉刷新
 */
const onRefresh = async () => {
  refreshing.value = true
  await loadCoupons()
  refreshing.value = false
}

/**
 * 切换分类
 */
const switchCategory = (category) => {
  currentCategory.value = category
}

/**
 * 使用优惠券
 */
const useCoupon = (coupon) => {
  // 跳转到订单页面或商品列表
  uni.showModal({
    title: '提示',
    content: '是否前往使用此优惠券？',
    success: (res) => {
      if (res.confirm) {
        uni.switchTab({
          url: '/pages/home/index'
        })
      }
    }
  })
}

/**
 * 查看详情
 */
const viewDetail = (coupon) => {
  selectedCoupon.value = coupon
  showModal.value = true
}

/**
 * 关闭弹窗
 */
const closeModal = () => {
  showModal.value = false
  selectedCoupon.value = null
}

/**
 * 获取状态文本
 */
const getStatusText = (status) => {
  const statusMap = {
    'available': '可使用',
    'used': '已使用',
    'expired': '已过期'
  }
  return statusMap[status] || '未知'
}

/**
 * 格式化日期范围
 */
const formatDateRange = (start, end) => {
  if (!start || !end) return ''

  const startDate = new Date(start)
  const endDate = new Date(end)

  const startStr = `${startDate.getMonth() + 1}.${startDate.getDate()}`
  const endStr = `${endDate.getMonth() + 1}.${endDate.getDate()}`

  return `${startStr} - ${endStr}`
}

/**
 * 获取空状态文本
 */
const getEmptyText = () => {
  if (currentCategory.value === 'available') return '暂无可用优惠券'
  if (currentCategory.value === 'used') return '暂无已使用的优惠券'
  if (currentCategory.value === 'expired') return '暂无过期优惠券'
  return '暂无优惠券'
}

/**
 * 获取空状态描述
 */
const getEmptyDesc = () => {
  if (currentCategory.value === 'available') return '快去领取更多优惠券吧~'
  return ''
}

/**
 * 去领券
 */
const goToExplore = () => {
  uni.switchTab({
    url: '/pages/home/index'
  })
}

/**
 * 返回上一页
 */
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.coupons-container {
  min-height: 100vh;
  background-color: $bg-color-base;
  display: flex;
  flex-direction: column;
}

/* 导航栏 */
.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 88rpx;
  background-color: $bg-color-white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 $spacing-md;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.nav-back,
.nav-placeholder {
  width: 88rpx;
  height: 88rpx;
  @include flex-center;
}

.back-icon {
  font-size: 48rpx;
  color: $text-color-primary;
  font-weight: bold;
}

.nav-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

/* 分类标签 */
.category-tabs {
  position: fixed;
  top: 88rpx;
  left: 0;
  right: 0;
  background-color: $bg-color-white;
  display: flex;
  padding: $spacing-md;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  z-index: 99;
}

.category-tab {
  position: relative;
  flex: 1;
  height: 64rpx;
  @include flex-center;
  border-radius: $border-radius-round;
  margin: 0 $spacing-xs;
  background-color: $bg-color-base;
  transition: all 0.3s;

  &.active {
    background-color: $primary-color;

    .tab-text {
      color: #fff;
      font-weight: $font-weight-bold;
    }
  }
}

.tab-text {
  font-size: $font-size-base;
  color: $text-color-primary;
}

.tab-badge {
  position: absolute;
  top: -4rpx;
  right: 8rpx;
  min-width: 32rpx;
  height: 32rpx;
  @include flex-center;
  padding: 0 8rpx;
  background-color: $danger-color;
  border-radius: $border-radius-round;
  font-size: $font-size-xs;
  color: #fff;
  font-weight: $font-weight-bold;
}

/* 优惠券列表 */
.coupons-scroll {
  flex: 1;
  margin-top: 200rpx;
  padding: $spacing-md;
  padding-bottom: env(safe-area-inset-bottom);
}

/* 空状态 */
.empty-container {
  @include flex-center-column;
  padding: 200rpx $spacing-xl;
  text-align: center;
}

.empty-icon {
  font-size: 160rpx;
  margin-bottom: $spacing-lg;
}

.empty-text {
  font-size: $font-size-xl;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
  margin-bottom: $spacing-sm;
}

.empty-desc {
  font-size: $font-size-base;
  color: $text-color-secondary;
  margin-bottom: $spacing-xl;
}

.explore-btn {
  padding: $spacing-md $spacing-xl;
  background-color: $primary-color;
  color: #fff;
  border-radius: $border-radius-round;
  font-size: $font-size-base;
  border: none;
}

/* 优惠券列表 */
.coupons-list {
  .coupon-item {
    display: flex;
    background-color: $bg-color-white;
    border-radius: $border-radius-lg;
    margin-bottom: $spacing-md;
    overflow: hidden;
    box-shadow: $box-shadow-sm;
    position: relative;

    &.status-available {
      border-left: 8rpx solid $primary-color;
    }

    &.status-used {
      border-left: 8rpx solid $text-color-secondary;
      opacity: 0.7;
    }

    &.status-expired {
      border-left: 8rpx solid $text-color-placeholder;
      opacity: 0.5;
    }

    &.disabled {
      pointer-events: none;
    }
  }
}

/* 左侧金额区域 */
.coupon-left {
  width: 200rpx;
  background: linear-gradient(135deg, #FF6B35, #FF8C61);
  padding: $spacing-lg $spacing-md;
  @include flex-center-column;
  justify-content: center;
  align-items: center;
}

.coupon-amount-wrapper {
  @include flex-center;
  margin-bottom: $spacing-xs;
}

.coupon-currency {
  font-size: $font-size-base;
  color: #fff;
  font-weight: $font-weight-bold;
}

.coupon-amount {
  font-size: 64rpx;
  color: #fff;
  font-weight: $font-weight-bold;
  line-height: 1;
}

.coupon-condition {
  font-size: $font-size-xs;
  color: rgba(255, 255, 255, 0.9);
}

/* 分割线 */
.coupon-divider {
  position: relative;
  width: 4rpx;
  background-color: $bg-color-base;
}

.divider-circle {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  width: 24rpx;
  height: 24rpx;
  background-color: $bg-color-base;
  border-radius: 50%;

  &.top {
    top: -12rpx;
  }

  &.bottom {
    bottom: -12rpx;
  }
}

.divider-line {
  position: absolute;
  left: 50%;
  top: 12rpx;
  bottom: 12rpx;
  width: 2rpx;
  background-color: $bg-color-base;
  transform: translateX(-50%);
}

/* 右侧信息区域 */
.coupon-right {
  flex: 1;
  padding: $spacing-md;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.coupon-header {
  @include flex-between;
  align-items: flex-start;
}

.coupon-name {
  flex: 1;
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
  line-height: 1.4;
}

.coupon-status-badge {
  padding: 4rpx 12rpx;
  border-radius: $border-radius-round;
  font-size: $font-size-xs;
  font-weight: $font-weight-bold;

  &.badge-available {
    background-color: rgba($success-color, 0.1);
    color: $success-color;
  }

  &.badge-used {
    background-color: rgba($text-color-secondary, 0.1);
    color: $text-color-secondary;
  }

  &.badge-expired {
    background-color: rgba($text-color-placeholder, 0.1);
    color: $text-color-placeholder;
  }
}

.coupon-desc {
  font-size: $font-size-sm;
  color: $text-color-secondary;
  line-height: 1.4;
}

.coupon-meta {
  @include flex-center;
  gap: $spacing-sm;
  flex-wrap: wrap;
}

.meta-item {
  font-size: $font-size-xs;
  color: $text-color-placeholder;
  padding: 4rpx 8rpx;
  background-color: $bg-color-base;
  border-radius: 4rpx;
}

.coupon-footer {
  @include flex-between;
  align-items: flex-end;
  margin-top: auto;
}

.coupon-time {
  font-size: $font-size-xs;
  color: $text-color-placeholder;
}

.action-btn {
  padding: $spacing-xs $spacing-lg;
  border-radius: $border-radius-round;
  font-size: $font-size-sm;
  border: none;

  &::after {
    border: none;
  }
}

.use-btn {
  background-color: $primary-color;
  color: #fff;
}

.expired-btn {
  background-color: $bg-color-base;
  color: $text-color-secondary;
}

.used-text {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

/* 详情弹窗 */
.coupon-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  z-index: 1000;
  @include flex-center;
}

.modal-content {
  width: 600rpx;
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  overflow: hidden;
}

.modal-header {
  @include flex-between;
  padding: $spacing-lg;
  border-bottom: 1rpx solid $border-color-lighter;
}

.modal-title {
  font-size: $font-size-lg;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
}

.modal-close {
  font-size: 48rpx;
  color: $text-color-secondary;
  line-height: 1;
  padding: 0 $spacing-xs;
}

.modal-body {
  padding: $spacing-xl;
  @include flex-center-column;
}

.detail-amount {
  @include flex-center;
  margin-bottom: $spacing-xl;
}

.amount-currency {
  font-size: $font-size-xl;
  color: $primary-color;
  font-weight: $font-weight-bold;
}

.amount-value {
  font-size: 120rpx;
  color: $primary-color;
  font-weight: $font-weight-bold;
  line-height: 1;
}

.detail-info {
  width: 100%;
  margin-bottom: $spacing-xl;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: $spacing-md 0;
  border-bottom: 1rpx solid $border-color-lighter;

  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  font-size: $font-size-base;
  color: $text-color-secondary;
}

.info-value {
  font-size: $font-size-base;
  color: $text-color-primary;
  text-align: right;
  flex: 1;
  margin-left: $spacing-lg;
}

.modal-close-btn {
  width: 100%;
  padding: $spacing-md;
  background-color: $primary-color;
  color: #fff;
  border-radius: $border-radius-lg;
  font-size: $font-size-base;
  border: none;
}
</style>
