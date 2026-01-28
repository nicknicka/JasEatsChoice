/**
 * 群订单管理 Composable
 * 负责群订单的创建、加入、购物车管理、商家商品选择等功能
 */
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'
import { useAuthStore } from '@/store/authStore'
import {
  findExistingOrderItem,
  mergeOrderItem,
  calculateOrderTotal,
  validateOrderItems
} from '@/utils/orderHelper'
import { ORDER_STATUS, ORDER_CONFIG } from '@/constants/orderConstants'

export function useGroupOrder({ selectedConversation, chatMessages }) {
  const router = useRouter()
  const authStore = useAuthStore()

  // ========== 状态管理 ==========
  const groupOrders = ref({})
  const orderDrawerVisible = ref(false)
  const loadingDraftOrder = ref(false)

  // 商家选择相关
  const merchantSelectDialogVisible = ref(false)
  const productSelectDialogVisible = ref(false)
  const selectedMerchant = ref(null)
  const orderingMerchant = ref(null)

  // 商品选择相关
  const selectedProducts = ref([])
  const productRemarks = ref({})
  const productSelectedOptionalIngredients = ref({})

  // ========== 商家管理 ==========

  /**
   * 打开商家选择对话框
   */
  const openMerchantSelectDialog = () => {
    if (!selectedConversation.value || !groupOrders.value[selectedConversation.value.id]) {
      ElMessage.error('请先创建群订单')
      return
    }

    if (orderingMerchant.value) {
      selectedMerchant.value = orderingMerchant.value
      productSelectDialogVisible.value = true
    } else {
      merchantSelectDialogVisible.value = true
    }
  }

  /**
   * 选择商家
   */
  const selectMerchant = (merchant) => {
    selectedMerchant.value = merchant
    orderingMerchant.value = merchant
    selectedProducts.value = []
    productRemarks.value = {}
    merchantSelectDialogVisible.value = false

    // 更新群订单信息
    if (selectedConversation.value && groupOrders.value[selectedConversation.value.id]) {
      const currentOrder = groupOrders.value[selectedConversation.value.id]
      currentOrder.merchantId = merchant.id
      currentOrder.merchantName = merchant.name
    }

    // 发送系统消息
    const action = groupOrders.value[selectedConversation.value.id].merchantId ? '更换' : '选择'
    const merchantSelectedMsg = {
      id: chatMessages.value.length + 1,
      sender: '系统',
      content: `已${action}商家：${merchant.name}${action === '更换' ? '，购物车已清空' : '，大家可以开始点餐了'}！`,
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    }
    chatMessages.value.push(merchantSelectedMsg)

    selectedConversation.value.lastMessage = `系统: 已选择商家：${merchant.name}`
    selectedConversation.value.time = merchantSelectedMsg.time

    productSelectDialogVisible.value = true
  }

  /**
   * 更换商家
   */
  const changeMerchant = () => {
    if (!selectedConversation.value) return

    merchantSelectDialogVisible.value = true

    const currentOrder = groupOrders.value[selectedConversation.value.id]
    if (currentOrder) {
      currentOrder.orderItems = []
      currentOrder.totalAmount = 0
    }
  }

  // ========== 商品管理 ==========

  /**
   * 切换商品选择
   */
  const toggleProductSelection = (product) => {
    const index = selectedProducts.value.findIndex((item) => item.id === product.id)
    if (index === -1) {
      productSelectedOptionalIngredients.value[product.id] =
        productSelectedOptionalIngredients.value[product.id] || []
      selectedProducts.value.push({
        ...product,
        quantity: 1,
        remark: productRemarks.value[product.id] || '',
        requiredIngredients: [...product.requiredIngredients],
        selectedOptionalIngredients: productSelectedOptionalIngredients.value[product.id] || []
      })
    } else {
      selectedProducts.value.splice(index, 1)
    }
  }

  /**
   * 更新商品数量
   */
  const updateProductQuantity = (product, change) => {
    const index = selectedProducts.value.findIndex((item) => item.id === product.id)
    if (index !== -1) {
      selectedProducts.value[index].quantity += change
      if (selectedProducts.value[index].quantity <= 0) {
        selectedProducts.value.splice(index, 1)
      }
    }
  }

  /**
   * 更新商品备注
   */
  const updateProductRemark = (productId, remark) => {
    productRemarks.value[productId] = remark
    const index = selectedProducts.value.findIndex((item) => item.id === productId)
    if (index !== -1) {
      selectedProducts.value[index].remark = remark
    }
  }

  /**
   * 更新商品可选食材
   */
  const updateProductOptionalIngredients = (productId, ingredients) => {
    productSelectedOptionalIngredients.value[productId] = ingredients
    const index = selectedProducts.value.findIndex((item) => item.id === productId)
    if (index !== -1) {
      selectedProducts.value[index].selectedOptionalIngredients = ingredients
    }
  }

  /**
   * 单个商品加入购物车
   */
  const addProductToCart = (product) => {
    const selectedProductIndex = selectedProducts.value.findIndex((item) => item.id === product.id)
    if (selectedProductIndex === -1) return

    const customizedProduct = selectedProducts.value[selectedProductIndex]

    if (selectedConversation.value && groupOrders.value[selectedConversation.value.id]) {
      const currentOrder = groupOrders.value[selectedConversation.value.id]

      // 使用工具函数查找已存在项
      const existingIndex = findExistingOrderItem(currentOrder.orderItems, customizedProduct)

      // 使用工具函数合并订单项
      mergeOrderItem(currentOrder.orderItems, customizedProduct, existingIndex)

      // 使用工具函数计算总金额
      currentOrder.totalAmount = calculateOrderTotal(currentOrder.orderItems)

      ElMessage.success('商品已加入购物车')
    }

    clearProductConfiguration(product.id)
  }

  /**
   * 清空商品配置
   */
  const clearProductConfiguration = (productId) => {
    const index = selectedProducts.value.findIndex((item) => item.id === productId)
    if (index !== -1) {
      selectedProducts.value.splice(index, 1)
    }
    productSelectedOptionalIngredients.value[productId] = []
    productRemarks.value[productId] = ''
  }

  /**
   * 确认选择商品（批量加入购物车）
   */
  const confirmProductSelection = () => {
    if (selectedProducts.value.length === 0) {
      ElMessage.error('请至少选择一个商品')
      return
    }

    if (selectedConversation.value && groupOrders.value[selectedConversation.value.id]) {
      const currentOrder = groupOrders.value[selectedConversation.value.id]

      selectedProducts.value.forEach((product) => {
        const existingIndex = findExistingOrderItem(currentOrder.orderItems, product)
        mergeOrderItem(currentOrder.orderItems, product, existingIndex)
      })

      currentOrder.totalAmount = calculateOrderTotal(currentOrder.orderItems)
      ElMessage.success('商品已添加到群订单')
    }

    productSelectDialogVisible.value = false
    selectedProducts.value = []
    selectedMerchant.value = null
  }

  // ========== 订单管理 ==========

  /**
   * 从后端加载草稿订单
   */
  const loadDraftOrder = async (groupId) => {
    if (!authStore.token || loadingDraftOrder.value) {
      return null
    }

    try {
      loadingDraftOrder.value = true

      // 获取当前用户ID
      const decodedToken = authStore.decodedToken || JSON.parse(atob(authStore.token.split('.')[1]))
      const userId = decodedToken.userId

      // 调用后端API获取或创建草稿订单
      const response = await api.get(`/v1/group-orders/groups/${groupId}/draft-order`, {
        params: { initiatorId: userId }
      })

      if (response.data && response.data.success) {
        const draftOrder = response.data.data

        // 转换为前端格式
        const frontendOrder = {
          orderId: draftOrder.id, // 使用后端返回的正式ID
          groupId: draftOrder.groupId,
          groupName: selectedConversation.value?.name || '群订单',
          creator: ORDER_CONFIG.DEFAULT_MEMBER,
          members: [ORDER_CONFIG.DEFAULT_MEMBER],
          orderItems: [],
          totalAmount: 0,
          status: ORDER_STATUS.ACTIVE,
          createTime: draftOrder.createTime || new Date().toISOString(),
          // 保存后端订单信息
          merchantId: draftOrder.merchantId,
          addressId: draftOrder.addressId,
          remark: draftOrder.remark,
          draftStatus: draftOrder.status // -1 表示草稿
        }

        // 保存到本地状态
        groupOrders.value[groupId] = frontendOrder

        return frontendOrder
      }
    } catch (error) {
      console.error('加载草稿订单失败:', error)
      // 静默失败，不影响用户体验
    } finally {
      loadingDraftOrder.value = false
    }

    return null
  }

  /**
   * 创建群订单
   * ⭐ 立即调用后端API创建草稿订单
   */
  const createGroupOrder = async () => {
    if (!selectedConversation.value) {
      ElMessage.error('请先选择一个群聊')
      return
    }

    try {
      // 获取当前用户ID
      const decodedToken = authStore.decodedToken || JSON.parse(atob(authStore.token.split('.')[1]))
      const userId = decodedToken.userId

      // 调用后端API获取或创建草稿订单
      const response = await api.get(`/v1/group-orders/groups/${selectedConversation.value.id}/draft-order`, {
        params: { initiatorId: userId }
      })

      if (response.data && response.data.success) {
        const draftOrder = response.data.data

        // 转换为前端格式
        const order = {
          orderId: draftOrder.id, // ⭐ 使用后端返回的正式ID
          groupId: draftOrder.groupId,
          groupName: selectedConversation.value.name,
          creator: ORDER_CONFIG.DEFAULT_MEMBER,
          members: [ORDER_CONFIG.DEFAULT_MEMBER],
          orderItems: [],
          totalAmount: 0,
          status: ORDER_STATUS.ACTIVE,
          createTime: draftOrder.createTime,
          // 保存后端订单信息
          merchantId: draftOrder.merchantId,
          addressId: draftOrder.addressId,
          remark: draftOrder.remark,
          draftStatus: draftOrder.status // -1 表示草稿
        }

        groupOrders.value[selectedConversation.value.id] = order
        ElMessage.success('群订单已创建')

        const orderMsg = {
          id: chatMessages.value.length + 1,
          sender: '系统',
          content: '我创建了一个群订单，大家可以加入并添加商品',
          time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
        }
        chatMessages.value.push(orderMsg)

        selectedConversation.value.lastMessage = '系统: 我创建了一个群订单'
        selectedConversation.value.time = orderMsg.time
      }
    } catch (error) {
      console.error('创建群订单失败:', error)
      ElMessage.error('创建群订单失败，请稍后重试')
    }
  }

  /**
   * 加入群订单
   */
  const joinGroupOrder = () => {
    if (!selectedConversation.value) {
      ElMessage.error('请先选择一个群聊')
      return
    }

    const conversationOrder = groupOrders.value[selectedConversation.value.id]
    if (!conversationOrder) {
      ElMessage.error('当前群没有订单，请先创建群订单')
      return
    }

    if (conversationOrder.status !== ORDER_STATUS.ACTIVE) {
      ElMessage.error('该群订单已关闭或已支付，无法加入')
      return
    }

    if (!conversationOrder.members.includes(ORDER_CONFIG.DEFAULT_MEMBER)) {
      conversationOrder.members.push(ORDER_CONFIG.DEFAULT_MEMBER)
      ElMessage.success('已加入群订单')

      const joinMsg = {
        id: chatMessages.value.length + 1,
        sender: '系统',
        content: '我加入了群订单',
        time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      }
      chatMessages.value.push(joinMsg)

      selectedConversation.value.lastMessage = '系统: 我加入了群订单'
      selectedConversation.value.lastTime = joinMsg.time
    } else {
      ElMessage.warning('你已经在群订单中了')
    }
  }

  /**
   * 取消群订单（创建者）
   * ⭐ 删除草稿订单，清空本地状态
   */
  const cancelGroupOrder = async () => {
    if (!selectedConversation.value) {
      ElMessage.error('请先选择一个群聊')
      return false
    }

    const currentOrder = groupOrders.value[selectedConversation.value.id]
    if (!currentOrder) {
      ElMessage.error('当前没有群订单')
      return false
    }

    try {
      // 调用后端API删除订单
      const response = await api.delete(`/v1/group-orders/group-orders/${currentOrder.orderId}`)

      if (response.data && response.data.success) {
        // 清空本地状态
        delete groupOrders.value[selectedConversation.value.id]

        // 发送系统消息到群聊
        const cancelMsg = {
          id: Date.now(),
          fromId: authStore.decodedToken?.userId || '',
          toId: currentOrder.groupId,
          sessionType: 'group',
          msgType: 'text',
          content: '我取消了群订单',
          createTime: new Date().toISOString(),
          formattedTime: '刚刚',
          sender: '我',
          avatar: '👤'
        }

        chatMessages.value.push(cancelMsg)
        if (selectedConversation.value) {
          selectedConversation.value.lastMessage = '系统: 我取消了群订单'
          selectedConversation.value.time = cancelMsg.formattedTime
        }

        ElMessage.success('群订单已取消')
        return true
      } else {
        ElMessage.error(response.data?.message || '取消订单失败')
        return false
      }
    } catch (error) {
      console.error('取消群订单失败:', error)
      ElMessage.error('取消订单失败，请稍后重试')
      return false
    }
  }

  /**
   * 跳转到订单确认页
   */
  const goToOrderConfirmation = () => {
    if (!selectedConversation.value || !groupOrders.value[selectedConversation.value.id]) {
      ElMessage.error('当前没有群订单')
      return
    }

    const currentOrder = groupOrders.value[selectedConversation.value.id]

    if (!validateOrderItems(currentOrder.orderItems)) {
      ElMessage.warning('购物车为空，无法进行订单确认')
      return
    }

    const pendingOrder = {
      cartItems: currentOrder.orderItems.map((item) => ({
        ...item,
        price: item.price || 22.2,
        remark: item.remark || ''
      })),
      totalAmount: currentOrder.totalAmount,
      fromChat: true,
      groupName: currentOrder.groupName,
      orderId: currentOrder.orderId,
      creator: currentOrder.creator,
      members: currentOrder.members
    }

    sessionStorage.setItem('pendingOrder', JSON.stringify(pendingOrder))
    router.push('/user/home/order-confirmation')
  }

  /**
   * 获取当前群订单
   */
  const currentGroupOrder = computed(() => {
    if (!selectedConversation.value) return null
    return groupOrders.value[selectedConversation.value.id] || null
  })

  /**
   * 检查是否有群订单
   */
  const hasGroupOrder = computed(() => {
    return currentGroupOrder.value !== null
  })

  return {
    // 状态
    groupOrders,
    orderDrawerVisible,
    loadingDraftOrder,
    merchantSelectDialogVisible,
    productSelectDialogVisible,
    selectedMerchant,
    orderingMerchant,
    selectedProducts,
    productRemarks,
    productSelectedOptionalIngredients,

    // 计算属性
    currentGroupOrder,
    hasGroupOrder,

    // 商家管理
    openMerchantSelectDialog,
    selectMerchant,
    changeMerchant,

    // 商品管理
    toggleProductSelection,
    updateProductQuantity,
    updateProductRemark,
    updateProductOptionalIngredients,
    addProductToCart,
    confirmProductSelection,

    // 订单管理
    loadDraftOrder,
    createGroupOrder,
    joinGroupOrder,
    cancelGroupOrder,
    goToOrderConfirmation
  }
}
