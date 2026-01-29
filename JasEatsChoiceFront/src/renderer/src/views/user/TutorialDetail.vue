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

// 返回教程列表页面
const goBackToList = () => {
  router.push('/user/home/tutorials')
}

// 当前教程
const currentTutorial = ref(null)
const loading = ref(true)

// 从后端获取教程详情
const fetchTutorialDetail = async () => {
  loading.value = true
  try {
    const tutorialId = route.params.id
    const response = await api.get(`${API_CONFIG.tutorial.detail}${tutorialId}`)

    if (response.data) {
      currentTutorial.value = response.data
    } else {
      ElMessage.error('教程不存在')
      goBackToList()
    }
  } catch (error) {
    console.error('获取教程详情失败:', error)
    ElMessage.error('加载教程失败')
    // 失败时使用模拟数据作为备份
    loadMockData()
  } finally {
    loading.value = false
  }
}

// 模拟数据（作为备份）
const loadMockData = () => {
  const tutorialId = parseInt(route.params.id)
  const mockTutorials = [
    {
      id: 1,
      title: '青木瓜沙拉制作教程',
      type: 'video',
      duration: '5:30',
      views: '12.5k',
      view_count: 12500,
      cover_image: 'https://picsum.photos/id/109/800/600',
      source_type: 'ADMIN',
      is_official: true,
      author: '官方营养师',
      rating: 4.8,
      rating_count: 156,
      difficulty: 'BEGINNER',
      content: `### 制作步骤
1. 将青木瓜去皮，用刨刀切成细丝
2. 加入花生碎、红辣椒丝、蒜末
3. 调制料汁：鱼露2勺+柠檬汁3勺+糖1勺
4. 将料汁倒入木瓜丝，用手抓拌均匀
5. 最后加入西红柿片和生菜叶点缀即可

### 小贴士
- 选择未成熟的青木瓜，口感更脆爽
- 根据个人口味调整辣椒和鱼露用量`
    },
    {
      id: 2,
      title: '夏日低卡饮食指南',
      type: 'article',
      duration: '8分钟',
      views: '8.2k',
      view_count: 8200,
      cover_image: 'https://picsum.photos/id/215/800/600',
      source_type: 'ADMIN',
      is_official: true,
      author: '官方营养师',
      rating: 4.9,
      rating_count: 234,
      difficulty: 'BEGINNER',
      content: `## 夏日低卡饮食黄金法则

### 🌞 早餐篇
- 选择燕麦粥配水果（约300卡）
- 全麦面包+水煮蛋+无糖豆浆（约350卡）

### 🥗 午餐篇
- 鸡胸肉沙拉配油醋汁（约400卡）
- 荞麦面配时蔬（约450卡）

### 🌙 晚餐篇
- 清蒸鱼+时蔬（约350卡）
- 豆腐汤+小碗糙米饭（约400卡）

### 💡 关键提示
1. 多喝水，每天至少2L
2. 避免含糖饮料
3. 选择蒸煮等健康烹饪方式
4. 控制份量，七分饱即可`
    },
    {
      id: 3,
      title: '健康早餐搭配技巧',
      type: 'video',
      duration: '3:45',
      views: '9.7k',
      view_count: 9700,
      cover_image: 'https://picsum.photos/id/1045/800/600',
      source_type: 'ADMIN',
      is_official: true,
      author: '官方营养师',
      rating: 4.6,
      rating_count: 189,
      difficulty: 'INTERMEDIATE',
      content: `## 均衡早餐四要素

### 1. 优质碳水（拳头大小）
- 燕麦粥、全麦面包、糙米饭

### 2. 优质蛋白（手掌大小）
- 鸡蛋、牛奶、豆浆、瘦肉

### 3. 蔬菜水果（双手捧起大小）
- 香蕉、苹果、菠菜、西红柿

### 4. 健康脂肪（拇指大小）
- 坚果、牛油果、橄榄油

### ⏰ 时间优化
- 前一天晚上准备食材
- 利用微波炉快速加热
- 选择快手食谱`
    }
  ]
  currentTutorial.value = mockTutorials.find((t) => t.id === tutorialId)
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

// 格式化内容（支持Markdown）
const formatContent = (content) => {
  if (!content) return ''
  return content
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
    <el-card v-else-if="currentTutorial" class="tutorial-detail-card" shadow="hover">
      <!-- 封面图 -->
      <div class="tutorial-cover">
        <img :src="currentTutorial.cover_image || currentTutorial.coverImage" :alt="currentTutorial.title" />
        <div class="tutorial-type-overlay">
          <el-icon :class="currentTutorial.type === 'video' ? 'video-icon' : 'article-icon'">
            <component :is="currentTutorial.type === 'video' ? VideoCamera : Document" />
          </el-icon>
          <span>{{ currentTutorial.type === 'video' ? '视频教程' : '图文指南' }}</span>
        </div>
      </div>

      <div class="tutorial-info">
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
.tutorial-detail-container {
  padding: 20px 0;
  max-width: 900px;
  margin: 0 auto;

  .page-header {
    margin-bottom: 25px;
  }

  .loading-container {
    padding: 40px 0;
  }

  .tutorial-detail-card {
    padding: 0;
    overflow: hidden;
  }

  .tutorial-cover {
    position: relative;
    width: 100%;
    height: 400px;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .tutorial-type-overlay {
      position: absolute;
      top: 20px;
      right: 20px;
      background: rgba(255, 255, 255, 0.95);
      padding: 8px 12px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      gap: 5px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

      .video-icon {
        color: #ff6b6b;
        font-size: 20px;
      }

      .article-icon {
        color: #f7b267;
        font-size: 20px;
      }

      span {
        font-weight: 600;
        color: #303133;
        font-size: 14px;
      }
    }
  }

  .tutorial-info {
    padding: 30px;

    .tutorial-source-badges {
      display: flex;
      gap: 10px;
      margin-bottom: 20px;
      flex-wrap: wrap;

      .el-tag {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 6px 12px;
        border-radius: 12px;
        font-size: 13px;

        .el-icon {
          font-size: 16px;
        }

        .reviewed-badge {
          margin-left: 4px;
          padding-left: 6px;
          border-left: 1px solid currentColor;
          font-size: 12px;
        }
      }
    }

    .tutorial-title {
      font-size: 32px;
      font-weight: bold;
      color: #303133;
      margin-bottom: 20px;
      line-height: 1.4;
    }

    .tutorial-meta {
      color: #606266;
      font-size: 15px;
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
          font-size: 18px;
        }
      }

      .rating-count {
        color: #909399;
        font-size: 13px;
        margin-left: 4px;
      }
    }

    .tutorial-content {
      h3 {
        font-size: 24px;
        font-weight: bold;
        color: #303133;
        margin-bottom: 20px;
        padding-bottom: 10px;
        border-bottom: 2px solid #ff6b6b;
      }

      .content-text {
        font-size: 16px;
        line-height: 2;
        color: #606266;

        :deep(h1),
        :deep(h2),
        :deep(h3) {
          margin-top: 20px;
          margin-bottom: 12px;
          color: #303133;
        }

        :deep(p) {
          margin-bottom: 15px;
        }

        :deep(ul),
        :deep(ol) {
          padding-left: 25px;
          margin-bottom: 15px;

          li {
            margin-bottom: 8px;
          }
        }

        :deep(strong) {
          color: #ff6b6b;
          font-weight: 600;
        }

        :deep(code) {
          background: #f5f5f5;
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
      color: #606266;
      margin-bottom: 25px;
      font-size: 24px;
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
      padding: 20px;

      .tutorial-title {
        font-size: 24px;
      }

      .tutorial-meta {
        font-size: 13px;
        gap: 15px;
      }

      .tutorial-content h3 {
        font-size: 20px;
      }
    }
  }
}
</style>
