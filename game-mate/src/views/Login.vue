<template>
  <div class="login-page">
    <div class="bg-animation">
      <div class="bg-circle circle-1"></div>
      <div class="bg-circle circle-2"></div>
      <div class="bg-circle circle-3"></div>
    </div>

    <div class="login-container fade-in">
      <div class="login-card">
        <div class="login-header">
          <div class="logo-icon">GM</div>
          <h1>登录 GameMate</h1>
          <p class="subtitle">开始你的AI陪玩之旅</p>
        </div>

        <el-tabs v-model="activeTab" class="login-tabs">
          <el-tab-pane label="登录" name="login">
            <el-form :model="loginForm" @submit.prevent="handleLogin">
              <el-form-item>
                <el-input
                  v-model="loginForm.account"
                  placeholder="手机号/邮箱"
                  size="large"
                  :prefix-icon="User"
                />
              </el-form-item>
              <el-form-item>
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="密码"
                  size="large"
                  :prefix-icon="Lock"
                  show-password
                />
              </el-form-item>
              <el-button
                type="primary"
                size="large"
                class="submit-btn"
                :loading="loading"
                @click="handleLogin"
              >
                登录
              </el-button>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="注册" name="register">
            <el-form :model="registerForm" @submit.prevent="handleRegister">
              <el-form-item>
                <el-input
                  v-model="registerForm.nickname"
                  placeholder="昵称"
                  size="large"
                  :prefix-icon="User"
                />
              </el-form-item>
              <el-form-item>
                <el-input
                  v-model="registerForm.phone"
                  placeholder="手机号"
                  size="large"
                  :prefix-icon="Phone"
                  maxlength="11"
                />
              </el-form-item>
              <el-form-item>
                <el-input
                  v-model="registerForm.email"
                  placeholder="邮箱"
                  size="large"
                  :prefix-icon="Message"
                />
              </el-form-item>
              <el-form-item>
                <el-input
                  v-model="registerForm.password"
                  type="password"
                  placeholder="密码"
                  size="large"
                  :prefix-icon="Lock"
                  show-password
                />
              </el-form-item>
              <el-form-item>
                <el-input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  placeholder="确认密码"
                  size="large"
                  :prefix-icon="Lock"
                  show-password
                />
              </el-form-item>
              <el-button
                type="primary"
                size="large"
                class="submit-btn"
                :loading="loading"
                @click="handleRegister"
              >
                注册
              </el-button>
            </el-form>
          </el-tab-pane>
        </el-tabs>

        <div class="login-footer">
          <span @click="$router.push('/')">返回首页</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, Phone } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/userStore'
import { useAiChatStore } from '../stores/aiChatStore'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const aiChatStore = useAiChatStore()

const activeTab = ref('login')
const loading = ref(false)

const loginForm = reactive({
  account: '',
  password: ''
})

const registerForm = reactive({
  nickname: '',
  phone: '',
  email: '',
  password: '',
  confirmPassword: ''
})

async function handleLogin() {
  if (!loginForm.account || !loginForm.password) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  loading.value = true
  try {
    const result = await userStore.login(loginForm.account, loginForm.password)
    if (result.success) {
      // 先重置aiChatStore到默认状态
      aiChatStore.resetToDefault()
      
      // 从后端加载用户数据
      const backendData = await userStore.loadUserData()
      if (backendData) {
        aiChatStore.loadFromBackend(backendData)
      }
      
      if (result.gift) {
        ElMessage.success(`登录成功！新用户福利：赠送${result.giftHours}小时体验时长`)
      } else {
        ElMessage.success('登录成功')
      }
      const redirect = route.query.redirect || '/home'
      router.push(redirect)
    } else {
      ElMessage.error(result.message || '登录失败')
    }
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  if (!registerForm.nickname || !registerForm.phone || !registerForm.email || !registerForm.password || !registerForm.confirmPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(registerForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }

  if (registerForm.password !== registerForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  
  loading.value = true
  try {
    const result = await userStore.register(
      registerForm.nickname,
      registerForm.phone,
      registerForm.email,
      registerForm.password,
      registerForm.confirmPassword
    )
    if (result.success) {
      // 重置aiChatStore到默认状态
      aiChatStore.resetToDefault()
      
      // 从后端加载用户数据
      const backendData = await userStore.loadUserData()
      if (backendData) {
        aiChatStore.loadFromBackend(backendData)
      }
      
      ElMessage.success('注册成功，赠送2小时体验时长！')
      router.push('/home')
    } else {
      ElMessage.error(result.message || '注册失败')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  overflow: hidden;
}

.bg-animation {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.3;
}

.circle-1 {
  width: 400px;
  height: 400px;
  background: var(--accent-purple);
  top: -100px;
  left: -100px;
  animation: float 20s ease-in-out infinite;
}

.circle-2 {
  width: 300px;
  height: 300px;
  background: var(--accent-blue);
  bottom: -50px;
  right: -50px;
  animation: float 25s ease-in-out infinite reverse;
}

.circle-3 {
  width: 250px;
  height: 250px;
  background: var(--accent-pink);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: float 30s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0);
  }
  50% {
    transform: translate(50px, 50px);
  }
}

.login-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  padding: 20px;
}

.login-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 24px;
  padding: 40px 32px;
  backdrop-filter: blur(20px);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-icon {
  width: 56px;
  height: 56px;
  background: var(--gradient-primary);
  border-radius: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 20px;
  margin-bottom: 16px;
}

.login-header h1 {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 8px;
}

.subtitle {
  color: var(--text-secondary);
  font-size: 14px;
}

.login-tabs {
  --el-tabs-nav-wrap-border-color: transparent;
  --el-tabs-active-color: var(--accent-purple);
}

.login-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.login-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
}

.login-tabs :deep(.el-tabs__active-bar) {
  background: var(--gradient-primary);
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  margin-top: 16px;
  background: var(--gradient-primary);
  border: none;
}

.submit-btn:hover {
  opacity: 0.9;
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--border-color);
}

.login-footer span {
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 14px;
  transition: color 0.3s;
}

.login-footer span:hover {
  color: var(--accent-purple);
}

/* 确保body没有边距 */
:deep(html),
:deep(body) {
  margin: 0;
  padding: 0;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
}

/* Element Plus 样式覆盖 */
:deep(.el-input__wrapper) {
  background-color: var(--bg-secondary);
  box-shadow: 0 0 0 1px var(--border-color) inset;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--accent-purple) inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--accent-purple) inset;
}

:deep(.el-input__inner) {
  color: var(--text-primary);
}

:deep(.el-input__inner::placeholder) {
  color: var(--text-muted);
}

:deep(.el-input__prefix .el-icon) {
  color: var(--text-muted);
}
</style>
