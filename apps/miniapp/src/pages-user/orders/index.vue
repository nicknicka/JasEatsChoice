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
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store'
import { orderApi } from '@/api'
import { createPageDebug } from '@/utils/page-debug'
import {
  HOME,
  USER_DISH_DETAIL,
  USER_MERCHANT_DETAIL,
  USER_ORDER_CONFIRM,
  USER_ORDER_DETAIL,
  USER_REVIEW_SUBMIT
} from '@/constants/routes'

// Store
const userStore = useUserStore()
const pageDebug = createPageDebug('订单列表')

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
const DEFAULT_DISH_IMAGE = '/static/images/default-dish.png'

const normalizeImage = (src) => {
  if (!src || src.includes('via.placeholder.com')) {
    return DEFAULT_DISH_IMAGE
  }
  return src
}

const normalizeOrderStatus = (status) => {
  const normalized = String(status ?? '')
  const statusMap = {
    '0': { value: 'pending', text: '待支付' },
    '1': { value: 'processing', text: '待接单' },
    '2': { value: 'processing', text: '制作中' },
    '3': { value: 'completed', text: '已完成' },
    '4': { value: 'cancelled', text: '已取消' },
    pending: { value: 'pending', text: '待支付' },
    paid: { value: 'processing', text: '已支付' },
    confirmed: { value: 'processing', text: '已确认' },
    preparing: { value: 'processing', text: '准备中' },
    ready: { value: 'processing', text: '待配送' },
    delivering: { value: 'delivering', text: '配送中' },
    completed: { value: 'completed', text: '已完成' },
    cancelled: { value: 'cancelled', text: '已取消' },
    refunded: { value: 'cancelled', text: '已退款' }
  }

  return statusMap[normalized] || {
    value: normalized || 'processing',
    text: normalized || '处理中'
  }
}

const shouldKeepOrder = (statusValue) => {
  if (selectedFilter.value === 'all') {
    return true
  }
  return statusValue === selectedFilter.value
}

const mapOrderItems = (items) => items.map(item => ({
  id: item.orderItemId || item.id,
  dishId: item.dishId || item.dish?.id,
  name: item.dishName || item.dish?.name || '未知菜品',
  spec: item.spec || item.customization || '',
  price: Number(item.price || item.dish?.price || 0).toFixed(2),
  quantity: Number(item.quantity || 0),
  image: normalizeImage(item.dish?.image || item.dish?.coverImage || item.image || '')
}))

/**
 * 切换筛选
 */
const changeFilter = (value) => {
  pageDebug.action('切换订单筛选', {
    from: selectedFilter.value,
    to: value
  })
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
    pageDebug.requestStart('加载订单列表', {
      filter: selectedFilter.value,
      page: page.value,
      showLoading
    })
    if (!userStore.isLogin) {
      pageDebug.anomaly('订单列表加载被登录校验拦截')
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      })
      loading.value = false
      refreshing.value = false
      return
    }

    const userId = userStore.userInfo?.userId || userStore.userInfo?.id

    const res = await orderApi.getByUser(userId)
    const rawOrders = Array.isArray(res?.data) ? res.data : Array.isArray(res) ? res : []

    if (rawOrders.length > 0) {
      const ordersWithDishes = await Promise.all(rawOrders.map(async (order) => {
        const orderId = order.orderId || order.id
        try {
          const dishesRes = await orderApi.getDishes(orderId)
          const dishes = Array.isArray(dishesRes?.data)
            ? dishesRes.data
            : Array.isArray(dishesRes) ? dishesRes : []
          return { order, dishes }
        } catch (error) {
          pageDebug.requestFail('加载订单菜品', {
            orderId,
            message: error?.message || '未知错误'
          })
          return { order, dishes: [] }
        }
      }))

      const filteredOrders = ordersWithDishes.filter(({ order }) => {
        const statusInfo = normalizeOrderStatus(order.status ?? order.orderStatus)
        return shouldKeepOrder(statusInfo.value)
      })
      const start = (page.value - 1) * pageSize.value
      const end = start + pageSize.value
      const pageOrders = filteredOrders.slice(start, end)
      const mappedOrders = pageOrders.map(({ order, dishes }) => {
        const statusInfo = normalizeOrderStatus(order.status ?? order.orderStatus)
        const items = mapOrderItems(dishes)
        return {
          id: order.orderId || order.id,
          orderNo: order.orderNo || order.orderNumber || order.id,
          merchantId: order.merchantId || order.merchant?.id || '',
          merchantName: order.merchantName || order.merchant?.name || dishes[0]?.dish?.merchantName || '商家',
          status: statusInfo.value,
          statusText: order.statusText || statusInfo.text,
          items,
          totalQuantity: items.reduce((sum, item) => sum + item.quantity, 0),
          totalAmount: Number(order.amount?.total || order.totalAmount || 0).toFixed(2),
          createTime: order.createTime || order.createdAt || ''
        }
      })

      if (page.value === 1) {
        orders.value = mappedOrders
      } else {
        orders.value.push(...mappedOrders)
      }

      hasMore.value = end < filteredOrders.length
      pageDebug.requestSuccess('加载订单列表', {
        count: mappedOrders.length,
        total: orders.value.length,
        hasMore: hasMore.value,
        sourceCount: rawOrders.length
      })
    } else {
      if (page.value === 1) {
        orders.value = []
      }
      hasMore.value = false
      pageDebug.anomaly('订单列表返回格式异常或为空', {
        page: page.value
      })
    }

    loading.value = false
    refreshing.value = false
  } catch (error) {
    pageDebug.requestFail('加载订单列表', error)
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
  return normalizeOrderStatus(status).text
}

/**
 * 下拉刷新
 */
const onRefresh = async () => {
  pageDebug.action('下拉刷新订单列表')
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
  pageDebug.action('订单列表加载更多', {
    page: page.value,
    filter: selectedFilter.value
  })
  loadOrders()
}

/**
 * 查看订单详情
 */
const viewOrderDetail = (order) => {
  pageDebug.action('查看订单详情', {
    orderId: order.id
  })
  uni.navigateTo({
    url: `${USER_ORDER_DETAIL}?id=${order.id}`
  })
}

/**
 * 复制订单号
 */
const copyOrderNo = (orderNo) => {
  pageDebug.action('复制订单号', {
    orderNo
  })
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
  pageDebug.action('从订单列表进入商家', {
    merchantId
  })
  uni.navigateTo({
    url: `${USER_MERCHANT_DETAIL}?id=${merchantId}`
  })
}

/**
 * 跳转菜品
 */
const toDish = (dishId) => {
  pageDebug.action('从订单列表进入菜品', {
    dishId
  })
  uni.navigateTo({
    url: `${USER_DISH_DETAIL}?id=${dishId}`
  })
}

/**
 * 取消订单
 */
const cancelOrder = async (order) => {
  pageDebug.action('取消订单', {
    orderId: order.id
  })
  uni.showModal({
    title: '取消订单',
    content: '确定要取消此订单吗？',
    confirmColor: '#FF6B35',
    success: async (res) => {
      if (res.confirm) {
        try {
          // 调用后端API取消订单
          await orderApi.cancel(order.id)
          pageDebug.requestSuccess('取消订单', {
            orderId: order.id
          })

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
          pageDebug.requestFail('取消订单', error)
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
  pageDebug.action('去支付订单', {
    orderId: order.id
  })
  uni.navigateTo({
    url: `${USER_ORDER_CONFIRM}?orderId=${order.id}`
  })
}

/**
 * 联系商家
 */
const contactMerchant = (order) => {
  pageDebug.action('联系商家', {
    orderId: order.id,
    merchantId: order.merchantId
  })
  uni.showToast({
    title: '正在联系商家...',
    icon: 'none'
  })
}

/**
 * 查看配送
 */
const viewLogistics = (order) => {
  pageDebug.action('查看配送信息', {
    orderId: order.id
  })
  uni.showToast({
    title: '查看配送信息...',
    icon: 'none'
  })
}

/**
 * 确认收货
 */
const confirmReceipt = async (order) => {
  pageDebug.action('确认收货', {
    orderId: order.id
  })
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
          pageDebug.requestSuccess('确认收货', {
            orderId: order.id
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
          pageDebug.requestFail('确认收货', error)
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
  pageDebug.action('评价订单', {
    orderId: order.id
  })
  uni.navigateTo({
    url: `${USER_REVIEW_SUBMIT}?orderId=${order.id}&type=order&id=${order.id}`
  })
}

/**
 * 再来一单
 */
const buyAgain = (order) => {
  pageDebug.action('再来一单', {
    orderId: order.id
  })
  uni.showToast({
    title: '已加入购物车',
    icon: 'success'
  })
}

/**
 * 返回首页
 */
const goToHome = () => {
  pageDebug.action('订单列表去首页')
  uni.switchTab({
    url: HOME
  })
}

// 组件挂载
onMounted(() => {
  pageDebug.lifecycle('页面挂载')
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options

  if (options.status) {
    selectedFilter.value = options.status
    pageDebug.state('读取订单列表参数', {
      status: selectedFilter.value
    })
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
