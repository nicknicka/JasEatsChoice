<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'
import { useAuthStore } from '../../store/authStore'
// 导入拆分后的组件
import MerchantInfo from '../../components/merchant/MerchantInfo.vue'
import BusinessOverview from '../../components/merchant/BusinessOverview.vue'
import OrderCenter from '../../components/merchant/OrderCenter.vue'
import TodayMenu from '../../components/merchant/TodayMenu.vue'
import ShopAlbum from '../../components/merchant/ShopAlbum.vue'
import DiscountManagement from '../../components/merchant/DiscountManagement.vue'
import AnnouncementManagement from '../../components/merchant/AnnouncementManagement.vue'

const router = useRouter()

// 从 Pinia store 获取商家ID
const authStore = useAuthStore()
let merchantId = authStore.merchantId

// 如果 Pinia 中没有商家ID，尝试从 localStorage 读取
if (!merchantId) {
  const localStorageMerchantId = localStorage.getItem('auth_merchantId')
  if (localStorageMerchantId) {
    merchantId = localStorageMerchantId
    authStore.setMerchantId(localStorageMerchantId) // 更新到 Pinia 中
  } else {
    // 如果 localStorage 中也没有，回到首页或注册页
    ElMessage.error('未检测到商家ID，请重新登录')
    router.push('/merchant/register') // 跳转到注册页或首页
  }
}

// 商家信息
const merchantInfo = ref({
  id: merchantId, // 确保id始终存在
  name: '健康轻食馆',
  rating: 4.8,
  phone: '138-1234-5678',
  email: 'health-food@example.com',
  address: '北京市朝阳区建国路88号'
})

// 商家营业概览
const businessOverview = ref({
  sales: 0,
  orders: 0,
  newComments: 0,
  unreadMessages: 3
})

// 页面跳转
const navigateToOrders = () => {
  router.push('/merchant/home/orders')
}

// 查看订单详情
const viewOrderDetails = (order) => {
  // 跳转到订单详情页面
  router.push(`/merchant/home/orders/details?orderId=${order.id}`)
}

// 更新订单状态
const updateOrderStatus = (order) => {
  // 定义订单状态流转逻辑
  const statusFlow = {
    1: 2, // 待处理 -> 备菜中
    2: 3, // 备菜中 -> 烹饪中
    3: 4, // 烹饪中 -> 待配送
    4: 5, // 待配送 -> 已完成
    5: 5, // 已完成 -> 已完成（不可再改）
    6: 6 // 已取消 -> 已取消（不可再改）
  }

  const nextStatus = statusFlow[order.status] || order.status

  // 如果状态没有变化
  if (nextStatus === order.status) {
    ElMessage.warning(`订单 ${order.id} 当前状态不可变更`)
    return
  }

  // 调用API更新订单状态
  const updateData = {
    orderId: order.id,
    status: nextStatus
  }

  api
    .put(API_CONFIG.merchant.updateOrderStatus.replace('{orderId}', order.id), updateData)
    .then((response) => {
      if (response.data && response.data.success) {
        // 更新本地订单状态
        order.status = nextStatus
        ElMessage.success(`订单 ${order.id} 状态已更新为 ${orderStatusMap[nextStatus]}`)
      }
    })
    .catch((error) => {
      console.error('更新订单状态失败:', error)
      ElMessage.error('更新订单状态失败')
    })
}

// 通知用户
const notifyUser = (order) => {
  // 调用API通知用户
  const notifyData = {
    orderId: order.id,
    message: `您的订单 ${order.id} 状态已更新为 ${orderStatusMap[order.status]}`
  }

  api
    .post(API_CONFIG.merchant.notifyUser.replace('{orderId}', order.id), notifyData)
    .then((response) => {
      if (response.data && response.data.success) {
        ElMessage.success(`已成功通知用户订单 ${order.id} 的最新状态`)
      }
    })
    .catch((error) => {
      console.error('通知用户失败:', error)
      ElMessage.error('通知用户失败')
    })
}

// 概览项导航
const navigateToStatistics = () => {
  router.push('/merchant/home/statistics')
}

const navigateToComments = () => {
  router.push('/merchant/home/comments')
}

const navigateToMessages = () => {
  router.push('/merchant/home/messages')
}

// 营业概览配置数组 - 使用循环减少冗余
const overviewConfig = ref([
  {
    key: 'sales',
    icon: '💰',
    label: '营业额',
    onClick: navigateToStatistics,
    trend: '↑ 12.5%',
    trendClass: 'trend-up',
    suffix: '¥'
  },
  {
    key: 'orders',
    icon: '🍽️',
    label: '订单数',
    onClick: navigateToOrders,
    trend: '↑ 8.3%',
    trendClass: 'trend-up'
  },
  {
    key: 'newComments',
    icon: '🌟',
    label: '新增评价',
    onClick: navigateToComments,
    trend: '↓ 2.1%',
    trendClass: 'trend-down'
  },
  {
    key: 'unreadMessages',
    icon: '📞',
    label: '未读消息',
    onClick: navigateToMessages,
    trend: '→ 0%',
    trendClass: 'trend-neutral'
  }
])

// 筛选功能
const activeFilter = ref('today')

// 所有订单数据
const allOrders = ref([])

// 筛选后的订单
const filteredOrders = ref([])

// 订单状态映射（对应后端状态码）
// 0-待支付、1-待接单、2-备菜中、3-烹饪中、4-待上菜、5-已送达、6-已取消、7-待评价、8-已评价
const orderStatusMap = {
  0: '待支付',
  1: '待接单',
  2: '备菜中',
  3: '烹饪中',
  4: '待上菜',
  5: '已送达',
  6: '已取消',
  7: '待评价',
  8: '已评价'
}

// 筛选订单
const filterOrders = (filterType) => {
  activeFilter.value = filterType

  // 简单的筛选逻辑，根据实际时间处理
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const weekStart = new Date(now.getFullYear(), now.getMonth(), now.getDate() - now.getDay())
  const monthStart = new Date(now.getFullYear(), now.getMonth(), 1)

  filteredOrders.value = allOrders.value.filter((order) => {
    const orderDate = new Date(order.createTime)

    // 时间范围过滤
    let timeMatch = true
    switch (filterType) {
      case 'today':
        timeMatch = orderDate >= today
        break
      case 'week':
        timeMatch = orderDate >= weekStart
        break
      case 'month':
        timeMatch = orderDate >= monthStart
        break
    }

    return timeMatch
  })
}

const navigateToMenu = () => {
  router.push('/merchant/home/menu')
}

// 快捷操作函数 - 设置优惠
const setDiscount = () => {
  // 优惠活动管理在当前页面，无需跳转
}

// 快捷操作函数 - 联系客服
const contactCustomerService = () => {
  ElMessage.info('联系客服功能已触发')
  // 可以在此处添加具体的实现逻辑
}

// 菜单状态映射
const menuStatusMap = {
  online: { text: '上架中', icon: '🟢', type: 'success' },
  draft: { text: '草稿', icon: '🟡', type: 'warning' },
  offline: { text: '下架中', icon: '🔴', type: 'danger' }
}

// 菜品状态映射
// 今日菜单数据
const todayMenus = ref([])

// 从后端获取今日菜单数据
const fetchTodayMenus = () => {
  api
    .get(`/v1/menus/merchants/${merchantId}/menu`)
    .then((response) => {
      if (response.code === '200' && response.data) {
        // 假设后端返回的菜单数据结构与我们需要的基本一致
        // 如果需要转换数据格式，可以在这里处理
        todayMenus.value = response.data.map((menu) => ({
          ...menu,
          status: menu.status === 'active' ? 'online' : 'offline',
          // 暂时设置dishes为0，后面需要实现获取菜品数量的接口
          dishes: 0,
          // 格式转换：LocalDateTime to String
          updateTime: menu.updateTime ? menu.updateTime.replace('T', ' ') : '',
          autoOnline: menu.autoStartTime ? menu.autoStartTime.replace('T', ' ') : '',
          autoOffline: menu.autoEndTime ? menu.autoEndTime.replace('T', ' ') : ''
        }))
        // 初始化筛选后的菜单
        filteredMenus.value = [...todayMenus.value]
      }
    })
    .catch((error) => {
      console.error('获取今日菜单数据失败:', error)
    })
}

// 筛选后的菜单
const filteredMenus = ref([...todayMenus.value])

// 菜单类型筛选

// 页面加载
onMounted(() => {
  // ElMessage.success("欢迎进入商家中心");
  // console.log("商家ID:", merchantId);
  // 调用后端API获取今日营业概览数据

  // 获取营业概览
  api
    .get(`/v1/merchant/${merchantId}/business-overview`)
    .then((response) => {
      if (response.code === '200' && response.data) {
        businessOverview.value = response.data
      }
    })
    .catch((error) => {
      console.error('获取营业概览数据失败:', error)
      // 如果获取失败，保留模拟数据
    })

  // 获取订单列表
  api
    .get(`/v1/orders/merchant/${merchantId}`)
    .then((response) => {
      if (response.code === '200' && response.data) {
        allOrders.value = response.data
        // 默认显示今日订单
        filterOrders('today')
      }
    })
    .catch((error) => {
      console.error('获取订单列表失败:', error)
      allOrders.value = []
      filteredOrders.value = []
    })

  // 获取商家信息
  api
    .get(`/v1/merchant/${merchantId}`)
    .then((response) => {
      if (response.code === '200' && response.data) {
        merchantInfo.value = response.data
      }
    })
    .catch((error) => {
      console.error('获取商家信息失败:', error)
    })

  // 获取今日菜单数据
  fetchTodayMenus()
})

// onUnmounted(() => {
//   ElMessage.success('欢迎下次再来');
// });
</script>

<template>
  <div class="merchant-home-container">
    <div class="merchant-content">
      <!-- 商家信息 -->
      <MerchantInfo />

      <!-- 今日营业概览 -->
      <BusinessOverview />

      <!-- 订单中心 -->
      <OrderCenter />

      <!-- 今日菜单 -->
      <TodayMenu />

      <!-- 优惠管理 -->
      <DiscountManagement :merchant-id="String(merchantId)" />

      <!-- 公告栏配置 -->
      <AnnouncementManagement :merchant-id="String(merchantId)" />

      <!-- 店铺相册 -->
      <ShopAlbum :merchant-id="String(merchantId)" />

      <!-- 快捷操作 -->
      <div class="quick-actions-card">
        <h3 class="card-title">🎯 快捷操作</h3>
        <div class="actions-grid">
          <div class="action-item" @click="navigateToMenu">
            <div class="action-icon">➕</div>
            <div class="action-label">新增菜单</div>
          </div>
          <div class="action-item" @click="setDiscount">
            <div class="action-icon">💰</div>
            <div class="action-label">设置优惠</div>
          </div>
          <div class="action-item" @click="contactCustomerService">
            <div class="action-icon">📞</div>
            <div class="action-label">联系客服</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';
@import '../../assets/css/merchant-theme.less';

// ===== Nordic Pro 商家端设计系统 =====
.merchant-home-container {
  width: 100%;
  min-height: 100%;
  background: @merchant-bg;
  padding: @nordic-space-lg @nordic-space-xl @nordic-space-2xl;
  box-sizing: border-box;

  .merchant-content {
    max-width: 1400px;
    margin: 0 auto;
  }

  // ===== 商家信息卡片 =====
  .merchant-info-card {
    .merchant-stat-card();
    margin-bottom: @nordic-space-lg;
    border-left-color: @merchant-secondary;

    .info-header {
      display: flex;
      align-items: center;
      gap: @nordic-space-lg;

      .avatar-section {
        .avatar {
          font-size: 64px;
        }
        .edit-btn {
          margin-top: @nordic-space-sm;
        }
      }

      .detail-section {
        flex: 1;

        .merchant-name {
          font-size: @nordic-text-lg;
          font-weight: 600;
          margin-bottom: @nordic-space-sm;
          color: @merchant-text;
        }

        .merchant-rating {
          margin-bottom: @nordic-space-sm;
        }

        .contact-info {
          display: flex;
          flex-wrap: wrap;
          gap: @nordic-space-lg;
          font-size: @nordic-text-base;
          color: @merchant-text-sec;
        }
      }
    }
  }

  // ===== 营业概览卡片 =====
  .overview-card {
    .merchant-stat-card();
    margin-bottom: @nordic-space-lg;
    padding: @nordic-space-lg;

    .card-title {
      .merchant-section-title();
      display: flex;
      align-items: center;
      gap: @nordic-space-sm;
      margin-bottom: @nordic-space-lg;

      &::before {
        content: '📊';
        font-size: @nordic-text-lg;
      }

      &::after {
        content: '';
        flex: 1;
        height: 1px;
        background: linear-gradient(to right, @merchant-border, transparent);
        margin-left: @nordic-space-md;
      }
    }

    .overview-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: @nordic-space-md;

      .overview-item {
        display: flex;
        align-items: center;
        gap: @nordic-space-md;
        padding: @nordic-space-lg;
        border-radius: @nordic-radius-lg;
        background: @merchant-surface;
        border: 1px solid @merchant-border;
        transition: all @nordic-transition-base ease;
        cursor: pointer;
        position: relative;
        overflow: hidden;

        // 左侧强调边框
        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 0;
          bottom: 0;
          width: 4px;
          border-radius: @nordic-radius-lg 0 0 @nordic-radius-lg;
        }

        &.sales::before { background: @merchant-success; }
        &.orders::before { background: @merchant-info; }
        &.comments::before { background: @merchant-warning; }
        &.messages::before { background: @merchant-error; }

        &:hover {
          transform: translateY(-3px);
          box-shadow: 0 8px 24px @merchant-shadow-hover;
          border-color: @merchant-primary;
        }

        .item-icon {
          font-size: 32px;
          width: 56px;
          height: 56px;
          display: flex;
          align-items: center;
          justify-content: center;
          border-radius: @nordic-radius-md;
          background: @merchant-primary-light;
        }

        .item-content {
          flex: 1;

          .overview-label {
            font-size: @nordic-text-sm;
            color: @merchant-text-muted;
            margin-bottom: 4px;
            font-weight: 500;
            text-transform: uppercase;
            letter-spacing: @nordic-letter-wide;
          }

          .overview-value {
            font-size: @nordic-text-xl;
            font-weight: 700;
            color: @merchant-text;
            letter-spacing: @nordic-letter-tighter;
            margin-bottom: 4px;
          }

          .item-trend {
            font-size: @nordic-text-xs;
            font-weight: 600;
            padding: 2px 8px;
            border-radius: @nordic-radius-pill;

            &.trend-up {
              color: @merchant-success;
              background: @merchant-success-light;
            }

            &.trend-down {
              color: @merchant-error;
              background: @merchant-error-light;
            }

            &.trend-neutral {
              color: @merchant-text-muted;
              background: @merchant-divider;
            }
          }
        }
      }
    }
  }

  // ===== 订单中心卡片 =====
  .orders-card {
    .merchant-stat-card();
    margin-bottom: @nordic-space-lg;
    border-left-color: @merchant-info;

    .orders-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: @nordic-space-lg;

      .card-title {
        .merchant-section-title();
        margin: 0;

        &::before {
          content: '📋';
          margin-right: @nordic-space-sm;
        }
      }

      .filter-section {
        .order-filter-tag {
          margin-right: @nordic-space-sm;
          cursor: pointer;
          transition: all @nordic-transition-base ease;
          border-radius: @nordic-radius-pill;

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px @merchant-shadow;
          }

          &.active {
            transform: translateY(-1px);
            box-shadow: 0 2px 8px @merchant-shadow;
          }
        }
      }
    }

    .orders-list {
      overflow-y: auto;
      max-height: 400px;

      .no-orders {
        text-align: center;
        padding: 80px 0;
        color: @merchant-text-muted;
        font-size: @nordic-text-md;
      }

      .order-item {
        .merchant-order-card();

        .order-info {
          .order-no {
            font-weight: 600;
            margin-bottom: @nordic-space-sm;
            color: @merchant-text;
          }

          .order-details {
            display: flex;
            flex-wrap: wrap;
            gap: @nordic-space-lg;
            font-size: @nordic-text-base;
            color: @merchant-text-sec;

            .amount {
              font-weight: 600;
              color: @merchant-secondary;
            }
          }
        }

        .order-actions {
          display: flex;
          gap: @nordic-space-sm;
          flex-wrap: wrap;
        }
      }
    }

    .view-all {
      text-align: right;
      margin-top: @nordic-space-md;
    }
  }

  // ===== 今日菜单卡片 =====
  .today-menu-card {
    .merchant-stat-card();
    margin-bottom: @nordic-space-lg;
    border-left-color: @merchant-success;

    .menu-header {
      display: flex;
      justify-content: flex-start;
      align-items: center;
      margin-bottom: @nordic-space-lg;
      flex-wrap: wrap;
      gap: @nordic-space-lg;

      .card-title {
        .merchant-section-title();
        margin: 0;

        &::before {
          content: '🍽️';
          margin-right: @nordic-space-sm;
        }
      }

      .filter-section {
        display: flex;
        align-items: center;
        gap: @nordic-space-md;
        flex-wrap: wrap;

        .menu-filter-tag,
        .menu-status-tag {
          cursor: pointer;
          transition: all @nordic-transition-base ease;
          border-radius: @nordic-radius-pill;

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px @merchant-shadow;
          }

          &.active {
            transform: translateY(-1px);
            box-shadow: 0 4px 12px @merchant-shadow-hover;
          }
        }
      }
    }

    .menu-list {
      margin-bottom: @nordic-space-lg;

      .menu-item,
      .menu-card {
        padding: @nordic-space-lg;
        border: 1px solid @merchant-border;
        border-radius: @nordic-radius-md;
        margin-bottom: @nordic-space-md;
        background: @merchant-surface;
        transition: all @nordic-transition-base ease;
        cursor: pointer;

        &:hover {
          box-shadow: 0 4px 16px @merchant-shadow-hover;
          border-color: @merchant-primary;
          transform: translateY(-3px);
        }

        &.active {
          border-color: @merchant-primary;
          background: @merchant-primary-light;
        }

        .menu-info {
          .menu-name {
            display: flex;
            align-items: center;
            gap: @nordic-space-md;
            margin-bottom: @nordic-space-md;

            .name {
              font-size: @nordic-text-md;
              font-weight: 600;
              color: @merchant-text;
            }
          }

          .menu-stats,
          .auto-times {
            display: flex;
            flex-wrap: wrap;
            gap: @nordic-space-lg;
            margin-bottom: @nordic-space-sm;
            font-size: @nordic-text-base;
            color: @merchant-text-sec;

            .dishes-count {
              color: @merchant-success;
              font-weight: 500;
            }
          }

          .auto-times {
            font-size: @nordic-text-sm;
            color: @merchant-text-muted;
          }
        }
      }

      .empty-menu {
        text-align: center;
        padding: 80px @nordic-space-lg;
        color: @merchant-text-muted;
        font-size: @nordic-text-md;
        background: @merchant-primary-light;
        border: 2px dashed @merchant-primary;
        border-radius: @nordic-radius-lg;
        margin-bottom: @nordic-space-lg;

        &:hover {
          background: @merchant-surface-alt;
        }

        span {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: @nordic-space-sm;
        }
      }
    }

    .view-all {
      text-align: right;
      margin-top: @nordic-space-lg;

      .el-button {
        color: @merchant-primary;
        border-color: @merchant-primary;
        transition: all @nordic-transition-base ease;

        &:hover {
          background: @merchant-primary;
          color: @merchant-surface;
          transform: scale(1.05);
        }
      }
    }
  }

  // ===== 快捷操作卡片 =====
  .quick-actions-card {
    .merchant-stat-card();
    margin-bottom: @nordic-space-lg;
    border-left-color: @merchant-warning;
    background: linear-gradient(135deg, @merchant-surface 0%, @merchant-surface-alt 100%);

    .card-title {
      .merchant-section-title();
      display: flex;
      align-items: center;
      gap: @nordic-space-sm;
      margin-bottom: @nordic-space-lg;
      padding-bottom: @nordic-space-md;
      border-bottom: 2px solid @merchant-divider;

      &::before {
        content: '🎯';
        font-size: @nordic-text-lg;
      }
    }

    .actions-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
      gap: @nordic-space-md;

      .action-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: @nordic-space-lg @nordic-space-md;
        border: 2px solid @merchant-border;
        border-radius: @nordic-radius-lg;
        cursor: pointer;
        transition: all @nordic-transition-base ease;
        background: @merchant-surface;
        position: relative;
        overflow: hidden;

        &::before {
          content: '';
          position: absolute;
          inset: 0;
          background: linear-gradient(135deg, @merchant-primary-light 0%, @merchant-surface 100%);
          opacity: 0;
          transition: opacity @nordic-transition-base ease;
        }

        &:hover {
          transform: translateY(-5px);
          box-shadow: 0 8px 24px @merchant-shadow-hover;
          border-color: @merchant-primary;

          &::before {
            opacity: 1;
          }
        }

        .action-icon {
          font-size: 48px;
          margin-bottom: @nordic-space-md;
          transition: all 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
          position: relative;
          z-index: 1;
        }

        &:hover .action-icon {
          transform: scale(1.15) rotate(5deg);
        }

        .action-label {
          font-size: @nordic-text-sm;
          font-weight: 600;
          color: @merchant-text;
          position: relative;
          z-index: 1;
        }
      }
    }
  }
}

// ===== 菜品列表样式 =====
.dishes-card {
  .merchant-stat-card();
  margin-bottom: @nordic-space-lg;
  border-left-color: @merchant-success;
  border-top: none;
  border-top-left-radius: 0;
  border-top-right-radius: 0;

  .dish-list {
    margin-bottom: @nordic-space-lg;

    .dish-item {
      padding: @nordic-space-lg;
      border: 1px solid @merchant-border;
      border-radius: @nordic-radius-md;
      margin-bottom: @nordic-space-md;
      background: @merchant-surface;
      transition: all @nordic-transition-base ease;
      display: flex;
      align-items: flex-start;
      gap: @nordic-space-md;
      overflow: hidden;

      &:hover {
        box-shadow: 0 4px 16px @merchant-shadow-hover;
        border-color: @merchant-primary;
        transform: translateY(-3px);
      }

      .dish-cover {
        font-size: 48px;
        width: 90px;
        height: 90px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: linear-gradient(135deg, @merchant-primary, @merchant-primary-light);
        border-radius: @nordic-radius-md;
        flex-shrink: 0;
        color: #fff;
        box-shadow: 0 2px 8px @merchant-shadow;
        transition: all @nordic-transition-base ease;
      }

      &:hover .dish-cover {
        transform: scale(1.1);
      }

      .dish-info {
        flex: 1;

        .dish-name {
          display: flex;
          align-items: center;
          gap: @nordic-space-md;
          margin-bottom: @nordic-space-sm;

          .name {
            font-size: @nordic-text-md;
            font-weight: 600;
            color: @merchant-text;
          }
        }

        .dish-desc {
          font-size: @nordic-text-base;
          color: @merchant-text-sec;
          margin-bottom: @nordic-space-md;
          line-height: 1.6;
        }

        .dish-stats {
          display: flex;
          flex-wrap: wrap;
          gap: @nordic-space-lg;
          font-size: @nordic-text-base;
          color: @merchant-text-sec;

          .dish-category {
            background: @merchant-primary-light;
            color: @merchant-primary;
            padding: 4px 12px;
            border-radius: @nordic-radius-sm;
            font-size: @nordic-text-xs;
            font-weight: 500;
          }

          .dish-price {
            color: @merchant-secondary;
            font-weight: 600;
            font-size: @nordic-text-md;
          }

          .dish-stock {
            font-size: @nordic-text-sm;
            font-weight: 500;

            &.stock-almost {
              color: @merchant-warning;
            }

            &.stock-off {
              color: @merchant-error;
            }
          }
        }
      }

      .dish-actions {
        display: flex;
        flex-direction: column;
        gap: @nordic-space-sm;
        flex-shrink: 0;

        .el-button {
          width: 90px;
          transition: all @nordic-transition-base ease;

          &:hover {
            transform: translateY(-2px);
          }
        }
      }
    }
  }
}
</style>
