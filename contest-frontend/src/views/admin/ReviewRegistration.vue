<template>
  <div class="rr-page">
    <NavBar />
    <div class="rr-body">
      <div class="rr-header">
        <div>
          <h1 class="rr-title">审核管理</h1>
          <p class="rr-subtitle">审核报名申请与团队</p>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="rr-tabs" @tab-change="tabChange">
        <el-tab-pane label="报名审核" name="reg">
          <template #label>
            <span class="tab-label">报名审核</span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="团队审核" name="team">
          <template #label>
            <span class="tab-label">团队审核</span>
          </template>
        </el-tab-pane>
      </el-tabs>

      <!-- Registration Review -->
      <template v-if="activeTab === 'reg'">
        <div class="rr-toolbar">
          <div class="rr-filter-group">
            <div class="rr-filter-item">
              <label class="rr-filter-label">竞赛</label>
              <el-select v-model="contestId" filterable placeholder="全部竞赛" clearable class="rr-select" @change="regPage=1; fetchReg()">
                <el-option v-for="c in contests" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </div>
            <div class="rr-filter-item">
              <label class="rr-filter-label">状态</label>
              <el-select v-model="regStatus" placeholder="全部状态" clearable class="rr-select rr-select--sm" @change="regPage=1; fetchReg()">
                <el-option label="待审核" :value="0" />
                <el-option label="已通过" :value="1" />
                <el-option label="已驳回" :value="2" />
                <el-option label="已取消" :value="3" />
              </el-select>
            </div>
          </div>
          <div class="rr-stats">{{ regTotal }} 条记录</div>
        </div>

        <div class="rr-card">
          <el-table :data="regList" v-loading="regLoading" element-loading-background="rgba(245,243,239,0.8)" empty-text="暂无报名记录" stripe height="100%">
            <el-table-column prop="id" label="ID" width="50" />
            <el-table-column label="竞赛名称" min-width="180">
              <template #default="{ row }">
                <span class="rr-cell-name">{{ row.contestName || `竞赛 #${row.contestId}` }}</span>
              </template>
            </el-table-column>
            <el-table-column label="用户" width="120">
              <template #default="{ row }">
                <span class="rr-cell-name">{{ row.userName || `用户 #${row.userId}` }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="regType" label="类型" width="64" align="center">
              <template #default="{ row }">
                <span :class="['rr-badge', row.regType === 0 ? 'rr-badge--personal' : 'rr-badge--team']">{{ row.regType === 0 ? '个人' : '团队' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80" align="center">
              <template #default="{ row }">
                <span :class="['rr-status', `rr-status--${regSType(row.status)}`]">{{ regSLabel(row.status) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="报名时间" width="140">
              <template #default="{ row }">
                <span class="rr-cell-time">{{ fmtTime(row.createTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="170" align="right" fixed="right">
              <template #default="{ row }">
                <template v-if="row.status === 0">
                  <button class="rr-btn rr-btn--approve" @click="approveReg(row.id)">通过</button>
                  <button class="rr-btn rr-btn--reject" @click="showRejectReg(row)">驳回</button>
                </template>
                <span v-else class="rr-done">-</span>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="rr-footer">
          <el-pagination v-if="regTotal > 0" background layout="total, prev, pager, next" :total="regTotal" :page-size="size" :current-page="regPage" @current-change="p=>{regPage=p; fetchReg()}" small />
        </div>
      </template>

      <!-- Team Review -->
      <template v-if="activeTab === 'team'">
        <div class="rr-toolbar">
          <div class="rr-filter-group">
            <div class="rr-filter-item">
              <label class="rr-filter-label">状态</label>
              <el-select v-model="teamStatus" placeholder="全部状态" clearable class="rr-select rr-select--sm" @change="teamPage=1; fetchTeam()">
                <el-option label="组建中" :value="0" />
                <el-option label="待审核" :value="1" />
                <el-option label="已通过" :value="2" />
                <el-option label="已驳回" :value="3" />
                <el-option label="已解散" :value="4" />
              </el-select>
            </div>
          </div>
          <div class="rr-stats">{{ teamTotal }} 条记录</div>
        </div>

        <div class="rr-card">
          <el-table :data="teamList" v-loading="teamLoading" element-loading-background="rgba(245,243,239,0.8)" empty-text="暂无团队" stripe height="100%">
            <el-table-column prop="id" label="ID" width="50" />
            <el-table-column prop="teamName" label="团队名称" min-width="180">
              <template #default="{ row }">
                <span class="rr-cell-name">{{ row.teamName }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="teamNo" label="编号" width="130" />
            <el-table-column label="状态" width="80" align="center">
              <template #default="{ row }">
                <span :class="['rr-status', `rr-status--${teamSType(row.status)}`]">{{ teamSLabel(row.status) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="memberCount" label="人数" width="60" align="center" />
            <el-table-column label="创建时间" width="140">
              <template #default="{ row }">
                <span class="rr-cell-time">{{ fmtTime(row.createTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="170" align="right" fixed="right">
              <template #default="{ row }">
                <template v-if="row.status === 1">
                  <button class="rr-btn rr-btn--approve" @click="approveTeam(row.id)">通过</button>
                  <button class="rr-btn rr-btn--reject" @click="showRejectTeam(row)">驳回</button>
                </template>
                <span v-else class="rr-done">-</span>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="rr-footer">
          <el-pagination v-if="teamTotal > 0" background layout="total, prev, pager, next" :total="teamTotal" :page-size="size" :current-page="teamPage" @current-change="p=>{teamPage=p; fetchTeam()}" small />
        </div>
      </template>
    </div>

    <el-dialog v-model="rejectVisible" :title="rejectTitle" width="400px" class="rr-dialog" :close-on-click-modal="false">
      <div class="rr-reject-form">
        <p class="rr-reject-hint">请说明驳回原因，不少于 5 个字符</p>
        <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="输入驳回原因..." />
      </div>
      <template #footer>
        <button class="rr-dialog-btn" @click="rejectVisible = false">取消</button>
        <button class="rr-dialog-btn rr-dialog-btn--danger" @click="confirmReject">确认驳回</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import NavBar from '../../components/NavBar.vue'
import { pageContests } from '../../api/contest'
import { pageRegistration, approveRegistration, rejectRegistration } from '../../api/registration'
import { pageTeams, adminApproveTeam, adminRejectTeam } from '../../api/team'

const activeTab = ref('reg')

const contests = ref([])

// Registration
const contestId = ref(null)
const regStatus = ref(null)
const regList = ref([])
const regTotal = ref(0)
const regPage = ref(1)
const regLoading = ref(true)

// Team
const teamStatus = ref(null)
const teamList = ref([])
const teamTotal = ref(0)
const teamPage = ref(1)
const teamLoading = ref(true)

const size = 20
const rejectVisible = ref(false)
const rejectReason = ref('')
const rejectTarget = ref(null) // { type: 'reg'|'team', id }
const rejectTitle = ref('')

const regSMap = { 0: '待审核', 1: '已通过', 2: '已驳回', 3: '已取消' }
const regSTypeMap = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
const regSLabel = s => regSMap[s] || ''
const regSType = s => regSTypeMap[s] || 'info'

const teamSMap = { 0: '组建中', 1: '待审核', 2: '已通过', 3: '已驳回', 4: '已解散' }
const teamSTypeMap = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger', 4: 'info' }
const teamSLabel = s => teamSMap[s] || ''
const teamSType = s => teamSTypeMap[s] || 'info'

function fmtTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ')
}

async function loadContests() {
  try {
    const res = await pageContests({ page: 1, size: 200 })
    contests.value = res.data.records || []
  } catch (e) { /* ignore */ }
}

// Registration
async function fetchReg() {
  regLoading.value = true
  try {
    const res = await pageRegistration({ contestId: contestId.value, status: regStatus.value, page: regPage.value, size })
    regList.value = res.data.records || []
    regTotal.value = res.data.total || 0
  } catch (e) { /* ignore */ } finally { regLoading.value = false }
}

async function approveReg(id) {
  try {
    await approveRegistration(id)
    ElMessage.success('已通过')
    fetchReg()
  } catch (e) { /* handled by axios interceptor */ }
}

function showRejectTeam(row) {
  rejectTarget.value = { type: 'team', id: row.id }
  rejectTitle.value = '驳回原因'
  rejectReason.value = ''
  rejectVisible.value = true
}

// Common reject
async function confirmReject() {
  if (rejectReason.value.length < 5) { ElMessage.warning('驳回原因不少于5个字符'); return }
  try {
    if (rejectTarget.value.type === 'reg') {
      await rejectRegistration(rejectTarget.value.id, rejectReason.value)
    } else {
      await adminRejectTeam(rejectTarget.value.id, rejectReason.value)
    }
    ElMessage.success('已驳回')
    rejectVisible.value = false
    if (rejectTarget.value.type === 'reg') fetchReg(); else fetchTeam()
  } catch (e) { /* handled by axios interceptor */ }
}

function showRejectReg(row) {
  rejectTarget.value = { type: 'reg', id: row.id }
  rejectTitle.value = '驳回原因'
  rejectReason.value = ''
  rejectVisible.value = true
}

// Team
async function fetchTeam() {
  teamLoading.value = true
  try {
    const res = await pageTeams({ status: teamStatus.value, page: teamPage.value, size })
    teamList.value = res.data.records || []
    teamTotal.value = res.data.total || 0
  } catch (e) { /* ignore */ } finally { teamLoading.value = false }
}

async function approveTeam(id) {
  await adminApproveTeam(id)
  ElMessage.success('已通过，已自动创建报名记录')
  fetchTeam()
}

function tabChange() {
  if (activeTab.value === 'reg') fetchReg()
  else fetchTeam()
}

onMounted(async () => {
  await loadContests()
  await fetchReg()
  teamLoading.value = false
})
</script>

<style scoped>
.rr-page { height: 100vh; display: flex; flex-direction: column; background: #f5f3ef; font-family: 'DM Sans', sans-serif; overflow: hidden; }
.rr-body { flex: 1; display: flex; flex-direction: column; padding: 24px 32px 16px; padding-top: calc(72px + 20px); max-width: 1400px; width: 100%; margin: 0 auto; min-height: 0; }
.rr-header { margin-bottom: 12px; flex-shrink: 0; }
.rr-title { font-family: 'DM Serif Display', serif; font-size: 28px; color: #1a2332; margin: 0 0 4px; }
.rr-subtitle { font-size: 13px; color: #1a2332; opacity: 0.5; margin: 0; }

.rr-tabs { flex-shrink: 0; margin-bottom: 12px; }
.rr-tabs :deep(.el-tabs__header) { margin: 0; border-bottom: 1px solid #e0ddd7; }
.rr-tabs :deep(.el-tabs__item) { font-size: 14px; font-weight: 500; color: #1a2332; height: 40px; line-height: 40px; }
.rr-tabs :deep(.el-tabs__item.is-active) { color: #1a2332; font-weight: 600; }
.rr-tabs :deep(.el-tabs__active-bar) { background: #1a2332; }
.tab-label { display: inline-flex; align-items: center; gap: 6px; }

.rr-toolbar { display: flex; align-items: flex-end; justify-content: space-between; margin-bottom: 10px; gap: 16px; flex-shrink: 0; }
.rr-filter-group { display: flex; align-items: flex-end; gap: 12px; flex-wrap: wrap; }
.rr-filter-item { display: flex; flex-direction: column; gap: 3px; }
.rr-filter-label { font-size: 11px; font-weight: 600; color: #1a2332; text-transform: uppercase; letter-spacing: 0.04em; }
.rr-select { width: 240px; }
.rr-select--sm { width: 110px; }
.rr-select :deep(.el-input__wrapper) { border-radius: 8px; border: 1px solid #e0ddd7; box-shadow: none; background: #fff; height: 34px; }
.rr-select :deep(.el-input__wrapper:hover) { border-color: #1a2332; }
.rr-select :deep(.el-input__wrapper.is-focus) { border-color: #1a2332; box-shadow: 0 0 0 2px rgba(26,35,50,0.08); }
.rr-stats { font-size: 13px; color: #1a2332; opacity: 0.4; white-space: nowrap; flex-shrink: 0; }

.rr-card { flex: 1; background: #fff; border-radius: 12px; border: 1px solid #edebe7; box-shadow: 0 1px 3px rgba(26,35,50,0.06); display: flex; flex-direction: column; min-height: 0; overflow: hidden; }
.rr-card :deep(.el-table) { flex: 1; border: none; width: 100%; }
.rr-card :deep(.el-table__body-wrapper) { flex: 1; overflow-y: auto; }
.rr-card :deep(.el-table th.el-table__cell) { background: #f5f3ef; color: #1a2332; font-weight: 600; font-size: 11px; letter-spacing: 0.03em; text-transform: uppercase; border-bottom: none; padding: 0; }
.rr-card :deep(.el-table th.el-table__cell > .cell) { padding: 10px 14px; }
.rr-card :deep(.el-table td.el-table__cell) { border-bottom: 1px solid #f0eeea; padding: 8px 0; }
.rr-card :deep(.el-table::before) { display: none; }
.rr-card :deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) { background: #faf9f7; }
.rr-card :deep(.el-table__inner-wrapper) { flex: 1; display: flex; flex-direction: column; }
.rr-card :deep(.el-table__header-wrapper) { flex-shrink: 0; }

.rr-cell-name { font-weight: 500; color: #1a2332; font-size: 13px; }
.rr-cell-time { color: #1a2332; font-size: 13px; }

.rr-badge { display: inline-block; padding: 1px 10px; border-radius: 5px; font-size: 11px; font-weight: 500; }
.rr-badge--personal { background: rgba(26,35,50,0.06); color: #1a2332; }
.rr-badge--team { background: rgba(232,93,74,0.1); color: #e85d4a; }

.rr-status { display: inline-block; padding: 2px 12px; border-radius: 20px; font-size: 11px; font-weight: 500; }
.rr-status--warning { background: rgba(196,137,32,0.1); color: #c48920; }
.rr-status--success { background: rgba(26,122,90,0.1); color: #1a7a5a; }
.rr-status--danger { background: rgba(232,93,74,0.1); color: #e85d4a; }
.rr-status--info { background: rgba(26,35,50,0.06); color: #1a2332; opacity: 0.5; }

.rr-btn { height: 30px; padding: 0 14px; border-radius: 7px; border: 1px solid transparent; font-family: 'DM Sans', sans-serif; font-size: 12px; font-weight: 500; cursor: pointer; transition: all 0.15s ease; display: inline-flex; align-items: center; gap: 3px; }
.rr-btn--approve { background: rgba(26,122,90,0.08); border-color: rgba(26,122,90,0.25); color: #1a7a5a; }
.rr-btn--approve:hover { background: #1a7a5a; border-color: #1a7a5a; color: #fff; }
.rr-btn--reject { background: rgba(232,93,74,0.08); border-color: rgba(232,93,74,0.25); color: #e85d4a; }
.rr-btn--reject:hover { background: #e85d4a; border-color: #e85d4a; color: #fff; }
.rr-done { color: #1a2332; opacity: 0.2; font-size: 13px; }

.rr-footer { flex-shrink: 0; display: flex; justify-content: center; padding-top: 10px; }
.rr-footer :deep(.el-pagination.is-background .btn-prev),
.rr-footer :deep(.el-pagination.is-background .btn-next),
.rr-footer :deep(.el-pagination.is-background .el-pager li) { background: #fff; border: 1px solid #e0ddd7; border-radius: 6px; margin: 0 2px; color: #1a2332; min-width: 28px; height: 28px; line-height: 28px; }
.rr-footer :deep(.el-pagination.is-background .el-pager li.is-active) { background: #1a2332; border-color: #1a2332; color: #fff; }

.rr-dialog :deep(.el-dialog) { border-radius: 14px; overflow: hidden; box-shadow: 0 20px 60px rgba(26,35,50,0.15); }
.rr-dialog :deep(.el-dialog__header) { background: #f5f3ef; padding: 20px 24px 12px; margin: 0; }
.rr-dialog :deep(.el-dialog__title) { font-family: 'DM Serif Display', serif; font-size: 18px; color: #1a2332; }
.rr-dialog :deep(.el-dialog__body) { padding: 24px; }
.rr-dialog :deep(.el-dialog__footer) { padding: 0 24px 20px; border-top: none; display: flex; justify-content: flex-end; gap: 8px; }
.rr-reject-form { display: flex; flex-direction: column; gap: 10px; }
.rr-reject-hint { margin: 0; font-size: 13px; color: #1a2332; opacity: 0.6; }
.rr-reject-form :deep(.el-textarea__inner) { border-radius: 8px; border: 1px solid #e0ddd7; box-shadow: none; resize: vertical; }
.rr-reject-form :deep(.el-textarea__inner:hover) { border-color: #1a2332; }
.rr-reject-form :deep(.el-textarea__inner:focus) { border-color: #1a2332; box-shadow: 0 0 0 2px rgba(26,35,50,0.08); }
.rr-dialog-btn { height: 36px; padding: 0 20px; border-radius: 8px; font-family: 'DM Sans', sans-serif; font-size: 13px; font-weight: 500; cursor: pointer; transition: all 0.15s ease; border: 1px solid #e0ddd7; background: #fff; color: #1a2332; }
.rr-dialog-btn:hover { border-color: #1a2332; }
.rr-dialog-btn--danger { background: #e85d4a; border-color: #e85d4a; color: #fff; }
.rr-dialog-btn--danger:hover { background: #d04a38; border-color: #d04a38; }

@media (max-width: 768px) {
  .rr-body { padding: calc(72px + 16px) 16px 12px; }
  .rr-toolbar { flex-direction: column; align-items: stretch; }
  .rr-filter-group { flex-direction: column; }
  .rr-select, .rr-select--sm { width: 100%; }
}
</style>
