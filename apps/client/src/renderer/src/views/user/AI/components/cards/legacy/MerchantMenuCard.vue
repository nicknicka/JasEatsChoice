<template>
  <div class="merchant-menu-card" v-if="visible">
    <!-- 弹窗遮罩 -->
    <div class="modal-overlay" @click="close"></div>

    <!-- 弹窗内容 -->
    <div class="modal-content">
      <!-- 头部 -->
      <div class="modal-header">
        <div class="merchant-info">
          <img :src="merchant.image || '/default-merchant.png'" class="merchant-logo" />
          <div class="merchant-details">
            <h3>{{ merchant.name }}</h3>
            <div class="merchant-meta">
              <el-rate v-model="merchant.rating" disabled show-score text-color="#ff9900" />
              <span class="average-price">人均 ¥{{ merchant.averagePrice }}</span>
            </div>
          </div>
        </div>
        <button class="close-btn" @click="close">
          <el-icon><Close /></el-icon>
        </button>
      </div>

      <!-- 菜品列表 -->
      <div class="modal-body">
        <h4>选择菜品</h4>

        <!-- 默认选中区域（AI推荐） -->
        <div v-if="defaultSelection.length > 0" class="default-selection">
          <div class="section-title">
            <el-icon><MagicStick /></el-icon>
            <span>AI推荐菜品</span>
          </div>
          <div class="dish-list">
            <div
              v-for="dish in defaultSelection"
              :key="dish.id"
              class="dish-item recommended"
              :class="{ selected: isDishSelected(dish.id) }"
              @click="toggleDish(dish)"
            >
              <img :src="dish.image || '/default-dish.png'" class="dish-image" />
              <div class="dish-info">
                <div class="dish-name">{{ dish.name }}</div>
                <div class="dish-meta">
                  <span class="price">¥{{ dish.price }}</span>
                  <span class="calorie">{{ dish.calorie }}kcal</span>
                </div>
              </div>
              <div class="dish-actions">
                <el-input-number
                  v-model="dish.quantity"
                  :min="1"
                  :max="99"
                  size="small"
                  @click.stop
                />
              </div>
            </div>
          </div>
        </div>

        <!-- 所有菜品 -->
        <div class="all-dishes">
          <div class="section-title">
            <el-icon><Menu /></el-icon>
            <span>全部菜品</span>
          </div>
          <div class="dish-list">
            <div
              v-for="dish in dishes"
              :key="dish.id"
              class="dish-item"
              :class="{ selected: isDishSelected(dish.id) }"
              @click="toggleDish(dish)"
            >
              <img :src="dish.image || '/default-dish.png'" class="dish-image" />
              <div class="dish-info">
                <div class="dish-name">{{ dish.name }}</div>
                <div class="dish-description">{{ dish.description }}</div>
                <div class="dish-meta">
                  <span class="price">¥{{ dish.price }}</span>
                  <span class="calorie">{{ dish.calorie }}kcal</span>
                  <el-rate
                    v-if="dish.avgRating"
                    :model-value="dish.avgRating"
                    disabled
                    show-score
                    text-color="#ff9900"
                    size="small"
                  />
                </div>
              </div>
              <div class="dish-actions">
                <el-input-number
                  v-if="isDishSelected(dish.id)"
                  v-model="selectedDishes[dish.id].quantity"
                  :min="1"
                  :max="99"
                  size="small"
                  @click.stop
                  @change="updateQuantity(dish.id, $event)"
                />
                <el-checkbox
                  v-else
                  :model-value="false"
                  @change="toggleDish(dish)"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- 就餐方式选择 -->
        <div class="dining-mode-section">
          <div class="section-title">
            <el-icon><Utensils /></el-icon>
            <span>就餐方式</span>
          </div>
          <el-radio-group v-model="diningMode" class="dining-mode-options">
            <el-radio label="dine_in" border>
              <div class="option-content">
                <span class="option-icon">🍽️</span>
                <span>堂食</span>
              </div>
            </el-radio>
            <el-radio label="takeout" border>
              <div class="option-content">
                <span class="option-icon">🥡</span>
                <span>自取</span>
              </div>
            </el-radio>
          </el-radio-group>

          <!-- 堂食座号输入 -->
          <el-input
            v-if="diningMode === 'dine_in'"
            v-model="tableNumber"
            placeholder="请输入座号，如：A12"
            class="table-number-input"
          >
            <template #prefix>
              <el-icon><Location /></el-icon>
            </template>
          </el-input>
        </div>

        <!-- 备注 -->
        <div class="note-section">
          <el-input
            v-model="note"
            type="textarea"
            placeholder="备注：少辣、不要葱花等（可选）"
            :rows="2"
            maxlength="100"
            show-word-limit
          />
        </div>
      </div>

      <!-- 底部操作栏 -->
      <div class="modal-footer">
        <div class="order-summary">
          <div class="summary-item">
            <span>已选 {{ selectedDishCount }} 项</span>
          </div>
          <div class="summary-item">
            <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
          </div>
        </div>

        <div class="action-buttons">
          <el-button @click="close">取消</el-button>
          <el-button type="primary" @click="submitByAI" :disabled="selectedDishCount === 0">
            <el-icon><ChatDotRound /></el-icon>
            让AI下单
          </el-button>
          <el-button type="success" @click="submitManually" :disabled="selectedDishCount === 0">
            <el-icon><Check /></el-icon>
            手动提交
          </el-button>
        </div>
      </div>
    </div>

    <!-- 支付确认弹窗 -->
    <PaymentConfirmationDialog
      v-if="showPaymentDialog"
      :order-data="orderData"
      @confirm="handlePayment"
      @cancel="showPaymentDialog = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Close, MagicStick, Menu, Utensils, Location, ChatDotRound, Check } from '@element-plus/icons-vue'
import PaymentConfirmationDialog from './PaymentConfirmationDialog.vue'

const props = defineProps({
  cardData: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'submit'])

// 商家信息
const merchant = ref(props.cardData.merchant || {})

// 菜品列表
const dishes = ref(props.cardData.dishes || [])

// 默认选中的菜品（AI推荐）
const defaultSelection = ref(props.cardData.defaultSelection || [])

// 选中的菜品
const selectedDishes = ref({})

// 就餐方式
const diningMode = ref('dine_in')

// 座号
const tableNumber = ref('')

// 备注
const note = ref('')

// 是否显示支付弹窗
const showPaymentDialog = ref(false)

// 订单数据
const orderData = ref(null)

// 弹窗是否可见
const visible = computed(() => props.cardData?.cardType === 'MERCHANT_MENU_CARD')

// 初始化默认选中的菜品
watch(() => props.cardData, (newData) => {
  if (newData?.defaultSelection) {
    newData.defaultSelection.forEach(dish => {
      selectedDishes.value[dish.dishId] = {
        dishId: dish.dishId,
        name: dish.name,
        quantity: dish.quantity,
        price: dish.price
      }
    })
  }
}, { immediate: true })

// 计算选中的菜品数量
const selectedDishCount = computed(() => {
  return Object.keys(selectedDishes.value).length
})

// 计算总价
const totalPrice = computed(() => {
  let total = 0
  Object.values(selectedDishes.value).forEach(dish => {
    total += dish.price * dish.quantity
  })

  // 堂食无包装费，自取每项2元包装费
  if (diningMode.value === 'takeout') {
    total += selectedDishCount.value * 2
  }

  return total
})

// 判断菜品是否已选中
const isDishSelected = (dishId) => {
  return !!selectedDishes.value[dishId]
}

// 切换菜品选中状态
const toggleDish = (dish) => {
  const dishId = dish.id || dish.dishId

  if (selectedDishes.value[dishId]) {
    delete selectedDishes.value[dishId]
  } else {
    selectedDishes.value[dishId] = {
      dishId: dishId,
      name: dish.name,
      quantity: 1,
      price: dish.price
    }
  }
}

// 更新菜品数量
const updateQuantity = (dishId, quantity) => {
  if (selectedDishes.value[dishId]) {
    selectedDishes.value[dishId].quantity = quantity
  }
}

// 关闭弹窗
const close = () => {
  emit('close')
}

// 构建订单数据
const buildOrderData = () => {
  return {
    merchantId: merchant.value.id,
    diningMode: diningMode.value,
    tableNumber: diningMode.value === 'dine_in' ? tableNumber.value : null,
    note: note.value,
    dishItems: Object.values(selectedDishes.value).map(dish => ({
      dishId: dish.dishId,
      quantity: dish.quantity,
      price: dish.price
    })),
    totalAmount: totalPrice.value
  }
}

// 让AI下单
const submitByAI = () => {
  if (selectedDishCount.value === 0) {
    ElMessage.warning('请至少选择一道菜品')
    return
  }

  if (diningMode.value === 'dine_in' && !tableNumber.value) {
    ElMessage.warning('堂食时请填写座号')
    return
  }

  const data = buildOrderData()

  // 发送给AI处理
  emit('submit', {
    type: 'ai_submit',
    data: data
  })

  close()
}

// 手动提交订单
const submitManually = () => {
  if (selectedDishCount.value === 0) {
    ElMessage.warning('请至少选择一道菜品')
    return
  }

  if (diningMode.value === 'dine_in' && !tableNumber.value) {
    ElMessage.warning('堂食时请填写座号')
    return
  }

  orderData.value = buildOrderData()
  showPaymentDialog.value = true
}

// 处理支付确认
const handlePayment = (paymentMethod) => {
  console.log('支付方式：', paymentMethod)
  console.log('订单数据：', orderData.value)

  // 调用后端API创建订单
  // TODO: 实现支付流程

  showPaymentDialog.value = false
  emit('submit', {
    type: 'manual_submit',
    data: {
      ...orderData.value,
      paymentMethod: paymentMethod
    }
  })

  close()
}
</script>

<style scoped>
.merchant-menu-card {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
}

.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
}

.modal-content {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 90%;
  max-width: 800px;
  max-height: 85vh;
  background: white;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.merchant-info {
  display: flex;
  gap: 15px;
  flex: 1;
}

.merchant-logo {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
}

.merchant-details {
  flex: 1;
}

.merchant-details h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
}

.merchant-meta {
  display: flex;
  align-items: center;
  gap: 15px;
}

.average-price {
  color: #666;
  font-size: 14px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  padding: 0;
  color: #999;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

.modal-body h4 {
  margin: 0 0 15px 0;
  font-size: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #333;
}

.default-selection {
  margin-bottom: 20px;
}

.dish-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dish-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border: 1px solid #eee;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.dish-item:hover {
  border-color: #409eff;
  background: #f5f7fa;
}

.dish-item.selected {
  border-color: #409eff;
  background: #ecf5ff;
}

.dish-item.recommended {
  border-color: #67c23a;
  background: #f0f9ff;
}

.dish-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
}

.dish-info {
  flex: 1;
}

.dish-name {
  font-weight: 600;
  margin-bottom: 4px;
}

.dish-description {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.dish-meta {
  display: flex;
  gap: 12px;
  font-size: 14px;
}

.dish-meta .price {
  color: #f56c6c;
  font-weight: 600;
}

.dish-meta .calorie {
  color: #999;
}

.dish-actions {
  display: flex;
  align-items: center;
}

.all-dishes {
  margin-top: 20px;
}

.dining-mode-section {
  margin-top: 20px;
}

.dining-mode-options {
  display: flex;
  gap: 15px;
  margin-bottom: 12px;
}

.option-content {
  display: flex;
  align-items: center;
  gap: 6px;
}

.option-icon {
  font-size: 20px;
}

.table-number-input {
  width: 200px;
}

.note-section {
  margin-top: 15px;
}

.modal-footer {
  padding: 20px;
  border-top: 1px solid #eee;
}

.order-summary {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.summary-item {
  font-weight: 600;
}

.total-price {
  font-size: 20px;
  color: #f56c6c;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.action-buttons .el-button {
  flex: 1;
}
</style>
