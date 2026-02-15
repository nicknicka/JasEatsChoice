<script setup>
import { computed } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'

// Props 定义
const props = defineProps({
  recipe: {
    type: Object,
    required: true
  },
  // 是否支持批量选择
  selectable: {
    type: Boolean,
    default: false
  },
  // 当前选中的食谱ID列表
  selectedIds: {
    type: Array,
    default: () => []
  },
  // 是否显示营养统计
  showNutrition: {
    type: Boolean,
    default: false
  },
  // 是否显示操作按钮
  showActions: {
    type: Boolean,
    default: true
  },
  // 是否显示菜品标签（如果为false则不显示）
  showDishTags: {
    type: Boolean,
    default: true
  },
  // 获取餐型图标的函数（可选，有默认实现）
  getMealIcon: {
    type: Function,
    default: null
  },
  // 获取标签类型的函数（可选，有默认实现）
  getTagType: {
    type: Function,
    default: null
  },
  // 是否显示时间统计
  showTime: {
    type: Boolean,
    default: false
  }
})

// Emits 定义
const emit = defineEmits([
  'toggle-select', // 切换选中状态
  'toggle-favorite', // 切换收藏状态
  'view-details', // 查看详情
  'add-dish', // 添加菜品
  'import-merchant-dish', // 导入商家菜品
  'replace-dish', // 替换菜品
  'delete-dish' // 删除菜品
])

// 计算属性：是否选中
const isSelected = computed(() => {
  const result = props.selectedIds.includes(props.recipe.id)
  console.log('=== RecipeCard isSelected 计算 ===')
  console.log('食谱ID:', props.recipe.id)
  console.log('食谱名称:', props.recipe.name)
  console.log('selectedIds:', props.selectedIds)
  console.log('是否选中:', result)
  console.log('========================')
  return result
})

// 计算属性：是否收藏
const isFavorite = computed(() => props.recipe.isFavorite || props.recipe.favorite || false)

// 计算属性：菜品列表
const dishList = computed(() => {
  const items = props.recipe.items || []
  // 确保是数组
  if (typeof items === 'string') {
    try {
      return JSON.parse(items)
    } catch {
      return []
    }
  }
  return items
})

// 计算属性：显示的菜品（最多3个）
const displayDishes = computed(() => {
  return dishList.value.slice(0, 3)
})

// 计算属性：更多菜品数量
const moreDishesCount = computed(() => {
  return Math.max(0, dishList.value.length - 3)
})

// 内部方法：获取餐型图标（默认实现）
const getDefaultMealIcon = (type) => {
  const mealTypeIcons = {
    breakfast: '🥣',
    lunch: '🍚',
    dinner: '🍱',
    afternoon_tea: '🍵',
    tea: '🍵',
    night_snack: '🍪',
    snack: '🍪',
    morning_snack: '🥐',
    brunch: '🥐',
    supper: '🌙',
    midnight_snack: '🌙',
    health_snack: '💪',
    fitness_meal: '💪',
    dessert: '🍰',
    sweet: '🍰',
    soup: '🍲',
    porridge: '🍲',
    salad: '🥗',
    vegetable: '🥗',
    meat: '🥩',
    protein: '🥩',
    // 中文支持
    早餐: '🥣',
    午餐: '🍚',
    晚餐: '🍱',
    加餐: '🍪'
  }
  return mealTypeIcons[type] || '🍴'
}

// 内部方法：获取标签类型（默认实现）
const getDefaultTagType = (type) => {
  const typeMap = {
    breakfast: 'warning',
    早餐: 'warning',
    lunch: 'success',
    午餐: 'success',
    dinner: 'primary',
    晚餐: 'primary',
    afternoon_tea: 'purple',
    tea: 'purple',
    night_snack: 'blue',
    snack: 'blue',
    morning_snack: 'orange',
    brunch: 'orange',
    supper: 'cyan',
    midnight_snack: 'cyan',
    health_snack: 'green',
    fitness_meal: 'green',
    dessert: 'pink',
    sweet: 'pink',
    soup: 'teal',
    porridge: 'teal',
    salad: 'success',
    vegetable: 'success',
    meat: 'brown',
    protein: 'brown',
    加餐: 'purple'
  }
  return typeMap[type] || 'info'
}

// 暴露给模板的方法（使用父组件提供的函数或默认实现）
const getMealIconFn = props.getMealIcon || getDefaultMealIcon
const getTagTypeFn = props.getTagType || getDefaultTagType

// 事件处理
const handleCardClick = () => {
  if (props.selectable) {
    console.log('=== 卡片点击事件 ===')
    console.log('食谱ID:', props.recipe.id)
    console.log('食谱名称:', props.recipe.name)
    emit('toggle-select', props.recipe.id)
    console.log('已发送 toggle-select 事件，ID:', props.recipe.id)
    console.log('==================')
  }
}

// 处理复选框变化
const handleCheckboxChange = (value) => {
  console.log('=== 复选框变化事件 ===')
  console.log('食谱ID:', props.recipe.id)
  console.log('食谱名称:', props.recipe.name)
  console.log('复选框新值:', value)
  console.log('当前 isSelected:', isSelected.value)
  emit('toggle-select', props.recipe.id)
  console.log('已发送 toggle-select 事件，ID:', props.recipe.id)
  console.log('===================')
}

const handleFavoriteClick = (e) => {
  e.stopPropagation()
  emit('toggle-favorite', props.recipe)
}

const handleViewDetails = (e) => {
  e.stopPropagation()
  emit('view-details', props.recipe)
}

const handleAddDish = (e) => {
  e.stopPropagation()
  emit('add-dish', props.recipe)
}

const handleImportMerchantDish = (e) => {
  e.stopPropagation()
  emit('import-merchant-dish', props.recipe)
}

const handleReplaceDish = (e, dish) => {
  if (e) e.stopPropagation()
  emit('replace-dish', { recipe: props.recipe, dish })
}

const handleDeleteDish = (e, dish) => {
  if (e) e.stopPropagation()
  emit('delete-dish', { recipe: props.recipe, dish })
}
</script>

<template>
  <el-card
    class="recipe-card"
    :class="[
      recipe.type,
      {
        'recipe-card-favorited': isFavorite,
        'recipe-card-selected': isSelected
      }
    ]"
    @click="handleCardClick"
  >
    <!-- 卡片头部 -->
    <template #header>
      <div class="card-header">
        <!-- 批量选择复选框 -->
        <div v-if="selectable" class="checkbox-wrapper" @click.stop>
          <el-checkbox
            :model-value="isSelected"
            @change="handleCheckboxChange"
          />
        </div>

        <!-- 餐型图标 -->
        <span class="meal-icon">
          {{ getMealIconFn(recipe?.type) }}
        </span>

        <!-- 食谱名称 -->
        <span class="recipe-name">{{ recipe.name }}</span>

        <!-- 右上角收藏按钮 -->
        <div class="card-favorite" @click.stop>
          <el-button
            type="text"
            size="small"
            :class="{ 'favorite-btn': isFavorite }"
            style="padding: 0; margin: 0; font-size: 18px"
            @click="handleFavoriteClick"
          >
            {{ isFavorite ? '⭐' : '☆' }}
          </el-button>
        </div>
      </div>
    </template>

    <!-- 菜品列表 -->
    <div v-if="showDishTags" class="recipe-items" @click.stop>
      <el-tag
        v-for="(item, index) in displayDishes"
        :key="index"
        :type="getTagTypeFn(recipe.type)"
      >
        {{ typeof item === 'object' ? item.name : item }}
      </el-tag>

      <!-- 更多菜品提示 -->
      <el-tag v-if="moreDishesCount > 0" type="info">
        +{{ moreDishesCount }} 更多
      </el-tag>

      <!-- 空菜品提示 -->
      <el-tag v-if="dishList.length === 0" type="warning">
        待添加菜品
      </el-tag>
    </div>

    <!-- 操作按钮 -->
    <div v-if="showActions" class="recipe-actions" @click.stop>
      <el-button type="text" size="small" @click="handleViewDetails">
        查看详情
      </el-button>
      <el-button type="text" size="small" @click="handleAddDish">
        添加菜品
      </el-button>
      <el-button type="text" size="small" @click="handleImportMerchantDish">
        导入商家菜品
      </el-button>

      <!-- 替换菜品按钮 -->
      <el-dropdown v-if="dishList.length > 0" trigger="click" @click.stop>
        <el-button type="text" size="small" @click.stop>
          替换菜品
          <el-icon class="el-icon--right"><ArrowDown /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item
              v-for="(dish, index) in dishList"
              :key="`replace-${recipe.id}-${dish.id || dish.name || index}`"
              @click.stop="handleReplaceDish($event, dish)"
            >
              {{ typeof dish === 'object' ? dish.name : dish }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <!-- 删除菜品按钮 -->
      <el-dropdown v-if="dishList.length > 0" trigger="click" @click.stop>
        <el-button type="text" size="small" @click.stop>
          删除菜品
          <el-icon class="el-icon--right"><ArrowDown /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item
              v-for="(dish, index) in dishList"
              :key="`delete-${recipe.id}-${dish.id || dish.name || index}`"
              @click.stop="handleDeleteDish($event, dish)"
            >
              {{ typeof dish === 'object' ? dish.name : dish }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-card>
</template>

<style scoped lang="less">
.recipe-card {
  margin-bottom: 16px !important;
  background: #ffffff !important;
  border-radius: 12px !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #e0e0e0 !important;
  overflow: hidden;
  position: relative;

  &.recipe-card-favorited {
    border: 2px solid #ffd700 !important;
    box-shadow: 0 2px 8px rgba(255, 215, 0, 0.2);
  }

  &.recipe-card-selected {
    border: 2px solid #667eea !important;
    box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);
  }

  &.recipe-card-favorited:hover {
    background: #fffbf0 !important;
  }

  &:hover {
    background: #f5f7ff !important;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  }

  .card-header {
    position: relative;
    display: flex;
    align-items: center;
    gap: 16px;
    font-size: 20px;
    font-weight: 700;
    color: #2c3e50;
    padding: 20px 24px !important;
    cursor: pointer;
    user-select: none;

    .recipe-name {
      flex: 1;
    }

    .meal-icon {
      font-size: 32px;
      padding: 10px;
      border-radius: 50%;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      box-shadow: 0 6px 20px rgba(102, 126, 234, 0.3);
      display: flex;
      align-items: center;
      justify-content: center;
      width: 56px;
      height: 56px;
    }
  }

  .recipe-items {
    margin: 24px;
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    cursor: default;

    .el-tag {
      padding: 8px 16px;
      border-radius: 20px;
      font-size: 14px;
      font-weight: 500;
    }
  }

  .recipe-stats {
    margin: 0 24px;
    display: flex;
    gap: 20px;

    .stat-item {
      display: flex;
      align-items: center;
      gap: 5px;
      color: #666;
      font-size: 14px;
    }
  }

  .recipe-actions {
    text-align: right;
    margin: 0 24px 20px;
    padding-top: 16px;
    border-top: 1px solid #eef2f7;
    cursor: default;
    display: flex;
    justify-content: flex-end;
    gap: 6px;
    flex-wrap: wrap; /* 允许按钮换行 */
    align-items: center; /* 垂直对齐 */

    .el-button {
      font-size: 12px;
      padding: 4px 10px;
      border-radius: 6px;
      margin: 0;
      white-space: nowrap; /* 防止按钮文字换行 */
    }
  }

  /* 不同餐型的样式 */
  &.breakfast,
  &.早餐 {
    border-left: 4px solid #ffc107;

    &::before {
      background: linear-gradient(90deg, #ffc107 0%, #ffeb3b 100%);
    }

    .meal-icon {
      background: linear-gradient(135deg, #ffc107 0%, #ffeb3b 100%) !important;
      color: #333 !important;
    }
  }

  &.lunch,
  &.午餐 {
    border-left: 4px solid #4caf50;

    &::before {
      background: linear-gradient(90deg, #4caf50 0%, #8bc34a 100%);
    }

    .meal-icon {
      background: linear-gradient(135deg, #4caf50 0%, #8bc34a 100%) !important;
      color: white !important;
    }
  }

  &.dinner,
  &.晚餐 {
    border-left: 4px solid #2196f3;

    &::before {
      background: linear-gradient(90deg, #2196f3 0%, #64b5f6 100%);
    }

    .meal-icon {
      background: linear-gradient(135deg, #2196f3 0%, #64b5f6 100%) !important;
      color: white !important;
    }
  }

  &.afternoon_tea,
  &.tea,
  &.加餐 {
    border-left: 4px solid #9c27b0;

    &::before {
      background: linear-gradient(90deg, #9c27b0 0%, #ba68c8 100%);
    }

    .meal-icon {
      background: linear-gradient(135deg, #9c27b0 0%, #ba68c8 100%) !important;
      color: white !important;
    }
  }

  &.night_snack,
  &.snack {
    border-left: 4px solid #1e88e5;

    &::before {
      background: linear-gradient(90deg, #1e88e5 0%, #42a5f5 100%);
    }

    .meal-icon {
      background: linear-gradient(135deg, #1e88e5 0%, #42a5f5 100%) !important;
      color: white !important;
    }
  }

  &.morning_snack,
  &.brunch {
    border-left: 4px solid #ff9800;

    &::before {
      background: linear-gradient(90deg, #ff9800 0%, #ffa726 100%);
    }

    .meal-icon {
      background: linear-gradient(135deg, #ff9800 0%, #ffa726 100%) !important;
      color: white !important;
    }
  }

  &.supper,
  &.midnight_snack {
    border-left: 4px solid #00bcd4;

    &::before {
      background: linear-gradient(90deg, #00bcd4 0%, #29b6f6 100%);
    }

    .meal-icon {
      background: linear-gradient(135deg, #00bcd4 0%, #29b6f6 100%) !important;
      color: white !important;
    }
  }

  &.health_snack,
  &.fitness_meal {
    border-left: 4px solid #4caf50;

    &::before {
      background: linear-gradient(90deg, #4caf50 0%, #81c784 100%);
    }

    .meal-icon {
      background: linear-gradient(135deg, #4caf50 0%, #81c784 100%) !important;
      color: white !important;
    }
  }

  &.dessert,
  &.sweet {
    border-left: 4px solid #e91e63;

    &::before {
      background: linear-gradient(90deg, #e91e63 0%, #f06292 100%);
    }

    .meal-icon {
      background: linear-gradient(135deg, #e91e63 0%, #f06292 100%) !important;
      color: white !important;
    }
  }

  &.soup,
  &.porridge {
    border-left: 4px solid #009688;

    &::before {
      background: linear-gradient(90deg, #009688 0%, #26a69a 100%);
    }

    .meal-icon {
      background: linear-gradient(135deg, #009688 0%, #26a69a 100%) !important;
      color: white !important;
    }
  }

  &.salad,
  &.vegetable {
    border-left: 4px solid #8bc34a;

    &::before {
      background: linear-gradient(90deg, #8bc34a 0%, #aed581 100%);
    }

    .meal-icon {
      background: linear-gradient(135deg, #8bc34a 0%, #aed581 100%) !important;
      color: white !important;
    }
  }

  &.meat,
  &.protein {
    border-left: 4px solid #795548;

    &::before {
      background: linear-gradient(90deg, #795548 0%, #a1887f 100%);
    }

    .meal-icon {
      background: linear-gradient(135deg, #795548 0%, #a1887f 100%) !important;
      color: white !important;
    }
  }

  &.info {
    border-left: 4px solid #00bcd4;

    .meal-icon {
      color: #00bcd4;
      font-size: 24px;
    }
  }

// 复选框样式
.checkbox-wrapper {
  :deep(.el-checkbox) {
    .el-checkbox__input.is-checked .el-checkbox__inner {
      background-color: #667eea !important;
      border-color: #667eea !important;
    }
  }

  :deep(.el-checkbox__label) {
    display: none !important;
  }

  margin-right: 10px;
}

/* 收藏按钮样式 */
.favorite-btn {
  color: #ffd700 !important;
  font-weight: bold;
}

/* 右上角收藏按钮 */
.card-favorite {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
}
}
</style>
