<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  Document,
  VideoCamera,
  View,
  Star,
  TrendCharts,
  User,
  Shop,
  Checked,
  Clock
} from '@element-plus/icons-vue'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 统计数据
const stats = ref({
  totalTutorials: 0,
  publishedTutorials: 0,
  pendingReview: 0,
  totalViews: 0,
  averageRating: 0
})

// 最近教程
const recentTutorials = ref([])
const loading = ref(false)

// 快捷操作卡片
const quickActions = [
  {
    title: '教程审核',
    description: '查看待审核的教程',
    icon: Checked,
    color: '#ff6b6b',
    route: '/admin/tutorials/review'
  },
  {
    title: '教程管理',
    description: '管理所有教程',
    icon: Document,
    color: '#4ecdc4',
    route: '/admin/tutorials/manage'
  },
  {
    title: '用户管理',
    description: '管理系统用户',
    icon: User,
    color: '#ffe66d',
    route: '/admin/users'
  },
  {
    title: '商家管理',
    description: '管理合作商家',
    icon: Shop,
    color: '#1a535c',
    route: '/admin/merchants'
  }
]

// 来源分布
const sourceDistribution = ref([
  { name: '管理员', value: 0, color: '#ff6b6b' },
  { name: '商家', value: 0, color: '#feca57' },
  { name: 'AI生成', value: 0, color: '#48dbfb' }
])

// 获取统计数据
const fetchStats = async () => {
  try {
    // TODO: 调用实际的统计API
    // const response = await api.get(API_CONFIG.tutorial.stats + '/overview')
    // stats.value = response.data

    // 临时使用模拟数据
    stats.value = {
      totalTutorials: 156,
      publishedTutorials: 142,
      pendingReview: 8,
      totalViews: 45680,
      averageRating: 4.6
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
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
    // 使用模拟数据
    recentTutorials.value = [
      {
        id: 7,
        title: '青木瓜沙拉制作教程',
        type: 'video',
        source_type: 'ADMIN',
        status: 'PUBLISHED',
        view_count: 12500,
        rating: 4.8,
        create_time: '2025-01-29'
      },
      {
        id: 8,
        title: '夏日低卡饮食指南',
        type: 'article',
        source_type: 'ADMIN',
        status: 'PUBLISHED',
        view_count: 8200,
        rating: 4.9,
        create_time: '2025-01-28'
      },
      {
        id: 10,
        title: '秘制红烧肉做法',
        type: 'video',
        source_type: 'MERCHANT',
        status: 'PENDING',
        view_count: 0,
        rating: 0,
        create_time: '2025-01-27'
      }
    ]
  } finally {
    loading.value = false
  }
}

// 导航到页面
const navigateTo = (path) => {
  router.push(path)
}

// 获取来源类型标签样式
const getSourceTypeStyle = (type) => {
  const styles = {
    ADMIN: { color: '#ff6b6b', bg: '#fff0f0' },
    MERCHANT: { color: '#feca57', bg: '#fff8e1' },
    AI_GENERATED: { color: '#48dbfb', bg: '#e0f2ff' }
  }
  return styles[type] || { color: '#909399', bg: '#f5f5f5' }
}

// 获取状态标签样式
const getStatusStyle = (status) => {
  const styles = {
    PUBLISHED: { color: '#67c23a', bg: '#f0f9ff' },
    PENDING: { color: '#e6a23c', bg: '#fdf6ec' },
    DRAFT: { color: '#909399', bg: '#f5f5f5' },
    REJECTED: { color: '#f56c6c', bg: '#fef0f0' }
  }
  return styles[status] || { color: '#909399', bg: '#f5f5f5' }
}

// 页面加载时获取数据
onMounted(() => {
  fetchStats()
  fetchRecentTutorials()
})
</script>

<template>
  <div class="admin-dashboard-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>管理员控制台</h1>
      <p class="subtitle">欢迎回来，管理员</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card primary">
        <div class="stat-icon">
          <el-icon :size="40"><Document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalTutorials }}</div>
          <div class="stat-label">教程总数</div>
        </div>
      </div>

      <div class="stat-card success">
        <div class="stat-icon">
          <el-icon :size="40"><Checked /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.publishedTutorials }}</div>
          <div class="stat-label">已发布</div>
        </div>
      </div>

      <div class="stat-card warning">
        <div class="stat-icon">
          <el-icon :size="40"><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.pendingReview }}</div>
          <div class="stat-label">待审核</div>
        </div>
      </div>

      <div class="stat-card info">
        <div class="stat-icon">
          <el-icon :size="40"><View /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalViews.toLocaleString() }}</div>
          <div class="stat-label">总浏览量</div>
        </div>
      </div>

      <div class="stat-card rating">
        <div class="stat-icon">
          <el-icon :size="40"><Star /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.averageRating }}</div>
          <div class="stat-label">平均评分</div>
        </div>
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
          <div class="action-icon" :style="{ backgroundColor: action.color + '20' }">
            <el-icon :size="32" :style="{ color: action.color }">
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

      <el-table :data="recentTutorials" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="教程标题" min-width="200" />

        <el-table-column label="来源" width="120">
          <template #default="{ row }">
            <el-tag
              :style="{
                backgroundColor: getSourceTypeStyle(row.source_type).bg,
                color: getSourceTypeStyle(row.source_type).color,
                border: 'none'
              }"
              size="small"
            >
              {{ row.source_type === 'ADMIN' ? '管理员' : row.source_type === 'MERCHANT' ? '商家' : 'AI' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :style="{
                backgroundColor: getStatusStyle(row.status).bg,
                color: getStatusStyle(row.status).color,
                border: 'none'
              }"
              size="small"
            >
              {{ row.status === 'PUBLISHED' ? '已发布' : row.status === 'PENDING' ? '待审核' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="浏览量" width="120">
          <template #default="{ row }">
            <span>{{ row.view_count?.toLocaleString() || 0 }}</span>
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
                :colors="['#ff6b6b', '#ff6b6b', '#ff6b6b']"
              />
            </span>
            <span v-else style="color: #909399">暂无</span>
          </template>
        </el-table-column>

        <el-table-column prop="create_time" label="创建时间" width="120" />
      </el-table>
    </div>
  </div>
</template>

<style scoped lang="less">
.admin-dashboard-container {
  padding: 24px;

  .page-header {
    margin-bottom: 32px;

    h1 {
      font-size: 28px;
      color: #303133;
      margin: 0 0 8px 0;
    }

    .subtitle {
      color: #909399;
      margin: 0;
      font-size: 14px;
    }
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
    gap: 20px;
    margin-bottom: 32px;

    .stat-card {
      background: white;
      border-radius: 12px;
      padding: 24px;
      display: flex;
      align-items: center;
      gap: 20px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
      transition: all 0.3s;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      }

      .stat-icon {
        width: 64px;
        height: 64px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      &.primary .stat-icon {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
      }

      &.success .stat-icon {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        color: white;
      }

      &.warning .stat-icon {
        background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
        color: white;
      }

      &.info .stat-icon {
        background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);
        color: white;
      }

      &.rating .stat-icon {
        background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
        color: white;
      }

      .stat-info {
        flex: 1;

        .stat-value {
          font-size: 32px;
          font-weight: bold;
          color: #303133;
          margin-bottom: 4px;
        }

        .stat-label {
          font-size: 14px;
          color: #909399;
        }
      }
    }
  }

  .section {
    background: white;
    border-radius: 12px;
    padding: 24px;
    margin-bottom: 24px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

    h2 {
      font-size: 20px;
      color: #303133;
      margin: 0 0 20px 0;
    }

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      h2 {
        margin: 0;
      }
    }
  }

  .quick-actions-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 20px;

    .quick-action-card {
      padding: 20px;
      border-radius: 12px;
      border: 2px solid #f5f5f5;
      display: flex;
      align-items: center;
      gap: 20px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        border-color: #409eff;
        background: #f0f7ff;
        transform: translateY(-2px);
      }

      .action-icon {
        width: 64px;
        height: 64px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      .action-info {
        flex: 1;

        h3 {
          font-size: 18px;
          color: #303133;
          margin: 0 0 6px 0;
        }

        p {
          font-size: 14px;
          color: #909399;
          margin: 0;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .admin-dashboard-container {
    padding: 16px;

    .stats-grid {
      grid-template-columns: 1fr;
    }

    .quick-actions-grid {
      grid-template-columns: 1fr;
    }
  }
}
</style>
