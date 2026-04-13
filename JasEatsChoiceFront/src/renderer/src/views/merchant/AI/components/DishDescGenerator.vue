<template>
  <div class="dish-generator">
    <!-- 输入区域 -->
    <div class="input-section">
      <div class="section-header">
        <span class="section-icon">
          <el-icon :size="18"><EditPen /></el-icon>
        </span>
        <h3>菜品信息</h3>
      </div>
      <el-form :model="dishForm" label-position="top" class="dish-form">
        <el-form-item label="菜品名称">
          <el-input v-model="dishForm.name" placeholder="请输入菜品名称" />
        </el-form-item>

        <el-form-item label="主要食材">
          <el-select
            v-model="dishForm.ingredients"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="选择或输入食材"
          >
            <el-option label="鸡肉" value="鸡肉" />
            <el-option label="牛肉" value="牛肉" />
            <el-option label="猪肉" value="猪肉" />
            <el-option label="鱼肉" value="鱼肉" />
            <el-option label="虾" value="虾" />
            <el-option label="蔬菜" value="蔬菜" />
            <el-option label="豆腐" value="豆腐" />
            <el-option label="米饭" value="米饭" />
            <el-option label="面条" value="面条" />
          </el-select>
        </el-form-item>

        <el-form-item label="菜品分类">
          <el-select v-model="dishForm.category" placeholder="选择分类">
            <el-option label="热菜" value="热菜" />
            <el-option label="凉菜" value="凉菜" />
            <el-option label="汤品" value="汤品" />
            <el-option label="主食" value="主食" />
            <el-option label="甜点" value="甜点" />
            <el-option label="饮品" value="饮品" />
          </el-select>
        </el-form-item>

        <el-form-item label="描述风格">
          <div class="style-grid">
            <div
              v-for="style in styleOptions"
              :key="style.value"
              class="style-card"
              :class="{ active: dishForm.style === style.value }"
              @click="dishForm.style = style.value"
            >
              <div class="style-icon-wrap">
                <el-icon :size="20"><component :is="style.icon" /></el-icon>
              </div>
              <span class="style-label">{{ style.label }}</span>
            </div>
          </div>
        </el-form-item>

        <button
          class="generate-btn"
          :class="{ disabled: !dishForm.name || isGenerating }"
          @click="generateDescription"
        >
          <el-icon v-if="!isGenerating"><MagicStick /></el-icon>
          <span v-else class="btn-loader"></span>
          <span>{{ isGenerating ? '生成中...' : '生成描述' }}</span>
        </button>
      </el-form>
    </div>

    <!-- 结果区域 -->
    <div class="result-section">
      <div class="section-header">
        <span class="section-icon result-icon">
          <el-icon :size="18"><Document /></el-icon>
        </span>
        <h3>生成结果</h3>
      </div>

      <Transition name="result-fade">
        <div v-if="generatedDescription" class="result-card">
          <div class="result-glow"></div>
          <div class="description-text">{{ generatedDescription }}</div>
          <div class="result-actions">
            <button class="action-btn" @click="copyDescription">
              <el-icon :size="14"><CopyDocument /></el-icon>
              <span>复制</span>
            </button>
            <button class="action-btn primary" @click="applyDescription">
              <el-icon :size="14"><Check /></el-icon>
              <span>应用到菜品</span>
            </button>
          </div>
        </div>
      </Transition>

      <div v-if="!generatedDescription && !isGenerating" class="empty-state">
        <div class="empty-visual">
          <div class="empty-line"></div>
          <div class="empty-line short"></div>
          <div class="empty-line"></div>
          <div class="empty-line medium"></div>
        </div>
        <p class="empty-title">填写菜品信息后点击生成</p>
        <p class="empty-hint">AI将为您生成吸引人的菜品描述</p>
      </div>

      <!-- 生成中状态 -->
      <div v-if="isGenerating" class="generating-state">
        <div class="generating-lines">
          <div class="gen-line" v-for="i in 3" :key="i" :style="{ animationDelay: `${i * 0.2}s` }"></div>
        </div>
        <p>AI正在创作中...</p>
      </div>

      <!-- 历史记录 -->
      <Transition name="history-slide">
        <div v-if="history.length" class="history-section">
          <div class="history-header">
            <h4>历史生成</h4>
            <span class="history-count">{{ history.length }}</span>
          </div>
          <div class="history-list">
            <div
              v-for="(item, index) in history"
              :key="index"
              class="history-item"
              @click="useHistory(item)"
            >
              <div class="history-info">
                <span class="dish-name">{{ item.name }}</span>
                <span class="style-tag">{{ getStyleLabel(item.style) }}</span>
              </div>
              <span class="history-arrow">→</span>
            </div>
          </div>
        </div>
      </Transition>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import {
  Document,
  CopyDocument,
  Check,
  EditPen,
  StarFilled as Heart,
  MagicStick,
  Present,
  TrendCharts
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'
import { AI_API } from '@/api'

const dishForm = reactive({
  name: '',
  ingredients: [],
  category: '',
  style: 'traditional'
})

const styleOptions = [
  { value: 'traditional', label: '传统描述', icon: EditPen },
  { value: 'health', label: '营养健康', icon: Heart },
  { value: 'story', label: '情感故事', icon: MagicStick },
  { value: 'promotion', label: '促销吸引', icon: Present }
]

const isGenerating = ref(false)
const generatedDescription = ref('')
const history = ref([])

const getStyleLabel = (style) => {
  const option = styleOptions.find(s => s.value === style)
  return option ? option.label : style
}

/**
 * 生成菜品描述
 */
const generateDescription = async () => {
  if (!dishForm.name) {
    ElMessage.warning('请输入菜品名称')
    return
  }

  isGenerating.value = true
  generatedDescription.value = ''

  try {
    const response = await api.post(AI_API.DISH_DESCRIPTION, {
      name: dishForm.name,
      ingredients: dishForm.ingredients,
      category: dishForm.category,
      style: dishForm.style
    })

    generatedDescription.value = response.data || ''

    // 添加到历史记录（不可变方式）
    history.value = [
      {
        name: dishForm.name,
        style: dishForm.style,
        description: generatedDescription.value
      },
      ...history.value.slice(0, 4)
    ]
  } catch (error) {
    console.error('生成菜品描述失败:', error)
    ElMessage.error('生成菜品描述失败')
  } finally {
    isGenerating.value = false
  }
}

/**
 * 复制描述
 */
const copyDescription = () => {
  navigator.clipboard.writeText(generatedDescription.value)
  ElMessage.success('已复制到剪贴板')
}

/**
 * 应用到菜品
 */
const applyDescription = () => {
  ElMessage.success('已应用到菜品信息')
  // TODO: 实际应用逻辑，可能需要调用API更新菜品
}

/**
 * 使用历史记录
 */
const useHistory = (item) => {
  dishForm.name = item.name
  dishForm.style = item.style
  generatedDescription.value = item.description
}
</script>

<style scoped lang="less">
@import '../../../../assets/css/nordic-theme.less';
@import '../../../../assets/css/merchant-theme.less';

.dish-generator {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  height: 100%;
  padding: 20px;
}

// --- 区域通用样式 ---
.input-section,
.result-section {
  background: linear-gradient(180deg, rgba(255,255,255,0.9), rgba(255,255,255,0.7));
  border: 1px solid rgba(226, 222, 216, 0.5);
  border-radius: 16px;
  padding: 20px;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: @merchant-border;
    border-radius: 2px;
  }
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  padding-bottom: 14px;
  border-bottom: 1px solid @merchant-divider;

  .section-icon {
    width: 32px;
    height: 32px;
    border-radius: 9px;
    background: linear-gradient(135deg, @merchant-primary-light, rgba(227, 240, 228, 0.5));
    color: @merchant-primary;
    display: flex;
    align-items: center;
    justify-content: center;

    &.result-icon {
      background: linear-gradient(135deg, @merchant-secondary-light, rgba(244, 230, 222, 0.5));
      color: @merchant-secondary;
    }
  }

  h3 {
    margin: 0;
    font-size: 15px;
    font-weight: 600;
    color: @merchant-text;
  }
}

// --- 表单样式 ---
.dish-form {
  :deep(.el-form-item__label) {
    font-size: 13px;
    font-weight: 500;
    color: @merchant-text-sec;
    padding-bottom: 6px;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select .el-input__wrapper) {
    border-radius: 10px;
    box-shadow: 0 0 0 1px @merchant-border;

    &:hover {
      box-shadow: 0 0 0 1px darken(@merchant-border, 10%);
    }

    &.is-focus {
      box-shadow: 0 0 0 1.5px @merchant-primary;
    }
  }

  .el-form-item {
    margin-bottom: 18px;
  }
}

// --- 风格选择 ---
.style-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.style-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 18px 12px;
  border: 1.5px solid @merchant-border;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.22, 1, 0.36, 1);
  background: @merchant-surface;

  .style-icon-wrap {
    width: 40px;
    height: 40px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: @merchant-text-sec;
    background: rgba(226, 222, 216, 0.2);
    transition: all 0.25s ease;
  }

  .style-label {
    font-size: 13px;
    color: @merchant-text-sec;
    font-weight: 500;
    transition: color 0.25s ease;
  }

  &:hover {
    border-color: @merchant-primary;
    background: rgba(227, 240, 228, 0.15);

    .style-icon-wrap {
      color: @merchant-primary;
      background: @merchant-primary-light;
    }

    .style-label {
      color: @merchant-text;
    }
  }

  &.active {
    border-color: @merchant-primary;
    background: rgba(227, 240, 228, 0.2);

    .style-icon-wrap {
      background: linear-gradient(135deg, @merchant-primary-light, rgba(227, 240, 228, 0.5));
      color: @merchant-primary;
    }

    .style-label {
      color: @merchant-primary-dark;
      font-weight: 600;
    }
  }
}

// --- 生成按钮 ---
.generate-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 46px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, @merchant-primary, darken(@merchant-primary, 4%));
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.22, 1, 0.36, 1);
  font-family: inherit;
  box-shadow: 0 3px 12px rgba(74, 122, 77, 0.25);
  margin-top: 6px;

  &:hover:not(.disabled) {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(74, 122, 77, 0.3);
  }

  &:active:not(.disabled) {
    transform: translateY(0);
  }

  &.disabled {
    opacity: 0.5;
    cursor: not-allowed;
    box-shadow: none;
  }

  .btn-loader {
    width: 16px;
    height: 16px;
    border: 2px solid rgba(255,255,255,0.3);
    border-top-color: #fff;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }
}

// --- 结果卡片 ---
.result-card {
  position: relative;
  background: linear-gradient(135deg, rgba(227, 240, 228, 0.3), rgba(244, 230, 222, 0.2));
  border: 1px solid rgba(74, 122, 77, 0.12);
  border-radius: 14px;
  padding: 20px;
  margin-bottom: 20px;
  overflow: hidden;

  .result-glow {
    position: absolute;
    top: -20px;
    right: -20px;
    width: 60px;
    height: 60px;
    border-radius: 50%;
    background: @merchant-primary;
    opacity: 0.05;
    filter: blur(20px);
  }

  .description-text {
    font-size: 15px;
    line-height: 1.85;
    color: @merchant-text;
    margin-bottom: 16px;
    position: relative;
  }

  .result-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
  }

  .action-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    border-radius: 10px;
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.25s ease;
    font-family: inherit;
    border: 1px solid @merchant-border;
    background: @merchant-surface;
    color: @merchant-text-sec;

    &:hover {
      border-color: @merchant-primary;
      color: @merchant-primary;
    }

    &.primary {
      background: linear-gradient(135deg, @merchant-primary, darken(@merchant-primary, 4%));
      color: #fff;
      border: none;
      box-shadow: 0 2px 8px rgba(74, 122, 77, 0.2);

      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(74, 122, 77, 0.3);
      }
    }
  }
}

// --- 空状态 ---
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;

  .empty-visual {
    display: flex;
    flex-direction: column;
    gap: 10px;
    margin-bottom: 20px;
    width: 60%;
  }

  .empty-line {
    height: 10px;
    background: rgba(226, 222, 216, 0.3);
    border-radius: 5px;

    &.short { width: 40%; }
    &.medium { width: 70%; }
  }

  .empty-title {
    font-size: 14px;
    font-weight: 600;
    color: @merchant-text-sec;
    margin: 0 0 4px;
  }

  .empty-hint {
    font-size: 13px;
    color: @merchant-text-muted;
    margin: 0;
  }
}

// --- 生成中状态 ---
.generating-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px 0;
  gap: 16px;

  .generating-lines {
    display: flex;
    flex-direction: column;
    gap: 12px;
    width: 70%;
  }

  .gen-line {
    height: 12px;
    background: linear-gradient(90deg, rgba(74, 122, 77, 0.06), rgba(74, 122, 77, 0.12), rgba(74, 122, 77, 0.06));
    background-size: 200% 100%;
    border-radius: 6px;
    animation: shimmer 1.5s ease-in-out infinite;
  }

  p {
    font-size: 14px;
    color: @merchant-text-sec;
    margin: 0;
  }
}

// --- 历史记录 ---
.history-section {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid @merchant-divider;

  .history-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;

    h4 {
      margin: 0;
      font-size: 13px;
      font-weight: 600;
      color: @merchant-text-sec;
    }

    .history-count {
      width: 20px;
      height: 20px;
      border-radius: 6px;
      background: rgba(226, 222, 216, 0.3);
      color: @merchant-text-muted;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 11px;
      font-weight: 700;
    }
  }

  .history-list {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  .history-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 14px;
    background: rgba(250, 248, 245, 0.5);
    border: 1px solid transparent;
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.25s ease;

    &:hover {
      background: @merchant-surface;
      border-color: @merchant-border;

      .history-arrow {
        color: @merchant-primary;
        transform: translateX(2px);
      }
    }

    .history-info {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .dish-name {
      font-size: 13px;
      color: @merchant-text;
      font-weight: 500;
    }

    .style-tag {
      font-size: 11px;
      color: @merchant-primary;
      background: rgba(74, 122, 77, 0.08);
      padding: 2px 8px;
      border-radius: 10px;
      font-weight: 500;
    }

    .history-arrow {
      font-size: 14px;
      color: @merchant-text-muted;
      transition: all 0.2s ease;
    }
  }
}

// --- 动画 ---
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.result-fade-enter-active {
  transition: all 0.4s cubic-bezier(0.22, 1, 0.36, 1);
}

.result-fade-leave-active {
  transition: all 0.25s ease;
}

.result-fade-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.result-fade-leave-to {
  opacity: 0;
}

.history-slide-enter-active {
  transition: all 0.35s cubic-bezier(0.22, 1, 0.36, 1);
}

.history-slide-leave-active {
  transition: all 0.25s ease;
}

.history-slide-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.history-slide-leave-to {
  opacity: 0;
}
</style>
