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
import { ref, onMounted } from 'vue'
import { Refresh, CopyDocument, Loading, ChatLineSquare } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'
import { buildUrl, MERCHANT_AI_API } from '@/api'

const props = defineProps({
  merchantId: {
    type: String,
    required: true
  }
})

// 待回复评价列表
const pendingReviews = ref([])

const selectedReview = ref(null)
const suggestions = ref([])
const isLoading = ref(false)
const selectedSuggestion = ref(-1)
const editingReply = ref(null)

const styleTags = ['感谢好评', '处理问题', '邀请再访']

const getStyleTag = (index) => styleTags[index] || '其他建议'

/**
 * 加载待回复评价列表
 */
const loadPendingReviews = async () => {
  try {
    const url = buildUrl(MERCHANT_AI_API.REVIEW_PENDING, { merchantId: props.merchantId })
    const response = await api.get(url)
    pendingReviews.value = response.data || []
  } catch (error) {
    console.error('加载评价列表失败:', error)
    ElMessage.error('加载评价列表失败')
  }
}

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

  try {
    const response = await api.post(MERCHANT_AI_API.REVIEW_GENERATE_REPLY, {
      reviewId: review.id,
      reviewContent: review.content,
      rating: review.rating,
      userName: review.userName
    })
    suggestions.value = response.data || []
  } catch (error) {
    console.error('生成回复建议失败:', error)
    ElMessage.error('生成回复建议失败')
    suggestions.value = []
  } finally {
    isLoading.value = false
  }
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
const submitReply = async () => {
  if (!editingReply.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  try {
    await api.post(MERCHANT_AI_API.REVIEW_SUBMIT_REPLY, {
      reviewId: selectedReview.value.id,
      merchantId: props.merchantId,
      content: editingReply.value
    })

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
  } catch (error) {
    console.error('提交回复失败:', error)
    ElMessage.error('提交回复失败')
  }
}

onMounted(() => {
  loadPendingReviews()
})
</script>

<style scoped lang="less">
@import '../../../../assets/css/nordic-theme.less';
@import '../../../../assets/css/merchant-theme.less';

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
  background: @merchant-surface;
  border-radius: 12px;
  overflow: hidden;

  .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    border-bottom: 1px solid @merchant-border;

    h3 {
      margin: 0;
      font-size: 16px;
      color: @merchant-secondary;
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
  background: @merchant-surface-alt;
  border: 2px solid transparent;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: @merchant-secondary-light;
    border-color: @merchant-border;
  }

  &.active {
    background: @merchant-secondary-light;
    border-color: @merchant-secondary;
  }

  .review-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    .user {
      font-weight: 600;
      color: @merchant-text;
    }
  }

  .review-content {
    font-size: 13px;
    color: @merchant-text-sec;
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
      color: @merchant-text-muted;
    }
  }
}

.suggestions-panel {
  display: flex;
  flex-direction: column;
  background: @merchant-surface;
  border-radius: 12px;
  overflow: hidden;

  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    border-bottom: 1px solid @merchant-border;

    h3 {
      margin: 0;
      font-size: 16px;
      color: @merchant-secondary;
    }
  }

  .review-detail {
    padding: 16px;
    background: @merchant-secondary-light;
    border-bottom: 1px solid @merchant-border;

    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .user {
        font-weight: 600;
        color: @merchant-text;
      }
    }

    .detail-content {
      font-size: 14px;
      color: @merchant-text;
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
  background: @merchant-surface-alt;
  border: 2px solid transparent;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: @merchant-secondary-light;
    border-color: @merchant-border;
  }

  &.selected {
    background: @merchant-secondary-light;
    border-color: @merchant-primary;
  }

  .style-tag {
    display: inline-block;
    padding: 4px 12px;
    background: @merchant-warning-light;
    color: @merchant-warning;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
    margin-bottom: 8px;
  }

  .text {
    font-size: 14px;
    color: @merchant-text;
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
  color: @merchant-secondary;

  p {
    margin-top: 12px;
    color: @merchant-text-sec;
  }
}

.edit-area {
  padding: 16px;
  background: @merchant-secondary-light;
  border-top: 1px solid @merchant-border;

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
  color: @merchant-text-muted;

  .el-icon {
    margin-bottom: 12px;
    color: @merchant-error;
  }

  p {
    margin: 4px 0;
  }

  .hint {
    font-size: 13px;
    color: @merchant-text-muted;
  }
}
</style>
