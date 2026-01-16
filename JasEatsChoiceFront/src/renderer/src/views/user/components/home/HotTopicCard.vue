<template>
  <div v-if="topic" class="hot-section">
    <el-card shadow="hover" class="hot-card">
      <div class="hot-content">
        <div class="hot-icon-wrapper">
          <span class="fire-icon">🔥</span>
          <span class="hot-badge">HOT</span>
        </div>
        <div class="hot-text">
          <span class="hot-label">今日热点</span>
          <span class="hot-description">{{ topic }}</span>
        </div>
        <el-icon class="hot-arrow"><ArrowRight /></el-icon>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ArrowRight } from '@element-plus/icons-vue'

interface Props {
  topic?: string
}

defineProps<Props>()
</script>

<style scoped lang="less">
.hot-section {
  margin-bottom: 20px;

  .hot-card {
    background: linear-gradient(135deg, #fff5f5 0%, #ffe8e8 50%, #fff 100%);
    border: none;
    overflow: hidden;
    position: relative;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 0 4px 16px rgba(255, 107, 107, 0.1);

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      width: 4px;
      height: 100%;
      background: linear-gradient(180deg, #ff6b6b 0%, #ff8787 100%);
    }

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(255, 107, 107, 0.2);
    }

    :deep(.el-card__body) {
      padding: 20px 24px;
    }
  }

  .hot-content {
    display: flex;
    align-items: center;
    gap: 20px;

    .hot-icon-wrapper {
      display: flex;
      align-items: center;
      gap: 12px;
      flex-shrink: 0;

      .fire-icon {
        font-size: 36px;
        animation: fire-pulse 2s ease-in-out infinite;
        display: inline-block;
        filter: drop-shadow(0 2px 4px rgba(255, 107, 107, 0.3));
      }

      .hot-badge {
        background: linear-gradient(135deg, #ff6b6b 0%, #ff8787 100%);
        color: white;
        padding: 6px 14px;
        border-radius: 16px;
        font-size: 12px;
        font-weight: 700;
        letter-spacing: 1px;
        box-shadow: 0 4px 12px rgba(255, 107, 107, 0.4);
        border: 1px solid rgba(255, 255, 255, 0.2);
      }
    }

    .hot-text {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 6px;

      .hot-label {
        font-size: 13px;
        color: #999;
        font-weight: 600;
        text-transform: uppercase;
        letter-spacing: 0.5px;
      }

      .hot-description {
        font-size: 17px;
        color: #333;
        font-weight: 600;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        line-height: 1.4;
      }
    }

    .hot-arrow {
      color: #ff6b6b;
      font-size: 20px;
      flex-shrink: 0;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      background: rgba(255, 107, 107, 0.1);
      padding: 8px;
      border-radius: 12px;

      &:hover {
        transform: translateX(6px);
        background: rgba(255, 107, 107, 0.2);
      }
    }
  }
}

@keyframes fire-pulse {
  0%,
  100% {
    transform: scale(1) rotate(0deg);
  }
  25% {
    transform: scale(1.05) rotate(-3deg);
  }
  50% {
    transform: scale(1.1) rotate(0deg);
  }
  75% {
    transform: scale(1.05) rotate(3deg);
  }
}
</style>
