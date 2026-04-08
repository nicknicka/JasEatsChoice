<template>
  <div class="dish-generator">
    <!-- 输入区域 -->
    <div class="input-section">
      <h3>菜品信息</h3>
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
          <div class="style-options">
            <div
              v-for="style in styleOptions"
              :key="style.value"
              class="style-option"
              :class="{ active: dishForm.style === style.value }"
              @click="dishForm.style = style.value"
            >
              <el-icon :size="24"><component :is="style.icon" /></el-icon>
              <span>{{ style.label }}</span>
            </div>
          </div>
        </el-form-item>

        <el-button
          type="primary"
          :loading="isGenerating"
          :disabled="!dishForm.name"
          @click="generateDescription"
          class="generate-btn"
        >
          <el-icon v-if="!isGenerating"><MagicStick /></el-icon>
          {{ isGenerating ? '生成中...' : '生成描述' }}
        </el-button>
      </el-form>
    </div>

    <!-- 结果区域 -->
    <div class="result-section">
      <h3>生成结果</h3>

      <div v-if="generatedDescription" class="result-card">
        <div class="description-text">{{ generatedDescription }}</div>
        <div class="result-actions">
          <el-button @click="copyDescription">
            <el-icon><CopyDocument /></el-icon>
            复制
          </el-button>
          <el-button type="primary" @click="applyDescription">
            <el-icon><Check /></el-icon>
            应用到菜品
          </el-button>
        </div>
      </div>

      <div v-else class="empty-state">
        <el-icon :size="48"><Document /></el-icon>
        <p>填写菜品信息后点击生成</p>
        <p class="hint">AI将为您生成吸引人的菜品描述</p>
      </div>

      <!-- 历史记录 -->
      <div v-if="history.length" class="history-section">
        <h4>历史生成</h4>
        <div class="history-list">
          <div
            v-for="(item, index) in history"
            :key="index"
            class="history-item"
            @click="useHistory(item)"
          >
            <span class="dish-name">{{ item.name }}</span>
            <span class="style-tag">{{ getStyleLabel(item.style) }}</span>
          </div>
        </div>
      </div>
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

  try {
    const response = await api.post(AI_API.DISH_DESCRIPTION, {
      name: dishForm.name,
      ingredients: dishForm.ingredients,
      category: dishForm.category,
      style: dishForm.style
    })

    generatedDescription.value = response.data || ''

    // 添加到历史记录
    history.value.unshift({
      name: dishForm.name,
      style: dishForm.style,
      description: generatedDescription.value
    })

    // 只保留最近5条
    if (history.value.length > 5) {
      history.value.pop()
    }
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
  gap: 24px;
  height: 100%;
  padding: 16px;
}

.input-section,
.result-section {
  background: @merchant-surface;
  border-radius: 12px;
  padding: 20px;
  overflow-y: auto;

  h3 {
    margin: 0 0 20px;
    font-size: 16px;
    color: @merchant-secondary;
    padding-bottom: 12px;
    border-bottom: 2px solid @merchant-border;
  }
}

.dish-form {
  .el-form-item {
    margin-bottom: 20px;
  }
}

.style-options {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.style-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 12px;
  border: 2px solid @merchant-border;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    border-color: @merchant-secondary;
    background: @merchant-secondary-light;
  }

  &.active {
    border-color: @merchant-secondary;
    background: @merchant-secondary-light;

    .el-icon {
      color: @merchant-secondary;
    }
  }

  .el-icon {
    color: @merchant-secondary;
  }

  span {
    font-size: 13px;
    color: @merchant-text;
  }
}

.generate-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  margin-top: 8px;
}

.result-card {
  background: @merchant-secondary-light;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;

  .description-text {
    font-size: 15px;
    line-height: 1.8;
    color: @merchant-text;
    margin-bottom: 16px;
  }

  .result-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: @merchant-text-muted;

  .el-icon {
    margin-bottom: 12px;
    color: @merchant-border;
  }

  p {
    margin: 4px 0;
  }

  .hint {
    font-size: 13px;
    color: @merchant-text-muted;
  }
}

.history-section {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid @merchant-border;

  h4 {
    margin: 0 0 12px;
    font-size: 14px;
    color: @merchant-text-sec;
  }

  .history-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .history-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 12px;
    background: @merchant-surface-alt;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      background: @merchant-secondary-light;
    }

    .dish-name {
      font-size: 14px;
      color: @merchant-text;
    }

    .style-tag {
      font-size: 12px;
      color: @merchant-secondary;
      background: @merchant-secondary-light;
      padding: 2px 8px;
      border-radius: 10px;
    }
  }
}
</style>
