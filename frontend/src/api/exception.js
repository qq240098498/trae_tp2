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

export function getCompensationStandards() {
  return request({
    url: '/compensation-standards',
    method: 'get'
  })
}

export function createCompensationStandard(data) {
  return request({
    url: '/compensation-standards',
    method: 'post',
    data
  })
}

export function updateCompensationStandard(id, data) {
  return request({
    url: `/compensation-standards/${id}`,
    method: 'put',
    data
  })
}

export function deleteCompensationStandard(id) {
  return request({
    url: `/compensation-standards/${id}`,
    method: 'delete'
  })
}

export function getCompensationStandardByType(exceptionType) {
  return request({
    url: `/compensation-standards/type/${exceptionType}`,
    method: 'get'
  })
}
