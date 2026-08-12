import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi, getUserInfo, updateUserInfo as updateUserInfoApi, deductBalance as deductBalanceApi } from '../api/user'
import { getAllUserData } from '../api/userData'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  const balance = ref(0)
  const loading = ref(false)
  const userData = ref(null)

  const isLoggedIn = computed(() => !!token.value)

  async function login(account, password) {
    loading.value = true
    try {
      const res = await loginApi({ account, password })
      if (res.code === 200) {
        token.value = res.data.token
        localStorage.setItem('token', token.value)
        userInfo.value = res.data
        balance.value = res.data.balance || 0
        return { success: true, gift: res.data.gift, giftHours: res.data.giftHours }
      } else {
        return { success: false, message: res.message }
      }
    } catch (error) {
      return { success: false, message: error.message || '登录失败' }
    } finally {
      loading.value = false
    }
  }

  async function loadUserData() {
    try {
      const res = await getAllUserData()
      if (res.code === 200) {
        userData.value = res.data
        if (res.data.user) {
          userInfo.value = { ...userInfo.value, ...res.data.user }
          balance.value = res.data.user.balance || 0
        }
        return res.data
      }
    } catch (error) {
      console.error('加载用户数据失败', error)
    }
    return null
  }

  async function register(nickname, phone, email, password, confirmPassword) {
    loading.value = true
    try {
      const res = await registerApi({ nickname, phone, email, password, confirmPassword })
      if (res.code === 200) {
        token.value = res.data.token
        localStorage.setItem('token', token.value)
        // 兼容不同的响应格式
        if (res.data.user) {
          userInfo.value = res.data.user
          balance.value = res.data.user.balance || 0
        } else {
          userInfo.value = res.data
          balance.value = res.data.balance || 0
        }
        return { success: true, gift: res.data.gift, giftHours: res.data.giftHours }
      } else {
        return { success: false, message: res.message }
      }
    } catch (error) {
      return { success: false, message: error.message || '注册失败' }
    } finally {
      loading.value = false
    }
  }

  async function fetchUserInfo() {
    try {
      const res = await getUserInfo()
      if (res.code === 200) {
        userInfo.value = res.data
        balance.value = res.data.balance
        return res.data
      }
    } catch (error) {
      console.error('获取用户信息失败', error)
    }
    return false
  }

  async function updateUserInfo(info) {
    try {
      const res = await updateUserInfoApi(info)
      if (res.code === 200) {
        userInfo.value = { ...userInfo.value, ...info }
        return true
      }
    } catch (error) {
      return false
    }
  }

  async function deductBalance(hours) {
    try {
      const res = await deductBalanceApi(hours)
      if (res.code === 200) {
        balance.value = Math.max(0, balance.value - hours)
        return true
      }
    } catch (error) {
      console.error('扣减余额失败', error)
      return false
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    balance.value = 0
    userData.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('balance')
    // 清除所有本地用户数据
    localStorage.removeItem('game-mate-settings-v2')
    localStorage.removeItem('game-mate-screenshot-v2')
    localStorage.removeItem('game-mate-messages-v2')
    localStorage.removeItem('game-mate-game-v2')
    localStorage.removeItem('game-mate-highlights-v2')
    localStorage.removeItem('game-mate-user-data-v2')
  }

  return {
    token,
    userInfo,
    balance,
    userData,
    isLoggedIn,
    loading,
    login,
    register,
    logout,
    loadUserData,
    fetchUserInfo,
    updateUserInfo,
    deductBalance
  }
})
