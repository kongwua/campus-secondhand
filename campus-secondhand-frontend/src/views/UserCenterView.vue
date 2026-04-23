<template>
  <div class="user-center-page">
    <div class="page-header">
      <button class="back-btn" @click="goHome">← 返回首页</button>
      <h2 class="page-title">个人中心</h2>
    </div>
    
    <div class="user-card">
      <div class="card-tape"></div>
      
      <div class="user-header">
        <div class="user-avatar">{{ user?.nickname?.charAt(0) || 'U' }}</div>
        <div class="user-info">
          <h3 class="user-name">{{ user?.nickname }}</h3>
          <div class="user-score">
            <span class="score-star">⭐</span>
            <span class="score-value">信用分 {{ user?.creditScore || 100 }}</span>
          </div>
        </div>
      </div>
      
      <div class="tab-nav">
        <button 
          v-for="tab in tabs" 
          :key="tab.key"
          class="tab-btn"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          <span class="tab-icon">{{ tab.icon }}</span>
          <span class="tab-label">{{ tab.label }}</span>
        </button>
      </div>
      
      <div class="tab-content">
        <div v-if="activeTab === 'profile'" class="profile-section">
          <div class="section-sticky">
            <div class="mini-tape"></div>
            <div class="form-group">
              <label class="form-label">🏷️ 昵称</label>
              <input v-model="userForm.nickname" class="form-input" placeholder="你的昵称" />
            </div>
            <div class="form-group">
              <label class="form-label">📱 手机号</label>
              <input v-model="userForm.phone" class="form-input" placeholder="联系方式" />
            </div>
            <div class="form-group">
              <label class="form-label">📧 邮箱</label>
              <input v-model="userForm.email" class="form-input" placeholder="邮箱地址" />
            </div>
            <button class="save-btn" @click="updateProfile">💾 保存修改</button>
          </div>
        </div>
        
        <div v-if="activeTab === 'products'" class="products-section">
          <div v-for="p in myProducts" :key="p.id" class="product-item">
            <div class="item-image"><img :src="p.images?.[0]" /></div>
            <div class="item-info">
              <h4 class="item-title">{{ p.title }}</h4>
              <span class="item-price">¥{{ p.price }}</span>
              <span class="item-status" :class="statusClass(p.status)">{{ statusText(p.status) }}</span>
            </div>
            <div class="item-actions">
              <button class="action-btn edit" @click="editProduct(p.id)">编辑</button>
              <button class="action-btn delete" @click="handleDeleteProduct(p.id)">下架</button>
            </div>
          </div>
          <div v-if="!myProducts.length" class="empty-state">
            <span class="empty-icon">📦</span>
            <p>还没有发布商品</p>
            <button class="publish-btn" @click="goPublish">去发布</button>
          </div>
        </div>
        
        <div v-if="activeTab === 'transactions'" class="transactions-section">
          <div v-for="t in transactions" :key="t.id" class="trans-item">
            <div class="trans-info">
              <span class="trans-title">{{ t.productTitle }}</span>
              <span class="trans-price">¥{{ t.price }}</span>
            </div>
            <span class="trans-status" :class="transClass(t.status)">{{ transText(t.status) }}</span>
          </div>
        </div>
        
        <div v-if="activeTab === 'reviews'" class="reviews-section">
          <div v-for="r in reviews" :key="r.id" class="review-item">
            <div class="review-rating">
              <span v-for="n in 5" :key="n" class="star" :class="{ filled: n <= r.rating }">★</span>
            </div>
            <p class="review-content">{{ r.content }}</p>
            <span class="review-time">{{ formatTime(r.createTime) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../store/auth'
import { updateUser, getUserInfo } from '../api/auth'
import { deleteProduct } from '../api/product'
import api from '../api/request'

const router = useRouter()
const authStore = useAuthStore()

const tabs = [
  { key: 'profile', label: '我的资料', icon: '👤' },
  { key: 'products', label: '我的发布', icon: '📦' },
  { key: 'transactions', label: '交易记录', icon: '📋' },
  { key: 'reviews', label: '收到的评价', icon: '⭐' }
]

const activeTab = ref('profile')
const userForm = ref({ nickname: '', phone: '', email: '' })
const myProducts = ref<any[]>([])
const transactions = ref<any[]>([])
const reviews = ref<any[]>([])

const user = computed(() => authStore.user)

const statusClass = (s: number) => ['selling', 'sold', 'offline'][s]
const statusText = (s: number) => ['在售', '已售', '下架'][s]
const transClass = (s: number) => ['pending', 'active', 'done', 'cancel'][s]
const transText = (s: number) => ['待确认', '进行中', '已完成', '已取消'][s]
const formatTime = (t: string) => t ? new Date(t).toLocaleDateString() : ''

const updateProfile = async () => {
  const res: any = await updateUser(userForm.value)
  if (res.code === 200) { ElMessage.success('资料已更新'); authStore.fetchUser() }
}

const editProduct = (id: number) => router.push(`/publish?edit=${id}`)
const handleDeleteProduct = async (id: number) => {
  const res: any = await deleteProduct(id)
  if (res.code === 200) { ElMessage.success('已下架'); fetchMyProducts() }
}

const fetchMyProducts = async () => {
  const res: any = await api.get('/user/products')
  if (res.code === 200) myProducts.value = res.data
}

const goHome = () => router.push('/')
const goPublish = () => router.push('/publish')

onMounted(async () => {
  const res: any = await getUserInfo()
  if (res.code === 200) userForm.value = res.data
})
</script>

<style scoped>
.user-center-page { max-width: 900px; margin: 0 auto; padding: 24px; }
.page-header { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
.back-btn { background: transparent; border: none; font-family: var(--font-display); color: var(--color-primary); cursor: pointer; }
.page-title { font-family: var(--font-display); color: var(--color-secondary); }
.user-card { background: white; padding: 32px; border-radius: var(--radius-lg); box-shadow: var(--shadow-card); position: relative; }
.card-tape { position: absolute; top: -12px; left: 50%; transform: translateX(-50%); width: 80px; height: 24px; background: var(--color-primary); opacity: 0.9; }
.user-header { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; padding-bottom: 16px; border-bottom: 2px dashed var(--border-light); }
.user-avatar { width: 64px; height: 64px; background: var(--color-primary); color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-family: var(--font-display); font-size: 28px; }
.user-name { font-family: var(--font-display); color: var(--color-secondary); }
.user-score { display: flex; align-items: center; gap: 4px; font-size: 14px; color: var(--text-muted); }
.score-star { color: var(--color-accent-warm); }
.tab-nav { display: flex; gap: 8px; margin-bottom: 24px; flex-wrap: wrap; }
.tab-btn { background: var(--bg-card-alt); border: none; padding: 12px 20px; border-radius: var(--radius-md); cursor: pointer; display: flex; align-items: center; gap: 8px; font-family: var(--font-body); transition: var(--transition-fast); }
.tab-btn.active { background: var(--color-primary); color: white; }
.tab-icon { font-size: 20px; }
.tab-content { min-height: 200px; }
.section-sticky { background: var(--bg-card-alt); padding: 24px; border-radius: var(--radius-sticky); position: relative; }
.mini-tape { position: absolute; top: -8px; left: 20px; width: 40px; height: 16px; background: var(--color-accent-warm); opacity: 0.8; }
.form-group { display: flex; flex-direction: column; gap: 8px; margin-bottom: 16px; }
.form-label { font-family: var(--font-body); color: var(--text-secondary); display: flex; align-items: center; gap: 4px; }
.form-input { border: 2px solid var(--border-light); border-radius: var(--radius-sm); padding: 12px; font-family: var(--font-body); background: white; }
.form-input:focus { border-color: var(--color-primary); outline: none; }
.save-btn { background: var(--color-primary); color: white; border: none; padding: 12px 24px; border-radius: var(--radius-md); font-family: var(--font-display); cursor: pointer; }
.product-item { display: flex; gap: 16px; padding: 16px; background: var(--bg-card-alt); border-radius: var(--radius-md); margin-bottom: 12px; align-items: center; }
.item-image { width: 80px; height: 80px; border-radius: var(--radius-sm); overflow: hidden; }
.item-image img { width: 100%; height: 100%; object-fit: cover; }
.item-info { flex: 1; }
.item-title { font-family: var(--font-body); margin-bottom: 4px; }
.item-price { font-family: var(--font-display); color: var(--text-price); }
.item-status { font-size: 12px; padding: 2px 8px; border-radius: var(--radius-sm); }
.item-status.selling { background: var(--color-acent-cool); color: white; }
.item-status.sold { background: var(--color-accent-pink); color: white; }
.item-status.offline { background: var(--text-muted); color: white; }
.item-actions { display: flex; gap: 8px; }
.action-btn { background: white; border: none; padding: 8px 16px; border-radius: var(--radius-sm); font-family: var(--font-body); cursor: pointer; }
.action-btn.delete { background: var(--color-accent-pink); color: white; }
.empty-state { text-align: center; padding: 40px; }
.empty-icon { font-size: 48px; }
.publish-btn { background: var(--color-primary); color: white; border: none; padding: 12px 24px; border-radius: var(--radius-md); font-family: var(--font-display); cursor: pointer; }
.trans-item { display: flex; justify-content: space-between; padding: 16px; background: var(--bg-card-alt); border-radius: var(--radius-md); margin-bottom: 12px; }
.trans-status { font-size: 12px; padding: 4px 12px; border-radius: var(--radius-sm); background: var(--color-accent-warm); }
.trans-status.done { background: var(--color-acent-cool); color: white; }
.review-item { padding: 16px; background: var(--bg-card-alt); border-radius: var(--radius-md); margin-bottom: 12px; }
.review-rating { display: flex; gap: 4px; margin-bottom: 8px; }
.star { color: var(--border-light); font-size: 20px; }
.star.filled { color: var(--color-accent-warm); }
.review-content { font-family: var(--font-body); margin-bottom: 8px; }
.review-time { font-size: 12px; color: var(--text-muted); }
</style>