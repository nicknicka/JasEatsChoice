<template>
  <div class="system-logs-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1>
            <el-icon class="header-icon"><Document /></el-icon>
            系统日志
          </h1>
          <p class="subtitle">查看和分析管理员操作日志</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" :icon="Refresh" @click="handleRefresh" :loading="loading">
            刷新
          </el-button>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><Search /></el-icon>
            筛选条件
          </span>
          <el-button text type="primary" @click="toggleExpand">
            {{ isExpanded ? '收起' : '展开' }}
            <el-icon class="ml-1">
              <ArrowUp v-if="isExpanded" />
              <ArrowDown v-else />
            </el-icon>
          </el-button>
        </div>
      </template>
      <el-collapse-transition>
        <div v-show="isExpanded">
          <el-form :model="searchForm" class="search-form" label-width="80px">
            <div class="search-grid">
              <el-form-item label="操作类型">
                <el-select v-model="searchForm.operationType" placeholder="全部" clearable style="width: 100%">
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
                <el-select v-model="searchForm.module" placeholder="全部" clearable style="width: 100%">
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
                  placeholder="输入操作人姓名"
                  clearable
                  :prefix-icon="User"
                />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 100%">
                  <el-option label="成功" value="SUCCESS" />
                  <el-option label="失败" value="FAILED" />
                  <el-option label="部分成功" value="PARTIAL" />
                </el-select>
              </el-form-item>
              <el-form-item label="日期范围" class="date-range-item">
                <el-date-picker
                  v-model="searchForm.dateRange"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  clearable
                  style="width: 100%"
                  value-format="YYYY-MM-DD HH:mm:ss"
                />
              </el-form-item>
              <el-form-item label=" " class="action-col">
                <el-button type="primary" :icon="Search" @click="handleSearch">
                  搜索
                </el-button>
                <el-button :icon="RefreshLeft" @click="handleReset">
                  重置
                </el-button>
                <el-button type="danger" :icon="Delete" @click="handleCleanLogs">
                  清理日志
                </el-button>
              </el-form-item>
            </div>
          </el-form>
        </div>
      </el-collapse-transition>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="8" :md="6" :lg="4">
        <div class="stat-card stat-primary">
          <div class="stat-icon">
            <el-icon :size="24"><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.totalLogs || 0 }}</div>
            <div class="stat-label">总日志数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="4">
        <div class="stat-card stat-success">
          <div class="stat-icon">
            <el-icon :size="24"><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.operationStats?.LOGIN || 0 }}</div>
            <div class="stat-label">登录操作</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="4">
        <div class="stat-card stat-info">
          <div class="stat-icon">
            <el-icon :size="24"><Plus /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.operationStats?.CREATE || 0 }}</div>
            <div class="stat-label">创建操作</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="4">
        <div class="stat-card stat-warning">
          <div class="stat-icon">
            <el-icon :size="24"><Edit /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.operationStats?.UPDATE || 0 }}</div>
            <div class="stat-label">更新操作</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="4">
        <div class="stat-card stat-danger">
          <div class="stat-icon">
            <el-icon :size="24"><Delete /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.operationStats?.DELETE || 0 }}</div>
            <div class="stat-label">删除操作</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="4">
        <div class="stat-card stat-default">
          <div class="stat-icon">
            <el-icon :size="24"><Search /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.operationStats?.QUERY || 0 }}</div>
            <div class="stat-label">查询操作</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 日志列表 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><List /></el-icon>
            日志列表
          </span>
          <div class="card-extra">
            <span class="record-count">共 {{ pagination.total }} 条记录</span>
          </div>
        </div>
      </template>
      <el-table
        :data="logList"
        v-loading="loading"
        stripe
        class="log-table"
        :row-class-name="getRowClassName"
      >
        <el-table-column prop="logId" label="ID" width="100" align="center" />
        <el-table-column prop="operatorName" label="操作人" width="110" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="operator-cell">
              <el-icon class="operator-icon"><User /></el-icon>
              <span>{{ row.operatorName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作类型" width="95" align="center">
          <template #default="{ row }">
            <el-tag :type="getOperationTypeColor(row.operationType)" size="small">
              {{ getOperationTypeText(row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="模块" width="90" align="center" />
        <el-table-column prop="description" label="操作描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="method" label="方法" width="130" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" width="130" show-overflow-tooltip />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="executeTime" label="耗时" width="80" align="center">
          <template #default="{ row }">
            <span :class="getExecuteTimeClass(row.executeTime)">
              {{ row.executeTime }}ms
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="165" />
        <el-table-column label="操作" width="80" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleViewDetail(row)">
              详情
            </el-button>
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
          background
        />
      </div>
    </el-card>

    <!-- 日志详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="日志详情"
      width="850px"
      :close-on-click-modal="false"
      class="detail-dialog"
    >
      <div v-if="currentLog" class="detail-content">
        <!-- 基本信息卡片 -->
        <div class="detail-section">
          <div class="section-title">
            <el-icon><InfoFilled /></el-icon>
            基本信息
          </div>
          <el-descriptions :column="2" border class="detail-descriptions">
            <el-descriptions-item label="日志ID">
              <el-tag type="info" size="small">#{{ currentLog.logId }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="操作人">
              <div class="operator-info">
                <el-icon><User /></el-icon>
                {{ currentLog.operatorName }}
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="操作类型">
              <el-tag :type="getOperationTypeColor(currentLog.operationType)">
                {{ getOperationTypeText(currentLog.operationType) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="模块名称">
              <el-tag type="info">{{ currentLog.module }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="角色">{{ currentLog.operatorRole || '-' }}</el-descriptions-item>
            <el-descriptions-item label="IP地址">
              <span class="ip-address">{{ currentLog.ip }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="操作描述" :span="2">
              {{ currentLog.description }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 执行信息卡片 -->
        <div class="detail-section">
          <div class="section-title">
            <el-icon><Operation /></el-icon>
            执行信息
          </div>
          <el-descriptions :column="2" border class="detail-descriptions">
            <el-descriptions-item label="执行方法" :span="2">
              <code class="method-code">{{ currentLog.method }}</code>
            </el-descriptions-item>
            <el-descriptions-item label="执行时长">
              <el-tag :type="getExecuteTimeTagType(currentLog.executeTime)">
                {{ currentLog.executeTime }} ms
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusColor(currentLog.status)">
                {{ getStatusText(currentLog.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="操作时间" :span="2">
              {{ currentLog.createTime }}
            </el-descriptions-item>
            <el-descriptions-item label="浏览器">
              <span class="env-info">{{ currentLog.browser || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="操作系统">
              <span class="env-info">{{ currentLog.os || '-' }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 请求参数 -->
        <div v-if="currentLog.params" class="detail-section">
          <div class="section-title">
            <el-icon><Document /></el-icon>
            请求参数
          </div>
          <div class="code-box">
            <pre class="json-content">{{ formatJson(currentLog.params) }}</pre>
          </div>
        </div>

        <!-- 返回结果 -->
        <div v-if="currentLog.result" class="detail-section">
          <div class="section-title">
            <el-icon><Check /></el-icon>
            返回结果
          </div>
          <div class="code-box">
            <pre class="json-content">{{ formatJson(currentLog.result) }}</pre>
          </div>
        </div>

        <!-- 错误信息 -->
        <div v-if="currentLog.errorMessage" class="detail-section">
          <div class="section-title error-title">
            <el-icon><Warning /></el-icon>
            错误信息
          </div>
          <div class="code-box error-box">
            <pre class="error-content">{{ currentLog.errorMessage }}</pre>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Refresh, Delete, Document, CircleCheck, Plus, Edit,
  ArrowUp, ArrowDown, RefreshLeft, User, List, InfoFilled,
  Operation, Check, Warning
} from '@element-plus/icons-vue'
import { getLogList, getLogStatistics, cleanExpiredLogs } from '@/api/admin'

const loading = ref(false)
const logList = ref([])
const currentLog = ref(null)
const detailDialogVisible = ref(false)
const isExpanded = ref(true)
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

// 获取执行时间样式类
const getExecuteTimeClass = (time) => {
  if (time >= 1000) return 'execute-time-slow'
  if (time >= 500) return 'execute-time-medium'
  return 'execute-time-fast'
}

// 获取执行时间标签类型
const getExecuteTimeTagType = (time) => {
  if (time >= 1000) return 'danger'
  if (time >= 500) return 'warning'
  return 'success'
}

// 获取表格行类名
const getRowClassName = ({ row }) => {
  if (row.status === 'FAILED') return 'error-row'
  if (row.executeTime >= 1000) return 'slow-row'
  return ''
}

// 切换展开/收起
const toggleExpand = () => {
  isExpanded.value = !isExpanded.value
}

// 刷新
const handleRefresh = () => {
  fetchLogList()
  fetchStatistics()
  ElMessage.success('已刷新')
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
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 40px);

  // 页面标题
  .page-header {
    margin-bottom: 16px;

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .header-left {
        h1 {
          font-size: 22px;
          font-weight: 600;
          color: #1f2937;
          margin: 0 0 6px 0;
          display: flex;
          align-items: center;
          gap: 10px;

          .header-icon {
            color: #409eff;
          }
        }

        .subtitle {
          color: #6b7280;
          margin: 0;
          font-size: 0.929rem /* 原值: 13px */;
        }
      }
    }
  }

  // 搜索卡片
  .search-card {
    margin-bottom: 16px;
    border-radius: 8px;
    border: 1px solid #e5e7eb;

    :deep(.el-card__header) {
      padding: 12px 16px;
      border-bottom: 1px solid #e5e7eb;
      background: #fafafa;
    }

    :deep(.el-card__body) {
      padding: 16px;
      overflow: visible;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .card-title {
        font-weight: 500;
        color: #374151;
        display: flex;
        align-items: center;
        gap: 6px;
      }

      .ml-1 {
        margin-left: 4px;
      }
    }

    .search-form {
      .search-grid {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 12px;
        align-items: start;
        width: 100%;
      }

      .date-range-item {
        grid-column: span 2;
      }

      .action-col {
        grid-column: span 2;
        display: flex;
        justify-content: flex-end;
        align-items: flex-end;

        .el-button {
          margin-left: 8px;

          &:first-child {
            margin-left: 0;
          }
        }
      }

      .el-form-item {
        margin-bottom: 0;

        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }

  // 统计卡片行
  .stats-row {
    margin-top: 20px;
    margin-bottom: 16px;

    .stat-card {
      background: white;
      border-radius: 8px;
      padding: 16px;
      display: flex;
      align-items: center;
      gap: 12px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
      transition: all 0.3s ease;
      border: 1px solid #e5e7eb;
      cursor: pointer;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }

      .stat-icon {
        width: 48px;
        height: 48px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
      }

      .stat-content {
        flex: 1;
        min-width: 0;

        .stat-value {
          font-size: 22px;
          font-weight: 600;
          line-height: 1.2;
          margin-bottom: 4px;
        }

        .stat-label {
          font-size: 0.857rem /* 原值: 12px */;
          color: #6b7280;
        }
      }

      &.stat-primary {
        .stat-icon {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
        }
        .stat-value {
          color: #667eea;
        }
      }

      &.stat-success {
        .stat-icon {
          background: linear-gradient(135deg, #10b981 0%, #059669 100%);
          color: white;
        }
        .stat-value {
          color: #10b981;
        }
      }

      &.stat-info {
        .stat-icon {
          background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
          color: white;
        }
        .stat-value {
          color: #3b82f6;
        }
      }

      &.stat-warning {
        .stat-icon {
          background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
          color: white;
        }
        .stat-value {
          color: #f59e0b;
        }
      }

      &.stat-danger {
        .stat-icon {
          background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
          color: white;
        }
        .stat-value {
          color: #ef4444;
        }
      }

      &.stat-default {
        .stat-icon {
          background: linear-gradient(135deg, #6b7280 0%, #4b5563 100%);
          color: white;
        }
        .stat-value {
          color: #6b7280;
        }
      }
    }
  }

  // 表格卡片
  .table-card {
    border-radius: 8px;
    border: 1px solid #e5e7eb;

    :deep(.el-card__header) {
      padding: 12px 16px;
      border-bottom: 1px solid #e5e7eb;
      background: #fafafa;
    }

    :deep(.el-card__body) {
      padding: 0;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .card-title {
        font-weight: 500;
        color: #374151;
        display: flex;
        align-items: center;
        gap: 6px;
      }

      .card-extra {
        .record-count {
          font-size: 0.929rem /* 原值: 13px */;
          color: #6b7280;
        }
      }
    }

    .log-table {
      :deep(.el-table__header) {
        th {
          background: #f9fafb;
          color: #374151;
          font-weight: 500;
        }
      }

      :deep(.error-row) {
        background: #fef2f2 !important;
      }

      :deep(.slow-row) {
        background: #fffbeb !important;
      }

      .operator-cell {
        display: flex;
        align-items: center;
        gap: 6px;

        .operator-icon {
          color: #9ca3af;
        }
      }

      .execute-time-fast {
        color: #10b981;
        font-weight: 500;
      }

      .execute-time-medium {
        color: #f59e0b;
        font-weight: 500;
      }

      .execute-time-slow {
        color: #ef4444;
        font-weight: 500;
      }
    }

    .pagination-container {
      padding: 16px;
      display: flex;
      justify-content: flex-end;
      border-top: 1px solid #e5e7eb;
      background: #fafafa;
    }
  }

  // 详情对话框
  .detail-dialog {
    .detail-content {
      max-height: 60vh;
      overflow-y: auto;

      .detail-section {
        margin-bottom: 20px;

        &:last-child {
          margin-bottom: 0;
        }

        .section-title {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 1rem /* 原值: 14px */;
          font-weight: 500;
          color: #374151;
          margin-bottom: 10px;
          padding: 8px 12px;
          background: #f3f4f6;
          border-radius: 6px;

          &.error-title {
            background: #fef2f2;
            color: #dc2626;
          }
        }

        .detail-descriptions {
          :deep(.el-descriptions__label) {
            background: #f9fafb !important;
            font-weight: 500;
          }
        }

        .operator-info {
          display: flex;
          align-items: center;
          gap: 6px;
        }

        .ip-address {
          font-family: 'Courier New', monospace;
          font-size: 0.929rem /* 原值: 13px */;
        }

        .method-code {
          font-family: 'Courier New', monospace;
          font-size: 0.857rem /* 原值: 12px */;
          background: #f3f4f6;
          padding: 4px 8px;
          border-radius: 4px;
        }

        .env-info {
          color: #6b7280;
          font-size: 0.929rem /* 原值: 13px */;
        }

        .code-box {
          border: 1px solid #e5e7eb;
          border-radius: 6px;
          overflow: hidden;

          &.error-box {
            border-color: #fecaca;
          }

          .json-content,
          .error-content {
            margin: 0;
            padding: 12px;
            background: #f9fafb;
            font-size: 0.857rem /* 原值: 12px */;
            line-height: 1.6;
            max-height: 250px;
            overflow: auto;
            font-family: 'Courier New', monospace;
          }

          .error-content {
            background: #fef2f2;
            color: #dc2626;
          }
        }
      }
    }

    .dialog-footer {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
    }
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .system-logs-container {
    .search-form {
      .search-grid {
        grid-template-columns: repeat(3, 1fr);
      }

      .date-range-item,
      .action-col {
        grid-column: span 3;
      }
    }
  }
}

@media (max-width: 768px) {
  .system-logs-container {
    padding: 12px;

    .page-header {
      .header-content {
        flex-direction: column;
        align-items: flex-start;
        gap: 12px;
      }
    }

    .search-form {
      .search-grid {
        grid-template-columns: repeat(2, 1fr);
      }

      .date-range-item,
      .action-col {
        grid-column: span 2;
      }
    }

    .stats-row {
      :deep(.el-col) {
        margin-bottom: 12px;
      }
    }
  }
}

@media (max-width: 480px) {
  .system-logs-container {
    .search-form {
      .search-grid {
        grid-template-columns: 1fr;
      }

      .date-range-item,
      .action-col {
        grid-column: span 1;
      }

      .action-col {
        justify-content: flex-start;
      }
    }
  }
}
</style>
