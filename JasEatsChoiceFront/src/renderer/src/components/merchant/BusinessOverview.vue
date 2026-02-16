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
.overview-card {
  margin-bottom: 24px;
  padding: 24px;
  border: 2px solid #67c23a;
  border-radius: 12px;
  background-color: #ffffff;
  box-shadow: 0 4px 20px rgba(103, 194, 58, 0.12);

  .card-title {
    font-size: 1.429rem /* 原值: 20px */;
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
      }

      &.orders {
        border-left: 4px solid #409eff;
      }

      &.newComments {
        border-left: 4px solid #e6a23c;
      }

      &.unreadMessages {
        border-left: 4px solid #f56c6c;
      }

      .item-icon {
        font-size: 2.286rem /* 原值: 32px */;
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
          font-size: 1rem /* 原值: 14px */;
          color: #909399;
          margin-bottom: 4px;
          font-weight: 500;
        }

        .overview-value {
          font-size: 1.714rem /* 原值: 24px */;
          font-weight: 700;
          margin-bottom: 4px;
        }

        .item-trend {
          font-size: 0.857rem /* 原值: 12px */;
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
</style>
