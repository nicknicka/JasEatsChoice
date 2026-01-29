<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Share } from '@element-plus/icons-vue'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'

const route = useRoute()
const router = useRouter()

// 热点数据
const hotTopic = ref({
  content: '',
  sourceType: '',
  sourceId: '',
  redirectUrl: '',
  clickable: false,
  publishTime: '',
  author: ''
})

// 加载状态
const loading = ref(true)

// 获取热点详情
const fetchHotTopicDetail = async () => {
  loading.value = true
  try {
    // 从路由参数或localStorage获取热点数据
    const topicData = route.params.data
      ? JSON.parse(decodeURIComponent(route.params.data))
      : JSON.parse(localStorage.getItem('currentHotTopic') || '{}')

    if (topicData && topicData.content) {
      hotTopic.value = topicData
    } else {
      // 如果没有传递数据，从后端重新获取
      const response = await api.get(API_CONFIG.home.hotTopic)
      if (response.data) {
        hotTopic.value = typeof response.data === 'object' ? response.data : {
          content: response.data,
          clickable: false
        }
      }
    }
  } catch (error) {
    console.error('加载热点详情失败:', error)
    ElMessage.error('加载热点详情失败')
  } finally {
    loading.value = false
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 分享热点
const shareHotTopic = async () => {
  const shareData = {
    title: '今日热点',
    text: hotTopic.value.content,
    url: window.location.href
  }

  try {
    if (navigator.share) {
      await navigator.share(shareData)
      ElMessage.success('分享成功')
    } else {
      // 降级处理:复制到剪贴板
      const shareText = `${shareData.title}\n${shareData.text}\n${shareData.url}`
      await navigator.clipboard.writeText(shareText)
      ElMessage.success('已复制到剪贴板')
    }
  } catch (error) {
    if (error.name !== 'AbortError') {
      console.error('分享失败:', error)
      ElMessage.error('分享失败,请重试')
    }
  }
}

// 获取来源类型的中文名称
const getSourceTypeName = (sourceType) => {
  const typeMap = {
    'TUTORIAL': '教程',
    'MERCHANT': '商家',
    'ADMIN': '官方',
    'AI_GENERATED': 'AI生成'
  }
  return typeMap[sourceType] || '未知来源'
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '未知时间'
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  fetchHotTopicDetail()
})
</script>

<template>
  <div class="hot-topic-detail-page">
    <!-- 顶部导航栏 -->
    <div class="top-nav-bar">
      <el-button circle @click="goBack" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <h2 class="page-title">今日热点</h2>
      <el-button circle @click="shareHotTopic" class="share-btn">
        <el-icon><Share /></el-icon>
      </el-button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton animated>
        <template #template>
          <el-skeleton-item variant="text" style="width: 80%; height: 40px; margin-bottom: 20px" />
          <el-skeleton-item variant="p" style="width: 100%; height: 200px" />
          <el-skeleton-item variant="text" style="width: 60%; margin-top: 20px" />
        </template>
      </el-skeleton>
    </div>

    <!-- 热点详情内容 -->
    <div v-else class="hot-topic-content">
      <!-- 来源信息 -->
      <div class="meta-info">
        <el-tag v-if="hotTopic.sourceType" type="danger" effect="dark" size="large">
          {{ getSourceTypeName(hotTopic.sourceType) }}
        </el-tag>
        <span v-if="hotTopic.publishTime" class="publish-time">
          {{ formatTime(hotTopic.publishTime) }}
        </span>
      </div>

      <!-- 热点内容 -->
      <div class="topic-content">
        <h1 class="topic-title">{{ hotTopic.content || '暂无热点内容' }}</h1>
      </div>

      <!-- 扩展信息 -->
      <div class="extended-info">
        <div v-if="hotTopic.author" class="info-item">
          <span class="info-label">发布者：</span>
          <span class="info-value">{{ hotTopic.author }}</span>
        </div>

        <div v-if="hotTopic.sourceType === 'TUTORIAL' && hotTopic.sourceId" class="info-item">
          <span class="info-label">关联教程：</span>
          <el-button
            type="primary"
            text
            @click="router.push(`/user/home/tutorials/${hotTopic.sourceId}`)"
          >
            查看教程详情
          </el-button>
        </div>

        <div v-if="hotTopic.redirectUrl" class="info-item">
          <span class="info-label">相关链接：</span>
          <el-button
            type="primary"
            text
            @click="
              hotTopic.redirectUrl.startsWith('http')
                ? window.api?.openExternal(hotTopic.redirectUrl)
                : router.push(hotTopic.redirectUrl)
            "
          >
            点击访问
          </el-button>
        </div>
      </div>

      <!-- 空状态提示 -->
      <el-empty
        v-if="!hotTopic.content"
        description="暂无热点内容"
        :image-size="200"
      >
        <el-button type="primary" @click="goBack">返回首页</el-button>
      </el-empty>
    </div>
  </div>
</template>

<style scoped lang="less">
.hot-topic-detail-page {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(135deg, #fff5f5 0%, #fff 50%, #f8f9fa 100%);
  padding-bottom: 40px;
}

// 顶部导航栏
.top-nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: #ffffff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
  backdrop-filter: blur(10px);
  background-color: rgba(255, 255, 255, 0.95);

  .back-btn,
  .share-btn {
    width: 40px;
    height: 40px;
    border: none;
    background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
    color: #fff;
    transition: all 0.3s ease;

    &:hover {
      transform: scale(1.1);
      box-shadow: 0 4px 12px rgba(255, 107, 107, 0.4);
    }
  }

  .page-title {
    margin: 0;
    font-size: 20px;
    font-weight: 700;
    color: #333;
    text-align: center;
    flex: 1;
  }
}

// 加载容器
.loading-container {
  padding: 40px 20px;
}

// 热点内容
.hot-topic-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

// 元信息
.meta-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;

  .publish-time {
    font-size: 14px;
    color: #999;
    padding: 6px 12px;
    background: rgba(0, 0, 0, 0.05);
    border-radius: 16px;
  }
}

// 热点标题
.topic-content {
  margin-bottom: 32px;

  .topic-title {
    font-size: 28px;
    font-weight: 700;
    color: #333;
    line-height: 1.6;
    margin: 0;
    padding: 24px;
    background: #ffffff;
    border-radius: 16px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  }
}

// 扩展信息
.extended-info {
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);

  .info-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .info-label {
      font-size: 14px;
      color: #666;
      font-weight: 600;
      min-width: 80px;
    }

    .info-value {
      font-size: 14px;
      color: #333;
    }
  }
}

// 响应式适配
@media (max-width: 768px) {
  .topic-content .topic-title {
    font-size: 24px;
    padding: 20px;
  }

  .extended-info {
    padding: 20px;
  }

  .meta-info .publish-time {
    font-size: 12px;
  }
}
</style>
