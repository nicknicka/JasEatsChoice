<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../../utils/api.js'
import { useAuthStore } from '../../store/authStore'

const router = useRouter()

const businessOverview = ref({
  sales: 0,
  orders: 0,
  newComments: 0,
  unreadMessages: 0,
  salesTrend: '→ 0%',
  ordersTrend: '→ 0%',
  commentsTrend: '→ 0%',
  messagesTrend: '→ 0%'
})

const authStore = useAuthStore()
let merchantId = authStore.merchantId

// 如果 Pinia 中没有商家ID，尝试从 localStorage 读取
if (!merchantId) {
  const localStorageMerchantId = localStorage.getItem('auth_merchantId')
  if (localStorageMerchantId) {
    merchantId = localStorageMerchantId
    authStore.setMerchantId(localStorageMerchantId) // 更新到 Pinia 中
  }
}

// 概览项导航配置（使用计算属性动态获取趋势）
const overviewConfig = ref([
  {
    key: 'sales',
    icon: '💰',
    label: '营业额',
    onClick: () => router.push('/merchant/home/statistics'),
    trendClass: 'trend-up',
    suffix: '¥',
    trendKey: 'salesTrend'
  },
  {
    key: 'orders',
    icon: '🍽️',
    label: '订单数',
    onClick: () => router.push('/merchant/home/orders'),
    trendClass: 'trend-up',
    trendKey: 'ordersTrend'
  },
  {
    key: 'newComments',
    icon: '🌟',
    label: '新增评价',
    onClick: () => router.push('/merchant/home/comments'),
    trendClass: 'trend-down',
    trendKey: 'commentsTrend'
  },
  {
    key: 'unreadMessages',
    icon: '📞',
    label: '未读消息',
    onClick: () => router.push('/merchant/home/messages'),
    trendClass: 'trend-neutral',
    trendKey: 'messagesTrend'
  }
])

// 获取趋势样式类
const getTrendClass = (trend) => {
  if (!trend) return 'trend-neutral'
  if (trend.includes('↑')) return 'trend-up'
  if (trend.includes('↓')) return 'trend-down'
  return 'trend-neutral'
}

// 获取营业概览
const fetchBusinessOverview = () => {
  api
    .get(`/v1/merchant/${merchantId}/business-overview`)
    .then((response) => {
      if (response.code === '200' && response.data) {
        businessOverview.value = response.data
      }
    })
    .catch((error) => {
      console.error('获取营业概览数据失败:', error)
    })
}

onMounted(() => {
  fetchBusinessOverview()
})
</script>

<template>
  <div class="overview-card">
    <h3 class="card-title">📈 今日营业概览：</h3>
    <div class="overview-grid">
      <div
        v-for="item in overviewConfig"
        :key="item.key"
        class="overview-item"
        :class="item.key"
        @click="item.onClick"
      >
        <div class="item-icon">{{ item.icon }}</div>
        <div class="item-content">
          <div class="overview-label">{{ item.label }}</div>
          <div class="overview-value">
            {{ item.suffix || '' }}
            {{
              item.key === 'sales' ? businessOverview.sales.toFixed(0) : businessOverview[item.key]
            }}
          </div>
          <div class="item-trend" :class="getTrendClass(businessOverview[item.trendKey])">
            {{ businessOverview[item.trendKey] }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';
@import '../../assets/css/merchant-theme.less';

.overview-card {
  margin-bottom: @nordic-space-lg;
  padding: @nordic-space-lg;
  background: @merchant-surface;
  border-radius: @nordic-radius-lg;
  border: 1px solid @merchant-border;
  border-left: 4px solid @merchant-success;
  box-shadow: 0 1px 4px @merchant-shadow;
  transition: all @nordic-transition-base ease;

  &:hover {
    box-shadow: 0 8px 24px @merchant-shadow-hover;
    border-color: @merchant-primary;
  }

  .card-title {
    .merchant-section-title();
    display: flex;
    align-items: center;
    gap: @nordic-space-sm;
    margin-bottom: @nordic-space-lg;

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
      &.newComments::before { background: @merchant-warning; }
      &.unreadMessages::before { background: @merchant-error; }

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
          display: inline-block;

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
</style>
