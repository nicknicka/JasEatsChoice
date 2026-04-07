<template>
  <div class="recognition-content-wrapper">
    <div class="recognition-section">
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
            <el-icon :size="48" class="camera-icon"><Camera /></el-icon>
            <p class="upload-text">点击或拖拽上传菜品图片</p>
            <p class="upload-hint">支持 JPG、PNG 格式，最大 10MB</p>
          </div>
          <div v-else class="image-preview">
            <img :src="selectedImage" alt="菜品图片" />
            <div class="image-overlay">
              <el-button
                type="danger"
                size="small"
                @click.stop="clearImage"
                class="delete-btn"
              >
                <el-icon><Delete /></el-icon>
                删除图片
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 识别进度条 -->
      <Transition name="progress-fade">
        <div v-if="recognitionLoading" class="recognition-progress">
          <div class="progress-container">
            <el-progress
              :percentage="recognitionProgress"
              :stroke-width="12"
              class="progress-bar"
            />
            <p class="progress-text">
              <span class="pulse-dot"></span>
              正在识别菜品，请稍候...
            </p>
          </div>
        </div>
      </Transition>

      <div class="recognition-buttons">
        <el-button
          type="primary"
          size="large"
          class="recognize-btn"
          @click="recognizeDish"
          :disabled="!selectedImage || recognitionLoading"
        >
          <el-icon v-if="recognitionLoading"><Loading /></el-icon>
          {{ recognitionLoading ? '识别中...' : '🔍 开始识别菜品' }}
        </el-button>

        <el-button
          v-if="recognitionResult"
          type="success"
          size="large"
          class="re-recognize-btn"
          @click="reRecognize"
          :disabled="recognitionLoading"
        >
          🔄 重新识别
        </el-button>
      </div>

      <Transition name="result-fade">
        <div v-if="recognitionResult" class="recognition-result">
          <div class="result-header">
            <h4>✨ 识别结果</h4>
          </div>
          <div class="result-cards">
            <div
              class="result-card main-card"
              :style="{ 'animation-delay': '0ms' }"
            >
              <div class="card-label">菜品名称</div>
              <div class="card-value">{{ recognitionResult.name }}</div>
            </div>
            <div
              class="result-card calories-card"
              :style="{ 'animation-delay': '100ms' }"
            >
              <div class="card-label">🔥 卡路里</div>
              <div class="card-value highlight">
                <AnimatedNumber :value="recognitionResult.calories" suffix=" kcal" />
              </div>
            </div>
            <div
              class="result-card"
              :style="{ 'animation-delay': '200ms' }"
            >
              <div class="card-label">👨‍🍳 难度</div>
              <div class="card-value">{{ recognitionResult.difficulty }}</div>
            </div>
            <div
              class="result-card"
              :style="{ 'animation-delay': '300ms' }"
            >
              <div class="card-label">⏱️ 烹饪时间</div>
              <div class="card-value">{{ recognitionResult.preparationTime }}</div>
            </div>

            <!-- 营养成分图表 -->
            <div
              class="result-card full-width nutrition-card"
              :style="{ 'animation-delay': '400ms' }"
            >
              <div class="card-label">📊 营养成分</div>
              <div class="nutrition-chart">
                <div class="nutrition-item">
                  <div class="nutrition-label">
                    <span class="nutrition-icon">💪</span>
                    <span>蛋白质</span>
                  </div>
                  <div class="nutrition-bar">
                    <div
                      class="nutrition-fill protein"
                      :style="{ width: proteinBarWidth + '%' }"
                    ></div>
                  </div>
                  <div class="nutrition-value">
                    <AnimatedNumber :value="displayProtein" suffix="g" />
                  </div>
                </div>
                <div class="nutrition-item">
                  <div class="nutrition-label">
                    <span class="nutrition-icon">🧈</span>
                    <span>脂肪</span>
                  </div>
                  <div class="nutrition-bar">
                    <div
                      class="nutrition-fill fat"
                      :style="{ width: fatBarWidth + '%' }"
                    ></div>
                  </div>
                  <div class="nutrition-value">
                    <AnimatedNumber :value="displayFat" suffix="g" />
                  </div>
                </div>
                <div class="nutrition-item">
                  <div class="nutrition-label">
                    <span class="nutrition-icon">🍞</span>
                    <span>碳水</span>
                  </div>
                  <div class="nutrition-bar">
                    <div
                      class="nutrition-fill carbs"
                      :style="{ width: carbsBarWidth + '%' }"
                    ></div>
                  </div>
                  <div class="nutrition-value">
                    <AnimatedNumber :value="displayCarbs" suffix="g" />
                  </div>
                </div>
              </div>
            </div>

            <div
              class="result-card full-width"
              :style="{ 'animation-delay': '500ms' }"
            >
              <div class="card-label">🥗 主要食材</div>
              <div class="card-value">
                <TransitionGroup name="tag-fade">
                  <el-tag
                    v-for="(ingredient, index) in recognitionResult.ingredients"
                    :key="ingredient"
                    class="ingredient-tag"
                    :style="{ 'animation-delay': (index * 50) + 'ms' }"
                  >
                    {{ ingredient }}
                  </el-tag>
                </TransitionGroup>
              </div>
            </div>
            <div
              class="result-card full-width"
              :style="{ 'animation-delay': '600ms' }"
            >
              <div class="card-label">🏷️ 标签</div>
              <div class="card-value">
                <TransitionGroup name="tag-fade">
                  <el-tag
                    v-for="(tag, index) in recognitionResult.tags"
                    :key="tag"
                    type="success"
                    class="tag-item"
                    :style="{ 'animation-delay': (index * 50) + 'ms' }"
                  >
                    {{ tag }}
                  </el-tag>
                </TransitionGroup>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
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
  template: `
    <span>{{ displayValue }}{{ suffix }}</span>
  `
}

// 状态
const selectedImage = ref(null)
const selectedFile = ref(null)
const recognitionLoading = ref(false)
const recognitionProgress = ref(0)
const recognitionResult = ref(null)
const isDragging = ref(false)

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
    // 可选：添加用户ID（如果有的话）
    // formData.append('userId', 'xxx')

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

    if (result.code === '200' && result.data) {
      clearInterval(progressInterval)
      recognitionProgress.value = 100

      // 映射后端返回的数据结构
      recognitionResult.value = {
        name: result.data.name,
        calories: result.data.calories,
        protein: result.data.protein,
        fat: result.data.fat,
        carbs: result.data.carbs,
        difficulty: result.data.difficulty,
        preparationTime: result.data.preparationTime,
        ingredients: result.data.ingredients || [],
        tags: result.data.tags || [],
        confidence: result.data.confidence,
        nutritionScore: result.data.nutritionScore
      }

      recognitionLoading.value = false
      ElMessage.success('识别成功！')
      logger.log('✅ 菜品识别完成:', recognitionResult.value.name)
    } else {
      throw new Error(result.msg || '识别失败')
    }
  } catch (error) {
    clearInterval(progressInterval)
    recognitionLoading.value = false
    recognitionProgress.value = 0
    ElMessage.error('识别失败：' + error.message)
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
  gap: 8px;
  overflow: hidden;
  min-height: 0;
  box-sizing: border-box;
}

.recognition-section {
  width: 100%;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 24px;
  box-sizing: border-box;

  .upload-area {
    margin-bottom: 24px;

    .upload-zone {
      border: 3px dashed @nordic-accent;
      border-radius: 16px;
      padding: 48px;
      text-align: center;
      cursor: pointer;
      transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
      background: linear-gradient(135deg, @nordic-accent-light 0%, #fff 100%);
      position: relative;
      overflow: hidden;

      &::before {
        content: '';
        position: absolute;
        top: 50%;
        left: 50%;
        width: 0;
        height: 0;
        border-radius: 50%;
        background: radial-gradient(circle, rgba(212, 132, 90, 0.1) 0%, transparent 70%);
        transform: translate(-50%, -50%);
        transition: width 0.6s ease, height 0.6s ease;
      }

      &:hover {
        border-color: @nordic-accent-dark;
        background: linear-gradient(135deg, @nordic-accent-light 0%, #fff 100%);
        transform: scale(1.02);
        box-shadow: 0 8px 24px rgba(212, 132, 90, 0.2);

        &::before {
          width: 600px;
          height: 600px;
        }

        .camera-icon {
          animation: cameraBounce 0.6s ease;
        }
      }

      &.has-image {
        padding: 0;
        border-style: solid;
        border-width: 2px;

        &:hover {
          transform: scale(1.01);
        }
      }

      &.is-dragging {
        border-color: #409eff;
        background: linear-gradient(135deg, #e3f2fd 0%, #fff 100%);
        transform: scale(1.02);
        animation: dragPulse 1s ease-in-out infinite;
      }

      .upload-placeholder {
        .camera-icon {
          color: @nordic-accent;
          margin-bottom: 16px;
          font-size: 56px;
          display: inline-block;
        }

        .upload-text {
          font-size: 17px;
          font-weight: 600;
          color: #303133;
          margin: 12px 0;
        }

        .upload-hint {
          font-size: 1rem /* 原值: 14px */;
          color: #909399;
        }
      }

      .image-preview {
        position: relative;
        width: 100%;
        height: 320px;
        overflow: hidden;
        border-radius: 12px;
        animation: imageFadeIn 0.4s ease-out;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          transition: transform 0.6s ease;
        }

        &:hover img {
          transform: scale(1.05);
        }

        .image-overlay {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: rgba(0, 0, 0, 0.6);
          display: flex;
          align-items: center;
          justify-content: center;
          opacity: 0;
          transition: all 0.4s ease;
          backdrop-filter: blur(2px);

          &:hover {
            opacity: 1;
          }

          .delete-btn {
            animation: buttonPop 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55);
          }
        }
      }
    }
  }

  .recognition-progress {
    margin: 20px 0;
    padding: 20px;
    background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(64, 158, 255, 0.15);

    .progress-container {
      display: flex;
      flex-direction: column;
      align-items: center;
    }

    .progress-bar {
      width: 100%;
      margin-bottom: 12px;

      :deep(.el-progress-bar__inner) {
        background: linear-gradient(90deg, #409eff 0%, #66b1ff 100%);
        transition: all 0.3s ease;
        box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
      }
    }

    .progress-text {
      text-align: center;
      font-size: 1rem /* 原值: 14px */;
      color: #409eff;
      font-weight: 500;
      display: flex;
      align-items: center;
      gap: 8px;

      .pulse-dot {
        display: inline-block;
        width: 8px;
        height: 8px;
        background: #409eff;
        border-radius: 50%;
        animation: pulse 1.5s ease-in-out infinite;
      }
    }
  }

  .recognition-buttons {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;

    .recognize-btn,
    .re-recognize-btn {
      flex: 1;
      height: 54px;
      font-size: 17px;
      font-weight: 600;
      background: linear-gradient(135deg, @nordic-accent 0%, @nordic-accent-dark 100%);
      border: none;
      transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
      border-radius: 14px;
      position: relative;
      overflow: hidden;

      &::before {
        content: '';
        position: absolute;
        top: 50%;
        left: 50%;
        width: 0;
        height: 0;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.3);
        transform: translate(-50%, -50%);
        transition: width 0.6s, height 0.6s;
      }

      &:hover:not(:disabled) {
        transform: translateY(-3px);
        box-shadow: 0 12px 28px rgba(212, 132, 90, 0.5);

        &::before {
          width: 500px;
          height: 500px;
        }
      }

      &:active:not(:disabled) {
        transform: translateY(-1px);
        box-shadow: 0 6px 16px rgba(212, 132, 90, 0.4);
      }

      &:disabled {
        background: linear-gradient(135deg, #d3d4d6 0%, #c8c9cc 100%);
        cursor: not-allowed;
        opacity: 0.7;
      }
    }
  }

  .recognition-result {
    .result-header {
      text-align: center;
      margin-bottom: 28px;
      animation: headerSlideDown 0.5s ease-out;

      h4 {
        font-size: 22px;
        font-weight: 700;
        color: #303133;
        margin: 0;
        background: linear-gradient(135deg, @nordic-accent 0%, @nordic-accent-dark 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }
    }

    .result-cards {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 18px;

      .result-card {
        background: linear-gradient(135deg, #fff 0%, @nordic-accent-light 100%);
        border: 2px solid @nordic-border;
        border-radius: 16px;
        padding: 24px;
        opacity: 0;
        animation: cardSlideUp 0.5s ease-out forwards;
        transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
        position: relative;
        overflow: hidden;

        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: -100%;
          width: 100%;
          height: 100%;
          background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
          transition: left 0.6s;
        }

        &:hover {
          transform: translateY(-8px);
          box-shadow: 0 12px 32px rgba(212, 132, 90, 0.25);
          border-color: @nordic-accent;

          &::before {
            left: 100%;
          }
        }

        &.main-card {
          grid-column: 1 / -1;
          background: linear-gradient(135deg, @nordic-accent 0%, @nordic-accent-dark 100%);
          border: none;

          &:hover {
            box-shadow: 0 12px 32px rgba(212, 132, 90, 0.4);
          }

          .card-label {
            color: rgba(255, 255, 255, 0.95);
            font-size: 1.071rem /* 原值: 15px */;
          }

          .card-value {
            color: #fff;
            font-size: 2rem /* 原值: 28px */;
            font-weight: 700;
            text-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
          }
        }

        &.calories-card {
          .card-value.highlight {
            color: @nordic-accent;
            font-size: 2.286rem /* 原值: 32px */;
            font-weight: 700;
            background: linear-gradient(135deg, @nordic-accent 0%, @nordic-accent-dark 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
          }
        }

        &.full-width {
          grid-column: 1 / -1;
        }

        .card-label {
          font-size: 1rem /* 原值: 14px */;
          font-weight: 600;
          color: #909399;
          margin-bottom: 10px;
        }

        .card-value {
          font-size: 17px;
          font-weight: 600;
          color: #303133;

          .ingredient-tag,
          .tag-item {
            margin: 5px;
            padding: 8px 14px;
            font-weight: 500;
            border-radius: 20px;
            animation: tagPopIn 0.3s ease-out forwards;
            opacity: 0;
          }
        }
      }
    }

    .nutrition-card {
      .nutrition-chart {
        margin-top: 15px;

        .nutrition-item {
          display: flex;
          align-items: center;
          gap: 15px;
          margin-bottom: 15px;

          &:last-child {
            margin-bottom: 0;
          }

          .nutrition-label {
            flex: 0 0 80px;
            display: flex;
            align-items: center;
            gap: 5px;
            font-size: 0.929rem /* 原值: 13px */;
            font-weight: 500;
            color: #606266;

            .nutrition-icon {
              font-size: 1.286rem /* 原值: 18px */;
              transition: transform 0.3s ease;
            }
          }

          .nutrition-bar {
            flex: 1;
            height: 24px;
            background-color: #f0f2f5;
            border-radius: 12px;
            overflow: hidden;
            position: relative;
            box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.05);

            .nutrition-fill {
              height: 100%;
              border-radius: 12px;
              transition: width 1.2s cubic-bezier(0.34, 1.56, 0.64, 1);
              position: relative;

              &::after {
                content: '';
                position: absolute;
                top: 0;
                left: 0;
                right: 0;
                bottom: 0;
                background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
                animation: shimmer 1.5s ease-in-out infinite;
              }

              &.protein {
                background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
                box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
              }

              &.fat {
                background: linear-gradient(90deg, #f093fb 0%, #f5576c 100%);
                box-shadow: 0 2px 8px rgba(240, 147, 251, 0.3);
              }

              &.carbs {
                background: linear-gradient(90deg, #4facfe 0%, #00f2fe 100%);
                box-shadow: 0 2px 8px rgba(79, 172, 254, 0.3);
              }
            }
          }

          .nutrition-value {
            flex: 0 0 50px;
            text-align: right;
            font-size: 1rem /* 原值: 14px */;
            font-weight: bold;
            color: #303133;
          }
        }

        .nutrition-item:hover .nutrition-icon {
          transform: scale(1.2) rotate(5deg);
        }
      }
    }
  }
}

// ============ 动画定义 ============

// 结果淡入淡出
.result-fade-enter-active {
  animation: resultFadeIn 0.5s ease-out;
}

.result-fade-leave-active {
  animation: resultFadeOut 0.3s ease-in;
}

// 进度条淡入淡出
.progress-fade-enter-active {
  animation: progressFadeIn 0.3s ease-out;
}

.progress-fade-leave-active {
  animation: progressFadeOut 0.3s ease-in;
}

// 标签淡入
.tag-fade-enter-active {
  animation: tagPopIn 0.3s ease-out forwards;
}

@keyframes resultFadeIn {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

@keyframes resultFadeOut {
  from {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
  to {
    opacity: 0;
    transform: scale(0.95) translateY(-20px);
  }
}

@keyframes progressFadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes progressFadeOut {
  from {
    opacity: 1;
    transform: translateY(0);
  }
  to {
    opacity: 0;
    transform: translateY(-10px);
  }
}

@keyframes cardSlideUp {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes tagPopIn {
  from {
    opacity: 0;
    transform: scale(0.5);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.5);
    opacity: 0.5;
  }
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

@keyframes cameraBounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

@keyframes dragPulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(64, 158, 255, 0.4);
  }
  50% {
    box-shadow: 0 0 0 10px rgba(64, 158, 255, 0);
  }
}

@keyframes buttonPop {
  0% {
    transform: scale(0);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes imageFadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes headerSlideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
