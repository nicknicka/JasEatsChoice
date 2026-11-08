<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Document, User, Shop, Checked } from '@element-plus/icons-vue'
import { getDashboardStats } from '@/api/admin'
import { API_CONFIG } from '../../config/index.js'
import { ElMessage } from 'element-plus'
import api from '../../utils/api.js'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import AppStatCard from '@/components/common/AppStatCard.vue'
import AppEmptyState from '@/components/common/AppEmptyState.vue'
import StatusTag from '@/components/common/StatusTag.vue'

const router = useRouter()

// 统计数据
const stats = ref({
  totalUsers: null,
  totalMerchants: null,
  newOrders: null,
  newRevenue: null,
  newUsers: null
})
const statsError = ref('')

const formatRevenue = (value) => {
  return value == null ? '--' : `¥${Number(value).toFixed(2)}`
}

const secondaryStats = computed(() => [
  { label: '用户总数', value: stats.value.totalUsers ?? '--' },
  { label: '商家总数', value: stats.value.totalMerchants ?? '--' },
  { label: '今日收入', value: formatRevenue(stats.value.newRevenue) },
  { label: '今日新增用户', value: stats.value.newUsers ?? '--' }
])

// 最近教程
const recentTutorials = ref([])
const loading = ref(false)

// 快捷操作卡片
const quickActions = [
  {
    title: '教程审核',
    description: '查看待审核的教程',
    icon: Checked,
    route: '/admin/tutorials/review'
  },
  {
    title: '教程管理',
    description: '管理所有教程',
    icon: Document,
    route: '/admin/tutorials/manage'
  },
  {
    title: '用户管理',
    description: '管理系统用户',
    icon: User,
    route: '/admin/users'
  },
  {
    title: '商家管理',
    description: '管理合作商家',
    icon: Shop,
    route: '/admin/merchants'
  }
]

// 获取统计数据
const fetchStats = async () => {
  try {
    console.log('[Dashboard] 获取控制台统计数据')
    const response = await getDashboardStats()

    if (response.success && response.data?.summary) {
      const { totalUsers, totalMerchants, newOrders, newRevenue, newUsers } = response.data.summary
      stats.value = { totalUsers, totalMerchants, newOrders, newRevenue, newUsers }
      statsError.value = ''
      console.log('[Dashboard] 获取统计数据成功:', stats.value)
    } else {
      console.warn('[Dashboard] 获取统计数据返回格式异常:', response)
      statsError.value = response.message || '接口返回数据格式异常'
      ElMessage.error(`获取统计数据失败: ${statsError.value}`)
    }
  } catch (error) {
    console.error('[Dashboard] 获取统计数据失败:', error)
    statsError.value = error.message || '网络错误'
    ElMessage.error(`获取统计数据失败: ${statsError.value}`)
  }
}

// 获取最近教程
const fetchRecentTutorials = async () => {
  loading.value = true
  try {
    const response = await api.get(API_CONFIG.tutorial.list)
    if (response.data) {
      recentTutorials.value = response.data.slice(0, 5)
    }
  } catch (error) {
    console.error('获取最近教程失败:', error)
    ElMessage.error('获取最近教程失败，请稍后重试')
    recentTutorials.value = []
  } finally {
    loading.value = false
  }
}

// 导航到页面
const navigateTo = (path) => {
  router.push(path)
}

// 来源标签：交给 StatusTag 统一取色（§3.1、§4.6）
const sourceStatus = (type) => {
  const map = { ADMIN: 'info', MERCHANT: 'warning', AI_GENERATED: 'success' }
  return map[type] || 'neutral'
}

const sourceLabel = (type) => {
  const map = { ADMIN: '管理员', MERCHANT: '商家', AI_GENERATED: 'AI' }
  return map[type] || '未知'
}

// 教程状态词典：按 §3.1 状态表映射
const tutorialStatusKey = (status) => {
  const map = {
    PUBLISHED: 'active',
    PENDING: 'pendingreview',
    DRAFT: 'draft',
    REJECTED: 'rejected'
  }
  return map[status] || 'unknown'
}

// 页面加载时获取数据
onMounted(() => {
  fetchStats()
  fetchRecentTutorials()
})
</script>

<template>
  <div class="admin-dashboard-container">
    <!-- 页头：统一标题容器与紧凑密度（§4.7） -->
    <AppPageHeader title="管理员控制台" subtitle="欢迎回来，管理员" compact />

    <el-alert
      v-if="statsError"
      class="stats-error"
      title="统计数据加载失败"
      :description="statsError"
      type="error"
      :closable="false"
      show-icon
    />

    <!-- 概览：1 个主指标 + 4 个次级条，避免 5 卡 4+1 空网格（§4.8、§7.1） -->
    <div class="stats-grid">
      <AppStatCard
        class="stat-card stats-primary"
        label="今日订单"
        :value="stats.newOrders ?? '--'"
        :icon="Document"
      />
      <div v-for="stat in secondaryStats" :key="stat.label" class="stat-card stats-secondary">
        <span class="stats-secondary__value">{{ stat.value }}</span>
        <span class="stats-secondary__label">{{ stat.label }}</span>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="section">
      <h2>快捷操作</h2>
      <div class="quick-actions-grid">
        <div
          v-for="action in quickActions"
          :key="action.title"
          class="quick-action-card"
          @click="navigateTo(action.route)"
        >
          <div class="action-icon">
            <el-icon :size="32">
              <component :is="action.icon" />
            </el-icon>
          </div>
          <div class="action-info">
            <h3>{{ action.title }}</h3>
            <p>{{ action.description }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 最近教程 -->
    <div class="section">
      <div class="section-header">
        <h2>最近教程</h2>
        <el-button text type="primary" @click="navigateTo('/admin/tutorials/manage')">
          查看全部 →
        </el-button>
      </div>

      <AppEmptyState
        v-if="!loading && recentTutorials.length === 0"
        compact
        description="暂时没有最近教程"
        action-text="去教程管理"
        @action="navigateTo('/admin/tutorials/manage')"
      />

      <el-table v-else v-loading="loading" :data="recentTutorials" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="教程标题" min-width="200" />

        <el-table-column label="来源" width="120">
          <template #default="{ row }">
            <StatusTag :status="sourceStatus(row.sourceType)" :text="sourceLabel(row.sourceType)" />
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :status="tutorialStatusKey(row.status)" />
          </template>
        </el-table-column>

        <el-table-column label="浏览量" width="120">
          <template #default="{ row }">
            <span>{{ row.viewCount?.toLocaleString() || 0 }}</span>
          </template>
        </el-table-column>

        <el-table-column label="评分" width="100">
          <template #default="{ row }">
            <span v-if="row.rating > 0">
              <el-rate
                v-model="row.rating"
                disabled
                show-score
                size="small"
                :colors="['var(--warning)', 'var(--warning)', 'var(--warning)']"
              />
            </span>
            <span v-else class="muted">暂无</span>
          </template>
        </el-table-column>

        <el-table-column prop="created_at" label="创建时间" width="120" />
      </el-table>
    </div>
  </div>
</template>

<style scoped lang="less">
.admin-dashboard-container {
  box-sizing: border-box;
  width: 100%;
  max-width: 1440px;
  min-height: calc(100vh - 100px);
  margin: 0 auto;
  padding: var(--card-padding-compact) var(--page-gutter) var(--space-8);
  background: var(--canvas);

  .stats-error {
    margin-bottom: var(--space-4);
  }

  .stats-grid {
    display: grid;
    grid-template-columns: minmax(240px, 320px) repeat(4, minmax(0, 1fr));
    gap: var(--card-gap-compact);
    margin-bottom: var(--space-8);

    .stats-primary {
      min-width: 0;
    }

    .stats-primary :deep(.app-stat-card__value) {
      font-size: var(--text-h1);
    }

    .stats-secondary {
      display: flex;
      flex-direction: column;
      gap: var(--space-1);
      padding: var(--card-padding-compact);
      border: 1px solid var(--line);
      border-radius: var(--radius-lg);
      background: var(--surface);
      box-shadow: inset 0 1px 0 var(--surface-highlight);
    }

    .stats-secondary__value {
      overflow: hidden;
      color: var(--ink-900);
      font-family: var(--font-num);
      font-size: var(--text-h2);
      font-weight: var(--weight-semibold);
      font-variant-numeric: tabular-nums;
      line-height: 1.2;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .stats-secondary__label {
      color: var(--ink-500);
      font-size: var(--text-body-sm);
    }
  }

  .section {
    padding: var(--card-padding-compact) var(--space-6);
    margin-bottom: var(--space-6);
    border: 1px solid var(--line);
    border-radius: var(--radius-lg);
    background: var(--surface);
    box-shadow: inset 0 1px 0 var(--surface-highlight);

    h2 {
      margin: 0 0 var(--space-5);
      color: var(--ink-900);
      font-size: var(--text-h2);
      font-weight: var(--weight-semibold);
      letter-spacing: var(--tracking-tight);
      line-height: 28px;
    }

    .section-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: var(--space-3);
      margin-bottom: var(--space-5);

      h2 {
        margin: 0;
      }
    }
  }

  .quick-actions-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
    gap: var(--card-gap-compact);

    .quick-action-card {
      display: flex;
      align-items: center;
      gap: var(--space-4);
      padding: var(--space-4);
      border: 1px solid var(--line);
      border-radius: var(--radius-lg);
      background: var(--surface);
      cursor: pointer;
      transition:
        border-color var(--duration-fast) var(--ease-out),
        box-shadow var(--duration-fast) var(--ease-out),
        transform var(--duration-fast) var(--ease-out);

      &:hover {
        border-color: var(--line-strong);
        box-shadow: var(--shadow-md);
        transform: translateY(-1px);
      }

      &:focus-visible {
        outline: 2px solid var(--brand-200);
        outline-offset: 2px;
      }

      .action-icon {
        display: flex;
        flex: 0 0 auto;
        align-items: center;
        justify-content: center;
        width: 56px;
        height: 56px;
        border-radius: var(--radius-lg);
        background: var(--brand-50);
        color: var(--brand-500);
      }

      .action-info {
        flex: 1;
        min-width: 0;

        h3 {
          margin: 0 0 var(--space-1);
          overflow: hidden;
          color: var(--ink-900);
          font-size: var(--text-h3);
          font-weight: var(--weight-semibold);
          line-height: 24px;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        p {
          margin: 0;
          overflow: hidden;
          color: var(--ink-500);
          font-size: var(--text-body-sm);
          line-height: 20px;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }
  }

  .muted {
    color: var(--ink-400);
  }
}

@media (max-width: 1200px) {
  .admin-dashboard-container {
    .stats-grid {
      grid-template-columns: repeat(4, minmax(0, 1fr));

      .stats-primary {
        grid-column: 1 / -1;
        max-width: 320px;
      }
    }
  }
}

@media (max-width: 768px) {
  .admin-dashboard-container {
    padding-right: var(--space-4);
    padding-left: var(--space-4);

    .stats-grid {
      grid-template-columns: 1fr;

      .stats-primary {
        grid-column: auto;
        max-width: none;
      }
    }

    .quick-actions-grid {
      grid-template-columns: 1fr;
    }
  }
}
</style>
