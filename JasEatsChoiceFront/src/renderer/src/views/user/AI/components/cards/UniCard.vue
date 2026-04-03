<template>
  <div v-if="parsedCard" class="uni-card">
    <UniCardHeader v-if="parsedCard.header" :header="parsedCard.header" />
    <div class="uni-card-body">
      <UniCardElement
        v-for="(el, i) in parsedCard.elements"
        :key="i"
        :element="el"
        @action="onAction"
      />
    </div>
    <UniCardActions
      v-if="parsedCard.actions?.length"
      :actions="parsedCard.actions"
      @action="onAction"
    />
    <UniCardFooter
      v-if="parsedCard.footer"
      :footer="parsedCard.footer"
      @action="onAction"
    />
  </div>
  <!-- 降级文本显示 -->
  <div v-else class="uni-card-fallback">
    <div class="fallback-content">
      <span class="fallback-icon">📄</span>
      <span class="fallback-text">{{ fallbackText }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUniCardParser } from '../../composables/useUniCardParser'
import UniCardHeader from './UniCardHeader.vue'
import UniCardActions from './UniCardActions.vue'
import UniCardFooter from './UniCardFooter.vue'
import UniCardElement from './elements/UniCardElement.vue'

const props = defineProps({
  // 兼容旧卡片组件的 data prop 名称
  data: {
    type: Object,
    default: null
  },
  // UniCard 标准的 cardData prop 名称
  cardData: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['action'])

const { parseCardData } = useUniCardParser()

// 实际卡片数据（兼容 data 和 cardData 两种 prop 名）
const effectiveData = computed(() => props.data || props.cardData)

// 解析卡片数据
const parseResult = computed(() => parseCardData(effectiveData.value))

// 解析后的标准卡片
const parsedCard = computed(() => parseResult.value.parsed ? parseResult.value.card : null)

// 降级文本
const fallbackText = computed(() => {
  if (!props.cardData) return '无数据'
  if (typeof props.cardData === 'string') return props.cardData
  return JSON.stringify(props.cardData).slice(0, 200)
})

// 事件转发
const onAction = (payload) => {
  emit('action', payload)
}
</script>

<style scoped>
.uni-card {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: box-shadow 0.3s ease;
}

.uni-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.uni-card-body {
  padding: 16px;
  background: white;
}

.uni-card-fallback {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  background: white;
  padding: 16px;
}

.fallback-content {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #999;
  font-size: 14px;
}

.fallback-icon {
  font-size: 18px;
}

.fallback-text {
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 1.6;
}
</style>
