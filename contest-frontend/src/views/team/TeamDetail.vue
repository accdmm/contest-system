<template>
  <div class="page">
    <NavBar />
    <div v-loading="loading" class="detail-wrap">
      <div class="container" v-if="team">
        <div class="detail-layout">
          <div class="detail-main">
            <div class="team-header anim-fade-up anim-delay-1">
              <div class="header-top">
                <div class="team-identity">
                  <div class="team-avatar">
                    {{ team.teamName?.charAt(0) }}
                  </div>
                  <div class="team-info">
                    <div class="team-name-row">
                      <h1 class="team-name">{{ team.teamName }}</h1>
                      <el-tag
                        :type="teamStatusType"
                        size="small"
                        class="status-tag"
                      >
                        {{ teamStatusLabel }}
                      </el-tag>
                    </div>
                    <div class="team-meta">
                      <span class="meta-item">
                        <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                          <rect x="1" y="2" width="12" height="10" rx="2" stroke="currentColor" stroke-width="1.2"/>
                          <path d="M1 4H13" stroke="currentColor" stroke-width="1.2"/>
                        </svg>
                        {{ team.teamNo }}
                      </span>
                      <span class="meta-divider" />
                      <span class="meta-item">
                        <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                          <circle cx="7" cy="7" r="6" stroke="currentColor" stroke-width="1.2"/>
                          <path d="M7 4V7L9 9" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
                        </svg>
                        {{ team.contestName || '未知竞赛' }}
                      </span>
                      <span class="meta-divider" />
                      <span class="meta-item">
                        <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                          <path d="M5 1L7 3L9 1" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
                          <path d="M7 3V13" stroke="currentColor" stroke-width="1.2"/>
                          <path d="M2 10C2 10 3.5 12 7 12C10.5 12 12 10 12 10" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
                        </svg>
                        {{ approvedCount }} 名成员
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              <div class="header-actions" v-if="isLeader">
                <el-button
                  v-if="team.status === 0"
                  type="primary"
                  size="large"
                  class="action-btn-primary"
                  @click="submitReview"
                >
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none" style="margin-right:6px">
                    <path d="M8 2V14M2 8H14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                  提交报名审核
                </el-button>
                <el-button
                  v-if="team.status === 0"
                  size="large"
                  class="action-btn-danger"
                  @click="dissolve"
                >
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none" style="margin-right:6px">
                    <path d="M4 4L12 12M12 4L4 12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                  解散团队
                </el-button>
                <el-button
                  v-if="team.status === 1"
                  disabled
                  size="large"
                  class="action-btn-disabled"
                >
                  已提交审核
                </el-button>
              </div>
              <div v-else class="header-actions">
                <el-button
                  size="large"
                  class="action-btn-danger"
                  @click="handleLeave"
                >
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none" style="margin-right:6px">
                    <path d="M6 2L2 6V14H14V6L10 2H6Z" stroke="currentColor" stroke-width="1.3" fill="none"/>
                    <path d="M6 8H10" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
                    <path d="M8 6V10" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
                  </svg>
                  退出团队
                </el-button>
              </div>
            </div>

            <div class="content-grid">
              <div class="content-section anim-fade-up anim-delay-2">
                <div class="section-head">
                  <h2 class="section-title-inner">团队成员</h2>
                  <span class="section-count">{{ approvedCount + pendingCount }}</span>
                  <span class="section-count-sub">已通过 {{ approvedCount }}，待审核 {{ pendingCount }}</span>
                </div>

                <div v-if="members.length === 0" class="empty-state">
                  <p>暂无成员</p>
                </div>

                <div v-else class="member-list">
                  <div
                    v-for="m in members"
                    :key="m.id"
                    class="member-card"
                  >
                    <div class="member-avatar" :style="{ background: m.role === 1 ? 'var(--c-accent)' : 'var(--c-primary)' }">
                      {{ m.userName?.charAt(0) || m.userId?.toString().charAt(0) || '?' }}
                    </div>
                    <div class="member-body">
                      <div class="member-name-row">
                        <span class="member-name">{{ m.userName || `用户 ${m.userId}` }}</span>
                        <el-tag
                          v-if="m.role === 1"
                          size="small"
                          class="role-tag role-leader"
                        >
                          队长
                        </el-tag>
                        <el-tag
                          v-else
                          size="small"
                          class="role-tag role-member"
                        >
                          成员
                        </el-tag>
                        <el-tag
                          v-if="m.status === 0"
                          size="small"
                          type="warning"
                          class="pending-tag"
                        >
                          待审核
                        </el-tag>
                        <el-tag
                          v-if="m.status === 2"
                          size="small"
                          type="danger"
                          class="pending-tag"
                        >
                          已拒绝
                        </el-tag>
                      </div>
                      <div class="member-status">
                        <span class="status-text">{{ memberStatusMap[m.status] }}</span>
                      </div>
                    </div>
                    <div class="member-actions" v-if="isLeader">
                      <el-button
                        v-if="m.status === 0"
                        size="small"
                        class="approve-btn"
                        @click="approve(m.id)"
                      >
                        通过
                      </el-button>
                      <el-button
                        v-if="m.status === 0"
                        size="small"
                        class="reject-btn"
                        @click="reject(m.id)"
                      >
                        拒绝
                      </el-button>
                      <el-button
                        v-if="m.status === 1 && m.role !== 1"
                        size="small"
                        class="remove-btn"
                        @click="remove(m.id)"
                      >
                        <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                          <path d="M3 3L11 11M11 3L3 11" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
                        </svg>
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>

              <div class="content-section anim-fade-up anim-delay-3">
                <h2 class="section-title-inner">邀请码</h2>
                <div class="invite-card">
                  <div class="invite-display" v-if="team.inviteCode">
                    <div class="invite-code">{{ team.inviteCode }}</div>
                    <el-button
                      size="small"
                      class="copy-btn"
                      @click="copyInviteCode"
                    >
                      <svg width="14" height="14" viewBox="0 0 14 14" fill="none" style="margin-right:4px">
                        <rect x="2" y="4" width="9" height="9" rx="1.5" stroke="currentColor" stroke-width="1.2"/>
                        <path d="M4 4V3C4 2 4 1 5.5 1H11C12 1 13 1 13 2.5V8C13 9.5 12 9.5 11 9.5H10" stroke="currentColor" stroke-width="1.2"/>
                      </svg>
                      复制
                    </el-button>
                  </div>
                  <div v-else class="invite-empty">
                    <p>尚未生成邀请码</p>
                  </div>
                  <div class="invite-action" v-if="isLeader">
                    <el-button
                      size="default"
                      class="generate-btn"
                      @click="generateCode"
                    >
                      <svg width="14" height="14" viewBox="0 0 14 14" fill="none" style="margin-right:4px">
                        <path d="M7 1V13M1 7H13" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                      </svg>
                      {{ team.inviteCode ? '重新生成' : '生成邀请码' }}
                    </el-button>
                  </div>
                </div>
              </div>

              <div class="content-section anim-fade-up anim-delay-4">
                <h2 class="section-title-inner">加入团队</h2>
                <div class="join-card">
                  <p class="join-desc">已有邀请码？输入后申请加入此团队</p>
                  <div class="join-input-row">
                    <el-input
                      v-model="inviteCode"
                      placeholder="输入邀请码"
                      class="join-input"
                      size="large"
                      @keyup.enter="joinTeam"
                    >
                      <template #prefix>
                        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                          <rect x="3" y="3" width="10" height="10" rx="2" stroke="currentColor" stroke-width="1.3"/>
                          <path d="M6 8H10M8 6V10" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
                        </svg>
                      </template>
                    </el-input>
                    <el-button
                      type="primary"
                      size="large"
                      class="join-btn"
                      @click="joinTeam"
                    >
                      申请加入
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import NavBar from '../../components/NavBar.vue'
import { getTeamById, listTeamMembers, listPendingMembers, generateInviteCode, approveMember, rejectMember, removeMember, dissolveTeam, submitTeamReview, joinByInviteCode, leaveTeam } from '../../api/team'
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
const approvedCount = computed(() => members.value.filter(m => m.status === 1).length)
const pendingCount = computed(() => members.value.filter(m => m.status === 0).length)

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

async function copyInviteCode() {
  try {
    await navigator.clipboard.writeText(team.value.inviteCode)
    ElMessage.success('邀请码已复制')
  } catch {
    ElMessage.warning('复制失败，请手动复制')
  }
}

async function approve(id) { await approveMember(team.value.id, store.userId, id); ElMessage.success('已通过'); fetchData() }
async function reject(id) { await rejectMember(team.value.id, store.userId, id); ElMessage.success('已拒绝'); fetchData() }
async function remove(id) { await removeMember(team.value.id, store.userId, id); ElMessage.success('已移除'); fetchData() }

async function dissolve() {
  await dissolveTeam(team.value.id, store.userId)
  ElMessage.success('团队已解散')
  fetchData()
}

async function handleLeave() {
  await leaveTeam(team.value.id, store.userId)
  ElMessage.success('已退出团队')
  router.push('/home')
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
.detail-wrap {
  padding: 40px 0;
  min-height: calc(100vh - 72px);
}

.detail-layout {
  max-width: 800px;
  margin: 0 auto;
}

/* ===== Team Header ===== */
.team-header {
  background: var(--c-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  padding: 36px;
  margin-bottom: 28px;
  border: 1px solid var(--c-border-light);
}

.header-top {
  margin-bottom: 20px;
}

.team-identity {
  display: flex;
  align-items: center;
  gap: 20px;
}

.team-avatar {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-sm);
  background: var(--c-primary);
  color: #fff;
  font-family: 'DM Serif Display', Georgia, serif;
  font-size: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.team-info {
  flex: 1;
  min-width: 0;
}

.team-name-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.team-name {
  font-family: 'DM Serif Display', Georgia, serif;
  font-size: 1.5rem;
  color: var(--c-primary);
  margin: 0;
}

.status-tag {
  flex-shrink: 0;
}

.team-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.85rem;
  color: var(--c-text-muted);
}

.meta-divider {
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: var(--c-border);
}

.header-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  padding-top: 20px;
  border-top: 1px solid var(--c-border-light);
}

.action-btn-primary {
  background: var(--c-accent) !important;
  box-shadow: 0 4px 14px rgba(232, 93, 74, 0.25) !important;
}

.action-btn-primary:hover {
  background: var(--c-accent-light) !important;
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(232, 93, 74, 0.35) !important;
}

.action-btn-danger {
  color: var(--c-danger) !important;
  border: 1.5px solid var(--c-danger) !important;
  background: transparent !important;
}

.action-btn-danger:hover {
  background: rgba(232, 93, 74, 0.06) !important;
  transform: translateY(-1px);
}

.action-btn-disabled {
  opacity: 0.7;
}

/* ===== Content Grid ===== */
.content-grid {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.content-section {
  background: var(--c-surface);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  padding: 28px;
  border: 1px solid var(--c-border-light);
}

.section-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.section-title-inner {
  font-family: 'DM Serif Display', Georgia, serif;
  font-size: 1.15rem;
  color: var(--c-primary);
  margin: 0;
}

.section-count {
  background: var(--c-border-light);
  color: var(--c-text-muted);
  font-size: 0.78rem;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 20px;
}

.section-count-sub {
  color: var(--c-text-light);
  font-size: 0.78rem;
  margin-left: 4px;
}

/* ===== Member List ===== */
.member-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.member-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--c-border-light);
  transition: var(--transition);
}

.member-card:hover {
  border-color: var(--c-border);
  background: #faf9f7;
}

.member-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  font-size: 0.85rem;
  flex-shrink: 0;
}

.member-body {
  flex: 1;
  min-width: 0;
}

.member-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 2px;
}

.member-name {
  font-size: 0.92rem;
  font-weight: 600;
  color: var(--c-primary);
}

.role-tag {
  border-radius: 12px !important;
  font-size: 0.72rem !important;
  padding: 0 10px !important;
  height: 22px !important;
}

.role-leader {
  background: rgba(232, 93, 74, 0.12) !important;
  color: var(--c-accent) !important;
}

.role-member {
  background: rgba(26, 35, 50, 0.08) !important;
  color: var(--c-primary) !important;
}

.pending-tag {
  font-size: 0.72rem !important;
  height: 22px !important;
  line-height: 22px !important;
}

.member-status {
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-text {
  font-size: 0.8rem;
  color: var(--c-text-muted);
}

.member-actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

.approve-btn {
  background: rgba(58, 175, 133, 0.1) !important;
  color: var(--c-success) !important;
  font-size: 0.8rem !important;
  padding: 6px 14px !important;
  height: 32px !important;
}

.approve-btn:hover {
  background: var(--c-success) !important;
  color: #fff !important;
}

.reject-btn {
  background: rgba(232, 93, 74, 0.1) !important;
  color: var(--c-danger) !important;
  font-size: 0.8rem !important;
  padding: 6px 14px !important;
  height: 32px !important;
}

.reject-btn:hover {
  background: var(--c-danger) !important;
  color: #fff !important;
}

.remove-btn {
  width: 32px !important;
  height: 32px !important;
  padding: 0 !important;
  display: flex !important;
  align-items: center;
  justify-content: center;
  background: transparent !important;
  color: var(--c-text-light) !important;
  border: 1px solid var(--c-border) !important;
}

.remove-btn:hover {
  color: var(--c-danger) !important;
  border-color: var(--c-danger) !important;
  background: rgba(232, 93, 74, 0.06) !important;
}

/* ===== Invite Code ===== */
.invite-card {
  padding: 0;
}

.invite-display {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 16px 20px;
  background: var(--c-bg);
  border-radius: var(--radius-sm);
  border: 1px dashed var(--c-border);
}

.invite-code {
  font-family: 'DM Serif Display', Georgia, serif;
  font-size: 1.3rem;
  letter-spacing: 3px;
  color: var(--c-primary);
  flex: 1;
  text-align: center;
}

.copy-btn {
  background: var(--c-surface) !important;
  border: 1px solid var(--c-border) !important;
  font-size: 0.8rem !important;
  flex-shrink: 0;
}

.copy-btn:hover {
  border-color: var(--c-primary) !important;
  color: var(--c-primary) !important;
}

.invite-empty {
  padding: 16px 20px;
  text-align: center;
  color: var(--c-text-muted);
  font-size: 0.88rem;
  margin-bottom: 16px;
  background: var(--c-bg);
  border-radius: var(--radius-sm);
}

.generate-btn {
  width: 100%;
  background: transparent !important;
  border: 1.5px dashed var(--c-border) !important;
  color: var(--c-text-muted) !important;
  height: 42px !important;
}

.generate-btn:hover {
  border-color: var(--c-primary-light) !important;
  color: var(--c-primary) !important;
  background: rgba(26, 35, 50, 0.02) !important;
}

/* ===== Join Section ===== */
.join-card {
  padding: 0;
}

.join-desc {
  font-size: 0.85rem;
  color: var(--c-text-muted);
  margin-bottom: 14px;
}

.join-input-row {
  display: flex;
  gap: 10px;
}

.join-input {
  flex: 1;
}

.join-input :deep(.el-input__prefix) {
  color: var(--c-text-light);
}

.join-btn {
  min-width: 130px;
  height: 46px !important;
  background: var(--c-primary) !important;
  box-shadow: 0 4px 14px rgba(26, 35, 50, 0.2) !important;
}

.join-btn:hover {
  background: var(--c-primary-light) !important;
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(26, 35, 50, 0.3) !important;
}

@media (max-width: 640px) {
  .team-header {
    padding: 24px;
  }

  .team-identity {
    flex-direction: column;
    align-items: flex-start;
  }

  .team-name-row {
    flex-wrap: wrap;
  }

  .header-actions {
    flex-direction: column;
  }

  .header-actions .el-button {
    width: 100%;
  }

  .member-card {
    flex-wrap: wrap;
  }

  .member-actions {
    width: 100%;
    justify-content: flex-end;
    padding-top: 8px;
    border-top: 1px solid var(--c-border-light);
  }

  .join-input-row {
    flex-direction: column;
  }

  .join-btn {
    width: 100%;
  }

  .content-section {
    padding: 20px;
  }
}
</style>
