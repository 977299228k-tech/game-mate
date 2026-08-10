import request from '../utils/request'

export const getAllUserData = () => {
  return request.get('/user-data/all')
}

export const getCustomGames = () => {
  return request.get('/user-data/custom-games')
}

export const addCustomGame = (data) => {
  return request.post('/user-data/custom-games', data)
}

export const deleteCustomGame = (gameId) => {
  return request.delete(`/user-data/custom-games/${gameId}`)
}

export const getSettings = () => {
  return request.get('/user-data/settings')
}

export const updateSettings = (data) => {
  return request.put('/user-data/settings', data)
}

export const getHighlights = () => {
  return request.get('/user-data/highlights')
}

export const deleteHighlight = (highlightId) => {
  return request.delete(`/user-data/highlights/${highlightId}`)
}

export const getExtraServices = () => {
  return request.get('/user-data/extra-services')
}
