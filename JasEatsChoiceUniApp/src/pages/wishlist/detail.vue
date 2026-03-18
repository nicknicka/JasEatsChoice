<template>
  <view class="wish-detail-container">
    <!-- 用户信息卡片 -->
    <view class="user-card">
      <image class="user-avatar" :src="wishDetail.user.avatar" mode="aspectFill"></image>
      <view class="user-info">
        <text class="user-name">{{ wishDetail.user.name }}</text>
        <text class="publish-time">{{ wishDetail.publishTime }}</text>
      </view>
      <view class="wish-status" :class="'status-' + wishDetail.status">
        {{ wishDetail.statusText }}
      </view>
    </view>

    <!-- 心愿详情 -->
    <view class="wish-detail-section">
      <view class="wish-content">{{ wishDetail.content }}</view>

      <!-- 期望菜品 -->
      <view class="dish-tags" v-if="wishDetail.dishes.length > 0">
        <text
          class="dish-tag"
          v-for="dish in wishDetail.dishes"
          :key="dish"
        >
          {{ dish }}
        </text>
      </view>

      <!-- 其他信息 -->
      <view class="wish-meta-list">
        <view class="meta-item" v-if="wishDetail.budget">
          <uni-icons type="wallet" size="16" color="#FF6B35"></uni-icons>
          <text class="meta-label">预算</text>
          <text class="meta-value">¥{{ wishDetail.budget }}</text>
        </view>
        <view class="meta-item" v-if="wishDetail.expectTime">
          <uni-icons type="calendar" size="16" color="#FF6B35"></uni-icons>
          <text class="meta-label">期望时间</text>
          <text class="meta-value">{{ wishDetail.expectTime }}</text>
        </view>
        <view class="meta-item" v-if="wishDetail.requirements">
          <uni-icons type="compose" size="16" color="#FF6B35"></uni-icons>
          <text class="meta-label">特殊要求</text>
          <text class="meta-value">{{ wishDetail.requirements }}</text>
        </view>
      </view>

      <!-- 参考图片 -->
      <view class="wish-images" v-if="wishDetail.images.length > 0">
        <image
          class="wish-image"
          v-for="(img, index) in wishDetail.images"
          :key="index"
          :src="img"
          mode="aspectFill"
          @tap="previewImage(wishDetail.images, index)"
        ></image>
      </view>

      <!-- 互动数据 -->
      <view class="interaction-stats">
        <view class="stat-item">
          <uni-icons type="hand-up" size="16" color="#999"></uni-icons>
          <text class="stat-value">{{ wishDetail.likeCount }}</text>
          <text class="stat-label">点赞</text>
        </view>
        <view class="stat-item">
          <uni-icons type="chatbubble" size="16" color="#999"></uni-icons>
          <text class="stat-value">{{ wishDetail.replyCount }}</text>
          <text class="stat-label">回复</text>
        </view>
        <view class="stat-item">
          <uni-icons type="eye" size="16" color="#999"></uni-icons>
          <text class="stat-value">{{ wishDetail.viewCount }}</text>
          <text class="stat-label">浏览</text>
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
          @tap="viewMerchantDetail(reply.merchant.id)"
        >
          <view class="reply-header">
            <image class="merchant-avatar" :src="reply.merchant.avatar" mode="aspectFill"></image>
            <view class="merchant-info">
              <text class="merchant-name">{{ reply.merchant.name }}</text>
              <view class="merchant-tags">
                <text class="tag">{{ reply.merchant.category }}</text>
                <text class="tag distance">{{ reply.merchant.distance }}</text>
              </view>
            </view>
            <view class="reply-status" v-if="reply.status">
              {{ reply.statusText }}
            </view>
          </view>
          <text class="reply-content">{{ reply.content }}</text>

          <!-- 推荐菜品 -->
          <view class="recommend-dishes" v-if="reply.dishes.length > 0">
            <text class="dishes-label">推荐菜品：</text>
            <scroll-view scroll-x class="dishes-scroll">
              <view
                class="dish-card"
                v-for="dish in reply.dishes"
                :key="dish.id"
                @tap.stop="viewDishDetail(dish.id)"
              >
                <image class="dish-image" :src="dish.image" mode="aspectFill"></image>
                <text class="dish-name">{{ dish.name }}</text>
                <text class="dish-price">¥{{ dish.price }}</text>
              </view>
            </scroll-view>
          </view>

          <!-- 回复时间 -->
          <text class="reply-time">{{ reply.time }}</text>
        </view>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-actions" v-if="wishDetail.status === 'pending'">
      <button class="action-btn like" @tap="likeWish">
        <uni-icons type="hand-up" size="18" :color="wishDetail.isLiked ? '#FF6B35' : '#666'"></uni-icons>
        <text :class="{ active: wishDetail.isLiked }">{{ wishDetail.likeCount }}</text>
      </button>
      <button class="action-btn share" @tap="shareWish">
        <uni-icons type="redo" size="18" color="#666"></uni-icons>
        <text>分享</text>
      </button>
    </view>

    <!-- 已完成状态 -->
    <view class="completed-info" v-else-if="wishDetail.status === 'completed'">
      <view class="completed-badge">
        <uni-icons type="checkbox-filled" size="20" color="#52C41A"></uni-icons>
        <text>已完成</text>
      </view>
      <text class="completed-text">{{ wishDetail.completedText }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 心愿详情
const wishDetail = ref({
  id: 1,
  user: {
    id: 1,
    name: '张同学',
    avatar: 'https://via.placeholder.com/80/FF6B35/FFFFFF?text=张'
  },
  content: '想吃正宗的川菜，有没有推荐的商家？最好是麻辣鲜香的口感，不要太辣，微辣就好。',
  dishes: ['宫保鸡丁', '水煮鱼', '麻婆豆腐'],
  budget: '50-80',
  expectTime: '本周五午餐',
  requirements: '少放辣椒，多放葱花',
  publishTime: '2小时前',
  images: [
    'https://via.placeholder.com/300/FF6B35/FFFFFF?text=1',
    'https://via.placeholder.com/300/FF6B35/FFFFFF?text=2'
  ],
  status: 'pending',
  statusText: '待响应',
  likeCount: 8,
  isLiked: false,
  replyCount: 3,
  viewCount: 25,
  replies: [
    {
      id: 1,
      merchant: {
        id: 1,
        name: '老王家常菜',
        avatar: 'https://via.placeholder.com/60/FF6B35/FFFFFF?text=店',
        category: '川菜',
        distance: '500m'
      },
      content: '同学你好！我们家的宫保鸡丁和鱼香肉丝都很受欢迎，麻辣鲜香，可以微辣哦。欢迎光临！',
      dishes: [
        {
          id: 1,
          name: '宫保鸡丁',
          price: 28,
          image: 'https://via.placeholder.com/100/FF6B35/FFFFFF?text=1'
        },
        {
          id: 2,
          name: '鱼香肉丝',
          price: 26,
          image: 'https://via.placeholder.com/100/FF6B35/FFFFFF?text=2'
        }
      ],
      status: 'accepted',
      statusText: '已采纳',
      time: '1小时前'
    }
  ],
  completedText: ''
})

onMounted(() => {
  loadWishDetail()
})

/**
 * 加载心愿详情
 */
const loadWishDetail = () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options
  const wishId = options.id

  // TODO: 调用API获取心愿详情
  // const res = await userApi.getWishDetail({ id: wishId })
  // wishDetail.value = res.data
}

/**
 * 预览图片
 */
const previewImage = (images, current) => {
  uni.previewImage({
    urls: images,
    current: current
  })
}

/**
 * 点赞心愿
 */
const likeWish = () => {
  wishDetail.value.isLiked = !wishDetail.value.isLiked
  wishDetail.value.likeCount += wishDetail.value.isLiked ? 1 : -1

  // TODO: 调用API点赞
  uni.showToast({
    title: wishDetail.value.isLiked ? '已点赞' : '已取消',
    icon: 'success'
  })
}

/**
 * 分享心愿
 */
const shareWish = () => {
  uni.showActionSheet({
    itemList: ['生成海报', '分享给好友', '分享到朋友圈'],
    success: (res) => {
      uni.showToast({
        title: '分享功能开发中',
        icon: 'none'
      })
    }
  })
}

/**
 * 查看商家详情
 */
const viewMerchantDetail = (merchantId) => {
  uni.navigateTo({
    url: `/pages/merchant/detail?id=${merchantId}`
  })
}

/**
 * 查看菜品详情
 */
const viewDishDetail = (dishId) => {
  uni.navigateTo({
    url: `/pages/dish/detail?id=${dishId}`
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.wish-detail-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 120rpx;
}

/* 用户卡片 */
.user-card {
  background: linear-gradient(135deg, #FF6B35, #FF8F6B);
  padding: 30rpx;
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

.publish-time {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.wish-status {
  padding: 8rpx 20rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20rpx;
  font-size: 24rpx;
  color: #fff;

  &.status-pending {
    background: rgba(250, 173, 20, 0.2);
  }

  &.status-processing {
    background: rgba(24, 144, 255, 0.2);
  }

  &.status-completed {
    background: rgba(82, 196, 26, 0.2);
  }
}

/* 心愿详情 */
.wish-detail-section {
  background: #fff;
  padding: 30rpx;
  margin: 20rpx;
  border-radius: 16rpx;
}

.wish-content {
  font-size: 30rpx;
  color: #333;
  line-height: 1.8;
  margin-bottom: 20rpx;
  display: block;
}

.dish-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-bottom: 20rpx;
}

.dish-tag {
  padding: 8rpx 20rpx;
  background: rgba(255, 107, 53, 0.1);
  color: #FF6B35;
  font-size: 26rpx;
  border-radius: 20rpx;
}

.wish-meta-list {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
  margin-bottom: 20rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 15rpx;
  background: #F5F5F5;
  border-radius: 8rpx;
}

.meta-label {
  font-size: 26rpx;
  color: #999;
  flex-shrink: 0;
}

.meta-value {
  font-size: 28rpx;
  color: #333;
  flex: 1;
}

.wish-images {
  display: flex;
  gap: 15rpx;
  margin-bottom: 20rpx;
}

.wish-image {
  width: 200rpx;
  height: 200rpx;
  border-radius: 12rpx;
}

.interaction-stats {
  display: flex;
  gap: 40rpx;
  padding-top: 15rpx;
  border-top: 1rpx solid #eee;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.stat-value {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
}

.stat-label {
  font-size: 24rpx;
  color: #999;
}

/* 商家回复 */
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
  width: 70rpx;
  height: 70rpx;
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
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
}

.merchant-tags {
  display: flex;
  gap: 10rpx;
}

.tag {
  font-size: 22rpx;
  color: #666;
  padding: 4rpx 10rpx;
  background: #fff;
  border-radius: 4rpx;

  &.distance {
    color: #FF6B35;
  }
}

.reply-status {
  padding: 6rpx 12rpx;
  background: #F6FFED;
  color: #52C41A;
  font-size: 22rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.reply-content {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
  margin-bottom: 15rpx;
  display: block;
}

.recommend-dishes {
  margin-bottom: 15rpx;
}

.dishes-label {
  font-size: 24rpx;
  color: #999;
  display: block;
  margin-bottom: 10rpx;
}

.dishes-scroll {
  white-space: nowrap;
}

.dish-card {
  display: inline-block;
  width: 150rpx;
  margin-right: 15rpx;
  vertical-align: top;
}

.dish-image {
  width: 150rpx;
  height: 150rpx;
  border-radius: 8rpx;
  margin-bottom: 8rpx;
}

.dish-name {
  font-size: 24rpx;
  color: #333;
  display: block;
  @include text-ellipsis;
}

.dish-price {
  font-size: 26rpx;
  color: #FF6B35;
  font-weight: bold;
  display: block;
}

.reply-time {
  font-size: 22rpx;
  color: #999;
}

/* 底部操作栏 */
.bottom-actions {
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

.action-btn {
  flex: 1;
  height: 80rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  background: #F5F5F5;
  color: #666;
  border: none;
  @include flex-center;
  gap: 10rpx;

  &.like text.active {
    color: #FF6B35;
  }
}

/* 已完成状态 */
.completed-info {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 30rpx;
  background: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15rpx;
}

.completed-badge {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 12rpx 30rpx;
  background: #F6FFED;
  color: #52C41A;
  font-size: 28rpx;
  font-weight: bold;
  border-radius: 30rpx;
}

.completed-text {
  font-size: 26rpx;
  color: #666;
  text-align: center;
}
</style>
