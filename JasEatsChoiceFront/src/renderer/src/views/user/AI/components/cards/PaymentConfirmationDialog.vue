<template>
  <el-dialog
    v-model="visible"
    title="确认支付"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <!-- 订单信息 -->
    <div class="order-info">
      <h4>订单详情</h4>
      <div class="info-section">
        <div class="info-row">
          <span class="label">商家：</span>
          <span class="value">XX餐厅</span>
        </div>
        <div class="info-row">
          <span class="label">就餐方式：</span>
          <span class="value">{{ diningModeText }}</span>
        </div>
        <div v-if="orderData.tableNumber" class="info-row">
          <span class="label">座号：</span>
          <span class="value">{{ orderData.tableNumber }}</span>
        </div>
      </div>

      <!-- 菜品列表 -->
      <h4>菜品明细</h4>
      <div class="dishes-section">
        <div v-for="item in orderData.dishItems" :key="item.dishId" class="dish-item">
          <span class="dish-name">{{ getDishName(item.dishId) }}</span>
          <span class="dish-detail">× {{ item.quantity }}</span>
          <span class="dish-price">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
        </div>
      </div>

      <!-- 费用明细 -->
      <div class="fee-section">
        <div class="fee-row">
          <span>菜品小计</span>
          <span>¥{{ dishTotal.toFixed(2) }}</span>
        </div>
        <div v-if="orderData.diningMode === 'takeout'" class="fee-row">
          <span>包装费</span>
          <span>¥{{ packagingFee.toFixed(2) }}</span>
        </div>
        <div class="fee-row total">
          <span>应付总额</span>
          <span class="total-price">¥{{ orderData.totalAmount.toFixed(2) }}</span>
        </div>
      </div>

      <!-- 备注 -->
      <div v-if="orderData.note" class="note-section">
        <span class="label">备注：</span>
        <span class="value">{{ orderData.note }}</span>
      </div>
    </div>

    <!-- 支付方式选择 -->
    <div class="payment-methods">
      <h4>选择支付方式</h4>
      <el-radio-group v-model="selectedPayment" class="payment-options">
        <el-radio label="wechat" border class="payment-option">
          <div class="option-content">
            <span class="payment-icon">💚</span>
            <div class="payment-info">
              <span class="payment-name">微信支付</span>
              <span class="payment-desc">推荐使用</span>
            </div>
          </div>
        </el-radio>
        <el-radio label="alipay" border class="payment-option">
          <div class="option-content">
            <span class="payment-icon">💙</span>
            <div class="payment-info">
              <span class="payment-name">支付宝</span>
              <span class="payment-desc">快捷支付</span>
            </div>
          </div>
        </el-radio>
        <el-radio label="balance" border class="payment-option">
          <div class="option-content">
            <span class="payment-icon">💰</span>
            <div class="payment-info">
              <span class="payment-name">余额支付</span>
              <span class="payment-desc">账户余额：¥{{ balance.toFixed(2) }}</span>
            </div>
          </div>
        </el-radio>
      </el-radio-group>
    </div>

    <!-- 倒计时 -->
    <div class="countdown">
      <el-icon><Timer /></el-icon>
      <span>请在 {{ formatTime(countdown) }} 内完成支付，超时订单将自动取消</span>
    </div>

    <!-- 操作按钮 -->
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          type="primary"
          size="large"
          :disabled="!selectedPayment"
          @click="handleConfirm"
        >
          <span v-if="countdown > 0">确认支付 ¥{{ orderData.totalAmount.toFixed(2) }}</span>
          <span v-else>已超时</span>
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Timer } from '@element-plus/icons-vue'

const props = defineProps({
  orderData: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['confirm', 'cancel'])

// 是否显示弹窗
const visible = computed(() => !!props.orderData)

// 选中的支付方式
const selectedPayment = ref('wechat')

// 账户余额（示例）
const balance = ref(100.00)

// 倒计时（15分钟）
const countdown = ref(900) // 15分钟 = 900秒

let timer = null

// 就餐方式文本
const diningModeText = computed(() => {
  return props.orderData?.diningMode === 'dine_in' ? '🍽️ 堂食' : '🥡 自取'
})

// 菜品小计
const dishTotal = computed(() => {
  if (!props.orderData?.dishItems) return 0
  return props.orderData.dishItems.reduce((sum, item) => {
    return sum + (item.price * item.quantity)
  }, 0)
})

// 包装费
const packagingFee = computed(() => {
  if (props.orderData?.diningMode !== 'takeout') return 0
  return props.orderData.dishItems.length * 2
})

// 获取菜品名称（示例）
const getDishName = (dishId) => {
  // TODO: 从dishItems中获取菜品名称
  return '菜品' + dishId.slice(-4)
}

// 格式化时间
const formatTime = (seconds) => {
  const minutes = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${minutes}:${secs.toString().padStart(2, '0')}`
}

// 确认支付
const handleConfirm = () => {
  if (countdown.value <= 0) {
    return
  }

  emit('confirm', {
    paymentMethod: selectedPayment.value,
    orderData: props.orderData
  })

  handleClose()
}

// 关闭弹窗
const handleClose = () => {
  emit('cancel')
}

// 启动倒计时
const startCountdown = () => {
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

// 组件挂载时启动倒计时
onMounted(() => {
  startCountdown()
})

// 组件卸载时清除定时器
onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.order-info {
  margin-bottom: 20px;
}

.order-info h4 {
  margin: 0 0 12px 0;
  font-size: 15px;
  font-weight: 600;
}

.info-section,
.dishes-section,
.fee-section,
.note-section {
  margin-bottom: 15px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.label {
  color: #666;
}

.value {
  font-weight: 500;
}

.dish-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.dish-item:last-child {
  margin-bottom: 0;
}

.dish-name {
  flex: 1;
}

.dish-detail {
  color: #666;
  margin-right: 20px;
}

.dish-price {
  font-weight: 600;
  color: #f56c6c;
}

.fee-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.fee-row.total {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ddd;
  font-weight: 600;
  font-size: 16px;
}

.total-price {
  color: #f56c6c;
  font-size: 18px;
}

.note-section {
  display: flex;
  gap: 8px;
}

.payment-methods h4 {
  margin: 0 0 12px 0;
  font-size: 15px;
  font-weight: 600;
}

.payment-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.payment-option {
  margin: 0;
}

.option-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.payment-icon {
  font-size: 32px;
}

.payment-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.payment-name {
  font-weight: 600;
  font-size: 15px;
}

.payment-desc {
  font-size: 12px;
  color: #999;
}

.countdown {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #fff3cd;
  border: 1px solid #ffc107;
  border-radius: 8px;
  margin-bottom: 15px;
  color: #856404;
  font-size: 14px;
}

.dialog-footer {
  display: flex;
  gap: 10px;
}

.dialog-footer .el-button {
  flex: 1;
}
</style>
