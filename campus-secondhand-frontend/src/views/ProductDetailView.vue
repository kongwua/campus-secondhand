<template>
  <div class="detail-page">
    <button class="back-btn" @click="goBack">← 返回市集</button>
    
    <div class="detail-card" v-if="product">
      <div class="card-tape"></div>
      
      <div class="detail-layout">
        <div class="image-section">
          <div class="image-frame">
            <div class="frame-corner top-left"></div>
            <div class="frame-corner top-right"></div>
            <div class="frame-corner bot-left"></div>
           <div class="frame-corner bot-right"></div>
            
            <div class="image-carousel">
              <img 
                v-if="!product.images || product.images.length === 0"
                :src="'/placeholder.png'"
                :alt="product.title"
                class="default-image"
              />
              <div 
                v-for="(img, idx) in product.images" 
                v-else
                :key="idx"
                class="carousel-slide"
                :class="{ active: currentImage === idx }"
              >
                <img :src="img" :alt="product.title" />
              </div>
            </div>
            
            <div class="carousel-nav" v-if="product.images?.length > 1">
              <button class="nav-btn prev" @click="prevImage" :disabled="currentImage === 0">‹</button>
              <div class="nav-dots">
                <span 
                  v-for="(_, idx) in product.images" 
                  :key="idx"
                  class="dot"
                  :class="{ active: currentImage === idx }"
                  @click="currentImage = idx"
                ></span>
              </div>
              <button class="nav-btn next" @click="nextImage" :disabled="currentImage === product.images.length - 1">›</button>
            </div>
          </div>
        </div>
        
        <div class="info-section">
          <div class="info-sticky">
            <div class="sticky-tape"></div>
            
            <div class="sticky-header">
              <h1 class="product-title">{{ product.title }}</h1>
              <div class="price-row">
                <span class="current-price">¥{{ product.price }}</span>
                <span v-if="product.originalPrice" class="original-price">原价 ¥{{ product.originalPrice }}</span>
              </div>
            </div>
            
            <div class="tag-row">
              <span class="tag category">{{ categoryName }}</span>
              <span class="tag views">👀 {{ product.viewCount }}</span>
              <span class="tag time">{{ formatTime(product.createTime) }}</span>
            </div>
            
            <div class="location-row">
              <span class="location-icon">📍</span>
              <span class="location-text">{{ product.location }}</span>
            </div>
          </div>
          
          <div class="desc-card">
            <div class="card-label"><span class="label-icon">📝</span> 商品描述</div>
            <p class="description-text">{{ product.description || '暂无详细描述' }}</p>
          </div>
          
          <div class="seller-card">
            <div class="card-label"><span class="label-icon">🧑</span> 卖家信息</div>
            <div class="seller-info" v-if="seller">
              <div class="seller-avatar">{{ seller.nickname?.charAt(0) }}</div>
              <div class="seller-meta">
                <span class="seller-name">{{ seller.nickname }}</span>
                <div class="seller-score">
                  <span class="score-icon">⭐</span>
                  <span class="score-text">信用分 {{ seller.creditScore }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="action-zone">
            <button class="contact-btn" @click="contactSeller" v-if="isLoggedIn">
              <span class="btn-icon">💬</span>
              <span class="btn-text">联系卖家</span>
            </button>
            <button class="buy-btn" @click="createTransaction" v-if="isLoggedIn && product.status === 1">
              <span class="btn-icon">🛒</span>
              <span class="btn-text">发起交易</span>
            </button>
            <div class="login-hint" v-if="!isLoggedIn">
              <p>登录后可联系卖家</p>
              <button class="login-btn" @click="goLogin">去登录</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductById } from '../api/product'
import { useAuthStore } from '../store/auth'
import api from '../api/request'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const product = ref<any>(null)
const seller = ref<any>(null)
const categoryName = ref('')
const currentImage = ref(0)

const isLoggedIn = computed(() => authStore.isLoggedIn)

const fetchDetail = async () => {
  const id = Number(route.params.id)
  try {
    const res: any = await getProductById(id)
    if (res && res.id) {
      product.value = res
      seller.value = { id: res.userId, nickname: res.username, creditScore: 100 }
      categoryName.value = res.categoryName || '未分类'
    } else {
      ElMessage.error('商品不存在')
      router.push('/')
    }
  } catch (e) {
    ElMessage.error('商品不存在')
    router.push('/')
  }
}

const formatTime = (time: string) => {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getMonth() + 1}/${d.getDate()} 发布`
}

const prevImage = () => { if (currentImage.value > 0) currentImage.value-- }
const nextImage = () => { if (product.value?.images && currentImage.value < product.value.images.length - 1) currentImage.value++ }

const contactSeller = () => {
  if (!seller.value) return
  router.push(`/messages?to=${seller.value.id}&product=${product.value.id}`)
}

const createTransaction = async () => {
  if (!product.value) return
  try {
    const res: any = await api.post('/transactions', { productId: product.value.id, price: product.value.price })
    if (res.code === 200) {
      ElMessage.success('交易已发起')
      router.push('/transactions')
    } else { ElMessage.error(res.message) }
  } catch (e: any) { ElMessage.error(e.message || '发起失败') }
}

const goBack = () => router.push('/')
const goLogin = () => router.push('/login')

onMounted(fetchDetail)
</script>

<style scoped>
.detail-page { max-width: 1000px; margin: 0 auto; padding: 24px; }
.back-btn { background: transparent; border: none; font-family: var(--font-display); color: var(--color-primary); cursor: pointer; margin-bottom: 24px; }
.detail-card { background: white; padding: 32px; border-radius: var(--radius-lg); box-shadow: var(--shadow-card); position: relative; }
.card-tape { position: absolute; top: -16px; left: 50%; transform: translateX(-50%); width: 100px; height: 32px; background: var(--color-accent-warm); opacity: 0.9; }
.detail-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 40px; }
@media (max-width: 700px) { .detail-layout { grid-template-columns: 1fr; } }
.image-section { }
.image-frame { position: relative; padding: 16px; background: var(--bg-main); border-radius: var(--radius-md); }
.frame-corner { position: absolute; width: 20px; height: 20px; background: var(--color-primary); opacity: 0.3; }
.frame-corner.top-left { top: 0; left: 0; }
.frame-corner.top-right { top: 0; right: 0; }
.frame-corner.bot-left { bottom: 0; left: 0; }
.frame-corner.bot-right { bottom: 0; right: 0; }
.image-carousel { position: relative; aspect-ratio: 1; overflow: hidden; border-radius: var(--radius-sm); }
.carousel-slide { position: absolute; inset: 0; opacity: 0; transition: opacity 0.5s ease; }
.carousel-slide.active { opacity: 1; }
.carousel-slide img { width: 100%; height: 100%; object-fit: cover; }
.default-image { width: 100%; height: 100%; object-fit: contain; background: var(--bg-card-alt); }
.carousel-nav { display: flex; justify-content: center; align-items: center; gap: 12px; margin-top: 12px; }
.nav-btn { background: var(--color-primary); color: white; border: none; width: 32px; height: 32px; border-radius: 50%; cursor: pointer; font-size: 18px; }
.nav-btn:disabled { opacity: 0.3; cursor: not-allowed; }
.nav-dots { display: flex; gap: 8px; }
.dot { width: 8px; height: 8px; background: var(--border-light); border-radius: 50%; cursor: pointer; }
.dot.active { background: var(--color-primary); transform: scale(1.3); }
.info-section { display: flex; flex-direction: column; gap: 20px; }
.info-sticky { background: var(--bg-card-alt); padding: 24px; border-radius: var(--radius-sticky); position: relative; }
.sticky-tape { position: absolute; top: -12px; left: 50%; transform: translateX(-50%); width: 60px; height: 20px; background: var(--color-primary); opacity: 0.8; }
.sticky-header { margin-bottom: 16px; }
.product-title { font-family: var(--font-display); color: var(--color-secondary); margin-bottom: 12px; }
.price-row { display: flex; align-items: center; gap: 12px; }
.current-price { font-family: var(--font-display); color: var(--text-price); font-size: 2rem; }
.original-price { font-size: 1rem; color: var(--text-muted); text-decoration: line-through; }
.tag-row { display: flex; gap: 12px; margin-bottom: 12px; }
.tag { font-size: 12px; padding: 4px 12px; background: white; border-radius: var(--radius-sm); }
.tag.category { background: var(--color-primary); color: white; }
.location-row { display: flex; align-items: center; gap: 8px; padding-top: 8px; border-top: 1px dashed var(--border-light); }
.location-icon { font-size: 20px; }
.location-text { font-family: var(--font-body); color: var(--text-secondary); }
.desc-card, .seller-card { background: white; padding: 20px; border-radius: var(--radius-md); box-shadow: var(--shadow-card); }
.card-label { font-family: var(--font-display); color: var(--color-primary); margin-bottom: 12px; display: flex; align-items: center; gap: 8px; }
.label-icon { font-size: 20px; }
.description-text { font-size: 15px; line-height: 1.8; color: var(--text-secondary); }
.seller-info { display: flex; align-items: center; gap: 12px; }
.seller-avatar { width: 48px; height: 48px; background: var(--color-primary); color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-family: var(--font-display); font-size: 24px; }
.seller-meta { display: flex; flex-direction: column; gap: 4px; }
.seller-name { font-family: var(--font-display); }
.seller-score { display: flex; align-items: center; gap: 4px; font-size: 14px; color: var(--text-secondary); }
.score-icon { color: var(--color-accent-warm); }
.action-zone { display: flex; gap: 16px; padding-top: 8px; }
.contact-btn, .buy-btn { flex: 1; padding: 16px; border: none; border-radius: var(--radius-md); font-family: var(--font-display); cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 8px; transition: var(--transition-bounce); }
.contact-btn { background: var(--bg-header); color: white; }
.buy-btn { background: var(--color-primary); color: white; }
.contact-btn:hover, .buy-btn:hover { transform: scale(1.05); }
.login-hint { text-align: center; padding: 16px; background: var(--bg-card-alt); border-radius: var(--radius-md); }
.login-hint p { color: var(--text-muted); margin-bottom: 8px; }
.login-btn { background: var(--color-primary); color: white; border: none; padding: 12px 24px; border-radius: var(--radius-sm); font-family: var(--font-display); cursor: pointer; }
</style>