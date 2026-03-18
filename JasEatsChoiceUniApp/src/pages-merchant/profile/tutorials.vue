<template>
  <view class="tutorials-container">
    <!-- 快速入门 -->
    <view class="section-card">
      <view class="section-header">
        <text class="section-title">快速入门</text>
        <text class="section-desc">新商家必看教程</text>
      </view>
      <view class="tutorial-list">
        <view
          class="tutorial-card"
          v-for="item in quickStartTutorials"
          :key="item.id"
          @tap="viewTutorial(item)"
        >
          <image class="tutorial-thumb" :src="item.thumb" mode="aspectFill"></image>
          <view class="tutorial-info">
            <view class="tutorial-tag">{{ item.tag }}</view>
            <text class="tutorial-title">{{ item.title }}</text>
            <view class="tutorial-meta">
              <text class="meta-item">
                <uni-icons type="videocam" size="12" color="#999"></uni-icons>
                {{ item.duration }}
              </text>
              <text class="meta-item">
                <uni-icons type="eye" size="12" color="#999"></uni-icons>
                {{ item.views }}
              </text>
            </view>
          </view>
          <view class="play-icon">
            <uni-icons type="play-filled" size="30" color="#fff"></uni-icons>
          </view>
        </view>
      </view>
    </view>

    <!-- 功能教程 -->
    <view class="section-card">
      <view class="section-header">
        <text class="section-title">功能教程</text>
        <text class="section-desc">详细功能使用指南</text>
      </view>
      <view class="category-tabs">
        <scroll-view scroll-x class="tabs-scroll">
          <view
            class="tab-item"
            :class="{ active: activeCategory === item.value }"
            v-for="item in categoryTabs"
            :key="item.value"
            @tap="changeCategory(item.value)"
          >
            {{ item.label }}
          </view>
        </scroll-view>
      </view>
      <view class="tutorial-list">
        <view
          class="tutorial-item"
          v-for="item in featureTutorials"
          :key="item.id"
          @tap="viewTutorial(item)"
        >
          <view class="item-icon" :class="'type-' + item.type">
            <uni-icons :type="getCategoryIcon(item.type)" size="24" color="#fff"></uni-icons>
          </view>
          <view class="item-info">
            <text class="item-title">{{ item.title }}</text>
            <text class="item-desc">{{ item.description }}</text>
          </view>
          <view class="item-action">
            <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
          </view>
        </view>
      </view>
    </view>

    <!-- 常见问题 -->
    <view class="section-card">
      <view class="section-header">
        <text class="section-title">常见问题</text>
        <text class="section-desc">快速解决遇到的问题</text>
      </view>
      <view class="faq-list">
        <view
          class="faq-item"
          v-for="(item, index) in faqList"
          :key="index"
        >
          <view class="faq-question" @tap="toggleFaq(index)">
            <text class="question-text">{{ item.question }}</text>
            <uni-icons
              :type="item.expanded ? 'arrowup' : 'arrowdown'"
              size="16"
              color="#999"
            ></uni-icons>
          </view>
          <view class="faq-answer" v-if="item.expanded">
            <text class="answer-text">{{ item.answer }}</text>
            <view class="answer-actions" v-if="item.helpful !== undefined">
              <text class="helpful-text">是否有帮助？</text>
              <view class="helpful-buttons">
                <button
                  class="helpful-btn"
                  :class="{ active: item.helpful === true }"
                  @tap="markHelpful(index, true)"
                >
                  <uni-icons type="hand-up" size="14"></uni-icons>
                  有
                </button>
                <button
                  class="helpful-btn"
                  :class="{ active: item.helpful === false }"
                  @tap="markHelpful(index, false)"
                >
                  <uni-icons type="hand-down" size="14"></uni-icons>
                  无
                </button>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 联系支持 -->
    <view class="support-card">
      <view class="support-content">
        <text class="support-title">还有问题？</text>
        <text class="support-desc">我们的客服团队随时为您服务</text>
      </view>
      <view class="support-actions">
        <button class="support-btn primary" @tap="contactOnlineService">
          <uni-icons type="chatbubble" size="18" color="#fff"></uni-icons>
          <text>在线客服</text>
        </button>
        <button class="support-btn" @tap="callService">
          <uni-icons type="phone" size="18" color="#FF6B35"></uni-icons>
          <text>电话客服</text>
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 快速入门教程
const quickStartTutorials = ref([
  {
    id: 1,
    title: '店铺注册与认证',
    tag: '新手必看',
    duration: '3:25',
    views: '1.2万',
    thumb: 'https://via.placeholder.com/300x200/FF6B35/FFFFFF?text=教程1',
    videoUrl: ''
  },
  {
    id: 2,
    title: '菜品上架与定价',
    tag: '核心功能',
    duration: '5:18',
    views: '8563',
    thumb: 'https://via.placeholder.com/300x200/FF6B35/FFFFFF?text=教程2',
    videoUrl: ''
  },
  {
    id: 3,
    title: '订单处理流程',
    tag: '重要',
    duration: '4:52',
    views: '6789',
    thumb: 'https://via.placeholder.com/300x200/FF6B35/FFFFFF?text=教程3',
    videoUrl: ''
  }
])

// 分类标签
const categoryTabs = ref([
  { label: '全部', value: 'all' },
  { label: '订单管理', value: 'order' },
  { label: '菜品管理', value: 'dish' },
  { label: '营销推广', value: 'marketing' },
  { label: '财务结算', value: 'finance' },
  { label: '数据分析', value: 'data' }
])

const activeCategory = ref('all')

// 功能教程
const featureTutorials = ref([
  {
    id: 1,
    type: 'order',
    title: '如何接单和拒单',
    description: '讲解接单流程和拒单规则',
    icon: 'checkbox-filled'
  },
  {
    id: 2,
    type: 'order',
    title: '订单状态更新说明',
    description: '详细说明各状态含义和操作',
    icon: 'loop'
  },
  {
    id: 3,
    type: 'dish',
    title: '菜品图片上传技巧',
    description: '如何拍摄吸引人的菜品图片',
    icon: 'image'
  },
  {
    id: 4,
    type: 'dish',
    title: '菜品分类管理',
    description: '创建和管理菜品分类',
    icon: 'list'
  },
  {
    id: 5,
    type: 'marketing',
    title: '优惠券设置',
    description: '创建和发放优惠券',
    icon: 'gift-filled'
  },
  {
    id: 6,
    type: 'finance',
    title: '提现流程说明',
    description: '如何提现到银行卡',
    icon: 'wallet-filled'
  }
])

// 常见问题
const faqList = ref([
  {
    question: '如何修改店铺营业时间？',
    answer: '进入"我的" → "店铺设置" → "营业时间"，点击编辑按钮即可修改营业时间。您可以设置不同日期的营业时间，也可以设置休息日。',
    expanded: false,
    helpful: undefined
  },
  {
    question: '订单可以拒绝吗？拒绝后有什么影响？',
    answer: '可以拒绝订单，但建议合理使用。频繁拒单会影响店铺评分和曝光率。拒单后用户会收到通知，您可以填写拒单原因说明情况。',
    expanded: false,
    helpful: undefined
  },
  {
    question: '提现多久能到账？',
    answer: '银行卡提现通常1-3个工作日到账；微信零钱和支付宝提现实时到账。提现手续费为提现金额的0.2%，最低0.01元。',
    expanded: false,
    helpful: undefined
  },
  {
    question: '如何处理用户差评？',
    answer: '遇到差评时，建议先联系用户了解具体情况，积极解决问题。在评价中心可以回复用户评价，真诚的回复可以挽回其他用户的信任。',
    expanded: false,
    helpful: undefined
  },
  {
    question: '菜品库存如何管理？',
    answer: '在菜品编辑页面可以设置库存数量。订单成交后库存会自动扣减。当库存为0时，菜品会自动显示为"已售罄"。您也可以手动设置菜品状态为"售罄"或"下架"。',
    expanded: false,
    helpful: undefined
  }
])

onMounted(() => {
  loadTutorials()
})

/**
 * 加载教程数据
 */
const loadTutorials = () => {
  // TODO: 调用API获取教程数据
  // const res = await merchantApi.getTutorials()
  // quickStartTutorials.value = res.data.quickStart
  // featureTutorials.value = res.data.features
  // faqList.value = res.data.faq
}

/**
 * 查看教程
 */
const viewTutorial = (tutorial) => {
  if (tutorial.videoUrl) {
    // 播放视频教程
    uni.navigateTo({
      url: `/pages-merchant/profile/tutorial-video?id=${tutorial.id}`
    })
  } else {
    // 查看图文教程
    uni.navigateTo({
      url: `/pages-merchant/profile/tutorial-detail?id=${tutorial.id}`
    })
  }
}

/**
 * 切换分类
 */
const changeCategory = (category) => {
  activeCategory.value = category
  // TODO: 根据分类筛选教程
}

/**
 * 获取分类图标
 */
const getCategoryIcon = (type) => {
  const iconMap = {
    order: 'checkbox-filled',
    dish: 'image-filled',
    marketing: 'gift-filled',
    finance: 'wallet-filled',
    data: 'pie-chart-filled'
  }
  return iconMap[type] || 'help-filled'
}

/**
 * 展开/收起FAQ
 */
const toggleFaq = (index) => {
  faqList.value[index].expanded = !faqList.value[index].expanded
}

/**
 * 标记是否有帮助
 */
const markHelpful = (index, helpful) => {
  faqList.value[index].helpful = helpful
  uni.showToast({
    title: '感谢您的反馈',
    icon: 'success'
  })
  // TODO: 调用API记录反馈
}

/**
 * 在线客服
 */
const contactOnlineService = () => {
  uni.navigateTo({
    url: '/pages-merchant/chat/index?type=service'
  })
}

/**
 * 电话客服
 */
const callService = () => {
  uni.makePhoneCall({
    phoneNumber: '400-123-4567'
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.tutorials-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 40rpx;
}

/* 卡片 */
.section-card {
  background: #fff;
  margin: 20rpx;
  padding: 30rpx;
  border-radius: 16rpx;
}

.section-header {
  display: flex;
  flex-direction: column;
  gap: 5rpx;
  margin-bottom: 25rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.section-desc {
  font-size: 24rpx;
  color: #999;
}

/* 快速入门 */
.tutorial-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.tutorial-card {
  position: relative;
  border-radius: 12rpx;
  overflow: hidden;
  background: #F5F5F5;
}

.tutorial-thumb {
  width: 100%;
  height: 300rpx;
}

.tutorial-info {
  padding: 20rpx;
  position: relative;
}

.tutorial-tag {
  display: inline-block;
  padding: 4rpx 12rpx;
  background: #FF6B35;
  color: #fff;
  font-size: 22rpx;
  border-radius: 4rpx;
  margin-bottom: 10rpx;
}

.tutorial-title {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
  display: block;
  margin-bottom: 10rpx;
}

.tutorial-meta {
  display: flex;
  gap: 20rpx;
}

.meta-item {
  font-size: 24rpx;
  color: #999;
  display: flex;
  align-items: center;
  gap: 5rpx;
}

.play-icon {
  position: absolute;
  top: 110rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 80rpx;
  height: 80rpx;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  @include flex-center;
}

/* 分类标签 */
.category-tabs {
  margin-bottom: 20rpx;
}

.tabs-scroll {
  white-space: nowrap;
}

.tab-item {
  display: inline-block;
  padding: 10rpx 24rpx;
  margin-right: 20rpx;
  font-size: 26rpx;
  color: #666;
  border-radius: 20rpx;
  background: #F5F5F5;

  &.active {
    background: #FF6B35;
    color: #fff;
  }
}

/* 功能教程 */
.tutorial-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
  margin-bottom: 15rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.item-icon {
  width: 70rpx;
  height: 70rpx;
  border-radius: 50%;
  @include flex-center;
  flex-shrink: 0;

  &.type-order {
    background: linear-gradient(135deg, #1890FF, #40A9FF);
  }

  &.type-dish {
    background: linear-gradient(135deg, #52C41A, #73D13D);
  }

  &.type-marketing {
    background: linear-gradient(135deg, #FA541C, #FF7A45);
  }

  &.type-finance {
    background: linear-gradient(135deg, #722ED1, #9254DE);
  }

  &.type-data {
    background: linear-gradient(135deg, #13C2C2, #36CFC9);
  }
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.item-title {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.item-desc {
  font-size: 24rpx;
  color: #999;
}

.item-action {
  flex-shrink: 0;
}

/* 常见问题 */
.faq-list {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.faq-item {
  background: #F5F5F5;
  border-radius: 12rpx;
  overflow: hidden;
}

.faq-question {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25rpx 20rpx;
  cursor: pointer;
}

.question-text {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  padding-right: 20rpx;
}

.faq-answer {
  padding: 0 20rpx 25rpx;
  border-top: 1rpx solid #eee;
  padding-top: 20rpx;
}

.answer-text {
  font-size: 26rpx;
  color: #666;
  line-height: 1.8;
  display: block;
  margin-bottom: 20rpx;
}

.answer-actions {
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.helpful-text {
  font-size: 24rpx;
  color: #999;
}

.helpful-buttons {
  display: flex;
  gap: 10rpx;
}

.helpful-btn {
  display: flex;
  align-items: center;
  gap: 5rpx;
  padding: 8rpx 16rpx;
  background: #fff;
  border-radius: 20rpx;
  font-size: 24rpx;
  color: #666;
  border: 1rpx solid #eee;

  &.active {
    background: #FF6B35;
    color: #fff;
    border-color: #FF6B35;
  }
}

/* 联系支持 */
.support-card {
  background: linear-gradient(135deg, #FF6B35, #FF8F6B);
  padding: 40rpx 30rpx;
  margin: 20rpx;
  border-radius: 16rpx;
}

.support-content {
  text-align: center;
  margin-bottom: 30rpx;
}

.support-title {
  display: block;
  font-size: 32rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 10rpx;
}

.support-desc {
  display: block;
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
}

.support-actions {
  display: flex;
  gap: 20rpx;
}

.support-btn {
  flex: 1;
  height: 80rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  border: none;

  &.primary {
    background: #fff;
    color: #FF6B35;
  }

  &:not(.primary) {
    background: rgba(255, 255, 255, 0.2);
    color: #fff;
    border: 1rpx solid rgba(255, 255, 255, 0.3);
  }
}
</style>
