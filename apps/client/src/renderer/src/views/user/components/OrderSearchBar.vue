<template>
  <div class="order-search-bar">
    <el-input
      :model-value="searchKeyword"
      placeholder="搜索订单号、商家名称、菜品名称、金额..."
      clearable
      size="default"
      class="search-input"
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
      <el-button type="primary" link size="small" class="clear-search-btn" @click="handleClear">
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
  margin-bottom: 16px;
  padding: 16px 20px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.06);

  .search-input {
    :deep(.el-input__wrapper) {
      border-radius: 24px;
      padding: 8px 16px;
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
      border: 1px solid rgba(179, 212, 252, 0.3);
      background: linear-gradient(135deg, #f8faff 0%, #ffffff 100%);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

      &:hover {
        box-shadow: 0 4px 12px rgba(92, 142, 255, 0.15);
        border-color: rgba(92, 142, 255, 0.4);
      }

      &.is-focus {
        box-shadow: 0 4px 16px rgba(92, 142, 255, 0.25);
        border-color: #6ba4ff;
        background: #ffffff;
      }
    }

    :deep(.el-input__inner) {
      font-size: 1rem /* 原值: 14px */;
      color: #2c5282;
      font-weight: 400;

      &::placeholder {
        color: #94a3b8;
        font-weight: 300;
      }
    }

    .search-icon {
      color: #5c8eff;
      font-size: 1.143rem /* 原值: 16px */;
      animation: search-glow 2s ease-in-out infinite;
    }

    @keyframes search-glow {
      0%,
      100% {
        opacity: 1;
        transform: scale(1);
      }
      50% {
        opacity: 0.7;
        transform: scale(1.1);
      }
    }

    .clear-icon {
      color: #94a3b8;
      font-size: 1.143rem /* 原值: 16px */;
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        color: #ff6b6b;
        transform: rotate(90deg) scale(1.1);
      }
    }
  }

  .search-result-info {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 12px;
    padding: 8px 12px;
    background: linear-gradient(135deg, #e7f5ff 0%, #f0f9ff 100%);
    border-radius: 8px;
    font-size: 0.929rem /* 原值: 13px */;
    color: #1971c2;
    border: 1px solid rgba(92, 142, 255, 0.2);

    .result-count {
      font-weight: 700;
      font-size: 1.143rem /* 原值: 16px */;
      color: #5c8eff;
      padding: 0 4px;
    }

    .clear-search-btn {
      margin-left: auto;
      font-size: 0.929rem /* 原值: 13px */;
      font-weight: 500;
      color: #5c8eff;
      transition: all 0.3s ease;

      &:hover {
        color: #4c7eff;
        transform: translateX(2px);
      }
    }
  }
}

@media (max-width: 768px) {
  .order-search-bar {
    padding: 12px 16px;
    border-radius: 10px;
  }
}
</style>
