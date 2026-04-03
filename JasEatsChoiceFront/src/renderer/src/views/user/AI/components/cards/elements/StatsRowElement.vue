<template>
  <div class="stats-row-element">
    <div
      v-for="(item, index) in items"
      :key="index"
      class="stat-card"
    >
      <span v-if="item.icon" class="stat-icon">{{ item.icon }}</span>
      <span class="stat-value">{{ item.value }}</span>
      <span class="stat-label">{{ item.label }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  element: {
    type: Object,
    required: true
  }
})

// 统计项列表
const items = computed(() => props.element.items || [])
</script>

<style scoped>
.stats-row-element {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  width: 100%;
}

.stat-card {
  flex: 1;
  min-width: 100px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 16px 12px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: background 0.3s;
}

.stat-card:hover {
  background: #f0f1f3;
}

.stat-icon {
  font-size: 24px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.stat-label {
  font-size: 12px;
  color: #999;
}
</style>
