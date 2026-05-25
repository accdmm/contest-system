<template>
  <div>
    <NavBar />
    <div class="container" v-loading="loading">
      <el-card v-if="contest">
        <div class="header">
          <h1>{{ contest.name }}</h1>
          <el-tag :type="statusType" size="large">{{ statusLabel }}</el-tag>
        </div>

        <el-descriptions :column="2" border style="margin:20px 0">
          <el-descriptions-item label="类别">{{ contest.category }}</el-descriptions-item>
          <el-descriptions-item label="级别">{{ contest.level }}</el-descriptions-item>
          <el-descriptions-item label="主办方">{{ contest.organizer }}</el-descriptions-item>
          <el-descriptions-item label="地点">{{ contest.location }}</el-descriptions-item>
          <el-descriptions-item label="竞赛时间">{{ contest.contestTime }}</el-descriptions-item>
          <el-descriptions-item label="报名截止">{{ contest.registerEndTime }}</el-descriptions-item>
          <el-descriptions-item label="参赛形式">{{ typeLabel }}</el-descriptions-item>
          <el-descriptions-item label="已报名">{{ contest.currentCount }}人</el-descriptions-item>
        </el-descriptions>

        <div v-html="contest.description" class="description"></div>

        <div style="margin-top:20px">
          <el-button type="primary" size="large" @click="handleRegister" :disabled="contest.status !== 1">
            {{ contest.status === 1 ? '立即报名' : '当前不可报名' }}
          </el-button>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="dialogVisible" title="选择报名方式" width="400px">
      <p>竞赛类型：{{ typeLabel }}</p>
      <template v-if="contest?.contestType !== 1">
        <el-button type="primary" @click="registerPersonal" style="width:100%;margin-bottom:10px">个人赛报名</el-button>
      </template>
      <template v-if="contest?.contestType !== 0">
        <el-button @click="$router.push(`/team/create?contestId=${contest?.id}`)" style="width:100%">创建团队报名</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import NavBar from '../../components/NavBar.vue'
import { getContestById } from '../../api/contest'
import { registerPersonal } from '../../api/registration'
import { useUserStore } from '../../stores/user'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const contest = ref(null)
const loading = ref(true)
const dialogVisible = ref(false)

const statusMap = { 0: { label: '未发布', type: 'info' }, 1: { label: '报名中', type: 'success' }, 2: { label: '已截止', type: 'warning' } }
const typeMap = { 0: '仅个人赛', 1: '仅团队赛', 2: '个人赛/团队赛均可' }
const statusLabel = computed(() => statusMap[contest.value?.status]?.label || '')
const statusType = computed(() => statusMap[contest.value?.status]?.type || 'info')
const typeLabel = computed(() => typeMap[contest.value?.contestType] || '')

async function handleRegister() {
  if (!store.isLoggedIn) { router.push('/login'); return }
  dialogVisible.value = true
}

async function registerPersonal() {
  try {
    await registerPersonal({ userId: store.userId, contestId: contest.value.id, remark: '' })
    ElMessage.success('报名成功，等待审核')
    dialogVisible.value = false
  } catch (e) { /* handled by axios interceptor */ }
}

onMounted(async () => {
  try {
    const res = await getContestById(route.params.id)
    contest.value = res.data
  } catch (e) { /* ignore */ } finally { loading.value = false }
})
</script>

<style scoped>
.container { max-width: 1000px; margin: 20px auto; padding: 0 20px; }
.header { display: flex; align-items: center; gap: 15px; }
.description { padding: 20px 0; border-top: 1px solid #eee; }
</style>
