<template>
  <header class="navbar">
    <div class="navbar__inner">
      <router-link to="/home" class="navbar__brand">
        <span class="navbar__logo-icon">C</span>
        <span class="navbar__logo-text">竞赛报名系统</span>
      </router-link>

      <nav class="navbar__links">
        <router-link
          v-for="link in navLinks"
          :key="link.path"
          :to="link.path"
          class="navbar__link"
          :class="{ 'navbar__link--active': route.path === link.path }"
        >
          {{ link.label }}
          <span v-if="link.path === '/notification' && unreadCount > 0" class="navbar__badge" />
          <span class="navbar__link-indicator" />
        </router-link>
      </nav>

      <button class="navbar__hamburger" @click="drawerOpen = true" aria-label="菜单">
        <span /><span /><span />
      </button>

      <el-drawer
        v-model="drawerOpen"
        direction="ltr"
        size="280px"
        :with-header="false"
        class="mobile-drawer"
      >
        <div class="drawer-header">
          <span class="drawer-brand">竞赛报名系统</span>
        </div>
        <div class="drawer-body">
          <router-link
            v-for="link in navLinks"
            :key="link.path"
            :to="link.path"
            class="drawer-link"
            :class="{ 'drawer-link--active': route.path === link.path }"
            @click="drawerOpen = false"
          >
            {{ link.label }}
            <span v-if="link.path === '/notification' && unreadCount > 0" class="drawer-badge" />
          </router-link>

          <div class="drawer-divider" />

          <router-link to="/my-teams" class="drawer-link" @click="drawerOpen = false">我的团队</router-link>
          <router-link to="/team/create" class="drawer-link" @click="drawerOpen = false">创建团队</router-link>
          <div class="drawer-link" @click="handleJoinTeam(); drawerOpen = false">加入团队</div>
          <router-link to="/profile" class="drawer-link" @click="drawerOpen = false">个人信息</router-link>

          <template v-if="store.isTeacher">
            <div class="drawer-divider" />
            <div class="drawer-section-label">教师</div>
            <router-link to="/teacher/teams" class="drawer-link" @click="drawerOpen = false">我指导的团队</router-link>
          </template>
          <template v-if="store.isAdmin">
            <div class="drawer-divider" />
            <div class="drawer-section-label">管理后台</div>
            <router-link to="/admin" class="drawer-link" @click="drawerOpen = false">仪表盘</router-link>
            <router-link to="/admin/contest" class="drawer-link" @click="drawerOpen = false">竞赛管理</router-link>
            <router-link to="/admin/review" class="drawer-link" @click="drawerOpen = false">报名审核</router-link>
            <router-link to="/admin/cms" class="drawer-link" @click="drawerOpen = false">内容管理</router-link>
            <router-link to="/admin/notification" class="drawer-link" @click="drawerOpen = false">通知推送</router-link>
            <router-link to="/admin/users" class="drawer-link" @click="drawerOpen = false">用户管理</router-link>
          </template>

          <div class="drawer-divider" />
          <div class="drawer-link drawer-link--danger" @click="handleLogout(); drawerOpen = false">退出登录</div>
        </div>
      </el-drawer>

      <div class="navbar__actions">
        <template v-if="store.isLoggedIn">
          <div class="navbar__user" @click="dropdownOpen = !dropdownOpen" ref="userMenuRef">
            <div class="navbar__avatar">
              <img v-if="store.user?.avatarUrl" :src="store.user.avatarUrl" class="navbar__avatar-img" />
              <span v-else>{{ store.user?.name?.charAt(0) }}</span>
            </div>
            <span class="navbar__username">{{ store.user?.name }}</span>
            <svg class="navbar__chevron" :class="{ 'navbar__chevron--open': dropdownOpen }" width="12" height="12" viewBox="0 0 12 12" fill="none">
              <path d="M3 4.5L6 7.5L9 4.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <transition name="dropdown">
              <div v-if="dropdownOpen" class="navbar__dropdown">
                <router-link to="/profile" class="navbar__dropdown-item" @click="dropdownOpen = false">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M8 8C9.933 8 11.5 6.433 11.5 4.5S9.933 1 8 1 4.5 2.567 4.5 4.5 6.067 8 8 8zm0 2c-3.315 0-6 1.343-6 3v1h12v-1c0-1.657-2.685-3-6-3z" fill="currentColor"/></svg>
                  个人信息
                </router-link>
                <router-link to="/my-teams" class="navbar__dropdown-item" @click="dropdownOpen = false">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M6 6a2.5 2.5 0 100-5 2.5 2.5 0 000 5zm4.5 1a2 2 0 100-4 2 2 0 000 4zM6 8c-2.21 0-4 1.12-4 2.5V12h8v-1.5C10 9.12 8.21 8 6 8zm4.5 0c-.28 0-.55.02-.82.06.98.69 1.82 1.57 1.82 2.44V12h3v-1.5c0-1.38-1.79-2.5-4-2.5z" fill="currentColor"/></svg>
                  我的团队
                </router-link>
                <router-link to="/team/create" class="navbar__dropdown-item" @click="dropdownOpen = false">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M6 6a2.5 2.5 0 100-5 2.5 2.5 0 000 5zm4.5 1a2 2 0 100-4 2 2 0 000 4zM6 8c-2.21 0-4 1.12-4 2.5V12h8v-1.5C10 9.12 8.21 8 6 8zm4.5 0c-.28 0-.55.02-.82.06.98.69 1.82 1.57 1.82 2.44V12h3v-1.5c0-1.38-1.79-2.5-4-2.5z" fill="currentColor"/></svg>
                  创建团队
                </router-link>
                <div class="navbar__dropdown-item" @click="handleJoinTeam">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M8 2v12M2 8h12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
                  加入团队
                </div>
                <router-link v-if="store.isAdmin" to="/admin" class="navbar__dropdown-item" @click="dropdownOpen = false">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M8 1L1 4v4c0 4.5 3 8.5 7 9 4-.5 7-4.5 7-9V4L8 1zm0 2.5L12.5 6v2c0 3-2.2 5.7-4.5 6.3-2.3-.6-4.5-3.3-4.5-6.3V6L8 3.5z" fill="currentColor"/></svg>
                  管理后台
                </router-link>
                <router-link v-if="store.isAdmin" to="/admin/notification" class="navbar__dropdown-item" @click="dropdownOpen = false">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M8 1C4.7 1 2 3.7 2 7v3l-1 2v1h14v-1l-1-2V7c0-3.3-2.7-6-6-6zM6 13h4c0 1.1-.9 2-2 2s-2-.9-2-2z" fill="currentColor"/></svg>
                  通知推送
                </router-link>
                <router-link v-if="store.isAdmin" to="/admin/users" class="navbar__dropdown-item" @click="dropdownOpen = false">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M8 1C4.7 1 2 3.7 2 7c0 2.2 1.2 4 3 5.5V15l3-2 3 2v-2.5c1.8-1.5 3-3.3 3-5.5 0-3.3-2.7-6-6-6z" fill="currentColor"/></svg>
                  用户管理
                </router-link>
                <router-link v-if="store.isTeacher" to="/teacher/teams" class="navbar__dropdown-item" @click="dropdownOpen = false">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M6 6a2.5 2.5 0 100-5 2.5 2.5 0 000 5zm4.5 1a2 2 0 100-4 2 2 0 000 4zM6 8c-2.21 0-4 1.12-4 2.5V12h8v-1.5C10 9.12 8.21 8 6 8zm4.5 0c-.28 0-.55.02-.82.06.98.69 1.82 1.57 1.82 2.44V12h3v-1.5c0-1.38-1.79-2.5-4-2.5z" fill="currentColor"/></svg>
                  我指导的团队
                </router-link>
                <div class="navbar__dropdown-divider" />
                <div class="navbar__dropdown-item navbar__dropdown-item--danger" @click="handleLogout">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M6 2v2h6V2a1 1 0 00-1-1H7a1 1 0 00-1 1zm7 0v2h1.5A1.5 1.5 0 0116 5.5v8a1.5 1.5 0 01-1.5 1.5h-13A1.5 1.5 0 010 13.5v-8A1.5 1.5 0 011.5 4H3V2a2 2 0 012-2h6a2 2 0 012 2zM5 8.5a.5.5 0 00.5.5h5a.5.5 0 000-1h-5a.5.5 0 00-.5.5z" fill="currentColor"/></svg>
                  退出登录
                </div>
              </div>
            </transition>
          </div>
        </template>
        <router-link v-else to="/login" class="navbar__login-btn">
          登录
        </router-link>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import { joinByInviteCode } from '../api/team'
import { getUnreadCount } from '../api/notification'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const dropdownOpen = ref(false)
const drawerOpen = ref(false)
const userMenuRef = ref(null)
const unreadCount = ref(0)
let pollTimer = null

async function fetchUnreadCount() {
  if (!store.isLoggedIn) { unreadCount.value = 0; return }
  try {
    const res = await getUnreadCount(store.userId)
    unreadCount.value = res.data || 0
  } catch { unreadCount.value = 0 }
}

watch(() => route.path, () => fetchUnreadCount())

function onClickOutside(e) {
  if (userMenuRef.value && !userMenuRef.value.contains(e.target)) {
    dropdownOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', onClickOutside)
  fetchUnreadCount()
  pollTimer = setInterval(fetchUnreadCount, 30000)
})
onUnmounted(() => {
  document.removeEventListener('click', onClickOutside)
  clearInterval(pollTimer)
})

const navLinks = [
  { path: '/home', label: '首页' },
  { path: '/contest', label: '竞赛列表' },
  { path: '/my-registration', label: '我的报名' },
  { path: '/notification', label: '通知' },
]

async function handleJoinTeam() {
  dropdownOpen.value = false
  try {
    const { value } = await ElMessageBox.prompt('请输入队长分享的邀请码', '加入团队', {
      confirmButtonText: '申请加入',
      cancelButtonText: '取消',
      inputPlaceholder: '输入邀请码',
      roundButton: true,
    })
    await joinByInviteCode({ userId: store.userId, inviteCode: value })
    ElMessage.success('已申请加入，等待队长审核')
  } catch {
  }
}

function handleLogout() {
  dropdownOpen.value = false
  store.logout()
  router.push('/login')
}
</script>

<style scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 72px;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(16px) saturate(1.4);
  -webkit-backdrop-filter: blur(16px) saturate(1.4);
  border-bottom: 1px solid var(--c-border-light);
  transition: var(--transition);
}

.navbar__inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.navbar__brand {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: var(--c-primary);
}

.navbar__logo-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: var(--c-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'DM Serif Display', serif;
  font-size: 1.1rem;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.navbar__logo-text {
  font-family: 'DM Serif Display', Georgia, serif;
  font-size: 1.25rem;
  font-weight: 400;
  letter-spacing: 0.02em;
  color: var(--c-primary);
}

.navbar__links {
  display: flex;
  align-items: center;
  gap: 4px;
}

.navbar__link {
  position: relative;
  text-decoration: none;
  color: var(--c-text-muted);
  font-size: 0.9rem;
  font-weight: 500;
  padding: 8px 18px;
  border-radius: var(--radius-sm);
  transition: var(--transition);
}

.navbar__link:hover {
  color: var(--c-primary);
  background: rgba(26, 35, 50, 0.04);
}

.navbar__link--active {
  color: var(--c-primary);
}

.navbar__badge {
  position: absolute;
  top: 2px;
  right: 8px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--c-danger);
}

.navbar__link-indicator {
  position: absolute;
  bottom: -1px;
  left: 50%;
  transform: translateX(-50%) scaleX(0);
  width: 20px;
  height: 2.5px;
  border-radius: 2px;
  background: var(--c-accent);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.navbar__link--active .navbar__link-indicator {
  transform: translateX(-50%) scaleX(1);
}

.navbar__actions {
  display: flex;
  align-items: center;
}

.navbar__user {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px 6px 6px;
  border-radius: 100px;
  cursor: pointer;
  transition: var(--transition);
  user-select: none;
}

.navbar__user:hover {
  background: rgba(26, 35, 50, 0.04);
}

.navbar__avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--c-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  font-weight: 600;
  letter-spacing: 0.5px;
  overflow: hidden;
}

.navbar__avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.navbar__username {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--c-text);
}

.navbar__chevron {
  color: var(--c-text-muted);
  transition: transform 0.25s ease;
}

.navbar__chevron--open {
  transform: rotate(180deg);
}

.navbar__login-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 9px 24px;
  background: var(--c-primary);
  color: #fff;
  font-size: 0.875rem;
  font-weight: 600;
  border-radius: 100px;
  text-decoration: none;
  transition: var(--transition);
  letter-spacing: 0.01em;
}

.navbar__login-btn:hover {
  background: var(--c-primary-light);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.navbar__dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 180px;
  background: var(--c-surface);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--c-border-light);
  padding: 6px;
  z-index: 100;
}

.navbar__dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 14px;
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--c-text);
  text-decoration: none;
  cursor: pointer;
  transition: var(--transition);
}

.navbar__dropdown-item svg {
  color: var(--c-text-muted);
  flex-shrink: 0;
}

.navbar__dropdown-item:hover {
  background: rgba(26, 35, 50, 0.04);
}

.navbar__dropdown-item--danger {
  color: var(--c-danger);
}

.navbar__dropdown-item--danger svg {
  color: var(--c-danger);
}

.navbar__dropdown-item--danger:hover {
  background: rgba(232, 93, 74, 0.08);
}

.navbar__dropdown-divider {
  height: 1px;
  background: var(--c-border-light);
  margin: 4px 8px;
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-4px) scale(0.97);
}

/* ===== Hamburger ===== */
.navbar__hamburger {
  display: none;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 5px;
  width: 40px;
  height: 40px;
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: var(--transition);
}
.navbar__hamburger:hover {
  background: rgba(26, 35, 50, 0.04);
}
.navbar__hamburger span {
  display: block;
  width: 20px;
  height: 2px;
  background: var(--c-primary);
  border-radius: 2px;
  transition: var(--transition);
}

/* ===== Mobile Drawer ===== */
.mobile-drawer :deep(.el-drawer__body) {
  padding: 0;
}
.drawer-header {
  padding: 24px 20px 16px;
  border-bottom: 1px solid var(--c-border-light);
}
.drawer-brand {
  font-family: 'DM Serif Display', Georgia, serif;
  font-size: 1.15rem;
  color: var(--c-primary);
}
.drawer-body {
  padding: 12px;
}
.drawer-link {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 14px;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--c-text);
  text-decoration: none;
  cursor: pointer;
  transition: var(--transition);
}
.drawer-link:hover {
  background: rgba(26, 35, 50, 0.04);
}
.drawer-link--active {
  color: var(--c-primary);
  background: rgba(26, 35, 50, 0.06);
  font-weight: 600;
}
.drawer-link--danger {
  color: var(--c-danger);
}
.drawer-link--danger:hover {
  background: rgba(232, 93, 74, 0.08);
}
.drawer-badge {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--c-danger);
  margin-left: auto;
}
.drawer-divider {
  height: 1px;
  background: var(--c-border-light);
  margin: 8px 12px;
}
.drawer-section-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--c-text-light);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  padding: 8px 14px 4px;
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .navbar__links {
    display: none;
  }
  .navbar__hamburger {
    display: flex;
  }
  .navbar__username {
    display: none;
  }
}
@media (min-width: 769px) {
  .navbar__hamburger {
    display: none;
  }
}
</style>
