<template>
  <div class="dish-detail-container">
    <!-- 页头：标题 --text-h1 + 上下 --space-6，「返回」进右侧动作区（§4.7） -->
    <AppPageHeader title="菜品详情">
      <template #actions>
        <CommonBackButton type="default" />
      </template>
    </AppPageHeader>

    <!-- 加载状态 -->
    <div v-loading="loading" element-loading-text="加载中..." class="loading-container">
      <!-- 菜品详情内容 -->
      <div v-if="dish" class="dish-detail-content">
        <!-- 主信息卡：菜品图 + 价格 / 库存 / 规格（§4.2 卡片） -->
        <AppCard class="dish-hero-card" padding="none">
          <div class="dish-hero">
            <div class="dish-media">
              <img
                v-if="showDishImage"
                :src="dish.image"
                :alt="dish.name"
                class="dish-image"
                @error="handleImageError"
              />
              <div v-else class="dish-media-placeholder">
                <el-icon :size="48"><Food /></el-icon>
                <span>{{ dish.name?.charAt(0) || '菜' }}</span>
              </div>
            </div>

            <div class="dish-info">
              <div class="dish-info-head">
                <h2 class="dish-name">{{ dish.name }}</h2>
                <StatusTag :status="dish.status" :text="dish.statusText" />
              </div>

              <div class="dish-price-row">
                <span class="current-price">¥{{ dish.price?.toFixed(2) || '0.00' }}</span>
                <span class="unit-price">每份</span>
              </div>

              <div class="dish-meta">
                <div class="meta-item">
                  <span class="label">分类:</span>
                  <span class="value">{{ dish.categoryName }}</span>
                </div>
                <div class="meta-item">
                  <span class="label">热量:</span>
                  <span class="value">{{ calorieText }}</span>
                </div>
              </div>
            </div>
          </div>
        </AppCard>

        <!-- 菜品介绍 -->
        <AppCard class="dish-section" padding="md">
          <div class="section-title">菜品介绍</div>
          <p class="description-text">{{ dish.description || '暂无介绍' }}</p>
        </AppCard>

        <!-- 食材信息 -->
        <AppCard class="dish-section" padding="md">
          <div class="section-title">
            <el-icon><FriedFood /></el-icon>
            主要食材
          </div>

          <template v-if="hasIngredients">
            <!-- 必选食材 -->
            <div v-if="dish.requiredIngredients.length > 0" class="ingredient-group">
              <div class="ingredient-group-title required-group">
                <el-icon class="warning-icon"><WarningFilled /></el-icon>
                <span>必选食材（可能含过敏原）</span>
              </div>
              <div class="ingredients-list">
                <el-tag
                  v-for="ing in dish.requiredIngredients"
                  :key="ing.id || ing.name"
                  type="danger"
                  effect="plain"
                  size="small"
                >
                  {{ ing.name }}
                  <span v-if="ing.price" class="ingredient-price">
                    +¥{{ ing.price?.toFixed(2) }}
                  </span>
                </el-tag>
              </div>
            </div>

            <!-- 可选食材 -->
            <div v-if="dish.optionalIngredients.length > 0" class="ingredient-group">
              <div class="ingredient-group-title optional-group">
                <el-icon><CirclePlus /></el-icon>
                <span>可选食材（可替换）</span>
              </div>
              <div class="ingredients-list">
                <div
                  v-for="ing in dish.optionalIngredients"
                  :key="ing.id || ing.name"
                  class="optional-ingredient-item"
                >
                  <el-checkbox
                    v-if="!isInCart"
                    :model="ing.selected"
                    @change="toggleOptionalIngredient(ing)"
                    size="small"
                  ></el-checkbox>
                  <span class="ingredient-name">{{ ing.name }}</span>
                  <span v-if="ing.price" class="ingredient-price">
                    +¥{{ ing.price?.toFixed(2) }}
                  </span>
                  <el-tag
                    v-if="ing.isAllergen"
                    type="warning"
                    effect="plain"
                    size="mini"
                    class="allergen-tag"
                  >
                    含过敏原
                  </el-tag>
                </div>
              </div>
            </div>
          </template>

          <!-- 无食材数据 -->
          <AppEmptyState v-else description="暂无食材信息" compact />
        </AppCard>

        <!-- 烹饪步骤 -->
        <AppCard v-if="dish.cookingSteps.length > 0" class="dish-section" padding="md">
          <div class="section-title">
            <el-icon><Van /></el-icon>
            烹饪步骤
          </div>
          <el-timeline>
            <el-timeline-item
              v-for="(step, index) in dish.cookingSteps"
              :key="index"
              :timestamp="step.stepTime || '预计' + (index + 1) + '分钟'"
            >
              <div class="step-content">
                <div class="step-title">{{ step.title }}</div>
                <div class="step-description" v-if="step.description">{{ step.description }}</div>
                <div class="step-meta" v-if="step.duration">
                  <el-icon><Clock /></el-icon>
                  <span>预计耗时: {{ step.duration }}</span>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </AppCard>

        <!-- 营养信息：仅在接口确实返回营养数据时展示 -->
        <AppCard v-if="nutritionItems.length > 0" class="dish-section" padding="md">
          <div class="section-title">
            <el-icon><TrendCharts /></el-icon>
            营养成分
          </div>
          <el-row :gutter="20" class="nutrition-grid">
            <el-col :span="8" v-for="(nutrient, key) in nutritionItems" :key="key">
              <div class="nutrition-item">
                <div class="nutrition-label">{{ nutrient.label }}</div>
                <div class="nutrition-value">{{ nutrient.value }}</div>
              </div>
            </el-col>
          </el-row>
        </AppCard>

        <!-- 饮食禁忌提示 -->
        <AppCard v-if="hasAllergens" class="dish-section" padding="md">
          <el-alert type="warning" :closable="false" show-icon>
            <template #title>
              <div class="alert-title">
                <el-icon class="alert-icon"><WarningFilled /></el-icon>
                过敏提示
              </div>
            </template>
            <div class="alert-content">
              <p>该菜品含以下过敏原：</p>
              <el-tag
                v-for="allergen in dish.detectedAllergens"
                :key="allergen"
                type="danger"
                effect="plain"
                size="small"
              >
                {{ allergen }}
              </el-tag>
              <p class="tip">如有过敏史，请谨慎点餐或选择替换食材</p>
            </div>
          </el-alert>
        </AppCard>

        <!-- 菜品备注 -->
        <AppCard v-if="dish.remark" class="dish-section" padding="md">
          <div class="section-title">
            <el-icon><EditPen /></el-icon>
            菜品备注
          </div>
          <p class="remark-text">{{ dish.remark }}</p>
        </AppCard>
      </div>

      <!-- 空状态 -->
      <AppEmptyState v-else description="暂无菜品详情" />
    </div>

    <!-- 底栏只保留数量与主操作（§4.1）；返回已移到页头动作区（§4.7） -->
    <div v-if="dish" class="bottom-actions">
      <el-input-number
        v-if="dish.status === 'available'"
        v-model="quantity"
        :min="1"
        :max="99"
        size="large"
        class="quantity-input"
        :disabled="isInCart"
      >
        <template #prepend>
          <el-button :icon="Minus" @click="quantity > 1 && quantity--" />
        </template>
        <template #append>
          <el-button :icon="Plus" @click="quantity < 99 && quantity++" />
        </template>
      </el-input-number>

      <el-button
        v-if="dish.status === 'available' && !isInCart"
        type="primary"
        size="large"
        class="add-to-cart-btn"
        @click="addToCart"
      >
        <el-icon><ShoppingCart /></el-icon>
        加入购物车
      </el-button>

      <el-button
        v-if="isInCart"
        type="danger"
        size="large"
        class="remove-from-cart-btn"
        @click="removeFromCart"
      >
        <el-icon><Delete /></el-icon>
        移出购物车
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { API_CONFIG } from '../../config'
import { ElMessage } from 'element-plus'
import {
  Shop,
  Clock,
  Location,
  Money,
  Van,
  WarningFilled,
  Food,
  Food as FriedFood,
  CirclePlus,
  ShoppingCart,
  Delete,
  TrendCharts,
  Plus,
  Minus,
  EditPen
} from '@element-plus/icons-vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import AppCard from '../../components/common/AppCard.vue'
import AppEmptyState from '../../components/common/AppEmptyState.vue'
import StatusTag from '../../components/common/StatusTag.vue'

const route = useRoute()
const router = useRouter()
const dishId = ref(route.params.dishId)
const dish = ref(null)
const loading = ref(true)
const quantity = ref(1)
const isInCart = ref(false)
const imageFailed = ref(false)

// 接口返回的图片可能来自第三方占位图服务，那不是真实菜品图；这类 URL 与加载失败统一按无图降级（§7.1：占位色只取 token）。
const PLACEHOLDER_IMAGE_HOSTS = ['placeholder.com', 'dummyimage.com']
const isPlaceholderImage = (url) => PLACEHOLDER_IMAGE_HOSTS.some((host) => url.includes(host))
const showDishImage = computed(
  () => !!dish.value?.image && !imageFailed.value && !isPlaceholderImage(dish.value.image)
)

// 接口暂无 calorie 字段，热量只能取 nutrition.calories；取不到就保持降级态，不伪造 0。
const calorieText = computed(() => {
  const calories = dish.value?.nutrition?.calories
  if (calories === undefined || calories === null || calories === '') return '暂无'
  return `${calories} 千卡/份`
})

// 营养成分数据映射
const nutritionDisplayMap = {
  protein: '蛋白质',
  fat: '脂肪',
  carbohydrate: '碳水化合物',
  fiber: '膳食纤维',
  sodium: '钠',
  calories: '热量'
}

const parseJsonValue = (value, fallback) => {
  if (!value) return fallback
  return typeof value === 'string' ? JSON.parse(value) : value
}

// 判断是否有食材数据
const hasIngredients = computed(() => {
  return dish.value && (
    (dish.value.requiredIngredients && dish.value.requiredIngredients.length > 0) ||
    (dish.value.optionalIngredients && dish.value.optionalIngredients.length > 0)
  )
})

// 判断是否有过敏原
const hasAllergens = computed(() => {
  return dish.value && dish.value.detectedAllergens && dish.value.detectedAllergens.length > 0
})

// 格式化营养信息显示
const nutritionItems = computed(() => {
  if (!dish.value?.nutrition) return []

  const nutrition = dish.value.nutrition
  return Object.keys(nutrition).map(key => ({
    label: nutritionDisplayMap[key] || key,
    value: nutrition[key]
  }))
})

// 加载菜品详情
const loadDishDetail = async () => {
  loading.value = true
  try {
    // 获取菜品基本信息
    const dishResponse = await axios.get(`${API_CONFIG.baseURL}/v1/dishes/${dishId.value}`)

    if (!dishResponse.data?.data) {
      throw new Error('菜品不存在')
    }

    const dishData = dishResponse.data.data
    const requiredIngredients = dishData.requiredIngredients || []
    const optionalIngredients = dishData.optionalIngredients || []

    // 组装菜品详情数据
    dish.value = {
      ...dishData,
      // 分类直接取接口已返回的 category 字段，不再依赖接口没有的 categoryId 二次查询
      categoryName: dishData.category || '未分类',
      status: dishData.status === 1 || dishData.status === true ? 'available' : 'unavailable',
      statusText: dishData.status === 1 || dishData.status === true ? '有售' : '已下架',
      // 食材在接口顶层数组，不在 ingredients 对象里
      requiredIngredients,
      optionalIngredients,
      // 解析烹饪步骤
      cookingSteps: parseJsonValue(dishData.cookingSteps, []),
      // 营养信息
      nutrition: parseJsonValue(dishData.nutrition, null),
      // 检测过敏原
      detectedAllergens: detectAllergens({ required: requiredIngredients, optional: optionalIngredients })
    }

    loading.value = false
  } catch (error) {
    console.error('加载菜品详情失败:', error)
    ElMessage.error('加载菜品详情失败')
    loading.value = false
  }
}

// 检测过敏原
const detectAllergens = (ingredients) => {
  const commonAllergens = ['花生', '坚果', '海鲜', '鸡蛋', '牛奶', '大豆', '小麦']
  const detected = []

  if (ingredients?.required) {
    ingredients.required.forEach(ing => {
      if (commonAllergens.some(allergen =>
          ing.name?.includes(allergen) || ing.name?.includes(allergen))) {
        if (!detected.includes(extractAllergenName(ing.name || ing.name))) {
          detected.push(...extractAllergenName(ing.name || ing.name))
        }
      }
    })
  }

  if (ingredients?.optional) {
    ingredients.optional.forEach(ing => {
      if (commonAllergens.some(allergen =>
          ing.name?.includes(allergen) || ing.name?.includes(allergen))) {
        if (!detected.includes(extractAllergenName(ing.name || ing.name))) {
          detected.push(...extractAllergenName(ing.name || ing.name))
        }
      }
    })
  }

  return detected
}

// 提取过敏原名称
const extractAllergenName = (name) => {
  const allergenMap = {
    '花生': '花生',
    '坚果': '坚果',
    '海鲜': '海鲜',
    '鸡蛋': '鸡蛋',
    '牛奶': '牛奶',
    '大豆': '大豆',
    '小麦': '小麦'
  }
  return allergenMap[name] || name
}

// 切换可选食材
const toggleOptionalIngredient = (ingredient) => {
  ingredient.selected = !ingredient.selected
  // TODO: 可以添加到购物车时自动计算价格差
}

// 加入购物车：后端没有购物车接口，购物车状态由商家详情页维护。
// 这里把菜品与数量打包成 query 跳到商家详情页，复用其 addToCart 通路落库到页面购物车。
const addToCart = async () => {
  try {
    const cartPayload = {
      dishId: dishId.value,
      dishName: dish.value?.name || '',
      price: dish.value?.price || 0,
      quantity: quantity.value,
      customization: ''
    }
    await router.push({
      path: '/user/home/merchant-detail',
      query: {
        id: dish.value?.merchantId,
        addToCart: JSON.stringify(cartPayload)
      }
    })
  } catch (error) {
    console.error('加入购物车失败:', error)
    ElMessage.error('加入购物车失败')
  }
}

// 移出购物车
const removeFromCart = async () => {
  try {
    const response = await axios.delete(`${API_CONFIG.baseURL}/cart/remove/${dishId.value}`)

    if (response.data?.success) {
      ElMessage.success('已移出购物车')
      isInCart.value = false
      quantity.value = 1
    } else {
      ElMessage.error(response.data?.message || '移出失败')
    }
  } catch (error) {
    console.error('移出购物车失败:', error)
    ElMessage.error('移出失败')
  }
}

// 处理图片加载错误：降级到本地占位态
const handleImageError = () => {
  imageFailed.value = true
}

onMounted(() => {
  loadDishDetail()
})
</script>

<style scoped>
/* user 单列居中 1200px（§3.3 栅格） */
.dish-detail-container {
  box-sizing: border-box;
  max-width: var(--page-max);
  margin: 0 auto;
  padding: 0 var(--page-gutter) var(--space-8);
}

.loading-container {
  min-height: 400px;
}

.dish-detail-content {
  display: flex;
  flex-direction: column;
  gap: var(--card-gap);
}

/* 主信息卡：菜品图 + 价格 / 库存 / 规格 */
.dish-hero-card {
  overflow: hidden;
}

.dish-hero {
  display: flex;
  align-items: stretch;
}

.dish-media {
  flex: 0 0 440px;
  min-height: 300px;
  background: var(--canvas-sunken);
}

.dish-image {
  display: block;
  width: 100%;
  height: 100%;
  min-height: 300px;
  object-fit: cover;
}

/* 本地占位态：接口只给第三方占位图时走这里；颜色一律取 tokens.css（§7.1） */
.dish-media-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  width: 100%;
  height: 100%;
  min-height: 300px;
  background: var(--brand-50);
  color: var(--brand-500);
  font-family: var(--font-sans);
  font-size: var(--text-display);
  font-weight: var(--weight-semibold);
}

.dish-info {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: var(--space-4);
  min-width: 0;
  padding: var(--card-padding);
}

.dish-info-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-3);
}

.dish-name {
  margin: 0;
  font-size: var(--text-h2);
  line-height: 28px;
  font-weight: var(--weight-semibold);
  color: var(--ink-900);
}

.dish-price-row {
  display: flex;
  align-items: baseline;
  gap: var(--space-2);
}

/* 金额用中性色 + 等宽数字（§5.2、§3.2），不再用红/绿单独染色 */
.current-price {
  font-family: var(--font-num);
  font-variant-numeric: tabular-nums;
  font-size: var(--text-display);
  line-height: 40px;
  font-weight: var(--weight-bold);
  color: var(--ink-900);
}

.unit-price {
  font-size: var(--text-body);
  color: var(--ink-500);
}

.dish-meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-4);
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-body);
}

.meta-item .label {
  color: var(--ink-500);
}

.meta-item .value {
  color: var(--ink-700);
  font-weight: var(--weight-medium);
}

.section-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
  color: var(--ink-900);
}

.section-title .el-icon {
  color: var(--brand-500);
}

.description-text {
  margin: 0;
  font-size: var(--text-body);
  line-height: 22px;
  color: var(--ink-700);
}

.ingredient-group + .ingredient-group {
  margin-top: var(--space-5);
}

.ingredient-group-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
  font-size: var(--text-body);
  font-weight: var(--weight-semibold);
  color: var(--ink-900);
}

.ingredient-group-title.required-group {
  color: var(--warning);
}

.ingredients-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
}

.optional-ingredient-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  background: var(--canvas-sunken);
  border-radius: var(--radius-sm);
  transition: background var(--duration-fast) var(--ease-out);
}

.optional-ingredient-item:hover {
  background: var(--brand-50);
}

.ingredient-name {
  font-size: var(--text-body);
  color: var(--ink-700);
}

.ingredient-price {
  font-family: var(--font-num);
  font-variant-numeric: tabular-nums;
  font-size: var(--text-body-sm);
  color: var(--ink-500);
}

.allergen-tag {
  margin-left: var(--space-1);
}

.step-title {
  font-size: var(--text-body);
  font-weight: var(--weight-medium);
  color: var(--ink-900);
}

.step-description {
  margin-top: var(--space-1);
  font-size: var(--text-body-sm);
  color: var(--ink-700);
}

.step-meta {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  margin-top: var(--space-1);
  font-size: var(--text-caption);
  color: var(--ink-500);
}

.nutrition-grid .el-col {
  margin-bottom: var(--space-4);
}

.nutrition-item {
  padding: var(--space-4);
  background: var(--canvas-sunken);
  border-radius: var(--radius-sm);
  text-align: center;
}

.nutrition-label {
  margin-bottom: var(--space-1);
  font-size: var(--text-body-sm);
  color: var(--ink-500);
}

.nutrition-value {
  font-family: var(--font-num);
  font-variant-numeric: tabular-nums;
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
  color: var(--ink-900);
}

.alert-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
  color: var(--warning);
}

.alert-icon {
  font-size: var(--text-h3);
}

.alert-content {
  line-height: 22px;
  color: var(--ink-700);
}

.alert-content p {
  margin: 0 0 var(--space-2);
}

.tip {
  margin-top: var(--space-2);
  font-size: var(--text-body-sm);
  color: var(--ink-500);
}

.remark-text {
  margin: 0;
  font-size: var(--text-body);
  line-height: 22px;
  color: var(--ink-700);
  white-space: pre-wrap;
}

/* 底栏只承载数量与主操作；主按钮唯一实心强调色 --brand-500（§4.1） */
.bottom-actions {
  position: sticky;
  bottom: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-3);
  margin-top: var(--space-6);
  padding: var(--space-4) var(--card-padding);
  background: var(--surface);
  border-top: 1px solid var(--line);
  border-radius: var(--radius-lg) var(--radius-lg) 0 0;
  box-shadow: var(--shadow-md);
}

.quantity-input {
  width: 168px;
}

.add-to-cart-btn,
.remove-from-cart-btn {
  height: var(--control-height-md);
  padding: 0 var(--space-6);
}

@media (max-width: 768px) {
  .dish-detail-container {
    padding-left: var(--space-4);
    padding-right: var(--space-4);
  }

  .dish-hero {
    flex-direction: column;
  }

  .dish-media {
    flex-basis: auto;
    min-height: 220px;
  }

  .dish-image,
  .dish-media-placeholder {
    min-height: 220px;
  }

  .bottom-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .quantity-input {
    width: 100%;
  }

  .add-to-cart-btn,
  .remove-from-cart-btn {
    width: 100%;
  }
}
</style>
