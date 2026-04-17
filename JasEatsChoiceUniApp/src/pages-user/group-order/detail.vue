<template>
  <view class="group-order-detail-container">
    <!-- 状态卡片 -->
    <view class="status-card" :class="'status-' + orderInfo.status">
      <view class="status-icon">{{ getStatusIcon(orderInfo.status) }}</view>
      <view class="status-info">
        <text class="status-text">{{ getStatusText(orderInfo.status) }}</text>
        <text class="status-desc">{{ getStatusDesc(orderInfo.status) }}</text>
      </view>
    </view>

    <!-- 订单信息 -->
    <view class="order-info-card">
      <view class="card-header">
        <text class="title">{{ orderInfo.name }}</text>
        <text class="order-code">订单码：{{ orderInfo.orderCode }}</text>
      </view>

      <view class="info-grid">
        <view class="grid-item">
          <text class="label">商家</text>
          <text class="value">{{ orderInfo.merchantName }}</text>
        </view>
        <view class="grid-item">
          <text class="label">发起人</text>
          <text class="value">{{ orderInfo.creatorName }}</text>
        </view>
        <view class="grid-item">
          <text class="label">参与人数</text>
          <text class="value">{{ displayCurrentCount }}/{{ displayMaxParticipants }}人</text>
        </view>
        <view class="grid-item">
          <text class="label">截止时间</text>
          <text class="value">{{ orderInfo.deadline }}</text>
        </view>
        <view class="grid-item full" v-if="orderInfo.deliveryAddress">
          <text class="label">配送地址</text>
          <text class="value">{{ orderInfo.deliveryAddress }}</text>
        </view>
        <view class="grid-item full" v-if="orderInfo.remark">
          <text class="label">备注</text>
          <text class="value">{{ orderInfo.remark }}</text>
        </view>
      </view>
    </view>

    <!-- 成员订单 - GROUP-007 -->
    <view class="members-card">
      <text class="card-title">成员订单</text>
      <view class="members-list">
        <view
          class="member-item"
          v-for="member in orderInfo.members"
          :key="member.id"
          :class="{ paid: member.paid }"
        >
          <image class="member-avatar" :src="member.avatar" mode="aspectFill"></image>
          <view class="member-info">
            <text class="member-name">{{ member.name }}</text>
            <view class="member-dishes">
              <text class="dish-text" v-for="dish in member.dishes" :key="dish.dishId">
                {{ dish.name }}×{{ dish.quantity }}
              </text>
            </view>
          </view>
          <view class="member-status">
            <text class="amount">¥{{ member.totalAmount }}</text>
            <text class="status" :class="{ paid: member.paid }">
              {{ member.paid ? '已支付' : '待支付' }}
            </text>
          </view>
        </view>

        <!-- 空状态 -->
        <view class="empty-members" v-if="orderInfo.members.length === 0">
          <empty text="暂无成员加入" icon="👥" />
        </view>
      </view>
    </view>

    <!-- 菜品汇总 -->
    <view class="dishes-card">
      <text class="card-title">菜品汇总</text>
      <view class="dishes-list">
        <view class="dish-item" v-for="dish in orderInfo.dishes" :key="dish.id">
          <image class="dish-image" :src="dish.image" mode="aspectFill"></image>
          <view class="dish-info">
            <text class="dish-name">{{ dish.name }}</text>
            <text class="dish-spec">{{ dish.specification }}</text>
          </view>
          <view class="dish-total">
            <text class="quantity">共{{ dish.totalQuantity }}份</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部操作 -->
    <view class="action-bar" v-if="!orderInfo.completed">
      <template v-if="isCreator">
        <button class="action-btn secondary" @tap="inviteMore">邀请好友</button>
        <button
          class="action-btn primary"
          @tap="orderInfo.locked ? goToSettle() : confirmOrder()"
          :disabled="!orderInfo.locked && !canConfirm"
        >
          {{ orderInfo.locked ? '去结算' : (canConfirm ? '确认成团' : '等待成员选择') }}
        </button>
      </template>

      <template v-else-if="hasJoined && !isCurrentUserPaid">
        <button class="action-btn secondary" @tap="selectDishes" :disabled="!canSelectDishes">选择菜品</button>
        <button class="action-btn secondary" @tap="leaveOrder" :disabled="!canLeaveOrder">退出拼单</button>
        <button class="action-btn primary" @tap="goToSettle" :disabled="!orderInfo.canPay">
          去支付 (¥{{ payableAmount }})
        </button>
      </template>

      <!-- 已支付 -->
      <template v-else-if="hasJoined && isCurrentUserPaid">
        <button class="action-btn full" disabled>已支付</button>
      </template>

      <template v-else>
        <button class="action-btn full" disabled>您还未加入此拼单</button>
      </template>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { groupOrderApi } from '@/api/modules/group-order-api.js'

const orderId = ref('')
const userId = ref('')

// 订单信息 - GROUP-007
const orderInfo = ref({
  id: '',
  name: '',
  orderCode: '',
  status: 'pending',
  locked: false,
  canEdit: false,
  canLeave: false,
  canConfirm: false,
  canPay: false,
  currentUserJoined: false,
  currentUserPaid: false,
  merchantName: '',
  creatorId: '',
  creatorName: '',
  maxParticipants: 0,
  currentCount: 0,
  deadline: '',
  deliveryAddress: '',
  remark: '',
  members: [],
  dishes: [],
  completed: false
})

// 是否是创建者
const isCreator = computed(() => {
  return orderInfo.value.creatorId === userId.value
})

// 我的成员信息
const myMember = computed(() => {
  return orderInfo.value.members.find(m => m.userId === userId.value)
})

const hasJoined = computed(() => {
  return Boolean(orderInfo.value.currentUserJoined || myMember.value)
})

const isCurrentUserPaid = computed(() => {
  return Boolean(orderInfo.value.currentUserPaid || myMember.value?.paid)
})

const displayCurrentCount = computed(() => {
  return Number(orderInfo.value.currentCount ?? orderInfo.value.members.length ?? 0)
})

const displayMaxParticipants = computed(() => {
  return Number((orderInfo.value.maxParticipants ?? displayCurrentCount.value) || 0)
})

const payableAmount = computed(() => {
  return myMember.value?.totalAmount || '0.00'
})

const canSelectDishes = computed(() => {
  return hasJoined.value && !isCurrentUserPaid.value && Boolean(orderInfo.value.canEdit)
})

const canLeaveOrder = computed(() => {
  return hasJoined.value && !isCurrentUserPaid.value && Boolean(orderInfo.value.canLeave)
})

// 是否可以确认订单
const canConfirm = computed(() => {
  return Boolean(orderInfo.value.canConfirm)
})

onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options || {}

  orderId.value = options.id || ''
  userId.value = uni.getStorageSync('userId') || ''

  // 加载订单详情
  loadOrderDetail()
})

/**
 * GROUP-007: 加载群订单详情
 */
const loadOrderDetail = async () => {
  try {
    uni.showLoading({ title: '加载中...' })

    const res = await groupOrderApi.getDetail(orderId.value)

    if (res.code === 200 && res.data) {
      const data = res.data
      orderInfo.value = {
        id: data.id,
        name: data.name,
        orderCode: data.orderCode,
        status: data.status || 'pending',
        locked: Boolean(data.locked),
        canEdit: Boolean(data.canEdit),
        canLeave: Boolean(data.canLeave),
        canConfirm: Boolean(data.canConfirm),
        canPay: Boolean(data.canPay),
        currentUserJoined: typeof data.currentUserJoined === 'boolean' ? data.currentUserJoined : Boolean((data.members || []).find(m => m.userId === userId.value)),
        currentUserPaid: typeof data.currentUserPaid === 'boolean' ? data.currentUserPaid : Boolean((data.members || []).find(m => m.userId === userId.value)?.paid),
        merchantName: data.merchantName || '',
        creatorId: data.creatorId || '',
        creatorName: data.creatorName || '',
        groupId: data.groupId || '',
        maxParticipants: Number(data.maxParticipants ?? data.members?.length ?? 0),
        currentCount: Number(data.currentCount ?? data.members?.length ?? 0),
        deadline: data.deadline || '',
        deliveryAddress: data.deliveryAddress || '',
        remark: data.remark || '',
        members: (data.members || []).map(m => ({
          id: m.id,
          userId: m.userId,
          name: m.name,
          avatar: m.avatar || 'https://via.placeholder.com/100',
          paid: m.paid || false,
          totalAmount: m.totalAmount || '0.00',
          dishes: m.dishes || []
        })),
        dishes: (data.dishes || []).map(d => ({
          id: d.dishId,
          name: d.name,
          image: d.image || 'https://via.placeholder.com/100',
          specification: d.specification || '',
          totalQuantity: d.totalQuantity || 0
        })),
        completed: data.status === 'completed' || data.status === 'cancelled'
      }
    }

    uni.hideLoading()
  } catch (error) {
    console.error('加载订单详情失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

/**
 * 获取状态图标
 */
const getStatusIcon = (status) => {
  const icons = {
    pending: '⏰',
    in_progress: '🍽️',
    completed: '✅',
    cancelled: '❌'
  }
  return icons[status] || '📋'
}

/**
 * 获取状态文本
 */
const getStatusText = (status) => {
  if (status === 'pending' && orderInfo.value.locked) {
    return '已确认成团'
  }
  const texts = {
    pending: '待确认成团',
    in_progress: '进行中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return texts[status] || '未知'
}

/**
 * 获取状态描述
 */
const getStatusDesc = (status) => {
  if (status === 'pending' && orderInfo.value.locked) {
    return '菜品已锁定，等待成员完成支付'
  }
  const descs = {
    pending: '等待成员选择菜品后由发起人确认成团',
    in_progress: '成员已支付完成，等待商家接单或处理中',
    completed: '订单已完成',
    cancelled: '订单已取消'
  }
  return descs[status] || ''
}

/**
 * 邀请更多好友
 */
const inviteMore = () => {
  uni.navigateTo({
    url: `/pages-user/group-order/share?id=${orderId.value}&code=${orderInfo.value.orderCode}`
  })
}

/**
 * 选择菜品
 */
const selectDishes = () => {
  if (!canSelectDishes.value) {
    uni.showToast({
      title: orderInfo.value.locked ? '拼单已锁定，不能再改菜' : (hasJoined.value ? '当前暂不可修改菜品' : '您还未加入此拼单'),
      icon: 'none'
    })
    return
  }
  uni.navigateTo({
    url: `/pages-user/group-order/select-dishes?id=${orderId.value}`
  })
}

/**
 * 确认订单
 */
const confirmOrder = async () => {
  if (!canConfirm.value) {
    uni.showToast({
      title: '请等待成员选择菜品',
      icon: 'none'
    })
    return
  }

  try {
    uni.showLoading({ title: '确认中...' })
    const res = await groupOrderApi.confirm(orderId.value, { userId: userId.value })
    uni.hideLoading()

    if (res.code === 200) {
      await loadOrderDetail()
      uni.navigateTo({
        url: `/pages-user/group-order/settle?id=${orderId.value}`
      })
      return
    }

    throw new Error(res.message || '确认成团失败')
  } catch (error) {
    console.error('确认成团失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: error.message || '确认成团失败',
      icon: 'none'
    })
  }
}

/**
 * 去结算
 */
const goToSettle = () => {
  if (!hasJoined.value && !isCreator.value) {
    uni.showToast({
      title: '您还未加入此拼单',
      icon: 'none'
    })
    return
  }
  if (!orderInfo.value.canPay && !isCreator.value) {
    uni.showToast({
      title: isCurrentUserPaid.value ? '您已支付过此拼单' : (orderInfo.value.locked ? '当前暂不可支付' : '请等待发起人确认成团'),
      icon: 'none'
    })
    return
  }
  uni.navigateTo({
    url: `/pages-user/group-order/settle?id=${orderId.value}`
  })
}

/**
 * 退出拼单
 */
const leaveOrder = () => {
  if (!hasJoined.value) {
    uni.showToast({
      title: '您还未加入此拼单',
      icon: 'none'
    })
    return
  }

  if (!canLeaveOrder.value) {
    uni.showToast({
      title: isCurrentUserPaid.value ? '已支付后不能退出拼单' : (orderInfo.value.locked ? '已确认成团，无法退出' : '当前暂不可退出'),
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '退出拼单',
    content: '确定退出当前拼单吗？已选菜品将被移除。',
    success: async (res) => {
      if (!res.confirm) {
        return
      }

      try {
        uni.showLoading({ title: '退出中...' })
        const result = await groupOrderApi.leave(orderId.value, { userId: userId.value })
        uni.hideLoading()

        if (result.code === 200) {
          uni.showToast({
            title: '已退出拼单',
            icon: 'success'
          })

          setTimeout(() => {
            if (orderInfo.value.groupId) {
              uni.redirectTo({
                url: `/pages-user/group-order/index?groupId=${orderInfo.value.groupId}`
              })
            } else {
              uni.navigateBack()
            }
          }, 600)
        } else {
          throw new Error(result.message || '退出失败')
        }
      } catch (error) {
        console.error('退出拼单失败:', error)
        uni.hideLoading()
        uni.showToast({
          title: error.message || '退出失败',
          icon: 'none'
        })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.group-order-detail-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 120rpx;
}

/* 状态卡片 */
.status-card {
  background: #fff;
  padding: 40rpx 30rpx;
  margin-bottom: 20rpx;
  display: flex;
  align-items: center;
  gap: 30rpx;

  &.status-pending {
    background: linear-gradient(135deg, #FFA500 0%, #FF8C00 100%);
    color: #fff;
  }

  &.status-in_progress {
    background: linear-gradient(135deg, #52C41A 0%, #73D13D 100%);
    color: #fff;
  }

  &.status-completed {
    background: linear-gradient(135deg, #1890FF 0%, #40A9FF 100%);
    color: #fff;
  }

  &.status-cancelled {
    background: linear-gradient(135deg, #8C8C8C 0%, #BFBFBF 100%);
    color: #fff;
  }
}

.status-icon {
  font-size: 80rpx;
}

.status-info {
  flex: 1;
}

.status-text {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  margin-bottom: 10rpx;
}

.status-desc {
  display: block;
  font-size: 26rpx;
  opacity: 0.9;
}

/* 订单信息 */
.order-info-card {
  background: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.card-header {
  margin-bottom: 30rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #eee;
}

.card-header .title {
  display: block;
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
}

.card-header .order-code {
  display: block;
  font-size: 24rpx;
  color: #999;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 25rpx;
}

.grid-item {
  &.full {
    grid-column: 1 / -1;
  }
}

.grid-item .label {
  display: block;
  font-size: 24rpx;
  color: #999;
  margin-bottom: 8rpx;
}

.grid-item .value {
  display: block;
  font-size: 26rpx;
  color: #333;
}

/* 成员订单 */
.members-card {
  background: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.card-title {
  display: block;
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.members-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
  border-left: 4rpx solid transparent;

  &.paid {
    border-left-color: #52C41A;
    background: rgba(82, 196, 26, 0.05);
  }
}

.member-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.member-info {
  flex: 1;
}

.member-name {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 10rpx;
}

.member-dishes {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.dish-text {
  font-size: 24rpx;
  color: #666;
}

.member-status {
  text-align: right;
}

.member-status .amount {
  display: block;
  font-size: 32rpx;
  font-weight: bold;
  color: #FF6B35;
  margin-bottom: 5rpx;
}

.member-status .status {
  display: block;
  font-size: 24rpx;
  color: #999;

  &.paid {
    color: #52C41A;
  }
}

/* 菜品汇总 */
.dishes-card {
  background: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.dishes-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.dish-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
}

.dish-image {
  width: 100rpx;
  height: 100rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
}

.dish-info {
  flex: 1;
}

.dish-name {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 5rpx;
}

.dish-spec {
  display: block;
  font-size: 24rpx;
  color: #999;
}

.dish-total .quantity {
  font-size: 26rpx;
  color: #FF6B35;
}

/* 底部操作 */
.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  background: #fff;
  display: flex;
  gap: 20rpx;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.action-btn {
  flex: 1;
  height: 90rpx;
  border-radius: 45rpx;
  font-size: 28rpx;
  border: none;

  &.secondary {
    background: #F5F5F5;
    color: #666;
  }

  &.primary {
    background: #FF6B35;
    color: #fff;

    &[disabled] {
      background: #D9D9D9;
    }
  }

  &.full {
    flex: none;
    width: 100%;
    background: #52C41A;
    color: #fff;
  }
}
</style>
