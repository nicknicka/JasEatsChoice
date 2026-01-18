<template>
  <div v-if="replyingTo" class="reply-preview">
    <div class="reply-content">
      <div class="reply-header">
        <span class="reply-label">回复 @{{ replyingTo.senderName || replyingTo.fromId }}</span>
        <el-icon @click="$emit('cancel')" style="cursor: pointer"><Close /></el-icon>
      </div>
      <div class="reply-text">{{ replyingTo.content }}</div>
    </div>
  </div>
</template>

<script setup>
import { Close } from '@element-plus/icons-vue'

defineProps({
  replyingTo: {
    type: Object,
    default: null
  }
})

defineEmits(['cancel'])
</script>

<style scoped lang="less">
.reply-preview {
  padding: 10px 12px;
  background-color: #f5f7fa;
  border-radius: 6px;
  border-left: 3px solid #409eff;

  .reply-content {
    .reply-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 6px;

      .reply-label {
        font-size: 13px;
        font-weight: 500;
        color: #409eff;
      }
    }

    .reply-text {
      font-size: 12px;
      color: #606266;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }
  }
}
</style>
