<template>
  <div class="publish-page">
    <div class="page-header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <h2 class="page-title">发布我的宝贝</h2>
    </div>
    
    <div class="publish-card">
      <div class="card-tape"></div>
      
      <form class="publish-form" @submit.prevent="handleSubmit">
        <div class="form-section">
          <div class="section-label"><span class="label-icon">📸</span> 商品图片</div>
          <div class="upload-zone">
            <div class="upload-grid">
              <div v-for="(img, idx) in imageList" :key="idx" class="upload-item">
                <img :src="img" />
                <button class="remove-btn" @click="removeImage(idx)">×</button>
              </div>
              <div v-if="imageList.length < 5" class="upload-slot" @click="triggerUpload">
                <div class="slot-icon">📷</div>
                <div class="slot-text">点击上传</div>
                <div class="slot-hint">最多5张</div>
              </div>
            </div>
            <input ref="fileInput" type="file" accept="image/*" hidden @change="handleFileChange" />
          </div>
        </div>
        
        <div class="form-section">
          <div class="section-label"><span class="label-icon">📝</span> 基本信息</div>
          <div class="form-fields">
            <div class="field-row">
              <div class="field-group title-group">
                <label class="field-label">商品标题</label>
                <input v-model="form.title" type="text" class="field-input" placeholder="如：高数教材第七版" maxlength="50" required />
                <span class="field-count">{{ form.title.length }}/50</span>
              </div>
            </div>
            <div class="field-row two-col">
              <div class="field-group">
                <label class="field-label">分类</label>
                <select v-model="form.categoryId" class="field-select" required>
                  <option :value="undefined">选择分类</option>
                  <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
                </select>
              </div>
              <div class="field-group">
                <label class="field-label">交易地点</label>
                <input v-model="form.location" type="text" class="field-input" placeholder="如：宿舍楼A栋" required />
              </div>
            </div>
            <div class="field-row two-col">
              <div class="field-group price-group">
                <label class="field-label">售价 (¥)</label>
                <input v-model.number="form.price" type="number" class="field-input price-input" min="0" step="0.01" required />
              </div>
              <div class="field-group">
                <label class="field-label">原价 (¥)</label>
                <input v-model.number="form.originalPrice" type="number" class="field-input" min="0" step="0.01" />
              </div>
            </div>
          </div>
        </div>
        
        <div class="form-section">
          <div class="section-label"><span class="label-icon">💭</span> 商品描述</div>
          <textarea v-model="form.description" class="desc-input" placeholder="描述一下宝贝的新旧程度..." rows="4"></textarea>
        </div>
        
        <div class="submit-zone">
          <button type="button" class="cancel-btn" @click="goBack">取消</button>
          <button type="submit" class="submit-btn" :disabled="loading || !isFormValid">
            <span v-if="!loading">✨ 发布商品</span>
            <span v-else>正在发布...</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCategories, getUploadUrl, createProduct, Category } from '../api/product'

const router = useRouter()
const loading = ref(false)
const categories = ref<Category[]>([])
const imageList = ref<string[]>([])
const fileInput = ref<HTMLInputElement>()

const form = ref({
  title: '',
  description: '',
  categoryId: undefined as number | undefined,
  price: 0,
  originalPrice: undefined as number | undefined,
  location: '',
  images: [] as string[]
})

const isFormValid = computed(() => form.value.title && form.value.categoryId && form.value.price > 0 && form.value.location)

const triggerUpload = () => fileInput.value?.click()

const handleFileChange = async (e: Event) => {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  try {
    const res: any = await getUploadUrl(file.name)
    const data = res.data || res
    if (data.uploadUrl) {
      await fetch(data.uploadUrl, { method: 'PUT', body: file })
      imageList.value.push(data.objectKey)
      form.value.images.push(data.objectKey)
      ElMessage.success('上传成功')
    }
  } catch (e) { ElMessage.error('上传失败') }
  target.value = ''
}

const removeImage = (idx: number) => { imageList.value.splice(idx, 1); form.value.images.splice(idx, 1) }

const handleSubmit = async () => {
  if (!isFormValid.value) { ElMessage.warning('请填写完整信息'); return }
  loading.value = true
  try {
    const res: any = await createProduct({ 
      ...form.value, 
      images: form.value.images.length > 0 ? form.value.images : null 
    })
    const data = res.data || res
    if (data.id) { 
      ElMessage.success('发布成功！')
      router.push(`/product/${data.id}`)
    } else if (res.code === 200) {
      ElMessage.success('发布成功！')
      router.push('/')
    } else { 
      ElMessage.error(res.message || '发布失败') 
    }
  } catch (e: any) { ElMessage.error(e.message || '发布失败') }
  finally { loading.value = false }
}

const goBack = () => router.push('/')

onMounted(async () => { 
  try {
    const res: any = await getCategories()
    categories.value = Array.isArray(res) ? res : (res.data || [])
  } catch (e) { console.error('获取分类失败', e) }
})
</script>

<style scoped>
.publish-page { max-width: 800px; margin: 0 auto; padding: 24px; }
.page-header { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
.back-btn { background: transparent; border: none; font-family: var(--font-display); color: var(--color-primary); cursor: pointer; }
.page-title { font-family: var(--font-display); color: var(--color-secondary); }
.publish-card { background: white; padding: 32px; border-radius: var(--radius-lg); box-shadow: var(--shadow-card); position: relative; }
.card-tape { position: absolute; top: -12px; left: 50%; transform: translateX(-50%); width: 80px; height: 24px; background: var(--color-acent-cool); opacity: 0.9; }
.publish-form { display: flex; flex-direction: column; gap: 32px; }
.form-section { display: flex; flex-direction: column; gap: 16px; }
.section-label { font-family: var(--font-display); color: var(--color-primary); display: flex; align-items: center; gap: 8px; padding-bottom: 8px; border-bottom: 2px dashed var(--border-light); }
.label-icon { font-size: 24px; }
.upload-zone { padding: 16px; background: var(--bg-card-alt); border-radius: var(--radius-md); }
.upload-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 12px; }
.upload-item { position: relative; aspect-ratio: 1; border-radius: var(--radius-sm); overflow: hidden; }
.upload-item img { width: 100%; height: 100%; object-fit: cover; }
.remove-btn { position: absolute; top: 4px; right: 4px; background: var(--color-accent-pink); color: white; border: none; width: 24px; height: 24px; border-radius: 50%; cursor: pointer; }
.upload-slot { aspect-ratio: 1; background: white; border: 2px dashed var(--color-primary); border-radius: var(--radius-sm); cursor: pointer; display: flex; flex-direction: column; align-items: center; justify-content: center; transition: var(--transition-medium); }
.upload-slot:hover { background: var(--bg-card-alt); transform: scale(1.05); }
.slot-icon { font-size: 32px; }
.slot-text { font-family: var(--font-display); color: var(--color-primary); }
.slot-hint { font-size: 12px; color: var(--text-muted); }
.form-fields { display: flex; flex-direction: column; gap: 16px; }
.field-row { display: flex; gap: 16px; }
.field-row.two-col { display: grid; grid-template-columns: 1fr 1fr; }
.field-group { display: flex; flex-direction: column; gap: 8px; }
.field-label { font-family: var(--font-body); font-size: 14px; color: var(--text-secondary); font-weight: 500; }
.field-input { border: 2px solid var(--border-light); border-radius: var(--radius-sm); padding: 12px 16px; font-family: var(--font-body); background: var(--bg-main); transition: var(--transition-fast); }
.field-input:focus { border-color: var(--color-primary); outline: none; background: white; }
.field-select { border: 2px solid var(--border-light); border-radius: var(--radius-sm); padding: 12px 16px; font-family: var(--font-body); background: var(--bg-main); cursor: pointer; }
.price-input { font-family: var(--font-display); color: var(--color-primary); }
.field-count { font-size: 12px; color: var(--text-muted); text-align: right; }
.desc-input { border: 2px solid var(--border-light); border-radius: var(--radius-md); padding: 16px; font-family: var(--font-body); resize: vertical; min-height: 120px; background: var(--bg-main); }
.desc-input:focus { border-color: var(--color-primary); outline: none; background: white; }
.submit-zone { display: flex; gap: 16px; justify-content: flex-end; padding-top: 16px; border-top: 2px dashed var(--border-light); }
.cancel-btn { background: transparent; border: 2px solid var(--border-light); padding: 12px 24px; border-radius: var(--radius-md); font-family: var(--font-display); cursor: pointer; }
.submit-btn { background: var(--color-primary); color: white; border: none; padding: 12px 32px; border-radius: var(--radius-md); font-family: var(--font-display); cursor: pointer; transition: var(--transition-bounce); }
.submit-btn:hover:not(:disabled) { transform: scale(1.05); }
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }
@media (max-width: 600px) { .field-row.two-col { grid-template-columns: 1fr; } .upload-grid { grid-template-columns: repeat(3, 1fr); } }
</style>