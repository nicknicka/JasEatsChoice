<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import { API_CONFIG, WS_CONFIG } from '../../config'
import { ElMessage } from 'element-plus'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import { Refresh } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 用户订单数据
const orders = ref([])

// 加载状态
const loading = ref(false)
// 刷新动画状态
const isRefreshing = ref(false)

// 处理刷新点击
const handleRefresh = () => {
  // 启动刷新动画
  isRefreshing.value = true

  // 确保动画至少持续一点时间
  const startTime = Date.now()

  // 调用加载订单函数
  loadOrders().finally(() => {
    const duration = Date.now() - startTime
    // 如果加载太快，延迟关闭动画以提供更好的视觉反馈
    const delay = Math.max(0, 500 - duration)

    setTimeout(() => {
      isRefreshing.value = false
    }, delay)
  })
}

// 加载用户订单数据
const loadOrders = async () => {
  loading.value = true

  try {
    const userId = parseInt(localStorage.getItem('userId') || '1', 10)

    const response = await axios.get(API_CONFIG.baseURL + API_CONFIG.order.list + userId)

    if (response.data.data) {
      // 转换后端Order实体到前端期望的格式，并加载菜品信息
      const ordersWithItems = await Promise.all(
        response.data.data.map(async (order) => {
          try {
            // 获取订单菜品信息
            const dishesResponse = await axios.get(
              `${API_CONFIG.baseURL}/v1/orders/${order.id}/dishes`
            )

            // 处理菜品列表，获取菜品详细信息
            let items = []
            if (dishesResponse.data?.data && dishesResponse.data.data.length > 0) {
              items = await Promise.all(
                dishesResponse.data.data.map(async (orderDish) => {
                  try {
                    // 获取菜品详情
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

            return {
              id: order.id,
              orderNo: order.id,
              status: orderStatusToText(order.status),
              merchant: `商家${order.merchantId}`,
              total: order.totalAmount,
              time: formatTime(order.createTime),
              items: items,
              itemCount: items.reduce((sum, item) => sum + item.quantity, 0),
              _raw: order
            }
          } catch (error) {
            console.error(`加载订单${order.id}的菜品失败:`, error)
            return {
              id: order.id,
              orderNo: order.id,
              status: orderStatusToText(order.status),
              merchant: `商家${order.merchantId}`,
              total: order.totalAmount,
              time: formatTime(order.createTime),
              items: [],
              itemCount: 0,
              _raw: order
            }
          }
        })
      )

      orders.value = ordersWithItems
    }
  } catch (error) {
    console.error('加载订单失败:', error)
    ElMessage.error('加载订单失败，请稍后重试')
    orders.value = []
  } finally {
    loading.value = false
  }
}

// 订单状态筛选
const activeStatus = ref('all')

// 明确按钮顺序的状态列表
const statusList = ref(['all', 'processing', 'pending', 'pendingComment', 'delivered', 'completed', 'cancelled'])

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
  completed: 'success',  // 已完成订单使用绿色更合理
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

// WebSocket实例
const ws = ref(null)

// 组件挂载时加载数据和初始化WebSocket
onMounted(() => {
  // 检查是否有传递的状态参数
  if (route.query.status) {
    activeStatus.value = route.query.status
  }
  loadOrders()

  // 初始化WebSocket连接
  initWebSocket()
})

// 组件卸载时关闭WebSocket连接
onUnmounted(() => {
  if (ws.value) {
    ws.value.close()
  }
})

// 初始化WebSocket
const initWebSocket = () => {
  try {
    ws.value = new WebSocket(WS_CONFIG.url)

    // 连接成功
    ws.value.onopen = () => {
      console.log('WebSocket连接成功')

      // 可以在这里发送用户ID等信息到服务器，以便服务器推送相关订单更新
      // ws.value.send(JSON.stringify({ userId: localStorage.getItem('userId') }))
    }

    // 接收消息
    ws.value.onmessage = (event) => {
      try {
        const orderUpdate = JSON.parse(event.data)

        // 更新本地订单状态
        const index = orders.value.findIndex(order => order.id === orderUpdate.id)
        if (index !== -1) {
          orders.value[index].status = orderUpdate.status

          // 显示更新提示
          const statusText = orderStatusMap[orderUpdate.status] || orderUpdate.status
          ElMessage.info(`订单 ${orders.value[index].orderNo} 状态已更新为: ${statusText}`)
        }
      } catch (error) {
        console.error('解析WebSocket消息失败:', error)
      }
    }

    // 连接关闭
    ws.value.onclose = () => {
      console.log('WebSocket连接关闭')

      // 可以在这里实现重连逻辑
      // setTimeout(() => initWebSocket(), 3000)
    }

    // 连接错误
    ws.value.onerror = (error) => {
      console.error('WebSocket连接错误:', error)
    }
  } catch (error) {
    console.error('初始化WebSocket失败:', error)
    ElMessage.error('WebSocket连接失败，无法接收实时订单更新')
  }
}

// 监听路由参数变化
watch(
  () => route.query.status,
  (newStatus) => {
    if (newStatus) {
      activeStatus.value = newStatus
    }
  }
)

// 筛选后的订单
const filteredOrders = computed(() => {
  if (activeStatus.value === 'all') {
    return orders.value
  }
  return orders.value.filter((order) => order.status === activeStatus.value)
})

// 分页相关
const currentPage = ref(1)
const pageSize = ref(5)

// 分页后的订单
const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredOrders.value.slice(start, end)
})

// 查看订单详情
const viewOrderDetails = (order) => {
  // 导航到订单详情页
  router.push({
    path: `/user/home/order-detail/${order.id}`,
    name: 'user-order-detail',
    params: { id: order.id }
  })
}

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

// 跳转到评价页面
const goToEvaluate = (order) => {
  // 导航到评价页面
  router.push({
    path: `/user/home/evaluate-order/${order.id}`,
    name: 'user-evaluate-order',
    params: { id: order.id }
  })
}

// 处理图片加载错误
const handleImageError = (event) => {
  const img = event.target
  img.style.display = 'none'
  const parent = img.parentElement
  if (parent && !parent.querySelector('.no-image')) {
    const noImageDiv = document.createElement('div')
    noImageDiv.className = 'no-image'
    noImageDiv.innerHTML = '<span>菜</span>'
    parent.appendChild(noImageDiv)
  }
}
</script>

<template>
  <div class="orders-container">
    <div class="page-header">
      <CommonBackButton />
      <h2 style="margin-left: 15px">查看订单</h2>
      <div style="flex: 1; text-align: right">
        <el-button type="default" size="small" @click="handleRefresh" :loading="loading">
          <el-icon :class="{ 'refresh-rotating': isRefreshing }"><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 订单筛选 -->
    <div class="order-filters">
      <el-button
        v-for="status in statusList"
        :key="status"
        type="primary"
        :plain="activeStatus !== status"
        @click="activeStatus = status"
        size="small"
      >
        {{ orderStatusMap[status] }}
      </el-button>
    </div>

    <!-- 订单列表 -->
    <div class="order-list" v-loading="loading" element-loading-text="加载中...">
      <el-card v-for="order in paginatedOrders" :key="order.id" class="order-card">
        <div class="order-header">
          <div class="order-info">
            <div class="order-no">订单号: {{ order.orderNo }}</div>
            <div class="order-merchant">商家: {{ order.merchant }}</div>
            <div class="order-time">时间: {{ order.time }}</div>
          </div>
          <div class="order-status">
            <el-tag :type="statusTagTypeMap[order.status]">
              {{ orderStatusMap[order.status] }}
            </el-tag>
          </div>
        </div>

        <!-- 菜品列表 -->
        <div class="order-items">
          <div class="items-header">
            <span class="item-count">共 {{ order.itemCount || 0 }} 件商品</span>
          </div>

          <div class="items-list">
            <div v-for="(item, index) in order.items" :key="index" class="item-row">
              <!-- 商品图片 -->
              <div class="item-image">
                <img
                  v-if="item.image"
                  :src="item.image"
                  :alt="item.name"
                  @error="handleImageError($event)"
                />
                <div v-else class="no-image">
                  <span>{{ item.name?.charAt(0) || '菜' }}</span>
                </div>
              </div>

              <!-- 商品信息 -->
              <div class="item-info">
                <div class="item-name">{{ item.name }}</div>
                <div v-if="item.customization" class="item-customization">
                  备注: {{ item.customization }}
                </div>
              </div>

              <!-- 数量和价格 -->
              <div class="item-price-info">
                <div class="item-quantity">x{{ item.quantity }}</div>
                <div class="item-price">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-if="!order.items || order.items.length === 0" class="items-empty">
              暂无商品信息
            </div>
          </div>
        </div>

        <div class="order-total">
          <div class="total-text">总金额:</div>
          <div class="total-amount">¥{{ order.total.toFixed(2) }}</div>
        </div>

        <div class="order-actions">
          <el-button type="primary" size="small" @click="viewOrderDetails(order)">
            查看详情
          </el-button>
          <el-button
            v-if="order.status === 'processing'"
            type="danger"
            size="small"
            @click="cancelOrder(order)"
          >
            取消订单
          </el-button>
          <el-button
            v-if="order.status === 'pendingComment'"
            type="success"
            size="small"
            @click="goToEvaluate(order)"
          >
            去评价
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 分页组件 -->
    <el-pagination
      v-if="filteredOrders.length > 0"
      background
      layout="total, sizes, prev, pager, next, jumper"
      :total="filteredOrders.length"
      :current-page="currentPage"
      :page-size="pageSize"
      @current-change="(page) => currentPage = page"
      @size-change="(size) => { pageSize = size; currentPage = 1; }"
      class="order-pagination"
    />

    <!-- 空数据提示 -->
    <el-empty v-if="filteredOrders.length === 0" description="暂无订单记录，快去下单吧！"></el-empty>
  </div>
</template>

<style scoped lang="less">
.orders-container {
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

  .order-filters {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
  }

  .order-list {
    display: flex;
    flex-direction: column;
    gap: 15px;
  }

  .order-pagination {
    margin-top: 20px;
    text-align: center;
  }

  .order-card {
    .order-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 15px;

      .order-info {
        .order-no {
          font-weight: bold;
          margin-bottom: 5px;
        }
        .order-merchant,
        .order-time {
          font-size: 14px;
          color: #606266;
          margin-bottom: 3px;
        }
      }
    }

    .order-items {
      margin-bottom: 15px;
      background: #f8f9fa;
      border-radius: 8px;
      padding: 12px;

      .items-header {
        margin-bottom: 10px;
        padding-bottom: 8px;
        border-bottom: 1px solid #e0e0e0;

        .item-count {
          font-size: 14px;
          color: #606266;
          font-weight: 500;
        }
      }

      .items-list {
        display: flex;
        flex-direction: column;
        gap: 10px;
      }

      .item-row {
        display: flex;
        align-items: center;
        padding: 10px;
        background: white;
        border-radius: 6px;
        transition: all 0.2s;

        &:hover {
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
          transform: translateY(-2px);
        }

        .item-image {
          width: 60px;
          height: 60px;
          border-radius: 6px;
          overflow: hidden;
          margin-right: 12px;
          flex-shrink: 0;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }

          .no-image {
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;

            span {
              font-size: 24px;
              font-weight: bold;
              color: white;
            }
          }
        }

        .item-info {
          flex: 1;
          min-width: 0;

          .item-name {
            font-size: 15px;
            font-weight: 500;
            color: #303133;
            margin-bottom: 4px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .item-customization {
            font-size: 12px;
            color: #909399;
            display: flex;
            align-items: center;

            &::before {
              content: '📝';
              margin-right: 4px;
            }
          }
        }

        .item-price-info {
          text-align: right;
          flex-shrink: 0;

          .item-quantity {
            font-size: 13px;
            color: #909399;
            margin-bottom: 4px;
          }

          .item-price {
            font-size: 16px;
            font-weight: bold;
            color: #ff6b6b;
          }
        }
      }

      .items-empty {
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
      margin-bottom: 15px;

      .total-text {
        margin-right: 10px;
        color: #606266;
      }

      .total-amount {
        font-size: 18px;
        font-weight: bold;
        color: #ff6b6b;
      }
    }

    .order-actions {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
    }
  }

  /* 刷新按钮旋转动画 */
  .refresh-rotating {
    animation: refresh-rotate 0.8s linear infinite;
  }

  @keyframes refresh-rotate {
    0% {
      transform: rotate(0deg);
    }
    100% {
      transform: rotate(360deg);
    }
  }

  /* 响应式设计 */
  @media (max-width: 768px) {
    .orders-container {
      padding: 0 10px 10px 10px;
    }

    .order-filters {
      flex-wrap: wrap;
      gap: 8px;
    }

    .order-card {
      .order-header {
        flex-direction: column;
        align-items: flex-start !important;

        .order-status {
          margin-top: 10px;
        }
      }

      .order-items {
        padding: 10px;

        .item-row {
          padding: 8px;

          .item-image {
            width: 50px;
            height: 50px;
            margin-right: 10px;
          }

          .item-info {
            .item-name {
              font-size: 14px;
            }

            .item-customization {
              font-size: 11px;
            }
          }

          .item-price-info {
            .item-quantity {
              font-size: 12px;
            }

            .item-price {
              font-size: 15px;
            }
          }
        }
      }

      .order-total, .order-actions {
        justify-content: flex-start !important;
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
