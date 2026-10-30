<template>
  <div v-if="parsedCard" class="merchant-ai-card">
    <UniCard :data="sanitizedCard" />
  </div>
  <div v-else class="merchant-ai-card-fallback">
    <span class="fallback-icon">📄</span>
    <span class="fallback-text">{{ fallbackText || '卡片内容暂不可用' }}</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import UniCard from '@/views/user/AI/components/cards/UniCard.vue'
import { useUniCardParser } from '@/views/user/AI/composables/useUniCardParser'

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

const { parseCardData } = useUniCardParser()

const parseResult = computed(() => parseCardData(props.data))
const parsedCard = computed(() => (parseResult.value.parsed ? parseResult.value.card : null))

const sanitizeCardData = (value) => {
  if (Array.isArray(value)) {
    return value.map(sanitizeCardData)
  }

  if (!value || typeof value !== 'object') {
    return value
  }

  return Object.fromEntries(
    Object.entries(value)
      .filter(([key]) => key !== 'actions')
      .map(([key, item]) => [key, sanitizeCardData(item)])
  )
}

const sanitizedCard = computed(() => sanitizeCardData(parsedCard.value))
</script>

<style scoped lang="less">
@import '../../../../assets/css/nordic-theme.less';
@import '../../../../assets/css/merchant-theme.less';

.merchant-ai-card {
  width: 100%;
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
