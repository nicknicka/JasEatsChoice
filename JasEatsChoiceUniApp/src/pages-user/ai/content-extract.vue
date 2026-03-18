<template>
  <view class="content-extract-container">
    <!-- 顶部标题 -->
    <view class="header">
      <text class="title">AI内容提取</text>
      <text class="subtitle">从图片、视频中提取菜品信息</text>
    </view>

    <!-- 提取方式选择 -->
    <view class="extract-methods">
      <view
        class="method-card"
        :class="{ active: extractMethod === 'image' }"
        @tap="selectMethod('image')"
      >
        <view class="method-icon">
          <uni-icons type="image" size="40" color="#FF6B35"></uni-icons>
        </view>
        <text class="method-title">图片识别</text>
        <text class="method-desc">从菜品图片中提取信息</text>
      </view>

      <view
        class="method-card"
        :class="{ active: extractMethod === 'video' }"
        @tap="selectMethod('video')"
      >
        <view class="method-icon">
          <uni-icons type="videocam" size="40" color="#FF6B35"></uni-icons>
        </view>
        <text class="method-title">视频提取</text>
        <text class="method-desc">从美食视频中提取步骤</text>
      </view>

      <view
        class="method-card"
        :class="{ active: extractMethod === 'text' }"
        @tap="selectMethod('text')"
      >
        <view class="method-icon">
          <uni-icons type="document" size="40" color="#FF6B35"></uni-icons>
        </view>
        <text class="method-title">文章解析</text>
        <text class="method-desc">从文章中提取食谱信息</text>
      </view>
    </view>

    <!-- 上传区域 -->
    <view class="upload-section">
      <view class="upload-area" @tap="chooseFile">
        <view class="upload-content" v-if="!uploadedFile">
          <uni-icons type="plus" size="60" color="#CCCCCC"></uni-icons>
          <text class="upload-text">点击上传{{ getMethodText() }}</text>
          <text class="upload-hint">{{ getMethodHint() }}</text>
        </view>
        <view class="uploaded-content" v-else>
          <image class="preview-image" :src="uploadedFile" mode="aspectFill" v-if="extractMethod === 'image'"></image>
          <video class="preview-video" :src="uploadedFile" v-if="extractMethod === 'video'"></video>
          <view class="preview-text" v-if="extractMethod === 'text'">
            <uni-icons type="link" size="40" color="#FF6B35"></uni-icons>
            <text class="link-text">{{ uploadedFile }}</text>
          </view>
          <view class="remove-btn" @tap.stop="removeFile">
            <uni-icons type="close" size="20" color="#fff"></uni-icons>
          </view>
        </view>
      </view>

      <!-- 文章链接输入 -->
      <view class="url-input-wrapper" v-if="extractMethod === 'text' && !urlInput">
        <button class="url-input-btn" @tap="showUrlInput">输入文章链接</button>
      </view>
      <view class="url-input-wrapper" v-if="extractMethod === 'text' && urlInput">
        <input
          class="url-input"
          v-model="articleUrl"
          placeholder="请输入文章链接"
          @confirm="handleUrlSubmit"
        />
        <button class="url-submit-btn" @tap="handleUrlSubmit">确认</button>
      </view>
    </view>

    <!-- 提取选项 -->
    <view class="extract-options" v-if="uploadedFile && extractMethod !== 'text'">
      <view class="option-title">提取选项</view>
      <view class="option-list">
        <view class="option-item" @tap="toggleOption('dishName')">
          <view class="option-left">
            <uni-icons type="checkbox" size="20" :color="extractOptions.dishName ? '#FF6B35' : '#CCC'"></uni-icons>
            <text class="option-label">菜品名称</text>
          </view>
        </view>
        <view class="option-item" @tap="toggleOption('ingredients')">
          <view class="option-left">
            <uni-icons type="checkbox" size="20" :color="extractOptions.ingredients ? '#FF6B35' : '#CCC'"></uni-icons>
            <text class="option-label">食材清单</text>
          </view>
        </view>
        <view class="option-item" @tap="toggleOption('steps')">
          <view class="option-left">
            <uni-icons type="checkbox" size="20" :color="extractOptions.steps ? '#FF6B35' : '#CCC'"></uni-icons>
            <text class="option-label">制作步骤</text>
          </view>
        </view>
        <view class="option-item" @tap="toggleOption('nutrition')">
          <view class="option-left">
            <uni-icons type="checkbox" size="20" :color="extractOptions.nutrition ? '#FF6B35' : '#CCC'"></uni-icons>
            <text class="option-label">营养信息</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 提取按钮 -->
    <view class="action-section">
      <button
        class="extract-btn"
        :disabled="!canExtract"
        @tap="startExtract"
      >
        {{ extracting ? '提取中...' : '开始提取' }}
      </button>
    </view>

    <!-- 提取结果 -->
    <view class="result-section" v-if="extractResult">
      <view class="result-header">
        <text class="result-title">提取结果</text>
        <view class="result-actions">
          <button class="action-btn" @tap="copyResult">复制</button>
          <button class="action-btn primary" @tap="saveAsRecipe">保存为食谱</button>
        </view>
      </view>

      <scroll-view class="result-content" scroll-y>
        <!-- 菜品名称 -->
        <view class="result-item" v-if="extractResult.dishName">
          <view class="item-label">菜品名称</view>
          <view class="item-value">{{ extractResult.dishName }}</view>
        </view>

        <!-- 食材清单 -->
        <view class="result-item" v-if="extractResult.ingredients && extractResult.ingredients.length > 0">
          <view class="item-label">食材清单</view>
          <view class="ingredients-list">
            <view
              class="ingredient-tag"
              v-for="(item, index) in extractResult.ingredients"
              :key="index"
            >
              {{ item.name }} {{ item.amount }}
            </view>
          </view>
        </view>

        <!-- 制作步骤 -->
        <view class="result-item" v-if="extractResult.steps && extractResult.steps.length > 0">
          <view class="item-label">制作步骤</view>
          <view class="steps-list">
            <view
              class="step-item"
              v-for="(step, index) in extractResult.steps"
              :key="index"
            >
              <view class="step-number">{{ index + 1 }}</view>
              <view class="step-content">{{ step }}</view>
            </view>
          </view>
        </view>

        <!-- 营养信息 -->
        <view class="result-item" v-if="extractResult.nutrition">
          <view class="item-label">营养信息</view>
          <view class="nutrition-grid">
            <view class="nutrition-item">
              <text class="nutrition-value">{{ extractResult.nutrition.calories }}</text>
              <text class="nutrition-label">卡路里</text>
            </view>
            <view class="nutrition-item">
              <text class="nutrition-value">{{ extractResult.nutrition.protein }}g</text>
              <text class="nutrition-label">蛋白质</text>
            </view>
            <view class="nutrition-item">
              <text class="nutrition-value">{{ extractResult.nutrition.carbs }}g</text>
              <text class="nutrition-label">碳水</text>
            </view>
            <view class="nutrition-item">
              <text class="nutrition-value">{{ extractResult.nutrition.fat }}g</text>
              <text class="nutrition-label">脂肪</text>
            </view>
          </view>
        </view>

        <!-- 置信度 -->
        <view class="result-confidence" v-if="extractResult.confidence">
          <text class="confidence-label">识别置信度：</text>
          <text class="confidence-value">{{ extractResult.confidence }}%</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

// 提取方式
const extractMethod = ref('image')
const uploadedFile = ref('')
const articleUrl = ref('')
const urlInput = ref(false)

// 提取选项
const extractOptions = ref({
  dishName: true,
  ingredients: true,
  steps: true,
  nutrition: false
})

// 提取状态
const extracting = ref(false)
const extractResult = ref(null)

/**
 * 选择提取方式
 */
const selectMethod = (method) => {
  extractMethod.value = method
  uploadedFile.value = ''
  articleUrl.value = ''
  extractResult.value = null
}

/**
 * 获取方式文本
 */
const getMethodText = () => {
  const textMap = {
    image: '菜品图片',
    video: '美食视频',
    text: '文章链接'
  }
  return textMap[extractMethod.value]
}

/**
 * 获取提示文本
 */
const getMethodHint = () => {
  const hintMap = {
    image: '支持JPG、PNG格式，最大5MB',
    video: '支持MP4格式，最大50MB',
    text: '支持微信公众号、知乎等平台'
  }
  return hintMap[extractMethod.value]
}

/**
 * 是否可以提取
 */
const canExtract = computed(() => {
  return uploadedFile.value && !extracting.value
})

/**
 * 选择文件
 */
const chooseFile = () => {
  if (extractMethod.value === 'text') {
    showUrlInput()
    return
  }

  if (extractMethod.value === 'image') {
    uni.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        uploadedFile.value = res.tempFilePaths[0]
      }
    })
  } else if (extractMethod.value === 'video') {
    uni.chooseVideo({
      sourceType: ['album', 'camera'],
      maxDuration: 300,
      success: (res) => {
        uploadedFile.value = res.tempFilePath
      }
    })
  }
}

/**
 * 显示URL输入框
 */
const showUrlInput = () => {
  urlInput.value = true
}

/**
 * 处理URL提交
 */
const handleUrlSubmit = () => {
  if (!articleUrl.value) {
    uni.showToast({
      title: '请输入文章链接',
      icon: 'none'
    })
    return
  }

  // 简单的URL验证
  const urlPattern = /^https?:\/\/.+/
  if (!urlPattern.test(articleUrl.value)) {
    uni.showToast({
      title: '请输入有效的链接',
      icon: 'none'
    })
    return
  }

  uploadedFile.value = articleUrl.value
  urlInput.value = false
}

/**
 * 移除文件
 */
const removeFile = () => {
  uni.showModal({
    title: '提示',
    content: '确定要移除已上传的文件吗？',
    success: (res) => {
      if (res.confirm) {
        uploadedFile.value = ''
        extractResult.value = null
      }
    }
  })
}

/**
 * 切换提取选项
 */
const toggleOption = (option) => {
  extractOptions.value[option] = !extractOptions.value[option]
}

/**
 * 开始提取
 */
const startExtract = () => {
  extracting.value = true

  // TODO: 调用AI提取API
  // const res = await aiApi.extractContent({
  //   type: extractMethod.value,
  //   file: uploadedFile.value,
  //   options: extractOptions.value
  // })

  // 模拟提取结果
  setTimeout(() => {
    extractResult.value = {
      dishName: '宫保鸡丁',
      ingredients: [
        { name: '鸡胸肉', amount: '300g' },
        { name: '花生米', amount: '50g' },
        { name: '干辣椒', amount: '10个' },
        { name: '花椒', amount: '适量' },
        { name: '葱', amount: '2根' },
        { name: '姜', amount: '3片' },
        { name: '蒜', amount: '3瓣' }
      ],
      steps: [
        '鸡胸肉切丁，用料酒、生抽腌制15分钟',
        '花生米炸酥脆，盛起备用',
        '热锅下油，爆香花椒和干辣椒',
        '下鸡丁炒至变色',
        '加入葱姜蒜炒香',
        '调入生抽、老抽、糖炒匀',
        '最后加入花生米，翻炒均匀即可'
      ],
      nutrition: {
        calories: '280',
        protein: '25',
        carbs: '12',
        fat: '18'
      },
      confidence: 95
    }
    extracting.value = false

    uni.showToast({
      title: '提取成功',
      icon: 'success'
    })
  }, 2000)
}

/**
 * 复制结果
 */
const copyResult = () => {
  let text = ''
  if (extractResult.value.dishName) {
    text += `菜品名称：${extractResult.value.dishName}\n\n`
  }
  if (extractResult.value.ingredients) {
    text += `食材清单：\n`
    extractResult.value.ingredients.forEach(item => {
      text += `- ${item.name} ${item.amount}\n`
    })
    text += '\n'
  }
  if (extractResult.value.steps) {
    text += `制作步骤：\n`
    extractResult.value.steps.forEach((step, index) => {
      text += `${index + 1}. ${step}\n`
    })
  }

  uni.setClipboardData({
    data: text,
    success: () => {
      uni.showToast({
        title: '已复制到剪贴板',
        icon: 'success'
      })
    }
  })
}

/**
 * 保存为食谱
 */
const saveAsRecipe = () => {
  uni.showModal({
    title: '保存为食谱',
    content: '确认将提取结果保存为食谱吗？',
    success: (res) => {
      if (res.confirm) {
        // TODO: 调用保存食谱API
        uni.showLoading({
          title: '保存中...'
        })

        setTimeout(() => {
          uni.hideLoading()
          uni.showToast({
            title: '保存成功',
            icon: 'success'
          })

          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        }, 1000)
      }
    }
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.content-extract-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding: 30rpx;
  padding-bottom: 150rpx;
}

/* 头部 */
.header {
  text-align: center;
  margin-bottom: 40rpx;
}

.title {
  display: block;
  font-size: 40rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
}

.subtitle {
  display: block;
  font-size: 26rpx;
  color: #999;
}

/* 提取方式 */
.extract-methods {
  display: flex;
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.method-card {
  flex: 1;
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s;

  &.active {
    border-color: #FF6B35;
    background: #FFFBF0;
  }
}

.method-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: #FFF7E6;
  @include flex-center;
}

.method-title {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.method-desc {
  font-size: 22rpx;
  color: #999;
  text-align: center;
}

/* 上传区域 */
.upload-section {
  margin-bottom: 30rpx;
}

.upload-area {
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx;
  border: 2rpx dashed #E8E8E8;
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20rpx;
}

.upload-text {
  font-size: 28rpx;
  color: #333;
}

.upload-hint {
  font-size: 24rpx;
  color: #999;
}

.uploaded-content {
  position: relative;
  width: 100%;
}

.preview-image {
  width: 100%;
  height: 400rpx;
  border-radius: 12rpx;
}

.preview-video {
  width: 100%;
  height: 400rpx;
  border-radius: 12rpx;
}

.preview-text {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 40rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
}

.link-text {
  flex: 1;
  font-size: 26rpx;
  color: #666;
  @include text-ellipsis;
}

.remove-btn {
  position: absolute;
  top: -10rpx;
  right: -10rpx;
  width: 50rpx;
  height: 50rpx;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  @include flex-center;
}

.url-input-wrapper {
  margin-top: 20rpx;
  display: flex;
  gap: 20rpx;
}

.url-input-btn {
  flex: 1;
  height: 80rpx;
  background: #F5F5F5;
  color: #666;
  font-size: 28rpx;
  border-radius: 12rpx;
  border: none;
}

.url-input {
  flex: 1;
  height: 80rpx;
  padding: 0 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
  font-size: 28rpx;
}

.url-submit-btn {
  width: 150rpx;
  height: 80rpx;
  background: #FF6B35;
  color: #fff;
  font-size: 28rpx;
  border-radius: 12rpx;
  border: none;
}

/* 提取选项 */
.extract-options {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}

.option-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.option-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.option-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.option-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.option-label {
  font-size: 28rpx;
  color: #333;
}

/* 操作按钮 */
.action-section {
  margin-bottom: 30rpx;
}

.extract-btn {
  width: 100%;
  height: 90rpx;
  background: #FF6B35;
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  border-radius: 45rpx;
  border: none;
  @include flex-center;

  &[disabled] {
    background: #E8E8E8;
    color: #999;
  }
}

/* 提取结果 */
.result-section {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #eee;
}

.result-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.result-actions {
  display: flex;
  gap: 15rpx;
}

.action-btn {
  padding: 10rpx 20rpx;
  background: #F5F5F5;
  color: #666;
  font-size: 24rpx;
  border-radius: 8rpx;
  border: none;

  &.primary {
    background: #FF6B35;
    color: #fff;
  }
}

.result-content {
  max-height: 800rpx;
  padding: 30rpx;
}

.result-item {
  margin-bottom: 30rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.item-label {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 15rpx;
}

.item-value {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
}

.ingredients-list {
  display: flex;
  flex-wrap: wrap;
  gap: 15rpx;
}

.ingredient-tag {
  padding: 10rpx 20rpx;
  background: #FFF7E6;
  color: #FF6B35;
  font-size: 24rpx;
  border-radius: 20rpx;
}

.steps-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.step-item {
  display: flex;
  gap: 20rpx;
}

.step-number {
  width: 50rpx;
  height: 50rpx;
  background: #FF6B35;
  color: #fff;
  font-size: 24rpx;
  border-radius: 50%;
  @include flex-center;
  flex-shrink: 0;
}

.step-content {
  flex: 1;
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
  padding-top: 5rpx;
}

.nutrition-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
}

.nutrition-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
}

.nutrition-value {
  font-size: 32rpx;
  font-weight: bold;
  color: #FF6B35;
}

.nutrition-label {
  font-size: 22rpx;
  color: #999;
}

.result-confidence {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  padding: 20rpx;
  background: #FFF7E6;
  border-radius: 12rpx;
  margin-top: 20rpx;
}

.confidence-label {
  font-size: 26rpx;
  color: #666;
}

.confidence-value {
  font-size: 26rpx;
  font-weight: bold;
  color: #FF6B35;
}
</style>
