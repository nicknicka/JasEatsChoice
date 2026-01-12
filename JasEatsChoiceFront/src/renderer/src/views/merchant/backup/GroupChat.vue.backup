<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

// 模拟群组列表
const groupList = ref([
  {
    id: 1,
    groupName: '商家交流群',
    groupAvatar: 'https://picsum.photos/id/100/40/40',
    lastMessage: '大家最近生意怎么样？',
    time: '2024-11-21 14:30',
    unreadCount: 2,
    memberCount: 25
  },
  {
    id: 2,
    groupName: '新品推广群',
    groupAvatar: 'https://picsum.photos/id/101/40/40',
    lastMessage: '新品上线，欢迎大家体验！',
    time: '2024-11-21 14:15',
    unreadCount: 0,
    memberCount: 18
  },
  {
    id: 3,
    groupName: '平台通知群',
    groupAvatar: 'https://picsum.photos/id/102/40/40',
    lastMessage: '明天系统维护，请提前做好准备！',
    time: '2024-11-21 13:45',
    unreadCount: 0,
    memberCount: 50
  }
])

// 模拟聊天记录
const chatMessages = ref([
  {
    id: 1,
    sender: '商家A',
    content: '大家最近生意怎么样？',
    time: '2024-11-21 14:30',
    isRead: false
  },
  {
    id: 2,
    sender: '商家B',
    content: '还行，比上个月好一点',
    time: '2024-11-21 14:31',
    isRead: false
  },
  {
    id: 3,
    sender: '商家C',
    content: '我这里也不错',
    time: '2024-11-21 14:32',
    isRead: true
  }
])

// 当前选中的群组
const selectedGroup = ref(null)

// 新消息内容
const newMessage = ref('')

// 页面加载
onMounted(() => {
  // 默认选中第一个群组
  if (groupList.value.length > 0) {
    selectedGroup.value = groupList.value[0]
  }
})

// 选择群组
const selectGroup = (group) => {
  selectedGroup.value = group
  // 清空未读消息
  if (group.unreadCount > 0) {
    group.unreadCount = 0
    ElMessage.success('消息已标记为已读')
  }
}

// 发送消息
const sendMessage = () => {
  if (!newMessage.value.trim() || !selectedGroup.value) {
    return
  }

  // 创建新消息
  const message = {
    id: Date.now(),
    sender: '我',
    content: newMessage.value.trim(),
    time: new Date().toISOString().slice(0, 19).replace('T', ' '),
    isRead: true
  }

  // 添加到聊天记录
  chatMessages.value.push(message)

  // 更新群组列表的最后一条消息
  selectedGroup.value.lastMessage = message.content
  selectedGroup.value.time = message.time

  // 清空输入框
  newMessage.value = ''
}
</script>

<template>
  <div class="group-chat-container">
    <div class="chat-header">
      <h3 class="page-title">【商家群聊】</h3>
    </div>

    <div class="chat-content">
      <!-- 群组列表 -->
      <div class="group-list">
        <div
          v-for="group in groupList"
          :key="group.id"
          class="group-item"
          :class="{ active: selectedGroup?.id === group.id }"
          @click="selectGroup(group)"
        >
          <div class="group-avatar">
            <img :src="group.groupAvatar" :alt="group.groupName" />
          </div>
          <div class="group-info">
            <div class="group-name-count">
              <span class="group-name">{{ group.groupName }}</span>
              <span class="member-count"> ({{ group.memberCount }}人)</span>
            </div>
            <div class="last-message">
              {{ group.lastMessage }}
            </div>
            <div class="time">
              {{ group.time }}
            </div>
          </div>
          <div v-if="group.unreadCount > 0" class="unread-count">
            {{ group.unreadCount }}
          </div>
        </div>
      </div>

      <!-- 聊天内容 -->
      <div class="chat-area" v-if="selectedGroup">
        <div class="chat-header">
          <div class="group-info">
            <div class="group-avatar">
              <img :src="selectedGroup.groupAvatar" :alt="selectedGroup.groupName" />
            </div>
            <div class="group-name-count">
              <span class="group-name">{{ selectedGroup.groupName }}</span>
              <span class="member-count"> ({{ selectedGroup.memberCount }}人)</span>
            </div>
          </div>
        </div>

        <div class="messages-container">
          <div
            v-for="message in chatMessages"
            :key="message.id"
            class="message-item"
            :class="{
              'others-message': message.sender !== '我',
              'my-message': message.sender === '我'
            }"
          >
            <div class="message-header" v-if="message.sender !== '我'">
              <span class="sender-name">{{ message.sender }}</span>
            </div>
            <div class="message-content">
              {{ message.content }}
              <div class="message-time">{{ message.time }}</div>
            </div>
          </div>
        </div>

        <div class="message-input-container">
          <el-input
            v-model="newMessage"
            type="textarea"
            placeholder="输入消息内容..."
            :rows="2"
            @keyup.enter="sendMessage"
          />
          <el-button type="primary" @click="sendMessage">发送</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.group-chat-container {
  padding: 0 20px 20px 20px;
  height: calc(100vh - 60px);

  .chat-header {
    margin-bottom: 20px;
    .page-title {
      font-size: 18px;
      font-weight: 600;
      margin: 0;
    }
  }

  .chat-content {
    display: flex;
    gap: 20px;
    height: calc(100vh - 120px);

    .group-list {
      width: 300px;
      border: 1px solid #e4e7ed;
      border-radius: 4px;
      overflow-y: auto;

      .group-item {
        display: flex;
        align-items: flex-start;
        padding: 12px;
        cursor: pointer;
        border-bottom: 1px solid #e4e7ed;
        transition: background-color 0.3s;

        &:hover {
          background-color: #f5f7fa;
        }

        &.active {
          background-color: #ecf5ff;
        }

        .group-avatar {
          margin-right: 12px;
          img {
            width: 48px;
            height: 48px;
            border-radius: 8px;
            object-fit: cover;
          }
        }

        .group-info {
          flex: 1;

          .group-name-count {
            margin-bottom: 4px;

            .group-name {
              font-weight: 500;
            }

            .member-count {
              font-size: 12px;
              color: #909399;
            }
          }

          .last-message {
            font-size: 14px;
            color: #606266;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            margin-bottom: 4px;
          }

          .time {
            font-size: 12px;
            color: #909399;
            text-align: right;
          }
        }

        .unread-count {
          background-color: #f56c6c;
          color: #fff;
          border-radius: 10px;
          padding: 0 6px;
          font-size: 12px;
          align-self: center;
        }
      }
    }

    .chat-area {
      flex: 1;
      border: 1px solid #e4e7ed;
      border-radius: 4px;
      display: flex;
      flex-direction: column;

      .chat-header {
        padding: 12px;
        border-bottom: 1px solid #e4e7ed;
        .group-info {
          display: flex;
          align-items: center;

          .group-avatar {
            margin-right: 8px;
            img {
              width: 32px;
              height: 32px;
              border-radius: 4px;
              object-fit: cover;
            }
          }

          .group-name-count {
            .group-name {
              font-weight: 500;
            }

            .member-count {
              font-size: 12px;
              color: #909399;
            }
          }
        }
      }

      .messages-container {
        flex: 1;
        padding: 16px;
        overflow-y: auto;

        .message-item {
          margin-bottom: 16px;
          max-width: 70%;

          .message-header {
            margin-bottom: 4px;
            .sender-name {
              font-size: 12px;
              color: #666;
            }
          }

          .message-content {
            border-radius: 10px;
            padding: 10px;

            .message-time {
              text-align: right;
              font-size: 12px;
              margin-top: 4px;
            }
          }

          &.others-message {
            align-self: flex-start;

            .message-content {
              background-color: #fff;
              border: 1px solid #ddd;

              .message-time {
                color: #909399;
              }
            }
          }

          &.my-message {
            align-self: flex-end;

            .message-content {
              background-color: #67c23a;
              color: #fff;

              .message-time {
                opacity: 0.8;
              }
            }
          }
        }
      }

      .message-input-container {
        padding: 12px;
        border-top: 1px solid #e4e7ed;
        display: flex;
        gap: 12px;

        .el-input {
          flex: 1;
        }

        button {
          align-self: flex-end;
        }
      }
    }
  }
}
</style>
