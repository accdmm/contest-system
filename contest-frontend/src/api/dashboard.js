import request from './request'

export function getDashboardStatistics() {
  return request.get('/admin/dashboard/statistics')
}

export function getContestCategoryDistribution() {
  return request.get('/admin/dashboard/contest-category')
}

export function getContestLevelDistribution() {
  return request.get('/admin/dashboard/contest-level')
}

export function getRegistrationTrend(days = 30) {
  return request.get('/admin/dashboard/registration-trend', { params: { days } })
}

export function getRegistrationStatus() {
  return request.get('/admin/dashboard/registration-status')
}

export function getUserGrowth(days = 30) {
  return request.get('/admin/dashboard/user-growth', { params: { days } })
}

export function getTopContests(limit = 10) {
  return request.get('/admin/dashboard/top-contests', { params: { limit } })
}
