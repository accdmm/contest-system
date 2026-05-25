<template>
  <div>
    <NavBar />
    <div class="container">
      <el-card>
        <template #header>创建团队</template>
        <el-form :model="form" label-width="100px">
          <el-form-item label="竞赛" required>
            <el-select v-model="form.contestId" filterable placeholder="选择竞赛">
              <el-option v-for="c in contests" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="团队名称" required>
            <el-input v-model="form.teamName" placeholder="请输入团队名称" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleCreate" :loading="loading">创建团队</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import NavBar from '../../components/NavBar.vue'
import { createTeam } from '../../api/team'
import { pageContests } from '../../api/contest'
import { useUserStore } from '../../stores/user'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const loading = ref(false)
const contests = ref([])
const form = reactive({ contestId: route.query.contestId || '', teamName: '' })

onMounted(async () => {
  try {
    const res = await pageContests({ page: 1, size: 100, status: 1 })
    contests.value = res.data.records || []
  } catch (e) { /* ignore */ }
})

async function handleCreate() {
  if (!form.contestId || !form.teamName) {
    ElMessage.warning('请填写完整信息')
    return
  }
  loading.value = true
  try {
    const res = await createTeam({ userId: store.userId, contestId: form.contestId, teamName: form.teamName })
    ElMessage.success('创建成功')
    router.push(`/team/${res.data.id}`)
  } finally { loading.value = false }
}
</script>

<style scoped>
.container { max-width: 600px; margin: 20px auto; }
</style>
