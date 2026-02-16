<template>
  <div class="chat-header">
    <h3 class="page-title">【聊天消息】</h3>
    <div class="chat-actions">
      <el-button type="primary" size="large" class="unified-action-btn" @click="$emit('open-action-panel')">
        <el-icon class="btn-icon"><Plus /></el-icon>
        <span>新建</span>
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { Plus } from '@element-plus/icons-vue'
defineEmits(['open-action-panel'])
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
    font-size: 1.429rem /* 原值: 20px */;
    font-weight: 600;
    color: #303133;
  }

  .chat-actions {
    display: flex;
    gap: 12px;
    align-items: center;

    .unified-action-btn {
      font-weight: 600;
      padding: 12px 32px;
      font-size: 1.143rem /* 原值: 16px */;
      border-radius: 12px;
      background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
      border: none;
      box-shadow: 0 4px 15px rgba(59, 130, 246, 0.4);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 25px rgba(59, 130, 246, 0.5);
        background: linear-gradient(135deg, #60a5fa 0%, #2563eb 100%);
      }

      &:active {
        transform: translateY(0);
        box-shadow: 0 2px 10px rgba(59, 130, 246, 0.4);
      }

      .btn-icon {
        font-size: 1.286rem /* 原值: 18px */;
        margin-right: 6px;
      }

      span {
        letter-spacing: 0.5px;
      }
    }
  }
}
</style>
