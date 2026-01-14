<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import { API_CONFIG, WS_CONFIG } from '../../config'
import { ElMessage } from 'element-plus'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import { Refresh, ArrowDown, EditPen } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 用户订单数据
const orders = ref([])

// 加载状态
const loading = ref(false)
// 刷新动画状态
const isRefreshing = ref(false)
// 展开的订单ID集合
const expandedOrderIds = ref(new Set())

// 切换订单展开状态
const toggleOrderExpand = (orderId) => {
  if (expandedOrderIds.value.has(orderId)) {
    expandedOrderIds.value.delete(orderId)
  } else {
    expandedOrderIds.value.add(orderId)
  }
  // 触发响应式更新
  expandedOrderIds.value = new Set(expandedOrderIds.value)
}

// 判断订单是否展开
const isOrderExpanded = (orderId) => {
  return expandedOrderIds.value.has(orderId)
}

// 获取订单显示的商品列表（超过3个商品时，收起状态只显示前3个）
const getDisplayItems = (order) => {
  const maxDisplay = 3
  if (isOrderExpanded(order.id) || order.items.length <= maxDisplay) {
    return order.items
  }
  return order.items.slice(0, maxDisplay)
}

// 判断是否有更多商品
const hasMoreItems = (order) => {
  return order.items.length > 3
}

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
                      image: dish?.image || '',
                      // 添加可选食材和备注信息
                      optionalIngredients: dish?.optionalIngredients || [],
                      requiredIngredients: dish?.requiredIngredients || [],
                      dishNote: orderDish.note || ''
                    }
                  } catch (error) {
                    console.error(`获取菜品${orderDish.dishId}详情失败:`, error)
                    return {
                      name: `菜品${orderDish.dishId}`,
                      quantity: orderDish.quantity,
                      price: orderDish.price,
                      image: '',
                      optionalIngredients: [],
                      requiredIngredients: [],
                      dishNote: ''
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
const statusList = ref(['all', 'pendingAccept', 'processing', 'pending', 'pendingComment', 'delivered', 'completed', 'cancelled'])

// 订单状态映射
const orderStatusMap = {
  all: '全部订单',
  pendingAccept: '待接单',
  processing: '进行中',
  pending: '待确认',
  pendingComment: '待评价',
  delivered: '已送达',
  completed: '已完成',
  cancelled: '已取消'
}

// 订单状态标签样式映射
const statusTagTypeMap = {
  pendingAccept: 'warning',
  processing: 'primary',
  pending: 'info',
  pendingComment: 'info',
  delivered: 'success',
  completed: 'success',  // 已完成订单使用绿色更合理
  cancelled: 'danger'
}

// 将后端状态码转换为前端状态文本
const orderStatusToText = (statusCode) => {
  const statusMap = {
    0: 'pending',        // 待支付
    1: 'pendingAccept',  // 待接单
    2: 'processing',     // 备菜中
    3: 'processing',     // 烹饪中
    4: 'processing',     // 待上菜
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
            <div class="header-left">
              <span class="item-count">共 {{ order.itemCount || 0 }} 件商品</span>
              <el-tag v-if="order.itemCount > 0" type="info" size="small" class="item-tag">
                {{ order.itemCount }} 种菜品
              </el-tag>
            </div>
            <el-button
              v-if="hasMoreItems(order)"
              type="primary"
              link
              size="small"
              @click="toggleOrderExpand(order.id)"
              class="expand-btn"
            >
              {{ isOrderExpanded(order.id) ? '收起' : `展开全部 (${order.itemCount}件)` }}
              <el-icon :class="{ 'expand-icon': true, 'expanded': isOrderExpanded(order.id) }">
                <ArrowDown />
              </el-icon>
            </el-button>
          </div>

          <div class="items-list">
            <div
              v-for="(item, index) in getDisplayItems(order)"
              :key="index"
              class="item-row"
              @click="viewOrderDetails(order)"
            >
              <!-- 商品图片 -->
              <div class="item-image">
                <img
                  v-if="item.image"
                  :src="item.image"
                  :alt="item.name"
                  loading="lazy"
                  @error="handleImageError($event)"
                />
                <div v-else class="no-image">
                  <span>{{ item.name?.charAt(0) || '菜' }}</span>
                </div>
                <!-- 数量徽章 -->
                <div class="quantity-badge">×{{ item.quantity }}</div>
              </div>

              <!-- 商品信息 -->
              <div class="item-info">
                <div class="item-name">{{ item.name }}</div>

                <!-- 必选食材 -->
                <div v-if="item.requiredIngredients && item.requiredIngredients.length > 0" class="item-ingredients">
                  <div class="ingredients-label">
                    <span class="label-text">必选:</span>
                  </div>
                  <div class="ingredients-list">
                    <span class="ingredient-tag required" v-for="ing in item.requiredIngredients" :key="ing">
                      {{ ing }}
                    </span>
                  </div>
                </div>

                <!-- 可选食材 -->
                <div v-if="item.optionalIngredients && item.optionalIngredients.length > 0" class="item-ingredients">
                  <div class="ingredients-label">
                    <span class="label-text">可选:</span>
                  </div>
                  <div class="ingredients-list">
                    <span
                      class="ingredient-tag optional"
                      v-for="ing in item.optionalIngredients"
                      :key="ing.id || ing.name"
                    >
                      {{ ing.name }}
                      <span v-if="ing.price" class="ingredient-price">+¥{{ ing.price.toFixed(2) }}</span>
                    </span>
                  </div>
                </div>

                <!-- 备注信息 -->
                <div v-if="item.dishNote" class="item-note">
                  <el-icon><EditPen /></el-icon>
                  <span class="note-text">{{ item.dishNote }}</span>
                </div>

                <!-- 自定义信息（兼容旧数据） -->
                <div v-if="item.customization && !item.dishNote" class="item-customization">
                  <el-icon><EditPen /></el-icon>
                  <el-tooltip
                    :content="item.customization"
                    placement="top"
                    :show-after="500"
                  >
                    <span class="customization-text">{{ item.customization }}</span>
                  </el-tooltip>
                </div>

                <div class="item-price-detail">
                  <span class="unit-price">¥{{ item.price.toFixed(2) }} /份</span>
                  <span class="total-price">小计 ¥{{ (item.price * item.quantity).toFixed(2) }}</span>
                </div>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-if="!order.items || order.items.length === 0" class="items-empty">
              <el-empty description="暂无商品信息" :image-size="60" />
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
            v-if="order.status === 'pendingAccept'"
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
  background: linear-gradient(180deg, #f0f7ff 0%, #ffffff 100%);
  min-height: calc(100vh - 80px);

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
      background: linear-gradient(135deg, #e3f2fd 0%, #f0f7ff 100%);
      border-radius: 8px;
      padding: 12px;

      .items-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 10px;
        padding-bottom: 8px;
        border-bottom: 1px solid #bbdefb;

        .header-left {
          display: flex;
          align-items: center;
          gap: 8px;
        }

        .item-count {
          font-size: 14px;
          color: #606266;
          font-weight: 500;
        }

        .item-tag {
          font-size: 12px;
        }

        .expand-btn {
          font-size: 13px;
          padding: 4px 8px;
          height: auto;

          .expand-icon {
            transition: transform 0.3s ease;
            margin-left: 2px;

            &.expanded {
              transform: rotate(180deg);
            }
          }
        }
      }

      .items-list {
        display: flex;
        flex-direction: column;
        gap: 10px;
      }

      .item-row {
        display: flex;
        align-items: stretch;
        padding: 12px;
        background: white;
        border-radius: 8px;
        transition: all 0.2s;
        position: relative;
        border: 1px solid #e3f2fd;
        min-height: 94px;
        cursor: pointer;

        &:hover {
          box-shadow: 0 4px 16px rgba(33, 150, 243, 0.15);
          transform: translateY(-2px);
          border-color: #90caf9;
        }

        &:active {
          transform: translateY(0);
          box-shadow: 0 2px 8px rgba(33, 150, 243, 0.15);
        }

        .item-image {
          width: 70px;
          min-height: 70px;
          height: 70px;
          border-radius: 8px;
          overflow: visible;
          margin-right: 14px;
          flex-shrink: 0;
          background: linear-gradient(135deg, #64b5f6 0%, #42a5f5 100%);
          position: relative;
          box-shadow: 0 2px 8px rgba(33, 150, 243, 0.2);
          display: flex;
          align-items: center;
          justify-content: center;

          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            border-radius: 8px;
          }

          .no-image {
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;

            span {
              font-size: 28px;
              font-weight: bold;
              color: white;
            }
          }

          .quantity-badge {
            position: absolute;
            top: -6px;
            right: -6px;
            background: linear-gradient(135deg, #ff5252 0%, #ff1744 100%);
            color: white;
            font-size: 11px;
            font-weight: 600;
            width: 20px;
            height: 20px;
            border-radius: 50%;
            box-shadow: 0 2px 6px rgba(255, 82, 82, 0.4);
            z-index: 10;
            display: flex;
            align-items: center;
            justify-content: center;
            line-height: 1;
          }
        }

        .item-info {
          flex: 1;
          min-width: 0;
          display: flex;
          flex-direction: column;
          justify-content: space-between;

          .item-name {
            font-size: 15px;
            font-weight: 600;
            color: #303133;
            margin-bottom: 6px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            flex-shrink: 0;
          }

          .item-ingredients {
            display: flex;
            align-items: flex-start;
            gap: 6px;
            margin-bottom: 6px;
            font-size: 12px;

            .ingredients-label {
              flex-shrink: 0;

              .label-text {
                color: #606266;
                font-weight: 500;
              }
            }

            .ingredients-list {
              display: flex;
              flex-wrap: wrap;
              gap: 4px;
              flex: 1;

              .ingredient-tag {
                display: inline-flex;
                align-items: center;
                gap: 2px;
                padding: 2px 6px;
                border-radius: 3px;
                font-size: 11px;
                line-height: 1.4;

                &.required {
                  background: linear-gradient(135deg, #67c23a 0%, #5daf34 100%);
                  color: white;
                  font-weight: 500;
                }

                &.optional {
                  background: #e8f4e8;
                  color: #67c23a;
                  border: 1px solid #b3e19d;
                }

                .ingredient-price {
                  font-size: 10px;
                  opacity: 0.9;
                  margin-left: 2px;
                }
              }
            }
          }

          .item-note {
            display: flex;
            align-items: flex-start;
            gap: 4px;
            margin-bottom: 6px;
            font-size: 12px;
            color: #e6a23c;
            padding: 4px 8px;
            background: #fdf6ec;
            border-radius: 4px;
            border: 1px solid #f5dab1;
            line-height: 1.4;

            .el-icon {
              font-size: 13px;
              color: #e6a23c;
              flex-shrink: 0;
              margin-top: 1px;
            }

            .note-text {
              flex: 1;
              word-break: break-word;
            }
          }

          .item-customization {
            font-size: 13px;
            color: #1976d2;
            margin-bottom: 6px;
            padding: 4px 8px;
            background: #e3f2fd;
            border-radius: 4px;
            border: 1px solid #bbdefb;
            line-height: 1.4;
            max-height: 40px;
            overflow: hidden;
            display: flex;
            align-items: flex-start;
            gap: 4px;

            .customization-text {
              flex: 1;
              overflow: hidden;
              text-overflow: ellipsis;
              display: -webkit-box;
              -webkit-line-clamp: 2;
              line-clamp: 2;
              -webkit-box-orient: vertical;
              word-break: break-word;
            }

            .el-icon {
              font-size: 14px;
              color: #2196f3;
              flex-shrink: 0;
              margin-top: 1px;
            }
          }

          .item-price-detail {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-top: auto;
            flex-shrink: 0;

            .unit-price {
              font-size: 13px;
              color: #909399;
            }

            .total-price {
              font-size: 15px;
              font-weight: bold;
              color: #ff6b6b;
            }
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
      background: linear-gradient(180deg, #f5f9ff 0%, #ffffff 100%);
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

        .items-header {
          .header-left {
            gap: 6px;
          }

          .expand-btn {
            font-size: 12px;
          }
        }

        .item-row {
          padding: 10px;
          min-height: 80px;

          .item-image {
            width: 56px;
            min-height: 56px;
            height: 56px;
            margin-right: 10px;
            border-radius: 6px;
            overflow: visible;

            img {
              border-radius: 6px;
            }

            .quantity-badge {
              width: 18px;
              height: 18px;
              font-size: 10px;
              top: -5px;
              right: -5px;
            }

            .no-image span {
              font-size: 22px;
            }
          }

          .item-info {
            .item-name {
              font-size: 14px;
            }

            .item-ingredients {
              font-size: 11px;
              margin-bottom: 4px;

              .ingredients-list {
                .ingredient-tag {
                  font-size: 10px;
                  padding: 2px 4px;
                }
              }
            }

            .item-note {
              font-size: 11px;
              padding: 3px 6px;

              .el-icon {
                font-size: 12px;
              }
            }

            .item-customization {
              font-size: 12px;
              padding: 3px 6px;
              max-height: 36px;
              min-height: 28px;

              .el-icon {
                font-size: 12px;
              }
            }

            .item-price-detail {
              flex-direction: column;
              align-items: flex-start;
              gap: 4px;

              .unit-price {
                font-size: 12px;
              }

              .total-price {
                font-size: 14px;
              }
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
