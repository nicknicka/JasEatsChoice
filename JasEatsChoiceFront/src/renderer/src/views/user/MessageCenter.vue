<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'

// 消息中心数据
const messages = ref([])

// 页面加载时初始化
onMounted(() => {
  // 从localStorage直接获取用户ID，避免JWT解码可能带来的问题
  let userId = parseInt(localStorage.getItem('userId') || '1', 10)

  // 从后端API加载消息数据
  api
    .get(API_CONFIG.message.list, {
      params: { userId }
    })
    .then((response) => {
      if (response && response.code === '200') {
        // 转换后端返回的数据格式以匹配前端期望的字段
        const formattedMessages = response.data.map((message) => ({
          id: message.id,
          // 后端返回的content作为前端的title和content
          title: message.content,
          content: message.content,
          // 后端返回的sendTime作为前端的time
          time: message.sendTime,
          // 后端返回的readStatus作为前端的read (0: 未读, 1: 已读)
          read: message.readStatus,
          // 暂时默认所有消息类型为system，实际应用中应根据后端返回类型映射
          type: message.type || 'system'
        }))

        messages.value = formattedMessages
      }
    })
    .catch((error) => {
      console.error('加载消息失败:', error)
    })
})

// 切换消息分类
const activeTab = ref('all')

// 筛选消息
const filteredMessages = computed(() => {
  if (activeTab.value === 'all') {
    return messages.value
  }
  return messages.value.filter((msg) => msg.type === activeTab.value)
})

// 消息详情模态框
const messageDetail = ref(null)
const showDetailModal = ref(false)

// 查看消息详情
const viewMessage = (message) => {
  message.read = true
  messageDetail.value = message
  showDetailModal.value = true
}

// 批量删除消息
const deleteSelected = () => {
  // 实际应用中实现批量删除功能
  console.log('批量删除消息')
}

// 全部标记为已读
const markAllAsRead = () => {
  messages.value.forEach((message) => {
    message.read = true
  })
  console.log('已将所有消息标记为已读')
}
</script>

<template>
  <div class="message-center-container">
    <h2>消息中心</h2>

    <!-- 消息分类标签页 -->
    <el-tabs v-model:active-name="activeTab" class="message-tabs">
      <el-tab-pane label="全部消息" name="all"></el-tab-pane>
      <el-tab-pane label="订单消息" name="order"></el-tab-pane>
      <el-tab-pane label="系统通知" name="system"></el-tab-pane>
      <el-tab-pane label="优惠活动" name="promotion"></el-tab-pane>
    </el-tabs>

    <!-- 消息列表 -->
    <div class="message-list">
      <el-card
        v-for="message in filteredMessages"
        :key="message.id"
        class="message-card"
        :class="{ unread: !message.read }"
      >
        <div class="message-header">
          <div class="message-type">
            <el-tag
              :type="
                message.type === 'order'
                  ? 'primary'
                  : message.type === 'system'
                    ? 'warning'
                    : 'success'
              "
              size="small"
            >
              {{
                message.type === 'order'
                  ? '订单消息'
                  : message.type === 'system'
                    ? '系统通知'
                    : '优惠活动'
              }}
            </el-tag>
          </div>
          <div class="message-time">{{ message.time }}</div>
        </div>

        <div class="message-content">
          <h3 class="message-title">{{ message.title }}</h3>
          <p class="message-text">{{ message.content }}</p>
        </div>

        <div class="message-actions">
          <el-button type="text" size="small" @click="viewMessage(message)"> 查看详情 </el-button>
          <el-button type="text" size="small" danger> 删除 </el-button>
        </div>
      </el-card>
    </div>

    <!-- 空数据提示 -->
    <el-empty v-if="filteredMessages.length === 0" description="暂无消息"></el-empty>

    <!-- 消息详情模态框 -->
    <el-dialog
      v-model="showDetailModal"
      :title="messageDetail ? messageDetail.title : ''"
      width="600px"
      top="20%"
    >
      <div v-if="messageDetail" class="message-detail-content">
        <div class="detail-header">
          <el-tag
            :type="
              messageDetail.type === 'order'
                ? 'primary'
                : messageDetail.type === 'system'
                  ? 'warning'
                  : 'success'
            "
          >
            {{
              messageDetail.type === 'order'
                ? '订单消息'
                : messageDetail.type === 'system'
                  ? '系统通知'
                  : '优惠活动'
            }}
          </el-tag>
          <span class="detail-time">{{ messageDetail.time }}</span>
        </div>
        <div class="detail-content">
          {{ messageDetail.content }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
.message-center-container {
  padding: 0 20px 20px 20px;

  h2 {
    font-size: 24px;
    margin: 0 0 20px 0;
  }

  .message-tabs {
    margin-bottom: 20px;
  }

  .message-list {
    display: flex;
    flex-direction: column;
    gap: 15px;
  }

  .message-card {
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    &.unread {
      border-left: 4px solid #409eff;
    }

    .message-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;

      .message-time {
        font-size: 14px;
        color: #909399;
      }
    }

    .message-content {
      margin-bottom: 15px;

      .message-title {
        font-size: 16px;
        font-weight: bold;
        margin-bottom: 5px;
        margin: 0;
      }

      .message-text {
        color: #606266;
        font-size: 14px;
        margin: 0;
      }
    }

    .message-actions {
      text-align: right;
    }
  }
  /* 消息详情模态框样式 */
  .message-detail-content {
    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      .detail-time {
        font-size: 14px;
        color: #909399;
      }
    }

    .detail-content {
      font-size: 16px;
      color: #303133;
      line-height: 1.6;
    }
  }
}
</style>
