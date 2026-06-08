import request from './request'

export function getReconciliations(params) {
  return request({
    url: '/reconciliation',
    method: 'get',
    params
  })
}

export function getReconciliationById(id) {
  return request({
    url: `/reconciliation/${id}`,
    method: 'get'
  })
}

export function generateReconciliation(data) {
  return request({
    url: '/reconciliation/generate',
    method: 'post',
    data
  })
}

export function confirmReconciliation(id) {
  return request({
    url: `/reconciliation/${id}/confirm`,
    method: 'put'
  })
}

export function updateReconciliationRemark(id, data) {
  return request({
    url: `/reconciliation/${id}/remark`,
    method: 'put',
    data
  })
}

export function getCourierList() {
  return request({
    url: '/reconciliation/couriers',
    method: 'get'
  })
}
