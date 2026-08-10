import request from '../utils/request'

export function getPlanList() {
  return request.get('/plan/list')
}

export function getExtraServiceList() {
  return request.get('/plan/extra/list')
}
