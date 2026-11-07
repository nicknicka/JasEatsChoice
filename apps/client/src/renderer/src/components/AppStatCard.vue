<template>
  <AppCard class="app-stat-card" :level="1" padding="md">
    <div v-if="props.icon || props.trend" class="app-stat-card__top">
      <el-icon v-if="props.icon" class="app-stat-card__icon" aria-hidden="true">
        <component :is="props.icon" />
      </el-icon>

      <span
        v-if="props.trend"
        class="app-stat-card__trend"
        :class="`app-stat-card__trend--${props.trend.direction}`"
      >
        <el-icon aria-hidden="true">
          <component :is="trendIcon" />
        </el-icon>
        {{ props.trend.text }}
      </span>
    </div>

    <div class="app-stat-card__value">
      <span class="app-stat-card__number">
        <slot>{{ props.value }}</slot>
      </span>
      <span v-if="props.suffix" class="app-stat-card__suffix">{{ props.suffix }}</span>
    </div>

    <div class="app-stat-card__label">{{ props.label }}</div>
  </AppCard>
</template>

<script setup>
import { computed } from 'vue'
import { Bottom, Minus, Top } from '@element-plus/icons-vue'
import AppCard from './AppCard.vue'

const props = defineProps({
  label: {
    type: String,
    required: true,
    validator: (value) => typeof value === 'string' && value.trim().length > 0
  },
  value: {
    type: [String, Number],
    required: true
  },
  icon: {
    type: [Object, Function, String],
    default: null
  },
  suffix: {
    type: String,
    default: ''
  },
  trend: {
    type: Object,
    default: null,
    validator: (value) =>
      value === null ||
      (['up', 'down', 'flat'].includes(value.direction) && typeof value.text === 'string')
  }
})

const TREND_ICONS = Object.freeze({
  up: Top,
  down: Bottom,
  flat: Minus
})

const trendIcon = computed(() => (props.trend ? TREND_ICONS[props.trend.direction] : null))
</script>

<style scoped>
.app-stat-card {
  width: 100%;
}

.app-stat-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}

.app-stat-card__icon {
  color: var(--brand-500);
  font-size: var(--text-h2);
}

.app-stat-card__trend {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  margin-left: auto;
  color: var(--neutral);
  font-size: var(--text-caption);
  font-weight: var(--weight-medium);
  line-height: normal;
  white-space: nowrap;
}

.app-stat-card__trend--up {
  color: var(--success);
}

.app-stat-card__trend--down {
  color: var(--danger);
}

.app-stat-card__trend--flat {
  color: var(--neutral);
}

.app-stat-card__value {
  display: flex;
  align-items: baseline;
  gap: var(--space-1);
  min-width: 0;
  margin-top: var(--space-2);
  color: var(--ink-900);
  font-family: var(--font-num);
  font-size: var(--text-display);
  font-weight: var(--weight-bold);
  font-variant-numeric: tabular-nums;
  letter-spacing: var(--tracking-tight);
  line-height: normal;
}

.app-stat-card__number {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-stat-card__suffix {
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
}

.app-stat-card__label {
  margin-top: var(--space-2);
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  line-height: normal;
}
</style>
