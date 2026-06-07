import request from './request'

export function calculatePrice(data) {
  return request({
    url: '/pricing/calculate',
    method: 'post',
    data
  })
}

export function getCompanies(params) {
  return request({
    url: '/pricing/companies',
    method: 'get',
    params
  })
}

export function getCompany(id) {
  return request({
    url: `/pricing/companies/${id}`,
    method: 'get'
  })
}

export function createCompany(data) {
  return request({
    url: '/pricing/companies',
    method: 'post',
    data
  })
}

export function updateCompany(id, data) {
  return request({
    url: `/pricing/companies/${id}`,
    method: 'put',
    data
  })
}

export function toggleCompany(id, enabled) {
  return request({
    url: `/pricing/companies/${id}/toggle`,
    method: 'put',
    params: { enabled }
  })
}

export function getTemplates(params) {
  return request({
    url: '/pricing/templates',
    method: 'get',
    params
  })
}

export function getTemplate(id) {
  return request({
    url: `/pricing/templates/${id}`,
    method: 'get'
  })
}

export function createTemplate(data) {
  return request({
    url: '/pricing/templates',
    method: 'post',
    data
  })
}

export function updateTemplate(id, data) {
  return request({
    url: `/pricing/templates/${id}`,
    method: 'put',
    data
  })
}

export function toggleTemplate(id, enabled) {
  return request({
    url: `/pricing/templates/${id}/toggle`,
    method: 'put',
    params: { enabled }
  })
}

export function setDefaultTemplate(id) {
  return request({
    url: `/pricing/templates/${id}/default`,
    method: 'put'
  })
}

export function getRules(params) {
  return request({
    url: '/pricing/rules',
    method: 'get',
    params
  })
}

export function getRule(id) {
  return request({
    url: `/pricing/rules/${id}`,
    method: 'get'
  })
}

export function createRule(data) {
  return request({
    url: '/pricing/rules',
    method: 'post',
    data
  })
}

export function updateRule(id, data) {
  return request({
    url: `/pricing/rules/${id}`,
    method: 'put',
    data
  })
}

export function toggleRule(id, enabled) {
  return request({
    url: `/pricing/rules/${id}/toggle`,
    method: 'put',
    params: { enabled }
  })
}

export function deleteRule(id) {
  return request({
    url: `/pricing/rules/${id}`,
    method: 'delete'
  })
}
