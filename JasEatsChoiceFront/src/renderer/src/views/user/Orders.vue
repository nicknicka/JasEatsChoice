<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import OrderSearchBar from './components/OrderSearchBar.vue'
import OrderFilterBar from './components/OrderFilterBar.vue'
import OrderCard from './components/OrderCard.vue'
import ReorderDialog from '../../components/ReorderDialog.vue'
import { useOrderData } from '../../composables/useOrderData'
import { useOrderFilter } from '../../composables/useOrderFilter'
import { useOrderPagination } from '../../composables/useOrderPagination'
import { useOrderWebSocket } from '../../composables/useOrderWebSocket'
import orderApi from '../../api/order'
import axios from 'axios'
import { API_CONFIG } from '../../config'

const router = useRouter()
const route = useRoute()

// 使用 composables
const {
  orders,
  loading,
  isRefreshing,
  refreshSuccess,
  listTransitionName,
  loadOrders,
  handleRefresh,
  handleImageError,
  updateOrderStatus
} = useOrderData()

const {
  activeStatus,
  searchKeyword,
  sortBy,
  statusList,
  sortOptions,
  sortedOrders,
  clearSearch,
  setStatusFilter
} = useOrderFilter(orders)

const {
  currentPage,
  pageSize,
  pageSizeOptions,
  paginatedOrders,
  total,
  handlePageChange,
  handleSizeChange,
  resetToFirstPage
} = useOrderPagination(sortedOrders, () => {
  // 当筛选或排序变化时重置到第一页
})

// WebSocket 订单更新回调
function handleOrderUpdate(orderUpdate) {
  updateOrderStatus(orderUpdate.id, orderUpdate.status)
  // 只更新本地状态，不再重新加载所有订单
  // 重新加载会导致订单显示又消失的问题
}

// 初始化 WebSocket
const { initWebSocket } = useOrderWebSocket(handleOrderUpdate)

/**
 * 查看订单详情
 */
function viewOrderDetails(order) {
  router.push({
    path: `/user/home/order-detail/${order.id}`,
    name: 'user-order-detail',
    params: { id: order.id }
  })
}

/**
 * 取消订单
 */
async function cancelOrder(order) {
  try {
    await axios.put(
      API_CONFIG.baseURL + API_CONFIG.order.detail + order.id + '/cancel'
    )

    order.status = 'cancelled'
    ElMessage.success('订单已取消')
  } catch (error) {
    console.error('取消订单失败:', error)
    ElMessage.error('取消订单失败，请稍后重试')
  }
}

/**
 * 确认收货
 */
async function confirmReceipt(order) {
  console.log('📦 Orders.vue - 开始确认收货流程', {
    orderId: order.id,
    orderNo: order.orderNo,
    currentStatus: order.status,
    statusText: getOrderStatusText(order.status),
    timestamp: new Date().toISOString()
  })

  try {
    await ElMessageBox.confirm('确认已收到餐品并完成订单吗？', '确认收货', {
      confirmButtonText: '确认收货',
      cancelButtonText: '取消',
      type: 'info'
    })

    console.log('📦 Orders.vue - 用户确认，开始调用API', {
      orderId: order.id,
      apiEndpoint: `/v1/orders/${order.id}/status`,
      targetStatus: 7,
      timestamp: new Date().toISOString()
    })

    const response = await orderApi.confirmReceipt(order.id)

    console.log('📦 Orders.vue - API响应', {
      orderId: order.id,
      response: response.data,
      success: response.data?.success,
      timestamp: new Date().toISOString()
    })

    if (response.data.success) {
      order.status = 'pendingComment'
      console.log('✅ Orders.vue - 确认收货成功', {
        orderId: order.id,
        newStatus: order.status,
        statusText: getOrderStatusText(order.status),
        timestamp: new Date().toISOString()
      })
      ElMessage.success('已确认收货，请对订单进行评价')
    } else {
      console.error('❌ Orders.vue - 确认收货失败（业务错误）', {
        orderId: order.id,
        message: response.data.message,
        timestamp: new Date().toISOString()
      })
      ElMessage.error(response.data.message || '确认收货失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('❌ Orders.vue - 确认收货异常', {
        orderId: order.id,
        error: error,
        errorMessage: error.message,
        errorStack: error.stack,
        timestamp: new Date().toISOString()
      })
      ElMessage.error('确认收货失败，请稍后重试')
    }
  }
}

/**
 * 跳转到评价页面
 */
function goToEvaluate(order) {
  router.push({
    path: `/user/home/evaluate-order/${order.id}`,
    name: 'user-evaluate-order',
    params: { id: order.id }
  })
}

/**
 * 跳转到追加评价页面
 */
function goToAdditionalReview(order) {
  router.push({
    path: `/user/home/evaluate-order/${order.id}`,
    name: 'user-evaluate-order',
    params: { id: order.id },
    query: { type: 'additional' }
  })
}

// 再来一单相关状态
const reorderDialogVisible = ref(false)
const currentOrderId = ref('')

/**
 * 再来一单
 */
function handleReorder(order) {
  currentOrderId.value = order.id
  reorderDialogVisible.value = true
}

/**
 * 确认再来一单
 */
function handleReorderConfirm(data) {
  // 保存商家信息到sessionStorage
  const merchantInfo = {
    merchantId: data.merchantId,
    name: data.merchantName
  }
  sessionStorage.setItem('selectedMerchant', JSON.stringify(merchantInfo))

  // 跳转到商家页面，传递选中的菜品信息
  router.push({
    path: '/user/home/merchant-detail',
    query: {
      reorderItems: JSON.stringify(data.items),
      originalRemark: data.originalRemark || '',
      originalAddressId: data.originalAddressId || '',
      merchantId: data.merchantId
    }
  })

  ElMessage.success('已跳转到商家页面，请确认订单信息')
}

/**
 * 监听筛选和排序变化，重置分页
 */
watch([activeStatus, sortBy], () => {
  resetToFirstPage()
})

// 调试：监控订单数据变化
watch(orders, (newOrders) => {
  console.log('Orders.vue - orders 变化:', {
    订单数量: newOrders.length,
    订单列表: newOrders.map(o => ({ id: o.id, status: o.status }))
  })
}, { deep: true })

// 调试：监控分页后订单变化
watch(paginatedOrders, (newPaginated) => {
  console.log('Orders.vue - paginatedOrders 变化:', {
    订单数量: newPaginated.length,
    订单IDs: newPaginated.map(o => o.id)
  })
}, { deep: true })

/**
 * 监听搜索关键词变化，重置分页
 */
watch(searchKeyword, () => {
  resetToFirstPage()
})

/**
 * 组件挂载时加载数据
 */
onMounted(() => {
  // 检查是否有传递的状态参数
  if (route.query.status) {
    setStatusFilter(route.query.status)
  }

  loadOrders()
  initWebSocket()
  // 滚动到页面顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
})

/**
 * 监听路由query参数变化，当从评价页面返回时刷新订单列表
 */
watch(
  () => route.query.refresh,
  (newRefresh, oldRefresh) => {
    // 如果refresh参数发生变化，说明是从评价页面返回，刷新订单列表
    if (newRefresh && newRefresh !== oldRefresh) {
      console.log('🔄 检测到刷新参数，重新加载订单列表', {
        newRefresh,
        oldRefresh,
        timestamp: new Date().toISOString()
      })
      loadOrders()
    }
  }
)
</script>

<template>
  <div class="orders-container">
    <div class="page-header fade-in-up">
      <CommonBackButton />
      <h2 style="margin-left: 15px">订单中心</h2>
      <div style="flex: 1; text-align: right">
        <el-button
          type="default"
          size="small"
          :loading="loading"
          :class="{
            'refresh-btn': true,
            'is-refreshing': isRefreshing,
            'is-success': refreshSuccess
          }"
          @click="handleRefresh"
        >
          <el-icon :class="{ 'refresh-rotating': isRefreshing, 'refresh-success': refreshSuccess }">
            <Refresh />
          </el-icon>
          <span class="refresh-text">{{ refreshSuccess ? '完成' : '刷新' }}</span>
        </el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <OrderSearchBar
      v-model:search-keyword="searchKeyword"
      :filtered-count="sortedOrders.length"
      @clear="clearSearch"
      class="fade-in-up delay-100"
    />

    <!-- 订单筛选 -->
    <OrderFilterBar
      v-model:active-status="activeStatus"
      v-model:sort-by="sortBy"
      :status-list="statusList"
      :sort-options="sortOptions"
      class="fade-in-up delay-200"
    />

    <!-- 订单列表 -->
    <div
      v-loading="loading"
      class="order-list"
      element-loading-text="加载中..."
      :class="listTransitionName"
    >
      <OrderCard
        v-for="order in paginatedOrders"
        :key="order.id"
        :order="order"
        class="stagger-item"
        :max-display="3"
        @view-details="viewOrderDetails"
        @cancel="cancelOrder"
        @confirm-receipt="confirmReceipt"
        @evaluate="goToEvaluate"
        @additional-review="goToAdditionalReview"
        @reorder="handleReorder"
        @image-error="handleImageError"
      />
    </div>

    <!-- 分页组件 -->
    <el-pagination
      v-if="sortedOrders.length > 0"
      background
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      :current-page="currentPage"
      :page-size="pageSize"
      :page-sizes="pageSizeOptions"
      class="order-pagination"
      @current-change="handlePageChange"
      @size-change="handleSizeChange"
    />

    <!-- 空数据提示 -->
    <el-empty
      v-if="sortedOrders.length === 0 && !loading"
      description="暂无订单记录，快去下单吧！"
    />

    <!-- 再来一单弹窗 -->
    <ReorderDialog
      v-model:visible="reorderDialogVisible"
      :order-id="currentOrderId"
      @confirm="handleReorderConfirm"
    />
  </div>
</template>

<style scoped lang="less">
.orders-container {
  padding: 0 20px 20px 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 80px);

  .page-header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    padding: 16px 20px;
    background: #ffffff;
    border-radius: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    border: 1px solid rgba(0, 0, 0, 0.06);
  }

  h2 {
    font-size: 1.714rem /* 原值: 24px */;
    margin: 0;
    color: #2c5282;
    font-weight: 600;
  }

  .order-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);

    /* 淡出动画 */
    &.list-fade-out {
      animation: list-fade-out 0.3s cubic-bezier(0.4, 0, 0.2, 1) forwards;
    }

    /* 淡入动画 */
    &.list-fade-in {
      animation: list-fade-in 0.4s cubic-bezier(0.4, 0, 0.2, 1) forwards;
    }
  }

  @keyframes list-fade-out {
    0% {
      opacity: 1;
      transform: translateY(0);
    }
    100% {
      opacity: 0;
      transform: translateY(-10px);
    }
  }

  @keyframes list-fade-in {
    0% {
      opacity: 0;
      transform: translateY(10px);
    }
    100% {
      opacity: 1;
      transform: translateY(0);
    }
  }

  .order-pagination {
    margin-top: 24px;
    text-align: center;

    :deep(.el-pagination) {
      .btn-prev,
      .btn-next,
      .el-pager li {
        border-radius: 8px;
        border: 1px solid rgba(179, 212, 252, 0.3);
        background: rgba(255, 255, 255, 0.8);

        &:hover {
          color: #5c8eff;
          border-color: #6ba4ff;
          background: rgba(235, 244, 255, 0.6);
        }

        &.active {
          background: linear-gradient(135deg, #6ba4ff 0%, #5c8eff 100%);
          color: white;
          border-color: transparent;
        }
      }
    }
  }

  /* 刷新按钮动画优化 */
  .refresh-btn {
    position: relative;
    overflow: hidden;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);

    /* 按钮内图标和文字的容器 */
    :deep(.el-icon) {
      transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    }

    /* 刷新文字动画 */
    .refresh-text {
      display: inline-block;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }

    /* 刷新中的状态 */
    &.is-refreshing {
      background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%) !important;
      border-color: #69c0ff !important;
      color: #1890ff !important;
      box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);

      .refresh-text {
        animation: text-pulse 1s ease-in-out infinite;
      }

      /* 添加脉冲波纹效果 */
      &::after {
        content: '';
        position: absolute;
        top: 50%;
        left: 50%;
        width: 0;
        height: 0;
        border-radius: 50%;
        background: rgba(24, 144, 255, 0.3);
        transform: translate(-50%, -50%);
        animation: ripple 1.5s ease-out infinite;
      }
    }

    /* 刷新成功的状态 */
    &.is-success {
      background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%) !important;
      border-color: #95de64 !important;
      color: #52c41a !important;
      box-shadow: 0 2px 8px rgba(82, 196, 26, 0.3);

      /* 成功时的缩放动画 */
      animation: success-bounce 0.6s cubic-bezier(0.68, -0.55, 0.265, 1.55);

      .refresh-text {
        color: #52c41a;
        font-weight: 600;
      }
    }
  }

  /* 刷新图标旋转动画 */
  .refresh-rotating {
    animation: refresh-rotate 0.8s linear infinite;
  }

  /* 刷新成功时的图标动画 */
  .refresh-success {
    animation: success-check 0.6s cubic-bezier(0.68, -0.55, 0.265, 1.55);
    color: #52c41a;
  }

  @keyframes refresh-rotate {
    0% {
      transform: rotate(0deg);
    }
    100% {
      transform: rotate(360deg);
    }
  }

  @keyframes text-pulse {
    0%,
    100% {
      opacity: 1;
      transform: scale(1);
    }
    50% {
      opacity: 0.7;
      transform: scale(0.95);
    }
  }

  @keyframes ripple {
    0% {
      width: 0;
      height: 0;
      opacity: 0.6;
    }
    100% {
      width: 200px;
      height: 200px;
      opacity: 0;
    }
  }

  @keyframes success-bounce {
    0% {
      transform: scale(1);
    }
    50% {
      transform: scale(1.05);
    }
    100% {
      transform: scale(1);
    }
  }

  @keyframes success-check {
    0% {
      transform: scale(0) rotate(-180deg);
      opacity: 0;
    }
    50% {
      transform: scale(1.2) rotate(10deg);
    }
    100% {
      transform: scale(1) rotate(0deg);
      opacity: 1;
    }
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .orders-container {
    padding: 0 12px 16px 12px;
    background: #f5f7fa;

    .page-header {
      padding: 14px 16px;
      border-radius: 14px;
    }

    h2 {
      font-size: 1.429rem /* 原值: 20px */;
    }

    .order-list {
      gap: 12px;
    }

    .order-pagination {
      margin-top: 20px;
    }
  }
}
</style>
