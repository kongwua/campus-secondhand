<template>
  <div class="login-page">
    <!-- Decorative Elements -->
    <div class="page-decor">
      <div class="decor-note note-top">📚</div>
      <div class="decor-note note-mid">💰</div>
      <div class="decor-note note-bot">🌱</div>
    </div>
    
    <!-- Login Card - Notebook Style -->
    <div class="login-card">
      <div class="card-tape"></div>
      
      <!-- Header -->
      <div class="card-header">
        <h2 class="card-title">欢迎回来</h2>
        <p class="card-subtitle">继续你的市集之旅</p>
      </div>
      
      <!-- Login Form -->
      <form class="login-form" @submit.prevent="handleLogin">
        <div class="form-field">
          <label class="field-label">
            <span class="label-icon">👤</span>
            用户名
          </label>
          <input 
            v-model="loginForm.username"
            type="text"
            class="field-input"
            placeholder="输入你的用户名"
            required
          />
        </div>
        
        <div class="form-field">
          <label class="field-label">
            <span class="label-icon">🔑</span>
            密码
          </label>
          <input 
            v-model="loginForm.password"
            type="password"
            class="field-input"
            placeholder="输入密码"
            required
          />
        </div>
        
        <button type="submit" class="submit-btn" :disabled="loading">
          <span v-if="!loading">进入市集</span>
          <span v-else class="loading-text">正在进入...</span>
        </button>
      </form>
      
      <!-- Divider -->
      <div class="card-divider">
        <span class="divider-text">或者</span>
      </div>
      
      <!-- Register Link -->
      <div class="register-zone">
        <p class="register-text">还没有账号？</p>
        <button class="register-link" @click="goRegister">
          ✨ 加入校园市集
        </button>
      </div>
      
      <!-- Footer Note -->
      <div class="card-footer">
        <p class="footer-note">🎓 校园市集 · 校园二手市集</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const loginForm = ref({
  username: '',
  password: ''
})

const handleLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }
  
  loading.value = true
  try {
    const res = await authStore.loginAction(loginForm.value.username, loginForm.value.password)
if (res.code === 200) {
        ElMessage.success('欢迎回来！')
        router.push('/')
      } else {
      ElMessage.error(res.message || '登录失败，请检查用户名密码')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const goRegister = () => router.push('/register')
</script>

<style scoped>
/* ============================================
   登录页 - Notebook Paper Style
   ============================================ */

.login-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 24px;
  position: relative;
  overflow: hidden;
}

/* Decorative Floating Notes */
.page-decor {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.decor-note {
  position: absolute;
  font-size: 48px;
  opacity: 0.15;
  animation: drift 8s ease-in-out infinite;
}

.note-top { top: 10%; left: 5%; animation-delay: 0s; }
.note-mid { top: 50%; right: 10%; animation-delay: 2s; }
.note-bot { bottom: 15%; left: 15%; animation-delay: 4s; }

@keyframes drift {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  50% { transform: translate(20px, 20px) rotate(10deg); }
}

/* Login Card */
.login-card {
  background: white;
  max-width: 400px;
  width: 100%;
  padding: 40px 32px;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  position: relative;
}

.card-tape {
  position: absolute;
  top: -16px;
  left: 50%;
  transform: translateX(-50%);
  width: 100px;
  height: 32px;
  background: var(--color-accent-warm);
  opacity: 0.9;
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.card-title {
  font-family: var(--font-display);
  font-size: 2rem;
  color: var(--color-primary);
  margin-bottom: 8px;
}

.card-subtitle {
  color: var(--text-muted);
  font-size: 14px;
}

/* Form */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-label {
  font-family: var(--font-body);
  font-size: 14px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.label-icon {
  font-size: 16px;
}

.field-input {
  border: 2px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: 14px 16px;
  font-family: var(--font-body);
  font-size: 15px;
  transition: var(--transition-fast);
  background: var(--bg-main);
}

.field-input:focus {
  border-color: var(--color-primary);
  outline: none;
  background: white;
}

.field-input::placeholder {
  color: var(--text-muted);
}

.submit-btn {
  background: var(--color-primary);
  color: white;
  border: none;
  padding: 16px;
  border-radius: var(--radius-md);
  font-family: var(--font-display);
  font-size: 1.1rem;
  cursor: pointer;
  transition: var(--transition-bounce);
  margin-top: 8px;
}

.submit-btn:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 8px 20px rgba(255, 107, 53, 0.3);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading-text {
  animation: pulse 1s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

/* Divider */
.card-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 24px 0;
  position: relative;
}

.card-divider::before {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  height: 1px;
  background: var(--border-light);
}

.divider-text {
  background: white;
  padding: 0 16px;
  color: var(--text-muted);
  font-size: 14px;
}

/* Register Zone */
.register-zone {
  text-align: center;
}

.register-text {
  color: var(--text-muted);
  font-size: 14px;
  margin-bottom: 8px;
}

.register-link {
  background: var(--bg-card-alt);
  border: none;
  padding: 12px 24px;
  border-radius: var(--radius-md);
  font-family: var(--font-display);
  color: var(--color-primary);
  cursor: pointer;
  transition: var(--transition-medium);
}

.register-link:hover {
  background: var(--color-primary);
  color: white;
}

/* Footer */
.card-footer {
  margin-top: 32px;
  text-align: center;
}

.footer-note {
  font-size: 12px;
  color: var(--text-muted);
}
</style>