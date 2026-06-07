import request from './request'

export function createContest(data) {
  return request.post('/contest', data)
}

export function updateContest(data) {
  return request.post('/contest', data)
}

export function deleteContest(id) {
  return request.post(`/contest/${id}`)
}

export function publishContest(id) {
  return request.post(`/contest/${id}/publish`)
}

export function unpublishContest(id) {
  return request.post(`/contest/${id}/unpublish`)
}

export function getContestById(id) {
  return request.get(`/contest/${id}`)
}

export function pageContests(params) {
  return request.get('/contest/page', { params })
}

export function getHotContests(limit = 5) {
  return request.get('/contest/hot', { params: { limit } })
}

export function getLatestContests(limit = 5) {
  return request.get('/contest/latest', { params: { limit } })
}
