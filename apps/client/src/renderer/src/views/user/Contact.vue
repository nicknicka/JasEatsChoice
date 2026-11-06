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
          <el-form-item label="标题" prop="title">
            <el-input v-model="feedback.title" placeholder="请简要描述您的问题或建议（如：订单支付失败）" />
          </el-form-item>
          <el-form-item label="内容" prop="content">
            <el-input
              v-model="feedback.content"
              placeholder="请详细描述您遇到的问题、您的建议或其他反馈内容..."
              type="textarea"
              :rows="5"
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
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
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

// 反馈表单数据
const feedback = ref({
  title: '',
  content: '',
  contact: ''
})

const isSubmitting = ref(false)

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

// 提交反馈
const submitFeedback = () => {
  if (isSubmitting.value) return

  // 表单验证
  if (!feedback.value.title || !feedback.value.content) {
    ElMessage.warning('请填写反馈标题和内容')
    return
  }

  isSubmitting.value = true

  try {
    // 模拟提交反馈
    console.log('提交的反馈:', feedback.value)

    // 重置表单
    resetForm()

    // 提示成功
    ElMessage.success({
      message: '✅ 反馈已提交成功！我们会尽快处理您的反馈，并通过邮件或电话回复您。',
      duration: 3000,
      showClose: true
    })
  } finally {
    isSubmitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  feedback.value = {
    title: '',
    content: '',
    contact: ''
  }
}
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
</style>
