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
            style="padding: 0; margin: 0; font-size: 1.286rem /* 原值: 18px */"
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
@import '../assets/css/nordic-theme.less';

// --- 北欧餐型色系 ---
@meal-amber:       #D9A84E;
@meal-amber-light: #F7EDDA;
@meal-sage:        #7BAE7F;
@meal-sage-light:  #E3F0E4;
@meal-slate:       #6B9BD2;
@meal-slate-light: #E0EDF6;
@meal-rose:        #C9898B;
@meal-rose-light:  #F3E2E2;
@meal-plum:        #9B8EC4;
@meal-plum-light:  #EDE8F5;
@meal-teal:        #6BAEA8;
@meal-teal-light:  #E0F0EE;
@meal-walnut:      #A08070;
@meal-walnut-light:#F0E6E0;

.recipe-card {
  margin-bottom: 0 !important;
  .nordic-card();
  border-left: 4px solid @nordic-accent;
  overflow: hidden;
  position: relative;

  &.recipe-card-favorited {
    border-color: @nordic-yellow;
    border-left-color: @nordic-yellow;
    box-shadow: 0 2px 12px fade(@nordic-yellow, 20%);
    &:hover { box-shadow: 0 6px 20px fade(@nordic-yellow, 25%); }
  }

  &.recipe-card-selected {
    border-color: @nordic-accent;
    border-left-color: @nordic-accent;
    box-shadow: 0 2px 12px fade(@nordic-accent, 18%);
    background: @nordic-accent-light !important;
  }

  &:hover {
    background: @nordic-surface !important;
  }

  .card-header {
    position: relative;
    display: flex;
    align-items: center;
    gap: @nordic-space-md;
    font-size: @nordic-text-md;
    font-weight: 600;
    color: @nordic-text;
    padding: 18px 20px !important;
    cursor: pointer;
    user-select: none;

    .recipe-name {
      flex: 1;
      letter-spacing: -0.3px;
    }

    .meal-icon {
      font-size: 1.6rem;
      width: 44px;
      height: 44px;
      padding: 0;
      border-radius: @nordic-radius-lg;
      background: @nordic-accent-light;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s ease;
      flex-shrink: 0;
    }
  }

  .recipe-items {
    margin: 0 20px 16px;
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    cursor: default;

    .el-tag {
      .nordic-pill-tag();
      background: @nordic-accent-light;
      color: @nordic-accent-dark;
    }
  }

  .recipe-stats {
    margin: 0 20px;
    display: flex;
    gap: @nordic-space-md;
    .stat-item {
      display: flex;
      align-items: center;
      gap: 5px;
      color: @nordic-text-secondary;
      font-size: @nordic-text-sm;
    }
  }

  .recipe-actions {
    margin: 0 20px 16px;
    padding-top: 12px;
    border-top: 1px solid @nordic-divider;
    cursor: default;
    display: flex;
    justify-content: flex-end;
    gap: 4px;
    flex-wrap: wrap;
    align-items: center;

    .el-button {
      font-size: @nordic-text-xs;
      padding: 4px 10px;
      border-radius: @nordic-radius-sm;
      margin: 0;
      white-space: nowrap;
      color: @nordic-text-secondary;
      &:hover { color: @nordic-accent; }
    }
  }

  // 早餐 — 暖金
  &.breakfast, &.早餐 {
    border-left-color: @meal-amber;
    .meal-icon { background: @meal-amber-light; }
    .recipe-items .el-tag { background: @meal-amber-light; color: darken(@meal-amber, 15%); }
  }
  // 午餐 — 鼠尾草绿
  &.lunch, &.午餐 {
    border-left-color: @meal-sage;
    .meal-icon { background: @meal-sage-light; }
    .recipe-items .el-tag { background: @meal-sage-light; color: @nordic-green-dark; }
  }
  // 晚餐 — 雾蓝
  &.dinner, &.晚餐 {
    border-left-color: @meal-slate;
    .meal-icon { background: @meal-slate-light; }
    .recipe-items .el-tag { background: @meal-slate-light; color: darken(@meal-slate, 18%); }
  }
  // 下午茶/加餐 — 柔紫
  &.afternoon_tea, &.tea, &.加餐 {
    border-left-color: @meal-plum;
    .meal-icon { background: @meal-plum-light; }
    .recipe-items .el-tag { background: @meal-plum-light; color: darken(@meal-plum, 15%); }
  }
  // 夜宵/零食 — 玫瑰
  &.night_snack, &.snack {
    border-left-color: @meal-rose;
    .meal-icon { background: @meal-rose-light; }
    .recipe-items .el-tag { background: @meal-rose-light; color: darken(@meal-rose, 15%); }
  }
  // 早午餐 — 暖金
  &.morning_snack, &.brunch {
    border-left-color: @meal-amber;
    .meal-icon { background: @meal-amber-light; }
  }
  // 晚间/宵夜 — 青绿
  &.supper, &.midnight_snack {
    border-left-color: @meal-teal;
    .meal-icon { background: @meal-teal-light; }
  }
  // 健身/健康 — 鼠尾草绿
  &.health_snack, &.fitness_meal {
    border-left-color: @meal-sage;
    .meal-icon { background: @meal-sage-light; }
  }
  // 甜点 — 玫瑰
  &.dessert, &.sweet {
    border-left-color: @meal-rose;
    .meal-icon { background: @meal-rose-light; }
  }
  // 汤/粥 — 青绿
  &.soup, &.porridge {
    border-left-color: @meal-teal;
    .meal-icon { background: @meal-teal-light; }
  }
  // 沙拉/蔬菜 — 鼠尾草绿
  &.salad, &.vegetable {
    border-left-color: @meal-sage;
    .meal-icon { background: @meal-sage-light; }
  }
  // 肉类 — 胡桃棕
  &.meat, &.protein {
    border-left-color: @meal-walnut;
    .meal-icon { background: @meal-walnut-light; }
  }
  // 默认
  &.info {
    border-left-color: @nordic-text-muted;
    .meal-icon { background: @nordic-divider; }
  }

  // --- 复选框 ---
  .checkbox-wrapper {
    margin-right: @nordic-space-sm;
    :deep(.el-checkbox) {
      .el-checkbox__input.is-checked .el-checkbox__inner {
        background-color: @nordic-accent !important;
        border-color: @nordic-accent !important;
      }
    }
    :deep(.el-checkbox__label) {
      display: none !important;
    }
  }

  // --- 收藏按钮 ---
  .favorite-btn {
    color: @nordic-yellow !important;
    font-weight: bold;
  }

  .card-favorite {
    position: absolute;
    right: 10px;
    top: 50%;
    transform: translateY(-50%);
  }
}
</style>
