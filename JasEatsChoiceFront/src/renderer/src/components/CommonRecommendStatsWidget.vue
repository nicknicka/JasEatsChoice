<template>
  <div class="recommend-stats-widget">
    <div class="stats-header">
      <h4 class="stats-title">📊 推荐效果</h4>
      <el-button type="text" size="small" @click="onRefresh">
        <el-icon :size="14">
          <Refresh />
        </el-icon>
      </el-button>
    </div>

    <div class="stats-grid">
      <!-- 总览览数 -->
      <div class="stat-card total-views">
        <div class="stat-icon">👁</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalViews }}</div>
          <div class="stat-label">总浏览数</div>
        </div>
      </div>

      <!-- 总点击数 -->
      <div class="stat-card total-clicks">
        <div class="stat-icon">👆</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalClicks }}</div>
          <div class="stat-label">总点击数</div>
        </div>
      </div>

      <!-- 总下单数 -->
      <div class="stat-card total-orders">
        <div class="stat-icon">🛒</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalOrders }}</div>
          <div class="stat-label">总下单数</div>
        </div>
      </div>

      <!-- 总拒绝数 -->
      <div class="stat-card total-rejects">
        <div class="stat-icon">🙅</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalRejects }}</div>
          <div class="stat-label">不感兴趣</div>
        </div>
      </div>
    </div>

    <!-- 转化率指标 -->
    <div class="metrics-row">
      <div class="metric-item">
        <span class="metric-label">点击率</span>
        <el-progress
          :percentage="clickRate"
          :color="getRateColor(clickRate)"
          :show-text="false"
        />
        <span class="metric-value">{{ clickRate }}%</span>
      </div>

      <div class="metric-item">
        <span class="metric-label">下单率</span>
        <el-progress
          :percentage="orderRate"
          :color="getRateColor(orderRate)"
          :show-text="false"
        />
        <span class="metric-value">{{ orderRate }}%</span>
      </div>
    </div>

    <!-- 缓存信息 -->
    <div class="cache-info">
      <div class="cache-item">
        <span class="cache-label">📦 缓存命中率</span>
        <span class="cache-value">{{ stats.cacheHitRate }}%</span>
      </div>
      <div class="cache-item">
        <span class="cache-label">⏰ 最后更新</span>
        <span class="cache-value">{{ formatTime(stats.lastUpdateTime) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Refresh } from '@element-plus/icons-vue'

const props = defineProps({
  stats: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['refresh'])

// 计算点击率
const clickRate = computed(() => {
  const views = props.stats.totalViews || 0
  const clicks = props.stats.totalClicks || 0
  return views > 0 ? Math.round((clicks / views) * 100) : 0
})

// 计算下单率
const orderRate = computed(() => {
  const clicks = props.stats.totalClicks || 0
  const orders = props.stats.totalOrders || 0
  return clicks > 0 ? Math.round((orders / clicks) * 100) : 0
})

// 获取转化率颜色
const getRateColor = (rate) => {
  if (rate >= 20) return '#67c23a' // 绿色
  if (rate >= 10) return '#e6a23c' // 橙色
  return '#f56c6c' // 红色
}

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return '未知'
  const date = new Date(timestamp)
  const now = new Date()
  const diff = Math.floor((now - date) / 1000) // 秒

  if (diff < 60) return `${diff}秒前`
  if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`
  return `${Math.floor(diff / 86400)}天前`
}

const onRefresh = () => {
  emit('refresh')
}
</script>

<style scoped lang="less">
.recommend-stats-widget {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

  .stats-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid #f0f0f0;

    .stats-title {
      font-size: 18px;
      font-weight: bold;
      margin: 0;
      color: #333;
    }
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
    gap: 16px;
    margin-bottom: 24px;
  }

  .stat-card {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    border-radius: 10px;
    background: linear-gradient(135deg, #f5f7fa 0%, #fcfcfc 100%);
    border: 1px solid #e8e8ed;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
    }
  }

  .stat-icon {
    font-size: 32px;
    line-height: 1;
  }

  .stat-content {
    flex: 1;
  }

  .stat-value {
    font-size: 24px;
    font-weight: 800;
    color: #333;
    line-height: 1.2;
  }

  .stat-label {
    font-size: 13px;
    color: #666;
    margin-top: 4px;
  }

  // 特定卡片样式
  .stat-card.total-views {
    background: linear-gradient(135deg, #e3f2fd 0%, #f3e5f5 100%);
  }

  .stat-card.total-clicks {
    background: linear-gradient(135deg, #fff7e6 0%, #fffbeb 100%);
  }

  .stat-card.total-orders {
    background: linear-gradient(135deg, #d4edda 0%, #fff3cd 100%);
  }

  .stat-card.total-rejects {
    background: linear-gradient(135deg, #f8d7da 0%, #fce2f2 100%);
  }

  .metrics-row {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .metric-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 16px;
    background: #f9fafb;
    border-radius: 8px;
    border: 1px solid #e5e7eb;
  }

  .metric-label {
    font-size: 14px;
    color: #666;
    min-width: 60px;
  }

  .metric-value {
    font-size: 16px;
    font-weight: bold;
    color: #333;
  }

  .cache-info {
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #f0f0f0;
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .cache-item {
    display: flex;
    justify-content: space-between;
    font-size: 13px;
    padding: 8px 0;
  }

  .cache-label {
    color: #666;
  }

  .cache-value {
    font-weight: 600;
    color: #333;
  }
}
</style>
