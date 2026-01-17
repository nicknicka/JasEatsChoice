<template>
  <el-card class="order-card">
    <div class="order-header">
      <div class="order-info">
        <div class="order-no">订单号: {{ order.orderNo }}</div>
        <div class="order-merchant">商家: {{ order.merchant }}</div>
        <div class="order-time">时间: {{ order.time }}</div>
      </div>
      <div class="order-status">
        <el-tag :type="getTagType(order.status)">
          {{ getStatusText(order.status) }}
        </el-tag>
      </div>
    </div>

    <!-- 菜品列表 -->
    <OrderItemsList
      :items="order.items"
      :item-count="order.itemCount"
      :dish-count="order.items.length"
      :max-display="maxDisplay"
      @item-click="handleItemClick"
      @image-error="handleImageError"
    />

    <div class="order-total">
      <div class="total-text">总金额:</div>
      <div class="total-amount">¥{{ formatAmount(order.total) }}</div>
    </div>

    <div class="order-actions">
      <el-button type="primary" size="small" @click="handleViewDetails">
        查看详情
      </el-button>
      <el-button
        v-if="canCancel"
        type="danger"
        size="small"
        @click="handleCancel"
      >
        取消订单
      </el-button>
      <el-button
        v-if="canConfirmReceipt"
        type="success"
        size="small"
        @click="handleConfirmReceipt"
      >
        确认收货
      </el-button>
      <el-button
        v-if="canEvaluate"
        type="success"
        size="small"
        @click="handleEvaluate"
      >
        去评价
      </el-button>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import OrderItemsList from './OrderItemsList.vue'
import {
  getOrderStatusText,
  getOrderStatusTagType,
  canCancelOrder,
  canConfirmReceipt,
  canEvaluateOrder
} from '../../../utils/orderStatus'
import { formatAmount } from '../../../utils/formatters'

/**
 * 订单卡片组件
 */
const props = defineProps({
  order: {
    type: Object,
    required: true
  },
  maxDisplay: {
    type: Number,
    default: 3
  }
})

const emit = defineEmits(['view-details', 'cancel', 'confirm-receipt', 'evaluate', 'image-error'])

/**
 * 获取状态文本
 */
function getStatusText(status) {
  return getOrderStatusText(status)
}

/**
 * 获取标签类型
 */
function getTagType(status) {
  return getOrderStatusTagType(status)
}

/**
 * 是否可以取消订单
 */
const canCancel = computed(() => canCancelOrder(props.order.status))

/**
 * 是否可以确认收货
 */
const canConfirmReceipt = computed(() => canConfirmReceipt(props.order.status))

/**
 * 是否可以评价
 */
const canEvaluate = computed(() => canEvaluateOrder(props.order.status))

/**
 * 查看详情
 */
function handleViewDetails() {
  emit('view-details', props.order)
}

/**
 * 取消订单
 */
function handleCancel() {
  emit('cancel', props.order)
}

/**
 * 确认收货
 */
function handleConfirmReceipt() {
  emit('confirm-receipt', props.order)
}

/**
 * 去评价
 */
function handleEvaluate() {
  emit('evaluate', props.order)
}

/**
 * 菜品点击
 */
function handleItemClick(item) {
  emit('view-details', props.order)
}

/**
 * 图片加载错误
 */
function handleImageError(item) {
  emit('image-error', item)
}
</script>

<style scoped lang="less">
.order-card {
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  position: relative;

  // 添加悬浮时的上移效果
  &:hover {
    transform: translateY(-4px) scale(1.01);
    box-shadow: 0 12px 32px rgba(92, 142, 255, 0.18);
    border-color: rgba(92, 142, 255, 0.4);

    // 为卡片添加微妙的渐变边框效果
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      border-radius: 16px;
      padding: 2px;
      background: linear-gradient(
        135deg,
        rgba(92, 142, 255, 0.2) 0%,
        rgba(138, 180, 248, 0.15) 50%,
        rgba(92, 142, 255, 0.2) 100%
      );
      -webkit-mask:
        linear-gradient(#fff 0 0) content-box,
        linear-gradient(#fff 0 0);
      -webkit-mask-composite: xor;
      mask:
        linear-gradient(#fff 0 0) content-box,
        linear-gradient(#fff 0 0);
      mask-composite: exclude;
      pointer-events: none;
    }
  }

  // 点击时的反馈
  &:active {
    transform: translateY(-2px) scale(1.005);
    box-shadow: 0 8px 24px rgba(92, 142, 255, 0.15);
  }

  :deep(.el-card__body) {
    padding: 20px;
  }

  .order-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 16px;
    padding-bottom: 16px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.06);

    .order-info {
      .order-no {
        font-weight: 600;
        margin-bottom: 6px;
        color: #2c5282;
        font-size: 15px;
      }
      .order-merchant,
      .order-time {
        font-size: 13px;
        color: #64748b;
        margin-bottom: 4px;
      }
    }

    .order-status {
      :deep(.el-tag) {
        border-radius: 20px;
        padding: 6px 14px;
        font-weight: 500;
        border: none;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      }
    }
  }

  .order-total {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    margin-bottom: 16px;
    margin-top: 16px;
    padding-top: 4px;

    .total-text {
      margin-right: 12px;
      color: #64748b;
      font-size: 14px;
      font-weight: 500;
    }

    .total-amount {
      font-size: 20px;
      font-weight: 700;
      color: #ff6b6b;
      text-shadow: 0 1px 2px rgba(255, 107, 107, 0.1);
    }
  }

  .order-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;

    :deep(.el-button) {
      border-radius: 20px;
      padding: 8px 18px;
      font-weight: 500;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);

      &:hover {
        transform: translateY(-1px);
      }

      &.el-button--primary {
        background: linear-gradient(135deg, #6ba4ff 0%, #5c8eff 100%);
        border-color: transparent;
        box-shadow: 0 3px 10px rgba(92, 142, 255, 0.3);

        &:hover {
          box-shadow: 0 4px 14px rgba(92, 142, 255, 0.4);
        }
      }

      &.el-button--danger {
        background: linear-gradient(135deg, #ff8a80 0%, #ff6b6b 100%);
        border-color: transparent;
        box-shadow: 0 3px 10px rgba(255, 107, 107, 0.3);

        &:hover {
          box-shadow: 0 4px 14px rgba(255, 107, 107, 0.4);
        }
      }

      &.el-button--success {
        background: linear-gradient(135deg, #81c784 0%, #66bb6a 100%);
        border-color: transparent;
        box-shadow: 0 3px 10px rgba(102, 187, 106, 0.3);

        &:hover {
          box-shadow: 0 4px 14px rgba(102, 187, 106, 0.4);
        }
      }
    }
  }
}

/* 订单卡片动画优化 */
.order-card {
  /* 为每个卡片添加入场动画 */
  animation: card-slide-in 0.5s cubic-bezier(0.4, 0, 0.2, 1) backwards;

  /* 为每个卡片添加递增的延迟，创建交错效果 */
  &:nth-child(1) {
    animation-delay: 0s;
  }
  &:nth-child(2) {
    animation-delay: 0.08s;
  }
  &:nth-child(3) {
    animation-delay: 0.16s;
  }
  &:nth-child(4) {
    animation-delay: 0.24s;
  }
  &:nth-child(5) {
    animation-delay: 0.32s;
  }
  &:nth-child(n + 6) {
    animation-delay: 0.4s;
  }
}

/* 为订单卡片添加入场动画 */
@keyframes card-slide-in {
  0% {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@media (max-width: 768px) {
  .order-card {
    border-radius: 14px;

    :deep(.el-card__body) {
      padding: 16px;
    }

    .order-header {
      flex-direction: column;
      align-items: flex-start !important;
      margin-bottom: 12px;
      padding-bottom: 12px;

      .order-status {
        margin-top: 10px;
      }
    }

    .order-total {
      margin-bottom: 12px;

      .total-text {
        font-size: 13px;
      }

      .total-amount {
        font-size: 18px;
      }
    }

    .order-actions {
      flex-direction: column;
      gap: 8px;

      :deep(.el-button) {
        width: 100%;
        padding: 10px;
      }
    }
  }
}
</style>
