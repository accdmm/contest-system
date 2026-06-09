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
  return request.post(`/team/${teamId}/members/${memberId}/approve`, null, { params: { userId } })
}

export function rejectMember(teamId, userId, memberId) {
  return request.post(`/team/${teamId}/members/${memberId}/reject`, null, { params: { userId } })
}

export function removeMember(teamId, userId, memberId) {
  return request.post(`/team/${teamId}/members/${memberId}`, null, { params: { userId } })
}

export function dissolveTeam(teamId, userId) {
  return request.post(`/team/${teamId}/dissolve`, null, { params: { userId } })
}

export function submitTeamReview(teamId, userId) {
  return request.post(`/team/${teamId}/submit`, null, { params: { userId } })
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
  return request.post(`/team/${teamId}/leave`, null, { params: { userId } })
}

export function pageTeams(params) {
  return request.get('/team/page', { params })
}

export function adminApproveTeam(teamId) {
  return request.post(`/team/${teamId}/admin-approve`)
}

export function adminRejectTeam(teamId, reason) {
  return request.post(`/team/${teamId}/admin-reject`, { reason })
}

export function listUserTeams(userId) {
  return request.get(`/team/user/${userId}`)
}

export function setTeamTeacher(teamId, teacherId) {
  return request.post(`/team/${teamId}/teacher`, { teacherId })
}

export function getTeacherTeams() {
  return request.get('/team/teacher')
}
