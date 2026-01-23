<template>
  <div class="chat-skeleton">
    <div
      v-for="index in count"
      :key="index"
      class="skeleton-message"
      :class="{ 'skeleton-user': index % 2 === 0 }"
    >
      <div class="skeleton-avatar"></div>
      <div class="skeleton-content">
        <div class="skeleton-line" :style="{ width: getRandomWidth() }"></div>
        <div class="skeleton-line" :style="{ width: getRandomWidth() }"></div>
        <div v-if="index % 3 === 0" class="skeleton-line" :style="{ width: '60%' }"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue'

defineProps({
  count: {
    type: Number,
    default: 3
  }
})

const getRandomWidth = () => {
  return `${Math.floor(Math.random() * 30) + 70}%`
}
</script>

<style scoped lang="less">
.chat-skeleton {
  padding: 24px;
}

.skeleton-message {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;

  &.skeleton-user {
    flex-direction: row-reverse;
  }

  .skeleton-avatar {
    width: 42px;
    height: 42px;
    border-radius: 50%;
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: skeleton-loading 1.5s ease-in-out infinite;
    flex-shrink: 0;
  }

  .skeleton-content {
    display: flex;
    flex-direction: column;
    gap: 8px;
    max-width: 75%;

    .skeleton-line {
      height: 16px;
      border-radius: 8px;
      background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
      background-size: 200% 100%;
      animation: skeleton-loading 1.5s ease-in-out infinite;
    }
  }
}

@keyframes skeleton-loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>
