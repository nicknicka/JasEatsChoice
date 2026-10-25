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
@import '../../assets/css/nordic-theme.less';

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: @nordic-space-md;
  padding: 0 0 @nordic-space-md 0;
  border-bottom: 1px solid @nordic-border;

  .page-title {
    margin: 0;
    font-size: @nordic-text-xl;
    font-weight: 700;
    color: @nordic-text;
    letter-spacing: @nordic-letter-tight;
  }

  .chat-actions {
    display: flex;
    align-items: center;

    .unified-action-btn {
      min-width: 144px;
      padding: 12px 26px;
      border-radius: 16px;
      border: 1px solid fade(@nordic-accent, 24%);
      background:
        linear-gradient(180deg, fade(@nordic-surface, 98%) 0%, #f8f3ed 100%);
      color: @nordic-accent-dark;
      font-size: @nordic-text-md;
      font-weight: 600;
      box-shadow:
        0 14px 28px fade(@nordic-accent, 16%),
        inset 0 1px 0 rgba(255, 255, 255, 0.85);
      transition: all @nordic-transition-base ease;

      &:hover {
        border-color: fade(@nordic-accent, 42%);
        color: @nordic-accent-dark;
        background:
          linear-gradient(180deg, #fff8f3 0%, #f4e5d8 100%);
        box-shadow:
          0 18px 34px fade(@nordic-accent, 20%),
          inset 0 1px 0 rgba(255, 255, 255, 0.92);
        transform: translateY(-1px);
      }

      &:active {
        transform: translateY(0);
        box-shadow:
          0 8px 18px fade(@nordic-accent, 14%),
          inset 0 1px 0 rgba(255, 255, 255, 0.8);
      }

      .btn-icon {
        margin-right: 8px;
        font-size: 18px;
      }

      span {
        letter-spacing: @nordic-letter-wide;
      }
    }
  }
}
</style>
