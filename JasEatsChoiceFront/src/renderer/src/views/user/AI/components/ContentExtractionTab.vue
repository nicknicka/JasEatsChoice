<template>
  <div class="content-extraction-tab">
    <div class="tab-header">
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon>
        添加内容源
      </el-button>
      <el-button @click="showHistory">
        <el-icon><List /></el-icon>
        提取历史
      </el-button>
    </div>

    <!-- 内容源列表 -->
    <content-source-list
      :sources="sources"
      :loading="loading"
      @refresh="loadSources"
      @view-detail="handleViewDetail"
      @re-extract="handleReExtract"
      @delete="handleDelete"
    />

    <!-- 添加内容源弹窗 -->
    <content-extraction-dialog
      v-model:visible="addDialogVisible"
      @success="handleAddSuccess"
    />

    <!-- 提取详情弹窗 -->
    <extraction-detail-dialog
      v-model:visible="detailDialogVisible"
      :extraction-id="selectedExtractionId"
      @published="handlePublished"
    />

    <!-- 提取历史弹窗 -->
    <el-dialog
      v-model="historyDialogVisible"
      title="提取历史"
      width="800px"
    >
      <el-table :data="sources" stripe>
        <el-table-column prop="contentUrl" label="内容链接" show-overflow-tooltip />
        <el-table-column prop="platformName" label="平台" width="100" />
        <el-table-column prop="contentTypeName" label="类型" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.extractionStatus)">
              {{ row.extractionStatusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="extractedDishName" label="提取菜品" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, List } from '@element-plus/icons-vue'
import ContentSourceList from '@/components/ContentSourceList.vue'
import ContentExtractionDialog from '@/components/ContentExtractionDialog.vue'
import ExtractionDetailDialog from '@/components/ExtractionDetailDialog.vue'
import contentExtractionApi from '@/api/contentExtraction'

const loading = ref(false)
const sources = ref([])
const addDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const historyDialogVisible = ref(false)
const selectedExtractionId = ref(null)

// 自动刷新定时器
let refreshTimer = null
// 轮询间隔（毫秒）
const POLL_INTERVAL = 3000

// 检查是否有处理中的任务
const hasProcessingTasks = computed(() => {
  return sources.value.some(s =>
    s.extractionStatus === 'PENDING' || s.extractionStatus === 'PROCESSING'
  )
})

// 加载内容源列表
const loadSources = async () => {
  loading.value = true
  try {
    const response = await contentExtractionApi.getSources()
    // 修复：兼容字符串和数字格式的 code
    if (response.code === '200' || response.code === 200) {
      sources.value = response.data || []
    } else {
      ElMessage.error(response.message || '加载失败')
    }
  } catch (error) {
    console.error('加载内容源失败:', error)
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 显示添加弹窗
const showAddDialog = () => {
  addDialogVisible.value = true
}

// 显示历史
const showHistory = () => {
  historyDialogVisible.value = true
}

// 添加成功
const handleAddSuccess = () => {
  ElMessage.success('内容源添加成功，正在提取中...')
  loadSources()
}

// 查看详情
const handleViewDetail = (source) => {
  if (source.extractionId) {
    selectedExtractionId.value = source.extractionId
    detailDialogVisible.value = true
  } else {
    ElMessage.info('内容正在提取中，请稍后查看')
  }
}

// 重新提取
const handleReExtract = async (source) => {
  try {
    await ElMessageBox.confirm(
      '确定要重新提取这个内容吗？',
      '确认操作',
      { type: 'warning' }
    )

    const response = await contentExtractionApi.reExtract(source.id)
    // 修复：兼容字符串和数字格式的 code
    if (response.code === '200' || response.code === 200) {
      ElMessage.success('重新提取任务已创建')
      loadSources()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重新提取失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 删除内容源
const handleDelete = async (source) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个内容源吗？删除后无法恢复。',
      '确认删除',
      { type: 'warning' }
    )

    const response = await contentExtractionApi.deleteSource(source.id)
    // 修复：兼容字符串和数字格式的 code
    if (response.code === '200' || response.code === 200) {
      ElMessage.success('删除成功')
      loadSources()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 发布成功回调
const handlePublished = () => {
  // ExtractionDetailDialog 内部已处理 API 调用和消息提示
  loadSources()
}

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    'PENDING': 'info',
    'PROCESSING': 'warning',
    'SUCCESS': 'success',
    'FAILED': 'danger'
  }
  return statusMap[status] || 'info'
}

// 启动自动轮询
const startPolling = () => {
  // 清除已存在的定时器
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }

  // 每3秒刷新一次列表
  refreshTimer = setInterval(() => {
    if (hasProcessingTasks.value) {
      console.log('自动刷新：有处理中的任务')
      loadSources()
    } else {
      console.log('自动刷新：没有处理中的任务，停止轮询')
      stopPolling()
    }
  }, POLL_INTERVAL)
}

// 停止轮询
const stopPolling = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

onMounted(() => {
  loadSources()

  // 启动自动轮询
  startPolling()
})

// 组件卸载时清除定时器
onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped lang="less">
@import '../../../../assets/css/nordic-theme.less';

.content-extraction-tab {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  padding: 24px;
  box-sizing: border-box;
  animation: tabFadeIn 0.35s ease-out;
}

.tab-header {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-shrink: 0;

  :deep(.el-button--primary) {
    background: @nordic-accent;
    border-color: @nordic-accent;
    border-radius: @nordic-radius-sm;
    font-weight: 600;
    transition: all @nordic-transition-base ease;

    &:hover {
      background: @nordic-accent-dark;
      border-color: @nordic-accent-dark;
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(212, 132, 90, 0.3);
    }
  }

  :deep(.el-button--default) {
    border-radius: @nordic-radius-sm;
    font-weight: 600;
    color: @nordic-text-secondary;
    border-color: @nordic-border;
    transition: all @nordic-transition-base ease;

    &:hover {
      color: @nordic-accent;
      border-color: @nordic-accent;
      background: @nordic-accent-light;
    }
  }
}

// 提取历史弹窗样式
:deep(.el-dialog) {
  border-radius: @nordic-radius-lg;

  .el-dialog__header {
    border-bottom: 1px solid @nordic-border;
    padding-bottom: 16px;
  }

  .el-table {
    --el-table-border-color: @nordic-border;
    --el-table-header-bg-color: @nordic-bg;
    border-radius: @nordic-radius-md;
    overflow: hidden;
  }
}

@keyframes tabFadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
