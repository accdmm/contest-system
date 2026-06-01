<template>
  <div class="page">
    <NavBar />
    <div class="my-teams-page">
      <div class="container">
        <div class="page-header anim-fade-up">
          <h2 class="page-title">我的团队</h2>
          <router-link to="/team/create" class="btn-create">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M8 2V14M2 8H14" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
            </svg>
            创建团队
          </router-link>
        </div>

        <div v-if="loading" class="loading-state anim-fade">
          <div class="loading-spinner" />
          <p>加载中...</p>
        </div>

        <template v-else>
          <div v-if="list.length === 0" class="empty-state anim-fade">
            <svg viewBox="0 0 120 120" fill="none" class="empty-icon">
              <circle cx="60" cy="60" r="40" stroke="currentColor" stroke-width="2" stroke-dasharray="6 4" />
              <circle cx="60" cy="60" r="20" stroke="currentColor" stroke-width="2" />
              <path d="M60 40V60L70 70" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
            </svg>
            <p>暂无团队</p>
            <router-link to="/team/create" class="btn-create-empty">创建一个团队</router-link>
          </div>

          <div v-else class="team-list">
            <div
              v-for="(item, index) in list"
              :key="item.id"
              class="team-card anim-fade-up"
              :class="`anim-delay-${Math.min(index % 6 + 1, 6)}`"
            >
              <div class="team-card__main" @click="goDetail(item.id)">
                <div class="team-card__avatar">{{ item.teamName?.charAt(0) }}</div>
                <div class="team-card__body">
                  <div class="team-card__top">
                    <h3 class="team-card__name">{{ item.teamName }}</h3>
                    <span v-if="item.leaderId === store.userId" class="role-badge">队长</span>
                  </div>
                  <div class="team-card__meta">
                    <span class="meta-item">
                      <svg width="13" height="13" viewBox="0 0 14 14" fill="none">
                        <rect x="1" y="2" width="12" height="10" rx="2" stroke="currentColor" stroke-width="1.2"/>
                        <path d="M1 4H13" stroke="currentColor" stroke-width="1.2"/>
                      </svg>
                      {{ item.teamNo }}
                    </span>
                    <span class="meta-dot" />
                    <span class="meta-item">
                      <svg width="13" height="13" viewBox="0 0 14 14" fill="none">
                        <path d="M5 1L7 3L9 1" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M7 3V13" stroke="currentColor" stroke-width="1.2"/>
                        <path d="M2 10C2 10 3.5 12 7 12C10.5 12 12 10 12 10" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
                      </svg>
                      {{ item.memberCount }} 人
                    </span>
                  </div>
                </div>
              </div>

              <div class="team-card__aside">
                <span class="status-badge" :class="`status-badge--${teamStatusType(item.status)}`">
                  {{ teamStatusLabel(item.status) }}
                </span>
                <div class="team-card__actions">
                  <button
                    v-if="item.leaderId === store.userId"
                    class="btn-action btn-action--danger"
                    @click="handleDissolve(item)"
                  >
                    解散
                  </button>
                  <button
                    v-else
                    class="btn-action btn-action--outline"
                    @click="handleLeave(item)"
                  >
                    退出
                  </button>
                </div>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import NavBar from '../../components/NavBar.vue'
import { listUserTeams, leaveTeam, dissolveTeam } from '../../api/team'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const store = useUserStore()
const list = ref([])
const loading = ref(true)

const teamStatusMap = { 0: '组建中', 1: '待审核', 2: '已通过', 3: '已驳回', 4: '已解散' }
const teamStatusTypeMap = { 0: 'warning', 1: 'pending', 2: 'success', 3: 'danger', 4: 'info' }
const teamStatusLabel = s => teamStatusMap[s] || '未知'
const teamStatusType = s => teamStatusTypeMap[s] || 'info'

async function fetchData() {
  loading.value = true
  try {
    const res = await listUserTeams(store.userId)
    list.value = res.data || []
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
}

function goDetail(id) {
  router.push(`/team/${id}`)
}

async function handleLeave(item) {
  try {
    await ElMessageBox.confirm(`确定要退出团队「${item.teamName}」吗？`, '退出团队', {
      confirmButtonText: '确定退出',
      cancelButtonText: '取消',
      type: 'warning',
      roundButton: true,
    })
    await leaveTeam(item.id, store.userId)
    ElMessage.success('已退出团队')
    fetchData()
  } catch {
  }
}

async function handleDissolve(item) {
  try {
    await ElMessageBox.confirm(`确定要解散团队「${item.teamName}」吗？此操作不可撤销。`, '解散团队', {
      confirmButtonText: '确定解散',
      cancelButtonText: '取消',
      type: 'warning',
      roundButton: true,
    })
    await dissolveTeam(item.id, store.userId)
    ElMessage.success('团队已解散')
    fetchData()
  } catch {
  }
}

onMounted(fetchData)
</script>

<style scoped>
.my-teams-page {
  min-height: calc(100vh - 72px);
  padding: 40px 0;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
}

.page-title {
  font-family: 'DM Serif Display', Georgia, serif;
  font-size: 1.6rem;
  color: var(--c-primary);
  margin: 0;
}

.btn-create {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 22px;
  background: var(--c-primary);
  color: #fff;
  font-family: 'DM Sans', sans-serif;
  font-size: 0.85rem;
  font-weight: 600;
  border-radius: var(--radius-sm);
  text-decoration: none;
  transition: var(--transition);
}
.btn-create:hover {
  background: var(--c-primary-light);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

/* === Loading === */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: var(--c-text-muted);
  gap: 12px;
}
.loading-spinner {
  width: 36px;
  height: 36px;
  border: 3px solid var(--c-border);
  border-top-color: var(--c-primary);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}

/* === Empty === */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: var(--c-text-muted);
  gap: 16px;
}
.empty-icon {
  width: 80px;
  height: 80px;
  opacity: 0.5;
}
.empty-state p {
  font-size: 0.95rem;
}
.btn-create-empty {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 24px;
  background: var(--c-primary);
  color: #fff;
  font-size: 0.85rem;
  font-weight: 600;
  border-radius: var(--radius-sm);
  text-decoration: none;
  transition: var(--transition);
}
.btn-create-empty:hover {
  background: var(--c-primary-light);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

/* === Team List === */
.team-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.team-card {
  display: flex;
  align-items: center;
  background: var(--c-surface);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--c-border-light);
  overflow: hidden;
  transition: var(--transition);
}
.team-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.team-card__main {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 20px 24px;
  cursor: pointer;
  min-width: 0;
}

.team-card__avatar {
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

.team-card__body {
  flex: 1;
  min-width: 0;
}

.team-card__top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.team-card__name {
  font-family: 'DM Serif Display', Georgia, serif;
  font-size: 1.05rem;
  color: var(--c-primary);
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.role-badge {
  font-size: 0.7rem;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 20px;
  background: rgba(232, 93, 74, 0.12);
  color: var(--c-accent);
  flex-shrink: 0;
}

.team-card__meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 0.82rem;
  color: var(--c-text-muted);
}

.meta-dot {
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: var(--c-border);
}

/* === Aside === */
.team-card__aside {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
  padding: 20px 24px 20px 0;
  flex-shrink: 0;
}

.status-badge {
  font-size: 0.75rem;
  font-weight: 600;
  padding: 3px 12px;
  border-radius: 20px;
  letter-spacing: 0.02em;
}
.status-badge--warning { background: #fef3e2; color: #b8860b; }
.status-badge--pending { background: #e8edf5; color: #2c5f8a; }
.status-badge--success { background: #e3f5ed; color: #1f8b5c; }
.status-badge--danger { background: #fde8e5; color: #c0392b; }
.status-badge--info { background: #eef0f2; color: #6b7280; }

.team-card__actions {
  display: flex;
  gap: 6px;
}

.btn-action {
  font-family: 'DM Sans', sans-serif;
  font-size: 0.8rem;
  font-weight: 600;
  padding: 6px 16px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: var(--transition);
  border: none;
}

.btn-action--danger {
  background: transparent;
  border: 1.5px solid var(--c-danger);
  color: var(--c-danger);
}
.btn-action--danger:hover {
  background: var(--c-danger);
  color: #fff;
}

.btn-action--outline {
  background: transparent;
  border: 1.5px solid var(--c-border);
  color: var(--c-text-muted);
}
.btn-action--outline:hover {
  border-color: var(--c-danger);
  color: var(--c-danger);
  background: rgba(232, 93, 74, 0.06);
}

@media (max-width: 640px) {
  .team-card {
    flex-direction: column;
    align-items: stretch;
  }
  .team-card__aside {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px 16px;
  }
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .btn-create {
    width: 100%;
    justify-content: center;
  }
}
</style>
