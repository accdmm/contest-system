<template>
  <div class="page">
    <NavBar />
    <div class="create-wrap">
      <div class="container">
        <div class="create-layout">
          <div class="create-sidebar anim-fade-up anim-delay-1">
            <div class="sidebar-sticky">
              <div class="sidebar-brand">
                <div class="brand-icon">
                  <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                    <rect x="2" y="2" width="28" height="28" rx="8" stroke="currentColor" stroke-width="2"/>
                    <path d="M16 8V24M8 16H24" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
                  </svg>
                </div>
                <h2 class="brand-title">创建团队</h2>
                <p class="brand-desc">组建你的竞赛战队，<br/>与伙伴一起挑战巅峰</p>
              </div>
              <div class="sidebar-steps">
                <div class="step-item active">
                  <span class="step-num">1</span>
                  <span class="step-label">填写名称</span>
                </div>
                <div class="step-connector" />
                <div class="step-item active">
                  <span class="step-num">2</span>
                  <span class="step-label">提交创建</span>
                </div>
              </div>
            </div>
          </div>

          <div class="create-main anim-fade-up anim-delay-2">
            <div class="form-card">
              <div class="form-header">
                <h3 class="form-title">团队信息</h3>
                <p class="form-subtitle">填写以下信息创建你的新团队</p>
              </div>

              <el-form
                ref="formRef"
                :model="form"
                label-position="top"
                class="styled-form"
                @submit.prevent="handleCreate"
              >
                <el-form-item label="团队名称" required>
                  <el-input
                    v-model="form.teamName"
                    placeholder="给你的团队取一个响亮的名字"
                    class="styled-input"
                    size="large"
                    maxlength="50"
                    show-word-limit
                  >
                    <template #prefix>
                      <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                        <path d="M9 1C5.1 1 2 4.1 2 8C2 12.2 9 17 9 17C9 17 16 12.2 16 8C16 4.1 12.9 1 9 1Z" stroke="currentColor" stroke-width="1.3"/>
                        <circle cx="9" cy="8" r="2.5" stroke="currentColor" stroke-width="1.3"/>
                      </svg>
                    </template>
                  </el-input>
                </el-form-item>

                <el-form-item>
                  <div class="form-actions">
                    <el-button
                      type="primary"
                      size="large"
                      :loading="loading"
                      class="submit-btn"
                      @click="handleCreate"
                    >
                      <svg width="18" height="18" viewBox="0 0 18 18" fill="none" style="margin-right:6px">
                        <path d="M9 2V16M2 9H16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                      </svg>
                      {{ loading ? '创建中...' : '创建团队' }}
                    </el-button>
                    <el-button
                      size="large"
                      class="cancel-btn"
                      @click="router.push('/home')"
                    >
                      取消
                    </el-button>
                  </div>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import NavBar from '../../components/NavBar.vue'
import { createTeam } from '../../api/team'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const store = useUserStore()
const loading = ref(false)
const form = reactive({ teamName: '' })

async function handleCreate() {
  if (!form.teamName) {
    ElMessage.warning('请输入团队名称')
    return
  }
  loading.value = true
  try {
    const res = await createTeam({ userId: store.userId, teamName: form.teamName })
    ElMessage.success('创建成功')
    router.push(`/team/${res.data.id}`)
  } finally { loading.value = false }
}
</script>

<style scoped>
.create-wrap {
  padding: 40px 0;
}

.create-layout {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 40px;
  align-items: start;
}

.create-sidebar {
  position: sticky;
  top: 112px;
}

.sidebar-sticky {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.sidebar-brand {
  background: var(--c-surface);
  border-radius: var(--radius-md);
  padding: 32px 28px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--c-border-light);
}

.brand-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-sm);
  background: var(--c-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.brand-title {
  font-family: 'DM Serif Display', Georgia, serif;
  font-size: 1.5rem;
  color: var(--c-primary);
  margin-bottom: 8px;
}

.brand-desc {
  font-size: 0.85rem;
  color: var(--c-text-muted);
  line-height: 1.6;
}

.sidebar-steps {
  background: var(--c-surface);
  border-radius: var(--radius-md);
  padding: 24px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--c-border-light);
}

.step-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.step-num {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  font-weight: 600;
  background: var(--c-border-light);
  color: var(--c-text-muted);
  flex-shrink: 0;
}

.step-item.active .step-num {
  background: var(--c-accent);
  color: #fff;
}

.step-item.active .step-label {
  color: var(--c-primary);
  font-weight: 600;
}

.step-label {
  font-size: 0.9rem;
  color: var(--c-text-muted);
}

.step-connector {
  width: 1px;
  height: 16px;
  background: var(--c-border);
  margin-left: 13.5px;
}

.create-main {
  min-width: 0;
}

.form-card {
  background: var(--c-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  padding: 40px;
  border: 1px solid var(--c-border-light);
}

.form-header {
  margin-bottom: 32px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--c-border-light);
}

.form-title {
  font-family: 'DM Serif Display', Georgia, serif;
  font-size: 1.35rem;
  color: var(--c-primary);
  margin-bottom: 6px;
}

.form-subtitle {
  font-size: 0.88rem;
  color: var(--c-text-muted);
}

.styled-form {
  max-width: 500px;
}

.styled-form :deep(.el-form-item__label) {
  font-size: 0.88rem;
  font-weight: 600;
  color: var(--c-primary);
  padding-bottom: 6px;
}

.styled-select {
  width: 100%;
}

.styled-input {
  width: 100%;
}

.styled-input :deep(.el-input__prefix) {
  color: var(--c-text-light);
}

.contest-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.option-name {
  font-size: 0.9rem;
}

.form-actions {
  display: flex;
  gap: 12px;
  padding-top: 8px;
}

.submit-btn {
  min-width: 160px;
  height: 46px;
  font-size: 0.95rem;
  background: var(--c-accent) !important;
  border-radius: var(--radius-sm) !important;
  box-shadow: 0 4px 14px rgba(232, 93, 74, 0.3) !important;
}

.submit-btn:hover {
  background: var(--c-accent-light) !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(232, 93, 74, 0.4) !important;
}

.cancel-btn {
  height: 46px;
  font-size: 0.95rem;
}

@media (max-width: 768px) {
  .create-layout {
    grid-template-columns: 1fr;
  }

  .create-sidebar {
    position: static;
  }

  .form-card {
    padding: 24px;
  }
}
</style>
