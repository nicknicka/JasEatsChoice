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

// 订单状态映射
const orderStatusMap = {
  1: '待处理',
  2: '备菜中',
  3: '烹饪中',
  4: '待配送',
  5: '已完成',
  6: '已取消'
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

      <!-- 店铺相册 -->
      <ShopAlbum :merchant-id="String(merchantId)" />

      <!-- 公告栏配置 -->
      <AnnouncementManagement :merchant-id="String(merchantId)" />

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
.merchant-home-container {
  padding: 0 20px 20px 20px;

  .merchant-info-card {
    margin-bottom: 24px;
    padding: 24px; /* 添加内边距 */
    border: 2px solid #409eff; /* 使用Element Plus主色 */
    border-radius: 12px; /* 增加圆角 */
    background-color: #ffffff; /* 白色背景 */
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08); /* 增强阴影效果 */

    .info-header {
      display: flex;
      align-items: center;
      gap: 20px;

      .avatar-section {
        .avatar {
          font-size: 64px;
        }
        .edit-btn {
          margin-top: 10px;
        }
      }

      .detail-section {
        flex: 1;

        .merchant-name {
          font-size: 20px;
          font-weight: 600;
          margin-bottom: 8px;
        }

        .merchant-rating {
          margin-bottom: 8px;
        }

        .contact-info {
          display: flex;
          flex-wrap: wrap;
          gap: 20px;
          font-size: 14px;
          color: #606266;
        }
      }
    }
  }

  .overview-card {
    margin-bottom: 24px;
    padding: 24px;
    border: 2px solid #67c23a; /* 使用成功绿 */
    border-radius: 12px;
    background-color: #ffffff;
    box-shadow: 0 4px 20px rgba(103, 194, 58, 0.12);

    .card-title {
      font-size: 20px;
      font-weight: 700;
      margin-bottom: 20px;
      color: #e6a23c;
      display: flex;
      align-items: center;

      &::after {
        content: '';
        flex: 1;
        height: 1px;
        background: linear-gradient(to right, #e6a23c, transparent);
        margin-left: 15px;
      }
    }

    .overview-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
      gap: 20px;

      .overview-item {
        display: flex;
        align-items: center;
        gap: 16px;
        padding: 20px;
        border-radius: 12px;
        background: white;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
        transition: all 0.3s ease;
        cursor: pointer;
        border: 1px solid #f0f0f0;

        &:hover {
          transform: translateY(-5px);
          box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
          border-color: #ffd7a3;
        }

        &.sales {
          border-left: 4px solid #67c23a;

          &:hover {
            border-left: 4px solid #67c23a;
          }
        }

        &.orders {
          border-left: 4px solid #409eff;

          &:hover {
            border-left: 4px solid #409eff;
          }
        }

        &.comments {
          border-left: 4px solid #e6a23c;

          &:hover {
            border-left: 4px solid #e6a23c;
          }
        }

        &.messages {
          border-left: 4px solid #f56c6c;

          &:hover {
            border-left: 4px solid #f56c6c;
          }
        }

        .item-icon {
          font-size: 32px;
          width: 60px;
          height: 60px;
          display: flex;
          align-items: center;
          justify-content: center;
          border-radius: 50%;
          background: rgba(230, 162, 60, 0.1);
        }

        .item-content {
          flex: 1;

          .overview-label {
            font-size: 14px;
            color: #909399;
            margin-bottom: 4px;
            font-weight: 500;
          }

          .overview-value {
            font-size: 24px;
            font-weight: 700;
            margin-bottom: 4px;
          }

          .item-trend {
            font-size: 12px;
            font-weight: 600;

            &.trend-up {
              color: #67c23a;
            }

            &.trend-down {
              color: #f56c6c;
            }

            &.trend-neutral {
              color: #909399;
            }
          }
        }
      }
    }
  }

  .orders-card {
    margin-bottom: 24px;
    padding: 24px; /* 添加内边距 */
    border: 2px solid #409eff; /* 加强边框 */
    border-radius: 12px; /* 统一圆角 */
    background-color: #ffffff; /* 白色背景 */
    box-shadow: 0 4px 20px rgba(64, 158, 255, 0.1); /* 增强阴影 */

    .orders-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      .card-title {
        font-size: 18px;
        font-weight: 600;
        margin: 0;
      }

      .filter-section {
        .order-filter-tag {
          margin-right: 10px;
          cursor: pointer;
          transition: all 0.3s ease;
          border-radius: 20px;

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 3px 12px rgba(0, 0, 0, 0.15);
          }

          &.active {
            transform: translateY(-1px);
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
          }
        }
      }
    }

    .orders-list {
      max-height: 400px;
      overflow-y: auto;
      padding-right: 8px;

      .no-orders {
        text-align: center;
        padding: 80px 0;
        color: #909399;
        font-size: 16px;
      }

      .order-item {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        padding: 16px;
        border: 1px solid #e4e7ed;
        border-radius: 4px;
        margin-bottom: 12px;

        .order-info {
          .order-no {
            font-weight: 600;
            margin-bottom: 8px;
          }

          .order-details {
            display: flex;
            flex-wrap: wrap;
            gap: 16px;
            font-size: 14px;

            .amount {
              font-weight: 600;
            }
          }
        }

        .order-actions {
          display: flex;
          gap: 8px;
          flex-wrap: wrap;
        }
      }
    }

    .view-all {
      text-align: right;
      margin-top: 12px;
    }
  }

  .quick-actions-card {
    margin-bottom: 24px;
    padding: 24px;
    border: 2px solid #e6a23c;
    border-radius: 12px;
    background: linear-gradient(135deg, #ffffff 0%, #fffbf5 100%);
    box-shadow: 0 4px 20px rgba(230, 162, 60, 0.12);

    .card-title {
      font-size: 20px;
      font-weight: 700;
      margin-bottom: 24px;
      color: #e6a23c;
      display: flex;
      align-items: center;
      gap: 8px;
      padding-bottom: 16px;
      border-bottom: 2px solid #f5e6d3;
    }

    .actions-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
      gap: 16px;

      .action-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 28px 20px;
        border: 2px solid #f5e6d3;
        border-radius: 12px;
        cursor: pointer;
        transition: all 0.3s ease;
        background: linear-gradient(135deg, #ffffff 0%, #fffbf8 100%);
        position: relative;
        overflow: hidden;

        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: linear-gradient(135deg, #ffe7ba 0%, #ffffff 100%);
          opacity: 0;
          transition: opacity 0.3s ease;
        }

        &:hover {
          transform: translateY(-8px);
          box-shadow: 0 8px 24px rgba(230, 162, 60, 0.25);
          border-color: #e6a23c;

          &::before {
            opacity: 1;
          }
        }

        .action-icon {
          font-size: 52px;
          margin-bottom: 12px;
          transition: all 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
          transform-origin: center center;
          position: relative;
          z-index: 1;
        }

        &:hover .action-icon {
          transform: scale(1.2) rotate(5deg);
        }

        .action-label {
          font-size: 15px;
          font-weight: 600;
          color: #303133;
          position: relative;
          z-index: 1;
        }
      }
    }
  }

  // 今日菜单
  .today-menu-card {
    margin-bottom: 24px;
    padding: 24px; /* 添加内边距 */
    border: 2px solid #67c23a; /* 绿色主题边框 */
    border-radius: 12px; /* 统一圆角 */
    background-color: #ffffff; /* 白色背景 */
    box-shadow: 0 4px 20px rgba(103, 194, 58, 0.08); /* 增强阴影 */

    .menu-header {
      display: flex;
      justify-content: flex-start;
      align-items: center;
      margin-bottom: 28px; /* 增加底部间距 */
      flex-wrap: wrap;
      gap: 24px; /* 增加整体间距 */

      // 处理只有标题的情况 (第一行)
      &:has(.card-title) {
        padding-bottom: 16px; /* 添加底部内边距 */
        border-bottom: 1px solid #f0f9eb; /* 添加分隔线 */
        margin-bottom: 24px; /* 调整标题行与筛选行的间距 */
      }

      .card-title {
        font-size: 20px;
        font-weight: 700;
        margin: 0;
        color: #67c23a; /* 绿色主题标题 */
      }

      .filter-label {
        font-weight: 600; /* 加粗标签 */
        margin-right: 12px; /* 增加标签右侧间距 */
        color: #606266;
        font-size: 14px;
      }

      .filter-section {
        display: flex;
        align-items: center;
        gap: 20px; /* 增加标签之间的间距 */
        flex-wrap: wrap;

        .menu-filter-tag,
        .menu-status-tag {
          cursor: pointer;
          transition: all 0.3s ease;
          border-radius: 20px;
          margin-right: 12px;
          margin-bottom: 8px;

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 3px 12px rgba(0, 0, 0, 0.15);
          }

          &.active {
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.18);
          }
        }
      }
    }

    .menu-list {
      margin-bottom: 20px;

      .menu-item,
      .menu-card {
        padding: 20px;
        border: 2px solid #eaf5ec; /* 淡绿色边框 */
        border-radius: 10px;
        margin-bottom: 16px;
        background-color: #fff;
        transition: all 0.3s ease;
        cursor: pointer;

        &:hover {
          box-shadow: 0 4px 16px rgba(103, 194, 58, 0.12); /* 绿色主题阴影 */
          border-color: #67c23a;
          transform: translateY(-4px);
        }

        &.active {
          border-color: #67c23a;
          box-shadow: 0 4px 16px rgba(103, 194, 58, 0.15);
          background-color: #f0f9eb; /* 淡绿色背景 */
        }

        .menu-info {
          .menu-name {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 16px;

            .name {
              font-size: 18px;
              font-weight: 600;
              color: #303133;
            }
          }

          .menu-stats,
          .auto-times {
            display: flex;
            flex-wrap: wrap;
            gap: 24px;
            margin-bottom: 8px;
            font-size: 14px;

            .dishes-count {
              color: #67c23a;
              font-weight: 500;
            }
          }

          .auto-times {
            font-size: 13px;
            color: #909399;
          }
        }
      }

      .empty-menu {
        text-align: center;
        padding: 80px 20px; /* 增加上下内边距 */
        color: #909399;
        font-size: 18px;
        background-color: #f7fff9; /* 淡绿色背景 */
        border: 2px dashed #67c23a; /* 绿色虚线边框 */
        border-radius: 12px;
        margin-bottom: 28px; /* 与其他元素保持一致的间距 */
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05); /* 轻微阴影 */
        transition: all 0.3s ease; /* 平滑过渡效果 */

        &:hover {
          box-shadow: 0 4px 16px rgba(103, 194, 58, 0.1); /* 悬停时增强阴影 */
          background-color: #eaf5ec; /* 悬停时加深背景色 */
        }

        span {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8px; /* 文字和图标间距 */
        }
      }
    }

    .view-all {
      text-align: right;
      margin-top: 24px;

      .el-button {
        color: #67c23a;
        border-color: #67c23a;
        transition: all 0.3s ease;
        transform-origin: center center;

        &:hover {
          background-color: #67c23a;
          color: #fff;
          transform: scale(1.05);
        }
      }
    }
  }
}

// 菜品列表样式
.dishes-card {
  margin-bottom: 24px;
  padding: 24px; /* 添加内边距 */
  border: 2px solid #67c23a; /* 绿色边框 */
  border-radius: 12px; /* 统一圆角 */
  background-color: #ffffff; /* 白色背景 */
  box-shadow: 0 4px 20px rgba(103, 194, 58, 0.08); /* 增强阴影 */
  border-top: none;
  border-top-left-radius: 0;
  border-top-right-radius: 0;

  .dish-list {
    margin-bottom: 20px;

    .dish-item {
      padding: 20px;
      border: 2px solid #f0f9eb; /* 淡绿色边框 */
      border-radius: 10px;
      margin-bottom: 16px;
      background-color: #fff;
      transition: all 0.3s ease;
      display: flex;
      align-items: flex-start;
      gap: 16px;
      overflow: hidden;

      &:hover {
        box-shadow: 0 4px 16px rgba(103, 194, 58, 0.12); /* 绿色主题阴影 */
        border-color: #67c23a;
        transform: translateY(-4px);
      }

      .dish-cover {
        font-size: 48px;
        width: 90px;
        height: 90px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: linear-gradient(135deg, #67c23a, #eaf5ec); /* 绿色渐变背景 */
        border-radius: 10px;
        flex-shrink: 0;
        color: #fff;
        box-shadow: 0 2px 8px rgba(103, 194, 58, 0.2);
        transition: all 0.3s ease;
      }

      &:hover .dish-cover {
        transform: scale(1.1);
      }

      .dish-info {
        flex: 1;

        .dish-name {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 10px;

          .name {
            font-size: 18px;
            font-weight: 600;
            color: #303133;
          }
        }

        .dish-desc {
          font-size: 14px;
          color: #606266;
          margin-bottom: 14px;
          line-height: 1.6;
        }

        .dish-stats {
          display: flex;
          flex-wrap: wrap;
          gap: 20px;
          font-size: 14px;
          color: #606266;

          .dish-category {
            background-color: #eaf5ec;
            color: #67c23a;
            padding: 4px 12px;
            border-radius: 6px;
            font-size: 12px;
            font-weight: 500;
          }

          .dish-price {
            color: #e6a23c;
            font-weight: 600;
            font-size: 16px;
          }

          .dish-stock {
            font-size: 13px;
            font-weight: 500;

            &.stock-almost {
              color: #f59f00;
            }

            &.stock-off {
              color: #f56c6c;
            }
          }
        }
      }

      .dish-actions {
        display: flex;
        flex-direction: column;
        gap: 10px;
        flex-shrink: 0;

        .el-button {
          width: 90px;
          transition: all 0.3s ease;

          &:hover {
            transform: translateY(-2px);
          }
        }
      }
    }
  }
}
</style>
