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
  return ingredients
    .map((ing) => (typeof ing === 'object' ? ing.name : ing))
    .join('、')
}
</script>

<style scoped lang="less">
.order-item-list {
  display: flex;
  flex-direction: column;
  gap: 12px;

  .order-item {
    padding: 16px;
    background: #fafafa;
    border-radius: 8px;
    border: 1px solid #f0f0f0;
    transition: all 0.3s;

    &:hover {
      background: #f5f7fa;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    }

    .item-main {
      margin-bottom: 12px;

      .item-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 10px;

        .item-name {
          font-size: 15px;
          font-weight: 600;
          color: #2c3e50;
        }

        .item-price {
          font-size: 14px;
          color: #7f8c8d;
        }
      }

      .item-specs {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        margin-bottom: 8px;

        .ingredient-tag {
          font-size: 12px;
        }
      }

      .item-note {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 8px 12px;
        background: #fff9e6;
        border-radius: 4px;
        font-size: 13px;
        color: #856404;
      }
    }

    .item-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 12px;
      border-top: 1px dashed #e4e7ed;

      .item-quantity {
        font-size: 14px;
        color: #7f8c8d;
      }

      .item-total {
        font-size: 18px;
        font-weight: 700;
        color: #e6a23c;
      }
    }

    .payment-info {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 12px;
      margin-top: 12px;
      border-top: 1px dashed #e4e7ed;

      .payment-detail {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 13px;
        color: #67c23a;
        font-weight: 500;
      }
    }
  }
}
</style>
