<template>
  <div v-if="parsedCard" class="merchant-ai-card">
    <UniCard :data="parsedCard" />
  </div>
  <div v-else class="merchant-ai-card-fallback">
    <span class="fallback-icon">📄</span>
    <span class="fallback-text">{{ safeFallbackText }}</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import UniCard from '@/views/user/AI/components/cards/UniCard.vue'
import { OrderStatusMap } from '@/views/user/AI/constants/messageTypes'
import { createMerchantAICardViewModel } from '../composables/merchantAICardDisplayService'

const props = defineProps({
  data: {
    type: [Object, Array, String],
    default: null
  },
  fallbackText: {
    type: String,
    default: ''
  }
})

// 订单列表组件只认英文状态键，而 AI 卡片给的是中文状态文案；
// 按 OrderStatusMap（订单状态文案的唯一来源）反推语义键，让状态色回到 §3.1 的状态表。
const STATUS_KEY_BY_COLOR = {
  warning: 'pending',
  primary: 'confirmed',
  success: 'completed',
  danger: 'cancelled',
  info: 'refunded'
}
const STATUS_KEY_BY_TEXT = Object.fromEntries(
  Object.values(OrderStatusMap).map((item) => [item.text, STATUS_KEY_BY_COLOR[item.color] || 'pending'])
)

const withOrderStatusKey = (card) => {
  if (!Array.isArray(card.elements)) {
    return card
  }

  return {
    ...card,
    elements: card.elements.map((element) => {
      if (element?.tag !== 'order_list' || !Array.isArray(element.orders)) {
        return element
      }

      return {
        ...element,
        orders: element.orders.map((order) => {
          const statusKey = order && !order.status ? STATUS_KEY_BY_TEXT[order.statusText] : ''
          return statusKey ? { ...order, status: statusKey } : order
        })
      }
    })
  }
}

const cardViewModel = computed(() => createMerchantAICardViewModel(props.data, props.fallbackText))
const parsedCard = computed(() => {
  const card = cardViewModel.value.kind === 'card' ? cardViewModel.value.card : null
  return card ? withOrderStatusKey(card) : null
})
const safeFallbackText = computed(() => (cardViewModel.value.kind === 'text' ? cardViewModel.value.text : '卡片内容暂不可用'))
</script>

<style scoped lang="less">
@import '../../../../assets/css/nordic-theme.less';
@import '../../../../assets/css/merchant-theme.less';

.merchant-ai-card {
  width: 100%;

  // §3.1：序号属中性信息，红色只留给「已取消」等异常状态
  :deep(.order-number) {
    background: var(--neutral-bg);
    color: var(--neutral);
  }

  // §5.2：金额不染色
  :deep(.value.price) {
    color: var(--ink-900);
  }
}

.merchant-ai-card-fallback {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 14px;
  border-radius: @nordic-radius-md;
  border: 1px dashed @merchant-border;
  background: @merchant-surface-alt;
  color: @merchant-text-sec;
  font-size: @nordic-text-xs;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.fallback-icon {
  flex-shrink: 0;
}

.fallback-text {
  min-width: 0;
}
</style>
