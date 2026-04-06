<template>
  <div class="contact-container">
    <h2>联系客服</h2>

    <el-card class="contact-card">
      <div class="contact-info">
        <div class="contact-item">
          <div class="contact-label">客服电话</div>
          <div class="contact-value">400-888-8888</div>
        </div>

        <div class="contact-item">
          <div class="contact-label">工作时间</div>
          <div class="contact-value">9:00-21:00（周一至周日）</div>
        </div>

        <div class="contact-item">
          <div class="contact-label">客服邮箱</div>
          <div class="contact-value">
            <span>support@jaseats.com</span>
            <el-tag type="info" size="small" style="margin-left: 10px">推荐</el-tag>
          </div>
        </div>

        <div class="contact-item">
          <div class="contact-label">反馈说明</div>
          <div class="contact-value">
            <span class="feedback-desc">
              如有任何问题或建议，请发送邮件至客服邮箱，或使用下方反馈表单提交。我们会在24小时内回复您。
            </span>
          </div>
        </div>
      </div>

      <el-divider />

      <div class="feedback-section">
        <h3>📝 在线反馈</h3>
        <el-form ref="feedbackFormRef" :model="feedback" label-width="80px">
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
          <el-form-item>
            <el-button type="primary" @click="submitFeedback">
              <el-icon><Promotion /></el-icon>
              提交反馈
            </el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Promotion } from '@element-plus/icons-vue'

// 反馈表单数据
const feedback = ref({
  title: '',
  content: '',
  contact: ''
})

// 提交反馈
const submitFeedback = () => {
  // 表单验证
  if (!feedback.value.title || !feedback.value.content) {
    ElMessage.warning('请填写反馈标题和内容')
    return
  }

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
@import '../../assets/css/nordic-theme.less';

.contact-container {
  padding: 0 @nordic-space-xl @nordic-space-xl @nordic-space-xl;

  h2 {
    font-size: @nordic-text-xl;
    margin: 0 0 @nordic-space-xl 0;
    font-weight: 600;
    color: @nordic-text;
  }

  .contact-card {
    padding: @nordic-space-xl;

    .contact-info {
      margin-bottom: @nordic-space-xl;

      .contact-item {
        margin-bottom: 15px;
        display: flex;
        align-items: center;

        .contact-label {
          width: 120px;
          font-weight: 600;
          margin-right: @nordic-space-xl;
          color: @nordic-text-secondary;
        }

        .contact-value {
          flex: 1;
          font-size: @nordic-text-md;
          color: @nordic-text;
        }
      }
    }

    .feedback-section {
      h3 {
        font-size: @nordic-text-lg;
        margin: 0 0 15px 0;
        font-weight: 600;
        color: @nordic-text;
      }

      .feedback-desc {
        color: @nordic-text-secondary;
        font-size: @nordic-text-base;
      }
    }
  }
}
</style>
