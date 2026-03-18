<template>
  <view class="group-order-detail-container">
    <!-- 顶部状态栏 -->
    <view class="status-header" :class="'status-' + orderDetail.status">
      <view class="status-info">
        <text class="status-text">{{ orderDetail.statusText }}</text>
        <text class="status-desc">{{ orderDetail.statusDesc }}</text>
      </view>
      <view class="countdown" v-if="orderDetail.countdown">
        <uni-icons type="notification" size="18" color="#fff"></uni-icons>
        <text class="countdown-text">{{ orderDetail.countdown }}</text>
      </view>
    </view>

    <!-- 商家信息 -->
    <view class="merchant-card" @tap="viewMerchant">
      <image class="merchant-avatar" :src="orderDetail.merchant.avatar" mode="aspectFill"></image>
      <view class="merchant-info">
        <text class="merchant-name">{{ orderDetail.merchant.name }}</text>
        <text class="merchant-category">{{ orderDetail.merchant.category }}</text>
      </view>
      <uni-icons type="arrowright" size="18" color="#999"></uni-icons>
    </view>

    <!-- 订单信息 -->
    <view class="order-info-card">
      <view class="info-header">
        <text class="info-title">订单信息</text>
        <view class="order-code">
          <text class="code-label">订单码</text>
          <text class="code-value">{{ orderDetail.orderCode }}</text>
          <button class="copy-btn" @tap="copyOrderCode">复制</button>
        </view>
      </view>

      <view class="info-list">
        <view class="info-item">
          <text class="item-label">发起人</text>
          <view class="creator-info">
            <image class="creator-avatar" :src="orderDetail.creator.avatar" mode="aspectFill"></image>
            <text class="creator-name">{{ orderDetail.creator.name }}</text>
          </view>
        </view>
        <view class="info-item">
          <text class="item-label">用餐时间</text>
          <text class="item-value">{{ orderDetail.diningTime }}</text>
        </view>
        <view class="info-item">
          <text class="item-label">人数限制</text>
          <text class="item-value">{{ orderDetail.participantCount }}/{{ orderDetail.maxPeople }}人</text>
        </view>
        <view class="info-item" v-if="orderDetail.remark">
          <text class="item-label">留言备注</text>
          <text class="item-value">{{ orderDetail.remark }}</text>
        </view>
      </view>
    </view>

    <!-- 参与人员 -->
    <view class="participants-card">
      <view class="card-header">
        <text class="card-title">参与人员 ({{ orderDetail.participants.length }})</text>
      </view>
      <view class="participants-list">
        <view
          class="participant-item"
          v-for="user in orderDetail.participants"
          :key="user.id"
        >
          <image class="participant-avatar" :src="user.avatar" mode="aspectFill"></image>
          <view class="participant-info">
            <text class="participant-name">{{ user.name }}</text>
            <view class="participant-dishes">
              <text class="dish-count">{{ user.dishCount }}道菜</text>
              <text class="dish-amount">¥{{ user.amount }}</text>
            </view>
          </view>
          <view class="participant-status" :class="'status-' + user.status">
            {{ user.statusText }}
          </view>
        </view>
      </view>
    </view>

    <!-- 菜品汇总 -->
    <view class="dishes-card">
      <view class="card-header">
        <text class="card-title">菜品汇总</text>
        <view class="total-section">
          <text class="total-label">订单总额</text>
          <text class="total-amount">¥{{ orderDetail.totalAmount }}</text>
        </view>
      </view>
      <view class="dishes-list">
        <view
          class="dish-item"
          v-for="dish in orderDetail.dishes"
          :key="dish.id"
        >
          <image class="dish-image" :src="dish.image" mode="aspectFill"></image>
          <view class="dish-info">
            <text class="dish-name">{{ dish.name }}</text>
            <view class="dish-meta">
              <text class="dish-count">{{ dish.totalCount }}份</text>
              <text class="dish-users">{{ dish.userCount }}人点</text>
            </view>
          </view>
          <text class="dish-price">¥{{ dish.totalPrice }}</text>
        </view>
      </view>
    </view>

    <!-- 我的菜品 -->
    <view class="my-dishes-card" v-if="orderDetail.myDishes.length > 0">
      <view class="card-header">
        <text class="card-title">我的菜品</text>
        <button class="action-btn" @tap="addDishes">
          <uni-icons type="plus" size="16" color="#FF6B35"></uni-icons>
          <text>添加</text>
        </button>
      </view>
      <view class="my-dishes-list">
        <view
          class="my-dish-item"
          v-for="dish in orderDetail.myDishes"
          :key="dish.id"
        >
          <image class="dish-thumb" :src="dish.image" mode="aspectFill"></image>
          <view class="dish-detail">
            <text class="dish-name">{{ dish.name }}</text>
            <view class="dish-spec" v-if="dish.spec">
              <text class="spec-text">{{ dish.spec }}</text>
            </view>
          </view>
          <view class="dish-amount">
            <text class="amount-value">¥{{ dish.price }}</text>
            <text class="amount-count">x{{ dish.count }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons" v-if="orderDetail.status === 'pending'">
      <button
        class="action-btn"
        v-if="orderDetail.isCreator"
        @tap="shareOrder"
      >
        <uni-icons type="redo" size="18" color="#666"></uni-icons>
        <text>分享邀请</text>
      </button>
      <button
        class="action-btn"
        v-if="orderDetail.isCreator && orderDetail.canSettle"
        @tap="settleOrder"
      >
        <uni-icons type="checkbox" size="18" color="#FF6B35"></uni-icons>
        <text>结算订单</text>
      </button>
      <button
        class="action-btn primary"
        v-if="!orderDetail.hasSelected"
        @tap="selectDishes"
      >
        <uni-icons type="plus" size="18" color="#fff"></uni-icons>
        <text>选择菜品</text>
      </button>
      <button
        class="action-btn primary"
        v-if="orderDetail.hasSelected"
        @tap="viewMyDishes"
      >
        <uni-icons type="list" size="18" color="#fff"></uni-icons>
        <text>我的菜品</text>
      </button>
    </view>

    <!-- 已完成状态 -->
    <view class="completed-bar" v-if="orderDetail.status === 'completed'">
      <view class="completed-info">
        <uni-icons type="checkbox-filled" size="20" color="#52C41A"></uni-icons>
        <text class="completed-text">订单已完成</text>
      </view>
      <button class="reorder-btn" @tap="reorder">
        <uni-icons type="refresh" size="16" color="#FF6B35"></uni-icons>
        <text>再来一单</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 订单详情
const orderDetail = ref({
  id: 1,
  status: 'pending',
  statusText: '进行中',
  statusDesc: '等待成员选择菜品',
  countdown: '29:30',
  orderCode: 'ABC123',
  merchant: {
    id: 1,
    name: '老王家常菜',
    avatar: 'https://via.placeholder.com/80/FF6B35/FFFFFF?text=店',
    category: '川菜'
  },
  creator: {
    id: 1,
    name: '张同学',
    avatar: 'https://via.placeholder.com/60/FF6B35/FFFFFF?text=张'
  },
  isCreator: true,
  diningTime: '2026-03-20 12:00',
  participantCount: 3,
  maxPeople: 10,
  remark: '大家尽量选不同的菜，丰富一些',
  totalAmount: '256.00',
  canSettle: true,
  hasSelected: true,
  participants: [
    {
      id: 1,
      name: '张同学',
      avatar: 'https://via.placeholder.com/60/FF6B35/FFFFFF?text=张',
      dishCount: 2,
      amount: '58.00',
      status: 'completed',
      statusText: '已选择'
    },
    {
      id: 2,
      name: '李同学',
      avatar: 'https://via.placeholder.com/60/52C41A/FFFFFF?text=李',
      dishCount: 3,
      amount: '86.00',
      status: 'completed',
      statusText: '已选择'
    },
    {
      id: 3,
      name: '王同学',
      avatar: 'https://via.placeholder.com/60/1890FF/FFFFFF?text=王',
      dishCount: 1,
      amount: '32.00',
      status: 'pending',
      statusText: '选择中'
    }
  ],
  dishes: [
    {
      id: 1,
      name: '宫保鸡丁',
      image: 'https://via.placeholder.com/100/FF6B35/FFFFFF?text=1',
      totalCount: 2,
      userCount: 2,
      totalPrice: '56.00'
    },
    {
      id: 2,
      name: '鱼香肉丝',
      image: 'https://via.placeholder.com/100/52C41A/FFFFFF?text=2',
      totalCount: 3,
      userCount: 3,
      totalPrice: '78.00'
    },
    {
      id: 3,
      name: '麻婆豆腐',
      image: 'https://via.placeholder.com/100/1890FF/FFFFFF?text=3',
      totalCount: 1,
      userCount: 1,
      totalPrice: '18.00'
    }
  ],
  myDishes: [
    {
      id: 1,
      name: '宫保鸡丁',
      image: 'https://via.placeholder.com/100/FF6B35/FFFFFF?text=1',
      spec: '微辣',
      price: '28.00',
      count: 1
    },
    {
      id: 2,
      name: '鱼香肉丝',
      image: 'https://via.placeholder.com/100/52C41A/FFFFFF?text=2',
      spec: '',
      price: '26.00',
      count: 1
    }
  ]
})

onMounted(() => {
  loadOrderDetail()
})

/**
 * 加载订单详情
 */
const loadOrderDetail = () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options
  const orderId = options.id

  // TODO: 调用API获取订单详情
  // const res = await userApi.getGroupOrderDetail({ id: orderId })
  // orderDetail.value = res.data
}

/**
 * 查看商家
 */
const viewMerchant = () => {
  uni.navigateTo({
    url: `/pages/merchant/detail?id=${orderDetail.value.merchant.id}`
  })
}

/**
 * 复制订单码
 */
const copyOrderCode = () => {
  uni.setClipboardData({
    data: orderDetail.value.orderCode,
    success: () => {
      uni.showToast({
        title: '已复制',
        icon: 'success'
      })
    }
  })
}

/**
 * 分享订单
 */
const shareOrder = () => {
  uni.showActionSheet({
    itemList: ['生成二维码', '复制链接', '分享订单码'],
    success: (res) => {
      if (res.tapIndex === 2) {
        copyOrderCode()
      } else {
        uni.showToast({
          title: '功能开发中',
          icon: 'none'
        })
      }
    }
  })
}

/**
 * 结算订单
 */
const settleOrder = () => {
  uni.navigateTo({
    url: `/pages/group-order/settle?id=${orderDetail.value.id}`
  })
}

/**
 * 添加菜品
 */
const addDishes = () => {
  uni.navigateTo({
    url: `/pages/group-order/select-dishes?id=${orderDetail.value.id}&add=true`
  })
}

/**
 * 选择菜品
 */
const selectDishes = () => {
  uni.navigateTo({
    url: `/pages/group-order/select-dishes?id=${orderDetail.value.id}`
  })
}

/**
 * 查看我的菜品
 */
const viewMyDishes = () => {
  uni.navigateTo({
    url: `/pages/group-order/my-selection?id=${orderDetail.value.id}`
  })
}

/**
 * 再来一单
 */
const reorder = () => {
  uni.showModal({
    title: '再来一单',
    content: '将以相同的配置创建新的群订单',
    success: (res) => {
      if (res.confirm) {
        uni.navigateTo({
          url: '/pages/group-order/create'
        })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.group-order-detail-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 140rpx;
}

/* 状态头部 */
.status-header {
  background: linear-gradient(135deg, #1890FF, #40A9FF);
  padding: 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;

  &.status-pending {
    background: linear-gradient(135deg, #1890FF, #40A9FF);
  }

  &.status-completed {
    background: linear-gradient(135deg, #52C41A, #73D13D);
  }
}

.status-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.status-text {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
}

.status-desc {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.countdown {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 20rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 30rpx;
}

.countdown-text {
  font-size: 28rpx;
  color: #fff;
  font-weight: bold;
}

/* 商家卡片 */
.merchant-card {
  background: #fff;
  padding: 25rpx;
  margin: 20rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.merchant-avatar {
  width: 90rpx;
  height: 90rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.merchant-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.merchant-name {
  font-size: 30rpx;
  color: #333;
  font-weight: bold;
}

.merchant-category {
  font-size: 24rpx;
  color: #999;
}

/* 订单信息卡片 */
.order-info-card {
  background: #fff;
  padding: 30rpx;
  margin: 0 20rpx 20rpx;
  border-radius: 16rpx;
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding-bottom: 15rpx;
  border-bottom: 1rpx solid #eee;
}

.info-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.order-code {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.code-label {
  font-size: 24rpx;
  color: #999;
}

.code-value {
  font-size: 28rpx;
  color: #FF6B35;
  font-weight: bold;
  letter-spacing: 2rpx;
}

.copy-btn {
  padding: 6rpx 16rpx;
  background: #F5F5F5;
  color: #666;
  font-size: 22rpx;
  border-radius: 20rpx;
  border: none;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.info-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.item-label {
  font-size: 28rpx;
  color: #666;
}

.item-value {
  font-size: 28rpx;
  color: #333;
}

.creator-info {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.creator-avatar {
  width: 50rpx;
  height: 50rpx;
  border-radius: 50%;
}

.creator-name {
  font-size: 28rpx;
  color: #333;
}

/* 参与人员卡片 */
.participants-card {
  background: #fff;
  padding: 30rpx;
  margin: 0 20rpx 20rpx;
  border-radius: 16rpx;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.card-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.participants-list {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.participant-item {
  display: flex;
  align-items: center;
  gap: 15rpx;
  padding: 15rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
}

.participant-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.participant-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.participant-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.participant-dishes {
  display: flex;
  gap: 15rpx;
}

.dish-count,
.dish-amount {
  font-size: 24rpx;
  color: #999;
}

.participant-status {
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 24rpx;

  &.status-completed {
    background: #F6FFED;
    color: #52C41A;
  }

  &.status-pending {
    background: #FFF7E6;
    color: #FAAD14;
  }
}

/* 菜品卡片 */
.dishes-card {
  background: #fff;
  padding: 30rpx;
  margin: 0 20rpx 20rpx;
  border-radius: 16rpx;
}

.total-section {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.total-label {
  font-size: 26rpx;
  color: #999;
}

.total-amount {
  font-size: 36rpx;
  color: #FF6B35;
  font-weight: bold;
}

.dishes-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.dish-item {
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.dish-image {
  width: 100rpx;
  height: 100rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
}

.dish-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.dish-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.dish-meta {
  display: flex;
  gap: 15rpx;
}

.dish-count,
.dish-users {
  font-size: 24rpx;
  color: #999;
}

.dish-price {
  font-size: 32rpx;
  color: #FF6B35;
  font-weight: bold;
}

/* 我的菜品卡片 */
.my-dishes-card {
  background: #fff;
  padding: 30rpx;
  margin: 0 20rpx 20rpx;
  border-radius: 16rpx;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  background: #F5F5F5;
  color: #666;
  font-size: 24rpx;
  border-radius: 20rpx;
  border: none;
}

.my-dishes-list {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.my-dish-item {
  display: flex;
  align-items: center;
  gap: 15rpx;
  padding: 15rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
}

.dish-thumb {
  width: 80rpx;
  height: 80rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
}

.dish-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.dish-spec {
  display: inline-block;
  align-self: flex-start;
  padding: 4rpx 10rpx;
  background: rgba(255, 107, 53, 0.1);
  color: #FF6B35;
  font-size: 22rpx;
  border-radius: 4rpx;
}

.spec-text {
  font-size: 22rpx;
}

.dish-amount {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 5rpx;
}

.amount-value {
  font-size: 28rpx;
  color: #FF6B35;
  font-weight: bold;
}

.amount-count {
  font-size: 24rpx;
  color: #999;
}

/* 操作按钮 */
.action-buttons {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx;
  background: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
  display: flex;
  gap: 20rpx;
}

.action-buttons .action-btn {
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
    background: #F5F5F5;
    color: #666;
  }
}

/* 已完成状态 */
.completed-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx;
  background: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.completed-info {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.completed-text {
  font-size: 28rpx;
  color: #52C41A;
  font-weight: bold;
}

.reorder-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 15rpx 30rpx;
  background: #FF6B35;
  color: #fff;
  font-size: 26rpx;
  border-radius: 30rpx;
  border: none;
}
</style>
