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
        <!-- 当前菜单名称 (仅在非用户评价标签时显示) -->
        <div v-if="activeMenuTab !== 'comments'" class="current-menu-name">
          <h2 class="menu-name-title">{{ currentMenuName }}</h2>
        </div>

        <!-- 动态渲染所有菜品分类 -->
        <div
          v-for="category in currentMenuCategories"
          :key="category"
          class="dish-category-section"
        >
          <h3 class="category-title">{{ getCategoryEmoji(category) }} {{ category }}</h3>
          <div class="dish-grid">
            <dish-card
              v-for="item in menuItems.filter(
                (item) => item.menuId === activeMenuTab && item.category === category
              )"
              :key="item.id"
              :dish="item"
              :category-emoji="getCategoryEmoji(category)"
              :view-mode="viewMode"
              @add-to-cart="addMenuItem"
              class="stagger-item"
            />
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
          <comments-section :comments="comments" :merchant-rating="merchant.rating || 4.5" />
        </div>
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
import { ShoppingCart } from '@element-plus/icons-vue'
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

// 评价数据
const comments = ref([])

// 菜单数据
const menuItems = ref([])

// 组件挂载时加载商家信息和恢复购物车
onMounted(() => {
  const savedMerchant = sessionStorage.getItem('selectedMerchant')
  if (savedMerchant) {
    // 从会话存储获取商家基本信息
    const baseMerchantInfo = JSON.parse(savedMerchant)
    merchant.value = { ...baseMerchantInfo }

    // 从后端获取完整的商家详情和菜品信息
    loadMerchantDetails(baseMerchantInfo.id)

    // 加载当前商家的独立购物车
    if (!cartItemsByMerchant.value[merchant.value.id]) {
      cartItemsByMerchant.value[merchant.value.id] = []
    }
    cartItems.value = cartItemsByMerchant.value[merchant.value.id]

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
      parsedOrder.merchant.id === merchant.value.id
    ) {
      // 清空当前购物车
      cartItemsByMerchant.value[merchant.value.id] = []
      // 恢复购物车项目
      parsedOrder.cartItems.forEach((item) => {
        // 确保购物车项目有必要的属性
        const cartItem = {
          ...item,
          note: item.note || '',
          tempNote: item.tempNote || '',
          isEditingNote: item.isEditingNote || false
        }
        cartItemsByMerchant.value[merchant.value.id].push(cartItem)
      })
      // 更新当前购物车引用
      cartItems.value = cartItemsByMerchant.value[merchant.value.id]
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
      cartItemsByMerchant.value[merchant.value.id] = []

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

// 从后端加载完整的商家详情和菜品信息
const loadMerchantDetails = async (merchantId) => {
  try {
    // 1. 先获取商家详情
    const merchantResponse = await axios.get(
      API_CONFIG.baseURL + API_CONFIG.merchant.detail + merchantId
    )
    console.log('获取商家详情 response:', merchantResponse.data)

    if (merchantResponse.data?.code === '200' && merchantResponse.data?.data) {
      // 更新商家信息
      merchant.value = {
        ...merchant.value,
        ...merchantResponse.data.data
      }
    }

    // 2. 再获取商家的菜单数据
    const menuResponse = await axios.get(
      `${API_CONFIG.baseURL}/v1/menus/merchants/${merchantId}/menu`
    )
    console.log('获取商家菜单 response:', menuResponse.data)

    if (
      menuResponse.data?.code === '200' &&
      menuResponse.data?.data &&
      menuResponse.data.data.length > 0
    ) {
      console.log('✅ 菜单数据存在，菜单数量:', menuResponse.data.data.length)

      // 为菜单项目添加必要的属性
      const allMenuItems = []

      // 遍历所有菜单
      menuResponse.data.data.forEach((menu) => {
        console.log(
          '📋 处理菜单:',
          menu.menuName,
          '菜单ID:',
          menu.id,
          '菜品数量:',
          menu.dishes?.length || 0
        )
        if (menu.dishes && menu.dishes.length > 0) {
          menu.dishes.forEach((dish) => {
            console.log('  🍲 菜品:', dish.name, 'category:', dish.category, 'id:', dish.id)
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
      menuItems.value = []
      menuTabs.value = [{ value: 'comments', label: '用户评价' }]
      activeMenuTab.value = 'comments'
      hasMenus.value = false
    }
  } catch (error) {
    console.error('加载商家详情和菜单失败:', error)
    ElMessage.error('加载商家详情失败，请稍后重试')
    hasMenus.value = false
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
        id: merchant.value.id
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
          id: String(merchant.value.id)
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
        collectableId: String(merchant.value.id)
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
  if (!merchant.value || !merchant.value.id) return

  // 确保当前购物车引用正确
  cartItems.value = cartItemsByMerchant.value[merchant.value.id]

  cartTotalQuantity.value = cartItems.value.reduce((total, item) => total + item.quantity, 0)
  cartTotalAmount.value = cartItems.value.reduce((total, item) => total + item.totalPrice, 0)
}

// 更新购物车 - 使用当前商家的购物车
const updateCart = (item) => {
  if (!merchant.value || !merchant.value.id) return

  // 获取当前商家的购物车
  const currentMerchantCart = cartItemsByMerchant.value[merchant.value.id]

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
.merchant-detail-container {
  padding: 0;
  min-height: 100vh;
  background-color: #f5f5f5;

  .merchant-detail-card {
    border-radius: 0;
    border: none;
    box-shadow: none;
    padding: 0;

    // 返回按钮
    .back-button-container {
      padding: 12px 24px;
      background-color: #ffffff;

      .back-button {
        font-size: 18px;
        color: #409eff;
        padding: 8px 16px;
        border-radius: 6px;
        transition: all 0.3s ease;

        &:hover {
          color: #66b1ff;
          background-color: rgba(64, 158, 255, 0.1);
        }
      }
    }

    // 菜单类型切换
    .menu-tabs {
      padding: 0 24px;
      background-color: #ffffff;
      border-bottom: 1px solid #e8e8e8;

      .merchant-menu-tabs {
        .el-tabs__nav {
          border-bottom: none;
        }

        .el-tabs__item {
          font-size: 14px;
          color: #666666;
          padding: 12px 0;
          transition: all 0.3s ease;

          &.is-active {
            color: #ff6b6b;
            border-bottom: 2px solid #ff6b6b;
            font-weight: 500;
          }
        }
      }
    }

    // 菜单展示区
    .menu-display-area {
      padding: 24px;
      background-color: #ffffff;

      // 当前菜单名称
      .current-menu-name {
        margin-bottom: 24px;

        .menu-name-title {
          font-size: 24px;
          font-weight: bold;
          color: #333;
          padding-bottom: 12px;
          border-bottom: 2px solid #e8e8e8;
        }
      }

      // 没有菜单的提示
      .no-menus-notice {
        margin: 24px 0;
        padding: 20px;
        background-color: #f5f5f5;
        border-radius: 8px;
        text-align: center;

        .notice-text {
          color: #999;
          font-size: 16px;
        }
      }

      // 菜品分类
      .dish-category-section {
        margin-bottom: 32px;

        .category-title {
          font-size: 18px;
          font-weight: bold;
          color: #333333;
          margin-bottom: 16px;
        }

        // 菜品网格布局
        .dish-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
          gap: 24px;
        }
      }

      // 没有菜单的提示
      .no-menus-notice {
        margin: 24px 0;
        padding: 32px;
        background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
        border-radius: 12px;
        text-align: center;
        border: 1px solid rgba(251, 191, 36, 0.3);

        .notice-icon {
          font-size: 48px;
          margin-bottom: 12px;
        }

        .notice-text {
          color: #92400e;
          font-size: 16px;
          font-weight: 500;
          margin: 0;
        }
      }
    }

    // 立即下单快捷操作区
    .quick-order-section {
      padding: 24px;
      background-color: #ffffff;
      border-top: 1px solid rgba(59, 130, 246, 0.1);

      .quick-order-button {
        width: 100%;
        height: 52px;
        font-size: 16px;
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
        border: none;
        border-radius: 12px;
        font-weight: 600;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        box-shadow: 0 4px 16px rgba(59, 130, 246, 0.3);

        &:hover {
          background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
          transform: translateY(-2px);
          box-shadow: 0 6px 24px rgba(59, 130, 246, 0.4);
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
  right: 24px;
  bottom: 100px;
  width: 88px;
  height: 88px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: grab;
  box-shadow: 0 8px 32px rgba(59, 130, 246, 0.5);
  color: white;
  transition:
    box-shadow 0.3s ease,
    transform 0.2s ease;
  z-index: 9999;
  border: 3px solid rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  user-select: none;
  touch-action: none;
  will-change: transform, left, top;
  padding: 8px;
  box-sizing: border-box;

  &:active {
    cursor: grabbing;
  }

  &:hover:not(:active) {
    transform: translateY(-4px);
    box-shadow: 0 12px 40px rgba(59, 130, 246, 0.5);
  }

  // 购物车图标容器
  .cart-icon-wrapper {
    position: relative;
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 4px;
    pointer-events: none;

    .cart-icon {
      color: #ffffff;
      font-size: 28px;
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
      background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
      border: 2px solid #ffffff;
      font-weight: 700;
      font-size: 11px;
      min-width: 18px;
      height: 18px;
      line-height: 18px;
      padding: 0 5px;
      box-shadow: 0 2px 8px rgba(245, 158, 11, 0.4);
    }
  }

  // 金额显示
  .cart-amount {
    font-size: 11px;
    font-weight: 700;
    color: #ffffff;
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
</style>
