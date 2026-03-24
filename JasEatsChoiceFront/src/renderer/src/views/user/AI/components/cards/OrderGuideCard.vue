<template>
  <div class="order-guide-card">
    <div class="card-header">
      <div class="header-title">
        <span class="icon">🛒</span>
        <span class="title">下单引导</span>
      </div>
      <div class="header-summary">{{ data.summary }}</div>
    </div>

    <div class="card-content">
      <!-- 找到的菜品列表 -->
      <div v-if="data.dishes && data.dishes.length > 0" class="dishes-section">
        <div class="section-title">✅ 已为您找到以下菜品</div>

        <div class="dish-list">
          <div
            v-for="(dish, index) in data.dishes"
            :key="index"
            class="dish-item"
          >
            <div class="dish-header">
              <span class="dish-index">{{ index + 1 }}</span>
              <span class="dish-name">{{ dish.name }}</span>
            </div>

            <div class="dish-details">
              <div class="detail-row">
                <span class="label">价格：</span>
                <span class="value price">¥{{ dish.price }} × {{ dish.quantity }} = ¥{{ dish.subtotal }}</span>
              </div>

              <div v-if="dish.calories" class="detail-row">
                <span class="label">热量：</span>
                <span class="value">{{ dish.calories }} kcal</span>
              </div>

              <div v-if="dish.merchantId" class="detail-row">
                <span class="label">商家ID：</span>
                <span class="value code">{{ dish.merchantId }}</span>
              </div>

              <div v-if="dish.dishId" class="detail-row">
                <span class="label">菜品ID：</span>
                <span class="value code">{{ dish.dishId }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 未找到的菜品 -->
        <div v-if="data.notFoundDishes && data.notFoundDishes.length > 0" class="not-found-section">
          <div class="not-found-title">⚠️ 以下菜品未找到</div>
          <ul class="not-found-list">
            <li v-for="(dish, index) in data.notFoundDishes" :key="index">
              {{ dish }}
            </li>
          </ul>
        </div>

        <!-- 总价信息 -->
        <div class="total-section">
          <div class="total-row">
            <span class="total-label">💰 预计总价：</span>
            <span class="total-amount">¥{{ data.totalAmount }}</span>
          </div>
          <div v-if="data.totalCalories" class="total-row">
            <span class="total-label">🔥 总热量：</span>
            <span class="total-value">{{ data.totalCalories }} kcal</span>
          </div>
        </div>
      </div>

      <!-- 操作引导 -->
      <div class="guide-section">
        <div class="guide-title">📱 下一步操作</div>
        <div class="guide-content">
          <p>请前往「下单页面」完成订单：</p>
          <ol>
            <li>确认配送地址</li>
            <li>选择支付方式</li>
            <li>确认并支付订单</li>
          </ol>
        </div>
      </div>

      <!-- 提示信息 -->
      <div class="tips-section">
        <div class="tips-title">💡 提示</div>
        <ul class="tips-list">
          <li>您可以在下单页面调整菜品数量</li>
          <li>如需添加备注，请在下单页面填写</li>
          <li>支持使用钱包余额或优惠券支付</li>
        </ul>
      </div>

      <!-- 操作按钮 -->
      <div class="actions-section">
        <el-button
          type="primary"
          size="large"
          :icon="ShoppingCart"
          @click="handleGoToOrder"
        >
          前往下单页面
        </el-button>
        <el-button
          size="large"
          @click="handleCancel"
        >
          取消
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ShoppingCart } from '@element-plus/icons-vue'

const props = defineProps({
  data: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['action'])

// 前往下单页面
const handleGoToOrder = () => {
  emit('action', {
    type: 'go_to_order',
    data: {
      dishes: props.data.dishes,
      totalAmount: props.data.totalAmount
    }
  })
}

// 取消
const handleCancel = () => {
  emit('action', {
    type: 'cancel',
    data: null
  })
}
</script>

<style scoped>
.order-guide-card {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 16px 20px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 4px;
}

.icon {
  font-size: 24px;
}

.header-summary {
  font-size: 14px;
  opacity: 0.9;
}

.card-content {
  background: white;
  padding: 20px;
}

.dishes-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.dish-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.dish-item {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 12px;
  background: #fafafa;
}

.dish-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.dish-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: #667eea;
  color: white;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
}

.dish-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.dish-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-left: 32px;
}

.detail-row {
  display: flex;
  font-size: 14px;
}

.detail-row .label {
  color: #666;
  min-width: 80px;
}

.detail-row .value {
  color: #333;
  font-weight: 500;
}

.detail-row .value.price {
  color: #f56c6c;
  font-weight: 600;
}

.detail-row .value.code {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #666;
}

.not-found-section {
  margin-bottom: 16px;
  padding: 12px;
  background: #fff3cd;
  border-radius: 8px;
  border: 1px solid #ffc107;
}

.not-found-title {
  font-size: 14px;
  font-weight: 600;
  color: #856404;
  margin-bottom: 8px;
}

.not-found-list {
  margin: 0;
  padding-left: 20px;
  color: #856404;
}

.not-found-list li {
  margin-bottom: 4px;
}

.total-section {
  background: #f0f9ff;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 16px;
}

.total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.total-row:last-child {
  margin-bottom: 0;
}

.total-label {
  font-size: 15px;
  color: #333;
  font-weight: 500;
}

.total-amount {
  font-size: 20px;
  color: #f56c6c;
  font-weight: 700;
}

.total-value {
  font-size: 15px;
  color: #333;
  font-weight: 600;
}

.guide-section {
  background: #e8f5e9;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 16px;
}

.guide-title {
  font-size: 15px;
  font-weight: 600;
  color: #2e7d32;
  margin-bottom: 8px;
}

.guide-content {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
}

.guide-content p {
  margin: 0 0 8px 0;
}

.guide-content ol {
  margin: 0;
  padding-left: 20px;
}

.guide-content li {
  margin-bottom: 4px;
}

.tips-section {
  background: #fff8e1;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 20px;
}

.tips-title {
  font-size: 14px;
  font-weight: 600;
  color: #f57f17;
  margin-bottom: 8px;
}

.tips-list {
  margin: 0;
  padding-left: 20px;
}

.tips-list li {
  font-size: 13px;
  color: #827717;
  margin-bottom: 4px;
}

.actions-section {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.actions-section .el-button {
  min-width: 140px;
}
</style>
