<template>
  <div class="card-message">
    <!-- 人类可读的文本 -->
    <div v-if="message.text" class="message-text">
      {{ message.text }}
    </div>

    <!-- 商家下单卡片 -->
    <MerchantOrderCard
      v-if="isMerchantOrderCard(message.cardData)"
      :merchant="message.cardData.merchant"
      :pre-selected-dishes="message.cardData.preSelectedDishes"
      :dining-mode="message.cardData.diningMode"
      :estimated-total="message.cardData.estimatedTotal"
      :action-buttons="message.cardData.actionButtons"
      @click="handleCardClick"
      @view-menu="handleViewMenu"
      @order-now="handleOrderNow"
    />

    <!-- 菜单卡片 -->
    <div v-else-if="isMenuCard(message.cardData)" class="menu-card">
      <h3>📋 {{ message.cardData.merchant?.name }} - 菜单</h3>
      <div class="menu-items">
        <div v-for="item in message.cardData.menuItems" :key="item.dishId" class="menu-item">
          <span class="item-name">{{ item.name }}</span>
          <span class="item-price">¥{{ item.price }}</span>
        </div>
      </div>
    </div>

    <!-- 美食推荐卡片 -->
    <div v-else-if="isFoodRecommendationCard(message.cardData)" class="food-recommendation-card">
      <h3>🍽️ 附近美食推荐</h3>
      <div class="recommendation-list">
        <div
          v-for="item in message.cardData.recommendations"
          :key="item.dishId"
          class="recommendation-item"
        >
          <div class="item-info">
            <span class="item-name">{{ item.name }}</span>
            <span class="item-merchant">{{ item.merchantName }}</span>
          </div>
          <span class="item-score">⭐ {{ item.score }}</span>
        </div>
      </div>
    </div>

    <!-- 未知卡片类型 -->
    <div v-else-if="message.cardData" class="unknown-card">
      <el-alert type="warning" :closable="false">
        <template #title>
          未知的卡片类型: {{ message.cardData.cardType }}
        </template>
      </el-alert>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import MerchantOrderCard from '../merchant/MerchantOrderCard.vue'
import {
  isMerchantOrderCard,
  isMenuCard,
  isFoodRecommendationCard
} from '../../utils/cardMessageParser'

const props = defineProps({
  message: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['card-click', 'view-menu', 'order-now'])

const handleCardClick = (merchant) => {
  emit('card-click', merchant)
}

const handleViewMenu = (merchant) => {
  emit('view-menu', merchant)
}

const handleOrderNow = (orderData) => {
  emit('order-now', orderData)
}
</script>

<style lang="scss" scoped>
.card-message {
  width: 100%;
  max-width: 600px;

  .message-text {
    white-space: pre-wrap;
    margin-bottom: 12px;
    line-height: 1.6;
  }
}

.menu-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  h3 {
    margin: 0 0 12px 0;
    font-size: 16px;
    color: #333;
  }

  .menu-items {
    .menu-item {
      display: flex;
      justify-content: space-between;
      padding: 8px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .item-name {
        color: #333;
      }

      .item-price {
        color: #f56c6c;
        font-weight: 600;
      }
    }
  }
}

.food-recommendation-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  h3 {
    margin: 0 0 12px 0;
    font-size: 16px;
    color: #333;
  }

  .recommendation-list {
    .recommendation-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px;
      background: #f8f9fa;
      border-radius: 6px;
      margin-bottom: 8px;

      &:last-child {
        margin-bottom: 0;
      }

      .item-info {
        flex: 1;

        .item-name {
          display: block;
          font-weight: 600;
          color: #333;
          margin-bottom: 4px;
        }

        .item-merchant {
          font-size: 13px;
          color: #666;
        }
      }

      .item-score {
        font-size: 14px;
        color: #ff9800;
        font-weight: 600;
      }
    }
  }
}

.unknown-card {
  padding: 0;
}
</style>
