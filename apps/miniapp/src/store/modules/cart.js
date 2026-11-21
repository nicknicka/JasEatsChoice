import { defineStore } from 'pinia'
import { cartApi } from '@/api/modules/cart'

/**
 * 购物车状态。
 *
 * 真相在后端（#603 起 /v1/cart）。本 store 只做两件事：
 *   1. 缓存接口返回的分组数据，供各页面读 count / totalPrice / items；
 *   2. 写操作先调接口，成功后再用接口结果刷新，不再本地自行加加减减。
 *
 * 本地存储仅作离线展示缓存：拿不到接口时显示上次结果，但绝不把本地数据当成已加入成功。
 */
const STORAGE_KEY = 'carts-cache'

export const useCartStore = defineStore('cart', {
  state: () => ({
    // 购物车列表（按商家分组），结构同 GET /v1/cart/{userId}
    carts: [],

    // 当前选中的商家ID
    currentMerchantId: null,

    // 最近一次拉取是否成功
    loaded: false
  }),

  getters: {
    // 购物车商品总件数
    totalCount: (state) => {
      return state.carts.reduce((total, cart) => {
        return total + (cart.items || []).reduce((sum, item) => sum + (item.quantity || 0), 0)
      }, 0)
    },

    // 购物车总价（只算选中项，与结算口径一致）
    totalPrice: (state) => {
      return state.carts.reduce((total, cart) => {
        return total + (cart.items || []).reduce(
          (sum, item) => sum + (item.selected === false ? 0 : (item.price || 0) * (item.quantity || 0)),
          0
        )
      }, 0)
    },

    // 指定商家的购物车
    getCartByMerchant: (state) => (merchantId) => {
      return state.carts.find(cart => cart.merchantId === merchantId)
    },

    // 当前商家的购物车
    currentCart: (state) => {
      if (!state.currentMerchantId) return null
      return state.carts.find(cart => cart.merchantId === state.currentMerchantId)
    },

    // 平铺的商品列表（兼容旧页面）
    items: (state) => {
      return state.carts.flatMap((cart) =>
        (cart.items || []).map((item) => ({
          id: item.id,
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
          remark: item.remark || '',
          selected: item.selected !== false
        }))
      )
    }
  },

  actions: {
    /**
     * 从后端拉取购物车。所有写操作之后都调用它来同步真实状态。
     * @returns {Promise<boolean>} 是否拉取成功
     */
    async fetchCart(userId) {
      const uid = userId || uni.getStorageSync('userId')
      if (!uid) {
        this.carts = []
        return false
      }

      try {
        const res = await cartApi.getCart(uid)
        if (res && res.success) {
          this.carts = res.data || []
          this.loaded = true
          // 仅作离线展示缓存，不作为写入依据
          uni.setStorageSync(STORAGE_KEY, this.carts)
          return true
        }
        return false
      } catch (error) {
        console.error('拉取购物车失败:', error)
        return false
      }
    },

    /**
     * 应用启动时恢复上次缓存，避免首屏空白；真实数据以 fetchCart 为准。
     */
    initialize() {
      try {
        const cached = uni.getStorageSync(STORAGE_KEY)
        if (Array.isArray(cached)) {
          this.carts = cached
        }
      } catch (error) {
        console.error('恢复购物车缓存失败:', error)
      }
    },

    setCurrentMerchant(merchantId) {
      this.currentMerchantId = merchantId
    },

    /**
     * 加入购物车：调接口，成功后刷新。
     * 失败直接返回 false，由调用方如实提示，不本地假加。
     */
    async addToCart(item) {
      const payload = item.dish
        ? {
            dishId: item.dish.id || item.dishId,
            merchantId: item.merchantId,
            quantity: item.quantity || 1,
            spec: item.spec || '',
            remark: item.remark || ''
          }
        : {
            dishId: item.dishId,
            merchantId: item.merchantId,
            quantity: item.quantity || 1,
            spec: item.spec || '',
            remark: item.remark || ''
          }

      if (!payload.dishId) {
        console.warn('加入购物车失败，缺少 dishId', payload)
        return false
      }

      try {
        const res = await cartApi.add(payload)
        if (!res || !res.success) {
          return false
        }
        await this.fetchCart()
        return true
      } catch (error) {
        console.error('加入购物车失败:', error)
        return false
      }
    },

    /**
     * 修改某个购物车项的数量。数量为 0 时后端会直接删除该项。
     */
    async updateQuantity(itemId, quantity) {
      if (!itemId) return false
      try {
        const res = await cartApi.update(itemId, { quantity })
        if (!res || !res.success) return false
        await this.fetchCart()
        return true
      } catch (error) {
        console.error('更新购物车失败:', error)
        return false
      }
    },

    /**
     * 删除单个购物车项。
     */
    async removeItem(itemId) {
      if (!itemId) return false
      try {
        const res = await cartApi.delete(itemId)
        if (!res || !res.success) return false
        await this.fetchCart()
        return true
      } catch (error) {
        console.error('删除购物车项失败:', error)
        return false
      }
    },

    /**
     * 批量删除。
     */
    async removeItems(itemIds) {
      if (!itemIds || itemIds.length === 0) return false
      try {
        const res = await cartApi.batchDelete({ cartIds: itemIds })
        if (!res || !res.success) return false
        await this.fetchCart()
        return true
      } catch (error) {
        console.error('批量删除购物车项失败:', error)
        return false
      }
    },

    /**
     * 选中 / 取消选中。
     */
    async selectItem(itemId, selected) {
      if (!itemId) return false
      try {
        const res = await cartApi.select(itemId, { selected })
        if (!res || !res.success) return false
        await this.fetchCart()
        return true
      } catch (error) {
        console.error('更新选中状态失败:', error)
        return false
      }
    },

    /**
     * 全选 / 取消全选，可按商家收窄。
     */
    async selectAll(selected, merchantId) {
      try {
        const res = await cartApi.selectAll({ selected, merchantId })
        if (!res || !res.success) return false
        await this.fetchCart()
        return true
      } catch (error) {
        console.error('全选失败:', error)
        return false
      }
    },

    /**
     * 清空购物车，可限定商家。
     */
    async clearCart(merchantId) {
      const uid = uni.getStorageSync('userId')
      if (!uid) return false
      try {
        const res = await cartApi.clearCart(uid, merchantId)
        if (!res || !res.success) return false
        await this.fetchCart()
        return true
      } catch (error) {
        console.error('清空购物车失败:', error)
        return false
      }
    }
  }
})
