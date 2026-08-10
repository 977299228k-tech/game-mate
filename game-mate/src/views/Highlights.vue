<template>
  <div class="highlights-page">
    <div class="page-container">
      <!-- 页面标题 -->
      <div class="page-header fade-in">
        <div class="header-title">
          <span class="title-icon">🎬</span>
          <h1>精彩高光</h1>
        </div>
        <p class="header-desc">从AI对话界面保存高光时刻，一键分享你的精彩操作</p>
      </div>

      <!-- 高光统计 -->
      <div class="stats-bar" v-if="highlights.length > 0">
        <div class="stat-card">
          <span class="stat-value">{{ highlights.length }}</span>
          <span class="stat-label">高光总数</span>
        </div>
        <div class="stat-card">
          <span class="stat-value">{{ gameCount }}</span>
          <span class="stat-label">涉及游戏</span>
        </div>
        <div class="stat-card">
          <span class="stat-value">{{ todayCount }}</span>
          <span class="stat-label">今日新增</span>
        </div>
      </div>

      <!-- 高光网格 -->
      <div class="highlights-grid" v-if="highlights.length > 0">
        <div
          v-for="item in highlights"
          :key="item.id"
          class="highlight-card"
          @click="viewHighlight(item)"
        >
          <div class="thumbnail-wrapper">
            <img 
              v-if="item.screenshot" 
              :src="item.screenshot" 
              :alt="item.title"
              class="highlight-thumbnail"
            />
            <div v-else class="thumbnail-bg" :style="{ background: item.color }">
              <span class="game-icon">{{ item.icon }}</span>
            </div>
            <div class="play-overlay">
              <div class="play-btn">
                <el-icon :size="24"><View /></el-icon>
              </div>
            </div>
            <div class="duration-badge">{{ item.duration || '高光' }}</div>
          </div>
          <div class="highlight-info">
            <h3>{{ item.title }}</h3>
            <p class="highlight-desc" v-if="item.description">{{ item.description }}</p>
            <div class="highlight-meta">
              <span class="game-tag" v-if="item.gameName">{{ item.gameName }}</span>
              <span class="time-tag">{{ item.time }}</span>
            </div>
          </div>
          <div class="highlight-actions">
            <button class="action-btn" @click.stop="shareHighlight(item)">
              <el-icon><Share /></el-icon>
            </button>
            <button class="action-btn danger" @click.stop="deleteHighlight(item)">
              <el-icon><Delete /></el-icon>
            </button>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-icon">🎮</div>
        <h3>暂无高光记录</h3>
        <p>在AI对话界面点击"高光"按钮，保存精彩截图</p>
        <router-link to="/ai-chat" class="btn-primary">
          <el-icon><VideoCamera /></el-icon>
          开始捕获高光
        </router-link>
      </div>

      <!-- 查看高光弹窗 -->
      <el-dialog v-model="showViewDialog" title="高光详情" width="600px" :show-close="true">
        <div class="highlight-view">
          <div class="view-thumbnail">
            <img 
              v-if="currentHighlight?.screenshot" 
              :src="currentHighlight.screenshot" 
              :alt="currentHighlight?.title"
            />
            <div v-else class="view-placeholder" :style="{ background: currentHighlight?.color }">
              <span>{{ currentHighlight?.icon }}</span>
            </div>
          </div>
          <div class="view-info">
            <h3>{{ currentHighlight?.title }}</h3>
            <p class="view-desc" v-if="currentHighlight?.description">{{ currentHighlight.description }}</p>
            <div class="view-meta">
              <span v-if="currentHighlight?.gameName">🎮 {{ currentHighlight.gameName }}</span>
              <span>{{ currentHighlight?.time }}</span>
            </div>
          </div>
          <div class="view-actions">
            <el-button type="primary" @click="shareHighlight(currentHighlight)">
              <el-icon><Share /></el-icon>
              分享
            </el-button>
            <el-button @click="showViewDialog = false">关闭</el-button>
          </div>
        </div>
      </el-dialog>

      <!-- 分享弹窗 -->
      <el-dialog v-model="showShareDialog" title="分享高光" width="400px" :show-close="true">
        <div class="share-content">
          <div class="share-preview">
            <div class="preview-thumbnail" :style="{ background: currentHighlight?.color }">
              <span>{{ currentHighlight?.icon }}</span>
            </div>
            <div class="preview-info">
              <h4>{{ currentHighlight?.title }}</h4>
              <p>{{ currentHighlight?.gameName }}</p>
            </div>
          </div>
          
          <div class="share-options">
            <div class="share-option" @click="shareTo('link')">
              <div class="option-icon">🔗</div>
              <span>复制链接</span>
            </div>
            <div class="share-option" @click="shareTo('wechat')">
              <div class="option-icon" style="background: #07c160;">💚</div>
              <span>微信</span>
            </div>
            <div class="share-option" @click="shareTo('qq')">
              <div class="option-icon" style="background: #1296db;">💙</div>
              <span>QQ</span>
            </div>
            <div class="share-option" @click="shareTo('local')">
              <div class="option-icon" style="background: var(--accent-purple);">⬇️</div>
              <span>保存本地</span>
            </div>
          </div>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, Share, Delete, VideoCamera } from '@element-plus/icons-vue'
import { useAiChatStore } from '../stores/aiChatStore'

const aiChatStore = useAiChatStore()

const showShareDialog = ref(false)
const showViewDialog = ref(false)
const currentHighlight = ref(null)

const highlights = computed(() => aiChatStore.highlights)

const gameCount = computed(() => {
  const games = new Set()
  highlights.value.forEach(h => {
    if (h.gameName) games.add(h.gameName)
  })
  return games.size
})

const todayCount = computed(() => {
  const today = new Date().toLocaleDateString('zh-CN')
  return highlights.value.filter(h => h.time?.includes(today)).length
})

function viewHighlight(item) {
  currentHighlight.value = item
  showViewDialog.value = true
}

function shareHighlight(item) {
  currentHighlight.value = item
  showShareDialog.value = true
}

function deleteHighlight(item) {
  ElMessageBox.confirm(`确定要删除"${item.title}"吗？`, '提示', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    aiChatStore.removeHighlight(item.id)
    ElMessage.success('高光已删除')
  }).catch(() => {})
}

function shareTo(platform) {
  showShareDialog.value = false
  
  const messages = {
    link: '链接已复制到剪贴板',
    wechat: '正在打开微信分享...',
    qq: '正在打开QQ分享...',
    local: '已保存到本地'
  }
  
  ElMessage.success(messages[platform])
}
</script>

<style scoped>
.highlights-page {
  padding: 32px 32px 120px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 48px;
}

.header-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 16px;
}

.title-icon {
  font-size: 56px;
}

.header-title h1 {
  font-size: 52px;
  font-weight: 800;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-desc {
  color: var(--text-secondary);
  font-size: 18px;
}

.stats-bar {
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-bottom: 32px;
}

.stat-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 16px 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-value {
  font-size: 32px;
  font-weight: 800;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
}

/* 高光网格 */
.highlights-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 28px;
}

.highlight-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.highlight-card:hover {
  border-color: var(--accent-purple);
  transform: translateY(-4px);
  box-shadow: var(--shadow-glow);
}

.thumbnail-wrapper {
  position: relative;
  width: 100%;
  height: 280px;
  overflow: hidden;
}

.highlight-thumbnail {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumbnail-bg {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.game-icon {
  font-size: 120px;
  opacity: 0.3;
}

.play-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.highlight-card:hover .play-overlay {
  opacity: 1;
}

.play-btn {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent-purple);
}

.duration-badge {
  position: absolute;
  bottom: 16px;
  right: 16px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 6px 14px;
  border-radius: 10px;
  font-size: 14px;
}

.highlight-info {
  padding: 24px;
}

.highlight-info h3 {
  font-size: 20px;
  margin-bottom: 10px;
}

.highlight-meta {
  display: flex;
  gap: 14px;
  font-size: 15px;
  color: var(--text-secondary);
}

.game-tag {
  background: rgba(139, 92, 246, 0.15);
  color: var(--accent-purple);
  padding: 4px 12px;
  border-radius: 12px;
}

.highlight-desc {
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.highlight-actions {
  display: flex;
  gap: 8px;
  padding: 0 24px 24px;
}

.action-btn {
  flex: 1;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  padding: 10px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  border-color: var(--accent-purple);
  color: var(--accent-purple);
}

.action-btn.danger:hover {
  border-color: #ef4444;
  color: #ef4444;
}

/* 高光查看弹窗 */
.highlight-view {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.view-thumbnail {
  width: 100%;
  max-height: 400px;
  border-radius: 12px;
  overflow: hidden;
  background: #000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.view-thumbnail img {
  max-width: 100%;
  max-height: 400px;
  object-fit: contain;
  display: block;
}

.view-placeholder {
  width: 100%;
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 80px;
}

.view-info h3 {
  font-size: 22px;
  margin-bottom: 8px;
}

.view-desc {
  color: var(--text-secondary);
  margin-bottom: 12px;
}

.view-meta {
  display: flex;
  gap: 16px;
  color: var(--text-muted);
  font-size: 14px;
}

.view-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 120px 20px;
}

.empty-icon {
  font-size: 96px;
  margin-bottom: 28px;
}

.empty-state h3 {
  font-size: 28px;
  margin-bottom: 12px;
}

.empty-state p {
  color: var(--text-secondary);
  font-size: 18px;
  margin-bottom: 36px;
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  background: var(--gradient-primary);
  color: white;
  padding: 20px 40px;
  border-radius: 16px;
  font-weight: 600;
  text-decoration: none;
  border: none;
  cursor: pointer;
  font-size: 18px;
}

/* 分享弹窗 */
.share-content {
  padding: 8px 0;
}

.share-preview {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  padding: 12px;
  background: var(--bg-secondary);
  border-radius: 12px;
}

.preview-thumbnail {
  width: 80px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.preview-info h4 {
  font-size: 14px;
  margin-bottom: 4px;
}

.preview-info p {
  color: var(--text-secondary);
  font-size: 12px;
}

.share-options {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.share-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: transform 0.2s;
}

.share-option:hover {
  transform: scale(1.05);
}

.option-icon {
  width: 48px;
  height: 48px;
  background: var(--bg-secondary);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.share-option span {
  font-size: 12px;
  color: var(--text-secondary);
}

/* 视频播放器 */
.video-player {
  aspect-ratio: 16/9;
  background: var(--bg-secondary);
  border-radius: 12px;
  overflow: hidden;
}

.player-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: linear-gradient(135deg, #1e1e38, #252542);
}

.placeholder-icon {
  font-size: 48px;
}

.player-controls {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.control-btn {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  padding: 10px 20px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.control-btn.primary {
  background: var(--accent-purple);
  border-color: var(--accent-purple);
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

/* 响应式 */
@media (max-width: 768px) {
  .highlights-grid {
    grid-template-columns: 1fr;
  }
  
  .video-thumbnail {
    height: 180px;
  }
  
  .share-options {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
