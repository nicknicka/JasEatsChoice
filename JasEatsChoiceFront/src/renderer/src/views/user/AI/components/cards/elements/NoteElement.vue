<template>
  <div class="note-element">
    <el-alert
      :title="element.content || ''"
      :type="alertType"
      :closable="false"
      show-icon
    />
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
</style>
