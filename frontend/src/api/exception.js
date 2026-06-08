import request from './request'

export function createException(data) {
  return request({
    url: '/exceptions',
    method: 'post',
    data
  })
}

export function searchExceptions(params) {
  return request({
    url: '/exceptions',
    method: 'get',
    params
  })
}

export function getExceptionById(id) {
  return request({
    url: `/exceptions/${id}`,
    method: 'get'
  })
}

export function getPendingExceptions() {
  return request({
    url: '/exceptions/pending',
    method: 'get'
  })
}

export function updateExceptionStatus(id, data) {
  return request({
    url: `/exceptions/${id}/status`,
    method: 'put',
    data
  })
}

export function compensate(data) {
  return request({
    url: '/exceptions/compensate',
    method: 'post',
    data
  })
}

export function getCompensationList() {
  return request({
    url: '/exceptions/compensations',
    method: 'get'
  })
}
