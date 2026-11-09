<template>
  <div class="uni-card-actions">
    <button
      v-for="(action, index) in actions"
      :key="index"
      :class="['action-btn', `action-btn--${getButtonType(action.type)}`]"
      @click="handleAction(action)"
    >
      {{ action.text }}
    </button>
  </div>
</template>

<script setup>
const props = defineProps({
  actions: {
    type: Array,
    required: true
  }
})

const emit = defineEmits(['action'])

// 操作按钮类型映射
const BUTTON_TYPE_MAP = {
  primary: 'primary',
  success: 'success',
  warning: 'warning',
  danger: 'danger',
  default: 'default',
  text: 'default'
}

/**
 * 获取 Element Plus 按钮类型
 * @param {string} type - 操作类型
 * @returns {string}
 */
const getButtonType = (type) => {
  return BUTTON_TYPE_MAP[type] || 'default'
}

/**
 * 处理操作点击
 * @param {Object} action - 操作数据
 */
const handleAction = (action) => {
  emit('action', {
    type: action.value || action.type,
    data: action
  })
}
</script>

<style scoped>
.uni-card-actions {
  display: flex;
  gap: 12px;
  padding: 12px 20px;
  background: var(--surface);
  border-top: 1px solid var(--line);
  flex-wrap: wrap;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 20px;
  border: none;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  line-height: 1.4;
  white-space: nowrap;
  letter-spacing: 0.2px;
}

.action-btn--primary {
  background: var(--brand-500);
  color: var(--surface);
  box-shadow: var(--shadow-sm);
}

.action-btn--primary:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

.action-btn--success {
  background: var(--success);
  color: var(--surface);
  box-shadow: var(--shadow-sm);
}

.action-btn--success:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

.action-btn--warning {
  background: var(--warning);
  color: var(--ink-900);
  box-shadow: var(--shadow-sm);
}

.action-btn--warning:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

.action-btn--danger {
  background: var(--danger);
  color: var(--surface);
  box-shadow: var(--shadow-sm);
}

.action-btn--danger:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

.action-btn--default {
  background: var(--canvas-sunken);
  color: var(--ink-700);
}

.action-btn--default:hover {
  background: var(--neutral-bg);
  transform: translateY(-1px);
}
</style>
