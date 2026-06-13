<template>
  <div class="page">
    <NavBar />
    <div class="container">
      <div class="notif-header anim-fade-up">
        <div class="notif-header__left">
          <h2 class="section-title" style="margin-bottom:0">消息通知</h2>
        </div>
        <div class="notif-header__right">
          <div class="unread-badge">
            <span class="unread-badge__dot" />
            <span class="unread-badge__text">{{ unreadCount }} 条未读</span>
          </div>
          <button class="btn-mark-all" :disabled="unreadCount === 0" @click="markAll">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
              <circle cx="12" cy="12" r="3" />
            </svg>
            全部标为已读
          </button>
        </div>
      </div>

      <div v-if="list.length === 0" class="empty-state anim-fade">
        <svg viewBox="0 0 120 120" fill="none">
          <path d="M60 20c-22 0-40 18-40 40 0 8 2.5 16 7 22l-5 15 16-4c6 3 13 5 22 5 22 0 40-18 40-40S82 20 60 20z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
        <p>暂无消息通知</p>
      </div>

      <div v-else class="notif-list">
        <div
          v-for="(n, index) in list"
          :key="n.id"
          class="notif-card anim-fade-up"
          :class="{
            'notif-card--unread': n.isRead === 0,
            [`anim-delay-${Math.min(index % 6 + 1, 6)}`]: true
          }"
          @click="handleClick(n)"
        >
          <div class="notif-card__indicator">
            <span v-if="n.isRead === 0" class="unread-dot" />
          </div>
          <div class="notif-card__icon-wrap">
            <div class="notif-card__icon" :class="`notif-card__icon--${typeClass(n.title)}`">
              <svg v-if="typeClass(n.title) === 'success'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M22 11.08V12a10 10 0 11-5.93-9.14" /><polyline points="22 4 12 14.01 9 11.01" />
              </svg>
              <svg v-else-if="typeClass(n.title) === 'warning'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" /><line x1="12" y1="9" x2="12" y2="13" /><line x1="12" y1="17" x2="12.01" y2="17" />
              </svg>
              <svg v-else-if="typeClass(n.title) === 'danger'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10" /><line x1="12" y1="8" x2="12" y2="12" /><line x1="12" y1="16" x2="12.01" y2="16" />
              </svg>
              <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9" /><path d="M13.73 21a2 2 0 01-3.46 0" />
              </svg>
            </div>
          </div>
          <div class="notif-card__content">
            <div class="notif-card__header">
              <h4 class="notif-card__title">{{ n.title }}</h4>
              <span class="notif-card__time">{{ formatTime(n.createTime) }}</span>
            </div>
            <p class="notif-card__body" v-html="n.content"></p>
          </div>
        </div>
      </div>

      <div v-if="total > 0" class="pagination-wrap anim-fade-up">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="size"
          :current-page="page"
          @current-change="pageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import NavBar from '../../components/NavBar.vue'
import { formatTime } from '../../utils/format'
import { sanitizeHtml } from '../../utils/sanitize'
import { pageNotificationByUser, getUnreadCount, markNotificationRead, markAllNotificationsRead } from '../../api/notification'
import { useUserStore } from '../../stores/user'

const store = useUserStore()
const router = useRouter()
const list = ref([])
const total = ref(0)
const unreadCount = ref(0)
const page = ref(1)
const size = 10

function typeClass(title) {
  if (!title) return 'info'
  const t = title.toLowerCase()
  if (t.includes('通过') || t.includes('成功') || t.includes('完成')) return 'success'
  if (t.includes('驳回') || t.includes('失败') || t.includes('拒绝')) return 'danger'
  if (t.includes('警告') || t.includes('提醒') || t.includes('待审')) return 'warning'
  return 'info'
}

async function fetchData() {
  try {
    const [notifRes, unreadRes] = await Promise.all([
      pageNotificationByUser(store.userId, { page: page.value, size }),
      getUnreadCount(store.userId)
    ])
    const records = notifRes.data.records || []
    records.forEach(n => { if (n.content) n.content = sanitizeHtml(n.content) })
    list.value = records
    total.value = notifRes.data.total || 0
    unreadCount.value = unreadRes.data || 0
  } catch (e) {
    ElMessage.error('加载通知失败')
  }
}

async function handleClick(n) {
  if (n.isRead === 0) {
    try {
      await markNotificationRead(n.id, store.userId)
      n.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
  } catch (e) { ElMessage.error('加载通知失败')
  }
}
  if (n.relatedType === 'contest') {
    router.push(`/contest/${n.relatedId}`)
  } else if (n.relatedType === 'team') {
    router.push(`/team/${n.relatedId}`)
  } else if (n.relatedType === 'registration') {
    router.push('/my-registration')
  }
}

async function markAll() {
  try {
    await markAllNotificationsRead(store.userId)
    ElMessage.success('已全部标为已读')
    fetchData()
  } catch (e) {
    ElMessage.error('标记已读失败')
  }
}

function pageChange(p) {
  page.value = p
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.notif-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
  flex-wrap: wrap;
  gap: 16px;
}

.notif-header__right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.unread-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.88rem;
  color: var(--c-text-muted);
}

.unread-badge__dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--c-accent);
  animation: pulse 2s ease infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.unread-badge__text {
  font-weight: 500;
}

.btn-mark-all {
  display: flex;
  align-items: center;
  gap: 6px;
  background: transparent;
  border: 1.5px solid var(--c-border);
  color: var(--c-text-muted);
  font-family: 'DM Sans', sans-serif;
  font-size: 0.82rem;
  font-weight: 500;
  padding: 7px 16px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: var(--transition);
}
.btn-mark-all:hover:not(:disabled) {
  border-color: var(--c-primary);
  color: var(--c-primary);
  background: #f8f6f3;
}
.btn-mark-all:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.notif-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.notif-card {
  display: flex;
  align-items: flex-start;
  gap: 0;
  background: var(--c-surface);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--c-border-light);
  padding: 16px 20px;
  cursor: pointer;
  transition: var(--transition);
  position: relative;
  overflow: hidden;
}
.notif-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateX(4px);
}

.notif-card--unread {
  border-left: 3px solid var(--c-accent);
  background: #fdfcfa;
}

.notif-card__indicator {
  width: 16px;
  flex-shrink: 0;
  display: flex;
  align-items: flex-start;
  padding-top: 4px;
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--c-accent);
  display: block;
}

.notif-card__icon-wrap {
  flex-shrink: 0;
  margin-right: 14px;
}

.notif-card__icon {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.notif-card__icon--info { background: #edf2f7; color: var(--c-info); }
.notif-card__icon--success { background: #e3f5ed; color: var(--c-success); }
.notif-card__icon--warning { background: #fef3e2; color: var(--c-warning); }
.notif-card__icon--danger { background: #fde8e5; color: var(--c-danger); }

.notif-card__content {
  flex: 1;
  min-width: 0;
}

.notif-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 4px;
}

.notif-card__title {
  font-family: 'DM Serif Display', Georgia, serif;
  font-size: 0.95rem;
  color: var(--c-primary);
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.notif-card__time {
  font-size: 0.78rem;
  color: var(--c-text-light);
  white-space: nowrap;
  flex-shrink: 0;
}

.notif-card__body {
  font-size: 0.85rem;
  color: var(--c-text-muted);
  margin: 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
</style>
