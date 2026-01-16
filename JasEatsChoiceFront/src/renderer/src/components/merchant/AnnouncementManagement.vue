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
const currentAnnouncement = ref({
  title: '',
  content: '',
  status: 'active',
  startTime: null,
  endTime: null
})
const isEditingAnnouncement = ref(false)

// 获取公告列表
const getAnnouncements = () => {
  let url = API_CONFIG.merchant.announcements
  url = url.replace('{merchantId}', props.merchantId)
  api
    .get(url)
    .then(function (response) {
      console.log('获取公告列表响应:', response)
      // 兼容不同的响应格式
      if (response && (response.success || response.code === '200')) {
        announcements.value = response.data || []
      } else if (response.data && (response.data.success || response.data.code === '200')) {
        announcements.value = response.data.data || []
      }
    })
    .catch(function (error) {
      console.error('获取公告列表失败:', error)
    })
}

// 打开公告编辑对话框
const openAnnouncementDialog = function (announcement = null) {
  announcementDialogVisible.value = true
  if (announcement) {
    isEditingAnnouncement.value = true
    currentAnnouncement.value = JSON.parse(JSON.stringify(announcement))
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
const saveAnnouncement = function () {
  // 简单验证
  if (!currentAnnouncement.value.title || !currentAnnouncement.value.content) {
    ElMessage.error('请填写完整的公告信息')
    return
  }

  let apiMethod = isEditingAnnouncement.value ? api.put : api.post
  let apiUrl = API_CONFIG.merchant.announcements.replace('{merchantId}', props.merchantId)
  if (isEditingAnnouncement.value) {
    apiUrl = apiUrl + '/' + currentAnnouncement.value.id
  }

  console.log('保存公告，URL:', apiUrl, '数据:', currentAnnouncement.value)

  apiMethod(apiUrl, currentAnnouncement.value)
    .then(function (response) {
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
    })
    .catch(function (error) {
      console.error('保存公告失败:', error)
      ElMessage.error('保存公告失败：' + (error.message || '网络错误'))
    })
}

// 删除公告
const deleteAnnouncement = function (announcement) {
  ElMessageBox.confirm(`确定要删除公告 '${announcement.title}' 吗？`, '删除公告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(function () {
      let url = API_CONFIG.merchant.announcements.replace('{merchantId}', props.merchantId)
      url = url + '/' + announcement.id
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
  let newStatus = announcement.status === 'active' ? 'inactive' : 'active'
  let statusText = newStatus === 'active' ? '已启用' : '已禁用'

  let url = API_CONFIG.merchant.announcements.replace('{merchantId}', props.merchantId)
  url = url + '/' + announcement.id + '/status'

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
        ElMessage.error('切换公告状态失败：' + (response?.message || '未知错误'))
      }
    })
    .catch(function (error) {
      console.error('切换公告状态失败:', error)
      ElMessage.error('切换公告状态失败：' + (error.message || '网络错误'))
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
        <el-table-column prop="title" label="公告标题" min-width="200" />
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
    >
      <el-form :model="currentAnnouncement" label-width="100px" status-icon>
        <el-form-item label="公告标题" prop="title" required>
          <el-input v-model="currentAnnouncement.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="公告内容" prop="content" required>
          <el-input
            v-model="currentAnnouncement.content"
            placeholder="请输入公告内容"
            type="textarea"
            :rows="4"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status" required>
          <el-select v-model="currentAnnouncement.status" placeholder="请选择公告状态">
            <el-option label="已启用" value="active" />
            <el-option label="已禁用" value="inactive" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="currentAnnouncement.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="currentAnnouncement.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="announcementDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveAnnouncement">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
.announcement-section {
  margin-bottom: 24px;
  padding: 24px;
  border: 2px solid #909399;
  border-radius: 12px;
  background: linear-gradient(135deg, #ffffff 0%, #f5f7fa 100%);
  box-shadow: 0 4px 20px rgba(144, 147, 153, 0.15);

  .announcement-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    padding-bottom: 20px;
    border-bottom: 2px solid #e4e7ed;

    .card-title {
      margin: 0;
      font-size: 22px;
      font-weight: 700;
      color: #606266;
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
    background: linear-gradient(135deg, #ffffff 0%, #fafafa 100%);
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    border: 1px solid #e4e7ed;

    :deep(.el-table) {
      border-radius: 8px;
      overflow: hidden;

      &::before {
        display: none;
      }

      .el-table__header-wrapper {
        th {
          background: linear-gradient(135deg, #f5f7fa 0%, #e8eaf0 100%);
          color: #303133;
          font-weight: 700;
          border-bottom: 2px solid #d4d7de;
          padding: 14px 0;
        }
      }

      .el-table__body-wrapper {
        tr {
          transition: all 0.3s ease;
          background-color: #ffffff;

          &:hover {
            background: linear-gradient(90deg, #f5f7fa 0%, #ffffff 100%);
            transform: scale(1.005);
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
          }

          td {
            border-bottom: 1px solid #f0f0f0;
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
      color: #909399;
      font-size: 15px;
      background: linear-gradient(135deg, #f9f9f9 0%, #ffffff 100%);
      border-radius: 8px;
      border: 2px dashed #dcdfe6;

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
      border-bottom: 2px solid #e4e7ed;
      background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);

      .el-dialog__title {
        font-size: 20px;
        font-weight: 700;
        color: #303133;
      }

      .el-dialog__headerbtn {
        top: 24px;
        right: 24px;
        width: 32px;
        height: 32px;
        border-radius: 50%;
        transition: all 0.3s ease;

        &:hover {
          background-color: #f5f7fa;
        }

        .el-dialog__close {
          font-size: 18px;
          color: #909399;
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
            color: #606266;
            font-size: 14px;
          }

          .el-input__inner,
          .el-textarea__inner {
            border-radius: 8px;
            border: 2px solid #dcdfe6;
            transition: all 0.3s ease;
            font-size: 14px;

            &:focus {
              border-color: #909399;
              box-shadow: 0 0 0 3px rgba(144, 147, 153, 0.1);
            }

            &:hover {
              border-color: #c0c4cc;
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
      border-top: 2px solid #e4e7ed;
      background: linear-gradient(135deg, #ffffff 0%, #f9f9f9 100%);

      .dialog-footer {
        display: flex;
        justify-content: flex-end;
        gap: 16px;

        .el-button {
          border-radius: 8px;
          padding: 12px 24px;
          font-weight: 600;
          font-size: 14px;
          transition: all 0.3s ease;
          border: 2px solid transparent;

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
          }

          &.el-button--primary {
            background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
            border: none;

            &:hover {
              background: linear-gradient(135deg, #66b1ff 0%, #409eff 100%);
            }
          }

          &.el-button--default {
            border-color: #dcdfe6;

            &:hover {
              border-color: #c0c4cc;
              background-color: #f5f7fa;
            }
          }
        }
      }
    }
  }
}
</style>
