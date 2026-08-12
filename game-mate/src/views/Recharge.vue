<template>
  <div class="recharge-page">
    <div class="page-wrapper">
      <!-- 余额卡片 -->
      <div class="balance-card fade-in">
        <div class="balance-info">
          <div class="balance-header">
            <span class="balance-label">当前余额</span>
          </div>
          <div class="balance-amount">
            <span class="amount-value">{{ currentBalance.toFixed(1) }}</span>
            <span class="amount-unit">小时</span>
          </div>
          <div class="balance-stats">
            <div class="stat-item">
              <span class="stat-label">已使用</span>
              <span class="stat-value">{{ usedHours.toFixed(1) }} 小时</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-label">会员等级</span>
              <span class="stat-value">{{ memberLevel }}</span>
            </div>
          </div>
          <div class="balance-bonus" v-if="currentBalance > 0">
            🎁 持续充值享受更多优惠
          </div>
        </div>
        <div class="balance-action">
          <button class="btn-recharge" @click="scrollToPlans">立即充值</button>
        </div>
      </div>

      <!-- 时长套餐 -->
      <section class="plans-section">
        <h2 class="section-title">时长套餐</h2>
        <div class="plans-grid">
          <div
            v-for="plan in plans"
            :key="plan.id"
            class="plan-card"
            :class="{ popular: plan.popular, active: selectedPlan?.id === plan.id }"
            @click="selectPlan(plan)"
          >
            <div v-if="plan.popular" class="plan-badge">最受欢迎</div>
            <div class="plan-hours">
              <span class="hours-value">{{ plan.hours }}</span>
              <span class="hours-unit">小时</span>
            </div>
            <div class="plan-price">
              <span class="price-symbol">¥</span>
              <span class="price-value">{{ plan.price }}</span>
            </div>
            <div class="plan-original">¥{{ plan.originalPrice }}</div>
          </div>
        </div>
      </section>

      <!-- 增值服务 -->
      <section class="extras-section">
        <h2 class="section-title">增值服务（开发中）</h2>
        <div class="extras-grid">
          <div
            v-for="extra in extras"
            :key="extra.id"
            class="extra-card"
            :class="{ active: selectedExtras.includes(extra.id), purchased: isExtraPurchased(extra.key) }"
            @click="notifyExtrasUnavailable"
          >
            <div class="extra-icon" :style="{ background: extra.color }">
              {{ extra.icon }}
            </div>
            <div class="extra-info">
              <h3>{{ extra.name }}</h3>
              <p>{{ extra.description }}</p>
              <div class="extra-stats" v-if="isExtraPurchased(extra.key)">
                <div class="extra-stat">
                  <span class="extra-stat-label">剩余</span>
                  <span class="extra-stat-value">{{ getExtraRemaining(extra.key).toFixed(1) }}小时</span>
                </div>
                <div class="extra-stat">
                  <span class="extra-stat-label">已用</span>
                  <span class="extra-stat-value">{{ getExtraUsed(extra.key).toFixed(1) }}小时</span>
                </div>
              </div>
            </div>
            <div class="extra-status" v-if="isExtraPurchased(extra.key)">
              <span class="status-tag purchased">已开通</span>
            </div>
            <div class="extra-check" v-else-if="selectedExtras.includes(extra.id)">
              <el-icon><CircleCheckFilled /></el-icon>
            </div>
          </div>
        </div>
      </section>

      <!-- 底部结算 -->
      <div class="checkout-section">
        <div class="checkout-card">
          <div class="checkout-left">
            <span class="checkout-label">合计</span>
            <span class="checkout-price">¥{{ totalPrice }}</span>
          </div>
          <button class="btn-pay" @click="handleCheckout" :disabled="!selectedPlan">
            <span>立即充值</span>
          </button>
        </div>
      </div>

      <!-- 支付弹窗 -->
      <el-dialog v-model="showPayDialog" title="选择支付方式" width="360px" :show-close="true">
        <div class="pay-content">
          <div class="pay-amount">
            支付金额：<span class="amount">{{ totalPrice }}</span>
          </div>
          <div class="pay-methods">
            <div
              v-for="method in payMethods"
              :key="method.id"
              class="pay-method"
              :class="{ active: selectedPay === method.id }"
              @click="selectedPay = method.id"
            >
              <div class="method-icon" :style="{ background: method.color }">
                {{ method.icon }}
              </div>
              <span>{{ method.name }}</span>
              <el-icon v-if="selectedPay === method.id" class="check-icon"><Check /></el-icon>
            </div>
          </div>
        </div>
        <template #footer>
          <el-button @click="showPayDialog = false">取消</el-button>
          <el-button type="primary" :loading="isPaying" @click="confirmPay">确认支付</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/userStore'
import { useAiChatStore } from '../stores/aiChatStore'
import { getPlanList } from '../api/plan'
import { createOrder, payOrder } from '../api/order'

const router = useRouter()
const userStore = useUserStore()
const aiChatStore = useAiChatStore()

const selectedPlan = ref(null)
const selectedExtras = ref([])
const showPayDialog = ref(false)
const selectedPay = ref('wechat')
const isPaying = ref(false)

const plans = ref([])

const extras = [
  {
    id: 1,
    key: 'voiceCustom',
    icon: '🎙️',
    name: '声优声线定制',
    description: '克隆你喜欢的声优声线',
    price: 288,
    hours: 30,
    color: 'linear-gradient(135deg, #10b981, #059669)'
  },
  {
    id: 2,
    key: 'personalityCustom',
    icon: '🎭',
    name: '专属AI性格定制',
    description: '自定义性格模板',
    price: 68,
    hours: 20,
    color: 'linear-gradient(135deg, #8b5cf6, #6d28d9)'
  }
]

const currentBalance = computed(() => {
  return userStore.balance || aiChatStore.userData.balance
})

const usedHours = computed(() => {
  return aiChatStore.userData.usedHours || 0
})

const memberLevel = computed(() => {
  if (currentBalance.value >= 50) return 'VIP 高级'
  if (currentBalance.value >= 20) return 'VIP 中级'
  if (currentBalance.value >= 5) return 'VIP 基础'
  return '普通用户'
})

function isExtraPurchased(key) {
  return aiChatStore.hasPurchasedExtra(key)
}

function getExtraRemaining(key) {
  const extra = aiChatStore.userData.extras[key]
  if (!extra) return 0
  return Math.max(0, extra.hours - extra.used)
}

function getExtraUsed(key) {
  const extra = aiChatStore.userData.extras[key]
  if (!extra) return 0
  return extra.used
}

const payMethods = [
  { id: 'wechat', name: '微信支付', icon: '💚', color: '#07c160' },
  { id: 'alipay', name: '支付宝', icon: '💙', color: '#1677ff' }
]

const totalPrice = computed(() => {
  return selectedPlan.value ? Number(selectedPlan.value.price) : 0
})

onMounted(async () => {
  try {
    const res = await getPlanList()
    plans.value = (res.data || []).map(plan => ({
      ...plan,
      price: Number(plan.price),
      originalPrice: Number(plan.originalPrice),
      popular: plan.isPopular === 1
    }))
  } catch (error) {
    ElMessage.error('套餐加载失败，请稍后重试')
  }
})

function selectPlan(plan) {
  selectedPlan.value = plan
}

function notifyExtrasUnavailable() {
  ElMessage.info('增值服务尚未接入订单结算，当前仅支持时长套餐')
}

function handleCheckout() {
  if (!selectedPlan.value) {
    ElMessage.warning('请选择一个套餐')
    return
  }
  showPayDialog.value = true
}

function scrollToPlans() {
  const plansSection = document.querySelector('.plans-section')
  if (plansSection) {
    plansSection.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

async function confirmPay() {
  if (!selectedPlan.value || isPaying.value) return

  isPaying.value = true
  try {
    const orderRes = await createOrder({
      planId: selectedPlan.value.id,
      extraIds: selectedExtras.value,
      payMethod: selectedPay.value
    })
    if (!orderRes?.data?.id) {
      throw new Error('订单创建失败，请稍后重试')
    }
    await payOrder(orderRes.data.id, selectedPay.value)
    await userStore.fetchUserInfo()
    aiChatStore.updateUserData({ balance: userStore.balance })

    ElMessage.success(`支付成功！已充值 ${selectedPlan.value.hours} 小时`)
    showPayDialog.value = false
    selectedPlan.value = null
    selectedExtras.value = []
    setTimeout(() => router.push('/ai-chat'), 1000)
  } catch (error) {
    ElMessage.error(error.message || '支付失败，请稍后重试')
  } finally {
    isPaying.value = false
  }
}
</script>

<style scoped>
.recharge-page {
  padding: 24px 32px 120px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-wrapper {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

/* 余额卡片 */
.balance-card {
  background: linear-gradient(135deg, #1e1e38, #252542);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  padding: 36px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.balance-info {
  flex: 1;
}

.balance-header {
  margin-bottom: 12px;
}

.balance-label {
  color: var(--text-secondary);
  font-size: 16px;
}

.balance-amount {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 14px;
}

.amount-value {
  font-size: 56px;
  font-weight: 800;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.amount-unit {
  font-size: 22px;
  color: var(--text-secondary);
}

.balance-stats {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.stat-divider {
  width: 1px;
  height: 30px;
  background: var(--border-color);
}

.balance-bonus {
  color: #f59e0b;
  font-size: 15px;
}

.balance-action {
  flex-shrink: 0;
}

.btn-recharge {
  background: var(--gradient-primary);
  color: white;
  border: none;
  padding: 16px 36px;
  border-radius: 14px;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-recharge:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(139, 92, 246, 0.4);
}

/* 套餐区域 */
.section-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 20px;
}

.plans-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.plans-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.plan-card {
  background: var(--bg-card);
  border: 2px solid var(--border-color);
  border-radius: 18px;
  padding: 32px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.plan-card:hover {
  border-color: var(--accent-purple);
  transform: translateY(-2px);
}

.plan-card.active {
  border-color: var(--accent-purple);
  background: rgba(139, 92, 246, 0.1);
}

.plan-card.popular {
  border-color: var(--accent-purple);
}

.plan-badge {
  position: absolute;
  top: -12px;
  left: 50%;
  transform: translateX(-50%);
  background: var(--gradient-primary);
  color: white;
  padding: 5px 16px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}

.plan-hours {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 6px;
  margin-bottom: 12px;
}

.hours-value {
  font-size: 52px;
  font-weight: 800;
}

.hours-unit {
  font-size: 18px;
  color: var(--text-secondary);
}

.plan-price {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
}

.price-symbol {
  font-size: 18px;
  color: var(--accent-purple);
}

.price-value {
  font-size: 40px;
  font-weight: 700;
  color: var(--accent-purple);
}

.plan-original {
  color: var(--text-muted);
  text-decoration: line-through;
  font-size: 14px;
  margin-top: 6px;
}

/* 增值服务 */
.extras-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.extras-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.extra-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 18px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 18px;
  cursor: pointer;
  transition: all 0.3s;
}

.extra-card:hover {
  border-color: var(--accent-purple);
}

.extra-card.active {
  border-color: var(--accent-purple);
  background: rgba(139, 92, 246, 0.1);
}

.extra-icon {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  flex-shrink: 0;
}

.extra-info {
  flex: 1;
}

.extra-info h3 {
  font-size: 18px;
  margin-bottom: 6px;
}

.extra-info p {
  color: var(--text-secondary);
  font-size: 15px;
}

.extra-stats {
  display: flex;
  gap: 16px;
  margin-top: 10px;
}

.extra-stat {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.extra-stat-label {
  font-size: 12px;
  color: var(--text-muted);
}

.extra-stat-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--accent-purple);
}

.extra-card.purchased {
  border-color: var(--accent-green, #10b981);
  background: rgba(16, 185, 129, 0.08);
}

.extra-status {
  flex-shrink: 0;
}

.status-tag {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
}

.status-tag.purchased {
  background: rgba(16, 185, 129, 0.2);
  color: var(--accent-green, #10b981);
}

.extra-check {
  color: var(--accent-purple);
  font-size: 20px;
}

/* 结算区 */
.checkout-section {
  margin-top: 24px;
  padding-bottom: 24px;
}

.checkout-card {
  background: linear-gradient(135deg, #1e1e38, #252542);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  padding: 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
}

.checkout-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.checkout-label {
  font-size: 16px;
  color: var(--text-secondary);
}

.checkout-price {
  font-size: 40px;
  font-weight: 800;
  color: var(--accent-purple);
}

.btn-pay {
  background: var(--gradient-primary);
  color: white;
  border: none;
  padding: 20px 48px;
  border-radius: 16px;
  font-size: 20px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-pay:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(139, 92, 246, 0.4);
}

.btn-pay:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-pay span {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 支付弹窗 */
.pay-content {
  padding: 8px 0;
}

.pay-amount {
  text-align: center;
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 20px;
}

.amount {
  font-size: 28px;
  font-weight: 800;
  color: var(--accent-purple);
}

.pay-methods {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.pay-method {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border: 2px solid var(--border-color);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.pay-method:hover {
  border-color: var(--accent-purple);
}

.pay-method.active {
  border-color: var(--accent-purple);
  background: rgba(139, 92, 246, 0.1);
}

.method-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.pay-method span {
  flex: 1;
  font-weight: 500;
}

.check-icon {
  color: var(--accent-purple);
  font-size: 18px;
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
@media (max-width: 768px) {
  .plans-grid {
    grid-template-columns: 1fr;
  }
  
  .extras-grid {
    grid-template-columns: 1fr;
  }
}
</style>
