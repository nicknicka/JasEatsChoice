<template>
  <div class="chat-header">
    <h3 class="page-title">【聊天消息】</h3>
    <div class="chat-actions">
      <el-button type="primary" size="small" @click="$emit('create-new-chat')">
        + 新建聊天
      </el-button>
      <el-button type="primary" size="small" @click="$emit('add-friend')">
        + 加好友
      </el-button>
      <el-button type="primary" size="small" @click="$emit('create-group')">
        + 新建群聊
      </el-button>
    </div>
  </div>
</template>

<script setup>
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
  }
}
</style>
