<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Wallet, List, Star, Bell, TrendCharts } from '@element-plus/icons-vue'
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

// 概览项导航配置（使用计算属性动态获取趋势，图标统一 Element Plus 线性图标 §4.9）
const overviewConfig = [
  {
    key: 'sales',
    icon: Wallet,
    label: '营业额',
    onClick: () => router.push('/merchant/home/statistics'),
    suffix: '¥',
    trendKey: 'salesTrend'
  },
  {
    key: 'orders',
    icon: List,
    label: '订单数',
    onClick: () => router.push('/merchant/home/orders'),
    trendKey: 'ordersTrend'
  },
  {
    key: 'newComments',
    icon: Star,
    label: '新增评价',
    onClick: () => router.push('/merchant/home/comments'),
    trendKey: 'commentsTrend'
  },
  {
    key: 'unreadMessages',
    icon: Bell,
    label: '未读消息',
    onClick: () => router.push('/merchant/home/messages'),
    trendKey: 'messagesTrend'
  }
]

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
    <h3 class="card-title">
      <el-icon aria-hidden="true"><TrendCharts /></el-icon>
      <span>今日营业概览：</span>
    </h3>
    <div class="overview-grid">
      <div
        v-for="item in overviewConfig"
        :key="item.key"
        class="overview-item"
        @click="item.onClick"
      >
        <el-icon class="item-icon" aria-hidden="true"><component :is="item.icon" /></el-icon>
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
/* 颜色、间距、圆角、字阶、动效全部取自 tokens.css（§3、§7.1），无裸色值、无彩色左框（§3.1）。 */
.overview-card {
  margin-bottom: var(--space-5);
}

.card-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin: 0 0 var(--space-4);
  color: var(--ink-900);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);

  .el-icon {
    color: var(--brand-500);
    font-size: var(--text-h3);
  }
}

/* KPI：一个线性图标 + 数值 + 标签的浅底卡，L1 微质感，无双重边框（§4.8）。 */
.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--card-gap);

  .overview-item {
    display: flex;
    align-items: center;
    gap: var(--space-3);
    min-width: 0;
    padding: var(--card-padding-compact);
    background: var(--surface);
    border: 1px solid var(--line);
    border-radius: var(--radius-lg);
    box-shadow: inset 0 1px 0 var(--surface-highlight);
    cursor: pointer;
    transition:
      border-color var(--duration-fast) var(--ease-out),
      box-shadow var(--duration-fast) var(--ease-out);

    &:hover {
      border-color: var(--brand-400);
      box-shadow: var(--shadow-sm);
    }

    &:focus-visible {
      outline: 2px solid var(--brand-400);
      outline-offset: 2px;
    }

    .item-icon {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      width: var(--control-height-md);
      height: var(--control-height-md);
      border-radius: var(--radius-md);
      background: var(--brand-50);
      color: var(--brand-600);
      font-size: var(--text-h2);
    }

    .item-content {
      flex: 1;
      min-width: 0;

      .overview-label {
        margin-bottom: var(--space-1);
        color: var(--ink-500);
        font-size: var(--text-body-sm);
        font-weight: var(--weight-medium);
      }

      .overview-value {
        margin-bottom: var(--space-1);
        color: var(--ink-900);
        font-family: var(--font-num);
        font-size: var(--text-h1);
        font-weight: var(--weight-bold);
        font-variant-numeric: tabular-nums;
        letter-spacing: var(--tracking-tight);
        line-height: 1.1;
      }

      .item-trend {
        display: inline-block;
        padding: 2px var(--space-2);
        border-radius: var(--radius-full);
        font-size: var(--text-caption);
        font-weight: var(--weight-semibold);

        &.trend-up {
          color: var(--success);
          background: var(--success-bg);
        }

        &.trend-down {
          color: var(--danger);
          background: var(--danger-bg);
        }

        &.trend-neutral {
          color: var(--ink-500);
          background: var(--neutral-bg);
        }
      }
    }
  }
}
</style>
