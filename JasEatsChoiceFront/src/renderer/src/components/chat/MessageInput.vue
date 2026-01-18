<template>
  <div class="message-input-container">
    <ReplyPreview :replying-to="replyingTo" @cancel="$emit('cancel-reply')" />

    <div class="input-wrapper">
      <el-input
        v-model="inputValue"
        type="textarea"
        placeholder="输入消息内容..."
        :rows="2"
        @keyup.enter="handleSend"
        :disabled="disabled"
      />
      <el-button type="primary" @click="handleSend" :disabled="disabled || !inputValue.trim()">
        发送
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import ReplyPreview from './ReplyPreview.vue'

const props = defineProps({
  replyingTo: {
    type: Object,
    default: null
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['send', 'cancel-reply'])

const inputValue = ref('')

const handleSend = () => {
  if (inputValue.value.trim()) {
    emit('send', inputValue.value.trim())
    inputValue.value = ''
  }
}

// 暴露方法供父组件调用
defineExpose({
  focus: () => {
    // 可以在这里添加聚焦逻辑
  },
  clear: () => {
    inputValue.value = ''
  }
})
</script>

<style scoped lang="less">
.message-input-container {
  padding: 12px;
  border-top: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  gap: 12px;

  .input-wrapper {
    display: flex;
    gap: 12px;
    width: 100%;
  }

  :deep(.el-input) {
    flex: 1;
  }

  button {
    align-self: flex-end;
  }
}
</style>
