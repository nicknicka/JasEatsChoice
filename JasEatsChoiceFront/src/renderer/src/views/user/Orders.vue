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
const loadOrders = () => {
  return new Promise((resolve, reject) => {
    // 显示加载状态
    loading.value = true

    const userId = parseInt(localStorage.getItem('userId') || '1', 10) // 临时使用固定用户ID

    axios
      .get(API_CONFIG.baseURL + API_CONFIG.order.list + userId)
      .then((response) => {
        if (response.data.data) {
          // 转换后端Order实体到前端期望的格式
          orders.value = response.data.data.map(order => ({
            id: order.id,
            orderNo: order.id, // 使用订单ID作为订单号
            status: orderStatusToText(order.status), // 转换状态码为文本
            merchant: `商家${order.merchantId}`, // TODO: 从商家API获取商家名称
            total: order.totalAmount, // totalAmount -> total
            time: formatTime(order.createTime), // 格式化时间
            items: order.items || [], // TODO: 从订单详情获取商品列表
            // 保留原始数据用于详情查看
            _raw: order
          }))
        }
      })
      .catch((error) => {
        console.error('加载订单失败:', error)
        // 使用默认数据作为 fallback
        orders.value = [
          {
            id: 1,
          orderNo: 'JD20231123001',
          status: 'delivered',
          merchant: '健康轻食馆',
          total: 28.8,
          time: '2023-11-23 12:30',
          items: ['健康轻食套餐', '矿泉水']
        },
        {
          id: 2,
          orderNo: 'JD20231123002',
          status: 'processing',
          merchant: '营养早餐店',
          total: 15.5,
          time: '2023-11-23 10:15',
          items: ['营养早餐组合']
        },
        {
          id: 3,
          orderNo: 'JD20231122001',
          status: 'completed',
          merchant: '美食天地',
          total: 42.0,
          time: '2023-11-22 18:45',
          items: ['宫保鸡丁', '麻婆豆腐', '米饭']
        },
        {
          id: 4,
          orderNo: 'JD20231121001',
          status: 'cancelled',
          merchant: '健身餐厅',
          total: 35.0,
          time: '2023-11-21 19:30',
          items: ['健身餐套餐']
        },
        {
          id: 5,
          orderNo: 'JD20231124001',
          status: 'pending',
          merchant: '中式快餐',
          total: 32.0,
          time: '2023-11-24 12:00',
          items: ['红烧肉盖饭', '青菜豆腐汤']
        },
        {
          id: 6,
          orderNo: 'JD20231124002',
          status: 'pendingComment',
          merchant: '西式餐厅',
          total: 68.0,
          time: '2023-11-24 18:30',
          items: ['牛排套餐', '柠檬红茶']
        }
      ]
      ElMessage.error('加载订单失败，将显示默认数据')
    })
    .finally(() => {
      // 隐藏加载状态
      loading.value = false
      resolve()
    })
  })
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

        <div class="order-items">
          <div class="item-title">商品:</div>
          <div class="item-list">
            <el-tag v-for="item in order.items" :key="item" type="info" size="small">{{
              item
            }}</el-tag>
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

      .item-title {
        font-weight: bold;
        margin-bottom: 10px;
      }

      .item-list {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
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
