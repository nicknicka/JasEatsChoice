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
            :class="{ published: source.isPublished, failed: source.extractionStatus === 'FAILED' || source.extractionStatus === 'PARSE_FAILED' }"
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, VideoCamera, Document, Loading } from '@element-plus/icons-vue'
import ContentExtractionDialog from './ContentExtractionDialog.vue'
import contentExtractionApi from '@/api/contentExtraction'

// 定义 props
const props = defineProps({
  sources: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

// 定义 emit
const emit = defineEmits(['refresh', 'view-detail', 're-extract', 'delete'])

const showAddDialog = ref(false)

// 自动刷新定时器
let refreshTimer = null
// 轮询间隔（毫秒）
const POLL_INTERVAL = 3000

// 进行中的提取
const processingSources = computed(() => {
  return props.sources.filter(s =>
    s.extractionStatus === 'PENDING' || s.extractionStatus === 'PROCESSING'
  )
})

// 已完成的提取
const completedSources = computed(() => {
  return props.sources.filter(s =>
    s.extractionStatus === 'SUCCESS' || s.extractionStatus === 'FAILED' || s.extractionStatus === 'PARSE_FAILED'
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

// 加载内容源列表（触发父组件刷新）
const loadSources = () => {
  emit('refresh')
}

// 查看提取详情
const viewExtraction = (source) => {
  if (source.extractionId) {
    emit('view-detail', source)
  } else if (source.extractionStatus === 'PENDING' || source.extractionStatus === 'PROCESSING') {
    ElMessage.info('内容正在提取中，请稍后查看')
  } else if (source.extractionStatus === 'PARSE_FAILED') {
    ElMessage.warning('内容已抓取但解析失败，请查看错误信息')
  } else {
    ElMessage.warning('无法查看详情，提取可能失败或未完成')
  }
}

// 发布为食谱
const publishAsRecipe = async (source) => {
  if (!source.extractionId) {
    ElMessage.warning('无法发布，请等待提取完成')
    return
  }

  try {
    await ElMessageBox.confirm('确认发布为食谱？', '提示', {
      type: 'warning'
    })

    const response = await contentExtractionApi.publishAsRecipe(source.extractionId, {})
    if (response.code === '200' || response.code === 200) {
      ElMessage.success('发布成功')
      emit('refresh')
    } else {
      ElMessage.error(response.message || '发布失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布失败:', error)
      ElMessage.error('发布失败')
    }
  }
}

// 删除内容源
const deleteSource = async (source) => {
  try {
    await ElMessageBox.confirm('确认删除该提取记录？', '提示', {
      type: 'warning'
    })
    emit('delete', source)
  } catch (error) {
    // 用户取消
  }
}

// 启动自动轮询
const startPolling = () => {
  // 清除已存在的定时器
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }

  // 每3秒刷新一次列表
  refreshTimer = setInterval(() => {
    // 检查是否有处理中的任务
    const hasProcessing = props.sources.some(s =>
      s.extractionStatus === 'PENDING' || s.extractionStatus === 'PROCESSING'
    )

    if (hasProcessing) {
      console.log('自动刷新：有处理中的任务')
      emit('refresh')
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

// 监听 sources 变化，自动启动/停止轮询
watch(() => props.sources, (newSources) => {
  const hasProcessing = newSources.some(s =>
    s.extractionStatus === 'PENDING' || s.extractionStatus === 'PROCESSING'
  )

  if (hasProcessing && !refreshTimer) {
    startPolling()
  } else if (!hasProcessing && refreshTimer) {
    stopPolling()
  }
}, { deep: true })

onMounted(() => {
  // 启动自动轮询
  startPolling()
})

// 组件卸载时清除定时器
onUnmounted(() => {
  stopPolling()
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
  font-size: 1.429rem /* 原值: 20px */;
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
  font-size: 1.143rem /* 原值: 16px */;
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
  font-size: 2.286rem /* 原值: 32px */;
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
  font-size: 1rem /* 原值: 14px */;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.source-meta {
  margin: 0 0 8px 0;
  font-size: 0.857rem /* 原值: 12px */;
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
  font-size: 0.857rem /* 原值: 12px */;
  color: #c0c4cc;
}

.extraction-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.929rem /* 原值: 13px */;
  color: #409eff;
}

.source-actions {
  display: flex;
  gap: 8px;
}
</style>
