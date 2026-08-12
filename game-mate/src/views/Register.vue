<template>
  <div class="register-page">
    <div class="register-container fade-in">
      <div class="register-card">
        <div class="register-header">
          <div class="logo-icon">GM</div>
          <h1>注册 GameMate</h1>
          <p class="subtitle">加入AI陪玩时代</p>
        </div>

        <el-form :model="form" label-position="top" class="register-form">
          <el-form-item label="昵称">
            <el-input
              v-model="form.nickname"
              placeholder="输入你的昵称"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input
              v-model="form.email"
              placeholder="输入你的邮箱"
              size="large"
              :prefix-icon="Message"
            />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input
              v-model="form.phone"
              placeholder="输入手机号"
              size="large"
              :prefix-icon="Phone"
            />
          </el-form-item>
          <el-form-item label="密码">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="设置密码"
              size="large"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="再次输入密码"
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
            创建账号
          </el-button>
        </el-form>

        <div class="register-footer">
          <span>已有账号？</span>
          <router-link to="/login" class="link">立即登录</router-link>
        </div>

        <div class="benefits">
          <h4>🎁 新用户福利</h4>
          <ul>
            <li>✓ 注册即送2小时免费体验</li>
            <li>✓ 7×24小时在线AI陪玩</li>
            <li>✓ 支持400+游戏攻略</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, Phone } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/userStore'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)

const form = reactive({
  nickname: '',
  phone: '',
  email: '',
  password: '',
  confirmPassword: ''
})

async function handleRegister() {
  if (!form.nickname || !form.phone || !form.email || !form.password || !form.confirmPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }

  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  
  if (form.password !== form.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  
  loading.value = true
  try {
    const result = await userStore.register(
      form.nickname,
      form.phone,
      form.email,
      form.password,
      form.confirmPassword
    )
    if (result.success) {
      ElMessage.success('注册成功，赠送2小时体验时长！')
      router.push('/ai-chat')
    } else {
      ElMessage.error(result.message || '注册失败')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-primary);
  padding: 20px;
}

.register-container {
  width: 100%;
  max-width: 440px;
}

.register-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 24px;
  padding: 36px 32px;
}

.register-header {
  text-align: center;
  margin-bottom: 28px;
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

.register-header h1 {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 8px;
}

.subtitle {
  color: var(--text-secondary);
  font-size: 14px;
}

.register-form {
  margin-bottom: 20px;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: var(--gradient-primary);
  border: none;
}

.register-footer {
  text-align: center;
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 24px;
}

.link {
  color: var(--accent-purple);
  text-decoration: none;
  margin-left: 6px;
}

.link:hover {
  text-decoration: underline;
}

.benefits {
  background: var(--bg-secondary);
  border-radius: 12px;
  padding: 16px;
}

.benefits h4 {
  font-size: 14px;
  margin-bottom: 10px;
  color: var(--accent-yellow);
}

.benefits ul {
  list-style: none;
  padding: 0;
}

.benefits li {
  color: var(--text-secondary);
  font-size: 13px;
  padding: 4px 0;
}

/* Element Plus 样式覆盖 */
:deep(.el-form-item__label) {
  color: var(--text-secondary);
}

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
