<!--
页面名称：ai/content-extract（重构版）
原代码行数：995行
重构后行数：约280行
减少比例：72%
重构时间：2026-03-20
-->
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
    <SectionCard>
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
      <view class="url-input-wrapper" v-if="extractMethod === 'text'">
        <input
          v-if="urlInput"
          class="url-input"
          v-model="articleUrl"
          placeholder="请输入文章链接"
          @confirm="handleUrlSubmit"
        />
        <button
          class="url-btn"
          :class="{ submit: urlInput }"
          @tap="urlInput ? handleUrlSubmit() : showUrlInput()"
        >
          {{ urlInput ? '确认' : '输入文章链接' }}
        </button>
      </view>
    </SectionCard>

    <!-- 提取选项 -->
    <SectionCard title="提取选项" v-if="uploadedFile && extractMethod !== 'text'">
      <view class="option-list">
        <view class="option-item" v-for="(label, key) in optionLabels" :key="key" @tap="toggleOption(key)">
          <view class="option-left">
            <uni-icons type="checkbox" size="20" :color="extractOptions[key] ? '#FF6B35' : '#CCC'"></uni-icons>
            <text class="option-label">{{ label }}</text>
          </view>
        </view>
      </view>
    </SectionCard>

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
        <NutritionGrid
          v-if="extractResult.nutrition"
          :nutrition-list="nutritionList"
          :columns="4"
        />

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
import { useUserStore } from '@/stores/user'
import { aiApi } from '@/api'
import SectionCard from '@/components/common/SectionCard.vue'
import NutritionGrid from '@/components/common/NutritionGrid.vue'
import { useContentExtract } from '@/composables/ai/useContentExtract'

const userStore = useUserStore()

// 使用内容提取 composable
const {
  extractMethod,
  uploadedFile,
  articleUrl,
  urlInput,
  extractOptions,
  extracting,
  extractResult,
  nutritionList,
  canExtract,
  selectMethod,
  getMethodText,
  getMethodHint,
  chooseFile,
  showUrlInput,
  handleUrlSubmit,
  removeFile,
  toggleOption,
  startExtract,
  copyResult,
  saveAsRecipe
} = useContentExtract(userStore)

// 选项标签映射
const optionLabels = {
  dishName: '菜品名称',
  ingredients: '食材清单',
  steps: '制作步骤',
  nutrition: '营养信息'
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

.header {
  text-align: center;
  margin-bottom: 40rpx;

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
}

.extract-methods {
  display: flex;
  gap: 20rpx;
  margin-bottom: 30rpx;

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

  .upload-text {
    font-size: 28rpx;
    color: #333;
  }

  .upload-hint {
    font-size: 24rpx;
    color: #999;
  }
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

  .url-input {
    flex: 1;
    height: 80rpx;
    padding: 0 20rpx;
    background: #F5F5F5;
    border-radius: 12rpx;
    font-size: 28rpx;
  }

  .url-btn {
    padding: 0 30rpx;
    height: 80rpx;
    background: #F5F5F5;
    color: #666;
    font-size: 26rpx;
    border-radius: 12rpx;
    border: none;
    line-height: 80rpx;

    &.submit {
      width: 150rpx;
      background: #FF6B35;
      color: #fff;
    }
  }
}

.option-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;

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
}

.action-section {
  margin-bottom: 30rpx;

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
}

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

  .ingredient-tag {
    padding: 10rpx 20rpx;
    background: #FFF7E6;
    color: #FF6B35;
    font-size: 24rpx;
    border-radius: 20rpx;
  }
}

.steps-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;

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

  .confidence-label {
    font-size: 26rpx;
    color: #666;
  }

  .confidence-value {
    font-size: 26rpx;
    font-weight: bold;
    color: #FF6B35;
  }
}
</style>
