import { defineStore } from 'pinia'

/**
 * 购物车状态管理
 */
export const useCartStore = defineStore('cart', {
  state: () => ({
    // 购物车列表（按商家分组）
    carts: uni.getStorageSync('carts') || [],

    // 当前选中的商家ID
    currentMerchantId: null
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
    }
  },

  actions: {
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
      const { merchantId, dishId, spec = '' } = item

      // 查找对应商家的购物车
      let cart = this.carts.find(c => c.merchantId === merchantId)

      if (!cart) {
        // 创建新的购物车
        cart = {
          merchantId,
          merchantName: item.merchantName || '',
          items: []
        }
        this.carts.push(cart)
      }

      // 查找是否已存在相同菜品和规格
      const existingItem = cart.items.find(
        i => i.dishId === dishId && i.spec === spec
      )

      if (existingItem) {
        // 更新数量
        existingItem.quantity += item.quantity
      } else {
        // 添加新菜品
        cart.items.push({
          dishId,
          name: item.name,
          price: item.price,
          spec: item.spec,
          quantity: item.quantity,
          ingredients: item.ingredients || [],
          remark: item.remark || ''
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
      const cart = this.carts.find(c => c.merchantId === merchantId)
      if (!cart) return

      const item = cart.items.find(
        i => i.dishId === dishId && i.spec === spec
      )

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
