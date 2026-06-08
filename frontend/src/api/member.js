import request from './request'

export function getMembers(params) {
  return request({
    url: '/members',
    method: 'get',
    params
  })
}

export function getMemberById(id) {
  return request({
    url: `/members/${id}`,
    method: 'get'
  })
}

export function getMemberByPhone(phone) {
  return request({
    url: `/members/phone/${phone}`,
    method: 'get'
  })
}

export function createMember(data) {
  return request({
    url: '/members',
    method: 'post',
    data
  })
}

export function updateMember(id, data) {
  return request({
    url: `/members/${id}`,
    method: 'put',
    data
  })
}

export function deleteMember(id) {
  return request({
    url: `/members/${id}`,
    method: 'delete'
  })
}
