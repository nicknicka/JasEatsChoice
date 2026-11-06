<template>
  <div class="order-item-list">
    <div class="order-item" v-for="item in items" :key="item.id">
      <div class="item-main">
        <div class="item-header">
          <span class="item-name">{{ item.name }}</span>
          <span class="item-price">¥{{ item.price.toFixed(2) }}</span>
        </div>

        <div class="item-specs" v-if="hasIngredients(item)">
          <el-tag
            v-if="item.requiredIngredients && item.requiredIngredients.length > 0"
            size="small"
            type="info"
            effect="plain"
            class="ingredient-tag"
          >
            必选: {{ item.requiredIngredients.join('、') }}
          </el-tag>
          <el-tag
            v-if="item.selectedOptionalIngredients && item.selectedOptionalIngredients.length > 0"
            size="small"
            type="success"
            effect="plain"
            class="ingredient-tag"
          >
            加选: {{ formatOptionalIngredients(item.selectedOptionalIngredients) }}
          </el-tag>
        </div>

        <div class="item-note" v-if="item.note">
          <el-icon><Document /></el-icon>
          <span>{{ item.note }}</span>
        </div>
      </div>

      <div class="item-footer">
        <div class="item-quantity">× {{ item.quantity }}</div>
        <div class="item-total">
          ¥{{ (item.totalPrice || item.price * item.quantity).toFixed(2) }}
        </div>
      </div>

      <!-- 支付信息（仅已支付订单显示） -->
      <div class="payment-info" v-if="showPaymentInfo">
        <div class="payment-detail">
          <el-icon><User /></el-icon>
          <span>{{ item.payee || '未知' }}</span>
        </div>
        <el-tag size="small" type="success" effect="plain">已支付</el-tag>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="items.length === 0" description="暂无商品" :image-size="80"></el-empty>
  </div>
</template>

<script setup>
import { Document, User } from '@element-plus/icons-vue'

defineProps({
  items: {
    type: Array,
    default: () => []
  },
  showPaymentInfo: {
    type: Boolean,
    default: false
  }
})

// 判断是否有食材信息
const hasIngredients = (item) => {
  return (
    (item.requiredIngredients && item.requiredIngredients.length > 0) ||
    (item.selectedOptionalIngredients && item.selectedOptionalIngredients.length > 0)
  )
}

// 格式化可选食材
const formatOptionalIngredients = (ingredients) => {
  return ingredients.map((ing) => (typeof ing === 'object' ? ing.name : ing)).join('、')
}
</script>

<style scoped lang="less">
.order-item-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);

  .order-item {
    padding: var(--space-4);
    background: var(--canvas);
    border: 1px solid var(--line);
    border-radius: var(--radius-md);

    .item-main {
      margin-bottom: var(--space-3);

      .item-header {
        display: flex;
        justify-content: space-between;
        align-items: baseline;
        gap: var(--space-4);
        margin-bottom: var(--space-2);

        .item-name {
          font-size: var(--text-body);
          font-weight: var(--weight-semibold);
          color: var(--ink-900);
        }

        .item-price {
          font-family: var(--font-num);
          font-variant-numeric: tabular-nums;
          font-size: var(--text-body-sm);
          color: var(--ink-500);
          white-space: nowrap;
        }
      }

      .item-specs {
        display: flex;
        flex-wrap: wrap;
        gap: var(--space-1);
        margin-bottom: var(--space-2);

        .ingredient-tag {
          font-size: var(--text-caption);
        }
      }

      .item-note {
        display: flex;
        align-items: center;
        gap: var(--space-1);
        padding: var(--space-2) var(--space-3);
        background: var(--warning-bg);
        border-radius: var(--radius-sm);
        font-size: var(--text-body-sm);
        color: var(--ink-700);
      }
    }

    .item-footer {
      display: flex;
      justify-content: space-between;
      align-items: baseline;
      gap: var(--space-4);
      padding-top: var(--space-3);
      border-top: 1px dashed var(--line);

      .item-quantity {
        font-size: var(--text-body-sm);
        color: var(--ink-500);
      }

      .item-total {
        font-family: var(--font-num);
        font-variant-numeric: tabular-nums;
        font-size: var(--text-h3);
        font-weight: var(--weight-bold);
        color: var(--ink-900);
        white-space: nowrap;
      }
    }

    .payment-info {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: var(--space-3);
      margin-top: var(--space-3);
      border-top: 1px dashed var(--line);

      .payment-detail {
        display: flex;
        align-items: center;
        gap: var(--space-1);
        font-size: var(--text-body-sm);
        color: var(--success);
        font-weight: var(--weight-medium);
      }
    }
  }
}
</style>
