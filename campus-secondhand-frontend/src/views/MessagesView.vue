<template>
  <div class="messages-page">
    <div class="page-header">
      <button class="back-btn" @click="goHome">← 返回</button>
      <h2 class="page-title">💬 消息中心</h2>
    </div>
    
    <div class="messages-layout">
      <div class="conversations-panel">
        <div class="panel-header">
          <span class="panel-icon">👥</span>
          <span class="panel-title">对话列表</span>
        </div>
        
        <div class="conv-list">
          <div 
            v-for="conv in conversations" 
            :key="conv.userId"
            class="conv-item"
            :class="{ active: activeUserId === conv.userId }"
            @click="selectConversation(conv.userId)"
          >
            <div class="conv-avatar">{{ conv.nickname?.charAt(0) }}</div>
            <div class="conv-info">
              <span class="conv-name">{{ conv.nickname }}</span>
              <span class="conv-preview">{{ conv.lastMessage || '点击查看对话' }}</span>
            </div>
            <div v-if="conv.unread > 0" class="conv-badge">{{ conv.unread }}</div>
          </div>
          
          <div v-if="!conversations.length" class="empty-state">
            <span class="empty-icon">📭</span>
            <p>暂无对话</p>
          </div>
        </div>
      </div>
      
      <div class="chat-panel">
        <div class="chat-card">
          <div class="chat-tape"></div>
          
          <div v-if="activeUserId" class="chat-header">
            <span class="chat-icon">💬</span>
            <span class="chat-with">与 {{ activeUserNickname }} 的对话</span>
          </div>
          
          <div class="message-list" ref="msgList">
            <div 
              v-for="msg in messages" 
              :key="msg.id"
              class="message-item"
              :class="{ mine: msg.senderId === currentUserId }"
            >
              <div class="msg-bubble">
                <p class="msg-content">{{ msg.content }}</p>
                <span class="msg-time">{{ formatTime(msg.createTime) }}</span>
              </div>
            </div>
            
            <div v-if="!messages.length && activeUserId" class="chat-empty">
              <span class="chat-icon-large">👋</span>
              <p>开始聊天吧</p>
            </div>
          </div>
          
          <div class="chat-input-zone" v-if="activeUserId">
            <input 
              v-model="newMessage"
              class="chat-input"
              placeholder="输入消息..."
              @keyup.enter="sendMessage"
            />
            <button class="send-btn" @click="sendMessage">发送 📨</button>
          </div>
          
          <div v-if="!activeUserId" class="no-chat-selected">
            <span class="select-icon">👈</span>
            <p>选择左侧对话开始聊天</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import api from '../api/request'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const conversations = ref<any[]>([])
const messages = ref<any[]>([])
const activeUserId = ref<number>(0)
const newMessage = ref('')
const productId = ref<number>(0)
const msgList = ref<HTMLElement>()

const currentUserId = computed(() => authStore.user?.id)
const activeUserNickname = computed(() => {
  const conv = conversations.value.find(c => c.userId === activeUserId.value)
  return conv?.nickname || '用户'
})

const formatTime = (t: string) => {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`
}

const fetchConversations = async () => {
  const res: any = await api.get('/messages')
  if (res.code === 200) conversations.value = res.data
}

const fetchMessages = async (userId: number) => {
  activeUserId.value = userId
  const res: any = await api.get(`/messages/conversation/${userId}`)
  if (res.code === 200) {
    messages.value = res.data
    await nextTick()
    if (msgList.value) msgList.value.scrollTop = msgList.value.scrollHeight
  }
}

const sendMessage = async () => {
  if (!newMessage.value.trim()) return
  await api.post('/messages', {
    receiverId: activeUserId.value,
    productId: productId.value,
    content: newMessage.value
  })
  newMessage.value = ''
  fetchMessages(activeUserId.value)
}

const selectConversation = (userId: number) => fetchMessages(userId)
const goHome = () => router.push('/')

onMounted(() => {
  fetchConversations()
  if (route.query.to) {
    activeUserId.value = Number(route.query.to)
    productId.value = Number(route.query.product || 0)
    fetchMessages(activeUserId.value)
  }
})
</script>

<style scoped>
.messages-page { max-width: 1000px; margin: 0 auto; padding: 24px; }
.page-header { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
.back-btn { background: transparent; border: none; font-family: var(--font-display); color: var(--color-primary); cursor: pointer; }
.page-title { font-family: var(--font-display); color: var(--color-secondary); }
.messages-layout { display: grid; grid-template-columns: 280px 1fr; gap: 24px; }
@media (max-width: 700px) { .messages-layout { grid-template-columns: 1fr; } }
.conversations-panel { background: white; border-radius: var(--radius-lg); box-shadow: var(--shadow-card); overflow: hidden; }
.panel-header { background: var(--bg-header); color: white; padding: 16px; display: flex; align-items: center; gap: 8px; }
.panel-title { font-family: var(--font-display); }
.conv-list { padding: 8px; max-height: 400px; overflow-y: auto; }
.conv-item { display: flex; gap: 12px; padding: 12px; border-radius: var(--radius-md); cursor: pointer; transition: var(--transition-fast); align-items: center; }
.conv-item:hover { background: var(--bg-card-alt); }
.conv-item.active { background: var(--color-primary-light); }
.conv-avatar { width: 40px; height: 40px; background: var(--color-primary); color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-family: var(--font-display); }
.conv-info { flex: 1; min-width: 0; }
.conv-name { font-family: var(--font-body); font-weight: 500; display: block; }
.conv-preview { font-size: 12px; color: var(--text-muted); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; display: block; }
.conv-badge { background: var(--color-accent-pink); color: white; font-size: 12px; padding: 2px 8px; border-radius: 10px; }
.empty-state { text-align: center; padding: 40px 16px; color: var(--text-muted); }
.empty-icon { font-size: 48px; }
.chat-panel { }
.chat-card { background: white; border-radius: var(--radius-lg); box-shadow: var(--shadow-card); position: relative; min-height: 500px; display: flex; flex-direction: column; }
.chat-tape { position: absolute; top: -12px; left: 50%; transform: translateX(-50%); width: 80px; height: 24px; background: var(--color-accent-warm); opacity: 0.9; }
.chat-header { padding: 16px; border-bottom: 2px dashed var(--border-light); display: flex; align-items: center; gap: 8px; }
.chat-with { font-family: var(--font-display); color: var(--color-secondary); }
.message-list { flex: 1; padding: 16px; overflow-y: auto; min-height: 300px; }
.message-item { margin-bottom: 12px; }
.message-item.mine { text-align: right; }
.msg-bubble { display: inline-block; max-width: 70%; padding: 12px 16px; border-radius: var(--radius-md); }
.message-item.mine .msg-bubble { background: var(--color-primary); color: white; }
.message-item:not(.mine) .msg-bubble { background: var(--bg-card-alt); }
.msg-content { font-family: var(--font-body); margin-bottom: 4px; }
.msg-time { font-size: 11px; color: var(--text-muted); }
.message-item.mine .msg-time { color: rgba(255,255,255,0.7); }
.chat-empty { text-align: center; padding: 60px 16px; color: var(--text-muted); }
.chat-icon-large { font-size: 64px; }
.chat-input-zone { padding: 16px; border-top: 2px dashed var(--border-light); display: flex; gap: 12px; }
.chat-input { flex: 1; border: 2px solid var(--border-light); border-radius: var(--radius-md); padding: 12px 16px; font-family: var(--font-body); background: var(--bg-main); }
.chat-input:focus { border-color: var(--color-primary); outline: none; background: white; }
.send-btn { background: var(--color-primary); color: white; border: none; padding: 12px 20px; border-radius: var(--radius-md); font-family: var(--font-display); cursor: pointer; transition: var(--transition-bounce); }
.send-btn:hover { transform: scale(1.05); }
.no-chat-selected { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; color: var(--text-muted); padding: 40px; }
.select-icon { font-size: 48px; }
</style>