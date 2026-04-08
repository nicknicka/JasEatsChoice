<template>
  <div class="recognition-content-wrapper">
    <!-- 顶部装饰条 -->
    <div class="page-header-strip">
      <div class="strip-pattern"></div>
    </div>

    <div class="recognition-section">
      <!-- 上传区域 -->
      <div class="upload-area">
        <input
          type="file"
          accept="image/*"
          style="display: none"
          id="dish-image-upload"
          @change="handleImageUpload"
        />
        <div
          class="upload-zone"
          :class="{
            'has-image': selectedImage,
            'is-dragging': isDragging
          }"
          @click="triggerUpload"
          @dragover="handleDragOver"
          @dragleave="handleDragLeave"
          @drop="handleDrop"
        >
          <div v-if="!selectedImage" class="upload-placeholder">
            <div class="upload-icon-ring">
              <div class="ring-dashed"></div>
              <div class="ring-inner">
                <svg class="camera-svg" viewBox="0 0 48 48" fill="none">
                  <path d="M8 18C8 15.7909 9.79086 14 12 14H14.5L17 10H31L33.5 14H36C38.2091 14 40 15.7909 40 18V34C40 36.2091 38.2091 38 36 38H12C9.79086 38 8 36.2091 8 34V18Z" stroke="currentColor" stroke-width="2.5" stroke-linejoin="round"/>
                  <circle cx="24" cy="25" r="6" stroke="currentColor" stroke-width="2.5"/>
                  <circle cx="24" cy="25" r="2.5" fill="currentColor"/>
                </svg>
              </div>
            </div>
            <div class="upload-text-group">
              <p class="upload-title">拖放或点击上传菜品图片</p>
              <p class="upload-hint">JPG / PNG，最大 10MB</p>
            </div>
            <div class="upload-decorations">
              <span class="deco-dot"></span>
              <span class="deco-dot"></span>
              <span class="deco-dot"></span>
            </div>
          </div>
          <div v-else class="image-preview">
            <img :src="selectedImage" alt="菜品图片" />
            <div class="image-overlay">
              <button class="overlay-btn change-btn" @click.stop="triggerUpload">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
                  <circle cx="12" cy="13" r="4"/>
                </svg>
                更换图片
              </button>
              <button class="overlay-btn delete-btn" @click.stop="clearImage">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6"/>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                </svg>
                删除
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 识别进度 -->
      <Transition name="progress-slide">
        <div v-if="recognitionLoading" class="recognition-progress">
          <div class="progress-track">
            <div class="progress-fill" :style="{ width: recognitionProgress + '%' }">
              <div class="progress-glow"></div>
            </div>
          </div>
          <div class="progress-info">
            <div class="progress-dot-wave">
              <span></span><span></span><span></span><span></span>
            </div>
            <span class="progress-label">AI 正在分析您的菜品</span>
          </div>
        </div>
      </Transition>

      <!-- 操作按钮 -->
      <div class="action-bar">
        <button
          class="action-btn primary-btn"
          @click="recognizeDish"
          :disabled="!selectedImage || recognitionLoading"
        >
          <span class="btn-bg"></span>
          <span class="btn-content">
            <svg v-if="recognitionLoading" class="spin-icon" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2.5">
              <path d="M12 2v4m0 12v4m-7.07-3.93l2.83-2.83m8.48-8.48l2.83-2.83M2 12h4m12 0h4M4.93 4.93l2.83 2.83m8.48 8.48l2.83 2.83"/>
            </svg>
            <svg v-else viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2.5">
              <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
            </svg>
            {{ recognitionLoading ? '识别中...' : '开始识别' }}
          </span>
        </button>

        <button
          v-if="recognitionResult"
          class="action-btn outline-btn"
          @click="reRecognize"
          :disabled="recognitionLoading"
        >
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.5">
            <polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
          </svg>
          重新识别
        </button>
      </div>

      <!-- 识别结果 -->
      <Transition name="result-reveal">
        <div v-if="recognitionResult" class="recognition-result">
          <!-- 菜品名称 - 大标题 -->
          <div class="dish-hero">
            <div class="dish-name-label">识别结果</div>
            <h2 class="dish-name">{{ recognitionResult.name }}</h2>
            <div class="dish-meta-row">
              <div class="meta-chip">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M12 23c-3.866 0-7-2.686-7-6 0-2.918 2.163-5.475 4-7.5l1-1c.3-.3.7-.3 1 0l1 1c1.837 2.025 4 4.582 4 7.5 0 3.314-3.134 6-7 6z"/></svg>
                <span>{{ recognitionResult.difficulty }}</span>
              </div>
              <div class="meta-chip">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10 10-4.5 10-10S17.5 2 12 2zm0 18c-4.4 0-8-3.6-8-8s3.6-8 8-8 8 3.6 8 8-3.6 8-8 8zm.5-13H11v6l5.2 3.2.8-1.3-4.5-2.7V7z"/></svg>
                <span>{{ recognitionResult.preparationTime }}</span>
              </div>
            </div>
          </div>

          <!-- 卡路里突出展示 -->
          <div class="calorie-showcase">
            <div class="calorie-ring">
              <svg viewBox="0 0 120 120">
                <circle class="ring-bg" cx="60" cy="60" r="52" />
                <circle
                  class="ring-fill"
                  cx="60" cy="60" r="52"
                  :style="{ strokeDasharray: 326.7, strokeDashoffset: 326.7 - (326.7 * Math.min(recognitionResult.calories / 800, 1)) }"
                />
              </svg>
              <div class="calorie-center">
                <span class="calorie-number"><AnimatedNumber :value="recognitionResult.calories" /></span>
                <span class="calorie-unit">千卡</span>
              </div>
            </div>
            <div class="calorie-label">
              <span class="fire-icon">
                <svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M12 23c-3.866 0-7-2.686-7-6 0-2.918 2.163-5.475 4-7.5l1-1c.3-.3.7-.3 1 0l1 1c1.837 2.025 4 4.582 4 7.5 0 3.314-3.134 6-7 6z"/></svg>
              </span>
              卡路里
            </div>
          </div>

          <!-- 营养成分 -->
          <div class="nutrition-section">
            <div class="section-title">
              <span class="title-bar"></span>
              营养成分
            </div>
            <div class="nutrition-grid">
              <div class="nutrition-item protein-item">
                <div class="nutrition-header">
                  <span class="nutrition-dot protein-dot"></span>
                  <span class="nutrition-name">蛋白质</span>
                </div>
                <div class="nutrition-bar-track">
                  <div class="nutrition-bar-fill protein-fill" :style="{ width: proteinBarWidth + '%' }"></div>
                </div>
                <div class="nutrition-val"><AnimatedNumber :value="displayProtein" /><span class="val-unit">g</span></div>
              </div>
              <div class="nutrition-item fat-item">
                <div class="nutrition-header">
                  <span class="nutrition-dot fat-dot"></span>
                  <span class="nutrition-name">脂肪</span>
                </div>
                <div class="nutrition-bar-track">
                  <div class="nutrition-bar-fill fat-fill" :style="{ width: fatBarWidth + '%' }"></div>
                </div>
                <div class="nutrition-val"><AnimatedNumber :value="displayFat" /><span class="val-unit">g</span></div>
              </div>
              <div class="nutrition-item carbs-item">
                <div class="nutrition-header">
                  <span class="nutrition-dot carbs-dot"></span>
                  <span class="nutrition-name">碳水</span>
                </div>
                <div class="nutrition-bar-track">
                  <div class="nutrition-bar-fill carbs-fill" :style="{ width: carbsBarWidth + '%' }"></div>
                </div>
                <div class="nutrition-val"><AnimatedNumber :value="displayCarbs" /><span class="val-unit">g</span></div>
              </div>
            </div>
          </div>

          <!-- 食材 -->
          <div v-if="recognitionResult.ingredients?.length" class="info-section">
            <div class="section-title">
              <span class="title-bar"></span>
              主要食材
            </div>
            <div class="tags-wrap">
              <TransitionGroup name="chip-pop">
                <span
                  v-for="(ingredient, index) in recognitionResult.ingredients"
                  :key="ingredient"
                  class="ingredient-chip"
                  :style="{ animationDelay: (index * 60) + 'ms' }"
                >
                  {{ ingredient }}
                </span>
              </TransitionGroup>
            </div>
          </div>

          <!-- 标签 -->
          <div v-if="recognitionResult.tags?.length" class="info-section">
            <div class="section-title">
              <span class="title-bar"></span>
              标签
            </div>
            <div class="tags-wrap">
              <TransitionGroup name="chip-pop">
                <span
                  v-for="(tag, index) in recognitionResult.tags"
                  :key="tag"
                  class="tag-chip"
                  :style="{ animationDelay: (index * 60) + 'ms' }"
                >
                  {{ tag }}
                </span>
              </TransitionGroup>
            </div>
          </div>
        </div>
      </Transition>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, h } from 'vue'
import { Camera, Delete, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { validateImageFile } from '../../../../utils/imageValidator'
import { logger } from '../../../../config/chatConfig'
import { API_CONFIG } from '../../../../config'

// 数字滚动动画组件
const AnimatedNumber = {
  props: ['value', 'suffix'],
  setup(props) {
    const displayValue = ref(0)
    const targetValue = ref(props.value || 0)

    // 动画函数
    const animateValue = (start, end, duration) => {
      const range = end - start
      const startTime = performance.now()

      const update = (currentTime) => {
        const elapsed = currentTime - startTime
        const progress = Math.min(elapsed / duration, 1)

        // 使用 easeOutQuart 缓动函数
        const easeOutQuart = 1 - Math.pow(1 - progress, 4)
        displayValue.value = Math.round(start + range * easeOutQuart)

        if (progress < 1) {
          requestAnimationFrame(update)
        }
      }

      requestAnimationFrame(update)
    }

    // 监听 value 变化
    watch(() => props.value, (newVal) => {
      targetValue.value = newVal || 0
      animateValue(displayValue.value, targetValue.value, 800)
    }, { immediate: true })

    return {
      displayValue
    }
  },
  render() {
    return h('span', `${this.displayValue}${this.suffix || ''}`)
  }
}

// 状态
const selectedImage = ref(null)
const selectedFile = ref(null)
const recognitionLoading = ref(false)
const recognitionProgress = ref(0)
const recognitionResult = ref(null)
const isDragging = ref(false)

const normalizeRecognitionMessage = (result, fallbackMessage) => {
  return result?.message || result?.msg || result?.data?.reason || result?.reason || fallbackMessage
}

const isNonDishRecognitionResult = (result) => {
  if (!result) {
    return false
  }

  const payload = result.data || result
  return result.code === '4001' || result.code === 4001 || result.notDish === true || result.isDish === false || payload?.notDish === true || payload?.isDish === false
}

const mapRecognitionResult = (data) => ({
  name: data.name || '未知菜品',
  calories: Number(data.calories) || 0,
  protein: Number(data.protein) || 0,
  fat: Number(data.fat) || 0,
  carbs: Number(data.carbs) || 0,
  difficulty: data.difficulty || '中等',
  preparationTime: data.preparationTime || '30分钟',
  ingredients: Array.isArray(data.ingredients) ? data.ingredients : [],
  tags: Array.isArray(data.tags) ? data.tags : [],
  confidence: Number(data.confidence) || 0,
  nutritionScore: Number(data.nutritionScore) || 0
})

// 营养条宽度动画
const proteinBarWidth = ref(0)
const fatBarWidth = ref(0)
const carbsBarWidth = ref(0)

// 营养数值显示
const displayProtein = ref(0)
const displayFat = ref(0)
const displayCarbs = ref(0)

// 监听识别结果，触发营养条动画
watch(recognitionResult, (newResult) => {
  if (newResult) {
    // 延迟启动营养条动画
    setTimeout(() => {
      proteinBarWidth.value = newResult.protein || 0
      fatBarWidth.value = newResult.fat || 0
      carbsBarWidth.value = newResult.carbs || 0

      displayProtein.value = newResult.protein || 0
      displayFat.value = newResult.fat || 0
      displayCarbs.value = newResult.carbs || 0
    }, 400) // 等待卡片淡入动画完成后
  } else {
    // 重置
    proteinBarWidth.value = 0
    fatBarWidth.value = 0
    carbsBarWidth.value = 0
    displayProtein.value = 0
    displayFat.value = 0
    displayCarbs.value = 0
  }
})

/**
 * 触发文件上传
 */
const triggerUpload = () => {
  document.getElementById('dish-image-upload').click()
}

/**
 * 处理图片上传
 */
const handleImageUpload = (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件
  const validation = validateImageFile(file)
  if (!validation.valid) {
    ElMessage.error(validation.error)
    return
  }

  selectedFile.value = file
  selectedImage.value = URL.createObjectURL(file)
  recognitionResult.value = null
  ElMessage.success('图片上传成功')
}

/**
 * 清空图片
 */
const clearImage = () => {
  if (selectedImage.value) {
    URL.revokeObjectURL(selectedImage.value)
  }
  selectedImage.value = null
  selectedFile.value = null
  recognitionResult.value = null
}

/**
 * 拖拽处理
 */
const handleDragOver = (event) => {
  event.preventDefault()
  isDragging.value = true
}

const handleDragLeave = (event) => {
  event.preventDefault()
  isDragging.value = false
}

const handleDrop = (event) => {
  event.preventDefault()
  isDragging.value = false

  const file = event.dataTransfer.files[0]
  if (file) {
    const validation = validateImageFile(file)
    if (!validation.valid) {
      ElMessage.error(validation.error)
      return
    }

    selectedFile.value = file
    selectedImage.value = URL.createObjectURL(file)
    recognitionResult.value = null
    ElMessage.success('图片上传成功')
  }
}

/**
 * 识别菜品
 */
const recognizeDish = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先上传菜品图片')
    return
  }

  recognitionLoading.value = true
  recognitionProgress.value = 0

  // 模拟进度条
  const progressInterval = setInterval(() => {
    if (recognitionProgress.value < 90) {
      recognitionProgress.value += 10
    }
  }, 150)

  try {
    logger.log('开始识别菜品，文件名:', selectedFile.value.name)

    // 构建FormData
    const formData = new FormData()
    formData.append('image', selectedFile.value)

    // 调用后端API
    const apiUrl = API_CONFIG.baseURL + API_CONFIG.ai.recognizeDish
    console.log('🔍 开始识别菜品，API URL:', apiUrl)
    console.log('📤 发送图片:', selectedFile.value.name, selectedFile.value.size, 'bytes')

    const response = await fetch(apiUrl, {
      method: 'POST',
      body: formData
    })

    console.log('📡 后端响应状态:', response.status, response.statusText)

    if (!response.ok) {
      const errorText = await response.text()
      console.error('❌ 请求失败:', response.status, errorText)
      throw new Error(`请求失败 (${response.status}): ${errorText}`)
    }

    const result = await response.json()

    const responseData = result?.data || null
    const nonDishMessage = normalizeRecognitionMessage(result, '请上传菜品图片')

    if (isNonDishRecognitionResult(result) || responseData?.isDish === false) {
      clearInterval(progressInterval)
      recognitionLoading.value = false
      recognitionProgress.value = 0
      recognitionResult.value = null
      ElMessage.warning(nonDishMessage || '请上传菜品图片')
      logger.warn('⚠️ 非菜品图片:', nonDishMessage)
      return
    }

    if ((result.success === true || result.code === '200') && responseData) {
      clearInterval(progressInterval)
      recognitionProgress.value = 100

      // 映射后端返回的数据结构
      recognitionResult.value = mapRecognitionResult(responseData)

      recognitionLoading.value = false
      ElMessage.success('识别成功！')
      logger.log('✅ 菜品识别完成:', recognitionResult.value.name)
    } else {
      throw new Error(normalizeRecognitionMessage(result, '识别失败'))
    }
  } catch (error) {
    clearInterval(progressInterval)
    recognitionLoading.value = false
    recognitionProgress.value = 0
    const errorMessage = error?.message || '识别失败'
    if (/菜品|食物图片/.test(errorMessage)) {
      ElMessage.warning(errorMessage)
      logger.warn('⚠️ 非菜品图片:', errorMessage)
      return
    }

    ElMessage.error('识别失败：' + errorMessage)
    logger.error('❌ 菜品识别失败:', error)
  }
}

/**
 * 重新识别
 */
const reRecognize = () => {
  recognitionResult.value = null
  recognizeDish()
}
</script>

<style scoped lang="less">
@import '../../../../assets/css/nordic-theme.less';

.recognition-content-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  flex: 1;
  overflow: hidden;
  min-height: 0;
  box-sizing: border-box;
  background: @nordic-bg;
}

// 顶部装饰条
.page-header-strip {
  height: 4px;
  flex-shrink: 0;
  background: linear-gradient(90deg, @nordic-accent, @nordic-yellow, @nordic-green, @nordic-accent);
  background-size: 200% 100%;
  animation: gradientShift 4s ease infinite;

  .strip-pattern {
    height: 100%;
    background: repeating-linear-gradient(
      90deg,
      transparent,
      transparent 8px,
      rgba(255,255,255,0.3) 8px,
      rgba(255,255,255,0.3) 10px
    );
  }
}

@keyframes gradientShift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.recognition-section {
  width: 100%;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 28px 24px;
  box-sizing: border-box;

  // 自定义滚动条
  &::-webkit-scrollbar {
    width: 5px;
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  &::-webkit-scrollbar-thumb {
    background: @nordic-border;
    border-radius: 3px;
  }
}

// ===== 上传区域 =====
.upload-area {
  margin-bottom: 20px;
}

.upload-zone {
  border: 2px dashed @nordic-border;
  border-radius: 20px;
  padding: 52px 32px;
  text-align: center;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  background: @nordic-surface;
  position: relative;
  overflow: hidden;

  // 背景装饰点阵
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background-image: radial-gradient(circle, @nordic-divider 1px, transparent 1px);
    background-size: 24px 24px;
    opacity: 0.5;
    transition: opacity 0.3s;
  }

  &:hover {
    border-color: @nordic-accent;
    box-shadow: 0 8px 32px rgba(212, 132, 90, 0.12);
    transform: translateY(-2px);

    &::before {
      opacity: 0.3;
    }

    .upload-icon-ring {
      transform: scale(1.05);
    }

    .ring-dashed {
      animation: ringRotate 12s linear infinite;
    }
  }

  &.has-image {
    padding: 0;
    border-style: solid;
    border-color: @nordic-border;
    border-radius: 16px;

    &::before {
      display: none;
    }

    &:hover {
      transform: translateY(-1px);
    }
  }

  &.is-dragging {
    border-color: @nordic-accent;
    background: @nordic-accent-light;
    transform: scale(1.01);
  }
}

@keyframes ringRotate {
  to { transform: rotate(360deg); }
}

.upload-placeholder {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.upload-icon-ring {
  position: relative;
  width: 80px;
  height: 80px;
  transition: transform 0.3s ease;

  .ring-dashed {
    position: absolute;
    inset: 0;
    border: 2.5px dashed @nordic-accent;
    border-radius: 50%;
    opacity: 0.4;
  }

  .ring-inner {
    position: absolute;
    inset: 8px;
    border-radius: 50%;
    background: linear-gradient(135deg, @nordic-accent-light, @nordic-surface);
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 16px rgba(212, 132, 90, 0.15);
  }

  .camera-svg {
    width: 36px;
    height: 36px;
    color: @nordic-accent;
  }
}

.upload-text-group {
  .upload-title {
    font-family: 'Georgia', 'Palatino', serif;
    font-size: 17px;
    font-weight: 600;
    color: @nordic-text;
    margin: 0 0 6px;
    letter-spacing: -0.3px;
  }

  .upload-hint {
    font-size: 13px;
    color: @nordic-text-muted;
    margin: 0;
    letter-spacing: 0.3px;
  }
}

.upload-decorations {
  display: flex;
  gap: 6px;

  .deco-dot {
    width: 5px;
    height: 5px;
    border-radius: 50%;
    background: @nordic-accent;
    opacity: 0.3;
  }
}

// 图片预览
.image-preview {
  position: relative;
  width: 100%;
  height: 320px;
  overflow: hidden;
  border-radius: 14px;
  background: @nordic-surface;
  animation: imgReveal 0.5s cubic-bezier(0.4, 0, 0.2, 1);

  img {
    width: 100%;
    height: 100%;
    object-fit: contain;
    transition: transform 0.6s ease;
  }

  &:hover img {
    transform: scale(1.03);
  }
}

@keyframes imgReveal {
  from { opacity: 0; transform: scale(0.97); }
  to { opacity: 1; transform: scale(1); }
}

.image-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0,0,0,0) 40%, rgba(0,0,0,0.65) 100%);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding-bottom: 20px;
  gap: 12px;
  opacity: 0;
  transition: opacity 0.3s ease;

  &:hover {
    opacity: 1;
  }
}

.overlay-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px;
  border: none;
  border-radius: 30px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
  backdrop-filter: blur(8px);

  &.change-btn {
    background: rgba(255,255,255,0.2);
    color: #fff;
    border: 1px solid rgba(255,255,255,0.3);

    &:hover {
      background: rgba(255,255,255,0.35);
    }
  }

  &.delete-btn {
    background: rgba(212, 75, 75, 0.6);
    color: #fff;

    &:hover {
      background: rgba(212, 75, 75, 0.85);
    }
  }
}

// ===== 进度条 =====
.recognition-progress {
  margin-bottom: 20px;
  padding: 20px 24px;
  background: @nordic-surface;
  border-radius: 14px;
  border: 1px solid @nordic-border;
  animation: slideDown 0.35s ease-out;
}

.progress-track {
  height: 6px;
  background: @nordic-divider;
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 14px;
}

.progress-fill {
  height: 100%;
  border-radius: 3px;
  background: linear-gradient(90deg, @nordic-accent, @nordic-yellow);
  transition: width 0.3s ease;
  position: relative;

  .progress-glow {
    position: absolute;
    right: 0;
    top: -3px;
    width: 12px;
    height: 12px;
    border-radius: 50%;
    background: @nordic-accent;
    box-shadow: 0 0 12px @nordic-accent;
  }
}

.progress-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.progress-dot-wave {
  display: flex;
  gap: 4px;
  span {
    width: 5px;
    height: 5px;
    border-radius: 50%;
    background: @nordic-accent;
    animation: wave 1.2s ease-in-out infinite;

    &:nth-child(2) { animation-delay: 0.1s; }
    &:nth-child(3) { animation-delay: 0.2s; }
    &:nth-child(4) { animation-delay: 0.3s; }
  }
}

@keyframes wave {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-6px); opacity: 1; }
}

.progress-label {
  font-size: 13px;
  color: @nordic-text-secondary;
  font-weight: 500;
}

// ===== 操作按钮 =====
.action-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.action-btn {
  flex: 1;
  height: 50px;
  border: none;
  border-radius: 14px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  letter-spacing: -0.2px;

  &.primary-btn {
    background: linear-gradient(135deg, @nordic-accent, @nordic-accent-dark);
    color: #fff;
    box-shadow: 0 4px 16px rgba(212, 132, 90, 0.25);

    .btn-bg {
      position: absolute;
      inset: 0;
      background: linear-gradient(135deg, rgba(255,255,255,0.15), transparent);
      opacity: 0;
      transition: opacity 0.3s;
    }

    &:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgba(212, 132, 90, 0.35);

      .btn-bg {
        opacity: 1;
      }
    }

    &:disabled {
      background: @nordic-border;
      box-shadow: none;
      cursor: not-allowed;
      color: @nordic-text-muted;
    }
  }

  &.outline-btn {
    background: @nordic-surface;
    border: 2px solid @nordic-border;
    color: @nordic-text-secondary;
    flex: 0 0 auto;
    padding: 0 24px;

    &:hover:not(:disabled) {
      border-color: @nordic-accent;
      color: @nordic-accent;
      background: @nordic-accent-light;
    }
  }

  .spin-icon {
    animation: spin 1s linear infinite;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

// ===== 识别结果 =====
.recognition-result {
  animation: resultReveal 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes resultReveal {
  from {
    opacity: 0;
    transform: translateY(24px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 菜品大标题
.dish-hero {
  background: @nordic-surface;
  border-radius: 18px;
  padding: 32px 28px 28px;
  margin-bottom: 16px;
  border: 1px solid @nordic-border;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(90deg, @nordic-accent, @nordic-yellow);
  }
}

.dish-name-label {
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 2px;
  color: @nordic-accent;
  font-weight: 700;
  margin-bottom: 8px;
}

.dish-name {
  font-family: 'Georgia', 'Palatino', serif;
  font-size: 32px;
  font-weight: 700;
  color: @nordic-text;
  margin: 0 0 16px;
  letter-spacing: -1px;
  line-height: 1.2;
}

.dish-meta-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.meta-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 14px;
  border-radius: 30px;
  background: @nordic-bg;
  font-size: 13px;
  color: @nordic-text-secondary;
  font-weight: 500;

  svg {
    color: @nordic-accent;
  }
}

// 卡路里展示
.calorie-showcase {
  background: linear-gradient(135deg, #FFF8F3 0%, #FFF1E8 100%);
  border-radius: 18px;
  padding: 28px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 24px;
  border: 1px solid rgba(212, 132, 90, 0.15);
}

.calorie-ring {
  position: relative;
  width: 110px;
  height: 110px;
  flex-shrink: 0;

  svg {
    width: 100%;
    height: 100%;
    transform: rotate(-90deg);
  }

  .ring-bg {
    fill: none;
    stroke: rgba(212, 132, 90, 0.12);
    stroke-width: 8;
  }

  .ring-fill {
    fill: none;
    stroke: @nordic-accent;
    stroke-width: 8;
    stroke-linecap: round;
    transition: stroke-dashoffset 1s cubic-bezier(0.4, 0, 0.2, 1);
    filter: drop-shadow(0 2px 4px rgba(212, 132, 90, 0.3));
  }
}

.calorie-center {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  .calorie-number {
    font-family: 'Georgia', serif;
    font-size: 30px;
    font-weight: 700;
    color: @nordic-accent-dark;
    line-height: 1;
    letter-spacing: -1px;
  }

  .calorie-unit {
    font-size: 12px;
    color: @nordic-text-muted;
    margin-top: 4px;
    font-weight: 500;
  }
}

.calorie-label {
  display: flex;
  flex-direction: column;
  gap: 4px;

  .fire-icon {
    color: @nordic-accent;
    display: flex;
  }

  font-size: 15px;
  font-weight: 600;
  color: @nordic-text;
}

// 营养成分
.nutrition-section {
  background: @nordic-surface;
  border-radius: 18px;
  padding: 24px 28px;
  margin-bottom: 16px;
  border: 1px solid @nordic-border;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 700;
  color: @nordic-text;
  margin-bottom: 18px;
  letter-spacing: -0.3px;

  .title-bar {
    width: 3px;
    height: 16px;
    border-radius: 2px;
    background: @nordic-accent;
    flex-shrink: 0;
  }
}

.nutrition-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.nutrition-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nutrition-header {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 0 0 72px;
}

.nutrition-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;

  &.protein-dot { background: #7C6FD0; }
  &.fat-dot { background: #E07B7B; }
  &.carbs-dot { background: #5DADE2; }
}

.nutrition-name {
  font-size: 13px;
  font-weight: 500;
  color: @nordic-text-secondary;
}

.nutrition-bar-track {
  flex: 1;
  height: 10px;
  background: @nordic-bg;
  border-radius: 5px;
  overflow: hidden;
}

.nutrition-bar-fill {
  height: 100%;
  border-radius: 5px;
  transition: width 1.2s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(90deg, transparent 0%, rgba(255,255,255,0.3) 50%, transparent 100%);
    animation: barShimmer 2s ease-in-out infinite;
  }

  &.protein-fill {
    background: linear-gradient(90deg, #7C6FD0, #A99BF0);
  }
  &.fat-fill {
    background: linear-gradient(90deg, #E07B7B, #F0A0A0);
  }
  &.carbs-fill {
    background: linear-gradient(90deg, #5DADE2, #85C7F0);
  }
}

@keyframes barShimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(200%); }
}

.nutrition-val {
  flex: 0 0 56px;
  text-align: right;
  font-size: 16px;
  font-weight: 700;
  color: @nordic-text;
  letter-spacing: -0.5px;

  .val-unit {
    font-size: 12px;
    font-weight: 500;
    color: @nordic-text-muted;
    margin-left: 1px;
  }
}

// 食材/标签区
.info-section {
  background: @nordic-surface;
  border-radius: 18px;
  padding: 24px 28px;
  margin-bottom: 16px;
  border: 1px solid @nordic-border;
}

.tags-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.ingredient-chip {
  display: inline-flex;
  align-items: center;
  padding: 7px 16px;
  border-radius: 30px;
  background: @nordic-accent-light;
  color: @nordic-accent-dark;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.25s ease;
  border: 1px solid rgba(212, 132, 90, 0.15);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(212, 132, 90, 0.15);
  }
}

.tag-chip {
  display: inline-flex;
  align-items: center;
  padding: 7px 16px;
  border-radius: 30px;
  background: @nordic-green-light;
  color: @nordic-green-dark;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.25s ease;
  border: 1px solid rgba(123, 174, 127, 0.15);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(123, 174, 127, 0.15);
  }
}

// ===== 动画 =====
.result-reveal-enter-active {
  animation: resultReveal 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}
.result-reveal-leave-active {
  animation: resultHide 0.3s ease-in forwards;
}

@keyframes resultHide {
  to {
    opacity: 0;
    transform: translateY(-12px);
  }
}

.progress-slide-enter-active {
  animation: slideDown 0.35s ease-out;
}
.progress-slide-leave-active {
  animation: slideUp 0.25s ease-in forwards;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-8px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes slideUp {
  to { opacity: 0; transform: translateY(-8px); }
}

.chip-pop-enter-active {
  animation: chipIn 0.35s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}

@keyframes chipIn {
  from {
    opacity: 0;
    transform: scale(0.6);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
