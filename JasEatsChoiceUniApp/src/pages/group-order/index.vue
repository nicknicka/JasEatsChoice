<template>
  <view class="group-order-container">
    <!-- 顶部说明卡片 -->
    <view class="intro-card">
      <view class="intro-icon">🍽️</view>
      <view class="intro-content">
        <text class="intro-title">什么是群订单？</text>
        <text class="intro-desc">邀请朋友一起点餐，共享美食，共享快乐！每个人可以自由选择菜品，最后统一结算。</text>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <button class="action-btn primary" @tap="createGroupOrder">
        <uni-icons type="plus" size="20" color="#fff"></uni-icons>
        <text>创建群订单</text>
      </button>
      <button class="action-btn" @tap="joinGroupOrder">
        <uni-icons type="search" size="20" color="#FF6B35"></uni-icons>
        <text>加入群订单</text>
      </button>
    </view>

    <!-- 我的群订单 -->
    <view class="my-orders-section">
      <view class="section-header">
        <text class="section-title">我的群订单</text>
      </view>

      <!-- 筛选标签 -->
      <view class="filter-tabs">
        <view
          class="tab-item"
          :class="{ active: activeTab === item.value }"
          v-for="item in filterTabs"
          :key="item.value"
          @tap="changeTab(item.value)"
        >
          {{ item.label }}
          <view class="tab-badge" v-if="item.count > 0">
            {{ item.count }}
          </view>
        </view>
      </view>

      <!-- 订单列表 -->
      <scroll-view
        class="order-list"
        scroll-y
        @scrolltolower="loadMore"
        :refresher-enabled="true"
        :refresher-triggered="refreshing"
        @refresherrefresh="onRefresh"
      >
        <view
          class="order-card"
          v-for="order in orderList"
          :key="order.id"
          @tap="viewOrderDetail(order)"
        >
          <!-- 订单头部 -->
          <view class="order-header">
            <view class="creator-info">
              <image class="creator-avatar" :src="order.creator.avatar" mode="aspectFill"></image>
              <text class="creator-name">{{ order.creator.name }}</text>
              <text class="creator-label" v-if="order.isMe">我发起的</text>
            </view>
            <view class="order-status" :class="'status-' + order.status">
              {{ order.statusText }}
            </view>
          </view>

          <!-- 商家信息 -->
          <view class="merchant-info">
            <image class="merchant-avatar" :src="order.merchant.avatar" mode="aspectFill"></image>
            <view class="merchant-detail">
              <text class="merchant-name">{{ order.merchant.name }}</text>
              <text class="merchant-category">{{ order.merchant.category }}</text>
            </view>
            <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
          </view>

          <!-- 参与人员 -->
          <view class="participants-section">
            <view class="avatars-stack">
              <image
                class="participant-avatar"
                v-for="(user, index) in order.participants.slice(0, 5)"
                :key="index"
                :src="user.avatar"
                mode="aspectFill"
              ></image>
              <view class="more-count" v-if="order.participants.length > 5">
                +{{ order.participants.length - 5 }}
              </view>
            </view>
            <text class="participants-count">{{ order.participants.length }}人参与</text>
          </view>

          <!-- 订单信息 -->
          <view class="order-info">
            <view class="info-item">
              <text class="info-label">已选菜品</text>
              <text class="info-value">{{ order.dishCount }}道</text>
            </view>
            <view class="info-item">
              <text class="info-label">订单总额</text>
              <text class="info-value amount">¥{{ order.totalAmount }}</text>
            </view>
          </view>

          <!-- 时间信息 -->
          <view class="time-info">
            <text class="time-text">{{ order.timeText }}</text>
            <view class="countdown" v-if="order.status === 'pending' && order.countdown">
              <uni-icons type="notification" size="14" color="#FF6B35"></uni-icons>
              <text class="countdown-text">{{ order.countdown }}</text>
            </view>
          </view>

          <!-- 操作按钮 -->
          <view class="order-actions" v-if="order.status === 'pending'">
            <button
              class="action-btn"
              v-if="order.isMe"
              @tap.stop="cancelOrder(order)"
            >
              取消订单
            </button>
            <button
              class="action-btn primary"
              v-if="order.isMe && order.canSettle"
              @tap.stop="settleOrder(order)"
            >
              结算订单
            </button>
            <button
              class="action-btn primary"
              v-if="!order.isMe && order.hasSelected"
              @tap.stop="viewMySelection(order)"
            >
              查看我的选择
            </button>
            <button
              class="action-btn"
              v-if="!order.isMe && !order.hasSelected"
              @tap.stop="selectDishes(order)"
            >
              选择菜品
            </button>
          </view>
        </view>

        <!-- 加载状态 -->
        <view class="load-status" v-if="orderList.length > 0">
          <text v-if="loading">加载中...</text>
          <text v-else-if="noMore">没有更多了</text>
        </view>

        <!-- 空状态 -->
        <view class="empty-state" v-if="orderList.length === 0 && !loading">
          <empty text="暂无群订单" icon="👥" buttonText="创建群订单" @button-click="createGroupOrder" />
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 筛选标签
const filterTabs = ref([
  { label: '全部', value: 'all', count: 0 },
  { label: '进行中', value: 'active', count: 2 },
  { label: '待结算', value: 'pending', count: 1 },
  { label: '已完成', value: 'completed', count: 0 }
])

const activeTab = ref('all')

// 订单列表
const orderList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const noMore = ref(false)
const page = ref(1)
const pageSize = 20

onMounted(() => {
  loadOrderList()
})

/**
 * 切换标签
 */
const changeTab = (tab) => {
  activeTab.value = tab
  loadOrderList(true)
}

/**
 * 加载订单列表
 */
const loadOrderList = async (isRefresh = false) => {
  if (loading.value) return

  loading.value = true
  if (isRefresh) {
    page.value = 1
    noMore.value = false
  }

  try {
    // TODO: 调用API获取群订单列表
    // const res = await userApi.getGroupOrders({
    //   status: activeTab.value,
    //   page: page.value,
    //   size: pageSize
    // })

    // 模拟数据
    setTimeout(() => {
      const mockData = generateMockOrders()
      if (isRefresh) {
        orderList.value = mockData
      } else {
        orderList.value = [...orderList.value, ...mockData]
      }

      if (mockData.length < pageSize) {
        noMore.value = true
      }

      loading.value = false
      refreshing.value = false
    }, 500)
  } catch (error) {
    console.error('加载群订单失败:', error)
    loading.value = false
    refreshing.value = false
  }
}

/**
 * 生成模拟订单数据
 */
const generateMockOrders = () => {
  const orders = []
  const count = Math.floor(Math.random() * 3) + 2

  for (let i = 0; i < count; i++) {
    const isMe = Math.random() > 0.5
    const participantCount = Math.floor(Math.random() * 5) + 2

    orders.push({
      id: page.value * 20 + i,
      creator: {
        id: 1,
        name: '张同学',
        avatar: 'https://via.placeholder.com/60/FF6B35/FFFFFF?text=张'
      },
      isMe: isMe,
      merchant: {
        id: 1,
        name: '老王家常菜',
        avatar: 'https://via.placeholder.com/60/FF6B35/FFFFFF?text=店',
        category: '川菜'
      },
      participants: generateParticipants(participantCount),
      dishCount: Math.floor(Math.random() * 10) + 3,
      totalAmount: (Math.random() * 200 + 50).toFixed(2),
      status: 'pending',
      statusText: '进行中',
      timeText: '剩余29分钟',
      countdown: '29:30',
      canSettle: Math.random() > 0.5,
      hasSelected: Math.random() > 0.3
    })
  }

  return orders
}

/**
 * 生成参与人员
 */
const generateParticipants = (count) => {
  const participants = []
  const names = ['张', '李', '王', '赵', '刘', '陈']

  for (let i = 0; i < count; i++) {
    participants.push({
      id: i + 1,
      name: names[i % names.length] + '同学',
      avatar: `https://via.placeholder.com/60/FF6B35/FFFFFF?text=${names[i % names.length]}`
    })
  }

  return participants
}

/**
 * 创建群订单
 */
const createGroupOrder = () => {
  uni.navigateTo({
    url: '/pages/group-order/create'
  })
}

/**
 * 加入群订单
 */
const joinGroupOrder = () => {
  uni.showToast({
    title: '输入订单码加入',
    icon: 'none'
  })
  // TODO: 实现订单码输入功能
}

/**
 * 查看订单详情
 */
const viewOrderDetail = (order) => {
  uni.navigateTo({
    url: `/pages/group-order/detail?id=${order.id}`
  })
}

/**
 * 取消订单
 */
const cancelOrder = (order) => {
  uni.showModal({
    title: '取消订单',
    content: '确定取消这个群订单吗？所有参与者都会收到通知。',
    confirmColor: '#F5222D',
    success: (res) => {
      if (res.confirm) {
        // TODO: 调用API取消订单
        uni.showToast({
          title: '已取消',
          icon: 'success'
        })
        loadOrderList(true)
      }
    }
  })
}

/**
 * 结算订单
 */
const settleOrder = (order) => {
  uni.navigateTo({
    url: `/pages/group-order/settle?id=${order.id}`
  })
}

/**
 * 查看我的选择
 */
const viewMySelection = (order) => {
  uni.navigateTo({
    url: `/pages/group-order/my-selection?id=${order.id}`
  })
}

/**
 * 选择菜品
 */
const selectDishes = (order) => {
  uni.navigateTo({
    url: `/pages/group-order/select-dishes?id=${order.id}`
  })
}

/**
 * 下拉刷新
 */
const onRefresh = () => {
  refreshing.value = true
  loadOrderList(true)
}

/**
 * 加载更多
 */
const loadMore = () => {
  if (!loading.value && !noMore.value) {
    page.value++
    loadOrderList()
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.group-order-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 40rpx;
}

/* 说明卡片 */
.intro-card {
  background: linear-gradient(135deg, #FF6B35, #FF8F6B);
  padding: 30rpx;
  margin: 20rpx;
  border-radius: 16rpx;
  display: flex;
  gap: 20rpx;
}

.intro-icon {
  font-size: 60rpx;
  flex-shrink: 0;
}

.intro-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.intro-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #fff;
}

.intro-desc {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.6;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 20rpx;
  padding: 0 20rpx 20rpx;
}

.action-btn {
  flex: 1;
  height: 90rpx;
  border-radius: 45rpx;
  font-size: 28rpx;
  border: none;
  @include flex-center;
  gap: 10rpx;

  &.primary {
    background: #FF6B35;
    color: #fff;
  }

  &:not(.primary) {
    background: #fff;
    color: #FF6B35;
  }
}

/* 我的订单 */
.my-orders-section {
  background: #fff;
  margin: 0 20rpx 20rpx;
  border-radius: 16rpx;
  overflow: hidden;
}

.section-header {
  padding: 30rpx 30rpx 20rpx;
  border-bottom: 1rpx solid #eee;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

/* 筛选标签 */
.filter-tabs {
  display: flex;
  padding: 20rpx 30rpx;
  gap: 20rpx;
  border-bottom: 1rpx solid #eee;
}

.tab-item {
  position: relative;
  padding: 10rpx 20rpx;
  font-size: 26rpx;
  color: #666;

  &.active {
    color: #FF6B35;
    font-weight: bold;
  }
}

.tab-badge {
  position: absolute;
  top: -5rpx;
  right: -5rpx;
  min-width: 28rpx;
  height: 28rpx;
  padding: 0 6rpx;
  background: #F5222D;
  color: #fff;
  font-size: 18rpx;
  border-radius: 14rpx;
  @include flex-center;
}

/* 订单列表 */
.order-list {
  max-height: 1000rpx;
  padding: 20rpx 30rpx;
}

.order-card {
  background: #F5F5F5;
  border-radius: 16rpx;
  padding: 25rpx;
  margin-bottom: 20rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

/* 订单头部 */
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.creator-info {
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.creator-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
}

.creator-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.creator-label {
  padding: 4rpx 12rpx;
  background: #FF6B35;
  color: #fff;
  font-size: 22rpx;
  border-radius: 12rpx;
}

.order-status {
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 24rpx;

  &.status-pending {
    background: #E6F7FF;
    color: #1890FF;
  }

  &.status-completed {
    background: #F6FFED;
    color: #52C41A;
  }

  &.status-cancelled {
    background: #FFF1F0;
    color: #F5222D;
  }
}

/* 商家信息 */
.merchant-info {
  display: flex;
  align-items: center;
  gap: 15rpx;
  padding: 20rpx;
  background: #fff;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
}

.merchant-avatar {
  width: 70rpx;
  height: 70rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.merchant-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.merchant-name {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
}

.merchant-category {
  font-size: 24rpx;
  color: #999;
}

/* 参与人员 */
.participants-section {
  display: flex;
  align-items: center;
  gap: 15rpx;
  margin-bottom: 20rpx;
}

.avatars-stack {
  display: flex;
  margin-left: 10rpx;
}

.participant-avatar {
  width: 50rpx;
  height: 50rpx;
  border-radius: 50%;
  border: 3rpx solid #fff;
  margin-left: -15rpx;

  &:first-child {
    margin-left: 0;
  }
}

.more-count {
  width: 50rpx;
  height: 50rpx;
  border-radius: 50%;
  background: #FF6B35;
  color: #fff;
  font-size: 20rpx;
  @include flex-center;
  margin-left: -15rpx;
  border: 3rpx solid #fff;
}

.participants-count {
  font-size: 24rpx;
  color: #666;
}

/* 订单信息 */
.order-info {
  display: flex;
  justify-content: space-around;
  padding: 20rpx;
  background: #fff;
  border-radius: 12rpx;
  margin-bottom: 15rpx;
}

.info-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.info-label {
  font-size: 24rpx;
  color: #999;
}

.info-value {
  font-size: 30rpx;
  color: #333;
  font-weight: bold;

  &.amount {
    color: #FF6B35;
  }
}

/* 时间信息 */
.time-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15rpx;
}

.time-text {
  font-size: 24rpx;
  color: #999;
}

.countdown {
  display: flex;
  align-items: center;
  gap: 5rpx;
}

.countdown-text {
  font-size: 24rpx;
  color: #FF6B35;
  font-weight: bold;
}

/* 操作按钮 */
.order-actions {
  display: flex;
  gap: 15rpx;
  padding-top: 15rpx;
  border-top: 1rpx solid #eee;
}

.order-actions .action-btn {
  flex: 1;
  height: 70rpx;
  border-radius: 35rpx;
  font-size: 26rpx;
  border: none;
  @include flex-center;

  &.primary {
    background: #FF6B35;
    color: #fff;
  }

  &:not(.primary) {
    background: #fff;
    color: #666;
  }
}

/* 加载状态 */
.load-status {
  text-align: center;
  padding: 30rpx 0;
  color: #999;
  font-size: 26rpx;
}

/* 空状态 */
.empty-state {
  padding-top: 150rpx;
}
</style>
