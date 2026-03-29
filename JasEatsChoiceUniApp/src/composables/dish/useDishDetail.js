/**
 * Composable: useDishDetail
 * 用途：菜品详情相关逻辑
 * 包含：数据加载、收藏、购物车、导航
 * 创建时间：2026-03-20
 */
import { ref, computed } from 'vue'
import { useCartStore, useUserStore } from '@/store'
import { dishApi, favoriteApi, reviewApi } from '@/api'

export function useDishDetail() {
  const cartStore = useCartStore()
  const userStore = useUserStore()

  // 状态
  const dishId = ref('')
  const merchantId = ref('')
  const isFavorite = ref(false)
  const quantity = ref(1)
  const reviews = ref([])
  const recommendDishes = ref([])

  // 营养图标和标签
  const nutritionIcons = {
    calories: '🔥',
    protein: '💪',
    fat: '🧈',
    carbohydrate: '🍞',
    fiber: '🥬'
  }

  const nutritionLabels = {
    calories: '热量',
    protein: '蛋白质',
    fat: '脂肪',
    carbohydrate: '碳水',
    fiber: '纤维'
  }

  // 菜品详情数据
  const dishDetail = ref({
    id: '',
    name: '',
    description: '',
    images: [],
    tags: [],
    price: '0',
    originalPrice: '',
    sales: 0,
    nutrition: {},
    ingredients: [],
    merchant: {
      id: '',
      name: '',
      logo: '',
      rating: 0,
      monthlySales: 0
    },
    reviewCount: 0,
    reviewSummary: null
  })

  // 计算属性
  const cartCount = computed(() => cartStore.totalCount)

  // 营养列表（用于 NutritionGrid 组件）
  const nutritionList = computed(() => {
    if (!dishDetail.value.nutrition) return []
    return Object.entries(dishDetail.value.nutrition).map(([key, value]) => ({
      icon: nutritionIcons[key],
      value: value,
      label: nutritionLabels[key]
    }))
  })

  /**
   * 加载菜品详情
   */
  const loadDishDetail = async () => {
    try {
      uni.showLoading({ title: '加载中...' })

      const res = await dishApi.getDetail(dishId.value)

      // 数据映射
      dishDetail.value = {
        id: res.dishId || res.id,
        name: res.dishName || res.name,
        description: res.description || res.desc || '',
        images: res.images && res.images.length > 0 ? res.images : [res.image || res.coverImage],
        tags: res.tags || [],
        price: res.price ? String(res.price) : '0',
        originalPrice: res.originalPrice ? String(res.originalPrice) : '',
        sales: res.monthlySales || res.sales || 0,
        nutrition: res.nutrition || {},
        ingredients: res.ingredients || [],
        merchant: {
          id: res.merchantId || res.merchant?.id,
          name: res.merchantName || res.merchant?.name,
          logo: res.merchant?.logo || res.merchant?.avatar || '',
          rating: res.merchant?.rating || 0,
          monthlySales: res.merchant?.monthlySales || 0
        },
        reviewCount: res.reviewCount || 0,
        reviewSummary: res.reviewSummary || null
      }

      // 保存商家ID
      merchantId.value = dishDetail.value.merchant.id

      // 检查收藏状态
      await checkFavorite()

      uni.hideLoading()
    } catch (error) {
      console.error('加载菜品详情失败:', error)
      uni.hideLoading()
      uni.showToast({
        title: error.message || '加载失败',
        icon: 'none'
      })
    }
  }

  /**
   * 加载评价列表
   */
  const loadReviews = async () => {
    try {
      const res = await reviewApi.getDishReviews(dishId.value, {
        page: 1,
        size: 3
      })

      // 数据映射
      if (Array.isArray(res)) {
        reviews.value = res.map(review => ({
          id: review.reviewId || review.id,
          user: {
            name: review.userName || review.user?.name || '匿名用户',
            avatar: review.userAvatar || review.user?.avatar || ''
          },
          rating: review.rating || 5,
          content: review.content || '',
          images: review.images || [],
          merchantReply: review.merchantReply || null,
          date: review.createTime || review.createdAt || ''
        }))
      } else {
        reviews.value = []
      }
    } catch (error) {
      console.error('加载评价失败:', error)
      reviews.value = []
    }
  }

  /**
   * 加载相关推荐
   */
  const loadRecommendDishes = async () => {
    try {
      const params = { limit: 4 }

      // 如果有用户ID，用于个性化推荐
      if (userStore.isLogin) {
        params.userId = userStore.userInfo?.userId || userStore.userInfo?.id
      }

      const res = await dishApi.getRecommend(params)

      // 数据映射
      if (Array.isArray(res)) {
        recommendDishes.value = res.map(dish => ({
          id: dish.dishId || dish.id,
          name: dish.dishName || dish.name,
          price: dish.price ? String(dish.price) : '0',
          sales: dish.monthlySales || dish.sales || 0,
          image: dish.image || dish.coverImage
        }))
      }
    } catch (error) {
      console.error('加载推荐失败:', error)
      recommendDishes.value = []
    }
  }

  /**
   * 预览图片
   */
  const previewImage = (index) => {
    uni.previewImage({
      urls: dishDetail.value.images,
      current: index
    })
  }

  /**
   * 预览评价图片
   */
  const previewReviewImage = (images, index) => {
    uni.previewImage({
      urls: images,
      current: index
    })
  }

  /**
   * 检查收藏状态
   */
  const checkFavorite = async () => {
    try {
      if (!userStore.isLogin) {
        isFavorite.value = false
        return
      }

      const userId = userStore.userInfo?.userId || userStore.userInfo?.id
      const res = await favoriteApi.checkDish(dishId.value, { userId })
      isFavorite.value = res || false
    } catch (error) {
      console.error('检查收藏状态失败:', error)
      isFavorite.value = false
    }
  }

  /**
   * 切换收藏
   */
  const toggleFavorite = async () => {
    try {
      if (!userStore.isLogin) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        setTimeout(() => {
          uni.navigateTo({ url: '/pages/login/index' })
        }, 1500)
        return
      }

      uni.showLoading({ title: '处理中...' })

      const userId = userStore.userInfo?.userId || userStore.userInfo?.id

      if (isFavorite.value) {
        await favoriteApi.removeDish(dishId.value, { userId })
        isFavorite.value = false
      } else {
        await favoriteApi.addDish({
          userId,
          dishId: dishId.value,
          merchantId: merchantId.value
        })
        isFavorite.value = true
      }

      uni.hideLoading()
      uni.showToast({
        title: isFavorite.value ? '已收藏' : '已取消收藏',
        icon: 'success'
      })
    } catch (error) {
      console.error('收藏失败:', error)
      uni.hideLoading()
      uni.showToast({
        title: error.message || '操作失败',
        icon: 'none'
      })
    }
  }

  /**
   * 增加数量
   */
  const increaseQuantity = () => {
    quantity.value++
  }

  /**
   * 减少数量
   */
  const decreaseQuantity = () => {
    if (quantity.value > 1) {
      quantity.value--
    }
  }

  /**
   * 加入购物车
   */
  const addToCart = async () => {
    try {
      const { cartApi } = await import('@/api')

      await cartApi.add({
        dishId: dishId.value,
        merchantId: dishDetail.value.merchant.id,
        quantity: quantity.value,
        spec: '',
        remark: ''
      })

      // 同时更新本地store
      cartStore.addToCart({
        merchantId: dishDetail.value.merchant.id,
        dish: {
          id: dishDetail.value.id,
          name: dishDetail.value.name,
          price: parseFloat(dishDetail.value.price),
          image: dishDetail.value.images[0]
        },
        quantity: quantity.value
      })

      uni.showToast({
        title: '已加入购物车',
        icon: 'success',
        duration: 2000
      })

      quantity.value = 1
    } catch (error) {
      console.error('加入购物车失败:', error)
      uni.showToast({
        title: error.message || '添加失败',
        icon: 'none'
      })
    }
  }

  /**
   * 跳转到商家详情
   */
  const toMerchant = () => {
    uni.navigateTo({
      url: `/pages/merchant/detail/index?id=${dishDetail.value.merchant.id}`
    })
  }

  /**
   * 跳转到菜品详情
   */
  const toDishDetail = (id) => {
    dishId.value = id
    loadDishDetail()
    loadReviews()
    loadRecommendDishes()
    uni.pageScrollTo({ scrollTop: 0, duration: 300 })
  }

  /**
   * 查看全部评价
   */
  const viewAllReviews = () => {
    if (!dishId.value) {
      uni.showToast({ title: '菜品信息不存在', icon: 'none' })
      return
    }

    uni.navigateTo({
      url: `/pages-user/review/list/index?dishId=${dishId.value}&merchantId=${merchantId.value}`
    })
  }

  /**
   * 跳转到购物车
   */
  const toCart = () => {
    uni.switchTab({
      url: '/cart/index',
      fail: () => {
        uni.navigateTo({ url: '/cart/index' })
      }
    })
  }

  /**
   * 联系商家
   */
  const contactMerchant = async () => {
    try {
      if (!merchantId.value) {
        uni.showToast({ title: '商家信息不存在', icon: 'none' })
        return
      }

      const { chatApi } = await import('@/api')
      const res = await chatApi.createConversation({
        targetUserId: merchantId.value,
        dishId: dishId.value
      })

      if (res.code === 200 || res.conversationId) {
        const conversationId = res.conversationId || res.data?.conversationId || res.id

        uni.navigateTo({
          url: `/pages-common/chat/chat-room?conversationId=${conversationId}&merchantId=${merchantId.value}`,
          fail: () => {
            uni.navigateTo({
              url: `/pages-user/chat/chat-room?conversationId=${conversationId}&merchantId=${merchantId.value}`
            })
          }
        })
      } else {
        throw new Error(res.message || '创建会话失败')
      }
    } catch (error) {
      console.error('跳转聊天页面失败:', error)
      uni.showToast({
        title: error.message || '打开聊天失败',
        icon: 'none'
      })
    }
  }

  return {
    dishId,
    merchantId,
    isFavorite,
    quantity,
    reviews,
    recommendDishes,
    dishDetail,
    cartCount,
    nutritionList,
    loadDishDetail,
    loadReviews,
    loadRecommendDishes,
    previewImage,
    previewReviewImage,
    toggleFavorite,
    increaseQuantity,
    decreaseQuantity,
    addToCart,
    toMerchant,
    toDishDetail,
    viewAllReviews,
    toCart,
    contactMerchant
  }
}
