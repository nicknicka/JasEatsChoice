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

// 订单状态映射
const orderStatusMap = {
  1: { text: '待接单', type: 'danger', color: '#f56c6c' },
  2: { text: '备菜中', type: 'warning', color: '#e6a23c' },
  3: { text: '烹饪中', type: 'warning', color: '#ff9800' },
  4: { text: '待上菜', type: 'primary', color: '#409eff' },
  5: { text: '已完成', type: 'success', color: '#67c23a' }
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
.order-info-panel-wrapper {
  margin: 12px;

  .order-summary {
    padding: 16px 20px;
    background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
    border: 1px solid #bae6fd;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(14, 165, 233, 0.1);

    .summary-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .header-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 14px;
        font-weight: 600;
        color: #0369a1;
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
          font-size: 12px;
          color: #64748b;
          font-weight: 500;
        }

        .value {
          font-size: 14px;
          color: #0c4a6e;
          font-weight: 600;

          &.amount {
            color: #ea580c;
          }
        }
      }
    }
  }

  .order-detail {
    padding: 16px;
    background: #ffffff;
    border: 1px solid #e8eef5;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      padding-bottom: 12px;
      border-bottom: 1px solid #e8eef5;

      .header-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 14px;
        font-weight: 600;
        color: #1f2937;
      }
    }

    .detail-content {
      margin-bottom: 16px;

      .order-basic-info {
        background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
        padding: 14px;
        border-radius: 10px;
        margin-bottom: 12px;
        border: 1px solid #e8eef5;

        .info-row {
          display: flex;
          justify-content: space-between;
          padding: 6px 0;
          font-size: 13px;

          .label {
            color: #6b7280;
            font-weight: 500;
          }

          .value {
            color: #1f2937;
            font-weight: 600;
          }
        }
      }

      .order-items {
        background: #ffffff;
        padding: 14px;
        border-radius: 10px;
        margin-bottom: 12px;
        border: 1px solid #e8eef5;

        .items-title {
          font-size: 13px;
          font-weight: 600;
          color: #1f2937;
          margin-bottom: 10px;
          padding-bottom: 8px;
          border-bottom: 1px solid #e8eef5;
        }

        .item-row {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 8px 0;
          font-size: 13px;
          border-bottom: 1px dashed #e8eef5;

          &:last-child {
            border-bottom: none;
          }

          .item-name {
            flex: 1;
            color: #374151;
            font-weight: 500;
          }

          .item-quantity {
            color: #6b7280;
            margin: 0 12px;
            font-weight: 500;
          }

          .item-price {
            color: #ea580c;
            font-weight: 700;
          }
        }
      }

      .order-total {
        background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
        padding: 14px;
        border-radius: 10px;
        border: 1px solid #fbbf24;

        .total-row {
          display: flex;
          justify-content: space-between;
          align-items: center;

          .label {
            font-size: 14px;
            color: #78350f;
            font-weight: 600;
          }

          .value {
            font-size: 18px;
            color: #b45309;
            font-weight: 700;
          }
        }
      }
    }

    .detail-actions {
      .actions-title {
        font-size: 13px;
        font-weight: 600;
        color: #6b7280;
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
