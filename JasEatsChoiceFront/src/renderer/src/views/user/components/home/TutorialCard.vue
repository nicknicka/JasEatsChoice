<template>
  <el-card
    shadow="hover"
    class="tutorial-card"
    @click="handleClick"
    :aria-label="`教程: ${tutorial.name}, ${tutorial.duration || '5分钟'}`"
    role="listitem"
    tabindex="0"
    @keyup.enter="handleClick"
  >
    <div class="tutorial-thumbnail">
      <img
        :src="tutorial.thumbnail || defaultThumbnail"
        :alt="tutorial.name"
        loading="lazy"
        decoding="async"
      />
      <div class="tutorial-type-badge">
        <el-icon v-if="tutorial.type === 'video'"><VideoCamera /></el-icon>
        <span v-else>💡</span>
      </div>
    </div>

    <div class="tutorial-content">
      <h4 class="tutorial-title">{{ tutorial.name }}</h4>
      <div class="tutorial-meta">
        <span class="tutorial-duration">{{ tutorial.duration || '5分钟' }}</span>
        <el-rate
          v-if="tutorial.rating"
          :model-value="tutorial.rating"
          disabled
          size="small"
          show-score
        />
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { VideoCamera } from '@element-plus/icons-vue'
import type { Tutorial } from '../../../types'
import { HOME_CONSTANTS } from '../../../constants/home'

interface Props {
  tutorial: Tutorial
}

interface Emits {
  (e: 'click', tutorial: Tutorial): void
}

defineProps<Props>()
const emit = defineEmits<Emits>()

const defaultThumbnail = HOME_CONSTANTS.DEFAULT_TUTORIAL_THUMBNAIL

const handleClick = () => {
  emit('click', props.tutorial)
}

const props = defineProps<Props>()
</script>

<style scoped lang="less">
.tutorial-card {
  height: 200px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(255, 107, 107, 0.15);

    .tutorial-thumbnail img {
      transform: scale(1.05);
    }
  }

  .tutorial-thumbnail {
    width: 100%;
    height: 120px;
    position: relative;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }

    .tutorial-type-badge {
      position: absolute;
      top: 8px;
      right: 8px;
      width: 32px;
      height: 32px;
      background: rgba(0, 0, 0, 0.6);
      backdrop-filter: blur(4px);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 14px;

      .el-icon {
        font-size: 16px;
      }
    }
  }

  .tutorial-content {
    flex: 1;
    padding: 12px;
    display: flex;
    flex-direction: column;

    .tutorial-title {
      margin: 0 0 8px 0;
      font-size: 15px;
      font-weight: 600;
      color: #333;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .tutorial-meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: auto;
      font-size: 13px;

      .tutorial-duration {
        color: #999;
      }

      :deep(.el-rate__text) {
        font-size: 12px;
      }
    }
  }
}
</style>
