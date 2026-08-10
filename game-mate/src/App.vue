<template>
  <div id="app-wrapper">
    <!-- 顶部导航 -->
    <header
      v-if="showHeader"
      class="app-header"
      :class="{ 'header-hidden': isHeaderHidden }"
    >
      <div class="header-inner page-container">
        <div class="logo" @click="$router.push('/home')">
          <div class="logo-icon">GM</div>
          <span class="logo-text">Game <span class="gradient-text">Mate</span></span>
        </div>
        <nav class="header-nav">
          <router-link to="/home" class="nav-link" active-class="active">首页</router-link>
          <router-link to="/ai-chat" class="nav-link" active-class="active">AI对话</router-link>
          <router-link to="/recharge" class="nav-link" active-class="active">充值</router-link>
          <router-link to="/settings" class="nav-link" active-class="active">设置</router-link>
          <router-link to="/highlights" class="nav-link" active-class="active">高光</router-link>
          <router-link to="/privacy" class="nav-link" active-class="active">隐私</router-link>
        </nav>
        <div class="header-right">
          <div v-if="userStore.isLoggedIn" class="user-menu-wrapper" @click.stop="toggleUserMenu">
            <div class="user-btn">
              <el-avatar :size="32" :style="{ background: 'var(--gradient-primary)' }">
                {{ userStore.userInfo?.nickname?.[0] || '' }}
              </el-avatar>
              <span class="user-label">我的</span>
            </div>
            <div v-if="showUserMenu" class="user-dropdown">
              <div class="dropdown-item" @click="navigateTo('/settings')">
                <el-icon><Setting /></el-icon>
                <span>个人设置</span>
              </div>
              <div class="dropdown-item" @click="navigateTo('/recharge')">
                <el-icon><Wallet /></el-icon>
                <span>充值中心</span>
              </div>
              <div class="dropdown-divider"></div>
              <div class="dropdown-item danger" @click="handleLogout">
                <el-icon><SwitchButton /></el-icon>
                <span>退出登录</span>
              </div>
            </div>
          </div>
          <router-link v-else to="/login" class="login-btn">登录</router-link>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="app-main" :class="{ 'with-header': showHeader }">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 底部导航（始终固定） -->
    <nav v-if="showFooter" class="app-footer">
      <router-link to="/home" class="footer-item" active-class="active">
        <el-icon :size="22"><HomeFilled /></el-icon>
        <span>首页</span>
      </router-link>
      <router-link to="/ai-chat" class="footer-item" active-class="active">
        <el-icon :size="22"><ChatDotRound /></el-icon>
        <span>AI对话</span>
      </router-link>
      <router-link to="/recharge" class="footer-item" active-class="active">
        <el-icon :size="22"><Wallet /></el-icon>
        <span>充值</span>
      </router-link>
      <router-link to="/settings" class="footer-item" active-class="active">
        <el-icon :size="22"><Setting /></el-icon>
        <span>设置</span>
      </router-link>
      <router-link to="/highlights" class="footer-item" active-class="active">
        <el-icon :size="22"><Star /></el-icon>
        <span>高光</span>
      </router-link>
    </nav>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { HomeFilled, ChatDotRound, Wallet, Setting, Star, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from './stores/userStore'
import { useAiChatStore } from './stores/aiChatStore'
import { useGameStore } from './stores/gameStore'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const aiChatStore = useAiChatStore()
const gameStore = useGameStore()

const showHeader = computed(() => !['Login', 'Register'].includes(route.name))
const showFooter = computed(() => 
  ['Home', 'AiChat', 'Recharge', 'Settings', 'Highlights', 'Privacy'].includes(route.name)
)

const isHeaderHidden = ref(false)
const lastScrollY = ref(0)
const showUserMenu = ref(false)

function handleScroll() {
  const currentScrollY = window.scrollY
  if (currentScrollY > lastScrollY.value && currentScrollY > 100) {
    isHeaderHidden.value = true
    showUserMenu.value = false
  } else {
    isHeaderHidden.value = false
  }
  lastScrollY.value = currentScrollY
}

function toggleUserMenu() {
  showUserMenu.value = !showUserMenu.value
}

function navigateTo(path) {
  showUserMenu.value = false
  router.push(path)
}

function handleLogout() {
  showUserMenu.value = false
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定退出',
    cancelButtonText: '取消'
  }).then(() => {
    userStore.logout()
    aiChatStore.resetToDefault()
    gameStore.reset()
    router.push('/login')
  }).catch(() => {})
}

function handleClickOutside() {
  showUserMenu.value = false
}

async function initUserData() {
  if (userStore.isLoggedIn) {
    try {
      const backendData = await userStore.loadUserData()
      if (backendData) {
        aiChatStore.loadFromBackend(backendData)
      }
      // 重新加载游戏列表
      gameStore.reset()
      await gameStore.fetchGames()
    } catch (e) {
      // 静默处理
    }
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  document.addEventListener('click', handleClickOutside)
  initUserData()
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
#app-wrapper {
  min-height: 100vh;
  background: var(--bg-primary);
  display: flex;
  flex-direction: column;
}

/* 顶部导航 */
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: rgba(13, 13, 20, 0.85);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--border-color);
  transition: transform 0.3s ease;
}

.app-header.header-hidden {
  transform: translateY(-100%);
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 80px;
  padding: 0 32px;
  max-width: 1400px;
  margin: 0 auto;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.logo-icon {
  width: 44px;
  height: 44px;
  background: var(--gradient-primary);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 16px;
}

.logo-text {
  font-size: 24px;
  font-weight: 700;
}

.header-nav {
  display: flex;
  gap: 40px;
}

.nav-link {
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 17px;
  font-weight: 500;
  padding: 10px 0;
  transition: color 0.3s;
  position: relative;
}

.nav-link:hover,
.nav-link.active {
  color: var(--text-primary);
}

.nav-link.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: var(--gradient-primary);
  border-radius: 1px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.login-btn {
  color: var(--text-primary);
  text-decoration: none;
  padding: 8px 24px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  transition: all 0.3s;
}

.login-btn:hover {
  border-color: var(--accent-purple);
  background: var(--accent-purple);
}

.user-menu-wrapper {
  position: relative;
}

.user-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 14px;
  border-radius: 20px;
  background: var(--bg-secondary);
  transition: all 0.3s;
}

.user-btn:hover {
  background: var(--bg-card);
  border-color: var(--accent-purple);
}

.user-label {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
}

.user-dropdown {
  position: absolute;
  top: calc(100% + 12px);
  right: 0;
  min-width: 180px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 8px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.4);
  z-index: 200;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 15px;
  color: var(--text-primary);
}

.dropdown-item:hover {
  background: var(--bg-secondary);
}

.dropdown-item.danger {
  color: #ef4444;
}

.dropdown-item.danger:hover {
  background: rgba(239, 68, 68, 0.1);
}

.dropdown-divider {
  height: 1px;
  background: var(--border-color);
  margin: 6px 0;
}

/* 主内容区 */
.app-main {
  flex: 1;
  padding-top: 80px;
  padding-bottom: 100px;
}

/* 底部导航 */
.app-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 80px;
  background: rgba(13, 13, 20, 0.95);
  backdrop-filter: blur(20px);
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 100;
  padding-bottom: env(safe-area-inset-bottom);
}

.footer-item {
  flex: 1;
  max-width: 140px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  text-decoration: none;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.3s;
  cursor: pointer;
  position: relative;
  padding: 8px 4px;
}

.footer-item:hover {
  color: var(--text-secondary);
  transform: translateY(-2px);
}

.footer-item.active {
  color: var(--accent-purple);
}

.footer-item.active::after {
  content: '';
  position: absolute;
  bottom: 4px;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 3px;
  background: var(--gradient-primary);
  border-radius: 2px;
}

.footer-item.active .el-icon {
  filter: drop-shadow(0 0 8px rgba(139, 92, 246, 0.6));
}

/* 路由过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 响应式 */
@media (max-width: 768px) {
  .header-nav {
    display: none;
  }
  
  .header-inner {
    padding: 0 16px;
  }
  
  .app-main {
    padding-bottom: 100px;
  }
  
  .app-footer {
    height: 70px;
  }
  
  .footer-item {
    font-size: 11px;
    max-width: 80px;
    gap: 4px;
  }
  
  .footer-item.active::after {
    width: 20px;
    height: 2px;
  }
}
</style>
