<template>
  <div class="dashboard-page">
    <div class="page-header">
      <div class="header-bg"></div>
      <div class="container">
        <h1 class="page-title">数据看板</h1>
        <p class="page-subtitle">竞赛平台运营数据概览</p>
      </div>
    </div>
    <div class="container">

      <!-- Stat Cards -->
      <div class="stats-grid">
        <div v-for="(card, i) in stats" :key="i" class="stat-card" :style="{ '--card-color': card.color }">
          <div class="stat-icon" v-html="card.icon"></div>
          <div class="stat-body">
            <span class="stat-value">{{ card.value }}</span>
            <span class="stat-label">{{ card.label }}</span>
          </div>
          <div class="stat-glow"></div>
        </div>
      </div>

      <!-- Charts Row 1 -->
      <div class="charts-grid">
        <div class="chart-card">
          <div class="chart-header">
            <h3>竞赛类别分布</h3>
            <span class="chart-subtitle">按学科类别统计</span>
          </div>
          <div ref="chartCategory" class="chart-container"></div>
        </div>
        <div class="chart-card">
          <div class="chart-header">
            <h3>报名趋势</h3>
            <span class="chart-subtitle">近30天每日报名数</span>
          </div>
          <div ref="chartTrend" class="chart-container"></div>
        </div>
      </div>

      <!-- Charts Row 2 -->
      <div class="charts-grid">
        <div class="chart-card">
          <div class="chart-header">
            <h3>报名状态分布</h3>
            <span class="chart-subtitle">当前全部报名记录</span>
          </div>
          <div ref="chartStatus" class="chart-container"></div>
        </div>
        <div class="chart-card">
          <div class="chart-header">
            <h3>用户增长</h3>
            <span class="chart-subtitle">近30天新增用户</span>
          </div>
          <div ref="chartGrowth" class="chart-container"></div>
        </div>
      </div>

      <!-- Charts Row 3 -->
      <div class="charts-grid">
        <div class="chart-card">
          <div class="chart-header">
            <h3>竞赛级别分布</h3>
            <span class="chart-subtitle">按竞赛级别统计</span>
          </div>
          <div ref="chartLevel" class="chart-container"></div>
        </div>
        <div class="chart-card">
          <div class="chart-header">
            <h3>热门竞赛 Top 10</h3>
            <span class="chart-subtitle">按报名人数排序</span>
          </div>
          <div ref="chartTop" class="chart-container"></div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import {
  getDashboardStatistics,
  getContestCategoryDistribution,
  getContestLevelDistribution,
  getRegistrationTrend,
  getRegistrationStatus,
  getUserGrowth,
  getTopContests
} from '../../api/dashboard'

// ── Stat Cards ──
const stats = ref([
  { label: '总用户数', value: 0, icon: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>', color: '#6366f1' },
  { label: '总竞赛数', value: 0, icon: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"/><path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"/><path d="M4 22h16"/><path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22"/><path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22"/><path d="M18 2H6v7a6 6 0 0 0 12 0V2Z"/></svg>', color: '#f59e0b' },
  { label: '总报名数', value: 0, icon: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>', color: '#10b981' },
  { label: '总团队数', value: 0, icon: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>', color: '#8b5cf6' },
])

// ── Chart Refs ──
const chartCategory = ref(null)
const chartTrend = ref(null)
const chartStatus = ref(null)
const chartGrowth = ref(null)
const chartLevel = ref(null)
const chartTop = ref(null)

let resizeHandler = null

function initChart(el) {
  if (!el) return null
  const existing = echarts.getInstanceByDom(el)
  if (existing) return existing
  const chart = echarts.init(el)
  return chart
}

// ── Color Palette ──
const colors = ['#6366f1', '#f59e0b', '#10b981', '#ef4444', '#8b5cf6', '#ec4899', '#14b8a6', '#f97316', '#06b6d4', '#84cc16']

// ── Status Mapping ──
const statusLabels = { 0: '待审核', 1: '已通过', 2: '已驳回', 3: '已取消' }
const statusColors = { 0: '#f59e0b', 1: '#10b981', 2: '#ef4444', 3: '#94a3b8' }

// ── Chart Renderers ──
function renderCategoryPie(data) {
  if (!chartCategory.value || !data?.length) return
  const chart = initChart(chartCategory.value)
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, textStyle: { color: '#94a3b8' } },
    series: [{
      type: 'pie',
      radius: ['35%', '60%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#1a2332', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14 } },
      data: data.map((item, i) => ({ name: item.category, value: Number(item.count), itemStyle: { color: colors[i % colors.length] } }))
    }]
  })
}

function renderTrendLine(data) {
  if (!chartTrend.value) return
  const chart = initChart(chartTrend.value)
  if (!data?.length) {
    chart.setOption({ title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#64748b', fontSize: 14 } } })
    return
  }
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, bottom: 30, top: 20 },
    xAxis: { type: 'category', data: data.map(item => item.date), axisLabel: { color: '#94a3b8', fontSize: 10 }, axisLine: { lineStyle: { color: '#334155' } } },
    yAxis: { type: 'value', axisLabel: { color: '#94a3b8' }, splitLine: { lineStyle: { color: '#1e293b' } } },
    series: [{
      type: 'line', smooth: true,
      data: data.map(item => Number(item.count)),
      symbol: 'circle', symbolSize: 6,
      lineStyle: { color: '#6366f1', width: 2 },
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(99,102,241,0.3)' }, { offset: 1, color: 'rgba(99,102,241,0.02)' }]) },
      itemStyle: { color: '#6366f1' }
    }]
  })
}

function renderStatusDonut(data) {
  if (!chartStatus.value) return
  const chart = initChart(chartStatus.value)
  if (!data?.length) {
    chart.setOption({ title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#64748b', fontSize: 14 } } })
    return
  }
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, textStyle: { color: '#94a3b8' } },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      avoidLabelOverlap: true,
      label: { show: true, formatter: '{b}\n{d}%', fontSize: 11, color: '#cbd5e1' },
      itemStyle: { borderRadius: 4, borderColor: '#1a2332', borderWidth: 2 },
      data: data.map(item => ({
        name: statusLabels[item.status] || '未知',
        value: Number(item.count),
        itemStyle: { color: statusColors[item.status] || '#94a3b8' }
      }))
    }]
  })
}

function renderGrowthLine(data) {
  if (!chartGrowth.value) return
  const chart = initChart(chartGrowth.value)
  if (!data?.length) {
    chart.setOption({ title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#64748b', fontSize: 14 } } })
    return
  }
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, bottom: 30, top: 20 },
    xAxis: { type: 'category', data: data.map(item => item.date), axisLabel: { color: '#94a3b8', fontSize: 10 }, axisLine: { lineStyle: { color: '#334155' } } },
    yAxis: { type: 'value', axisLabel: { color: '#94a3b8' }, splitLine: { lineStyle: { color: '#1e293b' } } },
    series: [{
      type: 'line', smooth: true,
      data: data.map(item => Number(item.count)),
      symbol: 'circle', symbolSize: 6,
      lineStyle: { color: '#10b981', width: 2 },
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(16,185,129,0.3)' }, { offset: 1, color: 'rgba(16,185,129,0.02)' }]) },
      itemStyle: { color: '#10b981' }
    }]
  })
}

function renderLevelBar(data) {
  if (!chartLevel.value) return
  const chart = initChart(chartLevel.value)
  if (!data?.length) {
    chart.setOption({ title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#64748b', fontSize: 14 } } })
    return
  }
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 60, right: 20, bottom: 30, top: 20 },
    xAxis: { type: 'category', data: data.map(item => item.level), axisLabel: { color: '#94a3b8' }, axisLine: { lineStyle: { color: '#334155' } } },
    yAxis: { type: 'value', axisLabel: { color: '#94a3b8' }, splitLine: { lineStyle: { color: '#1e293b' } } },
    series: [{
      type: 'bar',
      data: data.map((item, i) => ({ value: Number(item.count), itemStyle: { color: colors[i % colors.length], borderRadius: [4, 4, 0, 0] } })),
      barWidth: '50%'
    }]
  })
}

function renderTopBar(data) {
  if (!chartTop.value) return
  const chart = initChart(chartTop.value)
  if (!data?.length) {
    chart.setOption({ title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#64748b', fontSize: 14 } } })
    return
  }
  const sorted = [...data].sort((a, b) => (a.current_count || 0) - (b.current_count || 0))
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 140, right: 30, bottom: 20, top: 20 },
    xAxis: { type: 'value', axisLabel: { color: '#94a3b8' }, splitLine: { lineStyle: { color: '#1e293b' } } },
    yAxis: {
      type: 'category',
      data: sorted.map(item => (item.name || '').length > 14 ? (item.name || '').slice(0, 14) + '…' : (item.name || '')),
      axisLabel: { color: '#cbd5e1', fontSize: 11 },
      axisLine: { lineStyle: { color: '#334155' } }
    },
    series: [{
      type: 'bar',
      data: sorted.map((item, i) => ({
        value: Number(item.current_count || 0),
        itemStyle: { color: colors[i % colors.length], borderRadius: [0, 4, 4, 0] }
      })),
      barWidth: '60%'
    }]
  })
}

// ── Fetch & Render ──
async function fetchData() {
  try {
    const [statRes, catRes, levelRes, trendRes, statusRes, growthRes, topRes] = await Promise.all([
      getDashboardStatistics(),
      getContestCategoryDistribution(),
      getContestLevelDistribution(),
      getRegistrationTrend(30),
      getRegistrationStatus(),
      getUserGrowth(30),
      getTopContests(10)
    ])
    const s = statRes.data || {}
    stats.value[0].value = s.totalUsers ?? 0
    stats.value[1].value = s.totalContests ?? 0
    stats.value[2].value = s.totalRegistrations ?? 0
    stats.value[3].value = s.totalTeams ?? 0

    await nextTick()
    renderCategoryPie(catRes.data)
    renderLevelBar(levelRes.data)
    renderStatusDonut(statusRes.data)
    renderTrendLine(trendRes.data)
    renderGrowthLine(growthRes.data)
    renderTopBar(topRes.data)
  } catch (e) {
    console.error('Dashboard data fetch failed', e)
  }
}

onMounted(() => {
  fetchData()
  const charts = [chartCategory, chartTrend, chartStatus, chartGrowth, chartLevel, chartTop]
  resizeHandler = () => charts.forEach(ref => { const c = echarts.getInstanceByDom(ref.value); if (c) c.resize() })
  window.addEventListener('resize', resizeHandler)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeHandler)
  ;[chartCategory, chartTrend, chartStatus, chartGrowth, chartLevel, chartTop].forEach(ref => {
    if (!ref.value) return
    const c = echarts.getInstanceByDom(ref.value)
    if (c) c.dispose()
  })
})
</script>

<style scoped>
.dashboard-page {
  min-height: 100vh;
}

/* ── Header ── */
.page-header {
  position: relative;
  padding: 32px 0 24px;  /* 减小上下留白 */
  overflow: hidden;
  background: linear-gradient(160deg, #0a1018 0%, #12102a 40%, #0a1018 100%);
  border-bottom: 1px solid rgba(255,255,255,0.04);
}

.header-bg {
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.65' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)' opacity='0.04'/%3E%3C/svg%3E");
  opacity: 0.4;
  pointer-events: none;
}

.page-header .container {
  position: relative;
  z-index: 1;
}

.page-title {
  font-family: 'DM Serif Display', serif;
  font-size: 2rem;
  color: #fff;
  margin-bottom: 6px;
}

.page-subtitle {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.55);
}

/* ── Stat Cards ── */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin: 24px 0 24px;  /* 移除负 margin，header 和卡片之间正常间距 */
}

.stat-card {
  position: relative;
  background: var(--c-surface, #1e293b);
  border-radius: 10px;
  padding: 18px 20px;  /* 减小内边距 */
  display: flex;
  align-items: center;
  gap: 14px;
  border: 1px solid var(--c-border-light, #334155);
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.stat-glow {
  position: absolute;
  top: -50%;
  right: -50%;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, var(--card-color), transparent 70%);
  opacity: 0.06;
  pointer-events: none;
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: color-mix(in srgb, var(--card-color) 15%, transparent);
  color: var(--card-color);
  flex-shrink: 0;
}
.stat-icon svg {
  width: 20px;
  height: 20px;
}

.stat-body {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--c-text, #f1f5f9);
  line-height: 1.2;
  font-family: 'DM Sans', sans-serif;
}

.stat-label {
  font-size: 0.78rem;
  color: var(--c-text-muted, #94a3b8);
  margin-top: 2px;
}

/* ── Chart Cards ── */
.charts-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.chart-card {
  background: var(--c-surface, #1e293b);
  border-radius: 10px;
  border: 1px solid var(--c-border-light, #334155);
  padding: 16px;
}

.chart-header {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 12px;
}

.chart-header h3 {
  font-size: 0.92rem;
  font-weight: 600;
  color: var(--c-text, #f1f5f9);
  margin: 0;
}

.chart-subtitle {
  font-size: 0.75rem;
  color: var(--c-text-muted, #64748b);
}

.chart-container {
  width: 100%;
  height: 280px;
}

/* ── Responsive ── */
@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .charts-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  .page-header {
    padding: 32px 0 28px;
  }
  .page-title {
    font-size: 1.5rem;
  }
  .chart-container {
    height: 240px;
  }
}
</style>
