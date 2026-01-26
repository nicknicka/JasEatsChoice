<template>
  <el-dialog
    v-model="visible"
    width="750px"
    title="批量定制商品"
    class="batch-customize-dialog"
    :close-on-click-modal="false"
  >
    <div v-if="products && products.length > 0" class="batch-customize">
      <!-- 统一设置 -->
      <div class="customize-section">
        <div class="section-header">
          <el-icon :size="18" color="#409eff"><Setting /></el-icon>
          <span class="section-title">统一设置</span>
          <el-tag size="small" type="info">对所有商品生效</el-tag>
        </div>

        <div class="batch-settings">
          <!-- 统一数量 -->
          <div class="setting-item">
            <div class="setting-label">
              <span class="label-text">数量</span>
              <el-tooltip content="为所有商品设置相同的数量" placement="top">
                <el-icon class="label-icon"><QuestionFilled /></el-icon>
              </el-tooltip>
            </div>
            <el-input-number
              v-model="batchSettings.quantity"
              :min="1"
              :max="99"
              size="small"
              controls-position="right"
            />
          </div>

          <!-- 统一备注 -->
          <div class="setting-item">
            <div class="setting-label">
              <span class="label-text">统一备注</span>
              <el-tooltip content="为所有商品添加相同的备注" placement="top">
                <el-icon class="label-icon"><QuestionFilled /></el-icon>
              </el-tooltip>
            </div>
            <el-input
              v-model="batchSettings.remark"
              placeholder="统一备注（可选）"
              clearable
            />
          </div>
        </div>
      </div>

      <!-- 分隔线 -->
      <el-divider>或单独设置每个商品</el-divider>

      <!-- 商品列表 -->
      <div class="products-list">
        <div
          v-for="product in products"
          :key="product.id"
          class="product-customize-item"
        >
          <!-- 商品头部 -->
          <div class="product-header">
            <div class="product-info">
              <img
                v-if="product.image"
                :src="product.image"
                :alt="product.name"
                class="product-thumb"
              />
              <div v-else class="product-thumb-placeholder">
                <el-icon :size="24"><Food /></el-icon>
              </div>
              <div class="product-details">
                <h5 class="product-name">{{ product.name }}</h5>
                <span class="product-price">¥{{ (product.price || 0).toFixed(2) }}</span>
              </div>
            </div>
            <el-button
              size="small"
              text
              @click="toggleProductExpand(product.id)"
            >
              {{ isProductExpanded(product.id) ? '收起' : '展开设置' }}
              <el-icon class="expand-icon" :class="{ expanded: isProductExpanded(product.id) }">
                <ArrowDown />
              </el-icon>
            </el-button>
          </div>

          <!-- 商品定制选项 -->
          <transition name="expand">
            <div v-show="isProductExpanded(product.id)" class="product-settings">
              <!-- 数量 -->
              <div class="setting-row">
                <div class="setting-label">
                  <el-icon><Histogram /></el-icon>
                  <span>数量</span>
                </div>
                <el-input-number
                  v-model="productSettings[product.id].quantity"
                  :min="1"
                  :max="99"
                  size="small"
                  controls-position="right"
                />
              </div>

              <!-- 可选食材 -->
              <div
                v-if="product.optionalIngredients && product.optionalIngredients.length > 0"
                class="setting-row"
              >
                <div class="setting-label">
                  <el-icon><CirclePlus /></el-icon>
                  <span>可选食材</span>
                </div>
                <div class="optional-ingredients-compact">
                  <el-tag
                    v-for="ingredient in product.optionalIngredients"
                    :key="ingredient.id || ingredient.name"
                    :type="isIngredientSelected(product.id, ingredient) ? 'success' : 'info'"
                    effect="plain"
                    size="small"
                    closable
                    @close="toggleProductIngredient(product.id, ingredient)"
                    class="ingredient-tag"
                  >
                    {{ ingredient.name }}
                    <span v-if="ingredient.price" class="ingredient-price">
                      +¥{{ ingredient.price.toFixed(2) }}
                    </span>
                  </el-tag>
                  <el-button
                    size="small"
                    text
                    @click="showIngredientSelector(product.id)"
                  >
                    <el-icon><Plus /></el-icon>
                    添加
                  </el-button>
                </div>
              </div>

              <!-- 备注 -->
              <div class="setting-row">
                <div class="setting-label">
                  <el-icon><Edit /></el-icon>
                  <span>备注</span>
                </div>
                <el-input
                  v-model="productSettings[product.id].remark"
                  placeholder="添加备注..."
                  size="small"
                  maxlength="50"
                  show-word-limit
                />
              </div>

              <!-- 小计 -->
              <div class="setting-row subtotal">
                <span class="subtotal-label">小计：</span>
                <span class="subtotal-price">
                  ¥{{ getProductSubtotal(product.id).toFixed(2) }}
                </span>
              </div>
            </div>
          </transition>
        </div>
      </div>

      <!-- 总计 -->
      <div class="total-summary">
        <div class="summary-content">
          <div class="summary-info">
            <span class="summary-label">已选商品</span>
            <span class="summary-value">{{ products.length }} 个</span>
          </div>
          <div class="summary-divider"></div>
          <div class="summary-info">
            <span class="summary-label">总计</span>
            <span class="summary-value total-price">¥{{ totalAmount.toFixed(2) }}</span>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <el-empty description="请先选择商品"></el-empty>
    </div>

    <!-- 食材选择器抽屉 -->
    <el-drawer
      v-model="ingredientSelectorVisible"
      title="选择可选食材"
      direction="rtl"
      size="350px"
    >
      <div v-if="currentProductId" class="ingredient-selector">
        <div class="ingredient-list">
          <div
            v-for="ingredient in getCurrentProductOptionalIngredients()"
            :key="ingredient.id || ingredient.name"
            class="ingredient-item"
            @click="toggleProductIngredient(currentProductId, ingredient)"
          >
            <div class="ingredient-checkbox">
              <el-checkbox
                :model-value="isIngredientSelected(currentProductId, ingredient)"
              />
            </div>
            <div class="ingredient-content">
              <div class="ingredient-header">
                <span class="ingredient-name">{{ ingredient.name }}</span>
                <span v-if="ingredient.price" class="ingredient-price">
                  +¥{{ ingredient.price.toFixed(2) }}
                </span>
              </div>
              <p v-if="ingredient.description" class="ingredient-description">
                {{ ingredient.description }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">
          <el-icon><Close /></el-icon> 取消
        </el-button>
        <el-button type="primary" @click="handleConfirm">
          <el-icon><Select /></el-icon>
          确认定制
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import {
  Setting,
  QuestionFilled,
  Food,
  ArrowDown,
  Histogram,
  CirclePlus,
  Edit,
  Plus,
  Close,
  Select
} from '@element-plus/icons-vue'

/**
 * 批量定制对话框组件
 * @description 支持批量设置和单独设置每个商品的配置
 */
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  products: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'confirm'])

const visible = ref(props.modelValue)

// 批量设置
const batchSettings = ref({
  quantity: 1,
  remark: ''
})

// 单个商品设置
const productSettings = ref({})

// 展开状态
const expandedProducts = ref(new Set())

// 食材选择器
const ingredientSelectorVisible = ref(false)
const currentProductId = ref(null)

/**
 * 初始化商品设置
 */
const initProductSettings = () => {
  productSettings.value = {}
  props.products.forEach(product => {
    productSettings.value[product.id] = {
      quantity: 1,
      optionalIngredients: [],
      remark: ''
    }
  })
}

/**
 * 检查商品是否展开
 */
const isProductExpanded = (productId) => {
  return expandedProducts.value.has(productId)
}

/**
 * 切换商品展开状态
 */
const toggleProductExpand = (productId) => {
  if (expandedProducts.value.has(productId)) {
    expandedProducts.value.delete(productId)
  } else {
    expandedProducts.value.add(productId)
  }
}

/**
 * 获取商品的可选食材列表
 */
const getCurrentProductOptionalIngredients = () => {
  const product = props.products.find(p => p.id === currentProductId.value)
  return product?.optionalIngredients || []
}

/**
 * 检查食材是否已选择
 */
const isIngredientSelected = (productId, ingredient) => {
  return productSettings.value[productId]?.optionalIngredients.some(
    item => item.name === ingredient.name
  )
}

/**
 * 切换商品食材选择
 */
const toggleProductIngredient = (productId, ingredient) => {
  const settings = productSettings.value[productId]
  if (!settings) return

  const index = settings.optionalIngredients.findIndex(
    item => item.name === ingredient.name
  )

  if (index === -1) {
    settings.optionalIngredients.push({
      name: ingredient.name,
      price: ingredient.price || 0
    })
  } else {
    settings.optionalIngredients.splice(index, 1)
  }
}

/**
 * 显示食材选择器
 */
const showIngredientSelector = (productId) => {
  currentProductId.value = productId
  ingredientSelectorVisible.value = true
}

/**
 * 计算商品小计
 */
const getProductSubtotal = (productId) => {
  const product = props.products.find(p => p.id === productId)
  const settings = productSettings.value[productId]
  if (!product || !settings) return 0

  const basePrice = product.price || 0
  const extrasPrice = settings.optionalIngredients.reduce(
    (sum, ingredient) => sum + (ingredient.price || 0),
    0
  )

  return (basePrice + extrasPrice) * settings.quantity
}

/**
 * 计算总金额
 */
const totalAmount = computed(() => {
  let total = 0
  props.products.forEach(product => {
    total += getProductSubtotal(product.id)
  })
  return total
})

/**
 * 应用批量设置
 */
watch(() => [batchSettings.value.quantity, batchSettings.value.remark], ([newQuantity, newRemark]) => {
  props.products.forEach(product => {
    if (productSettings.value[product.id]) {
      if (newQuantity) {
        productSettings.value[product.id].quantity = newQuantity
      }
      if (newRemark) {
        productSettings.value[product.id].remark = newRemark
      }
    }
  })
}, { deep: true })

/**
 * 确认定制
 */
const handleConfirm = () => {
  const customizations = {}
  Object.keys(productSettings.value).forEach(productId => {
    customizations[productId] = productSettings.value[productId]
  })

  emit('confirm', customizations)
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
 * 监听对话框打开
 */
watch(() => props.modelValue, (newVal) => {
  visible.value = newVal
  if (newVal) {
    initProductSettings()
    // 默认展开第一个商品
    if (props.products.length > 0) {
      expandedProducts.value.add(props.products[0].id)
    }
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
.batch-customize-dialog {
  :deep(.el-dialog__body) {
    padding: 20px;
    max-height: 650px;
    overflow-y: auto;
  }
}

.batch-customize {
  .customize-section {
    margin-bottom: 20px;
    padding: 16px;
    background: #fafbfc;
    border-radius: 8px;
    border: 1px solid #e4e7ed;

    .section-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 16px;

      .section-title {
        font-size: 15px;
        font-weight: 600;
        color: #303133;
      }
    }

    .batch-settings {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 16px;

      .setting-item {
        display: flex;
        flex-direction: column;
        gap: 8px;

        .setting-label {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 14px;
          font-weight: 500;
          color: #606266;

          .label-icon {
            font-size: 14px;
            color: #909399;
            cursor: help;
          }
        }
      }
    }
  }

  :deep(.el-divider__text) {
    font-size: 13px;
    color: #909399;
  }

  .products-list {
    margin-bottom: 20px;

    .product-customize-item {
      margin-bottom: 12px;
      padding: 16px;
      background: white;
      border: 2px solid #e4e7ed;
      border-radius: 8px;
      transition: all 0.3s;

      &:hover {
        border-color: #409eff;
      }

      .product-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;

        .product-info {
          display: flex;
          align-items: center;
          gap: 12px;

          .product-thumb {
            width: 50px;
            height: 50px;
            border-radius: 6px;
            object-fit: cover;
          }

          .product-thumb-placeholder {
            width: 50px;
            height: 50px;
            border-radius: 6px;
            background: #f0f0f0;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #909399;
          }

          .product-details {
            .product-name {
              font-size: 15px;
              font-weight: 600;
              margin: 0 0 4px 0;
              color: #303133;
            }

            .product-price {
              font-size: 14px;
              color: #f56c6c;
              font-weight: 600;
            }
          }
        }

        .expand-icon {
          transition: transform 0.3s;

          &.expanded {
            transform: rotate(180deg);
          }
        }
      }

      .product-settings {
        padding-top: 12px;
        border-top: 1px dashed #e4e7ed;

        .setting-row {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 12px;

          &:last-child {
            margin-bottom: 0;
          }

          .setting-label {
            display: flex;
            align-items: center;
            gap: 6px;
            min-width: 80px;
            font-size: 14px;
            font-weight: 500;
            color: #606266;
          }

          .optional-ingredients-compact {
            flex: 1;
            display: flex;
            gap: 6px;
            flex-wrap: wrap;
            align-items: center;

            .ingredient-tag {
              .ingredient-price {
                margin-left: 4px;
                font-size: 12px;
                color: #f56c6c;
              }
            }
          }

          &.subtotal {
            justify-content: flex-end;
            padding-top: 8px;
            border-top: 1px dashed #e4e7ed;

            .subtotal-label {
              font-size: 14px;
              color: #606266;
            }

            .subtotal-price {
              font-size: 20px;
              font-weight: 700;
              color: #f56c6c;
            }
          }
        }
      }
    }
  }

  .total-summary {
    padding: 16px;
    background: linear-gradient(135deg, #f0f9ff 0%, #ffffff 100%);
    border: 1px solid #b3d8ff;
    border-radius: 8px;

    .summary-content {
      display: flex;
      align-items: center;
      justify-content: space-around;

      .summary-info {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 6px;

        .summary-label {
          font-size: 13px;
          color: #909399;
        }

        .summary-value {
          font-size: 18px;
          font-weight: 600;
          color: #303133;

          &.total-price {
            font-size: 24px;
            font-weight: 700;
            color: #f56c6c;
          }
        }
      }

      .summary-divider {
        width: 1px;
        height: 40px;
        background: #dcdfe6;
      }
    }
  }
}

// 食材选择器样式
.ingredient-selector {
  .ingredient-list {
    display: flex;
    flex-direction: column;
    gap: 12px;

    .ingredient-item {
      display: flex;
      gap: 12px;
      padding: 12px;
      background: white;
      border: 2px solid #e4e7ed;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        border-color: #409eff;
      }

      .ingredient-checkbox {
        flex-shrink: 0;
        padding-top: 2px;
      }

      .ingredient-content {
        flex: 1;

        .ingredient-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 4px;

          .ingredient-name {
            font-size: 14px;
            font-weight: 500;
            color: #303133;
          }

          .ingredient-price {
            font-size: 14px;
            font-weight: 600;
            color: #f56c6c;
          }
        }

        .ingredient-description {
          font-size: 12px;
          color: #909399;
          margin: 0;
          line-height: 1.4;
        }
      }
    }
  }
}

// 展开动画
.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  max-height: 0;
  opacity: 0;
}

.expand-enter-to,
.expand-leave-from {
  max-height: 500px;
  opacity: 1;
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
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
