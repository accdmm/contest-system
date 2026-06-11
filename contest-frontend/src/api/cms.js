import request from './request'

export function listBanners() {
  return request.get('/cms/banners')
}

export function listAnnouncements(position) {
  const params = {}
  if (position !== undefined && position !== null) params.position = position
  return request.get('/cms/announcements', { params })
}

export function createCmsContent(data) {
  return request.post('/cms', data)
}

export function updateCmsContent(data) {
  return request.put(`/cms/${data.id}`, data)
}

export function deleteCmsContent(id) {
  return request.delete(`/cms/${id}`)
}

export function getCmsContentById(id) {
  return request.get(`/cms/${id}`)
}
