<template>
  <view class="today-orders-container">
    <!-- 统计卡片（5状态系统） -->
    <view class="stats-card">
      <view class="stat-item" @tap="filterByStatus('all')">
        <text class="stat-value">{{ statistics.total }}</text>
        <text class="stat-label">全部订单</text>
      </view>
      <view class="stat-item" @tap="filterByStatus(1)">
        <text class="stat-value warning">{{ statistics.pending }}</text>
        <text class="stat-label">待接单</text>
      </view>
      <view class="stat-item" @tap="filterByStatus(2)">
        <text class="stat-value primary">{{ statistics.cooking }}</text>
        <text class="stat-label">制作中</text>
      </view>
      <view class="stat-item" @tap="filterByStatus(3)">
        <text class="stat-value success">{{ statistics.completed }}</text>
        <text class="stat-label">已完成</text>
      </view>
    </view>

    <!-- 快捷筛选 -->
    <view class="filter-tabs">
      <scroll-view scroll-x class="tabs-scroll">
        <view
          class="tab-item"
          :class="{ active: activeFilter === item.value }"
          v-for="item in filterTabs"
          :key="item.value"
          @tap="changeFilter(item.value)"
        >
          {{ item.label }}
          <view class="tab-badge" v-if="item.count > 0">{{ item.count }}</view>
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
        <!-- 紧急标识 -->
        <view class="urgent-badge" v-if="order.isUrgent">
          <text class="urgent-text">急</text>
        </view>

        <!-- 订单头部 -->
        <view class="card-header">
          <view class="order-info">
            <text class="table-no" v-if="order.tableNo">{{ order.tableNo }}</text>
            <text class="wait-time">{{ order.waitTime }}</text>
          </view>
          <view class="order-status" :class="'status-' + order.status">
            {{ order.statusText }}
          </view>
        </view>

        <!-- 菜品信息 -->
        <view class="dish-list">
          <view class="dish-item" v-for="dish in order.dishes" :key="dish.id">
            <text class="dish-name">{{ dish.name }}</text>
            <view class="dish-specs" v-if="dish.specs">
              <text class="spec" v-for="spec in dish.specs" :key="spec">{{ spec }}</text>
            </view>
            <text class="dish-quantity">x{{ dish.quantity }}</text>
          </view>
        </view>

        <!-- 特殊备注 -->
        <view class="remark-badge" v-if="order.remark">
          <uni-icons type="chatbubble" size="14" color="#FF6B35"></uni-icons>
          <text class="remark-text">{{ order.remark }}</text>
        </view>

        <!-- 订单信息 -->
        <view class="order-meta">
          <text class="order-no">{{ order.orderNo }}</text>
          <text class="order-time">{{ order.orderTime }}</text>
        </view>

        <!-- 快捷操作（5状态系统） -->
        <view class="quick-actions" @tap.stop>
          <button
            class="action-btn primary"
            v-if="order.status === 1"
            @tap="quickAccept(order)"
          >
            接单
          </button>
          <button
            class="action-btn success"
            v-if="order.status === 2"
            @tap="quickComplete(order)"
          >
            完成
          </button>
          <button
            class="action-btn"
            @tap="contactCustomer(order)"
          >
            联系
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
        <empty text="暂无今日订单" icon="📋" buttonText="刷新列表" @button-click="onRefresh" />
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { merchantApi } from '@/api'
import { formatTime } from '@/utils/helper'
import { useMerchantStore } from '@/store/modules/merchant'

const merchantStore = useMerchantStore()

// 统计数据（5状态系统）
const statistics = ref({
  total: 0,
  pending: 0,    // 待接单
  cooking: 0,    // 制作中
  completed: 0   // 已完成
})

// 筛选Tab（5状态系统）
const filterTabs = ref([
  { label: '全部', value: 'all', count: 0 },
  { label: '待接单', value: 1, count: 0 },
  { label: '制作中', value: 2, count: 0 },
  { label: '已完成', value: 3, count: 0 },
  { label: '已取消', value: 4, count: 0 }
])

const activeFilter = ref('all')
const orderList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const noMore = ref(false)
const page = ref(1)
const pageSize = 20

let refreshTimer = null

onMounted(() => {
  loadOrders()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})

/**
 * 开始自动刷新
 */
const startAutoRefresh = () => {
  refreshTimer = setInterval(() => {
    loadOrders(true)
  }, 30000) // 30秒刷新一次
}

/**
 * 停止自动刷新
 */
const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

/**
 * 切换筛选
 */
const changeFilter = (filter) => {
  activeFilter.value = filter
  page.value = 1
  noMore.value = false
  loadOrders()
}

/**
 * 按状态筛选
 */
const filterByStatus = (status) => {
  changeFilter(status)
}

/**
 * 加载订单列表
 */
const loadOrders = async (isRefresh = false) => {
  if (loading.value) return

  loading.value = true
  if (isRefresh) {
    refreshing.value = true
  }

  try {
    const merchantId = merchantStore.merchantInfo?.merchantId || merchantStore.merchantInfo?.id

    if (!merchantId) {
      uni.showToast({
        title: '未找到商家信息',
        icon: 'none'
      })
      loading.value = false
      refreshing.value = false
      return
    }

    // 调用API获取今日订单
    const res = await merchantApi.getTodayOrders(merchantId)

    if (res && res.success && res.data) {
      const orders = Array.isArray(res.data) ? res.data : []

      // 为每个订单获取菜品列表
      const orderListWithData = await Promise.all(
        orders.map(async (order) => {
          try {
            const dishesRes = await merchantApi.getOrderDishes(order.id)
            const dishes = dishesRes && dishesRes.success ? dishesRes.data || [] : []

            return {
              id: order.id,
              orderNo: `OD${String(order.id).padStart(6, '0')}`,
              status: order.status,
              statusText: getStatusText(order.status),
              dishes: dishes.map(dish => ({
                id: dish.dishId || dish.id,
                name: dish.dishName || dish.name,
                specs: dish.spec ? [dish.spec] : [],
                quantity: dish.quantity || 1
              })),
              remark: order.remark || '',
              tableNo: order.tableNo || 'A01',
              waitTime: calculateWaitTime(order.createTime),
              orderTime: formatTime(order.createTime),
              isUrgent: calculateWaitTime(order.createTime).includes('25分钟'),
              customerId: order.userId
            }
          } catch (error) {
            console.error('获取订单菜品失败:', order.id, error)
            return {
              id: order.id,
              orderNo: `OD${String(order.id).padStart(6, '0')}`,
              status: order.status,
              statusText: getStatusText(order.status),
              dishes: [],
              remark: order.remark || '',
              tableNo: 'A01',
              waitTime: '等待中',
              orderTime: formatTime(order.createTime),
              isUrgent: false,
              customerId: order.userId
            }
          }
        })
      )

      // 根据activeFilter筛选订单（5状态系统）
      let filteredOrders = orderListWithData
      if (activeFilter.value !== 'all') {
        filteredOrders = orderListWithData.filter(order => order.status === activeFilter.value)
      }

      orderList.value = filteredOrders

      // 更新统计数据
      updateStatistics(orderListWithData)
    } else {
      throw new Error(res?.message || '获取订单失败')
    }

    loading.value = false
    refreshing.value = false
  } catch (error) {
    console.error('加载订单失败:', error)
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    })
    loading.value = false
    refreshing.value = false
  }
}

/**
 * 更新统计数据（5状态系统）
 */
const updateStatistics = (orders) => {
  const stats = {
    total: orders.length,
    pending: 0,    // 待接单
    cooking: 0,    // 制作中
    completed: 0   // 已完成
  }

  orders.forEach(order => {
    if (order.status === 1) stats.pending++
    if (order.status === 2) stats.cooking++
    if (order.status === 3) stats.completed++
  })

  statistics.value = stats

  // 更新筛选Tab的计数
  filterTabs.value[0].count = stats.total
  filterTabs.value[1].count = stats.pending
  filterTabs.value[2].count = stats.cooking
  filterTabs.value[3].count = stats.completed
  filterTabs.value[4].count = orders.filter(o => o.status === 4).length // 已取消
}

/**
 * 计算等待时间
 */
const calculateWaitTime = (orderTime) => {
  if (!orderTime) return '等待中'

  const now = new Date()
  const orderDate = new Date(orderTime)
  const diffMinutes = Math.floor((now - orderDate) / 1000 / 60)

  if (diffMinutes < 1) return '等待中'
  if (diffMinutes < 60) return `等待${diffMinutes}分钟`

  const hours = Math.floor(diffMinutes / 60)
  const minutes = diffMinutes % 60
  return minutes > 0 ? `等待${hours}小时${minutes}分钟` : `等待${hours}小时`
}

/**
 * 生成模拟订单数据
 */
const generateMockOrders = () => {
  const orders = []
  const count = Math.floor(Math.random() * 5) + 5

  for (let i = 0; i < count; i++) {
    const statusList = ['pending', 'cooking', 'ready']
    const status = activeFilter.value === 'all'
      ? statusList[Math.floor(Math.random() * statusList.length)]
      : activeFilter.value

    const waitTimes = ['等待5分钟', '等待12分钟', '等待25分钟', '等待3分钟']
    const tables = ['A01', 'A02', 'A05', 'B03', '外卖']

    orders.push({
      id: page.value * 20 + i,
      orderNo: `OD${String(page.value * 20 + i).padStart(6, '0')}`,
      status: status,
      statusText: getStatusText(status),
      dishes: [
        { id: 1, name: '宫保鸡丁', specs: ['中辣'], quantity: 1 },
        { id: 2, name: '鱼香肉丝', specs: [], quantity: 2 }
      ],
      remark: i % 3 === 0 ? '少放辣，多放葱花' : '',
      tableNo: tables[Math.floor(Math.random() * tables.length)],
      waitTime: waitTimes[Math.floor(Math.random() * waitTimes.length)],
      orderTime: '12:30',
      isUrgent: i % 5 === 0
    })
  }

  return orders
}

/**
 * 获取状态文本
 */
const getStatusText = (status) => {
  // 5状态系统
  const statusMap = {
    0: '待支付',
    1: '待接单',
    2: '制作中',
    3: '已完成',
    4: '已取消'
  }
  return statusMap[status] || '未知状态'
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
 * 快捷接单
 */
const quickAccept = async (order) => {
  uni.showModal({
    title: '确认接单',
    content: `确认接单 ${order.orderNo} 吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          // 调用API接单（状态改为2-备菜中）
          await merchantApi.acceptOrder(order.id)

          uni.showToast({
            title: '接单成功',
            icon: 'success'
          })

          // 刷新订单列表
          loadOrders()
        } catch (error) {
          console.error('接单失败:', error)
          uni.showToast({
            title: '接单失败，请重试',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 快捷完成
 */
const quickComplete = async (order) => {
  uni.showModal({
    title: '确认完成',
    content: `确认订单 ${order.orderNo} 已完成吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          // 调用API完成订单（状态改为5-已送达）
          await merchantApi.completeOrder(order.id)

          uni.showToast({
            title: '已完成',
            icon: 'success'
          })

          // 刷新订单列表
          loadOrders()
        } catch (error) {
          console.error('完成订单失败:', error)
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
        uni.showToast({
          title: '跳转聊天',
          icon: 'none'
        })
      }
    }
  })
}

/**
 * 跳转到订单详情
 */
const toOrderDetail = (id) => {
  uni.navigateTo({
    url: `/pages-merchant/order/detail?id=${id}`
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.today-orders-container {
  min-height: 100vh;
  background: #F5F5F5;
  display: flex;
  flex-direction: column;
}

/* 统计卡片 */
.stats-card {
  background: linear-gradient(135deg, #FF6B35, #FF8F6B);
  padding: 30rpx;
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
  font-size: 48rpx;
  font-weight: bold;
  color: #fff;

  &.warning {
    color: #FFC53D;
  }

  &.primary {
    color: #91D5FF;
  }

  &.success {
    color: #B7EB8F;
  }
}

.stat-label {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.9);
}

/* 筛选Tab */
.filter-tabs {
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
  font-size: 28rpx;
  color: #666;
  border-radius: 30rpx;
  background: #F5F5F5;

  &.active {
    background: #FF6B35;
    color: #fff;
    font-weight: bold;
  }

  &:last-child {
    margin-right: 0;
  }
}

.tab-badge {
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
  position: relative;
}

/* 紧急标识 */
.urgent-badge {
  position: absolute;
  top: 0;
  right: 0;
  background: linear-gradient(135deg, #F5222D, #FF4D4F);
  padding: 8rpx 20rpx;
  border-radius: 0 16rpx 0 20rpx;
}

.urgent-text {
  font-size: 24rpx;
  color: #fff;
  font-weight: bold;
}

/* 订单头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding-bottom: 15rpx;
  border-bottom: 1rpx solid #eee;
}

.order-info {
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.table-no {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.wait-time {
  font-size: 24rpx;
  color: #999;
  padding: 4rpx 12rpx;
  background: #FFF7E6;
  border-radius: 20rpx;
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
  margin-bottom: 15rpx;
}

.dish-item {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 12rpx 0;
  font-size: 28rpx;
  color: #333;
}

.dish-name {
  font-weight: bold;
  flex-shrink: 0;
}

.dish-specs {
  display: flex;
  gap: 8rpx;
}

.spec {
  font-size: 24rpx;
  color: #999;
  padding: 2rpx 8rpx;
  background: #F5F5F5;
  border-radius: 4rpx;
}

.dish-quantity {
  color: #FF6B35;
  font-weight: bold;
  margin-left: auto;
}

/* 备注标识 */
.remark-badge {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 12rpx 15rpx;
  background: #FFF7E6;
  border-radius: 8rpx;
  margin-bottom: 15rpx;
}

.remark-text {
  flex: 1;
  font-size: 26rpx;
  color: #FF6B35;
  @include text-ellipsis;
}

/* 订单信息 */
.order-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 24rpx;
  color: #999;
  margin-bottom: 15rpx;
}

.order-no {
  font-weight: bold;
}

/* 快捷操作 */
.quick-actions {
  display: flex;
  gap: 15rpx;
  padding-top: 15rpx;
  border-top: 1rpx solid #eee;
}

.action-btn {
  flex: 1;
  height: 70rpx;
  border-radius: 35rpx;
  font-size: 26rpx;
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
