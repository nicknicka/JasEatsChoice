<template>
  <view class="wish-detail-container">
    <!-- 加载状态 -->
    <view class="loading-state" v-if="loading">
      <uni-load-more status="loading" />
    </view>

    <!-- 详情内容 -->
    <template v-else-if="wishInfo.id">
      <!-- 状态卡片 -->
      <view class="status-card" :class="'status-' + wishInfo.status">
        <view class="status-icon">{{ getStatusIcon(wishInfo.status) }}</view>
        <view class="status-info">
          <text class="status-text">{{ getStatusText(wishInfo.status) }}</text>
          <text class="status-desc">{{ getStatusDesc(wishInfo.status) }}</text>
        </view>
      </view>

      <!-- 心愿内容 -->
      <view class="wish-content-card">
        <!-- 用户信息 -->
        <view class="user-header">
          <image class="user-avatar" :src="wishInfo.user.avatar" mode="aspectFill"></image>
          <view class="user-info">
            <text class="user-name">{{ wishInfo.user.name }}</text>
            <text class="submit-time">{{ wishInfo.submitTime }}</text>
          </view>
          <button
            class="follow-btn"
            size="mini"
            :class="{ following: wishInfo.followed }"
            @tap="toggleFollow"
          >
            {{ wishInfo.followed ? '已关注' : '关注' }}
          </button>
        </view>

        <!-- 心愿描述 -->
        <view class="wish-description">
          <text class="description-text">{{ wishInfo.content }}</text>
        </view>

        <!-- 期望菜品 -->
        <view class="expected-dishes" v-if="wishInfo.dishes.length > 0">
          <text class="section-title">期望菜品</text>
          <view class="dish-list">
            <view class="dish-item" v-for="dish in wishInfo.dishes" :key="dish">
              <text class="dish-name">{{ dish }}</text>
            </view>
          </view>
        </view>

        <!-- 图片 -->
        <view class="wish-images" v-if="wishInfo.images.length > 0">
          <image
            class="wish-image"
            v-for="(img, index) in wishInfo.images"
            :key="index"
            :src="img"
            mode="aspectFill"
            @tap="previewImage(index)"
          />
        </view>

        <!-- 其他信息 -->
        <view class="wish-meta">
          <view class="meta-item" v-if="wishInfo.budget">
            <text class="label">预算</text>
            <text class="value">¥{{ wishInfo.budget }}</text>
          </view>
          <view class="meta-item" v-if="wishInfo.category">
            <text class="label">分类</text>
            <text class="value">{{ wishInfo.category }}</text>
          </view>
          <view class="meta-item" v-if="wishInfo.expectedTime">
            <text class="label">期望时间</text>
            <text class="value">{{ wishInfo.expectedTime }}</text>
          </view>
        </view>

        <!-- 互动数据 -->
        <view class="interaction-stats">
          <view class="stat-item" @tap="toggleLike">
            <uni-icons
              :type="wishInfo.liked ? 'heart-filled' : 'heart'"
              :color="wishInfo.liked ? '#FF6B35' : '#999'"
              size="20"
            />
            <text class="stat-text" :class="{ active: wishInfo.liked }">
              {{ wishInfo.likeCount || 0 }} 赞
            </text>
          </view>
          <view class="stat-item">
            <uni-icons type="chat" size="20" color="#999"></uni-icons>
            <text class="stat-text">{{ wishInfo.commentCount || 0 }} 评论</text>
          </view>
          <view class="stat-item">
            <uni-icons type="eye" size="20" color="#999"></uni-icons>
            <text class="stat-text">{{ wishInfo.viewCount || 0 }} 浏览</text>
          </view>
        </view>
      </view>

      <!-- 商家响应列表 -->
      <view class="responses-card" v-if="wishInfo.responses.length > 0">
        <text class="card-title">商家响应 ({{ wishInfo.responses.length }})</text>
        <view class="responses-list">
          <view
            class="response-item"
            v-for="response in wishInfo.responses"
            :key="response.merchantId"
          >
            <image class="merchant-avatar" :src="response.avatar" mode="aspectFill"></image>
            <view class="response-info">
              <text class="merchant-name">{{ response.name }}</text>
              <view class="response-details">
                <text class="detail-item" v-if="response.quote">
                  报价：¥{{ response.quote }}
                </text>
                <text class="detail-item" v-if="response.estimatedTime">
                  预计：{{ response.estimatedTime }}
                </text>
              </view>
              <text class="response-remark" v-if="response.remark">
                {{ response.remark }}
              </text>
            </view>
            <button
              class="select-btn"
              size="mini"
              v-if="wishInfo.status === 'pending' && !wishInfo.selectedMerchantId"
              @tap="selectMerchant(response)"
            >
              选择
            </button>
            <view
              class="selected-badge"
              v-if="wishInfo.selectedMerchantId === response.merchantId"
            >
              已选择
            </view>
          </view>
        </view>
      </view>

      <!-- 底部操作栏 -->
      <view class="action-bar" v-if="wishInfo.status === 'pending'">
        <button class="action-btn secondary" @tap="shareWish">分享</button>
        <button class="action-btn primary" @tap="editWish" v-if="isOwner">编辑</button>
        <button class="action-btn danger" @tap="deleteWish" v-if="isOwner">删除</button>
      </view>
    </template>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { wishApi } from '@/api/modules/wish.js'

const wishId = ref('')
const userId = ref('')
const loading = ref(true)

// 心愿详情 - WISH-002
const wishInfo = ref({
  id: '',
  content: '',
  status: 'pending',
  dishes: [],
  images: [],
  budget: '',
  category: '',
  expectedTime: '',
  likeCount: 0,
  commentCount: 0,
  viewCount: 0,
  liked: false,
  followed: false,
  submitTime: '',
  selectedMerchantId: '',
  user: {
    id: '',
    name: '',
    avatar: ''
  },
  responses: []
})

// 是否是拥有者
const isOwner = computed(() => {
  return wishInfo.value.user.id === userId.value
})

onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options || {}

  wishId.value = options.id || ''
  userId.value = uni.getStorageSync('userId') || ''

  // WISH-002: 加载心愿详情
  loadWishDetail()
})

/**
 * WISH-002: 加载心愿详情
 */
const loadWishDetail = async () => {
  try {
    loading.value = true

    // WISH-002: 调用API获取心愿详情
    const res = await wishApi.getDetail(wishId.value)

    if (res.code === 200 && res.data) {
      const data = res.data
      wishInfo.value = {
        id: data.id,
        content: data.content,
        status: data.status || 'pending',
        dishes: data.dishes || [],
        images: data.images || [],
        budget: data.budget || '',
        category: data.category || '',
        expectedTime: data.expectedTime || '',
        likeCount: data.likeCount || 0,
        commentCount: data.commentCount || 0,
        viewCount: data.viewCount || 0,
        liked: data.liked || false,
        followed: data.followed || false,
        submitTime: formatTime(data.createdAt),
        selectedMerchantId: data.selectedMerchantId || '',
        user: {
          id: data.userId,
          name: data.userName || '匿名用户',
          avatar: data.userAvatar || 'https://via.placeholder.com/100'
        },
        responses: (data.responses || []).map(r => ({
          merchantId: r.merchantId,
          name: r.merchantName,
          avatar: r.merchantAvatar || 'https://via.placeholder.com/100',
          quote: r.quote || '',
          estimatedTime: r.estimatedTime || '',
          remark: r.remark || ''
        }))
      }
    }

    loading.value = false
  } catch (error) {
    console.error('加载心愿详情失败:', error)
    loading.value = false
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

/**
 * WISH-003: 点赞/取消点赞
 */
const toggleLike = async () => {
  try {
    if (wishInfo.value.liked) {
      await wishApi.unlike(wishId.value, { userId: userId.value })
      wishInfo.value.liked = false
      wishInfo.value.likeCount = Math.max(0, wishInfo.value.likeCount - 1)
    } else {
      await wishApi.like(wishId.value, { userId: userId.value })
      wishInfo.value.liked = true
      wishInfo.value.likeCount++
    }
  } catch (error) {
    console.error('点赞失败:', error)
  }
}

/**
 * 关注/取消关注
 */
const toggleFollow = () => {
  // TODO: 实现关注功能
  wishInfo.value.followed = !wishInfo.value.followed
}

/**
 * 选择商家
 */
const selectMerchant = (response) => {
  uni.showModal({
    title: '确认选择',
    content: `确定选择"${response.name}"吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          const apiRes = await wishApi.selectMerchant(wishId.value, {
            userId: userId.value,
            merchantId: response.merchantId
          })

          if (apiRes.code === 200) {
            uni.showToast({
              title: '选择成功',
              icon: 'success'
            })

            // 刷新详情
            setTimeout(() => {
              loadWishDetail()
            }, 1500)
          }
        } catch (error) {
          uni.showToast({
            title: '选择失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 预览图片
 */
const previewImage = (index) => {
  uni.previewImage({
    urls: wishInfo.value.images,
    current: index
  })
}

/**
 * 分享心愿
 */
const shareWish = () => {
  uni.showActionSheet({
    itemList: ['分享给好友', '生成海报'],
    success: (res) => {
      if (res.tapIndex === 0) {
        // 分享给好友
        uni.share({
          provider: 'weixin',
          type: 0,
          title: '我的心愿单',
          summary: wishInfo.value.content,
          success: () => {
            uni.showToast({
              title: '分享成功',
              icon: 'success'
            })
          }
        })
      } else {
        // 生成海报
        uni.showToast({
          title: '海报生成功能开发中',
          icon: 'none'
        })
      }
    }
  })
}

/**
 * 编辑心愿
 */
const editWish = () => {
  uni.navigateTo({
    url: `/pages-user/wish/add?id=${wishId.value}&mode=edit`
  })
}

/**
 * 删除心愿
 */
const deleteWish = () => {
  uni.showModal({
    title: '提示',
    content: '确定删除此心愿吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          const apiRes = await wishApi.delete(wishId.value, { userId: userId.value })

          if (apiRes.code === 200) {
            uni.showToast({
              title: '删除成功',
              icon: 'success'
            })

            setTimeout(() => {
              uni.navigateBack()
            }, 1500)
          }
        } catch (error) {
          uni.showToast({
            title: '删除失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 获取状态图标
 */
const getStatusIcon = (status) => {
  const icons = {
    pending: '💝',
    accepted: '✅',
    rejected: '❌',
    completed: '🎉'
  }
  return icons[status] || '💝'
}

/**
 * 获取状态文本
 */
const getStatusText = (status) => {
  const texts = {
    pending: '等待响应',
    accepted: '已接受',
    rejected: '已拒绝',
    completed: '已完成'
  }
  return texts[status] || '未知'
}

/**
 * 获取状态描述
 */
const getStatusDesc = (status) => {
  const descs = {
    pending: '等待商家响应您的需求',
    accepted: '商家已接受您的需求',
    rejected: '商家拒绝了您的需求',
    completed: '心愿已完成'
  }
  return descs[status] || ''
}

/**
 * 格式化时间
 */
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.wish-detail-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 120rpx;
}

.loading-state {
  padding: 100rpx 0;
}

/* 状态卡片 */
.status-card {
  background: #fff;
  padding: 40rpx 30rpx;
  margin-bottom: 20rpx;
  display: flex;
  align-items: center;
  gap: 30rpx;

  &.status-pending {
    background: linear-gradient(135deg, #FFA500 0%, #FF8C00 100%);
    color: #fff;
  }

  &.status-accepted {
    background: linear-gradient(135deg, #52C41A 0%, #73D13D 100%);
    color: #fff;
  }

  &.status-rejected {
    background: linear-gradient(135deg, #8C8C8C 0%, #BFBFBF 100%);
    color: #fff;
  }

  &.status-completed {
    background: linear-gradient(135deg, #1890FF 0%, #40A9FF 100%);
    color: #fff;
  }
}

.status-icon {
  font-size: 80rpx;
}

.status-info {
  flex: 1;
}

.status-text {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  margin-bottom: 10rpx;
}

.status-desc {
  display: block;
  font-size: 26rpx;
  opacity: 0.9;
}

/* 心愿内容 */
.wish-content-card {
  background: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 15rpx;
  margin-bottom: 25rpx;
}

.user-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
}

.user-info {
  flex: 1;
}

.user-name {
  display: block;
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 5rpx;
}

.submit-time {
  display: block;
  font-size: 24rpx;
  color: #999;
}

.follow-btn {
  padding: 0 20rpx;
  height: 55rpx;
  line-height: 55rpx;
  border-radius: 27rpx;
  font-size: 24rpx;
  background: #F5F5F5;
  color: #666;
  border: none;

  &.following {
    background: #FF6B35;
    color: #fff;
  }
}

.wish-description {
  margin-bottom: 25rpx;
}

.description-text {
  display: block;
  font-size: 28rpx;
  color: #333;
  line-height: 1.8;
}

.expected-dishes {
  margin-bottom: 25rpx;
}

.section-title {
  display: block;
  font-size: 26rpx;
  color: #666;
  margin-bottom: 15rpx;
}

.dish-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.dish-item {
  padding: 8rpx 20rpx;
  background: #F5F5F5;
  border-radius: 20rpx;
}

.dish-name {
  font-size: 24rpx;
  color: #FF6B35;
}

.wish-images {
  display: flex;
  flex-wrap: wrap;
  gap: 15rpx;
  margin-bottom: 25rpx;
}

.wish-image {
  width: 200rpx;
  height: 200rpx;
  border-radius: 12rpx;
}

.wish-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 30rpx;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
  margin-bottom: 25rpx;
}

.meta-item {
  font-size: 26rpx;

  .label {
    color: #999;
    margin-right: 8rpx;
  }

  .value {
    color: #333;
    font-weight: 500;
  }
}

.interaction-stats {
  display: flex;
  justify-content: space-around;
  padding-top: 20rpx;
  border-top: 1rpx solid #eee;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.stat-text {
  font-size: 24rpx;
  color: #666;

  &.active {
    color: #FF6B35;
  }
}

/* 商家响应 */
.responses-card {
  background: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.card-title {
  display: block;
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.responses-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.response-item {
  display: flex;
  align-items: center;
  gap: 15rpx;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
  position: relative;
}

.merchant-avatar {
  width: 70rpx;
  height: 70rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.response-info {
  flex: 1;
}

.merchant-name {
  display: block;
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
}

.response-details {
  display: flex;
  flex-wrap: wrap;
  gap: 15rpx;
  margin-bottom: 8rpx;
}

.detail-item {
  font-size: 24rpx;
  color: #FF6B35;
}

.response-remark {
  display: block;
  font-size: 24rpx;
  color: #666;
}

.select-btn {
  padding: 0 20rpx;
  height: 55rpx;
  line-height: 55rpx;
  border-radius: 27rpx;
  font-size: 24rpx;
  background: #FF6B35;
  color: #fff;
  border: none;
}

.selected-badge {
  padding: 0 20rpx;
  height: 55rpx;
  line-height: 55rpx;
  border-radius: 27rpx;
  background: #52C41A;
  color: #fff;
  font-size: 24rpx;
}

/* 底部操作 */
.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  background: #fff;
  display: flex;
  gap: 20rpx;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.action-btn {
  flex: 1;
  height: 90rpx;
  border-radius: 45rpx;
  font-size: 28rpx;
  border: none;

  &.secondary {
    background: #F5F5F5;
    color: #666;
  }

  &.primary {
    background: #FF6B35;
    color: #fff;
  }

  &.danger {
    background: #F5222D;
    color: #fff;
  }
}
</style>
