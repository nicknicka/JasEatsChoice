<template>
  <div class="dish-card">
    <div class="dish-image">
      <img
        v-if="dish.image"
        :src="dish.image"
        :alt="dish.name"
        loading="lazy"
        class="dish-img"
        @error="handleImageError"
      />
      <span v-else class="dish-emoji">{{ categoryEmoji }}</span>
    </div>

    <div class="dish-info">
      <div class="dish-name">{{ dish.name }}</div>
      <div class="dish-price-row">
        <div class="dish-price">¥{{ calculateRealTimePrice(dish).toFixed(2) }}</div>
        <el-input-number
          v-model="dish.quantity"
          :min="1"
          :max="10"
          size="small"
          class="quantity-input"
        />
      </div>

      <!-- 菜品描述 - 最多显示2行 -->
      <div v-if="dish.description" class="dish-desc">{{ dish.description }}</div>

      <!-- 简化的食材展示 -->
      <div v-if="hasIngredients" class="dish-ingredients">
        <!-- 必选食材 - 简化为标签 -->
        <div v-if="dish.requiredIngredients?.length" class="ingredient-section">
          <div class="ingredient-label">
            <span class="label-icon">🥗</span>
            <span class="label-text">必选：</span>
          </div>
          <div class="ingredient-tags">
            <el-tag size="small" type="info" v-for="ing in dish.requiredIngredients.slice(0, 3)" :key="ing">
              {{ ing }}
            </el-tag>
            <el-tag v-if="dish.requiredIngredients.length > 3" size="small" type="info">
              +{{ dish.requiredIngredients.length - 3 }}
            </el-tag>
          </div>
        </div>

        <!-- 可选食材 - 标签显示 -->
        <div v-if="dish.optionalIngredients?.length" class="optional-section">
          <div class="optional-label">
            <span class="label-icon">➕</span>
            <span class="label-text">可加：</span>
          </div>
          <div class="optional-tags">
            <el-tag
              v-for="ingredient in dish.optionalIngredients"
              :key="ingredient.id || ingredient.name"
              size="small"
              :type="ingredient.selected ? 'warning' : 'info'"
              class="optional-tag"
              :class="{ 'is-selected': ingredient.selected }"
              @click="toggleOptionalIngredient(ingredient)"
            >
              {{ ingredient.name }}
              <span v-if="ingredient.price" class="ingredient-price">
                (+¥{{ ingredient.price.toFixed(2) }})
              </span>
            </el-tag>
          </div>
        </div>
      </div>

      <el-button type="primary" size="default" @click="handleAddToCart" class="add-cart-btn">
        <el-icon><ShoppingCart /></el-icon>
        <span>加入购物车</span>
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElInputNumber, ElButton, ElTag } from 'element-plus'
import { ShoppingCart } from '@element-plus/icons-vue'

const props = defineProps({
  dish: {
    type: Object,
    required: true
  },
  categoryEmoji: {
    type: String,
    default: '🍽️'
  }
})

const emit = defineEmits(['add-to-cart'])

// 图片加载失败标记
const imageError = ref(false)

// 处理图片加载错误
const handleImageError = () => {
  imageError.value = true
}

// 计算是否有食材
const hasIngredients = computed(() => {
  return (props.dish.requiredIngredients?.length > 0) || (props.dish.optionalIngredients?.length > 0)
})

// 计算实时价格函数
const calculateRealTimePrice = (item) => {
  if (!item) {
    return 0
  }
  const optionalTotal = item.optionalIngredients?.reduce((sum, ingredient) => {
    return sum + (ingredient.selected ? ingredient.price : 0)
  }, 0) || 0
  return item.price + optionalTotal
}

const handleAddToCart = () => {
  emit('add-to-cart', props.dish)
}

// 切换可选食材选中状态
const toggleOptionalIngredient = (ingredient) => {
  ingredient.selected = !ingredient.selected
}
</script>

<style scoped lang="less">
.dish-card {
  border: 1px solid rgba(59, 130, 246, 0.1);
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  display: flex;
  flex-direction: column;

  &:hover {
    box-shadow: 0 6px 20px rgba(59, 130, 246, 0.15);
    transform: translateY(-4px);
    border-color: rgba(59, 130, 246, 0.2);
  }

  .dish-image {
    width: 100%;
    height: 140px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    overflow: hidden;
    background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf4 100%);
    flex-shrink: 0;

    .dish-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }

    .dish-emoji {
      font-size: 3.429rem /* 原值: 48px */;
      filter: drop-shadow(0 2px 4px rgba(59, 130, 246, 0.1));
    }
  }

  &:hover {
    .dish-image {
      .dish-img {
        transform: scale(1.05);
      }
    }
  }

  .dish-info {
    padding: 16px;
    flex: 1;
    display: flex;
    flex-direction: column;
  }

  .dish-name {
    font-size: 1.143rem /* 原值: 16px */;
    font-weight: 700;
    color: #1e293b;
    line-height: 1.3;
    margin-bottom: 8px;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .dish-price-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 12px;
  }

  .dish-price {
    font-size: 1.429rem /* 原值: 20px */;
    color: #f59e0b;
    font-weight: 700;
    background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    flex: 1;
  }

  .quantity-input {
    width: 100px;
    flex-shrink: 0;

    :deep(.el-input-number) {
      width: 100%;

      .el-input-number__decrease,
      .el-input-number__increase {
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
        border-color: transparent;
        color: #ffffff;
      }
    }
  }

  .dish-desc {
    font-size: 0.857rem /* 原值: 12px */;
    color: #64748b;
    margin-bottom: 12px;
    line-height: 1.5;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .dish-ingredients {
    margin-bottom: 12px;

    .ingredient-section {
      margin-bottom: 8px;

      .ingredient-label {
        display: flex;
        align-items: center;
        margin-bottom: 6px;
        font-size: 0.857rem /* 原值: 12px */;
        color: #64748b;

        .label-icon {
          font-size: 0.857rem /* 原值: 12px */;
          margin-right: 4px;
        }

        .label-text {
          font-weight: 500;
        }
      }

      .ingredient-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 6px;
      }
    }

    .optional-section {
      margin-top: 6px;
      padding-top: 10px;
      border-top: 1px dashed #e0e0e0;

      .optional-label {
        display: flex;
        align-items: center;
        margin-bottom: 6px;
        font-size: 0.857rem /* 原值: 12px */;
        color: #64748b;

        .label-icon {
          font-size: 0.857rem /* 原值: 12px */;
          margin-right: 4px;
        }

        .label-text {
          font-weight: 500;
        }
      }

      .optional-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 6px;
      }
    }

    .optional-tag {
      cursor: pointer;
      transition: all 0.2s ease;
      user-select: none;
      border-style: solid;

      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
      }

      &.is-selected {
        font-weight: 600;
        border-width: 1.5px;
      }

      .ingredient-price {
        font-size: 0.75rem /* 原值: 11px */;
        margin-left: 2px;
      }
    }
  }

  .add-cart-btn {
    width: 100%;
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    border: none;
    border-radius: 8px;
    height: 40px;
    font-size: 1rem /* 原值: 14px */;
    font-weight: 600;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 0 2px 8px rgba(59, 130, 246, 0.25);
    margin-top: auto;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;

    &:hover {
      background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
    }

    &:active {
      transform: translateY(0);
    }
  }
}
</style>
