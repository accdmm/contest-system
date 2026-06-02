<template>
  <div class="page">
    <NavBar />
    <section class="hero anim-fade">
      <div class="hero-bg">
        <div class="hero-shape hero-shape--1"></div>
        <div class="hero-shape hero-shape--2"></div>
        <div class="hero-shape hero-shape--3"></div>
        <div class="hero-stars"></div>
      </div>
      <div class="container hero-inner">
        <div class="hero-content">
          <span class="hero-badge">Explore</span>
          <h1 class="hero-title">所有竞赛</h1>
          <p class="hero-subtitle">浏览并报名你感兴趣的学科竞赛</p>
        </div>
        <div class="hero-decoration">
          <div class="hero-decoration-ring"></div>
          <div class="hero-decoration-dots"></div>
          <div class="orbit orbit--1"><span class="orbit-dot"></span></div>
          <div class="orbit orbit--2"><span class="orbit-dot"></span></div>
          <div class="orbit orbit--3"><span class="orbit-dot"></span></div>
          <div class="orbit orbit--4"><span class="orbit-dot"></span></div>
        </div>
      </div>
    </section>
    <div class="container">

      <div class="filter-card anim-fade-up anim-delay-1">
        <div class="filter-row">
          <div class="filter-group">
            <input
              v-model="query.keyword"
              class="filter-input"
              placeholder="搜索竞赛名称..."
              @keyup.enter="search"
            />
          </div>
          <div class="filter-group">
            <select v-model="query.category" class="filter-select">
              <option value="">全部类别</option>
              <option v-for="c in categories" :key="c" :value="c">{{ c }}</option>
            </select>
          </div>
          <div class="filter-group">
            <select v-model="query.status" class="filter-select">
              <option value="">全部状态</option>
              <option value="1">报名中</option>
              <option value="2">已截止</option>
              <option v-if="store.isLoggedIn && store.user?.role === 1" value="0">草稿</option>
            </select>
          </div>
          <div class="filter-group">
            <select v-model="query.contestType" class="filter-select">
              <option value="">全部形式</option>
              <option value="0">个人赛</option>
              <option value="1">团队赛</option>
              <option value="2">均可</option>
            </select>
          </div>
          <div class="filter-group filter-group--sort">
            <select v-model="query.sortBy" class="filter-select">
              <option value="">按更新时间</option>
              <option value="hot">按热门程度</option>
              <option value="deadline">按报名截止</option>
            </select>
          </div>
          <button class="btn btn-primary" @click="search">搜索</button>
        </div>
      </div>

      <div v-if="list.length === 0" class="empty-state anim-fade">
        <svg viewBox="0 0 80 80" fill="none">
          <rect x="10" y="20" width="60" height="48" rx="6" stroke="currentColor" stroke-width="2"/>
          <line x1="20" y1="34" x2="60" y2="34" stroke="currentColor" stroke-width="2"/>
          <line x1="20" y1="44" x2="50" y2="44" stroke="currentColor" stroke-width="2"/>
          <line x1="20" y1="54" x2="40" y2="54" stroke="currentColor" stroke-width="2"/>
        </svg>
        <p>暂无匹配的竞赛</p>
      </div>

      <div v-else class="card-grid">
        <div
          v-for="(c, i) in list"
          :key="c.id"
          class="grid-item"
          :class="`anim-fade-up anim-delay-${Math.min(i + 1, 6)}`"
        >
          <ContestCard :contest="c" />
        </div>
      </div>

      <BasePagination :total="total" :page-size="query.size" :current-page="query.page" @change="pageChange" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import NavBar from '../../components/NavBar.vue'
import ContestCard from '../../components/ContestCard.vue'
import BasePagination from '../../components/BasePagination.vue'
import { ElMessage } from 'element-plus'
import { pageContests } from '../../api/contest'
import { useUserStore } from '../../stores/user'

const store = useUserStore()

const categories = ['理工类', '文史类', '艺术类', '体育类', '创新创业类']
const list = ref([])
const total = ref(0)
const query = reactive({ keyword: '', category: '', status: '', contestType: '', sortBy: '', page: 1, size: 6 })

async function fetchData() {
  const params = { ...query }
  if (!params.category) delete params.category
  if (!params.keyword) delete params.keyword
  if (!params.status && params.status !== 0) delete params.status
  if (!params.sortBy) delete params.sortBy
  if (params.contestType === '') delete params.contestType
  try {
    const res = await pageContests(params)
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) { ElMessage.error('搜索失败，请重试') }
}

function search() { query.page = 1; fetchData() }
function pageChange(p) { query.page = p; fetchData() }

onMounted(fetchData)
</script>

<style scoped>
/* ===== Hero ===== */
.hero {
  position: relative;
  padding: 80px 0 64px;
  overflow: hidden;
  background: linear-gradient(160deg, #0a1018 0%, #12102a 40%, #0a1018 100%);
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 60px;
}

.hero-content {
  flex: 1;
  max-width: 640px;
}

.hero-badge {
  display: inline-block;
  font-size: 0.8rem;
  font-weight: 600;
  letter-spacing: 0.15em;
  text-transform: uppercase;
  color: var(--c-accent);
  background: rgba(232, 93, 74, 0.12);
  padding: 6px 16px;
  border-radius: 20px;
  margin-bottom: 20px;
}

.hero-title {
  font-family: 'DM Serif Display', serif;
  font-size: 3.5rem;
  color: #fff;
  line-height: 1.15;
  margin-bottom: 20px;
}

.hero-subtitle {
  font-size: 1.1rem;
  color: rgba(255, 255, 255, 0.65);
  line-height: 1.7;
  margin-bottom: 32px;
  max-width: 520px;
}

/* === Decoration === */
.hero-decoration {
  flex-shrink: 0;
  position: relative;
  width: 340px;
  height: 340px;
}

.hero-decoration-ring {
  width: 340px;
  height: 340px;
  border: 1.5px solid rgba(201, 168, 76, 0.2);
  border-radius: 50%;
  position: absolute;
  top: 0;
  left: 0;
  transform-origin: center;
  animation: orbit-spin 25s linear infinite reverse;
}

.hero-decoration-ring::before {
  content: '';
  position: absolute;
  inset: 40px;
  border: 1.5px dashed rgba(201, 168, 76, 0.15);
  border-radius: 50%;
  transform-origin: center;
  animation: orbit-spin 20s linear infinite;
}

.hero-decoration-ring::after {
  content: '';
  position: absolute;
  inset: 80px;
  background: rgba(168, 85, 247, 0.08);
  border-radius: 50%;
  transform-origin: center;
  animation: orbit-spin 15s linear infinite reverse;
}

.hero-decoration-dots {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 8px;
  height: 8px;
  background: var(--c-gold);
  border-radius: 50%;
  box-shadow:
    24px 24px 0 rgba(201, 168, 76, 0.25),
    -24px -24px 0 rgba(201, 168, 76, 0.25),
    24px -24px 0 rgba(201, 168, 76, 0.15),
    -24px 24px 0 rgba(201, 168, 76, 0.15),
    0 -36px 0 rgba(255, 255, 255, 0.08),
    0 36px 0 rgba(255, 255, 255, 0.08);
}

.orbit {
  position: absolute;
  inset: 0;
  pointer-events: none;
  transform-origin: center;
}

.orbit-dot {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 7px;
  height: 7px;
  margin-left: -3.5px;
  border-radius: 50%;
  animation: orbit-pulse 4s ease-in-out infinite;
}

.orbit--1 { animation: orbit-spin 35s linear infinite; }
.orbit--1 .orbit-dot { margin-top: calc(-230px - 3.5px); background: rgba(168,85,247,0.7); box-shadow: 0 0 8px rgba(168,85,247,0.4); }

.orbit--2 { animation: orbit-spin 25s linear infinite reverse; animation-delay: -6s; }
.orbit--2 .orbit-dot { margin-top: calc(-170px - 3.5px); background: rgba(201,168,76,0.7); box-shadow: 0 0 8px rgba(201,168,76,0.4); }

.orbit--3 { animation: orbit-spin 20s linear infinite; animation-delay: -5s; }
.orbit--3 .orbit-dot { margin-top: calc(-130px - 3.5px); background: rgba(201,168,76,0.6); box-shadow: 0 0 6px rgba(201,168,76,0.3); }

.orbit--4 { animation: orbit-spin 15s linear infinite reverse; animation-delay: -4s; }
.orbit--4 .orbit-dot { margin-top: calc(-90px - 3.5px); background: rgba(168,85,247,0.6); box-shadow: 0 0 6px rgba(168,85,247,0.3); }

@keyframes orbit-pulse {
  0%, 100% { opacity: 0.5; transform: scale(0.8); }
  50% { opacity: 1; transform: scale(1.2); }
}

@keyframes nebula-drift {
  0% { transform: scale(1) translate(0, 0); opacity: 0.6; }
  50% { transform: scale(1.05) translate(-1%, 1%); opacity: 1; }
  100% { transform: scale(1) translate(1%, -1%); opacity: 0.6; }
}

@keyframes orbit-spin {
  to { transform: rotate(360deg); }
}

@keyframes star-twinkle {
  0%, 100% { opacity: 0.2; }
  40% { opacity: 1; }
  70% { opacity: 0.2; }
}

.filter-card {
  background: var(--c-surface);
  border-radius: var(--radius-md);
  padding: 20px 24px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--c-border-light);
  margin-bottom: 36px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-group {
  flex: 1;
  min-width: 160px;
}

.filter-group--sort {
  flex: 0.6;
  min-width: 140px;
}

.filter-input,
.filter-select {
  width: 100%;
  height: 44px;
  padding: 0 14px;
  border: 1.5px solid var(--c-border);
  border-radius: var(--radius-sm);
  background: var(--c-bg);
  color: var(--c-text);
  font-family: 'DM Sans', sans-serif;
  font-size: 0.9rem;
  transition: var(--transition);
  outline: none;
}

.filter-input:focus,
.filter-select:focus {
  border-color: var(--c-primary);
  background: var(--c-surface);
}

.filter-input::placeholder {
  color: var(--c-text-light);
}

.btn {
  height: 44px;
  padding: 0 28px;
  border: none;
  border-radius: var(--radius-sm);
  font-family: 'DM Sans', sans-serif;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: var(--transition);
  white-space: nowrap;
}

.btn-primary {
  background: var(--c-primary);
  color: #fff;
}

.btn-primary:hover {
  background: var(--c-primary-light);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.btn-primary:active {
  transform: translateY(0);
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

@media (max-width: 900px) {
  .hero-decoration {
    display: none;
  }
  .hero-inner {
    gap: 0;
  }
}

@media (max-width: 768px) {
  .hero {
    padding: 48px 0 40px;
  }
  .hero-title {
    font-size: 2rem;
  }
  .hero-subtitle {
    font-size: 1rem;
  }
  .hero-decoration {
    display: none;
  }
  .card-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .filter-row {
    flex-direction: column;
  }
  .filter-group {
    width: 100%;
    min-width: unset;
  }
  .btn {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .hero {
    padding: 40px 0 32px;
  }
  .hero-title {
    font-size: 1.75rem;
  }
  .card-grid {
    grid-template-columns: 1fr;
  }
  .filter-card {
    padding: 16px;
  }
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  padding: 40px 0 60px;
}
</style>
