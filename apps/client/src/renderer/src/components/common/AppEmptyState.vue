<template>
  <div class="app-empty-state" :class="{ 'app-empty-state--compact': props.compact }">
    <el-icon class="app-empty-state__icon" aria-hidden="true">
      <component :is="props.icon || Box" />
    </el-icon>
    <p class="app-empty-state__description">{{ props.description }}</p>
    <el-button
      v-if="props.actionText"
      class="app-empty-state__action"
      :class="{ 'app-empty-state__action--solid': props.actionVariant === 'solid' }"
      :size="props.actionSize || undefined"
      type="primary"
      :link="props.actionVariant === 'link'"
      @click="emit('action')"
    >
      {{ props.actionText }}
    </el-button>
  </div>
</template>

<script setup>
import { Box } from '@element-plus/icons-vue'

const props = defineProps({
  description: {
    type: String,
    default: '暂时没有内容'
  },
  actionText: {
    type: String,
    default: ''
  },
  actionVariant: {
    type: String,
    default: 'link',
    validator: (value) => ['link', 'solid'].includes(value)
  },
  actionSize: {
    type: String,
    default: ''
  },
  icon: {
    type: [Object, Function, String],
    default: null
  },
  compact: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['action'])
</script>

<style scoped>
.app-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 32px 24px;
  text-align: center;
}

.app-empty-state__icon {
  color: var(--ink-400);
  font-size: 40px;
}

.app-empty-state__description {
  margin: 0;
  color: var(--ink-500);
  font-size: 14px;
  line-height: 22px;
}

.app-empty-state__action--solid {
  border-radius: var(--radius-md);
  color: var(--surface);
  font-weight: var(--weight-medium);
}

.app-empty-state--compact {
  gap: 8px;
  padding: 20px 16px;
}

.app-empty-state--compact .app-empty-state__icon {
  font-size: 32px;
}

.app-empty-state--compact .app-empty-state__description {
  font-size: 13px;
  line-height: 20px;
}
</style>
