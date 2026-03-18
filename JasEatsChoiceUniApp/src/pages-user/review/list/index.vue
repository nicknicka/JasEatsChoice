<template>
  <view class="review-list-container">
    <!-- 评分概览 -->
    <view class="rating-summary card" v-if="summaryData">
      <view class="rating-overview">
        <view class="rating-score">
          <text class="score-value">{{ summaryData.averageRating }}</text>
          <view class="rating-stars">
            <text class="star" v-for="i in 5" :key="i">
              {{ i <= Math.floor(summaryData.averageRating) ? '⭐' : '☆' }}
            </text>
          </view>
        </view>
        <view class="rating-count">{{ summaryData.totalCount }}条评价</view>
      </view>

      <view class="rating-distribution">
        <view class="distribution-item" v-for="item in ratingDistribution" :key="item.star">
          <text class="star-label">{{ item.star }}星</text>
          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: item.percentage + '%' }"></view>
          </view>
          <text class="percentage">{{ item.percentage }}%</text>
        </view>
      </view>
    </view>

    <!-- 评价标签 -->
    <view class="review-tags card" v-if="reviewTags.length > 0">
      <scroll-view class="tags-scroll" scroll-x show-scrollbar="false">
        <view class="tags-list">
          <view
            class="tag-item"
            :class="{ active: activeTag === tag.id }"
            v-for="tag in reviewTags"
            :key="tag.id"
            @click="selectTag(tag.id)"
          >
            {{ tag.label }} {{ tag.count }}
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 筛选Tab -->
    <view class="filter-tabs">
      <scroll-view class="tabs-scroll" scroll-x show-scrollbar="false">
        <view class="tabs-list">
          <view
            class="tab-item"
            :class="{ active: activeFilter === filter.value }"
            v-for="filter in filters"
            :key="filter.value"
            @click="switchFilter(filter.value)"
          >
            {{ filter.label }}
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 评价列表 -->
    <scroll-view class="review-scroll" scroll-y @scrolltolower="loadMore">
      <view class="review-list">
        <view
          class="review-item card"
          v-for="review in reviews"
          :key="review.id"
        >
          <!-- 用户信息 -->
          <view class="review-user">
            <image class="user-avatar" :src="review.user.avatar" mode="aspectFill" />
            <view class="user-info">
              <view class="user-name">{{ review.user.name }}</view>
              <view class="review-stars">
                <text class="star" v-for="i in 5" :key="i">
                  {{ i <= review.rating ? '⭐' : '☆' }}
                </text>
              </view>
            </view>
            <view class="review-date">{{ review.date }}</view>
          </view>

          <!-- 评价内容 -->
          <view class="review-content">{{ review.content }}</view>

          <!-- 评价图片 -->
          <view class="review-images" v-if="review.images && review.images.length > 0">
            <image
              class="review-image"
              v-for="(image, index) in review.images"
              :key="index"
              :src="image"
              mode="aspectFill"
              @click="previewImage(review.images, index)"
            />
          </view>

          <!-- 菜品信息 -->
          <view class="review-dishes" v-if="review.dishes && review.dishes.length > 0">
            <view class="dish-item" v-for="dish in review.dishes" :key="dish.id">
              <image class="dish-image" :src="dish.image" mode="aspectFill" />
              <text class="dish-name">{{ dish.name }}</text>
            </view>
          </view>

          <!-- 商家回复 -->
          <view class="review-reply" v-if="review.merchantReply">
            <view class="reply-header">
              <text class="reply-label">商家回复：</text>
              <text class="reply-time">{{ review.replyTime }}</text>
            </view>
            <view class="reply-content">{{ review.merchantReply }}</view>
          </view>

          <!-- 点赞和评论 -->
          <view class="review-actions">
            <view class="action-item" @click="toggleLike(review)">
              <text class="action-icon">{{ review.liked ? '❤️' : '🤍' }}</text>
              <text class="action-text">{{ review.likeCount || '点赞' }}</text>
            </view>
            <view class="action-item" @click="showCommentInput(review)">
              <text class="action-icon">💬</text>
              <text class="action-text">{{ review.commentCount || '评论' }}</text>
            </view>
          </view>

          <!-- 评价评论列表 -->
          <view class="review-comments" v-if="review.comments && review.comments.length > 0">
            <view
              class="comment-item"
              v-for="comment in review.comments"
              :key="comment.id"
            >
              <text class="comment-user">{{ comment.userName }}</text>
              <text class="comment-text">：{{ comment.content }}</text>
            </view>
          </view>
        </view>
      </scroll-view>

      <!-- 加载更多 -->
      <view class="load-more" v-if="!noMore">
        <uni-load-more :status="loadMoreStatus" />
      </view>

      <!-- 没有更多 -->
      <view class="no-more" v-if="noMore">
        <text>~ 没有更多了 ~</text>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="reviews.length === 0 && !loading">
        <view class="empty-icon">📝</view>
        <view class="empty-text">暂无评价</view>
      </view>
    </view>

    <!-- 评论输入框（悬浮） -->
    <view class="comment-input-modal" v-if="showCommentBox" @click.self="closeCommentInput">
      <view class="comment-input-box" @click.stop>
        <textarea
          class="comment-textarea"
          v-model="commentText"
          placeholder="说点什么..."
          :focus="commentBoxFocused"
          :maxlength="200"
        />
        <view class="comment-actions">
          <button class="cancel-btn" @click="closeCommentInput">取消</button>
          <button class="submit-btn" @click="submitComment">发送</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store'
import { reviewApi } from '@/api'

// Store
const userStore = useUserStore()

// 状态
const targetType = ref('dish') // dish 或 merchant
const targetId = ref('')
const activeFilter = ref('all')
const activeTag = ref('all')
const loading = ref(false)
const noMore = ref(false)
const currentPage = ref(1)
const pageSize = 10

// 评论输入
const showCommentBox = ref(false)
const commentBoxFocused = ref(false)
const commentText = ref('')
const currentReview = ref(null)

// 筛选选项
const filters = ref([
  { label: '全部', value: 'all' },
  { label: '好评', value: 'good' },
  { label: '中评', value: 'medium' },
  { label: '差评', value: 'bad' },
  { label: '有图', value: 'withImage' }
])

// 评分概览数据
const summaryData = ref({
  averageRating: 4.7,
  totalCount: 256
})

// 评分分布
const ratingDistribution = ref([
  { star: 5, percentage: 75 },
  { star: 4, percentage: 15 },
  { star: 3, percentage: 6 },
  { star: 2, percentage: 3 },
  { star: 1, percentage: 1 }
])

// 评价标签
const reviewTags = ref([
  { id: 'all', label: '全部', count: 256 },
  { id: 'tasty', label: '味道好', count: 156 },
  { id: 'portion', label: '分量足', count: 128 },
  { id: 'fast', label: '配送快', count: 98 },
  { id: 'clean', label: '卫生好', count: 87 },
  { id: 'fresh', label: '食材新鲜', count: 65 }
])

// 评价列表
const reviews = ref([])

// 计算属性
const loadMoreStatus = computed(() => {
  if (loading.value) return 'loading'
  if (noMore.value) return 'noMore'
  return 'more'
})

/**
 * 加载评价列表
 */
const loadReviews = async (refresh = false) => {
  if (loading.value) return

  loading.value = true

  try {
    // 构建请求参数
    const params = {
      page: currentPage.value,
      size: pageSize
    }

    // 根据筛选条件添加参数
    if (activeFilter.value !== 'all') {
      if (activeFilter.value === 'good') {
        params.minRating = 4
        params.maxRating = 5
      } else if (activeFilter.value === 'medium') {
        params.minRating = 3
        params.maxRating = 3
      } else if (activeFilter.value === 'bad') {
        params.minRating = 1
        params.maxRating = 2
      } else if (activeFilter.value === 'withImage') {
        params.hasImage = true
      }
    }

    // 根据标签筛选
    if (activeTag.value !== 'all') {
      params.tag = activeTag.value
    }

    // 根据目标类型调用不同的API
    let res
    if (targetType.value === 'dish') {
      res = await reviewApi.getDishReviews(targetId.value, params)
    } else if (targetType.value === 'merchant') {
      res = await reviewApi.getMerchantReviews(targetId.value, params)
    } else {
      throw new Error('不支持的目标类型')
    }

    // 数据映射：将后端返回的数据转换为前端需要的格式
    const mappedReviews = (res.data.list || []).map(review => ({
      id: review.reviewId || review.id,
      user: {
        avatar: review.userAvatar || review.user?.avatar || '',
        name: review.isAnonymous ? '匿名用户' : (review.userName || review.user?.name || '用户***')
      },
      rating: review.rating || 5,
      date: review.createTime || review.createdAt || '',
      content: review.content || '',
      images: review.images || [],
      dishes: (review.dishes || []).map(dish => ({
        id: dish.dishId || dish.id,
        name: dish.dishName || dish.name,
        image: dish.image || dish.coverImage || ''
      })),
      merchantReply: review.merchantReply || '',
      replyTime: review.replyTime || '',
      liked: review.liked || false,
      likeCount: review.likeCount || 0,
      commentCount: review.commentCount || 0,
      comments: (review.comments || []).map(comment => ({
        id: comment.commentId || comment.id,
        userName: comment.userName || comment.user?.name || '用户***',
        content: comment.content || ''
      }))
    }))

    // 更新列表数据
    if (refresh) {
      reviews.value = mappedReviews
    } else {
      reviews.value.push(...mappedReviews)
    }

    // 判断是否还有更多数据
    if (mappedReviews.length < pageSize) {
      noMore.value = true
    }
  } catch (error) {
    console.error('加载评价失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 切换筛选
 */
const switchFilter = (filter) => {
  activeFilter.value = filter
  currentPage.value = 1
  noMore.value = false
  loadReviews(true)
}

/**
 * 选择标签
 */
const selectTag = (tagId) => {
  activeTag.value = tagId
  currentPage.value = 1
  noMore.value = false
  loadReviews(true)
}

/**
 * 上拉加载更多
 */
const loadMore = () => {
  if (loading.value || noMore.value) return

  currentPage.value++
  loadReviews(false)
}

/**
 * 预览图片
 */
const previewImage = (images, index) => {
  uni.previewImage({
    urls: images,
    current: index
  })
}

/**
 * 点赞评价
 */
const toggleLike = async (review) => {
  try {
    // TODO: 调用后端API
    // await reviewApi.like(review.id)

    review.liked = !review.liked
    review.likeCount = review.liked ? (review.likeCount || 0) + 1 : Math.max((review.likeCount || 0) - 1, 0)

    uni.showToast({
      title: review.liked ? '已点赞' : '已取消',
      icon: 'success',
      duration: 1500
    })
  } catch (error) {
    console.error('点赞失败:', error)
  }
}

/**
 * 显示评论输入框
 */
const showCommentInput = (review) => {
  currentReview.value = review
  showCommentBox.value = true
  commentBoxFocused.value = true
}

/**
 * 关闭评论输入框
 */
const closeCommentInput = () => {
  showCommentBox.value = false
  commentText.value = ''
  currentReview.value = null
}

/**
 * 提交评论
 */
const submitComment = async () => {
  if (!commentText.value.trim()) {
    uni.showToast({
      title: '请输入评论内容',
      icon: 'none'
    })
    return
  }

  try {
    // TODO: 调用后端API
    // await reviewApi.addComment(currentReview.value.id, {
    //   content: commentText.value
    // })

    // 模拟添加评论成功
    if (!currentReview.value.comments) {
      currentReview.value.comments = []
    }

    currentReview.value.comments.push({
      id: Date.now(),
      userName: '我',
      content: commentText.value
    })

    currentReview.value.commentCount = (currentReview.value.commentCount || 0) + 1

    uni.showToast({
      title: '评论成功',
      icon: 'success'
    })

    closeCommentInput()
  } catch (error) {
    console.error('评论失败:', error)
    uni.showToast({
      title: '评论失败',
      icon: 'none'
    })
  }
}

// 组件挂载时加载数据
onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options

  if (options.type) {
    targetType.value = options.type
  }

  if (options.id) {
    targetId.value = options.id
  }

  // 加载评价列表
  loadReviews(true)
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.review-list-container {
  min-height: 100vh;
  background-color: $bg-color-base;
  padding-bottom: $spacing-md;
}

.card {
  background-color: $bg-color-white;
  margin-bottom: $spacing-md;
  padding: $spacing-md;
}

/* 评分概览 */
.rating-summary {
  @include flex-between;
  gap: $spacing-xl;
}

.rating-overview {
  @include flex-center-column;
  gap: $spacing-sm;
  text-align: center;
}

.score-value {
  font-size: 72rpx;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.rating-stars {
  @include flex-center;
  gap: 4rpx;

  .star {
    font-size: $font-size-base;
    color: #f5a623;
  }
}

.rating-count {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.rating-distribution {
  flex: 1;
}

.distribution-item {
  @include flex-center;
  gap: $spacing-sm;
  margin-bottom: $spacing-sm;

  &:last-child {
    margin-bottom: 0;
  }
}

.star-label {
  width: 80rpx;
  font-size: $font-size-sm;
  color: $text-color-regular;
}

.progress-bar {
  flex: 1;
  height: 12rpx;
  background-color: $bg-color-base;
  border-radius: 6rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background-color: #f5a623;
  border-radius: 6rpx;
}

.percentage {
  width: 80rpx;
  font-size: $font-size-sm;
  color: $text-color-secondary;
  text-align: right;
}

/* 评价标签 */
.review-tags {
  .tags-scroll {
    white-space: nowrap;
  }

  .tags-list {
    display: flex;
    gap: $spacing-sm;
  }

  .tag-item {
    flex-shrink: 0;
    padding: $spacing-sm $spacing-md;
    font-size: $font-size-sm;
    color: $text-color-regular;
    background-color: $bg-color-base;
    border-radius: $border-radius-round;
    border: 1rpx solid $border-color-base;

    &.active {
      color: $primary-color;
      background-color: rgba(255, 107, 53, 0.1);
      border-color: $primary-color;
      font-weight: $font-weight-medium;
    }
  }
}

/* 筛选Tab */
.filter-tabs {
  background-color: $bg-color-white;
  position: sticky;
  top: 0;
  z-index: $z-index-normal;
}

.tabs-scroll {
  white-space: nowrap;
}

.tabs-list {
  display: flex;
  padding: $spacing-sm $spacing-md;
}

.tab-item {
  flex-shrink: 0;
  padding: $spacing-sm $spacing-md;
  margin-right: $spacing-sm;
  font-size: $font-size-base;
  color: $text-color-regular;
  background-color: $bg-color-base;
  border-radius: $border-radius-round;
  transition: $transition-base;

  &.active {
    color: #fff;
    background-color: $primary-color;
    font-weight: $font-weight-medium;
  }
}

/* 评价列表 */
.review-scroll {
  height: calc(100vh - 400rpx);
}

.review-list {
  .review-item {
    margin-bottom: $spacing-md;
  }
}

.review-user {
  @include flex-between;
  margin-bottom: $spacing-sm;
}

.user-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
}

.user-info {
  flex: 1;
  margin-left: $spacing-sm;
}

.user-name {
  font-size: $font-size-base;
  color: $text-color-primary;
  margin-bottom: $spacing-xs;
}

.review-stars {
  @include flex-center;
  gap: 4rpx;

  .star {
    font-size: $font-size-sm;
    color: #f5a623;
  }
}

.review-date {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.review-content {
  font-size: $font-size-base;
  color: $text-color-regular;
  line-height: $line-height-lg;
  margin-bottom: $spacing-sm;
}

.review-images {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
  margin-bottom: $spacing-sm;
}

.review-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: $border-radius-base;
}

.review-dishes {
  @include flex-center;
  gap: $spacing-sm;
  margin-bottom: $spacing-sm;
}

.dish-item {
  @include flex-center;
  gap: $spacing-xs;
  padding: $spacing-sm;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
}

.dish-image {
  width: 60rpx;
  height: 60rpx;
  border-radius: $border-radius-sm;
}

.dish-name {
  font-size: $font-size-sm;
  color: $text-color-regular;
}

.review-reply {
  padding: $spacing-sm;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  margin-bottom: $spacing-sm;
}

.reply-header {
  @include flex-between;
  margin-bottom: $spacing-xs;
}

.reply-label {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.reply-time {
  font-size: $font-size-xs;
  color: $text-color-placeholder;
}

.reply-content {
  font-size: $font-size-sm;
  color: $text-color-regular;
  line-height: $line-height-lg;
}

.review-actions {
  @include flex-center;
  gap: $spacing-lg;
  padding-top: $spacing-sm;
  border-top: 1rpx solid $border-color-light;
}

.action-item {
  @include flex-center;
  gap: $spacing-xs;
}

.action-icon {
  font-size: 32rpx;
}

.action-text {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.review-comments {
  padding: $spacing-sm;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  margin-top: $spacing-sm;
}

.comment-item {
  font-size: $font-size-sm;
  color: $text-color-regular;
  line-height: $line-height-lg;
  margin-bottom: $spacing-xs;

  &:last-child {
    margin-bottom: 0;
  }
}

.comment-user {
  color: $primary-color;
  font-weight: $font-weight-medium;
}

/* 加载状态 */
.load-more {
  padding: $spacing-lg 0;
  text-align: center;
}

.no-more {
  padding: $spacing-lg 0;
  text-align: center;
  color: $text-color-secondary;
  font-size: $font-size-sm;
}

/* 空状态 */
.empty-state {
  @include flex-center-column;
  gap: $spacing-md;
  padding: 200rpx 0;
}

.empty-icon {
  font-size: 160rpx;
}

.empty-text {
  font-size: $font-size-base;
  color: $text-color-secondary;
}

/* 评论输入框 */
.comment-input-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: $z-index-modal;
  @include flex-center;
}

.comment-input-box {
  width: 600rpx;
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: $spacing-md;
  margin: $spacing-md;
}

.comment-textarea {
  width: 100%;
  min-height: 200rpx;
  padding: $spacing-sm;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  font-size: $font-size-base;
  color: $text-color-primary;
  margin-bottom: $spacing-md;
}

.comment-actions {
  @include flex-center;
  gap: $spacing-md;
}

.cancel-btn,
.submit-btn {
  flex: 1;
  height: 72rpx;
  @include flex-center;
  font-size: $font-size-base;
  border-radius: $border-radius-base;
  border: none;
}

.cancel-btn {
  background-color: $bg-color-base;
  color: $text-color-regular;
}

.submit-btn {
  background-color: $primary-color;
  color: #fff;
}
</style>
