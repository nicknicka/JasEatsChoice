<script setup>
/**
 * 订单搜索栏组件
 */
import { ref, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'

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

const localKeyword = ref(props.searchKeyword)

// 监听外部变化
watch(() => props.searchKeyword, (newVal) => {
  localKeyword.value = newVal
})

// 监听本地变化并同步到外部
watch(localKeyword, (newVal) => {
  emit('update:searchKeyword', newVal)
})

// 清空搜索
function handleClear() {
  localKeyword.value = ''
  emit('clear')
}
</script>

<template>
  <div class="order-search-bar">
    <el-input
      v-model="localKeyword"
      placeholder="搜索订单号、商家名称..."
      clearable
      :prefix-icon="Search"
      class="search-input"
      @clear="handleClear"
    >
      <template #suffix>
        <span v-if="localKeyword" class="result-count">
          找到 {{ filteredCount }} 个结果
        </span>
      </template>
    </el-input>
  </div>
</template>

<style scoped lang="less">
.order-search-bar {
  margin-bottom: 16px;
  padding: 16px 20px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  .search-input {
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
      color: #2c5282;
      font-size: 14px;

      &::placeholder {
        color: #a0aec0;
      }
    }

    :deep(.el-input__prefix) {
      color: #6ba4ff;
    }
  }

  .result-count {
    display: inline-block;
    padding: 2px 8px;
    margin-left: 8px;
    font-size: 12px;
    color: #5c8eff;
    background: rgba(235, 244, 255, 0.6);
    border-radius: 4px;
  }
}
</style>
