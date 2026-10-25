<template>
  <component
    :is="elementComponent"
    :element="element"
    @action="onAction"
  />
  <!-- 未知 tag 降级显示 -->
  <div v-if="!elementComponent" class="unknown-element">
    <span class="unknown-tag">{{ element.tag }}</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import DishListElement from './DishListElement.vue'
import OrderListElement from './OrderListElement.vue'
import HealthStatsElement from './HealthStatsElement.vue'
import MarkdownElement from './MarkdownElement.vue'
import NoteElement from './NoteElement.vue'
import StatsRowElement from './StatsRowElement.vue'
import DividerElement from './DividerElement.vue'

const ELEMENT_MAP = {
  dish_list: DishListElement,
  order_list: OrderListElement,
  health_stats: HealthStatsElement,
  markdown: MarkdownElement,
  note: NoteElement,
  stats_row: StatsRowElement,
  divider: DividerElement
}

const props = defineProps({
  element: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['action'])

// 根据元素 tag 动态选择子组件
const elementComponent = computed(() => {
  return ELEMENT_MAP[props.element.tag] || null
})

// 事件转发
const onAction = (payload) => {
  emit('action', payload)
}
</script>

<style scoped>
.unknown-element {
  padding: 12px;
  background: #fef2f2;
  border-radius: 8px;
  border: 1px dashed #fca5a5;
  text-align: center;
}

.unknown-tag {
  font-size: 13px;
  color: #dc2626;
  font-family: monospace;
}
</style>
