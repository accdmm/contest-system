<template>
  <div class="page page--404">
    <NavBar />
    <div class="notfound">
      <div class="notfound-code">404</div>
      <h1 class="notfound-title">页面未找到</h1>
      <p class="notfound-desc">你访问的页面不存在，请检查链接是否正确</p>
      <router-link to="/home" class="notfound-btn">返回首页</router-link>
    </div>
  </div>
</template>

<script setup>
import NavBar from '../components/NavBar.vue'
</script>

<style scoped>
.page--404 {
  display: flex;
  flex-direction: column;
}
.notfound {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 40px 24px;
}
.notfound-code {
  font-family: 'DM Serif Display', serif;
  font-size: 96px;
  color: var(--c-accent);
  line-height: 1;
  margin-bottom: 8px;
}
.notfound-title {
  font-size: 24px;
  color: var(--c-primary);
  margin-bottom: 8px;
}
.notfound-desc {
  font-size: 14px;
  color: var(--c-text-muted);
  margin-bottom: 28px;
}
.notfound-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 28px;
  background: var(--c-primary);
  color: #fff;
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: var(--transition);
}
.notfound-btn:hover {
  background: var(--c-primary-light);
  transform: translateY(-2px);
}
</style>
