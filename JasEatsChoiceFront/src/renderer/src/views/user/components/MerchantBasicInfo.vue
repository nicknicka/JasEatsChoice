<template>
  <div class="merchant-basic-info">
    <div class="info-grid">
      <div class="info-item">
        <div class="info-icon rating-icon">
          <el-icon><StarFilled /></el-icon>
        </div>
        <div class="info-content">
          <div class="info-label">评分</div>
          <div class="info-value">
            {{ merchant.rating ? merchant.rating.toFixed(1) : '暂无评价' }}
          </div>
        </div>
      </div>
      <div class="info-item">
        <div class="info-icon location-icon">
          <el-icon><Location /></el-icon>
        </div>
        <div class="info-content">
          <div class="info-label">距离</div>
          <div class="info-value">{{ merchant.distance || '未知距离' }}</div>
        </div>
      </div>
      <div class="info-item">
        <div class="info-icon time-icon">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="info-content">
          <div class="info-label">营业时间</div>
          <div class="info-value">11:00-22:00</div>
        </div>
      </div>
      <div class="info-item">
        <div class="info-icon price-icon">
          <el-icon><Coin /></el-icon>
        </div>
        <div class="info-content">
          <div class="info-label">人均消费</div>
          <div class="info-value">¥88</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { StarFilled, Location, Clock, Coin } from '@element-plus/icons-vue'

defineProps({
  merchant: {
    type: Object,
    required: true
  }
})
</script>

<style scoped lang="less">
.merchant-basic-info {
  padding: 24px;
  background: linear-gradient(135deg, #eff6ff 0%, #f0f9ff 100%);
  border-bottom: 1px solid rgba(59, 130, 246, 0.1);

  .info-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
    gap: 16px;

    .info-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 16px;
      background: #ffffff;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(59, 130, 246, 0.08);
      transition: all 0.3s ease;

      &:hover {
        box-shadow: 0 4px 16px rgba(59, 130, 246, 0.15);
        transform: translateY(-2px);
      }

      .info-icon {
        width: 48px;
        height: 48px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 12px;
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
        flex-shrink: 0;
        color: #ffffff;
        font-size: 20px;

        &.rating-icon {
          background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
        }

        &.location-icon {
          background: linear-gradient(135deg, #10b981 0%, #059669 100%);
        }

        &.time-icon {
          background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
        }

        &.price-icon {
          background: linear-gradient(135deg, #ec4899 0%, #db2777 100%);
        }
      }

      .info-content {
        flex: 1;
        min-width: 0;

        .info-label {
          font-size: 12px;
          color: #64748b;
          margin-bottom: 4px;
          font-weight: 500;
        }

        .info-value {
          font-size: 15px;
          color: #1e293b;
          font-weight: 600;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }
    }
  }
}
</style>
