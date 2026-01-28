<template>
  <el-dialog
    v-model="visible"
    width="750px"
    @close="handleClose"
    @opened="handleDialogOpened"
    class="product-select-dialog"
    :close-on-click-modal="false"
    :close-on-press-escape="true"
  >
    <template #header>
      <div class="dialog-header">
        <div class="header-left">
          <el-icon :size="24" color="#409eff"><Food /></el-icon>
          <div class="header-title-group">
            <span class="header-title">{{ merchant?.name || '' }}</span>
            <span class="header-subtitle">选择您喜欢的商品</span>
          </div>
        </div>
      </div>
    </template>

    <!-- 搜索和筛选 -->
    <div class="filter-section">
      <div class="filter-row">
        <el-input
          v-model="searchInput"
          placeholder="搜索商品名称..."
          clearable
          class="search-input"
          @input="handleSearchInput"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-select
          v-model="sortBy"
          placeholder="排序方式"
          class="sort-select"
          @change="handleSortChange"
        >
          <el-option label="默认排序" value="default" />
          <el-option label="价格从低到高" value="price-asc">
            <el-icon><Sort /></el-icon> 价格从低到高
          </el-option>
          <el-option label="价格从高到低" value="price-desc">
            <el-icon><Sort /></el-icon> 价格从高到低
          </el-option>
        </el-select>
      </div>
    </div>


    <!-- 商品列表 -->
    <div v-loading="isLoading" class="product-list" v-if="merchant && merchant.products">
      <div v-if="filteredProducts.length === 0 && searchKeyword" class="no-results">
        <el-empty description="未找到匹配的商品">
          <el-button type="primary" @click="clearSearch">清除搜索</el-button>
        </el-empty>
      </div>

      <div
        v-for="product in sortedProducts"
        :key="product.id"
        class="product-item"
        :class="{ selected: isProductSelected(product.id) }"
        role="option"
        :aria-selected="isProductSelected(product.id)"
        tabindex="0"
        @click="handleProductCardClick(product, $event)"
        @keydown="handleProductKeydown($event, product)"
      >
        <!-- 商品图片 -->
        <div class="product-image">
          <img
            v-if="product.image"
            :src="product.image"
            :alt="product.name"
            loading="lazy"
            @error="handleImageError($event, product)"
          />
          <div v-else class="image-placeholder">
            <el-icon :size="28"><Food /></el-icon>
          </div>
          <div class="product-price-badge">
            <span class="price">¥{{ (product.price || 0).toFixed(2) }}</span>
          </div>
        </div>

        <!-- 商品内容 -->
        <div class="product-content">
          <div class="product-header">
            <h4 class="product-name">{{ product.name }}</h4>
          </div>

          <p class="product-description">{{ product.description }}</p>

          <!-- 食材信息 -->
          <div class="product-ingredients">
            <!-- 必选食材 -->
            <div class="ingredients-section">
              <div class="ingredients-title">
                <el-icon :size="14" color="#f56c6c"><Star /></el-icon>
                <span>必选食材</span>
              </div>
              <div v-if="getRequiredIngredients(product).length > 0" class="ingredients-tags">
                <el-tag
                  v-for="(ingredient, index) in getRequiredIngredients(product).slice(0, 5)"
                  :key="index"
                  type="danger"
                  effect="plain"
                  size="small"
                  class="ingredient-tag"
                >
                  {{ ingredient }}
                </el-tag>
                <span v-if="getRequiredIngredients(product).length > 5" class="more-ingredients">
                  +{{ getRequiredIngredients(product).length - 5 }}
                </span>
              </div>
              <div v-else class="no-ingredients">
                <span class="no-ingredients-text">暂无必选食材</span>
              </div>
            </div>

            <!-- 可选食材 -->
            <div class="ingredients-section">
              <div class="ingredients-title">
                <el-icon :size="14" color="#409eff"><CirclePlus /></el-icon>
                <span>可选食材</span>
              </div>
              <div v-if="getOptionalIngredients(product).length > 0" class="ingredients-tags">
                <el-tag
                  v-for="(ingredient, index) in getOptionalIngredients(product).slice(0, 5)"
                  :key="index"
                  type="primary"
                  effect="plain"
                  size="small"
                  class="ingredient-tag"
                >
                  {{ ingredient }}
                </el-tag>
                <span v-if="getOptionalIngredients(product).length > 5" class="more-ingredients">
                  +{{ getOptionalIngredients(product).length - 5 }}
                </span>
              </div>
              <div v-else class="no-ingredients">
                <span class="no-ingredients-text">暂无可选食材</span>
              </div>
            </div>
          </div>

          <!-- 已选配置摘要 -->
          <div v-if="getProductCustomization(product.id)" class="customization-summary">
            <el-tag size="small" type="success" closable @close="clearProductCustomization(product.id)">
              {{ getProductCustomizationSummary(product.id) }}
            </el-tag>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="product-actions">
          <el-button
            size="small"
            @click="viewProductDetail(product)"
            class="action-btn"
          >
            <el-icon><View /></el-icon>
            详情
          </el-button>

          <el-button
            v-if="!isProductSelected(product.id)"
            type="primary"
            size="small"
            @click="customizeProduct(product)"
            class="action-btn"
          >
            <el-icon><Edit /></el-icon>
            定制
          </el-button>

          <el-button
            v-else
            type="success"
            size="small"
            @click="customizeProduct(product)"
            class="action-btn"
          >
            <el-icon><Edit /></el-icon>
            修改
          </el-button>

          <el-button
            v-if="getProductCustomization(product.id)"
            type="warning"
            size="small"
            @click="handleAddToCart(product)"
            class="action-btn"
          >
            <el-icon><ShoppingCart /></el-icon>
            加入订单
          </el-button>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <el-empty description="该商家暂无商品">
        <template #image>
          <el-icon :size="64" color="#c0c4cc"><Food /></el-icon>
        </template>
        <template #description>
          <p class="empty-description">该商家暂无商品</p>
          <p class="empty-hint">试试切换其他商家看看？</p>
        </template>
      </el-empty>
    </div>

  </el-dialog>

  <!-- 商品详情对话框 -->
  <ProductDetailDialog
    v-model="productDetailVisible"
    :product="currentProduct"
  />

  <!-- 商品定制对话框 -->
  <ProductCustomizeDialog
    v-model="productCustomizeVisible"
    :product="currentProduct"
    :customization="currentProductCustomization"
    @confirm="handleProductCustomized"
  />

</template>

<script setup>
import { ref, watch, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Food,
  Search,
  ShoppingCart,
  View,
  Edit,
  Star,
  CirclePlus,
  Sort
} from '@element-plus/icons-vue'
import ProductDetailDialog from './ProductDetailDialog.vue'
import ProductCustomizeDialog from './ProductCustomizeDialog.vue'

/**
 * 商品选择对话框组件（优化版）
 * @description 支持复选框多选、批量操作、商品详情查看、自定义配置、搜索防抖、价格排序、快捷键、可访问性
 */
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  merchant: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'addToCart', 'confirm', 'confirmAll'])

// 对话框状态
const visible = ref(props.modelValue)
const searchInput = ref('')
const searchKeyword = ref('')
const sortBy = ref('default')
const isLoading = ref(false)
const previousActiveElement = ref(null)
let searchDebounceTimer = null

// 商品定制信息
const productCustomizations = ref({}) // 存储每个商品的定制信息 { productId: { quantity, optionalIngredients, remark } }

// 当前操作的商品
const currentProduct = ref(null)
const currentProductCustomization = ref(null)

// 子对话框状态
const productDetailVisible = ref(false)
const productCustomizeVisible = ref(false)

/**
 * 筛选后的商品列表
 */
const filteredProducts = computed(() => {
  if (!props.merchant || !props.merchant.products) return []

  if (!searchKeyword.value) {
    return props.merchant.products
  }

  const keyword = searchKeyword.value.toLowerCase()
  return props.merchant.products.filter(product =>
    product.name.toLowerCase().includes(keyword) ||
    (product.description && product.description.toLowerCase().includes(keyword))
  )
})

/**
 * 排序后的商品列表
 */
const sortedProducts = computed(() => {
  if (sortBy.value === 'default') return filteredProducts.value

  return [...filteredProducts.value].sort((a, b) => {
    const priceA = a.price || 0
    const priceB = b.price || 0
    return sortBy.value === 'price-asc' ? priceA - priceB : priceB - priceA
  })
})

/**
 * 处理搜索输入（带防抖）
 */
const handleSearchInput = (value) => {
  clearTimeout(searchDebounceTimer)
  searchDebounceTimer = setTimeout(() => {
    searchKeyword.value = value
  }, 300)
}

/**
 * 清除搜索
 */
const clearSearch = () => {
  searchInput.value = ''
  searchKeyword.value = ''
}

/**
 * 处理排序变化
 */
const handleSortChange = () => {
  // 排序变化时可以添加动画或反馈
}

/**
 * 处理图片加载错误
 */
const handleImageError = (event, product) => {
  event.target.src = ''
  ElMessage.warning(`商品"${product.name}"的图片加载失败`)
}

/**
 * 处理商品键盘事件
 */
const handleProductKeydown = (event, product) => {
  if (event.key === 'Enter' || event.key === ' ') {
    event.preventDefault()
    toggleProductSelection(product)
  }
}

/**
 * 处理对话框打开
 */
const handleDialogOpened = () => {
  // 保存当前焦点元素，用于关闭后恢复
  previousActiveElement.value = document.activeElement
}

/**
 * 全局快捷键处理
 */
const handleGlobalKeydown = (event) => {
  if (!visible.value) return

  // ESC键关闭对话框
  if (event.key === 'Escape') {
    handleClose()
  }
}

/**
 * 检查商品是否已选择
 */
const isProductSelected = (productId) => {
  return productId in productCustomizations.value
}

/**
 * 解析食材列表（处理可能是JSON字符串或对象数组的情况）
 */
const parseIngredients = (ingredients) => {
  if (!ingredients) return []

  let parsed = ingredients

  // 如果是字符串，尝试解析
  if (typeof ingredients === 'string') {
    try {
      parsed = JSON.parse(ingredients)
    } catch {
      // 解析失败，可能就是普通字符串
      return [ingredients]
    }
  }

  // 如果解析后是数组
  if (Array.isArray(parsed)) {
    return parsed.map(item => {
      // 如果是对象，提取name字段
      if (typeof item === 'object' && item !== null) {
        return item.name || String(item)
      }
      // 如果是字符串，直接使用
      return String(item)
    })
  }

  return []
}

/**
 * 获取必选食材列表
 */
const getRequiredIngredients = (product) => {
  // 优先使用 ingredients.mandatory 格式(商家端标准格式)
  if (product.ingredients?.mandatory && Array.isArray(product.ingredients.mandatory)) {
    return product.ingredients.mandatory
  }
  // 兼容 requiredIngredients 格式
  return parseIngredients(product.requiredIngredients)
}

/**
 * 获取可选食材列表
 */
const getOptionalIngredients = (product) => {
  // 优先使用 ingredients.optional 格式(商家端标准格式)
  if (product.ingredients?.optional && Array.isArray(product.ingredients.optional)) {
    return product.ingredients.optional
  }
  // 兼容 optionalIngredients 格式
  return parseIngredients(product.optionalIngredients)
}

/**
 * 获取商品定制信息
 */
const getProductCustomization = (productId) => {
  return productCustomizations.value[productId] || null
}

/**
 * 获取商品定制摘要
 */
const getProductCustomizationSummary = (productId) => {
  const customization = getProductCustomization(productId)
  if (!customization) return ''

  const parts = []
  if (customization.quantity > 1) {
    parts.push(`×${customization.quantity}`)
  }
  if (customization.optionalIngredients && customization.optionalIngredients.length > 0) {
    parts.push(`${customization.optionalIngredients.length}种加料`)
  }
  if (customization.remark) {
    parts.push('有备注')
  }

  return parts.join(' | ') || '默认配置'
}

/**
 * 清除商品定制信息
 */
const clearProductCustomization = (productId) => {
  delete productCustomizations.value[productId]
}

/**
 * 查看商品详情
 */
const viewProductDetail = (product) => {
  currentProduct.value = product
  productDetailVisible.value = true
}

/**
 * 定制单个商品
 */
const customizeProduct = (product) => {
  currentProduct.value = product
  currentProductCustomization.value = getProductCustomization(product.id) || {
    quantity: 1,
    optionalIngredients: [],
    remark: ''
  }
  productCustomizeVisible.value = true
}

/**
 * 处理商品定制确认
 */
const handleProductCustomized = (data) => {
  const { productId, customization } = data
  productCustomizations.value[productId] = customization
}

/**
 * 处理商品卡片点击 - 打开定制对话框
 */
const handleProductCardClick = (product, event) => {
  // 如果点击的是按钮等交互元素，不触发定制对话框
  const target = event.target
  const isInteractiveElement =
    target.closest('.action-btn') ||
    target.tagName === 'INPUT' ||
    target.tagName === 'BUTTON'

  if (isInteractiveElement) {
    return
  }

  // 直接打开定制对话框
  customizeProduct(product)
}

/**
 * 单个商品加入购物车
 */
const handleAddToCart = (product) => {
  const customization = getProductCustomization(product.id)

  emit('addToCart', {
    product,
    customization: customization || {
      quantity: 1,
      optionalIngredients: [],
      remark: ''
    }
  })

  // 重置该商品的定制配置
  clearProductCustomization(product.id)

  ElMessage.success(`${product.name} 已加入订单，配置已重置`)
}

/**
 * 关闭对话框
 */
const handleClose = () => {
  visible.value = false
  emit('update:modelValue', false)

  // 恢复焦点到之前的元素
  nextTick(() => {
    previousActiveElement.value?.focus()
  })
}

/**
 * 生命周期钩子
 */
onMounted(() => {
  document.addEventListener('keydown', handleGlobalKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleGlobalKeydown)
  clearTimeout(searchDebounceTimer)
})

/**
 * 监听外部 modelValue 变化
 */
watch(() => props.modelValue, (newVal) => {
  visible.value = newVal
  if (newVal) {
    // 对话框打开时重置状态
    productCustomizations.value = {}
    searchKeyword.value = ''
  }
})

/**
 * 监听内部 visible 变化
 */
watch(visible, (newVal) => {
  if (!newVal) {
    emit('update:modelValue', false)
  }
})
</script>

<style scoped lang="less">
.product-select-dialog {
  :deep(.el-dialog__header) {
    padding: 0;
    margin: 0;
  }

  :deep(.el-dialog__body) {
    padding: 0;
    background-color: #f5f7fa;
  }

  :deep(.el-dialog__footer) {
    padding: 12px 16px;
    background: white;
    border-top: 1px solid #e4e7ed;
  }
}

.dialog-header {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  padding: 14px 16px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;

  .header-left {
    display: flex;
    align-items: center;
    gap: 8px;

    .header-title-group {
      display: flex;
      flex-direction: column;

      .header-title {
        font-size: 18px;
        font-weight: 600;
        line-height: 1.2;
      }

      .header-subtitle {
        font-size: 12px;
        opacity: 0.9;
        margin-top: 3px;
      }
    }
  }
}

.filter-section {
  padding: 10px 16px;
  background: white;
  border-bottom: 1px solid #e4e7ed;

  .filter-row {
    display: flex;
    gap: 8px;
    align-items: center;
    flex-wrap: wrap;

    .search-input {
      flex: 1;
      min-width: 180px;
      max-width: 320px;
    }

    .sort-select {
      width: 140px;
    }
  }
}

.product-list {
  max-height: 60vh;
  overflow-y: auto;
  padding: 10px 12px;

  .product-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 8px 10px;
    background: white;
    border-radius: 8px;
    margin-bottom: 8px;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
    transition: all 0.2s;
    border: 2px solid transparent;

    &:hover {
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
    }

    &.selected {
      border-color: #67c23a;
      background: linear-gradient(135deg, #f0f9ff 0%, #ffffff 100%);
    }

    .product-image {
      position: relative;
      width: 70px;
      height: 70px;
      flex-shrink: 0;
      border-radius: 6px;
      overflow: hidden;
      background: linear-gradient(135deg, #e4e7ed 0%, #dcdfe6 100%);

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .image-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #909399;

        .el-icon {
          font-size: 28px;
        }
      }

      .product-price-badge {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
        padding: 4px 8px;
        color: white;

        .price {
          font-size: 13px;
          font-weight: 700;
        }
      }
    }

    .product-content {
      flex: 1;
      min-width: 0;

      .product-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 6px;

        .product-name {
          font-size: 15px;
          font-weight: 600;
          margin: 0;
          color: #303133;
        }
      }

      .product-description {
        font-size: 12px;
        color: #909399;
        margin: 0 0 8px 0;
        line-height: 1.5;
        display: -webkit-box;
        -webkit-line-clamp: 1;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }

      .product-ingredients {
        margin-bottom: 8px;
        padding-bottom: 8px;
        border-bottom: 1px solid #f0f0f0;

        .ingredients-section {
          margin-bottom: 8px;

          &:last-child {
            margin-bottom: 0;
          }

          .ingredients-title {
            display: flex;
            align-items: center;
            gap: 4px;
            margin-bottom: 6px;
            font-size: 12px;
            font-weight: 600;
            color: #4a5568;
          }

          .ingredients-tags {
            display: flex;
            flex-wrap: wrap;
            gap: 4px;
            align-items: center;

            .ingredient-tag {
              border-radius: 4px;
              font-size: 11px;
              padding: 2px 6px;
              height: 20px;
              line-height: 16px;
            }

            .more-ingredients {
              font-size: 11px;
              color: #909399;
              font-weight: 500;
              padding: 2px 4px;
              background-color: #f5f7fa;
              border-radius: 4px;
            }
          }

          .no-ingredients {
            padding: 4px 8px;
            background-color: #fafafa;
            border-radius: 4px;
            border: 1px dashed #e4e7ed;

            .no-ingredients-text {
              font-size: 11px;
              color: #909399;
              font-weight: 400;
            }
          }
        }
      }

      .customization-summary {
        display: flex;
        gap: 4px;
        flex-wrap: wrap;
      }
    }

    .product-actions {
      flex-shrink: 0;
      display: flex;
      flex-direction: column;
      gap: 6px;

      .action-btn {
        width: 80px;
        padding: 0 8px !important;
        height: 26px;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 3px;
        font-size: 12px;
        box-sizing: border-box;
        border-radius: 4px;
        line-height: 26px;

        // 重置所有按钮类型的默认样式差异
        &.el-button {
          margin: 0;
          vertical-align: top;
        }

        :deep(.el-icon) {
          font-size: 12px;
          width: 12px;
          height: 12px;
          display: inline-flex;
          align-items: center;
          justify-content: center;
          vertical-align: middle;
        }

        :deep(span) {
          font-size: 12px;
          line-height: 1;
          display: inline-block;
          vertical-align: middle;
        }

        // 确保所有按钮的边框和内边距一致
        &.el-button--default,
        &.el-button--primary,
        &.el-button--success,
        &.el-button--warning {
          border-width: 1px;
          box-sizing: border-box;
        }

        // 深度选择器重置 Element Plus 内部样式
        :deep(.el-button__content) {
          display: inline-flex;
          align-items: center;
          justify-content: center;
          gap: 4px;
          width: 100%;
          height: 100%;
        }
      }
    }
  }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  margin: 16px;
  border-radius: 12px;

  .empty-description {
    font-size: 14px;
    color: #606266;
    margin: 0 0 8px 0;
  }

  .empty-hint {
    font-size: 13px;
    color: #909399;
    margin: 0;
  }
}

.no-results {
  padding: 40px 20px;
  text-align: center;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .product-select-dialog {
    :deep(.el-dialog) {
      width: 95% !important;
      margin: 0 auto;
    }
  }

  .dialog-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;

    .header-right {
      width: 100%;
    }
  }

  .filter-section {
    .filter-row {
      flex-direction: column;
      align-items: stretch;

      .search-input {
        max-width: 100%;
      }

      .sort-select {
        width: 100%;
      }
    }
  }

  .product-list {
    padding: 10px;

    .product-item {
      flex-direction: column;
      align-items: stretch;
      padding: 10px;

      .product-image {
        width: 100%;
        height: 140px;
      }

      .product-content {
        width: 100%;
      }

      .product-actions {
        flex-direction: row;
        justify-content: space-between;
        width: 100%;

        .action-btn {
          flex: 1;
        }
      }
    }
  }
}

/* 优化焦点样式 */
.product-item {
  &:focus-visible {
    outline: 2px solid #409eff;
    outline-offset: 2px;
  }

  &:focus-within {
    outline: 2px solid #409eff;
    outline-offset: 2px;
  }
}

</style>
