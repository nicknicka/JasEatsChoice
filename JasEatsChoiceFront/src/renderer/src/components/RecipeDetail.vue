<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { API_CONFIG } from '../config'

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
const emit = defineEmits(['close', 'update:visible', 'update:recipe'])

// 处理关闭事件
const handleClose = () => {
  emit('close')
  emit('update:visible', false)
}

// 编辑状态
const isEditing = ref(false)
const isSaving = ref(false)

// 创建一个基准时间（2000年1月1日 0点0分）
const createBaseTime = (hours = 0, minutes = 0, seconds = 0) => {
  const date = new Date(2000, 0, 1, hours, minutes, seconds)
  return date
}

// 将字符串时间转换为 Date 对象的辅助函数
const parseTimeStringToDate = (timeString) => {
  // 如果没有时间或为空，返回默认时间（0点0分）
  if (!timeString || timeString === '00:00' || timeString === '') {
    return createBaseTime(0, 0, 0)
  }
  if (timeString instanceof Date) {
    return timeString
  }
  if (typeof timeString === 'string' && timeString.includes(':')) {
    const [hours, minutes] = timeString.split(':').map(Number)
    return createBaseTime(hours, minutes, 0)
  }
  return createBaseTime(0, 0, 0)
}

// 可编辑的食谱数据
const editableRecipe = ref({
  name: '',
  type: 'breakfast',
  cookTime: createBaseTime(0, 0, 0), // 使用基准时间作为默认值
  detail: '',
  // 自定义营养信息（如果为null则使用计算值）
  customNutrition: null
})

// 监听 props.recipe 变化，初始化可编辑数据
watch(
  () => props.recipe,
  (newRecipe) => {
    if (newRecipe) {
      // 处理 customNutrition：如果是 JSON 字符串则解析为对象
      let customNutritionObj = null
      if (newRecipe.customNutrition) {
        if (typeof newRecipe.customNutrition === 'string') {
          try {
            customNutritionObj = JSON.parse(newRecipe.customNutrition)
          } catch (e) {
            console.error('解析 customNutrition 失败:', e)
            customNutritionObj = null
          }
        } else {
          customNutritionObj = newRecipe.customNutrition
        }
      }

      editableRecipe.value = {
        name: newRecipe.name || '',
        type: newRecipe.type || 'breakfast',
        cookTime: parseTimeStringToDate(newRecipe.cookTime),
        detail: newRecipe.detail || '',
        customNutrition: customNutritionObj
      }
    }
  },
  { immediate: true }
)

// 监听编辑状态变化，进入编辑模式时自动启用自定义营养
watch(isEditing, (newVal) => {
  if (newVal && !editableRecipe.value.customNutrition) {
    // 进入编辑模式且没有自定义营养时，自动初始化为当前计算值
    editableRecipe.value.customNutrition = { ...nutritionData.value }
  }
})

// 保存编辑
const saveEdit = async () => {
  if (!props.recipe) {
    ElMessage.warning('食谱数据为空')
    return
  }

  if (!editableRecipe.value.name.trim()) {
    ElMessage.warning('请填写食谱名称')
    return
  }

  isSaving.value = true

  try {
    // 将 Date 对象转换为字符串格式 "HH:mm"
    let cookTimeToSave = ''
    const cookTime = editableRecipe.value.cookTime
    if (cookTime instanceof Date) {
      const hours = String(cookTime.getHours()).padStart(2, '0')
      const minutes = String(cookTime.getMinutes()).padStart(2, '0')
      cookTimeToSave = `${hours}:${minutes}`
    } else if (typeof cookTime === 'string' && cookTime.includes(':')) {
      cookTimeToSave = cookTime
    }

    // 处理 items 字段：如果是数组则转换为 JSON 字符串
    let itemsToSave = props.recipe.items
    if (Array.isArray(props.recipe.items)) {
      itemsToSave = JSON.stringify(props.recipe.items)
    }

    // 处理 customNutrition：如果是对象则序列化为 JSON 字符串
    let customNutritionToSave = null
    if (editableRecipe.value.customNutrition) {
      const customNutritionType = typeof editableRecipe.value.customNutrition
      console.log('customNutrition 类型:', customNutritionType)
      customNutritionToSave = customNutritionType === 'string'
        ? editableRecipe.value.customNutrition
        : JSON.stringify(editableRecipe.value.customNutrition)
      console.log('序列化后的 customNutrition:', customNutritionToSave)
      console.log('序列化后是否为字符串:', typeof customNutritionToSave === 'string')
    }

    const updateData = {
      ...props.recipe,
      name: editableRecipe.value.name,
      type: editableRecipe.value.type,
      cookTime: cookTimeToSave,
      detail: editableRecipe.value.detail,
      items: itemsToSave,
      // 保存自定义营养信息（已序列化为JSON字符串）
      customNutrition: customNutritionToSave
    }

    console.log('=== RecipeDetail 保存数据 ===')
    console.log('editableRecipe.customNutrition:', editableRecipe.value.customNutrition)
    console.log('准备保存的 customNutrition:', customNutritionToSave)
    console.log('updateData.customNutrition 类型:', typeof updateData.customNutrition)
    console.log('完整 updateData:', updateData)

    // 调用后端API更新食谱
    const response = await axios.put(
      API_CONFIG.baseURL + API_CONFIG.recipe.update + props.recipe.id,
      updateData
    )

    console.log('=== RecipeDetail 后端响应 ===')
    console.log('后端返回的完整数据:', response.data?.data)
    console.log('后端返回的 customNutrition:', response.data?.data?.customNutrition)

    if (response.data && response.data.data) {
      // 触发update:recipe事件，传递更新后的食谱数据给父组件
      emit('update:recipe', response.data.data)
      ElMessage.success('食谱已更新')
      isEditing.value = false
    } else {
      ElMessage.error('更新失败：服务器返回数据异常')
    }
  } catch (error) {
    console.error('更新食谱失败:', error)
    ElMessage.error('更新失败，请稍后重试')
  } finally {
    isSaving.value = false
  }
}

// 取消编辑
const cancelEdit = () => {
  // 恢复原始数据
  if (props.recipe) {
    // 处理 customNutrition：如果是 JSON 字符串则解析为对象
    let customNutritionObj = null
    if (props.recipe.customNutrition) {
      if (typeof props.recipe.customNutrition === 'string') {
        try {
          customNutritionObj = JSON.parse(props.recipe.customNutrition)
        } catch (e) {
          console.error('解析 customNutrition 失败:', e)
          customNutritionObj = null
        }
      } else {
        customNutritionObj = props.recipe.customNutrition
      }
    }

    editableRecipe.value = {
      name: props.recipe.name || '',
      type: props.recipe.type || 'breakfast',
      cookTime: parseTimeStringToDate(props.recipe.cookTime),
      detail: props.recipe.detail || '',
      customNutrition: customNutritionObj
    }
  }
  isEditing.value = false
}

// 根据食谱类型获取颜色主题
const getThemeColor = () => {
  if (!props.recipe?.type) return 'default'

  switch (props.recipe.type) {
    case 'breakfast':
    case 'brunch':
      return 'breakfast'
    case 'lunch':
      return 'lunch'
    case 'dinner':
    case 'supper':
      return 'dinner'
    case 'afternoon_tea':
    case 'tea':
    case 'night_snack':
    case 'snack':
      return 'snack'
    default:
      return 'default'
  }
}

// 计算营养数据
const nutritionData = computed(() => {
  if (!props.recipe) {
    return {
      calories: 0,
      protein: 0,
      carbs: 0,
      fat: 0
    }
  }

  // 如果有自定义营养信息且在编辑模式，优先使用自定义值
  if (isEditing.value && editableRecipe.value.customNutrition) {
    return editableRecipe.value.customNutrition
  }

  // 如果有自定义营养信息（存储在recipe中），使用自定义值
  if (props.recipe.customNutrition) {
    // 如果是 JSON 字符串则解析为对象
    if (typeof props.recipe.customNutrition === 'string') {
      try {
        return JSON.parse(props.recipe.customNutrition)
      } catch (e) {
        console.error('解析 customNutrition 失败:', e)
        // 解析失败则继续使用计算值
      }
    } else {
      return props.recipe.customNutrition
    }
  }

  // 确保items是数组
  let items = []
  if (props.recipe.items) {
    items = Array.isArray(props.recipe.items)
      ? props.recipe.items
      : typeof props.recipe.items === 'string'
        ? JSON.parse(props.recipe.items)
        : []
  }

  return items.reduce(
    (acc, dish) => {
      return {
        calories: acc.calories + (dish?.calories || 0),
        protein: acc.protein + (dish?.protein || 0),
        carbs: acc.carbs + (dish?.carbs || 0),
        fat: acc.fat + (dish?.fat || 0)
      }
    },
    { calories: 0, protein: 0, carbs: 0, fat: 0 }
  )
})

// 格式化餐型显示
const getMealTypeName = (type) => {
  const mealTypeMap = {
    breakfast: '早餐',
    lunch: '午餐',
    dinner: '晚餐',
    afternoon_tea: '下午茶',
    tea: '茶点',
    night_snack: '夜宵',
    snack: '零食',
    morning_snack: '上午加餐',
    brunch: '早午餐',
    supper: '晚餐',
    midnight_snack: '宵夜',
    health_snack: '健康零食',
    fitness_meal: '健身餐',
    dessert: '甜点',
    sweet: '甜食',
    soup: '汤',
    porridge: '粥',
    salad: '沙拉',
    vegetable: '蔬菜',
    meat: '肉类',
    protein: '蛋白质'
  }
  return mealTypeMap[type] || type
}

// 格式化烹饪时间显示
const formatCookTime = (time) => {
  if (!time) return '未设置'

  let hours = 0, minutes = 0

  if (time instanceof Date) {
    hours = time.getHours()
    minutes = time.getMinutes()
  } else if (typeof time === 'string' && time.includes(':')) {
    ;[hours, minutes] = time.split(':').map(Number)
  }

  if (hours === 0 && minutes === 0) {
    return '未设置'
  }

  let formatted = ''
  if (hours > 0) {
    formatted += `${hours} 小时`
  }

  if (minutes > 0) {
    formatted += (formatted ? ' ' : '') + `${minutes} 分钟`
  }

  return formatted || '未设置'
}

// 餐型选项
const mealTypeOptions = [
  { label: '早餐', value: 'breakfast' },
  { label: '午餐', value: 'lunch' },
  { label: '晚餐', value: 'dinner' },
  { label: '下午茶', value: 'afternoon_tea' },
  { label: '夜宵', value: 'night_snack' }
]

// 更新自定义营养信息
const updateCustomNutrition = (field, value) => {
  if (!editableRecipe.value.customNutrition) {
    editableRecipe.value.customNutrition = {
      calories: 0,
      protein: 0,
      carbs: 0,
      fat: 0
    }
  }
  editableRecipe.value.customNutrition[field] = Number(value) || 0
}
</script>

<template>
  <el-dialog
    :model-value="props.visible"
    :title="recipe ? `${recipe.name} 详情` : '食谱详情'"
    width="600px"
    top="5%"
    @update:model-value="emit('update:visible', $event)"
    @close="handleClose"
  >
    <div v-if="recipe" class="recipe-details" :class="getThemeColor()">
      <!-- 食谱封面与标题区域 -->
      <div class="recipe-header-section">
        <div class="recipe-title-block">
          <!-- 食谱名称：可编辑 -->
          <div v-if="!isEditing" class="recipe-main-title">{{ recipe.name }}</div>
          <el-input
            v-else
            v-model="editableRecipe.name"
            placeholder="食谱名称"
            size="large"
            style="margin-bottom: 16px"
          />

          <div class="recipe-basic-info">
            <!-- 餐型：可编辑 -->
            <div class="info-item">
              <span class="info-label">餐型:</span>
              <span v-if="!isEditing" class="info-value type-tag">{{ getMealTypeName(recipe.type) }}</span>
              <el-select
                v-else
                v-model="editableRecipe.type"
                placeholder="选择餐型"
                size="small"
                style="width: 120px"
              >
                <el-option
                  v-for="option in mealTypeOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </div>

            <!-- 烹饪时间：可编辑 -->
            <div class="info-item">
              <span class="info-label">烹饪时间:</span>
              <span v-if="!isEditing" class="info-value time-text">{{ formatCookTime(recipe.cookTime) }}</span>
              <el-time-picker
                v-else
                v-model="editableRecipe.cookTime"
                type="time"
                format="HH:mm"
                placeholder="选择烹饪时间"
                size="small"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="recipe-content-section">
        <!-- 营养信息卡片 -->
        <div class="nutrition-section">
          <div class="section-title">营养信息</div>
          <div class="nutrition-card-container">
            <div
              v-for="(nutrient, index) in [
                { label: '卡路里', value: nutritionData.calories, unit: 'kcal', icon: '🔥', field: 'calories' },
                { label: '蛋白质', value: nutritionData.protein, unit: 'g', icon: '💪', field: 'protein' },
                { label: '碳水化合物', value: nutritionData.carbs, unit: 'g', icon: '🍚', field: 'carbs' },
                { label: '脂肪', value: nutritionData.fat, unit: 'g', icon: '🥑', field: 'fat' }
              ]"
              :key="index"
              class="nutrition-card"
            >
              <div class="nutrition-icon">{{ nutrient.icon }}</div>
              <div class="nutrition-label">{{ nutrient.label }}</div>
              <div v-if="!isEditing" class="nutrition-value">
                {{ nutrient.value }} {{ nutrient.unit }}
              </div>
              <el-input-number
                v-else
                :model-value="editableRecipe.customNutrition[nutrient.field]"
                @update:model-value="updateCustomNutrition(nutrient.field, $event)"
                :min="0"
                :precision="1"
                size="small"
                style="width: 120px;"
              >
                <template #suffix>
                  <span style="font-size: 12px; color: #909399;">{{ nutrient.unit }}</span>
                </template>
              </el-input-number>
            </div>
          </div>
        </div>

        <!-- 菜品列表区域 -->
        <div class="dishes-section">
          <div class="section-title">菜品组成</div>
          <div class="dish-list-container">
            <div
              v-for="(item, index) in recipe.items && recipe.items.length > 0
                ? recipe.items
                : ['待添加菜品']"
              :key="index"
              class="dish-card-item"
              :class="{ 'empty-dish-card': typeof item === 'string' }"
            >
              <div class="dish-card-header">
                <span class="dish-emoji">🍽️</span>
                <h3 class="dish-name-text">
                  {{ typeof item === 'object' ? item.name : item }}
                </h3>
              </div>

              <div class="dish-card-body">
                <div
                  v-if="typeof item === 'object' && item.ingredients && item.ingredients.length > 0"
                  class="ingredients-list"
                >
                  <el-tag
                    v-for="(ingredient, ingIndex) in item.ingredients"
                    :key="ingIndex"
                    size="small"
                  >
                    {{ ingredient }}
                  </el-tag>
                </div>
                <div
                  v-else-if="
                    typeof item === 'object' && (!item.ingredients || item.ingredients.length === 0)
                  "
                  class="no-ingredients-info"
                >
                  <el-tag size="small" type="warning">无食材信息</el-tag>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 食谱详情信息：可编辑 -->
        <div class="details-section">
          <div class="section-title">食谱详情</div>
          <div class="details-content">
            <div v-if="!isEditing" class="details-display">
              <span class="details-text">{{ editableRecipe.detail || '暂无详情' }}</span>
            </div>

            <div v-else class="details-editor">
              <el-input
                v-model="editableRecipe.detail"
                type="textarea"
                :rows="3"
                placeholder="请输入食谱详情"
                size="default"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button v-if="!isEditing" type="primary" @click="handleClose">关闭</el-button>
        <el-button v-else type="default" @click="cancelEdit">取消</el-button>
        <el-button v-if="!isEditing" type="success" @click="isEditing = true">编辑</el-button>
        <el-button v-else type="primary" :loading="isSaving" @click="saveEdit">
          {{ isSaving ? '保存中...' : '保存' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
/* 食谱详情对话框样式 */
.recipe-details {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  padding: 16px;
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

/* 食谱头部区域 */
.recipe-header-section {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f2f5;
}

.recipe-title-block {
  text-align: center;
}

.recipe-main-title {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 16px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.recipe-basic-info {
  display: flex;
  justify-content: center;
  gap: 40px;
  flex-wrap: wrap;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.info-label {
  font-weight: 600;
  color: #667eea;
  font-size: 14px;
}

.info-value {
  font-size: 16px;
  font-weight: 500;
  color: #4a5568;
}

.type-tag {
  background: linear-gradient(135deg, #e6f2ff 0%, #e8d5ff 100%);
  color: #333;
  padding: 8px 18px;
  border-radius: 20px;
  font-weight: 600;
}

.time-text {
  font-size: 16px;
  font-weight: 600;
  color: #f57c00;
}

/* 内容区域 */
.recipe-content-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 12px;
  padding-left: 8px;
  border-left: 4px solid #667eea;
  display: flex;
  align-items: center;
}

/* 营养信息 */
.nutrition-section {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9ff 100%);
  padding: 24px;
  border-radius: 16px;
  border: 2px solid #e6ecf5;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.08);
}

.nutrition-card-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.nutrition-card {
  background: linear-gradient(135deg, #ffffff 0%, #f9fafb 100%);
  padding: 16px;
  border-radius: 14px;
  text-align: center;
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 110px;
}

.nutrition-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.15);
  border-color: #667eea;
}

.nutrition-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.nutrition-label {
  font-size: 14px;
  font-weight: 600;
  color: #718096;
  margin-bottom: 8px;
}

.nutrition-value {
  font-size: 20px;
  font-weight: 700;
  color: #667eea;
  min-height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 营养信息编辑模式样式 */
.nutrition-card :deep(.el-input-number) {
  .el-input__inner {
    text-align: center;
    font-weight: 700;
    color: #667eea;
  }

  .el-input-number__decrease,
  .el-input-number__increase {
    background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
    border-color: #667eea40;

    &:hover {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-color: #667eea;
      color: white;
    }
  }
}

/* 菜品列表 */
.dishes-section {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9ff 100%);
  padding: 16px;
  border-radius: 16px;
  border: 2px solid #e6ecf5;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.08);
}

.dish-list-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: calc(100vh - 400px);
  overflow-y: auto;
  padding-right: 8px;
}

.dish-card-item {
  background: linear-gradient(135deg, #ffffff 0%, #f9fafb 100%);
  padding: 20px;
  border-radius: 14px;
  border-left: 5px solid #667eea;
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.dish-card-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 32px rgba(102, 126, 234, 0.12);
  border-color: #667eea;
}

.dish-card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.dish-emoji {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.2);
}

.dish-name-text {
  font-size: 18px;
  font-weight: 700;
  color: #2d3748;
  margin: 0;
}

.ingredients-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.ingredients-list .el-tag {
  background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
  border: 1px solid #667eea40;
  color: #667eea;
  font-weight: 500;
  font-size: 13px;
  border-radius: 20px;
  padding: 6px 14px;
  transition: all 0.3s ease;
}

.ingredients-list .el-tag:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #667eea;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.no-ingredients-info {
  margin-top: 12px;
}

/* 空菜品卡片 */
.empty-dish-card {
  background: linear-gradient(135deg, #ffffff 0%, #f7fafc 100%) !important;
  border: 2px dashed #cbd5e0 !important;
  border-left: 5px solid #a0aec0 !important;
  opacity: 0.8;
}

.empty-dish-card:hover {
  transform: translateY(-3px) !important;
  cursor: default;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05) !important;
}

.empty-dish-card .dish-name-text {
  font-style: italic;
  color: #a0aec0;
}

/* 食谱详情 */
.details-section {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9ff 100%);
  padding: 16px;
  border-radius: 16px;
  border: 2px solid #e6ecf5;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.08);
  margin-top: 16px;
}

.details-content {
  margin-top: 12px;
}

.details-display {
  position: relative;
  padding: 12px;
  background-color: #f9fafb;
  border-radius: 8px;
}

.details-text {
  font-size: 14px;
  line-height: 1.6;
  color: #4a5568;
  white-space: pre-wrap;
}

.details-editor {
  display: flex;
  flex-direction: column;
}

/* 对话框底部 */
.dialog-footer {
  text-align: center;
  padding: 20px;
  display: flex;
  justify-content: center;
  gap: 16px;
}

.dialog-footer .el-button {
  border-radius: 24px;
  padding: 12px 36px;
  font-weight: 600;
  font-size: 16px;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.1);
  transition: all 0.3s ease;
}

.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #e3f2fd 0%, #f8f9ff 100%);
  border: none;
  color: #333333;
}

.dialog-footer .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.2);
  background: linear-gradient(135deg, #d1ecf1 0%, #e6f7ff 100%);
}

.dialog-footer .el-button--success {
  background: linear-gradient(135deg, #e8f5e9 0%, #f1f8ff 100%);
  border: none;
  color: #333333;
}

.dialog-footer .el-button--success:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(76, 175, 80, 0.2);
  background: linear-gradient(135deg, #c8e6c9 0%, #e6f7ff 100%);
}

/* 不同主题颜色适配 */
.recipe-details.breakfast .section-title {
  border-left-color: #f57c00;
}

.recipe-details.lunch .section-title {
  border-left-color: #388e3c;
}

.recipe-details.dinner .section-title {
  border-left-color: #1976d2;
}

.recipe-details.snack .section-title {
  border-left-color: #1565c0;
}
</style>
