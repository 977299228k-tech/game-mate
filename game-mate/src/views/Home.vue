<template>
  <div class="home-page">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-bg">
        <div class="glow glow-1"></div>
        <div class="glow glow-2"></div>
      </div>
      
      <div class="hero-content fade-in">
        <div class="hero-badge">
          <span class="live-dot"></span>
          7×24在线 · 即刻开黑
        </div>
        
        <h1 class="hero-title">
          AI 电竞伴侣
        </h1>
        
        <p class="hero-subtitle">
          实时捕捉画面，AI语音报攻略，深夜有人陪、关卡不用查。10元/小时，<br>
          零社交压力的专属单机伴侣。
        </p>
        
        <div class="hero-buttons">
          <router-link to="/ai-chat" class="btn-primary">
            <span>开始体验</span>
          </router-link>
          <button class="btn-secondary" @click="showMore = !showMore">
            了解更多
          </button>
        </div>

        <div v-if="showMore" class="hero-details">
          <div class="detail-item">
            <h4>🎯 实时语音对话</h4>
            <p>AI与玩家实时语音交互，支持打断、语气切换</p>
          </div>
          <div class="detail-item">
            <h4>👁️ 游戏场景识别</h4>
            <p>OCR自动识别游戏画面，覆盖多种游戏类型</p>
          </div>
          <div class="detail-item">
            <h4>⚡ 实时攻略提示</h4>
            <p>卡关时主动提示解谜思路、BOSS打法</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 数据统计 -->
    <section class="stats-section">
      <div class="stats-grid">
        <div class="stat-card">
          <span class="stat-value">400+</span>
          <span class="stat-label">覆盖游戏</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-card">
          <span class="stat-value">85%+</span>
          <span class="stat-label">场景识别准确率</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-card">
          <span class="stat-value">10元/小时</span>
          <span class="stat-label">超低价格</span>
        </div>
      </div>
    </section>

    <!-- 热门游戏 -->
    <section class="games-section">
      <div class="section-header">
        <h2 class="section-title">热门游戏</h2>
        <router-link to="#" class="view-all">查看全部 <el-icon :size="14"><ArrowRight /></el-icon></router-link>
      </div>

      <div class="games-grid">
        <div
          v-for="game in gameStore.games"
          :key="game.id"
          class="game-card"
          @click="selectGame(game)"
        >
          <div class="game-image" :style="{ background: game.color }">
            <img
              v-if="game.imageUrl && !imageErrors[game.id]"
              :src="game.imageUrl"
              :alt="game.name"
              class="game-photo"
              @error="imageErrors[game.id] = true"
            />
            <span v-else class="game-emoji">{{ game.icon }}</span>
          </div>
          <div class="game-info">
            <h3>
              {{ game.name }}
              <span v-if="game.isCustom" class="game-custom-badge">自定义</span>
            </h3>
            <span class="game-genre">{{ game.genre }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 核心能力 -->
    <section class="features-section">
      <div class="section-header">
        <h2 class="section-title">核心能力</h2>
      </div>

      <div class="features-grid">
        <div
          v-for="(feature, index) in gameStore.aiFeatures"
          :key="index"
          class="feature-card"
        >
          <div class="feature-icon">{{ feature.icon }}</div>
          <div class="feature-content">
            <h3>{{ feature.title }}</h3>
            <p>{{ feature.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="cta-section">
      <div class="cta-card">
        <h2>开始你的AI陪玩之旅</h2>
        <p class="cta-desc">10元/小时，随时开始，随时停止</p>
        <div class="cta-features">
          <div class="cf-item">
            <el-icon><Check /></el-icon>
            <span>新用户注册赠送2小时体验</span>
          </div>
          <div class="cf-item">
            <el-icon><Check /></el-icon>
            <span>支持400+主流游戏</span>
          </div>
          <div class="cf-item">
            <el-icon><Check /></el-icon>
            <span>7×24小时在线服务</span>
          </div>
        </div>
        <router-link to="/recharge" class="btn-primary large">
          立即充值
        </router-link>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useGameStore } from '../stores/gameStore'

const router = useRouter()
const gameStore = useGameStore()
const showMore = ref(false)
const imageErrors = reactive({})

onMounted(() => {
  gameStore.fetchGames()
})

function selectGame(game) {
  router.push({ path: '/ai-chat', query: { game: game.id } })
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: var(--bg-primary);
  padding: 0 32px 120px;
}

/* Hero Section */
.hero-section {
  position: relative;
  padding: 80px 0 60px;
  text-align: center;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(120px);
  opacity: 0.25;
}

.glow-1 {
  width: 400px;
  height: 400px;
  background: var(--accent-purple);
  top: -150px;
  left: -100px;
}

.glow-2 {
  width: 350px;
  height: 350px;
  background: var(--accent-blue);
  bottom: -100px;
  right: -80px;
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 0 auto;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(139, 92, 246, 0.15);
  border: 1px solid rgba(139, 92, 246, 0.3);
  border-radius: 20px;
  color: var(--accent-purple);
  font-size: 13px;
  margin-bottom: 24px;
}

.live-dot {
  width: 8px;
  height: 8px;
  background: var(--accent-green);
  border-radius: 50%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.hero-title {
  font-size: 64px;
  font-weight: 800;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 20px;
  line-height: 1.1;
}

.hero-subtitle {
  font-size: 18px;
  color: var(--text-secondary);
  line-height: 1.8;
  margin-bottom: 40px;
}

.hero-buttons {
  display: flex;
  gap: 20px;
  justify-content: center;
  margin-bottom: 40px;
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 18px 40px;
  background: var(--gradient-primary);
  color: white;
  text-decoration: none;
  border-radius: 14px;
  font-weight: 600;
  font-size: 17px;
  border: none;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(139, 92, 246, 0.4);
}

.btn-primary.large {
  padding: 16px 40px;
  font-size: 16px;
}

.btn-secondary {
  padding: 14px 32px;
  background: transparent;
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  font-weight: 500;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-secondary:hover {
  border-color: var(--accent-purple);
  color: var(--accent-purple);
}

.hero-details {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  max-width: 800px;
  margin: 0 auto;
  text-align: left;
}

.detail-item {
  background: var(--bg-card);
  padding: 16px;
  border-radius: 12px;
  border: 1px solid var(--border-color);
}

.detail-item h4 {
  font-size: 15px;
  margin-bottom: 6px;
}

.detail-item p {
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.5;
}

/* Stats Section */
.stats-section {
  padding: 48px 0;
  border-top: 1px solid var(--border-color);
  border-bottom: 1px solid var(--border-color);
}

.stats-grid {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 60px;
}

.stat-card {
  text-align: center;
  flex: 1;
}

.stat-value {
  display: block;
  font-size: 44px;
  font-weight: 700;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-label {
  color: var(--text-secondary);
  font-size: 15px;
  margin-top: 6px;
}

.stat-divider {
  width: 1px;
  height: 50px;
  background: var(--border-color);
}

/* Section Header */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
}

.view-all {
  color: var(--accent-purple);
  text-decoration: none;
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.view-all:hover {
  text-decoration: underline;
}

/* Games Section */
.games-section {
  padding: 60px 0;
}

.games-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.game-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.game-card:hover {
  border-color: var(--accent-purple);
  transform: translateY(-4px);
}

.game-image {
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.game-photo {
  width: 100%;
  height: 100%;
  object-fit: cover;
  image-rendering: -webkit-optimize-contrast;
}

.game-emoji {
  font-size: 64px;
  opacity: 0.8;
}

.game-info {
  padding: 16px 18px;
}

.game-info h3 {
  font-size: 18px;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.game-custom-badge {
  font-size: 11px;
  padding: 2px 6px;
  background: var(--accent-purple);
  color: white;
  border-radius: 4px;
  font-weight: 500;
}

.game-genre {
  color: var(--text-secondary);
  font-size: 14px;
}

/* Features Section */
.features-section {
  padding: 60px 0;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.feature-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 28px;
  display: flex;
  gap: 20px;
  transition: all 0.3s;
}

.feature-card:hover {
  border-color: var(--accent-purple);
}

.feature-icon {
  font-size: 36px;
  flex-shrink: 0;
}

.feature-content h3 {
  font-size: 20px;
  margin-bottom: 8px;
}

.feature-content p {
  color: var(--text-secondary);
  font-size: 15px;
  line-height: 1.6;
}

/* CTA Section */
.cta-section {
  padding: 60px 0;
}

.cta-card {
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.1), rgba(59, 130, 246, 0.1));
  border: 1px solid var(--border-color);
  border-radius: 24px;
  padding: 60px;
  text-align: center;
}

.cta-card h2 {
  font-size: 32px;
  margin-bottom: 12px;
}

.cta-desc {
  color: var(--text-secondary);
  font-size: 18px;
  margin-bottom: 32px;
}

.cta-features {
  display: flex;
  justify-content: center;
  gap: 48px;
  margin-bottom: 36px;
  flex-wrap: wrap;
}

.cf-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--text-secondary);
  font-size: 16px;
}

.cf-item .el-icon {
  color: var(--accent-green);
}

/* Responsive */
@media (max-width: 1024px) {
  .games-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .home-page {
    padding: 0 16px 60px;
  }
  
  .hero-title {
    font-size: 36px;
  }
  
  .hero-subtitle {
    font-size: 15px;
  }
  
  .hero-details {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    flex-direction: column;
    gap: 24px;
  }
  
  .stat-divider {
    width: 60px;
    height: 1px;
  }
  
  .games-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .cta-features {
    flex-direction: column;
    align-items: center;
    gap: 16px;
  }
  
  .cta-card {
    padding: 32px 20px;
  }
}
</style>
