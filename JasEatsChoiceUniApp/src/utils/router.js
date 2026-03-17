/**
 * 路由工具类
 */
export const router = {
  /**
   * 跳转到登录页
   */
  toLogin() {
    uni.reLaunch({
      url: '/pages/login/index'
    })
  },

  /**
   * 跳转到用户首页
   */
  toUserHome() {
    uni.switchTab({
      url: '/pages/index/index'
    })
  },

  /**
   * 跳转到商家首页
   */
  toMerchantHome() {
    // TODO: 商家端TabBar配置后实现
    uni.switchTab({
      url: '/pages-merchant/home/index'
    })
  },

  /**
   * 跳转到菜品详情
   * @param {number} dishId - 菜品ID
   */
  toDishDetail(dishId) {
    uni.navigateTo({
      url: `/pages-user/dish/detail?id=${dishId}`
    })
  },

  /**
   * 跳转到商家详情
   * @param {number} merchantId - 商家ID
   */
  toMerchantDetail(merchantId) {
    uni.navigateTo({
      url: `/pages-user/home/merchant-detail?id=${merchantId}`
    })
  },

  /**
   * 跳转到订单详情
   * @param {number} orderId - 订单ID
   */
  toOrderDetail(orderId) {
    uni.navigateTo({
      url: `/pages-user/order/detail?id=${orderId}`
    })
  },

  /**
   * 跳转到订单确认
   */
  toOrderConfirm() {
    uni.navigateTo({
      url: '/pages-user/order/confirm'
    })
  },

  /**
   * 跳转到聊天
   * @param {number} userId - 用户ID
   */
  toChat(userId) {
    uni.navigateTo({
      url: `/pages-common/chat/chat-room?userId=${userId}`
    })
  },

  /**
   * 返回上一页
   * @param {number} delta - 返回层数
   */
  back(delta = 1) {
    const pages = getCurrentPages()
    if (pages.length > delta) {
      uni.navigateBack({ delta })
    } else {
      // 如果没有上一页，返回首页
      this.toUserHome()
    }
  },

  /**
   * 返回上一页或首页
   */
  backOrHome() {
    const pages = getCurrentPages()
    if (pages.length > 1) {
      uni.navigateBack()
    } else {
      this.toUserHome()
    }
  }
}
