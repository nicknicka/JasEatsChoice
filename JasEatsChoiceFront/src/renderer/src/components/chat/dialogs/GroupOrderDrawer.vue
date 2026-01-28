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
            :type="getOrderStatusType(groupOrder?.status)"
            size="default"
            effect="dark"
          >
            {{ getOrderStatusText(groupOrder?.status) }}
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

      <!-- 快速点餐入口（仅草稿和进行中状态显示） -->
      <div
        class="quick-order-entry"
        v-if="['draft', 'active'].includes(groupOrder.status)"
      >
        <div class="quick-order-card">
          <div class="quick-order-content">
            <el-icon :size="24" color="#67c23a"><Plus /></el-icon>
            <div class="quick-order-text">
              <div class="quick-order-title">{{ hasMerchant ? '选择菜品' : '开始点餐' }}</div>
              <div class="quick-order-desc">{{ hasMerchant ? '浏览并添加商品' : '选择商家并开始点餐' }}</div>
            </div>
          </div>
          <el-button
            type="success"
            size="default"
            @click="$emit('select-merchant')"
            class="quick-order-btn"
          >
            <el-icon><ShoppingCart /></el-icon> {{ hasMerchant ? '选择菜品' : '选择商家' }}
          </el-button>
        </div>
      </div>

      <!-- 商品列表 -->
      <div class="order-items-section">
        <div class="section-header">
          <div class="section-title">
            <el-icon :size="18" color="#409eff"><ShoppingCart /></el-icon>
            <span>已选商品 ({{ groupOrder.orderItems?.length || 0 }})</span>
          </div>
        </div>

        <div v-if="groupOrder.orderItems && groupOrder.orderItems.length > 0" class="order-items-list">
          <div
            v-for="(item, index) in groupOrder.orderItems"
            :key="item.id || index"
            class="order-item-card"
          >
            <div class="item-image">
              <img
                v-if="item.productImage"
                :src="item.productImage"
                :alt="item.productName"
              />
              <div v-else class="image-placeholder">
                <el-icon :size="32"><Food /></el-icon>
              </div>
            </div>

            <div class="item-content">
              <div class="item-name">{{ item.productName }}</div>
              <div class="item-price">¥{{ item.productPrice?.toFixed(2) || '0.00' }}</div>

              <!-- 可选食材 -->
              <div v-if="item.optionalIngredients && item.optionalIngredients.length > 0" class="item-ingredients">
                <el-tag
                  v-for="(ing, idx) in item.optionalIngredients"
                  :key="idx"
                  size="small"
                  type="primary"
                  effect="plain"
                >
                  +{{ ing.name || ing }}
                </el-tag>
              </div>

              <!-- 备注 -->
              <div v-if="item.remark" class="item-remark">
                <el-icon :size="12"><Edit /></el-icon>
                {{ item.remark }}
              </div>
            </div>

            <div class="item-actions">
              <div class="item-quantity">×{{ item.quantity }}</div>
              <div class="item-subtotal">¥{{ item.subtotal?.toFixed(2) || '0.00' }}</div>
            </div>
          </div>
        </div>

        <!-- 空状态提示 -->
        <div v-else class="empty-cart">
          <el-empty description="暂无商品，快去点餐吧！">
            <template #image>
              <el-icon :size="64" color="#c0c4cc"><ShoppingCart /></el-icon>
            </template>
          </el-empty>
        </div>
      </div>

      <!-- 已支付订单历史记录 -->
      <div
        v-if="groupOrder.paidOrders && groupOrder.paidOrders.length > 0"
        class="paid-orders-section"
      >
        <div class="section-header">
          <div class="section-title">
            <el-icon :size="18" color="#67c23a"><Clock /></el-icon>
            <span>已支付订单 ({{ groupOrder.paidOrders.length }})</span>
          </div>
        </div>

        <div class="paid-orders-list">
          <div
            v-for="(paidOrder, index) in groupOrder.paidOrders"
            :key="paidOrder.orderId || index"
            class="paid-order-card clickable"
            @click="handleViewOrderDetail(paidOrder.orderId)"
          >
            <div class="paid-order-header">
              <div class="paid-order-info">
                <el-tag :type="paidOrder.status === 'completed' ? '' : 'warning'" size="small">
                  {{ paidOrder.status === 'completed' ? '已完成' : '已支付' }}
                </el-tag>
                <span class="paid-order-time">{{ paidOrder.paymentTime || paidOrder.createTime }}</span>
              </div>
              <div class="paid-order-amount-right">
                <div class="paid-order-amount">¥{{ paidOrder.totalAmount?.toFixed(2) || '0.00' }}</div>
                <el-icon class="click-icon"><Right /></el-icon>
              </div>
            </div>

            <div class="paid-order-items">
              <div
                v-for="(item, idx) in paidOrder.orderItems.slice(0, 3)"
                :key="idx"
                class="paid-order-item"
              >
                <span class="item-name">{{ item.productName }}</span>
                <span class="item-quantity">×{{ item.quantity }}</span>
              </div>
              <div v-if="paidOrder.orderItems.length > 3" class="more-items">
                等 {{ paidOrder.orderItems.length }} 件商品
              </div>
            </div>

            <div v-if="paidOrder.remark" class="paid-order-remark">
              <el-icon><Edit /></el-icon>
              {{ paidOrder.remark }}
            </div>
          </div>
        </div>
      </div>

      <!-- 加菜功能入口（支付后显示） -->
      <div
        class="add-dish-section"
        v-if="['paid', 'completed'].includes(groupOrder.status) && hasMerchant"
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
          <!-- 草稿订单：显示取消订单和去支付按钮 -->
          <template v-if="isDraftOrder || groupOrder.status === 'active'">
            <el-button
              v-if="isDraftOrder"
              type="danger"
              size="default"
              @click="handleCancelGroupOrder"
            >
              <el-icon><Delete /></el-icon>
              取消订单
            </el-button>

            <el-button size="default" @click="$emit('select-merchant')">
              <el-icon><Shop /></el-icon>
              {{ hasMerchant ? '去点菜' : '选择商家' }}
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
          </template>

          <!-- 已支付订单：显示继续点餐按钮 -->
          <template v-else-if="['paid', 'completed'].includes(groupOrder.status)">
            <el-button
              type="info"
              size="default"
              @click="$emit('view-history')"
            >
              <el-icon><Clock /></el-icon>
              查看历史
            </el-button>
            <el-button
              type="primary"
              size="default"
              @click="$emit('continue-order')"
            >
              <el-icon><Plus /></el-icon>
              继续点餐
            </el-button>
          </template>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  ShoppingCart,
  InfoFilled,
  Refresh,
  Plus,
  Shop,
  Wallet,
  Dish,
  DocumentChecked,
  Food,
  Edit,
  Delete,
  Clock,
  Right
} from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const router = useRouter()

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
  'open-pending-payment',
  'cancel-group-order',
  'view-history',
  'create-new-order'
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

// 是否为草稿订单
const isDraftOrder = computed(() => {
  return props.groupOrder && props.groupOrder.draftStatus === -1
})

// 获取订单状态文本
const getOrderStatusText = (status) => {
  const statusMap = {
    draft: '草稿',
    active: '进行中',
    paid: '已支付',
    completed: '已完成',
    cancelled: '已取消'
  }
  return statusMap[status] || '未知状态'
}

// 获取订单状态标签类型
const getOrderStatusType = (status) => {
  const typeMap = {
    draft: 'info',
    active: 'success',
    paid: 'warning',
    completed: '',
    cancelled: 'danger'
  }
  return typeMap[status] || 'info'
}

// 跳转到订单详情页
const handleViewOrderDetail = (orderId) => {
  if (!orderId) {
    console.error('订单ID不存在')
    return
  }

  // 判断当前用户角色并跳转到对应的订单详情页
  const currentRole = localStorage.getItem('currentRole')
  if (currentRole === 'merchant') {
    router.push(`/merchant/home/order-detail/${orderId}`)
  } else {
    router.push(`/user/home/order-detail/${orderId}`)
  }
}

// 处理取消群订单
const handleCancelGroupOrder = async () => {
  try {
    await ElMessageBox.confirm(
      '取消后将删除该群订单，所有成员将无法访问。是否继续？',
      '取消群订单',
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '再想想',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )

    emit('cancel-group-order')
  } catch {
    // 用户取消了操作
  }
}
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

  .order-items-section {
    background: white;
    border-radius: 10px;
    padding: 14px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      padding-bottom: 10px;
      border-bottom: 2px solid #f0f0f0;

      .section-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 15px;
        font-weight: 600;
        color: #303133;
      }
    }

    .order-items-list {
      max-height: 400px;
      overflow-y: auto;

      .order-item-card {
        display: flex;
        gap: 12px;
        padding: 12px;
        background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
        border-radius: 8px;
        margin-bottom: 10px;
        border: 1px solid #e4e7ed;
        transition: all 0.3s;

        &:hover {
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
          border-color: #409eff;
        }

        .item-image {
          width: 60px;
          height: 60px;
          flex-shrink: 0;
          border-radius: 6px;
          overflow: hidden;
          background: #f0f0f0;

          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }

          .image-placeholder {
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #c0c4cc;
            background: linear-gradient(135deg, #e4e7ed 0%, #dcdfe6 100%);
          }
        }

        .item-content {
          flex: 1;
          min-width: 0;

          .item-name {
            font-size: 14px;
            font-weight: 600;
            color: #303133;
            margin-bottom: 6px;
            line-height: 1.4;
          }

          .item-price {
            font-size: 13px;
            color: #f56c6c;
            font-weight: 500;
            margin-bottom: 8px;
          }

          .item-ingredients {
            display: flex;
            flex-wrap: wrap;
            gap: 4px;
            margin-bottom: 6px;
          }

          .item-remark {
            font-size: 12px;
            color: #909399;
            display: flex;
            align-items: center;
            gap: 4px;
            padding: 4px 8px;
            background: #f5f7fa;
            border-radius: 4px;
          }
        }

        .item-actions {
          flex-shrink: 0;
          display: flex;
          flex-direction: column;
          align-items: flex-end;
          justify-content: center;
          gap: 6px;

          .item-quantity {
            font-size: 14px;
            font-weight: 600;
            color: #409eff;
          }

          .item-subtotal {
            font-size: 16px;
            font-weight: 700;
            color: #f56c6c;
          }
        }
      }
    }

    .empty-cart {
      padding: 30px 20px;
      text-align: center;
    }
  }

  .paid-orders-section {
    background: white;
    border-radius: 10px;
    padding: 14px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    margin-bottom: 14px;

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      padding-bottom: 10px;
      border-bottom: 2px solid #f0f0f0;

      .section-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 15px;
        font-weight: 600;
        color: #303133;
      }
    }

    .paid-orders-list {
      max-height: 400px;
      overflow-y: auto;

      .paid-order-card {
        background: linear-gradient(135deg, #f0f9ff 0%, #ffffff 100%);
        border-radius: 8px;
        padding: 12px;
        margin-bottom: 10px;
        border: 1px solid #e4e7ed;
        transition: all 0.3s;

        &.clickable {
          cursor: pointer;

          &:hover {
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            border-color: #409eff;
            transform: translateY(-2px);
          }

          &:active {
            transform: translateY(0);
          }
        }

        .paid-order-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 10px;
          padding-bottom: 8px;
          border-bottom: 1px dashed #e4e7ed;

          .paid-order-info {
            display: flex;
            align-items: center;
            gap: 8px;

            .paid-order-time {
              font-size: 12px;
              color: #909399;
            }
          }

          .paid-order-amount-right {
            display: flex;
            align-items: center;
            gap: 8px;

            .paid-order-amount {
              font-size: 16px;
              font-weight: 600;
              color: #67c23a;
            }

            .click-icon {
              color: #409eff;
              font-size: 16px;
              transition: transform 0.3s;
            }
          }
        }

        &.clickable:hover {
          .click-icon {
            transform: translateX(4px);
          }
        }

        .paid-order-items {
          display: flex;
          flex-direction: column;
          gap: 6px;
          margin-bottom: 8px;

          .paid-order-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 13px;
            padding: 4px 8px;
            background: white;
            border-radius: 4px;

            .item-name {
              color: #606266;
              flex: 1;
            }

            .item-quantity {
              color: #909399;
              font-size: 12px;
            }
          }

          .more-items {
            font-size: 12px;
            color: #409eff;
            text-align: center;
            padding: 4px;
            background: #ecf5ff;
            border-radius: 4px;
          }
        }

        .paid-order-remark {
          font-size: 12px;
          color: #909399;
          display: flex;
          align-items: center;
          gap: 4px;
          padding: 6px 8px;
          background: #f5f7fa;
          border-radius: 4px;
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
        height: 40px;
        margin: 0;
        border: 1px solid;

        // 重置默认样式
        :deep(.el-button__content) {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 6px;
          width: 100%;
          height: 100%;
        }

        :deep(.el-icon) {
          font-size: 16px;
          display: inline-flex;
          align-items: center;
          justify-content: center;
        }

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
        border-color: #e6a23c;

        &:hover {
          background: #fff7e6;
          color: #e6a23c;
          border-color: #d9983b;
        }
      }

      .review-btn {
        background: white;
        color: #409eff;
        border-color: #409eff;
        font-weight: 600;

        &:hover {
          background: #ecf5ff;
          color: #409eff;
          border-color: #409eff;
        }
      }

      .payment-btn {
        background: white;
        color: #67c23a;
        border-color: #67c23a;

        &:hover {
          background: #f0f9ff;
          color: #67c23a;
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
