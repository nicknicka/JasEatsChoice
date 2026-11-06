<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Wallet,
  List,
  Star,
  Bell,
  Plus,
  PriceTag,
  Service,
  ArrowRight,
  CircleCheck
} from '@element-plus/icons-vue'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import AppCard from '../../components/common/AppCard.vue'
import AppStatCard from '../../components/common/AppStatCard.vue'
import { useAuthStore } from '../../store/authStore'
import api from '../../utils/api.js'
import { normalizeOrderStatusCode } from '../../utils/orderStatus'
// 下半屏经营区块沿用既有实现，本页只重排首屏层级
import MerchantInfo from '../../components/merchant/MerchantInfo.vue'
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

// 今日待处理：首屏主角（§4.7、§4.8）
const pendingOrders = ref(0)
const overview = ref({
  sales: 0,
  orders: 0,
  newComments: 0,
  unreadMessages: 0
})
const ready = ref(false)

const salesText = computed(() => Number(overview.value.sales || 0).toFixed(0))

// 商家后台货架只有「待接单 / 制作中」需要商家处理，二者构成今天的工作量
const PENDING_STATUS_CODES = [1, 2]

const fetchPendingOrders = () => {
  if (!merchantId) {
    return Promise.resolve()
  }
  return api
    .get(`/v1/orders/merchant/${merchantId}?today=true`)
    .then((response) => {
      if (response.success && Array.isArray(response.data)) {
        pendingOrders.value = response.data.filter((order) =>
          PENDING_STATUS_CODES.includes(normalizeOrderStatusCode(order.status))
        ).length
      }
    })
    .catch((error) => {
      console.error('获取今日待处理订单失败:', error)
    })
}

const fetchOverview = () => {
  if (!merchantId) {
    return Promise.resolve()
  }
  return api
    .get(`/v1/merchant/${merchantId}/business-overview`)
    .then((response) => {
      if (response.code === '200' && response.data) {
        overview.value = response.data
      }
    })
    .catch((error) => {
      console.error('获取营业概览数据失败:', error)
    })
}

onMounted(() => {
  Promise.allSettled([fetchOverview(), fetchPendingOrders()]).finally(() => {
    ready.value = true
  })
})

const goPendingOrders = () => {
  router.push('/merchant/home/orders?status=1')
}

const goMessages = () => {
  router.push('/merchant/home/messages')
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
</script>

<template>
  <div class="merchant-home">
    <!-- 页头：merchant 用紧凑版，标题降一档（§4.7） -->
    <AppPageHeader :compact="true" title="商家首页" />

    <!-- 首屏主角：今日待处理订单与未读消息（§4.7、§4.8） -->
    <AppCard class="todo-card" padding="none">
      <div v-if="ready && pendingOrders === 0 && overview.unreadMessages === 0" class="todo-empty">
        <el-icon class="todo-empty__icon" aria-hidden="true"><CircleCheck /></el-icon>
        <p class="todo-empty__text">今天没有待处理的订单和未读消息</p>
      </div>
      <div v-else class="todo-grid">
        <button type="button" class="todo-tile" @click="goPendingOrders">
          <span class="todo-tile__icon todo-tile__icon--orders">
            <el-icon aria-hidden="true"><List /></el-icon>
          </span>
          <span class="todo-tile__body">
            <span class="todo-tile__value">{{ pendingOrders }}</span>
            <span class="todo-tile__label">待处理订单</span>
          </span>
          <span class="todo-tile__cta">
            去处理<el-icon aria-hidden="true"><ArrowRight /></el-icon>
          </span>
        </button>
        <button type="button" class="todo-tile" @click="goMessages">
          <span class="todo-tile__icon todo-tile__icon--messages">
            <el-icon aria-hidden="true"><Bell /></el-icon>
          </span>
          <span class="todo-tile__body">
            <span class="todo-tile__value">{{ overview.unreadMessages }}</span>
            <span class="todo-tile__label">未读消息</span>
          </span>
          <span class="todo-tile__cta">
            去查看<el-icon aria-hidden="true"><ArrowRight /></el-icon>
          </span>
        </button>
      </div>
    </AppCard>

    <!-- 今日数据：线性图标 + 数值 + 标签（§4.8） -->
    <section class="stat-grid" aria-label="今日经营数据">
      <AppStatCard label="营业额" :value="`¥${salesText}`" :icon="Wallet" />
      <AppStatCard label="订单数" :value="overview.orders" :icon="List" />
      <AppStatCard label="新增评价" :value="overview.newComments" :icon="Star" />
    </section>

    <!-- 店铺资料 -->
    <section aria-label="店铺资料">
      <MerchantInfo />
    </section>

    <!-- 订单中心：首屏即可露出列表或空状态 -->
    <section aria-label="订单中心">
      <OrderCenter />
    </section>

    <!-- 今日菜单 -->
    <section aria-label="今日菜单">
      <TodayMenu />
    </section>

    <!-- 优惠管理 -->
    <section aria-label="优惠管理">
      <DiscountManagement :merchant-id="String(merchantId)" />
    </section>

    <!-- 公告栏配置 -->
    <section aria-label="公告栏配置">
      <AnnouncementManagement :merchant-id="String(merchantId)" />
    </section>

    <!-- 店铺相册 -->
    <section aria-label="店铺相册">
      <ShopAlbum :merchant-id="String(merchantId)" />
    </section>

    <!-- 快捷操作 -->
    <AppCard class="quick-card" padding="none">
      <h2 class="section-title">快捷操作</h2>
      <div class="quick-grid">
        <button type="button" class="quick-item" @click="navigateToMenu">
          <el-icon class="quick-item__icon" aria-hidden="true"><Plus /></el-icon>
          <span class="quick-item__label">新增菜单</span>
        </button>
        <button type="button" class="quick-item" @click="setDiscount">
          <el-icon class="quick-item__icon" aria-hidden="true"><PriceTag /></el-icon>
          <span class="quick-item__label">设置优惠</span>
        </button>
        <button type="button" class="quick-item" @click="contactCustomerService">
          <el-icon class="quick-item__icon" aria-hidden="true"><Service /></el-icon>
          <span class="quick-item__label">联系客服</span>
        </button>
      </div>
    </AppCard>
  </div>
</template>

<style scoped lang="less">
/* 页面颜色、间距、圆角、字阶、动效全部取自 tokens.css（§3、§7.1），无裸色值。 */
.merchant-home {
  box-sizing: border-box;
  width: 100%;
  max-width: 1400px;
  min-height: 100%;
  margin: 0 auto;
  padding: var(--space-6) var(--space-6) var(--space-12);
  background: var(--canvas);
}

/* ===== 今日待处理 ===== */
.todo-card {
  margin-bottom: var(--space-5);
}

.todo-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--card-gap-compact);
  padding: var(--card-padding-compact);
}

.todo-tile {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  width: 100%;
  padding: var(--space-4);
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  background: var(--surface);
  color: var(--ink-900);
  font: inherit;
  text-align: left;
  cursor: pointer;
  transition:
    border-color var(--duration-fast) var(--ease-out),
    box-shadow var(--duration-fast) var(--ease-out),
    transform var(--duration-fast) var(--ease-out);
}

.todo-tile:hover {
  border-color: var(--brand-400);
  box-shadow: var(--shadow-sm);
  transform: translateY(-1px);
}

.todo-tile:active {
  transform: translateY(0);
  box-shadow: none;
}

.todo-tile:focus-visible {
  outline: 2px solid var(--brand-400);
  outline-offset: 2px;
}

.todo-tile__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: var(--control-height-md);
  height: var(--control-height-md);
  border-radius: var(--radius-md);
  font-size: var(--text-h3);
}

.todo-tile__icon--orders {
  background: var(--warning-bg);
  color: var(--warning);
}

.todo-tile__icon--messages {
  background: var(--info-bg);
  color: var(--info);
}

.todo-tile__body {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  min-width: 0;
}

.todo-tile__value {
  font-family: var(--font-num);
  font-size: var(--text-h1);
  font-weight: var(--weight-bold);
  font-variant-numeric: tabular-nums;
  line-height: 1;
  color: var(--ink-900);
}

.todo-tile__label {
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  color: var(--ink-500);
}

.todo-tile__cta {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  margin-left: auto;
  flex-shrink: 0;
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  color: var(--brand-600);
}

.todo-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  padding: var(--space-6) var(--card-padding-compact);
}

.todo-empty__icon {
  color: var(--success);
  font-size: var(--text-h2);
}

.todo-empty__text {
  margin: 0;
  font-size: var(--text-body);
  color: var(--ink-500);
}

/* ===== 今日数据 ===== */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--card-gap);
  margin-bottom: var(--space-5);
}

/* merchant 密度：卡片内边距取 --card-padding-compact（§4.2） */
.stat-grid :deep(.app-stat-card) {
  padding: var(--card-padding-compact);
}

/* ===== 快捷操作 ===== */
.quick-card {
  padding: var(--card-padding-compact);
}

.section-title {
  margin: 0 0 var(--space-3);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
  color: var(--ink-900);
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--card-gap-compact);
}

.quick-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  min-height: var(--control-height-md);
  padding: 0 var(--space-4);
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  background: var(--surface);
  color: var(--ink-700);
  font: inherit;
  font-size: var(--text-body);
  font-weight: var(--weight-medium);
  cursor: pointer;
  transition:
    border-color var(--duration-fast) var(--ease-out),
    color var(--duration-fast) var(--ease-out),
    background var(--duration-fast) var(--ease-out);
}

.quick-item:hover {
  border-color: var(--brand-400);
  color: var(--brand-600);
  background: var(--brand-50);
}

.quick-item:active {
  background: var(--brand-100);
}

.quick-item:focus-visible {
  outline: 2px solid var(--brand-400);
  outline-offset: 2px;
}

.quick-item__icon {
  font-size: var(--text-h3);
}

/* ===== 响应式：窄屏堆叠（§3.3） ===== */
@media (max-width: 768px) {
  .merchant-home {
    padding: var(--space-4) var(--space-4) var(--space-8);
  }

  .todo-grid,
  .stat-grid,
  .quick-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
