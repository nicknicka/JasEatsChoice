<script setup>
import { ref, onMounted, onUnmounted, onActivated, onDeactivated, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, List } from '@element-plus/icons-vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import AppCard from '../../components/common/AppCard.vue'
import AppEmptyState from '../../components/common/AppEmptyState.vue'
import OrderSearchBar from './components/OrderSearchBar.vue'
import OrderFilterBar from './components/OrderFilterBar.vue'
import OrderCard from './components/OrderCard.vue'
import ReorderDialog from '../../components/ReorderDialog.vue'
import { useOrderData } from '../../composables/useOrderData'
import { useOrderFilter } from '../../composables/useOrderFilter'
import { useOrderPagination } from '../../composables/useOrderPagination'
import { useOrderWebSocket } from '../../composables/useOrderWebSocket'
import orderApi from '../../api/order'
import paymentApi from '../../api/payment'

const router = useRouter()
const route = useRoute()
const AUTO_REFRESH_INTERVAL = 10000

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
  updateOrderStatus,
  getUserId
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
  if (orderUpdate?.id && orderUpdate?.status !== undefined) {
    updateOrderStatus(orderUpdate.id, orderUpdate.status)
    scheduleSilentRefresh(300)
  }
}

// 初始化 WebSocket
const { initWebSocket } = useOrderWebSocket({
  onOrderUpdate: handleOrderUpdate
})

let autoRefreshTimer = null
let silentRefreshTimer = null

function isOrdersPageVisible() {
  return route.name === 'user-orders' && document.visibilityState !== 'hidden'
}

function refreshOrdersSilently() {
  if (!isOrdersPageVisible()) return
  loadOrders({ silent: true })
}

function scheduleSilentRefresh(delay = 0) {
  if (silentRefreshTimer) {
    clearTimeout(silentRefreshTimer)
  }

  silentRefreshTimer = setTimeout(() => {
    silentRefreshTimer = null
    refreshOrdersSilently()
  }, delay)
}

function startAutoRefresh() {
  stopAutoRefresh()
  autoRefreshTimer = setInterval(refreshOrdersSilently, AUTO_REFRESH_INTERVAL)
}

function stopAutoRefresh() {
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer)
    autoRefreshTimer = null
  }
}

function handleWindowFocus() {
  scheduleSilentRefresh()
}

function handleVisibilityChange() {
  if (document.visibilityState === 'visible') {
    scheduleSilentRefresh()
  }
}

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
 * 支付订单
 */
async function payOrder(order) {
  // 支付密码只在本次请求内持有，不落 localStorage、不写日志
  let paymentPassword
  try {
    const { value } = await ElMessageBox.prompt('请输入 6 位支付密码', '支付验证', {
      confirmButtonText: '确认支付',
      cancelButtonText: '取消',
      inputType: 'password',
      inputPattern: /^\d{6}$/,
      inputErrorMessage: '支付密码为 6 位数字'
    })
    paymentPassword = value
  } catch {
    return
  }

  try {
    const response = await paymentApi.payOrder(order.id, getUserId(), 'wallet', paymentPassword)

    if (response?.success) {
      updateOrderStatus(order.id, 1)
      ElMessage.success(response.message || '支付成功')
      scheduleSilentRefresh()
    } else {
      ElMessage.error(response?.message || '支付失败')
    }
  } catch (error) {
    console.error('订单支付失败:', error)
    ElMessage.error(error?.data?.message || error?.response?.data?.message || error?.message || '支付失败')
  }
}

/**
 * 取消订单
 */
async function cancelOrder(order) {
  try {
    await orderApi.updateOrderStatus(order.id, 4)

    // 使用不可变更新：通过 updateOrderStatus 更新本地状态
    updateOrderStatus(order.id, 4)
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

    if (response?.success) {
      // 使用不可变更新：通过 updateOrderStatus 更新本地状态
      updateOrderStatus(order.id, 3)
      ElMessage.success('已确认收货')
    } else {
      ElMessage.error(response?.message || '确认收货失败')
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
  startAutoRefresh()
  window.addEventListener('focus', handleWindowFocus)
  document.addEventListener('visibilitychange', handleVisibilityChange)
  // 滚动到页面顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
})

onActivated(() => {
  scheduleSilentRefresh()
  startAutoRefresh()
})

onDeactivated(() => {
  stopAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
  if (silentRefreshTimer) {
    clearTimeout(silentRefreshTimer)
    silentRefreshTimer = null
  }
  window.removeEventListener('focus', handleWindowFocus)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})

/**
 * 监听路由query参数变化，当从评价页面返回时刷新订单列表
 */
watch(
  () => route.query.refresh,
  (newRefresh, oldRefresh) => {
    // 如果refresh参数发生变化，说明是从评价页面返回，刷新订单列表
    if (newRefresh && newRefresh !== oldRefresh) {
      loadOrders({ silent: true })
    }
  }
)
</script>

<template>
  <div class="orders-container">
    <!-- 页头：标准版，标题 --text-h1 + 上下 --space-6（DESIGN-BASELINE §4.7） -->
    <header class="orders-header">
      <h1 class="orders-header__title">订单中心</h1>
      <div class="orders-header__actions">
        <CommonBackButton />
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
    </header>

    <!-- 搜索 + 筛选收进同一张 L1 卡，压缩工具区高度（DESIGN-BASELINE §3.4 / §4.1） -->
    <AppCard class="orders-toolbar fade-in-up" :level="1">
      <OrderSearchBar
        v-model:search-keyword="searchKeyword"
        :filtered-count="sortedOrders.length"
        @clear="clearSearch"
      />

      <OrderFilterBar
        v-model:active-status="activeStatus"
        v-model:sort-by="sortBy"
        :status-list="statusList"
        :sort-options="sortOptions"
      />
    </AppCard>

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
        @pay="payOrder"
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

    <!-- 空数据提示：§4.5 线性图标 + 一句中文说明 -->
    <AppEmptyState
      v-if="sortedOrders.length === 0 && !loading"
      class="order-empty"
      description="暂无订单记录，快去下单吧！"
      :icon="List"
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
  box-sizing: border-box;
  width: 100%;
  max-width: var(--page-max);
  min-height: calc(100vh - 80px);
  margin: 0 auto;
  padding: 0 var(--page-gutter) var(--space-10);
  background: var(--canvas);

  .orders-header {
    display: flex;
    align-items: center;
    gap: var(--space-4);
    padding: var(--space-6) 0;
    margin-bottom: var(--space-6);
    border-bottom: 1px solid var(--line);
  }

  .orders-header__title {
    margin: 0;
    color: var(--ink-900);
    font-size: var(--text-h1);
    font-weight: var(--weight-semibold);
    line-height: 32px;
    letter-spacing: var(--tracking-tight);
  }

  .orders-header__actions {
    display: flex;
    align-items: center;
    gap: var(--space-3);
    margin-left: auto;
  }

  .orders-toolbar {
    margin-bottom: var(--card-gap);
  }

  .order-list {
    display: flex;
    flex-direction: column;
    gap: var(--card-gap);

    /* 刷新时的列表过渡（DESIGN-BASELINE §3.5：只动 opacity / transform） */
    &.list-fade-out {
      animation: list-fade-out var(--duration-base) var(--ease-out) forwards;
    }

    &.list-fade-in {
      animation: list-fade-in var(--duration-base) var(--ease-out) forwards;
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
    margin-top: var(--space-6);
    text-align: center;

    :deep(.el-pagination) {
      .btn-prev,
      .btn-next,
      .el-pager li {
        border: 1px solid var(--line);
        border-radius: var(--radius-sm);
        background: var(--surface);

        &:hover {
          color: var(--brand-600);
          border-color: var(--brand-300);
          background: var(--brand-50);
        }

        &.active {
          background: var(--brand-500);
          border-color: var(--brand-500);
          color: var(--surface);
        }
      }
    }
  }

  .order-empty {
    margin-top: var(--space-6);
  }

  /* 刷新按钮的加载 / 成功反馈（DESIGN-BASELINE §4.6） */
  .refresh-btn {
    .refresh-text {
      display: inline-block;
      transition: color var(--duration-fast) var(--ease-out);
    }

    &.is-refreshing {
      border-color: var(--brand-300);
      background: var(--brand-50);
      color: var(--brand-600);
    }

    &.is-success {
      border-color: var(--success);
      background: var(--success-bg);
      color: var(--success);

      .refresh-text {
        color: var(--success);
        font-weight: var(--weight-semibold);
      }
    }
  }

  .refresh-rotating {
    animation: refresh-rotate var(--spin-duration) linear infinite;
  }

  .refresh-success {
    color: var(--success);
  }

  @keyframes refresh-rotate {
    0% {
      transform: rotate(0deg);
    }
    100% {
      transform: rotate(360deg);
    }
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .orders-container {
    padding: 0 var(--space-4) var(--space-6);

    .orders-header {
      padding: var(--space-4) 0;
      margin-bottom: var(--space-4);
    }

    .orders-header__title {
      font-size: var(--text-h2);
      line-height: 28px;
    }

    .order-list {
      gap: var(--card-gap-compact);
    }

    .order-pagination {
      margin-top: var(--space-4);
    }
  }
}
</style>
