<template>
  <div class="page">
    <NavBar />

    <HeroSection
      badge="高校学科竞赛平台"
      title="发现你的竞赛之路"
      subtitle="汇聚全国高校权威赛事，助你以赛促学、以赛促创，在竞技中成长，在挑战中突破。"
      :show-actions="true"
      :show-decoration="true"
      primary-text="浏览全部竞赛"
      primary-action="/contest"
      secondary-text="立即加入"
      :secondary-action="handleJoin"
    />

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
import { sanitizeHtml } from '../../utils/sanitize'
import HeroSection from '../../components/HeroSection.vue'
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
  try { const r = await listBanners(); banners.value = r.data || [] } catch (e) { banners.value = [] }
  try {
    const r = await listAnnouncements('message_center')
    const raw = r.data || []
    raw.forEach(a => { if (a.content) a.content = sanitizeHtml(a.content) })
    announcements.value = raw
  } catch (e) { announcements.value = [] }
  try { const r = await getHotContests(4); hotContests.value = r.data || [] } catch (e) { hotContests.value = [] }
  try { const r = await getLatestContests(4); latestContests.value = r.data || [] } catch (e) { latestContests.value = [] }
})
</script>

<style scoped>
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
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
