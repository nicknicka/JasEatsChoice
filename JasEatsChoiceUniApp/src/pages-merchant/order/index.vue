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
            v-if="order.status === 1"
            @tap.stop="acceptOrder(order)"
          >
            接单
          </button>
          <button
            class="action-btn success"
            v-if="order.status === 2"
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
import { merchantApi } from '@/api'
import { useMerchantStore } from '@/store/modules/merchant'
import { normalizeOrderStatusCode } from '@/config/order-status'

const merchantStore = useMerchantStore()

// 订单状态映射（5状态系统）
// 0-待支付、1-待接单、2-制作中、3-已完成、4-已取消
const orderStatusMap = {
  0: { text: '待支付', value: 'unpaid' },
  1: { text: '待接单', value: 'pending' },
  2: { text: '制作中', value: 'preparing' },
  3: { text: '已完成', value: 'completed' },
  4: { text: '已取消', value: 'cancelled' }
}

// 状态Tab
const statusTabs = ref([
  { label: '全部', value: 'all', count: 0 },
  { label: '待接单', value: 1, count: 0 },
  { label: '制作中', value: 2, count: 0 },
  { label: '已完成', value: 3, count: 0 },
  { label: '已取消', value: 4, count: 0 }
])

const activeStatus = ref('all')
const orderList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const noMore = ref(false)

onMounted(() => {
  loadOrders()
})

/**
 * 切换状态
 */
const changeStatus = (status) => {
  activeStatus.value = status
  loadOrders()
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

    // 调用API获取商家订单列表
    const res = await merchantApi.getOrders(merchantId, { today: false })

    if (res && res.success && res.data) {
      const orders = Array.isArray(res.data) ? res.data : []

      // 为每个订单获取菜品列表
      const orderListWithData = await Promise.all(
        orders.map(async (order) => {
          const normalizedStatus = normalizeOrderStatusCode(order.status)
          try {
            const dishesRes = await merchantApi.getOrderDishes(order.id)
            const dishes = dishesRes && dishesRes.success ? dishesRes.data || [] : []

            return {
              id: order.id,
              orderNo: `OD${String(order.id).padStart(6, '0')}`,
              status: normalizedStatus,
              statusText: getStatusText(normalizedStatus),
              dishes: dishes.map(dish => ({
                id: dish.dishId || dish.id,
                name: dish.dishName || dish.name,
                spec: dish.spec || '',
                quantity: dish.quantity || 1
              })),
              remark: order.remark || '',
              orderTime: formatFullTime(order.createTime),
              expectTime: formatFullTime(order.updateTime) || '未设置',
              takeType: '到店自取',
              amount: formatAmount(order.totalAmount || 0),
              customerId: order.userId
            }
          } catch (error) {
            console.error('获取订单菜品失败:', order.id, error)
            return {
              id: order.id,
              orderNo: `OD${String(order.id).padStart(6, '0')}`,
              status: normalizedStatus,
              statusText: getStatusText(normalizedStatus),
              dishes: [],
              remark: order.remark || '',
              orderTime: formatFullTime(order.createTime),
              expectTime: '未设置',
              takeType: '到店自取',
              amount: formatAmount(order.totalAmount || 0),
              customerId: order.userId
            }
          }
        })
      )

      // 根据activeStatus筛选订单（5状态系统）
      let filteredOrders = orderListWithData
      if (activeStatus.value !== 'all') {
        filteredOrders = orderListWithData.filter(order => order.status === activeStatus.value)
      }

      orderList.value = filteredOrders

      // 更新状态计数
      updateStatusCounts(orderListWithData)
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
 * 更新状态计数（5状态系统）
 */
const updateStatusCounts = (orders) => {
  const counts = {
    1: 0, // 待接单
    2: 0, // 制作中
    3: 0, // 已完成
    4: 0  // 已取消
  }

  orders.forEach(order => {
    if (counts[order.status] !== undefined) {
      counts[order.status]++
    }
  })

  statusTabs.value[1].count = counts[1] // 待接单
  statusTabs.value[2].count = counts[2] // 制作中
  statusTabs.value[3].count = counts[3] // 已完成
  statusTabs.value[4].count = counts[4] // 已取消
  statusTabs.value[0].count = orders.length // 全部
}

/**
 * 格式化完整时间
 */
const formatFullTime = (time) => {
  if (!time) return ''
  if (typeof time === 'string' && time.includes('-')) return time
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

/**
 * 格式化金额
 */
const formatAmount = (amount) => {
  if (typeof amount === 'number') {
    return amount.toFixed(2)
  }
  return String(amount)
}

/**
 * 获取状态文本（5状态系统）
 */
const getStatusText = (status) => {
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
 * 下拉刷新
 */
const onRefresh = () => {
  refreshing.value = true
  loadOrders()
}

/**
 * 接单
 */
const acceptOrder = async (order) => {
  uni.showModal({
    title: '确认接单',
    content: `确认接单 ${order.orderNo} 吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          // 调用API接单（状态改为2-制作中）
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
 * 完成订单
 */
const completeOrder = async (order) => {
  uni.showModal({
    title: '确认完成',
    content: `确认订单 ${order.orderNo} 已完成吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          // 调用API完成订单（状态改为3-已完成）
          await merchantApi.completeOrder(order.id)

          uni.showToast({
            title: '订单已完成',
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

  &.status-1 {
    background: #FFF7E6;
    color: #FAAD14;
  }

  &.status-2 {
    background: #E6F7FF;
    color: #1890FF;
  }

  &.status-3 {
    background: #F6FFED;
    color: #52C41A;
  }

  &.status-4 {
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
