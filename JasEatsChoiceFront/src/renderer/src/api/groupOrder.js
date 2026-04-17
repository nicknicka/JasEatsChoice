import api from '../utils/api'

export default {
  getDetail(groupOrderId, userId) {
    return api.get(`/v1/group-orders/group-orders/${groupOrderId}`, {
      params: userId ? { userId } : {}
    })
  },

  saveSelections(groupOrderId, data) {
    return api.post(`/v1/group-orders/group-orders/${groupOrderId}/selections`, data)
  },

  confirm(groupOrderId, data) {
    return api.post(`/v1/group-orders/group-orders/${groupOrderId}/confirm`, data)
  },

  getSettlement(groupOrderId, userId) {
    return api.get(`/v1/group-orders/group-orders/${groupOrderId}/settlement`, {
      params: { userId }
    })
  },

  pay(groupOrderId, data) {
    return api.post(`/v1/group-orders/group-orders/${groupOrderId}/pay`, data)
  }
}
