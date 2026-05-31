<template>
  <div class="admin-layout">
    <div class="admin-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <router-link to="/admin" class="sidebar-brand">
          <div class="brand-icon">C</div>
          <span class="brand-text" v-show="!sidebarCollapsed">管理后台</span>
        </router-link>
        <button class="sidebar-toggle" @click="sidebarCollapsed = !sidebarCollapsed">
          <svg :class="{ rotated: sidebarCollapsed }" width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M10 4L6 8L10 12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>

      <div class="sidebar-menu">
        <router-link v-for="item in menuItems" :key="item.path" :to="item.path" class="menu-item" :class="{ active: isActive(item.path) }">
          <div class="menu-icon" v-html="item.icon"></div>
          <span class="menu-label" v-show="!sidebarCollapsed">{{ item.label }}</span>
        </router-link>
      </div>

      <div class="sidebar-footer">
        <router-link to="/home" class="menu-item">
          <div class="menu-icon">
            <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
              <path d="M1 9L9 2L17 9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M3 7V15H7V11H11V15H15V7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <span class="menu-label" v-show="!sidebarCollapsed">前台首页</span>
        </router-link>
      </div>
    </div>

    <div class="admin-main" :class="{ expanded: sidebarCollapsed }">
      <header class="admin-topbar">
        <div class="topbar-left">
          <h2 class="topbar-title">{{ currentTitle }}</h2>
        </div>
        <div class="topbar-right">
          <span class="topbar-user">{{ store.user?.name }}</span>
          <el-button text class="logout-btn" @click="handleLogout">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none" style="margin-right:4px">
              <path d="M6 2v2h6V2a1 1 0 00-1-1H7a1 1 0 00-1 1zm7 0v2h1.5A1.5 1.5 0 0116 5.5v8a1.5 1.5 0 01-1.5 1.5h-13A1.5 1.5 0 010 13.5v-8A1.5 1.5 0 011.5 4H3V2a2 2 0 012-2h6a2 2 0 012 2z" fill="currentColor"/>
            </svg>
            退出
          </el-button>
        </div>
      </header>
      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const sidebarCollapsed = ref(false)

const menuItems = [
  { path: '/admin', label: '仪表盘', icon: '<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><rect x="1" y="4" width="6" height="12" rx="1" stroke="currentColor" stroke-width="1.3"/><rect x="11" y="1" width="6" height="15" rx="1" stroke="currentColor" stroke-width="1.3"/></svg>' },
  { path: '/admin/contest', label: '竞赛管理', icon: '<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><path d="M6 1L9 4L12 1" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round"/><path d="M9 4V17" stroke="currentColor" stroke-width="1.3"/><path d="M2 8C2 8 4 11 9 11C14 11 16 8 16 8" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/></svg>' },
  { path: '/admin/review', label: '报名审核', icon: '<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><path d="M4 9L7 12L14 5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/><rect x="1" y="1" width="16" height="16" rx="3" stroke="currentColor" stroke-width="1.3"/></svg>' },
  { path: '/admin/cms', label: '内容管理', icon: '<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><rect x="1" y="1" width="16" height="16" rx="2" stroke="currentColor" stroke-width="1.3"/><path d="M5 5H13M5 9H13M5 13H10" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/></svg>' },
  { path: '/admin/notification', label: '通知推送', icon: '<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><path d="M9 2C6.2 2 4 4.2 4 7v3l-1 2v1h12v-1l-1-2V7c0-2.8-2.2-5-5-5zM7 14h4c0 1.1-.9 2-2 2s-2-.9-2-2z" stroke="currentColor" stroke-width="1.3"/></svg>' },
  { path: '/admin/users', label: '用户管理', icon: '<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><circle cx="7" cy="5" r="3" stroke="currentColor" stroke-width="1.3"/><path d="M2 16c0-3 2.2-5 5-5s5 2 5 5" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/><circle cx="13" cy="6" r="2" stroke="currentColor" stroke-width="1.3"/><path d="M12 16c0-2 1.3-3.5 3-3.5s3 1.5 3 3.5" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/></svg>' },
  { path: '/admin/permissions', label: '权限管理', icon: '<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><rect x="3" y="7" width="12" height="10" rx="1.5" stroke="currentColor" stroke-width="1.3"/><path d="M6 7V4.5C6 3 6.5 1 9 1C11.5 1 12 3 12 4.5V7" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/><circle cx="9" cy="11.5" r="1.3" stroke="currentColor" stroke-width="1.2"/></svg>' },
]

const routeTitles = {
  '/admin': '仪表盘',
  '/admin/contest': '竞赛管理',
  '/admin/review': '报名审核',
  '/admin/cms': '内容管理',
  '/admin/notification': '通知推送',
  '/admin/users': '用户管理',
  '/admin/permissions': '权限管理',
}

const currentTitle = computed(() => routeTitles[route.path] || '管理后台')

function isActive(path) {
  if (path === '/admin') return route.path === '/admin'
  return route.path.startsWith(path)
}

function handleLogout() {
  store.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  background: #f0f2f5;
}

.admin-sidebar {
  width: 220px;
  min-width: 220px;
  background: #1a2332;
  color: #fff;
  display: flex;
  flex-direction: column;
  transition: width 0.25s ease, min-width 0.25s ease;
  overflow: hidden;
}

.admin-sidebar.collapsed {
  width: 60px;
  min-width: 60px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  height: 64px;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: #fff;
  overflow: hidden;
}

.brand-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #e85d4a;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'DM Serif Display', Georgia, serif;
  font-size: 1rem;
  font-weight: 700;
  flex-shrink: 0;
}

.brand-text {
  font-family: 'DM Serif Display', Georgia, serif;
  font-size: 1rem;
  white-space: nowrap;
}

.sidebar-toggle {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  border: none;
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: var(--transition);
}

.sidebar-toggle:hover {
  background: rgba(255, 255, 255, 0.14);
  color: #fff;
}

.sidebar-toggle svg {
  transition: transform 0.25s ease;
}

.sidebar-toggle svg.rotated {
  transform: rotate(180deg);
}

.sidebar-menu {
  flex: 1;
  padding: 12px 8px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow-y: auto;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  text-decoration: none;
  color: rgba(255, 255, 255, 0.65);
  font-size: 0.88rem;
  font-weight: 500;
  transition: all 0.15s ease;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
}

.menu-item:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.menu-item.active {
  background: #e85d4a;
  color: #fff;
}

.menu-icon {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.menu-label {
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar-footer {
  padding: 8px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.sidebar-footer .menu-item {
  font-size: 0.82rem;
}

.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  transition: margin-left 0.25s ease;
}

.admin-topbar {
  height: 64px;
  min-height: 64px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.topbar-title {
  font-family: 'DM Serif Display', Georgia, serif;
  font-size: 1.2rem;
  color: #1a2332;
  margin: 0;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.topbar-user {
  font-size: 0.88rem;
  color: #666;
  font-weight: 500;
}

.logout-btn {
  color: #999 !important;
  font-size: 0.85rem !important;
}

.logout-btn:hover {
  color: #e85d4a !important;
}

.admin-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}
</style>
