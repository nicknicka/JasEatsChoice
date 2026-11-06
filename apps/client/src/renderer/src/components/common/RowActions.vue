<template>
  <div v-if="props.primary" class="row-actions">
    <el-button link class="row-actions__primary" @click="props.primary.onClick">
      {{ props.primary.text }}
    </el-button>

    <el-button
      v-for="(item, index) in visibleItems"
      :key="`${item.text}-${index}`"
      link
      :disabled="item.disabled"
      @click="handleItemClick(item)"
    >
      {{ item.text }}
    </el-button>

    <el-dropdown v-if="moreItems.length" trigger="click" popper-class="row-actions__popper">
      <el-button link class="row-actions__more">更多</el-button>

      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item
            v-for="(item, index) in moreItems"
            :key="`${item.text}-${index}`"
            :disabled="item.disabled"
            :class="{ 'row-actions__item--danger': item.danger }"
            @click="handleItemClick(item)"
          >
            {{ item.text }}
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessageBox } from 'element-plus'

const props = defineProps({
  primary: {
    type: Object,
    default: null,
    validator: (value) =>
      value === null || (typeof value?.text === 'string' && typeof value?.onClick === 'function')
  },
  items: {
    type: Array,
    required: true,
    validator: (value) =>
      value.every(
        (item) =>
          item &&
          typeof item.text === 'string' &&
          typeof item.onClick === 'function' &&
          (item.danger === undefined || typeof item.danger === 'boolean') &&
          (item.disabled === undefined || typeof item.disabled === 'boolean')
      )
  },
  maxVisible: {
    type: Number,
    default: 1,
    validator: (value) => Number.isInteger(value) && value >= 0
  }
})

const visibleItems = computed(() =>
  props.items.filter((item) => !item.danger).slice(0, props.maxVisible)
)

const moreItems = computed(() => {
  const visible = new Set(visibleItems.value)
  return props.items.filter((item) => item.danger || !visible.has(item))
})

const handleItemClick = async (item) => {
  if (item.disabled) return

  if (item.danger) {
    const confirmed = await ElMessageBox.confirm(`确定要执行“${item.text}”吗？`, '危险操作确认', {
      confirmButtonText: '确认执行',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'row-actions__confirm-button',
      distinguishCancelAndClose: true
    })
      .then(() => true)
      .catch(() => false)

    if (!confirmed) return
  }

  item.onClick()
}
</script>

<style scoped>
.row-actions {
  display: inline-flex;
  align-items: center;
  gap: var(--space-3);
  white-space: nowrap;
}

.row-actions :deep(.el-button.is-link) {
  min-height: auto;
  padding: 0;
  color: var(--brand-600);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
}

.row-actions :deep(.el-button.is-link:hover),
.row-actions :deep(.el-button.is-link:focus) {
  color: var(--brand-500);
}

.row-actions :deep(.el-button.is-link:active) {
  color: var(--brand-700);
}

.row-actions :deep(.el-button.is-link.is-disabled) {
  color: var(--el-text-color-placeholder);
}

.row-actions :deep(.el-button.is-link:focus-visible) {
  border-radius: var(--radius-sm);
  outline: 2px solid var(--brand-200);
  outline-offset: 2px;
}

:global(.row-actions__popper .el-dropdown-menu) {
  padding: var(--space-1) 0;
  border-color: var(--el-border-color-lighter);
  border-radius: var(--el-border-radius-base);
  background-color: var(--el-bg-color);
}

:global(.row-actions__popper .el-dropdown-menu__item) {
  min-width: 96px;
  padding: var(--space-2) var(--space-4);
  color: var(--el-text-color-regular);
  font-size: var(--el-font-size-base);
}

:global(.row-actions__popper .row-actions__item--danger) {
  color: var(--danger);
}

:global(.row-actions__popper .row-actions__item--danger:hover),
:global(.row-actions__popper .row-actions__item--danger:focus) {
  color: var(--danger);
  background-color: var(--danger-bg);
}

:global(.row-actions__confirm-button.el-button--primary) {
  --el-button-bg-color: var(--danger);
  --el-button-border-color: var(--danger);
  --el-button-hover-bg-color: color-mix(in srgb, var(--danger) 88%, var(--surface));
  --el-button-hover-border-color: color-mix(in srgb, var(--danger) 88%, var(--surface));
  --el-button-hover-text-color: var(--surface);
  --el-button-active-bg-color: var(--danger);
  --el-button-active-border-color: var(--danger);
  --el-button-outline-color: var(--danger-bg);
}
</style>
