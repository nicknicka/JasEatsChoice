<template>
  <div class="system-logs-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>系统日志</h1>
      <p class="subtitle">查看管理员操作日志</p>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.operationType" placeholder="全部" clearable style="width: 150px">
            <el-option label="登录" value="LOGIN" />
            <el-option label="退出" value="LOGOUT" />
            <el-option label="创建" value="CREATE" />
            <el-option label="更新" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
            <el-option label="查询" value="QUERY" />
            <el-option label="导出" value="EXPORT" />
          </el-select>
        </el-form-item>
        <el-form-item label="模块名称">
          <el-select v-model="searchForm.module" placeholder="全部" clearable style="width: 150px">
            <el-option label="用户管理" value="USER" />
            <el-option label="商家管理" value="MERCHANT" />
            <el-option label="订单管理" value="ORDER" />
            <el-option label="菜品管理" value="DISH" />
            <el-option label="财务管理" value="FINANCE" />
            <el-option label="系统管理" value="SYSTEM" />
            <el-option label="角色管理" value="ROLE" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人">
          <el-input
            v-model="searchForm.operatorName"
            placeholder="操作人姓名"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAILED" />
            <el-option label="部分成功" value="PARTIAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            clearable
            style="width: 360px"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button type="danger" :icon="Delete" @click="handleCleanLogs">清理日志</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">总日志数</div>
            <div class="stat-value">{{ statistics.totalLogs || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">登录</div>
            <div class="stat-value">{{ statistics.operationStats?.LOGIN || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">创建</div>
            <div class="stat-value">{{ statistics.operationStats?.CREATE || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">更新</div>
            <div class="stat-value">{{ statistics.operationStats?.UPDATE || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">删除</div>
            <div class="stat-value">{{ statistics.operationStats?.DELETE || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">查询</div>
            <div class="stat-value">{{ statistics.operationStats?.QUERY || 0 }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 日志列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="logList" v-loading="loading" stripe>
        <el-table-column prop="logId" label="日志ID" width="100" />
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getOperationTypeColor(row.operationType)">
              {{ getOperationTypeText(row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="模块名称" width="100" />
        <el-table-column prop="description" label="操作描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="method" label="执行方法" width="150" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="executeTime" label="耗时(ms)" width="100" />
        <el-table-column prop="createTime" label="操作时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleViewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchLogList"
          @current-change="fetchLogList"
        />
      </div>
    </el-card>

    <!-- 日志详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="日志详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-descriptions v-if="currentLog" :column="2" border>
        <el-descriptions-item label="日志ID">{{ currentLog.logId }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentLog.operatorName }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <el-tag :type="getOperationTypeColor(currentLog.operationType)">
            {{ getOperationTypeText(currentLog.operationType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="模块名称">{{ currentLog.module }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ currentLog.operatorRole }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.ip }}</el-descriptions-item>
        <el-descriptions-item label="操作描述" :span="2">{{ currentLog.description }}</el-descriptions-item>
        <el-descriptions-item label="执行方法" :span="2">{{ currentLog.method }}</el-descriptions-item>
        <el-descriptions-item label="执行时长">{{ currentLog.executeTime }} ms</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusColor(currentLog.status)">
            {{ getStatusText(currentLog.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ currentLog.createTime }}</el-descriptions-item>
        <el-descriptions-item label="浏览器">{{ currentLog.browser || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作系统">{{ currentLog.os || '-' }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2" v-if="currentLog.params">
          <pre class="json-content">{{ formatJson(currentLog.params) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="返回结果" :span="2" v-if="currentLog.result">
          <pre class="json-content">{{ formatJson(currentLog.result) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="currentLog.errorMessage">
          <pre class="error-content">{{ currentLog.errorMessage }}</pre>
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Delete } from '@element-plus/icons-vue'
import { getLogList, getLogStatistics, cleanExpiredLogs } from '@/api/admin'

const loading = ref(false)
const logList = ref([])
const currentLog = ref(null)
const detailDialogVisible = ref(false)
const statistics = ref({
  totalLogs: 0,
  operationStats: {}
})

const searchForm = reactive({
  operationType: '',
  module: '',
  operatorName: '',
  status: '',
  dateRange: null
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 获取日志列表
const fetchLogList = async () => {
  loading.value = true
  try {
    console.log('[系统日志] 获取日志列表')

    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      operationType: searchForm.operationType || undefined,
      module: searchForm.module || undefined,
      operatorName: searchForm.operatorName || undefined,
      status: searchForm.status || undefined
    }

    // 处理日期范围
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startTime = searchForm.dateRange[0]
      params.endTime = searchForm.dateRange[1]
    }

    const response = await getLogList(params)
    console.log('[系统日志] API响应:', response)

    if (response && response.success) {
      logList.value = response.records || []
      pagination.total = response.total || 0
      console.log('[系统日志] 获取日志列表成功, 总数:', pagination.total)
    } else {
      logList.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('[系统日志] 获取日志列表失败:', error)
    ElMessage.error('获取日志列表失败: ' + (error.message || '网络错误'))
    logList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 获取统计数据
const fetchStatistics = async () => {
  try {
    console.log('[系统日志] 获取统计数据')
    const response = await getLogStatistics()
    console.log('[系统日志] 统计数据响应:', response)

    if (response && response.success && response.data) {
      statistics.value = response.data
    }
  } catch (error) {
    console.error('[系统日志] 获取统计数据失败:', error)
  }
}

// 获取操作类型颜色
const getOperationTypeColor = (type) => {
  const colors = {
    'LOGIN': 'success',
    'LOGOUT': 'info',
    'CREATE': 'primary',
    'UPDATE': 'warning',
    'DELETE': 'danger',
    'QUERY': 'info',
    'EXPORT': 'primary'
  }
  return colors[type] || 'info'
}

// 获取操作类型文本
const getOperationTypeText = (type) => {
  const texts = {
    'LOGIN': '登录',
    'LOGOUT': '退出',
    'CREATE': '创建',
    'UPDATE': '更新',
    'DELETE': '删除',
    'QUERY': '查询',
    'EXPORT': '导出'
  }
  return texts[type] || '未知'
}

// 获取状态颜色
const getStatusColor = (status) => {
  const colors = {
    'SUCCESS': 'success',
    'FAILED': 'danger',
    'PARTIAL': 'warning'
  }
  return colors[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    'SUCCESS': '成功',
    'FAILED': '失败',
    'PARTIAL': '部分成功'
  }
  return texts[status] || '未知'
}

// 格式化JSON
const formatJson = (jsonStr) => {
  try {
    const obj = typeof jsonStr === 'string' ? JSON.parse(jsonStr) : jsonStr
    return JSON.stringify(obj, null, 2)
  } catch {
    return jsonStr
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchLogList()
}

// 重置
const handleReset = () => {
  searchForm.operationType = ''
  searchForm.module = ''
  searchForm.operatorName = ''
  searchForm.status = ''
  searchForm.dateRange = null
  pagination.page = 1
  fetchLogList()
}

// 清理日志
const handleCleanLogs = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清理90天前的日志吗？此操作不可恢复！',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    console.log('[系统日志] 清理过期日志')
    const response = await cleanExpiredLogs(90)

    if (response && response.success) {
      ElMessage.success(response.message || '清理成功')
      fetchLogList()
      fetchStatistics()
    } else {
      ElMessage.error(response.message || '清理失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('[系统日志] 清理日志失败:', error)
      ElMessage.error('清理日志失败: ' + (error.message || '网络错误'))
    }
  }
}

// 查看详情
const handleViewDetail = (row) => {
  currentLog.value = row
  detailDialogVisible.value = true
}

onMounted(() => {
  fetchLogList()
  fetchStatistics()
})
</script>

<style scoped lang="less">
.system-logs-container {
  .page-header {
    margin-bottom: 20px;

    h1 {
      font-size: 24px;
      color: #303133;
      margin: 0 0 8px 0;
    }

    .subtitle {
      color: #909399;
      margin: 0;
      font-size: 14px;
    }
  }

  .search-card {
    margin-bottom: 20px;

    .search-form {
      margin-bottom: 0;
    }
  }

  .stats-row {
    margin-bottom: 20px;

    .stat-item {
      text-align: center;

      .stat-label {
        font-size: 14px;
        color: #909399;
        margin-bottom: 8px;
      }

      .stat-value {
        font-size: 24px;
        font-weight: bold;
        color: #409eff;
      }
    }
  }

  .table-card {
    .pagination-container {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }

    .json-content {
      background: #f5f7fa;
      padding: 10px;
      border-radius: 4px;
      max-height: 200px;
      overflow: auto;
      font-size: 12px;
      line-height: 1.5;
    }

    .error-content {
      background: #fef0f0;
      color: #f56c6c;
      padding: 10px;
      border-radius: 4px;
      max-height: 200px;
      overflow: auto;
      font-size: 12px;
      line-height: 1.5;
    }
  }
}
</style>
