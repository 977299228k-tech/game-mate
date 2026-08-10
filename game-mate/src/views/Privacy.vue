<template>
  <div class="privacy-page">
    <div class="page-container">
      <!-- 页面标题 -->
      <div class="page-header fade-in">
        <div class="header-icon">
          <el-icon :size="48"><Lock /></el-icon>
        </div>
        <h1>隐私保护中心</h1>
        <p class="header-desc">你的语音数据全程本地处理，绝不上传云端。安心游戏，零隐私顾虑。</p>
      </div>

      <!-- 隐私特性卡片 -->
      <div class="privacy-grid">
        <div
          v-for="(item, index) in privacyFeatures"
          :key="index"
          class="privacy-card"
        >
          <div class="card-icon" :style="{ background: item.color }">
            <span>{{ item.icon }}</span>
          </div>
          <div class="card-content">
            <h3>{{ item.title }}</h3>
            <p>{{ item.desc }}</p>
          </div>
        </div>
      </div>

      <!-- 数据管理 -->
      <div class="data-management">
        <h2>数据管理</h2>
        <div class="management-options">
          <div class="management-item">
            <div class="item-info">
              <h4>清除AI对话记录</h4>
              <p>删除所有AI对话内容和截图，不影响高光和游戏记录</p>
            </div>
            <button class="btn-danger" @click="clearChatData">
              <el-icon><Delete /></el-icon>
              清除对话
            </button>
          </div>
          <div class="management-item">
            <div class="item-info">
              <h4>清除游戏记录</h4>
              <p>删除游戏选择和截图记录，不影响对话和高光</p>
            </div>
            <button class="btn-danger" @click="clearGameRecords">
              <el-icon><Delete /></el-icon>
              清除记录
            </button>
          </div>
          <div class="management-item">
            <div class="item-info">
              <h4>清除高光记录</h4>
              <p>删除所有保存的高光时刻</p>
            </div>
            <button class="btn-danger" @click="clearHighlights">
              <el-icon><Delete /></el-icon>
              清除高光
            </button>
          </div>
          <div class="management-item">
            <div class="item-info">
              <h4>清除所有数据</h4>
              <p>清除AI对话、游戏记录、高光、设置配置，恢复初始状态</p>
            </div>
            <button class="btn-danger danger-all" @click="clearLocalData">
              <el-icon><Delete /></el-icon>
              清除所有数据
            </button>
          </div>
          <div class="management-item">
            <div class="item-info">
              <h4>自动清理</h4>
              <p>设置定期自动清理旧数据，保持存储空间整洁</p>
            </div>
            <el-switch
              v-model="autoClean"
              active-text="已开启"
              inactive-text="未开启"
            />
          </div>
          <div class="management-item">
            <div class="item-info">
              <h4>数据保存时长</h4>
              <p>选择本地数据的保留时长，过期自动清除</p>
            </div>
            <el-select v-model="retentionPeriod" class="period-select">
              <el-option label="30天" value="30" />
              <el-option label="90天" value="90" />
              <el-option label="180天" value="180" />
              <el-option label="永久保存" value="forever" />
            </el-select>
          </div>
        </div>
      </div>

      <!-- 隐私协议 -->
      <div class="agreement-section">
        <h2>隐私政策</h2>
        <div class="agreement-content">
          <div class="agreement-item" @click="toggleAgreement(0)">
            <div class="agreement-header">
              <span>数据收集说明</span>
              <el-icon :class="{ expanded: expandedIndex === 0 }"><ArrowRight /></el-icon>
            </div>
            <div v-if="expandedIndex === 0" class="agreement-body">
              <p>GameMate 仅收集必要的游戏数据以提供AI分析服务。所有语音和画面数据均在本地设备上处理，不会上传至任何服务器。我们不会收集您的个人身份信息，除非您主动提供。</p>
            </div>
          </div>
          <div class="agreement-item" @click="toggleAgreement(1)">
            <div class="agreement-header">
              <span>数据使用范围</span>
              <el-icon :class="{ expanded: expandedIndex === 1 }"><ArrowRight /></el-icon>
            </div>
            <div v-if="expandedIndex === 1" class="agreement-body">
              <p>收集的数据仅用于：1) 为您提供AI游戏分析和建议；2) 生成高光视频内容；3) 优化我们的服务质量。我们不会将您的数据用于任何其他目的，也不会与第三方共享。</p>
            </div>
          </div>
          <div class="agreement-item" @click="toggleAgreement(2)">
            <div class="agreement-header">
              <span>数据保护措施</span>
              <el-icon :class="{ expanded: expandedIndex === 2 }"><ArrowRight /></el-icon>
            </div>
            <div v-if="expandedIndex === 2" class="agreement-body">
              <p>我们采用业界标准的加密技术保护您的本地数据。所有敏感信息在存储前均已加密。应用程序采用最小权限原则，仅访问必要的系统资源。</p>
            </div>
          </div>
        </div>
        <div class="agreement-footer">
          <el-checkbox v-model="agreedToPolicy">
            我已阅读并同意《隐私政策》
          </el-checkbox>
        </div>
      </div>

      <!-- 联系我们 -->
      <div class="contact-section">
        <h2>隐私咨询</h2>
        <p>如有任何隐私相关问题，请联系我们的隐私保护团队</p>
        <div class="contact-options">
          <a href="mailto:privacy@gamemate.com" class="contact-card">
            <div class="contact-icon">📧</div>
            <h4>邮件咨询</h4>
            <span>privacy@gamemate.com</span>
          </a>
          <button class="contact-card" @click="showContactDialog = true">
            <div class="contact-icon">💬</div>
            <h4>在线客服</h4>
            <span>立即对话</span>
          </button>
        </div>
      </div>

      <!-- 清除数据确认弹窗 -->
      <el-dialog v-model="showConfirmDialog" title="确认清除" width="400px">
        <div class="confirm-content">
          <div class="confirm-icon">⚠️</div>
          <h3>{{ getConfirmText() }}</h3>
          <p>此操作将删除对应数据，且无法恢复。请谨慎操作。</p>
        </div>
        <template #footer>
          <el-button @click="showConfirmDialog = false">取消</el-button>
          <el-button type="danger" @click="confirmClearData">确认清除</el-button>
        </template>
      </el-dialog>

      <!-- 联系弹窗 -->
      <el-dialog v-model="showContactDialog" title="联系隐私保护团队" width="500px">
        <div class="contact-form">
          <el-input
            v-model="contactEmail"
            placeholder="您的邮箱"
            type="email"
          />
          <el-input
            v-model="contactSubject"
            placeholder="咨询主题"
          />
          <el-input
            v-model="contactMessage"
            type="textarea"
            :rows="4"
            placeholder="请描述您的隐私相关问题..."
          />
          <el-button type="primary" @click="sendContactMessage">发送消息</el-button>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAiChatStore } from '../stores/aiChatStore'

const router = useRouter()
const aiChatStore = useAiChatStore()

const expandedIndex = ref(-1)
const autoClean = ref(true)
const retentionPeriod = ref('90')
const agreedToPolicy = ref(true)
const showConfirmDialog = ref(false)
const showContactDialog = ref(false)
const contactEmail = ref('')
const contactSubject = ref('')
const contactMessage = ref('')
const clearTarget = ref('all')

const privacyFeatures = [
  {
    icon: '🔒',
    title: '本地语音处理',
    desc: 'AI语音识别在本地完成，原始语音不会离开你的电脑',
    color: 'linear-gradient(135deg, #6366f1, #8b5cf6)'
  },
  {
    icon: '👁️',
    title: '画面仅本机捕捉',
    desc: '屏幕截取仅用于本地分析，不存储、不传输',
    color: 'linear-gradient(135deg, #ec4899, #f43f5e)'
  },
  {
    icon: '🗑️',
    title: '一键消除数据',
    desc: '随时删除所有本地存储和游戏记忆数据',
    color: 'linear-gradient(135deg, #f59e0b, #ea580c)'
  },
  {
    icon: '📋',
    title: '透明数据政策',
    desc: '清晰告知数据流向，无隐藏条款',
    color: 'linear-gradient(135deg, #10b981, #059669)'
  }
]

const clearOptions = [
  { id: 'all', name: '所有数据', desc: '清除AI对话、游戏记录、高光、设置' },
  { id: 'chat', name: 'AI对话记录', desc: '清除所有AI对话内容和截图' },
  { id: 'game', name: '游戏记录', desc: '清除游戏选择和截图记录' },
  { id: 'highlights', name: '高光记录', desc: '清除所有保存的高光时刻' }
]

function toggleAgreement(index) {
  expandedIndex.value = expandedIndex.value === index ? -1 : index
}

function clearLocalData() {
  clearTarget.value = 'all'
  showConfirmDialog.value = true
}

function clearChatData() {
  clearTarget.value = 'chat'
  showConfirmDialog.value = true
}

function clearGameRecords() {
  clearTarget.value = 'game'
  showConfirmDialog.value = true
}

function clearHighlights() {
  clearTarget.value = 'highlights'
  showConfirmDialog.value = true
}

function confirmClearData() {
  showConfirmDialog.value = false
  
  switch (clearTarget.value) {
    case 'chat':
      aiChatStore.clearChatData()
      ElMessage.success('AI对话记录已清除')
      break
    case 'game':
      aiChatStore.clearGameRecords()
      ElMessage.success('游戏记录已清除')
      break
    case 'highlights':
      aiChatStore.clearHighlights()
      ElMessage.success('高光记录已清除')
      break
    case 'all':
    default:
      aiChatStore.clearAll()
      ElMessage.success('所有本地数据已清除')
      break
  }
}

function getConfirmText() {
  const option = clearOptions.find(o => o.id === clearTarget.value)
  if (!option) return ''
  return `确定要清除${option.name}吗？此操作不可恢复。`
}

function sendContactMessage() {
  if (!contactEmail.value || !contactSubject.value || !contactMessage.value) {
    ElMessage.warning('请填写完整信息')
    return
  }
  showContactDialog.value = false
  ElMessage.success('消息已发送，我们会尽快回复您')
  contactEmail.value = ''
  contactSubject.value = ''
  contactMessage.value = ''
}
</script>

<style scoped>
.privacy-page {
  padding: 32px 32px 120px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 页面头部 */
.page-header {
  text-align: center;
  margin-bottom: 56px;
}

.header-icon {
  width: 120px;
  height: 120px;
  background: var(--gradient-primary);
  border-radius: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 28px;
  color: white;
}

.page-header h1 {
  font-size: 48px;
  font-weight: 800;
  margin-bottom: 16px;
}

.header-desc {
  color: var(--text-secondary);
  font-size: 18px;
  max-width: 600px;
  margin: 0 auto;
}

/* 隐私特性网格 */
.privacy-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 28px;
  margin-bottom: 56px;
}

.privacy-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  padding: 32px;
  display: flex;
  gap: 20px;
  transition: all 0.3s;
}

.privacy-card:hover {
  border-color: var(--accent-purple);
  transform: translateY(-2px);
}

.card-icon {
  width: 72px;
  height: 72px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  flex-shrink: 0;
}

.card-content h3 {
  font-size: 20px;
  margin-bottom: 8px;
}

.card-content p {
  color: var(--text-secondary);
  font-size: 16px;
  line-height: 1.5;
}

/* 数据管理 */
.data-management {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 24px;
  padding: 40px;
  margin-bottom: 56px;
}

.data-management h2 {
  font-size: 26px;
  margin-bottom: 28px;
}

.management-options {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.management-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px;
  background: var(--bg-secondary);
  border-radius: 16px;
}

.item-info h4 {
  font-size: 19px;
  margin-bottom: 8px;
}

.item-info p {
  color: var(--text-secondary);
  font-size: 15px;
}

.btn-danger {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.3);
  padding: 14px 24px;
  border-radius: 12px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
}

.btn-danger:hover {
  background: rgba(239, 68, 68, 0.25);
}

.btn-danger.danger-all {
  background: rgba(220, 38, 38, 0.2);
  border-color: rgba(220, 38, 38, 0.5);
  color: #dc2626;
}

.btn-danger.danger-all:hover {
  background: rgba(220, 38, 38, 0.35);
}

.period-select {
  width: 140px;
}

/* 隐私协议 */
.agreement-section {
  margin-bottom: 56px;
}

.agreement-section h2 {
  font-size: 26px;
  margin-bottom: 24px;
}

.agreement-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.agreement-item {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  overflow: hidden;
  transition: border-color 0.3s;
}

.agreement-item:hover {
  border-color: var(--accent-purple);
}

.agreement-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  cursor: pointer;
  font-weight: 500;
  font-size: 17px;
}

.agreement-header .el-icon {
  transition: transform 0.3s;
}

.agreement-header .expanded {
  transform: rotate(90deg);
}

.agreement-body {
  padding: 0 24px 20px;
  color: var(--text-secondary);
  font-size: 16px;
  line-height: 1.6;
}

.agreement-footer {
  margin-top: 24px;
  padding: 20px;
  background: var(--bg-secondary);
  border-radius: 14px;
}

/* 联系我们 */
.contact-section {
  text-align: center;
}

.contact-section h2 {
  font-size: 26px;
  margin-bottom: 10px;
}

.contact-section > p {
  color: var(--text-secondary);
  font-size: 17px;
  margin-bottom: 32px;
}

.contact-options {
  display: flex;
  justify-content: center;
  gap: 28px;
}

.contact-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  padding: 36px;
  cursor: pointer;
  transition: all 0.3s;
  min-width: 200px;
  text-decoration: none;
  color: inherit;
}

.contact-card:hover {
  border-color: var(--accent-purple);
  transform: translateY(-2px);
}

.contact-icon {
  font-size: 48px;
  margin-bottom: 6px;
}

.contact-card h4 {
  font-size: 19px;
}

.contact-card span {
  color: var(--text-secondary);
  font-size: 15px;
}

/* 确认弹窗 */
.confirm-content {
  text-align: center;
  padding: 16px 0;
}

.confirm-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.confirm-content h3 {
  font-size: 18px;
  margin-bottom: 8px;
}

.confirm-content p {
  color: var(--text-secondary);
  font-size: 14px;
}

/* 联系表单 */
.contact-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* Element Plus 样式覆盖 */
:deep(.el-dialog) {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
}

:deep(.el-dialog__title) {
  color: var(--text-primary);
}

:deep(.el-dialog__headerbtn .el-dialog__close) {
  color: var(--text-secondary);
}

:deep(.el-input__wrapper) {
  background: var(--bg-secondary);
  box-shadow: none;
  border: 1px solid var(--border-color);
}

:deep(.el-input__wrapper:hover) {
  border-color: var(--accent-purple);
}

:deep(.el-input__wrapper.is-focus) {
  border-color: var(--accent-purple);
}

/* 响应式 */
@media (max-width: 768px) {
  .privacy-grid {
    grid-template-columns: 1fr;
  }
  
  .management-item {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }
  
  .contact-options {
    flex-direction: column;
  }
  
  .contact-card {
    flex-direction: row;
    justify-content: center;
    gap: 16px;
  }
}
</style>
