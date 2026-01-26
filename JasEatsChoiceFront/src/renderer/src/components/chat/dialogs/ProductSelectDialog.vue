<template>
  <el-dialog
    v-model="visible"
    width="900px"
    @close="handleClose"
    class="product-select-dialog"
    :close-on-click-modal="false"
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
        <div class="header-right">
          <el-tag type="success" size="large" effect="dark">
            {{ selectedProducts.length }} / {{ filteredProducts.length }} 已选
          </el-tag>
        </div>
      </div>
    </template>

    <!-- 搜索和筛选 -->
    <div class="filter-section">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索商品名称..."
        clearable
        class="search-input"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 批量操作栏 -->
    <div v-if="filteredProducts.length > 0" class="batch-actions">
      <span class="select-all">
        <el-checkbox
          :indeterminate="selectAllState === 1"
          :model-value="selectAllState === 2"
          @change="toggleSelectAll"
        />
        <span class="select-text">
          全选
          <span v-if="selectedProducts.length > 0" class="selected-count">
            ({{ selectedProducts.length }}/{{ filteredProducts.length }})
          </span>
        </span>
      </span>

      <el-button
        type="primary"
        :disabled="selectedProducts.length === 0"
        @click="openBatchCustomize"
        class="batch-btn"
      >
        <el-icon><Setting /></el-icon>
        批量定制 ({{ selectedProducts.length }})
      </el-button>

      <el-button
        type="success"
        :disabled="selectedProductsWithCustomization.length === 0"
        @click="handleBatchAddToCart"
        class="batch-btn"
      >
        <el-icon><ShoppingCart /></el-icon>
        批量加入订单 ({{ selectedProductsWithCustomization.length }})
      </el-button>
    </div>

    <!-- 商品列表 -->
    <div class="product-list" v-if="merchant && merchant.products">
      <div
        v-for="product in filteredProducts"
        :key="product.id"
        class="product-item"
        :class="{ selected: isProductSelected(product.id) }"
      >
        <!-- 左侧复选框 -->
        <div class="product-checkbox">
          <el-checkbox
            :model-value="isProductSelected(product.id)"
            @change="toggleProductSelection(product)"
          />
        </div>

        <!-- 商品图片 -->
        <div class="product-image">
          <img v-if="product.image" :src="product.image" :alt="product.name" />
          <div v-else class="image-placeholder">
            <el-icon :size="48"><Food /></el-icon>
          </div>
          <div class="product-price-badge">
            <span class="price">¥{{ (product.price || 0).toFixed(2) }}</span>
          </div>
        </div>

        <!-- 商品内容 -->
        <div class="product-content">
          <div class="product-header">
            <h4 class="product-name">{{ product.name }}</h4>
            <div class="product-badges">
              <el-tag
                v-if="product.requiredIngredients && product.requiredIngredients.length > 0"
                type="danger"
                size="small"
                effect="plain"
              >
                <el-icon><Star /></el-icon>
                {{ product.requiredIngredients.length }}种必选
              </el-tag>
              <el-tag
                v-if="product.optionalIngredients && product.optionalIngredients.length > 0"
                type="info"
                size="small"
                effect="plain"
              >
                <el-icon><CirclePlus /></el-icon>
                {{ product.optionalIngredients.length }}种可选
              </el-tag>
            </div>
          </div>

          <p class="product-description">{{ product.description }}</p>

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
        <el-icon :size="64" color="#c0c4cc"><Food /></el-icon>
      </el-empty>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button size="large" @click="handleClose">
          <el-icon><Close /></el-icon> 取消
        </el-button>
        <el-button
          type="primary"
          size="large"
          @click="handleConfirmAll"
          :disabled="selectedProductsWithCustomization.length === 0"
        >
          <el-icon><ShoppingCartFull /></el-icon>
          确认选择 ({{ selectedProductsWithCustomization.length }}个商品)
        </el-button>
      </div>
    </template>
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

  <!-- 批量定制对话框 -->
  <BatchCustomizeDialog
    v-model="batchCustomizeVisible"
    :products="selectedProducts"
    @confirm="handleBatchCustomized"
  />
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import {
  Food,
  Search,
  Setting,
  ShoppingCart,
  ShoppingCartFull,
  Close,
  View,
  Edit,
  Star,
  CirclePlus
} from '@element-plus/icons-vue'
import ProductDetailDialog from './ProductDetailDialog.vue'
import ProductCustomizeDialog from './ProductCustomizeDialog.vue'
import BatchCustomizeDialog from './BatchCustomizeDialog.vue'

/**
 * 商品选择对话框组件（重构版）
 * @description 支持复选框多选、批量操作、商品详情查看、自定义配置
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

const emit = defineEmits(['update:modelValue', 'addToCart', 'confirmAll'])

// 对话框状态
const visible = ref(props.modelValue)
const searchKeyword = ref('')

// 商品选择状态
const selectedProducts = ref([])
const productCustomizations = ref({}) // 存储每个商品的定制信息 { productId: { quantity, optionalIngredients, remark } }

// 当前操作的商品
const currentProduct = ref(null)
const currentProductCustomization = ref(null)

// 子对话框状态
const productDetailVisible = ref(false)
const productCustomizeVisible = ref(false)
const batchCustomizeVisible = ref(false)

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
 * 全选状态：0=未选择，1=部分选择，2=全选
 */
const selectAllState = computed(() => {
  if (selectedProducts.value.length === 0) return 0
  if (selectedProducts.value.length === filteredProducts.value.length) return 2
  return 1
})

/**
 * 已配置的商品列表
 */
const selectedProductsWithCustomization = computed(() => {
  return selectedProducts.value.filter(product =>
    productCustomizations.value[product.id]
  )
})

/**
 * 检查商品是否已选择
 */
const isProductSelected = (productId) => {
  return selectedProducts.value.some(item => item.id === productId)
}

/**
 * 切换商品选择状态
 */
const toggleProductSelection = (product) => {
  const index = selectedProducts.value.findIndex(item => item.id === product.id)
  if (index === -1) {
    selectedProducts.value.push(product)
  } else {
    selectedProducts.value.splice(index, 1)
    // 如果取消选择，同时清除定制信息
    if (!isProductSelected(product.id)) {
      delete productCustomizations.value[product.id]
    }
  }
}

/**
 * 全选/取消全选
 */
const toggleSelectAll = () => {
  if (selectAllState.value === 2) {
    // 取消全选
    selectedProducts.value = []
    productCustomizations.value = {}
  } else {
    // 全选
    selectedProducts.value = [...filteredProducts.value]
  }
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
 * 批量定制
 */
const openBatchCustomize = () => {
  if (selectedProducts.value.length === 0) {
    return
  }
  batchCustomizeVisible.value = true
}

/**
 * 处理商品定制确认
 */
const handleProductCustomized = (data) => {
  const { productId, customization } = data
  productCustomizations.value[productId] = customization

  // 如果商品还未选择，自动添加到选择列表
  if (!isProductSelected(productId)) {
    const product = props.merchant.products.find(p => p.id === productId)
    if (product) {
      selectedProducts.value.push(product)
    }
  }
}

/**
 * 处理批量定制确认
 */
const handleBatchCustomized = (customizations) => {
  // 更新所有商品的定制信息
  Object.keys(customizations).forEach(productId => {
    productCustomizations.value[productId] = customizations[productId]
  })
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
}

/**
 * 批量加入购物车
 */
const handleBatchAddToCart = () => {
  const items = selectedProductsWithCustomization.value.map(product => ({
    product,
    customization: productCustomizations.value[product.id]
  }))

  items.forEach(item => {
    emit('addToCart', item)
  })
}

/**
 * 确认所有选择
 */
const handleConfirmAll = () => {
  handleBatchAddToCart()
  handleClose()
}

/**
 * 关闭对话框
 */
const handleClose = () => {
  visible.value = false
  emit('update:modelValue', false)
}

/**
 * 监听外部 modelValue 变化
 */
watch(() => props.modelValue, (newVal) => {
  visible.value = newVal
  if (newVal) {
    // 对话框打开时重置状态
    selectedProducts.value = []
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
    padding: 16px 20px;
    background: white;
    border-top: 1px solid #e4e7ed;
  }
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;

    .header-title-group {
      display: flex;
      flex-direction: column;

      .header-title {
        font-size: 20px;
        font-weight: 600;
        line-height: 1.2;
      }

      .header-subtitle {
        font-size: 13px;
        opacity: 0.9;
        margin-top: 4px;
      }
    }
  }
}

.filter-section {
  padding: 16px 20px;
  background: white;
  border-bottom: 1px solid #e4e7ed;

  .search-input {
    width: 100%;
    max-width: 400px;
  }
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 20px;
  background: white;
  border-bottom: 1px solid #e4e7ed;

  .select-all {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 500;
    color: #303133;
    cursor: pointer;

    .select-text {
      font-size: 14px;

      .selected-count {
        color: #909399;
        font-size: 12px;
      }
    }
  }

  .batch-btn {
    display: inline-flex;
    align-items: center;
    gap: 6px;
  }
}

.product-list {
  max-height: 500px;
  overflow-y: auto;
  padding: 16px;

  .product-item {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 16px;
    background: white;
    border-radius: 12px;
    margin-bottom: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    transition: all 0.3s;
    border: 2px solid transparent;

    &:hover {
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
    }

    &.selected {
      border-color: #67c23a;
      background: linear-gradient(135deg, #f0f9ff 0%, #ffffff 100%);
    }

    .product-checkbox {
      flex-shrink: 0;
    }

    .product-image {
      position: relative;
      width: 100px;
      height: 100px;
      flex-shrink: 0;
      border-radius: 8px;
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
      }

      .product-price-badge {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
        padding: 6px 10px;
        color: white;

        .price {
          font-size: 16px;
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
        margin-bottom: 8px;

        .product-name {
          font-size: 16px;
          font-weight: 600;
          margin: 0;
          color: #303133;
        }

        .product-badges {
          display: flex;
          gap: 6px;
          flex-shrink: 0;
        }
      }

      .product-description {
        font-size: 13px;
        color: #909399;
        margin: 0 0 8px 0;
        line-height: 1.6;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }

      .customization-summary {
        display: flex;
        gap: 6px;
        flex-wrap: wrap;
      }
    }

    .product-actions {
      flex-shrink: 0;
      display: flex;
      flex-direction: column;
      gap: 8px;

      .action-btn {
        min-width: 90px;
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
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  gap: 12px;

  .el-button {
    flex: 1;
    font-weight: 500;
  }
}
</style>
