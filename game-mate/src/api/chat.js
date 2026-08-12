import request from '../utils/request'
import { buildServerUrl } from '../config/runtime'

export function getChatMessages(gameId) {
  return request.get('/chat/messages', { params: { gameId } })
}

export function sendMessage(data) {
  return request.post('/chat/messages', data)
}

export function sendMessageWithPersonality(data) {
  return request.post('/chat/messages-with-personality', data)
}

export async function streamMessageWithPersonality(data, { onDelta, signal } = {}) {
  const token = localStorage.getItem('token')
  const response = await fetch(buildServerUrl('/api/chat/messages-with-personality/stream'), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify(data),
    signal
  })

  if (!response.ok) {
    const message = await response.text()
    if (response.status === 401) {
      localStorage.removeItem('token')
      window.location.hash = '#/login'
      throw new Error('登录已过期，请重新登录')
    }
    throw new Error(message || `流式请求失败（${response.status}）`)
  }
  if (!response.body) {
    throw new Error('当前浏览器不支持读取流式响应')
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''
  let completeContent = ''

  const consumeEvent = (block) => {
    if (!block.trim()) return
    let eventName = 'message'
    const dataLines = []
    block.split('\n').forEach((line) => {
      if (line.startsWith('event:')) eventName = line.slice(6).trim()
      if (line.startsWith('data:')) dataLines.push(line.slice(5).trim())
    })
    if (!dataLines.length) return

    const rawData = dataLines.join('\n')
    let payload
    try {
      payload = JSON.parse(rawData)
    } catch {
      payload = { content: rawData }
    }

    if (eventName === 'error') {
      throw new Error(payload.message || 'AI流式回答失败')
    }
    if (eventName === 'delta' && payload.content) {
      completeContent += payload.content
      onDelta?.(payload.content, completeContent)
    }
    if (eventName === 'done' && !completeContent && payload.content) {
      completeContent = payload.content
      onDelta?.(payload.content, completeContent)
    }
  }

  while (true) {
    const { value, done } = await reader.read()
    buffer += decoder.decode(value || new Uint8Array(), { stream: !done }).replace(/\r\n/g, '\n')
    const blocks = buffer.split('\n\n')
    buffer = blocks.pop() || ''
    blocks.forEach(consumeEvent)
    if (done) break
  }
  consumeEvent(buffer)
  return completeContent
}

export function getRecentMessages(gameId, limit = 20) {
  return request.get('/chat/recent', { params: { gameId, limit } })
}

export function analyzeScreen(data) {
  const formData = new FormData()
  formData.append('gameId', data.gameId)
  formData.append('image', data.image)
  return request.post('/chat/analyze', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function analyzeScreenWithQuery(data) {
  const formData = new FormData()
  formData.append('gameId', data.gameId)
  formData.append('image', data.image)
  if (data.content) {
    formData.append('content', data.content)
  }
  return request.post('/chat/analyze-with-query', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function analyzeScreenWithPersonality(data) {
  return request.post('/chat/analyze-with-personality', {
    gameId: data.gameId,
    imageBase64: data.imageBase64,
    content: data.content,
    personality: data.personality
  })
}
