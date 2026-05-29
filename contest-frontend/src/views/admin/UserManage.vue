<template>
  <div class="um-page">
    <NavBar />
    <div class="um-bg">
      <div class="container">
        <div class="um-header">
          <div>
            <h1 class="um-title">用户管理</h1>
            <p class="um-subtitle">查询和编辑用户信息</p>
          </div>
          <div class="um-search">
            <el-input v-model="keyword" placeholder="搜索学号/姓名/学院" clearable class="um-search-input" @keyup.enter="search" @clear="search">
              <template #prefix>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
              </template>
            </el-input>
            <el-button class="um-search-btn" @click="search">搜索</el-button>
          </div>
        </div>

        <div class="um-card">
          <el-table :data="list" v-loading="loading" element-loading-background="rgba(245,243,239,0.8)" empty-text="暂无数据">
            <el-table-column prop="id" label="ID" width="64" />
            <el-table-column prop="username" label="学号" width="140" />
            <el-table-column prop="name" label="姓名" width="120" />
            <el-table-column prop="college" label="学院" min-width="160" />
            <el-table-column prop="major" label="专业" min-width="160" />
            <el-table-column prop="role" label="角色" width="80" align="center">
              <template #default="{ row }">
                <span class="um-role" :class="row.role === 1 ? 'um-role--admin' : 'um-role--user'">{{ row.role === 1 ? '管理员' : '学生' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160" align="right">
              <template #default="{ row }">
                <el-button class="um-action-btn" @click="editUser(row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-if="total > 0"
            background
            layout="prev, pager, next"
            :total="total"
            :page-size="size"
            :current-page="page"
            @current-change="pageChange"
            class="um-pagination"
          />
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" title="编辑用户" width="480px" class="um-dialog" :close-on-click-modal="false">
      <el-form :model="editForm" label-position="top" class="um-form">
        <el-form-item label="学号">
          <el-input :model-value="editForm.username" disabled />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="学院">
          <el-input v-model="editForm.college" />
        </el-form-item>
        <el-form-item label="专业">
          <el-input v-model="editForm.major" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button class="um-dialog-cancel" @click="dialogVisible = false">取消</el-button>
        <el-button class="um-dialog-save" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import NavBar from '../../components/NavBar.vue'
import { pageUsers, updateProfile, getUserById } from '../../api/user'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const loading = ref(true)
const keyword = ref('')
const dialogVisible = ref(false)
const saving = ref(false)
const editForm = ref({})

async function fetchData() {
  loading.value = true
  try {
    const res = await pageUsers({ keyword: keyword.value, page: page.value, size })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

function search() {
  page.value = 1
  fetchData()
}

function pageChange(p) {
  page.value = p
  fetchData()
}

async function editUser(row) {
  try {
    const res = await getUserById(row.id)
    editForm.value = { ...res.data }
    dialogVisible.value = true
  } catch {
    ElMessage.error('获取用户信息失败')
  }
}

async function handleSave() {
  saving.value = true
  try {
    await updateProfile(editForm.value.id, editForm.value)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchData()
  } finally { saving.value = false }
}

onMounted(fetchData)
</script>

<style scoped>
.um-page { min-height: 100vh; padding-top: 72px; background: var(--c-bg); font-family: 'DM Sans', sans-serif; }
.um-bg { padding: 40px 0 60px; }
.um-header { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 32px; gap: 16px; flex-wrap: wrap; }
.um-title { font-family: 'DM Serif Display', serif; font-size: 32px; color: var(--c-text); margin: 0 0 6px; }
.um-subtitle { font-size: 14px; color: var(--c-text); opacity: 0.5; margin: 0; }
.um-search { display: flex; gap: 8px; align-items: center; }
.um-search-input { width: 260px; }
.um-search-input :deep(.el-input__wrapper) { border-radius: 10px; border: 1px solid var(--c-border); box-shadow: none; }
.um-search-input :deep(.el-input__wrapper:hover) { border-color: var(--c-primary); }
.um-search-input :deep(.el-input__wrapper.is-focus) { border-color: var(--c-primary); }
.um-search-btn { height: 36px; padding: 0 20px; border: none; border-radius: 10px; background: var(--c-primary); color: #fff; font-weight: 500; }
.um-search-btn:hover { background: var(--c-primary-light); }
.um-card { background: var(--c-surface); border-radius: var(--radius-md); padding: 24px; box-shadow: var(--shadow-sm); }
.um-card :deep(.el-table) { border: none; }
.um-card :deep(.el-table th.el-table__cell) { background: var(--c-bg); color: var(--c-primary); font-weight: 600; font-size: 12px; letter-spacing: 0.02em; border-bottom: none; }
.um-card :deep(.el-table th.el-table__cell > .cell) { padding: 12px 16px; }
.um-card :deep(.el-table td.el-table__cell) { border-bottom: 1px solid var(--c-border-light); }
.um-card :deep(.el-table::before) { display: none; }
.um-role { display: inline-block; padding: 2px 12px; border-radius: 20px; font-size: 12px; font-weight: 500; }
.um-role--admin { background: rgba(232,93,74,0.1); color: var(--c-accent); }
.um-role--user { background: rgba(26,35,50,0.06); color: var(--c-primary); }
.um-action-btn { height: 30px; padding: 0 14px; border: 1px solid var(--c-border); border-radius: 8px; background: var(--c-surface); color: var(--c-primary); font-size: 12px; font-weight: 500; }
.um-action-btn:hover { border-color: var(--c-primary); background: var(--c-primary); color: #fff; }
.um-pagination { margin-top: 20px; display: flex; justify-content: center; }
.um-dialog :deep(.el-dialog) { border-radius: var(--radius-md); }
.um-dialog :deep(.el-dialog__header) { background: var(--c-bg); padding: 24px 28px 16px; }
.um-dialog :deep(.el-dialog__title) { font-family: 'DM Serif Display', serif; font-size: 20px; color: var(--c-primary); }
.um-dialog :deep(.el-dialog__body) { padding: 28px; }
.um-form :deep(.el-form-item__label) { font-size: 13px; font-weight: 600; color: var(--c-primary); }
.um-form :deep(.el-input__wrapper) { border-radius: 10px; border: 1px solid var(--c-border); box-shadow: none; }
.um-form :deep(.el-input__wrapper:hover) { border-color: var(--c-primary); }
.um-form :deep(.el-input__wrapper.is-focus) { border-color: var(--c-primary); }
.um-dialog-cancel { height: 38px; padding: 0 22px; border: 1px solid var(--c-border); border-radius: 10px; background: var(--c-surface); color: var(--c-primary); font-weight: 500; }
.um-dialog-save { height: 38px; padding: 0 22px; border: none; border-radius: 10px; background: var(--c-primary); color: #fff; font-weight: 500; }
.um-dialog-save:hover { background: var(--c-primary-light); }

@media (max-width: 768px) {
  .um-card { overflow-x: auto; }
  .um-card .el-table { min-width: 720px; }
}
</style>
