<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, Document, ShoppingBag, Refresh, Timer } from '@element-plus/icons-vue'
import CommonBackButton from '../../common/CommonBackButton.vue'

const props = defineProps({
  order: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['status-update', 'send-reminder'])

const router = useRouter()
const showDetail = ref(false)

// 订单状态映射（对应后端状态码）
// 0-待支付、1-待接单、2-备菜中、3-烹饪中、4-待上菜、5-已送达、6-已取消、7-待评价、8-已评价
const orderStatusMap = {
  0: { text: '待支付', type: 'info', color: '#909399' },
  1: { text: '待接单', type: 'danger', color: '#f56c6c' },
  2: { text: '备菜中', type: 'warning', color: '#e6a23c' },
  3: { text: '烹饪中', type: 'warning', color: '#ff9800' },
  4: { text: '待上菜', type: 'primary', color: '#409eff' },
  5: { text: '已送达', type: 'success', color: '#67c23a' },
  6: { text: '已取消', type: 'info', color: '#c0c4cc' },
  7: { text: '待评价', type: 'success', color: '#95d475' },
  8: { text: '已评价', type: 'success', color: '#85ce61' }
}

// 订单状态
const orderStatus = computed(() => {
  if (!props.order) return null
  return orderStatusMap[props.order.status] || orderStatusMap[1]
})

// 切换详情显示
const toggleDetail = () => {
  showDetail.value = !showDetail.value
}

// 查看订单详情
const viewOrderDetail = () => {
  if (props.order) {
    router.push(`/merchant/home/order-detail/${props.order.orderId}`)
  }
}

// 更新订单状态
const updateOrderStatus = (newStatus) => {
  if (!props.order) return

  ElMessageBox.confirm(
    `确定要将订单状态更新为"${orderStatusMap[newStatus].text}"吗？`,
    '更新订单状态',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(() => {
      emit('status-update', {
        orderId: props.order.orderId,
        status: newStatus
      })
      ElMessage.success('订单状态已更新')
    })
    .catch(() => {})
}

// 发送订单提醒
const sendReminder = () => {
  if (!props.order) return

  const statusText = orderStatusMap[props.order.status]?.text || '未知'
  const reminderContent = `您的订单（${props.order.orderId}）${statusText}，请耐心等待`

  emit('send-reminder', {
    orderId: props.order.orderId,
    content: reminderContent
  })

  ElMessage.success('订单提醒已发送')
}
</script>

<template>
  <div v-if="order" class="order-info-panel-wrapper">
    <!-- 订单摘要 -->
    <div v-if="!showDetail" class="order-summary">
      <div class="summary-header">
        <div class="header-title">
          <el-icon :size="18"><ShoppingBag /></el-icon>
          <span>关联订单信息</span>
        </div>
        <el-button type="primary" size="small" @click="toggleDetail" :icon="View">
          查看详情
        </el-button>
      </div>
      <div class="summary-content">
        <div class="info-item">
          <span class="label">订单号：</span>
          <span class="value">{{ order.orderId }}</span>
        </div>
        <div class="info-item">
          <span class="label">金额：</span>
          <span class="value amount">¥{{ order.totalAmount }}</span>
        </div>
        <div class="info-item">
          <span class="label">状态：</span>
          <el-tag v-if="orderStatus" :type="orderStatus.type" size="small">
            {{ orderStatus.text }}
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 订单详情 -->
    <div v-else class="order-detail">
      <div class="detail-header">
        <div class="header-title">
          <el-icon :size="18"><Document /></el-icon>
          <span>订单详情</span>
        </div>
        <el-button size="small" @click="toggleDetail">收起</el-button>
      </div>

      <div class="detail-content">
        <!-- 基本信息 -->
        <div class="order-basic-info">
          <div class="info-row">
            <span class="label">订单号</span>
            <span class="value">{{ order.orderId }}</span>
          </div>
          <div class="info-row">
            <span class="label">联系电话</span>
            <span class="value">{{ order.phone }}</span>
          </div>
          <div class="info-row">
            <span class="label">配送地址</span>
            <span class="value">{{ order.address }}</span>
          </div>
        </div>

        <!-- 菜品清单 -->
        <div class="order-items">
          <div class="items-title">菜品清单</div>
          <div v-for="(item, index) in order.items" :key="index" class="item-row">
            <span class="item-name">{{ item.name }}</span>
            <span class="item-quantity">×{{ item.quantity }}</span>
            <span class="item-price">¥{{ item.price }}</span>
          </div>
        </div>

        <!-- 订单总额 -->
        <div class="order-total">
          <div class="total-row">
            <span class="label">订单总额</span>
            <span class="value amount">¥{{ order.totalAmount }}</span>
          </div>
        </div>
      </div>

      <!-- 快捷操作 -->
      <div class="detail-actions">
        <div class="actions-title">快捷操作</div>
        <div class="action-buttons">
          <el-button size="small" @click="viewOrderDetail" :icon="View"> 查看订单 </el-button>
          <el-dropdown trigger="click">
            <el-button type="primary" size="small" :icon="Refresh"> 更新状态 </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="(status, key) in orderStatusMap"
                  :key="key"
                  @click="updateOrderStatus(parseInt(key))"
                  :disabled="parseInt(key) === order.status"
                >
                  <el-tag :type="status.type" size="small">{{ status.text }}</el-tag>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button type="success" size="small" @click="sendReminder" :icon="Timer">
            发送提醒
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
@import '../../../../assets/css/nordic-theme.less';
@import '../../../../assets/css/merchant-theme.less';

.order-info-panel-wrapper {
  margin: 12px;

  .order-summary {
    padding: 16px 20px;
    background: linear-gradient(135deg, @merchant-primary-light 0%, @merchant-surface-alt 100%);
    border: 1px solid @merchant-border;
    border-radius: @nordic-radius-lg;
    box-shadow: 0 2px 8px @merchant-shadow;

    .summary-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .header-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: @nordic-text-base;
        font-weight: 600;
        color: @merchant-primary-dark;
      }

      :deep(.el-button) {
        font-weight: 500;
      }
    }

    .summary-content {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 12px;

      .info-item {
        display: flex;
        flex-direction: column;
        gap: 4px;

        .label {
          font-size: @nordic-text-xs;
          color: @merchant-text-sec;
          font-weight: 500;
        }

        .value {
          font-size: @nordic-text-base;
          color: @merchant-text;
          font-weight: 600;

          &.amount {
            color: @merchant-secondary;
          }
        }
      }
    }
  }

  .order-detail {
    padding: 16px;
    background: @merchant-surface;
    border: 1px solid @merchant-border;
    border-radius: @nordic-radius-lg;
    box-shadow: 0 2px 8px @merchant-shadow;

    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      padding-bottom: 12px;
      border-bottom: 1px solid @merchant-divider;

      .header-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: @nordic-text-base;
        font-weight: 600;
        color: @merchant-text;
      }
    }

    .detail-content {
      margin-bottom: 16px;

      .order-basic-info {
        background: linear-gradient(135deg, @merchant-surface-alt 0%, @merchant-surface 100%);
        padding: 14px;
        border-radius: @nordic-radius-md;
        margin-bottom: 12px;
        border: 1px solid @merchant-border;

        .info-row {
          display: flex;
          justify-content: space-between;
          padding: 6px 0;
          font-size: @nordic-text-sm;

          .label {
            color: @merchant-text-sec;
            font-weight: 500;
          }

          .value {
            color: @merchant-text;
            font-weight: 600;
          }
        }
      }

      .order-items {
        background: @merchant-surface;
        padding: 14px;
        border-radius: @nordic-radius-md;
        margin-bottom: 12px;
        border: 1px solid @merchant-border;

        .items-title {
          font-size: @nordic-text-sm;
          font-weight: 600;
          color: @merchant-text;
          margin-bottom: 10px;
          padding-bottom: 8px;
          border-bottom: 1px solid @merchant-divider;
        }

        .item-row {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 8px 0;
          font-size: @nordic-text-sm;
          border-bottom: 1px dashed @merchant-divider;

          &:last-child {
            border-bottom: none;
          }

          .item-name {
            flex: 1;
            color: @merchant-text;
            font-weight: 500;
          }

          .item-quantity {
            color: @merchant-text-sec;
            margin: 0 12px;
            font-weight: 500;
          }

          .item-price {
            color: @merchant-secondary;
            font-weight: 700;
          }
        }
      }

      .order-total {
        background: linear-gradient(135deg, @merchant-warning-light 0%, @nordic-yellow-light 100%);
        padding: 14px;
        border-radius: @nordic-radius-md;
        border: 1px solid @merchant-warning;

        .total-row {
          display: flex;
          justify-content: space-between;
          align-items: center;

          .label {
            font-size: @nordic-text-base;
            color: @merchant-text;
            font-weight: 600;
          }

          .value {
            font-size: 1.286rem /* 原值: 18px */;
            color: @merchant-secondary;
            font-weight: 700;
          }
        }
      }
    }

    .detail-actions {
      .actions-title {
        font-size: @nordic-text-sm;
        font-weight: 600;
        color: @merchant-text-sec;
        margin-bottom: 10px;
      }

      .action-buttons {
        display: flex;
        gap: 10px;
        flex-wrap: wrap;

        :deep(.el-button) {
          font-weight: 500;
        }
      }
    }
  }
}
</style>
