<script setup>
import { onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import OrderSearchBar from './components/OrderSearchBar.vue'
import OrderFilterBar from './components/OrderFilterBar.vue'
import OrderCard from './components/OrderCard.vue'
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
  // 重新加载订单以获取最新数据
  loadOrders()
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
  try {
    await ElMessageBox.confirm('确认已收到餐品并完成订单吗？', '确认收货', {
      confirmButtonText: '确认收货',
      cancelButtonText: '取消',
      type: 'info'
    })

    const response = await orderApi.confirmReceipt(order.id)

    if (response.data.success) {
      order.status = 'completed'
      ElMessage.success('已确认收货，订单完成')
    } else {
      ElMessage.error(response.data.message || '确认收货失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认收货失败:', error)
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
 * 监听筛选和排序变化，重置分页
 */
watch([activeStatus, sortBy], () => {
  resetToFirstPage()
})

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
})
</script>

<template>
  <div class="orders-container">
    <div class="page-header">
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
    />

    <!-- 订单筛选 -->
    <OrderFilterBar
      v-model:active-status="activeStatus"
      v-model:sort-by="sortBy"
      :status-list="statusList"
      :sort-options="sortOptions"
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
        :max-display="3"
        @view-details="viewOrderDetails"
        @cancel="cancelOrder"
        @confirm-receipt="confirmReceipt"
        @evaluate="goToEvaluate"
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
    font-size: 24px;
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
      font-size: 20px;
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
