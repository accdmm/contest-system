<template>
  <div class="permission-page">
    <div class="page-container">
      <h2 class="page-title">权限管理</h2>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="按角色分配" name="role">
          <div class="role-tabs">
            <el-radio-group v-model="currentRole" @change="loadRolePerms">
              <el-radio-button :value="1">管理员</el-radio-button>
              <el-radio-button :value="0">学生</el-radio-button>
            </el-radio-group>
          </div>
          <el-card class="perm-card">
            <template #header>
              <span>角色权限 — {{ currentRole === 1 ? '管理员' : '学生' }}</span>
              <el-button type="primary" size="small" style="float:right" @click="saveRole">保存</el-button>
            </template>
            <div v-loading="roleLoading">
              <div v-for="group in permissionGroups" :key="group.module" class="perm-group">
                <h4>{{ group.module }}</h4>
                <el-checkbox-group v-model="selectedPerms">
                  <el-checkbox v-for="p in group.permissions" :key="p.id" :label="p.id" :value="p.id">
                    {{ p.name }} <span class="perm-code">({{ p.code }})</span>
                  </el-checkbox>
                </el-checkbox-group>
              </div>
            </div>
          </el-card>
        </el-tab-pane>

        <el-tab-pane label="按用户分配" name="user">
          <div class="user-search">
            <el-input v-model="searchKeyword" placeholder="搜索学号/姓名" clearable style="width:300px" @keyup.enter="searchUser" @clear="searchUser" />
            <el-button type="primary" @click="searchUser">搜索</el-button>
          </div>
          <el-table :data="userList" v-loading="userLoading" style="margin-bottom:16px">
            <el-table-column prop="username" label="学号" width="140" />
            <el-table-column prop="name" label="姓名" width="120" />
            <el-table-column prop="college" label="学院" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button size="small" @click="editUserPerms(row)">分配权限</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-dialog v-model="permDialog" :title="'分配权限 — ' + currentUser?.name" width="600px">
            <div v-loading="userPermLoading">
              <div v-for="group in permissionGroups" :key="group.module" class="perm-group">
                <h4>{{ group.module }}</h4>
                <el-checkbox-group v-model="userSelectedPerms">
                  <el-checkbox v-for="p in group.permissions" :key="p.id" :label="p.id" :value="p.id">
                    {{ p.name }} <span class="perm-code">({{ p.code }})</span>
                  </el-checkbox>
                </el-checkbox-group>
              </div>
            </div>
            <template #footer>
              <el-button @click="permDialog = false">取消</el-button>
              <el-button type="primary" @click="saveUserPerms">保存</el-button>
            </template>
          </el-dialog>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'
import { pageUsers } from '../../api/user'
const activeTab = ref('role')
const currentRole = ref(1)
const allPermissions = ref([])
const selectedPerms = ref([])
const roleLoading = ref(false)

// User-level
const searchKeyword = ref('')
const userList = ref([])
const userLoading = ref(false)
const permDialog = ref(false)
const currentUser = ref(null)
const userSelectedPerms = ref([])
const userPermLoading = ref(false)

const permissionGroups = computed(() => {
  const map = {}
  allPermissions.value.forEach(p => {
    if (!map[p.module]) map[p.module] = { module: p.module, permissions: [] }
    map[p.module].permissions.push(p)
  })
  return Object.values(map)
})

async function loadAllPerms() {
  try {
    const res = await request.get('/permission/list')
    allPermissions.value = res.data || []
  } catch {}
}

async function loadRolePerms() {
  roleLoading.value = true
  try {
    const res = await request.get(`/permission/role/${currentRole.value}`)
    selectedPerms.value = res.data || []
  } finally {
    roleLoading.value = false
  }
}

async function saveRole() {
  try {
    await request.put(`/permission/role/${currentRole.value}`, { permissionIds: selectedPerms.value })
    ElMessage.success('保存成功')
  } catch {}
}

async function searchUser() {
  userLoading.value = true
  try {
    const res = await pageUsers({ keyword: searchKeyword.value, page: 1, size: 20 })
    userList.value = res.data.records || []
  } finally {
    userLoading.value = false
  }
}

async function editUserPerms(row) {
  currentUser.value = row
  userPermLoading.value = true
  permDialog.value = true
  try {
    const res = await request.get(`/permission/user/${row.id}`)
    userSelectedPerms.value = res.data || []
  } finally {
    userPermLoading.value = false
  }
}

async function saveUserPerms() {
  try {
    await request.put(`/permission/user/${currentUser.value.id}`, { permissionIds: userSelectedPerms.value })
    ElMessage.success('保存成功')
    permDialog.value = false
  } catch {}
}

onMounted(() => {
  loadAllPerms()
  loadRolePerms()
})
</script>

<style scoped>
.permission-page { background: #f5f7fa; }
.page-container { max-width: 960px; margin: 0 auto; padding: 24px 16px; }
.page-title { font-size: 22px; font-weight: 600; margin-bottom: 20px; }
.role-tabs { margin-bottom: 16px; }
.perm-card { margin-bottom: 24px; }
.perm-group { margin-bottom: 20px; }
.perm-group h4 { margin: 0 0 8px; color: #409eff; font-size: 15px; }
.perm-code { color: #999; font-size: 12px; margin-left: 4px; }
.el-checkbox { margin-right: 16px; margin-bottom: 8px; }
.user-search { display: flex; gap: 8px; margin-bottom: 16px; }
</style>
