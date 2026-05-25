<template>
  <el-card shadow="hover" @click="$router.push(`/contest/${contest.id}`)" style="cursor: pointer">
    <div class="contest-card">
      <el-tag :type="statusType" size="small">{{ statusLabel }}</el-tag>
      <h3>{{ contest.name }}</h3>
      <p class="meta">
        <el-icon><Clock /></el-icon>
        截止：{{ contest.registerEndTime }}
      </p>
      <p class="meta">
        <el-icon><User /></el-icon>
        {{ contest.currentCount }} 人已报名
      </p>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  contest: { type: Object, required: true }
})

const statusMap = {
  0: { label: '未发布', type: 'info' },
  1: { label: '报名中', type: 'success' },
  2: { label: '已截止', type: 'warning' }
}

const statusLabel = computed(() => statusMap[props.contest.status]?.label || '未知')
const statusType = computed(() => statusMap[props.contest.status]?.type || 'info')
</script>

<style scoped>
.contest-card { line-height: 1.8; }
.contest-card h3 { margin: 8px 0; font-size: 16px; }
.meta { color: #666; font-size: 13px; }
</style>
