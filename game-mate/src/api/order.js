import request from '../utils/request'

export function createOrder(data) {
  return request.post('/order', data)
}

export function payOrder(orderId, payMethod) {
  return request.post(`/order/${orderId}/pay`, null, { params: { payMethod } })
}

export function getOrderList(params) {
  return request.get('/order/list', { params })
}

export function getOrderDetail(id) {
  return request.get(`/order/${id}`)
}