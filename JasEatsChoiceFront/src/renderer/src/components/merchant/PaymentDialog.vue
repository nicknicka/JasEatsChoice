<template>
  <el-dialog
    v-model="dialogVisible"
    title="💳 选择支付方式"
    :width="dialogWidth"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    class="payment-dialog"
    @close="handleClose"
  >
    <div class="dialog-content">
      <!-- 订单摘要 -->
      <div class="order-summary">
        <div class="summary-item">
          <span class="label">订单号</span>
          <span class="value">{{ orderId }}</span>
        </div>
        <div class="summary-item">
          <span class="label">商家</span>
          <span class="value">{{ merchantName }}</span>
        </div>
        <div class="summary-item total">
          <span class="label">应付金额</span>
          <span class="value amount">¥{{ totalAmount.toFixed(2) }}</span>
        </div>
      </div>

      <!-- 支付方式选择 -->
      <div class="payment-methods">
        <div
          v-for="method in paymentMethods"
          :key="method.value"
          class="payment-method"
          :class="{ 'is-selected': selectedMethod === method.value }"
          @click="selectPaymentMethod(method.value)"
        >
          <div class="method-icon">{{ method.icon }}</div>
          <div class="method-info">
            <div class="method-name">{{ method.name }}</div>
            <div v-if="method.description" class="method-description">{{ method.description }}</div>
          </div>
          <div class="method-radio">
            <el-radio v-model="selectedMethod" :label="method.value">
              <span></span>
            </el-radio>
          </div>
        </div>
      </div>

      <!-- 余额信息 -->
      <div v-if="selectedMethod === 'balance' && userBalance !== null" class="balance-info">
        <el-alert type="info" :closable="false">
          <template #title>
            <span class="balance-text">
              账户余额：¥{{ userBalance.toFixed(2) }}
              <span v-if="userBalance < totalAmount" class="insufficient">
                （余额不足，请充值或选择其他支付方式）
              </span>
            </span>
          </template>
        </el-alert>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose" :disabled="isProcessing">取消</el-button>
        <el-button
          type="primary"
          @click="handleConfirmPayment"
          :loading="isProcessing"
          :disabled="!selectedMethod || (selectedMethod === 'balance' && userBalance < totalAmount)"
        >
          <el-icon v-if="!isProcessing"><Check /></el-icon>
          确认支付 ¥{{ totalAmount.toFixed(2) }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import axios from 'axios'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  orderId: {
    type: String,
    required: true
  },
  merchantName: {
    type: String,
    default: ''
  },
  totalAmount: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'success', 'close', 'insufficient-balance'])

const dialogVisible = ref(props.modelValue)
const selectedMethod = ref('wechat')
const isProcessing = ref(false)
const userBalance = ref(null)

// 支付方式列表
const paymentMethods = [
  {
    value: 'wechat',
    name: '微信支付',
    icon: '💚',
    description: '使用微信扫码支付'
  },
  {
    value: 'alipay',
    name: '支付宝',
    icon: '💙',
    description: '使用支付宝扫码支付'
  },
  {
    value: 'balance',
    name: '余额支付',
    icon: '💰',
    description: '使用账户余额支付'
  }
]

watch(() => props.modelValue, (val) => {
  dialogVisible.value = val
  if (val) {
    loadUserBalance()
  }
})

watch(dialogVisible, (val) => {
  emit('update:modelValue', val)
})

// 弹窗宽度（响应式）
const dialogWidth = computed(() => {
  return window.innerWidth < 768 ? '95%' : '480px'
})

// 加载用户余额
const loadUserBalance = async () => {
  try {
    // TODO: 替换为实际的API接口
    // const response = await axios.get('/api/user/balance')
    // userBalance.value = response.data.balance
    userBalance.value = 100 // 示例数据
  } catch (error) {
    console.error('加载余额失败:', error)
  }
}

// 选择支付方式
const selectPaymentMethod = (method) => {
  selectedMethod.value = method
}

// 处理关闭
const handleClose = () => {
  if (!isProcessing.value) {
    dialogVisible.value = false
    emit('close')
  }
}

// 确认支付
const handleConfirmPayment = async () => {
  if (!selectedMethod.value) {
    ElMessage.warning('请选择支付方式')
    return
  }

  // 检查余额是否充足
  if (selectedMethod.value === 'balance' && userBalance.value < props.totalAmount) {
    emit('insufficient-balance', {
      balance: userBalance.value,
      required: props.totalAmount
    })
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认使用${getPaymentMethodName(selectedMethod.value)}支付 ¥${props.totalAmount.toFixed(2)}？`,
      '确认支付',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    isProcessing.value = true

    // TODO: 调用实际支付接口
    const response = await axios.post('/api/payment/pay', {
      orderId: props.orderId,
      paymentMethod: selectedMethod.value
    })

    if (response.data.success) {
      ElMessage.success('支付成功！')
      emit('success', {
        orderId: props.orderId,
        paymentMethod: selectedMethod.value,
        amount: props.totalAmount
      })
      dialogVisible.value = false
    } else {
      throw new Error(response.data.message || '支付失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('支付失败:', error)
      ElMessage.error(error.message || '支付失败，请重试')
    }
  } finally {
    isProcessing.value = false
  }
}

// 获取支付方式名称
const getPaymentMethodName = (method) => {
  const methodInfo = paymentMethods.find(m => m.value === method)
  return methodInfo?.name || method
}
</script>

<style lang="scss" scoped>
.payment-dialog {
  :deep(.el-dialog__body) {
    padding: 20px;
  }
}

.dialog-content {
  .order-summary {
    margin-bottom: 24px;
    padding: 16px;
    background: #f8f9fa;
    border-radius: 8px;

    .summary-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      font-size: 14px;

      &:last-child {
        margin-bottom: 0;
      }

      &.total {
        padding-top: 12px;
        border-top: 1px solid #dee2e6;
        font-weight: 600;
        font-size: 16px;

        .amount {
          font-size: 24px;
          color: #f56c6c;
          font-weight: 700;
        }
      }

      .label {
        color: #666;
      }

      .value {
        color: #333;
        font-weight: 500;
      }
    }
  }

  .payment-methods {
    margin-bottom: 16px;

    .payment-method {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 16px;
      background: #fff;
      border: 2px solid #e9ecef;
      border-radius: 8px;
      margin-bottom: 12px;
      cursor: pointer;
      transition: all 0.3s;

      &:last-child {
        margin-bottom: 0;
      }

      &:hover {
        border-color: #667eea;
        background: #f8f9fa;
      }

      &.is-selected {
        border-color: #667eea;
        background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
      }

      .method-icon {
        width: 48px;
        height: 48px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 28px;
        background: #f8f9fa;
        border-radius: 8px;
        flex-shrink: 0;
      }

      .method-info {
        flex: 1;

        .method-name {
          font-size: 16px;
          font-weight: 600;
          color: #333;
          margin-bottom: 4px;
        }

        .method-description {
          font-size: 13px;
          color: #666;
        }
      }

      .method-radio {
        flex-shrink: 0;

        :deep(.el-radio) {
          margin: 0;

          .el-radio__label {
            display: none;
          }
        }
      }
    }
  }

  .balance-info {
    margin-bottom: 16px;

    .balance-text {
      font-size: 14px;

      .insufficient {
        color: #f56c6c;
        font-weight: 600;
      }
    }
  }
}

.dialog-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;

  .el-button {
    :deep(.el-icon) {
      margin-right: 4px;
    }
  }
}

// 响应式
@media (max-width: 768px) {
  .payment-dialog {
    :deep(.el-dialog) {
      width: 95% !important;
      margin: 0 auto;
    }

    :deep(.el-dialog__body) {
      padding: 12px;
    }
  }

  .dialog-content {
    .payment-methods {
      .payment-method {
        padding: 12px;

        .method-icon {
          width: 40px;
          height: 40px;
          font-size: 24px;
        }

        .method-info {
          .method-name {
            font-size: 15px;
          }

          .method-description {
            font-size: 12px;
          }
        }
      }
    }
  }
}
</style>
