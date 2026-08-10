import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getGameList, addCustomGame as addCustomGameApi, deleteCustomGame as deleteCustomGameApi, uploadGameIcon } from '../api/game'

const ICON_OPTIONS = ['🎮', '⚔️', '🗡️', '🏹', '🔫', '💎', '🎲', '🪄', '🦖', '🤠', '🌾', '🌆', '🚀', '🏎️', '⚽', '🏀', '🎯', '🛡️', '⚡', '🔥', '❄️', '🌙', '⭐', '🌈', '🎨', '🎭', '🎪', '🏰', '🗺️', '🏆']

const COLOR_OPTIONS = [
  '#6366f1', '#8b5cf6', '#3b82f6', '#06b6d4', '#10b981', 
  '#22c55e', '#eab308', '#f59e0b', '#ef4444', '#ec4899',
  '#dc2626', '#ea580c', '#78350f', '#65a30d', '#a855f7'
]

const AI_FEATURES = [
  { icon: '🎯', title: '实时语音对话', description: 'AI与玩家实时语音交互，支持打断、语气切换' },
  { icon: '👁️', title: '游戏场景识别', description: 'OCR自动识别游戏画面，覆盖多种游戏类型' },
  { icon: '⚡', title: '实时攻略提示', description: '卡关时主动提示解谜思路、BOSS打法' },
  { icon: '🎨', title: '智能操作建议', description: '分析玩家操作习惯，提供个性化提升建议' }
]

export const useGameStore = defineStore('game', () => {
  const games = ref([])
  const iconOptions = ref(ICON_OPTIONS)
  const colorOptions = ref(COLOR_OPTIONS)
  const aiFeatures = ref(AI_FEATURES)
  const loading = ref(false)
  const initialized = ref(false)

  const popularGames = computed(() => games.value.slice(0, 6))
  const customGames = computed(() => games.value.filter(g => g.isCustom))

  async function fetchGames() {
    if (initialized.value) return
    loading.value = true
    try {
      const res = await getGameList()
      if (res.code === 200) {
        // 映射后端字段到前端使用的字段
        games.value = res.data.map(game => ({
          id: game.id,
          name: game.name,
          genre: game.genre,
          icon: game.icon,
          imageUrl: game.imageUrl,
          color: game.color,
          description: game.description,
          tags: game.tags,
          isCustom: game.isCustom === 1
        }))
        initialized.value = true
      }
    } catch (error) {
      console.error('获取游戏列表失败', error)
    } finally {
      loading.value = false
    }
  }

  async function addCustomGame(gameData) {
    try {
      let iconUrl = ''
      if (gameData.iconFile) {
        const res = await uploadGameIcon(gameData.iconFile)
        if (res.code === 200) {
          iconUrl = res.data
        }
      }
      
      const data = {
        name: gameData.name,
        genre: gameData.genre || '自定义',
        icon: gameData.icon || '🎮',
        imageUrl: iconUrl,
        color: gameData.color || '#8b5cf6',
        description: gameData.description || '自定义游戏',
        tags: gameData.tags || ['自定义']
      }
      
      const res = await addCustomGameApi(data)
      if (res.code === 200) {
        games.value.push(res.data)
        return res.data
      }
    } catch (error) {
      console.error('添加自定义游戏失败', error)
      return null
    }
  }

  async function removeCustomGame(gameId) {
    try {
      const res = await deleteCustomGameApi(gameId)
      if (res.code === 200) {
        const index = games.value.findIndex(g => g.id === gameId)
        if (index !== -1) {
          games.value.splice(index, 1)
        }
        return true
      }
    } catch (error) {
      console.error('删除自定义游戏失败', error)
      return false
    }
  }

  function reset() {
    games.value = []
    initialized.value = false
  }

  return {
    games,
    iconOptions,
    colorOptions,
    aiFeatures,
    loading,
    popularGames,
    customGames,
    fetchGames,
    addCustomGame,
    removeCustomGame,
    reset
  }
})
