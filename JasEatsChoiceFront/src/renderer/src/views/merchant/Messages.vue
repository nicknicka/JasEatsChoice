<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../store/authStore'
import api, { decodeJwt } from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import {
  Bell,
  ChatDotRound,
  Notification,
  ChatLineSquare,
  Refresh,
  Check,
  Filter
} from '@element-plus/icons-vue'

const router = useRouter()

// 消息分类映射
const messageCategories = {
  all: { text: '所有消息', icon: ChatDotRound },
  system: { text: '系统通知', icon: Notification },
  order: { text: '订单消息', icon: Bell },
  comment: { text: '评价消息', icon: ChatLineSquare }
}

// 消息数据，将从后端API获取
const messages = ref([])
const selectedMessage = ref(null)
const activeCategory = ref('all')
const loading = ref(false)

// 筛选后的消息
const filteredMessages = ref([])

// 未读消息统计
const unreadCounts = ref({
  system: 0,
  order: 0,
  comment: 0,
  total: 0
})

// 数字动画
const animatedValues = ref({
  total: 0,
  system: 0,
  order: 0,
  comment: 0
})

// 动画数字
const animateValue = (key, endValue, duration = 1000) => {
  const startValue = animatedValues.value[key]
  const startTime = performance.now()

  const animate = (currentTime) => {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)
    const easeOutQuart = 1 - Math.pow(1 - progress, 4)
    animatedValues.value[key] = Math.floor(startValue + (endValue - startValue) * easeOutQuart)

    if (progress < 1) {
      requestAnimationFrame(animate)
    } else {
      animatedValues.value[key] = endValue
    }
  }

  requestAnimationFrame(animate)
}

// 计算未读消息数量
const calculateUnreadCounts = () => {
  unreadCounts.value = {
    total: messages.value.filter((msg) => !msg.isRead).length,
    system: messages.value.filter((msg) => msg.type === 'system' && !msg.isRead).length,
    order: messages.value.filter((msg) => msg.type === 'order' && !msg.isRead).length,
    comment: messages.value.filter((msg) => msg.type === 'comment' && !msg.isRead).length
  }
}

// 总计统计
const totalStats = computed(() => {
  return {
    total: messages.value.length,
    todayMessages: messages.value.filter((msg) => {
      const msgDate = new Date(msg.time)
      const today = new Date()
      return msgDate.toDateString() === today.toDateString()
    }).length
  }
})

// 监听未读消息变化，触发动画
import { watch } from 'vue'
watch(
  unreadCounts,
  (newVal) => {
    animateValue('total', newVal.total)
    animateValue('system', newVal.system)
    animateValue('order', newVal.order)
    animateValue('comment', newVal.comment)
  },
  { deep: true }
)

// 更新筛选
const updateFilter = () => {
  filteredMessages.value = messages.value.filter((message) => {
    return activeCategory.value === 'all' || message.type === activeCategory.value
  })
  calculateUnreadCounts() // 更新未读消息统计
}

// 刷新消息
const refreshMessages = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('刷新成功')
  }, 500)
}

// 页面加载时初始化
onMounted(() => {
  // 从后端API加载实际消息数据
  // 从JWT令牌中获取用户ID
  const authStore = useAuthStore()
  const token = authStore.token
  let userId = 1 // 默认值

  if (token) {
    const decodedToken = decodeJwt(token)
    if (decodedToken && decodedToken.userId) {
      userId = decodedToken.userId
    }
  } else {
    // 无法获取用户ID，弹出提示框要求重新登录
    ElMessageBox.alert('无法获取用户ID，请重新登录', '身份验证失败', {
      confirmButtonText: '重新登录',
      type: 'error',
      closeOnClickModal: false,
      closeOnPressEscape: false
    })
      .then(() => {
        // 用户点击重新登录按钮，清除本地存储并跳转到登录页面
        const authStore = useAuthStore()
        authStore.clearAuth()
        router.push('/login')
      })
      .catch(() => {
        // 点击取消按钮的处理，也可以跳转到登录页面
        const authStore = useAuthStore()
        authStore.clearAuth()
        router.push('/login')
      })
  }

  api
    .get(API_CONFIG.message.list, {
      params: { userId }
    })
    .then((response) => {
      if (response.data && response.data.success) {
        // 转换后端返回的数据格式以匹配前端期望的字段
        const formattedMessages = response.data.data.map((message) => ({
          id: message.id,
          // 后端返回的content作为前端的title
          title: message.content,
          content: message.content,
          // 后端返回的senderName作为前端的sender
          sender: message.senderName,
          // 后端返回的createTime作为前端的time
          time: message.createTime,
          // 后端返回的readStatus作为前端的isRead
          isRead: message.readStatus,
          // 暂时默认所有消息类型为system
          type: 'system'
        }))

        messages.value = formattedMessages
        filteredMessages.value = [...messages.value]
        calculateUnreadCounts() // 初始化未读消息统计
      }
    })
    .catch((error) => {
      console.error('加载消息失败:', error)
      ElMessage.error('加载消息失败，请稍后重试')
    })
})

// 查看消息详情
const viewMessageDetail = (message) => {
  // 检查是否是订单消息，如果是则导航到订单详情页
  if (message.type === 'order') {
    // 从消息标题或内容中提取订单号
    const orderIdMatch = message.title.match(/订单号(?:JD)?(\\d+)/)
    if (orderIdMatch) {
      const orderId = orderIdMatch[1]
      router.push(`/merchant/home/order-detail/${orderId}`)
      return
    }
  }
  // 普通消息则显示详情
  selectedMessage.value = message
  // 自动标记为已读
  if (!message.isRead) {
    message.isRead = true
    ElMessage.success('消息已标记为已读')
    updateFilter() // 刷新筛选后的列表以更新状态
  }
}

// 返回消息列表
const backToList = () => {
  selectedMessage.value = null
}

// 标记为已读
const markAsRead = (message) => {
  message.isRead = true
  ElMessage.success('消息已标记为已读')
  updateFilter()
}

// 全部标记为已读
const markAllAsRead = () => {
  filteredMessages.value.forEach((message) => {
    message.isRead = true
  })
  ElMessage.success('所有消息已标记为已读')
  updateFilter()
}
</script>

<template>
  <div class="messages-management-container">
    <!-- 头部 -->
    <div class="messages-header">
      <div class="header-left">
        <h3 class="page-title">消息中心</h3>
        <p class="page-subtitle">管理您的所有通知和消息</p>
      </div>
      <div class="header-right" v-if="!selectedMessage">
        <el-button type="success" @click="markAllAsRead" :icon="Check"> 全部标记为已读 </el-button>
        <el-button type="default" @click="refreshMessages" :loading="loading" :icon="Refresh">
          刷新
        </el-button>
        <CommonBackButton />
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section" v-if="!selectedMessage">
      <div class="stat-card total">
        <div class="stat-icon">
          <el-icon :size="28"><ChatDotRound /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value animated-number">{{ totalStats.total }}</div>
          <div class="stat-label">总消息</div>
        </div>
      </div>

      <div class="stat-card unread">
        <div class="stat-icon">
          <el-icon :size="28"><Bell /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value animated-number">{{ animatedValues.total }}</div>
          <div class="stat-label">未读消息</div>
        </div>
      </div>

      <div class="stat-card system">
        <div class="stat-icon">
          <el-icon :size="28"><Notification /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value animated-number">{{ animatedValues.system }}</div>
          <div class="stat-label">系统通知</div>
        </div>
      </div>

      <div class="stat-card order">
        <div class="stat-icon">
          <el-icon :size="28"><Bell /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value animated-number">{{ animatedValues.order }}</div>
          <div class="stat-label">订单消息</div>
        </div>
      </div>

      <div class="stat-card comment">
        <div class="stat-icon">
          <el-icon :size="28"><ChatLineSquare /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value animated-number">{{ animatedValues.comment }}</div>
          <div class="stat-label">评价消息</div>
        </div>
      </div>
    </div>

    <div class="messages-content">
      <!-- 消息分类与列表 -->
      <div class="messages-list-container" v-if="!selectedMessage">
        <!-- 分类筛选 -->
        <div class="category-section">
          <div class="filter-header">
            <el-icon class="filter-icon"><Filter /></el-icon>
            <span class="filter-label">消息分类</span>
          </div>
          <div class="category-tags">
            <div
              v-for="category in ['all', 'system', 'order', 'comment']"
              :key="category"
              :class="[
                'category-tag',
                `category-tag-${category}`,
                { active: activeCategory === category }
              ]"
              @click="
                () => {
                  activeCategory = category
                  updateFilter()
                }
              "
            >
              <el-icon class="tag-icon">
                <component :is="messageCategories[category].icon" />
              </el-icon>
              <span class="tag-text">{{ messageCategories[category].text }}</span>
              <el-badge
                v-if="category !== 'all' && unreadCounts[category] > 0"
                :value="unreadCounts[category]"
                type="danger"
                class="tag-badge"
              />
            </div>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="messages-list" v-loading="loading">
          <div
            v-for="message in filteredMessages"
            :key="message.id"
            :class="['message-item', { 'unread-message': !message.isRead }]"
            @click="viewMessageDetail(message)"
          >
            <div class="message-left">
              <div class="message-icon" :class="`icon-${message.type}`">
                <el-icon :size="20">
                  <component :is="messageCategories[message.type]?.icon || Notification" />
                </el-icon>
              </div>
              <div class="message-indicator" v-if="!message.isRead"></div>
            </div>

            <div class="message-content">
              <div class="message-title">{{ message.title }}</div>
              <div class="message-preview" v-if="message.content">
                {{ message.content.substring(0, 50) }}...
              </div>
              <div class="message-meta">
                <span class="message-time">{{ message.time }}</span>
                <el-tag :type="message.isRead ? 'success' : 'warning'" size="small">
                  {{ message.isRead ? '已读' : '未读' }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- 空数据提示 -->
        <div v-if="filteredMessages.length === 0 && !loading" class="empty-messages">
          <el-empty description="暂无消息"></el-empty>
        </div>
      </div>

      <!-- 消息详情 -->
      <div class="message-detail-container" v-if="selectedMessage">
        <div class="detail-header">
          <div class="detail-title">
            <div class="title-icon" :class="`icon-${selectedMessage.type}`">
              <el-icon :size="24">
                <component :is="messageCategories[selectedMessage.type]?.icon || Notification" />
              </el-icon>
            </div>
            <h3>{{ selectedMessage.title }}</h3>
            <el-tag :type="selectedMessage.isRead ? 'success' : 'warning'">
              {{ selectedMessage.isRead ? '已读' : '未读' }}
            </el-tag>
          </div>
          <div class="detail-meta">
            <div class="meta-item" v-if="selectedMessage.sender">
              <span class="meta-label">发送者:</span>
              <span class="meta-value">{{ selectedMessage.sender }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">时间:</span>
              <span class="meta-value">{{ selectedMessage.time }}</span>
            </div>
          </div>
        </div>
        <div class="detail-content">
          {{ selectedMessage.content }}
        </div>
        <div class="detail-actions">
          <CommonBackButton @click="backToList" :useRouterBack="false" text="返回列表" />
          <el-button
            v-if="!selectedMessage.isRead"
            type="success"
            @click="markAsRead(selectedMessage)"
          >
            标记为已读
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.messages-management-container {
  padding: 0 20px 20px 20px;

  .messages-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24px 28px;
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    border-radius: 20px;
    box-shadow: 0 8px 24px rgba(59, 130, 246, 0.25);
    margin-bottom: 24px;
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      top: -50%;
      right: -10%;
      width: 300px;
      height: 300px;
      background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
      border-radius: 50%;
    }

    .header-left {
      display: flex;
      flex-direction: column;
      gap: 6px;
      position: relative;
      z-index: 1;

      .page-title {
        font-size: 26px;
        font-weight: 700;
        margin: 0;
        color: #ffffff;
        letter-spacing: 0.8px;
        text-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
      }

      .page-subtitle {
        font-size: 1rem /* 原值: 14px */;
        color: rgba(255, 255, 255, 0.95);
        margin: 0;
        font-weight: 400;
      }
    }

    .header-right {
      display: flex;
      gap: 12px;
      align-items: center;
      position: relative;
      z-index: 1;

      :deep(.el-button) {
        backdrop-filter: blur(12px);
        background: rgba(255, 255, 255, 0.15);
        border: 1px solid rgba(255, 255, 255, 0.35);
        color: #ffffff;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        font-weight: 500;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

        &:hover {
          transform: translateY(-2px);
          background: rgba(255, 255, 255, 0.25);
          box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
        }

        &:active {
          transform: translateY(0);
        }
      }
    }
  }

  // 统计卡片
  .stats-section {
    display: flex;
    justify-content: space-between;
    align-items: stretch;
    padding: 20px;
    background: linear-gradient(135deg, #f8f9fa 0%, #f3f4f6 100%);
    border: 1px solid #e8eef5;
    border-radius: 20px;
    margin-bottom: 24px;
    flex-wrap: wrap;
    gap: 16px;
    box-shadow: 0 2px 16px rgba(0, 0, 0, 0.04);

    .stat-card {
      display: flex;
      align-items: center;
      gap: 18px;
      padding: 22px 24px;
      background: #ffffff;
      border-radius: 18px;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
      transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
      cursor: pointer;
      flex: 1;
      min-width: 160px;
      border: 1px solid #e8eef5;
      position: relative;
      overflow: hidden;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 0;
        bottom: 0;
        width: 5px;
        transition: width 0.3s ease;
      }

      &:hover {
        transform: translateY(-6px) scale(1.01);
        box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);

        &::before {
          width: 6px;
        }

        .stat-icon {
          transform: scale(1.1) rotate(5deg);
        }
      }

      &:active {
        transform: translateY(-3px) scale(1.005);
      }

      .stat-icon {
        width: 56px;
        height: 56px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        border-radius: 16px;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

        .el-icon {
          color: inherit;
        }
      }

      .stat-content {
        flex: 1;

        .stat-value {
          font-size: 2rem /* 原值: 28px */;
          font-weight: 700;
          color: #1f2937;
          line-height: 1.1;
          margin-bottom: 4px;
        }

        .stat-label {
          font-size: 0.929rem /* 原值: 13px */;
          color: #6b7280;
          font-weight: 500;
          letter-spacing: 0.3px;
        }
      }

      &.total {
        &::before {
          background: linear-gradient(180deg, #3b82f6 0%, #2563eb 100%);
        }
        .stat-icon {
          background: linear-gradient(
            135deg,
            rgba(59, 130, 246, 0.12) 0%,
            rgba(37, 99, 235, 0.08) 100%
          );
          color: #3b82f6;
          box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
        }
        .stat-value {
          background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
        }
      }

      &.unread {
        &::before {
          background: linear-gradient(180deg, #f56c6c 0%, #ff8787 100%);
        }
        .stat-icon {
          background: linear-gradient(
            135deg,
            rgba(245, 108, 108, 0.12) 0%,
            rgba(255, 135, 135, 0.08) 100%
          );
          color: #f56c6c;
          box-shadow: 0 4px 12px rgba(245, 108, 108, 0.15);
        }
        .stat-value {
          color: #f56c6c;
        }
      }

      &.system {
        &::before {
          background: linear-gradient(180deg, #e6a23c 0%, #f0a858 100%);
        }
        .stat-icon {
          background: linear-gradient(
            135deg,
            rgba(230, 162, 60, 0.12) 0%,
            rgba(240, 168, 88, 0.08) 100%
          );
          color: #e6a23c;
          box-shadow: 0 4px 12px rgba(230, 162, 60, 0.15);
        }
        .stat-value {
          color: #e6a23c;
        }
      }

      &.order {
        &::before {
          background: linear-gradient(180deg, #409eff 0%, #66b1ff 100%);
        }
        .stat-icon {
          background: linear-gradient(
            135deg,
            rgba(64, 158, 255, 0.12) 0%,
            rgba(102, 177, 255, 0.08) 100%
          );
          color: #409eff;
          box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
        }
        .stat-value {
          color: #409eff;
        }
      }

      &.comment {
        &::before {
          background: linear-gradient(180deg, #67c23a 0%, #7bcf58 100%);
        }
        .stat-icon {
          background: linear-gradient(
            135deg,
            rgba(103, 194, 58, 0.12) 0%,
            rgba(123, 207, 88, 0.08) 100%
          );
          color: #67c23a;
          box-shadow: 0 4px 12px rgba(103, 194, 58, 0.15);
        }
        .stat-value {
          color: #67c23a;
        }
      }
    }
  }

  .messages-content {
    .messages-list-container {
      .category-section {
        display: flex;
        flex-direction: column;
        gap: 12px;
        padding: 16px 20px;
        background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
        border-radius: 12px;
        margin-bottom: 16px;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
        border: 1px solid #e8eef5;

        .filter-header {
          display: flex;
          align-items: center;
          gap: 6px;

          .filter-icon {
            font-size: 1.143rem /* 原值: 16px */;
            color: #3b82f6;
          }

          .filter-label {
            font-size: 0.929rem /* 原值: 13px */;
            font-weight: 600;
            color: #303133;
          }
        }

        .category-tags {
          display: flex;
          flex-wrap: wrap;
          gap: 8px;

          .category-tag {
            cursor: pointer;
            transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
            padding: 6px 12px;
            font-size: 0.929rem /* 原值: 13px */;
            font-weight: 500;
            border-radius: 8px;
            display: inline-flex;
            align-items: center;
            gap: 6px;
            user-select: none;
            position: relative;

            .tag-icon {
              font-size: 1rem /* 原值: 14px */;
            }

            .tag-text {
              font-size: 0.929rem /* 原值: 13px */;
            }

            .tag-badge {
              margin-left: 4px;
            }

            &:hover {
              transform: translateY(-1px);
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
            }

            &.category-tag-all {
              background: #f0f2f5;
              color: #606266;
              border: 1px solid #dcdfe6;
              &.active {
                background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
                color: #ffffff;
                border-color: #3b82f6;
                box-shadow: 0 2px 6px rgba(59, 130, 246, 0.3);
              }
            }

            &.category-tag-system {
              background: #fff7e6;
              color: #e6a23c;
              border: 1px solid #ffd591;
              &.active {
                background: linear-gradient(135deg, #e6a23c 0%, #f0a858 100%);
                color: #ffffff;
              }
            }

            &.category-tag-order {
              background: #e6f7ff;
              color: #409eff;
              border: 1px solid #91d5ff;
              &.active {
                background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
                color: #ffffff;
              }
            }

            &.category-tag-comment {
              background: #f6ffed;
              color: #67c23a;
              border: 1px solid #b7eb8f;
              &.active {
                background: linear-gradient(135deg, #67c23a 0%, #7bcf58 100%);
                color: #ffffff;
              }
            }
          }
        }
      }

      .messages-list {
        .message-item {
          display: flex;
          align-items: flex-start;
          padding: 18px 22px;
          border: 2px solid #e8eef5;
          border-radius: 14px;
          margin-bottom: 12px;
          background-color: #fff;
          cursor: pointer;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
          position: relative;
          overflow: hidden;

          &::before {
            content: '';
            position: absolute;
            left: 0;
            top: 0;
            bottom: 0;
            width: 4px;
            background: #e8eef5;
            transition: all 0.3s ease;
            border-radius: 14px 0 0 14px;
          }

          &:hover {
            box-shadow: 0 8px 28px rgba(0, 0, 0, 0.1);
            border-color: #cbd5e1;
            transform: translateY(-3px);

            .message-icon {
              transform: scale(1.08);
            }

            .message-title {
              color: #3b82f6;
            }
          }

          &:active {
            transform: translateY(-1px);
          }

          &.unread-message {
            background: linear-gradient(to right, #fef2f2 0%, #ffffff 35%);
            border-color: #fca5a5;

            &::before {
              background: linear-gradient(180deg, #f56c6c 0%, #ff8787 100%);
              box-shadow: 0 0 12px rgba(245, 108, 108, 0.4);
            }

            .message-title {
              color: #1f2937;
              font-weight: 700;
            }

            .message-preview {
              color: #4b5563;
            }
          }

          .message-left {
            position: relative;
            margin-right: 18px;

            .message-icon {
              width: 48px;
              height: 48px;
              display: flex;
              align-items: center;
              justify-content: center;
              border-radius: 14px;
              transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

              &.icon-system {
                background: linear-gradient(
                  135deg,
                  rgba(230, 162, 60, 0.12) 0%,
                  rgba(230, 162, 60, 0.06) 100%
                );
                color: #e6a23c;
                box-shadow: 0 4px 12px rgba(230, 162, 60, 0.12);
              }

              &.icon-order {
                background: linear-gradient(
                  135deg,
                  rgba(64, 158, 255, 0.12) 0%,
                  rgba(64, 158, 255, 0.06) 100%
                );
                color: #409eff;
                box-shadow: 0 4px 12px rgba(64, 158, 255, 0.12);
              }

              &.icon-comment {
                background: linear-gradient(
                  135deg,
                  rgba(103, 194, 58, 0.12) 0%,
                  rgba(103, 194, 58, 0.06) 100%
                );
                color: #67c23a;
                box-shadow: 0 4px 12px rgba(103, 194, 58, 0.12);
              }
            }

            .message-indicator {
              position: absolute;
              top: -3px;
              right: -3px;
              width: 12px;
              height: 12px;
              background: linear-gradient(135deg, #f56c6c 0%, #ff8787 100%);
              border: 2.5px solid #fff;
              border-radius: 50%;
              box-shadow: 0 2px 8px rgba(245, 108, 108, 0.4);
              animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
            }
          }

          .message-content {
            flex: 1;

            .message-title {
              font-size: 1.071rem /* 原值: 15px */;
              font-weight: 600;
              margin-bottom: 7px;
              color: #1f2937;
              transition: color 0.2s ease;
            }

            .message-preview {
              font-size: 0.929rem /* 原值: 13px */;
              color: #6b7280;
              margin-bottom: 10px;
              line-height: 1.6;
            }

            .message-meta {
              display: flex;
              justify-content: space-between;
              align-items: center;
              font-size: 0.857rem /* 原值: 12px */;
              color: #9ca3af;
            }
          }
        }
      }

      .empty-messages {
        text-align: center;
        margin-top: 50px;
      }
    }

    .message-detail-container {
      padding: 0;

      .detail-header {
        margin-bottom: 20px;
        padding: 20px;
        background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
        border-radius: 12px;
        border: 1px solid #e8eef5;

        .detail-title {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 15px;

          .title-icon {
            width: 40px;
            height: 40px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 10px;

            &.icon-system {
              background: linear-gradient(
                135deg,
                rgba(230, 162, 60, 0.15) 0%,
                rgba(230, 162, 60, 0.08) 100%
              );
              color: #e6a23c;
            }

            &.icon-order {
              background: linear-gradient(
                135deg,
                rgba(64, 158, 255, 0.15) 0%,
                rgba(64, 158, 255, 0.08) 100%
              );
              color: #409eff;
            }

            &.icon-comment {
              background: linear-gradient(
                135deg,
                rgba(103, 194, 58, 0.15) 0%,
                rgba(103, 194, 58, 0.08) 100%
              );
              color: #67c23a;
            }
          }

          h3 {
            font-size: 1.429rem /* 原值: 20px */;
            margin: 0;
            flex: 1;
          }
        }

        .detail-meta {
          display: flex;
          flex-direction: column;
          gap: 8px;
          font-size: 1rem /* 原值: 14px */;

          .meta-item {
            display: flex;
            align-items: center;
            gap: 8px;

            .meta-label {
              color: #909399;
              font-weight: 500;
              min-width: 60px;
            }

            .meta-value {
              color: #303133;
            }
          }
        }
      }

      .detail-content {
        font-size: 1.143rem /* 原值: 16px */;
        line-height: 1.8;
        margin-bottom: 30px;
        padding: 24px;
        background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
        border-radius: 12px;
        border: 1px solid #e8eef5;
        color: #303133;
      }

      .detail-actions {
        display: flex;
        justify-content: flex-end;
        gap: 12px;
      }
    }
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.8;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .messages-management-container {
    padding: 12px;

    .messages-header {
      flex-direction: column;
      gap: 12px;
      align-items: stretch;
    }

    .stats-section {
      gap: 10px;

      .stat-card {
        min-width: calc(50% - 5px);
        padding: 16px;

        .stat-value {
          font-size: 1.429rem /* 原值: 20px */ !important;
        }
      }
    }

    .messages-content {
      .messages-list-container {
        .category-section {
          padding: 12px;

          .category-tags {
            .category-tag {
              padding: 4px 10px;
              font-size: 0.857rem /* 原值: 12px */;
            }
          }
        }

        .messages-list {
          .message-item {
            padding: 14px;

            .message-left {
              .message-icon {
                width: 38px;
                height: 38px;
              }
            }

            .message-content {
              .message-title {
                font-size: 1rem /* 原值: 14px */;
              }

              .message-preview {
                font-size: 0.857rem /* 原值: 12px */;
              }
            }
          }
        }
      }
    }
  }
}
</style>
