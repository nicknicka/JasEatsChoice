<template>
  <div v-if="visible" class="search-results-panel">
    <div class="search-header">
      <span>找到 {{ results.length }} 条结果</span>
      <el-button type="text" size="small" @click="$emit('clear')">
        <el-icon><Close /></el-icon> 清除
      </el-button>
    </div>

    <div class="search-results-list">
      <div
        v-for="(result, index) in results"
        :key="result.id"
        class="search-result-item"
        :class="{ active: currentIndex === index }"
        @click="$emit('jump', index)"
      >
        <div class="result-time">
          {{ result.formattedTime || result.time }}
        </div>
        <div class="result-content" v-html="result.highlightedContent"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Close } from '@element-plus/icons-vue'

defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  results: {
    type: Array,
    default: () => []
  },
  currentIndex: {
    type: Number,
    default: -1
  }
})

defineEmits(['clear', 'jump'])
</script>

<style scoped lang="less">
.search-results-panel {
  position: absolute;
  top: 73px;
  right: 0;
  width: 300px;
  max-height: 400px;
  background-color: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  z-index: 100;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  .search-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    border-bottom: 1px solid #e4e7ed;
    background-color: #fafafa;

    span {
      font-size: 14px;
      font-weight: 500;
      color: #303133;
    }
  }

  .search-results-list {
    flex: 1;
    overflow-y: auto;
    padding: 8px 0;

    .search-result-item {
      padding: 12px 16px;
      cursor: pointer;
      transition: background-color 0.2s;
      border-bottom: 1px solid #f5f7fa;

      &:last-child {
        border-bottom: none;
      }

      &:hover {
        background-color: #f5f7fa;
      }

      &.active {
        background-color: #ecf5ff;
        border-left: 3px solid #409eff;
      }

      .result-time {
        font-size: 12px;
        color: #909399;
        margin-bottom: 4px;
      }

      .result-content {
        font-size: 13px;
        color: #606266;
        line-height: 1.5;
        word-break: break-word;

        :deep(mark) {
          background-color: #ffeb3b;
          padding: 0 2px;
          border-radius: 2px;
        }
      }
    }
  }
}
</style>
