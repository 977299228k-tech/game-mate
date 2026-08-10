import request from '../utils/request'

export function getGameList() {
  return request.get('/game/list')
}

export function getPresetGames() {
  return request.get('/game/preset')
}

export function getCustomGames() {
  return request.get('/game/custom')
}

export function addCustomGame(data) {
  return request.post('/game/custom', data)
}

export function deleteCustomGame(id) {
  return request.delete(`/game/custom/${id}`)
}

export function uploadGameIcon(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/game/icon', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function uploadGameIcons(files) {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  return request.post('/game/icons', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}