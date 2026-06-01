<template>
  <div class="page">
    <NavBar />
    <div v-loading="loading" class="detail-wrap">
      <template v-if="contest">
        <div class="hero anim-fade" :style="heroBg">
          <div class="hero-bg">
            <div class="hero-shape hero-shape--1"></div>
            <div class="hero-shape hero-shape--2"></div>
            <div class="hero-shape hero-shape--3"></div>
            <div class="hero-stars"></div>
          </div>
          <div class="container hero-inner">
            <div class="hero-body">
              <div class="hero-badge">
                <span class="badge-dot" :class="`badge-${statusType}`"></span>
                {{ statusLabel }}
              </div>
              <h1 class="hero-title">{{ contest.name }}</h1>
              <div class="hero-meta">
                <span class="meta-chip">{{ contest.category }}</span>
                <span class="meta-chip" :class="contest.level === '国家级' ? 'meta-chip--gold' : ''">{{ contest.level }}</span>
                <span class="meta-chip">{{ contest.organizer }}</span>
              </div>
              <div class="hero-info">
                <div class="info-item">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <circle cx="8" cy="8" r="7" stroke="currentColor" stroke-width="1.2"/>
                    <path d="M8 4.5V8L10.5 10.5" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
                  </svg>
                  <span>{{ formatTime(contest.contestTime) }}</span>
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
                  <span>报名截止：{{ formatTime(contest.registerEndTime) }}</span>
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
                    <template v-else>
                      <div class="team-info">
                        <el-select v-model="selectedTeamId" placeholder="选择团队" class="team-select" size="large">
                          <el-option v-for="t in myTeams" :key="t.id" :label="t.teamName" :value="t.id" />
                        </el-select>
                        <button class="btn btn-accent btn-block" :disabled="!selectedTeamId" @click="registerTeamSubmit">
                          以团队报名
                        </button>
                        <button class="btn btn-block btn-secondary" @click="$router.push('/team/create')">
                          创建新团队
                        </button>
                      </div>
                    </template>
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
                    <dd>{{ formatTime(contest.contestTime) }}</dd>
                  </div>
                  <div class="info-row">
                    <dt>报名截止</dt>
                    <dd>{{ formatTime(contest.registerEndTime) }}</dd>
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
import { listUserTeams, getTeamById } from '../../api/team'
import { formatTime } from '../../utils/format'
import { useUserStore } from '../../stores/user'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const contest = ref(null)
const loading = ref(true)
const registrations = ref([])
const myTeams = ref([])
const selectedTeamId = ref(null)
const myRegistrations = ref([])

const statusMap = { 0: { label: '未发布', type: 'info' }, 1: { label: '报名中', type: 'success' }, 2: { label: '已截止', type: 'warning' } }
const typeMap = { 0: '仅个人赛', 1: '仅团队赛', 2: '个人赛/团队赛均可' }
const statusLabel = computed(() => statusMap[contest.value?.status]?.label || '')
const statusType = computed(() => statusMap[contest.value?.status]?.type || 'info')
const typeLabel = computed(() => typeMap[contest.value?.contestType] || '')

const categoryGradients = {
  '理工类': 'linear-gradient(160deg, #0f1923 0%, #1a2a3a 40%, #0f1923 100%)',
  '文史类': 'linear-gradient(160deg, #3a204a 0%, #5a3a6a 40%, #3a204a 100%)',
  '艺术类': 'linear-gradient(160deg, #4a1510 0%, #7a2a20 40%, #4a1510 100%)',
  '体育类': 'linear-gradient(160deg, #0a3a2a 0%, #1a5a3a 40%, #0a3a2a 100%)',
  '创新创业类': 'linear-gradient(160deg, #3a2a0a 0%, #5a4a1a 40%, #3a2a0a 100%)',
}
const heroBg = computed(() => ({
  background: categoryGradients[contest.value?.category] || 'linear-gradient(160deg, #0a1018 0%, #12102a 40%, #0a1018 100%)'
}))

const regStatusMap = { 0: '待审核', 1: '已通过', 2: '已拒绝', 3: '已取消' }

const personalReg = computed(() => myRegistrations.value.find(r => !r.teamId && r.status !== 3))
const teamReg = computed(() => myRegistrations.value.find(r => r.teamId && r.status !== 3))

async function registerPersonal() {
  if (!store.isLoggedIn) { router.push('/login'); return }
  try {
    await apiRegisterPersonal({ userId: store.userId, contestId: contest.value.id, remark: '' })
    ElMessage.success('报名成功，等待审核')
    await loadMyRegistration()
  } catch (e) { /* handled by axios interceptor */ }
}

async function registerTeamSubmit() {
  if (!selectedTeamId.value) { ElMessage.warning('请选择团队'); return }
  try {
    await apiRegisterTeam({ userId: store.userId, contestId: contest.value.id, teamId: selectedTeamId.value, remark: '' })
    ElMessage.success('团队报名成功，等待审核')
    selectedTeamId.value = null
    await loadMyRegistration()
  } catch (e) { /* handled by axios interceptor */ }
}

async function loadRegistrations() {
  if (!store.isAdmin) return
  try {
    const res = await pageRegistrationByContest(route.params.id, { page: 1, size: 50 })
    registrations.value = res.data.records || []
  } catch (e) { registrations.value = [] }
}

async function handleApprove(id) {
  try {
    await approveRegistration(id)
    ElMessage.success('已通过')
    await loadRegistrations()
  } catch (e) { ElMessage.error('审批操作失败') }
}

async function handleReject(id) {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝报名', { confirmButtonText: '确定', cancelButtonText: '取消', inputValidator: v => v.length >= 5 || '驳回原因不少于5个字符' })
    await rejectRegistration(id, value)
    ElMessage.success('已拒绝')
    await loadRegistrations()
  } catch (e) { /* user cancelled prompt or error handled by interceptor */ }
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

async function loadMyTeams() {
  if (!store.isLoggedIn) return
  try {
    const res = await listUserTeams(store.userId)
    myTeams.value = res.data || []
  } catch (e) { myTeams.value = [] }
}

onMounted(async () => {
  try {
    const res = await getContestById(route.params.id)
    contest.value = res.data
    await Promise.all([loadRegistrations(), loadMyTeams(), loadMyRegistration()])
  } catch (e) { contest.value = null } finally { loading.value = false }
})
</script>

<style scoped>
.detail-wrap {
  min-height: 60vh;
}

/* ===== Hero ===== */
.hero {
  position: relative;
  padding: 80px 0 64px;
  overflow: hidden;
}

.hero::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.65' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)' opacity='0.04'/%3E%3C/svg%3E");
  opacity: 0.4;
  pointer-events: none;
  z-index: 1;
}

.hero::after {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 30% 40%, rgba(168,85,247,0.12) 0%, transparent 50%),
    radial-gradient(ellipse at 70% 60%, rgba(201,168,76,0.10) 0%, transparent 50%),
    radial-gradient(ellipse at 50% 20%, rgba(232,93,74,0.08) 0%, transparent 40%);
  animation: nebula-drift 20s ease-in-out infinite alternate;
  pointer-events: none;
  z-index: 0;
}

.hero > * {
  position: relative;
  z-index: 2;
}

.hero-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 1;
}

.hero-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.06;
}

.hero-shape--1 {
  width: 600px;
  height: 600px;
  background: var(--c-gold);
  top: -200px;
  right: -100px;
  opacity: 0.04;
}

.hero-shape--2 {
  width: 500px;
  height: 500px;
  border: 1px solid rgba(201, 168, 76, 0.08);
  top: 50%;
  left: -200px;
  transform: translateY(-50%);
  background: none;
}

.hero-shape--3 {
  width: 200px;
  height: 200px;
  background: var(--c-accent);
  bottom: 20%;
  right: 30%;
  opacity: 0.06;
}

.hero-stars {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 1;
}

.hero-stars::before {
  content: '';
  position: absolute;
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: #fff;
  animation: star-twinkle 4s ease-in-out infinite;
  box-shadow:
    80px 60px 0 0 rgba(255,255,255,0.6),
    200px 30px 0 0 rgba(255,255,255,0.5),
    350px 80px 0 0 rgba(201,168,76,0.5),
    500px 45px 0 0 rgba(255,255,255,0.7),
    650px 100px 0 0 rgba(200,168,255,0.5),
    100px 150px 0 0 rgba(255,255,255,0.4),
    300px 180px 0 0 rgba(201,168,76,0.4),
    550px 200px 0 0 rgba(255,255,255,0.6),
    700px 160px 0 0 rgba(200,168,255,0.4),
    150px 250px 0 0 rgba(255,255,255,0.5),
    400px 280px 0 0 rgba(201,168,76,0.4),
    600px 300px 0 0 rgba(255,255,255,0.5),
    50px 320px 0 0 rgba(200,168,255,0.4),
    250px 350px 0 0 rgba(255,255,255,0.6),
    500px 380px 0 0 rgba(201,168,76,0.4),
    680px 340px 0 0 rgba(255,255,255,0.5),
    180px 420px 0 0 rgba(200,168,255,0.4),
    450px 450px 0 0 rgba(255,255,255,0.5);
}

.hero-stars::after {
  content: '';
  position: absolute;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: rgba(201, 168, 76, 0.6);
  animation: star-twinkle 7s ease-in-out infinite 1s;
  box-shadow:
    150px 100px 0 0,
    400px 50px 0 0,
    600px 250px 0 0,
    80px 400px 0 0,
    500px 420px 0 0;
}

.hero-inner {
  position: relative;
  z-index: 2;
}

.hero-body {
  max-width: 800px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 14px;
  border-radius: 2px;
  background: rgba(255,255,255,0.06);
  color: rgba(255,255,255,0.85);
  font-family: var(--font-mono);
  font-size: 0.72rem;
  font-weight: 500;
  letter-spacing: 0.05em;
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
  font-family: var(--font-display);
  font-size: clamp(2.5rem, 5vw, 4rem);
  font-weight: 400;
  color: #fff;
  margin: 0 0 20px;
  line-height: 1.05;
  letter-spacing: -0.02em;
  text-wrap: balance;
}

.hero-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}

.meta-chip {
  padding: 6px 16px;
  border-radius: 2px;
  background: rgba(255,255,255,0.08);
  color: rgba(255,255,255,0.85);
  font-family: var(--font-mono);
  font-size: 0.72rem;
  font-weight: 500;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.meta-chip--gold {
  background: transparent;
  color: var(--c-gold);
  border: 1px solid rgba(201, 168, 76, 0.4);
}

.hero-info {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: rgba(255,255,255,0.65);
  font-size: 0.82rem;
}

.hero-type {
  display: inline-block;
  padding: 6px 16px;
  border-radius: 2px;
  border: 1px solid var(--c-accent);
  color: var(--c-accent);
  font-family: var(--font-mono);
  font-size: 0.72rem;
  font-weight: 600;
  letter-spacing: 0.05em;
}

/* ===== Animations ===== */
@keyframes nebula-drift {
  0% { transform: scale(1) translate(0, 0); opacity: 0.6; }
  50% { transform: scale(1.05) translate(-1%, 1%); opacity: 1; }
  100% { transform: scale(1) translate(1%, -1%); opacity: 0.6; }
}

@keyframes star-twinkle {
  0%, 100% { opacity: 0.2; }
  40% { opacity: 1; }
  70% { opacity: 0.2; }
}

/* ===== Detail Body ===== */
.detail-body {
  padding: 48px 0 64px;
}

.detail-layout {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 4rem;
  align-items: start;
}

/* ===== Main Content ===== */
.detail-main .content-section {
  background: var(--c-paper);
  border-radius: 0;
  padding: 2.5rem;
  box-shadow: none;
  border: none;
  margin-bottom: 2px;
  position: relative;
}

.detail-main .content-section::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 10%;
  right: 10%;
  height: 1px;
  background: var(--c-border-light);
}

.detail-main .content-section:last-child::after {
  display: none;
}

.description {
  line-height: 1.9;
  color: var(--c-ink);
  font-size: 0.95rem;
}

.description :deep(p) {
  margin-bottom: 1em;
}

.description :deep(img) {
  max-width: 100%;
  border-radius: 2px;
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
  font-family: var(--font-display);
  margin: 1.5em 0 0.5em;
}

/* ===== Registration Management ===== */
.registration-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.reg-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  background: var(--c-surface);
  border-radius: 2px;
  gap: 12px;
  border: 1px solid rgba(0,0,0,0.04);
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
  color: var(--c-ink);
}

.reg-type-tag {
  font-size: 0.7rem;
  padding: 2px 8px;
  border-radius: 2px;
  font-weight: 500;
  font-family: var(--font-mono);
}

.tag-personal { background: rgba(58,175,133,0.1); color: var(--c-success); }
.tag-team { background: rgba(91,127,165,0.1); color: var(--c-info); }

.reg-status {
  font-size: 0.78rem;
  font-weight: 500;
  font-family: var(--font-mono);
}

.status-pending { color: var(--c-warning); }
.status-approved { color: var(--c-success); }
.status-rejected { color: var(--c-danger); }

.reg-actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

/* ===== Sidebar ===== */
.sidebar-card {
  background: var(--c-surface);
  border-radius: 2px;
  padding: 28px;
  box-shadow:
    0 0 0 1px rgba(0,0,0,0.03),
    0 4px 12px rgba(0,0,0,0.04);
  margin-bottom: 20px;
}

.detail-sidebar .sidebar-card:first-child {
  position: sticky;
  top: 92px;
}

.sidebar-title {
  font-family: var(--font-display);
  font-size: 1.15rem;
  font-weight: 400;
  color: var(--c-primary);
  margin: 0 0 20px;
  padding-bottom: 14px;
  border-bottom: 2px solid var(--c-accent);
  width: 60%;
}

.sidebar-action {
  display: flex;
  flex-direction: column;
  gap: 12px;
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
  font-size: 0.82rem;
  font-weight: 600;
  padding: 8px 14px;
  border-radius: 2px;
  flex: 1;
  text-align: center;
  font-family: var(--font-mono);
  letter-spacing: 0.03em;
}

.reg-badge--pending {
  background: rgba(232, 168, 56, 0.1);
  color: var(--c-warning);
}

.reg-badge--approved {
  background: rgba(58, 175, 133, 0.1);
  color: var(--c-success);
}

.reg-badge--rejected {
  background: rgba(232, 93, 74, 0.1);
  color: var(--c-danger);
}

/* ===== Team Info ===== */
.team-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.team-select {
  width: 100%;
}

.team-select :deep(.el-select__wrapper) {
  border-radius: 2px !important;
}

.team-select :deep(.el-select__wrapper:hover) {
  border-color: var(--c-gold) !important;
}

/* ===== Sidebar Button Overrides ===== */
.sidebar-action .btn {
  border-radius: 2px !important;
  padding: 14px 20px !important;
  font-size: 0.82rem !important;
  letter-spacing: 0.03em;
}

.sidebar-action .btn-primary {
  background: var(--c-accent) !important;
  box-shadow: 0 4px 14px rgba(232, 93, 74, 0.2) !important;
}

.sidebar-action .btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(232, 93, 74, 0.3) !important;
}

.sidebar-action .btn-accent {
  background: transparent;
  color: var(--c-gold);
  border: 1px solid var(--c-gold);
}

.sidebar-action .btn-accent:hover {
  background: var(--c-gold);
  color: var(--c-primary-dark);
}

.sidebar-action .btn-danger {
  background: transparent;
  color: var(--c-danger);
  border: 1px solid var(--c-danger);
}

.sidebar-action .btn-danger:hover {
  background: var(--c-danger);
  color: #fff;
}

.sidebar-action .btn-secondary {
  background: var(--c-accent) !important;
  color: #fff !important;
  opacity: 0.85;
  font-size: 0.78rem !important;
}

.sidebar-action .btn-secondary:hover {
  opacity: 1;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(232, 93, 74, 0.2) !important;
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
  padding: 12px 0;
  border-bottom: 1px solid var(--c-border-light);
  font-size: 0.82rem;
}

.info-row:last-child {
  border-bottom: none;
}

.info-row dt {
  color: var(--c-text-muted);
  flex-shrink: 0;
  font-family: var(--font-mono);
  font-size: 0.72rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.info-row dd {
  color: var(--c-ink);
  font-weight: 500;
  text-align: right;
  word-break: break-word;
  max-width: 60%;
}

/* ===== Responsive ===== */
@media (min-width: 1024px) {
  .detail-sidebar .sidebar-card:first-child {
    position: sticky;
    top: 92px;
  }
}

@media (max-width: 900px) {
  .detail-layout {
    grid-template-columns: 1fr;
    gap: 2rem;
  }
}

@media (max-width: 768px) {
  .hero {
    padding: 40px 0 32px;
  }
  .hero-title {
    font-size: clamp(1.6rem, 6vw, 2.2rem);
  }
  .hero-info {
    gap: 12px;
  }
  .detail-main .content-section {
    padding: 1.5rem;
  }
  .detail-body {
    padding: 24px 0 40px;
  }
  .sidebar-card {
    padding: 20px;
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
