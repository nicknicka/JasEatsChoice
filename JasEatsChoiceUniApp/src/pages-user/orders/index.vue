<template>
  <view class="orders-container">
    <!-- 状态筛选 -->
    <view class="filter-bar">
      <scroll-view class="filter-scroll" scroll-x>
        <view
          class="filter-item"
          :class="{ active: selectedFilter === filter.value }"
          v-for="filter in filters"
          :key="filter.value"
          @click="changeFilter(filter.value)"
        >
          <text class="filter-text">{{ filter.label }}</text>
          <view class="filter-badge" v-if="filter.count > 0">{{ filter.count }}</view>
        </view>
      </scroll-view>
    </view>

    <!-- 订单列表 -->
    <scroll-view
      class="scroll-container"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
    >
      <!-- 空状态 -->
      <view class="empty-state" v-if="orders.length === 0 && !loading">
        <text class="empty-icon">📦</text>
        <text class="empty-text">还没有订单</text>
        <text class="empty-tips">去首页看看心仪的美食吧</text>
        <button class="go-home-btn" @click="goToHome">去逛逛</button>
      </view>

      <!-- 订单列表 -->
      <view class="orders-list" v-else>
        <view
          class="order-item"
          v-for="order in orders"
          :key="order.id"
          @click="viewOrderDetail(order)"
        >
          <!-- 订单头部 -->
          <view class="order-header">
            <view class="order-info">
              <text class="order-no">订单号: {{ order.orderNo }}</text>
              <text class="copy-btn" @click.stop="copyOrderNo(order.orderNo)">复制</text>
            </view>
            <view class="order-status" :class="order.status">
              {{ order.statusText }}
            </view>
          </view>

          <!-- 商家信息 -->
          <view class="merchant-info" @click.stop="toMerchant(order.merchantId)">
            <text class="merchant-name">{{ order.merchantName }}</text>
            <text class="merchant-arrow">→</text>
          </view>

          <!-- 订单商品 -->
          <view class="order-items">
            <scroll-view class="items-scroll" scroll-x>
              <view
                class="item-card"
                v-for="item in order.items"
                :key="item.id"
                @click.stop="toDish(item.dishId)"
              >
                <image class="item-image" :src="item.image" mode="aspectFill" />
                <view class="item-info">
                  <text class="item-name">{{ item.name }}</text>
                  <text class="item-spec" v-if="item.spec">{{ item.spec }}</text>
                  <text class="item-price">¥{{ item.price }} x{{ item.quantity }}</text>
                </view>
              </view>
            </scroll-view>
          </view>

          <!-- 订单总价 -->
          <view class="order-total">
            <text class="total-label">共{{ order.totalQuantity }}件</text>
            <text class="total-price">实付 ¥{{ order.totalAmount }}</text>
          </view>

          <!-- 订单操作 -->
          <view class="order-actions" @click.stop>
            <button
              class="action-btn outline"
              v-if="order.status === 'pending'"
              @click="cancelOrder(order)"
            >
              取消订单
            </button>
            <button
              class="action-btn primary"
              v-if="order.status === 'pending'"
              @click="payOrder(order)"
            >
              立即支付
            </button>
            <button
              class="action-btn outline"
              v-if="['processing', 'delivering'].includes(order.status)"
              @click="contactMerchant(order)"
            >
              联系商家
            </button>
            <button
              class="action-btn outline"
              v-if="order.status === 'delivering'"
              @click="viewLogistics(order)"
            >
              查看配送
            </button>
            <button
              class="action-btn primary"
              v-if="order.status === 'delivering'"
              @click="confirmReceipt(order)"
            >
              确认收货
            </button>
            <button
              class="action-btn outline"
              v-if="order.status === 'completed'"
              @click="reviewOrder(order)"
            >
              评价
            </button>
            <button
              class="action-btn outline"
              v-if="order.status === 'completed'"
              @click="buyAgain(order)"
            >
              再来一单
            </button>
          </view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="load-more" v-if="orders.length > 0">
        <view class="load-text" v-if="loading">加载中...</view>
        <view class="load-text" v-else-if="!hasMore">没有更多了</view>
        <view class="load-text" v-else>上拉加载更多</view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store'
import { orderApi } from '@/api'

// Store
const userStore = useUserStore()

// 筛选选项
const filters = ref([
  { label: '全部', value: 'all', count: 0 },
  { label: '待支付', value: 'pending', count: 2 },
  { label: '处理中', value: 'processing', count: 1 },
  { label: '配送中', value: 'delivering', count: 3 },
  { label: '已完成', value: 'completed', count: 0 },
  { label: '已取消', value: 'cancelled', count: 0 }
])

// 当前筛选
const selectedFilter = ref('all')

// 订单列表
const orders = ref([])

// 加载状态
const loading = ref(false)
const refreshing = ref(false)
const hasMore = ref(true)

// 分页参数
const page = ref(1)
const pageSize = ref(10)

/**
 * 切换筛选
 */
const changeFilter = (value) => {
  selectedFilter.value = value
  page.value = 1
  orders.value = []
  loadOrders()
}

/**
 * 加载订单列表
 */
const loadOrders = async (showLoading = true) => {
  if (showLoading) {
    loading.value = true
  }

  try {
    if (!userStore.isLogin) {
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      })
      loading.value = false
      return
    }

    const userId = userStore.userInfo?.userId || userStore.userInfo?.id

    // 调用后端API获取订单列表
    const res = await orderApi.getList({
      userId,
      status: selectedFilter.value === 'all' ? '' : selectedFilter.value,
      page: page.value,
      size: pageSize.value
    })

    // 数据映射
    if (Array.isArray(res)) {
      const mappedOrders = res.map(order => ({
        id: order.orderId || order.id,
        orderNo: order.orderNo || order.orderNumber,
        merchantId: order.merchantId || order.merchant?.id,
        merchantName: order.merchantName || order.merchant?.name,
        status: order.status || order.orderStatus,
        statusText: mapOrderStatusText(order.status || order.orderStatus),
        items: (order.items || []).map(item => ({
          id: item.orderItemId || item.id,
          dishId: item.dishId || item.dish?.id,
          name: item.dishName || item.dish?.name,
          spec: item.spec || '',
          price: parseFloat(item.price).toFixed(2),
          quantity: item.quantity,
          image: item.dish?.image || item.dish?.coverImage || ''
        })),
        totalQuantity: (order.items || []).reduce((sum, item) => sum + item.quantity, 0),
        totalAmount: parseFloat(order.amount?.total || order.totalAmount || 0).toFixed(2),
        createTime: order.createTime || order.createdAt
      }))

      if (page.value === 1) {
        orders.value = mappedOrders
      } else {
        orders.value.push(...mappedOrders)
      }

      // 判断是否还有更多数据
      hasMore.value = mappedOrders.length >= pageSize.value
    } else {
      if (page.value === 1) {
        orders.value = []
      }
      hasMore.value = false
    }

    loading.value = false
    refreshing.value = false
  } catch (error) {
    console.error('加载订单列表失败:', error)
    loading.value = false
    refreshing.value = false
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    })
  }
}

/**
 * 映射订单状态文本
 */
const mapOrderStatusText = (status) => {
  const statusMap = {
    'pending': '待支付',
    'paid': '已支付',
    'confirmed': '已确认',
    'preparing': '准备中',
    'ready': '待配送',
    'delivering': '配送中',
    'completed': '已完成',
    'cancelled': '已取消',
    'refunded': '已退款'
  }
  return statusMap[status] || status
}

/**
 * 下拉刷新
 */
const onRefresh = async () => {
  refreshing.value = true
  page.value = 1
  await loadOrders(false)
  refreshing.value = false
}

/**
 * 上拉加载更多
 */
const onLoadMore = () => {
  if (loading.value || !hasMore.value) return
  page.value++
  loadOrders()
}

/**
 * 查看订单详情
 */
const viewOrderDetail = (order) => {
  uni.navigateTo({
    url: `/pages/order/detail/index?id=${order.id}`
  })
}

/**
 * 复制订单号
 */
const copyOrderNo = (orderNo) => {
  uni.setClipboardData({
    data: orderNo,
    success: () => {
      uni.showToast({
        title: '订单号已复制',
        icon: 'success'
      })
    }
  })
}

/**
 * 跳转商家
 */
const toMerchant = (merchantId) => {
  uni.navigateTo({
    url: `/pages/merchant/detail/index?id=${merchantId}`
  })
}

/**
 * 跳转菜品
 */
const toDish = (dishId) => {
  uni.navigateTo({
    url: `/pages/dish/detail/index?id=${dishId}`
  })
}

/**
 * 取消订单
 */
const cancelOrder = async (order) => {
  uni.showModal({
    title: '取消订单',
    content: '确定要取消此订单吗？',
    confirmColor: '#FF6B35',
    success: async (res) => {
      if (res.confirm) {
        try {
          // 调用后端API取消订单
          await orderApi.cancel(order.id)

          // 从列表中移除或更新状态
          const index = orders.value.findIndex(item => item.id === order.id)
          if (index > -1) {
            orders.value[index].status = 'cancelled'
            orders.value[index].statusText = '已取消'
          }

          uni.showToast({
            title: '订单已取消',
            icon: 'success'
          })
        } catch (error) {
          console.error('取消订单失败:', error)
          uni.showToast({
            title: '取消失败，请重试',
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
    url: `/pages/order/confirm/index?orderId=${order.id}`
  })
}

/**
 * 联系商家
 */
const contactMerchant = (order) => {
  uni.showToast({
    title: '正在联系商家...',
    icon: 'none'
  })
}

/**
 * 查看配送
 */
const viewLogistics = (order) => {
  uni.showToast({
    title: '查看配送信息...',
    icon: 'none'
  })
}

/**
 * 确认收货
 */
const confirmReceipt = async (order) => {
  uni.showModal({
    title: '确认收货',
    content: '确认已收到餐品吗？',
    confirmColor: '#FF6B35',
    success: async (res) => {
      if (res.confirm) {
        try {
          // 调用后端API确认收货
          await orderApi.confirm(order.id, {
            userId: userStore.userInfo?.userId || userStore.userInfo?.id
          })

          // 更新订单状态
          const index = orders.value.findIndex(item => item.id === order.id)
          if (index > -1) {
            orders.value[index].status = 'completed'
            orders.value[index].statusText = '已完成'
          }

          uni.showToast({
            title: '确认收货成功',
            icon: 'success'
          })
        } catch (error) {
          console.error('确认收货失败:', error)
          uni.showToast({
            title: '操作失败，请重试',
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
    url: `/pages/review/submit/index?orderId=${order.id}&type=order&id=${order.id}`
  })
}

/**
 * 再来一单
 */
const buyAgain = (order) => {
  uni.showToast({
    title: '已加入购物车',
    icon: 'success'
  })
}

/**
 * 返回首页
 */
const goToHome = () => {
  uni.switchTab({
    url: '/pages/index/index'
  })
}

// 组件挂载
onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options

  if (options.status) {
    selectedFilter.value = options.status
  }

  // 加载订单列表
  loadOrders()
})
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

/* 状态筛选栏 */
.filter-bar {
  background-color: $bg-color-white;
  box-shadow: $box-shadow-sm;
  position: sticky;
  top: 0;
  z-index: $z-index-sticky;
}

.filter-scroll {
  @include flex-center;
  white-space: nowrap;
  padding: $spacing-md $spacing-md;
}

.filter-item {
  position: relative;
  padding: $spacing-sm $spacing-md;
  margin-right: $spacing-sm;
  background-color: $bg-color-base;
  border-radius: $border-radius-round;
  @include flex-center;
  gap: $spacing-xs;
  flex-shrink: 0;
  transition: all 0.3s;

  &.active {
    background: linear-gradient(135deg, $primary-color, #FF8F61);
    color: #fff;
  }

  &:active {
    transform: scale(0.95);
  }
}

.filter-text {
  font-size: $font-size-sm;
  color: $text-color-regular;

  .active & {
    color: #fff;
    font-weight: $font-weight-medium;
  }
}

.filter-badge {
  min-width: 32rpx;
  height: 32rpx;
  @include flex-center;
  padding: 0 8rpx;
  background-color: $danger-color;
  border-radius: $border-radius-round;
  font-size: $font-size-xs;
  color: #fff;
  font-weight: $font-weight-bold;

  .active & {
    background-color: #fff;
    color: $primary-color;
  }
}

/* 滚动容器 */
.scroll-container {
  flex: 1;
  height: calc(100vh - 100rpx);
}

/* 空状态 */
.empty-state {
  @include flex-center-column;
  padding: 200rpx $spacing-lg;

  .empty-icon {
    font-size: 120rpx;
    margin-bottom: $spacing-lg;
    opacity: 0.5;
  }

  .empty-text {
    font-size: $font-size-lg;
    color: $text-color-primary;
    margin-bottom: $spacing-sm;
  }

  .empty-tips {
    font-size: $font-size-sm;
    color: $text-color-secondary;
    margin-bottom: $spacing-xl;
  }
}

.go-home-btn {
  width: 240rpx;
  height: 72rpx;
  @include flex-center;
  background: linear-gradient(135deg, $primary-color, #FF8F61);
  color: #fff;
  font-size: $font-size-base;
  border-radius: $border-radius-round;
  border: none;
}

/* 订单列表 */
.orders-list {
  padding: $spacing-md;
}

.order-item {
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: $spacing-md;
  margin-bottom: $spacing-md;
  box-shadow: $box-shadow-sm;
}

/* 订单头部 */
.order-header {
  @include flex-between;
  align-items: flex-start;
  margin-bottom: $spacing-md;
  padding-bottom: $spacing-md;
  border-bottom: 1rpx solid $border-color-lighter;
}

.order-info {
  @include flex-center;
  gap: $spacing-sm;
}

.order-no {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.copy-btn {
  padding: 4rpx 12rpx;
  background-color: $bg-color-base;
  border-radius: $border-radius-sm;
  font-size: $font-size-xs;
  color: $text-color-regular;
}

.order-status {
  padding: 6rpx 16rpx;
  border-radius: $border-radius-round;
  font-size: $font-size-xs;
  font-weight: $font-weight-medium;
  color: #fff;

  &.pending {
    background-color: $warning-color;
  }

  &.processing {
    background-color: $info-color;
  }

  &.delivering {
    background-color: $primary-color;
  }

  &.completed {
    background-color: $success-color;
  }

  &.cancelled {
    background-color: $text-color-secondary;
  }
}

/* 商家信息 */
.merchant-info {
  @include flex-center;
  gap: $spacing-xs;
  margin-bottom: $spacing-md;
  padding: $spacing-sm;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
}

.merchant-name {
  flex: 1;
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-medium;
}

.merchant-arrow {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

/* 订单商品 */
.order-items {
  margin-bottom: $spacing-md;
}

.items-scroll {
  white-space: nowrap;
}

.item-card {
  display: inline-flex;
  align-items: center;
  width: 400rpx;
  padding: $spacing-sm;
  margin-right: $spacing-sm;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  flex-shrink: 0;
}

.item-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: $border-radius-base;
  margin-right: $spacing-sm;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.item-name {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-medium;
  @include text-ellipsis;
}

.item-spec {
  font-size: $font-size-sm;
  color: $text-color-secondary;
  @include text-ellipsis;
}

.item-price {
  font-size: $font-size-sm;
  color: $primary-color;
  font-weight: $font-weight-medium;
}

/* 订单总价 */
.order-total {
  @include flex-between;
  align-items: center;
  margin-bottom: $spacing-md;
  padding-top: $spacing-md;
  border-top: 1rpx solid $border-color-lighter;
}

.total-label {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.total-price {
  font-size: $font-size-lg;
  color: $primary-color;
  font-weight: $font-weight-bold;
}

/* 订单操作 */
.order-actions {
  @include flex-center;
  gap: $spacing-sm;
  justify-content: flex-end;
}

.action-btn {
  min-width: 160rpx;
  height: 64rpx;
  @include flex-center;
  padding: 0 $spacing-md;
  border-radius: $border-radius-round;
  font-size: $font-size-sm;
  border: none;

  &.outline {
    background-color: $bg-color-white;
    color: $text-color-regular;
    border: 1rpx solid $border-color-base;

    &:active {
      background-color: $bg-color-base;
    }
  }

  &.primary {
    background: linear-gradient(135deg, $primary-color, #FF8F61);
    color: #fff;

    &:active {
      opacity: 0.8;
    }
  }
}

/* 加载状态 */
.load-more {
  @include flex-center;
  padding: $spacing-lg 0;
}

.load-text {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}
</style>
