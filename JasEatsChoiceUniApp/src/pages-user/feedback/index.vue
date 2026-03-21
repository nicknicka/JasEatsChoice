<template>
  <view class="feedback-container">
    <!-- 反馈类型 -->
    <view class="type-section">
      <view class="section-title">反馈类型</view>
      <view class="type-list">
        <view
          class="type-item"
          :class="{ active: feedbackType === item.value }"
          v-for="item in typeList"
          :key="item.value"
          @click="selectType(item.value)"
        >
          <text class="type-icon">{{ item.icon }}</text>
          <text class="type-name">{{ item.label }}</text>
          <view class="type-check" v-if="feedbackType === item.value">✓</view>
        </view>
      </view>
    </view>

    <!-- 反馈内容 -->
    <view class="content-section">
      <view class="section-title">问题描述</view>
      <textarea
        class="feedback-textarea"
        v-model="feedbackContent"
        placeholder="请详细描述您遇到的问题或建议，我们会认真对待每一条反馈"
        maxlength="500"
        :show-confirm-bar="false"
      />
      <view class="char-count">{{ (feedbackContent || '').length }}/500</view>
    </view>

    <!-- 图片上传 -->
    <view class="image-section">
      <view class="section-title">
        <text>图片上传</text>
        <text class="section-tip">（选填，最多3张）</text>
      </view>

      <view class="image-list">
        <!-- 已上传的图片 -->
        <view
          class="image-item"
          v-for="(image, index) in imageList"
          :key="index"
        >
          <image class="image-preview" :src="image" mode="aspectFill" />
          <view class="image-delete" @click="deleteImage(index)">
            <text class="delete-icon">×</text>
          </view>
        </view>

        <!-- 上传按钮 -->
        <view
          class="image-upload"
          v-if="imageList.length < 3"
          @click="chooseImage"
        >
          <text class="upload-icon">+</text>
          <text class="upload-text">添加图片</text>
        </view>
      </view>
    </view>

    <!-- 联系方式 -->
    <view class="contact-section">
      <view class="section-title">联系方式</view>
      <view class="contact-input">
        <text class="input-icon">📱</text>
        <input
          class="input-field"
          type="text"
          v-model="contactInfo"
          placeholder="请输入手机号或邮箱（选填）"
        />
      </view>
      <view class="contact-tip">
        留下联系方式，方便我们跟进处理结果
      </view>
    </view>

    <!-- 历史反馈 -->
    <view class="history-section">
      <view class="section-header">
        <text class="section-title">历史反馈</text>
        <text class="section-count" v-if="historyList.length > 0">共{{ historyList.length }}条</text>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="historyList.length === 0">
        <Empty
          icon="📝"
          text="还没有反馈记录"
          description="您的每一条反馈都能帮助我们改进"
        />
      </view>

      <!-- 历史列表 -->
      <scroll-view class="history-list" scroll-y v-else>
        <view
          class="history-item"
          v-for="item in historyList"
          :key="item.id"
          @click="viewHistoryDetail(item)"
        >
          <view class="history-header">
            <text class="history-type">{{ item.typeText }}</text>
            <view class="history-status" :class="item.status">
              <text class="status-text">{{ item.statusText }}</text>
            </view>
          </view>

          <text class="history-content">{{ item.content }}</text>

          <view class="history-footer">
            <text class="history-time">{{ item.time }}</text>
            <text class="history-reply" v-if="item.reply">已回复</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 底部按钮 -->
    <view class="bottom-bar">
      <button class="submit-btn" @click="submitFeedback" :disabled="!canSubmit">
        提交反馈
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { formatRelativeTime } from '@/utils/helper'
import Empty from '@/components/common/Empty.vue'
import { feedbackApi } from '@/api'

// 用户信息store
const userStore = useUserStore()

// 反馈类型
const feedbackType = ref('')

// 反馈内容
const feedbackContent = ref('')

// 图片列表
const imageList = ref([])

// 联系方式
const contactInfo = ref('')

// 历史反馈列表
const historyList = ref([])

// 反馈类型列表
const typeList = [
  { value: 'bug', label: '功能异常', icon: '🐛' },
  { value: 'feature', label: '功能建议', icon: '💡' },
  { value: 'ui', label: '界面问题', icon: '🎨' },
  { value: 'performance', label: '性能问题', icon: '⚡' },
  { value: 'order', label: '订单问题', icon: '📦' },
  { value: 'payment', label: '支付问题', icon: '💰' },
  { value: 'other', label: '其他问题', icon: '📝' }
]

// 是否可以提交
const canSubmit = computed(() => {
  return feedbackType.value && feedbackContent.value.trim().length > 0
})

/**
 * 选择反馈类型
 */
const selectType = (type) => {
  feedbackType.value = type
}

/**
 * 选择图片
 */
const chooseImage = () => {
  const maxCount = 3 - imageList.value.length

  uni.chooseImage({
    count: maxCount,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      imageList.value.push(...res.tempFilePaths)
    }
  })
}

/**
 * 删除图片
 */
const deleteImage = (index) => {
  imageList.value.splice(index, 1)
}

/**
 * 提交反馈
 */
const submitFeedback = async () => {
  if (!userStore.isLogin) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    return
  }

  if (!feedbackType.value) {
    uni.showToast({
      title: '请选择反馈类型',
      icon: 'none'
    })
    return
  }

  if (!feedbackContent.value.trim()) {
    uni.showToast({
      title: '请输入反馈内容',
      icon: 'none'
    })
    return
  }

  uni.showLoading({
    title: '提交中...'
  })

  try {
    // U-027: 实现图片上传API
    const uploadedImages = []
    for (const imagePath of imageList.value) {
      try {
        // 导入上传工具
        const { upload } = await import('@/utils/request')

        // 上传图片到服务器
        const uploadRes = await upload('/api/upload/image', imagePath, {
          type: 'feedback'
        })

        if (uploadRes && uploadRes.url) {
          uploadedImages.push(uploadRes.url)
        }
      } catch (error) {
        console.error('图片上传失败:', error)
        // 继续上传其他图片，不中断流程
      }
    }

    const userId = userStore.userInfo?.userId || userStore.userInfo?.id

    // 提交反馈
    await feedbackApi.submit({
      userId,
      type: feedbackType.value,
      content: feedbackContent.value,
      images: uploadedImages,
      contact: contactInfo.value,
      deviceInfo: {
        platform: uni.getSystemInfoSync().platform,
        system: uni.getSystemInfoSync().system
      }
    })

    uni.hideLoading()

    uni.showToast({
      title: '提交成功',
      icon: 'success'
    })

    // 重置表单
    feedbackType.value = ''
    feedbackContent.value = ''
    imageList.value = []
    contactInfo.value = ''

    // 重新加载历史反馈
    setTimeout(() => {
      loadHistoryFeedback()
    }, 1500)
  } catch (error) {
    console.error('提交反馈失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: '提交失败，请重试',
      icon: 'none'
    })
  }
}

/**
 * 加载历史反馈
 */
const loadHistoryFeedback = async () => {
  if (!userStore.isLogin) {
    return
  }

  try {
    const userId = userStore.userInfo?.userId || userStore.userInfo?.id
    const res = await feedbackApi.getList({
      userId,
      page: 1,
      size: 10
    })

    const list = res.list || res.data?.list || []
    historyList.value = list.map(item => {
      const typeConfig = typeList.find(t => t.value === item.type)
      return {
        id: item.feedbackId || item.id,
        type: item.type,
        typeText: typeConfig ? typeConfig.label : '其他',
        content: item.content,
        status: item.status,
        statusText: getStatusText(item.status),
        time: formatTime(item.createTime || item.createdAt),
        reply: item.reply || null
      }
    })
  } catch (error) {
    console.error('加载历史反馈失败:', error)

    // 使用空列表
    historyList.value = []
  }
}

/**
 * 获取状态文本
 */
function getStatusText(status) {
  const statusMap = {
    pending: '待处理',
    processing: '处理中',
    resolved: '已解决',
    closed: '已关闭'
  }
  return statusMap[status] || '未知'
}

/**
 * 查看历史反馈详情
 */
const viewHistoryDetail = (item) => {
  uni.navigateTo({
    url: `/pages/feedback/detail/index?id=${item.id}`
  })
}

// 组件挂载
onMounted(() => {
  loadHistoryFeedback()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.feedback-container {
  min-height: 100vh;
  background-color: $bg-color-base;
  padding-bottom: 120rpx;
}

/* 反馈类型 */
.type-section {
  background-color: $bg-color-white;
  margin: $spacing-md;
  padding: $spacing-lg;
  border-radius: $border-radius-lg;
  box-shadow: $box-shadow-sm;
}

.section-title {
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
  margin-bottom: $spacing-md;
}

.section-tip {
  font-size: $font-size-sm;
  color: $text-color-secondary;
  font-weight: normal;
}

.type-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-sm;
}

.type-item {
  position: relative;
  @include flex-center-column;
  gap: $spacing-xs;
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  border: 2rpx solid transparent;
  transition: all 0.3s;

  &.active {
    background-color: rgba(255, 107, 53, 0.05);
    border-color: $primary-color;
  }

  &:active {
    transform: scale(0.95);
  }
}

.type-icon {
  font-size: $font-size-xl;
}

.type-name {
  font-size: $font-size-sm;
  color: $text-color-primary;
}

.type-check {
  position: absolute;
  top: $spacing-xs;
  right: $spacing-xs;
  width: 32rpx;
  height: 32rpx;
  @include flex-center;
  background-color: $primary-color;
  color: #fff;
  border-radius: 50%;
  font-size: $font-size-xs;
  font-weight: $font-weight-bold;
}

/* 反馈内容 */
.content-section {
  background-color: $bg-color-white;
  margin: $spacing-md;
  padding: $spacing-lg;
  border-radius: $border-radius-lg;
  box-shadow: $box-shadow-sm;
}

.feedback-textarea {
  width: 100%;
  min-height: 240rpx;
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  font-size: $font-size-base;
  color: $text-color-primary;
  line-height: $line-height-lg;
}

.char-count {
  text-align: right;
  font-size: $font-size-xs;
  color: $text-color-placeholder;
  margin-top: $spacing-sm;
}

/* 图片上传 */
.image-section {
  background-color: $bg-color-white;
  margin: $spacing-md;
  padding: $spacing-lg;
  border-radius: $border-radius-lg;
  box-shadow: $box-shadow-sm;
}

.image-list {
  @include flex-center;
  flex-wrap: wrap;
  gap: $spacing-sm;
}

.image-item {
  position: relative;
  width: 200rpx;
  height: 200rpx;
}

.image-preview {
  width: 100%;
  height: 100%;
  border-radius: $border-radius-base;
}

.image-delete {
  position: absolute;
  top: -$spacing-sm;
  right: -$spacing-sm;
  width: 48rpx;
  height: 48rpx;
  @include flex-center;
  background-color: $danger-color;
  border-radius: 50%;
  box-shadow: $box-shadow-sm;

  &:active {
    opacity: 0.8;
  }
}

.delete-icon {
  font-size: $font-size-xl;
  color: #fff;
  line-height: 1;
}

.image-upload {
  width: 200rpx;
  height: 200rpx;
  @include flex-center-column;
  gap: $spacing-xs;
  background-color: $bg-color-base;
  border: 2rpx dashed $border-color;
  border-radius: $border-radius-base;

  &:active {
    background-color: rgba(0, 0, 0, 0.02);
  }
}

.upload-icon {
  font-size: 64rpx;
  color: $text-color-placeholder;
}

.upload-text {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

/* 联系方式 */
.contact-section {
  background-color: $bg-color-white;
  margin: $spacing-md;
  padding: $spacing-lg;
  border-radius: $border-radius-lg;
  box-shadow: $box-shadow-sm;
}

.contact-input {
  @include flex-center;
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  margin-bottom: $spacing-sm;
}

.input-icon {
  font-size: $font-size-xl;
  margin-right: $spacing-sm;
}

.input-field {
  flex: 1;
  font-size: $font-size-base;
  color: $text-color-primary;
}

.contact-tip {
  font-size: $font-size-sm;
  color: $text-color-secondary;
  padding-left: $spacing-sm;
}

/* 历史反馈 */
.history-section {
  background-color: $bg-color-white;
  margin: $spacing-md;
  padding: $spacing-lg;
  border-radius: $border-radius-lg;
  box-shadow: $box-shadow-sm;
}

.section-header {
  @include flex-between;
  align-items: center;
  margin-bottom: $spacing-md;
}

.section-count {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.empty-state {
  padding: 80rpx 0;
}

.history-list {
  max-height: 600rpx;
}

.history-item {
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  margin-bottom: $spacing-sm;

  &:last-child {
    margin-bottom: 0;
  }

  &:active {
    background-color: rgba(0, 0, 0, 0.02);
  }
}

.history-header {
  @include flex-between;
  align-items: center;
  margin-bottom: $spacing-sm;
}

.history-type {
  font-size: $font-size-sm;
  color: $primary-color;
  font-weight: $font-weight-medium;
}

.history-status {
  padding: 4rpx 12rpx;
  border-radius: $border-radius-round;
  font-size: $font-size-xs;

  &.pending {
    background-color: rgba(255, 107, 53, 0.1);
    color: $primary-color;
  }

  &.processing {
    background-color: rgba(33, 150, 243, 0.1);
    color: #2196F3;
  }

  &.resolved {
    background-color: rgba(76, 175, 80, 0.1);
    color: $success-color;
  }

  &.closed {
    background-color: rgba(158, 158, 158, 0.1);
    color: $text-color-placeholder;
  }
}

.status-text {
  font-size: $font-size-xs;
}

.history-content {
  font-size: $font-size-sm;
  color: $text-color-primary;
  line-height: $line-height-lg;
  @include text-ellipsis-multi(2);
  margin-bottom: $spacing-sm;
  display: block;
}

.history-footer {
  @include flex-between;
  align-items: center;
}

.history-time {
  font-size: $font-size-xs;
  color: $text-color-placeholder;
}

.history-reply {
  font-size: $font-size-xs;
  color: $success-color;
}

/* 底部按钮 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: $bg-color-white;
  padding: $spacing-md;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
  z-index: $z-index-fixed;
  @include safe-area-bottom;
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  @include flex-center;
  background: linear-gradient(135deg, $primary-color, #FF8F61);
  color: #fff;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  border-radius: $border-radius-round;
  border: none;

  &:active {
    transform: scale(0.98);
  }

  &[disabled] {
    opacity: 0.5;
  }
}
</style>
