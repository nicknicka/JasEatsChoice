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

const hasValue = (value) => value !== undefined && value !== null && value !== ''

const pickFirst = (...values) => {
  for (const value of values) {
    if (hasValue(value)) {
      return value
    }
  }
  return undefined
}

const pickNumber = (...values) => {
  for (const value of values) {
    if (!hasValue(value)) {
      continue
    }
    const parsed = Number(value)
    if (!Number.isNaN(parsed)) {
      return parsed
    }
  }
  return undefined
}

const pickBoolean = (...values) => {
  for (const value of values) {
    if (typeof value === 'boolean') {
      return value
    }
    if (value === 1 || value === 0) {
      return Boolean(value)
    }
    if (value === 'true' || value === 'false') {
      return value === 'true'
    }
  }
  return undefined
}

const formatAmount = (value = 0) => Number(value || 0).toFixed(2)

const groupDishItems = (dishItems = []) => {
  const dishMap = {}
  const memberMap = {}

  dishItems.forEach((item) => {
    const dishId = item.dishId || ''
    const userId = item.userId || ''
    const quantity = Number(item.quantity || 0)
    const price = Number(item.price || 0)
    const lineAmount = Number(item.lineAmount || (price * quantity))
    const paid = Boolean(item.paid)

    if (!dishMap[dishId]) {
      dishMap[dishId] = {
        id: dishId,
        dishId,
        name: item.dishName || `菜品 ${dishId.slice(-4) || dishId}`,
        image: item.image || 'https://via.placeholder.com/100',
        specification: item.customization || '',
        totalQuantity: 0,
        amount: 0,
        participantIds: new Set()
      }
    }
    dishMap[dishId].totalQuantity += quantity
    dishMap[dishId].amount += lineAmount
    if (userId) {
      dishMap[dishId].participantIds.add(userId)
    }

    if (!memberMap[userId]) {
      memberMap[userId] = {
        id: userId,
        userId,
        name: item.userName || `用户${userId.slice(-4) || ''}`,
        avatar: item.avatar || 'https://via.placeholder.com/100',
        paid,
        totalAmount: '0.00',
        dishes: []
      }
    }
    memberMap[userId].paid = memberMap[userId].paid || paid
    memberMap[userId].totalAmount = (Number(memberMap[userId].totalAmount || 0) + lineAmount).toFixed(2)
    memberMap[userId].dishes.push({
      dishId,
      name: dishMap[dishId].name,
      quantity,
      price: price.toFixed(2),
      spec: item.customization || item.specification || ''
    })
  })

  return {
    dishes: Object.values(dishMap).map(item => ({
      id: item.id,
      dishId: item.dishId,
      name: item.name,
      image: item.image,
      specification: item.specification,
      totalQuantity: item.totalQuantity,
      amount: item.amount.toFixed(2),
      participantCount: item.participantIds.size
    })),
    members: Object.values(memberMap)
  }
}

const normalizeMemberDish = (dish = {}) => ({
  dishId: pickFirst(dish.dishId, dish.id, ''),
  name: pickFirst(dish.name, dish.dishName, '未命名菜品'),
  quantity: pickNumber(dish.quantity, dish.count, 0) || 0,
  price: formatAmount(pickFirst(dish.price, 0)),
  spec: pickFirst(dish.spec, dish.specification, dish.customization, '')
})

const normalizeParticipantDish = (dish = {}) => ({
  dishId: pickFirst(dish.dishId, dish.id, ''),
  dishName: pickFirst(dish.dishName, dish.name, '未命名菜品'),
  spec: pickFirst(dish.spec, dish.specification, dish.customization, ''),
  count: pickNumber(dish.count, dish.quantity, 0) || 0,
  price: formatAmount(pickFirst(dish.price, 0))
})

const normalizeMember = (member = {}) => {
  const userId = pickFirst(member.userId, member.id, '')
  return {
    id: userId,
    userId,
    name: pickFirst(member.name, member.nickname, member.userName, `用户${`${userId}`.slice(-4) || ''}`),
    avatar: pickFirst(member.avatar, 'https://via.placeholder.com/100'),
    paid: pickBoolean(member.paid, member.orderStatus === 'paid') ?? false,
    totalAmount: formatAmount(pickFirst(member.totalAmount, member.amount, 0)),
    dishes: Array.isArray(member.dishes) ? member.dishes.map(normalizeMemberDish) : []
  }
}

const normalizeParticipant = (participant = {}) => {
  const userId = pickFirst(participant.userId, participant.id, '')
  const dishes = Array.isArray(participant.dishes) ? participant.dishes.map(normalizeParticipantDish) : []
  return {
    userId,
    nickname: pickFirst(participant.nickname, participant.userName, participant.name, `用户${`${userId}`.slice(-4) || ''}`),
    userName: pickFirst(participant.userName, participant.nickname, participant.name, `用户${`${userId}`.slice(-4) || ''}`),
    avatar: pickFirst(participant.avatar, 'https://via.placeholder.com/100'),
    dishCount: pickNumber(participant.dishCount, dishes.length, 0) || 0,
    amount: formatAmount(pickFirst(participant.amount, participant.totalAmount, 0)),
    paid: pickBoolean(participant.paid, participant.orderStatus === 'paid') ?? false,
    dishes
  }
}

const buildMembersFromParticipants = (participants = []) => participants.map(item => ({
  id: item.userId,
  userId: item.userId,
  name: pickFirst(item.nickname, item.userName, `用户${`${item.userId}`.slice(-4) || ''}`),
  avatar: item.avatar || 'https://via.placeholder.com/100',
  paid: Boolean(item.paid),
  totalAmount: formatAmount(item.amount),
  dishes: (item.dishes || []).map(dish => ({
    dishId: dish.dishId,
    name: dish.dishName,
    quantity: pickNumber(dish.count, dish.quantity, 0) || 0,
    price: formatAmount(dish.price),
    spec: pickFirst(dish.spec, '')
  }))
}))

const buildParticipantsFromMembers = (members = []) => members.map(member => ({
  userId: member.userId,
  nickname: member.name,
  userName: member.name,
  avatar: member.avatar,
  dishCount: (member.dishes || []).length,
  amount: formatAmount(member.totalAmount),
  paid: Boolean(member.paid),
  dishes: (member.dishes || []).map(dish => ({
    dishId: dish.dishId,
    dishName: dish.name,
    spec: pickFirst(dish.spec, ''),
    count: pickNumber(dish.quantity, dish.count, 0) || 0,
    price: formatAmount(dish.price)
  }))
}))

const normalizeDishSummary = (dish = {}) => ({
  dishId: pickFirst(dish.dishId, dish.id, ''),
  dishName: pickFirst(dish.dishName, dish.name, '未命名菜品'),
  dishImage: pickFirst(dish.dishImage, dish.image, 'https://via.placeholder.com/100'),
  count: pickNumber(dish.count, dish.totalQuantity, 0) || 0,
  participantCount: pickNumber(dish.participantCount, 0) || 0,
  amount: formatAmount(pickFirst(dish.amount, 0))
})

const normalizeDishDetail = (dish = {}) => ({
  id: pickFirst(dish.id, dish.dishId, ''),
  dishId: pickFirst(dish.dishId, dish.id, ''),
  name: pickFirst(dish.name, dish.dishName, '未命名菜品'),
  image: pickFirst(dish.image, dish.dishImage, 'https://via.placeholder.com/100'),
  specification: pickFirst(dish.specification, dish.spec, ''),
  totalQuantity: pickNumber(dish.totalQuantity, dish.count, 0) || 0,
  amount: formatAmount(pickFirst(dish.amount, 0)),
  participantCount: pickNumber(dish.participantCount, 0) || 0
})

const normalizeDetailData = (rawData = {}, orderId) => {
  const cached = getOrderCache()[orderId] || {}
  const groupOrder = rawData.groupOrder || rawData || {}
  const rawDishItems = Array.isArray(rawData.dishItems)
    ? rawData.dishItems
    : (Array.isArray(groupOrder.dishItems) ? groupOrder.dishItems : (cached.dishItems || []))
  const grouped = groupDishItems(rawDishItems)
  const currentUserId = getCurrentUserId()
  const rawStatus = pickFirst(groupOrder.status, rawData.status, cached.rawStatus)
  const statusText = typeof rawStatus === 'string'
    ? rawStatus
    : mapStatusCodeToText(pickNumber(rawStatus, cached.rawStatus, 0))
  const orderCode = pickFirst(groupOrder.orderCode, rawData.orderCode, cached.orderCode, getOrderCode(orderId || groupOrder.id))
  const selected = getSelectionCache()[getSelectionKey(orderId || groupOrder.id, currentUserId)] || []
  const locked = pickBoolean(groupOrder.locked, rawData.locked, cached.locked) ?? false
  const serverParticipants = Array.isArray(rawData.participants)
    ? rawData.participants.map(normalizeParticipant)
    : (Array.isArray(groupOrder.participants) ? groupOrder.participants.map(normalizeParticipant) : [])
  const serverMembers = Array.isArray(rawData.members)
    ? rawData.members.map(normalizeMember)
    : (Array.isArray(groupOrder.members) ? groupOrder.members.map(normalizeMember) : [])
  const hasServerMemberData = serverParticipants.length > 0 || serverMembers.length > 0

  let myOrder = null
  if (selected.length > 0) {
    const totalAmount = selected.reduce((sum, item) => sum + (Number(item.price || 0) * Number(item.quantity || 0)), 0)
    myOrder = {
      userId: currentUserId,
      totalAmount: totalAmount.toFixed(2),
      paid: false,
      dishes: selected.map(item => ({
        dishId: item.dishId,
        name: item.name,
        quantity: item.quantity
      }))
    }
  }

  const members = serverMembers.length > 0
    ? serverMembers
    : (serverParticipants.length > 0 ? buildMembersFromParticipants(serverParticipants) : [...grouped.members])

  if (myOrder && !hasServerMemberData && !members.find(item => item.userId === myOrder.userId)) {
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

  const participants = serverParticipants.length > 0 ? serverParticipants : buildParticipantsFromMembers(members)
  const dishList = Array.isArray(rawData.dishes)
    ? rawData.dishes.map(normalizeDishDetail)
    : (Array.isArray(groupOrder.dishes) ? groupOrder.dishes.map(normalizeDishDetail) : grouped.dishes.map(normalizeDishDetail))
  const dishSummary = Array.isArray(rawData.dishSummary)
    ? rawData.dishSummary.map(normalizeDishSummary)
    : (Array.isArray(groupOrder.dishSummary) ? groupOrder.dishSummary.map(normalizeDishSummary) : grouped.dishes.map(normalizeDishSummary))
  const orderTotalAmount = dishSummary.reduce((sum, item) => sum + Number(item.amount || 0), 0)
  const creatorId = pickFirst(groupOrder.initiatorId, rawData.creatorId, groupOrder.creatorId, cached.creatorId, '')
  const currentUserJoined = pickBoolean(
    groupOrder.currentUserJoined,
    rawData.currentUserJoined,
    participants.some(item => item.userId === currentUserId),
    members.some(item => item.userId === currentUserId),
    myOrder && myOrder.userId === currentUserId
  ) ?? false
  const currentUserPaid = pickBoolean(
    groupOrder.currentUserPaid,
    rawData.currentUserPaid,
    participants.find(item => item.userId === currentUserId)?.paid,
    members.find(item => item.userId === currentUserId)?.paid
  ) ?? false
  const rawStatusCode = typeof rawStatus === 'number' ? rawStatus : mapStatusTextToCode(statusText)
  const canEdit = pickBoolean(groupOrder.canEdit, rawData.canEdit, currentUserJoined && !currentUserPaid && !locked) ?? false
  const canLeave = pickBoolean(
    groupOrder.canLeave,
    rawData.canLeave,
    currentUserJoined && !currentUserPaid && !locked && creatorId !== currentUserId
  ) ?? false
  const canConfirm = pickBoolean(
    groupOrder.canConfirm,
    rawData.canConfirm,
    creatorId === currentUserId && !locked && (participants.some(item => item.dishCount > 0) || dishSummary.some(item => Number(item.count || 0) > 0))
  ) ?? false
  const canPay = pickBoolean(
    groupOrder.canPay,
    rawData.canPay,
    currentUserJoined && !currentUserPaid && locked && rawStatusCode === 0
  ) ?? false
  const currentCount = pickNumber(
    groupOrder.currentCount,
    rawData.currentCount,
    participants.length,
    members.length,
    creatorId ? 1 : 0
  ) || 0
  const maxParticipants = pickNumber(groupOrder.maxParticipants, rawData.maxParticipants, cached.maxParticipants, currentCount) || currentCount

  return {
    id: groupOrder.id || orderId,
    orderId: groupOrder.id || orderId,
    name: pickFirst(groupOrder.name, rawData.name, cached.name, `群订单 ${orderCode}`),
    orderCode,
    status: statusText,
    rawStatus: rawStatusCode,
    locked,
    joinable: pickBoolean(groupOrder.joinable, rawData.joinable, !locked) ?? !locked,
    canEdit,
    canLeave,
    canConfirm,
    canPay,
    currentUserJoined,
    currentUserPaid,
    merchantId: pickFirst(groupOrder.merchantId, rawData.merchantId, cached.merchantId, ''),
    merchantName: pickFirst(groupOrder.merchantName, rawData.merchantName, cached.merchantName, groupOrder.merchantId, ''),
    merchantAvatar: pickFirst(groupOrder.merchantAvatar, rawData.merchantAvatar, cached.merchantAvatar, 'https://via.placeholder.com/100'),
    creatorId,
    creatorName: pickFirst(groupOrder.creatorName, rawData.creatorName, cached.creatorName, ''),
    groupId: pickFirst(groupOrder.groupId, rawData.groupId, cached.groupId, ''),
    currentCount,
    maxParticipants,
    deadline: pickFirst(groupOrder.deadline, rawData.deadline, cached.deadline, groupOrder.updateTime, groupOrder.createTime, ''),
    deliveryAddress: pickFirst(groupOrder.deliveryAddress, rawData.deliveryAddress, cached.deliveryAddress, groupOrder.addressId, ''),
    addressId: pickFirst(groupOrder.addressId, rawData.addressId, cached.addressId, ''),
    remark: pickFirst(groupOrder.remark, rawData.remark, cached.remark, ''),
    totalAmount: formatAmount(pickFirst(groupOrder.totalAmount, rawData.totalAmount, cached.totalAmount, orderTotalAmount, 0)),
    discount: formatAmount(pickFirst(groupOrder.discount, rawData.discount, cached.discount, 0)),
    finalAmount: formatAmount(pickFirst(groupOrder.finalAmount, rawData.finalAmount, groupOrder.totalAmount, rawData.totalAmount, cached.finalAmount, cached.totalAmount, orderTotalAmount, 0)),
    createTime: pickFirst(groupOrder.createTime, rawData.createTime, cached.createTime, ''),
    expireTime: pickFirst(groupOrder.expireTime, rawData.expireTime, cached.expireTime, ''),
    members,
    participants,
    dishes: dishList,
    dishSummary,
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
  const rawStatus = pickFirst(order.status, cached.rawStatus)
  const statusText = typeof rawStatus === 'string' ? rawStatus : mapStatusCodeToText(pickNumber(rawStatus, 0))
  const members = Array.isArray(order.members) ? order.members : (cached.members || [])
  const currentCount = pickNumber(order.currentCount, cached.currentCount, members.length, order.initiatorId || cached.creatorId ? 1 : 0) || 0
  const maxParticipants = pickNumber(order.maxParticipants, cached.maxParticipants, currentCount) || currentCount
  return {
    id: order.id,
    name: pickFirst(order.name, cached.name, `群订单 ${getOrderCode(order.id)}`),
    orderCode: pickFirst(order.orderCode, cached.orderCode, getOrderCode(order.id)),
    status: statusText,
    merchantId: pickFirst(order.merchantId, cached.merchantId, ''),
    merchantName: pickFirst(order.merchantName, cached.merchantName, order.merchantId, ''),
    merchantAvatar: pickFirst(order.merchantAvatar, cached.merchantAvatar, 'https://via.placeholder.com/100'),
    creatorId: pickFirst(order.initiatorId, order.creatorId, cached.creatorId, ''),
    creatorName: pickFirst(order.creatorName, cached.creatorName, ''),
    currentCount,
    maxParticipants,
    deadline: pickFirst(order.deadline, cached.deadline, order.updateTime, order.createTime, ''),
    members
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
      const response = await get(`/v1/group-orders/group-orders/${orderId}`, {
        userId: getCurrentUserId()
      })
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

  pay: (orderId, data = {}) => post(`/v1/group-orders/group-orders/${orderId}/pay`, data),
  payOrder: (orderId, data = {}) => groupOrderApi.pay(orderId, data),
  payMember: (orderId, data = {}) => groupOrderApi.pay(orderId, { ...data, paymentType: 'single' }),
  payMemberOrder: (orderId, data = {}) => groupOrderApi.pay(orderId, { ...data, paymentType: 'single' }),
  joinByCode: async (data = {}) => post('/v1/group-orders/join', data),
  join: () => unsupported('后端暂未提供加入拼单接口'),
  joinGroupOrder: () => unsupported('后端暂未提供加入拼单接口'),
  leave: async (orderId, data = {}) => {
    const userId = data.userId || getCurrentUserId()
    const response = await del(buildQueryUrl(`/v1/group-orders/group-orders/${orderId}/leave`, { userId }))

    const selectionCache = getSelectionCache()
    delete selectionCache[getSelectionKey(orderId, userId)]
    saveSelectionCache(selectionCache)

    const orderCache = getOrderCache()
    if (orderCache[orderId]) {
      const members = Array.isArray(orderCache[orderId].members) ? orderCache[orderId].members : []
      orderCache[orderId] = {
        ...orderCache[orderId],
        members: members.filter(member => member.userId !== userId),
        currentCount: Math.max(0, Number(orderCache[orderId].currentCount || members.length || 0) - 1)
      }
      saveOrderCache(orderCache)
    }

    return response
  },
  leaveGroupOrder: (orderId, data = {}) => groupOrderApi.leave(orderId, data),
  quit: (orderId, data = {}) => groupOrderApi.leave(orderId, data),
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
  invite: (orderId, data = {}) => post(`/v1/group-orders/group-orders/${orderId}/invite`, data),
  confirm: async (orderId, data = {}) => {
    const response = await post(`/v1/group-orders/group-orders/${orderId}/confirm`, {
      userId: data.userId || getCurrentUserId()
    })

    let normalized = response?.data
    if (response?.data?.id || response?.data?.orderId) {
      normalized = normalizeDetailData({ groupOrder: response.data, dishItems: getOrderCache()[orderId]?.dishItems || [] }, orderId)
      mergeOrderCache(orderId, {
        ...getOrderCache()[orderId],
        ...normalized
      })
    }

    return {
      ...response,
      data: normalized
    }
  },
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
