<template>
  <el-dialog
    v-model="visible"
    width="650px"
    :title="`定制 ${product?.name || ''}`"
    class="product-customize-dialog"
    :close-on-click-modal="false"
  >
    <div v-if="product" class="product-customize">
      <!-- 数量选择 -->
      <div class="customize-section">
        <div class="section-header">
          <el-icon :size="18" color="#409eff"><Histogram /></el-icon>
          <span class="section-title">数量</span>
          <el-tag size="small" type="info">必填</el-tag>
        </div>
        <div class="quantity-control">
          <el-input-number
            v-model="formData.quantity"
            :min="1"
            :max="99"
            size="large"
            controls-position="right"
          />
        </div>
      </div>

      <!-- 可选食材 -->
      <div
        v-if="product.optionalIngredients && product.optionalIngredients.length > 0"
        class="customize-section"
      >
        <div class="section-header">
          <el-icon :size="18" color="#67c23a"><CirclePlus /></el-icon>
          <span class="section-title">可选食材</span>
          <el-tag size="small" type="success">可多选</el-tag>
        </div>
        <div class="optional-ingredients-grid">
          <div
            v-for="ingredient in product.optionalIngredients"
            :key="getIngredientKey(ingredient)"
            class="ingredient-checkbox-wrapper"
            :class="{ checked: isIngredientSelected(ingredient) }"
            @click="toggleIngredient(ingredient)"
          >
            <div class="ingredient-card">
              <div class="ingredient-header">
                <div class="ingredient-icon-wrap">
                  <span class="ingredient-emoji">{{ getIngredientEmoji(getIngredientName(ingredient)) }}</span>
                  <el-checkbox
                    :model-value="isIngredientSelected(ingredient)"
                    @change="toggleIngredient(ingredient)"
                    @click.stop
                  />
                </div>
                <div class="ingredient-info">
                  <span class="ingredient-name">{{ getIngredientName(ingredient) }}</span>
                  <span v-if="getIngredientPrice(ingredient) > 0" class="ingredient-price">
                    +¥{{ getIngredientPrice(ingredient).toFixed(2) }}
                  </span>
                </div>
              </div>
              <p v-if="ingredient.description" class="ingredient-description">
                {{ ingredient.description }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- 必选食材展示 -->
      <div
        v-if="product.requiredIngredients && product.requiredIngredients.length > 0"
        class="customize-section"
      >
        <div class="section-header">
          <el-icon :size="18" color="#f56c6c"><Star /></el-icon>
          <span class="section-title">必选食材</span>
          <el-tag size="small" type="danger">不可修改</el-tag>
        </div>
        <div class="required-ingredients-grid">
          <div
            v-for="(ingredient, index) in product.requiredIngredients"
            :key="index"
            class="required-ingredient-item"
          >
            <div class="ingredient-icon">🥗</div>
            <div class="ingredient-content">
              <span class="ingredient-name">{{ ingredient }}</span>
              <span class="ingredient-tip">已包含</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 备注 -->
      <div class="customize-section">
        <div class="section-header">
          <el-icon :size="18" color="#e6a23c"><Edit /></el-icon>
          <span class="section-title">备注</span>
          <el-tag size="small" type="warning">可选</el-tag>
        </div>

        <!-- 快捷备注 -->
        <div class="quick-remarks">
          <el-tag
            v-for="quickRemark in quickRemarks"
            :key="quickRemark"
            effect="plain"
            :type="isQuickRemarkSelected(quickRemark) ? 'warning' : 'info'"
            @click="toggleQuickRemark(quickRemark)"
            class="quick-remark-tag"
          >
            {{ quickRemark }}
          </el-tag>
        </div>

        <el-input
          v-model="formData.remark"
          type="textarea"
          :rows="3"
          placeholder="添加备注，如：少辣、不要香菜、多放葱等..."
          maxlength="100"
          show-word-limit
          class="remark-textarea"
        />

        <!-- 备注提示 -->
        <div class="remark-tips">
          <el-icon :size="14"><InfoFilled /></el-icon>
          <span>我们会尽量满足您的需求，但不能保证完全符合</span>
        </div>
      </div>

      <!-- 价格预览 -->
      <div class="price-summary">
        <div class="summary-item">
          <span class="summary-label">商品单价</span>
          <span class="summary-value">¥{{ (product.price || 0).toFixed(2) }}</span>
        </div>
        <div v-if="extraIngredientsPrice > 0" class="summary-item">
          <span class="summary-label">加料费用</span>
          <span class="summary-value extra">+¥{{ extraIngredientsPrice.toFixed(2) }}</span>
        </div>
        <div class="summary-item total">
          <span class="summary-label">小计</span>
          <span class="summary-value total-price">¥{{ totalPrice.toFixed(2) }}</span>
        </div>
        <p class="price-tip">共 {{ formData.quantity }} 份</p>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button size="large" @click="handleClose">
          <el-icon><Close /></el-icon> 取消
        </el-button>
        <el-button
          type="primary"
          size="large"
          @click="handleConfirm"
        >
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
  Histogram,
  CirclePlus,
  Star,
  Edit,
  Close,
  Select,
  InfoFilled
} from '@element-plus/icons-vue'

/**
 * 商品定制对话框组件
 * @description 支持数量调整、可选食材选择、备注添加
 */
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  product: {
    type: Object,
    default: null
  },
  customization: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'confirm'])

const visible = ref(props.modelValue)

// 表单数据
const formData = ref({
  quantity: 1,
  optionalIngredients: [],
  remark: ''
})

// 快捷备注选项
const quickRemarks = ref([
  '不要辣',
  '微辣',
  '少辣',
  '不要香菜',
  '不要葱',
  '不要蒜',
  '多放葱',
  '多放醋',
  '少油'
])

/**
 * 初始化表单数据
 */
const initFormData = () => {
  if (props.customization) {
    formData.value = { ...props.customization }
  } else {
    formData.value = {
      quantity: 1,
      optionalIngredients: [],
      remark: ''
    }
  }
}

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
 * 获取食材唯一标识（用于key）
 */
const getIngredientKey = (ingredient) => {
  if (typeof ingredient === 'string') {
    return ingredient
  }
  if (typeof ingredient === 'object' && ingredient !== null) {
    return ingredient.id || ingredient.name || ingredient.ingredientName || ''
  }
  return String(ingredient)
}

/**
 * 检查食材是否已选择
 */
const isIngredientSelected = (ingredient) => {
  const ingredientName = getIngredientName(ingredient)
  return formData.value.optionalIngredients.some(
    item => item.name === ingredientName
  )
}

/**
 * 检查快捷备注是否已选择
 */
const isQuickRemarkSelected = (remark) => {
  return formData.value.remark.includes(remark)
}

/**
 * 切换快捷备注
 */
const toggleQuickRemark = (remark) => {
  if (isQuickRemarkSelected(remark)) {
    // 移除备注
    const remarkText = formData.value.remark
    const regex = new RegExp(`${remark}[,，]?\\s*`, 'g')
    formData.value.remark = remarkText.replace(regex, '').trim()
  } else {
    // 添加备注
    if (formData.value.remark) {
      formData.value.remark += `，${remark}`
    } else {
      formData.value.remark = remark
    }
  }
}

/**
 * 切换食材选择
 */
const toggleIngredient = (ingredient) => {
  const ingredientName = getIngredientName(ingredient)
  const ingredientPrice = getIngredientPrice(ingredient)

  const index = formData.value.optionalIngredients.findIndex(
    item => item.name === ingredientName
  )

  if (index === -1) {
    // 添加食材
    formData.value.optionalIngredients.push({
      name: ingredientName,
      price: ingredientPrice
    })
  } else {
    // 移除食材
    formData.value.optionalIngredients.splice(index, 1)
  }
}

/**
 * 根据食材名称获取对应的 emoji
 */
const getIngredientEmoji = (name) => {
  const emojiMap = {
    '鸡蛋': '🥚',
    '香菜': '🌿',
    '葱花': '🟢',
    '辣椒': '🌶️',
    '花椒': '🌿',
    '蒜': '🧄',
    '姜': '🫚',
    '豆腐': '🧈',
    '蘑菇': '🍄',
    '木耳': '🍄',
    '胡萝卜': '🥕',
    '黄瓜': '🥒',
    '西红柿': '🍅',
    '土豆': '🥔',
    '玉米': '🌽',
    '青菜': '🥬',
    '白菜': '🥬',
    '菠菜': '🥬',
    '韭菜': '🌿',
    '豆芽': '🌱',
    '花生': '🥜',
    '芝麻': '🫘',
    '芝士': '🧀',
    '培根': '🥓',
    '火腿': '🍖',
    '牛肉': '🥩',
    '猪肉': '🥩',
    '鸡肉': '🍗',
    '鱼肉': '🐟',
    '虾': '🦐',
    '蟹': '🦀',
    '海带': '🌿',
    '紫菜': '🌿',
    '粉丝': '🍜'
  }

  for (const [key, emoji] of Object.entries(emojiMap)) {
    if (name.includes(key)) {
      return emoji
    }
  }

  return '🍽️'
}

/**
 * 计算加料费用
 */
const extraIngredientsPrice = computed(() => {
  return formData.value.optionalIngredients.reduce(
    (sum, ingredient) => sum + (ingredient.price || 0),
    0
  )
})

/**
 * 计算总价
 */
const totalPrice = computed(() => {
  if (!props.product) return 0
  const basePrice = props.product.price || 0
  return (basePrice + extraIngredientsPrice.value) * formData.value.quantity
})

/**
 * 确认定制
 */
const handleConfirm = () => {
  emit('confirm', {
    productId: props.product.id,
    customization: {
      quantity: formData.value.quantity,
      optionalIngredients: formData.value.optionalIngredients,
      remark: formData.value.remark
    }
  })
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
    initFormData()
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
.product-customize-dialog {
  :deep(.el-dialog__body) {
    padding: 20px;
    max-height: 600px;
    overflow-y: auto;
  }
}

.product-customize {
  .customize-section {
    margin-bottom: 24px;
    padding: 16px;
    background: #fafbfc;
    border-radius: 8px;
    border: 1px solid #e4e7ed;

    &:last-child {
      margin-bottom: 16px;
    }

    .section-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 16px;

      // 缩小标签大小
      :deep(.el-tag) {
        height: 20px;
        line-height: 20px;
        padding: 0 6px;
        font-size: 0.75rem /* 原值: 11px */;
      }

      .section-title {
        font-size: 1.071rem /* 原值: 15px */;
        font-weight: 600;
        color: #303133;
      }
    }

    .quantity-control {
      display: flex;
      justify-content: center;
      padding: 12px 0;
    }

    .optional-ingredients-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 12px;

      .ingredient-checkbox-wrapper {
        cursor: pointer;
        transition: all 0.3s;

        .ingredient-card {
          padding: 10px;
          background: white;
          border: 2px solid #e4e7ed;
          border-radius: 6px;
          transition: all 0.3s;

          .ingredient-header {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 6px;

            .ingredient-icon-wrap {
              position: relative;
              width: 32px;
              height: 32px;
              display: flex;
              align-items: center;
              justify-content: center;
              background: #f5f7fa;
              border-radius: 6px;
              flex-shrink: 0;

              .ingredient-emoji {
                font-size: 1.286rem /* 原值: 18px */;
                line-height: 1;
              }

              :deep(.el-checkbox) {
                position: absolute;
                bottom: -6px;
                right: -6px;
                margin: 0;
                background: white;
                border-radius: 50%;
                padding: 2px;

                .el-checkbox__input {
                  .el-checkbox__inner {
                    border-width: 2px;
                  }
                }
              }
            }

            .ingredient-info {
              flex: 1;
              display: flex;
              flex-direction: column;
              gap: 4px;
              min-width: 0;

              .ingredient-name {
                font-size: 0.929rem /* 原值: 13px */;
                font-weight: 500;
                color: #303133;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
              }

              .ingredient-price {
                font-size: 0.929rem /* 原值: 13px */;
                font-weight: 600;
                color: #f56c6c;
              }
            }
          }

          .ingredient-description {
            font-size: 0.857rem /* 原值: 12px */;
            color: #909399;
            margin: 0;
            line-height: 1.4;
            padding-left: 50px;
          }
        }

        &:hover .ingredient-card {
          border-color: #409eff;
          box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
        }

        &.checked .ingredient-card {
          border-color: #67c23a;
          background: linear-gradient(135deg, #f0f9ff 0%, #ffffff 100%);
          box-shadow: 0 2px 12px rgba(103, 194, 58, 0.3);
        }
      }
    }

    .required-ingredients-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
      gap: 12px;

      .required-ingredient-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px 10px;
        background: #fef0f0;
        border: 1px solid #fde2e2;
        border-radius: 6px;
        opacity: 0.9;

        .ingredient-icon {
          font-size: 1.429rem /* 原值: 20px */;
          flex-shrink: 0;
        }

        .ingredient-content {
          display: flex;
          flex-direction: column;
          gap: 2px;
          flex: 1;
          min-width: 0;

          .ingredient-name {
            font-size: 0.929rem /* 原值: 13px */;
            font-weight: 500;
            color: #303133;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }

          .ingredient-tip {
            font-size: 10px;
            color: #909399;
          }
        }
      }
    }

    :deep(.el-textarea__inner) {
      border-radius: 8px;
    }

    .quick-remarks {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
      margin-bottom: 12px;

      .quick-remark-tag {
        cursor: pointer;
        user-select: none;
        transition: all 0.3s;
        height: 24px;
        line-height: 24px;
        padding: 0 8px;
        font-size: 0.857rem /* 原值: 12px */;

        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
        }

        &:active {
          transform: translateY(0);
        }
      }
    }

    .remark-textarea {
      margin-bottom: 8px;
    }

    .remark-tips {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 8px 12px;
      background: #fff7e6;
      border-left: 3px solid #e6a23c;
      border-radius: 4px;
      font-size: 0.857rem /* 原值: 12px */;
      color: #909399;
    }
  }

  .price-summary {
    padding: 16px;
    background: linear-gradient(135deg, #fff7e6 0%, #fff 100%);
    border: 1px solid #ffe7ba;
    border-radius: 8px;

    .summary-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 0;

      .summary-label {
        font-size: 1rem /* 原值: 14px */;
        color: #606266;
      }

      .summary-value {
        font-size: 1.143rem /* 原值: 16px */;
        font-weight: 600;
        color: #303133;

        &.extra {
          color: #67c23a;
        }
      }

      &.total {
        border-top: 1px dashed #e4e7ed;
        padding-top: 12px;
        margin-top: 4px;

        .summary-label {
          font-size: 1.143rem /* 原值: 16px */;
          font-weight: 600;
        }

        .total-price {
          font-size: 1.714rem /* 原值: 24px */;
          font-weight: 700;
          color: #f56c6c;
        }
      }
    }

    .price-tip {
      text-align: right;
      font-size: 0.929rem /* 原值: 13px */;
      color: #909399;
      margin: 12px 0 0 0;
    }
  }
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
