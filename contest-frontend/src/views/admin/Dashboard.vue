<template>
  <div class="dashboard-page">
    <div class="dashboard-bg">
      <div class="container">
        <div class="dashboard-header">
          <h1 class="page-title">管理后台</h1>
          <p class="page-subtitle">竞赛平台管理中心</p>
        </div>

        <div class="stats-grid">
          <div class="stat-card" @click="router.push('/admin/contest')">
            <div class="stat-icon stat-icon--blue">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M6 9H4.5a2.5 2.5 0 0 1 0-5C7 4 8 6.5 8 6.5"/><path d="M18 9h1.5a2.5 2.5 0 0 0 0-5C17 4 16 6.5 16 6.5"/><path d="M4 22h16"/><path d="M10 22V8"/><path d="M14 22V8"/><path d="M8 22V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v18"/></svg>
            </div>
            <div class="stat-content">
              <h3 class="stat-title">竞赛管理</h3>
              <p class="stat-desc">发布、编辑、上下架竞赛</p>
            </div>
            <div class="stat-arrow">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m9 18 6-6-6-6"/></svg>
            </div>
          </div>

          <div class="stat-card" @click="router.push('/admin/review')">
            <div class="stat-icon stat-icon--green">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z"/><polyline points="14 2 14 8 20 8"/><path d="m9 15 2 2 4-4"/></svg>
            </div>
            <div class="stat-content">
              <h3 class="stat-title">报名审核</h3>
              <p class="stat-desc">审核学生报名申请</p>
            </div>
            <div class="stat-arrow">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m9 18 6-6-6-6"/></svg>
            </div>
          </div>

          <div class="stat-card" @click="router.push('/admin/cms')">
            <div class="stat-icon stat-icon--orange">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>
            </div>
            <div class="stat-content">
              <h3 class="stat-title">内容管理</h3>
              <p class="stat-desc">轮播图、公告管理</p>
            </div>
            <div class="stat-arrow">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m9 18 6-6-6-6"/></svg>
            </div>
          </div>

          <div class="stat-card" @click="router.push('/admin/permissions')">
            <div class="stat-icon stat-icon--blue">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
            </div>
            <div class="stat-content">
              <h3 class="stat-title">权限管理</h3>
              <p class="stat-desc">角色权限分配</p>
            </div>
            <div class="stat-arrow">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m9 18 6-6-6-6"/></svg>
            </div>
          </div>
        </div>
      </div>

      <div class="dashboard-decoration">
        <div class="deco-dot deco-dot--1" />
        <div class="deco-dot deco-dot--2" />
        <div class="deco-line" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.dashboard-page {
}

.dashboard-bg {
  position: relative;
  overflow: hidden;
  padding: 40px 0 60px;
}

.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 24px;
  position: relative;
  z-index: 1;
}

.dashboard-header {
  margin-bottom: 48px;
}

.page-title {
  font-family: 'DM Serif Display', serif;
  font-size: 36px;
  color: #1a2332;
  margin: 0 0 8px;
  letter-spacing: -0.02em;
}

.page-subtitle {
  font-family: 'DM Sans', sans-serif;
  font-size: 15px;
  color: #1a2332;
  opacity: 0.5;
  margin: 0;
  font-weight: 400;
  letter-spacing: 0.02em;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px 24px;
  cursor: pointer;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(26, 35, 50, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  isolation: isolate;
}

.stat-card::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(26, 35, 50, 0.02), transparent 60%);
  pointer-events: none;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(26, 35, 50, 0.1);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  position: relative;
}

.stat-icon--blue {
  background: linear-gradient(135deg, #1a2332, #2a3a52);
}

.stat-icon--green {
  background: linear-gradient(135deg, #1a7a5a, #2a9a7a);
}

.stat-icon--orange {
  background: linear-gradient(135deg, #e85d4a, #f07a6a);
}

.stat-content {
  flex: 1;
}

.stat-title {
  font-family: 'DM Serif Display', serif;
  font-size: 18px;
  color: #1a2332;
  margin: 0 0 6px;
  letter-spacing: -0.01em;
}

.stat-desc {
  font-family: 'DM Sans', sans-serif;
  font-size: 13px;
  color: #1a2332;
  opacity: 0.5;
  margin: 0;
  line-height: 1.5;
}

.stat-arrow {
  color: #1a2332;
  opacity: 0.2;
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.stat-card:hover .stat-arrow {
  opacity: 0.6;
  transform: translateX(4px);
}

.dashboard-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.deco-dot {
  position: absolute;
  border-radius: 50%;
}

.deco-dot--1 {
  width: 240px;
  height: 240px;
  background: radial-gradient(circle, rgba(232, 93, 74, 0.06), transparent 70%);
  top: -80px;
  right: -60px;
}

.deco-dot--2 {
  width: 180px;
  height: 180px;
  background: radial-gradient(circle, rgba(26, 35, 50, 0.04), transparent 70%);
  bottom: -40px;
  left: -60px;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .page-title {
    font-size: 28px;
  }

  .dashboard-header {
    margin-bottom: 32px;
  }

  .stat-card {
    padding: 24px 20px;
    flex-direction: row;
    text-align: left;
  }
}
</style>
