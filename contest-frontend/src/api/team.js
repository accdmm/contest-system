import request from './request'

export function createTeam(data) {
  return request.post('/team', data)
}

export function generateInviteCode(teamId, userId) {
  return request.post(`/team/${teamId}/invite`, { userId })
}

export function joinByInviteCode(data) {
  return request.post('/team/join', data)
}

export function approveMember(teamId, userId, memberId) {
  return request.put(`/team/${teamId}/members/${memberId}/approve`, null, { params: { userId } })
}

export function rejectMember(teamId, userId, memberId) {
  return request.put(`/team/${teamId}/members/${memberId}/reject`, null, { params: { userId } })
}

export function removeMember(teamId, userId, memberId) {
  return request.delete(`/team/${teamId}/members/${memberId}`, { params: { userId } })
}

export function dissolveTeam(teamId, userId) {
  return request.put(`/team/${teamId}/dissolve`, null, { params: { userId } })
}

export function submitTeamReview(teamId, userId) {
  return request.put(`/team/${teamId}/submit`, null, { params: { userId } })
}

export function getTeamById(id) {
  return request.get(`/team/${id}/detail`)
}

export function listTeamMembers(teamId) {
  return request.get(`/team/${teamId}/members`)
}

export function listPendingMembers(teamId) {
  return request.get(`/team/${teamId}/pending`)
}

export function getTeamsByLeader(userId) {
  return request.get('/team/leader', { params: { userId } })
}

export function leaveTeam(teamId, userId) {
  return request.put(`/team/${teamId}/leave`, null, { params: { userId } })
}

export function pageTeams(params) {
  return request.get('/team/page', { params })
}

export function adminApproveTeam(teamId) {
  return request.put(`/team/${teamId}/admin-approve`)
}

export function adminRejectTeam(teamId, reason) {
  return request.put(`/team/${teamId}/admin-reject`, { reason })
}

export function listUserTeams(userId) {
  return request.get(`/team/user/${userId}`)
}
