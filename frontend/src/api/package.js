import request from './request'

export function getDashboardStats() {
  return request({
    url: '/dashboard/stats',
    method: 'get'
  })
}

export function createPackage(data) {
  return request({
    url: '/packages',
    method: 'post',
    data
  })
}

export function searchPackages(params) {
  return request({
    url: '/packages',
    method: 'get',
    params
  })
}

export function getPackageById(id) {
  return request({
    url: `/packages/${id}`,
    method: 'get'
  })
}

export function pickupPackage(id, data) {
  return request({
    url: `/packages/${id}/pickup`,
    method: 'put',
    data
  })
}

export function getOverduePackages() {
  return request({
    url: '/packages/overdue',
    method: 'get'
  })
}

export function getDailyStats() {
  return request({
    url: '/packages/stats/daily',
    method: 'get'
  })
}

export function createShipment(data) {
  return request({
    url: '/shipments',
    method: 'post',
    data
  })
}

export function getShipments(params) {
  return request({
    url: '/shipments',
    method: 'get',
    params
  })
}

export function markShipmentSent(id) {
  return request({
    url: `/shipments/${id}/ship`,
    method: 'put'
  })
}
