<template>
  <div class="uni-card-footer">
    <div v-if="footer.note" class="footer-text">{{ footer.note }}</div>
    <div v-if="footer.actions?.length" class="footer-actions">
      <button
        v-for="(action, index) in footer.actions"
        :key="index"
        class="footer-link-btn"
        @click="handleAction(action)"
      >
        {{ action.text }}
      </button>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  footer: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['action'])

/**
 * 处理底部操作点击
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
.uni-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 20px;
  background: #f8f9fa;
  border-top: 1px solid #f0f2f5;
  border-radius: 0 0 16px 16px;
}

.footer-text {
  font-size: 12px;
  color: #8c93a0;
  line-height: 1.5;
}

.footer-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.footer-link-btn {
  background: none;
  border: none;
  padding: 2px 4px;
  font-size: 12px;
  font-weight: 500;
  color: #667eea;
  cursor: pointer;
  transition: color 0.2s ease;
  white-space: nowrap;
}

.footer-link-btn:hover {
  color: #5562d6;
  text-decoration: underline;
}
</style>
