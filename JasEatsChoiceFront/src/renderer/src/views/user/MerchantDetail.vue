<template>
  <div class="merchant-detail-container">
    <el-card class="merchant-detail-card">
      <!-- 返回按钮 -->
      <div class="back-button-container">
        <common-back-button />
      </div>

      <!-- 使用子组件：商家头部信息 -->
      <merchant-header
        :merchant="merchant"
        :is-favorite="isFavorite"
        @toggle-favorite="toggleFavorite"
        class="scale-in"
      />

      <!-- 菜单类型切换 -->
      <div class="menu-tabs">
        <el-tabs
          class="merchant-menu-tabs"
          :model-value="activeMenuTab"
          @update:model-value="activeMenuTab = $event"
        >
          <el-tab-pane
            v-for="tab in menuTabs"
            :key="tab.value"
            :label="tab.label"
            :name="tab.value"
          >
            <!-- Tab content will be handled by v-if based on activeMenuTab -->
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 菜单展示区 -->
      <div class="menu-display-area fade-in-up delay-100">
        <!-- 加载状态 -->
        <div v-if="isLoading" class="loading-container">
          <el-skeleton :rows="3" animated />
          <el-skeleton :rows="3" animated style="margin-top: 20px;" />
        </div>

        <!-- 空状态：商家没有菜单 -->
        <div v-else-if="!hasMenus && activeMenuTab !== 'comments'" class="empty-state-notice">
          <div class="empty-state-icon">🍽️</div>
          <h3 class="empty-state-title">该商家暂未上架菜单</h3>
          <p class="empty-state-desc">商家正在努力准备中，敬请期待</p>
          <el-button type="primary" @click="router.push('/user/home/merchants')">
            查看其他商家
          </el-button>
        </div>

        <!-- 菜单内容 -->
        <template v-else>
          <!-- 当前菜单名称 (仅在非用户评价标签时显示) -->
          <div v-if="activeMenuTab !== 'comments'" class="current-menu-name">
            <h2 class="menu-name-title">{{ currentMenuName }}</h2>

            <!-- 菜品搜索和筛选 -->
            <div class="dish-search-filter">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索菜品名称"
                :prefix-icon="Search"
                clearable
                class="search-input"
                size="default"
              />
              <el-select
                v-model="sortBy"
                placeholder="排序方式"
                size="default"
                class="sort-select"
              >
                <el-option label="默认排序" value="default" />
                <el-option label="价格从低到高" value="priceAsc" />
                <el-option label="价格从高到低" value="priceDesc" />
              </el-select>
            </div>

            <!-- 分类快速导航 -->
            <div v-if="currentMenuCategories.length > 0" class="category-nav">
              <el-tag
                v-for="category in currentMenuCategories"
                :key="category"
                class="category-nav-tag"
                @click="scrollToCategory(category)"
              >
                {{ getCategoryEmoji(category) }} {{ category }}
              </el-tag>
            </div>
          </div>

          <!-- 动态渲染所有菜品分类 -->
          <div
            v-for="category in currentMenuCategories"
            :key="category"
            :id="`category-${category}`"
            class="dish-category-section"
          >
            <h3 class="category-title">{{ getCategoryEmoji(category) }} {{ category }}</h3>
            <div class="dish-grid">
              <dish-card
                v-for="item in filteredAndSortedDishes(category)"
                :key="item.id"
                :dish="item"
                :category-emoji="getCategoryEmoji(category)"
                :view-mode="viewMode"
                @add-to-cart="addMenuItem"
                class="stagger-item"
              />
            </div>
            <!-- 空状态：该分类下没有搜索结果 -->
            <div v-if="filteredAndSortedDishes(category).length === 0" class="category-empty">
              <p>该分类下暂无匹配的菜品</p>
            </div>
          </div>

          <!-- 用户评价 -->
          <div v-if="activeMenuTab === 'comments'">
            <!-- 商家没有菜单的提示 -->
            <div v-if="!hasMenus" class="no-menus-notice">
              <div class="notice-icon">📋</div>
              <p class="notice-text">当前商家还没有上架菜单</p>
            </div>

            <!-- 使用子组件：评价区域 -->
            <comments-section :comments="comments" :merchant-rating="merchant.rating" />
          </div>
        </template>
      </div>

      <!-- 立即下单快捷操作区（仅在order模式下显示） -->
      <div v-if="viewMode === 'order' && hasMenus" class="quick-order-section">
        <el-button
          type="primary"
          size="large"
          class="quick-order-button"
          @click="goToOrderConfirmation"
        >
          进入订单确认页
        </el-button>
      </div>

      <!-- 可拖动悬浮购物车 -->
      <div ref="cartBallRef" class="draggable-cart-ball" @pointerdown="startDrag" @click="viewCart">
        <div class="cart-icon-wrapper">
          <el-icon class="cart-icon" :size="28"><ShoppingCart /></el-icon>
          <el-badge :value="cartTotalQuantity" class="cart-badge" />
        </div>
        <div class="cart-amount">¥{{ cartTotalAmount.toFixed(2) }}</div>
      </div>

      <!-- 返回顶部按钮 -->
      <transition name="fade">
        <div v-show="showBackToTop" class="back-to-top" @click="scrollToTop">
          <el-icon :size="20"><CaretTop /></el-icon>
        </div>
      </transition>
    </el-card>

    <!-- 使用子组件：购物车弹窗 -->
    <shopping-cart-dialog
      v-model="cartVisible"
      :cart-items="cartItems"
      @update-cart="handleUpdateCart"
      @submit-order="submitOrder"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart, Search, CaretTop } from '@element-plus/icons-vue'
import axios from 'axios'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import { useAuthStore } from '../../store/authStore'
import { useUserStore } from '../../store/userStore'

// 引入子组件
import MerchantHeader from './components/MerchantHeader.vue'
import DishCard from './components/DishCard.vue'
import CommentsSection from './components/CommentsSection.vue'
import ShoppingCartDialog from './components/ShoppingCart.vue'

// 引入API配置
import { API_CONFIG } from '../../config/index.js'

const router = useRouter()
const route = useRoute()

// 获取 Pinia 存储
const authStore = useAuthStore()
const userStore = useUserStore()

// 商家信息
const merchant = ref({
  id: 0,
  name: '', // 后端字段名是 name，不是 nickname
  type: '',
  rating: 4.5, // Default to 4.5 for mock data
  distance: '',
  status: '',
  tags: [],
  image: ''
})

// 收藏状态
const isFavorite = ref(false)

// 当前视图模式: details(查看详情) / order(立即下单)
const viewMode = ref(route.query.viewMode || 'order') // 默认值改为order以显示立即下单按钮

// 提交订单并导航到订单确认页
const submitOrder = () => {
  if (cartItems.value.length === 0) {
    ElMessage.warning('请先添加商品到购物车')
    return
  }

  // 将订单信息存储到会话存储
  const orderInfo = {
    merchant: merchant.value,
    cartItems: cartItems.value,
    totalAmount: cartItems.value.reduce((total, item) => total + item.totalPrice, 0)
  }
  sessionStorage.setItem('pendingOrder', JSON.stringify(orderInfo))

  // 关闭购物车
  closeCart()

  // 导航到订单确认页面
  router.push({ path: '/user/home/order-confirmation' })
}
// 菜单类型标签
const activeMenuTab = ref('comments') // 默认显示用户评价

// 菜单类型数据
const menuTabs = ref([{ value: 'comments', label: '用户评价' }])

// 标记商家是否有菜单
const hasMenus = ref(false)

// 加载状态
const isLoading = ref(false)

// 菜品搜索关键词
const searchKeyword = ref('')

// 菜品排序方式
const sortBy = ref('default')

// 返回顶部按钮显示状态
const showBackToTop = ref(false)

// 计算当前选中的菜单名称
const currentMenuName = computed(() => {
  const activeTab = menuTabs.value.find((tab) => tab.value === activeMenuTab.value)
  return activeTab ? activeTab.label : ''
})

// 动态获取当前菜单中的所有菜品分类
const currentMenuCategories = computed(() => {
  if (activeMenuTab.value === 'comments') {
    return []
  }

  // 获取当前菜单的所有菜品
  const currentMenuItems = menuItems.value.filter((item) => item.menuId === activeMenuTab.value)

  // 提取所有唯一的分类
  const categories = [...new Set(currentMenuItems.map((item) => item.category))].filter(
    (category) => category && category.trim() !== ''
  )

  return categories
})

// 过滤和排序指定分类的菜品
const filteredAndSortedDishes = (category) => {
  // 第一步：筛选出当前分类和当前菜单的菜品
  let dishes = menuItems.value.filter(
    (item) => item.menuId === activeMenuTab.value && item.category === category
  )

  // 第二步：根据搜索关键词过滤
  if (searchKeyword.value && searchKeyword.value.trim() !== '') {
    const keyword = searchKeyword.value.toLowerCase().trim()
    dishes = dishes.filter((item) =>
      item.name && item.name.toLowerCase().includes(keyword)
    )
  }

  // 第三步：根据选择的方式排序
  if (sortBy.value !== 'default') {
    dishes = [...dishes].sort((a, b) => {
      if (sortBy.value === 'priceAsc') {
        return (a.price || 0) - (b.price || 0)
      } else if (sortBy.value === 'priceDesc') {
        return (b.price || 0) - (a.price || 0)
      }
      return 0
    })
  }

  return dishes
}

// 评价数据
const comments = ref([])

// 菜单数据
const menuItems = ref([])

// 组件挂载时加载商家信息和恢复购物车
onMounted(() => {
  const savedMerchant = sessionStorage.getItem('selectedMerchant')
  console.log('🔍 onMounted - 从 sessionStorage 读取的原始数据:', savedMerchant)

  if (savedMerchant) {
    // 从会话存储获取商家基本信息
    const baseMerchantInfo = JSON.parse(savedMerchant)
    console.log('🔍 onMounted - 解析后的商家对象:', baseMerchantInfo)
    console.log('🔍 onMounted - 商家对象的所有字段:', Object.keys(baseMerchantInfo))
    console.log('🔍 onMounted - 商家ID:', baseMerchantInfo.id, '| userId:', baseMerchantInfo.userId, '| merchantId:', baseMerchantInfo.merchantId)

    merchant.value = { ...baseMerchantInfo }

    // 从后端获取完整的商家详情和菜品信息
    // 注意：商家对象使用 merchantId 字段，不是 id
    loadMerchantDetails(baseMerchantInfo.merchantId)

    // 加载当前商家的独立购物车
    if (!cartItemsByMerchant.value[merchant.value.merchantId]) {
      cartItemsByMerchant.value[merchant.value.merchantId] = []
    }
    cartItems.value = cartItemsByMerchant.value[merchant.value.merchantId]

    // 检查商家是否已被收藏
    checkFavoriteStatus()
  } else {
    // 如果没有商家信息，返回商家列表
    router.push('/user/home/merchants')
    return
  }

  // 恢复购物车数据（当从订单确认页返回且未完成支付时）
  const pendingOrder = sessionStorage.getItem('pendingOrder')
  if (pendingOrder) {
    const parsedOrder = JSON.parse(pendingOrder)
    if (
      parsedOrder.cartItems &&
      parsedOrder.cartItems.length > 0 &&
      parsedOrder.merchant.merchantId === merchant.value.merchantId
    ) {
      // 清空当前购物车
      cartItemsByMerchant.value[merchant.value.merchantId] = []
      // 恢复购物车项目
      parsedOrder.cartItems.forEach((item) => {
        // 确保购物车项目有必要的属性
        const cartItem = {
          ...item,
          note: item.note || '',
          tempNote: item.tempNote || '',
          isEditingNote: item.isEditingNote || false
        }
        cartItemsByMerchant.value[merchant.value.merchantId].push(cartItem)
      })
      // 更新当前购物车引用
      cartItems.value = cartItemsByMerchant.value[merchant.value.merchantId]
      // 更新购物车统计信息
      updateCartStats()
    }
  }

  // 处理"再来一单"功能
  // 处理单个商品添加（替换推荐菜品）
  const addToCart = route.query.addToCart
  if (addToCart) {
    try {
      const itemToAdd = JSON.parse(addToCart)
      // 添加到购物车
      updateCart({
        id: itemToAdd.dishId,
        name: itemToAdd.dishName,
        price: itemToAdd.price,
        totalPrice: itemToAdd.price * itemToAdd.quantity,
        quantity: itemToAdd.quantity,
        selectedOptionalIngredients: [],
        note: itemToAdd.customization || ''
      })
      ElMessage.success(`已添加"${itemToAdd.dishName}"到购物车`)

      // 清除query参数，避免重复添加
      router.replace({ query: {} })
    } catch (error) {
      console.error('解析addToCart参数失败:', error)
    }
  }

  // 处理多个商品添加（再来一单确认）
  const reorderItems = route.query.reorderItems
  if (reorderItems) {
    try {
      const itemsToAdd = JSON.parse(reorderItems)

      // 清空当前购物车
      cartItemsByMerchant.value[merchant.value.merchantId] = []

      // 添加所有选中的菜品
      itemsToAdd.forEach(item => {
        updateCart({
          id: item.dishId,
          name: item.dishName,
          price: item.price,
          totalPrice: item.price * item.quantity,
          quantity: item.quantity,
          selectedOptionalIngredients: [],
          note: item.customization || ''
        })
      })

      ElMessage.success(`已添加${itemsToAdd.length}个菜品到购物车`)

      // 清除query参数
      router.replace({ query: {} })
    } catch (error) {
      console.error('解析reorderItems参数失败:', error)
    }
  }
})

// 滚动监听
const handleScroll = () => {
  showBackToTop.value = window.scrollY > 300
}

// 滚动到顶部
const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 组件挂载后添加滚动监听
onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

// 组件卸载前移除滚动监听
onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll)
})

// 从后端加载完整的商家详情和菜品信息
const loadMerchantDetails = async (merchantId) => {
  isLoading.value = true
  try {
    // 1. 先获取商家详情
    const merchantResponse = await axios.get(
      API_CONFIG.baseURL + API_CONFIG.merchant.detail + merchantId
    )
    console.log('获取商家详情 response:', merchantResponse.data)
    console.log('获取商家详情 data 字段:', merchantResponse.data?.data)
    if (merchantResponse.data?.data) {
      console.log('商家详情对象的字段:', Object.keys(merchantResponse.data.data))
      console.log('merchantId:', merchantResponse.data.data.merchantId)
    }

    if (merchantResponse.data?.code === '200' && merchantResponse.data?.data) {
      // 更新商家信息
      merchant.value = {
        ...merchant.value,
        ...merchantResponse.data.data
      }
      console.log('更新后的 merchant.value 有 merchantId:', merchant.value.merchantId)
    }

    // 2. 再获取商家的菜单数据
    const menuUrl = `${API_CONFIG.baseURL}/v1/menus/merchants/${merchantId}/menu`
    console.log('🔍 准备获取商家菜单，URL:', menuUrl)
    console.log('🔍 商家ID:', merchantId, '类型:', typeof merchantId)
    console.log('🔍 完整的API配置:', API_CONFIG)

    const menuResponse = await axios.get(menuUrl)
    console.log('📥 获取商家菜单 response status:', menuResponse.status)
    console.log('📥 获取商家菜单 response data:', menuResponse.data)
    console.log('📥 response.data 结构:', {
      code: menuResponse.data?.code,
      data: menuResponse.data?.data,
      dataType: typeof menuResponse.data?.data,
      dataLength: menuResponse.data?.data?.length,
      isArray: Array.isArray(menuResponse.data?.data)
    })

    if (
      menuResponse.data?.code === '200' &&
      menuResponse.data?.data &&
      menuResponse.data.data.length > 0
    ) {
      console.log('✅ 菜单数据存在，菜单数量:', menuResponse.data.data.length)
      console.log('✅ 第一个菜单的完整数据:', JSON.stringify(menuResponse.data.data[0], null, 2))

      // 为菜单项目添加必要的属性
      const allMenuItems = []

      // 遍历所有菜单
      menuResponse.data.data.forEach((menu, index) => {
        console.log(`📋 [${index}] 处理菜单:`, {
          menuName: menu.menuName,
          id: menu.id,
          dishesCount: menu.dishes?.length || 0,
          dishes: menu.dishes
        })
        if (menu.dishes && menu.dishes.length > 0) {
          menu.dishes.forEach((dish, dishIndex) => {
            console.log(`  🍲 [${dishIndex}] 菜品:`, {
              name: dish.name,
              category: dish.category,
              id: dish.id,
              price: dish.price,
              description: dish.description,
              image: dish.image,
              requiredIngredients: dish.requiredIngredients,
              optionalIngredients: dish.optionalIngredients
            })
            allMenuItems.push({
              ...dish,
              menuId: menu.id, // 保存菜单ID (后端使用id字段)
              menuName: menu.menuName, // 保存菜单名称
              quantity: 1, // 默认数量为1
              optionalIngredients: dish.optionalIngredients || [], // 确保可选食材数组存在
              selectedOptionalIngredients: [], // 初始化选中的可选食材
              note: '', // 添加备注字段
              tempNote: '', // 添加临时备注字段
              isEditingNote: false // 添加编辑状态字段
            })
          })
        } else {
          console.log(`  ⚠️ 菜单 [${index}] "${menu.menuName}" 没有菜品`)
        }
      })

      menuItems.value = allMenuItems
      console.log('📦 最终 menuItems 数量:', menuItems.value.length)
      console.log(
        '📦 menuItems 详情:',
        menuItems.value.map((item) => ({
          name: item.name,
          menuId: item.menuId,
          category: item.category,
          price: item.price,
          description: item.description,
          image: item.image,
          requiredIngredients: item.requiredIngredients,
          optionalIngredients: item.optionalIngredients
        }))
      )
      console.log('📦 第一个菜品的完整数据:', menuItems.value[0])

      // 确保可选食材有selected属性，并处理可能的字符串格式
      menuItems.value.forEach((item) => {
        if (item.optionalIngredients && item.optionalIngredients.length > 0) {
          item.optionalIngredients = item.optionalIngredients.map((ingredient) => {
            // 如果是字符串，转换为对象格式
            if (typeof ingredient === 'string') {
              return {
                name: ingredient,
                price: 0,
                selected: false
              }
            }
            // 如果已经是对象，确保有selected属性
            return {
              ...ingredient,
              selected: ingredient.selected || false
            }
          })
        }
      })

      // 根据后端返回的菜单生成标签
      menuTabs.value = menuResponse.data.data.map((menu) => ({
        value: menu.id,
        label: menu.menuName
      }))
      console.log('🏷️ 生成的标签页:', menuTabs.value)

      // 添加用户评价标签
      menuTabs.value.push({ value: 'comments', label: '用户评价' })

      // 默认激活第一个菜单
      activeMenuTab.value = menuResponse.data.data[0].id
      console.log(
        '🎯 默认激活的标签页 (activeMenuTab):',
        activeMenuTab.value,
        '类型:',
        typeof activeMenuTab.value
      )

      hasMenus.value = true
    } else {
      // 商家没有菜单
      console.log('⚠️ 商家没有菜单数据')
      console.log('  ⚠️ menuResponse.data?.code:', menuResponse.data?.code)
      console.log('  ⚠️ menuResponse.data?.data:', menuResponse.data?.data)
      console.log('  ⚠️ menuResponse.data.data?.length:', menuResponse.data?.data?.length)

      menuItems.value = []
      menuTabs.value = [{ value: 'comments', label: '用户评价' }]
      activeMenuTab.value = 'comments'
      hasMenus.value = false
    }
    isLoading.value = false
  } catch (error) {
    console.error('❌ 加载商家详情和菜单失败')
    console.error('  ❌ 错误对象:', error)
    console.error('  ❌ 错误消息:', error.message)
    console.error('  ❌ 错误响应:', error.response)
    console.error('  ❌ 错误请求:', error.request)
    console.error('  ❌ 错误堆栈:', error.stack)
    ElMessage.error('加载商家详情失败，请稍后重试')
    hasMenus.value = false
    isLoading.value = false
  }
}

// 检查商家是否已被收藏
const checkFavoriteStatus = async () => {
  try {
    // 从 authStore 或 userStore 获取用户ID
    // 注意：userStore.userInfo 中的字段名是 userId，不是 id
    const userId = userStore.userInfo?.userId || authStore.userId
    if (!userId) {
      console.log('用户未登录，跳过收藏状态检查')
      return
    }

    const response = await axios.get(API_CONFIG.baseURL + API_CONFIG.collection.check, {
      params: {
        userId: userId,
        type: 'merchant', // 商家类型
        id: merchant.value.merchantId
      }
    })

    // 后端返回格式: { success: true, code: "200", message: "成功", data: true/false }
    if (response.data && response.data.success && response.data.code === '200') {
      isFavorite.value = response.data.data === true
    }
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
}

// 切换收藏状态
const toggleFavorite = async () => {
  try {
    // 从 authStore 或 userStore 获取用户ID
    // 注意：userStore.userInfo 中的字段名是 userId，不是 id
    const userId = userStore.userInfo?.userId || authStore.userId
    if (!userId) {
      ElMessage.warning('请先登录')
      return
    }

    if (isFavorite.value) {
      // 取消收藏
      const response = await axios.delete(API_CONFIG.baseURL + API_CONFIG.collection.remove, {
        params: {
          userId: userId,
          type: 'merchant',
          id: String(merchant.value.merchantId)
        }
      })

      // 后端返回格式: { success: true, code: "200", message: "成功", data: null }
      if (response.data && response.data.success && response.data.code === '200') {
        isFavorite.value = false
        ElMessage.success(`${merchant.value.name} 已取消收藏`)
      } else {
        ElMessage.error(response.data?.message || '取消收藏失败')
      }
    } else {
      // 添加收藏
      const collectionData = {
        userId: userId,
        collectableType: 'merchant',
        collectableId: String(merchant.value.merchantId)
      }

      const response = await axios.post(
        API_CONFIG.baseURL + API_CONFIG.collection.add,
        collectionData
      )

      // 后端返回格式: { success: true, code: "200", message: "成功", data: 14(收藏ID) }
      if (response.data && response.data.success && response.data.code === '200') {
        isFavorite.value = true
        ElMessage.success(`${merchant.value.name} 已加入收藏`)
      } else {
        ElMessage.error(response.data?.message || '收藏失败')
      }
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
    // 恢复原状态
    isFavorite.value = !isFavorite.value
  }
}

// 购物车数据 - 每个商家有独立的购物车
const cartItemsByMerchant = ref({})

// 当前商家的购物车数据
const cartItems = ref([])

// 购物车显示状态
const cartVisible = ref(false)

// 计算购物车总数量（当前商家购物车所有商品数量之和）
const cartTotalQuantity = ref(0)

// 计算购物车总金额（当前商家购物车总金额）
const cartTotalAmount = ref(0)

// 可拖动购物车相关
const cartBallRef = ref(null)
let isDragging = false
let hasDragged = false // 标记是否有实际拖动
let justDragged = false // 标记刚刚结束拖动
let startX = 0
let startY = 0
let initialX = 0
let initialY = 0

// 开始拖动
const startDrag = (e) => {
  if (!cartBallRef.value) return

  // 阻止文本选择和默认事件
  e.preventDefault()
  e.stopPropagation()

  isDragging = true
  startX = e.clientX
  startY = e.clientY

  // 获取购物车球的初始位置
  const rect = cartBallRef.value.getBoundingClientRect()
  initialX = rect.left
  initialY = rect.top

  // 拖动时移除过渡动画，消除阻尼感
  cartBallRef.value.style.transition = 'none'

  // 添加事件监听（使用 pointer 事件以支持触摸屏）
  document.addEventListener('pointermove', onDrag, { passive: false })
  document.addEventListener('pointerup', stopDrag)
}

// 拖动中
const onDrag = (e) => {
  if (!isDragging || !cartBallRef.value) return

  hasDragged = true // 设置为已拖动

  const dx = e.clientX - startX
  const dy = e.clientY - startY

  // 计算新位置
  let newX = initialX + dx
  let newY = initialY + dy

  // 限制在视窗内
  const windowWidth = window.innerWidth
  const windowHeight = window.innerHeight
  const cartWidth = cartBallRef.value.offsetWidth
  const cartHeight = cartBallRef.value.offsetHeight

  newX = Math.max(0, Math.min(newX, windowWidth - cartWidth))
  newY = Math.max(0, Math.min(newY, windowHeight - cartHeight))

  // 使用 transform 替代 left/top，性能更好
  cartBallRef.value.style.transform = `translate(${newX - initialX}px, ${newY - initialY}px) scale(1.08)`
  cartBallRef.value.style.left = `${initialX}px`
  cartBallRef.value.style.top = `${initialY}px`
}

// 停止拖动
const stopDrag = () => {
  if (!cartBallRef.value) return

  // 重置拖动状态
  const wasDragging = hasDragged
  isDragging = false
  hasDragged = false

  document.removeEventListener('pointermove', onDrag)
  document.removeEventListener('pointerup', stopDrag)

  // 如果有拖动，保存最终位置并恢复过渡效果
  if (wasDragging) {
    // 获取当前位置
    const rect = cartBallRef.value.getBoundingClientRect()

    // 保存最终位置
    cartBallRef.value.style.left = `${rect.left}px`
    cartBallRef.value.style.top = `${rect.top}px`
    cartBallRef.value.style.transform = 'scale(1)'

    // 恢复过渡效果
    requestAnimationFrame(() => {
      if (cartBallRef.value) {
        cartBallRef.value.style.transition = ''
      }
    })

    justDragged = true
    // 设置一个短暂的延迟来重置标记，确保click事件能检测到
    setTimeout(() => {
      justDragged = false
    }, 100)
  } else {
    // 如果没有拖动，恢复过渡效果
    cartBallRef.value.style.transition = ''
    cartBallRef.value.style.transform = ''
  }
}

// 更新购物车统计信息 - 使用当前商家的购物车
const updateCartStats = () => {
  if (!merchant.value || !merchant.value.merchantId) return

  // 确保当前购物车引用正确
  cartItems.value = cartItemsByMerchant.value[merchant.value.merchantId]

  cartTotalQuantity.value = cartItems.value.reduce((total, item) => total + item.quantity, 0)
  cartTotalAmount.value = cartItems.value.reduce((total, item) => total + item.totalPrice, 0)
}

// 更新购物车 - 使用当前商家的购物车
const updateCart = (item) => {
  if (!merchant.value || !merchant.value.merchantId) return

  // 获取当前商家的购物车
  const currentMerchantCart = cartItemsByMerchant.value[merchant.value.merchantId]

  // 检查是否有相同的商品和相同的可选食材组合
  const existingItem = currentMerchantCart.find(
    (cartItem) =>
      cartItem.id === item.id &&
      JSON.stringify(cartItem.selectedOptionalIngredients) ===
        JSON.stringify(item.selectedOptionalIngredients)
  )

  if (existingItem) {
    // 如果存在相同的组合，增加数量
    existingItem.quantity += item.quantity
    existingItem.totalPrice += item.totalPrice
  } else {
    // 如果不存在，添加新的购物车项目
    currentMerchantCart.push({ ...item })
  }

  // 更新购物车统计信息
  updateCartStats()
}

// 计算实时价格函数
const calculateRealTimePrice = (item) => {
  if (!item) {
    console.log('calculateRealTimePrice: item is null/undefined')
    return 0
  }
  const optionalTotal = item.optionalIngredients.reduce((sum, ingredient) => {
    return sum + (ingredient.selected ? ingredient.price : 0)
  }, 0)
  const result = item.price + optionalTotal
  console.log(
    `💰 计算价格 - 菜品: ${item.name}, basePrice: ${item.price}, optionalTotal: ${optionalTotal}, finalPrice: ${result}`
  )
  return result
}

// 根据菜品分类返回对应的 emoji 图标
const getCategoryEmoji = (category) => {
  const emojiMap = {
    招牌菜: '🔥',
    主食: '🍚',
    饮品: '🥤',
    小吃: '🍢',
    甜点: '🍰',
    汤: '🍲',
    凉菜: '🥗',
    热菜: '🍛'
  }
  return emojiMap[category] || '🍽️'
}

// 滚动到指定分类
const scrollToCategory = (category) => {
  const element = document.getElementById(`category-${category}`)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

// 添加菜单项到购物车
const addMenuItem = (item) => {
  // 计算选中的可选食材
  const selectedOptionalIngredients = item.optionalIngredients.filter(
    (ingredient) => ingredient.selected
  )
  const totalPrice =
    item.price + selectedOptionalIngredients.reduce((sum, ingredient) => sum + ingredient.price, 0)

  // 创建购物车项目
  const cartItem = {
    ...item,
    quantity: item.quantity,
    selectedOptionalIngredients: [...selectedOptionalIngredients],
    totalPrice: totalPrice * item.quantity,
    note: '', // Add note property
    tempNote: '', // Add temporary note property for input
    isEditingNote: false // Add editing state
  }

  updateCart(cartItem)
  ElMessage.success(`${item.name} 已加入购物车`)

  // 清空配置：重置数量为1，取消选中所有可选食材
  item.quantity = 1
  item.optionalIngredients.forEach((ingredient) => {
    ingredient.selected = false
  })

  // 这里可以添加真实的购物车逻辑，比如保存到数据库或本地存储
  console.log('加入购物车:', cartItem)
}

// 查看购物车
const viewCart = () => {
  // 如果正在拖动、已经拖动或刚刚结束拖动，不打开购物车
  if (isDragging || hasDragged || justDragged) {
    return
  }
  cartVisible.value = true
}

// 关闭购物车
const closeCart = () => {
  cartVisible.value = false
}

// 处理购物车更新
const handleUpdateCart = ({ action, index }) => {
  if (action === 'remove') {
    // 移除商品
    cartItems.value.splice(index, 1)
  } else if (action === 'update') {
    // 更新商品
    // 商品已经在 ShoppingCart 组件中更新
  }
  updateCartStats()
}

// 跳转到订单确认页
const goToOrderConfirmation = () => {
  // 将订单信息存储到会话存储
  const orderInfo = {
    merchant: merchant.value,
    cartItems: cartItems.value,
    totalAmount: cartItems.value.reduce((total, item) => total + item.totalPrice, 0),
    // 单聊/店铺直接下单时，设置默认值
    fromChat: false,
    groupName: '默认订单群',
    // 这里可以替换为实际的用户名，假设从用户信息中获取
    userName: '当前用户' // 示例值，实际应从登录信息中获取
  }
  sessionStorage.setItem('pendingOrder', JSON.stringify(orderInfo))

  router.push('/user/home/order-confirmation')
}

// 监听标签页切换，输出过滤结果
watch(activeMenuTab, (newTab, oldTab) => {
  console.log('🔄 标签页切换')
  console.log('  旧标签:', oldTab, '类型:', typeof oldTab)
  console.log('  新标签:', newTab, '类型:', typeof newTab)

  // 测试招牌菜过滤
  const signatureDishes = menuItems.value.filter(
    (item) => item.menuId === newTab && item.category === '招牌菜'
  )
  console.log('  🔥 招牌菜过滤结果数量:', signatureDishes.length)
  if (signatureDishes.length > 0) {
    console.log(
      '  🔥 招牌菜详情:',
      signatureDishes.map((d) => ({
        name: d.name,
        menuId: d.menuId,
        category: d.category
      }))
    )
  }

  // 测试主食过滤
  const stapleDishes = menuItems.value.filter(
    (item) => item.menuId === newTab && item.category === '主食'
  )
  console.log('  🍚 主食过滤结果数量:', stapleDishes.length)
  if (stapleDishes.length > 0) {
    console.log(
      '  🍚 主食详情:',
      stapleDishes.map((d) => ({
        name: d.name,
        menuId: d.menuId,
        category: d.category
      }))
    )
  }

  // 测试饮品过滤
  const drinkDishes = menuItems.value.filter(
    (item) => item.menuId === newTab && item.category === '饮品'
  )
  console.log('  🥤 饮品过滤结果数量:', drinkDishes.length)

  // 测试所有菜品的 menuId 匹配
  const allMatchingItems = menuItems.value.filter((item) => item.menuId === newTab)
  console.log('  📋 所有匹配当前标签的菜品数量:', allMatchingItems.length)
  if (allMatchingItems.length === 0) {
    console.log('  ⚠️ 没有找到匹配的菜品！')
    console.log(
      '  📦 所有 menuItems 的 menuId:',
      menuItems.value.map((item) => ({
        name: item.name,
        menuId: item.menuId,
        menuIdType: typeof item.menuId
      }))
    )
  }
})

// 监听滚动事件的代码已合并到上面的onMounted钩子中
</script>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';

.merchant-detail-container {
  padding: 0;
  min-height: 100vh;
  background-color: @nordic-bg;

  .merchant-detail-card {
    border-radius: 0;
    border: none;
    box-shadow: none;
    padding: 0;

    // 返回按钮
    .back-button-container {
      padding: @nordic-space-md @nordic-space-lg;
      background-color: @nordic-surface;

      .back-button {
        font-size: @nordic-text-lg;
        color: @nordic-blue;
        padding: @nordic-space-sm @nordic-space-md;
        border-radius: @nordic-radius-sm;
        transition: all @nordic-transition-slow ease;

        &:hover {
          color: @nordic-blue;
          background-color: @nordic-blue-light;
        }
      }
    }

    // 菜单类型切换
    .menu-tabs {
      padding: 0 @nordic-space-lg;
      background-color: @nordic-surface;
      border-bottom: 1px solid @nordic-border;

      .merchant-menu-tabs {
        .el-tabs__nav {
          border-bottom: none;
        }

        .el-tabs__item {
          font-size: @nordic-text-base;
          color: @nordic-text-secondary;
          padding: @nordic-space-md 0;
          transition: all @nordic-transition-slow ease;

          &.is-active {
            color: @nordic-red;
            border-bottom: 2px solid @nordic-red;
            font-weight: 500;
          }
        }
      }
    }

    // 菜单展示区
    .menu-display-area {
      padding: @nordic-space-lg;
      background-color: @nordic-surface;
      min-height: 400px;

      // 加载状态
      .loading-container {
        padding: 40px @nordic-space-lg;
      }

      // 空状态
      .empty-state-notice {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 60px @nordic-space-lg;
        text-align: center;
        min-height: 400px;

        .empty-state-icon {
          font-size: 64px;
          margin-bottom: @nordic-space-md;
          opacity: 0.8;
        }

        .empty-state-title {
          font-size: @nordic-text-lg;
          font-weight: 600;
          color: @nordic-text;
          margin: 0 0 @nordic-space-sm 0;
        }

        .empty-state-desc {
          font-size: @nordic-text-base;
          color: @nordic-text-secondary;
          margin: 0 0 @nordic-space-lg 0;
        }
      }

      // 当前菜单名称
      .current-menu-name {
        margin-bottom: @nordic-space-lg;

        .menu-name-title {
          font-size: @nordic-text-xl;
          font-weight: bold;
          color: @nordic-text;
          padding-bottom: @nordic-space-md;
          border-bottom: 2px solid @nordic-border;
          margin-bottom: @nordic-space-md;
        }

        // 菜品搜索和筛选
        .dish-search-filter {
          display: flex;
          gap: @nordic-space-md;
          margin-bottom: @nordic-space-md;
          flex-wrap: wrap;

          .search-input {
            flex: 1;
            min-width: 200px;
          }

          .sort-select {
            width: 140px;
          }
        }

        // 分类导航
        .category-nav {
          display: flex;
          flex-wrap: wrap;
          gap: @nordic-space-sm;
          position: sticky;
          top: 0;
          z-index: 10;
          background: fade(@nordic-surface, 95%);
          backdrop-filter: blur(10px);
          padding: @nordic-space-md 0;
          border-radius: @nordic-radius-md;
          box-shadow: 0 2px 8px @nordic-shadow;
          margin-top: @nordic-space-md;

          .category-nav-tag {
            cursor: pointer;
            transition: all @nordic-transition-slow ease;
            font-size: @nordic-text-base;
            padding: @nordic-space-sm @nordic-space-md;

            &:hover {
              transform: translateY(-2px);
              box-shadow: 0 4px 12px fade(@nordic-accent, 20%);
            }
          }
        }
      }

      // 分类空状态
      .category-empty {
        padding: @nordic-space-xl;
        text-align: center;
        color: @nordic-text-muted;
        font-size: @nordic-text-base;
        background-color: @nordic-bg;
        border-radius: @nordic-radius-md;
        margin-top: @nordic-space-md;
      }

      // 菜品分类
      .dish-category-section {
        margin-bottom: @nordic-space-xl;

        .category-title {
          font-size: @nordic-text-md;
          font-weight: bold;
          color: @nordic-text;
          margin-bottom: @nordic-space-md;
        }

        // 菜品网格布局
        .dish-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
          gap: @nordic-space-lg;
        }
      }

      // 没有菜单的提示
      .no-menus-notice {
        margin: @nordic-space-lg 0;
        padding: @nordic-space-xl;
        background: @nordic-yellow-light;
        border-radius: @nordic-radius-lg;
        text-align: center;
        border: 1px solid fade(@nordic-yellow, 30%);

        .notice-icon {
          font-size: 3.429rem;
          margin-bottom: @nordic-space-md;
        }

        .notice-text {
          color: @nordic-yellow-dark;
          font-size: @nordic-text-md;
          font-weight: 500;
          margin: 0;
        }
      }
    }

    // 立即下单快捷操作区
    .quick-order-section {
      padding: @nordic-space-lg;
      background-color: @nordic-surface;
      border-top: 1px solid fade(@nordic-accent, 10%);

      .quick-order-button {
        width: 100%;
        height: 52px;
        font-size: @nordic-text-md;
        background: linear-gradient(135deg, @nordic-accent 0%, @nordic-accent-dark 100%);
        border: none;
        border-radius: @nordic-radius-lg;
        font-weight: 600;
        transition: all @nordic-transition-slow cubic-bezier(0.4, 0, 0.2, 1);
        box-shadow: 0 4px 16px fade(@nordic-accent, 30%);

        &:hover {
          background: linear-gradient(135deg, @nordic-accent-dark 0%, darken(@nordic-accent-dark, 8%) 100%);
          transform: translateY(-2px);
          box-shadow: 0 6px 24px fade(@nordic-accent, 40%);
        }

        &:active {
          transform: translateY(0);
        }
      }
    }
  }
}

// 可拖动悬浮购物车
.draggable-cart-ball {
  position: fixed;
  right: @nordic-space-lg;
  bottom: 100px;
  width: 88px;
  height: 88px;
  background: linear-gradient(135deg, @nordic-accent 0%, @nordic-accent-dark 100%);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: grab;
  box-shadow: 0 8px 32px fade(@nordic-accent, 50%);
  color: @nordic-white;
  transition:
    box-shadow @nordic-transition-slow ease,
    transform @nordic-transition-fast ease;
  z-index: 9999;
  border: 3px solid fade(@nordic-white, 30%);
  backdrop-filter: blur(10px);
  user-select: none;
  touch-action: none;
  will-change: transform, left, top;
  padding: @nordic-space-sm;
  box-sizing: border-box;

  &:active {
    cursor: grabbing;
  }

  &:hover:not(:active) {
    transform: translateY(-4px);
    box-shadow: 0 12px 40px fade(@nordic-accent, 50%);
  }

  // 购物车图标容器
  .cart-icon-wrapper {
    position: relative;
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: @nordic-space-xs;
    pointer-events: none;

    .cart-icon {
      color: @nordic-white;
      font-size: 2rem;
    }
  }

  // 徽章样式
  .cart-badge {
    position: absolute;
    top: -4px;
    right: -8px;
    transform: translate(50%, -50%);
    pointer-events: none;
    z-index: 1;

    :deep(.el-badge__content) {
      background: linear-gradient(135deg, @nordic-yellow 0%, darken(@nordic-yellow, 10%) 100%);
      border: 2px solid @nordic-white;
      font-weight: 700;
      font-size: 0.75rem;
      min-width: 18px;
      height: 18px;
      line-height: 18px;
      padding: 0 5px;
      box-shadow: 0 2px 8px fade(@nordic-yellow, 40%);
    }
  }

  // 金额显示
  .cart-amount {
    font-size: 0.75rem;
    font-weight: 700;
    color: @nordic-white;
    text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
    pointer-events: none;
    white-space: nowrap;
    line-height: 1.2;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  // 当金额过长时调整字体
  .cart-amount.long {
    font-size: 10px;
  }
}

// 返回顶部按钮
.back-to-top {
  position: fixed;
  right: @nordic-space-lg;
  bottom: 200px;
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, @nordic-green 0%, @nordic-green-dark 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 16px fade(@nordic-green, 40%);
  z-index: 9998;
  border: 2px solid fade(@nordic-white, 30%);
  transition: all @nordic-transition-slow ease;
  color: @nordic-white;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 6px 24px fade(@nordic-green, 50%);
  }

  &:active {
    transform: translateY(-2px);
  }
}

// 淡入淡出动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity @nordic-transition-slow ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
