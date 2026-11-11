<template>
  <div class="contact-page">
    <AppPageHeader title="联系客服">
      <template #actions>
        <CommonBackButton type="default" />
      </template>
    </AppPageHeader>

    <div class="contact-sections">
      <AppCard class="contact-card">
        <header class="section-header">
          <div class="section-title">
            <el-icon><Service /></el-icon>
            <h2>选择联系方式</h2>
          </div>
        </header>

        <div class="contact-info">
          <div class="contact-item">
            <div class="contact-label">
              <el-icon><Phone /></el-icon>
              <span>客服电话</span>
            </div>
            <div class="contact-value">
              <span class="contact-value__text">400-888-8888</span>
              <el-button
                class="copy-button"
                type="primary"
                text
                :icon="CopyDocument"
                aria-label="复制客服电话"
                @click="copyContact('400-888-8888', '客服电话')"
              />
            </div>
          </div>

          <div class="contact-item">
            <div class="contact-label">
              <el-icon><Clock /></el-icon>
              <span>工作时间</span>
            </div>
            <div class="contact-value">9:00-21:00（周一至周日）</div>
          </div>

          <div class="contact-item">
            <div class="contact-label">
              <el-icon><Message /></el-icon>
              <span>客服邮箱</span>
            </div>
            <div class="contact-value">
              <span class="contact-value__text">support@jaseats.com</span>
              <el-tag class="recommend-tag" size="small" effect="plain">推荐</el-tag>
              <el-button
                class="copy-button"
                type="primary"
                text
                :icon="CopyDocument"
                aria-label="复制客服邮箱"
                @click="copyContact('support@jaseats.com', '客服邮箱')"
              />
            </div>
          </div>

          <div class="contact-item contact-item--description">
            <div class="contact-label">
              <el-icon><InfoFilled /></el-icon>
              <span>反馈说明</span>
            </div>
            <div class="contact-value">
              <span class="feedback-desc">
                如有任何问题或建议，请发送邮件至客服邮箱，或使用下方反馈表单提交。我们会在24小时内回复您。
              </span>
            </div>
          </div>
        </div>
      </AppCard>

      <AppCard class="feedback-card">
        <header class="section-header">
          <div class="section-title">
            <el-icon><Promotion /></el-icon>
            <h2>提交反馈</h2>
          </div>
        </header>

        <el-form
          class="feedback-form"
          :model="feedback"
          label-position="top"
          @submit.prevent
        >
          <el-form-item label="反馈类型" prop="type">
            <el-select
              v-model="feedback.type"
              class="feedback-type-select"
              placeholder="请选择反馈类型"
            >
              <el-option
                v-for="item in typeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="内容" prop="content">
            <el-input
              v-model="feedback.content"
              placeholder="请详细描述您遇到的问题、您的建议或其他反馈内容..."
              type="textarea"
              :rows="5"
              maxlength="2000"
              show-word-limit
            />
          </el-form-item>
          <el-form-item label="联系方式" prop="contact">
            <el-input
              v-model="feedback.contact"
              placeholder="请留下您的手机号或邮箱，方便我们及时回复您"
            />
          </el-form-item>
          <el-form-item class="feedback-actions">
            <el-button type="primary" :loading="isSubmitting" :disabled="isSubmitting" @click="submitFeedback">
              <el-icon><Promotion /></el-icon>
              提交反馈
            </el-button>
            <el-button type="default" :disabled="isSubmitting" @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </AppCard>

      <AppCard class="feedback-history-card">
        <header class="section-header">
          <div class="section-title">
            <el-icon><Message /></el-icon>
            <h2>我的反馈</h2>
          </div>
        </header>

        <div v-if="historyLoading" class="history-empty">加载中…</div>
        <div v-else-if="history.length === 0" class="history-empty">还没有反馈记录</div>
        <ul v-else class="feedback-history">
          <li v-for="item in history" :key="item.feedbackId" class="feedback-history__item">
            <div class="feedback-history__head">
              <span class="feedback-history__type">{{ item.typeLabel || item.type }}</span>
              <el-tag size="small" effect="plain">{{ item.statusLabel || item.status }}</el-tag>
            </div>
            <p class="feedback-history__content">{{ item.content }}</p>
            <div class="feedback-history__meta">{{ formatTime(item.createdAt) }}</div>
          </li>
        </ul>
      </AppCard>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Clock,
  CopyDocument,
  InfoFilled,
  Message,
  Phone,
  Promotion,
  Service
} from '@element-plus/icons-vue'
import AppCard from '../../components/common/AppCard.vue'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import { createFeedback, listFeedbacks } from '../../api/feedback'

// 反馈类型与后端 FeedbackType 一一对应
const typeOptions = [
  { value: 'bug', label: '功能异常' },
  { value: 'feature', label: '功能建议' },
  { value: 'ui', label: '界面问题' },
  { value: 'performance', label: '性能问题' },
  { value: 'order', label: '订单问题' },
  { value: 'payment', label: '支付问题' },
  { value: 'other', label: '其他问题' }
]

// 反馈表单数据
const feedback = ref({
  type: '',
  content: '',
  contact: ''
})

const isSubmitting = ref(false)
const history = ref([])
const historyLoading = ref(false)

// 复制联系方式后给出即时反馈
const copyContact = async (text, label) => {
  try {
    await window.api.clipboard.writeText(text)
    ElMessage.success(`${label}已复制`)
  } catch (error) {
    console.error('复制客服联系方式失败:', error)
    ElMessage.error('复制失败，请手动复制')
  }
}

// 提交反馈：归属由后端从可信 JWT 推导，这里只提交业务字段
const submitFeedback = async () => {
  if (isSubmitting.value) return

  if (!feedback.value.type) {
    ElMessage.warning('请选择反馈类型')
    return
  }
  if (!feedback.value.content || !feedback.value.content.trim()) {
    ElMessage.warning('请填写反馈内容')
    return
  }

  isSubmitting.value = true
  try {
    const response = await createFeedback({
      type: feedback.value.type,
      content: feedback.value.content.trim(),
      contact: feedback.value.contact ? feedback.value.contact.trim() : undefined,
      deviceInfo: { platform: 'desktop', system: window.navigator?.userAgent || '' }
    })

    if (response.success) {
      ElMessage.success('反馈已提交，我们会跟进处理')
      resetForm()
      await loadHistory()
    } else {
      ElMessage.error(response.message || '反馈提交失败')
    }
  } catch (error) {
    console.error('提交反馈失败:', error)
    ElMessage.error(error.message || '反馈提交失败，请稍后重试')
  } finally {
    isSubmitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  feedback.value = {
    type: '',
    content: '',
    contact: ''
  }
}

// 加载自己的反馈历史
const loadHistory = async () => {
  historyLoading.value = true
  try {
    const response = await listFeedbacks({ page: 1, size: 10 })
    history.value = response?.data?.list ?? []
  } catch (error) {
    console.error('加载反馈历史失败:', error)
    history.value = []
  } finally {
    historyLoading.value = false
  }
}

const formatTime = (value) => {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN', { hour12: false })
}

onMounted(loadHistory)
</script>

<style scoped lang="less">
/* 页面颜色、间距、圆角与字阶全部来自 tokens.css（§3、§7.1）。 */
.contact-page {
  box-sizing: border-box;
  width: 100%;
  max-width: var(--page-max);
  min-height: 100%;
  margin: 0 auto;
  padding: 0 var(--page-gutter) var(--space-10);
  background: var(--canvas);
}

/* 页头动作区控件高度取 --control-height-md（§3.4、§4.7）。 */
.contact-page :deep(.app-page-header__actions .el-button) {
  height: var(--control-height-md);
  min-height: var(--control-height-md);
}

.contact-sections {
  display: flex;
  flex-direction: column;
  gap: var(--card-gap);
}

.section-header {
  margin-bottom: var(--space-6);
}

.section-title {
  display: flex;
  align-items: center;
  gap: var(--space-3);

  .el-icon {
    font-size: 20px;
    color: var(--brand-500);
  }

  h2 {
    margin: 0;
    font-size: var(--text-h2);
    line-height: 28px;
    font-weight: var(--weight-semibold);
    color: var(--ink-900);
  }
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.contact-item {
  display: grid;
  grid-template-columns: 120px minmax(0, 1fr);
  gap: var(--space-4);
  align-items: center;
}

.contact-label {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-body-sm);
  line-height: 20px;
  font-weight: var(--weight-medium);
  color: var(--ink-500);

  .el-icon {
    font-size: 16px;
    color: var(--ink-400);
  }
}

.contact-value {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  min-width: 0;
  font-size: var(--text-body);
  line-height: 22px;
  color: var(--ink-900);
}

.contact-value__text {
  overflow-wrap: anywhere;
}

.contact-item--description {
  align-items: flex-start;

  .contact-label {
    padding-top: 1px;
  }

  .contact-value {
    color: var(--ink-700);
  }
}

.recommend-tag {
  margin-left: var(--space-2);
}

.copy-button {
  height: var(--control-height-sm);
  padding-inline: var(--space-1);
}

.feedback-form {
  :deep(.el-form-item) {
    margin-bottom: var(--space-4);
  }

  :deep(.el-form-item__label) {
    height: auto;
    padding: 0 0 var(--space-2);
    font-size: var(--text-body);
    line-height: 22px;
    color: var(--ink-700);
  }

  :deep(.el-form-item__error) {
    font-size: var(--text-caption);
    line-height: 18px;
  }
}

.feedback-actions {
  margin-bottom: 0;

  :deep(.el-button) {
    height: var(--control-height-md);
    min-height: var(--control-height-md);
  }
}

/* 反馈类型下拉占满一行；历史列表用卡片内轻分隔，不引入新控件。 */
.feedback-type-select {
  width: 100%;
}

.history-empty {
  padding: var(--space-4) 0;
  color: var(--text-secondary);
  font-size: var(--text-body);
}

.feedback-history {
  margin: 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.feedback-history__item {
  padding: var(--space-4);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  background: var(--surface-sunken);
}

.feedback-history__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  margin-bottom: var(--space-2);
}

.feedback-history__type {
  font-weight: var(--weight-semibold);
  color: var(--text-primary);
}

.feedback-history__content {
  margin: 0 0 var(--space-2);
  color: var(--text-primary);
  white-space: pre-wrap;
  word-break: break-word;
}

.feedback-history__meta {
  color: var(--text-secondary);
  font-size: var(--text-caption);
}

</style>
