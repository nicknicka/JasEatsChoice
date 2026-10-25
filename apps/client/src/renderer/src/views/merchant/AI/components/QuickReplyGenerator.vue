<template>
  <div class="reply-generator">
    <!-- 评价列表 -->
    <div class="review-list">
      <div class="list-header">
        <div class="list-title-group">
          <h3>待回复评价</h3>
          <span class="pending-count">{{ pendingReviews.length }}</span>
        </div>
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
            <div class="user-info">
              <span class="user-avatar">{{ review.userName.charAt(0) }}</span>
              <span class="user-name">{{ review.userName }}</span>
            </div>
            <el-rate :model-value="review.rating" disabled size="small" />
          </div>
          <div class="review-content">{{ review.content }}</div>
          <div class="review-footer">
            <span class="time">{{ review.time }}</span>
            <span class="reply-status" :class="review.replied ? 'replied' : 'pending'">
              {{ review.replied ? '已回复' : '待回复' }}
            </span>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="pendingReviews.length === 0" class="list-empty">
          <span class="empty-icon">✓</span>
          <p>所有评价已回复</p>
        </div>
      </div>
    </div>

    <!-- AI回复建议 -->
    <div class="suggestions-panel">
      <template v-if="selectedReview">
        <div class="panel-header">
          <div class="panel-title-group">
            <span class="ai-badge">AI</span>
            <h3>智能回复建议</h3>
          </div>
          <button class="refresh-btn" @click="refreshSuggestions" :class="{ spinning: isLoading }">
            <el-icon :size="14"><Refresh /></el-icon>
            <span>重新生成</span>
          </button>
        </div>

        <!-- 评价详情 -->
        <div class="review-detail">
          <div class="detail-header">
            <span class="user-name">{{ selectedReview.userName }}</span>
            <el-rate :model-value="selectedReview.rating" disabled size="small" />
          </div>
          <div class="detail-content">{{ selectedReview.content }}</div>
        </div>

        <!-- 建议列表 -->
        <div class="suggestions" v-if="suggestions.length">
          <div
            v-for="(suggestion, index) in suggestions"
            :key="index"
            class="suggestion-card"
            :class="{ selected: selectedSuggestion === index }"
            @click="selectSuggestion(index)"
          >
            <div class="suggestion-header">
              <span class="style-tag" :class="`style-${index}`">{{ getStyleTag(index) }}</span>
              <div class="suggestion-actions">
                <button class="icon-btn" @click.stop="copySuggestion(suggestion)" title="复制">
                  <el-icon :size="14"><CopyDocument /></el-icon>
                </button>
              </div>
            </div>
            <div class="suggestion-text">{{ suggestion }}</div>
            <button class="use-btn" @click.stop="useSuggestion(suggestion)">
              <el-icon :size="14"><Check /></el-icon>
              <span>采用此回复</span>
            </button>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-else-if="isLoading" class="loading-state">
          <div class="loading-dots">
            <span></span><span></span><span></span>
          </div>
          <p>正在生成回复建议...</p>
        </div>

        <!-- 编辑区域 -->
        <Transition name="edit-slide">
          <div class="edit-area" v-if="editingReply">
            <div class="edit-header">
              <span>编辑回复</span>
              <button class="icon-btn" @click="editingReply = null">
                <el-icon :size="16"><Close /></el-icon>
              </button>
            </div>
            <el-input
              v-model="editingReply"
              type="textarea"
              :rows="4"
              placeholder="编辑回复内容..."
            />
            <div class="edit-actions">
              <button class="cancel-btn" @click="editingReply = null">取消</button>
              <button class="submit-btn" @click="submitReply">
                <el-icon :size="14"><Position /></el-icon>
                <span>提交回复</span>
              </button>
            </div>
          </div>
        </Transition>
      </template>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-icon-wrap">
          <el-icon :size="36"><ChatLineSquare /></el-icon>
        </div>
        <p class="empty-title">选择一条评价</p>
        <p class="empty-hint">AI将为您生成专业的回复建议</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Refresh, CopyDocument, Check, ChatLineSquare, Position, Close } from '@element-plus/icons-vue'
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
      pendingReviews.value = pendingReviews.value.map((r, i) =>
        i === index ? { ...r, replied: true } : r
      )
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
  grid-template-columns: 300px 1fr;
  gap: 16px;
  height: 100%;
  padding: 20px;
}

// --- 评价列表 ---
.review-list {
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, rgba(255,255,255,0.85), rgba(255,255,255,0.7));
  border: 1px solid rgba(226, 222, 216, 0.5);
  border-radius: 16px;
  overflow: hidden;

  .list-header {
    padding: 18px 16px;
    border-bottom: 1px solid @merchant-divider;

    .list-title-group {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    h3 {
      margin: 0;
      font-size: 15px;
      font-weight: 600;
      color: @merchant-text;
    }

    .pending-count {
      width: 24px;
      height: 24px;
      border-radius: 8px;
      background: @merchant-warning-light;
      color: @merchant-warning;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 12px;
      font-weight: 700;
    }
  }

  .list-content {
    flex: 1;
    overflow-y: auto;
    padding: 12px;

    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background: @merchant-border;
      border-radius: 2px;
    }
  }
}

.review-card {
  padding: 14px;
  background: rgba(250, 248, 245, 0.4);
  border: 1.5px solid transparent;
  border-radius: 12px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.22, 1, 0.36, 1);

  &:hover {
    background: rgba(244, 230, 222, 0.3);
    border-color: @merchant-border;
  }

  &.active {
    background: rgba(227, 240, 228, 0.3);
    border-color: @merchant-primary;
    box-shadow: 0 2px 8px rgba(74, 122, 77, 0.08);
  }

  .review-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .user-avatar {
      width: 28px;
      height: 28px;
      border-radius: 8px;
      background: linear-gradient(135deg, @merchant-secondary-light, rgba(244, 230, 222, 0.6));
      color: @merchant-secondary;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 12px;
      font-weight: 700;
    }

    .user-name {
      font-weight: 600;
      font-size: 13px;
      color: @merchant-text;
    }
  }

  .review-content {
    font-size: 13px;
    color: @merchant-text-sec;
    line-height: 1.6;
    margin-bottom: 10px;
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
      font-size: 11px;
      color: @merchant-text-muted;
    }

    .reply-status {
      font-size: 11px;
      font-weight: 600;
      padding: 2px 8px;
      border-radius: 20px;

      &.replied {
        background: rgba(90, 143, 94, 0.08);
        color: @merchant-success;
      }

      &.pending {
        background: rgba(196, 91, 91, 0.08);
        color: @merchant-error;
      }
    }
  }
}

.list-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: @merchant-text-muted;

  .empty-icon {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: @merchant-success-light;
    color: @merchant-success;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    font-weight: 700;
    margin-bottom: 8px;
  }

  p {
    font-size: 13px;
    margin: 0;
  }
}

// --- 建议面板 ---
.suggestions-panel {
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, rgba(255,255,255,0.85), rgba(255,255,255,0.7));
  border: 1px solid rgba(226, 222, 216, 0.5);
  border-radius: 16px;
  overflow: hidden;

  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 18px 20px;
    border-bottom: 1px solid @merchant-divider;

    .panel-title-group {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    h3 {
      margin: 0;
      font-size: 15px;
      font-weight: 600;
      color: @merchant-text;
    }

    .ai-badge {
      font-size: 10px;
      font-weight: 700;
      color: #fff;
      background: linear-gradient(135deg, @merchant-primary, darken(@merchant-primary, 5%));
      padding: 3px 8px;
      border-radius: 6px;
      letter-spacing: 0.5px;
    }
  }

  .refresh-btn {
    display: flex;
    align-items: center;
    gap: 5px;
    padding: 6px 12px;
    border: 1px solid @merchant-border;
    border-radius: 8px;
    background: @merchant-surface;
    color: @merchant-text-sec;
    font-size: 12px;
    cursor: pointer;
    transition: all 0.25s ease;
    font-family: inherit;

    &:hover {
      border-color: @merchant-primary;
      color: @merchant-primary;
    }

    &.spinning .el-icon {
      animation: spin 1s linear infinite;
    }
  }
}

// --- 评价详情 ---
.review-detail {
  padding: 16px 20px;
  background: rgba(250, 248, 245, 0.5);
  border-bottom: 1px solid @merchant-divider;

  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;

    .user-name {
      font-weight: 600;
      font-size: 14px;
      color: @merchant-text;
    }
  }

  .detail-content {
    font-size: 14px;
    color: @merchant-text;
    line-height: 1.7;
  }
}

// --- 建议卡片 ---
.suggestions {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: @merchant-border;
    border-radius: 2px;
  }
}

.suggestion-card {
  background: rgba(250, 248, 245, 0.5);
  border: 1.5px solid transparent;
  border-radius: 14px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.22, 1, 0.36, 1);

  &:hover {
    background: @merchant-surface;
    border-color: @merchant-border;
    box-shadow: 0 2px 8px rgba(45, 42, 38, 0.04);
  }

  &.selected {
    background: @merchant-surface;
    border-color: @merchant-primary;
    box-shadow: 0 4px 14px rgba(74, 122, 77, 0.08);
  }

  .suggestion-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
  }

  .style-tag {
    display: inline-flex;
    align-items: center;
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;

    &.style-0 {
      background: rgba(90, 143, 94, 0.1);
      color: @merchant-success;
    }

    &.style-1 {
      background: rgba(212, 168, 85, 0.1);
      color: @merchant-warning;
    }

    &.style-2 {
      background: rgba(91, 139, 210, 0.1);
      color: @merchant-info;
    }
  }

  .suggestion-actions {
    display: flex;
    gap: 4px;
  }

  .icon-btn {
    width: 30px;
    height: 30px;
    border-radius: 8px;
    border: 1px solid @merchant-border;
    background: @merchant-surface;
    color: @merchant-text-sec;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      border-color: @merchant-primary;
      color: @merchant-primary;
    }
  }

  .suggestion-text {
    font-size: 14px;
    color: @merchant-text;
    line-height: 1.7;
    margin-bottom: 12px;
  }

  .use-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 7px 14px;
    background: linear-gradient(135deg, @merchant-primary, darken(@merchant-primary, 4%));
    color: #fff;
    border: none;
    border-radius: 8px;
    font-size: 12px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.25s cubic-bezier(0.22, 1, 0.36, 1);
    font-family: inherit;

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 3px 10px rgba(74, 122, 77, 0.25);
    }
  }
}

// --- 加载状态 ---
.loading-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;

  .loading-dots {
    display: flex;
    gap: 6px;

    span {
      width: 8px;
      height: 8px;
      background: @merchant-primary;
      border-radius: 50%;
      animation: loadingBounce 1.4s ease-in-out infinite;

      &:nth-child(1) { animation-delay: 0s; }
      &:nth-child(2) { animation-delay: 0.15s; }
      &:nth-child(3) { animation-delay: 0.3s; }
    }
  }

  p {
    color: @merchant-text-sec;
    font-size: 14px;
    margin: 0;
  }
}

// --- 编辑区域 ---
.edit-area {
  padding: 16px 20px;
  background: rgba(244, 230, 222, 0.3);
  border-top: 1px solid @merchant-divider;

  .edit-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    span {
      font-size: 13px;
      font-weight: 600;
      color: @merchant-text;
    }
  }

  :deep(.el-textarea__inner) {
    border-radius: 10px;
    border-color: @merchant-border;
    font-size: 14px;
    line-height: 1.6;

    &:focus {
      border-color: @merchant-primary;
      box-shadow: 0 0 0 3px rgba(74, 122, 77, 0.08);
    }
  }

  .edit-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 12px;
  }

  .cancel-btn {
    padding: 8px 16px;
    border: 1px solid @merchant-border;
    border-radius: 8px;
    background: @merchant-surface;
    color: @merchant-text-sec;
    font-size: 13px;
    cursor: pointer;
    transition: all 0.2s ease;
    font-family: inherit;

    &:hover {
      border-color: @merchant-text-muted;
    }
  }

  .submit-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 18px;
    background: linear-gradient(135deg, @merchant-primary, darken(@merchant-primary, 4%));
    color: #fff;
    border: none;
    border-radius: 8px;
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.25s ease;
    font-family: inherit;

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 3px 10px rgba(74, 122, 77, 0.25);
    }
  }
}

// --- 空状态 ---
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  .empty-icon-wrap {
    width: 64px;
    height: 64px;
    border-radius: 18px;
    background: rgba(226, 222, 216, 0.3);
    color: @merchant-text-muted;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 16px;
  }

  .empty-title {
    font-size: 15px;
    font-weight: 600;
    color: @merchant-text;
    margin: 0 0 6px;
  }

  .empty-hint {
    font-size: 13px;
    color: @merchant-text-muted;
    margin: 0;
  }
}

// --- 动画 ---
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes loadingBounce {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-8px);
    opacity: 1;
  }
}

.edit-slide-enter-active {
  transition: all 0.35s cubic-bezier(0.22, 1, 0.36, 1);
}

.edit-slide-leave-active {
  transition: all 0.25s ease;
}

.edit-slide-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.edit-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
