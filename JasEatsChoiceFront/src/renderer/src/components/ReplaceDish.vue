<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { API_CONFIG } from '../config'
import axios from 'axios'

// Props 定义
const props = defineProps({
  visible: {
    type: Boolean,
    required: true
  },
  recipe: {
    type: Object,
    default: null
  },
  dish: {
    type: Object,
    default: null
  }
})

// Emits 定义
const emit = defineEmits(['update:visible', 'close', 'replace'])

// 响应式数据
const replacementDishes = ref([])
const showCustomDishInput = ref(false)
const customDishName = ref('')
const loading = ref(false)

// 验证菜品名称格式的函数
const isValidDishName = (name) => {
  const nameRegex = /^[\u4e00-\u9fa5a-zA-Z0-9\s\-_\(\)\[\]\{\}\/\.\,，。！？；：]*$/
  return nameRegex.test(name.trim())
}

// 加载可替换菜品
const loadReplacementDishes = async () => {
  if (!props.recipe || !props.dish) return

  loading.value = true
  try {
    const response = await axios.get(API_CONFIG.baseURL + '/v1/dishes/replacement', {
      params: {
        type: props.recipe.type,
        exclude: props.dish.name
      }
    })

    if (response.data.code === '200' && response.data.data) {
      replacementDishes.value = response.data.data
      console.log('加载可替换菜品成功:', replacementDishes.value)
    } else {
      replacementDishes.value = []
    }
  } catch (error) {
    console.error('加载可替换菜品失败:', error)
    replacementDishes.value = []
  } finally {
    loading.value = false
  }
}

// 确认替换菜品（预设菜品）
const confirmReplaceDish = (newDish) => {
  emit('replace', {
    recipe: props.recipe,
    oldDish: props.dish,
    newDish: newDish
  })
  handleClose()
}

// 确认替换菜品（自定义菜品）
const handleCustomDishReplacement = () => {
  if (customDishName.value.trim()) {
    if (!isValidDishName(customDishName.value)) {
      ElMessage.error('菜品名称只能包含中文、英文、数字和常见符号')
      return
    }

    confirmReplaceDish({
      name: customDishName.value.trim(),
      type: props.recipe.type,
      ingredients: [],
      calories: 0,
      protein: 0,
      carbs: 0,
      fat: 0
    })
  }
}

// 关闭对话框
const handleClose = () => {
  replacementDishes.value = []
  customDishName.value = ''
  showCustomDishInput.value = false
  emit('update:visible', false)
  emit('close')
}

// 监听对话框打开，加载可替换菜品
watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      loadReplacementDishes()
    }
  }
)

// 获取餐型标签颜色
const getMealTagColor = (type) => {
  const colorMap = {
    breakfast: '#ffc107',
    lunch: '#4caf50',
    dinner: '#2196f3',
    afternoon_tea: '#9c27b0',
    tea: '#9c27b0',
    night_snack: '#1e88e5',
    snack: '#1e88e5',
    morning_snack: '#ff9800',
    brunch: '#ff9800',
    supper: '#00bcd4',
    midnight_snack: '#00bcd4',
    health_snack: '#4caf50',
    fitness_meal: '#4caf50',
    dessert: '#e91e63',
    sweet: '#e91e63',
    soup: '#009688',
    porridge: '#009688',
    salad: '#8bc34a',
    vegetable: '#8bc34a',
    meat: '#795548',
    protein: '#795548'
  }
  return colorMap[type] || '#ccc'
}
</script>

<template>
  <el-dialog
    :model-value="visible"
    :title="dish ? `替换 ${dish.name}` : '替换菜品'"
    width="500px"
    top="12%"
    @close="handleClose"
  >
    <div v-if="dish" class="replace-dish-container">
      <!-- 当前菜品 -->
      <div class="current-dish">
        <span class="detail-label">当前菜品:</span>
        <span class="detail-value">{{ dish.name }}</span>
      </div>

      <!-- 可选菜品 -->
      <div class="available-dishes">
        <span class="detail-label">可选菜品:</span>
        <div class="dish-hint">点击菜品卡片即可替换</div>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="3" animated />
        </div>

        <!-- 菜品列表 -->
        <div v-else class="dish-list" v-if="replacementDishes.length > 0">
          <el-card
            v-for="dishItem in replacementDishes"
            :key="dishItem.id"
            :class="['dish-card', dishItem.type]"
            shadow="hover"
          >
            <div class="dish-name dish-name-clickable" @click="confirmReplaceDish(dishItem)">
              {{ dishItem.name }}
            </div>
            <div class="dish-nutrition">
              {{ dishItem.calories || dishItem.calorie || 0 }}kcal
              <span v-if="dishItem.protein || dishItem.carbs || dishItem.fat">
                | 蛋白质:{{ dishItem.protein || 0 }}g
                | 碳水:{{ dishItem.carbs || 0 }}g
                | 脂肪:{{ dishItem.fat || 0 }}g
              </span>
            </div>
          </el-card>
        </div>

        <!-- 空状态 -->
        <el-empty
          v-if="!loading && replacementDishes.length === 0"
          description="暂无可替换菜品，请使用自定义菜品"
        ></el-empty>
      </div>

      <el-divider />

      <!-- 自定义菜品 -->
      <div class="custom-dish-section">
        <el-button type="text" @click="showCustomDishInput = !showCustomDishInput">
          {{ showCustomDishInput ? '▼ 收起' : '▶ 自定义菜品' }}
        </el-button>

        <div v-if="showCustomDishInput" class="custom-dish-input">
          <div class="input-row">
            <el-input
              v-model="customDishName"
              placeholder="请输入自定义菜品名称"
              clearable
              size="large"
            />
            <el-button
              type="primary"
              :disabled="!customDishName.trim()"
              @click="handleCustomDishReplacement"
            >
              确认替换
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<style scoped lang="less">
.replace-dish-container {
  .current-dish {
    margin-bottom: 20px;
    padding: 12px;
    background: linear-gradient(135deg, #fff5f5 0%, #ffebee 100%);
    border-radius: 10px;
    border-left: 4px solid #ff6b6b;

    .detail-label {
      font-weight: 700;
      font-size: 13px;
      color: #666;
      margin-right: 10px;
    }

    .detail-value {
      color: #ff6b6b;
      font-weight: 700;
      font-size: 16px;
    }
  }

  .available-dishes {
    .detail-label {
      font-weight: 700;
      font-size: 15px;
      color: #2c3e50;
      display: block;
      margin-bottom: 6px;
    }

    .dish-hint {
      color: #999;
      font-size: 11px;
      margin-bottom: 10px;
    }

    .loading-container {
      padding: 16px 0;
    }

    .dish-list {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
      gap: 12px;
    }

    .dish-card {
      border-left: 4px solid #ccc !important;
      position: relative;
      background: #ffffff !important;
      border-radius: 12px !important;
      border: 1px solid #e0e0e0 !important;

      .dish-name {
        font-size: 15px;
        font-weight: bold;
        margin-bottom: 6px;
        color: #2c3e50;
      }

      .dish-name-clickable {
        cursor: pointer;
        padding: 4px;
        margin: -4px;
        border-radius: 6px;
        transition: all 0.2s ease;

        &:hover {
          background: #f5f7ff;
          color: #667eea;
        }

        &:active {
          background: #eef2f7;
        }
      }

      .dish-nutrition {
        font-size: 13px;
        color: #666;
        line-height: 1.4;
      }
    }
  }

  .custom-dish-section {
    margin-top: 16px;

    .custom-dish-input {
      margin-top: 12px;
      padding: 16px;
      background: linear-gradient(135deg, #f8f9ff 0%, #eef2f7 100%);
      border-radius: 10px;

      .input-row {
        display: flex;
        gap: 12px;
        align-items: center;

        .el-input {
          flex: 1;
        }
      }
    }
  }
}

// 动态设置菜品卡片左边框颜色
.dish-card {
  border-left-color: v-bind('getMealTagColor(dishItem?.type)') !important;
}

// 确保卡片整体不需要点击手势
:deep(.el-card) {
  .el-card__body {
    cursor: default;
  }
}

// 对话框标题样式
:deep(.el-dialog__title) {
  font-size: 20px !important;
  font-weight: 700 !important;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  background-clip: text !important;
  -webkit-background-clip: text !important;
  color: transparent !important;
}

// 按钮样式
:deep(.el-dialog__footer) {
  .el-button {
    padding: 8px 20px;
    border-radius: 8px;
    font-weight: 600;
  }

  .el-button--primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;

    &:hover {
      background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
      box-shadow: 0 2px 6px rgba(102, 126, 234, 0.3);
    }
  }
}
</style>
