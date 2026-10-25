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
  padding: 14px 12px;
  background: #fafbfc;
  border-radius: 12px;
  border-left: 3px solid #667eea;
  transition: all 0.2s ease;
}

/* 使用 nth-child 为不同位置的卡片设置不同的左边框颜色 */
.stat-card:nth-child(4n+1) {
  border-left-color: #667eea;
}

.stat-card:nth-child(4n+2) {
  border-left-color: #ff6b6b;
}

.stat-card:nth-child(4n+3) {
  border-left-color: #56d48f;
}

.stat-card:nth-child(4n+4) {
  border-left-color: #ffd93d;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(31, 41, 55, 0.08);
}

.stat-icon {
  font-size: 22px;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.stat-label {
  font-size: 12px;
  color: #94a3a8;
}
</style>
