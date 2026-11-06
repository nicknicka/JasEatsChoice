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
  padding: var(--space-5);
  background: var(--surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--line);
  box-shadow: var(--shadow-sm);
  margin-bottom: var(--space-4);

  .header-top {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: var(--space-3);

    .avatar-name-section {
      display: flex;
      align-items: center;
      gap: var(--space-3);
      flex: 1;

      .merchant-avatar {
        width: 56px;
        height: 56px;
        border-radius: var(--radius-lg);
        overflow: hidden;
        background: var(--canvas-sunken);
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        .default-avatar {
          color: var(--ink-400);
        }
      }

      .name-rating-info {
        flex: 1;
        min-width: 0;

        .merchant-name {
          font-size: var(--text-h2);
          font-weight: var(--weight-semibold);
          color: var(--ink-900);
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
            font-family: var(--font-num);
            font-variant-numeric: tabular-nums;
            font-size: var(--text-h2);
            font-weight: var(--weight-semibold);
            color: var(--warning);
            line-height: 1;
          }

          .rating-stars {
            display: flex;
            align-items: center;

            :deep(.el-rate) {
              .el-rate__icon {
                font-size: var(--text-h3);
                margin-right: 2px;
              }
            }
          }

          .rating-score-text {
            font-family: var(--font-num);
            font-variant-numeric: tabular-nums;
            font-size: var(--text-body);
            font-weight: var(--weight-semibold);
            color: var(--warning);
            margin-left: var(--space-2);
          }
        }
      }
    }

    .favorite-button {
      flex-shrink: 0;
      height: 36px;
      padding: 0 var(--space-4);
      transition: background var(--duration-fast) var(--ease-out), transform var(--duration-fast) var(--ease-out);
      border-radius: var(--radius-md);
      font-size: var(--text-body-sm);
      font-weight: var(--weight-semibold);

      &:hover {
        transform: translateY(-1px);
      }

      &:active {
        transform: translateY(0);
      }

      &.el-button--warning {
        background: var(--brand-500);
        border-color: transparent;
        color: var(--surface);
      }

      &.el-button--default {
        background: var(--canvas-sunken);
        border-color: transparent;
        color: var(--ink-700);
      }
    }
  }

  .tags-row {
    margin-bottom: var(--space-3);

    .tags-status-group {
      display: flex;
      align-items: center;
      gap: var(--space-2);
      flex-wrap: wrap;
      min-height: 32px;

      .type-tag {
        background: var(--canvas-sunken);
        border: none;
        color: var(--ink-700);
        font-size: var(--text-caption);
        padding: 4px 12px;
        font-weight: var(--weight-semibold);
        border-radius: var(--radius-sm);
        flex-shrink: 0;
      }

      .status-badge {
        display: inline-flex;
        align-items: center;
        gap: 5px;
        padding: 4px 10px;
        border-radius: var(--radius-sm);
        font-size: var(--text-caption);
        font-weight: var(--weight-semibold);
        flex-shrink: 0;

        .status-dot {
          width: 6px;
          height: 6px;
          border-radius: var(--radius-full);
          animation: pulse 2s infinite;
        }

        &.status-open {
          background: var(--success-bg);
          color: var(--success);

          .status-dot {
            background: var(--success);
            box-shadow: 0 0 0 2px color-mix(in srgb, var(--success) 20%, transparent);
          }
        }

        &.status-closed {
          background: var(--danger-bg);
          color: var(--danger);

          .status-dot {
            background: var(--danger);
            animation: none;
          }
        }
      }

      .feature-tag {
        background: var(--canvas-sunken);
        border: 1px solid var(--line);
        color: var(--ink-500);
        font-size: var(--text-caption);
        padding: 3px 10px;
        border-radius: var(--radius-sm);
        font-weight: var(--weight-medium);
      }

      .distance-badge {
        display: inline-flex;
        align-items: center;
        gap: var(--space-1);
        padding: 4px 10px;
        background: var(--info-bg);
        color: var(--info);
        border-radius: var(--radius-sm);
        font-size: var(--text-caption);
        font-weight: var(--weight-semibold);
        flex-shrink: 0;

        .el-icon {
          font-size: var(--text-body-sm);
        }
      }
    }
  }

  .info-row {
    display: flex;
    align-items: center;
    gap: var(--space-4);
    margin-bottom: var(--space-3);
    flex-wrap: wrap;

    .info-item {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      font-size: var(--text-body-sm);
      color: var(--ink-700);
      font-weight: var(--weight-medium);
      padding: 6px 12px;
      background: var(--canvas);
      border-radius: var(--radius-md);

      .el-icon {
        color: var(--ink-500);
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
    box-shadow: 0 0 0 0 color-mix(in srgb, var(--success) 40%, transparent);
  }
  50% {
    box-shadow: 0 0 0 4px color-mix(in srgb, var(--success) 0%, transparent);
  }
}
</style>
