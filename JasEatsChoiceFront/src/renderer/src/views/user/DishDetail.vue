<template>
  <div class="dish-detail-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <CommonBackButton />
      <h2 class="page-title">菜品详情</h2>
    </div>

    <!-- 加载状态 -->
    <div v-loading="loading" element-loading-text="加载中..." class="loading-container">
      <!-- 菜品详情内容 -->
      <div v-if="dish" class="dish-detail-content">

        <!-- 菜品图片卡片 -->
        <div class="dish-image-card">
          <img
            v-if="dish.image"
            :src="dish.image"
            :alt="dish.name"
            class="dish-image"
            @error="handleImageError"
          />
          <div v-else class="no-image-placeholder">
            <el-icon :size="60"><Food /></el-icon>
            <span>{{ dish.name?.charAt(0) || '菜' }}</span>
          </div>
        </div>

        <!-- 菜品基本信息 -->
        <el-card class="dish-info-card" shadow="hover">
          <template #header>
            <div class="card-header-row">
              <div class="dish-name">{{ dish.name }}</div>
              <el-tag
                v-if="dish.status === 'available'"
                type="success"
                effect="dark">
                有售
              </el-tag>
              <el-tag
                v-else
                type="info"
                effect="plain">
                {{ dish.statusText || '已下架' }}
              </el-tag>
            </div>
            <div class="dish-price-row">
              <span class="current-price">¥{{ dish.price?.toFixed(2) || '0.00' }}</span>
              <span class="unit-price">每份</span>
            </div>
          </template>

          <div class="dish-body">
            <!-- 菜品分类和标签 -->
            <div class="dish-meta">
              <div class="meta-item">
                <span class="label">分类:</span>
                <span class="value">{{ dish.categoryName }}</span>
              </div>
              <div class="meta-item" v-if="dish.tags && dish.tags.length > 0">
                <span class="label">标签:</span>
                <el-tag
                  v-for="tag in dish.tags"
                  :key="tag"
                  size="small"
                  class="dish-tag">
                  {{ tag }}
                </el-tag>
              </div>
              <div class="meta-item">
                <span class="label">热量:</span>
                <span class="value highlight">{{ dish.calorie || 0 }} 千卡/份</span>
              </div>
            </div>

            <el-divider />

            <!-- 菜品描述 -->
            <div class="dish-description" v-if="dish.description">
              <div class="section-title">菜品介绍</div>
              <p class="description-text">{{ dish.description }}</p>
            </div>

            <!-- 食材信息 -->
            <div class="ingredients-section">
              <div class="section-title">
                <el-icon><FriedFood /></el-icon>
                主要食材
              </div>

              <div class="ingredients-content" v-if="hasIngredients">
                <!-- 必选食材 -->
                <div v-if="dish.requiredIngredients && dish.requiredIngredients.length > 0" class="ingredient-group">
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
                          size="small">
                      {{ ing.name }}
                      <span v-if="ing.price" class="ingredient-price">
                        +¥{{ ing.price?.toFixed(2) }}
                      </span>
                    </el-tag>
                  </div>
                </div>

                <!-- 可选食材 -->
                <div v-if="dish.optionalIngredients && dish.optionalIngredients.length > 0" class="ingredient-group">
                  <div class="ingredient-group-title optional-group">
                    <el-icon><CirclePlus /></el-icon>
                    <span>可选食材（可替换）</span>
                  </div>
                  <div class="ingredients-list">
                    <div
                          v-for="ing in dish.optionalIngredients"
                          :key="ing.id || ing.name"
                          class="optional-ingredient-item">
                      <el-checkbox
                            v-if="!isInCart"
                            :model="ing.selected"
                            @change="toggleOptionalIngredient(ing)"
                            size="small">
                      </el-checkbox>
                      <span class="ingredient-name">{{ ing.name }}</span>
                      <span v-if="ing.price" class="ingredient-price">
                        +¥{{ ing.price?.toFixed(2) }}
                      </span>
                      <el-tag
                            v-if="ing.isAllergen"
                            type="warning"
                            effect="plain"
                            size="mini"
                            class="allergen-tag">
                        含过敏原
                      </el-tag>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 无食材数据 -->
              <el-empty
                v-if="!hasIngredients"
                description="暂无食材信息"
                :image-size="60" />
            </div>

            <el-divider />

            <!-- 烹饪步骤 -->
            <div class="cooking-section" v-if="dish.cookingSteps && dish.cookingSteps.length > 0">
              <div class="section-title">
                <el-icon><Van /></el-icon>
                烹饪步骤
              </div>
              <el-timeline>
                <el-timeline-item
                      v-for="(step, index) in dish.cookingSteps"
                      :key="index"
                      :timestamp="step.stepTime || '预计' + (index + 1) + '分钟'">
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
            </div>

            <el-divider />

            <!-- 营养信息 -->
            <div class="nutrition-section" v-if="dish.nutrition">
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
            </div>

            <!-- 饮食禁忌提示 -->
            <div class="allergen-warning" v-if="hasAllergens">
              <el-alert
                    type="warning"
                    :closable="false"
                    show-icon>
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
                        size="small">
                    {{ allergen }}
                  </el-tag>
                  <p class="tip">如有过敏史，请谨慎点餐或选择替换食材</p>
                </div>
              </el-alert>
            </div>

            <!-- 菜品备注 -->
            <div class="dish-remark" v-if="dish.remark">
              <div class="section-title">
                <el-icon><EditPen /></el-icon>
                菜品备注
              </div>
              <p class="remark-text">{{ dish.remark }}</p>
            </div>
          </div>
        </el-card>

        <!-- 底部操作栏 -->
        <div class="bottom-actions">
          <!-- 加入购物车/选择数量 -->
          <div class="action-group">
            <el-input-number
                  v-if="dish.status === 'available'"
                  v-model="quantity"
                  :min="1"
                  :max="99"
                  size="large"
                  class="quantity-input"
                  :disabled="isInCart">
              <template #prepend>
                <el-button
                          :icon="Minus"
                          @click="quantity > 1 && quantity--" />
              </template>
              <template #append>
                <el-button
                          :icon="Plus"
                          @click="quantity < 99 && quantity++" />
              </template>
            </el-input-number>

            <el-button
                  v-if="dish.status === 'available' && !isInCart"
                  type="primary"
                  size="large"
                  class="add-to-cart-btn"
                  @click="addToCart">
              <el-icon><ShoppingCart /></el-icon>
              加入购物车
            </el-button>

            <el-button
                  v-if="isInCart"
                  type="danger"
                  size="large"
                  @click="removeFromCart">
              <el-icon><Delete /></el-icon>
              移出购物车
            </el-button>
          </div>

          <!-- 返回按钮 -->
          <el-button
                type="default"
                size="large"
                class="back-btn"
                @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty
            v-else
            description="暂无菜品详情"
            :image-size="100" />
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
  ArrowLeft,
  TrendCharts
} from '@element-plus/icons-vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'

const route = useRoute()
const router = useRouter()
const dishId = ref(route.params.id)
const dish = ref(null)
const loading = ref(true)
const quantity = ref(1)
const isInCart = ref(false)

// 营养成分数据映射
const nutritionDisplayMap = {
  protein: '蛋白质',
  fat: '脂肪',
  carbohydrate: '碳水化合物',
  fiber: '膳食纤维',
  sodium: '钠',
  calories: '热量'
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
    const dishResponse = await axios.get(`${API_CONFIG.baseURL}/dishes/${dishId.value}`)

    if (!dishResponse.data?.data) {
      throw new Error('菜品不存在')
    }

    const dishData = dishResponse.data.data

    // 获取商家信息以获取分类名称
    let categoryName = '未分类'
    if (dishData.categoryId) {
      try {
        const categoryResponse = await axios.get(`${API_CONFIG.baseURL}/categories/${dishData.categoryId}`)
        if (categoryResponse.data?.data?.name) {
          categoryName = categoryResponse.data.data.name
        }
      } catch (error) {
        console.error('获取分类名称失败:', error)
      }
    }

    // 组装菜品详情数据
    dish.value = {
      ...dishData,
      categoryName,
      status: dishData.status === 1 ? 'available' : 'unavailable',
      statusText: dishData.status === 1 ? '有售' : '已下架',
      // 解析食材
      requiredIngredients: dishData.ingredients?.required || [],
      optionalIngredients: dishData.ingredients?.optional || [],
      // 解析烹饪步骤
      cookingSteps: dishData.cookingSteps ? JSON.parse(dishData.cookingSteps) : [],
      // 营养信息
      nutrition: dishData.nutrition ? JSON.parse(dishData.nutrition) : null,
      // 检测过敏原
      detectedAllergens: detectAllergens(dishData.ingredients)
    }

    // 检查是否已在购物车
    await checkCartStatus()

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

// 检查购物车状态
const checkCartStatus = async () => {
  try {
    const response = await axios.get(`${API_CONFIG.baseURL}/cart/check/${dishId.value}`)
    isInCart.value = response.data?.data?.inCart || false
  } catch (error) {
    console.error('检查购物车状态失败:', error)
  }
}

// 切换可选食材
const toggleOptionalIngredient = (ingredient) => {
  ingredient.selected = !ingredient.selected
  // TODO: 可以添加到购物车时自动计算价格差
}

// 加入购物车
const addToCart = async () => {
  try {
    const response = await axios.post(`${API_CONFIG.baseURL}/cart/add`, {
      dishId: dishId.value,
      quantity: quantity.value,
      optionalIngredients: dish.value.optionalIngredients?.filter(ing => ing.selected).map(ing => ({
        ingredientId: ing.id || ing.name,
        quantity: 1
      }))
    })

    if (response.data?.success) {
      ElMessage.success('已加入购物车')
      isInCart.value = true
    } else {
      ElMessage.error(response.data?.message || '加入购物车失败')
    }
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

// 返回上一页
const goBack = () => {
  router.back()
}

// 处理图片加载错误
const handleImageError = (event) => {
  const img = event.target
  img.style.display = 'none'
  const parent = img.parentElement
  if (parent && !parent.querySelector('.no-image')) {
    const noImageDiv = document.createElement('div')
    noImageDiv.className = 'no-image'
    noImageDiv.innerHTML = '<span>菜</span>'
    parent.appendChild(noImageDiv)
  }
}

onMounted(() => {
  loadDishDetail()
})
</script>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';

.dish-detail-container {
  padding: 0 @nordic-space-lg @nordic-space-lg;
  background: @nordic-bg;
  min-height: calc(100vh - 80px);
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: @nordic-space-lg;
  padding: @nordic-space-md @nordic-space-lg;
  background: @nordic-surface;
  border-radius: @nordic-radius-lg;
  box-shadow: 0 2px 8px @nordic-shadow;
}

.page-title {
  font-size: @nordic-text-lg;
  margin: 0;
  color: @nordic-text;
  font-weight: 600;
}

.loading-container {
  min-height: 400px;
}

.dish-detail-content {
  display: flex;
  flex-direction: column;
  gap: @nordic-space-lg;
}

.dish-image-card {
  border-radius: @nordic-radius-lg;
  overflow: hidden;
  box-shadow: 0 2px 12px @nordic-shadow;
  background: @nordic-surface;
}

.dish-image {
  width: 100%;
  height: 300px;
  object-fit: cover;
  display: block;
}

.no-image-placeholder {
  width: 100%;
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, @nordic-accent 0%, @nordic-accent-dark 100%);
  color: @nordic-surface;
  font-size: 3.429rem /* 原值: 48px */;
}

.no-image-placeholder span {
  font-size: 3.429rem /* 原值: 48px */;
}

.dish-info-card {
  background: @nordic-surface;
  border-radius: @nordic-radius-lg;
  box-shadow: 0 2px 12px @nordic-shadow;
  overflow: hidden;
}

.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: @nordic-space-lg 24px;
  border-bottom: 1px solid @nordic-border;
}

.dish-name {
  font-size: @nordic-text-lg;
  font-weight: 600;
  color: @nordic-text;
  flex: 1;
  margin-right: @nordic-space-md;
}

.dish-price-row {
  display: flex;
  align-items: baseline;
  gap: @nordic-space-sm;
}

.current-price {
  font-size: 2.286rem /* 原值: 32px */;
  font-weight: 700;
  color: @nordic-red;
}

.unit-price {
  font-size: @nordic-text-base;
  color: @nordic-text-muted;
}

.dish-body {
  padding: @nordic-space-lg;
}

.dish-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: @nordic-space-lg;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: @nordic-text-base;
}

.meta-item .label {
  color: @nordic-text-muted;
}

.meta-item .value {
  color: @nordic-text;
  font-weight: 500;
}

.section-title {
  font-size: @nordic-text-md;
  font-weight: 600;
  color: @nordic-text;
  margin-bottom: @nordic-space-md;
  display: flex;
  align-items: center;
  gap: @nordic-space-sm;
}

.section-title .el-icon {
  color: @nordic-yellow;
}

.description-text {
  color: @nordic-text-secondary;
  line-height: 1.6;
}

.ingredients-section {
  margin-bottom: @nordic-space-lg;
}

.ingredient-group {
  margin-bottom: @nordic-space-lg;
}

.ingredient-group-title {
  font-size: @nordic-text-base;
  font-weight: 600;
  color: @nordic-text;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.ingredient-group-title.required-group {
  color: @nordic-yellow;
}

.ingredient-group-title.optional-group {
  color: @nordic-green;
}

.ingredients-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.optional-ingredient-item {
  display: flex;
  align-items: center;
  gap: @nordic-space-sm;
  padding: @nordic-space-sm 12px;
  background: @nordic-bg;
  border-radius: @nordic-radius-sm;
  transition: all @nordic-transition-slow;
}

.optional-ingredient-item:hover {
  background: @nordic-blue-light;
  transform: translateY(-2px);
}

.ingredient-name {
  font-size: @nordic-text-base;
  color: @nordic-text;
  flex: 1;
}

.ingredient-price {
  font-size: @nordic-text-xs;
  color: @nordic-red;
}

.allergen-tag {
  margin-left: @nordic-space-xs;
}

.cooking-section {
  margin-bottom: @nordic-space-lg;
}

.nutrition-section {
  margin-bottom: @nordic-space-lg;
}

.nutrition-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: @nordic-space-md;
}

.nutrition-item {
  padding: 12px;
  background: @nordic-bg;
  border-radius: @nordic-radius-sm;
  text-align: center;
}

.nutrition-label {
  font-size: @nordic-text-xs;
  color: @nordic-text-muted;
  margin-bottom: @nordic-space-sm;
}

.nutrition-value {
  font-size: @nordic-text-md;
  font-weight: 600;
  color: @nordic-text;
}

.allergen-warning {
  margin-bottom: @nordic-space-lg;
}

.alert-title {
  display: flex;
  align-items: center;
  gap: @nordic-space-sm;
  font-size: @nordic-text-md;
  font-weight: 600;
  color: @nordic-yellow;
}

.alert-icon {
  font-size: 1.429rem /* 原值: 20px */;
}

.alert-content {
  line-height: 1.6;
  color: @nordic-text-secondary;
}

.tip {
  margin-top: @nordic-space-sm;
  font-size: @nordic-text-xs;
  color: @nordic-text-muted;
}

.dish-remark {
  margin-bottom: @nordic-space-lg;
}

.remark-text {
  color: @nordic-text-secondary;
  line-height: 1.6;
  white-space: pre-wrap;
}

.bottom-actions {
  position: sticky;
  bottom: 0;
  background: @nordic-surface;
  padding: @nordic-space-md @nordic-space-lg;
  border-top: 1px solid @nordic-border;
  box-shadow: 0 -2px 10px @nordic-shadow;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: @nordic-space-md;
}

.action-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.quantity-input {
  flex: 1;
}

.add-to-cart-btn,
.remove-from-cart-btn {
  height: 40px;
  padding: 0 @nordic-space-lg;
}

.back-btn {
  height: 40px;
  padding: 0 @nordic-space-xl;
}

@media (max-width: @nordic-breakpoint-md) {
  .bottom-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .action-group {
    width: 100%;
  }

  .quantity-input,
  .add-to-cart-btn,
  .remove-from-cart-btn {
    width: 100%;
  }

  .back-btn {
    width: 100%;
  }
}
</style>
