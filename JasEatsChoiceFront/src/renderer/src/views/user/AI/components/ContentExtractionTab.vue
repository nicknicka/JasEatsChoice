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
      @publish="handlePublish"
      @verify="handleVerify"
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
import { ref, onMounted } from 'vue'
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

// 加载内容源列表
const loadSources = async () => {
  loading.value = true
  try {
    const response = await contentExtractionApi.getSources()
    if (response.code === 200) {
      sources.value = response.data || []
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
    if (response.code === 200) {
      ElMessage.success('重新提取任务已创建')
      loadSources()
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
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadSources()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 发布为食谱
const handlePublish = () => {
  ElMessage.success('发布成功！')
  loadSources()
}

// 验证提取
const handleVerify = () => {
  ElMessage.success('验证成功！')
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

onMounted(() => {
  loadSources()
})
</script>

<style scoped lang="scss">
.content-extraction-tab {
  padding: 20px;
}

.tab-header {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}
</style>
