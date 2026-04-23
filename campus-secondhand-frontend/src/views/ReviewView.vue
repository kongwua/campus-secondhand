<template>
  <div class="review-page">
    <div class="page-header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <h2 class="page-title">📝 发表评价</h2>
    </div>
    
    <div class="review-card">
      <div class="card-tape"></div>
      
      <!-- Transaction Info Sticky -->
      <div class="info-sticky">
        <div class="mini-tape"></div>
        <div class="info-content">
          <h4 class="info-title">{{ transactionInfo?.productTitle || '商品信息加载中...' }}</h4>
          <div class="info-meta">
            <span class="info-price">¥{{ transactionInfo?.price || '--' }}</span>
            <span class="info-seller">交易对象: {{ transactionInfo?.otherNickname || '--' }}</span>
          </div>
        </div>
      </div>
      
      <!-- Rating Section -->
      <div class="rating-section">
        <div class="section-label">
          <span class="label-icon">⭐</span>
          <span class="label-text">给这次交易打个分吧</span>
        </div>
        
        <div class="rating-stars">
          <div 
            v-for="i in 5" 
            :key="i"
            class="star-item"
            :class="{ active: form.rating >= i, hover: hoverRating >= i }"
            @click="form.rating = i"
            @mouseenter="hoverRating = i"
            @mouseleave="hoverRating = 0"
          >
            <span class="star-icon">{{ form.rating >= i || hoverRating >= i ? '★' : '☆' }}</span>
          </div>
        </div>
        
        <div class="rating-desc">{{ ratingDescriptions[form.rating] }}</div>
      </div>
      
      <!-- Comment Section -->
      <div class="comment-section">
        <div class="section-label">
          <span class="label-icon">💬</span>
          <span class="label-text">说说你的感受</span>
        </div>
        
        <div class="comment-note">
          <div class="note-lines">
            <textarea 
              v-model="form.content"
              class="comment-input"
              placeholder="交易很顺利，卖家很友好~ 商品质量也很棒！"
              rows="5"
            ></textarea>
          </div>
          <div class="note-footer">
            <span class="char-count">{{ form.content.length }}/200</span>
          </div>
        </div>
      </div>
      
      <!-- Quick Tags -->
      <div class="tags-section">
        <div class="section-label">
          <span class="label-icon">🏷️</span>
          <span class="label-text">快速标签</span>
        </div>
        
        <div class="tags-grid">
          <button 
            v-for="tag in quickTags"
            :key="tag"
            class="tag-btn"
            :class="{ active: selectedTags.includes(tag) }"
            @click="toggleTag(tag)"
          >
            {{ tag }}
          </button>
        </div>
      </div>
      
      <!-- Submit Actions -->
      <div class="actions-bar">
        <button class="submit-btn" @click="submit" :disabled="!canSubmit">
          <span class="btn-icon">🚀</span>
          <span class="btn-text">提交评价</span>
        </button>
        <button class="skip-btn" @click="skipReview">
          <span class="btn-text">稍后评价</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api/request'

const route = useRoute()
const router = useRouter()

const transactionId = Number(route.params.id)
const transactionInfo = ref<any>(null)
const hoverRating = ref(0)

const form = ref({
  rating: 5,
  content: ''
})

const selectedTags = ref<string[]>([])

const quickTags = ['态度友好', '发货及时', '商品如描述', '价格合理', '包装完好', '沟通顺畅']

const ratingDescriptions: Record<number, string> = {
  1: '非常不满意 😞',
  2: '不太满意 😕',
  3: '一般 😐',
  4: '比较满意 😊',
  5: '非常满意 😍'
}

const canSubmit = computed(() => form.value.rating > 0 && form.value.content.trim().length >= 10)

const toggleTag = (tag: string) => {
  const idx = selectedTags.value.indexOf(tag)
  if (idx === -1) selectedTags.value.push(tag)
  else selectedTags.value.splice(idx, 1)
  
  // Add tag to comment
  if (idx === -1 && form.value.content.length < 180) {
    form.value.content += ` #${tag}`
  }
}

const fetchTransactionInfo = async () => {
  try {
    const res: any = await api.get(`/transactions/${transactionId}`)
    if (res.code === 200) {
      transactionInfo.value = res.data
    }
  } catch (e) {
    console.error('Failed to load transaction info')
  }
}

const submit = async () => {
  if (!canSubmit.value) {
    ElMessage.warning('请填写至少10字评价内容')
    return
  }
  
  try {
    const res: any = await api.post('/reviews', {
      transactionId,
      rating: form.value.rating,
      content: form.value.content.trim()
    })
    
    if (res.code === 200) {
      ElMessage.success('评价成功！感谢你的反馈 💖')
      router.push('/transactions')
    } else {
      ElMessage.error(res.message || '评价失败')
    }
  } catch (e) {
    ElMessage.error('网络错误，请稍后重试')
  }
}

const skipReview = () => {
  ElMessage.info('已跳过，可稍后在交易管理中评价')
  router.push('/transactions')
}

const goBack = () => router.back()

onMounted(fetchTransactionInfo)
</script>

<style scoped>
.review-page {
  max-width: 600px;
  margin: 0 auto;
  padding: 24px;
  min-height: 100vh;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.back-btn {
  background: transparent;
  border: none;
  font-family: var(--font-display);
  color: var(--color-primary);
  cursor: pointer;
  font-size: 18px;
  transition: var(--transition-fast);
}

.back-btn:hover {
  transform: translateX(-4px);
}

.page-title {
  font-family: var(--font-display);
  color: var(--color-secondary);
}

/* Main Card */
.review-card {
  background: white;
  padding: 32px 24px;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  position: relative;
}

.card-tape {
  position: absolute;
  top: -12px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 24px;
  background: var(--color-secondary);
  opacity: 0.9;
}

/* Info Sticky */
.info-sticky {
  background: var(--bg-card-alt);
  padding: 16px 20px;
  border-radius: var(--radius-sticky);
  position: relative;
  margin-bottom: 24px;
}

.mini-tape {
  position: absolute;
  top: -8px;
  left: 20px;
  width: 40px;
  height: 16px;
  background: var(--color-accent-warm);
  opacity: 0.8;
}

.info-title {
  font-family: var(--font-display);
  color: var(--color-secondary);
  margin-bottom: 8px;
}

.info-meta {
  display: flex;
  gap: 16px;
  align-items: center;
}

.info-price {
  font-family: var(--font-display);
  color: var(--text-price);
  font-size: 1.25rem;
}

.info-seller {
  font-size: 14px;
  color: var(--text-muted);
}

/* Section Label */
.section-label {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.label-icon {
  font-size: 20px;
}

.label-text {
  font-family: var(--font-display);
  color: var(--color-secondary);
  font-size: 18px;
}

/* Rating Stars */
.rating-section {
  margin-bottom: 32px;
}

.rating-stars {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-bottom: 12px;
}

.star-item {
  cursor: pointer;
  transition: var(--transition-bounce);
}

.star-item:hover {
  transform: scale(1.2);
}

.star-icon {
  font-size: 36px;
  color: var(--border-light);
  transition: var(--transition-fast);
}

.star-item.active .star-icon,
.star-item.hover .star-icon {
  color: var(--color-accent-warm);
}

.rating-desc {
  text-align: center;
  font-family: var(--font-display);
  color: var(--text-secondary);
  font-size: 16px;
}

/* Comment Section */
.comment-section {
  margin-bottom: 24px;
}

.comment-note {
  background: var(--bg-card-alt);
  padding: 20px;
  border-radius: var(--radius-md);
  position: relative;
  border-left: 4px solid var(--color-primary);
}

.note-lines {
  position: relative;
}

.comment-input {
  width: 100%;
  border: none;
  background: transparent;
  font-family: var(--font-body);
  font-size: 16px;
  color: var(--text-primary);
  resize: none;
  line-height: 1.8;
  outline: none;
}

.comment-input::placeholder {
  color: var(--text-muted);
  font-style: italic;
}

.note-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 8px;
}

.char-count {
  font-size: 12px;
  color: var(--text-muted);
}

/* Tags Section */
.tags-section {
  margin-bottom: 32px;
}

.tags-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.tag-btn {
  background: white;
  border: 2px solid var(--border-light);
  padding: 10px 16px;
  border-radius: var(--radius-sm);
  font-family: var(--font-display);
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: var(--transition-fast);
}

.tag-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.tag-btn.active {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: white;
}

/* Actions Bar */
.actions-bar {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.submit-btn {
  background: var(--color-primary);
  color: white;
  border: none;
  padding: 16px 32px;
  border-radius: var(--radius-md);
  font-family: var(--font-display);
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: var(--transition-bounce);
}

.submit-btn:hover:not(:disabled) {
  background: var(--color-primary-light);
  transform: translateY(-4px);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-icon {
  font-size: 20px;
}

.skip-btn {
  background: transparent;
  color: var(--text-muted);
  border: none;
  padding: 16px 24px;
  font-family: var(--font-display);
  cursor: pointer;
  transition: var(--transition-fast);
}

.skip-btn:hover {
  color: var(--color-secondary);
}

/* Mobile Adjustments */
@media (max-width: 480px) {
  .review-page {
    padding: 16px;
  }
  
  .rating-stars {
    gap: 8px;
  }
  
  .star-icon {
    font-size: 28px;
  }
  
  .actions-bar {
    flex-direction: column;
  }
  
  .submit-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>