<template>
  <div class="transactions-page">
    <div class="page-header">
      <button class="back-btn" @click="goHome">← 返回</button>
      <h2 class="page-title">📋 交易管理</h2>
    </div>
    
    <div class="trans-card">
      <div class="card-tape"></div>
      
      <div class="role-switcher">
        <button 
          class="role-btn"
          :class="{ active: role === 'buyer' }"
          @click="role = 'buyer'"
        >
          <span class="role-icon">🛒</span>
          <span class="role-label">我买的</span>
        </button>
        <button 
          class="role-btn"
          :class="{ active: role === 'seller' }"
          @click="role = 'seller'"
        >
          <span class="role-icon">💰</span>
          <span class="role-label">我卖的</span>
        </button>
      </div>
      
      <div class="trans-list">
        <div 
          v-for="t in currentTransactions" 
          :key="t.id"
          class="trans-item"
        >
          <div class="trans-sticky">
            <div class="mini-tape"></div>
            
            <div class="trans-header">
              <h4 class="trans-title">{{ t.productTitle }}</h4>
              <div class="trans-meta">
                <span class="trans-price">¥{{ t.price }}</span>
                <span class="trans-role">{{ role === 'buyer' ? `卖家: ${t.sellerNickname}` : `买家: ${t.buyerNickname}` }}</span>
              </div>
            </div>
            
            <div class="status-bar">
              <div class="status-track">
                <div class="status-step" :class="{ active: t.status >= 0 }">
                  <span class="step-dot"></span>
                  <span class="step-label">发起</span>
                </div>
                <div class="status-step" :class="{ active: t.status >= 1 }">
                  <span class="step-dot"></span>
                  <span class="step-label">确认</span>
                </div>
                <div class="status-step" :class="{ active: t.status >= 2 }">
                  <span class="step-dot"></span>
                  <span class="step-label">完成</span>
                </div>
              </div>
              <span class="status-badge" :class="statusClass(t.status)">{{ statusText(t.status) }}</span>
            </div>
            
            <div class="trans-actions" v-if="t.status < 3">
              <button v-if="role === 'seller' && t.status === 0" class="action-btn confirm-btn" @click="confirm(t.id)">✅ 确认交易</button>
              <button v-if="t.status === 1" class="action-btn complete-btn" @click="complete(t.id)">🎉 确认完成</button>
              <button v-if="t.status < 2" class="action-btn cancel-btn" @click="cancel(t.id)">❌ 取消</button>
              <button v-if="t.status === 2 && !t.reviewed" class="action-btn review-btn" @click="review(t.id)">⭐ 评价</button>
            </div>
          </div>
        </div>
        
        <div v-if="!currentTransactions.length" class="empty-state">
          <span class="empty-icon">📭</span>
          <p>暂无{{ role === 'buyer' ? '购买' : '销售' }}记录</p>
          <button v-if="role === 'buyer'" class="go-btn" @click="goHome">去逛逛</button>
          <button v-if="role === 'seller'" class="go-btn" @click="goPublish">去发布</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api/request'

const router = useRouter()
const role = ref('buyer')
const buyerTransactions = ref<any[]>([])
const sellerTransactions = ref<any[]>([])

const currentTransactions = computed(() => role.value === 'buyer' ? buyerTransactions.value : sellerTransactions.value)

const statusText = (status: number) => ['待确认', '进行中', '已完成', '已取消'][status] || '未知'
const statusClass = (status: number) => ['pending', 'active', 'done', 'cancel'][status]

const fetchTransactions = async () => {
  const res: any = await api.get(`/transactions/my?role=${role.value}`)
  if (res.code === 200) {
    if (role.value === 'buyer') buyerTransactions.value = res.data
    else sellerTransactions.value = res.data
  }
}

const confirm = async (id: number) => {
  await api.put(`/transactions/${id}/confirm`)
  ElMessage.success('已确认')
  fetchTransactions()
}

const complete = async (id: number) => {
  await api.put(`/transactions/${id}/complete`)
  ElMessage.success('交易完成！')
  fetchTransactions()
}

const cancel = async (id: number) => {
  await api.put(`/transactions/${id}/cancel`)
  ElMessage.success('已取消')
  fetchTransactions()
}

const review = (id: number) => router.push(`/review/${id}`)
const goHome = () => router.push('/')
const goPublish = () => router.push('/publish')

watch(role, fetchTransactions)
onMounted(fetchTransactions)
</script>

<style scoped>
.transactions-page { max-width: 900px; margin: 0 auto; padding: 24px; }
.page-header { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
.back-btn { background: transparent; border: none; font-family: var(--font-display); color: var(--color-primary); cursor: pointer; }
.page-title { font-family: var(--font-display); color: var(--color-secondary); }
.trans-card { background: white; padding: 32px; border-radius: var(--radius-lg); box-shadow: var(--shadow-card); position: relative; }
.card-tape { position: absolute; top: -12px; left: 50%; transform: translateX(-50%); width: 80px; height: 24px; background: var(--color-secondary); opacity: 0.9; }
.role-switcher { display: flex; gap: 16px; margin-bottom: 24px; }
.role-btn { background: var(--bg-card-alt); border: none; padding: 16px 24px; border-radius: var(--radius-md); cursor: pointer; display: flex; align-items: center; gap: 8px; font-family: var(--font-display); transition: var(--transition-bounce); }
.role-btn.active { background: var(--color-primary); color: white; transform: scale(1.05); }
.role-icon { font-size: 24px; }
.trans-list { display: flex; flex-direction: column; gap: 20px; }
.trans-item { }
.trans-sticky { background: var(--bg-card-alt); padding: 20px; border-radius: var(--radius-sticky); position: relative; }
.mini-tape { position: absolute; top: -8px; left: 24px; width: 40px; height: 16px; background: var(--color-accent-warm); opacity: 0.8; }
.trans-header { margin-bottom: 16px; }
.trans-title { font-family: var(--font-display); color: var(--color-secondary); margin-bottom: 8px; }
.trans-meta { display: flex; gap: 16px; align-items: center; }
.trans-price { font-family: var(--font-display); color: var(--text-price); font-size: 1.25rem; }
.trans-role { font-size: 14px; color: var(--text-muted); }
.status-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.status-track { display: flex; gap: 24px; }
.status-step { display: flex; flex-direction: column; align-items: center; gap: 4px; }
.step-dot { width: 12px; height: 12px; background: var(--border-light); border-radius: 50%; }
.status-step.active .step-dot { background: var(--color-primary); }
.step-label { font-size: 12px; color: var(--text-muted); }
.status-badge { font-family: var(--font-display); padding: 4px 12px; border-radius: var(--radius-sm); font-size: 14px; }
.status-badge.pending { background: var(--color-accent-warm); color: var(--color-secondary); }
.status-badge.active { background: var(--color-primary); color: white; }
.status-badge.done { background: var(--color-acent-cool); color: white; }
.status-badge.cancel { background: var(--text-muted); color: white; }
.trans-actions { display: flex; gap: 12px; flex-wrap: wrap; }
.action-btn { background: white; border: none; padding: 10px 16px; border-radius: var(--radius-sm); font-family: var(--font-display); cursor: pointer; transition: var(--transition-fast); }
.action-btn:hover { transform: translateY(-2px); }
.confirm-btn { background: var(--color-primary); color: white; }
.complete-btn { background: var(--color-acent-cool); color: white; }
.cancel-btn { background: var(--text-muted); color: white; }
.review-btn { background: var(--color-accent-warm); color: var(--color-secondary); }
.empty-state { text-align: center; padding: 60px 24px; }
.empty-icon { font-size: 64px; }
.go-btn { background: var(--color-primary); color: white; border: none; padding: 12px 24px; border-radius: var(--radius-md); font-family: var(--font-display); cursor: pointer; margin-top: 16px; }
</style>