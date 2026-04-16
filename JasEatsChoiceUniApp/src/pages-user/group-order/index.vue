<template>
  <view class="group-order-list-container">
    <!-- 顶部搜索和加入 -->
    <view class="header-actions">
      <!-- GROUP-009: 订单码输入 -->
      <view class="join-code-section">
        <input
          class="code-input"
          v-model="joinCode"
          placeholder="输入6位订单码"
          maxlength="6"
          type="number"
        />
        <button class="join-btn" @tap="joinByCode">加入</button>
      </view>

      <!-- 状态筛选 -->
      <view class="status-tabs">
        <view
          class="tab-item"
          :class="{ active: activeStatus === item.value }"
          v-for="item in statusTabs"
          :key="item.value"
          @tap="changeStatus(item.value)"
        >
          {{ item.label }}
        </view>
      </view>
    </view>

    <!-- 订单列表 - GROUP-008 -->
    <scroll-view
      class="order-list"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <view
        class="order-card"
        v-for="order in orderList"
        :key="order.id"
        @tap="goToDetail(order.id)"
      >
        <!-- 状态标签 -->
        <view class="status-badge" :class="'status-' + order.status">
          {{ getStatusText(order.status) }}
        </view>

        <!-- 订单信息 -->
        <view class="card-header">
          <text class="order-name">{{ order.name }}</text>
          <text class="order-code">{{ order.orderCode }}</text>
        </view>

        <!-- 商家信息 -->
        <view class="merchant-info">
          <image class="merchant-avatar" :src="order.merchantAvatar" mode="aspectFill"></image>
          <text class="merchant-name">{{ order.merchantName }}</text>
        </view>

        <!-- 进度信息 -->
        <view class="progress-info">
          <view class="progress-item">
            <text class="label">参与人数</text>
            <text class="value">{{ order.currentCount }}/{{ order.maxParticipants }}人</text>
          </view>
          <view class="progress-item">
            <text class="label">截止时间</text>
            <text class="value">{{ order.deadline }}</text>
          </view>
        </view>

        <!-- 成员头像 -->
        <view class="members-avatars" v-if="order.members.length > 0">
          <image
            class="avatar"
            v-for="(member, index) in order.members.slice(0, 5)"
            :key="member.id"
            :src="member.avatar"
            mode="aspectFill"
          />
          <view class="avatar-more" v-if="order.members.length > 5">
            +{{ order.members.length - 5 }}
          </view>
        </view>

        <!-- 操作按钮 -->
        <view class="card-actions" @tap.stop>
          <button
            class="action-btn"
            size="mini"
            @tap="shareOrder(order)"
          >
            分享
          </button>
          <button
            class="action-btn primary"
            size="mini"
            @tap="viewOrder(order)"
          >
            查看
          </button>
          <button
            class="action-btn danger"
            size="mini"
            v-if="canCancel(order)"
            @tap="cancelOrder(order)"
          >
            取消
          </button>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="orderList.length === 0 && !loading">
        <empty text="暂无群订单" icon="📋" buttonText="创建群订单" @button-click="createOrder" />
      </view>

      <!-- 加载状态 -->
      <view class="load-status" v-if="loading">
        <uni-load-more :status="hasMore ? 'loading' : 'noMore'" />
      </view>
    </scroll-view>

    <!-- 创建按钮 -->
    <view class="create-btn" @tap="createOrder">
      <uni-icons type="plus" size="20" color="#fff"></uni-icons>
      <text>创建群订单</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { groupOrderApi } from '@/api/modules/group-order-api.js'

const userId = ref('')
const groupId = ref('')

// 加入码
const joinCode = ref('')

// 状态筛选
const activeStatus = ref('all')
const statusTabs = [
  { label: '全部', value: 'all' },
  { label: '进行中', value: 'pending' },
  { label: '已完成', value: 'completed' }
]

// 订单列表
const orderList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const hasMore = ref(true)

onMounted(() => {
  userId.value = uni.getStorageSync('userId') || ''
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options || {}
  groupId.value = options.groupId || ''

  // GROUP-008: 加载群订单列表
  loadOrderList()
})

/**
 * GROUP-008: 加载群订单列表
 */
const loadOrderList = async (isRefresh = false) => {
  if (loading.value) return

  try {
    loading.value = true

    if (isRefresh) {
      pageNum.value = 1
      hasMore.value = true
    }

    const params = {
      userId: userId.value,
      groupId: groupId.value || undefined,
      page: pageNum.value,
      size: pageSize.value
    }

    if (activeStatus.value !== 'all') {
      params.status = activeStatus.value
    }

    // GROUP-008: 调用API获取群订单列表
    const res = await groupOrderApi.getList(params)

    if (res.code === 200 && res.data) {
      const orders = res.data.list || res.data || []

      // 转换数据格式
      const formattedOrders = orders.map(order => ({
        id: order.id,
        name: order.name,
        orderCode: order.orderCode,
        status: order.status || 'pending',
        merchantName: order.merchantName || '',
        merchantAvatar: order.merchantAvatar || 'https://via.placeholder.com/100',
        creatorId: order.creatorId || '',
        currentCount: order.currentCount || 0,
        maxParticipants: order.maxParticipants || 0,
        deadline: order.deadline || '',
        members: (order.members || []).map(m => ({
          id: m.id,
          avatar: m.avatar || 'https://via.placeholder.com/100'
        }))
      }))

      if (isRefresh) {
        orderList.value = formattedOrders
      } else {
        orderList.value.push(...formattedOrders)
      }

      // 判断是否还有更多
      hasMore.value = orders.length >= pageSize.value
      pageNum.value++
    }
  } catch (error) {
    console.error('加载群订单列表失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
    refreshing.value = false
  }
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
  if (!loading.value && hasMore.value) {
    loadOrderList()
  }
}

/**
 * 切换状态
 */
const changeStatus = (status) => {
  activeStatus.value = status
  loadOrderList(true)
}

/**
 * GROUP-009: 通过订单码加入
 */
const joinByCode = async () => {
  if (!joinCode.value) {
    uni.showToast({
      title: '请输入订单码',
      icon: 'none'
    })
    return
  }

  if (joinCode.value.length !== 6) {
    uni.showToast({
      title: '订单码为6位数字',
      icon: 'none'
    })
    return
  }

  try {
    uni.showLoading({
      title: '加入中...',
      mask: true
    })

    // GROUP-009: 调用API加入群订单
    const res = await groupOrderApi.joinByCode({
      orderCode: joinCode.value,
      userId: userId.value
    })

    uni.hideLoading()

    if (res.code === 200) {
      const groupOrderId = res.data?.groupOrderId || res.data?.orderId || ''

      // 清空输入
      joinCode.value = ''

      uni.showToast({
        title: '订单码有效，请先选菜',
        icon: 'none'
      })

      setTimeout(() => {
        if (groupOrderId) {
          uni.navigateTo({
            url: `/pages-user/group-order/select-dishes?id=${groupOrderId}`
          })
          return
        }

        loadOrderList(true)
      }, 600)
    } else {
      throw new Error(res.message || '加入失败')
    }
  } catch (error) {
    console.error('加入群订单失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: error.message || '加入失败',
      icon: 'none'
    })
  }
}

/**
 * GROUP-010: 取消群订单
 */
const cancelOrder = (order) => {
  uni.showModal({
    title: '提示',
    content: `确定取消群订单"${order.name}"吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({
            title: '取消中...',
            mask: true
          })

          // GROUP-010: 调用API取消群订单
          const apiRes = await groupOrderApi.cancel(order.id, {
            userId: userId.value,
            reason: '用户主动取消'
          })

          uni.hideLoading()

          if (apiRes.code === 200) {
            uni.showToast({
              title: '取消成功',
              icon: 'success'
            })

            // 刷新列表
            setTimeout(() => {
              loadOrderList(true)
            }, 1500)
          } else {
            throw new Error(apiRes.message || '取消失败')
          }
        } catch (error) {
          console.error('取消群订单失败:', error)
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
 * 判断是否可以取消
 */
const canCancel = (order) => {
  // 只有创建者可以取消，且只能取消pending状态的订单
  return order.creatorId === userId.value && order.status === 'pending'
}

/**
 * 获取状态文本
 */
const getStatusText = (status) => {
  const texts = {
    pending: '进行中',
    in_progress: '配送中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return texts[status] || '未知'
}

/**
 * 查看订单
 */
const viewOrder = (order) => {
  uni.navigateTo({
    url: `/pages-user/group-order/detail?id=${order.id}`
  })
}

/**
 * 跳转到详情
 */
const goToDetail = (orderId) => {
  uni.navigateTo({
    url: `/pages-user/group-order/detail?id=${orderId}`
  })
}

/**
 * 分享订单
 */
const shareOrder = (order) => {
  uni.navigateTo({
    url: `/pages-user/group-order/share?id=${order.id}&code=${order.orderCode}`
  })
}

/**
 * 创建群订单
 */
const createOrder = () => {
  uni.navigateTo({
    url: groupId.value ? `/group-order/create?groupId=${groupId.value}` : '/group-order/create'
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.group-order-list-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 120rpx;
}

/* 顶部操作 */
.header-actions {
  background: #fff;
  padding: 20rpx;
  margin-bottom: 20rpx;
}

.join-code-section {
  display: flex;
  gap: 15rpx;
  margin-bottom: 20rpx;
}

.code-input {
  flex: 1;
  height: 70rpx;
  padding: 0 20rpx;
  background: #F5F5F5;
  border-radius: 35rpx;
  font-size: 28rpx;
  color: #333;
  letter-spacing: 5rpx;
  text-align: center;
}

.join-btn {
  padding: 0 30rpx;
  height: 70rpx;
  background: #FF6B35;
  color: #fff;
  border-radius: 35rpx;
  font-size: 26rpx;
  border: none;
}

.status-tabs {
  display: flex;
  gap: 15rpx;
}

.tab-item {
  flex: 1;
  height: 60rpx;
  background: #F5F5F5;
  border-radius: 30rpx;
  @include flex-center;
  font-size: 26rpx;
  color: #666;

  &.active {
    background: #FF6B35;
    color: #fff;
  }
}

/* 订单列表 */
.order-list {
  height: calc(100vh - 260rpx);
  padding: 0 20rpx;
}

.order-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 25rpx;
  margin-bottom: 20rpx;
  position: relative;
}

.status-badge {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
  font-size: 22rpx;

  &.status-pending {
    background: rgba(255, 165, 0, 0.1);
    color: #FFA500;
  }

  &.status-in_progress {
    background: rgba(82, 196, 26, 0.1);
    color: #52C41A;
  }

  &.status-completed {
    background: rgba(24, 144, 255, 0.1);
    color: #1890FF;
  }

  &.status-cancelled {
    background: rgba(140, 140, 140, 0.1);
    color: #8C8C8C;
  }
}

.card-header {
  margin-bottom: 20rpx;
  padding-right: 100rpx;
}

.order-name {
  display: block;
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.order-code {
  display: block;
  font-size: 24rpx;
  color: #999;
}

.merchant-info {
  display: flex;
  align-items: center;
  gap: 15rpx;
  margin-bottom: 20rpx;
  padding: 15rpx;
  background: #F5F5F5;
  border-radius: 8rpx;
}

.merchant-avatar {
  width: 50rpx;
  height: 50rpx;
  border-radius: 50%;
}

.merchant-name {
  font-size: 26rpx;
  color: #333;
}

.progress-info {
  display: flex;
  gap: 30rpx;
  margin-bottom: 20rpx;
}

.progress-item {
  flex: 1;
}

.progress-item .label {
  display: block;
  font-size: 24rpx;
  color: #999;
  margin-bottom: 5rpx;
}

.progress-item .value {
  display: block;
  font-size: 26rpx;
  color: #333;
}

.members-avatars {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.members-avatars .avatar {
  width: 50rpx;
  height: 50rpx;
  border-radius: 50%;
  border: 2rpx solid #fff;
  margin-left: -15rpx;

  &:first-child {
    margin-left: 0;
  }
}

.avatar-more {
  width: 50rpx;
  height: 50rpx;
  border-radius: 50%;
  background: #FF6B35;
  color: #fff;
  font-size: 20rpx;
  @include flex-center;
  margin-left: -15rpx;
  border: 2rpx solid #fff;
}

.card-actions {
  display: flex;
  gap: 15rpx;
  padding-top: 15rpx;
  border-top: 1rpx solid #eee;
}

.action-btn {
  flex: 1;
  height: 60rpx;
  border-radius: 30rpx;
  font-size: 24rpx;
  border: none;
  background: #F5F5F5;
  color: #666;

  &.primary {
    background: #FF6B35;
    color: #fff;
  }

  &.danger {
    background: #F5222D;
    color: #fff;
  }
}

/* 创建按钮 */
.create-btn {
  position: fixed;
  bottom: 30rpx;
  right: 30rpx;
  padding: 20rpx 40rpx;
  background: #FF6B35;
  color: #fff;
  border-radius: 50rpx;
  display: flex;
  align-items: center;
  gap: 10rpx;
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.4);
  font-size: 28rpx;
}
</style>
