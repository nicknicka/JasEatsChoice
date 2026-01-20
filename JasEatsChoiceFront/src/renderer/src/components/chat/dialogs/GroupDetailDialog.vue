<template>
  <el-dialog
    v-model="visible"
    title=""
    width="580px"
    :close-on-click-modal="false"
    :close-on-press-escape="true"
    draggable
    class="group-detail-dialog"
  >
    <div v-if="groupInfo" class="group-detail-content">
      <!-- 群头像和基本信息 -->
      <div class="group-header">
        <div class="group-avatar-wrapper">
          <div class="group-avatar">{{ groupInfo.avatar }}</div>
          <div class="avatar-badge">
            <el-icon><User /></el-icon>
            {{ groupInfo.memberCount }}
          </div>
        </div>
        <div class="group-basic-info">
          <div class="group-name">{{ groupInfo.name }}</div>
          <div class="group-id">群ID: {{ groupInfo.id }}</div>
        </div>
      </div>

      <!-- 群统计信息 -->
      <div class="group-stats">
        <div class="stat-item">
          <div class="stat-content">
            <div class="stat-value">{{ groupInfo.memberCount }}</div>
            <div class="stat-label">群成员</div>
          </div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <div class="stat-content">
            <div class="stat-value">{{ groupInfo.creator }}</div>
            <div class="stat-label">群主</div>
          </div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <div class="stat-content">
            <div class="stat-value text-sm">{{ formatDate(groupInfo.createdAt) }}</div>
            <div class="stat-label">创建时间</div>
          </div>
        </div>
      </div>

      <!-- 群成员列表 -->
      <div class="group-members-section">
        <div class="section-header">
          <div class="section-title">
            <el-icon><Avatar /></el-icon>
            群成员列表 ({{ groupInfo.memberCount }})
          </div>
        </div>

        <div class="members-list">
          <div
            v-for="(member, index) in sortedMembers"
            :key="member.id"
            class="member-card"
            :class="{ 'creator-card': member.id === groupInfo.creatorId }"
            :style="{ 'animation-delay': `${index * 0.05}s` }"
          >
            <div class="member-avatar">
              {{ getMemberAvatar(member.name) }}
            </div>
            <div class="member-info">
              <div class="member-name">
                {{ member.name }}
                <el-tag
                  v-if="member.id === groupInfo.creatorId"
                  size="small"
                  effect="dark"
                  class="role-tag creator-tag"
                >
                  👑 群主
                </el-tag>
                <el-tag
                  v-if="member.role === 'admin'"
                  type="warning"
                  size="small"
                  effect="dark"
                  class="role-tag"
                >
                  管理员
                </el-tag>
                <el-tag
                  v-if="member.isCurrentUser"
                  type="success"
                  size="small"
                  effect="dark"
                  class="role-tag"
                >
                  我
                </el-tag>
              </div>
              <div class="member-id">ID: {{ member.id }}</div>
            </div>
            <div class="member-role-icon">
              <el-icon v-if="member.role === 'admin'" class="crown-icon"><Trophy /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="handleClose">
          <el-icon><Check /></el-icon>
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { User, Avatar, Check, Trophy } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  groupInfo: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue'])

const visible = ref(props.modelValue)

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 对成员进行排序：群主在前，管理员其次，普通成员最后
const sortedMembers = computed(() => {
  if (!props.groupInfo?.memberDetails) return []

  return [...props.groupInfo.memberDetails].sort((a, b) => {
    // 群主（creatorId对应的成员）排最前
    if (a.id === props.groupInfo.creatorId) return -1
    if (b.id === props.groupInfo.creatorId) return 1

    // 管理员排第二
    if (a.role === 'admin' && b.role !== 'admin') return -1
    if (b.role === 'admin' && a.role !== 'admin') return 1

    // 当前用户往前排一点
    if (a.isCurrentUser) return -1
    if (b.isCurrentUser) return 1

    return 0
  })
})

// 根据成员名生成头像emoji
const getMemberAvatar = (name) => {
  const avatarMap = {
    '我': '😊',
    '未知用户': '❓'
  }

  if (avatarMap[name]) return avatarMap[name]

  // 根据名字的哈希值选择emoji
  const emojis = ['👨', '👩', '👨‍💼', '👩‍💼', '👨‍🎓', '👩‍🎓', '👨‍🍳', '👩‍🍳', '👨‍💻', '👩‍💻', '🧑', '👴', '👵']
  const hash = name.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0)
  return emojis[hash % emojis.length]
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '未知'

  try {
    const date = new Date(dateStr)
    const now = new Date()
    const diffDays = Math.floor((now - date) / (1000 * 60 * 60 * 24))

    if (diffDays === 0) return '今天'
    if (diffDays === 1) return '昨天'
    if (diffDays < 7) return `${diffDays}天前`
    if (diffDays < 30) return `${Math.floor(diffDays / 7)}周前`
    if (diffDays < 365) return `${Math.floor(diffDays / 30)}个月前`

    return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
  } catch (error) {
    return dateStr
  }
}

const handleClose = () => {
  visible.value = false
}
</script>

<style scoped lang="less">
// 对话框样式
:deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

:deep(.el-dialog__header) {
  padding: 0;
  margin: 0;
}

:deep(.el-dialog__body) {
  padding: 0;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  background: linear-gradient(to right, #f8f9fe, #ffffff);
  border-top: 1px solid #e8eaf0;
}

.group-detail-content {
  padding: 24px;

  // 群头部信息
  .group-header {
    display: flex;
    align-items: center;
    gap: 20px;
    padding: 24px;
    background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
    border-radius: 16px;
    margin-bottom: 24px;
    box-shadow: 0 8px 24px rgba(59, 130, 246, 0.3);
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      top: -50%;
      right: -20%;
      width: 200px;
      height: 200px;
      background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
      border-radius: 50%;
    }

    .group-avatar-wrapper {
      position: relative;
      flex-shrink: 0;

      .group-avatar {
        width: 80px;
        height: 80px;
        font-size: 48px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: linear-gradient(135deg, rgba(255, 255, 255, 0.3) 0%, rgba(255, 255, 255, 0.1) 100%);
        border-radius: 20px;
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
        backdrop-filter: blur(10px);
        border: 2px solid rgba(255, 255, 255, 0.3);
        animation: float 3s ease-in-out infinite;
      }

      .avatar-badge {
        position: absolute;
        bottom: -8px;
        right: -8px;
        display: flex;
        align-items: center;
        gap: 4px;
        padding: 4px 10px;
        background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
        color: #fff;
        font-size: 12px;
        font-weight: 600;
        border-radius: 20px;
        box-shadow: 0 2px 8px rgba(245, 158, 11, 0.3);
      }
    }

    .group-basic-info {
      flex: 1;
      color: #fff;

      .group-name {
        font-size: 24px;
        font-weight: 700;
        margin-bottom: 8px;
        text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
      }

      .group-id {
        font-size: 13px;
        opacity: 0.9;
        font-family: 'Courier New', monospace;
        background: rgba(255, 255, 255, 0.2);
        display: inline-block;
        padding: 4px 12px;
        border-radius: 12px;
        backdrop-filter: blur(10px);
      }
    }
  }

  // 统计信息卡片
  .group-stats {
    display: flex;
    align-items: center;
    justify-content: space-around;
    padding: 24px;
    background: linear-gradient(135deg, #ffffff 0%, #f8f9fe 100%);
    border-radius: 20px;
    margin-bottom: 24px;
    box-shadow: 0 8px 32px rgba(59, 130, 246, 0.12);
    border: 2px solid rgba(59, 130, 246, 0.1);
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 4px;
      background: linear-gradient(90deg, #3b82f6 0%, #8b5cf6 50%, #ec4899 100%);
    }

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
      flex: 1;
      justify-content: center;
      position: relative;
      z-index: 1;

      .stat-content {
        text-align: center;

        .stat-value {
          font-size: 20px;
          font-weight: 700;
          background: linear-gradient(135deg, #1a1a1a 0%, #4a5568 100%);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
          margin-bottom: 4px;

          &.text-sm {
            font-size: 13px;
          }
        }

        .stat-label {
          font-size: 12px;
          color: #6b7280;
          font-weight: 500;
        }
      }
    }

    .stat-divider {
      width: 2px;
      height: 50px;
      background: linear-gradient(to bottom, transparent, #cbd5e0, transparent);
    }
  }

  // 群成员部分
  .group-members-section {
    background: linear-gradient(135deg, #ffffff 0%, #fdfbff 100%);
    border-radius: 20px;
    padding: 24px;
    box-shadow: 0 8px 32px rgba(59, 130, 246, 0.12);
    border: 2px solid rgba(139, 92, 246, 0.1);
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 4px;
      background: linear-gradient(90deg, #3b82f6 0%, #8b5cf6 50%, #ec4899 100%);
    }

    .section-header {
      margin-bottom: 20px;

      .section-title {
        display: flex;
        align-items: center;
        gap: 10px;
        font-size: 18px;
        font-weight: 700;
        background: linear-gradient(135deg, #1a1a1a 0%, #4a5568 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;

        .el-icon {
          font-size: 24px;
          background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
        }
      }
    }

    .members-list {
      max-height: 320px;
      overflow-y: auto;
      padding-right: 8px;

      &::-webkit-scrollbar {
        width: 6px;
      }

      &::-webkit-scrollbar-track {
        background: #f1f1f1;
        border-radius: 3px;
      }

      &::-webkit-scrollbar-thumb {
        background: linear-gradient(135deg, #cbd5e0 0%, #a0aec0 100%);
        border-radius: 3px;

        &:hover {
          background: linear-gradient(135deg, #a0aec0 0%, #718096 100%);
        }
      }

      .member-card {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px 14px;
        background: linear-gradient(135deg, #fdfbff 0%, #ffffff 100%);
        border-radius: 14px;
        margin-bottom: 10px;
        border: 2px solid rgba(139, 92, 246, 0.1);
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        cursor: pointer;
        animation: slideInLeft 0.5s ease backwards;
        position: relative;
        overflow: hidden;
        max-width: 100%;

        // 群主特殊卡片样式
        &.creator-card {
          background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
          border: 2px solid #f59e0b;
          box-shadow: 0 4px 16px rgba(245, 158, 11, 0.2);

          &::after {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            border-radius: 12px;
            background: linear-gradient(135deg, rgba(245, 158, 11, 0.05) 0%, transparent 50%);
            pointer-events: none;
          }

          .member-avatar {
            background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
            box-shadow: 0 4px 12px rgba(245, 158, 11, 0.4);
          }
        }

        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 0;
          bottom: 0;
          width: 4px;
          background: linear-gradient(180deg, #3b82f6 0%, #8b5cf6 100%);
          opacity: 0;
          transition: opacity 0.3s ease;
        }

        &:hover {
          background: linear-gradient(135deg, #ffffff 0%, #f8f9fe 100%);
          border-color: #8b5cf6;
          box-shadow: 0 6px 20px rgba(139, 92, 246, 0.2);
          transform: translateX(4px);

          &::before {
            opacity: 1;
          }

          .member-avatar {
            transform: rotate(3deg) scale(1.03);
          }
        }

        // 群主卡片hover效果
        &.creator-card:hover {
          background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%);
          border-color: #d97706;
          box-shadow: 0 6px 24px rgba(245, 158, 11, 0.35);

          .member-avatar {
            background: linear-gradient(135deg, #d97706 0%, #b45309 100%);
          }
        }

        .member-avatar {
          width: 46px;
          height: 46px;
          font-size: 26px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          border-radius: 14px;
          box-shadow: 0 2px 8px rgba(102, 126, 234, 0.25);
          flex-shrink: 0;
          transition: all 0.3s ease;
        }

        .member-info {
          flex: 1;
          min-width: 0;

          .member-name {
            font-size: 16px;
            font-weight: 700;
            background: linear-gradient(135deg, #1a1a1a 0%, #4a5568 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            margin-bottom: 8px;
            display: flex;
            align-items: center;
            gap: 8px;
            flex-wrap: wrap;

            .role-tag {
              font-size: 11px;
              padding: 4px 10px;
              height: 20px;
              line-height: 20px;
              border-radius: 10px;
              font-weight: 600;
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);

              &.creator-tag {
                background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
                border: none;
                box-shadow: 0 2px 8px rgba(245, 158, 11, 0.3);
              }
            }
          }

          .member-id {
            font-size: 12px;
            color: #9ca3af;
            font-family: 'Courier New', monospace;
            background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
            padding: 4px 10px;
            border-radius: 8px;
            display: inline-block;
            font-weight: 500;
          }
        }

        .member-role-icon {
          flex-shrink: 0;
          font-size: 18px;
          opacity: 0.7;

          .crown-icon {
            color: #f59e0b;
          }

          .creator-icon {
            color: #ef4444;
          }
        }
      }
    }
  }
}

// 底部按钮
.dialog-footer {
  display: flex;
  justify-content: flex-end;

  :deep(.el-button) {
    border-radius: 12px;
    padding: 12px 32px;
    font-weight: 600;
    font-size: 15px;
    background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
    border: none;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 0 4px 16px rgba(139, 92, 246, 0.3);
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: -100%;
      width: 100%;
      height: 100%;
      background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
      transition: left 0.5s ease;
    }

    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 8px 28px rgba(139, 92, 246, 0.5);
      background: linear-gradient(135deg, #2563eb 0%, #7c3aed 100%);

      &::before {
        left: 100%;
      }
    }

    &:active {
      transform: translateY(-1px);
    }
  }
}

// 动画效果
@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-8px);
  }
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}
</style>
