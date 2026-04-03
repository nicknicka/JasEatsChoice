<template>
  <div class="uni-card-actions">
    <el-button
      v-for="(action, index) in actions"
      :key="index"
      :type="getButtonType(action.type)"
      size="small"
      @click="handleAction(action)"
    >
      {{ action.text }}
    </el-button>
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
  gap: 8px;
  padding: 12px 20px;
  background: white;
  border-top: 1px solid #f0f0f0;
  flex-wrap: wrap;
}
</style>
