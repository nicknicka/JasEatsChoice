<template>
  <div class="order-search-bar">
    <el-input
      :model-value="searchKeyword"
      placeholder="搜索订单号、商家名称、菜品名称、金额..."
      clearable
      size="default"
      class="search-input"
      aria-label="搜索订单"
      @input="handleSearchInput"
      @clear="handleClear"
    >
      <template #prefix>
        <el-icon class="search-icon">
          <Search />
        </el-icon>
      </template>
      <template #suffix>
        <el-icon v-if="searchKeyword" class="clear-icon" @click="handleClear">
          <Delete />
        </el-icon>
      </template>
    </el-input>

    <div v-if="searchKeyword" class="search-result-info">
      找到 <span class="result-count">{{ filteredCount }}</span> 个相关订单
      <el-button class="clear-search-btn" @click="handleClear">
        清除搜索
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { Search, Delete } from '@element-plus/icons-vue'

/**
 * 订单搜索栏组件
 */
const props = defineProps({
  searchKeyword: {
    type: String,
    default: ''
  },
  filteredCount: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['update:searchKeyword', 'clear'])

/**
 * 处理搜索输入（使用防抖）
 */
let debounceTimer = null
function handleSearchInput(value) {
  if (debounceTimer) {
    clearTimeout(debounceTimer)
  }

  debounceTimer = setTimeout(() => {
    emit('update:searchKeyword', value)
  }, 300)
}

/**
 * 处理清除
 */
function handleClear() {
  emit('clear')
}
</script>

<style scoped lang="less">
.order-search-bar {
  margin-bottom: var(--space-3);

  .search-input {
    :deep(.el-input__wrapper) {
      min-height: 36px;
      padding: 0 var(--space-3);
      border: 1px solid var(--line);
      border-radius: var(--radius-md);
      background: var(--surface);
      box-shadow: none;
      transition:
        border-color var(--duration-fast) var(--ease-out),
        outline-color var(--duration-fast) var(--ease-out);

      &:hover {
        border-color: var(--line-strong);
      }

      &.is-focus {
        border-color: var(--brand-500);
        outline: 2px solid var(--brand-200);
        outline-offset: 1px;
      }
    }

    :deep(.el-input__inner) {
      font-size: var(--text-body);
      color: var(--ink-900);

      &::placeholder {
        color: var(--ink-400);
      }
    }

    .search-icon {
      color: var(--ink-400);
      font-size: var(--text-body);
    }

    .clear-icon {
      color: var(--ink-400);
      font-size: var(--text-body);
      cursor: pointer;
      transition: color var(--duration-fast) var(--ease-out);

      &:hover {
        color: var(--brand-600);
      }
    }
  }

  .search-result-info {
    display: flex;
    align-items: center;
    gap: var(--space-2);
    margin-top: var(--space-3);
    padding: var(--space-2) var(--space-3);
    background: var(--canvas-sunken);
    border-radius: var(--radius-sm);
    font-size: var(--text-body-sm);
    color: var(--ink-700);

    .result-count {
      font-family: var(--font-num);
      font-variant-numeric: tabular-nums;
      font-weight: var(--weight-semibold);
      color: var(--ink-900);
    }

    .clear-search-btn {
      margin-left: auto;
      border-color: var(--line-strong);
      border-radius: var(--radius-md);
      font-size: var(--text-body-sm);
      color: var(--brand-600);

      &:hover,
      &:focus {
        border-color: var(--brand-300);
        background: var(--brand-50);
        color: var(--brand-600);
      }
    }
  }
}
</style>
