<template>
  <div class="ai-chat-page">
    <!-- 顶部状态栏 -->
    <div class="status-bar" v-if="selectedGame">
      <div class="status-left">
        <span class="status-label">待机中</span>
        <span class="status-sep">·</span>
        <span class="status-game">{{ selectedGame.name }}</span>
      </div>
      <div class="status-metrics">
        <span>AI延迟 <strong>{{ latency }}ms</strong></span>
        <span>识别率 <strong>{{ recognitionRate }}%</strong></span>
        <span>识别对象 <strong>{{ objectCount }}</strong></span>
      </div>
    </div>
    <div class="status-bar empty" v-else>
      <span class="status-hint">选择游戏后开始使用</span>
    </div>

    <div class="chat-layout">
      <!-- 左侧游戏选择 -->
      <aside class="game-sidebar">
        <div class="sidebar-title">
          <h3>选择游戏画面源</h3>
          <span class="game-count">{{ gameStore.games.length }}款</span>
        </div>
        <div class="game-list-wrapper">
        
        <div class="game-list">
          <div
            v-for="game in gameStore.games"
            :key="game.id"
            class="game-item"
            :class="{ active: selectedGame?.id === game.id, custom: game.isCustom }"
            @click="selectGame(game)"
          >
            <div class="game-icon" :style="{ background: game.color + '20' }">
              <img 
                v-if="game.imageUrl && !imageLoadFailed[game.id]" 
                :src="game.imageUrl" 
                :alt="game.name" 
                class="game-icon-img"
                @error="handleImageError($event, game)" 
              />
              <span v-else>{{ game.icon }}</span>
            </div>
            <div class="game-info">
              <span class="game-name">
                {{ game.name }}
                <span v-if="game.isCustom" class="custom-tag">自定义</span>
              </span>
              <span class="game-tags">{{ game.genre }}{{ game.tags ? ' · ' + game.tags.split(',').slice(0, 2).join('、') : '' }}</span>
            </div>
            <div v-if="game.isCustom" class="game-delete" @click.stop="handleDeleteGame(game)">
              <el-icon :size="16"><Delete /></el-icon>
            </div>
          </div>
        </div>
        <div class="add-game-btn" @click="showAddGameDialog = true">
          <el-icon :size="20"><Plus /></el-icon>
          <span>添加游戏</span>
        </div>
        </div>
      </aside>

      <!-- 主内容区 -->
      <main class="chat-main">
        <!-- 画面分析区 -->
        <div class="screen-analysis">
          <div class="analysis-area" :class="{ active: isCapturing, 'has-screenshot': currentScreenshot }">
            <div v-if="!isCapturing && !currentScreenshot" class="placeholder">
              <div class="placeholder-icon">🎮</div>
              <h3>AI游戏助手</h3>
              <p>选择左侧游戏后，可直接提问获取游戏攻略/技巧，也可截取或上传游戏画面进行实时分析</p>
              <div class="capture-options">
                <button class="btn-capture" @click="startScreenCapture" :disabled="!selectedGame">
                  <el-icon><VideoCamera /></el-icon>
                  截取屏幕
                </button>
                <label class="btn-upload" :class="{ disabled: !selectedGame }">
                  <el-icon><UploadFilled /></el-icon>
                  上传截图
                  <input type="file" accept="image/*" @change="handleScreenshotUpload" style="display: none" :disabled="!selectedGame" />
                </label>
              </div>
              <div class="no-screenshot-tip" v-if="selectedGame">
                <el-icon><InfoFilled /></el-icon>
                <span>💡 当前已选择《{{ selectedGame.name }}》，无需截图也可直接提问游戏相关问题</span>
              </div>
            </div>
            
            <div v-else-if="isCapturing" class="capturing">
              <div class="capture-badge">
                <span class="rec-dot"></span>
                REC · 正在捕获
              </div>
              <div v-if="currentScreenshot" class="screenshot-display">
                <img :src="currentScreenshot" alt="游戏截图" class="screenshot-img" />
                <div class="screenshot-actions">
                  <button class="ctrl-btn primary" @click="analyzeScreenshot" :disabled="isAnalyzing">
                    <el-icon><View /></el-icon>
                    {{ isAnalyzing ? `AI分析中(${analyzingSeconds}s)` : 'AI分析' }}
                  </button>
                  <button class="ctrl-btn" @click="captureNow">
                    <el-icon><VideoCamera /></el-icon>
                    重新截图
                  </button>
                  <button class="ctrl-btn" @click="clearScreenshot">
                    <el-icon><Delete /></el-icon>
                    清除
                  </button>
                </div>
              </div>
              <div v-else class="game-visual" :style="{ background: selectedGame?.color }">
                <img 
                  v-if="selectedGame?.imageUrl && selectedGame?.id && !imageLoadFailed[selectedGame.id]" 
                  :src="selectedGame.imageUrl" 
                  :alt="selectedGame?.name" 
                  class="visual-img"
                  @error="handleImageError($event, selectedGame)"
                />
                <span v-else class="visual-emoji">{{ selectedGame?.icon }}</span>
              </div>
              <div class="capture-controls">
                <button class="ctrl-btn" @click="stopCapture">
                  <el-icon><VideoPause /></el-icon>
                  停止捕获
                </button>
                <button class="ctrl-btn primary" @click="captureNow">
                  <el-icon><VideoCamera /></el-icon>
                  立即截图
                </button>
                <button class="ctrl-btn" @click="showSettings = true">
                  <el-icon><Setting /></el-icon>
                  设置
                </button>
              </div>
            </div>
            
            <div v-else class="screenshot-only">
              <div v-if="currentScreenshot" class="screenshot-header">
                <span class="screenshot-title">最新截图</span>
                <div class="screenshot-buttons">
                  <button class="ctrl-btn small" @click="startScreenCapture">
                    <el-icon><VideoCamera /></el-icon>
                    屏幕捕获
                  </button>
                  <label class="ctrl-btn small">
                    <el-icon><UploadFilled /></el-icon>
                    上传
                    <input type="file" accept="image/*" @change="handleScreenshotUpload" style="display: none" />
                  </label>
                  <button class="ctrl-btn small primary" @click="analyzeScreenshot" :disabled="isAnalyzing">
                    <el-icon><View /></el-icon>
                    {{ isAnalyzing ? `${analyzingSeconds}s` : 'AI分析' }}
                  </button>
                  <button class="ctrl-btn small" @click="showAddHighlight = true">
                    <el-icon><Star /></el-icon>
                    高光
                  </button>
                  <button class="ctrl-btn small" @click="clearScreenshot">
                    <el-icon><Delete /></el-icon>
                  </button>
                </div>
              </div>
              <div v-else class="game-visual" :style="{ background: selectedGame?.color }">
                <img 
                  v-if="selectedGame?.imageUrl && selectedGame?.id && !imageLoadFailed[selectedGame.id]" 
                  :src="selectedGame.imageUrl" 
                  :alt="selectedGame?.name" 
                  class="visual-img"
                  @error="handleImageError($event, selectedGame)"
                />
                <span v-else class="visual-emoji">{{ selectedGame?.icon }}</span>
              </div>
              <img v-if="currentScreenshot" :src="currentScreenshot" alt="游戏截图" class="screenshot-img large" />
              <div class="capture-controls" v-if="!currentScreenshot">
                <button class="ctrl-btn primary" @click="startScreenCapture">
                  <el-icon><VideoCamera /></el-icon>
                  屏幕捕获
                </button>
                <label class="ctrl-btn">
                  <el-icon><UploadFilled /></el-icon>
                  上传截图
                  <input type="file" accept="image/*" @change="handleScreenshotUpload" style="display: none" />
                </label>
                <button class="ctrl-btn" @click="showSettings = true">
                  <el-icon><Setting /></el-icon>
                  设置
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- AI对话区 -->
        <div class="chat-area">
          <div class="chat-messages" ref="messagesRef">
            <div v-for="(msg, idx) in messages" :key="idx" class="message" :class="[msg.role, { streaming: msg.streaming }]">
              <div class="message-avatar">
                {{ msg.role === 'ai' ? 'GM' : '我' }}
              </div>
              <div class="message-bubble">
                <p>{{ msg.content }}<span v-if="msg.streaming" class="streaming-cursor"></span></p>
                <div class="message-actions" v-if="msg.role === 'ai'">
                  <span class="message-time">{{ msg.time }}</span>
                  <button 
                    class="speak-btn" 
                    @click="toggleSpeak(msg.content, idx)"
                    :class="{ active: currentSpeakingIdx === idx && isSpeaking }"
                  >
                    <span v-if="currentSpeakingIdx === idx && isPaused">▶ 继续</span>
                    <span v-else-if="currentSpeakingIdx === idx && isSpeaking">⏸ 暂停</span>
                    <span v-else>🔊 朗读</span>
                  </button>
                </div>
                <span class="message-time" v-else>{{ msg.time }}</span>
              </div>
            </div>
            
            <!-- AI思考中显示 -->
            <div v-if="isAnalyzing && !isStreamingReply" class="message ai thinking">
              <div class="message-avatar">GM</div>
              <div class="message-bubble">
                <div class="thinking-indicator">
                  <span class="dot"></span>
                  <span class="dot"></span>
                  <span class="dot"></span>
                </div>
                <span class="thinking-text">AI正在分析中... ({{analyzingSeconds}}s)</span>
              </div>
            </div>
          </div>

          <!-- 快速回复 -->
          <div class="quick-replies" v-if="selectedGame && !isCapturing">
            <button v-for="reply in quickReplies" :key="reply" class="quick-btn" @click="sendMessage(reply)">
              {{ reply }}
            </button>
          </div>

          <!-- 图片分析提示 -->
          <div class="image-analysis-tip" v-if="currentScreenshot">
            <el-icon><View /></el-icon>
            <span>当前消息将基于截图进行分析</span>
            <button class="clear-screenshot-btn" @click="clearScreenshot" title="清除截图">
              <el-icon><Close /></el-icon>
            </button>
          </div>
          <div class="game-knowledge-tip" v-else-if="selectedGame">
            <el-icon><InfoFilled /></el-icon>
            <span>将基于《{{ selectedGame.name }}》的游戏知识为您解答</span>
          </div>

          <!-- 输入区 -->
          <div v-if="isVoiceConversationActive" class="voice-conversation-bar">
            <span class="voice-pulse" :class="{ listening: isListening }"></span>
            <span>{{ voiceConversationStatus }}</span>
            <button @click="stopVoiceConversation">结束语音对话</button>
          </div>
          <div class="chat-input-area">
            <button
              class="voice-btn"
              :class="{ active: isVoiceConversationActive, listening: isListening }"
              :title="isVoiceConversationActive ? '结束连续语音对话' : '开始连续语音对话'"
              @click="toggleVoice"
            >
              <el-icon :size="20"><Microphone /></el-icon>
            </button>
            <div class="input-box">
              <input
                v-model="inputText"
                type="text"
                :placeholder="currentScreenshot ? '描述你的问题，AI会结合截图分析...' : '问我关于游戏的任何问题，如装备推荐、通关技巧等...'"
                @keydown.enter.exact.prevent="sendMessage(inputText)"
              />
            </div>
            <button class="send-btn" @click="sendMessage(inputText)" :disabled="!inputText.trim()">
              <el-icon :size="18"><Promotion /></el-icon>
            </button>
          </div>
        </div>
      </main>
    </div>

    <!-- 捕获设置弹窗 -->
    <el-dialog v-model="showSettings" title="AI设置" width="480px" :show-close="true">
      <div class="settings-form">
        <div class="form-item">
          <label>AI性格/人格</label>
          <el-select v-model="settings.personality" @change="handlePersonalityChange">
            <el-option 
              v-for="p in personalityOptions" 
              :key="p.value" 
              :label="p.label" 
              :value="p.value"
            />
          </el-select>
          <span class="form-hint">{{ getPersonalityDescription(settings.personality) }}</span>
        </div>
        <div class="form-item">
          <label>AI语音音色</label>
          <el-select v-model="settings.voice" @change="handleVoiceChange">
            <el-option 
              v-for="v in voiceOptions" 
              :key="v.value" 
              :label="v.label" 
              :value="v.value"
            />
          </el-select>
        </div>
        <div class="form-item">
          <label>语音识别语言</label>
          <el-select v-model="settings.language" @change="saveSettings">
            <el-option label="中文" value="zh-CN" />
            <el-option label="English" value="en-US" />
          </el-select>
        </div>
        <div class="form-item">
          <label>语速</label>
          <el-slider v-model="settings.speechRate" :min="0.5" :max="2" :step="0.1" show-input @change="saveSettings" />
        </div>
        <div class="form-item">
          <label>音调</label>
          <el-slider v-model="settings.speechPitch" :min="0" :max="2" :step="0.1" show-input @change="saveSettings" />
        </div>
        <div class="form-item-divider"></div>
        <div class="form-item">
          <label>捕获分辨率</label>
          <el-select v-model="settings.resolution" @change="saveSettings">
            <el-option label="720p" value="720p" />
            <el-option label="1080p" value="1080p" />
          </el-select>
        </div>
        <div class="form-item">
          <label>帧速率</label>
          <el-select v-model="settings.fps" @change="saveSettings">
            <el-option label="15 FPS" :value="15" />
            <el-option label="30 FPS" :value="30" />
          </el-select>
        </div>
      </div>
      <template #footer>
        <el-button @click="testSpeakVoice">🔊 测试语音</el-button>
        <el-button type="primary" @click="showSettings = false">完成</el-button>
      </template>
    </el-dialog>

    <!-- 添加游戏弹窗 -->
    <el-dialog v-model="showAddGameDialog" title="添加自定义游戏" width="480px" :show-close="true">
      <div class="add-game-form">
        <div class="form-item">
          <label>游戏名称 *</label>
          <el-input v-model="newGame.name" placeholder="请输入游戏名称" maxlength="20" />
        </div>
        <div class="form-item">
          <label>游戏类型</label>
          <el-input v-model="newGame.genre" placeholder="如：动作RPG、模拟经营等" maxlength="20" />
        </div>
        <div class="form-item">
          <label>游戏介绍</label>
          <el-input 
            v-model="newGame.description" 
            type="textarea" 
            :rows="2" 
            placeholder="简单介绍一下这款游戏" 
            maxlength="100"
            show-word-limit
          />
        </div>
        <div class="form-item">
          <label>标签（用、分隔）</label>
          <el-input v-model="newGame.tagsText" placeholder="如：攻略、技巧、剧情" />
        </div>
        <div class="form-item">
          <label>选择图标</label>
          <div class="icon-upload-area">
            <div class="icon-upload" @click="triggerUpload" v-if="!newGame.image">
              <el-icon :size="32"><UploadFilled /></el-icon>
              <span>上传图标</span>
              <input 
                type="file" 
                ref="fileInputRef" 
                accept="image/*" 
                @change="handleImageUpload"
                style="display: none"
              />
            </div>
            <div class="icon-preview" v-else>
              <img :src="newGame.image" alt="预览" />
              <div class="icon-actions">
                <el-icon :size="18" @click="triggerUpload" class="action-icon"><UploadFilled /></el-icon>
                <el-icon :size="18" @click="removeImage" class="action-icon delete"><Delete /></el-icon>
              </div>
            </div>
          </div>
          <div class="icon-picker-title">或选择预设图标</div>
          <div class="icon-picker">
            <div 
              v-for="icon in gameStore.iconOptions" 
              :key="icon" 
              class="icon-option"
              :class="{ active: newGame.icon === icon && !newGame.image }"
              @click="selectIcon(icon)"
            >
              {{ icon }}
            </div>
          </div>
        </div>
        <div class="form-item">
          <label>选择颜色</label>
          <div class="color-picker">
            <div 
              v-for="color in gameStore.colorOptions" 
              :key="color" 
              class="color-option"
              :class="{ active: newGame.color === color }"
              :style="{ background: color }"
              @click="newGame.color = color"
            ></div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showAddGameDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddGame">确认添加</el-button>
      </template>
    </el-dialog>

    <!-- 添加高光弹窗 -->
    <el-dialog v-model="showAddHighlight" title="保存高光" width="560px" :show-close="true">
      <div class="highlight-form">
        <div class="highlight-preview" v-if="currentScreenshot">
          <img :src="currentScreenshot" alt="截图预览" />
        </div>
        <div class="form-item">
          <label>高光标题 *</label>
          <el-input v-model="highlightForm.title" placeholder="如：深渊12层满星通关" maxlength="30" />
        </div>
        <div class="form-item">
          <label>高光描述</label>
          <el-input 
            v-model="highlightForm.description" 
            type="textarea" 
            :rows="3" 
            placeholder="描述这个高光时刻的背景和过程..." 
            maxlength="100"
            show-word-limit
          />
        </div>
        <div class="form-item">
          <label>关联游戏</label>
          <el-input :model-value="selectedGame?.name || '未选择'" disabled />
        </div>
      </div>
      <template #footer>
        <el-button @click="showAddHighlight = false">取消</el-button>
        <el-button type="primary" @click="saveHighlight">保存高光</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Plus, UploadFilled, VideoCamera, VideoPause, View, Setting, Microphone, Promotion, Star, Close, InfoFilled } from '@element-plus/icons-vue'
import { useGameStore } from '../stores/gameStore'
import { useAiChatStore } from '../stores/aiChatStore'
import { sendMessageWithPersonality, streamMessageWithPersonality, analyzeScreenWithPersonality } from '../api/chat'

const route = useRoute()
const gameStore = useGameStore()
const aiChatStore = useAiChatStore()

const selectedGame = ref(null)
const isCapturing = ref(false)
const isListening = ref(false)
const isVoiceConversationActive = ref(false)
const voiceConversationStatus = ref('正在准备麦克风...')
const isStreamingReply = ref(false)
const isAnalyzing = ref(false)
const analyzingSeconds = ref(0)
let requestTimer = null
const latency = ref(120)
const recognitionRate = ref(95)
const objectCount = ref(3)
const inputText = ref('')
const showSettings = ref(false)
const showAddGameDialog = ref(false)
const showAddHighlight = ref(false)
const messagesRef = ref(null)
const fileInputRef = ref(null)
const currentScreenshotFile = ref(null)
const screenStream = ref(null)
const recognition = ref(null)
const streamAbortController = ref(null)
const isSpeaking = ref(false)
const isPaused = ref(false)
const currentSpeakingIdx = ref(-1)
const availableVoices = ref([])
const imageLoadFailed = ref({})

let voiceDraft = ''
let voiceSubmitTimer = null
let recognitionRestartTimer = null
let streamingSpeechBuffer = ''
let streamingSpeechQueue = []
let streamingSpeechFinished = false
let streamingUtterance = null
let voiceMonitorStream = null
let voiceAudioContext = null
let voiceAnalyser = null
let voiceMonitorFrame = null
let recognitionPausedForSpeech = false
let vadLoudFrames = 0
let vadNoiseFloor = 0.012

const highlightForm = reactive({
  title: '',
  description: ''
})

const messages = computed({
  get: () => aiChatStore.messages,
  set: (val) => aiChatStore.setMessages(val)
})

const settings = computed({
  get: () => aiChatStore.settings,
  set: (val) => aiChatStore.updateSettings(val)
})

const currentScreenshot = computed({
  get: () => aiChatStore.currentScreenshot,
  set: (val) => aiChatStore.setCurrentScreenshot(val)
})

function handleImageError(event, game) {
  if (game && game.id) {
    imageLoadFailed.value[game.id] = true
  }
}

const newGame = reactive({
  name: '',
  genre: '',
  description: '',
  tagsText: '',
  icon: '🎮',
  color: '#8b5cf6',
  image: '',
  iconFile: null
})

const quickReplies = ref([
  '这关怎么过？',
  'BOSS有什么弱点？',
  '推荐一下装备搭配',
  '新手怎么玩？',
  '有哪些实用技巧？'
])

const personalityOptions = [
  { value: 'friendly', label: '😊 友好亲切' },
  { value: 'professional', label: '🎯 专业冷静' },
  { value: 'passionate', label: '🔥 激情教练' },
  { value: 'cute', label: '🎀 可爱萌系' },
  { value: 'serious', label: '📚 严肃导师' },
  { value: 'funny', label: '😂 幽默风趣' },
  { value: 'strategist', label: '♟️ 战术大师' },
  { value: 'mentor', label: '👨‍🏫 耐心导师' }
]

const voiceOptions = [
  { value: 'default', label: '默认音色' },
  { value: 'female-1', label: '温柔女声' },
  { value: 'female-2', label: '活泼女声' },
  { value: 'male-1', label: '沉稳男声' },
  { value: 'male-2', label: '年轻男声' },
  { value: 'child', label: '童声' }
]

function selectGame(game) {
  selectedGame.value = game
  aiChatStore.setSelectedGameId(game.id)
  aiChatStore.addMessage({
    role: 'ai',
    content: `已切换到《${game.name}》，我对这款游戏非常熟悉，有什么想知道的吗？`,
    time: '刚刚'
  })
  scrollToBottom()
}

async function startScreenCapture() {
  if (!selectedGame.value) {
    ElMessage.warning('请先选择一个游戏')
    return
  }
  
  try {
    if (!navigator.mediaDevices || !navigator.mediaDevices.getDisplayMedia) {
      ElMessage.error('当前浏览器不支持屏幕捕获功能，请使用最新版Chrome或Edge')
      return
    }
    
    isCapturing.value = true
    ElMessage.info('请在弹窗中选择要捕获的屏幕或窗口')
    
    const stream = await navigator.mediaDevices.getDisplayMedia({
      video: { frameRate: 30 },
      audio: false
    })
    
    screenStream.value = stream
    
    const videoTrack = stream.getVideoTracks()[0]
    if (videoTrack) {
      videoTrack.onended = () => {
        stopCapture()
      }
    }
    
    ElMessage.success('屏幕捕获已启动，请点击"立即截图"截取画面')
    messages.value.push({
      role: 'ai',
      content: `屏幕捕获已启动，正在监听《${selectedGame.value.name}》的游戏画面...`,
      time: '刚刚'
    })
    scrollToBottom()
    
    startMetricsUpdate()
  } catch (err) {
    console.error('屏幕捕获失败:', err)
    isCapturing.value = false
    ElMessage.error('屏幕捕获失败：' + err.message)
  }
}

async function captureNow() {
  if (!screenStream.value) {
    ElMessage.warning('请先启动屏幕捕获')
    return
  }
  
  try {
    const stream = screenStream.value
    const videoTrack = stream.getVideoTracks()[0]
    const trackSettings = videoTrack.getSettings()
    
    const video = document.createElement('video')
    video.srcObject = stream
    video.width = trackSettings.width || 1920
    video.height = trackSettings.height || 1080
    
    await new Promise((resolve) => {
      video.onloadedmetadata = () => {
        video.play()
        resolve()
      }
    })
    
    await new Promise((r) => setTimeout(r, 200))
    
    const canvas = document.createElement('canvas')
    const maxWidth = 2560
    const scale = Math.min(1, maxWidth / video.width)
    canvas.width = video.width * scale
    canvas.height = video.height * scale
    const ctx = canvas.getContext('2d')
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
    
    const dataUrl = canvas.toDataURL('image/jpeg', 0.95)
    
    await aiChatStore.setCurrentScreenshot(null)
    await aiChatStore.setCurrentScreenshot(dataUrl)
    
    canvas.toBlob((blob) => {
      if (blob) {
        const file = new File([blob], `screenshot_${Date.now()}.jpg`, { type: 'image/jpeg' })
        currentScreenshotFile.value = file
        ElMessage.success('截图成功！')
      }
    }, 'image/jpeg', 0.95)
    
  } catch (err) {
    console.error('截图失败:', err)
    ElMessage.error('截图失败：' + err.message)
  }
}

function stopCapture() {
  isCapturing.value = false
  if (screenStream.value) {
    screenStream.value.getTracks().forEach(track => track.stop())
    screenStream.value = null
  }
  ElMessage.info('屏幕捕获已停止')
}

function clearScreenshot() {
  aiChatStore.clearScreenshot()
  currentScreenshotFile.value = null
}

async function handleScreenshotUpload(event) {
  const file = event.target.files?.[0]
  if (!file) return
  
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过10MB')
    return
  }
  
  try {
    const reader = new FileReader()
    reader.onload = async (e) => {
      if (e.target?.result) {
        await aiChatStore.setCurrentScreenshot(null)
        await aiChatStore.setCurrentScreenshot(e.target.result)
        currentScreenshotFile.value = file
        ElMessage.success('截图上传成功')
      }
    }
    reader.onerror = () => {
      ElMessage.error('图片读取失败')
    }
    reader.readAsDataURL(file)
  } catch (err) {
    console.error('截图上传失败:', err)
    ElMessage.error('截图上传失败')
  }
  event.target.value = ''
}

async function analyzeScreenshot() {
  if (!currentScreenshot.value || !selectedGame.value) {
    ElMessage.warning('请先截取或上传游戏画面，并选择游戏')
    return
  }
  
  const description = inputText.value.trim() || '请分析这张游戏画面，识别当前局势并给出建议'
  await doAnalyze(description)
}

function saveHighlight() {
  if (!highlightForm.title.trim()) {
    ElMessage.warning('请输入高光标题')
    return
  }
  
  aiChatStore.addHighlight({
    id: Date.now(),
    title: highlightForm.title,
    description: highlightForm.description,
    gameId: selectedGame.value?.id,
    gameName: selectedGame.value?.name,
    screenshot: currentScreenshot.value,
    time: new Date().toLocaleString('zh-CN'),
    duration: '截图',
    icon: selectedGame.value?.icon || '🎮',
    color: selectedGame.value?.color || '#8b5cf6'
  })
  
  ElMessage.success('高光已保存！')
  showAddHighlight.value = false
  highlightForm.title = ''
  highlightForm.description = ''
}

function base64ToBlob(base64, mime = 'image/jpeg') {
  if (!base64) {
    throw new Error('图片数据为空')
  }
  
  let base64Data = base64
  if (base64.includes(',')) {
    base64Data = base64.split(',')[1]
  }
  
  try {
    const byteCharacters = atob(base64Data)
    const byteNumbers = new Array(byteCharacters.length)
    for (let i = 0; i < byteCharacters.length; i++) {
      byteNumbers[i] = byteCharacters.charCodeAt(i)
    }
    const byteArray = new Uint8Array(byteNumbers)
    return new Blob([byteArray], { type: mime })
  } catch (e) {
    console.error('base64转Blob失败:', e)
    throw new Error('图片格式转换失败')
  }
}

async function doAnalyze(description) {
  isAnalyzing.value = true
  analyzingSeconds.value = 0
  const startTime = Date.now()
  requestTimer = setInterval(() => {
    analyzingSeconds.value++
  }, 1000)
  
  try {
    aiChatStore.addMessage({
      role: 'user',
      content: `[画面分析] ${description}`,
      time: '刚刚'
    })
    scrollToBottom()
    
    if (!currentScreenshot.value) {
      aiChatStore.addMessage({
        role: 'ai',
        content: '请先截取或上传一张游戏画面！',
        time: '刚刚'
      })
      return
    }
    
    const res = await analyzeScreenWithPersonality({
      gameId: selectedGame.value.id,
      imageBase64: currentScreenshot.value,
      content: description,
      personality: settings.personality
    })
    
    if (res.code === 200 && res.data) {
      const elapsed = (Date.now() - startTime) / 1000
      aiChatStore.addMessage({
        role: 'ai',
        content: res.data.content,
        time: '刚刚'
      })
      aiChatStore.recordAIUsage(elapsed)
      if (settings.personality && settings.personality !== 'friendly') {
        aiChatStore.recordPersonalityUsage(elapsed)
      }
    } else {
      throw new Error(res.message || '分析失败')
    }
  } catch (err) {
    console.error('画面分析失败:', err)
    let errorMsg = err.message || '网络错误，请稍后重试'
    if (err.code === 'ECONNABORTED' || err.message?.includes('timeout')) {
      errorMsg = 'AI响应超时，可能是图片太大或网络较慢。建议截图时选择较小的分辨率。'
    }
    aiChatStore.addMessage({
      role: 'ai',
      content: '❌ 画面分析失败：' + errorMsg,
      time: '刚刚'
    })
    ElMessage.error('画面分析失败')
  } finally {
    isAnalyzing.value = false
    analyzingSeconds.value = 0
    requestTimer && clearInterval(requestTimer)
    inputText.value = ''
    scrollToBottom()
  }
}

function startMetricsUpdate() {
  const interval = setInterval(() => {
    if (!isCapturing.value) {
      clearInterval(interval)
      return
    }
    latency.value = Math.floor(80 + Math.random() * 100)
    recognitionRate.value = Math.floor(90 + Math.random() * 10)
    objectCount.value = Math.floor(2 + Math.random() * 5)
  }, 2000)
}

function initSpeechRecognition() {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
  if (!SpeechRecognition) {
    return null
  }
  
  const recognitionInstance = new SpeechRecognition()
  recognitionInstance.lang = settings.language
  recognitionInstance.continuous = true
  recognitionInstance.interimResults = true
  recognitionInstance.maxAlternatives = 1

  recognitionInstance.onstart = () => {
    isListening.value = true
    voiceConversationStatus.value = isAnalyzing.value || isSpeaking.value
      ? 'AI正在回答，你可以随时开口打断'
      : '正在聆听，请开始说话'
  }
  
  recognitionInstance.onresult = (event) => {
    let finalTranscript = ''
    let interimTranscript = ''
    for (let i = event.resultIndex; i < event.results.length; i++) {
      const transcript = event.results[i][0].transcript
      if (event.results[i].isFinal) {
        finalTranscript += transcript
      } else {
        interimTranscript += transcript
      }
    }

    if (!finalTranscript && !interimTranscript) return

    if (isSpeaking.value || isAnalyzing.value || streamAbortController.value) {
      interruptVoiceResponse()
    }

    if (finalTranscript) {
      voiceDraft = `${voiceDraft} ${finalTranscript}`.trim()
    }
    inputText.value = `${voiceDraft} ${interimTranscript}`.trim()
    voiceConversationStatus.value = '检测到语音，停顿后将自动发送'
    scheduleVoiceSubmit(finalTranscript ? 650 : 1100)
  }
  
  recognitionInstance.onerror = (event) => {
    console.error('语音识别错误:', event.error)
    isListening.value = false
    if (event.error === 'not-allowed' || event.error === 'service-not-allowed') {
      isVoiceConversationActive.value = false
      voiceConversationStatus.value = '麦克风权限被拒绝'
      ElMessage.error('无法使用麦克风，请在浏览器地址栏允许麦克风权限')
      return
    }
    if (isVoiceConversationActive.value && event.error !== 'aborted') {
      voiceConversationStatus.value = event.error === 'no-speech' ? '未检测到声音，继续聆听中' : '语音识别暂时中断，正在重连'
    }
  }
  
  recognitionInstance.onend = () => {
    if (recognition.value === recognitionInstance) {
      recognition.value = null
    }
    isListening.value = false
    if (!recognitionPausedForSpeech) {
      scheduleRecognitionRestart()
    }
  }
  
  return recognitionInstance
}

function scheduleRecognitionRestart(delay = 250) {
  clearTimeout(recognitionRestartTimer)
  if (!isVoiceConversationActive.value) return
  recognitionRestartTimer = setTimeout(() => {
    startVoiceRecognition()
  }, delay)
}

function startVoiceRecognition() {
  if (!isVoiceConversationActive.value || isListening.value || recognition.value) return
  recognition.value = initSpeechRecognition()
  if (!recognition.value) return
  try {
    recognition.value.lang = settings.language
    recognition.value.start()
  } catch (error) {
    console.warn('语音识别启动失败，稍后重试:', error)
    recognition.value = null
    scheduleRecognitionRestart(600)
  }
}

function scheduleVoiceSubmit(delay) {
  clearTimeout(voiceSubmitTimer)
  voiceSubmitTimer = setTimeout(() => {
    const text = inputText.value.trim()
    if (!text || !isVoiceConversationActive.value) return

    voiceDraft = ''
    inputText.value = ''
    const currentRecognition = recognition.value
    recognition.value = null
    if (currentRecognition) {
      try {
        currentRecognition.abort()
      } catch {
        // 已结束的识别会话无需再次处理。
      }
    }
    scheduleRecognitionRestart(200)
    sendMessage(text, { voiceConversation: true })
  }, delay)
}

function interruptVoiceResponse() {
  if (streamAbortController.value) {
    streamAbortController.value.abort()
    streamAbortController.value = null
  }
  stopSpeaking()
  recognitionPausedForSpeech = false
  scheduleRecognitionRestart(50)
  isStreamingReply.value = false
  isAnalyzing.value = false
  voiceConversationStatus.value = '已打断AI，请继续说话'
}

async function startVoiceActivityMonitor() {
  if (voiceMonitorStream || !navigator.mediaDevices?.getUserMedia) return
  voiceMonitorStream = await navigator.mediaDevices.getUserMedia({
    audio: {
      echoCancellation: true,
      noiseSuppression: true,
      autoGainControl: true
    }
  })

  const AudioContext = window.AudioContext || window.webkitAudioContext
  if (!AudioContext) return
  voiceAudioContext = new AudioContext()
  const source = voiceAudioContext.createMediaStreamSource(voiceMonitorStream)
  voiceAnalyser = voiceAudioContext.createAnalyser()
  voiceAnalyser.fftSize = 1024
  voiceAnalyser.smoothingTimeConstant = 0.25
  source.connect(voiceAnalyser)
  const samples = new Float32Array(voiceAnalyser.fftSize)

  const monitor = () => {
    if (!isVoiceConversationActive.value || !voiceAnalyser) return
    voiceAnalyser.getFloatTimeDomainData(samples)
    let energy = 0
    for (let i = 0; i < samples.length; i++) energy += samples[i] * samples[i]
    const rms = Math.sqrt(energy / samples.length)

    if (!isSpeaking.value) {
      vadNoiseFloor = vadNoiseFloor * 0.96 + Math.min(rms, 0.04) * 0.04
      vadLoudFrames = 0
    } else {
      const threshold = Math.max(0.035, vadNoiseFloor * 3.2)
      vadLoudFrames = rms > threshold ? vadLoudFrames + 1 : 0
      if (vadLoudFrames >= 3) {
        vadLoudFrames = 0
        interruptVoiceResponse()
      }
    }
    voiceMonitorFrame = requestAnimationFrame(monitor)
  }
  voiceMonitorFrame = requestAnimationFrame(monitor)
}

function stopVoiceActivityMonitor() {
  if (voiceMonitorFrame) cancelAnimationFrame(voiceMonitorFrame)
  voiceMonitorFrame = null
  voiceAnalyser = null
  if (voiceMonitorStream) {
    voiceMonitorStream.getTracks().forEach(track => track.stop())
    voiceMonitorStream = null
  }
  if (voiceAudioContext) {
    voiceAudioContext.close().catch(() => {})
    voiceAudioContext = null
  }
  vadLoudFrames = 0
}

function pauseRecognitionForSpeech() {
  if (!isVoiceConversationActive.value) return
  recognitionPausedForSpeech = true
  clearTimeout(recognitionRestartTimer)
  const currentRecognition = recognition.value
  recognition.value = null
  if (currentRecognition) {
    try {
      currentRecognition.abort()
    } catch {
      // 识别已经结束。
    }
  }
  isListening.value = false
}

function resumeRecognitionAfterSpeech() {
  recognitionPausedForSpeech = false
  scheduleRecognitionRestart(80)
}

function stopVoiceConversation() {
  isVoiceConversationActive.value = false
  isListening.value = false
  voiceDraft = ''
  recognitionPausedForSpeech = false
  clearTimeout(voiceSubmitTimer)
  clearTimeout(recognitionRestartTimer)
  voiceSubmitTimer = null
  recognitionRestartTimer = null

  if (recognition.value) {
    const currentRecognition = recognition.value
    recognition.value = null
    try {
      currentRecognition.abort()
    } catch {
      // 识别已经结束。
    }
  }
  if (streamAbortController.value) {
    streamAbortController.value.abort()
    streamAbortController.value = null
  }
  stopSpeaking()
  stopVoiceActivityMonitor()
  isStreamingReply.value = false
  isAnalyzing.value = false
  voiceConversationStatus.value = '语音对话已结束'
  ElMessage.info('连续语音对话已结束')
}

async function toggleVoice() {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
  if (!SpeechRecognition) {
    ElMessage.warning('当前浏览器不支持连续语音识别，请使用最新版Chrome或Edge')
    return
  }

  if (isVoiceConversationActive.value) {
    stopVoiceConversation()
    return
  }

  if (!selectedGame.value) {
    ElMessage.warning('请先选择一个游戏，再开始语音对话')
    return
  }

  isVoiceConversationActive.value = true
  voiceConversationStatus.value = '正在启动麦克风...'
  try {
    await startVoiceActivityMonitor()
    startVoiceRecognition()
    ElMessage.success('连续语音对话已开启，说完停顿即可自动发送')
  } catch (error) {
    console.error('麦克风启动失败:', error)
    isVoiceConversationActive.value = false
    stopVoiceActivityMonitor()
    ElMessage.error('无法启动麦克风，请检查浏览器权限和系统麦克风设置')
  }
}

async function sendStreamingVoiceMessage(text) {
  if (!selectedGame.value) {
    ElMessage.warning('请先选择一个游戏')
    return
  }

  aiChatStore.addMessage({
    role: 'user',
    content: text,
    time: '刚刚'
  })
  aiChatStore.addMessage({
    role: 'ai',
    content: '',
    time: '正在回答',
    streaming: true
  })
  const aiMessageIndex = messages.value.length - 1
  const controller = new AbortController()
  streamAbortController.value = controller
  isAnalyzing.value = true
  isStreamingReply.value = true
  analyzingSeconds.value = 0
  const startTime = Date.now()
  requestTimer && clearInterval(requestTimer)
  requestTimer = setInterval(() => analyzingSeconds.value++, 1000)
  voiceConversationStatus.value = 'AI正在生成回答，你可以随时开口打断'
  beginStreamingSpeech(aiMessageIndex)
  scrollToBottom()

  try {
    const completeContent = await streamMessageWithPersonality({
      gameId: selectedGame.value.id,
      content: text,
      personality: settings.personality
    }, {
      signal: controller.signal,
      onDelta: (chunk) => {
        const message = messages.value[aiMessageIndex]
        if (!message) return
        message.content += chunk
        queueStreamingSpeech(chunk)
        scrollToBottom()
      }
    })

    const message = messages.value[aiMessageIndex]
    if (message) {
      message.streaming = false
      message.time = '刚刚'
      if (!message.content && completeContent) message.content = completeContent
    }
    finishStreamingSpeech()
    const elapsed = (Date.now() - startTime) / 1000
    aiChatStore.recordAIUsage(elapsed)
    if (settings.personality && settings.personality !== 'friendly') {
      aiChatStore.recordPersonalityUsage(elapsed)
    }
  } catch (error) {
    const message = messages.value[aiMessageIndex]
    if (error.name === 'AbortError') {
      if (message) {
        message.streaming = false
        message.time = '已被语音打断'
        if (!message.content) messages.value.splice(aiMessageIndex, 1)
      }
    } else {
      console.error('流式语音聊天失败:', error)
      if (message) {
        message.streaming = false
        message.content = message.content || `❌ ${error.message || 'AI流式回答失败'}`
        message.time = '请求失败'
      }
      stopSpeaking()
      ElMessage.error('AI流式回答失败')
    }
  } finally {
    if (streamAbortController.value === controller) {
      streamAbortController.value = null
      isAnalyzing.value = false
      isStreamingReply.value = false
      analyzingSeconds.value = 0
      requestTimer && clearInterval(requestTimer)
      requestTimer = null
      if (isVoiceConversationActive.value && !isSpeaking.value) {
        voiceConversationStatus.value = '正在聆听，请继续说话'
      }
    }
    scrollToBottom()
  }
}

async function sendMessage(text, options = {}) {
  if (!text || !text.trim()) return

  if (options.voiceConversation && !currentScreenshot.value) {
    return sendStreamingVoiceMessage(text.trim())
  }
  
  aiChatStore.addMessage({
    role: 'user',
    content: text,
    time: '刚刚'
  })
  
  const currentText = text
  inputText.value = ''
  scrollToBottom()
  
  isAnalyzing.value = true
  analyzingSeconds.value = 0
  const startTime = Date.now()
  requestTimer = setInterval(() => {
    analyzingSeconds.value++
  }, 1000)
  let voiceReplyText = ''
  
  try {
    if (!selectedGame.value) {
      aiChatStore.addMessage({
        role: 'ai',
        content: '请先选择一个游戏，我才能更好地为你提供帮助。你可以在左侧游戏列表中选择，或者点击"添加游戏"添加自定义游戏。',
        time: '刚刚'
      })
      return
    }
    
    if (currentScreenshot.value) {
      const res = await analyzeScreenWithPersonality({
        gameId: selectedGame.value.id,
        imageBase64: currentScreenshot.value,
        content: currentText,
        personality: settings.personality
      })
      
      if (res.code === 200 && res.data) {
        const elapsed = (Date.now() - startTime) / 1000
        aiChatStore.addMessage({
          role: 'ai',
          content: res.data.content,
          time: '刚刚'
        })
        voiceReplyText = res.data.content
        aiChatStore.recordAIUsage(elapsed)
        if (settings.personality && settings.personality !== 'friendly') {
          aiChatStore.recordPersonalityUsage(elapsed)
        }
      } else {
        throw new Error(res.message || '分析失败')
      }
    } else {
      const res = await sendMessageWithPersonality({
        gameId: selectedGame.value.id,
        content: currentText,
        personality: settings.personality
      })
      
      if (res.code === 200 && res.data) {
        const elapsed = (Date.now() - startTime) / 1000
        aiChatStore.addMessage({
          role: 'ai',
          content: res.data.content,
          time: '刚刚'
        })
        voiceReplyText = res.data.content
        aiChatStore.recordAIUsage(elapsed)
        if (settings.personality && settings.personality !== 'friendly') {
          aiChatStore.recordPersonalityUsage(elapsed)
        }
      }
    }
  } catch (err) {
    console.error('发送消息失败:', err)
    let errorMsg = '网络错误，请稍后重试'
    if (err.code === 'ECONNABORTED' || err.message?.includes('timeout')) {
      errorMsg = 'AI响应超时，可能是图片太大或网络较慢。建议截图时选择较小的分辨率，或稍后重试。'
    } else if (err.response?.status === 401) {
      errorMsg = 'API认证失败，请检查后端API Key配置'
    } else {
      errorMsg = err.message || errorMsg
    }
    aiChatStore.addMessage({
      role: 'ai',
      content: '❌ ' + errorMsg,
      time: '刚刚'
    })
    ElMessage.error('请求失败')
  } finally {
    isAnalyzing.value = false
    analyzingSeconds.value = 0
    requestTimer && clearInterval(requestTimer)
    if (options.voiceConversation && voiceReplyText && isVoiceConversationActive.value) {
      speakText(voiceReplyText)
      voiceConversationStatus.value = 'AI正在播报，你可以随时开口打断'
    }
  }
  
  scrollToBottom()
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

// ============ TTS 语音合成功能 ============

function initVoices() {
  if ('speechSynthesis' in window) {
    window.speechSynthesis.onvoiceschanged = () => {
      availableVoices.value = window.speechSynthesis.getVoices()
    }
    availableVoices.value = window.speechSynthesis.getVoices()
  }
}

function beginStreamingSpeech(messageIndex) {
  stopSpeaking()
  streamingSpeechBuffer = ''
  streamingSpeechQueue = []
  streamingSpeechFinished = false
  streamingUtterance = null
  currentSpeakingIdx.value = messageIndex
}

function queueStreamingSpeech(chunk) {
  if (!chunk || !isVoiceConversationActive.value) return
  streamingSpeechBuffer += chunk.replace(/[\*#`]/g, '')

  let boundaryIndex = streamingSpeechBuffer.search(/[。！？!?；;\n]/)
  while (boundaryIndex >= 0) {
    const sentence = streamingSpeechBuffer.slice(0, boundaryIndex + 1).trim()
    streamingSpeechBuffer = streamingSpeechBuffer.slice(boundaryIndex + 1)
    if (sentence) streamingSpeechQueue.push(sentence)
    boundaryIndex = streamingSpeechBuffer.search(/[。！？!?；;\n]/)
  }

  // 模型长时间不输出句号时，以逗号为安全边界，避免等待整段生成完成才播报。
  if (streamingSpeechBuffer.length >= 48) {
    const commaIndex = Math.max(streamingSpeechBuffer.lastIndexOf('，'), streamingSpeechBuffer.lastIndexOf(','))
    if (commaIndex > 16) {
      streamingSpeechQueue.push(streamingSpeechBuffer.slice(0, commaIndex + 1).trim())
      streamingSpeechBuffer = streamingSpeechBuffer.slice(commaIndex + 1)
    }
  }
  pumpStreamingSpeech()
}

function finishStreamingSpeech() {
  const remaining = streamingSpeechBuffer.trim()
  if (remaining) streamingSpeechQueue.push(remaining)
  streamingSpeechBuffer = ''
  streamingSpeechFinished = true
  pumpStreamingSpeech()
}

function pumpStreamingSpeech() {
  if (!isVoiceConversationActive.value || streamingUtterance || !window.speechSynthesis) return
  const sentence = streamingSpeechQueue.shift()
  if (!sentence) {
    if (streamingSpeechFinished) {
      isSpeaking.value = false
      isPaused.value = false
      currentSpeakingIdx.value = -1
      voiceConversationStatus.value = '正在聆听，请继续说话'
      resumeRecognitionAfterSpeech()
    }
    return
  }

  const utterance = new SpeechSynthesisUtterance(sentence)
  utterance.rate = settings.speechRate || 1.2
  utterance.pitch = settings.speechPitch || 1.0
  utterance.lang = settings.language || 'zh-CN'
  const voice = selectVoice(settings.voice)
  if (voice) utterance.voice = voice

  streamingUtterance = utterance
  utterance.onstart = () => {
    isSpeaking.value = true
    isPaused.value = false
    pauseRecognitionForSpeech()
    voiceConversationStatus.value = 'AI正在播报，你可以随时开口打断'
  }
  utterance.onend = () => {
    streamingUtterance = null
    pumpStreamingSpeech()
  }
  utterance.onerror = (event) => {
    streamingUtterance = null
    if (event.error !== 'canceled' && event.error !== 'interrupted') {
      console.warn('流式语音播报失败:', event.error)
    }
    pumpStreamingSpeech()
  }
  window.speechSynthesis.speak(utterance)
}

function toggleSpeak(text, idx) {
  if (!text || !window.speechSynthesis) return
  
  if (currentSpeakingIdx.value === idx && isSpeaking.value) {
    if (isPaused.value) {
      window.speechSynthesis.resume()
      isPaused.value = false
    } else {
      window.speechSynthesis.pause()
      isPaused.value = true
    }
    return
  }
  
  if (currentSpeakingIdx.value === idx && isPaused.value) {
    window.speechSynthesis.resume()
    isPaused.value = false
    return
  }
  
  stopSpeaking()
  speakText(text, idx)
}

function speakText(text, idx = -1) {
  if (!text || !window.speechSynthesis) return
  
  stopSpeaking()
  
  const cleanText = text.replace(/[\*\#\`]/g, '').replace(/\n/g, '。')
  const sentences = cleanText.split(/[。！？!?.\n]/).filter(s => s.trim().length > 0)
  
  const voice = selectVoice(settings.voice)
  const rate = settings.speechRate || 1.2
  const pitch = settings.speechPitch || 1.0
  
  currentSpeakingIdx.value = idx
  isPaused.value = false
  isSpeaking.value = true
  
  const speechStartTime = Date.now()
  let totalSpeechDuration = 0
  
  sentences.forEach((sentence, index) => {
    const utterance = new SpeechSynthesisUtterance(sentence.trim())
    utterance.rate = rate
    utterance.pitch = pitch
    utterance.lang = 'zh-CN'
    
    if (voice) {
      utterance.voice = voice
    }
    
    utterance.onstart = () => {
      isSpeaking.value = true
      isPaused.value = false
      pauseRecognitionForSpeech()
    }
    
    utterance.onend = () => {
      const sentenceDuration = 1 / rate
      totalSpeechDuration += sentenceDuration
      
      if (index === sentences.length - 1) {
        const totalSeconds = (Date.now() - speechStartTime) / 1000
        isSpeaking.value = false
        isPaused.value = false
        currentSpeakingIdx.value = -1
        if (settings.voice && settings.voice !== 'default') {
          aiChatStore.recordVoiceUsage(totalSeconds)
        }
        resumeRecognitionAfterSpeech()
      }
    }
    
    utterance.onerror = () => {
      isSpeaking.value = false
      isPaused.value = false
      currentSpeakingIdx.value = -1
      if (isVoiceConversationActive.value) resumeRecognitionAfterSpeech()
    }
    
    window.speechSynthesis.speak(utterance)
  })
}

function stopSpeaking() {
  streamingSpeechBuffer = ''
  streamingSpeechQueue = []
  streamingSpeechFinished = true
  streamingUtterance = null
  if (window.speechSynthesis) {
    window.speechSynthesis.cancel()
    isSpeaking.value = false
    isPaused.value = false
    currentSpeakingIdx.value = -1
  }
}

function selectVoice(voiceSetting) {
  const voices = availableVoices.value
  if (!voices.length) return null
  
  const zhVoices = voices.filter(v => v.lang.startsWith('zh'))
  const enVoices = voices.filter(v => v.lang.startsWith('en'))
  
  if (zhVoices.length === 0 && enVoices.length === 0) {
    return voices[0]
  }
  
  const searchInVoices = (voiceList, patterns) => {
    for (const pattern of patterns) {
      const found = voiceList.find(v => pattern.test(v.name))
      if (found) return found
    }
    return null
  }
  
  switch (voiceSetting) {
    case 'female-1':
      return searchInVoices(zhVoices, [
        /female|woman|zi|xiao|ting|mei|yaoyao|xiaoxiao|xiaoyi/i,
        /Tingting|Meijia|Yaoyao|Xiaoxiao|Xiaoyi/i
      ]) || zhVoices[0]
    case 'female-2':
      return searchInVoices(zhVoices, [
        /female|woman|活泼|young|xiaoyou|xiaomo/i,
        /Xiaoyou|Xiaomo|Hui|Yunxi/i
      ]) || zhVoices[0]
    case 'male-1':
      return searchInVoices(zhVoices, [
        /male|man|chen|bo|沉稳|yunjian|yunxi/i,
        /Yunxi|Yunjian|Lei|Kangkang/i
      ]) || zhVoices[0]
    case 'male-2':
      return searchInVoices(zhVoices, [
        /male|man|年轻|yunqi|yunyang/i,
        /Yunqi|Yunyang|Yunze/i
      ]) || zhVoices[0]
    case 'child':
      return searchInVoices(zhVoices, [
        /child|kid|童声|xiaoshuang|xiaoyan/i
      ]) || zhVoices[0]
    default:
      return zhVoices[0] || enVoices[0] || voices[0]
  }
}

function testSpeakVoice() {
  const testText = getPersonalityTestText(settings.personality)
  speakText(testText)
  ElMessage.info('正在播放测试语音...')
}

function getPersonalityTestText(personality) {
  const tests = {
    friendly: '你好呀！我是你的AI游戏伙伴，很高兴为你服务。有什么我可以帮你的吗？',
    professional: '你好，我是你的AI分析师。我可以帮你分析游戏局势，提供专业的战术建议。',
    passionate: '嘿！准备好迎接挑战了吗？让我们一起征服这款游戏！',
    cute: '呜呜呜...你好呀~人家是你的小可爱助手啦~快来玩游戏吧！',
    serious: '你好，我是你的AI导师。请认真对待每一局游戏，我会帮助你提升水平。',
    funny: '欢迎来到游戏世界！小心哦，前方可能有敌人在等你，也可能有惊喜！',
    strategist: '你好，我是你的战术顾问。让我们一起制定最佳策略，赢得这场比赛。',
    mentor: '你好同学，我是你的AI导师。今天我们来学习一些新的游戏技巧。'
  }
  return tests[personality] || tests.friendly
}

// ============ AI性格/人格设置 ============

function getPersonalityDescription(personality) {
  const descriptions = {
    friendly: '语气亲切自然，像朋友一样交流',
    professional: '理性分析，注重数据和战术细节',
    passionate: '充满活力，像教练一样激发斗志',
    cute: '活泼可爱，像闺蜜一样陪伴',
    serious: '严谨专业，给出精准建议',
    funny: '轻松幽默，适时加入游戏梗',
    strategist: '从战术层面分析，提供系统策略',
    mentor: '耐心教学，循序渐进地引导'
  }
  return descriptions[personality] || ''
}

function handlePersonalityChange() {
  ElMessage.success('AI性格已更新，下次对话将使用新人格')
}

function handleVoiceChange() {
  ElMessage.success('语音音色已更新')
}

function triggerUpload() {
  fileInputRef.value?.click()
}

function selectIcon(icon) {
  newGame.icon = icon
  newGame.image = ''
}

function handleImageUpload(event) {
  const file = event.target.files?.[0]
  if (!file) return
  
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过5MB')
    return
  }
  
  newGame.iconFile = file
  
  const reader = new FileReader()
  reader.onload = async (e) => {
    const imageData = e.target?.result
    if (imageData) {
      const compressed = await compressImage(imageData)
      if (compressed) {
        newGame.image = compressed
        ElMessage.success('图片上传成功')
      }
    }
  }
  reader.readAsDataURL(file)
  event.target.value = ''
}

async function compressImage(base64) {
  return new Promise((resolve) => {
    const img = new Image()
    img.onload = () => {
      const maxSize = 800
      let { width, height } = img
      
      if (width > maxSize || height > maxSize) {
        if (width > height) {
          height = (height / width) * maxSize
          width = maxSize
        } else {
          width = (width / height) * maxSize
          height = maxSize
        }
      }
      
      const canvas = document.createElement('canvas')
      canvas.width = width
      canvas.height = height
      const ctx = canvas.getContext('2d')
      ctx.drawImage(img, 0, 0, width, height)
      
      const compressed = canvas.toDataURL('image/jpeg', 0.92)
      resolve(compressed)
    }
    img.onerror = () => resolve(base64)
    img.src = base64
  })
}

function removeImage() {
  newGame.image = ''
  newGame.iconFile = null
}

function handleAddGame() {
  if (!newGame.name.trim()) {
    ElMessage.warning('请输入游戏名称')
    return
  }
  
  const gameData = {
    name: newGame.name.trim(),
    genre: newGame.genre.trim() || '自定义',
    description: newGame.description.trim() || '自定义游戏',
    tags: newGame.tagsText.split(/[、,，]/).map(t => t.trim()).filter(Boolean).slice(0, 3) || ['自定义'],
    icon: newGame.icon,
    color: newGame.color,
    iconFile: newGame.iconFile
  }
  
  gameStore.addCustomGame(gameData).then(newAddedGame => {
    ElMessage.success(`已添加《${gameData.name}》`)
    showAddGameDialog.value = false
    
    newGame.name = ''
    newGame.genre = ''
    newGame.description = ''
    newGame.tagsText = ''
    newGame.icon = '🎮'
    newGame.color = '#8b5cf6'
    newGame.image = ''
    newGame.iconFile = null
    
    if (newAddedGame) {
      selectGame(newAddedGame)
    }
  })
}

function handleDeleteGame(game) {
  ElMessageBox.confirm(`确定要删除《${game.name}》吗？`, '提示', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    gameStore.removeCustomGame(game.id)
    if (selectedGame.value?.id === game.id) {
      selectedGame.value = null
    }
    ElMessage.success('已删除')
  }).catch(() => {})
}

onMounted(async () => {
  localStorage.removeItem('ai-chat-state')
  
  if (!gameStore.initialized) {
    await gameStore.fetchGames()
  }
  
  if (aiChatStore.selectedGameId) {
    const game = gameStore.games.find(g => g.id === aiChatStore.selectedGameId)
    if (game) {
      selectedGame.value = game
    }
  }
  
  const gameId = route.query.game
  if (gameId) {
    const game = gameStore.games.find(g => g.id === Number(gameId))
    if (game) {
      selectedGame.value = game
      aiChatStore.setSelectedGameId(game.id)
    }
  }
  
  initVoices()
})

onUnmounted(() => {
  isVoiceConversationActive.value = false
  clearTimeout(voiceSubmitTimer)
  clearTimeout(recognitionRestartTimer)
  if (streamAbortController.value) {
    streamAbortController.value.abort()
    streamAbortController.value = null
  }
  stopVoiceActivityMonitor()
  if (screenStream.value) {
    screenStream.value.getTracks().forEach(track => track.stop())
    screenStream.value = null
  }
  if (recognition.value) {
    recognition.value.abort()
    recognition.value = null
  }
  stopSpeaking()
})

watch(() => settings.language, (newLang) => {
  if (recognition.value) {
    const currentRecognition = recognition.value
    recognition.value = null
    try {
      currentRecognition.abort()
    } catch {
      // 识别已经结束。
    }
    scheduleRecognitionRestart(200)
  }
})

watch(() => settings.speechRate, () => {
  if (isSpeaking.value) {
    stopSpeaking()
  }
})

watch(() => settings.speechPitch, () => {
  if (isSpeaking.value) {
    stopSpeaking()
  }
})
</script>

<style scoped>
.ai-chat-page {
  padding: 24px 32px 120px;
}

/* 状态栏 */
.status-bar {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.status-bar.empty {
  justify-content: center;
}

.status-hint {
  color: var(--text-muted);
  font-size: 15px;
}

.status-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.status-label {
  background: var(--accent-purple);
  color: white;
  padding: 5px 14px;
  border-radius: 12px;
  font-size: 14px;
}

.status-sep {
  color: var(--text-muted);
}

.status-game {
  font-weight: 600;
  font-size: 16px;
}

.status-metrics {
  display: flex;
  gap: 28px;
  font-size: 14px;
  color: var(--text-secondary);
}

.status-metrics strong {
  color: var(--text-primary);
  font-weight: 600;
}

/* 布局 */
.chat-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 24px;
  height: calc(100vh - 160px);
  min-height: 600px;
}

/* 游戏侧边栏 */
.game-sidebar {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.sidebar-title {
  padding: 18px 20px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-title h3 {
  font-size: 15px;
  font-weight: 600;
}

.game-count {
  color: var(--text-muted);
  font-size: 13px;
}

.game-list-wrapper {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.game-list {
  flex: 1;
  padding: 10px;
  overflow-y: visible;
}

.game-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 6px;
}

.game-item:hover {
  background: var(--bg-secondary);
}

.game-item.active {
  background: rgba(139, 92, 246, 0.15);
  border: 1px solid var(--accent-purple);
}

.game-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
  overflow: hidden;
}

.game-icon-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.game-info {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.game-name {
  font-size: 15px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.game-tags {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 3px;
  line-height: 1.3;
}

.game-item.custom {
  border: 1px dashed var(--border-color);
}

.game-item.custom:hover {
  border-color: var(--accent-purple);
}

.custom-tag {
  display: inline-block;
  font-size: 11px;
  padding: 1px 6px;
  background: var(--accent-purple);
  color: white;
  border-radius: 4px;
  margin-left: 6px;
  vertical-align: middle;
}

.game-delete {
  opacity: 0;
  color: var(--text-muted);
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s;
}

.game-item:hover .game-delete {
  opacity: 1;
}

.game-delete:hover {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
}

.add-game-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px;
  margin: 10px;
  border: 2px dashed var(--border-color);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text-muted);
  font-size: 14px;
  font-weight: 500;
}

.add-game-btn:hover {
  border-color: var(--accent-purple);
  color: var(--accent-purple);
  background: rgba(139, 92, 246, 0.05);
}

/* 添加游戏弹窗样式 */
.add-game-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.add-game-form .form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.add-game-form label {
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
}

.icon-upload-area {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.icon-upload {
  width: 120px;
  height: 120px;
  border: 2px dashed var(--border-color);
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text-muted);
  background: var(--bg-secondary);
}

.icon-upload:hover {
  border-color: var(--accent-purple);
  color: var(--accent-purple);
}

.icon-upload span {
  font-size: 13px;
  font-weight: 500;
}

.icon-preview {
  width: 120px;
  height: 120px;
  border-radius: 16px;
  overflow: hidden;
  position: relative;
  border: 2px solid var(--border-color);
}

.icon-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.icon-actions {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  opacity: 0;
  transition: opacity 0.2s;
}

.icon-preview:hover .icon-actions {
  opacity: 1;
}

.action-icon {
  color: white;
  cursor: pointer;
  transition: color 0.2s;
}

.action-icon:hover {
  color: var(--accent-purple);
}

.action-icon.delete:hover {
  color: #ef4444;
}

.icon-picker-title {
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 4px;
}

.icon-picker {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.icon-option {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  border: 2px solid var(--border-color);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  background: var(--bg-secondary);
}

.icon-option:hover {
  border-color: var(--accent-purple);
}

.icon-option.active {
  border-color: var(--accent-purple);
  background: rgba(139, 92, 246, 0.2);
}

.color-picker {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.color-option {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  border: 3px solid transparent;
  transition: all 0.2s;
}

.color-option:hover {
  transform: scale(1.1);
}

.color-option.active {
  border-color: white;
  transform: scale(1.1);
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.3);
}

/* Element Plus 弹窗样式覆盖 */
.add-game-form :deep(.el-input__wrapper) {
  background-color: var(--bg-secondary);
  box-shadow: 0 0 0 1px var(--border-color) inset;
}

.add-game-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--accent-purple) inset;
}

.add-game-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--accent-purple) inset;
}

.add-game-form :deep(.el-input__inner) {
  color: var(--text-primary);
}

/* 主内容区 */
.chat-main {
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow: hidden;
}

/* 画面分析区 */
.screen-analysis {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  flex: 1;
  min-height: 300px;
  overflow: auto;
}

.analysis-area {
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-secondary);
  position: relative;
  padding: 32px;
}

.placeholder {
  text-align: center;
  color: var(--text-secondary);
}

.placeholder-icon {
  font-size: 72px;
  margin-bottom: 16px;
}

.placeholder h3 {
  font-size: 22px;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.placeholder p {
  font-size: 16px;
  margin-bottom: 24px;
}

.btn-capture {
  background: var(--gradient-primary);
  color: white;
  border: none;
  padding: 14px 32px;
  border-radius: 12px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  font-size: 16px;
}

.btn-capture:hover:not(:disabled) {
  opacity: 0.9;
}

.btn-capture:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.capturing {
  position: relative;
  width: 100%;
  min-height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 24px;
}

.capture-badge {
  position: absolute;
  top: 16px;
  left: 16px;
  background: rgba(220, 38, 38, 0.9);
  padding: 6px 14px;
  border-radius: 8px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: white;
}

.rec-dot {
  width: 8px;
  height: 8px;
  background: white;
  border-radius: 50%;
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.game-visual {
  width: 160px;
  height: 160px;
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0.6;
}

.visual-emoji {
  font-size: 88px;
}

.visual-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 12px;
}

.capture-controls {
  display: flex;
  gap: 12px;
}

.ctrl-btn {
  background: rgba(37, 37, 66, 0.9);
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  padding: 12px 20px;
  border-radius: 10px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.ctrl-btn:hover {
  border-color: var(--accent-purple);
}

.ctrl-btn.primary {
  background: var(--accent-purple);
  border-color: var(--accent-purple);
}

/* 聊天区 */
.chat-area {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.chat-messages {
  max-height: 180px;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.message {
  display: flex;
  gap: 10px;
}

.message.ai {
  flex-direction: row;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.message.ai .message-avatar {
  background: var(--gradient-primary);
}

/* AI思考中状态 */
.message.thinking .message-bubble {
  display: flex;
  align-items: center;
  gap: 12px;
}

.thinking-indicator {
  display: flex;
  gap: 4px;
}

.thinking-indicator .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--text-secondary);
  animation: bounce 1.4s infinite ease-in-out both;
}

.thinking-indicator .dot:nth-child(1) { animation-delay: -0.32s; }
.thinking-indicator .dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.thinking-text {
  color: var(--text-secondary);
  font-size: 14px;
}

.message.user .message-avatar {
  background: var(--accent-green);
}

.message-bubble {
  max-width: 70%;
}

.message-bubble p {
  background: var(--bg-secondary);
  padding: 12px 16px;
  border-radius: 14px;
  font-size: 15px;
  line-height: 1.5;
  margin: 0;
}

.streaming-cursor {
  display: inline-block;
  width: 7px;
  height: 16px;
  margin-left: 4px;
  vertical-align: -2px;
  border-radius: 2px;
  background: var(--accent-purple);
  animation: cursor-blink 0.8s infinite;
}

@keyframes cursor-blink {
  0%, 45% { opacity: 1; }
  46%, 100% { opacity: 0.15; }
}

.message.user .message-bubble p {
  background: var(--accent-purple);
  color: white;
}

.message-time {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
  display: block;
}

.quick-replies {
  display: flex;
  gap: 10px;
  padding: 0 16px 12px;
  flex-wrap: wrap;
}

.quick-btn {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  padding: 8px 16px;
  border-radius: 16px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-btn:hover {
  border-color: var(--accent-purple);
  color: var(--accent-purple);
}

/* 图片分析提示 */
.image-analysis-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: linear-gradient(90deg, rgba(139, 92, 246, 0.15), rgba(139, 92, 246, 0.05));
  border-top: 1px solid var(--border-color);
  border-bottom: 1px solid var(--border-color);
  font-size: 13px;
  color: var(--accent-purple);
}

.image-analysis-tip .el-icon {
  font-size: 16px;
}

.clear-screenshot-btn {
  margin-left: auto;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(239, 68, 68, 0.2);
  border: none;
  border-radius: 50%;
  color: #ef4444;
  cursor: pointer;
  transition: all 0.2s;
}

.clear-screenshot-btn:hover {
  background: rgba(239, 68, 68, 0.4);
  color: #fff;
}

/* 游戏知识提示（无截图时） */
.game-knowledge-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: linear-gradient(90deg, rgba(16, 185, 129, 0.12), rgba(16, 185, 129, 0.03));
  border-top: 1px solid var(--border-color);
  border-bottom: 1px solid var(--border-color);
  font-size: 13px;
  color: #10b981;
}

.game-knowledge-tip .el-icon {
  font-size: 16px;
}

/* 无截图时的提示 */
.no-screenshot-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  padding: 10px 14px;
  background: rgba(139, 92, 246, 0.1);
  border-radius: 10px;
  font-size: 13px;
  color: var(--accent-purple);
  line-height: 1.5;
}

.no-screenshot-tip .el-icon {
  font-size: 16px;
  flex-shrink: 0;
}

.chat-input-area {
  display: flex;
  gap: 12px;
  padding: 14px 16px;
  border-top: 1px solid var(--border-color);
  align-items: center;
}

.voice-conversation-bar {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 10px 16px;
  color: #a78bfa;
  font-size: 13px;
  background: linear-gradient(90deg, rgba(139, 92, 246, 0.16), rgba(59, 130, 246, 0.06));
  border-top: 1px solid rgba(139, 92, 246, 0.28);
}

.voice-conversation-bar button {
  margin-left: auto;
  padding: 5px 10px;
  color: #fca5a5;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.28);
  border-radius: 12px;
  cursor: pointer;
}

.voice-pulse {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: #8b5cf6;
}

.voice-pulse.listening {
  animation: voice-pulse 1.2s infinite;
}

@keyframes voice-pulse {
  0% { box-shadow: 0 0 0 0 rgba(139, 92, 246, 0.65); }
  70% { box-shadow: 0 0 0 8px rgba(139, 92, 246, 0); }
  100% { box-shadow: 0 0 0 0 rgba(139, 92, 246, 0); }
}

.voice-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.voice-btn:hover,
.voice-btn.active {
  background: var(--accent-purple);
  color: white;
  border-color: var(--accent-purple);
}

.voice-btn.listening {
  animation: voice-button-pulse 1.2s infinite;
}

@keyframes voice-button-pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(139, 92, 246, 0.45); }
  50% { box-shadow: 0 0 0 7px rgba(139, 92, 246, 0); }
}

.input-box {
  flex: 1;
}

.input-box input {
  width: 100%;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  border-radius: 22px;
  padding: 12px 18px;
  font-size: 15px;
  outline: none;
}

.input-box input:focus {
  border-color: var(--accent-purple);
}

.send-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--gradient-primary);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.send-btn:hover:not(:disabled) {
  opacity: 0.9;
}

/* 设置弹窗 */
.settings-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.form-item-divider {
  height: 1px;
  background: var(--border-color);
  margin: 8px 0;
}

.form-hint {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
  display: block;
}

.message-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
}

.speak-btn {
  background: transparent;
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.speak-btn:hover {
  border-color: var(--accent-purple);
  color: var(--accent-purple);
}

.speak-btn.active {
  background: var(--accent-purple);
  border-color: var(--accent-purple);
  color: white;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-item label {
  color: var(--text-secondary);
  font-size: 13px;
}

/* 截图相关样式 */
.capture-options {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 16px;
}

.btn-upload {
  background: var(--bg-secondary);
  color: var(--text-primary);
  border: 2px solid var(--border-color);
  padding: 12px 24px;
  border-radius: 12px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 15px;
  transition: all 0.2s;
}

.btn-upload:hover:not(.disabled) {
  border-color: var(--accent-purple);
  color: var(--accent-purple);
}

.btn-upload.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.analysis-area.has-screenshot {
  padding: 16px;
}

.screenshot-display {
  width: 100%;
  max-width: 700px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.screenshot-img {
  max-width: 100%;
  max-height: 500px;
  border-radius: 12px;
  object-fit: contain;
  border: 2px solid var(--border-color);
  background: #000;
}

.screenshot-img.large {
  max-height: 500px;
}

.screenshot-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
}

.screenshot-only {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
}

.screenshot-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.screenshot-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.screenshot-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.ctrl-btn.small {
  padding: 8px 14px;
  font-size: 13px;
}

.ctrl-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.ctrl-btn:disabled:hover {
  border-color: var(--border-color);
}

.screenshot-tip {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}

/* 响应式 */
/* 自定义滚动条 */
.screen-analysis::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.screen-analysis::-webkit-scrollbar-track {
  background: var(--bg-secondary);
  border-radius: 4px;
}

.screen-analysis::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 4px;
}

.screen-analysis::-webkit-scrollbar-thumb:hover {
  background: var(--accent-purple);
}

.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: var(--accent-purple);
}

/* 高光弹窗样式 */
.highlight-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.highlight-preview {
  width: 100%;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  border-radius: 12px;
  background: #000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.highlight-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 12px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-item label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

@media (max-width: 900px) {
  .chat-layout {
    grid-template-columns: 1fr;
    height: auto;
  }
  
  .game-sidebar {
    max-height: 180px;
  }
  
  .status-metrics {
    display: none;
  }

  .capture-options {
    flex-direction: column;
    align-items: center;
  }

  .screenshot-buttons {
    justify-content: center;
  }
}
</style>
