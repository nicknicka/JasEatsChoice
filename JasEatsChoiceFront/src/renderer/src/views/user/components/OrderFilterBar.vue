<template>
  <div class="order-filter-bar">
    <div class="filter-buttons">
      <el-button
        v-for="status in statusList"
        :key="status.value"
        type="primary"
        :plain="activeStatus !== status.value"
        size="small"
        @click="handleStatusClick(status.value)"
      >
        {{ status.label }}
      </el-button>
    </div>

    <!-- 排序选择器 -->
    <div class="sort-selector">
      <el-dropdown trigger="click" @command="handleSortChange">
        <el-button type="default" size="small" class="sort-dropdown-btn">
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
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  padding: 12px 16px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.06);
  flex-wrap: wrap;

  .filter-buttons {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
    flex: 1;
  }

  :deep(.el-button) {
    border-radius: 20px;
    border-color: rgba(179, 212, 252, 0.4);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

    &.el-button--primary {
      background: linear-gradient(135deg, #6ba4ff 0%, #5c8eff 100%);
      border-color: transparent;
      box-shadow: 0 2px 8px rgba(92, 142, 255, 0.3);

      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(92, 142, 255, 0.4);
      }
    }

    &.is-plain {
      background: #ffffff;
      color: #5c8eff;
      border-color: #d9d9d9;

      &:hover {
        background: #f0f9ff;
        border-color: #6ba4ff;
        color: #4c7eff;
      }
    }
  }

  // 排序选择器样式
  .sort-selector {
    flex-shrink: 0;

    .sort-dropdown-btn {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 8px 16px;
      border-radius: 20px;
      background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
      border: 1px solid #dee2e6;
      color: #495057;
      font-weight: 500;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);

      &:hover {
        background: linear-gradient(135deg, #e9ecef 0%, #dee2e6 100%);
        border-color: #adb5bd;
        transform: translateY(-1px);
        box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
      }

      .sort-icon {
        font-size: 1.143rem /* 原值: 16px */;
        color: #5c8eff;
      }

      .sort-label {
        font-size: 0.929rem /* 原值: 13px */;
      }

      .el-icon {
        font-size: 0.857rem /* 原值: 12px */;
        transition: transform 0.3s ease;
      }
    }

    :deep(.el-dropdown-menu__item) {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 10px 16px;
      border-radius: 8px;
      margin: 4px 8px;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

      &.is-active {
        background: linear-gradient(135deg, #e7f5ff 0%, #d0ebff 100%);
        color: #1971c2;
        font-weight: 600;

        .sort-option-icon {
          transform: scale(1.1);
        }
      }

      &:hover {
        background: #f8f9fa;
        transform: translateX(2px);
      }

      .sort-option-icon {
        font-size: 1.143rem /* 原值: 16px */;
        color: #5c8eff;
        transition: transform 0.3s ease;
      }

      .sort-option-label {
        flex: 1;
        font-size: 0.929rem /* 原值: 13px */;
      }

      .check-icon {
        color: #1971c2;
        font-size: 1rem /* 原值: 14px */;
        font-weight: bold;
      }
    }
  }
}

@media (max-width: 768px) {
  .order-filter-bar {
    padding: 10px 12px;
    border-radius: 10px;
    gap: 8px;

    .filter-buttons {
      gap: 6px;

      :deep(.el-button) {
        font-size: 0.857rem /* 原值: 12px */;
        padding: 6px 12px;
      }
    }

    .sort-selector {
      .sort-dropdown-btn {
        padding: 6px 12px;

        .sort-label {
          font-size: 0.857rem /* 原值: 12px */;
        }
      }
    }
  }
}
</style>
