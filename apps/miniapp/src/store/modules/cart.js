import { defineStore } from 'pinia'

/**
 * 购物车状态管理
 */
export const useCartStore = defineStore('cart', {
  state: () => ({
    // 购物车列表（按商家分组）（延迟初始化）
    carts: [],

    // 当前选中的商家ID
    currentMerchantId: null,

    // 是否已初始化
    _initialized: false
  }),

  getters: {
    // 获取购物车总数
    totalCount: (state) => {
      return state.carts.reduce((total, cart) => {
        return total + cart.items.reduce((sum, item) => sum + item.quantity, 0)
      }, 0)
    },

    // 获取购物车总价
    totalPrice: (state) => {
      return state.carts.reduce((total, cart) => {
        return total + cart.items.reduce((sum, item) => sum + item.price * item.quantity, 0)
      }, 0)
    },

    // 获取指定商家的购物车
    getCartByMerchant: (state) => (merchantId) => {
      return state.carts.find(cart => cart.merchantId === merchantId)
    },

    // 获取当前商家的购物车
    currentCart: (state) => {
      if (!state.currentMerchantId) return null
      return state.carts.find(cart => cart.merchantId === state.currentMerchantId)
    },

    // 兼容旧页面使用的平铺商品列表
    items: (state) => {
      return state.carts.flatMap((cart) =>
        cart.items.map((item) => ({
          merchantId: cart.merchantId,
          merchantName: cart.merchantName || '',
          dish: {
            id: item.dishId,
            name: item.name,
            price: item.price,
            image: item.image || ''
          },
          quantity: item.quantity,
          spec: item.spec || '',
          remark: item.remark || ''
        }))
      )
    }
  },

  actions: {
    /**
     * 初始化 store - 从本地存储恢复数据
     * 这个方法应该在应用启动时调用
     */
    initialize() {
      if (this._initialized) return

      try {
        // 从本地存储恢复购物车
        const savedCarts = uni.getStorageSync('carts')
        if (savedCarts && Array.isArray(savedCarts)) {
          this.carts = savedCarts
        }

        this._initialized = true
        console.log('✅ Cart store 初始化成功')
      } catch (error) {
        console.error('❌ Cart store 初始化失败:', error)
      }
    },

    /**
     * 设置当前商家
     * @param {number} merchantId - 商家ID
     */
    setCurrentMerchant(merchantId) {
      this.currentMerchantId = merchantId
    },

    /**
     * 添加到购物车
     * @param {Object} item - 菜品信息
     * @param {number} item.merchantId - 商家ID
     * @param {number} item.dishId - 菜品ID
     * @param {string} item.name - 菜品名称
     * @param {number} item.price - 价格
     * @param {string} item.spec - 规格
     * @param {number} item.quantity - 数量
     * @param {Object} item.ingredients - 食材
     * @param {string} item.remark - 备注
     */
    addToCart(item) {
      const normalizedItem = item.dish ? {
        merchantId: item.merchantId,
        merchantName: item.merchantName || item.merchant?.name || '',
        dishId: item.dish.id || item.dishId,
        name: item.dish.name || item.name,
        price: item.dish.price || item.price || 0,
        image: item.dish.image || item.image || '',
        spec: item.spec || '',
        quantity: item.quantity || 1,
        ingredients: item.ingredients || [],
        remark: item.remark || ''
      } : item

      const {
        merchantId,
        merchantName = '',
        dishId,
        name,
        price,
        image = '',
        spec = '',
        quantity = 1,
        ingredients = [],
        remark = ''
      } = normalizedItem

      if (!merchantId || !dishId) {
        console.warn('购物车添加失败，缺少必要参数', normalizedItem)
        return
      }

      // 查找对应商家的购物车
      let cart = this.carts.find(c => c.merchantId === merchantId)

      if (!cart) {
        // 创建新的购物车
        cart = {
          merchantId,
          merchantName,
          items: []
        }
        this.carts.push(cart)
      } else if (merchantName && !cart.merchantName) {
        cart.merchantName = merchantName
      }

      // 查找是否已存在相同菜品和规格
      const existingItem = cart.items.find(
        i => i.dishId === dishId && i.spec === spec
      )

      if (existingItem) {
        // 更新数量
        existingItem.quantity += quantity
        if (image && !existingItem.image) {
          existingItem.image = image
        }
      } else {
        // 添加新菜品
        cart.items.push({
          dishId,
          name,
          price,
          image,
          spec,
          quantity,
          ingredients,
          remark
        })
      }

      this.saveCarts()
    },

    /**
     * 更新购物车项数量
     * @param {number} merchantId - 商家ID
     * @param {number} dishId - 菜品ID
     * @param {string} spec - 规格
     * @param {number} quantity - 数量
     */
    updateQuantity(merchantId, dishId, spec, quantity) {
      if (typeof quantity === 'undefined') {
        quantity = spec
        spec = ''
      }

      const cart = this.carts.find(c => c.merchantId === merchantId)
      if (!cart) return

      const item = cart.items.find(
        i => i.dishId === dishId && (spec ? i.spec === spec : true)
      ) || cart.items.find(i => i.dishId === dishId)

      if (item) {
        if (quantity <= 0) {
          // 移除菜品
          const index = cart.items.indexOf(item)
          cart.items.splice(index, 1)
        } else {
          // 更新数量
          item.quantity = quantity
        }

        // 如果购物车为空，移除购物车
        if (cart.items.length === 0) {
          const index = this.carts.indexOf(cart)
          this.carts.splice(index, 1)
        }

        this.saveCarts()
      }
    },

    removeFromCart(merchantId, dishId, spec = '') {
      const cart = this.carts.find(c => c.merchantId === merchantId)
      if (!cart) return

      const index = cart.items.findIndex(
        item => item.dishId === dishId && (spec ? item.spec === spec : true)
      )

      if (index > -1) {
        cart.items.splice(index, 1)
      }

      if (cart.items.length === 0) {
        const cartIndex = this.carts.findIndex(c => c.merchantId === merchantId)
        if (cartIndex > -1) {
          this.carts.splice(cartIndex, 1)
        }
      }

      this.saveCarts()
    },

    removeItem(merchantId, dishId, spec = '') {
      this.removeFromCart(merchantId, dishId, spec)
    },

    /**
     * 清空购物车
     * @param {number} merchantId - 商家ID（不传则清空所有）
     */
    clearCart(merchantId) {
      if (merchantId) {
        // 清空指定商家的购物车
        const index = this.carts.findIndex(c => c.merchantId === merchantId)
        if (index > -1) {
          this.carts.splice(index, 1)
        }
      } else {
        // 清空所有购物车
        this.carts = []
      }
      this.saveCarts()
    },

    /**
     * 保存购物车到本地存储
     */
    saveCarts() {
      uni.setStorageSync('carts', this.carts)
    },

    /**
     * 从本地存储加载购物车
     */
    loadCarts() {
      const carts = uni.getStorageSync('carts')
      if (carts) {
        this.carts = carts
      }
    }
  }
})
