import request from './request'

export function login(data) {
  return request.post('/user/login', data)
}

export function register(data) {
  return request.post('/user/register', data)
}

export function getUserById(id) {
  return request.get(`/user/${id}`)
}

export function updateProfile(id, data) {
  return request.put(`/user/${id}/profile`, data)
}

export function changePassword(id, data) {
  return request.put(`/user/${id}/password`, data)
}

export function freezeUser(id) {
  return request.put(`/user/${id}/freeze`)
}

export function unfreezeUser(id) {
  return request.put(`/user/${id}/unfreeze`)
}

export function pageUsers(params) {
  return request.get('/user/page', { params })
}
