<template>
  <div class="page">
    <NavBar />
    <div class="container profile-page">
      <h2 class="section-title anim-fade-up">个人中心</h2>

      <div class="profile-grid">
        <!-- Profile Card -->
        <div class="profile-card anim-fade-up anim-delay-1">
          <div class="profile-card__header">
            <div class="avatar" style="cursor:pointer; overflow:hidden;" @click="avatarInput.click()">
              <img v-if="form.avatarUrl" :src="form.avatarUrl" class="avatar-img" />
              <span v-else class="avatar__text">{{ initials }}</span>
            </div>
            <input type="file" ref="avatarInput" accept="image/*" @change="handleAvatarUpload" hidden />
            <div class="avatar-info">
              <h3 class="avatar-info__name">{{ form.name || '用户' }}</h3>
              <p class="avatar-info__meta">{{ form.college }} · {{ form.major }}</p>
            </div>
          </div>
          <div class="profile-card__divider" />
          <el-form :model="form" label-position="top" class="profile-form">
            <el-form-item label="学号">
              <el-input v-model="form.username" disabled />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="学院">
              <el-select v-model="form.collegeId" placeholder="请选择学院" :disabled="!store.isAdmin" clearable style="width:100%" @change="onCollegeChange">
                <el-option v-for="c in colleges" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="专业">
              <el-select v-model="form.majorId" :placeholder="form.collegeId ? '请选择专业' : '请先选择学院'" :disabled="!store.isAdmin || !form.collegeId" clearable style="width:100%">
                <el-option v-for="m in majors" :key="m.id" :label="m.name" :value="m.id" />
              </el-select>
              <p v-if="!store.isAdmin" class="field-hint">学院/专业信息仅管理员可修改</p>
            </el-form-item>
            <el-form-item>
              <button class="btn-save" @click.prevent="handleUpdate">保存修改</button>
            </el-form-item>
          </el-form>
        </div>

        <!-- Password Card -->
        <div class="profile-card anim-fade-up anim-delay-3">
          <div class="profile-card__header">
            <div class="avatar avatar--accent">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                <path d="M7 11V7a5 5 0 0110 0v4" />
              </svg>
            </div>
            <div class="avatar-info">
              <h3 class="avatar-info__name">修改密码</h3>
              <p class="avatar-info__meta">定期更换密码保障账户安全</p>
            </div>
          </div>
          <div class="profile-card__divider" />
          <el-form :model="pwdForm" label-position="top" class="profile-form">
            <el-form-item label="原密码">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="输入原密码" />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="输入新密码" />
            </el-form-item>
            <el-form-item>
              <button class="btn-save" @click.prevent="handleChangePwd">修改密码</button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import NavBar from '../../components/NavBar.vue'
import { getUserById, updateProfile, changePassword, getColleges, getMajors } from '../../api/user'
import { useUserStore } from '../../stores/user'
import request from '../../api/request'

const store = useUserStore()
const form = reactive({ username: '', name: '', email: '', phone: '', college: '', major: '', collegeId: '', majorId: '', avatarUrl: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '' })
const avatarInput = ref(null)
const colleges = ref([])
const majors = ref([])

onMounted(async () => {
  try {
    const [userRes, collegeRes] = await Promise.all([
      getUserById(store.userId),
      getColleges()
    ])
    Object.assign(form, userRes.data)
    colleges.value = collegeRes.data || []
    if (form.collegeId) {
      const majorRes = await getMajors(form.collegeId)
      majors.value = majorRes.data || []
    }
  } catch {}
})

function onCollegeChange() {
  majors.value = []
  form.majorId = ''
  if (!form.collegeId) return
  getMajors(form.collegeId).then(res => {
    majors.value = res.data || []
  }).catch(() => {})
}

const initials = computed(() => {
  if (!form.name) return '?'
  const parts = form.name.trim().split(/\s+/)
  if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase()
  return form.name.slice(0, 2).toUpperCase()
})

async function handleAvatarUpload(e) {
  const file = e.target.files?.[0]
  if (!file) return
  const f = new FormData()
  f.append('file', file)
  try {
    const res = await request.post('/upload', f, { headers: { 'Content-Type': 'multipart/form-data' } })
    form.avatarUrl = res.data
    ElMessage.success('头像上传成功')
  } catch { /* handled by interceptor */ }
}

async function handleUpdate() {
  try {
    await updateProfile(store.userId, form)
    if (store.user) {
      Object.assign(store.user, form)
      localStorage.setItem('user', JSON.stringify(store.user))
    }
    ElMessage.success('保存成功')
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

async function handleChangePwd() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) {
    ElMessage.warning('请填写完整密码信息')
    return
  }
  try {
    await changePassword(store.userId, pwdForm)
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
  } catch (e) {
    ElMessage.error('密码修改失败')
  }
}
</script>

<style scoped>
.profile-page {
  max-width: 860px;
}

.profile-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  align-items: start;
}

@media (max-width: 768px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}

.profile-card {
  background: var(--c-surface);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--c-border-light);
  padding: 28px;
  transition: var(--transition);
}
.profile-card:hover {
  box-shadow: var(--shadow-md);
}

.profile-card__header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--c-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #fff;
}

.avatar--accent {
  background: var(--c-accent);
}

.avatar__text {
  font-family: 'DM Serif Display', Georgia, serif;
  font-size: 1.3rem;
  font-weight: 500;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-info__name {
  font-family: 'DM Serif Display', Georgia, serif;
  font-size: 1.15rem;
  color: var(--c-primary);
  margin: 0;
}

.avatar-info__meta {
  font-size: 0.82rem;
  color: var(--c-text-muted);
  margin: 2px 0 0;
}

.profile-card__divider {
  height: 1px;
  background: var(--c-border-light);
  margin: 20px 0;
}

.profile-form {
  margin-top: 0;
}

.profile-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.profile-form :deep(.el-form-item__label) {
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--c-text-muted);
  padding-bottom: 4px;
}

.field-hint {
  font-size: 0.75rem;
  color: var(--c-text-muted);
  margin: 6px 0 0;
}

.btn-save {
  width: 100%;
  background: var(--c-primary);
  color: #fff;
  border: none;
  font-family: 'DM Sans', sans-serif;
  font-size: 0.9rem;
  font-weight: 600;
  padding: 10px 24px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: var(--transition);
  margin-top: 4px;
}
.btn-save:hover {
  background: var(--c-primary-light);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}
.btn-save:active {
  transform: translateY(0);
}
</style>
