<template>
  <transition name="slide-down">
    <div v-if="show" class="quick-questions-panel">
      <div class="quick-questions-header">
        <span class="quick-questions-title">💡 快捷提问</span>
        <el-button
          :icon="Close"
          circle
          size="small"
          text
          @click="$emit('close')"
        />
      </div>
      <div class="quick-questions-list">
        <el-tag
          v-for="question in questions"
          :key="question"
          @click="$emit('select', question)"
          class="question-tag"
          type="info"
          effect="plain"
        >
          {{ question }}
        </el-tag>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { Close } from '@element-plus/icons-vue'

defineProps({
  show: {
    type: Boolean,
    default: true
  },
  questions: {
    type: Array,
    required: true
  }
})

defineEmits(['close', 'select'])
</script>

<style scoped lang="less">
.quick-questions-panel {
  background: linear-gradient(135deg, #f0f9ff 0%, #e8f4fd 100%);
  border: 1px solid #d1e9ff;
  border-radius: 12px;
  padding: 12px 16px;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.08);

  .quick-questions-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;

    .quick-questions-title {
      font-size: 14px;
      font-weight: 600;
      color: #2c7be5;
    }
  }

  .quick-questions-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    .question-tag {
      margin: 0;
      padding: 6px 14px;
      cursor: pointer;
      transition: all 0.3s ease;
      font-size: 13px;
      font-weight: 500;
      border-radius: 20px;
      background-color: #fff;
      border-color: #b3e0ff;
      color: #409eff;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(64, 158, 255, 0.25);
        background: linear-gradient(135deg, #409eff 0%, #5dade2 100%);
        color: #fff;
        border-color: transparent;
      }
    }
  }
}

/* 快捷提问面板滑入滑出动画 */
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-down-enter-from {
  opacity: 0;
  transform: translateY(-12px);
}

.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-12px);
}
</style>
