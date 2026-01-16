<template>
  <div class="merchant-header">
    <div class="header-content">
      <div class="merchant-avatar">
        <img
          v-if="merchant.image && merchant.image !== '未知'"
          :src="merchant.image"
          :alt="merchant.name"
          class="avatar-img"
        />
        <div v-else class="avatar-placeholder">
          <el-icon :size="40"><Shop /></el-icon>
        </div>
      </div>
      <div class="merchant-info-section">
        <div class="merchant-name-row">
          <h1 class="merchant-name-main">{{ merchant.name }}</h1>
          <el-button type="text" size="small" class="favorite-button" @click="handleToggleFavorite">
            <el-icon class="favorite-icon">
              <component :is="isFavorite ? 'StarFilled' : 'Star'" />
            </el-icon>
            {{ isFavorite ? '已收藏' : '收藏' }}
          </el-button>
        </div>
        <div class="merchant-meta-tags">
          <el-tag v-if="merchant.type" type="primary" size="small" class="meta-tag">
            {{ merchant.type }}
          </el-tag>
          <el-tag
            v-for="tag in merchant.tags?.slice(0, 3)"
            :key="tag"
            size="small"
            class="meta-tag"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Shop, Star, StarFilled } from '@element-plus/icons-vue'

defineProps({
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

const handleToggleFavorite = () => {
  emit('toggle-favorite')
}
</script>

<style scoped lang="less">
.merchant-header {
  padding: 20px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -10%;
    width: 400px;
    height: 400px;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
    border-radius: 50%;
  }

  .header-content {
    display: flex;
    gap: 20px;
    align-items: center;
    position: relative;
    z-index: 1;

    .merchant-avatar {
      flex-shrink: 0;
      width: 80px;
      height: 80px;
      border-radius: 50%;
      overflow: hidden;
      border: 3px solid rgba(255, 255, 255, 0.3);
      background: rgba(255, 255, 255, 0.1);
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);

      .avatar-img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .avatar-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        background: linear-gradient(
          135deg,
          rgba(255, 255, 255, 0.2) 0%,
          rgba(255, 255, 255, 0.1) 100%
        );
        color: #ffffff;
      }
    }

    .merchant-info-section {
      flex: 1;
      min-width: 0;

      .merchant-name-row {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 16px;
        margin-bottom: 12px;

        .merchant-name-main {
          font-size: 24px;
          font-weight: 700;
          color: #ffffff;
          margin: 0;
          letter-spacing: -0.5px;
          text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
        }

        .favorite-button {
          color: rgba(255, 255, 255, 0.9);
          background: rgba(255, 255, 255, 0.15);
          border: 1px solid rgba(255, 255, 255, 0.2);
          padding: 8px 16px;
          border-radius: 20px;
          backdrop-filter: blur(10px);
          transition: all 0.3s ease;
          font-weight: 500;

          .favorite-icon {
            margin-right: 4px;
            font-size: 16px;
          }

          &:hover {
            background: rgba(255, 255, 255, 0.25);
            border-color: rgba(255, 255, 255, 0.3);
            color: #ffffff;
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
          }
        }
      }

      .merchant-meta-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;

        .meta-tag {
          background: rgba(255, 255, 255, 0.2);
          border: 1px solid rgba(255, 255, 255, 0.3);
          color: #ffffff;
          backdrop-filter: blur(10px);
        }
      }
    }
  }
}
</style>
