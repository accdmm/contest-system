<template>
  <div class="page">
    <NavBar />
    <div class="teams-wrap">
      <div class="container">
        <div class="page-header anim-fade-up">
          <h1 class="page-title">我指导的团队</h1>
          <p class="page-desc">查看你作为指导教师负责的所有团队</p>
        </div>

        <div v-loading="loading" class="teams-list anim-fade-up anim-delay-1">
          <div v-if="teams.length === 0" class="empty-state">
            <p>暂无指导的团队</p>
          </div>

          <div v-for="t in teams" :key="t.id" class="team-card" @click="router.push(`/team/${t.id}`)">
            <div class="team-avatar">{{ t.teamName?.charAt(0) }}</div>
            <div class="team-body">
              <div class="team-name-row">
                <span class="team-name">{{ t.teamName }}</span>
                <el-tag :type="teamStatusType(t.status)" size="small">{{ teamStatusLabel(t.status) }}</el-tag>
              </div>
              <div class="team-meta">
                <span>{{ t.teamNo }}</span>
                <span class="meta-divider" />
                <span>{{ t.memberCount }} 名成员</span>
              </div>
            </div>
            <div class="team-arrow">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M6 4L10 8L6 12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import NavBar from '../../components/NavBar.vue'
import { getTeacherTeams } from '../../api/team'

const router = useRouter()
const loading = ref(true)
const teams = ref([])

const teamStatusMap = { 0: '组建中', 1: '待审核', 2: '已通过', 3: '已驳回' }
const teamStatusTypeMap = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'danger' }

function teamStatusLabel(status) { return teamStatusMap[status] || '' }
function teamStatusType(status) { return teamStatusTypeMap[status] || 'info' }

onMounted(async () => {
  try {
    const res = await getTeacherTeams()
    teams.value = res.data || []
  } catch (e) { /* ignore */ } finally { loading.value = false }
})
</script>

<style scoped>
.teams-wrap {
  padding: 40px 0;
}

.page-header {
  margin-bottom: 32px;
}

.page-title {
  font-family: 'DM Serif Display', Georgia, serif;
  font-size: 1.8rem;
  color: var(--c-primary);
  margin-bottom: 8px;
}

.page-desc {
  color: var(--c-text-muted);
  font-size: 0.9rem;
}

.teams-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.team-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: var(--c-surface);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--c-border-light);
  cursor: pointer;
  transition: var(--transition);
}

.team-card:hover {
  border-color: var(--c-primary-light);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.team-avatar {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-sm);
  background: var(--c-primary);
  color: #fff;
  font-family: 'DM Serif Display', Georgia, serif;
  font-size: 1.2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.team-body {
  flex: 1;
  min-width: 0;
}

.team-name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.team-name {
  font-size: 1rem;
  font-weight: 600;
  color: var(--c-primary);
}

.team-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.82rem;
  color: var(--c-text-muted);
}

.meta-divider {
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: var(--c-border);
}

.team-arrow {
  color: var(--c-text-light);
  flex-shrink: 0;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
  color: var(--c-text-muted);
}

.empty-state p {
  font-size: 0.95rem;
}
</style>
