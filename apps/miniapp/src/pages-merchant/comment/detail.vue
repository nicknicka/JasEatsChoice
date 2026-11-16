<template>
  <view class="comment-detail-container">
    <!-- 加载中 -->
    <view class="page-status" v-if="loading">
      <text class="status-text">加载中...</text>
    </view>

    <!-- 加载失败：只给错误提示和重试入口，不渲染任何占位评价 -->
    <view class="page-status" v-else-if="loadError">
      <text class="status-text">{{ loadError }}</text>
      <button class="retry-btn" @tap="loadCommentDetail">重新加载</button>
    </view>

    <template v-else-if="comment">
      <!-- 评价卡片 -->
      <view class="comment-card">
        <!-- 用户信息：详情接口不返回用户头像字段，这里只渲染昵称 -->
        <view class="user-header">
          <view class="user-info">
            <text class="user-name">{{ comment.userName }}</text>
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
        <view class="comment-images" v-if="images.length > 0">
          <image
            class="comment-image"
            v-for="(img, index) in images"
            :key="index"
            :src="img"
            mode="aspectFill"
            @tap="previewImage(images, index)"
          ></image>
        </view>

        <!-- 订单信息：接口只回订单号和菜名，订单查不到时 orderNo 整个字段不存在 -->
        <view class="order-info" v-if="comment.orderNo || dishes.length > 0">
          <text class="order-no" v-if="comment.orderNo">订单号：{{ comment.orderNo }}</text>
          <text class="order-dishes" v-if="dishes.length > 0">{{ dishes.join('、') }}</text>
        </view>
      </view>

      <!-- 商家回复 -->
      <view class="reply-section" v-if="merchantReplies.length > 0">
        <view class="section-title">商家回复</view>
        <view
          class="reply-content-card"
          v-for="reply in merchantReplies"
          :key="reply.id"
        >
          <text class="reply-content">{{ reply.content }}</text>
          <text class="reply-time">{{ reply.time }}</text>
        </view>
      </view>

      <!-- 用户追评 -->
      <view class="reply-section" v-if="additionalReplies.length > 0">
        <view class="section-title">用户追评</view>
        <view
          class="reply-content-card"
          v-for="reply in additionalReplies"
          :key="reply.id"
        >
          <text class="reply-content">{{ reply.content }}</text>
          <text class="reply-time">{{ reply.time }}</text>
        </view>
      </view>

      <!-- 操作按钮：只有没有商家回复时才能回复 -->
      <view class="action-buttons" v-if="merchantReplies.length === 0">
        <button class="action-btn primary" @tap="replyComment">
          <uni-icons type="chatbubble" size="18" color="#fff"></uni-icons>
          <text>回复评价</text>
        </button>
      </view>
    </template>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { reviewApi, parseReviewImages } from '@/api/modules/review.js'

// 评价详情：字段全部来自 GET /v1/reviews/{reviewId}，
// 接口失败时只显示错误态，不渲染任何写死的评价内容（#593）
const comment = ref(null)
const loading = ref(false)
const loadError = ref('')
const reviewId = ref('')

const images = computed(() => parseReviewImages(comment.value?.images))
const dishes = computed(() => (Array.isArray(comment.value?.dishes) ? comment.value.dishes : []))
const replies = computed(() => (Array.isArray(comment.value?.replies) ? comment.value.replies : []))
// isMerchant 由后端按 isAdditional 算好：0（或缺失）是商家回复，1 是用户追评
const merchantReplies = computed(() => replies.value.filter(reply => reply.isMerchant))
const additionalReplies = computed(() => replies.value.filter(reply => !reply.isMerchant))

onMounted(() => {
  reviewId.value = resolveReviewId()
  loadCommentDetail()
})

/**
 * 取页面参数里的评价ID：列表页可能用 itemId / reviewId 跳转，回复页用的是 id
 */
const resolveReviewId = () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage?.options || {}
  return options.itemId || options.reviewId || options.id || ''
}

/**
 * 加载评价详情
 */
const loadCommentDetail = async () => {
  if (!reviewId.value) {
    loadError.value = '缺少评价ID，无法加载评价详情'
    return
  }

  loading.value = true
  loadError.value = ''

  try {
    const res = await reviewApi.getDetail(reviewId.value)

    // 后端 code 是字符串 "200"，不按数字比较；请求层已把 success=false（含 404）转成 reject
    const detail = res?.data
    if (!detail || typeof detail !== 'object') {
      throw new Error(res?.message || '获取评价详情失败')
    }

    comment.value = detail
  } catch (error) {
    comment.value = null
    loadError.value = error.message || '评价详情加载失败'
  } finally {
    loading.value = false
  }
}

/**
 * 预览图片
 */
const previewImage = (urls, current) => {
  uni.previewImage({
    urls: urls,
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
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.comment-detail-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 40rpx;
}

/* 加载 / 错误状态 */
.page-status {
  padding-top: 200rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30rpx;
}

.status-text {
  font-size: 28rpx;
  color: #999;
}

.retry-btn {
  height: 70rpx;
  padding: 0 50rpx;
  background: #FF6B35;
  color: #fff;
  font-size: 28rpx;
  border-radius: 35rpx;
  border: none;
  @include flex-center;
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

/* 回复区域 */
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

.reply-content-card {
  background: #F5F5F5;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 15rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.reply-content {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
  margin-bottom: 15rpx;
  display: block;
}

.reply-time {
  font-size: 22rpx;
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
