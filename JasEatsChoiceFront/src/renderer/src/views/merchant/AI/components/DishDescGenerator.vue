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

  // 模拟AI生成
  await new Promise(resolve => setTimeout(resolve, 2000))

  const { name, ingredients, style } = dishForm
  const ingredientText = ingredients.length > 0 ? ingredients.join('、') : '精选食材'

  const descriptions = {
    traditional: `【${name}】精选${ingredientText}精心烹制，传承经典做法，色香味俱全。菜品口感鲜美，回味无穷，是您不可错过的美味佳肴。每一口都能品尝到食材的鲜美与厨师的匠心。`,

    health: `【${name}】富含优质蛋白和多种维生素，低脂健康，营养均衡。选用新鲜${ingredientText}，采用健康烹饪方式，保留食材原味与营养。适合注重健康饮食的您，美味与健康兼得。`,

    story: `每一道【${name}】都承载着厨师的匠心与故事。精选${ingredientText}，经过多道工序精心烹制，只为给您带来最纯正的味觉体验。这不仅是一道菜，更是一份用心的传递，期待您的品尝。`,

    promotion: `🔥 限时特惠！【${name}】原价XX元，现价仅需XX元！精选${ingredientText}，大师级烹饪，美味不容错过！数量有限，先到先得！立即下单，享受超值优惠！`
  }

  generatedDescription.value = descriptions[style]

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

  isGenerating.value = false
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
.dish-generator {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  height: 100%;
  padding: 16px;
}

.input-section,
.result-section {
  background: #FFF;
  border-radius: 12px;
  padding: 20px;
  overflow-y: auto;

  h3 {
    margin: 0 0 20px;
    font-size: 16px;
    color: #DC2626;
    padding-bottom: 12px;
    border-bottom: 2px solid #FECACA;
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
  border: 2px solid #FECACA;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    border-color: #DC2626;
    background: #FEF2F2;
  }

  &.active {
    border-color: #DC2626;
    background: #FEE2E2;

    .el-icon {
      color: #DC2626;
    }
  }

  .el-icon {
    color: #F87171;
  }

  span {
    font-size: 13px;
    color: #374151;
  }
}

.generate-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  margin-top: 8px;
}

.result-card {
  background: #FEF2F2;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;

  .description-text {
    font-size: 15px;
    line-height: 1.8;
    color: #374151;
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
  color: #9CA3AF;

  .el-icon {
    margin-bottom: 12px;
    color: #FECACA;
  }

  p {
    margin: 4px 0;
  }

  .hint {
    font-size: 13px;
    color: #D1D5DB;
  }
}

.history-section {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #FECACA;

  h4 {
    margin: 0 0 12px;
    font-size: 14px;
    color: #6B7280;
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
    background: #FAFAFA;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      background: #FEF2F2;
    }

    .dish-name {
      font-size: 14px;
      color: #374151;
    }

    .style-tag {
      font-size: 12px;
      color: #DC2626;
      background: #FEE2E2;
      padding: 2px 8px;
      border-radius: 10px;
    }
  }
}
</style>
