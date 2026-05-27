<template>
  <div class="page">
    <NavBar />
    <div class="container">
      <div class="list-hero anim-fade">
        <h1 class="list-title">所有竞赛</h1>
        <p class="list-subtitle">浏览并报名你感兴趣的学科竞赛</p>
      </div>

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
            <span class="filter-status-label">报名中</span>
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
import { pageContests } from '../../api/contest'

const categories = ['理工类', '文史类', '艺术类', '体育类', '创新创业类']
const list = ref([])
const total = ref(0)
const query = reactive({ keyword: '', category: '', page: 1, size: 12 })

async function fetchData() {
  const params = { ...query, status: 1 }
  if (!params.category) delete params.category
  if (!params.keyword) delete params.keyword
  try {
    const res = await pageContests(params)
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) { /* ignore */ }
}

function search() { query.page = 1; fetchData() }
function pageChange(p) { query.page = p; fetchData() }

onMounted(fetchData)
</script>

<style scoped>
.list-hero {
  text-align: center;
  padding: 48px 0 36px;
}

.list-title {
  font-family: 'DM Serif Display', Georgia, serif;
  font-size: 2.5rem;
  font-weight: 400;
  color: var(--c-primary);
  margin: 0 0 8px;
  letter-spacing: 0.02em;
}

.list-subtitle {
  color: var(--c-text-muted);
  font-size: 1rem;
  margin: 0;
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

.filter-status-label {
  display: flex;
  align-items: center;
  height: 44px;
  padding: 0 14px;
  font-size: 0.9rem;
  color: var(--c-success);
  font-weight: 600;
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
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

@media (max-width: 1024px) {
  .card-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .card-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .list-title {
    font-size: 2rem;
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
  .list-hero {
    padding: 32px 0 24px;
  }
  .list-title {
    font-size: 1.75rem;
  }
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  padding: 40px 0 60px;
}
</style>
