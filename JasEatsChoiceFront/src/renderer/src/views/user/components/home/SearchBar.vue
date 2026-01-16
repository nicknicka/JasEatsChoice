<template>
  <div class="top-action-bar">
    <div class="search-section" role="search">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索菜品、教程..."
        clearable
        size="large"
        class="search-input"
        @keyup.enter="handleSearch"
        @clear="clearSearch"
        aria-label="搜索菜品和教程"
      >
        <template #append>
          <el-button :icon="Search" @click="handleSearch" aria-label="执行搜索"> 搜索 </el-button>
        </template>
      </el-input>
    </div>

    <!-- 刷新按钮 -->
    <el-button
      type="primary"
      :icon="Refresh"
      :loading="loading"
      @click="onRefresh"
      class="refresh-action-btn"
      circle
      size="large"
      title="刷新内容"
    />
  </div>
</template>

<script setup lang="ts">
import { Search, Refresh } from '@element-plus/icons-vue'

interface Props {
  searchKeyword: string
  loading?: boolean
}

interface Emits {
  (e: 'update:searchKeyword', value: string): void
  (e: 'search'): void
  (e: 'clear'): void
  (e: 'refresh'): void
}

defineProps<Props>()
const emit = defineEmits<Emits>()

const searchKeyword = defineModel<string>('searchKeyword')

const handleSearch = () => {
  emit('search')
}

const clearSearch = () => {
  emit('clear')
}

const onRefresh = () => {
  emit('refresh')
}
</script>

<style scoped lang="less">
.top-action-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 16px 20px;
  background: #ffffff;
  border-radius: 0 0 16px 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
  position: sticky;
  top: 0;
  z-index: 100;
  backdrop-filter: blur(10px);
  background-color: rgba(255, 255, 255, 0.95);

  .search-section {
    flex: 1;
    margin-bottom: 0;
    display: flex;
    align-items: center;

    .search-input {
      display: inline-flex;
      width: 100%;

      :deep(.el-input__wrapper) {
        border-radius: 24px 0 0 24px;
        border-right: none;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        padding-right: 0;
        background: rgba(255, 255, 255, 0.95);

        &:hover {
          box-shadow: 0 4px 16px rgba(255, 107, 107, 0.15);
          background: rgba(255, 255, 255, 1);
        }

        &.is-focus {
          box-shadow: 0 4px 24px rgba(255, 107, 107, 0.3);
          border-right: none;
          background: rgba(255, 255, 255, 1);
        }
      }

      :deep(.el-input-group__append) {
        border-radius: 0 24px 24px 0;
        background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
        border: none;
        border-left: none;
        padding: 0;
        margin: 0;
        margin-left: -1px;
        box-shadow: 0 2px 12px rgba(255, 107, 107, 0.3);
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        position: relative;
        z-index: 1;
        overflow: hidden;

        &::before {
          content: '';
          position: absolute;
          top: 50%;
          left: 50%;
          width: 0;
          height: 0;
          border-radius: 50%;
          background: rgba(255, 255, 255, 0.3);
          transform: translate(-50%, -50%);
          transition:
            width 0.6s,
            height 0.6s;
        }

        &:hover::before {
          width: 300px;
          height: 300px;
        }

        .el-button {
          background-color: transparent;
          border: none;
          color: #fff;
          font-weight: 600;
          padding: 16px 24px;
          height: 100%;
          border-radius: 0 24px 24px 0;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          box-shadow: none;
          margin: 0;
          position: relative;
          z-index: 1;

          &:hover {
            background-color: rgba(255, 255, 255, 0.15);
            transform: scale(1.02);
            box-shadow: none;
          }

          &:active {
            transform: scale(0.98);
          }
        }
      }

      :deep(.el-input-group__append),
      :deep(.el-input-group__prepend) {
        box-shadow: none;
      }
    }
  }

  .refresh-action-btn {
    flex-shrink: 0;
    width: 48px;
    height: 48px;
    background: linear-gradient(135deg, #6ba4ff 0%, #5c8eff 100%);
    border: none;
    box-shadow: 0 4px 12px rgba(92, 142, 255, 0.3);
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: -100%;
      width: 100%;
      height: 100%;
      background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
      transition: left 0.6s;
    }

    &:hover {
      transform: translateY(-2px) scale(1.08);
      box-shadow: 0 8px 20px rgba(92, 142, 255, 0.5);
      background: linear-gradient(135deg, #7ab4ff 0%, #6c9eff 100%);

      &::before {
        left: 100%;
      }

      .el-icon {
        transform: rotate(180deg);
      }
    }

    &:active {
      transform: translateY(0) scale(0.95);
      box-shadow: 0 2px 8px rgba(92, 142, 255, 0.3);
    }

    .el-icon {
      transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
    }

    &.is-loading {
      .el-icon {
        animation: loading-spin 1s linear infinite;
      }
    }
  }
}

@keyframes loading-spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>
