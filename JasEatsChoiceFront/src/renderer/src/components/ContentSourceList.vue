<template>
  <div class="content-source-list">
    <!-- 头部 -->
    <div class="list-header">
      <h3 class="list-title">我的提取</h3>
      <el-button type="primary" size="small" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        添加内容
      </el-button>
    </div>

    <!-- 内容源列表 -->
    <div v-loading="loading" class="source-content">
      <!-- 进行中的提取 -->
      <div v-if="processingSources.length > 0" class="section">
        <h4 class="section-title">提取中</h4>
        <div class="source-cards">
          <div
            v-for="source in processingSources"
            :key="source.id"
            class="source-card processing"
          >
            <div class="source-cover" v-if="source.coverImage">
              <img :src="source.coverImage" :alt="source.title" />
            </div>
            <div class="source-cover placeholder" v-else>
              <el-icon><VideoCamera /></el-icon>
            </div>

            <div class="source-info">
              <h5 class="source-title">{{ source.title || '未命名内容' }}</h5>
              <p class="source-meta">
                <el-tag :type="getPlatformTagType(source.platform)" size="small">
                  {{ source.platformName }}
                </el-tag>
                <span class="source-url">{{ truncateUrl(source.contentUrl) }}</span>
              </p>
              <div class="extraction-status">
                <el-icon class="is-loading"><Loading /></el-icon>
                <span>正在提取中...</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 已完成的提取 -->
      <div class="section">
        <h4 class="section-title">提取完成</h4>
        <div class="source-cards">
          <div
            v-for="source in completedSources"
            :key="source.id"
            class="source-card"
            :class="{ published: source.isPublished }"
            @click="viewExtraction(source)"
          >
            <div class="source-cover" v-if="source.extractedDishImage">
              <img :src="source.extractedDishImage" :alt="source.extractedDishName" />
            </div>
            <div class="source-cover placeholder" v-else-if="source.coverImage">
              <img :src="source.coverImage" :alt="source.title" />
            </div>
            <div class="source-cover placeholder" v-else>
              <el-icon><Document /></el-icon>
            </div>

            <div class="source-info">
              <h5 class="source-title">{{ source.extractedDishName || source.title }}</h5>
              <p class="source-meta">
                <el-tag :type="getPlatformTagType(source.platform)" size="small">
                  {{ source.platformName }}
                </el-tag>
                <el-tag v-if="source.isPublished" type="success" size="small">已发布</el-tag>
              </p>
              <p class="source-time">{{ formatTime(source.extractionTime || source.createTime) }}</p>
            </div>

            <div class="source-actions">
              <el-button
                type="primary"
                size="small"
                text
                @click.stop="viewExtraction(source)"
              >
                查看
              </el-button>
              <el-button
                v-if="!source.isPublished"
                type="success"
                size="small"
                text
                @click.stop="publishAsRecipe(source)"
              >
                发布
              </el-button>
              <el-button
                type="danger"
                size="small"
                text
                @click.stop="deleteSource(source)"
              >
                删除
              </el-button>
            </div>
          </div>

          <!-- 空状态 -->
          <el-empty
            v-if="!loading && completedSources.length === 0"
            description="暂无提取记录"
            :image-size="80"
          />
        </div>
      </div>
    </div>

    <!-- 添加内容对话框 -->
    <content-extraction-dialog
      v-model:visible="showAddDialog"
      @success="loadSources"
    />

    <!-- 提取详情对话框 -->
    <extraction-detail-dialog
      v-model:visible="showDetailDialog"
      :extraction-id="selectedExtractionId"
      @published="loadSources"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, VideoCamera, Document, Loading } from '@element-plus/icons-vue'
import ContentExtractionDialog from './ContentExtractionDialog.vue'
import ExtractionDetailDialog from './ExtractionDetailDialog.vue'
import api from '@/api'

const loading = ref(false)
const showAddDialog = ref(false)
const showDetailDialog = ref(false)
const selectedExtractionId = ref('')
const sources = ref([])

// 进行中的提取
const processingSources = computed(() => {
  return sources.value.filter(s =>
    s.extractionStatus === 'PENDING' || s.extractionStatus === 'PROCESSING'
  )
})

// 已完成的提取
const completedSources = computed(() => {
  return sources.value.filter(s =>
    s.extractionStatus === 'SUCCESS' || s.extractionStatus === 'FAILED'
  )
})

// 获取平台标签类型
const getPlatformTagType = (platform) => {
  const typeMap = {
    '抖音': 'danger',
    '小红书': 'warning',
    '哔哩哔哩': 'primary',
    '微信': 'success',
    '今日头条': 'info',
    '快手': 'warning'
  }
  return typeMap[platform] || 'info'
}

// 截断URL
const truncateUrl = (url) => {
  if (!url) return ''
  if (url.length > 40) {
    return url.substring(0, 40) + '...'
  }
  return url
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`

  return date.toLocaleDateString()
}

// 加载内容源列表
const loadSources = async () => {
  loading.value = true
  try {
    const response = await api.get('/v1/content-extraction/sources')
    if (response.data.code === 200) {
      sources.value = response.data.data || []
    }
  } catch (error) {
    console.error('加载失败:', error)
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 查看提取详情
const viewExtraction = (source) => {
  // 需要先获取extractionId
  // 这里简化处理，实际应该从source中获取或通过API查询
  ElMessage.info('查看详情功能开发中')
}

// 发布为食谱
const publishAsRecipe = async (source) => {
  try {
    await ElMessageBox.confirm('确认发布为食谱？', '提示', {
      type: 'warning'
    })

    // TODO: 调用发布API
    ElMessage.success('发布成功')
    loadSources()
  } catch (error) {
    // 用户取消
  }
}

// 删除内容源
const deleteSource = async (source) => {
  try {
    await ElMessageBox.confirm('确认删除该提取记录？', '提示', {
      type: 'warning'
    })

    const response = await api.delete(`/v1/content-extraction/source/${source.id}`)
    if (response.data.code === 200) {
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

onMounted(() => {
  loadSources()
})
</script>

<style scoped>
.content-source-list {
  padding: 20px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.list-title {
  margin: 0;
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.source-content {
  min-height: 200px;
}

.section {
  margin-bottom: 24px;
}

.section:last-child {
  margin-bottom: 0;
}

.section-title {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #606266;
}

.source-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.source-card {
  display: flex;
  padding: 12px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  cursor: pointer;
  transition: all 0.3s;
}

.source-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.source-card.published {
  border-color: #67c23a;
}

.source-card.processing {
  border-color: #409eff;
}

.source-cover {
  width: 80px;
  height: 80px;
  margin-right: 12px;
  flex-shrink: 0;
  border-radius: 6px;
  overflow: hidden;
}

.source-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.source-cover.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  font-size: 32px;
  color: #c0c4cc;
}

.source-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.source-title {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.source-meta {
  margin: 0 0 8px 0;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 8px;
}

.source-url {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.source-time {
  margin: 0;
  font-size: 12px;
  color: #c0c4cc;
}

.extraction-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #409eff;
}

.source-actions {
  display: flex;
  gap: 8px;
}
</style>
