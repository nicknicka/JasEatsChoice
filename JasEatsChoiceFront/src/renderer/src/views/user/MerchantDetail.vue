<template>
  <div class="merchant-detail-container">
    <el-card class="merchant-detail-card">
      <!-- 返回按钮 -->
      <div class="back-button-container">
        <common-back-button />
      </div>

      <!-- 商家头部信息 -->
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
              <el-button type="text" size="small" class="favorite-button" @click="toggleFavorite">
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

      <!-- 商家基本信息卡片 -->
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
      <div class="menu-display-area">
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
            <div
              class="dish-card"
              v-for="item in menuItems.filter(
                (item) => item.menuId === activeMenuTab && item.category === category
              )"
              :key="item.id"
            >
              <div class="dish-image">{{ getCategoryEmoji(category) }}</div>
              <div class="dish-name">{{ item.name }}</div>
              <div class="dish-price">¥{{ calculateRealTimePrice(item).toFixed(2) }}</div>
              <div class="dish-desc">{{ item.description }}</div>

              <!-- 食材组成 -->
              <div class="dish-ingredients">
                <div class="ingredient-section" v-if="item.requiredIngredients && item.requiredIngredients.length > 0">
                  <span class="ingredient-title">必选食材:</span>
                  <div class="ingredient-list">
                    <span
                      class="ingredient-item"
                      v-for="ingredient in item.requiredIngredients"
                      :key="ingredient"
                      >{{ ingredient }}</span
                    >
                  </div>
                </div>

                <div class="ingredient-section" v-if="item.optionalIngredients && item.optionalIngredients.length > 0">
                  <span class="ingredient-title">可选食材:</span>
                  <div class="ingredient-list">
                    <el-checkbox
                      v-for="ingredient in item.optionalIngredients"
                      :key="ingredient.id || ingredient.name"
                      v-model="ingredient.selected"
                      class="ingredient-checkbox"
                    >
                      {{ ingredient.name }}
                      <span class="ingredient-price" v-if="ingredient.price">(+¥{{ ingredient.price.toFixed(2) }})</span>
                    </el-checkbox>
                  </div>
                </div>
              </div>

              <!-- 数量选择 -->
              <div class="dish-quantity">
                <el-input-number
                  v-model="item.quantity"
                  :min="1"
                  :max="10"
                  label="数量"
                  style="width: 100%"
                />
              </div>

              <el-button type="primary" size="small" @click="addMenuItem(item)" style="width: 100%">
                {{ viewMode === 'order' ? '立即购买' : '加入购物车' }}
              </el-button>
            </div>
          </div>
        </div>

        <!-- 用户评价 -->
        <div v-if="activeMenuTab === 'comments'" class="comments-section">
          <div class="comments-header">
            <h3 class="comments-title">用户评价</h3>
            <div class="comments-stats">
              <div class="average-rating">
                <div class="rating-number">4.7</div>
                <div class="rating-stars">
                  <el-rate :model-value="4.7" :disabled="true" size="small" show-score />
                </div>
              </div>
              <div class="total-comments">共 {{ comments.length }} 条评价</div>
            </div>
          </div>

          <!-- 商家没有菜单的提示 -->
          <div v-if="!hasMenus" class="no-menus-notice">
            <div class="notice-icon">📋</div>
            <p class="notice-text">当前商家还没有上架菜单</p>
          </div>

          <div class="comments-list">
            <div class="comment-card" v-for="comment in comments" :key="comment.id">
              <div class="comment-main">
                <div class="comment-avatar">
                  <el-icon :size="24"><User /></el-icon>
                </div>
                <div class="comment-body">
                  <div class="comment-header">
                    <div class="comment-user-info">
                      <span class="user-name">{{ comment.userName }}</span>
                      <el-tag size="small" class="user-badge">VIP会员</el-tag>
                    </div>
                    <span class="comment-date">{{ comment.date }}</span>
                  </div>
                  <div class="comment-rating">
                    <el-rate v-model="comment.rating" :disabled="true" size="small" />
                  </div>
                  <div class="comment-content">
                    {{ comment.comment }}
                  </div>

                  <!-- 展开/折叠回复按钮 -->
                  <div v-if="comment.replies && comment.replies.length > 0" class="reply-toggle">
                    <el-button
                      text
                      size="small"
                      @click="comment.expandReplies = !comment.expandReplies"
                      class="toggle-button"
                    >
                      <el-icon class="toggle-icon">
                        <component :is="comment.expandReplies ? 'ArrowUp' : 'ArrowDown'" />
                      </el-icon>
                      {{ comment.expandReplies ? '收起回复' : `查看回复 (${comment.replies.length})` }}
                    </el-button>
                  </div>
                </div>
              </div>

              <!-- 回复列表 -->
              <div v-if="comment.expandReplies && comment.replies.length > 0" class="replies-wrapper">
                <div class="replies-list">
                  <div
                    class="reply-card"
                    v-for="reply in comment.replies"
                    :key="reply.id"
                    :class="{ 'merchant-reply': reply.type === 'merchant' }"
                  >
                    <div class="reply-avatar">
                      <el-icon :size="20">
                        <component :is="reply.type === 'merchant' ? 'Shop' : 'User'" />
                      </el-icon>
                    </div>
                    <div class="reply-body">
                      <div class="reply-header">
                        <div class="reply-user-info">
                          <span class="reply-username">{{ reply.userName }}</span>
                          <el-tag
                            v-if="reply.type === 'merchant'"
                            size="small"
                            type="success"
                            class="merchant-badge"
                          >
                            商家
                          </el-tag>
                        </div>
                        <span class="reply-date">{{ reply.date }}</span>
                      </div>
                      <div class="reply-content">
                        {{ reply.comment }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
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

    <!-- 购物车弹窗 -->
    <el-dialog
      v-model="cartVisible"
      title="我的购物车"
      width="500px"
      @close="closeCart"
      :lock-scroll="false"
    >
      <div class="cart-content">
        <div v-if="cartItems.length === 0" class="empty-cart">
          <div class="empty-cart-icon">
            <el-icon :size="64"><ShoppingCart /></el-icon>
          </div>
          <div class="empty-cart-text">购物车是空的</div>
        </div>
        <div v-else class="cart-items-list">
          <div class="cart-item-card" v-for="(item, index) in cartItems" :key="item.id">
            <!-- 商品信息区(左侧) -->
            <div class="cart-item-left">
              <div class="cart-item-name">{{ item.name }}</div>

              <!-- 单价 -->
              <div class="cart-item-price">¥{{ item.price.toFixed(2) }}</div>

              <!-- 备注区域 -->
              <div class="cart-item-note">
                <div class="note-display" v-if="!item.isEditingNote">
                  <div class="note-content-wrapper">
                    <span v-if="item.note" class="note-text">{{ item.note }}</span>
                    <span v-else class="note-empty">暂无备注</span>
                  </div>
                  <el-button
                    size="small"
                    class="edit-note-btn"
                    @click="item.isEditingNote = true"
                    text
                  >
                    <el-icon class="edit-icon"><Edit /></el-icon>
                  </el-button>
                </div>
                <div class="note-edit" v-else>
                  <el-input
                    v-model="item.tempNote"
                    placeholder="输入备注..."
                    size="small"
                    type="textarea"
                    :rows="2"
                    resize="none"
                    autofocus
                  />
                  <div class="note-actions">
                    <el-button size="small" type="primary" @click="confirmNote(item)" class="confirm-note-btn">
                      确认
                    </el-button>
                    <el-button size="small" @click="cancelNote(item)" class="cancel-note-btn">取消</el-button>
                  </div>
                </div>
              </div>

              <!-- 可选食材展示 -->
              <div
                v-if="
                  item.selectedOptionalIngredients && item.selectedOptionalIngredients.length > 0
                "
                class="cart-item-ingredients"
              >
                <span
                  v-for="(ingredient, idx) in item.selectedOptionalIngredients"
                  :key="idx"
                  class="ingredient-tag"
                >
                  +{{ ingredient.name }} (¥{{ ingredient.price.toFixed(2) }})
                </span>
              </div>
            </div>

            <!-- 数量和总价区(右侧) -->
            <div class="cart-item-right">
              <!-- 数量调整 -->
              <div class="quantity-control">
                <el-button
                  class="quantity-btn quantity-btn-decrease"
                  :disabled="item.quantity <= 1"
                  @click="
                    () => {
                      if (item.quantity > 1) {
                        item.quantity--
                        item.totalPrice = (item.price + getOptionalPrice(item)) * item.quantity
                      } else {
                        cartItems.splice(index, 1)
                        updateCartStats()
                      }
                    }
                  "
                  circle
                  size="small"
                >
                  <el-icon><Minus /></el-icon>
                </el-button>
                <span class="quantity-number">{{ item.quantity }}</span>
                <el-button
                  class="quantity-btn quantity-btn-increase"
                  @click="
                    () => {
                      item.quantity++
                      item.totalPrice = (item.price + getOptionalPrice(item)) * item.quantity
                    }
                  "
                  circle
                  size="small"
                >
                  <el-icon><Plus /></el-icon>
                </el-button>
              </div>

              <!-- 商品总价 -->
              <div class="cart-item-total">¥{{ item.totalPrice.toFixed(2) }}</div>
            </div>
          </div>

          <!-- 总计区域 -->
          <div class="cart-total-section">
            <div class="total-label">总计</div>
            <div class="total-amount">
              ¥{{ cartItems.reduce((total, item) => total + item.totalPrice, 0).toFixed(2) }}
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button class="cancel-btn" @click="closeCart">取消</el-button>
          <el-button type="primary" v-if="cartItems.length > 0" @click="submitOrder" class="submit-btn">
            提交订单
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star, StarFilled, Location, Clock, Coin, ShoppingCart, Shop, User, ArrowUp, ArrowDown, Edit, Plus, Minus } from '@element-plus/icons-vue'
import axios from 'axios'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import { useAuthStore } from '../../store/authStore'
import { useUserStore } from '../../store/userStore'

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
  const currentMenuItems = menuItems.value.filter(
    (item) => item.menuId === activeMenuTab.value
  )

  // 提取所有唯一的分类
  const categories = [...new Set(currentMenuItems.map((item) => item.category))].filter(
    (category) => category && category.trim() !== ''
  )

  return categories
})

// 评价数据
const comments = ref([
  {
    id: 1,
    userName: '张三',
    rating: 5,
    comment: '这家店的健康餐特别好吃，食材新鲜，味道不错！',
    date: '2024-05-20',
    replies: [
      {
        id: 11,
        type: 'customer',
        userName: '张三',
        comment: '追加评论：今天又点了一次，还是一样的好吃！',
        date: '2024-05-21'
      },
      {
        id: 12,
        type: 'merchant',
        userName: 'XX餐厅客服',
        comment: '感谢您的喜爱和追加评价，我们会继续保持品质！',
        date: '2024-05-21'
      }
    ],
    expandReplies: false
  },
  {
    id: 2,
    userName: '李四',
    rating: 4,
    comment: '味道很好，配送也很快，下次还会再来！',
    date: '2024-05-19',
    replies: [
      {
        id: 21,
        type: 'merchant',
        userName: 'XX餐厅客服',
        comment: '感谢您的支持，祝您用餐愉快！',
        date: '2024-05-19'
      }
    ],
    expandReplies: false
  },
  {
    id: 3,
    userName: '王五',
    rating: 3,
    comment: '价格有点贵，但是味道还可以。',
    date: '2024-05-18',
    replies: [],
    expandReplies: false
  }
])

// 菜单数据
const menuItems = ref([
  {
    id: 1,
    name: '经典健康套餐',
    category: 'signature',
    price: 28.8,
    description: '包含新鲜蔬菜沙拉、烤鸡胸肉和糙米饭',
    requiredIngredients: ['新鲜蔬菜沙拉', '烤鸡胸肉', '糙米饭'],
    optionalIngredients: [
      { id: 101, name: '额外鸡胸肉', price: 8.0, selected: false },
      { id: 102, name: '煎蛋', price: 2.5, selected: false },
      { id: 103, name: '额外蔬菜', price: 3.0, selected: false }
    ]
  },
  {
    id: 2,
    name: '高蛋白健身餐',
    category: 'signature',
    price: 35.0,
    description: '适合增肌人群的高蛋白套餐',
    requiredIngredients: ['烤牛肉', '煮鸡蛋', '西兰花', '糙米饭'],
    optionalIngredients: [
      { id: 201, name: '额外牛肉', price: 12.0 },
      { id: 202, name: '蛋白粉', price: 5.0 }
    ]
  },
  {
    id: 3,
    name: '素食套餐',
    category: 'signature',
    price: 22.5,
    description: '全素食，健康无负担',
    requiredIngredients: ['素食沙拉', '烤蔬菜', '藜麦饭'],
    optionalIngredients: [
      { id: 301, name: '额外素食沙拉', price: 4.0 },
      { id: 302, name: '坚果', price: 3.5 }
    ]
  },
  // 新增饮品数据
  {
    id: 4,
    category: 'drink',
    name: '可乐',
    price: 5.0,
    description: '碳酸饮料',
    requiredIngredients: ['可乐'],
    optionalIngredients: [
      { id: 401, name: '加冰', price: 0.0 },
      { id: 402, name: '加柠檬', price: 0.5 }
    ]
  },
  {
    id: 5,
    category: 'drink',
    name: '雪碧',
    price: 6.0,
    description: '碳酸饮料',
    requiredIngredients: ['雪碧'],
    optionalIngredients: [
      { id: 501, name: '加冰', price: 0.0 },
      { id: 502, name: '加薄荷', price: 0.5 }
    ]
  },
  {
    id: 6,
    category: 'drink',
    name: '酸梅汤',
    price: 7.0,
    description: '传统饮品',
    requiredIngredients: ['酸梅汤'],
    optionalIngredients: [
      { id: 601, name: '加冰', price: 0.0 },
      { id: 602, name: '加桂花', price: 0.5 }
    ]
  },
  // 新增主食数据
  {
    id: 7,
    category: 'staple',
    name: '米饭',
    price: 2.0,
    description: '主食',
    requiredIngredients: ['米饭'],
    optionalIngredients: [
      { id: 701, name: '加量', price: 1.0 },
      { id: 702, name: '小米饭', price: 0.5 }
    ]
  },
  {
    id: 8,
    category: 'staple',
    name: '面条',
    price: 3.0,
    description: '主食',
    requiredIngredients: ['面条'],
    optionalIngredients: [
      { id: 801, name: '加量', price: 1.5 },
      { id: 802, name: '鸡蛋面', price: 1.0 }
    ]
  },
  {
    id: 9,
    category: 'staple',
    name: '烧饼',
    price: 1.5,
    description: '主食',
    requiredIngredients: ['烧饼'],
    optionalIngredients: [
      { id: 901, name: '夹肉', price: 2.0 },
      { id: 902, name: '夹鸡蛋', price: 1.0 }
    ]
  }
])

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
})

// 从后端加载完整的商家详情和菜品信息
const loadMerchantDetails = async (merchantId) => {
  try {
    // 1. 先获取商家详情
    const merchantResponse = await axios.get(API_CONFIG.baseURL + API_CONFIG.merchant.detail + merchantId)
    console.log("获取商家详情 response:", merchantResponse.data)

    if (merchantResponse.data?.code === "200" && merchantResponse.data?.data) {
      // 更新商家信息
      merchant.value = {
        ...merchant.value,
        ...merchantResponse.data.data
      }
    }

    // 2. 再获取商家的菜单数据
    const menuResponse = await axios.get(`${API_CONFIG.baseURL}/v1/menus/merchants/${merchantId}/menu`)
    console.log("获取商家菜单 response:", menuResponse.data)

    if (menuResponse.data?.code === "200" && menuResponse.data?.data && menuResponse.data.data.length > 0) {
      console.log("✅ 菜单数据存在，菜单数量:", menuResponse.data.data.length)

      // 为菜单项目添加必要的属性
      const allMenuItems = []

      // 遍历所有菜单
      menuResponse.data.data.forEach((menu) => {
        console.log("📋 处理菜单:", menu.menuName, "菜单ID:", menu.id, "菜品数量:", menu.dishes?.length || 0)
        if (menu.dishes && menu.dishes.length > 0) {
          menu.dishes.forEach((dish) => {
            console.log("  🍲 菜品:", dish.name, "category:", dish.category, "id:", dish.id)
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
      console.log("📦 最终 menuItems 数量:", menuItems.value.length)
      console.log("📦 menuItems 详情:", menuItems.value.map(item => ({
        name: item.name,
        menuId: item.menuId,
        category: item.category,
        price: item.price,
        description: item.description,
        image: item.image,
        requiredIngredients: item.requiredIngredients,
        optionalIngredients: item.optionalIngredients
      })))
      console.log("📦 第一个菜品的完整数据:", menuItems.value[0])

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
      console.log("🏷️ 生成的标签页:", menuTabs.value)

      // 添加用户评价标签
      menuTabs.value.push({ value: 'comments', label: '用户评价' })

      // 默认激活第一个菜单
      activeMenuTab.value = menuResponse.data.data[0].id
      console.log("🎯 默认激活的标签页 (activeMenuTab):", activeMenuTab.value, "类型:", typeof activeMenuTab.value)

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
    // 失败时使用模拟数据作为备份
    ElMessage.warning('加载商家详情失败，将使用模拟数据')
    // 设置hasMenus为true，因为模拟数据有菜单
    hasMenus.value = true
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
    if (response.data && response.data.success && response.data.code === "200") {
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
          id: merchant.value.id
        }
      })

      // 后端返回格式: { success: true, code: "200", message: "成功", data: null }
      if (response.data && response.data.success && response.data.code === "200") {
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
        collectableId: merchant.value.id
      }

      const response = await axios.post(API_CONFIG.baseURL + API_CONFIG.collection.add, collectionData)

      // 后端返回格式: { success: true, code: "200", message: "成功", data: 14(收藏ID) }
      if (response.data && response.data.success && response.data.code === "200") {
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

// 初始化数量和可选食材选中状态（仅用于模拟数据）
menuItems.value.forEach((item) => {
  item.quantity = 1
  if (item.optionalIngredients && Array.isArray(item.optionalIngredients)) {
    item.optionalIngredients = item.optionalIngredients.map((ingredient) => {
      if (typeof ingredient === 'string') {
        return {
          name: ingredient,
          price: 0,
          selected: false
        }
      }
      return {
        ...ingredient,
        selected: false
      }
    })
  }
})

// 计算实时价格函数
const calculateRealTimePrice = (item) => {
  if (!item) {
    console.log("calculateRealTimePrice: item is null/undefined")
    return 0
  }
  const optionalTotal = item.optionalIngredients.reduce((sum, ingredient) => {
    return sum + (ingredient.selected ? ingredient.price : 0)
  }, 0)
  const result = item.price + optionalTotal
  console.log(`💰 计算价格 - 菜品: ${item.name}, basePrice: ${item.price}, optionalTotal: ${optionalTotal}, finalPrice: ${result}`)
  return result
}

// 根据菜品分类返回对应的 emoji 图标
const getCategoryEmoji = (category) => {
  const emojiMap = {
    '招牌菜': '🔥',
    '主食': '🍚',
    '饮品': '🥤',
    '小吃': '🍢',
    '甜点': '🍰',
    '汤': '🍲',
    '凉菜': '🥗',
    '热菜': '🍛'
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

// 确认添加备注
const confirmNote = (item) => {
  item.note = item.tempNote
  item.isEditingNote = false // Exit edit mode
  ElMessage.success('备注已保存')
}

// 取消添加备注
const cancelNote = (item) => {
  item.tempNote = item.note // Reset temp note to current note
  item.isEditingNote = false // Exit edit mode
  ElMessage.info('已取消备注修改')
}

// 计算可选食材总价
const getOptionalPrice = (item) => {
  if (!item.selectedOptionalIngredients || item.selectedOptionalIngredients.length === 0) {
    return 0
  }
  return item.selectedOptionalIngredients.reduce((sum, ingredient) => sum + ingredient.price, 0)
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
  console.log("🔄 标签页切换")
  console.log("  旧标签:", oldTab, "类型:", typeof oldTab)
  console.log("  新标签:", newTab, "类型:", typeof newTab)

  // 测试招牌菜过滤
  const signatureDishes = menuItems.value.filter(
    (item) => item.menuId === newTab && item.category === '招牌菜'
  )
  console.log("  🔥 招牌菜过滤结果数量:", signatureDishes.length)
  if (signatureDishes.length > 0) {
    console.log("  🔥 招牌菜详情:", signatureDishes.map(d => ({
      name: d.name,
      menuId: d.menuId,
      category: d.category
    })))
  }

  // 测试主食过滤
  const stapleDishes = menuItems.value.filter(
    (item) => item.menuId === newTab && item.category === '主食'
  )
  console.log("  🍚 主食过滤结果数量:", stapleDishes.length)
  if (stapleDishes.length > 0) {
    console.log("  🍚 主食详情:", stapleDishes.map(d => ({
      name: d.name,
      menuId: d.menuId,
      category: d.category
    })))
  }

  // 测试饮品过滤
  const drinkDishes = menuItems.value.filter(
    (item) => item.menuId === newTab && item.category === '饮品'
  )
  console.log("  🥤 饮品过滤结果数量:", drinkDishes.length)

  // 测试所有菜品的 menuId 匹配
  const allMatchingItems = menuItems.value.filter((item) => item.menuId === newTab)
  console.log("  📋 所有匹配当前标签的菜品数量:", allMatchingItems.length)
  if (allMatchingItems.length === 0) {
    console.log("  ⚠️ 没有找到匹配的菜品！")
    console.log("  📦 所有 menuItems 的 menuId:", menuItems.value.map(item => ({
      name: item.name,
      menuId: item.menuId,
      menuIdType: typeof item.menuId
    })))
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

    // 商家头部信息
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
            background: linear-gradient(135deg, rgba(255, 255, 255, 0.2) 0%, rgba(255, 255, 255, 0.1) 100%);
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

    // 商家基本信息
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

        // 菜品卡片
        .dish-card {
          border: 1px solid rgba(59, 130, 246, 0.1);
          border-radius: 16px;
          padding: 24px;
          display: flex;
          flex-direction: column;
          background: linear-gradient(to bottom, #ffffff 0%, #f8fafc 100%);
          box-shadow: 0 2px 12px rgba(59, 130, 246, 0.08);
          transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
          position: relative;
          overflow: hidden;

          &::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 3px;
            background: linear-gradient(90deg, #3b82f6 0%, #06b6d4 100%);
            opacity: 0;
            transition: opacity 0.3s ease;
          }

          &:hover {
            box-shadow: 0 12px 40px rgba(59, 130, 246, 0.18);
            transform: translateY(-6px);
            border-color: rgba(59, 130, 246, 0.2);

            &::before {
              opacity: 1;
            }
          }

          .dish-image {
            font-size: 64px;
            margin-bottom: 16px;
            text-align: center;
            filter: drop-shadow(0 4px 8px rgba(59, 130, 246, 0.15));
          }

          .dish-name {
            font-size: 18px;
            font-weight: 700;
            color: #1e293b;
            text-align: center;
            line-height: 1.4;
            margin-bottom: 8px;
          }

          .dish-price {
            font-size: 24px;
            color: #f59e0b;
            font-weight: 700;
            text-align: center;
            background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            margin-bottom: 12px;
          }

          .dish-desc {
            font-size: 13px;
            color: #64748b;
            text-align: center;
            margin-bottom: 16px;
            line-height: 1.6;
            padding: 0 8px;
          }

          // 食材组成
          .dish-ingredients {
            width: 100%;
            margin: 12px 0;
            padding: 16px;
            background: rgba(59, 130, 246, 0.03);
            border-radius: 12px;
            border: 1px solid rgba(59, 130, 246, 0.08);

            .ingredient-section {
              margin-bottom: 16px;

              &:last-child {
                margin-bottom: 0;
              }

              .ingredient-title {
                display: block;
                font-weight: 600;
                color: #334155;
                margin-bottom: 8px;
                font-size: 13px;
              }

              .ingredient-list {
                display: flex;
                flex-direction: column;
                gap: 8px;

                .ingredient-item {
                  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
                  color: #ffffff;
                  padding: 6px 12px;
                  border-radius: 6px;
                  font-size: 12px;
                  display: inline-block;
                  font-weight: 500;
                  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.2);
                }

                .ingredient-checkbox {
                  display: flex;
                  align-items: center;
                  gap: 8px;
                  padding: 8px;
                  background: #ffffff;
                  border-radius: 8px;
                  border: 1px solid rgba(59, 130, 246, 0.1);
                  transition: all 0.3s ease;

                  &:hover {
                    background: rgba(59, 130, 246, 0.05);
                    border-color: rgba(59, 130, 246, 0.2);
                  }

                  .ingredient-price {
                    color: #f59e0b;
                    font-size: 12px;
                    font-weight: 600;
                  }
                }
              }
            }
          }

          // 数量选择
          .dish-quantity {
            width: 100%;
            margin: 12px 0;

            :deep(.el-input-number) {
              width: 100%;

              .el-input-number__decrease,
              .el-input-number__increase {
                background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
                border-color: transparent;
                color: #ffffff;

                &:hover {
                  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
                }
              }
            }
          }

          .el-button {
            width: 100%;
            background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
            border: none;
            border-radius: 12px;
            height: 44px;
            font-size: 15px;
            font-weight: 600;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);

            &:hover {
              background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
              transform: translateY(-2px);
              box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
            }

            &:active {
              transform: translateY(0);
            }
          }
        }
      }

      // 用户评价
      .comments-section {
        margin-bottom: 32px;

        .comments-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 24px;
          padding: 20px;
          background: linear-gradient(135deg, #eff6ff 0%, #f0f9ff 100%);
          border-radius: 12px;
          border: 1px solid rgba(59, 130, 246, 0.1);

          .comments-title {
            font-size: 20px;
            font-weight: 700;
            color: #1e40af;
            margin: 0;
          }

          .comments-stats {
            display: flex;
            gap: 24px;
            align-items: center;

            .average-rating {
              display: flex;
              align-items: center;
              gap: 12px;

              .rating-number {
                font-size: 32px;
                font-weight: 700;
                color: #f59e0b;
                line-height: 1;
              }

              .rating-stars {
                display: flex;
                flex-direction: column;
                align-items: flex-start;
              }
            }

            .total-comments {
              font-size: 14px;
              color: #64748b;
              font-weight: 500;
              padding-left: 24px;
              border-left: 2px solid rgba(59, 130, 246, 0.2);
            }
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

        .comments-list {
          display: flex;
          flex-direction: column;
          gap: 20px;
        }

        .comment-card {
          background: #ffffff;
          border-radius: 16px;
          box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
          border: 1px solid rgba(59, 130, 246, 0.08);
          overflow: hidden;
          transition: all 0.3s ease;

          &:hover {
            box-shadow: 0 4px 20px rgba(59, 130, 246, 0.12);
            transform: translateY(-2px);
          }

          .comment-main {
            display: flex;
            gap: 16px;
            padding: 20px;

            .comment-avatar {
              width: 48px;
              height: 48px;
              border-radius: 50%;
              background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
              display: flex;
              align-items: center;
              justify-content: center;
              color: #ffffff;
              flex-shrink: 0;
              box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
            }

            .comment-body {
              flex: 1;
              min-width: 0;

              .comment-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 12px;

                .comment-user-info {
                  display: flex;
                  align-items: center;
                  gap: 8px;

                  .user-name {
                    font-size: 15px;
                    font-weight: 600;
                    color: #1e293b;
                  }

                  .user-badge {
                    background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
                    border: none;
                    color: white;
                    font-size: 11px;
                    padding: 2px 8px;
                    height: auto;
                    font-weight: 500;
                  }
                }

                .comment-date {
                  font-size: 12px;
                  color: #94a3b8;
                  font-weight: 500;
                }
              }

              .comment-rating {
                margin-bottom: 12px;

                :deep(.el-rate) {
                  .el-rate__icon {
                    font-size: 16px;
                  }
                }
              }

              .comment-content {
                font-size: 14px;
                color: #475569;
                line-height: 1.7;
                margin-bottom: 12px;
                font-weight: 400;
              }

              .reply-toggle {
                margin-top: 12px;
                padding-top: 12px;
                border-top: 1px dashed rgba(59, 130, 246, 0.15);

                .toggle-button {
                  color: #3b82f6;
                  font-weight: 500;
                  padding: 0;
                  font-size: 13px;

                  &:hover {
                    color: #2563eb;
                    background: transparent;
                  }

                  .toggle-icon {
                    margin-right: 4px;
                    font-size: 14px;
                  }
                }
              }
            }
          }

          .replies-wrapper {
            background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
            border-top: 1px solid rgba(59, 130, 246, 0.1);
            padding: 16px 20px;

            .replies-list {
              display: flex;
              flex-direction: column;
              gap: 12px;
              margin: 0;
              padding: 0;

              .reply-card {
                display: flex;
                gap: 12px;
                padding: 0;
                background: transparent;
                border-radius: 0;
                transition: all 0.3s ease;

                &:hover {
                  background: rgba(59, 130, 246, 0.03);
                  border-radius: 8px;
                  padding: 8px;
                  margin: -8px;
                }

                .reply-avatar {
                  width: 36px;
                  height: 36px;
                  border-radius: 50%;
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  flex-shrink: 0;
                  background: linear-gradient(135deg, #64748b 0%, #475569 100%);
                  color: #ffffff;
                  box-shadow: 0 2px 8px rgba(100, 116, 139, 0.3);
                }

                .reply-body {
                  flex: 1;
                  min-width: 0;

                  .reply-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-bottom: 8px;

                    .reply-user-info {
                      display: flex;
                      align-items: center;
                      gap: 8px;

                      .reply-username {
                        font-size: 14px;
                        font-weight: 600;
                        color: #334155;
                      }

                      .merchant-badge {
                        background: linear-gradient(135deg, #10b981 0%, #059669 100%);
                        border: none;
                        color: white;
                        font-size: 11px;
                        padding: 2px 8px;
                        height: auto;
                        font-weight: 500;
                      }
                    }

                    .reply-date {
                      font-size: 11px;
                      color: #94a3b8;
                      font-weight: 500;
                    }
                  }

                  .reply-content {
                    font-size: 13px;
                    color: #475569;
                    line-height: 1.6;
                  }
                }

                &.merchant-reply {
                  .reply-avatar {
                    background: linear-gradient(135deg, #10b981 0%, #059669 100%);
                    box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);
                  }
                }
              }
            }
          }
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

// 购物车弹窗样式
.cart-content {
  padding: 0;

  .empty-cart {
    text-align: center;
    padding: 60px 20px;

    .empty-cart-icon {
      margin-bottom: 20px;
      opacity: 0.3;
      color: #94a3b8;
    }

    .empty-cart-text {
      font-size: 16px;
      color: #64748b;
      font-weight: 500;
    }
  }

  .cart-items-list {
    max-height: 500px;
    overflow-y: auto;
    padding: 16px;

    // 商品卡片
    .cart-item-card {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      padding: 16px;
      margin-bottom: 16px;
      background: #ffffff;
      border-radius: 12px;
      border: 1px solid #e8e8e8;
      transition: all 0.3s ease;

      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
        border-color: #1890ff;
      }

      // 左侧商品信息区
      .cart-item-left {
        flex: 1;
        min-width: 0;
        margin-right: 16px;

        .cart-item-name {
          font-size: 16px;
          font-weight: 600;
          color: #1a1a1a;
          margin-bottom: 8px;
          line-height: 1.4;
        }

        .cart-item-price {
          font-size: 14px;
          color: #1890ff;
          font-weight: 500;
          margin-bottom: 12px;
        }

        // 备注区域
        .cart-item-note {
          margin: 8px 0;

          .note-display {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 8px;
            padding: 8px 12px;
            background: #f5f7fa;
            border-radius: 6px;
            min-height: 36px;

            .note-content-wrapper {
              flex: 1;
              min-width: 0;

              .note-text {
                font-size: 13px;
                color: #333333;
                word-wrap: break-word;
                word-break: break-all;
                line-height: 1.5;
              }

              .note-empty {
                font-size: 13px;
                color: #999999;
              }
            }

            .edit-note-btn {
              flex-shrink: 0;
              padding: 4px;
              color: #1890ff;
              transition: all 0.3s ease;

              .edit-icon {
                font-size: 16px;
              }

              &:hover {
                background: rgba(24, 144, 255, 0.1);
                border-radius: 4px;
              }
            }
          }

          .note-edit {
            .el-textarea {
              margin-bottom: 8px;

              :deep(.el-textarea__inner) {
                font-size: 13px;
                padding: 8px;
                border-radius: 6px;
              }
            }

            .note-actions {
              display: flex;
              gap: 8px;

              .confirm-note-btn {
                border-radius: 6px;
                padding: 6px 16px;
                background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
                border: none;
                font-weight: 500;
                transition: all 0.3s ease;

                &:hover {
                  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
                  transform: translateY(-1px);
                  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
                }
              }

              .cancel-note-btn {
                border-radius: 6px;
                padding: 6px 16px;
                border: 1px solid #d9d9d9;
                color: #666;
                background: #ffffff;
                font-weight: 500;
                transition: all 0.3s ease;

                &:hover {
                  color: #3b82f6;
                  border-color: #3b82f6;
                  background: rgba(59, 130, 246, 0.05);
                  transform: translateY(-1px);
                }
              }
            }
          }
        }

        // 可选食材标签
        .cart-item-ingredients {
          margin-top: 8px;

          .ingredient-tag {
            display: inline-block;
            font-size: 12px;
            color: #1890ff;
            background: #e6f7ff;
            border: 1px solid #91d5ff;
            padding: 4px 8px;
            border-radius: 4px;
            margin-right: 6px;
            margin-bottom: 4px;
            font-weight: 500;
          }
        }
      }

      // 右侧数量和总价区
      .cart-item-right {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 12px;
        flex-shrink: 0;

        // 数量控制器
        .quantity-control {
          display: flex;
          align-items: center;
          gap: 12px;

          .quantity-btn {
            width: 32px;
            height: 32px;
            padding: 0;
            display: flex;
            align-items: center;
            justify-content: center;
            border: 1px solid #d9d9d9;
            transition: all 0.3s ease;

            &.quantity-btn-decrease {
              &:not(:disabled):hover {
                color: #1890ff;
                border-color: #1890ff;
              }

              &:disabled {
                color: #d9d9d9;
                border-color: #d9d9d9;
                cursor: not-allowed;
              }
            }

            &.quantity-btn-increase {
              background: #1890ff;
              border-color: #1890ff;
              color: #ffffff;

              &:hover {
                background: #40a9ff;
                border-color: #40a9ff;
              }
            }
          }

          .quantity-number {
            min-width: 24px;
            text-align: center;
            font-size: 16px;
            font-weight: 600;
            color: #1a1a1a;
          }
        }

        // 商品总价
        .cart-item-total {
          font-size: 16px;
          font-weight: 700;
          color: #1890ff;
          text-align: center;
          min-width: 80px;
        }
      }
    }

    // 总计区域
    .cart-total-section {
      display: flex;
      justify-content: flex-end;
      align-items: center;
      padding: 16px 20px;
      margin-top: 8px;
      background: linear-gradient(135deg, #f0f5ff 0%, #e6f7ff 100%);
      border-radius: 12px;
      border: 1px solid #adc6ff;

      .total-label {
        font-size: 16px;
        font-weight: 600;
        color: #333333;
        margin-right: 12px;
      }

      .total-amount {
        font-size: 20px;
        font-weight: 700;
        color: #1890ff;
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
  transition: box-shadow 0.3s ease, transform 0.2s ease;
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

// 对话框footer按钮样式
:deep(.el-dialog__footer) {
  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;

    .cancel-btn {
      min-width: 80px;
      height: 38px;
      border: 1px solid #d9d9d9;
      color: #666;
      background: #ffffff;
      border-radius: 8px;
      font-weight: 500;
      transition: all 0.3s ease;

      &:hover {
        color: #3b82f6;
        border-color: #3b82f6;
        background: rgba(59, 130, 246, 0.05);
        transform: translateY(-1px);
      }
    }

    .submit-btn {
      min-width: 100px;
      height: 38px;
      background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
      border: none;
      border-radius: 8px;
      font-weight: 600;
      transition: all 0.3s ease;
      box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);

      &:hover {
        background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
        transform: translateY(-1px);
        box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
      }
    }
  }
}
</style>
