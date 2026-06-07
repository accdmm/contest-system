import request from './request'

export function registerPersonal(data) {
  return request.post('/registration/personal', data)
}

export function registerTeam(data) {
  return request.post('/registration/team', data)
}

export function approveRegistration(id) {
  return request.post(`/registration/${id}/approve`)
}

export function rejectRegistration(id, reason) {
  return request.post(`/registration/${id}/reject`, { reason })
}

export function cancelRegistration(id, userId) {
  return request.post(`/registration/${id}/cancel`, null, { params: { userId } })
}

export function getRegistrationById(id) {
  return request.get(`/registration/${id}`)
}

export function pageRegistrationByUser(userId, params) {
  return request.get(`/registration/user/${userId}`, { params })
}

export function pageRegistrationByContest(contestId, params) {
  return request.get(`/registration/contest/${contestId}`, { params })
}

export function pageRegistration(params) {
  return request.get('/registration/page', { params })
}
