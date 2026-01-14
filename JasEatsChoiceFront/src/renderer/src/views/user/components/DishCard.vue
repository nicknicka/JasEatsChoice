<template>
  <div class="dish-card">
    <div class="dish-image">{{ categoryEmoji }}</div>
    <div class="dish-name">{{ dish.name }}</div>
    <div class="dish-price">¥{{ calculateRealTimePrice(dish).toFixed(2) }}</div>
    <div class="dish-desc">{{ dish.description }}</div>

    <!-- 食材组成 -->
    <div class="dish-ingredients">
      <div
        class="ingredient-section"
        v-if="dish.requiredIngredients && dish.requiredIngredients.length > 0"
      >
        <span class="ingredient-title">必选食材:</span>
        <div class="ingredient-list">
          <span
            class="ingredient-item"
            v-for="ingredient in dish.requiredIngredients"
            :key="ingredient"
          >
            {{ ingredient }}
          </span>
        </div>
      </div>

      <div
        class="ingredient-section"
        v-if="dish.optionalIngredients && dish.optionalIngredients.length > 0"
      >
        <span class="ingredient-title">可选食材:</span>
        <div class="ingredient-list">
          <el-checkbox
            v-for="ingredient in dish.optionalIngredients"
            :key="ingredient.id || ingredient.name"
            v-model="ingredient.selected"
            class="ingredient-checkbox"
          >
            {{ ingredient.name }}
            <span class="ingredient-price" v-if="ingredient.price">
              (+¥{{ ingredient.price.toFixed(2) }})
            </span>
          </el-checkbox>
        </div>
      </div>
    </div>

    <!-- 数量选择 -->
    <div class="dish-quantity">
      <el-input-number v-model="dish.quantity" :min="1" :max="10" label="数量" style="width: 100%" />
    </div>

    <el-button type="primary" size="small" @click="handleAddToCart" style="width: 100%">
      {{ viewMode === 'order' ? '立即购买' : '加入购物车' }}
    </el-button>
  </div>
</template>

<script setup>
import { ElInputNumber, ElCheckbox, ElButton } from 'element-plus'

const props = defineProps({
  dish: {
    type: Object,
    required: true
  },
  categoryEmoji: {
    type: String,
    default: '🍽️'
  },
  viewMode: {
    type: String,
    default: 'order'
  }
})

const emit = defineEmits(['add-to-cart'])

// 计算实时价格函数
const calculateRealTimePrice = (item) => {
  if (!item) {
    return 0
  }
  const optionalTotal = item.optionalIngredients.reduce((sum, ingredient) => {
    return sum + (ingredient.selected ? ingredient.price : 0)
  }, 0)
  return item.price + optionalTotal
}

const handleAddToCart = () => {
  emit('add-to-cart', props.dish)
}
</script>

<style scoped lang="less">
.dish-card {
  border: 1px solid rgba(59, 130, 246, 0.1);
  border-radius: 16px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  background: linear-gradient(to bottom, #ffffff 0%, #f8fafc 100%);
  box-shadow: 0 2px 12px rgba(59, 130, 246, 0.08);
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(90deg, #3b82f6 0%, #06b6d4 100%);
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  &:hover {
    box-shadow: 0 12px 40px rgba(59, 130, 246, 0.18);
    transform: translateY(-6px);
    border-color: rgba(59, 130, 246, 0.2);

    &::before {
      opacity: 1;
    }
  }

  .dish-image {
    font-size: 64px;
    margin-bottom: 16px;
    text-align: center;
    filter: drop-shadow(0 4px 8px rgba(59, 130, 246, 0.15));
  }

  .dish-name {
    font-size: 18px;
    font-weight: 700;
    color: #1e293b;
    text-align: center;
    line-height: 1.4;
    margin-bottom: 8px;
  }

  .dish-price {
    font-size: 24px;
    color: #f59e0b;
    font-weight: 700;
    text-align: center;
    background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    margin-bottom: 12px;
  }

  .dish-desc {
    font-size: 13px;
    color: #64748b;
    text-align: center;
    margin-bottom: 16px;
    line-height: 1.6;
    padding: 0 8px;
  }

  // 食材组成
  .dish-ingredients {
    width: 100%;
    margin: 12px 0;
    padding: 16px;
    background: rgba(59, 130, 246, 0.03);
    border-radius: 12px;
    border: 1px solid rgba(59, 130, 246, 0.08);

    .ingredient-section {
      margin-bottom: 16px;

      &:last-child {
        margin-bottom: 0;
      }

      .ingredient-title {
        display: block;
        font-weight: 600;
        color: #334155;
        margin-bottom: 8px;
        font-size: 13px;
      }

      .ingredient-list {
        display: flex;
        flex-direction: column;
        gap: 8px;

        .ingredient-item {
          background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
          color: #ffffff;
          padding: 6px 12px;
          border-radius: 6px;
          font-size: 12px;
          display: inline-block;
          font-weight: 500;
          box-shadow: 0 2px 4px rgba(59, 130, 246, 0.2);
        }

        .ingredient-checkbox {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 8px;
          background: #ffffff;
          border-radius: 8px;
          border: 1px solid rgba(59, 130, 246, 0.1);
          transition: all 0.3s ease;

          &:hover {
            background: rgba(59, 130, 246, 0.05);
            border-color: rgba(59, 130, 246, 0.2);
          }

          .ingredient-price {
            color: #f59e0b;
            font-size: 12px;
            font-weight: 600;
          }
        }
      }
    }
  }

  // 数量选择
  .dish-quantity {
    width: 100%;
    margin: 12px 0;

    :deep(.el-input-number) {
      width: 100%;

      .el-input-number__decrease,
      .el-input-number__increase {
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
        border-color: transparent;
        color: #ffffff;

        &:hover {
          background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
        }
      }
    }
  }

  .el-button {
    width: 100%;
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    border: none;
    border-radius: 12px;
    height: 44px;
    font-size: 15px;
    font-weight: 600;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);

    &:hover {
      background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
    }

    &:active {
      transform: translateY(0);
    }
  }
}
</style>
