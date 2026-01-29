<script setup>
import { ref } from 'vue'
import { Setting, Notification, Lock, User } from '@element-plus/icons-vue'

const activeTab = ref('system')

// 系统设置
const systemSettings = ref({
  siteName: '佳食宜选',
  allowUserUpload: false,
  requireTutorialReview: true,
  aiTutorialEnabled: true
})

// 通知设置
const notificationSettings = ref({
  emailEnabled: true,
  smsEnabled: false,
  reviewNotifyEnabled: true
})

// 安全设置
const securitySettings = ref({
  sessionTimeout: 30,
  maxLoginAttempts: 5,
  passwordMinLength: 6
})

const saveSystemSettings = () => {
  console.log('保存系统设置:', systemSettings.value)
  alert('设置已保存')
}

const saveNotificationSettings = () => {
  console.log('保存通知设置:', notificationSettings.value)
  alert('设置已保存')
}

const saveSecuritySettings = () => {
  console.log('保存安全设置:', securitySettings.value)
  alert('设置已保存')
}
</script>

<template>
  <div class="admin-settings-container">
    <el-card shadow="never">
      <template #header>
        <div class="header">
          <h3>
            <el-icon><Setting /></el-icon>
            系统设置
          </h3>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 系统设置 -->
        <el-tab-pane label="系统设置" name="system">
          <el-form :model="systemSettings" label-width="140px" style="max-width: 600px">
            <h4>基本设置</h4>
            <el-form-item label="网站名称">
              <el-input v-model="systemSettings.siteName" />
            </el-form-item>

            <el-form-item label="允许用户上传教程">
              <el-switch v-model="systemSettings.allowUserUpload" />
              <span class="form-tip">开启后，普通用户可以上传自己的教程</span>
            </el-form-item>

            <el-form-item label="教程审核机制">
              <el-switch v-model="systemSettings.requireTutorialReview" />
              <span class="form-tip">商家和AI生成的教程需要审核后发布</span>
            </el-form-item>

            <el-form-item label="AI教程生成">
              <el-switch v-model="systemSettings.aiTutorialEnabled" />
              <span class="form-tip">启用AI自动生成教程内容</span>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="saveSystemSettings">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 通知设置 -->
        <el-tab-pane label="通知设置" name="notification">
          <el-form :model="notificationSettings" label-width="140px" style="max-width: 600px">
            <h4>通知方式</h4>

            <el-form-item label="邮件通知">
              <el-switch v-model="notificationSettings.emailEnabled" />
              <span class="form-tip">审核结果等重要事件将通过邮件通知</span>
            </el-form-item>

            <el-form-item label="短信通知">
              <el-switch v-model="notificationSettings.smsEnabled" />
              <span class="form-tip">紧急通知将通过短信发送</span>
            </el-form-item>

            <el-form-item label="审核通知">
              <el-switch v-model="notificationSettings.reviewNotifyEnabled" />
              <span class="form-tip">商家提交审核时通知管理员</span>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="saveNotificationSettings">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 安全设置 -->
        <el-tab-pane label="安全设置" name="security">
          <el-form :model="securitySettings" label-width="140px" style="max-width: 600px">
            <h4>会话管理</h4>

            <el-form-item label="会话超时时间">
              <el-input-number v-model="securitySettings.sessionTimeout" :min="5" :max="120" />
              <span class="form-tip">单位：分钟，超时后需要重新登录</span>
            </el-form-item>

            <h4>登录安全</h4>

            <el-form-item label="最大登录尝试次数">
              <el-input-number v-model="securitySettings.maxLoginAttempts" :min="3" :max="10" />
              <span class="form-tip">超过次数后将锁定账户</span>
            </el-form-item>

            <el-form-item label="密码最小长度">
              <el-input-number v-model="securitySettings.passwordMinLength" :min="6" :max="20" />
              <span class="form-tip">用户密码的最小长度要求</span>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="saveSecuritySettings">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<style scoped lang="less">
.admin-settings-container {
  padding: 20px;

  .header {
    h3 {
      margin: 0;
      font-size: 20px;
      color: #303133;
      display: flex;
      align-items: center;
      gap: 10px;
    }
  }

  h4 {
    font-size: 16px;
      color: #303133;
      margin: 20px 0 15px 0;
      padding-bottom: 8px;
      border-bottom: 2px solid #ff6b6b;
  }

  .form-tip {
    margin-left: 10px;
    color: #909399;
    font-size: 12px;
  }
}
</style>
