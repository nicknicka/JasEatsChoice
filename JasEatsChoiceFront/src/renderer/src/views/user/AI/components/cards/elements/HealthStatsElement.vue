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
        <el-progress
          :percentage="stat.percent || 0"
          :color="stat.color || '#667eea'"
          :stroke-width="10"
          :show-text="false"
        />
      </div>
    </div>

    <!-- 健康建议 -->
    <div v-if="element.suggestion" class="suggestion">
      <el-alert
        :title="element.suggestion"
        type="info"
        :closable="false"
        show-icon
      />
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
  gap: 16px;
  margin-bottom: 16px;
}

.stat-item {
  padding: 4px 0;
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.stat-value {
  font-size: 14px;
  font-weight: 600;
}

.suggestion {
  margin-top: 12px;
}
</style>
