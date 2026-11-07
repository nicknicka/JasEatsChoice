<template>
  <el-dialog
    v-model="dialogVisible"
    width="450px"
    @close="handleClose"
    class="quick-add-dialog"
    :close-on-click-modal="false"
  >
    <template #header>
      <div class="dialog-header">
        <el-icon :size="24" color="#67c23a"><CirclePlus /></el-icon>
        <span class="header-title">添加商品</span>
      </div>
    </template>

    <div v-if="product" class="quick-add-content">
      <!-- 商品信息 -->
      <div class="product-info">
        <div v-if="product.image" class="product-image">
          <img :src="product.image" :alt="product.name" />
        </div>
        <div class="product-details">
          <h3 class="product-name">{{ product.name }}</h3>
          <p v-if="product.description" class="product-description">{{ product.description }}</p>
          <p class="product-price">
            <span class="price-symbol">¥</span>
            <span class="price-value">{{ product.price }}</span>
          </p>
        </div>
      </div>

      <el-divider />

      <!-- 数量选择 -->
      <div class="form-section">
        <label class="form-label">
          <el-icon><ShoppingCart /></el-icon>
          选择数量
        </label>
        <el-input-number
          v-model="formData.quantity"
          :min="1"
          :max="99"
          size="large"
          class="quantity-input"
        />
      </div>

      <!-- 备注输入 -->
      <div class="form-section">
        <label class="form-label">
          <el-icon><Edit /></el-icon>
          备注信息（可选）
        </label>
        <el-input
          v-model="formData.remark"
          type="textarea"
          :rows="3"
          placeholder="例如：不要香菜、微辣、多放葱等"
          maxlength="100"
          show-word-limit
        />
      </div>

      <!-- 金额统计 -->
      <div class="total-section">
        <div class="total-info">
          <span class="total-label">总计金额：</span>
          <span class="total-price">
            <span class="price-symbol">¥</span>
            <span class="price-value">{{ totalAmount.toFixed(2) }}</span>
          </span>
        </div>
        <div class="total-detail">
          ¥{{ product.price }} × {{ formData.quantity }}
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button size="large" @click="handleClose">
          取消
        </el-button>
        <el-button
          type="primary"
          size="large"
          @click="handleConfirm"
          class="confirm-button"
        >
          <el-icon><ShoppingCart /></el-icon>
          加入订单（¥{{ totalAmount.toFixed(2) }}）
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { CirclePlus, ShoppingCart, Edit } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  product: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'confirm'])

const dialogVisible = ref(props.modelValue)
const formData = ref({
  quantity: 1,
  remark: ''
})

// 计算总金额
const totalAmount = computed(() => {
  if (!props.product) return 0
  return (props.product.price || 0) * formData.value.quantity
})

watch(() => props.modelValue, (val) => {
  dialogVisible.value = val
})

watch(dialogVisible, (val) => {
  emit('update:modelValue', val)
  // 对话框打开时重置表单
  if (val) {
    formData.value = {
      quantity: 1,
      remark: ''
    }
  }
})

const handleClose = () => {
  dialogVisible.value = false
}

const handleConfirm = () => {
  if (!props.product) return

  emit('confirm', {
    product: props.product,
    quantity: formData.value.quantity,
    remark: formData.value.remark.trim(),
    totalAmount: totalAmount.value
  })

  dialogVisible.value = false
}
</script>

<style scoped lang="less">
.quick-add-dialog {
  .dialog-header {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 1.286rem /* 原值: 18px */;
    font-weight: 600;
    color: #303133;
  }

  .quick-add-content {
    padding: 10px 0;
  }

  .product-info {
    display: flex;
    gap: 16px;
    margin-bottom: 20px;

    .product-image {
      width: 100px;
      height: 100px;
      border-radius: 8px;
      overflow: hidden;
      flex-shrink: 0;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }

    .product-details {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: space-between;

      .product-name {
        font-size: 1.286rem /* 原值: 18px */;
        font-weight: 600;
        color: #303133;
        margin: 0 0 8px 0;
        line-height: 1.4;
      }

      .product-description {
        font-size: 0.929rem /* 原值: 13px */;
        color: #909399;
        margin: 0 0 8px 0;
        line-height: 1.5;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }

      .product-price {
        margin: 0;
        font-size: 1.714rem /* 原值: 24px */;
        font-weight: 700;
        color: #f56c6c;

        .price-symbol {
          font-size: 1.143rem /* 原值: 16px */;
          margin-right: 2px;
        }

        .price-value {
          font-size: 1.714rem /* 原值: 24px */;
        }
      }
    }
  }

  .form-section {
    margin-bottom: 20px;

    .form-label {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 1rem /* 原值: 14px */;
      font-weight: 600;
      color: #606266;
      margin-bottom: 10px;
    }

    .quantity-input {
      width: 100%;
    }
  }

  .total-section {
    background: linear-gradient(135deg, #fff7e6 0%, #ffe8cc 100%);
    border-radius: 8px;
    padding: 16px;
    margin-top: 20px;

    .total-info {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .total-label {
        font-size: 1.143rem /* 原值: 16px */;
        font-weight: 600;
        color: #606266;
      }

      .total-price {
        font-size: 1.714rem /* 原值: 24px */;
        font-weight: 700;
        color: #f56c6c;

        .price-symbol {
          font-size: 1.286rem /* 原值: 18px */;
        }

        .price-value {
          font-size: 1.714rem /* 原值: 24px */;
        }
      }
    }

    .total-detail {
      font-size: 0.929rem /* 原值: 13px */;
      color: #909399;
      text-align: right;
    }
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;

    .confirm-button {
      min-width: 180px;
    }
  }
}
</style>
