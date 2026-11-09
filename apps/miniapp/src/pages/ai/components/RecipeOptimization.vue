<template>
  <view class="recipe-optimization">
    <!-- 固定内容：输入区域 -->
    <view class="fixed-content">
      <view class="section-title">🍳 食谱优化</view>

      <!-- 优化目标 -->
      <view class="option-group">
        <view class="option-label">选择优化目标</view>
        <view class="option-list">
          <view
            class="option-item"
            v-for="option in optimizationOptions"
            :key="option.key"
            :class="{ active: selectedGoal === option.key }"
            @click="selectGoal(option.key)"
          >
            <text class="option-icon">{{ option.icon }}</text>
            <text class="option-text">{{ option.label }}</text>
          </view>
        </view>
      </view>

      <!-- 食谱输入 -->
      <view class="recipe-input">
        <view class="input-label">输入食谱内容</view>
        <textarea
          class="recipe-textarea"
          v-model="recipeText"
          placeholder="请输入您想要优化的食谱内容，例如：&#10;食材：鸡胸肉200g，西兰花100g，橄榄油10ml&#10;做法：鸡胸肉煎至两面金黄，加入西兰花炒熟..."
          :maxlength="2000"
        />
        <view class="input-footer">
          <text class="char-count">{{ recipeText.length }}/2000</text>
          <button class="clear-btn" @click="clearText" v-if="recipeText">
            🗑️ 清空
          </button>
        </view>
      </view>

      <!-- 优化按钮 -->
      <button
        class="optimize-btn"
        :class="{ loading: isOptimizing }"
        :disabled="!recipeText.trim() || !selectedGoal || isOptimizing"
        @click="optimizeRecipe"
      >
        <text v-if="isOptimizing">优化中...</text>
        <text v-else>✨ 开始优化</text>
      </button>
    </view>

    <!-- 可滚动内容：优化结果 -->
    <scroll-view v-if="optimizationResult" class="scrollable-content" scroll-y>
        <!-- 原食谱分析 -->
        <view class="analysis-card">
          <view class="card-header">
            <text class="card-icon">📊</text>
            <text class="card-title">原食谱分析</text>
          </view>
          <view class="analysis-content">
            <view class="analysis-item">
              <text class="analysis-label">总卡路里</text>
              <text class="analysis-value">{{ optimizationResult.originalCalories }} kcal</text>
            </view>
            <view class="analysis-item">
              <text class="analysis-label">蛋白质</text>
              <text class="analysis-value">{{ optimizationResult.originalProtein }}g</text>
            </view>
            <view class="analysis-item">
              <text class="analysis-label">脂肪</text>
              <text class="analysis-value">{{ optimizationResult.originalFat }}g</text>
            </view>
            <view class="analysis-item">
              <text class="analysis-label">碳水化合物</text>
              <text class="analysis-value">{{ optimizationResult.originalCarb }}g</text>
            </view>
          </view>
        </view>

        <!-- 优化后食谱 -->
        <view class="optimized-card">
          <view class="card-header">
            <text class="card-icon">✨</text>
            <text class="card-title">优化后食谱</text>
            <text class="card-recipe-name">{{ optimizationResult.recipeName }}</text>
          </view>

          <!-- 食材清单 -->
          <view class="recipe-block" v-if="optimizationResult.ingredients.length">
            <view class="recipe-block-title">
              <text class="recipe-block-icon">🥕</text>
              <text class="recipe-block-label">食材清单</text>
            </view>
            <view class="ingredient-list">
              <view
                class="ingredient-item"
                v-for="(ingredient, index) in optimizationResult.ingredients"
                :key="index"
              >
                <text class="ingredient-dot">·</text>
                <text class="ingredient-text">{{ ingredient }}</text>
              </view>
            </view>
          </view>

          <!-- 步骤列表 -->
          <view class="recipe-block" v-if="optimizationResult.steps.length">
            <view class="recipe-block-title">
              <text class="recipe-block-icon">👩🍳</text>
              <text class="recipe-block-label">步骤列表</text>
            </view>
            <view class="step-list">
              <view
                class="step-item"
                v-for="(step, index) in optimizationResult.steps"
                :key="index"
              >
                <view class="step-index">{{ index + 1 }}</view>
                <text class="step-text">{{ step }}</text>
              </view>
            </view>
          </view>

          <!-- 营养对比 -->
          <view class="nutrition-comparison">
            <view class="comparison-item">
              <view class="comparison-label">卡路里</view>
              <view class="comparison-bars">
                <view class="bar-container">
                  <view class="bar original" :style="{ width: '100%' }"></view>
                </view>
                <text class="arrow">→</text>
                <view class="bar-container">
                  <view
                    class="bar optimized"
                    :style="{ width: getCalorieReduction() + '%' }"
                  ></view>
                </view>
              </view>
              <view class="comparison-values">
                <text class="value original">{{ optimizationResult.originalCalories }}</text>
                <text class="value optimized">{{ optimizationResult.optimizedCalories }}</text>
              </view>
            </view>
          </view>

          <!-- 优化建议列表 -->
          <view class="suggestions-list">
            <view
              class="suggestion-item"
              v-for="(suggestion, index) in optimizationResult.suggestions"
              :key="index"
            >
              <view class="suggestion-header">
                <text class="suggestion-icon">{{ suggestion.icon }}</text>
                <text class="suggestion-title">{{ suggestion.title }}</text>
              </view>
              <text class="suggestion-content">{{ suggestion.content }}</text>
            </view>
          </view>

          <!-- 估算说明 -->
          <text
            class="estimation-note"
            v-if="optimizationResult.estimationNote"
          >{{ optimizationResult.estimationNote }}</text>
        </view>

        <!-- 操作按钮 -->
        <view class="result-actions">
          <button class="action-btn primary" @click="saveRecipe">
            💾 保存优化食谱
          </button>
          <button class="action-btn secondary" @click="shareResult">
            📤 分享
          </button>
        </view>
      </scroll-view>

    <!-- 可滚动内容：历史记录 -->
    <scroll-view v-if="!optimizationResult && historyList.length > 0" class="scrollable-content" scroll-y>
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
              <text class="history-goal">{{ getGoalLabel(item.goal) }}</text>
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
import { ref, computed, onMounted } from 'vue'
import { formatTime } from '@/utils/helper'
import { aiApi } from '@/api/modules/ai'

const RECIPE_TIMEOUT_BUFFER_MS = 5000

// 优化目标选项
const optimizationOptions = ref([
  { key: 'balanced', label: '营养均衡', icon: '⚖️' },
  { key: 'fat_loss', label: '减脂', icon: '🔥' },
  { key: 'muscle_gain', label: '增肌', icon: '💪' },
  { key: 'blood_sugar_control', label: '控糖', icon: '🥗' }
])

// 选中的优化目标
const selectedGoal = ref('')

// 食谱文本
const recipeText = ref('')

// 是否正在优化
const isOptimizing = ref(false)

// 后端统一食谱优化超时配置
const recipeTimeoutMs = ref(0)

// 优化结果
const optimizationResult = ref(null)

// 历史记录
const historyList = ref([])

/**
 * 选择优化目标
 */
const selectGoal = (goal) => {
  selectedGoal.value = goal
}

/**
 * 清空文本
 */
const clearText = () => {
  recipeText.value = ''
}

/**
 * 读取食谱优化超时配置
 */
const loadRecipeCapabilities = async () => {
  try {
    const res = await aiApi.getCapabilities()
    const capabilities = res?.data || res
    const timeout = Number(capabilities?.recipeStreamTimeoutMs)

    if (!Number.isFinite(timeout) || timeout <= 0) {
      throw new Error('AI 能力状态缺少超时配置')
    }

    recipeTimeoutMs.value = timeout
    return timeout
  } catch (error) {
    recipeTimeoutMs.value = 0
    console.warn('读取 AI 能力失败:', error)
    return 0
  }
}

/**
 * 将接口错误转换为页面统一文案
 */
const getOptimizationErrorMessage = (error) => {
  if (error?.timedOut || error?.code === 'AI_TIMEOUT') {
    return '优化超时，请稍后重试'
  }

  if (error?.statusCode === 401) {
    return '登录已过期，请重新登录'
  }

  if (error?.statusCode === 403) {
    return '无权使用此功能，请重新登录后重试'
  }

  if (error?.statusCode === 429) {
    return '使用次数已达上限，请稍后再试'
  }

  return error?.message || '优化失败，请重试'
}

/**
 * 把统一契约里的 ingredients 字符串解析成展示用列表。
 * 兼容换行、中英文逗号、顿号、分号等常见分隔符。
 */
const parseIngredientList = (value) => {
  if (typeof value !== 'string') return []
  return value
    .split(/[\n\r,，、;；]+/)
    .map((item) => item.trim())
    .filter(Boolean)
}

/**
 * 把统一契约里的 steps 字符串解析成步骤列表。
 * 优先按换行拆分；单行时再按行内编号（1. / 2、/ 3) 等）拆分。
 */
const parseStepList = (value) => {
  if (typeof value !== 'string') return []
  const text = value.trim()
  if (!text) return []
  const lines = text
    .split(/[\n\r]+/)
    .map((item) => item.trim())
    .filter(Boolean)
  const segments = lines.length > 1 ? lines : [text]
  return segments
    .flatMap((line) => line.split(/(?=(?:^|\s)\d{1,2}\s*[.、)．])/))
    .map((item) => item.trim())
    .filter(Boolean)
}

/**
 * 优化食谱
 */
const optimizeRecipe = async () => {
  if (!recipeText.value.trim() || !selectedGoal.value) return

  isOptimizing.value = true

  try {
    const timeout = recipeTimeoutMs.value || await loadRecipeCapabilities()
    if (!timeout) {
      throw new Error('AI 能力状态读取失败，请稍后重试')
    }

    const res = await aiApi.optimizeRecipe(
      {
        recipe: recipeText.value,
        goal: selectedGoal.value
      },
      {
        timeout: timeout + RECIPE_TIMEOUT_BUFFER_MS,
        showError: false
      }
    )

    // 后端返回 ResponseResult 格式: { success: true, code: "200", data: {...} }
    const data = res.data || res

    if (!data || data.error) {
      throw new Error(data?.message || '优化失败')
    }

    const num = (v) => Number(v) || 0
    const orig = data.nutrition?.original || {}
    const opt = data.nutrition?.optimized || {}
    const ICONS = ['💡', '🥗', '🔥', '💪', '⚖️']

    optimizationResult.value = {
      recipeName: data.recipe?.name || '优化食谱',
      ingredients: parseIngredientList(data.recipe?.ingredients),
      steps: parseStepList(data.recipe?.steps),
      originalCalories: num(orig.calorie),
      originalProtein: num(orig.protein),
      originalFat: num(orig.fat),
      originalCarb: num(orig.carb),
      optimizedCalories: num(opt.calorie),
      optimizedProtein: num(opt.protein),
      optimizedFat: num(opt.fat),
      optimizedCarb: num(opt.carb),
      suggestions: Array.isArray(data.improvements)
        ? data.improvements.map((item, index) => ({
            icon: ICONS[index % ICONS.length],
            title: typeof item === 'string' ? item : (item.title || ''),
            content: typeof item === 'string' ? '' : (item.detail || '')
          }))
        : [],
      estimationNote: data.estimationNote || ''
    }

    // 添加到历史记录
    historyList.value.unshift({
      goal: selectedGoal.value,
      recipe: recipeText.value,
      result: optimizationResult.value,
      time: formatTime(new Date())
    })

    // 限制历史记录数量
    if (historyList.value.length > 10) {
      historyList.value = historyList.value.slice(0, 10)
    }

    uni.showToast({
      title: '优化完成！',
      icon: 'success'
    })
  } catch (error) {
    console.error('优化失败:', error)
    uni.showToast({
      title: getOptimizationErrorMessage(error),
      icon: 'none'
    })
  } finally {
    isOptimizing.value = false
  }
}

onMounted(loadRecipeCapabilities)

/**
 * 计算卡路里减少百分比
 */
const getCalorieReduction = () => {
  if (!optimizationResult.value) return 0
  const original = optimizationResult.value.originalCalories
  if (!original) return 0
  const reduction = original - optimizationResult.value.optimizedCalories
  return Math.round((reduction / original) * 100)
}

/**
 * 获取目标标签
 */
const getGoalLabel = (goal) => {
  const option = optimizationOptions.value.find(o => o.key === goal)
  return option ? option.label : goal
}

/**
 * 加载历史记录
 */
const loadHistory = (item) => {
  selectedGoal.value = item.goal
  recipeText.value = item.recipe
  optimizationResult.value = item.result
}

/**
 * 保存食谱
 */
const saveRecipe = () => {
  uni.showToast({
    title: '已保存到我的食谱',
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

.recipe-optimization {
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

.option-group {
  margin-bottom: $spacing-lg;
}

.option-label {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-medium;
  margin-bottom: $spacing-md;
}

.option-list {
  @include flex-center;
  flex-wrap: wrap;
  gap: $spacing-sm;
}

.option-item {
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

.option-icon {
  font-size: 44rpx;
}

.option-text {
  font-size: $font-size-sm;
  color: $text-color-regular;
}

.option-item.active .option-text {
  color: $primary-color;
  font-weight: $font-weight-medium;
}

/* 食谱输入 */
.recipe-input {
  margin-bottom: $spacing-lg;
}

.input-label {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-medium;
  margin-bottom: $spacing-sm;
}

.recipe-textarea {
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

.clear-btn {
  padding: $spacing-xs $spacing-md;
  background: rgba(255, 107, 53, 0.1);
  color: $primary-color;
  border-radius: $border-radius-round;
  font-size: $font-size-sm;
  border: none;
}

/* 优化按钮 */
.optimize-btn {
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
  max-height: 1000rpx;
}

/* 分析卡片 */
.analysis-card,
.optimized-card {
  background: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  margin-bottom: $spacing-md;
  box-shadow: $box-shadow-sm;
}

.card-header {
  @include flex-center;
  gap: $spacing-sm;
  margin-bottom: $spacing-md;
}

.card-icon {
  font-size: $font-size-xl;
}

.card-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.card-recipe-name {
  font-size: $font-size-base;
  color: $text-color-secondary;
}

/* 食材清单 / 步骤列表 */
.recipe-block {
  margin-top: $spacing-lg;
}

.recipe-block-title {
  @include flex-center;
  justify-content: flex-start;
  gap: $spacing-xs;
  margin-bottom: $spacing-sm;
}

.recipe-block-icon {
  font-size: $font-size-lg;
}

.recipe-block-label {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $text-color-primary;
}

.ingredient-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.ingredient-item {
  display: flex;
  align-items: flex-start;
  gap: $spacing-xs;
}

.ingredient-dot {
  flex-shrink: 0;
  color: $primary-color;
  font-weight: $font-weight-bold;
  line-height: $line-height-lg;
}

.ingredient-text {
  flex: 1;
  font-size: $font-size-sm;
  color: $text-color-regular;
  line-height: $line-height-lg;
}

.step-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.step-item {
  display: flex;
  align-items: flex-start;
  gap: $spacing-sm;
}

.step-index {
  flex-shrink: 0;
  width: 36rpx;
  height: 36rpx;
  @include flex-center;
  margin-top: 4rpx;
  border-radius: $border-radius-round;
  background: rgba(237, 106, 66, 0.12);
  color: $primary-color;
  font-size: $font-size-xs;
  font-weight: $font-weight-bold;
}

.step-text {
  flex: 1;
  font-size: $font-size-sm;
  color: $text-color-regular;
  line-height: $line-height-lg;
}

.analysis-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-md;
}

.analysis-item {
  @include flex-center-column;
  gap: $spacing-xs;
  padding: $spacing-md;
  background: $bg-color-base;
  border-radius: $border-radius-base;
}

.analysis-label {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.analysis-value {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $primary-color;
}

/* 营养对比 */
.nutrition-comparison {
  margin-bottom: $spacing-lg;
}

.comparison-item {
  margin-bottom: $spacing-md;
}

.comparison-label {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-medium;
  margin-bottom: $spacing-sm;
}

.comparison-bars {
  @include flex-center;
  gap: $spacing-sm;
  margin-bottom: $spacing-xs;
}

.bar-container {
  flex: 1;
  height: 24rpx;
  background: $bg-color-base;
  border-radius: 12rpx;
  overflow: hidden;
}

.bar {
  height: 100%;
  border-radius: 12rpx;
  transition: width 0.6s ease;

  &.original {
    background: linear-gradient(90deg, #ff9a9e, #fad0c4);
  }

  &.optimized {
    background: linear-gradient(90deg, #a8edea, #fed6e3);
  }
}

.arrow {
  font-size: $font-size-lg;
  color: $text-color-secondary;
}

.comparison-values {
  @include flex-center;
  gap: $spacing-lg;
}

.value {
  font-size: $font-size-base;
  font-weight: $font-weight-bold;

  &.original {
    color: #ff6b6b;
  }

  &.optimized {
    color: #67c23a;
  }
}

/* 建议列表 */
.suggestions-list {
  margin-top: $spacing-lg;
}

.suggestion-item {
  padding: $spacing-md;
  background: $bg-color-base;
  border-radius: $border-radius-base;
  margin-bottom: $spacing-sm;

  &:last-child {
    margin-bottom: 0;
  }
}

.suggestion-header {
  @include flex-center;
  gap: $spacing-xs;
  margin-bottom: $spacing-xs;
}

.suggestion-icon {
  font-size: $font-size-lg;
}

.suggestion-title {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $text-color-primary;
}

.suggestion-content {
  font-size: $font-size-sm;
  color: $text-color-regular;
  line-height: $line-height-lg;
}

.estimation-note {
  display: block;
  margin-top: $spacing-md;
  font-size: $font-size-sm;
  color: $text-color-secondary;
  line-height: $line-height-lg;
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
}

.history-goal {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-medium;
}

.history-time {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.history-arrow {
  font-size: $font-size-xl;
  color: $text-color-placeholder;
}
</style>
