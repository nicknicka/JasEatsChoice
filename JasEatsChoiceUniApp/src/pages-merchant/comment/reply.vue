<template>
  <view class="comment-reply-container">
    <!-- 原评价 -->
    <view class="original-comment">
      <view class="comment-header">
        <image class="user-avatar" :src="comment.user.avatar" mode="aspectFill"></image>
        <view class="user-info">
          <text class="user-name">{{ comment.user.name }}</text>
          <uni-rate :value="comment.rating" size="12" readonly></uni-rate>
        </view>
        <text class="comment-time">{{ comment.time }}</text>
      </view>
      <text class="comment-content">{{ comment.content }}</text>
    </view>

    <!-- 回复编辑器 -->
    <view class="reply-editor">
      <view class="editor-header">
        <text class="title">{{ isEdit ? '编辑回复' : '回复评价' }}</text>
        <text class="tips">（48小时内）</text>
      </view>

      <!-- 快捷回复 -->
      <view class="quick-reply-section">
        <text class="section-label">快捷回复</text>
        <view class="quick-reply-list">
          <view
            class="quick-reply-item"
            v-for="(item, index) in quickReplies"
            :key="index"
            @tap="selectQuickReply(item)"
          >
            {{ item }}
          </view>
        </view>
      </view>

      <!-- 回复内容输入 -->
      <view class="input-section">
        <textarea
          class="reply-input"
          v-model="replyContent"
          placeholder="请输入回复内容，真诚的回复能赢得更多好评哦~"
          maxlength="500"
          :show-confirm-bar="false"
        />
        <view class="input-footer">
          <text class="word-count">{{ replyContent.length }}/500</text>
        </view>
      </view>

      <!-- 图片上传 -->
      <view class="image-section">
        <text class="section-label">添加图片（可选）</text>
        <view class="image-list">
          <view
            class="image-item"
            v-for="(img, index) in replyImages"
            :key="index"
          >
            <image class="upload-image" :src="img" mode="aspectFill"></image>
            <view class="delete-btn" @tap="deleteImage(index)">
              <uni-icons type="closeempty" size="16" color="#fff"></uni-icons>
            </view>
          </view>
          <view
            class="upload-btn"
            v-if="replyImages.length < 3"
            @tap="chooseImage"
          >
            <uni-icons type="camera" size="30" color="#D9D9D9"></uni-icons>
            <text class="upload-text">添加图片</text>
          </view>
        </view>
      </view>

      <!-- 回复提示 -->
      <view class="reply-tips">
        <uni-icons type="info" size="16" color="#FF6B35"></uni-icons>
        <text class="tips-text">优质回复可提升店铺评分，建议针对用户评价内容进行个性化回复</text>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <button class="action-btn cancel" @tap="cancel">取消</button>
      <button class="action-btn primary" @tap="submitReply">提交回复</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { reviewApi } from '@/api/modules/review.js'

const reviewId = ref('')
const merchantId = ref('')

// 是否是编辑模式
const isEdit = ref(false)

// 原评价
const comment = ref({
  id: 1,
  user: {
    id: 1,
    name: '张同学',
    avatar: 'https://via.placeholder.com/60/FF6B35/FFFFFF?text=张'
  },
  rating: 5,
  time: '2小时前',
  content: '味道非常不错，宫保鸡丁很正宗，分量也足，下次还会再来的！'
})

// 回复内容
const replyContent = ref('')

// 回复图片
const replyImages = ref([])

// 快捷回复
const quickReplies = ref([
  '感谢您的好评！我们会继续努力提供更优质的服务和菜品。',
  '感谢您的支持与认可，期待您的下次光临！',
  '非常感谢您的详细评价，我们会继续努力！',
  '感谢您的认可，这是我们前进的最大动力！',
  '感谢您的反馈，我们会持续改进，为您提供更好的体验。'
])

onMounted(async () => {
  merchantId.value = uni.getStorageSync('merchantId') || ''

  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options

  reviewId.value = options.id || ''
  isEdit.value = options.edit === 'true'

  // REVIEW-005: 加载评价详情
  if (reviewId.value) {
    await loadReviewDetail()
  }

  // REVIEW-006: 如果是编辑模式，加载已有回复
  if (isEdit.value) {
    await loadReply()
  }
})

/**
 * REVIEW-005: 加载评价详情
 */
const loadReviewDetail = async () => {
  try {
    const res = await reviewApi.getDetail(reviewId.value)

    if (res.code === 200 && res.data) {
      const data = res.data
      comment.value = {
        id: data.id,
        user: {
          id: data.userId,
          name: data.isAnonymous ? '匿名用户' : (data.userName || '用户***'),
          avatar: data.userAvatar || 'https://via.placeholder.com/60'
        },
        rating: data.rating || 5,
        time: formatTime(data.createdAt),
        content: data.content || ''
      }
    }
  } catch (error) {
    console.error('加载评价详情失败:', error)
  }
}

/**
 * REVIEW-006: 加载已有回复
 */
const loadReply = async () => {
  if (!reviewId.value) return

  try {
    // REVIEW-006: 调用API获取评价详情（包含回复信息）
    const res = await reviewApi.getDetail(reviewId.value)

    if (res.code === 200 && res.data && res.data.reply) {
      replyContent.value = res.data.reply.content || ''
      replyImages.value = res.data.reply.images || []
    }
  } catch (error) {
    console.error('加载回复失败:', error)
  }
}

/**
 * 格式化时间
 */
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 86400000) return '今天'
  if (diff < 172800000) return '昨天'
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`

  return `${date.getMonth() + 1}-${date.getDate()}`
}

/**
 * 选择快捷回复
 */
const selectQuickReply = (text) => {
  replyContent.value = text
}

/**
 * 选择图片
 */
const chooseImage = () => {
  const remainCount = 3 - replyImages.value.length
  uni.chooseImage({
    count: remainCount,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      replyImages.value.push(...res.tempFilePaths)
    }
  })
}

/**
 * 删除图片
 */
const deleteImage = (index) => {
  replyImages.value.splice(index, 1)
}

/**
 * 取消回复
 */
const cancel = () => {
  if (replyContent.value || replyImages.value.length > 0) {
    uni.showModal({
      title: '提示',
      content: '确定放弃编辑吗？已输入的内容将不会保存。',
      success: (res) => {
        if (res.confirm) {
          uni.navigateBack()
        }
      }
    })
  } else {
    uni.navigateBack()
  }
}

/**
 * 提交回复
 */
const submitReply = () => {
  if (!replyContent.value.trim()) {
    uni.showToast({
      title: '请输入回复内容',
      icon: 'none'
    })
    return
  }

  if (replyContent.value.length < 10) {
    uni.showToast({
      title: '回复内容至少10个字',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '确认提交',
    content: '确认提交回复吗？',
    success: (res) => {
      if (res.confirm) {
        saveReply()
      }
    }
  })
}

/**
 * REVIEW-007: 保存回复
 */
const saveReply = async () => {
  try {
    uni.showLoading({ title: '提交中...' })

    // REVIEW-007: 准备回复数据
    const data = {
      merchantId: merchantId.value,
      content: replyContent.value,
      images: replyImages.value
    }

    // REVIEW-007: 调用API保存回复
    const res = await reviewApi.reply(reviewId.value, data)

    uni.hideLoading()

    if (res.code === 200) {
      uni.showToast({
        title: isEdit.value ? '修改成功' : '回复成功',
        icon: 'success'
      })

      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    } else {
      throw new Error(res.message || '提交失败')
    }
  } catch (error) {
    console.error('保存回复失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: error.message || '提交失败',
      icon: 'none'
    })
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.comment-reply-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 40rpx;
}

/* 原评价 */
.original-comment {
  background: #fff;
  padding: 30rpx;
  margin: 20rpx;
  border-radius: 16rpx;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 15rpx;
  margin-bottom: 15rpx;
}

.user-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.user-name {
  font-size: 26rpx;
  font-weight: bold;
  color: #333;
}

.comment-time {
  font-size: 22rpx;
  color: #999;
}

.comment-content {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
}

/* 回复编辑器 */
.reply-editor {
  background: #fff;
  padding: 30rpx;
  margin: 0 20rpx 20rpx;
  border-radius: 16rpx;
}

.editor-header {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 20rpx;
  padding-bottom: 15rpx;
  border-bottom: 1rpx solid #eee;
}

.title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.tips {
  font-size: 24rpx;
  color: #999;
}

/* 快捷回复 */
.quick-reply-section {
  margin-bottom: 25rpx;
}

.section-label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 15rpx;
  font-weight: 500;
}

.quick-reply-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.quick-reply-item {
  padding: 10rpx 20rpx;
  background: #F5F5F5;
  border-radius: 20rpx;
  font-size: 24rpx;
  color: #666;
  border: 1rpx solid transparent;

  &:active {
    background: rgba(255, 107, 53, 0.1);
    color: #FF6B35;
    border-color: #FF6B35;
  }
}

/* 输入区域 */
.input-section {
  margin-bottom: 25rpx;
}

.reply-input {
  width: 100%;
  min-height: 200rpx;
  padding: 15rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
}

.input-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 10rpx;
}

.word-count {
  font-size: 24rpx;
  color: #999;
}

/* 图片上传 */
.image-section {
  margin-bottom: 25rpx;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 15rpx;
}

.image-item {
  width: 150rpx;
  height: 150rpx;
  position: relative;
  border-radius: 12rpx;
  overflow: hidden;
}

.upload-image {
  width: 100%;
  height: 100%;
}

.delete-btn {
  position: absolute;
  top: 5rpx;
  right: 5rpx;
  width: 40rpx;
  height: 40rpx;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  @include flex-center;
}

.upload-btn {
  width: 150rpx;
  height: 150rpx;
  border: 2rpx dashed #D9D9D9;
  border-radius: 12rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 10rpx;
}

.upload-text {
  font-size: 24rpx;
  color: #999;
}

/* 回复提示 */
.reply-tips {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 15rpx;
  background: #FFF7E6;
  border-radius: 8rpx;
}

.tips-text {
  flex: 1;
  font-size: 24rpx;
  color: #FF6B35;
  line-height: 1.5;
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
  @include flex-center;

  &.cancel {
    background: #fff;
    color: #666;
  }

  &.primary {
    background: #FF6B35;
    color: #fff;
  }
}
</style>
