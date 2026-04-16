/**
 * 拼单相关 API
 * 对接后端 GroupOrderController，未落地能力使用本地兼容
 */
import { get, post, put, del } from '@/utils/request'
import { dishApi } from './dish'

const GROUP_ORDER_CACHE_KEY = 'groupOrderCache'
const GROUP_ORDER_SELECTION_KEY = 'groupOrderSelections'

const STATUS_TO_CODE = {
  draft: -1,
  pending: 0,
  in_progress: 1,
  completed: 5,
  cancelled: 6
}

const getCurrentUserId = () => {
  const userInfo = uni.getStorageSync('userInfo') || {}
  return userInfo.userId || userInfo.id || uni.getStorageSync('userId') || ''
}

const buildQueryUrl = (url, params = {}) => {
  const query = Object.entries(params)
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join('&')
  return query ? `${url}?${query}` : url
}

const getOrderCache = () => uni.getStorageSync(GROUP_ORDER_CACHE_KEY) || {}

const saveOrderCache = (cache) => {
  uni.setStorageSync(GROUP_ORDER_CACHE_KEY, cache)
}

const mergeOrderCache = (orderId, patch = {}) => {
  const cache = getOrderCache()
  cache[orderId] = {
    ...(cache[orderId] || {}),
    ...patch
  }
  saveOrderCache(cache)
  return cache[orderId]
}

const getSelectionCache = () => uni.getStorageSync(GROUP_ORDER_SELECTION_KEY) || {}

const saveSelectionCache = (cache) => {
  uni.setStorageSync(GROUP_ORDER_SELECTION_KEY, cache)
}

const getSelectionKey = (orderId, userId) => `${orderId}:${userId}`

const getOrderCode = (orderId = '') => `${orderId}`.replace(/\D/g, '').slice(-6).padStart(6, '0')

const mapStatusCodeToText = (status) => {
  switch (status) {
    case -1:
    case 0:
      return 'pending'
    case 1:
    case 2:
    case 3:
    case 4:
      return 'in_progress'
    case 5:
      return 'completed'
    case 6:
      return 'cancelled'
    default:
      return 'pending'
  }
}

const mapStatusTextToCode = (status) => {
  if (typeof status === 'number') {
    return status
  }
  return STATUS_TO_CODE[status] ?? undefined
}

const groupDishItems = (dishItems = []) => {
  const dishMap = {}
  const memberMap = {}

  dishItems.forEach((item) => {
    const dishId = item.dishId || ''
    const userId = item.userId || ''

    if (!dishMap[dishId]) {
      dishMap[dishId] = {
        id: dishId,
        dishId,
        name: item.dishName || `菜品 ${dishId.slice(-4) || dishId}`,
        image: item.image || 'https://via.placeholder.com/100',
        specification: item.customization || '',
        totalQuantity: 0
      }
    }
    dishMap[dishId].totalQuantity += Number(item.quantity || 0)

    if (!memberMap[userId]) {
      memberMap[userId] = {
        id: userId,
        userId,
        name: item.userName || `用户${userId.slice(-4) || ''}`,
        avatar: item.avatar || 'https://via.placeholder.com/100',
        paid: false,
        totalAmount: '0.00',
        dishes: []
      }
    }
    memberMap[userId].dishes.push({
      dishId,
      name: dishMap[dishId].name,
      quantity: Number(item.quantity || 0)
    })
  })

  return {
    dishes: Object.values(dishMap),
    members: Object.values(memberMap)
  }
}

const normalizeDetailData = (rawData = {}, orderId) => {
  const cached = getOrderCache()[orderId] || {}
  const groupOrder = rawData.groupOrder || rawData || {}
  const rawDishItems = rawData.dishItems || cached.dishItems || []
  const grouped = groupDishItems(rawDishItems)
  const statusText = cached.status || mapStatusCodeToText(groupOrder.status)
  const orderCode = cached.orderCode || getOrderCode(orderId || groupOrder.id)
  const selected = getSelectionCache()[getSelectionKey(orderId || groupOrder.id, getCurrentUserId())] || []

  let myOrder = null
  if (selected.length > 0) {
    const totalAmount = selected.reduce((sum, item) => sum + (Number(item.price || 0) * Number(item.quantity || 0)), 0)
    myOrder = {
      userId: getCurrentUserId(),
      totalAmount: totalAmount.toFixed(2),
      paid: false,
      dishes: selected.map(item => ({
        dishId: item.dishId,
        name: item.name,
        quantity: item.quantity
      }))
    }
  }

  const members = [...grouped.members]
  if (myOrder && !members.find(item => item.userId === myOrder.userId)) {
    members.push({
      id: myOrder.userId,
      userId: myOrder.userId,
      name: cached.creatorName || `用户${myOrder.userId.slice(-4) || ''}`,
      avatar: 'https://via.placeholder.com/100',
      paid: false,
      totalAmount: myOrder.totalAmount,
      dishes: myOrder.dishes
    })
  }

  return {
    id: groupOrder.id || orderId,
    orderId: groupOrder.id || orderId,
    name: cached.name || `群订单 ${orderCode}`,
    orderCode,
    status: statusText,
    merchantId: groupOrder.merchantId || cached.merchantId || '',
    merchantName: cached.merchantName || groupOrder.merchantId || '',
    merchantAvatar: cached.merchantAvatar || 'https://via.placeholder.com/100',
    creatorId: groupOrder.initiatorId || cached.creatorId || '',
    creatorName: cached.creatorName || '',
    groupId: groupOrder.groupId || cached.groupId || '',
    currentCount: groupOrder.currentCount || cached.currentCount || members.length || (groupOrder.initiatorId || cached.creatorId ? 1 : 0),
    maxParticipants: cached.maxParticipants || members.length,
    deadline: cached.deadline || groupOrder.updateTime || groupOrder.createTime || '',
    deliveryAddress: cached.deliveryAddress || groupOrder.addressId || '',
    addressId: groupOrder.addressId || cached.addressId || '',
    remark: groupOrder.remark || cached.remark || '',
    totalAmount: Number(groupOrder.totalAmount || cached.totalAmount || 0).toFixed(2),
    createTime: groupOrder.createTime || cached.createTime || '',
    members,
    dishes: grouped.dishes,
    dishItems: rawDishItems,
    completed: statusText === 'completed' || statusText === 'cancelled'
  }
}

const normalizeCreateResponse = (response, payload) => {
  const orderId = typeof response?.data === 'string' ? response.data : (response?.data?.id || response?.data?.orderId)
  if (!orderId) {
    return response
  }

  mergeOrderCache(orderId, {
    id: orderId,
    groupId: payload.groupId,
    creatorId: payload.initiatorId,
    creatorName: payload.creatorName || '',
    merchantId: payload.merchantId || '',
    merchantName: payload.merchantName || '',
    merchantAvatar: payload.merchantAvatar || '',
    name: payload.name || `群订单 ${getOrderCode(orderId)}`,
    orderCode: payload.orderCode || getOrderCode(orderId),
    maxParticipants: payload.maxParticipants || payload.targetPeople || 0,
    deadline: payload.deadline || '',
    deliveryAddress: payload.deliveryAddress || '',
    addressId: payload.addressId || '',
    remark: payload.remark || '',
    dishItems: payload.dishItems || [],
    status: 'pending',
    currentCount: 1,
    createTime: new Date().toISOString()
  })

  return {
    ...response,
    data: {
      id: orderId,
      orderId,
      orderCode: getOrderCode(orderId)
    }
  }
}

const normalizeListItem = (order = {}) => {
  const cached = getOrderCache()[order.id] || {}
  const statusText = cached.status || mapStatusCodeToText(order.status)
  return {
    id: order.id,
    name: cached.name || `群订单 ${getOrderCode(order.id)}`,
    orderCode: cached.orderCode || getOrderCode(order.id),
    status: statusText,
    merchantId: order.merchantId || cached.merchantId || '',
    merchantName: cached.merchantName || order.merchantId || '',
    merchantAvatar: cached.merchantAvatar || 'https://via.placeholder.com/100',
    creatorId: order.initiatorId || cached.creatorId || '',
    creatorName: cached.creatorName || '',
    currentCount: order.currentCount || cached.currentCount || (order.initiatorId || cached.creatorId ? 1 : 0),
    maxParticipants: order.maxParticipants || cached.maxParticipants || 0,
    deadline: cached.deadline || order.updateTime || order.createTime || '',
    members: cached.members || []
  }
}

const unsupported = (message) => Promise.reject(new Error(message))

export const groupOrderApi = {
  /**
   * 创建拼单
   * POST /v1/group-orders/group-orders
   */
  create: async (data = {}) => {
    const initiatorId = data.initiatorId || data.creatorId || data.userId || getCurrentUserId()
    const payload = {
      initiatorId,
      groupId: data.groupId,
      merchantId: data.merchantId,
      addressId: data.addressId,
      remark: data.remark || '',
      dishItems: data.dishItems || [],
      name: data.name,
      maxParticipants: data.maxParticipants,
      deadline: data.deadline,
      deliveryAddress: data.deliveryAddress,
      merchantName: data.merchantName,
      merchantAvatar: data.merchantAvatar,
      creatorName: data.creatorName
    }

    if (!payload.groupId) {
      throw new Error('后端创建拼单必须提供群ID（groupId）')
    }

    const response = await post('/v1/group-orders/group-orders', payload)
    return normalizeCreateResponse(response, payload)
  },

  createGroupOrder: (data) => groupOrderApi.create(data),

  /**
   * 获取拼单列表
   * 当前后端仅支持按群获取；无 groupId 时回退本地缓存
   */
  getList: async (params = {}) => {
    const statusCode = mapStatusTextToCode(params.status)

    if (!params.groupId && params.userId) {
      const response = await get(`/v1/group-orders/users/${params.userId}/orders`, {
        status: statusCode,
        page: params.page || 1,
        size: params.size || 10
      })

      return {
        ...response,
        data: Array.isArray(response?.data) ? response.data.map(normalizeListItem) : []
      }
    }

    if (!params.groupId) {
      const cache = Object.values(getOrderCache())
        .filter(item => !params.userId || item.creatorId === params.userId)
        .filter(item => !params.status || item.status === params.status)
        .map(item => ({
          id: item.id,
          initiatorId: item.creatorId,
          merchantId: item.merchantId,
          status: mapStatusTextToCode(item.status) ?? 0,
          createTime: item.createTime,
          updateTime: item.deadline
        }))

      return {
        code: 200,
        success: true,
        data: cache.map(normalizeListItem)
      }
    }

    const response = await get(`/v1/group-orders/groups/${params.groupId}/orders`, {
      status: statusCode,
      page: params.page || 1,
      size: params.size || 10
    })

    return {
      ...response,
      data: Array.isArray(response?.data) ? response.data.map(normalizeListItem) : []
    }
  },

  getGroupOrders: (params) => groupOrderApi.getList(params),

  /**
   * 获取拼单详情
   * GET /v1/group-orders/group-orders/{groupOrderId}
   */
  getDetail: async (orderId) => {
    try {
      const response = await get(`/v1/group-orders/group-orders/${orderId}`)
      const data = normalizeDetailData(response?.data || {}, orderId)
      mergeOrderCache(orderId, {
        ...getOrderCache()[orderId],
        ...data
      })
      return {
        ...response,
        data
      }
    } catch (error) {
      const cached = getOrderCache()[orderId]
      if (!cached) {
        throw error
      }
      return {
        code: 200,
        success: true,
        data: normalizeDetailData(cached, orderId)
      }
    }
  },

  getGroupOrder: (id) => groupOrderApi.getDetail(id),

  /**
   * 取消拼单
   * DELETE /v1/group-orders/group-orders/{groupOrderId}
   */
  cancel: async (orderId) => {
    const response = await del(`/v1/group-orders/group-orders/${orderId}`)
    const cached = getOrderCache()[orderId]
    if (cached) {
      mergeOrderCache(orderId, { status: 'cancelled' })
    }
    return response
  },

  delete: (orderId) => groupOrderApi.cancel(orderId),
  deleteGroupOrder: (orderId) => groupOrderApi.cancel(orderId),

  /**
   * 更新拼单状态
   */
  updateStatus: async (orderId, data = {}) => {
    const payload = {
      status: mapStatusTextToCode(data.status),
      totalAmount: data.totalAmount
    }
    const response = await put(`/v1/group-orders/group-orders/${orderId}/status`, payload)
    if (data.status) {
      mergeOrderCache(orderId, { status: data.status })
    }
    return response
  },

  /**
   * 获取邀请码二维码
   */
  getQRCode: (orderId) => get(`/v1/group-orders/group-orders/${orderId}/qrcode`),

  /**
   * 获取可选菜品
   * 用订单详情中的 merchantId 调真实菜品接口
   */
  getAvailableDishes: async (orderId, params = {}) => {
    const detail = await groupOrderApi.getDetail(orderId)
    const merchantId = detail?.data?.merchantId
    if (!merchantId) {
      return {
        code: 200,
        success: true,
        data: []
      }
    }

    const response = await dishApi.getList({
      merchantId,
      category: params.category === 'all' ? undefined : params.category,
      keyword: params.keyword
    })

    const list = Array.isArray(response?.data) ? response.data : (Array.isArray(response) ? response : [])
    const page = Number(params.page || 1)
    const size = Number(params.size || 20)
    const start = (page - 1) * size

    return {
      code: 200,
      success: true,
      data: list.slice(start, start + size)
    }
  },

  /**
   * 保存用户选菜
   */
  saveSelections: async (orderId, data = {}) => {
    const userId = data.userId || getCurrentUserId()
    const response = await post(`/v1/group-orders/group-orders/${orderId}/selections`, {
      userId,
      dishes: data.dishes || []
    })

    const normalizedSelections = Array.isArray(response?.data)
      ? response.data.map(item => ({
        dishId: item.dishId,
        quantity: Number(item.quantity || 0),
        specification: item.specification || item.customization || '',
        name: item.name || item.dishName || `菜品 ${`${item.dishId}`.slice(-4)}`,
        image: item.image || 'https://via.placeholder.com/100',
        price: Number(item.price || 0)
      }))
      : []

    const selectionCache = getSelectionCache()
    selectionCache[getSelectionKey(orderId, userId)] = normalizedSelections
    saveSelectionCache(selectionCache)

    return {
      ...response,
      data: normalizedSelections
    }
  },

  /**
   * 获取用户已选菜品
   */
  getUserSelections: async (orderId, userId = getCurrentUserId()) => {
    const response = await get(`/v1/group-orders/group-orders/${orderId}/selections/${userId}`)
    const normalizedSelections = Array.isArray(response?.data)
      ? response.data.map(item => ({
        dishId: item.dishId,
        quantity: Number(item.quantity || 0),
        specification: item.specification || item.customization || '',
        name: item.name || item.dishName || `菜品 ${`${item.dishId}`.slice(-4)}`,
        image: item.image || 'https://via.placeholder.com/100',
        price: Number(item.price || 0)
      }))
      : []

    const selectionCache = getSelectionCache()
    selectionCache[getSelectionKey(orderId, userId)] = normalizedSelections
    saveSelectionCache(selectionCache)

    return {
      ...response,
      data: normalizedSelections
    }
  },

  /**
   * 获取结算信息
   */
  getSettlement: async (orderId, userId = getCurrentUserId()) => get(
    `/v1/group-orders/group-orders/${orderId}/settlement`,
    { userId }
  ),

  pay: () => unsupported('后端暂未提供群订单支付接口'),
  payOrder: () => unsupported('后端暂未提供群订单支付接口'),
  payMember: () => unsupported('后端暂未提供群订单成员支付接口'),
  payMemberOrder: () => unsupported('后端暂未提供群订单成员支付接口'),
  joinByCode: async (data = {}) => post('/v1/group-orders/join', data),
  join: () => unsupported('后端暂未提供加入拼单接口'),
  joinGroupOrder: () => unsupported('后端暂未提供加入拼单接口'),
  leave: () => unsupported('后端暂未提供退出拼单接口'),
  leaveGroupOrder: () => unsupported('后端暂未提供退出拼单接口'),
  quit: () => unsupported('后端暂未提供退出拼单接口'),
  update: () => unsupported('后端暂未提供更新拼单基础信息接口'),
  updateGroupOrder: () => unsupported('后端暂未提供更新拼单基础信息接口'),
  getMembers: async (orderId) => {
    const detail = await groupOrderApi.getDetail(orderId)
    return {
      code: 200,
      success: true,
      data: detail?.data?.members || []
    }
  },
  invite: () => unsupported('后端暂未提供拼单邀请接口'),
  confirm: () => unsupported('后端暂未提供拼单确认接口'),
  getOrders: (params = {}) => groupOrderApi.getList(params),
  share: async (orderId) => ({
    code: 200,
    success: true,
    data: {
      orderId,
      orderCode: getOrderCode(orderId)
    }
  }),
  getStatistics: async (params = {}) => ({
    code: 200,
    success: true,
    data: {
      groupOrderId: params.groupOrderId || '',
      currentCount: 0,
      paidCount: 0
    }
  }),
  getUserGroupOrders: (userId, params = {}) => groupOrderApi.getList({ ...params, userId })
}

export default groupOrderApi
