<template>
  <div class="login-page">
    <!-- Decorative panel -->
    <div class="login-panel">
      <div class="panel-bg">
        <div class="panel-shape panel-shape--1"></div>
        <div class="panel-shape panel-shape--2"></div>
        <div class="panel-shape panel-shape--3"></div>
        <div class="panel-shape panel-shape--4"></div>
      </div>
      <div class="panel-content anim-fade-up">
        <div class="panel-brand">
          <span class="panel-badge">高校学科竞赛</span>
          <h1 class="panel-title">报名管理系统</h1>
        </div>
        <p class="panel-desc">汇聚权威赛事，搭建竞技舞台。<br/>以赛促学，以赛促创，成就非凡。</p>
        <div class="panel-features">
          <div class="panel-feature">
            <span class="feature-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none"><path d="M10 2L12.9 7.8L19 8.6L14.5 13L15.8 19.2L10 16.2L4.2 19.2L5.5 13L1 8.6L7.1 7.8L10 2Z" fill="currentColor"/></svg>
            </span>
            <span>权威赛事信息</span>
          </div>
          <div class="panel-feature">
            <span class="feature-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none"><path d="M10 2L12.9 7.8L19 8.6L14.5 13L15.8 19.2L10 16.2L4.2 19.2L5.5 13L1 8.6L7.1 7.8L10 2Z" fill="currentColor"/></svg>
            </span>
            <span>一键报名管理</span>
          </div>
          <div class="panel-feature">
            <span class="feature-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none"><path d="M10 2L12.9 7.8L19 8.6L14.5 13L15.8 19.2L10 16.2L4.2 19.2L5.5 13L1 8.6L7.1 7.8L10 2Z" fill="currentColor"/></svg>
            </span>
            <span>团队协作支持</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Form card -->
    <div class="login-main anim-scale">
      <div class="login-card">
        <div class="login-header">
          <h2 class="login-heading" v-text="activeTab === 'login' ? '欢迎回来' : '创建账号'"></h2>
          <p class="login-subheading">
            {{ activeTab === 'login' ? '登录以管理你的竞赛之旅' : '注册以探索更多竞赛' }}
          </p>
        </div>

        <el-tabs v-model="activeTab" class="login-tabs" :stretch="true">
          <el-tab-pane label="登录" name="login">
            <el-form ref="loginForm" :model="loginData" :rules="rules" label-position="top" class="login-form">
              <el-form-item label="账号" prop="username">
                <el-input v-model="loginData.username" placeholder="学号或管理员账号" ref="usernameRef" />
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input v-model="loginData.password" type="password" show-password placeholder="请输入密码" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleLogin" :loading="loading" class="login-btn">登录</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="注册" name="register">
            <el-form ref="registerForm" :model="registerData" :rules="registerRules" label-position="top" class="login-form">
              <el-form-item label="学号" prop="username">
                <el-input v-model="registerData.username" placeholder="请输入学号" />
              </el-form-item>
              <el-form-item label="姓名" prop="name">
                <el-input v-model="registerData.name" placeholder="请输入姓名" />
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input v-model="registerData.password" type="password" show-password placeholder="8-20位密码" />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="registerData.email" placeholder="example@school.edu.cn" />
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="registerData.phone" placeholder="可选" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleRegister" :loading="loading" class="login-btn">注册</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '../../api/user'
import { useUserStore } from '../../stores/user'

const usernameRef = ref(null)
const router = useRouter()
const store = useUserStore()

onMounted(() => {
  usernameRef.value?.focus()
})
const activeTab = ref('login')
const loading = ref(false)
const loginForm = ref(null)
const registerForm = ref(null)

const loginData = reactive({ username: '', password: '' })
const registerData = reactive({ username: '', name: '', password: '', email: '', phone: '' })

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 20, message: '密码长度8-20位', trigger: 'blur' }
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await loginForm.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await login(loginData)
    store.setUser(res.data.user, res.data.token)
    ElMessage.success('登录成功')
    router.push('/home')
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  const valid = await registerForm.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await register(registerData)
    store.setUser(res.data.user, res.data.token)
    ElMessage.success('注册成功')
    router.push('/home')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  min-height: 100vh;
}

/* ===== Decorative Panel ===== */
.login-panel {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--c-primary) 0%, #0f1622 100%);
  overflow: hidden;
  padding: 60px;
}

.panel-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.panel-shape {
  position: absolute;
  border-radius: 50%;
}

.panel-shape--1 {
  width: 500px;
  height: 500px;
  background: var(--c-accent);
  opacity: 0.06;
  top: -150px;
  right: -100px;
}

.panel-shape--2 {
  width: 350px;
  height: 350px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  bottom: -80px;
  left: -80px;
}

.panel-shape--3 {
  width: 200px;
  height: 200px;
  background: rgba(232, 93, 74, 0.08);
  bottom: 30%;
  right: 15%;
}

.panel-shape--4 {
  width: 120px;
  height: 120px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  top: 40%;
  left: 10%;
}

.panel-content {
  position: relative;
  z-index: 1;
  max-width: 420px;
}

.panel-badge {
  display: inline-block;
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.15em;
  text-transform: uppercase;
  color: var(--c-accent);
  background: rgba(232, 93, 74, 0.12);
  padding: 6px 14px;
  border-radius: 20px;
  margin-bottom: 16px;
}

.panel-title {
  font-family: 'DM Serif Display', serif;
  font-size: 2.8rem;
  color: #fff;
  line-height: 1.2;
  margin-bottom: 16px;
}

.panel-desc {
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.55);
  line-height: 1.7;
  margin-bottom: 40px;
}

.panel-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.panel-feature {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.7);
}

.feature-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(232, 93, 74, 0.1);
  border-radius: 8px;
  color: var(--c-accent);
  flex-shrink: 0;
}

/* ===== Form ===== */
.login-main {
  width: 480px;
  min-width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: var(--c-bg);
}

.login-card {
  width: 100%;
  max-width: 400px;
}

.login-header {
  margin-bottom: 32px;
  text-align: center;
}

.login-heading {
  font-family: 'DM Serif Display', serif;
  font-size: 2rem;
  color: var(--c-primary);
  margin-bottom: 8px;
}

.login-subheading {
  font-size: 0.9rem;
  color: var(--c-text-muted);
}

.login-tabs {
  margin-bottom: 0;
}

.login-tabs :deep(.el-tabs__header) {
  margin-bottom: 28px;
}

.login-tabs :deep(.el-tabs__item) {
  font-size: 0.95rem;
  padding: 0 12px 16px;
  height: auto;
  line-height: 1.4;
  font-weight: 500;
}

.login-form {
  width: 100%;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 22px;
}

.login-form :deep(.el-form-item__label) {
  font-size: 0.85rem;
  font-weight: 600 !important;
  padding-bottom: 6px;
  line-height: 1.4;
}

.login-form :deep(.el-input__wrapper) {
  padding: 4px 14px;
}

.login-btn {
  width: 100%;
  height: 46px !important;
  font-size: 0.95rem !important;
  margin-top: 4px;
  border-radius: var(--radius-sm) !important;
}

.login-btn:not(:hover) {
  background: var(--c-primary) !important;
}

.login-btn:hover {
  background: var(--c-primary-light) !important;
}

/* ===== Responsive ===== */
@media (max-width: 900px) {
  .login-panel {
    display: none;
  }
  .login-main {
    width: 100%;
    min-width: unset;
    padding: 40px 24px;
  }
}
</style>
