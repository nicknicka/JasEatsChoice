<template>
  <view class="merchant-home-container">
    <!-- 商家头部信息 -->
    <view class="merchant-header">
      <view class="shop-info">
        <image class="shop-logo" :src="merchantInfo.logo" mode="aspectFill"></image>
        <view class="shop-details">
          <text class="shop-name">{{ merchantInfo.name }}</text>
          <text class="shop-status" :class="{ active: merchantInfo.isOpen }">
            {{ merchantInfo.isOpen ? '营业中' : '已打烊' }}
          </text>
        </view>
      </view>
      <view class="status-switch" @tap="toggleBusinessStatus">
        <text class="switch-text">{{ merchantInfo.isOpen ? '打烊' : '开市' }}</text>
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
          <text class="stat-value">{{ todayStats.avgPrice }}</text>
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
            <text class="order-no">订单号：{{ order.orderNo }}</text>
            <text class="order-time">{{ order.time }}</text>
          </view>
          <view class="order-content">
            <text class="order-dishes">{{ order.dishes }}</text>
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

    <!-- 营业状态提示 -->
    <view class="business-tips" v-if="!merchantInfo.isOpen">
      <view class="tips-icon">💡</view>
      <text class="tips-text">店铺已打烊，开始营业后可接收新订单</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { toOrderDetail } from '@/utils/router'

// 商家信息
const merchantInfo = ref({
  id: 1,
  name: '老王家常菜',
  logo: 'https://via.placeholder.com/100/FF6B35/FFFFFF?text=店',
  isOpen: true
})

// 今日统计数据
const todayStats = ref({
  orders: 28,
  revenue: '1,680',
  customers: 32,
  avgPrice: '52.5'
})

// 待处理订单数
const pendingOrders = ref(3)
const pendingComments = ref(5)

// 待处理订单列表
const pendingOrderList = ref([])

onMounted(() => {
  loadMerchantInfo()
  loadTodayStats()
  loadPendingOrders()
})

/**
 * 加载商家信息
 */
const loadMerchantInfo = () => {
  // TODO: 调用API获取商家信息
  // const res = await merchantApi.getInfo()
  // merchantInfo.value = res.data
}

/**
 * 加载今日统计
 */
const loadTodayStats = () => {
  // TODO: 调用API获取今日统计
  // const res = await merchantApi.getTodayStats()
  // todayStats.value = res.data
}

/**
 * 加载待处理订单
 */
const loadPendingOrders = () => {
  // TODO: 调用API获取待处理订单
  pendingOrderList.value = [
    {
      id: 1,
      orderNo: 'OD202603180001',
      time: '12:30',
      dishes: '宫保鸡丁 x1, 鱼香肉丝 x1',
      amount: '54.00',
      status: 'pending',
      statusText: '待接单'
    },
    {
      id: 2,
      orderNo: 'OD202603180002',
      time: '12:25',
      dishes: '回锅肉 x1, 青菜 x2',
      amount: '48.00',
      status: 'cooking',
      statusText: '制作中'
    },
    {
      id: 3,
      orderNo: 'OD202603180003',
      time: '12:15',
      dishes: '麻婆豆腐 x1',
      amount: '18.00',
      status: 'ready',
      statusText: '待配送'
    }
  ]
}

/**
 * 切换营业状态
 */
const toggleBusinessStatus = () => {
  uni.showModal({
    title: '提示',
    content: merchantInfo.value.isOpen ? '确认打烊吗？' : '确认开市吗？',
    success: (res) => {
      if (res.confirm) {
        merchantInfo.value.isOpen = !merchantInfo.value.isOpen
        // TODO: 调用API更新营业状态
        uni.showToast({
          title: merchantInfo.value.isOpen ? '已开市' : '已打烊',
          icon: 'success'
        })
      }
    }
  })
}

/**
 * 跳转到订单列表
 */
const toOrderList = () => {
  uni.switchTab({
    url: '/pages-merchant/order/index'
  })
}

/**
 * 跳转到菜品管理
 */
const toDishManage = () => {
  uni.switchTab({
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

.order-content {
  margin-bottom: 15rpx;
}

.order-dishes {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
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

  &.status-pending {
    background: #FFF7E6;
    color: #FAAD14;
  }

  &.status-cooking {
    background: #E6F7FF;
    color: #1890FF;
  }

  &.status-ready {
    background: #F6FFED;
    color: #52C41A;
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
</style>
