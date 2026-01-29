<script setup>
import { ref, onMounted, computed } from 'vue'
import { VideoCamera, Document, Search, Plus, User } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 返回用户首页
const goBackToHome = () => {
  router.push('/user/home')
}

// 教程数据
const tutorials = ref([])
const loading = ref(false)

// 筛选条件
const searchKeyword = ref('')
const selectedType = ref('all') // all, video, article
const selectedSource = ref('all') // all, ADMIN, MERCHANT, USER, AI_GENERATED
const selectedDifficulty = ref('all') // all, BEGINNER, INTERMEDIATE, ADVANCED

// 统计数据
const stats = computed(() => {
  const all = tutorials.value
  return {
    total: all.length,
    video: all.filter(t => t.type === 'video').length,
    article: all.filter(t => t.type === 'article').length,
    admin: all.filter(t => t.source_type === 'ADMIN').length,
    merchant: all.filter(t => t.source_type === 'MERCHANT').length,
    user: all.filter(t => t.source_type === 'USER').length,
    ai: all.filter(t => t.source_type === 'AI_GENERATED').length
  }
})

// 过滤后的教程列表
const filteredTutorials = computed(() => {
  let result = tutorials.value

  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(t =>
      (t.title && t.title.toLowerCase().includes(keyword)) ||
      (t.content && t.content.toLowerCase().includes(keyword))
    )
  }

  // 类型过滤
  if (selectedType.value !== 'all') {
    result = result.filter(t => t.type === selectedType.value)
  }

  // 来源过滤
  if (selectedSource.value !== 'all') {
    result = result.filter(t => t.source_type === selectedSource.value)
  }

  // 难度过滤
  if (selectedDifficulty.value !== 'all') {
    result = result.filter(t => t.difficulty === selectedDifficulty.value)
  }

  return result
})

// 获取教程数据
const fetchTutorials = async () => {
  loading.value = true
  try {
    const response = await api.get(API_CONFIG.tutorial.list)
    if (response.data) {
      tutorials.value = response.data
    }
  } catch (error) {
    console.error('加载教程列表失败:', error)
    ElMessage.warning('加载失败，显示模拟数据')
    // 使用模拟数据
    tutorials.value = [
      {
        id: 1,
        title: '青木瓜沙拉制作教程',
        type: 'video',
        source_type: 'ADMIN',
        status: 'PUBLISHED',
        difficulty: 'BEGINNER',
        duration: '5:30',
        view_count: 12500,
        rating: 4.8,
        is_official: true
      },
      {
        id: 2,
        title: '夏日低卡饮食指南',
        type: 'article',
        source_type: 'ADMIN',
        status: 'PUBLISHED',
        difficulty: 'BEGINNER',
        duration: '8分钟',
        view_count: 8200,
        rating: 4.9,
        is_official: true
      },
      {
        id: 3,
        title: '秘制红烧肉做法',
        type: 'video',
        source_type: 'MERCHANT',
        status: 'PUBLISHED',
        difficulty: 'INTERMEDIATE',
        duration: '12:30',
        view_count: 3500,
        rating: 4.6
      },
      {
        id: 4,
        title: '我的家常菜做法',
        type: 'article',
        source_type: 'USER',
        status: 'PUBLISHED',
        difficulty: 'BEGINNER',
        duration: '20分钟',
        view_count: 356,
        rating: 4.5
      },
      {
        id: 5,
        title: '番茄鸡蛋面的10种做法',
        type: 'article',
        source_type: 'AI_GENERATED',
        status: 'PUBLISHED',
        difficulty: 'INTERMEDIATE',
        review_status: 'APPROVED',
        duration: '15分钟',
        view_count: 5800,
        rating: 4.7
      }
    ]
  } finally {
    loading.value = false
  }
}

// 获取来源类型标签信息
const getSourceTag = (tutorial) => {
  const sourceMap = {
    ADMIN: {
      type: 'danger',
      effect: 'dark',
      icon: '✓',
      text: tutorial.is_official ? '官方认证' : '管理员'
    },
    MERCHANT: {
      type: 'warning',
      effect: 'plain',
      icon: '🏪',
      text: tutorial.merchantName || '商家'
    },
    USER: {
      type: 'primary',
      effect: 'plain',
      icon: '👤',
      text: '用户贡献'
    },
    AI_GENERATED: {
      type: tutorial.review_status === 'APPROVED' ? 'success' : 'info',
      effect: 'plain',
      icon: '✨',
      text: `AI生成${tutorial.review_status === 'APPROVED' ? ' ✓ 人工审核' : ''}`
    }
  }
  return sourceMap[tutorial.source_type] || { type: '', text: tutorial.source_type }
}

// 获取难度名称
const getDifficultyName = (difficulty) => {
  const map = {
    BEGINNER: '初级',
    INTERMEDIATE: '中级',
    ADVANCED: '高级'
  }
  return map[difficulty] || ''
}

// 重置筛选
const resetFilters = () => {
  searchKeyword.value = ''
  selectedType.value = 'all'
  selectedSource.value = 'all'
  selectedDifficulty.value = 'all'
}

// 页面加载时获取数据
onMounted(() => {
  fetchTutorials()
})
</script>

<template>
  <div class="tutorials-plaza-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <common-back-button
          type="primary"
          size="small"
          text="返回首页"
          @click="goBackToHome"
          :use-router-back="false"
        />
        <h2>教程广场</h2>
        <div class="stats-info">
          <el-tag size="small" type="info">共 {{ stats.total }} 个教程</el-tag>
          <el-tag size="small" type="danger">官方 {{ stats.admin }}</el-tag>
          <el-tag size="small" type="warning">商家 {{ stats.merchant }}</el-tag>
          <el-tag size="small" type="primary">用户 {{ stats.user }}</el-tag>
          <el-tag size="small" type="success">AI {{ stats.ai }}</el-tag>
        </div>
      </div>
      <div class="header-actions">
        <el-button @click="router.push('/user/home/my-tutorials')">
          <el-icon><User /></el-icon> 我的教程
        </el-button>
        <el-button type="primary" @click="router.push('/user/home/publish-tutorial')">
          <el-icon><Plus /></el-icon> 发布教程
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选栏 -->
    <div class="filter-bar">
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索教程标题或内容..."
          :prefix-icon="Search"
          clearable
          style="width: 300px"
        />
      </div>

      <div class="filter-group">
        <el-select v-model="selectedType" placeholder="教程类型" style="width: 120px">
          <el-option label="全部类型" value="all" />
          <el-option label="视频教程" value="video" />
          <el-option label="图文指南" value="article" />
        </el-select>

        <el-select v-model="selectedSource" placeholder="来源" style="width: 130px">
          <el-option label="全部来源" value="all" />
          <el-option label="官方认证" value="ADMIN" />
          <el-option label="商家贡献" value="MERCHANT" />
          <el-option label="用户贡献" value="USER" />
          <el-option label="AI生成" value="AI_GENERATED" />
        </el-select>

        <el-select v-model="selectedDifficulty" placeholder="难度" style="width: 120px">
          <el-option label="全部难度" value="all" />
          <el-option label="初级" value="BEGINNER" />
          <el-option label="中级" value="INTERMEDIATE" />
          <el-option label="高级" value="ADVANCED" />
        </el-select>

        <el-button @click="resetFilters">重置</el-button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="6" animated />
    </div>

    <!-- 空状态 -->
    <div v-else-if="filteredTutorials.length === 0" class="empty-container">
      <el-empty description="没有找到符合条件的教程">
        <el-button type="primary" @click="resetFilters">清除筛选条件</el-button>
      </el-empty>
    </div>

    <!-- 教程列表 -->
    <div v-else class="tutorial-grid">
      <el-card
        v-for="tutorial in filteredTutorials"
        :key="tutorial.id"
        class="tutorial-card"
        shadow="hover"
        @click="router.push(`/user/home/tutorials/${tutorial.id}`)"
      >
        <div class="tutorial-header">
          <div class="type-icon">
            <el-icon :size="32" :class="tutorial.type === 'video' ? 'video-icon' : 'article-icon'">
              <component :is="tutorial.type === 'video' ? VideoCamera : Document" />
            </el-icon>
          </div>
          <el-tag :type="tutorial.type === 'video' ? 'danger' : 'warning'" size="small">
            {{ tutorial.type === 'video' ? '视频' : '图文' }}
          </el-tag>
        </div>

        <div class="tutorial-content">
          <!-- 来源标签 -->
          <div class="source-tags">
            <el-tag
              :type="getSourceTag(tutorial).type"
              :effect="getSourceTag(tutorial).effect"
              size="small"
            >
              <span class="tag-icon">{{ getSourceTag(tutorial).icon }}</span>
              {{ getSourceTag(tutorial).text }}
            </el-tag>
            <el-tag v-if="tutorial.difficulty" type="info" size="small">
              {{ getDifficultyName(tutorial.difficulty) }}
            </el-tag>
          </div>

          <h3 class="tutorial-title">{{ tutorial.title }}</h3>

          <div class="tutorial-meta">
            <span class="duration">{{ tutorial.duration }}</span>
            <span class="views">{{ tutorial.view_count?.toLocaleString() || 0 }} 浏览</span>
            <span v-if="tutorial.rating" class="rating">⭐ {{ tutorial.rating }}</span>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped lang="less">
.tutorials-plaza-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    padding: 20px;
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;

      h2 {
        margin: 0;
        font-size: 24px;
        font-weight: bold;
        color: #303133;
      }

      .stats-info {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
      }
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .filter-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 16px 20px;
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

    .search-box {
      flex-shrink: 0;
    }

    .filter-group {
      display: flex;
      gap: 12px;
      align-items: center;
    }
  }

  .loading-container {
    padding: 40px;
    background: white;
    border-radius: 12px;
  }

  .empty-container {
    padding: 60px 20px;
    background: white;
    border-radius: 12px;
    text-align: center;
  }

  .tutorial-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 20px;

    .tutorial-card {
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      }

      .tutorial-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;
        padding-bottom: 12px;
        border-bottom: 1px solid #f0f0f0;

        .type-icon {
          .video-icon {
            color: #ff6b6b;
          }

          .article-icon {
            color: #f7b267;
          }
        }
      }

      .tutorial-content {
        .source-tags {
          display: flex;
          gap: 8px;
          margin-bottom: 12px;
          flex-wrap: wrap;

          .tag-icon {
            margin-right: 4px;
          }
        }

        .tutorial-title {
          font-size: 18px;
          font-weight: 600;
          color: #303133;
          margin: 0 0 12px 0;
          line-height: 1.4;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          line-clamp: 2;
          -webkit-box-orient: vertical;
          min-height: 50px;
        }

        .tutorial-meta {
          display: flex;
          justify-content: space-between;
          align-items: center;
          font-size: 13px;
          color: #909399;

          .duration {
            display: flex;
            align-items: center;
          }

          .views {
            display: flex;
            align-items: center;
          }

          .rating {
            color: #f7ba2a;
            font-weight: 500;
          }
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .tutorials-plaza-container {
    padding: 12px;

    .page-header {
      flex-direction: column;
      gap: 16px;
      align-items: stretch;

      .header-left {
        flex-direction: column;
        align-items: flex-start;
        gap: 12px;

        h2 {
          font-size: 20px;
        }

        .stats-info {
          width: 100%;
          overflow-x: auto;
        }
      }

      .header-actions {
        flex-direction: column;

        .el-button {
          width: 100%;
        }
      }
    }

    .filter-bar {
      flex-direction: column;
      gap: 12px;
      align-items: stretch;

      .search-box {
        width: 100%;

        :deep(.el-input) {
          width: 100% !important;
        }
      }

      .filter-group {
        flex-wrap: wrap;
        justify-content: center;

        :deep(.el-select) {
          width: calc(50% - 6px) !important;
        }
      }
    }

    .tutorial-grid {
      grid-template-columns: 1fr;
    }
  }
}
</style>
