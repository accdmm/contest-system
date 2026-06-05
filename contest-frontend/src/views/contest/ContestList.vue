<template>
  <div class="page">
    <NavBar />
    <HeroSection
      badge="Explore"
      title="所有竞赛"
      subtitle="浏览并报名你感兴趣的学科竞赛"
      :show-actions="false"
      :show-decoration="true"
    />
    <div class="container">

      <div class="filter-card anim-fade-up anim-delay-1">
        <div class="filter-row">
          <div class="filter-group">
            <input
              v-model="query.keyword"
              class="filter-input"
              placeholder="搜索竞赛名称..."
              @keyup.enter="search"
              @input="onKeywordInput"
            />
          </div>
          <div class="filter-group">
            <select v-model="query.category" class="filter-select" @change="search">
              <option value="">全部类别</option>
              <option v-for="c in categories" :key="c" :value="c">{{ c }}</option>
            </select>
          </div>
          <div class="filter-group">
            <select v-model="query.status" class="filter-select" @change="search">
              <option value="">全部状态</option>
              <option value="1">报名中</option>
              <option value="2">已截止</option>
              <option v-if="store.isLoggedIn && store.user?.role === 1" value="0">草稿</option>
            </select>
          </div>
          <div class="filter-group">
            <select v-model="query.contestType" class="filter-select" @change="search">
              <option value="">全部形式</option>
              <option value="0">个人赛</option>
              <option value="1">团队赛</option>
              <option value="2">均可</option>
            </select>
          </div>
          <div class="filter-group filter-group--sort">
            <select v-model="query.sortBy" class="filter-select" @change="search">
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
import HeroSection from '../../components/HeroSection.vue'
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

let keywordTimer
function onKeywordInput() {
  clearTimeout(keywordTimer)
  keywordTimer = setTimeout(() => { query.page = 1; fetchData() }, 300)
}

onMounted(fetchData)
</script>

<style scoped>
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

@media (max-width: 768px) {
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
