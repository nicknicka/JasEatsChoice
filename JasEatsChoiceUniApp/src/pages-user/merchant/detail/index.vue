<template>
  <view class="merchant-detail-container">
    <scroll-view class="scroll-container" scroll-y>
      <!-- 商家头部信息 -->
      <view class="merchant-header">
        <view class="header-bg"></view>

        <view class="merchant-info-card">
          <image class="merchant-logo" :src="merchantDetail.logo" mode="aspectFill" />

          <view class="merchant-basic">
            <view class="merchant-name">{{ merchantDetail.name }}</view>
            <view class="merchant-rating">
              <text class="star">⭐</text>
              <text class="rating-score">{{ merchantDetail.rating }}</text>
              <text class="rating-count">（{{ merchantDetail.reviewCount }}条评价）</text>
            </view>

            <view class="merchant-tags">
              <text class="tag" v-for="tag in merchantDetail.tags" :key="tag">{{ tag }}</text>
            </view>

            <view class="merchant-stats">
              <view class="stat-item">
                <text class="stat-value">{{ merchantDetail.monthlySales }}</text>
                <text class="stat-label">月售</text>
              </view>
              <view class="stat-divider"></view>
              <view class="stat-item">
                <text class="stat-value">{{ merchantDetail.dishCount }}</text>
                <text class="stat-label">菜品</text>
              </view>
              <view class="stat-divider"></view>
              <view class="stat-item">
                <text class="stat-value">{{ merchantDetail.deliveryTime }}</text>
                <text class="stat-label">分钟</text>
              </view>
            </view>
          </view>

          <view class="merchant-actions">
            <view class="action-btn" @click="toggleFavorite">
              <text class="action-icon">{{ isFavorite ? '❤️' : '🤍' }}</text>
              <text class="action-text">{{ isFavorite ? '已收藏' : '收藏' }}</text>
            </view>
            <view class="action-btn" @click="shareMerchant">
              <text class="action-icon">📤</text>
              <text class="action-text">分享</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 优惠券 -->
      <view class="coupon-section card" v-if="coupons.length > 0">
        <view class="section-title">店铺优惠</view>
        <scroll-view class="coupon-scroll" scroll-x show-scrollbar="false">
          <view class="coupon-list">
            <view
              class="coupon-item"
              v-for="coupon in coupons"
              :key="coupon.id"
              @click="receiveCoupon(coupon)"
            >
              <view class="coupon-left">
                <text class="coupon-amount">¥{{ coupon.amount }}</text>
                <text class="coupon-condition">{{ coupon.condition }}</text>
              </view>
              <view class="coupon-right">
                <text class="coupon-btn">{{ coupon.received ? '已领取' : '领取' }}</text>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 商家公告 -->
      <view class="notice-section card" v-if="merchantDetail.notice">
        <view class="notice-icon">📢</view>
        <view class="notice-content">{{ merchantDetail.notice }}</view>
      </view>

      <!-- 菜品分类Tab -->
      <view class="category-tabs">
        <scroll-view class="tabs-scroll" scroll-x show-scrollbar="false">
          <view class="tabs-list">
            <view
              class="tab-item"
              :class="{ active: activeCategory === category.id }"
              v-for="category in categories"
              :key="category.id"
              @click="switchCategory(category.id)"
            >
              {{ category.name }}
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 菜品列表 -->
      <view class="dish-list-section">
        <view class="dish-list">
          <view
            class="dish-item"
            v-for="dish in currentDishes"
            :key="dish.id"
            @click="toDishDetail(dish.id)"
          >
            <image class="dish-image" :src="dish.image" mode="aspectFill" />

            <view class="dish-info">
              <view class="dish-name">{{ dish.name }}</view>
              <view class="dish-description">{{ dish.description }}</view>

              <view class="dish-bottom">
                <view class="price-section">
                  <text class="price-symbol">¥</text>
                  <text class="price-value">{{ dish.price }}</text>
                  <text class="price-original" v-if="dish.originalPrice">¥{{ dish.originalPrice }}</text>
                </view>
                <view class="sales-info">月售{{ dish.monthlySales }}</view>
              </view>

              <view class="dish-tags" v-if="dish.tags && dish.tags.length > 0">
                <text class="tag" v-for="tag in dish.tags" :key="tag">{{ tag }}</text>
              </view>
            </view>

            <view class="dish-action">
              <view class="add-btn" @click.stop="quickAdd(dish)">+</view>
            </view>
          </view>
        </view>
      </view>

      <!-- 商家评价 -->
      <view class="review-section card">
        <view class="section-header">
          <text class="section-title">用户评价</text>
          <text class="review-count">{{ merchantDetail.reviewCount }}条</text>
        </view>

        <view class="review-summary">
          <view class="rating-overview">
            <text class="rating-score">{{ merchantDetail.rating }}</text>
            <view class="rating-stars">
              <text class="star" v-for="i in 5" :key="i">
                {{ i <= Math.floor(merchantDetail.rating) ? '⭐' : '☆' }}
              </text>
            </view>
          </view>

          <view class="rating-tags">
            <text class="rating-tag" v-for="tag in reviewTags" :key="tag.label">
              {{ tag.label }} {{ tag.count }}条
            </text>
          </view>
        </view>

        <view class="review-list">
          <view class="review-item" v-for="review in reviews" :key="review.id">
            <view class="review-user">
              <image class="user-avatar" :src="review.user.avatar" mode="aspectFill" />
              <view class="user-info">
                <view class="user-name">{{ review.user.name }}</view>
                <view class="review-stars">
                  <text class="star" v-for="i in 5" :key="i">
                    {{ i <= review.rating ? '⭐' : '☆' }}
                  </text>
                </view>
              </view>
              <view class="review-date">{{ review.date }}</view>
            </view>

            <view class="review-content">{{ review.content }}</view>

            <view class="review-dishes" v-if="review.dishes && review.dishes.length > 0">
              <text class="dish-tag" v-for="dish in review.dishes" :key="dish">{{ dish }}</text>
            </view>
          </view>
        </view>

        <view class="view-all-reviews" @click="viewAllReviews">
          查看全部评价 ›
        </view>
      </view>

      <!-- 商家信息 -->
      <view class="merchant-info-section card">
        <view class="section-title">商家信息</view>

        <view class="info-item">
          <text class="info-label">营业时间</text>
          <text class="info-value">{{ merchantDetail.businessHours }}</text>
        </view>

        <view class="info-item">
          <text class="info-label">配送时间</text>
          <text class="info-value">{{ merchantDetail.deliveryTime }}分钟</text>
        </view>

        <view class="info-item">
          <text class="info-label">配送费</text>
          <text class="info-value">¥{{ merchantDetail.deliveryFee }}</text>
        </view>

        <view class="info-item">
          <text class="info-label">起送价</text>
          <text class="info-value">¥{{ merchantDetail.minOrderAmount }}</text>
        </view>

        <view class="info-item">
          <text class="info-label">商家地址</text>
          <text class="info-value">{{ merchantDetail.address }}</text>
        </view>

        <view class="info-item" @click="callMerchant">
          <text class="info-label">联系电话</text>
          <text class="info-value phone">{{ merchantDetail.phone }} ›</text>
        </view>
      </view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="bar-left">
        <view class="bar-icon" @click="toCart">
          <text class="icon">🛒</text>
          <view class="badge" v-if="cartCount > 0">{{ cartCount }}</view>
          <view class="cart-total" v-if="cartTotal > 0">¥{{ cartTotal }}</view>
        </view>
      </view>

      <view class="bar-right">
        <view class="start-order-btn" @click="startOrder">
          去结算
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useCartStore, useUserStore } from '@/store'
import { merchantApi, dishApi, couponApi } from '@/api'

// Store
const cartStore = useCartStore()
const userStore = useUserStore()

// 状态
const merchantId = ref('')
const isFavorite = ref(false)
const activeCategory = ref('all')

// 计算属性
const cartCount = computed(() => cartStore.totalCount)
const cartTotal = computed(() => cartStore.totalPrice.toFixed(2))

// 商家详情数据
const merchantDetail = ref({
  id: '',
  name: '老王家常菜',
  logo: 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=老王',
  rating: 4.8,
  reviewCount: 256,
  tags: ['家常菜', '配送快', '好评多'],
  monthlySales: 999,
  dishCount: 35,
  deliveryTime: 30,
  notice: '本店所有菜品均为现做，如需加急请提前联系，感谢您的理解与支持！',
  businessHours: '09:00-22:00',
  deliveryFee: 5,
  minOrderAmount: 20,
  address: 'XX市XX区XX街道XX号',
  phone: '13800138000'
})

// 优惠券数据
const coupons = ref([
  {
    id: 1,
    amount: 10,
    condition: '满50可用',
    received: false
  },
  {
    id: 2,
    amount: 5,
    condition: '满30可用',
    received: true
  },
  {
    id: 3,
    amount: 20,
    condition: '满100可用',
    received: false
  }
])

// 菜品分类
const categories = ref([
  { id: 'all', name: '全部' },
  { id: 'hot', name: '热销' },
  { id: 'recommend', name: '推荐' },
  { id: 'meat', name: '荤菜' },
  { id: 'vegetable', name: '素菜' },
  { id: 'soup', name: '汤类' },
  { id: 'staple', name: '主食' }
])

// 菜品列表
const dishes = ref({
  all: [],
  hot: [],
  recommend: [],
  meat: [],
  vegetable: [],
  soup: [],
  staple: []
})

// 当前分类的菜品
const currentDishes = computed(() => {
  return dishes.value[activeCategory.value] || []
})

// 评价标签
const reviewTags = ref([
  { label: '味道好', count: 156 },
  { label: '分量足', count: 128 },
  { label: '配送快', count: 98 },
  { label: '卫生好', count: 87 }
])

// 评价列表
const reviews = ref([])

/**
 * 加载商家详情
 */
const loadMerchantDetail = async () => {
  try {
    uni.showLoading({ title: '加载中...' })

    // 调用后端API获取商家详情
    const res = await merchantApi.getDetail(merchantId.value)

    // 数据映射
    merchantDetail.value = {
      id: res.merchantId || res.id,
      name: res.merchantName || res.name,
      logo: res.avatar || res.logo || res.coverImage,
      rating: res.rating || res.score || 5.0,
      reviewCount: res.reviewCount || res.commentCount || 0,
      tags: res.tags || [],
      monthlySales: res.monthlySales || 0,
      dishCount: res.dishCount || 0,
      deliveryTime: res.deliveryTime || 30,
      notice: res.notice || res.announcement || '',
      businessHours: res.businessHours || '09:00-22:00',
      deliveryFee: res.deliveryFee || 0,
      minOrderAmount: res.minOrderAmount || res.minPrice || 0,
      address: res.address || '',
      phone: res.phone || ''
    }

    // 检查是否收藏
    await checkFavorite()

    uni.hideLoading()
  } catch (error) {
    console.error('加载商家详情失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    })
  }
}

/**
 * 加载优惠券
 */
const loadCoupons = async () => {
  try {
    // 调用后端API获取商家优惠券
    const res = await merchantApi.getCoupons(merchantId.value)

    // 数据映射
    if (Array.isArray(res)) {
      coupons.value = res.map(coupon => ({
        id: coupon.couponId || coupon.id,
        amount: coupon.amount || coupon.discount || 0,
        condition: coupon.condition || coupon.minAmount ? `满${coupon.minAmount}可用` : '',
        received: coupon.received || false
      }))
    }
  } catch (error) {
    console.error('加载优惠券失败:', error)
    // 优惠券加载失败不影响页面显示
    coupons.value = []
  }
}

/**
 * 加载菜品列表
 */
const loadDishes = async () => {
  try {
    // 调用后端API获取商家菜品列表
    const res = await dishApi.getMerchantDishes(merchantId.value, { available: true })

    // 数据映射
    let allDishes = []
    if (Array.isArray(res)) {
      allDishes = res
    } else if (res && res.list) {
      allDishes = res.list
    }

    // 映射字段
    const mappedDishes = allDishes.map(dish => ({
      id: dish.dishId || dish.id,
      name: dish.dishName || dish.name,
      description: dish.description || dish.desc || '',
      price: dish.price ? String(dish.price) : '0',
      originalPrice: dish.originalPrice ? String(dish.originalPrice) : '',
      monthlySales: dish.monthlySales || dish.sales || 0,
      image: dish.image || dish.coverImage || dish.avatar,
      tags: dish.tags || [],
      category: dish.category || ''
    }))

    // 按分类分配
    dishes.value.all = mappedDishes
    dishes.value.hot = mappedDishes.filter(d => d.tags.includes('热销') || d.monthlySales > 100)
    dishes.value.recommend = mappedDishes.filter(d => d.tags.includes('推荐'))
    dishes.value.meat = mappedDishes.filter(d => d.category === '荤菜' || d.category === 'meat')
    dishes.value.vegetable = mappedDishes.filter(d => d.category === '素菜' || d.category === 'vegetable')
    dishes.value.soup = mappedDishes.filter(d => d.category === '汤类' || d.category === 'soup')
    dishes.value.staple = mappedDishes.filter(d => d.category === '主食' || d.category === 'staple')

  } catch (error) {
    console.error('加载菜品失败:', error)
    // 菜品加载失败使用空数组
    dishes.value = {
      all: [],
      hot: [],
      recommend: [],
      meat: [],
      vegetable: [],
      soup: [],
      staple: []
    }
  }
}

/**
 * 加载评价列表
 */
const loadReviews = async () => {
  try {
    // 调用后端API获取商家评价
    const res = await merchantApi.getReviews(merchantId.value, { page: 1, size: 3 })

    // 数据映射
    if (res && Array.isArray(res)) {
      reviews.value = res.map(review => ({
        id: review.reviewId || review.id,
        user: {
          avatar: review.userAvatar || review.user?.avatar,
          name: review.userName || review.user?.name || '用户***'
        },
        rating: review.rating || review.score || 5,
        date: review.createTime || review.date || '',
        content: review.content || review.comment || '',
        dishes: review.dishes || []
      }))
    }
  } catch (error) {
    console.error('加载评价失败:', error)
    // 评价加载失败不影响页面显示
    reviews.value = []
  }
}

/**
 * 切换分类
 */
const switchCategory = (categoryId) => {
  activeCategory.value = categoryId
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
    if (!userId) {
      isFavorite.value = false
      return
    }

    const res = await merchantApi.checkFavorite(userId, merchantId.value)
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
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      })
      setTimeout(() => {
        uni.navigateTo({
          url: '/pages/login/index'
        })
      }, 1500)
      return
    }

    const userId = userStore.userInfo?.userId || userStore.userInfo?.id
    if (!userId) {
      throw new Error('用户ID不存在')
    }

    uni.showLoading({ title: '处理中...' })

    if (isFavorite.value) {
      // 取消收藏
      await merchantApi.unfavorite(userId, merchantId.value)
      isFavorite.value = false
      uni.showToast({
        title: '已取消收藏',
        icon: 'success'
      })
    } else {
      // 添加收藏
      await merchantApi.favorite(userId, merchantId.value)
      isFavorite.value = true
      uni.showToast({
        title: '已收藏',
        icon: 'success'
      })
    }

    uni.hideLoading()
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
 * 分享商家
 */
/**
 * 分享商家 - U-016: 实现分享功能
 */
const shareMerchant = () => {
  // 检查是否支持分享
  if (!uni.shareProvider) {
    // 如果不支持原生分享，使用截图分享或复制链接
    uni.showActionSheet({
      itemList: ['复制链接', '保存图片'],
      success: (res) => {
        if (res.tapIndex === 0) {
          // 复制链接
          const shareUrl = `https://yourdomain.com/merchant/${merchantId.value}`
          uni.setClipboardData({
            data: shareUrl,
            success: () => {
              uni.showToast({
                title: '链接已复制',
                icon: 'success'
              })
            }
          })
        } else if (res.tapIndex === 1) {
          // 保存图片（生成二维码或截图）
          uni.showToast({
            title: '功能开发中',
            icon: 'none'
          })
        }
      }
    })
    return
  }

  // 使用原生分享功能
  uni.share({
    provider: 'weixin',
    scene: 'WXSceneSession',
    type: 0,
    title: merchantInfo.value.name || '佳食宜选商家',
    summary: merchantInfo.value.description || '欢迎光临',
    href: `https://yourdomain.com/merchant/${merchantId.value}`,
    imageUrl: merchantInfo.value.logo || merchantInfo.value.avatar || '',
    success: () => {
      uni.showToast({
        title: '分享成功',
        icon: 'success'
      })
    },
    fail: (err) => {
      console.error('分享失败:', err)
      uni.showToast({
        title: '分享失败',
        icon: 'none'
      })
    }
  })
}

/**
 * 领取优惠券
 */
const receiveCoupon = async (coupon) => {
  if (coupon.received) return

  try {
    if (!userStore.isLogin) {
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      })
      setTimeout(() => {
        uni.navigateTo({
          url: '/pages/login/index'
        })
      }, 1500)
      return
    }

    uni.showLoading({ title: '领取中...' })

    // 调用后端API领取优惠券
    await couponApi.receive(coupon.id)

    coupon.received = true

    uni.hideLoading()
    uni.showToast({
      title: '领取成功',
      icon: 'success'
    })
  } catch (error) {
    console.error('领取优惠券失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: error.message || '领取失败',
      icon: 'none'
    })
  }
}

/**
 * 跳转到菜品详情
 */
const toDishDetail = (dishId) => {
  uni.navigateTo({
    url: `/pages/dish/detail/index?id=${dishId}`
  })
}

/**
 * 快速添加到购物车
 */
const quickAdd = async (dish) => {
  try {
    // 使用store添加到购物车
    cartStore.addToCart({
      merchantId: merchantId.value,
      dish: {
        id: dish.id,
        name: dish.name,
        price: parseFloat(dish.price),
        image: dish.image
      },
      quantity: 1
    })

    uni.showToast({
      title: '已加入购物车',
      icon: 'success',
      duration: 1500
    })
  } catch (error) {
    console.error('加入购物车失败:', error)
  }
}

/**
 * 查看全部评价 - U-017: 跳转到评价列表页
 */
const viewAllReviews = () => {
  if (!merchantId.value) {
    uni.showToast({
      title: '商家信息不存在',
      icon: 'none'
    })
    return
  }

  // 跳转到商家评价列表页面
  uni.navigateTo({
    url: `/pages-user/review/list/index?merchantId=${merchantId.value}`,
    success: () => {
      console.log('跳转到评价列表页成功')
    },
    fail: (err) => {
      console.error('跳转评价列表页失败:', err)
      uni.showToast({
        title: '打开评价页面失败',
        icon: 'none'
      })
    }
  })
}

/**
 * 拨打商家电话
 */
const callMerchant = () => {
  uni.makePhoneCall({
    phoneNumber: merchantDetail.value.phone
  })
}

/**
 * 跳转到购物车 - U-018: 跳转到购物车页
 */
const toCart = () => {
  // 跳转到购物车页面
  uni.switchTab({
    url: '/pages-user/cart/index',
    success: () => {
      console.log('跳转到购物车成功')
    },
    fail: () => {
      // 如果switchTab失败（可能不是tabBar页面），使用navigateTo
      uni.navigateTo({
        url: '/pages-user/cart/index',
        success: () => {
          console.log('跳转到购物车成功')
        },
        fail: (err) => {
          console.error('跳转购物车失败:', err)
          uni.showToast({
            title: '打开购物车失败',
            icon: 'none'
          })
        }
      })
    }
  })
}

/**
 * 去结算 - U-019: 跳转到订单确认页
 */
const startOrder = () => {
  if (cartCount.value === 0) {
    uni.showToast({
      title: '请先添加菜品',
      icon: 'none'
    })
    return
  }

  if (!userStore.isLogin) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    setTimeout(() => {
      uni.navigateTo({
        url: '/pages/login/index'
      })
    }, 1500)
    return
  }

  // 将购物车数据存储到临时存储，用于订单确认页面
  try {
    const cartItems = cartStore.items
    const merchantItems = cartItems.filter(item => item.merchantId === merchantId.value)

    if (merchantItems.length === 0) {
      uni.showToast({
        title: '当前商家购物车为空',
        icon: 'none'
      })
      return
    }

    // 准备订单数据
    const tempItems = merchantItems.map(item => ({
      dishId: item.dish.id,
      dish: item.dish,
      merchantId: item.merchantId,
      merchant: merchantInfo.value,
      quantity: item.quantity,
      spec: item.spec || '',
      price: item.dish.price
    }))

    // 存储到临时存储
    uni.setStorageSync('temp_order_items', tempItems)
    uni.setStorageSync('temp_order_summary', {
      totalCount: tempItems.reduce((sum, item) => sum + item.quantity, 0),
      totalPrice: tempItems.reduce((sum, item) => sum + item.price * item.quantity, 0)
    })

    // 跳转到订单确认页面
    uni.navigateTo({
      url: '/pages-user/order/confirm/index',
      success: () => {
        console.log('跳转到订单确认页成功')
      },
      fail: (err) => {
        console.error('跳转订单确认页失败:', err)
        uni.showToast({
          title: '打开订单确认页失败',
          icon: 'none'
        })
      }
    })
  } catch (error) {
    console.error('准备订单数据失败:', error)
    uni.showToast({
      title: '订单准备失败',
      icon: 'none'
    })
  }
}

// 组件挂载时加载数据
onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options

  if (options.id) {
    merchantId.value = options.id
  }

  // 加载数据
  loadMerchantDetail()
  loadCoupons()
  loadDishes()
  loadReviews()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.merchant-detail-container {
  min-height: 100vh;
  background-color: $bg-color-base;
  padding-bottom: 120rpx;
}

.scroll-container {
  height: 100vh;
}

/* 卡片通用样式 */
.card {
  background-color: $bg-color-white;
  margin-bottom: $spacing-md;
  padding: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
  margin-bottom: $spacing-md;
}

/* 商家头部 */
.merchant-header {
  position: relative;
  padding-bottom: $spacing-md;
}

.header-bg {
  height: 200rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.merchant-info-card {
  position: relative;
  margin: -60rpx $spacing-md 0;
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: $spacing-md;
  box-shadow: $box-shadow-base;
}

.merchant-logo {
  width: 120rpx;
  height: 120rpx;
  border-radius: $border-radius-base;
  position: absolute;
  top: -$spacing-lg;
  left: $spacing-md;
  border: 4rpx solid $bg-color-white;
}

.merchant-basic {
  margin-left: 140rpx;
  margin-bottom: $spacing-md;
}

.merchant-name {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
  margin-bottom: $spacing-xs;
}

.merchant-rating {
  @include flex-center;
  gap: $spacing-xs;
  font-size: $font-size-sm;
  margin-bottom: $spacing-sm;

  .star {
    color: #f5a623;
  }

  .rating-score {
    font-weight: $font-weight-bold;
    color: $text-color-primary;
  }

  .rating-count {
    color: $text-color-secondary;
  }
}

.merchant-tags {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-xs;
  margin-bottom: $spacing-md;

  .tag {
    font-size: $font-size-xs;
    color: $primary-color;
    background-color: rgba(255, 107, 53, 0.1);
    padding: 6rpx 12rpx;
    border-radius: 6rpx;
  }
}

.merchant-stats {
  @include flex-center;
  gap: $spacing-md;
}

.stat-item {
  @include flex-center-column;
  gap: 4rpx;

  .stat-value {
    font-size: $font-size-lg;
    font-weight: $font-weight-bold;
    color: $text-color-primary;
  }

  .stat-label {
    font-size: $font-size-xs;
    color: $text-color-secondary;
  }
}

.stat-divider {
  width: 1rpx;
  height: 40rpx;
  background-color: $border-color-light;
}

.merchant-actions {
  @include flex-center;
  gap: $spacing-md;
  padding-top: $spacing-md;
  border-top: 1rpx solid $border-color-light;
}

.action-btn {
  flex: 1;
  @include flex-center-column;
  gap: 4rpx;
  padding: $spacing-sm;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;

  .action-icon {
    font-size: 36rpx;
  }

  .action-text {
    font-size: $font-size-sm;
    color: $text-color-regular;
  }
}

/* 优惠券 */
.coupon-section {
  .coupon-scroll {
    white-space: nowrap;
  }

  .coupon-list {
    display: flex;
    gap: $spacing-md;
  }

  .coupon-item {
    display: flex;
    flex-shrink: 0;
    width: 280rpx;
    height: 140rpx;
    background: linear-gradient(135deg, #FF6B35 0%, #FF8A5C 100%);
    border-radius: $border-radius-base;
    overflow: hidden;
  }

  .coupon-left {
    flex: 1;
    @include flex-center-column;
    gap: 4rpx;
    padding: $spacing-sm;
    border-right: 1rpx dashed rgba(255, 255, 255, 0.3);

    .coupon-amount {
      font-size: $font-size-xxl;
      font-weight: $font-weight-bold;
      color: #fff;
    }

    .coupon-condition {
      font-size: $font-size-xs;
      color: rgba(255, 255, 255, 0.9);
    }
  }

  .coupon-right {
    width: 80rpx;
    @include flex-center;

    .coupon-btn {
      font-size: $font-size-sm;
      color: #fff;
      padding: 8rpx 16rpx;
      background-color: rgba(255, 255, 255, 0.2);
      border-radius: $border-radius-round;
    }
  }
}

/* 商家公告 */
.notice-section {
  @include flex-center;
  gap: $spacing-sm;
  background-color: #fff9e6;
  border: 1rpx solid #ffe58f;

  .notice-icon {
    font-size: $font-size-lg;
  }

  .notice-content {
    flex: 1;
    font-size: $font-size-sm;
    color: $text-color-regular;
    @include text-ellipsis;
  }
}

/* 分类Tab */
.category-tabs {
  background-color: $bg-color-white;
  position: sticky;
  top: 0;
  z-index: $z-index-normal;
}

.tabs-scroll {
  white-space: nowrap;
}

.tabs-list {
  display: flex;
  padding: $spacing-sm $spacing-md;
}

.tab-item {
  flex-shrink: 0;
  padding: $spacing-sm $spacing-md;
  margin-right: $spacing-sm;
  font-size: $font-size-base;
  color: $text-color-regular;
  background-color: $bg-color-base;
  border-radius: $border-radius-round;
  transition: $transition-base;

  &.active {
    color: #fff;
    background-color: $primary-color;
    font-weight: $font-weight-medium;
  }
}

/* 菜品列表 */
.dish-list-section {
  padding: $spacing-md;
}

.dish-list {
  .dish-item {
    display: flex;
    background-color: $bg-color-white;
    border-radius: $border-radius-base;
    padding: $spacing-md;
    margin-bottom: $spacing-md;
  }

  .dish-image {
    width: 180rpx;
    height: 180rpx;
    border-radius: $border-radius-base;
    flex-shrink: 0;
  }

  .dish-info {
    flex: 1;
    margin-left: $spacing-md;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }

  .dish-name {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $text-color-primary;
    @include text-ellipsis;
  }

  .dish-description {
    font-size: $font-size-sm;
    color: $text-color-secondary;
    margin-top: $spacing-xs;
    @include text-ellipsis;
  }

  .dish-bottom {
    @include flex-between;
    margin-top: $spacing-xs;
  }

  .price-section {
    @include flex-center;
    gap: 4rpx;
    color: $danger-color;

    .price-symbol {
      font-size: $font-size-sm;
    }

    .price-value {
      font-size: $font-size-xl;
      font-weight: $font-weight-bold;
    }

    .price-original {
      font-size: $font-size-xs;
      color: $text-color-secondary;
      text-decoration: line-through;
      margin-left: $spacing-xs;
    }
  }

  .sales-info {
    font-size: $font-size-sm;
    color: $text-color-secondary;
  }

  .dish-tags {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-xs;
    margin-top: $spacing-xs;

    .tag {
      font-size: $font-size-xs;
      color: $primary-color;
      background-color: rgba(255, 107, 53, 0.1);
      padding: 4rpx 8rpx;
      border-radius: 4rpx;
    }
  }

  .dish-action {
    @include flex-center;
    align-self: flex-end;
  }

  .add-btn {
    width: 56rpx;
    height: 56rpx;
    @include flex-center;
    background-color: $primary-color;
    color: #fff;
    font-size: $font-size-xl;
    border-radius: 50%;
  }
}

/* 评价列表 */
.review-section {
  .section-header {
    @include flex-between;
    margin-bottom: $spacing-md;
  }

  .review-count {
    font-size: $font-size-sm;
    color: $text-color-secondary;
  }

  .review-summary {
    padding: $spacing-md;
    background-color: $bg-color-base;
    border-radius: $border-radius-base;
    margin-bottom: $spacing-md;
  }

  .rating-overview {
    @include flex-center;
    gap: $spacing-sm;
    margin-bottom: $spacing-sm;

    .rating-score {
      font-size: 48rpx;
      font-weight: $font-weight-bold;
      color: $text-color-primary;
    }

    .rating-stars {
      @include flex-center;
      gap: 4rpx;

      .star {
        font-size: $font-size-base;
        color: #f5a623;
      }
    }
  }

  .rating-tags {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;

    .rating-tag {
      font-size: $font-size-sm;
      color: $text-color-regular;
      background-color: $bg-color-white;
      padding: 8rpx 16rpx;
      border-radius: 8rpx;
    }
  }

  .review-list {
    .review-item {
      padding: $spacing-md 0;
      border-bottom: 1rpx solid $border-color-light;

      &:last-child {
        border-bottom: none;
      }
    }

    .review-user {
      @include flex-between;
      margin-bottom: $spacing-sm;
    }

    .user-avatar {
      width: 72rpx;
      height: 72rpx;
      border-radius: 50%;
    }

    .user-info {
      flex: 1;
      margin-left: $spacing-sm;
    }

    .user-name {
      font-size: $font-size-base;
      color: $text-color-primary;
      margin-bottom: $spacing-xs;
    }

    .review-stars {
      .star {
        font-size: $font-size-sm;
        color: #f5a623;
      }
    }

    .review-date {
      font-size: $font-size-sm;
      color: $text-color-secondary;
    }

    .review-content {
      font-size: $font-size-base;
      color: $text-color-regular;
      line-height: $line-height-lg;
      margin-bottom: $spacing-sm;
    }

    .review-dishes {
      display: flex;
      flex-wrap: wrap;
      gap: $spacing-xs;

      .dish-tag {
        font-size: $font-size-xs;
        color: $text-color-secondary;
        background-color: $bg-color-base;
        padding: 6rpx 12rpx;
        border-radius: 6rpx;
      }
    }
  }

  .view-all-reviews {
    text-align: center;
    padding: $spacing-md;
    color: $primary-color;
    font-size: $font-size-base;
  }
}

/* 商家信息 */
.merchant-info-section {
  .info-item {
    @include flex-between;
    padding: $spacing-sm 0;
    border-bottom: 1rpx solid $border-color-light;

    &:last-child {
      border-bottom: none;
    }
  }

  .info-label {
    font-size: $font-size-base;
    color: $text-color-regular;
  }

  .info-value {
    font-size: $font-size-base;
    color: $text-color-primary;
    text-align: right;

    &.phone {
      color: $primary-color;
    }
  }
}

/* 底部操作栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  @include flex-between;
  background-color: $bg-color-white;
  padding: $spacing-md;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
  z-index: $z-index-fixed;
}

.bar-left {
  @include flex-center;
  gap: $spacing-lg;
}

.bar-icon {
  position: relative;
  @include flex-center-column;
  gap: 4rpx;

  .icon {
    font-size: 48rpx;
  }

  .badge {
    position: absolute;
    top: 0;
    right: -8rpx;
    min-width: 32rpx;
    height: 32rpx;
    padding: 0 8rpx;
    background-color: $danger-color;
    color: #fff;
    font-size: $font-size-xs;
    line-height: 32rpx;
    text-align: center;
    border-radius: 16rpx;
  }

  .cart-total {
    font-size: $font-size-xs;
    color: $danger-color;
    font-weight: $font-weight-bold;
  }
}

.bar-right {
  flex: 1;
  margin-left: $spacing-md;
}

.start-order-btn {
  width: 100%;
  height: 72rpx;
  @include flex-center;
  background-color: $primary-color;
  color: #fff;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  border-radius: $border-radius-base;
}
</style>
