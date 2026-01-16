<template>
  <el-card
    shadow="hover"
    class="dish-card"
    @click="handleClick"
    :aria-label="`菜品: ${dish.name}, ${dish.kcal} 卡路里, 评分: ${dish.rating || 0}分`"
    role="article"
    tabindex="0"
    @keyup.enter="handleClick"
  >
    <!-- 菜品图片区域 -->
    <div class="dish-image-background">
      <img
        :src="dish.image || defaultImage"
        :alt="dish.name"
        loading="lazy"
        decoding="async"
        @error="handleImageError"
      />
      <span class="dish-category">{{ dish.category || '推荐' }}</span>
    </div>

    <!-- 菜品信息区域 -->
    <div class="dish-info-overlay">
      <div class="dish-header">
        <div class="dish-name">{{ dish.name }}</div>
        <div class="dish-actions">
          <el-button
            circle
            size="small"
            class="share-btn"
            @click="handleShare"
            :aria-label="`分享 ${dish.name}`"
            tabindex="0"
            @keyup.enter="handleShare"
          >
            <el-icon><Share /></el-icon>
          </el-button>
          <el-button
            circle
            size="small"
            class="favorite-btn"
            @click="handleToggleFavorite"
            :class="{ 'is-favorite': isFavorite }"
            :aria-label="`${isFavorite ? '取消收藏' : '收藏'} ${dish.name}`"
            tabindex="0"
            @keyup.enter="handleToggleFavorite"
          >
            <el-icon><Star /></el-icon>
          </el-button>
        </div>
      </div>

      <div class="dish-meta">
        <span class="dish-kcal">{{ dish.kcal }} kcal</span>
        <span v-if="dish.tags" class="dish-tags">{{ dish.tags }}</span>
      </div>

      <div class="dish-rating">
        <el-rate
          v-if="dish.rating && dish.rating > 0"
          :model-value="dish.rating"
          disabled
          show-score
          text-color="#FF6B6B"
          class="rating"
        />
        <div v-else class="no-rating">
          <el-icon><Star /></el-icon>
          <span>暂无评分</span>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { Share, Star } from '@element-plus/icons-vue'
import type { Dish } from '../../../types'
import { HOME_CONSTANTS } from '../../../constants/home'

interface Props {
  dish: Dish
  isFavorite: boolean
}

interface Emits {
  (e: 'toggle-favorite', dish: Dish, event: Event): void
  (e: 'share', dish: Dish, event: Event): void
  (e: 'click', dish: Dish): void
}

defineProps<Props>()
const emit = defineEmits<Emits>()

const defaultImage = HOME_CONSTANTS.DEFAULT_DISH_IMAGE

const handleToggleFavorite = (event: Event) => {
  emit('toggle-favorite', props.dish, event)
}

const handleShare = (event: Event) => {
  emit('share', props.dish, event)
}

const handleClick = () => {
  emit('click', props.dish)
}

const handleImageError = (event: Event) => {
  const target = event.target as HTMLImageElement
  target.src = defaultImage
}

const props = defineProps<Props>()
</script>

<style scoped lang="less">
.dish-card {
  height: 320px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  border-radius: 16px;

  &:hover {
    transform: translateY(-8px) scale(1.02);
    box-shadow: 0 16px 40px rgba(255, 107, 107, 0.3);

    .dish-image-background img {
      transform: scale(1.1);
    }

    .share-btn,
    .favorite-btn {
      opacity: 1;
      transform: translateY(0);
    }
  }

  &:active {
    transform: translateY(-4px) scale(0.98);
    transition: all 0.1s ease;
  }

  .dish-image-background {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 1;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
      filter: brightness(0.95) contrast(1.05) saturate(1.05);
    }

    .dish-category {
      position: absolute;
      top: 16px;
      left: 16px;
      background: linear-gradient(135deg, rgba(255, 107, 107, 0.95) 0%, rgba(255, 135, 135, 0.95) 100%);
      color: white;
      padding: 8px 16px;
      border-radius: 20px;
      font-size: 13px;
      font-weight: 700;
      backdrop-filter: blur(8px);
      box-shadow: 0 4px 12px rgba(255, 107, 107, 0.4);
      z-index: 2;
      letter-spacing: 0.5px;
      border: 1px solid rgba(255, 255, 255, 0.2);
    }
  }

  .dish-info-overlay {
    position: relative;
    z-index: 2;
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
    padding: 24px;
    background: linear-gradient(
      to top,
      rgba(0, 0, 0, 0.88) 0%,
      rgba(0, 0, 0, 0.65) 35%,
      rgba(0, 0, 0, 0.35) 65%,
      transparent 100%
    );

    .dish-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 12px;
      margin-bottom: 12px;

      .dish-name {
        flex: 1;
        font-size: 24px;
        font-weight: 700;
        color: #fff;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        text-shadow: 0 2px 8px rgba(0, 0, 0, 0.6);
        letter-spacing: 0.5px;
      }

      .dish-actions {
        display: flex;
        gap: 8px;
        flex-shrink: 0;
      }

      .share-btn,
      .favorite-btn {
        width: 40px;
        height: 40px;
        background: rgba(255, 255, 255, 0.15);
        border: 1px solid rgba(255, 255, 255, 0.2);
        backdrop-filter: blur(8px);
        color: #fff;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        opacity: 0.9;

        &:hover {
          background: rgba(255, 255, 255, 0.35);
          transform: scale(1.15) translateY(-2px);
          box-shadow: 0 6px 16px rgba(0, 0, 0, 0.4);
          opacity: 1;
        }

        &:active {
          transform: scale(1.05) translateY(0);
        }
      }

      .favorite-btn.is-favorite {
        background: rgba(255, 215, 0, 0.35);
        border-color: rgba(255, 215, 0, 0.5);
        color: #ffd700;
        box-shadow: 0 0 16px rgba(255, 215, 0, 0.4);

        .el-icon {
          animation: star-bounce 0.3s ease;
        }
      }
    }

    .dish-meta {
      display: flex;
      justify-content: flex-start;
      align-items: center;
      gap: 12px;
      margin-bottom: 12px;
      font-size: 14px;

      .dish-kcal {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        color: #fff;
        font-weight: 700;
        padding: 8px 16px;
        background: linear-gradient(135deg, #ff6b6b 0%, #ff8787 100%);
        border-radius: 20px;
        backdrop-filter: blur(8px);
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
        box-shadow: 0 3px 10px rgba(255, 107, 107, 0.4);
        font-size: 15px;
        border: 1px solid rgba(255, 255, 255, 0.2);

        &::before {
          content: '🔥';
          font-size: 16px;
          animation: flame-flicker 0.5s ease-in-out infinite alternate;
        }
      }

      .dish-tags {
        color: rgba(255, 255, 255, 0.95);
        font-size: 13px;
        font-weight: 600;
        padding: 6px 14px;
        background: rgba(255, 255, 255, 0.15);
        backdrop-filter: blur(8px);
        border-radius: 16px;
        border: 1px solid rgba(255, 255, 255, 0.2);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
      }
    }

    .dish-rating {
      margin-top: 4px;

      :deep(.el-rate) .el-rate__icon {
        font-size: 22px;
        color: #ffd700;
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
      }

      :deep(.el-rate__text) {
        color: #fff !important;
        font-size: 16px;
        font-weight: 700;
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
      }

      .no-rating {
        display: flex;
        align-items: center;
        gap: 6px;
        color: rgba(255, 255, 255, 0.8);
        font-size: 14px;
        font-weight: 500;

        .el-icon {
          font-size: 18px;
          opacity: 0.6;
        }
      }
    }
  }
}

@keyframes star-bounce {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.3);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes flame-flicker {
  0% {
    transform: scale(1) rotate(-2deg);
    opacity: 0.9;
  }
  100% {
    transform: scale(1.1) rotate(2deg);
    opacity: 1;
  }
}
</style>
