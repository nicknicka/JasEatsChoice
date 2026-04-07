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

<style lang="less" scoped>
@import '../../assets/css/nordic-theme.less';
@import '../../assets/css/merchant-theme.less';

.merchant-order-card {
  background: @merchant-surface;
  border-radius: @nordic-radius-lg;
  border: 1px solid @merchant-border;
  box-shadow: 0 1px 4px @merchant-shadow;
  margin: 12px 0;
  cursor: pointer;
  transition: all @nordic-transition-base ease;
  overflow: hidden;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 24px @merchant-shadow-hover;
    border-color: @merchant-primary;
  }
}

.merchant-header {
  display: flex;
  align-items: center;
  padding: @nordic-space-md;
  background: linear-gradient(135deg, @merchant-primary 0%, @merchant-primary-dark 100%);
  color: @merchant-surface;

  .merchant-avatar {
    width: 56px;
    height: 56px;
    border-radius: @nordic-radius-sm;
    overflow: hidden;
    margin-right: @nordic-space-md;
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
      font-size: @nordic-text-md;
      font-weight: 600;
      color: @merchant-surface;
    }

    .merchant-meta {
      display: flex;
      gap: @nordic-space-md;
      font-size: @nordic-text-sm;
      opacity: 0.9;

      span {
        display: flex;
        align-items: center;
        gap: 4px;

        .el-icon {
          font-size: @nordic-text-base;
        }
      }
    }
  }

  .merchant-status {
    :deep(.el-tag) {
      background: rgba(255, 255, 255, 0.2);
      border-color: rgba(255, 255, 255, 0.3);
      color: @merchant-surface;
    }
  }
}

.preselected-dishes {
  padding: @nordic-space-md;
  background: @merchant-surface-alt;
  border-top: 1px solid @merchant-divider;

  .dishes-title {
    font-size: @nordic-text-sm;
    color: @merchant-text-sec;
    margin-bottom: @nordic-space-sm;
  }

  .dishes-list {
    display: flex;
    flex-wrap: wrap;
    gap: @nordic-space-sm;

    .dish-preview {
      display: flex;
      align-items: center;
      gap: 4px;
      padding: 4px 8px;
      background: @merchant-surface;
      border-radius: @nordic-radius-xs;
      font-size: @nordic-text-xs;

      .dish-name {
        color: @merchant-text;
      }

      .dish-info {
        color: @merchant-text-sec;
      }
    }

    .more-dishes {
      padding: 4px 8px;
      background: @merchant-divider;
      border-radius: @nordic-radius-xs;
      font-size: @nordic-text-xs;
      color: @merchant-text-sec;
    }
  }
}

.merchant-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: @nordic-space-md;
  background: @merchant-surface;

  .price-info {
    .estimated-label {
      font-size: @nordic-text-sm;
      color: @merchant-text-sec;
    }

    .estimated-price {
      font-size: @nordic-text-md;
      font-weight: 600;
      color: @merchant-secondary;
      margin-left: @nordic-space-sm;
    }
  }

  .action-buttons {
    display: flex;
    gap: @nordic-space-sm;

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
    margin: @nordic-space-sm 0;
  }

  .merchant-header {
    padding: @nordic-space-md;

    .merchant-avatar {
      width: 48px;
      height: 48px;

      .default-avatar {
        font-size: 24px;
      }
    }

    .merchant-info {
      .merchant-name {
        font-size: @nordic-text-base;
      }

      .merchant-meta {
        gap: @nordic-space-sm;
        font-size: @nordic-text-xs;
      }
    }
  }

  .preselected-dishes {
    padding: 10px @nordic-space-md;
  }

  .merchant-footer {
    padding: 10px @nordic-space-md;
    flex-direction: column;
    gap: @nordic-space-md;
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
