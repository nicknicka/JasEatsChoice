<template>
  <div class="reply-generator">
    <!-- 评价列表 -->
    <div class="review-list">
      <div class="list-header">
        <h3>待回复评价</h3>
        <el-tag type="warning">{{ pendingReviews.length }} 条待处理</el-tag>
      </div>
      <div class="list-content">
        <div
          v-for="review in pendingReviews"
          :key="review.id"
          class="review-card"
          :class="{ active: selectedReview?.id === review.id }"
          @click="selectReview(review)"
        >
          <div class="review-header">
            <span class="user">{{ review.userName }}</span>
            <el-rate :model-value="review.rating" disabled size="small" />
          </div>
          <div class="review-content">{{ review.content }}</div>
          <div class="review-footer">
            <span class="time">{{ review.time }}</span>
            <el-tag v-if="!review.replied" type="danger" size="small">未回复</el-tag>
            <el-tag v-else type="success" size="small">已回复</el-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- AI回复建议 -->
    <div class="suggestions-panel">
      <template v-if="selectedReview">
        <div class="panel-header">
          <h3>AI回复建议</h3>
          <el-button size="small" @click="refreshSuggestions" :loading="isLoading">
            <el-icon><Refresh /></el-icon>
            重新生成
          </el-button>
        </div>

        <!-- 评价详情 -->
        <div class="review-detail">
          <div class="detail-header">
            <span class="user">{{ selectedReview.userName }}</span>
            <el-rate :model-value="selectedReview.rating" disabled size="small" />
          </div>
          <div class="detail-content">{{ selectedReview.content }}</div>
        </div>

        <!-- 建议列表 -->
        <div class="suggestions" v-if="suggestions.length">
          <div
            v-for="(suggestion, index) in suggestions"
            :key="index"
            class="suggestion-item"
            :class="{ selected: selectedSuggestion === index }"
            @click="selectSuggestion(index)"
          >
            <div class="style-tag">{{ getStyleTag(index) }}</div>
            <div class="text">{{ suggestion }}</div>
            <div class="actions">
              <el-button size="small" @click.stop="copySuggestion(suggestion)">
                <el-icon><CopyDocument /></el-icon>
              </el-button>
              <el-button size="small" type="primary" @click.stop="useSuggestion(suggestion)">
                采用
              </el-button>
            </div>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-else-if="isLoading" class="loading-state">
          <el-icon class="is-loading" :size="32"><Loading /></el-icon>
          <p>正在生成回复建议...</p>
        </div>

        <!-- 编辑区域 -->
        <div class="edit-area" v-if="editingReply">
          <el-input
            v-model="editingReply"
            type="textarea"
            :rows="4"
            placeholder="编辑回复内容..."
          />
          <div class="edit-actions">
            <el-button @click="editingReply = null">取消</el-button>
            <el-button type="primary" @click="submitReply">提交回复</el-button>
          </div>
        </div>
      </template>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <el-icon :size="48"><ChatLineSquare /></el-icon>
        <p>请从左侧选择一条评价</p>
        <p class="hint">AI将为您生成专业的回复建议</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Refresh, CopyDocument, Loading, ChatLineSquare } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// 待回复评价列表
const pendingReviews = ref([
  { id: 1, userName: '张先生', rating: 5, content: '菜品非常新鲜，服务态度也很好！下次还会再来！', time: '2024-01-15 12:30', replied: false },
  { id: 2, userName: '李女士', rating: 3, content: '味道还可以，但是上菜有点慢，等了快半小时', time: '2024-01-15 11:20', replied: false },
  { id: 3, userName: '王先生', rating: 4, content: '整体不错，就是分量稍微有点少，希望能改进', time: '2024-01-14 19:45', replied: false },
  { id: 4, userName: '赵女士', rating: 2, content: '菜品味道一般，价格偏贵，性价比不高', time: '2024-01-14 18:30', replied: true }
])

const selectedReview = ref(null)
const suggestions = ref([])
const isLoading = ref(false)
const selectedSuggestion = ref(-1)
const editingReply = ref(null)

const styleTags = ['感谢好评', '处理问题', '邀请再访']

const getStyleTag = (index) => styleTags[index] || '其他建议'

/**
 * 选择评价
 */
const selectReview = async (review) => {
  selectedReview.value = review
  suggestions.value = []
  selectedSuggestion.value = -1
  editingReply.value = null
  await generateSuggestions(review)
}

/**
 * 生成回复建议
 */
const generateSuggestions = async (review) => {
  isLoading.value = true

  // 模拟AI生成
  await new Promise(resolve => setTimeout(resolve, 1500))

  // 根据评分生成不同风格的回复
  if (review.rating >= 4) {
    suggestions.value = [
      `感谢您的好评！您的满意是我们最大的动力，我们会继续努力为您提供更优质的菜品和服务，期待您的再次光临！`,
      `非常感谢您的认可！我们一直坚持选用新鲜食材，用心做好每一道菜。您的支持是我们前进的动力，欢迎下次再来！`,
      `谢谢您的五星好评！很高兴您喜欢我们的菜品和服务。我们会继续保持，也欢迎您向朋友推荐我们哦！`
    ]
  } else if (review.rating === 3) {
    suggestions.value = [
      `感谢您的反馈！对于您提到的问题，我们非常重视。我们会加强培训，提升服务效率，希望能给您带来更好的体验。`,
      `非常抱歉给您带来了不好的体验！您提到的问题我们已经记录，会立即改进。期待您再次光临，让我们有机会为您提供更好的服务。`,
      `感谢您的宝贵意见！我们会认真对待每一个问题，努力改进。希望下次能为您提供满意的用餐体验！`
    ]
  } else {
    suggestions.value = [
      `非常抱歉给您带来了不好的体验！您提到的问题我们非常重视，会立即进行整改。希望能有机会再次为您服务，让您看到我们的改变。`,
      `感谢您的反馈，我们深感抱歉！请您联系我们的客服，我们愿意为您提供补偿方案。我们会认真改进，争取下次让您满意。`,
      `非常抱歉让您失望了！您的意见对我们非常重要，我们会认真分析问题并改进。期待您给我们一个弥补的机会！`
    ]
  }

  isLoading.value = false
}

/**
 * 刷新建议
 */
const refreshSuggestions = () => {
  if (selectedReview.value) {
    suggestions.value = []
    generateSuggestions(selectedReview.value)
  }
}

/**
 * 选择建议
 */
const selectSuggestion = (index) => {
  selectedSuggestion.value = index
}

/**
 * 复制建议
 */
const copySuggestion = (text) => {
  navigator.clipboard.writeText(text)
  ElMessage.success('已复制到剪贴板')
}

/**
 * 采用建议
 */
const useSuggestion = (text) => {
  editingReply.value = text
}

/**
 * 提交回复
 */
const submitReply = () => {
  if (!editingReply.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  // 模拟提交
  ElMessage.success('回复已提交')

  // 更新状态
  const index = pendingReviews.value.findIndex(r => r.id === selectedReview.value.id)
  if (index !== -1) {
    pendingReviews.value[index].replied = true
  }

  // 重置
  editingReply.value = null
  selectedReview.value = null
  suggestions.value = []
}
</script>

<style scoped lang="less">
.reply-generator {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 20px;
  height: 100%;
  padding: 16px;
}

.review-list {
  display: flex;
  flex-direction: column;
  background: #FFF;
  border-radius: 12px;
  overflow: hidden;

  .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    border-bottom: 1px solid #FECACA;

    h3 {
      margin: 0;
      font-size: 16px;
      color: #DC2626;
    }
  }

  .list-content {
    flex: 1;
    overflow-y: auto;
    padding: 12px;
  }
}

.review-card {
  padding: 12px;
  background: #FAFAFA;
  border: 2px solid transparent;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: #FEF2F2;
    border-color: #FECACA;
  }

  &.active {
    background: #FEF2F2;
    border-color: #DC2626;
  }

  .review-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    .user {
      font-weight: 600;
      color: #374151;
    }
  }

  .review-content {
    font-size: 13px;
    color: #6B7280;
    line-height: 1.5;
    margin-bottom: 8px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .review-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .time {
      font-size: 12px;
      color: #9CA3AF;
    }
  }
}

.suggestions-panel {
  display: flex;
  flex-direction: column;
  background: #FFF;
  border-radius: 12px;
  overflow: hidden;

  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    border-bottom: 1px solid #FECACA;

    h3 {
      margin: 0;
      font-size: 16px;
      color: #DC2626;
    }
  }

  .review-detail {
    padding: 16px;
    background: #FEF2F2;
    border-bottom: 1px solid #FECACA;

    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .user {
        font-weight: 600;
        color: #374151;
      }
    }

    .detail-content {
      font-size: 14px;
      color: #374151;
      line-height: 1.6;
    }
  }
}

.suggestions {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.suggestion-item {
  background: #FAFAFA;
  border: 2px solid transparent;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: #FEF2F2;
    border-color: #FECACA;
  }

  &.selected {
    background: #FEF2F2;
    border-color: #CA8A04;
  }

  .style-tag {
    display: inline-block;
    padding: 4px 12px;
    background: #FEF3C7;
    color: #CA8A04;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
    margin-bottom: 8px;
  }

  .text {
    font-size: 14px;
    color: #374151;
    line-height: 1.6;
    margin-bottom: 12px;
  }

  .actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }
}

.loading-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #DC2626;

  p {
    margin-top: 12px;
    color: #6B7280;
  }
}

.edit-area {
  padding: 16px;
  background: #FEF2F2;
  border-top: 1px solid #FECACA;

  .edit-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 12px;
  }
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #9CA3AF;

  .el-icon {
    margin-bottom: 12px;
    color: #FECACA;
  }

  p {
    margin: 4px 0;
  }

  .hint {
    font-size: 13px;
    color: #D1D5DB;
  }
}
</style>
