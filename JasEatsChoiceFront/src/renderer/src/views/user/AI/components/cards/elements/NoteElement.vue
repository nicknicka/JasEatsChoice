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
  border: 1px solid transparent;
  transition: background 0.2s ease;
}

.note-card--info {
  background: #f0f4ff;
  border-color: rgba(102, 126, 234, 0.1);
}

.note-card--warning {
  background: #fffbf0;
  border-color: rgba(255, 217, 61, 0.2);
}

.note-card--success {
  background: #f0faf4;
  border-color: rgba(86, 212, 143, 0.15);
}

.note-card--error {
  background: #fff5f5;
  border-color: rgba(255, 107, 107, 0.15);
}

.note-icon {
  font-size: 16px;
  flex-shrink: 0;
  margin-top: 1px;
}

.note-content {
  font-size: 13px;
  color: #4a5568;
  line-height: 1.5;
}
</style>
