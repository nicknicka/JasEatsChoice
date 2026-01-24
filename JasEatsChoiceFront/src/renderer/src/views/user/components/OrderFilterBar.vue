<script setup>
/**
 * 订单筛选栏组件
 */
import { computed } from 'vue'

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

// 当前状态标签
const currentStatusLabel = computed(() => {
  const status = props.statusList.find(s => s.value === props.activeStatus)
  return status ? status.label : '全部订单'
})

// 当前排序标签
const currentSortLabel = computed(() => {
  const sort = props.sortOptions.find(o => o.value === props.sortBy)
  return sort ? sort.label : '时间排序'
})
</script>

<template>
  <div class="order-filter-bar">
    <!-- 状态筛选 -->
    <div class="filter-section">
      <div class="filter-label">订单状态</div>
      <el-radio-group :model-value="activeStatus" @change="emit('update:activeStatus', $event)">
        <el-radio-button
          v-for="status in statusList"
          :key="status.value"
          :label="status.value"
        >
          {{ status.label }}
        </el-radio-button>
      </el-radio-group>
    </div>

    <!-- 排序方式 -->
    <div class="filter-section">
      <div class="filter-label">排序方式</div>
      <el-select
        :model-value="sortBy"
        @change="emit('update:sortBy', $event)"
        class="sort-select"
      >
        <el-option
          v-for="option in sortOptions"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
    </div>
  </div>
</template>

<style scoped lang="less">
.order-filter-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  padding: 16px 20px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  flex-wrap: wrap;

  .filter-section {
    display: flex;
    align-items: center;
    gap: 12px;

    .filter-label {
      font-size: 14px;
      font-weight: 500;
      color: #2c5282;
      white-space: nowrap;
    }
  }

  // 状态筛选按钮组样式
  :deep(.el-radio-group) {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;

    .el-radio-button {
      .el-radio-button__inner {
        border-radius: 8px;
        border: 1px solid rgba(179, 212, 252, 0.3);
        background: rgba(235, 244, 255, 0.3);
        color: #4a5568;
        font-size: 13px;
        padding: 8px 16px;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        box-shadow: none;

        &:hover {
          border-color: #6ba4ff;
          background: rgba(235, 244, 255, 0.6);
          color: #5c8eff;
        }
      }

      &.is-active {
        .el-radio-button__inner {
          background: linear-gradient(135deg, #6ba4ff 0%, #5c8eff 100%);
          border-color: #5c8eff;
          color: white;
          box-shadow: 0 2px 8px rgba(92, 142, 255, 0.3);
        }
      }
    }
  }

  // 排序下拉框样式
  .sort-select {
    width: 140px;

    :deep(.el-input__wrapper) {
      border-radius: 8px;
      border: 1px solid rgba(179, 212, 252, 0.3);
      background: rgba(235, 244, 255, 0.3);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

      &:hover {
        border-color: #6ba4ff;
        background: rgba(235, 244, 255, 0.5);
      }

      &.is-focus {
        border-color: #5c8eff;
        background: #ffffff;
        box-shadow: 0 0 0 3px rgba(92, 142, 255, 0.1);
      }
    }

    :deep(.el-input__inner) {
      font-size: 13px;
      color: #2c5282;
    }
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .order-filter-bar {
    padding: 14px 16px;
    gap: 12px;

    .filter-section {
      width: 100%;

      .filter-label {
        font-size: 13px;
      }
    }

    :deep(.el-radio-group) {
      width: 100%;

      .el-radio-button {
        flex: 1;

        .el-radio-button__inner {
          padding: 6px 12px;
          font-size: 12px;
        }
      }
    }

    .sort-select {
      width: 100%;
    }
  }
}
</style>
