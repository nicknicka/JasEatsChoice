<template>
  <view class="content-extraction">
    <!-- 固定内容：输入区域 -->
    <view class="fixed-content">
      <view class="section-title">📝 内容提取</view>

      <!-- 提取类型 -->
      <view class="type-group">
        <view class="type-label">选择提取类型</view>
        <view class="type-list">
          <view
            class="type-item"
            v-for="type in extractionTypes"
            :key="type.key"
            :class="{ active: selectedType === type.key }"
            @click="selectType(type.key)"
          >
            <text class="type-icon">{{ type.icon }}</text>
            <text class="type-text">{{ type.label }}</text>
          </view>
        </view>
      </view>

      <!-- 内容输入 -->
      <view class="content-input">
        <view class="input-label">输入要提取的内容</view>
        <textarea
          class="content-textarea"
          v-model="contentText"
          :placeholder="getPlaceholder()"
          :maxlength="5000"
        />
        <view class="input-footer">
          <text class="char-count">{{ contentText.length }}/5000</text>
          <button class="paste-btn" @click="pasteContent" v-if="!contentText">
            📋 粘贴
          </button>
          <button class="clear-btn" @click="clearContent" v-else>
            🗑️ 清空
          </button>
        </view>
      </view>

      <!-- 提取按钮 -->
      <button
        class="extract-btn"
        :class="{ loading: isExtracting }"
        :disabled="!contentText.trim() || !selectedType || isExtracting"
        @click="extractContent"
      >
        <text v-if="isExtracting">提取中...</text>
        <text v-else>✨ 开始提取</text>
      </button>
    </view>

    <!-- 可滚动内容：提取结果 -->
    <scroll-view v-if="extractionResult" class="scrollable-content" scroll-y>
        <!-- 结果卡片 -->
        <view class="result-card">
          <!-- 标题 -->
          <view class="result-header">
            <text class="result-icon">✨</text>
            <text class="result-title">{{ getTypeLabel(selectedType) }}</text>
          </view>

          <!-- 提取的内容 -->
          <view class="extracted-content">
            <!-- 食谱提取 -->
            <template v-if="selectedType === 'recipe'">
              <view class="recipe-detail">
                <view class="detail-item">
                  <text class="detail-label">📖 菜名</text>
                  <text class="detail-value">{{ extractionResult.name }}</text>
                </view>
                <view class="detail-item">
                  <text class="detail-label">🔥 卡路里</text>
                  <text class="detail-value">{{ extractionResult.calories }} kcal</text>
                </view>
                <view class="detail-item">
                  <text class="detail-label">⏱️ 时间</text>
                  <text class="detail-value">{{ extractionResult.time }}</text>
                </view>
                <view class="detail-section">
                  <text class="detail-section-label">🥘 食材</text>
                  <view class="ingredient-list">
                    <view
                      class="ingredient-item"
                      v-for="(ingredient, index) in extractionResult.ingredients"
                      :key="index"
                    >
                      <text class="ingredient-name">{{ ingredient.name }}</text>
                      <text class="ingredient-amount">{{ ingredient.amount }}</text>
                    </view>
                  </view>
                </view>
                <view class="detail-section">
                  <text class="detail-section-label">👨‍🍳 步骤</text>
                  <view class="step-list">
                    <view
                      class="step-item"
                      v-for="(step, index) in extractionResult.steps"
                      :key="index"
                    >
                      <text class="step-number">{{ index + 1 }}</text>
                      <text class="step-text">{{ step }}</text>
                    </view>
                  </view>
                </view>
              </view>
            </template>

            <!-- 营养信息提取 -->
            <template v-else-if="selectedType === 'nutrition'">
              <view class="nutrition-detail">
                <view class="nutrition-grid">
                  <view
                    class="nutrition-box"
                    v-for="(item, index) in extractionResult.nutrition"
                    :key="index"
                  >
                    <text class="nutrition-icon">{{ item.icon }}</text>
                    <text class="nutrition-name">{{ item.name }}</text>
                    <text class="nutrition-value">{{ item.value }}</text>
                  </view>
                </view>
              </view>
            </template>

            <!-- 关键信息提取 -->
            <template v-else-if="selectedType === 'keypoints'">
              <view class="keypoints-detail">
                <view
                  class="keypoint-item"
                  v-for="(point, index) in extractionResult.points"
                  :key="index"
                >
                  <text class="keypoint-icon">💡</text>
                  <text class="keypoint-text">{{ point }}</text>
                </view>
              </view>
            </template>

            <!-- 总结提取 -->
            <template v-else-if="selectedType === 'summary'">
              <view class="summary-detail">
                <text class="summary-text">{{ extractionResult.summary }}</text>
              </view>
            </template>
          </view>
        </view>

        <!-- 操作按钮 -->
        <view class="result-actions">
          <button class="action-btn primary" @click="copyResult">
            📋 复制结果
          </button>
          <button class="action-btn secondary" @click="shareResult">
            📤 分享
          </button>
        </view>
      </scroll-view>

    <!-- 可滚动内容：历史记录 -->
    <scroll-view v-if="!extractionResult && historyList.length > 0" class="scrollable-content" scroll-y>
      <view class="history-section">
        <view class="section-title">📜 历史记录</view>
        <view class="history-list">
          <view
            class="history-item"
            v-for="(item, index) in historyList"
            :key="index"
            @click="loadHistory(item)"
          >
            <view class="history-info">
              <text class="history-type">{{ getTypeLabel(item.type) }}</text>
              <text class="history-preview">{{ item.content.substring(0, 30) }}...</text>
              <text class="history-time">{{ item.time }}</text>
            </view>
            <text class="history-arrow">→</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { formatTime } from '@/utils/helper'

// 提取类型选项
const extractionTypes = ref([
  { key: 'recipe', label: '食谱', icon: '🍳' },
  { key: 'nutrition', label: '营养', icon: '🥗' },
  { key: 'keypoints', label: '要点', icon: '💡' },
  { key: 'summary', label: '总结', icon: '📝' }
])

// 选中的提取类型
const selectedType = ref('')

// 内容文本
const contentText = ref('')

// 是否正在提取
const isExtracting = ref(false)

// 提取结果
const extractionResult = ref(null)

// 历史记录
const historyList = ref([])

/**
 * 选择提取类型
 */
const selectType = (type) => {
  selectedType.value = type
}

/**
 * 获取占位符文本
 */
const getPlaceholder = () => {
  const placeholders = {
    recipe: '请输入或粘贴食谱内容，例如：\n宫保鸡丁\n\n食材：\n- 鸡胸肉 300g\n- 花生米 100g\n- 干辣椒 10个\n\n步骤：\n1. 鸡胸肉切丁，用料酒腌制...\n2. 热锅下油，炒鸡丁...',
    nutrition: '请输入或粘贴包含营养信息的文本...',
    keypoints: '请输入或粘贴需要提取要点的文章内容...',
    summary: '请输入或粘贴需要总结的文章内容...'
  }
  return placeholders[selectedType.value] || '请输入要提取的内容...'
}

/**
 * 获取类型标签
 */
const getTypeLabel = (type) => {
  const typeObj = extractionTypes.value.find(t => t.key === type)
  return typeObj ? typeObj.label : type
}

/**
 * 粘贴内容
 */
const pasteContent = () => {
  uni.getClipboardData({
    success: (res) => {
      contentText.value = res.data
    },
    fail: () => {
      uni.showToast({
        title: '粘贴失败',
        icon: 'none'
      })
    }
  })
}

/**
 * 清空内容
 */
const clearContent = () => {
  contentText.value = ''
}

/**
 * 提取内容
 */
const extractContent = async () => {
  if (!contentText.value.trim() || !selectedType.value) return

  isExtracting.value = true

  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 2000))

    // 根据类型生成不同的结果
    if (selectedType.value === 'recipe') {
      extractionResult.value = {
        name: '宫保鸡丁',
        calories: 285,
        time: '30分钟',
        ingredients: [
          { name: '鸡胸肉', amount: '300g' },
          { name: '花生米', amount: '100g' },
          { name: '干辣椒', amount: '10个' },
          { name: '葱', amount: '2根' },
          { name: '蒜', amount: '3瓣' }
        ],
        steps: [
          '鸡胸肉切丁，用料酒、盐腌制15分钟',
          '热锅下油，炒鸡丁至变色盛起',
          '爆炒干辣椒、蒜瓣出香味',
          '加入鸡丁翻炒，调入生抽、老抽',
          '最后加入花生米和葱段炒匀即可'
        ]
      }
    } else if (selectedType.value === 'nutrition') {
      extractionResult.value = {
        nutrition: [
          { icon: '🔥', name: '卡路里', value: '285 kcal' },
          { icon: '💪', name: '蛋白质', value: '23.5g' },
          { icon: '🧈', name: '脂肪', value: '12.8g' },
          { icon: '🍞', name: '碳水', value: '18.2g' },
          { icon: '🥬', name: '纤维', value: '2.5g' },
          { icon: '🧂', name: '钠', value: '480mg' }
        ]
      }
    } else if (selectedType.value === 'keypoints') {
      extractionResult.value = {
        points: [
          '宫保鸡丁是一道经典的川菜',
          '主要食材是鸡胸肉和花生米',
          '口感麻辣鲜香，营养丰富',
          '蛋白质含量高，适合健身人群',
          '烹饪时间约30分钟，简单易学'
        ]
      }
    } else if (selectedType.value === 'summary') {
      extractionResult.value = {
        summary: '宫保鸡丁是一道著名的中式菜肴，起源于四川。这道菜以鸡胸肉为主料，配以花生米、干辣椒等食材炒制而成。口感麻辣鲜香，色彩丰富，营养均衡。富含优质蛋白质，适合各类人群食用。制作方法简单，是家庭常备菜谱之一。'
      }
    }

    // 添加到历史记录
    historyList.value.unshift({
      type: selectedType.value,
      content: contentText.value,
      result: extractionResult.value,
      time: formatTime(new Date())
    })

    // 限制历史记录数量
    if (historyList.value.length > 10) {
      historyList.value = historyList.value.slice(0, 10)
    }

    uni.showToast({
      title: '提取完成！',
      icon: 'success'
    })
  } catch (error) {
    console.error('提取失败:', error)
    uni.showToast({
      title: '提取失败，请重试',
      icon: 'none'
    })
  } finally {
    isExtracting.value = false
  }
}

/**
 * 加载历史记录
 */
const loadHistory = (item) => {
  selectedType.value = item.type
  contentText.value = item.content
  extractionResult.value = item.result
}

/**
 * 复制结果
 */
const copyResult = () => {
  let text = ''
  if (selectedType.value === 'recipe') {
    text = `菜名：${extractionResult.value.name}\n卡路里：${extractionResult.value.calories}kcal\n时间：${extractionResult.value.time}`
  } else if (selectedType.value === 'keypoints') {
    text = extractionResult.value.points.join('\n')
  } else if (selectedType.value === 'summary') {
    text = extractionResult.value.summary
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

.content-extraction {
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

/* 输入区域 */
.input-section {
  margin-bottom: $spacing-lg;
}

.type-group {
  margin-bottom: $spacing-lg;
}

.type-label {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-medium;
  margin-bottom: $spacing-md;
}

.type-list {
  @include flex-center;
  flex-wrap: wrap;
  gap: $spacing-sm;
}

.type-item {
  flex: 1;
  min-width: 140rpx;
  @include flex-center-column;
  gap: $spacing-xs;
  padding: $spacing-md;
  background: $bg-color-white;
  border: 2rpx solid $border-color-light;
  border-radius: $border-radius-lg;
  transition: all 0.3s ease;

  &.active {
    border-color: $primary-color;
    background: linear-gradient(135deg, rgba(255, 107, 53, 0.1), rgba(255, 107, 53, 0.05));
  }

  &:active {
    transform: scale(0.95);
  }
}

.type-icon {
  font-size: 44rpx;
}

.type-text {
  font-size: $font-size-sm;
  color: $text-color-regular;
}

.type-item.active .type-text {
  color: $primary-color;
  font-weight: $font-weight-medium;
}

/* 内容输入 */
.content-input {
  margin-bottom: $spacing-lg;
}

.input-label {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-medium;
  margin-bottom: $spacing-sm;
}

.content-textarea {
  width: 100%;
  min-height: 300rpx;
  padding: $spacing-md;
  background: $bg-color-white;
  border: 2rpx solid $border-color-light;
  border-radius: $border-radius-lg;
  font-size: $font-size-base;
  color: $text-color-primary;
  line-height: $line-height-lg;
}

.input-footer {
  @include flex-between;
  margin-top: $spacing-sm;
}

.char-count {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.paste-btn,
.clear-btn {
  padding: $spacing-xs $spacing-md;
  background: rgba(255, 107, 53, 0.1);
  color: $primary-color;
  border-radius: $border-radius-round;
  font-size: $font-size-sm;
  border: none;
}

/* 提取按钮 */
.extract-btn {
  width: 100%;
  height: 88rpx;
  @include flex-center;
  background: linear-gradient(135deg, $primary-color, #ff5252);
  color: #fff;
  border-radius: $border-radius-lg;
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  border: none;

  &.disabled {
    opacity: 0.5;
  }

  &.loading {
    opacity: 0.8;
  }

  &:active:not(.disabled) {
    transform: scale(0.98);
  }
}

/* 结果区域 */
.result-section {
  margin-bottom: $spacing-lg;
}

.result-content {
  max-height: 1200rpx;
}

.result-card {
  background: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  box-shadow: $box-shadow-sm;
  margin-bottom: $spacing-md;
}

.result-header {
  @include flex-center;
  gap: $spacing-sm;
  margin-bottom: $spacing-lg;
  padding-bottom: $spacing-md;
  border-bottom: 2rpx solid $border-color-lighter;
}

.result-icon {
  font-size: $font-size-xl;
}

.result-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

/* 食谱详情 */
.recipe-detail {
  .detail-item {
    @include flex-between;
    padding: $spacing-md;
    margin-bottom: $spacing-sm;
    background: $bg-color-base;
    border-radius: $border-radius-base;
  }

  .detail-label {
    font-size: $font-size-base;
    color: $text-color-secondary;
  }

  .detail-value {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $primary-color;
  }

  .detail-section {
    margin-top: $spacing-lg;
  }

  .detail-section-label {
    font-size: $font-size-base;
    font-weight: $font-weight-bold;
    color: $text-color-primary;
    display: block;
    margin-bottom: $spacing-md;
  }

  .ingredient-list {
    background: $bg-color-base;
    border-radius: $border-radius-base;
    padding: $spacing-md;
  }

  .ingredient-item {
    @include flex-between;
    padding: $spacing-xs 0;

    &:not(:last-child) {
      border-bottom: 1rpx solid $border-color-lighter;
    }
  }

  .ingredient-name {
    font-size: $font-size-base;
    color: $text-color-primary;
  }

  .ingredient-amount {
    font-size: $font-size-sm;
    color: $text-color-secondary;
  }

  .step-list {
    background: $bg-color-base;
    border-radius: $border-radius-base;
    padding: $spacing-md;
  }

  .step-item {
    @include flex-start;
    gap: $spacing-md;
    margin-bottom: $spacing-md;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .step-number {
    width: 48rpx;
    height: 48rpx;
    @include flex-center;
    background: linear-gradient(135deg, $primary-color, #ff5252);
    color: #fff;
    border-radius: 50%;
    font-size: $font-size-sm;
    font-weight: $font-weight-bold;
    flex-shrink: 0;
  }

  .step-text {
    flex: 1;
    font-size: $font-size-base;
    color: $text-color-primary;
    line-height: $line-height-lg;
  }
}

/* 营养详情 */
.nutrition-detail {
  .nutrition-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: $spacing-md;
  }

  .nutrition-box {
    @include flex-center-column;
    gap: $spacing-xs;
    padding: $spacing-lg;
    background: $bg-color-base;
    border-radius: $border-radius-base;
  }

  .nutrition-icon {
    font-size: $font-size-xl;
  }

  .nutrition-name {
    font-size: $font-size-sm;
    color: $text-color-secondary;
  }

  .nutrition-value {
    font-size: $font-size-lg;
    font-weight: $font-weight-bold;
    color: $primary-color;
  }
}

/* 要点详情 */
.keypoints-detail {
  .keypoint-item {
    @include flex-start;
    gap: $spacing-md;
    padding: $spacing-md;
    margin-bottom: $spacing-sm;
    background: $bg-color-base;
    border-radius: $border-radius-base;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .keypoint-icon {
    font-size: $font-size-xl;
    flex-shrink: 0;
  }

  .keypoint-text {
    flex: 1;
    font-size: $font-size-base;
    color: $text-color-primary;
    line-height: $line-height-lg;
  }
}

/* 总结详情 */
.summary-detail {
  .summary-text {
    font-size: $font-size-base;
    color: $text-color-primary;
    line-height: $line-height-lg;
    text-align: justify;
  }
}

/* 操作按钮 */
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

/* 历史记录 */
.history-section {
  margin-top: $spacing-lg;
}

.history-list {
  max-height: 400rpx;
}

.history-item {
  @include flex-between;
  padding: $spacing-md;
  background: $bg-color-white;
  border-radius: $border-radius-base;
  margin-bottom: $spacing-sm;
  box-shadow: $box-shadow-sm;

  &:active {
    background: $bg-color-base;
  }
}

.history-info {
  @include flex-center-column;
  align-items: flex-start;
  gap: $spacing-xs;
  flex: 1;
}

.history-type {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-medium;
}

.history-preview {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.history-time {
  font-size: $font-size-xs;
  color: $text-color-placeholder;
}

.history-arrow {
  font-size: $font-size-xl;
  color: $text-color-placeholder;
  margin-left: $spacing-md;
}
</style>
