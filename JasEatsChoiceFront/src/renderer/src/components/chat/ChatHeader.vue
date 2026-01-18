<template>
  <div class="chat-header">
    <h3 class="page-title">【聊天消息】</h3>
    <div class="chat-actions">
      <el-button type="primary" size="default" class="primary-action" @click="$emit('create-new-chat')">
        <el-icon><ChatDotRound /></el-icon>
        <span>新建聊天</span>
      </el-button>
      <el-button type="default" size="small" plain @click="$emit('add-friend')">
        <el-icon><UserFilled /></el-icon>
        <span>加好友</span>
      </el-button>
      <el-button type="default" size="small" plain @click="$emit('create-group')">
        <el-icon><User /></el-icon>
        <span>新建群聊</span>
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ChatDotRound, UserFilled, User } from '@element-plus/icons-vue'
defineEmits(['create-new-chat', 'add-friend', 'create-group'])
</script>

<style scoped lang="less">
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 0 16px 0;
  border-bottom: 1px solid #e4e7ed;

  .page-title {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: #303133;
  }

  .chat-actions {
    display: flex;
    gap: 12px;
    align-items: center;

    .primary-action {
      font-weight: 500;
      padding: 10px 20px;
      font-size: 15px;

      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 4px 8px rgba(64, 158, 255, 0.3);
      }
    }

    .el-button {
      display: flex;
      align-items: center;
      gap: 6px;
      transition: all 0.3s ease;

      .el-icon {
        font-size: 16px;
      }
    }
  }
}
</style>
