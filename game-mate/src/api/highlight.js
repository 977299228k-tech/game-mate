import request from '../utils/request'

export function getHighlightList(params) {
  return request.get('/highlight/list', { params })
}

export function uploadHighlight(data) {
  const formData = new FormData()
  formData.append('gameId', data.gameId)
  formData.append('title', data.title)
  formData.append('video', data.video)
  formData.append('thumbnail', data.thumbnail)
  return request.post('/highlight', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function deleteHighlight(id) {
  return request.delete(`/highlight/${id}`)
}