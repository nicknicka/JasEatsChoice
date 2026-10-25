<template>
  <view class="feedback-container">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <view class="nav-title">意见反馈</view>
      <view class="nav-action" @click="submitFeedback">
        <text class="action-text">提交</text>
      </view>
    </view>

    <scroll-view class="content-scroll" scroll-y>
      <!-- 反馈类型 -->
      <view class="section-card">
        <view class="section-title">反馈类型</view>
        <view class="type-grid">
          <view
            class="type-item"
            v-for="type in feedbackTypes"
            :key="type.value"
            :class="{ active: formData.type === type.value }"
            @click="formData.type = type.value"
          >
            <text class="type-icon">{{ type.icon }}</text>
            <text class="type-text">{{ type.label }}</text>
          </view>
        </view>
      </view>

      <!-- 反馈内容 -->
      <view class="section-card">
        <view class="section-title">问题描述</view>
        <textarea
          class="feedback-textarea"
          v-model="formData.content"
          placeholder="请详细描述您遇到的问题或建议..."
          maxlength="500"
          :placeholder-style="'color: #999'"
        />
        <view class="char-count">{{ formData.content.length }}/500</view>
      </view>

      <!-- 图片上传 -->
      <view class="section-card">
        <view class="section-title">上传图片（选填，最多3张）</view>
        <view class="image-upload-grid">
          <view
            class="image-item"
            v-for="(image, index) in formData.images"
            :key="index"
          >
            <image class="upload-image" :src="image" mode="aspectFill" />
            <view class="image-delete" @click="deleteImage(index)">
              <text class="delete-icon">×</text>
            </view>
          </view>

          <view
            class="upload-btn"
            v-if="formData.images.length < 3"
            @click="chooseImage"
          >
            <text class="upload-icon">📷</text>
            <text class="upload-text">添加图片</text>
          </view>
        </view>
      </view>

      <!-- 联系方式 -->
      <view class="section-card">
        <view class="section-title">联系方式（选填）</view>
        <input
          class="contact-input"
          v-model="formData.contact"
          placeholder="手机号或邮箱，方便我们联系您"
          :placeholder-style="'color: #999'"
        />
      </view>

      <!-- 历史反馈 -->
      <view class="section-card" v-if="historyList.length > 0">
        <view class="section-title">历史反馈</view>
        <view class="history-list">
          <view
            class="history-item"
            v-for="item in historyList"
            :key="item.id"
            @click="viewHistory(item)"
          >
            <view class="history-header">
              <text class="history-type">{{ getTypeName(item.type) }}</text>
              <view class="history-status" :class="'status-' + item.status">
                {{ getStatusName(item.status) }}
              </view>
            </view>
            <text class="history-content">{{ item.content }}</text>
            <text class="history-time">{{ formatTime(item.createTime) }}</text>
          </view>
        </view>
      </view>

      <!-- 底部提示 -->
      <view class="bottom-tip">
        <text class="tip-text">感谢您的反馈，我们会认真处理每一条意见</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { feedbackApi } from '@/api'

// 表单数据
const formData = ref({
  type: 'bug',
  content: '',
  images: [],
  contact: ''
})

// 反馈类型
const feedbackTypes = [
  { label: '功能异常', value: 'bug', icon: '🐛' },
  { label: '功能建议', value: 'feature', icon: '💡' },
  { label: '体验问题', value: 'experience', icon: '😕' },
  { label: '其他', value: 'other', icon: '📝' }
]

// 历史反馈列表
const historyList = ref([])

// 组件挂载
onMounted(() => {
  loadHistory()
})

/**
 * 加载历史反馈
 */
const loadHistory = async () => {
  try {
    // 模拟数据
    historyList.value = [
      {
        id: 1,
        type: 'bug',
        content: '订单支付时偶尔会出现卡顿',
        status: 1,
        createTime: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString()
      },
      {
        id: 2,
        type: 'feature',
        content: '希望增加菜品收藏功能',
        status: 0,
        createTime: new Date(Date.now() - 1000 * 60 * 60 * 24 * 3).toISOString()
      }
    ]
  } catch (error) {
    console.error('加载历史反馈失败:', error)
  }
}

/**
 * 选择图片
 */
const chooseImage = () => {
  uni.chooseImage({
    count: 3 - formData.value.images.length,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      formData.value.images.push(...res.tempFilePaths)
    }
  })
}

/**
 * 删除图片
 */
const deleteImage = (index) => {
  formData.value.images.splice(index, 1)
}

/**
 * 提交反馈
 */
const submitFeedback = async () => {
  // 表单验证
  if (!formData.value.content.trim()) {
    uni.showToast({
      title: '请输入问题描述',
      icon: 'none'
    })
    return
  }

  if (formData.value.content.length < 10) {
    uni.showToast({
      title: '描述至少10个字',
      icon: 'none'
    })
    return
  }

  try {
    uni.showLoading({
      title: '提交中...'
    })

    // TODO: 调用提交反馈接口
    // await feedbackApi.submit(formData.value)

    setTimeout(() => {
      uni.hideLoading()

      uni.showToast({
        title: '提交成功',
        icon: 'success'
      })

      // 清空表单
      formData.value = {
        type: 'bug',
        content: '',
        images: [],
        contact: ''
      }

      // 刷新历史记录
      loadHistory()

      // 延迟返回
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }, 1000)
  } catch (error) {
    uni.hideLoading()
    uni.showToast({
      title: error.message || '提交失败',
      icon: 'none'
    })
  }
}

/**
 * 查看历史反馈详情
 */
const viewHistory = (item) => {
  uni.showModal({
    title: '反馈详情',
    content: `${item.content}\n\n状态：${getStatusName(item.status)}`,
    showCancel: false
  })
}

/**
 * 获取类型名称
 */
const getTypeName = (type) => {
  const typeMap = {
    'bug': '功能异常',
    'feature': '功能建议',
    'experience': '体验问题',
    'other': '其他'
  }
  return typeMap[type] || '未知'
}

/**
 * 获取状态名称
 */
const getStatusName = (status) => {
  const statusMap = {
    0: '待处理',
    1: '处理中',
    2: '已完成'
  }
  return statusMap[status] || '未知'
}

/**
 * 格式化时间
 */
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`
}

/**
 * 返回上一页
 */
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.feedback-container {
  min-height: 100vh;
  background-color: $bg-color-base;
}

/* 导航栏 */
.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 88rpx;
  background-color: $bg-color-white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 $spacing-md;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.nav-back {
  width: 88rpx;
  height: 88rpx;
  @include flex-center;
}

.back-icon {
  font-size: 48rpx;
  color: $text-color-primary;
  font-weight: bold;
}

.nav-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.nav-action {
  width: 88rpx;
  height: 88rpx;
  @include flex-center;
}

.action-text {
  font-size: $font-size-base;
  color: $primary-color;
  font-weight: $font-weight-bold;
}

/* 内容滚动 */
.content-scroll {
  height: 100vh;
  padding-top: 108rpx;
  padding-bottom: env(safe-area-inset-bottom);
}

/* 区块卡片 */
.section-card {
  background-color: $bg-color-white;
  margin: 0 $spacing-md $spacing-md;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  box-shadow: $box-shadow-sm;
}

.section-title {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
  margin-bottom: $spacing-md;
}

/* 反馈类型 */
.type-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-md;
}

.type-item {
  @include flex-center-column;
  gap: $spacing-sm;
  padding: $spacing-lg;
  border: 2rpx solid $border-color-base;
  border-radius: $border-radius-lg;
  background-color: $bg-color-base;
  transition: all 0.3s;

  &.active {
    border-color: $primary-color;
    background-color: rgba($primary-color, 0.1);
  }
}

.type-icon {
  font-size: 48rpx;
}

.type-text {
  font-size: $font-size-sm;
  color: $text-color-primary;
}

/* 反馈内容 */
.feedback-textarea {
  width: 100%;
  min-height: 240rpx;
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  font-size: $font-size-base;
  color: $text-color-primary;
  border: none;
  outline: none;
}

.char-count {
  text-align: right;
  font-size: $font-size-xs;
  color: $text-color-secondary;
  margin-top: $spacing-xs;
}

/* 图片上传 */
.image-upload-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-sm;
}

.image-item {
  position: relative;
  width: 100%;
  padding-bottom: 100%;
  border-radius: $border-radius-base;
  overflow: hidden;
}

.upload-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.image-delete {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 48rpx;
  height: 48rpx;
  @include flex-center;
  background-color: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
}

.delete-icon {
  font-size: 36rpx;
  color: #fff;
  font-weight: bold;
}

.upload-btn {
  width: 100%;
  padding-bottom: 100%;
  position: relative;
  border: 2rpx dashed $border-color-base;
  border-radius: $border-radius-base;
  @include flex-center-column;
  gap: $spacing-xs;
  background-color: $bg-color-base;
}

.upload-icon,
.upload-text {
  position: absolute;
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.upload-icon {
  font-size: 48rpx;
  top: 30%;
}

.upload-text {
  bottom: 25%;
}

/* 联系方式 */
.contact-input {
  width: 100%;
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  font-size: $font-size-base;
  color: $text-color-primary;
  border: none;
  outline: none;
}

/* 历史反馈 */
.history-list {
  .history-item {
    padding: $spacing-md 0;
    border-bottom: 1rpx solid $border-color-lighter;

    &:last-child {
      border-bottom: none;
    }

    &:active {
      background-color: $bg-color-base;
    }
  }
}

.history-header {
  @include flex-between;
  margin-bottom: $spacing-sm;
}

.history-type {
  font-size: $font-size-sm;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
}

.history-status {
  font-size: $font-size-xs;
  padding: 4rpx 12rpx;
  border-radius: $border-radius-round;

  &.status-0 {
    background-color: $warning-color;
    color: #fff;
  }

  &.status-1 {
    background-color: $primary-color;
    color: #fff;
  }

  &.status-2 {
    background-color: $success-color;
    color: #fff;
  }
}

.history-content {
  display: block;
  font-size: $font-size-sm;
  color: $text-color-secondary;
  margin-bottom: $spacing-xs;
  line-height: 1.5;
}

.history-time {
  display: block;
  font-size: $font-size-xs;
  color: $text-color-placeholder;
}

/* 底部提示 */
.bottom-tip {
  padding: $spacing-xl;
  text-align: center;
}

.tip-text {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}
</style>
