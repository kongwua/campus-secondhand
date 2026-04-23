<template>
  <div class="home-page">
    <!-- Header - Campus Market Style -->
    <header class="market-header">
      <div class="header-content">
        <!-- Logo with handwritten feel -->
        <div class="brand">
          <span class="brand-icon">📚</span>
          <h1 class="brand-name">校园二手市集</h1>
        </div>
        
        <!-- Search Bar - Sticky Note Style -->
        <div class="search-sticky">
          <div class="sticky-tape"></div>
          <div class="search-body">
            <input 
              v-model="searchKeyword"
              type="text"
              class="search-input"
              placeholder="找教材、耳机、台灯..."
              @keyup.enter="handleSearch"
            />
            <select v-model="selectedCategory" class="category-select">
              <option value="">全分类</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                {{ cat.name }}
              </option>
            </select>
            <button class="search-btn" @click="handleSearch">
              <span class="btn-icon">🔍</span>
              <span class="btn-text">搜一搜</span>
            </button>
          </div>
        </div>
        
        <!-- User Actions -->
        <div class="user-zone">
          <template v-if="isLoggedIn">
            <div class="user-badge" @click="toggleUserMenu">
              <span class="user-avatar">{{ user?.nickname?.charAt(0) || '用' }}</span>
              <span class="user-name">{{ user?.nickname }}</span>
              <div v-if="showUserMenu" class="user-menu">
                <div class="menu-item" @click="goUserCenter">📦 我的发布</div>
                <div class="menu-item" @click="goMessages">💬 消息中心</div>
                <div class="menu-item highlight" @click="goPublish">✨ 发布新宝贝</div>
                <div class="menu-item" @click="handleLogout">👋 退出登录</div>
              </div>
            </div>
          </template>
          <template v-else>
            <button class="auth-btn login-btn" @click="goLogin">登录</button>
            <button class="auth-btn register-btn" @click="goRegister">加入市集</button>
          </template>
        </div>
      </div>
    </header>
    
    <!-- Hero Banner -->
    <section class="hero-banner">
      <div class="hero-content">
        <h2 class="hero-title">
          <span class="title-line">让闲置</span>
          <span class="title-line highlight">流转起来</span>
        </h2>
        <p class="hero-subtitle">毕业季清仓 · 闲置变现 · 环保循环</p>
        <button class="publish-btn" @click="goPublish">
          <span>发布我的宝贝</span>
          <span class="btn-arrow">→</span>
        </button>
      </div>
      <div class="hero-decor">
        <div class="floating-note note-1">教材</div>
        <div class="floating-note note-2">耳机</div>
        <div class="floating-note note-3">台灯</div>
      </div>
    </section>
    
    <!-- Category Quick Nav -->
    <section class="category-nav">
      <div 
        v-for="cat in categories" 
        :key="cat.id" 
        class="cat-chip"
        :class="{ active: selectedCategory === cat.id }"
        @click="selectCategory(cat.id)"
      >
        <span class="cat-emoji">{{ getCategoryEmoji(cat.name) }}</span>
        <span class="cat-name">{{ cat.name }}</span>
      </div>
    </section>
    
    <!-- Product Grid - Sticky Notes Style -->
    <main class="product-section">
      <div class="section-header">
        <h3 class="section-title">
          <span class="title-decor">📍</span>
          最新上架
        </h3>
        <span class="product-count">共 {{ total }} 件宝贝</span>
      </div>
      
      <div class="product-grid">
        <div 
          v-for="(product, index) in products" 
          :key="product.id"
          class="product-card"
          :style="{ '--rotate': getCardRotate(index) }"
          @click="goDetail(product.id)"
        >
          <!-- Sticky note tape -->
          <div class="card-tape"></div>
          
          <!-- Product Image -->
          <div class="card-image">
            <img 
              :src="product.images?.[0] || '/placeholder.png'" 
              :alt="product.title"
              loading="lazy"
            />
            <div v-if="product.status === 0" class="sold-badge offline">已下架</div>
            <div v-if="product.status === 2" class="sold-badge sold">已售</div>
          </div>
          
          <!-- Product Info -->
          <div class="card-body">
            <h4 class="product-title">{{ product.title }}</h4>
            <div class="product-meta">
              <span class="product-price">¥{{ product.price }}</span>
              <span v-if="product.originalPrice" class="original-price">
                原价 ¥{{ product.originalPrice }}
              </span>
            </div>
            <div class="product-tags">
              <span class="tag location">📍 {{ product.location }}</span>
              <span class="tag views">👀 {{ product.viewCount }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Pagination -->
      <div class="pagination-zone">
        <button 
          class="page-btn" 
          :disabled="pageNum === 1"
          @click="prevPage"
        >
          ← 上一页
        </button>
        <span class="page-info">第 {{ pageNum }} 页</span>
        <button 
          class="page-btn" 
          :disabled="pageNum * pageSize >= total"
          @click="nextPage"
        >
          下一页 →
        </button>
      </div>
    </main>
    
    <!-- Footer -->
    <footer class="market-footer">
      <p class="footer-text">校园市集 · 校园二手市集 · 让闲置流转</p>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProducts, getCategories, searchProducts, Category, Product, PageResult } from '../api/product'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const authStore = useAuthStore()

// State
const products = ref<Product[]>([])
const categories = ref<Category[]>([])
const searchKeyword = ref('')
const selectedCategory = ref<number | undefined>()
const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)
const showUserMenu = ref(false)

// Computed
const isLoggedIn = computed(() => authStore.isLoggedIn)
const user = computed(() => authStore.user)

// Helpers
const getCategoryEmoji = (name: string) => {
  const emojis: Record<string, string> = {
    '电子产品': '📱',
    '书籍教材': '📚',
    '生活用品': '🛋️',
    '运动器材': '⚽',
    '其他': '🎯'
  }
  return emojis[name] || '📦'
}

const getCardRotate = (index: number) => {
  const rotates = [-2, 1, -1, 2, -3, 1, 2, -1]
  return rotates[index % rotates.length] + 'deg'
}

// Methods
const fetchProducts = async () => {
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    const res: PageResult<Product> = await getProducts(params)
    products.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    console.error('获取商品失败', e)
  }
}

const fetchCategories = async () => {
  try {
    categories.value = await getCategories()
  } catch (e) {
    console.error('获取分类失败', e)
  }
}

const handleSearch = async () => {
  pageNum.value = 1
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value,
      categoryId: selectedCategory.value
    }
    const res: PageResult<Product> = await searchProducts(params)
    products.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    console.error('搜索失败', e)
  }
}

const selectCategory = (catId: number) => {
  selectedCategory.value = selectedCategory.value === catId ? undefined : catId
  handleSearch()
}

const prevPage = () => {
  if (pageNum.value > 1) {
    pageNum.value--
    fetchProducts()
  }
}

const nextPage = () => {
  if (pageNum.value * pageSize.value < total.value) {
    pageNum.value++
    fetchProducts()
  }
}

// Navigation
const goDetail = (id: number) => router.push(`/product/${id}`)
const goLogin = () => router.push('/login')
const goRegister = () => router.push('/register')
const goUserCenter = () => router.push('/user')
const goPublish = () => router.push('/publish')
const goMessages = () => router.push('/messages')

const toggleUserMenu = () => showUserMenu.value = !showUserMenu.value

const handleLogout = async () => {
  await authStore.logoutAction()
  ElMessage.success('再见，下次再来逛逛~')
}

// Lifecycle
onMounted(() => {
  fetchProducts()
  fetchCategories()
  if (isLoggedIn.value) {
    authStore.fetchUser()
  }
})
</script>

<style scoped>
/* ============================================
   首页 - Campus Market Notebook Style
   ============================================ */

.home-page {
  min-height: 100vh;
  padding-bottom: 60px;
}

/* Header */
.market-header {
  background: var(--bg-header);
  padding: 16px 24px;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 10px rgba(45, 58, 74, 0.1);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1200px;
  margin: 0 auto;
  gap: 24px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
}

.brand-icon {
  font-size: 32px;
}

.brand-name {
  font-family: var(--font-display);
  font-size: 1.5rem;
  color: white;
  white-space: nowrap;
}

/* Search - Sticky Note Style */
.search-sticky {
  position: relative;
  flex: 1;
  max-width: 500px;
}

.sticky-tape {
  position: absolute;
  top: -12px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 24px;
  background: var(--color-accent-warm);
  opacity: 0.9;
  border-radius: 2px;
}

.search-body {
  background: var(--bg-card-alt);
  padding: 12px 16px;
  border-radius: var(--radius-sticky);
  box-shadow: var(--shadow-sticky);
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  font-family: var(--font-body);
  font-size: 14px;
  color: var(--text-primary);
  outline: none;
}

.search-input::placeholder {
  color: var(--text-muted);
}

.category-select {
  border: none;
  background: white;
  padding: 8px 12px;
  border-radius: var(--radius-sm);
  font-family: var(--font-body);
  cursor: pointer;
}

.search-btn {
  background: var(--color-primary);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  font-family: var(--font-display);
  transition: var(--transition-bounce);
}

.search-btn:hover {
  transform: scale(1.05);
}

/* User Zone */
.user-zone {
  display: flex;
  gap: 12px;
  align-items: center;
}

.user-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  position: relative;
}

.user-avatar {
  width: 36px;
  height: 36px;
  background: var(--color-primary);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-size: 18px;
}

.user-name {
  color: white;
  font-family: var(--font-display);
}

.user-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  padding: 8px 0;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  min-width: 160px;
}

.menu-item {
  padding: 12px 16px;
  cursor: pointer;
  transition: var(--transition-fast);
  font-size: 14px;
}

.menu-item:hover {
  background: var(--bg-card-alt);
}

.menu-item.highlight {
  color: var(--color-primary);
  font-family: var(--font-display);
}

.auth-btn {
  background: transparent;
  border: 2px solid white;
  color: white;
  padding: 8px 16px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-family: var(--font-display);
  transition: var(--transition-medium);
}

.auth-btn:hover {
  background: white;
  color: var(--color-secondary);
}

.register-btn {
  background: var(--color-primary);
  border-color: var(--color-primary);
}

.register-btn:hover {
  background: var(--color-primary-light);
  border-color: var(--color-primary-light);
  color: white;
}

/* Hero Banner */
.hero-banner {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  padding: 60px 24px;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.hero-content {
  max-width: 600px;
  margin: 0 auto;
}

.hero-title {
  font-family: var(--font-display);
  font-size: 3rem;
  color: white;
  margin-bottom: 16px;
  line-height: 1.3;
}

.title-line {
  display: block;
}

.title-line.highlight {
  font-size: 3.5rem;
  text-shadow: 2px 2px 0 rgba(0,0,0,0.1);
}

.hero-subtitle {
  color: rgba(255,255,255,0.9);
  font-size: 1.1rem;
  margin-bottom: 24px;
}

.publish-btn {
  background: white;
  color: var(--color-primary);
  border: none;
  padding: 16px 32px;
  border-radius: var(--radius-lg);
  font-family: var(--font-display);
  font-size: 1.1rem;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: var(--transition-bounce);
}

.publish-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 8px 20px rgba(255,255,255,0.3);
}

.btn-arrow {
  font-size: 1.5rem;
}

.hero-decor {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.floating-note {
  position: absolute;
  background: var(--bg-card-alt);
  padding: 8px 16px;
  border-radius: var(--radius-sticky);
  font-family: var(--font-display);
  color: var(--color-primary);
  box-shadow: var(--shadow-sticky);
  animation: float 4s ease-in-out infinite;
}

.note-1 { top: 20%; left: 10%; animation-delay: 0s; }
.note-2 { top: 40%; right: 15%; animation-delay: 1s; }
.note-3 { bottom: 30%; left: 20%; animation-delay: 2s; }

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(-3deg); }
  50% { transform: translateY(-10px) rotate(3deg); }
}

/* Category Nav */
.category-nav {
  display: flex;
  gap: 12px;
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  flex-wrap: wrap;
  justify-content: center;
}

.cat-chip {
  background: white;
  padding: 12px 20px;
  border-radius: var(--radius-md);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: var(--transition-bounce);
  box-shadow: var(--shadow-card);
}

.cat-chip:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-card-hover);
}

.cat-chip.active {
  background: var(--color-primary);
  color: white;
}

.cat-emoji {
  font-size: 20px;
}

.cat-name {
  font-family: var(--font-body);
  font-weight: 500;
}

/* Product Section */
.product-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title {
  font-family: var(--font-display);
  font-size: 1.5rem;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-decor {
  font-size: 1.25rem;
}

.product-count {
  color: var(--text-muted);
  font-size: 14px;
}

/* Product Grid - Sticky Notes */
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

@media (max-width: 900px) {
  .product-grid { grid-template-columns: repeat(3, 1fr); }
}

@media (max-width: 600px) {
  .product-grid { grid-template-columns: repeat(2, 1fr); }
}

.product-card {
  background: white;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: var(--transition-medium);
  transform: rotate(var(--rotate, 0deg));
  position: relative;
  overflow: hidden;
  box-shadow: var(--shadow-card);
}

.product-card:hover {
  transform: rotate(0deg) translateY(-8px) scale(1.02);
  box-shadow: var(--shadow-card-hover);
}

.card-tape {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 20px;
  background: var(--color-accent-warm);
  opacity: 0.8;
  z-index: 1;
}

.card-image {
  position: relative;
  height: 180px;
  overflow: hidden;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: var(--transition-medium);
}

.product-card:hover .card-image img {
  transform: scale(1.1);
}

.sold-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: var(--color-accent-pink);
  color: white;
  padding: 4px 12px;
  border-radius: var(--radius-sm);
  font-family: var(--font-display);
  font-size: 12px;
}

.sold-badge.offline {
  background: var(--text-muted);
}

.sold-badge.sold {
  background: var(--color-accent-pink);
}

.card-body {
  padding: 16px;
}

.product-title {
  font-family: var(--font-body);
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.product-price {
  font-family: var(--font-display);
  color: var(--text-price);
  font-size: 1.25rem;
}

.original-price {
  font-size: 12px;
  color: var(--text-muted);
  text-decoration: line-through;
}

.product-tags {
  display: flex;
  gap: 12px;
}

.tag {
  font-size: 12px;
  color: var(--text-muted);
}

/* Pagination */
.pagination-zone {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 24px;
  padding: 32px 0;
}

.page-btn {
  background: white;
  border: none;
  padding: 12px 24px;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-family: var(--font-display);
  transition: var(--transition-medium);
}

.page-btn:hover:not(:disabled) {
  background: var(--color-primary);
  color: white;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-family: var(--font-body);
  color: var(--text-secondary);
}

/* Footer */
.market-footer {
  text-align: center;
  padding: 24px;
  color: var(--text-muted);
}

.footer-text {
  font-size: 14px;
}
</style>