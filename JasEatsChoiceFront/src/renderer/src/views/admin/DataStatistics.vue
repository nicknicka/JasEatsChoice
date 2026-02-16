<template>
  <div class="data-statistics-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>数据统计</h1>
      <p class="subtitle">系统运营数据分析和可视化</p>
    </div>

    <!-- 日期选择器 -->
    <el-card class="date-selector-card" shadow="never">
      <el-form :inline="true">
        <el-form-item label="统计周期">
          <el-radio-group v-model="dateRange" @change="fetchStatistics">
            <el-radio-button label="7">最近7天</el-radio-button>
            <el-radio-button label="30">最近30天</el-radio-button>
            <el-radio-button label="90">最近90天</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="自定义">
          <el-date-picker
            v-model="customDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="fetchStatistics"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Refresh" @click="fetchStatistics">刷新</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 核心指标卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card">
        <el-statistic title="总用户数" :value="stats.totalUsers">
          <template #suffix>
            <span class="stat-change positive">+{{ stats.newUsers }}</span>
          </template>
        </el-statistic>
      </el-card>
      <el-card class="stat-card">
        <el-statistic title="总商家数" :value="stats.totalMerchants">
          <template #suffix>
            <span class="stat-change positive">+{{ stats.newMerchants }}</span>
          </template>
        </el-statistic>
      </el-card>
      <el-card class="stat-card">
        <el-statistic title="总订单数" :value="stats.totalOrders">
          <template #suffix>
            <span class="stat-change positive">+{{ stats.newOrders }}</span>
          </template>
        </el-statistic>
      </el-card>
      <el-card class="stat-card">
        <el-statistic title="总收入" :value="stats.totalRevenue" :precision="2" prefix="¥">
          <template #suffix>
            <span class="stat-change positive">+{{ stats.newRevenue }}</span>
          </template>
        </el-statistic>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <div class="charts-container">
      <!-- 用户增长趋势 -->
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>用户增长趋势</span>
          </div>
        </template>
        <div ref="userTrendChartRef" class="chart"></div>
      </el-card>

      <!-- 订单趋势 -->
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>订单趋势</span>
          </div>
        </template>
        <div ref="orderTrendChartRef" class="chart"></div>
      </el-card>

      <!-- 收入趋势 -->
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>收入趋势</span>
          </div>
        </template>
        <div ref="revenueTrendChartRef" class="chart"></div>
      </el-card>

      <!-- 用户分布 -->
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>用户类型分布</span>
          </div>
        </template>
        <div ref="userDistributionChartRef" class="chart"></div>
      </el-card>
    </div>

    <!-- 详细数据表格 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>每日详细数据</span>
          <el-button type="primary" size="small" :icon="Download" @click="handleExport">导出数据</el-button>
        </div>
      </template>
      <el-table :data="dailyData" stripe>
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="newUsers" label="新增用户" width="100" />
        <el-table-column prop="newMerchants" label="新增商家" width="100" />
        <el-table-column prop="totalOrders" label="订单数" width="100" />
        <el-table-column prop="completedOrders" label="完成订单" width="100" />
        <el-table-column prop="revenue" label="收入" width="120">
          <template #default="{ row }">¥{{ row.revenue.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="activeUsers" label="活跃用户" width="100" />
        <el-table-column prop="averageOrderAmount" label="客单价" width="120">
          <template #default="{ row }">¥{{ row.averageOrderAmount.toFixed(2) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Download } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import statisticsApi from '../../api/statistics'

const dateRange = ref('7')
const customDateRange = ref(null)
const loading = ref(false)

const stats = reactive({
  totalUsers: 0,
  newUsers: 0,
  totalMerchants: 0,
  newMerchants: 0,
  totalOrders: 0,
  newOrders: 0,
  totalRevenue: 0,
  newRevenue: 0
})

const dailyData = ref([])

// 图表引用
const userTrendChartRef = ref(null)
const orderTrendChartRef = ref(null)
const revenueTrendChartRef = ref(null)
const userDistributionChartRef = ref(null)

// 图表实例
let userTrendChart = null
let orderTrendChart = null
let revenueTrendChart = null
let userDistributionChart = null

// 获取统计数据
const fetchStatistics = async () => {
  loading.value = true
  try {
    // 调用统计API获取数据
    const response = await statisticsApi.getDashboardStatistics(dateRange.value)

    if (response.code === '200' && response.data) {
      const data = response.data

      // 更新核心指标
      stats.totalUsers = data.summary.totalUsers || 0
      stats.newUsers = data.summary.newUsers || 0
      stats.totalMerchants = data.summary.totalMerchants || 0
      stats.newMerchants = data.summary.newMerchants || 0
      stats.totalOrders = data.summary.totalOrders || 0
      stats.newOrders = data.summary.newOrders || 0
      stats.totalRevenue = data.summary.totalRevenue || 0
      stats.newRevenue = data.summary.newRevenue || 0

      // 更新每日数据
      dailyData.value = data.daily || []

      // 渲染图表
      nextTick(() => {
        const trends = data.trends || {}
        renderUserTrendChart(trends.dates || [], trends.newUsers || [])
        renderOrderTrendChart(trends.dates || [], trends.orders || [])
        renderRevenueTrendChart(trends.dates || [], trends.revenue || [])
        renderUserDistributionChart()
      })

      loading.value = false
    } else {
      ElMessage.error(response.message || '获取统计数据失败')
      loading.value = false
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
    loading.value = false
  }
}

// 渲染用户增长趋势图
const renderUserTrendChart = (dates, data) => {
  if (!userTrendChartRef.value) return

  if (!userTrendChart) {
    userTrendChart = echarts.init(userTrendChartRef.value)
  }

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      name: '新增用户',
      type: 'line',
      smooth: true,
      data: data,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
        ])
      },
      lineStyle: {
        color: '#409eff',
        width: 2
      },
      itemStyle: {
        color: '#409eff'
      }
    }]
  }

  userTrendChart.setOption(option)
}

// 渲染订单趋势图
const renderOrderTrendChart = (dates, data) => {
  if (!orderTrendChartRef.value) return

  if (!orderTrendChart) {
    orderTrendChart = echarts.init(orderTrendChartRef.value)
  }

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      name: '订单数',
      type: 'line',
      smooth: true,
      data: data,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
          { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
        ])
      },
      lineStyle: {
        color: '#67c23a',
        width: 2
      },
      itemStyle: {
        color: '#67c23a'
      }
    }]
  }

  orderTrendChart.setOption(option)
}

// 渲染收入趋势图
const renderRevenueTrendChart = (dates, data) => {
  if (!revenueTrendChartRef.value) return

  if (!revenueTrendChart) {
    revenueTrendChart = echarts.init(revenueTrendChartRef.value)
  }

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>收入: ¥{c}'
    },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      name: '收入',
      type: 'line',
      smooth: true,
      data: data,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(255, 107, 107, 0.3)' },
          { offset: 1, color: 'rgba(255, 107, 107, 0.05)' }
        ])
      },
      lineStyle: {
        color: '#ff6b6b',
        width: 2
      },
      itemStyle: {
        color: '#ff6b6b'
      }
    }]
  }

  revenueTrendChart.setOption(option)
}

// 渲染用户分布饼图
const renderUserDistributionChart = () => {
  if (!userDistributionChartRef.value) return

  if (!userDistributionChart) {
    userDistributionChart = echarts.init(userDistributionChartRef.value)
  }

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [{
      name: '用户类型',
      type: 'pie',
      radius: '60%',
      data: [
        { value: 12350, name: '普通用户' },
        { value: 2330, name: '商家用户' },
        { value: 1000, name: '管理员' }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }

  userDistributionChart.setOption(option)
}

// 导出数据
const handleExport = async () => {
  try {
    const response = await statisticsApi.exportStatistics({
      days: dateRange.value,
      format: 'excel'
    })

    if (response.code === '200') {
      if (response.data.downloadUrl) {
        // 下载文件
        window.open(response.data.downloadUrl, '_blank')
        ElMessage.success('数据导出成功')
      } else {
        ElMessage.info('数据导出功能开发中，敬请期待')
      }
    } else {
      ElMessage.error(response.message || '导出失败')
    }
  } catch (error) {
    console.error('导出数据失败:', error)
    ElMessage.error('导出失败，请稍后重试')
  }
}

// 窗口大小改变时重新渲染图表
const handleResize = () => {
  userTrendChart?.resize()
  orderTrendChart?.resize()
  revenueTrendChart?.resize()
  userDistributionChart?.resize()
}

onMounted(() => {
  fetchStatistics()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  userTrendChart?.dispose()
  orderTrendChart?.dispose()
  revenueTrendChart?.dispose()
  userDistributionChart?.dispose()
})
</script>

<style scoped lang="less">
.data-statistics-container {
  .page-header {
    margin-bottom: 20px;

    h1 {
      font-size: 1.714rem /* 原值: 24px */;
      color: #303133;
      margin: 0 0 8px 0;
    }

    .subtitle {
      color: #909399;
      margin: 0;
      font-size: 1rem /* 原值: 14px */;
    }
  }

  .date-selector-card {
    margin-bottom: 20px;
  }

  .stats-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 20px;
    margin-bottom: 20px;

    .stat-card {
      text-align: center;

      .stat-change {
        font-size: 1rem /* 原值: 14px */;
        margin-left: 8px;

        &.positive {
          color: #67c23a;
        }

        &.negative {
          color: #f56c6c;
        }
      }
    }
  }

  .charts-container {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
    margin-bottom: 20px;

    .chart-card {
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-weight: bold;
      }

      .chart {
        height: 300px;
      }
    }
  }

  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-weight: bold;
    }
  }
}

@media (max-width: 1200px) {
  .charts-container {
    grid-template-columns: 1fr;
  }
}
</style>
