<template>
  <el-dialog
    v-model="visible"
    width="720px"
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
            {{ selectedProducts.length }} 个已选
          </el-tag>
        </div>
      </div>
    </template>

    <div class="product-list" v-if="merchant && merchant.products">
      <div
        v-for="product in merchant.products"
        :key="product.id"
        class="product-card"
        :class="{ selected: isProductSelected(product.id) }"
      >
        <div class="product-main">
          <div class="product-image">
            <img v-if="product.image" :src="product.image" :alt="product.name" />
            <div v-else class="image-placeholder">
              <el-icon :size="48"><Food /></el-icon>
            </div>
            <div class="product-price-badge">
              <span class="price">¥{{ (product.price || 0).toFixed(2) }}</span>
            </div>
          </div>

          <div class="product-content">
            <h4 class="product-name">{{ product.name }}</h4>
            <p class="product-description">{{ product.description }}</p>

            <!-- 必选食材 -->
            <div
              class="ingredients-section"
              v-if="product.requiredIngredients && product.requiredIngredients.length > 0"
            >
              <div class="ingredients-header">
                <el-icon :size="16" color="#f56c6c"><Star /></el-icon>
                <span>必选食材</span>
              </div>
              <div class="ingredients-tags">
                <el-tag
                  v-for="ingredient in product.requiredIngredients"
                  :key="ingredient"
                  size="small"
                  type="danger"
                  effect="plain"
                >
                  {{ ingredient }}
                </el-tag>
              </div>
            </div>

            <!-- 可选食材提示 -->
            <div
              class="optional-hint"
              v-if="product.optionalIngredients && product.optionalIngredients.length > 0"
            >
              <el-icon :size="14"><InfoFilled /></el-icon>
              <span>可选 {{ product.optionalIngredients.length }} 种食材</span>
            </div>
          </div>

          <div class="product-action">
            <el-button
              :type="isProductSelected(product.id) ? 'success' : 'primary'"
              size="large"
              @click="handleToggleProduct(product)"
              class="select-btn"
            >
              <el-icon>
                <Select v-if="!isProductSelected(product.id)" />
                <CircleCheck v-else />
              </el-icon>
              {{ isProductSelected(product.id) ? '已选择' : '选择商品' }}
            </el-button>
          </div>
        </div>

        <!-- 展开的面板 - 选择后显示 -->
        <transition name="panel-slide">
          <div v-if="isProductSelected(product.id)" class="product-panel">
            <div class="panel-content">
              <!-- 数量控制 -->
              <div class="control-section">
                <div class="control-header">
                  <el-icon><Histogram /></el-icon>
                  <span>数量</span>
                </div>
                <el-input-number
                  :model-value="getProductQuantity(product.id)"
                  @change="(val) => handleUpdateQuantity(product, val - getProductQuantity(product.id))"
                  :min="1"
                  :max="99"
                  size="large"
                />
              </div>

              <!-- 可选食材 -->
              <div
                class="control-section"
                v-if="product.optionalIngredients && product.optionalIngredients.length > 0"
              >
                <div class="control-header">
                  <el-icon><CirclePlus /></el-icon>
                  <span>可选食材</span>
                </div>
                <el-checkbox-group
                  :model-value="getProductOptionalIngredients(product.id)"
                  @change="handleUpdateOptionalIngredients(product.id, $event)"
                >
                  <div class="ingredients-grid">
                    <el-checkbox
                      v-for="ingredient in product.optionalIngredients"
                      :key="ingredient.id"
                      :label="ingredient"
                      class="ingredient-checkbox"
                    >
                      <span class="ingredient-name">{{ ingredient.name }}</span>
                      <span v-if="ingredient.price" class="ingredient-price">
                        +¥{{ ingredient.price.toFixed(2) }}
                      </span>
                    </el-checkbox>
                  </div>
                </el-checkbox-group>
              </div>

              <!-- 备注 -->
              <div class="control-section">
                <div class="control-header">
                  <el-icon><Edit /></el-icon>
                  <span>备注</span>
                </div>
                <el-input
                  :model-value="getProductRemark(product.id)"
                  @input="handleUpdateRemark(product.id, $event)"
                  placeholder="添加备注，如：少辣、不要香菜等..."
                  type="textarea"
                  :rows="2"
                  maxlength="100"
                  show-word-limit
                />
              </div>

              <!-- 加入购物车按钮 -->
              <el-button
                type="success"
                size="large"
                @click="handleAddToCart(product)"
                class="add-cart-btn"
              >
                <el-icon><ShoppingCart /></el-icon>
                加入订单
              </el-button>
            </div>
          </div>
        </transition>
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
        <el-button size="large" @click="handleClose">
          <el-icon><Clock /></el-icon> 稍后再看
        </el-button>
        <el-button
          type="primary"
          size="large"
          @click="handleConfirmAll"
          :disabled="selectedProducts.length === 0"
        >
          <el-icon><ShoppingCartFull /></el-icon>
          全部加入订单 ({{ selectedProducts.length }})
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import {
  Food,
  Star,
  InfoFilled,
  Select,
  CircleCheck,
  Histogram,
  CirclePlus,
  Edit,
  ShoppingCart,
  Close,
  Clock,
  ShoppingCartFull
} from '@element-plus/icons-vue'

/**
 * 商品选择对话框组件
 * @description 用于选择商家的商品，支持可选食材、备注、数量调整
 */
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  merchant: {
    type: Object,
    default: null
  },
  selectedProducts: {
    type: Array,
    default: () => []
  },
  productRemarks: {
    type: Object,
    default: () => ({})
  },
  productOptionalIngredients: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits([
  'update:modelValue',
  'toggleProduct',
  'updateQuantity',
  'updateRemark',
  'updateOptionalIngredients',
  'addToCart',
  'confirmAll'
])

const visible = ref(props.modelValue)

/**
 * 关闭对话框
 */
const handleClose = () => {
  visible.value = false
  emit('update:modelValue', false)
}

/**
 * 检查商品是否已选择
 */
const isProductSelected = (productId) => {
  return props.selectedProducts.some((item) => item.id === productId)
}

/**
 * 获取商品数量
 */
const getProductQuantity = (productId) => {
  const product = props.selectedProducts.find((item) => item.id === productId)
  return product ? product.quantity : 0
}

/**
 * 获取商品备注
 */
const getProductRemark = (productId) => {
  return props.productRemarks[productId] || ''
}

/**
 * 获取商品可选食材
 */
const getProductOptionalIngredients = (productId) => {
  return props.productOptionalIngredients[productId] || []
}

/**
 * 切换商品选择
 */
const handleToggleProduct = (product) => {
  emit('toggleProduct', product)
}

/**
 * 更新商品数量
 */
const handleUpdateQuantity = (product, change) => {
  emit('updateQuantity', { product, change })
}

/**
 * 更新商品备注
 */
const handleUpdateRemark = (productId, remark) => {
  emit('updateRemark', { productId, remark })
}

/**
 * 更新可选食材
 */
const handleUpdateOptionalIngredients = (productId, ingredients) => {
  emit('updateOptionalIngredients', { productId, ingredients })
}

/**
 * 单个商品加入购物车
 */
const handleAddToCart = (product) => {
  emit('addToCart', product)
}

/**
 * 一键加入购物车
 */
const handleConfirmAll = () => {
  emit('confirmAll')
  handleClose()
}

/**
 * 监听外部 modelValue 变化
 */
watch(() => props.modelValue, (newVal) => {
  visible.value = newVal
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

.product-list {
  max-height: 600px;
  overflow-y: auto;
  padding: 16px;

  .product-card {
    background: white;
    border-radius: 12px;
    margin-bottom: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    transition: all 0.3s;
    overflow: hidden;

    &:hover {
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
    }

    &.selected {
      border: 2px solid #67c23a;
      box-shadow: 0 4px 16px rgba(103, 194, 58, 0.2);
    }

    .product-main {
      display: flex;
      gap: 16px;
      padding: 20px;

      .product-image {
        position: relative;
        width: 120px;
        height: 120px;
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
          padding: 8px 12px;
          color: white;

          .price {
            font-size: 18px;
            font-weight: 700;
          }
        }
      }

      .product-content {
        flex: 1;

        .product-name {
          font-size: 18px;
          font-weight: 600;
          margin: 0 0 8px 0;
          color: #303133;
        }

        .product-description {
          font-size: 14px;
          color: #909399;
          margin: 0 0 12px 0;
          line-height: 1.6;
        }

        .ingredients-section {
          margin-bottom: 8px;

          .ingredients-header {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 13px;
            color: #f56c6c;
            margin-bottom: 8px;
            font-weight: 500;
          }

          .ingredients-tags {
            display: flex;
            flex-wrap: wrap;
            gap: 6px;
          }
        }

        .optional-hint {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 12px;
          color: #409eff;
          background: #ecf5ff;
          padding: 6px 12px;
          border-radius: 4px;
          width: fit-content;
        }
      }

      .product-action {
        flex-shrink: 0;
        display: flex;
        align-items: center;

        .select-btn {
          min-width: 120px;
        }
      }
    }

    .product-panel {
      border-top: 1px solid #e4e7ed;
      background: #fafbfc;

      .panel-content {
        padding: 20px;
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
        gap: 20px;

        .control-section {
          display: flex;
          flex-direction: column;
          gap: 12px;

          .control-header {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 14px;
            font-weight: 500;
            color: #606266;
          }

          .ingredients-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
            gap: 12px;

            .ingredient-checkbox {
              margin: 0;
              padding: 12px;
              background: white;
              border: 1px solid #e4e7ed;
              border-radius: 8px;
              transition: all 0.3s;

              &:hover {
                border-color: #409eff;
              }

              :deep(.el-checkbox__label) {
                display: flex;
                justify-content: space-between;
                align-items: center;
                width: 100%;
                padding-left: 8px;

                .ingredient-name {
                  flex: 1;
                }

                .ingredient-price {
                  font-size: 12px;
                  color: #f56c6c;
                  font-weight: 500;
                }
              }
            }
          }
        }

        .add-cart-btn {
          grid-column: 1 / -1;
          height: 48px;
          font-size: 16px;
          font-weight: 600;
        }
      }
    }
  }
}

// 面板滑动动画
.panel-slide-enter-active,
.panel-slide-leave-active {
  transition: all 0.3s ease;
  max-height: 800px;
  overflow: hidden;
}

.panel-slide-enter-from,
.panel-slide-leave-to {
  max-height: 0;
  opacity: 0;
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
