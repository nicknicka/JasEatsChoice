<template>
  <el-dialog
    v-model="dialogVisible"
    title="🛒 选择菜品"
    :width="dialogWidth"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    class="dish-selector-dialog"
    @close="handleClose"
  >
    <div class="dialog-content">
      <!-- 商家摘要 -->
      <div class="merchant-summary">
        <h3>{{ cardData.merchant?.name }}</h3>
        <div class="summary-info">
          <el-tag :type="cardData.diningMode === 'takeout' ? 'warning' : 'success'" size="small">
            {{ cardData.diningMode === 'takeout' ? '🥡 自取' : '🍽️ 堂食' }}
          </el-tag>
          <span class="address">{{ cardData.merchant?.address }}</span>
        </div>
        <div v-if="cardData.recommendationReason" class="recommendation">
          💡 {{ cardData.recommendationReason }}
        </div>
      </div>

      <!-- AI预选菜品 -->
      <div class="section-title">🍽️ AI为您预选的菜品</div>
      <div class="dishes-list">
        <div
          v-for="dish in cardData.preSelectedDishes"
          :key="dish.dishId"
          class="dish-item"
          :class="{ 'zero-quantity': getDishQuantity(dish.dishId) === 0 }"
        >
          <div class="dish-image">
            <img v-if="dish.imageUrl" :src="dish.imageUrl" :alt="dish.dishName" />
            <div v-else class="placeholder-image">{{ dish.dishName?.charAt(0) }}</div>
          </div>
          <div class="dish-info">
            <h4 class="dish-name">{{ dish.dishName }}</h4>
            <div class="dish-meta">
              <el-tag size="small" type="info">{{ dish.category }}</el-tag>
              <span class="calories">{{ dish.calories }} kcal</span>
            </div>
            <div v-if="dish.reason" class="dish-recommendation">
              <el-icon><InfoFilled /></el-icon>
              {{ dish.reason }}
            </div>
            <div class="dish-price">¥{{ dish.price?.toFixed(2) }}</div>
          </div>
          <div class="dish-quantity">
            <el-input-number
              v-model="dishQuantities[dish.dishId]"
              :min="0"
              :max="10"
              size="small"
              @change="handleQuantityChange"
            />
          </div>
        </div>
      </div>

      <!-- 添加其他菜品按钮 -->
      <div class="add-dish-section">
        <el-button type="primary" plain @click="handleAddDish">
          <el-icon><Plus /></el-icon>
          添加其他菜品
        </el-button>
      </div>

      <!-- 价格明细 -->
      <div class="price-breakdown">
        <div class="price-row">
          <span>菜品小计</span>
          <span>¥{{ dishSubtotal.toFixed(2) }}</span>
        </div>
        <div v-if="cardData.diningMode === 'takeout'" class="price-row">
          <span>包装费</span>
          <span>¥{{ packagingFee.toFixed(2) }}</span>
        </div>
        <div class="price-row total">
          <span>预估总价</span>
          <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          v-if="cardData.actionButtons?.allowAIOrder"
          type="info"
          @click="handleAIOrder"
        >
          <el-icon><ChatDotRound /></el-icon>
          让AI帮我下单
        </el-button>
        <el-button type="primary" @click="handleConfirmOrder" :disabled="totalQuantity === 0">
          <el-icon><Check /></el-icon>
          确认下单 ({{ totalQuantity }}道菜)
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { InfoFilled, Plus, ChatDotRound, Check } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  cardData: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'confirm', 'close', 'add-dish', 'ai-order'])

const dialogVisible = ref(props.modelValue)
const dishQuantities = ref({})

// 初始化菜品数量
watch(() => props.cardData, (newData) => {
  if (newData?.preSelectedDishes) {
    const quantities = {}
    newData.preSelectedDishes.forEach(dish => {
      quantities[dish.dishId] = dish.quantity || 0
    })
    dishQuantities.value = quantities
  }
}, { immediate: true, deep: true })

watch(() => props.modelValue, (val) => {
  dialogVisible.value = val
})

watch(dialogVisible, (val) => {
  emit('update:modelValue', val)
})

// 计算菜品数量
const getDishQuantity = (dishId) => {
  return dishQuantities.value[dishId] || 0
}

// 计算总数量
const totalQuantity = computed(() => {
  return Object.values(dishQuantities.value).reduce((sum, qty) => sum + qty, 0)
})

// 计算菜品小计
const dishSubtotal = computed(() => {
  let subtotal = 0
  props.cardData.preSelectedDishes?.forEach(dish => {
    const quantity = dishQuantities.value[dish.dishId] || 0
    subtotal += (dish.price || 0) * quantity
  })
  return subtotal
})

// 计算包装费
const packagingFee = computed(() => {
  if (props.cardData.diningMode === 'takeout') {
    return totalQuantity.value * 2 // 每项2元包装费
  }
  return 0
})

// 计算总价
const totalPrice = computed(() => {
  return dishSubtotal.value + packagingFee.value
})

// 弹窗宽度（响应式）
const dialogWidth = computed(() => {
  return window.innerWidth < 768 ? '95%' : '600px'
})

// 处理数量变化
const handleQuantityChange = () => {
  // 触发价格更新
}

// 处理关闭
const handleClose = () => {
  dialogVisible.value = false
  emit('close')
}

// 添加其他菜品
const handleAddDish = () => {
  emit('add-dish', props.cardData.merchant?.merchantId)
}

// AI下单
const handleAIOrder = () => {
  emit('ai-order', {
    merchant: props.cardData.merchant,
    selectedDishes: getSelectedDishes(),
    diningMode: props.cardData.diningMode
  })
}

// 确认下单
const handleConfirmOrder = async () => {
  if (totalQuantity.value === 0) {
    ElMessage.warning('请至少选择一道菜品')
    return
  }

  const selectedDishes = getSelectedDishes()

  try {
    await ElMessageBox.confirm(
      `确认下单 ${selectedDishes.length} 道菜品，总价 ¥${totalPrice.value.toFixed(2)}？`,
      '确认订单',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    emit('confirm', {
      merchant: props.cardData.merchant,
      selectedDishes: selectedDishes,
      diningMode: props.cardData.diningMode,
      totalAmount: totalPrice.value
    })

    dialogVisible.value = false
  } catch {
    // 用户取消
  }
}

// 获取选中的菜品列表
const getSelectedDishes = () => {
  const selected = []
  props.cardData.preSelectedDishes?.forEach(dish => {
    const quantity = dishQuantities.value[dish.dishId] || 0
    if (quantity > 0) {
      selected.push({
        dishId: dish.dishId,
        dishName: dish.dishName,
        price: dish.price,
        quantity: quantity
      })
    }
  })
  return selected
}
</script>

<style lang="scss" scoped>
.dish-selector-dialog {
  :deep(.el-dialog__body) {
    padding: 20px;
    max-height: 60vh;
    overflow-y: auto;
  }
}

.dialog-content {
  .merchant-summary {
    margin-bottom: 20px;
    padding: 16px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 8px;
    color: #fff;

    h3 {
      margin: 0 0 8px 0;
      font-size: 18px;
    }

    .summary-info {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 8px;

      :deep(.el-tag) {
        background: rgba(255, 255, 255, 0.2);
        border-color: rgba(255, 255, 255, 0.3);
        color: #fff;
      }

      .address {
        font-size: 13px;
        opacity: 0.9;
      }
    }

    .recommendation {
      font-size: 13px;
      opacity: 0.95;
    }
  }

  .section-title {
    font-size: 14px;
    font-weight: 600;
    color: #333;
    margin-bottom: 12px;
    padding-bottom: 8px;
    border-bottom: 2px solid #e9ecef;
  }

  .dishes-list {
    margin-bottom: 16px;

    .dish-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px;
      background: #f8f9fa;
      border-radius: 8px;
      margin-bottom: 12px;
      transition: all 0.3s;

      &.zero-quantity {
        opacity: 0.5;
      }

      &:hover {
        background: #e9ecef;
      }

      .dish-image {
        width: 60px;
        height: 60px;
        border-radius: 6px;
        overflow: hidden;
        flex-shrink: 0;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        .placeholder-image {
          width: 100%;
          height: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
          background: #dee2e6;
          color: #6c757d;
          font-size: 24px;
          font-weight: 600;
        }
      }

      .dish-info {
        flex: 1;

        .dish-name {
          margin: 0 0 4px 0;
          font-size: 15px;
          color: #333;
        }

        .dish-meta {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 6px;

          .calories {
            font-size: 12px;
            color: #666;
          }
        }

        .dish-recommendation {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 12px;
          color: #ff9800;
          margin-bottom: 4px;

          .el-icon {
            font-size: 14px;
          }
        }

        .dish-price {
          font-size: 16px;
          font-weight: 600;
          color: #f56c6c;
        }
      }

      .dish-quantity {
        flex-shrink: 0;
      }
    }
  }

  .add-dish-section {
    margin-bottom: 16px;

    .el-button {
      width: 100%;
    }
  }

  .price-breakdown {
    padding: 16px;
    background: #f8f9fa;
    border-radius: 8px;

    .price-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
      font-size: 14px;

      &:last-child {
        margin-bottom: 0;
      }

      &.total {
        padding-top: 8px;
        border-top: 1px solid #dee2e6;
        font-weight: 600;
        font-size: 16px;

        .total-price {
          font-size: 20px;
          color: #f56c6c;
        }
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
  .dish-selector-dialog {
    :deep(.el-dialog) {
      width: 95% !important;
      margin: 0 auto;
    }

    :deep(.el-dialog__body) {
      padding: 12px;
    }
  }

  .dialog-content {
    .dish-item {
      flex-direction: column;
      align-items: stretch;

      .dish-image {
        width: 100%;
        height: 120px;
      }

      .dish-quantity {
        align-self: flex-end;
      }
    }
  }
}
</style>
