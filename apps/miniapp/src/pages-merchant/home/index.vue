<template>
  <view class="merchant-home-container">
    <!-- 加载中 -->
    <view class="state-block" v-if="loading && !merchantInfo">
      <text class="state-text">加载中...</text>
    </view>

    <!-- 错误态：拿不到商家身份或接口失败，不回填任何假数据 -->
    <view class="state-block" v-else-if="loadError">
      <text class="state-text">{{ loadError }}</text>
      <button class="state-btn" @tap="loadPage">重试</button>
    </view>

    <template v-else>
      <!-- 商家头部信息 -->
      <view class="merchant-header">
        <view class="shop-info">
          <image class="shop-logo" :src="merchantInfo.logo" mode="aspectFill"></image>
          <view class="shop-details">
            <text class="shop-name">{{ merchantInfo.name }}</text>
            <text class="shop-status" :class="{ active: isOpen }">
              {{ isOpen ? '营业中' : '已打烊' }}
            </text>
          </view>
        </view>
        <view class="status-switch" @tap="toggleBusinessStatus">
          <text class="switch-text">{{ isOpen ? '打烊' : '开市' }}</text>
        </view>
      </view>

      <!-- 今日数据概览 -->
      <view class="today-stats">
        <view class="section-title">今日数据</view>
        <view class="stats-grid">
          <view class="stat-item">
            <text class="stat-value">{{ todayStats.orders }}</text>
            <text class="stat-label">订单数</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">¥{{ todayStats.revenue }}</text>
            <text class="stat-label">营业额</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ todayStats.customers }}</text>
            <text class="stat-label">顾客数</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">¥{{ todayStats.avgPrice }}</text>
            <text class="stat-label">客单价</text>
          </view>
        </view>
      </view>

      <!-- 快捷操作 -->
      <view class="quick-actions">
        <view class="action-item" @tap="toOrderList">
          <view class="action-icon">📋</view>
          <text class="action-name">订单管理</text>
          <view class="action-badge" v-if="pendingOrders > 0">{{ pendingOrders }}</view>
        </view>
        <view class="action-item" @tap="toDishManage">
          <view class="action-icon">🍜</view>
          <text class="action-name">菜品管理</text>
        </view>
        <view class="action-item" @tap="toStatistics">
          <view class="action-icon">📊</view>
          <text class="action-name">数据统计</text>
        </view>
        <view class="action-item" @tap="toComment">
          <view class="action-icon">⭐</view>
          <text class="action-name">评价中心</text>
          <view class="action-badge" v-if="pendingComments > 0">{{ pendingComments }}</view>
        </view>
      </view>

      <!-- 待处理订单 -->
      <view class="pending-orders" v-if="pendingOrderList.length > 0">
        <view class="section-header">
          <text class="section-title">待处理订单 ({{ pendingOrderList.length }})</text>
          <text class="more" @tap="toOrderList">查看全部 ›</text>
        </view>
        <view class="order-list">
          <view
            class="order-item"
            v-for="order in pendingOrderList"
            :key="order.id"
            @tap="toOrderDetail(order.id)"
          >
            <view class="order-header">
              <text class="order-no">订单号：{{ order.id }}</text>
              <text class="order-time">{{ order.time }}</text>
            </view>
            <view class="order-footer">
              <text class="order-amount">¥{{ order.amount }}</text>
              <view class="order-status" :class="'status-' + order.status">
                {{ order.statusText }}
              </view>
            </view>
          </view>
        </view>
      </view>
      <view class="pending-orders" v-else>
        <view class="section-header">
          <text class="section-title">待处理订单</text>
        </view>
        <text class="empty-text">今天没有待处理的订单</text>
      </view>

      <!-- 营业状态提示 -->
      <view class="business-tips" v-if="!isOpen">
        <view class="tips-icon">💡</view>
        <text class="tips-text">店铺已打烊，开始营业后可接收新订单</text>
      </view>
    </template>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { toOrderDetail } from '@/utils/router'
import { merchantApi } from '@/api/modules/merchant.js'
import { reviewApi } from '@/api/modules/review.js'
import { useMerchantStore } from '@/store/modules/merchant'

const merchantStore = useMerchantStore()

// 商家信息（全部来自服务端；拿不到身份时不回填任何假数据）
const merchantInfo = ref(null)
const merchantId = ref('')
// 营业状态：Merchant.status，true=营业 / false=休息
const isOpen = ref(false)

// 今日统计数据（后端 data.basicStats）
const todayStats = ref({
  orders: 0,
  revenue: '0.00',
  customers: 0,
  avgPrice: '0.00'
})

// 待处理订单
const pendingOrders = ref(0)
const pendingOrderList = ref([])
// 待回复评价数
const pendingComments = ref(0)

// 加载与错误状态
const loading = ref(false)
const loadError = ref('')

// 商家后台只有「待接单 / 制作中」需要商家处理，与桌面端 HomeContent.vue 的 PENDING_STATUS_CODES 口径一致
const PENDING_STATUS_CODES = [1, 2]
const PENDING_STATUS_TEXT = { 1: '待接单', 2: '制作中' }

onMounted(loadPage)

/**
 * 加载整页数据：解析商家身份 → 商家信息 → 今日统计 / 今日订单 / 营业状态
 */
const loadPage = async () => {
  if (loading.value) return

  loading.value = true
  loadError.value = ''

  try {
    // 身份统一由 store 解析；拿不到返回 null，不再回退到写死的商家 id
    const info = await merchantStore.ensureMerchant()
    if (!info) {
      loadError.value = '未获取到商家信息，请重新登录'
      return
    }

    merchantId.value = merchantStore.merchantId
    merchantInfo.value = {
      name: info.name || info.merchantName || '',
      logo: info.logo || info.avatar || ''
    }

    await Promise.all([
      loadBusinessStatus(),
      loadTodayStats(),
      loadTodayOrders(),
      loadPendingComments()
    ])
  } catch (error) {
    console.error('商家首页加载失败:', error)
    loadError.value = error?.message || '加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

/**
 * 加载营业状态
 * 单数 /v1/merchant/{merchantId} 才返回 Merchant.status，复数的 getDetail 响应里没有该字段
 */
const loadBusinessStatus = async () => {
  const res = await merchantApi.getShopDetail(merchantId.value)
  isOpen.value = res.data.status === true
}

/**
 * 加载今日统计
 * GET /v1/merchant/{merchantId}/statistics?timeRange=today（timeRange 为必填参数）
 */
const loadTodayStats = async () => {
  const res = await merchantApi.getStatistics(merchantId.value, { timeRange: 'today' })
  const basicStats = res.data.basicStats || {}

  todayStats.value = {
    orders: Number(basicStats.orders) || 0,
    revenue: formatAmount(basicStats.totalAmount),
    customers: Number(basicStats.newCustomers) || 0,
    avgPrice: formatAmount(basicStats.avgAmount)
  }
}

/**
 * 加载今日待处理订单
 */
const loadTodayOrders = async () => {
  const res = await merchantApi.getTodayOrders(merchantId.value)
  const orders = Array.isArray(res.data) ? res.data : []
  const pending = orders.filter(order => PENDING_STATUS_CODES.includes(Number(order.status)))

  pendingOrders.value = pending.length
  pendingOrderList.value = pending.map(order => ({
    id: order.id,
    time: formatTime(order.createdAt),
    amount: formatAmount(order.totalAmount),
    status: Number(order.status),
    statusText: PENDING_STATUS_TEXT[Number(order.status)]
  }))
}

/**
 * 加载待回复评价数
 * GET /v1/reviews/merchant/{merchantId}/statistics，取 data.unrepliedCount
 */
const loadPendingComments = async () => {
  const res = await reviewApi.getStatistics(merchantId.value)
  pendingComments.value = Number(res.data.unrepliedCount) || 0
}

/**
 * 切换营业状态
 * 服务端对 status 取反且不回传新状态，所以切换后必须重新回读真实状态
 */
const toggleBusinessStatus = async () => {
  const confirmed = await new Promise(resolve => {
    uni.showModal({
      title: '提示',
      content: isOpen.value ? '确认打烊吗？' : '确认开市吗？',
      success: res => resolve(res.confirm === true),
      fail: () => resolve(false)
    })
  })
  if (!confirmed) return

  try {
    await merchantApi.updateStatus(merchantId.value)
    await loadBusinessStatus()
    uni.showToast({
      title: isOpen.value ? '已开市' : '已打烊',
      icon: 'success'
    })
  } catch (error) {
    console.error('切换营业状态失败:', error)
    uni.showToast({
      title: '切换失败，请稍后重试',
      icon: 'none'
    })
  }
}

/**
 * 金额格式化：保留两位小数
 */
const formatAmount = (amount) => {
  const num = Number(amount)
  return Number.isFinite(num) ? num.toFixed(2) : '0.00'
}

/**
 * 时间格式化：后端 createdAt 为 LocalDateTime，取其中的时:分
 */
const formatTime = (time) => {
  const matched = String(time || '').match(/(\d{2}):(\d{2})/)
  return matched ? `${matched[1]}:${matched[2]}` : ''
}

/**
 * 跳转到订单列表
 */
const toOrderList = () => {
  uni.navigateTo({
    url: '/pages-merchant/order/index'
  })
}

/**
 * 跳转到菜品管理
 */
const toDishManage = () => {
  uni.navigateTo({
    url: '/pages-merchant/dish/index'
  })
}

/**
 * 跳转到数据统计
 */
const toStatistics = () => {
  uni.navigateTo({
    url: '/pages-merchant/home/statistics'
  })
}

/**
 * 跳转到评价中心
 */
const toComment = () => {
  uni.navigateTo({
    url: '/pages-merchant/comment/index'
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.merchant-home-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 20rpx;
}

/* 商家头部 */
.merchant-header {
  background: linear-gradient(135deg, #FF6B35, #FF8F6B);
  padding: 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.shop-info {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.shop-logo {
  width: 100rpx;
  height: 100rpx;
  border-radius: 12rpx;
  background: #fff;
}

.shop-details {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.shop-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #fff;
}

.shop-status {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
  background: rgba(0, 0, 0, 0.2);

  &.active {
    background: rgba(82, 196, 26, 0.3);
  }
}

.status-switch {
  padding: 16rpx 32rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 40rpx;
  backdrop-filter: blur(10px);
}

.switch-text {
  font-size: 28rpx;
  color: #fff;
  font-weight: bold;
}

/* 今日统计 */
.today-stats {
  background: #fff;
  margin: 20rpx;
  padding: 30rpx;
  border-radius: 16rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
}

.stat-value {
  font-size: 36rpx;
  font-weight: bold;
  color: #FF6B35;
}

.stat-label {
  font-size: 24rpx;
  color: #999;
}

/* 快捷操作 */
.quick-actions {
  background: #fff;
  margin: 0 20rpx 20rpx;
  padding: 30rpx;
  border-radius: 16rpx;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15rpx;
  position: relative;
}

.action-icon {
  width: 100rpx;
  height: 100rpx;
  @include flex-center;
  background: linear-gradient(135deg, #FFF5F0, #FFE8DB);
  border-radius: 20rpx;
  font-size: 48rpx;
}

.action-name {
  font-size: 26rpx;
  color: #333;
}

.action-badge {
  position: absolute;
  top: -5rpx;
  right: 10rpx;
  min-width: 36rpx;
  height: 36rpx;
  padding: 0 8rpx;
  background: #F5222D;
  color: #fff;
  font-size: 20rpx;
  border-radius: 18rpx;
  @include flex-center;
}

/* 待处理订单 */
.pending-orders {
  background: #fff;
  margin: 0 20rpx 20rpx;
  padding: 30rpx;
  border-radius: 16rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.more {
  font-size: 26rpx;
  color: #FF6B35;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.order-item {
  padding: 24rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15rpx;
}

.order-no {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.order-time {
  font-size: 24rpx;
  color: #999;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-amount {
  font-size: 32rpx;
  font-weight: bold;
  color: #FF6B35;
}

.order-status {
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: bold;

  /* 状态数值口径：1-待接单、2-制作中（后端 Order.status） */
  &.status-1 {
    background: #FFF7E6;
    color: #FAAD14;
  }

  &.status-2 {
    background: #E6F7FF;
    color: #1890FF;
  }
}

/* 营业状态提示 */
.business-tips {
  background: #FFF7E6;
  margin: 0 20rpx;
  padding: 20rpx 30rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  gap: 15rpx;
  border: 1rpx solid #FFD666;
}

.tips-icon {
  font-size: 36rpx;
}

.tips-text {
  flex: 1;
  font-size: 26rpx;
  color: #FF6B35;
}

/* 加载 / 错误态 */
.state-block {
  @include flex-center;
  flex-direction: column;
  gap: 20rpx;
  padding: 120rpx 40rpx;
  margin: 20rpx;
  background: #fff;
  border-radius: 16rpx;
}

.state-text {
  font-size: 28rpx;
  color: #999;
}

.state-btn {
  min-width: 200rpx;
  height: 72rpx;
  @include flex-center;
  background: linear-gradient(135deg, #FF6B35, #FF8F61);
  color: #fff;
  font-size: 28rpx;
  border-radius: 36rpx;
  border: none;
}

/* 空态 */
.empty-text {
  font-size: 26rpx;
  color: #999;
}
</style>
