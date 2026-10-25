<template>
  <view class="orders-container">
    <!-- 顶部标签栏 -->
    <view class="tabs-container">
      <scroll-view class="tabs-scroll" scroll-x>
        <view class="tabs-wrapper">
          <view
            class="tab-item"
            v-for="tab in tabs"
            :key="tab.value"
            :class="{ active: currentTab === tab.value }"
            @click="switchTab(tab.value)"
          >
            <text class="tab-text">{{ tab.label }}</text>
            <view class="tab-badge" v-if="tab.count > 0">{{ tab.count > 99 ? '99+' : tab.count }}</view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 订单列表 -->
    <scroll-view
      class="orders-scroll"
      scroll-y
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 空状态 -->
      <view class="empty-container" v-if="orderList.length === 0 && !loading">
        <text class="empty-icon">📦</text>
        <text class="empty-text">暂无订单</text>
        <text class="empty-desc">快去下单品尝美食吧~</text>
        <button class="go-shop-btn" @click="goToShop">去逛逛</button>
      </view>

      <!-- 订单列表 -->
      <view class="order-list" v-else>
        <view
          class="order-item"
          v-for="order in orderList"
          :key="order.orderId"
          @click="viewOrderDetail(order.orderId)"
        >
          <!-- 订单头部 -->
          <view class="order-header">
            <view class="merchant-info">
              <text class="merchant-icon">🏪</text>
              <text class="merchant-name">{{ order.merchantName }}</text>
            </view>
            <view class="order-status" :class="'status-' + order.status">
              {{ getStatusText(order.status) }}
            </view>
          </view>

          <!-- 订单菜品 -->
          <view class="order-dishes">
            <view class="dish-item" v-for="dish in order.dishes" :key="dish.dishId">
              <image class="dish-image" :src="dish.image" mode="aspectFill" />
              <view class="dish-info">
                <text class="dish-name">{{ dish.name }}</text>
                <text class="dish-spec">x{{ dish.quantity }}</text>
              </view>
              <text class="dish-price">¥{{ (dish.price * dish.quantity).toFixed(2) }}</text>
            </view>
          </view>

          <!-- 订单信息 -->
          <view class="order-info">
            <text class="order-time">{{ formatTime(order.createTime) }}</text>
            <text class="order-total">共{{ getTotalCount(order.dishes) }}件 实付</text>
            <text class="order-amount">¥{{ order.totalAmount }}</text>
          </view>

          <!-- 订单操作 -->
          <view class="order-actions" @click.stop>
            <!-- 待支付 -->
            <template v-if="order.status === 0">
              <button class="action-btn secondary-btn" @click="cancelOrder(order.orderId)">取消订单</button>
              <button class="action-btn primary-btn" @click="payOrder(order)">立即支付</button>
            </template>

            <!-- 待接单 -->
            <template v-else-if="order.status === 1">
              <button class="action-btn secondary-btn" @click="cancelOrder(order.orderId)">取消订单</button>
              <button class="actionBtn primary-btn" @click="urgeOrder(order.orderId)">催单</button>
            </template>

            <!-- 制作中 -->
            <template v-else-if="order.status === 2">
              <button class="action-btn secondary-btn" @click="contactMerchant(order.merchantId)">联系商家</button>
            </template>

            <!-- 配送中 -->
            <template v-else-if="order.status === 3">
              <button class="action-btn secondary-btn" @click="viewLogistics(order.orderId)">查看物流</button>
              <button class="action-btn primary-btn" @click="confirmReceipt(order.orderId)">确认收货</button>
            </template>

            <!-- 已完成 -->
            <template v-else-if="order.status === 4">
              <button class="action-btn secondary-btn" @click="deleteOrder(order.orderId)">删除订单</button>
              <button class="action-btn primary-btn" @click="reviewOrder(order)">评价</button>
            </template>

            <!-- 已取消 -->
            <template v-else-if="order.status === 5">
              <button class="action-btn secondary-btn" @click="deleteOrder(order.orderId)">删除订单</button>
              <button class="action-btn primary-btn" @click="reorder(order)">再来一单</button>
            </template>
          </view>
        </view>
      </view>

      <!-- 加载更多 -->
      <view class="load-more" v-if="orderList.length > 0">
        <uni-load-more
          :status="loadMoreStatus"
          :content-text="{ contentdown: '上拉加载更多', contentrefresh: '加载中...', contentnomore: '没有更多了' }"
        ></uni-load-more>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store'
import { orderApi } from '@/api'

const userStore = useUserStore()

// 当前标签
const currentTab = ref('all')

// 标签列表
const tabs = ref([
  { label: '全部', value: 'all', count: 0 },
  { label: '待支付', value: 'pending', count: 0 },
  { label: '待接单', value: 'received', count: 0 },
  { label: '制作中', value: 'processing', count: 0 },
  { label: '配送中', value: 'delivering', count: 0 },
  { label: '已完成', value: 'completed', count: 0 }
])

// 订单列表
const orderList = ref([])

// 分页参数
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const hasMore = ref(true)

// 加载状态
const loading = ref(false)
const refreshing = ref(false)
const loadMoreStatus = ref('more')

// 组件挂载
onMounted(() => {
  loadOrderCounts()
  loadOrders()
})

/**
 * 切换标签
 */
const switchTab = (tabValue) => {
  currentTab.value = tabValue
  page.value = 1
  orderList.value = []
  hasMore.value = true
  loadOrders()
}

/**
 * 加载订单数量
 */
const loadOrderCounts = async () => {
  try {
    const userId = userStore.userInfo?.userId || userStore.userInfo?.id
    const res = await orderApi.getCount({ userId })

    if (res) {
      tabs.value[0].count = res.all || 0
      tabs.value[1].count = res.pending || 0
      tabs.value[2].count = res.received || 0
      tabs.value[3].count = res.processing || 0
      tabs.value[4].count = res.delivering || 0
      tabs.value[5].count = res.completed || 0
    }
  } catch (error) {
    console.error('加载订单数量失败:', error)
  }
}

/**
 * 加载订单列表
 */
const loadOrders = async () => {
  if (loading.value || !hasMore.value) return

  loading.value = true

  try {
    const userId = userStore.userInfo?.userId || userStore.userInfo?.id
    let status = null

    // 根据当前标签转换状态
    const statusMap = {
      'pending': 0,
      'received': 1,
      'processing': 2,
      'delivering': 3,
      'completed': 4
    }

    if (currentTab.value !== 'all') {
      status = statusMap[currentTab.value]
    }

    const res = await orderApi.getByUser(userId, {
      status,
      page: page.value,
      size: pageSize.value
    })

    if (res && res.list) {
      if (page.value === 1) {
        orderList.value = res.list
      } else {
        orderList.value = [...orderList.value, ...res.list]
      }

      total.value = res.total || 0
      hasMore.value = orderList.value.length < total.value

      if (!hasMore.value) {
        loadMoreStatus.value = 'noMore'
      }
    }
  } catch (error) {
    console.error('加载订单列表失败:', error)
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
  page.value = 1
  hasMore.value = true
  loadMoreStatus.value = 'more'

  try {
    await Promise.all([
      loadOrderCounts(),
      loadOrders()
    ])
  } finally {
    refreshing.value = false
  }
}

/**
 * 加载更多
 */
const loadMore = () => {
  if (hasMore.value && !loading.value) {
    page.value++
    loadMoreStatus.value = 'loading'
    loadOrders()
  }
}

/**
 * 获取状态文本
 */
const getStatusText = (status) => {
  const statusMap = {
    0: '待支付',
    1: '待接单',
    2: '制作中',
    3: '配送中',
    4: '已完成',
    5: '已取消'
  }
  return statusMap[status] || '未知'
}

/**
 * 格式化时间
 */
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hour = date.getHours().toString().padStart(2, '0')
  const minute = date.getMinutes().toString().padStart(2, '0')
  return `${month}月${day}日 ${hour}:${minute}`
}

/**
 * 获取菜品总数
 */
const getTotalCount = (dishes) => {
  return dishes.reduce((sum, dish) => sum + dish.quantity, 0)
}

/**
 * 查看订单详情
 */
const viewOrderDetail = (orderId) => {
  uni.navigateTo({
    url: `/pages/order-detail/index?orderId=${orderId}`
  })
}

/**
 * 取消订单
 */
const cancelOrder = (orderId) => {
  uni.showModal({
    title: '提示',
    content: '确定要取消订单吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '取消中...' })
          await orderApi.cancel(orderId, { reason: '用户主动取消' })
          uni.hideLoading()

          uni.showToast({
            title: '订单已取消',
            icon: 'success'
          })

          onRefresh()
        } catch (error) {
          uni.hideLoading()
          uni.showToast({
            title: error.message || '取消失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 支付订单
 */
const payOrder = (order) => {
  uni.navigateTo({
    url: `/pages/payment/index?orderId=${order.orderId}&amount=${order.totalAmount}`
  })
}

/**
 * 催单
 */
const urgeOrder = (orderId) => {
  uni.showToast({
    title: '已通知商家',
    icon: 'none'
  })
}

/**
 * 联系商家
 */
const contactMerchant = (merchantId) => {
  uni.navigateTo({
    url: `/pages/chat/index?merchantId=${merchantId}`
  })
}

/**
 * 查看物流
 */
const viewLogistics = (orderId) => {
  uni.navigateTo({
    url: `/pages/logistics/index?orderId=${orderId}`
  })
}

/**
 * 确认收货
 */
const confirmReceipt = (orderId) => {
  uni.showModal({
    title: '提示',
    content: '确认已收到餐品吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '确认中...' })
          await orderApi.confirm(orderId)
          uni.hideLoading()

          uni.showToast({
            title: '确认成功',
            icon: 'success'
          })

          onRefresh()
        } catch (error) {
          uni.hideLoading()
          uni.showToast({
            title: error.message || '确认失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 评价订单
 */
const reviewOrder = (order) => {
  uni.navigateTo({
    url: `/pages/review/create?orderId=${order.orderId}`
  })
}

/**
 * 删除订单
 */
const deleteOrder = (orderId) => {
  uni.showModal({
    title: '提示',
    content: '确定要删除订单吗？',
    success: async (res) => {
      if (res.confirm) {
        // 删除本地订单
        const index = orderList.value.findIndex(o => o.orderId === orderId)
        if (index > -1) {
          orderList.value.splice(index, 1)
        }

        uni.showToast({
          title: '删除成功',
          icon: 'success'
        })
      }
    }
  })
}

/**
 * 再来一单
 */
const reorder = (order) => {
  // 将订单中的菜品加入购物车
  uni.showToast({
    title: '已加入购物车',
    icon: 'success'
  })

  setTimeout(() => {
    uni.switchTab({
      url: '/pages/cart/index'
    })
  }, 1500)
}

/**
 * 去逛逛
 */
const goToShop = () => {
  uni.switchTab({
    url: '/pages/home/index'
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.orders-container {
  min-height: 100vh;
  background-color: $bg-color-base;
  display: flex;
  flex-direction: column;
}

/* 标签栏 */
.tabs-container {
  position: sticky;
  top: 0;
  z-index: 10;
  background-color: $bg-color-white;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.tabs-scroll {
  white-space: nowrap;
}

.tabs-wrapper {
  @include flex-center;
  padding: $spacing-md $spacing-sm;
}

.tab-item {
  position: relative;
  padding: $spacing-sm $spacing-lg;
  margin: 0 $spacing-xs;
  border-radius: $border-radius-round;
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
  top: -8rpx;
  right: -8rpx;
  min-width: 32rpx;
  height: 32rpx;
  @include flex-center;
  padding: 0 8rpx;
  background-color: $danger-color;
  border-radius: $border-radius-round;
  border: 2rpx solid $bg-color-white;
  font-size: $font-size-xs;
  color: #fff;
  font-weight: $font-weight-bold;
}

/* 订单列表 */
.orders-scroll {
  flex: 1;
  padding: $spacing-md;
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

.go-shop-btn {
  padding: $spacing-md $spacing-xl;
  background-color: $primary-color;
  color: #fff;
  border-radius: $border-radius-round;
  font-size: $font-size-base;
  border: none;
}

/* 订单列表 */
.order-list {
  .order-item {
    background-color: $bg-color-white;
    border-radius: $border-radius-lg;
    padding: $spacing-md;
    margin-bottom: $spacing-md;
    box-shadow: $box-shadow-sm;
  }
}

/* 订单头部 */
.order-header {
  @include flex-between;
  margin-bottom: $spacing-md;
  padding-bottom: $spacing-sm;
  border-bottom: 1rpx solid $border-color-lighter;
}

.merchant-info {
  @include flex-center;
  gap: $spacing-xs;
}

.merchant-icon {
  font-size: $font-size-lg;
}

.merchant-name {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
}

.order-status {
  font-size: $font-size-sm;
  color: $text-color-secondary;

  &.status-0 {
    color: $warning-color;
  }

  &.status-1 {
    color: $primary-color;
  }

  &.status-2 {
    color: $primary-color;
  }

  &.status-3 {
    color: $success-color;
  }

  &.status-4 {
    color: $text-color-secondary;
  }

  &.status-5 {
    color: $text-color-secondary;
  }
}

/* 订单菜品 */
.order-dishes {
  margin-bottom: $spacing-md;
}

.dish-item {
  display: flex;
  align-items: center;
  margin-bottom: $spacing-sm;

  &:last-child {
    margin-bottom: 0;
  }
}

.dish-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: $border-radius-base;
  margin-right: $spacing-md;
}

.dish-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.dish-name {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
}

.dish-spec {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.dish-price {
  font-size: $font-size-base;
  color: $danger-color;
  font-weight: $font-weight-bold;
}

/* 订单信息 */
.order-info {
  @include flex-center;
  gap: $spacing-sm;
  padding-top: $spacing-sm;
  border-top: 1rpx solid $border-color-lighter;
  margin-bottom: $spacing-md;
}

.order-time {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.order-total {
  flex: 1;
  font-size: $font-size-sm;
  color: $text-color-secondary;
  text-align: right;
}

.order-amount {
  font-size: $font-size-lg;
  color: $danger-color;
  font-weight: $font-weight-bold;
}

/* 订单操作 */
.order-actions {
  @include flex-center;
  gap: $spacing-sm;
  justify-content: flex-end;
}

.action-btn {
  padding: $spacing-sm $spacing-lg;
  border-radius: $border-radius-round;
  font-size: $font-size-sm;
  border: 1rpx solid $border-color-base;
  background-color: transparent;

  &::after {
    border: none;
  }
}

.primary-btn {
  background-color: $primary-color;
  color: #fff;
  border-color: $primary-color;
}

.secondary-btn {
  color: $text-color-primary;
}
</style>
