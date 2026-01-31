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
            <el-option label="审核" value="AUDIT" />
          </el-select>
        </el-form-item>
        <el-form-item label="模块名称">
          <el-input
            v-model="searchForm.moduleName"
            placeholder="模块名称"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input
            v-model="searchForm.username"
            placeholder="用户名"
            clearable
            style="width="150px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width="120px">
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAIL" />
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
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="logList" v-loading="loading" stripe>
        <el-table-column prop="logId" label="日志ID" width="100" />
        <el-table-column prop="username" label="操作人" width="120" />
        <el-table-column label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getOperationTypeColor(row.operationType)">
              {{ getOperationTypeText(row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="moduleName" label="模块名称" width="120" />
        <el-table-column prop="operationDesc" label="操作描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="requestMethod" label="请求方法" width="90" />
        <el-table-column prop="requestUrl" label="请求URL" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP地址" width="140" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
              {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
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
        <el-descriptions-item label="操作人">{{ currentLog.username }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <el-tag :type="getOperationTypeColor(currentLog.operationType)">
            {{ getOperationTypeText(currentLog.operationType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="模块名称">{{ currentLog.moduleName }}</el-descriptions-item>
        <el-descriptions-item label="操作描述" :span="2">{{ currentLog.operationDesc }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ currentLog.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="请求URL" :span="2">{{ currentLog.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="执行时长">{{ currentLog.executeTime }} ms</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentLog.status === 'SUCCESS' ? 'success' : 'danger'">
            {{ currentLog.status === 'SUCCESS' ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ currentLog.createTime }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2" v-if="currentLog.requestParams">
          <pre class="json-content">{{ formatJson(currentLog.requestParams) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="响应结果" :span="2" v-if="currentLog.responseResult">
          <pre class="json-content">{{ formatJson(currentLog.responseResult) }}</pre>
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
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import axios from 'axios'

const loading = ref(false)
const logList = ref([])
const currentLog = ref(null)
const detailDialogVisible = ref(false)

const searchForm = reactive({
  operationType: '',
  moduleName: '',
  username: '',
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
    // TODO: 调用实际的日志API
    // const response = await axios.get('http://localhost:8080/api/admin/settings/logs', {
    //   params: {
    //     page: pagination.page,
    //     pageSize: pagination.pageSize,
    //     ...searchForm
    //   }
    // })

    // 临时使用模拟数据
    setTimeout(() => {
      logList.value = [
        {
          logId: 1,
          adminId: 1,
          username: 'admin',
          operationType: 'LOGIN',
          moduleName: '系统登录',
          operationDesc: '管理员登录',
          requestMethod: 'POST',
          requestUrl: '/api/admin/login',
          requestParams: '{"username":"admin"}',
          responseResult: '{"success":true}',
          ipAddress: '192.168.1.100',
          executeTime: 125,
          status: 'SUCCESS',
          errorMessage: null,
          createTime: '2025-01-31 10:30:00'
        },
        {
          logId: 2,
          adminId: 1,
          username: 'admin',
          operationType: 'CREATE',
          moduleName: '用户管理',
          operationDesc: '创建管理员',
          requestMethod: 'POST',
          requestUrl: '/api/admin/create',
          requestParams: '{"username":"test","roleCode":"ADMIN"}',
          responseResult: '{"success":true}',
          ipAddress: '192.168.1.100',
          executeTime: 85,
          status: 'SUCCESS',
          errorMessage: null,
          createTime: '2025-01-31 10:35:00'
        },
        {
          logId: 3,
          adminId: 1,
          username: 'admin',
          operationType: 'UPDATE',
          moduleName: '商家管理',
          operationDesc: '审核商家：通过',
          requestMethod: 'PUT',
          requestUrl: '/api/admin/merchants/1/audit',
          requestParams: '{"status":"APPROVED"}',
          responseResult: '{"success":true}',
          ipAddress: '192.168.1.100',
          executeTime: 156,
          status: 'SUCCESS',
          errorMessage: null,
          createTime: '2025-01-31 10:40:00'
        },
        {
          logId: 4,
          adminId: 1,
          username: 'admin',
          operationType: 'DELETE',
          moduleName: '用户管理',
          operationDesc: '删除用户',
          requestMethod: 'DELETE',
          requestUrl: '/api/admin/users/U1234567890123456',
          requestParams: '{}',
          responseResult: '{"success":true}',
          ipAddress: '192.168.1.100',
          executeTime: 95,
          status: 'SUCCESS',
          errorMessage: null,
          createTime: '2025-01-31 10:45:00'
        }
      ]
      pagination.total = 100
      loading.value = false
    }, 500)
  } catch (error) {
    console.error('获取日志列表失败:', error)
    ElMessage.error('获取日志列表失败')
    loading.value = false
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
    'AUDIT': 'primary'
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
    'AUDIT': '审核'
  }
  return texts[type] || '未知'
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
  searchForm.moduleName = ''
  searchForm.username = ''
  searchForm.status = ''
  searchForm.dateRange = null
  pagination.page = 1
  fetchLogList()
}

// 查看详情
const handleViewDetail = (row) => {
  currentLog.value = row
  detailDialogVisible.value = true
}

onMounted(() => {
  fetchLogList()
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
