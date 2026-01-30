<script setup>
import { ref, onMounted, computed } from 'vue'
import { VideoCamera, Document, Search, Plus, User, Grid, Refresh, Shop, MagicStick, CircleCheck, View, Star, ArrowLeft } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

// 检测跳转来源：从侧边栏跳转时带有 fromSidebar=true 参数
const fromSidebar = computed(() => route.query.fromSidebar === 'true')

// 是否显示返回按钮
const showBackButton = computed(() => !fromSidebar.value)

// 返回上一页
const goBack = () => {
  router.back()
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
        <div class="title-section">
          <h2>教程广场</h2>
          <p class="subtitle">探索美食制作技巧，分享你的独家秘方</p>
        </div>
      </div>
      <div class="header-right">
        <!-- 返回按钮：只在非侧边栏跳转时显示 -->
        <el-button
          v-if="showBackButton"
          @click="goBack"
          class="back-btn"
          :icon="ArrowLeft"
        >
          返回
        </el-button>
        <el-button @click="router.push('/user/home/tutorials/my')" class="action-btn">
          <el-icon><User /></el-icon> 我的教程
        </el-button>
        <el-button type="primary" @click="router.push('/user/home/tutorials/publish')" class="publish-btn">
          <el-icon><Plus /></el-icon> 发布教程
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card total">
        <div class="stat-icon">
          <el-icon :size="32"><Grid /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">全部教程</div>
        </div>
      </div>

      <div class="stat-card admin">
        <div class="stat-icon">
          <el-icon :size="32"><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.admin }}</div>
          <div class="stat-label">官方认证</div>
        </div>
      </div>

      <div class="stat-card merchant">
        <div class="stat-icon">
          <el-icon :size="32"><Shop /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.merchant }}</div>
          <div class="stat-label">商家贡献</div>
        </div>
      </div>

      <div class="stat-card user">
        <div class="stat-icon">
          <el-icon :size="32"><User /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.user }}</div>
          <div class="stat-label">用户贡献</div>
        </div>
      </div>

      <div class="stat-card ai">
        <div class="stat-icon">
          <el-icon :size="32"><MagicStick /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.ai }}</div>
          <div class="stat-label">AI生成</div>
        </div>
      </div>
    </div>

    <!-- 搜索和筛选栏 -->
    <div class="filter-bar">
      <div class="filter-left">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索教程标题或内容..."
          :prefix-icon="Search"
          clearable
          class="search-input"
        />
      </div>

      <div class="filter-right">
        <el-select v-model="selectedType" placeholder="教程类型" class="filter-select">
          <template #prefix>
            <el-icon><Grid /></el-icon>
          </template>
          <el-option label="全部类型" value="all">
            <span class="option-label">
              <el-icon><Grid /></el-icon>
              全部类型
            </span>
          </el-option>
          <el-option label="视频教程" value="video">
            <span class="option-label">
              <el-icon><VideoCamera /></el-icon>
              视频教程
            </span>
          </el-option>
          <el-option label="图文指南" value="article">
            <span class="option-label">
              <el-icon><Document /></el-icon>
              图文指南
            </span>
          </el-option>
        </el-select>

        <el-select v-model="selectedSource" placeholder="来源" class="filter-select">
          <template #prefix>
            <el-icon><Shop /></el-icon>
          </template>
          <el-option label="全部来源" value="all">
            <span class="option-label">
              <el-icon><Grid /></el-icon>
              全部来源
            </span>
          </el-option>
          <el-option label="官方认证" value="ADMIN">
            <span class="option-label">
              <el-icon><CircleCheck /></el-icon>
              官方认证
            </span>
          </el-option>
          <el-option label="商家贡献" value="MERCHANT">
            <span class="option-label">
              <el-icon><Shop /></el-icon>
              商家贡献
            </span>
          </el-option>
          <el-option label="用户贡献" value="USER">
            <span class="option-label">
              <el-icon><User /></el-icon>
              用户贡献
            </span>
          </el-option>
          <el-option label="AI生成" value="AI_GENERATED">
            <span class="option-label">
              <el-icon><MagicStick /></el-icon>
              AI生成
            </span>
          </el-option>
        </el-select>

        <el-select v-model="selectedDifficulty" placeholder="难度" class="filter-select">
          <template #prefix>
            <el-icon><Star /></el-icon>
          </template>
          <el-option label="全部难度" value="all">
            <span class="option-label">
              <el-icon><Grid /></el-icon>
              全部难度
            </span>
          </el-option>
          <el-option label="初级" value="BEGINNER">
            <span class="option-label">
              <span class="difficulty-icon">🌱</span>
              初级
            </span>
          </el-option>
          <el-option label="中级" value="INTERMEDIATE">
            <span class="option-label">
              <span class="difficulty-icon">🌿</span>
              中级
            </span>
          </el-option>
          <el-option label="高级" value="ADVANCED">
            <span class="option-label">
              <span class="difficulty-icon">🌳</span>
              高级
            </span>
          </el-option>
        </el-select>

        <el-button @click="resetFilters" :icon="Refresh" class="reset-btn">重置</el-button>
      </div>
    </div>

    <!-- 活跃筛选信息 -->
    <div v-if="selectedType !== 'all' || selectedSource !== 'all' || selectedDifficulty !== 'all' || searchKeyword" class="filter-info">
      <span class="filter-info-text">
        当前筛选:
        <el-tag v-if="selectedType !== 'all'" size="small" closable @close="selectedType = 'all'" class="filter-tag">
          {{ selectedType === 'video' ? '视频教程' : '图文指南' }}
        </el-tag>
        <el-tag v-if="selectedSource !== 'all'" size="small" closable @close="selectedSource = 'all'" class="filter-tag">
          {{ selectedSource === 'ADMIN' ? '官方认证' : selectedSource === 'MERCHANT' ? '商家贡献' : selectedSource === 'USER' ? '用户贡献' : 'AI生成' }}
        </el-tag>
        <el-tag v-if="selectedDifficulty !== 'all'" size="small" closable @close="selectedDifficulty = 'all'" class="filter-tag">
          {{ getDifficultyName(selectedDifficulty) }}
        </el-tag>
        <el-tag v-if="searchKeyword" size="small" closable @close="searchKeyword = ''" class="filter-tag">
          搜索: {{ searchKeyword }}
        </el-tag>
      </span>
      <el-button text type="primary" @click="resetFilters" :icon="Refresh">清除全部</el-button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="6" animated />
    </div>

    <!-- 空状态 -->
    <div v-else-if="filteredTutorials.length === 0" class="empty-container">
      <el-empty description="没有找到符合条件的教程" :image-size="180">
        <template #image>
          <div class="empty-icon">
            <el-icon :size="100" color="#ddd"><Document /></el-icon>
          </div>
        </template>
        <el-button type="primary" @click="resetFilters" :icon="Refresh">清除筛选条件</el-button>
      </el-empty>
    </div>

    <!-- 教程列表 -->
    <transition-group name="tutorial-fade" v-else tag="div" class="tutorial-grid">
      <el-card
        v-for="tutorial in filteredTutorials"
        :key="tutorial.id"
        class="tutorial-card"
        shadow="hover"
        @click="router.push(`/user/home/tutorials/${tutorial.id}`)"
      >
        <div class="tutorial-header">
          <div class="type-badge" :class="tutorial.type">
            <el-icon :size="24">
              <component :is="tutorial.type === 'video' ? VideoCamera : Document" />
            </el-icon>
            <span>{{ tutorial.type === 'video' ? '视频' : '图文' }}</span>
          </div>
          <div class="rating-badge" v-if="tutorial.rating">
            <span class="star">⭐</span>
            <span>{{ tutorial.rating }}</span>
          </div>
        </div>

        <div class="tutorial-content">
          <!-- 来源标签 -->
          <div class="source-tags">
            <el-tag
              :type="getSourceTag(tutorial).type"
              :effect="getSourceTag(tutorial).effect"
              size="small"
              class="source-tag"
            >
              <span class="tag-icon">{{ getSourceTag(tutorial).icon }}</span>
              {{ getSourceTag(tutorial).text }}
            </el-tag>
            <el-tag v-if="tutorial.difficulty" type="info" size="small" effect="plain" class="difficulty-tag">
              {{ getDifficultyName(tutorial.difficulty) }}
            </el-tag>
          </div>

          <h3 class="tutorial-title">{{ tutorial.title }}</h3>

          <div class="tutorial-meta">
            <div class="meta-left">
              <span class="duration">
                <el-icon><VideoCamera /></el-icon>
                {{ tutorial.duration }}
              </span>
              <span class="views">
                <el-icon><View /></el-icon>
                {{ tutorial.view_count?.toLocaleString() || 0 }}
              </span>
            </div>
          </div>
        </div>

        <!-- 卡片装饰 -->
        <div class="card-decoration" :class="getSourceTag(tutorial).type"></div>
      </el-card>
    </transition-group>
  </div>
</template>

<style scoped lang="less">
.tutorials-plaza-container {
  padding: 24px;
  background: linear-gradient(180deg, #f8f9fa 0%, #ffffff 100%);
  min-height: calc(100vh - 60px);

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    padding: 24px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 16px;
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.25);
    color: white;

    .header-left {
      .title-section {
        h2 {
          margin: 0 0 4px 0;
          font-size: 28px;
          font-weight: bold;
          color: white;
        }

        .subtitle {
          margin: 0;
          font-size: 14px;
          opacity: 0.9;
          color: rgba(255, 255, 255, 0.85);
        }
      }
    }

    .header-right {
      display: flex;
      flex-direction: column;
      gap: 12px;
      align-items: flex-end;

      .back-btn {
        background: rgba(255, 255, 255, 0.15);
        border-color: rgba(255, 255, 255, 0.25);
        color: white;
        backdrop-filter: blur(10px);
        transition: all 0.3s;
        padding: 8px 20px;
        font-size: 14px;

        &:hover {
          background: rgba(255, 255, 255, 0.25);
          border-color: rgba(255, 255, 255, 0.4);
          transform: translateX(-2px);
        }
      }

      .action-btn {
        background: rgba(255, 255, 255, 0.2);
        border-color: rgba(255, 255, 255, 0.3);
        color: white;
        backdrop-filter: blur(10px);
        transition: all 0.3s;

        &:hover {
          background: rgba(255, 255, 255, 0.3);
          border-color: rgba(255, 255, 255, 0.5);
          transform: translateY(-2px);
        }
      }

      .publish-btn {
        background: white;
        border-color: white;
        color: #667eea;
        font-weight: 600;
        transition: all 0.3s;

        &:hover {
          background: #f0f0f0;
          border-color: #f0f0f0;
          transform: translateY(-2px);
          box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
        }
      }
    }
  }

  .stats-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
    gap: 16px;
    margin-bottom: 24px;

    .stat-card {
      background: white;
      border-radius: 12px;
      padding: 20px;
      display: flex;
      align-items: center;
      gap: 16px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
      transition: all 0.3s;
      position: relative;
      overflow: hidden;

      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        width: 4px;
        height: 100%;
      }

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      }

      &.total::before {
        background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
      }

      &.admin::before {
        background: linear-gradient(180deg, #f093fb 0%, #f5576c 100%);
      }

      &.merchant::before {
        background: linear-gradient(180deg, #ffecd2 0%, #fcb69f 100%);
      }

      &.user::before {
        background: linear-gradient(180deg, #a8edea 0%, #fed6e3 100%);
      }

      &.ai::before {
        background: linear-gradient(180deg, #d299c2 0%, #fef9d7 100%);
      }

      .stat-icon {
        width: 56px;
        height: 56px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;

        .total & {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
        }

        .admin & {
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          color: white;
        }

        .merchant & {
          background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
          color: #ff6b6b;
        }

        .user & {
          background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
          color: #4ecdc4;
        }

        .ai & {
          background: linear-gradient(135deg, #d299c2 0%, #fef9d7 100%);
          color: #9b59b6;
        }
      }

      .stat-info {
        .stat-value {
          font-size: 28px;
          font-weight: bold;
          color: #303133;
          margin-bottom: 4px;
          line-height: 1;
        }

        .stat-label {
          font-size: 13px;
          color: #909399;
        }
      }
    }
  }

  .filter-bar {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    margin-bottom: 16px;
    padding: 20px;
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

    .filter-left {
      flex: 1;
      min-width: 280px;

      .search-input {
        width: 100%;

        :deep(.el-input__wrapper) {
          border-radius: 20px;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
          transition: all 0.3s;

          &:hover {
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
          }

          &.is-focus {
            box-shadow: 0 4px 16px rgba(102, 126, 234, 0.2);
          }
        }
      }
    }

    .filter-right {
      display: flex;
      gap: 12px;
      align-items: center;
      flex-wrap: wrap;

      .filter-select {
        width: 140px;

        :deep(.el-input__wrapper) {
          border-radius: 20px;
        }

        .option-label {
          display: flex;
          align-items: center;
          gap: 8px;

          .difficulty-icon {
            font-size: 16px;
          }
        }
      }

      .reset-btn {
        border-radius: 20px;
        transition: all 0.3s;

        &:hover {
          transform: translateY(-2px);
        }
      }
    }
  }

  .filter-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 20px;
    margin-bottom: 16px;
    background: linear-gradient(135deg, #e3f2fd 0%, #f3e5f5 100%);
    border-radius: 8px;
    animation: slideDown 0.3s ease;

    .filter-info-text {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
      font-size: 14px;
      color: #333;

      .filter-tag {
        margin-left: 4px;
        animation: scaleIn 0.3s ease;
      }
    }
  }

  .loading-container {
    padding: 40px;
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  }

  .empty-container {
    padding: 80px 20px;
    background: white;
    border-radius: 12px;
    text-align: center;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

    .empty-icon {
      margin-bottom: 20px;
      animation: float 3s ease-in-out infinite;
    }
  }

  .tutorial-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;

    .tutorial-card {
      cursor: pointer;
      transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
      border: none;
      border-radius: 16px;
      overflow: visible;
      position: relative;

      &:hover {
        transform: translateY(-8px) scale(1.02);
        box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
      }

      :deep(.el-card__body) {
        padding: 24px;
      }

      .tutorial-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;
        padding-bottom: 12px;
        border-bottom: 2px solid #f5f5f5;

        .type-badge {
          display: flex;
          align-items: center;
          gap: 6px;
          padding: 8px 16px;
          border-radius: 20px;
          font-size: 13px;
          font-weight: 600;

          &.video {
            background: linear-gradient(135deg, #ff6b6b 0%, #ff8e8e 100%);
            color: white;
          }

          &.article {
            background: linear-gradient(135deg, #f7b267 0%, #ffcc80 100%);
            color: white;
          }
        }

        .rating-badge {
          display: flex;
          align-items: center;
          gap: 4px;
          padding: 6px 12px;
          background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
          border-radius: 20px;
          font-size: 14px;
          font-weight: 600;
          color: #ff6b6b;

          .star {
            font-size: 16px;
          }
        }
      }

      .tutorial-content {
        .source-tags {
          display: flex;
          gap: 8px;
          margin-bottom: 12px;
          flex-wrap: wrap;

          .source-tag {
            border-radius: 20px;
            font-weight: 500;
            animation: scaleIn 0.3s ease;

            .tag-icon {
              margin-right: 4px;
            }
          }

          .difficulty-tag {
            border-radius: 20px;
            font-weight: 500;
          }
        }

        .tutorial-title {
          font-size: 18px;
          font-weight: 700;
          color: #303133;
          margin: 0 0 16px 0;
          line-height: 1.5;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          line-clamp: 2;
          -webkit-box-orient: vertical;
          min-height: 54px;
        }

        .tutorial-meta {
          .meta-left {
            display: flex;
            gap: 16px;
            font-size: 13px;
            color: #909399;

            .duration,
            .views {
              display: flex;
              align-items: center;
              gap: 4px;
            }
          }
        }
      }

      .card-decoration {
        position: absolute;
        top: -50%;
        right: -50%;
        width: 200%;
        height: 200%;
        background: radial-gradient(circle, rgba(102, 126, 234, 0.1) 0%, transparent 70%);
        pointer-events: none;
        opacity: 0;
        transition: opacity 0.3s;

        &.danger {
          background: radial-gradient(circle, rgba(245, 87, 108, 0.1) 0%, transparent 70%);
        }

        &.warning {
          background: radial-gradient(circle, rgba(252, 182, 159, 0.1) 0%, transparent 70%);
        }

        &.primary {
          background: radial-gradient(circle, rgba(78, 205, 196, 0.1) 0%, transparent 70%);
        }

        &.success {
          background: radial-gradient(circle, rgba(155, 89, 182, 0.1) 0%, transparent 70%);
        }
      }

      &:hover .card-decoration {
        opacity: 1;
      }
    }
  }
}

// 动画
@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.tutorial-fade-enter-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.tutorial-fade-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.tutorial-fade-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.9);
}

.tutorial-fade-leave-to {
  opacity: 0;
  transform: translateY(-20px) scale(0.9);
}

// 响应式
@media (max-width: 1200px) {
  .tutorials-plaza-container {
    .stats-cards {
      grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
    }

    .tutorial-grid {
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    }
  }
}

@media (max-width: 768px) {
  .tutorials-plaza-container {
    padding: 16px;

    .page-header {
      flex-direction: column;
      gap: 16px;
      padding: 20px;

      .header-left {
        .title-section {
          h2 {
            font-size: 22px;
          }

          .subtitle {
            font-size: 13px;
          }
        }
      }

      .header-right {
        width: 100%;
        align-items: stretch;
        flex-direction: row;
        flex-wrap: wrap;

        .back-btn,
        .action-btn,
        .publish-btn {
          flex: 1;
          min-width: 100px;
        }
      }
    }

    .stats-cards {
      grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
      gap: 12px;

      .stat-card {
        padding: 16px;

        .stat-icon {
          width: 48px;
          height: 48px;
        }

        .stat-info {
          .stat-value {
            font-size: 24px;
          }

          .stat-label {
            font-size: 12px;
          }
        }
      }
    }

    .filter-bar {
      flex-direction: column;
      gap: 12px;
      padding: 16px;

      .filter-left {
        min-width: 100%;
      }

      .filter-right {
        justify-content: center;

        .filter-select {
          flex: 1;
          min-width: 120px;
        }
      }
    }

    .filter-info {
      flex-direction: column;
      gap: 12px;
      align-items: stretch;

      .filter-info-text {
        justify-content: center;
      }
    }

    .tutorial-grid {
      grid-template-columns: 1fr;
    }
  }
}
</style>
