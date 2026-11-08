<template>
  <div class="system-logs-container">
    <!-- 页头：统一标题容器 + 右侧动作区，刷新为次级、清理日志为危险动作（§4.1、§4.7） -->
    <AppPageHeader title="系统日志" subtitle="查看和分析管理员操作日志" :compact="true">
      <template #actions>
        <el-button type="default" :icon="Refresh" :loading="loading" @click="handleRefresh">
          刷新
        </el-button>
        <el-button
          type="danger"
          plain
          :icon="Delete"
          class="danger-action"
          @click="handleCleanLogs"
        >
          清理日志
        </el-button>
      </template>
    </AppPageHeader>

    <!-- 概览卡：线性图标 + 数值 + 标签，统一同源色（§4.8、§7.1） -->
    <div class="stats-section">
      <AppStatCard compact label="总日志数" :value="statistics.totalLogs || 0" icon="Document" />
      <AppStatCard compact
        label="登录操作"
        :value="statistics.operationStats?.LOGIN || 0"
        icon="User"
      />
      <AppStatCard compact
        label="创建操作"
        :value="statistics.operationStats?.CREATE || 0"
        icon="CirclePlus"
      />
      <AppStatCard compact
        label="更新操作"
        :value="statistics.operationStats?.UPDATE || 0"
        icon="Edit"
      />
      <AppStatCard compact
        label="删除操作"
        :value="statistics.operationStats?.DELETE || 0"
        icon="Delete"
      />
      <AppStatCard compact
        label="查询操作"
        :value="statistics.operationStats?.QUERY || 0"
        icon="Search"
      />
    </div>

    <!-- 筛选条（§4.3） -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.operationType" placeholder="全部" clearable style="width: 130px">
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
          <el-select v-model="searchForm.module" placeholder="全部" clearable style="width: 130px">
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
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 130px">
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
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志列表（§4.3、§5.4） -->
    <el-card class="table-card" shadow="never">
      <!-- 空数据：紧凑中文空状态替换表体，并提供重置筛选的替代动作（§4.3、§5.5） -->
      <AppEmptyState
        v-if="!loading && logList.length === 0"
        compact
        icon="Document"
        description="没有符合条件的操作日志，换个筛选条件或清空后重试"
        action-text="重置筛选"
        @action="handleReset"
      />

      <template v-else>
        <el-table :data="logList" v-loading="loading" stripe>
          <!-- ID：单行省略 + 悬浮完整内容，不折行（§4.3、§5.4） -->
          <el-table-column prop="logId" label="ID" width="100" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="cell-num">{{ row.logId }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="operatorName" label="操作人" width="90" show-overflow-tooltip />
          <!-- 操作类型是分类而非状态，统一走中性标签，不用语义色（§3.1、§7.1） -->
          <el-table-column label="操作类型" width="80" align="center">
            <template #default="{ row }">
              <StatusTag status="neutral" :text="getOperationTypeText(row.operationType)" />
            </template>
          </el-table-column>
          <el-table-column prop="module" label="模块" width="76" align="center" />
          <el-table-column prop="description" label="操作描述" min-width="130" show-overflow-tooltip />
          <el-table-column prop="method" label="方法" width="100" show-overflow-tooltip />
          <el-table-column prop="ip" label="IP地址" width="110" show-overflow-tooltip />
          <el-table-column label="状态" width="82" align="center">
            <template #default="{ row }">
              <StatusTag :status="getStatusMeta(row.status).key" :text="getStatusMeta(row.status).text" />
            </template>
          </el-table-column>
          <!-- 耗时：无衬线等宽数字 + 中性深色，不按阈值染色（§4.3、§5.2） -->
          <el-table-column label="耗时" width="70" align="right">
            <template #default="{ row }">
              <span class="cell-num">{{ row.executeTime }}ms</span>
            </template>
          </el-table-column>
          <!-- 时间统一 YYYY-MM-DD HH:mm，去掉 ISO 的 T，单行不折行（§4.3） -->
          <el-table-column label="操作时间" width="152">
            <template #default="{ row }">
              <span class="cell-num">{{ formatTime(row.createdAt) }}</span>
            </template>
          </el-table-column>
          <!-- 查看详情：先弹对话框再提交，保持可见（#441；每行最多 2 个可见动作） -->
          <el-table-column label="操作" width="76" fixed="right">
            <template #default="{ row }">
              <RowActions :primary="{ text: '详情', onClick: () => handleViewDetail(row) }" :items="[]" />
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页：空数据时不渲染（§4.3、§5.4） -->
        <div v-if="logList.length > 0" class="pagination-container">
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
      </template>
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
        <div class="detail-section">
          <div class="section-title">
            <el-icon><InfoFilled /></el-icon>
            基本信息
          </div>
          <el-descriptions :column="2" border class="detail-descriptions">
            <el-descriptions-item label="日志ID">
              <span class="cell-num">#{{ currentLog.logId }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="操作人">
              <div class="operator-info">
                <el-icon><User /></el-icon>
                {{ currentLog.operatorName }}
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="操作类型">
              <StatusTag status="neutral" :text="getOperationTypeText(currentLog.operationType)" />
            </el-descriptions-item>
            <el-descriptions-item label="模块名称">
              <span>{{ currentLog.module || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="角色">{{ currentLog.operatorRole || '-' }}</el-descriptions-item>
            <el-descriptions-item label="IP地址">
              <span class="cell-num">{{ currentLog.ip }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="操作描述" :span="2">
              {{ currentLog.description }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

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
              <span class="cell-num">{{ currentLog.executeTime }} ms</span>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <StatusTag :status="getStatusMeta(currentLog.status).key" :text="getStatusMeta(currentLog.status).text" />
            </el-descriptions-item>
            <el-descriptions-item label="操作时间" :span="2">
              {{ formatTime(currentLog.createdAt) }}
            </el-descriptions-item>
            <el-descriptions-item label="浏览器">
              <span class="env-info">{{ currentLog.browser || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="操作系统">
              <span class="env-info">{{ currentLog.os || '-' }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div v-if="currentLog.params" class="detail-section">
          <div class="section-title">
            <el-icon><Document /></el-icon>
            请求参数
          </div>
          <div class="code-box">
            <pre class="json-content">{{ formatJson(currentLog.params) }}</pre>
          </div>
        </div>

        <div v-if="currentLog.result" class="detail-section">
          <div class="section-title">
            <el-icon><Check /></el-icon>
            返回结果
          </div>
          <div class="code-box">
            <pre class="json-content">{{ formatJson(currentLog.result) }}</pre>
          </div>
        </div>

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
  Search, Refresh, Delete, Document, User,
  InfoFilled, Operation, Check, Warning
} from '@element-plus/icons-vue'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import AppStatCard from '@/components/common/AppStatCard.vue'
import AppEmptyState from '@/components/common/AppEmptyState.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import RowActions from '@/components/common/RowActions.vue'
import { formatTime } from '@/utils/formatters'
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

// 状态 → StatusTag 词典键（§3.1 状态表，语义色只做状态）
const STATUS_META = Object.freeze({
  SUCCESS: { key: 'success', text: '成功' },
  FAILED: { key: 'failed', text: '失败' },
  PARTIAL: { key: 'warning', text: '部分成功' }
})

const getStatusMeta = (status) => STATUS_META[status] || { key: 'neutral', text: '未知' }

// 操作类型文本（分类，不含语义色）
const getOperationTypeText = (type) => {
  const texts = {
    LOGIN: '登录',
    LOGOUT: '退出',
    CREATE: '创建',
    UPDATE: '更新',
    DELETE: '删除',
    QUERY: '查询',
    EXPORT: '导出'
  }
  return texts[type] || '未知'
}

// 获取日志列表
const fetchLogList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      operationType: searchForm.operationType || undefined,
      module: searchForm.module || undefined,
      operatorName: searchForm.operatorName || undefined,
      status: searchForm.status || undefined
    }

    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startTime = searchForm.dateRange[0]
      params.endTime = searchForm.dateRange[1]
    }

    const response = await getLogList(params)

    if (response && response.success) {
      logList.value = response.records || []
      pagination.total = response.total ?? 0
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
    const response = await getLogStatistics()
    if (response && response.success && response.data) {
      statistics.value = response.data
    }
  } catch (error) {
    console.error('[系统日志] 获取统计数据失败:', error)
  }
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

// 清理日志：危险动作，二次确认由 ElMessageBox 承担（§4.1、§4.6）
const handleCleanLogs = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清理90天前的日志吗？此操作不可恢复！',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )

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
  padding: var(--card-padding-compact) var(--page-gutter);
  background: var(--canvas);
  max-width: 1440px;
  width: 100%;
  margin: 0 auto;
  box-sizing: border-box;

  /* 危险动作与普通动作留出 --space-6 间距（§4.1） */
  .danger-action {
    margin-left: var(--space-6);
  }

  /* 六张概览卡在 1440 下压成一行摘要，窄屏回退为 3/2/1 列（§4.8、§4.10） */
  .stats-section {
    display: grid;
    grid-template-columns: repeat(6, minmax(0, 1fr));
    gap: var(--card-gap-compact);
    margin-bottom: var(--space-8);
  }

  .search-card {
    margin-bottom: var(--space-8);

    .search-form {
      margin-bottom: 0;
    }
  }

  .table-card {
    .pagination-container {
      margin-top: var(--space-5);
      display: flex;
      justify-content: flex-end;
    }

    /* 长表滚动时表头可辨：浅底色 + 中量字重（§4.3） */
    :deep(.el-table__header th) {
      background-color: var(--canvas-sunken);
      color: var(--ink-500);
      font-weight: var(--weight-semibold);
    }

    /* 行 hover 高亮，避免横向读串行（§3.5 高频微交互用 CSS transition） */
    :deep(.el-table__body tr:hover > td) {
      background-color: var(--canvas-sunken);
      transition: background-color var(--duration-fast) var(--ease-out);
    }
  }

  /* ID / 时间 / 数值：单行不折行 + 等宽数字（§4.3、§5.4） */
  .cell-num {
    font-family: var(--font-num);
    font-variant-numeric: tabular-nums;
    white-space: nowrap;
  }

  /* 详情对话框 */
  .detail-dialog {
    .detail-content {
      max-height: 60vh;
      overflow-y: auto;

      .detail-section {
        margin-bottom: var(--space-5);

        &:last-child {
          margin-bottom: 0;
        }

        .section-title {
          display: flex;
          align-items: center;
          gap: var(--space-2);
          font-size: var(--text-body);
          font-weight: var(--weight-medium);
          color: var(--ink-700);
          margin-bottom: var(--space-2);
          padding: var(--space-2) var(--space-3);
          background: var(--canvas-sunken);
          border-radius: var(--radius-sm);

          &.error-title {
            background: var(--danger-bg);
            color: var(--danger);
          }
        }

        .detail-descriptions {
          :deep(.el-descriptions__label) {
            background: var(--canvas-sunken) !important;
            font-weight: var(--weight-medium);
          }
        }

        .operator-info {
          display: flex;
          align-items: center;
          gap: var(--space-2);
        }

        .method-code {
          font-family: var(--font-num);
          font-size: var(--text-caption);
          background: var(--canvas-sunken);
          padding: 4px 8px;
          border-radius: var(--radius-sm);
        }

        .env-info {
          color: var(--ink-500);
          font-size: var(--text-body-sm);
        }

        .code-box {
          border: 1px solid var(--line);
          border-radius: var(--radius-md);
          overflow: hidden;

          &.error-box {
            border-color: var(--danger);
          }

          .json-content,
          .error-content {
            margin: 0;
            padding: var(--space-3);
            background: var(--canvas-sunken);
            font-size: var(--text-caption);
            line-height: 1.6;
            max-height: 250px;
            overflow: auto;
            font-family: var(--font-num);
          }

          .error-content {
            background: var(--danger-bg);
            color: var(--danger);
          }
        }
      }
    }

    .dialog-footer {
      display: flex;
      justify-content: flex-end;
      gap: var(--space-2);
    }
  }
}

/* 窄屏下概览卡换行显示，避免六卡硬挤一行（§4.8） */
@media (max-width: 1200px) {
  .system-logs-container .stats-section {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .system-logs-container .stats-section {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 520px) {
  .system-logs-container .stats-section {
    grid-template-columns: minmax(0, 1fr);
  }
}

/* §4.3：admin 列表只保留单行 40px / 双行 ≤56px 两档，列宽不足由单元格省略而不是折行。 */
.system-logs-container :deep(.el-table__body .el-table__cell) {
  padding-top: 0;
  padding-bottom: 0;
}

.system-logs-container :deep(.el-table__body .el-table__row) {
  height: 40px;
}

/* §4.10：统计卡使用紧凑档后，收口卡片间距，保证 1440×900 下的首屏预算。 */
.system-logs-container .stats-section {
  margin-bottom: var(--space-3);
}

.system-logs-container .search-card {
  margin-bottom: var(--space-3);
}

.system-logs-container :deep(.search-card > .el-card__body) {
  padding: var(--space-2) var(--space-4);
}

.system-logs-container :deep(.table-card > .el-card__body) {
  padding-top: var(--space-2);
}
</style>
