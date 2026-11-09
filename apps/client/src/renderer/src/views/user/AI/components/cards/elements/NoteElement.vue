<template>
  <div class="note-element">
    <div :class="['note-card', `note-card--${alertType}`]">
      <span class="note-icon">{{ alertType === 'warning' ? '⚠️' : alertType === 'success' ? '✅' : alertType === 'error' ? '❌' : '💡' }}</span>
      <span class="note-content">{{ element.content || '' }}</span>
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

// 将 element.type 映射为 el-alert 类型
const alertType = computed(() => {
  const typeMap = {
    info: 'info',
    warning: 'warning',
    success: 'success',
    error: 'error'
  }
  return typeMap[props.element.type] || 'info'
})
</script>

<style scoped>
.note-element {
  width: 100%;
}

.note-card {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 12px;
  border: 1px solid var(--line);
  transition: background 0.2s ease;
}

.note-card--info {
  background: var(--info-bg);
}

.note-card--warning {
  background: var(--warning-bg);
}

.note-card--success {
  background: var(--success-bg);
}

.note-card--error {
  background: var(--danger-bg);
}

.note-icon {
  font-size: 16px;
  flex-shrink: 0;
  margin-top: 1px;
}

.note-content {
  font-size: 13px;
  color: var(--ink-700);
  line-height: 1.5;
}
</style>
