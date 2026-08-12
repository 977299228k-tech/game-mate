<template>
  <div class="settings-page">
    <div class="page-wrapper">
      <!-- AI人格 -->
      <section class="settings-section">
        <h3 class="section-title">
          AI人格
          <span class="vip-hint" v-if="!hasPersonalityCustom">
            <el-icon><Lock /></el-icon>
            充值「专属AI性格定制」解锁更多人格
          </span>
        </h3>
        <div class="personality-grid">
          <div
            v-for="p in personalities"
            :key="p.id"
            class="personality-item"
            :class="{ 
              active: selectedPersonality === p.key,
              locked: !p.isDefault && !hasPersonalityCustom 
            }"
            @click="updatePersonality(p)"
          >
            <span class="p-emoji">{{ p.emoji }}</span>
            <span class="p-name">{{ p.name }}</span>
            <span class="lock-icon" v-if="!p.isDefault && !hasPersonalityCustom">🔒</span>
          </div>
          <div 
            class="personality-item custom" 
            :class="{ locked: !hasPersonalityCustom }"
            @click="handleCustomClick"
          >
            <span class="p-emoji">✨</span>
            <span class="p-name">自定义人格</span>
            <span class="lock-icon" v-if="!hasPersonalityCustom">🔒</span>
          </div>
        </div>
      </section>

      <!-- 声线选择 -->
      <section class="settings-section">
        <h3 class="section-title">
          声线选择
          <span class="vip-hint" v-if="!hasVoiceCustom">
            <el-icon><Lock /></el-icon>
            充值「声优声线定制」解锁更多声线
          </span>
        </h3>
        <div class="voice-grid">
          <div
            v-for="v in voices"
            :key="v.id"
            class="voice-item"
            :class="{ 
              active: selectedVoice === v.key,
              locked: !v.isDefault && !hasVoiceCustom 
            }"
            @click="updateVoice(v)"
          >
            <span class="voice-emoji">{{ v.emoji }}</span>
            <span class="voice-name">{{ v.name }}</span>
            <span class="lock-icon" v-if="!v.isDefault && !hasVoiceCustom">🔒</span>
          </div>
        </div>
      </section>

      <!-- 功能设置 -->
      <section class="settings-section">
        <h3 class="section-title">功能设置</h3>
        <div class="setting-list">
          <div
            v-for="feature in features"
            :key="feature.key"
            class="setting-item"
          >
            <div class="setting-info">
              <div class="setting-icon">{{ feature.icon }}</div>
              <div class="setting-desc">
                <h4>{{ feature.title }}</h4>
                <p>{{ feature.description }}</p>
              </div>
            </div>
            <el-switch
              v-model="feature.enabled"
              @change="toggleFeature(feature.key)"
            />
          </div>
        </div>
      </section>

      <!-- 隐私与安全 -->
      <section class="settings-section">
        <h3 class="section-title">隐私与安全</h3>
        <div class="setting-list">
          <div class="setting-item">
            <div class="setting-info">
              <div class="setting-icon">🔒</div>
              <div class="setting-desc">
                <h4>本地语音处理</h4>
                <p>语音数据本地处理，不上传云端</p>
              </div>
            </div>
            <el-switch v-model="privacy.localProcess" />
          </div>
          <div class="setting-item clickable" @click="clearData">
            <div class="setting-info">
              <div class="setting-icon">🗑️</div>
              <div class="setting-desc">
                <h4>清除保存数据</h4>
                <p>删除本地存储的游戏记忆和偏好</p>
              </div>
            </div>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
      </section>

      <!-- 退出登录 -->
      <div class="logout-section">
        <button class="btn-logout" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          退出登录
        </button>
      </div>
    </div>

    <!-- 自定义人格弹窗 -->
    <el-dialog v-model="showCustomDialog" title="自定义人格" width="400px">
      <el-form :model="customForm" label-position="top">
        <el-form-item label="人格名称">
          <el-input v-model="customForm.name" placeholder="输入人格名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="customForm.desc" type="textarea" :rows="3" placeholder="描述这个人格的特点" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCustomDialog = false">取消</el-button>
        <el-button type="primary" @click="saveCustom">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Lock, ArrowRight, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/userStore'
import { useAiChatStore } from '../stores/aiChatStore'
import { useGameStore } from '../stores/gameStore'

const router = useRouter()
const userStore = useUserStore()
const aiChatStore = useAiChatStore()
const gameStore = useGameStore()

const showCustomDialog = ref(false)

const customForm = reactive({
  name: '',
  desc: ''
})

const personalities = [
  { id: 1, key: 'friendly', name: '温柔治愈', emoji: '🌸', isDefault: true },
  { id: 2, key: 'passionate', name: '激励热血', emoji: '🔥', isDefault: false },
  { id: 3, key: 'roaster', name: '犀利互怼', emoji: '😈', isDefault: false },
  { id: 4, key: 'funny', name: '幽默风趣', emoji: '😂', isDefault: false },
  { id: 5, key: 'strategist', name: '冷静分析', emoji: '🧊', isDefault: false }
]

const voices = [
  { id: 1, key: 'default', name: '甜美女声', emoji: '👩', isDefault: true },
  { id: 2, key: 'mature', name: '御姐声线', emoji: '👩‍🦰', isDefault: false },
  { id: 3, key: 'child', name: '正太声线', emoji: '👦', isDefault: false },
  { id: 4, key: 'fox', name: '火狐声线', emoji: '🦊', isDefault: false },
  { id: 5, key: 'electronic', name: '电子音', emoji: '🤖', isDefault: false },
  { id: 6, key: 'custom', name: '自定义声线', emoji: '✨', isDefault: false }
]

const hasPersonalityCustom = computed(() => {
  return aiChatStore.hasEnoughBalance('personalityCustom')
})

const hasVoiceCustom = computed(() => {
  return aiChatStore.hasEnoughBalance('voiceCustom')
})

const selectedPersonality = computed({
  get: () => aiChatStore.settings.personality || 'friendly',
  set: (val) => aiChatStore.updateSettings({ personality: val })
})

const selectedVoice = computed({
  get: () => aiChatStore.settings.voice || 'default',
  set: (val) => aiChatStore.updateSettings({ voice: val })
})

const features = reactive([
  {
    key: 'memory',
    icon: '🧠',
    title: 'AI记忆系统',
    description: '记住你的游戏习惯和偏好',
    enabled: true
  },
  {
    key: 'emotion',
    icon: '💜',
    title: '情感互动',
    description: '击杀夸奖、失误鼓励',
    enabled: true
  },
  {
    key: 'tactic',
    icon: '⚡',
    title: '实时战术提示',
    description: '关键时刻语音提醒策略',
    enabled: true
  },
  {
    key: 'guide',
    icon: '🎯',
    title: '语言攻略',
    description: '遇阻自动提示攻略',
    enabled: true
  }
])

const privacy = reactive({
  localProcess: true
})

function updatePersonality(p) {
  if (!p.isDefault && !hasPersonalityCustom.value) {
    ElMessage.warning('请先充值「专属AI性格定制」解锁更多人格')
    return
  }
  selectedPersonality.value = p.key
  aiChatStore.updateSettings({ personality: p.key })
  ElMessage.success(`已切换为${p.name}人格`)
}

function updateVoice(v) {
  if (!v.isDefault && !hasVoiceCustom.value) {
    ElMessage.warning('请先充值「声优声线定制」解锁更多声线')
    return
  }
  selectedVoice.value = v.key
  aiChatStore.updateSettings({ voice: v.key })
  ElMessage.success(`已切换为${v.name}`)
}

function handleCustomClick() {
  if (!hasPersonalityCustom.value) {
    ElMessage.warning('请先充值「专属AI性格定制」解锁自定义人格')
    return
  }
  showCustomDialog.value = true
}

function toggleFeature(key) {
  const feature = features.find(f => f.key === key)
  if (feature) {
    aiChatStore.updateSettings({ [`${key}Enabled`]: feature.enabled })
  }
}

onMounted(() => {
  const settings = aiChatStore.settings
  if (settings.personality) {
    const personality = personalities.find(p => p.key === settings.personality)
    if (personality) {
      const canUse = personality.isDefault || hasPersonalityCustom.value
      if (!canUse) {
        aiChatStore.updateSettings({ personality: 'friendly' })
      }
    }
  }
  if (settings.voice) {
    const voice = voices.find(v => v.key === settings.voice)
    if (voice) {
      const canUse = voice.isDefault || hasVoiceCustom.value
      if (!canUse) {
        aiChatStore.updateSettings({ voice: 'default' })
      }
    }
  }
})

function saveCustom() {
  if (!customForm.name) {
    ElMessage.warning('请输入人格名称')
    return
  }
  personalities.push({
    id: personalities.length + 1,
    key: `custom-${Date.now()}`,
    name: customForm.name,
    emoji: '✨',
    isDefault: false
  })
  showCustomDialog.value = false
  customForm.name = ''
  customForm.desc = ''
  ElMessage.success('人格已添加')
}

function clearData() {
  ElMessageBox.confirm('确定要清除本地保存数据吗？', '提示', {
    confirmButtonText: '确定清除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    aiChatStore.clearAll()
    gameStore.reset()
    ElMessage.success('数据已清除')
  }).catch(() => {})
}

function handleLogout() {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定退出',
    cancelButtonText: '取消'
  }).then(() => {
    aiChatStore.resetToDefault()
    gameStore.reset()
    userStore.logout()
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.settings-page {
  padding: 24px 32px 120px;
  max-width: 1000px;
  margin: 0 auto;
}

.page-wrapper {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

/* 设置区块 */
.settings-section {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 18px;
  padding: 24px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 12px;
}

.vip-hint {
  font-size: 13px;
  font-weight: 400;
  color: #f59e0b;
  display: flex;
  align-items: center;
  gap: 4px;
}

.vip-hint .el-icon {
  font-size: 14px;
}

/* 人格选择 */
.personality-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.personality-item {
  background: var(--bg-secondary);
  border: 1px solid transparent;
  border-radius: 14px;
  padding: 20px 14px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.personality-item:hover {
  border-color: var(--border-color);
}

.personality-item.active {
  border-color: var(--accent-purple);
  background: rgba(139, 92, 246, 0.15);
}

.personality-item.custom {
  border-style: dashed;
}

.p-emoji {
  font-size: 36px;
}

.p-name {
  font-size: 15px;
  color: var(--text-secondary);
}

.personality-item.active .p-name {
  color: var(--text-primary);
}

.personality-item.locked,
.voice-item.locked {
  opacity: 0.6;
  cursor: not-allowed;
  position: relative;
}

.personality-item.locked:hover,
.voice-item.locked:hover {
  border-color: transparent;
}

.lock-icon {
  position: absolute;
  top: 8px;
  right: 8px;
  font-size: 14px;
  opacity: 0.7;
}

/* 声线选择 */
.voice-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.voice-item {
  background: var(--bg-secondary);
  border: 1px solid transparent;
  border-radius: 14px;
  padding: 16px 18px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.voice-item:hover {
  border-color: var(--border-color);
}

.voice-item.active {
  border-color: var(--accent-purple);
  background: rgba(139, 92, 246, 0.15);
}

.voice-emoji {
  font-size: 28px;
}

.voice-name {
  font-size: 15px;
  flex: 1;
}

/* 设置列表 */
.setting-list {
  display: flex;
  flex-direction: column;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 0;
  border-bottom: 1px solid var(--border-color);
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-item.clickable {
  cursor: pointer;
}

.setting-item.clickable:hover {
  opacity: 0.8;
}

.setting-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.setting-icon {
  font-size: 28px;
}

.setting-desc h4 {
  font-size: 18px;
  margin-bottom: 4px;
}

.setting-desc p {
  color: var(--text-secondary);
  font-size: 14px;
}

.arrow-icon {
  color: var(--text-muted);
}

/* 退出登录 */
.logout-section {
  text-align: center;
  margin-top: 16px;
}

.btn-logout {
  background: transparent;
  border: 1px solid rgba(220, 38, 38, 0.5);
  color: #ef4444;
  padding: 16px 40px;
  border-radius: 14px;
  font-size: 17px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  transition: all 0.2s;
}

.btn-logout:hover {
  background: rgba(220, 38, 38, 0.1);
  border-color: #ef4444;
}

/* Element Plus 样式覆盖 */
:deep(.el-dialog) {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
}

:deep(.el-dialog__title) {
  color: var(--text-primary);
}

/* 响应式 */
@media (max-width: 600px) {
  .personality-grid,
  .voice-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
