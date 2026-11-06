<template>
  <div class="order-filter-bar">
    <div class="filter-buttons" role="group" aria-label="订单状态筛选">
      <button
        v-for="status in statusList"
        :key="status.value"
        type="button"
        :class="['filter-tag', { active: activeStatus === status.value }]"
        @click="handleStatusClick(status.value)"
      >
        {{ status.label }}
      </button>
    </div>

    <!-- 排序选择器 -->
    <div class="sort-selector">
      <el-dropdown trigger="click" @command="handleSortChange">
        <el-button size="small" class="sort-dropdown-btn">
          <el-icon class="sort-icon">
            <component :is="currentSortOption.icon" />
          </el-icon>
          <span class="sort-label">{{ currentSortOption.label }}</span>
          <el-icon class="el-icon--right">
            <ArrowDown />
          </el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item
              v-for="option in sortOptions"
              :key="option.value"
              :command="option.value"
              :class="{ 'is-active': sortBy === option.value }"
            >
              <el-icon class="sort-option-icon">
                <component :is="option.icon" />
              </el-icon>
              <span class="sort-option-label">{{ option.label }}</span>
              <el-icon v-if="sortBy === option.value" class="check-icon">
                <Check />
              </el-icon>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ArrowDown, Check, Clock, Calendar, Timer, Coin, Wallet } from '@element-plus/icons-vue'
import { ORDER_STATUS_MAP } from '../../../utils/orderStatus'

/**
 * 图标映射
 */
const ICON_MAP = {
  Clock,
  Calendar,
  Timer,
  Coin,
  Wallet
}

/**
 * 订单筛选栏组件
 */
const props = defineProps({
  activeStatus: {
    type: String,
    default: 'all'
  },
  sortBy: {
    type: String,
    default: 'timeDesc'
  },
  statusList: {
    type: Array,
    default: () => []
  },
  sortOptions: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:activeStatus', 'update:sortBy'])

/**
 * 当前排序选项
 */
const currentSortOption = computed(() => {
  const option = props.sortOptions.find((opt) => opt.value === props.sortBy)
  return option || props.sortOptions[0] || { label: '排序', icon: 'Clock' }
})

/**
 * 获取状态标签
 */
function getStatusLabel(status) {
  return ORDER_STATUS_MAP[status] || status
}

/**
 * 处理状态点击
 */
function handleStatusClick(status) {
  emit('update:activeStatus', status)
}

/**
 * 处理排序变化
 */
function handleSortChange(value) {
  emit('update:sortBy', value)
}
</script>

<style scoped lang="less">
.order-filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  flex-wrap: wrap;
}

.filter-buttons {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-2);
  min-width: 0;
}

.filter-tag {
  appearance: none;
  min-height: 28px;
  padding: 0 var(--space-3);
  border: 1px solid var(--line);
  border-radius: var(--radius-full);
  background: var(--surface);
  color: var(--ink-700);
  font: inherit;
  font-size: var(--text-body-sm);
  line-height: 26px;
  cursor: pointer;
  transition:
    border-color var(--duration-fast) var(--ease-out),
    background-color var(--duration-fast) var(--ease-out),
    color var(--duration-fast) var(--ease-out);
}

.filter-tag:hover {
  border-color: var(--brand-300);
  background: var(--brand-50);
  color: var(--brand-600);
}

.filter-tag:focus-visible {
  outline: 2px solid var(--brand-500);
  outline-offset: 2px;
}

/* 选中态用页面主色，与「商家查找」的筛选控件同一套规则 */
.filter-tag.active {
  border-color: var(--brand-500);
  background: var(--brand-500);
  color: var(--surface);
}

.sort-selector {
  flex-shrink: 0;
}

.sort-dropdown-btn {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  min-height: 28px;
  padding: 0 var(--space-3);
  border: 1px solid var(--line);
  border-radius: var(--radius-full);
  background: var(--surface);
  color: var(--ink-700);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);

  &:hover,
  &:focus {
    border-color: var(--brand-300);
    background: var(--brand-50);
    color: var(--brand-600);
  }

  .sort-icon {
    font-size: var(--text-body);
    color: var(--brand-600);
  }

  .sort-label {
    font-size: var(--text-body-sm);
  }

  .el-icon {
    font-size: var(--text-caption);
  }
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-body-sm);

  &:hover {
    background: var(--brand-50);
  }

  .sort-option-icon {
    font-size: var(--text-body);
    color: var(--brand-600);
  }

  .sort-option-label {
    flex: 1;
  }

  .check-icon {
    color: var(--brand-600);
    font-size: var(--text-body);
    font-weight: var(--weight-semibold);
  }
}
</style>
