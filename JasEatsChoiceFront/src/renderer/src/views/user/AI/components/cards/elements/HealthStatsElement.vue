<template>
  <div class="health-stats-element">
    <!-- 营养统计列表 -->
    <div v-if="stats.length > 0" class="stats-list">
      <div
        v-for="(stat, index) in stats"
        :key="index"
        class="stat-item"
      >
        <div class="stat-header">
          <span class="stat-label">{{ stat.label }}</span>
          <span class="stat-value" :style="{ color: stat.color || '#667eea' }">
            {{ stat.value }}{{ stat.unit || '' }}
          </span>
        </div>
        <div class="custom-progress">
          <div
            class="custom-progress-bar"
            :style="{
              width: (stat.percent || 0) + '%',
              background: stat.color || '#667eea'
            }"
          ></div>
        </div>
      </div>
    </div>

    <!-- 健康建议 -->
    <div v-if="element.suggestion" class="suggestion">
      <div class="suggestion-card">
        <span class="suggestion-icon">💡</span>
        <span class="suggestion-text">{{ element.suggestion }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  element: {
    type: Object,
    required: true
  }
})

// 营养统计列表
const stats = computed(() => props.element.stats || [])
</script>

<style scoped>
.health-stats-element {
  width: 100%;
}

.stats-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-bottom: 14px;
}

.stat-item {
  padding: 2px 0;
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.stat-label {
  font-size: 13px;
  color: #1f2937;
  font-weight: 500;
}

.stat-value {
  font-size: 13px;
  font-weight: 600;
}

/* 自定义圆角进度条 */
.custom-progress {
  width: 100%;
  height: 6px;
  background: #f0f2f5;
  border-radius: 999px;
  overflow: hidden;
}

.custom-progress-bar {
  height: 100%;
  border-radius: 999px;
  transition: width 0.4s ease;
}

.suggestion {
  margin-top: 12px;
}

.suggestion-card {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 14px;
  background: #f0f4ff;
  border-radius: 12px;
  border: 1px solid rgba(102, 126, 234, 0.1);
}

.suggestion-icon {
  font-size: 16px;
  flex-shrink: 0;
  margin-top: 1px;
}

.suggestion-text {
  font-size: 13px;
  color: #4a5568;
  line-height: 1.5;
}
</style>
