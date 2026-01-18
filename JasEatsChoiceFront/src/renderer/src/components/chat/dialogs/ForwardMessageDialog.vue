<template>
  <el-dialog v-model="visible" title="转发消息" width="400px">
    <div v-if="message" class="forward-dialog-content">
      <div class="forward-preview">转发内容: {{ message.content }}</div>
      <div class="forward-target-select">
        <div class="select-label">选择转发到:</div>
        <el-select v-model="selectedTarget" placeholder="选择会话" style="width: 100%">
          <el-option
            v-for="conv in conversations"
            :key="conv.id"
            :label="conv.name"
            :value="conv.id"
          >
            <div class="conversation-option">
              <span>{{ conv.name }}</span>
              <span class="conversation-type-badge">{{ conv.type === 'group' ? '群聊' : '私聊' }}</span>
            </div>
          </el-option>
        </el-select>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleConfirm" :disabled="!selectedTarget">
          确认转发
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  message: {
    type: Object,
    default: null
  },
  conversations: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'confirm'])

const visible = ref(props.modelValue)
const selectedTarget = ref(null)

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (!val) {
    selectedTarget.value = null
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const handleConfirm = () => {
  if (!selectedTarget.value) return

  emit('confirm', {
    message: props.message,
    targetId: selectedTarget.value
  })

  visible.value = false
  selectedTarget.value = null
}

const handleCancel = () => {
  visible.value = false
  selectedTarget.value = null
}
</script>

<style scoped lang="less">
.forward-dialog-content {
  padding: 10px 0;

  .forward-preview {
    padding: 12px;
    background-color: #f5f7fa;
    border-radius: 6px;
    margin-bottom: 16px;
    font-size: 14px;
    color: #606266;
    word-break: break-word;
  }

  .forward-target-select {
    .select-label {
      margin-bottom: 8px;
      font-size: 14px;
      font-weight: 500;
      color: #303133;
    }

    .conversation-option {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .conversation-type-badge {
        font-size: 12px;
        padding: 2px 6px;
        border-radius: 4px;
        background-color: #ecf5ff;
        color: #409eff;
      }
    }
  }
}
</style>
