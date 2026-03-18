<template>
  <view class="order-manage-container">
    <!-- 状态Tab -->
    <view class="status-tabs">
      <scroll-view scroll-x class="tabs-scroll">
        <view
          class="tab-item"
          :class="{ active: activeStatus === item.value }"
          v-for="item in statusTabs"
          :key="item.value"
          @tap="changeStatus(item.value)"
        >
          <text class="tab-name">{{ item.label }}</text>
          <view class="tab-count" v-if="item.count > 0">{{ item.count }}</view>
        </view>
      </scroll-view>
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
        @tap="toOrderDetail(order.id)"
      >
        <!-- 订单头部 -->
        <view class="card-header">
          <text class="order-no">订单号：{{ order.orderNo }}</text>
          <view class="order-status" :class="'status-' + order.status">
            {{ order.statusText }}
          </view>
        </view>

        <!-- 菜品信息 -->
        <view class="dish-list">
          <view class="dish-item" v-for="dish in order.dishes" :key="dish.id">
            <text class="dish-name">{{ dish.name }}</text>
            <text class="dish-spec" v-if="dish.spec">({{ dish.spec }})</text>
            <text class="dish-quantity">x{{ dish.quantity }}</text>
          </view>
        </view>

        <!-- 备注信息 -->
        <view class="order-remark" v-if="order.remark">
          <uni-icons type="chatbubble" size="16" color="#FF6B35"></uni-icons>
          <text class="remark-text">{{ order.remark }}</text>
        </view>

        <!-- 订单信息 -->
        <view class="order-info">
          <view class="info-row">
            <text class="label">下单时间：</text>
            <text class="value">{{ order.orderTime }}</text>
          </view>
          <view class="info-row">
            <text class="label">期望时间：</text>
            <text class="value">{{ order.expectTime }}</text>
          </view>
          <view class="info-row">
            <text class="label">取餐方式：</text>
            <text class="value">{{ order.takeType }}</text>
          </view>
        </view>

        <!-- 金额信息 -->
        <view class="amount-section">
          <text class="amount-label">订单金额</text>
          <text class="amount-value">¥{{ order.amount }}</text>
        </view>

        <!-- 操作按钮 -->
        <view class="action-buttons">
          <button
            class="action-btn primary"
            v-if="order.status === 'pending'"
            @tap.stop="acceptOrder(order)"
          >
            接单
          </button>
          <button
            class="action-btn primary"
            v-if="order.status === 'cooking'"
            @tap.stop="updateProgress(order)"
          >
            更新进度
          </button>
          <button
            class="action-btn success"
            v-if="order.status === 'ready'"
            @tap.stop="completeOrder(order)"
          >
            完成
          </button>
          <button
            class="action-btn"
            @tap.stop="contactCustomer(order)"
          >
            联系顾客
          </button>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="load-status" v-if="orderList.length > 0">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多了</text>
        <text v-else @tap="loadMore">上拉加载更多</text>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="orderList.length === 0 && !loading">
        <empty text="暂无订单" />
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { toOrderDetail } from '@/utils/router'

// 状态Tab
const statusTabs = ref([
  { label: '全部', value: 'all', count: 0 },
  { label: '待接单', value: 'pending', count: 3 },
  { label: '制作中', value: 'cooking', count: 5 },
  { label: '待取餐', value: 'ready', count: 2 },
  { label: '已完成', value: 'completed', count: 28 },
  { label: '已取消', value: 'cancelled', count: 1 }
])

const activeStatus = ref('all')
const orderList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const noMore = ref(false)
const page = ref(1)
const pageSize = 10

onMounted(() => {
  loadOrders()
})

/**
 * 切换状态
 */
const changeStatus = (status) => {
  activeStatus.value = status
  page.value = 1
  noMore.value = false
  loadOrders()
}

/**
 * 加载订单列表
 */
const loadOrders = async (isRefresh = false) => {
  if (loading.value) return

  loading.value = true
  if (isRefresh) {
    page.value = 1
    noMore.value = false
  }

  try {
    // TODO: 调用API获取订单列表
    // const res = await merchantApi.getOrders({
    //   status: activeStatus.value,
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
    console.error('加载订单失败:', error)
    loading.value = false
    refreshing.value = false
  }
}

/**
 * 生成模拟订单数据
 */
const generateMockOrders = () => {
  const orders = []
  const count = Math.floor(Math.random() * 5) + 3

  for (let i = 0; i < count; i++) {
    const statusList = ['pending', 'cooking', 'ready', 'completed']
    const status = activeStatus.value === 'all'
      ? statusList[Math.floor(Math.random() * statusList.length)]
      : activeStatus.value

    orders.push({
      id: page.value * 10 + i,
      orderNo: `OD20260318${String(page.value * 10 + i).padStart(4, '0')}`,
      status: status,
      statusText: getStatusText(status),
      dishes: [
        { id: 1, name: '宫保鸡丁', spec: '中辣', quantity: 1 },
        { id: 2, name: '鱼香肉丝', spec: '', quantity: 2 }
      ],
      remark: i % 3 === 0 ? '少放辣，多放葱花' : '',
      orderTime: '12:30',
      expectTime: '13:00',
      takeType: i % 2 === 0 ? '到店自取' : '外卖配送',
      amount: '54.00'
    })
  }

  return orders
}

/**
 * 获取状态文本
 */
const getStatusText = (status) => {
  const statusMap = {
    pending: '待接单',
    cooking: '制作中',
    ready: '待取餐',
    completed: '已完成',
    cancelled: '已取消'
  }
  return statusMap[status] || status
}

/**
 * 加载更多
 */
const loadMore = () => {
  if (!loading.value && !noMore.value) {
    page.value++
    loadOrders()
  }
}

/**
 * 下拉刷新
 */
const onRefresh = () => {
  refreshing.value = true
  loadOrders(true)
}

/**
 * 接单
 */
const acceptOrder = (order) => {
  uni.showModal({
    title: '确认接单',
    content: `确认接单 ${order.orderNo} 吗？`,
    success: (res) => {
      if (res.confirm) {
        // TODO: 调用API接单
        uni.showToast({
          title: '接单成功',
          icon: 'success'
        })
        loadOrders(true)
      }
    }
  })
}

/**
 * 更新进度
 */
const updateProgress = (order) => {
  uni.navigateTo({
    url: `/pages-merchant/order/process?id=${order.id}`
  })
}

/**
 * 完成订单
 */
const completeOrder = (order) => {
  uni.showModal({
    title: '确认完成',
    content: `确认订单 ${order.orderNo} 已完成吗？`,
    success: (res) => {
      if (res.confirm) {
        // TODO: 调用API完成订单
        uni.showToast({
          title: '订单已完成',
          icon: 'success'
        })
        loadOrders(true)
      }
    }
  })
}

/**
 * 联系顾客
 */
const contactCustomer = (order) => {
  uni.showActionSheet({
    itemList: ['拨打电话', '发送消息'],
    success: (res) => {
      if (res.tapIndex === 0) {
        uni.makePhoneCall({
          phoneNumber: '138****8888'
        })
      } else {
        // 跳转到聊天页面
        uni.navigateTo({
          url: `/pages-common/chat/chat-room?userId=${order.customerId}`
        })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.order-manage-container {
  min-height: 100vh;
  background: #F5F5F5;
  display: flex;
  flex-direction: column;
}

/* 状态Tab */
.status-tabs {
  background: #fff;
  border-bottom: 1rpx solid #eee;
}

.tabs-scroll {
  white-space: nowrap;
  padding: 20rpx;
}

.tab-item {
  display: inline-block;
  position: relative;
  padding: 12rpx 24rpx;
  margin-right: 20rpx;
  background: #F5F5F5;
  border-radius: 30rpx;
  font-size: 28rpx;
  color: #666;

  &.active {
    background: #FF6B35;
    color: #fff;
    font-weight: bold;
  }

  &:last-child {
    margin-right: 0;
  }
}

.tab-name {
  position: relative;
  z-index: 1;
}

.tab-count {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  min-width: 32rpx;
  height: 32rpx;
  padding: 0 8rpx;
  background: #F5222D;
  color: #fff;
  font-size: 20rpx;
  border-radius: 16rpx;
  @include flex-center;
}

/* 订单列表 */
.order-list {
  flex: 1;
  padding: 20rpx;
}

.order-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

/* 订单头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #eee;
}

.order-no {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
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

  &.status-completed {
    background: #F5F5F5;
    color: #999;
  }
}

/* 菜品列表 */
.dish-list {
  margin-bottom: 20rpx;
}

.dish-item {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 15rpx 0;
  font-size: 28rpx;
  color: #333;
}

.dish-name {
  font-weight: bold;
}

.dish-spec {
  color: #999;
  font-size: 26rpx;
}

.dish-quantity {
  color: #FF6B35;
  font-weight: bold;
}

/* 备注信息 */
.order-remark {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 15rpx;
  background: #FFF7E6;
  border-radius: 8rpx;
  margin-bottom: 20rpx;
}

.remark-text {
  flex: 1;
  font-size: 26rpx;
  color: #FF6B35;
}

/* 订单信息 */
.order-info {
  margin-bottom: 20rpx;
}

.info-row {
  display: flex;
  padding: 10rpx 0;
  font-size: 26rpx;
}

.label {
  color: #999;
  width: 150rpx;
}

.value {
  color: #333;
  flex: 1;
}

/* 金额 */
.amount-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-top: 1rpx solid #eee;
  margin-bottom: 20rpx;
}

.amount-label {
  font-size: 28rpx;
  color: #666;
}

.amount-value {
  font-size: 36rpx;
  font-weight: bold;
  color: #FF6B35;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 20rpx;
}

.action-btn {
  flex: 1;
  height: 70rpx;
  border-radius: 35rpx;
  font-size: 28rpx;
  background: #F5F5F5;
  color: #666;
  border: none;

  &.primary {
    background: #FF6B35;
    color: #fff;
  }

  &.success {
    background: #52C41A;
    color: #fff;
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
  padding-top: 200rpx;
}
</style>
