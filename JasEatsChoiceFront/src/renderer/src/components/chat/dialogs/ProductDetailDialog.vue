<template>
  <el-dialog
    v-model="visible"
    width="700px"
    title="商品详情"
    class="product-detail-dialog"
    :close-on-click-modal="false"
  >
    <div v-if="product" class="product-detail">
      <!-- 商品基本信息 -->
      <div class="detail-section">
        <div class="product-main-info">
          <div class="product-image-large">
            <img v-if="product.image" :src="product.image" :alt="product.name" />
            <div v-else class="image-placeholder">
              <el-icon :size="80"><Food /></el-icon>
            </div>
          </div>

          <div class="product-basic-content">
            <h3 class="product-name">{{ product.name }}</h3>
            <div class="product-price">
              <span class="price-symbol">¥</span>
              <span class="price-value">{{ (product.price || 0).toFixed(2) }}</span>
            </div>
            <p class="product-description">{{ product.description }}</p>

            <div class="product-tags">
              <el-tag
                v-if="product.category"
                type="info"
                size="small"
                effect="plain"
              >
                {{ product.category }}
              </el-tag>
              <el-tag
                v-if="product.status === 'available'"
                type="success"
                size="small"
                effect="plain"
              >
                有货
              </el-tag>
              <el-tag
                v-else-if="product.status === 'sold_out'"
                type="danger"
                size="small"
                effect="plain"
              >
                售罄
              </el-tag>
            </div>
          </div>
        </div>
      </div>

      <!-- 必选食材 -->
      <div class="detail-section">
        <div class="section-title">
          <el-icon :size="18" color="#f56c6c"><Star /></el-icon>
          <span>必选食材</span>
        </div>
        <div v-if="product.requiredIngredients && product.requiredIngredients.length > 0" class="ingredients-list">
          <el-tag
            v-for="(ingredient, index) in product.requiredIngredients"
            :key="index"
            type="danger"
            effect="plain"
            size="large"
            class="ingredient-tag"
          >
            <el-icon :size="14"><Star /></el-icon>
            {{ ingredient }}
          </el-tag>
        </div>
        <div v-else class="no-ingredients-hint">
          <span class="no-ingredients-text">暂无必选食材</span>
        </div>
        <p v-if="product.requiredIngredients && product.requiredIngredients.length > 0" class="section-tip">以上食材为商品必配，无法取消</p>
      </div>

      <!-- 可选食材 -->
      <div class="detail-section">
        <div class="section-title">
          <el-icon :size="18" color="#409eff"><CirclePlus /></el-icon>
          <span>可选食材</span>
        </div>
        <div v-if="product.optionalIngredients && product.optionalIngredients.length > 0" class="optional-ingredients-list">
          <el-tag
            v-for="(ingredient, index) in product.optionalIngredients"
            :key="ingredient.id || ingredient.name || index"
            type="primary"
            effect="plain"
            size="large"
            class="ingredient-tag"
          >
            <el-icon :size="14"><CirclePlus /></el-icon>
            {{ getIngredientName(ingredient) }}
            <span v-if="getIngredientPrice(ingredient) > 0" class="ingredient-price">
              +¥{{ getIngredientPrice(ingredient).toFixed(2) }}
            </span>
          </el-tag>
        </div>
        <div v-else class="no-ingredients-hint">
          <span class="no-ingredients-text">暂无可选食材</span>
        </div>
        <p v-if="product.optionalIngredients && product.optionalIngredients.length > 0" class="section-tip">可根据个人喜好添加，额外收费</p>
      </div>

      <!-- 营养信息 -->
      <div v-if="product.nutritionInfo" class="detail-section">
        <div class="section-title">
          <el-icon :size="18" color="#67c23a"><TrendCharts /></el-icon>
          <span>营养信息</span>
        </div>
        <div class="nutrition-grid">
          <div v-if="product.nutritionInfo.calories" class="nutrition-item">
            <span class="nutrition-label">热量</span>
            <span class="nutrition-value">{{ product.nutritionInfo.calories }} kcal</span>
          </div>
          <div v-if="product.nutritionInfo.protein" class="nutrition-item">
            <span class="nutrition-label">蛋白质</span>
            <span class="nutrition-value">{{ product.nutritionInfo.protein }} g</span>
          </div>
          <div v-if="product.nutritionInfo.fat" class="nutrition-item">
            <span class="nutrition-label">脂肪</span>
            <span class="nutrition-value">{{ product.nutritionInfo.fat }} g</span>
          </div>
          <div v-if="product.nutritionInfo.carbohydrate" class="nutrition-item">
            <span class="nutrition-label">碳水化合物</span>
            <span class="nutrition-value">{{ product.nutritionInfo.carbohydrate }} g</span>
          </div>
        </div>
      </div>

      <!-- 注意事项 -->
      <div v-if="product.allergyInfo || product.tips" class="detail-section">
        <div class="section-title">
          <el-icon :size="18" color="#e6a23c"><Warning /></el-icon>
          <span>注意事项</span>
        </div>
        <ul v-if="product.allergyInfo" class="info-list">
          <li v-for="(info, index) in product.allergyInfo" :key="index" class="info-item">
            {{ info }}
          </li>
        </ul>
        <p v-if="product.tips" class="tips-text">{{ product.tips }}</p>
      </div>
    </div>

    <div v-else class="empty-detail">
      <el-empty description="暂无商品信息"></el-empty>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button type="primary" @click="handleCustomize">
          <el-icon><Edit /></el-icon>
          去定制
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import {
  Food,
  Star,
  CirclePlus,
  TrendCharts,
  Warning,
  Edit
} from '@element-plus/icons-vue'

/**
 * 商品详情对话框组件
 * @description 展示商品的详细信息，包括图片、价格、食材、营养等
 */
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  product: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'customize'])

const visible = ref(props.modelValue)

/**
 * 去定制商品
 */
const handleCustomize = () => {
  emit('customize')
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
})

/**
 * 获取食材名称（兼容字符串和对象格式）
 */
const getIngredientName = (ingredient) => {
  if (typeof ingredient === 'string') {
    return ingredient
  }
  if (typeof ingredient === 'object' && ingredient !== null) {
    return ingredient.name || ingredient.ingredientName || ''
  }
  return String(ingredient)
}

/**
 * 获取食材价格（兼容字符串和对象格式）
 */
const getIngredientPrice = (ingredient) => {
  if (typeof ingredient === 'string') {
    return 0
  }
  if (typeof ingredient === 'object' && ingredient !== null) {
    return ingredient.price || ingredient.extraPrice || 0
  }
  return 0
}

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
.product-detail-dialog {
  :deep(.el-dialog__body) {
    padding: 20px;
    max-height: 600px;
    overflow-y: auto;
  }
}

.product-detail {
  .detail-section {
    margin-bottom: 24px;
    padding-bottom: 24px;
    border-bottom: 1px solid #f0f0f0;

    &:last-of-type {
      border-bottom: none;
      margin-bottom: 0;
      padding-bottom: 0;
    }

    .section-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 16px;
    }
  }

  .product-main-info {
    display: flex;
    gap: 20px;

    .product-image-large {
      width: 200px;
      height: 200px;
      flex-shrink: 0;
      border-radius: 12px;
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
    }

    .product-basic-content {
      flex: 1;

      .product-name {
        font-size: 20px;
        font-weight: 700;
        margin: 0 0 12px 0;
        color: #303133;
      }

      .product-price {
        display: flex;
        align-items: baseline;
        margin-bottom: 12px;

        .price-symbol {
          font-size: 18px;
          color: #f56c6c;
          font-weight: 600;
        }

        .price-value {
          font-size: 32px;
          color: #f56c6c;
          font-weight: 700;
          line-height: 1;
        }
      }

      .product-description {
        font-size: 14px;
        color: #606266;
        line-height: 1.6;
        margin: 0 0 16px 0;
      }

      .product-tags {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
      }
    }
  }

  .ingredients-list {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
    margin-bottom: 8px;

    .ingredient-tag {
      padding: 8px 16px;
      font-size: 14px;
      min-height: 32px;
      display: inline-flex;
      align-items: center;
      gap: 6px;

      .el-icon {
        display: inline-flex;
        align-items: center;
        vertical-align: middle;
      }
    }
  }

  .no-ingredients-hint {
    padding: 16px 20px;
    background-color: #fafafa;
    border-radius: 8px;
    border: 1px dashed #e4e7ed;
    text-align: center;
    margin-bottom: 8px;

    .no-ingredients-text {
      font-size: 14px;
      color: #909399;
      font-weight: 400;
    }
  }

  .section-tip {
    font-size: 13px;
    color: #909399;
    margin: 8px 0 0 0;
    line-height: 1.5;
  }

  .optional-ingredients-list {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
    margin-bottom: 8px;

    .ingredient-tag {
      padding: 8px 16px;
      font-size: 14px;
      min-height: 32px;
      display: inline-flex;
      align-items: center;
      gap: 6px;

      .el-icon {
        display: inline-flex;
        align-items: center;
        vertical-align: middle;
      }

      .ingredient-price {
        margin-left: 4px;
        font-weight: 600;
        opacity: 0.9;
      }
    }
  }

  .nutrition-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
    gap: 16px;

    .nutrition-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 16px;
      background: #f5f7fa;
      border-radius: 8px;

      .nutrition-label {
        font-size: 12px;
        color: #909399;
        margin-bottom: 8px;
      }

      .nutrition-value {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }
    }
  }

  .info-list {
    list-style: none;
    padding: 0;
    margin: 0 0 12px 0;

    .info-item {
      padding: 8px 0;
      padding-left: 24px;
      position: relative;
      font-size: 14px;
      color: #606266;
      line-height: 1.6;

      &:before {
        content: '•';
        position: absolute;
        left: 8px;
        color: #e6a23c;
        font-size: 20px;
        line-height: 1;
      }
    }
  }

  .tips-text {
    font-size: 13px;
    color: #909399;
    margin: 0;
    line-height: 1.6;
    padding: 8px 12px;
    background: #fff7e6;
    border-left: 3px solid #e6a23c;
    border-radius: 4px;
  }
}

.empty-detail {
  padding: 40px 20px;
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
