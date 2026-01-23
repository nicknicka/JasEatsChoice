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
            <el-icon :size="48"><Camera /></el-icon>
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
              >
                <el-icon><Delete /></el-icon>
                删除图片
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 识别进度条 -->
      <div v-if="recognitionLoading" class="recognition-progress">
        <el-progress
          :percentage="recognitionProgress"
          :stroke-width="12"
        />
        <p class="progress-text">正在识别菜品，请稍候...</p>
      </div>

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

      <div v-if="recognitionResult" class="recognition-result">
        <div class="result-header">
          <h4>✨ 识别结果</h4>
        </div>
        <div class="result-cards">
          <div class="result-card main-card">
            <div class="card-label">菜品名称</div>
            <div class="card-value">{{ recognitionResult.name }}</div>
          </div>
          <div class="result-card calories-card">
            <div class="card-label">🔥 卡路里</div>
            <div class="card-value highlight">{{ recognitionResult.calories }} kcal</div>
          </div>
          <div class="result-card">
            <div class="card-label">👨‍🍳 难度</div>
            <div class="card-value">{{ recognitionResult.difficulty }}</div>
          </div>
          <div class="result-card">
            <div class="card-label">⏱️ 烹饪时间</div>
            <div class="card-value">{{ recognitionResult.preparationTime }}</div>
          </div>

          <!-- 营养成分图表 -->
          <div class="result-card full-width nutrition-card">
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
                    :style="{ width: recognitionResult.protein + '%' }"
                  ></div>
                </div>
                <div class="nutrition-value">{{ recognitionResult.protein }}g</div>
              </div>
              <div class="nutrition-item">
                <div class="nutrition-label">
                  <span class="nutrition-icon">🧈</span>
                  <span>脂肪</span>
                </div>
                <div class="nutrition-bar">
                  <div
                    class="nutrition-fill fat"
                    :style="{ width: recognitionResult.fat + '%' }"
                  ></div>
                </div>
                <div class="nutrition-value">{{ recognitionResult.fat }}g</div>
              </div>
              <div class="nutrition-item">
                <div class="nutrition-label">
                  <span class="nutrition-icon">🍞</span>
                  <span>碳水</span>
                </div>
                <div class="nutrition-bar">
                  <div
                    class="nutrition-fill carbs"
                    :style="{ width: recognitionResult.carbs + '%' }"
                  ></div>
                </div>
                <div class="nutrition-value">{{ recognitionResult.carbs }}g</div>
              </div>
            </div>
          </div>

          <div class="result-card full-width">
            <div class="card-label">🥗 主要食材</div>
            <div class="card-value">
              <el-tag
                v-for="ingredient in recognitionResult.ingredients"
                :key="ingredient"
                class="ingredient-tag"
              >
                {{ ingredient }}
              </el-tag>
            </div>
          </div>
          <div class="result-card full-width">
            <div class="card-label">🏷️ 标签</div>
            <div class="card-value">
              <el-tag
                v-for="tag in recognitionResult.tags"
                :key="tag"
                type="success"
                class="tag-item"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Camera, Delete, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { validateImageFile } from '../../../../utils/imageValidator'
import { logger } from '../../../../config/chatConfig'

// 状态
const selectedImage = ref(null)
const selectedFile = ref(null)
const recognitionLoading = ref(false)
const recognitionProgress = ref(0)
const recognitionResult = ref(null)
const isDragging = ref(false)

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
    const response = await fetch('http://localhost:8080/api/v1/ai/dish-recognize', {
      method: 'POST',
      body: formData
    })

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
  background-color: #fff;
  border-radius: 16px;
  padding: 24px;
  box-sizing: border-box;
  box-shadow: 0 2px 16px 0 rgba(0, 0, 0, 0.06);

  .upload-area {
    margin-bottom: 24px;

    .upload-zone {
      border: 3px dashed #ff6b6b;
      border-radius: 16px;
      padding: 48px;
      text-align: center;
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      background: linear-gradient(135deg, #fff9fa 0%, #fff 100%);

      &:hover {
        border-color: #ff5252;
        background: linear-gradient(135deg, #ffe8e8 0%, #fff 100%);
        transform: scale(1.01);
        box-shadow: 0 4px 16px rgba(255, 107, 107, 0.15);
      }

      &.has-image {
        padding: 0;
        border-style: solid;
        border-width: 2px;
      }

      &.is-dragging {
        border-color: #409eff;
        background: linear-gradient(135deg, #e3f2fd 0%, #fff 100%);
        transform: scale(1.02);
      }

      .upload-placeholder {
        .el-icon {
          color: #ff6b6b;
          margin-bottom: 16px;
          font-size: 56px;
        }

        .upload-text {
          font-size: 17px;
          font-weight: 600;
          color: #303133;
          margin: 12px 0;
        }

        .upload-hint {
          font-size: 14px;
          color: #909399;
        }
      }

      .image-preview {
        position: relative;
        width: 100%;
        height: 320px;
        overflow: hidden;
        border-radius: 12px;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
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
          transition: opacity 0.3s ease;

          &:hover {
            opacity: 1;
          }
        }
      }
    }
  }

  .recognition-progress {
    margin: 20px 0;
    padding: 20px;
    background-color: #f0f9ff;
    border-radius: 12px;

    .progress-text {
      text-align: center;
      margin-top: 10px;
      font-size: 14px;
      color: #409eff;
      font-weight: 500;
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
      background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
      border: none;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      border-radius: 14px;

      &:hover:not(:disabled) {
        transform: translateY(-3px);
        box-shadow: 0 8px 20px rgba(255, 107, 107, 0.4);
      }

      &:disabled {
        background: linear-gradient(135deg, #d3d4d6 0%, #c8c9cc 100%);
        cursor: not-allowed;
      }
    }
  }

  .recognition-result {
    animation: resultFadeIn 0.5s ease-out;

    .result-header {
      text-align: center;
      margin-bottom: 28px;

      h4 {
        font-size: 22px;
        font-weight: 700;
        color: #303133;
        margin: 0;
      }
    }

    .result-cards {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 18px;

      .result-card {
        background: linear-gradient(135deg, #fff 0%, #fff9fa 100%);
        border: 2px solid #ffe0e3;
        border-radius: 16px;
        padding: 24px;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

        &:hover {
          transform: translateY(-6px);
          box-shadow: 0 8px 24px rgba(255, 107, 107, 0.2);
          border-color: #ff6b6b;
        }

        &.main-card {
          grid-column: 1 / -1;
          background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
          border: none;

          .card-label {
            color: rgba(255, 255, 255, 0.95);
            font-size: 15px;
          }

          .card-value {
            color: #fff;
            font-size: 28px;
            font-weight: 700;
          }
        }

        &.calories-card {
          .card-value.highlight {
            color: #ff6b6b;
            font-size: 32px;
            font-weight: 700;
          }
        }

        &.full-width {
          grid-column: 1 / -1;
        }

        .card-label {
          font-size: 14px;
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
            font-size: 13px;
            font-weight: 500;
            color: #606266;

            .nutrition-icon {
              font-size: 18px;
            }
          }

          .nutrition-bar {
            flex: 1;
            height: 24px;
            background-color: #f0f2f5;
            border-radius: 12px;
            overflow: hidden;
            position: relative;

            .nutrition-fill {
              height: 100%;
              border-radius: 12px;
              transition: width 0.6s ease-out;
              position: relative;

              &.protein {
                background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
              }

              &.fat {
                background: linear-gradient(90deg, #f093fb 0%, #f5576c 100%);
              }

              &.carbs {
                background: linear-gradient(90deg, #4facfe 0%, #00f2fe 100%);
              }
            }
          }

          .nutrition-value {
            flex: 0 0 50px;
            text-align: right;
            font-size: 14px;
            font-weight: bold;
            color: #303133;
          }
        }
      }
    }
  }
}

@keyframes resultFadeIn {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(10px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}
</style>
