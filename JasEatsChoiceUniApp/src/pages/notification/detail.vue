<template>
  <view class="notification-detail-container">
    <!-- 通知头部 -->
    <view class="notification-header" :class="'type-' + notificationDetail.type">
      <view class="header-icon">
        <uni-icons
          :type="getNotificationIcon(notificationDetail.type)"
          size="40"
          color="#fff"
        ></uni-icons>
      </view>
      <view class="header-info">
        <text class="notification-type">{{ getNotificationTypeText(notificationDetail.type) }}</text>
        <text class="notification-time">{{ notificationDetail.time }}</text>
      </view>
      <view class="header-action" @tap="deleteNotification">
        <uni-icons type="trash" size="20" color="#fff"></uni-icons>
      </view>
    </view>

    <!-- 通知内容 -->
    <view class="notification-content-card">
      <view class="content-title">{{ notificationDetail.title }}</view>
      <text class="content-body">{{ notificationDetail.content }}</text>

      <!-- 关联数据 -->
      <view class="related-data" v-if="notificationDetail.relatedData">
        <!-- 订单信息 -->
        <view
          class="order-related"
          v-if="notificationDetail.relatedData.order"
          @tap="viewOrder"
        >
          <view class="related-header">
            <uni-icons type="shop" size="18" color="#FF6B35"></uni-icons>
            <text class="related-title">订单信息</text>
          </view>
          <view class="related-content">
            <text class="order-no">订单号：{{ notificationDetail.relatedData.order.orderNo }}</text>
            <text class="order-dishes">{{ notificationDetail.relatedData.order.dishes }}</text>
            <text class="order-amount">¥{{ notificationDetail.relatedData.order.amount }}</text>
          </view>
        </view>

        <!-- 菜品信息 -->
        <view
          class="dish-related"
          v-if="notificationDetail.relatedData.dish"
          @tap="viewDish"
        >
          <view class="related-header">
            <uni-icons type="image" size="18" color="#FF6B35"></uni-icons>
            <text class="related-title">菜品信息</text>
          </view>
          <view class="related-content">
            <image class="dish-image" :src="notificationDetail.relatedData.dish.image" mode="aspectFill"></image>
            <text class="dish-name">{{ notificationDetail.relatedData.dish.name }}</text>
            <text class="dish-price">¥{{ notificationDetail.relatedData.dish.price }}</text>
          </view>
        </view>

        <!-- 商家信息 -->
        <view
          class="merchant-related"
          v-if="notificationDetail.relatedData.merchant"
          @tap="viewMerchant"
        >
          <view class="related-header">
            <uni-icons type="home" size="18" color="#FF6B35"></uni-icons>
            <text class="related-title">商家信息</text>
          </view>
          <view class="related-content">
            <image class="merchant-avatar" :src="notificationDetail.relatedData.merchant.avatar" mode="aspectFill"></image>
            <text class="merchant-name">{{ notificationDetail.relatedData.merchant.name }}</text>
            <text class="merchant-category">{{ notificationDetail.relatedData.merchant.category }}</text>
          </view>
        </view>

        <!-- 评价信息 -->
        <view
          class="review-related"
          v-if="notificationDetail.relatedData.review"
        >
          <view class="related-header">
            <uni-icons type="star" size="18" color="#FF6B35"></uni-icons>
            <text class="related-title">评价信息</text>
          </view>
          <view class="related-content">
            <view class="review-header">
              <image class="reviewer-avatar" :src="notificationDetail.relatedData.review.user.avatar" mode="aspectFill"></image>
              <view class="reviewer-info">
                <text class="reviewer-name">{{ notificationDetail.relatedData.review.user.name }}</text>
                <uni-rate :value="notificationDetail.relatedData.review.rating" size="12" readonly></uni-rate>
              </view>
            </view>
            <text class="review-content">{{ notificationDetail.relatedData.review.content }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <button
        class="action-btn"
        v-if="notificationDetail.relatedData.order"
        @tap="viewOrder"
      >
        查看订单
      </button>
      <button
        class="action-btn"
        v-if="notificationDetail.relatedData.dish"
        @tap="viewDish"
      >
        查看菜品
      </button>
      <button
        class="action-btn"
        v-if="notificationDetail.relatedData.merchant"
        @tap="viewMerchant"
      >
        查看商家
      </button>
      <button
        class="action-btn primary"
        @tap="markAsRead"
        v-if="!notificationDetail.isRead"
      >
        标记已读
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 通知详情
const notificationDetail = ref({
  id: 1,
  type: 'order', // order, dish, merchant, review, system
  title: '您的订单已接单',
  content: '您在"老王家常菜"的订单已被商家接单，正在准备中，预计15分钟内完成。',
  time: '2026-03-18 12:35',
  isRead: false,
  relatedData: {
    order: {
      id: 1,
      orderNo: 'OD202603180001',
      dishes: '宫保鸡丁、鱼香肉丝',
      amount: '54.00'
    }
  }
})

onMounted(() => {
  loadNotificationDetail()
})

/**
 * 加载通知详情
 */
const loadNotificationDetail = () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options
  const notificationId = options.id

  // TODO: 调用API获取通知详情
  // const res = await userApi.getNotificationDetail({ id: notificationId })
  // notificationDetail.value = res.data

  // 自动标记为已读
  if (!notificationDetail.value.isRead) {
    markAsRead()
  }
}

/**
 * 获取通知图标
 */
const getNotificationIcon = (type) => {
  const iconMap = {
    order: 'shop',
    dish: 'image',
    merchant: 'home',
    review: 'star',
    system: 'notification'
  }
  return iconMap[type] || 'notification'
}

/**
 * 获取通知类型文本
 */
const getNotificationTypeText = (type) => {
  const textMap = {
    order: '订单通知',
    dish: '菜品推荐',
    merchant: '商家消息',
    review: '评价提醒',
    system: '系统通知'
  }
  return textMap[type] || '通知'
}

/**
 * 查看订单
 */
const viewOrder = () => {
  uni.navigateTo({
    url: `/pages-user/order/detail/index?id=${notificationDetail.value.relatedData.order.id}`
  })
}

/**
 * 查看菜品
 */
const viewDish = () => {
  uni.navigateTo({
    url: `/pages-user/dish/detail/index?id=${notificationDetail.value.relatedData.dish.id}`
  })
}

/**
 * 查看商家
 */
const viewMerchant = () => {
  uni.navigateTo({
    url: `/pages-user/home/merchant-detail/index?id=${notificationDetail.value.relatedData.merchant.id}`
  })
}

/**
 * 标记已读
 */
const markAsRead = () => {
  // TODO: 调用API标记为已读
  notificationDetail.value.isRead = true
  uni.showToast({
    title: '已标记为已读',
    icon: 'success'
  })
}

/**
 * 删除通知
 */
const deleteNotification = () => {
  uni.showModal({
    title: '删除通知',
    content: '确定删除这条通知吗？',
    success: (res) => {
      if (res.confirm) {
        // TODO: 调用API删除通知
        uni.showToast({
          title: '已删除',
          icon: 'success'
        })

        setTimeout(() => {
          uni.navigateBack()
        }, 1500)
      }
    }
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.notification-detail-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 140rpx;
}

/* 通知头部 */
.notification-header {
  background: linear-gradient(135deg, #FF6B35, #FF8F6B);
  padding: 30rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;

  &.type-order {
    background: linear-gradient(135deg, #1890FF, #40A9FF);
  }

  &.type-dish {
    background: linear-gradient(135deg, #52C41A, #73D13D);
  }

  &.type-merchant {
    background: linear-gradient(135deg, #FA541C, #FF7A45);
  }

  &.type-review {
    background: linear-gradient(135deg, #FAAD14, #FFEC3D);
  }

  &.type-system {
    background: linear-gradient(135deg, #722ED1, #9254DE);
  }
}

.header-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  @include flex-center;
  flex-shrink: 0;
}

.header-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.notification-type {
  font-size: 32rpx;
  font-weight: bold;
  color: #fff;
}

.notification-time {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.header-action {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  @include flex-center;
  flex-shrink: 0;
}

/* 通知内容 */
.notification-content-card {
  background: #fff;
  padding: 30rpx;
  margin: 20rpx;
  border-radius: 16rpx;
}

.content-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.content-body {
  font-size: 28rpx;
  color: #666;
  line-height: 1.8;
  margin-bottom: 30rpx;
  display: block;
}

/* 关联数据 */
.related-data {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.related-data > view {
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
}

.related-header {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 15rpx;
}

.related-title {
  font-size: 26rpx;
  color: #333;
  font-weight: 500;
}

.related-content {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

/* 订单相关 */
.order-no,
.order-dishes,
.order-amount {
  font-size: 26rpx;
  color: #666;
}

.order-amount {
  font-size: 32rpx;
  color: #FF6B35;
  font-weight: bold;
  align-self: flex-end;
}

/* 菜品相关 */
.dish-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 8rpx;
}

.dish-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.dish-price {
  font-size: 32rpx;
  color: #FF6B35;
  font-weight: bold;
}

/* 商家相关 */
.merchant-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 12rpx;
}

.merchant-name {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
}

.merchant-category {
  font-size: 24rpx;
  color: #999;
}

/* 评价相关 */
.review-header {
  display: flex;
  align-items: center;
  gap: 15rpx;
  margin-bottom: 15rpx;
}

.reviewer-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
}

.reviewer-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.reviewer-name {
  font-size: 26rpx;
  color: #333;
}

.review-content {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
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
  gap: 15rpx;
}

.action-btn {
  flex: 1;
  height: 80rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  background: #F5F5F5;
  color: #666;
  border: none;
  @include flex-center;

  &.primary {
    background: #FF6B35;
    color: #fff;
  }
}
</style>
