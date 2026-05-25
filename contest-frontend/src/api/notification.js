import request from './request'

export function pageNotificationByUser(userId, params) {
  return request.get(`/notification/user/${userId}`, { params })
}

export function getUnreadCount(userId) {
  return request.get(`/notification/unread/${userId}`)
}

export function markNotificationRead(id, userId) {
  return request.put(`/notification/${id}/read`, null, { params: { userId } })
}

export function markAllNotificationsRead(userId) {
  return request.put(`/notification/read-all/${userId}`)
}
