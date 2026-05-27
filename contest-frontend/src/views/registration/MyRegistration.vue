<template>
  <div class="page">
    <NavBar />
    <div class="container">
      <h2 class="section-title anim-fade-up">我的报名</h2>

      <div v-if="loading" class="loading-state anim-fade">
        <div class="loading-spinner" />
        <p>加载中...</p>
      </div>

      <template v-else>
        <div v-if="list.length === 0" class="empty-state anim-fade">
          <svg viewBox="0 0 120 120" fill="none">
            <rect x="20" y="30" width="80" height="60" rx="8" stroke="currentColor" stroke-width="2" />
            <path d="M40 50h40M40 62h30M40 74h20" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
          </svg>
          <p>暂无报名记录</p>
        </div>

        <div v-else class="reg-list">
          <div
            v-for="(item, index) in list"
            :key="item.id"
            class="reg-card anim-fade-up"
            :class="`anim-delay-${Math.min(index % 6 + 1, 6)}`"
          >
            <div class="reg-card__top">
              <div class="reg-card__info">
                <h3 class="reg-card__title">{{ item.contestName || '竞赛 #' + item.contestId }}</h3>
                <span class="reg-card__type">{{ item.regType === 0 ? '个人赛' : '团队赛' }}</span>
              </div>
              <span class="reg-badge" :class="`reg-badge--${statusType(item.status)}`">
                {{ statusLabel(item.status) }}
              </span>
            </div>
            <div class="reg-card__body">
              <div class="reg-card__meta">
                <span class="meta-label">报名时间</span>
                <span class="meta-value">{{ formatTime(item.createTime) }}</span>
              </div>
              <div v-if="item.reviewReason" class="reg-card__meta">
                <span class="meta-label">驳回原因</span>
                <span class="meta-value meta-value--danger">{{ item.reviewReason }}</span>
              </div>
            </div>
            <div v-if="item.status === 0" class="reg-card__actions">
              <button class="btn-cancel" @click="handleCancel(item)">取消报名</button>
            </div>
          </div>
        </div>

        <BasePagination :total="total" :page-size="size" :current-page="page" @change="pageChange" />
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import NavBar from '../../components/NavBar.vue'
import { pageRegistrationByUser, cancelRegistration } from '../../api/registration'
import { useUserStore } from '../../stores/user'

const store = useUserStore()
const list = ref([])
const total = ref(0)
import { formatTime } from '../../utils/format'

const page = ref(1)
const size = 10
const loading = ref(true)

const statusMap = {
  0: { label: '待审核', type: 'warning' },
  1: { label: '已通过', type: 'success' },
  2: { label: '已驳回', type: 'danger' },
  3: { label: '已取消', type: 'info' }
}
const statusLabel = s => statusMap[s]?.label || '未知'
const statusType = s => statusMap[s]?.type || 'info'

async function fetchData() {
  loading.value = true
  try {
    const res = await pageRegistrationByUser(store.userId, { page: page.value, size })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
  } finally {
    loading.value = false
  }
}

async function handleCancel(row) {
  try {
    const msg = row.regType === 1
      ? '确定要取消此团队报名吗？这将同时取消整个团队的报名记录。'
      : '确定要取消此报名吗？'
    await ElMessageBox.confirm(msg, '确认取消', {
      confirmButtonText: '确定',
      cancelButtonText: '返回',
      type: 'warning',
      roundButton: true,
    })
    await cancelRegistration(row.id, store.userId)
    ElMessage.success('已取消')
    fetchData()
  } catch {
  }
}

function pageChange(p) {
  page.value = p
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.reg-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.reg-card {
  background: var(--c-surface);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--c-border-light);
  overflow: hidden;
  transition: var(--transition);
}
.reg-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.reg-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px 0;
}

.reg-card__info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.reg-card__title {
  font-family: 'DM Serif Display', Georgia, serif;
  font-size: 1.1rem;
  color: var(--c-primary);
  margin: 0;
}

.reg-card__type {
  font-size: 0.8rem;
  color: var(--c-text-muted);
  background: var(--c-bg);
  padding: 2px 12px;
  border-radius: 20px;
  font-weight: 500;
}

.reg-badge {
  font-size: 0.78rem;
  font-weight: 600;
  padding: 4px 14px;
  border-radius: 20px;
  letter-spacing: 0.02em;
}
.reg-badge--warning { background: #fef3e2; color: #b8860b; }
.reg-badge--success { background: #e3f5ed; color: #1f8b5c; }
.reg-badge--danger { background: #fde8e5; color: #c0392b; }
.reg-badge--info { background: #eef0f2; color: #6b7280; }

.reg-card__body {
  padding: 16px 24px 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.reg-card__meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.88rem;
}

.meta-label {
  color: var(--c-text-muted);
  min-width: 64px;
}

.meta-value {
  color: var(--c-text);
}
.meta-value--danger {
  color: var(--c-danger);
}

.reg-card__actions {
  padding: 12px 24px;
  border-top: 1px solid var(--c-border-light);
  margin-top: 12px;
}

.btn-cancel {
  background: transparent;
  border: 1.5px solid var(--c-danger);
  color: var(--c-danger);
  font-family: 'DM Sans', sans-serif;
  font-size: 0.82rem;
  font-weight: 600;
  padding: 6px 18px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: var(--transition);
}
.btn-cancel:hover {
  background: var(--c-danger);
  color: #fff;
}

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

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
</style>
