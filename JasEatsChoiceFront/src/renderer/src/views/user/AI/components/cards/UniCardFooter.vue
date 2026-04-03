<template>
  <div class="uni-card-footer">
    <div v-if="footer.text" class="footer-text">{{ footer.text }}</div>
    <div v-if="footer.actions?.length" class="footer-actions">
      <el-button
        v-for="(action, index) in footer.actions"
        :key="index"
        type="primary"
        link
        size="small"
        @click="handleAction(action)"
      >
        {{ action.text }}
      </el-button>
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
  padding: 8px 20px;
  background: #f9fafb;
  border-top: 1px solid #f0f0f0;
}

.footer-text {
  font-size: 12px;
  color: #999;
  line-height: 1.5;
}

.footer-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}
</style>
