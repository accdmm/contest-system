<template>
  <div class="page">
    <NavBar />

    <!-- Hero -->
    <section class="hero">
      <div class="hero-bg">
        <div class="hero-shape hero-shape--1"></div>
        <div class="hero-shape hero-shape--2"></div>
        <div class="hero-shape hero-shape--3"></div>
        <div class="hero-stars"></div>
      </div>
      <div class="hero-inner container">
        <div class="hero-content anim-fade-up">
          <span class="hero-badge">高校学科竞赛平台</span>
          <h1 class="hero-title">发现你的竞赛之路</h1>
          <p class="hero-subtitle">汇聚全国高校权威赛事，助你以赛促学、以赛促创，在竞技中成长，在挑战中突破。</p>
          <div class="hero-actions">
            <el-button type="primary" size="large" round @click="$router.push('/contest')">浏览全部竞赛</el-button>
            <el-button size="large" round @click="handleJoin">立即加入</el-button>
          </div>
        </div>
        <div class="hero-decoration anim-slide-right anim-delay-2">
          <div class="hero-decoration-ring"></div>
          <div class="hero-decoration-dots"></div>
          <div class="orbit orbit--1"><span class="orbit-dot"></span></div>
          <div class="orbit orbit--2"><span class="orbit-dot"></span></div>
          <div class="orbit orbit--3"><span class="orbit-dot"></span></div>
          <div class="orbit orbit--4"><span class="orbit-dot"></span></div>
        </div>
      </div>
    </section>

    <!-- Banner Carousel -->
    <section class="banner-section anim-fade-up" v-if="banners.length">
      <div class="container">
        <div class="banner-header">
          <span class="banner-header-label">赛事公告</span>
          <span class="banner-header-line"></span>
        </div>
        <el-carousel height="420px" indicator-position="outside" arrow="always">
          <el-carousel-item v-for="b in banners" :key="b.id">
            <a v-if="b.linkUrl" :href="b.linkUrl" target="_blank" class="banner-link">
              <div class="banner-slide">
                <img :src="b.imageUrl" class="banner-img" />
                <div class="banner-overlay">
                  <div class="banner-overlay-content">
                    <h3 v-if="b.title" class="banner-title">{{ b.title }}</h3>
                    <p v-if="b.content" class="banner-desc">{{ b.content }}</p>
                  </div>
                </div>
              </div>
            </a>
            <div v-else class="banner-slide">
              <img :src="b.imageUrl" class="banner-img" />
              <div class="banner-overlay">
                <div class="banner-overlay-content">
                  <h3 v-if="b.title" class="banner-title">{{ b.title }}</h3>
                  <p v-if="b.content" class="banner-desc">{{ b.content }}</p>
                </div>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
    </section>

    <!-- Hot Contests -->
    <section class="section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">热门竞赛</h2>
          <el-button text type="primary" @click="$router.push('/contest')">查看全部 &rarr;</el-button>
        </div>
        <div v-if="hotContests.length" class="card-grid">
          <div
            v-for="(c, i) in hotContests"
            :key="c.id"
            class="anim-fade-up"
            :class="'anim-delay-' + (i + 1)"
          >
            <ContestCard :contest="c" />
          </div>
        </div>
        <div v-else class="empty-state">
          <p>暂无热门竞赛</p>
        </div>
      </div>
    </section>

    <!-- Latest Contests -->
    <section class="section section--alt">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">最新竞赛</h2>
          <el-button text type="primary" @click="$router.push('/contest')">查看全部 &rarr;</el-button>
        </div>
        <div v-if="latestContests.length" class="card-grid">
          <div
            v-for="(c, i) in latestContests"
            :key="c.id"
            class="anim-fade-up"
            :class="'anim-delay-' + (i + 1)"
          >
            <ContestCard :contest="c" />
          </div>
        </div>
        <div v-else class="empty-state">
          <p>暂无最新竞赛</p>
        </div>
      </div>
    </section>

    <!-- Announcements -->
    <section class="section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">公告</h2>
        </div>
        <div v-if="announcements.length" class="announcement-list">
          <div
            v-for="(a, i) in announcements"
            :key="a.id"
            class="announcement-card anim-fade-up"
            :class="'anim-delay-' + (i + 1)"
          >
            <div class="announcement-dot"></div>
            <div class="announcement-body">
              <h3 class="announcement-title">{{ a.title }}</h3>
              <p v-if="a.content" class="announcement-content" v-html="a.content"></p>
              <span class="announcement-time">{{ formatTime(a.publishTime || a.createTime) }}</span>
            </div>
            <img v-if="a.imageUrl" :src="a.imageUrl" class="announcement-img" />
          </div>
        </div>
        <div v-else class="empty-state">
          <p>暂无公告</p>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <footer class="footer">
      <div class="container">
        <p>高校学科竞赛报名管理系统 &copy; {{ new Date().getFullYear() }}</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { formatTime } from '../../utils/format'
import NavBar from '../../components/NavBar.vue'
import ContestCard from '../../components/ContestCard.vue'
import { listBanners, listAnnouncements } from '../../api/cms'
import { getHotContests, getLatestContests } from '../../api/contest'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const store = useUserStore()

const banners = ref([])
const hotContests = ref([])
const latestContests = ref([])
const announcements = ref([])

function handleJoin() {
  router.push(store.isLoggedIn ? '/contest' : '/login')
}

onMounted(async () => {
  try {
    const [bRes, aRes, hotRes, latestRes] = await Promise.all([
      listBanners(),
      listAnnouncements('message_center'),
      getHotContests(4),
      getLatestContests(4)
    ])
    banners.value = bRes.data || []
    announcements.value = aRes.data || []
    hotContests.value = hotRes.data || []
    latestContests.value = latestRes.data || []
  } catch (e) {
    banners.value = []; announcements.value = []; hotContests.value = []; latestContests.value = []
  }
})
</script>

<style scoped>
/* ===== Hero ===== */
.hero {
  position: relative;
  padding: 100px 0 80px;
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

.hero-actions {
  display: flex;
  gap: 12px;
}

.hero-actions .el-button {
  padding: 12px 32px;
  font-size: 0.95rem;
}

.hero-actions .el-button--primary {
  background: var(--c-accent) !important;
}

.hero-actions .el-button--primary:hover {
  background: var(--c-accent-light) !important;
}

.hero-actions .el-button:not(.el-button--primary) {
  background: rgba(255, 255, 255, 0.08) !important;
  color: #fff !important;
  border: 1px solid rgba(255, 255, 255, 0.15) !important;
}

.hero-actions .el-button:not(.el-button--primary):hover {
  background: rgba(255, 255, 255, 0.14) !important;
}

.hero-decoration {
  flex-shrink: 0;
  position: relative;
  width: 340px;
  height: 340px;
}

.hero-decoration::before {
  content: '';
  position: absolute;
  top: -60px;
  left: -60px;
  width: 460px;
  height: 460px;
  border: 1.5px solid rgba(168, 85, 247, 0.12);
  border-radius: 50%;
  transform-origin: center;
  animation: orbit-spin 35s linear infinite;
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

/* === Orbiting Nodes === */
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

/* === Stars === */
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

/* ===== Animations ===== */
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

/* ===== Banner ===== */
.banner-section {
  padding: 40px 0;
  position: relative;
}

.banner-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 2px;
  background: var(--c-gold);
  opacity: 0.4;
}

.banner-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 24px;
}

.banner-header-label {
   font-family: 'DM Serif Display', Georgia, serif;
   font-size: 1.35rem;
   color: var(--c-primary);
   flex-shrink: 0;
}

.banner-header-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, rgba(201,168,76,0.3), transparent);
}

.banner-slide {
  position: relative;
  border-radius: var(--radius-lg);
  overflow: hidden;
  height: 100%;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12), 0 2px 8px rgba(0, 0, 0, 0.08);
}

.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.banner-link {
  display: block;
  height: 100%;
}

.banner-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(26, 35, 50, 0.85) 0%, rgba(26, 35, 50, 0.15) 60%, transparent 100%);
  display: flex;
  align-items: flex-end;
  padding: 48px;
}

.banner-overlay-content {
  max-width: 500px;
}

.banner-title {
  font-family: 'DM Serif Display', serif;
  font-size: 2rem;
  color: #fff;
  line-height: 1.2;
  margin-bottom: 12px;
}

.banner-desc {
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.85);
  line-height: 1.6;
}

/* ===== Sections ===== */
.section {
  padding: 64px 0;
}

.section--alt {
  background: var(--c-surface);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32px;
}

/* ===== Card Grid ===== */
.card-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

/* ===== Announcements ===== */
.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.announcement-card {
  display: flex;
  gap: 20px;
  padding: 24px 0;
  border-bottom: 1px solid var(--c-border-light);
  transition: var(--transition);
}

.announcement-card:last-child {
  border-bottom: none;
}

.announcement-card:hover {
  padding-left: 8px;
}

.announcement-dot {
  flex-shrink: 0;
  width: 10px;
  height: 10px;
  background: var(--c-accent);
  border-radius: 50%;
  margin-top: 8px;
  box-shadow: 0 0 0 4px rgba(232, 93, 74, 0.12);
}

.announcement-body {
  flex: 1;
  min-width: 0;
}

.announcement-title {
  font-family: 'DM Sans', sans-serif;
  font-size: 1.05rem;
  font-weight: 600;
  color: var(--c-text);
  margin-bottom: 6px;
}

.announcement-content {
  font-size: 0.9rem;
  color: var(--c-text-muted);
  line-height: 1.6;
  margin-bottom: 8px;
}

.announcement-time {
  font-size: 0.8rem;
  color: var(--c-text-light);
}

.announcement-img {
  width: 120px;
  height: 68px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
  margin-top: 4px;
}

/* ===== Footer ===== */
.footer {
  text-align: center;
  padding: 32px 0;
  border-top: 1px solid var(--c-border-light);
  color: var(--c-text-muted);
  font-size: 0.85rem;
}

/* ===== Responsive ===== */
@media (max-width: 1024px) {
  .card-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  .hero-title {
    font-size: 2.5rem;
  }
  .hero-decoration {
    display: none;
  }
}

@media (max-width: 820px) {
  .card-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .card-grid {
    grid-template-columns: 1fr;
  }
  .hero {
    padding: 60px 0 60px;
  }
  .hero-title {
    font-size: 2rem;
  }
  .hero-actions {
    flex-direction: column;
  }
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
