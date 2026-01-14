<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import { API_CONFIG } from '../../config'
import { ElMessage } from 'element-plus'
import CommonBackButton from '../../components/common/CommonBackButton.vue'

const route = useRoute()
const orderId = ref(route.params.id)
const order = ref(null)
const loading = ref(true)

// 订单状态映射
const orderStatusMap = {
  all: '全部订单',
  processing: '进行中',
  pending: '待确认',
  pendingComment: '待评价',
  delivered: '已送达',
  completed: '已完成',
  cancelled: '已取消'
}

// 订单状态标签样式映射
const statusTagTypeMap = {
  processing: 'warning',
  pending: 'primary',
  pendingComment: 'info',
  delivered: 'success',
  completed: 'success',
  cancelled: 'danger'
}

// 将后端状态码转换为前端状态文本
const orderStatusToText = (statusCode) => {
  const statusMap = {
    0: 'pending',        // 待支付
    1: 'processing',    // 待接单
    2: 'processing',    // 备菜中
    3: 'processing',    // 烹饪中
    4: 'processing',    // 待上菜
    5: 'delivered',      // 已送达
    6: 'cancelled'       // 已取消
  }
  return statusMap[statusCode] || 'pending'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 加载订单详情(包含菜品信息)
const loadOrderDetail = async () => {
  loading.value = true
  try {
    // 1. 获取订单基本信息
    const orderResponse = await axios.get(
      API_CONFIG.baseURL + API_CONFIG.order.detail + orderId.value
    )

    if (!orderResponse.data?.data) {
      throw new Error('订单不存在')
    }

    const orderData = orderResponse.data.data

    // 2. 获取订单菜品信息
    const dishesResponse = await axios.get(
      `${API_CONFIG.baseURL}/v1/orders/${orderData.id}/dishes`
    )

    // 3. 获取菜品详情
    let items = []
    if (dishesResponse.data?.data && dishesResponse.data.data.length > 0) {
      items = await Promise.all(
        dishesResponse.data.data.map(async (orderDish) => {
          try {
            const dishResponse = await axios.get(
              `${API_CONFIG.baseURL}/dishes/${orderDish.dishId}`
            )
            const dish = dishResponse.data?.data
            return {
              name: dish?.name || `菜品${orderDish.dishId}`,
              quantity: orderDish.quantity,
              price: orderDish.price,
              customization: orderDish.customization,
              image: dish?.image || ''
            }
          } catch (error) {
            console.error(`获取菜品${orderDish.dishId}详情失败:`, error)
            return {
              name: `菜品${orderDish.dishId}`,
              quantity: orderDish.quantity,
              price: orderDish.price,
              image: ''
            }
          }
        })
      )
    }

    // 4. 组装订单数据
    order.value = {
      id: orderData.id,
      orderNo: orderData.id,
      status: orderStatusToText(orderData.status),
      merchant: `商家${orderData.merchantId}`,
      total: orderData.totalAmount,
      time: formatTime(orderData.createTime),
      items: items,
      itemCount: items.reduce((sum, item) => sum + item.quantity, 0),
      _raw: orderData
    }
  } catch (error) {
    console.error('加载订单详情失败:', error)
    ElMessage.error('加载订单详情失败')
    order.value = null
  } finally {
    loading.value = false
  }
}

// 组件挂载时加载订单详情
onMounted(() => {
  loadOrderDetail()
})

// 取消订单
const cancelOrder = (order) => {
  // 调用后端API取消订单
  axios.put(API_CONFIG.baseURL + API_CONFIG.order.detail + order.id + '/cancel')
    .then((response) => {
      if (response.data.success) {
        order.status = 'cancelled'
        ElMessage.success('订单已取消')
      } else {
        ElMessage.error(response.data.message || '取消订单失败')
      }
    })
    .catch((error) => {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败，请稍后重试')
    })
}
</script>

<template>
  <div class="order-detail-container">
    <div class="page-header">
      <CommonBackButton />
      <h2 style="margin-left: 15px">订单详情</h2>
    </div>

    <div class="order-detail-card" v-loading="loading" element-loading-text="加载中...">
      <div v-if="order" class="order-info">
        <!-- 订单基本信息 -->
        <div class="order-base-info">
          <div class="order-no">订单号: {{ order.orderNo }}</div>
          <div class="order-status">
            <el-tag :type="statusTagTypeMap[order.status]">
              {{ orderStatusMap[order.status] }}
            </el-tag>
          </div>
        </div>

        <el-divider />

        <!-- 商家信息 -->
        <div class="merchant-info">
          <div class="merchant-title">商家信息</div>
          <div class="merchant-name">{{ order.merchant }}</div>
        </div>

        <el-divider />

        <!-- 商品列表 -->
        <div class="product-list">
          <div class="product-title">商品列表 (共{{ order.itemCount || 0 }}件)</div>
          <div v-if="order.items && order.items.length > 0">
            <div class="product-item" v-for="(item, index) in order.items" :key="index">
              <div class="product-details">
                <div class="product-name">{{ item.name }}</div>
                <div class="product-meta">
                  <span class="product-quantity">x{{ item.quantity }}</span>
                  <span class="product-price">¥{{ item.price }} × {{ item.quantity }} = ¥{{ (item.price * item.quantity).toFixed(2) }}</span>
                </div>
                <div v-if="item.customization" class="product-customization">
                  备注: {{ item.customization }}
                </div>
              </div>
            </div>
          </div>
          <div v-else class="product-empty">
            暂无商品信息
          </div>
        </div>

        <el-divider />

        <!-- 订单金额 -->
        <div class="order-total">
          <div class="total-text">总金额:</div>
          <div class="total-amount">¥{{ order.total ? order.total.toFixed(2) : '0.00' }}</div>
        </div>

        <el-divider />

        <!-- 订单时间 -->
        <div class="order-time">
          <div class="time-text">下单时间:</div>
          <div class="time-value">{{ order.time || '-' }}</div>
        </div>

        <div class="order-actions">
          <!-- 评价按钮 -->
          <el-button
            v-if="order.status === 'pendingComment'"
            type="success"
            size="small"
            @click="$router.push(`/user/home/evaluate-order/${order.id}`)"
          >
            去评价
          </el-button>

          <!-- 取消订单按钮 -->
          <el-button
            v-if="order.status === 'processing'"
            type="danger"
            size="small"
            @click="cancelOrder(order)"
          >
            取消订单
          </el-button>
        </div>
      </div>
    </div>

    <!-- 空数据提示 -->
    <el-empty v-if="!order && !loading" description="暂无订单详情"></el-empty>
  </div>
</template>

<style scoped lang="less">
.order-detail-container {
  padding: 0 20px 20px 20px;

  .page-header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
  }

  h2 {
    font-size: 24px;
    margin: 0;
  }

  .order-detail-card {
    .order-base-info {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      .order-no {
        font-size: 18px;
        font-weight: bold;
      }

      .order-status {
        margin-left: 20px;
      }
    }

    .merchant-info {
      margin-bottom: 20px;

      .merchant-title {
        font-weight: bold;
        margin-bottom: 10px;
      }

      .merchant-name {
        font-size: 16px;
        color: #606266;
      }
    }

    .product-list {
      margin-bottom: 20px;

      .product-title {
        font-weight: bold;
        margin-bottom: 10px;
        font-size: 16px;
      }

      .product-item {
        margin-bottom: 12px;
        padding: 12px;
        background-color: #f5f7fa;
        border-radius: 6px;
        transition: all 0.2s;

        &:hover {
          background-color: #e8f0fe;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }

        .product-details {
          .product-name {
            font-size: 15px;
            font-weight: 500;
            color: #303133;
            margin-bottom: 8px;
          }

          .product-meta {
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 14px;

            .product-quantity {
              color: #606266;
            }

            .product-price {
              color: #ff6b6b;
              font-weight: bold;
            }
          }

          .product-customization {
            margin-top: 8px;
            padding: 6px 10px;
            background: #fff9e6;
            border-radius: 4px;
            font-size: 12px;
            color: #856404;
          }
        }
      }

      .product-empty {
        text-align: center;
        padding: 20px;
        color: #909399;
        font-size: 14px;
      }
    }

    .order-total {
      display: flex;
      justify-content: flex-end;
      align-items: center;
      margin-bottom: 20px;

      .total-text {
        margin-right: 10px;
        color: #606266;
      }

      .total-amount {
        font-size: 20px;
        font-weight: bold;
        color: #ff6b6b;
      }
    }

    .order-time {
      display: flex;
      align-items: center;
      margin-bottom: 20px;

      .time-text {
        margin-right: 10px;
        color: #606266;
      }

      .time-value {
        font-size: 14px;
      }
    }

    .order-actions {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      margin-top: 20px;
    }
  }
}

@media (max-width: 768px) {
  .order-detail-container {
    padding: 0 10px 10px 10px;

    .order-detail-card {
      .order-base-info {
        flex-direction: column;
        align-items: flex-start !important;

        .order-status {
          margin-left: 0;
          margin-top: 10px;
        }
      }

      .order-actions {
        flex-direction: column;
        gap: 8px;

        el-button {
          width: 100%;
        }
      }
    }
  }
}
</style>
