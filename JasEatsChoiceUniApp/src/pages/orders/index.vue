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
    // TODO: 调用后端API
    // const res = await orderApi.list({
    //   status: selectedFilter.value === 'all' ? '' : selectedFilter.value,
    //   page: page.value,
    //   pageSize: pageSize.value
    // })

    // 模拟数据
    const mockOrders = [
      {
        id: 1,
        orderNo: 'JSCY202603170001',
        merchantId: 1,
        merchantName: '老王家常菜',
        status: 'pending',
        statusText: '待支付',
        items: [
          {
            id: 1,
            dishId: 1,
            name: '宫保鸡丁',
            spec: '微辣',
            price: '28.00',
            quantity: 1,
            image: 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=宫保鸡丁'
          },
          {
            id: 2,
            dishId: 2,
            name: '米饭',
            spec: '大份',
            price: '2.00',
            quantity: 2,
            image: 'https://via.placeholder.com/200x200/FFCCBC/FFFFFF?text=米饭'
          }
        ],
        totalQuantity: 3,
        totalAmount: '32.00',
        createTime: '2026-03-17 12:34:56'
      },
      {
        id: 2,
        orderNo: 'JSCY202603160002',
        merchantId: 2,
        merchantName: '川味馆',
        status: 'processing',
        statusText: '准备中',
        items: [
          {
            id: 3,
            dishId: 3,
            name: '鱼香肉丝',
            spec: '中辣',
            price: '26.00',
            quantity: 1,
            image: 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=鱼香肉丝'
          }
        ],
        totalQuantity: 1,
        totalAmount: '26.00',
        createTime: '2026-03-16 18:23:45'
      },
      {
        id: 3,
        orderNo: 'JSCY202603150003',
        merchantId: 1,
        merchantName: '老王家常菜',
        status: 'delivering',
        statusText: '配送中',
        items: [
          {
            id: 4,
            dishId: 4,
            name: '回锅肉',
            spec: '',
            price: '32.00',
            quantity: 1,
            image: 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=回锅肉'
          }
        ],
        totalQuantity: 1,
        totalAmount: '32.00',
        createTime: '2026-03-15 19:56:12'
      },
      {
        id: 4,
        orderNo: 'JSCY202603140004',
        merchantId: 3,
        merchantName: '健康轻食',
        status: 'completed',
        statusText: '已完成',
        items: [
          {
            id: 5,
            dishId: 5,
            name: '蔬菜沙拉',
            spec: '低卡',
            price: '22.00',
            quantity: 1,
            image: 'https://via.placeholder.com/200x200/C8E6C9/FFFFFF?text=沙拉'
          }
        ],
        totalQuantity: 1,
        totalAmount: '22.00',
        createTime: '2026-03-14 12:15:30'
      }
    ]

    if (page.value === 1) {
      orders.value = mockOrders
    } else {
      orders.value.push(...mockOrders)
    }

    // 判断是否还有更多数据
    hasMore.value = orders.value.length >= pageSize.value
  } catch (error) {
    console.error('加载订单列表失败:', error)
    uni.showToast({
      title: '加载失败，请重试',
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
const cancelOrder = (order) => {
  uni.showModal({
    title: '取消订单',
    content: '确定要取消此订单吗？',
    confirmColor: '#FF6B35',
    success: async (res) => {
      if (res.confirm) {
        try {
          // TODO: 调用后端API
          // await orderApi.cancel(order.id)

          // 从列表中移除
          const index = orders.value.findIndex(item => item.id === order.id)
          if (index > -1) {
            orders.value.splice(index, 1)
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
const confirmReceipt = (order) => {
  uni.showModal({
    title: '确认收货',
    content: '确认已收到餐品吗？',
    confirmColor: '#FF6B35',
    success: async (res) => {
      if (res.confirm) {
        try {
          // TODO: 调用后端API
          // await orderApi.confirmReceipt(order.id)

          // 更新订单状态
          order.status = 'completed'
          order.statusText = '已完成'

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
