<template>
  <view class="dish-recognition">
    <!-- 固定内容：上传区域 -->
    <view class="fixed-content">
      <!-- 上传区域 -->
      <view class="upload-section">
        <view class="section-title">📷 上传菜品图片</view>

        <view
          class="upload-area"
          :class="{ 'has-image': selectedImage }"
          @click="chooseImage"
        >
          <view v-if="!selectedImage" class="upload-placeholder">
            <text class="upload-icon">📷</text>
            <text class="upload-text">点击上传菜品图片</text>
            <text class="upload-hint">支持 JPG、PNG 格式</text>
          </view>

          <view v-else class="image-preview">
            <image class="preview-image" :src="selectedImage" mode="aspectFill" />
            <view class="image-overlay">
              <text class="delete-btn" @click.stop="clearImage">🗑️ 删除</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 识别按钮 -->
      <view class="action-buttons">
        <button
          class="recognize-btn"
          :class="{ loading: isRecognizing }"
          :disabled="!selectedImage || isRecognizing"
          @click="recognizeDish"
        >
          <text v-if="isRecognizing">识别中...</text>
          <text v-else>🔍 开始识别菜品</text>
        </button>

        <button
          v-if="recognitionResult"
          class="re-recognize-btn"
          :disabled="isRecognizing"
          @click="recognizeDish"
        >
          🔄 重新识别
        </button>
      </view>
    </view>

    <!-- 可滚动内容：识别结果 -->
    <scroll-view v-if="recognitionResult" class="scrollable-content" scroll-y>
      <view class="result-section">
        <view class="section-title">✨ 识别结果</view>
        <!-- 菜品名称 -->
        <view class="result-card main-card">
          <view class="card-label">菜品名称</view>
          <view class="card-value">{{ recognitionResult.name }}</view>
        </view>

        <!-- 卡路里 -->
        <view class="result-card calories-card">
          <view class="card-label">🔥 卡路里</view>
          <view class="card-value highlight">{{ recognitionResult.calories }} kcal</view>
        </view>

        <!-- 难度 -->
        <view class="result-card">
          <view class="card-label">👨‍🍳 难度</view>
          <view class="card-value">{{ recognitionResult.difficulty }}</view>
        </view>

        <!-- 烹饪时间 -->
        <view class="result-card">
          <view class="card-label">⏱️ 烹饪时间</view>
          <view class="card-value">{{ recognitionResult.preparationTime }}</view>
        </view>

        <!-- 营养成分 -->
        <view class="result-card full-width">
          <view class="card-label">📊 营养成分</view>
          <view class="nutrition-list">
            <view class="nutrition-item">
              <view class="nutrition-info">
                <text class="nutrition-icon">💪</text>
                <text class="nutrition-name">蛋白质</text>
              </view>
              <view class="nutrition-bar">
                <view
                  class="nutrition-fill protein"
                  :style="{ width: recognitionResult.nutrition.protein + '%' }"
                ></view>
              </view>
              <text class="nutrition-value">{{ recognitionResult.nutrition.proteinValue }}g</text>
            </view>

            <view class="nutrition-item">
              <view class="nutrition-info">
                <text class="nutrition-icon">🧈</text>
                <text class="nutrition-name">脂肪</text>
              </view>
              <view class="nutrition-bar">
                <view
                  class="nutrition-fill fat"
                  :style="{ width: recognitionResult.nutrition.fat + '%' }"
                ></view>
              </view>
              <text class="nutrition-value">{{ recognitionResult.nutrition.fatValue }}g</text>
            </view>

            <view class="nutrition-item">
              <view class="nutrition-info">
                <text class="nutrition-icon">🍞</text>
                <text class="nutrition-name">碳水</text>
              </view>
              <view class="nutrition-bar">
                <view
                  class="nutrition-fill carb"
                  :style="{ width: recognitionResult.nutrition.carb + '%' }"
                ></view>
              </view>
              <text class="nutrition-value">{{ recognitionResult.nutrition.carbValue }}g</text>
            </view>
          </view>
        </view>

        <!-- 操作按钮 -->
        <view class="result-actions">
          <button class="action-btn primary" @click="addToRecipe">
            📖 添加到食谱
          </button>
          <button class="action-btn secondary" @click="shareResult">
            📤 分享结果
          </button>
        </view>
      </view>
    </scroll-view>

    <!-- 使用说明 -->
    <scroll-view v-if="!recognitionResult" class="scrollable-content" scroll-y>
      <view class="tips-section">
        <view class="section-title">💡 使用说明</view>
        <view class="tips-list">
          <view class="tip-item">
            <text class="tip-icon">1️⃣</text>
            <text class="tip-text">上传清晰的菜品照片</text>
          </view>
          <view class="tip-item">
            <text class="tip-icon">2️⃣</text>
            <text class="tip-text">确保光线充足，菜品完整</text>
          </view>
          <view class="tip-item">
            <text class="tip-icon">3️⃣</text>
            <text class="tip-text">AI将自动识别并分析营养成分</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

// 选中的图片
const selectedImage = ref('')
const isRecognizing = ref(false)
const recognitionResult = ref(null)

/**
 * 选择图片
 */
const chooseImage = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      selectedImage.value = res.tempFilePaths[0]
      recognitionResult.value = null
    }
  })
}

/**
 * 清除图片
 */
const clearImage = () => {
  selectedImage.value = ''
  recognitionResult.value = null
}

/**
 * 识别菜品
 */
const recognizeDish = async () => {
  if (!selectedImage.value) return

  isRecognizing.value = true

  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 2000))

    // 模拟识别结果
    recognitionResult.value = {
      name: '宫保鸡丁',
      calories: 285,
      difficulty: '中等',
      preparationTime: '30分钟',
      nutrition: {
        protein: 75,
        proteinValue: 23.5,
        fat: 45,
        fatValue: 12.8,
        carb: 60,
        carbValue: 18.2
      }
    }

    uni.showToast({
      title: '识别成功！',
      icon: 'success'
    })
  } catch (error) {
    console.error('识别失败:', error)
    uni.showToast({
      title: '识别失败，请重试',
      icon: 'none'
    })
  } finally {
    isRecognizing.value = false
  }
}

/**
 * 添加到食谱
 */
const addToRecipe = () => {
  uni.showToast({
    title: '已添加到我的食谱',
    icon: 'success'
  })
}

/**
 * 分享结果
 */
const shareResult = () => {
  uni.showShareMenu({
    withShareTicket: true
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.dish-recognition {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 固定内容区域 */
.fixed-content {
  flex-shrink: 0;
  padding: $spacing-lg;
  background: $bg-color-white;
}

/* 可滚动内容区域 */
.scrollable-content {
  flex: 1;
  overflow-y: auto;
  padding: $spacing-lg;
  background: $bg-color-base;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
  margin-bottom: $spacing-md;
}

/* 上传区域 */
.upload-section {
  margin-bottom: $spacing-lg;
}

.upload-area {
  min-height: 400rpx;
  background: linear-gradient(135deg, #fff9fa 0%, #fff 100%);
  border: 3rpx dashed #ffe0e3;
  border-radius: $border-radius-lg;
  @include flex-center;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;

  &.has-image {
    border-style: solid;
    border-color: $primary-color;
  }
}

.upload-placeholder {
  @include flex-center-column;
  gap: $spacing-md;
  text-align: center;
}

.upload-icon {
  font-size: 100rpx;
}

.upload-text {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-medium;
}

.upload-hint {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.image-preview {
  width: 100%;
  height: 100%;
  position: relative;
}

.preview-image {
  width: 100%;
  height: 100%;
}

.image-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
  padding: $spacing-lg;
  @include flex-center;
}

.delete-btn {
  color: #fff;
  font-size: $font-size-base;
  padding: $spacing-sm $spacing-md;
  background: rgba(255, 107, 53, 0.9);
  border-radius: $border-radius-round;
}

/* 操作按钮 */
.action-buttons {
  @include flex-center;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
}

.recognize-btn,
.re-recognize-btn {
  flex: 1;
  height: 88rpx;
  @include flex-center;
  border-radius: $border-radius-lg;
  font-size: $font-size-base;
  border: none;
  transition: all 0.3s ease;

  &.disabled {
    opacity: 0.5;
  }
}

.recognize-btn {
  background: linear-gradient(135deg, $primary-color, #ff5252);
  color: #fff;
  font-weight: $font-weight-bold;

  &.loading {
    opacity: 0.8;
  }

  &:active:not(.disabled) {
    transform: scale(0.98);
  }
}

.re-recognize-btn {
  background: linear-gradient(135deg, #67c23a, #85ce61);
  color: #fff;

  &:active {
    transform: scale(0.98);
  }
}

/* 识别结果 */
.result-section {
  margin-bottom: $spacing-lg;
}

.result-cards {
  max-height: 800rpx;
}

.result-card {
  background: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  margin-bottom: $spacing-md;
  box-shadow: $box-shadow-sm;
  animation: cardFadeIn 0.5s ease-out;

  &.main-card {
    background: linear-gradient(135deg, $primary-color, #ff5252);
    .card-label,
    .card-value {
      color: #fff;
    }
  }

  &.calories-card {
    border-left: 6rpx solid #ff6b6b;
  }

  &.full-width {
    width: 100%;
  }
}

@keyframes cardFadeIn {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-label {
  font-size: $font-size-sm;
  color: $text-color-secondary;
  margin-bottom: $spacing-xs;
}

.card-value {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: $text-color-primary;

  &.highlight {
    color: $primary-color;
  }
}

/* 营养成分 */
.nutrition-list {
  margin-top: $spacing-md;
}

.nutrition-item {
  margin-bottom: $spacing-md;
  &:last-child {
    margin-bottom: 0;
  }
}

.nutrition-info {
  @include flex-center;
  gap: $spacing-xs;
  margin-bottom: $spacing-xs;
}

.nutrition-icon {
  font-size: $font-size-lg;
}

.nutrition-name {
  font-size: $font-size-sm;
  color: $text-color-primary;
  font-weight: $font-weight-medium;
}

.nutrition-bar {
  height: 16rpx;
  background: $bg-color-base;
  border-radius: 8rpx;
  overflow: hidden;
  margin-bottom: $spacing-xs;
}

.nutrition-fill {
  height: 100%;
  border-radius: 8rpx;
  transition: width 0.6s ease;

  &.protein {
    background: linear-gradient(90deg, #667eea, #764ba2);
  }

  &.fat {
    background: linear-gradient(90deg, #f093fb, #f5576c);
  }

  &.carb {
    background: linear-gradient(90deg, #4facfe, #00f2fe);
  }
}

.nutrition-value {
  font-size: $font-size-sm;
  color: $text-color-secondary;
  text-align: right;
  display: block;
}

/* 结果操作 */
.result-actions {
  @include flex-center;
  gap: $spacing-md;
  margin-top: $spacing-lg;
}

.action-btn {
  flex: 1;
  height: 80rpx;
  @include flex-center;
  border-radius: $border-radius-lg;
  font-size: $font-size-base;
  border: none;

  &.primary {
    background: linear-gradient(135deg, $primary-color, #ff5252);
    color: #fff;
  }

  &.secondary {
    background: linear-gradient(135deg, #67c23a, #85ce61);
    color: #fff;
  }

  &:active {
    transform: scale(0.98);
  }
}

/* 使用说明 */
.tips-section {
  margin-top: $spacing-lg;
}

.tips-list {
  background: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  box-shadow: $box-shadow-sm;
}

.tip-item {
  @include flex-center;
  gap: $spacing-md;
  margin-bottom: $spacing-md;
  padding-bottom: $spacing-md;
  border-bottom: 1rpx solid $border-color-lighter;

  &:last-child {
    margin-bottom: 0;
    padding-bottom: 0;
    border-bottom: none;
  }
}

.tip-icon {
  font-size: $font-size-xl;
  flex-shrink: 0;
}

.tip-text {
  font-size: $font-size-base;
  color: $text-color-regular;
  line-height: $line-height-lg;
}
</style>
