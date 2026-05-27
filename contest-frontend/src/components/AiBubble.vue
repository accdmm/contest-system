<template>
  <div class="ai-bubble" v-if="store.isLoggedIn">
    <Transition name="bubble-slide">
      <div v-if="!opened" class="bubble-trigger" @click="openChat">
        <div class="bubble-icon">
          <img src="/images/ai-avatar.jpg" alt="AI" class="avatar-img" />
        </div>
        <span class="bubble-pulse"></span>
      </div>
    </Transition>

    <Transition name="bubble-panel">
      <div v-if="opened" class="chat-panel">
        <div class="chat-header">
          <span class="chat-header-title">AI 助手</span>
          <button class="chat-close" @click="closeChat">&times;</button>
        </div>

        <div class="chat-messages" ref="messagesRef">
          <div v-for="(msg, i) in messages" :key="i"
               class="msg" :class="msg.role">
            <div class="msg-content">{{ msg.content }}</div>
          </div>
          <div v-if="loading" class="msg assistant">
            <div class="msg-content thinking">
              <span class="dot-pulse"></span>
            </div>
          </div>
        </div>

        <div class="chat-input">
          <input v-model="inputText" @keydown.enter="send" placeholder="输入消息..."
                 :disabled="loading" class="input-field" />
          <button v-if="!loading" @click="send" :disabled="!inputText.trim()" class="send-btn">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
              <path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z"/>
            </svg>
          </button>
          <button v-else @click="stop" class="stop-btn">
            <svg viewBox="0 0 24 24" width="16" height="16">
              <rect x="6" y="6" width="12" height="12" rx="2" fill="currentColor"/>
            </svg>
          </button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { useUserStore } from '../stores/user'
import { createChatStream, stopChatStream } from '../api/ai'

const store = useUserStore()
const opened = ref(false)
const messages = ref([])
const inputText = ref('')
const loading = ref(false)
const messagesRef = ref(null)
const currentConversationId = ref(null)

const savedMessages = JSON.parse(localStorage.getItem('ai_messages') || '[]')
if (savedMessages.length > 0) {
  messages.value = savedMessages
}

watch(messages, () => {
  localStorage.setItem('ai_messages', JSON.stringify(messages.value))
}, { deep: true })

function openChat() {
  opened.value = true
  if (messages.value.length === 0) {
    const greeting = '你好！我是竞赛助手，可以帮你查询竞赛信息、了解报名状态等，有什么可以帮你的吗？'
    messages.value.push({ role: 'assistant', content: greeting })
  }
  scrollDown()
}

function closeChat() {
  opened.value = false
}

function scrollDown() {
  nextTick(() => {
    const el = messagesRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

function send() {
  const text = inputText.value.trim()
  if (!text || loading.value) return
  if (!localStorage.getItem('token')) {
    messages.value.push({ role: 'assistant', content: '请先登录后再使用AI助手' })
    inputText.value = ''
    return
  }
  inputText.value = ''

  messages.value.push({ role: 'user', content: text })
  loading.value = true
  scrollDown()

  const fullText = ref('')
  createChatStream(
    currentConversationId.value,
    text,
    chunk => {
      fullText.value += chunk
      const last = messages.value[messages.value.length - 1]
      if (last && last.role === 'assistant') {
        last.content = fullText.value
      } else {
        messages.value.push({ role: 'assistant', content: fullText.value })
      }
      scrollDown()
    },
    () => {
      loading.value = false
      scrollDown()
    },
    err => {
      if (err.message && err.message.includes('401')) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        messages.value.push({ role: 'assistant', content: '登录已过期，请重新登录后使用' })
        loading.value = false
        scrollDown()
        return
      }
      messages.value.push({ role: 'assistant', content: '抱歉，连接失败，请稍后重试。' })
      loading.value = false
      scrollDown()
    },
    id => { currentConversationId.value = Number(id) }
  )
}

function stop() {
  if (currentConversationId.value) {
    stopChatStream(currentConversationId.value)
    loading.value = false
  }
}
</script>

<style scoped>
.ai-bubble {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 999;
}

.bubble-trigger {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  transition: transform 0.2s, box-shadow 0.2s;
  position: relative;
  overflow: hidden;
}

.bubble-trigger:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 28px rgba(0, 0, 0, 0.22);
}

.bubble-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-3px); }
}

.bubble-pulse {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 2px solid #3b82f6;
  animation: pulse-ring 2s ease-out infinite;
}

@keyframes pulse-ring {
  0% { transform: scale(1); opacity: 0.6; }
  100% { transform: scale(1.4); opacity: 0; }
}

.chat-panel {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 360px;
  height: 500px;
  background: #f0f4ff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(59, 130, 246, 0.18);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  background: linear-gradient(135deg, #1e3a5f, #3b82f6);
  color: #fff;
}

.chat-header-title {
  font-weight: 600;
  font-size: 0.95rem;
}

.chat-close {
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.7);
  font-size: 1.4rem;
  cursor: pointer;
  line-height: 1;
  padding: 0 4px;
}

.chat-close:hover {
  color: #fff;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: #e8eefb;
}

.msg {
  max-width: 85%;
  display: flex;
}

.msg.user {
  align-self: flex-end;
}

.msg.assistant {
  align-self: flex-start;
}

.msg-content {
  padding: 10px 14px;
  border-radius: 14px;
  font-size: 0.88rem;
  line-height: 1.55;
  white-space: pre-wrap;
  word-break: break-word;
}

.msg.user .msg-content {
  background: linear-gradient(135deg, #3b82f6, #60a5fa);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.msg.assistant .msg-content {
  background: #fff;
  color: #1a2332;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 4px rgba(59, 130, 246, 0.08);
}

.thinking {
  min-width: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dot-pulse {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #999;
  animation: dotPulse 1.2s infinite;
}

@keyframes dotPulse {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 1; }
}

.chat-input {
  display: flex;
  padding: 12px;
  gap: 8px;
  border-top: 1px solid #e8eaed;
  background: #fff;
}

.input-field {
  flex: 1;
  padding: 8px 14px;
  border: 1px solid #d0d4da;
  border-radius: 24px;
  font-size: 0.88rem;
  outline: none;
  transition: border-color 0.2s;
}

.input-field:focus {
  border-color: #3b82f6;
}

.send-btn {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #3b82f6, #60a5fa);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: opacity 0.2s;
}

.send-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.stop-btn {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  border: 2px solid #e74c3c;
  background: #fff;
  color: #e74c3c;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: background 0.2s, color 0.2s;
}

.stop-btn:hover {
  background: #e74c3c;
  color: #fff;
}

.bubble-slide-enter-active,
.bubble-slide-leave-active,
.bubble-panel-enter-active,
.bubble-panel-leave-active {
  transition: all 0.3s ease;
}

.bubble-slide-enter-from,
.bubble-slide-leave-to {
  opacity: 0;
  transform: translateY(12px);
}

.bubble-panel-enter-from,
.bubble-panel-leave-to {
  opacity: 0;
  transform: scale(0.9) translateY(20px);
}
</style>
