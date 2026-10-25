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
  background: white;
  border-top: 1px solid #f0f2f5;
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
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.action-btn--primary:hover {
  box-shadow: 0 4px 14px rgba(102, 126, 234, 0.45);
  transform: translateY(-1px);
}

.action-btn--success {
  background: linear-gradient(135deg, #56d48f, #38b26a);
  color: white;
  box-shadow: 0 2px 8px rgba(86, 212, 143, 0.3);
}

.action-btn--success:hover {
  box-shadow: 0 4px 14px rgba(86, 212, 143, 0.45);
  transform: translateY(-1px);
}

.action-btn--warning {
  background: linear-gradient(135deg, #ffd93d, #f0b429);
  color: #6b5200;
  box-shadow: 0 2px 8px rgba(255, 217, 61, 0.3);
}

.action-btn--warning:hover {
  box-shadow: 0 4px 14px rgba(255, 217, 61, 0.45);
  transform: translateY(-1px);
}

.action-btn--danger {
  background: linear-gradient(135deg, #ff6b6b, #ee5a52);
  color: white;
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3);
}

.action-btn--danger:hover {
  box-shadow: 0 4px 14px rgba(255, 107, 107, 0.45);
  transform: translateY(-1px);
}

.action-btn--default {
  background: #f0f2f5;
  color: #1f2937;
}

.action-btn--default:hover {
  background: #e8eaee;
  transform: translateY(-1px);
}
</style>
