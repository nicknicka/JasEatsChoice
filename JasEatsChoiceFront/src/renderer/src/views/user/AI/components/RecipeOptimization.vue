<template>
  <div class="recipe-content-wrapper">
    <div class="recipe-section">
      <div class="recipe-input">
        <el-input
          v-model="originalRecipe"
          placeholder="请输入您的食谱...&#10;例如：西红柿鸡蛋的做法：1. 准备西红柿2个，鸡蛋2个；2. 煎鸡蛋；3. 炒西红柿；4. 混合翻炒"
          clearable
          resize="vertical"
          :rows="6"
          type="textarea"
          maxlength="10000"
          show-word-limit
        />
      </div>

      <el-button
        type="primary"
        size="large"
        class="optimize-btn"
        @click="optimizeRecipe"
        :disabled="!originalRecipe || optimizationLoading"
      >
        <el-icon v-if="optimizationLoading"><Loading /></el-icon>
        {{ optimizationLoading ? '优化中...' : '✨ 开始优化食谱' }}
      </el-button>

      <div v-if="optimizedRecipe" class="recipe-result">
        <div class="result-header">
          <h4>✨ 优化结果</h4>
        </div>

        <div class="recipe-comparison">
          <div class="recipe-card original-recipe-card">
            <div class="card-header">
              <span class="card-title">📝 原食谱</span>
            </div>
            <div class="card-content">
              <pre>{{ optimizedRecipe.original }}</pre>
            </div>
          </div>

          <div class="recipe-arrow">→</div>

          <div class="recipe-card optimized-recipe-card">
            <div class="card-header">
              <span class="card-title">⭐ 优化后</span>
            </div>
            <div class="card-content">
              <pre>{{ optimizedRecipe.optimized }}</pre>
            </div>
          </div>
        </div>

        <div class="improvements-section">
          <div class="improvements-title">🎯 优化亮点</div>
          <div class="improvements-tags">
            <el-tag
              v-for="improvement in optimizedRecipe.improvements"
              :key="improvement"
              size="large"
              type="success"
              effect="plain"
            >
              {{ improvement }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { API_CONFIG } from '../../../config/index'
import { validateRecipe } from '../../../utils/imageValidator'
import { handleApiError } from '../../../utils/errorHandler'
import { logger } from '../../../config/chatConfig'

// 状态
const originalRecipe = ref('')
const optimizedRecipe = ref(null)
const optimizationLoading = ref(false)

/**
 * 优化食谱
 */
const optimizeRecipe = () => {
  // 验证食谱
  const validation = validateRecipe(originalRecipe.value)
  if (!validation.valid) {
    ElMessage.warning(validation.error)
    return
  }

  optimizationLoading.value = true

  // 调用后端API
  axios
    .post(API_CONFIG.baseURL + API_CONFIG.ai.recipe, {
      foodName: originalRecipe.value
    })
    .then((response) => {
      const backendRecipes = response.data.data

      if (backendRecipes && backendRecipes.length > 0) {
        const firstRecipe = backendRecipes[0]
        optimizedRecipe.value = {
          original: originalRecipe.value,
          optimized: `推荐食谱：${firstRecipe.name}
难度：${firstRecipe.difficulty}
卡路里：${firstRecipe.calorie}大卡
食材：${firstRecipe.ingredients}
步骤：${firstRecipe.steps}`,
          improvements: ['营养均衡', '口味优化', '步骤简化']
        }
        logger.log('✅ 食谱优化成功:', firstRecipe.name)
      } else {
        optimizedRecipe.value = {
          original: originalRecipe.value,
          optimized: '优化失败：没有找到合适的优化食谱。',
          improvements: []
        }
      }
    })
    .catch((error) => {
      logger.error('❌ 食谱优化失败:', error)

      optimizedRecipe.value = {
        original: originalRecipe.value,
        optimized: handleApiError(error),
        improvements: []
      }

      ElMessage.error(handleApiError(error))
    })
    .finally(() => {
      optimizationLoading.value = false
    })
}
</script>

<style scoped lang="less">
.recipe-content-wrapper {
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

.recipe-section {
  width: 100%;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  background-color: #fff;
  border-radius: 16px;
  padding: 24px;
  box-sizing: border-box;
  box-shadow: 0 2px 16px 0 rgba(0, 0, 0, 0.06);

  .recipe-input {
    margin-bottom: 24px;

    :deep(.el-textarea__inner) {
      border-radius: 14px;
      transition: all 0.3s ease;
      font-size: 15px;
      padding: 14px 16px;

      &:focus {
        box-shadow: 0 0 0 3px rgba(255, 107, 107, 0.12);
        border-color: #ff6b6b;
      }
    }
  }

  .optimize-btn {
    width: 100%;
    height: 54px;
    font-size: 17px;
    font-weight: 600;
    margin-bottom: 24px;
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

  .recipe-result {
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

    .recipe-comparison {
      display: flex;
      align-items: stretch;
      gap: 24px;
      margin-bottom: 32px;

      @media (max-width: 768px) {
        flex-direction: column;
      }

      .recipe-card {
        flex: 1;
        background: #fff;
        border: 2px solid #ffe0e3;
        border-radius: 16px;
        overflow: hidden;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

        &:hover {
          transform: translateY(-6px);
          box-shadow: 0 8px 24px rgba(255, 107, 107, 0.2);
        }

        .card-header {
          padding: 18px 24px;
          background: linear-gradient(135deg, #fff9fa 0%, #ffe8e8 100%);
          border-bottom: 2px solid #ffe0e3;

          .card-title {
            font-size: 17px;
            font-weight: 700;
            color: #303133;
          }
        }

        .card-content {
          padding: 24px;
          max-height: 420px;
          overflow-y: auto;

          pre {
            margin: 0;
            white-space: pre-wrap;
            word-wrap: break-word;
            font-family: inherit;
            line-height: 1.9;
            color: #606266;
            font-size: 15px;
          }
        }

        &.optimized-recipe-card {
          .card-header {
            background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
            border-bottom: none;

            .card-title {
              color: #fff;
            }
          }
        }
      }

      .recipe-arrow {
        display: flex;
        align-items: center;
        font-size: 40px;
        color: #ff6b6b;
        font-weight: 700;
        flex-shrink: 0;

        @media (max-width: 768px) {
          transform: rotate(90deg);
        }
      }
    }

    .improvements-section {
      background: linear-gradient(135deg, #fff 0%, #f0f9ff 100%);
      border: 2px solid #b3e0ff;
      border-radius: 16px;
      padding: 24px;

      .improvements-title {
        font-size: 17px;
        font-weight: 700;
        color: #303133;
        margin-bottom: 18px;
      }

      .improvements-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 12px;

        .el-tag {
          padding: 12px 24px;
          font-size: 15px;
          font-weight: 600;
          border-radius: 24px;
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
