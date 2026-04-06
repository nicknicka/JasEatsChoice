<script setup>
import { ref, onMounted } from 'vue'
import { VideoCamera, Document, Check, Shop, MagicStick, Star, View } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

// 返回教程列表页面（根据来源决定返回哪里）
const goBackToList = () => {
  // 检查URL参数中的from字段
  const from = route.query.from

  if (from === 'my') {
    // 从"我的教程"来的，返回到我的教程
    router.push('/user/home/tutorials/my')
  } else if (from === 'square') {
    // 从"教程广场"来的，返回到教程广场
    router.push('/user/home/tutorials')
  } else {
    // 默认返回到我的教程（因为通常用户是从自己的教程进入的）
    router.push('/user/home/tutorials/my')
  }
}

// 当前教程
const currentTutorial = ref(null)
const loading = ref(true)

// 从后端获取教程详情
const fetchTutorialDetail = async () => {
  loading.value = true
  console.log('=== fetchTutorialDetail 开始执行 ===')
  console.log('教程ID:', route.params.id)
  console.log('请求URL:', `${API_CONFIG.tutorial.detail}${route.params.id}`)

  try {
    const tutorialId = route.params.id
    const response = await api.get(`${API_CONFIG.tutorial.detail}${tutorialId}`)

    console.log('API响应完整内容:', response)
    console.log('response.data:', response.data)
    console.log('response.id:', response.id)
    console.log('response.title:', response.title)

    // 支持多种响应格式：
    // 1. response.data 是教程对象
    // 2. response 本身是教程对象
    let tutorial = null

    if (response.data && typeof response.data === 'object' && response.data.id) {
      tutorial = response.data
      console.log('使用 response.data (教程对象)')
    } else if (response && typeof response === 'object' && response.id) {
      tutorial = response
      console.log('使用 response (教程对象)')
    }

    if (tutorial) {
      currentTutorial.value = tutorial
      console.log('✅ 成功加载教程详情:', tutorial.title)
    } else {
      console.warn('⚠️ 教程不存在或响应格式异常')
      ElMessage.error('教程不存在')
      goBackToList()
    }
  } catch (error) {
    console.error('❌ 获取教程详情失败')
    console.error('错误对象:', error)
    console.error('错误消息:', error.message)
    console.error('响应状态:', error.response?.status)

    if (error.response?.status === 404) {
      ElMessage.error('教程不存在')
    } else {
      ElMessage.error(error.response?.data?.message || error.message || '加载教程失败')
    }
  } finally {
    loading.value = false
    console.log('=== fetchTutorialDetail 执行结束 ===')
  }
}

// 获取难度显示名称
const getDifficultyName = (difficulty) => {
  const map = {
    BEGINNER: '初级',
    INTERMEDIATE: '中级',
    ADVANCED: '高级'
  }
  return map[difficulty] || difficulty
}

// Markdown 转 HTML（简单的实现）
const renderMarkdown = (content) => {
  if (!content) return ''

  let html = content
    // 标题（需要按顺序替换，避免 # 被处理多次）
    .replace(/^### (.*$)/gim, '<h3>$1</h3>')
    .replace(/^## (.*$)/gim, '<h2>$1</h2>')
    .replace(/^# (.*$)/gim, '<h1>$1</h1>')
    // 粗体
    .replace(/\*\*(.*?)\*\*/gim, '<strong>$1</strong>')
    // 斜体
    .replace(/\*(.*?)\*/gim, '<em>$1</em>')
    // 链接
    .replace(/\[([^\]]+)\]\(([^)]+)\)/gim, '<a href="$2" target="_blank">$1</a>')
    // 无序列表
    .replace(/^\- (.*$)/gim, '<li>$1</li>')
    // 有序列表
    .replace(/^\d+\. (.*$)/gim, '<li>$1</li>')
    // 将连续的列表项包装在 ul 中
    .replace(/(<li>.*<\/li>)/gim, '<ul>$1</ul>')
    // 合并相邻的 ul
    .replace(/<\/ul>\s*<ul>/gim, '')
    // 换行（除了在 HTML 标签后的换行）
    .replace(/\n/gim, '<br>')

  return html
}

// 格式化内容（支持Markdown）
const formatContent = (content) => {
  if (!content) return ''
  return renderMarkdown(content)
}

// 处理图片加载失败
const handleImageError = (event) => {
  // 当图片加载失败时，隐藏图片元素，让CSS背景显示
  event.target.style.display = 'none'
  // 添加has-no-cover类到父元素
  event.target.parentElement?.classList.add('has-no-cover')
}

// 页面加载时获取教程数据
onMounted(() => {
  fetchTutorialDetail()
})
</script>

<template>
  <div class="tutorial-detail-container">
    <div class="page-header">
      <common-back-button
        type="primary"
        size="small"
        text="返回列表"
        @click="goBackToList"
        :use-router-back="false"
      />
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton animated>
        <template #template>
          <el-skeleton-item variant="image" style="width: 100%; height: 400px; border-radius: 8px" />
          <el-skeleton-item variant="h1" style="width: 60%; margin: 20px 0" />
          <el-skeleton-item variant="text" style="width: 80%" />
          <el-skeleton-item variant="text" style="width: 70%" />
        </template>
      </el-skeleton>
    </div>

    <!-- 教程详情 -->
    <el-card v-else-if="currentTutorial" class="tutorial-detail-card scale-in" shadow="hover">
      <!-- 封面图 -->
      <div class="tutorial-cover" :class="{ 'has-no-cover': !currentTutorial.cover_image && !currentTutorial.coverImage }">
        <img
          v-if="currentTutorial.cover_image || currentTutorial.coverImage"
          :src="currentTutorial.cover_image || currentTutorial.coverImage"
          :alt="currentTutorial.title"
          @error="handleImageError"
        />

        <!-- 没有封面图时的默认内容 -->
        <div v-if="!currentTutorial.cover_image && !currentTutorial.coverImage" class="default-cover-content">
          <el-icon :size="64" color="white">
            <component :is="currentTutorial.type === 'video' ? VideoCamera : Document" />
          </el-icon>
          <div class="default-cover-text">
            <div class="cover-type-badge">{{ currentTutorial.type === 'video' ? '视频' : '图文' }}</div>
            <div class="cover-label">{{ currentTutorial.type === 'video' ? '视频教程' : '图文指南' }}</div>
          </div>
        </div>

        <div class="tutorial-type-overlay" v-if="currentTutorial.cover_image || currentTutorial.coverImage">
          <el-icon :class="currentTutorial.type === 'video' ? 'video-icon' : 'article-icon'">
            <component :is="currentTutorial.type === 'video' ? VideoCamera : Document" />
          </el-icon>
          <span>{{ currentTutorial.type === 'video' ? '视频教程' : '图文指南' }}</span>
        </div>
      </div>

      <div class="tutorial-info fade-in-up delay-100">
        <!-- 来源标签 -->
        <div class="tutorial-source-badges">
          <!-- 官方认证标签 -->
          <el-tag v-if="currentTutorial.source_type === 'ADMIN' && currentTutorial.is_official"
                  type="danger"
                  effect="dark">
            <el-icon><Check /></el-icon> 官方认证
          </el-tag>

          <!-- 商家标签 -->
          <el-tag v-if="currentTutorial.source_type === 'MERCHANT'"
                  type="warning"
                  effect="plain">
            <el-icon><Shop /></el-icon> {{ currentTutorial.merchantName || '商家贡献' }}
          </el-tag>

          <!-- AI生成标签 -->
          <el-tag v-if="currentTutorial.source_type === 'AI_GENERATED'"
                  :type="currentTutorial.review_status === 'APPROVED' ? 'success' : 'info'"
                  effect="plain">
            <el-icon><MagicStick /></el-icon>
            AI生成
            <span v-if="currentTutorial.review_status === 'APPROVED'" class="reviewed-badge">
              ✓ 人工审核通过
            </span>
          </el-tag>

          <!-- 难度标签 -->
          <el-tag v-if="currentTutorial.difficulty"
                  type="info"
                  effect="plain">
            {{ getDifficultyName(currentTutorial.difficulty) }}
          </el-tag>
        </div>

        <h1 class="tutorial-title">{{ currentTutorial.title }}</h1>

        <div class="tutorial-meta">
          <span class="duration">
            <el-icon><VideoCamera /></el-icon>
            {{ currentTutorial.duration }}
          </span>
          <span class="views">
            <el-icon><View /></el-icon>
            {{ currentTutorial.view_count || currentTutorial.views }} 浏览
          </span>
          <span v-if="currentTutorial.rating" class="rating">
            <el-icon><Star /></el-icon>
            {{ currentTutorial.rating }}
            <span class="rating-count">({{ currentTutorial.rating_count }}人评分)</span>
          </span>
          <span v-if="currentTutorial.author" class="author">
            作者: {{ currentTutorial.author }}
          </span>
        </div>

        <div class="tutorial-content">
          <h3>内容</h3>
          <div class="content-text" v-html="formatContent(currentTutorial.content)"></div>
        </div>
      </div>
    </el-card>

    <!-- 未找到教程 -->
    <div v-else class="not-found">
      <h2>教程不存在</h2>
      <el-button type="primary" @click="goBackToList">返回列表</el-button>
    </div>
  </div>
</template>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';

.tutorial-detail-container {
  .nordic-page-container();
  max-width: 900px;
  margin: 0 auto;

  .page-header {
    margin-bottom: 25px;
  }

  .loading-container {
    padding: 40px 0;
  }

  .tutorial-detail-card {
    .nordic-card();
    padding: 0;
    overflow: hidden;
  }

  .tutorial-cover {
    position: relative;
    width: 100%;
    height: 400px;
    overflow: hidden;

    // 有封面图时
    &:not(.has-no-cover) {
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }

    // 没有封面图时的渐变背景
    &.has-no-cover {
      background: linear-gradient(135deg, @nordic-accent 0%, @nordic-blue 100%);
      display: flex;
      align-items: center;
      justify-content: center;

      .default-cover-content {
        display: flex;
        align-items: center;
        gap: @nordic-space-lg;
        color: @nordic-surface;
        z-index: 1;
      }

      .default-cover-text {
        display: flex;
        flex-direction: column;
        gap: @nordic-space-sm;
        align-items: center;

        .cover-type-badge {
          background: rgba(255, 255, 255, 0.3);
          padding: 6px @nordic-space-md;
          border-radius: @nordic-radius-lg;
          font-size: @nordic-text-sm;
          font-weight: 600;
          backdrop-filter: blur(10px);
        }

        .cover-label {
          font-size: @nordic-text-md;
          font-weight: 500;
          text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
        }
      }
    }

    .tutorial-type-overlay {
      position: absolute;
      top: 20px;
      right: 20px;
      background: rgba(255, 255, 255, 0.95);
      padding: @nordic-space-sm @nordic-space-md;
      border-radius: @nordic-radius-sm;
      display: flex;
      align-items: center;
      gap: 5px;
      box-shadow: 0 2px 8px @nordic-shadow;

      .video-icon {
        color: @nordic-red;
        font-size: @nordic-text-lg;
      }

      .article-icon {
        color: @nordic-yellow;
        font-size: @nordic-text-lg;
      }

      span {
        font-weight: 600;
        color: @nordic-text;
        font-size: @nordic-text-base;
      }
    }
  }

  .tutorial-info {
    padding: 30px;

    .tutorial-source-badges {
      display: flex;
      gap: 10px;
      margin-bottom: @nordic-space-lg;
      flex-wrap: wrap;

      .el-tag {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 6px @nordic-space-md;
        border-radius: @nordic-radius-md;
        font-size: @nordic-text-sm;

        .el-icon {
          font-size: @nordic-text-md;
        }

        .reviewed-badge {
          margin-left: 4px;
          padding-left: 6px;
          border-left: 1px solid currentColor;
          font-size: @nordic-text-xs;
        }
      }
    }

    .tutorial-title {
      font-size: 2.286rem /* 原值: 32px */;
      font-weight: bold;
      color: @nordic-text;
      margin-bottom: @nordic-space-lg;
      line-height: 1.4;
    }

    .tutorial-meta {
      color: @nordic-text-secondary;
      font-size: 1.071rem /* 原值: 15px */;
      margin-bottom: 35px;
      display: flex;
      flex-wrap: wrap;
      gap: 25px;
      align-items: center;

      span {
        display: flex;
        align-items: center;
        gap: 6px;

        .el-icon {
          font-size: 1.286rem /* 原值: 18px */;
        }
      }

      .rating-count {
        color: @nordic-text-muted;
        font-size: @nordic-text-sm;
        margin-left: 4px;
      }
    }

    .tutorial-content {
      h3 {
        font-size: 1.714rem /* 原值: 24px */;
        font-weight: bold;
        color: @nordic-text;
        margin-bottom: @nordic-space-lg;
        padding-bottom: 10px;
        border-bottom: 2px solid @nordic-red;
      }

      .content-text {
        font-size: @nordic-text-md;
        line-height: 2;
        color: @nordic-text-secondary;

        :deep(h1),
        :deep(h2),
        :deep(h3) {
          margin-top: @nordic-space-lg;
          margin-bottom: 12px;
          color: @nordic-text;
        }

        :deep(p) {
          margin-bottom: 15px;
        }

        :deep(ul),
        :deep(ol) {
          padding-left: 25px;
          margin-bottom: 15px;

          li {
            margin-bottom: @nordic-space-sm;
          }
        }

        :deep(strong) {
          color: @nordic-red;
          font-weight: 600;
        }

        :deep(code) {
          background: @nordic-divider;
          padding: 2px 6px;
          border-radius: 4px;
          font-family: 'Courier New', monospace;
        }
      }
    }
  }

  .not-found {
    text-align: center;
    padding: 80px 0;

    h2 {
      color: @nordic-text-secondary;
      margin-bottom: 25px;
      font-size: 1.714rem /* 原值: 24px */;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .tutorial-detail-container {
    padding: 15px 0;

    .tutorial-cover {
      height: 250px;
    }

    .tutorial-info {
      padding: @nordic-space-lg;

      .tutorial-title {
        font-size: 1.714rem /* 原值: 24px */;
      }

      .tutorial-meta {
        font-size: @nordic-text-sm;
        gap: 15px;
      }

      .tutorial-content h3 {
        font-size: @nordic-text-lg;
      }
    }
  }
}
</style>
