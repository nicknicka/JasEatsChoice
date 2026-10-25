<template>
  <div class="merchant-header">
    <!-- 顶部：头像、名称和评分 -->
    <div class="header-top">
      <div class="avatar-name-section">
        <div class="merchant-avatar">
          <img v-if="merchant.image" :src="merchant.image" :alt="merchant.name" />
          <el-icon v-else class="default-avatar" :size="28">
            <Shop />
          </el-icon>
        </div>
        <div class="name-rating-info">
          <h1 class="merchant-name">{{ merchant.name || '商家名称' }}</h1>
          <div class="rating-wrapper">
              <div v-if="merchant.rating !== undefined && merchant.rating !== 0">
              <div class="rating-stars">
                <el-rate :model-value="merchant.rating" disabled size="small" />
              </div>
              <div class="rating-score-text">{{ merchant.rating }}分</div>
            </div>
            <div v-else class="rating-placeholder">
              <span class="rating-number">暂无评分</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 收藏按钮放在右侧 -->
      <el-button
        :type="isFavorite ? 'warning' : 'default'"
        :icon="isFavorite ? StarFilled : Star"
        class="favorite-button"
        @click="handleToggleFavorite"
      >
        {{ isFavorite ? '已收藏' : '收藏' }}
      </el-button>
    </div>

    <!-- 标签和状态 -->
    <div class="tags-row">
      <div class="tags-status-group">
        <el-tag v-if="merchant.type" type="primary" size="small" class="type-tag">
          {{ merchant.type }}
        </el-tag>
        <div v-if="merchant.status" class="status-badge" :class="statusClass">
          <span class="status-dot"></span>
          {{ statusText }}
        </div>
        <el-tag
          v-for="(tag, index) in merchant.tags"
          :key="index"
          size="small"
          class="feature-tag"
        >
          {{ tag }}
        </el-tag>
        <div v-if="merchant.distance" class="distance-badge">
          <el-icon><Location /></el-icon>
          {{ merchant.distance }}
        </div> 
      </div>
    </div>

    <!-- 联系信息行 -->
    <div class="info-row">
      <div class="info-item" v-if="merchant.phone && typeof merchant.phone === 'string' && merchant.phone.trim()">
        <el-icon :size="16"><Phone /></el-icon>
        <span>{{ merchant.phone }}</span>
      </div>
      <div class="info-item" v-if="merchant.businessHours && typeof merchant.businessHours === 'string' && formatBusinessHours(merchant.businessHours)">
        <el-icon :size="16"><Clock /></el-icon>
        <span>{{ formatBusinessHours(merchant.businessHours) }}</span>
      </div>
      <div class="info-item" v-if="merchant.address && typeof merchant.address === 'string' && merchant.address.trim()">
        <el-icon :size="16"><Location /></el-icon>
        <span class="address-text">{{ merchant.address }}</span>
      </div>
    </div>

  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Shop, Star, StarFilled, Location, Phone, Clock } from '@element-plus/icons-vue'

const props = defineProps({
  merchant: {
    type: Object,
    required: true
  },
  isFavorite: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['toggle-favorite'])

const statusText = computed(() => {
  // 处理布尔值类型的状态
  let statusValue = props.merchant.status
  if (typeof statusValue === 'boolean') {
    statusValue = statusValue ? '营业中' : '未知'
  }

  const statusMap = {
    营业中: '营业中',
    休息中: '休息中',
    打烊: '已打烊'
  }
  return statusMap[statusValue] || '未知'
})

const statusClass = computed(() => {
  const classMap = {
    营业中: 'status-open',
    休息中: 'status-closed',
    打烊: 'status-closed'
  }
  return classMap[props.merchant.status] || ''
})

const formatBusinessHours = (businessHours) => {
  if (!businessHours) return ''

  if (typeof businessHours === 'string') {
    return businessHours
  }

  if (typeof businessHours === 'object') {
    if (businessHours.open && businessHours.close) {
      return `${businessHours.open} - ${businessHours.close}`
    }
    if (businessHours.start && businessHours.end) {
      return `${businessHours.start} - ${businessHours.end}`
    }
    const days = Object.keys(businessHours)
    if (days.length > 0) {
      const firstDayHours = businessHours[days[0]]
      if (typeof firstDayHours === 'string') {
        return firstDayHours
      }
    }
  }

  return ''
}

const handleToggleFavorite = () => {
  emit('toggle-favorite')
}
</script>

<style scoped lang="less">
.merchant-header {
  padding: 18px;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 16px;
  border: 1px solid rgba(59, 130, 246, 0.08);
  box-shadow: 0 4px 20px rgba(59, 130, 246, 0.08);
  margin-bottom: 16px;

  .header-top {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 14px;

    .avatar-name-section {
      display: flex;
      align-items: center;
      gap: 12px;
      flex: 1;

      .merchant-avatar {
        width: 56px;
        height: 56px;
        border-radius: 12px;
        overflow: hidden;
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 6px 20px rgba(59, 130, 246, 0.25);
        flex-shrink: 0;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        .default-avatar {
          color: #ffffff;
        }
      }

      .name-rating-info {
        flex: 1;
        min-width: 0;

        .merchant-name {
          font-size: 1.286rem /* 原值: 18px */;
          font-weight: 800;
          color: #0f172a;
          line-height: 1.3;
          margin: 0 0 6px 0;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .rating-wrapper {
          display: flex;
          align-items: center;
          gap: 10px;

          .rating-number {
            font-size: 26px;
            font-weight: 700;
            color: #f59e0b;
            line-height: 1;
          }

          .rating-stars {
            display: flex;
            align-items: center;

            :deep(.el-rate) {
              .el-rate__icon {
                font-size: 1.143rem /* 原值: 16px */;
                margin-right: 2px;
              }
            }
          }

          .rating-score-text {
            font-size: 1rem /* 原值: 14px */;
            font-weight: 700;
            color: #f59e0b;
            margin-left: 8px;
          }
        }
      }
    }

    .favorite-button {
      flex-shrink: 0;
      height: 36px;
      padding: 0 16px;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      border-radius: 10px;
      font-size: 0.929rem /* 原值: 13px */;
      font-weight: 600;

      &:hover {
        transform: translateY(-2px);
      }

      &:active {
        transform: translateY(0);
      }

      &.el-button--warning {
        background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
        border-color: transparent;
        box-shadow: 0 4px 14px rgba(245, 158, 11, 0.35);
        color: #ffffff;
      }

      &.el-button--default {
        background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
        border-color: transparent;
        color: #475569;
      }
    }
  }

  .tags-row {
    margin-bottom: 14px;

    .tags-status-group {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
      min-height: 32px;

      .type-tag {
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
        border: none;
        color: #ffffff;
        font-size: 0.857rem /* 原值: 12px */;
        padding: 4px 12px;
        font-weight: 600;
        border-radius: 6px;
        box-shadow: 0 2px 8px rgba(59, 130, 246, 0.25);
        flex-shrink: 0;
      }

      .status-badge {
        display: inline-flex;
        align-items: center;
        gap: 5px;
        padding: 4px 10px;
        border-radius: 6px;
        font-size: 0.857rem /* 原值: 12px */;
        font-weight: 600;
        flex-shrink: 0;

        .status-dot {
          width: 6px;
          height: 6px;
          border-radius: 50%;
          animation: pulse 2s infinite;
        }

        &.status-open {
          background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
          color: #047857;

          .status-dot {
            background: #10b981;
            box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.2);
          }
        }

        &.status-closed {
          background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
          color: #b91c1c;

          .status-dot {
            background: #ef4444;
            animation: none;
          }
        }
      }

      .feature-tag {
        background: rgba(59, 130, 246, 0.08);
        border: 1px solid rgba(59, 130, 246, 0.15);
        color: #3b82f6;
        font-size: 0.75rem /* 原值: 11px */;
        padding: 3px 10px;
        border-radius: 6px;
        font-weight: 500;
      }

      .distance-badge {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        padding: 4px 10px;
        background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
        color: #1d4ed8;
        border-radius: 6px;
        font-size: 0.857rem /* 原值: 12px */;
        font-weight: 600;
        flex-shrink: 0;

        .el-icon {
          font-size: 0.929rem /* 原值: 13px */;
        }
      }
    }
  }

  .info-row {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 12px;
    flex-wrap: wrap;

    .info-item {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      font-size: 0.929rem /* 原值: 13px */;
      color: #475569;
      font-weight: 500;
      padding: 6px 12px;
      background: rgba(59, 130, 246, 0.05);
      border-radius: 8px;

      .el-icon {
        color: #3b82f6;
        flex-shrink: 0;
      }

      span {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .address-text {
        max-width: 300px;
      }
    }
  }

}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.4);
  }
  50% {
    box-shadow: 0 0 0 4px rgba(16, 185, 129, 0);
  }
}
</style>
