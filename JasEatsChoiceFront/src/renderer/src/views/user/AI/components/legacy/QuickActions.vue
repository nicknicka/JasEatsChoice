<template>
  <div class="quick-actions-wrapper">
    <div class="quick-actions-title">🚀 快速操作</div>
    <div class="quick-actions-grid">
      <div
        v-for="action in actions"
        :key="action.id"
        class="quick-action-item"
        @click="handleActionClick(action)"
      >
        <div class="action-icon">{{ action.icon }}</div>
        <div class="action-label">{{ action.label }}</div>
        <div class="action-description">{{ action.description }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { QuickActions } from '../constants/messageTypes'

const props = defineProps({
  actions: {
    type: Array,
    default: () => QuickActions
  }
})

const emit = defineEmits(['action'])

// 处理快速操作点击
const handleActionClick = (action) => {
  emit('action', {
    queryType: action.queryType,
    label: action.label
  })
}
</script>

<style scoped>
.quick-actions-wrapper {
  margin-bottom: 16px;
}

.quick-actions-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  padding-left: 4px;
}

.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 12px;
}

.quick-action-item {
  background: white;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  padding: 16px 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.quick-action-item:hover {
  border-color: #409eff;
  background: #ecf5ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.action-icon {
  font-size: 32px;
  line-height: 1;
}

.action-label {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.action-description {
  font-size: 11px;
  color: #999;
  line-height: 1.3;
}
</style>
