<template>
  <div class="merchant-order-card" @click="handleCardClick">
    <div class="merchant-header">
      <div class="merchant-avatar">
        <img v-if="merchant.logo" :src="merchant.logo" :alt="merchant.name" />
        <span v-else class="default-avatar">🏪</span>
      </div>
      <div class="merchant-info">
        <h3 class="merchant-name">{{ merchant.name }}</h3>
        <div class="merchant-meta">
          <span v-if="merchant.rating" class="rating">
            <el-icon><Star /></el-icon>
            {{ merchant.rating }}
          </span>
          <span v-if="merchant.distance" class="distance">
            <el-icon><Location /></el-icon>
            {{ merchant.distance }}米
          </span>
          <span v-if="merchant.estimatedTime" class="time">
            <el-icon><Clock /></el-icon>
            {{ merchant.estimatedTime }}分钟
          </span>
        </div>
      </div>
      <div class="merchant-status">
        <el-tag v-if="merchant.isOpen" type="success" size="small">营业中</el-tag>
        <el-tag v-else type="info" size="small">已休息</el-tag>
      </div>
    </div>

    <div v-if="preSelectedDishes && preSelectedDishes.length > 0" class="preselected-dishes">
      <div class="dishes-title">🍽️ AI为您预选：</div>
      <div class="dishes-list">
        <div v-for="dish in preSelectedDishes.slice(0, 3)" :key="dish.dishId" class="dish-preview">
          <span class="dish-name">{{ dish.dishName }}</span>
          <span class="dish-info">×{{ dish.quantity}}</span>
        </div>
        <div v-if="preSelectedDishes.length > 3" class="more-dishes">
          等{{ preSelectedDishes.length }}道菜品
        </div>
      </div>
    </div>

    <div class="merchant-footer">
      <div class="price-info">
        <span class="estimated-label">预估总价：</span>
        <span class="estimated-price">¥{{ estimatedTotal?.toFixed(2) }}</span>
      </div>
      <div class="action-buttons">
        <el-button size="small" @click.stop="handleViewMenu">
          <el-icon><Menu /></el-icon>
          查看菜单
        </el-button>
        <el-button type="primary" size="small" @click.stop="handleOrderNow">
          <el-icon><ShoppingCart /></el-icon>
          立即下单
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Star, Location, Clock, Menu, ShoppingCart } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  merchant: {
    type: Object,
    required: true
  },
  preSelectedDishes: {
    type: Array,
    default: () => []
  },
  diningMode: {
    type: String,
    default: 'takeout'
  },
  estimatedTotal: {
    type: Number,
    default: 0
  },
  actionButtons: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['click', 'view-menu', 'order-now'])

const handleCardClick = () => {
  emit('click', props.merchant)
}

const handleViewMenu = () => {
  emit('view-menu', props.merchant)
}

const handleOrderNow = () => {
  emit('order-now', {
    merchant: props.merchant,
    preSelectedDishes: props.preSelectedDishes,
    diningMode: props.diningMode,
    estimatedTotal: props.estimatedTotal
  })
}
</script>

<style lang="scss" scoped>
.merchant-order-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  margin: 12px 0;
  cursor: pointer;
  transition: all 0.3s;
  overflow: hidden;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
  }
}

.merchant-header {
  display: flex;
  align-items: center;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;

  .merchant-avatar {
    width: 56px;
    height: 56px;
    border-radius: 8px;
    overflow: hidden;
    margin-right: 12px;
    background: rgba(255, 255, 255, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .default-avatar {
      font-size: 28px;
    }
  }

  .merchant-info {
    flex: 1;

    .merchant-name {
      margin: 0 0 6px 0;
      font-size: 18px;
      font-weight: 600;
      color: #fff;
    }

    .merchant-meta {
      display: flex;
      gap: 12px;
      font-size: 13px;
      opacity: 0.9;

      span {
        display: flex;
        align-items: center;
        gap: 4px;

        .el-icon {
          font-size: 14px;
        }
      }
    }
  }

  .merchant-status {
    :deep(.el-tag) {
      background: rgba(255, 255, 255, 0.2);
      border-color: rgba(255, 255, 255, 0.3);
      color: #fff;
    }
  }
}

.preselected-dishes {
  padding: 12px 16px;
  background: #f8f9fa;
  border-top: 1px solid #e9ecef;

  .dishes-title {
    font-size: 13px;
    color: #666;
    margin-bottom: 8px;
  }

  .dishes-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    .dish-preview {
      display: flex;
      align-items: center;
      gap: 4px;
      padding: 4px 8px;
      background: #fff;
      border-radius: 4px;
      font-size: 12px;

      .dish-name {
        color: #333;
      }

      .dish-info {
        color: #666;
      }
    }

    .more-dishes {
      padding: 4px 8px;
      background: #e9ecef;
      border-radius: 4px;
      font-size: 12px;
      color: #666;
    }
  }
}

.merchant-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #fff;

  .price-info {
    .estimated-label {
      font-size: 13px;
      color: #666;
    }

    .estimated-price {
      font-size: 18px;
      font-weight: 600;
      color: #f56c6c;
      margin-left: 8px;
    }
  }

  .action-buttons {
    display: flex;
    gap: 8px;

    .el-button {
      :deep(.el-icon) {
        margin-right: 4px;
      }
    }
  }
}

// 响应式
@media (max-width: 768px) {
  .merchant-order-card {
    margin: 8px 0;
  }

  .merchant-header {
    padding: 12px;

    .merchant-avatar {
      width: 48px;
      height: 48px;

      .default-avatar {
        font-size: 24px;
      }
    }

    .merchant-info {
      .merchant-name {
        font-size: 16px;
      }

      .merchant-meta {
        gap: 8px;
        font-size: 12px;
      }
    }
  }

  .preselected-dishes {
    padding: 10px 12px;
  }

  .merchant-footer {
    padding: 10px 12px;
    flex-direction: column;
    gap: 12px;
    align-items: stretch;

    .price-info {
      text-align: center;
    }

    .action-buttons {
      justify-content: space-between;

      .el-button {
        flex: 1;
      }
    }
  }
}
</style>
