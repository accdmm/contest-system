<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>高校学科竞赛报名管理系统</h2>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="登录" name="login">
          <el-form ref="loginForm" :model="loginData" :rules="rules" label-width="80px">
            <el-form-item label="账号" prop="username">
              <el-input v-model="loginData.username" placeholder="学号或管理员账号" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="loginData.password" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleLogin" :loading="loading" style="width:100%">登录</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册" name="register">
          <el-form ref="registerForm" :model="registerData" :rules="registerRules" label-width="80px">
            <el-form-item label="学号" prop="username">
              <el-input v-model="registerData.username" />
            </el-form-item>
            <el-form-item label="姓名" prop="name">
              <el-input v-model="registerData.name" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="registerData.password" type="password" show-password />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="registerData.email" />
            </el-form-item>
            <el-form-item label="手机" prop="phone">
              <el-input v-model="registerData.phone" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleRegister" :loading="loading" style="width:100%">注册</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '../../api/user'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const store = useUserStore()
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
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card { width: 420px; }
.login-card h2 { text-align: center; margin-bottom: 20px; color: #333; }
</style>
