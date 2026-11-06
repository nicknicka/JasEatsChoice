<template>
  <div class="dish-card">
    <div class="dish-image">
      <img
        v-if="dish.image"
        :src="dish.image"
        :alt="dish.name"
        loading="lazy"
        class="dish-img"
      />
      <el-icon v-else class="dish-placeholder-icon" :size="32">
        <component :is="categoryIcon" />
      </el-icon>
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
import { computed } from 'vue'
import { ElInputNumber, ElButton, ElTag } from 'element-plus'
import { ShoppingCart } from '@element-plus/icons-vue'

const props = defineProps({
  dish: {
    type: Object,
    required: true
  },
  categoryIcon: {
    type: [Object, Function],
    default: null
  }
})

const emit = defineEmits(['add-to-cart'])

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
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--surface);
  box-shadow: var(--shadow-sm);
  transition: box-shadow var(--duration-base) var(--ease-out), transform var(--duration-base) var(--ease-out);
  overflow: hidden;
  display: flex;
  flex-direction: column;

  &:hover {
    box-shadow: var(--shadow-md);
    transform: translateY(-2px);
    border-color: var(--line-strong);
  }

  .dish-image {
    width: 100%;
    height: 120px;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    background: var(--canvas-sunken);
    flex-shrink: 0;

    .dish-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform var(--duration-base) var(--ease-out);
    }

    .dish-placeholder-icon {
      color: var(--ink-400);
    }
  }

  &:hover {
    .dish-image {
      .dish-img {
        transform: scale(1.04);
      }
    }
  }

  .dish-info {
    padding: var(--space-4);
    flex: 1;
    display: flex;
    flex-direction: column;
  }

  .dish-name {
    font-size: var(--text-h3);
    font-weight: var(--weight-semibold);
    color: var(--ink-900);
    line-height: 1.3;
    margin-bottom: var(--space-2);
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
    gap: var(--space-3);
    margin-bottom: var(--space-3);
  }

  .dish-price {
    font-family: var(--font-num);
    font-variant-numeric: tabular-nums;
    font-size: var(--text-h2);
    color: var(--ink-900);
    font-weight: var(--weight-semibold);
    flex: 1;
  }

  .quantity-input {
    width: 100px;
    flex-shrink: 0;

    :deep(.el-input-number) {
      width: 100%;

      .el-input-number__decrease,
      .el-input-number__increase {
        background: var(--brand-500);
        border-color: transparent;
        color: var(--surface);
      }
    }
  }

  .dish-desc {
    font-size: var(--text-body-sm);
    color: var(--ink-500);
    margin-bottom: var(--space-3);
    line-height: 1.5;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .dish-ingredients {
    margin-bottom: var(--space-3);

    .ingredient-section {
      margin-bottom: var(--space-2);

      .ingredient-label {
        display: flex;
        align-items: center;
        margin-bottom: 6px;
        font-size: var(--text-body-sm);
        color: var(--ink-500);

        .label-icon {
          font-size: var(--text-body-sm);
          margin-right: var(--space-1);
        }

        .label-text {
          font-weight: var(--weight-medium);
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
      border-top: 1px dashed var(--line);

      .optional-label {
        display: flex;
        align-items: center;
        margin-bottom: 6px;
        font-size: var(--text-body-sm);
        color: var(--ink-500);

        .label-icon {
          font-size: var(--text-body-sm);
          margin-right: var(--space-1);
        }

        .label-text {
          font-weight: var(--weight-medium);
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
      transition: all var(--duration-fast) var(--ease-out);
      user-select: none;
      border-style: solid;

      &:hover {
        transform: translateY(-1px);
        box-shadow: var(--shadow-sm);
      }

      &.is-selected {
        font-weight: var(--weight-semibold);
        border-width: 1.5px;
      }

      .ingredient-price {
        font-size: var(--text-caption);
        margin-left: 2px;
      }
    }
  }

  .add-cart-btn {
    width: 100%;
    background: var(--brand-500);
    border: none;
    border-radius: var(--radius-md);
    height: 40px;
    font-size: var(--text-body);
    font-weight: var(--weight-semibold);
    transition: background var(--duration-fast) var(--ease-out), transform var(--duration-fast) var(--ease-out);
    margin-top: auto;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;

    &:hover {
      background: var(--brand-600);
      transform: translateY(-1px);
    }

    &:active {
      transform: translateY(0);
    }
  }
}
</style>
