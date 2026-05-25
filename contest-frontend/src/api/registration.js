import request from './request'

export function registerPersonal(data) {
  return request.post('/registration/personal', data)
}

export function registerTeam(data) {
  return request.post('/registration/team', data)
}

export function approveRegistration(id) {
  return request.put(`/registration/${id}/approve`)
}

export function rejectRegistration(id, reason) {
  return request.put(`/registration/${id}/reject`, { reason })
}

export function cancelRegistration(id, userId) {
  return request.put(`/registration/${id}/cancel`, null, { params: { userId } })
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
