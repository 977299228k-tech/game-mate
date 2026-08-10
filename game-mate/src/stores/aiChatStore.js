import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import { updateSettings as updateSettingsApi } from '../api/userData'

const SETTINGS_KEY = 'game-mate-settings-v2'
const SCREENSHOT_KEY = 'game-mate-screenshot-v2'
const MESSAGES_KEY = 'game-mate-messages-v2'
const GAME_KEY = 'game-mate-game-v2'
const HIGHLIGHTS_KEY = 'game-mate-highlights-v2'
const USER_DATA_KEY = 'game-mate-user-data-v2'

function compressScreenshot(base64) {
  return new Promise((resolve) => {
    if (!base64) {
      resolve(null)
      return
    }
    
    if (base64.length < 2000000) {
      resolve(base64)
      return
    }
    
    const img = new Image()
    img.onload = () => {
      const canvas = document.createElement('canvas')
      const maxDim = 1920
      let { width, height } = img
      
      if (width > maxDim || height > maxDim) {
        const ratio = Math.min(maxDim / width, maxDim / height)
        width = Math.floor(width * ratio)
        height = Math.floor(height * ratio)
      }
      
      canvas.width = width
      canvas.height = height
      const ctx = canvas.getContext('2d')
      ctx.drawImage(img, 0, 0, width, height)
      
      try {
        const result = canvas.toDataURL('image/jpeg', 0.90)
        resolve(result)
      } catch (e) {
        resolve(base64)
      }
    }
    img.onerror = () => {
      resolve(base64)
    }
    img.src = base64
  })
}

function loadJSON(key) {
  try {
    const data = localStorage.getItem(key)
    if (data) return JSON.parse(data)
  } catch (e) {
    console.warn('Failed to load', key, ':', e)
  }
  return null
}

function saveJSON(key, value) {
  try {
    localStorage.setItem(key, JSON.stringify(value))
    return true
  } catch (e) {
    console.warn('Failed to save', key, ':', e)
    return false
  }
}

function loadString(key) {
  try {
    return localStorage.getItem(key)
  } catch (e) {
    return null
  }
}

function saveString(key, value) {
  try {
    if (value === null) {
      localStorage.removeItem(key)
    } else {
      localStorage.setItem(key, value)
    }
    return true
  } catch (e) {
    console.warn('Failed to save', key, ':', e)
    return false
  }
}

function removeMarkdown(text) {
  if (!text) return text
  return text
    .replace(/\*\*(.+?)\*\*/g, '$1')
    .replace(/\*(.+?)\*/g, '$1')
    .replace(/`(.+?)`/g, '$1')
    .replace(/^#{1,6}\s+/gm, '')
    .replace(/^>+/gm, '')
    .replace(/\n{3,}/g, '\n\n')
}

const defaultSettings = {
  resolution: '1080p',
  fps: 30,
  language: 'zh-CN',
  personality: 'friendly',
  voice: 'default',
  speechRate: 1.2,
  speechPitch: 1.0
}

const defaultUserData = {
  balance: 0,
  usedHours: 0,
  extras: {
    voiceCustom: { purchased: false, hours: 0, used: 0 },
    personalityCustom: { purchased: false, hours: 0, used: 0 }
  }
}

const defaultMessages = [
  {
    role: 'ai',
    content: '你好呀，我是你的AI电竞伴侣。选择一个游戏开始吧！🎮',
    time: '刚刚'
  }
]

export const useAiChatStore = defineStore('aiChat', () => {
  const savedSettings = loadJSON(SETTINGS_KEY)
  const savedScreenshot = loadString(SCREENSHOT_KEY)
  const savedMessages = loadJSON(MESSAGES_KEY)
  const savedGameId = loadString(GAME_KEY)
  const savedHighlights = loadJSON(HIGHLIGHTS_KEY)
  const savedUserData = loadJSON(USER_DATA_KEY)

  const selectedGameId = ref(savedGameId ? Number(savedGameId) : null)
  const messages = ref(savedMessages || [...defaultMessages])
  const currentScreenshot = ref(savedScreenshot)
  const settings = ref({ ...defaultSettings, ...(savedSettings || {}) })
  const highlights = ref(savedHighlights || [])
  const userData = ref({ ...defaultUserData, ...(savedUserData || {}) })

  watch(selectedGameId, (val) => {
    saveString(GAME_KEY, val ? String(val) : null)
  })

  watch(messages, (val) => {
    const trimmed = val.slice(-50)
    saveJSON(MESSAGES_KEY, trimmed)
  }, { deep: true })

  watch(settings, (val) => {
    saveJSON(SETTINGS_KEY, val)
    // 同步设置到后端
    syncSettingsToBackend()
  }, { deep: true })

  watch(highlights, (val) => {
    saveJSON(HIGHLIGHTS_KEY, val)
  }, { deep: true })

  watch(userData, (val) => {
    saveJSON(USER_DATA_KEY, val)
  }, { deep: true })

  let settingsSyncTimer = null
  let isResetting = false
  
  const syncSettingsToBackend = () => {
    if (isResetting) return
    // 检查是否已登录（通过token）
    const token = localStorage.getItem('token')
    if (!token) return
    
    clearTimeout(settingsSyncTimer)
    settingsSyncTimer = setTimeout(async () => {
      try {
        await updateSettingsApi({
          personality: settings.value.personality,
          voice: settings.value.voice
        })
      } catch (e) {
        // 静默失败
      }
    }, 500)
  }

  const resetToDefault = () => {
    isResetting = true
    selectedGameId.value = null
    messages.value = [...defaultMessages]
    currentScreenshot.value = null
    settings.value = { ...defaultSettings }
    highlights.value = []
    userData.value = { ...defaultUserData }
    
    localStorage.removeItem(SETTINGS_KEY)
    localStorage.removeItem(SCREENSHOT_KEY)
    localStorage.removeItem(MESSAGES_KEY)
    localStorage.removeItem(GAME_KEY)
    localStorage.removeItem(HIGHLIGHTS_KEY)
    localStorage.removeItem(USER_DATA_KEY)
    isResetting = false
  }

  const loadFromBackend = (backendData) => {
    if (!backendData) return
    
    if (backendData.settings) {
      settings.value = {
        ...defaultSettings,
        personality: backendData.settings.personality || defaultSettings.personality,
        voice: backendData.settings.voice || defaultSettings.voice
      }
    }
    
    if (backendData.highlights) {
      highlights.value = backendData.highlights.map(h => ({
        id: h.id,
        gameId: h.gameId,
        title: h.title,
        videoUrl: h.videoUrl,
        thumbnail: h.thumbnail,
        duration: h.duration,
        createTime: h.createTime
      }))
    }
    
    if (backendData.extraServices) {
      backendData.extraServices.forEach(es => {
        if (es.extraId === 1) {
          userData.value.extras.personalityCustom = {
            purchased: es.totalHours > 0,
            hours: es.totalHours,
            used: es.usedHours
          }
        } else if (es.extraId === 2) {
          userData.value.extras.voiceCustom = {
            purchased: es.totalHours > 0,
            hours: es.totalHours,
            used: es.usedHours
          }
        }
      })
    }
    
    if (backendData.user) {
      userData.value.balance = backendData.user.balance || 0
    }
  }

  const setSelectedGameId = (id) => {
    selectedGameId.value = id
  }

  const setMessages = (msgs) => {
    messages.value = msgs
  }

  const addMessage = (msg) => {
    if (msg.role === 'ai' && msg.content) {
      msg.content = removeMarkdown(msg.content)
    }
    messages.value.push(msg)
    if (messages.value.length > 100) {
      messages.value = messages.value.slice(-100)
    }
  }

  const setCurrentScreenshot = async (screenshot) => {
    if (screenshot === null || screenshot === undefined) {
      currentScreenshot.value = null
      saveString(SCREENSHOT_KEY, null)
      return
    }
    
    if (typeof screenshot === 'string') {
      try {
        const compressed = await compressScreenshot(screenshot)
        currentScreenshot.value = compressed || screenshot
        saveString(SCREENSHOT_KEY, currentScreenshot.value)
      } catch (e) {
        console.error('截图处理失败:', e)
        currentScreenshot.value = screenshot
        saveString(SCREENSHOT_KEY, screenshot)
      }
    } else {
      currentScreenshot.value = screenshot
      saveString(SCREENSHOT_KEY, screenshot)
    }
  }

  const clearScreenshot = () => {
    currentScreenshot.value = null
    saveString(SCREENSHOT_KEY, null)
  }

  const updateSettings = (newSettings) => {
    settings.value = { ...settings.value, ...newSettings }
  }

  const addHighlight = (highlight) => {
    highlights.value.unshift(highlight)
    if (highlights.value.length > 50) {
      highlights.value = highlights.value.slice(0, 50)
    }
  }

  const removeHighlight = (id) => {
    const index = highlights.value.findIndex(h => h.id === id)
    if (index > -1) {
      highlights.value.splice(index, 1)
    }
  }

  const updateUserData = (data) => {
    userData.value = { ...userData.value, ...data }
  }

  const purchaseExtra = (extraType, hours) => {
    if (userData.value.extras[extraType]) {
      userData.value.extras[extraType].purchased = true
      userData.value.extras[extraType].hours += hours
    }
  }

  const deductUserBalance = (hours) => {
    userData.value.balance = Math.max(0, userData.value.balance - hours)
    userData.value.usedHours += hours
  }

  const addUserBalance = (hours) => {
    userData.value.balance += hours
  }

  const recordAIUsage = (seconds) => {
    const hours = seconds / 3600
    if (userData.value.balance > 0) {
      const deduct = Math.min(userData.value.balance, hours)
      userData.value.balance = Math.max(0, userData.value.balance - deduct)
      userData.value.usedHours += deduct
    }
  }

  const recordPersonalityUsage = (seconds) => {
    const hours = seconds / 3600
    const extra = userData.value.extras.personalityCustom
    if (extra && extra.purchased && extra.hours > extra.used) {
      const available = extra.hours - extra.used
      const deduct = Math.min(available, hours)
      extra.used += deduct
    } else if (userData.value.balance > 0) {
      const deduct = Math.min(userData.value.balance, hours * 1.5)
      userData.value.balance = Math.max(0, userData.value.balance - deduct)
      userData.value.usedHours += deduct
    }
  }

  const recordVoiceUsage = (seconds) => {
    const hours = seconds / 3600
    const extra = userData.value.extras.voiceCustom
    if (extra && extra.purchased && extra.hours > extra.used) {
      const available = extra.hours - extra.used
      const deduct = Math.min(available, hours)
      extra.used += deduct
    } else if (userData.value.balance > 0) {
      const deduct = Math.min(userData.value.balance, hours * 0.5)
      userData.value.balance = Math.max(0, userData.value.balance - deduct)
      userData.value.usedHours += deduct
    }
  }

  const hasPurchasedExtra = (type) => {
    return userData.value.extras[type]?.purchased || false
  }

  const hasEnoughBalance = (type) => {
    if (type === 'personalityCustom') {
      const extra = userData.value.extras.personalityCustom
      if (extra && extra.purchased && extra.hours > extra.used) return true
      return userData.value.balance > 0
    }
    if (type === 'voiceCustom') {
      const extra = userData.value.extras.voiceCustom
      if (extra && extra.purchased && extra.hours > extra.used) return true
      return userData.value.balance > 0
    }
    return userData.value.balance > 0
  }

  const clearChatData = () => {
    messages.value = [...defaultMessages]
    currentScreenshot.value = null
    saveString(SCREENSHOT_KEY, null)
  }

  const clearGameRecords = () => {
    selectedGameId.value = null
    saveString(GAME_KEY, null)
    currentScreenshot.value = null
    saveString(SCREENSHOT_KEY, null)
  }

  const clearHighlights = () => {
    highlights.value = []
  }

  const clearAll = () => {
    resetToDefault()
  }

  return {
    selectedGameId,
    messages,
    currentScreenshot,
    settings,
    highlights,
    userData,
    setSelectedGameId,
    setMessages,
    addMessage,
    setCurrentScreenshot,
    clearScreenshot,
    updateSettings,
    addHighlight,
    removeHighlight,
    updateUserData,
    purchaseExtra,
    deductUserBalance,
    addUserBalance,
    recordAIUsage,
    recordPersonalityUsage,
    recordVoiceUsage,
    hasPurchasedExtra,
    hasEnoughBalance,
    clearChatData,
    clearGameRecords,
    clearHighlights,
    clearAll,
    resetToDefault,
    loadFromBackend
  }
})
