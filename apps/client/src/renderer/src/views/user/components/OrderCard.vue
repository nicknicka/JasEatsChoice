<template>
  <el-card
    class="order-card"
    shadow="never"
    @click="handleViewDetails"
  >
    <!-- ① 商家 -->
    <div class="order-merchant">
      <el-icon class="order-merchant__icon" aria-hidden="true"><Shop /></el-icon>
      <span class="order-merchant__text">商家: {{ order.merchant }}</span>
    </div>

    <!-- ② 商品摘要 -->
    <OrderItemsList
      :items="order.items"
      :item-count="order.itemCount"
      :dish-count="order.items.length"
      :max-display="maxDisplay"
      @item-click="handleItemClick"
      @image-error="handleImageError"
    />

    <!-- ③ 订单号 → ④ 状态 → ⑤ 金额 → ⑥ 主操作（DESIGN-BASELINE §4.2 卡片字段骨架） -->
    <div class="order-footer">
      <div class="order-meta">
        <span class="order-no">订单号: {{ order.orderNo }}</span>
        <span class="order-time">时间: {{ order.time }}</span>
        <span class="order-status" :class="`order-status--${statusTone}`">
          <el-tag effect="light" round>{{ statusText }}</el-tag>
        </span>
      </div>

      <div class="order-total">
        <span class="total-text">总金额:</span>
        <span class="total-amount">¥{{ formatAmount(order.total) }}</span>
      </div>

      <div class="order-actions">
        <el-button
          type="primary"
          size="small"
          class="order-actions__primary"
          @click.stop="primaryAction.onClick"
        >
          {{ primaryAction.text }}
        </el-button>
        <el-button
          v-for="action in secondaryActions"
          :key="action.key"
          size="small"
          :class="[
            'order-actions__secondary',
            { 'order-actions__secondary--danger': action.danger }
          ]"
          @click.stop="action.onClick"
        >
          {{ action.text }}
        </el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { Shop } from '@element-plus/icons-vue'
import OrderItemsList from './OrderItemsList.vue'
import {
  getOrderStatusText,
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

const emit = defineEmits(['view-details', 'pay', 'cancel', 'confirm-receipt', 'evaluate', 'additional-review', 'reorder', 'image-error'])

/**
 * 订单状态 → 语义色档位（DESIGN-BASELINE §3.1 状态表）
 */
const STATUS_TONE_MAP = {
  pending: 'warning',
  pendingAccept: 'warning',
  preparing: 'info',
  completed: 'success',
  cancelled: 'danger'
}

/**
 * 获取状态文本
 */
const statusText = computed(() => getOrderStatusText(props.order.status))

/**
 * 获取状态语义色档位
 */
const statusTone = computed(() => STATUS_TONE_MAP[props.order.status] || 'neutral')

/**
 * 是否可以取消订单
 */
const canCancel = computed(() => canCancelOrder(props.order.status))

/**
 * 是否可以确认收货
 */
const canConfirmReceiptOrder = computed(() => canConfirmReceipt(props.order.status))

/**
 * 是否可以评价
 */
const canEvaluate = computed(() => canEvaluateOrder(props.order.status))

/**
 * 是否可以追加评价（已评价订单可以追加评价）
 */
const canAdditionalReview = computed(() => {
  // 只有已评价的订单可以追加评价（状态码8对应reviewed）
  return props.order.status === 'reviewed'
})

/**
 * 是否可以再来一单
 */
const canReorder = computed(() => {
  // 已评价/待评价/已取消的订单可以再来一单
  return props.order.status === 'reviewed' ||
         props.order.status === 'pendingComment' ||
         props.order.status === 'cancelled'
})

/**
 * 主操作：待支付订单是继续支付，其余是查看详情；
 * 每张卡只有一个实心主按钮，其余动作走次级样式（DESIGN-BASELINE §4.1）
 */
const primaryAction = computed(() => {
  if (props.order.status === 'pending') {
    return { key: 'pay', text: '支付', onClick: handlePay }
  }
  return { key: 'view', text: '查看详情', onClick: handleViewDetails }
})

/**
 * 次操作：按状态给出的可执行动作，危险动作用危险色文字按钮
 */
const secondaryActions = computed(() => {
  const actions = []

  if (canCancel.value) {
    actions.push({ key: 'cancel', text: '取消订单', danger: true, onClick: handleCancel })
  }
  if (canConfirmReceiptOrder.value) {
    actions.push({ key: 'confirm', text: '确认收货', onClick: handleConfirmReceipt })
  }
  if (canEvaluate.value) {
    actions.push({ key: 'evaluate', text: '去评价', onClick: handleEvaluate })
  }
  if (canAdditionalReview.value) {
    actions.push({ key: 'additional', text: '追加评价', onClick: handleAdditionalReview })
  }
  if (canReorder.value) {
    actions.push({ key: 'reorder', text: '再来一单', onClick: handleReorder })
  }

  return actions
})

/**
 * 查看详情
 */
function handleViewDetails() {
  emit('view-details', props.order)
}

/**
 * 支付订单
 */
function handlePay() {
  emit('pay', props.order)
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
 * 追加评价
 */
function handleAdditionalReview() {
  emit('additional-review', props.order)
}

/**
 * 再来一单
 */
function handleReorder() {
  emit('reorder', props.order)
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
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition:
    border-color var(--duration-fast) var(--ease-out),
    box-shadow var(--duration-fast) var(--ease-out),
    transform var(--duration-fast) var(--ease-out);

  &:hover {
    border-color: var(--line-strong);
    box-shadow: var(--shadow-md);
    transform: translateY(-1px);
  }

  &:focus-visible {
    outline: 2px solid var(--brand-500);
    outline-offset: 2px;
  }

  :deep(.el-card__body) {
    padding: var(--card-padding);
  }

  .order-merchant {
    display: flex;
    align-items: center;
    gap: var(--space-2);
    min-width: 0;

    .order-merchant__icon {
      color: var(--ink-500);
      font-size: var(--text-h3);
    }

    .order-merchant__text {
      min-width: 0;
      overflow: hidden;
      color: var(--ink-900);
      font-size: var(--text-h3);
      font-weight: var(--weight-semibold);
      line-height: 24px;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .order-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: var(--space-2) var(--space-4);
    margin-top: var(--space-4);
    padding-top: var(--space-4);
    border-top: 1px solid var(--line);
  }

  .order-meta {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: var(--space-1) var(--space-3);
    min-width: 0;

    .order-no,
    .order-time {
      color: var(--ink-500);
      font-size: var(--text-body-sm);
      line-height: 20px;
      white-space: nowrap;
    }

    .order-no {
      font-family: var(--font-num);
      font-variant-numeric: tabular-nums;
      color: var(--ink-700);
    }
  }

  .order-status {
    display: inline-flex;

    :deep(.el-tag) {
      height: auto;
      border: none;
      border-radius: var(--radius-full);
      padding: 2px var(--space-3);
      font-size: var(--text-body-sm);
      font-weight: var(--weight-medium);
      line-height: 18px;
    }
  }

  .order-status--warning :deep(.el-tag) {
    background-color: var(--warning-bg);
    color: var(--warning);
  }

  .order-status--info :deep(.el-tag) {
    background-color: var(--info-bg);
    color: var(--info);
  }

  .order-status--success :deep(.el-tag) {
    background-color: var(--success-bg);
    color: var(--success);
  }

  .order-status--danger :deep(.el-tag) {
    background-color: var(--danger-bg);
    color: var(--danger);
  }

  .order-status--neutral :deep(.el-tag) {
    background-color: var(--neutral-bg);
    color: var(--neutral);
  }

  .order-total {
    display: flex;
    align-items: baseline;
    gap: var(--space-2);
    margin-left: auto;

    .total-text {
      color: var(--ink-500);
      font-size: var(--text-body-sm);
    }

    .total-amount {
      color: var(--ink-900);
      font-family: var(--font-num);
      font-variant-numeric: tabular-nums;
      font-size: var(--text-h2);
      font-weight: var(--weight-semibold);
      line-height: 28px;
    }
  }

  .order-actions {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: var(--space-2);

    :deep(.el-button) {
      min-height: 32px;
      padding: 0 var(--space-4);
      border-radius: var(--radius-md);
      font-size: var(--text-body-sm);
      font-weight: var(--weight-medium);
      transition:
        background-color var(--duration-fast) var(--ease-out),
        border-color var(--duration-fast) var(--ease-out),
        color var(--duration-fast) var(--ease-out);
    }

    .order-actions__primary {
      background: var(--brand-500);
      border-color: var(--brand-500);
      color: var(--surface);

      &:hover,
      &:focus {
        background: var(--brand-600);
        border-color: var(--brand-600);
        color: var(--surface);
      }
    }

    .order-actions__secondary {
      background: var(--surface);
      border-color: var(--line-strong);
      color: var(--ink-700);

      &:hover,
      &:focus {
        background: var(--brand-50);
        border-color: var(--brand-300);
        color: var(--brand-600);
      }
    }

    .order-actions__secondary--danger {
      border-color: var(--danger-bg);
      color: var(--danger);

      &:hover,
      &:focus {
        background: var(--danger-bg);
        border-color: var(--danger);
        color: var(--danger);
      }
    }
  }
}

@media (max-width: 768px) {
  .order-card {
    :deep(.el-card__body) {
      padding: var(--card-padding-compact);
    }

    .order-footer {
      flex-direction: column;
      align-items: stretch;
    }

    .order-total {
      margin-left: 0;
      justify-content: flex-end;
    }

    .order-actions {
      justify-content: flex-end;

      :deep(.el-button) {
        flex: 1;
      }
    }
  }
}
</style>
