import request from './request'

export function login(data) {
  return request.post('/user/login', data)
}

export function register(data) {
  return request.post('/user/register', data)
}

export function getUserById(id) {
  return request.get(`/user/detail/${id}`)
}

export function updateProfile(id, data) {
  return request.post(`/user/${id}/profile`, data)
}

export function changePassword(id, data) {
  return request.post(`/user/${id}/password`, data)
}

export function freezeUser(id) {
  return request.post(`/user/${id}/freeze`)
}

export function unfreezeUser(id) {
  return request.post(`/user/${id}/unfreeze`)
}

export function pageUsers(params) {
  return request.get('/user/page', { params })
}

export function getColleges() {
  return request.get('/user/colleges')
}

export function listTeachers() {
  return request.get('/user/teachers')
}

export function getMajors(collegeId) {
  return request.get('/user/majors', { params: { collegeId } })
}

export function adminCreateUser(data) {
  return request.post('/user/admin/create', data)
}
