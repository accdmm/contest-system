<template>
  <div>
    <NavBar />
    <div class="container" v-loading="loading">
      <el-card v-if="team">
        <template #header>
          <span>{{ team.teamName }} - {{ team.teamNo }}</span>
          <el-tag :type="teamStatusType" style="margin-left:10px">{{ teamStatusLabel }}</el-tag>
        </template>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="邀请码">
            <span v-if="team.inviteCode">
              <strong>{{ team.inviteCode }}</strong>
              <el-button type="primary" link @click="generateCode">重新生成</el-button>
            </span>
            <el-button v-else type="primary" size="small" @click="generateCode">生成邀请码</el-button>
          </el-descriptions-item>
          <el-descriptions-item label="成员数">{{ team.memberCount }}</el-descriptions-item>
        </el-descriptions>

        <h3 style="margin:20px 0 10px">团队成员</h3>
        <el-table :data="members" border stripe>
          <el-table-column prop="userId" label="成员ID" />
          <el-table-column prop="role" label="角色">
            <template #default="{ row }">{{ row.role === 1 ? '队长' : '成员' }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态">
            <template #default="{ row }">{{ memberStatusMap[row.status] }}</template>
          </el-table-column>
          <el-table-column label="操作" width="200" v-if="isLeader">
            <template #default="{ row }">
              <el-button v-if="row.status === 0" size="small" type="success" @click="approve(row.id)">通过</el-button>
              <el-button v-if="row.status === 0" size="small" type="danger" @click="reject(row.id)">拒绝</el-button>
              <el-button v-if="row.status === 1 && row.role !== 1" size="small" @click="remove(row.id)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div style="margin-top:20px; display:flex; gap:10px">
          <el-button v-if="isLeader && team.status === 0" type="primary" @click="submitReview">提交报名审核</el-button>
          <el-button v-if="isLeader && team.status === 0" @click="dissolve">解散团队</el-button>
          <el-button v-if="isLeader && team.status === 1" disabled>已提交审核</el-button>
        </div>

        <el-divider />
        <h3>加入团队</h3>
        <el-input v-model="inviteCode" placeholder="输入邀请码" style="width:300px;margin-right:10px" />
        <el-button type="primary" @click="joinTeam">加入</el-button>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import NavBar from '../../components/NavBar.vue'
import { getTeamById, listTeamMembers, listPendingMembers, generateInviteCode, approveMember, rejectMember, removeMember, dissolveTeam, submitTeamReview, joinByInviteCode } from '../../api/team'
import { useUserStore } from '../../stores/user'

const route = useRoute()
const store = useUserStore()
const loading = ref(true)
const team = ref(null)
const members = ref([])
const inviteCode = ref('')

const teamStatusMap = { 0: '组建中', 1: '待审核', 2: '已通过', 3: '已驳回', 4: '已解散' }
const teamStatusTypeMap = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'danger', 4: 'info' }
const memberStatusMap = { 0: '待审核', 1: '已通过', 2: '已拒绝' }

const isLeader = computed(() => team.value?.leaderId === store.userId)
const teamStatusLabel = computed(() => teamStatusMap[team.value?.status] || '')
const teamStatusType = computed(() => teamStatusTypeMap[team.value?.status] || 'info')

async function fetchData() {
  try {
    const [teamRes, memberRes, pendingRes] = await Promise.all([
      getTeamById(route.params.id),
      listTeamMembers(route.params.id),
      listPendingMembers(route.params.id)
    ])
    team.value = teamRes.data
    members.value = [...(memberRes.data || []), ...(pendingRes.data || [])]
  } catch (e) { /* ignore */ } finally { loading.value = false }
}

async function generateCode() {
  await generateInviteCode(team.value.id, store.userId)
  ElMessage.success('邀请码已重新生成')
  fetchData()
}

async function approve(id) { await approveMember(team.value.id, store.userId, id); ElMessage.success('已通过'); fetchData() }
async function reject(id) { await rejectMember(team.value.id, store.userId, id); ElMessage.success('已拒绝'); fetchData() }
async function remove(id) { await removeMember(team.value.id, store.userId, id); ElMessage.success('已移除'); fetchData() }

async function dissolve() {
  await dissolveTeam(team.value.id, store.userId)
  ElMessage.success('团队已解散')
  fetchData()
}

async function submitReview() {
  await submitTeamReview(team.value.id, store.userId)
  ElMessage.success('已提交审核')
  fetchData()
}

async function joinTeam() {
  if (!inviteCode.value) { ElMessage.warning('请输入邀请码'); return }
  await joinByInviteCode({ userId: store.userId, inviteCode: inviteCode.value })
  ElMessage.success('已申请加入，等待队长审核')
  inviteCode.value = ''
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.container { max-width: 900px; margin: 20px auto; }
</style>
