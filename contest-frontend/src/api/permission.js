import request from './request'

export function getMyPermissions() {
  return request.get('/permission/mine')
}
