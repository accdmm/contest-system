import request from './request'

export function pageNotificationByUser(userId, params) {
  return request.get(`/notification/user/${userId}`, { params })
}

export function getUnreadCount(userId) {
  return request.get(`/notification/unread/${userId}`)
}

export function markNotificationRead(id, userId) {
  return request.post(`/notification/${id}/read`, null, { params: { userId } })
}

export function markAllNotificationsRead(userId) {
  return request.post(`/notification/read-all/${userId}`)
}

export function sendNotification(params) {
  return request.post('/notification/send', null, { params })
}

export function sendBroadcast(params) {
  return request.post('/notification/broadcast', null, { params })
}
