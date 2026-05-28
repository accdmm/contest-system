<template>
  <div class="ai-bubble" v-if="store.isLoggedIn">
    <Transition name="bubble-slide">
      <div v-if="!opened" class="bubble-wrapper" @click="openChat">
        <div class="bubble-trigger">
          <div class="bubble-icon">
            <img src="/images/ai-avatar.jpg" alt="AI" class="avatar-img" />
          </div>
          <span class="bubble-pulse"></span>
        </div>
        <div class="wave-arcs">
          <div class="wave-arc"></div>
          <div class="wave-arc"></div>
          <div class="wave-arc"></div>
        </div>
      </div>
    </Transition>

    <Transition name="bubble-panel">
      <div v-if="opened" class="chat-panel">
        <div class="chat-header">
          <span class="chat-header-title">AI 助手</span>
          <button class="chat-close" @click="closeChat">
            <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5" width="18" height="18">
              <path d="M5 5L15 15M15 5L5 15" stroke-linecap="round"/>
            </svg>
          </button>
        </div>

        <div class="chat-messages" ref="messagesRef">
          <div v-for="(msg, i) in messages" :key="i"
               class="msg" :class="msg.role">
            <div v-if="msg.role === 'assistant'" class="msg-avatar">
              <img src="/images/ai-avatar.jpg" alt="AI" />
            </div>
            <div class="msg-content">{{ msg.content }}</div>
            <div v-if="msg.role === 'user'" class="msg-avatar">
              <img v-if="store.user?.avatarUrl" :src="store.user.avatarUrl" />
              <span v-else>{{ store.user?.name?.charAt(0) }}</span>
            </div>
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

.bubble-wrapper {
  position: relative;
  width: 60px;
  height: 60px;
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

.bubble-trigger {
  animation: rock-heartbeat 1.2s ease-in-out infinite;
}

.bubble-trigger:hover {
  animation: none;
  transform: scale(1.1);
  box-shadow: 0 6px 28px rgba(0, 0, 0, 0.22);
}

@keyframes rock-heartbeat {
  0%, 100% { transform: scale(1); box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15); }
  10% { transform: scale(1.10); box-shadow: 0 8px 32px rgba(0, 0, 0, 0.25); }
  18% { transform: scale(0.98); box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15); }
  28% { transform: scale(1.06); box-shadow: 0 6px 24px rgba(0, 0, 0, 0.20); }
  38% { transform: scale(1); box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15); }
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
}

.bubble-pulse {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 2px solid rgba(179, 157, 219, 0.5);
  animation: pulse-ring 2s ease-out infinite;
}

@keyframes pulse-ring {
  0% { transform: scale(1); opacity: 0.6; }
  100% { transform: scale(1.4); opacity: 0; }
}

.wave-arcs {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100%;
  pointer-events: none;
}

.wave-arc {
  position: absolute;
  left: 50%;
  bottom: 0;
  width: 24px;
  height: 8px;
  margin-left: -12px;
  border: 2px solid transparent;
  border-top-color: rgba(179, 157, 219, 0.7);
  border-radius: 50%;
  animation: wave-jump 1.2s ease-in-out infinite;
}

.wave-arc:nth-child(2) {
  width: 32px;
  margin-left: -16px;
  animation-delay: 0.15s;
  border-top-color: rgba(179, 157, 219, 0.5);
}

.wave-arc:nth-child(3) {
  width: 40px;
  margin-left: -20px;
  animation-delay: 0.3s;
  border-top-color: rgba(179, 157, 219, 0.35);
}

@keyframes wave-jump {
  0%, 100% { height: 6px; opacity: 0.2; }
  20% { height: 20px; opacity: 0.9; }
  45% { height: 6px; opacity: 0.2; }
}

.chat-panel {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 360px;
  height: 500px;
  background: #f5f3ef;
  border-radius: 12px;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.2), 0 0 0 1px rgba(201, 168, 76, 0.12);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: #0f1620;
}

.chat-header-title {
  font-family: 'DM Serif Display', Georgia, serif;
  font-size: 1rem;
  color: #c9a84c;
  letter-spacing: 0.03em;
}

.chat-close {
  background: none;
  border: none;
  color: rgba(201, 168, 76, 0.5);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 6px;
  transition: color 0.2s, background 0.2s;
}

.chat-close:hover {
  color: #c9a84c;
  background: rgba(201, 168, 76, 0.1);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background:
    radial-gradient(ellipse at 20% 20%, rgba(201,168,76,0.02) 0%, transparent 60%),
    radial-gradient(ellipse at 80% 80%, rgba(201,168,76,0.015) 0%, transparent 60%),
    #f5f3ef;
}

.msg {
  max-width: 85%;
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.msg.user {
  align-self: flex-end;
}

.msg.assistant {
  align-self: flex-start;
}

.msg-content {
  padding: 10px 14px;
  font-size: 0.88rem;
  line-height: 1.55;
  white-space: pre-wrap;
  word-break: break-word;
}

.msg.user .msg-content {
  background: #1a2332;
  color: #f0ede8;
  border-radius: 10px 10px 2px 10px;
}

.msg.assistant .msg-content {
  background: #fff;
  color: #1a2332;
  border-radius: 10px 10px 10px 2px;
  border: 1px solid #e0ddd7;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.msg-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  flex-shrink: 0;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.msg-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.msg.user .msg-avatar {
  background: #1a2332;
  color: #f0ede8;
  font-size: 0.75rem;
  font-weight: 600;
  border: 1px solid rgba(201,168,76,0.15);
  margin-left: 8px;
}

.msg.assistant .msg-avatar {
  margin-right: 8px;
  border: 1px solid rgba(201,168,76,0.15);
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
  background: #c9a84c;
  animation: dotFade 1.2s infinite;
}

@keyframes dotFade {
  0%, 100% { opacity: 0.2; }
  50% { opacity: 1; }
}

.chat-input {
  display: flex;
  padding: 12px;
  gap: 8px;
  border-top: 1px solid rgba(201, 168, 76, 0.15);
  background: #f5f3ef;
}

.input-field {
  flex: 1;
  padding: 8px 14px;
  border: 1px solid #e0ddd7;
  border-radius: 24px;
  font-size: 0.85rem;
  outline: none;
  background: #fff;
  color: #1a2332;
  transition: border-color 0.2s;
}

.input-field::placeholder {
  color: rgba(26, 35, 50, 0.35);
}

.input-field:focus {
  border-color: #c9a84c;
  box-shadow: 0 0 0 2px rgba(201, 168, 76, 0.1);
}

.send-btn {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  border: none;
  background: #c9a84c;
  color: #0f1620;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: background 0.2s, transform 0.2s;
}

.send-btn:hover:not(:disabled) {
  background: #dbb95a;
  transform: scale(1.05);
}

.send-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.stop-btn {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  border: 1.5px solid #c9a84c;
  background: transparent;
  color: #c9a84c;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: background 0.2s, color 0.2s;
}

.stop-btn:hover {
  background: #c9a84c;
  color: #0f1620;
}

.bubble-slide-enter-active,
.bubble-slide-leave-active {
  transition: all 0.35s cubic-bezier(0.16, 1, 0.3, 1);
}

.bubble-panel-enter-active,
.bubble-panel-leave-active {
  transition: all 0.35s cubic-bezier(0.16, 1, 0.3, 1);
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
