<template>
  <div class="page">
    <NavBar />

    <!-- Hero -->
    <section class="hero">
      <div class="hero-bg">
        <div class="hero-shape hero-shape--1"></div>
        <div class="hero-shape hero-shape--2"></div>
        <div class="hero-shape hero-shape--3"></div>
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
        </div>
      </div>
    </section>

    <!-- Banner Carousel -->
    <section class="banner-section anim-fade-up" v-if="banners.length">
      <div class="container">
        <el-carousel height="420px" indicator-position="outside" arrow="always">
          <el-carousel-item v-for="b in banners" :key="b.id">
            <a v-if="b.linkUrl" :href="b.linkUrl" target="_blank" class="banner-link">
              <div class="banner-slide">
                <img :src="b.imageUrl" class="banner-img" />
                <div class="banner-overlay">
                  <div class="banner-overlay-content">
                    <h3 v-if="b.title" class="banner-title">{{ b.title }}</h3>
                    <p v-if="b.description" class="banner-desc">{{ b.description }}</p>
                  </div>
                </div>
              </div>
            </a>
            <div v-else class="banner-slide">
              <img :src="b.imageUrl" class="banner-img" />
              <div class="banner-overlay">
                <div class="banner-overlay-content">
                  <h3 v-if="b.title" class="banner-title">{{ b.title }}</h3>
                  <p v-if="b.description" class="banner-desc">{{ b.description }}</p>
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
              <span class="announcement-time">{{ a.publishTime || a.createTime }}</span>
            </div>
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
      listAnnouncements(),
      getHotContests(4),
      getLatestContests(4)
    ])
    banners.value = bRes.data || []
    announcements.value = aRes.data || []
    hotContests.value = hotRes.data || []
    latestContests.value = latestRes.data || []
  } catch (e) {
    // ignore
  }
})
</script>

<style scoped>
/* ===== Hero ===== */
.hero {
  position: relative;
  padding: 100px 0 80px;
  overflow: hidden;
  background: linear-gradient(180deg, var(--c-primary) 0%, #0f1622 100%);
}

.hero-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.hero-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.08;
}

.hero-shape--1 {
  width: 600px;
  height: 600px;
  background: var(--c-accent);
  top: -200px;
  right: -150px;
}

.hero-shape--2 {
  width: 400px;
  height: 400px;
  background: #fff;
  bottom: -100px;
  left: -100px;
}

.hero-shape--3 {
  width: 200px;
  height: 200px;
  background: var(--c-accent);
  bottom: 20%;
  right: 30%;
}

.hero-inner {
  position: relative;
  z-index: 1;
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
  width: 280px;
  height: 280px;
}

.hero-decoration-ring {
  width: 280px;
  height: 280px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  position: absolute;
  top: 0;
  left: 0;
}

.hero-decoration-ring::before {
  content: '';
  position: absolute;
  inset: 30px;
  border: 1px solid rgba(232, 93, 74, 0.2);
  border-radius: 50%;
}

.hero-decoration-ring::after {
  content: '';
  position: absolute;
  inset: 60px;
  background: rgba(232, 93, 74, 0.1);
  border-radius: 50%;
}

.hero-decoration-dots {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 8px;
  height: 8px;
  background: var(--c-accent);
  border-radius: 50%;
  box-shadow:
    20px 20px 0 rgba(232, 93, 74, 0.3),
    -20px -20px 0 rgba(232, 93, 74, 0.3),
    20px -20px 0 rgba(232, 93, 74, 0.2),
    -20px 20px 0 rgba(232, 93, 74, 0.2),
    0 -30px 0 rgba(255, 255, 255, 0.1),
    0 30px 0 rgba(255, 255, 255, 0.1);
}

/* ===== Banner ===== */
.banner-section {
  padding-top: 20px;
}

.banner-slide {
  position: relative;
  border-radius: var(--radius-lg);
  overflow: hidden;
  height: 100%;
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
  background: linear-gradient(90deg, rgba(26, 35, 50, 0.75) 0%, rgba(26, 35, 50, 0.1) 60%, transparent 100%);
  display: flex;
  align-items: flex-end;
  padding: 40px;
}

.banner-overlay-content {
  max-width: 500px;
}

.banner-title {
  font-family: 'DM Serif Display', serif;
  font-size: 1.6rem;
  color: #fff;
  margin-bottom: 8px;
}

.banner-desc {
  font-size: 0.95rem;
  color: rgba(255, 255, 255, 0.7);
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
    grid-template-columns: repeat(2, 1fr);
  }
  .hero-title {
    font-size: 2.5rem;
  }
  .hero-decoration {
    display: none;
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
