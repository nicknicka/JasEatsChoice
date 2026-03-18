<template>
  <view class="comment-detail-container">
    <!-- 评价卡片 -->
    <view class="comment-card">
      <!-- 用户信息 -->
      <view class="user-header">
        <image class="user-avatar" :src="comment.user.avatar" mode="aspectFill"></image>
        <view class="user-info">
          <text class="user-name">{{ comment.user.name }}</text>
          <view class="user-rating">
            <uni-rate :value="comment.rating" size="14" readonly></uni-rate>
            <text class="rating-text">{{ comment.rating }}分</text>
          </view>
        </view>
        <text class="comment-time">{{ comment.time }}</text>
      </view>

      <!-- 评价内容 -->
      <text class="comment-content">{{ comment.content }}</text>

      <!-- 评价图片 -->
      <view class="comment-images" v-if="comment.images.length > 0">
        <image
          class="comment-image"
          v-for="(img, index) in comment.images"
          :key="index"
          :src="img"
          mode="aspectFill"
          @tap="previewImage(comment.images, index)"
        ></image>
      </view>

      <!-- 订单信息 -->
      <view class="order-info" v-if="comment.order">
        <text class="order-no">订单号：{{ comment.order.no }}</text>
        <text class="order-dishes">{{ comment.order.dishes }}</text>
        <text class="order-amount">¥{{ comment.order.amount }}</text>
      </view>

      <!-- 标签 -->
      <view class="comment-tags" v-if="comment.tags.length > 0">
        <text
          class="tag"
          v-for="tag in comment.tags"
          :key="tag"
        >
          {{ tag }}
        </text>
      </view>

      <!-- 互动数据 -->
      <view class="interaction-stats">
        <view class="stat-item">
          <uni-icons type="hand-up" size="16" color="#999"></uni-icons>
          <text class="stat-value">{{ comment.likeCount }}</text>
        </view>
        <view class="stat-item">
          <uni-icons type="eye" size="16" color="#999"></uni-icons>
          <text class="stat-value">{{ comment.viewCount }}</text>
        </view>
      </view>
    </view>

    <!-- 商家回复 -->
    <view class="reply-section" v-if="comment.reply">
      <view class="section-title">商家回复</view>
      <view class="reply-content-card">
        <view class="reply-header">
          <image class="merchant-avatar" :src="merchantInfo.avatar" mode="aspectFill"></image>
          <view class="merchant-info">
            <text class="merchant-name">{{ merchantInfo.name }}</text>
            <text class="reply-time">{{ comment.reply.time }}</text>
          </view>
        </view>
        <text class="reply-content">{{ comment.reply.content }}</text>
        <view class="reply-actions" v-if="comment.reply.canEdit">
          <button class="action-btn" @tap="editReply">
            <uni-icons type="compose" size="16" color="#1890FF"></uni-icons>
            <text>编辑</text>
          </button>
        </view>
      </view>
    </view>

    <!-- 菜品信息 -->
    <view class="dish-section" v-if="comment.dish">
      <view class="section-title">关联菜品</view>
      <view class="dish-card">
        <image class="dish-image" :src="comment.dish.image" mode="aspectFill"></image>
        <view class="dish-info">
          <text class="dish-name">{{ comment.dish.name }}</text>
          <text class="dish-price">¥{{ comment.dish.price }}</text>
          <text class="dish-sales">月售{{ comment.dish.sales }}份</text>
        </view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons" v-if="!comment.reply">
      <button class="action-btn primary" @tap="replyComment">
        <uni-icons type="chatbubble" size="18" color="#fff"></uni-icons>
        <text>回复评价</text>
      </button>
      <button class="action-btn" @tap="shareComment">
        <uni-icons type="redo" size="18" color="#666"></uni-icons>
        <text>分享</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 商家信息
const merchantInfo = ref({
  id: 1,
  name: '老王家常菜',
  avatar: 'https://via.placeholder.com/60/FF6B35/FFFFFF?text=店'
})

// 评价详情
const comment = ref({
  id: 1,
  user: {
    id: 1,
    name: '张同学',
    avatar: 'https://via.placeholder.com/80/FF6B35/FFFFFF?text=张'
  },
  rating: 5,
  time: '2026-03-18 12:30',
  content: '味道非常不错，宫保鸡丁很正宗，麻辣鲜香，分量也很足，包装很仔细，送到还是热的。老板服务态度很好，下次还会再来的！强烈推荐给大家！',
  images: [
    'https://via.placeholder.com/300/FF6B35/FFFFFF?text=1',
    'https://via.placeholder.com/300/FF6B35/FFFFFF?text=2',
    'https://via.placeholder.com/300/FF6B35/FFFFFF?text=3'
  ],
  order: {
    no: 'OD202603180001',
    dishes: '宫保鸡丁、鱼香肉丝',
    amount: '54.00'
  },
  dish: {
    id: 1,
    name: '宫保鸡丁',
    price: 28,
    sales: 156,
    image: 'https://via.placeholder.com/100/FF6B35/FFFFFF?text=1'
  },
  tags: ['口味好', '分量足', '配送快', '服务好'],
  likeCount: 12,
  viewCount: 89,
  reply: null
})

onMounted(() => {
  loadCommentDetail()
})

/**
 * 加载评价详情
 */
const loadCommentDetail = () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options
  const commentId = options.id

  // TODO: 调用API获取评价详情
  // const res = await merchantApi.getCommentDetail({ id: commentId })
  // comment.value = res.data
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
 * 回复评价
 */
const replyComment = () => {
  uni.navigateTo({
    url: `/pages-merchant/comment/reply?id=${comment.value.id}`
  })
}

/**
 * 编辑回复
 */
const editReply = () => {
  uni.navigateTo({
    url: `/pages-merchant/comment/reply?id=${comment.value.id}&edit=true`
  })
}

/**
 * 分享评价
 */
const shareComment = () => {
  uni.showActionSheet({
    itemList: ['生成海报', '复制链接', '分享到微信'],
    success: (res) => {
      uni.showToast({
        title: '分享功能开发中',
        icon: 'none'
      })
    }
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.comment-detail-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 40rpx;
}

/* 评价卡片 */
.comment-card {
  background: #fff;
  padding: 30rpx;
  margin: 20rpx;
  border-radius: 16rpx;
}

/* 用户头部 */
.user-header {
  display: flex;
  align-items: center;
  gap: 15rpx;
  margin-bottom: 20rpx;
}

.user-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.user-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

.user-rating {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.rating-text {
  font-size: 24rpx;
  color: #FF6B35;
  font-weight: bold;
}

.comment-time {
  font-size: 24rpx;
  color: #999;
}

/* 评价内容 */
.comment-content {
  font-size: 28rpx;
  color: #333;
  line-height: 1.8;
  margin-bottom: 20rpx;
}

.comment-images {
  display: flex;
  flex-wrap: wrap;
  gap: 15rpx;
  margin-bottom: 20rpx;
}

.comment-image {
  width: 200rpx;
  height: 200rpx;
  border-radius: 12rpx;
}

/* 订单信息 */
.order-info {
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  margin-bottom: 20rpx;
}

.order-no {
  font-size: 26rpx;
  color: #666;
}

.order-dishes {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
}

.order-amount {
  font-size: 32rpx;
  color: #FF6B35;
  font-weight: bold;
  align-self: flex-end;
}

/* 标签 */
.comment-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-bottom: 20rpx;
}

.tag {
  padding: 8rpx 20rpx;
  background: rgba(255, 107, 53, 0.1);
  color: #FF6B35;
  font-size: 24rpx;
  border-radius: 20rpx;
}

/* 互动数据 */
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
  font-size: 26rpx;
  color: #999;
}

/* 回复区域 */
.reply-section,
.dish-section {
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

.reply-content-card {
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

.reply-content {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
  margin-bottom: 15rpx;
}

.reply-actions {
  display: flex;
  gap: 15rpx;
}

.action-btn {
  height: 60rpx;
  padding: 0 20rpx;
  background: #fff;
  border-radius: 30rpx;
  @include flex-center;
  gap: 8rpx;
  font-size: 24rpx;
  color: #666;
  border: none;
}

/* 菜品信息 */
.dish-card {
  display: flex;
  gap: 20rpx;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
}

.dish-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
}

.dish-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.dish-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.dish-price {
  font-size: 32rpx;
  font-weight: bold;
  color: #FF6B35;
}

.dish-sales {
  font-size: 24rpx;
  color: #999;
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
  background: #fff;
  color: #666;
  border: none;
  @include flex-center;
  gap: 10rpx;

  &.primary {
    background: #FF6B35;
    color: #fff;
  }
}
</style>
