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
            <el-avatar :size="100" :src="getAvatarUrl(profileForm.avatar)">
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
          {{ profileForm.roleName }}
        </el-descriptions-item>
        <el-descriptions-item label="角色代码">
          {{ profileForm.roleCode }}
        </el-descriptions-item>
        <el-descriptions-item label="账号状态">
          <el-tooltip
            :content="profileForm.status === 'ACTIVE' ? '账号功能正常，可以正常使用系统所有功能' : '账号已被禁用，无法登录和使用系统，请联系超级管理员'"
            placement="top"
          >
            <el-tag :type="profileForm.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ profileForm.status === 'ACTIVE' ? '正常' : '禁用' }}
            </el-tag>
          </el-tooltip>
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
import { getAvatarUrl } from '@/utils/avatar'
import api from '@/utils/api'

const profileFormRef = ref(null)
const loading = ref(false)
// 临时存储待上传的头像 base64 数据
const pendingAvatarBase64 = ref(null)

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
const initProfile = async () => {
  try {
    // 先从后端获取最新数据
    const response = await api.get('/admin/current')
    if (response.success && response.admin) {
      // 更新 localStorage
      localStorage.setItem('admin_info', JSON.stringify(response.admin))
      // 更新表单
      Object.assign(profileForm, response.admin)
      console.log('[个人信息] 从后端加载最新管理员信息:', response.admin)
    } else {
      // 如果后端获取失败，降级从 localStorage 读取
      const adminInfo = getAdminInfo()
      if (adminInfo) {
        Object.assign(profileForm, adminInfo)
        console.log('[个人信息] 从本地存储加载管理员信息:', adminInfo)
      }
    }
  } catch (error) {
    console.error('[个人信息] 获取管理员信息失败:', error)
    // 出错时也从 localStorage 读取
    const adminInfo = getAdminInfo()
    if (adminInfo) {
      Object.assign(profileForm, adminInfo)
      console.log('[个人信息] 从本地存储加载管理员信息（降级）:', adminInfo)
    }
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

// 上传头像（仅前端预览）
const uploadAvatar = async ({ file }) => {
  try {
    console.log('[个人信息] 选择头像:', file.name)

    // 将文件转换为base64
    const reader = new FileReader()
    reader.onload = (e) => {
      const base64Image = e.target.result
      // 临时存储 base64 数据，等待保存时上传
      pendingAvatarBase64.value = base64Image
      // 立即更新预览
      profileForm.avatar = base64Image
      console.log('[个人信息] 头像预览已更新')
    }

    reader.readAsDataURL(file)
  } catch (error) {
    console.error('[个人信息] 读取头像失败:', error)
    ElMessage.error('读取头像失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!profileFormRef.value) return

  try {
    // 验证表单（如果有验证错误会抛出异常）
    await profileFormRef.value.validate()
    loading.value = true

    console.log('[个人信息] 保存个人信息:', profileForm)

    // 如果有待上传的头像，先上传头像
    if (pendingAvatarBase64.value) {
      console.log('[个人信息] 检测到新头像，开始上传...')

      try {
        const avatarResponse = await api.put('/admin/profile/avatar/base64', {
          avatarBase64: pendingAvatarBase64.value
        })

        if (avatarResponse.code !== '200' && !avatarResponse.success) {
          ElMessage.error('头像上传失败: ' + (avatarResponse.message || '未知错误'))
          return
        }

        console.log('[个人信息] 头像上传成功')

        // 清除待上传的头像数据
        pendingAvatarBase64.value = null
      } catch (error) {
        console.error('[个人信息] 头像上传失败:', error)
        ElMessage.error('头像上传失败: ' + (error.message || '网络错误'))
        return
      }
    }

    // 只发送基本信息，不发送 avatar
    const { adminId, realName, phone, email } = profileForm
    const updateData = { adminId, realName, phone, email }

    // 调用更新个人信息API
    const response = await updateAdminProfile(updateData)

    if (response.code === '200' || response.success) {
      // 重新从后端获取最新的管理员信息（包含新的头像 URL）
      const currentResponse = await api.get('/admin/current')
      if (currentResponse.success && currentResponse.admin) {
        const updatedInfo = {
          ...profileForm,
          ...currentResponse.admin
        }
        localStorage.setItem('admin_info', JSON.stringify(updatedInfo))
        // 更新表单
        Object.assign(profileForm, currentResponse.admin)
      }

      ElMessage.success('保存成功')
    } else {
      ElMessage.error('保存失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    // 验证错误会被 Element Plus 自动处理，不需要额外提示
    console.error('[个人信息] 保存失败:', error)
    // 只在网络错误或其他非验证错误时显示提示
    if (error.message && !error.message.includes('validation')) {
      ElMessage.error('保存失败: ' + error.message)
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
