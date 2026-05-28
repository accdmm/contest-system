<template>
  <div class="contest-card" @click="goDetail">
    <div class="contest-card__cover" :style="coverStyle">
      <div class="contest-card__badge" :class="`contest-card__badge--${statusType}`">
        <span class="contest-card__dot" />
        {{ statusLabel }}
      </div>
      <span class="contest-card__category-tag">{{ contest.category }}</span>
    </div>

    <div class="contest-card__body">
      <h3 class="contest-card__title">{{ contest.name }}</h3>

      <div class="contest-card__meta">
        <el-tag size="small" class="contest-card__tag">{{ contest.category }}</el-tag>
        <el-tag size="small" v-if="contest.level" class="contest-card__tag contest-card__tag--level">
            {{ levelLabel }}
        </el-tag>
      </div>

      <div class="contest-card__info">
        <div class="contest-card__info-item">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
            <path d="M7 1C4.24 1 2 3.24 2 6s2 5 5 5 5-2.24 5-5-2.24-5-5-5zM6 3.5v3L9 8" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>截止 {{ formatDate(contest.registerEndTime) }}</span>
        </div>
        <div class="contest-card__info-item">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
            <path d="M7 7a2.5 2.5 0 100-5 2.5 2.5 0 000 5zm0 1.5c-3.315 0-5 1.343-5 3v.5h10v-.5c0-1.657-1.685-3-5-3z" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>{{ contest.currentCount || 0 }} 人已报名</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  contest: { type: Object, required: true }
})

const router = useRouter()

const statusMap = {
  0: { label: '未发布', type: 'info' },
  1: { label: '报名中', type: 'success' },
  2: { label: '已截止', type: 'warning' },
}

const statusLabel = computed(() => statusMap[props.contest.status]?.label || '未知')
const statusType = computed(() => statusMap[props.contest.status]?.type || 'info')

const levelMap = { '校级': '校级', '市级': '市级', '省级': '省级', '国家级': '国家级' }
const levelLabel = computed(() => levelMap[props.contest.level] || '—')

const gradientMap = {
  '理工类': 'linear-gradient(135deg, #1a2332 0%, #2a3a52 50%, #3a4a6a 100%)',
  '文史类': 'linear-gradient(135deg, #5b3e6b 0%, #7a5a8a 50%, #9a7aaa 100%)',
  '艺术类': 'linear-gradient(135deg, #c0392b 0%, #e85d4a 50%, #f07a6a 100%)',
  '体育类': 'linear-gradient(135deg, #1a7a5a 0%, #3aaf85 50%, #5acfa5 100%)',
  '创新创业类': 'linear-gradient(135deg, #c07a2a 0%, #e8a838 50%, #f0c858 100%)',
}

const coverGradient = computed(() => {
  return gradientMap[props.contest.category] || 'linear-gradient(135deg, #2a3a52 0%, #4a5a6a 50%, #6a7a8a 100%)'
})

const coverStyle = computed(() => {
  if (props.contest.coverImageUrl) {
    return { background: `linear-gradient(135deg, rgba(26,35,50,0.7) 0%, rgba(42,58,82,0.5) 100%), url(${props.contest.coverImageUrl}) center / cover` }
  }
  return { background: coverGradient.value }
})

function formatDate(dateStr) {
  if (!dateStr) return '—'
  const d = new Date(dateStr)
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${m}/${day}`
}

function goDetail() {
  router.push(`/contest/${props.contest.id}`)
}
</script>

<style scoped>
.contest-card {
  border-radius: 2px;
  overflow: hidden;
  background: var(--c-surface);
  box-shadow:
    0 0 0 1px rgba(0,0,0,0.03),
    0 2px 4px rgba(0,0,0,0.02),
    0 8px 16px rgba(0,0,0,0.02);
  cursor: pointer;
  transition: all 0.5s var(--ease-editorial);
}

.contest-card:hover {
  transform: translateY(-6px);
  box-shadow:
    0 0 0 1px rgba(0,0,0,0.04),
    0 12px 24px rgba(0,0,0,0.06);
}

.contest-card__cover {
  position: relative;
  height: 130px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 14px;
  background-size: 100%;
  transition: background-size 0.6s var(--ease-editorial);
}

.contest-card:hover .contest-card__cover {
  background-size: 105%;
}

.contest-card__cover::after {
  content: '';
  position: absolute;
  bottom: 0; left: 0; right: 0;
  height: 60%;
  background: linear-gradient(to top, rgba(10, 16, 24, 0.7) 0%, transparent 100%);
  pointer-events: none;
}

.contest-card__badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 12px;
  border-radius: 100px;
  font-size: 0.75rem;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(4px);
  color: var(--c-primary);
  position: relative;
  z-index: 1;
}

.contest-card__dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--c-text-muted);
}

.contest-card__badge--success .contest-card__dot {
  background: var(--c-success);
  box-shadow: 0 0 6px rgba(58, 175, 133, 0.5);
}

.contest-card__badge--warning .contest-card__dot {
  background: var(--c-warning);
  box-shadow: 0 0 6px rgba(232, 168, 56, 0.5);
}

.contest-card__badge--info .contest-card__dot {
  background: var(--c-text-muted);
}

.contest-card__category-tag {
  padding: 4px 12px;
  border-radius: 100px;
  font-size: 0.7rem;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(4px);
  color: #fff;
  position: relative;
  z-index: 1;
}

.contest-card__body {
  padding: 16px 18px 20px;
}

.contest-card__title {
  font-family: var(--font-display);
  font-size: 1.1rem;
  font-weight: 400;
  color: var(--c-primary);
  line-height: 1.35;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.3s var(--ease-editorial);
}

.contest-card:hover .contest-card__title {
  color: var(--c-accent);
}

.contest-card__title::after {
  content: '';
  display: block;
  width: 32px;
  height: 2px;
  background: var(--c-accent);
  margin-top: 0.5rem;
}

.contest-card__meta {
  display: flex;
  gap: 6px;
  margin-bottom: 14px;
}

.contest-card__tag {
  font-size: 0.7rem !important;
  padding: 0 10px !important;
  height: 22px !important;
  line-height: 22px !important;
  background: rgba(26, 35, 50, 0.06) !important;
  color: var(--c-text-muted) !important;
  border-radius: 2px !important;
}

.contest-card__tag--level {
  background: rgba(232, 93, 74, 0.08) !important;
  color: var(--c-accent) !important;
}

.contest-card__info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.contest-card__info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.8rem;
  color: var(--c-text-muted);
}

.contest-card__info-item svg {
  flex-shrink: 0;
  color: var(--c-text-light);
}
</style>
