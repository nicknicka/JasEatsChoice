<template>
  <view class="wishlist-audit-container">
    <!-- 用户信息卡片 -->
    <view class="user-card">
      <image class="user-avatar" :src="wishDetail.user.avatar" mode="aspectFill"></image>
      <view class="user-info">
        <text class="user-name">{{ wishDetail.user.name }}</text>
        <text class="submit-time">{{ wishDetail.submitTime }}</text>
      </view>
      <view class="user-stats">
        <view class="stat-item">
          <text class="stat-value">{{ wishDetail.user.publishCount }}</text>
          <text class="stat-label">发布</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ wishDetail.user.completedCount }}</text>
          <text class="stat-label">完成</text>
        </view>
      </view>
    </view>

    <!-- 需求详情 -->
    <view class="wish-detail-section">
      <view class="section-title">需求内容</view>
      <text class="wish-content">{{ wishDetail.content }}</text>

      <!-- 期望菜品 -->
      <view class="detail-item" v-if="wishDetail.dishes.length > 0">
        <text class="item-label">期望菜品</text>
        <view class="dish-tags">
          <text class="dish-tag" v-for="dish in wishDetail.dishes" :key="dish">
            {{ dish }}
          </text>
        </view>
      </view>

      <!-- 预算范围 -->
      <view class="detail-item" v-if="wishDetail.budget">
        <text class="item-label">预算范围</text>
        <text class="item-value">¥{{ wishDetail.budget }}</text>
      </view>

      <!-- 期望时间 -->
      <view class="detail-item" v-if="wishDetail.expectTime">
        <text class="item-label">期望时间</text>
        <text class="item-value">{{ wishDetail.expectTime }}</text>
      </view>

      <!-- 特殊要求 -->
      <view class="detail-item" v-if="wishDetail.requirements">
        <text class="item-label">特殊要求</text>
        <text class="item-value">{{ wishDetail.requirements }}</text>
      </view>
    </view>

    <!-- 互动数据 -->
    <view class="interaction-section">
      <view class="section-title">互动情况</view>
      <view class="interaction-stats">
        <view class="stat-item">
          <text class="stat-value">{{ wishDetail.viewCount }}</text>
          <text class="stat-label">浏览</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ wishDetail.likeCount }}</text>
          <text class="stat-label">点赞</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ wishDetail.replyCount }}</text>
          <text class="stat-label">回复</text>
        </view>
      </view>
    </view>

    <!-- 商家回复列表 -->
    <view class="reply-section" v-if="wishDetail.replies.length > 0">
      <view class="section-title">商家回复 ({{ wishDetail.replies.length }})</view>
      <view class="reply-list">
        <view
          class="reply-item"
          v-for="reply in wishDetail.replies"
          :key="reply.id"
        >
          <view class="reply-header">
            <image class="merchant-avatar" :src="reply.merchant.avatar" mode="aspectFill"></image>
            <view class="merchant-info">
              <text class="merchant-name">{{ reply.merchant.name }}</text>
              <text class="reply-time">{{ reply.time }}</text>
            </view>
            <view class="reply-status" v-if="reply.status">
              {{ reply.statusText }}
            </view>
          </view>
          <text class="reply-content">{{ reply.content }}</text>
          <!-- 回复的菜品 -->
          <view class="reply-dishes" v-if="reply.dishes.length > 0">
            <text class="dishes-label">推荐菜品：</text>
            <view class="dishes-list">
              <view
                class="dish-card"
                v-for="dish in reply.dishes"
                :key="dish.id"
              >
                <image class="dish-image" :src="dish.image" mode="aspectFill"></image>
                <text class="dish-name">{{ dish.name }}</text>
                <text class="dish-price">¥{{ dish.price }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons" v-if="wishDetail.status === 'pending'">
      <button class="action-btn reject" @tap="rejectWish">
        无法满足
      </button>
      <button class="action-btn accept" @tap="acceptWish">
        接受并添加菜品
      </button>
    </view>

    <!-- 已处理状态 -->
    <view class="processed-info" v-else>
      <view class="status-badge" :class="'status-' + wishDetail.status">
        {{ wishDetail.statusText }}
      </view>
      <view class="process-time" v-if="wishDetail.processTime">
        处理时间：{{ wishDetail.processTime }}
      </view>
      <view class="process-note" v-if="wishDetail.processNote">
        备注：{{ wishDetail.processNote }}
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { showConfirm } from '@/utils/helper'

// 需求详情
const wishDetail = ref({
  id: 1,
  user: {
    id: 1,
    name: '张同学',
    avatar: 'https://via.placeholder.com/80/FF6B35/FFFFFF?text=张',
    publishCount: 5,
    completedCount: 3
  },
  content: '想吃家乡的味道，有没有正宗的川菜推荐？最好是那种麻辣鲜香的，不要太辣，微辣就好。',
  dishes: ['宫保鸡丁', '水煮鱼', '麻婆豆腐'],
  budget: '50-80',
  expectTime: '本周五午餐',
  requirements: '少放辣椒，多放葱花',
  submitTime: '2026-03-18 12:30',
  viewCount: 25,
  likeCount: 8,
  replyCount: 2,
  status: 'pending',
  statusText: '待审核',
  replies: [
    {
      id: 1,
      merchant: {
        id: 1,
        name: '老王家常菜',
        avatar: 'https://via.placeholder.com/60/FF6B35/FFFFFF?text=店'
      },
      content: '同学你好！我们家的宫保鸡丁和鱼香肉丝都很受欢迎，麻辣鲜香，可以微辣哦。',
      time: '1小时前',
      status: 'accepted',
      statusText: '已接受',
      dishes: [
        { id: 1, name: '宫保鸡丁', price: 28, image: 'https://via.placeholder.com/100/FF6B35/FFFFFF?text=1' },
        { id: 2, name: '鱼香肉丝', price: 26, image: 'https://via.placeholder.com/100/FF6B35/FFFFFF?text=2' }
      ]
    }
  ]
})

onMounted(() => {
  loadWishDetail()
})

/**
 * 加载需求详情
 */
const loadWishDetail = () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options
  const wishId = options.id

  // TODO: 调用API获取需求详情
  // const res = await merchantApi.getWishDetail({ id: wishId })
  // wishDetail.value = res.data
}

/**
 * 接受需求
 */
const acceptWish = () => {
  uni.showModal({
    title: '接受需求',
    content: '接受后将通知用户，您可以立即添加菜品或稍后添加。确认接受吗？',
    success: (res) => {
      if (res.confirm) {
        // TODO: 调用API接受需求
        uni.showModal({
          title: '提示',
          content: '是否立即添加菜品？',
          success: (res) => {
            if (res.confirm) {
              uni.navigateTo({
                url: '/dish/add'
              })
            } else {
              uni.showToast({
                title: '已接受',
                icon: 'success'
              })
              setTimeout(() => {
                uni.navigateBack()
              }, 1500)
            }
          }
        })
      }
    }
  })
}

/**
 * 拒绝需求
 */
const rejectWish = async () => {
  const reasons = ['暂时无法满足', '食材不足', '不在营业范围', '其他原因']

  uni.showActionSheet({
    itemList: [...reasons, '自定义原因'],
    success: async (res) => {
      let note = ''

      if (res.tapIndex === reasons.length) {
        // 自定义原因
        const input = await uni.showModal({
          title: '拒绝原因',
          content: '',
          editable: true,
          placeholderText: '请输入拒绝原因'
        })
        if (input.confirm) {
          note = input.content
        } else {
          return
        }
      } else {
        note = reasons[res.tapIndex]
      }

      // TODO: 调用API拒绝需求
      uni.showToast({
        title: '已拒绝',
        icon: 'success'
      })

      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.wishlist-audit-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 40rpx;
}

/* 用户卡片 */
.user-card {
  background: linear-gradient(135deg, #FF6B35, #FF8F6B);
  padding: 40rpx 30rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.user-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.user-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #fff;
}

.submit-time {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.user-stats {
  display: flex;
  gap: 20rpx;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5rpx;
}

.stat-value {
  font-size: 32rpx;
  font-weight: bold;
  color: #fff;
}

.stat-label {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.8);
}

/* 需求详情 */
.wish-detail-section,
.interaction-section,
.reply-section {
  background: #fff;
  padding: 30rpx;
  margin: 20rpx;
  border-radius: 16rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.wish-content {
  font-size: 28rpx;
  color: #333;
  line-height: 1.8;
  margin-bottom: 20rpx;
}

.detail-item {
  display: flex;
  margin-bottom: 15rpx;
  padding: 15rpx;
  background: #F5F5F5;
  border-radius: 8rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.item-label {
  width: 150rpx;
  font-size: 26rpx;
  color: #999;
  flex-shrink: 0;
}

.item-value {
  flex: 1;
  font-size: 26rpx;
  color: #333;
}

.dish-tags {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.dish-tag {
  padding: 6rpx 16rpx;
  background: rgba(255, 107, 53, 0.1);
  color: #FF6B35;
  font-size: 24rpx;
  border-radius: 20rpx;
}

/* 互动数据 */
.interaction-stats {
  display: flex;
  gap: 40rpx;
}

/* 回复列表 */
.reply-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.reply-item {
  background: #F5F5F5;
  border-radius: 12rpx;
  padding: 20rpx;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 15rpx;
  margin-bottom: 15rpx;
}

.merchant-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.merchant-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.merchant-name {
  font-size: 26rpx;
  font-weight: bold;
  color: #333;
}

.reply-time {
  font-size: 22rpx;
  color: #999;
}

.reply-status {
  padding: 4rpx 12rpx;
  background: #F6FFED;
  color: #52C41A;
  font-size: 22rpx;
  border-radius: 12rpx;
}

.reply-content {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
  margin-bottom: 15rpx;
}

.reply-dishes {
  padding-top: 15rpx;
  border-top: 1rpx solid #eee;
}

.dishes-label {
  display: block;
  font-size: 24rpx;
  color: #999;
  margin-bottom: 10rpx;
}

.dishes-list {
  display: flex;
  gap: 15rpx;
}

.dish-card {
  width: 150rpx;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.dish-image {
  width: 150rpx;
  height: 150rpx;
  border-radius: 8rpx;
}

.dish-name {
  font-size: 24rpx;
  color: #333;
  @include text-ellipsis;
}

.dish-price {
  font-size: 26rpx;
  font-weight: bold;
  color: #FF6B35;
}

/* 操作按钮 */
.action-buttons {
  padding: 0 20rpx;
  display: flex;
  gap: 20rpx;
}

.action-btn {
  flex: 1;
  height: 90rpx;
  border-radius: 45rpx;
  font-size: 28rpx;
  border: none;

  &.accept {
    background: #FF6B35;
    color: #fff;
  }

  &.reject {
    background: #fff;
    color: #666;
    border: 2rpx solid #eee;
  }
}

/* 已处理状态 */
.processed-info {
  background: #fff;
  margin: 20rpx;
  padding: 40rpx 30rpx;
  border-radius: 16rpx;
  text-align: center;
}

.status-badge {
  display: inline-block;
  padding: 12rpx 30rpx;
  border-radius: 30rpx;
  font-size: 28rpx;
  font-weight: bold;
  margin-bottom: 15rpx;

  &.status-accepted {
    background: #F6FFED;
    color: #52C41A;
  }

  &.status-rejected {
    background: #FFF1F0;
    color: #F5222D;
  }

  &.status-completed {
    background: #E6F7FF;
    color: #1890FF;
  }
}

.process-time {
  font-size: 26rpx;
  color: #999;
  margin-bottom: 10rpx;
}

.process-note {
  font-size: 26rpx;
  color: #666;
  padding: 15rpx;
  background: #F5F5F5;
  border-radius: 8rpx;
}
</style>
