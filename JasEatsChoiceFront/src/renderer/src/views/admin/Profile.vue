<template>
  <div class="admin-profile-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>个人信息</h1>
      <p class="subtitle">查看和编辑管理员个人信息</p>
    </div>

    <!-- 个人信息卡片 -->
    <el-card class="profile-card" shadow="never">
      <el-form :model="profileForm" :rules="rules" ref="profileFormRef" label-width="120px">
        <el-divider content-position="left">基本信息</el-divider>

        <el-form-item label="用户名">
          <el-input v-model="profileForm.username" disabled />
        </el-form-item>

        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
        </el-form-item>

        <el-form-item label="头像">
          <div class="avatar-upload">
            <el-avatar :size="100" :src="profileForm.avatar">
              {{ profileForm.realName ? profileForm.realName.charAt(0) : 'A' }}
            </el-avatar>
            <el-upload
              class="avatar-uploader"
              action="#"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="uploadAvatar"
            >
              <el-button size="small" type="primary">点击上传</el-button>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            保存修改
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 角色信息 -->
    <el-card class="role-card" shadow="never">
      <el-divider content-position="left">角色信息</el-divider>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="角色名称">
          <el-tag type="warning">{{ profileForm.roleName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="角色代码">
          {{ profileForm.roleCode }}
        </el-descriptions-item>
        <el-descriptions-item label="账号状态">
          <el-tag :type="profileForm.status === 'ACTIVE' ? 'success' : 'danger'">
            {{ profileForm.status === 'ACTIVE' ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ profileForm.createTime }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminInfo } from '@/utils/auth'
import { updateAdminProfile } from '@/api/admin'

const profileFormRef = ref(null)
const loading = ref(false)

const profileForm = reactive({
  adminId: null,
  username: '',
  realName: '',
  phone: '',
  email: '',
  avatar: '',
  roleCode: '',
  roleName: '',
  status: '',
  createTime: ''
})

const rules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 初始化表单数据
const initProfile = () => {
  const adminInfo = getAdminInfo()
  if (adminInfo) {
    Object.assign(profileForm, adminInfo)
    console.log('[个人信息] 加载管理员信息:', adminInfo)
  }
}

// 头像上传前校验
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 上传头像
const uploadAvatar = async ({ file }) => {
  try {
    console.log('[个人信息] 上传头像:', file.name)
    // TODO: 实现头像上传功能
    ElMessage.info('头像上传功能开发中...')
  } catch (error) {
    console.error('[个人信息] 上传头像失败:', error)
    ElMessage.error('上传失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!profileFormRef.value) return

  try {
    await profileFormRef.value.validate()
    loading.value = true

    console.log('[个人信息] 保存个人信息:', profileForm)

    // TODO: 调用更新个人信息API
    // await updateAdminProfile(profileForm)

    // 更新localStorage中的信息
    const updatedInfo = {
      ...profileForm
    }
    localStorage.setItem('admin_info', JSON.stringify(updatedInfo))

    ElMessage.success('保存成功')
  } catch (error) {
    console.error('[个人信息] 保存失败:', error)
    if (error !== 'cancel') {
      ElMessage.error('保存失败: ' + (error.message || '网络错误'))
    }
  } finally {
    loading.value = false
  }
}

// 重置表单
const handleReset = () => {
  initProfile()
  ElMessage.info('已重置')
}

onMounted(() => {
  initProfile()
})
</script>

<style scoped lang="less">
.admin-profile-container {
  .page-header {
    margin-bottom: 20px;

    h1 {
      font-size: 24px;
      color: #303133;
      margin: 0 0 8px 0;
    }

    .subtitle {
      color: #909399;
      margin: 0;
      font-size: 14px;
    }
  }

  .profile-card,
  .role-card {
    margin-bottom: 20px;
  }

  .avatar-upload {
    display: flex;
    align-items: center;
    gap: 20px;

    .avatar-uploader {
      display: block;
    }
  }
}
</style>
