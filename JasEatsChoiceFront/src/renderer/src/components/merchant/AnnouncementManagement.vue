<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'

const props = defineProps({
  merchantId: {
    type: String,
    required: true
  }
})

// 公告栏配置
const announcements = ref([])
const announcementDialogVisible = ref(false)
const announcementFormRef = ref(null)
const savingAnnouncement = ref(false)
const currentAnnouncement = ref({
  title: '',
  content: '',
  status: 'active',
  startTime: null,
  endTime: null
})
const isEditingAnnouncement = ref(false)

// 表单验证规则
const announcementRules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度应在2-50个字符之间', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' },
    { min: 5, max: 500, message: '内容长度应在5-500个字符之间', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择公告状态', trigger: 'change' }
  ],
  startTime: [
    {
      validator: (rule, value, callback) => {
        if (currentAnnouncement.value.endTime && value) {
          if (new Date(value) > new Date(currentAnnouncement.value.endTime)) {
            callback(new Error('开始时间不能晚于结束时间'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  endTime: [
    {
      validator: (rule, value, callback) => {
        if (currentAnnouncement.value.startTime && value) {
          if (new Date(value) < new Date(currentAnnouncement.value.startTime)) {
            callback(new Error('结束时间不能早于开始时间'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 开始时间禁用选项
const startTimeDisabled = (date) => {
  if (currentAnnouncement.value.endTime) {
    return date > new Date(currentAnnouncement.value.endTime)
  }
  return false
}

// 结束时间禁用选项
const endTimeDisabled = (date) => {
  if (currentAnnouncement.value.startTime) {
    return date < new Date(currentAnnouncement.value.startTime)
  }
  return false
}

// 获取公告ID（兼容多种字段名）
const getAnnouncementId = (announcement) => {
  return announcement?.id || announcement?.announcementId || announcement?.announcement_id || null
}

// 获取公告列表
const getAnnouncements = () => {
  let url = API_CONFIG.merchant.announcements
  url = url.replace('{merchantId}', props.merchantId)
  api
    .get(url)
    .then(function (response) {
      console.log('获取公告列表响应:', response)
      // 兼容不同的响应格式
      let dataList = []
      if (response && (response.success || response.code === '200')) {
        dataList = response.data || []
      } else if (response.data && (response.data.success || response.data.code === '200')) {
        dataList = response.data.data || []
      }

      // 打印第一条数据用于调试
      if (dataList.length > 0) {
        console.log('第一条公告数据:', dataList[0])
        console.log('公告ID字段:', dataList[0].id, dataList[0].announcementId, dataList[0].announcement_id)
      }

      announcements.value = dataList
    })
    .catch(function (error) {
      console.error('获取公告列表失败:', error)
    })
}

// 打开公告编辑对话框
const openAnnouncementDialog = function (announcement = null) {
  announcementDialogVisible.value = true

  // 重置表单验证
  if (announcementFormRef.value) {
    announcementFormRef.value.clearValidate()
  }

  if (announcement) {
    isEditingAnnouncement.value = true
    // 深拷贝公告数据
    currentAnnouncement.value = JSON.parse(JSON.stringify(announcement))
    // 确保ID字段存在
    if (!currentAnnouncement.value.id) {
      const id = getAnnouncementId(announcement)
      if (id) {
        currentAnnouncement.value.id = id
      }
    }
  } else {
    isEditingAnnouncement.value = false
    currentAnnouncement.value = {
      title: '',
      content: '',
      status: 'active',
      startTime: null,
      endTime: null
    }
  }
}

// 保存公告
const saveAnnouncement = async function () {
  if (!announcementFormRef.value) return

  try {
    // 验证表单
    await announcementFormRef.value.validate()

    savingAnnouncement.value = true

    // 格式化时间数据
    const announcementData = {
      ...currentAnnouncement.value,
      startTime: currentAnnouncement.value.startTime ? new Date(currentAnnouncement.value.startTime).toISOString() : null,
      endTime: currentAnnouncement.value.endTime ? new Date(currentAnnouncement.value.endTime).toISOString() : null
    }

    let apiMethod = isEditingAnnouncement.value ? api.put : api.post
    let apiUrl = API_CONFIG.merchant.announcements.replace('{merchantId}', props.merchantId)
    if (isEditingAnnouncement.value) {
      const announcementId = getAnnouncementId(currentAnnouncement.value)
      if (!announcementId) {
        ElMessage.error('保存失败：公告ID不存在')
        return
      }
      apiUrl = apiUrl + '/' + announcementId
    }

    console.log('保存公告，URL:', apiUrl, '数据:', announcementData)

    const response = await apiMethod(apiUrl, announcementData)
    console.log('保存公告响应:', response)

    // 兼容不同的响应格式
    const isSuccess = response && (response.success || response.code === '200')
    const isSuccessData = response.data && (response.data.success || response.data.code === '200')

    if (isSuccess || isSuccessData) {
      let message = isEditingAnnouncement.value ? '公告已更新' : '公告已添加'
      ElMessage.success(message)
      getAnnouncements() // 刷新公告列表
      announcementDialogVisible.value = false
    } else {
      console.error('保存公告失败，响应格式:', response)
      ElMessage.error('保存公告失败：' + (response?.message || '未知错误'))
    }
  } catch (error) {
    if (error === 'cancel') {
      // 表单验证失败，不处理
      return
    }
    console.error('保存公告失败:', error)
    ElMessage.error('保存公告失败：' + (error.message || '网络错误'))
  } finally {
    savingAnnouncement.value = false
  }
}

// 删除公告
const deleteAnnouncement = function (announcement) {
  const announcementId = getAnnouncementId(announcement)
  if (!announcementId) {
    console.error('公告ID不存在:', announcement)
    ElMessage.error('删除失败：公告ID不存在')
    return
  }

  ElMessageBox.confirm(`确定要删除公告 '${announcement.title}' 吗？`, '删除公告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(function () {
      let url = API_CONFIG.merchant.announcements.replace('{merchantId}', props.merchantId)
      url = url + '/' + announcementId

      console.log('删除公告，URL:', url, '公告ID:', announcementId)

      api
        .delete(url)
        .then(function (response) {
          console.log('删除公告响应:', response)
          // 兼容不同的响应格式
          const isSuccess = response && (response.success || response.code === '200')
          const isSuccessData =
            response.data && (response.data.success || response.data.code === '200')

          if (isSuccess || isSuccessData) {
            ElMessage.success('公告已删除')
            getAnnouncements() // 刷新公告列表
          } else {
            ElMessage.error('删除公告失败：' + (response?.message || '未知错误'))
          }
        })
        .catch(function (error) {
          console.error('删除公告失败:', error)
          ElMessage.error('删除公告失败：' + (error.message || '网络错误'))
        })
    })
    .catch(function () {
      ElMessage.info('已取消删除')
    })
}

// 切换公告状态
const toggleAnnouncementStatus = function (announcement) {
  const announcementId = getAnnouncementId(announcement)

  // 检查公告ID是否存在
  if (!announcementId) {
    console.error('公告ID不存在:', announcement)
    ElMessage.error('切换失败：公告ID不存在，请刷新页面重试')
    return
  }

  let newStatus = announcement.status === 'active' ? 'inactive' : 'active'
  let statusText = newStatus === 'active' ? '已启用' : '已禁用'

  let url = API_CONFIG.merchant.announcements.replace('{merchantId}', props.merchantId)
  url = url + '/' + announcementId + '/status'

  console.log('切换公告状态，URL:', url, '公告ID:', announcementId, '新状态:', newStatus)
  console.log('完整公告对象:', announcement)

  api
    .put(url, { status: newStatus })
    .then(function (response) {
      console.log('切换公告状态响应:', response)
      // 兼容不同的响应格式
      const isSuccess = response && (response.success || response.code === '200')
      const isSuccessData = response.data && (response.data.success || response.data.code === '200')

      if (isSuccess || isSuccessData) {
        announcement.status = newStatus
        ElMessage.success('公告已' + statusText)
      } else {
        console.error('切换公告状态失败，响应格式:', response)
        let errorMsg = response?.message || response?.data?.message || '未知错误'
        ElMessage.error('切换公告状态失败：' + errorMsg)
      }
    })
    .catch(function (error) {
      console.error('切换公告状态失败:', error)
      let errorMsg = error.response?.data?.message || error.message || '网络错误'
      ElMessage.error('切换公告状态失败：' + errorMsg)
    })
}

onMounted(() => {
  getAnnouncements()
})
</script>

<template>
  <div class="announcement-section">
    <div class="announcement-header">
      <h3 class="card-title">📢 公告栏管理</h3>
      <el-button type="primary" size="small" @click="openAnnouncementDialog()">
        <el-icon><Plus /></el-icon> 添加公告
      </el-button>
    </div>
    <div class="announcement-table-container">
      <el-table :data="announcements" :default-sort="{ prop: 'createdTime', order: 'descending' }">
        <el-table-column prop="title" label="公告标题" min-width="200">
          <template #default="scope">
            <span>{{ scope.row.title }}</span>
            <el-tag v-if="scope.row.type === 'system' && !scope.row.merchantId" type="info" size="small" style="margin-left: 8px">
              系统公告
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'warning'">
              {{ scope.row.status === 'active' ? '已启用' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <!-- 判断是否为系统公告：type为system且merchantId为空 -->
            <template v-if="scope.row.type === 'system' && !scope.row.merchantId">
              <el-tooltip content="系统公告不允许编辑" placement="top">
                <el-button type="primary" size="small" disabled>
                  编辑
                </el-button>
              </el-tooltip>
              <el-tooltip content="系统公告不允许修改状态" placement="top">
                <el-button :type="scope.row.status === 'active' ? 'warning' : 'success'" size="small" disabled>
                  {{ scope.row.status === 'active' ? '禁用' : '启用' }}
                </el-button>
              </el-tooltip>
              <el-tooltip content="系统公告不允许删除" placement="top">
                <el-button type="danger" size="small" disabled>
                  删除
                </el-button>
              </el-tooltip>
            </template>
            <!-- 商家公告允许操作 -->
            <template v-else>
              <el-button type="primary" size="small" @click="openAnnouncementDialog(scope.row)">
                编辑
              </el-button>
              <el-button
                :type="scope.row.status === 'active' ? 'warning' : 'success'"
                size="small"
                @click="toggleAnnouncementStatus(scope.row)"
              >
                {{ scope.row.status === 'active' ? '禁用' : '启用' }}
              </el-button>
              <el-button type="danger" size="small" @click="() => deleteAnnouncement(scope.row)">
                删除
              </el-button>
            </template>
          </template>
        </el-table-column>
        <template #empty>
          <div class="empty-state">
            <span class="el-icon-info" />
            <p>暂无公告，请点击右上角"添加公告"创建</p>
          </div>
        </template>
      </el-table>
    </div>

    <!-- 公告编辑对话框 -->
    <el-dialog
      v-model="announcementDialogVisible"
      :title="isEditingAnnouncement ? '编辑公告' : '添加公告'"
      width="600px"
      top="10%"
      :close-on-click-modal="false"
    >
      <el-form
        ref="announcementFormRef"
        :model="currentAnnouncement"
        :rules="announcementRules"
        label-width="100px"
        status-icon
      >
        <el-form-item label="公告标题" prop="title" required>
          <el-input
            v-model="currentAnnouncement.title"
            placeholder="请输入公告标题（2-50个字符）"
            maxlength="50"
            show-word-limit
            clearable
          />
        </el-form-item>
        <el-form-item label="公告内容" prop="content" required>
          <el-input
            v-model="currentAnnouncement.content"
            placeholder="请输入公告内容（5-500个字符）"
            type="textarea"
            :rows="5"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="状态" prop="status" required>
          <el-select
            v-model="currentAnnouncement.status"
            placeholder="请选择公告状态"
            style="width: 100%"
          >
            <el-option label="已启用" value="active">
              <span>已启用</span>
              <span style="color: #8492a6; font-size: 13px; margin-left: 8px">公告会立即展示</span>
            </el-option>
            <el-option label="已禁用" value="inactive">
              <span>已禁用</span>
              <span style="color: #8492a6; font-size: 13px; margin-left: 8px">公告不会展示</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="currentAnnouncement.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
            :disabled-date="startTimeDisabled"
            clearable
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="currentAnnouncement.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
            :disabled-date="endTimeDisabled"
            clearable
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="announcementDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="savingAnnouncement" @click="saveAnnouncement">
            {{ savingAnnouncement ? '保存中...' : '确定' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
@import '../../assets/css/merchant-theme.less';

.announcement-section {
  margin-bottom: 24px;
  padding: 24px;
  border: 2px solid @merchant-text-muted;
  border-radius: 12px;
  background: linear-gradient(135deg, @merchant-surface 0%, @merchant-surface-alt 100%);
  box-shadow: 0 4px 20px rgba(144, 147, 153, 0.15);

  .announcement-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    padding-bottom: 20px;
    border-bottom: 2px solid @merchant-border;

    .card-title {
      margin: 0;
      font-size: 22px;
      font-weight: 700;
      color: @merchant-text-sec;
      display: flex;
      align-items: center;
      gap: 10px;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    }

    :deep(.el-button) {
      border-radius: 8px;
      padding: 10px 20px;
      font-weight: 600;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(144, 147, 153, 0.3);
      }
    }
  }

  .announcement-table-container {
    background: linear-gradient(135deg, @merchant-surface 0%, @merchant-surface-alt 100%);
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    border: 1px solid @merchant-border;

    :deep(.el-table) {
      border-radius: 8px;
      overflow: hidden;

      &::before {
        display: none;
      }

      .el-table__header-wrapper {
        th {
          background: linear-gradient(135deg, @merchant-surface-alt 0%, @merchant-border 100%);
          color: @merchant-text;
          font-weight: 700;
          border-bottom: 2px solid @merchant-border;
          padding: 14px 0;
        }
      }

      .el-table__body-wrapper {
        tr {
          transition: all 0.3s ease;
          background-color: @merchant-surface;

          &:hover {
            background: linear-gradient(90deg, @merchant-surface-alt 0%, @merchant-surface 100%);
            transform: scale(1.005);
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
          }

          td {
            border-bottom: 1px solid @merchant-divider;
            padding: 14px 0;
          }
        }
      }

      .el-tag {
        border-radius: 6px;
        padding: 6px 12px;
        font-weight: 600;
        border: none;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      }

      .el-button {
        border-radius: 6px;
        font-weight: 500;
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }
      }
    }

    .empty-state {
      padding: 80px 20px;
      text-align: center;
      color: @merchant-text-muted;
      font-size: 1.071rem /* 原值: 15px */;
      background: linear-gradient(135deg, @merchant-surface-alt 0%, @merchant-surface 100%);
      border-radius: 8px;
      border: 2px dashed @merchant-border;

      span {
        font-size: 64px;
        display: block;
        margin-bottom: 16px;
        opacity: 0.6;
      }

      p {
        margin: 0;
        line-height: 1.8;
        font-weight: 500;
      }
    }
  }

  :deep(.el-dialog) {
    border-radius: 16px;
    box-shadow: 0 12px 48px rgba(0, 0, 0, 0.2);
    overflow: hidden;

    .el-dialog__header {
      padding: 24px 28px;
      border-bottom: 2px solid @merchant-border;
      background: linear-gradient(135deg, @merchant-surface-alt 0%, @merchant-surface 100%);

      .el-dialog__title {
        font-size: 1.429rem /* 原值: 20px */;
        font-weight: 700;
        color: @merchant-text;
      }

      .el-dialog__headerbtn {
        top: 24px;
        right: 24px;
        width: 32px;
        height: 32px;
        border-radius: 50%;
        transition: all 0.3s ease;

        &:hover {
          background-color: @merchant-surface-alt;
        }

        .el-dialog__close {
          font-size: 1.286rem /* 原值: 18px */;
          color: @merchant-text-muted;
        }
      }
    }

    .el-dialog__body {
      padding: 28px;

      .el-form {
        .el-form-item {
          margin-bottom: 24px;

          .el-form-item__label {
            font-weight: 600;
            color: @merchant-text-sec;
            font-size: 1rem /* 原值: 14px */;
          }

          .el-input__inner,
          .el-textarea__inner {
            border-radius: 8px;
            transition: all 0.3s ease;
            font-size: 1rem /* 原值: 14px */;

            &:hover {
              border-color: @merchant-text-muted;
            }

            &:focus {
              border-color: @merchant-info;
              box-shadow: 0 0 0 3px fade(@merchant-info, 10%);
            }
          }

          .el-textarea__inner {
            padding: 12px;
            line-height: 1.6;
          }

          .el-select {
            width: 100%;

            .el-input__inner {
              cursor: pointer;
            }
          }

          .el-date-editor {
            width: 100%;
          }
        }
      }
    }

    .el-dialog__footer {
      padding: 20px 28px;
      border-top: 2px solid @merchant-border;
      background: linear-gradient(135deg, @merchant-surface 0%, @merchant-surface-alt 100%);

      .dialog-footer {
        display: flex;
        justify-content: flex-end;
        gap: 16px;

        .el-button {
          border-radius: 8px;
          padding: 12px 24px;
          font-weight: 600;
          font-size: 1rem /* 原值: 14px */;
          transition: all 0.3s ease;
          border: 2px solid transparent;

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
          }

          &.el-button--primary {
            background: linear-gradient(135deg, @merchant-info 0%, lighten(@merchant-info, 15%) 100%);
            border: none;

            &:hover {
              background: linear-gradient(135deg, lighten(@merchant-info, 15%) 0%, @merchant-info 100%);
            }
          }

          &.el-button--default {
            border-color: @merchant-border;

            &:hover {
              border-color: @merchant-text-muted;
              background-color: @merchant-surface-alt;
            }
          }
        }
      }
    }
  }
}
</style>
