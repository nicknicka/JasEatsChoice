<template>
  <el-dialog v-model="visible" title="选择商家" width="600px" @close="handleClose">
    <div class="merchant-list">
      <div
        v-for="merchant in merchants"
        :key="merchant.id"
        class="merchant-item"
        :class="{ disabled: merchantId && merchant.id !== merchantId }"
        @click="handleSelectMerchant(merchant)"
      >
        <div class="merchant-avatar">{{ merchant.avatar }}</div>
        <div class="merchant-info">
          <h3 class="merchant-name">{{ merchant.name }}</h3>
          <p class="merchant-type">{{ merchant.type }}</p>
          <div v-if="merchantId && merchant.id === merchantId" class="selected-badge">
            当前选择
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'

/**
 * 商家选择对话框组件
 * @description 用于群订单中选择下单商家
 */
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  merchants: {
    type: Array,
    default: () => []
  },
  currentMerchantId: {
    type: [Number, String],
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'select'])

const visible = ref(props.modelValue)
const merchantId = computed(() => props.currentMerchantId)

/**
 * 处理对话框关闭
 */
const handleClose = () => {
  visible.value = false
  emit('update:modelValue', false)
}

/**
 * 选择商家
 */
const handleSelectMerchant = (merchant) => {
  emit('select', merchant)
  handleClose()
}

/**
 * 监听外部 modelValue 变化
 */
watch(() => props.modelValue, (newVal) => {
  visible.value = newVal
})

/**
 * 监听内部 visible 变化
 */
watch(visible, (newVal) => {
  if (!newVal) {
    emit('update:modelValue', false)
  }
})
</script>

<style scoped lang="less">
.merchant-list {
  max-height: 400px;
  overflow-y: auto;

  .merchant-item {
    display: flex;
    align-items: center;
    padding: 16px;
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    margin-bottom: 12px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      background-color: #f5f7fa;
      border-color: #409eff;
    }

    &.disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    .merchant-avatar {
      width: 50px;
      height: 50px;
      font-size: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16px;
      background-color: #f0f0f0;
      border-radius: 8px;
    }

    .merchant-info {
      flex: 1;
      position: relative;

      .merchant-name {
        font-size: 16px;
        font-weight: 600;
        margin: 0 0 4px 0;
        color: #303133;
      }

      .merchant-type {
        font-size: 14px;
        color: #909399;
        margin: 0;
      }

      .selected-badge {
        position: absolute;
        top: 0;
        right: 0;
        background-color: #67c23a;
        color: white;
        padding: 2px 8px;
        border-radius: 4px;
        font-size: 12px;
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>
