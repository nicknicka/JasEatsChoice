<template>
  <view class="help-container">
    <!-- 搜索框 -->
    <view class="search-section">
      <view class="search-box">
        <text class="search-icon">🔍</text>
        <input
          class="search-input"
          type="text"
          v-model="searchKeyword"
          placeholder="搜索问题"
          @confirm="handleSearch"
        />
      </view>
    </view>

    <!-- 常见问题 -->
    <view class="faq-section">
      <view class="section-header">
        <text class="section-title">常见问题</text>
      </view>

      <!-- FAQ分类Tab -->
      <view class="category-tabs">
        <scroll-view class="tabs-scroll" scroll-x>
          <view
            class="tab-item"
            :class="{ active: activeCategory === 'all' }"
            @click="changeCategory('all')"
          >
            <text class="tab-text">全部</text>
          </view>
          <view
            class="tab-item"
            :class="{ active: activeCategory === item.value }"
            v-for="item in categories"
            :key="item.value"
            @click="changeCategory(item.value)"
          >
            <text class="tab-text">{{ item.label }}</text>
          </view>
        </scroll-view>
      </view>

      <!-- FAQ列表 -->
      <view class="faq-list">
        <!-- 搜索结果 -->
        <view class="search-results" v-if="searchKeyword">
          <view
            class="faq-item"
            v-for="item in searchResults"
            :key="item.id"
            @click="viewFaqDetail(item)"
          >
            <view class="faq-question">
              <text class="question-text">{{ item.question }}</text>
              <text class="question-arrow">›</text>
            </view>
            <view class="faq-answer" v-if="item.showAnswer">
              <text class="answer-text">{{ item.answer }}</text>
            </view>
          </view>

          <!-- 空状态 -->
          <view class="empty-state" v-if="searchResults.length === 0">
            <Empty
              icon="🔍"
              text="未找到相关问题"
              description="换个关键词试试"
            />
          </view>
        </view>

        <!-- 分类问题列表 -->
        <view class="category-questions" v-else>
          <view
            class="faq-item"
            v-for="item in categoryQuestions"
            :key="item.id"
            @click="viewFaqDetail(item)"
          >
            <view class="faq-question">
              <text class="question-icon">❓</text>
              <text class="question-text">{{ item.question }}</text>
              <text class="question-arrow">›</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 快捷入口 -->
    <view class="quick-links">
      <view class="section-header">
        <text class="section-title">快捷服务</text>
      </view>

      <view class="links-grid">
        <view
          class="link-item"
          v-for="link in quickLinks"
          :key="link.id"
          @click="handleQuickLink(link)"
        >
          <text class="link-icon">{{ link.icon }}</text>
          <text class="link-text">{{ link.name }}</text>
        </view>
      </view>
    </view>

    <!-- 联系我们 -->
    <view class="contact-section">
      <view class="section-header">
        <text class="section-title">联系我们</text>
      </view>

      <view class="contact-list">
        <view class="contact-item" @click="makePhoneCall">
          <text class="contact-icon">📞</text>
          <view class="contact-info">
            <text class="contact-title">客服热线</text>
            <text class="contact-desc">400-123-4567</text>
          </view>
          <text class="contact-arrow">›</text>
        </view>

        <view class="contact-item" @click="openChat">
          <text class="contact-icon">💬</text>
          <view class="contact-info">
            <text class="contact-title">在线客服</text>
            <text class="contact-desc">7x24小时在线</text>
          </view>
          <text class="contact-arrow">›</text>
        </view>

        <view class="contact-item" @click="sendEmail">
          <text class="contact-icon">📧</text>
          <view class="contact-info">
            <text class="contact-title">邮箱反馈</text>
            <text class="contact-desc">support@example.com</text>
          </view>
          <text class="contact-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- FAQ详情弹窗 -->
    <uni-popup ref="faqPopup" type="center">
      <view class="faq-popup" v-if="currentFaq">
        <view class="popup-header">
          <text class="popup-title">问题详情</text>
          <text class="popup-close" @click="closeFaqPopup">×</text>
        </view>

        <scroll-view class="popup-content" scroll-y>
          <view class="faq-detail">
            <text class="detail-question">{{ currentFaq.question }}</text>
            <view class="detail-answer">
              <text class="answer-text">{{ currentFaq.answer }}</text>
            </view>
          </view>
        </scroll-view>

        <view class="popup-footer">
          <button class="footer-btn" @click="faqNotHelpful">没帮助</button>
          <button class="footer-btn primary" @click="faqHelpful">有帮助</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store'
import Empty from '@/components/common/Empty.vue'
import { feedbackApi } from '@/api'

// 用户信息store
const userStore = useUserStore()

// 搜索关键词
const searchKeyword = ref('')

// 当前激活的分类
const activeCategory = ref('all')

// FAQ列表
const faqList = ref([])

// 当前查看的FAQ
const currentFaq = ref(null)

// 分类列表
const categories = [
  { value: 'order', label: '订单问题' },
  { value: 'payment', label: '支付问题' },
  { value: 'delivery', label: '配送问题' },
  { value: 'account', label: '账户问题' },
  { value: 'other', label: '其他问题' }
]

// 快捷入口
const quickLinks = [
  { id: 1, name: '意见反馈', icon: '📝', action: 'feedback' },
  { id: 2, name: '用户协议', icon: '📄', action: 'agreement' },
  { id: 3, name: '隐私政策', icon: '🔒', action: 'privacy' },
  { id: 4, name: '关于我们', icon: 'ℹ️', action: 'about' }
]

// 搜索结果
const searchResults = computed(() => {
  if (!searchKeyword.value) return []

  const keyword = searchKeyword.value.toLowerCase()
  return faqList.value.filter(item =>
    item.question.toLowerCase().includes(keyword) ||
    item.answer.toLowerCase().includes(keyword)
  )
})

// 分类问题列表
const categoryQuestions = computed(() => {
  if (activeCategory.value === 'all') {
    return faqList.value.slice(0, 10)
  }
  return faqList.value.filter(item => item.category === activeCategory.value)
})

/**
 * 加载FAQ列表
 */
const loadFaqList = async () => {
  try {
    // 调用feedbackApi获取FAQ
    const res = await feedbackApi.getFAQ()

    if (res && res.data) {
      faqList.value = res.data || []
    } else {
      // 使用模拟数据
      faqList.value = [
        {
          id: 1,
          category: 'order',
          question: '如何查看订单状态？',
          answer: '您可以在"我的"页面点击"我的订单"，查看所有订单的状态。点击具体订单可以查看详细的配送进度和骑手信息。'
        },
        {
          id: 2,
          category: 'order',
          question: '如何取消订单？',
          answer: '订单在商家接单前可以取消。进入订单详情页，点击"取消订单"按钮即可。如果商家已接单，需要联系商家协商处理。'
        },
        {
          id: 3,
          category: 'payment',
          question: '支持哪些支付方式？',
          answer: '目前支持微信支付、支付宝、余额支付等多种支付方式。您可以在结算时选择您偏好的支付方式。'
        },
        {
          id: 4,
          category: 'delivery',
          question: '配送时间一般是多久？',
          answer: '一般情况下，配送时间为30-45分钟。具体时间会根据距离、天气、餐品制作时间等因素有所变化，请以APP显示的预计送达时间为准。'
        },
        {
          id: 5,
          category: 'account',
          question: '如何修改个人资料？',
          answer: '进入"我的"页面，点击头像进入个人中心，即可编辑昵称、头像、性别、生日等个人信息。'
        },
        {
          id: 6,
          category: 'other',
          question: '如何联系客服？',
          answer: '您可以通过以下方式联系我们：1. 客服热线：400-123-4567；2. 在线客服：在"我的"页面点击"联系客服"；3. 邮箱：support@example.com'
        }
      ]
    }
  } catch (error) {
    console.error('加载FAQ失败:', error)

    // 使用模拟数据
    faqList.value = [
      {
        id: 1,
        category: 'order',
        question: '如何查看订单状态？',
        answer: '您可以在"我的"页面点击"我的订单"，查看所有订单的状态。点击具体订单可以查看详细的配送进度和骑手信息。'
      },
      {
        id: 2,
        category: 'order',
        question: '如何取消订单？',
        answer: '订单在商家接单前可以取消。进入订单详情页，点击"取消订单"按钮即可。如果商家已接单，需要联系商家协商处理。'
      },
      {
        id: 3,
        category: 'payment',
        question: '支持哪些支付方式？',
        answer: '目前支持微信支付、支付宝、余额支付等多种支付方式。您可以在结算时选择您偏好的支付方式。'
      },
      {
        id: 4,
        category: 'delivery',
        question: '配送时间一般是多久？',
        answer: '一般情况下，配送时间为30-45分钟。具体时间会根据距离、天气、餐品制作时间等因素有所变化，请以APP显示的预计送达时间为准。'
      },
      {
        id: 5,
        category: 'account',
        question: '如何修改个人资料？',
        answer: '进入"我的"页面，点击头像进入个人中心，即可编辑昵称、头像、性别、生日等个人信息。'
      },
      {
        id: 6,
        category: 'other',
        question: '如何联系客服？',
        answer: '您可以通过以下方式联系我们：1. 客服热线：400-123-4567；2. 在线客服：在"我的"页面点击"联系客服"；3. 邮箱：support@example.com'
      }
    ]
  }
}

/**
 * 搜索问题
 */
const handleSearch = () => {
  // 搜索结果会自动通过计算属性更新
}

/**
 * 切换分类
 */
const changeCategory = (category) => {
  activeCategory.value = category
}

/**
 * 查看FAQ详情
 */
const viewFaqDetail = (item) => {
  currentFaq.value = item
  // 打开弹窗
  uni.showModal({
    title: item.question,
    content: item.answer,
    showCancel: false
  })
}

/**
 * 关闭FAQ弹窗
 */
const closeFaqPopup = () => {
  currentFaq.value = null
}

/**
 * FAQ有帮助
 */
const faqHelpful = () => {
  uni.showToast({
    title: '感谢您的反馈',
    icon: 'success'
  })
  closeFaqPopup()
}

/**
 * FAQ没帮助
 */
const faqNotHelpful = () => {
  uni.showToast({
    title: '我们会继续改进',
    icon: 'none'
  })
  // 可以跳转到反馈页面
  uni.navigateTo({
    url: '/pages/feedback/index'
  })
  closeFaqPopup()
}

/**
 * 处理快捷入口
 */
const handleQuickLink = (link) => {
  switch (link.action) {
    case 'feedback':
      uni.navigateTo({
        url: '/pages/feedback/index'
      })
      break
    case 'agreement':
      uni.navigateTo({
        url: '/pages/agreement/index?type=user'
      })
      break
    case 'privacy':
      uni.navigateTo({
        url: '/pages/agreement/index?type=privacy'
      })
      break
    case 'about':
      uni.navigateTo({
        url: '/pages/about/index'
      })
      break
  }
}

/**
 * 拨打电话
 */
const makePhoneCall = () => {
  uni.makePhoneCall({
    phoneNumber: '400-123-4567'
  })
}

/**
 * 打开聊天
 */
const openChat = () => {
  uni.navigateTo({
    url: '/pages/service/chat/index'
  })
}

/**
 * 发送邮件
 */
const sendEmail = () => {
  uni.setClipboardData({
    data: 'support@example.com',
    success: () => {
      uni.showToast({
        title: '邮箱已复制',
        icon: 'success'
      })
    }
  })
}

// 组件挂载
onMounted(() => {
  loadFaqList()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.help-container {
  min-height: 100vh;
  background-color: $bg-color-base;
  padding-bottom: $spacing-md;
}

/* 搜索区域 */
.search-section {
  background-color: $bg-color-white;
  padding: $spacing-md;
  box-shadow: $box-shadow-sm;
}

.search-box {
  @include flex-center;
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-round;
}

.search-icon {
  font-size: $font-size-xl;
  margin-right: $spacing-sm;
}

.search-input {
  flex: 1;
  font-size: $font-size-base;
  color: $text-color-primary;
}

/* 常见问题 */
.faq-section {
  background-color: $bg-color-white;
  margin: $spacing-md;
  padding: $spacing-lg;
  border-radius: $border-radius-lg;
  box-shadow: $box-shadow-sm;
}

.section-header {
  margin-bottom: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

/* 分类Tab */
.category-tabs {
  margin-bottom: $spacing-md;
}

.tabs-scroll {
  white-space: nowrap;
}

.tab-item {
  display: inline-block;
  padding: $spacing-sm $spacing-md;
  margin-right: $spacing-sm;
  background-color: $bg-color-base;
  border-radius: $border-radius-round;
  transition: all 0.3s;

  &.active {
    background-color: $primary-color;

    .tab-text {
      color: #fff;
    }
  }

  &:active {
    transform: scale(0.95);
  }
}

.tab-text {
  font-size: $font-size-sm;
  color: $text-color-regular;
}

/* FAQ列表 */
.faq-list {
  @include flex-center-column;
}

.faq-item {
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  margin-bottom: $spacing-sm;

  &:last-child {
    margin-bottom: 0;
  }

  &:active {
    background-color: rgba(0, 0, 0, 0.02);
  }
}

.faq-question {
  @include flex-center;
}

.question-icon {
  font-size: $font-size-xl;
  margin-right: $spacing-sm;
}

.question-text {
  flex: 1;
  font-size: $font-size-base;
  color: $text-color-primary;
  line-height: $line-height-lg;
}

.question-arrow {
  font-size: $font-size-xl;
  color: $text-color-placeholder;
}

.faq-answer {
  margin-top: $spacing-md;
  padding-top: $spacing-md;
  border-top: 1rpx solid $border-color-lighter;
}

.answer-text {
  font-size: $font-size-sm;
  color: $text-color-regular;
  line-height: $line-height-lg;
}

/* 空状态 */
.empty-state {
  padding: 80rpx 0;
}

/* 快捷入口 */
.quick-links {
  background-color: $bg-color-white;
  margin: $spacing-md;
  padding: $spacing-lg;
  border-radius: $border-radius-lg;
  box-shadow: $box-shadow-sm;
}

.links-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-lg;
}

.link-item {
  @include flex-center-column;
  gap: $spacing-sm;

  &:active {
    transform: scale(0.95);
  }
}

.link-icon {
  font-size: 48rpx;
}

.link-text {
  font-size: $font-size-sm;
  color: $text-color-primary;
  text-align: center;
}

/* 联系我们 */
.contact-section {
  background-color: $bg-color-white;
  margin: $spacing-md;
  padding: $spacing-lg;
  border-radius: $border-radius-lg;
  box-shadow: $box-shadow-sm;
}

.contact-list {
  @include flex-center-column;
  gap: $spacing-md;
}

.contact-item {
  @include flex-center;
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;

  &:active {
    background-color: rgba(0, 0, 0, 0.02);
  }
}

.contact-icon {
  font-size: $font-size-xl;
  margin-right: $spacing-md;
}

.contact-info {
  flex: 1;
  @include flex-center-column;
  align-items: flex-start;
  gap: 4rpx;
}

.contact-title {
  font-size: $font-size-base;
  color: $text-color-primary;
}

.contact-desc {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.contact-arrow {
  font-size: $font-size-xl;
  color: $text-color-placeholder;
}

/* FAQ详情弹窗 */
.faq-popup {
  width: 600rpx;
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  overflow: hidden;
}

.popup-header {
  @include flex-between;
  align-items: center;
  padding: $spacing-lg;
  border-bottom: 1rpx solid $border-color-lighter;
}

.popup-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.popup-close {
  width: 48rpx;
  height: 48rpx;
  @include flex-center;
  font-size: 48rpx;
  color: $text-color-placeholder;

  &:active {
    opacity: 0.6;
  }
}

.popup-content {
  max-height: 600rpx;
  padding: $spacing-lg;
}

.faq-detail {
  @include flex-center-column;
  gap: $spacing-md;
}

.detail-question {
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
  line-height: $line-height-lg;
}

.detail-answer {
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
}

.answer-text {
  font-size: $font-size-sm;
  color: $text-color-regular;
  line-height: $line-height-lg;
}

.popup-footer {
  @include flex-center;
  gap: $spacing-md;
  padding: $spacing-lg;
  border-top: 1rpx solid $border-color-lighter;
}

.footer-btn {
  flex: 1;
  height: 80rpx;
  @include flex-center;
  background-color: $bg-color-base;
  color: $text-color-primary;
  border-radius: $border-radius-base;
  font-size: $font-size-base;
  border: none;

  &.primary {
    background-color: $primary-color;
    color: #fff;
  }

  &:active {
    transform: scale(0.98);
  }
}
</style>
