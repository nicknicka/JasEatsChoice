<template>
  <view class="user-center-container">
    <scroll-view
      class="scroll-container"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="loading-container" v-if="loading">
        <uni-load-more status="loading" :content-text="{ contentdown: '加载中...' }"></uni-load-more>
      </view>

      <view class="not-login-container" v-else-if="!userStore.isLogin">
        <view class="not-login-content">
          <uni-icons type="person-filled" size="52" color="#FF6B35"></uni-icons>
          <text class="not-login-text">您还未登录</text>
          <text class="not-login-desc">登录后可查看订单、卡路里记录、收藏与钱包信息</text>
          <button class="login-btn" @click="goToLogin">立即登录</button>
        </view>
      </view>

      <template v-else>
        <view class="user-info-card">
          <view class="user-avatar">
            <image class="avatar-image" :src="userInfo.avatar || DEFAULT_AVATAR" mode="aspectFill" />
            <view class="avatar-badge" v-if="userInfo.vipLevel > 0">
              <text class="badge-text">VIP{{ userInfo.vipLevel }}</text>
            </view>
          </view>

          <view class="user-detail">
            <view class="user-name-row">
              <text class="user-name">{{ displayName }}</text>
              <view class="gender-badge" v-if="genderLabel">
                <text class="gender-badge-text">{{ genderLabel }}</text>
              </view>
            </view>

            <view class="meta-pills" v-if="profileMetaItems.length > 0">
              <view class="meta-pill" v-for="item in profileMetaItems" :key="item.text">
                <uni-icons :type="item.icon" size="13" color="#FFFFFF"></uni-icons>
                <text class="meta-pill-text">{{ item.text }}</text>
              </view>
            </view>

            <text class="user-subline">{{ profileHint }}</text>

            <view class="user-tags" v-if="displayTags.length > 0">
              <text class="tag-item" v-for="tag in displayTags" :key="tag">{{ tag }}</text>
            </view>
          </view>

          <view class="edit-btn" @click="editProfile">
            <uni-icons type="compose" size="18" color="#FFFFFF"></uni-icons>
            <text class="edit-btn-text">编辑</text>
          </view>
        </view>

        <view class="stats-card">
          <view
            v-for="item in statsItems"
            :key="item.key"
            class="stat-item"
            @click="navigateTo(item.page)"
          >
            <text class="stat-value">{{ formatBadgeValue(item.value) }}</text>
            <text class="stat-label">{{ item.label }}</text>
          </view>
        </view>

        <view class="section-card">
          <view class="section-header" @click="navigateTo('orders')">
            <view class="section-title-wrap">
              <text class="section-title">我的订单</text>
              <text class="section-subtitle">查看待支付、制作中、配送中和待评价订单</text>
            </view>
            <view class="section-more">
              <text>查看全部</text>
              <uni-icons type="right" size="14" color="#999999"></uni-icons>
            </view>
          </view>

          <view class="order-status-grid">
            <view
              v-for="item in orderQuickActions"
              :key="item.key"
              class="status-item"
              @click="navigateTo('orders', item.params)"
            >
              <view class="status-icon-wrapper" :style="{ backgroundColor: item.bgColor }">
                <uni-icons :type="item.icon" size="24" :color="item.color"></uni-icons>
                <view class="status-badge" v-if="item.count > 0">
                  <text class="status-badge-text">{{ formatBadgeValue(item.count) }}</text>
                </view>
              </view>
              <text class="status-text">{{ item.label }}</text>
            </view>
          </view>
        </view>

        <view class="section-card">
          <view class="section-header" @click="navigateTo('wallet')">
            <view class="section-title-wrap">
              <text class="section-title">钱包与积分</text>
              <text class="section-subtitle">余额、积分和优惠券统一查看</text>
            </view>
            <view class="section-more">
              <text>查看全部</text>
              <uni-icons type="right" size="14" color="#999999"></uni-icons>
            </view>
          </view>

          <view class="assets-list">
            <view
              v-for="item in benefitItems"
              :key="item.key"
              class="asset-item"
              @click="navigateTo(item.page)"
            >
              <text class="asset-value" :class="{ money: item.money }">{{ item.value }}</text>
              <text class="asset-label">{{ item.label }}</text>
              <text class="asset-desc">{{ item.desc }}</text>
            </view>
          </view>
        </view>

        <view class="section-card">
          <view class="section-header">
            <view class="section-title-wrap">
              <text class="section-title">常用功能</text>
              <text class="section-subtitle">优先保留已落地且高频使用的入口</text>
            </view>
          </view>

          <view class="menu-list">
            <view
              v-for="item in primaryMenus"
              :key="item.key"
              class="menu-item"
              @click="navigateTo(item.page)"
            >
              <view class="menu-icon-wrapper">
                <uni-icons :type="item.icon" size="22" :color="item.iconColor || '#FF6B35'"></uni-icons>
              </view>
              <view class="menu-content">
                <text class="menu-label">{{ item.label }}</text>
                <text class="menu-desc">{{ item.desc }}</text>
              </view>
              <uni-icons type="right" size="16" color="#B7B7B7"></uni-icons>
            </view>
          </view>
        </view>

        <view class="section-card">
          <view class="section-header">
            <view class="section-title-wrap">
              <text class="section-title">服务与设置</text>
              <text class="section-subtitle">消息、帮助、反馈和账号设置</text>
            </view>
          </view>

          <view class="menu-list">
            <view
              v-for="item in supportMenus"
              :key="item.key"
              class="menu-item"
              @click="navigateTo(item.page)"
            >
              <view class="menu-icon-wrapper">
                <uni-icons :type="item.icon" size="22" :color="item.iconColor || '#FF6B35'"></uni-icons>
                <view class="menu-badge" v-if="item.badge > 0">
                  <text class="menu-badge-text">{{ formatBadgeValue(item.badge) }}</text>
                </view>
              </view>
              <view class="menu-content">
                <text class="menu-label">{{ item.label }}</text>
                <text class="menu-desc">{{ item.desc }}</text>
              </view>
              <uni-icons type="right" size="16" color="#B7B7B7"></uni-icons>
            </view>

            <view class="menu-item logout-item" @click="handleLogout">
              <view class="menu-icon-wrapper logout-icon">
                <uni-icons type="redo" size="22" color="#F56C6C"></uni-icons>
              </view>
              <view class="menu-content">
                <text class="menu-label logout-text">退出登录</text>
                <text class="menu-desc">退出当前账号并清除本地登录状态</text>
              </view>
            </view>
          </view>
        </view>

        <view class="bottom-spacer"></view>
      </template>
    </scroll-view>
  </view>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/store'
import { chatApi, orderApi, userApi, walletApi } from '@/api'
import { createPageDebug } from '@/utils/page-debug'
import {
  LOGIN,
  MESSAGE,
  USER_ADDRESS,
  USER_CALORIE,
  USER_CALORIE_STATISTICS,
  USER_COLLECTION,
  USER_COUPON,
  USER_FEEDBACK,
  USER_HELP,
  USER_HISTORY,
  USER_ORDERS,
  USER_PROFILE_ABOUT,
  USER_PROFILE_EDIT,
  USER_PROFILE_INTEGRAL,
  USER_RECIPE_MY,
  USER_SETTINGS,
  USER_WALLET
} from '@/constants/routes'

const DEFAULT_AVATAR = '/static/images/default-avatar.png'

const GOAL_LABEL_MAP = {
  lose_weight: '减脂中',
  gain_muscle: '增肌中',
  keep_fit: '保持健康',
  maintain: '保持体重',
  no_goal: '无特殊目标'
}

const TASTE_LABEL_MAP = {
  spicy: '辣味',
  sweet: '甜味',
  sour: '酸味',
  salty: '咸鲜',
  light: '清淡'
}

const TAB_BAR_ROUTES = new Set([
  '/pages/home/index/index',
  '/pages/recipe/index',
  '/pages/ai/index',
  MESSAGE,
  '/pages/profile/user-center/index'
])

const userStore = useUserStore()
const pageDebug = createPageDebug('我的')

const userInfo = ref(createDefaultUserInfo())
const stats = ref(createDefaultStats())
const orderCounts = ref(createDefaultOrderCounts())
const wallet = ref(createDefaultWallet())
const unreadCount = ref(0)
const loading = ref(false)
const refreshing = ref(false)
const pageInitialized = ref(false)

const displayName = computed(() => userInfo.value.name || '佳食宜选用户')

const genderLabel = computed(() => {
  const gender = userInfo.value.gender
  if (gender === 'male' || gender === 1 || gender === '1' || gender === '男') {
    return '男'
  }
  if (gender === 'female' || gender === 2 || gender === '2' || gender === '女') {
    return '女'
  }
  return ''
})

const goalLabel = computed(() => GOAL_LABEL_MAP[userInfo.value.goal] || '')

const maskedPhone = computed(() => {
  const phone = String(userInfo.value.phone || '')
  if (!phone) {
    return ''
  }
  if (phone.length >= 11) {
    return `${phone.slice(0, 3)}****${phone.slice(-4)}`
  }
  return phone
})

const displayTags = computed(() => {
  const rawTags = Array.isArray(userInfo.value.tags) && userInfo.value.tags.length > 0
    ? userInfo.value.tags
    : userInfo.value.taste

  return normalizeTags(rawTags).slice(0, 4)
})

const profileMetaItems = computed(() => {
  const items = []

  if (maskedPhone.value) {
    items.push({ icon: 'phone-filled', text: maskedPhone.value })
  }
  if (goalLabel.value) {
    items.push({ icon: 'fire-filled', text: goalLabel.value })
  }
  if (userInfo.value.vipLevel > 0) {
    items.push({ icon: 'vip-filled', text: `会员 Lv.${userInfo.value.vipLevel}` })
  }

  return items
})

const profileHint = computed(() => {
  if (userInfo.value.bio) {
    return userInfo.value.bio
  }
  if (userInfo.value.id) {
    return `用户ID：${userInfo.value.id}`
  }
  return '完善饮食偏好后，可获得更精准的饮食推荐'
})

const statsItems = computed(() => ([
  { key: 'orders', label: '订单', value: stats.value.orders, page: 'orders' },
  { key: 'favorites', label: '收藏', value: stats.value.favorites, page: 'favorites' },
  { key: 'history', label: '浏览', value: stats.value.history, page: 'history' },
  { key: 'coupons', label: '优惠券', value: stats.value.coupons, page: 'coupons' }
]))

const orderQuickActions = computed(() => ([
  {
    key: 'pending',
    label: '待支付',
    icon: 'wallet-filled',
    color: '#FF6B35',
    bgColor: '#FFF1EB',
    count: orderCounts.value.pending,
    params: { status: 'pending' }
  },
  {
    key: 'processing',
    label: '制作中',
    icon: 'spinner-cycle',
    color: '#FF9800',
    bgColor: '#FFF7E8',
    count: orderCounts.value.processing,
    params: { status: 'processing' }
  },
  {
    key: 'delivering',
    label: '配送中',
    icon: 'paperplane-filled',
    color: '#409EFF',
    bgColor: '#EDF5FF',
    count: orderCounts.value.delivering,
    params: { status: 'delivering' }
  },
  {
    key: 'completed',
    label: '待评价',
    icon: 'star-filled',
    color: '#67C23A',
    bgColor: '#EFF8EB',
    count: orderCounts.value.completed,
    params: { status: 'completed' }
  }
]))

const benefitItems = computed(() => ([
  {
    key: 'wallet',
    label: '余额',
    value: `¥${formatMoney(wallet.value.balance)}`,
    desc: '钱包管理',
    page: 'wallet',
    money: true
  },
  {
    key: 'integral',
    label: '积分',
    value: formatBadgeValue(wallet.value.points),
    desc: '查看积分明细',
    page: 'integral'
  },
  {
    key: 'coupons',
    label: '优惠券',
    value: formatBadgeValue(stats.value.coupons),
    desc: '查看可用优惠',
    page: 'coupons'
  }
]))

const primaryMenus = computed(() => ([
  {
    key: 'address',
    label: '收货地址',
    desc: '管理常用配送地址',
    icon: 'location-filled',
    page: 'address'
  },
  {
    key: 'calorie',
    label: '卡路里管理',
    desc: '查看每日摄入和营养趋势',
    icon: 'fire-filled',
    page: 'calorie'
  },
  {
    key: 'calorie-statistics',
    label: '统计分析',
    desc: '进入卡路里图表与趋势页',
    icon: 'chartbar',
    page: 'calorie-statistics'
  },
  {
    key: 'recipe',
    label: '我的食谱',
    desc: '管理自定义与收藏食谱',
    icon: 'list',
    page: 'recipe'
  },
  {
    key: 'favorites',
    label: '我的收藏',
    desc: `已收藏 ${formatBadgeValue(stats.value.favorites)} 项内容`,
    icon: 'star-filled',
    page: 'favorites'
  }
]))

const supportMenus = computed(() => ([
  {
    key: 'history',
    label: '浏览历史',
    desc: `最近浏览 ${formatBadgeValue(stats.value.history)} 项内容`,
    icon: 'eye-filled',
    page: 'history'
  },
  {
    key: 'message',
    label: '消息中心',
    desc: unreadCount.value > 0
      ? `当前有 ${formatBadgeValue(unreadCount.value)} 条未读消息`
      : '查看聊天与系统消息',
    icon: 'chatbubble-filled',
    page: 'message',
    badge: unreadCount.value
  },
  {
    key: 'help',
    label: '帮助中心',
    desc: '常见问题与客服入口',
    icon: 'help-filled',
    page: 'help'
  },
  {
    key: 'settings',
    label: '设置',
    desc: '账号、通知与隐私配置',
    icon: 'settings-filled',
    page: 'settings'
  },
  {
    key: 'feedback',
    label: '意见反馈',
    desc: '提交建议和使用问题',
    icon: 'compose',
    page: 'feedback'
  },
  {
    key: 'about',
    label: '关于我们',
    desc: '查看产品介绍与版本信息',
    icon: 'info-filled',
    page: 'about'
  }
]))

function createDefaultUserInfo() {
  return {
    id: '',
    name: '佳食宜选用户',
    avatar: DEFAULT_AVATAR,
    gender: '',
    tags: [],
    taste: [],
    goal: '',
    phone: '',
    bio: '',
    vipLevel: 0
  }
}

function createDefaultStats() {
  return {
    orders: 0,
    favorites: 0,
    history: 0,
    coupons: 0
  }
}

function createDefaultOrderCounts() {
  return {
    pending: 0,
    processing: 0,
    delivering: 0,
    completed: 0
  }
}

function createDefaultWallet() {
  return {
    balance: '0.00',
    points: 0
  }
}

function unwrapResponse(response) {
  if (response && typeof response === 'object' && 'data' in response && response.data !== undefined && response.data !== null) {
    return response.data
  }
  return response || {}
}

function toNumber(value) {
  const numeric = Number(value)
  return Number.isFinite(numeric) ? numeric : 0
}

function formatMoney(value) {
  const numeric = Number(value)
  return Number.isFinite(numeric) ? numeric.toFixed(2) : '0.00'
}

function formatBadgeValue(value) {
  const numeric = toNumber(value)
  return numeric > 99 ? '99+' : String(numeric)
}

function normalizeTags(tags) {
  if (!Array.isArray(tags)) {
    return []
  }

  return tags
    .map(tag => TASTE_LABEL_MAP[tag] || String(tag || '').trim())
    .filter(Boolean)
}

function mapUserInfo(source) {
  const normalized = source || {}

  return {
    id: normalized.userId || normalized.id || '',
    name: normalized.nickname || normalized.name || '佳食宜选用户',
    avatar: normalized.avatar || DEFAULT_AVATAR,
    gender: normalized.gender ?? '',
    tags: Array.isArray(normalized.tags) ? normalized.tags : [],
    taste: Array.isArray(normalized.taste) ? normalized.taste : [],
    goal: normalized.goal || normalized.dietGoal || '',
    phone: normalized.phone || '',
    bio: normalized.bio || normalized.signature || '',
    vipLevel: toNumber(normalized.vipLevel || normalized.memberLevel)
  }
}

function goToLogin() {
  pageDebug.action('前往登录页')
  uni.navigateTo({ url: LOGIN })
}

function openPage(url) {
  if (TAB_BAR_ROUTES.has(url)) {
    uni.switchTab({ url })
    return
  }

  uni.navigateTo({
    url,
    fail: () => {
      uni.showToast({
        title: '页面开发中...',
        icon: 'none'
      })
    }
  })
}

function editProfile() {
  pageDebug.action('编辑个人资料')
  openPage(USER_PROFILE_EDIT)
}

function handleLogout() {
  pageDebug.action('尝试退出登录')
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (!res.confirm) {
        return
      }

      userStore.logout()
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
      uni.removeStorageSync('userId')
      uni.removeStorageSync('role')

      userInfo.value = createDefaultUserInfo()
      stats.value = createDefaultStats()
      orderCounts.value = createDefaultOrderCounts()
      wallet.value = createDefaultWallet()
      unreadCount.value = 0

      uni.showToast({
        title: '已退出登录',
        icon: 'success'
      })
    }
  })
}

function navigateTo(page, params = {}) {
  pageDebug.action('个人中心页面跳转', { page, params })

  if (!userStore.isLogin && page !== 'login') {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    setTimeout(() => {
      uni.navigateTo({ url: LOGIN })
    }, 300)
    return
  }

  const pageMap = {
    orders: USER_ORDERS,
    favorites: USER_COLLECTION,
    history: USER_HISTORY,
    coupons: USER_COUPON,
    wallet: USER_WALLET,
    integral: USER_PROFILE_INTEGRAL,
    address: USER_ADDRESS,
    calorie: USER_CALORIE,
    'calorie-statistics': USER_CALORIE_STATISTICS,
    recipe: USER_RECIPE_MY,
    message: MESSAGE,
    help: USER_HELP,
    feedback: USER_FEEDBACK,
    about: USER_PROFILE_ABOUT,
    settings: USER_SETTINGS,
    login: LOGIN
  }

  const path = pageMap[page]

  if (!path) {
    pageDebug.anomaly('目标页面未实现', { page })
    uni.showToast({
      title: '页面开发中...',
      icon: 'none'
    })
    return
  }

  const query = Object.keys(params || {})
    .filter(key => params[key] !== undefined && params[key] !== null && params[key] !== '')
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    .join('&')

  openPage(query ? `${path}?${query}` : path)
}

async function loadUserInfo() {
  if (!userStore.isLogin) {
    userInfo.value = createDefaultUserInfo()
    return
  }

  const localUserInfo = userStore.userInfo || uni.getStorageSync('userInfo')
  if (localUserInfo) {
    userInfo.value = mapUserInfo(localUserInfo)
  }

  const userId = localUserInfo?.userId || localUserInfo?.id || userStore.userId
  if (!userId) {
    pageDebug.anomaly('缺少用户ID，跳过用户信息请求')
    return
  }

  pageDebug.requestStart('加载用户信息', { userId })
  try {
    const response = await userApi.getUserInfo(userId)
    const data = unwrapResponse(response)

    if (data && Object.keys(data).length > 0) {
      userInfo.value = mapUserInfo(data)
      userStore.setUserInfo(data)
    }

    pageDebug.requestSuccess('加载用户信息', {
      userId: userInfo.value.id,
      name: userInfo.value.name
    })
  } catch (error) {
    pageDebug.requestFail('加载用户信息', error)
    console.error('加载用户信息失败:', error)

    if (error.code === '404' || error.response?.status === 404 || error.message?.includes('用户不存在')) {
      userStore.logout()
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
      uni.removeStorageSync('userId')
      uni.removeStorageSync('role')
      userInfo.value = createDefaultUserInfo()
      return
    }

    if (localUserInfo) {
      userInfo.value = mapUserInfo(localUserInfo)
    }
  }
}

async function loadStats() {
  if (!userStore.isLogin) {
    stats.value = createDefaultStats()
    return
  }

  const userId = userStore.userInfo?.userId || userStore.userInfo?.id || userInfo.value.id
  if (!userId) {
    stats.value = createDefaultStats()
    return
  }

  pageDebug.requestStart('加载统计数据', { userId })

  try {
    const response = await userApi.getUserStats(userId)
    const data = unwrapResponse(response)

    stats.value = {
      orders: toNumber(data.totalOrders ?? data.orders ?? data.ordersCount),
      favorites: toNumber(data.totalFavorites ?? data.favorites ?? data.favoritesCount),
      history: toNumber(data.totalHistory ?? data.history ?? data.historyCount),
      coupons: toNumber(data.availableCoupons ?? data.coupons ?? data.couponsCount)
    }

    pageDebug.requestSuccess('加载统计数据', stats.value)
  } catch (error) {
    pageDebug.requestFail('加载统计数据', error)
    console.error('加载统计数据失败:', error)
    stats.value = createDefaultStats()
  }
}

async function loadOrderCounts() {
  if (!userStore.isLogin) {
    orderCounts.value = createDefaultOrderCounts()
    return
  }

  const userId = userStore.userInfo?.userId || userStore.userInfo?.id || userInfo.value.id
  if (!userId) {
    orderCounts.value = createDefaultOrderCounts()
    return
  }

  pageDebug.requestStart('加载订单数量', { userId })

  try {
    const response = await orderApi.getCount({ userId })
    const data = unwrapResponse(response)

    orderCounts.value = {
      pending: toNumber(data.pending ?? data.unpaid),
      processing: toNumber(data.processing ?? data.preparing ?? data.confirmed),
      delivering: toNumber(data.delivering ?? data.shipping),
      completed: toNumber(data.completed ?? data.pendingReview)
    }

    pageDebug.requestSuccess('加载订单数量', orderCounts.value)
  } catch (error) {
    pageDebug.requestFail('加载订单数量', error)
    console.error('加载订单数量失败:', error)
    orderCounts.value = createDefaultOrderCounts()
  }
}

async function loadWalletData() {
  if (!userStore.isLogin) {
    wallet.value = createDefaultWallet()
    return
  }

  const userId = userStore.userInfo?.userId || userStore.userInfo?.id || userInfo.value.id
  if (!userId) {
    wallet.value = createDefaultWallet()
    return
  }

  pageDebug.requestStart('加载钱包数据', { userId })

  try {
    const response = await walletApi.getWallet(userId)
    const data = unwrapResponse(response)

    wallet.value = {
      balance: formatMoney(data.balance),
      points: toNumber(data.points)
    }

    pageDebug.requestSuccess('加载钱包数据', wallet.value)
  } catch (error) {
    pageDebug.requestFail('加载钱包数据', error)
    console.error('加载钱包数据失败:', error)
    wallet.value = createDefaultWallet()
  }
}

async function loadUnreadCount() {
  if (!userStore.isLogin) {
    unreadCount.value = 0
    return
  }

  pageDebug.requestStart('加载未读消息数')

  try {
    const response = await chatApi.getUnreadCount()
    const data = unwrapResponse(response)
    unreadCount.value = toNumber(data.count ?? response?.count ?? response?.total ?? data)
    pageDebug.requestSuccess('加载未读消息数', { unreadCount: unreadCount.value })
  } catch (error) {
    pageDebug.requestFail('加载未读消息数', error)
    console.error('加载未读消息数失败:', error)
    unreadCount.value = 0
  }
}

async function refreshPageData(showPageLoading = false) {
  if (showPageLoading) {
    loading.value = true
  }

  try {
    await loadUserInfo()

    if (!userStore.isLogin) {
      stats.value = createDefaultStats()
      orderCounts.value = createDefaultOrderCounts()
      wallet.value = createDefaultWallet()
      unreadCount.value = 0
      return
    }

    await Promise.all([
      loadStats(),
      loadOrderCounts(),
      loadWalletData(),
      loadUnreadCount()
    ])
  } finally {
    if (showPageLoading) {
      loading.value = false
    }
  }
}

async function onRefresh() {
  refreshing.value = true
  pageDebug.action('下拉刷新个人中心')
  try {
    await refreshPageData(false)
    pageDebug.requestSuccess('刷新个人中心数据')
  } catch (error) {
    pageDebug.requestFail('刷新个人中心数据', error)
  } finally {
    refreshing.value = false
  }
}

onMounted(async () => {
  pageDebug.lifecycle('页面挂载', { isLogin: userStore.isLogin })
  await refreshPageData(true)
  pageInitialized.value = true
})

onShow(async () => {
  if (!pageInitialized.value) {
    return
  }

  await refreshPageData(false)
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.user-center-container {
  min-height: 100vh;
  background-color: $bg-color-base;
}

.scroll-container {
  height: 100vh;
}

.loading-container {
  @include flex-center;
  padding: 100rpx 0;
}

.not-login-container {
  @include flex-center;
  min-height: 60vh;
  padding: $spacing-xl;
}

.not-login-content {
  @include flex-center-column;
  gap: $spacing-md;
  text-align: center;
  align-items: center;
}

.not-login-text {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.not-login-desc {
  font-size: $font-size-base;
  color: $text-color-secondary;
  line-height: 1.6;
}

.login-btn {
  margin-top: $spacing-lg;
  padding: $spacing-md $spacing-xl;
  background-color: $primary-color;
  color: #FFFFFF;
  border-radius: $border-radius-round;
  font-size: $font-size-base;
  border: none;
}

.user-info-card {
  background: linear-gradient(135deg, #FF6B35 0%, #FF8457 100%);
  margin: $spacing-md;
  padding: $spacing-lg;
  border-radius: $border-radius-lg;
  display: flex;
  align-items: center;
  gap: $spacing-md;
  box-shadow: $box-shadow-md;
}

.user-avatar {
  position: relative;
  width: 120rpx;
  height: 120rpx;
  flex-shrink: 0;
}

.avatar-image {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 255, 255, 0.28);
  background-color: rgba(255, 255, 255, 0.16);
}

.avatar-badge {
  position: absolute;
  bottom: -4rpx;
  right: -4rpx;
  padding: 4rpx 10rpx;
  border-radius: $border-radius-round;
  background: linear-gradient(135deg, #FFD166 0%, #FFB347 100%);
  border: 2rpx solid #FFFFFF;

  .badge-text {
    font-size: $font-size-xs;
    color: #FFFFFF;
    font-weight: $font-weight-bold;
  }
}

.user-detail {
  flex: 1;
  min-width: 0;
}

.user-name-row {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-bottom: $spacing-xs;
}

.user-name {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: #FFFFFF;
  max-width: 320rpx;
  @include text-ellipsis;
}

.gender-badge {
  padding: 4rpx 12rpx;
  border-radius: $border-radius-round;
  background-color: rgba(255, 255, 255, 0.18);
  flex-shrink: 0;
}

.gender-badge-text {
  font-size: $font-size-xs;
  color: #FFFFFF;
}

.meta-pills {
  display: flex;
  gap: $spacing-xs;
  flex-wrap: wrap;
  margin-bottom: $spacing-xs;
}

.meta-pill {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 6rpx 14rpx;
  border-radius: $border-radius-round;
  background-color: rgba(255, 255, 255, 0.18);
}

.meta-pill-text {
  font-size: $font-size-xs;
  color: #FFFFFF;
}

.user-subline {
  display: block;
  font-size: $font-size-sm;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.5;
}

.user-tags {
  display: flex;
  gap: $spacing-xs;
  flex-wrap: wrap;
  margin-top: $spacing-xs;
}

.tag-item {
  padding: 4rpx 12rpx;
  border-radius: $border-radius-round;
  font-size: $font-size-xs;
  color: #FFFFFF;
  background-color: rgba(255, 255, 255, 0.16);
}

.edit-btn {
  width: 88rpx;
  min-height: 88rpx;
  padding: 10rpx 0;
  border-radius: 24rpx;
  background-color: rgba(255, 255, 255, 0.18);
  @include flex-center-column;
  gap: 6rpx;
  flex-shrink: 0;
}

.edit-btn-text {
  font-size: $font-size-xs;
  color: #FFFFFF;
}

.stats-card,
.section-card {
  background-color: $bg-color-white;
  margin: 0 $spacing-md $spacing-md;
  border-radius: $border-radius-lg;
  box-shadow: $box-shadow-sm;
}

.stats-card {
  display: flex;
  padding: $spacing-md 0;
}

.stat-item {
  flex: 1;
  @include flex-center-column;
  gap: $spacing-xs;
  padding: $spacing-sm 0;
}

.stat-value {
  font-size: 40rpx;
  font-weight: $font-weight-bold;
  color: $primary-color;
}

.stat-label {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.section-card {
  padding: $spacing-md;
}

.section-header {
  @include flex-between;
  align-items: center;
  margin-bottom: $spacing-md;
}

.section-title-wrap {
  flex: 1;
  min-width: 0;
}

.section-title {
  display: block;
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.section-subtitle {
  display: block;
  margin-top: 8rpx;
  font-size: $font-size-sm;
  color: $text-color-secondary;
  line-height: 1.5;
}

.section-more {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-left: $spacing-md;
  font-size: $font-size-sm;
  color: $text-color-secondary;
  flex-shrink: 0;
}

.order-status-grid {
  display: flex;
  gap: $spacing-xs;
}

.status-item {
  flex: 1;
  @include flex-center-column;
  gap: $spacing-sm;
  padding: $spacing-sm 0;
}

.status-icon-wrapper {
  position: relative;
  width: 88rpx;
  height: 88rpx;
  border-radius: 24rpx;
  @include flex-center;
}

.status-badge {
  position: absolute;
  top: -10rpx;
  right: -10rpx;
  min-width: 34rpx;
  height: 34rpx;
  padding: 0 8rpx;
  border-radius: $border-radius-round;
  background-color: $danger-color;
  border: 2rpx solid #FFFFFF;
  @include flex-center;
}

.status-badge-text {
  font-size: $font-size-xs;
  color: #FFFFFF;
  font-weight: $font-weight-bold;
}

.status-text {
  font-size: $font-size-sm;
  color: $text-color-regular;
}

.assets-list {
  display: flex;
}

.asset-item {
  flex: 1;
  padding: $spacing-md $spacing-sm;
  @include flex-center-column;
  gap: 8rpx;

  &:not(:last-child) {
    border-right: 1rpx solid $border-color-lighter;
  }
}

.asset-value {
  font-size: 34rpx;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.asset-value.money {
  color: $primary-color;
}

.asset-label {
  font-size: $font-size-sm;
  color: $text-color-primary;
}

.asset-desc {
  font-size: $font-size-xs;
  color: $text-color-secondary;
}

.menu-list {
  .menu-item {
    display: flex;
    align-items: center;
    padding: $spacing-md 0;
    border-bottom: 1rpx solid $border-color-lighter;

    &:last-child {
      border-bottom: none;
    }
  }
}

.menu-icon-wrapper {
  position: relative;
  width: 72rpx;
  height: 72rpx;
  border-radius: 20rpx;
  background-color: #FFF3ED;
  margin-right: $spacing-md;
  @include flex-center;
  flex-shrink: 0;
}

.menu-badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  min-width: 32rpx;
  height: 32rpx;
  padding: 0 8rpx;
  border-radius: $border-radius-round;
  background-color: $danger-color;
  @include flex-center;
}

.menu-badge-text {
  font-size: $font-size-xs;
  color: #FFFFFF;
  font-weight: $font-weight-bold;
}

.menu-content {
  flex: 1;
  min-width: 0;
}

.menu-label {
  display: block;
  font-size: $font-size-base;
  color: $text-color-primary;
}

.menu-desc {
  display: block;
  margin-top: 8rpx;
  font-size: $font-size-sm;
  color: $text-color-secondary;
  line-height: 1.5;
}

.logout-item {
  margin-top: $spacing-xs;
}

.logout-icon {
  background-color: rgba($danger-color, 0.1);
}

.logout-text {
  color: $danger-color;
  font-weight: $font-weight-bold;
}

.bottom-spacer {
  height: 40rpx;
}
</style>
