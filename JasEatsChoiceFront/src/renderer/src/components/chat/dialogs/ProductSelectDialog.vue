<template>
  <el-dialog
    v-model="visible"
    :title="`选择 ${merchant?.name || ''} 的商品`"
    width="800px"
    @close="handleClose"
  >
    <div class="product-list" v-if="merchant">
      <div v-for="product in merchant.products" :key="product.id" class="product-item">
        <div class="product-info">
          <h4 class="product-name">{{ product.name }}</h4>
          <p class="product-description">{{ product.description }}</p>

          <!-- 必选食材 -->
          <div
            class="product-ingredients"
            v-if="product.requiredIngredients && product.requiredIngredients.length > 0"
          >
            <div class="ingredient-label">必选食材:</div>
            <div class="ingredient-list">
              <el-tag
                v-for="ingredient in product.requiredIngredients"
                :key="ingredient"
                size="small"
                type="info"
                style="margin: 0 4px 4px 0"
              >
                {{ ingredient }}
              </el-tag>
            </div>
          </div>

          <p class="product-price">¥{{ product.price.toFixed(2) }}</p>
        </div>

        <div class="product-actions">
          <el-button
            type="primary"
            size="small"
            @click="handleToggleProduct(product)"
            :class="{ 'is-selected': isProductSelected(product.id) }"
          >
            {{ isProductSelected(product.id) ? '已选择' : '选择' }}
          </el-button>

          <!-- 数量控制 -->
          <div class="quantity-control" v-if="isProductSelected(product.id)">
            <el-button size="small" @click="handleUpdateQuantity(product, -1)">-</el-button>
            <span class="quantity">{{ getProductQuantity(product.id) }}</span>
            <el-button size="small" @click="handleUpdateQuantity(product, 1)">+</el-button>
          </div>

          <!-- 可选食材选择 -->
          <div
            class="optional-ingredients"
            v-if="
              isProductSelected(product.id) &&
              product.optionalIngredients &&
              product.optionalIngredients.length > 0
            "
          >
            <div class="ingredient-label">可选食材:</div>
            <div class="ingredient-list">
              <el-checkbox-group
                :model-value="getProductOptionalIngredients(product.id)"
                @change="handleUpdateOptionalIngredients(product.id, $event)"
              >
                <el-checkbox
                  v-for="ingredient in product.optionalIngredients"
                  :key="ingredient.id"
                  :label="ingredient"
                  style="margin: 0 8px 8px 0"
                >
                  {{ ingredient.name }}
                </el-checkbox>
              </el-checkbox-group>
            </div>
          </div>

          <!-- 备注 -->
          <el-input
            v-if="isProductSelected(product.id)"
            :model-value="getProductRemark(product.id)"
            @input="handleUpdateRemark(product.id, $event)"
            placeholder="添加备注..."
            size="small"
            type="textarea"
            :rows="1"
            style="width: 100%; margin-top: 8px"
          />

          <!-- 加入购物车按钮 -->
          <el-button
            v-if="isProductSelected(product.id)"
            type="success"
            size="small"
            @click="handleAddToCart(product)"
            style="width: 100%; margin-top: 8px"
          >
            加入购物车
          </el-button>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="info" @click="handleClose">稍后再看</el-button>
        <el-button type="primary" @click="handleConfirmAll">一键加入购物车</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'

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
.product-list {
  max-height: 500px;
  overflow-y: auto;

  .product-item {
    display: flex;
    justify-content: space-between;
    padding: 16px;
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    margin-bottom: 16px;

    &:hover {
      border-color: #409eff;
    }

    .product-info {
      flex: 1;
      margin-right: 16px;

      .product-name {
        font-size: 16px;
        font-weight: 600;
        margin: 0 0 8px 0;
        color: #303133;
      }

      .product-description {
        font-size: 14px;
        color: #909399;
        margin: 0 0 12px 0;
      }

      .product-ingredients {
        margin-bottom: 8px;

        .ingredient-label {
          font-size: 12px;
          color: #606266;
          margin-bottom: 4px;
        }

        .ingredient-list {
          display: flex;
          flex-wrap: wrap;
        }
      }

      .product-price {
        font-size: 18px;
        font-weight: 600;
        color: #f56c6c;
        margin: 8px 0 0 0;
      }
    }

    .product-actions {
      width: 200px;
      display: flex;
      flex-direction: column;

      .quantity-control {
        display: flex;
        align-items: center;
        justify-content: center;
        margin-top: 8px;
        gap: 8px;

        .quantity {
          min-width: 40px;
          text-align: center;
          font-weight: 600;
        }
      }

      .optional-ingredients {
        margin-top: 12px;

        .ingredient-label {
          font-size: 12px;
          color: #606266;
          margin-bottom: 4px;
        }

        .ingredient-list {
          display: flex;
          flex-wrap: wrap;
        }
      }

      .is-selected {
        background-color: #67c23a;
        border-color: #67c23a;
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
