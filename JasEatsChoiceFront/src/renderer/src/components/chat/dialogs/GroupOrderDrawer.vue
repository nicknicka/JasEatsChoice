<template>
  <el-drawer
    v-model="visible"
    direction="rtl"
    size="45%"
    :close-on-click-modal="true"
    class="group-order-drawer"
  >
    <template #header>
      <div class="drawer-header">
        <div class="header-left">
          <el-icon :size="20" color="#409eff"><ShoppingCart /></el-icon>
          <span class="header-title">群订单详情</span>
        </div>
        <div class="header-right">
          <el-tag
            :type="groupOrder?.status === 'active' ? 'success' : 'info'"
            size="default"
            effect="dark"
          >
            {{ groupOrder?.status === 'active' ? '进行中' : '已结束' }}
          </el-tag>
        </div>
      </div>
    </template>

    <div v-if="groupOrder" class="drawer-content">
      <!-- 订单概览 -->
      <div class="order-overview">
        <div class="overview-card">
          <div class="overview-header">
            <el-icon :size="17" color="#409eff"><InfoFilled /></el-icon>
            <span class="overview-title">订单信息</span>
          </div>

          <div class="overview-grid">
            <div class="overview-item">
              <div class="item-icon">👥</div>
              <div class="item-content">
                <div class="item-label">群名称</div>
                <div class="item-value">{{ groupOrder.groupName }}</div>
              </div>
            </div>

            <div class="overview-item">
              <div class="item-icon">👤</div>
              <div class="item-content">
                <div class="item-label">创建人</div>
                <div class="item-value">{{ groupOrder.creator }}</div>
              </div>
            </div>

            <div class="overview-item" v-if="groupOrder.merchantName">
              <div class="item-icon">🏪</div>
              <div class="item-content">
                <div class="item-label">已选商家</div>
                <div class="item-value">
                  {{ groupOrder.merchantName }}
                  <el-button
                    type="primary"
                    size="small"
                    text
                    @click="$emit('change-merchant')"
                    v-if="canChangeMerchant"
                    class="change-merchant-btn"
                  >
                    <el-icon><Refresh /></el-icon> 更换
                  </el-button>
                </div>
              </div>
            </div>

            <div class="overview-item">
              <div class="item-icon">💰</div>
              <div class="item-content">
                <div class="item-label">总金额</div>
                <div class="item-value price">¥{{ groupOrder.totalAmount.toFixed(2) }}</div>
              </div>
            </div>

            <div class="overview-item">
              <div class="item-icon">👥</div>
              <div class="item-content">
                <div class="item-label">参与人数</div>
                <div class="item-value">{{ groupOrder.members.length }} 人</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 快速点餐入口 -->
      <div
        class="quick-order-entry"
        v-if="groupOrder.status === 'active'"
      >
        <div class="quick-order-card">
          <div class="quick-order-content">
            <el-icon :size="24" color="#67c23a"><Plus /></el-icon>
            <div class="quick-order-text">
              <div class="quick-order-title">{{ hasMerchant ? '继续点餐' : '开始点餐' }}</div>
              <div class="quick-order-desc">{{ hasMerchant ? '添加更多商品到订单' : '选择商家并开始点餐' }}</div>
            </div>
          </div>
          <el-button
            type="success"
            size="default"
            @click="hasMerchant ? $emit('continue-order') : $emit('select-merchant')"
            class="quick-order-btn"
          >
            <el-icon><ShoppingCart /></el-icon> {{ hasMerchant ? '立即点餐' : '选择商家' }}
          </el-button>
        </div>
      </div>

      <!-- 加菜功能入口 -->
      <div
        class="add-dish-section"
        v-if="groupOrder.status === 'active' && hasMerchant"
      >
        <div class="add-dish-header">
          <div class="add-dish-title">
            <el-icon :size="18" color="#e6a23c"><Plus /></el-icon>
            <span>加菜功能</span>
          </div>
        </div>

        <div class="add-dish-actions">
          <el-button
            type="warning"
            size="default"
            @click="$emit('open-add-dish-dialog')"
            class="add-dish-btn"
          >
            <el-icon><Dish /></el-icon>
            我要加菜
          </el-button>

          <el-button
            v-if="isInitiator"
            type="primary"
            size="default"
            @click="$emit('open-add-dish-review')"
            class="review-btn"
          >
            <el-icon><DocumentChecked /></el-icon>
            查看审核
            <el-badge
              v-if="pendingReviewCount > 0"
              :value="pendingReviewCount"
              class="review-badge"
            />
          </el-button>

          <el-button
            v-if="hasPendingPayments"
            type="success"
            size="default"
            @click="$emit('open-pending-payment')"
            class="payment-btn"
          >
            <el-icon><Wallet /></el-icon>
            待支付加菜
            <el-badge
              :value="pendingPaymentCount"
              class="payment-badge"
            />
          </el-button>
        </div>
      </div>

      <!-- 底部按钮 -->
      <div class="drawer-footer">
        <div class="footer-actions">
          <el-button size="default" @click="$emit('select-merchant')">
            <el-icon><Shop /></el-icon>
            选择商家
          </el-button>
          <el-button
            type="success"
            size="default"
            @click="$emit('go-to-pay')"
            :disabled="!hasMerchant"
          >
            <el-icon><Wallet /></el-icon>
            去支付
          </el-button>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import {
  ShoppingCart,
  InfoFilled,
  Refresh,
  Plus,
  Shop,
  Wallet,
  Dish,
  DocumentChecked
} from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  groupOrder: {
    type: Object,
    default: null
  },
  currentUserId: {
    type: [String, Number],
    required: true
  },
  pendingReviewCount: {
    type: Number,
    default: 0
  },
  pendingPaymentCount: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits([
  'update:modelValue',
  'change-merchant',
  'continue-order',
  'select-merchant',
  'go-to-pay',
  'open-add-dish-dialog',
  'open-add-dish-review',
  'open-pending-payment'
])

const visible = ref(props.modelValue)

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const hasMerchant = computed(() => {
  return props.groupOrder && props.groupOrder.merchantName
})

const canChangeMerchant = computed(() => {
  return (
    props.groupOrder &&
    props.groupOrder.creator === '我' &&
    props.groupOrder.orderItems.length === 0 &&
    props.groupOrder.status === 'active'
  )
})

// 是否为发起者
const isInitiator = computed(() => {
  return props.groupOrder && props.groupOrder.creator === '我'
})

// 是否有待支付的加菜订单
const hasPendingPayments = computed(() => {
  return props.pendingPaymentCount > 0
})
</script>

<style scoped lang="less">
.group-order-drawer {
  :deep(.el-drawer__header) {
    margin-bottom: 0;
    padding: 14px 20px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
  }

  :deep(.el-drawer__body) {
    padding: 14px;
    background-color: #f5f7fa;
  }
}

.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;

  .header-left {
    display: flex;
    align-items: center;
    gap: 10px;

    .header-title {
      font-size: 17px;
      font-weight: 600;
      color: white;
    }
  }
}

.drawer-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 14px;

  .order-overview {
    .overview-card {
      background: white;
      border-radius: 10px;
      padding: 14px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

      .overview-header {
        display: flex;
        align-items: center;
        gap: 6px;
        margin-bottom: 12px;
        padding-bottom: 10px;
        border-bottom: 2px solid #f0f0f0;

        .overview-title {
          font-size: 14px;
          font-weight: 600;
          color: #303133;
        }
      }

      .overview-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
        gap: 10px;

        .overview-item {
          display: flex;
          align-items: center;
          gap: 10px;
          padding: 10px;
          background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
          border-radius: 8px;
          transition: all 0.3s;

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
          }

          .item-icon {
            font-size: 24px;
            width: 40px;
            height: 40px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: white;
            border-radius: 8px;
          }

          .item-content {
            flex: 1;

            .item-label {
              font-size: 11px;
              color: #909399;
              margin-bottom: 3px;
            }

            .item-value {
              font-size: 13px;
              font-weight: 500;
              color: #303133;

              &.price {
                font-size: 16px;
                color: #f56c6c;
                font-weight: 600;
              }

              .change-merchant-btn {
                margin-left: 6px;
              }
            }
          }
        }
      }
    }
  }

  .quick-order-entry {
    .quick-order-card {
      background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
      border-radius: 10px;
      padding: 14px;
      color: white;
      box-shadow: 0 4px 16px rgba(103, 194, 58, 0.3);

      .quick-order-content {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 12px;

        .quick-order-text {
          flex: 1;

          .quick-order-title {
            font-size: 15px;
            font-weight: 600;
            margin-bottom: 3px;
          }

          .quick-order-desc {
            font-size: 12px;
            opacity: 0.9;
          }
        }
      }

      .quick-order-btn {
        width: 100%;
        background: white;
        color: #67c23a;
        border: none;
        font-weight: 600;

        &:hover {
          background: #f0f9ff;
          color: #67c23a;
        }
      }
    }
  }

  .add-dish-section {
    background: linear-gradient(135deg, #fff7e6 0%, #ffe8cc 100%);
    border-radius: 10px;
    padding: 14px;
    box-shadow: 0 2px 12px rgba(230, 162, 60, 0.2);
    border: 1px solid #ffe8cc;

    .add-dish-header {
      margin-bottom: 12px;

      .add-dish-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 14px;
        font-weight: 600;
        color: #e6a23c;
      }
    }

    .add-dish-actions {
      display: flex;
      flex-direction: column;
      gap: 8px;

      .el-button {
        width: 100%;
        font-weight: 500;
        position: relative;

        .review-badge,
        .payment-badge {
          position: absolute;
          top: -8px;
          right: -8px;
        }
      }

      .add-dish-btn {
        background: white;
        color: #e6a23c;
        border: 1px solid #e6a23c;

        &:hover {
          background: #fff7e6;
          color: #e6a23c;
          border-color: #d9983b;
        }
      }

      .review-btn,
      .payment-btn {
        background: white;
        border: 1px solid #409eff;

        &:hover {
          background: #ecf5ff;
        }
      }
    }
  }

  .drawer-footer {
    background: white;
    border-radius: 10px;
    padding: 14px;
    box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.08);

    .footer-actions {
      display: flex;
      gap: 10px;

      .el-button {
        flex: 1;
        font-weight: 500;
      }
    }
  }
}
</style>
