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
        <!-- Header -->
        <div class="chat-header">
          <div class="chat-header-left">
            <button class="sidebar-toggle" @click="showSidebar = !showSidebar" :title="showSidebar ? '收起侧栏' : '展开侧栏'">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                <line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/>
              </svg>
            </button>
            <span class="chat-header-title">AI 助手</span>
          </div>
          <button class="chat-close" @click="closeChat">
            <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5" width="18" height="18">
              <path d="M5 5L15 15M15 5L5 15" stroke-linecap="round"/>
            </svg>
          </button>
        </div>

        <div class="chat-body">
          <!-- Sidebar -->
          <div v-if="showSidebar" class="chat-sidebar">
            <div class="sidebar-actions">
              <button class="new-btn" @click="newConversation">
                <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5" width="14" height="14">
                  <line x1="10" y1="3" x2="10" y2="17"/><line x1="3" y1="10" x2="17" y2="10"/>
                </svg>
                新建
              </button>
              <button v-if="!selectMode" class="new-btn" @click="enterSelectMode" title="管理会话">
                <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5" width="14" height="14">
                  <path d="M4 5h12M4 10h12M4 15h8"/>
                </svg>
                管理
              </button>
              <button v-else class="new-btn" @click="exitSelectMode">
                取消
              </button>
            </div>
            <div v-if="selectMode" class="batch-bar">
              <label class="batch-select-all">
                <input type="checkbox" class="sidebar-checkbox"
                       :checked="isAllSelected"
                       @change="toggleSelectAll" />
                <span>全选</span>
              </label>
              <div class="batch-right">
                <span class="batch-count">已选 {{ selectedIds.length }}</span>
                <button v-if="selectedIds.length > 0" class="batch-del-btn" @click="batchDelete">删除</button>
              </div>
            </div>
            <div class="sidebar-list">
              <div v-for="c in conversations" :key="c.id"
                   class="sidebar-item"
                   :class="{ active: !selectMode && c.id === currentConversationId }"
                   @click="selectMode ? toggleSelect(c) : switchConversation(c)">
                <input v-if="selectMode" type="checkbox" class="sidebar-checkbox"
                       :checked="selectedIds.includes(c.id)" @click.stop="toggleSelect(c)" />
                <span class="sidebar-item-title" :title="c.title || '新会话'">{{ c.title || '新会话' }}</span>
                <button v-if="!selectMode" class="sidebar-del" @click.stop="removeConversation(c)"
                        title="删除会话">
                  <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5" width="12" height="12">
                    <path d="M4 5h12M7 5V4a1 1 0 011-1h4a1 1 0 011 1v1M5 5v11a1 1 0 001 1h8a1 1 0 001-1V5"/>
                  </svg>
                </button>
              </div>
              <div v-if="!conversations.length" class="sidebar-empty">暂无会话</div>
            </div>
          </div>

          <!-- Chat Area -->
          <div class="chat-main">
            <div class="chat-messages" ref="messagesRef">
              <div v-for="(msg, i) in messages" :key="i"
                   class="msg" :class="msg.role">
                <div v-if="msg.role === 'assistant'" class="msg-avatar">
                  <img src="/images/ai-avatar.jpg" alt="AI" @error="onImgError" />
                </div>
                <div class="msg-content">{{ msg.content }}</div>
                <div v-if="msg.role === 'user'" class="msg-avatar">
                  <img v-if="store.user?.avatarUrl" :src="store.user.avatarUrl" @error="onImgError" />
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
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { createChatStream, stopChatStream, listConversations, deleteConversation, getConversationMessages, batchDeleteConversations } from '../api/ai'

const store = useUserStore()
const opened = ref(false)
const showSidebar = ref(true)
const messages = ref([])
const conversations = ref([])
const inputText = ref('')
const loading = ref(false)
const messagesRef = ref(null)
const currentConversationId = ref(null)
const selectMode = ref(false)
const selectedIds = ref([])

function enterSelectMode() { selectMode.value = true; selectedIds.value = [] }
function exitSelectMode() { selectMode.value = false; selectedIds.value = [] }

function toggleSelect(c) {
  const idx = selectedIds.value.indexOf(c.id)
  if (idx >= 0) selectedIds.value.splice(idx, 1)
  else selectedIds.value.push(c.id)
}

const isAllSelected = computed(() =>
  conversations.value.length > 0 && selectedIds.value.length === conversations.value.length
)

function toggleSelectAll() {
  if (isAllSelected.value) {
    selectedIds.value = []
  } else {
    selectedIds.value = conversations.value.map(c => c.id)
  }
}

async function batchDelete() {
  if (!selectedIds.value.length) return
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个会话吗？`, '批量删除', {
      type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消'
    })
  } catch { return }
  try {
    const ids = [...selectedIds.value]
    await batchDeleteConversations(ids)
    // 如果当前会话被删了，回到新会话
    if (currentConversationId.value && ids.includes(currentConversationId.value)) {
      newConversation()
    }
    conversations.value = conversations.value.filter(c => !ids.includes(c.id))
    exitSelectMode()
    ElMessage.success(`已删除 ${ids.length} 个会话`)
  } catch { ElMessage.error('批量删除失败') }
}

async function openChat() {
  opened.value = true
  showSidebar.value = window.innerWidth > 640
  await loadConversations()
  if (messages.value.length === 0) {
    const greeting = '你好！我是竞赛助手，可以帮你查询竞赛信息、了解报名状态等，有什么可以帮你的吗？'
    messages.value.push({ role: 'assistant', content: greeting })
  }
  scrollDown()
}

function closeChat() {
  opened.value = false
}

async function loadConversations() {
  try {
    const res = await listConversations()
    conversations.value = res.data || []
  } catch { conversations.value = [] }
}

async function switchConversation(c) {
  currentConversationId.value = c.id
  messages.value = []
  try {
    const res = await getConversationMessages(c.id)
    messages.value = (res.data || []).map(m => ({ role: m.role, content: m.content }))
    scrollDown()
  } catch {
    messages.value.push({ role: 'assistant', content: '加载消息失败，请重试' })
  }
}

function newConversation() {
  currentConversationId.value = null
  messages.value = []
  const greeting = '你好！我是竞赛助手，可以帮你查询竞赛信息、了解报名状态等，有什么可以帮你的吗？'
  messages.value.push({ role: 'assistant', content: greeting })
  scrollDown()
}

async function removeConversation(c) {
  try {
    await ElMessageBox.confirm('确定要删除这个会话吗？', '确认删除', {
      type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消'
    })
  } catch { return }
  try {
    await deleteConversation(c.id)
    if (currentConversationId.value === c.id) {
      newConversation()
    }
    conversations.value = conversations.value.filter(x => x.id !== c.id)
    ElMessage.success('已删除')
  } catch { ElMessage.error('删除失败') }
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
      loadConversations() // 刷新列表以获取新标题
      scrollDown()
    },
    (err, httpStatus) => {
      // httpStatus=401：token 过期，登出
      if (httpStatus === 401) {
        store.logout()
        messages.value.push({ role: 'assistant', content: '登录已过期，请重新登录后使用' })
      } else {
        // httpStatus=0 是 SSE 事件错误（AI 调用失败等），403 是无权限
        const detail = err?.message?.replace(/^\d+:\s*/, '').trim() || '抱歉，连接失败，请稍后重试。'
        messages.value.push({ role: 'assistant', content: httpStatus === 403 ? '没有权限，请重新登录' : detail })
      }
      loading.value = false
      scrollDown()
    },
    id => {
      currentConversationId.value = Number(id)
      loadConversations()
    }
  )
}

function stop() {
  if (currentConversationId.value) {
    stopChatStream(currentConversationId.value)
    loading.value = false
  }
}

/** 图片加载失败时隐藏整个头像容器 */
function onImgError(e) {
  const el = e.target
  if (el) {
    el.style.display = 'none'
    const container = el.closest('.msg-avatar')
    if (container) container.style.display = 'none'
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

/* ── Panel Layout ── */
.chat-panel {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 520px;
  max-width: calc(100vw - 48px);
  height: 540px;
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
  padding: 12px 16px;
  background: #0f1620;
  flex-shrink: 0;
}

.chat-header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sidebar-toggle {
  background: none;
  border: none;
  color: rgba(201, 168, 76, 0.5);
  cursor: pointer;
  display: flex;
  align-items: center;
  padding: 4px;
  border-radius: 4px;
  transition: color 0.2s, background 0.2s;
}

.sidebar-toggle:hover {
  color: #c9a84c;
  background: rgba(201, 168, 76, 0.1);
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

.chat-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* ── Sidebar ── */
.chat-sidebar {
  width: 140px;
  flex-shrink: 0;
  background: #e8e5de;
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(201, 168, 76, 0.12);
}

.new-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin: 8px;
  padding: 6px;
  border: 1px dashed rgba(201, 168, 76, 0.4);
  border-radius: 6px;
  background: transparent;
  color: #8a7a5a;
  font-size: 0.78rem;
  cursor: pointer;
  transition: all 0.2s;
}

.new-btn:hover {
  background: rgba(201, 168, 76, 0.1);
  border-color: #c9a84c;
  color: #c9a84c;
}

.sidebar-list {
  flex: 1;
  overflow-y: auto;
  padding: 4px 8px;
}

.sidebar-item {
  display: flex;
  align-items: center;
  padding: 6px 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.15s;
  margin-bottom: 2px;
  gap: 4px;
}

.sidebar-item:hover {
  background: rgba(201, 168, 76, 0.08);
}

.sidebar-item.active {
  background: rgba(201, 168, 76, 0.15);
}

.sidebar-item-title {
  flex: 1;
  font-size: 0.75rem;
  color: #1a2332;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sidebar-del {
  display: none;
  background: none;
  border: none;
  color: #a09078;
  cursor: pointer;
  padding: 2px;
  border-radius: 3px;
  flex-shrink: 0;
}

.sidebar-item:hover .sidebar-del {
  display: flex;
}

.sidebar-del:hover {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
}

.sidebar-empty {
  padding: 20px 8px;
  text-align: center;
  font-size: 0.75rem;
  color: #a09078;
}

.sidebar-actions {
  display: flex;
  gap: 4px;
  padding: 6px 8px 0;
}
.sidebar-actions .new-btn {
  flex: 1;
  margin: 0;
  font-size: 0.72rem;
  padding: 5px 4px;
}

.batch-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 4px 8px;
  padding: 4px 8px;
  background: rgba(239, 68, 68, 0.08);
  border-radius: 6px;
}
.batch-select-all {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.72rem;
  color: #1a2332;
  cursor: pointer;
}
.batch-select-all .sidebar-checkbox {
  width: 13px;
  height: 13px;
}
.batch-right {
  display: flex;
  align-items: center;
  gap: 6px;
}
.batch-count {
  font-size: 0.72rem;
  color: #ef4444;
}
.batch-del-btn {
  font-size: 0.7rem;
  padding: 2px 8px;
  border: none;
  border-radius: 4px;
  background: #ef4444;
  color: #fff;
  cursor: pointer;
}
.batch-del-btn:hover {
  background: #dc2626;
}

.sidebar-checkbox {
  width: 14px;
  height: 14px;
  cursor: pointer;
  accent-color: #c9a84c;
  flex-shrink: 0;
}

/* ── Chat Area ── */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
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

.msg.user { align-self: flex-end; }
.msg.assistant { align-self: flex-start; }

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

.input-field::placeholder { color: rgba(26, 35, 50, 0.35); }

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

/* ── Transitions ── */
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

@media (max-width: 640px) {
  .chat-sidebar {
    width: 120px;
  }
  .chat-panel {
    width: calc(100vw - 32px);
    height: 480px;
  }
}
</style>
