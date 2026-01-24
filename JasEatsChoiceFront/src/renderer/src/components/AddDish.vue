<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Food as SilverwareIcon,
  DataAnalysis as ScaleIcon,
  Bowl as BowlIcon
} from '@element-plus/icons-vue'

// 接收从父组件传递的 props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  recipe: {
    type: Object,
    default: null
  }
})

// 定义事件
const emit = defineEmits(['close', 'add', 'update:visible'])

// 新菜品输入
const newDish = ref({
  name: '',
  ingredients: [], // 食材列表
  calories: 0,
  protein: 0,
  carbs: 0,
  fat: 0
})

// 食材输入
const newIngredient = ref('')

// 添加食材
const addIngredient = () => {
  if (newIngredient.value.trim()) {
    // 防止重复添加
    if (!newDish.value.ingredients.includes(newIngredient.value.trim())) {
      newDish.value.ingredients.push(newIngredient.value.trim())
    }
    newIngredient.value = '' // 清空输入
  }
}

// 移除食材
const removeIngredient = (index) => {
  newDish.value.ingredients.splice(index, 1)
}

// 验证菜品名称格式的函数
const isValidDishName = (name) => {
  // 允许中文、英文、数字、空格以及常见的标点符号
  const nameRegex = /^[\u4e00-\u9fa5a-zA-Z0-9\s\-\_\(\)\[\]\{\}\/\.\,，。！？；：]*$/
  return nameRegex.test(name.trim())
}

// 确认添加菜品
const confirmAddDish = () => {
  if (props.recipe && newDish.value.name.trim()) {
    // 验证菜品名称格式
    if (!isValidDishName(newDish.value.name)) {
      ElMessage.error('菜品名称只能包含中文、英文、数字和常见符号')
      return
    }

    // 如果有食材，将菜品和食材一起保存
    const dishWithIngredients = {
      name: newDish.value.name,
      ingredients: [...newDish.value.ingredients],
      calories: newDish.value.calories,
      protein: newDish.value.protein,
      carbs: newDish.value.carbs,
      fat: newDish.value.fat
    }

    // 发送添加事件
    emit('add', props.recipe, dishWithIngredients)

    // 重置表单
    resetForm()

    // 关闭对话框
    emit('close')
  } else {
    ElMessage.error('请输入菜品名称')
  }
}

// 处理关闭事件
const handleClose = () => {
  resetForm()
  emit('close')
}

// 重置表单
const resetForm = () => {
  newDish.value = {
    name: '',
    ingredients: [],
    calories: 0,
    protein: 0,
    carbs: 0,
    fat: 0
  }
  newIngredient.value = ''
}
</script>

<template>
  <el-dialog
    :model-value="visible"
    :title="recipe ? `为${recipe.name}添加菜品` : '添加菜品'"
    width="520px"
    top="10%"
    @update:model-value="emit('update:visible', $event)"
    @close="handleClose"
    transition="dialog-fade"
  >
    <div v-if="recipe" class="add-dish-form">
      <el-form class="form-container">
        <el-form-item label="菜品名称" class="is-required">
          <template #label>
            <div class="form-item-label">
              <el-icon class="label-icon"><SilverwareIcon /></el-icon>
              <span>菜品名称</span>
            </div>
          </template>
          <el-input
            v-model="newDish.name"
            placeholder="请输入新菜品名称"
            @blur="
              () => {
                if (newDish.name.trim() && !isValidDishName(newDish.name)) {
                  ElMessage.error('菜品名称只能包含中文、英文、数字和常见符号')
                }
              }
            "
          />
        </el-form-item>

        <!-- 食材输入区域 -->
        <el-form-item label="食材（非必选）">
          <template #label>
            <div class="form-item-label">
              <el-icon class="label-icon"><BowlIcon /></el-icon>
              <span>食材（非必选）</span>
            </div>
          </template>
          <div class="ingredients-input">
            <el-input
              v-model="newIngredient"
              placeholder="请输入食材名称"
              @keyup.enter="addIngredient"
            />
            <el-button type="primary" size="small" @click="addIngredient"> 添加食材 </el-button>
          </div>

          <!-- 食材列表 -->
          <div v-if="newDish.ingredients.length > 0" class="ingredients-list">
            <el-tag
              v-for="(ingredient, index) in newDish.ingredients"
              :key="index"
              closable
              @close="removeIngredient(index)"
            >
              {{ ingredient }}
            </el-tag>
          </div>
        </el-form-item>

        <!-- 营养数据编辑 -->
        <el-form-item label="营养数据">
          <template #label>
            <div class="form-item-label">
              <el-icon class="label-icon"><ScaleIcon /></el-icon>
              <span>营养数据</span>
            </div>
          </template>
          <div class="nutrition-edit-section">
            <div class="nutrition-input-group">
              <div class="nutrition-label">卡路里</div>
              <el-input-number
                v-model="newDish.calories"
                :min="0"
                :precision="0"
                style="width: 100%"
                placeholder="(kcal)"
              />
            </div>
            <div class="nutrition-input-group">
              <div class="nutrition-label">蛋白质</div>
              <el-input-number
                v-model="newDish.protein"
                :min="0"
                :precision="1"
                style="width: 100%"
                placeholder="(g)"
              />
            </div>
            <div class="nutrition-input-group">
              <div class="nutrition-label">碳水化合物</div>
              <el-input-number
                v-model="newDish.carbs"
                :min="0"
                :precision="1"
                style="width: 100%"
                placeholder="(g)"
              />
            </div>
            <div class="nutrition-input-group">
              <div class="nutrition-label">脂肪</div>
              <el-input-number
                v-model="newDish.fat"
                :min="0"
                :precision="1"
                style="width: 100%"
                placeholder="(g)"
              />
            </div>
          </div>
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="confirmAddDish"> 确定 </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
/* 表单容器 */
.add-dish-form {
  padding: 30px 0;
  max-width: 460px;
  margin: 0 auto;

  .form-container {
    background: white;
    padding: 0;
    border-radius: 0;
    border: none;
  }

  /* 表单字段样式 */
  :deep(.el-form-item) {
    margin-bottom: 32px; /* 增加字段间距 */
  }
}

/* 自定义Dialog样式 */
:deep(.el-dialog__header) {
  border-bottom: 2px solid rgba(102, 126, 234, 0.3);
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  padding: 24px 28px;
}

:deep(.el-dialog__title) {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

:deep(.el-dialog__body) {
  padding: 32px 28px;
}

/* 表单标签样式 */
:deep(.el-form-item__label) {
  font-weight: 500 !important;
  font-size: 14px !important;
  color: #555 !important;
}

/* 带图标的标签样式 */
.form-item-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.label-icon {
  font-size: 18px;
  color: #667eea;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  vertical-align: middle;
}

/* 输入框样式 */
:deep(.el-input__wrapper),
:deep(.el-select__wrapper),
:deep(.el-textarea__inner) {
  border-radius: 8px;
  border: 2px solid #e5e7eb;
  transition: all 0.3s ease;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

:deep(.el-input__wrapper:hover),
:deep(.el-select__wrapper:hover) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-select__wrapper.is-focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
}

/* 食材输入区域样式 */
.ingredients-input {
  display: flex;
  gap: 12px;
  align-items: flex-start; /* 改为顶部对齐 */
  margin-top: 4px;
  padding-top: 2px; /* 添加顶部内边距，微调对齐 */
}

/* 添加食材按钮样式 */
:deep(.ingredients-input .el-button) {
  padding: 8px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  border-radius: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  &:hover {
    background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
    box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
    transform: translateY(-2px);
  }
}

/* 食材列表样式 */
.ingredients-list {
  margin-top: 16px;

  :deep(.el-tag) {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    color: white;
    opacity: 0.9;
    transition: all 0.3s ease;
    margin-bottom: 8px;

    &:hover {
      opacity: 1;
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    }
  }
}

/* 营养编辑区域样式 */
.nutrition-edit-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-top: 20px;
}

.nutrition-input-group {
  margin-bottom: 0;

  :deep(.el-input-number) {
    width: 100%;
    border-radius: 8px;
  }

  :deep(.el-input-number__wrapper) {
    border: 2px solid #e5e7eb;
    transition: all 0.3s ease;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);

    &:hover {
      border-color: #667eea;
      box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
    }

    &.is-focus {
      border-color: #667eea;
      box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
    }
  }

  /* 营养数据标签 */
  .nutrition-label {
    font-size: 12px;
    font-weight: 500;
    color: #666;
    margin-bottom: 8px;
  }
}

/* 弹窗动画 */
.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.dialog-fade-enter-from {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
}

.dialog-fade-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

/* 按钮样式 */
:deep(.dialog-footer) {
  text-align: center;
  padding: 0 28px 24px;
}

:deep(.dialog-footer .el-button) {
  padding: 10px 28px;
  border-radius: 8px;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

:deep(.dialog-footer .el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

:deep(.dialog-footer .el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
}

:deep(.dialog-footer .el-button--default) {
  border-color: #e5e7eb;
}

:deep(.dialog-footer .el-button--default:hover) {
  border-color: #667eea;
  color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}
</style>
