<template>
  <div class="page">
    <NavBar />
    <div v-loading="loading" class="detail-wrap">
      <template v-if="contest">
        <div class="hero anim-fade">
          <div class="container hero-inner">
            <div class="hero-body">
              <div class="hero-badge">
                <span class="badge-dot" :class="`badge-${statusType}`"></span>
                {{ statusLabel }}
              </div>
              <h1 class="hero-title">{{ contest.name }}</h1>
              <div class="hero-meta">
                <span class="meta-chip">{{ contest.category }}</span>
                <span class="meta-chip">{{ contest.level }}</span>
                <span class="meta-chip">{{ contest.organizer }}</span>
              </div>
              <div class="hero-info">
                <div class="info-item">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <circle cx="8" cy="8" r="7" stroke="currentColor" stroke-width="1.2"/>
                    <path d="M8 4.5V8L10.5 10.5" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
                  </svg>
                  <span>{{ contest.contestTime }}</span>
                </div>
                <div class="info-item">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <path d="M8 1C5.2 1 3 3.2 3 6C3 9.5 8 15 8 15C8 15 13 9.5 13 6C13 3.2 10.8 1 8 1Z" stroke="currentColor" stroke-width="1.2"/>
                    <circle cx="8" cy="6" r="2" stroke="currentColor" stroke-width="1.2"/>
                  </svg>
                  <span>{{ contest.location || '线上' }}</span>
                </div>
                <div class="info-item">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <path d="M8 1V8L11 11" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
                    <circle cx="8" cy="8" r="7" stroke="currentColor" stroke-width="1.2"/>
                  </svg>
                  <span>报名截止：{{ contest.registerEndTime }}</span>
                </div>
                <div class="info-item">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <circle cx="6" cy="5" r="2.5" stroke="currentColor" stroke-width="1.2"/>
                    <path d="M1 14C1 11.2 3.2 9 6 9C8.8 9 11 11.2 11 14" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
                    <path d="M11 4L13 6L15 3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <span>{{ contest.currentCount }} 人已报名</span>
                </div>
              </div>
              <div class="hero-type">{{ typeLabel }}</div>
            </div>
          </div>
        </div>

        <div class="container detail-body">
          <div class="detail-layout">
            <div class="detail-main">
              <section class="content-section anim-fade-up anim-delay-1">
                <h2 class="section-title">竞赛详情</h2>
                <div class="description" v-html="contest.description"></div>
              </section>

              <section v-if="registrations.length > 0 && store.isAdmin" class="content-section anim-fade-up anim-delay-3">
                <h2 class="section-title">报名管理</h2>
                <div class="registration-list">
                  <div v-for="reg in registrations" :key="reg.id" class="reg-card">
                    <div class="reg-info">
                      <span class="reg-user">{{ reg.userName || reg.userId }}</span>
                      <span class="reg-type-tag" :class="reg.teamId ? 'tag-team' : 'tag-personal'">
                        {{ reg.teamId ? '团队报名' : '个人报名' }}
                      </span>
                      <span v-if="reg.status === 0" class="reg-status status-pending">待审核</span>
                      <span v-else-if="reg.status === 1" class="reg-status status-approved">已通过</span>
                      <span v-else class="reg-status status-rejected">已拒绝</span>
                    </div>
                    <div v-if="reg.status === 0" class="reg-actions">
                      <button class="btn btn-sm btn-success" @click="handleApprove(reg.id)">通过</button>
                      <button class="btn btn-sm btn-danger" @click="handleReject(reg.id)">拒绝</button>
                    </div>
                  </div>
                </div>
              </section>
            </div>

            <div class="detail-sidebar">
              <div class="sidebar-card anim-fade-up anim-delay-2">
                <h3 class="sidebar-title">报名参赛</h3>
                <div v-if="!store.isLoggedIn" class="sidebar-action">
                  <p class="sidebar-hint">请先登录后再报名</p>
                  <button class="btn btn-primary btn-block" @click="$router.push('/login')">去登录</button>
                </div>
                <div v-else-if="contest.status !== 1" class="sidebar-action">
                  <p class="sidebar-hint">{{ contest.status === 2 ? '报名已截止' : '竞赛暂未开放' }}</p>
                  <button class="btn btn-block btn-disabled" disabled>{{ contest.status === 2 ? '已截止' : '未开放' }}</button>
                </div>
                <div v-else class="sidebar-action">
                  <template v-if="contest.contestType !== 1">
                    <template v-if="personalReg">
                      <div class="reg-status-bar">
                        <span class="reg-badge" :class="'reg-badge--' + (personalReg.status === 1 ? 'approved' : personalReg.status === 0 ? 'pending' : 'rejected')">
                          {{ regStatusMap[personalReg.status] || '未知' }}
                        </span>
                        <button v-if="personalReg.status === 0 || personalReg.status === 1" class="btn btn-sm btn-danger" @click="handleCancelRegistration(personalReg)">
                          取消报名
                        </button>
                      </div>
                    </template>
                    <button v-else class="btn btn-primary btn-block" @click="registerPersonal">
                      立即报名（个人赛）
                    </button>
                  </template>
                  <template v-if="contest.contestType !== 0">
                    <template v-if="teamReg">
                      <div class="reg-status-bar">
                        <span class="reg-badge" :class="'reg-badge--' + (teamReg.status === 1 ? 'approved' : teamReg.status === 0 ? 'pending' : 'rejected')">
                          {{ regStatusMap[teamReg.status] || '未知' }}
                        </span>
                        <button v-if="teamReg.status === 0 || teamReg.status === 1" class="btn btn-sm btn-danger" @click="handleCancelRegistration(teamReg)">
                          取消报名
                        </button>
                      </div>
                    </template>
                    <template v-else-if="myTeam">
                      <div class="team-info">
                        <div class="team-info-header">
                          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                            <circle cx="6" cy="5" r="2.5" stroke="currentColor" stroke-width="1.2"/>
                            <path d="M1 14C1 11.2 3.2 9 6 9C8.8 9 11 11.2 11 14" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
                            <circle cx="12" cy="4" r="2" stroke="currentColor" stroke-width="1.2"/>
                            <path d="M9 12C9 10 10.5 8.5 12 8.5C13.5 8.5 15 10 15 12" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
                          </svg>
                          <span>{{ myTeam.teamName }}</span>
                        </div>
                        <button class="btn btn-primary btn-block" @click="registerTeamSubmit">
                          以团队报名
                        </button>
                      </div>
                    </template>
                    <button v-else class="btn btn-accent btn-block" @click="$router.push(`/team/create?contestId=${contest.id}`)">
                      创建团队
                    </button>
                  </template>
                </div>
              </div>

              <div class="sidebar-card anim-fade-up anim-delay-3">
                <h3 class="sidebar-title">竞赛信息</h3>
                <dl class="info-list">
                  <div class="info-row">
                    <dt>类别</dt>
                    <dd>{{ contest.category }}</dd>
                  </div>
                  <div class="info-row">
                    <dt>级别</dt>
                    <dd>{{ contest.level }}</dd>
                  </div>
                  <div class="info-row">
                    <dt>主办方</dt>
                    <dd>{{ contest.organizer }}</dd>
                  </div>
                  <div class="info-row">
                    <dt>地点</dt>
                    <dd>{{ contest.location || '线上' }}</dd>
                  </div>
                  <div class="info-row">
                    <dt>竞赛时间</dt>
                    <dd>{{ contest.contestTime }}</dd>
                  </div>
                  <div class="info-row">
                    <dt>报名截止</dt>
                    <dd>{{ contest.registerEndTime }}</dd>
                  </div>
                  <div class="info-row">
                    <dt>参赛形式</dt>
                    <dd>{{ typeLabel }}</dd>
                  </div>
                </dl>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import NavBar from '../../components/NavBar.vue'
import { getContestById } from '../../api/contest'
import { registerPersonal as apiRegisterPersonal, registerTeam as apiRegisterTeam, approveRegistration, rejectRegistration, cancelRegistration, pageRegistrationByUser, pageRegistrationByContest } from '../../api/registration'
import { getTeamByLeader, getTeamById } from '../../api/team'
import { useUserStore } from '../../stores/user'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const contest = ref(null)
const loading = ref(true)
const registrations = ref([])
const myTeam = ref(null)
const myRegistrations = ref([])

const statusMap = { 0: { label: '未发布', type: 'info' }, 1: { label: '报名中', type: 'success' }, 2: { label: '已截止', type: 'warning' } }
const typeMap = { 0: '仅个人赛', 1: '仅团队赛', 2: '个人赛/团队赛均可' }
const statusLabel = computed(() => statusMap[contest.value?.status]?.label || '')
const statusType = computed(() => statusMap[contest.value?.status]?.type || 'info')
const typeLabel = computed(() => typeMap[contest.value?.contestType] || '')

const regStatusMap = { 0: '待审核', 1: '已通过', 2: '已拒绝', 3: '已取消' }

const personalReg = computed(() => myRegistrations.value.find(r => !r.teamId))
const teamReg = computed(() => myRegistrations.value.find(r => r.teamId))

async function registerPersonal() {
  if (!store.isLoggedIn) { router.push('/login'); return }
  try {
    await apiRegisterPersonal({ userId: store.userId, contestId: contest.value.id, remark: '' })
    ElMessage.success('报名成功，等待审核')
    await loadMyRegistration()
  } catch (e) { /* handled by axios interceptor */ }
}

async function registerTeamSubmit() {
  if (!myTeam.value) { ElMessage.warning('请先创建团队'); return }
  try {
    await apiRegisterTeam({ userId: store.userId, contestId: contest.value.id, teamId: myTeam.value.id, remark: '' })
    ElMessage.success('团队报名成功，等待审核')
    await loadMyRegistration()
  } catch (e) { /* handled by axios interceptor */ }
}

async function loadRegistrations() {
  if (!store.isAdmin) return
  try {
    const res = await pageRegistrationByContest(route.params.id, { page: 1, size: 50 })
    registrations.value = res.data.records || []
  } catch (e) { /* ignore */ }
}

async function handleApprove(id) {
  try {
    await approveRegistration(id)
    ElMessage.success('已通过')
    await loadRegistrations()
  } catch (e) { /* ignore */ }
}

async function handleReject(id) {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝报名', { confirmButtonText: '确定', cancelButtonText: '取消', inputValidator: v => v.length >= 5 || '驳回原因不少于5个字符' })
    await rejectRegistration(id, value)
    ElMessage.success('已拒绝')
    await loadRegistrations()
  } catch (e) {
  }
}

async function loadMyRegistration() {
  if (!store.isLoggedIn || !contest.value) return
  try {
    const res = await pageRegistrationByUser(store.userId, { page: 1, size: 100 })
    myRegistrations.value = (res.data.records || []).filter(r => r.contestId === contest.value.id)
  } catch { myRegistrations.value = [] }
}

async function handleCancelRegistration(reg) {
  if (reg.status === 1) {
    try {
      await ElMessageBox.confirm('确定取消已通过的报名吗？取消后需重新报名等待审核。', '确认取消')
    } catch { return }
  }
  try {
    await cancelRegistration(reg.id, store.userId)
    ElMessage.success('已取消')
    await loadMyRegistration()
  } catch { /* handled by interceptor */ }
}

async function loadMyTeam() {
  if (!store.isLoggedIn) return
  try {
    const res = await getTeamByLeader(store.userId, contest.value?.id)
    if (res.data && res.data.contestId === contest.value?.id) {
      myTeam.value = res.data
    }
  } catch (e) { /* no team */ }
}

onMounted(async () => {
  try {
    const res = await getContestById(route.params.id)
    contest.value = res.data
    await Promise.all([loadRegistrations(), loadMyTeam(), loadMyRegistration()])
  } catch (e) { /* ignore */ } finally { loading.value = false }
})
</script>

<style scoped>
.detail-wrap {
  min-height: 60vh;
}

/* ===== Hero ===== */
.hero {
  background: linear-gradient(135deg, var(--c-primary) 0%, var(--c-primary-light) 100%);
  padding: 60px 0 48px;
  position: relative;
  overflow: hidden;
}

.hero::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 500px;
  height: 500px;
  border-radius: 50%;
  background: rgba(255,255,255,0.03);
  pointer-events: none;
}

.hero::after {
  content: '';
  position: absolute;
  bottom: -30%;
  left: -10%;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: rgba(255,255,255,0.02);
  pointer-events: none;
}

.hero-inner {
  position: relative;
  z-index: 1;
}

.hero-body {
  max-width: 800px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 14px;
  border-radius: 20px;
  background: rgba(255,255,255,0.1);
  color: rgba(255,255,255,0.9);
  font-size: 0.8rem;
  font-weight: 500;
  margin-bottom: 16px;
}

.badge-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.badge-success { background: var(--c-success); }
.badge-warning { background: var(--c-warning); }
.badge-info { background: var(--c-info); }

.hero-title {
  font-family: 'DM Serif Display', Georgia, serif;
  font-size: 2.25rem;
  font-weight: 400;
  color: #fff;
  margin: 0 0 16px;
  line-height: 1.25;
}

.hero-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.meta-chip {
  padding: 4px 12px;
  border-radius: var(--radius-sm);
  background: rgba(255,255,255,0.1);
  color: rgba(255,255,255,0.85);
  font-size: 0.8rem;
  font-weight: 500;
}

.hero-info {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: rgba(255,255,255,0.75);
  font-size: 0.85rem;
}

.hero-type {
  display: inline-block;
  padding: 4px 14px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--c-accent);
  color: var(--c-accent);
  font-size: 0.8rem;
  font-weight: 600;
}

/* ===== Detail Body ===== */
.detail-body {
  padding: 40px 0 60px;
}

.detail-layout {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 32px;
  align-items: start;
}

@media (max-width: 900px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }
}

/* ===== Main Content ===== */
.content-section {
  background: var(--c-surface);
  border-radius: var(--radius-md);
  padding: 32px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--c-border-light);
  margin-bottom: 24px;
}

.description {
  line-height: 1.8;
  color: var(--c-text);
  font-size: 0.95rem;
}

.description :deep(p) {
  margin-bottom: 1em;
}

.description :deep(img) {
  max-width: 100%;
  border-radius: var(--radius-sm);
}

.description :deep(ul), .description :deep(ol) {
  padding-left: 1.5em;
  margin-bottom: 1em;
}

.description :deep(a) {
  color: var(--c-accent);
  text-decoration: none;
}

.description :deep(a:hover) {
  text-decoration: underline;
}

.description :deep(h1), .description :deep(h2), .description :deep(h3) {
  font-family: 'DM Serif Display', Georgia, serif;
  margin: 1.5em 0 0.5em;
}

/* ===== Registration Management ===== */
.registration-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.reg-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: var(--c-bg);
  border-radius: var(--radius-sm);
  gap: 12px;
}

.reg-info {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.reg-user {
  font-weight: 600;
  font-size: 0.9rem;
}

.reg-type-tag {
  font-size: 0.75rem;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 500;
}

.tag-personal { background: rgba(58,175,133,0.12); color: var(--c-success); }
.tag-team { background: rgba(91,127,165,0.12); color: var(--c-info); }

.reg-status {
  font-size: 0.8rem;
  font-weight: 500;
}

.status-pending { color: var(--c-warning); }
.status-approved { color: var(--c-success); }
.status-rejected { color: var(--c-danger); }

.reg-actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

.btn-sm {
  padding: 4px 12px;
  font-size: 0.8rem;
  height: 30px;
}

/* ===== Sidebar ===== */
.sidebar-card {
  background: var(--c-surface);
  border-radius: var(--radius-md);
  padding: 24px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--c-border-light);
  margin-bottom: 20px;
}

.sidebar-title {
  font-family: 'DM Serif Display', Georgia, serif;
  font-size: 1.1rem;
  font-weight: 400;
  color: var(--c-primary);
  margin: 0 0 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--c-border-light);
}

.sidebar-action {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sidebar-hint {
  color: var(--c-text-muted);
  font-size: 0.85rem;
  text-align: center;
  margin: 0;
}

.reg-status-bar {
  display: flex;
  align-items: center;
  gap: 10px;
}

.reg-badge {
  font-size: 0.85rem;
  font-weight: 600;
  padding: 6px 14px;
  border-radius: 20px;
  flex: 1;
  text-align: center;
}

.reg-badge--pending {
  background: rgba(232, 168, 56, 0.12);
  color: var(--c-warning);
}

.reg-badge--approved {
  background: rgba(58, 175, 133, 0.12);
  color: var(--c-success);
}

.reg-badge--rejected {
  background: rgba(232, 93, 74, 0.12);
  color: var(--c-danger);
}

.btn-block {
  width: 100%;
  justify-content: center;
}

.btn-disabled {
  background: var(--c-border) !important;
  color: var(--c-text-light) !important;
  cursor: not-allowed !important;
  box-shadow: none !important;
  transform: none !important;
}

.btn-accent {
  background: var(--c-accent);
  color: #fff;
}

.btn-accent:hover {
  background: var(--c-accent-light);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.btn-success {
  background: var(--c-success);
  color: #fff;
}

.btn-success:hover {
  opacity: 0.9;
}

.btn-danger {
  background: var(--c-danger);
  color: #fff;
}

.btn-danger:hover {
  opacity: 0.9;
}

/* ===== Team Info ===== */
.team-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.team-info-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: var(--c-bg);
  border-radius: var(--radius-sm);
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--c-primary);
}

/* ===== Info List ===== */
.info-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid var(--c-border-light);
  font-size: 0.85rem;
}

.info-row:last-child {
  border-bottom: none;
}

.info-row dt {
  color: var(--c-text-muted);
  flex-shrink: 0;
}

.info-row dd {
  color: var(--c-text);
  font-weight: 500;
  text-align: right;
  word-break: break-word;
  max-width: 60%;
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .hero {
    padding: 40px 0 32px;
  }
  .hero-title {
    font-size: 1.75rem;
  }
  .hero-info {
    gap: 12px;
  }
  .content-section {
    padding: 20px;
  }
  .detail-body {
    padding: 24px 0 40px;
  }
  .reg-card {
    flex-direction: column;
    align-items: stretch;
  }
  .reg-actions {
    justify-content: flex-end;
  }
}
</style>
